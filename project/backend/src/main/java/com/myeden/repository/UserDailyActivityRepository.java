package com.myeden.repository;

import com.myeden.entity.UserDailyActivity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 用户每日活动统计数据访问层
 * 
 * 功能说明：
 * - 提供用户每日活动统计数据的CRUD操作
 * - 支持按日期范围查询活动数据
 * - 支持活动数据的聚合统计
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-01-27
 */
@Repository
public interface UserDailyActivityRepository extends MongoRepository<UserDailyActivity, String> {
    
    /**
     * 根据用户ID和日期查找活动记录
     * 
     * @param userId 用户ID
     * @param date 日期
     * @return 活动记录
     */
    Optional<UserDailyActivity> findByUserIdAndDate(String userId, LocalDate date);
    
    /**
     * 根据用户ID和日期范围查找活动记录
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 活动记录列表
     */
    @Query("{ 'userId': ?0, 'date': { $gte: ?1, $lte: ?2 } }")
    List<UserDailyActivity> findByUserIdAndDateBetweenOrderByDateAsc(String userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据用户ID和日期范围查找活动记录（降序）
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 活动记录列表
     */
    List<UserDailyActivity> findByUserIdAndDateBetweenOrderByDateDesc(String userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据用户ID查找最近的活动记录
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 活动记录列表
     */
    @Query("{ 'userId': ?0 }")
    List<UserDailyActivity> findRecentActivitiesByUserId(String userId, int limit);
    
    /**
     * 根据用户ID统计总活动次数
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总活动次数
     */
    @Query(value = "{ 'userId': ?0, 'date': { $gte: ?1, $lte: ?2 } }", 
           fields = "{ 'totalCount': 1 }")
    List<UserDailyActivity> findTotalCountByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据用户ID统计各类活动总数
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 活动记录列表
     */
    @Query("{ 'userId': ?0, 'date': { $gte: ?1, $lte: ?2 } }")
    List<UserDailyActivity> findActivityStatsByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 检查用户在指定日期是否有活动记录
     * 
     * @param userId 用户ID
     * @param date 日期
     * @return 是否存在记录
     */
    boolean existsByUserIdAndDate(String userId, LocalDate date);
    
    /**
     * 删除指定日期之前的活动记录（用于数据清理）
     * 
     * @param cutoffDate 截止日期
     * @return 删除的记录数
     */
    long deleteByDateBefore(LocalDate cutoffDate);
    
    /**
     * 获取用户最活跃的日期
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 活动记录列表
     */
    @Query(value = "{ 'userId': ?0, 'totalCount': { $gt: 0 } }", 
           sort = "{ 'totalCount': -1 }")
    List<UserDailyActivity> findMostActiveByUserId(String userId, int limit);
}