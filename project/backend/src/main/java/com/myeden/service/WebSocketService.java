package com.myeden.service;

import com.myeden.model.WebSocketMessage;

/**
 * WebSocket服务接口
 * 
 * 功能说明：
 * - 管理WebSocket连接和会话
 * - 提供实时消息推送功能
 * - 支持广播和点对点消息
 * - 处理消息去重和过滤
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface WebSocketService {
    
    /**
     * 广播消息给所有在线用户
     * 
     * @param message 消息内容
     * @param <T> 消息数据类型
     */
    <T> void broadcastMessage(WebSocketMessage<T> message);
    
    /**
     * 发送消息给指定用户
     * 
     * @param userId 目标用户ID
     * @param message 消息内容
     * @param <T> 消息数据类型
     */
    <T> void sendMessageToUser(String userId, WebSocketMessage<T> message);
    
    /**
     * 发送消息给指定用户组
     * 
     * @param userIds 目标用户ID列表
     * @param message 消息内容
     * @param <T> 消息数据类型
     */
    <T> void sendMessageToUsers(java.util.List<String> userIds, WebSocketMessage<T> message);
    
    /**
     * 推送动态更新消息
     * 
     * @param postData 动态数据
     * @param <T> 动态数据类型
     */
    <T> void pushPostUpdate(T postData);
    
    /**
     * 推送评论更新消息
     * 
     * @param commentData 评论数据
     * @param <T> 评论数据类型
     */
    <T> void pushCommentUpdate(T commentData);
    
    /**
     * 推送通知消息
     * 
     * @param title 通知标题
     * @param content 通知内容
     * @param data 通知数据
     * @param <T> 通知数据类型
     */
    <T> void pushNotification(String title, String content, T data);
    
    /**
     * 推送机器人行为消息
     * 
     * @param actionData 机器人行为数据
     * @param <T> 行为数据类型
     */
    <T> void pushRobotAction(T actionData);
    
    /**
     * 推送系统消息
     * 
     * @param title 系统消息标题
     * @param content 系统消息内容
     * @param data 系统消息数据
     * @param <T> 系统消息数据类型
     */
    <T> void pushSystemMessage(String title, String content, T data);
    
    /**
     * 获取在线用户数量
     * 
     * @return 在线用户数量
     */
    int getOnlineUserCount();
    
    /**
     * 获取在线用户列表
     * 
     * @return 在线用户ID列表
     */
    java.util.List<String> getOnlineUsers();
    
    /**
     * 检查用户是否在线
     * 
     * @param userId 用户ID
     * @return 是否在线
     */
    boolean isUserOnline(String userId);
    
    /**
     * 断开用户连接
     * 
     * @param userId 用户ID
     */
    void disconnectUser(String userId);
    
    /**
     * 发送心跳消息
     */
    void sendHeartbeat();
} 