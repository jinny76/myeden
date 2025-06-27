package com.myeden.controller;

import com.myeden.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * WebSocket控制器
 * 
 * 功能说明：
 * - 提供WebSocket连接状态查询接口
 * - 提供在线用户信息查询接口
 * - 提供手动消息推送接口
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/websocket")
public class WebSocketController {
    
    @Autowired
    private WebSocketService webSocketService;
    
    /**
     * 获取在线用户数量
     * 
     * @return 在线用户数量
     */
    @GetMapping("/online-count")
    public EventResponse getOnlineUserCount() {
        try {
            int count = webSocketService.getOnlineUserCount();
            return EventResponse.success(count, "获取在线用户数量成功");
        } catch (Exception e) {
            return EventResponse.error("获取在线用户数量失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取在线用户列表
     * 
     * @return 在线用户ID列表
     */
    @GetMapping("/online-users")
    public EventResponse getOnlineUsers() {
        try {
            List<String> users = webSocketService.getOnlineUsers();
            return EventResponse.success(users, "获取在线用户列表成功");
        } catch (Exception e) {
            return EventResponse.error("获取在线用户列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查用户是否在线
     * 
     * @param userId 用户ID
     * @return 是否在线
     */
    @GetMapping("/user/{userId}/online")
    public EventResponse isUserOnline(@PathVariable String userId) {
        try {
            boolean isOnline = webSocketService.isUserOnline(userId);
            return EventResponse.success(isOnline, "检查用户在线状态成功");
        } catch (Exception e) {
            return EventResponse.error("检查用户在线状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 断开用户连接
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/user/{userId}/disconnect")
    public EventResponse disconnectUser(@PathVariable String userId) {
        try {
            webSocketService.disconnectUser(userId);
            return EventResponse.success("用户连接已断开", "断开用户连接成功");
        } catch (Exception e) {
            return EventResponse.error("断开用户连接失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送心跳消息
     * 
     * @return 操作结果
     */
    @PostMapping("/heartbeat")
    public EventResponse sendHeartbeat() {
        try {
            webSocketService.sendHeartbeat();
            return EventResponse.success("心跳消息已发送", "发送心跳消息成功");
        } catch (Exception e) {
            return EventResponse.error("发送心跳消息失败: " + e.getMessage());
        }
    }
} 