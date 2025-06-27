package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * 机器人实体类
 * 
 * 功能说明：
 * - 存储AI机器人的基本信息和配置
 * - 管理机器人的行为模式和概率
 * - 记录机器人的活跃状态
 * - 支持机器人个性化设定
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Document(collection = "robots")
public class Robot {
    
    @Id
    private String id;
    
    /**
     * 机器人ID，唯一
     */
    @Indexed(unique = true)
    private String robotId;
    
    /**
     * 机器人名称，唯一
     */
    @Indexed(unique = true)
    private String name;
    
    /**
     * 机器人头像
     */
    private String avatar;
    
    /**
     * 机器人性别
     */
    private String gender;
    
    /**
     * 一句话简介
     */
    private String introduction;
    
    /**
     * 性格设定
     */
    private String personality;
    
    /**
     * 职业
     */
    private String profession;
    
    /**
     * MBTI
     */
    private String mbti;
    
    /**
     * 回复速度（1-10）
     */
    private Integer replySpeed;
    
    /**
     * 回复频度（1-10）
     */
    private Integer replyFrequency;
    
    /**
     * 分享频度（1-10）
     */
    private Integer shareFrequency;
    
    /**
     * 活跃时间段
     */
    private List<ActiveTimeRange> activeTimeRanges = new ArrayList<>();
    
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
    
    // 内部类：活跃时间段
    public static class ActiveTimeRange {
        private String startTime; // 开始时间 HH:mm
        private String endTime;   // 结束时间 HH:mm
        
        public ActiveTimeRange() {}
        
        public ActiveTimeRange(String startTime, String endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        public String getStartTime() {
            return startTime;
        }
        
        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }
        
        public String getEndTime() {
            return endTime;
        }
        
        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
    }
    
    // 构造函数
    public Robot() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Robot(String robotId, String name) {
        this();
        this.robotId = robotId;
        this.name = name;
    }
    
    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getRobotId() {
        return robotId;
    }
    
    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getIntroduction() {
        return introduction;
    }
    
    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
    
    public String getPersonality() {
        return personality;
    }
    
    public void setPersonality(String personality) {
        this.personality = personality;
    }
    
    public String getProfession() {
        return profession;
    }
    
    public void setProfession(String profession) {
        this.profession = profession;
    }
    
    public String getMbti() {
        return mbti;
    }
    
    public void setMbti(String mbti) {
        this.mbti = mbti;
    }
    
    public Integer getReplySpeed() {
        return replySpeed;
    }
    
    public void setReplySpeed(Integer replySpeed) {
        this.replySpeed = replySpeed;
    }
    
    public Integer getReplyFrequency() {
        return replyFrequency;
    }
    
    public void setReplyFrequency(Integer replyFrequency) {
        this.replyFrequency = replyFrequency;
    }
    
    public Integer getShareFrequency() {
        return shareFrequency;
    }
    
    public void setShareFrequency(Integer shareFrequency) {
        this.shareFrequency = shareFrequency;
    }
    
    public List<ActiveTimeRange> getActiveTimeRanges() {
        return activeTimeRanges;
    }
    
    public void setActiveTimeRanges(List<ActiveTimeRange> activeTimeRanges) {
        this.activeTimeRanges = activeTimeRanges;
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
     * 添加活跃时间段
     */
    public void addActiveTimeRange(String startTime, String endTime) {
        this.activeTimeRanges.add(new ActiveTimeRange(startTime, endTime));
    }
    
    /**
     * 检查是否在活跃时间段
     */
    public boolean isInActiveTimeSlot() {
        if (this.activeTimeRanges.isEmpty()) {
            return true; // 如果没有设置活跃时间，默认全天活跃
        }
        
        LocalDateTime now = LocalDateTime.now();
        String currentTime = String.format("%02d:%02d", now.getHour(), now.getMinute());
        
        for (ActiveTimeRange range : this.activeTimeRanges) {
            if (isTimeInRange(currentTime, range.getStartTime(), range.getEndTime())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查时间是否在指定范围内
     */
    private boolean isTimeInRange(String currentTime, String startTime, String endTime) {
        return currentTime.compareTo(startTime) >= 0 && currentTime.compareTo(endTime) <= 0;
    }
    
    /**
     * 获取机器人状态描述
     */
    public String getStatusDescription() {
        return this.isActive ? "在线" : "离线";
    }
    
    /**
     * 更新机器人信息
     */
    public void updateRobot(Robot robot) {
        this.name = robot.getName();
        this.avatar = robot.getAvatar();
        this.gender = robot.getGender();
        this.introduction = robot.getIntroduction();
        this.personality = robot.getPersonality();
        this.profession = robot.getProfession();
        this.mbti = robot.getMbti();
        this.replySpeed = robot.getReplySpeed();
        this.replyFrequency = robot.getReplyFrequency();
        this.shareFrequency = robot.getShareFrequency();
        this.activeTimeRanges = robot.getActiveTimeRanges();
        this.isActive = robot.getIsActive();
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Robot{" +
                "id='" + id + '\'' +
                ", robotId='" + robotId + '\'' +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", profession='" + profession + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
} 