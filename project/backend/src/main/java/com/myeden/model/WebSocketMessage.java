package com.myeden.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * WebSocket消息模型
 * 
 * 功能说明：
 * - 定义WebSocket消息的标准格式
 * - 支持不同类型的消息（动态、评论、通知、机器人行为）
 * - 包含消息元数据和内容
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketMessage<T> {
    
    /**
     * 消息类型枚举
     */
    public enum MessageType {
        POST_UPDATE,        // 动态更新
        COMMENT_UPDATE,     // 评论更新
        NOTIFICATION,       // 通知消息
        ROBOT_ACTION,       // 机器人行为
        SYSTEM_MESSAGE,     // 系统消息
        HEARTBEAT          // 心跳消息
    }
    
    /**
     * 消息ID，用于去重和追踪
     */
    private String messageId;
    
    /**
     * 消息类型
     */
    private MessageType type;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息数据（泛型，可以是任意类型）
     */
    private T data;
    
    /**
     * 发送者ID
     */
    private String senderId;
    
    /**
     * 发送者类型（user/robot/system）
     */
    private String senderType;
    
    /**
     * 目标用户ID（用于点对点消息）
     */
    private String targetUserId;
    
    /**
     * 消息优先级（1-10，10为最高）
     */
    private Integer priority;
    
    /**
     * 消息标签，用于分类和过滤
     */
    private String[] tags;
    
    /**
     * 消息创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 消息过期时间（可选）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
    
    /**
     * 是否已读
     */
    private Boolean isRead;
    
    /**
     * 消息元数据（扩展字段）
     */
    private Object metadata;
    
    /**
     * 创建动态更新消息
     */
    public static <T> WebSocketMessage<T> postUpdate(T postData) {
        return WebSocketMessage.<T>builder()
                .type(MessageType.POST_UPDATE)
                .title("新动态")
                .content("有新的动态发布")
                .data(postData)
                .priority(5)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
    }
    
    /**
     * 创建评论更新消息
     */
    public static <T> WebSocketMessage<T> commentUpdate(T commentData) {
        return WebSocketMessage.<T>builder()
                .type(MessageType.COMMENT_UPDATE)
                .title("新评论")
                .content("有新的评论")
                .data(commentData)
                .priority(6)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
    }
    
    /**
     * 创建通知消息
     */
    public static <T> WebSocketMessage<T> notification(String title, String content, T data) {
        return WebSocketMessage.<T>builder()
                .type(MessageType.NOTIFICATION)
                .title(title)
                .content(content)
                .data(data)
                .priority(7)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
    }
    
    /**
     * 创建机器人行为消息
     */
    public static <T> WebSocketMessage<T> robotAction(T actionData) {
        return WebSocketMessage.<T>builder()
                .type(MessageType.ROBOT_ACTION)
                .title("机器人行为")
                .content("机器人执行了新的行为")
                .data(actionData)
                .priority(4)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
    }
    
    /**
     * 创建系统消息
     */
    public static <T> WebSocketMessage<T> systemMessage(String title, String content, T data) {
        return WebSocketMessage.<T>builder()
                .type(MessageType.SYSTEM_MESSAGE)
                .title(title)
                .content(content)
                .data(data)
                .priority(3)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
    }
    
    /**
     * 创建心跳消息
     */
    public static <T> WebSocketMessage<T> heartbeat() {
        return WebSocketMessage.<T>builder()
                .type(MessageType.HEARTBEAT)
                .title("心跳")
                .content("ping")
                .priority(1)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
    }
} 