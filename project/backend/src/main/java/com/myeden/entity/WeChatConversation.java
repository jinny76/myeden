package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * 微信对话记录实体类
 */
@Document(collection = "wechat_conversations")
public class WeChatConversation {
    
    @Id
    private String id;
    
    /**
     * 用户ID（企业微信用户ID）
     */
    @Field("user_id")
    @Indexed
    private String userId;
    
    /**
     * 消息类型：user（用户发送）, assistant（AI回复）
     */
    @Field("message_type")
    private String messageType;
    
    /**
     * 消息内容
     */
    @Field("content")
    private String content;
    
    /**
     * 消息时间戳
     */
    @Field("message_time")
    @Indexed
    private LocalDateTime messageTime;
    
    /**
     * 微信消息ID（用于去重）
     */
    @Field("wechat_msg_id")
    @Indexed
    private String wechatMsgId;
    
    /**
     * 创建时间
     */
    @Field("created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    public WeChatConversation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public WeChatConversation(String userId, String messageType, String content) {
        this();
        this.userId = userId;
        this.messageType = messageType;
        this.content = content;
        this.messageTime = LocalDateTime.now();
    }
    
    public WeChatConversation(String userId, String messageType, String content, String wechatMsgId) {
        this(userId, messageType, content);
        this.wechatMsgId = wechatMsgId;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getMessageTime() {
        return messageTime;
    }
    
    public void setMessageTime(LocalDateTime messageTime) {
        this.messageTime = messageTime;
    }
    
    public String getWechatMsgId() {
        return wechatMsgId;
    }
    
    public void setWechatMsgId(String wechatMsgId) {
        this.wechatMsgId = wechatMsgId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "WeChatConversation{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", messageType='" + messageType + '\'' +
                ", content='" + content + '\'' +
                ", messageTime=" + messageTime +
                ", wechatMsgId='" + wechatMsgId + '\'' +
                '}';
    }
    
    /**
     * 消息类型常量
     */
    public static class MessageType {
        public static final String USER = "user";
        public static final String ASSISTANT = "assistant";
    }
}