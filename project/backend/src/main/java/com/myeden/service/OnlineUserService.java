package com.myeden.service;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 在线用户状态管理服务接口
 * 负责追踪用户在聊天室页面的在线状态
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
public interface OnlineUserService {
    
    /**
     * 标记用户在聊天室页面上线
     * 
     * @param roomId 房间ID
     * @param userId 用户ID
     */
    void userOnline(String roomId, String userId);
    
    /**
     * 标记用户离开聊天室页面
     * 
     * @param roomId 房间ID
     * @param userId 用户ID
     */
    void userOffline(String roomId, String userId);
    
    /**
     * 更新用户心跳时间
     * 
     * @param roomId 房间ID
     * @param userId 用户ID
     */
    void updateHeartbeat(String roomId, String userId);
    
    /**
     * 获取聊天室在线用户数
     * 
     * @param roomId 房间ID
     * @return 在线用户数
     */
    int getOnlineUserCount(String roomId);
    
    /**
     * 获取聊天室在线用户列表
     * 
     * @param roomId 房间ID
     * @return 在线用户ID集合
     */
    Set<String> getOnlineUsers(String roomId);
    
    /**
     * 检查用户是否在聊天室页面在线
     * 
     * @param roomId 房间ID
     * @param userId 用户ID
     * @return 是否在线
     */
    boolean isUserOnline(String roomId, String userId);
    
    /**
     * 清理超时的在线状态
     * 定时任务调用，清理长时间没有心跳的用户
     */
    void cleanupTimeoutUsers();
    
    /**
     * 获取用户最后心跳时间
     * 
     * @param roomId 房间ID
     * @param userId 用户ID
     * @return 最后心跳时间
     */
    LocalDateTime getLastHeartbeat(String roomId, String userId);
}