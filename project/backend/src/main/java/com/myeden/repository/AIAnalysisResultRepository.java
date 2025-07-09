package com.myeden.repository;

import com.myeden.entity.AIAnalysisResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Date;
import java.util.List;

/**
 * AIAnalysisResultRepository
 * 支持ai_analysis_result集合的基本增删查改及多维检索
 */
public interface AIAnalysisResultRepository extends MongoRepository<AIAnalysisResult, String> {

    /**
     * 根据内容ID查找AI分析结果
     */
    List<AIAnalysisResult> findByContentId(String contentId);

    /**
     * 根据AI标签查找，按分析时间段过滤
     */
    List<AIAnalysisResult> findByAiTagsContainingAndAnalysisTimeBetween(String tag, Date start, Date end);

    /**
     * 根据AI标签查找所有结果
     */
    List<AIAnalysisResult> findByAiTagsContaining(String tag);
} 