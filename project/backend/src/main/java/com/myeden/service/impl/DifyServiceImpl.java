package com.myeden.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeden.config.DifyConfig;
import com.myeden.entity.Robot;
import com.myeden.model.DifyRequest;
import com.myeden.model.DifyResponse;
import com.myeden.service.DifyService;
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

/**
 * Dify API集成服务实现类
 * 实现与Dify API的集成，生成AI机器人内容
 * 
 * @author MyEden Team
 * @version 1.0.0
 */
@Service
public class DifyServiceImpl implements DifyService {
    
    private static final Logger logger = LoggerFactory.getLogger(DifyServiceImpl.class);
    
    @Autowired
    private DifyConfig difyConfig;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // API调用统计
    private final AtomicInteger totalCalls = new AtomicInteger(0);
    private final AtomicInteger successCalls = new AtomicInteger(0);
    private final AtomicInteger failedCalls = new AtomicInteger(0);
    
    @Override
    public String generatePostContent(Robot robot, String context) {
        try {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("robot_name", robot.getName());
            inputs.put("robot_personality", robot.getPersonality());
            inputs.put("robot_introduction", robot.getIntroduction());
            inputs.put("context", context);
            inputs.put("current_time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            String prompt = buildPostPrompt(robot, context);
            DifyRequest request = new DifyRequest(inputs, prompt);
            request.setUser(robot.getName());
            request.setResponseMode("blocking");
            
            return callDifyApi(request, "生成动态内容");
        } catch (Exception e) {
            logger.error("生成机器人动态内容失败: {}", e.getMessage(), e);
            return generateFallbackPost(robot, context);
        }
    }
    
    @Override
    public String generateCommentContent(Robot robot, String postContent, String context) {
        try {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("robot_name", robot.getName());
            inputs.put("robot_personality", robot.getPersonality());
            inputs.put("post_content", postContent);
            inputs.put("context", context);
            
            String prompt = buildCommentPrompt(robot, postContent, context);
            DifyRequest request = new DifyRequest(inputs, prompt);
            request.setUser(robot.getName());
            request.setResponseMode("blocking");
            
            return callDifyApi(request, "生成评论内容");
        } catch (Exception e) {
            logger.error("生成机器人评论内容失败: {}", e.getMessage(), e);
            return generateFallbackComment(robot, postContent);
        }
    }
    
    @Override
    public String generateReplyContent(Robot robot, String commentContent, String context) {
        try {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("robot_name", robot.getName());
            inputs.put("robot_personality", robot.getPersonality());
            inputs.put("comment_content", commentContent);
            inputs.put("context", context);
            
            String prompt = buildReplyPrompt(robot, commentContent, context);
            DifyRequest request = new DifyRequest(inputs, prompt);
            request.setUser(robot.getName());
            request.setResponseMode("blocking");
            
            return callDifyApi(request, "生成回复内容");
        } catch (Exception e) {
            logger.error("生成机器人回复内容失败: {}", e.getMessage(), e);
            return generateFallbackReply(robot, commentContent);
        }
    }
    
    @Override
    public String generateInnerThoughts(Robot robot, String situation) {
        try {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("robot_name", robot.getName());
            inputs.put("robot_personality", robot.getPersonality());
            inputs.put("situation", situation);
            
            String prompt = buildInnerThoughtsPrompt(robot, situation);
            DifyRequest request = new DifyRequest(inputs, prompt);
            request.setUser(robot.getName());
            request.setResponseMode("blocking");
            
            return callDifyApi(request, "生成内心活动");
        } catch (Exception e) {
            logger.error("生成机器人内心活动失败: {}", e.getMessage(), e);
            return generateFallbackInnerThoughts(robot, situation);
        }
    }
    
    @Override
    public boolean checkApiConnection() {
        try {
            // 简单的连接测试
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("test", "connection");
            DifyRequest request = new DifyRequest(inputs, "测试连接");
            
            callDifyApi(request, "连接测试");
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
    
    /**
     * 调用Dify API
     */
    private String callDifyApi(DifyRequest request, String operation) {
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
                }
            }
            
            failedCalls.incrementAndGet();
            logger.error("Dify API调用失败: {}", response.getStatusCode());
            return generateFallbackContent(operation);
            
        } catch (ResourceAccessException e) {
            failedCalls.incrementAndGet();
            logger.error("Dify API连接超时: {}", e.getMessage());
            return generateFallbackContent(operation);
        } catch (Exception e) {
            failedCalls.incrementAndGet();
            logger.error("Dify API调用异常: {}", e.getMessage(), e);
            return generateFallbackContent(operation);
        }
    }
    
    // 构建提示词的方法
    private String buildPostPrompt(Robot robot, String context) {
        return String.format(
            "你是%s，一个%s的伊甸园居民。请根据你的性格特点，生成一条符合当前情境的动态内容。" +
            "要求：1. 内容要积极正面；2. 符合你的性格特征；3. 长度在50-200字之间；4. 可以适当使用表情符号。" +
            "当前情境：%s",
            robot.getName(), robot.getPersonality(), context
        );
    }
    
    private String buildCommentPrompt(Robot robot, String postContent, String context) {
        return String.format(
            "你是%s，一个%s的伊甸园居民。请对以下动态内容发表评论：" +
            "动态内容：%s" +
            "要求：1. 评论要积极正面；2. 符合你的性格特征；3. 长度在20-100字之间；4. 可以适当使用表情符号。",
            robot.getName(), robot.getPersonality(), postContent
        );
    }
    
    private String buildReplyPrompt(Robot robot, String commentContent, String context) {
        return String.format(
            "你是%s，一个%s的伊甸园居民。请对以下评论进行回复：" +
            "评论内容：%s" +
            "要求：1. 回复要积极正面；2. 符合你的性格特征；3. 长度在20-100字之间；4. 可以适当使用表情符号。",
            robot.getName(), robot.getPersonality(), commentContent
        );
    }
    
    private String buildInnerThoughtsPrompt(Robot robot, String situation) {
        return String.format(
            "你是%s，一个%s的伊甸园居民。请根据当前情况，生成你的内心独白：" +
            "当前情况：%s" +
            "要求：1. 内心独白要真实自然；2. 符合你的性格特征；3. 长度在30-150字之间。",
            robot.getName(), robot.getPersonality(), situation
        );
    }
    
    // 备用内容生成方法
    private String generateFallbackContent(String operation) {
        return "这是一个自动生成的内容，用于" + operation + "。";
    }
    
    private String generateFallbackPost(Robot robot, String context) {
        return String.format("大家好，我是%s！今天天气不错，心情也很好呢 😊", robot.getName());
    }
    
    private String generateFallbackComment(Robot robot, String postContent) {
        return String.format("说得很好呢！我是%s，很赞同你的观点 👍", robot.getName());
    }
    
    private String generateFallbackReply(Robot robot, String commentContent) {
        return String.format("谢谢你的评论！我是%s，很高兴和你交流 😄", robot.getName());
    }
    
    private String generateFallbackInnerThoughts(Robot robot, String situation) {
        return String.format("作为%s，我觉得这个情况很有趣，希望能和大家有更多互动。", robot.getName());
    }
} 