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
     * 根据作者ID和是否删除查找动态
     * @param authorId 作者ID
     * @param isDeleted 是否删除
     * @return 动态列表
     */
    List<Post> findByAuthorIdAndIsDeleted(String authorId, Boolean isDeleted);


    /**
     * 查找指定作者的最近动态
     * @param authorId 作者ID
     * @param limit 限制数量
     * @param currentUserId 当前用户ID（可选，用于隐私控制）
     * @param connectedRobotIds 已连接机器人ID列表（可选，用于机器人链接过滤）
     * @return 动态列表
     */
    @Query(value = "{'authorId': ?0, 'isDeleted': false, $or: [{'visibility': 'public'}, {'authorId': ?1}, {'authorId': {$in: ?2}}]}", sort = "{'createdAt': -1}")
    List<Post> findRecentPostsByAuthor(String authorId, int limit, String currentUserId, List<String> connectedRobotIds);

    /**
     * 根据关键字搜索动态（内容和作者ID）
     * @param keyword 搜索关键字
     * @param currentUserId 当前用户ID（可选，用于隐私控制）
     * @param connectedRobotIds 已连接机器人ID列表（可选，用于机器人链接过滤）
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{$and: [{$or: [{'content': {$regex: ?0, $options: 'i'}}, {'authorId': {$regex: ?0, $options: 'i'}}]}, {'isDeleted': false}, {$or: [{'visibility': 'public'}, {'authorId': ?1}, {'authorId': {$in: ?2}}]}]}")
    Page<Post> findByKeywordAndIsDeletedFalse(String keyword, String currentUserId, List<String> connectedRobotIds, Pageable pageable);

    /**
     * 根据关键字和作者类型搜索动态（内容和作者ID）
     * @param keyword 搜索关键字
     * @param authorType 作者类型
     * @param currentUserId 当前用户ID（可选，用于隐私控制）
     * @param connectedRobotIds 已连接机器人ID列表（可选，用于机器人链接过滤）
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{$and: [{$or: [{'content': {$regex: ?0, $options: 'i'}}, {'authorId': {$regex: ?0, $options: 'i'}}]}, {'authorType': ?1}, {'isDeleted': false}, {$or: [{'visibility': 'public'}, {'authorId': ?2}, {'authorId': {$in: ?3}}]}]}")
    Page<Post> findByKeywordAndAuthorTypeAndIsDeletedFalse(String keyword, String authorType, String currentUserId, List<String> connectedRobotIds, Pageable pageable);
    
    /**
     * 根据内容关键字搜索动态（分页）
     * @param keyword 内容关键字
     * @param currentUserId 当前用户ID（可选，用于隐私控制）
     * @param connectedRobotIds 已连接机器人ID列表（可选，用于机器人链接过滤）
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{$and: [{'content': {$regex: ?0, $options: 'i'}}, {'isDeleted': false}, {$or: [{'visibility': 'public'}, {'authorId': ?1}, {'authorId': {$in: ?2}}]}]}")
    Page<Post> findByContentKeywordAndIsDeletedFalse(String keyword, String currentUserId, List<String> connectedRobotIds, Pageable pageable);
    
    /**
     * 根据作者关键字搜索动态（分页）
     * @param keyword 作者关键字
     * @param currentUserId 当前用户ID（可选，用于隐私控制）
     * @param connectedRobotIds 已连接机器人ID列表（可选，用于机器人链接过滤）
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query("{$and: [{'authorId': {$regex: ?0, $options: 'i'}}, {'isDeleted': false}, {$or: [{'visibility': 'public'}, {'authorId': ?1}, {'authorId': {$in: ?2}}]}]}")
    Page<Post> findByAuthorKeywordAndIsDeletedFalse(String keyword, String currentUserId, List<String> connectedRobotIds, Pageable pageable);

    /**
     * 根据作者ID和是否删除统计动态数量
     * @param authorId 作者ID
     * @param isDeleted 是否删除
     * @return 动态数量
     */
    long countByAuthorIdAndIsDeleted(String authorId, Boolean isDeleted);
    
    /**
     * 统计指定作者今日发布的动态数量
     * @param authorId 作者ID
     * @return 动态数量
     */
    @Query(value = "{'authorId': ?0, 'createdAt': {$gte: ?1}}", count = true)
    long countTodayPostsByAuthor(String authorId, java.time.LocalDateTime startOfDay);
    
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
     * @param currentUserId 当前用户ID（可选，用于隐私控制）
     * @param connectedRobotIds 已连接机器人ID列表（可选，用于机器人链接过滤）
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query(value = "{'authorId': ?0, 'isDeleted': false, $or: [{'visibility': 'public'}, {'authorId': ?1}, {'authorId': {$in: ?2}}]}")
    Page<Post> findByAuthorIdAndIsDeletedFalse(String authorId, String currentUserId, List<String> connectedRobotIds, Pageable pageable);
    
    /**
     * 根据作者类型和未删除分页查找动态
     * @param authorType 作者类型
     * @param currentUserId 当前用户ID（可选，用于隐私控制）
     * @param connectedRobotIds 已连接机器人ID列表（可选，用于机器人链接过滤）
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query(value = "{'authorType': ?0, 'isDeleted': false, $or: [{'visibility': 'public'}, {'authorId': ?1}, {'authorId': {$in: ?2}}]}")
    Page<Post> findByAuthorTypeAndIsDeletedFalse(String authorType, String currentUserId, List<String> connectedRobotIds, Pageable pageable);
    
    /**
     * 根据未删除分页查找动态
     * @param currentUserId 当前用户ID（可选，用于隐私控制）
     * @param connectedRobotIds 已连接机器人ID列表（可选，用于机器人链接过滤）
     * @param pageable 分页参数
     * @return 动态分页结果
     */
    @Query(value = "{'isDeleted': false, $or: [{'visibility': 'public'}, {'authorId': ?0}, {'authorId': {$in: ?1}}]}")
    Page<Post> findByIsDeletedFalse(String currentUserId, List<String> connectedRobotIds, Pageable pageable);
    
    /**
     * 根据创建时间查找指定时间之后的未删除动态，按创建时间倒序排列
     * @param createdAt 创建时间
     * @param currentUserId 当前用户ID（可选，用于隐私控制）
     * @param connectedRobotIds 已连接机器人ID列表（可选，用于机器人链接过滤）
     * @return 动态列表
     */
    @Query("{'createdAt': {$gte: ?0}, 'isDeleted': false, $or: [{'visibility': 'public'}, {'authorId': ?1}, {'authorId': {$in: ?2}}]}")
    List<Post> findByCreatedAtAfterAndIsDeletedFalseOrderByCreatedAtDesc(LocalDateTime createdAt, String currentUserId, List<String> connectedRobotIds);
    

} 