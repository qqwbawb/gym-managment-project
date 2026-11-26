package com.xq.web.ai.entity;

import lombok.Data;

/**
 * 训练计划生成结果（非流式接口的返回体）。
 */
@Data
public class AiPlanVO {

    /** 会话id */
    private String sessionId;

    /** 计划主键 */
    private Integer planId;

    /** 结构化计划 */
    private AiPlanResult plan;

    /** 规则引擎产生的告警（空表示未发现冲突） */
    private java.util.List<String> warnings;

    /** 生成耗时(毫秒) */
    private Long costMs;

    /** 推荐课程（仅展示，报名需会员自己去课程页操作） */
    private java.util.List<RecommendCourse> recommendCourses = new java.util.ArrayList<>();

    @Data
    public static class RecommendCourse {
        private Integer courseId;
        private String courseName;
        private String teacherName;
        private String image;
        private Integer courseHour;
        private java.math.BigDecimal coursePrice;
    }
}
