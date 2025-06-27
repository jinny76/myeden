package com.myeden.repository;

import com.myeden.entity.CommentLike;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 评论点赞数据访问层
 * 
 * 功能说明：
 * - 提供评论点赞的增删改查操作
 * - 支持点赞状态查询和统计
 * - 防止重复点赞
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface CommentLikeRepository extends MongoRepository<CommentLike, String> {
    
    /**
     * 根据评论ID和用户ID查询点赞记录
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 点赞记录
     */
    Optional<CommentLike> findByCommentIdAndUserId(String commentId, String userId);
    
    /**
     * 根据评论ID查询所有点赞记录
     * 
     * @param commentId 评论ID
     * @return 点赞记录列表
     */
    List<CommentLike> findByCommentId(String commentId);
    
    /**
     * 根据评论ID统计点赞数量
     * 
     * @param commentId 评论ID
     * @return 点赞数量
     */
    long countByCommentId(String commentId);
    
    /**
     * 根据用户ID查询用户的所有点赞记录
     * 
     * @param userId 用户ID
     * @return 点赞记录列表
     */
    List<CommentLike> findByUserId(String userId);
    
    /**
     * 根据评论ID和用户ID删除点赞记录
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void deleteByCommentIdAndUserId(String commentId, String userId);
    
    /**
     * 根据评论ID删除所有点赞记录
     * 
     * @param commentId 评论ID
     */
    void deleteByCommentId(String commentId);
    
    /**
     * 根据用户ID删除用户的所有点赞记录
     * 
     * @param userId 用户ID
     */
    void deleteByUserId(String userId);
    
    /**
     * 检查用户是否已点赞指定评论
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    @Query("SELECT COUNT(c) > 0 FROM CommentLike c WHERE c.commentId = ?1 AND c.userId = ?2")
    boolean existsByCommentIdAndUserId(String commentId, String userId);
} 