package com.myeden.service.impl;

import com.myeden.entity.Robot;
import com.myeden.entity.Post;
import com.myeden.repository.RobotRepository;
import com.myeden.repository.PostRepository;
import com.myeden.service.DifyService;
import com.myeden.service.PostService;
import com.myeden.service.CommentService;
import com.myeden.service.RobotBehaviorService;
import com.myeden.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.UUID;

/**
 * 机器人行为管理服务实现类
 * 实现AI机器人的行为触发、时机控制和状态管理
 * 
 * @author MyEden Team
 * @version 1.0.0
 */
@Service
public class RobotBehaviorServiceImpl implements RobotBehaviorService {
    
    private static final Logger logger = LoggerFactory.getLogger(RobotBehaviorServiceImpl.class);
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private DifyService difyService;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    private final Random random = new Random();
    private final ConcurrentHashMap<String, RobotDailyStats> dailyStats = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> localCache = new ConcurrentHashMap<>();
    
    /**
     * 机器人每日行为统计内部类
     */
    private static class RobotDailyStats {
        private int postCount = 0;
        private int commentCount = 0;
        private int replyCount = 0;
        private LocalDateTime lastReset = LocalDateTime.now();
        
        public void incrementPost() { postCount++; }
        public void incrementComment() { commentCount++; }
        public void incrementReply() { replyCount++; }
        
        public int getPostCount() { return postCount; }
        public int getCommentCount() { return commentCount; }
        public int getReplyCount() { return replyCount; }
        public LocalDateTime getLastReset() { return lastReset; }
        
        public void reset() {
            postCount = 0;
            commentCount = 0;
            replyCount = 0;
            lastReset = LocalDateTime.now();
        }
    }
    
    /**
     * 本地缓存操作 - 替代Redis功能
     */
    private void setCacheValue(String key, Object value) {
        localCache.put(key, value);
    }
    
    private Object getCacheValue(String key) {
        return localCache.get(key);
    }
    
    private void deleteCacheValue(String key) {
        localCache.remove(key);
    }
    
    private boolean hasCacheKey(String key) {
        return localCache.containsKey(key);
    }
    
    @Override
    public boolean triggerRobotPost(String robotId) {
        try {
            Robot robot = robotRepository.findByRobotId(robotId).orElse(null);
            if (robot == null || !robot.getIsActive()) {
                logger.warn("机器人不存在或未激活: {}", robotId);
                return false;
            }
            
            // 检查是否在活跃时间段
            if (!isRobotActive(robot)) {
                logger.info("机器人不在活跃时间段: {}", robotId);
                return false;
            }
            
            // 检查今日发布数量限制
            RobotDailyStats stats = getDailyStats(robotId);
            if (stats.getPostCount() >= 10) { // 每日最多10条动态
                logger.info("机器人今日发布数量已达上限: {}", robotId);
                return false;
            }
            
            // 计算触发概率
            double probability = calculateBehaviorProbability(robot, "post", "自动发布动态");
            if (random.nextDouble() > probability) {
                logger.info("机器人发布动态概率未触发: {}, 概率: {}", robotId, probability);
                return false;
            }
            
            // 生成动态内容
            String context = buildPostContext();
            String content = difyService.generatePostContent(robot, context);
            
            // 直接创建动态实体，避免调用postService.createPost
            Post post = new Post();
            post.setPostId(generatePostId());
            post.setAuthorId(robotId);
            post.setAuthorType("robot");
            post.setContent(content);
            post.setImages(new ArrayList<>());
            post.setLikeCount(0);
            post.setCommentCount(0);
            post.setIsDeleted(false);
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            
            // 保存到数据库
            Post savedPost = postRepository.save(post);
            
            if (savedPost != null) {
                stats.incrementPost();
                logger.info("机器人成功发布动态: {}, 内容: {}", robotId, content);
                
                // 推送WebSocket消息
                try {
                    Map<String, Object> actionData = new HashMap<>();
                    actionData.put("robotId", robotId);
                    actionData.put("robotName", robot.getName());
                    actionData.put("actionType", "post");
                    actionData.put("actionContent", content);
                    actionData.put("postId", savedPost.getPostId());
                    actionData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    
                    webSocketService.pushRobotAction(actionData);
                    logger.info("WebSocket机器人行为消息推送成功");
                } catch (Exception e) {
                    logger.warn("WebSocket消息推送失败", e);
                }
                
                return true;
            }
            
            return false;
        } catch (Exception e) {
            logger.error("触发机器人发布动态失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean triggerRobotComment(String robotId, String postId) {
        try {
            Robot robot = robotRepository.findByRobotId(robotId).orElse(null);
            if (robot == null || !robot.getIsActive()) {
                return false;
            }
            
            // 检查是否在活跃时间段
            if (!isRobotActive(robot)) {
                return false;
            }
            
            // 检查今日评论数量限制
            RobotDailyStats stats = getDailyStats(robotId);
            if (stats.getCommentCount() >= 20) { // 每日最多20条评论
                return false;
            }
            
            // 计算触发概率
            double probability = calculateBehaviorProbability(robot, "comment", "对动态发表评论");
            if (random.nextDouble() > probability) {
                return false;
            }
            
            // 获取动态内容
            PostService.PostDetail postDetail = postService.getPostDetail(postId);
            String postContent = postDetail.getContent();
            String context = buildCommentContext(postContent);
            String content = difyService.generateCommentContent(robot, postContent, context);
            
            // 发表评论
            CommentService.CommentResult commentResult = commentService.createComment(postId, robotId, "robot", content);
            if (commentResult != null) {
                stats.incrementComment();
                logger.info("机器人成功发表评论: {}, 内容: {}", robotId, content);
                
                // 推送WebSocket消息
                try {
                    Map<String, Object> actionData = new HashMap<>();
                    actionData.put("robotId", robotId);
                    actionData.put("robotName", robot.getName());
                    actionData.put("actionType", "comment");
                    actionData.put("actionContent", content);
                    actionData.put("postId", postId);
                    actionData.put("commentId", commentResult.getCommentId());
                    actionData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    
                    webSocketService.pushRobotAction(actionData);
                    logger.info("WebSocket机器人行为消息推送成功");
                } catch (Exception e) {
                    logger.warn("WebSocket消息推送失败", e);
                }
                
                return true;
            }
            
            return false;
        } catch (Exception e) {
            logger.error("触发机器人发表评论失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean triggerRobotReply(String robotId, String commentId) {
        try {
            Robot robot = robotRepository.findByRobotId(robotId).orElse(null);
            if (robot == null || !robot.getIsActive()) {
                return false;
            }
            
            // 检查是否在活跃时间段
            if (!isRobotActive(robot)) {
                return false;
            }
            
            // 检查今日回复数量限制
            RobotDailyStats stats = getDailyStats(robotId);
            if (stats.getReplyCount() >= 15) { // 每日最多15条回复
                return false;
            }
            
            // 计算触发概率
            double probability = calculateBehaviorProbability(robot, "reply", "回复评论");
            if (random.nextDouble() > probability) {
                return false;
            }
            
            // 获取评论内容
            CommentService.CommentDetail commentDetail = commentService.getCommentDetail(commentId);
            String commentContent = commentDetail.getContent();
            String context = buildReplyContext(commentContent);
            
            // 生成回复内容和内心活动
            String content = difyService.generateReplyContent(robot, commentContent, context);
            String innerThoughts = difyService.generateInnerThoughts(robot, "回复评论: " + commentContent);
            
            // 回复评论
            CommentService.CommentResult replyResult = commentService.replyComment(commentId, robotId, "robot", content);
            if (replyResult != null) {
                stats.incrementReply();
                logger.info("机器人成功回复评论: {}, 内容: {}, 内心活动: {}", robotId, content, innerThoughts);
                
                // 推送WebSocket消息
                try {
                    Map<String, Object> actionData = new HashMap<>();
                    actionData.put("robotId", robotId);
                    actionData.put("robotName", robot.getName());
                    actionData.put("actionType", "reply");
                    actionData.put("actionContent", content);
                    actionData.put("innerThoughts", innerThoughts);
                    actionData.put("commentId", commentId);
                    actionData.put("replyId", replyResult.getCommentId());
                    actionData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    
                    webSocketService.pushRobotAction(actionData);
                    logger.info("WebSocket机器人行为消息推送成功");
                } catch (Exception e) {
                    logger.warn("WebSocket消息推送失败", e);
                }
                
                return true;
            }
            
            return false;
        } catch (Exception e) {
            logger.error("触发机器人回复评论失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean isRobotActive(Robot robot) {
        if (robot == null || !robot.getIsActive()) {
            return false;
        }
        
        // 使用机器人配置的活跃时间段进行判断
        return robot.isInActiveTimeSlot();
    }
    
    @Override
    public double calculateBehaviorProbability(Robot robot, String behaviorType, String context) {
        double baseProbability = 0.3; // 基础概率
        
        // 根据行为类型调整概率
        switch (behaviorType) {
            case "post":
                baseProbability = 0.2; // 发布动态概率较低
                break;
            case "comment":
                baseProbability = 0.4; // 评论概率中等
                break;
            case "reply":
                baseProbability = 0.5; // 回复概率较高
                break;
        }
        
        // 根据时间调整概率
        LocalTime currentTime = LocalTime.now();
        if (currentTime.getHour() >= 9 && currentTime.getHour() <= 18) {
            baseProbability *= 1.5; // 工作时间概率提高
        } else if (currentTime.getHour() >= 19 && currentTime.getHour() <= 21) {
            baseProbability *= 1.2; // 晚上时间概率稍高
        } else {
            baseProbability *= 0.5; // 其他时间概率降低
        }
        
        // 根据机器人性格调整概率
        if (robot.getPersonality().contains("活泼") || robot.getPersonality().contains("开朗")) {
            baseProbability *= 1.3;
        } else if (robot.getPersonality().contains("安静") || robot.getPersonality().contains("内敛")) {
            baseProbability *= 0.7;
        }
        
        return Math.min(baseProbability, 1.0); // 确保概率不超过1
    }
    
    @Override
    public String getRobotDailyStats(String robotId) {
        RobotDailyStats stats = getDailyStats(robotId);
        return String.format("机器人%s今日统计 - 动态: %d, 评论: %d, 回复: %d", 
                           robotId, stats.getPostCount(), stats.getCommentCount(), stats.getReplyCount());
    }
    
    @Override
    public void resetRobotDailyStats(String robotId) {
        RobotDailyStats stats = getDailyStats(robotId);
        stats.reset();
        logger.info("重置机器人每日统计: {}", robotId);
    }
    
    @Override
    public void startBehaviorScheduler() {
        logger.info("启动机器人行为调度器");
    }
    
    @Override
    public void stopBehaviorScheduler() {
        logger.info("停止机器人行为调度器");
    }
    
    /**
     * 定时触发机器人行为（每分钟执行一次）
     */
    @Scheduled(fixedRate = 60000) // 1分钟
    public void scheduledRobotBehavior() {
        try {
            List<Robot> activeRobots = robotRepository.findByIsActiveTrue();
            for (Robot robot : activeRobots) {
                if (isRobotActive(robot)) {
                    // 随机触发机器人行为
                    double randomValue = random.nextDouble();
                    if (randomValue < 0.3) {
                        triggerRobotPost(robot.getRobotId());
                    } else if (randomValue < 0.6) {
                        // 随机选择一个动态进行评论
                        // 这里需要实现动态选择逻辑
                    }
                }
            }
        } catch (Exception e) {
            logger.error("定时机器人行为执行失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 每日重置机器人统计（每天0点执行）
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetDailyStats() {
        try {
            dailyStats.clear();
            logger.info("重置所有机器人每日统计");
        } catch (Exception e) {
            logger.error("重置每日统计失败: {}", e.getMessage(), e);
        }
    }
    
    // 辅助方法
    private RobotDailyStats getDailyStats(String robotId) {
        return dailyStats.computeIfAbsent(robotId, k -> new RobotDailyStats());
    }
    
    private String buildPostContext() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("当前时间: %s, 天气: 晴朗, 心情: 愉快", 
                           now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }
    
    private String buildCommentContext(String postContent) {
        return String.format("对动态内容进行评论: %s", postContent);
    }
    
    private String buildReplyContext(String commentContent) {
        return String.format("对评论内容进行回复: %s", commentContent);
    }
    
    /**
     * 刷新机器人在线状态（每5分钟执行一次）
     * 根据机器人的活跃时间配置更新数据库中的isActive状态
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void refreshRobotActiveStatus() {
        try {
            logger.info("开始刷新机器人在线状态...");
            List<Robot> allRobots = robotRepository.findAll();
            int updatedCount = 0;
            
            for (Robot robot : allRobots) {
                boolean shouldBeActive = robot.isInActiveTimeSlot();
                boolean currentActive = robot.getIsActive();
                
                // 如果状态需要更新
                if (shouldBeActive != currentActive) {
                    robot.setIsActive(shouldBeActive);
                    robot.setUpdatedAt(LocalDateTime.now());
                    robotRepository.save(robot);
                    updatedCount++;
                    
                    logger.info("机器人 {} 状态更新: {} -> {}", 
                              robot.getName(), 
                              currentActive ? "在线" : "离线", 
                              shouldBeActive ? "在线" : "离线");
                    
                    // 推送WebSocket消息通知状态变化
                    try {
                        Map<String, Object> statusData = new HashMap<>();
                        statusData.put("robotId", robot.getRobotId());
                        statusData.put("robotName", robot.getName());
                        statusData.put("status", shouldBeActive ? "online" : "offline");
                        statusData.put("statusText", shouldBeActive ? "在线" : "离线");
                        statusData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                        
                        webSocketService.pushRobotAction(statusData);
                        logger.debug("WebSocket机器人状态变化消息推送成功: {}", robot.getName());
                    } catch (Exception e) {
                        logger.warn("WebSocket状态变化消息推送失败: {}", e.getMessage());
                    }
                }
            }
            
            if (updatedCount > 0) {
                logger.info("机器人状态刷新完成，共更新 {} 个机器人状态", updatedCount);
            } else {
                logger.debug("机器人状态刷新完成，无需更新");
            }
            
        } catch (Exception e) {
            logger.error("刷新机器人在线状态失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 触发所有在线机器人对指定动态进行评论
     * 当有新动态发布时，自动触发所有符合条件的机器人进行AI评论
     * 
     * @param postId 动态ID
     * @param postContent 动态内容（用于日志记录）
     * @return 成功触发的机器人数量
     */
    @Async("aiTaskExecutor")
    public void triggerAllRobotsComment(String postId, String postContent) {
        try {
            logger.info("开始触发所有在线机器人评论，动态ID: {}, 内容: {}", postId, 
                       postContent != null ? postContent.substring(0, Math.min(postContent.length(), 50)) + "..." : "无内容");
            
            // 获取所有激活的机器人
            List<Robot> activeRobots = robotRepository.findByIsActiveTrue();
            if (activeRobots.isEmpty()) {
                logger.info("没有找到激活的机器人");
                return;
            }
            
            int triggeredCount = 0;
            int totalRobots = activeRobots.size();
            List<String> triggeredRobots = new ArrayList<>();
            List<String> skippedRobots = new ArrayList<>();
            
            for (Robot robot : activeRobots) {
                try {
                    // 检查机器人是否在活跃时间段
                    if (!isRobotActive(robot)) {
                        logger.debug("机器人 {} 不在活跃时间段，跳过", robot.getName());
                        skippedRobots.add(robot.getName() + "(非活跃时间)");
                        continue;
                    }
                    
                    // 检查今日评论数量限制
                    RobotDailyStats stats = getDailyStats(robot.getRobotId());
                    if (stats.getCommentCount() >= 20) { // 每日最多20条评论
                        logger.debug("机器人 {} 今日评论数量已达上限，跳过", robot.getName());
                        skippedRobots.add(robot.getName() + "(评论上限)");
                        continue;
                    }
                    
                    // 计算触发概率
                    double probability = calculateBehaviorProbability(robot, "comment", "对动态发表评论");
                    if (random.nextDouble() > probability) {
                        logger.debug("机器人 {} 评论概率未触发，概率: {}", robot.getName(), probability);
                        skippedRobots.add(robot.getName() + "(概率未命中)");
                        continue;
                    }
                    
                    // 触发机器人评论
                    boolean success = triggerRobotComment(robot.getRobotId(), postId);
                    if (success) {
                        triggeredCount++;
                        triggeredRobots.add(robot.getName());
                        logger.info("机器人 {} 成功触发评论", robot.getName());
                    } else {
                        skippedRobots.add(robot.getName() + "(触发失败)");
                        logger.debug("机器人 {} 触发评论失败", robot.getName());
                    }
                    
                    // 添加随机延迟，避免机器人同时评论
                    Thread.sleep(random.nextInt(3000) + 1000); // 1-4秒随机延迟
                    
                } catch (Exception e) {
                    logger.error("触发机器人 {} 评论失败: {}", robot.getName(), e.getMessage());
                    skippedRobots.add(robot.getName() + "(异常:" + e.getMessage() + ")");
                }
            }
            
            // 记录详细的触发结果
            logger.info("AI机器人评论触发完成，动态ID: {}, 总机器人: {}, 成功触发: {}", 
                      postId, totalRobots, triggeredCount);
            logger.info("成功触发的机器人: {}", String.join(", ", triggeredRobots));
            if (!skippedRobots.isEmpty()) {
                logger.info("跳过的机器人: {}", String.join(", ", skippedRobots));
            }
            
            return;
            
        } catch (Exception e) {
            logger.error("触发所有机器人评论失败: {}", e.getMessage(), e);
            return;
        }
    }
    
    /**
     * 生成动态ID
     */
    private String generatePostId() {
        return "post_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
} 