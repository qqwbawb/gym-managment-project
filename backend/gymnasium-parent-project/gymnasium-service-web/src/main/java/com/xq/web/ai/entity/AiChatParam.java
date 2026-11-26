package com.xq.web.ai.entity;

import lombok.Data;

/**
 * 对话生成计划 / 打补丁的入参。
 *
 * 注意：不含 memberId —— 会员身份取自登录态。
 */
@Data
public class AiChatParam {

    /** 会话id，不传则新建 */
    private String sessionId;

    /** 会员本轮的诉求 */
    private String message;
}
