package com.myeden.service;

import com.myeden.config.WorldConfig;
import com.myeden.config.RobotConfig;

/**
 * 配置管理服务接口
 * 
 * 功能说明：
 * - 提供配置文件加载和解析功能
 * - 支持配置热更新和动态重载
 * - 管理世界配置和机器人配置
 * - 提供配置验证和错误处理
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface ConfigService {
    
    /**
     * 加载世界配置
     * @return 世界配置对象
     */
    WorldConfig loadWorldConfig();
    
    /**
     * 加载机器人配置
     * @return 机器人配置对象
     */
    RobotConfig loadRobotConfig();
    
    /**
     * 重新加载世界配置
     * @return 是否加载成功
     */
    boolean reloadWorldConfig();
    
    /**
     * 重新加载机器人配置
     * @return 是否加载成功
     */
    boolean reloadRobotConfig();
    
    /**
     * 验证世界配置
     * @param config 世界配置对象
     * @return 验证结果
     */
    ConfigValidationResult validateWorldConfig(WorldConfig config);
    
    /**
     * 验证机器人配置
     * @param config 机器人配置对象
     * @return 验证结果
     */
    ConfigValidationResult validateRobotConfig(RobotConfig config);
    
    /**
     * 获取配置加载状态
     * @return 配置状态信息
     */
    ConfigStatus getConfigStatus();

    RobotConfig getRobotConfig();

    WorldConfig getWorldConfig();

    /**
     * 配置验证结果
     */
    class ConfigValidationResult {
        private boolean valid;
        private String message;
        private String details;
        
        public ConfigValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public ConfigValidationResult(boolean valid, String message, String details) {
            this.valid = valid;
            this.message = message;
            this.details = details;
        }
        
        // Getter方法
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public String getDetails() { return details; }
    }
    
    /**
     * 配置状态信息
     */
    class ConfigStatus {
        private boolean worldConfigLoaded;
        private boolean robotConfigLoaded;
        private String lastLoadTime;
        private String errorMessage;
        
        public ConfigStatus(boolean worldConfigLoaded, boolean robotConfigLoaded, String lastLoadTime) {
            this.worldConfigLoaded = worldConfigLoaded;
            this.robotConfigLoaded = robotConfigLoaded;
            this.lastLoadTime = lastLoadTime;
        }
        
        // Getter和Setter方法
        public boolean isWorldConfigLoaded() { return worldConfigLoaded; }
        public void setWorldConfigLoaded(boolean worldConfigLoaded) { this.worldConfigLoaded = worldConfigLoaded; }
        
        public boolean isRobotConfigLoaded() { return robotConfigLoaded; }
        public void setRobotConfigLoaded(boolean robotConfigLoaded) { this.robotConfigLoaded = robotConfigLoaded; }
        
        public String getLastLoadTime() { return lastLoadTime; }
        public void setLastLoadTime(String lastLoadTime) { this.lastLoadTime = lastLoadTime; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
} 