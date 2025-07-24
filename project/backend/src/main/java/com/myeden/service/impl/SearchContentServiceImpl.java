package com.myeden.service.impl;

import com.myeden.entity.SearchContent;
import com.myeden.repository.SearchContentRepository;
import com.myeden.service.AIAnalysisService;
import com.myeden.service.SearchContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.myeden.integration.SearxngClient;

/**
 * SearchContentServiceImpl
 * 内容检索与管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchContentServiceImpl implements SearchContentService {

    private final SearchContentRepository searchContentRepository;
    private final SearxngClient searxngClient; // Assuming SearxngClient is a dependency

    @Autowired
    private final AIAnalysisService aiAnalysisService;

    @Override
    public List<SearchContent> search(String keyword, String sourceType, Date start, Date end) {
        if (keyword != null && sourceType != null && start != null && end != null) {
            return searchContentRepository.complexSearch(sourceType, keyword, start, end);
        } else if (keyword != null) {
            return searchContentRepository.findByKeywordInResults(keyword);
        } else if (sourceType != null) {
            return searchContentRepository.findBySourceType(sourceType);
        } else if (start != null && end != null) {
            return searchContentRepository.findByCreatedAtBetween(start, end);
        } else {
            return searchContentRepository.findAll();
        }
    }

    @Override
    public List<SearchContent> searchBySource(String source) {
        return searchContentRepository.findByResultSource(source);
    }

    @Override
    public SearchContent findByQuery(String query) {
        return searchContentRepository.findByQuery(query);
    }

    @Override
    public SearchContent saveSearchResults(String query, String sourceType, List<SearchContent.SearchResultItem> results) {
        SearchContent content = new SearchContent();
        content.setQuery(query);
        content.setSourceType(sourceType);
        content.setResults(results);
        content.setCreatedAt(new Date());
        content.setStatus("pending");
        return searchContentRepository.save(content);
    }

    @Override
    public boolean triggerSearch(String query, String sourceType) {
        Map<String, String> params = new java.util.HashMap<>();
        params.put("format", "json");
        SearxngClient.SearxngResponse response = searxngClient.search(query, params);
        if (response == null || response.getResults() == null) {
            return false;
        }
        // 类型安全地处理结果
        List<SearxngClient.SearxngResultItem> searxngResults = response.getResults();
        List<SearchContent.SearchResultItem> resultItems = new java.util.ArrayList<>();
        if (searxngResults != null) {
            for (SearxngClient.SearxngResultItem item : searxngResults) {
                if (item.getContent() == null || !item.getContent().contains(query)) {
                    continue;
                }
                resultItems.add(new SearchContent.SearchResultItem(
                        item.getTitle(),
                        item.getContent(),
                        item.getUrl(),
                        item.getEngine()
                ));
            }
        }
        if (!resultItems.isEmpty()) {
            SearchContent content = saveSearchResults(query, sourceType, resultItems);
            aiAnalysisService.analyzeContent(content.getId());
            
            return true;
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> searchImages(String query) {
        try {
            // 设置图片搜索参数
            Map<String, String> params = new java.util.HashMap<>();
            params.put("format", "json");
            params.put("categories", "images"); // 指定搜索图片分类
            params.put("safesearch", "1"); // 安全搜索
            params.put("language", "zh-CN");
            
            // 调用搜索引擎
            SearxngClient.SearxngResponse response = searxngClient.search(query, params);
            
            if (response == null || response.getResults() == null) {
                return new java.util.ArrayList<>();
            }
            
            // 直接返回图片搜索结果，不保存到数据库
            List<Map<String, Object>> imageResults = new java.util.ArrayList<>();
            for (SearxngClient.SearxngResultItem item : response.getResults()) {
                // 只处理有图片的结果
                if (item.getImg_src() != null && !item.getImg_src().isEmpty()) {
                    Map<String, Object> imageResult = new java.util.HashMap<>();
                    imageResult.put("title", item.getTitle());
                    imageResult.put("url", item.getUrl());
                    imageResult.put("imgSrc", item.getImg_src());
                    imageResult.put("thumbnail", item.getThumbnail());
                    imageResult.put("engine", item.getEngine());
                    imageResult.put("content", item.getContent());
                    imageResults.add(imageResult);
                }
            }
            
            return imageResults;
            
        } catch (Exception e) {
            log.error("图片搜索失败: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> searchVideos(String query) {
        try {
            // 设置视频搜索参数
            Map<String, String> params = new java.util.HashMap<>();
            params.put("format", "json");
            params.put("categories", "videos"); // 指定搜索视频分类
            params.put("safesearch", "1"); // 安全搜索
            params.put("language", "zh-CN");
            
            // 调用搜索引擎
            SearxngClient.SearxngResponse response = searxngClient.search(query, params);
            
            if (response == null || response.getResults() == null) {
                return new java.util.ArrayList<>();
            }
            
            // 直接返回视频搜索结果，不保存到数据库
            List<Map<String, Object>> videoResults = new java.util.ArrayList<>();
            for (SearxngClient.SearxngResultItem item : response.getResults()) {
                Map<String, Object> videoResult = new java.util.HashMap<>();
                videoResult.put("title", item.getTitle());
                videoResult.put("url", item.getUrl());
                videoResult.put("content", item.getContent());
                videoResult.put("engine", item.getEngine());
                // 视频可能有缩略图
                if (item.getThumbnail() != null && !item.getThumbnail().isEmpty()) {
                    videoResult.put("thumbnail", item.getThumbnail());
                }
                /*// 视频可能有时长信息
                if (item.getLength() != null && !item.getLength().isEmpty()) {
                    videoResult.put("duration", item.getLength());
                }
                // 视频可能有发布时间
                if (item.getPublishedDate() != null) {
                    videoResult.put("publishedDate", item.getPublishedDate());
                }*/
                videoResults.add(videoResult);
            }
            
            return videoResults;
            
        } catch (Exception e) {
            log.error("视频搜索失败: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }
} 