package com.myeden.service;

import com.myeden.dto.coze.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Coze API服务接口
 */
public interface CozeService {
    
    /**
     * 创建对话
     * 
     * @param request 创建对话请求
     * @return 对话响应
     */
    CozeConversationResponse createConversation(CozeConversationRequest request);
    
    /**
     * 发送聊天消息 (非流式)
     * 
     * @param request 聊天请求
     * @return 聊天响应
     */
    CozeChatResponse chat(CozeChatRequest request);
    
    /**
     * 发送简单文本消息
     * 
     * @param botId 机器人ID
     * @param userId 用户ID
     * @param message 消息内容
     * @return 聊天响应
     */
    CozeChatResponse sendMessage(String botId, String userId, String message);
    
    /**
     * 在指定对话中发送消息
     * 
     * @param botId 机器人ID
     * @param userId 用户ID
     * @param conversationId 对话ID
     * @param message 消息内容
     * @return 聊天响应
     */
    CozeChatResponse sendMessage(String botId, String userId, String conversationId, String message);
    
    /**
     * 上传文件
     * 
     * @param file 文件
     * @param purpose 用途：assistants
     * @return 文件上传响应
     */
    CozeFileResponse uploadFile(MultipartFile file, String purpose);
    
    /**
     * 获取对话历史
     * 
     * @param conversationId 对话ID
     * @param limit 限制条数
     * @param offset 偏移量
     * @return 消息列表
     */
    List<CozeMessage> getConversationHistory(String conversationId, Integer limit, Integer offset);
    
    /**
     * 获取服务状态
     * 
     * @return 是否可用
     */
    boolean isServiceAvailable();
    
    /**
     * 验证API配置
     * 
     * @return 配置是否有效
     */
    boolean validateConfiguration();
    
    /**
     * 获取对话详细信息
     * 
     * @param chatId 对话ID
     * @param conversationId 会话ID
     * @return 对话详细信息
     */
    CozeChatDetailResponse getChatDetail(String chatId, String conversationId);
    
    /**
     * 获取默认的机器人ID
     * 
     * @return 默认机器人ID
     */
    String getDefaultBotId();
    
    /**
     * 获取对话消息详情
     * 查看指定对话中除Query以外的其他消息，包括模型回复、智能体执行的中间结果等消息
     * 
     * @param conversationId 会话ID
     * @param chatId 对话ID
     * @return 消息详情响应
     */
    CozeMessageDetailResponse getMessageDetails(String conversationId, String chatId);
}