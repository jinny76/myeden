package com.myeden.dto.wechat;

/**
 * 企业微信回调请求
 */
public class WeChatCallbackRequest {
    
    /**
     * 企业微信加密签名
     */
    private String msgSignature;
    
    /**
     * 时间戳
     */
    private String timestamp;
    
    /**
     * 随机数
     */
    private String nonce;
    
    /**
     * 随机字符串（用于验证URL）
     */
    private String echostr;
    
    /**
     * 加密的消息体
     */
    private String data;
    
    public WeChatCallbackRequest() {}
    
    public WeChatCallbackRequest(String msgSignature, String timestamp, String nonce, String echostr) {
        this.msgSignature = msgSignature;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.echostr = echostr;
    }
    
    // Getters and Setters
    public String getMsgSignature() {
        return msgSignature;
    }
    
    public void setMsgSignature(String msgSignature) {
        this.msgSignature = msgSignature;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getNonce() {
        return nonce;
    }
    
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
    
    public String getEchostr() {
        return echostr;
    }
    
    public void setEchostr(String echostr) {
        this.echostr = echostr;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "WeChatCallbackRequest{" +
                "msgSignature='" + msgSignature + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", nonce='" + nonce + '\'' +
                ", echostr='" + echostr + '\'' +
                ", hasData=" + (data != null && !data.isEmpty()) +
                '}';
    }
}