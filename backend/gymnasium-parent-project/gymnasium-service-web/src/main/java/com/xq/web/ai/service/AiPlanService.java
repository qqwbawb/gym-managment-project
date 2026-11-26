package com.xq.web.ai.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xq.web.ai.config.AiConfig;
import com.xq.web.ai.entity.AiChatMessage;
import com.xq.web.ai.entity.AiPlan;
import com.xq.web.ai.entity.AiPlanResult;
import com.xq.web.ai.entity.AiPlanVO;
import com.xq.web.ai.entity.AiTrainingLog;
import com.xq.web.ai.entity.AiUserProfile;
import com.xq.web.ai.mapper.AiChatMessageMapper;
import com.xq.web.ai.mapper.AiTrainingLogMapper;
import com.xq.web.ai.rule.ExerciseRuleEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 训练计划生成服务（核心）
 *
 * 执行流程：
 *   ① 查画像      -> ai_user_profile
 *   ② 查近期训练   -> ai_training_log（近 N 天）
 *   ③ 查对话历史   -> ai_chat_message（最近 N 轮）
 *   ④ 组装 Prompt
 *   ⑤ 调模型（要求纯 JSON）
 *   ⑥ JSON 解析（第二道防线）+ 规则校验/修复（第三道防线）
 *   ⑦ 存计划 + 存对话
 *   ⑧ 返回
 *
 * 关于「失败重试」：
 *   小模型（如本地 7B）JSON 输出稳定性一般。解析失败时不直接报错，
 *   而是把校验问题追加到 Prompt 里重试一次，仍失败才抛业务异常。
 *   这样既不让用户看到脏数据，也提高了小模型场景的可用性。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiPlanService {

    private final ChatClient chatClient;
    private final AiProfileService profileService;
    private final PromptBuilder promptBuilder;
    private final AiPlanPipeline pipeline;
    private final ExerciseRuleEngine ruleEngine;
    private final AiTrainingLogMapper trainingLogMapper;
    private final AiChatMessageMapper chatMessageMapper;
    private final AiConfig aiConfig;
    private final AiGenerationGuard generationGuard;

    /** 单条用户消息的最大长度（字符）。过长会烧 token 且挤掉有用的上下文 */
    public static final int MAX_MESSAGE_LENGTH = 500;

    /** 会话 id 最大长度，与 ai_chat_message.session_id 的 varchar(64) 保持一致 */
    public static final int MAX_SESSION_ID_LENGTH = 64;

    /** 近 N 天训练记录 */
    @Value("${ai.coach.recent-log-days:7}")
    private int recentLogDays;

    /** 最近对话轮数 */
    @Value("${ai.coach.recent-chat-rounds:5}")
    private int recentChatRounds;

    /** 单次模型调用允许的最长耗时（毫秒），与流式链路的 blockLast 超时保持一致 */
    @Value("${ai.coach.timeout-ms:120000}")
    private long timeoutMs;

    /** 解析失败时的最大重试次数（不含首次） */
    private static final int MAX_RETRY = 1;

    /**
     * 校验并规范化 sessionId。
     *
     * 为什么必须在入口校验：库里 session_id 是 varchar(64)，
     * 超长的值要等模型跑完（几十秒）才在 persist 插入时报 Data too long，
     * 用户看到 500、token 也白花了。入口直接挡掉最省事。
     */
    public static String validateSessionId(String sessionId) {
        if (StringUtils.isBlank(sessionId)) {
            return null;
        }
        String s = sessionId.trim();
        if (s.length() > MAX_SESSION_ID_LENGTH) {
            throw new IllegalArgumentException(
                    "会话标识过长（" + s.length() + " 字符），最多 " + MAX_SESSION_ID_LENGTH + " 字符");
        }
        return s;
    }

    /**
     * 校验用户消息：非空 + 不超长。
     * 两条链路（同步/流式）入口都调这里，避免只在一处校验。
     */
    public static void validateMessage(String userMessage) {
        if (StringUtils.isBlank(userMessage)) {
            throw new IllegalArgumentException("请描述你的训练需求");
        }
        if (userMessage.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(
                    "需求描述太长了（" + userMessage.length() + " 字），请控制在 "
                            + MAX_MESSAGE_LENGTH + " 字以内");
        }
    }

    /**
     * 生成训练计划（非流式，给前端 REST 调用）。
     *
     * 注意：本方法【不】标 @Transactional。
     * 里面包含 30-120 秒的模型 HTTP 调用，若包在事务里会一直占着数据库连接，
     * 并发几个请求就能把连接池（默认 10）打满。
     * 真正的 DB 写集中在 AiPlanPipeline#persist()，那里才是事务边界。
     */
    public AiPlanVO generate(Integer memberId, String userMessage) {
        long start = System.currentTimeMillis();
        validateMessage(userMessage);

        // ① 画像必须存在——画像来自表单，是事实数据，不让模型自己编
        AiUserProfile profile = profileService.getByMemberId(memberId);
        if (profile == null) {
            throw new IllegalStateException("请先填写健身画像，再让 AI 帮你制定计划");
        }

        String sessionId = UUID.randomUUID().toString();
        // 并发保护：同一会员同时只能跑一个生成任务，避免连点重复烧 token
        Long guardToken = generationGuard.tryAcquire(memberId);
        if (guardToken == null) {
            throw new IllegalStateException("正在生成中，请稍候…");
        }
        try {
            return doGenerate(memberId, profile, sessionId, userMessage, start);
        } finally {
            // 传凭证：若本任务已超时被别的请求接管，这里不会误删对方的锁
            generationGuard.release(memberId, guardToken);
        }
    }

    /**
     * 在已有会话中继续对话（多轮）。
     * 同样不标 @Transactional，理由见 generate()。
     */
    public AiPlanVO continueChat(Integer memberId, String sessionId, String userMessage) {
        long start = System.currentTimeMillis();
        validateMessage(userMessage);
        // 必须在调模型前校验：超长 sessionId 会等到 persist 插库时才报
        // Data too long，那时 token 已经烧掉了
        String safeSessionId = validateSessionId(sessionId);

        AiUserProfile profile = profileService.getByMemberId(memberId);
        if (profile == null) {
            throw new IllegalStateException("请先填写健身画像，再让 AI 帮你制定计划");
        }
        if (StringUtils.isBlank(safeSessionId)) {
            throw new IllegalArgumentException("sessionId 不能为空");
        }
        Long guardToken = generationGuard.tryAcquire(memberId);
        if (guardToken == null) {
            throw new IllegalStateException("正在生成中，请稍候…");
        }
        try {
            return doGenerate(memberId, profile, safeSessionId, userMessage, start);
        } finally {
            generationGuard.release(memberId, guardToken);
        }
    }

    private AiPlanVO doGenerate(Integer memberId, AiUserProfile profile,
                                String sessionId, String userMessage, long start) {
        // ⓪ 前置校验：在线模式下没配 API Key 就提前给出可操作的提示，
        //   而不是等调用时抛出底层异常（那样用户只看到一堆英文堆栈）
        if (aiConfig.isOpenAiProvider() && !aiConfig.isApiKeyConfigured()) {
            throw new IllegalStateException(
                    "AI 服务未配置 API Key，暂时无法生成计划。"
                            + "请在启动参数或系统环境变量中设置 DEEPSEEK_API_KEY 后重启应用。");
        }

        // ② 查近 N 天训练记录（记忆第二层）
        List<AiTrainingLog> recentLogs = listRecentLogs(memberId);

        // ③ 查最近 N 轮对话（记忆第三层）
        List<AiChatMessage> recentChats = listRecentChats(memberId, sessionId);

        // ④ 组装 Prompt
        String userPrompt = promptBuilder.buildUserPrompt(profile, recentLogs, recentChats, userMessage);
        List<String> injuries = splitCsv(profile.getInjuries());

        // ⑤ 调模型 + ⑥ 解析与校验（带一次重试）
        String raw = null;
        AiPlanResult result = null;
        List<String> violations = Collections.emptyList();

        for (int attempt = 0; attempt <= MAX_RETRY; attempt++) {
            String currentPrompt = attempt == 0
                    ? userPrompt
                    : promptBuilder.buildRetryUserPrompt(userPrompt, violations);

            raw = callModel(currentPrompt);
            result = parsePlan(raw);

            if (result == null) {
                violations = Collections.singletonList("上一次输出不是合法的 JSON 计划结构");
                log.warn("模型输出解析失败（第{}次），原始内容片段: {}", attempt + 1, brief(raw));
                continue;
            }

            // 第三道防线：确定性校验
            violations = ruleEngine.validate(result, injuries);
            if (violations.isEmpty()) {
                break;
            }
            log.warn("规则引擎发现 {} 处伤病冲突（第{}次）: {}", violations.size(), attempt + 1, violations);
        }

        if (result == null) {
            throw new IllegalStateException("AI 生成计划失败，请重试。若多次失败，可尝试换用在线模型");
        }

        // ⑥ 第三道防线：校验 + 修复（统一走 pipeline，与流式链路行为一致）
        List<String> warnings = pipeline.applyRules(result, profile);

        long cost = System.currentTimeMillis() - start;

        // ⑦ 存对话 + 存计划（pipeline 内部才是事务边界）
        AiPlan plan = pipeline.persist(memberId, sessionId, userMessage, result, warnings, cost);

        // ⑧ 组装返回
        AiPlanVO vo = new AiPlanVO();
        vo.setSessionId(sessionId);
        vo.setPlanId(plan.getPlanId());
        vo.setPlan(result);
        vo.setWarnings(warnings);
        vo.setCostMs(cost);
        vo.setRecommendCourses(pipeline.recommend(result, profile));
        return vo;
    }

    /**
     * 调用模型并返回原始文本。
     *
     * 注意：这里用 call() 而不是 entity()，因为我们要先拿到原始文本做日志与容错，
     * 并且在重试时要把原始输出反馈给模型。JSON 清洗在 parsePlan 里统一处理。
     */
    private String callModel(String userPrompt) {
        java.util.concurrent.CompletableFuture<String> future =
                java.util.concurrent.CompletableFuture.supplyAsync(() -> chatClient.prompt()
                        .system(PromptBuilder.SYSTEM_PROMPT)
                        .user(userPrompt)
                        .call()
                        .content(), modelExecutor);
        try {
            // 总时长兜底：即使 read-timeout 因为服务端慢速吐数据而没有触发，
            // 这里也能保证请求线程按时返回、guard 按时释放。
            return future.get(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException te) {
            future.cancel(true);
            log.error("调用 AI 模型超时（{} ms）", timeoutMs);
            throw new IllegalStateException("AI 响应超时，请稍后重试或换用更快的模型", te);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            throw new IllegalStateException("AI 调用被中断，请重试", ie);
        } catch (java.util.concurrent.ExecutionException ee) {
            Throwable cause = ee.getCause() == null ? ee : ee.getCause();
            log.error("调用 AI 模型失败", cause);
            throw new IllegalStateException("AI 服务调用失败：" + describeModelError(cause), cause);
        }
    }

    /**
     * 把模型调用的底层异常翻译成用户能看懂的一句话。
     *
     * 为什么需要：底层超时/连接异常很多是 message 为 null 或英文的
     * （例如 Netty 的 ReadTimeoutException、"Timeout on blocking read for 120000 MILLISECONDS"），
     * 直接拼给用户毫无意义。
     *
     * 做成 public static：同步（call）与流式（blockLast）两条链路共用同一套文案，
     * 避免一边中文一边英文。
     */
    public static String describeModelError(Throwable e) {
        if (e == null) {
            return "未知错误";
        }
        Throwable cur = e;
        while (cur != null) {
            String simpleName = cur.getClass().getSimpleName();
            String msg = cur.getMessage();

            if (simpleName.contains("Timeout") || (msg != null && msg.contains("Timeout on blocking read"))) {
                return "AI 响应超时，请稍后重试或换用更快的模型";
            }
            if (simpleName.contains("Connect") || simpleName.contains("UnknownHost")) {
                return "无法连接 AI 服务，请检查网络或 base-url 配置";
            }
            if (msg != null && !msg.trim().isEmpty()) {
                return msg;
            }
            cur = cur.getCause();
        }
        return e.getClass().getSimpleName();
    }

    /**
     * 同步调用模型的专用线程池。
     *
     * 为什么要单独起线程：Spring AI 的 call() 是阻塞调用，本身不接收超时参数。
     * 直接调的话一旦模型不返回，Tomcat 请求线程会一直挂着；
     * 更糟的是 AiGenerationGuard 4 分钟后强制接管，新请求进来又会并发执行，
     * 可能两份计划同时落库。所以这里用「提交到线程池 + get(timeout)」强制给总时长兜底。
     */
    private final java.util.concurrent.ExecutorService modelExecutor =
            java.util.concurrent.Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "ai-model-call");
                t.setDaemon(true);
                return t;
            });

    @jakarta.annotation.PreDestroy
    public void shutdownModelExecutor() {
        modelExecutor.shutdownNow();
    }

    /**
     * 第二道防线：JSON 解析。
     *
     * 处理模型常见的几种「不老实」：
     *   1. 包裹 ```json ``` 代码块
     *   2. JSON 前后有解释性文字
     *   3. 字段名对不上或缺失
     *
     * @return 解析失败返回 null（由调用方决定是否重试）
     */
    AiPlanResult parsePlan(String raw) {
        // 解析逻辑抽到 AiPlanParser（纯函数），便于用单测覆盖各种畸形输出，
        // 不必为了测一个字符串处理去启动 Spring 容器。
        return AiPlanParser.parse(raw);
    }

    // ---------------------------------------------------------------------
    // 数据访问
    // ---------------------------------------------------------------------

    /**
     * 查近 N 天训练记录，按日期倒序（最近的在前）。
     */
    List<AiTrainingLog> listRecentLogs(Integer memberId) {
        Date since = Date.from(LocalDate.now().minusDays(recentLogDays)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
        // 上界取「今天 23:59:59」：正常不会有未来日期（打卡时已拦截），
        // 但历史脏数据里可能存在，加上界可以保证它们不会混进「近期训练」影响计划生成。
        Date until = Date.from(LocalDate.now().plusDays(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
        LambdaQueryWrapper<AiTrainingLog> query = new LambdaQueryWrapper<>();
        query.eq(AiTrainingLog::getMemberId, memberId)
                .ge(AiTrainingLog::getTrainDate, since)
                .lt(AiTrainingLog::getTrainDate, until)
                .orderByDesc(AiTrainingLog::getTrainDate)
                .last("limit 10");
        return trainingLogMapper.selectList(query);
    }

    /**
     * 查最近 N 轮对话，按时间正序（保证 Prompt 里的顺序符合人类阅读习惯）。
     *
     * 说明：先倒序取 2N 条（一问一答算一轮），再反转。
     */
    List<AiChatMessage> listRecentChats(Integer memberId, String sessionId) {
        LambdaQueryWrapper<AiChatMessage> query = new LambdaQueryWrapper<>();
        query.eq(AiChatMessage::getMemberId, memberId)
                .eq(StringUtils.isNotBlank(sessionId), AiChatMessage::getSessionId, sessionId)
                .orderByDesc(AiChatMessage::getMessageId)
                .last("limit " + (recentChatRounds * 2));
        List<AiChatMessage> list = new ArrayList<>(chatMessageMapper.selectList(query));
        Collections.reverse(list);
        return list;
    }

    /**
     * 把逗号分隔的枚举串拆成列表。
     *
     * 包级可见 + static：画像的 equipment / injuries 都是这种格式，
     * AiPlanPipeline 与 AiChatStreamService 也要用，放在这里作为唯一的解析入口。
     */
    static List<String> splitCsv(String csv) {
        if (StringUtils.isBlank(csv)) {
            return Collections.emptyList();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String brief(String raw) {
        if (raw == null) {
            return "null";
        }
        return raw.length() <= 200 ? raw : raw.substring(0, 200) + "...";
    }
}
