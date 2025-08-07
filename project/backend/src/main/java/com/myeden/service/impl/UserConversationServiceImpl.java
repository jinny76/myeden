package com.myeden.service.impl;

import com.myeden.dto.coze.CozeConversationRequest;
import com.myeden.dto.coze.CozeConversationResponse;
import com.myeden.entity.UserConversation;
import com.myeden.repository.UserConversationRepository;
import com.myeden.service.CozeService;
import com.myeden.service.UserConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户对话关系服务实现类
 */
@Service
public class UserConversationServiceImpl implements UserConversationService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserConversationServiceImpl.class);
    
    @Autowired
    private UserConversationRepository userConversationRepository;
    
    @Autowired
    private CozeService cozeService;
    
    @Override
    public UserConversation getOrCreateConversation(String userId, String botId) {
        try {
            // 首先查找用户是否已有活跃的对话关系
            Optional<UserConversation> existingConversation = userConversationRepository.findByUserIdAndIsActiveTrue(userId);
            
            if (existingConversation.isPresent()) {
                UserConversation conversation = existingConversation.get();
                // 更新活跃时间
                conversation.updateActiveTime();
                userConversationRepository.save(conversation);
                logger.info("找到用户现有对话关系 - 用户ID: {}, 会话ID: {}", userId, conversation.getConversationId());
                return conversation;
            }
            
            // 如果用户没有对话关系，创建一个新的
            logger.info("用户没有对话关系，创建新的 - 用户ID: {}, 机器人ID: {}", userId, botId);
            
            // 调用Coze API创建新的会话
            CozeConversationRequest request = new CozeConversationRequest("用户" + userId + "的对话");
            CozeConversationResponse response = cozeService.createConversation(request);
            
            if (!response.isSuccess()) {
                logger.error("创建Coze会话失败 - 用户ID: {}, 错误: {}", userId, response.getMessage());
                throw new RuntimeException("创建Coze会话失败: " + response.getMessage());
            }
            
            // 创建新的对话关系
            UserConversation newConversation = new UserConversation(userId, response.getConversationId(), botId);
            newConversation.setRemark("自动创建的用户对话");
            
            UserConversation savedConversation = userConversationRepository.save(newConversation);
            logger.info("创建新的对话关系成功 - 用户ID: {}, 会话ID: {}", userId, savedConversation.getConversationId());
            
            return savedConversation;
            
        } catch (Exception e) {
            logger.error("获取或创建对话关系时发生异常 - 用户ID: {}", userId, e);
            throw new RuntimeException("获取或创建对话关系失败", e);
        }
    }
    
    @Override
    public Optional<UserConversation> findByUserId(String userId) {
        return userConversationRepository.findByUserId(userId);
    }
    
    @Override
    public Optional<UserConversation> findActiveByUserId(String userId) {
        return userConversationRepository.findByUserIdAndIsActiveTrue(userId);
    }
    
    @Override
    public UserConversation save(UserConversation userConversation) {
        return userConversationRepository.save(userConversation);
    }
    
    @Override
    public void updateActiveTime(String userId) {
        Optional<UserConversation> conversation = userConversationRepository.findByUserIdAndIsActiveTrue(userId);
        if (conversation.isPresent()) {
            UserConversation userConversation = conversation.get();
            userConversation.updateActiveTime();
            userConversationRepository.save(userConversation);
            logger.debug("更新用户活跃时间 - 用户ID: {}", userId);
        }
    }
    
    @Override
    public void deactivate(String userId) {
        Optional<UserConversation> conversation = userConversationRepository.findByUserIdAndIsActiveTrue(userId);
        if (conversation.isPresent()) {
            UserConversation userConversation = conversation.get();
            userConversation.setInactive();
            userConversationRepository.save(userConversation);
            logger.info("设置用户对话关系为非活跃 - 用户ID: {}", userId);
        }
    }
    
    @Override
    public void delete(String userId) {
        userConversationRepository.deleteByUserId(userId);
        logger.info("删除用户对话关系 - 用户ID: {}", userId);
    }
    
    @Override
    public int cleanupExpiredConversations(int days) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(days);
        List<UserConversation> expiredConversations = userConversationRepository.findByLastActiveAtBefore(cutoffTime);
        
        for (UserConversation conversation : expiredConversations) {
            conversation.setInactive();
            userConversationRepository.save(conversation);
        }
        
        logger.info("清理过期对话关系 - 清理数量: {}, 过期天数: {}", expiredConversations.size(), days);
        return expiredConversations.size();
    }
    
    @Override
    public List<UserConversation> findAllActive() {
        return userConversationRepository.findByIsActiveTrue();
    }
    
    @Override
    public List<UserConversation> findByBotId(String botId) {
        return userConversationRepository.findByBotIdAndIsActiveTrue(botId);
    }
}
