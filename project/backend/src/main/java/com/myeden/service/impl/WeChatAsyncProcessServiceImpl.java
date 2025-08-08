package com.myeden.service.impl;

import com.myeden.dto.coze.CozeChatRequest;
import com.myeden.dto.coze.CozeChatResponse;
import com.myeden.dto.coze.CozeMessage;
import com.myeden.dto.coze.CozeMessageDetailResponse;
import com.myeden.entity.UserConversation;
import com.myeden.service.CozeService;
import com.myeden.service.UserConversationService;
import com.myeden.service.UserMessageMonitorService;
import com.myeden.service.WeChatAsyncProcessService;
import com.myeden.service.WeChatConversationService;
import com.myeden.service.WeChatSendService;
import com.myeden.dto.wechat.WeChatMessage;
import com.myeden.dto.wechat.WeChatSendMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import com.myeden.dto.coze.CozeChatDetailResponse;

/**
 * 微信异步处理服务实现类
 */
@Service
public class WeChatAsyncProcessServiceImpl implements WeChatAsyncProcessService {

    private static final Logger logger = LoggerFactory.getLogger(WeChatAsyncProcessServiceImpl.class);

    private final CozeService cozeService;
    private final WeChatSendService weChatSendService;
    private final WeChatConversationService conversationService;
    private final UserConversationService userConversationService;
    private final UserMessageMonitorService userMessageMonitorService;

    @Autowired
    public WeChatAsyncProcessServiceImpl(CozeService cozeService, WeChatSendService weChatSendService, 
                                       WeChatConversationService conversationService, 
                                       UserConversationService userConversationService,
                                       UserMessageMonitorService userMessageMonitorService) {
        this.cozeService = cozeService;
        this.weChatSendService = weChatSendService;
        this.conversationService = conversationService;
        this.userConversationService = userConversationService;
        this.userMessageMonitorService = userMessageMonitorService;
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
     * 使用Coze AI生成回复内容
     */
    private String generateAIReply(String userMessage, String userId) {
        try {
            // 获取或创建用户的对话关系
            String botId = cozeService.getDefaultBotId();
            UserConversation userConversation = userConversationService.getOrCreateConversation(userId, botId);
            
            // 构建Coze聊天请求
            CozeChatRequest request = new CozeChatRequest();
            request.setBotId(botId);
            request.setUserId(userId);
            request.setConversationId(userConversation.getConversationId());
            request.setStream(false);
            
            // 构建消息
            CozeMessage userMsg = new CozeMessage("user", "question", userMessage, "text");
            request.setAdditionalMessages(Arrays.asList(userMsg));
            
            logger.info("调用Coze API生成AI回复，用户ID: {}, 会话ID: {}", userId, userConversation.getConversationId());
            
            // 调用Coze API生成回复
            CozeChatResponse chatResponse = cozeService.chat(request);
            
            if (chatResponse != null && chatResponse.isSuccess() && chatResponse.getChatId() != null) {
                logger.info("聊天请求发起成功 - 对话ID: {}, ChatID: {}", 
                           chatResponse.getConversationId(), chatResponse.getChatId());
                
                // 等待对话完成处理
                logger.info("开始等待对话完成处理 - ChatID: {}", chatResponse.getChatId());
                
                CozeMessageDetailResponse messageDetail = waitForChatCompletion(
                        chatResponse.getChatId(), 
                        userConversation.getConversationId(), 
                        60000  // 30秒超时（微信场景可以稍短一些）
                );
                
                if (messageDetail != null && messageDetail.isSuccess() && messageDetail.getData() != null) {
                    // 获取最后一个answer类型的回复
                    String reply = extractLastAnswerContent(messageDetail.getData());
                    
                    // 记录最后一条消息ID，用于后续监控新消息
                    String lastMessageId = extractLastMessageId(messageDetail.getData());
                    if (lastMessageId != null) {
                        userConversationService.updateLastMessageId(userId, lastMessageId);
                        logger.debug("记录最后消息ID - 用户ID: {}, 消息ID: {}", userId, lastMessageId);
                    }
                    
                    if (reply != null && !reply.trim().isEmpty()) {
                        // 过滤和处理回复内容
                        /*if (reply.length() > 300) {
                            reply = reply.substring(0, 297) + "...";
                        }*/
                        
                        logger.info("Coze AI生成回复成功: {}", reply);
                        return reply;
                    }
                }
            }
            
            logger.warn("Coze AI生成回复失败或为空");
            return getDefaultReply();
            
        } catch (Exception e) {
            logger.error("生成Coze AI回复时出现异常", e);
            return getDefaultReply();
        }
    }
    
    /**
     * 等待对话完成的辅助方法
     * 参考CozeController的waitForChatCompletion逻辑
     */
    private CozeMessageDetailResponse waitForChatCompletion(String chatId, String conversationId, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        long pollInterval = 2000; // 2秒轮询间隔
        
        logger.info("开始等待对话完成 - chatId: {}, 超时: {}ms", chatId, timeoutMs);
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                CozeChatDetailResponse detail = cozeService.getChatDetail(chatId, conversationId);
                
                if (detail != null && detail.isSuccess() && detail.getData() != null) {
                    String status = detail.getData().getStatus();
                    logger.debug("对话状态检查 - chatId: {}, status: {}", chatId, status);
                    
                    if ("completed".equals(status) || "failed".equals(status)) {
                        logger.info("对话已结束 - chatId: {}, 最终状态: {}", chatId, status);
                        break;
                    }
                    
                    if ("requires_action".equals(status)) {
                        logger.warn("对话需要用户操作 - chatId: {}", chatId);
                        break;
                    }
                }
                
                // 等待下一次轮询
                Thread.sleep(pollInterval);
                
            } catch (InterruptedException e) {
                logger.warn("等待对话完成时被中断", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("轮询对话状态时发生异常", e);
                // 继续轮询，不中断
            }
        }
        
        logger.warn("等待对话完成超时 - chatId: {}", chatId);
        // 超时后返回最后一次的状态
        try {
            return cozeService.getMessageDetails(conversationId, chatId);
        } catch (Exception e) {
            logger.error("超时后获取对话详情失败", e);
            return CozeMessageDetailResponse.error(408, "等待对话完成超时");
        }
    }
    
    /**
     * 从消息详情中提取最后一个answer类型的回复内容
     */
    private String extractLastAnswerContent(List<CozeMessageDetailResponse.ChatV3MessageDetail> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        
        // 查找最后一个answer类型的消息
        for (int i = messages.size() - 1; i >= 0; i--) {
            CozeMessageDetailResponse.ChatV3MessageDetail message = messages.get(i);
            if ("answer".equals(message.getType()) && "assistant".equals(message.getRole())) {
                return message.getContent();
            }
        }
        
        return null;
    }
    
    /**
     * 从消息详情中提取最后一条answer类型消息的ID
     */
    private String extractLastMessageId(List<CozeMessageDetailResponse.ChatV3MessageDetail> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        
        // 查找最后一条answer类型的消息ID，从后往前遍历
        for (int i = messages.size() - 1; i >= 0; i--) {
            CozeMessageDetailResponse.ChatV3MessageDetail message = messages.get(i);
            if ("answer".equals(message.getType()) && "assistant".equals(message.getRole())) {
                String messageId = message.getId();
                if (messageId != null && !messageId.trim().isEmpty()) {
                    logger.debug("提取最后一条answer消息ID: {}", messageId);
                    return messageId;
                }
            }
        }
        
        logger.warn("未找到有效的answer类型消息ID");
        return null;
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
                
                // 启动用户消息监控，监听Coze主动推送的新消息
                /*try {
                    userMessageMonitorService.startMonitoring(toUser);
                    logger.info("已启动用户消息监控 - 用户: {}", toUser);
                } catch (Exception e) {
                    logger.error("启动用户消息监控失败 - 用户: {}", toUser, e);
                }*/
                
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