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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 企业微信消息接收Controller
 */
@RestController
@RequestMapping("/api/v1/wechat")
public class WeChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(WeChatController.class);
    
    private final WeChatWorkProperties weChatProperties;
    private final WeChatCryptoService weChatCryptoService;
    private final WeChatMessageService weChatMessageService;
    private final WeChatSendService weChatSendService;
    private final WeChatAsyncProcessService weChatAsyncProcessService;
    
    @Autowired
    public WeChatController(WeChatWorkProperties weChatProperties,
                           WeChatCryptoService weChatCryptoService,
                           WeChatMessageService weChatMessageService,
                           WeChatSendService weChatSendService,
                           WeChatAsyncProcessService weChatAsyncProcessService) {
        this.weChatProperties = weChatProperties;
        this.weChatCryptoService = weChatCryptoService;
        this.weChatMessageService = weChatMessageService;
        this.weChatSendService = weChatSendService;
        this.weChatAsyncProcessService = weChatAsyncProcessService;
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
}