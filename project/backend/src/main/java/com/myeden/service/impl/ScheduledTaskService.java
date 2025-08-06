package com.myeden.service.impl;

import com.myeden.config.TaskProperties;
import com.myeden.service.WeatherNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 定时任务服务
 */
@Service
public class ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    @Autowired
    private TaskProperties taskProperties;

    @Autowired
    private WeatherNotificationService weatherNotificationService;

    /**
     * 天气预报推送定时任务
     * 根据配置的cron表达式执行
     */
    @Scheduled(cron = "${task.weather.cron:0 15 7 * * ?}")
    public void weatherNotificationTask() {
        try {
            if (!taskProperties.getWeather().isEnabled()) {
                logger.debug("天气预报推送任务已禁用，跳过执行");
                return;
            }

            logger.info("开始执行天气预报推送定时任务");

            TaskProperties.Weather weatherConfig = taskProperties.getWeather();
            int successCount = weatherNotificationService.pushWeatherToWechat(
                    weatherConfig.getCities(),
                    weatherConfig.getUsers()
            );

            if (successCount > 0) {
                logger.info("天气预报推送定时任务执行完成，成功推送 {} 条消息", successCount);
            } else {
                logger.warn("天气预报推送定时任务执行完成，但没有成功推送任何消息");
            }

        } catch (Exception e) {
            logger.error("天气预报推送定时任务执行失败", e);
        }
    }

    /**
     * 手动触发天气预报推送任务
     * 供测试接口调用
     */
    public int triggerWeatherNotificationTask() {
        try {
            logger.info("手动触发天气预报推送任务");

            TaskProperties.Weather weatherConfig = taskProperties.getWeather();
            int successCount = weatherNotificationService.pushWeatherToWechat(
                    weatherConfig.getCities(),
                    weatherConfig.getUsers()
            );

            logger.info("手动触发天气预报推送任务完成，成功推送 {} 条消息", successCount);
            return successCount;

        } catch (Exception e) {
            logger.error("手动触发天气预报推送任务失败", e);
            return 0;
        }
    }

    /**
     * 获取任务执行状态
     */
    public TaskStatus getTaskStatus() {
        TaskStatus status = new TaskStatus();
        status.setWeatherTaskEnabled(taskProperties.getWeather().isEnabled());
        status.setWeatherCron(taskProperties.getWeather().getCron());
        status.setWeatherCitiesCount(taskProperties.getWeather().getCities() != null ? 
                taskProperties.getWeather().getCities().size() : 0);
        status.setWeatherUsersCount(taskProperties.getWeather().getUsers() != null ? 
                taskProperties.getWeather().getUsers().size() : 0);
        return status;
    }

    /**
     * 任务状态信息
     */
    public static class TaskStatus {
        private boolean weatherTaskEnabled;
        private String weatherCron;
        private int weatherCitiesCount;
        private int weatherUsersCount;

        public boolean isWeatherTaskEnabled() {
            return weatherTaskEnabled;
        }

        public void setWeatherTaskEnabled(boolean weatherTaskEnabled) {
            this.weatherTaskEnabled = weatherTaskEnabled;
        }

        public String getWeatherCron() {
            return weatherCron;
        }

        public void setWeatherCron(String weatherCron) {
            this.weatherCron = weatherCron;
        }

        public int getWeatherCitiesCount() {
            return weatherCitiesCount;
        }

        public void setWeatherCitiesCount(int weatherCitiesCount) {
            this.weatherCitiesCount = weatherCitiesCount;
        }

        public int getWeatherUsersCount() {
            return weatherUsersCount;
        }

        public void setWeatherUsersCount(int weatherUsersCount) {
            this.weatherUsersCount = weatherUsersCount;
        }
    }
}