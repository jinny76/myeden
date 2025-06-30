package com.myeden.service;

import java.util.List;
import com.myeden.entity.Comment;

/**
 * 评论管理服务接口
 * 
 * 功能说明：
 * - 提供评论发布、查询、删除功能
 * - 支持评论回复功能
 * - 管理评论的点赞和回复统计
 * - 支持分页查询和排序
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface CommentService {
    
    /**
     * 发表评论
     * @param postId 动态ID
     * @param authorId 评论者ID
     * @param authorType 评论者类型（user/robot）
     * @param content 评论内容
     * @param innerThoughts 内心活动（可选，主要用于机器人）
     * @return 评论发布结果
     */
    CommentResult createComment(String postId, String authorId, String authorType, String content, String innerThoughts);
    
    /**
     * 发表评论（重载方法，兼容旧版本）
     * @param postId 动态ID
     * @param authorId 评论者ID
     * @param authorType 评论者类型（user/robot）
     * @param content 评论内容
     * @return 评论发布结果
     */
    default CommentResult createComment(String postId, String authorId, String authorType, String content) {
        return createComment(postId, authorId, authorType, content, null);
    }
    
    /**
     * 回复评论
     * @param commentId 评论ID
     * @param authorId 回复者ID
     * @param authorType 回复者类型（user/robot）
     * @param content 回复内容
     * @param innerThoughts 内心活动（可选，主要用于机器人）
     * @return 回复发布结果
     */
    CommentResult replyComment(String commentId, String authorId, String authorType, String content, String innerThoughts);
    
    /**
     * 回复评论（重载方法，兼容旧版本）
     * @param commentId 评论ID
     * @param authorId 回复者ID
     * @param authorType 回复者类型（user/robot）
     * @param content 回复内容
     * @return 回复发布结果
     */
    default CommentResult replyComment(String commentId, String authorId, String authorType, String content) {
        return replyComment(commentId, authorId, authorType, content, null);
    }
    
    /**
     * 获取动态评论列表
     * 
     * @param postId 动态ID
     * @param page 页码，从1开始
     * @param size 每页大小
     * @param currentUserId 当前用户ID，用于隐私控制
     * @return 评论列表结果
     */
    CommentListResult getCommentList(String postId, int page, int size, String currentUserId);
    
    /**
     * 获取评论回复列表
     * 
     * @param commentId 评论ID
     * @param page 页码，从1开始
     * @param size 每页大小
     * @param currentUserId 当前用户ID，用于隐私控制
     * @return 回复列表结果
     */
    CommentListResult getReplyList(String commentId, int page, int size, String currentUserId);
    
    /**
     * 获取评论详情
     * @param commentId 评论ID
     * @return 评论详细信息
     */
    CommentDetail getCommentDetail(String commentId);
    
    /**
     * 删除评论
     * @param commentId 评论ID
     * @param authorId 作者ID（用于权限验证）
     * @return 删除结果
     */
    boolean deleteComment(String commentId, String authorId);
    
    /**
     * 点赞评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 点赞结果
     */
    boolean likeComment(String commentId, String userId);
    
    /**
     * 取消点赞评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 取消点赞结果
     */
    boolean unlikeComment(String commentId, String userId);
    
    /**
     * 检查指定机器人是否已经评论过指定帖子
     * @param robotId 机器人ID
     * @param postId 帖子ID
     * @return 是否已评论过
     */
    boolean hasRobotCommentedOnPost(String robotId, String postId);
    
    /**
     * 查找指定时间之后的评论
     * @param since 起始时间
     * @return 评论列表
     */
    List<Comment> findRecentComments(java.time.LocalDateTime since);
    
    /**
     * 检查指定机器人是否已经回复过指定评论
     * @param robotId 机器人ID
     * @param commentId 评论ID
     * @return 是否已回复过
     */
    boolean hasRobotRepliedToComment(String robotId, String commentId);
    
    /**
     * 评论发布结果
     */
    class CommentResult {
        private String commentId;
        private String content;
        private String parentId;
        private String replyToId;
        private String createdAt;
        
        public CommentResult(String commentId, String content, String parentId, String replyToId, String createdAt) {
            this.commentId = commentId;
            this.content = content;
            this.parentId = parentId;
            this.replyToId = replyToId;
            this.createdAt = createdAt;
        }
        
        // Getter方法
        public String getCommentId() { return commentId; }
        public String getContent() { return content; }
        public String getParentId() { return parentId; }
        public String getReplyToId() { return replyToId; }
        public String getCreatedAt() { return createdAt; }
    }
    
    /**
     * 评论列表结果
     */
    class CommentListResult {
        private List<CommentSummary> comments;
        private int total;
        private int page;
        private int size;
        private int totalPages;
        
        public CommentListResult(List<CommentSummary> comments, int total, int page, int size) {
            this.comments = comments;
            this.total = total;
            this.page = page;
            this.size = size;
            this.totalPages = (int) Math.ceil((double) total / size);
        }
        
        // Getter方法
        public List<CommentSummary> getComments() { return comments; }
        public int getTotal() { return total; }
        public int getPage() { return page; }
        public int getSize() { return size; }
        public int getTotalPages() { return totalPages; }
    }
    
    /**
     * 评论摘要信息
     */
    class CommentSummary {
        private String commentId;
        private String postId;
        private String authorId;
        private String authorType;
        private String authorName;
        private String authorAvatar;
        private String content;
        private String innerThoughts;
        private String parentId;
        private String replyToId;
        private String replyToName;
        private int likeCount;
        private int replyCount;
        private boolean isLiked;
        private String createdAt;
        private String updatedAt;
        
        public CommentSummary(String commentId, String postId, String authorId, String authorType, 
                             String authorName, String authorAvatar, String content, String innerThoughts, String parentId, 
                             String replyToId, String replyToName, int likeCount, int replyCount, 
                             boolean isLiked, String createdAt, String updatedAt) {
            this.commentId = commentId;
            this.postId = postId;
            this.authorId = authorId;
            this.authorType = authorType;
            this.authorName = authorName;
            this.authorAvatar = authorAvatar;
            this.content = content;
            this.innerThoughts = innerThoughts;
            this.parentId = parentId;
            this.replyToId = replyToId;
            this.replyToName = replyToName;
            this.likeCount = likeCount;
            this.replyCount = replyCount;
            this.isLiked = isLiked;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
        
        // Getter方法
        public String getCommentId() { return commentId; }
        public String getPostId() { return postId; }
        public String getAuthorId() { return authorId; }
        public String getAuthorType() { return authorType; }
        public String getAuthorName() { return authorName; }
        public String getAuthorAvatar() { return authorAvatar; }
        public String getContent() { return content; }
        public String getInnerThoughts() { return innerThoughts; }
        public String getParentId() { return parentId; }
        public String getReplyToId() { return replyToId; }
        public String getReplyToName() { return replyToName; }
        public int getLikeCount() { return likeCount; }
        public int getReplyCount() { return replyCount; }
        public boolean isLiked() { return isLiked; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
    }
    
    /**
     * 评论详细信息
     */
    class CommentDetail {
        private String commentId;
        private String postId;
        private String authorId;
        private String authorType;
        private String authorName;
        private String authorAvatar;
        private String content;
        private String innerThoughts;
        private String parentId;
        private String replyToId;
        private String replyToName;
        private int likeCount;
        private int replyCount;
        private boolean isLiked;
        private List<String> likedUsers;
        private String createdAt;
        private String updatedAt;
        
        public CommentDetail(String commentId, String postId, String authorId, String authorType, 
                            String authorName, String authorAvatar, String content, String innerThoughts, String parentId, 
                            String replyToId, String replyToName, int likeCount, int replyCount, 
                            boolean isLiked, List<String> likedUsers, String createdAt, String updatedAt) {
            this.commentId = commentId;
            this.postId = postId;
            this.authorId = authorId;
            this.authorType = authorType;
            this.authorName = authorName;
            this.authorAvatar = authorAvatar;
            this.content = content;
            this.innerThoughts = innerThoughts;
            this.parentId = parentId;
            this.replyToId = replyToId;
            this.replyToName = replyToName;
            this.likeCount = likeCount;
            this.replyCount = replyCount;
            this.isLiked = isLiked;
            this.likedUsers = likedUsers;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
        
        // Getter方法
        public String getCommentId() { return commentId; }
        public String getPostId() { return postId; }
        public String getAuthorId() { return authorId; }
        public String getAuthorType() { return authorType; }
        public String getAuthorName() { return authorName; }
        public String getAuthorAvatar() { return authorAvatar; }
        public String getContent() { return content; }
        public String getInnerThoughts() { return innerThoughts; }
        public String getParentId() { return parentId; }
        public String getReplyToId() { return replyToId; }
        public String getReplyToName() { return replyToName; }
        public int getLikeCount() { return likeCount; }
        public int getReplyCount() { return replyCount; }
        public boolean isLiked() { return isLiked; }
        public List<String> getLikedUsers() { return likedUsers; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
    }
} 