package com.myeden.repository;

import com.myeden.entity.UserConversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户对话关系Repository
 * 用于管理用户和Coze conversationId的对应关系
 */
@Repository
public interface UserConversationRepository extends MongoRepository<UserConversation, String> {
    
    /**
     * 根据用户ID查找对话关系
     * 
     * @param userId 用户ID
     * @return 对话关系
     */
    Optional<UserConversation> findByUserId(String userId);
    
    /**
     * 根据用户ID查找活跃的对话关系
     * 
     * @param userId 用户ID
     * @return 活跃的对话关系
     */
    Optional<UserConversation> findByUserIdAndIsActiveTrue(String userId);
    
    /**
     * 根据会话ID查找对话关系
     * 
     * @param conversationId 会话ID
     * @return 对话关系
     */
    Optional<UserConversation> findByConversationId(String conversationId);
    
    /**
     * 查找所有活跃的对话关系
     * 
     * @return 活跃的对话关系列表
     */
    List<UserConversation> findByIsActiveTrue();
    
    /**
     * 查找最后活跃时间在指定时间之前的对话关系
     * 
     * @param lastActiveAt 最后活跃时间
     * @return 对话关系列表
     */
    List<UserConversation> findByLastActiveAtBefore(LocalDateTime lastActiveAt);
    
    /**
     * 根据机器人ID查找对话关系
     * 
     * @param botId 机器人ID
     * @return 对话关系列表
     */
    List<UserConversation> findByBotId(String botId);
    
    /**
     * 根据机器人ID查找活跃的对话关系
     * 
     * @param botId 机器人ID
     * @return 活跃的对话关系列表
     */
    List<UserConversation> findByBotIdAndIsActiveTrue(String botId);
    
    /**
     * 检查用户是否存在对话关系
     * 
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByUserId(String userId);
    
    /**
     * 检查用户是否存在活跃的对话关系
     * 
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByUserIdAndIsActiveTrue(String userId);
    
    /**
     * 删除用户的对话关系
     * 
     * @param userId 用户ID
     */
    void deleteByUserId(String userId);
    
    /**
     * 将用户的所有对话关系设置为非活跃
     * 
     * @param userId 用户ID
     */
    @Query("{'userId': ?0}")
    void deactivateByUserId(String userId);
}
