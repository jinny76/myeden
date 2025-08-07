package com.myeden.controller;

import com.myeden.dto.coze.*;
import com.myeden.service.CozeService;
import com.myeden.service.impl.CozeServiceImpl;
import com.myeden.controller.EventResponse;
import com.myeden.entity.UserConversation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.myeden.dto.coze.CozeChatResponse;
import com.myeden.dto.coze.CozeMessageDetailResponse;
import com.myeden.dto.coze.CozeMessage;
import com.myeden.service.UserConversationService;
import java.util.Arrays;

/**
 * Coze API控制器
 */
@RestController
@RequestMapping("/api/v1/coze")
@Tag(name = "Coze AI", description = "Coze AI对话接口")
public class CozeController {

    private static final Logger logger = LoggerFactory.getLogger(CozeController.class);

    @Autowired
    private CozeService cozeService;
    
    @Autowired
    private CozeServiceImpl cozeServiceImpl;

    @Autowired
    private UserConversationService userConversationService;

    /**
     * 创建对话
     */
    @Operation(summary = "创建对话", description = "创建一个新的对话会话")
    @ApiResponse(responseCode = "200", description = "对话创建成功")
    @PostMapping("/conversations")
    public ResponseEntity<?> createConversation(@RequestBody CozeConversationRequest request) {
        try {
            logger.info("收到创建对话请求: 会话名称={}", request.getName());

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(EventResponse.error("会话名称不能为空"));
            }

            CozeConversationResponse response = cozeService.createConversation(request);

            if (response.isSuccess()) {
                logger.info("对话创建成功 - 对话ID: {}", response.getConversationId());
                return ResponseEntity.ok(EventResponse.success(response, "对话创建成功"));
            } else {
                logger.error("对话创建失败: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(EventResponse.error("对话创建失败: " + response.getMessage()));
            }

        } catch (Exception e) {
            logger.error("创建对话时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EventResponse.error("创建对话异常: " + e.getMessage()));
        }
    }

    /**
     * 发送聊天消息
     */
    @Operation(summary = "发送聊天消息", description = "向Coze机器人发送消息并等待完整回复，如无会话则自动创建")
    @ApiResponse(responseCode = "200", description = "消息发送成功并已完成处理")
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody CozeChatRequest request) {
        try {
            logger.info("收到聊天请求: 机器人ID={}, 用户ID={}, 会话ID={}, 消息数量={}", 
                       request.getBotId(), request.getUserId(), request.getConversationId(),
                       request.getMessages() != null ? request.getMessages().size() : 0);

            if (request.getMessages() == null || request.getMessages().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(EventResponse.error("消息列表不能为空"));
            }

            // 如果没有指定botId，从配置中获取默认值
            if (request.getBotId() == null || request.getBotId().trim().isEmpty()) {
                String defaultBotId = cozeService.getDefaultBotId();
                if (defaultBotId != null && !defaultBotId.trim().isEmpty()) {
                    request.setBotId(defaultBotId);
                    logger.info("未指定机器人ID，使用配置中的默认值: {}", defaultBotId);
                } else {
                    return ResponseEntity.badRequest()
                            .body(EventResponse.error("未指定机器人ID且配置中无默认值"));
                }
            }

            // 如果没有指定userId，使用默认值
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                request.setUserId("default_user");
                logger.info("未指定用户ID，使用默认值: default_user");
            }

            String conversationId = request.getConversationId();
            CozeConversationResponse createdConversation = null;

            // 0. 如果没有conversationId，先创建一个会话
            if (conversationId == null || conversationId.trim().isEmpty()) {
                logger.info("未提供会话ID，自动创建新会话");
                
                // 生成会话名称：基于用户ID和时间戳或消息内容
                CozeConversationRequest convRequest = new CozeConversationRequest();
                
                createdConversation = cozeService.createConversation(convRequest);
                
                if (!createdConversation.isSuccess()) {
                    logger.error("自动创建会话失败: {}", createdConversation.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(EventResponse.error("自动创建会话失败: " + createdConversation.getMessage()));
                }
                
                conversationId = createdConversation.getConversationId();
                request.setConversationId(conversationId);
                logger.info("自动创建会话成功 - 会话ID: {}, 会话名称: {}", conversationId);
            }

            // 1. 发起聊天请求
            CozeChatResponse chatResponse = cozeService.chat(request);

            if (!chatResponse.isSuccess()) {
                logger.error("聊天请求失败: {}", chatResponse.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(EventResponse.error("聊天请求失败: " + chatResponse.getMessage()));
            }

            logger.info("聊天请求发起成功 - 对话ID: {}, 消息ID: {}, ChatID: {}", 
                       chatResponse.getConversationId(), chatResponse.getMessageId(), chatResponse.getChatId());

            // 2. 等待对话完成处理
            if (chatResponse.getChatId() != null && chatResponse.getConversationId() != null) {
                logger.info("开始等待对话完成处理 - ChatID: {}", chatResponse.getChatId());
                
                CozeMessageDetailResponse detailResponse = waitForChatCompletion(
                        chatResponse.getChatId(), 
                        chatResponse.getConversationId(), 
                        60000  // 60秒超时
                );
                
                // 3. 构建完整响应结果
                Map<String, Object> result = new HashMap<>();
                result.put("chat", chatResponse);
                result.put("detail", detailResponse);
                
                // 如果自动创建了会话，添加会话信息
                if (createdConversation != null) {
                    result.put("conversation", createdConversation);
                    result.put("auto_created_conversation", true);
                }
                
                logger.info("对话处理完成 - ChatID: {}, 最终状态: completed", chatResponse.getChatId());
                return ResponseEntity.ok(EventResponse.success(result, "聊天消息发送成功并已完成处理"));
               
            } else {
                // 如果没有ChatID，直接返回聊天响应
                logger.warn("未获取到ChatID，直接返回聊天响应");
                return ResponseEntity.ok(EventResponse.success(chatResponse, "聊天请求成功"));
            }

        } catch (Exception e) {
            logger.error("聊天请求时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EventResponse.error("聊天请求异常: " + e.getMessage()));
        }
    }

    /**
     * 上传文件
     */
    @Operation(summary = "上传文件", description = "上传文件到Coze服务器")
    @ApiResponse(responseCode = "200", description = "文件上传成功")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @Parameter(description = "要上传的文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件用途", example = "assistants")
            @RequestParam(value = "purpose", defaultValue = "assistants") String purpose) {
        try {
            logger.info("收到文件上传请求: 文件名={}, 大小={}, 用途={}", 
                       file.getOriginalFilename(), file.getSize(), purpose);

            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(EventResponse.error("文件不能为空"));
            }

            CozeFileResponse response = cozeService.uploadFile(file, purpose);

            if (response.isSuccess()) {
                logger.info("文件上传成功 - 文件ID: {}, 文件名: {}", 
                           response.getId(), response.getFilename());
                return ResponseEntity.ok(EventResponse.success(response, "文件上传成功"));
            } else {
                logger.error("文件上传失败: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(EventResponse.error("文件上传失败: " + response.getMessage()));
            }

        } catch (Exception e) {
            logger.error("文件上传时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EventResponse.error("文件上传异常: " + e.getMessage()));
        }
    }

    /**
     * 获取对话历史
     */
    @Operation(summary = "获取对话历史", description = "获取指定对话的消息历史")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<?> getConversationHistory(
            @Parameter(description = "对话ID", required = true)
            @PathVariable String conversationId,
            @Parameter(description = "限制条数", example = "20")
            @RequestParam(value = "limit", defaultValue = "20") Integer limit,
            @Parameter(description = "偏移量", example = "0")
            @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        try {
            logger.info("获取对话历史: 对话ID={}, 限制={}, 偏移={}", conversationId, limit, offset);

            List<CozeMessage> messages = cozeService.getConversationHistory(conversationId, limit, offset);

            Map<String, Object> result = new HashMap<>();
            result.put("conversation_id", conversationId);
            result.put("messages", messages);
            result.put("total", messages.size());

            return ResponseEntity.ok(EventResponse.success(result, "获取对话历史成功"));

        } catch (Exception e) {
            logger.error("获取对话历史时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EventResponse.error("获取对话历史异常: " + e.getMessage()));
        }
    }

    /**
     * 获取对话详细信息
     */
    @Operation(summary = "获取对话详细信息", description = "查看对话的详细信息，包括状态和使用情况")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/chat/{chatId}/detail")
    public ResponseEntity<?> getChatDetail(
            @Parameter(description = "对话ID", required = true)
            @PathVariable String chatId,
            @Parameter(description = "会话ID", required = true)
            @RequestParam String conversationId) {
        try {
            logger.info("获取对话详情: 对话ID={}, 会话ID={}", chatId, conversationId);

            CozeChatDetailResponse response = cozeService.getChatDetail(chatId, conversationId);

            if (response.isSuccess()) {
                logger.info("获取对话详情成功 - 对话ID: {}, 状态: {}", 
                           chatId, response.getData() != null ? response.getData().getStatus() : "unknown");
                return ResponseEntity.ok(EventResponse.success(response, "获取对话详情成功"));
            } else {
                logger.error("获取对话详情失败: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(EventResponse.error("获取对话详情失败: " + response.getMessage()));
            }

        } catch (Exception e) {
            logger.error("获取对话详情时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EventResponse.error("获取对话详情异常: " + e.getMessage()));
        }
    }

    /**
     * 获取对话消息详情
     * 查看指定对话中除Query以外的其他消息，包括模型回复、智能体执行的中间结果等消息
     */
    @Operation(summary = "获取对话消息详情", description = "查看指定对话中除Query以外的其他消息，包括模型回复、智能体执行的中间结果等消息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/chat/{chatId}/messages")
    public ResponseEntity<?> getMessageDetails(
            @Parameter(description = "对话ID", required = true)
            @PathVariable String chatId,
            @Parameter(description = "会话ID", required = true)
            @RequestParam String conversationId) {
        try {
            logger.info("获取消息详情: 对话ID={}, 会话ID={}", chatId, conversationId);

            CozeMessageDetailResponse response = cozeService.getMessageDetails(conversationId, chatId);

            if (response.isSuccess()) {
                logger.info("获取消息详情成功 - 对话ID: {}, 消息数量: {}", 
                           chatId, response.getData() != null ? response.getData().size() : 0);
                return ResponseEntity.ok(EventResponse.success(response, "获取消息详情成功"));
            } else {
                logger.error("获取消息详情失败: {}", response.getMsg());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(EventResponse.error("获取消息详情失败: " + response.getMsg()));
            }

        } catch (Exception e) {
            logger.error("获取消息详情时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EventResponse.error("获取消息详情异常: " + e.getMessage()));
        }
    }

    /**
     * 等待对话完成的辅助方法
     */
    private CozeMessageDetailResponse waitForChatCompletion(String chatId, String conversationId, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        long pollInterval = 2000; // 2秒轮询间隔
        
        logger.info("开始等待对话完成 - chatId: {}, 超时: {}ms", chatId, timeoutMs);
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                CozeChatDetailResponse detail = cozeService.getChatDetail(chatId, conversationId);
                
                if (detail != null && detail.isSuccess() && detail.getData() != null) {
                    String status = detail.getData().getStatus();
                    logger.debug("对话状态检查 - chatId: {}, status: {}", chatId, status);
                    
                    if ("completed".equals(status) || "failed".equals(status)) {
                        logger.info("对话已结束 - chatId: {}, 最终状态: {}", chatId, status);
                        break;
                    }
                    
                    if ("requires_action".equals(status)) {
                        logger.warn("对话需要用户操作 - chatId: {}", chatId);
                        break;
                    }
                }
                
                // 等待下一次轮询
                Thread.sleep(pollInterval);
                
            } catch (InterruptedException e) {
                logger.warn("等待对话完成时被中断", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("轮询对话状态时发生异常", e);
                // 继续轮询，不中断
            }
        }
        
        logger.warn("等待对话完成超时 - chatId: {}", chatId);
        // 超时后返回最后一次的状态
        try {
            return cozeService.getMessageDetails(conversationId, chatId);
        } catch (Exception e) {
            logger.error("超时后获取对话详情失败", e);
            return CozeMessageDetailResponse.error(408, "等待对话完成超时");
        }
    }
    
    /**
     * 获取对话完整结果（包括消息详情）
     * 在对话完成后，获取完整的对话结果，包括消息详情
     */
    @Operation(summary = "获取对话完整结果", description = "在对话完成后，获取完整的对话结果，包括消息详情")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/chat/{chatId}/complete-result")
    public ResponseEntity<?> getCompleteChatResult(
            @Parameter(description = "对话ID", required = true)
            @PathVariable String chatId,
            @Parameter(description = "会话ID", required = true)
            @RequestParam String conversationId) {
        try {
            logger.info("获取对话完整结果: 对话ID={}, 会话ID={}", chatId, conversationId);

            // 1. 首先获取对话详情
            CozeChatDetailResponse detailResponse = cozeService.getChatDetail(chatId, conversationId);
            
            if (!detailResponse.isSuccess()) {
                logger.error("获取对话详情失败: {}", detailResponse.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(EventResponse.error("获取对话详情失败: " + detailResponse.getMessage()));
            }
            
            // 2. 检查对话状态
            if (detailResponse.getData() != null) {
                String status = detailResponse.getData().getStatus();
                logger.info("对话状态: {}", status);
                
                // 如果对话已完成，获取消息详情
                if ("completed".equals(status)) {
                    CozeMessageDetailResponse messageDetail = cozeService.getMessageDetails(conversationId, chatId);
                    
                    // 3. 构建完整响应结果
                    Map<String, Object> result = new HashMap<>();
                    result.put("chat_detail", detailResponse);
                    result.put("message_details", messageDetail);
                    result.put("status", status);
                    result.put("completed", true);
                    
                    logger.info("获取对话完整结果成功 - 对话ID: {}, 状态: {}, 消息数量: {}", 
                               chatId, status, 
                               messageDetail.getData() != null ? messageDetail.getData().size() : 0);
                    
                    return ResponseEntity.ok(EventResponse.success(result, "获取对话完整结果成功"));
                } else if ("failed".equals(status)) {
                    logger.error("对话处理失败 - 对话ID: {}", chatId);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(EventResponse.error("对话处理失败"));
                } else {
                    logger.warn("对话尚未完成 - 对话ID: {}, 状态: {}", chatId, status);
                    return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                            .body(EventResponse.error("对话尚未完成，当前状态: " + status));
                }
            } else {
                logger.error("对话详情数据为空");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(EventResponse.error("对话详情数据为空"));
            }

        } catch (Exception e) {
            logger.error("获取对话完整结果时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EventResponse.error("获取对话完整结果异常: " + e.getMessage()));
        }
    }

    /**
     * 获取服务状态
     */
    @Operation(summary = "获取服务状态", description = "检查Coze API服务是否可用")
    @ApiResponse(responseCode = "200", description = "状态获取成功")
    @GetMapping("/status")
    public ResponseEntity<?> getServiceStatus() {
        try {
            boolean available = cozeService.isServiceAvailable();
            boolean configValid = cozeService.validateConfiguration();

            Map<String, Object> status = new HashMap<>();
            status.put("service_available", available);
            status.put("config_valid", configValid);
            status.put("overall_status", available && configValid ? "healthy" : "unhealthy");
            status.put("timestamp", System.currentTimeMillis());

            logger.info("Coze服务状态: 可用={}, 配置有效={}", available, configValid);

            return ResponseEntity.ok(EventResponse.success(status, "服务状态获取成功"));

        } catch (Exception e) {
            logger.error("获取服务状态时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EventResponse.error("获取服务状态异常: " + e.getMessage()));
        }
    }

    /**
     * 测试Coze API调用
     * 使用与curl请求相同的参数进行测试
     */
    @PostMapping("/test")
    public ResponseEntity<CozeChatResponse> testCozeApi() {
        CozeChatResponse response = cozeServiceImpl.testChatWithConversationId();
        return ResponseEntity.ok(response);
    }

    /**
     * 测试微信集成的Coze功能
     * 模拟微信用户发送消息并获取回复
     */
    @Operation(summary = "测试微信集成的Coze功能", description = "模拟微信用户发送消息并获取回复")
    @ApiResponse(responseCode = "200", description = "测试成功")
    @PostMapping("/test-wechat-integration")
    public ResponseEntity<?> testWeChatIntegration(
            @Parameter(description = "用户ID", required = true)
            @RequestParam String userId,
            @Parameter(description = "消息内容", required = true)
            @RequestParam String message) {
        try {
            logger.info("测试微信集成的Coze功能 - 用户ID: {}, 消息: {}", userId, message);

            // 获取或创建用户的对话关系
            String botId = cozeService.getDefaultBotId();
            UserConversation userConversation = userConversationService.getOrCreateConversation(userId, botId);
            
            // 构建Coze聊天请求
            CozeChatRequest request = new CozeChatRequest();
            request.setBotId(botId);
            request.setUserId(userId);
            request.setConversationId(userConversation.getConversationId());
            request.setStream(false);
            
            // 构建消息
            CozeMessage userMsg = new CozeMessage("user", "question", message, "text");
            request.setAdditionalMessages(Arrays.asList(userMsg));
            
            // 调用Coze API生成回复
            CozeChatResponse chatResponse = cozeService.chat(request);
            
            if (chatResponse != null && chatResponse.isSuccess() && chatResponse.getChatId() != null) {
                // 等待对话完成并获取消息详情
                CozeMessageDetailResponse messageDetail = cozeService.getMessageDetails(
                    userConversation.getConversationId(), chatResponse.getChatId());
                
                // 构建响应结果
                Map<String, Object> result = new HashMap<>();
                result.put("user_id", userId);
                result.put("message", message);
                result.put("conversation_id", userConversation.getConversationId());
                result.put("chat_id", chatResponse.getChatId());
                result.put("message_details", messageDetail);
                
                // 提取回复内容
                String reply = extractLastAnswerContent(messageDetail.getData());
                result.put("reply", reply);
                
                logger.info("微信集成测试成功 - 用户ID: {}, 回复: {}", userId, reply);
                return ResponseEntity.ok(EventResponse.success(result, "微信集成测试成功"));
            } else {
                logger.error("微信集成测试失败 - 用户ID: {}", userId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(EventResponse.error("微信集成测试失败"));
            }

        } catch (Exception e) {
            logger.error("微信集成测试时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EventResponse.error("微信集成测试异常: " + e.getMessage()));
        }
    }
    
    /**
     * 从消息详情中提取最后一个answer类型的回复内容
     */
    private String extractLastAnswerContent(List<CozeMessageDetailResponse.ChatV3MessageDetail> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        
        // 查找最后一个answer类型的消息
        for (int i = messages.size() - 1; i >= 0; i--) {
            CozeMessageDetailResponse.ChatV3MessageDetail message = messages.get(i);
            if ("answer".equals(message.getType()) && "assistant".equals(message.getRole())) {
                return message.getContent();
            }
        }
        
        return null;
    }

    /**
     * 发送消息请求对象
     */
    public static class SendMessageRequest {
        private String botId;
        private String userId;
        private String conversationId;
        private String message;

        // Getters and Setters
        public String getBotId() {
            return botId;
        }

        public void setBotId(String botId) {
            this.botId = botId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getConversationId() {
            return conversationId;
        }

        public void setConversationId(String conversationId) {
            this.conversationId = conversationId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "SendMessageRequest{" +
                    "botId='" + botId + '\'' +
                    ", userId='" + userId + '\'' +
                    ", conversationId='" + conversationId + '\'' +
                    ", message='" + (message != null ? message.substring(0, Math.min(100, message.length())) : null) + '\'' +
                    '}';
        }
    }
}