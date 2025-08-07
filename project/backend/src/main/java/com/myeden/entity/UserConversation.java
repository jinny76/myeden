package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户对话关系实体
 * 用于保存用户和Coze conversationId的对应关系
 */
@Document(collection = "user_conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserConversation {
    
    /**
     * MongoDB主键ID
     */
    @Id
    private String id;
    
    /**
     * 用户ID（微信用户ID或其他用户标识）
     */
    @Indexed(unique = true)
    private String userId;
    
    /**
     * Coze会话ID
     */
    @Indexed
    private String conversationId;
    
    /**
     * 机器人ID
     */
    private String botId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveAt;
    
    /**
     * 是否活跃（用于清理过期会话）
     */
    private Boolean isActive = true;
    
    /**
     * 备注信息
     */
    private String remark;
    
    /**
     * 构造函数
     */
    public UserConversation(String userId, String conversationId, String botId) {
        this.userId = userId;
        this.conversationId = conversationId;
        this.botId = botId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
        this.isActive = true;
    }
    
    /**
     * 更新活跃时间
     */
    public void updateActiveTime() {
        this.lastActiveAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 设置非活跃状态
     */
    public void setInactive() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
}
