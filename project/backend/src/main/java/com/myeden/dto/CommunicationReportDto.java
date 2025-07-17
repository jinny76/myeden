package com.myeden.dto;

import com.myeden.entity.CommunicationReport;
import java.time.LocalDateTime;

/**
 * 沟通报告DTO类
 * 
 * 功能说明：
 * - 包含基本的沟通报告信息
 * - 扩展对方名称和第一句话信息
 * - 用于API响应的数据传输
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-07-17
 */
public class CommunicationReportDto {
    
    /** 报告ID */
    private String id;
    
    /** 用户ID */
    private String userId;
    
    /** 对话ID */
    private String conversationId;
    
    /** 对方名称（机器人名称） */
    private String partnerName;
    
    /** 对话第一句话 */
    private String firstMessage;
    
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
    public CommunicationReportDto() {}
    
    /**
     * 从CommunicationReport实体转换为DTO
     * 
     * @param report 沟通报告实体
     * @param partnerName 对方名称
     * @param firstMessage 第一句话
     * @return DTO对象
     */
    public static CommunicationReportDto fromEntity(CommunicationReport report, String partnerName, String firstMessage) {
        CommunicationReportDto dto = new CommunicationReportDto();
        dto.setId(report.getId());
        dto.setUserId(report.getUserId());
        dto.setConversationId(report.getConversationId());
        dto.setPartnerName(partnerName);
        dto.setFirstMessage(firstMessage);
        dto.setScore(report.getScore());
        dto.setDepthScore(report.getDepthScore());
        dto.setEmotionScore(report.getEmotionScore());
        dto.setInteractionScore(report.getInteractionScore());
        dto.setLanguageScore(report.getLanguageScore());
        dto.setEmpathyScore(report.getEmpathyScore());
        dto.setEvaluation(report.getEvaluation());
        dto.setSuggestions(report.getSuggestions());
        dto.setPointsAwarded(report.getPointsAwarded());
        dto.setCreatedAt(report.getCreatedAt());
        return dto;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    
    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }
    
    public String getFirstMessage() { return firstMessage; }
    public void setFirstMessage(String firstMessage) { this.firstMessage = firstMessage; }
    
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
        return "CommunicationReportDto{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", partnerName='" + partnerName + '\'' +
                ", firstMessage='" + firstMessage + '\'' +
                ", score=" + score +
                ", pointsAwarded=" + pointsAwarded +
                ", createdAt=" + createdAt +
                '}';
    }
}