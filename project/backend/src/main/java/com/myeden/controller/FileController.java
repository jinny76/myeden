package com.myeden.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件访问控制器
 * 
 * 功能说明：
 * - 提供文件下载和访问服务
 * - 支持图片、文档等文件的直接访问
 * - 提供安全的文件访问控制
 * - 支持ETag缓存和条件请求
 * - 本地文件元数据缓存优化
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
    
    @Value("${file.cache.enabled:true}")
    private boolean cacheEnabled;
    
    @Value("${file.cache.max-size:1000}")
    private int maxCacheSize;
    
    @Value("${file.cache.ttl:3600}")
    private int cacheTtlSeconds;
    
    // 文件元数据缓存（文件路径 -> 文件元数据）
    private final ConcurrentHashMap<String, FileMetadata> fileMetadataCache = new ConcurrentHashMap<>();
    
    /**
     * 文件元数据内部类
     */
    private static class FileMetadata {
        private final String etag;
        private final long lastModified;
        private final long fileSize;
        private final String contentType;
        private final LocalDateTime cacheTime;
        
        public FileMetadata(String etag, long lastModified, long fileSize, String contentType) {
            this.etag = etag;
            this.lastModified = lastModified;
            this.fileSize = fileSize;
            this.contentType = contentType;
            this.cacheTime = LocalDateTime.now();
        }
        
        public String getEtag() { return etag; }
        public long getLastModified() { return lastModified; }
        public long getFileSize() { return fileSize; }
        public String getContentType() { return contentType; }
        public LocalDateTime getCacheTime() { return cacheTime; }
        
        public boolean isExpired(int ttlSeconds) {
            return LocalDateTime.now().isAfter(cacheTime.plusSeconds(ttlSeconds));
        }
    }
    
    /**
     * 获取文件
     * GET /api/v1/files/{subDirectory}/{filename}
     * 
     * @param subDirectory 子目录
     * @param filename 文件名
     * @param ifNoneMatch ETag条件请求头
     * @param ifModifiedSince 修改时间条件请求头
     * @return 文件资源
     */
    @GetMapping("/{subDirectory}/{filename:.+}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String subDirectory, 
            @PathVariable String filename,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
            @RequestHeader(value = "If-Modified-Since", required = false) String ifModifiedSince) {
        
        try {
            // 获取绝对路径
            String absoluteUploadPath = getAbsoluteUploadPath();
            Path filePath = Paths.get(absoluteUploadPath, subDirectory, filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                logger.warn("文件不存在或不可读: {}", filePath.toString());
                return ResponseEntity.notFound().build();
            }
            
            // 获取或生成文件元数据
            String cacheKey = subDirectory + "/" + filename;
            FileMetadata metadata = getFileMetadata(cacheKey, filePath, filename);
            
            // 处理条件请求
            ResponseEntity<Resource> conditionalResponse = handleConditionalRequest(
                metadata, ifNoneMatch, ifModifiedSince);
            if (conditionalResponse != null) {
                return conditionalResponse;
            }
            
                // 设置响应头
                HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(metadata.getContentType()));
            headers.setETag("\"" + metadata.getEtag() + "\"");
            headers.setLastModified(metadata.getLastModified());
            headers.setContentLength(metadata.getFileSize());
            
            // 缓存控制头
            headers.setCacheControl("public, max-age=" + cacheTtlSeconds + ", must-revalidate");
            headers.setExpires(System.currentTimeMillis() + (cacheTtlSeconds * 1000L));
                
                // 允许跨域访问
                headers.setAccessControlAllowOrigin("*");
            headers.setAccessControlAllowMethods(java.util.Arrays.asList(HttpMethod.GET, HttpMethod.HEAD));
            headers.setAccessControlMaxAge(cacheTtlSeconds);
            
            logger.debug("返回文件: {} (ETag: {}, Size: {})", filePath.toString(), metadata.getEtag(), metadata.getFileSize());
                
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            
        } catch (MalformedURLException e) {
            logger.error("文件路径错误", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("文件访问失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 处理条件请求（ETag和Last-Modified）
     */
    private ResponseEntity<Resource> handleConditionalRequest(
            FileMetadata metadata, String ifNoneMatch, String ifModifiedSince) {
        
        // 检查ETag条件
        if (ifNoneMatch != null && !ifNoneMatch.isEmpty()) {
            String etag = ifNoneMatch.replace("\"", "");
            if (etag.equals(metadata.getEtag())) {
                logger.debug("ETag匹配，返回304 Not Modified");
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
        }
        
        // 检查Last-Modified条件
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            try {
                long ifModifiedSinceTime = LocalDateTime.parse(
                    ifModifiedSince, 
                    DateTimeFormatter.RFC_1123_DATE_TIME
                ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                
                if (metadata.getLastModified() <= ifModifiedSinceTime) {
                    logger.debug("Last-Modified匹配，返回304 Not Modified");
                    return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
                }
            } catch (Exception e) {
                logger.debug("解析If-Modified-Since失败: {}", ifModifiedSince);
            }
        }
        
        return null; // 继续正常处理
    }
    
    /**
     * 获取文件元数据（带缓存）
     */
    private FileMetadata getFileMetadata(String cacheKey, Path filePath, String filename) throws IOException {
        // 检查缓存
        if (cacheEnabled) {
            FileMetadata cached = fileMetadataCache.get(cacheKey);
            if (cached != null && !cached.isExpired(cacheTtlSeconds)) {
                return cached;
            }
        }
        
        // 获取文件属性
        BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
        
        // 生成ETag（基于文件大小和修改时间）
        String etag = generateETag(attrs.size(), attrs.lastModifiedTime().toMillis());
        
        // 创建元数据
        FileMetadata metadata = new FileMetadata(
            etag,
            attrs.lastModifiedTime().toMillis(),
            attrs.size(),
            getContentType(filename)
        );
        
        // 缓存元数据
        if (cacheEnabled) {
            // 清理过期缓存
            if (fileMetadataCache.size() >= maxCacheSize) {
                cleanupExpiredCache();
            }
            fileMetadataCache.put(cacheKey, metadata);
        }
        
        return metadata;
    }
    
    /**
     * 生成ETag
     */
    private String generateETag(long fileSize, long lastModified) {
        try {
            String input = fileSize + "_" + lastModified;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // 降级方案：使用简单的字符串
            return String.valueOf(fileSize) + "_" + String.valueOf(lastModified);
        }
    }
    
    /**
     * 清理过期缓存
     */
    private void cleanupExpiredCache() {
        fileMetadataCache.entrySet().removeIf(entry -> 
            entry.getValue().isExpired(cacheTtlSeconds));
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
            case "svg":
                return "image/svg+xml";
            case "ico":
                return "image/x-icon";
            case "pdf":
                return "application/pdf";
            case "txt":
                return "text/plain";
            case "json":
                return "application/json";
            case "xml":
                return "application/xml";
            case "css":
                return "text/css";
            case "js":
                return "application/javascript";
            case "html":
            case "htm":
                return "text/html";
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
    
    /**
     * 获取缓存统计信息
     * @return 缓存统计
     */
    @GetMapping("/cache/stats")
    public ResponseEntity<Object> getCacheStats() {
        return ResponseEntity.ok(new Object() {
            public final int cacheSize = fileMetadataCache.size();
            public final boolean cacheEnabled = FileController.this.cacheEnabled;
            public final int maxCacheSize = FileController.this.maxCacheSize;
            public final int cacheTtlSeconds = FileController.this.cacheTtlSeconds;
        });
    }
    
    /**
     * 清理文件缓存
     * @return 清理结果
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<Object> clearCache() {
        int size = fileMetadataCache.size();
        fileMetadataCache.clear();
        logger.info("清理文件缓存，共清理 {} 个条目", size);
        return ResponseEntity.ok(new Object() {
            public final String message = "缓存清理成功";
            public final int clearedCount = size;
        });
    }
} 