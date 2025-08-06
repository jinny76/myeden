package com.myeden.service;

import com.myeden.dto.wechat.WeChatMediaUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 企业微信媒体文件服务接口
 */
public interface WeChatMediaService {
    
    /**
     * 上传临时素材（通过MultipartFile）
     * 
     * @param file 文件
     * @param mediaType 媒体类型（image、voice、video、file）
     * @return 上传结果
     */
    WeChatMediaUploadResponse uploadMedia(MultipartFile file, String mediaType);
    
    /**
     * 上传临时素材（通过InputStream）
     * 
     * @param inputStream 文件输入流
     * @param filename 文件名
     * @param mediaType 媒体类型（image、voice、video、file）
     * @return 上传结果
     */
    WeChatMediaUploadResponse uploadMedia(InputStream inputStream, String filename, String mediaType);
    
    /**
     * 上传图片并发送图片消息
     * 
     * @param file 图片文件
     * @param toUser 接收用户ID
     * @return 发送结果，包含mediaId和消息发送状态
     */
    WeChatMediaUploadAndSendResult uploadImageAndSend(MultipartFile file, String toUser);
    
    /**
     * 上传语音并发送语音消息
     * 
     * @param file 语音文件
     * @param toUser 接收用户ID
     * @return 发送结果
     */
    WeChatMediaUploadAndSendResult uploadVoiceAndSend(MultipartFile file, String toUser);
    
    /**
     * 上传视频并发送视频消息
     * 
     * @param file 视频文件
     * @param toUser 接收用户ID
     * @param title 视频标题
     * @param description 视频描述
     * @return 发送结果
     */
    WeChatMediaUploadAndSendResult uploadVideoAndSend(MultipartFile file, String toUser, String title, String description);
    
    /**
     * 上传文件并发送文件消息
     * 
     * @param file 文件
     * @param toUser 接收用户ID
     * @return 发送结果
     */
    WeChatMediaUploadAndSendResult uploadFileAndSend(MultipartFile file, String toUser);
    
    /**
     * 检查文件类型是否有效
     * 
     * @param file 文件
     * @param mediaType 媒体类型
     * @return 是否有效
     */
    boolean isValidFileType(MultipartFile file, String mediaType);
    
    /**
     * 检查文件大小是否在限制范围内
     * 
     * @param file 文件
     * @param mediaType 媒体类型
     * @return 是否在限制范围内
     */
    boolean isValidFileSize(MultipartFile file, String mediaType);
    
    /**
     * 上传并发送结果
     */
    public static class WeChatMediaUploadAndSendResult {
        private boolean uploadSuccess;
        private String mediaId;
        private boolean sendSuccess;
        private String msgId;
        private String errorMsg;
        
        public WeChatMediaUploadAndSendResult() {}
        
        public WeChatMediaUploadAndSendResult(boolean uploadSuccess, String mediaId, boolean sendSuccess, String msgId, String errorMsg) {
            this.uploadSuccess = uploadSuccess;
            this.mediaId = mediaId;
            this.sendSuccess = sendSuccess;
            this.msgId = msgId;
            this.errorMsg = errorMsg;
        }
        
        public boolean isUploadSuccess() {
            return uploadSuccess;
        }
        
        public void setUploadSuccess(boolean uploadSuccess) {
            this.uploadSuccess = uploadSuccess;
        }
        
        public String getMediaId() {
            return mediaId;
        }
        
        public void setMediaId(String mediaId) {
            this.mediaId = mediaId;
        }
        
        public boolean isSendSuccess() {
            return sendSuccess;
        }
        
        public void setSendSuccess(boolean sendSuccess) {
            this.sendSuccess = sendSuccess;
        }
        
        public String getMsgId() {
            return msgId;
        }
        
        public void setMsgId(String msgId) {
            this.msgId = msgId;
        }
        
        public String getErrorMsg() {
            return errorMsg;
        }
        
        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }
        
        public boolean isSuccess() {
            return uploadSuccess && sendSuccess;
        }
    }
}