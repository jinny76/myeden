package com.myeden.service;

import com.myeden.entity.Robot;
import com.myeden.entity.GroupChatMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 聊天室提示词服务接口
 * 专门管理聊天室相关的提示词模板和内容生成
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
public interface ChatroomPromptService {
    
    /**
     * 构建基础聊天提示词
     * 
     * @param robot 机器人对象
     * @param chatContext 聊天上下文
     * @return 构建好的提示词
     */
    String buildBasicChatPrompt(Robot robot, String chatContext);
    
    /**
     * 构建回复用户消息的提示词
     * 
     * @param robot 机器人对象
     * @param userMessage 用户消息
     * @param chatContext 聊天上下文
     * @return 构建好的提示词
     */
    String buildReplyToUserPrompt(Robot robot, GroupChatMessage userMessage, String chatContext);
    
    /**
     * 构建话题切换提示词
     * 
     * @param robot 机器人对象
     * @param currentTime 当前时间
     * @return 构建好的提示词
     */
    String buildTopicSwitchPrompt(Robot robot, LocalDateTime currentTime);
    
    /**
     * 获取后备回复
     * 
     * @param robot 机器人对象
     * @return 后备回复内容
     */
    String getFallbackResponse(Robot robot);
    
    /**
     * 获取个性化问候语
     * 
     * @param robot 机器人对象
     * @param currentTime 当前时间
     * @return 问候语
     */
    String getTimeBasedGreeting(Robot robot, LocalDateTime currentTime);
    
    /**
     * 获取情境化回复
     * 
     * @param robot 机器人对象
     * @param contextType 情境类型（new_member_join, member_leave等）
     * @param parameters 参数Map
     * @return 情境化回复
     */
    String getContextualResponse(Robot robot, String contextType, Map<String, String> parameters);
    
    /**
     * 构建聊天上下文字符串
     * 
     * @param messages 消息列表
     * @param maxLength 最大长度
     * @return 格式化的上下文字符串
     */
    String buildChatContextString(List<GroupChatMessage> messages, int maxLength);
    
    /**
     * 根据机器人个性获取回复模板
     * 
     * @param robot 机器人对象
     * @param templateType 模板类型（agreement, disagreement等）
     * @return 个性化模板
     */
    String getPersonalityTemplate(Robot robot, String templateType);
    
    /**
     * 获取话题建议
     * 
     * @param topicType 话题类型
     * @param robot 机器人对象
     * @return 话题建议
     */
    String getTopicSuggestion(String topicType, Robot robot);
    
    /**
     * 处理提示词模板中的变量替换
     * 
     * @param template 模板字符串
     * @param variables 变量Map
     * @return 处理后的字符串
     */
    String processTemplate(String template, Map<String, String> variables);
    
    /**
     * 获取系统消息模板
     * 
     * @param messageType 消息类型
     * @param parameters 参数
     * @return 系统消息内容
     */
    String getSystemMessage(String messageType, Map<String, String> parameters);
    
    /**
     * 获取错误处理回复
     * 
     * @param errorType 错误类型
     * @return 错误处理回复
     */
    String getErrorHandlingResponse(String errorType);
    
    /**
     * 判断是否需要切换话题
     * 
     * @param messages 最近的消息列表
     * @param silenceDuration 沉默时长（分钟）
     * @return 是否需要切换话题
     */
    boolean shouldSwitchTopic(List<GroupChatMessage> messages, int silenceDuration);
    
    /**
     * 分析消息情感并选择合适的回复风格
     * 
     * @param message 消息内容
     * @param robot 机器人对象
     * @return 回复风格建议
     */
    String analyzeMessageSentimentAndGetStyle(String message, Robot robot);
}