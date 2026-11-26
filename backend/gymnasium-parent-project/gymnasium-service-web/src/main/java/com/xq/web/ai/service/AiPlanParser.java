package com.xq.web.ai.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xq.web.ai.entity.AiPlanResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 模型输出的 JSON 解析器（纯函数，无任何外部依赖）。
 *
 * 为什么单独抽出来：
 *   1. 这是「三道防线」里的第二道，正确性直接决定功能可用性，必须能写单测；
 *   2. 它本来就不需要数据库、不需要 Spring 上下文，混在 Service 里反而不好测；
 *   3. 抽成纯类后可以用 JUnit 直接覆盖各种畸形输出，不用起容器。
 *
 * 处理的模型常见「不老实」：
 *   1. 包裹 ```json ``` 代码块
 *   2. JSON 前后夹带解释性文字
 *   3. 字段缺失、动作名为空
 *   4. 输出完全不是 JSON
 */
@Slf4j
public final class AiPlanParser {

    /** 计划最多允许的训练日数。模型异常时会输出几十天，既不可执行也没法渲染 */
    private static final int MAX_DAYS = 14;

    /** 单个训练日最多允许的动作数 */
    private static final int MAX_EXERCISES_PER_DAY = 15;

    /**
     * 身体部位中英文归一化映射。
     *
     * 为什么需要：模型经常输中文（"胸""腿"），而下游的课程推荐 switch 和前端标签表
     * 都只认 CHEST/BACK 这类枚举。不归一化会出现「部位显示正确但推荐课程为空」的怪现象。
     */
    private static final java.util.Map<String, String> BODY_PART_ALIAS = new java.util.HashMap<>();

    static {
        BODY_PART_ALIAS.put("胸", "CHEST");
        BODY_PART_ALIAS.put("胸部", "CHEST");
        BODY_PART_ALIAS.put("胸肌", "CHEST");
        BODY_PART_ALIAS.put("背", "BACK");
        BODY_PART_ALIAS.put("背部", "BACK");
        BODY_PART_ALIAS.put("腿", "LEG");
        BODY_PART_ALIAS.put("腿部", "LEG");
        BODY_PART_ALIAS.put("腿臀", "LEG");
        BODY_PART_ALIAS.put("下肢", "LEG");
        BODY_PART_ALIAS.put("臀腿", "LEG");
        BODY_PART_ALIAS.put("肩", "SHOULDER");
        BODY_PART_ALIAS.put("肩部", "SHOULDER");
        BODY_PART_ALIAS.put("三角肌", "SHOULDER");
        BODY_PART_ALIAS.put("手臂", "ARM");
        BODY_PART_ALIAS.put("胳膊", "ARM");
        BODY_PART_ALIAS.put("二头", "ARM");
        BODY_PART_ALIAS.put("三头", "ARM");
        BODY_PART_ALIAS.put("核心", "CORE");
        BODY_PART_ALIAS.put("腹部", "CORE");
        BODY_PART_ALIAS.put("腹", "CORE");
        BODY_PART_ALIAS.put("腰腹", "CORE");
        BODY_PART_ALIAS.put("有氧", "CARDIO");
        BODY_PART_ALIAS.put("心肺", "CARDIO");
        BODY_PART_ALIAS.put("全身", "CARDIO");
    }

    private AiPlanParser() {
    }

    /**
     * 把模型原始输出解析为计划对象。
     *
     * @return 解析失败或计划为空时返回 null（由调用方决定是否重试）
     */
    public static AiPlanResult parse(String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        String json = cleanJson(raw);
        try {
            JSONObject obj = JSON.parseObject(json);
            if (obj == null) {
                return null;
            }

            AiPlanResult result = new AiPlanResult();
            result.setTitle(obj.getString("title"));
            result.setNotes(obj.getString("notes"));

            JSONArray daysArr = obj.getJSONArray("days");
            List<AiPlanResult.DayPlan> days = new ArrayList<>();
            if (daysArr != null) {
                for (int i = 0; i < daysArr.size(); i++) {
                    // 规模上限：超出部分直接截断，避免异常输出把库和前端撑爆
                    if (days.size() >= MAX_DAYS) {
                        log.warn("模型输出训练日过多（{} 天），已截断到 {} 天",
                                daysArr.size(), MAX_DAYS);
                        break;
                    }
                    JSONObject d = daysArr.getJSONObject(i);
                    if (d == null) {
                        continue;
                    }
                    AiPlanResult.DayPlan day = new AiPlanResult.DayPlan();
                    day.setDay(d.getInteger("day") == null ? i + 1 : d.getInteger("day"));
                    day.setBodyPart(normalizeBodyPart(d.getString("bodyPart")));

                    JSONArray exArr = d.getJSONArray("exercises");
                    List<AiPlanResult.Exercise> exercises = new ArrayList<>();
                    if (exArr != null) {
                        for (int j = 0; j < exArr.size(); j++) {
                            if (exercises.size() >= MAX_EXERCISES_PER_DAY) {
                                log.warn("第 {} 天动作过多（{} 个），已截断到 {} 个",
                                        day.getDay(), exArr.size(), MAX_EXERCISES_PER_DAY);
                                break;
                            }
                            JSONObject e = exArr.getJSONObject(j);
                            // 没有动作名的条目直接丢弃，避免前端渲染出空行
                            if (e == null || StringUtils.isBlank(e.getString("name"))) {
                                continue;
                            }
                            AiPlanResult.Exercise ex = new AiPlanResult.Exercise();
                            ex.setName(e.getString("name").trim());
                            ex.setSets(e.getInteger("sets"));
                            ex.setReps(e.getString("reps"));
                            ex.setRestSec(e.getInteger("restSec"));
                            exercises.add(ex);
                        }
                    }
                    day.setExercises(exercises);
                    // 一个动作都没有的训练日没有意义，丢弃
                    if (!exercises.isEmpty()) {
                        days.add(day);
                    }
                }
            }
            result.setDays(days);

            // 完全空计划视为解析失败，交给上层重试
            return result.isEmpty() ? null : result;
        } catch (Exception e) {
            log.warn("JSON 解析失败: {} , 内容片段: {}", e.getMessage(), brief(raw));
            return null;
        }
    }

    /**
     * 清洗模型输出：去掉 markdown 代码块包裹、截取首个 { 到最后一个 }。
     */
    public static String cleanJson(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim();
        // 去掉 ```json ... ``` 或 ``` ... ```
        if (s.startsWith("```")) {
            int firstLineEnd = s.indexOf('\n');
            if (firstLineEnd > 0) {
                s = s.substring(firstLineEnd + 1);
            }
            int fenceEnd = s.lastIndexOf("```");
            if (fenceEnd >= 0) {
                s = s.substring(0, fenceEnd);
            }
        }
        // 截取 JSON 主体，容忍前后废话
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start >= 0 && end > start) {
            s = s.substring(start, end + 1);
        }
        return s.trim();
    }

    /**
     * 身体部位归一化：中文/混合写法 → 标准枚举。
     *
     * 已是英文枚举的原样返回（转大写）；无法识别时返回大写原值，
     * 由下游按"未知部位"处理，不抛异常（宁可展示原值也不让整份计划失败）。
     */
    static String normalizeBodyPart(String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        String s = raw.trim();
        // 先查中文映射（支持 "胸" "腿部" 等）
        String mapped = BODY_PART_ALIAS.get(s);
        if (mapped != null) {
            return mapped;
        }
        // 可能是 "胸(chest)" 这类混合写法，做包含匹配
        for (java.util.Map.Entry<String, String> e : BODY_PART_ALIAS.entrySet()) {
            if (s.contains(e.getKey())) {
                return e.getValue();
            }
        }
        return s.toUpperCase();
    }

    private static String brief(String raw) {
        if (raw == null) {
            return "null";
        }
        return raw.length() <= 200 ? raw : raw.substring(0, 200) + "...";
    }
}
