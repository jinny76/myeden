package com.myeden.service;

import com.myeden.repository.ContentGenerationLogRepository;
import com.myeden.entity.ContentGenerationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 内容生成日志归档/清理服务
 * 定期清理7天前的日志，防止数据膨胀
 */
@Service
public class ContentGenerationLogArchiveService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 每天凌晨2点执行一次清理任务
     * 只保留7天内的日志
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void archiveOldLogs() {
        // 计算7天前的时间
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Date threshold = Date.from(sevenDaysAgo.atZone(ZoneId.systemDefault()).toInstant());

        // 构建查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("generatedAt").lt(threshold));

        // 删除7天前的日志
        long deletedCount = mongoTemplate.remove(query, ContentGenerationLog.class).getDeletedCount();
        System.out.println("已归档/清理内容生成日志条数: " + deletedCount);
    }
} 