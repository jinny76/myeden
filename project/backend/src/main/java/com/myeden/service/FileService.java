package com.myeden.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件服务类
 * 
 * 功能说明：
 * - 提供文件上传和管理功能
 * - 支持文件类型验证和大小限制
 * - 提供安全的文件存储路径管理
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
public class FileService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;
    
    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize;
    
    @Value("${file.upload.allowed-types:jpg,jpeg,png,gif}")
    private String allowedTypes;
    
    /**
     * 上传文件
     * @param file 文件
     * @param subDirectory 子目录
     * @return 文件URL
     */
    public String uploadFile(MultipartFile file, String subDirectory) {
        try {
            // 验证文件
            validateFile(file);
            
            // 获取绝对路径
            String absoluteUploadPath = getAbsoluteUploadPath();
            String directory = absoluteUploadPath + "/" + subDirectory;
            
            // 创建目录
            createDirectoryIfNotExists(directory);
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = generateUniqueFilename(extension);
            
            // 保存文件
            Path filePath = Paths.get(directory, filename);
            Files.copy(file.getInputStream(), filePath);
            
            logger.info("文件上传成功: {} -> {}", originalFilename, filePath.toString());
            
            // 返回文件URL
            return "/uploads/" + subDirectory + "/" + filename;
            
        } catch (IOException e) {
            logger.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }
    
    /**
     * 获取绝对上传路径
     * @return 绝对路径
     */
    private String getAbsoluteUploadPath() {
        if (uploadPath.startsWith("./")) {
            String currentDir = System.getProperty("user.dir");
            return currentDir + "/" + uploadPath.substring(2);
        }
        return uploadPath;
    }
    
    /**
     * 验证文件
     * @param file 文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("文件大小超过限制");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }
        
        String extension = getFileExtension(originalFilename);
        if (!isAllowedFileType(extension)) {
            throw new RuntimeException("不支持的文件类型");
        }
    }
    
    /**
     * 创建目录（如果不存在）
     * @param directory 目录路径
     */
    private void createDirectoryIfNotExists(String directory) {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * 检查是否为允许的文件类型
     * @param extension 文件扩展名
     * @return 是否允许
     */
    private boolean isAllowedFileType(String extension) {
        String[] allowedTypeArray = allowedTypes.split(",");
        for (String allowedType : allowedTypeArray) {
            if (allowedType.trim().equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 生成唯一文件名
     * @param extension 文件扩展名
     * @return 唯一文件名
     */
    private String generateUniqueFilename(String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + "." + extension;
    }
    
    /**
     * 删除文件
     * @param fileUrl 文件URL
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
            String relativePath = fileUrl.substring(8); // 移除 "/uploads/"
            Path filePath = Paths.get(uploadPath, relativePath);
            
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                throw new RuntimeException("文件删除失败", e);
            }
        }
    }
    
    /**
     * 上传图片文件
     * @param file 图片文件
     * @return 图片URL
     */
    public String uploadImage(MultipartFile file) {
        return uploadFile(file, "images");
    }
} 