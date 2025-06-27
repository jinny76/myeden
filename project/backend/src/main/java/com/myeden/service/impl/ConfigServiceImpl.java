package com.myeden.service.impl;

import com.myeden.config.WorldConfig;
import com.myeden.config.RobotConfig;
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

/**
 * 配置管理服务实现类
 * 
 * 功能说明：
 * - 实现配置文件加载和解析功能
 * - 支持配置热更新和动态重载
 * - 管理世界配置和机器人配置
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
} 