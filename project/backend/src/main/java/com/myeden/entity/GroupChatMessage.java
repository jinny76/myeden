package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

/**
 * 群聊消息实体类（MongoDB）
 * 存储聊天室内的群聊消息，支持文本、图片和回复功能
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
@Document(collection = "group_chat_messages")
public class GroupChatMessage {
    @Id
    private String id; // MongoDB主键
    
    /** 房间ID */
    @Indexed
    private String roomId;
    
    /** 发送者类型：USER/ROBOT */
    private String senderType;
    
    /** 发送者ID */
    @Indexed
    private String senderId;
    
    /** 发送者昵称（冗余字段，便于查询显示） */
    private String senderNickname;
    
    /** 发送者头像（冗余字段，便于查询显示） */
    private String senderAvatar;
    
    /** 消息内容 */
    private String content;
    
    /** 消息类型：text/image/system */
    private String messageType = "text";
    
    /** 图片URL（可选） */
    private String imageUrl;
    
    /** 回复消息ID（可选） */
    private String replyToId;
    
    /** 回复消息内容摘要（可选，便于显示） */
    private String replyToContent;
    
    /** 回复消息发送者昵称（可选，便于显示） */
    private String replyToSenderNickname;
    
    /** 发送时间 */
    @Indexed
    private LocalDateTime sentAt;
    
    /** 是否被删除（软删除） */
    private Boolean isDeleted = false;
    
    /** 是否为系统消息 */
    private Boolean isSystemMessage = false;
    
    /** 消息序号（用于排序） */
    private Long sequenceNumber;

    // 构造方法
    public GroupChatMessage() {
        this.sentAt = LocalDateTime.now();
    }

    public GroupChatMessage(String roomId, String senderType, String senderId, String content) {
        this();
        this.roomId = roomId;
        this.senderType = senderType;
        this.senderId = senderId;
        this.content = content;
    }

    // Getter和Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }
    
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    
    public String getSenderNickname() { return senderNickname; }
    public void setSenderNickname(String senderNickname) { this.senderNickname = senderNickname; }
    
    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getReplyToId() { return replyToId; }
    public void setReplyToId(String replyToId) { this.replyToId = replyToId; }
    
    public String getReplyToContent() { return replyToContent; }
    public void setReplyToContent(String replyToContent) { this.replyToContent = replyToContent; }
    
    public String getReplyToSenderNickname() { return replyToSenderNickname; }
    public void setReplyToSenderNickname(String replyToSenderNickname) { this.replyToSenderNickname = replyToSenderNickname; }
    
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    
    public Boolean getIsSystemMessage() { return isSystemMessage; }
    public void setIsSystemMessage(Boolean isSystemMessage) { this.isSystemMessage = isSystemMessage; }
    
    public Long getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Long sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    // 业务方法
    /** 判断是否为用户消息 */
    public boolean isUserMessage() {
        return "USER".equals(this.senderType);
    }
    
    /** 判断是否为机器人消息 */
    public boolean isRobotMessage() {
        return "ROBOT".equals(this.senderType);
    }
    
    /** 判断是否为回复消息 */
    public boolean isReplyMessage() {
        return this.replyToId != null && !this.replyToId.isEmpty();
    }
    
    /** 判断是否为图片消息 */
    public boolean isImageMessage() {
        return "image".equals(this.messageType);
    }
    
    /** 软删除消息 */
    public void softDelete() {
        this.isDeleted = true;
    }
    
    /** 恢复消息 */
    public void restore() {
        this.isDeleted = false;
    }
    
    /** 设置为系统消息 */
    public void setAsSystemMessage() {
        this.isSystemMessage = true;
        this.messageType = "system";
        this.senderType = "SYSTEM";
    }
    
    /** 创建回复消息的便捷方法 */
    public static GroupChatMessage createReply(String roomId, String senderType, String senderId, 
                                               String content, GroupChatMessage originalMessage) {
        GroupChatMessage reply = new GroupChatMessage(roomId, senderType, senderId, content);
        reply.setReplyToId(originalMessage.getId());
        reply.setReplyToContent(originalMessage.getContent().length() > 50 ? 
                               originalMessage.getContent().substring(0, 50) + "..." : 
                               originalMessage.getContent());
        reply.setReplyToSenderNickname(originalMessage.getSenderNickname());
        return reply;
    }
    
    /** 创建系统消息的便捷方法 */
    public static GroupChatMessage createSystemMessage(String roomId, String content) {
        GroupChatMessage systemMessage = new GroupChatMessage(roomId, "SYSTEM", "system", content);
        systemMessage.setAsSystemMessage();
        return systemMessage;
    }

    @Override
    public String toString() {
        return "GroupChatMessage{" +
                "id='" + id + '\'' +
                ", roomId='" + roomId + '\'' +
                ", senderType='" + senderType + '\'' +
                ", senderId='" + senderId + '\'' +
                ", senderNickname='" + senderNickname + '\'' +
                ", messageType='" + messageType + '\'' +
                ", content='" + content + '\'' +
                ", replyToId='" + replyToId + '\'' +
                ", isSystemMessage=" + isSystemMessage +
                ", sentAt=" + sentAt +
                '}';
    }
}