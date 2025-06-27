package com.myeden.service.impl;

import com.myeden.config.WorldConfig;
import com.myeden.config.RobotConfig;
import com.myeden.service.WorldService;
import com.myeden.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 世界管理服务实现类
 * 
 * 功能说明：
 * - 实现世界信息查询功能
 * - 管理世界背景和设定展示
 * - 提供世界统计信息
 * - 管理世界活动信息
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
public class WorldServiceImpl implements WorldService {
    
    private static final Logger logger = LoggerFactory.getLogger(WorldServiceImpl.class);
    
    @Autowired
    private ConfigService configService;
    
    @Override
    public WorldInfo getWorldInfo() {
        try {
            WorldConfig worldConfig = configService.loadWorldConfig();
            if (worldConfig == null) {
                logger.error("世界配置加载失败");
                return null;
            }
            
            return new WorldInfo(
                worldConfig.getName(),
                worldConfig.getVersion(),
                worldConfig.getDescription(),
                worldConfig.getStatistics().getWorldCreatedAt()
            );
            
        } catch (Exception e) {
            logger.error("获取世界信息失败", e);
            return null;
        }
    }
    
    @Override
    public WorldBackground getWorldBackground() {
        try {
            WorldConfig worldConfig = configService.loadWorldConfig();
            if (worldConfig == null || worldConfig.getBackground() == null) {
                logger.error("世界背景配置加载失败");
                return null;
            }
            
            WorldConfig.Background background = worldConfig.getBackground();
            return new WorldBackground(
                background.getStory(),
                background.getRules(),
                background.getFeatures()
            );
            
        } catch (Exception e) {
            logger.error("获取世界背景信息失败", e);
            return null;
        }
    }
    
    @Override
    public WorldEnvironment getWorldEnvironment() {
        try {
            WorldConfig worldConfig = configService.loadWorldConfig();
            if (worldConfig == null || worldConfig.getEnvironment() == null) {
                logger.error("世界环境配置加载失败");
                return null;
            }
            
            WorldConfig.Environment environment = worldConfig.getEnvironment();
            return new WorldEnvironment(
                environment.getTheme(),
                environment.getColorScheme(),
                environment.getAtmosphere(),
                environment.getWeather()
            );
            
        } catch (Exception e) {
            logger.error("获取世界环境信息失败", e);
            return null;
        }
    }
    
    @Override
    public WorldActivities getWorldActivities() {
        try {
            WorldConfig worldConfig = configService.loadWorldConfig();
            if (worldConfig == null || worldConfig.getActivities() == null) {
                logger.error("世界活动配置加载失败");
                return null;
            }
            
            WorldConfig.Activities activities = worldConfig.getActivities();
            
            // 转换日常活动
            List<DailyEvent> dailyEvents = null;
            if (activities.getDailyEvents() != null) {
                dailyEvents = activities.getDailyEvents().stream()
                    .map(event -> new DailyEvent(
                        event.getName(),
                        event.getTime(),
                        event.getDescription()
                    ))
                    .collect(Collectors.toList());
            }
            
            // 转换特殊活动
            List<SpecialEvent> specialEvents = null;
            if (activities.getSpecialEvents() != null) {
                specialEvents = activities.getSpecialEvents().stream()
                    .map(event -> new SpecialEvent(
                        event.getName(),
                        event.getTrigger(),
                        event.getDescription()
                    ))
                    .collect(Collectors.toList());
            }
            
            return new WorldActivities(dailyEvents, specialEvents);
            
        } catch (Exception e) {
            logger.error("获取世界活动信息失败", e);
            return null;
        }
    }
    
    @Override
    public WorldStatistics getWorldStatistics() {
        try {
            WorldConfig worldConfig = configService.loadWorldConfig();
            if (worldConfig == null || worldConfig.getStatistics() == null) {
                logger.error("世界统计配置加载失败");
                return null;
            }
            
            WorldConfig.Statistics statistics = worldConfig.getStatistics();
            return new WorldStatistics(
                statistics.getTotalUsers(),
                statistics.getTotalPosts(),
                statistics.getTotalComments(),
                statistics.getTotalRobots(),
                statistics.getWorldCreatedAt()
            );
            
        } catch (Exception e) {
            logger.error("获取世界统计信息失败", e);
            return null;
        }
    }
    
    @Override
    public WorldSettings getWorldSettings() {
        try {
            WorldConfig worldConfig = configService.loadWorldConfig();
            if (worldConfig == null || worldConfig.getSettings() == null) {
                logger.error("世界设置配置加载失败");
                return null;
            }
            
            WorldConfig.Settings settings = worldConfig.getSettings();
            return new WorldSettings(
                settings.getMaxPostLength(),
                settings.getMaxCommentLength(),
                settings.getMaxImageSize(),
                settings.getAllowedImageTypes(),
                settings.getAutoCleanupDays(),
                settings.getMaxRobotsPerUser()
            );
            
        } catch (Exception e) {
            logger.error("获取世界设置信息失败", e);
            return null;
        }
    }
    
    @Override
    public List<RobotSummary> getRobotList() {
        try {
            RobotConfig robotConfig = configService.loadRobotConfig();
            if (robotConfig == null || robotConfig.getList() == null) {
                logger.error("机器人配置加载失败");
                return null;
            }
            
            return robotConfig.getList().stream()
                .map(robot -> new RobotSummary(
                    robot.getId(),
                    robot.getName(),
                    robot.getNickname(),
                    robot.getAvatar(),
                    robot.getPersonality(),
                    robot.getDescription(),
                    robot.isActive()
                ))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.error("获取机器人列表失败", e);
            return null;
        }
    }
    
    @Override
    public RobotDetail getRobotDetail(String robotId) {
        try {
            RobotConfig robotConfig = configService.loadRobotConfig();
            if (robotConfig == null || robotConfig.getList() == null) {
                logger.error("机器人配置加载失败");
                return null;
            }
            
            // 查找指定机器人
            RobotConfig.RobotInfo robotInfo = robotConfig.getList().stream()
                .filter(robot -> robot.getId().equals(robotId))
                .findFirst()
                .orElse(null);
                
            if (robotInfo == null) {
                logger.warn("未找到机器人: {}", robotId);
                return null;
            }
            
            // 转换说话风格
            SpeakingStyle speakingStyle = null;
            if (robotInfo.getSpeakingStyle() != null) {
                RobotConfig.SpeakingStyle style = robotInfo.getSpeakingStyle();
                speakingStyle = new SpeakingStyle(
                    style.getTone(),
                    style.getVocabulary(),
                    style.getEmojiUsage(),
                    style.getSentenceLength()
                );
            }
            
            // 转换行为模式
            BehaviorPatterns behaviorPatterns = null;
            if (robotInfo.getBehaviorPatterns() != null) {
                RobotConfig.BehaviorPatterns patterns = robotInfo.getBehaviorPatterns();
                behaviorPatterns = new BehaviorPatterns(
                    patterns.getGreetingFrequency(),
                    patterns.getComfortFrequency(),
                    patterns.getShareFrequency(),
                    patterns.getCommentFrequency(),
                    patterns.getReplyFrequency()
                );
            }
            
            // 转换活跃时间
            List<ActiveHours> activeHours = null;
            if (robotInfo.getActiveHours() != null) {
                activeHours = robotInfo.getActiveHours().stream()
                    .map(hours -> new ActiveHours(
                        hours.getStart(),
                        hours.getEnd(),
                        hours.getProbability()
                    ))
                    .collect(Collectors.toList());
            }
            
            return new RobotDetail(
                robotInfo.getId(),
                robotInfo.getName(),
                robotInfo.getNickname(),
                robotInfo.getAvatar(),
                robotInfo.getPersonality(),
                robotInfo.getDescription(),
                robotInfo.getBackground(),
                robotInfo.getTraits(),
                robotInfo.getInterests(),
                speakingStyle,
                behaviorPatterns,
                activeHours,
                robotInfo.isActive()
            );
            
        } catch (Exception e) {
            logger.error("获取机器人详情失败: {}", robotId, e);
            return null;
        }
    }
} 