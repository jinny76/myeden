package com.myeden.service;

import com.myeden.model.external.ExternalDataCache;
import com.myeden.model.external.NewsItem;
import com.myeden.model.external.WeatherInfo;
import com.myeden.model.external.MusicItem;
import com.myeden.model.external.MovieItem;
import com.myeden.model.external.HotSearchItem;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import java.time.LocalDate;

/**
 * 外部数据缓存服务
 * 负责管理外部数据缓存的加载、保存和线程安全访问
 * 便于在定时任务和业务逻辑中统一管理缓存
 *
 * @author MyEden Team
 * @version 1.0
 * @since 2024-07-03
 */
@Service
public class ExternalDataCacheService {
    private final ExternalDataCache cache = new ExternalDataCache();

    /**
     * 服务启动时自动加载本地缓存
     */
    @PostConstruct
    public void init() {
        synchronized (cache) {
            cache.loadCacheFromFile();
            // 启动时只加载本地缓存，不主动采集，彻底消除循环依赖
        }
    }

    /**
     * 保存缓存到本地文件
     */
    public void save() {
        synchronized (cache) {
            cache.setCacheDate(LocalDate.now().toString());
            cache.saveCacheToFile();
        }
    }

    // 以下为线程安全的getter/setter
    public List<NewsItem> getNews() {
        synchronized (cache) { return cache.getNews(); }
    }
    public void setNews(List<NewsItem> news) {
        synchronized (cache) { cache.setNews(news); }
    }
    public List<HotSearchItem> getHotSearchItems() {
        synchronized (cache) { return cache.getHotSearchItems(); }
    }
    public void setHotSearchItems(List<HotSearchItem> hotSearchItems) {
        synchronized (cache) { cache.setHotSearchItems(hotSearchItems); }
    }
    public Map<String, WeatherInfo> getWeatherMap() {
        synchronized (cache) { return cache.getWeatherMap(); }
    }
    public void setWeatherMap(Map<String, WeatherInfo> weatherMap) {
        synchronized (cache) { cache.setWeatherMap(weatherMap); }
    }
    public List<MusicItem> getMusic() {
        synchronized (cache) { return cache.getMusic(); }
    }
    public void setMusic(List<MusicItem> music) {
        synchronized (cache) { cache.setMusic(music); }
    }
    public List<MovieItem> getMovies() {
        synchronized (cache) { return cache.getMovies(); }
    }
    public void setMovies(List<MovieItem> movies) {
        synchronized (cache) { cache.setMovies(movies); }
    }
} 