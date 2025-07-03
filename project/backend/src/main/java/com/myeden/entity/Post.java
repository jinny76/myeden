package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * 动态实体类
 * 
 * 功能说明：
 * - 存储朋友圈动态的基本信息
 * - 支持用户和机器人发布动态
 * - 记录动态的统计信息和状态
 * - 支持图片和内容管理
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Document(collection = "posts")
public class Post {
    
    @Id
    private String id;
    
    /**
     * 动态ID，唯一
     */
    @Indexed(unique = true)
    private String postId;
    
    /**
     * 作者ID（用户或机器人）
     */
    @Indexed
    private String authorId;
    
    /**
     * 作者类型：user/robot
     */
    @Indexed
    private String authorType;
    
    /**
     * 动态内容
     */
    private String content;
    
    /**
     * 内心活动（仅机器人动态）
     */
    private String innerThoughts;
    
    /**
     * 图片URL数组，最多9张
     */
    private List<String> images = new ArrayList<>();
    
    /**
     * 点赞数
     */
    private Integer likeCount = 0;
    
    /**
     * 评论数
     */
    private Integer commentCount = 0;
    
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
    
    /**
     * 外部数据链接信息（如新闻、音乐、影视等原始url及相关元数据）
     */
    private LinkInfo link;
    
    /**
     * 外部数据链接对象
     */
    public static class LinkInfo {
        /** 链接URL */
        private String url;
        /** 链接标题 */
        private String title;
        /** 数据类型（如news/music/movie等） */
        private String dataType;
        /** 图片URL（如有） */
        private String image;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
    }
    
    // 构造函数
    public Post() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Post(String postId, String authorId, String authorType, String content) {
        this();
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
    
    public List<String> getImages() {
        return images;
    }
    
    public void setImages(List<String> images) {
        this.images = images;
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
    
    public LinkInfo getLink() {
        return link;
    }
    
    public void setLink(LinkInfo link) {
        this.link = link;
    }
    
    /**
     * 添加图片
     */
    public void addImage(String imageUrl) {
        if (this.images.size() < 9 && !this.images.contains(imageUrl)) {
            this.images.add(imageUrl);
        }
    }
    
    /**
     * 移除图片
     */
    public void removeImage(String imageUrl) {
        this.images.remove(imageUrl);
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
     * 增加评论数
     */
    public void incrementCommentCount() {
        this.commentCount++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 减少评论数
     */
    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 软删除动态
     */
    public void softDelete() {
        this.isDeleted = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 恢复动态
     */
    public void restore() {
        this.isDeleted = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 检查是否为机器人发布
     */
    public boolean isRobotPost() {
        return "robot".equals(this.authorType);
    }
    
    /**
     * 检查是否为用户发布
     */
    public boolean isUserPost() {
        return "user".equals(this.authorType);
    }
    
    /**
     * 更新动态内容
     */
    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Post{" +
                "id='" + id + '\'' +
                ", postId='" + postId + '\'' +
                ", authorId='" + authorId + '\'' +
                ", authorType='" + authorType + '\'' +
                ", content='" + content + '\'' +
                ", likeCount=" + likeCount +
                ", commentCount=" + commentCount +
                ", isDeleted=" + isDeleted +
                ", createdAt=" + createdAt +
                '}';
    }
} 