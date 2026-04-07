package com.apple.chain.trade.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * M7 聊天消息. Table: td_chat_message (V19).
 */
@Getter
@Setter
@TableName("td_chat_message")
public class ChatMessage extends BaseEntity {

    private String sessionId;
    private Long fromUserId;
    private Long toUserId;
    private String msgType;
    private String content;
    private LocalDateTime sendTime;
    private Integer readFlag;

    public static final String TYPE_TEXT        = "TEXT";
    public static final String TYPE_PRICE_OFFER = "PRICE_OFFER";
    public static final String TYPE_IMAGE       = "IMAGE";
    public static final String TYPE_CONTRACT    = "CONTRACT";
}
