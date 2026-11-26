package com.xq.web.ai.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.ai.entity.AiChatParam;
import com.xq.web.ai.entity.AiPlanVO;
import com.xq.web.ai.service.AiChatStreamService;
import com.xq.web.ai.service.AiPlanService;
import com.xq.web.ai.service.CurrentMemberResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 对话接口。
 *
 * 提供两种模式：
 *   POST /api/ai/chat        同步返回完整计划（简单、兼容性好，前端一次性渲染）
 *   POST /api/ai/chat/stream SSE 流式输出（打字机效果，用户能更早看到内容）
 *
 * 安全约定：memberId 一律取自登录态。
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize(
        "hasAnyAuthority('sys:aiCoach','sys:aiProfile','sys:aiPlanList')")
public class AiChatController {

    private final AiPlanService planService;
    private final AiChatStreamService streamService;
    private final CurrentMemberResolver memberResolver;

    /**
     * 同步生成计划。
     *
     * 请求体：{ "sessionId": "可选，不传则新建", "message": "帮我排下周三天的计划" }
     */
    @PostMapping("/chat")
    public ResultVo chat(@RequestBody AiChatParam param) {
        Integer memberId;
        try {
            memberId = memberResolver.requireMemberId();
        } catch (IllegalStateException e) {
            return ResultUtils.error(e.getMessage());
        }
        if (param == null || StringUtils.isBlank(param.getMessage())) {
            return ResultUtils.error("请描述你的训练需求");
        }
        try {
            AiPlanVO vo = StringUtils.isBlank(param.getSessionId())
                    ? planService.generate(memberId, param.getMessage())
                    : planService.continueChat(memberId, param.getSessionId(), param.getMessage());
            return ResultUtils.success("生成成功", vo);
        } catch (IllegalStateException | IllegalArgumentException e) {
            // 画像未填写 / 模型调用失败 等业务异常，返回友好提示
            return ResultUtils.error(e.getMessage());
        }
    }

    /**
     * 流式生成计划（SSE）。
     *
     * 事件类型：
     *   token  : 模型输出的增量文本，前端逐字渲染
     *   result : 完整结构化计划（JSON），前端收到后用表格替换原始文本
     *   error  : 出错信息
     *   done   : 结束标记
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody AiChatParam param) {
        Integer memberId;
        try {
            memberId = memberResolver.requireMemberId();
        } catch (IllegalStateException e) {
            // 认证类问题直接把错误放进流里，前端能统一处理
            return errorEmitter(e.getMessage());
        }
        if (param == null || StringUtils.isBlank(param.getMessage())) {
            return errorEmitter("请描述你的训练需求");
        }
        return streamService.streamPlan(memberId, param.getSessionId(), param.getMessage());
    }

    /**
     * 构造一个立即返回错误的 SseEmitter，保证前端总是走同一条事件处理逻辑。
     */
    private SseEmitter errorEmitter(String message) {
        SseEmitter emitter = new SseEmitter(10_000L);
        try {
            JSONObject payload = new JSONObject();
            payload.put("message", message);
            emitter.send(SseEmitter.event().name("error").data(payload.toJSONString()));
            emitter.complete();
        } catch (Exception ex) {
            emitter.completeWithError(ex);
        }
        return emitter;
    }
}
