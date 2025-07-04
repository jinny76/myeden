package com.myeden.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import com.myeden.service.CommentService.CommentSummary;
import com.myeden.model.PostQueryParams;

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
     * @param visibility 可见性设置（public/private/null，null表示继承用户设置）
     * @return 动态发布结果
     */
    PostResult createPost(String authorId, String authorType, String content, List<MultipartFile> images, String visibility);
    
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
     * @param currentUserId 当前用户ID（可选，用于判断点赞状态）
     * @return 动态详细信息
     */
    PostDetail getPostDetail(String postId, String currentUserId);
    
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
     * 搜索动态（已废弃，建议使用queryPosts）
     * @param keyword 搜索关键字
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    PostListResult searchPosts(String keyword, int page, int size);
    
    /**
     * 统一动态查询方法
     * 整合分页、作者类型过滤、关键词搜索、安全性过滤、机器人ID过滤等功能
     * 
     * @param params 查询参数对象
     * @return 动态列表结果
     */
    PostListResult queryPosts(PostQueryParams params);
    
    /**
     * 获取动态的所有点赞信息
     * @param postId 动态ID
     * @param currentUserId 当前用户ID（可选，用于权限控制）
     * @return 点赞信息列表
     */
    LikeInfoResult getPostLikes(String postId, String currentUserId);
    
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
        private List<String> imageInfos;
        private int likeCount;
        private int commentCount;
        private boolean isLiked;
        private List<LikeDetail> likes; // 点赞详情列表
        private List<CommentSummary> comments; // 新增：评论列表
        private String createdAt;
        private String updatedAt;
        
        public PostDetail(String postId, String authorId, String authorType, String authorName, 
                         String authorAvatar, String content, List<String> images, List<String> imageInfos, int likeCount, 
                         int commentCount, boolean isLiked, List<LikeDetail> likes, 
                         List<CommentSummary> comments, String createdAt, String updatedAt) {
            this.postId = postId;
            this.authorId = authorId;
            this.authorType = authorType;
            this.authorName = authorName;
            this.authorAvatar = authorAvatar;
            this.content = content;
            this.images = images;
            this.imageInfos = imageInfos;
            this.likeCount = likeCount;
            this.commentCount = commentCount;
            this.isLiked = isLiked;
            this.likes = likes;
            this.comments = comments;
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
        public List<String> getImageInfos() { return imageInfos; }
        public int getLikeCount() { return likeCount; }
        public int getCommentCount() { return commentCount; }
        public boolean isLiked() { return isLiked; }
        public List<LikeDetail> getLikes() { return likes; }
        public List<CommentSummary> getComments() { return comments; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
    }
    
    /**
     * 点赞信息结果
     */
    class LikeInfoResult {
        private String postId;
        private int totalLikes;
        private List<LikeDetail> likes;
        
        public LikeInfoResult(String postId, int totalLikes, List<LikeDetail> likes) {
            this.postId = postId;
            this.totalLikes = totalLikes;
            this.likes = likes;
        }
        
        // Getter方法
        public String getPostId() { return postId; }
        public int getTotalLikes() { return totalLikes; }
        public List<LikeDetail> getLikes() { return likes; }
    }
    
    /**
     * 点赞详情
     */
    class LikeDetail {
        private String userId;
        private String userName;
        private String userAvatar;
        private String userType; // user/robot
        private String likedAt;
        
        public LikeDetail(String userId, String userName, String userAvatar, String userType, String likedAt) {
            this.userId = userId;
            this.userName = userName;
            this.userAvatar = userAvatar;
            this.userType = userType;
            this.likedAt = likedAt;
        }
        
        // Getter方法
        public String getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getUserAvatar() { return userAvatar; }
        public String getUserType() { return userType; }
        public String getLikedAt() { return likedAt; }
    }
} 