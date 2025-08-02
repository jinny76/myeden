package com.myeden.service.impl;

import com.myeden.service.ModelFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 模型文件管理服务实现类
 * 
 * 功能说明：
 * - 扫描uploads/models目录下的GLB文件
 * - 根据文件名推断模型类别和描述
 * - 提供模型文件信息给AI动画生成
 * - 支持基于主题的模型推荐
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
public class ModelFileServiceImpl implements ModelFileService {
    
    private static final Logger log = LoggerFactory.getLogger(ModelFileServiceImpl.class);
    
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;
    
    // 模型文件缓存
    private final List<ModelFileInfo> modelCache = new ArrayList<>();
    private final Map<String, List<ModelFileInfo>> categoryCache = new ConcurrentHashMap<>();
    
    // 模型类别关键词映射
    private final Map<String, String> categoryKeywords = new HashMap<>();
    private final Map<String, List<String>> themeModelMapping = new HashMap<>();
    
    @PostConstruct
    public void init() {
        initializeCategoryKeywords();
        initializeThemeMapping();
        refreshModelCache();
    }
    
    /**
     * 初始化类别关键词映射
     */
    private void initializeCategoryKeywords() {
        categoryKeywords.put("tree", "植物");
        categoryKeywords.put("plant", "植物");
        categoryKeywords.put("flower", "植物");
        categoryKeywords.put("grass", "植物");
        categoryKeywords.put("building", "建筑");
        categoryKeywords.put("house", "建筑");
        categoryKeywords.put("tower", "建筑");
        categoryKeywords.put("castle", "建筑");
        categoryKeywords.put("car", "交通工具");
        categoryKeywords.put("vehicle", "交通工具");
        categoryKeywords.put("boat", "交通工具");
        categoryKeywords.put("plane", "交通工具");
        categoryKeywords.put("animal", "动物");
        categoryKeywords.put("bird", "动物");
        categoryKeywords.put("fish", "动物");
        categoryKeywords.put("cat", "动物");
        categoryKeywords.put("dog", "动物");
        categoryKeywords.put("character", "角色");
        categoryKeywords.put("person", "角色");
        categoryKeywords.put("human", "角色");
        categoryKeywords.put("robot", "角色");
        categoryKeywords.put("furniture", "家具");
        categoryKeywords.put("chair", "家具");
        categoryKeywords.put("table", "家具");
        categoryKeywords.put("bed", "家具");
        categoryKeywords.put("rock", "自然物体");
        categoryKeywords.put("stone", "自然物体");
        categoryKeywords.put("mountain", "自然物体");
        categoryKeywords.put("water", "自然物体");
        categoryKeywords.put("food", "食物");
        categoryKeywords.put("fruit", "食物");
        categoryKeywords.put("cake", "食物");
        categoryKeywords.put("weapon", "武器");
        categoryKeywords.put("sword", "武器");
        categoryKeywords.put("gun", "武器");
        categoryKeywords.put("tool", "工具");
        categoryKeywords.put("hammer", "工具");
        categoryKeywords.put("wrench", "工具");
    }
    
    /**
     * 初始化主题与模型的映射关系
     */
    private void initializeThemeMapping() {
        themeModelMapping.put("自然", Arrays.asList("tree", "plant", "flower", "grass", "rock", "stone", "mountain", "water", "animal", "bird", "fish"));
        themeModelMapping.put("城市", Arrays.asList("building", "house", "tower", "car", "vehicle", "furniture", "chair", "table"));
        themeModelMapping.put("科幻", Arrays.asList("robot", "weapon", "tool", "vehicle", "building", "character"));
        themeModelMapping.put("童话", Arrays.asList("castle", "character", "animal", "plant", "flower", "tree"));
        themeModelMapping.put("现代", Arrays.asList("car", "building", "furniture", "character", "tool"));
        themeModelMapping.put("古典", Arrays.asList("castle", "tower", "weapon", "sword", "building", "character"));
        themeModelMapping.put("食物", Arrays.asList("food", "fruit", "cake"));
        themeModelMapping.put("动物", Arrays.asList("animal", "bird", "fish", "cat", "dog"));
    }
    
    @Override
    public List<ModelFileInfo> getAvailableGlbModels() {
        return new ArrayList<>(modelCache);
    }
    
    @Override
    public void refreshModelCache() {
        try {
            log.info("开始刷新模型文件缓存");
            
            modelCache.clear();
            categoryCache.clear();
            
            String modelsDir = getModelsDirectory();
            Path modelsPath = Paths.get(modelsDir);
            
            if (!Files.exists(modelsPath)) {
                log.warn("模型目录不存在: {}", modelsDir);
                return;
            }
            
            try (Stream<Path> files = Files.walk(modelsPath)) {
                List<ModelFileInfo> models = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".glb"))
                    .map(this::createModelFileInfo)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                
                modelCache.addAll(models);
                
                // 按类别分组缓存
                Map<String, List<ModelFileInfo>> grouped = models.stream()
                    .collect(Collectors.groupingBy(ModelFileInfo::getCategory));
                categoryCache.putAll(grouped);
                
                log.info("模型文件缓存刷新完成，共加载 {} 个GLB模型文件", models.size());
                
                // 打印统计信息
                grouped.forEach((category, modelList) -> 
                    log.debug("类别 [{}]: {} 个模型", category, modelList.size()));
                
            }
            
        } catch (IOException e) {
            log.error("刷新模型文件缓存时发生错误", e);
        }
    }
    
    @Override
    public List<ModelFileInfo> getRecommendedModels(String theme) {
        if (theme == null || theme.trim().isEmpty()) {
            return getAvailableGlbModels();
        }
        
        List<ModelFileInfo> recommended = new ArrayList<>();
        
        // 根据主题关键词查找推荐模型
        themeModelMapping.entrySet().stream()
            .filter(entry -> theme.contains(entry.getKey()))
            .forEach(entry -> {
                entry.getValue().forEach(keyword -> {
                    modelCache.stream()
                        .filter(model -> model.getFilename().toLowerCase().contains(keyword.toLowerCase()) ||
                                       model.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                        .forEach(model -> {
                            if (!recommended.contains(model)) {
                                recommended.add(model);
                            }
                        });
                });
            });
        
        // 如果没有找到推荐模型，返回所有模型
        if (recommended.isEmpty()) {
            return getAvailableGlbModels();
        }
        
        // 限制推荐数量，避免提示词过长
        return recommended.stream().limit(10).collect(Collectors.toList());
    }
    
    /**
     * 创建模型文件信息
     */
    private ModelFileInfo createModelFileInfo(Path filePath) {
        try {
            String filename = filePath.getFileName().toString();
            String relativePath = getRelativePath(filePath);
            String displayName = getDisplayName(filename);
            String description = getDescription(filename);
            String category = getCategory(filename);
            long fileSize = Files.size(filePath);
            
            return new ModelFileInfo(filename, relativePath, displayName, description, category, fileSize);
            
        } catch (IOException e) {
            log.warn("无法获取模型文件信息: {}", filePath, e);
            return null;
        }
    }
    
    /**
     * 获取相对路径
     */
    private String getRelativePath(Path filePath) {
        String uploadDir = getAbsoluteUploadPath();
        Path uploadPath = Paths.get(uploadDir);
        Path relativePath = uploadPath.relativize(filePath);
        return relativePath.toString().replace("\\", "/");
    }
    
    /**
     * 获取显示名称
     */
    private String getDisplayName(String filename) {
        // 移除扩展名并格式化
        String name = filename.toLowerCase().replace(".glb", "");
        name = name.replace("_", " ").replace("-", " ");
        
        // 首字母大写
        return Arrays.stream(name.split(" "))
            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
            .collect(Collectors.joining(" "));
    }
    
    /**
     * 获取模型描述
     */
    private String getDescription(String filename) {
        String lowerName = filename.toLowerCase();
        
        // 根据文件名关键词生成描述
        for (Map.Entry<String, String> entry : categoryKeywords.entrySet()) {
            if (lowerName.contains(entry.getKey())) {
                return "三维" + entry.getValue() + "模型";
            }
        }
        
        return "三维模型文件";
    }
    
    /**
     * 获取模型类别
     */
    private String getCategory(String filename) {
        String lowerName = filename.toLowerCase();
        
        // 根据文件名关键词确定类别
        for (Map.Entry<String, String> entry : categoryKeywords.entrySet()) {
            if (lowerName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return "其他";
    }
    
    /**
     * 获取模型目录路径
     */
    private String getModelsDirectory() {
        return getAbsoluteUploadPath() + "/models";
    }
    
    /**
     * 获取绝对上传路径
     */
    private String getAbsoluteUploadPath() {
        if (uploadPath.startsWith("./")) {
            String currentDir = System.getProperty("user.dir");
            return currentDir + "/" + uploadPath.substring(2);
        }
        return uploadPath;
    }
}