package com.myeden.service;

import com.myeden.dto.wechat.WeChatMessage;

/**
 * 企业微信异步消息处理服务接口
 */
public interface WeChatAsyncProcessService {
    
    /**
     * 异步处理微信消息
     * 
     * @param message 微信消息对象
     */
    void processMessageAsync(WeChatMessage message);
    
    /**
     * 异步处理文本消息并生成智能回复
     * 
     * @param message 微信文本消息
     */
    void processTextMessageAsync(WeChatMessage message);
    
    /**
     * 异步处理事件消息
     * 
     * @param message 微信事件消息
     */
    void processEventMessageAsync(WeChatMessage message);
}