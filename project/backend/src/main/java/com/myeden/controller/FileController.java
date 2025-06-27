package com.myeden.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件访问控制器
 * 
 * 功能说明：
 * - 提供文件下载和访问服务
 * - 支持图片、文档等文件的直接访问
 * - 提供安全的文件访问控制
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/files")
public class FileController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;
    
    /**
     * 测试文件访问权限
     * GET /api/v1/files/test
     */
    @GetMapping("/test")
    public ResponseEntity<String> testAccess() {
        logger.info("文件访问测试接口被调用");
        return ResponseEntity.ok("文件访问接口正常工作");
    }
    
    /**
     * 获取文件
     * GET /api/v1/files/{subDirectory}/{filename}
     * 
     * @param subDirectory 子目录
     * @param filename 文件名
     * @return 文件资源
     */
    @GetMapping("/{subDirectory}/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String subDirectory, @PathVariable String filename) {
        try {
            // 获取绝对路径
            String absoluteUploadPath = getAbsoluteUploadPath();
            Path filePath = Paths.get(absoluteUploadPath, subDirectory, filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            logger.info("请求文件: {} -> {}", filePath.toString(), resource.exists());
            
            if (resource.exists() && resource.isReadable()) {
                // 设置响应头
                HttpHeaders headers = new HttpHeaders();
                
                // 根据文件扩展名设置Content-Type
                String contentType = getContentType(filename);
                headers.setContentType(MediaType.parseMediaType(contentType));
                
                // 允许跨域访问
                headers.setAccessControlAllowOrigin("*");
                
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } else {
                logger.warn("文件不存在或不可读: {}", filePath.toString());
                return ResponseEntity.notFound().build();
            }
            
        } catch (MalformedURLException e) {
            logger.error("文件路径错误", e);
            return ResponseEntity.badRequest().build();
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
     * 根据文件名获取Content-Type
     * @param filename 文件名
     * @return Content-Type
     */
    private String getContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "pdf":
                return "application/pdf";
            case "txt":
                return "text/plain";
            case "json":
                return "application/json";
            case "xml":
                return "application/xml";
            default:
                return "application/octet-stream";
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
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }
} 