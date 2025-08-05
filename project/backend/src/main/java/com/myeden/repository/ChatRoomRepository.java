package com.myeden.repository;

import com.myeden.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 聊天室数据访问接口
 * 负责聊天室的CRUD操作和查询
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    
    /**
     * 根据房间ID查找聊天室
     * @param roomId 房间ID
     * @return 聊天室对象
     */
    Optional<ChatRoom> findByRoomId(String roomId);
    
    /**
     * 根据房间ID判断聊天室是否存在
     * @param roomId 房间ID
     * @return 是否存在
     */
    boolean existsByRoomId(String roomId);
    
    /**
     * 查找所有活跃状态的聊天室
     * @return 活跃聊天室列表
     */
    List<ChatRoom> findByStatus(String status);
    
    /**
     * 查找指定时间后有活动的聊天室
     * @param sinceTime 时间点
     * @return 聊天室列表
     */
    List<ChatRoom> findByLastActiveAtAfter(LocalDateTime sinceTime);
    
    /**
     * 查找指定时间后有消息的聊天室
     * @param sinceTime 时间点
     * @return 聊天室列表
     */
    List<ChatRoom> findByLastMessageAtAfter(LocalDateTime sinceTime);
    
    /**
     * 按创建时间降序查找所有聊天室
     * @return 聊天室列表
     */
    List<ChatRoom> findAllByOrderByCreatedAtDesc();
    
    /**
     * 按最后活跃时间降序查找所有聊天室
     * @return 聊天室列表
     */
    List<ChatRoom> findAllByOrderByLastActiveAtDesc();
    
    /**
     * 查找消息数量大于指定值的聊天室
     * @param messageCount 消息数量阈值
     * @return 聊天室列表
     */
    List<ChatRoom> findByMessageCountGreaterThan(Integer messageCount);
    
    /**
     * 查找活跃成员数量大于指定值的聊天室
     * @param memberCount 成员数量阈值
     * @return 聊天室列表
     */
    List<ChatRoom> findByActiveMemberCountGreaterThan(Integer memberCount);
    
    /**
     * 统计指定状态的聊天室数量
     * @param status 状态
     * @return 数量
     */
    long countByStatus(String status);
    
    /**
     * 统计指定时间后活跃的聊天室数量
     * @param sinceTime 时间点
     * @return 数量
     */
    long countByLastActiveAtAfter(LocalDateTime sinceTime);
    
    /**
     * 根据房间名称模糊搜索聊天室
     * @param keyword 关键词
     * @return 聊天室列表
     */
    @Query("{'roomName': {$regex: ?0, $options: 'i'}}")
    List<ChatRoom> findByRoomNameContainingIgnoreCase(String keyword);
    
    /**
     * 查找长时间无活动的聊天室（用于清理或提醒）
     * @param beforeTime 时间点
     * @return 聊天室列表
     */
    List<ChatRoom> findByLastActiveAtBefore(LocalDateTime beforeTime);
    
    /**
     * 删除指定房间ID的聊天室
     * @param roomId 房间ID
     */
    void deleteByRoomId(String roomId);
}