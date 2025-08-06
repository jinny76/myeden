package com.myeden.dto.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 企业微信临时素材上传响应对象
 */
public class WeChatMediaUploadResponse {
    
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
     * 媒体文件类型，分别有图片（image）、语音（voice）、视频（video），普通文件（file）
     */
    @JsonProperty("type")
    private String type;
    
    /**
     * 媒体文件上传后获取的唯一标识，3天内有效
     */
    @JsonProperty("media_id")
    private String mediaId;
    
    /**
     * 媒体文件上传时间戳
     */
    @JsonProperty("created_at")
    private Long createdAt;
    
    public WeChatMediaUploadResponse() {}
    
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getMediaId() {
        return mediaId;
    }
    
    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }
    
    public Long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * 判断是否上传成功
     */
    public boolean isSuccess() {
        return errCode != null && errCode == 0;
    }
    
    @Override
    public String toString() {
        return "WeChatMediaUploadResponse{" +
                "errCode=" + errCode +
                ", errMsg='" + errMsg + '\'' +
                ", type='" + type + '\'' +
                ", mediaId='" + mediaId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}