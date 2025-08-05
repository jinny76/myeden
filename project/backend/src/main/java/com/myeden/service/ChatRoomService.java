package com.myeden.service;

import com.myeden.entity.ChatRoom;
import java.util.List;
import java.util.Optional;
import java.util.Map;

/**
 * 聊天室服务接口
 * 负责聊天室的创建、管理、状态监控和模式切换
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
public interface ChatRoomService {
    
    /**
     * 创建或获取用户的聊天室
     * 如果用户已有聊天室则返回现有的，否则创建新的
     * 
     * @param userId 用户ID
     * @param nickname 用户昵称（用于生成默认房间名）
     * @return 聊天室对象
     */
    ChatRoom createOrGetUserChatRoom(String userId, String nickname);
    
    /**
     * 根据房间ID获取聊天室
     * 
     * @param roomId 房间ID
     * @return 聊天室对象，不存在则返回null
     */
    Optional<ChatRoom> getChatRoomById(String roomId);
    
    /**
     * 更新聊天室信息
     * 
     * @param chatRoom 聊天室对象
     * @return 更新后的聊天室对象
     */
    ChatRoom updateChatRoom(ChatRoom chatRoom);
    
    /**
     * 删除聊天室
     * 会同时删除相关的成员和消息记录
     * 
     * @param roomId 房间ID
     * @return 是否删除成功
     */
    boolean deleteChatRoom(String roomId);
    
    /**
     * 更新聊天室名称
     * 
     * @param roomId 房间ID
     * @param newName 新名称
     * @return 是否更新成功
     */
    boolean updateChatRoomName(String roomId, String newName);
    
    /**
     * 切换聊天室状态（活跃/非活跃）
     * 
     * @param roomId 房间ID
     * @param status 新状态
     * @return 是否切换成功
     */
    boolean switchChatRoomStatus(String roomId, String status);
    
    /**
     * 获取所有活跃的聊天室
     * 
     * @return 活跃聊天室列表
     */
    List<ChatRoom> getActiveChatRooms();
    
    /**
     * 获取所有非活跃的聊天室
     * 
     * @return 非活跃聊天室列表
     */
    List<ChatRoom> getInactiveChatRooms();
    
    /**
     * 根据活跃时间和消息数量自动调整聊天室状态
     * 定时任务调用，用于实现高频/低频模式自动切换
     */
    void autoAdjustChatRoomStatus();
    
    /**
     * 标记用户进入聊天室（触发高频模式）
     * 
     * @param roomId 房间ID
     * @param userId 用户ID
     */
    void markUserEnterRoom(String roomId, String userId);
    
    /**
     * 标记用户离开聊天室（可能触发低频模式）
     * 
     * @param roomId 房间ID
     * @param userId 用户ID
     */
    void markUserLeaveRoom(String roomId, String userId);
    
    /**
     * 获取聊天室当前在线用户数
     * 
     * @param roomId 房间ID
     * @return 在线用户数
     */
    int getOnlineUserCount(String roomId);
    
    /**
     * 切换聊天室为高频模式
     * 
     * @param roomId 房间ID
     */
    void switchToHighFrequencyMode(String roomId);
    
    /**
     * 切换聊天室为低频模式
     * 
     * @param roomId 房间ID
     */
    void switchToLowFrequencyMode(String roomId);
    
    /**
     * 更新聊天室的最后活跃时间
     * 
     * @param roomId 房间ID
     */
    void updateLastActiveTime(String roomId);
    
    /**
     * 更新聊天室的最后消息时间和消息计数
     * 
     * @param roomId 房间ID
     */
    void updateLastMessageTime(String roomId);
    
    /**
     * 更新聊天室的活跃成员数量
     * 
     * @param roomId 房间ID
     * @param memberCount 活跃成员数量
     */
    void updateActiveMemberCount(String roomId, Integer memberCount);
    
    /**
     * 获取聊天室统计信息
     * 
     * @param roomId 房间ID
     * @return 统计信息Map对象
     */
    Map<String, Object> getChatRoomStats(String roomId);
    
    /**
     * 搜索聊天室
     * 
     * @param keyword 关键词
     * @return 匹配的聊天室列表
     */
    List<ChatRoom> searchChatRooms(String keyword);
    
    /**
     * 获取需要清理的长时间无活动聊天室
     * 
     * @param daysInactive 无活动天数
     * @return 需要清理的聊天室列表
     */
    List<ChatRoom> getInactiveChatRooms(int daysInactive);
    
    /**
     * 批量清理长时间无活动的聊天室
     * 
     * @param daysInactive 无活动天数阈值
     * @return 清理的聊天室数量
     */
    int cleanupInactiveChatRooms(int daysInactive);
}