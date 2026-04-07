package com.apple.chain.trade.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.trade.entity.ChatMessage;
import com.apple.chain.trade.mapper.ChatMessageMapper;
import com.apple.chain.trade.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    /** Hard cap on message body to keep DB rows small and prevent abuse. */
    private static final int MAX_CONTENT_LENGTH = 4096;

    private final ChatMessageMapper chatMessageMapper;

    @Override
    public ChatMessage send(String sessionId, Long fromUserId, Long toUserId, String msgType, String content) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new BizException(ResultCode.PARAM_ERROR, "sessionId 不能为空");
        }
        if (fromUserId == null || toUserId == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "from/to 用户 ID 不能为空");
        }
        if (content == null || content.isBlank()) {
            throw new BizException(ResultCode.PARAM_ERROR, "消息内容不能为空");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new BizException(ResultCode.PARAM_ERROR,
                    "消息内容超过 " + MAX_CONTENT_LENGTH + " 字符上限");
        }

        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setFromUserId(fromUserId);
        msg.setToUserId(toUserId);
        msg.setMsgType(msgType == null ? ChatMessage.TYPE_TEXT : msgType);
        msg.setContent(content);
        msg.setSendTime(LocalDateTime.now());
        msg.setReadFlag(0);
        chatMessageMapper.insert(msg);
        return msg;
    }

    @Override
    public List<ChatMessage> history(String sessionId, int limit) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new BizException(ResultCode.PARAM_ERROR, "sessionId 不能为空");
        }
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        return chatMessageMapper.findBySession(sessionId, safeLimit);
    }

    @Override
    public int markRead(String sessionId, Long userId) {
        if (sessionId == null || userId == null) {
            return 0;
        }
        return chatMessageMapper.markRead(sessionId, userId);
    }
}
