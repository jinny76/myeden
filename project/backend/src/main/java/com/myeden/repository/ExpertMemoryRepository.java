package com.myeden.repository;

import com.myeden.entity.ExpertMemory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 专家记忆数据访问层
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface ExpertMemoryRepository extends MongoRepository<ExpertMemory, String> {
    
    /**
     * 根据用户ID、机器人ID和主题ID查找所有有效记忆
     * 按优先级降序、更新时间降序排序
     */
    @Query("{'userId': ?0, 'robotId': ?1, 'themeId': ?2, 'isActive': true}")
    List<ExpertMemory> findByUserIdAndRobotIdAndThemeIdAndIsActiveTrue(
        String userId, String robotId, String themeId);
    
    /**
     * 根据用户ID、机器人ID、主题ID和字段名查找记忆
     */
    Optional<ExpertMemory> findByUserIdAndRobotIdAndThemeIdAndFieldNameAndIsActiveTrue(
        String userId, String robotId, String themeId, String fieldName);
    
    /**
     * 根据用户ID、机器人ID、主题ID和记忆类型查找记忆
     */
    List<ExpertMemory> findByUserIdAndRobotIdAndThemeIdAndMemoryTypeAndIsActiveTrueOrderByPriorityDescUpdatedAtDesc(
        String userId, String robotId, String themeId, String memoryType);
    
    /**
     * 根据用户ID和机器人ID查找所有主题的记忆
     */
    List<ExpertMemory> findByUserIdAndRobotIdAndIsActiveTrueOrderByUpdatedAtDesc(
        String userId, String robotId);
    
    /**
     * 根据用户ID查找所有记忆
     */
    List<ExpertMemory> findByUserIdAndIsActiveTrueOrderByUpdatedAtDesc(String userId);
    
    /**
     * 统计用户在特定主题下的记忆数量
     */
    long countByUserIdAndRobotIdAndThemeIdAndIsActiveTrue(
        String userId, String robotId, String themeId);
    
    /**
     * 查找指定时间之前创建的记忆(用于清理)
     */
    List<ExpertMemory> findByCreatedAtBeforeAndIsActiveTrue(LocalDateTime before);
    
    /**
     * 根据记忆类型统计数量
     */
    long countByMemoryTypeAndIsActiveTrue(String memoryType);
    
    /**
     * 查找用户在特定主题下的基础信息记忆
     */
    @Query("{'userId': ?0, 'robotId': ?1, 'themeId': ?2, 'memoryType': 'basic_info', 'isActive': true}")
    List<ExpertMemory> findBasicInfoMemories(String userId, String robotId, String themeId);
    
    /**
     * 查找用户在特定主题下的会话摘要记忆
     */
    @Query("{'userId': ?0, 'robotId': ?1, 'themeId': ?2, 'memoryType': 'session_summary', 'isActive': true}")
    List<ExpertMemory> findSessionSummaryMemories(String userId, String robotId, String themeId);
    
    /**
     * 查找用户在特定主题下的关键事件记忆
     */
    @Query("{'userId': ?0, 'robotId': ?1, 'themeId': ?2, 'memoryType': 'key_event', 'isActive': true}")
    List<ExpertMemory> findKeyEventMemories(String userId, String robotId, String themeId);
    
    /**
     * 根据优先级查找高优先级记忆
     */
    @Query("{'userId': ?0, 'robotId': ?1, 'themeId': ?2, 'priority': {$gte: ?3}, 'isActive': true}")
    List<ExpertMemory> findHighPriorityMemories(String userId, String robotId, String themeId, int minPriority);
    
    /**
     * 删除指定用户的所有记忆
     */
    void deleteByUserId(String userId);
    
    /**
     * 删除指定机器人的所有记忆
     */
    void deleteByRobotId(String robotId);

}