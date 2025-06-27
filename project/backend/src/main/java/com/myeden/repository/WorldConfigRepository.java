package com.myeden.repository;

import com.myeden.entity.WorldConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 世界设定数据访问层
 * 
 * 功能说明：
 * - 提供世界设定相关的数据库操作
 * - 支持按世界ID、名称等字段查询
 * - 提供世界设定统计和状态查询
 * - 支持世界设定数据分页和排序
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface WorldConfigRepository extends MongoRepository<WorldConfig, String> {
    
    /**
     * 根据世界ID查找世界设定
     * @param worldId 世界ID
     * @return 世界设定信息
     */
    Optional<WorldConfig> findByWorldId(String worldId);
    
    /**
     * 根据世界名称查找世界设定
     * @param name 世界名称
     * @return 世界设定信息
     */
    Optional<WorldConfig> findByName(String name);
    
    /**
     * 检查世界ID是否存在
     * @param worldId 世界ID
     * @return 是否存在
     */
    boolean existsByWorldId(String worldId);
    
    /**
     * 检查世界名称是否存在
     * @param name 世界名称
     * @return 是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 根据是否激活查找世界设定
     * @param isActive 是否激活
     * @return 世界设定列表
     */
    List<WorldConfig> findByIsActive(Boolean isActive);
    
    /**
     * 查找激活的世界设定
     * @return 世界设定列表
     */
    List<WorldConfig> findByIsActiveTrue();
    
    /**
     * 查找未激活的世界设定
     * @return 世界设定列表
     */
    List<WorldConfig> findByIsActiveFalse();
    
    /**
     * 查找最近创建的世界设定
     * @param limit 限制数量
     * @return 世界设定列表
     */
    @Query(value = "{}", sort = "{'createdAt': -1}")
    List<WorldConfig> findRecentWorldConfigs(int limit);
    
    /**
     * 根据名称模糊查询世界设定
     * @param name 名称关键词
     * @return 世界设定列表
     */
    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    List<WorldConfig> findByNameContaining(String name);
    
    /**
     * 根据描述模糊查询世界设定
     * @param description 描述关键词
     * @return 世界设定列表
     */
    @Query("{'description': {$regex: ?0, $options: 'i'}}")
    List<WorldConfig> findByDescriptionContaining(String description);
    
    /**
     * 根据世界背景prompt模糊查询世界设定
     * @param backgroundPrompt 世界背景prompt关键词
     * @return 世界设定列表
     */
    @Query("{'backgroundPrompt': {$regex: ?0, $options: 'i'}}")
    List<WorldConfig> findByBackgroundPromptContaining(String backgroundPrompt);
    
    /**
     * 根据世界观prompt模糊查询世界设定
     * @param worldviewPrompt 世界观prompt关键词
     * @return 世界设定列表
     */
    @Query("{'worldviewPrompt': {$regex: ?0, $options: 'i'}}")
    List<WorldConfig> findByWorldviewPromptContaining(String worldviewPrompt);
    
    /**
     * 查找包含指定机器人的世界设定
     * @param robotId 机器人ID
     * @return 世界设定列表
     */
    @Query("{'robotIds': ?0}")
    List<WorldConfig> findByRobotIdsContaining(String robotId);
    
    /**
     * 查找包含多个机器人的世界设定
     * @param robotIds 机器人ID列表
     * @return 世界设定列表
     */
    @Query("{'robotIds': {$in: ?0}}")
    List<WorldConfig> findByRobotIdsIn(List<String> robotIds);
    
    /**
     * 查找机器人数量大于等于指定值的世界设定
     * @param robotCount 机器人数量
     * @return 世界设定列表
     */
    @Query("{'robotIds': {$size: {$gte: ?0}}}")
    List<WorldConfig> findByRobotCountGreaterThanEqual(Integer robotCount);
    
    /**
     * 查找机器人数量等于指定值的世界设定
     * @param robotCount 机器人数量
     * @return 世界设定列表
     */
    @Query("{'robotIds': {$size: ?0}}")
    List<WorldConfig> findByRobotCount(Integer robotCount);
    
    /**
     * 查找今日创建的世界设定
     * @return 世界设定列表
     */
    @Query("{'createdAt': {$gte: ?0}}")
    List<WorldConfig> findTodayWorldConfigs(java.time.LocalDateTime startOfDay);
    
    /**
     * 查找世界设定数量统计
     * @return 世界设定总数
     */
    long count();
    
    /**
     * 根据是否激活统计世界设定数量
     * @param isActive 是否激活
     * @return 世界设定数量
     */
    long countByIsActive(Boolean isActive);
    
    /**
     * 统计激活的世界设定数量
     * @return 世界设定数量
     */
    long countByIsActiveTrue();
    
    /**
     * 统计未激活的世界设定数量
     * @return 世界设定数量
     */
    long countByIsActiveFalse();
    
    /**
     * 统计今日创建的世界设定数量
     * @return 世界设定数量
     */
    @Query(value = "{'createdAt': {$gte: ?0}}", count = true)
    long countTodayWorldConfigs(java.time.LocalDateTime startOfDay);
    
    /**
     * 统计包含指定机器人的世界设定数量
     * @param robotId 机器人ID
     * @return 世界设定数量
     */
    @Query(value = "{'robotIds': ?0}", count = true)
    long countByRobotIdsContaining(String robotId);
    
    /**
     * 统计机器人数量大于等于指定值的世界设定数量
     * @param robotCount 机器人数量
     * @return 世界设定数量
     */
    @Query(value = "{'robotIds': {$size: {$gte: ?0}}}", count = true)
    long countByRobotCountGreaterThanEqual(Integer robotCount);
    
    /**
     * 统计机器人数量等于指定值的世界设定数量
     * @param robotCount 机器人数量
     * @return 世界设定数量
     */
    @Query(value = "{'robotIds': {$size: ?0}}", count = true)
    long countByRobotCount(Integer robotCount);
} 