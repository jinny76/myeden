package com.myeden.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 企业微信配置属性
 */
@Component
@ConfigurationProperties(prefix = "wechat.work")
public class WeChatWorkProperties {
    
    /**
     * 回调Token
     */
    private String token;
    
    /**
     * 消息加密密钥
     */
    private String encodingAesKey;
    
    /**
     * 企业ID
     */
    private String corpId;
    
    /**
     * 应用ID
     */
    private String agentId;
    
    /**
     * 应用密钥
     */
    private String secret;
    
    /**
     * 是否启用微信消息接收
     */
    private boolean enabled = false;
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getEncodingAesKey() {
        return encodingAesKey;
    }
    
    public void setEncodingAesKey(String encodingAesKey) {
        this.encodingAesKey = encodingAesKey;
    }
    
    public String getCorpId() {
        return corpId;
    }
    
    public void setCorpId(String corpId) {
        this.corpId = corpId;
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}