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
 * SearchContent
 * 存储一次SearXNG搜索的所有结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "search_content")
public class SearchContent {

    @Id
    private String id;

    /** 本次搜索的原始关键词 */
    @Indexed
    private String query;

    /** 内容类型（如news、post、music、movie、hotspot等） */
    @Indexed
    private String sourceType;

    /** 搜索结果列表 */
    private List<SearchResultItem> results;

    /** 搜索时间 */
    @Indexed
    private Date createdAt;

    /** 状态（pending|analyzed|failed） */
    @Indexed
    private String status;

    /**
     * 搜索结果子对象
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResultItem {
        private String title;
        private String summary;
        private String url;
        private String source;
        // 可扩展字段，如图片、发布时间等

        /**
         * 获取内容（兼容AI分析服务）
         * 优先返回summary
         */
        public String getContent() {
            return summary;
        }
        public void setContent(String content) {
            this.summary = content;
        }
    }
} 