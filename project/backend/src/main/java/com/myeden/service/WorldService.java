package com.myeden.service;

import com.myeden.config.WorldConfig;
import com.myeden.config.RobotConfig;

import java.util.List;
import java.util.Map;

/**
 * 世界管理服务接口
 * 
 * 功能说明：
 * - 提供世界信息查询功能
 * - 管理世界背景和设定展示
 * - 提供世界统计信息
 * - 管理世界活动信息
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface WorldService {
    
    /**
     * 获取世界基本信息
     * @return 世界基本信息
     */
    WorldInfo getWorldInfo();
    
    /**
     * 获取世界背景信息
     * @return 世界背景信息
     */
    WorldBackground getWorldBackground();
    
    /**
     * 获取世界环境信息
     * @return 世界环境信息
     */
    WorldEnvironment getWorldEnvironment();
    
    /**
     * 获取世界活动信息
     * @return 世界活动信息
     */
    WorldActivities getWorldActivities();
    
    /**
     * 获取世界统计信息
     * @return 世界统计信息
     */
    WorldStatistics getWorldStatistics();
    
    /**
     * 获取世界设置信息
     * @return 世界设置信息
     */
    WorldSettings getWorldSettings();
    
    /**
     * 获取机器人列表
     * @return 机器人列表
     */
    List<RobotSummary> getRobotList();
    
    /**
     * 获取机器人详情
     * @param robotId 机器人ID
     * @return 机器人详情
     */
    RobotDetail getRobotDetail(String robotId);
    
    /**
     * 世界基本信息
     */
    class WorldInfo {
        private String name;
        private String version;
        private String description;
        private String createdAt;
        
        public WorldInfo(String name, String version, String description, String createdAt) {
            this.name = name;
            this.version = version;
            this.description = description;
            this.createdAt = createdAt;
        }
        
        // Getter方法
        public String getName() { return name; }
        public String getVersion() { return version; }
        public String getDescription() { return description; }
        public String getCreatedAt() { return createdAt; }
    }
    
    /**
     * 世界背景信息
     */
    class WorldBackground {
        private String story;
        private List<String> rules;
        private List<String> features;
        
        public WorldBackground(String story, List<String> rules, List<String> features) {
            this.story = story;
            this.rules = rules;
            this.features = features;
        }
        
        // Getter方法
        public String getStory() { return story; }
        public List<String> getRules() { return rules; }
        public List<String> getFeatures() { return features; }
    }
    
    /**
     * 世界环境信息
     */
    class WorldEnvironment {
        private String theme;
        private String colorScheme;
        private String atmosphere;
        private String weather;
        
        public WorldEnvironment(String theme, String colorScheme, String atmosphere, String weather) {
            this.theme = theme;
            this.colorScheme = colorScheme;
            this.atmosphere = atmosphere;
            this.weather = weather;
        }
        
        // Getter方法
        public String getTheme() { return theme; }
        public String getColorScheme() { return colorScheme; }
        public String getAtmosphere() { return atmosphere; }
        public String getWeather() { return weather; }
    }
    
    /**
     * 世界活动信息
     */
    class WorldActivities {
        private List<DailyEvent> dailyEvents;
        private List<SpecialEvent> specialEvents;
        
        public WorldActivities(List<DailyEvent> dailyEvents, List<SpecialEvent> specialEvents) {
            this.dailyEvents = dailyEvents;
            this.specialEvents = specialEvents;
        }
        
        // Getter方法
        public List<DailyEvent> getDailyEvents() { return dailyEvents; }
        public List<SpecialEvent> getSpecialEvents() { return specialEvents; }
    }
    
    /**
     * 日常活动
     */
    class DailyEvent {
        private String name;
        private String time;
        private String description;
        
        public DailyEvent(String name, String time, String description) {
            this.name = name;
            this.time = time;
            this.description = description;
        }
        
        // Getter方法
        public String getName() { return name; }
        public String getTime() { return time; }
        public String getDescription() { return description; }
    }
    
    /**
     * 特殊活动
     */
    class SpecialEvent {
        private String name;
        private String trigger;
        private String description;
        
        public SpecialEvent(String name, String trigger, String description) {
            this.name = name;
            this.trigger = trigger;
            this.description = description;
        }
        
        // Getter方法
        public String getName() { return name; }
        public String getTrigger() { return trigger; }
        public String getDescription() { return description; }
    }
    
    /**
     * 世界统计信息
     */
    class WorldStatistics {
        private int totalUsers;
        private int totalPosts;
        private int totalComments;
        private int totalRobots;
        private String worldCreatedAt;
        
        public WorldStatistics(int totalUsers, int totalPosts, int totalComments, int totalRobots, String worldCreatedAt) {
            this.totalUsers = totalUsers;
            this.totalPosts = totalPosts;
            this.totalComments = totalComments;
            this.totalRobots = totalRobots;
            this.worldCreatedAt = worldCreatedAt;
        }
        
        // Getter方法
        public int getTotalUsers() { return totalUsers; }
        public int getTotalPosts() { return totalPosts; }
        public int getTotalComments() { return totalComments; }
        public int getTotalRobots() { return totalRobots; }
        public String getWorldCreatedAt() { return worldCreatedAt; }
    }
    
    /**
     * 世界设置信息
     */
    class WorldSettings {
        private int maxPostLength;
        private int maxCommentLength;
        private String maxImageSize;
        private List<String> allowedImageTypes;
        private int autoCleanupDays;
        private int maxRobotsPerUser;
        
        public WorldSettings(int maxPostLength, int maxCommentLength, String maxImageSize, 
                           List<String> allowedImageTypes, int autoCleanupDays, int maxRobotsPerUser) {
            this.maxPostLength = maxPostLength;
            this.maxCommentLength = maxCommentLength;
            this.maxImageSize = maxImageSize;
            this.allowedImageTypes = allowedImageTypes;
            this.autoCleanupDays = autoCleanupDays;
            this.maxRobotsPerUser = maxRobotsPerUser;
        }
        
        // Getter方法
        public int getMaxPostLength() { return maxPostLength; }
        public int getMaxCommentLength() { return maxCommentLength; }
        public String getMaxImageSize() { return maxImageSize; }
        public List<String> getAllowedImageTypes() { return allowedImageTypes; }
        public int getAutoCleanupDays() { return autoCleanupDays; }
        public int getMaxRobotsPerUser() { return maxRobotsPerUser; }
    }
    
    /**
     * 机器人摘要信息
     */
    class RobotSummary {
        private String id;
        private String name;
        private String nickname;
        private String avatar;
        private String personality;
        private String description;
        private boolean isActive;
        
        public RobotSummary(String id, String name, String nickname, String avatar, 
                          String personality, String description, boolean isActive) {
            this.id = id;
            this.name = name;
            this.nickname = nickname;
            this.avatar = avatar;
            this.personality = personality;
            this.description = description;
            this.isActive = isActive;
        }
        
        // Getter方法
        public String getId() { return id; }
        public String getName() { return name; }
        public String getNickname() { return nickname; }
        public String getAvatar() { return avatar; }
        public String getPersonality() { return personality; }
        public String getDescription() { return description; }
        public boolean isActive() { return isActive; }
    }
    
    /**
     * 机器人详细信息
     */
    class RobotDetail {
        private String id;
        private String name;
        private String nickname;
        private String avatar;
        private String personality;
        private String description;
        private String background;
        private List<String> traits;
        private List<String> interests;
        private SpeakingStyle speakingStyle;
        private BehaviorPatterns behaviorPatterns;
        private List<ActiveHours> activeHours;
        private boolean isActive;
        
        public RobotDetail(String id, String name, String nickname, String avatar, String personality,
                         String description, String background, List<String> traits, List<String> interests,
                         SpeakingStyle speakingStyle, BehaviorPatterns behaviorPatterns, 
                         List<ActiveHours> activeHours, boolean isActive) {
            this.id = id;
            this.name = name;
            this.nickname = nickname;
            this.avatar = avatar;
            this.personality = personality;
            this.description = description;
            this.background = background;
            this.traits = traits;
            this.interests = interests;
            this.speakingStyle = speakingStyle;
            this.behaviorPatterns = behaviorPatterns;
            this.activeHours = activeHours;
            this.isActive = isActive;
        }
        
        // Getter方法
        public String getId() { return id; }
        public String getName() { return name; }
        public String getNickname() { return nickname; }
        public String getAvatar() { return avatar; }
        public String getPersonality() { return personality; }
        public String getDescription() { return description; }
        public String getBackground() { return background; }
        public List<String> getTraits() { return traits; }
        public List<String> getInterests() { return interests; }
        public SpeakingStyle getSpeakingStyle() { return speakingStyle; }
        public BehaviorPatterns getBehaviorPatterns() { return behaviorPatterns; }
        public List<ActiveHours> getActiveHours() { return activeHours; }
        public boolean isActive() { return isActive; }
    }
    
    /**
     * 说话风格
     */
    class SpeakingStyle {
        private String tone;
        private String vocabulary;
        private String emojiUsage;
        private String sentenceLength;
        
        public SpeakingStyle(String tone, String vocabulary, String emojiUsage, String sentenceLength) {
            this.tone = tone;
            this.vocabulary = vocabulary;
            this.emojiUsage = emojiUsage;
            this.sentenceLength = sentenceLength;
        }
        
        // Getter方法
        public String getTone() { return tone; }
        public String getVocabulary() { return vocabulary; }
        public String getEmojiUsage() { return emojiUsage; }
        public String getSentenceLength() { return sentenceLength; }
    }
    
    /**
     * 行为模式
     */
    class BehaviorPatterns {
        private double greetingFrequency;
        private double comfortFrequency;
        private double shareFrequency;
        private double commentFrequency;
        private double replyFrequency;
        
        public BehaviorPatterns(double greetingFrequency, double comfortFrequency, double shareFrequency,
                              double commentFrequency, double replyFrequency) {
            this.greetingFrequency = greetingFrequency;
            this.comfortFrequency = comfortFrequency;
            this.shareFrequency = shareFrequency;
            this.commentFrequency = commentFrequency;
            this.replyFrequency = replyFrequency;
        }
        
        // Getter方法
        public double getGreetingFrequency() { return greetingFrequency; }
        public double getComfortFrequency() { return comfortFrequency; }
        public double getShareFrequency() { return shareFrequency; }
        public double getCommentFrequency() { return commentFrequency; }
        public double getReplyFrequency() { return replyFrequency; }
    }
    
    /**
     * 活跃时间
     */
    class ActiveHours {
        private String start;
        private String end;
        private double probability;
        
        public ActiveHours(String start, String end, double probability) {
            this.start = start;
            this.end = end;
            this.probability = probability;
        }
        
        // Getter方法
        public String getStart() { return start; }
        public String getEnd() { return end; }
        public double getProbability() { return probability; }
    }
} 