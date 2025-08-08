package com.myeden.dto.coze;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Coze消息列表响应
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CozeMessageListResponse {
    
    /**
     * 消息列表数据（直接是数组，不是嵌套对象）
     */
    private List<CozeMessageItem> data;
    
    private int code = 0;
    private String msg = "Success";
    
    /**
     * 是否有更多消息
     */
    @JsonProperty("has_more")
    private Boolean hasMore;
    
    /**
     * 第一条消息ID（用于向前分页）
     */
    @JsonProperty("first_id")
    private String firstId;
    
    /**
     * 最后一条消息ID（用于向后分页）
     */
    @JsonProperty("last_id")
    private String lastId;
    
    /**
     * 错误详情（当请求失败时出现）
     */
    private Map<String, Object> detail;
    
    public CozeMessageListResponse() {}
    
    public static CozeMessageListResponse success(List<CozeMessageItem> data) {
        CozeMessageListResponse response = new CozeMessageListResponse();
        response.setData(data);
        response.setCode(0);
        response.setMsg("Success");
        return response;
    }
    
    public static CozeMessageListResponse error(int code, String message) {
        CozeMessageListResponse response = new CozeMessageListResponse();
        response.setCode(code);
        response.setMsg(message);
        return response;
    }
    
    public boolean isSuccess() {
        return code == 0;
    }
    
    /**
     * 获取详细错误信息
     */
    public String getDetailError() {
        if (detail != null && detail.containsKey("logid")) {
            return msg + " (logid: " + detail.get("logid") + ")";
        }
        return msg;
    }

    // Getters and Setters
    public List<CozeMessageItem> getData() {
        return data;
    }

    public void setData(List<CozeMessageItem> data) {
        this.data = data;
    }

    public Boolean getHasMore() {
        return hasMore;
    }

    public void setHasMore(Boolean hasMore) {
        this.hasMore = hasMore;
    }

    public String getFirstId() {
        return firstId;
    }

    public void setFirstId(String firstId) {
        this.firstId = firstId;
    }

    public String getLastId() {
        return lastId;
    }

    public void setLastId(String lastId) {
        this.lastId = lastId;
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

    public Map<String, Object> getDetail() {
        return detail;
    }

    public void setDetail(Map<String, Object> detail) {
        this.detail = detail;
    }

    /**
     * 消息项
     */
    public static class CozeMessageItem {
        
        /**
         * 消息ID
         */
        private String id;
        
        /**
         * 会话ID
         */
        @JsonProperty("conversation_id")
        private String conversationId;
        
        /**
         * 机器人ID
         */
        @JsonProperty("bot_id")
        private String botId;
        
        /**
         * 对话ID
         */
        @JsonProperty("chat_id")
        private String chatId;
        
        /**
         * 消息内容
         */
        private String content;
        
        /**
         * 内容类型
         */
        @JsonProperty("content_type")
        private String contentType;
        
        /**
         * 消息角色
         */
        private String role;
        
        /**
         * 消息类型
         */
        private String type;
        
        /**
         * 创建时间戳
         */
        @JsonProperty("created_at")
        private Long createdAt;
        
        /**
         * 更新时间戳
         */
        @JsonProperty("updated_at")
        private Long updatedAt;
        
        /**
         * 元数据
         */
        @JsonProperty("meta_data")
        private Object metaData;
        
        /**
         * 推理内容
         */
        @JsonProperty("reasoning_content")
        private String reasoningContent;
        
        /**
         * 会话区段ID
         */
        @JsonProperty("section_id")
        private String sectionId;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getConversationId() {
            return conversationId;
        }

        public void setConversationId(String conversationId) {
            this.conversationId = conversationId;
        }

        public String getBotId() {
            return botId;
        }

        public void setBotId(String botId) {
            this.botId = botId;
        }

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Long createdAt) {
            this.createdAt = createdAt;
        }

        public Long getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Long updatedAt) {
            this.updatedAt = updatedAt;
        }

        public Object getMetaData() {
            return metaData;
        }

        public void setMetaData(Object metaData) {
            this.metaData = metaData;
        }

        public String getReasoningContent() {
            return reasoningContent;
        }

        public void setReasoningContent(String reasoningContent) {
            this.reasoningContent = reasoningContent;
        }

        public String getSectionId() {
            return sectionId;
        }

        public void setSectionId(String sectionId) {
            this.sectionId = sectionId;
        }

        @Override
        public String toString() {
            return "CozeMessageItem{" +
                    "id='" + id + '\'' +
                    ", conversationId='" + conversationId + '\'' +
                    ", botId='" + botId + '\'' +
                    ", chatId='" + chatId + '\'' +
                    ", content='" + content + '\'' +
                    ", contentType='" + contentType + '\'' +
                    ", role='" + role + '\'' +
                    ", type='" + type + '\'' +
                    ", createdAt=" + createdAt +
                    ", updatedAt=" + updatedAt +
                    ", metaData=" + metaData +
                    ", reasoningContent='" + reasoningContent + '\'' +
                    ", sectionId='" + sectionId + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "CozeMessageListResponse{" +
                "data=" + data +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}