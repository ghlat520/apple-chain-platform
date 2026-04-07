package com.apple.chain.trade.service;

import com.apple.chain.trade.entity.ChatMessage;

import java.util.List;

/**
 * M7 chat. REST-based for v1 (offline persistence is the only acceptance
 * requirement; real-time push via WebSocket is a future iteration).
 */
public interface ChatService {

    ChatMessage send(String sessionId, Long fromUserId, Long toUserId, String msgType, String content);

    List<ChatMessage> history(String sessionId, int limit);

    int markRead(String sessionId, Long userId);
}
