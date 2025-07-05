package com.myeden.entity;

import java.util.Date;

/**
 * 聊天消息实体类
 * 用于封装用户与AI之间的消息内容
 * conversationId：用于未来扩展多会话场景，便于会话分组和检索
 */
public class ChatMessage {
    private Long id;              // 消息主键
    private String sessionId;     // 会话ID
    private String conversationId; // 会话分组ID（备用）
    private String senderId;      // 发送者ID
    private String senderType;    // 发送者类型（user/ai）
    private String receiverId;    // 接收者ID
    private String receiverType;  // 接收者类型（user/ai）
    private String content;       // 消息内容
    private String msgType;       // 消息类型（text等）
    private Date createdAt;       // 发送时间
    private Boolean isRead;       // 是否已读

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public String getConversationId() {
        return conversationId;
    }
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    public String getSenderId() {
        return senderId;
    }
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    public String getSenderType() {
        return senderType;
    }
    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }
    public String getReceiverId() {
        return receiverId;
    }
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
    public String getReceiverType() {
        return receiverType;
    }
    public void setReceiverType(String receiverType) {
        this.receiverType = receiverType;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getMsgType() {
        return msgType;
    }
    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public Boolean getIsRead() {
        return isRead;
    }
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
} 