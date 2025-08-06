package com.myeden.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时任务配置属性
 */
@Component
@ConfigurationProperties(prefix = "task")
public class TaskProperties {

    /**
     * 线程池配置
     */
    private Pool pool = new Pool();
    
    /**
     * 天气预报推送任务配置
     */
    private Weather weather = new Weather();

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    /**
     * 线程池配置
     */
    public static class Pool {
        private int coreSize = 5;
        private int maxSize = 10;
        private int keepAlive = 60;
        private int queueCapacity = 100;
        private String threadNamePrefix = "task-";

        public int getCoreSize() {
            return coreSize;
        }

        public void setCoreSize(int coreSize) {
            this.coreSize = coreSize;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }

        public int getKeepAlive() {
            return keepAlive;
        }

        public void setKeepAlive(int keepAlive) {
            this.keepAlive = keepAlive;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        public String getThreadNamePrefix() {
            return threadNamePrefix;
        }

        public void setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }
    }

    /**
     * 天气预报推送任务配置
     */
    public static class Weather {
        private boolean enabled = true;
        private String cron = "0 15 7 * * ?";
        private List<String> cities;
        private List<String> users;
        private String messageTemplate;
        private String sendMode = "combined";
        private String combinedTemplate;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }

        public List<String> getCities() {
            return cities;
        }

        public void setCities(List<String> cities) {
            this.cities = cities;
        }

        public List<String> getUsers() {
            return users;
        }

        public void setUsers(List<String> users) {
            this.users = users;
        }

        public String getMessageTemplate() {
            return messageTemplate;
        }

        public void setMessageTemplate(String messageTemplate) {
            this.messageTemplate = messageTemplate;
        }

        public String getSendMode() {
            return sendMode;
        }

        public void setSendMode(String sendMode) {
            this.sendMode = sendMode;
        }

        public String getCombinedTemplate() {
            return combinedTemplate;
        }

        public void setCombinedTemplate(String combinedTemplate) {
            this.combinedTemplate = combinedTemplate;
        }
    }
}