package com.myeden.repository;

import com.myeden.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 评论数据访问层
 * 
 * 功能说明：
 * - 提供评论相关的数据库操作
 * - 支持按动态、作者等字段查询
 * - 提供评论统计和状态查询
 * - 支持评论数据分页和排序
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
    
    /**
     * 根据评论ID查找评论
     * @param commentId 评论ID
     * @return 评论信息
     */
    Optional<Comment> findByCommentId(String commentId);
    
    /**
     * 根据动态ID查找评论
     * @param postId 动态ID
     * @return 评论列表
     */
    List<Comment> findByPostId(String postId);
    
    /**
     * 根据作者ID查找评论
     * @param authorId 作者ID
     * @return 评论列表
     */
    List<Comment> findByAuthorId(String authorId);
    
    /**
     * 根据作者类型查找评论
     * @param authorType 作者类型
     * @return 评论列表
     */
    List<Comment> findByAuthorType(String authorType);
    
    /**
     * 根据作者ID和作者类型查找评论
     * @param authorId 作者ID
     * @param authorType 作者类型
     * @return 评论列表
     */
    List<Comment> findByAuthorIdAndAuthorType(String authorId, String authorType);
    
    /**
     * 根据父评论ID查找回复
     * @param parentId 父评论ID
     * @return 回复列表
     */
    List<Comment> findByParentId(String parentId);
    
    /**
     * 根据回复目标ID查找回复
     * @param replyToId 回复目标ID
     * @return 回复列表
     */
    List<Comment> findByReplyToId(String replyToId);
    
    /**
     * 根据是否删除查找评论
     * @param isDeleted 是否删除
     * @return 评论列表
     */
    List<Comment> findByIsDeleted(Boolean isDeleted);
    
    /**
     * 根据动态ID和是否删除查找评论
     * @param postId 动态ID
     * @param isDeleted 是否删除
     * @return 评论列表
     */
    List<Comment> findByPostIdAndIsDeleted(String postId, Boolean isDeleted);
    
    /**
     * 根据作者ID和是否删除查找评论
     * @param authorId 作者ID
     * @param isDeleted 是否删除
     * @return 评论列表
     */
    List<Comment> findByAuthorIdAndIsDeleted(String authorId, Boolean isDeleted);
    
    /**
     * 根据作者类型和是否删除查找评论
     * @param authorType 作者类型
     * @param isDeleted 是否删除
     * @return 评论列表
     */
    List<Comment> findByAuthorTypeAndIsDeleted(String authorType, Boolean isDeleted);
    
    /**
     * 查找指定动态的最近评论
     * @param postId 动态ID
     * @param limit 限制数量
     * @return 评论列表
     */
    @Query(value = "{'postId': ?0}", sort = "{'createdAt': -1}")
    List<Comment> findRecentCommentsByPost(String postId, int limit);
    
    /**
     * 查找指定作者的最近评论
     * @param authorId 作者ID
     * @param limit 限制数量
     * @return 评论列表
     */
    @Query(value = "{'authorId': ?0}", sort = "{'createdAt': -1}")
    List<Comment> findRecentCommentsByAuthor(String authorId, int limit);
    
    /**
     * 查找指定类型的最近评论
     * @param authorType 作者类型
     * @param limit 限制数量
     * @return 评论列表
     */
    @Query(value = "{'authorType': ?0}", sort = "{'createdAt': -1}")
    List<Comment> findRecentCommentsByType(String authorType, int limit);
    
    /**
     * 根据内容模糊查询评论
     * @param content 内容关键词
     * @return 评论列表
     */
    @Query("{'content': {$regex: ?0, $options: 'i'}}")
    List<Comment> findByContentContaining(String content);
    
    /**
     * 查找点赞数大于等于指定值的评论
     * @param likeCount 点赞数
     * @return 评论列表
     */
    @Query("{'likeCount': {$gte: ?0}}")
    List<Comment> findByLikeCountGreaterThanEqual(Integer likeCount);
    
    /**
     * 查找回复数大于等于指定值的评论
     * @param replyCount 回复数
     * @return 评论列表
     */
    @Query("{'replyCount': {$gte: ?0}}")
    List<Comment> findByReplyCountGreaterThanEqual(Integer replyCount);
    
    /**
     * 查找热门评论（按点赞数排序）
     * @param limit 限制数量
     * @return 评论列表
     */
    @Query(value = "{}", sort = "{'likeCount': -1}")
    List<Comment> findPopularComments(int limit);
    
    /**
     * 查找热门评论（按回复数排序）
     * @param limit 限制数量
     * @return 评论列表
     */
    @Query(value = "{}", sort = "{'replyCount': -1}")
    List<Comment> findMostRepliedComments(int limit);
    
    /**
     * 查找今日发布的评论
     * @return 评论列表
     */
    @Query("{'createdAt': {$gte: ?0}}")
    List<Comment> findTodayComments(java.time.LocalDateTime startOfDay);
    
    /**
     * 查找指定动态今日发布的评论
     * @param postId 动态ID
     * @return 评论列表
     */
    @Query("{'postId': ?0, 'createdAt': {$gte: ?1}}")
    List<Comment> findTodayCommentsByPost(String postId, java.time.LocalDateTime startOfDay);
    
    /**
     * 查找指定作者今日发布的评论
     * @param authorId 作者ID
     * @return 评论列表
     */
    @Query("{'authorId': ?0, 'createdAt': {$gte: ?1}}")
    List<Comment> findTodayCommentsByAuthor(String authorId, java.time.LocalDateTime startOfDay);
    
    /**
     * 查找指定类型今日发布的评论
     * @param authorType 作者类型
     * @return 评论列表
     */
    @Query("{'authorType': ?0, 'createdAt': {$gte: ?1}}")
    List<Comment> findTodayCommentsByType(String authorType, java.time.LocalDateTime startOfDay);
    
    /**
     * 查找评论数量统计
     * @return 评论总数
     */
    long count();
    
    /**
     * 根据动态ID统计评论数量
     * @param postId 动态ID
     * @return 评论数量
     */
    long countByPostId(String postId);
    
    /**
     * 根据作者ID统计评论数量
     * @param authorId 作者ID
     * @return 评论数量
     */
    long countByAuthorId(String authorId);
    
    /**
     * 根据作者类型统计评论数量
     * @param authorType 作者类型
     * @return 评论数量
     */
    long countByAuthorType(String authorType);
    
    /**
     * 根据父评论ID统计回复数量
     * @param parentId 父评论ID
     * @return 回复数量
     */
    long countByParentId(String parentId);
    
    /**
     * 根据是否删除统计评论数量
     * @param isDeleted 是否删除
     * @return 评论数量
     */
    long countByIsDeleted(Boolean isDeleted);
    
    /**
     * 根据动态ID和是否删除统计评论数量
     * @param postId 动态ID
     * @param isDeleted 是否删除
     * @return 评论数量
     */
    long countByPostIdAndIsDeleted(String postId, Boolean isDeleted);
    
    /**
     * 根据作者ID和是否删除统计评论数量
     * @param authorId 作者ID
     * @param isDeleted 是否删除
     * @return 评论数量
     */
    long countByAuthorIdAndIsDeleted(String authorId, Boolean isDeleted);
    
    /**
     * 根据作者类型和是否删除统计评论数量
     * @param authorType 作者类型
     * @param isDeleted 是否删除
     * @return 评论数量
     */
    long countByAuthorTypeAndIsDeleted(String authorType, Boolean isDeleted);
    
    /**
     * 统计今日发布的评论数量
     * @return 评论数量
     */
    @Query(value = "{'createdAt': {$gte: ?0}}", count = true)
    long countTodayComments(java.time.LocalDateTime startOfDay);
    
    /**
     * 统计指定动态今日发布的评论数量
     * @param postId 动态ID
     * @return 评论数量
     */
    @Query(value = "{'postId': ?0, 'createdAt': {$gte: ?1}}", count = true)
    long countTodayCommentsByPost(String postId, java.time.LocalDateTime startOfDay);
    
    /**
     * 统计指定作者今日发布的评论数量
     * @param authorId 作者ID
     * @return 评论数量
     */
    @Query(value = "{'authorId': ?0, 'createdAt': {$gte: ?1}}", count = true)
    long countTodayCommentsByAuthor(String authorId, java.time.LocalDateTime startOfDay);
    
    /**
     * 统计指定类型今日发布的评论数量
     * @param authorType 作者类型
     * @return 评论数量
     */
    @Query(value = "{'authorType': ?0, 'createdAt': {$gte: ?1}}", count = true)
    long countTodayCommentsByType(String authorType, java.time.LocalDateTime startOfDay);
    
    /**
     * 根据评论ID和是否删除查找评论
     * @param commentId 评论ID
     * @param isDeleted 是否删除
     * @return 评论信息
     */
    Optional<Comment> findByCommentIdAndIsDeleted(String commentId, Boolean isDeleted);
    
    /**
     * 根据动态ID和父评论ID为null且未删除分页查找评论
     * @param postId 动态ID
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    Page<Comment> findByPostIdAndParentIdIsNullAndIsDeletedFalse(String postId, Pageable pageable);
    
    /**
     * 根据父评论ID和未删除分页查找回复
     * @param parentId 父评论ID
     * @param pageable 分页参数
     * @return 回复分页结果
     */
    Page<Comment> findByParentIdAndIsDeletedFalse(String parentId, Pageable pageable);
    
    /**
     * 根据动态ID和未删除分页查找评论
     * @param postId 动态ID
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    Page<Comment> findByPostIdAndIsDeletedFalse(String postId, Pageable pageable);
    
    /**
     * 根据作者ID和未删除分页查找评论
     * @param authorId 作者ID
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    Page<Comment> findByAuthorIdAndIsDeletedFalse(String authorId, Pageable pageable);
    
    /**
     * 根据作者类型和未删除分页查找评论
     * @param authorType 作者类型
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    Page<Comment> findByAuthorTypeAndIsDeletedFalse(String authorType, Pageable pageable);
    
    /**
     * 根据评论ID和未删除查找评论
     * @param commentId 评论ID
     * @return 评论信息
     */
    Optional<Comment> findByCommentIdAndIsDeletedFalse(String commentId);
    
    /**
     * 根据动态ID、作者ID、作者类型和未删除统计评论数量
     * 用于检查指定机器人是否已评论过指定帖子
     * @param postId 动态ID
     * @param authorId 作者ID
     * @param authorType 作者类型
     * @return 评论数量
     */
    @Query(value = "{'postId': ?0, 'authorId': ?1, 'authorType': ?2, 'isDeleted': false}", count = true)
    long countByPostIdAndAuthorIdAndAuthorTypeAndIsDeletedFalse(String postId, String authorId, String authorType);
    
    /**
     * 根据创建时间之后和未删除查找评论，按创建时间倒序排列
     * 用于查找近期评论
     * @param createdAt 创建时间
     * @return 评论列表
     */
    @Query(value = "{'createdAt': {$gt: ?0}, 'isDeleted': false}", sort = "{'createdAt': -1}")
    List<Comment> findByCreatedAtAfterAndIsDeletedFalseOrderByCreatedAtDesc(LocalDateTime createdAt);
    
    /**
     * 根据回复目标ID、作者ID、作者类型和未删除统计回复数量
     * 用于检查指定机器人是否已回复过指定评论
     * @param replyToId 回复目标ID
     * @param authorId 作者ID
     * @param authorType 作者类型
     * @return 回复数量
     */
    @Query(value = "{'replyToId': ?0, 'authorId': ?1, 'authorType': ?2, 'isDeleted': false}", count = true)
    long countByReplyToIdAndAuthorIdAndAuthorTypeAndIsDeletedFalse(String replyToId, String authorId, String authorType);
} 