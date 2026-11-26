package com.xq.web.ai.entity;

import lombok.Data;

/**
 * 训练打卡入参。
 *
 * 注意：不含 memberId —— 会员身份取自登录态。
 */
@Data
public class AiTrainingLogParam {

    /** 来源计划id，可空 */
    private Integer planId;

    /** 训练日期 yyyy-MM-dd，不传默认今天 */
    private String trainDate;

    /** 训练部位: CHEST/BACK/LEG/SHOULDER/ARM/CORE/CARDIO */
    private String bodyPart;

    /** 动作摘要 */
    private String exerciseSummary;

    /** 实际训练时长(分钟) */
    private Integer durationMin;
}
