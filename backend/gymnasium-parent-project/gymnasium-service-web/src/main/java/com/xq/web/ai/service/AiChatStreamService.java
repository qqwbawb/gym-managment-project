package com.xq.web.ai.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xq.web.ai.config.AiConfig;
import com.xq.web.ai.entity.AiChatMessage;
import com.xq.web.ai.entity.AiPlan;
import com.xq.web.ai.entity.AiPlanResult;
import com.xq.web.ai.entity.AiPlanVO;
import com.xq.web.ai.entity.AiTrainingLog;
import com.xq.web.ai.entity.AiUserProfile;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 流式对话服务（SSE）。
 *
 * 实现要点：
 *   1. 模型以流的方式输出增量文本，逐段推给前端做「打字机」效果；
 *   2. 模型吐出的是 JSON，边流边累积全文，结束后再统一解析；
 *   3. 解析之后的「校验 → 修复 → 存库 → 推荐」全部走 AiPlanPipeline，
 *      与同步链路共用同一份实现，避免两边行为分叉。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatStreamService {

    private final ChatClient chatClient;
    private final AiPlanService planService;
    private final AiProfileService profileService;
    private final PromptBuilder promptBuilder;
    private final AiPlanPipeline pipeline;
    private final AiConfig aiConfig;
    private final AiGenerationGuard generationGuard;

    /** 流式输出最长等待时间 */
    @Value("${ai.coach.timeout-ms:120000}")
    private long timeoutMs;

    /**
     * 流式任务线程池。
     *
     * 与之前 newFixedThreadPool 的区别：
     *   newFixedThreadPool 用的是【无界队列】，任务堆积时会无限吃内存直到 OOM；
     *   这里改成有界队列 + CallerRunsPolicy：队列满了由提交线程（Tomcat 工作线程）
     *   自己执行，形成天然的背压，而不是把内存撑爆。
     */
    private final ThreadPoolExecutor streamExecutor = new ThreadPoolExecutor(
            4, 4,
            60L, java.util.concurrent.TimeUnit.SECONDS,
            new java.util.concurrent.LinkedBlockingQueue<>(32),
            r -> {
                Thread t = new Thread(r, "ai-stream");
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy());

    @PreDestroy
    public void shutdown() {
        streamExecutor.shutdown();
        heartbeatScheduler.shutdownNow();
    }

    /** SSE 心跳间隔（秒）。要小于常见网关的 proxy_read_timeout（通常 60s） */
    private static final long HEARTBEAT_SECONDS = 15L;

    /**
     * 心跳调度器。
     * 用独立的单线程池：心跳只是定时发一行空事件，不能占用生成线程池的名额。
     */
    private final java.util.concurrent.ScheduledExecutorService heartbeatScheduler =
            java.util.concurrent.Executors.newScheduledThreadPool(1, r -> {
                Thread t = new Thread(r, "ai-sse-heartbeat");
                t.setDaemon(true);
                return t;
            });

    /**
     * 执行流式生成。
     */
    public SseEmitter streamPlan(Integer memberId, String sessionId, String userMessage) {
        // 入口先做参数校验，避免白占线程池名额
        String safeSessionId;
        try {
            AiPlanService.validateMessage(userMessage);
            safeSessionId = AiPlanService.validateSessionId(sessionId);
        } catch (IllegalArgumentException e) {
            return errorEmitter(e.getMessage());
        }

        // 并发保护：同一会员同时只能有一个生成任务（同步/流式共用同一把锁）
        Long guardToken = generationGuard.tryAcquire(memberId);
        if (guardToken == null) {
            return errorEmitter("正在生成中，请稍候…");
        }

        SseEmitter emitter = new SseEmitter(timeoutMs + 10_000L);
        String actualSessionId = StringUtils.isBlank(safeSessionId)
                ? UUID.randomUUID().toString() : safeSessionId;

        emitter.onTimeout(() -> {
            log.warn("SSE 超时, memberId={}", memberId);
            emitter.complete();
        });
        emitter.onError(e -> log.warn("SSE 异常: {}", e.getMessage()));

        try {
            streamExecutor.execute(() -> {
                try {
                    handle(emitter, memberId, actualSessionId, userMessage);
                } finally {
                    // 带凭证释放：本任务若已超时被接管，不会误删对方的锁
                    generationGuard.release(memberId, guardToken);
                }
            });
        } catch (RejectedExecutionException e) {
            // 有界队列 + CallerRuns 后正常不会走到这里，兜底防止线程池已关闭
            log.error("流式任务提交失败, memberId={}", memberId, e);
            generationGuard.release(memberId, guardToken);
            sendError(emitter, "AI 服务繁忙，请稍后重试");
            emitter.complete();
        }

        return emitter;
    }

    /**
     * 构造一个立即返回错误的 SseEmitter，让前端始终走同一套事件处理逻辑。
     */
    private SseEmitter errorEmitter(String message) {
        SseEmitter emitter = new SseEmitter(10_000L);
        sendError(emitter, message);
        emitter.complete();
        return emitter;
    }

    /**
     * 真正执行生成流程，运行在线程池里。
     */
    private void handle(SseEmitter emitter, Integer memberId, String actualSessionId, String userMessage) {
        long start = System.currentTimeMillis();

        // 客户端是否已断开。
        // 为什么需要它：前端点「停止」只是 abort 掉 fetch，后端 blockLast() 感知不到，
        // 模型会继续跑完并 persist()，造成 ①token 白烧 ②历史里多出一份用户没看到的计划。
        final java.util.concurrent.atomic.AtomicBoolean cancelled =
                new java.util.concurrent.atomic.AtomicBoolean(false);

        // SSE 心跳：模型思考阶段可能几十秒不吐 token，经过 nginx/网关时
        // 容易被 proxy_read_timeout 判定为空闲而断连。每 15 秒发一个 ping 保活。
        ScheduledFuture<?> heartbeat = heartbeatScheduler.scheduleAtFixedRate(() -> {
            if (cancelled.get()) {
                return;
            }
            try {
                emitter.send(SseEmitter.event().name("ping").data("{}"));
            } catch (Exception e) {
                // 发送失败说明连接已断，标记取消，让后续 doOnNext 终止模型流
                cancelled.set(true);
            }
        }, HEARTBEAT_SECONDS, HEARTBEAT_SECONDS, TimeUnit.SECONDS);

        emitter.onCompletion(() -> {
            cancelled.set(true);
            heartbeat.cancel(true);
        });
        emitter.onTimeout(() -> {
            cancelled.set(true);
            heartbeat.cancel(true);
        });
        emitter.onError(e -> {
            cancelled.set(true);
            heartbeat.cancel(true);
        });

        try {
            // ① 画像必须存在
            AiUserProfile profile = profileService.getByMemberId(memberId);
            if (profile == null) {
                sendError(emitter, "请先填写健身画像，再让 AI 帮你制定计划");
                emitter.complete();
                return;
            }

            // ② 前置校验：在线模式未配置 API Key 时直接提示，避免用户干等
            if (aiConfig.isOpenAiProvider() && !aiConfig.isApiKeyConfigured()) {
                sendError(emitter, "AI 服务未配置 API Key，暂时无法生成计划。"
                        + "请设置环境变量 DEEPSEEK_API_KEY 后重启应用。");
                emitter.complete();
                return;
            }

            // ③ 三层记忆检索
            List<AiTrainingLog> recentLogs = planService.listRecentLogs(memberId);
            List<AiChatMessage> recentChats = planService.listRecentChats(memberId, actualSessionId);
            String userPrompt = promptBuilder.buildUserPrompt(profile, recentLogs, recentChats, userMessage);

            // ④ 流式调用模型，同时累积全文
            StringBuilder fullText = new StringBuilder();
            chatClient.prompt()
                    .system(PromptBuilder.SYSTEM_PROMPT)
                    .user(userPrompt)
                    .stream()
                    .content()
                    .doOnNext(chunk -> {
                        // 客户端已断开就立刻终止：每收到一个 chunk 检查一次，
                        // 最多再多消耗一个 chunk 的 token，而不是把整份计划跑完。
                        if (cancelled.get()) {
                            throw new GenerationCancelledException();
                        }
                        if (!sendToken(emitter, chunk)) {
                            cancelled.set(true);
                            throw new GenerationCancelledException();
                        }
                        fullText.append(chunk);
                    })
                    .blockLast(java.time.Duration.ofMillis(timeoutMs));
            // ↑ 必须带超时：SseEmitter 的 onTimeout 只会 complete()，不会打断这个阻塞调用。
            //   如果模型永不返回且没有数据帧，blockLast() 会一直挂着，
            //   finally 里的 release 永远不执行 → 该会员被永久锁死。
            //   AiGenerationGuard 的超时兜底是第二层保险。

            // 走到这里说明客户端还在，若此时已取消则同样不再落库
            if (cancelled.get()) {
                log.info("客户端在生成完成前断开，跳过落库, memberId={}", memberId);
                return;
            }

            // ⑤ 解析
            String raw = fullText.toString();
            AiPlanResult result = planService.parsePlan(raw);
            if (result == null) {
                // 流式已经推给前端了，无法像同步链路那样重试（会造成内容错乱），
                // 这里只提示用户重试，并建议换用在线模型。
                sendError(emitter, "AI 输出无法解析为计划，请重试。若反复失败，可换用在线模型");
                emitter.complete();
                return;
            }

            // ⑥ 校验 + 修复（统一走 pipeline，与同步链路完全一致）
            List<String> warnings = pipeline.applyRules(result, profile);

            long cost = System.currentTimeMillis() - start;

            // ⑦ 存库（pipeline.persist 内部才是事务边界）
            AiPlan plan = pipeline.persist(memberId, actualSessionId, userMessage, result, warnings, cost);

            // ⑧ 推送结构化结果，前端用它替换打字机文本为表格。
            //    注意必须带上 recommendCourses，否则流式（默认路径）永远看不到课程推荐。
            JSONObject payload = new JSONObject();
            payload.put("sessionId", actualSessionId);
            payload.put("planId", plan.getPlanId());
            payload.put("plan", JSON.parseObject(JSON.toJSONString(result)));
            payload.put("warnings", warnings);
            payload.put("costMs", cost);
            payload.put("recommendCourses", JSON.toJSON(pipeline.recommend(result, profile)));
            emitter.send(SseEmitter.event().name("result").data(payload.toJSONString()));

            // 补发 done：前端以此判定「后端正常收尾」。
            // 只有连接意外中断时前端收不到 done，才能区分「正常完成」和「网络断了」——
            // 否则只要流读完就当作成功，断流时用户会看到半截内容却显示「就绪」。
            emitter.send(SseEmitter.event().name("done").data("{}"));

            emitter.complete();
        } catch (GenerationCancelledException e) {
            // 用户主动停止：不算错误，也【不落库】。
            // 这样既不浪费后续 token，也不会在历史里留下用户没见过的计划。
            log.info("用户取消生成，已终止模型流且不落库, memberId={}", memberId);
        } catch (Exception e) {
            if (cancelled.get()) {
                log.info("客户端已断开，终止生成, memberId={}", memberId);
                return;
            }
            log.error("流式生成失败, memberId={}", memberId, e);
            // 统一用公共翻译：blockLast 超时抛的是英文
            // "Timeout on blocking read for 120000 MILLISECONDS"，不能直接给用户看
            sendError(emitter, "AI 生成失败：" + AiPlanService.describeModelError(e));
            emitter.complete();
        } finally {
            heartbeat.cancel(true);
        }
    }

    /** 内部信号：客户端已断开/用户主动停止，用于中断模型流 */
    private static class GenerationCancelledException extends RuntimeException {
        GenerationCancelledException() {
            super("客户端已取消生成", null, false, false);
        }
    }

    /**
     * 推送一个 token。
     *
     * @return true 推送成功；false 表示连接已断（调用方据此终止模型流）
     */
    private boolean sendToken(SseEmitter emitter, String chunk) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("text", chunk);
            emitter.send(SseEmitter.event().name("token").data(payload.toJSONString()));
            return true;
        } catch (Exception e) {
            log.debug("推送 token 失败，客户端可能已断开: {}", e.getMessage());
            return false;
        }
    }

    private void sendError(SseEmitter emitter, String message) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("message", message);
            emitter.send(SseEmitter.event().name("error").data(payload.toJSONString()));
        } catch (Exception e) {
            log.debug("推送 error 失败: {}", e.getMessage());
        }
    }
}
