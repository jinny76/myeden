package com.myeden.controller;

import com.myeden.entity.ChatMessage;
import com.myeden.model.WebSocketMessage;
import com.myeden.service.ChatService;
import com.myeden.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private WebSocketService webSocketService;

    /**
     * 发送聊天消息，并推送给接收方
     */
    @PostMapping("/send")
    public void sendMessage(@RequestBody ChatMessage message) {
        chatService.sendMessage(message);
        // 推送给接收方
        WebSocketMessage<ChatMessage> wsMsg = WebSocketMessage.chat(message); // 需实现chat类型工厂方法
        webSocketService.sendMessageToUser(message.getReceiverId(), wsMsg);
    }

    /**
     * 查询会话历史消息（按sessionId）
     */
    @GetMapping("/history")
    public List<ChatMessage> getHistoryBySession(
            @RequestParam String sessionId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return chatService.getHistoryBySession(sessionId, limit, offset);
    }

    /**
     * 查询与指定机器人所有历史消息
     */
    @GetMapping("/history/robot")
    public List<ChatMessage> getHistoryWithRobot(
            @RequestParam String userId,
            @RequestParam String robotId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return chatService.getHistoryWithRobot(userId, robotId, limit, offset);
    }
} 