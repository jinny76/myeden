package com.myeden.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 机器人配置类
 * 
 * 功能说明：
 * - 映射robots-config.yaml配置文件的结构
 * - 提供机器人基本设定、性格特征和行为模式
 * - 支持配置热更新和动态加载
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Component
@ConfigurationProperties(prefix = "robots")
public class RobotConfig {
    
    private BaseConfig baseConfig;
    private List<RobotInfo> list;
    
    // Getter和Setter方法
    public BaseConfig getBaseConfig() { return baseConfig; }
    public void setBaseConfig(BaseConfig baseConfig) { this.baseConfig = baseConfig; }
    
    public List<RobotInfo> getList() { return list; }
    public void setList(List<RobotInfo> list) { this.list = list; }
    
    /**
     * 机器人基础配置
     */
    public static class BaseConfig {
        private int totalCount;
        private int maxActivePerUser;
        private List<Integer> responseDelayRange;
        private int maxDailyPosts;
        private int maxDailyComments;
        private int maxDailyReplies;
        
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        
        public int getMaxActivePerUser() { return maxActivePerUser; }
        public void setMaxActivePerUser(int maxActivePerUser) { this.maxActivePerUser = maxActivePerUser; }
        
        public List<Integer> getResponseDelayRange() { return responseDelayRange; }
        public void setResponseDelayRange(List<Integer> responseDelayRange) { this.responseDelayRange = responseDelayRange; }
        
        public int getMaxDailyPosts() { return maxDailyPosts; }
        public void setMaxDailyPosts(int maxDailyPosts) { this.maxDailyPosts = maxDailyPosts; }
        
        public int getMaxDailyComments() { return maxDailyComments; }
        public void setMaxDailyComments(int maxDailyComments) { this.maxDailyComments = maxDailyComments; }
        
        public int getMaxDailyReplies() { return maxDailyReplies; }
        public void setMaxDailyReplies(int maxDailyReplies) { this.maxDailyReplies = maxDailyReplies; }
    }
    
    /**
     * 机器人信息配置
     */
    public static class RobotInfo {
        private String id;
        private String name;
        private String nickname;
        private String avatar;
        private String personality;
        private String description;
        private String background;
        
        // 个人基本信息
        private String gender;           // 性别：male/female/other
        private int age;                 // 年龄
        private String mbti;             // MBTI性格类型
        private String bloodType;        // 血型：A/B/O/AB
        private String zodiac;           // 星座
        private String occupation;       // 职业
        private String location;         // 所在地
        private String education;        // 学历
        private String relationship;     // 感情状态：single/married/in_relationship
        
        private List<String> traits;
        private List<String> interests;
        private SpeakingStyle speakingStyle;
        private BehaviorPatterns behaviorPatterns;
        private List<ActiveHours> activeHours;
        private boolean isActive;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        
        public String getPersonality() { return personality; }
        public void setPersonality(String personality) { this.personality = personality; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getBackground() { return background; }
        public void setBackground(String background) { this.background = background; }
        
        // 个人基本信息的Getter和Setter
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        
        public String getMbti() { return mbti; }
        public void setMbti(String mbti) { this.mbti = mbti; }
        
        public String getBloodType() { return bloodType; }
        public void setBloodType(String bloodType) { this.bloodType = bloodType; }
        
        public String getZodiac() { return zodiac; }
        public void setZodiac(String zodiac) { this.zodiac = zodiac; }
        
        public String getOccupation() { return occupation; }
        public void setOccupation(String occupation) { this.occupation = occupation; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public String getEducation() { return education; }
        public void setEducation(String education) { this.education = education; }
        
        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }
        
        public List<String> getTraits() { return traits; }
        public void setTraits(List<String> traits) { this.traits = traits; }
        
        public List<String> getInterests() { return interests; }
        public void setInterests(List<String> interests) { this.interests = interests; }
        
        public SpeakingStyle getSpeakingStyle() { return speakingStyle; }
        public void setSpeakingStyle(SpeakingStyle speakingStyle) { this.speakingStyle = speakingStyle; }
        
        public BehaviorPatterns getBehaviorPatterns() { return behaviorPatterns; }
        public void setBehaviorPatterns(BehaviorPatterns behaviorPatterns) { this.behaviorPatterns = behaviorPatterns; }
        
        public List<ActiveHours> getActiveHours() { return activeHours; }
        public void setActiveHours(List<ActiveHours> activeHours) { this.activeHours = activeHours; }
        
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
    }
    
    /**
     * 说话风格配置
     */
    public static class SpeakingStyle {
        private String tone;
        private String vocabulary;
        private String emojiUsage;
        private String sentenceLength;
        
        public String getTone() { return tone; }
        public void setTone(String tone) { this.tone = tone; }
        
        public String getVocabulary() { return vocabulary; }
        public void setVocabulary(String vocabulary) { this.vocabulary = vocabulary; }
        
        public String getEmojiUsage() { return emojiUsage; }
        public void setEmojiUsage(String emojiUsage) { this.emojiUsage = emojiUsage; }
        
        public String getSentenceLength() { return sentenceLength; }
        public void setSentenceLength(String sentenceLength) { this.sentenceLength = sentenceLength; }
    }
    
    /**
     * 行为模式配置
     */
    public static class BehaviorPatterns {
        private double greetingFrequency;
        private double comfortFrequency;
        private double shareFrequency;
        private double commentFrequency;
        private double replyFrequency;
        
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
    
    /**
     * 活跃时间段配置
     */
    public static class ActiveHours {
        private String start;
        private String end;
        private double probability;
        
        public String getStart() { return start; }
        public void setStart(String start) { this.start = start; }
        
        public String getEnd() { return end; }
        public void setEnd(String end) { this.end = end; }
        
        public double getProbability() { return probability; }
        public void setProbability(double probability) { this.probability = probability; }
    }
} 