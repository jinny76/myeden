package com.myeden.service;

import com.myeden.model.external.NewsItem;
import com.myeden.model.external.WeatherInfo;
import com.myeden.model.external.MusicItem;
import com.myeden.model.external.HotSearchItem;
import com.myeden.model.external.MovieItem;
import java.util.List;

/**
 * 外部数据采集服务接口
 * 负责采集新闻、热搜、天气、音乐、影视等数据
 * 所有方法均为只读操作，实际采集由实现类完成。
 *
 * @author MyEden Team
 * @version 1.0
 * @since 2024-07-03
 */
public interface ExternalDataService {
    /**
     * 获取最新新闻列表
     * @return 新闻条目列表，按时间倒序排列，最多返回10条
     */
    List<NewsItem> getLatestNews();

    /**
     * 获取最新热搜关键词
     * @return 热搜关键词列表，按热度降序，最多返回10条
     */
    List<HotSearchItem> getHotSearches();

    /**
     * 获取指定城市的天气信息
     * @param city 城市名（如"北京"），如为空则返回默认城市天气
     * @return 天气信息对象
     */
    WeatherInfo getWeather(String city);

    /**
     * 获取最新音乐推荐
     * @return 音乐条目列表，按热度降序，最多返回10条
     */
    List<MusicItem> getMusicRecommendations();

    /**
     * 获取最新影视推荐
     * @return 影视条目列表，按热度降序，最多返回10条
     */
    List<MovieItem> getMovieRecommendations();
} 