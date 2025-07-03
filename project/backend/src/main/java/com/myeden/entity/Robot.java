package com.myeden.entity;

import com.myeden.config.RobotConfig;
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
 * 用于数据库存储和业务逻辑，结构与RobotConfig.RobotInfo完全一致。
 * 支持机器人基础信息、性格、行为、活跃时间、主题等配置。
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
     * 机器人昵称
     */
    private String nickname;
    
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
    private String description;
    
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
    private List<ActiveHours> activeHours = new ArrayList<>();
    
    /**
     * 个人主题列表
     * 存储机器人喜欢讨论的个人主题，用于内容生成
     */
    private List<RobotConfig.Topic> topics = new ArrayList<>();
    
    /**
     * 机器人所有者ID
     */
    @Indexed
    private String owner;
    
    /**
     * 是否激活
     */
    private Boolean isActive = true;
    
    /**
     * 是否删除（软删除标记）
     */
    private Boolean isDeleted = false;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    // 内部类：活跃时间段
    public static class ActiveHours {
        private String start;
        private String end;
        private double probability;
        
        public ActiveHours() {}
        public ActiveHours(String start, String end) {
            this.start = start;
            this.end = end;
        }
        
        public String getStart() { return start; }
        public void setStart(String start) { this.start = start; }
        
        public String getEnd() { return end; }
        public void setEnd(String end) { this.end = end; }
        
        public double getProbability() { return probability; }
        public void setProbability(double probability) { this.probability = probability; }
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
    
    public Robot(String robotId, String name, String owner) {
        this();
        this.robotId = robotId;
        this.name = name;
        this.owner = owner;
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
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
        this.traits = traits != null ? traits : new ArrayList<>();
    }
    
    public List<String> getInterests() {
        return interests;
    }
    
    public void setInterests(List<String> interests) {
        this.interests = interests != null ? interests : new ArrayList<>();
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
    
    public List<ActiveHours> getActiveHours() {
        return activeHours;
    }
    
    public void setActiveHours(List<ActiveHours> activeHours) {
        this.activeHours = activeHours != null ? activeHours : new ArrayList<>();
    }
    
    public List<RobotConfig.Topic> getTopics() {
        return topics;
    }
    
    public void setTopics(List<RobotConfig.Topic> topics) {
        this.topics = topics != null ? topics : new ArrayList<>();
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
    
    public String getOwner() {
        return owner;
    }
    
    public void setOwner(String owner) {
        this.owner = owner;
    }
    
    public Boolean getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
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
    public void addActiveHour(String start, String end) {
        this.activeHours.add(new ActiveHours(start, end));
    }
    
    /**
     * 添加个人主题
     */
    public void addTopic(String name, int frequency, String content) {
        this.topics.add(new RobotConfig.Topic(name, frequency, content, ""));
    }
    
    /**
     * 添加个人主题
     */
    public void addTopic(RobotConfig.Topic topic) {
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
    public RobotConfig.Topic getTopicByName(String topicName) {
        return this.topics.stream()
                .filter(topic -> topicName.equals(topic.getName()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 检查是否在活跃时间段
     */
    public boolean isInActiveTimeSlot() {
        if (this.activeHours.isEmpty()) {
            return true; // 如果没有设置活跃时间，默认全天活跃
        }
        
        LocalTime currentTime = LocalTime.now();
        
        for (ActiveHours range : this.activeHours) {
            if (isTimeInRange(currentTime, range.getStart(), range.getEnd())) {
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
        this.description = robot.getDescription();
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
        this.activeHours = robot.getActiveHours();
        this.topics = robot.getTopics();
        this.isActive = robot.getIsActive();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 软删除机器人
     */
    public void softDelete() {
        this.isDeleted = true;
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 恢复机器人
     */
    public void restore() {
        this.isDeleted = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 检查机器人是否属于指定用户
     */
    public boolean isOwnedBy(String userId) {
        return userId != null && userId.equals(this.owner);
    }
    
    @Override
    public String toString() {
        return "Robot{" +
                "id='" + id + '\'' +
                ", robotId='" + robotId + '\'' +
                ", name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                ", gender='" + gender + '\'' +
                ", occupation='" + occupation + '\'' +
                ", isActive=" + isActive +
                ", isDeleted=" + isDeleted +
                ", topicsCount=" + (topics != null ? topics.size() : 0) +
                ", createdAt=" + createdAt +
                '}';
    }
} 