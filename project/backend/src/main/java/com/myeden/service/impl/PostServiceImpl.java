package com.myeden.service.impl;

import com.myeden.entity.Post;
import com.myeden.entity.User;
import com.myeden.entity.Robot;
import com.myeden.entity.PostLike;
import com.myeden.repository.PostRepository;
import com.myeden.repository.UserRepository;
import com.myeden.repository.RobotRepository;
import com.myeden.repository.PostLikeRepository;
import com.myeden.service.PostService;
import com.myeden.service.FileService;
import com.myeden.service.WebSocketService;
import com.myeden.service.RobotBehaviorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 动态管理服务实现类
 * 
 * 功能说明：
 * - 实现动态发布、查询、删除功能
 * - 支持图片上传和处理
 * - 管理动态的点赞和评论统计
 * - 支持分页查询和排序
 * - 集成WebSocket实时消息推送
 * - 完善点赞功能，防止重复点赞
 * - 自动触发AI机器人评论功能
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
public class PostServiceImpl implements PostService {
    
    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private PostLikeRepository postLikeRepository;
    
    @Autowired
    private FileService fileService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private RobotBehaviorService robotBehaviorService;
    
    @Override
    public PostResult createPost(String authorId, String authorType, String content, List<MultipartFile> images) {
        try {
            logger.info("开始创建动态，作者ID: {}, 作者类型: {}", authorId, authorType);
            
            // 验证参数
            if (!StringUtils.hasText(content)) {
                throw new IllegalArgumentException("动态内容不能为空");
            }
            
            if (!StringUtils.hasText(authorId)) {
                throw new IllegalArgumentException("作者ID不能为空");
            }
            
            if (!StringUtils.hasText(authorType)) {
                throw new IllegalArgumentException("作者类型不能为空");
            }
            
            // 验证作者是否存在
            String authorName = "";
            String authorAvatar = "";
            
            if ("user".equals(authorType)) {
                Optional<User> userOpt = userRepository.findByUserId(authorId);
                if (userOpt.isEmpty()) {
                    throw new IllegalArgumentException("用户不存在");
                }
                User user = userOpt.get();
                authorName = user.getNickname();
                authorAvatar = user.getAvatar();
            } else if ("robot".equals(authorType)) {
                Optional<Robot> robotOpt = robotRepository.findByRobotId(authorId);
                if (robotOpt.isEmpty()) {
                    throw new IllegalArgumentException("机器人不存在");
                }
                Robot robot = robotOpt.get();
                authorName = robot.getName();
                authorAvatar = robot.getAvatar();
            } else {
                throw new IllegalArgumentException("无效的作者类型");
            }
            
            // 处理图片上传
            List<String> imageUrls = new ArrayList<>();
            if (images != null && !images.isEmpty()) {
                for (MultipartFile image : images) {
                    if (image != null && !image.isEmpty()) {
                        String imageUrl = fileService.uploadImage(image);
                        imageUrls.add(imageUrl);
                    }
                }
            }
            
            // 创建动态实体
            Post post = new Post();
            post.setPostId(generatePostId());
            post.setAuthorId(authorId);
            post.setAuthorType(authorType);
            post.setContent(content);
            post.setImages(imageUrls);
            post.setLikeCount(0);
            post.setCommentCount(0);
            post.setIsDeleted(false);
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            
            // 保存到数据库
            Post savedPost = postRepository.save(post);
            
            logger.info("动态创建成功，动态ID: {}", savedPost.getPostId());
            
            // 推送WebSocket消息
            try {
                Map<String, Object> postData = new HashMap<>();
                postData.put("postId", savedPost.getPostId());
                postData.put("authorId", savedPost.getAuthorId());
                postData.put("authorType", savedPost.getAuthorType());
                postData.put("authorName", authorName);
                postData.put("authorAvatar", authorAvatar);
                postData.put("content", savedPost.getContent());
                postData.put("images", savedPost.getImages());
                postData.put("createdAt", savedPost.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                
                webSocketService.pushPostUpdate(postData);
                logger.info("WebSocket动态更新消息推送成功");
            } catch (Exception e) {
                logger.warn("WebSocket消息推送失败", e);
            }
            
            // 触发AI机器人评论（异步执行，避免阻塞主流程）
            triggerRobotCommentsAsync(savedPost.getPostId(), content);
            
            return new PostResult(
                savedPost.getPostId(),
                savedPost.getContent(),
                savedPost.getImages(),
                savedPost.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
        } catch (Exception e) {
            logger.error("创建动态失败", e);
            throw e;
        }
    }
    
    @Override
    public PostListResult getPostList(int page, int size, String authorType) {
        try {
            logger.info("获取动态列表，页码: {}, 大小: {}, 作者类型: {}", page, size, authorType);
            
            // 创建分页请求
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            
            // 查询动态
            Page<Post> postPage;
            if (StringUtils.hasText(authorType)) {
                postPage = postRepository.findByAuthorTypeAndIsDeletedFalse(authorType, pageable);
            } else {
                postPage = postRepository.findByIsDeletedFalse(pageable);
            }
            
            // 转换为摘要信息
            List<PostSummary> postSummaries = postPage.getContent().stream()
                .map(this::convertToPostSummary)
                .collect(Collectors.toList());
            
            logger.info("获取动态列表成功，总数: {}", postPage.getTotalElements());
            
            return new PostListResult(
                postSummaries,
                (int) postPage.getTotalElements(),
                page,
                size
            );
            
        } catch (Exception e) {
            logger.error("获取动态列表失败", e);
            throw e;
        }
    }
    
    @Override
    public PostDetail getPostDetail(String postId) {
        try {
            logger.info("获取动态详情，动态ID: {}", postId);
            
            // 查询动态
            Optional<Post> postOpt = postRepository.findByPostIdAndIsDeletedFalse(postId);
            if (postOpt.isEmpty()) {
                throw new IllegalArgumentException("动态不存在");
            }
            
            Post post = postOpt.get();
            
            // 获取作者信息
            String authorName = "";
            String authorAvatar = "";
            
            if ("user".equals(post.getAuthorType())) {
                Optional<User> userOpt = userRepository.findByUserId(post.getAuthorId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    authorName = user.getNickname();
                    authorAvatar = user.getAvatar();
                }
            } else if ("robot".equals(post.getAuthorType())) {
                Optional<Robot> robotOpt = robotRepository.findByRobotId(post.getAuthorId());
                if (robotOpt.isPresent()) {
                    Robot robot = robotOpt.get();
                    authorName = robot.getName();
                    authorAvatar = robot.getAvatar();
                }
            }
            
            // 获取点赞用户列表和当前用户是否点赞状态
            List<PostLike> postLikes = postLikeRepository.findByPostId(postId);
            List<String> likedUsers = postLikes.stream()
                .map(PostLike::getUserId)
                .collect(Collectors.toList());
            boolean isLiked = false; // 这里需要传入当前用户ID来判断，暂时设为false
            
            logger.info("获取动态详情成功");
            
            return new PostDetail(
                post.getPostId(),
                post.getAuthorId(),
                post.getAuthorType(),
                authorName,
                authorAvatar,
                post.getContent(),
                post.getImages(),
                post.getLikeCount(),
                post.getCommentCount(),
                isLiked,
                likedUsers,
                post.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                post.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
        } catch (Exception e) {
            logger.error("获取动态详情失败", e);
            throw e;
        }
    }
    
    @Override
    public boolean deletePost(String postId, String authorId) {
        try {
            logger.info("删除动态，动态ID: {}, 作者ID: {}", postId, authorId);
            
            // 查询动态
            Optional<Post> postOpt = postRepository.findByPostIdAndIsDeletedFalse(postId);
            if (postOpt.isEmpty()) {
                throw new IllegalArgumentException("动态不存在");
            }
            
            Post post = postOpt.get();
            
            // 验证权限
            if (!post.getAuthorId().equals(authorId)) {
                throw new IllegalArgumentException("无权限删除此动态");
            }
            
            // 软删除
            post.setIsDeleted(true);
            post.setUpdatedAt(LocalDateTime.now());
            postRepository.save(post);
            
            logger.info("动态删除成功");
            return true;
            
        } catch (Exception e) {
            logger.error("删除动态失败", e);
            throw e;
        }
    }
    
    @Override
    public boolean likePost(String postId, String userId) {
        try {
            logger.info("点赞动态，动态ID: {}, 用户ID: {}", postId, userId);
            
            // 查询动态
            Optional<Post> postOpt = postRepository.findByPostIdAndIsDeletedFalse(postId);
            if (postOpt.isEmpty()) {
                throw new IllegalArgumentException("动态不存在");
            }
            
            Post post = postOpt.get();
            
            // 检查用户是否已经点赞
            Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId);
            if (existingLike.isPresent()) {
                logger.warn("用户已经点赞过此动态: postId={}, userId={}", postId, userId);
                return false;
            }
            
            // 创建点赞记录
            PostLike postLike = PostLike.builder()
                .postLikeId(generatePostLikeId())
                .postId(postId)
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            postLikeRepository.save(postLike);
            
            // 增加点赞数
            post.setLikeCount(post.getLikeCount() + 1);
            post.setUpdatedAt(LocalDateTime.now());
            postRepository.save(post);
            
            logger.info("动态点赞成功");
            return true;
            
        } catch (Exception e) {
            logger.error("点赞动态失败", e);
            throw e;
        }
    }
    
    @Override
    public boolean unlikePost(String postId, String userId) {
        try {
            logger.info("取消点赞动态，动态ID: {}, 用户ID: {}", postId, userId);
            
            // 查询动态
            Optional<Post> postOpt = postRepository.findByPostIdAndIsDeletedFalse(postId);
            if (postOpt.isEmpty()) {
                throw new IllegalArgumentException("动态不存在");
            }
            
            Post post = postOpt.get();
            
            // 检查用户是否已经点赞
            Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId);
            if (existingLike.isEmpty()) {
                logger.warn("用户未点赞过此动态: postId={}, userId={}", postId, userId);
                return false;
            }
            
            // 删除点赞记录
            postLikeRepository.delete(existingLike.get());
            
            // 减少点赞数
            if (post.getLikeCount() > 0) {
                post.setLikeCount(post.getLikeCount() - 1);
                post.setUpdatedAt(LocalDateTime.now());
                postRepository.save(post);
            }
            
            logger.info("取消点赞成功");
            return true;
            
        } catch (Exception e) {
            logger.error("取消点赞失败", e);
            throw e;
        }
    }
    
    @Override
    public PostListResult getUserPosts(String authorId, int page, int size) {
        try {
            logger.info("获取用户动态列表，用户ID: {}, 页码: {}, 大小: {}", authorId, page, size);
            
            // 创建分页请求
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            
            // 查询用户的动态
            Page<Post> postPage = postRepository.findByAuthorIdAndIsDeletedFalse(authorId, pageable);
            
            // 转换为摘要信息
            List<PostSummary> postSummaries = postPage.getContent().stream()
                .map(this::convertToPostSummary)
                .collect(Collectors.toList());
            
            logger.info("获取用户动态列表成功，总数: {}", postPage.getTotalElements());
            
            return new PostListResult(
                postSummaries,
                (int) postPage.getTotalElements(),
                page,
                size
            );
            
        } catch (Exception e) {
            logger.error("获取用户动态列表失败", e);
            throw e;
        }
    }
    
    @Override
    public PostListResult searchPosts(String keyword, String searchType, int page, int size) {
        try {
            logger.info("搜索动态，关键字: {}, 搜索类型: {}, 页码: {}, 大小: {}", keyword, searchType, page, size);
            
            // 创建分页请求
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            
            // 根据搜索类型执行不同的查询
            Page<Post> postPage;
            switch (searchType.toLowerCase()) {
                case "content":
                    // 只搜索内容
                    postPage = postRepository.findByContentKeywordAndIsDeletedFalse(keyword, pageable);
                    break;
                case "author":
                    // 只搜索作者
                    postPage = postRepository.findByAuthorKeywordAndIsDeletedFalse(keyword, pageable);
                    break;
                case "all":
                default:
                    // 搜索内容和作者
                    postPage = postRepository.findByKeywordAndIsDeletedFalse(keyword, pageable);
                    break;
            }
            
            // 转换为摘要信息
            List<PostSummary> postSummaries = postPage.getContent().stream()
                .map(this::convertToPostSummary)
                .collect(Collectors.toList());
            
            logger.info("搜索动态成功，关键字: {}, 结果数量: {}", keyword, postPage.getTotalElements());
            
            return new PostListResult(
                postSummaries,
                (int) postPage.getTotalElements(),
                page,
                size
            );
            
        } catch (Exception e) {
            logger.error("搜索动态失败", e);
            throw e;
        }
    }
    
    /**
     * 将Post实体转换为PostSummary
     */
    private PostSummary convertToPostSummary(Post post) {
        return convertToPostSummary(post, null);
    }
    
    /**
     * 将Post实体转换为PostSummary（带用户点赞状态）
     */
    private PostSummary convertToPostSummary(Post post, String currentUserId) {
        // 获取作者信息
        String authorName = "";
        String authorAvatar = "";
        
        if ("user".equals(post.getAuthorType())) {
            Optional<User> userOpt = userRepository.findByUserId(post.getAuthorId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                authorName = user.getNickname();
                authorAvatar = user.getAvatar();
            }
        } else if ("robot".equals(post.getAuthorType())) {
            Optional<Robot> robotOpt = robotRepository.findByRobotId(post.getAuthorId());
            if (robotOpt.isPresent()) {
                Robot robot = robotOpt.get();
                authorName = robot.getName();
                authorAvatar = robot.getAvatar();
            }
        }
        
        // 获取当前用户是否点赞状态
        boolean isLiked = false;
        if (currentUserId != null) {
            Optional<PostLike> userLike = postLikeRepository.findByPostIdAndUserId(post.getPostId(), currentUserId);
            isLiked = userLike.isPresent();
        }
        
        return new PostSummary(
            post.getPostId(),
            post.getAuthorId(),
            post.getAuthorType(),
            authorName,
            authorAvatar,
            post.getContent(),
            post.getImages(),
            post.getLikeCount(),
            post.getCommentCount(),
            isLiked,
            post.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            post.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
    
    /**
     * 生成动态ID
     */
    private String generatePostId() {
        return "post_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * 生成点赞记录ID
     */
    private String generatePostLikeId() {
        return "post_like_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * 异步触发所有在线机器人对新动态进行评论
     * 使用Spring的@Async注解，由AI任务执行器处理
     * 添加20-60秒随机延时，使回复更自然
     * 
     * @param postId 动态ID
     * @param postContent 动态内容
     */
    @Async("aiTaskExecutor")
    public void triggerRobotCommentsAsync(String postId, String postContent) {
        try {
            logger.info("开始触发AI机器人评论，动态ID: {}", postId);
            
            // 添加20-60秒的随机延时，使回复更自然
            int delaySeconds = new Random().nextInt(41) + 20; // 20-60秒
            logger.info("机器人评论延时 {} 秒后执行", delaySeconds);
            
            try {
                Thread.sleep(delaySeconds * 1000L);
            } catch (InterruptedException e) {
                logger.warn("机器人评论延时被中断", e);
                Thread.currentThread().interrupt();
                return;
            }
            
            // 获取所有活跃的机器人
            List<Robot> activeRobots = robotRepository.findByIsActiveTrue();
            if (activeRobots.isEmpty()) {
                logger.info("没有活跃的机器人，跳过评论触发");
                return;
            }
            
            // 随机选择1-3个机器人进行评论
            int commentCount = new Random().nextInt(3) + 1;
            commentCount = Math.min(commentCount, activeRobots.size());
            
            // 随机打乱机器人列表
            Collections.shuffle(activeRobots);
            
            List<String> skippedRobots = new ArrayList<>();
            
            for (int i = 0; i < commentCount; i++) {
                Robot robot = activeRobots.get(i);
                
                try {
                    // 检查机器人是否在活跃时间段
                    if (!robotBehaviorService.isRobotActive(robot)) {
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
                    boolean success = robotBehaviorService.triggerRobotComment(robot.getRobotId(), postId);
                    
                    if (success) {
                        logger.info("机器人 {} 评论成功", robot.getName());
                    } else {
                        logger.debug("机器人 {} 评论未触发", robot.getName());
                        skippedRobots.add(robot.getName() + "(概率未触发)");
                    }
                    
                } catch (Exception e) {
                    logger.error("机器人 {} 评论失败", robot.getName(), e);
                    skippedRobots.add(robot.getName() + "(评论失败)");
                }
            }
            
            if (!skippedRobots.isEmpty()) {
                logger.info("跳过的机器人: {}", String.join(", ", skippedRobots));
            }
            
        } catch (Exception e) {
            logger.error("触发AI机器人评论失败", e);
        }
    }
    
    @Override
    public LikeInfoResult getPostLikes(String postId) {
        try {
            logger.info("获取动态点赞信息，动态ID: {}", postId);
            
            // 验证动态是否存在
            Optional<Post> postOpt = postRepository.findByPostId(postId);
            if (postOpt.isEmpty()) {
                throw new IllegalArgumentException("动态不存在");
            }
            
            // 查询所有点赞记录
            List<PostLike> postLikes = postLikeRepository.findByPostId(postId);
            
            // 转换为点赞详情
            List<LikeDetail> likeDetails = new ArrayList<>();
            
            for (PostLike postLike : postLikes) {
                String userId = postLike.getUserId();
                String userName = "";
                String userAvatar = "";
                String userType = "user";
                
                // 查询用户信息
                Optional<User> userOpt = userRepository.findByUserId(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    userName = user.getNickname();
                    userAvatar = user.getAvatar();
                    userType = "user";
                } else {
                    // 如果不是用户，可能是机器人
                    Optional<Robot> robotOpt = robotRepository.findByRobotId(userId);
                    if (robotOpt.isPresent()) {
                        Robot robot = robotOpt.get();
                        userName = robot.getName();
                        userAvatar = robot.getAvatar();
                        userType = "robot";
                    } else {
                        // 如果用户和机器人都找不到，使用默认信息
                        userName = "未知用户";
                        userAvatar = "";
                        userType = "unknown";
                    }
                }
                
                LikeDetail likeDetail = new LikeDetail(
                    userId,
                    userName,
                    userAvatar,
                    userType,
                    postLike.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                );
                
                likeDetails.add(likeDetail);
            }
            
            // 按点赞时间倒序排列
            likeDetails.sort((a, b) -> b.getLikedAt().compareTo(a.getLikedAt()));
            
            logger.info("获取动态点赞信息成功，动态ID: {}, 点赞数量: {}", postId, likeDetails.size());
            
            return new LikeInfoResult(postId, likeDetails.size(), likeDetails);
            
        } catch (Exception e) {
            logger.error("获取动态点赞信息失败，动态ID: {}", postId, e);
            throw e;
        }
    }
    
    /**
     * 机器人每日行为统计内部类
     */
    private static class RobotDailyStats {
        private int commentCount = 0;
        
        public int getCommentCount() {
            return commentCount;
        }
        
        public void incrementComment() {
            commentCount++;
        }
    }
    
    /**
     * 获取机器人每日统计
     */
    private RobotDailyStats getDailyStats(String robotId) {
        // 简化实现，实际项目中应该使用缓存或数据库
        return new RobotDailyStats();
    }
} 