package com.myeden.repository;

import com.myeden.entity.Robot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 机器人数据访问层
 * 
 * 功能说明：
 * - 提供机器人相关的数据库操作
 * - 支持按机器人ID、名称等字段查询
 * - 提供机器人统计和状态查询
 * - 支持机器人数据分页和排序
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface RobotRepository extends MongoRepository<Robot, String> {
    
    /**
     * 根据机器人ID查找机器人
     * @param robotId 机器人ID
     * @return 机器人信息
     */
    Optional<Robot> findByRobotId(String robotId);
    
    /**
     * 根据机器人名称查找机器人
     * @param name 机器人名称
     * @return 机器人信息
     */
    Optional<Robot> findByName(String name);
    
    /**
     * 检查机器人ID是否存在
     * @param robotId 机器人ID
     * @return 是否存在
     */
    boolean existsByRobotId(String robotId);
    
    /**
     * 检查机器人名称是否存在
     * @param name 机器人名称
     * @return 是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 根据性别查找机器人
     * @param gender 性别
     * @return 机器人列表
     */
    List<Robot> findByGender(String gender);
    
    /**
     * 根据职业查找机器人
     * @param occupation 职业
     * @return 机器人列表
     */
    List<Robot> findByOccupation(String occupation);
    
    /**
     * 根据MBTI查找机器人
     * @param mbti MBTI类型
     * @return 机器人列表
     */
    List<Robot> findByMbti(String mbti);
    
    /**
     * 根据是否激活查找机器人
     * @param isActive 是否激活
     * @return 机器人列表
     */
    List<Robot> findByIsActive(Boolean isActive);
    
    /**
     * 查找激活的机器人
     * @return 机器人列表
     */
    List<Robot> findByIsActiveTrue();
    
    /**
     * 查找未激活的机器人
     * @return 机器人列表
     */
    List<Robot> findByIsActiveFalse();
    
    
    /**
     * 查找最近创建的机器人
     * @param limit 限制数量
     * @return 机器人列表
     */
    @Query(value = "{}", sort = "{'createdAt': -1}")
    List<Robot> findRecentRobots(int limit);
    
    /**
     * 根据名称模糊查询机器人
     * @param name 名称关键词
     * @return 机器人列表
     */
    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    List<Robot> findByNameContaining(String name);
    
    /**
     * 根据介绍模糊查询机器人
     * @param description 介绍关键词
     * @return 机器人列表
     */
    @Query("{'description': {$regex: ?0, $options: 'i'}}")
    List<Robot> findByDescriptionContaining(String description);
    
    /**
     * 根据性格模糊查询机器人
     * @param personality 性格关键词
     * @return 机器人列表
     */
    @Query("{'personality': {$regex: ?0, $options: 'i'}}")
    List<Robot> findByPersonalityContaining(String personality);
    
    
    
    /**
     * 查找今日创建的机器人
     * @return 机器人列表
     */
    @Query("{'createdAt': {$gte: ?0}}")
    List<Robot> findTodayRobots(java.time.LocalDateTime startOfDay);
    
    /**
     * 查找机器人数量统计
     * @return 机器人总数
     */
    long count();
    
    /**
     * 根据性别统计机器人数量
     * @param gender 性别
     * @return 机器人数量
     */
    long countByGender(String gender);
    
    /**
     * 根据职业统计机器人数量
     * @param occupation 职业
     * @return 机器人数量
     */
    long countByOccupation(String occupation);
    
    /**
     * 根据MBTI统计机器人数量
     * @param mbti MBTI类型
     * @return 机器人数量
     */
    long countByMbti(String mbti);
    
    /**
     * 根据是否激活统计机器人数量
     * @param isActive 是否激活
     * @return 机器人数量
     */
    long countByIsActive(Boolean isActive);
    
    /**
     * 统计激活的机器人数量
     * @return 机器人数量
     */
    long countByIsActiveTrue();
    
    /**
     * 统计未激活的机器人数量
     * @return 机器人数量
     */
    long countByIsActiveFalse();
    
    /**
     * 统计今日创建的机器人数量
     * @return 机器人数量
     */
    @Query(value = "{'createdAt': {$gte: ?0}}", count = true)
    long countTodayRobots(java.time.LocalDateTime startOfDay);
    
    
    /**
     * 根据所有者查找机器人
     * @param owner 所有者ID
     * @return 机器人列表
     */
    List<Robot> findByOwner(String owner);
    
    /**
     * 根据所有者和删除状态查找机器人
     * @param owner 所有者ID
     * @param isDeleted 是否删除
     * @return 机器人列表
     */
    List<Robot> findByOwnerAndIsDeleted(String owner, Boolean isDeleted);
    
    /**
     * 根据所有者统计机器人数量
     * @param owner 所有者ID
     * @return 机器人数量
     */
    long countByOwner(String owner);
    
    /**
     * 根据所有者和删除状态统计机器人数量
     * @param owner 所有者ID
     * @param isDeleted 是否删除
     * @return 机器人数量
     */
    long countByOwnerAndIsDeleted(String owner, Boolean isDeleted);
} 