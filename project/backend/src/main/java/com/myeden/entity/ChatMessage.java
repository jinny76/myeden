package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.myeden.service.AIChatService;

import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

/**
 * 聊天消息实体类（MongoDB）
 * 存储用户与AI/机器人之间的消息内容，支持多会话、分组、已读等功能
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Document(collection = "chat_messages")
public class ChatMessage {
    @Id
    private String id; // MongoDB主键

    /** 会话ID（如一对一会话唯一标识） */
    @Indexed
    private String sessionId;

    /** conversationId：多轮对话分组ID */
    @Indexed
    private String conversationId;

    /** 发送者ID */
    @Indexed
    private String senderId;

    /** 发送者类型（user/robot） */
    private String senderType;

    /** 接收者ID */
    @Indexed
    private String receiverId;

    /** 接收者类型（user/robot） */
    private String receiverType;

    /** 消息内容 */
    private String content;

    /** 消息类型（text/image等） */
    private String msgType = "text";

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 是否已读 */
    private Boolean isRead = false;

    /** 是否删除（软删除） */
    private Boolean isDeleted = false;

    /** 图片base64 */
    private String imageBase64;

    /** 语音base64 */
    private String audioBase64;

    /** 语音识别结果 */
    private AIChatService.ASRRawTextInfo asrResult;

    /** 是否为机器人主动触发的消息 */
    private Boolean isProactiveMessage = false;

    // 构造方法
    public ChatMessage() {
        this.createdAt = LocalDateTime.now();
    }

    public ChatMessage(String sessionId, String conversationId, String senderId, String senderType, String receiverId, String receiverType, String content) {
        this();
        this.sessionId = sessionId;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderType = senderType;
        this.receiverId = receiverId;
        this.receiverType = receiverType;
        this.content = content;
        this.imageBase64 = imageBase64;
        this.audioBase64 = audioBase64;
    }

    // Getter和Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    public String getReceiverType() { return receiverType; }
    public void setReceiverType(String receiverType) { this.receiverType = receiverType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMsgType() { return msgType; }
    public void setMsgType(String msgType) { this.msgType = msgType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public String getAudioBase64() { return audioBase64; }
    public void setAudioBase64(String audioBase64) { this.audioBase64 = audioBase64; }
    public AIChatService.ASRRawTextInfo getAsrResult() { return asrResult; }
    public void setAsrResult(AIChatService.ASRRawTextInfo asrResult) { this.asrResult = asrResult; }
    public Boolean getIsProactiveMessage() { return isProactiveMessage; }
    public void setIsProactiveMessage(Boolean isProactiveMessage) { this.isProactiveMessage = isProactiveMessage; }
    
    // 业务方法
    /** 标记为已读 */
    public void markAsRead() {
        this.isRead = true;
    }
    /** 软删除消息 */
    public void softDelete() {
        this.isDeleted = true;
    }
    /** 恢复消息 */
    public void restore() {
        this.isDeleted = false;
    }
    @Override
    public String toString() {
        return "ChatMessage{" +
                "id='" + id + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", senderType='" + senderType + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", receiverType='" + receiverType + '\'' +
                ", content='" + content + '\'' +
                ", msgType='" + msgType + '\'' +
                ", isRead=" + isRead +
                ", isDeleted=" + isDeleted +
                ", createdAt=" + createdAt +
                '}';
    }
} 