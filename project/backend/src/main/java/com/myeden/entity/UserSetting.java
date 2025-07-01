package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * 用户个性化设置实体
 * 
 * 功能说明：
 * - 存储用户的个性化偏好设置
 * - 支持主题模式、通知设置等配置项
 * - 提供默认值机制，确保新用户有良好的初始体验
 * - 结构可扩展，便于后续增加更多设置项
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Document(collection = "user_settings")
public class UserSetting {
    
    /**
     * 用户ID，作为主键
     */
    @Id
    private String userId;
    
    /**
     * 主题模式：light(浅色)、dark(深色)、auto(跟随系统)
     */
    @Field("theme_mode")
    private String themeMode = "auto";
    
    /**
     * 用户上线通知开关
     */
    @Field("notify_user_online")
    private Boolean notifyUserOnline = true;
    
    /**
     * 动态发布通知开关
     */
    @Field("notify_post_published")
    private Boolean notifyPostPublished = true;
    
    /**
     * 评论通知开关
     */
    @Field("notify_comment_received")
    private Boolean notifyCommentReceived = true;
    
    /**
     * 点赞通知开关
     */
    @Field("notify_like_received")
    private Boolean notifyLikeReceived = true;
    
    /**
     * 机器人互动通知开关
     */
    @Field("notify_robot_interaction")
    private Boolean notifyRobotInteraction = true;
    
    /**
     * 公开我的帖子开关
     */
    @Field("public_posts")
    private Boolean publicPosts = false;
    
    /**
     * 语言设置：zh-CN(中文)、en-US(英文)
     */
    @Field("language")
    private String language = "zh-CN";
    
    /**
     * 时区设置
     */
    @Field("timezone")
    private String timezone = "Asia/Shanghai";
    
    /**
     * 创建时间
     */
    @Field("created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    // 构造函数
    public UserSetting() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public UserSetting(String userId) {
        this();
        this.userId = userId;
    }
    
    public UserSetting(String userId, String themeMode, Boolean notifyUserOnline) {
        this(userId);
        this.themeMode = themeMode != null ? themeMode : "auto";
        this.notifyUserOnline = notifyUserOnline != null ? notifyUserOnline : true;
    }
    
    // Getter和Setter方法
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getThemeMode() {
        return themeMode;
    }
    
    public void setThemeMode(String themeMode) {
        this.themeMode = themeMode != null ? themeMode : "auto";
    }
    
    public Boolean getNotifyUserOnline() {
        return notifyUserOnline;
    }
    
    public void setNotifyUserOnline(Boolean notifyUserOnline) {
        this.notifyUserOnline = notifyUserOnline != null ? notifyUserOnline : true;
    }
    
    public Boolean getNotifyPostPublished() {
        return notifyPostPublished;
    }
    
    public void setNotifyPostPublished(Boolean notifyPostPublished) {
        this.notifyPostPublished = notifyPostPublished != null ? notifyPostPublished : true;
    }
    
    public Boolean getNotifyCommentReceived() {
        return notifyCommentReceived;
    }
    
    public void setNotifyCommentReceived(Boolean notifyCommentReceived) {
        this.notifyCommentReceived = notifyCommentReceived != null ? notifyCommentReceived : true;
    }
    
    public Boolean getNotifyLikeReceived() {
        return notifyLikeReceived;
    }
    
    public void setNotifyLikeReceived(Boolean notifyLikeReceived) {
        this.notifyLikeReceived = notifyLikeReceived != null ? notifyLikeReceived : true;
    }
    
    public Boolean getNotifyRobotInteraction() {
        return notifyRobotInteraction;
    }
    
    public void setNotifyRobotInteraction(Boolean notifyRobotInteraction) {
        this.notifyRobotInteraction = notifyRobotInteraction != null ? notifyRobotInteraction : true;
    }
    
    public Boolean getPublicPosts() {
        return publicPosts;
    }
    
    public void setPublicPosts(Boolean publicPosts) {
        this.publicPosts = publicPosts != null ? publicPosts : false;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language != null ? language : "zh-CN";
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone != null ? timezone : "Asia/Shanghai";
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
     * 更新设置时自动更新时间戳
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 验证设置参数的有效性
     */
    public void validate() {
        // 验证主题模式
        if (themeMode == null || (!themeMode.equals("light") && !themeMode.equals("dark") && !themeMode.equals("auto"))) {
            themeMode = "auto";
        }
        
        // 验证语言设置
        if (language == null || (!language.equals("zh-CN") && !language.equals("en-US"))) {
            language = "zh-CN";
        }
        
        // 验证时区设置
        if (timezone == null || timezone.trim().isEmpty()) {
            timezone = "Asia/Shanghai";
        }
        
        // 确保布尔值不为null
        if (notifyUserOnline == null) notifyUserOnline = true;
        if (notifyPostPublished == null) notifyPostPublished = true;
        if (notifyCommentReceived == null) notifyCommentReceived = true;
        if (notifyLikeReceived == null) notifyLikeReceived = true;
        if (notifyRobotInteraction == null) notifyRobotInteraction = true;
        if (publicPosts == null) publicPosts = false;
    }
    
    /**
     * 获取默认设置
     */
    public static UserSetting getDefaultSetting(String userId) {
        UserSetting setting = new UserSetting(userId);
        setting.validate();
        return setting;
    }
    
    @Override
    public String toString() {
        return "UserSetting{" +
                "userId='" + userId + '\'' +
                ", themeMode='" + themeMode + '\'' +
                ", notifyUserOnline=" + notifyUserOnline +
                ", notifyPostPublished=" + notifyPostPublished +
                ", notifyCommentReceived=" + notifyCommentReceived +
                ", notifyLikeReceived=" + notifyLikeReceived +
                ", notifyRobotInteraction=" + notifyRobotInteraction +
                ", language='" + language + '\'' +
                ", timezone='" + timezone + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 