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
     * 查找指定强度以上的链接
     * 
     * @param userId 用户ID
     * @param minStrength 最小强度
     * @return 链接列表
     */
    List<UserRobotLink> findByUserIdAndStrengthGreaterThanEqual(String userId, Integer minStrength);
    
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
     * 查找链接强度最高的链接
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 链接列表
     */
    @Query("{'userId': ?0}")
    Page<UserRobotLink> findTopByUserIdOrderByStrengthDesc(String userId, Pageable pageable);
    
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
} 