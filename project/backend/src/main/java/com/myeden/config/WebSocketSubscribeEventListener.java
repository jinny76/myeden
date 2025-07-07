package com.myeden.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
public class WebSocketSubscribeEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketSubscribeEventListener.class);

    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();
        String user = accessor.getUser() != null ? accessor.getUser().getName() : "匿名";
        logger.info("WebSocket订阅事件: sessionId={}, user={}, destination={}", sessionId, user, destination);
    }
} 