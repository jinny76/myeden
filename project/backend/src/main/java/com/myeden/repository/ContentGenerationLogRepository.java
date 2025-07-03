package com.myeden.repository;

import com.myeden.entity.ContentGenerationLog;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * AI内容生成日志的MongoDB持久化接口
 */
public interface ContentGenerationLogRepository extends MongoRepository<ContentGenerationLog, String> {
    // 可根据需要扩展自定义查询方法
} 