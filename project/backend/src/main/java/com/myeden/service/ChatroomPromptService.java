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
     * 获取后备回复
     * 
     * @param robot 机器人对象
     * @return 后备回复内容
     */
    String getFallbackResponse(Robot robot);
    
    /**
     * 构建聊天上下文字符串
     * 
     * @param messages 消息列表
     * @param maxLength 最大长度
     * @return 格式化的上下文字符串
     */
    String buildChatContextString(List<GroupChatMessage> messages, int maxLength);
    
    /**
     * 处理提示词模板中的变量替换
     * 
     * @param template 模板字符串
     * @param variables 变量Map
     * @return 处理后的字符串
     */
    String processTemplate(String template, Map<String, String> variables);
    
    /**
     * 构建包含聊天室成员信息的提示词
     * 
     * @param robot 当前发言的机器人
     * @param roomId 聊天室ID
     * @param chatContext 聊天上下文
     * @return 包含成员信息的提示词
     */
    String buildChatPromptWithMemberInfo(Robot robot, String roomId, String chatContext);
}