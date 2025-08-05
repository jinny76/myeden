package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import java.time.LocalDateTime;

/**
 * 聊天室成员实体类（MongoDB）
 * 表示房间成员关系，区分用户和机器人成员
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
@Document(collection = "chat_room_members")
@CompoundIndex(def = "{'roomId': 1, 'memberId': 1}", unique = true)
public class ChatRoomMember {
    @Id
    private String id; // MongoDB主键
    
    /** 房间ID */
    @Indexed
    private String roomId;
    
    /** 成员类型：USER/ROBOT */
    private String memberType;
    
    /** 成员ID */
    @Indexed
    private String memberId;
    
    /** 成员昵称（冗余字段，便于查询显示） */
    private String memberNickname;
    
    /** 成员头像（冗余字段，便于查询显示） */
    private String memberAvatar;
    
    /** 加入时间 */
    private LocalDateTime joinedAt;
    
    /** 最后活跃时间 */
    private LocalDateTime lastActiveAt;
    
    /** 是否在线（对于机器人表示是否活跃） */
    private Boolean isOnline = false;
    
    /** 是否被静音 */
    private Boolean isMuted = false;
    
    /** 成员角色：owner(房主)/member(普通成员) */
    private String role = "member";

    // 构造方法
    public ChatRoomMember() {
        this.joinedAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
    }

    public ChatRoomMember(String roomId, String memberType, String memberId, String memberNickname) {
        this();
        this.roomId = roomId;
        this.memberType = memberType;
        this.memberId = memberId;
        this.memberNickname = memberNickname;
    }

    // Getter和Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getMemberType() { return memberType; }
    public void setMemberType(String memberType) { this.memberType = memberType; }
    
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    
    public String getMemberNickname() { return memberNickname; }
    public void setMemberNickname(String memberNickname) { this.memberNickname = memberNickname; }
    
    public String getMemberAvatar() { return memberAvatar; }
    public void setMemberAvatar(String memberAvatar) { this.memberAvatar = memberAvatar; }
    
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    
    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }
    
    public Boolean getIsOnline() { return isOnline; }
    public void setIsOnline(Boolean isOnline) { this.isOnline = isOnline; }
    
    public Boolean getIsMuted() { return isMuted; }
    public void setIsMuted(Boolean isMuted) { this.isMuted = isMuted; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // 业务方法
    /** 判断是否为用户成员 */
    public boolean isUser() {
        return "USER".equals(this.memberType);
    }
    
    /** 判断是否为机器人成员 */
    public boolean isRobot() {
        return "ROBOT".equals(this.memberType);
    }
    
    /** 判断是否为房主 */
    public boolean isOwner() {
        return "owner".equals(this.role);
    }
    
    /** 设置为房主 */
    public void setAsOwner() {
        this.role = "owner";
    }
    
    /** 设置为普通成员 */
    public void setAsMember() {
        this.role = "member";
    }
    
    /** 设置在线状态 */
    public void setOnline() {
        this.isOnline = true;
        this.lastActiveAt = LocalDateTime.now();
    }
    
    /** 设置离线状态 */
    public void setOffline() {
        this.isOnline = false;
    }
    
    /** 静音成员 */
    public void mute() {
        this.isMuted = true;
    }
    
    /** 取消静音 */
    public void unmute() {
        this.isMuted = false;
    }
    
    /** 更新最后活跃时间 */
    public void updateLastActiveTime() {
        this.lastActiveAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "ChatRoomMember{" +
                "id='" + id + '\'' +
                ", roomId='" + roomId + '\'' +
                ", memberType='" + memberType + '\'' +
                ", memberId='" + memberId + '\'' +
                ", memberNickname='" + memberNickname + '\'' +
                ", role='" + role + '\'' +
                ", isOnline=" + isOnline +
                ", isMuted=" + isMuted +
                ", joinedAt=" + joinedAt +
                '}';
    }
}