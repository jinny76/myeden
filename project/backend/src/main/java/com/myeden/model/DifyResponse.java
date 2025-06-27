package com.myeden.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dify API响应模型
 * 封装Dify API的响应数据结构
 * 
 * @author MyEden Team
 * @version 1.0.0
 */
public class DifyResponse {
    
    /**
     * 响应状态码
     */
    @JsonProperty("code")
    private String code;
    
    /**
     * 响应消息
     */
    @JsonProperty("message")
    private String message;
    
    /**
     * 响应数据
     */
    @JsonProperty("data")
    private DifyData data;
    
    /**
     * 响应数据内部类
     */
    public static class DifyData {
        
        /**
         * 生成的文本内容
         */
        @JsonProperty("text")
        private String text;
        
        /**
         * 消息ID
         */
        @JsonProperty("message_id")
        private String messageId;
        
        /**
         * 对话ID
         */
        @JsonProperty("conversation_id")
        private String conversationId;
        
        /**
         * 使用情况
         */
        @JsonProperty("usage")
        private Usage usage;
        
        // Getters and Setters
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
        
        public String getMessageId() {
            return messageId;
        }
        
        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }
        
        public String getConversationId() {
            return conversationId;
        }
        
        public void setConversationId(String conversationId) {
            this.conversationId = conversationId;
        }
        
        public Usage getUsage() {
            return usage;
        }
        
        public void setUsage(Usage usage) {
            this.usage = usage;
        }
    }
    
    /**
     * 使用情况内部类
     */
    public static class Usage {
        
        /**
         * 提示词token数量
         */
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        
        /**
         * 完成token数量
         */
        @JsonProperty("completion_tokens")
        private int completionTokens;
        
        /**
         * 总token数量
         */
        @JsonProperty("total_tokens")
        private int totalTokens;
        
        // Getters and Setters
        public int getPromptTokens() {
            return promptTokens;
        }
        
        public void setPromptTokens(int promptTokens) {
            this.promptTokens = promptTokens;
        }
        
        public int getCompletionTokens() {
            return completionTokens;
        }
        
        public void setCompletionTokens(int completionTokens) {
            this.completionTokens = completionTokens;
        }
        
        public int getTotalTokens() {
            return totalTokens;
        }
        
        public void setTotalTokens(int totalTokens) {
            this.totalTokens = totalTokens;
        }
    }
    
    // Getters and Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public DifyData getData() {
        return data;
    }
    
    public void setData(DifyData data) {
        this.data = data;
    }
} 