package com.myeden.service;

import com.myeden.dto.wechat.WeChatMessage;

/**
 * 企业微信消息处理服务接口
 */
public interface WeChatMessageService {
    
    /**
     * 处理接收到的微信消息
     * 
     * @param message 微信消息对象
     * @return 回复消息的XML字符串，如果不需要回复则返回null
     */
    String processMessage(WeChatMessage message);
    
    /**
     * 处理文本消息
     * 
     * @param message 微信消息对象
     * @return 回复消息的XML字符串，如果不需要回复则返回null
     */
    String processTextMessage(WeChatMessage message);
    
    /**
     * 处理事件消息
     * 
     * @param message 微信消息对象
     * @return 回复消息的XML字符串，如果不需要回复则返回null
     */
    String processEventMessage(WeChatMessage message);
    
    /**
     * 构建文本回复消息XML
     * 
     * @param toUserName 接收方用户名
     * @param fromUserName 发送方用户名
     * @param content 回复内容
     * @return 回复消息的XML字符串
     */
    String buildTextReplyXml(String toUserName, String fromUserName, String content);
}