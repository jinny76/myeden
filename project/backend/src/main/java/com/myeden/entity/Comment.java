package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

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
     * 评论内容
     */
    private String content;
    
    /**
     * 评论者ID
     */
    @Indexed
    private String userId;
    
    /**
     * 评论者昵称
     */
    private String userNickname;
    
    /**
     * 评论者头像
     */
    private String userAvatar;
    
    /**
     * 所属动态ID
     */
    @Indexed
    private String postId;
    
    /**
     * 父评论ID（用于回复功能）
     */
    @Indexed
    private String parentId;
    
    /**
     * 回复的用户ID
     */
    private String replyUserId;
    
    /**
     * 回复的用户昵称
     */
    private String replyUserNickname;
    
    /**
     * 子评论列表
     */
    private List<Comment> replies = new ArrayList<>();
    
    /**
     * 点赞数
     */
    private Integer likeCount = 0;
    
    /**
     * 点赞用户ID列表
     */
    private List<String> likedUserIds = new ArrayList<>();
    
    /**
     * 评论状态：0-正常，1-已删除，2-已隐藏
     */
    private Integer status = 0;
    
    /**
     * 是否为机器人评论
     */
    private Boolean isRobot = false;
    
    /**
     * 机器人ID（如果是机器人评论）
     */
    private String robotId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    // 构造函数
    public Comment() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    public Comment(String content, String userId, String postId) {
        this();
        this.content = content;
        this.userId = userId;
        this.postId = postId;
    }
    
    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
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
    
    public String getPostId() {
        return postId;
    }
    
    public void setPostId(String postId) {
        this.postId = postId;
    }
    
    public String getParentId() {
        return parentId;
    }
    
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    
    public String getReplyUserId() {
        return replyUserId;
    }
    
    public void setReplyUserId(String replyUserId) {
        this.replyUserId = replyUserId;
    }
    
    public String getReplyUserNickname() {
        return replyUserNickname;
    }
    
    public void setReplyUserNickname(String replyUserNickname) {
        this.replyUserNickname = replyUserNickname;
    }
    
    public List<Comment> getReplies() {
        return replies;
    }
    
    public void setReplies(List<Comment> replies) {
        this.replies = replies;
    }
    
    public Integer getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
    
    public List<String> getLikedUserIds() {
        return likedUserIds;
    }
    
    public void setLikedUserIds(List<String> likedUserIds) {
        this.likedUserIds = likedUserIds;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Boolean getIsRobot() {
        return isRobot;
    }
    
    public void setIsRobot(Boolean isRobot) {
        this.isRobot = isRobot;
    }
    
    public String getRobotId() {
        return robotId;
    }
    
    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    /**
     * 添加回复
     */
    public void addReply(Comment reply) {
        this.replies.add(reply);
    }
    
    /**
     * 点赞
     */
    public void like(String userId) {
        if (!this.likedUserIds.contains(userId)) {
            this.likedUserIds.add(userId);
            this.likeCount++;
        }
    }
    
    /**
     * 取消点赞
     */
    public void unlike(String userId) {
        if (this.likedUserIds.contains(userId)) {
            this.likedUserIds.remove(userId);
            this.likeCount = Math.max(0, this.likeCount - 1);
        }
    }
    
    /**
     * 检查用户是否已点赞
     */
    public boolean isLikedBy(String userId) {
        return this.likedUserIds.contains(userId);
    }
    
    /**
     * 软删除评论
     */
    public void softDelete() {
        this.status = 1;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 隐藏评论
     */
    public void hide() {
        this.status = 2;
        this.updateTime = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", userId='" + userId + '\'' +
                ", postId='" + postId + '\'' +
                ", likeCount=" + likeCount +
                ", status=" + status +
                ", isRobot=" + isRobot +
                ", createTime=" + createTime +
                '}';
    }
} 