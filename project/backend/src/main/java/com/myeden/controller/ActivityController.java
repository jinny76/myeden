package com.myeden.controller;

import com.myeden.service.ActivityService;
import com.myeden.service.dto.ActivityData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 活动数据控制器
 * 
 * 功能说明：
 * - 提供用户活动数据的API接口
 * - 支持获取用户在指定时间范围内的活动统计
 * - 用于生成类似GitHub贡献图的活动可视化
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-01-27
 */
@RestController
@RequestMapping("/api/v1/activity")
public class ActivityController {
    
    private static final Logger logger = LoggerFactory.getLogger(ActivityController.class);
    
    @Autowired
    private ActivityService activityService;
    
    /**
     * 获取用户活动数据
     * 
     * @param userId 用户ID
     * @param startDate 开始日期（可选，默认为一年前）
     * @param endDate 结束日期（可选，默认为今天）
     * @return 用户活动数据列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<EventResponse> getUserActivity(
            @PathVariable String userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        try {
            logger.info("获取用户活动数据，用户ID: {}, 开始日期: {}, 结束日期: {}", userId, startDate, endDate);
            
            // 设置默认日期范围（过去一年）
            if (startDate == null) {
                startDate = LocalDate.now().minusYears(1);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            // 验证日期范围
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body(
                    EventResponse.error("开始日期不能晚于结束日期"));
            }
            
            // 获取活动数据
            List<ActivityData> activityData = activityService.getUserActivityData(userId, startDate, endDate);
            
            logger.info("获取用户活动数据成功，数据条数: {}", activityData.size());
            return ResponseEntity.ok(EventResponse.success(activityData));
            
        } catch (Exception e) {
            logger.error("获取用户活动数据失败", e);
            return ResponseEntity.internalServerError().body(
                EventResponse.error("获取活动数据失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取用户活动统计
     * 
     * @param userId 用户ID
     * @param startDate 开始日期（可选，默认为一年前）
     * @param endDate 结束日期（可选，默认为今天）
     * @return 用户活动统计数据
     */
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<EventResponse> getUserActivityStats(
            @PathVariable String userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        try {
            logger.info("获取用户活动统计，用户ID: {}, 开始日期: {}, 结束日期: {}", userId, startDate, endDate);
            
            // 设置默认日期范围（过去一年）
            if (startDate == null) {
                startDate = LocalDate.now().minusYears(1);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            // 获取活动统计
            Object stats = activityService.getUserActivityStats(userId, startDate, endDate);
            
            logger.info("获取用户活动统计成功");
            return ResponseEntity.ok(EventResponse.success(stats));
            
        } catch (Exception e) {
            logger.error("获取用户活动统计失败", e);
            return ResponseEntity.internalServerError().body(
                EventResponse.error("获取活动统计失败: " + e.getMessage()));
        }
    }
    
    /**
     * 记录用户活动
     * 
     * @param userId 用户ID
     * @param activityType 活动类型（chat, post, comment, reply, like）
     * @return 记录结果
     */
    @PostMapping("/user/{userId}/record")
    public ResponseEntity<EventResponse> recordUserActivity(
            @PathVariable String userId,
            @RequestParam String activityType) {
        
        try {
            logger.info("记录用户活动，用户ID: {}, 活动类型: {}", userId, activityType);
            
            // 验证活动类型
            if (!isValidActivityType(activityType)) {
                return ResponseEntity.badRequest().body(
                    EventResponse.error("无效的活动类型: " + activityType));
            }
            
            // 记录活动
            ((com.myeden.service.impl.ActivityServiceImpl) activityService)
                .recordUserActivity(userId, activityType);
            
            logger.info("记录用户活动成功");
            return ResponseEntity.ok(EventResponse.success("活动记录成功"));
            
        } catch (Exception e) {
            logger.error("记录用户活动失败", e);
            return ResponseEntity.internalServerError().body(
                EventResponse.error("记录活动失败: " + e.getMessage()));
        }
    }
    
    /**
     * 验证活动类型
     */
    private boolean isValidActivityType(String activityType) {
        return activityType != null && 
               (activityType.equals("chat") || 
                activityType.equals("post") || 
                activityType.equals("comment") || 
                activityType.equals("reply") || 
                activityType.equals("like"));
    }
}