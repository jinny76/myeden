package com.myeden.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * 动态点赞实体
 * 
 * 功能说明：
 * - 记录用户对动态的点赞信息
 * - 支持点赞状态查询和统计
 * - 防止重复点赞
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "post_likes")
public class PostLike {
    
    /**
     * 点赞记录ID
     */
    @Id
    private String id;
    
    /**
     * 点赞记录ID（业务ID）
     */
    @Indexed(unique = true)
    private String postLikeId;
    
    /**
     * 动态ID
     */
    @Indexed
    private String postId;
    
    /**
     * 用户ID
     */
    @Indexed
    private String userId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 