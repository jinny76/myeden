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
import com.myeden.service.CommentService;
import com.myeden.service.UserRobotLinkService;
import com.myeden.service.CommentService.CommentSummary;
import com.myeden.model.PostQueryParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private UserRobotLinkService userRobotLinkService;
    
    @Override
    public PostResult createPost(String authorId, String authorType, String content, List<MultipartFile> images, String visibility) {
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
            
            // 设置可见性
            if ("user".equals(authorType)) {
                // 用户动态：根据传入的visibility参数设置
                if (visibility != null) {
                    post.setVisibility(visibility);
                    logger.info("用户动态可见性设置为: {}", visibility);
                } else {
                    // 如果visibility为null，保持默认值（private）
                    logger.info("用户动态可见性保持默认值: private");
                }
            } else if ("robot".equals(authorType)) {
                // 机器人动态：不设置visibility，保持为null
                post.setVisibility(null);
                logger.info("机器人动态可见性保持为null");
            }
            
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
    public PostListResult getPostList(int page, int size, String authorType, String currentUserId) {
        try {
            logger.info("获取动态列表，页码: {}, 大小: {}, 作者类型: {}", page, size, authorType);
            
            // 创建分页请求
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            
            // 获取用户已连接的机器人ID列表
            List<String> connectedRobotIds = new ArrayList<>();
            if (currentUserId != null) {
                // 这里需要注入 UserRobotLinkService 来获取连接的机器人
                // 暂时使用空列表，后续可以完善
            }
            
            // 查询动态
            Page<Post> postPage;
            if (StringUtils.hasText(authorType)) {
                postPage = postRepository.findByAuthorTypeAndIsDeletedFalse(authorType, pageable, currentUserId, connectedRobotIds);
            } else {
                postPage = postRepository.findByIsDeletedFalse(pageable, currentUserId, connectedRobotIds);
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
    public PostDetail getPostDetail(String postId, String currentUserId) {
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
            
            // 获取点赞详情列表和当前用户是否点赞状态
            List<PostLike> postLikes = postLikeRepository.findByPostId(postId);
            List<LikeDetail> likes = new ArrayList<>();
            boolean isLiked = false;
            
            // 判断当前用户是否已点赞
            if (currentUserId != null) {
                Optional<PostLike> userLike = postLikeRepository.findByPostIdAndUserId(postId, currentUserId);
                isLiked = userLike.isPresent();
            }
            
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
                
                likes.add(likeDetail);
            }
            
            // 按点赞时间倒序排列
            likes.sort((a, b) -> b.getLikedAt().compareTo(a.getLikedAt()));
            
            // 加载评论和回复列表
            List<CommentSummary> comments = loadCommentsWithReplies(postId);
            
            logger.info("获取动态详情成功，评论数量: {}", comments.size());
            
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
                likes,
                comments,
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
            Page<Post> postPage = postRepository.findByAuthorIdAndIsDeletedFalse(authorId, pageable, authorId);
            
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
    public PostListResult searchPosts(String keyword, int page, int size) {
        logger.info("搜索动态，关键字: {}, 页码: {}, 大小: {}", keyword, page, size);
        // 直接调用queryPosts逻辑或返回空实现（如已废弃）
        return null;
    }
    
    @Override
    public PostListResult queryPosts(PostQueryParams params) {
        try {
            logger.info("统一查询动态，参数: {}", params);
            
            // 验证参数
            params.validate();
            
            // 使用传入的已连接机器人ID列表，如果没有则自动获取
            List<String> connectedRobotIds = params.getConnectedRobotIds();
            if (connectedRobotIds == null || connectedRobotIds.isEmpty()) {
                connectedRobotIds = getConnectedRobotIds(params.getCurrentUserId());
                logger.debug("自动获取用户 {} 连接的机器人，数量: {}", params.getCurrentUserId(), connectedRobotIds.size());
            } else {
                logger.debug("使用传入的连接机器人参数，数量: {}", connectedRobotIds.size());
            }
            
            // 确定排序方式
            Sort sort = determineSort(params);
            Pageable pageable = PageRequest.of(params.getPage() - 1, params.getSize(), sort);
            
            // 执行查询，根据参数选择不同的查询方法
            Page<Post> postPage;
            
            if (StringUtils.hasText(params.getKeyword())) {
                // 有关键词搜索
                if (StringUtils.hasText(params.getAuthorType())) {
                    // 关键词 + 作者类型过滤 - 使用专门的查询方法
                    postPage = postRepository.findByKeywordAndAuthorTypeAndIsDeletedFalse(
                        params.getKeyword(),
                        params.getAuthorType(),
                        pageable, 
                        params.getCurrentUserId(), 
                        connectedRobotIds
                    );
                } else {
                    // 只有关键词搜索
                    postPage = postRepository.findByKeywordAndIsDeletedFalse(
                        params.getKeyword(), 
                        pageable, 
                        params.getCurrentUserId(), 
                        connectedRobotIds
                    );
                }
            } else if (StringUtils.hasText(params.getAuthorType())) {
                // 只有作者类型过滤
                postPage = postRepository.findByAuthorTypeAndIsDeletedFalse(
                    params.getAuthorType(), 
                    pageable, 
                    params.getCurrentUserId(), 
                    connectedRobotIds
                );
            } else {
                // 基础查询（无过滤条件）
                postPage = postRepository.findByIsDeletedFalse(
                    pageable, 
                    params.getCurrentUserId(), 
                    connectedRobotIds
                );
            }
            
            // 转换为摘要信息
            List<PostSummary> postSummaries = postPage.getContent().stream()
                .map(post -> convertToPostSummary(post, params.getCurrentUserId()))
                .collect(Collectors.toList());
            
            logger.info("统一查询动态成功，总数: {}", postPage.getTotalElements());
            
            return new PostListResult(
                postSummaries,
                (int) postPage.getTotalElements(),
                params.getPage(),
                params.getSize()
            );
            
        } catch (Exception e) {
            logger.error("统一查询动态失败", e);
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
     * 移除延时逻辑，直接执行机器人评论
     * 
     * @param postId 动态ID
     * @param postContent 动态内容
     */
    @Async("aiTaskExecutor")
    public void triggerRobotCommentsAsync(String postId, String postContent) {
        try {
            logger.info("开始触发AI机器人评论，动态ID: {}", postId);
            
            // 直接执行机器人评论，不添加延时
            
            // 获取动态信息，确定作者
            Optional<Post> postOpt = postRepository.findByPostId(postId);
            if (postOpt.isEmpty()) {
                logger.warn("动态不存在，跳过机器人评论触发，动态ID: {}", postId);
                return;
            }
            
            Post post = postOpt.get();
            String authorId = post.getAuthorId();
            String authorType = post.getAuthorType();
            
            // 只对用户发布的动态触发机器人评论
            if (!"user".equals(authorType)) {
                logger.debug("动态作者不是用户，跳过机器人评论触发，作者类型: {}", authorType);
                return;
            }
            
            // 获取与用户有链接的机器人
            List<UserRobotLinkService.LinkSummary> userLinks = userRobotLinkService.getUserActiveLinks(authorId);
            if (userLinks.isEmpty()) {
                logger.debug("用户 {} 没有链接的机器人，跳过评论触发", authorId);
                return;
            }
            
            // 获取链接的机器人ID列表
            List<String> linkedRobotIds = userLinks.stream()
                .map(UserRobotLinkService.LinkSummary::getRobotId)
                .collect(Collectors.toList());
            
            // 获取这些机器人的详细信息
            List<Robot> linkedRobots = new ArrayList<>();
            for (String robotId : linkedRobotIds) {
                Optional<Robot> robotOpt = robotRepository.findByRobotId(robotId);
                if (robotOpt.isPresent()) {
                    linkedRobots.add(robotOpt.get());
                }
            }
            
            if (linkedRobots.isEmpty()) {
                logger.debug("没有找到链接的机器人，跳过评论触发");
                return;
            }
            
            // 随机选择1-3个有链接的机器人进行评论
            int commentCount = new Random().nextInt(3) + 1;
            commentCount = Math.min(commentCount, linkedRobots.size());
            
            // 随机打乱机器人列表
            Collections.shuffle(linkedRobots);
            
            List<String> skippedRobots = new ArrayList<>();
            List<String> successRobots = new ArrayList<>();
            
            for (int i = 0; i < commentCount; i++) {
                Robot robot = linkedRobots.get(i);
                
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
                        successRobots.add(robot.getName());
                    } else {
                        logger.debug("机器人 {} 评论未触发", robot.getName());
                        skippedRobots.add(robot.getName() + "(概率未触发)");
                    }
                    
                } catch (Exception e) {
                    logger.error("机器人 {} 评论失败", robot.getName(), e);
                    skippedRobots.add(robot.getName() + "(评论失败)");
                }
            }
            
            logger.info("机器人评论触发完成，动态ID: {}, 成功: {}, 跳过: {}", 
                       postId, successRobots.size(), skippedRobots.size());
            
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
     * 获取用户已连接的机器人ID列表
     * 这些机器人的动态对用户可见，无论visibility设置如何
     */
    private List<String> getConnectedRobotIds(String currentUserId) {
        List<String> connectedRobotIds = new ArrayList<>();
        if (currentUserId != null) {
            try {
                // 获取用户激活的机器人链接
                List<UserRobotLinkService.LinkSummary> links = userRobotLinkService.getUserActiveLinks(currentUserId);
                connectedRobotIds = links.stream()
                    .map(UserRobotLinkService.LinkSummary::getRobotId)
                    .collect(Collectors.toList());
                
                logger.debug("用户 {} 连接的机器人数量: {}", currentUserId, connectedRobotIds.size());
            } catch (Exception e) {
                logger.warn("获取用户连接机器人失败，用户ID: {}", currentUserId, e);
            }
        }
        return connectedRobotIds;
    }
    
    /**
     * 确定排序方式
     */
    private Sort determineSort(PostQueryParams params) {
        // 默认按创建时间倒序排列
        return Sort.by(Sort.Direction.DESC, "createdAt");
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
    
    /**
     * 加载动态的评论和回复列表
     * 一次性加载所有评论和回复，避免前端多次调用
     * 
     * @param postId 动态ID
     * @return 评论和回复列表
     */
    private List<CommentSummary> loadCommentsWithReplies(String postId) {
        try {
            logger.debug("开始加载动态评论和回复，动态ID: {}", postId);
            
            // 获取动态的所有评论（一级评论）
            CommentService.CommentListResult commentResult = commentService.getCommentList(postId, 1, 1000, null); // 获取所有评论
            List<CommentSummary> allComments = new ArrayList<>();
            
            logger.debug("获取到一级评论数量: {}", commentResult.getComments().size());
            
            for (CommentSummary comment : commentResult.getComments()) {
                // 添加一级评论
                allComments.add(comment);
                logger.debug("添加一级评论: {}, 回复数: {}", comment.getCommentId(), comment.getReplyCount());
                
                // 如果评论有回复，加载回复列表
                if (comment.getReplyCount() > 0) {
                    try {
                        CommentService.CommentListResult replyResult = commentService.getReplyList(comment.getCommentId(), 1, 1000, null); // 获取所有回复
                        logger.debug("评论 {} 的回复数量: {}", comment.getCommentId(), replyResult.getComments().size());
                        allComments.addAll(replyResult.getComments());
                        
                        // 调试回复信息
                        for (CommentSummary reply : replyResult.getComments()) {
                            logger.debug("回复: commentId={}, parentId={}, content={}", 
                                       reply.getCommentId(), reply.getParentId(), reply.getContent());
                        }
                    } catch (Exception e) {
                        logger.warn("加载评论回复失败，评论ID: {}", comment.getCommentId(), e);
                    }
                }
            }
            
            // 按创建时间排序
            allComments.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
            
            logger.debug("动态评论和回复加载完成，动态ID: {}, 总数: {}", postId, allComments.size());
            
            return allComments;
            
        } catch (Exception e) {
            logger.error("加载动态评论和回复失败，动态ID: {}", postId, e);
            return new ArrayList<>();
        }
    }
} 