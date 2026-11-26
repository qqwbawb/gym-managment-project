package com.xq.web.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * AI 生成的训练计划
 *
 * plan_json 存完整结构化计划；model / token_usage / cost_ms 用于成本观测。
 */
@Data
@TableName("ai_plan")
public class AiPlan {

    @TableId(type = IdType.AUTO)
    private Integer planId;

    private Integer memberId;

    private String sessionId;

    private String title;

    /** 完整计划(JSON) */
    private String planJson;

    /** 一句话摘要，列表展示用 */
    private String summary;

    /** 规则引擎校验告警(JSON 数组) */
    private String warnings;

    /** 模型名称 */
    private String model;

    /** token 消耗 */
    private Integer tokenUsage;

    /** 生成耗时(毫秒) */
    private Integer costMs;

    private Date createTime;
}
