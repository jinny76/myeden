package com.myeden.service;

import com.myeden.entity.WeChatConversation;

import java.util.List;

/**
 * 微信对话记录服务接口
 */
public interface WeChatConversationService {
    
    /**
     * 保存用户消息
     * 
     * @param userId 用户ID
     * @param content 消息内容
     * @param wechatMsgId 微信消息ID（可选，用于去重）
     * @return 保存的对话记录
     */
    WeChatConversation saveUserMessage(String userId, String content, String wechatMsgId);
    
    /**
     * 保存AI回复消息
     * 
     * @param userId 用户ID
     * @param content AI回复内容
     * @return 保存的对话记录
     */
    WeChatConversation saveAssistantMessage(String userId, String content);
    
    /**
     * 获取用户最近的对话历史（用于构建上下文）
     * 
     * @param userId 用户ID
     * @param limit 获取的对话条数，默认20条
     * @return 对话记录列表（按时间倒序）
     */
    List<WeChatConversation> getRecentConversations(String userId, int limit);
    
    /**
     * 获取用户最近的对话历史（默认获取20条）
     * 
     * @param userId 用户ID
     * @return 对话记录列表
     */
    List<WeChatConversation> getRecentConversations(String userId);
    
    /**
     * 构建对话上下文字符串（用于AI对话）
     * 
     * @param userId 用户ID
     * @param contextLimit 上下文对话条数限制
     * @return 格式化的对话上下文字符串
     */
    String buildConversationContext(String userId, int contextLimit);
    
    /**
     * 构建对话上下文字符串（默认10条对话）
     * 
     * @param userId 用户ID
     * @return 格式化的对话上下文字符串
     */
    String buildConversationContext(String userId);
    
    /**
     * 检查消息是否已存在（根据微信消息ID去重）
     * 
     * @param wechatMsgId 微信消息ID
     * @return true表示已存在，false表示不存在
     */
    boolean isMessageExists(String wechatMsgId);
    
    /**
     * 删除用户的所有对话记录
     * 
     * @param userId 用户ID
     */
    void deleteUserConversations(String userId);
    
    /**
     * 清理历史对话记录（保留最近指定天数的记录）
     * 
     * @param daysToKeep 保留天数
     */
    void cleanupOldConversations(int daysToKeep);
    
    /**
     * 获取用户对话统计信息
     * 
     * @param userId 用户ID
     * @return 对话总数
     */
    long getUserConversationCount(String userId);
}