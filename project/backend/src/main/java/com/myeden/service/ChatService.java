package com.myeden.service;

import com.myeden.entity.ChatMessage;
import java.util.List;

public interface ChatService {
    void sendMessage(ChatMessage message);
    List<ChatMessage> getHistoryBySession(String sessionId, int limit, int offset);
    List<ChatMessage> getHistoryWithRobot(String userId, String robotId, int limit, int offset);
} 