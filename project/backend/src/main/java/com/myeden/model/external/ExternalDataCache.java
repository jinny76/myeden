package com.myeden.model.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 外部数据缓存，支持本地持久化
 * 存储新闻、热搜、天气、音乐、影视等数据，服务重启后可自动加载
 *
 * @author MyEden Team
 * @version 1.0
 * @since 2024-07-03
 */
public class ExternalDataCache {
    private static final String CACHE_FILE = "external_data_cache.json";

    /** 新闻列表 */
    private List<NewsItem> news;
    /** 热搜关键词列表 */
    private List<String> hotSearches;
    /** 城市-天气映射 */
    private Map<String, WeatherInfo> weatherMap;
    /** 音乐推荐列表 */
    private List<MusicItem> music;
    /** 影视推荐列表 */
    private List<MovieItem> movies;

    // 标准getter/setter
    public List<NewsItem> getNews() { return news; }
    public void setNews(List<NewsItem> news) { this.news = news; }
    public List<String> getHotSearches() { return hotSearches; }
    public void setHotSearches(List<String> hotSearches) { this.hotSearches = hotSearches; }
    public Map<String, WeatherInfo> getWeatherMap() { return weatherMap; }
    public void setWeatherMap(Map<String, WeatherInfo> weatherMap) { this.weatherMap = weatherMap; }
    public List<MusicItem> getMusic() { return music; }
    public void setMusic(List<MusicItem> music) { this.music = music; }
    public List<MovieItem> getMovies() { return movies; }
    public void setMovies(List<MovieItem> movies) { this.movies = movies; }

    /**
     * 服务启动时自动加载本地缓存
     */
    @PostConstruct
    public void loadCacheFromFile() {
        try {
            File file = new File(CACHE_FILE);
            if (file.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                ExternalDataCache cache = mapper.readValue(file, ExternalDataCache.class);
                this.news = cache.news;
                this.hotSearches = cache.hotSearches;
                this.weatherMap = cache.weatherMap;
                this.music = cache.music;
                this.movies = cache.movies;
            }
        } catch (Exception e) {
            // 日志记录，首次启动或文件损坏时自动忽略
        }
    }

    /**
     * 每次刷新后持久化到本地
     */
    public void saveCacheToFile() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File(CACHE_FILE), this);
        } catch (Exception e) {
            // 日志记录
        }
    }
} 