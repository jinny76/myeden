package com.myeden.repository;

import com.myeden.entity.UserRobotLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户机器人链接数据访问接口
 * 
 * 功能说明：
 * - 提供用户机器人链接的CRUD操作
 * - 支持按用户ID、机器人ID查询链接
 * - 提供链接状态和强度查询功能
 * - 支持分页查询和排序
 * 
 * @author MyEden Team
 * @version 1.0.1
 * @since 2025-01-27
 */
@Repository
public interface UserRobotLinkRepository extends MongoRepository<UserRobotLink, String> {
    
    /**
     * 根据用户ID查找所有链接
     * 
     * @param userId 用户ID
     * @return 链接列表
     */
    List<UserRobotLink> findByUserId(String userId);
    
    /**
     * 根据用户ID查找激活的链接
     * 
     * @param userId 用户ID
     * @return 激活的链接列表
     */
    List<UserRobotLink> findByUserIdAndStatus(String userId, String status);
    
    /**
     * 根据机器人ID查找所有链接
     * 
     * @param robotId 机器人ID
     * @return 链接列表
     */
    List<UserRobotLink> findByRobotId(String robotId);
    
    /**
     * 根据机器人ID查找激活的链接
     * 
     * @param robotId 机器人ID
     * @return 激活的链接列表
     */
    List<UserRobotLink> findByRobotIdAndStatus(String robotId, String status);
    
    /**
     * 根据用户ID和机器人ID查找链接
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @return 链接对象
     */
    Optional<UserRobotLink> findByUserIdAndRobotId(String userId, String robotId);
    
    /**
     * 检查用户和机器人之间是否存在链接
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @return 是否存在链接
     */
    boolean existsByUserIdAndRobotId(String userId, String robotId);
    
    /**
     * 检查用户和机器人之间是否存在激活的链接
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @return 是否存在激活的链接
     */
    boolean existsByUserIdAndRobotIdAndStatus(String userId, String robotId, String status);
    
    /**
     * 根据用户ID分页查询链接
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<UserRobotLink> findByUserId(String userId, Pageable pageable);
    
    /**
     * 根据用户ID和状态分页查询链接
     * 
     * @param userId 用户ID
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<UserRobotLink> findByUserIdAndStatus(String userId, String status, Pageable pageable);
    
    /**
     * 根据机器人ID分页查询链接
     * 
     * @param robotId 机器人ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<UserRobotLink> findByRobotId(String robotId, Pageable pageable);
    
    /**
     * 根据机器人ID和状态分页查询链接
     * 
     * @param robotId 机器人ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<UserRobotLink> findByRobotIdAndStatus(String robotId, String status, Pageable pageable);
    
    /**
     * 查找最近有互动的链接
     * 
     * @param userId 用户ID
     * @param since 时间点
     * @return 链接列表
     */
    List<UserRobotLink> findByUserIdAndLastInteractionTimeAfter(String userId, LocalDateTime since);
    
    /**
     * 查找互动次数最多的链接
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 链接列表
     */
    @Query("{'userId': ?0}")
    Page<UserRobotLink> findTopByUserIdOrderByInteractionCountDesc(String userId, Pageable pageable);
    
    /**
     * 统计用户的链接数量
     * 
     * @param userId 用户ID
     * @return 链接数量
     */
    long countByUserId(String userId);
    
    /**
     * 统计用户激活的链接数量
     * 
     * @param userId 用户ID
     * @return 激活链接数量
     */
    long countByUserIdAndStatus(String userId, String status);
    
    /**
     * 统计机器人的链接数量
     * 
     * @param robotId 机器人ID
     * @return 链接数量
     */
    long countByRobotId(String robotId);
    
    /**
     * 统计机器人激活的链接数量
     * 
     * @param robotId 机器人ID
     * @return 激活链接数量
     */
    long countByRobotIdAndStatus(String robotId, String status);
    
    /**
     * 删除用户的所有链接
     * 
     * @param userId 用户ID
     */
    void deleteByUserId(String userId);
    
    /**
     * 删除机器人的所有链接
     * 
     * @param robotId 机器人ID
     */
    void deleteByRobotId(String robotId);
    
    /**
     * 删除指定的用户机器人链接
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     */
    void deleteByUserIdAndRobotId(String userId, String robotId);
    
    // 熟悉度相关查询方法
    
    /**
     * 根据用户ID查找有待沟通消息的链接
     * 
     * @param userId 用户ID
     * @return 有待沟通消息的链接列表
     */
    List<UserRobotLink> findByUserIdAndHasPendingMessageTrue(String userId);
    
    /**
     * 根据机器人ID查找有待沟通消息的链接
     * 
     * @param robotId 机器人ID
     * @return 有待沟通消息的链接列表
     */
    List<UserRobotLink> findByRobotIdAndHasPendingMessageTrue(String robotId);
    
    /**
     * 根据用户ID和熟悉度等级查找链接
     * 
     * @param userId 用户ID
     * @param familiarityLevel 熟悉度等级
     * @return 指定等级的链接列表
     */
    List<UserRobotLink> findByUserIdAndFamiliarityLevel(String userId, Integer familiarityLevel);
    
    /**
     * 根据用户ID查找指定熟悉度等级以上的链接
     * 
     * @param userId 用户ID
     * @param minLevel 最小熟悉度等级
     * @return 链接列表
     */
    List<UserRobotLink> findByUserIdAndFamiliarityLevelGreaterThanEqual(String userId, Integer minLevel);
    
    /**
     * 根据用户ID查找指定熟悉度积分以上的链接
     * 
     * @param userId 用户ID
     * @param minScore 最小熟悉度积分
     * @return 链接列表
     */
    List<UserRobotLink> findByUserIdAndFamiliarityScoreGreaterThanEqual(String userId, Integer minScore);
    
    /**
     * 根据用户ID按熟悉度等级降序查询链接（分页）
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("{'userId': ?0}")
    Page<UserRobotLink> findByUserIdOrderByFamiliarityLevelDesc(String userId, Pageable pageable);
    
    /**
     * 根据用户ID按熟悉度积分降序查询链接（分页）
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("{'userId': ?0}")
    Page<UserRobotLink> findByUserIdOrderByFamiliarityScoreDesc(String userId, Pageable pageable);
    
    /**
     * 查找可以主动沟通的机器人链接（好友及以上等级）
     * 
     * @param userId 用户ID
     * @return 可主动沟通的链接列表
     */
    @Query("{'userId': ?0, 'familiarityLevel': {$gte: 3}, 'status': 'active'}")
    List<UserRobotLink> findActiveLinksForProactiveChat(String userId);
    
    /**
     * 世界模块专用：按优先级排序查询用户的机器人链接
     * 优先级：有待沟通消息 > 熟悉度高 > 已链接
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("{'userId': ?0, 'status': 'active'}")
    Page<UserRobotLink> findByUserIdForWorldModule(String userId, Pageable pageable);
    
    /**
     * 统计用户指定熟悉度等级的链接数量
     * 
     * @param userId 用户ID
     * @param familiarityLevel 熟悉度等级
     * @return 链接数量
     */
    long countByUserIdAndFamiliarityLevel(String userId, Integer familiarityLevel);
    
    /**
     * 统计用户指定熟悉度等级以上的链接数量
     * 
     * @param userId 用户ID
     * @param minLevel 最小熟悉度等级
     * @return 链接数量
     */
    long countByUserIdAndFamiliarityLevelGreaterThanEqual(String userId, Integer minLevel);
} 