package com.myeden.service.impl;

import com.myeden.entity.ChatMessage;
import com.myeden.repository.ChatMessageRepository;
import com.myeden.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Override
    public void sendMessage(ChatMessage message) {
        if (message == null || message.getSenderId() == null || message.getReceiverId() == null || message.getContent() == null) {
            throw new IllegalArgumentException("消息内容、发送者和接收者不能为空");
        }

        chatMessageRepository.save(message);
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
} 