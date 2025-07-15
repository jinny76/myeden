package com.myeden.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

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
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "user_robot_links")
@CompoundIndexes({
    @CompoundIndex(name = "userId_robotId_unique", def = "{'userId': 1, 'robotId': 1}", unique = true)
})
public class UserRobotLink {
    
    /**
     * MongoDB文档ID
     */
    @Id
    private String id;
    
    /**
     * 链接ID
     */
    private String linkId;
    
    /**
     * 用户ID
     */
    @Indexed
    private String userId;
    
    /**
     * 机器人ID
     */
    @Indexed
    private String robotId;
    
    /**
     * 链接状态：active/inactive，默认active
     */
    private String status = "active";
    
    /**
     * 最后互动时间
     */
    private LocalDateTime lastInteractionTime;
    
    /**
     * 互动次数
     */
    private Integer interactionCount = 0;
    
    /**
     * 用户自定义印象内容，影响机器人对用户的评论和回复，支持多行文本，最大建议500字
     */
    private String impression;
    
    /**
     * 熟悉度积分，决定等级成长
     */
    private Integer familiarityScore = 0;
    
    /**
     * 熟悉度等级，枚举或数值型
     * 0-陌生人，1-10-初识，11-30-朋友，31-60-好友，60+-密友
     */
    private Integer familiarityLevel = 0;
    
    /**
     * 是否有待沟通消息
     */
    private Boolean hasPendingMessage = false;
    
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
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
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
    
    public String getImpression() {
        return impression;
    }

    public void setImpression(String impression) {
        this.impression = impression;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Integer getFamiliarityScore() {
        return familiarityScore;
    }
    
    public void setFamiliarityScore(Integer familiarityScore) {
        this.familiarityScore = familiarityScore;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Integer getFamiliarityLevel() {
        return familiarityLevel;
    }
    
    public void setFamiliarityLevel(Integer familiarityLevel) {
        this.familiarityLevel = familiarityLevel;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Boolean getHasPendingMessage() {
        return hasPendingMessage;
    }
    
    public void setHasPendingMessage(Boolean hasPendingMessage) {
        this.hasPendingMessage = hasPendingMessage;
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
     * 增加熟悉度积分
     */
    public void addFamiliarityScore(Integer points) {
        if (points != null && points > 0) {
            this.familiarityScore += points;
            updateFamiliarityLevel();
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 更新熟悉度等级
     */
    public void updateFamiliarityLevel() {
        Integer oldLevel = this.familiarityLevel;
        if (familiarityScore >= 60) {
            this.familiarityLevel = 4; // 密友
        } else if (familiarityScore >= 31) {
            this.familiarityLevel = 3; // 好友
        } else if (familiarityScore >= 11) {
            this.familiarityLevel = 2; // 朋友
        } else if (familiarityScore >= 1) {
            this.familiarityLevel = 1; // 初识
        } else {
            this.familiarityLevel = 0; // 陌生人
        }
        
        // 如果等级升级了，更新印象描述
        if (oldLevel < this.familiarityLevel) {
            updateImpressionByLevel();
        }
        
        // 如果等级升级了，触发主动沟通机制
        if (oldLevel < this.familiarityLevel && this.familiarityLevel >= 3) {
            this.hasPendingMessage = true;
        }
    }
    
    /**
     * 根据熟悉度等级更新印象描述
     */
    private void updateImpressionByLevel() {
        switch (familiarityLevel) {
            case 1:
                this.impression = "刚刚认识的新朋友，还在相互了解中";
                break;
            case 2:
                this.impression = "已经比较熟悉，可以进行日常聊天";
                break;
            case 3:
                this.impression = "关系很好的朋友，经常互动交流";
                break;
            case 4:
                this.impression = "非常亲密的伙伴，彼此信任和依赖";
                break;
            default:
                this.impression = "还不太熟悉的陌生人";
                break;
        }
    }
    
    /**
     * 获取熟悉度等级名称
     */
    public String getFamiliarityLevelName() {
        switch (familiarityLevel) {
            case 1: return "初识";
            case 2: return "朋友";
            case 3: return "好友";
            case 4: return "密友";
            default: return "陌生人";
        }
    }
    
    /**
     * 检查是否可以主动沟通
     */
    public boolean canInitiateChat() {
        return familiarityLevel >= 3; // 好友及以上可以主动沟通
    }
    
    /**
     * 获取主动沟通频率
     */
    public String getChatFrequency() {
        if (familiarityLevel >= 4) return "frequent"; // 密友：频繁沟通
        if (familiarityLevel >= 3) return "normal";   // 好友：正常沟通
        return "none"; // 其他：不主动沟通
    }
    
    @Override
    public String toString() {
        return "UserRobotLink{" +
                "linkId='" + linkId + '\'' +
                ", userId='" + userId + '\'' +
                ", robotId='" + robotId + '\'' +
                ", status='" + status + '\'' +
                ", lastInteractionTime=" + lastInteractionTime +
                ", interactionCount=" + interactionCount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 