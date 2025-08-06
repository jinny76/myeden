package com.myeden.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeden.config.WeChatWorkProperties;
import com.myeden.dto.wechat.WeChatMediaUploadResponse;
import com.myeden.dto.wechat.WeChatSendMessageResponse;
import com.myeden.service.WeChatAccessTokenService;
import com.myeden.service.WeChatMediaService;
import com.myeden.service.WeChatSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业微信媒体文件服务实现类
 */
@Service
public class WeChatMediaServiceImpl implements WeChatMediaService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeChatMediaServiceImpl.class);
    
    private static final String UPLOAD_MEDIA_URL = "https://qyapi.weixin.qq.com/cgi-bin/media/upload";
    
    // 文件类型限制
    private static final Map<String, List<String>> ALLOWED_FILE_TYPES = new HashMap<>();
    private static final Map<String, Long> MAX_FILE_SIZES = new HashMap<>();
    
    static {
        // 图片类型限制
        ALLOWED_FILE_TYPES.put("image", Arrays.asList("jpg", "jpeg", "png", "gif", "bmp"));
        MAX_FILE_SIZES.put("image", 10L * 1024 * 1024); // 10MB
        
        // 语音类型限制
        ALLOWED_FILE_TYPES.put("voice", Arrays.asList("amr", "mp3", "wav", "silk"));
        MAX_FILE_SIZES.put("voice", 2L * 1024 * 1024); // 2MB
        
        // 视频类型限制
        ALLOWED_FILE_TYPES.put("video", Arrays.asList("mp4", "avi", "mov", "wmv", "3gp"));
        MAX_FILE_SIZES.put("video", 20L * 1024 * 1024); // 20MB
        
        // 普通文件类型限制
        ALLOWED_FILE_TYPES.put("file", Arrays.asList("doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf", "txt", "zip", "rar"));
        MAX_FILE_SIZES.put("file", 20L * 1024 * 1024); // 20MB
    }
    
    private final WeChatWorkProperties weChatProperties;
    private final WeChatAccessTokenService accessTokenService;
    private final WeChatSendService weChatSendService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public WeChatMediaServiceImpl(WeChatWorkProperties weChatProperties,
                                  WeChatAccessTokenService accessTokenService,
                                  WeChatSendService weChatSendService) {
        this.weChatProperties = weChatProperties;
        this.accessTokenService = accessTokenService;
        this.weChatSendService = weChatSendService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public WeChatMediaUploadResponse uploadMedia(MultipartFile file, String mediaType) {
        try {
            // 将文件转换为字节数组，避免InputStreamResource的重复读取问题
            byte[] fileBytes = file.getBytes();
            String filename = file.getOriginalFilename();
            return uploadMediaFromBytes(fileBytes, filename, mediaType);
        } catch (IOException e) {
            logger.error("读取文件内容失败", e);
            return createErrorUploadResponse(-1, "读取文件内容失败: " + e.getMessage());
        }
    }
    
    @Override
    public WeChatMediaUploadResponse uploadMedia(InputStream inputStream, String filename, String mediaType) {
        try {
            // 将InputStream转换为字节数组
            byte[] fileBytes = inputStream.readAllBytes();
            return uploadMediaFromBytes(fileBytes, filename, mediaType);
        } catch (IOException e) {
            logger.error("读取输入流失败", e);
            return createErrorUploadResponse(-1, "读取输入流失败: " + e.getMessage());
        }
    }
    
    /**
     * 从字节数组上传媒体文件的核心方法
     */
    private WeChatMediaUploadResponse uploadMediaFromBytes(byte[] fileBytes, String filename, String mediaType) {
        try {
            if (!weChatProperties.isEnabled()) {
                logger.warn("企业微信功能未启用");
                return createErrorUploadResponse(-1, "企业微信功能未启用");
            }
            
            // 获取Access Token
            String accessToken = accessTokenService.getAccessToken();
            if (accessToken == null || accessToken.trim().isEmpty()) {
                logger.error("获取Access Token失败");
                return createErrorUploadResponse(-2, "获取Access Token失败");
            }
            
            // 构建请求URL
            String url = UPLOAD_MEDIA_URL + "?access_token=" + accessToken + "&type=" + mediaType;
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.add("User-Agent", "MyEden-WeChat-Client/1.0");
            
            // 使用ByteArrayResource替代InputStreamResource
            ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };
            
            // 构建multipart请求
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("media", fileResource);
            
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
            
            logger.info("上传企业微信媒体文件: filename={}, mediaType={}, size={}", filename, mediaType, fileBytes.length);
            logger.debug("请求URL: {}", url);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                WeChatMediaUploadResponse result = objectMapper.readValue(response.getBody(), WeChatMediaUploadResponse.class);
                
                if (result.isSuccess()) {
                    logger.info("媒体文件上传成功: mediaId={}, type={}", result.getMediaId(), result.getType());
                } else {
                    logger.error("媒体文件上传失败: errCode={}, errMsg={}", 
                               result.getErrCode(), result.getErrMsg());
                    
                    // 如果是token过期，尝试刷新token并重试一次
                    if (result.getErrCode() != null && 
                        (result.getErrCode() == 40014 || result.getErrCode() == 42001)) {
                        logger.info("Access Token过期，尝试刷新并重试");
                        accessToken = accessTokenService.refreshAccessToken();
                        if (accessToken != null) {
                            return retryUploadWithNewToken(fileBytes, filename, mediaType, accessToken);
                        }
                    }
                }
                
                return result;
            } else {
                logger.error("调用企业微信上传媒体文件API失败: status={}", response.getStatusCode());
                return createErrorUploadResponse(-3, "调用企业微信API失败");
            }
            
        } catch (Exception e) {
            logger.error("上传媒体文件时出现异常", e);
            return createErrorUploadResponse(-4, "上传媒体文件时出现异常: " + e.getMessage());
        }
    }
    
    @Override
    public WeChatMediaUploadAndSendResult uploadImageAndSend(MultipartFile file, String toUser) {
        // 验证文件
        if (!isValidFileType(file, "image")) {
            return new WeChatMediaUploadAndSendResult(false, null, false, null, 
                "不支持的图片格式，支持的格式: " + ALLOWED_FILE_TYPES.get("image"));
        }
        
        if (!isValidFileSize(file, "image")) {
            return new WeChatMediaUploadAndSendResult(false, null, false, null, 
                "图片文件大小超限，最大支持: " + (MAX_FILE_SIZES.get("image") / 1024 / 1024) + "MB");
        }
        
        // 上传文件
        WeChatMediaUploadResponse uploadResponse = uploadMedia(file, "image");
        if (!uploadResponse.isSuccess()) {
            return new WeChatMediaUploadAndSendResult(false, null, false, null, 
                "上传失败: " + uploadResponse.getErrMsg());
        }
        
        // 发送消息
        WeChatSendMessageResponse sendResponse = weChatSendService.sendImageMessage(toUser, uploadResponse.getMediaId());
        return new WeChatMediaUploadAndSendResult(
            true, 
            uploadResponse.getMediaId(), 
            sendResponse.isSuccess(), 
            sendResponse.getMsgId(), 
            sendResponse.isSuccess() ? null : sendResponse.getErrMsg()
        );
    }
    
    @Override
    public WeChatMediaUploadAndSendResult uploadVoiceAndSend(MultipartFile file, String toUser) {
        // 验证文件
        if (!isValidFileType(file, "voice")) {
            return new WeChatMediaUploadAndSendResult(false, null, false, null, 
                "不支持的语音格式，支持的格式: " + ALLOWED_FILE_TYPES.get("voice"));
        }
        
        if (!isValidFileSize(file, "voice")) {
            return new WeChatMediaUploadAndSendResult(false, null, false, null, 
                "语音文件大小超限，最大支持: " + (MAX_FILE_SIZES.get("voice") / 1024 / 1024) + "MB");
        }
        
        // 上传文件
        WeChatMediaUploadResponse uploadResponse = uploadMedia(file, "voice");
        if (!uploadResponse.isSuccess()) {
            return new WeChatMediaUploadAndSendResult(false, null, false, null, 
                "上传失败: " + uploadResponse.getErrMsg());
        }
        
        // 发送消息
        WeChatSendMessageResponse sendResponse = weChatSendService.sendVoiceMessage(toUser, uploadResponse.getMediaId());
        return new WeChatMediaUploadAndSendResult(
            true, 
            uploadResponse.getMediaId(), 
            sendResponse.isSuccess(), 
            sendResponse.getMsgId(), 
            sendResponse.isSuccess() ? null : sendResponse.getErrMsg()
        );
    }
    
    @Override
    public WeChatMediaUploadAndSendResult uploadVideoAndSend(MultipartFile file, String toUser, String title, String description) {
        // 验证文件
        if (!isValidFileType(file, "video")) {
            return new WeChatMediaUploadAndSendResult(false, null, false, null, 
                "不支持的视频格式，支持的格式: " + ALLOWED_FILE_TYPES.get("video"));
        }
        
        if (!isValidFileSize(file, "video")) {
            return new WeChatMediaUploadAndSendResult(false, null, false, null, 
                "视频文件大小超限，最大支持: " + (MAX_FILE_SIZES.get("video") / 1024 / 1024) + "MB");
        }
        
        // 上传文件
        WeChatMediaUploadResponse uploadResponse = uploadMedia(file, "video");
        if (!uploadResponse.isSuccess()) {
            return new WeChatMediaUploadAndSendResult(false, null, false, null, 
                "上传失败: " + uploadResponse.getErrMsg());
        }
        
        // 发送消息
        WeChatSendMessageResponse sendResponse = weChatSendService.sendVideoMessage(toUser, uploadResponse.getMediaId(), title, description);
        return new WeChatMediaUploadAndSendResult(
            true, 
            uploadResponse.getMediaId(), 
            sendResponse.isSuccess(), 
            sendResponse.getMsgId(), 
            sendResponse.isSuccess() ? null : sendResponse.getErrMsg()
        );
    }
    
    @Override
    public WeChatMediaUploadAndSendResult uploadFileAndSend(MultipartFile file, String toUser) {
        // 验证文件
        if (!isValidFileType(file, "file")) {
            return new WeChatMediaUploadAndSendResult(false, null, false, null, 
                "不支持的文件格式，支持的格式: " + ALLOWED_FILE_TYPES.get("file"));
        }
        
        if (!isValidFileSize(file, "file")) {
            return new WeChatMediaUploadAndSendResult(false, null, false, null, 
                "文件大小超限，最大支持: " + (MAX_FILE_SIZES.get("file") / 1024 / 1024) + "MB");
        }
        
        // 上传文件
        WeChatMediaUploadResponse uploadResponse = uploadMedia(file, "file");
        if (!uploadResponse.isSuccess()) {
            return new WeChatMediaUploadAndSendResult(false, null, false, null, 
                "上传失败: " + uploadResponse.getErrMsg());
        }
        
        // 发送消息
        WeChatSendMessageResponse sendResponse = weChatSendService.sendFileMessage(toUser, uploadResponse.getMediaId());
        return new WeChatMediaUploadAndSendResult(
            true, 
            uploadResponse.getMediaId(), 
            sendResponse.isSuccess(), 
            sendResponse.getMsgId(), 
            sendResponse.isSuccess() ? null : sendResponse.getErrMsg()
        );
    }
    
    @Override
    public boolean isValidFileType(MultipartFile file, String mediaType) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        List<String> allowedExtensions = ALLOWED_FILE_TYPES.get(mediaType);
        
        return allowedExtensions != null && allowedExtensions.contains(extension);
    }
    
    @Override
    public boolean isValidFileSize(MultipartFile file, String mediaType) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        Long maxSize = MAX_FILE_SIZES.get(mediaType);
        if (maxSize == null) {
            return false;
        }
        
        return file.getSize() <= maxSize;
    }
    
    /**
     * 使用新的Access Token重试上传
     */
    private WeChatMediaUploadResponse retryUploadWithNewToken(byte[] fileBytes, String filename, String mediaType, String accessToken) {
        try {
            String url = UPLOAD_MEDIA_URL + "?access_token=" + accessToken + "&type=" + mediaType;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.add("User-Agent", "MyEden-WeChat-Client/1.0");
            
            ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("media", fileResource);
            
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
            
            logger.info("使用新Access Token重试上传媒体文件");
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                WeChatMediaUploadResponse result = objectMapper.readValue(response.getBody(), WeChatMediaUploadResponse.class);
                
                if (result.isSuccess()) {
                    logger.info("重试上传成功: mediaId={}", result.getMediaId());
                } else {
                    logger.error("重试上传失败: errCode={}, errMsg={}", 
                               result.getErrCode(), result.getErrMsg());
                }
                
                return result;
            } else {
                logger.error("重试调用企业微信上传API失败: status={}", response.getStatusCode());
                return createErrorUploadResponse(-3, "重试调用企业微信API失败");
            }
            
        } catch (Exception e) {
            logger.error("重试上传媒体文件时出现异常", e);
            return createErrorUploadResponse(-4, "重试上传媒体文件时出现异常: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1);
    }
    
    /**
     * 创建错误上传响应
     */
    private WeChatMediaUploadResponse createErrorUploadResponse(int errCode, String errMsg) {
        WeChatMediaUploadResponse response = new WeChatMediaUploadResponse();
        response.setErrCode(errCode);
        response.setErrMsg(errMsg);
        return response;
    }
}