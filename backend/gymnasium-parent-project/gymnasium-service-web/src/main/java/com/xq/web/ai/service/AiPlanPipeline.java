package com.xq.web.ai.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xq.web.ai.entity.AiChatMessage;
import com.xq.web.ai.entity.AiPlan;
import com.xq.web.ai.entity.AiPlanResult;
import com.xq.web.ai.entity.AiPlanVO;
import com.xq.web.ai.entity.AiUserProfile;
import com.xq.web.ai.mapper.AiChatMessageMapper;
import com.xq.web.ai.mapper.AiPlanMapper;
import com.xq.web.ai.rule.ExerciseRuleEngine;
import com.xq.web.course.entity.Course;
import com.xq.web.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 计划生成的公共管道：校验 → 修复 → 存库 → 推荐。
 *
 * 为什么要有这个类：
 *   原先同步链路（AiPlanService）与流式链路（AiChatStreamService）
 *   各自实现了一遍「规则校验 + 修复 + 存库 + 推荐」，导致两边行为逐渐分叉：
 *     - 流式链路修复时传的不可用器械是空列表，可能把受伤动作换成会员没有的器械；
 *     - 流式链路忘了返回课程推荐；
 *     - 两边的 model 字段、助手回复文案不一致。
 *   维护两份逻辑早晚出错，这里抽成一份，两条链路都调它。
 *
 * 事务边界（重要）：
 *   本类只有 persist() 标 @Transactional，且内部不含任何远程调用。
 *   模型调用（30-120 秒的 HTTP 请求）由调用方在事务外执行，
 *   避免长时间占用数据库连接把连接池打满。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiPlanPipeline {

    private final AiChatMessageMapper chatMessageMapper;
    private final AiPlanMapper planMapper;
    private final ExerciseRuleEngine ruleEngine;
    private final CourseService courseService;

    /** 是否允许自动修复伤病冲突 */
    @Value("${ai.coach.auto-fix-conflict:true}")
    private boolean autoFixConflict;

    @Value("${spring.ai.model.chat:openai}")
    private String provider;

    @Value("${spring.ai.ollama.chat.options.model:qwen2.5:7b}")
    private String ollamaModel;

    @Value("${spring.ai.openai.chat.options.model:deepseek-flash}")
    private String openaiModel;

    /**
     * 应用规则引擎：先校验，有冲突就修复，再校验残留冲突。
     *
     * 两条链路都必须走这里，保证「是否允许修复」和「不可用器械过滤」行为一致。
     *
     * @param result  计划（原地修改）
     * @param profile 会员画像（提供伤病与器械）
     * @return 合并后的告警列表：修复说明 + 修复后仍残留的冲突
     */
    public List<String> applyRules(AiPlanResult result, AiUserProfile profile) {
        List<String> injuries = AiPlanService.splitCsv(profile.getInjuries());
        List<String> violations = ruleEngine.validate(result, injuries);
        if (violations.isEmpty()) {
            return Collections.emptyList();
        }

        if (!autoFixConflict) {
            // 配置关闭自动修复时，只如实上报冲突，不改计划
            return violations;
        }

        // 关键：必须按会员实际拥有的器械过滤替代动作，
        // 否则可能把「深蹲」换成「杠铃臀推」而该会员根本没有杠铃。
        List<String> unavailable = unavailableEquipment(profile.getEquipment());
        List<String> fixNotes = ruleEngine.fix(result, injuries, unavailable);

        List<String> merged = new ArrayList<>(fixNotes);
        merged.addAll(ruleEngine.validate(result, injuries));
        return merged;
    }

    /**
     * 持久化对话与计划。
     *
     * 事务只包住 DB 写，不包含模型调用。
     *
     * @return 已落库的计划（含 planId）
     */
    @Transactional(rollbackFor = Exception.class)
    public AiPlan persist(Integer memberId, String sessionId, String userMessage,
                          AiPlanResult result, List<String> warnings, long cost) {
        saveMessage(memberId, sessionId, AiChatMessage.ROLE_USER, userMessage);
        saveMessage(memberId, sessionId, AiChatMessage.ROLE_ASSISTANT,
                buildAssistantReply(result, warnings));
        return savePlan(memberId, sessionId, result, warnings, cost);
    }

    /**
     * 根据计划涉及的身体部位推荐现有课程。
     *
     * 边界说明：这里只做「推荐展示」，点击后跳转课程页由会员本人决定是否报名。
     * AI 不会也不能替会员报名或扣费——报名必须走 MemberCourseService.joinCourse
     * 那条带事务与余额校验的正式链路。
     */
    public List<AiPlanVO.RecommendCourse> recommend(AiPlanResult result, AiUserProfile profile) {
        if (result == null || result.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> keywords = new ArrayList<>();
        for (AiPlanResult.DayPlan day : result.getDays()) {
            String bp = day.getBodyPart() == null ? "" : day.getBodyPart();
            switch (bp) {
                case "CARDIO":
                    keywords.addAll(Arrays.asList("单车", "有氧", "游泳"));
                    break;
                case "LEG":
                    keywords.addAll(Arrays.asList("单车", "普拉提"));
                    break;
                case "CORE":
                    keywords.addAll(Arrays.asList("普拉提", "瑜伽"));
                    break;
                case "CHEST":
                case "BACK":
                case "SHOULDER":
                    keywords.addAll(Arrays.asList("搏击", "力量"));
                    break;
                default:
                    break;
            }
        }
        if ("LOSE_FAT".equals(profile.getGoal())) {
            keywords.add("单车");
        }
        if (keywords.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Course> query = new LambdaQueryWrapper<>();
        query.and(w -> {
            for (int i = 0; i < keywords.size(); i++) {
                if (i == 0) {
                    w.like(Course::getCourseName, keywords.get(i));
                } else {
                    w.or().like(Course::getCourseName, keywords.get(i));
                }
            }
        }).last("limit 3");

        return courseService.list(query).stream().map(c -> {
            AiPlanVO.RecommendCourse rc = new AiPlanVO.RecommendCourse();
            rc.setCourseId(c.getCourseId());
            rc.setCourseName(c.getCourseName());
            rc.setTeacherName(c.getTeacherName());
            rc.setImage(c.getImage());
            rc.setCourseHour(c.getCourseHour());
            rc.setCoursePrice(c.getCoursePrice());
            return rc;
        }).collect(Collectors.toList());
    }

    /**
     * 当前实际使用的模型名，用于成本观测与前端展示。
     * 两条链路共用，避免一边写 "stream"、一边写真实模型名。
     */
    public String currentModelName() {
        return "openai".equalsIgnoreCase(provider) ? openaiModel : ollamaModel;
    }

    /**
     * 给用户看的助手回复（存进对话历史，也作为多轮上下文的一部分）。
     */
    public String buildAssistantReply(AiPlanResult result, List<String> warnings) {
        StringBuilder sb = new StringBuilder();
        sb.append("已生成《").append(result.getTitle()).append("》：");
        sb.append(ruleEngine.summarize(result, 0)).append("。");
        if (StringUtils.isNotBlank(result.getNotes())) {
            sb.append(result.getNotes());
        }
        if (warnings != null && !warnings.isEmpty()) {
            sb.append("（已按你的伤病情况调整：").append(String.join("；", warnings)).append("）");
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------------

    private void saveMessage(Integer memberId, String sessionId, String role, String content) {
        AiChatMessage msg = new AiChatMessage();
        msg.setMemberId(memberId);
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        chatMessageMapper.insert(msg);
    }

    private AiPlan savePlan(Integer memberId, String sessionId, AiPlanResult result,
                            List<String> warnings, long cost) {
        String planJson = JSON.toJSONString(result);
        AiPlan plan = new AiPlan();
        plan.setMemberId(memberId);
        plan.setSessionId(sessionId);
        // title 列是 varchar(128)。模型偶尔给出超长标题，不截断会 Data too long，
        // 整个事务回滚，用户等了两分钟只看到「AI 生成失败」。
        plan.setTitle(truncate(result.getTitle(), 128));
        plan.setPlanJson(planJson);
        plan.setSummary(truncate(ruleEngine.summarize(result, 0), 500));
        plan.setWarnings(JSON.toJSONString(warnings == null ? Collections.emptyList() : warnings));
        plan.setModel(currentModelName());
        // token 消耗按字符数估算，不为了精确统计而多调一次模型
        plan.setTokenUsage(Math.max(1, planJson.length() / 2));
        plan.setCostMs((int) Math.min(cost, Integer.MAX_VALUE));
        planMapper.insert(plan);
        return plan;
    }

    /** 安全截断：null 原样返回，超长才裁剪 */
    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    /**
     * 计算会员「没有的」器械关键词，用于过滤替代动作。
     */
    private List<String> unavailableEquipment(String ownedCsv) {
        List<String> owned = AiPlanService.splitCsv(ownedCsv);
        List<String> unavailable = new ArrayList<>();
        if (!owned.contains("BARBELL")) {
            unavailable.add("杠铃");
        }
        if (!owned.contains("DUMBBELL")) {
            unavailable.add("哑铃");
        }
        if (!owned.contains("CABLE")) {
            unavailable.add("绳索");
            unavailable.add("龙门");
        }
        return unavailable;
    }
}
