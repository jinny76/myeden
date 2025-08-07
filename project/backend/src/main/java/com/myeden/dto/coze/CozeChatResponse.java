package com.myeden.dto.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Coze聊天响应对象 - 非流式响应结构
 */
public class CozeChatResponse {
    
    /**
     * 本次对话的基本信息
     */
    private ChatData data;
    
    /**
     * 状态码，0代表调用成功
     */
    private int code = 0;
    
    /**
     * 状态信息，API调用失败时可通过此字段查看详细错误信息
     */
    private String msg = "Success";

    public CozeChatResponse() {}

    /**
     * Chat Object - 对话基本信息
     */
    public static class ChatData {
        /**
         * 聊天对话ID
         */
        @JsonProperty("id")
        private String chatId;
        
        /**
         * 对话ID
         */
        @JsonProperty("conversation_id")
        private String conversationId;
        
        /**
         * 智能体ID
         */
        @JsonProperty("bot_id")
        private String botId;
        
        /**
         * 对话状态：created/in_progress/completed/failed/requires_action
         */
        private String status;
        
        /**
         * 创建时间
         */
        @JsonProperty("created_at")
        private Long createdAt;
        
        /**
         * 完成时间
         */
        @JsonProperty("completed_at")
        private Long completedAt;
        
        /**
         * 失败时间
         */
        @JsonProperty("failed_at")
        private Long failedAt;
        
        /**
         * 元数据
         */
        @JsonProperty("meta_data")
        private Object metaData;
        
        /**
         * 使用的Token数
         */
        private Usage usage;
        
        /**
         * 最后一个错误
         */
        @JsonProperty("last_error")
        private ChatError lastError;

        // Getters and Setters for ChatData
        public String getChatId() { return chatId; }
        public void setChatId(String chatId) { this.chatId = chatId; }
        
        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
        
        public String getBotId() { return botId; }
        public void setBotId(String botId) { this.botId = botId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Long getCreatedAt() { return createdAt; }
        public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
        
        public Long getCompletedAt() { return completedAt; }
        public void setCompletedAt(Long completedAt) { this.completedAt = completedAt; }
        
        public Long getFailedAt() { return failedAt; }
        public void setFailedAt(Long failedAt) { this.failedAt = failedAt; }
        
        public Object getMetaData() { return metaData; }
        public void setMetaData(Object metaData) { this.metaData = metaData; }
        
        public Usage getUsage() { return usage; }
        public void setUsage(Usage usage) { this.usage = usage; }
        
        public ChatError getLastError() { return lastError; }
        public void setLastError(ChatError lastError) { this.lastError = lastError; }
    }

    /**
     * 对话错误信息
     */
    public static class ChatError {
        private String code;
        private String message;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // 便捷构造方法
    public static CozeChatResponse success(ChatData chatData) {
        CozeChatResponse response = new CozeChatResponse();
        response.setData(chatData);
        response.setCode(0);
        response.setMsg("Success");
        return response;
    }
    
    public static CozeChatResponse error(int code, String msg) {
        CozeChatResponse response = new CozeChatResponse();
        response.setCode(code);
        response.setMsg(msg);
        return response;
    }
    
    public boolean isSuccess() {
        return code == 0;
    }

    // 便捷方法获取ChatData中的信息
    public String getChatId() {
        return data != null ? data.getChatId() : null;
    }

    public String getConversationId() {
        return data != null ? data.getConversationId() : null;
    }

    public String getMessageId() {
        // 为了向后兼容，返回chatId作为messageId
        return getChatId();
    }

    // 主要的Getters and Setters
    public ChatData getData() {
        return data;
    }

    public void setData(ChatData data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    // 为了向后兼容，保留getMessage方法
    public String getMessage() {
        return msg;
    }

    public void setMessage(String message) {
        this.msg = message;
    }

    /**
     * Token使用统计
     */
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        
        @JsonProperty("completion_tokens")
        private int completionTokens;
        
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

        @Override
        public String toString() {
            return "Usage{" +
                    "promptTokens=" + promptTokens +
                    ", completionTokens=" + completionTokens +
                    ", totalTokens=" + totalTokens +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "CozeChatResponse{" +
                "data=" + data +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}