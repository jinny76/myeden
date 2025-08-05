package com.myeden.repository;

import com.myeden.entity.GroupChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 群聊消息数据访问接口
 * 负责群聊消息的CRUD操作和查询
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
@Repository
public interface GroupChatMessageRepository extends MongoRepository<GroupChatMessage, String> {
    
    /**
     * 根据房间ID查找消息（按时间正序）
     * @param roomId 房间ID
     * @return 消息列表
     */
    List<GroupChatMessage> findByRoomIdAndIsDeletedFalseOrderBySentAtAsc(String roomId);
    
    /**
     * 根据房间ID分页查找消息（按时间倒序）
     * @param roomId 房间ID
     * @param pageable 分页参数
     * @return 消息分页
     */
    Page<GroupChatMessage> findByRoomIdAndIsDeletedFalseOrderBySentAtDesc(String roomId, Pageable pageable);
    
    /**
     * 根据房间ID查找最新的N条消息
     * @param roomId 房间ID
     * @param limit 限制数量
     * @return 消息列表
     */
    @Query(value = "{'roomId': ?0, 'isDeleted': false}", sort = "{'sentAt': -1}")
    List<GroupChatMessage> findLatestMessagesByRoomId(String roomId, int limit);
    
    /**
     * 根据房间ID和发送者ID查找消息
     * @param roomId 房间ID
     * @param senderId 发送者ID
     * @return 消息列表
     */
    List<GroupChatMessage> findByRoomIdAndSenderIdAndIsDeletedFalseOrderBySentAtDesc(String roomId, String senderId);
    
    /**
     * 根据房间ID和发送者类型查找消息
     * @param roomId 房间ID
     * @param senderType 发送者类型
     * @return 消息列表
     */
    List<GroupChatMessage> findByRoomIdAndSenderTypeAndIsDeletedFalseOrderBySentAtDesc(String roomId, String senderType);
    
    /**
     * 根据房间ID和消息类型查找消息
     * @param roomId 房间ID
     * @param messageType 消息类型
     * @return 消息列表
     */
    List<GroupChatMessage> findByRoomIdAndMessageTypeAndIsDeletedFalseOrderBySentAtDesc(String roomId, String messageType);
    
    /**
     * 根据房间ID查找系统消息
     * @param roomId 房间ID
     * @param isSystemMessage 是否为系统消息
     * @return 消息列表
     */
    List<GroupChatMessage> findByRoomIdAndIsSystemMessageAndIsDeletedFalseOrderBySentAtDesc(String roomId, Boolean isSystemMessage);
    
    /**
     * 根据房间ID和时间范围查找消息
     * @param roomId 房间ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消息列表
     */
    List<GroupChatMessage> findByRoomIdAndSentAtBetweenAndIsDeletedFalseOrderBySentAtAsc(String roomId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据房间ID查找指定时间后的消息
     * @param roomId 房间ID
     * @param sinceTime 时间点
     * @return 消息列表
     */
    List<GroupChatMessage> findByRoomIdAndSentAtAfterAndIsDeletedFalseOrderBySentAtAsc(String roomId, LocalDateTime sinceTime);
    
    /**
     * 根据回复消息ID查找所有回复
     * @param replyToId 回复消息ID
     * @return 消息列表
     */
    List<GroupChatMessage> findByReplyToIdAndIsDeletedFalseOrderBySentAtAsc(String replyToId);
    
    /**
     * 统计房间内消息总数
     * @param roomId 房间ID
     * @return 消息数量
     */
    long countByRoomIdAndIsDeletedFalse(String roomId);
    
    /**
     * 统计房间内指定发送者的消息数量
     * @param roomId 房间ID
     * @param senderId 发送者ID
     * @return 消息数量
     */
    long countByRoomIdAndSenderIdAndIsDeletedFalse(String roomId, String senderId);
    
    /**
     * 统计房间内指定类型的消息数量
     * @param roomId 房间ID
     * @param senderType 发送者类型
     * @return 消息数量
     */
    long countByRoomIdAndSenderTypeAndIsDeletedFalse(String roomId, String senderType);
    
    /**
     * 统计房间内指定时间后的消息数量
     * @param roomId 房间ID
     * @param sinceTime 时间点
     * @return 消息数量
     */
    long countByRoomIdAndSentAtAfterAndIsDeletedFalse(String roomId, LocalDateTime sinceTime);
    
    /**
     * 查找房间内最后一条消息
     * @param roomId 房间ID
     * @return 最后一条消息
     */
    Optional<GroupChatMessage> findFirstByRoomIdAndIsDeletedFalseOrderBySentAtDesc(String roomId);
    
    /**
     * 查找房间内第一条消息
     * @param roomId 房间ID
     * @return 第一条消息
     */
    Optional<GroupChatMessage> findFirstByRoomIdAndIsDeletedFalseOrderBySentAtAsc(String roomId);
    
    /**
     * 根据消息内容模糊搜索
     * @param roomId 房间ID
     * @param keyword 关键词
     * @return 消息列表
     */
    @Query("{'roomId': ?0, 'content': {$regex: ?1, $options: 'i'}, 'isDeleted': false}")
    List<GroupChatMessage> searchByContent(String roomId, String keyword);
    
    /**
     * 查找房间内包含图片的消息
     * @param roomId 房间ID
     * @return 消息列表
     */
    @Query("{'roomId': ?0, 'imageUrl': {$ne: null, $ne: ''}, 'isDeleted': false}")
    List<GroupChatMessage> findImageMessagesByRoomId(String roomId);
    
    /**
     * 删除房间内所有消息（物理删除）
     * @param roomId 房间ID
     */
    void deleteByRoomId(String roomId);
    
    /**
     * 根据发送者ID删除所有消息（物理删除）
     * @param senderId 发送者ID
     */
    void deleteBySenderId(String senderId);
    
    /**
     * 删除指定时间前的消息（用于定期清理）
     * @param beforeTime 时间点
     */
    void deleteBySentAtBefore(LocalDateTime beforeTime);
}