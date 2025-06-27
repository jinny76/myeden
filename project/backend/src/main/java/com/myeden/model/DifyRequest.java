package com.myeden.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Dify API请求模型
 * 封装Dify API的请求数据结构
 * 
 * @author MyEden Team
 * @version 1.0.0
 */
public class DifyRequest {
    
    /**
     * 输入参数
     */
    @JsonProperty("inputs")
    private Map<String, Object> inputs;
    
    /**
     * 查询参数
     */
    @JsonProperty("query")
    private String query;
    
    /**
     * 响应模式
     */
    @JsonProperty("response_mode")
    private String responseMode = "blocking";
    
    /**
     * 用户ID
     */
    @JsonProperty("user")
    private String user;
    
    /**
     * 对话ID
     */
    @JsonProperty("conversation_id")
    private String conversationId;
    
    /**
     * 文件列表
     */
    @JsonProperty("files")
    private List<Object> files;
    
    /**
     * 构造函数
     */
    public DifyRequest() {}
    
    /**
     * 构造函数
     * @param inputs 输入参数
     * @param query 查询参数
     */
    public DifyRequest(Map<String, Object> inputs, String query) {
        this.inputs = inputs;
        this.query = query;
    }
    
    // Getters and Setters
    public Map<String, Object> getInputs() {
        return inputs;
    }
    
    public void setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public String getResponseMode() {
        return responseMode;
    }
    
    public void setResponseMode(String responseMode) {
        this.responseMode = responseMode;
    }
    
    public String getUser() {
        return user;
    }
    
    public void setUser(String user) {
        this.user = user;
    }
    
    public String getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    
    public List<Object> getFiles() {
        return files;
    }
    
    public void setFiles(List<Object> files) {
        this.files = files;
    }
} 