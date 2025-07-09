package com.myeden.service.impl;

import com.myeden.entity.SearchContent;
import com.myeden.repository.SearchContentRepository;
import com.myeden.service.AIAnalysisService;
import com.myeden.service.SearchContentService;
import lombok.RequiredArgsConstructor;
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
                if (item.getContent() == null || (!item.getContent().contains(query) && !"新闻".equals(sourceType))) {
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
} 