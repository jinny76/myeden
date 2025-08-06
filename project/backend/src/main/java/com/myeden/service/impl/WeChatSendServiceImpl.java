package com.myeden.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeden.config.WeChatWorkProperties;
import com.myeden.dto.wechat.WeChatSendMessageRequest;
import com.myeden.dto.wechat.WeChatSendMessageResponse;
import com.myeden.service.WeChatAccessTokenService;
import com.myeden.service.WeChatSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 企业微信消息发送服务实现类
 */
@Service
public class WeChatSendServiceImpl implements WeChatSendService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeChatSendServiceImpl.class);
    
    private static final String SEND_MESSAGE_URL = "https://qyapi.weixin.qq.com/cgi-bin/message/send";
    
    private final WeChatWorkProperties weChatProperties;
    private final WeChatAccessTokenService accessTokenService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public WeChatSendServiceImpl(WeChatWorkProperties weChatProperties,
                                WeChatAccessTokenService accessTokenService) {
        this.weChatProperties = weChatProperties;
        this.accessTokenService = accessTokenService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public WeChatSendMessageResponse sendTextMessage(String toUser, String content) {
        if (!weChatProperties.isEnabled()) {
            logger.warn("企业微信功能未启用");
            return createErrorResponse(-1, "企业微信功能未启用");
        }
        
        WeChatSendMessageRequest request = new WeChatSendMessageRequest();
        request.setToUser(toUser);
        request.setMsgType("text");
        request.setAgentId(weChatProperties.getAgentId());
        request.setText(new WeChatSendMessageRequest.TextContent(content));
        
        return sendMessage(request);
    }
    
    @Override
    public WeChatSendMessageResponse sendTextMessageToParty(String toParty, String content) {
        if (!weChatProperties.isEnabled()) {
            logger.warn("企业微信功能未启用");
            return createErrorResponse(-1, "企业微信功能未启用");
        }
        
        WeChatSendMessageRequest request = new WeChatSendMessageRequest();
        request.setToParty(toParty);
        request.setMsgType("text");
        request.setAgentId(weChatProperties.getAgentId());
        request.setText(new WeChatSendMessageRequest.TextContent(content));
        
        return sendMessage(request);
    }
    
    @Override
    public WeChatSendMessageResponse sendTextMessageToTag(String toTag, String content) {
        if (!weChatProperties.isEnabled()) {
            logger.warn("企业微信功能未启用");
            return createErrorResponse(-1, "企业微信功能未启用");
        }
        
        WeChatSendMessageRequest request = new WeChatSendMessageRequest();
        request.setToTag(toTag);
        request.setMsgType("text");
        request.setAgentId(weChatProperties.getAgentId());
        request.setText(new WeChatSendMessageRequest.TextContent(content));
        
        return sendMessage(request);
    }
    
    @Override
    public WeChatSendMessageResponse sendMessage(WeChatSendMessageRequest request) {
        try {
            if (!weChatProperties.isEnabled()) {
                logger.warn("企业微信功能未启用");
                return createErrorResponse(-1, "企业微信功能未启用");
            }
            
            // 获取Access Token
            String accessToken = accessTokenService.getAccessToken();
            if (accessToken == null || accessToken.trim().isEmpty()) {
                logger.error("获取Access Token失败");
                return createErrorResponse(-2, "获取Access Token失败");
            }
            
            // 构建请求URL
            String url = SEND_MESSAGE_URL + "?access_token=" + accessToken;
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("User-Agent", "MyEden-WeChat-Client/1.0");
            
            // 转换请求体为JSON
            String jsonBody = objectMapper.writeValueAsString(request);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            
            logger.info("发送企业微信消息: toUser={}, content={}", 
                       request.getToUser(), 
                       request.getText() != null ? request.getText().getContent() : "");
            logger.debug("请求URL: {}", url);
            logger.debug("请求体: {}", jsonBody);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                WeChatSendMessageResponse result = objectMapper.readValue(response.getBody(), WeChatSendMessageResponse.class);
                
                if (result.isSuccess()) {
                    logger.info("企业微信消息发送成功: msgId={}", result.getMsgId());
                } else {
                    logger.error("企业微信消息发送失败: errCode={}, errMsg={}", 
                               result.getErrCode(), result.getErrMsg());
                    
                    // 如果是token过期，尝试刷新token并重试一次
                    if (result.getErrCode() != null && 
                        (result.getErrCode() == 40014 || result.getErrCode() == 42001)) {
                        logger.info("Access Token过期，尝试刷新并重试");
                        accessToken = accessTokenService.refreshAccessToken();
                        if (accessToken != null) {
                            return retryWithNewToken(request, accessToken);
                        }
                    }
                }
                
                return result;
            } else {
                logger.error("调用企业微信发送消息API失败: status={}", response.getStatusCode());
                return createErrorResponse(-3, "调用企业微信API失败");
            }
            
        } catch (Exception e) {
            logger.error("发送企业微信消息时出现异常", e);
            return createErrorResponse(-4, "发送消息时出现异常: " + e.getMessage());
        }
    }
    
    @Override
    public WeChatSendMessageResponse sendNotificationToAll(String content) {
        return sendTextMessage("@all", content);
    }
    
    /**
     * 使用新的Access Token重试发送消息
     */
    private WeChatSendMessageResponse retryWithNewToken(WeChatSendMessageRequest request, String accessToken) {
        try {
            String url = SEND_MESSAGE_URL + "?access_token=" + accessToken;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("User-Agent", "MyEden-WeChat-Client/1.0");
            
            String jsonBody = objectMapper.writeValueAsString(request);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            
            logger.info("使用新Access Token重试发送消息");
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                WeChatSendMessageResponse result = objectMapper.readValue(response.getBody(), WeChatSendMessageResponse.class);
                
                if (result.isSuccess()) {
                    logger.info("重试发送成功: msgId={}", result.getMsgId());
                } else {
                    logger.error("重试发送失败: errCode={}, errMsg={}", 
                               result.getErrCode(), result.getErrMsg());
                }
                
                return result;
            } else {
                logger.error("重试调用企业微信API失败: status={}", response.getStatusCode());
                return createErrorResponse(-3, "重试调用企业微信API失败");
            }
            
        } catch (Exception e) {
            logger.error("重试发送消息时出现异常", e);
            return createErrorResponse(-4, "重试发送消息时出现异常: " + e.getMessage());
        }
    }
    
    /**
     * 创建错误响应
     */
    private WeChatSendMessageResponse createErrorResponse(int errCode, String errMsg) {
        WeChatSendMessageResponse response = new WeChatSendMessageResponse();
        response.setErrCode(errCode);
        response.setErrMsg(errMsg);
        return response;
    }
}