package com.xq.web.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * AI 用户画像
 *
 * 记忆结构第一层：静态事实数据，由会员在表单里填写，模型无权修改。
 */
@Data
@TableName("ai_user_profile")
public class AiUserProfile {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Integer profileId;

    /** 会员id（取自登录态，不接受前端传入） */
    private Integer memberId;

    /** 目标: LOSE_FAT 减脂 / BUILD_MUSCLE 增肌 / ENDURANCE 耐力 / SHAPE 塑形 */
    private String goal;

    /** 水平: BEGINNER / INTERMEDIATE / ADVANCED */
    private String level;

    /** 每周训练次数 */
    private Integer daysPerWeek;

    /** 单次训练时长(分钟) */
    private Integer sessionMinutes;

    /** 可用器械，逗号分隔 */
    private String equipment;

    /** 伤病/禁忌部位，逗号分隔 */
    private String injuries;

    /** 伤病补充说明（用户自由填写） */
    private String injuryNote;

    private Date createTime;

    private Date updateTime;
}
