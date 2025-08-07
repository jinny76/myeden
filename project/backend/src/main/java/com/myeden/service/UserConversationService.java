package com.myeden.service;

import com.myeden.entity.UserConversation;
import java.util.List;
import java.util.Optional;

/**
 * 用户对话关系服务接口
 * 用于管理用户和Coze conversationId的对应关系
 */
public interface UserConversationService {
    
    /**
     * 根据用户ID获取或创建对话关系
     * 如果用户没有对话关系，则创建一个新的
     * 
     * @param userId 用户ID
     * @param botId 机器人ID
     * @return 对话关系
     */
    UserConversation getOrCreateConversation(String userId, String botId);
    
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
    Optional<UserConversation> findActiveByUserId(String userId);
    
    /**
     * 保存对话关系
     * 
     * @param userConversation 对话关系
     * @return 保存后的对话关系
     */
    UserConversation save(UserConversation userConversation);
    
    /**
     * 更新对话关系的活跃时间
     * 
     * @param userId 用户ID
     */
    void updateActiveTime(String userId);
    
    /**
     * 设置对话关系为非活跃
     * 
     * @param userId 用户ID
     */
    void deactivate(String userId);
    
    /**
     * 删除对话关系
     * 
     * @param userId 用户ID
     */
    void delete(String userId);
    
    /**
     * 清理过期的对话关系
     * 
     * @param days 过期天数
     * @return 清理的数量
     */
    int cleanupExpiredConversations(int days);
    
    /**
     * 获取所有活跃的对话关系
     * 
     * @return 活跃的对话关系列表
     */
    List<UserConversation> findAllActive();
    
    /**
     * 根据机器人ID获取对话关系
     * 
     * @param botId 机器人ID
     * @return 对话关系列表
     */
    List<UserConversation> findByBotId(String botId);
}
