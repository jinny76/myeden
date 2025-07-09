package com.myeden.controller;

import com.myeden.entity.AIAnalysisResult;
import com.myeden.service.AIAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * AIAnalysisController
 * AI分析结果查询接口
 */
@RestController
@RequestMapping("/api/ai-analysis")
public class AIAnalysisController {

    @Autowired
    private AIAnalysisService aiAnalysisService;

    /**
     * 根据内容ID获取AI分析结果
     */
    @GetMapping("/by-content")
    public EventResponse getResultByContentId(@RequestParam String contentId) {
        AIAnalysisResult result = aiAnalysisService.getResultByContentId(contentId);
        return EventResponse.success(result);
    }

    /**
     * 根据AI标签和时间段检索分析结果
     */
    @GetMapping("/by-tag")
    public EventResponse findByAiTagAndTime(
            @RequestParam String tag,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date end) {
        List<AIAnalysisResult> result = aiAnalysisService.findByAiTagAndTime(tag, start, end);
        return EventResponse.success(result);
    }
} 