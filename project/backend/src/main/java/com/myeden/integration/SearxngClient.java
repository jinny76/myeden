package com.myeden.integration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * SearXNG聚合搜索对接工具
 */
@Slf4j
@Component
public class SearxngClient {

    @Value("${searxng.base-url:http://192.168.1.200:8080/search}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 发起SearXNG搜索
     * @param query 搜索关键词
     * @param params 其他可选参数（如format, categories, engines, language, page, time_range等）
     * @return SearXNG原始响应
     */
    public SearxngResponse search(String query, Map<String, String> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("q", query);
        if (params != null) {
            params.forEach(builder::queryParam);
        }
        URI uri = builder.build(false).encode(StandardCharsets.UTF_8).toUri();
        log.info("请求SearXNG: {}", uri);
        return restTemplate.getForObject(uri, SearxngResponse.class);
    }

    @Data
    public static class SearxngResponse {
        private String query;
        private int number_of_results;
        private List<SearxngResultItem> results;
        private List<Object> answers;
        private List<Object> corrections;
        private List<Object> infoboxes;
        private List<Object> suggestions;
        private List<List<Object>> unresponsive_engines;
    }

    @Data
    public static class SearxngResultItem {
        private String url;
        private String title;
        private String content;
        private String engine;
        private String template;
        private List<String> parsed_url;
        private String img_src;
        private String thumbnail;
        private String priority;
        private List<String> engines;
        private List<Integer> positions;
        private double score;
        private String category;
    }
} 