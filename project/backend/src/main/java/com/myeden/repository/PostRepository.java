package com.myeden.repository;

import com.myeden.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 动态数据访问层
 * 
 * 功能说明：
 * - 提供动态相关的数据库操作
 * - 支持按作者、内容等字段查询
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
     * 根据动态ID查找动态
     * @param postId 动态ID
     * @return 动态信息
     */
    Optional<Post> findByPostId(String postId);
    
    /**
     * 根据作者ID查找动态
     * @param authorId 作者ID
     * @return 动态列表
     */
    List<Post> findByAuthorId(String authorId);
    
    /**
     * 根据作者类型查找动态
     * @param authorType 作者类型
     * @return 动态列表
     */
    List<Post> findByAuthorType(String authorType);
    
    /**
     * 根据作者ID和作者类型查找动态
     * @param authorId 作者ID
     * @param authorType 作者类型
     * @return 动态列表
     */
    List<Post> findByAuthorIdAndAuthorType(String authorId, String authorType);
    
    /**
     * 根据是否删除查找动态
     * @param isDeleted 是否删除
     * @return 动态列表
     */
    List<Post> findByIsDeleted(Boolean isDeleted);
    
    /**
     * 根据作者ID和是否删除查找动态
     * @param authorId 作者ID
     * @param isDeleted 是否删除
     * @return 动态列表
     */
    List<Post> findByAuthorIdAndIsDeleted(String authorId, Boolean isDeleted);
    
    /**
     * 根据作者类型和是否删除查找动态
     * @param authorType 作者类型
     * @param isDeleted 是否删除
     * @return 动态列表
     */
    List<Post> findByAuthorTypeAndIsDeleted(String authorType, Boolean isDeleted);
    
    /**
     * 查找最近发布的动态
     * @param limit 限制数量
     * @return 动态列表
     */
    @Query(value = "{}", sort = "{'createdAt': -1}")
    List<Post> findRecentPosts(int limit);
    
    /**
     * 查找指定作者的最近动态
     * @param authorId 作者ID
     * @param limit 限制数量
     * @return 动态列表
     */
    @Query(value = "{'authorId': ?0}", sort = "{'createdAt': -1}")
    List<Post> findRecentPostsByAuthor(String authorId, int limit);
    
    /**
     * 查找指定类型的最近动态
     * @param authorType 作者类型
     * @param limit 限制数量
     * @return 动态列表
     */
    @Query(value = "{'authorType': ?0}", sort = "{'createdAt': -1}")
    List<Post> findRecentPostsByType(String authorType, int limit);
    
    /**
     * 根据内容模糊查询动态
     * @param content 内容关键词
     * @return 动态列表
     */
    @Query("{'content': {$regex: ?0, $options: 'i'}}")
    List<Post> findByContentContaining(String content);
    
    /**
     * 查找有图片的动态
     * @return 动态列表
     */
    @Query("{'images': {$exists: true, $ne: []}}")
    List<Post> findPostsWithImages();
    
    /**
     * 查找没有图片的动态
     * @return 动态列表
     */
    @Query("{$or: [{'images': {$exists: false}}, {'images': []}]}")
    List<Post> findPostsWithoutImages();
    
    /**
     * 查找点赞数大于等于指定值的动态
     * @param likeCount 点赞数
     * @return 动态列表
     */
    @Query("{'likeCount': {$gte: ?0}}")
    List<Post> findByLikeCountGreaterThanEqual(Integer likeCount);
    
    /**
     * 查找评论数大于等于指定值的动态
     * @param commentCount 评论数
     * @return 动态列表
     */
    @Query("{'commentCount': {$gte: ?0}}")
    List<Post> findByCommentCountGreaterThanEqual(Integer commentCount);
    
    /**
     * 查找热门动态（按点赞数排序）
     * @param limit 限制数量
     * @return 动态列表
     */
    @Query(value = "{}", sort = "{'likeCount': -1}")
    List<Post> findPopularPosts(int limit);
    
    /**
     * 查找热门动态（按评论数排序）
     * @param limit 限制数量
     * @return 动态列表
     */
    @Query(value = "{}", sort = "{'commentCount': -1}")
    List<Post> findMostCommentedPosts(int limit);
    
    /**
     * 查找今日发布的动态
     * @return 动态列表
     */
    @Query("{'createdAt': {$gte: ?0}}")
    List<Post> findTodayPosts(java.time.LocalDateTime startOfDay);
    
    /**
     * 查找指定作者今日发布的动态
     * @param authorId 作者ID
     * @return 动态列表
     */
    @Query("{'authorId': ?0, 'createdAt': {$gte: ?1}}")
    List<Post> findTodayPostsByAuthor(String authorId, java.time.LocalDateTime startOfDay);
    
    /**
     * 查找指定类型今日发布的动态
     * @param authorType 作者类型
     * @return 动态列表
     */
    @Query("{'authorType': ?0, 'createdAt': {$gte: ?1}}")
    List<Post> findTodayPostsByType(String authorType, java.time.LocalDateTime startOfDay);
    
    /**
     * 查找动态数量统计
     * @return 动态总数
     */
    long count();
    
    /**
     * 根据作者ID统计动态数量
     * @param authorId 作者ID
     * @return 动态数量
     */
    long countByAuthorId(String authorId);
    
    /**
     * 根据作者类型统计动态数量
     * @param authorType 作者类型
     * @return 动态数量
     */
    long countByAuthorType(String authorType);
    
    /**
     * 根据是否删除统计动态数量
     * @param isDeleted 是否删除
     * @return 动态数量
     */
    long countByIsDeleted(Boolean isDeleted);
    
    /**
     * 根据作者ID和是否删除统计动态数量
     * @param authorId 作者ID
     * @param isDeleted 是否删除
     * @return 动态数量
     */
    long countByAuthorIdAndIsDeleted(String authorId, Boolean isDeleted);
    
    /**
     * 根据作者类型和是否删除统计动态数量
     * @param authorType 作者类型
     * @param isDeleted 是否删除
     * @return 动态数量
     */
    long countByAuthorTypeAndIsDeleted(String authorType, Boolean isDeleted);
    
    /**
     * 统计今日发布的动态数量
     * @return 动态数量
     */
    @Query(value = "{'createdAt': {$gte: ?0}}", count = true)
    long countTodayPosts(java.time.LocalDateTime startOfDay);
    
    /**
     * 统计指定作者今日发布的动态数量
     * @param authorId 作者ID
     * @return 动态数量
     */
    @Query(value = "{'authorId': ?0, 'createdAt': {$gte: ?1}}", count = true)
    long countTodayPostsByAuthor(String authorId, java.time.LocalDateTime startOfDay);
    
    /**
     * 统计指定类型今日发布的动态数量
     * @param authorType 作者类型
     * @return 动态数量
     */
    @Query(value = "{'authorType': ?0, 'createdAt': {$gte: ?1}}", count = true)
    long countTodayPostsByType(String authorType, java.time.LocalDateTime startOfDay);
    
    /**
     * 根据动态ID和是否删除查找动态
     * @param postId 动态ID
     * @param isDeleted 是否删除
     * @return 动态信息
     */
    Optional<Post> findByPostIdAndIsDeleted(String postId, Boolean isDeleted);
    
    /**
     * 根据作者ID和是否删除分页查找动态
     * @param authorId 作者ID
     * @param isDeleted 是否删除
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    Page<Post> findByAuthorIdAndIsDeleted(String authorId, Boolean isDeleted, Pageable pageable);
    
    /**
     * 根据作者类型和是否删除分页查找动态
     * @param authorType 作者类型
     * @param isDeleted 是否删除
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    Page<Post> findByAuthorTypeAndIsDeleted(String authorType, Boolean isDeleted, Pageable pageable);
    
    /**
     * 根据是否删除分页查找动态
     * @param isDeleted 是否删除
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    Page<Post> findByIsDeleted(Boolean isDeleted, Pageable pageable);
    
    /**
     * 根据动态ID和未删除查找动态
     * @param postId 动态ID
     * @return 动态信息
     */
    Optional<Post> findByPostIdAndIsDeletedFalse(String postId);
    
    /**
     * 根据作者ID和未删除分页查找动态
     * @param authorId 作者ID
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    Page<Post> findByAuthorIdAndIsDeletedFalse(String authorId, Pageable pageable);
    
    /**
     * 根据作者类型和未删除分页查找动态
     * @param authorType 作者类型
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    Page<Post> findByAuthorTypeAndIsDeletedFalse(String authorType, Pageable pageable);
    
    /**
     * 根据未删除分页查找动态
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    Page<Post> findByIsDeletedFalse(Pageable pageable);
} 