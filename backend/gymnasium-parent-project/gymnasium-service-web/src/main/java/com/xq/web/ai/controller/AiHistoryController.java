package com.xq.web.ai.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.ai.entity.AiChatMessage;
import com.xq.web.ai.entity.AiPlan;
import com.xq.web.ai.entity.AiPlanResult;
import com.xq.web.ai.entity.AiTrainingLog;
import com.xq.web.ai.entity.AiTrainingLogParam;
import com.xq.web.ai.service.AiHistoryService;
import com.xq.web.ai.service.AiPlanService;
import com.xq.web.ai.service.AiTrainingLogService;
import com.xq.web.ai.service.CurrentMemberResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 历史计划与训练记录接口。
 *
 * 安全约定：memberId 一律取自登录态，查询条件强制带 memberId。
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize(
        "hasAnyAuthority('sys:aiCoach','sys:aiProfile','sys:aiPlanList')")
public class AiHistoryController {

    private final AiHistoryService historyService;
    private final AiTrainingLogService trainingLogService;
    private final CurrentMemberResolver memberResolver;

    /**
     * 我的历史计划（分页，列表不含完整 JSON）。
     */
    @GetMapping("/plans")
    public ResultVo pageMyPlans(@RequestParam(defaultValue = "1") long current,
                                @RequestParam(defaultValue = "10") long size) {
        Integer memberId = memberIdOrNull();
        if (memberId == null) {
            return ResultUtils.error("请使用会员账号登录");
        }
        IPage<AiPlan> page = historyService.pageMyPlans(memberId, current, size);
        return ResultUtils.success("查询成功", page);
    }

    /**
     * 计划详情（含结构化 plan）。
     */
    @GetMapping("/plans/{planId}")
    public ResultVo getPlanDetail(@PathVariable("planId") Integer planId) {
        Integer memberId = memberIdOrNull();
        if (memberId == null) {
            return ResultUtils.error("请使用会员账号登录");
        }
        AiPlan plan = historyService.getMyPlan(memberId, planId);
        if (plan == null) {
            return ResultUtils.error("计划不存在或无权查看");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("planId", plan.getPlanId());
        data.put("title", plan.getTitle());
        data.put("sessionId", plan.getSessionId());
        data.put("model", plan.getModel());
        data.put("tokenUsage", plan.getTokenUsage());
        data.put("costMs", plan.getCostMs());
        data.put("createTime", plan.getCreateTime());
        data.put("warnings", parseWarnings(plan.getWarnings()));
        data.put("plan", StringUtils.isBlank(plan.getPlanJson())
                ? null : JSON.parseObject(plan.getPlanJson(), AiPlanResult.class));
        return ResultUtils.success("查询成功", data);
    }

    /**
     * 某个会话的完整对话记录（前端恢复多轮对话上下文用）。
     */
    @GetMapping("/messages")
    public ResultVo listMessages(@RequestParam("sessionId") String sessionId) {
        Integer memberId = memberIdOrNull();
        if (memberId == null) {
            return ResultUtils.error("请使用会员账号登录");
        }
        List<AiChatMessage> list = historyService.listSessionMessages(memberId, sessionId);
        return ResultUtils.success("查询成功", list);
    }

    /**
     * 训练打卡：会员完成一次训练后记录一条，作为「记忆」的来源。
     */
    @PostMapping("/training-log")
    public ResultVo addTrainingLog(@RequestBody AiTrainingLogParam param) {
        Integer memberId = memberIdOrNull();
        if (memberId == null) {
            return ResultUtils.error("请使用会员账号登录");
        }
        try {
            trainingLogService.addLog(memberId, param);
            return ResultUtils.success("打卡成功，AI 下次会参考这次训练");
        } catch (IllegalArgumentException e) {
            return ResultUtils.error(e.getMessage());
        }
    }

    /**
     * 我的近期训练记录。
     */
    @GetMapping("/training-log")
    public ResultVo listTrainingLog(@RequestParam(defaultValue = "10") int limit) {
        Integer memberId = memberIdOrNull();
        if (memberId == null) {
            return ResultUtils.error("请使用会员账号登录");
        }
        List<AiTrainingLog> list = trainingLogService.listRecent(memberId, limit);
        return ResultUtils.success("查询成功", list);
    }

    /**
     * 删除自己的一条训练记录（误打卡时用，避免污染 AI 记忆）。
     */
    @DeleteMapping("/training-log/{logId}")
    public ResultVo deleteTrainingLog(@PathVariable("logId") Integer logId) {
        Integer memberId = memberIdOrNull();
        if (memberId == null) {
            return ResultUtils.error("请使用会员账号登录");
        }
        try {
            trainingLogService.deleteLog(memberId, logId);
            return ResultUtils.success("删除成功");
        } catch (IllegalArgumentException e) {
            return ResultUtils.error(e.getMessage());
        }
    }

    private Integer memberIdOrNull() {
        try {
            return memberResolver.requireMemberId();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private List<String> parseWarnings(String warningsJson) {
        if (StringUtils.isBlank(warningsJson)) {
            return java.util.Collections.emptyList();
        }
        try {
            return JSON.parseArray(warningsJson, String.class);
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
}
