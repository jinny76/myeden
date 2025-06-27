package com.myeden.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 世界配置类
 * 
 * 功能说明：
 * - 映射world-config.yaml配置文件的结构
 * - 提供世界背景、环境、活动等配置信息
 * - 支持配置热更新和动态加载
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Component
@ConfigurationProperties(prefix = "world")
public class WorldConfig {
    
    private String name;
    private String version;
    private String description;
    private Background background;
    private Environment environment;
    private Activities activities;
    private Statistics statistics;
    private Settings settings;
    
    // Getter和Setter方法
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Background getBackground() { return background; }
    public void setBackground(Background background) { this.background = background; }
    
    public Environment getEnvironment() { return environment; }
    public void setEnvironment(Environment environment) { this.environment = environment; }
    
    public Activities getActivities() { return activities; }
    public void setActivities(Activities activities) { this.activities = activities; }
    
    public Statistics getStatistics() { return statistics; }
    public void setStatistics(Statistics statistics) { this.statistics = statistics; }
    
    public Settings getSettings() { return settings; }
    public void setSettings(Settings settings) { this.settings = settings; }
    
    /**
     * 世界背景配置
     */
    public static class Background {
        private String story;
        private List<String> rules;
        private List<String> features;
        
        public String getStory() { return story; }
        public void setStory(String story) { this.story = story; }
        
        public List<String> getRules() { return rules; }
        public void setRules(List<String> rules) { this.rules = rules; }
        
        public List<String> getFeatures() { return features; }
        public void setFeatures(List<String> features) { this.features = features; }
    }
    
    /**
     * 世界环境配置
     */
    public static class Environment {
        private String theme;
        private String colorScheme;
        private String atmosphere;
        private String weather;
        
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        
        public String getColorScheme() { return colorScheme; }
        public void setColorScheme(String colorScheme) { this.colorScheme = colorScheme; }
        
        public String getAtmosphere() { return atmosphere; }
        public void setAtmosphere(String atmosphere) { this.atmosphere = atmosphere; }
        
        public String getWeather() { return weather; }
        public void setWeather(String weather) { this.weather = weather; }
    }
    
    /**
     * 世界活动配置
     */
    public static class Activities {
        private List<DailyEvent> dailyEvents;
        private List<SpecialEvent> specialEvents;
        
        public List<DailyEvent> getDailyEvents() { return dailyEvents; }
        public void setDailyEvents(List<DailyEvent> dailyEvents) { this.dailyEvents = dailyEvents; }
        
        public List<SpecialEvent> getSpecialEvents() { return specialEvents; }
        public void setSpecialEvents(List<SpecialEvent> specialEvents) { this.specialEvents = specialEvents; }
    }
    
    /**
     * 日常活动配置
     */
    public static class DailyEvent {
        private String name;
        private String time;
        private String description;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    /**
     * 特殊活动配置
     */
    public static class SpecialEvent {
        private String name;
        private String trigger;
        private String description;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getTrigger() { return trigger; }
        public void setTrigger(String trigger) { this.trigger = trigger; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    /**
     * 世界统计配置
     */
    public static class Statistics {
        private int totalUsers;
        private int totalPosts;
        private int totalComments;
        private int totalRobots;
        private String worldCreatedAt;
        
        public int getTotalUsers() { return totalUsers; }
        public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
        
        public int getTotalPosts() { return totalPosts; }
        public void setTotalPosts(int totalPosts) { this.totalPosts = totalPosts; }
        
        public int getTotalComments() { return totalComments; }
        public void setTotalComments(int totalComments) { this.totalComments = totalComments; }
        
        public int getTotalRobots() { return totalRobots; }
        public void setTotalRobots(int totalRobots) { this.totalRobots = totalRobots; }
        
        public String getWorldCreatedAt() { return worldCreatedAt; }
        public void setWorldCreatedAt(String worldCreatedAt) { this.worldCreatedAt = worldCreatedAt; }
    }
    
    /**
     * 世界设置配置
     */
    public static class Settings {
        private int maxPostLength;
        private int maxCommentLength;
        private String maxImageSize;
        private List<String> allowedImageTypes;
        private int autoCleanupDays;
        private int maxRobotsPerUser;
        
        public int getMaxPostLength() { return maxPostLength; }
        public void setMaxPostLength(int maxPostLength) { this.maxPostLength = maxPostLength; }
        
        public int getMaxCommentLength() { return maxCommentLength; }
        public void setMaxCommentLength(int maxCommentLength) { this.maxCommentLength = maxCommentLength; }
        
        public String getMaxImageSize() { return maxImageSize; }
        public void setMaxImageSize(String maxImageSize) { this.maxImageSize = maxImageSize; }
        
        public List<String> getAllowedImageTypes() { return allowedImageTypes; }
        public void setAllowedImageTypes(List<String> allowedImageTypes) { this.allowedImageTypes = allowedImageTypes; }
        
        public int getAutoCleanupDays() { return autoCleanupDays; }
        public void setAutoCleanupDays(int autoCleanupDays) { this.autoCleanupDays = autoCleanupDays; }
        
        public int getMaxRobotsPerUser() { return maxRobotsPerUser; }
        public void setMaxRobotsPerUser(int maxRobotsPerUser) { this.maxRobotsPerUser = maxRobotsPerUser; }
    }
} 