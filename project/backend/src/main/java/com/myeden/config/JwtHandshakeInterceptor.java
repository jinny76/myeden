package com.myeden.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import com.myeden.service.JwtService;
import org.springframework.http.server.ServletServerHttpRequest;
import java.util.Map;

/**
 * WebSocket握手拦截器，从Header中提取JWT并解析userId，注入attributes
 */
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = null;
        // 1. 优先从URL参数获取
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            token = servletRequest.getServletRequest().getParameter("token");
        }
        // 2. 其次从Header获取
        if (token == null) {
            token = request.getHeaders().getFirst("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
        }
        if (token != null) {
            // 通过Spring上下文获取JwtService实例
            ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(
                ((ServletServerHttpRequest) request).getServletRequest().getServletContext()
            );
            JwtService jwtService = ctx.getBean(JwtService.class);
            String userId = jwtService.extractUserId(token);
            if (userId != null) {
                attributes.put("userId", userId);
            }
        }
        return true;
    }
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {}
} 