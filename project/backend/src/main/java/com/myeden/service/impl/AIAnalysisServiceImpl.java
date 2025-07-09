package com.myeden.service.impl;

import com.myeden.entity.AIAnalysisResult;
import com.myeden.repository.AIAnalysisResultRepository;
import com.myeden.service.AIAnalysisService;
import com.myeden.service.DifyService;
import com.myeden.service.SearchContentService;
import com.myeden.entity.SearchContent;
import com.myeden.repository.SearchContentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

// DTO for Dify AI结果
import lombok.Data;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

@Data
class DifyAiResultDTO {
    private String aiSummary;
    private List<String> aiTags;
    private List<Map<String, Object>> entities;
    private String sentiment;
}

/**
 * AIAnalysisServiceImpl
 * AI分析与标签服务实现
 */
@Service
@RequiredArgsConstructor
public class AIAnalysisServiceImpl implements AIAnalysisService {

    private final AIAnalysisResultRepository aiAnalysisResultRepository;

    private final DifyService difyService;

    private final SearchContentRepository searchContentRepository;

    @Value("${dify.data.apiKey}")
    private String apiKey;

    @Override
    public AIAnalysisResult analyzeContent(String contentId) {
        // 1. 查找content
        SearchContent content = searchContentRepository.findById(contentId).orElse(null);
        if (content == null || content.getResults() == null || content.getResults().isEmpty()) {
            return null;
        }
        // 2. 合并内容
        StringBuilder sb = new StringBuilder();
        for (SearchContent.SearchResultItem item : content.getResults()) {
            if (item.getContent() != null) {
                sb.append(item.getContent()).append("\n");
            }
        }
        String mergedContent = sb.toString();
        try {
            // 3. 写入临时文件
            java.io.File tempFile = java.io.File.createTempFile("dify_content_", ".txt");
            java.nio.file.Files.write(tempFile.toPath(), mergedContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            // 4. 调用DifyService分析
            // apiKey由配置文件注入
            String userId = "system";
            String variableName = "file";
            String json = difyService.recognizeFileWorkflow(tempFile.getAbsolutePath(), apiKey, userId, variableName);

            if (json != null) {
                if (!json.startsWith("{") || !json.endsWith("}")) {
                    if (json.indexOf("{") != -1 && json.indexOf("}") != -1) {
                        json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
                    } else {
                        json = "{}";
                    }
                }

                // 5. 反序列化
                ObjectMapper objectMapper = new ObjectMapper();
                AIAnalysisResult result = objectMapper.readValue(json, AIAnalysisResult.class);
                result.setContentId(contentId);
                result.setSourceType(content.getSourceType());
                result.setAnalysisTime(new java.util.Date());
                // 实体等可进一步处理
                // 7. 存入数据库
                if (result.getAiSummary() != null) {
                    return aiAnalysisResultRepository.save(result);
                } else {
                    return null;
                }
            }

            return null;
        } catch (Exception e) {
            // 日志省略
            return null;
        }
    }

    @Override
    public List<AIAnalysisResult> findByAiTagAndTime(String tag, Date start, Date end) {
        if (start == null && end == null) {
            // 只按tag搜索，合并所有AI分析结果为主题背景
            return aiAnalysisResultRepository.findByAiTagsContaining(tag);            
        } else {
            // 原有按tag+时间段过滤
            return aiAnalysisResultRepository.findByAiTagsContainingAndAnalysisTimeBetween(tag, start, end);
        }
    }

    @Override
    public AIAnalysisResult getResultByContentId(String contentId) {
        List<AIAnalysisResult> results = aiAnalysisResultRepository.findByContentId(contentId);
        return results.isEmpty() ? null : results.get(0);
    }
} 