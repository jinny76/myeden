package com.myeden.repository;

import com.myeden.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论数据访问层
 * 
 * 功能说明：
 * - 提供评论相关的数据库操作
 * - 支持按动态、用户、父评论等字段查询
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
     * 根据动态ID查找评论
     * @param postId 动态ID
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    Page<Comment> findByPostId(String postId, Pageable pageable);
    
    /**
     * 根据动态ID查找评论（按创建时间倒序）
     * @param postId 动态ID
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    @Query(value = "{'postId': ?0}", sort = "{'createTime': -1}")
    Page<Comment> findByPostIdOrderByCreateTimeDesc(String postId, Pageable pageable);
    
    /**
     * 根据用户ID查找评论
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    Page<Comment> findByUserId(String userId, Pageable pageable);
    
    /**
     * 根据用户ID查找评论（按创建时间倒序）
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    @Query(value = "{'userId': ?0}", sort = "{'createTime': -1}")
    Page<Comment> findByUserIdOrderByCreateTimeDesc(String userId, Pageable pageable);
    
    /**
     * 根据父评论ID查找回复
     * @param parentId 父评论ID
     * @param pageable 分页参数
     * @return 回复分页结果
     */
    Page<Comment> findByParentId(String parentId, Pageable pageable);
    
    /**
     * 根据父评论ID查找回复（按创建时间倒序）
     * @param parentId 父评论ID
     * @param pageable 分页参数
     * @return 回复分页结果
     */
    @Query(value = "{'parentId': ?0}", sort = "{'createTime': -1}")
    Page<Comment> findByParentIdOrderByCreateTimeDesc(String parentId, Pageable pageable);
    
    /**
     * 查找顶级评论（没有父评论的评论）
     * @param postId 动态ID
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    @Query("{'postId': ?0, 'parentId': {$exists: false}}")
    Page<Comment> findTopLevelComments(String postId, Pageable pageable);
    
    /**
     * 查找顶级评论（按创建时间倒序）
     * @param postId 动态ID
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    @Query(value = "{'postId': ?0, 'parentId': {$exists: false}}", sort = "{'createTime': -1}")
    Page<Comment> findTopLevelCommentsOrderByCreateTimeDesc(String postId, Pageable pageable);
    
    /**
     * 根据状态查找评论
     * @param status 评论状态
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    Page<Comment> findByStatus(Integer status, Pageable pageable);
    
    /**
     * 查找机器人评论
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    @Query("{'isRobot': true}")
    Page<Comment> findRobotComments(Pageable pageable);
    
    /**
     * 查找用户评论
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    @Query("{'isRobot': false}")
    Page<Comment> findUserComments(Pageable pageable);
    
    /**
     * 根据机器人ID查找评论
     * @param robotId 机器人ID
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    Page<Comment> findByRobotId(String robotId, Pageable pageable);
    
    /**
     * 根据回复用户ID查找评论
     * @param replyUserId 回复用户ID
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    Page<Comment> findByReplyUserId(String replyUserId, Pageable pageable);
    
    /**
     * 查找热门评论（按点赞数排序）
     * @param postId 动态ID
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    @Query(value = "{'postId': ?0}", sort = "{'likeCount': -1}")
    Page<Comment> findHotComments(String postId, Pageable pageable);
    
    /**
     * 查找最新评论（按创建时间排序）
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    @Query(value = "{}", sort = "{'createTime': -1}")
    Page<Comment> findLatestComments(Pageable pageable);
    
    /**
     * 查找指定时间范围内的评论
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    @Query("{'createTime': {$gte: ?0, $lte: ?1}}")
    Page<Comment> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 查找今日发布的评论
     * @param startOfDay 今日开始时间
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    @Query("{'createTime': {$gte: ?0}}")
    Page<Comment> findTodayComments(LocalDateTime startOfDay, Pageable pageable);
    
    /**
     * 查找本周发布的评论
     * @param startOfWeek 本周开始时间
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    @Query("{'createTime': {$gte: ?0}}")
    Page<Comment> findThisWeekComments(LocalDateTime startOfWeek, Pageable pageable);
    
    /**
     * 查找本月发布的评论
     * @param startOfMonth 本月开始时间
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    @Query("{'createTime': {$gte: ?0}}")
    Page<Comment> findThisMonthComments(LocalDateTime startOfMonth, Pageable pageable);
    
    /**
     * 根据内容关键词查找评论
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    @Query("{'content': {$regex: ?0, $options: 'i'}}")
    Page<Comment> findByContentContaining(String keyword, Pageable pageable);
    
    /**
     * 查找用户点赞的评论
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    @Query("{'likedUserIds': ?0}")
    Page<Comment> findLikedCommentsByUser(String userId, Pageable pageable);
    
    /**
     * 根据动态ID和状态查找评论
     * @param postId 动态ID
     * @param status 评论状态
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    Page<Comment> findByPostIdAndStatus(String postId, Integer status, Pageable pageable);
    
    /**
     * 根据用户ID和状态查找评论
     * @param userId 用户ID
     * @param status 评论状态
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    Page<Comment> findByUserIdAndStatus(String userId, Integer status, Pageable pageable);
    
    /**
     * 统计动态评论数量
     * @param postId 动态ID
     * @return 评论数量
     */
    long countByPostId(String postId);
    
    /**
     * 统计用户评论数量
     * @param userId 用户ID
     * @return 评论数量
     */
    long countByUserId(String userId);
    
    /**
     * 统计机器人评论数量
     * @param robotId 机器人ID
     * @return 评论数量
     */
    long countByRobotId(String robotId);
    
    /**
     * 统计父评论的回复数量
     * @param parentId 父评论ID
     * @return 回复数量
     */
    long countByParentId(String parentId);
    
    /**
     * 统计顶级评论数量
     * @param postId 动态ID
     * @return 评论数量
     */
    @Query(value = "{'postId': ?0, 'parentId': {$exists: false}}", count = true)
    long countTopLevelComments(String postId);
    
    /**
     * 统计今日评论数量
     * @param startOfDay 今日开始时间
     * @return 评论数量
     */
    @Query(value = "{'createTime': {$gte: ?0}}", count = true)
    long countTodayComments(LocalDateTime startOfDay);
    
    /**
     * 统计本周评论数量
     * @param startOfWeek 本周开始时间
     * @return 评论数量
     */
    @Query(value = "{'createTime': {$gte: ?0}}", count = true)
    long countThisWeekComments(LocalDateTime startOfWeek);
    
    /**
     * 统计本月评论数量
     * @param startOfMonth 本月开始时间
     * @return 评论数量
     */
    @Query(value = "{'createTime': {$gte: ?0}}", count = true)
    long countThisMonthComments(LocalDateTime startOfMonth);
    
    /**
     * 统计状态为指定值的评论数量
     * @param status 评论状态
     * @return 评论数量
     */
    long countByStatus(Integer status);
    
    /**
     * 统计机器人评论数量
     * @return 评论数量
     */
    @Query(value = "{'isRobot': true}", count = true)
    long countRobotComments();
    
    /**
     * 统计用户评论数量
     * @return 评论数量
     */
    @Query(value = "{'isRobot': false}", count = true)
    long countUserComments();
    
    /**
     * 查找所有评论（不分页）
     * @return 评论列表
     */
    @Query(value = "{}", sort = "{'createTime': -1}")
    List<Comment> findAllOrderByCreateTimeDesc();
    
    /**
     * 根据动态ID查找所有评论
     * @param postId 动态ID
     * @return 评论列表
     */
    @Query(value = "{'postId': ?0}", sort = "{'createTime': -1}")
    List<Comment> findAllByPostIdOrderByCreateTimeDesc(String postId);
    
    /**
     * 根据用户ID查找所有评论
     * @param userId 用户ID
     * @return 评论列表
     */
    @Query(value = "{'userId': ?0}", sort = "{'createTime': -1}")
    List<Comment> findAllByUserIdOrderByCreateTimeDesc(String userId);
    
    /**
     * 查找热门评论（不分页）
     * @param limit 限制数量
     * @return 评论列表
     */
    @Query(value = "{}", sort = "{'likeCount': -1}")
    List<Comment> findHotComments(int limit);
    
    /**
     * 查找最新评论（不分页）
     * @param limit 限制数量
     * @return 评论列表
     */
    @Query(value = "{}", sort = "{'createTime': -1}")
    List<Comment> findLatestComments(int limit);
    
    /**
     * 查找动态的所有顶级评论（不分页）
     * @param postId 动态ID
     * @return 评论列表
     */
    @Query(value = "{'postId': ?0, 'parentId': {$exists: false}}", sort = "{'createTime': -1}")
    List<Comment> findAllTopLevelCommentsByPostId(String postId);
    
    /**
     * 查找父评论的所有回复（不分页）
     * @param parentId 父评论ID
     * @return 回复列表
     */
    @Query(value = "{'parentId': ?0}", sort = "{'createTime': -1}")
    List<Comment> findAllRepliesByParentId(String parentId);
} 