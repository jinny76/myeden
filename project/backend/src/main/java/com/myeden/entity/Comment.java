package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * 评论实体类
 * 
 * 功能说明：
 * - 存储动态的评论信息
 * - 支持多级评论回复
 * - 记录评论者和被评论者信息
 * - 支持评论状态管理
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Document(collection = "comments")
public class Comment {
    
    @Id
    private String id;
    
    /**
     * 评论ID，唯一
     */
    @Indexed(unique = true)
    private String commentId;
    
    /**
     * 动态ID
     */
    @Indexed
    private String postId;
    
    /**
     * 评论者ID
     */
    private String authorId;
    
    /**
     * 评论者类型：user/robot
     */
    private String authorType;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 内心活动（仅机器人评论）
     */
    private String innerThoughts;
    
    /**
     * 父评论ID（用于回复）
     */
    @Indexed
    private String parentId;
    
    /**
     * 回复目标ID
     */
    private String replyToId;
    
    /**
     * 点赞数
     */
    private Integer likeCount = 0;
    
    /**
     * 回复数
     */
    private Integer replyCount = 0;
    
    /**
     * 是否删除
     */
    private Boolean isDeleted = false;
    
    /**
     * 可见性：private/public，继承用户设置
     */
    private String visibility = "private";
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    // 构造函数
    public Comment() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Comment(String commentId, String postId, String authorId, String authorType, String content) {
        this();
        this.commentId = commentId;
        this.postId = postId;
        this.authorId = authorId;
        this.authorType = authorType;
        this.content = content;
    }
    
    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCommentId() {
        return commentId;
    }
    
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
    
    public String getPostId() {
        return postId;
    }
    
    public void setPostId(String postId) {
        this.postId = postId;
    }
    
    public String getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
    
    public String getAuthorType() {
        return authorType;
    }
    
    public void setAuthorType(String authorType) {
        this.authorType = authorType;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getInnerThoughts() {
        return innerThoughts;
    }
    
    public void setInnerThoughts(String innerThoughts) {
        this.innerThoughts = innerThoughts;
    }
    
    public String getParentId() {
        return parentId;
    }
    
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    
    public String getReplyToId() {
        return replyToId;
    }
    
    public void setReplyToId(String replyToId) {
        this.replyToId = replyToId;
    }
    
    public Integer getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
    
    public Integer getReplyCount() {
        return replyCount;
    }
    
    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }
    
    public Boolean getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
    
    public String getVisibility() {
        return visibility;
    }
    
    public void setVisibility(String visibility) {
        this.visibility = visibility;
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
     * 增加点赞数
     */
    public void incrementLikeCount() {
        this.likeCount++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 减少点赞数
     */
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 增加回复数
     */
    public void incrementReplyCount() {
        this.replyCount++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 减少回复数
     */
    public void decrementReplyCount() {
        if (this.replyCount > 0) {
            this.replyCount--;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 软删除评论
     */
    public void softDelete() {
        this.isDeleted = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 恢复评论
     */
    public void restore() {
        this.isDeleted = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 检查是否为机器人评论
     */
    public boolean isRobotComment() {
        return "robot".equals(this.authorType);
    }
    
    /**
     * 检查是否为用户评论
     */
    public boolean isUserComment() {
        return "user".equals(this.authorType);
    }
    
    /**
     * 检查是否为回复评论
     */
    public boolean isReply() {
        return this.parentId != null && !this.parentId.isEmpty();
    }
    
    /**
     * 更新评论内容
     */
    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", commentId='" + commentId + '\'' +
                ", postId='" + postId + '\'' +
                ", authorId='" + authorId + '\'' +
                ", authorType='" + authorType + '\'' +
                ", content='" + content + '\'' +
                ", innerThoughts='" + innerThoughts + '\'' +
                ", parentId='" + parentId + '\'' +
                ", likeCount=" + likeCount +
                ", replyCount=" + replyCount +
                ", isDeleted=" + isDeleted +
                ", visibility='" + visibility + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
} 