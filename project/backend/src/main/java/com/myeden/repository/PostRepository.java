package com.myeden.repository;

import com.myeden.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态数据访问层
 * 
 * 功能说明：
 * - 提供动态相关的数据库操作
 * - 支持按用户、标签、内容等字段查询
 * - 提供动态统计和状态查询
 * - 支持动态数据分页和排序
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    
    /**
     * 根据用户ID查找动态
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    Page<Post> findByUserId(String userId, Pageable pageable);
    
    /**
     * 根据用户ID查找动态（按创建时间倒序）
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query(value = "{'userId': ?0}", sort = "{'createTime': -1}")
    Page<Post> findByUserIdOrderByCreateTimeDesc(String userId, Pageable pageable);
    
    /**
     * 根据状态查找动态
     * @param status 动态状态
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    Page<Post> findByStatus(Integer status, Pageable pageable);
    
    /**
     * 根据标签查找动态
     * @param tag 标签
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'tags': ?0}")
    Page<Post> findByTag(String tag, Pageable pageable);
    
    /**
     * 根据多个标签查找动态
     * @param tags 标签列表
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'tags': {$in: ?0}}")
    Page<Post> findByTags(List<String> tags, Pageable pageable);
    
    /**
     * 根据内容关键词查找动态
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'content': {$regex: ?0, $options: 'i'}}")
    Page<Post> findByContentContaining(String keyword, Pageable pageable);
    
    /**
     * 查找热门动态（按点赞数排序）
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query(value = "{}", sort = "{'likeCount': -1}")
    Page<Post> findHotPosts(Pageable pageable);
    
    /**
     * 查找最新动态（按创建时间排序）
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query(value = "{}", sort = "{'createTime': -1}")
    Page<Post> findLatestPosts(Pageable pageable);
    
    /**
     * 查找评论数最多的动态
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query(value = "{}", sort = "{'commentCount': -1}")
    Page<Post> findMostCommentedPosts(Pageable pageable);
    
    /**
     * 查找机器人发布的动态
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'isRobot': true}")
    Page<Post> findRobotPosts(Pageable pageable);
    
    /**
     * 查找用户发布的动态
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'isRobot': false}")
    Page<Post> findUserPosts(Pageable pageable);
    
    /**
     * 根据机器人ID查找动态
     * @param robotId 机器人ID
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    Page<Post> findByRobotId(String robotId, Pageable pageable);
    
    /**
     * 查找指定时间范围内的动态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'createTime': {$gte: ?0, $lte: ?1}}")
    Page<Post> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 查找今日发布的动态
     * @param startOfDay 今日开始时间
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'createTime': {$gte: ?0}}")
    Page<Post> findTodayPosts(LocalDateTime startOfDay, Pageable pageable);
    
    /**
     * 查找本周发布的动态
     * @param startOfWeek 本周开始时间
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'createTime': {$gte: ?0}}")
    Page<Post> findThisWeekPosts(LocalDateTime startOfWeek, Pageable pageable);
    
    /**
     * 查找本月发布的动态
     * @param startOfMonth 本月开始时间
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'createTime': {$gte: ?0}}")
    Page<Post> findThisMonthPosts(LocalDateTime startOfMonth, Pageable pageable);
    
    /**
     * 查找有图片的动态
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'images': {$exists: true, $ne: []}}")
    Page<Post> findPostsWithImages(Pageable pageable);
    
    /**
     * 查找有视频的动态
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'videos': {$exists: true, $ne: []}}")
    Page<Post> findPostsWithVideos(Pageable pageable);
    
    /**
     * 查找置顶动态
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'isPinned': true}")
    Page<Post> findPinnedPosts(Pageable pageable);
    
    /**
     * 查找精华动态
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'isFeatured': true}")
    Page<Post> findFeaturedPosts(Pageable pageable);
    
    /**
     * 查找公开动态
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'visibility': 'public'}")
    Page<Post> findPublicPosts(Pageable pageable);
    
    /**
     * 查找好友可见动态
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'visibility': 'friends'}")
    Page<Post> findFriendsPosts(Pageable pageable);
    
    /**
     * 查找私密动态
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'visibility': 'private'}")
    Page<Post> findPrivatePosts(Pageable pageable);
    
    /**
     * 根据用户ID和状态查找动态
     * @param userId 用户ID
     * @param status 动态状态
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    Page<Post> findByUserIdAndStatus(String userId, Integer status, Pageable pageable);
    
    /**
     * 根据用户ID和可见性查找动态
     * @param userId 用户ID
     * @param visibility 可见性
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    Page<Post> findByUserIdAndVisibility(String userId, String visibility, Pageable pageable);
    
    /**
     * 查找用户点赞的动态
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'likedUserIds': ?0}")
    Page<Post> findLikedPostsByUser(String userId, Pageable pageable);
    
    /**
     * 查找用户收藏的动态
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{'favoritedUserIds': ?0}")
    Page<Post> findFavoritedPostsByUser(String userId, Pageable pageable);
    
    /**
     * 统计用户动态数量
     * @param userId 用户ID
     * @return 动态数量
     */
    long countByUserId(String userId);
    
    /**
     * 统计机器人动态数量
     * @param robotId 机器人ID
     * @return 动态数量
     */
    long countByRobotId(String robotId);
    
    /**
     * 统计今日动态数量
     * @param startOfDay 今日开始时间
     * @return 动态数量
     */
    @Query(value = "{'createTime': {$gte: ?0}}", count = true)
    long countTodayPosts(LocalDateTime startOfDay);
    
    /**
     * 统计本周动态数量
     * @param startOfWeek 本周开始时间
     * @return 动态数量
     */
    @Query(value = "{'createTime': {$gte: ?0}}", count = true)
    long countThisWeekPosts(LocalDateTime startOfWeek);
    
    /**
     * 统计本月动态数量
     * @param startOfMonth 本月开始时间
     * @return 动态数量
     */
    @Query(value = "{'createTime': {$gte: ?0}}", count = true)
    long countThisMonthPosts(LocalDateTime startOfMonth);
    
    /**
     * 统计状态为指定值的动态数量
     * @param status 动态状态
     * @return 动态数量
     */
    long countByStatus(Integer status);
    
    /**
     * 统计机器人动态数量
     * @return 动态数量
     */
    @Query(value = "{'isRobot': true}", count = true)
    long countRobotPosts();
    
    /**
     * 统计用户动态数量
     * @return 动态数量
     */
    @Query(value = "{'isRobot': false}", count = true)
    long countUserPosts();
    
    /**
     * 查找所有动态（不分页）
     * @return 动态列表
     */
    @Query(value = "{}", sort = "{'createTime': -1}")
    List<Post> findAllOrderByCreateTimeDesc();
    
    /**
     * 根据用户ID查找所有动态
     * @param userId 用户ID
     * @return 动态列表
     */
    @Query(value = "{'userId': ?0}", sort = "{'createTime': -1}")
    List<Post> findAllByUserIdOrderByCreateTimeDesc(String userId);
    
    /**
     * 查找热门动态（不分页）
     * @param limit 限制数量
     * @return 动态列表
     */
    @Query(value = "{}", sort = "{'likeCount': -1}")
    List<Post> findHotPosts(int limit);
    
    /**
     * 查找最新动态（不分页）
     * @param limit 限制数量
     * @return 动态列表
     */
    @Query(value = "{}", sort = "{'createTime': -1}")
    List<Post> findLatestPosts(int limit);
} 