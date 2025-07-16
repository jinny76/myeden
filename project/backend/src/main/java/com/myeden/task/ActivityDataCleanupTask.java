package com.myeden.task;

import com.myeden.service.ActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 活动数据清理定时任务
 * 定期清理过期的活动统计数据，避免数据库无限增长
 */
@Component
public class ActivityDataCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(ActivityDataCleanupTask.class);

    @Autowired
    private ActivityService activityService;

    /**
     * 每天凌晨2点执行清理任务
     * 清理90天前的活动数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldActivityData() {
        try {
            logger.info("开始清理过期活动数据");
            
            // 计算90天前的日期
            LocalDate cutoffDate = LocalDate.now().minusDays(90);
            
            // 清理90天前的活动数据
            long deletedCount = activityService.deleteActivityDataBefore(cutoffDate);
            
            logger.info("活动数据清理完成，删除了 {} 条记录", deletedCount);
            
        } catch (Exception e) {
            logger.error("清理活动数据时发生错误", e);
        }
    }
}