package com.xq.web.ai.rule;

import com.xq.web.ai.entity.AiPlanResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 规则引擎单元测试。
 *
 * 这是整个 AI 模块最值得测的部分：
 *   「模型输出不可信 → 用确定性的规则引擎兜底」这个论点，
 *   只有配上测试才算真的成立，否则规则引擎自己也可能写错。
 */
class ExerciseRuleEngineTest {

    private final ExerciseRuleEngine engine = new ExerciseRuleEngine();

    // ------------------------------------------------------------------
    // 校验
    // ------------------------------------------------------------------

    @Test
    @DisplayName("膝伤会员被排深蹲 → 校验应发现冲突")
    void validateDetectsKneeConflict() {
        AiPlanResult plan = planOf(day(1, "LEG", "杠铃深蹲"));

        List<String> violations = engine.validate(plan, Collections.singletonList("KNEE"));

        assertEquals(1, violations.size());
        assertTrue(violations.get(0).contains("深蹲"));
        assertTrue(violations.get(0).contains("膝关节"));
    }

    @Test
    @DisplayName("无伤病会员 → 校验不产生任何冲突")
    void validateIgnoresWhenNoInjury() {
        AiPlanResult plan = planOf(day(1, "LEG", "杠铃深蹲"));

        List<String> violations = engine.validate(plan, new ArrayList<>());

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("多部位伤病 → 各部位冲突都能被识别")
    void validateHandlesMultipleInjuries() {
        AiPlanResult plan = planOf(
                day(1, "LEG", "深蹲"),
                day(2, "CHEST", "卧推"),
                day(3, "BACK", "硬拉"));

        List<String> violations = engine.validate(plan,
                Arrays.asList("KNEE", "SHOULDER", "LOWER_BACK"));

        assertEquals(3, violations.size());
    }

    // ------------------------------------------------------------------
    // 修复
    // ------------------------------------------------------------------

    @Test
    @DisplayName("修复后不应再存在与该伤病冲突的动作")
    void fixRemovesAllConflicts() {
        AiPlanResult plan = planOf(
                day(1, "LEG", "杠铃深蹲"),
                day(2, "LEG", "哑铃弓步蹲"));

        engine.fix(plan, Collections.singletonList("KNEE"), new ArrayList<>());

        assertTrue(engine.validate(plan, Collections.singletonList("KNEE")).isEmpty(),
                "修复后仍有冲突");
        assertFalse(plan.isEmpty(), "计划不应被整体清空");
    }

    @Test
    @DisplayName("同一个计划内不重复使用同一个替代动作")
    void fixDoesNotRepeatSameAlternative() {
        AiPlanResult plan = planOf(
                day(1, "LEG", "杠铃深蹲"),
                day(2, "LEG", "哑铃弓步蹲"),
                day(3, "LEG", "腿举"));

        engine.fix(plan, Collections.singletonList("KNEE"), new ArrayList<>());

        List<String> names = new ArrayList<>();
        plan.getDays().forEach(d -> d.getExercises().forEach(e -> names.add(e.getName())));

        // 三个动作原本冲突，修复后名字应互不相同（或部分被移除）
        long distinct = names.stream().distinct().count();
        assertEquals(names.size(), distinct,
                "出现了重复的替代动作：" + names);
    }

    @Test
    @DisplayName("有氧动作被替换为力量动作时，组数/次数/休息应被重置为合理值")
    void fixNormalizesParametersWhenReplacingTimedExercise() {
        AiPlanResult plan = new AiPlanResult();
        AiPlanResult.DayPlan d = new AiPlanResult.DayPlan();
        d.setDay(1);
        d.setBodyPart("CARDIO");
        AiPlanResult.Exercise e = new AiPlanResult.Exercise();
        e.setName("跑步机快走");   // 命中膝伤禁忌词「跑」
        e.setSets(1);
        e.setReps("25-30分钟");
        e.setRestSec(0);
        d.setExercises(new ArrayList<>(Collections.singletonList(e)));
        plan.setDays(new ArrayList<>(Collections.singletonList(d)));

        engine.fix(plan, Collections.singletonList("KNEE"), new ArrayList<>());

        AiPlanResult.Exercise fixed = plan.getDays().get(0).getExercises().get(0);
        // 替换后的动作不能仍然是「25-30分钟 / 0秒休息」这种力量动作无法执行的参数
        assertFalse(fixed.getReps() != null && fixed.getReps().contains("分钟")
                        && fixed.getRestSec() != null && fixed.getRestSec() == 0,
                "力量动作沿用了有氧的时长参数：" + fixed.getReps() + " / rest=" + fixed.getRestSec());
    }

    @Test
    @DisplayName("会员没有杠铃时，替代动作不应依赖杠铃")
    void fixRespectsUnavailableEquipment() {
        AiPlanResult plan = planOf(day(1, "LEG", "杠铃深蹲"));
        List<String> unavailable = new ArrayList<>(Collections.singletonList("杠铃"));

        engine.fix(plan, Collections.singletonList("KNEE"), unavailable);

        for (AiPlanResult.DayPlan d : plan.getDays()) {
            for (AiPlanResult.Exercise e : d.getExercises()) {
                assertFalse(e.getName().contains("杠铃"),
                        "替代动作依赖了会员没有的器械：" + e.getName());
            }
        }
    }

    // ------------------------------------------------------------------
    // 摘要
    // ------------------------------------------------------------------

    @Test
    @DisplayName("摘要应包含训练天数与动作总数")
    void summarizeContainsDaysAndExerciseCount() {
        AiPlanResult plan = planOf(
                day(1, "CHEST", "卧推"),
                day(2, "BACK", "划船"));

        String summary = engine.summarize(plan, 0);

        assertTrue(summary.contains("每周2练"));
        assertTrue(summary.contains("2个动作"));
    }

    // ------------------------------------------------------------------
    // 数据一致性：替代动作不能被自己的禁忌词命中
    // ------------------------------------------------------------------

    @Test
    @DisplayName("每个伤病的替代动作候选都不能被该伤病的禁忌词命中（否则该候选永远不可用）")
    @SuppressWarnings("unchecked")
    void alternativesMustNotBeForbiddenBySameInjury() throws Exception {
        Field forbiddenField = ExerciseRuleEngine.class.getDeclaredField("FORBIDDEN");
        Field altField = ExerciseRuleEngine.class.getDeclaredField("ALTERNATIVES");
        forbiddenField.setAccessible(true);
        altField.setAccessible(true);

        java.util.Map<String, java.util.Set<String>> forbidden =
                (java.util.Map<String, java.util.Set<String>>) forbiddenField.get(null);
        java.util.Map<String, java.util.List<String>> alternatives =
                (java.util.Map<String, java.util.List<String>>) altField.get(null);

        List<String> problems = new ArrayList<>();
        for (java.util.Map.Entry<String, java.util.List<String>> entry : alternatives.entrySet()) {
            String injury = entry.getKey();
            java.util.Set<String> banned = forbidden.getOrDefault(injury, java.util.Collections.emptySet());
            for (String candidate : entry.getValue()) {
                for (String keyword : banned) {
                    if (candidate.contains(keyword)) {
                        problems.add(injury + " 的替代候选「" + candidate
                                + "」被自己的禁忌词「" + keyword + "」命中，永远不会被选中");
                    }
                }
            }
        }
        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }

    // ------------------------------------------------------------------
    // 构造辅助
    // ------------------------------------------------------------------

    private AiPlanResult planOf(AiPlanResult.DayPlan... days) {
        AiPlanResult plan = new AiPlanResult();
        plan.setTitle("测试计划");
        plan.setDays(new ArrayList<>(Arrays.asList(days)));
        return plan;
    }

    private AiPlanResult.DayPlan day(int no, String bodyPart, String exerciseName) {
        AiPlanResult.DayPlan d = new AiPlanResult.DayPlan();
        d.setDay(no);
        d.setBodyPart(bodyPart);
        AiPlanResult.Exercise e = new AiPlanResult.Exercise();
        e.setName(exerciseName);
        e.setSets(3);
        e.setReps("10-12");
        e.setRestSec(60);
        d.setExercises(new ArrayList<>(Collections.singletonList(e)));
        return d;
    }
}
