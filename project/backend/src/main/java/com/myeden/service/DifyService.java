package com.myeden.service;

import com.myeden.entity.Robot;

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
    String callDifyApi(String prompt, String userId);
    
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
     * 测试API调用
     * 发送测试请求验证API功能
     * 
     * @param testPrompt 测试提示词
     * @return 测试结果
     */
    String testApiCall(String testPrompt);
} 