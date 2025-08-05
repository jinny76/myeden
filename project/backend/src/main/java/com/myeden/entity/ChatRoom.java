package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

/**
 * 聊天室实体类（MongoDB）
 * 每个用户拥有一个聊天室，聊天室ID就是用户ID
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
@Document(collection = "chat_rooms")
public class ChatRoom {
    @Id
    private String id; // MongoDB主键
    
    /** 房间ID（使用user_id作为room_id，确保唯一性） */
    @Indexed(unique = true)
    private String roomId;
    
    /** 房间名称，默认是[user.nickname]的聊天室，可修改 */
    private String roomName;
    
    /** 房间状态：active(活跃)/inactive(非活跃)，活跃就高频聊天，反之则低频聊天 */
    private String status = "active";
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 最后活跃时间 */
    private LocalDateTime lastActiveAt;
    
    /** 最后一条消息时间 */
    private LocalDateTime lastMessageAt;
    
    /** 消息总数 */
    private Integer messageCount = 0;
    
    /** 当前活跃成员数量 */
    private Integer activeMemberCount = 0;

    // 构造方法
    public ChatRoom() {
        this.createdAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
    }

    public ChatRoom(String roomId, String roomName) {
        this();
        this.roomId = roomId;
        this.roomName = roomName;
    }

    // Getter和Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }
    
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    
    public Integer getMessageCount() { return messageCount; }
    public void setMessageCount(Integer messageCount) { this.messageCount = messageCount; }
    
    public Integer getActiveMemberCount() { return activeMemberCount; }
    public void setActiveMemberCount(Integer activeMemberCount) { this.activeMemberCount = activeMemberCount; }

    // 业务方法
    /** 判断是否为活跃状态 */
    public boolean isActive() {
        return "active".equals(this.status);
    }
    
    /** 设置为活跃状态 */
    public void setActive() {
        this.status = "active";
        this.lastActiveAt = LocalDateTime.now();
    }
    
    /** 设置为非活跃状态 */
    public void setInactive() {
        this.status = "inactive";
    }
    
    /** 更新最后活跃时间 */
    public void updateLastActiveTime() {
        this.lastActiveAt = LocalDateTime.now();
    }
    
    /** 更新最后消息时间并增加消息计数 */
    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
        this.messageCount++;
    }

    @Override
    public String toString() {
        return "ChatRoom{" +
                "id='" + id + '\'' +
                ", roomId='" + roomId + '\'' +
                ", roomName='" + roomName + '\'' +
                ", status='" + status + '\'' +
                ", messageCount=" + messageCount +
                ", activeMemberCount=" + activeMemberCount +
                ", createdAt=" + createdAt +
                ", lastActiveAt=" + lastActiveAt +
                '}';
    }
}