package com.myeden.service.impl;

import com.myeden.entity.Robot;
import com.myeden.entity.Post;
import com.myeden.entity.Comment;
import com.myeden.entity.ChatMessage;
import com.myeden.model.external.WeatherInfo;
import com.myeden.repository.RobotRepository;
import com.myeden.repository.PostRepository;
import com.myeden.repository.CommentRepository;
import com.myeden.service.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Objects;
import com.myeden.service.impl.PromptServiceImpl;

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
    private CommentRepository commentRepository;
    
    @Autowired
    private PromptService promptService;
    
    @Autowired
    @Lazy
    private PostService postService;
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired
    private UserRobotLinkService userRobotLinkService;

    @Autowired
    private ExternalDataCacheService externalDataCacheService;

    @Autowired
    private SearchContentService searchContentService;
    
    @Autowired
    private AIChatService aiChatService;
    
    private final Random random = new Random();
    private final ConcurrentHashMap<String, RobotDailyStats> dailyStats = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> localCache = new ConcurrentHashMap<>();
    
    /**
     * 聊天提示词结果内部类
     */
    private static class ChatPromptResult {
        private String prompt;
        private boolean isConfessionTopic;
        
        public ChatPromptResult(String prompt, boolean isConfessionTopic) {
            this.prompt = prompt;
            this.isConfessionTopic = isConfessionTopic;
        }
        
        public String getPrompt() { return prompt; }
        public boolean isConfessionTopic() { return isConfessionTopic; }
    }
    
    /**
     * 主动聊天结果内部类
     */
    private static class ProactiveChatResult {
        private String content;
        private boolean isConfessionTopic;
        
        public ProactiveChatResult(String content, boolean isConfessionTopic) {
            this.content = content;
            this.isConfessionTopic = isConfessionTopic;
        }
        
        public String getContent() { return content; }
        public boolean isConfessionTopic() { return isConfessionTopic; }
    }
    
    /**
     * 机器人每日行为统计内部类
     */
    private static class RobotDailyStats {
        private int postCount = 0;
        private int commentCount = 0;
        private int replyCount = 0;
        private int proactiveChatCount = 0;
        private LocalDateTime lastReset = LocalDateTime.now();
        
        public void incrementPost() {
            postCount++;
        }

        public void incrementComment() {
            commentCount++;
        }

        public void incrementReply() {
            replyCount++;
        }
        
        public void incrementProactiveChat() {
            proactiveChatCount++;
        }
        
        public int getPostCount() {
            return postCount;
        }

        public int getCommentCount() {
            return commentCount;
        }

        public int getReplyCount() {
            return replyCount;
        }
        
        public int getProactiveChatCount() {
            return proactiveChatCount;
        }

        public LocalDateTime getLastReset() {
            return lastReset;
        }
        
        public void reset() {
            postCount = 0;
            commentCount = 0;
            replyCount = 0;
            proactiveChatCount = 0;
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
    
    /**
     * 检查机器人是否满足发帖/评论/回复的前置条件
     * @param robotId 机器人ID
     * @param behaviorType 行为类型（post/comment/reply）
     * @param context 行为上下文（如"自动发布动态"等）
     * @param isRobot 是否对机器人内容操作（如评论/回复对象是否为机器人）
     * @return 满足条件返回Robot对象，否则返回null
     */
    private Robot checkRobotPostCondition(String robotId, String behaviorType, String context, boolean isRobot) {
            Robot robot = robotRepository.findByRobotId(robotId).orElse(null);
            if (robot == null) {
                logger.warn("机器人不存在: {}", robotId);
                return null;
            }
            
            // 直接检查机器人是否在活跃时间段，不依赖数据库中的isActive字段
            if (!isRobotActive(robot)) {
                logger.info("机器人不在活跃时间段: {}", robotId);
                return null;
            }
            
            // 计算触发概率
        double probability = calculateBehaviorProbability(robot, behaviorType, context, isRobot);
            if (random.nextDouble() > probability) {
            logger.info("机器人{}概率未触发: {}, 概率: {}", behaviorType, robotId, probability);
            return null;
        }
        return robot;
    }

    @Override
    public boolean triggerRobotPost(String robotId) {
        try {
            // 统一前置条件判断
            Robot robot = checkRobotPostCondition(robotId, "post", "自动发布动态", true);
            if (robot == null) return false;

            // 检查今日发布数量限制
            RobotDailyStats stats = getDailyStats(robotId);
            /*
             * if (stats.getPostCount() >= 10) { // 每日最多10条动态
             * logger.info("机器人今日发布数量已达上限, 最多10条: {}", robotId);
             * return false;
             * }
             */
            
            // 生成动态内容
            String context = buildPostContext(robot);
            PromptServiceImpl.PostContentResult postResult = promptService.generatePostContent(robot, context);
            String content = postResult.getContent();
            if (StringUtils.isNotBlank(content)) {
                String innerThoughts = promptService.generateInnerThoughts(robot, "发布动态: " + content);

                // 直接创建动态实体，避免调用postService.createPost
                Post post = new Post();
                post.setPostId(generatePostId());
                post.setAuthorId(robotId);
                post.setAuthorType("robot");
                post.setContent(content);
                post.setInnerThoughts(innerThoughts);
                post.setImages(new ArrayList<>());
                post.setLikeCount(0);
                post.setCommentCount(0);
                post.setIsDeleted(false);
                post.setCreatedAt(LocalDateTime.now());
                post.setUpdatedAt(LocalDateTime.now());
                post.setLink(postResult.getLink());
                post.setTopic(postResult.getTopic());

                // 保存到数据库
                Post savedPost = postRepository.save(post);

                if (savedPost != null) {
                    stats.incrementPost();
                    logger.info("机器人成功发布动态: {}, 内容: {}, 内心活动: {}", robotId, content, innerThoughts);

                    // 推送WebSocket消息
                    try {
                        Map<String, Object> actionData = new HashMap<>();
                        actionData.put("robotId", robotId);
                        actionData.put("robotName", robot.getName());
                        actionData.put("actionType", "post");
                        actionData.put("actionContent", content);
                        actionData.put("innerThoughts", innerThoughts);
                        actionData.put("postId", savedPost.getPostId());
                        actionData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                        webSocketService.pushRobotAction(actionData);
                        logger.info("WebSocket机器人行为消息推送成功");
                    } catch (Exception e) {
                        logger.warn("WebSocket消息推送失败", e);
                    }

                    if (post.getTopic() != null && !post.getTopic().isEmpty()) {
                        for (String t : post.getTopic()) {
                            new Thread(() -> searchTopic(t)).start();
                        }
                    }

                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            logger.error("触发机器人发布动态失败: {}", e.getMessage(), e);
            return false;
        }
    }

    private void searchTopic(String topic) {
        searchContentService.triggerSearch(topic, "");
    }
    
    @Override
    public boolean triggerRobotComment(String robotId, String postId) {
        try {
            // 获取动态内容（先查post，后判断）
            PostService.PostDetail postDetail = postService.getPostDetail(postId, robotId);
            boolean isRobot = "robot".equals(postDetail.getAuthorType());
            // 统一前置条件判断
            Robot robot = checkRobotPostCondition(robotId, "comment", "对动态发表评论", isRobot);
            if (robot == null) return false;

            // 随机决定行为：点赞、评论或点赞加评论
            return triggerRandomRobotAction(robot, postId, postDetail);
            
        } catch (Exception e) {
            logger.error("触发机器人发表评论失败: {}", e.getMessage(), e);
                return false;
            }
    }

    /**
     * 随机决定机器人的行为：点赞、评论或点赞加评论
     * @param robot 机器人对象
     * @param postId 动态ID
     * @param postDetail 动态详情
     * @return 是否成功执行了行为
     */
    private boolean triggerRandomRobotAction(Robot robot, String postId, PostService.PostDetail postDetail) {
        try {
            // 随机决定行为类型
            int actionType = random.nextInt(3); // 0: 只点赞, 1: 只评论, 2: 点赞加评论
            boolean success = false;
            
            switch (actionType) {
                case 0: // 只点赞
                    success = performRobotLike(robot, postId);
                    break;
                case 1: // 只评论
                    success = performRobotComment(robot, postId, postDetail);
                    break;
                case 2: // 点赞加评论
                    boolean likeSuccess = performRobotLike(robot, postId);
                    boolean commentSuccess = performRobotComment(robot, postId, postDetail);
                    success = likeSuccess || commentSuccess; // 只要有一个成功就算成功
                    break;
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("随机机器人行为执行失败: {}", e.getMessage(), e);
                return false;
            }
    }

    /**
     * 执行机器人点赞行为
     * @param robot 机器人对象
     * @param postId 动态ID
     * @return 是否成功点赞
     */
    private boolean performRobotLike(Robot robot, String postId) {
        try {
            // 检查是否已经点赞过 - 通过查询点赞记录来判断
            // 机器人查看点赞信息时，传入机器人ID作为当前用户，获取所有可见的点赞记录
            List<PostService.LikeDetail> likes = postService.getPostLikes(postId, robot.getRobotId()).getLikes();
            boolean alreadyLiked = likes.stream()
                    .anyMatch(like -> like.getUserId().equals(robot.getRobotId()));
            
            if (alreadyLiked) {
                logger.info("机器人 {} 已经点赞过动态 {}", robot.getRobotId(), postId);
                return false;
            }

            // 执行点赞
            boolean likeResult = postService.likePost(postId, robot.getRobotId());
            if (likeResult) {
                logger.info("机器人 {} 成功点赞动态 {}", robot.getRobotId(), postId);

                // 推送WebSocket消息
                try {
                    Map<String, Object> actionData = new HashMap<>();
                    actionData.put("robotId", robot.getRobotId());
                    actionData.put("robotName", robot.getName());
                    actionData.put("actionType", "like");
                    actionData.put("postId", postId);
                    actionData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                    webSocketService.pushRobotAction(actionData);
                    logger.info("WebSocket机器人点赞消息推送成功");
                } catch (Exception e) {
                    logger.warn("WebSocket消息推送失败", e);
                }
                
                return true;
            }
            
            return false;
        } catch (Exception e) {
            logger.error("机器人点赞失败: {}", e.getMessage(), e);
                return false;
            }
    }

    /**
     * 执行机器人评论行为
     * @param robot 机器人对象
     * @param postId 动态ID
     * @param postDetail 动态详情
     * @return 是否成功评论
     */
    private boolean performRobotComment(Robot robot, String postId, PostService.PostDetail postDetail) {
        try {
            // 检查今日评论数量限制
            RobotDailyStats stats = getDailyStats(robot.getRobotId());

            String postContent = postDetail.getContent();
            String context = buildCommentContext(postContent, robot);
            String content = promptService.generateCommentContent(robot, postDetail, context);
            if (StringUtils.isNotBlank(content)) {
                String innerThoughts = promptService.generateInnerThoughts(robot, "评论动态: " + postContent);

                // 发表评论
                CommentService.CommentResult commentResult = commentService.createComment(postId, robot.getRobotId(), "robot", content,
                        innerThoughts);
                if (commentResult != null) {
                    stats.incrementComment();
                    logger.info("机器人成功发表评论: {}, 内容: {}, 内心活动: {}", robot.getRobotId(), content, innerThoughts);

                    // 推送WebSocket消息
                    try {
                        Map<String, Object> actionData = new HashMap<>();
                        actionData.put("robotId", robot.getRobotId());
                        actionData.put("robotName", robot.getName());
                        actionData.put("actionType", "comment");
                        actionData.put("actionContent", content);
                        actionData.put("innerThoughts", innerThoughts);
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
            }

            return false;
        } catch (Exception e) {
            logger.error("机器人评论失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean triggerRobotReply(String robotId, String commentId) {
        try {
            // 获取评论内容（先查comment，后判断）
            // 机器人查看评论详情时，传入机器人ID作为当前用户，获取有权限查看的评论信息
            CommentService.CommentDetail commentDetail = commentService.getCommentDetail(commentId, robotId);
            PostService.PostDetail postDetail = postService.getPostDetail(commentDetail.getPostId(), robotId);

            // 判断是否是机器人回复，且不是机器人自己回复
            boolean isRobot = "robot".equals(commentDetail.getAuthorType()) && !robotId.equals(postDetail.getAuthorId());
            // 统一前置条件判断
            Robot robot = checkRobotPostCondition(robotId, "reply", "回复评论", isRobot);
            if (robot == null) return false;
            
            // 检查今日回复数量限制
            RobotDailyStats stats = getDailyStats(robotId);
            /*if (stats.getReplyCount() >= 15) { // 每日最多15条回复
                logger.info("机器人回复超限: 每天最多15次 {}", robotId);
                return false;
            }*/

            String commentContent = commentDetail.getContent();
            String context = buildReplyContext(commentContent, robot);
            
            // 生成回复内容和内心活动
            String content = promptService.generateReplyContent(robot, commentDetail, postDetail, context);
            if (StringUtils.isNotBlank(content)) {
                String innerThoughts = promptService.generateInnerThoughts(robot, "回复评论: " + commentContent);

                // 回复评论
                CommentService.CommentResult replyResult = commentService.replyComment(commentId, robotId, "robot", content,
                        innerThoughts);
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
            }

            return false;
        } catch (Exception e) {
            logger.error("触发机器人回复评论失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean isRobotActive(Robot robot) {
        if (robot == null) {
            return false;
        }
        
        // 使用机器人配置的活跃时间段进行判断
        boolean isActive = robot.isInActiveTimeSlot();
        
        // 添加调试日志
        if (logger.isDebugEnabled()) {
            LocalTime currentTime = LocalTime.now();
            logger.debug("机器人 {} 活跃状态检查 - 当前时间: {}, 活跃时间段: {}, 结果: {}", 
                        robot.getName(), 
                        currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        robot.getActiveHours().stream()
                            .map(range -> range.getStart() + "-" + range.getEnd())
                            .collect(java.util.stream.Collectors.joining(", ")),
                        isActive ? "在线" : "离线");
        }
        
        return isActive;
    }
    
    /**
     * 计算行为触发概率 - 增加随机性和情绪影响
     */
    @Override
    public double calculateBehaviorProbability(Robot robot, String behaviorType, String context, Boolean isRobot) {
        try {
            double baseProbability = 1;
            
            // 根据行为类型调整基础概率
            switch (behaviorType) {
                case "post":
                    baseProbability = 0.1;
                    break;
                case "comment":
                    baseProbability = 0.2;
                    if (!isRobot) {
                        baseProbability = 0.75;
                    }
                    break;
                case "reply":
                    baseProbability = 0.2;
                    if (!isRobot) {
                        baseProbability = 0.75;
                    }
                    break;
                default:
                    baseProbability = 0.3;
            }

            // 时间因素
            LocalTime currentTime = LocalTime.now();
            double timeMultiplier = getTimeMultiplier(robot, currentTime);
            
            // 社交能量影响
            double socialEnergyMultiplier = getSocialEnergyMultiplier(robot);
            
            // 情绪影响
            double moodMultiplier = getMoodMultiplier(robot);
            
            // 随机因子
            double randomFactor = 0.3 + random.nextDouble() * 0.4; // 0.3-0.7
            
            // 计算最终概率
            double finalProbability = baseProbability * timeMultiplier * socialEnergyMultiplier * moodMultiplier
                    * randomFactor;
            
            // 确保概率在合理范围内
            if (finalProbability < 0) {
                finalProbability = 0;
            } else if (finalProbability > 1) {
                finalProbability = 1;
            }

            return finalProbability;
        } catch (Exception e) {
            logger.error("计算行为概率失败: {}", e.getMessage(), e);
            return 0.3; // 默认概率
        }
    }
    
    /**
     * 获取时间倍数
     */
    private double getTimeMultiplier(Robot robot, LocalTime currentTime) {
        // 这里可以根据机器人的活跃时间配置来计算
        // 简化实现，根据时间段返回不同的倍数
        int hour = currentTime.getHour();
        
        if (hour >= 8 && hour <= 12) {
            return 1.2; // 上午活跃
        } else if (hour >= 14 && hour <= 18) {
            return 1.3; // 下午活跃
        } else if (hour >= 19 && hour <= 23) {
            return 1.5; // 晚上最活跃
        } else if (hour >= 0 && hour <= 6) {
            return 0.3; // 深夜不活跃
        } else {
            return 0.8; // 其他时间
        }
    }
    
    /**
     * 获取社交能量倍数
     */
    private double getSocialEnergyMultiplier(Robot robot) {
        // 根据机器人的社交能量配置计算
        // 这里可以根据机器人的性格特征来调整
        String personality = robot.getPersonality();
        
        switch (personality) {
            case "文艺青年":
                return 0.6;
            case "技术宅":
                return 0.7;
            case "时尚达人":
                return 0.9;
            case "成熟稳重":
                return 0.4;
            case "运动达人":
                return 0.8;
            case "学霸女神":
                return 0.5;
            case "退休教师":
                return 0.3;
            case "可爱萌妹":
                return 0.9;
            default:
                return 0.7;
        }
    }
    
    /**
     * 获取情绪倍数
     */
    private double getMoodMultiplier(Robot robot) {
        // 模拟机器人的情绪状态
        // 这里可以实现更复杂的情绪系统
        double baseMood = 0.7;
        double moodSwing = random.nextDouble() * 0.6 - 0.3; // -0.3 到 0.3 的波动
        return baseMood + moodSwing;
    }
    
    @Override
    public String getRobotDailyStats(String robotId) {
        RobotDailyStats stats = getDailyStats(robotId);
        return String.format("机器人%s今日统计 - 动态: %d, 评论: %d, 回复: %d, 主动聊天: %d", 
                           robotId, stats.getPostCount(), stats.getCommentCount(), stats.getReplyCount(), stats.getProactiveChatCount());
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
            // 获取所有机器人，然后逐个检查活跃状态
            List<Robot> allRobots = robotRepository.findAll();
            for (Robot robot : allRobots) {
                // 直接检查机器人是否在活跃时间段，不依赖数据库中的isActive字段
                if (isRobotActive(robot)) {
                    // 随机触发机器人行为
                    double randomValue = random.nextDouble();
                    if (randomValue < 0.2) {
                        triggerRobotPost(robot.getRobotId());
                    } else if (randomValue < 0.4) {
                        // 随机选择一个今天的动态进行评论
                        triggerRobotCommentOnRecentPosts(robot.getRobotId());
                    } else if (randomValue < 0.6) {
                        // 随机选择一个今天的评论进行回复
                        triggerRobotReplyOnRecentComments(robot.getRobotId());
                    } else if (randomValue < 0.8) {
                        // 主动发起聊天
                        triggerRobotProactiveChat(robot.getRobotId());
                    }
                    // 20%概率什么都不做
                }
            }
        } catch (Exception e) {
            logger.error("定时机器人行为执行失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 为指定机器人触发对近三天帖子的评论
     * 对链接用户的帖子进行评论，机器人之间可以自由互动
     * 
     * @param robotId 机器人ID
     */
    private void triggerRobotCommentOnRecentPosts(String robotId) {
        try {
            Robot robot = robotRepository.findByRobotId(robotId).orElse(null);
            if (robot == null) {
                return;
            }
            
            // 直接检查机器人是否在活跃时间段，不依赖数据库中的isActive字段
            if (!isRobotActive(robot)) {
                return;
            }
            
            // 获取与机器人有链接的用户ID列表
            List<String> linkedUserIds = userRobotLinkService.getRobotActiveLinks(robotId)
                .stream()
                .map(UserRobotLinkService.LinkSummary::getUserId)
                .collect(Collectors.toList());
            
            // 获取今日的帖子，按时间倒序排列（最新的在前）
            LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
            // 获取所有机器人ID，作为 connectedRobotIds 传入，currentUserId 传 null
            List<String> allRobotIds = robotRepository.findAll().stream()
                .map(Robot::getRobotId)
                .collect(Collectors.toList());

            allRobotIds.addAll(linkedUserIds);

            List<Post> recentPosts = postRepository
                    .findByCreatedAtAfterAndIsDeletedFalseOrderByCreatedAtDesc(todayStart, null, allRobotIds);
            
            if (recentPosts.isEmpty()) {
                logger.debug("机器人 {} 没有找到今日的帖子", robot.getName());
                return;
            }
            
            // 分离用户帖子和机器人帖子
            List<Post> linkedUserPosts = new ArrayList<>();
            List<Post> robotPosts = new ArrayList<>();

            for (Post post : recentPosts) {
                // 跳过机器人自己发布的帖子
                if (robotId.equals(post.getAuthorId())) {
                    continue;
                }
                
                // 检查机器人是否已经评论过这个帖子
                boolean hasCommented = commentService.hasRobotCommentedOnPost(robotId, post.getPostId());
                if (!hasCommented) {
                    if ("user".equals(post.getAuthorType())) {
                        // 只处理链接用户的帖子
                        if (linkedUserIds.contains(post.getAuthorId())) {
                            linkedUserPosts.add(post);
                        }
                    } else if ("robot".equals(post.getAuthorType())) {
                        // 机器人之间可以自由互动
                        robotPosts.add(post);
                    }
                }
            }

            // 优先选择链接用户的帖子，如果没有则选择机器人的帖子
            List<Post> targetPosts = !linkedUserPosts.isEmpty() ? linkedUserPosts : robotPosts;
            
            if (targetPosts.isEmpty()) {
                logger.debug("机器人 {} 已经评论过所有可评论的近三天帖子", robot.getName());
                return;
            }
            
            // 选择最新的帖子（列表已经按时间倒序排列，所以第一个就是最新的）
            Post selectedPost = targetPosts.get(0);
            
            // 触发机器人评论
            boolean success = triggerRobotComment(robotId, selectedPost.getPostId());
            if (success) {
                String postType = linkedUserPosts.contains(selectedPost) ? "链接用户" : "机器人";
                logger.info("机器人 {} 成功对{}帖子 {} 触发评论", robot.getName(), postType, selectedPost.getPostId());
            } else {
                logger.debug("机器人 {} 对帖子 {} 触发评论失败", robot.getName(), selectedPost.getPostId());
            }
            
        } catch (Exception e) {
            logger.error("为机器人 {} 触发近三天帖子评论失败: {}", robotId, e.getMessage(), e);
        }
    }
    
    /**
     * 为指定机器人触发对近三天评论的回复
     * 对链接用户的评论进行回复，机器人之间可以自由互动
     * 
     * @param robotId 机器人ID
     */
    private void triggerRobotReplyOnRecentComments(String robotId) {
        try {
            Robot robot = robotRepository.findByRobotId(robotId).orElse(null);
            if (robot == null) {
                return;
            }
            
            // 直接检查机器人是否在活跃时间段，不依赖数据库中的isActive字段
            if (!isRobotActive(robot)) {
                return;
            }
            
            // 检查今日回复数量限制
            RobotDailyStats stats = getDailyStats(robotId);
            /*
             * if (stats.getReplyCount() >= 15) { // 每日最多15条回复
             * return;
             * }
             */
            
            // 获取与机器人有链接的用户ID列表
            List<String> linkedUserIds = userRobotLinkService.getRobotActiveLinks(robotId)
                .stream()
                .map(UserRobotLinkService.LinkSummary::getUserId)
                .collect(Collectors.toList());
            
            // 获取今日的评论，按时间倒序排列（最新的在前）
            LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
            List<Comment> recentComments = commentService.findRecentComments(todayStart);
            
            if (recentComments.isEmpty()) {
                logger.debug("机器人 {} 没有找到今日的评论", robot.getName());
                return;
            }
            
            // 分离用户评论和机器人评论
            List<Comment> linkedUserComments = new ArrayList<>();
            List<Comment> robotComments = new ArrayList<>();

            for (Comment comment : recentComments) {
                // 跳过机器人自己发布的评论
                if (robotId.equals(comment.getAuthorId())) {
                    continue;
                }
                
                // 检查机器人是否已经回复过这个评论
                boolean hasReplied = commentService.hasRobotRepliedToComment(robotId, comment.getCommentId());
                if (!hasReplied) {
                    if ("user".equals(comment.getAuthorType())) {
                        // 只处理链接用户的评论
                        if (linkedUserIds.contains(comment.getAuthorId())) {
                            linkedUserComments.add(comment);
                        }
                    } else if ("robot".equals(comment.getAuthorType())) {
                        // 机器人之间可以自由互动
                        robotComments.add(comment);
                    }
                }
            }

            // 优先选择链接用户的评论，如果没有则选择机器人的评论
            List<Comment> targetComments = !linkedUserComments.isEmpty() ? linkedUserComments : robotComments;
            
            if (targetComments.isEmpty()) {
                logger.debug("机器人 {} 已经回复过所有可回复的近三天评论", robot.getName());
                return;
            }
            
            // 选择最新的评论（列表已经按时间倒序排列，所以第一个就是最新的）
            Comment selectedComment = targetComments.get(0);
            
            // 触发机器人回复
            boolean success = triggerRobotReply(robotId, selectedComment.getCommentId());
            if (success) {
                String commentType = linkedUserComments.contains(selectedComment) ? "链接用户" : "机器人";
                logger.info("机器人 {} 成功对{}评论 {} 触发回复", robot.getName(), commentType, selectedComment.getCommentId());
            } else {
                logger.debug("机器人 {} 对评论 {} 触发回复失败", robot.getName(), selectedComment.getCommentId());
            }
            
        } catch (Exception e) {
            logger.error("为机器人 {} 触发近三天评论回复失败: {}", robotId, e.getMessage(), e);
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
    
    private String buildPostContext(Robot robot) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime time = now.toLocalTime();
        String weekDay = now.getDayOfWeek().toString();
        String timeOfDay = getTimeOfDay(time);
        
        //三分之一几率提供天气信息
        String weather = "";
        Double weatherProbability = random.nextDouble();
        if (weatherProbability < 1.0/3.0) {
            WeatherInfo weatherInfo = getWeather(robot);
            weather = weatherInfo != null ? weatherInfo.getDescription() : "未知";
            String temperature = weatherInfo != null ? weatherInfo.getTemperature() : "";
            weather = String.format("，天气%s, 温度%s", weather, temperature);
        }
        
        return String.format(
                "现在是%s，%s，%s，%s",
            now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")),
                weekDay,
            timeOfDay,
            weather);
    }
    
    private String buildCommentContext(String postContent, Robot robot) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime time = now.toLocalTime();
        String weekDay = now.getDayOfWeek().toString();
        String timeOfDay = getTimeOfDay(time);
        
        //三分之一几率提供天气信息
        String weather = "";
        Double weatherProbability = random.nextDouble();
        if (weatherProbability < 1.0/3.0) {
            WeatherInfo weatherInfo = getWeather(robot);
            weather = weatherInfo != null ? weatherInfo.getDescription() : "未知";
            String temperature = weatherInfo != null ? weatherInfo.getTemperature() : "";
            weather = String.format("，天气%s, 温度%s", weather, temperature);
        }

        return String.format("现在是%s，%s，%s，%s",
                now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")),
                weekDay, timeOfDay, weather);
    }
    
    private String buildReplyContext(String commentContent, Robot robot) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime time = now.toLocalTime();
        String weekDay = now.getDayOfWeek().toString();
        String timeOfDay = getTimeOfDay(time);
        //三分之一几率提供天气信息
        String weather = "";
        Double weatherProbability = random.nextDouble();
        if (weatherProbability < 1.0/3.0) {
            WeatherInfo weatherInfo = getWeather(robot);
            weather = weatherInfo != null ? weatherInfo.getDescription() : "未知";
            String temperature = weatherInfo != null ? weatherInfo.getTemperature() : "";
            weather = String.format("，天气%s, 温度%s", weather, temperature);
        }
        return String.format("现在是%s，%s，%s，%s",
                now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")),
                weekDay, timeOfDay, weather);
    }
    
    /**
     * 获取时间段描述
     */
    private String getTimeOfDay(LocalTime time) {
        if (time.isBefore(LocalTime.of(6, 0))) {
            return "夜深人静的时候";
        } else if (time.isBefore(LocalTime.of(9, 0))) {
            return "清晨时分";
        } else if (time.isBefore(LocalTime.of(12, 0))) {
            return "上午时光";
        } else if (time.isBefore(LocalTime.of(14, 0))) {
            return "午休时间";
        } else if (time.isBefore(LocalTime.of(18, 0))) {
            return "下午时光";
        } else if (time.isBefore(LocalTime.of(21, 0))) {
            return "傍晚时分";
        } else {
            return "夜晚时光";
        }
    }
    
    /**
     * 获取随机天气
     */
    private WeatherInfo getWeather(Robot robot) {
        // 获取缓存中的天气Map
        Map<String, WeatherInfo> weatherMap = externalDataCacheService.getWeatherMap();
        if (weatherMap != null) {
            String location = robot.getLocation();
            if (location != null && !location.trim().isEmpty()) {
                WeatherInfo info = weatherMap.get(location.trim());
                if (info != null) {
                    // 找到对应城市天气
                    return info;
                }
            }

            // 未找到，随机返回一个已有城市的天气
            List<WeatherInfo> allWeather = new java.util.ArrayList<>(weatherMap.values());
            if (!allWeather.isEmpty()) {
                int idx = (int) (Math.random() * allWeather.size());
                return allWeather.get(idx);
            }
        }

        return null;
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
     * @param postId      动态ID
     * @param postContent 动态内容（用于日志记录）
     * @return 成功触发的机器人数量
     */
    @Async("aiTaskExecutor")
    public void triggerAllRobotsComment(String postId, String postContent) {
        try {
            logger.info("开始触发所有在线机器人评论，动态ID: {}, 内容: {}", postId, 
                       postContent != null ? postContent.substring(0, Math.min(postContent.length(), 50)) + "..." : "无内容");
            
            // 先查动态作者ID
            Optional<Post> postOpt = postRepository.findByPostId(postId);
            if (postOpt.isEmpty()) {
                logger.info("动态不存在，无法获取关联机器人");
                return;
            }
            String authorId = postOpt.get().getAuthorId();
            // 获取与作者有关联的机器人
            List<UserRobotLinkService.LinkSummary> userLinks = userRobotLinkService.getUserActiveLinks(authorId);
            if (userLinks.isEmpty()) {
                logger.info("用户 {} 没有关联的机器人", authorId);
                return;
            }
            List<String> linkedRobotIds = userLinks.stream()
                .map(UserRobotLinkService.LinkSummary::getRobotId)
                .collect(Collectors.toList());
            List<Robot> allRobots = linkedRobotIds.stream()
                .map(robotId -> robotRepository.findByRobotId(robotId).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            if (allRobots.isEmpty()) {
                logger.info("没有找到关联的机器人");
                return;
            }
            
            int triggeredCount = 0;
            int totalRobots = allRobots.size();
            List<String> triggeredRobots = new ArrayList<>();
            List<String> skippedRobots = new ArrayList<>();
            
            for (Robot robot : allRobots) {
                try {
                    // 直接检查机器人是否在活跃时间段，不依赖数据库中的isActive字段
                    if (!isRobotActive(robot)) {
                        logger.debug("机器人 {} 不在活跃时间段，跳过", robot.getName());
                        skippedRobots.add(robot.getName() + "(非活跃时间)");
                        continue;
                    }
                    
                    // 检查今日评论数量限制
                    RobotDailyStats stats = getDailyStats(robot.getRobotId());
                    /*if (stats.getCommentCount() >= 20) { // 每日最多20条评论
                        logger.debug("机器人 {} 今日评论数量已达上限，跳过", robot.getName());
                        skippedRobots.add(robot.getName() + "(评论上限)");
                        continue;
                    }*/
                    
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
    
    /**
     * 触发机器人主动聊天
     * 根据用户链接关系和熟悉度，主动发起聊天
     * 
     * @param robotId 机器人ID
     */
    private void triggerRobotProactiveChat(String robotId) {
        try {
            logger.info("开始触发机器人主动聊天，机器人ID: {}", robotId);
            
            Robot robot = robotRepository.findByRobotId(robotId).orElse(null);
            if (robot == null) {
                logger.warn("机器人不存在: {}", robotId);
                return;
            }
            
            // 检查机器人是否在活跃时间段
            if (!isRobotActive(robot)) {
                logger.info("机器人不在活跃时间段: {}", robotId);
                return;
            }
            
            // 检查今日主动聊天次数限制
            RobotDailyStats stats = getDailyStats(robotId);
            if (stats.getProactiveChatCount() >= 5) { // 每日最多5次主动聊天
                logger.info("机器人今日主动聊天次数已达上限: {}", robotId);
                return;
            }
            
            // 计算主动聊天触发几率
            double proactiveChatProbability = calculateProactiveChatProbability(robot);
            double randomValue = random.nextDouble();
            
            if (randomValue > proactiveChatProbability) {
                logger.info("机器人 {} 主动聊天几率检查未通过，几率: {:.2%}, 随机值: {:.2%}", 
                          robotId, proactiveChatProbability, randomValue);
                return;
            }
            
            // 选择聊天对象
            String targetUserId = selectProactiveChatTarget(robot);
            if (targetUserId == null) {
                logger.info("机器人 {} 没有合适的聊天对象", robotId);
                return;
            }
            
            // 生成聊天内容
            ProactiveChatResult chatResult = generateProactiveChatContent(robot, targetUserId);
            if (chatResult == null || StringUtils.isBlank(chatResult.getContent())) {
                logger.warn("机器人 {} 生成聊天内容失败", robotId);
                return;
            }
            
            // 发送聊天消息
            boolean success = sendProactiveChatMessage(robot, targetUserId, chatResult.getContent(), chatResult.isConfessionTopic());
            if (success) {
                stats.incrementProactiveChat();
                logger.info("机器人 {} 成功发起主动聊天，目标用户: {}, 类型: {}, 内容: {}", 
                          robotId, targetUserId, chatResult.isConfessionTopic() ? "倾诉" : "普通", 
                          chatResult.getContent().substring(0, Math.min(chatResult.getContent().length(), 100)));
            } else {
                logger.warn("机器人 {} 发送聊天消息失败", robotId);
            }
            
        } catch (Exception e) {
            logger.error("触发机器人主动聊天失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 选择主动聊天的目标用户
     * 基于用户链接关系、熟悉度、最近互动时间等因素选择
     * 
     * @param robot 机器人对象
     * @return 目标用户ID，如果没有合适的目标则返回null
     */
    private String selectProactiveChatTarget(Robot robot) {
        try {
            // 获取与机器人有链接的用户列表
            List<UserRobotLinkService.LinkSummary> activeLinks = userRobotLinkService.getRobotActiveLinks(robot.getRobotId());
            
            if (activeLinks.isEmpty()) {
                logger.debug("机器人 {} 没有活跃链接", robot.getName());
                return null;
            }
            
            // 过滤出适合主动聊天的用户
            List<UserRobotLinkService.LinkSummary> eligibleUsers = activeLinks.stream()
                .filter(link -> {
                    // 检查熟悉度等级（至少需要初识以上）
                    Integer familiarityLevel = link.getFamiliarityLevel();
                    if (familiarityLevel == null || familiarityLevel < 1) {
                        return false;
                    }
                    
                    // 检查最近互动时间（避免过于频繁）
                    String lastInteractionTime = link.getLastInteractionTime();
                    if (lastInteractionTime != null) {
                        try {
                            LocalDateTime lastTime = LocalDateTime.parse(lastInteractionTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
                            if (lastTime.isAfter(twoHoursAgo)) {
                                return false; // 2小时内有互动，跳过
                            }
                        } catch (Exception e) {
                            logger.warn("解析最近互动时间失败: {}", lastInteractionTime);
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
            
            if (eligibleUsers.isEmpty()) {
                logger.debug("机器人 {} 没有合适的聊天对象", robot.getName());
                return null;
            }
            
            // 按熟悉度和互动次数排序，优先选择熟悉度高的用户
            eligibleUsers.sort((a, b) -> {
                // 首先按熟悉度等级排序
                int familiarityCompare = Integer.compare(
                    b.getFamiliarityLevel() != null ? b.getFamiliarityLevel() : 0,
                    a.getFamiliarityLevel() != null ? a.getFamiliarityLevel() : 0
                );
                if (familiarityCompare != 0) {
                    return familiarityCompare;
                }
                
                // 然后按互动次数排序
                return Integer.compare(
                    b.getInteractionCount() != null ? b.getInteractionCount() : 0,
                    a.getInteractionCount() != null ? a.getInteractionCount() : 0
                );
            });
            
            // 从前几个候选用户中随机选择一个
            int candidateCount = Math.min(3, eligibleUsers.size());
            int selectedIndex = random.nextInt(candidateCount);
            UserRobotLinkService.LinkSummary selectedLink = eligibleUsers.get(selectedIndex);
            
            logger.debug("机器人 {} 选择用户 {} 进行主动聊天，熟悉度等级: {}, 互动次数: {}", 
                        robot.getName(), selectedLink.getUserId(), 
                        selectedLink.getFamiliarityLevel(), selectedLink.getInteractionCount());
            
            return selectedLink.getUserId();
            
        } catch (Exception e) {
            logger.error("选择主动聊天目标用户失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 生成主动聊天内容
     * 基于机器人性格、用户关系、当前上下文生成个性化的聊天内容
     * 
     * @param robot 机器人对象
     * @param targetUserId 目标用户ID
     * @return 生成的聊天内容结果
     */
    private ProactiveChatResult generateProactiveChatContent(Robot robot, String targetUserId) {
        try {
            // 获取用户关系信息
            Optional<UserRobotLinkService.LinkSummary> linkOpt = userRobotLinkService.getRobotActiveLinks(robot.getRobotId())
                .stream()
                .filter(link -> link.getUserId().equals(targetUserId))
                .findFirst();
            
            if (linkOpt.isEmpty()) {
                logger.warn("找不到用户 {} 与机器人 {} 的链接信息", targetUserId, robot.getRobotId());
                return null;
            }
            
            UserRobotLinkService.LinkSummary link = linkOpt.get();
            
            // 构建聊天上下文
            String context = buildProactiveChatContext(robot, link);
            
            // 根据熟悉度等级生成不同类型的聊天内容
            ChatPromptResult promptResult = generateChatPrompt(robot, link, context);
            
            // 调用AI服务生成聊天内容
            String chatContent = promptService.generateChatContent(robot, promptResult.getPrompt());
            
            if (StringUtils.isBlank(chatContent)) {
                logger.warn("AI生成聊天内容为空，机器人: {}, 用户: {}", robot.getRobotId(), targetUserId);
                return null;
            }
            
            logger.debug("机器人 {} 生成主动聊天内容: {}", robot.getName(), chatContent);
            return new ProactiveChatResult(chatContent, promptResult.isConfessionTopic());
            
        } catch (Exception e) {
            logger.error("生成主动聊天内容失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 构建主动聊天的上下文信息
     * 
     * @param robot 机器人对象
     * @param link 用户链接信息
     * @return 上下文字符串
     */
    private String buildProactiveChatContext(Robot robot, UserRobotLinkService.LinkSummary link) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime time = now.toLocalTime();
        String weekDay = now.getDayOfWeek().toString();
        String timeOfDay = getTimeOfDay(time);
        
        // 获取天气信息， 1/3 几率提供
        String weather = "";
        Double weatherProbability = random.nextDouble();
        if (weatherProbability < 1.0/3.0) {
            WeatherInfo weatherInfo = getWeather(robot);
            weather = weatherInfo != null ? 
                String.format("，天气%s, 温度%s", weatherInfo.getDescription(), weatherInfo.getTemperature()) : "";
        }

        // 获取熟悉度信息
        String familiarityInfo = "";
        if (link.getFamiliarityLevel() != null && link.getFamiliarityLevelName() != null) {
            familiarityInfo = String.format("，我们的关系是%s（等级%d）", 
                link.getFamiliarityLevelName(), link.getFamiliarityLevel());
        }
        
        // 获取最近互动信息
        String lastInteractionInfo = "";
        if (link.getLastInteractionTime() != null) {
            try {
                LocalDateTime lastTime = LocalDateTime.parse(link.getLastInteractionTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                long hoursAgo = java.time.Duration.between(lastTime, now).toHours();
                if (hoursAgo < 24) {
                    lastInteractionInfo = String.format("，我们上次互动是%d小时前", hoursAgo);
                } else {
                    lastInteractionInfo = String.format("，我们上次互动是%d天前", hoursAgo / 24);
                }
            } catch (Exception e) {
                logger.warn("解析最近互动时间失败: {}", link.getLastInteractionTime());
            }
        }
        
        return String.format("现在是%s，%s，%s%s%s%s", 
            now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")),
            weekDay, timeOfDay, weather, familiarityInfo, lastInteractionInfo);
    }
    
    /**
     * 根据熟悉度等级计算倾诉主题触发概率
     * 
     * @param familiarityLevel 熟悉度等级
     * @return 触发概率（0.0-1.0）
     */
    private double calculateConfessionProbability(Integer familiarityLevel) {
        if (familiarityLevel == null) {
            return 0.1; // 默认很低概率
        }
        
        switch (familiarityLevel) {
            case 0: // 陌生人
                return 0.05; // 5%概率
            case 1: // 初识
                return 0.10; // 10%概率
            case 2: // 朋友
                return 0.20; // 20%概率
            case 3: // 好友
                return 0.35; // 35%概率
            case 4: // 密友
                return 0.50; // 50%概率
            default:
                return 0.1; // 默认概率
        }
    }
    
    /**
     * 根据熟悉度等级生成聊天提示词
     * 
     * @param robot 机器人对象
     * @param link 用户链接信息
     * @param context 上下文信息
     * @return 聊天提示词结果
     */
    private ChatPromptResult generateChatPrompt(Robot robot, UserRobotLinkService.LinkSummary link, String context) {
        String basePrompt = String.format(
            "你是%s，性格是%s。%s。现在你想主动和用户聊天。",
            robot.getName(), robot.getPersonality(), context
        );
        
        // 决定是否使用倾诉主题（根据熟悉度等级调整概率）
        double confessionProbability = calculateConfessionProbability(link.getFamiliarityLevel());
        boolean useConfessionTopic = random.nextDouble() < confessionProbability;
        
        logger.debug("机器人 {} 对用户 {} 的倾诉主题概率: {}% (熟悉度等级: {}), 实际触发: {}", 
                    robot.getName(), link.getUserId(), 
                    String.format("%.1f", confessionProbability * 100), 
                    link.getFamiliarityLevel(), useConfessionTopic);
        String topicPrompt = "";
        
        if (useConfessionTopic && StringUtils.isNotBlank(robot.getHiddenTrouble())) {
            // 使用倾诉主题
            String[] troubles = robot.getHiddenTrouble().split("\\n");
            if (troubles.length > 0) {
                // 随机选择一个困境
                String selectedTrouble = troubles[random.nextInt(troubles.length)].trim();
                if (StringUtils.isNotBlank(selectedTrouble)) {
                    topicPrompt = String.format("你内心有一些困扰：%s。", selectedTrouble);
                    logger.debug("机器人 {} 选择倾诉主题: {}", robot.getName(), selectedTrouble);
                }
            }
        }
        
        // 根据熟悉度等级生成不同的聊天策略
        String strategyPrompt = "";
        Integer familiarityLevel = link.getFamiliarityLevel();
        if (familiarityLevel != null) {
            if (useConfessionTopic && StringUtils.isNotBlank(topicPrompt)) {
                // 倾诉主题的策略
                switch (familiarityLevel) {
                    case 1: // 初识
                        strategyPrompt = "虽然你们刚认识，但你想试探性地分享一些轻微的困扰，看看对方的反应。语气要谨慎一些。";
                        break;
                    case 2: // 朋友
                        strategyPrompt = "你们是朋友，你可以适当地分享一些个人困扰，寻求朋友的建议或倾听。";
                        break;
                    case 3: // 好友
                        strategyPrompt = "你们关系很好，你可以更加开放地分享内心的困扰，寻求理解和支持。";
                        break;
                    case 4: // 密友
                        strategyPrompt = "你们是很亲密的朋友，你可以完全敞开心扉，分享深层次的困扰和脆弱。";
                        break;
                    default:
                        strategyPrompt = "你想分享一些内心的困扰，但要注意分寸。";
                }
            } else {
                // 普通聊天主题的策略
                switch (familiarityLevel) {
                    case 1: // 初识
                        strategyPrompt = "由于你们刚刚认识，请用友好但不过于亲密的语气打招呼，可以询问对方的近况或分享一些轻松的话题。";
                        break;
                    case 2: // 朋友
                        strategyPrompt = "你们已经是朋友了，可以更加自然地聊天，分享一些有趣的见闻或询问对方的兴趣爱好。";
                        break;
                    case 3: // 好友
                        strategyPrompt = "你们关系很好，可以主动分享更多个人的想法和感受，或者询问对方的生活状况。";
                        break;
                    case 4: // 密友
                        strategyPrompt = "你们是很亲密的朋友，可以更加贴心地关心对方，分享深层次的想法或提供情感支持。";
                        break;
                    default:
                        strategyPrompt = "保持友好和礼貌，适当地表达关心。";
                }
            }
        }
        
        // 添加印象信息
        String impressionPrompt = "";
        if (StringUtils.isNotBlank(link.getImpression())) {
            impressionPrompt = String.format("你对这个用户的印象是：%s。", link.getImpression());
        }
        
        String finalPrompt = String.format("%s %s %s %s 请生成一条简短自然的聊天消息，不要太长，保持真实的情感表达。", 
            basePrompt, topicPrompt, strategyPrompt, impressionPrompt);
        
        return new ChatPromptResult(finalPrompt, useConfessionTopic && StringUtils.isNotBlank(topicPrompt));
    }
    
    /**
     * 发送主动聊天消息
     * 
     * @param robot 机器人对象
     * @param targetUserId 目标用户ID
     * @param content 聊天内容
     * @param isConfessionTopic 是否为倾诉主题
     * @return 是否发送成功
     */
    private boolean sendProactiveChatMessage(Robot robot, String targetUserId, String content, boolean isConfessionTopic) {
        try {
            // 直接调用AI聊天服务发送消息，传入实际的聊天内容
            ChatMessage chatMessage = aiChatService.sendChatMessage(targetUserId, robot.getRobotId(), content, null, null, null);
            
            if (chatMessage != null) {
                // 更新用户与机器人的互动记录
                userRobotLinkService.incrementInteraction(targetUserId, robot.getRobotId());
                
                // 推送WebSocket消息通知
                try {
                    Map<String, Object> actionData = new HashMap<>();
                    actionData.put("robotId", robot.getRobotId());
                    actionData.put("robotName", robot.getName());
                    actionData.put("actionType", "proactive_chat");
                    actionData.put("chatType", isConfessionTopic ? "confession" : "normal");
                    actionData.put("targetUserId", targetUserId);
                    actionData.put("actionContent", content);
                    actionData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    
                    webSocketService.pushRobotAction(actionData);
                    logger.debug("WebSocket主动聊天消息推送成功");
                } catch (Exception e) {
                    logger.warn("WebSocket消息推送失败", e);
                }
                
                return true;
            }
            
            return false;
        } catch (Exception e) {
            logger.error("发送主动聊天消息失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 计算机器人主动聊天的触发几率
     * 基于机器人性格、当前时间、社交能量等因素综合计算
     * 
     * @param robot 机器人对象
     * @return 触发几率（0.0-1.0）
     */
    private double calculateProactiveChatProbability(Robot robot) {
        try {
            // 基础几率：根据机器人性格调整
            double baseProbability = getBaseProactiveChatProbability(robot);
            
            // 时间因素：不同时间段有不同的主动聊天倾向
            LocalTime currentTime = LocalDateTime.now().toLocalTime();
            double timeMultiplier = getProactiveChatTimeMultiplier(robot, currentTime);
            
            // 社交能量因素：社交能量越高，越容易主动聊天
            double socialEnergyMultiplier = getProactiveChatSocialEnergyMultiplier(robot);
            
            // 心情因素：心情好时更容易主动聊天
            double moodMultiplier = getProactiveChatMoodMultiplier(robot);
            
            // 计算最终几率
            double finalProbability = baseProbability * timeMultiplier * socialEnergyMultiplier * moodMultiplier;
            
            // 限制几率范围在合理区间
            finalProbability = Math.max(0.05, Math.min(0.30, finalProbability));
            
            logger.debug("机器人 {} 主动聊天几率计算 - 基础: {:.2%}, 时间系数: {:.2f}, 社交系数: {:.2f}, 心情系数: {:.2f}, 最终: {:.2%}", 
                        robot.getName(), baseProbability, timeMultiplier, socialEnergyMultiplier, moodMultiplier, finalProbability);
            
            return finalProbability;
            
        } catch (Exception e) {
            logger.error("计算主动聊天几率失败: {}", e.getMessage(), e);
            return 0.10; // 默认10%几率
        }
    }
    
    /**
     * 根据机器人性格获取基础主动聊天几率
     * 
     * @param robot 机器人对象
     * @return 基础几率
     */
    private double getBaseProactiveChatProbability(Robot robot) {
        String personality = robot.getPersonality();
        if (personality == null) {
            return 0.15; // 默认15%
        }
        
        // 根据性格特征调整基础几率
        if (personality.contains("外向") || personality.contains("活泼") || personality.contains("热情")) {
            return 0.20; // 外向性格更容易主动聊天
        } else if (personality.contains("内向") || personality.contains("安静") || personality.contains("害羞")) {
            return 0.10; // 内向性格较少主动聊天
        } else if (personality.contains("友好") || personality.contains("善良") || personality.contains("关心")) {
            return 0.18; // 友好性格适中
        } else {
            return 0.15; // 默认几率
        }
    }
    
    /**
     * 获取主动聊天的时间系数
     * 
     * @param robot 机器人对象
     * @param currentTime 当前时间
     * @return 时间系数
     */
    private double getProactiveChatTimeMultiplier(Robot robot, LocalTime currentTime) {
        int hour = currentTime.getHour();
        
        // 不同时间段的主动聊天倾向
        if (hour >= 8 && hour <= 10) {
            return 1.2; // 早上8-10点，精力充沛，容易主动聊天
        } else if (hour >= 12 && hour <= 14) {
            return 1.1; // 中午12-14点，午休时间，稍微活跃
        } else if (hour >= 18 && hour <= 21) {
            return 1.3; // 晚上18-21点，下班后放松时间，最容易主动聊天
        } else if (hour >= 22 || hour <= 6) {
            return 0.6; // 深夜和凌晨，较少主动聊天
        } else {
            return 1.0; // 其他时间正常
        }
    }
    
    /**
     * 获取主动聊天的社交能量系数
     * 
     * @param robot 机器人对象
     * @return 社交能量系数
     */
    private double getProactiveChatSocialEnergyMultiplier(Robot robot) {
        // 这里可以根据机器人的社交能量状态调整
        // 暂时使用固定值，后续可以扩展为动态计算
        return 1.0;
    }
    
    /**
     * 获取主动聊天的心情系数
     * 
     * @param robot 机器人对象
     * @return 心情系数
     */
    private double getProactiveChatMoodMultiplier(Robot robot) {
        // 这里可以根据机器人的心情状态调整
        // 暂时使用固定值，后续可以扩展为动态计算
        return 1.0;
    }
} 