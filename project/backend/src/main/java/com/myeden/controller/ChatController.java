package com.myeden.controller;

import com.myeden.entity.ChatMessage;
import com.myeden.model.WebSocketMessage;
import com.myeden.service.ChatService;
import com.myeden.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.myeden.service.AIChatService;
import com.myeden.controller.EventResponse;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private AIChatService aiChatService;

    /**
     * 发送聊天消息，并推送给接收方
     */
    @PostMapping("/send")
    public ResponseEntity<EventResponse> sendMessage(@RequestBody ChatMessage message) {
        try {
            // 1. 保存用户消息
            chatService.sendMessage(message);
            // 2. 推送给接收方
            WebSocketMessage<ChatMessage> wsMsg = WebSocketMessage.chat(message);
            webSocketService.sendMessageToUser(message.getSenderId(), wsMsg);

            // 3. 检查是否需要AI回复，异步处理（手动新建线程）
            if ("robot".equalsIgnoreCase(message.getReceiverType()) || isRobotId(message.getReceiverId())) {
                new Thread(() -> aiReplyAsync(message)).start();
            }

            // 4. 立即返回，仅包含用户消息
            return ResponseEntity.ok(new EventResponse(200, "消息发送成功", List.of(message)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new EventResponse(400, "消息发送失败: " + e.getMessage(), null));
        }
    }

    /**
     * 异步生成AI回复并推送（不再依赖@Async）
     * @param userMessage 用户发送的消息
     */
    public void aiReplyAsync(ChatMessage userMessage) {
        try {
            // 1. 生成AI回复
            ChatMessage aiReply = aiChatService.generateAIReply(userMessage);
            // 2. 保存AI回复
            chatService.sendMessage(aiReply);
            // 3. 推送AI回复
            WebSocketMessage<ChatMessage> aiWsMsg = WebSocketMessage.chat(aiReply);
            webSocketService.sendMessageToUser(aiReply.getReceiverId(), aiWsMsg);
        } catch (Exception e) {
            // 可记录日志，便于排查AI回复异常
            org.slf4j.LoggerFactory.getLogger(ChatController.class).error("AI回复生成失败", e);
        }
    }

    // 判断是否为AI机器人ID（可根据实际业务调整）
    private boolean isRobotId(String receiverId) {
        return receiverId != null && receiverId.startsWith("robot_");
    }

    /**
     * 查询会话历史消息（按sessionId）
     */
    @GetMapping("/history")
    public ResponseEntity<EventResponse> getHistoryBySession(
            @RequestParam String sessionId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        try {
            List<ChatMessage> history = chatService.getHistoryBySession(sessionId, limit, offset);
            return ResponseEntity.ok(new EventResponse(200, "获取历史消息成功", history));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new EventResponse(400, "获取历史消息失败: " + e.getMessage(), null));
        }
    }

    /**
     * 查询与指定机器人所有历史消息（游标分页，desc排序，前端reverse）
     */
    @GetMapping("/history/robot")
    public ResponseEntity<EventResponse> getHistoryWithRobot(
            @RequestParam String userId,
            @RequestParam String robotId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String before // ISO时间字符串
    ) {
        try {
            List<ChatMessage> history;
            if (before != null && !before.isEmpty()) {
                java.time.LocalDateTime beforeTime = java.time.LocalDateTime.parse(before);
                history = chatService.getHistoryWithRobotBefore(userId, robotId, beforeTime, limit);
            } else {
                history = chatService.getLatestHistoryWithRobot(userId, robotId, limit);
            }
            // 不再排序，直接返回desc，前端reverse
            return ResponseEntity.ok(new EventResponse(200, "获取历史消息成功", history));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new EventResponse(400, "获取历史消息失败: " + e.getMessage(), null));
        }
    }
} 