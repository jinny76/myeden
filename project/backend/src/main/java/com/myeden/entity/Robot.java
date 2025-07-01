package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
     * 机器人年龄
     */
    private Integer age;
    
    /**
     * 一句话简介
     */
    private String introduction;
    
    /**
     * 性格设定
     */
    private String personality;
    
    /**
     * MBTI
     */
    private String mbti;
    
    /**
     * 血型
     */
    private String bloodType;
    
    /**
     * 星座
     */
    private String zodiac;
    
    /**
     * 所在地
     */
    private String location;

    /**
     * 职业
     */
    private String occupation;

    /**
     * 背景
     */
    private String background;

    /**
     * 学历
     */
    private String education;
    
    /**
     * 感情状态
     */
    private String relationship;
    
    /**
     * 家庭背景
     */
    private String family;
    
    /**
     * 性格特征列表
     */
    private List<String> traits = new ArrayList<>();
    
    /**
     * 兴趣爱好列表
     */
    private List<String> interests = new ArrayList<>();
    
    /**
     * 说话风格
     */
    private SpeakingStyle speakingStyle;
    
    /**
     * 行为模式
     */
    private BehaviorPatterns behaviorPatterns;
    
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
     * 个人主题列表
     * 存储机器人喜欢讨论的个人主题，用于内容生成
     */
    private List<Topic> topics = new ArrayList<>();
    
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
    
    // 内部类：说话风格
    public static class SpeakingStyle {
        private String tone;
        private String vocabulary;
        private String emojiUsage;
        private String sentenceLength;
        
        public SpeakingStyle() {}
        
        public String getTone() { return tone; }
        public void setTone(String tone) { this.tone = tone; }
        
        public String getVocabulary() { return vocabulary; }
        public void setVocabulary(String vocabulary) { this.vocabulary = vocabulary; }
        
        public String getEmojiUsage() { return emojiUsage; }
        public void setEmojiUsage(String emojiUsage) { this.emojiUsage = emojiUsage; }
        
        public String getSentenceLength() { return sentenceLength; }
        public void setSentenceLength(String sentenceLength) { this.sentenceLength = sentenceLength; }
    }
    
    // 内部类：行为模式
    public static class BehaviorPatterns {
        private double greetingFrequency;
        private double comfortFrequency;
        private double shareFrequency;
        private double commentFrequency;
        private double replyFrequency;
        
        public BehaviorPatterns() {}
        
        public double getGreetingFrequency() { return greetingFrequency; }
        public void setGreetingFrequency(double greetingFrequency) { this.greetingFrequency = greetingFrequency; }
        
        public double getComfortFrequency() { return comfortFrequency; }
        public void setComfortFrequency(double comfortFrequency) { this.comfortFrequency = comfortFrequency; }
        
        public double getShareFrequency() { return shareFrequency; }
        public void setShareFrequency(double shareFrequency) { this.shareFrequency = shareFrequency; }
        
        public double getCommentFrequency() { return commentFrequency; }
        public void setCommentFrequency(double commentFrequency) { this.commentFrequency = commentFrequency; }
        
        public double getReplyFrequency() { return replyFrequency; }
        public void setReplyFrequency(double replyFrequency) { this.replyFrequency = replyFrequency; }
    }
    
    // 内部类：个人主题
    public static class Topic {
        private String name;
        private int frequency;
        private String content;
        
        public Topic() {}
        
        public Topic(String name, int frequency, String content) {
            this.name = name;
            this.frequency = frequency;
            this.content = content;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getFrequency() { return frequency; }
        public void setFrequency(int frequency) { this.frequency = frequency; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
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
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
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
    
    public String getMbti() {
        return mbti;
    }
    
    public void setMbti(String mbti) {
        this.mbti = mbti;
    }
    
    public String getBloodType() {
        return bloodType;
    }
    
    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }
    
    public String getZodiac() {
        return zodiac;
    }
    
    public void setZodiac(String zodiac) {
        this.zodiac = zodiac;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }

    public String getOccupation() {
        return occupation;
    }
    
    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getBackground() {
        return background;
    }
    
    public void setBackground(String background) {
        this.background = background;
    }
    
    public String getEducation() {
        return education;
    }
    
    public void setEducation(String education) {
        this.education = education;
    }
    
    public String getRelationship() {
        return relationship;
    }
    
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
    
    public String getFamily() {
        return family;
    }
    
    public void setFamily(String family) {
        this.family = family;
    }
    
    public List<String> getTraits() {
        return traits;
    }
    
    public void setTraits(List<String> traits) {
        this.traits = traits;
    }
    
    public List<String> getInterests() {
        return interests;
    }
    
    public void setInterests(List<String> interests) {
        this.interests = interests;
    }
    
    public SpeakingStyle getSpeakingStyle() {
        return speakingStyle;
    }
    
    public void setSpeakingStyle(SpeakingStyle speakingStyle) {
        this.speakingStyle = speakingStyle;
    }
    
    public BehaviorPatterns getBehaviorPatterns() {
        return behaviorPatterns;
    }
    
    public void setBehaviorPatterns(BehaviorPatterns behaviorPatterns) {
        this.behaviorPatterns = behaviorPatterns;
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
    
    public List<Topic> getTopics() {
        return topics;
    }
    
    public void setTopics(List<Topic> topics) {
        this.topics = topics;
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
     * 添加个人主题
     */
    public void addTopic(String name, int frequency, String content) {
        this.topics.add(new Topic(name, frequency, content));
    }
    
    /**
     * 添加个人主题
     */
    public void addTopic(Topic topic) {
        this.topics.add(topic);
    }
    
    /**
     * 移除个人主题
     */
    public void removeTopic(String topicName) {
        this.topics.removeIf(topic -> topicName.equals(topic.getName()));
    }
    
    /**
     * 获取指定名称的主题
     */
    public Topic getTopicByName(String topicName) {
        return this.topics.stream()
                .filter(topic -> topicName.equals(topic.getName()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 检查是否在活跃时间段
     */
    public boolean isInActiveTimeSlot() {
        if (this.activeTimeRanges.isEmpty()) {
            return true; // 如果没有设置活跃时间，默认全天活跃
        }
        
        LocalTime currentTime = LocalTime.now();
        
        for (ActiveTimeRange range : this.activeTimeRanges) {
            if (isTimeInRange(currentTime, range.getStartTime(), range.getEndTime())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查时间是否在指定范围内（支持跨天时间段）
     */
    private boolean isTimeInRange(LocalTime currentTime, String startTimeStr, String endTimeStr) {
        LocalTime startTime = LocalTime.parse(startTimeStr);
        LocalTime endTime = LocalTime.parse(endTimeStr);
        
        // 如果结束时间小于开始时间，说明是跨天时间段
        if (endTime.isBefore(startTime)) {
            // 跨天时间段：当前时间 >= 开始时间 或 当前时间 <= 结束时间
            return currentTime.isAfter(startTime) || currentTime.equals(startTime) || 
                   currentTime.isBefore(endTime) || currentTime.equals(endTime);
        } else {
            // 同一天时间段：开始时间 <= 当前时间 <= 结束时间
            return (currentTime.isAfter(startTime) || currentTime.equals(startTime)) && 
                   (currentTime.isBefore(endTime) || currentTime.equals(endTime));
        }
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
        this.age = robot.getAge();
        this.introduction = robot.getIntroduction();
        this.personality = robot.getPersonality();
        this.occupation = robot.getOccupation();
        this.mbti = robot.getMbti();
        this.bloodType = robot.getBloodType();
        this.zodiac = robot.getZodiac();
        this.location = robot.getLocation();
        this.education = robot.getEducation();
        this.relationship = robot.getRelationship();
        this.family = robot.getFamily();
        this.traits = robot.getTraits();
        this.interests = robot.getInterests();
        this.speakingStyle = robot.getSpeakingStyle();
        this.behaviorPatterns = robot.getBehaviorPatterns();
        this.replySpeed = robot.getReplySpeed();
        this.replyFrequency = robot.getReplyFrequency();
        this.shareFrequency = robot.getShareFrequency();
        this.activeTimeRanges = robot.getActiveTimeRanges();
        this.topics = robot.getTopics();
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
                ", occupation='" + occupation + '\'' +
                ", isActive=" + isActive +
                ", topicsCount=" + (topics != null ? topics.size() : 0) +
                ", createdAt=" + createdAt +
                '}';
    }
} 