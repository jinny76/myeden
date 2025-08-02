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
    
    // 天空盒子文件缓存
    private final List<SkyboxFileInfo> skyboxCache = new ArrayList<>();
    private final Map<String, List<SkyboxFileInfo>> skyboxCategoryCache = new ConcurrentHashMap<>();
    
    // 模型类别关键词映射
    private final Map<String, String> categoryKeywords = new HashMap<>();
    private final Map<String, List<String>> themeModelMapping = new HashMap<>();
    
    // 天空盒子类别关键词映射
    private final Map<String, String> skyboxKeywords = new HashMap<>();
    private final Map<String, List<String>> themeSkyboxMapping = new HashMap<>();
    
    @PostConstruct
    public void init() {
        initializeCategoryKeywords();
        initializeThemeMapping();
        initializeSkyboxKeywords();
        initializeSkyboxThemeMapping();
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
            log.info("开始刷新模型文件和天空盒子缓存");
            
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
            
            // 同时刷新天空盒子缓存
            refreshSkyboxCache();
            
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
    
    // ==================== 天空盒子相关方法 ====================
    
    /**
     * 初始化天空盒子关键词映射
     */
    private void initializeSkyboxKeywords() {
        skyboxKeywords.put("sky", "天空");
        skyboxKeywords.put("cloud", "天空");
        skyboxKeywords.put("sunset", "日落");
        skyboxKeywords.put("sunrise", "日出");
        skyboxKeywords.put("night", "夜晚");
        skyboxKeywords.put("star", "星空");
        skyboxKeywords.put("space", "太空");
        skyboxKeywords.put("nebula", "星云");
        skyboxKeywords.put("forest", "森林");
        skyboxKeywords.put("mountain", "山景");
        skyboxKeywords.put("ocean", "海洋");
        skyboxKeywords.put("desert", "沙漠");
        skyboxKeywords.put("city", "城市");
        skyboxKeywords.put("urban", "城市");
        skyboxKeywords.put("winter", "冬季");
        skyboxKeywords.put("snow", "雪景");
        skyboxKeywords.put("autumn", "秋季");
        skyboxKeywords.put("spring", "春季");
        skyboxKeywords.put("summer", "夏季");
        skyboxKeywords.put("fantasy", "奇幻");
        skyboxKeywords.put("cyberpunk", "赛博朋克");
        skyboxKeywords.put("industrial", "工业");
    }
    
    /**
     * 初始化天空盒子主题映射
     */
    private void initializeSkyboxThemeMapping() {
        themeSkyboxMapping.put("自然", Arrays.asList("sky", "cloud", "forest", "mountain", "ocean", "sunset", "sunrise"));
        themeSkyboxMapping.put("城市", Arrays.asList("city", "urban", "industrial", "cyberpunk"));
        themeSkyboxMapping.put("科幻", Arrays.asList("space", "nebula", "star", "cyberpunk", "night"));
        themeSkyboxMapping.put("童话", Arrays.asList("fantasy", "cloud", "sunset", "spring", "forest"));
        themeSkyboxMapping.put("现代", Arrays.asList("city", "urban", "sky", "cloud"));
        themeSkyboxMapping.put("古典", Arrays.asList("mountain", "forest", "sunset", "autumn"));
        themeSkyboxMapping.put("神秘", Arrays.asList("night", "star", "space", "nebula", "fantasy"));
        themeSkyboxMapping.put("温馨", Arrays.asList("sunset", "sunrise", "spring", "summer", "cloud"));
        themeSkyboxMapping.put("冬季", Arrays.asList("winter", "snow", "night", "star"));
    }
    
    @Override
    public List<SkyboxFileInfo> getAvailableSkyboxes() {
        return new ArrayList<>(skyboxCache);
    }
    
    @Override
    public SkyboxFileInfo getRandomSkybox() {
        if (skyboxCache.isEmpty()) {
            refreshSkyboxCache();
        }
        
        if (skyboxCache.isEmpty()) {
            return null;
        }
        
        Random random = new Random();
        int index = random.nextInt(skyboxCache.size());
        return skyboxCache.get(index);
    }
    
    @Override
    public SkyboxFileInfo getRecommendedSkybox(String theme) {
        if (theme == null || theme.trim().isEmpty()) {
            return getRandomSkybox();
        }
        
        List<SkyboxFileInfo> recommended = new ArrayList<>();
        
        // 根据主题关键词查找推荐天空盒子
        themeSkyboxMapping.entrySet().stream()
            .filter(entry -> theme.contains(entry.getKey()))
            .forEach(entry -> {
                entry.getValue().forEach(keyword -> {
                    skyboxCache.stream()
                        .filter(skybox -> skybox.getFilename().toLowerCase().contains(keyword.toLowerCase()) ||
                                        skybox.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                        .forEach(skybox -> {
                            if (!recommended.contains(skybox)) {
                                recommended.add(skybox);
                            }
                        });
                });
            });
        
        // 如果没有找到推荐天空盒子，返回随机一个
        if (recommended.isEmpty()) {
            return getRandomSkybox();
        }
        
        // 随机选择一个推荐的天空盒子
        Random random = new Random();
        return recommended.get(random.nextInt(recommended.size()));
    }
    
    /**
     * 刷新天空盒子文件缓存
     */
    public void refreshSkyboxCache() {
        try {
            log.info("开始刷新天空盒子文件缓存");
            
            skyboxCache.clear();
            skyboxCategoryCache.clear();
            
            String skyboxDir = getSkyboxDirectory();
            Path skyboxPath = Paths.get(skyboxDir);
            
            if (!Files.exists(skyboxPath)) {
                log.warn("天空盒子目录不存在: {}", skyboxDir);
                return;
            }
            
            try (Stream<Path> files = Files.walk(skyboxPath, 2)) {
                List<SkyboxFileInfo> skyboxes = new ArrayList<>();
                
                // 扫描立方体贴图目录（包含6张独立图片的文件夹）
                files.filter(Files::isDirectory)
                    .filter(dir -> !dir.equals(skyboxPath)) // 排除根目录
                    .forEach(dir -> {
                        SkyboxFileInfo cubeMapInfo = createCubeMapSkyboxInfo(dir);
                        if (cubeMapInfo != null) {
                            skyboxes.add(cubeMapInfo);
                        }
                    });
                
                // 继续支持单个天空盒子文件
                try (Stream<Path> singleFiles = Files.list(skyboxPath)) {
                    List<SkyboxFileInfo> singleSkyboxes = singleFiles
                        .filter(Files::isRegularFile)
                        .filter(path -> {
                            String filename = path.toString().toLowerCase();
                            return filename.endsWith(".jpg") || 
                                   filename.endsWith(".jpeg") || 
                                   filename.endsWith(".png") || 
                                   filename.endsWith(".hdr") || 
                                   filename.endsWith(".exr");
                        })
                        .map(this::createSkyboxFileInfo)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                    
                    skyboxes.addAll(singleSkyboxes);
                }
                
                skyboxCache.addAll(skyboxes);
                
                // 按类别分组缓存
                Map<String, List<SkyboxFileInfo>> grouped = skyboxes.stream()
                    .collect(Collectors.groupingBy(SkyboxFileInfo::getCategory));
                skyboxCategoryCache.putAll(grouped);
                
                log.info("天空盒子文件缓存刷新完成，共加载 {} 个天空盒子文件", skyboxes.size());
                
                // 打印统计信息
                grouped.forEach((category, skyboxList) -> 
                    log.debug("类别 [{}]: {} 个天空盒子", category, skyboxList.size()));
                
            }
            
        } catch (IOException e) {
            log.error("刷新天空盒子文件缓存时发生错误", e);
        }
    }
    
    /**
     * 创建立方体贴图天空盒子文件信息
     */
    private SkyboxFileInfo createCubeMapSkyboxInfo(Path dirPath) {
        try {
            String dirName = dirPath.getFileName().toString();
            
            // 检查是否包含立方体贴图的6个文件
            String[] requiredFiles = {"px.png", "nx.png", "py.png", "ny.png", "pz.png", "nz.png"};
            boolean hasAllFiles = true;
            
            for (String requiredFile : requiredFiles) {
                Path filePath = dirPath.resolve(requiredFile);
                if (!Files.exists(filePath)) {
                    hasAllFiles = false;
                    break;
                }
            }
            
            if (!hasAllFiles) {
                log.debug("目录 {} 不包含完整的立方体贴图文件", dirName);
                return null;
            }
            
            // 计算总文件大小
            long totalSize = 0;
            for (String requiredFile : requiredFiles) {
                Path filePath = dirPath.resolve(requiredFile);
                totalSize += Files.size(filePath);
            }
            
            String relativePath = "/skybox/" + dirName;
            String displayName = getSkyboxDisplayName(dirName);
            String description = getSkyboxDescription(dirName);
            String category = getSkyboxCategory(dirName);
            
            log.debug("发现立方体贴图天空盒子: {} ({})", displayName, dirName);
            
            return new SkyboxFileInfo(dirName, relativePath, displayName, description, category, totalSize);
            
        } catch (IOException e) {
            log.warn("无法获取立方体贴图天空盒子信息: {}", dirPath, e);
            return null;
        }
    }

    /**
     * 创建天空盒子文件信息
     */
    private SkyboxFileInfo createSkyboxFileInfo(Path filePath) {
        try {
            String filename = filePath.getFileName().toString();
            String relativePath = getSkyboxRelativePath(filePath);
            String displayName = getSkyboxDisplayName(filename);
            String description = getSkyboxDescription(filename);
            String category = getSkyboxCategory(filename);
            long fileSize = Files.size(filePath);
            
            return new SkyboxFileInfo(filename, relativePath, displayName, description, category, fileSize);
            
        } catch (IOException e) {
            log.warn("无法获取天空盒子文件信息: {}", filePath, e);
            return null;
        }
    }
    
    /**
     * 获取天空盒子相对路径
     */
    private String getSkyboxRelativePath(Path filePath) {
        String uploadDir = getAbsoluteUploadPath();
        Path uploadPath = Paths.get(uploadDir);
        Path relativePath = uploadPath.relativize(filePath);
        return relativePath.toString().replace("\\", "/");
    }
    
    /**
     * 获取天空盒子显示名称
     */
    private String getSkyboxDisplayName(String filename) {
        // 移除扩展名并格式化
        String name = filename.toLowerCase();
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            name = name.substring(0, lastDot);
        }
        name = name.replace("_", " ").replace("-", " ");
        
        // 首字母大写
        return Arrays.stream(name.split(" "))
            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
            .collect(Collectors.joining(" "));
    }
    
    /**
     * 获取天空盒子描述
     */
    private String getSkyboxDescription(String filename) {
        String lowerName = filename.toLowerCase();
        
        // 根据文件名关键词生成描述
        for (Map.Entry<String, String> entry : skyboxKeywords.entrySet()) {
            if (lowerName.contains(entry.getKey())) {
                return entry.getValue() + "天空盒子";
            }
        }
        
        return "天空盒子纹理";
    }
    
    /**
     * 获取天空盒子类别
     */
    private String getSkyboxCategory(String filename) {
        String lowerName = filename.toLowerCase();
        
        // 根据文件名关键词确定类别
        for (Map.Entry<String, String> entry : skyboxKeywords.entrySet()) {
            if (lowerName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return "其他";
    }
    
    /**
     * 获取天空盒子目录路径
     */
    private String getSkyboxDirectory() {
        return getAbsoluteUploadPath() + "/skybox";
    }
}