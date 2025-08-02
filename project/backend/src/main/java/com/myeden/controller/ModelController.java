package com.myeden.controller;

import com.myeden.service.ModelFileService;
import com.myeden.service.ModelFileService.ModelFileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 模型文件管理控制器
 * 
 * 功能说明：
 * - 提供3D模型文件查询和管理API
 * - 支持模型文件缓存刷新
 * - 提供基于主题的模型推荐
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/models")
public class ModelController {
    
    private static final Logger logger = LoggerFactory.getLogger(ModelController.class);
    
    @Autowired
    private ModelFileService modelFileService;
    
    /**
     * 获取所有可用的GLB模型文件
     * GET /api/v1/models
     * 
     * @return GLB模型文件列表
     */
    @GetMapping
    public ResponseEntity<EventResponse> getAllModels() {
        try {
            logger.info("获取所有GLB模型文件");
            
            List<ModelFileInfo> models = modelFileService.getAvailableGlbModels();
            
            logger.info("成功获取 {} 个GLB模型文件", models.size());
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "获取模型文件列表成功",
                models
            ));
            
        } catch (Exception e) {
            logger.error("获取模型文件列表失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "获取模型文件列表失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 根据主题获取推荐的模型文件
     * GET /api/v1/models/recommend?theme={theme}
     * 
     * @param theme 动画主题
     * @return 推荐的模型文件列表
     */
    @GetMapping("/recommend")
    public ResponseEntity<EventResponse> getRecommendedModels(@RequestParam String theme) {
        try {
            logger.info("获取主题 [{}] 的推荐模型文件", theme);
            
            List<ModelFileInfo> models = modelFileService.getRecommendedModels(theme);
            
            logger.info("主题 [{}] 推荐 {} 个模型文件", theme, models.size());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("theme", theme);
            responseData.put("models", models);
            responseData.put("count", models.size());
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "获取推荐模型文件成功",
                responseData
            ));
            
        } catch (Exception e) {
            logger.error("获取推荐模型文件失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "获取推荐模型文件失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 刷新模型文件缓存
     * POST /api/v1/models/refresh
     * 
     * @return 刷新结果
     */
    @PostMapping("/refresh")
    public ResponseEntity<EventResponse> refreshModelCache() {
        try {
            logger.info("开始刷新模型文件缓存");
            
            modelFileService.refreshModelCache();
            List<ModelFileInfo> models = modelFileService.getAvailableGlbModels();
            
            logger.info("模型文件缓存刷新完成，共 {} 个文件", models.size());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "缓存刷新成功");
            responseData.put("totalModels", models.size());
            responseData.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "模型文件缓存刷新成功",
                responseData
            ));
            
        } catch (Exception e) {
            logger.error("刷新模型文件缓存失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "刷新模型文件缓存失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 获取模型文件统计信息
     * GET /api/v1/models/stats
     * 
     * @return 统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<EventResponse> getModelStats() {
        try {
            logger.info("获取模型文件统计信息");
            
            List<ModelFileInfo> models = modelFileService.getAvailableGlbModels();
            
            // 按类别统计
            java.util.Map<String, Long> categoryStats = models.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    ModelFileInfo::getCategory,
                    java.util.stream.Collectors.counting()
                ));
            
            // 计算总大小
            long totalSize = models.stream()
                .mapToLong(ModelFileInfo::getFileSize)
                .sum();
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("totalModels", models.size());
            responseData.put("categoryStats", categoryStats);
            responseData.put("totalSizeBytes", totalSize);
            responseData.put("totalSizeFormatted", formatFileSize(totalSize));
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "获取模型统计信息成功",
                responseData
            ));
            
        } catch (Exception e) {
            logger.error("获取模型统计信息失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "获取模型统计信息失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}