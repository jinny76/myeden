package com.myeden.service.impl;

import com.myeden.entity.ChatMessage;
import com.myeden.repository.ChatMessageRepository;
import com.myeden.service.ChatService;
import com.myeden.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private ActivityService activityService;

    @Override
    public void sendMessage(ChatMessage message) {
        if (message == null || message.getSenderId() == null || message.getReceiverId() == null || message.getContent() == null) {
            throw new IllegalArgumentException("消息内容、发送者和接收者不能为空");
        }

        chatMessageRepository.save(message);
        
        // 记录用户活动（只记录用户的活动，不记录机器人的活动）
        if ("user".equals(message.getSenderType())) {
            try {
                ((ActivityServiceImpl) activityService).recordUserActivity(message.getSenderId(), "chat");
                logger.debug("用户聊天活动记录成功，用户ID: {}", message.getSenderId());
            } catch (Exception e) {
                logger.warn("记录用户聊天活动失败", e);
            }
        }
    }

    @Override
    public List<ChatMessage> getHistoryBySession(String sessionId, int limit, int offset) {
        if (sessionId == null || limit <= 0) {
            throw new IllegalArgumentException("会话ID和分页参数非法");
        }
        return chatMessageRepository.findBySessionIdOrderByCreatedAtDesc(
            sessionId, PageRequest.of(offset / limit, limit));
    }

    @Override
    public List<ChatMessage> getHistoryWithRobot(String userId, String robotId, int limit, int offset) {
        if (userId == null || robotId == null || limit <= 0) {
            throw new IllegalArgumentException("用户ID、机器人ID和分页参数非法");
        }
        return chatMessageRepository.findHistoryWithRobotDesc(
            userId, robotId, PageRequest.of(offset / limit, limit));
    }

    @Override
    public List<ChatMessage> getLatestHistoryWithRobot(String userId, String robotId, int limit) {
        if (userId == null || robotId == null || limit <= 0) {
            throw new IllegalArgumentException("用户ID、机器人ID和分页参数非法");
        }
        return chatMessageRepository.findHistoryWithRobotDesc(
            userId, robotId, PageRequest.of(0, limit));
    }

    @Override
    public List<ChatMessage> getHistoryWithRobotBefore(String userId, String robotId, java.time.LocalDateTime before, int limit) {
        if (userId == null || robotId == null || before == null || limit <= 0) {
            throw new IllegalArgumentException("用户ID、机器人ID、时间和分页参数非法");
        }
        return chatMessageRepository.findHistoryWithRobotBeforeDesc(
            userId, robotId, before, PageRequest.of(0, limit));
    }

    @Override
    public List<ChatMessage> getLatestHistoryWithRobotByExpertTheme(String userId, String robotId, String expertThemeId, int limit) {
        if (userId == null || robotId == null || limit <= 0) {
            throw new IllegalArgumentException("用户ID、机器人ID和分页参数非法");
        }
        return chatMessageRepository.findHistoryWithRobotByExpertThemeDesc(
            userId, robotId, expertThemeId, PageRequest.of(0, limit));
    }

    @Override
    public List<ChatMessage> getHistoryWithRobotByExpertThemeBefore(String userId, String robotId, String expertThemeId, java.time.LocalDateTime before, int limit) {
        if (userId == null || robotId == null || before == null || limit <= 0) {
            throw new IllegalArgumentException("用户ID、机器人ID、时间和分页参数非法");
        }
        return chatMessageRepository.findHistoryWithRobotByExpertThemeBeforeDesc(
            userId, robotId, expertThemeId, before, PageRequest.of(0, limit));
    }

    @Override
    public List<ChatMessage> getHistoryByConversationId(String conversationId) {
        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }
        return chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }
} 