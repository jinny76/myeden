package com.myeden.dto.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Coze对话详细信息响应对象
 */
public class CozeChatDetailResponse {
    
    /**
     * 对话详细数据
     */
    private ChatDetailData data;
    
    /**
     * 请求详细信息
     */
    private ResponseDetail detail;
    
    /**
     * 调用状态码，0表示成功
     */
    private int code = 0;
    
    /**
     * 状态信息
     */
    private String msg = "Success";

    public CozeChatDetailResponse() {}

    /**
     * 对话详细数据内部类
     */
    public static class ChatDetailData {
        /**
         * 对话ID
         */
        @JsonProperty("chat_id")
        private String chatId;
        
        /**
         * 会话ID
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
         * 元信息
         */
        @JsonProperty("meta_data")
        private Object metaData;
        
        /**
         * 最后一个错误
         */
        @JsonProperty("last_error")
        private ChatError lastError;
        
        /**
         * 使用情况
         */
        private ChatUsage usage;

        // Getters and Setters
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
        
        public ChatError getLastError() { return lastError; }
        public void setLastError(ChatError lastError) { this.lastError = lastError; }
        
        public ChatUsage getUsage() { return usage; }
        public void setUsage(ChatUsage usage) { this.usage = usage; }
    }

    /**
     * 对话错误信息
     */
    public static class ChatError {
        /**
         * 错误代码
         */
        private String code;
        
        /**
         * 错误消息
         */
        private String message;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * 对话使用情况
     */
    public static class ChatUsage {
        /**
         * 输入tokens
         */
        @JsonProperty("input_count")
        private Integer inputCount;
        
        /**
         * 输出tokens
         */
        @JsonProperty("output_count")
        private Integer outputCount;
        
        /**
         * 总tokens
         */
        @JsonProperty("total_count")
        private Integer totalCount;

        public Integer getInputCount() { return inputCount; }
        public void setInputCount(Integer inputCount) { this.inputCount = inputCount; }
        
        public Integer getOutputCount() { return outputCount; }
        public void setOutputCount(Integer outputCount) { this.outputCount = outputCount; }
        
        public Integer getTotalCount() { return totalCount; }
        public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    }

    /**
     * 响应详情内部类
     */
    public static class ResponseDetail {
        /**
         * 日志ID
         */
        private String logid;

        public String getLogid() { return logid; }
        public void setLogid(String logid) { this.logid = logid; }
    }

    // 便捷构造方法
    public static CozeChatDetailResponse success(ChatDetailData data) {
        CozeChatDetailResponse response = new CozeChatDetailResponse();
        response.setData(data);
        response.setCode(0);
        response.setMsg("Success");
        return response;
    }
    
    public static CozeChatDetailResponse error(int code, String msg) {
        CozeChatDetailResponse response = new CozeChatDetailResponse();
        response.setCode(code);
        response.setMsg(msg);
        return response;
    }
    
    public boolean isSuccess() {
        return code == 0;
    }
    
    public boolean isCompleted() {
        return data != null && "completed".equals(data.getStatus());
    }

    public boolean isFailed() {
        return data != null && "failed".equals(data.getStatus());
    }

    public boolean isInProgress() {
        return data != null && "in_progress".equals(data.getStatus());
    }

    // Getters and Setters
    public ChatDetailData getData() {
        return data;
    }

    public void setData(ChatDetailData data) {
        this.data = data;
    }

    public ResponseDetail getDetail() {
        return detail;
    }

    public void setDetail(ResponseDetail detail) {
        this.detail = detail;
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

    public String getMessage() {
        return msg;
    }

    public void setMessage(String message) {
        this.msg = message;
    }

    @Override
    public String toString() {
        return "CozeChatDetailResponse{" +
                "data=" + data +
                ", detail=" + detail +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}