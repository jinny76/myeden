package com.myeden.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeden.model.WebSocketMessage;
import com.myeden.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.UUID;

/**
 * WebSocket服务实现类
 * 
 * 功能说明：
 * - 实现WebSocket连接管理和消息推送
 * - 维护在线用户会话映射
 * - 提供广播和点对点消息功能
 * - 处理消息去重和过滤
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class WebSocketServiceImpl implements WebSocketService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 在线用户会话映射：用户ID -> 会话ID列表
     */
    private final Map<String, List<String>> userSessions = new ConcurrentHashMap<>();
    
    /**
     * 会话用户映射：会话ID -> 用户ID
     */
    private final Map<String, String> sessionUsers = new ConcurrentHashMap<>();
    
    /**
     * 消息去重缓存：消息ID -> 时间戳
     */
    private final Map<String, Long> messageDeduplication = new ConcurrentHashMap<>();
    
    /**
     * 消息去重时间窗口（毫秒）
     */
    private static final long DEDUPLICATION_WINDOW = 5000; // 5秒
    
    /**
     * 广播消息给所有在线用户
     */
    @Override
    public <T> void broadcastMessage(WebSocketMessage<T> message) {
        try {
            // 生成消息ID
            if (message.getMessageId() == null) {
                message.setMessageId(UUID.randomUUID().toString());
            }
            
            // 检查消息去重
            if (isDuplicateMessage(message.getMessageId())) {
                log.debug("消息已存在，跳过广播: {}", message.getMessageId());
                return;
            }
            
            // 记录消息
            recordMessage(message.getMessageId());
            
            // 转换为JSON
            String messageJson = objectMapper.writeValueAsString(message);
            
            // 广播消息
            messagingTemplate.convertAndSend("/topic/broadcast", messageJson);
            
            log.info("广播消息成功: type={}, messageId={}", message.getType(), message.getMessageId());
        } catch (JsonProcessingException e) {
            log.error("消息序列化失败", e);
        } catch (Exception e) {
            log.error("广播消息失败", e);
        }
    }
    
    /**
     * 发送消息给指定用户
     */
    @Override
    public <T> void sendMessageToUser(String userId, WebSocketMessage<T> message) {
        try {
            // 生成消息ID
            if (message.getMessageId() == null) {
                message.setMessageId(UUID.randomUUID().toString());
            }
            
            // 检查消息去重
            if (isDuplicateMessage(message.getMessageId())) {
                log.debug("消息已存在，跳过发送: {}", message.getMessageId());
                return;
            }
            
            // 记录消息
            recordMessage(message.getMessageId());
            
            // 转换为JSON
            String messageJson = objectMapper.writeValueAsString(message);
            
            // 发送给指定用户
            messagingTemplate.convertAndSendToUser(userId, "/queue/messages", messageJson);
            
            log.info("发送消息给用户成功: userId={}, type={}, messageId={}", 
                    userId, message.getType(), message.getMessageId());
        } catch (JsonProcessingException e) {
            log.error("消息序列化失败", e);
        } catch (Exception e) {
            log.error("发送消息给用户失败: userId={}", userId, e);
        }
    }
    
    /**
     * 发送消息给指定用户组
     */
    @Override
    public <T> void sendMessageToUsers(List<String> userIds, WebSocketMessage<T> message) {
        try {
            // 生成消息ID
            if (message.getMessageId() == null) {
                message.setMessageId(UUID.randomUUID().toString());
            }
            
            // 检查消息去重
            if (isDuplicateMessage(message.getMessageId())) {
                log.debug("消息已存在，跳过发送: {}", message.getMessageId());
                return;
            }
            
            // 记录消息
            recordMessage(message.getMessageId());
            
            // 转换为JSON
            String messageJson = objectMapper.writeValueAsString(message);
            
            // 发送给用户组
            for (String userId : userIds) {
                if (isUserOnline(userId)) {
                    messagingTemplate.convertAndSendToUser(userId, "/queue/messages", messageJson);
                }
            }
            
            log.info("发送消息给用户组成功: userIds={}, type={}, messageId={}", 
                    userIds, message.getType(), message.getMessageId());
        } catch (JsonProcessingException e) {
            log.error("消息序列化失败", e);
        } catch (Exception e) {
            log.error("发送消息给用户组失败: userIds={}", userIds, e);
        }
    }
    
    /**
     * 推送动态更新消息
     */
    @Override
    public <T> void pushPostUpdate(T postData) {
        WebSocketMessage<T> message = WebSocketMessage.postUpdate(postData);
        broadcastMessage(message);
    }
    
    /**
     * 推送评论更新消息
     */
    @Override
    public <T> void pushCommentUpdate(T commentData) {
        WebSocketMessage<T> message = WebSocketMessage.commentUpdate(commentData);
        broadcastMessage(message);
    }
    
    /**
     * 推送通知消息
     */
    @Override
    public <T> void pushNotification(String title, String content, T data) {
        WebSocketMessage<T> message = WebSocketMessage.notification(title, content, data);
        broadcastMessage(message);
    }
    
    /**
     * 推送机器人行为消息
     */
    @Override
    public <T> void pushRobotAction(T actionData) {
        WebSocketMessage<T> message = WebSocketMessage.robotAction(actionData);
        broadcastMessage(message);
    }
    
    /**
     * 推送系统消息
     */
    @Override
    public <T> void pushSystemMessage(String title, String content, T data) {
        WebSocketMessage<T> message = WebSocketMessage.systemMessage(title, content, data);
        broadcastMessage(message);
    }
    
    /**
     * 获取在线用户数量
     */
    @Override
    public int getOnlineUserCount() {
        return userSessions.size();
    }
    
    /**
     * 获取在线用户列表
     */
    @Override
    public List<String> getOnlineUsers() {
        return new CopyOnWriteArrayList<>(userSessions.keySet());
    }
    
    /**
     * 检查用户是否在线
     */
    @Override
    public boolean isUserOnline(String userId) {
        List<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }
    
    /**
     * 断开用户连接
     */
    @Override
    public void disconnectUser(String userId) {
        List<String> sessions = userSessions.get(userId);
        if (sessions != null) {
            for (String sessionId : sessions) {
                sessionUsers.remove(sessionId);
            }
            userSessions.remove(userId);
            log.info("用户连接已断开: userId={}", userId);
        }
    }
    
    /**
     * 发送心跳消息
     */
    @Override
    public void sendHeartbeat() {
        WebSocketMessage<String> heartbeat = WebSocketMessage.heartbeat();
        broadcastMessage(heartbeat);
    }
    
    /**
     * 处理用户连接事件
     */
    public void handleUserConnected(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String userId = extractUserId(accessor);
        
        if (userId != null) {
            // 添加用户会话映射
            userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(sessionId);
            sessionUsers.put(sessionId, userId);
            
            log.info("用户连接成功: userId={}, sessionId={}", userId, sessionId);
            
            // 发送连接成功消息
            WebSocketMessage<String> welcomeMessage = WebSocketMessage.systemMessage(
                "连接成功", 
                "欢迎来到我的伊甸园！", 
                "连接已建立"
            );
            sendMessageToUser(userId, welcomeMessage);
        }
    }
    
    /**
     * 处理用户断开事件
     */
    public void handleUserDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String userId = sessionUsers.remove(sessionId);
        
        if (userId != null) {
            // 移除用户会话映射
            List<String> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            
            log.info("用户断开连接: userId={}, sessionId={}", userId, sessionId);
        }
    }
    
    /**
     * 从STOMP头中提取用户ID
     */
    private String extractUserId(StompHeaderAccessor accessor) {
        // 这里需要根据实际的认证方式提取用户ID
        // 可以从JWT token、用户头信息等地方获取
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            return (String) sessionAttributes.get("userId");
        }
        return null;
    }
    
    /**
     * 检查是否为重复消息
     */
    private boolean isDuplicateMessage(String messageId) {
        Long timestamp = messageDeduplication.get(messageId);
        if (timestamp == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - timestamp > DEDUPLICATION_WINDOW) {
            messageDeduplication.remove(messageId);
            return false;
        }
        
        return true;
    }
    
    /**
     * 记录消息时间戳
     */
    private void recordMessage(String messageId) {
        messageDeduplication.put(messageId, System.currentTimeMillis());
        
        // 清理过期消息记录
        long currentTime = System.currentTimeMillis();
        messageDeduplication.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > DEDUPLICATION_WINDOW
        );
    }
} 