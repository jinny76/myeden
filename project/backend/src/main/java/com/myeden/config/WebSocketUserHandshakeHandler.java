package com.myeden.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket握手处理器，确保Principal为userId
 */
public class WebSocketUserHandshakeHandler extends DefaultHandshakeHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketUserHandshakeHandler.class);
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        final String userIdFromAttr = (String) attributes.get("userId");
        final String userIdFromHeader = request.getHeaders().getFirst("userId");
        final String userId;
        if (userIdFromAttr != null) {
            userId = userIdFromAttr;
        } else if (userIdFromHeader != null) {
            userId = userIdFromHeader;
        } else {
            userId = "anonymous_" + System.currentTimeMillis();
        }
        logger.info("[WebSocket握手] attributes.userId={}, header.userId={}, 最终userId={}", userIdFromAttr, userIdFromHeader, userId);
        return new Principal() {
            @Override
            public String getName() {
                return userId;
            }
        };
    }
} 