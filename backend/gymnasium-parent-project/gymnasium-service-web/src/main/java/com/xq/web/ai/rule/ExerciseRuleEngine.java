package com.xq.web.ai.rule;

import com.xq.web.ai.entity.AiPlanResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 动作规则引擎（三层防线的第三道）
 *
 * 为什么需要它：
 *   Prompt 里的约束是「软」的——模型输出是概率性的，同样的指令它可能 9 次遵守 1 次违反。
 *   工程上不能赌模型行为，所以必须在代码层再做一次确定性校验。
 *
 * 本类做两件事：
 *   1. validate：扫描计划中每个动作，找出与会员伤病部位冲突的动作；
 *   2. fix：把冲突动作替换成安全替代动作（而不是像很多实现那样直接 return plan）。
 *
 * 设计取舍：
 *   - 用「部位 -> 动作关键词」而不是完整动作库，因为动作名称千变万化，
 *     精确匹配会漏判；关键词包含匹配覆盖面更大。
 *   - 替代动作同样要过滤：如果替代动作本身也命中禁忌，就丢弃该动作，
 *     并记一条告警让用户知道「这里被删了」，而不是静默吞掉。
 */
@Slf4j
@Component
public class ExerciseRuleEngine {

    /**
     * 伤病部位 -> 该部位有伤时应避免的动作关键词。
     *
     * 关键词取动作名的核心词（如「深蹲」能同时命中「杠铃深蹲」「哑铃深蹲」「保加利亚深蹲」）。
     */
    private static final Map<String, Set<String>> FORBIDDEN = new HashMap<>();

    /**
     * 伤病部位 -> 安全替代动作候选（按顺序取第一个可用的）。
     */
    private static final Map<String, List<String>> ALTERNATIVES = new HashMap<>();

    /** 部位中文名，用于生成可读的告警文案 */
    private static final Map<String, String> INJURY_LABEL = new HashMap<>();

    /** 训练部位 -> 中文名 */
    private static final Map<String, String> BODY_PART_LABEL = new HashMap<>();

    /** 训练部位 -> 该部位常见动作关键词（用于让替代动作落在同一天的主题内） */
    private static final Map<String, Set<String>> BODY_PART_HINTS = new HashMap<>();

    /** 有氧/计时类动作关键词（替换后需要重置参数） */
    private static final Set<String> TIMED_KEYWORDS = new HashSet<>(Arrays.asList(
            "椭圆机", "单车", "跑步机", "快走", "慢跑", "有氧", "划船机", "跳绳", "波比"));

    static {
        // ---------- 膝 ----------
        FORBIDDEN.put("KNEE", new HashSet<>(Arrays.asList(
                "深蹲", "弓步", "箭步", "跑", "跳", "登山", "腿举", "腿屈伸", "腿弯举", "开合跳", "跳绳"
        )));
        ALTERNATIVES.put("KNEE", Arrays.asList("臀桥", "靠墙静蹲", "直腿硬拉(轻重量)"));

        // ---------- 肩 ----------
        FORBIDDEN.put("SHOULDER", new HashSet<>(Arrays.asList(
                "卧推", "推举", "引体", "上举", "侧平举", "前平举", "俯身飞鸟", "双杠臂屈伸"
        )));
        ALTERNATIVES.put("SHOULDER", Arrays.asList("器械夹胸", "高位下拉(窄握)", "坐姿划船"));

        // ---------- 下背 / 腰 ----------
        FORBIDDEN.put("LOWER_BACK", new HashSet<>(Arrays.asList(
                "硬拉", "杠铃划船", "早安", "山羊挺身", "深蹲", "俯身", "划船"
        )));
        ALTERNATIVES.put("LOWER_BACK", Arrays.asList("高位下拉(宽握)", "坐姿腿屈伸(轻重量)", "臀桥"));

        // ---------- 肘 ----------
        FORBIDDEN.put("ELBOW", new HashSet<>(Arrays.asList(
                "臂屈伸", "弯举", "俯卧撑", "窄距", "下压"
        )));
        ALTERNATIVES.put("ELBOW", Arrays.asList("器械推胸", "高位下拉"));

        // ---------- 腕 ----------
        FORBIDDEN.put("WRIST", new HashSet<>(Arrays.asList(
                "俯卧撑", "弯举", "腕", "握", "提拉"
        )));
        ALTERNATIVES.put("WRIST", Arrays.asList("器械推胸", "坐姿划船"));

        // ---------- 踝 ----------
        FORBIDDEN.put("ANKLE", new HashSet<>(Arrays.asList(
                "跑", "跳", "提踵", "跳绳", "开合跳", "箭步", "弓步"
        )));
        ALTERNATIVES.put("ANKLE", Arrays.asList("椭圆机", "坐姿腿屈伸(轻重量)"));

        // ---------- 颈 ----------
        FORBIDDEN.put("NECK", new HashSet<>(Arrays.asList(
                "耸肩", "颈", "负重", "头后"
        )));
        ALTERNATIVES.put("NECK", Arrays.asList("坐姿划船(带靠背)", "器械夹胸"));

        // ---------- 标签 ----------
        INJURY_LABEL.put("KNEE", "膝关节");
        INJURY_LABEL.put("SHOULDER", "肩关节");
        INJURY_LABEL.put("LOWER_BACK", "腰/下背");
        INJURY_LABEL.put("ELBOW", "肘关节");
        INJURY_LABEL.put("WRIST", "腕关节");
        INJURY_LABEL.put("ANKLE", "踝关节");
        INJURY_LABEL.put("NECK", "颈椎");

        BODY_PART_LABEL.put("CHEST", "胸");
        BODY_PART_LABEL.put("BACK", "背");
        BODY_PART_LABEL.put("LEG", "腿");
        BODY_PART_LABEL.put("SHOULDER", "肩");
        BODY_PART_LABEL.put("ARM", "手臂");
        BODY_PART_LABEL.put("CORE", "核心");
        BODY_PART_LABEL.put("CARDIO", "有氧");

        // 各部位的典型动作关键词，供替代动作做「部位匹配」
        BODY_PART_HINTS.put("CHEST", new HashSet<>(Arrays.asList(
                "卧推", "夹胸", "飞鸟", "俯卧撑", "推胸")));
        BODY_PART_HINTS.put("BACK", new HashSet<>(Arrays.asList(
                "下拉", "划船", "引体", "硬拉", "挺身")));
        BODY_PART_HINTS.put("LEG", new HashSet<>(Arrays.asList(
                "深蹲", "腿举", "腿屈伸", "腿弯举", "臀桥", "硬拉", "弓步", "提踵")));
        BODY_PART_HINTS.put("SHOULDER", new HashSet<>(Arrays.asList(
                "推举", "侧平举", "前平举", "飞鸟", "上举")));
        BODY_PART_HINTS.put("ARM", new HashSet<>(Arrays.asList(
                "弯举", "臂屈伸", "下压", "弯", "伸")));
        BODY_PART_HINTS.put("CORE", new HashSet<>(Arrays.asList(
                "卷腹", "平板", "死虫", "俄罗斯转体", "臀桥")));
        BODY_PART_HINTS.put("CARDIO", new HashSet<>(Arrays.asList(
                "椭圆机", "单车", "跑步机", "有氧", "划船机")));
    }

    /**
     * 校验计划与伤病是否有冲突。
     *
     * @param plan     模型输出的计划
     * @param injuries 会员伤病部位列表（如 ["KNEE","SHOULDER"]）
     * @return 违规描述列表，空表示无冲突
     */
    public List<String> validate(AiPlanResult plan, List<String> injuries) {
        List<String> violations = new ArrayList<>();
        if (plan == null || plan.isEmpty() || injuries == null || injuries.isEmpty()) {
            return violations;
        }
        for (AiPlanResult.DayPlan day : plan.getDays()) {
            if (day.getExercises() == null) {
                continue;
            }
            for (AiPlanResult.Exercise exercise : day.getExercises()) {
                String hit = matchInjury(exercise.getName(), injuries);
                if (hit != null) {
                    violations.add(String.format("第%d天（%s）的「%s」与%s伤病冲突",
                            day.getDay(), labelBodyPart(day.getBodyPart()),
                            exercise.getName(), labelInjury(hit)));
                }
            }
        }
        return violations;
    }

    /**
     * 修复：把与伤病冲突的动作替换为安全替代动作。
     *
     * 与文档骨架里那个「直接 return plan」的空实现不同，这里做真实替换：
     *   - 有可用替代动作 -> 替换动作名，保留组数/次数/休息；
     *   - 无可用替代动作 -> 从当天移除该动作；
     *   - 每个动作只替换一次，避免反复命中。
     *
     * @param plan        计划（原地修改）
     * @param injuries    伤病部位
     * @param unavailableEquip 会员不可用的器械关键词（用于过滤替代动作），可为空
     * @return 修复说明列表（用于提示用户「这里被改过」）
     */
    public List<String> fix(AiPlanResult plan, List<String> injuries, List<String> unavailableEquip) {
        List<String> notes = new ArrayList<>();
        if (plan == null || plan.isEmpty() || injuries == null || injuries.isEmpty()) {
            return notes;
        }
        // 记录本次修复已用过的替代动作，避免同一计划里反复替换成同一个动作
        Set<String> usedAlternatives = new HashSet<>();
        for (AiPlanResult.DayPlan day : plan.getDays()) {
            if (day.getExercises() == null || day.getExercises().isEmpty()) {
                continue;
            }
            List<AiPlanResult.Exercise> kept = new ArrayList<>();
            for (AiPlanResult.Exercise exercise : day.getExercises()) {
                String hit = matchInjury(exercise.getName(), injuries);
                if (hit == null) {
                    kept.add(exercise);
                    continue;
                }
                String replacement = pickAlternative(hit, day.getBodyPart(), injuries,
                        unavailableEquip, usedAlternatives);
                if (replacement != null) {
                    usedAlternatives.add(replacement);
                    notes.add(String.format("第%d天的「%s」与%s伤病冲突，已替换为「%s」",
                            day.getDay(), exercise.getName(),
                            labelInjury(hit), replacement));
                    // 替换时同步修正参数：若原动作是「有氧时长型」而替代动作是「力量型」，
                    // 必须重置组数/次数/休息，否则会出现「直腿硬拉 25-30 分钟、休息 0 秒」这种荒谬组合。
                    normalizeAfterReplace(exercise, replacement);
                    exercise.setName(replacement);
                    kept.add(exercise);
                } else {
                    notes.add(String.format("第%d天的「%s」与%s伤病冲突且无可用替代动作，已移除",
                            day.getDay(), exercise.getName(), labelInjury(hit)));
                }
            }
            day.setExercises(kept);
        }
        // 全天动作被清空的日子整个移除，避免前端展示空卡片
        plan.getDays().removeIf(d -> d.getExercises() == null || d.getExercises().isEmpty());
        return notes;
    }

    /**
     * 判断动作名是否命中任一伤病的禁忌词，命中返回该伤病部位，否则 null。
     */
    private String matchInjury(String exerciseName, List<String> injuries) {
        if (exerciseName == null || exerciseName.isEmpty()) {
            return null;
        }
        for (String injury : injuries) {
            Set<String> banned = FORBIDDEN.get(injury);
            if (banned == null) {
                continue;
            }
            for (String keyword : banned) {
                if (exerciseName.contains(keyword)) {
                    return injury;
                }
            }
        }
        return null;
    }

    /**
     * 替换动作后修正训练参数。
     *
     * 典型问题：原动作是「跑步机快走 25-30 分钟」（reps 里写的是时长、restSec=0），
     * 被替换成「直腿硬拉」后如果原样保留参数，就成了「直腿硬拉 25-30 分钟、0 秒休息」，
     * 实际根本无法执行。这里按「替代动作的类型」重新给出合理参数。
     */
    private void normalizeAfterReplace(AiPlanResult.Exercise exercise, String replacement) {
        boolean originalIsTimed = isTimedExercise(exercise);
        boolean replacementIsTimed = isTimedName(replacement);

        if (replacementIsTimed) {
            // 替代动作本身是有氧/计时类：统一为 20 分钟、无组数
            exercise.setSets(1);
            exercise.setReps("20分钟");
            exercise.setRestSec(0);
            return;
        }
        if (originalIsTimed) {
            // 原动作是计时类、替代是力量类：改成标准力量参数
            exercise.setSets(exercise.getSets() == null || exercise.getSets() <= 1 ? 3 : exercise.getSets());
            exercise.setReps("12-15");
            exercise.setRestSec(60);
        }
    }

    /** 原动作是否为「按时间」计量的动作（有氧/平板支撑等） */
    private boolean isTimedExercise(AiPlanResult.Exercise exercise) {
        if (exercise.getReps() != null && exercise.getReps().contains("分")) {
            return true;
        }
        if (exercise.getReps() != null && exercise.getReps().contains("秒")) {
            return true;
        }
        return Integer.valueOf(0).equals(exercise.getRestSec());
    }

    /** 名称上判断是否为有氧/计时类动作 */
    private boolean isTimedName(String name) {
        if (name == null) {
            return false;
        }
        for (String k : TIMED_KEYWORDS) {
            if (name.contains(k)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 选一个既不在禁忌表、也不依赖不可用器械、且未在本计划中用过的替代动作。
     *
     * 为什么需要 bodyPart 参数：
     *   早期实现只按伤病部位取候选列表第一个，导致一个计划里三个腿部动作
     *   全被替换成同一个「臀桥」，实际训练中很荒谬。
     *   现在优先选与目标部位一致的候选；同类候选用尽后，退而选任意可用动作。
     *
     * @param injury          触发的伤病部位
     * @param bodyPart        该训练日的目标部位（CHEST/BACK/LEG/...）
     * @param injuries        会员全部伤病（替代动作不能踩中任何一条）
     * @param unavailableEquip 会员没有的器械关键词
     * @param usedAlternatives 已经用过的替代动作（避免同一计划里重复）
     */
    private String pickAlternative(String injury, String bodyPart, List<String> injuries,
                                   List<String> unavailableEquip, Set<String> usedAlternatives) {
        List<String> candidates = ALTERNATIVES.get(injury);
        if (candidates == null) {
            return null;
        }
        // 第一轮：只接受与目标部位匹配的候选
        String matched = firstUsable(candidates, bodyPart, injuries, unavailableEquip, usedAlternatives, true);
        if (matched != null) {
            return matched;
        }
        // 第二轮：放宽部位限制，只要能用且没重复
        return firstUsable(candidates, bodyPart, injuries, unavailableEquip, usedAlternatives, false);
    }

    private String firstUsable(List<String> candidates, String bodyPart, List<String> injuries,
                               List<String> unavailableEquip, Set<String> usedAlternatives,
                               boolean requireBodyPartMatch) {
        for (String candidate : candidates) {
            if (usedAlternatives.contains(candidate)) {
                continue;
            }
            if (matchInjury(candidate, injuries) != null) {
                continue;
            }
            if (unavailableEquip != null && mentionsEquipment(candidate, unavailableEquip)) {
                continue;
            }
            if (requireBodyPartMatch && !fitsBodyPart(candidate, bodyPart)) {
                continue;
            }
            return candidate;
        }
        return null;
    }

    /**
     * 粗略判断动作是否适合某个训练部位（按关键词）。
     * 用于让替代动作尽量落在同一天的训练主题内。
     */
    private boolean fitsBodyPart(String exerciseName, String bodyPart) {
        if (bodyPart == null) {
            return true;
        }
        Set<String> keywords = BODY_PART_HINTS.get(bodyPart);
        if (keywords == null || keywords.isEmpty()) {
            return true;
        }
        for (String k : keywords) {
            if (exerciseName.contains(k)) {
                return true;
            }
        }
        return false;
    }

    private boolean mentionsEquipment(String exerciseName, List<String> equipment) {
        for (String equip : equipment) {
            if (exerciseName.contains(equip)) {
                return true;
            }
        }
        return false;
    }

    public String labelInjury(String injury) {
        return INJURY_LABEL.getOrDefault(injury, injury);
    }

    public String labelBodyPart(String bodyPart) {
        return BODY_PART_LABEL.getOrDefault(bodyPart, bodyPart == null ? "未指定" : bodyPart);
    }

    /**
     * 生成一句话计划摘要，列表页展示用。
     */
    public String summarize(AiPlanResult plan, int daysPerWeek) {
        if (plan == null || plan.isEmpty()) {
            return "";
        }
        Map<String, Integer> bodyPartCount = new LinkedHashMap<>();
        int exerciseCount = 0;
        for (AiPlanResult.DayPlan day : plan.getDays()) {
            String label = labelBodyPart(day.getBodyPart());
            bodyPartCount.merge(label, 1, Integer::sum);
            exerciseCount += day.getExercises() == null ? 0 : day.getExercises().size();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("每周").append(plan.getDays().size()).append("练，共")
                .append(exerciseCount).append("个动作");
        if (!bodyPartCount.isEmpty()) {
            sb.append("，涉及");
            int i = 0;
            for (Map.Entry<String, Integer> e : bodyPartCount.entrySet()) {
                if (i++ > 0) {
                    sb.append("、");
                }
                sb.append(e.getKey());
            }
        }
        return sb.toString();
    }
}
