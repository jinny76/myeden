package com.myeden.service;

import com.myeden.entity.ChatMessage;
import com.myeden.service.AIChatService.CommunicationScore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 沟通评分服务
 * 
 * 功能说明：
 * - 对用户与机器人的沟通质量进行心理学评分
 * - 根据评分给予用户积分奖励
 * - 分析沟通深度、情感表达、互动质量等维度
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-07-15
 */
@Service
public class CommunicationScoringService {
    
    /**
     * 评估沟通质量
     * 
     * @param chatMessages 聊天消息列表
     * @return 沟通评分结果
     */
    public CommunicationScore evaluateCommunicationQuality(List<ChatMessage> chatMessages) {
        if (chatMessages == null || chatMessages.isEmpty()) {
            return new CommunicationScore(0, "poor", "没有有效的沟通内容", 0);
        }
        
        // 计算各个维度的分数
        int lengthScore = evaluateMessageLength(chatMessages);
        int depthScore = evaluateCommunicationDepth(chatMessages);
        int emotionScore = evaluateEmotionalExpression(chatMessages);
        int interactionScore = evaluateInteractionQuality(chatMessages);
        int continuityScore = evaluateContinuity(chatMessages);
        
        // 加权计算总分
        double totalScore = (lengthScore * 0.15 + 
                           depthScore * 0.25 + 
                           emotionScore * 0.2 + 
                           interactionScore * 0.25 + 
                           continuityScore * 0.15);
        
        int finalScore = Math.min(10, Math.max(0, (int) Math.round(totalScore)));
        
        String quality = getQualityLevel(finalScore);
        String feedback = generateFeedback(finalScore, lengthScore, depthScore, emotionScore, interactionScore, continuityScore);
        int pointsAwarded = calculatePointsReward(finalScore);
        
        return new CommunicationScore(finalScore, quality, feedback, pointsAwarded);
    }
    
    /**
     * 评估消息长度和数量
     */
    private int evaluateMessageLength(List<ChatMessage> messages) {
        int totalLength = messages.stream()
                .mapToInt(msg -> msg.getContent() != null ? msg.getContent().length() : 0)
                .sum();
        
        int messageCount = messages.size();
        
        // 理想的沟通应该有适当的长度和轮次
        if (messageCount >= 6 && totalLength >= 200) {
            return 10;
        } else if (messageCount >= 4 && totalLength >= 100) {
            return 8;
        } else if (messageCount >= 2 && totalLength >= 50) {
            return 6;
        } else if (messageCount >= 1 && totalLength >= 20) {
            return 4;
        } else {
            return 2;
        }
    }
    
    /**
     * 评估沟通深度
     */
    private int evaluateCommunicationDepth(List<ChatMessage> messages) {
        int depthScore = 0;
        
        for (ChatMessage message : messages) {
            String content = message.getContent();
            if (content == null) continue;
            
            // 检查是否包含深度沟通的关键词
            if (containsDeepCommunicationKeywords(content)) {
                depthScore += 3;
            }
            
            // 检查是否包含个人分享
            if (containsPersonalSharingKeywords(content)) {
                depthScore += 2;
            }
            
            // 检查是否包含问题询问
            if (containsQuestionKeywords(content)) {
                depthScore += 1;
            }
        }
        
        return Math.min(10, depthScore);
    }
    
    /**
     * 评估情感表达
     */
    private int evaluateEmotionalExpression(List<ChatMessage> messages) {
        int emotionScore = 0;
        
        for (ChatMessage message : messages) {
            String content = message.getContent();
            if (content == null) continue;
            
            // 检查情感词汇
            if (containsEmotionalKeywords(content)) {
                emotionScore += 2;
            }
            
            // 检查表情符号
            if (containsEmojis(content)) {
                emotionScore += 1;
            }
            
            // 检查感叹号或问号等表达性标点
            if (content.contains("!") || content.contains("?") || content.contains("…")) {
                emotionScore += 1;
            }
        }
        
        return Math.min(10, emotionScore);
    }
    
    /**
     * 评估互动质量
     */
    private int evaluateInteractionQuality(List<ChatMessage> messages) {
        if (messages.size() < 2) return 0;
        
        int interactionScore = 0;
        
        // 检查是否有来回互动
        boolean hasBackAndForth = false;
        String lastSender = null;
        int alternatingCount = 0;
        
        for (ChatMessage message : messages) {
            String currentSender = message.getSenderId();
            if (lastSender != null && !lastSender.equals(currentSender)) {
                alternatingCount++;
                hasBackAndForth = true;
            }
            lastSender = currentSender;
        }
        
        if (hasBackAndForth) {
            interactionScore += Math.min(6, alternatingCount);
        }
        
        // 检查是否有回应性对话
        for (int i = 1; i < messages.size(); i++) {
            ChatMessage prev = messages.get(i - 1);
            ChatMessage curr = messages.get(i);
            
            if (isResponseMessage(prev.getContent(), curr.getContent())) {
                interactionScore += 2;
            }
        }
        
        return Math.min(10, interactionScore);
    }
    
    /**
     * 评估对话连续性
     */
    private int evaluateContinuity(List<ChatMessage> messages) {
        if (messages.size() < 2) return 5; // 单条消息给中等分
        
        // 检查时间间隔是否合理（假设有时间戳）
        // 这里简化处理，主要看消息之间的逻辑连贯性
        int continuityScore = 5; // 基础分
        
        // 检查主题连贯性
        if (hasTopicContinuity(messages)) {
            continuityScore += 3;
        }
        
        // 检查逻辑连贯性
        if (hasLogicalFlow(messages)) {
            continuityScore += 2;
        }
        
        return Math.min(10, continuityScore);
    }
    
    /**
     * 检查是否包含深度沟通关键词
     */
    private boolean containsDeepCommunicationKeywords(String content) {
        String[] keywords = {"感受", "想法", "梦想", "希望", "担心", "困扰", "经历", "回忆", "未来", "计划", "目标", "价值观"};
        for (String keyword : keywords) {
            if (content.contains(keyword)) return true;
        }
        return false;
    }
    
    /**
     * 检查是否包含个人分享关键词
     */
    private boolean containsPersonalSharingKeywords(String content) {
        String[] keywords = {"我觉得", "我认为", "我的", "我曾经", "我希望", "我担心", "我喜欢", "我不喜欢", "对我来说"};
        for (String keyword : keywords) {
            if (content.contains(keyword)) return true;
        }
        return false;
    }
    
    /**
     * 检查是否包含问题询问关键词
     */
    private boolean containsQuestionKeywords(String content) {
        return content.contains("?") || content.contains("？") || 
               content.contains("为什么") || content.contains("怎么样") || 
               content.contains("如何") || content.contains("什么时候") ||
               content.contains("你觉得") || content.contains("你认为");
    }
    
    /**
     * 检查是否包含情感关键词
     */
    private boolean containsEmotionalKeywords(String content) {
        String[] keywords = {"开心", "快乐", "高兴", "兴奋", "满足", "幸福", 
                           "难过", "伤心", "失望", "沮丧", "焦虑", "紧张",
                           "愤怒", "生气", "惊讶", "感动", "温暖", "感谢"};
        for (String keyword : keywords) {
            if (content.contains(keyword)) return true;
        }
        return false;
    }
    
    /**
     * 检查是否包含表情符号
     */
    private boolean containsEmojis(String content) {
        // 简单检查一些常见的文字表情和emoji
        return content.matches(".*[😀-🙿🚀-🛿☀-⭿].*") || 
               content.contains(":)") || content.contains(":(") || 
               content.contains(":D") || content.contains("^_^") ||
               content.contains("T_T") || content.contains(">_<");
    }
    
    /**
     * 检查是否为回应性消息
     */
    private boolean isResponseMessage(String prevContent, String currContent) {
        if (prevContent == null || currContent == null) return false;
        
        // 简单的回应检查
        if (prevContent.contains("?") || prevContent.contains("？")) {
            // 前一条是问题，当前回答不是问题，可能是回应
            return !(currContent.contains("?") || currContent.contains("？"));
        }
        
        // 检查是否包含回应性词汇
        String[] responseKeywords = {"是的", "不是", "我觉得", "我认为", "确实", "可能", "应该"};
        for (String keyword : responseKeywords) {
            if (currContent.contains(keyword)) return true;
        }
        
        return false;
    }
    
    /**
     * 检查主题连贯性
     */
    private boolean hasTopicContinuity(List<ChatMessage> messages) {
        // 简化实现：检查是否有关键词的延续
        // 实际应用中可以使用更复杂的NLP技术
        return true; // 暂时返回true
    }
    
    /**
     * 检查逻辑连贯性
     */
    private boolean hasLogicalFlow(List<ChatMessage> messages) {
        // 简化实现：检查对话的逻辑流畅性
        // 实际应用中可以使用更复杂的对话分析技术
        return true; // 暂时返回true
    }
    
    /**
     * 获取质量等级
     */
    private String getQualityLevel(int score) {
        if (score >= 8) return "excellent";
        if (score >= 6) return "good";
        if (score >= 4) return "fair";
        return "poor";
    }
    
    /**
     * 生成反馈
     */
    private String generateFeedback(int finalScore, int lengthScore, int depthScore, 
                                  int emotionScore, int interactionScore, int continuityScore) {
        StringBuilder feedback = new StringBuilder();
        
        if (finalScore >= 8) {
            feedback.append("这是一次高质量的深度沟通！");
        } else if (finalScore >= 6) {
            feedback.append("这次沟通质量不错，");
        } else if (finalScore >= 4) {
            feedback.append("这次沟通还可以，");
        } else {
            feedback.append("这次沟通还有改进空间，");
        }
        
        // 给出具体建议
        if (depthScore < 5) {
            feedback.append("可以尝试分享更多个人想法和感受；");
        }
        if (emotionScore < 5) {
            feedback.append("可以更多地表达情感；");
        }
        if (interactionScore < 5) {
            feedback.append("可以增加互动问答；");
        }
        
        return feedback.toString();
    }
    
    /**
     * 计算积分奖励
     */
    private int calculatePointsReward(int score) {
        // 根据评分计算积分奖励
        if (score >= 9) return 10;
        if (score >= 8) return 8;
        if (score >= 7) return 6;
        if (score >= 6) return 5;
        if (score >= 5) return 3;
        if (score >= 4) return 2;
        if (score >= 2) return 1;
        return 0;
    }
}