package com.myeden.dto.coze;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Coze消息对象
 */
public class CozeMessage {
    
    /**
     * 消息角色：user/assistant/system
     */
    private String role;
    
    /**
     * 消息类型：text/image/file等
     */
    private String type = "question";
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息内容类型：text/object_string/card
     */
    @JsonProperty("content_type")
    private String contentType = "text";

    public CozeMessage() {}
    
    public CozeMessage(String role, String content) {
        this.role = role;
        this.content = content;
        this.type = "text";
        this.contentType = "text";
    }
    
    public CozeMessage(String role, String type, String content, String contentType) {
        this.role = role;
        this.type = type;
        this.content = content;
        this.contentType = contentType;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "CozeMessage{" +
                "role='" + role + '\'' +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", contentType='" + contentType + '\'' +
                '}';
    }
}