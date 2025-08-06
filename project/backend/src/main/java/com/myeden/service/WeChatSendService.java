package com.myeden.service;

import com.myeden.dto.wechat.WeChatSendMessageRequest;
import com.myeden.dto.wechat.WeChatSendMessageResponse;

/**
 * 企业微信消息发送服务接口
 */
public interface WeChatSendService {
    
    /**
     * 发送文本消息
     * 
     * @param toUser 接收用户ID，多个用|分隔，@all表示全员
     * @param content 消息内容
     * @return 发送结果
     */
    WeChatSendMessageResponse sendTextMessage(String toUser, String content);
    
    /**
     * 发送文本消息到部门
     * 
     * @param toParty 接收部门ID，多个用|分隔
     * @param content 消息内容
     * @return 发送结果
     */
    WeChatSendMessageResponse sendTextMessageToParty(String toParty, String content);
    
    /**
     * 发送文本消息到标签组
     * 
     * @param toTag 接收标签ID，多个用|分隔
     * @param content 消息内容
     * @return 发送结果
     */
    WeChatSendMessageResponse sendTextMessageToTag(String toTag, String content);
    
    /**
     * 发送消息（通用方法）
     * 
     * @param request 消息发送请求对象
     * @return 发送结果
     */
    WeChatSendMessageResponse sendMessage(WeChatSendMessageRequest request);
    
    /**
     * 发送全员通知
     * 
     * @param content 消息内容
     * @return 发送结果
     */
    WeChatSendMessageResponse sendNotificationToAll(String content);
    
    /**
     * 发送图片消息
     * 
     * @param toUser 接收用户ID
     * @param mediaId 图片媒体文件ID
     * @return 发送结果
     */
    WeChatSendMessageResponse sendImageMessage(String toUser, String mediaId);
    
    /**
     * 发送语音消息
     * 
     * @param toUser 接收用户ID
     * @param mediaId 语音媒体文件ID
     * @return 发送结果
     */
    WeChatSendMessageResponse sendVoiceMessage(String toUser, String mediaId);
    
    /**
     * 发送视频消息
     * 
     * @param toUser 接收用户ID
     * @param mediaId 视频媒体文件ID
     * @param title 视频标题
     * @param description 视频描述
     * @return 发送结果
     */
    WeChatSendMessageResponse sendVideoMessage(String toUser, String mediaId, String title, String description);
    
    /**
     * 发送文件消息
     * 
     * @param toUser 接收用户ID
     * @param mediaId 文件媒体文件ID
     * @return 发送结果
     */
    WeChatSendMessageResponse sendFileMessage(String toUser, String mediaId);
    
    /**
     * 发送图文消息
     * 
     * @param toUser 接收用户ID
     * @param articles 图文消息数组
     * @return 发送结果
     */
    WeChatSendMessageResponse sendNewsMessage(String toUser, WeChatSendMessageRequest.NewsContent.Article[] articles);
    
    /**
     * 发送Markdown消息
     * 
     * @param toUser 接收用户ID
     * @param content Markdown内容
     * @return 发送结果
     */
    WeChatSendMessageResponse sendMarkdownMessage(String toUser, String content);
}