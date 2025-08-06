package com.myeden.dto.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 企业微信消息对象
 */
public class WeChatMessage {
    
    /**
     * 消息类型：text, image, voice, video, file等
     */
    @JsonProperty("MsgType")
    private String msgType;
    
    /**
     * 消息ID
     */
    @JsonProperty("MsgId")
    private String msgId;
    
    /**
     * 发送方账号
     */
    @JsonProperty("FromUserName")
    private String fromUserName;
    
    /**
     * 接收方账号
     */
    @JsonProperty("ToUserName")
    private String toUserName;
    
    /**
     * 消息创建时间
     */
    @JsonProperty("CreateTime")
    private Long createTime;
    
    /**
     * 应用ID
     */
    @JsonProperty("AgentID")
    private String agentId;
    
    /**
     * 文本消息内容
     */
    @JsonProperty("Content")
    private String content;
    
    /**
     * 媒体文件ID
     */
    @JsonProperty("MediaId")
    private String mediaId;
    
    /**
     * 图片链接
     */
    @JsonProperty("PicUrl")
    private String picUrl;
    
    /**
     * 语音格式
     */
    @JsonProperty("Format")
    private String format;
    
    /**
     * 事件类型（当MsgType为event时）
     */
    @JsonProperty("Event")
    private String event;
    
    /**
     * 事件KEY值
     */
    @JsonProperty("EventKey")
    private String eventKey;
    
    // Getters and Setters
    public String getMsgType() {
        return msgType;
    }
    
    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
    
    public String getMsgId() {
        return msgId;
    }
    
    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
    
    public String getFromUserName() {
        return fromUserName;
    }
    
    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }
    
    public String getToUserName() {
        return toUserName;
    }
    
    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }
    
    public Long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getMediaId() {
        return mediaId;
    }
    
    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }
    
    public String getPicUrl() {
        return picUrl;
    }
    
    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public String getEvent() {
        return event;
    }
    
    public void setEvent(String event) {
        this.event = event;
    }
    
    public String getEventKey() {
        return eventKey;
    }
    
    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }
    
    @Override
    public String toString() {
        return "WeChatMessage{" +
                "msgType='" + msgType + '\'' +
                ", msgId='" + msgId + '\'' +
                ", fromUserName='" + fromUserName + '\'' +
                ", toUserName='" + toUserName + '\'' +
                ", createTime=" + createTime +
                ", agentId='" + agentId + '\'' +
                ", content='" + content + '\'' +
                ", event='" + event + '\'' +
                '}';
    }
}