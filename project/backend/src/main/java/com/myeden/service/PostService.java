package com.myeden.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 动态管理服务接口
 * 
 * 功能说明：
 * - 提供动态发布、查询、删除功能
 * - 支持图片上传和处理
 * - 管理动态的点赞和评论统计
 * - 支持分页查询和排序
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface PostService {
    
    /**
     * 发布动态
     * @param authorId 作者ID
     * @param authorType 作者类型（user/robot）
     * @param content 动态内容
     * @param images 图片文件列表
     * @return 动态发布结果
     */
    PostResult createPost(String authorId, String authorType, String content, List<MultipartFile> images);
    
    /**
     * 获取动态列表
     * 
     * @param page 页码，从1开始
     * @param size 每页大小
     * @param authorType 作者类型（可选）
     * @param currentUserId 当前用户ID，用于隐私控制
     * @return 动态列表结果
     */
    PostListResult getPostList(int page, int size, String authorType, String currentUserId);
    
    /**
     * 获取动态详情
     * @param postId 动态ID
     * @return 动态详细信息
     */
    PostDetail getPostDetail(String postId);
    
    /**
     * 删除动态
     * @param postId 动态ID
     * @param authorId 作者ID（用于权限验证）
     * @return 删除结果
     */
    boolean deletePost(String postId, String authorId);
    
    /**
     * 点赞动态
     * @param postId 动态ID
     * @param userId 用户ID
     * @return 点赞结果
     */
    boolean likePost(String postId, String userId);
    
    /**
     * 取消点赞动态
     * @param postId 动态ID
     * @param userId 用户ID
     * @return 取消点赞结果
     */
    boolean unlikePost(String postId, String userId);
    
    /**
     * 获取用户的动态列表
     * @param authorId 作者ID
     * @param page 页码
     * @param size 每页大小
     * @return 用户动态列表
     */
    PostListResult getUserPosts(String authorId, int page, int size);
    
    /**
     * 根据关键字搜索动态
     * @param keyword 搜索关键字
     * @param searchType 搜索类型：content(内容)、author(发帖人)、all(全部)
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 搜索结果和分页信息
     */
    PostListResult searchPosts(String keyword, String searchType, int page, int size);
    
    /**
     * 动态发布结果
     */
    class PostResult {
        private String postId;
        private String content;
        private List<String> imageUrls;
        private String createdAt;
        
        public PostResult(String postId, String content, List<String> imageUrls, String createdAt) {
            this.postId = postId;
            this.content = content;
            this.imageUrls = imageUrls;
            this.createdAt = createdAt;
        }
        
        // Getter方法
        public String getPostId() { return postId; }
        public String getContent() { return content; }
        public List<String> getImageUrls() { return imageUrls; }
        public String getCreatedAt() { return createdAt; }
    }
    
    /**
     * 动态列表结果
     */
    class PostListResult {
        private List<PostSummary> posts;
        private int total;
        private int page;
        private int size;
        private int totalPages;
        
        public PostListResult(List<PostSummary> posts, int total, int page, int size) {
            this.posts = posts;
            this.total = total;
            this.page = page;
            this.size = size;
            this.totalPages = (int) Math.ceil((double) total / size);
        }
        
        // Getter方法
        public List<PostSummary> getPosts() { return posts; }
        public int getTotal() { return total; }
        public int getPage() { return page; }
        public int getSize() { return size; }
        public int getTotalPages() { return totalPages; }
    }
    
    /**
     * 动态摘要信息
     */
    class PostSummary {
        private String postId;
        private String authorId;
        private String authorType;
        private String authorName;
        private String authorAvatar;
        private String content;
        private List<String> images;
        private int likeCount;
        private int commentCount;
        private boolean isLiked;
        private String createdAt;
        private String updatedAt;
        
        public PostSummary(String postId, String authorId, String authorType, String authorName, 
                          String authorAvatar, String content, List<String> images, int likeCount, 
                          int commentCount, boolean isLiked, String createdAt, String updatedAt) {
            this.postId = postId;
            this.authorId = authorId;
            this.authorType = authorType;
            this.authorName = authorName;
            this.authorAvatar = authorAvatar;
            this.content = content;
            this.images = images;
            this.likeCount = likeCount;
            this.commentCount = commentCount;
            this.isLiked = isLiked;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
        
        // Getter方法
        public String getPostId() { return postId; }
        public String getAuthorId() { return authorId; }
        public String getAuthorType() { return authorType; }
        public String getAuthorName() { return authorName; }
        public String getAuthorAvatar() { return authorAvatar; }
        public String getContent() { return content; }
        public List<String> getImages() { return images; }
        public int getLikeCount() { return likeCount; }
        public int getCommentCount() { return commentCount; }
        public boolean isLiked() { return isLiked; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
    }
    
    /**
     * 动态详细信息
     */
    class PostDetail {
        private String postId;
        private String authorId;
        private String authorType;
        private String authorName;
        private String authorAvatar;
        private String content;
        private List<String> images;
        private int likeCount;
        private int commentCount;
        private boolean isLiked;
        private List<String> likedUsers;
        private String createdAt;
        private String updatedAt;
        
        public PostDetail(String postId, String authorId, String authorType, String authorName, 
                         String authorAvatar, String content, List<String> images, int likeCount, 
                         int commentCount, boolean isLiked, List<String> likedUsers, 
                         String createdAt, String updatedAt) {
            this.postId = postId;
            this.authorId = authorId;
            this.authorType = authorType;
            this.authorName = authorName;
            this.authorAvatar = authorAvatar;
            this.content = content;
            this.images = images;
            this.likeCount = likeCount;
            this.commentCount = commentCount;
            this.isLiked = isLiked;
            this.likedUsers = likedUsers;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
        
        // Getter方法
        public String getPostId() { return postId; }
        public String getAuthorId() { return authorId; }
        public String getAuthorType() { return authorType; }
        public String getAuthorName() { return authorName; }
        public String getAuthorAvatar() { return authorAvatar; }
        public String getContent() { return content; }
        public List<String> getImages() { return images; }
        public int getLikeCount() { return likeCount; }
        public int getCommentCount() { return commentCount; }
        public boolean isLiked() { return isLiked; }
        public List<String> getLikedUsers() { return likedUsers; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
    }
} 