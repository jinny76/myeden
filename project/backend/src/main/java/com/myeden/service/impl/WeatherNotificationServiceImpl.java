package com.myeden.service.impl;

import com.myeden.config.TaskProperties;
import com.myeden.model.external.WeatherInfo;
import com.myeden.service.ExternalDataService;
import com.myeden.service.WeatherNotificationService;
import com.myeden.service.WeChatSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 天气通知服务实现类
 */
@Service
public class WeatherNotificationServiceImpl implements WeatherNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherNotificationServiceImpl.class);

    @Autowired
    private TaskProperties taskProperties;

    @Autowired
    private ExternalDataService externalDataService;

    @Autowired(required = false)
    private WeChatSendService weChatSendService;

    @Override
    public int pushWeatherToWechat(final List<String> cities, List<String> users) {
        try {
            if (weChatSendService == null) {
                logger.warn("微信发送服务未启用，无法推送天气信息");
                return 0;
            }

            if (users == null || users.isEmpty()) {
                logger.warn("用户列表为空，使用配置中的默认用户");
                users = taskProperties.getWeather().getUsers();
            }

            if (cities == null || cities.isEmpty() || users == null || users.isEmpty()) {
                logger.error("城市列表或用户列表为空，无法推送天气信息");
                return 0;
            }

            // 获取天气信息
            List<WeatherInfo> weatherInfoList = externalDataService.getWeather();
            if (weatherInfoList == null || weatherInfoList.isEmpty()) {
                logger.error("获取天气信息失败，无法推送");
                return 0;
            }

            // 过滤出指定城市的天气信息
            List<WeatherInfo> filteredWeather = weatherInfoList.stream()
                    .filter(weather -> cities.contains(weather.getCity()))
                    .collect(Collectors.toList());

            if (filteredWeather.isEmpty()) {
                logger.warn("没有找到指定城市的天气信息，城市列表: {}", cities);
                return 0;
            }

            String sendMode = taskProperties.getWeather().getSendMode();
            int successCount = 0;

            if ("single".equals(sendMode)) {
                // 单独发送模式：每个城市单独发送
                successCount = sendWeatherSeparately(filteredWeather, users);
            } else {
                // 合并发送模式：多个城市合并发送
                successCount = sendWeatherCombined(filteredWeather, users);
            }

            logger.info("天气预报推送完成，成功推送 {} 条消息", successCount);
            return successCount;

        } catch (Exception e) {
            logger.error("推送天气预报到微信时发生异常", e);
            return 0;
        }
    }

    @Override
    public int pushCityWeatherToAllUsers(String city) {
        List<String> cities = List.of(city);
        List<String> users = taskProperties.getWeather().getUsers();
        return pushWeatherToWechat(cities, users);
    }

    @Override
    public int pushAllCityWeatherToUser(String user) {
        List<String> cities = taskProperties.getWeather().getCities();
        List<String> users = List.of(user);
        return pushWeatherToWechat(cities, users);
    }

    @Override
    public boolean isWeatherTaskEnabled() {
        return taskProperties.getWeather().isEnabled();
    }

    @Override
    public void setWeatherTaskEnabled(boolean enabled) {
        taskProperties.getWeather().setEnabled(enabled);
        logger.info("天气推送任务状态已更新: {}", enabled ? "启用" : "禁用");
    }

    /**
     * 单独发送模式：每个城市单独发送
     */
    private int sendWeatherSeparately(List<WeatherInfo> weatherList, List<String> users) {
        int successCount = 0;
        String template = taskProperties.getWeather().getMessageTemplate();

        for (WeatherInfo weather : weatherList) {
            String message = formatWeatherMessage(template, weather);
            
            for (String user : users) {
                try {
                    boolean success = weChatSendService.sendTextMessage(user, message).isSuccess();
                    if (success) {
                        successCount++;
                        logger.debug("成功发送天气信息给用户 {} (城市: {})", user, weather.getCity());
                    } else {
                        logger.warn("发送天气信息失败，用户: {}, 城市: {}", user, weather.getCity());
                    }
                } catch (Exception e) {
                    logger.error("发送天气信息给用户 {} 时发生异常 (城市: {})", user, weather.getCity(), e);
                }
            }
        }

        return successCount;
    }

    /**
     * 合并发送模式：多个城市合并发送
     */
    private int sendWeatherCombined(List<WeatherInfo> weatherList, List<String> users) {
        int successCount = 0;
        String combinedTemplate = taskProperties.getWeather().getCombinedTemplate();
        String singleTemplate = taskProperties.getWeather().getMessageTemplate();

        // 构建天气列表字符串
        StringBuilder weatherListBuilder = new StringBuilder();
        for (int i = 0; i < weatherList.size(); i++) {
            WeatherInfo weather = weatherList.get(i);
            String weatherItem = formatWeatherMessage(singleTemplate, weather);
            // 去掉单个模板的标题和结尾祝福语，只保留核心信息
            weatherItem = extractCoreWeatherInfo(weatherItem, weather);
            weatherListBuilder.append(weatherItem);
            if (i < weatherList.size() - 1) {
                weatherListBuilder.append("\n");
            }
        }

        String combinedMessage = combinedTemplate.replace("{weather-list}", weatherListBuilder.toString());

        for (String user : users) {
            try {
                boolean success = weChatSendService.sendTextMessage(user, combinedMessage).isSuccess();
                if (success) {
                    successCount++;
                    logger.debug("成功发送合并天气信息给用户 {} ({}个城市)", user, weatherList.size());
                } else {
                    logger.warn("发送合并天气信息失败，用户: {}", user);
                }
            } catch (Exception e) {
                logger.error("发送合并天气信息给用户 {} 时发生异常", user, e);
            }
        }

        return successCount;
    }

    /**
     * 格式化天气消息
     */
    private String formatWeatherMessage(String template, WeatherInfo weather) {
        return template
                .replace("{city}", weather.getCity() != null ? weather.getCity() : "未知")
                .replace("{temperature}", weather.getTemperature() != null ? weather.getTemperature() : "未知")
                .replace("{description}", weather.getDescription() != null ? weather.getDescription() : "未知");
    }

    /**
     * 提取核心天气信息（用于合并模式）
     */
    private String extractCoreWeatherInfo(String fullMessage, WeatherInfo weather) {
        return String.format("📍 %s: %s, %s",
                weather.getCity() != null ? weather.getCity() : "未知",
                weather.getTemperature() != null ? weather.getTemperature() : "未知温度",
                weather.getDescription() != null ? weather.getDescription() : "未知天气");
    }
}