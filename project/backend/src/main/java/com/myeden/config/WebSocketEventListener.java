package com.myeden.config;

import com.myeden.service.impl.WebSocketServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket事件监听器
 * 
 * 功能说明：
 * - 监听WebSocket连接和断开事件
 * - 处理用户会话管理
 * - 记录连接状态变化
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Component
public class WebSocketEventListener {
    
    @Autowired
    private WebSocketServiceImpl webSocketService;
    
    /**
     * 处理用户连接事件
     * 
     * @param event 连接事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        webSocketService.handleUserConnected(event);
    }
    
    /**
     * 处理用户断开事件
     * 
     * @param event 断开事件
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        webSocketService.handleUserDisconnected(event);
    }
} 