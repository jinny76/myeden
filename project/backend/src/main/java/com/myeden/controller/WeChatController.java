package com.myeden.controller;

import com.myeden.config.WeChatWorkProperties;
import com.myeden.dto.wechat.WeChatCallbackRequest;
import com.myeden.dto.wechat.WeChatMessage;
import com.myeden.dto.wechat.WeChatSendMessageRequest;
import com.myeden.dto.wechat.WeChatSendMessageResponse;
import com.myeden.service.WeChatCryptoService;
import com.myeden.service.WeChatMessageService;
import com.myeden.service.WeChatSendService;
import com.myeden.service.WeChatAsyncProcessService;
import com.myeden.service.WeChatMediaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 企业微信消息接收Controller
 */
@RestController
@RequestMapping("/api/v1/wechat")
@Tag(name = "企业微信", description = "企业微信消息接收和发送接口")
public class WeChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(WeChatController.class);
    
    private final WeChatWorkProperties weChatProperties;
    private final WeChatCryptoService weChatCryptoService;
    private final WeChatMessageService weChatMessageService;
    private final WeChatSendService weChatSendService;
    private final WeChatAsyncProcessService weChatAsyncProcessService;
    private final WeChatMediaService weChatMediaService;
    
    @Autowired
    public WeChatController(WeChatWorkProperties weChatProperties,
                           WeChatCryptoService weChatCryptoService,
                           WeChatMessageService weChatMessageService,
                           WeChatSendService weChatSendService,
                           WeChatAsyncProcessService weChatAsyncProcessService,
                           WeChatMediaService weChatMediaService) {
        this.weChatProperties = weChatProperties;
        this.weChatCryptoService = weChatCryptoService;
        this.weChatMessageService = weChatMessageService;
        this.weChatSendService = weChatSendService;
        this.weChatAsyncProcessService = weChatAsyncProcessService;
        this.weChatMediaService = weChatMediaService;
    }
    
    /**
     * 接收企业微信回调消息（GET请求用于验证URL）
     */
    @GetMapping("/callback")
    public ResponseEntity<String> verifyUrl(
            @RequestParam("msg_signature") String msgSignature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr) {
        
        try {
            logger.info("收到企业微信URL验证请求: msgSignature={}, timestamp={}, nonce={}", 
                       msgSignature, timestamp, nonce);
            
            if (!weChatProperties.isEnabled()) {
                logger.warn("企业微信功能未启用，拒绝URL验证请求");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("WeChat service disabled");
            }
            
            // 验证并解密echostr
            String decryptedEchostr = weChatCryptoService.verifyUrl(msgSignature, timestamp, nonce, echostr);
            
            if (decryptedEchostr != null) {
                logger.info("企业微信URL验证成功");
                return ResponseEntity.ok(decryptedEchostr);
            } else {
                logger.error("企业微信URL验证失败");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verification failed");
            }
            
        } catch (Exception e) {
            logger.error("处理企业微信URL验证时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
    
    /**
     * 接收企业微信消息（POST请求）
     */
    @PostMapping(value = "/callback", consumes = MediaType.TEXT_XML_VALUE, produces = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<String> receiveMessage(
            @RequestParam("msg_signature") String msgSignature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestBody String requestBody) {
        
        try {
            logger.info("收到企业微信消息推送: msgSignature={}, timestamp={}, nonce={}", 
                       msgSignature, timestamp, nonce);
            logger.debug("消息体: {}", requestBody);
            
            if (!weChatProperties.isEnabled()) {
                logger.warn("企业微信功能未启用，忽略消息推送");
                return ResponseEntity.ok("success");
            }
            
            // 从XML中提取加密数据
            String encryptData = extractEncryptDataFromXml(requestBody);
            if (encryptData == null || encryptData.trim().isEmpty()) {
                logger.error("无法从请求体中提取加密数据");
                return ResponseEntity.badRequest().body("Invalid request body");
            }
            
            // 解密消息
            WeChatMessage message = weChatCryptoService.decryptMessage(msgSignature, timestamp, nonce, encryptData);
            if (message == null) {
                logger.error("消息解密失败");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Message decryption failed");
            }
            
            // 异步处理消息，立即返回success
            weChatAsyncProcessService.processMessageAsync(message);
            logger.info("消息已提交异步处理，立即返回success");
            
            return ResponseEntity.ok("success");
            
        } catch (Exception e) {
            logger.error("处理企业微信消息时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
    
    /**
     * 获取企业微信配置状态
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        try {
            boolean enabled = weChatProperties.isEnabled();
            boolean configured = weChatProperties.getToken() != null && 
                               !weChatProperties.getToken().equals("your_token_here") &&
                               weChatProperties.getEncodingAesKey() != null && 
                               !weChatProperties.getEncodingAesKey().equals("your_encoding_aes_key_here");
            
            return ResponseEntity.ok(new WeChatStatus(enabled, configured));
            
        } catch (Exception e) {
            logger.error("获取企业微信状态时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to get WeChat status");
        }
    }
    
    /**
     * 从XML中提取加密数据
     */
    private String extractEncryptDataFromXml(String xml) {
        try {
            // 简单的XML解析，提取<Encrypt>标签中的内容
            String startTag = "<Encrypt><![CDATA[";
            String endTag = "]]></Encrypt>";
            
            int startIndex = xml.indexOf(startTag);
            if (startIndex == -1) {
                return null;
            }
            startIndex += startTag.length();
            
            int endIndex = xml.indexOf(endTag, startIndex);
            if (endIndex == -1) {
                return null;
            }
            
            return xml.substring(startIndex, endIndex);
            
        } catch (Exception e) {
            logger.error("提取加密数据时出现异常", e);
            return null;
        }
    }
    
    /**
     * 微信状态响应类
     */
    public static class WeChatStatus {
        private boolean enabled;
        private boolean configured;
        
        public WeChatStatus(boolean enabled, boolean configured) {
            this.enabled = enabled;
            this.configured = configured;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public boolean isConfigured() {
            return configured;
        }
        
        public void setConfigured(boolean configured) {
            this.configured = configured;
        }
    }
    
    /**
     * 发送文本消息
     */
    @PostMapping("/send/text")
    public ResponseEntity<?> sendTextMessage(@RequestBody SendTextMessageRequest request) {
        try {
            logger.info("收到发送文本消息请求: toUser={}, content={}", 
                       request.getToUser(), request.getContent());
            
            if (!weChatProperties.isEnabled()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("WeChat service disabled");
            }
            
            if (request.getToUser() == null || request.getToUser().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("toUser cannot be empty");
            }
            
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("content cannot be empty");
            }
            
            // 发送消息
            WeChatSendMessageResponse response = weChatSendService.sendTextMessage(
                request.getToUser(), request.getContent());
            
            if (response.isSuccess()) {
                logger.info("消息发送成功: msgId={}", response.getMsgId());
                return ResponseEntity.ok(response);
            } else {
                logger.error("消息发送失败: {}", response);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            logger.error("发送文本消息时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to send message: " + e.getMessage());
        }
    }
    
    /**
     * 发送消息到部门
     */
    @PostMapping("/send/party")
    public ResponseEntity<?> sendMessageToParty(@RequestBody SendMessageToPartyRequest request) {
        try {
            logger.info("收到发送部门消息请求: toParty={}, content={}", 
                       request.getToParty(), request.getContent());
            
            if (!weChatProperties.isEnabled()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("WeChat service disabled");
            }
            
            if (request.getToParty() == null || request.getToParty().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("toParty cannot be empty");
            }
            
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("content cannot be empty");
            }
            
            WeChatSendMessageResponse response = weChatSendService.sendTextMessageToParty(
                request.getToParty(), request.getContent());
            
            if (response.isSuccess()) {
                logger.info("部门消息发送成功: msgId={}", response.getMsgId());
                return ResponseEntity.ok(response);
            } else {
                logger.error("部门消息发送失败: {}", response);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            logger.error("发送部门消息时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to send message to party: " + e.getMessage());
        }
    }
    
    /**
     * 发送全员通知
     */
    @PostMapping("/send/notification")
    public ResponseEntity<?> sendNotification(@RequestBody SendNotificationRequest request) {
        try {
            logger.info("收到发送全员通知请求: content={}", request.getContent());
            
            if (!weChatProperties.isEnabled()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("WeChat service disabled");
            }
            
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("content cannot be empty");
            }
            
            WeChatSendMessageResponse response = weChatSendService.sendNotificationToAll(request.getContent());
            
            if (response.isSuccess()) {
                logger.info("全员通知发送成功: msgId={}", response.getMsgId());
                return ResponseEntity.ok(response);
            } else {
                logger.error("全员通知发送失败: {}", response);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            logger.error("发送全员通知时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to send notification: " + e.getMessage());
        }
    }
    
    /**
     * 通用发送消息接口
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody WeChatSendMessageRequest request) {
        try {
            logger.info("收到通用发送消息请求: {}", request);
            
            if (!weChatProperties.isEnabled()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("WeChat service disabled");
            }
            
            // 设置默认的agentId
            if (request.getAgentId() == null || request.getAgentId().trim().isEmpty()) {
                request.setAgentId(weChatProperties.getAgentId());
            }
            
            WeChatSendMessageResponse response = weChatSendService.sendMessage(request);
            
            if (response.isSuccess()) {
                logger.info("消息发送成功: msgId={}", response.getMsgId());
                return ResponseEntity.ok(response);
            } else {
                logger.error("消息发送失败: {}", response);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            logger.error("发送消息时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to send message: " + e.getMessage());
        }
    }
    
    /**
     * 发送文本消息请求
     */
    public static class SendTextMessageRequest {
        private String toUser;
        private String content;
        
        public String getToUser() {
            return toUser;
        }
        
        public void setToUser(String toUser) {
            this.toUser = toUser;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
    
    /**
     * 发送部门消息请求
     */
    public static class SendMessageToPartyRequest {
        private String toParty;
        private String content;
        
        public String getToParty() {
            return toParty;
        }
        
        public void setToParty(String toParty) {
            this.toParty = toParty;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
    
    /**
     * 发送通知请求
     */
    public static class SendNotificationRequest {
        private String content;
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
    
    /**
     * 发送图片消息
     */
    @PostMapping("/send/image")
    public ResponseEntity<?> sendImageMessage(@RequestBody SendImageMessageRequest request) {
        try {
            logger.info("收到发送图片消息请求: toUser={}, mediaId={}", 
                       request.getToUser(), request.getMediaId());
            
            if (!weChatProperties.isEnabled()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("WeChat service disabled");
            }
            
            if (request.getToUser() == null || request.getToUser().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("toUser cannot be empty");
            }
            
            if (request.getMediaId() == null || request.getMediaId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("mediaId cannot be empty");
            }
            
            WeChatSendMessageResponse response = weChatSendService.sendImageMessage(
                request.getToUser(), request.getMediaId());
            
            if (response.isSuccess()) {
                logger.info("图片消息发送成功: msgId={}", response.getMsgId());
                return ResponseEntity.ok(response);
            } else {
                logger.error("图片消息发送失败: {}", response);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            logger.error("发送图片消息时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to send image message: " + e.getMessage());
        }
    }
    
    /**
     * 发送Markdown消息
     */
    @PostMapping("/send/markdown")
    public ResponseEntity<?> sendMarkdownMessage(@RequestBody SendMarkdownMessageRequest request) {
        try {
            logger.info("收到发送Markdown消息请求: toUser={}, content={}", 
                       request.getToUser(), request.getContent());
            
            if (!weChatProperties.isEnabled()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("WeChat service disabled");
            }
            
            if (request.getToUser() == null || request.getToUser().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("toUser cannot be empty");
            }
            
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("content cannot be empty");
            }
            
            WeChatSendMessageResponse response = weChatSendService.sendMarkdownMessage(
                request.getToUser(), request.getContent());
            
            if (response.isSuccess()) {
                logger.info("Markdown消息发送成功: msgId={}", response.getMsgId());
                return ResponseEntity.ok(response);
            } else {
                logger.error("Markdown消息发送失败: {}", response);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            logger.error("发送Markdown消息时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to send markdown message: " + e.getMessage());
        }
    }
    
    /**
     * 发送图片消息请求
     */
    public static class SendImageMessageRequest {
        private String toUser;
        private String mediaId;
        
        public String getToUser() {
            return toUser;
        }
        
        public void setToUser(String toUser) {
            this.toUser = toUser;
        }
        
        public String getMediaId() {
            return mediaId;
        }
        
        public void setMediaId(String mediaId) {
            this.mediaId = mediaId;
        }
    }
    
    /**
     * 发送Markdown消息请求
     */
    public static class SendMarkdownMessageRequest {
        private String toUser;
        private String content;
        
        public String getToUser() {
            return toUser;
        }
        
        public void setToUser(String toUser) {
            this.toUser = toUser;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
    
    /**
     * 上传并发送图片消息
     */
    @Operation(summary = "上传并发送图片消息", description = "上传图片文件到企业微信服务器并发送给指定用户")
    @ApiResponse(responseCode = "200", description = "上传并发送成功")
    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAndSendImage(
            @Parameter(description = "要上传的图片文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "接收用户ID", required = true, example = "user123")
            @RequestParam("toUser") String toUser) {
        try {
            logger.info("收到上传图片并发送消息请求: toUser={}, filename={}", 
                       toUser, file.getOriginalFilename());
            
            if (!weChatProperties.isEnabled()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("WeChat service disabled");
            }
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("文件不能为空");
            }
            
            if (toUser == null || toUser.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("toUser cannot be empty");
            }
            
            WeChatMediaService.WeChatMediaUploadAndSendResult result = 
                weChatMediaService.uploadImageAndSend(file, toUser);
            
            if (result.isSuccess()) {
                logger.info("图片上传并发送成功: mediaId={}, msgId={}", 
                           result.getMediaId(), result.getMsgId());
                return ResponseEntity.ok(result);
            } else {
                logger.error("图片上传或发送失败: {}", result.getErrorMsg());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            
        } catch (Exception e) {
            logger.error("上传图片并发送消息时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to upload and send image: " + e.getMessage());
        }
    }
    
    /**
     * 上传并发送语音消息
     */
    @Operation(summary = "上传并发送语音消息", description = "上传语音文件到企业微信服务器并发送给指定用户")
    @ApiResponse(responseCode = "200", description = "上传并发送成功")
    @PostMapping(value = "/upload/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAndSendVoice(
            @Parameter(description = "要上传的语音文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "接收用户ID", required = true, example = "user123")
            @RequestParam("toUser") String toUser) {
        try {
            logger.info("收到上传语音并发送消息请求: toUser={}, filename={}", 
                       toUser, file.getOriginalFilename());
            
            if (!weChatProperties.isEnabled()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("WeChat service disabled");
            }
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("文件不能为空");
            }
            
            if (toUser == null || toUser.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("toUser cannot be empty");
            }
            
            WeChatMediaService.WeChatMediaUploadAndSendResult result = 
                weChatMediaService.uploadVoiceAndSend(file, toUser);
            
            if (result.isSuccess()) {
                logger.info("语音上传并发送成功: mediaId={}, msgId={}", 
                           result.getMediaId(), result.getMsgId());
                return ResponseEntity.ok(result);
            } else {
                logger.error("语音上传或发送失败: {}", result.getErrorMsg());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            
        } catch (Exception e) {
            logger.error("上传语音并发送消息时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to upload and send voice: " + e.getMessage());
        }
    }
    
    /**
     * 上传并发送视频消息
     */
    @Operation(summary = "上传并发送视频消息", description = "上传视频文件到企业微信服务器并发送给指定用户")
    @ApiResponse(responseCode = "200", description = "上传并发送成功")
    @PostMapping(value = "/upload/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAndSendVideo(
            @Parameter(description = "要上传的视频文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "接收用户ID", required = true, example = "user123")
            @RequestParam("toUser") String toUser,
            @Parameter(description = "视频标题", required = false, example = "演示视频")
            @RequestParam(value = "title", required = false) String title,
            @Parameter(description = "视频描述", required = false, example = "这是一个演示视频")
            @RequestParam(value = "description", required = false) String description) {
        try {
            logger.info("收到上传视频并发送消息请求: toUser={}, filename={}, title={}", 
                       toUser, file.getOriginalFilename(), title);
            
            if (!weChatProperties.isEnabled()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("WeChat service disabled");
            }
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("文件不能为空");
            }
            
            if (toUser == null || toUser.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("toUser cannot be empty");
            }
            
            // 设置默认标题和描述
            if (title == null || title.trim().isEmpty()) {
                title = "视频消息";
            }
            if (description == null || description.trim().isEmpty()) {
                description = "来自我的伊甸园的视频";
            }
            
            WeChatMediaService.WeChatMediaUploadAndSendResult result = 
                weChatMediaService.uploadVideoAndSend(file, toUser, title, description);
            
            if (result.isSuccess()) {
                logger.info("视频上传并发送成功: mediaId={}, msgId={}", 
                           result.getMediaId(), result.getMsgId());
                return ResponseEntity.ok(result);
            } else {
                logger.error("视频上传或发送失败: {}", result.getErrorMsg());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            
        } catch (Exception e) {
            logger.error("上传视频并发送消息时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to upload and send video: " + e.getMessage());
        }
    }
    
    /**
     * 上传并发送文件消息
     */
    @Operation(summary = "上传并发送文件消息", description = "上传文件到企业微信服务器并发送给指定用户")
    @ApiResponse(responseCode = "200", description = "上传并发送成功")
    @PostMapping(value = "/upload/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAndSendFile(
            @Parameter(description = "要上传的文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "接收用户ID", required = true, example = "user123")
            @RequestParam("toUser") String toUser) {
        try {
            logger.info("收到上传文件并发送消息请求: toUser={}, filename={}", 
                       toUser, file.getOriginalFilename());
            
            if (!weChatProperties.isEnabled()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("WeChat service disabled");
            }
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("文件不能为空");
            }
            
            if (toUser == null || toUser.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("toUser cannot be empty");
            }
            
            WeChatMediaService.WeChatMediaUploadAndSendResult result = 
                weChatMediaService.uploadFileAndSend(file, toUser);
            
            if (result.isSuccess()) {
                logger.info("文件上传并发送成功: mediaId={}, msgId={}", 
                           result.getMediaId(), result.getMsgId());
                return ResponseEntity.ok(result);
            } else {
                logger.error("文件上传或发送失败: {}", result.getErrorMsg());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            
        } catch (Exception e) {
            logger.error("上传文件并发送消息时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to upload and send file: " + e.getMessage());
        }
    }
    
    /**
     * 仅上传媒体文件（不发送消息）
     */
    @Operation(summary = "上传媒体文件", description = "仅上传媒体文件到企业微信服务器，返回mediaId供后续使用")
    @ApiResponse(responseCode = "200", description = "上传成功")
    @PostMapping(value = "/upload/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMedia(
            @Parameter(description = "要上传的媒体文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "媒体类型", required = true, example = "image", 
                      schema = @Schema(allowableValues = {"image", "voice", "video", "file"}))
            @RequestParam("type") String mediaType) {
        try {
            logger.info("收到上传媒体文件请求: filename={}, mediaType={}", 
                       file.getOriginalFilename(), mediaType);
            
            if (!weChatProperties.isEnabled()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("WeChat service disabled");
            }
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("文件不能为空");
            }
            
            if (mediaType == null || mediaType.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("mediaType cannot be empty");
            }
            
            // 验证媒体类型
            if (!Arrays.asList("image", "voice", "video", "file").contains(mediaType)) {
                return ResponseEntity.badRequest().body("不支持的媒体类型，支持: image, voice, video, file");
            }
            
            // 验证文件类型和大小
            if (!weChatMediaService.isValidFileType(file, mediaType)) {
                return ResponseEntity.badRequest().body("文件类型不符合要求");
            }
            
            if (!weChatMediaService.isValidFileSize(file, mediaType)) {
                return ResponseEntity.badRequest().body("文件大小超出限制");
            }
            
            com.myeden.dto.wechat.WeChatMediaUploadResponse result = 
                weChatMediaService.uploadMedia(file, mediaType);
            
            if (result.isSuccess()) {
                logger.info("媒体文件上传成功: mediaId={}, type={}", 
                           result.getMediaId(), result.getType());
                return ResponseEntity.ok(result);
            } else {
                logger.error("媒体文件上传失败: errCode={}, errMsg={}", 
                           result.getErrCode(), result.getErrMsg());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            
        } catch (Exception e) {
            logger.error("上传媒体文件时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to upload media: " + e.getMessage());
        }
    }
}