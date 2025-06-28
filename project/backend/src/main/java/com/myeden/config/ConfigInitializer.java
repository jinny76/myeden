package com.myeden.config;

import com.myeden.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 配置初始化器
 * 
 * 功能说明：
 * - 应用启动时自动加载配置文件
 * - 将机器人配置同步到数据库
 * - 验证配置的完整性和正确性
 * - 提供启动状态反馈
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Component
public class ConfigInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigInitializer.class);
    
    @Autowired
    private ConfigService configService;
    
    /**
     * 应用启动时执行配置初始化
     */
    @Override
    public void run(String... args) throws Exception {
        logger.info("🚀 开始初始化配置...");
        
        try {
            // 加载世界配置
            logger.info("📖 加载世界配置...");
            var worldConfig = configService.loadWorldConfig();
            if (worldConfig != null) {
                logger.info("✅ 世界配置加载成功: {}", worldConfig.getName());
            } else {
                logger.error("❌ 世界配置加载失败");
            }
            
            // 加载机器人配置
            logger.info("🤖 加载机器人配置...");
            var robotConfig = configService.loadRobotConfig();
            if (robotConfig != null) {
                logger.info("✅ 机器人配置加载成功，机器人数量: {}", 
                    robotConfig.getList() != null ? robotConfig.getList().size() : 0);
            } else {
                logger.error("❌ 机器人配置加载失败");
            }
            
            // 检查配置状态
            var configStatus = configService.getConfigStatus();
            if (configStatus.isWorldConfigLoaded() && configStatus.isRobotConfigLoaded()) {
                logger.info("🎉 所有配置加载完成");
            } else {
                logger.warn("⚠️ 部分配置加载失败，请检查配置文件");
                if (configStatus.getErrorMessage() != null) {
                    logger.error("错误信息: {}", configStatus.getErrorMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("❌ 配置初始化失败", e);
            throw e;
        }
    }
} 