package com.myeden.service.impl;

import com.myeden.model.external.*;
import com.myeden.service.ExternalDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 外部数据采集服务Mock实现
 * 返回模拟数据，便于开发和联调
 */
@Service
@Slf4j
public class ExternalDataServiceImpl implements ExternalDataService {

    @Override
    public List<NewsItem> getLatestNews() {
        List<NewsItem> news = new ArrayList<>();
        try {
            String apiKey = "36de5db81215";
            String apiUrl = "https://whyta.cn/api/toutiao?key=" + apiKey;
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(conn.getInputStream());
            if (root.has("items")) {
                for (JsonNode item : root.get("items")) {
                    NewsItem newsItem = new NewsItem();
                    newsItem.setTitle(item.has("title") ? item.get("title").asText() : null);
                    newsItem.setUrl(item.has("url") ? item.get("url").asText() : null);
                    news.add(newsItem);
                }
            }
        } catch (Exception e) {
            log.error("获取新闻失败", e);
        }
        return news;
    }

    @Override
    public List<HotSearchItem> getHotSearches() {
        List<HotSearchItem> hot = new ArrayList<>();
        try {
            String apiKey = "36de5db81215";
            String apiUrl = "https://whyta.cn/api/tx/weibohot?key=" + apiKey;
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(conn.getInputStream());
            if (root.has("code") && root.get("code").asInt() == 200 && root.has("result") && root.get("result").has("list")) {
                for (JsonNode item : root.get("result").get("list")) {
                    HotSearchItem hotItem = new HotSearchItem();
                    hotItem.setTitle(item.has("hotword") ? item.get("hotword").asText() : null);
                    hotItem.setSummary(item.has("hotwordnum") ? item.get("hotwordnum").asText() : null);
                    hot.add(hotItem);
                }
            }
        } catch (Exception e) {
            log.error("获取热搜失败", e);
        }
        return hot;
    }

    @Override
    public WeatherInfo getWeather(String city) {
        WeatherInfo info = new WeatherInfo();
        try {
            String apiKey = "36de5db81215";
            String apiUrl = "https://whyta.cn/api/tianqi?key=" + apiKey + "&city=" + java.net.URLEncoder.encode(city == null ? "北京" : city, "UTF-8");
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(conn.getInputStream());
            if (root.has("lives") && root.get("lives").isArray() && root.get("lives").size() > 0) {
                JsonNode live = root.get("lives").get(0);
                info.setCity(live.has("city") ? live.get("city").asText() : city);
                info.setDescription(live.has("weather") ? live.get("weather").asText() : null);
                info.setTemperature(live.has("temperature") ? live.get("temperature").asText() + "℃" : null);
            } else {
                return null;
            }
            Thread.sleep(1000L);
        } catch (Exception e) {
            info.setCity(city == null ? "北京" : city);
            info.setDescription("获取失败");
            info.setTemperature("");
        }
        return info;
    }

    @Override
    public List<MusicItem> getMusicRecommendations() {
        List<MusicItem> music = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            try {
                String apiUrl = "https://free.wqwlkj.cn/wqwlapi/wyy_random.php?type=json";
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(conn.getInputStream());
                JsonNode dataNode = root.has("data") ? root.get("data") : root;
                if (dataNode.has("name") && dataNode.has("artistsname") && dataNode.has("url")) {
                    MusicItem item = new MusicItem();
                    item.setTitle(dataNode.get("name").asText());
                    item.setArtist(dataNode.get("artistsname").asText());
                    item.setUrl(dataNode.get("url").asText());
                    if (dataNode.has("picurl")) {
                        item.setImage(dataNode.get("picurl").asText());
                    }
                    music.add(item);
                }
                Thread.sleep(1000L);
            } catch (Exception e) {
                log.error("批量获取音乐失败", e);
            }
        }

        return music;
    }

    @Override
    public List<MovieItem> getMovieRecommendations() {
        List<MovieItem> movies = new ArrayList<>();
        try {
            String apiUrl = "https://api.52vmy.cn/api/wl/top/tv?type=json";
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(conn.getInputStream());
            if (root.has("code") && root.get("code").asInt() == 200 && root.has("data")) {
                for (JsonNode item : root.get("data")) {
                    if (item.has("seriesInfo")) {
                        JsonNode series = item.get("seriesInfo");
                        MovieItem movie = new MovieItem();
                        movie.setTitle(series.has("name") ? series.get("name").asText() : null);
                        movies.add(movie);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取热播剧失败", e);
        }
        return movies;
    }
} 