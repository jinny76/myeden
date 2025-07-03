package com.myeden.service;

import com.myeden.model.external.NewsItem;
import com.myeden.model.external.WeatherInfo;
import com.myeden.model.external.MusicItem;
import com.myeden.model.external.HotSearchItem;
import com.myeden.model.external.MovieItem;
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

/**
 * 外部数据定时采集任务
 * 每天定时拉取并缓存数据，服务重启后自动加载
 *
 * @author MyEden Team
 * @version 1.0
 * @since 2024-07-03
 */
@Component
public class ExternalDataScheduler implements ApplicationContextAware {
    @Autowired
    private ExternalDataService externalDataService;
    @Autowired
    private RobotRepository robotRepository;
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    /**
     * 每天凌晨1点自动采集并持久化外部数据
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void fetchAndCacheData() {
        // 采集新闻
        List<NewsItem> news = externalDataService.getLatestNews();
        ExternalDataCacheService cacheService = context.getBean(ExternalDataCacheService.class);
        cacheService.setNews(news);
        // 采集热搜
        List<HotSearchItem> hotSearches = externalDataService.getHotSearches();
        cacheService.setHotSearchItems(hotSearches);
        // 采集天气（遍历所有机器人location）
        List<Robot> robots = robotRepository.findAll();
        Map<String, WeatherInfo> weatherMap = new HashMap<>();
        for (Robot robot : robots) {
            String location = robot.getLocation();
            if (location != null && !location.trim().isEmpty() && !weatherMap.containsKey(location)) {
                WeatherInfo weather = externalDataService.getWeather(location);
                if (weather != null) {
                    weatherMap.put(location, weather);
                }
            }
        }
        cacheService.setWeatherMap(weatherMap);
        // 采集音乐
        List<MusicItem> music = externalDataService.getMusicRecommendations();
        cacheService.setMusic(music);
        // 采集影视
        List<MovieItem> movies = externalDataService.getMovieRecommendations();
        cacheService.setMovies(movies);
        // 持久化到本地
        cacheService.save();
    }
} 