package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import java.time.LocalDateTime;

/**
 * 专家主题记忆实体类
 * 存储用户与机器人在特定专家主题下的记忆信息
 * 支持跨会话记忆复用和增量更新
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Document(collection = "expert_memories")
@CompoundIndexes({
    @CompoundIndex(name = "user_robot_theme_field_idx", 
                  def = "{'userId': 1, 'robotId': 1, 'themeId': 1, 'fieldName': 1}", 
                  unique = true)
})
public class ExpertMemory {
    
    @Id
    private String id;
    
    /** 用户ID */
    @Indexed
    private String userId;
    
    /** 机器人ID */
    @Indexed
    private String robotId;
    
    /** 专家主题ID */
    @Indexed
    private String themeId;
    
    /** 记忆类型：basic_info(基础信息), session_summary(会话摘要), key_event(关键事件) */
    private String memoryType;
    
    /** 信息字段名称(如"基本情况"、"困惑点"、"咨询历史") */
    private String fieldName;
    
    /** 记忆内容 */
    private String content;
    
    /** 内容摘要(用于快速检索) */
    private String summary;
    
    /** 优先级(1-5，5最高) */
    private Integer priority;
    
    /** 是否有效 */
    private Boolean isActive;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
    
    // 构造方法
    public ExpertMemory() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
        this.priority = 3; // 默认优先级
    }
    
    public ExpertMemory(String userId, String robotId, String themeId, String memoryType, 
                       String fieldName, String content) {
        this();
        this.userId = userId;
        this.robotId = robotId;
        this.themeId = themeId;
        this.memoryType = memoryType;
        this.fieldName = fieldName;
        this.content = content;
    }
    
    // Getter和Setter方法
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
    
    public String getRobotId() {
        return robotId;
    }
    
    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }
    
    public String getThemeId() {
        return themeId;
    }
    
    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }
    
    public String getMemoryType() {
        return memoryType;
    }
    
    public void setMemoryType(String memoryType) {
        this.memoryType = memoryType;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
    
    // 业务方法
    
    /**
     * 更新记忆内容
     */
    public void updateContent(String newContent) {
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 设置为无效状态
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 激活记忆
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 检查是否为基础信息记忆
     */
    public boolean isBasicInfo() {
        return "basic_info".equals(this.memoryType);
    }
    
    /**
     * 检查是否为会话摘要记忆
     */
    public boolean isSessionSummary() {
        return "session_summary".equals(this.memoryType);
    }
    
    /**
     * 检查是否为关键事件记忆
     */
    public boolean isKeyEvent() {
        return "key_event".equals(this.memoryType);
    }
    
    @Override
    public String toString() {
        return "ExpertMemory{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", robotId='" + robotId + '\'' +
                ", themeId='" + themeId + '\'' +
                ", memoryType='" + memoryType + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", content='" + (content != null && content.length() > 50 ? 
                                content.substring(0, 50) + "..." : content) + '\'' +
                ", priority=" + priority +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}