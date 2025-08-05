package com.myeden.service.impl;

import com.myeden.service.OnlineUserService;
import com.myeden.repository.ChatRoomRepository;
import com.myeden.entity.ChatRoom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线用户状态管理服务实现类
 * 使用内存缓存管理用户在线状态，支持心跳机制和自动清理
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
@Service
public class OnlineUserServiceImpl implements OnlineUserService {
    
    private static final Logger logger = LoggerFactory.getLogger(OnlineUserServiceImpl.class);
    
    // 用户在线状态缓存：roomId -> (userId -> lastHeartbeat)
    private final Map<String, Map<String, LocalDateTime>> onlineUsers = new ConcurrentHashMap<>();
    
    // 心跳超时时间（分钟）
    private static final int HEARTBEAT_TIMEOUT_MINUTES = 3;
    
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Override
    public void userOnline(String roomId, String userId) {
        try {
            Map<String, LocalDateTime> roomUsers = onlineUsers.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());
            boolean wasOffline = !roomUsers.containsKey(userId);
            
            roomUsers.put(userId, LocalDateTime.now());
            
            if (wasOffline) {
                logger.info("用户上线: roomId={}, userId={}", roomId, userId);
                
                // 如果这是房间内第一个上线的用户，切换到高频模式
                int onlineCount = getOnlineUserCount(roomId);
                if (onlineCount == 1) {
                    switchToHighFrequencyMode(roomId);
                    logger.info("切换到高频模式: roomId={}, onlineCount={}", roomId, onlineCount);
                }
            }
            
        } catch (Exception e) {
            logger.error("用户上线处理失败: roomId={}, userId={}", roomId, userId, e);
        }
    }
    
    @Override
    public void userOffline(String roomId, String userId) {
        try {
            Map<String, LocalDateTime> roomUsers = onlineUsers.get(roomId);
            if (roomUsers != null) {
                boolean wasOnline = roomUsers.remove(userId) != null;
                
                if (wasOnline) {
                    logger.info("用户下线: roomId={}, userId={}", roomId, userId);
                    
                    // 如果房间内没有用户在线了，切换到低频模式
                    int onlineCount = getOnlineUserCount(roomId);
                    if (onlineCount == 0) {
                        switchToLowFrequencyMode(roomId);
                        logger.info("切换到低频模式: roomId={}, onlineCount={}", roomId, onlineCount);
                    }
                }
                
                // 如果房间内没有用户了，清理房间缓存
                if (roomUsers.isEmpty()) {
                    onlineUsers.remove(roomId);
                }
            }
            
        } catch (Exception e) {
            logger.error("用户下线处理失败: roomId={}, userId={}", roomId, userId, e);
        }
    }
    
    @Override
    public void updateHeartbeat(String roomId, String userId) {
        try {
            Map<String, LocalDateTime> roomUsers = onlineUsers.get(roomId);
            if (roomUsers != null && roomUsers.containsKey(userId)) {
                roomUsers.put(userId, LocalDateTime.now());
                logger.debug("更新用户心跳: roomId={}, userId={}", roomId, userId);
            } else {
                // 如果用户不在在线列表中，标记为上线
                userOnline(roomId, userId);
            }
            
        } catch (Exception e) {
            logger.error("更新用户心跳失败: roomId={}, userId={}", roomId, userId, e);
        }
    }
    
    @Override
    public int getOnlineUserCount(String roomId) {
        Map<String, LocalDateTime> roomUsers = onlineUsers.get(roomId);
        return roomUsers != null ? roomUsers.size() : 0;
    }
    
    @Override
    public Set<String> getOnlineUsers(String roomId) {
        Map<String, LocalDateTime> roomUsers = onlineUsers.get(roomId);
        return roomUsers != null ? roomUsers.keySet() : Set.of();
    }
    
    @Override
    public boolean isUserOnline(String roomId, String userId) {
        Map<String, LocalDateTime> roomUsers = onlineUsers.get(roomId);
        return roomUsers != null && roomUsers.containsKey(userId);
    }
    
    @Override
    public LocalDateTime getLastHeartbeat(String roomId, String userId) {
        Map<String, LocalDateTime> roomUsers = onlineUsers.get(roomId);
        return roomUsers != null ? roomUsers.get(userId) : null;
    }
    
    /**
     * 定时清理超时的在线用户
     * 每分钟执行一次
     */
    @Override
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void cleanupTimeoutUsers() {
        try {
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(HEARTBEAT_TIMEOUT_MINUTES);
            
            for (Map.Entry<String, Map<String, LocalDateTime>> roomEntry : onlineUsers.entrySet()) {
                String roomId = roomEntry.getKey();
                Map<String, LocalDateTime> roomUsers = roomEntry.getValue();
                
                // 找出超时的用户
                Set<String> timeoutUsers = roomUsers.entrySet().stream()
                        .filter(entry -> entry.getValue().isBefore(timeoutThreshold))
                        .map(Map.Entry::getKey)
                        .collect(java.util.stream.Collectors.toSet());
                
                // 移除超时用户
                for (String userId : timeoutUsers) {
                    logger.info("清理超时用户: roomId={}, userId={}, lastHeartbeat={}", 
                              roomId, userId, roomUsers.get(userId));
                    userOffline(roomId, userId);
                }
            }
            
        } catch (Exception e) {
            logger.error("清理超时用户失败", e);
        }
    }
    
    /**
     * 切换聊天室为高频模式
     */
    private void switchToHighFrequencyMode(String roomId) {
        try {
            Optional<ChatRoom> roomOpt = chatRoomRepository.findByRoomId(roomId);
            if (roomOpt.isPresent()) {
                ChatRoom room = roomOpt.get();
                if (!"high_frequency".equals(room.getStatus())) {
                    room.setStatus("high_frequency");
                    room.setLastActiveAt(LocalDateTime.now());
                    chatRoomRepository.save(room);
                    
                    logger.info("切换到高频模式: roomId={}", roomId);
                    
                    // 发布频率变更事件，让RobotChatService调整频率
                    publishFrequencyChangeEvent(roomId, "high_frequency");
                }
            }
        } catch (Exception e) {
            logger.error("切换到高频模式失败: roomId={}", roomId, e);
        }
    }
    
    /**
     * 切换聊天室为低频模式
     */
    private void switchToLowFrequencyMode(String roomId) {
        try {
            Optional<ChatRoom> roomOpt = chatRoomRepository.findByRoomId(roomId);
            if (roomOpt.isPresent()) {
                ChatRoom room = roomOpt.get();
                if (!"low_frequency".equals(room.getStatus())) {
                    room.setStatus("low_frequency");
                    room.setLastActiveAt(LocalDateTime.now());
                    chatRoomRepository.save(room);
                    
                    logger.info("切换到低频模式: roomId={}", roomId);
                    
                    // 发布频率变更事件，让RobotChatService调整频率
                    publishFrequencyChangeEvent(roomId, "low_frequency");
                }
            }
        } catch (Exception e) {
            logger.error("切换到低频模式失败: roomId={}", roomId, e);
        }
    }
    
    /**
     * 发布频率变更事件
     */
    private void publishFrequencyChangeEvent(String roomId, String frequency) {
        try {
            // 创建一个简单的频率变更事件
            FrequencyChangeEvent event = new FrequencyChangeEvent(roomId, frequency);
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            logger.error("发布频率变更事件失败: roomId={}, frequency={}", roomId, frequency, e);
        }
    }
    
    /**
     * 频率变更事件类
     */
    public static class FrequencyChangeEvent {
        private final String roomId;
        private final String frequency;
        
        public FrequencyChangeEvent(String roomId, String frequency) {
            this.roomId = roomId;
            this.frequency = frequency;
        }
        
        public String getRoomId() {
            return roomId;
        }
        
        public String getFrequency() {
            return frequency;
        }
    }
}