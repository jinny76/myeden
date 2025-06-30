package com.myeden.entity;

import java.time.LocalDateTime;

/**
 * 用户机器人链接实体类
 * 
 * 功能说明：
 * - 管理用户与机器人的专属链接关系
 * - 记录链接的创建时间、状态等信息
 * - 支持链接的启用/禁用状态管理
 * - 提供链接强度评估功能
 * 
 * @author MyEden Team
 * @version 1.0.1
 * @since 2025-01-27
 */
public class UserRobotLink {
    
    /**
     * 链接ID
     */
    private String linkId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 机器人ID
     */
    private String robotId;
    
    /**
     * 链接状态：active/inactive，默认active
     */
    private String status = "active";
    
    /**
     * 链接强度：1-10，默认5
     */
    private Integer strength = 5;
    
    /**
     * 最后互动时间
     */
    private LocalDateTime lastInteractionTime;
    
    /**
     * 互动次数
     */
    private Integer interactionCount = 0;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    // 构造函数
    public UserRobotLink() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastInteractionTime = LocalDateTime.now();
    }
    
    public UserRobotLink(String userId, String robotId) {
        this();
        this.userId = userId;
        this.robotId = robotId;
    }
    
    // Getter和Setter方法
    public String getLinkId() {
        return linkId;
    }
    
    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getRobotId() {
        return robotId;
    }
    
    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Integer getStrength() {
        return strength;
    }
    
    public void setStrength(Integer strength) {
        this.strength = strength;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getLastInteractionTime() {
        return lastInteractionTime;
    }
    
    public void setLastInteractionTime(LocalDateTime lastInteractionTime) {
        this.lastInteractionTime = lastInteractionTime;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Integer getInteractionCount() {
        return interactionCount;
    }
    
    public void setInteractionCount(Integer interactionCount) {
        this.interactionCount = interactionCount;
        this.updatedAt = LocalDateTime.now();
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
    
    /**
     * 检查链接是否激活
     */
    public boolean isActive() {
        return "active".equals(status);
    }
    
    /**
     * 激活链接
     */
    public void activate() {
        this.status = "active";
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 停用链接
     */
    public void deactivate() {
        this.status = "inactive";
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 增加互动次数
     */
    public void incrementInteraction() {
        this.interactionCount++;
        this.lastInteractionTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新链接强度
     */
    public void updateStrength(Integer newStrength) {
        if (newStrength != null && newStrength >= 1 && newStrength <= 10) {
            this.strength = newStrength;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 获取链接强度等级
     */
    public String getStrengthLevel() {
        if (strength >= 8) return "strong";
        if (strength >= 5) return "medium";
        return "weak";
    }
    
    @Override
    public String toString() {
        return "UserRobotLink{" +
                "linkId='" + linkId + '\'' +
                ", userId='" + userId + '\'' +
                ", robotId='" + robotId + '\'' +
                ", status='" + status + '\'' +
                ", strength=" + strength +
                ", lastInteractionTime=" + lastInteractionTime +
                ", interactionCount=" + interactionCount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 