package com.myeden.service;

import com.myeden.entity.ChatRoomMember;
import java.util.List;
import java.util.Optional;

/**
 * 聊天室成员服务接口
 * 负责聊天室成员的管理、状态同步和权限控制
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
public interface ChatRoomMemberService {
    
    /**
     * 添加成员到聊天室
     * 
     * @param roomId 房间ID
     * @param memberType 成员类型（USER/ROBOT）
     * @param memberId 成员ID
     * @param memberNickname 成员昵称
     * @param memberAvatar 成员头像
     * @return 成员对象
     */
    ChatRoomMember addMemberToChatRoom(String roomId, String memberType, String memberId, 
                                       String memberNickname, String memberAvatar);
    
    /**
     * 从聊天室移除成员
     * 
     * @param roomId 房间ID
     * @param memberId 成员ID
     * @return 是否移除成功
     */
    boolean removeMemberFromChatRoom(String roomId, String memberId);
    
    /**
     * 获取聊天室所有成员
     * 
     * @param roomId 房间ID
     * @return 成员列表
     */
    List<ChatRoomMember> getChatRoomMembers(String roomId);
    
    /**
     * 获取聊天室在线成员
     * 
     * @param roomId 房间ID
     * @return 在线成员列表
     */
    List<ChatRoomMember> getOnlineMembers(String roomId);
    
    /**
     * 获取聊天室机器人成员
     * 
     * @param roomId 房间ID
     * @return 机器人成员列表
     */
    List<ChatRoomMember> getRobotMembers(String roomId);
    
    /**
     * 获取聊天室在线机器人成员
     * 
     * @param roomId 房间ID
     * @return 在线机器人成员列表
     */
    List<ChatRoomMember> getActiveRobotMembers(String roomId);
    
    /**
     * 获取聊天室用户成员
     * 
     * @param roomId 房间ID
     * @return 用户成员列表
     */
    List<ChatRoomMember> getUserMembers(String roomId);
    
    /**
     * 根据成员ID获取成员信息
     * 
     * @param roomId 房间ID
     * @param memberId 成员ID
     * @return 成员对象
     */
    Optional<ChatRoomMember> getChatRoomMember(String roomId, String memberId);
    
    /**
     * 更新成员在线状态
     * 
     * @param roomId 房间ID
     * @param memberId 成员ID
     * @param isOnline 是否在线
     * @return 是否更新成功
     */
    boolean updateMemberOnlineStatus(String roomId, String memberId, Boolean isOnline);
    
    /**
     * 更新成员最后活跃时间
     * 
     * @param roomId 房间ID
     * @param memberId 成员ID
     */
    void updateMemberLastActiveTime(String roomId, String memberId);
    
    /**
     * 设置成员静音状态
     * 
     * @param roomId 房间ID
     * @param memberId 成员ID
     * @param isMuted 是否静音
     * @return 是否设置成功
     */
    boolean setMemberMuteStatus(String roomId, String memberId, Boolean isMuted);
    
    /**
     * 检查成员是否存在于聊天室
     * 
     * @param roomId 房间ID
     * @param memberId 成员ID
     * @return 是否存在
     */
    boolean isMemberInChatRoom(String roomId, String memberId);
    
    /**
     * 检查成员是否为房主
     * 
     * @param roomId 房间ID
     * @param memberId 成员ID
     * @return 是否为房主
     */
    boolean isChatRoomOwner(String roomId, String memberId);
    
    /**
     * 获取聊天室房主
     * 
     * @param roomId 房间ID
     * @return 房主成员对象
     */
    ChatRoomMember getChatRoomOwner(String roomId);
    
    /**
     * 统计聊天室成员数量
     * 
     * @param roomId 房间ID
     * @return 成员数量
     */
    long countChatRoomMembers(String roomId);
    
    /**
     * 统计聊天室在线成员数量
     * 
     * @param roomId 房间ID
     * @return 在线成员数量
     */
    long countOnlineMembers(String roomId);
    
    /**
     * 统计聊天室机器人成员数量
     * 
     * @param roomId 房间ID
     * @return 机器人成员数量
     */
    long countRobotMembers(String roomId);
    
    /**
     * 根据机器人活跃状态刷新聊天室机器人在线状态
     * 由定时任务调用，同步机器人的活跃状态到聊天室成员状态
     * 
     * @param roomId 房间ID
     */
    void refreshRobotOnlineStatus(String roomId);
    
    /**
     * 批量添加机器人到聊天室
     * 
     * @param roomId 房间ID
     * @param robotIds 机器人ID列表
     * @return 成功添加的数量
     */
    int batchAddRobotsToRoom(String roomId, List<String> robotIds);
    
    /**
     * 批量从聊天室移除机器人
     * 
     * @param roomId 房间ID
     * @param robotIds 机器人ID列表
     * @return 成功移除的数量
     */
    int batchRemoveRobotsFromRoom(String roomId, List<String> robotIds);
    
    /**
     * 清理聊天室的所有成员
     * 
     * @param roomId 房间ID
     * @return 清理的成员数量
     */
    int clearChatRoomMembers(String roomId);
}