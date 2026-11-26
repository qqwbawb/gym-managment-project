package com.xq.web.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * AI 训练记录
 *
 * 记忆结构第二层：会员完成训练后的打卡记录，用于判断「近期练过什么」。
 */
@Data
@TableName("ai_training_log")
public class AiTrainingLog {

    @TableId(type = IdType.AUTO)
    private Integer logId;

    private Integer memberId;

    /** 来源计划id，手动打卡可为空 */
    private Integer planId;

    /** 训练日期 */
    private Date trainDate;

    /** 训练部位: CHEST/BACK/LEG/SHOULDER/ARM/CORE/CARDIO */
    private String bodyPart;

    /** 动作摘要，如「哑铃卧推/飞鸟」 */
    private String exerciseSummary;

    /** 实际训练时长(分钟) */
    private Integer durationMin;

    private Date createTime;
}
