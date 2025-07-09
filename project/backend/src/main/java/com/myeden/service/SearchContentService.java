package com.myeden.service;

import com.myeden.entity.SearchContent;
import java.util.Date;
import java.util.List;

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
} 