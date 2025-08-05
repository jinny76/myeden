package com.myeden.repository;

import com.myeden.entity.ChatRoomMember;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 聊天室成员数据访问接口
 * 负责聊天室成员的CRUD操作和查询
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
@Repository
public interface ChatRoomMemberRepository extends MongoRepository<ChatRoomMember, String> {
    
    /**
     * 根据房间ID查找所有成员
     * @param roomId 房间ID
     * @return 成员列表
     */
    List<ChatRoomMember> findByRoomId(String roomId);
    
    /**
     * 根据房间ID和成员ID查找成员
     * @param roomId 房间ID
     * @param memberId 成员ID
     * @return 成员对象
     */
    Optional<ChatRoomMember> findByRoomIdAndMemberId(String roomId, String memberId);
    
    /**
     * 根据房间ID和成员类型查找成员
     * @param roomId 房间ID
     * @param memberType 成员类型
     * @return 成员列表
     */
    List<ChatRoomMember> findByRoomIdAndMemberType(String roomId, String memberType);
    
    /**
     * 根据房间ID查找所有在线成员
     * @param roomId 房间ID
     * @param isOnline 是否在线
     * @return 成员列表
     */
    List<ChatRoomMember> findByRoomIdAndIsOnline(String roomId, Boolean isOnline);
    
    /**
     * 根据房间ID查找所有未被静音的成员
     * @param roomId 房间ID
     * @param isMuted 是否被静音
     * @return 成员列表
     */
    List<ChatRoomMember> findByRoomIdAndIsMuted(String roomId, Boolean isMuted);
    
    /**
     * 根据房间ID和角色查找成员
     * @param roomId 房间ID
     * @param role 角色
     * @return 成员列表
     */
    List<ChatRoomMember> findByRoomIdAndRole(String roomId, String role);
    
    /**
     * 根据成员ID查找所有参与的聊天室
     * @param memberId 成员ID
     * @return 成员列表
     */
    List<ChatRoomMember> findByMemberId(String memberId);
    
    /**
     * 根据成员ID和成员类型查找所有参与的聊天室
     * @param memberId 成员ID
     * @param memberType 成员类型
     * @return 成员列表
     */
    List<ChatRoomMember> findByMemberIdAndMemberType(String memberId, String memberType);
    
    /**
     * 统计房间内指定类型的成员数量
     * @param roomId 房间ID
     * @param memberType 成员类型
     * @return 数量
     */
    long countByRoomIdAndMemberType(String roomId, String memberType);
    
    /**
     * 统计房间内在线成员数量
     * @param roomId 房间ID
     * @param isOnline 是否在线
     * @return 数量
     */
    long countByRoomIdAndIsOnline(String roomId, Boolean isOnline);
    
    /**
     * 统计房间内总成员数量
     * @param roomId 房间ID
     * @return 数量
     */
    long countByRoomId(String roomId);
    
    /**
     * 判断成员是否存在于房间中
     * @param roomId 房间ID
     * @param memberId 成员ID
     * @return 是否存在
     */
    boolean existsByRoomIdAndMemberId(String roomId, String memberId);
    
    /**
     * 查找指定时间后活跃的房间成员
     * @param roomId 房间ID
     * @param sinceTime 时间点
     * @return 成员列表
     */
    List<ChatRoomMember> findByRoomIdAndLastActiveAtAfter(String roomId, LocalDateTime sinceTime);
    
    /**
     * 按加入时间降序查找房间成员
     * @param roomId 房间ID
     * @return 成员列表
     */
    List<ChatRoomMember> findByRoomIdOrderByJoinedAtDesc(String roomId);
    
    /**
     * 按最后活跃时间降序查找房间成员
     * @param roomId 房间ID
     * @return 成员列表
     */
    List<ChatRoomMember> findByRoomIdOrderByLastActiveAtDesc(String roomId);
    
    /**
     * 查找房间内的机器人成员且在线
     * @param roomId 房间ID
     * @return 机器人成员列表
     */
    @Query("{'roomId': ?0, 'memberType': 'ROBOT', 'isOnline': true}")
    List<ChatRoomMember> findActiveRobotsByRoomId(String roomId);
    
    /**
     * 查找房间内的用户成员且在线
     * @param roomId 房间ID
     * @return 用户成员列表
     */
    @Query("{'roomId': ?0, 'memberType': 'USER', 'isOnline': true}")
    List<ChatRoomMember> findActiveUsersByRoomId(String roomId);
    
    /**
     * 删除房间内的指定成员
     * @param roomId 房间ID
     * @param memberId 成员ID
     */
    void deleteByRoomIdAndMemberId(String roomId, String memberId);
    
    /**
     * 删除房间内的所有成员
     * @param roomId 房间ID
     */
    void deleteByRoomId(String roomId);
    
    /**
     * 删除指定成员的所有房间关系
     * @param memberId 成员ID
     */
    void deleteByMemberId(String memberId);
}