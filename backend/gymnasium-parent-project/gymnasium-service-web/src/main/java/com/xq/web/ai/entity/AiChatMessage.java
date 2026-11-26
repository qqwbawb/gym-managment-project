package com.xq.web.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * AI 对话记录
 *
 * 记忆结构第三层：最近 N 轮对话，拼进 Prompt 作为多轮上下文。
 */
@Data
@TableName("ai_chat_message")
public class AiChatMessage {

    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";

    @TableId(type = IdType.AUTO)
    private Integer messageId;

    private Integer memberId;

    /** 会话id，一次连续对话 */
    private String sessionId;

    /** user / assistant */
    private String role;

    private String content;

    private Date createTime;
}
