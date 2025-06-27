package com.myeden.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Dify API配置类
 * 管理Dify API的连接参数和配置信息
 * 
 * @author MyEden Team
 * @version 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "dify.api")
public class DifyConfig {
    
    /**
     * Dify API基础URL
     */
    private String url = "https://api.dify.ai/v1";
    
    /**
     * Dify API密钥
     */
    private String key;
    
    /**
     * API调用超时时间（毫秒）
     */
    private int timeout = 30000;
    
    /**
     * 最大重试次数
     */
    private int maxRetries = 3;
    
    /**
     * 重试间隔（毫秒）
     */
    private int retryInterval = 1000;
    
    /**
     * 是否启用API调用
     */
    private boolean enabled = true;
    
    /**
     * 每日API调用限制
     */
    private int dailyLimit = 1000;
    
    /**
     * 每小时API调用限制
     */
    private int hourlyLimit = 100;
    
    // Getters and Setters
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public int getRetryInterval() {
        return retryInterval;
    }
    
    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getDailyLimit() {
        return dailyLimit;
    }
    
    public void setDailyLimit(int dailyLimit) {
        this.dailyLimit = dailyLimit;
    }
    
    public int getHourlyLimit() {
        return hourlyLimit;
    }
    
    public void setHourlyLimit(int hourlyLimit) {
        this.hourlyLimit = hourlyLimit;
    }
} 