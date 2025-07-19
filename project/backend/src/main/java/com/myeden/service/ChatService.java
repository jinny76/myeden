package com.myeden.service;

import com.myeden.entity.ChatMessage;
import java.util.List;

public interface ChatService {
    void sendMessage(ChatMessage message);
    List<ChatMessage> getHistoryBySession(String sessionId, int limit, int offset);
    List<ChatMessage> getHistoryWithRobot(String userId, String robotId, int limit, int offset);
    List<ChatMessage> getLatestHistoryWithRobot(String userId, String robotId, int limit);
    List<ChatMessage> getHistoryWithRobotBefore(String userId, String robotId, java.time.LocalDateTime before, int limit);
    
    /**
     * 根据expertThemeId过滤获取用户与机器人的最新历史消息
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param expertThemeId 专家主题ID（null表示随便聊聊模式）
     * @param limit 限制数量
     * @return 过滤后的消息列表
     */
    List<ChatMessage> getLatestHistoryWithRobotByExpertTheme(String userId, String robotId, String expertThemeId, int limit);
    
    /**
     * 根据expertThemeId过滤获取用户与机器人在指定时间之前的历史消息
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param expertThemeId 专家主题ID（null表示随便聊聊模式）
     * @param before 指定时间
     * @param limit 限制数量
     * @return 过滤后的消息列表
     */
    List<ChatMessage> getHistoryWithRobotByExpertThemeBefore(String userId, String robotId, String expertThemeId, java.time.LocalDateTime before, int limit);
    
    /**
     * 根据conversationId获取该会话的所有消息，按创建时间升序
     * @param conversationId 会话ID
     * @return 消息列表
     */
    List<ChatMessage> getHistoryByConversationId(String conversationId);
} 