package com.myeden.service;

import java.util.List;

/**
 * 模型文件管理服务接口
 * 
 * 功能说明：
 * - 扫描和管理3D模型文件（GLB、GLTF等）
 * - 提供模型文件列表给AI动画生成
 * - 模型文件信息缓存管理
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface ModelFileService {
    
    /**
     * 获取所有可用的GLB模型文件
     * @return GLB模型文件列表
     */
    List<ModelFileInfo> getAvailableGlbModels();
    
    /**
     * 刷新模型文件缓存
     */
    void refreshModelCache();
    
    /**
     * 根据主题获取推荐的模型文件
     * @param theme 动画主题
     * @return 推荐的模型文件列表
     */
    List<ModelFileInfo> getRecommendedModels(String theme);
    
    /**
     * 模型文件信息类
     */
    class ModelFileInfo {
        private String filename;
        private String relativePath;
        private String displayName;
        private String description;
        private String category;
        private long fileSize;
        private String downloadUrl;
        
        public ModelFileInfo(String filename, String relativePath, String displayName, 
                           String description, String category, long fileSize) {
            this.filename = filename;
            this.relativePath = relativePath;
            this.displayName = displayName;
            this.description = description;
            this.category = category;
            this.fileSize = fileSize;
            this.downloadUrl = "/api/v1/files/" + relativePath;
        }
        
        // Getters
        public String getFilename() { return filename; }
        public String getRelativePath() { return relativePath; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public long getFileSize() { return fileSize; }
        public String getDownloadUrl() { return downloadUrl; }
        
        @Override
        public String toString() {
            return String.format("%s (%s) - %s", displayName, category, description);
        }
    }
}