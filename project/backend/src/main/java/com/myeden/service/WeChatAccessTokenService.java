package com.myeden.service;

/**
 * 企业微信Access Token管理服务接口
 */
public interface WeChatAccessTokenService {
    
    /**
     * 获取有效的Access Token
     * 
     * @return Access Token字符串，获取失败返回null
     */
    String getAccessToken();
    
    /**
     * 强制刷新Access Token
     * 
     * @return 新的Access Token字符串，获取失败返回null
     */
    String refreshAccessToken();
    
    /**
     * 检查Access Token是否有效
     * 
     * @param accessToken 要检查的Access Token
     * @return true表示有效，false表示无效
     */
    boolean isTokenValid(String accessToken);
}