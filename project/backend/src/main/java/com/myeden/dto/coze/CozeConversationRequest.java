package com.myeden.dto.coze;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Coze创建对话请求对象
 */
public class CozeConversationRequest {
    
    /**
     * 会话的名称，默认为空，最多支持100个字符
     */
    private String name;
    
    /**
     * 会话中的消息内容
     */
    private List<CozeMessage> messages = new ArrayList<CozeMessage>();
    
    /**
     * 该会话在哪个渠道创建，默认1024表示API
     */
    @JsonProperty("connector_id")
    private String connectorId = "1024";
    
    /**
     * 附加信息，通常用于封装一些业务相关的字段
     */
    @JsonProperty("meta_data")
    private Map<String, Object> metaData = new HashMap<String, Object>();

    public CozeConversationRequest() {}
    
    public CozeConversationRequest(String name) {
        this.name = name;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CozeMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<CozeMessage> messages) {
        this.messages = messages;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
    }

    @Override
    public String toString() {
        return "CozeConversationRequest{" +
                "name='" + name + '\'' +
                ", messages=" + messages +
                ", connectorId='" + connectorId + '\'' +
                ", metaData=" + metaData +
                '}';
    }
}