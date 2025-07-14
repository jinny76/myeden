package com.myeden.service;

import com.myeden.entity.Robot;
import com.myeden.service.impl.DifyImageResult;

/**
 * Dify API集成服务接口
 * 专门负责与Dify API的通讯，包括调用API、处理响应、管理连接等
 * 提示词构建和内容处理逻辑已抽提到PromptService中
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface DifyService {
    
    /**
     * 调用Dify API生成内容
     * 向Dify API发送提示词并获取生成的内容
     * 
     * @param prompt 提示词
     * @param userId 机器人信息（用于API配置）
     * @return 生成的内容
     */
    DifyChatResult callDifyApi(String prompt, String userId, String appKey);
    
    /**
     * 调用Dify API，支持传递conversationId以实现多轮对话
     * @param prompt 用户输入的提示词
     * @param userId 用户唯一标识
     * @param conversationId 会话ID（可为null或空字符串，表示无上下文）
     * @return Dify API返回的回复内容
     */
    DifyChatResult callDifyApi(String prompt, String userId, String conversationId, String appKey);
    
    /**
     * 检查Dify API连接状态
     * 验证API密钥和连接是否正常
     * 
     * @return 连接是否正常
     */
    boolean checkApiConnection();
    
    /**
     * 获取API调用统计信息
     * 返回当前API调用次数、成功率等信息
     * 
     * @return API统计信息
     */
    String getApiStatistics();
    
    /**
     * 获取API配置信息
     * 返回当前使用的API配置，如端点、模型等
     * 
     * @return API配置信息
     */
    String getApiConfiguration();

        /**
     * Dify聊天API标准返回对象
     */
    public static class DifyChatResult {
        /** Dify返回的event类型 */
        public String event;
        /** 消息ID */
        public String messageId;
        /** 会话ID */
        public String conversationId;
        /** 模式 */
        public String mode;
        /** AI回复内容 */
        public String answer;
        /** 元数据 */
        public Object metadata;
        /** 创建时间戳 */
        public Long createdAt;
        public boolean success;
        public String error;
    }

    DifyImageResult recognizeImageByWorkflow(String imagePath, String apiKey, String userId, String variableName);

    String recognizeFileWorkflow(String filePath, String apiKey, String userId, String variableName);
} 