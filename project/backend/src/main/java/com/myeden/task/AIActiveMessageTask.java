package com.myeden.task;

import com.myeden.entity.ChatMessage;
import com.myeden.repository.ChatMessageRepository;
import com.myeden.model.WebSocketMessage;
import com.myeden.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.List;

@Component
public class AIActiveMessageTask {
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private WebSocketService webSocketService;

    // 示例：每天8点主动发消息
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendMorningGreeting() {
        List<String> userIds = getAllActiveUserIds(); // 需实现
        for (String userId : userIds) {
            ChatMessage aiMsg = new ChatMessage();
            aiMsg.setSessionId(generateSessionId(userId, "ai-robot-id"));
            aiMsg.setSenderId("ai-robot-id");
            aiMsg.setSenderType("ai");
            aiMsg.setReceiverId(userId);
            aiMsg.setReceiverType("user");
            aiMsg.setContent("早安，今天也要元气满满哦！");
            aiMsg.setMsgType("text");
            aiMsg.setCreatedAt(new Date());
            aiMsg.setIsRead(false);
            chatMessageRepository.save(aiMsg);
            // 推送到前端
            WebSocketMessage<ChatMessage> wsMsg = WebSocketMessage.chat(aiMsg);
            webSocketService.sendMessageToUser(userId, wsMsg);
        }
    }

    // 示例方法：获取所有活跃用户ID
    private List<String> getAllActiveUserIds() {
        // TODO: 实现获取所有需要AI主动推送的用户ID逻辑
        return List.of();
    }

    // 示例方法：生成会话ID
    private String generateSessionId(String userId, String aiId) {
        // TODO: 实现会话ID生成规则
        return userId + "_" + aiId;
    }
} 