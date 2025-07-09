package com.myeden.service;

import com.myeden.entity.AIAnalysisResult;
import java.util.Date;
import java.util.List;

/**
 * AIAnalysisService
 * AI分析与标签服务接口
 */
public interface AIAnalysisService {

    AIAnalysisResult analyzeContent(String contentId);

    List<AIAnalysisResult> findByAiTagAndTime(String tag, Date start, Date end);

    AIAnalysisResult getResultByContentId(String contentId);
} 