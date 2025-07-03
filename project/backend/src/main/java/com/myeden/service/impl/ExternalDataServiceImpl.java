package com.myeden.service.impl;

import com.myeden.model.external.*;
import com.myeden.service.ExternalDataService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 外部数据采集服务Mock实现
 * 返回模拟数据，便于开发和联调
 */
@Service
public class ExternalDataServiceImpl implements ExternalDataService {

    @Override
    public List<NewsItem> getLatestNews() {
        List<NewsItem> news = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            NewsItem item = new NewsItem();
            item.setTitle("Mock新闻标题" + i);
            item.setSummary("这是第" + i + "条新闻的摘要内容，仅供测试。");
            item.setUrl("https://news.example.com/mock" + i);
            news.add(item);
        }
        return news;
    }

    @Override
    public List<String> getHotSearches() {
        List<String> hot = new ArrayList<>();
        Collections.addAll(hot, "热搜词1", "热搜词2", "热搜词3", "热搜词4", "热搜词5");
        return hot;
    }

    @Override
    public WeatherInfo getWeather(String city) {
        WeatherInfo info = new WeatherInfo();
        info.setCity(city == null ? "北京" : city);
        info.setDescription("晴转多云");
        info.setTemperature("28℃");
        return info;
    }

    @Override
    public List<MusicItem> getMusicRecommendations() {
        List<MusicItem> music = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            MusicItem item = new MusicItem();
            item.setTitle("Mock歌曲" + i);
            item.setArtist("歌手" + i);
            item.setUrl("https://music.example.com/mock" + i);
            music.add(item);
        }
        return music;
    }

    @Override
    public List<MovieItem> getMovieRecommendations() {
        List<MovieItem> movies = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            MovieItem item = new MovieItem();
            item.setTitle("Mock电影" + i);
            item.setDescription("这是一部Mock电影的简介" + i);
            item.setUrl("https://movie.example.com/mock" + i);
            movies.add(item);
        }
        return movies;
    }
} 