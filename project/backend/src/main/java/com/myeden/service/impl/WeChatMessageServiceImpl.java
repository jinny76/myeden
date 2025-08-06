package com.myeden.service.impl;

import com.myeden.dto.wechat.WeChatMessage;
import com.myeden.service.DifyService;
import com.myeden.service.WeChatMessageService;
import com.myeden.service.DifyService.DifyChatResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 企业微信消息处理服务实现类
 */
@Service
public class WeChatMessageServiceImpl implements WeChatMessageService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeChatMessageServiceImpl.class);
    
    private final DifyService difyService;
    
    @Autowired
    public WeChatMessageServiceImpl(DifyService difyService) {
        this.difyService = difyService;
    }
    
    @Override
    public String processMessage(WeChatMessage message) {
        try {
            logger.info("开始处理微信消息: {}", message);
            
            if (message == null) {
                logger.warn("收到空的微信消息");
                return null;
            }
            
            String msgType = message.getMsgType();
            if (msgType == null) {
                logger.warn("微信消息类型为空");
                return null;
            }
            
            switch (msgType.toLowerCase()) {
                case "text":
                    return processTextMessage(message);
                case "event":
                    return processEventMessage(message);
                case "image":
                case "voice":
                case "video":
                case "file":
                    logger.info("收到媒体消息类型: {}，暂不处理", msgType);
                    return null;
                default:
                    logger.warn("收到未知消息类型: {}", msgType);
                    return null;
            }
            
        } catch (Exception e) {
            logger.error("处理微信消息时出现异常", e);
            return null;
        }
    }
    
    @Override
    public String processTextMessage(WeChatMessage message) {
        try {
            String content = message.getContent();
            if (content == null || content.trim().isEmpty()) {
                logger.warn("收到空的文本消息内容");
                return null;
            }
            
            logger.info("处理文本消息: 发送者={}, 内容={}", message.getFromUserName(), content);
            
            // 使用AI生成回复
            String aiReply = generateAIReply(content, message.getFromUserName());
            if (aiReply != null && !aiReply.trim().isEmpty()) {
                // 构建回复消息XML
                return buildTextReplyXml(message.getFromUserName(), message.getToUserName(), aiReply);
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("处理文本消息时出现异常", e);
            return null;
        }
    }
    
    @Override
    public String processEventMessage(WeChatMessage message) {
        try {
            String event = message.getEvent();
            if (event == null) {
                logger.warn("事件消息中事件类型为空");
                return null;
            }
            
            logger.info("处理事件消息: 事件类型={}, 发送者={}", event, message.getFromUserName());
            
            switch (event.toLowerCase()) {
                case "subscribe":
                    // 用户关注事件
                    String welcomeMsg = "欢迎关注我的伊甸园！\n" +
                                      "我是你的AI助手，可以和我聊天互动。\n" +
                                      "发送任何消息给我，我会智能回复哦~";
                    return buildTextReplyXml(message.getFromUserName(), message.getToUserName(), welcomeMsg);
                    
                case "unsubscribe":
                    // 用户取消关注事件
                    logger.info("用户{}取消关注", message.getFromUserName());
                    return null;
                    
                case "click":
                    // 菜单点击事件
                    String eventKey = message.getEventKey();
                    return handleMenuClickEvent(message, eventKey);
                    
                default:
                    logger.info("收到其他事件: {}", event);
                    return null;
            }
            
        } catch (Exception e) {
            logger.error("处理事件消息时出现异常", e);
            return null;
        }
    }
    
    @Override
    public String buildTextReplyXml(String toUserName, String fromUserName, String content) {
        long createTime = System.currentTimeMillis() / 1000;
        
        return String.format(
            "<xml>" +
            "<ToUserName><![CDATA[%s]]></ToUserName>" +
            "<FromUserName><![CDATA[%s]]></FromUserName>" +
            "<CreateTime>%d</CreateTime>" +
            "<MsgType><![CDATA[text]]></MsgType>" +
            "<Content><![CDATA[%s]]></Content>" +
            "</xml>",
            toUserName, fromUserName, createTime, content
        );
    }
    
    /**
     * 使用AI生成回复内容
     */
    private String generateAIReply(String userMessage, String userId) {
        try {
            // 构建AI对话提示词
            String prompt = String.format(
                "你是一个友好的AI助手，名叫伊甸园助手。请用温馨、自然的语气回复用户的消息。\n" +
                "用户消息：%s\n" +
                "请生成一个简洁、友好的回复（不超过200字）：",
                userMessage
            );
            
            // 调用Dify API生成回复
            DifyChatResult result = difyService.callDifyApi(prompt, userId, null);
            
            if (result != null && result.success && result.answer != null) {
                String reply = result.answer.trim();
                
                // 过滤和处理回复内容
                if (reply.length() > 300) {
                    reply = reply.substring(0, 297) + "...";
                }
                
                logger.info("AI生成回复: {}", reply);
                return reply;
            } else {
                logger.warn("AI生成回复失败: {}", result);
                return getDefaultReply();
            }
            
        } catch (Exception e) {
            logger.error("生成AI回复时出现异常", e);
            return getDefaultReply();
        }
    }
    
    /**
     * 处理菜单点击事件
     */
    private String handleMenuClickEvent(WeChatMessage message, String eventKey) {
        try {
            logger.info("处理菜单点击事件: eventKey={}", eventKey);
            
            String reply;
            switch (eventKey) {
                case "HELP":
                    reply = "🤖 伊甸园助手使用指南\n\n" +
                           "• 直接发送消息与我聊天\n" +
                           "• 我可以回答各种问题\n" +
                           "• 支持日常对话和知识咨询\n" +
                           "• 随时为你提供帮助！";
                    break;
                case "ABOUT":
                    reply = "🌟 关于我的伊甸园\n\n" +
                           "这是一个AI驱动的智能社交平台，" +
                           "致力于为用户提供温馨、智能的交流体验。\n\n" +
                           "在这里，你可以与AI助手自由对话，" +
                           "享受个性化的智能服务。";
                    break;
                default:
                    reply = "收到菜单点击：" + eventKey + "\n请发送消息与我聊天吧~";
                    break;
            }
            
            return buildTextReplyXml(message.getFromUserName(), message.getToUserName(), reply);
            
        } catch (Exception e) {
            logger.error("处理菜单点击事件时出现异常", e);
            return null;
        }
    }
    
    /**
     * 获取默认回复
     */
    private String getDefaultReply() {
        String[] defaultReplies = {
            "收到你的消息了，让我想想怎么回复~",
            "嗯，这是个有趣的话题，我需要一点时间思考。",
            "谢谢你的消息！我正在努力理解你的意思。",
            "你说得很有道理，我再想想如何回复。",
            "感谢分享！我正在组织语言回复你。"
        };
        
        int index = (int) (Math.random() * defaultReplies.length);
        return defaultReplies[index];
    }
}