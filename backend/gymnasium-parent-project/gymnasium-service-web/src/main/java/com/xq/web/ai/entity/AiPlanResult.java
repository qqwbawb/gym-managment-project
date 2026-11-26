package com.xq.web.ai.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 模型输出的计划结构（同时也是返回给前端、存入 ai_plan.plan_json 的结构）
 *
 * 设计要点：
 * 1. 所有集合字段初始化为空集合，避免模型漏字段时前端 NPE；
 * 2. 这是「三层防线」第二道（JSON Schema 校验）的落点——
 *    模型输出先解析成本结构，解析不出来的字段即视为脏数据。
 */
@Data
public class AiPlanResult {

    /** 计划标题 */
    private String title;

    /** 训练日列表 */
    private List<DayPlan> days = new ArrayList<>();

    /** 注意事项 */
    private String notes;

    @Data
    public static class DayPlan {
        /** 第几天，从 1 开始 */
        private Integer day;

        /** 训练部位: CHEST/BACK/LEG/SHOULDER/ARM/CORE/CARDIO */
        private String bodyPart;

        private List<Exercise> exercises = new ArrayList<>();
    }

    @Data
    public static class Exercise {
        /** 动作名称 */
        private String name;

        /** 组数 */
        private Integer sets;

        /** 次数，如 "8-12" 或 "30分钟" */
        private String reps;

        /** 组间休息(秒) */
        private Integer restSec;
    }

    /** 供规则引擎与摘要使用：是否包含任何训练日 */
    public boolean isEmpty() {
        return days == null || days.isEmpty();
    }
}
