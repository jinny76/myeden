package com.myeden.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Coze API配置属性
 */
@Component
@ConfigurationProperties(prefix = "coze.api")
public class CozeProperties {

    private String baseUrl = "https://api.coze.cn";
    private String key;
    private boolean enabled = true;
    
    private Timeout timeout = new Timeout();
    private Retry retry = new Retry();
    private RateLimit rateLimit = new RateLimit();
    private Defaults defaults = new Defaults();
    private FileConfig file = new FileConfig();
    private Monitoring monitoring = new Monitoring();

    // Getters and Setters
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Timeout getTimeout() {
        return timeout;
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public Defaults getDefaults() {
        return defaults;
    }

    public void setDefaults(Defaults defaults) {
        this.defaults = defaults;
    }

    public FileConfig getFile() {
        return file;
    }

    public void setFile(FileConfig file) {
        this.file = file;
    }

    public Monitoring getMonitoring() {
        return monitoring;
    }

    public void setMonitoring(Monitoring monitoring) {
        this.monitoring = monitoring;
    }

    /**
     * 超时配置
     */
    public static class Timeout {
        private int connect = 10000;
        private int read = 30000;
        private int write = 10000;

        public int getConnect() {
            return connect;
        }

        public void setConnect(int connect) {
            this.connect = connect;
        }

        public int getRead() {
            return read;
        }

        public void setRead(int read) {
            this.read = read;
        }

        public int getWrite() {
            return write;
        }

        public void setWrite(int write) {
            this.write = write;
        }
    }

    /**
     * 重试配置
     */
    public static class Retry {
        private int maxAttempts = 3;
        private long delay = 1000;
        private double backoffMultiplier = 2.0;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getDelay() {
            return delay;
        }

        public void setDelay(long delay) {
            this.delay = delay;
        }

        public double getBackoffMultiplier() {
            return backoffMultiplier;
        }

        public void setBackoffMultiplier(double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
        }
    }

    /**
     * 速率限制配置
     */
    public static class RateLimit {
        private int requestsPerMinute = 60;
        private int requestsPerDay = 1000;

        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public void setRequestsPerMinute(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }

        public int getRequestsPerDay() {
            return requestsPerDay;
        }

        public void setRequestsPerDay(int requestsPerDay) {
            this.requestsPerDay = requestsPerDay;
        }
    }

    /**
     * 默认配置
     */
    public static class Defaults {
        private String botId;
        private String userId = "default-user";
        private String chatMode = "blocking";
        private boolean autoSaveHistory = true;
        private int maxTokens = 2048;

        public String getBotId() {
            return botId;
        }

        public void setBotId(String botId) {
            this.botId = botId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getChatMode() {
            return chatMode;
        }

        public void setChatMode(String chatMode) {
            this.chatMode = chatMode;
        }

        public boolean isAutoSaveHistory() {
            return autoSaveHistory;
        }

        public void setAutoSaveHistory(boolean autoSaveHistory) {
            this.autoSaveHistory = autoSaveHistory;
        }

        public int getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }
    }

    /**
     * 文件配置
     */
    public static class FileConfig {
        private List<String> supportedTypes;
        private long maxSize = 52428800; // 50MB
        private int uploadTimeout = 60;

        public List<String> getSupportedTypes() {
            return supportedTypes;
        }

        public void setSupportedTypes(List<String> supportedTypes) {
            this.supportedTypes = supportedTypes;
        }

        public long getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(long maxSize) {
            this.maxSize = maxSize;
        }

        public int getUploadTimeout() {
            return uploadTimeout;
        }

        public void setUploadTimeout(int uploadTimeout) {
            this.uploadTimeout = uploadTimeout;
        }
    }

    /**
     * 监控配置
     */
    public static class Monitoring {
        private boolean metricsEnabled = true;
        private String logLevel = "INFO";
        private boolean logRequestResponse = false;

        public boolean isMetricsEnabled() {
            return metricsEnabled;
        }

        public void setMetricsEnabled(boolean metricsEnabled) {
            this.metricsEnabled = metricsEnabled;
        }

        public String getLogLevel() {
            return logLevel;
        }

        public void setLogLevel(String logLevel) {
            this.logLevel = logLevel;
        }

        public boolean isLogRequestResponse() {
            return logRequestResponse;
        }

        public void setLogRequestResponse(boolean logRequestResponse) {
            this.logRequestResponse = logRequestResponse;
        }
    }
}