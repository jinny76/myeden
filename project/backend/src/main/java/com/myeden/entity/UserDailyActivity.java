package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户每日活动统计实体
 * 
 * 功能说明：
 * - 存储用户每日的活动统计数据
 * - 支持各类活动的计数统计
 * - 用于生成活动贡献图
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-01-27
 */
@Document(collection = "user_daily_activities")
@CompoundIndexes({
    @CompoundIndex(name = "user_date_idx", def = "{'userId': 1, 'date': 1}", unique = true),
    @CompoundIndex(name = "user_date_range_idx", def = "{'userId': 1, 'date': -1}")
})
public class UserDailyActivity {
    
    @Id
    private String id;
    
    /**
     * 用户ID
     */
    @Indexed
    private String userId;
    
    /**
     * 日期
     */
    @Indexed
    private LocalDate date;
    
    /**
     * 聊天次数
     */
    private Integer chatCount = 0;
    
    /**
     * 发帖次数
     */
    private Integer postCount = 0;
    
    /**
     * 评论次数
     */
    private Integer commentCount = 0;
    
    /**
     * 回复次数
     */
    private Integer replyCount = 0;
    
    /**
     * 点赞次数
     */
    private Integer likeCount = 0;
    
    /**
     * 总活动次数
     */
    private Integer totalCount = 0;
    
    /**
     * 活动详情（Map形式存储各类活动数据）
     */
    private Map<String, Integer> activityDetails;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 构造函数
     */
    public UserDailyActivity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public UserDailyActivity(String userId, LocalDate date) {
        this();
        this.userId = userId;
        this.date = date;
    }
    
    /**
     * 增加聊天次数
     */
    public void incrementChatCount() {
        this.chatCount++;
        updateTotalCount();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 增加发帖次数
     */
    public void incrementPostCount() {
        this.postCount++;
        updateTotalCount();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 增加评论次数
     */
    public void incrementCommentCount() {
        this.commentCount++;
        updateTotalCount();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 增加回复次数
     */
    public void incrementReplyCount() {
        this.replyCount++;
        updateTotalCount();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 增加点赞次数
     */
    public void incrementLikeCount() {
        this.likeCount++;
        updateTotalCount();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新总计数
     */
    private void updateTotalCount() {
        this.totalCount = chatCount + postCount + commentCount + replyCount + likeCount;
    }
    
    /**
     * 根据活动类型增加计数
     */
    public void incrementActivityCount(String activityType) {
        switch (activityType.toLowerCase()) {
            case "chat":
                incrementChatCount();
                break;
            case "post":
                incrementPostCount();
                break;
            case "comment":
                incrementCommentCount();
                break;
            case "reply":
                incrementReplyCount();
                break;
            case "like":
                incrementLikeCount();
                break;
            default:
                // 未知活动类型，不做处理
                break;
        }
    }
    
    // Getters and Setters
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
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public Integer getChatCount() {
        return chatCount;
    }
    
    public void setChatCount(Integer chatCount) {
        this.chatCount = chatCount;
        updateTotalCount();
    }
    
    public Integer getPostCount() {
        return postCount;
    }
    
    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
        updateTotalCount();
    }
    
    public Integer getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
        updateTotalCount();
    }
    
    public Integer getReplyCount() {
        return replyCount;
    }
    
    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
        updateTotalCount();
    }
    
    public Integer getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
        updateTotalCount();
    }
    
    public Integer getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
    
    public Map<String, Integer> getActivityDetails() {
        return activityDetails;
    }
    
    public void setActivityDetails(Map<String, Integer> activityDetails) {
        this.activityDetails = activityDetails;
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
}