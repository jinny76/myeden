package com.myeden.dto.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 企业微信发送消息请求对象
 */
public class WeChatSendMessageRequest {
    
    /**
     * 指定接收消息的成员，成员ID列表（多个接收者用'|'分隔，最多支持1000个）
     * 特殊情况：指定为"@all"，则向该企业应用的全部成员发送
     */
    @JsonProperty("touser")
    private String toUser;
    
    /**
     * 指定接收消息的部门，部门ID列表，多个接收者用'|'分隔，最多支持100个
     * 当touser为"@all"时忽略本参数
     */
    @JsonProperty("toparty")
    private String toParty;
    
    /**
     * 指定接收消息的标签，标签ID列表，多个接收者用'|'分隔，最多支持100个
     * 当touser为"@all"时忽略本参数
     */
    @JsonProperty("totag")
    private String toTag;
    
    /**
     * 消息类型，此时固定为：text
     */
    @JsonProperty("msgtype")
    private String msgType;
    
    /**
     * 企业应用的id，整型。企业内部开发，可在应用的设置页面查看
     */
    @JsonProperty("agentid")
    private String agentId;
    
    /**
     * 文本消息内容
     */
    @JsonProperty("text")
    private TextContent text;
    
    /**
     * 表示是否是保密消息，0表示可对外分享，1表示不能分享且内容显示水印，默认为0
     */
    @JsonProperty("safe")
    private Integer safe = 0;
    
    /**
     * 表示是否开启id转译，0表示否，1表示是，默认0。
     * 仅第三方应用需要用到，企业自建应用可以忽略。
     */
    @JsonProperty("enable_id_trans")
    private Integer enableIdTrans = 0;
    
    /**
     * 表示是否开启重复消息检查，0表示否，1表示是，默认0
     */
    @JsonProperty("enable_duplicate_check")
    private Integer enableDuplicateCheck = 0;
    
    /**
     * 表示是否重复消息检查的时间间隔，默认1800s，最大不超过4小时
     */
    @JsonProperty("duplicate_check_interval")
    private Integer duplicateCheckInterval = 1800;
    
    public WeChatSendMessageRequest() {}
    
    public WeChatSendMessageRequest(String toUser, String msgType, String agentId, TextContent text) {
        this.toUser = toUser;
        this.msgType = msgType;
        this.agentId = agentId;
        this.text = text;
    }
    
    // Getters and Setters
    public String getToUser() {
        return toUser;
    }
    
    public void setToUser(String toUser) {
        this.toUser = toUser;
    }
    
    public String getToParty() {
        return toParty;
    }
    
    public void setToParty(String toParty) {
        this.toParty = toParty;
    }
    
    public String getToTag() {
        return toTag;
    }
    
    public void setToTag(String toTag) {
        this.toTag = toTag;
    }
    
    public String getMsgType() {
        return msgType;
    }
    
    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
    
    public TextContent getText() {
        return text;
    }
    
    public void setText(TextContent text) {
        this.text = text;
    }
    
    public Integer getSafe() {
        return safe;
    }
    
    public void setSafe(Integer safe) {
        this.safe = safe;
    }
    
    public Integer getEnableIdTrans() {
        return enableIdTrans;
    }
    
    public void setEnableIdTrans(Integer enableIdTrans) {
        this.enableIdTrans = enableIdTrans;
    }
    
    public Integer getEnableDuplicateCheck() {
        return enableDuplicateCheck;
    }
    
    public void setEnableDuplicateCheck(Integer enableDuplicateCheck) {
        this.enableDuplicateCheck = enableDuplicateCheck;
    }
    
    public Integer getDuplicateCheckInterval() {
        return duplicateCheckInterval;
    }
    
    public void setDuplicateCheckInterval(Integer duplicateCheckInterval) {
        this.duplicateCheckInterval = duplicateCheckInterval;
    }
    
    /**
     * 文本消息内容
     */
    public static class TextContent {
        /**
         * 消息内容，最长不超过2048个字节，超过将截断（支持id转译）
         */
        @JsonProperty("content")
        private String content;
        
        public TextContent() {}
        
        public TextContent(String content) {
            this.content = content;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
}