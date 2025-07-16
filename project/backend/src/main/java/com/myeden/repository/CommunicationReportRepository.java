package com.myeden.repository;

import com.myeden.entity.CommunicationReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * 沟通评估报告Repository接口
 * 
 * 功能说明：
 * - 提供沟通评估报告的数据访问方法
 * - 支持按用户查询、按对话查询等功能
 * - 用于避免重复评估和统计分析
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-07-15
 */
@Repository
public interface CommunicationReportRepository extends MongoRepository<CommunicationReport, String> {
    
    /**
     * 根据用户ID查询沟通报告，按创建时间降序排列
     * 
     * @param userId 用户ID
     * @return 沟通报告列表
     */
    List<CommunicationReport> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * 根据用户ID和对话ID查询沟通报告
     * 
     * @param userId 用户ID
     * @param conversationId 对话ID
     * @return 沟通报告
     */
    CommunicationReport findByUserIdAndConversationId(String userId, String conversationId);
    
    /**
     * 检查是否存在指定对话的评估报告
     * 
     * @param conversationId 对话ID
     * @return 是否存在
     */
    boolean existsByConversationId(String conversationId);
    
    /**
     * 获取所有已评估过的对话ID
     * 
     * @return 已评估的对话ID集合
     */
    @Query(value = "{}", fields = "{ 'conversationId' : 1 }")
    List<CommunicationReport> findAllConversationIds();
    
    /**
     * 获取所有已评估过的对话ID（默认方法）
     * 
     * @return 已评估的对话ID集合
     */
    default Set<String> findAllEvaluatedConversationIds() {
        return findAllConversationIds().stream()
            .map(CommunicationReport::getConversationId)
            .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * 根据评分范围查询沟通报告
     * 
     * @param userId 用户ID
     * @param minScore 最低分
     * @param maxScore 最高分
     * @return 沟通报告列表
     */
    List<CommunicationReport> findByUserIdAndScoreBetweenOrderByCreatedAtDesc(String userId, int minScore, int maxScore);
    
    /**
     * 获取用户的评估报告数量
     * 
     * @param userId 用户ID
     * @return 报告数量
     */
    long countByUserId(String userId);
    
    /**
     * 获取用户的平均分数
     * 
     * @param userId 用户ID
     * @return 平均分数
     */
    @Query("{ 'userId': ?0 }")
    List<CommunicationReport> findByUserIdForAverage(String userId);
    
    /**
     * 获取用户在指定日期范围内已评估过的对话ID
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 评估报告列表
     */
    @Query(
      value = "{ $and: [ { 'userId': ?0 }, { 'createdAt': { $gte: ?1, $lte: ?2 } } ] }",
      fields = "{ 'conversationId': 1 }"
    )
    List<CommunicationReport> findConversationIdsByUserIdAndDateRange(String userId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    /**
     * 获取用户在指定日期范围内已评估过的对话ID集合（默认方法）
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 已评估的对话ID集合
     */
    default Set<String> findEvaluatedConversationIdsByUserIdAndDateRange(String userId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return findConversationIdsByUserIdAndDateRange(userId, startDate, endDate).stream()
            .map(CommunicationReport::getConversationId)
            .filter(id -> id != null && !id.trim().isEmpty())
            .collect(java.util.stream.Collectors.toSet());
    }
}