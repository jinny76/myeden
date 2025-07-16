package com.myeden.service.impl;

import com.myeden.entity.UserDailyActivity;
import com.myeden.repository.UserDailyActivityRepository;
import com.myeden.service.ActivityService;
import com.myeden.service.dto.ActivityData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 活动数据服务实现类
 * 
 * 功能说明：
 * - 基于预统计数据提供用户活动信息
 * - 支持快速查询和统计分析
 * - 优化贡献图数据加载性能
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-01-27
 */
@Service
public class ActivityServiceImpl implements ActivityService {
    
    private static final Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);
    
    @Autowired
    private UserDailyActivityRepository userDailyActivityRepository;
    
    @Override
    public List<ActivityData> getUserActivityData(String userId, LocalDate startDate, LocalDate endDate) {
        try {
            logger.info("获取用户活动数据，用户ID: {}, 日期范围: {} - {}", userId, startDate, endDate);
            
            // 从数据库查询统计数据
            List<UserDailyActivity> activities = userDailyActivityRepository
                .findByUserIdAndDateBetweenOrderByDateAsc(userId, startDate, endDate);
            
            // 转换为ActivityData格式
            List<ActivityData> result = activities.stream()
                .map(this::convertToActivityData)
                .collect(Collectors.toList());
            
            logger.info("获取用户活动数据成功，数据条数: {}", result.size());
            return result;
            
        } catch (Exception e) {
            logger.error("获取用户活动数据失败", e);
            throw new RuntimeException("获取活动数据失败", e);
        }
    }
    
    @Override
    public Map<String, Object> getUserActivityStats(String userId, LocalDate startDate, LocalDate endDate) {
        try {
            logger.info("获取用户活动统计，用户ID: {}, 日期范围: {} - {}", userId, startDate, endDate);
            
            // 查询统计数据
            List<UserDailyActivity> activities = userDailyActivityRepository
                .findActivityStatsByUserIdAndDateBetween(userId, startDate, endDate);
            
            // 计算统计信息
            Map<String, Object> stats = new HashMap<>();
            
            // 总活动次数
            int totalActivities = activities.stream()
                .mapToInt(UserDailyActivity::getTotalCount)
                .sum();
            stats.put("totalActivities", totalActivities);
            
            // 活动天数
            int activeDays = (int) activities.stream()
                .filter(a -> a.getTotalCount() > 0)
                .count();
            stats.put("activeDays", activeDays);
            
            // 各类活动统计
            int totalChat = activities.stream().mapToInt(UserDailyActivity::getChatCount).sum();
            int totalPost = activities.stream().mapToInt(UserDailyActivity::getPostCount).sum();
            int totalComment = activities.stream().mapToInt(UserDailyActivity::getCommentCount).sum();
            int totalReply = activities.stream().mapToInt(UserDailyActivity::getReplyCount).sum();
            int totalLike = activities.stream().mapToInt(UserDailyActivity::getLikeCount).sum();
            
            Map<String, Integer> activityBreakdown = new HashMap<>();
            activityBreakdown.put("chat", totalChat);
            activityBreakdown.put("post", totalPost);
            activityBreakdown.put("comment", totalComment);
            activityBreakdown.put("reply", totalReply);
            activityBreakdown.put("like", totalLike);
            stats.put("activityBreakdown", activityBreakdown);
            
            // 平均每日活动次数
            double averagePerDay = activeDays > 0 ? (double) totalActivities / activeDays : 0;
            stats.put("averagePerDay", Math.round(averagePerDay * 100.0) / 100.0);
            
            // 最活跃的日期
            UserDailyActivity mostActiveDay = activities.stream()
                .max(Comparator.comparing(UserDailyActivity::getTotalCount))
                .orElse(null);
            
            if (mostActiveDay != null) {
                Map<String, Object> mostActive = new HashMap<>();
                mostActive.put("date", mostActiveDay.getDate());
                mostActive.put("count", mostActiveDay.getTotalCount());
                stats.put("mostActiveDay", mostActive);
            }
            
            // 连续活跃天数
            int longestStreak = calculateLongestStreak(activities);
            stats.put("longestStreak", longestStreak);
            
            logger.info("获取用户活动统计成功");
            return stats;
            
        } catch (Exception e) {
            logger.error("获取用户活动统计失败", e);
            throw new RuntimeException("获取活动统计失败", e);
        }
    }
    
    @Override
    public Map<LocalDate, Integer> getUserChatActivity(String userId, LocalDate startDate, LocalDate endDate) {
        List<UserDailyActivity> activities = userDailyActivityRepository
            .findByUserIdAndDateBetweenOrderByDateAsc(userId, startDate, endDate);
        
        return activities.stream()
            .collect(Collectors.toMap(
                UserDailyActivity::getDate,
                UserDailyActivity::getChatCount
            ));
    }
    
    @Override
    public Map<LocalDate, Integer> getUserPostActivity(String userId, LocalDate startDate, LocalDate endDate) {
        List<UserDailyActivity> activities = userDailyActivityRepository
            .findByUserIdAndDateBetweenOrderByDateAsc(userId, startDate, endDate);
        
        return activities.stream()
            .collect(Collectors.toMap(
                UserDailyActivity::getDate,
                UserDailyActivity::getPostCount
            ));
    }
    
    @Override
    public Map<LocalDate, Integer> getUserCommentActivity(String userId, LocalDate startDate, LocalDate endDate) {
        List<UserDailyActivity> activities = userDailyActivityRepository
            .findByUserIdAndDateBetweenOrderByDateAsc(userId, startDate, endDate);
        
        return activities.stream()
            .collect(Collectors.toMap(
                UserDailyActivity::getDate,
                activity -> activity.getCommentCount() + activity.getReplyCount()
            ));
    }
    
    @Override
    public Map<LocalDate, Integer> getUserLikeActivity(String userId, LocalDate startDate, LocalDate endDate) {
        List<UserDailyActivity> activities = userDailyActivityRepository
            .findByUserIdAndDateBetweenOrderByDateAsc(userId, startDate, endDate);
        
        return activities.stream()
            .collect(Collectors.toMap(
                UserDailyActivity::getDate,
                UserDailyActivity::getLikeCount
            ));
    }
    
    /**
     * 记录用户活动（供其他服务调用）
     * 
     * @param userId 用户ID
     * @param activityType 活动类型
     */
    public void recordUserActivity(String userId, String activityType) {
        try {
            LocalDate today = LocalDate.now();
            
            // 查找或创建今日活动记录
            UserDailyActivity activity = userDailyActivityRepository
                .findByUserIdAndDate(userId, today)
                .orElse(new UserDailyActivity(userId, today));
            
            // 增加相应活动计数
            activity.incrementActivityCount(activityType);
            
            // 保存到数据库
            userDailyActivityRepository.save(activity);
            
            logger.debug("记录用户活动成功，用户ID: {}, 活动类型: {}", userId, activityType);
            
        } catch (Exception e) {
            logger.error("记录用户活动失败，用户ID: {}, 活动类型: {}", userId, activityType, e);
        }
    }
    
    /**
     * 转换为ActivityData格式
     */
    private ActivityData convertToActivityData(UserDailyActivity activity) {
        List<ActivityData.ActivityDetail> details = new ArrayList<>();
        
        if (activity.getChatCount() > 0) {
            details.add(new ActivityData.ActivityDetail("chat", activity.getChatCount(), "聊天"));
        }
        if (activity.getPostCount() > 0) {
            details.add(new ActivityData.ActivityDetail("post", activity.getPostCount(), "发帖"));
        }
        if (activity.getCommentCount() > 0) {
            details.add(new ActivityData.ActivityDetail("comment", activity.getCommentCount(), "评论"));
        }
        if (activity.getReplyCount() > 0) {
            details.add(new ActivityData.ActivityDetail("reply", activity.getReplyCount(), "回复"));
        }
        if (activity.getLikeCount() > 0) {
            details.add(new ActivityData.ActivityDetail("like", activity.getLikeCount(), "点赞"));
        }
        
        return new ActivityData(activity.getDate(), activity.getTotalCount(), details);
    }
    
    /**
     * 计算最长连续活跃天数
     */
    private int calculateLongestStreak(List<UserDailyActivity> activities) {
        if (activities.isEmpty()) {
            return 0;
        }
        
        // 按日期排序
        List<UserDailyActivity> sorted = activities.stream()
            .filter(a -> a.getTotalCount() > 0)
            .sorted(Comparator.comparing(UserDailyActivity::getDate))
            .collect(Collectors.toList());
        
        int maxStreak = 0;
        int currentStreak = 0;
        LocalDate previousDate = null;
        
        for (UserDailyActivity activity : sorted) {
            if (previousDate == null || activity.getDate().equals(previousDate.plusDays(1))) {
                currentStreak++;
            } else {
                maxStreak = Math.max(maxStreak, currentStreak);
                currentStreak = 1;
            }
            previousDate = activity.getDate();
        }
        
        return Math.max(maxStreak, currentStreak);
    }
    
    @Override
    public long deleteActivityDataBefore(LocalDate cutoffDate) {
        try {
            logger.info("开始删除 {} 之前的活动数据", cutoffDate);
            
            // 删除指定日期之前的活动数据
            long deletedCount = userDailyActivityRepository.deleteByDateBefore(cutoffDate);
            
            logger.info("成功删除 {} 条活动数据", deletedCount);
            return deletedCount;
            
        } catch (Exception e) {
            logger.error("删除活动数据时发生错误", e);
            throw new RuntimeException("删除活动数据失败", e);
        }
    }
}