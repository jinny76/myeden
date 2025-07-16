package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * 沟通评估报告实体类
 * 
 * 功能说明：
 * - 存储AI社交沟通大师对用户对话的评估结果
 * - 包含多维度评分和专业建议
 * - 用于积分奖励和沟通质量追踪
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-07-15
 */
@Document(collection = "communication_reports")
public class CommunicationReport {
    
    @Id
    private String id;
    
    /** 用户ID */
    @Indexed
    private String userId;
    
    /** 对话ID */
    @Indexed
    private String conversationId;
    
    /** 总分 (0-10) */
    private int score;
    
    /** 沟通深度评分 (0-10) */
    private int depthScore;
    
    /** 情感表达评分 (0-10) */
    private int emotionScore;
    
    /** 互动质量评分 (0-10) */
    private int interactionScore;
    
    /** 语言表达评分 (0-10) */
    private int languageScore;
    
    /** 共情能力评分 (0-10) */
    private int empathyScore;
    
    /** 专业评价 */
    private String evaluation;
    
    /** 改进建议 */
    private String suggestions;
    
    /** 奖励积分 */
    private int pointsAwarded;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    // 构造方法
    public CommunicationReport() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public int getDepthScore() { return depthScore; }
    public void setDepthScore(int depthScore) { this.depthScore = depthScore; }
    
    public int getEmotionScore() { return emotionScore; }
    public void setEmotionScore(int emotionScore) { this.emotionScore = emotionScore; }
    
    public int getInteractionScore() { return interactionScore; }
    public void setInteractionScore(int interactionScore) { this.interactionScore = interactionScore; }
    
    public int getLanguageScore() { return languageScore; }
    public void setLanguageScore(int languageScore) { this.languageScore = languageScore; }
    
    public int getEmpathyScore() { return empathyScore; }
    public void setEmpathyScore(int empathyScore) { this.empathyScore = empathyScore; }
    
    public String getEvaluation() { return evaluation; }
    public void setEvaluation(String evaluation) { this.evaluation = evaluation; }
    
    public String getSuggestions() { return suggestions; }
    public void setSuggestions(String suggestions) { this.suggestions = suggestions; }
    
    public int getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(int pointsAwarded) { this.pointsAwarded = pointsAwarded; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return "CommunicationReport{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", score=" + score +
                ", depthScore=" + depthScore +
                ", emotionScore=" + emotionScore +
                ", interactionScore=" + interactionScore +
                ", languageScore=" + languageScore +
                ", empathyScore=" + empathyScore +
                ", pointsAwarded=" + pointsAwarded +
                ", createdAt=" + createdAt +
                '}';
    }
}