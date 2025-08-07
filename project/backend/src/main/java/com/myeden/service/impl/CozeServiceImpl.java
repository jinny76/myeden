package com.myeden.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeden.config.CozeProperties;
import com.myeden.dto.coze.*;
import com.myeden.service.CozeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Coze API服务实现类
 */
@Service
public class CozeServiceImpl implements CozeService {

    private static final Logger logger = LoggerFactory.getLogger(CozeServiceImpl.class);
    
    @Autowired
    private CozeProperties cozeProperties;
    
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        // 初始化RestTemplate
        RestTemplateBuilder builder = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(cozeProperties.getTimeout().getConnect()))
                .setReadTimeout(Duration.ofMillis(cozeProperties.getTimeout().getRead()));
        
        this.restTemplate = builder.build();
        this.objectMapper = new ObjectMapper();
        
        logger.info("Coze服务初始化完成 - 启用状态: {}, 基础URL: {}", 
                   cozeProperties.isEnabled(), cozeProperties.getBaseUrl());
    }

    @Override
    public CozeConversationResponse createConversation(CozeConversationRequest request) {
        if (!cozeProperties.isEnabled()) {
            logger.warn("Coze服务未启用");
            return CozeConversationResponse.error(400, "Coze服务未启用");
        }

        try {
            String url = String.format("%s/%s/conversation/create",
                                     cozeProperties.getBaseUrl(), "v1");
            
            // 设置HTTP Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cozeProperties.getKey());
            
            HttpEntity<CozeConversationRequest> httpEntity = new HttpEntity<>(request, headers);
            logger.debug("创建对话请求: {}", request);
            
            ResponseEntity<CozeConversationResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, httpEntity, CozeConversationResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("创建对话成功 - 对话ID: {}", response.getBody().getConversationId());
                return response.getBody();
            } else {
                logger.error("创建对话失败 - HTTP状态码: {}", response.getStatusCode());
                return CozeConversationResponse.error(response.getStatusCode().value(), "创建对话失败");
            }
            
        } catch (Exception e) {
            logger.error("创建对话时发生异常", e);
            return CozeConversationResponse.error(500, "创建对话异常: " + e.getMessage());
        }
    }

    @Override
    public CozeChatResponse chat(CozeChatRequest request) {
        if (!cozeProperties.isEnabled()) {
            logger.warn("Coze服务未启用");
            return CozeChatResponse.error(400, "Coze服务未启用");
        }

        try {
            // 构建URL，包含conversation_id查询参数
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(String.format("%s/%s/chat", cozeProperties.getBaseUrl(), "v3"));
            
            if (StringUtils.hasText(request.getConversationId())) {
                urlBuilder.append("?conversation_id=").append(request.getConversationId());
            }
            
            String url = urlBuilder.toString();
            
            // 设置默认值
            if (!StringUtils.hasText(request.getBotId())) {
                request.setBotId(cozeProperties.getDefaults().getBotId());
            }
            if (!StringUtils.hasText(request.getUserId())) {
                request.setUserId(cozeProperties.getDefaults().getUserId());
            }
            
            // 设置HTTP Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cozeProperties.getKey());
            
            HttpEntity<CozeChatRequest> httpEntity = new HttpEntity<>(request, headers);
            logger.debug("聊天请求: URL={}, Request={}", url, request);
            
            ResponseEntity<CozeChatResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, httpEntity, CozeChatResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                CozeChatResponse chatResponse = response.getBody();
                logger.info("聊天请求成功 - 对话ID: {}, 消息ID: {}", 
                           chatResponse.getConversationId(), chatResponse.getMessageId());
                return chatResponse;
            } else {
                logger.error("聊天请求失败 - HTTP状态码: {}", response.getStatusCode());
                return CozeChatResponse.error(response.getStatusCode().value(), "聊天请求失败");
            }
            
        } catch (Exception e) {
            logger.error("聊天请求时发生异常", e);
            return CozeChatResponse.error(500, "聊天请求异常: " + e.getMessage());
        }
    }

    @Override
    public CozeChatResponse sendMessage(String botId, String userId, String message) {
        return sendMessage(botId, userId, null, message);
    }

    @Override
    public CozeChatResponse sendMessage(String botId, String userId, String conversationId, String message) {
        if (!StringUtils.hasText(message)) {
            return CozeChatResponse.error(400, "消息内容不能为空");
        }
        
        // 构建请求
        CozeChatRequest request = new CozeChatRequest();
        request.setBotId(StringUtils.hasText(botId) ? botId : cozeProperties.getDefaults().getBotId());
        request.setUserId(StringUtils.hasText(userId) ? userId : cozeProperties.getDefaults().getUserId());
        request.setConversationId(conversationId);
        request.setStream(false);
        request.setAutoSaveHistory(cozeProperties.getDefaults().isAutoSaveHistory());
        
        // 构建消息 - 使用additional_messages字段
        CozeMessage userMessage = new CozeMessage("user", "question", message, "text");
        request.setAdditionalMessages(Arrays.asList(userMessage));
        
        return chat(request);
    }

    @Override
    public CozeFileResponse uploadFile(MultipartFile file, String purpose) {
        if (!cozeProperties.isEnabled()) {
            logger.warn("Coze服务未启用");
            return CozeFileResponse.error(400, "Coze服务未启用");
        }

        try {
            // 验证文件
            if (file.isEmpty()) {
                return CozeFileResponse.error(400, "文件不能为空");
            }
            
            if (file.getSize() > cozeProperties.getFile().getMaxSize()) {
                return CozeFileResponse.error(400, "文件大小超出限制");
            }
            
            String fileExtension = getFileExtension(file.getOriginalFilename());
            if (!cozeProperties.getFile().getSupportedTypes().contains(fileExtension.toLowerCase())) {
                return CozeFileResponse.error(400, "不支持的文件类型: " + fileExtension);
            }
            
            String url = String.format("%s/%s/files", 
                                     cozeProperties.getBaseUrl(), "v1");
            
            // 构建multipart请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(cozeProperties.getKey());
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.add("purpose", purpose != null ? purpose : "assistants");
            
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);
            logger.debug("文件上传请求: 文件名={}, 大小={}", file.getOriginalFilename(), file.getSize());
            
            ResponseEntity<CozeFileResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, httpEntity, CozeFileResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                CozeFileResponse fileResponse = response.getBody();
                logger.info("文件上传成功 - 文件ID: {}, 文件名: {}", 
                           fileResponse.getId(), fileResponse.getFilename());
                return fileResponse;
            } else {
                logger.error("文件上传失败 - HTTP状态码: {}", response.getStatusCode());
                return CozeFileResponse.error(response.getStatusCode().value(), "文件上传失败");
            }
            
        } catch (Exception e) {
            logger.error("文件上传时发生异常", e);
            return CozeFileResponse.error(500, "文件上传异常: " + e.getMessage());
        }
    }

    @Override
    public List<CozeMessage> getConversationHistory(String conversationId, Integer limit, Integer offset) {
        // TODO: 实现获取对话历史的逻辑
        logger.warn("获取对话历史功能暂未实现");
        return Arrays.asList();
    }

    @Override
    public boolean isServiceAvailable() {
        try {
            if (!cozeProperties.isEnabled()) {
                return false;
            }
            
            // 简单的健康检查 - 尝试创建一个测试对话
            CozeConversationRequest testRequest = new CozeConversationRequest("Health Check");
            CozeConversationResponse response = createConversation(testRequest);
            
            return response.isSuccess();
            
        } catch (Exception e) {
            logger.debug("服务可用性检查失败", e);
            return false;
        }
    }

    @Override
    public boolean validateConfiguration() {
        try {
            boolean valid = StringUtils.hasText(cozeProperties.getKey()) && 
                           StringUtils.hasText(cozeProperties.getBaseUrl()) &&
                           StringUtils.hasText(cozeProperties.getDefaults().getBotId());
            
            if (!valid) {
                logger.warn("Coze配置验证失败 - 缺少必需的配置项");
            }
            
            return valid;
            
        } catch (Exception e) {
            logger.error("配置验证时发生异常", e);
            return false;
        }
    }

    @Override
    public CozeChatDetailResponse getChatDetail(String chatId, String conversationId) {
        if (!cozeProperties.isEnabled()) {
            logger.warn("Coze服务未启用");
            return CozeChatDetailResponse.error(400, "Coze服务未启用");
        }

        try {
            String url = String.format("%s/v3/chat/retrieve?conversation_id=%s&chat_id=%s", 
                                     cozeProperties.getBaseUrl(), conversationId, chatId);
            
            // 设置HTTP Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(cozeProperties.getKey());
            
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
            logger.debug("获取对话详情请求: conversationId={}, chatId={}", conversationId, chatId);
            
            ResponseEntity<CozeChatDetailResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, httpEntity, CozeChatDetailResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                CozeChatDetailResponse chatDetail = response.getBody();
                logger.info("获取对话详情成功 - 对话ID: {}, 状态: {}", 
                           chatId, chatDetail.getData() != null ? chatDetail.getData().getStatus() : "unknown");
                return chatDetail;
            } else {
                logger.error("获取对话详情失败 - HTTP状态码: {}", response.getStatusCode());
                return CozeChatDetailResponse.error(response.getStatusCode().value(), "获取对话详情失败");
            }
            
        } catch (Exception e) {
            logger.error("获取对话详情时发生异常", e);
            return CozeChatDetailResponse.error(500, "获取对话详情异常: " + e.getMessage());
        }
    }
    
    @Override
    public String getDefaultBotId() {
        try {
            return cozeProperties.getDefaults() != null ? cozeProperties.getDefaults().getBotId() : null;
        } catch (Exception e) {
            logger.error("获取默认机器人ID时发生异常", e);
            return null;
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
    
    @Override
    public CozeMessageDetailResponse getMessageDetails(String conversationId, String chatId) {
        if (!cozeProperties.isEnabled()) {
            logger.warn("Coze服务未启用");
            return CozeMessageDetailResponse.error(400, "Coze服务未启用");
        }

        try {
            // 构建URL，包含必需的查询参数
            String url = String.format("%s/v3/chat/message/list?conversation_id=%s&chat_id=%s", 
                                     cozeProperties.getBaseUrl(), conversationId, chatId);
            
            // 设置HTTP Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(cozeProperties.getKey());
            
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
            logger.debug("获取消息详情请求: conversationId={}, chatId={}", conversationId, chatId);
            
            ResponseEntity<CozeMessageDetailResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, httpEntity, CozeMessageDetailResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                CozeMessageDetailResponse messageDetail = response.getBody();
                logger.info("获取消息详情成功 - 会话ID: {}, 对话ID: {}, 消息数量: {}", 
                           conversationId, chatId, 
                           messageDetail.getData() != null ? messageDetail.getData().size() : 0);
                return messageDetail;
            } else {
                logger.error("获取消息详情失败 - HTTP状态码: {}", response.getStatusCode());
                return CozeMessageDetailResponse.error(response.getStatusCode().value(), "获取消息详情失败");
            }
            
        } catch (Exception e) {
            logger.error("获取消息详情时发生异常", e);
            return CozeMessageDetailResponse.error(500, "获取消息详情异常: " + e.getMessage());
        }
    }

    /**
     * 测试方法：验证Coze API调用是否正常工作
     * 使用与curl请求相同的参数进行测试
     */
    public CozeChatResponse testChatWithConversationId() {
        if (!cozeProperties.isEnabled()) {
            logger.warn("Coze服务未启用");
            return CozeChatResponse.error(400, "Coze服务未启用");
        }

        try {
            // 构建测试请求，模拟curl请求
            CozeChatRequest request = new CozeChatRequest();
            request.setBotId("7525775173111889955");
            request.setUserId("Jinni");
            request.setConversationId("7535749878162407443");
            request.setStream(false);
            
            // 构建消息，与curl请求保持一致
            CozeMessage userMessage = new CozeMessage("user", "question", "明天深圳天气如何", "text");
            request.setAdditionalMessages(Arrays.asList(userMessage));
            
            logger.info("开始测试Coze API调用...");
            return chat(request);
            
        } catch (Exception e) {
            logger.error("测试Coze API调用时发生异常", e);
            return CozeChatResponse.error(500, "测试调用异常: " + e.getMessage());
        }
    }
}