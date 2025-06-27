package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * 世界设定实体类
 * 
 * 功能说明：
 * - 存储虚拟世界的基本信息和配置
 * - 管理世界背景和世界观设定
 * - 记录世界中的机器人列表
 * - 支持世界状态管理
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Document(collection = "world_configs")
public class WorldConfig {
    
    @Id
    private String id;
    
    /**
     * 世界ID
     */
    @Indexed(unique = true)
    private String worldId;
    
    /**
     * 世界名称
     */
    private String name;
    
    /**
     * 世界描述
     */
    private String description;
    
    /**
     * 世界背景prompt
     */
    private String backgroundPrompt;
    
    /**
     * 世界观prompt
     */
    private String worldviewPrompt;
    
    /**
     * 机器人ID列表
     */
    private List<String> robotIds = new ArrayList<>();
    
    /**
     * 是否激活
     */
    private Boolean isActive = true;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    // 构造函数
    public WorldConfig() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public WorldConfig(String worldId, String name, String description) {
        this();
        this.worldId = worldId;
        this.name = name;
        this.description = description;
    }
    
    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getWorldId() {
        return worldId;
    }
    
    public void setWorldId(String worldId) {
        this.worldId = worldId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getBackgroundPrompt() {
        return backgroundPrompt;
    }
    
    public void setBackgroundPrompt(String backgroundPrompt) {
        this.backgroundPrompt = backgroundPrompt;
    }
    
    public String getWorldviewPrompt() {
        return worldviewPrompt;
    }
    
    public void setWorldviewPrompt(String worldviewPrompt) {
        this.worldviewPrompt = worldviewPrompt;
    }
    
    public List<String> getRobotIds() {
        return robotIds;
    }
    
    public void setRobotIds(List<String> robotIds) {
        this.robotIds = robotIds;
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
    
    /**
     * 添加机器人到世界
     */
    public void addRobot(String robotId) {
        if (!this.robotIds.contains(robotId)) {
            this.robotIds.add(robotId);
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 从世界移除机器人
     */
    public void removeRobot(String robotId) {
        if (this.robotIds.remove(robotId)) {
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 检查世界是否包含指定机器人
     */
    public boolean containsRobot(String robotId) {
        return this.robotIds.contains(robotId);
    }
    
    /**
     * 获取机器人数量
     */
    public int getRobotCount() {
        return this.robotIds.size();
    }
    
    /**
     * 激活世界
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 停用世界
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新世界信息
     */
    public void updateWorldConfig(WorldConfig worldConfig) {
        this.name = worldConfig.getName();
        this.description = worldConfig.getDescription();
        this.backgroundPrompt = worldConfig.getBackgroundPrompt();
        this.worldviewPrompt = worldConfig.getWorldviewPrompt();
        this.robotIds = worldConfig.getRobotIds();
        this.isActive = worldConfig.getIsActive();
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "WorldConfig{" +
                "id='" + id + '\'' +
                ", worldId='" + worldId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", robotCount=" + getRobotCount() +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
} 