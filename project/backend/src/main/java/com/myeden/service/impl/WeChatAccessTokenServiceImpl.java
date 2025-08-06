package com.myeden.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeden.config.WeChatWorkProperties;
import com.myeden.service.WeChatAccessTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 企业微信Access Token管理服务实现类
 */
@Service
public class WeChatAccessTokenServiceImpl implements WeChatAccessTokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeChatAccessTokenServiceImpl.class);
    
    private static final String GET_TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
    
    private final WeChatWorkProperties weChatProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ReentrantLock tokenLock = new ReentrantLock();
    
    // Token缓存
    private String cachedAccessToken;
    private LocalDateTime tokenExpiryTime;
    
    @Autowired
    public WeChatAccessTokenServiceImpl(WeChatWorkProperties weChatProperties) {
        this.weChatProperties = weChatProperties;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public String getAccessToken() {
        if (!weChatProperties.isEnabled()) {
            logger.warn("企业微信功能未启用");
            return null;
        }
        
        tokenLock.lock();
        try {
            // 检查缓存的token是否仍然有效
            if (cachedAccessToken != null && tokenExpiryTime != null && 
                LocalDateTime.now().isBefore(tokenExpiryTime.minusMinutes(5))) {
                return cachedAccessToken;
            }
            
            // Token即将过期或不存在，重新获取
            return refreshAccessToken();
            
        } finally {
            tokenLock.unlock();
        }
    }
    
    @Override
    public String refreshAccessToken() {
        if (!weChatProperties.isEnabled()) {
            logger.warn("企业微信功能未启用");
            return null;
        }
        
        tokenLock.lock();
        try {
            logger.info("开始获取企业微信Access Token");
            
            String url = String.format("%s?corpid=%s&corpsecret=%s", 
                                     GET_TOKEN_URL, 
                                     weChatProperties.getCorpId(), 
                                     weChatProperties.getSecret());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                int errcode = jsonNode.get("errcode").asInt();
                if (errcode == 0) {
                    cachedAccessToken = jsonNode.get("access_token").asText();
                    int expiresIn = jsonNode.get("expires_in").asInt();
                    tokenExpiryTime = LocalDateTime.now().plusSeconds(expiresIn);
                    
                    logger.info("成功获取企业微信Access Token，有效期: {}秒", expiresIn);
                    return cachedAccessToken;
                } else {
                    String errmsg = jsonNode.get("errmsg").asText();
                    logger.error("获取企业微信Access Token失败: errcode={}, errmsg={}", errcode, errmsg);
                }
            } else {
                logger.error("调用企业微信API失败: status={}", response.getStatusCode());
            }
            
            // 清除无效的缓存
            cachedAccessToken = null;
            tokenExpiryTime = null;
            return null;
            
        } catch (Exception e) {
            logger.error("获取企业微信Access Token时出现异常", e);
            cachedAccessToken = null;
            tokenExpiryTime = null;
            return null;
        } finally {
            tokenLock.unlock();
        }
    }
    
    @Override
    public boolean isTokenValid(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 通过调用一个简单的API来验证token是否有效
            String url = String.format("https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token=%s&userid=test", 
                                     accessToken);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                int errcode = jsonNode.get("errcode").asInt();
                
                // 40014: 不合法的access_token
                // 42001: access_token超时
                return errcode != 40014 && errcode != 42001;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.warn("验证Access Token时出现异常", e);
            return false;
        }
    }
}