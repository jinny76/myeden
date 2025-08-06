package com.myeden.repository;

import com.myeden.entity.WeChatConversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 微信对话记录Repository接口
 */
@Repository
public interface WeChatConversationRepository extends MongoRepository<WeChatConversation, String> {
    
    /**
     * 根据用户ID查找最近的对话记录
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 对话记录列表
     */
    @Query("{'userId': ?0}")
    List<WeChatConversation> findByUserIdOrderByMessageTimeDesc(String userId, Pageable pageable);
    
    /**
     * 根据用户ID和时间范围查找对话记录
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 对话记录列表
     */
    @Query("{'userId': ?0, 'messageTime': {'$gte': ?1, '$lte': ?2}}")
    List<WeChatConversation> findByUserIdAndMessageTimeBetweenOrderByMessageTimeDesc(
            String userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 根据微信消息ID查找记录（用于去重）
     * 
     * @param wechatMsgId 微信消息ID
     * @return 对话记录
     */
    Optional<WeChatConversation> findByWechatMsgId(String wechatMsgId);
    
    /**
     * 根据用户ID查找最近的指定数量的对话记录
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 对话记录列表
     */
    @Query("{'userId': ?0}")
    List<WeChatConversation> findTopByUserIdOrderByMessageTimeDesc(String userId, Pageable pageable);
    
    /**
     * 删除指定时间之前的对话记录（用于清理历史数据）
     * 
     * @param beforeTime 删除此时间之前的记录
     */
    void deleteByMessageTimeBefore(LocalDateTime beforeTime);
    
    /**
     * 统计用户的对话总数
     * 
     * @param userId 用户ID
     * @return 对话总数
     */
    long countByUserId(String userId);
    
    /**
     * 根据用户ID和消息类型查找最近的对话记录
     * 
     * @param userId 用户ID
     * @param messageType 消息类型
     * @param pageable 分页参数
     * @return 对话记录列表
     */
    @Query("{'userId': ?0, 'messageType': ?1}")
    List<WeChatConversation> findByUserIdAndMessageTypeOrderByMessageTimeDesc(
            String userId, String messageType, Pageable pageable);
    
    /**
     * 查找用户最后一条消息
     * 
     * @param userId 用户ID
     * @return 最后一条消息
     */
    @Query("{'userId': ?0}")
    Optional<WeChatConversation> findFirstByUserIdOrderByMessageTimeDesc(String userId);
}