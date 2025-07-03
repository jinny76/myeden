package com.myeden.service.impl;

import com.myeden.config.WorldConfig;
import com.myeden.config.RobotConfig;
import com.myeden.entity.Robot;
import com.myeden.entity.User;
import com.myeden.entity.Post;
import com.myeden.entity.Comment;
import com.myeden.repository.RobotRepository;
import com.myeden.repository.UserRepository;
import com.myeden.repository.PostRepository;
import com.myeden.repository.CommentRepository;
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
 * <p>
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

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

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
            logger.info("从数据库获取实时世界统计信息...");

            // 从数据库实时统计
            long totalUsers = userRepository.count();
            long totalPosts = postRepository.count();
            long totalComments = commentRepository.count();
            long totalRobots = robotRepository.count();
            long activeRobots = robotRepository.countByIsActiveTrue();

            // 获取世界创建时间（从配置文件中获取）
            String worldCreatedAt = "2024-01-01";
            try {
                WorldConfig worldConfig = configService.loadWorldConfig();
                if (worldConfig != null && worldConfig.getStatistics() != null) {
                    worldCreatedAt = worldConfig.getStatistics().getWorldCreatedAt();
                }
            } catch (Exception e) {
                logger.warn("无法从配置文件获取世界创建时间，使用默认值", e);
            }

            WorldStatistics statistics = new WorldStatistics(
                    (int) totalUsers,
                    (int) totalPosts,
                    (int) totalComments,
                    (int) totalRobots,
                    (int) activeRobots,
                    worldCreatedAt
            );

            logger.info("世界统计信息 - 用户: {}, 动态: {}, 评论: {}, 机器人: {} (在线: {})",
                    totalUsers, totalPosts, totalComments, totalRobots, activeRobots);

            return statistics;

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
            logger.info("从数据库获取机器人列表...");
            List<Robot> robots = robotRepository.findAll();

            List<RobotSummary> robotSummaries = robots.stream()
                    .map(robot -> new RobotSummary(
                            robot.getRobotId(), // 使用业务逻辑的robotId而不是MongoDB的id
                            robot.getName(),
                            robot.getName(), // 使用name作为nickname
                            robot.getAvatar(),
                            robot.getPersonality(),
                            robot.getDescription(), // 使用description作为description
                            robot.getIsActive() != null ? robot.getIsActive() : false,
                            robot.getIsDeleted() != null ? robot.getIsDeleted() : false,
                            robot.getGender(),
                            robot.getAge()
                    ))
                    .collect(Collectors.toList());

            logger.info("成功获取 {} 个机器人信息", robotSummaries.size());
            return robotSummaries;

        } catch (Exception e) {
            logger.error("获取机器人列表失败", e);
            return null;
        }
    }

    @Override
    public RobotDetail getRobotDetail(String robotId) {
        try {
            Robot robot = robotRepository.findByRobotId(robotId).orElse(null);
            if (robot == null) {
                logger.warn("未找到机器人: {}", robotId);
                return null;
            }

            // 转换活跃时间为ActiveHours格式
            List<ActiveHours> activeHours = null;
            if (robot.getActiveHours() != null && !robot.getActiveHours().isEmpty()) {
                activeHours = robot.getActiveHours().stream()
                        .map(range -> new ActiveHours(
                                range.getStart(),
                                range.getEnd(),
                                1.0 // 默认概率为1.0
                        ))
                        .collect(Collectors.toList());
            }

            return new RobotDetail(
                    robot.getRobotId(), // 使用业务逻辑的robotId而不是MongoDB的id
                    robot.getName(),
                    robot.getName(), // 使用name作为nickname
                    robot.getAvatar(),
                    robot.getPersonality(),
                    robot.getDescription(), // 使用description作为description
                    robot.getDescription(), // 使用description作为background
                    null, // example - Robot实体中没有对应字段
                    null, // traits - Robot实体中没有对应字段
                    null, // interests - Robot实体中没有对应字段
                    null, // speakingStyle - Robot实体中没有对应字段
                    null, // behaviorPatterns - Robot实体中没有对应字段
                    activeHours,
                    robot.getIsActive() != null ? robot.getIsActive() : false
            );

        } catch (Exception e) {
            logger.error("获取机器人详情失败: {}", robotId, e);
            return null;
        }
    }
} 