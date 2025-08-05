package com.myeden.service;

import com.myeden.entity.GroupChatMessage;
import com.myeden.entity.Robot;
import java.util.List;
import java.util.concurrent.Future;

/**
 * 机器人群聊服务接口
 * 负责机器人在群聊中的发言调度、智能回复生成和多线程管理
 * 每个聊天室运行在独立线程中，房间内机器人按活跃度随机发言
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
public interface RobotChatService {
    
    /**
     * 启动聊天室的机器人发言调度
     * 为指定聊天室创建独立的调度线程
     * 
     * @param roomId 房间ID
     * @return 调度任务Future对象
     */
    Future<?> startRoomChatScheduler(String roomId);
    
    /**
     * 停止聊天室的机器人发言调度
     * 
     * @param roomId 房间ID
     * @return 是否停止成功
     */
    boolean stopRoomChatScheduler(String roomId);
    
    /**
     * 检查聊天室调度器是否运行中
     * 
     * @param roomId 房间ID
     * @return 是否运行中
     */
    boolean isRoomSchedulerRunning(String roomId);
    
    /**
     * 触发聊天室机器人发言
     * 根据机器人活跃设定随机选择一个机器人发言
     * 
     * @param roomId 房间ID
     * @return 是否成功触发发言
     */
    boolean triggerRoomChat(String roomId);
    
    /**
     * 选择下一个发言的机器人
     * 根据机器人活跃度和发言频率进行加权随机选择
     * 
     * @param roomId 房间ID
     * @return 选中的机器人，如果没有合适的返回null
     */
    Robot selectNextSpeaker(String roomId);
    
    /**
     * 生成机器人群聊消息
     * 基于聊天上下文和机器人个性生成1-5句话的消息
     * 
     * @param robot 机器人对象
     * @param roomId 房间ID
     * @param contextMessages 上下文消息列表
     * @return 生成的消息内容
     */
    String generateRobotChatMessage(Robot robot, String roomId, List<GroupChatMessage> contextMessages);
    
    /**
     * 机器人发送群聊消息
     * 
     * @param roomId 房间ID
     * @param robot 机器人对象
     * @param content 消息内容
     * @param imageUrl 图片URL（可选）
     * @param replyToId 回复消息ID（可选）
     * @return 发送的消息对象
     */
    GroupChatMessage sendRobotMessage(String roomId, Robot robot, String content, 
                                      String imageUrl, String replyToId);
    
    /**
     * 处理用户消息，触发机器人回复
     * 当用户发送消息时，根据概率触发机器人回复
     * 
     * @param roomId 房间ID
     * @param userMessage 用户消息
     * @return 回复的机器人数量
     */
    int handleUserMessage(String roomId, GroupChatMessage userMessage);
    
    /**
     * 计算机器人在房间内的发言概率
     * 基于机器人活跃度、房间状态、时间等因素计算
     * 
     * @param robot 机器人对象
     * @param roomId 房间ID
     * @return 发言概率（0-1之间）
     */
    double calculateRobotSpeakProbability(Robot robot, String roomId);
    
    /**
     * 检查机器人是否可以在房间内发言
     * 检查机器人状态、房间成员关系、静音状态等
     * 
     * @param robot 机器人对象
     * @param roomId 房间ID
     * @return 是否可以发言
     */
    boolean canRobotSpeakInRoom(Robot robot, String roomId);
    
    /**
     * 获取聊天室当前活跃的机器人列表
     * 
     * @param roomId 房间ID
     * @return 活跃机器人列表
     */
    List<Robot> getActiveRobotsInRoom(String roomId);
    
    /**
     * 更新机器人在房间内的发言统计
     * 
     * @param robotId 机器人ID
     * @param roomId 房间ID
     */
    void updateRobotChatStats(String robotId, String roomId);
    
    /**
     * 获取机器人在房间内的发言统计
     * 
     * @param robotId 机器人ID
     * @param roomId 房间ID
     * @return 统计信息
     */
    RobotChatStats getRobotChatStats(String robotId, String roomId);
    
    /**
     * 启动所有活跃聊天室的调度器
     * 系统启动时调用
     */
    void startAllRoomSchedulers();
    
    /**
     * 停止所有聊天室的调度器
     * 系统关闭时调用
     */
    void stopAllRoomSchedulers();
    
    /**
     * 根据房间活跃度调整发言频率
     * 高频模式：10-30秒发言一次
     * 低频模式：1-5分钟发言一次
     * 
     * @param roomId 房间ID
     */
    void adjustChatFrequency(String roomId);
    
    /**
     * 生成带图片的机器人消息
     * 集成图片生成功能
     * 
     * @param robot 机器人对象
     * @param roomId 房间ID
     * @param context 上下文
     * @return 生成的消息（包含图片）
     */
    GroupChatMessage generateRobotImageMessage(Robot robot, String roomId, String context);
    
    /**
     * 处理话题切换
     * 当聊天内容单调时主动切换话题
     * 
     * @param roomId 房间ID
     * @return 是否成功切换话题
     */
    boolean switchTopic(String roomId);
    
    /**
     * 机器人聊天统计类
     */
    class RobotChatStats {
        private String robotId;
        private String roomId;
        private long totalMessages;
        private long todayMessages;
        private double averageMessageLength;
        private String lastMessageTime;
        private double responseRate;
        
        public RobotChatStats(String robotId, String roomId) {
            this.robotId = robotId;
            this.roomId = roomId;
        }
        
        // Getters and Setters
        public String getRobotId() { return robotId; }
        public void setRobotId(String robotId) { this.robotId = robotId; }
        
        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }
        
        public long getTotalMessages() { return totalMessages; }
        public void setTotalMessages(long totalMessages) { this.totalMessages = totalMessages; }
        
        public long getTodayMessages() { return todayMessages; }
        public void setTodayMessages(long todayMessages) { this.todayMessages = todayMessages; }
        
        public double getAverageMessageLength() { return averageMessageLength; }
        public void setAverageMessageLength(double averageMessageLength) { this.averageMessageLength = averageMessageLength; }
        
        public String getLastMessageTime() { return lastMessageTime; }
        public void setLastMessageTime(String lastMessageTime) { this.lastMessageTime = lastMessageTime; }
        
        public double getResponseRate() { return responseRate; }
        public void setResponseRate(double responseRate) { this.responseRate = responseRate; }
    }
}