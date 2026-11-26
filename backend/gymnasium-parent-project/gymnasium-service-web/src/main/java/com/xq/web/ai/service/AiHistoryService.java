package com.xq.web.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xq.web.ai.entity.AiChatMessage;
import com.xq.web.ai.entity.AiPlan;
import com.xq.web.ai.mapper.AiChatMessageMapper;
import com.xq.web.ai.mapper.AiPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 历史计划与会话查询。
 *
 * 所有查询都强制带 memberId 条件（取自登录态），
 * 保证会员只能看到自己的数据。
 */
@Service
@RequiredArgsConstructor
public class AiHistoryService {

    private final AiPlanMapper planMapper;
    private final AiChatMessageMapper chatMessageMapper;

    /** 单页最大条数，防止前端传超大 size 一次拉全表 */
    private static final long MAX_PAGE_SIZE = 50L;

    /**
     * 分页查询我的历史计划（列表不含 plan_json，避免一次拉太多）。
     */
    public IPage<AiPlan> pageMyPlans(Integer memberId, long current, long size) {
        // 与训练记录一致：clamp 到上限，防止 size=100000 把全表拉回来
        long safeSize = Math.min(Math.max(size, 1L), MAX_PAGE_SIZE);
        long safeCurrent = Math.max(current, 1L);
        Page<AiPlan> page = new Page<>(safeCurrent, safeSize);
        LambdaQueryWrapper<AiPlan> query = new LambdaQueryWrapper<>();
        query.eq(AiPlan::getMemberId, memberId)
                .select(AiPlan::getPlanId, AiPlan::getTitle, AiPlan::getSummary,
                        AiPlan::getModel, AiPlan::getTokenUsage, AiPlan::getCostMs,
                        AiPlan::getCreateTime, AiPlan::getSessionId, AiPlan::getWarnings)
                .orderByDesc(AiPlan::getPlanId);
        return planMapper.selectPage(page, query);
    }

    /**
     * 查询某条计划的详情（含 plan_json）。
     */
    public AiPlan getMyPlan(Integer memberId, Integer planId) {
        LambdaQueryWrapper<AiPlan> query = new LambdaQueryWrapper<>();
        query.eq(AiPlan::getPlanId, planId)
                .eq(AiPlan::getMemberId, memberId);
        return planMapper.selectOne(query);
    }

    /**
     * 查询某个会话的完整对话记录。
     */
    public List<AiChatMessage> listSessionMessages(Integer memberId, String sessionId) {
        LambdaQueryWrapper<AiChatMessage> query = new LambdaQueryWrapper<>();
        query.eq(AiChatMessage::getMemberId, memberId)
                .eq(AiChatMessage::getSessionId, sessionId)
                .orderByAsc(AiChatMessage::getMessageId);
        return chatMessageMapper.selectList(query);
    }

    /**
     * 查询我的最近会话列表（按最后一条消息倒序去重）。
     */
    public List<AiChatMessage> listRecentSessions(Integer memberId, int limit) {
        LambdaQueryWrapper<AiChatMessage> query = new LambdaQueryWrapper<>();
        query.eq(AiChatMessage::getMemberId, memberId)
                .orderByDesc(AiChatMessage::getMessageId)
                .last("limit " + Math.min(Math.max(limit, 1), 100));
        return chatMessageMapper.selectList(query);
    }
}
