package com.myeden.entity;

import java.time.LocalDateTime;

/**
 * 用户隐私设置类
 * 
 * 功能说明：
 * - 存储用户的隐私控制设置
 * - 管理用户发帖和回复的可见性
 * - 控制用户查看他人内容的权限
 * 
 * @author MyEden Team
 * @version 1.0.1
 * @since 2025-01-27
 */
public class UserPrivacySettings {
    
    /**
     * 发帖可见性：private/public，默认private
     */
    private String postVisibility = "private";
    
    /**
     * 回复可见性：private/public，默认private
     */
    private String replyVisibility = "private";
    
    /**
     * 是否查看他人发帖，默认true
     */
    private boolean viewOthersPosts = true;
    
    /**
     * 是否查看他人回复，默认true
     */
    private boolean viewOthersReplies = true;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    // 构造函数
    public UserPrivacySettings() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public String getPostVisibility() {
        return postVisibility;
    }
    
    public void setPostVisibility(String postVisibility) {
        this.postVisibility = postVisibility;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getReplyVisibility() {
        return replyVisibility;
    }
    
    public void setReplyVisibility(String replyVisibility) {
        this.replyVisibility = replyVisibility;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isViewOthersPosts() {
        return viewOthersPosts;
    }
    
    public void setViewOthersPosts(boolean viewOthersPosts) {
        this.viewOthersPosts = viewOthersPosts;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isViewOthersReplies() {
        return viewOthersReplies;
    }
    
    public void setViewOthersReplies(boolean viewOthersReplies) {
        this.viewOthersReplies = viewOthersReplies;
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
     * 检查发帖是否可见
     */
    public boolean isPostVisible() {
        return "public".equals(postVisibility);
    }
    
    /**
     * 检查回复是否可见
     */
    public boolean isReplyVisible() {
        return "public".equals(replyVisibility);
    }
    
    /**
     * 更新隐私设置
     */
    public void updatePrivacySettings(String postVisibility, String replyVisibility, 
                                    boolean viewOthersPosts, boolean viewOthersReplies) {
        this.postVisibility = postVisibility;
        this.replyVisibility = replyVisibility;
        this.viewOthersPosts = viewOthersPosts;
        this.viewOthersReplies = viewOthersReplies;
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "UserPrivacySettings{" +
                "postVisibility='" + postVisibility + '\'' +
                ", replyVisibility='" + replyVisibility + '\'' +
                ", viewOthersPosts=" + viewOthersPosts +
                ", viewOthersReplies=" + viewOthersReplies +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 