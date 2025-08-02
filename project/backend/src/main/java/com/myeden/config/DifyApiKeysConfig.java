package com.myeden.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Dify API Keys配置类
 * 管理不同功能的Dify API密钥
 * 
 * @author MyEden Team
 * @version 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "dify")
public class DifyApiKeysConfig {
    
    /**
     * 图片处理API密钥
     */
    private String imageApiKey;
    
    /**
     * 数据处理API密钥
     */
    private String dataApiKey;
    
    /**
     * 翻译功能API密钥
     */
    private String translationApiKey;
    
    // Getters and Setters
    public String getImageApiKey() {
        return imageApiKey;
    }
    
    public void setImageApiKey(String imageApiKey) {
        this.imageApiKey = imageApiKey;
    }
    
    public String getDataApiKey() {
        return dataApiKey;
    }
    
    public void setDataApiKey(String dataApiKey) {
        this.dataApiKey = dataApiKey;
    }
    
    public String getTranslationApiKey() {
        return translationApiKey;
    }
    
    public void setTranslationApiKey(String translationApiKey) {
        this.translationApiKey = translationApiKey;
    }
} 