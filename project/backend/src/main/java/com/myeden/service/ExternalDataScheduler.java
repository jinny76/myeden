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
    private ExternalDataCacheService externalDataCacheService;
    @Autowired
    private RobotRepository robotRepository;
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    /**
     * 两小时自动采集并持久化外部数据
     */
    @Scheduled(cron = "0 0/2 * * * ?")
    public void fetchAndCacheData() {
        List<NewsItem> news = externalDataService.getLatestNews();
        externalDataCacheService.setNews(news);
        List<HotSearchItem> hot = externalDataService.getHotSearches();
        externalDataCacheService.setHotSearchItems(hot);
        List<MusicItem> music = externalDataService.getMusicRecommendations();
        externalDataCacheService.setMusic(music);
        List<MovieItem> movies = externalDataService.getMovieRecommendations();
        externalDataCacheService.setMovies(movies);
        List<Robot> robots = robotRepository.findAll();
        Map<String, WeatherInfo> weatherMap = new HashMap<>();
        for (Robot robot : robots) {
            String location = robot.getLocation();
            if (location != null && !location.trim().isEmpty() && !weatherMap.containsKey(location)) {
                WeatherInfo weather = externalDataService.getWeather(location);
                weatherMap.put(location, weather);
            }
        }
        externalDataCacheService.setWeatherMap(weatherMap);
        externalDataCacheService.save();
}
} 