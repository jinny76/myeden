package com.myeden.repository;

import com.myeden.entity.PostLike;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 动态点赞数据访问层
 * 
 * 功能说明：
 * - 提供动态点赞的增删改查操作
 * - 支持点赞状态查询和统计
 * - 防止重复点赞
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface PostLikeRepository extends MongoRepository<PostLike, String> {
    
    /**
     * 根据动态ID和用户ID查询点赞记录
     * 
     * @param postId 动态ID
     * @param userId 用户ID
     * @return 点赞记录
     */
    Optional<PostLike> findByPostIdAndUserId(String postId, String userId);
    
    /**
     * 根据动态ID查询所有点赞记录
     * 
     * @param postId 动态ID
     * @return 点赞记录列表
     */
    List<PostLike> findByPostId(String postId);
    
    /**
     * 根据动态ID查询点赞记录（带权限过滤）
     * 只返回当前用户有权限查看的点赞记录
     * 
     * @param postId 动态ID
     * @param currentUserId 当前用户ID（可选，用于权限控制）
     * @param connectedRobotIds 已连接机器人ID列表（可选，用于机器人链接过滤）
     * @return 点赞记录列表
     */
    @Query(value = "{'postId': ?0, $or: [{'userId': ?1}, {'userId': {$in: ?2}}]}")
    List<PostLike> findByPostIdWithPermissionFilter(String postId, String currentUserId, List<String> connectedRobotIds);
    
    /**
     * 根据动态ID统计点赞数量
     * 
     * @param postId 动态ID
     * @return 点赞数量
     */
    long countByPostId(String postId);
    
    /**
     * 根据用户ID查询用户的所有点赞记录
     * 
     * @param userId 用户ID
     * @return 点赞记录列表
     */
    List<PostLike> findByUserId(String userId);
    
    /**
     * 根据动态ID和用户ID删除点赞记录
     * 
     * @param postId 动态ID
     * @param userId 用户ID
     */
    void deleteByPostIdAndUserId(String postId, String userId);
    
    /**
     * 根据动态ID删除所有点赞记录
     * 
     * @param postId 动态ID
     */
    void deleteByPostId(String postId);
    
    /**
     * 根据用户ID删除用户的所有点赞记录
     * 
     * @param userId 用户ID
     */
    void deleteByUserId(String userId);
    
    /**
     * 检查用户是否已点赞指定动态
     * 
     * @param postId 动态ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    @Query("SELECT COUNT(p) > 0 FROM PostLike p WHERE p.postId = ?1 AND p.userId = ?2")
    boolean existsByPostIdAndUserId(String postId, String userId);
} 