package com.myeden.service;

import java.io.File;

import com.myeden.entity.ChatMessage;

public interface AIChatService {
    ChatMessage generateAIReply(ChatMessage userMessage);

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
} 