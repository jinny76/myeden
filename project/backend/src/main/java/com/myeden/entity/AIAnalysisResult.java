package com.myeden.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * AIAnalysisResult
 * 存储AI分析的结构化结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_analysis_result")
public class AIAnalysisResult {

    @Id
    private String id;

    /** 对应search_content的id */
    @Indexed
    private String contentId;

    /** 内容类型 */
    @Indexed
    private String sourceType;

    /** AI生成的摘要 */
    private String aiSummary;

    /** AI生成的标签 */
    private List<String> aiTags;

    /** 结构化实体（如人名、地名等） */
    private List<Entity> entities;

    /** 情感分析结果 */
    private String sentiment;

    /** 分析时间 */
    @Indexed
    private Date analysisTime;

    /**
     * 结构化实体对象
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entity {
        private String type;   // person/location/organization等
        private String value;
        private String source; // 来源（可选）
    }
} 