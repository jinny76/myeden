package com.myeden.controller;

import com.myeden.entity.Robot;
import com.myeden.model.external.*;
import com.myeden.repository.RobotRepository;
import com.myeden.service.RobotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.myeden.service.ExternalDataService;
import com.myeden.service.ExternalDataCacheService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 测试控制器
 * 
 * 功能说明：
 * - 提供测试接口验证JWT认证
 * - 调试Spring Security配置
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/test")
public class TestController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    
    @Autowired
    private ExternalDataService externalDataService;
    
    @Autowired
    private ExternalDataCacheService externalDataCacheService;

    @Autowired
    private RobotRepository robotRepository;
    
    /**
     * 公开测试接口
     * @return 测试响应
     */
    @GetMapping("/public")
    public Map<String, Object> publicTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "公开接口测试成功");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    /**
     * 需要认证的测试接口
     * @return 认证信息
     */
    @GetMapping("/auth")
    public Map<String, Object> authTest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "认证接口测试成功");
        response.put("authenticated", authentication != null && authentication.isAuthenticated());
        response.put("principal", authentication != null ? authentication.getPrincipal() : null);
        response.put("authorities", authentication != null ? authentication.getAuthorities() : null);
        response.put("timestamp", System.currentTimeMillis());
        
        logger.info("认证测试 - 用户: {}, 已认证: {}", 
            authentication != null ? authentication.getName() : "null",
            authentication != null && authentication.isAuthenticated());
        
        return response;
    }
    
    /**
     * 机器人创建测试接口
     * @return 测试响应
     */
    @PostMapping("/robot-create")
    public Map<String, Object> robotCreateTest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "机器人创建测试成功");
        response.put("authenticated", authentication != null && authentication.isAuthenticated());
        response.put("principal", authentication != null ? authentication.getPrincipal() : null);
        response.put("timestamp", System.currentTimeMillis());
        
        logger.info("机器人创建测试 - 用户: {}, 已认证: {}", 
            authentication != null ? authentication.getName() : "null",
            authentication != null && authentication.isAuthenticated());
        
        return response;
    }
    
    /**
     * 新闻抓取测试接口
     * @return 新闻列表
     */
    @GetMapping("/news")
    public Map<String, Object> newsTest() {
        List<NewsItem> news = externalDataService.getLatestNews();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "新闻抓取测试成功");
        response.put("count", news != null ? news.size() : 0);
        response.put("data", news);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    /**
     * 热搜抓取测试接口
     * @return 热搜列表
     */
    @GetMapping("/hotsearch")
    public Map<String, Object> hotSearchTest() {
        List<HotSearchItem> hot = externalDataService.getHotSearches();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "热搜抓取测试成功");
        response.put("count", hot != null ? hot.size() : 0);
        response.put("data", hot);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    /**
     * 音乐抓取测试接口
     * @return 音乐列表
     */
    @GetMapping("/music")
    public Map<String, Object> musicTest() {
        List<MusicItem> music = externalDataService.getMusicRecommendations();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "音乐抓取测试成功");
        response.put("count", music != null ? music.size() : 0);
        response.put("data", music);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    /**
     * 视频（热播剧）抓取测试接口
     * @return 视频列表
     */
    @GetMapping("/movie")
    public Map<String, Object> movieTest() {
        List<MovieItem> movies = externalDataService.getMovieRecommendations();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "视频抓取测试成功");
        response.put("count", movies != null ? movies.size() : 0);
        response.put("data", movies);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    /**
     * 手动采集外部数据并写入缓存
     * @return 操作结果
     */
    @PostMapping("/reload")
    public Map<String, Object> reloadExternalData() {
        Map<String, Object> response = new HashMap<>();
        try {
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
            response.put("success", true);
            response.put("message", "外部数据采集并缓存成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "采集或缓存失败: " + e.getMessage());
        }
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    /**
     * 城市天气抓取测试接口
     * 
     * 功能说明：
     * - 根据传入的城市名(city)获取该城市的天气信息
     * - 便于前后端联调和功能验证
     * 
     * @param city 城市名称（如"北京"、"上海"等），必填
     * @return 包含天气信息的响应Map
     */
    @GetMapping("/weather")
    public Map<String, Object> weatherTest(@RequestParam("city") String city) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 调用外部数据服务获取天气信息
            com.myeden.model.external.WeatherInfo weatherInfo = externalDataService.getWeather(city);
            response.put("message", "天气抓取测试成功");
            response.put("city", city);
            response.put("data", weatherInfo);
        } catch (Exception e) {
            response.put("message", "天气抓取失败: " + e.getMessage());
            response.put("city", city);
            response.put("data", null);
        }
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    
} 