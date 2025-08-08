package com.myeden.dto.coze;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Coze消息列表请求
 * 对应API: POST /v1/conversation/message/list
 */
public class CozeMessageListRequest {
    
    /**
     * 排序方式：asc 升序，desc 降序，默认为 desc
     */
    private String order = "desc";
    
    /**
     * 对话ID（可选）
     */
    @JsonProperty("chat_id")
    private String chatId;
    
    /**
     * 获取指定消息ID之前的消息（用于向前分页）
     */
    @JsonProperty("before_id")
    private String beforeId;
    
    /**
     * 获取指定消息ID之后的消息（用于向后分页）
     */
    @JsonProperty("after_id")
    private String afterId;
    
    /**
     * 限制返回的消息数量，默认20，最大100
     */
    private Integer limit = 20;
    
    public CozeMessageListRequest() {}
    
    public CozeMessageListRequest(String afterId, Integer limit) {
        this.afterId = afterId;
        this.limit = limit != null ? Math.min(limit, 100) : 20;
    }

    // Getters and Setters
    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getBeforeId() {
        return beforeId;
    }

    public void setBeforeId(String beforeId) {
        this.beforeId = beforeId;
    }

    public String getAfterId() {
        return afterId;
    }

    public void setAfterId(String afterId) {
        this.afterId = afterId;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit != null ? Math.min(limit, 100) : 20;
    }

    @Override
    public String toString() {
        return "CozeMessageListRequest{" +
                "order='" + order + '\'' +
                ", chatId='" + chatId + '\'' +
                ", beforeId='" + beforeId + '\'' +
                ", afterId='" + afterId + '\'' +
                ", limit=" + limit +
                '}';
    }
}