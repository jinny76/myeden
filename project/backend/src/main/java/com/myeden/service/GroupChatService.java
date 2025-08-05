package com.myeden.service;

import com.myeden.entity.GroupChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 群聊消息服务接口
 * 负责群聊消息的处理、存储、分发和上下文管理
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
public interface GroupChatService {
    
    /**
     * 发送群聊消息
     * 
     * @param roomId 房间ID
     * @param senderType 发送者类型（USER/ROBOT）
     * @param senderId 发送者ID
     * @param content 消息内容
     * @param imageUrl 图片URL（可选）
     * @param replyToId 回复消息ID（可选）
     * @return 发送的消息对象
     */
    GroupChatMessage sendGroupMessage(String roomId, String senderType, String senderId, 
                                      String content, String imageUrl, String replyToId);
    
    /**
     * 发送机器人群聊消息（包含机器人详细信息）
     * 
     * @param roomId 房间ID
     * @param robot 机器人对象
     * @param content 消息内容
     * @param imageUrl 图片URL（可选）
     * @param replyToId 回复消息ID（可选）
     * @return 发送的消息对象
     */
    GroupChatMessage sendRobotGroupMessage(String roomId, com.myeden.entity.Robot robot, 
                                         String content, String imageUrl, String replyToId);
    
    /**
     * 发送系统消息
     * 
     * @param roomId 房间ID
     * @param content 消息内容
     * @return 系统消息对象
     */
    GroupChatMessage sendSystemMessage(String roomId, String content);
    
    /**
     * 获取聊天室消息历史（分页）
     * 
     * @param roomId 房间ID
     * @param pageable 分页参数
     * @return 消息分页
     */
    Page<GroupChatMessage> getChatHistory(String roomId, Pageable pageable);
    
    /**
     * 获取聊天室最新消息
     * 
     * @param roomId 房间ID
     * @param limit 限制数量
     * @return 最新消息列表
     */
    List<GroupChatMessage> getLatestMessages(String roomId, int limit);
    
    /**
     * 获取指定时间后的消息
     * 
     * @param roomId 房间ID
     * @param sinceTime 时间点
     * @return 消息列表
     */
    List<GroupChatMessage> getMessagesSince(String roomId, LocalDateTime sinceTime);
    
    /**
     * 根据消息ID获取消息
     * 
     * @param messageId 消息ID
     * @return 消息对象
     */
    Optional<GroupChatMessage> getMessageById(String messageId);
    
    /**
     * 获取回复某条消息的所有回复
     * 
     * @param messageId 原消息ID
     * @return 回复消息列表
     */
    List<GroupChatMessage> getReplies(String messageId);
    
    /**
     * 删除消息（软删除）
     * 
     * @param messageId 消息ID
     * @param operatorId 操作者ID
     * @return 是否删除成功
     */
    boolean deleteMessage(String messageId, String operatorId);
    
    /**
     * 搜索聊天室消息
     * 
     * @param roomId 房间ID
     * @param keyword 关键词
     * @return 匹配的消息列表
     */
    List<GroupChatMessage> searchMessages(String roomId, String keyword);
    
    /**
     * 获取聊天室图片消息
     * 
     * @param roomId 房间ID
     * @return 图片消息列表
     */
    List<GroupChatMessage> getImageMessages(String roomId);
    
    /**
     * 统计聊天室消息数量
     * 
     * @param roomId 房间ID
     * @return 消息数量
     */
    long countMessages(String roomId);
    
    /**
     * 统计指定发送者的消息数量
     * 
     * @param roomId 房间ID
     * @param senderId 发送者ID
     * @return 消息数量
     */
    long countMessagesBySender(String roomId, String senderId);
    
    /**
     * 统计指定时间后的消息数量
     * 
     * @param roomId 房间ID
     * @param sinceTime 时间点
     * @return 消息数量
     */
    long countMessagesSince(String roomId, LocalDateTime sinceTime);
    
    /**
     * 获取聊天室最后一条消息
     * 
     * @param roomId 房间ID
     * @return 最后一条消息
     */
    Optional<GroupChatMessage> getLastMessage(String roomId);
    
    /**
     * 获取聊天上下文（用于AI生成回复）
     * 获取最近的N条消息作为上下文
     * 
     * @param roomId 房间ID
     * @param contextSize 上下文大小
     * @return 上下文消息列表
     */
    List<GroupChatMessage> getChatContext(String roomId, int contextSize);
    
    /**
     * 构建AI对话上下文字符串
     * 
     * @param roomId 房间ID
     * @param contextSize 上下文大小
     * @return 格式化的上下文字符串
     */
    String buildAIChatContext(String roomId, int contextSize);
    
    /**
     * 清理聊天室所有消息
     * 
     * @param roomId 房间ID
     * @return 清理的消息数量
     */
    int clearChatRoomMessages(String roomId);
    
    /**
     * 清理指定时间前的消息
     * 
     * @param beforeTime 时间点
     * @return 清理的消息数量
     */
    int cleanupOldMessages(LocalDateTime beforeTime);
    
    /**
     * 获取活跃时间段内的消息统计
     * 用于判断聊天室活跃度
     * 
     * @param roomId 房间ID
     * @param hours 统计时间范围（小时）
     * @return 消息统计信息
     */
    ChatActivityStats getChatActivityStats(String roomId, int hours);
    
    /**
     * 聊天活跃度统计类
     */
    class ChatActivityStats {
        private long messageCount;
        private long userMessageCount;
        private long robotMessageCount;
        private int activeUserCount;
        private int activeRobotCount;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        
        public ChatActivityStats(long messageCount, long userMessageCount, long robotMessageCount,
                               int activeUserCount, int activeRobotCount, 
                               LocalDateTime startTime, LocalDateTime endTime) {
            this.messageCount = messageCount;
            this.userMessageCount = userMessageCount;
            this.robotMessageCount = robotMessageCount;
            this.activeUserCount = activeUserCount;
            this.activeRobotCount = activeRobotCount;
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        // Getters
        public long getMessageCount() { return messageCount; }
        public long getUserMessageCount() { return userMessageCount; }
        public long getRobotMessageCount() { return robotMessageCount; }
        public int getActiveUserCount() { return activeUserCount; }
        public int getActiveRobotCount() { return activeRobotCount; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        
        public double getActivityScore() {
            // 计算活跃度评分：消息数量 + 活跃用户数 * 10
            return messageCount + (activeUserCount + activeRobotCount) * 10.0;
        }
        
        public boolean isHighActivity() {
            // 判断是否为高活跃度：1小时内超过20条消息或有3个以上活跃成员
            return messageCount > 20 || (activeUserCount + activeRobotCount) > 3;
        }
    }
}