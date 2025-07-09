package com.myeden.service;

import com.myeden.model.external.NewsItem;
import com.myeden.model.external.WeatherInfo;
import com.myeden.model.external.MusicItem;
import com.myeden.model.external.HotSearchItem;
import com.myeden.model.external.MovieItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import com.myeden.repository.RobotRepository;
import com.myeden.entity.Robot;

import java.util.HashMap;
import com.myeden.repository.AIAnalysisResultRepository;
import com.myeden.service.SearchContentService;
import com.myeden.service.AIAnalysisService;
import com.myeden.entity.SearchContent;
import com.myeden.entity.AIAnalysisResult;
import java.util.ArrayList;

/**
 * 外部数据定时采集任务
 * 每天定时拉取并缓存数据，服务重启后自动加载
 *
 * @author MyEden Team
 * @version 1.0
 * @since 2024-07-03
 */
@Component
@Slf4j
public class ExternalDataScheduler implements ApplicationContextAware {
    @Autowired
    private ExternalDataService externalDataService;

    @Autowired
    private ExternalDataCacheService externalDataCacheService;
    @Autowired
    private RobotRepository robotRepository;
    @Autowired
    private SearchContentService searchContentService;
    @Autowired
    private AIAnalysisService aiAnalysisService;
    @Autowired
    private AIAnalysisResultRepository aiAnalysisResultRepository;
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    /**
     * 判断指定title是否已被AI分析（通过AI标签查重）
     * @param title 内容标题
     * @return true-已分析，false-未分析
     */
    private boolean isAnalyzedByTitle(String title) {
        List<AIAnalysisResult> results = aiAnalysisResultRepository.findByAiTagsContaining(title);
        return results != null && !results.isEmpty();
    }

    /**
     * 搜索任务队列元素定义
     */
    private static class SearchTask {
        /** 搜索内容（如标题） */
        public String query;
        /** 来源类型（如news/music/movie） */
        public String sourceType;
        /** 原始对象（可选，便于后续扩展） */
        public Object raw;
        public SearchTask(String query, String sourceType, Object raw) {
            this.query = query;
            this.sourceType = sourceType;
            this.raw = raw;
        }
    }

    /**
     * 两小时自动采集并持久化外部数据
     */
    @Scheduled(cron = "0 0/2 * * * ?")
    public void fetchAndCacheData() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. 采集外部数据
            List<NewsItem> news = externalDataService.getLatestNews();
            externalDataCacheService.setNews(news);
            List<HotSearchItem> hot = externalDataService.getHotSearches();
            externalDataCacheService.setHotSearchItems(hot);
            List<MusicItem> music = externalDataService.getMusicRecommendations();
            externalDataCacheService.setMusic(music);
            List<MovieItem> movies = externalDataService.getMovieRecommendations();
            externalDataCacheService.setMovies(movies);
            List<WeatherInfo> weathers = externalDataService.getWeather();
            Map<String, WeatherInfo> weatherMap = new HashMap<>();
            for (WeatherInfo weather : weathers) {
                weatherMap.put(weather.getCity(), weather);
            }
            externalDataCacheService.setWeatherMap(weatherMap);
            externalDataCacheService.save();

            // 2. 构建搜索队列
            List<SearchTask> searchQueue = new ArrayList<>();
            for (NewsItem item : news) {
                searchQueue.add(new SearchTask(item.getTitle(), "新闻", item));
            }
            for (MusicItem item : music) {
                searchQueue.add(new SearchTask(item.getTitle(), "音乐", item));
            }
            for (MovieItem item : movies) {
                searchQueue.add(new SearchTask(item.getTitle(), "影视", item));
            }
            // 3. 依次处理队列
            for (SearchTask task : searchQueue) {
                // 3.1 用title查AI分析结果
                if (isAnalyzedByTitle(task.query)) {
                    log.info("AI分析已存在（通过title标签查重），跳过: [{}] {}", task.sourceType, task.query);
                    continue;
                }

                // 未采集，自动采集
                boolean triggerOk = searchContentService.triggerSearch(task.query, task.sourceType);
                if (!triggerOk) {
                    log.warn("内容采集失败，跳过: [{}] {}", task.sourceType, task.query);
                    continue;
                }

                Thread.sleep(5000);
            }
        } catch (Exception e) {
            log.error("定时采集与AI分析任务异常: {}", e.getMessage());
        }
    }
} 