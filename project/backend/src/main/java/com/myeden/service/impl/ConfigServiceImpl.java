package com.myeden.service.impl;

import com.myeden.config.WorldConfig;
import com.myeden.config.RobotConfig;
import com.myeden.entity.Robot;
import com.myeden.repository.RobotRepository;
import com.myeden.repository.WorldConfigRepository;
import com.myeden.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 配置管理服务实现类
 * 
 * 功能说明：
 * - 实现配置文件加载和解析功能
 * - 支持配置热更新和动态重载
 * - 管理世界配置和机器人配置
 * - 将世界配置和机器人配置同步到数据库
 * - 提供配置验证和错误处理
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
public class ConfigServiceImpl implements ConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);
    
    @Autowired
    private WorldConfig worldConfig;
    
    @Autowired
    private RobotConfig robotConfig;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private WorldConfigRepository worldConfigRepository;
    
    private ConfigStatus configStatus;
    
    public ConfigServiceImpl() {
        this.configStatus = new ConfigStatus(false, false, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    
    @Override
    public WorldConfig loadWorldConfig() {
        try {
            logger.info("开始加载世界配置...");
            
            // 验证配置
            ConfigValidationResult validationResult = validateWorldConfig(worldConfig);
            if (!validationResult.isValid()) {
                logger.error("世界配置验证失败: {}", validationResult.getMessage());
                configStatus.setErrorMessage(validationResult.getMessage());
                return null;
            }
            
            // 同步世界配置到数据库
            syncWorldConfigToDatabase();
            
            configStatus.setWorldConfigLoaded(true);
            configStatus.setLastLoadTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            configStatus.setErrorMessage(null);
            
            logger.info("世界配置加载成功: {}", worldConfig.getName());
            return worldConfig;
            
        } catch (Exception e) {
            logger.error("加载世界配置失败", e);
            configStatus.setWorldConfigLoaded(false);
            configStatus.setErrorMessage("加载世界配置失败: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public RobotConfig loadRobotConfig() {
        try {
            logger.info("开始加载机器人配置...");
            
            // 验证配置
            ConfigValidationResult validationResult = validateRobotConfig(robotConfig);
            if (!validationResult.isValid()) {
                logger.error("机器人配置验证失败: {}", validationResult.getMessage());
                configStatus.setErrorMessage(validationResult.getMessage());
                return null;
            }
            
            // 同步机器人配置到数据库
            syncRobotsToDatabase();
            
            configStatus.setRobotConfigLoaded(true);
            configStatus.setLastLoadTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            configStatus.setErrorMessage(null);
            
            logger.info("机器人配置加载成功，机器人数量: {}", 
                robotConfig.getList() != null ? robotConfig.getList().size() : 0);
            return robotConfig;
            
        } catch (Exception e) {
            logger.error("加载机器人配置失败", e);
            configStatus.setRobotConfigLoaded(false);
            configStatus.setErrorMessage("加载机器人配置失败: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean reloadWorldConfig() {
        try {
            logger.info("重新加载世界配置...");
            
            // 重新验证配置
            ConfigValidationResult validationResult = validateWorldConfig(worldConfig);
            if (!validationResult.isValid()) {
                logger.error("世界配置验证失败: {}", validationResult.getMessage());
                configStatus.setErrorMessage(validationResult.getMessage());
                return false;
            }
            
            // 同步世界配置到数据库
            syncWorldConfigToDatabase();
            
            configStatus.setWorldConfigLoaded(true);
            configStatus.setLastLoadTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            configStatus.setErrorMessage(null);
            
            logger.info("世界配置重新加载成功");
            return true;
            
        } catch (Exception e) {
            logger.error("重新加载世界配置失败", e);
            configStatus.setWorldConfigLoaded(false);
            configStatus.setErrorMessage("重新加载世界配置失败: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean reloadRobotConfig() {
        try {
            logger.info("重新加载机器人配置...");
            
            // 重新验证配置
            ConfigValidationResult validationResult = validateRobotConfig(robotConfig);
            if (!validationResult.isValid()) {
                logger.error("机器人配置验证失败: {}", validationResult.getMessage());
                configStatus.setErrorMessage(validationResult.getMessage());
                return false;
            }
            
            // 同步机器人配置到数据库
            syncRobotsToDatabase();
            
            configStatus.setRobotConfigLoaded(true);
            configStatus.setLastLoadTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            configStatus.setErrorMessage(null);
            
            logger.info("机器人配置重新加载成功");
            return true;
            
        } catch (Exception e) {
            logger.error("重新加载机器人配置失败", e);
            configStatus.setRobotConfigLoaded(false);
            configStatus.setErrorMessage("重新加载机器人配置失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 将世界配置同步到数据库
     */
    private void syncWorldConfigToDatabase() {
        if (worldConfig == null) {
            logger.warn("世界配置为空，跳过数据库同步");
            return;
        }
        
        try {
            logger.info("🔄 开始同步世界配置到数据库: {}", worldConfig.getName());
            
            // 生成世界ID
            String worldId = "world_" + worldConfig.getName().toLowerCase().replaceAll("\\s+", "_");
            
            // 检查世界配置是否已存在
            Optional<com.myeden.entity.WorldConfig> existingWorld = worldConfigRepository.findByWorldId(worldId);
            
            com.myeden.entity.WorldConfig worldEntity;
            if (existingWorld.isPresent()) {
                // 更新现有世界配置
                worldEntity = existingWorld.get();
                updateWorldFromConfig(worldEntity, worldConfig);
                logger.debug("🔄 更新世界配置: {}", worldConfig.getName());
            } else {
                // 创建新世界配置
                worldEntity = convertToWorldConfigEntity(worldConfig, worldId);
                logger.debug("➕ 创建世界配置: {}", worldConfig.getName());
            }
            
            // 保存到数据库
            worldConfigRepository.save(worldEntity);
            
            logger.info("✅ 世界配置同步完成: {}", worldConfig.getName());
            
        } catch (Exception e) {
            logger.error("❌ 同步世界配置失败: {}", worldConfig.getName(), e);
            throw e;
        }
    }
    
    /**
     * 将WorldConfig转换为WorldConfigEntity
     */
    private com.myeden.entity.WorldConfig convertToWorldConfigEntity(WorldConfig worldConfig, String worldId) {
        com.myeden.entity.WorldConfig worldEntity = new com.myeden.entity.WorldConfig();
        
        // 基本信息
        worldEntity.setWorldId(worldId);
        worldEntity.setName(worldConfig.getName());
        worldEntity.setDescription(worldConfig.getDescription());
        
        // 构建背景prompt
        StringBuilder backgroundPrompt = new StringBuilder();
        if (worldConfig.getBackground() != null) {
            backgroundPrompt.append("世界背景故事：").append(worldConfig.getBackground().getStory()).append("\n\n");
            
            if (worldConfig.getBackground().getRules() != null && !worldConfig.getBackground().getRules().isEmpty()) {
                backgroundPrompt.append("世界规则：\n");
                for (String rule : worldConfig.getBackground().getRules()) {
                    backgroundPrompt.append("- ").append(rule).append("\n");
                }
                backgroundPrompt.append("\n");
            }
            
            if (worldConfig.getBackground().getFeatures() != null && !worldConfig.getBackground().getFeatures().isEmpty()) {
                backgroundPrompt.append("世界特色：\n");
                for (String feature : worldConfig.getBackground().getFeatures()) {
                    backgroundPrompt.append("- ").append(feature).append("\n");
                }
                backgroundPrompt.append("\n");
            }
        }
        worldEntity.setBackgroundPrompt(backgroundPrompt.toString());
        
        // 构建世界观prompt
        StringBuilder worldviewPrompt = new StringBuilder();
        worldviewPrompt.append("这是一个名为「").append(worldConfig.getName()).append("」的虚拟世界。\n");
        worldviewPrompt.append("世界描述：").append(worldConfig.getDescription()).append("\n\n");
        
        if (worldConfig.getEnvironment() != null) {
            worldviewPrompt.append("环境设定：\n");
            worldviewPrompt.append("- 主题：").append(worldConfig.getEnvironment().getTheme()).append("\n");
            worldviewPrompt.append("- 色彩：").append(worldConfig.getEnvironment().getColorScheme()).append("\n");
            worldviewPrompt.append("- 氛围：").append(worldConfig.getEnvironment().getAtmosphere()).append("\n");
            worldviewPrompt.append("- 天气：").append(worldConfig.getEnvironment().getWeather()).append("\n\n");
        }
        
        if (worldConfig.getSettings() != null) {
            worldviewPrompt.append("世界设定：\n");
            worldviewPrompt.append("- 最大动态长度：").append(worldConfig.getSettings().getMaxPostLength()).append("字符\n");
            worldviewPrompt.append("- 最大评论长度：").append(worldConfig.getSettings().getMaxCommentLength()).append("字符\n");
            worldviewPrompt.append("- 最大图片大小：").append(worldConfig.getSettings().getMaxImageSize()).append("\n");
            worldviewPrompt.append("- 自动清理天数：").append(worldConfig.getSettings().getAutoCleanupDays()).append("天\n");
            worldviewPrompt.append("- 每用户最大机器人数：").append(worldConfig.getSettings().getMaxRobotsPerUser()).append("个\n");
        }
        
        worldEntity.setWorldviewPrompt(worldviewPrompt.toString());
        
        // 设置默认值
        worldEntity.setIsActive(true);
        worldEntity.setCreatedAt(LocalDateTime.now());
        worldEntity.setUpdatedAt(LocalDateTime.now());
        
        return worldEntity;
    }
    
    /**
     * 从配置更新世界配置信息
     */
    private void updateWorldFromConfig(com.myeden.entity.WorldConfig existing, WorldConfig newConfig) {
        // 更新基本信息
        existing.setName(newConfig.getName());
        existing.setDescription(newConfig.getDescription());
        
        // 重新构建背景prompt和世界观prompt
        String worldId = existing.getWorldId();
        com.myeden.entity.WorldConfig newEntity = convertToWorldConfigEntity(newConfig, worldId);
        existing.setBackgroundPrompt(newEntity.getBackgroundPrompt());
        existing.setWorldviewPrompt(newEntity.getWorldviewPrompt());
        
        // 更新时间戳
        existing.setUpdatedAt(LocalDateTime.now());
    }
    
    /**
     * 将机器人配置同步到数据库
     */
    private void syncRobotsToDatabase() {
        if (robotConfig == null || robotConfig.getList() == null) {
            logger.warn("机器人配置为空，跳过数据库同步");
            return;
        }
        
        List<RobotConfig.RobotInfo> robotConfigs = robotConfig.getList();
        int created = 0;
        int updated = 0;
        int skipped = 0;
        
        logger.info("🔄 开始同步 {} 个机器人配置到数据库", robotConfigs.size());
        
        for (RobotConfig.RobotInfo robotConfig : robotConfigs) {
            try {
                // 转换为Robot实体
                Robot robot = convertToRobot(robotConfig);
                
                // 检查机器人是否已存在
                Optional<Robot> existingRobot = robotRepository.findByRobotId(robot.getRobotId());
                
                if (existingRobot.isPresent()) {
                    // 更新现有机器人
                    Robot existing = existingRobot.get();
                    updateRobotFromConfig(existing, robot);
                    robotRepository.save(existing);
                    updated++;
                    logger.debug("🔄 更新机器人: {}", robot.getName());
                } else {
                    // 创建新机器人
                    robotRepository.save(robot);
                    created++;
                    logger.debug("➕ 创建机器人: {}", robot.getName());
                }
                
            } catch (Exception e) {
                logger.error("❌ 同步机器人配置失败: {}", robotConfig.getName(), e);
                skipped++;
            }
        }
        
        logger.info("📊 机器人同步完成 - 创建: {}, 更新: {}, 跳过: {}", created, updated, skipped);
    }
    
    /**
     * 将RobotConfig.RobotInfo转换为Robot实体
     */
    private Robot convertToRobot(RobotConfig.RobotInfo robotConfig) {
        Robot robot = new Robot();
        
        // 基本信息
        robot.setRobotId(robotConfig.getId());
        robot.setName(robotConfig.getName());
        robot.setAvatar(robotConfig.getAvatar());
        robot.setPersonality(robotConfig.getPersonality());
        robot.setIntroduction(robotConfig.getDescription());
        
        // 昵称作为简介的一部分
        if (StringUtils.hasText(robotConfig.getNickname())) {
            robot.setIntroduction(robotConfig.getNickname() + " - " + robot.getIntroduction());
        }
        
        // 背景信息
        if (StringUtils.hasText(robotConfig.getBackground())) {
            robot.setIntroduction(robot.getIntroduction() + "\n\n" + robotConfig.getBackground());
        }
        
        // 性格特征
        if (robotConfig.getTraits() != null && !robotConfig.getTraits().isEmpty()) {
            String traitsStr = "性格特征: " + String.join(", ", robotConfig.getTraits());
            robot.setIntroduction(robot.getIntroduction() + "\n" + traitsStr);
        }
        
        // 兴趣爱好
        if (robotConfig.getInterests() != null && !robotConfig.getInterests().isEmpty()) {
            String interestsStr = "兴趣爱好: " + String.join(", ", robotConfig.getInterests());
            robot.setIntroduction(robot.getIntroduction() + "\n" + interestsStr);
        }
        
        // 行为模式
        if (robotConfig.getBehaviorPatterns() != null) {
            RobotConfig.BehaviorPatterns patterns = robotConfig.getBehaviorPatterns();
            
            // 转换行为模式为数值（0-10）
            if (patterns.getReplyFrequency() > 0) {
                robot.setReplyFrequency((int) (patterns.getReplyFrequency() * 10));
            }
            if (patterns.getShareFrequency() > 0) {
                robot.setShareFrequency((int) (patterns.getShareFrequency() * 10));
            }
        }
        
        // 活跃时间段
        if (robotConfig.getActiveHours() != null) {
            for (RobotConfig.ActiveHours timeRange : robotConfig.getActiveHours()) {
                if (StringUtils.hasText(timeRange.getStart()) && StringUtils.hasText(timeRange.getEnd())) {
                    robot.addActiveTimeRange(timeRange.getStart(), timeRange.getEnd());
                }
            }
        }
        
        // 设置默认值
        robot.setGender("未知");
        robot.setProfession("AI助手");
        robot.setMbti("未知");
        robot.setReplySpeed(5);
        robot.setIsActive(robotConfig.isActive());
        robot.setCreatedAt(LocalDateTime.now());
        robot.setUpdatedAt(LocalDateTime.now());
        
        return robot;
    }
    
    /**
     * 从配置更新机器人信息
     */
    private void updateRobotFromConfig(Robot existing, Robot newConfig) {
        // 更新基本信息
        existing.setName(newConfig.getName());
        existing.setAvatar(newConfig.getAvatar());
        existing.setPersonality(newConfig.getPersonality());
        existing.setIntroduction(newConfig.getIntroduction());
        
        // 更新行为模式
        existing.setReplyFrequency(newConfig.getReplyFrequency());
        existing.setShareFrequency(newConfig.getShareFrequency());
        
        // 更新活跃时间段
        existing.setActiveTimeRanges(newConfig.getActiveTimeRanges());
        
        // 更新时间戳
        existing.setUpdatedAt(LocalDateTime.now());
    }
    
    @Override
    public ConfigValidationResult validateWorldConfig(WorldConfig config) {
        if (config == null) {
            return new ConfigValidationResult(false, "世界配置对象为空");
        }
        
        // 验证基本信息
        if (!StringUtils.hasText(config.getName())) {
            return new ConfigValidationResult(false, "世界名称不能为空");
        }
        
        if (!StringUtils.hasText(config.getVersion())) {
            return new ConfigValidationResult(false, "世界版本不能为空");
        }
        
        if (!StringUtils.hasText(config.getDescription())) {
            return new ConfigValidationResult(false, "世界描述不能为空");
        }
        
        // 验证背景信息
        if (config.getBackground() == null) {
            return new ConfigValidationResult(false, "世界背景配置不能为空");
        }
        
        if (!StringUtils.hasText(config.getBackground().getStory())) {
            return new ConfigValidationResult(false, "世界背景故事不能为空");
        }
        
        // 验证环境信息
        if (config.getEnvironment() == null) {
            return new ConfigValidationResult(false, "世界环境配置不能为空");
        }
        
        // 验证设置信息
        if (config.getSettings() == null) {
            return new ConfigValidationResult(false, "世界设置配置不能为空");
        }
        
        if (config.getSettings().getMaxPostLength() <= 0) {
            return new ConfigValidationResult(false, "最大动态长度必须大于0");
        }
        
        if (config.getSettings().getMaxCommentLength() <= 0) {
            return new ConfigValidationResult(false, "最大评论长度必须大于0");
        }
        
        return new ConfigValidationResult(true, "世界配置验证通过");
    }
    
    @Override
    public ConfigValidationResult validateRobotConfig(RobotConfig config) {
        if (config == null) {
            return new ConfigValidationResult(false, "机器人配置对象为空");
        }
        
        // 验证基础配置
        if (config.getBaseConfig() == null) {
            return new ConfigValidationResult(false, "机器人基础配置不能为空");
        }
        
        if (config.getBaseConfig().getTotalCount() <= 0) {
            return new ConfigValidationResult(false, "机器人总数必须大于0");
        }
        
        if (config.getBaseConfig().getMaxActivePerUser() <= 0) {
            return new ConfigValidationResult(false, "每个用户最大活跃机器人数必须大于0");
        }
        
        // 验证机器人列表
        List<RobotConfig.RobotInfo> robots = config.getList();
        if (robots == null || robots.isEmpty()) {
            return new ConfigValidationResult(false, "机器人列表不能为空");
        }
        
        // 验证每个机器人的配置
        for (int i = 0; i < robots.size(); i++) {
            RobotConfig.RobotInfo robot = robots.get(i);
            ConfigValidationResult robotValidation = validateRobotInfo(robot, i);
            if (!robotValidation.isValid()) {
                return robotValidation;
            }
        }
        
        return new ConfigValidationResult(true, "机器人配置验证通过");
    }
    
    /**
     * 验证单个机器人信息
     */
    private ConfigValidationResult validateRobotInfo(RobotConfig.RobotInfo robot, int index) {
        if (robot == null) {
            return new ConfigValidationResult(false, String.format("第%d个机器人配置为空", index + 1));
        }
        
        if (!StringUtils.hasText(robot.getId())) {
            return new ConfigValidationResult(false, String.format("第%d个机器人ID不能为空", index + 1));
        }
        
        if (!StringUtils.hasText(robot.getName())) {
            return new ConfigValidationResult(false, String.format("第%d个机器人名称不能为空", index + 1));
        }
        
        if (!StringUtils.hasText(robot.getPersonality())) {
            return new ConfigValidationResult(false, String.format("第%d个机器人性格不能为空", index + 1));
        }
        
        if (!StringUtils.hasText(robot.getDescription())) {
            return new ConfigValidationResult(false, String.format("第%d个机器人描述不能为空", index + 1));
        }
        
        // 验证行为模式
        if (robot.getBehaviorPatterns() == null) {
            return new ConfigValidationResult(false, String.format("第%d个机器人行为模式不能为空", index + 1));
        }
        
        // 验证活跃时间
        if (robot.getActiveHours() == null || robot.getActiveHours().isEmpty()) {
            return new ConfigValidationResult(false, String.format("第%d个机器人活跃时间不能为空", index + 1));
        }
        
        return new ConfigValidationResult(true, "机器人信息验证通过");
    }
    
    @Override
    public ConfigStatus getConfigStatus() {
        return configStatus;
    }
    
    /**
     * 获取机器人同步统计信息
     */
    public RobotSyncStats getRobotSyncStats() {
        long totalRobots = robotRepository.count();
        long activeRobots = robotRepository.countByIsActiveTrue();
        
        return new RobotSyncStats(
            totalRobots,
            activeRobots,
            robotConfig != null && robotConfig.getList() != null ? robotConfig.getList().size() : 0,
            LocalDateTime.now()
        );
    }
    
    /**
     * 获取世界配置同步统计信息
     */
    public WorldSyncStats getWorldSyncStats() {
        long totalWorlds = worldConfigRepository.count();
        long activeWorlds = worldConfigRepository.countByIsActiveTrue();
        
        return new WorldSyncStats(
            totalWorlds,
            activeWorlds,
            worldConfig != null ? 1 : 0,
            LocalDateTime.now()
        );
    }
    
    /**
     * 机器人同步统计信息
     */
    public static class RobotSyncStats {
        private final long totalRobots;
        private final long activeRobots;
        private final long configRobots;
        private final LocalDateTime lastSync;
        
        public RobotSyncStats(long totalRobots, long activeRobots, long configRobots, LocalDateTime lastSync) {
            this.totalRobots = totalRobots;
            this.activeRobots = activeRobots;
            this.configRobots = configRobots;
            this.lastSync = lastSync;
        }
        
        public long getTotalRobots() { return totalRobots; }
        public long getActiveRobots() { return activeRobots; }
        public long getConfigRobots() { return configRobots; }
        public LocalDateTime getLastSync() { return lastSync; }
    }
    
    /**
     * 世界配置同步统计信息
     */
    public static class WorldSyncStats {
        private final long totalWorlds;
        private final long activeWorlds;
        private final long configWorlds;
        private final LocalDateTime lastSync;
        
        public WorldSyncStats(long totalWorlds, long activeWorlds, long configWorlds, LocalDateTime lastSync) {
            this.totalWorlds = totalWorlds;
            this.activeWorlds = activeWorlds;
            this.configWorlds = configWorlds;
            this.lastSync = lastSync;
        }
        
        public long getTotalWorlds() { return totalWorlds; }
        public long getActiveWorlds() { return activeWorlds; }
        public long getConfigWorlds() { return configWorlds; }
        public LocalDateTime getLastSync() { return lastSync; }
    }
} 