package com.myeden.dto.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Coze消息详情响应对象
 * 用于查看指定对话中除Query以外的其他消息
 */
public class CozeMessageDetailResponse {
    
    /**
     * 消息详情列表
     */
    private List<ChatV3MessageDetail> data;
    
    /**
     * 响应详情信息
     */
    private ResponseDetail detail;
    
    /**
     * 调用状态码，0表示调用成功
     */
    private int code = 0;
    
    /**
     * 状态信息
     */
    private String msg = "Success";

    public CozeMessageDetailResponse() {}

    /**
     * 便捷构造方法
     */
    public static CozeMessageDetailResponse success(List<ChatV3MessageDetail> data) {
        CozeMessageDetailResponse response = new CozeMessageDetailResponse();
        response.setData(data);
        response.setCode(0);
        response.setMsg("Success");
        return response;
    }
    
    public static CozeMessageDetailResponse error(int code, String msg) {
        CozeMessageDetailResponse response = new CozeMessageDetailResponse();
        response.setCode(code);
        response.setMsg(msg);
        return response;
    }
    
    public boolean isSuccess() {
        return code == 0;
    }

    // Getters and Setters
    public List<ChatV3MessageDetail> getData() {
        return data;
    }

    public void setData(List<ChatV3MessageDetail> data) {
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

    /**
     * ChatV3MessageDetail - 消息详情对象
     */
    public static class ChatV3MessageDetail {
        /**
         * Message ID，即消息的唯一标识
         */
        private String id;
        
        /**
         * 此消息所在的会话ID
         */
        @JsonProperty("conversation_id")
        private String conversationId;
        
        /**
         * 发送这条消息的实体
         * user：代表该条消息内容是用户发送的
         * assistant：代表该条消息内容是智能体发送的
         */
        private String role;
        
        /**
         * 消息类型
         * question：用户输入内容
         * answer：智能体返回给用户的消息内容
         * function_call：智能体对话过程中调用函数的中间结果
         * tool_output：调用工具后返回的结果
         * tool_response：调用工具后返回的结果
         * follow_up：推荐问题相关的回复内容
         * verbose：多answer场景下的完成标志
         */
        private String type;
        
        /**
         * 编写此消息的智能体ID
         */
        @JsonProperty("bot_id")
        private String botId;
        
        /**
         * Chat ID
         */
        @JsonProperty("chat_id")
        private String chatId;
        
        /**
         * 上下文片段ID
         */
        @JsonProperty("section_id")
        private String sectionId;
        
        /**
         * 消息的内容
         */
        private String content;
        
        /**
         * 创建消息时的附加消息
         */
        @JsonProperty("meta_data")
        private Map<String, Object> metaData;
        
        /**
         * 消息的创建时间，格式为10位的Unixtime时间戳
         */
        @JsonProperty("created_at")
        private Long createdAt;
        
        /**
         * 消息的更新时间，格式为10位的Unixtime时间戳
         */
        @JsonProperty("updated_at")
        private Long updatedAt;
        
        /**
         * 消息内容的类型
         * text：文本
         * object_string：多模态内容
         * card：卡片
         */
        @JsonProperty("content_type")
        private String contentType;
        
        /**
         * DeepSeek-R1模型的思维链（CoT）
         * 仅在使用DeepSeek-R1模型时才会返回
         */
        @JsonProperty("reasoning_content")
        private String reasoningContent;

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

        public String getSectionId() {
            return sectionId;
        }

        public void setSectionId(String sectionId) {
            this.sectionId = sectionId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Map<String, Object> getMetaData() {
            return metaData;
        }

        public void setMetaData(Map<String, Object> metaData) {
            this.metaData = metaData;
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

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getReasoningContent() {
            return reasoningContent;
        }

        public void setReasoningContent(String reasoningContent) {
            this.reasoningContent = reasoningContent;
        }

        @Override
        public String toString() {
            return "ChatV3MessageDetail{" +
                    "id='" + id + '\'' +
                    ", conversationId='" + conversationId + '\'' +
                    ", role='" + role + '\'' +
                    ", type='" + type + '\'' +
                    ", botId='" + botId + '\'' +
                    ", chatId='" + chatId + '\'' +
                    ", sectionId='" + sectionId + '\'' +
                    ", content='" + content + '\'' +
                    ", metaData=" + metaData +
                    ", createdAt=" + createdAt +
                    ", updatedAt=" + updatedAt +
                    ", contentType='" + contentType + '\'' +
                    ", reasoningContent='" + reasoningContent + '\'' +
                    '}';
        }
    }

    /**
     * ResponseDetail - 响应详情信息
     */
    public static class ResponseDetail {
        /**
         * 本次请求的日志ID
         */
        private String logid;

        public String getLogid() {
            return logid;
        }

        public void setLogid(String logid) {
            this.logid = logid;
        }

        @Override
        public String toString() {
            return "ResponseDetail{" +
                    "logid='" + logid + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "CozeMessageDetailResponse{" +
                "data=" + data +
                ", detail=" + detail +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
