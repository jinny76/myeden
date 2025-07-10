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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.myeden.repository.RobotRepository;
import java.util.Set;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

/**
 * 外部数据采集服务Mock实现
 * 返回模拟数据，便于开发和联调
 */
@Service
@Slf4j
public class ExternalDataServiceImpl implements ExternalDataService {

    private final RobotRepository robotRepository;

    public ExternalDataServiceImpl(RobotRepository robotRepository) {
        this.robotRepository = robotRepository;
    }

    /**
     * 获取最新新闻列表
     * 
     * 该方法从 https://api.xhus.cn/api/rdouyin 获取新闻数据，
     * 接口返回为纯文本，每行一个新闻标题，格式如：
     * 1. 济南暴雨
     * 2. 东亚杯揭幕战中国vs韩国
     * ...
     * 
     * 每行去除序号后作为 NewsItem 的 title 字段，其他字段置为 null。
     * 
     * @return 新闻条目列表，每个条目仅包含标题
     */
    @Override
    public List<NewsItem> getLatestNews() {
        List<NewsItem> news = new ArrayList<>();
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            // 1. 构建请求 URL
            String apiUrl = "https://api.xhus.cn/api/rdouyin";
            URL url = new URL(apiUrl);

            // 2. 打开 HTTP 连接
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            // 3. 读取响应内容（纯文本，每行一个新闻标题）
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // 跳过空行
                if (!line.isEmpty()) {
                    // 只取标题部分，忽略前面的序号和点
                    String title = line.replaceFirst("^\\d+\\.\\s*", "");
                    NewsItem newsItem = new NewsItem();
                    newsItem.setTitle(title);
                    newsItem.setSummary(null);
                    newsItem.setUrl(null);
                    newsItem.setImage(null);
                    news.add(newsItem);
                }
            }
        } catch (Exception e) {
            log.error("获取新闻失败", e);
        } finally {
            // 4. 关闭资源
            if (reader != null) {
                try { reader.close(); } catch (Exception ignored) {}
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return news;
    }

    /**
     * 获取热搜列表
     * 
     * 该方法从 https://api.xhus.cn/api/rweibo 获取热搜数据，
     * 接口返回为纯文本，每行一个热搜标题，格式如：
     * 1. 张子枫简直蜕变
     * 2. 老师因学生志愿未报清北解散群聊
     * ...
     * 
     * 每行去除序号后作为 HotSearchItem 的 title 字段，其他字段置为 null。
     * 
     * @return 热搜条目列表，每个条目仅包含标题
     */
    @Override
    public List<HotSearchItem> getHotSearches() {
        List<HotSearchItem> hot = new ArrayList<>();
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            // 1. 构建请求 URL
            String apiUrl = "https://api.xhus.cn/api/rweibo";
            URL url = new URL(apiUrl);

            // 2. 打开 HTTP 连接
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            // 3. 读取响应内容（纯文本，每行一个热搜标题）
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // 跳过空行
                if (!line.isEmpty()) {
                    // 只取标题部分，忽略前面的序号和点
                    String title = line.replaceFirst("^\\d+\\.\\s*", "");
                    HotSearchItem hotItem = new HotSearchItem();
                    hotItem.setTitle(title);
                    hotItem.setSummary(null);
                    hotItem.setUrl(null);
                    hotItem.setImage(null);
                    hot.add(hotItem);
                }
            }
        } catch (Exception e) {
            log.error("获取热搜失败", e);
        } finally {
            // 4. 关闭资源
            if (reader != null) {
                try { reader.close(); } catch (Exception ignored) {}
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return hot;
    }

    /**
     * 支持自动处理301/302/303/307/308重定向的GET请求
     * @param urlStr 原始URL
     * @param maxRedirects 最大重定向次数
     * @return 响应内容字符串
     */
    private String fetchWithRedirect(String urlStr, int maxRedirects) throws Exception {
        int redirects = 0;
        String currentUrl = urlStr;
        while (redirects < maxRedirects) {
            java.net.URL url = new java.net.URL(currentUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false); // 手动处理重定向
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int code = conn.getResponseCode();
            if (code == 301 || code == 302 || code == 303 || code == 307 || code == 308) {
                String location = conn.getHeaderField("Location");
                if (location == null) throw new RuntimeException("重定向无Location头");
                currentUrl = location;
                redirects++;
                continue;
            }
            if (code == 200) {
                return new String(conn.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
            throw new RuntimeException("请求失败，状态码: " + code);
        }
        throw new RuntimeException("重定向次数过多");
    }

    /**
     * 获取所有激活机器人所在城市的天气信息（全部返回）
     * @return List<WeatherInfo>（所有城市的天气信息）
     */
    public List<WeatherInfo> getWeather() {
        List<WeatherInfo> result = new ArrayList<>();
        try {
            List<com.myeden.entity.Robot> robots = robotRepository.findAll();
            Set<String> cityNames = new java.util.HashSet<>();
            for (com.myeden.entity.Robot robot : robots) {
                if (robot.getLocation() != null && !robot.getLocation().trim().isEmpty()) {
                    cityNames.add(robot.getLocation().trim());
                }
            }
            if (cityNames.isEmpty()) {
                log.warn("无可用机器人城市信息");
                return result;
            }
            List<CityCodeItem> cityCodeList = loadAllCityCodes();
            java.util.Map<String, String> cityNameToId = new java.util.HashMap<>();
            for (CityCodeItem item : cityCodeList) {
                if (item.getCountyname() != null && item.getAreaid() != null) {
                    cityNameToId.put(item.getCountyname(), item.getAreaid());
                }
            }
            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper objectMapper = new ObjectMapper();
            for (String name : cityNames) {
                String cityId = cityNameToId.get(name);
                if (cityId == null) continue;
                String url = "http://t.weather.itboy.net/api/weather/city/" + cityId;
                try {
                    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                    if (response.getStatusCodeValue() == 200) {
                        JsonNode root = objectMapper.readTree(response.getBody());
                        if (root.path("status").asInt() == 200) {
                            WeatherInfo info = new WeatherInfo();
                            info.setCity(name);
                            // 提取温度和天气描述
                            JsonNode dataNode = root.path("data");
                            String wendu = dataNode.path("wendu").asText();
                            String type = dataNode.path("forecast").isArray() && dataNode.path("forecast").size() > 0
                                ? dataNode.path("forecast").get(0).path("type").asText() : "";
                            info.setTemperature(wendu + "℃");
                            info.setDescription(type);
                            result.add(info);
                        }
                    }
                } catch (Exception ex) {
                    log.warn("获取城市天气失败:{} {}", name, ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("批量获取天气失败", e);
        }
        return result;
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

    /**
     * 城市编码实体类
     * 用于存储城市名称与对应的编码
     */
    public static class CityCodeItem {
        /** 区县名称 */
        private String countyname;
        /** 区县编码 */
        private String areaid;
        public String getCountyname() { return countyname; }
        public void setCountyname(String countyname) { this.countyname = countyname; }
        public String getAreaid() { return areaid; }
        public void setAreaid(String areaid) { this.areaid = areaid; }
    }

    /**
     * 从 classpath:/config/citycode.json 加载所有城市编码
     * 
     * @return 城市编码列表，每个元素包含 countyname 和 areaid
     */
    public List<CityCodeItem> loadAllCityCodes() {
        List<CityCodeItem> cityCodes = new ArrayList<>();
        if (cityCodes.size() > 0) {
            return cityCodes;
        }
        try {
            // 1. 通过ClassLoader读取资源，兼容本地和jar包
            ClassLoader classLoader = getClass().getClassLoader();
            java.io.InputStream is = classLoader.getResourceAsStream("config/citycode.json");
            if (is == null) {
                log.error("未找到城市编码文件: config/citycode.json");
                return cityCodes;
            }
            byte[] bytes = is.readAllBytes();
            String cityCodeJson = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);

            // 2. 解析JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(cityCodeJson);
            for (JsonNode item : root) {
                CityCodeItem codeItem = new CityCodeItem();
                codeItem.setCountyname(item.has("countyname") ? item.get("countyname").asText() : null);
                codeItem.setAreaid(item.has("areaid") ? item.get("areaid").asText() : null);
                cityCodes.add(codeItem);
            }
        } catch (Exception e) {
            log.error("加载城市编码列表失败", e);
        }
        return cityCodes;
    }
} 