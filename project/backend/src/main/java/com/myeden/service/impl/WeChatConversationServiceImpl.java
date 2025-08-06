package com.myeden.service.impl;

import com.myeden.entity.WeChatConversation;
import com.myeden.repository.WeChatConversationRepository;
import com.myeden.service.WeChatConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * 微信对话记录服务实现类
 */
@Service
public class WeChatConversationServiceImpl implements WeChatConversationService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeChatConversationServiceImpl.class);
    
    private static final int DEFAULT_CONTEXT_LIMIT = 20;
    private static final int DEFAULT_AI_CONTEXT_LIMIT = 10;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    private final WeChatConversationRepository conversationRepository;
    
    @Autowired
    public WeChatConversationServiceImpl(WeChatConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }
    
    @Override
    public WeChatConversation saveUserMessage(String userId, String content, String wechatMsgId) {
        try {
            // 检查消息是否已存在（去重）
            if (wechatMsgId != null && isMessageExists(wechatMsgId)) {
                logger.warn("消息已存在，跳过保存: wechatMsgId={}", wechatMsgId);
                return conversationRepository.findByWechatMsgId(wechatMsgId).orElse(null);
            }
            
            WeChatConversation conversation = new WeChatConversation(
                userId, 
                WeChatConversation.MessageType.USER, 
                content, 
                wechatMsgId
            );
            
            WeChatConversation saved = conversationRepository.save(conversation);
            logger.info("保存用户消息成功: userId={}, content={}", userId, content);
            return saved;
            
        } catch (Exception e) {
            logger.error("保存用户消息失败: userId={}, content={}", userId, content, e);
            return null;
        }
    }
    
    @Override
    public WeChatConversation saveAssistantMessage(String userId, String content) {
        try {
            WeChatConversation conversation = new WeChatConversation(
                userId, 
                WeChatConversation.MessageType.ASSISTANT, 
                content
            );
            
            WeChatConversation saved = conversationRepository.save(conversation);
            logger.info("保存AI回复消息成功: userId={}, content={}", userId, content);
            return saved;
            
        } catch (Exception e) {
            logger.error("保存AI回复消息失败: userId={}, content={}", userId, content, e);
            return null;
        }
    }
    
    @Override
    public List<WeChatConversation> getRecentConversations(String userId, int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<WeChatConversation> conversations = conversationRepository
                .findByUserIdOrderByMessageTimeDesc(userId, pageable);
            
            // 按时间正序排列（最早的在前面）
            Collections.reverse(conversations);
            
            logger.debug("获取用户最近对话记录: userId={}, count={}", userId, conversations.size());
            return conversations;
            
        } catch (Exception e) {
            logger.error("获取用户最近对话记录失败: userId={}, limit={}", userId, limit, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<WeChatConversation> getRecentConversations(String userId) {
        return getRecentConversations(userId, DEFAULT_CONTEXT_LIMIT);
    }
    
    @Override
    public String buildConversationContext(String userId, int contextLimit) {
        try {
            List<WeChatConversation> conversations = getRecentConversations(userId, contextLimit);
            
            if (conversations.isEmpty()) {
                return "这是一个新的对话。";
            }
            
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("以下是最近的对话记录：\n\n");
            
            for (WeChatConversation conv : conversations) {
                String timeStr = conv.getMessageTime().format(TIME_FORMATTER);
                
                if (WeChatConversation.MessageType.USER.equals(conv.getMessageType())) {
                    contextBuilder.append(String.format("[%s] 用户: %s\n", timeStr, conv.getContent()));
                } else if (WeChatConversation.MessageType.ASSISTANT.equals(conv.getMessageType())) {
                    contextBuilder.append(String.format("[%s] 翠鸟小新新: %s\n", timeStr, conv.getContent()));
                }
            }
            
            contextBuilder.append("\n---\n\n");
            
            String context = contextBuilder.toString();
            logger.debug("构建对话上下文: userId={}, contextLength={}", userId, context.length());
            return context;
            
        } catch (Exception e) {
            logger.error("构建对话上下文失败: userId={}, contextLimit={}", userId, contextLimit, e);
            return "无法获取对话历史。";
        }
    }
    
    @Override
    public String buildConversationContext(String userId) {
        return buildConversationContext(userId, DEFAULT_AI_CONTEXT_LIMIT);
    }
    
    @Override
    public boolean isMessageExists(String wechatMsgId) {
        try {
            if (wechatMsgId == null || wechatMsgId.trim().isEmpty()) {
                return false;
            }
            
            return conversationRepository.findByWechatMsgId(wechatMsgId).isPresent();
            
        } catch (Exception e) {
            logger.error("检查消息是否存在失败: wechatMsgId={}", wechatMsgId, e);
            return false;
        }
    }
    
    @Override
    public void deleteUserConversations(String userId) {
        try {
            List<WeChatConversation> userConversations = conversationRepository
                .findByUserIdOrderByMessageTimeDesc(userId, Pageable.unpaged());
            
            if (!userConversations.isEmpty()) {
                conversationRepository.deleteAll(userConversations);
                logger.info("删除用户所有对话记录: userId={}, count={}", userId, userConversations.size());
            }
            
        } catch (Exception e) {
            logger.error("删除用户对话记录失败: userId={}", userId, e);
        }
    }
    
    @Override
    public void cleanupOldConversations(int daysToKeep) {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
            conversationRepository.deleteByMessageTimeBefore(cutoffTime);
            
            logger.info("清理历史对话记录完成: 保留{}天内的记录", daysToKeep);
            
        } catch (Exception e) {
            logger.error("清理历史对话记录失败: daysToKeep={}", daysToKeep, e);
        }
    }
    
    @Override
    public long getUserConversationCount(String userId) {
        try {
            return conversationRepository.countByUserId(userId);
            
        } catch (Exception e) {
            logger.error("获取用户对话统计失败: userId={}", userId, e);
            return 0;
        }
    }
}