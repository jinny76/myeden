package com.myeden.service;

import java.io.File;

import com.myeden.entity.ChatMessage;

public interface AIChatService {
    ChatMessage generateAIReply(ChatMessage userMessage);
    
    /**
     * 机器人主动发起沟通
     * 当用户熟悉度达到好友等级后，机器人可主动分享内心困境
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @return 主动沟通消息
     */
    ChatMessage initiateProactiveChat(String userId, String robotId);
    
    /**
     * 根据熟悉度等级生成不同深度的沟通内容
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param familiarityLevel 熟悉度等级
     * @return 沟通消息
     */
    ChatMessage generateFamiliarityBasedMessage(String userId, String robotId, Integer familiarityLevel);
    
    /**
     * 对沟通质量进行心理学评分
     * 评分范围：0-10，用于用户积分奖励
     * 
     * @param chatMessages 聊天消息列表
     * @return 沟通评分
     */
    CommunicationScore evaluateCommunicationQuality(java.util.List<ChatMessage> chatMessages);
    
    /**
     * 检查是否应该触发主动沟通
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @return 是否应该主动沟通
     */
    boolean shouldInitiateProactiveChat(String userId, String robotId);
    
    /**
     * 发送聊天消息
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param content 消息内容
     * @param imageBase64 图片base64（可选）
     * @param audioBase64 音频base64（可选）
     * @param conversationId 会话ID（可选）
     * @return 发送的消息对象
     */
    ChatMessage sendChatMessage(String userId, String robotId, String content, String imageBase64, String audioBase64, String conversationId);

    /**
     * 调用ASR服务识别音频内容
     * @param audioFile

     * @return 识别结果
     */
    ASRRawTextInfo callASRService(File audioFile);

    /**
     * ASR识别结果结构体
     */
    class ASRRawTextInfo {
        private String lang;      // 语言
        private String emotion;   // 情感
        private String type;      // 类型
        private String speaker;   // 说话人
        private String text;      // 实际内容
        public String getLang() { return lang; }
        public void setLang(String lang) { this.lang = lang; }
        public String getEmotion() { return emotion; }
        public void setEmotion(String emotion) { this.emotion = emotion; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getSpeaker() { return speaker; }
        public void setSpeaker(String speaker) { this.speaker = speaker; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    /**
     * 解析ASR raw_text结构
     * @param rawText ASR返回的raw_text
     * @return ASRRawTextInfo对象
     */
    static ASRRawTextInfo parseASRRawText(String rawText) {
        ASRRawTextInfo info = new ASRRawTextInfo();
        if (rawText == null) return info;
        String pattern = "^<\\|([^|]+)\\|><\\|([^|]+)\\|><\\|([^|]+)\\|><\\|([^|]+)\\|>(.*)$";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(rawText);
        if (m.matches()) {
            info.setLang(m.group(1));
            info.setEmotion(m.group(2));
            info.setType(m.group(3));
            info.setSpeaker(m.group(4));
            info.setText(m.group(5));
        } else {
            info.setText(rawText);
        }
        return info;
    }
    
    /**
     * 沟通评分结果类
     */
    class CommunicationScore {
        private Integer score;           // 评分：0-10
        private String quality;          // 质量等级：poor/fair/good/excellent
        private String feedback;         // 评分反馈
        private Integer pointsAwarded;   // 奖励积分
        
        public CommunicationScore(Integer score, String quality, String feedback, Integer pointsAwarded) {
            this.score = score;
            this.quality = quality;
            this.feedback = feedback;
            this.pointsAwarded = pointsAwarded;
        }
        
        // Getter方法
        public Integer getScore() { return score; }
        public String getQuality() { return quality; }
        public String getFeedback() { return feedback; }
        public Integer getPointsAwarded() { return pointsAwarded; }
        
        // Setter方法
        public void setScore(Integer score) { this.score = score; }
        public void setQuality(String quality) { this.quality = quality; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
        public void setPointsAwarded(Integer pointsAwarded) { this.pointsAwarded = pointsAwarded; }
    }
} 