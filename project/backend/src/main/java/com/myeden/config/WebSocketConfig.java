package com.myeden.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * 
 * 功能说明：
 * - 配置WebSocket端点和消息代理
 * - 启用STOMP协议支持
 * - 配置消息路由和订阅前缀
 * - 设置跨域访问策略
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     * 
     * @param registry 消息代理注册器
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单的内存消息代理，用于向客户端发送消息
        // 客户端订阅前缀为 /topic
        registry.enableSimpleBroker("/topic", "/queue");
        
        // 设置应用程序前缀，客户端发送消息到服务器时使用
        registry.setApplicationDestinationPrefixes("/app");
        
        // 设置用户目的地前缀，用于点对点消息
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 注册STOMP端点
     * 
     * @param registry STOMP端点注册器
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点，客户端通过此端点建立连接
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 允许所有来源，生产环境应该限制
                .withSockJS(); // 启用SockJS支持，提供降级方案
        
        // 注册纯WebSocket端点（不使用SockJS）
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
} 