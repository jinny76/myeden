package com.myeden.dto.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Coze v3聊天请求对象
 */
public class CozeChatRequest {
    
    /**
     * 智能体的ID，必选
     */
    @JsonProperty("bot_id")
    private String botId;
    
    /**
     * 用户的ID，用于标识用户身份，必选
     */
    @JsonProperty("user_id")
    private String userId;
    
    /**
     * 标识对话发生在哪一次会话中，可选
     */
    @JsonProperty("conversation_id")
    private String conversationId;
    
    /**
     * 附加消息列表，可选
     */
    @JsonProperty("additional_messages")
    private List<CozeMessage> additionalMessages;
    
    /**
     * 是否启用流式返回，可选，默认false
     */
    private boolean stream = false;
    
    /**
     * 是否保存本次对话记录，可选，默认true
     */
    @JsonProperty("auto_save_history")
    private boolean autoSaveHistory = true;
    
    /**
     * 附加的元信息，支持传入用户自定义数据
     */
    @JsonProperty("meta_data")
    private Map<String, Object> metaData;
    
    /**
     * 传入的自定义变量，可选
     */
    private Map<String, Object> parameters;
    
    /**
     * 设置问答节点返回的内容是否为卡片形式，可选，默认false
     */
    @JsonProperty("enable_card")
    private Boolean enableCard;

    public CozeChatRequest() {}
    
    public CozeChatRequest(String botId, String userId) {
        this.botId = botId;
        this.userId = userId;
    }
    
    public CozeChatRequest(String botId, String userId, List<CozeMessage> additionalMessages) {
        this.botId = botId;
        this.userId = userId;
        this.additionalMessages = additionalMessages;
    }

    // Getters and Setters
    public String getBotId() {
        return botId;
    }

    public void setBotId(String botId) {
        this.botId = botId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public List<CozeMessage> getAdditionalMessages() {
        return additionalMessages;
    }

    public void setAdditionalMessages(List<CozeMessage> additionalMessages) {
        this.additionalMessages = additionalMessages;
    }

    // 保持向后兼容性
    public List<CozeMessage> getMessages() {
        return additionalMessages;
    }

    public void setMessages(List<CozeMessage> messages) {
        this.additionalMessages = messages;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public boolean isAutoSaveHistory() {
        return autoSaveHistory;
    }

    public void setAutoSaveHistory(boolean autoSaveHistory) {
        this.autoSaveHistory = autoSaveHistory;
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Boolean getEnableCard() {
        return enableCard;
    }

    public void setEnableCard(Boolean enableCard) {
        this.enableCard = enableCard;
    }

    @Override
    public String toString() {
        return "CozeChatRequest{" +
                "botId='" + botId + '\'' +
                ", userId='" + userId + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", additionalMessages=" + additionalMessages +
                ", stream=" + stream +
                ", autoSaveHistory=" + autoSaveHistory +
                ", metaData=" + metaData +
                ", parameters=" + parameters +
                ", enableCard=" + enableCard +
                '}';
    }
}