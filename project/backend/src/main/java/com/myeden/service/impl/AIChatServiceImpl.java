package com.myeden.service.impl;

import com.myeden.entity.ChatMessage;
import com.myeden.entity.Robot;
import com.myeden.repository.RobotRepository;
import com.myeden.service.AIChatService;
import com.myeden.service.PromptService;
import com.myeden.service.DifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AIChatServiceImpl implements AIChatService {
    @Autowired
    private RobotRepository robotRepository;
    @Autowired
    private PromptService promptService;
    @Autowired
    private DifyService difyService;

    /**
     * 根据用户消息生成AI回复，参考生成评论的标准流程
     * @param userMessage 用户发送的消息
     * @return AI生成的回复消息
     */
    @Override
    public ChatMessage generateAIReply(ChatMessage userMessage) {
        String robotId = userMessage.getReceiverId();
        Optional<Robot> robotOpt = robotRepository.findByRobotId(robotId);
        Robot robot = robotOpt.orElse(null);
        if (robot == null) {
            ChatMessage aiMsg = new ChatMessage();
            aiMsg.setSessionId(userMessage.getSessionId());
            aiMsg.setConversationId(userMessage.getConversationId()); // 保持会话ID一致
            aiMsg.setSenderId(robotId);
            aiMsg.setSenderType("ai");
            aiMsg.setReceiverId(userMessage.getSenderId());
            aiMsg.setReceiverType("user");
            aiMsg.setContent("很抱歉，机器人暂时无法回复。");
            aiMsg.setMsgType("text");
            aiMsg.setCreatedAt(LocalDateTime.now());
            aiMsg.setIsRead(false);
            return aiMsg;
        }
        try {
            // 直接调用PromptService统一生成AI回复
            DifyService.DifyChatResult result = promptService.generateChatReply(robot, userMessage, null);
            ChatMessage aiMsg = new ChatMessage();
            aiMsg.setSessionId(userMessage.getSessionId());
            aiMsg.setConversationId(result.conversationId); // 保持会话ID一致
            aiMsg.setSenderId(robotId);
            aiMsg.setSenderType("ai");
            aiMsg.setReceiverId(userMessage.getSenderId());
            aiMsg.setReceiverType("user");
            aiMsg.setContent(result.answer);
            aiMsg.setMsgType("text");
            aiMsg.setCreatedAt(LocalDateTime.now());
            aiMsg.setIsRead(false);
            return aiMsg;
        } catch (Exception e) {
            ChatMessage aiMsg = new ChatMessage();
            aiMsg.setSessionId(userMessage.getSessionId());
            aiMsg.setConversationId(userMessage.getConversationId()); // 保持会话ID一致
            aiMsg.setSenderId(robotId);
            aiMsg.setSenderType("ai");
            aiMsg.setReceiverId(userMessage.getSenderId());
            aiMsg.setReceiverType("user");
            aiMsg.setContent("AI回复失败，请稍后再试。");
            aiMsg.setMsgType("text");
            aiMsg.setCreatedAt(LocalDateTime.now());
            aiMsg.setIsRead(false);
            return aiMsg;
        }
    }
} 