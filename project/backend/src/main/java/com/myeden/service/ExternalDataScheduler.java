package com.myeden.service;

import com.myeden.model.external.NewsItem;
import com.myeden.model.external.WeatherInfo;
import com.myeden.model.external.MusicItem;
import com.myeden.model.external.MovieItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

/**
 * 外部数据定时采集任务
 * 每天定时拉取并缓存数据，服务重启后自动加载
 *
 * @author MyEden Team
 * @version 1.0
 * @since 2024-07-03
 */
@Component
public class ExternalDataScheduler {
    @Autowired
    private ExternalDataService externalDataService;
    @Autowired
    private ExternalDataCacheService externalDataCacheService;

    /**
     * 每天凌晨1点自动采集并持久化外部数据
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void fetchAndCacheData() {
        // 采集新闻
        List<NewsItem> news = externalDataService.getLatestNews();
        externalDataCacheService.setNews(news);
        // 采集热搜
        List<String> hotSearches = externalDataService.getHotSearches();
        externalDataCacheService.setHotSearches(hotSearches);
        // 采集天气（可根据需要采集多个城市）
        // Map<String, WeatherInfo> weatherMap = ...
        // externalDataCacheService.setWeatherMap(weatherMap);
        // 采集音乐
        List<MusicItem> music = externalDataService.getMusicRecommendations();
        externalDataCacheService.setMusic(music);
        // 采集影视
        List<MovieItem> movies = externalDataService.getMovieRecommendations();
        externalDataCacheService.setMovies(movies);
        // 持久化到本地
        externalDataCacheService.save();
    }
} 