package com.myeden.controller;

import com.myeden.entity.SearchContent;
import com.myeden.service.SearchContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * SearchContentController
 * 内容检索与管理接口
 */
@Tag(name = "内容检索接口", description = "SearchContent相关API")
@RestController
@RequestMapping("/api/search-content")
public class SearchContentController {

    @Autowired
    private SearchContentService searchContentService;

    /**
     * 根据关键词、类型、时间段检索内容
     */
    @Operation(summary = "根据关键词、类型、时间段检索内容")
    @GetMapping("/search")
    public EventResponse search(
            @RequestParam String keyword,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date end) {
        List<SearchContent> result = searchContentService.search(keyword, sourceType, start, end);
        return EventResponse.success(result);
    }

    /**
     * 按来源检索内容
     */
    @Operation(summary = "按来源检索内容")
    @GetMapping("/by-source")
    public EventResponse searchBySource(@RequestParam String source) {
        List<SearchContent> result = searchContentService.searchBySource(source);
        return EventResponse.success(result);
    }

    /**
     * 根据原始搜索关键词查找历史内容
     */
    @Operation(summary = "根据原始搜索关键词查找历史内容")
    @GetMapping("/by-query")
    public EventResponse findByQuery(@RequestParam String query) {
        SearchContent content = searchContentService.findByQuery(query);
        return EventResponse.success(content);
    }

    /**
     * 触发搜索
     */
    @Operation(summary = "触发搜索")
    @PostMapping("/trigger-search")
    public EventResponse triggerSearch(@RequestParam String query, @RequestParam String sourceType) {
        boolean result = searchContentService.triggerSearch(query, sourceType);
        return EventResponse.success(result);
    }

    /**
     * 图片搜索
     */
    @Operation(summary = "图片搜索")
    @GetMapping("/search-images")
    public EventResponse searchImages(@RequestParam String query) {
        List<Map<String, Object>> results = searchContentService.searchImages(query);
        return EventResponse.success(results);
    }

    /**
     * 视频搜索
     */
    @Operation(summary = "视频搜索")
    @GetMapping("/search-videos")
    public EventResponse searchVideos(@RequestParam String query) {
        List<Map<String, Object>> results = searchContentService.searchVideos(query);
        return EventResponse.success(results);
    }
} 