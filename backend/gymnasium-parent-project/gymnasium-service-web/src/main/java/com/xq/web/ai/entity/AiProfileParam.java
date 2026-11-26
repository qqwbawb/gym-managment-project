package com.xq.web.ai.entity;

import lombok.Data;

import java.util.List;

/**
 * 保存画像的入参。
 *
 * 注意：不含 memberId —— 会员身份一律取自登录态，防止前端传别人 id 越权。
 */
@Data
public class AiProfileParam {

    /** 目标: LOSE_FAT / BUILD_MUSCLE / ENDURANCE / SHAPE */
    private String goal;

    /** 水平: BEGINNER / INTERMEDIATE / ADVANCED */
    private String level;

    /** 每周训练次数 */
    private Integer daysPerWeek;

    /** 单次训练时长(分钟) */
    private Integer sessionMinutes;

    /** 可用器械（前端传数组，后端逗号拼接存储） */
    private List<String> equipment;

    /** 伤病/禁忌部位（前端传数组，可多选） */
    private List<String> injuries;

    /** 伤病补充说明 */
    private String injuryNote;
}
