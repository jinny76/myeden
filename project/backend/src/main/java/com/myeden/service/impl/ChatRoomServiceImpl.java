package com.myeden.service.impl;

import com.myeden.entity.ChatRoom;
import com.myeden.repository.ChatRoomRepository;
import com.myeden.repository.ChatRoomMemberRepository;
import com.myeden.repository.GroupChatMessageRepository;
import com.myeden.service.ChatRoomService;
import com.myeden.service.ChatRoomMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * 聊天室服务实现类
 * 负责聊天室的创建、管理、状态监控和模式切换
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
@Service
public class ChatRoomServiceImpl implements ChatRoomService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomServiceImpl.class);
    
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    
    @Autowired
    private ChatRoomMemberRepository memberRepository;
    
    @Autowired
    private GroupChatMessageRepository messageRepository;
    
    @Autowired
    private ChatRoomMemberService memberService;
    
    // 活跃度阈值配置
    private static final int HIGH_ACTIVITY_MESSAGE_THRESHOLD = 20; // 1小时内超过20条消息为高活跃
    private static final int HIGH_ACTIVITY_MEMBER_THRESHOLD = 3; // 超过3个活跃成员为高活跃
    private static final int INACTIVE_HOURS_THRESHOLD = 24; // 24小时无活动视为非活跃
    
    @Override
    @Transactional
    public ChatRoom createOrGetUserChatRoom(String userId, String nickname) {
        logger.info("创建或获取用户聊天室: userId={}, nickname={}", userId, nickname);
        
        try {
            // 先尝试获取现有聊天室
            Optional<ChatRoom> existingRoom = chatRoomRepository.findByRoomId(userId);
            if (existingRoom.isPresent()) {
                logger.info("找到现有聊天室: roomId={}", userId);
                return existingRoom.get();
            }
            
            // 创建新聊天室
            String roomName = nickname + "的聊天室";
            ChatRoom newRoom = new ChatRoom(userId, roomName);
            newRoom.setStatus("active"); // 新建聊天室默认为活跃状态
            
            ChatRoom savedRoom = chatRoomRepository.save(newRoom);
            logger.info("创建新聊天室成功: roomId={}, roomName={}", userId, roomName);
            
            // 将用户自己添加为房主
            memberService.addMemberToChatRoom(userId, "USER", userId, nickname, null);
            
            return savedRoom;
            
        } catch (Exception e) {
            logger.error("创建或获取用户聊天室失败: userId={}", userId, e);
            throw new RuntimeException("创建聊天室失败", e);
        }
    }
    
    @Override
    public Optional<ChatRoom> getChatRoomById(String roomId) {
        logger.debug("获取聊天室: roomId={}", roomId);
        return chatRoomRepository.findByRoomId(roomId);
    }
    
    @Override
    public ChatRoom updateChatRoom(ChatRoom chatRoom) {
        logger.info("更新聊天室: roomId={}", chatRoom.getRoomId());
        
        try {
            return chatRoomRepository.save(chatRoom);
        } catch (Exception e) {
            logger.error("更新聊天室失败: roomId={}", chatRoom.getRoomId(), e);
            throw new RuntimeException("更新聊天室失败", e);
        }
    }
    
    @Override
    @Transactional
    public boolean deleteChatRoom(String roomId) {
        logger.info("删除聊天室: roomId={}", roomId);
        
        try {
            // 检查聊天室是否存在
            if (!chatRoomRepository.existsByRoomId(roomId)) {
                logger.warn("要删除的聊天室不存在: roomId={}", roomId);
                return false;
            }
            
            // 删除相关数据
            messageRepository.deleteByRoomId(roomId); // 删除消息
            memberRepository.deleteByRoomId(roomId); // 删除成员
            chatRoomRepository.deleteByRoomId(roomId); // 删除聊天室
            
            logger.info("删除聊天室成功: roomId={}", roomId);
            return true;
            
        } catch (Exception e) {
            logger.error("删除聊天室失败: roomId={}", roomId, e);
            return false;
        }
    }
    
    @Override
    public boolean updateChatRoomName(String roomId, String newName) {
        logger.info("更新聊天室名称: roomId={}, newName={}", roomId, newName);
        
        try {
            Optional<ChatRoom> roomOpt = chatRoomRepository.findByRoomId(roomId);
            if (roomOpt.isPresent()) {
                ChatRoom room = roomOpt.get();
                room.setRoomName(newName);
                chatRoomRepository.save(room);
                logger.info("更新聊天室名称成功: roomId={}", roomId);
                return true;
            } else {
                logger.warn("聊天室不存在: roomId={}", roomId);
                return false;
            }
        } catch (Exception e) {
            logger.error("更新聊天室名称失败: roomId={}", roomId, e);
            return false;
        }
    }
    
    @Override
    public boolean switchChatRoomStatus(String roomId, String status) {
        logger.info("切换聊天室状态: roomId={}, status={}", roomId, status);
        
        try {
            Optional<ChatRoom> roomOpt = chatRoomRepository.findByRoomId(roomId);
            if (roomOpt.isPresent()) {
                ChatRoom room = roomOpt.get();
                room.setStatus(status);
                if ("active".equals(status)) {
                    room.updateLastActiveTime();
                }
                chatRoomRepository.save(room);
                logger.info("切换聊天室状态成功: roomId={}, status={}", roomId, status);
                return true;
            } else {
                logger.warn("聊天室不存在: roomId={}", roomId);
                return false;
            }
        } catch (Exception e) {
            logger.error("切换聊天室状态失败: roomId={}", roomId, e);
            return false;
        }
    }
    
    @Override
    public List<ChatRoom> getActiveChatRooms() {
        logger.debug("获取所有活跃聊天室");
        return chatRoomRepository.findByStatus("active");
    }
    
    @Override
    public List<ChatRoom> getInactiveChatRooms() {
        logger.debug("获取所有非活跃聊天室");
        return chatRoomRepository.findByStatus("inactive");
    }
    
    @Override
    public void autoAdjustChatRoomStatus() {
        logger.info("开始自动调整聊天室状态");
        
        try {
            List<ChatRoom> allRooms = chatRoomRepository.findAll();
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            LocalDateTime inactiveThreshold = LocalDateTime.now().minusHours(INACTIVE_HOURS_THRESHOLD);
            
            for (ChatRoom room : allRooms) {
                String roomId = room.getRoomId();
                
                // 统计最近1小时的消息数量
                long recentMessages = messageRepository.countByRoomIdAndSentAtAfterAndIsDeletedFalse(roomId, oneHourAgo);
                
                // 统计活跃成员数量
                long activeMembers = memberRepository.countByRoomIdAndIsOnline(roomId, true);
                
                // 判断是否应该为高活跃状态
                boolean shouldBeActive = recentMessages >= HIGH_ACTIVITY_MESSAGE_THRESHOLD || 
                                       activeMembers >= HIGH_ACTIVITY_MEMBER_THRESHOLD ||
                                       (room.getLastActiveAt() != null && room.getLastActiveAt().isAfter(inactiveThreshold));
                
                String newStatus = shouldBeActive ? "active" : "inactive";
                
                if (!newStatus.equals(room.getStatus())) {
                    logger.info("调整聊天室状态: roomId={}, {} -> {}, 消息数:{}, 活跃成员:{}", 
                              roomId, room.getStatus(), newStatus, recentMessages, activeMembers);
                    room.setStatus(newStatus);
                    if (shouldBeActive) {
                        room.updateLastActiveTime();
                    }
                    chatRoomRepository.save(room);
                }
            }
            
            logger.info("完成聊天室状态自动调整");
            
        } catch (Exception e) {
            logger.error("自动调整聊天室状态失败", e);
        }
    }
    
    @Override
    public void updateLastActiveTime(String roomId) {
        logger.debug("更新聊天室最后活跃时间: roomId={}", roomId);
        
        try {
            Optional<ChatRoom> roomOpt = chatRoomRepository.findByRoomId(roomId);
            if (roomOpt.isPresent()) {
                ChatRoom room = roomOpt.get();
                room.updateLastActiveTime();
                chatRoomRepository.save(room);
            }
        } catch (Exception e) {
            logger.error("更新聊天室最后活跃时间失败: roomId={}", roomId, e);
        }
    }
    
    @Override
    public void updateLastMessageTime(String roomId) {
        logger.debug("更新聊天室最后消息时间: roomId={}", roomId);
        
        try {
            Optional<ChatRoom> roomOpt = chatRoomRepository.findByRoomId(roomId);
            if (roomOpt.isPresent()) {
                ChatRoom room = roomOpt.get();
                room.updateLastMessageTime();
                chatRoomRepository.save(room);
            }
        } catch (Exception e) {
            logger.error("更新聊天室最后消息时间失败: roomId={}", roomId, e);
        }
    }
    
    @Override
    public void updateActiveMemberCount(String roomId, Integer memberCount) {
        logger.debug("更新聊天室活跃成员数量: roomId={}, count={}", roomId, memberCount);
        
        try {
            Optional<ChatRoom> roomOpt = chatRoomRepository.findByRoomId(roomId);
            if (roomOpt.isPresent()) {
                ChatRoom room = roomOpt.get();
                room.setActiveMemberCount(memberCount);
                chatRoomRepository.save(room);
            }
        } catch (Exception e) {
            logger.error("更新聊天室活跃成员数量失败: roomId={}", roomId, e);
        }
    }
    
    @Override
    public Map<String, Object> getChatRoomStats(String roomId) {
        logger.debug("获取聊天室统计信息: roomId={}", roomId);
        
        try {
            Optional<ChatRoom> roomOpt = chatRoomRepository.findByRoomId(roomId);
            if (!roomOpt.isPresent()) {
                return Map.of("error", "聊天室不存在");
            }
            
            ChatRoom room = roomOpt.get();
            long totalMembers = memberRepository.countByRoomId(roomId);
            long onlineMembers = memberRepository.countByRoomIdAndIsOnline(roomId, true);
            long robotMembers = memberRepository.countByRoomIdAndMemberType(roomId, "ROBOT");
            long totalMessages = messageRepository.countByRoomIdAndIsDeletedFalse(roomId);
            
            // 计算机器人消息数
            long robotMessages = messageRepository.countByRoomIdAndSenderTypeAndIsDeletedFalse(roomId, "ROBOT");
            
            // 计算活跃时长（从最后活跃时间到现在的小时数）
            long activeDuration = 0;
            if (room.getLastActiveAt() != null) {
                long hoursSinceLastActive = java.time.Duration.between(
                    room.getLastActiveAt(), 
                    LocalDateTime.now()
                ).toHours();
                activeDuration = Math.max(0, hoursSinceLastActive);
            }
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("roomId", roomId);
            stats.put("roomName", room.getRoomName());
            stats.put("status", room.getStatus());
            stats.put("totalMembers", totalMembers);
            stats.put("onlineMembers", onlineMembers);
            stats.put("robotMembers", robotMembers);
            stats.put("totalMessages", totalMessages);
            stats.put("activeMembers", onlineMembers); // 活跃成员数（在线成员）
            stats.put("robotMessages", robotMessages); // 机器人消息数
            stats.put("activeDuration", activeDuration); // 活跃时长（小时）
            
            return stats;
            
        } catch (Exception e) {
            logger.error("获取聊天室统计信息失败: roomId={}", roomId, e);
            return Map.of("error", "获取统计信息失败");
        }
    }
    
    @Override
    public List<ChatRoom> searchChatRooms(String keyword) {
        logger.debug("搜索聊天室: keyword={}", keyword);
        return chatRoomRepository.findByRoomNameContainingIgnoreCase(keyword);
    }
    
    @Override
    public List<ChatRoom> getInactiveChatRooms(int daysInactive) {
        logger.debug("获取长时间无活动聊天室: daysInactive={}", daysInactive);
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysInactive);
        return chatRoomRepository.findByLastActiveAtBefore(threshold);
    }
    
    @Override
    @Transactional
    public int cleanupInactiveChatRooms(int daysInactive) {
        logger.info("开始清理长时间无活动聊天室: daysInactive={}", daysInactive);
        
        try {
            List<ChatRoom> inactiveRooms = getInactiveChatRooms(daysInactive);
            int cleanedCount = 0;
            
            for (ChatRoom room : inactiveRooms) {
                if (deleteChatRoom(room.getRoomId())) {
                    cleanedCount++;
                    logger.info("清理无活动聊天室: roomId={}, roomName={}", room.getRoomId(), room.getRoomName());
                }
            }
            
            logger.info("完成清理无活动聊天室, 清理数量: {}", cleanedCount);
            return cleanedCount;
            
        } catch (Exception e) {
            logger.error("清理无活动聊天室失败", e);
            return 0;
        }
    }
}