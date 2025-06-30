package com.myeden.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeden.config.DifyConfig;
import com.myeden.config.RobotConfig;
import com.myeden.entity.Robot;
import com.myeden.model.DifyRequest;
import com.myeden.model.DifyResponse;
import com.myeden.service.CommentService;
import com.myeden.service.DifyService;
import com.myeden.service.PostService;
import com.myeden.service.PromptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;
import java.util.List;

/**
 * Dify API集成服务实现类
 * 专门负责与Dify API的通讯，包括调用API、处理响应、管理连接等
 * 提示词构建和内容处理逻辑已抽提到PromptService中
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
public class DifyServiceImpl implements DifyService {
    
    private static final Logger logger = LoggerFactory.getLogger(DifyServiceImpl.class);
    
    @Autowired
    private DifyConfig difyConfig;
    
    @Autowired
    private RestTemplate restTemplate;
    
    // API调用统计
    private final AtomicInteger totalCalls = new AtomicInteger(0);
    private final AtomicInteger successCalls = new AtomicInteger(0);
    private final AtomicInteger failedCalls = new AtomicInteger(0);
    
    @Override
    public String callDifyApi(String prompt, String userId) {
        try {
            DifyRequest request = new DifyRequest(new HashMap<>(), prompt);
            request.setUser(userId);
            request.setResponseMode("blocking");
            
            return callDifyApiInternal(request, "API调用");
        } catch (Exception e) {
            logger.error("调用Dify API失败: {}", e.getMessage(), e);
            return generateFallbackContent("API调用");
        }
    }
    
    @Override
    public boolean checkApiConnection() {
        try {
            // 简单的连接测试
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("test", "connection");
            DifyRequest request = new DifyRequest(inputs, "测试连接");
            
            callDifyApiInternal(request, "连接测试");
            return true;
        } catch (Exception e) {
            logger.error("Dify API连接测试失败: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getApiStatistics() {
        int total = totalCalls.get();
        int success = successCalls.get();
        int failed = failedCalls.get();
        double successRate = total > 0 ? (double) success / total * 100 : 0;
        
        return String.format("API调用统计 - 总数: %d, 成功: %d, 失败: %d, 成功率: %.2f%%", 
                           total, success, failed, successRate);
    }
    
    @Override
    public String getApiConfiguration() {
        return String.format("Dify API配置 - URL: %s, 启用状态: %s", 
                           difyConfig.getUrl(), 
                           difyConfig.isEnabled() ? "启用" : "禁用");
    }

    /**
     * 内部调用Dify API的方法
     */
    private String callDifyApiInternal(DifyRequest request, String operation) {
        if (!difyConfig.isEnabled()) {
            logger.warn("Dify API已禁用，使用备用内容生成");
            return generateFallbackContent(operation);
        }

        totalCalls.incrementAndGet();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + difyConfig.getKey());

            HttpEntity<DifyRequest> entity = new HttpEntity<>(request, headers);
            String url = difyConfig.getUrl() + "/chat-messages";

            ResponseEntity<DifyResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, DifyResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                DifyResponse difyResponse = response.getBody();
                if ("message".equals(difyResponse.getEvent()) && difyResponse.getAnswer() != null) {
                    successCalls.incrementAndGet();
                    return difyResponse.getAnswer();
                } else {
                    failedCalls.incrementAndGet();
                    logger.error("Dify API返回异常响应: {}", difyResponse);
                    return generateFallbackContent(operation);
                }
            } else {
                failedCalls.incrementAndGet();
                logger.error("Dify API调用失败，状态码: {}", response.getStatusCode());
                return generateFallbackContent(operation);
            }
        } catch (ResourceAccessException e) {
            failedCalls.incrementAndGet();
            logger.error("Dify API连接失败: {}", e.getMessage());
            return generateFallbackContent(operation);
        } catch (Exception e) {
            failedCalls.incrementAndGet();
            logger.error("Dify API调用异常: {}", e.getMessage(), e);
            return generateFallbackContent(operation);
        }
    }

    // 备用内容生成方法
    private String generateFallbackContent(String operation) {
        return String.format("我是谁, 我失忆了, 头好疼", operation);
    }
} 