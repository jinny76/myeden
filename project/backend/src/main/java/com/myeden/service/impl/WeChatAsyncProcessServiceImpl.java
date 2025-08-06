package com.myeden.service.impl;

import com.myeden.dto.wechat.WeChatMessage;
import com.myeden.dto.wechat.WeChatSendMessageResponse;
import com.myeden.service.DifyService;
import com.myeden.service.DifyService.DifyChatResult;
import com.myeden.service.WeChatAsyncProcessService;
import com.myeden.service.WeChatSendService;
import com.myeden.service.WeChatConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 企业微信异步消息处理服务实现类
 */
@Service
public class WeChatAsyncProcessServiceImpl implements WeChatAsyncProcessService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeChatAsyncProcessServiceImpl.class);
    
    private final DifyService difyService;
    private final WeChatSendService weChatSendService;
    private final WeChatConversationService conversationService;
    
    @Autowired
    public WeChatAsyncProcessServiceImpl(DifyService difyService, WeChatSendService weChatSendService, WeChatConversationService conversationService) {
        this.difyService = difyService;
        this.weChatSendService = weChatSendService;
        this.conversationService = conversationService;
    }
    
    @Override
    @Async("weChatAsyncExecutor")
    public void processMessageAsync(WeChatMessage message) {
        try {
            logger.info("开始异步处理微信消息: {}", message);
            
            if (message == null) {
                logger.warn("收到空的微信消息，跳过处理");
                return;
            }
            
            String msgType = message.getMsgType();
            if (msgType == null) {
                logger.warn("微信消息类型为空，跳过处理");
                return;
            }
            
            switch (msgType.toLowerCase()) {
                case "text":
                    processTextMessageAsync(message);
                    break;
                case "event":
                    processEventMessageAsync(message);
                    break;
                case "image":
                case "voice":
                case "video":
                case "file":
                    logger.info("收到媒体消息类型: {}，暂不处理", msgType);
                    break;
                default:
                    logger.warn("收到未知消息类型: {}，跳过处理", msgType);
                    break;
            }
            
        } catch (Exception e) {
            logger.error("异步处理微信消息时出现异常", e);
        }
    }
    
    @Override
    @Async("weChatAsyncExecutor")
    public void processTextMessageAsync(WeChatMessage message) {
        try {
            String receivedContent = message.getContent();
            if (receivedContent == null || receivedContent.trim().isEmpty()) {
                logger.warn("收到空的文本消息内容，跳过处理");
                return;
            }
            
            String fromUser = message.getFromUserName();
            logger.info("异步处理文本消息: 发送者={}, 内容={}", fromUser, receivedContent);
            
            // 保存用户消息到数据库
            conversationService.saveUserMessage(fromUser, receivedContent, message.getMsgId());
            
            // 调用Dify API生成智能回复（包含上下文）
            String processedContent = generateAIReply(receivedContent, fromUser);
            
            if (processedContent != null && !processedContent.trim().isEmpty()) {
                // 异步发送回复消息
                if (processedContent.contains("</think>")) {
                    processedContent = processedContent.substring(processedContent.indexOf("</think>\n") + "</think>\n".length());
                }
                if (processedContent.contains("<think>") && !processedContent.contains("</think>")) {
                    String content = processedContent;
                    int thinkIdx = content.indexOf("<think>");
                    content = content.substring(thinkIdx + "<think>".length()).trim();
                    int lastColon = content.lastIndexOf(':');
                    if (lastColon != -1 && lastColon < content.length() - 1) {
                        processedContent = content.substring(lastColon + 1).trim();
                    } else {
                        int lastComma = content.lastIndexOf(',');
                        if (lastComma != -1 && lastComma < content.length() - 1) {
                            processedContent = content.substring(lastComma + 1).trim();
                        } else {
                            processedContent = content.trim();
                        }
                    }
                }

                sendReplyAsync(fromUser, processedContent.trim());
            } else {
                logger.warn("AI未能生成有效回复，跳过发送消息");
            }
            
        } catch (Exception e) {
            logger.error("异步处理文本消息时出现异常", e);
        }
    }
    
    @Override
    @Async("weChatAsyncExecutor")
    public void processEventMessageAsync(WeChatMessage message) {
        try {
            String event = message.getEvent();
            if (event == null) {
                logger.warn("事件消息中事件类型为空，跳过处理");
                return;
            }
            
            String fromUser = message.getFromUserName();
            logger.info("异步处理事件消息: 事件类型={}, 发送者={}", event, fromUser);
            
            String replyContent = null;
            
            switch (event.toLowerCase()) {
                case "subscribe":
                    // 用户关注事件
                    replyContent = "欢迎关注我的伊甸园！\n" +
                                  "我是你的AI助手，可以和我聊天互动。\n" +
                                  "发送任何消息给我，我会智能回复哦~";
                    break;
                    
                case "unsubscribe":
                    // 用户取消关注事件
                    logger.info("用户{}取消关注", fromUser);
                    return;
                    
                case "click":
                    // 菜单点击事件
                    String eventKey = message.getEventKey();
                    replyContent = handleMenuClickEvent(eventKey);
                    break;
                    
                default:
                    logger.info("收到其他事件: {}，不需要回复", event);
                    return;
            }
            
            if (replyContent != null && !replyContent.trim().isEmpty()) {
                sendReplyAsync(fromUser, replyContent);
            }
            
        } catch (Exception e) {
            logger.error("异步处理事件消息时出现异常", e);
        }
    }
    
    /**
     * 使用AI生成回复内容
     */
    private String generateAIReply(String userMessage, String userId) {
        try {
            // 获取对话上下文
            String conversationContext = conversationService.buildConversationContext(userId);
            
            // 构建包含上下文的AI对话提示词
            String prompt = String.format(
                "/no_think 你是翠鸟小新新。像微信聊天一样回复，要简短自然，别太正式，别说太多废话。\n\n" +
                "%s" +
                "用户: %s\n\n" +
                "像平时微信聊天一样简短回复：",
                conversationContext, userMessage
            );
            
            logger.info("调用Dify API生成AI回复，用户ID: {}", userId);
            
            // 调用Dify API生成回复
            DifyChatResult result = difyService.callDifyApi(prompt, userId, null);
            
            if (result != null && result.success && result.answer != null) {
                String reply = result.answer.trim();
                
                // 过滤和处理回复内容
                if (reply.length() > 300) {
                    reply = reply.substring(0, 297) + "...";
                }
                
                logger.info("AI生成回复成功: {}", reply);
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
     * 异步发送回复消息
     */
    @Async("weChatAsyncExecutor")
    public void sendReplyAsync(String toUser, String content) {
        try {
            logger.info("异步发送回复消息: toUser={}, content={}", toUser, content);
            
            WeChatSendMessageResponse response = weChatSendService.sendTextMessage(toUser, content);
            
            if (response.isSuccess()) {
                logger.info("回复消息发送成功: msgId={}", response.getMsgId());
                
                // 保存AI回复消息到数据库
                conversationService.saveAssistantMessage(toUser, content);
                
            } else {
                logger.error("回复消息发送失败: {}", response);
            }
            
        } catch (Exception e) {
            logger.error("异步发送回复消息时出现异常", e);
        }
    }
    
    /**
     * 处理菜单点击事件
     */
    private String handleMenuClickEvent(String eventKey) {
        try {
            logger.info("处理菜单点击事件: eventKey={}", eventKey);
            
            switch (eventKey) {
                case "HELP":
                    return "🤖 伊甸园助手使用指南\n\n" +
                           "• 直接发送消息与我聊天\n" +
                           "• 我可以回答各种问题\n" +
                           "• 支持日常对话和知识咨询\n" +
                           "• 随时为你提供帮助！";
                case "ABOUT":
                    return "🌟 关于我的伊甸园\n\n" +
                           "这是一个AI驱动的智能社交平台，" +
                           "致力于为用户提供温馨、智能的交流体验。\n\n" +
                           "在这里，你可以与AI助手自由对话，" +
                           "享受个性化的智能服务。";
                default:
                    return "收到菜单点击：" + eventKey + "\n请发送消息与我聊天吧~";
            }
            
        } catch (Exception e) {
            logger.error("处理菜单点击事件时出现异常", e);
            return "感谢你的点击，请发送消息与我聊天吧~";
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