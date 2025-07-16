package com.myeden.service;

import com.myeden.service.dto.ActivityData;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 活动数据服务接口
 * 
 * 功能说明：
 * - 聚合用户的各类活动数据
 * - 提供活动统计和分析功能
 * - 支持贡献图数据生成
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-01-27
 */
public interface ActivityService {
    
    /**
     * 获取用户活动数据
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户活动数据列表
     */
    List<ActivityData> getUserActivityData(String userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户活动统计
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 活动统计数据
     */
    Map<String, Object> getUserActivityStats(String userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户聊天活动数据
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 聊天活动数据
     */
    Map<LocalDate, Integer> getUserChatActivity(String userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户发帖活动数据
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 发帖活动数据
     */
    Map<LocalDate, Integer> getUserPostActivity(String userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户评论活动数据
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 评论活动数据
     */
    Map<LocalDate, Integer> getUserCommentActivity(String userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户点赞活动数据
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 点赞活动数据
     */
    Map<LocalDate, Integer> getUserLikeActivity(String userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 删除指定日期之前的活动数据
     * 
     * @param cutoffDate 截止日期
     * @return 删除的记录数
     */
    long deleteActivityDataBefore(LocalDate cutoffDate);
}