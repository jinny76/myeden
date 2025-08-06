package com.myeden.dto.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 企业微信发送消息响应对象
 */
public class WeChatSendMessageResponse {
    
    /**
     * 返回码
     */
    @JsonProperty("errcode")
    private Integer errCode;
    
    /**
     * 对返回码的文本描述内容
     */
    @JsonProperty("errmsg")
    private String errMsg;
    
    /**
     * 消息id，用于撤回应用消息
     */
    @JsonProperty("msgid")
    private String msgId;
    
    /**
     * 仅消息类型为"按钮交互型"，"投票选择型"和"多项选择型"的模板卡片消息返回，应用可使用response_code调用更新模版卡片消息接口
     */
    @JsonProperty("response_code")
    private String responseCode;
    
    /**
     * 不合法的userid，不区分大小写，统一转为小写
     */
    @JsonProperty("invaliduser")
    private String invalidUser;
    
    /**
     * 不合法的partyid
     */
    @JsonProperty("invalidparty")
    private String invalidParty;
    
    /**
     * 不合法的标签id
     */
    @JsonProperty("invalidtag")
    private String invalidTag;
    
    /**
     * 没有基础接口许可(包含已过期)的userid
     */
    @JsonProperty("unlicenseduser")
    private String unlicensedUser;
    
    public WeChatSendMessageResponse() {}
    
    // Getters and Setters
    public Integer getErrCode() {
        return errCode;
    }
    
    public void setErrCode(Integer errCode) {
        this.errCode = errCode;
    }
    
    public String getErrMsg() {
        return errMsg;
    }
    
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
    
    public String getMsgId() {
        return msgId;
    }
    
    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
    
    public String getResponseCode() {
        return responseCode;
    }
    
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
    
    public String getInvalidUser() {
        return invalidUser;
    }
    
    public void setInvalidUser(String invalidUser) {
        this.invalidUser = invalidUser;
    }
    
    public String getInvalidParty() {
        return invalidParty;
    }
    
    public void setInvalidParty(String invalidParty) {
        this.invalidParty = invalidParty;
    }
    
    public String getInvalidTag() {
        return invalidTag;
    }
    
    public void setInvalidTag(String invalidTag) {
        this.invalidTag = invalidTag;
    }
    
    public String getUnlicensedUser() {
        return unlicensedUser;
    }
    
    public void setUnlicensedUser(String unlicensedUser) {
        this.unlicensedUser = unlicensedUser;
    }
    
    /**
     * 判断是否发送成功
     */
    public boolean isSuccess() {
        return errCode != null && errCode == 0;
    }
    
    @Override
    public String toString() {
        return "WeChatSendMessageResponse{" +
                "errCode=" + errCode +
                ", errMsg='" + errMsg + '\'' +
                ", msgId='" + msgId + '\'' +
                ", invalidUser='" + invalidUser + '\'' +
                ", invalidParty='" + invalidParty + '\'' +
                ", invalidTag='" + invalidTag + '\'' +
                ", unlicensedUser='" + unlicensedUser + '\'' +
                '}';
    }
}