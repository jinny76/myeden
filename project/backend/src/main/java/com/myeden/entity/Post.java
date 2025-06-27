package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态实体类
 * 
 * 功能说明：
 * - 定义朋友圈动态的基本信息
 * - 包含内容、图片、统计信息等
 * - 支持MongoDB文档存储和全文搜索
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Document(collection = "posts")
public class Post {

    @Id
    private String id;

    @Indexed
    private String userId; // 发布者ID

    @Indexed
    private String userNickname; // 发布者昵称

    private String userAvatar; // 发布者头像

    @TextIndexed
    private String content; // 动态内容

    private List<String> images; // 图片URL列表

    private List<String> tags; // 标签列表

    private String location; // 发布位置

    private String mood; // 心情状态

    private String weather; // 天气信息

    // 统计信息
    private Integer likeCount = 0; // 点赞数量
    private Integer commentCount = 0; // 评论数量
    private Integer shareCount = 0; // 分享数量
    private Integer viewCount = 0; // 浏览数量

    // 互动信息
    private List<String> likedUserIds; // 点赞用户ID列表
    private List<String> commentedUserIds; // 评论用户ID列表

    // 权限设置
    private String visibility = "PUBLIC"; // 可见性：PUBLIC, FRIENDS, PRIVATE
    private Boolean allowComment = true; // 是否允许评论
    private Boolean allowLike = true; // 是否允许点赞

    // 机器人相关
    private Boolean isRobotPost = false; // 是否为机器人发布
    private String robotId; // 机器人ID
    private String robotName; // 机器人名称

    // 系统信息
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
    private Boolean isDeleted = false; // 是否删除
    private Boolean isPinned = false; // 是否置顶

    // 默认构造函数
    public Post() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.likedUserIds = List.of();
        this.commentedUserIds = List.of();
    }

    // 带参数的构造函数
    public Post(String userId, String userNickname, String content) {
        this();
        this.userId = userId;
        this.userNickname = userNickname;
        this.content = content;
    }

    // Getter和Setter方法
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

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getShareCount() {
        return shareCount;
    }

    public void setShareCount(Integer shareCount) {
        this.shareCount = shareCount;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public List<String> getLikedUserIds() {
        return likedUserIds;
    }

    public void setLikedUserIds(List<String> likedUserIds) {
        this.likedUserIds = likedUserIds;
    }

    public List<String> getCommentedUserIds() {
        return commentedUserIds;
    }

    public void setCommentedUserIds(List<String> commentedUserIds) {
        this.commentedUserIds = commentedUserIds;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public Boolean getAllowComment() {
        return allowComment;
    }

    public void setAllowComment(Boolean allowComment) {
        this.allowComment = allowComment;
    }

    public Boolean getAllowLike() {
        return allowLike;
    }

    public void setAllowLike(Boolean allowLike) {
        this.allowLike = allowLike;
    }

    public Boolean getIsRobotPost() {
        return isRobotPost;
    }

    public void setIsRobotPost(Boolean isRobotPost) {
        this.isRobotPost = isRobotPost;
    }

    public String getRobotId() {
        return robotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
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

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Boolean getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }

    /**
     * 增加点赞数量
     */
    public void incrementLikeCount() {
        this.likeCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 减少点赞数量
     */
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 增加评论数量
     */
    public void incrementCommentCount() {
        this.commentCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 减少评论数量
     */
    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 增加分享数量
     */
    public void incrementShareCount() {
        this.shareCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 增加浏览数量
     */
    public void incrementViewCount() {
        this.viewCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 添加点赞用户
     */
    public void addLikedUser(String userId) {
        if (!this.likedUserIds.contains(userId)) {
            this.likedUserIds.add(userId);
            this.incrementLikeCount();
        }
    }

    /**
     * 移除点赞用户
     */
    public void removeLikedUser(String userId) {
        if (this.likedUserIds.remove(userId)) {
            this.decrementLikeCount();
        }
    }

    /**
     * 添加评论用户
     */
    public void addCommentedUser(String userId) {
        if (!this.commentedUserIds.contains(userId)) {
            this.commentedUserIds.add(userId);
        }
    }

    /**
     * 检查用户是否已点赞
     */
    public Boolean isLikedByUser(String userId) {
        return this.likedUserIds.contains(userId);
    }

    /**
     * 检查用户是否已评论
     */
    public Boolean isCommentedByUser(String userId) {
        return this.commentedUserIds.contains(userId);
    }

    @Override
    public String toString() {
        return "Post{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", userNickname='" + userNickname + '\'' +
                ", content='" + content + '\'' +
                ", likeCount=" + likeCount +
                ", commentCount=" + commentCount +
                ", createdAt=" + createdAt +
                '}';
    }
} 