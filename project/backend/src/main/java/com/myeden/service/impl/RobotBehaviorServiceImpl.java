package com.myeden.service.impl;

import com.myeden.entity.Robot;
import com.myeden.entity.Post;
import com.myeden.entity.Comment;
import com.myeden.repository.RobotRepository;
import com.myeden.repository.PostRepository;
import com.myeden.repository.CommentRepository;
import com.myeden.service.*;
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

        public void incrementPost() {
            postCount++;
        }

        public void incrementComment() {
            commentCount++;
        }

        public void incrementReply() {
            replyCount++;
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

        public LocalDateTime getLastReset() {
            return lastReset;
        }

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
        if (robot == null || !robot.getIsActive()) {
            logger.warn("机器人不存在或未激活: {}", robotId);
            return null;
        }
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
            String context = buildPostContext();
            String content = promptService.generatePostContent(robot, context);
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
            // 获取动态内容（先查post，后判断）
            PostService.PostDetail postDetail = postService.getPostDetail(postId);
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
            List<PostService.LikeDetail> likes = postService.getPostLikes(postId).getLikes();
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
            /*
             * if (stats.getCommentCount() >= 20) { // 每日最多20条评论
             * logger.info("机器人评论超限: 每天最多20次 {}", robot.getRobotId());
             * return false;
             * }
             */

            String postContent = postDetail.getContent();
            String context = buildCommentContext(postContent);
            String content = promptService.generateCommentContent(robot, postDetail, context);
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
            CommentService.CommentDetail commentDetail = commentService.getCommentDetail(commentId);
            PostService.PostDetail postDetail = postService.getPostDetail(commentDetail.getPostId());
            boolean isRobot = "robot".equals(commentDetail.getAuthorType());
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
            String context = buildReplyContext(commentContent);

            // 生成回复内容和内心活动
            String content = promptService.generateReplyContent(robot, commentDetail, postDetail, context);
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
                    baseProbability = 0.015;
                    break;
                case "comment":
                    baseProbability = 0.1;
                    if (!isRobot) {
                        baseProbability = 0.75;
                    }
                    break;
                case "reply":
                    baseProbability = 0.1;
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
                    if (randomValue < 0.25) {
                        triggerRobotPost(robot.getRobotId());
                    } else if (randomValue < 0.5) {
                        // 随机选择一个近三天的动态进行评论
                        triggerRobotCommentOnRecentPosts(robot.getRobotId());
                    } else if (randomValue < 0.75) {
                        // 随机选择一个近三天的评论进行回复
                        triggerRobotReplyOnRecentComments(robot.getRobotId());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("定时机器人行为执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 为指定机器人触发对近三天帖子的评论
     * 优先选择最新的人类文章进行回复
     * 
     * @param robotId 机器人ID
     */
    private void triggerRobotCommentOnRecentPosts(String robotId) {
        try {
            Robot robot = robotRepository.findByRobotId(robotId).orElse(null);
            if (robot == null || !robot.getIsActive()) {
                return;
            }

            // 检查是否在活跃时间段
            if (!isRobotActive(robot)) {
                return;
            }

            // 检查今日评论数量限制
            RobotDailyStats stats = getDailyStats(robotId);
            /*
             * if (stats.getCommentCount() >= 20) { // 每日最多20条评论
             * return;
             * }
             */

            // 获取近三天的帖子，按时间倒序排列（最新的在前）
            LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(1);
            List<Post> recentPosts = postRepository
                    .findByCreatedAtAfterAndIsDeletedFalseOrderByCreatedAtDesc(threeDaysAgo);

            if (recentPosts.isEmpty()) {
                logger.debug("机器人 {} 没有找到近三天的帖子", robot.getName());
                return;
            }

            // 分离人类用户和机器人的帖子
            List<Post> humanPosts = new ArrayList<>();
            List<Post> robotPosts = new ArrayList<>();

            for (Post post : recentPosts) {
                // 跳过机器人自己发布的帖子
                if (robotId.equals(post.getAuthorId())) {
                    continue;
                }

                // 检查机器人是否已经评论过这个帖子
                boolean hasCommented = commentService.hasRobotCommentedOnPost(robotId, post.getPostId());
                if (!hasCommented) {
                    // 根据authorType判断是否为人类用户
                    if ("user".equals(post.getAuthorType())) {
                        humanPosts.add(post);
                    } else if ("robot".equals(post.getAuthorType())) {
                        robotPosts.add(post);
                    }
                }
            }

            // 优先选择人类用户的帖子，如果没有则选择机器人的帖子
            List<Post> targetPosts = !humanPosts.isEmpty() ? humanPosts : robotPosts;

            if (targetPosts.isEmpty()) {
                logger.debug("机器人 {} 已经评论过所有近三天的帖子", robot.getName());
                return;
            }

            // 选择最新的帖子（列表已经按时间倒序排列，所以第一个就是最新的）
            Post selectedPost = targetPosts.get(0);

            // 触发机器人评论
            boolean success = triggerRobotComment(robotId, selectedPost.getPostId());
            if (success) {
                String postType = humanPosts.contains(selectedPost) ? "人类用户" : "机器人";
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
     * 优先回复最新的人类用户评论
     * 
     * @param robotId 机器人ID
     */
    private void triggerRobotReplyOnRecentComments(String robotId) {
        try {
            Robot robot = robotRepository.findByRobotId(robotId).orElse(null);
            if (robot == null || !robot.getIsActive()) {
                return;
            }

            // 检查是否在活跃时间段
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

            // 获取近三天的评论，按时间倒序排列（最新的在前）
            LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(1);
            List<Comment> recentComments = commentService.findRecentComments(threeDaysAgo);

            if (recentComments.isEmpty()) {
                logger.debug("机器人 {} 没有找到近三天的评论", robot.getName());
                return;
            }

            // 分离人类用户和机器人的评论
            List<Comment> humanComments = new ArrayList<>();
            List<Comment> robotComments = new ArrayList<>();

            for (Comment comment : recentComments) {
                // 跳过机器人自己发布的评论
                if (robotId.equals(comment.getAuthorId())) {
                    continue;
                }

                // 检查机器人是否已经回复过这个评论
                boolean hasReplied = commentService.hasRobotRepliedToComment(robotId, comment.getCommentId());
                if (!hasReplied) {
                    // 根据authorType判断是否为人类用户
                    if ("user".equals(comment.getAuthorType())) {
                        humanComments.add(comment);
                    } else if ("robot".equals(comment.getAuthorType())) {
                        robotComments.add(comment);
                    }
                }
            }

            // 优先选择人类用户的评论，如果没有则选择机器人的评论
            List<Comment> targetComments = !humanComments.isEmpty() ? humanComments : robotComments;

            if (targetComments.isEmpty()) {
                logger.debug("机器人 {} 已经回复过所有近三天的评论", robot.getName());
                return;
            }

            // 选择最新的评论（列表已经按时间倒序排列，所以第一个就是最新的）
            Comment selectedComment = targetComments.get(0);

            // 触发机器人回复
            boolean success = triggerRobotReply(robotId, selectedComment.getCommentId());
            if (success) {
                String commentType = humanComments.contains(selectedComment) ? "人类用户" : "机器人";
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

    private String buildPostContext() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime time = now.toLocalTime();
        String weekDay = now.getDayOfWeek().toString();
        String timeOfDay = getTimeOfDay(time);
        String weather = getWeather();
        String mood = getMood();
        String activity = getRandomActivity(timeOfDay);

        return String.format(
                "现在是%s，%s，%s，%s, %s, %s",
                now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")),
                weekDay,
                timeOfDay,
                weather,
                mood,
                activity);
    }

    private String buildCommentContext(String postContent) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime time = now.toLocalTime();
        String weekDay = now.getDayOfWeek().toString();
        String timeOfDay = getTimeOfDay(time);
        String weather = getWeather();
        String mood = getMood();
        String activity = getRandomActivity(timeOfDay);

        return String.format("现在是%s，%s，%s，%s, 你感到 %s, 你正在 %s, 你看到一条朋友圈动态：%s",
                now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")),
                weekDay, timeOfDay, weather, mood,
                activity, postContent);
    }

    private String buildReplyContext(String commentContent) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime time = now.toLocalTime();
        String weekDay = now.getDayOfWeek().toString();
        String timeOfDay = getTimeOfDay(time);
        String weather = getWeather();
        String mood = getMood();
        String activity = getRandomActivity(timeOfDay);

        return String.format("现在是%s，%s，%s，%s, 你感到 %s, 你正在 %s,你看到有人评论了： %s",
                now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")),
                weekDay, timeOfDay, weather, mood,
                activity, commentContent);
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
    private String getWeather() {
        String[] weathers = {
                "阳光明媚，心情舒畅",
                "微风轻拂，很舒服",
                "阴天多云，适合思考",
                "小雨绵绵，很有诗意",
                "天气不错，适合出门",
                "今天天气很好呢",
                "阳光正好，微风不燥",
                "天气有点阴，但心情不错"
        };
        return weathers[random.nextInt(weathers.length)];
    }

    /**
     * 获取随机心情
     */
    private String getMood() {
        String[] moods = {
                "心情很愉快",
                "感觉还不错",
                "心情平静",
                "略显焦虑",
                "略显疲惫",
                "略显无聊",
                "略显迷茫",
                "略显伤心",
                "略显恐惧",
                "略显愤怒",
                "略显疯狂"
        };
        return moods[random.nextInt(moods.length)];
    }

    /**
     * 获取随机活动
     */
    private String getRandomActivity(String timeOfDay) {
        return "看手机朋友圈";
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
}