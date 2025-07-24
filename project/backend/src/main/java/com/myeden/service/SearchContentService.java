package com.myeden.service;

import com.myeden.entity.SearchContent;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * SearchContentService
 * 内容检索与管理服务接口
 */
public interface SearchContentService {

    List<SearchContent> search(String keyword, String sourceType, Date start, Date end);

    List<SearchContent> searchBySource(String source);

    SearchContent findByQuery(String query);

    SearchContent saveSearchResults(String query, String sourceType, List<SearchContent.SearchResultItem> results);

    boolean triggerSearch(String query, String sourceType);

    /**
     * 图片搜索，直接返回搜索引擎结果
     * @param query 搜索关键词
     * @return 图片搜索结果列表
     */
    List<Map<String, Object>> searchImages(String query);

    /**
     * 视频搜索，直接返回搜索引擎结果
     * @param query 搜索关键词
     * @return 视频搜索结果列表
     */
    List<Map<String, Object>> searchVideos(String query);
} 