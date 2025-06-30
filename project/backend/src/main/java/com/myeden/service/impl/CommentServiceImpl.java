package com.myeden.service.impl;

import com.myeden.entity.Comment;
import com.myeden.entity.Post;
import com.myeden.entity.User;
import com.myeden.entity.Robot;
import com.myeden.entity.CommentLike;
import com.myeden.entity.UserPrivacySettings;
import com.myeden.repository.CommentRepository;
import com.myeden.repository.PostRepository;
import com.myeden.repository.UserRepository;
import com.myeden.repository.RobotRepository;
import com.myeden.repository.CommentLikeRepository;
import com.myeden.service.CommentService;
import com.myeden.service.WebSocketService;
import com.myeden.service.UserRobotLinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论管理服务实现类
 * 
 * 功能说明：
 * - 实现评论发布、查询、删除功能
 * - 支持评论回复功能
 * - 管理评论的点赞和回复统计
 * - 支持分页查询和排序
 * - 集成WebSocket实时消息推送
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
public class CommentServiceImpl implements CommentService {
    
    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private CommentLikeRepository commentLikeRepository;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired
    private UserRobotLinkService userRobotLinkService;
    
    @Override
    public CommentResult createComment(String postId, String authorId, String authorType, String content, String innerThoughts) {
        try {
            logger.info("开始创建评论，动态ID: {}, 作者ID: {}, 作者类型: {}", postId, authorId, authorType);
            
            // 验证参数
            if (!StringUtils.hasText(content)) {
                throw new IllegalArgumentException("评论内容不能为空");
            }
            
            if (!StringUtils.hasText(postId)) {
                throw new IllegalArgumentException("动态ID不能为空");
            }
            
            if (!StringUtils.hasText(authorId)) {
                throw new IllegalArgumentException("作者ID不能为空");
            }
            
            if (!StringUtils.hasText(authorType)) {
                throw new IllegalArgumentException("作者类型不能为空");
            }
            
            // 验证动态是否存在
            Optional<Post> postOpt = postRepository.findByPostIdAndIsDeletedFalse(postId);
            if (postOpt.isEmpty()) {
                throw new IllegalArgumentException("动态不存在");
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
            
            // 创建评论实体
            Comment comment = new Comment();
            comment.setCommentId(generateCommentId());
            comment.setPostId(postId);
            comment.setAuthorId(authorId);
            comment.setAuthorType(authorType);
            comment.setContent(content);
            comment.setInnerThoughts(innerThoughts);
            comment.setParentId(null); // 一级评论
            comment.setReplyToId(null); // 一级评论
            comment.setLikeCount(0);
            comment.setReplyCount(0);
            comment.setIsDeleted(false);
            comment.setCreatedAt(LocalDateTime.now());
            comment.setUpdatedAt(LocalDateTime.now());
            
            // 根据作者类型设置可见性
            if ("user".equals(authorType)) {
                // 获取用户的隐私设置
                Optional<User> userOpt = userRepository.findByUserId(authorId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    UserPrivacySettings privacySettings = user.getPrivacySettings();
                    if (privacySettings != null) {
                        comment.setVisibility(privacySettings.getReplyVisibility());
                        logger.info("用户评论可见性设置为: {}", privacySettings.getReplyVisibility());
                    } else {
                        comment.setVisibility("private"); // 默认私有
                        logger.info("用户隐私设置为空，评论可见性默认为: private");
                    }
                } else {
                    comment.setVisibility("private"); // 默认私有
                    logger.info("用户不存在，评论可见性默认为: private");
                }
            } else if ("robot".equals(authorType)) {
                // 机器人评论默认为公开
                comment.setVisibility("public");
                logger.info("机器人评论可见性设置为: public");
            }
            
            // 保存到数据库
            Comment savedComment = commentRepository.save(comment);
            
            // 更新动态的评论数
            Post post = postOpt.get();
            post.setCommentCount(post.getCommentCount() + 1);
            post.setUpdatedAt(LocalDateTime.now());
            postRepository.save(post);
            
            logger.info("评论创建成功，评论ID: {}", savedComment.getCommentId());
            
            // 推送WebSocket消息
            try {
                Map<String, Object> commentData = new HashMap<>();
                commentData.put("commentId", savedComment.getCommentId());
                commentData.put("postId", savedComment.getPostId());
                commentData.put("authorId", savedComment.getAuthorId());
                commentData.put("authorType", savedComment.getAuthorType());
                commentData.put("authorName", authorName);
                commentData.put("authorAvatar", authorAvatar);
                commentData.put("content", savedComment.getContent());
                commentData.put("innerThoughts", savedComment.getInnerThoughts());
                commentData.put("parentId", savedComment.getParentId());
                commentData.put("replyToId", savedComment.getReplyToId());
                commentData.put("createdAt", savedComment.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                
                webSocketService.pushCommentUpdate(commentData);
                logger.info("WebSocket评论更新消息推送成功");
            } catch (Exception e) {
                logger.warn("WebSocket消息推送失败", e);
            }
            
            return new CommentResult(
                savedComment.getCommentId(),
                savedComment.getContent(),
                savedComment.getParentId(),
                savedComment.getReplyToId(),
                savedComment.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
        } catch (Exception e) {
            logger.error("创建评论失败", e);
            throw e;
        }
    }
    
    @Override
    public CommentResult replyComment(String commentId, String authorId, String authorType, String content, String innerThoughts) {
        try {
            logger.info("开始回复评论，评论ID: {}, 作者ID: {}, 作者类型: {}", commentId, authorId, authorType);
            
            // 验证参数
            if (!StringUtils.hasText(content)) {
                throw new IllegalArgumentException("回复内容不能为空");
            }
            
            if (!StringUtils.hasText(commentId)) {
                throw new IllegalArgumentException("评论ID不能为空");
            }
            
            if (!StringUtils.hasText(authorId)) {
                throw new IllegalArgumentException("作者ID不能为空");
            }
            
            if (!StringUtils.hasText(authorType)) {
                throw new IllegalArgumentException("作者类型不能为空");
            }
            
            // 验证原评论是否存在
            Optional<Comment> parentCommentOpt = commentRepository.findByCommentIdAndIsDeletedFalse(commentId);
            if (parentCommentOpt.isEmpty()) {
                throw new IllegalArgumentException("原评论不存在");
            }
            
            Comment parentComment = parentCommentOpt.get();
            
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
            
            // 创建回复评论
            Comment reply = new Comment();
            reply.setCommentId(generateCommentId());
            reply.setPostId(parentComment.getPostId());
            reply.setAuthorId(authorId);
            reply.setAuthorType(authorType);
            reply.setContent(content);
            reply.setInnerThoughts(innerThoughts);
            reply.setParentId(commentId); // 父评论ID
            reply.setReplyToId(parentComment.getAuthorId()); // 回复目标ID
            reply.setLikeCount(0);
            reply.setReplyCount(0);
            reply.setIsDeleted(false);
            reply.setCreatedAt(LocalDateTime.now());
            reply.setUpdatedAt(LocalDateTime.now());
            
            // 根据作者类型设置可见性
            if ("user".equals(authorType)) {
                // 获取用户的隐私设置
                Optional<User> userOpt = userRepository.findByUserId(authorId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    UserPrivacySettings privacySettings = user.getPrivacySettings();
                    if (privacySettings != null) {
                        reply.setVisibility(privacySettings.getReplyVisibility());
                        logger.info("用户回复可见性设置为: {}", privacySettings.getReplyVisibility());
                    } else {
                        reply.setVisibility("private"); // 默认私有
                        logger.info("用户隐私设置为空，回复可见性默认为: private");
                    }
                } else {
                    reply.setVisibility("private"); // 默认私有
                    logger.info("用户不存在，回复可见性默认为: private");
                }
            } else if ("robot".equals(authorType)) {
                // 机器人回复默认为公开
                reply.setVisibility("public");
                logger.info("机器人回复可见性设置为: public");
            }
            
            // 保存到数据库
            Comment savedReply = commentRepository.save(reply);
            
            // 更新父评论的回复数
            parentComment.setReplyCount(parentComment.getReplyCount() + 1);
            parentComment.setUpdatedAt(LocalDateTime.now());
            commentRepository.save(parentComment);
            
            // 更新动态的评论数
            Optional<Post> postOpt = postRepository.findByPostIdAndIsDeletedFalse(parentComment.getPostId());
            if (postOpt.isPresent()) {
                Post post = postOpt.get();
                post.setCommentCount(post.getCommentCount() + 1);
                post.setUpdatedAt(LocalDateTime.now());
                postRepository.save(post);
            }
            
            logger.info("回复评论成功，回复ID: {}", savedReply.getCommentId());
            
            // 推送WebSocket消息
            try {
                Map<String, Object> replyData = new HashMap<>();
                replyData.put("commentId", savedReply.getCommentId());
                replyData.put("postId", savedReply.getPostId());
                replyData.put("authorId", savedReply.getAuthorId());
                replyData.put("authorType", savedReply.getAuthorType());
                replyData.put("authorName", authorName);
                replyData.put("authorAvatar", authorAvatar);
                replyData.put("content", savedReply.getContent());
                replyData.put("innerThoughts", savedReply.getInnerThoughts());
                replyData.put("parentId", savedReply.getParentId());
                replyData.put("replyToId", savedReply.getReplyToId());
                replyData.put("createdAt", savedReply.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                
                webSocketService.pushCommentUpdate(replyData);
                logger.info("WebSocket回复更新消息推送成功");
            } catch (Exception e) {
                logger.warn("WebSocket消息推送失败", e);
            }
            
            return new CommentResult(
                savedReply.getCommentId(),
                savedReply.getContent(),
                savedReply.getParentId(),
                savedReply.getReplyToId(),
                savedReply.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
        } catch (Exception e) {
            logger.error("回复评论失败", e);
            throw e;
        }
    }
    
    @Override
    public CommentListResult getCommentList(String postId, int page, int size, String currentUserId) {
        try {
            logger.info("获取动态评论列表，动态ID: {}, 页码: {}, 大小: {}, 当前用户: {}", postId, page, size, currentUserId);
            
            // 创建分页请求
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "createdAt"));
            
            // 获取用户已连接的机器人ID列表
            List<String> connectedRobotIds = new ArrayList<>();
            if (currentUserId != null) {
                List<UserRobotLinkService.LinkSummary> activeLinks = userRobotLinkService.getUserActiveLinks(currentUserId);
                connectedRobotIds = activeLinks.stream()
                    .map(UserRobotLinkService.LinkSummary::getRobotId)
                    .collect(Collectors.toList());
            }
            
            // 查询一级评论（parentId为null，在分页前完成过滤）
            Page<Comment> commentPage = commentRepository.findByPostIdAndParentIdIsNullAndIsDeletedFalse(postId, pageable, currentUserId, connectedRobotIds);
            
            // 转换为摘要信息
            List<CommentSummary> commentSummaries = commentPage.getContent().stream()
                .map(comment -> convertToCommentSummary(comment, currentUserId))
                .collect(Collectors.toList());
            
            logger.info("获取动态评论列表成功，总数: {}", commentPage.getTotalElements());
            
            return new CommentListResult(
                commentSummaries,
                (int) commentPage.getTotalElements(),
                page,
                size
            );
            
        } catch (Exception e) {
            logger.error("获取动态评论列表失败", e);
            throw e;
        }
    }
    
    /**
     * 检查评论是否对用户可见
     * 
     * @param comment 评论
     * @param currentUserId 当前用户ID
     * @return 是否可见
     */
    private boolean isCommentVisibleToUser(Comment comment, String currentUserId) {
        // 如果是公开内容，所有人都可见
        if ("public".equals(comment.getVisibility())) {
            return true;
        }
        
        // 如果是私有内容，只有作者本人可见
        if ("private".equals(comment.getVisibility())) {
            return comment.getAuthorId().equals(currentUserId);
        }
        
        // 默认可见
        return true;
    }
    
    @Override
    public CommentListResult getReplyList(String commentId, int page, int size, String currentUserId) {
        try {
            logger.info("获取评论回复列表，评论ID: {}, 页码: {}, 大小: {}, 当前用户: {}", commentId, page, size, currentUserId);
            
            // 创建分页请求
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "createdAt"));
            
            // 获取用户已连接的机器人ID列表
            List<String> connectedRobotIds = new ArrayList<>();
            if (currentUserId != null) {
                List<UserRobotLinkService.LinkSummary> activeLinks = userRobotLinkService.getUserActiveLinks(currentUserId);
                connectedRobotIds = activeLinks.stream()
                    .map(UserRobotLinkService.LinkSummary::getRobotId)
                    .collect(Collectors.toList());
            }
            
            // 查询回复（parentId为commentId，在分页前完成过滤）
            Page<Comment> replyPage = commentRepository.findByParentIdAndIsDeletedFalse(commentId, pageable, currentUserId, connectedRobotIds);
            
            // 转换为摘要信息
            List<CommentSummary> replySummaries = replyPage.getContent().stream()
                .map(reply -> convertToCommentSummary(reply, currentUserId))
                .collect(Collectors.toList());
            
            logger.info("获取评论回复列表成功，总数: {}", replyPage.getTotalElements());
            
            return new CommentListResult(
                replySummaries,
                (int) replyPage.getTotalElements(),
                page,
                size
            );
            
        } catch (Exception e) {
            logger.error("获取评论回复列表失败", e);
            throw e;
        }
    }
    
    @Override
    public CommentDetail getCommentDetail(String commentId) {
        try {
            logger.info("获取评论详情，评论ID: {}", commentId);
            
            // 查询评论
            Optional<Comment> commentOpt = commentRepository.findByCommentIdAndIsDeletedFalse(commentId);
            if (commentOpt.isEmpty()) {
                throw new IllegalArgumentException("评论不存在");
            }
            
            Comment comment = commentOpt.get();
            
            // 获取作者信息
            String authorName = "";
            String authorAvatar = "";
            
            if ("user".equals(comment.getAuthorType())) {
                Optional<User> userOpt = userRepository.findByUserId(comment.getAuthorId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    authorName = user.getNickname();
                    authorAvatar = user.getAvatar();
                }
            } else if ("robot".equals(comment.getAuthorType())) {
                Optional<Robot> robotOpt = robotRepository.findByRobotId(comment.getAuthorId());
                if (robotOpt.isPresent()) {
                    Robot robot = robotOpt.get();
                    authorName = robot.getName();
                    authorAvatar = robot.getAvatar();
                }
            }
            
            // 获取回复目标信息
            String replyToName = "";
            if (comment.getReplyToId() != null) {
                if ("user".equals(comment.getAuthorType())) {
                    Optional<User> replyToUserOpt = userRepository.findByUserId(comment.getReplyToId());
                    if (replyToUserOpt.isPresent()) {
                        replyToName = replyToUserOpt.get().getNickname();
                    }
                } else if ("robot".equals(comment.getAuthorType())) {
                    Optional<Robot> replyToRobotOpt = robotRepository.findByRobotId(comment.getReplyToId());
                    if (replyToRobotOpt.isPresent()) {
                        replyToName = replyToRobotOpt.get().getName();
                    }
                }
            }
            
            // 获取点赞用户列表和当前用户是否点赞状态
            List<CommentLike> commentLikes = commentLikeRepository.findByCommentId(commentId);
            List<String> likedUsers = commentLikes.stream()
                .map(CommentLike::getUserId)
                .collect(Collectors.toList());
            boolean isLiked = false; // 这里需要传入当前用户ID来判断，暂时设为false
            
            logger.info("获取评论详情成功");
            
            return new CommentDetail(
                comment.getCommentId(),
                comment.getPostId(),
                comment.getAuthorId(),
                comment.getAuthorType(),
                authorName,
                authorAvatar,
                comment.getContent(),
                comment.getInnerThoughts(),
                comment.getParentId(),
                comment.getReplyToId(),
                replyToName,
                comment.getLikeCount(),
                comment.getReplyCount(),
                isLiked,
                likedUsers,
                comment.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                comment.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
        } catch (Exception e) {
            logger.error("获取评论详情失败", e);
            throw e;
        }
    }
    
    @Override
    public boolean deleteComment(String commentId, String authorId) {
        try {
            logger.info("删除评论，评论ID: {}, 作者ID: {}", commentId, authorId);
            
            // 查询评论
            Optional<Comment> commentOpt = commentRepository.findByCommentIdAndIsDeletedFalse(commentId);
            if (commentOpt.isEmpty()) {
                throw new IllegalArgumentException("评论不存在");
            }
            
            Comment comment = commentOpt.get();
            
            // 验证权限
            if (!comment.getAuthorId().equals(authorId)) {
                throw new IllegalArgumentException("无权限删除此评论");
            }
            
            // 软删除
            comment.setIsDeleted(true);
            comment.setUpdatedAt(LocalDateTime.now());
            commentRepository.save(comment);
            
            // 更新动态的评论数
            Optional<Post> postOpt = postRepository.findByPostIdAndIsDeletedFalse(comment.getPostId());
            if (postOpt.isPresent()) {
                Post post = postOpt.get();
                if (post.getCommentCount() > 0) {
                    post.setCommentCount(post.getCommentCount() - 1);
                    post.setUpdatedAt(LocalDateTime.now());
                    postRepository.save(post);
                }
            }
            
            // 如果是回复，更新父评论的回复数
            if (comment.getParentId() != null) {
                Optional<Comment> parentCommentOpt = commentRepository.findByCommentIdAndIsDeletedFalse(comment.getParentId());
                if (parentCommentOpt.isPresent()) {
                    Comment parentComment = parentCommentOpt.get();
                    if (parentComment.getReplyCount() > 0) {
                        parentComment.setReplyCount(parentComment.getReplyCount() - 1);
                        parentComment.setUpdatedAt(LocalDateTime.now());
                        commentRepository.save(parentComment);
                    }
                }
            }
            
            logger.info("评论删除成功");
            return true;
            
        } catch (Exception e) {
            logger.error("删除评论失败", e);
            throw e;
        }
    }
    
    @Override
    public boolean likeComment(String commentId, String userId) {
        try {
            logger.info("点赞评论，评论ID: {}, 用户ID: {}", commentId, userId);
            
            // 查询评论
            Optional<Comment> commentOpt = commentRepository.findByCommentIdAndIsDeletedFalse(commentId);
            if (commentOpt.isEmpty()) {
                throw new IllegalArgumentException("评论不存在");
            }
            
            Comment comment = commentOpt.get();
            
            // 检查用户是否已经点赞
            Optional<CommentLike> existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId);
            if (existingLike.isPresent()) {
                logger.warn("用户已经点赞过此评论: commentId={}, userId={}", commentId, userId);
                return false;
            }
            
            // 创建点赞记录
            CommentLike commentLike = CommentLike.builder()
                .commentLikeId(generateCommentLikeId())
                .commentId(commentId)
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            commentLikeRepository.save(commentLike);
            
            // 增加点赞数
            comment.setLikeCount(comment.getLikeCount() + 1);
            comment.setUpdatedAt(LocalDateTime.now());
            commentRepository.save(comment);
            
            logger.info("评论点赞成功");
            return true;
            
        } catch (Exception e) {
            logger.error("点赞评论失败", e);
            throw e;
        }
    }
    
    @Override
    public boolean unlikeComment(String commentId, String userId) {
        try {
            logger.info("取消点赞评论，评论ID: {}, 用户ID: {}", commentId, userId);
            
            // 查询评论
            Optional<Comment> commentOpt = commentRepository.findByCommentIdAndIsDeletedFalse(commentId);
            if (commentOpt.isEmpty()) {
                throw new IllegalArgumentException("评论不存在");
            }
            
            Comment comment = commentOpt.get();
            
            // 检查用户是否已经点赞
            Optional<CommentLike> existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId);
            if (existingLike.isEmpty()) {
                logger.warn("用户未点赞过此评论: commentId={}, userId={}", commentId, userId);
                return false;
            }
            
            // 删除点赞记录
            commentLikeRepository.delete(existingLike.get());
            
            // 减少点赞数
            if (comment.getLikeCount() > 0) {
                comment.setLikeCount(comment.getLikeCount() - 1);
                comment.setUpdatedAt(LocalDateTime.now());
                commentRepository.save(comment);
            }
            
            logger.info("取消点赞成功");
            return true;
            
        } catch (Exception e) {
            logger.error("取消点赞失败", e);
            throw e;
        }
    }
    
    /**
     * 将Comment实体转换为CommentSummary
     */
    private CommentSummary convertToCommentSummary(Comment comment) {
        return convertToCommentSummary(comment, null);
    }
    
    /**
     * 将Comment实体转换为CommentSummary（带用户点赞状态）
     */
    private CommentSummary convertToCommentSummary(Comment comment, String currentUserId) {
        // 获取作者信息
        String authorName = "";
        String authorAvatar = "";
        
        if ("user".equals(comment.getAuthorType())) {
            Optional<User> userOpt = userRepository.findByUserId(comment.getAuthorId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                authorName = user.getNickname();
                authorAvatar = user.getAvatar();
            }
        } else if ("robot".equals(comment.getAuthorType())) {
            Optional<Robot> robotOpt = robotRepository.findByRobotId(comment.getAuthorId());
            if (robotOpt.isPresent()) {
                Robot robot = robotOpt.get();
                authorName = robot.getName();
                authorAvatar = robot.getAvatar();
            }
        }
        
        // 获取回复目标信息
        String replyToName = "";
        if (comment.getReplyToId() != null) {
            if ("user".equals(comment.getAuthorType())) {
                Optional<User> replyToUserOpt = userRepository.findByUserId(comment.getReplyToId());
                if (replyToUserOpt.isPresent()) {
                    replyToName = replyToUserOpt.get().getNickname();
                }
            } else if ("robot".equals(comment.getAuthorType())) {
                Optional<Robot> replyToRobotOpt = robotRepository.findByRobotId(comment.getReplyToId());
                if (replyToRobotOpt.isPresent()) {
                    replyToName = replyToRobotOpt.get().getName();
                }
            }
        }
        
        // 获取当前用户是否点赞状态
        boolean isLiked = false;
        if (currentUserId != null) {
            Optional<CommentLike> userLike = commentLikeRepository.findByCommentIdAndUserId(comment.getCommentId(), currentUserId);
            isLiked = userLike.isPresent();
        }
        
        return new CommentSummary(
            comment.getCommentId(),
            comment.getPostId(),
            comment.getAuthorId(),
            comment.getAuthorType(),
            authorName,
            authorAvatar,
            comment.getContent(),
            comment.getInnerThoughts(),
            comment.getParentId(),
            comment.getReplyToId(),
            replyToName,
            comment.getLikeCount(),
            comment.getReplyCount(),
            isLiked,
            comment.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            comment.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
    
    /**
     * 生成评论ID
     */
    private String generateCommentId() {
        return "comment_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * 生成评论点赞记录ID
     */
    private String generateCommentLikeId() {
        return "comment_like_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    @Override
    public boolean hasRobotCommentedOnPost(String robotId, String postId) {
        try {
            //logger.debug("检查机器人 {} 是否已评论过帖子 {}", robotId, postId);
            
            // 查询该机器人在指定帖子下的评论数量
            long commentCount = commentRepository.countByPostIdAndAuthorIdAndAuthorTypeAndIsDeletedFalse(
                postId, robotId, "robot");
            
            boolean hasCommented = commentCount > 0;
            //logger.debug("机器人 {} 在帖子 {} 下的评论数量: {}, 已评论: {}",
            //            robotId, postId, commentCount, hasCommented);
            
            return hasCommented;
            
        } catch (Exception e) {
            logger.error("检查机器人评论状态失败: robotId={}, postId={}, error={}", 
                        robotId, postId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public List<Comment> findRecentComments(LocalDateTime since) {
        try {
            logger.debug("查找 {} 之后的评论", since);
            
            // 查询指定时间之后的评论，按创建时间倒序排列
            List<Comment> recentComments = commentRepository.findByCreatedAtAfterAndIsDeletedFalseOrderByCreatedAtDesc(since);
            
            logger.debug("找到 {} 条近期评论", recentComments.size());
            return recentComments;
            
        } catch (Exception e) {
            logger.error("查找近期评论失败: since={}, error={}", since, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean hasRobotRepliedToComment(String robotId, String commentId) {
        try {
            //logger.debug("检查机器人 {} 是否已回复过评论 {}", robotId, commentId);
            
            // 查询该机器人对指定评论的回复数量
            long replyCount = commentRepository.countByReplyToIdAndAuthorIdAndAuthorTypeAndIsDeletedFalse(
                commentId, robotId, "robot");
            
            boolean hasReplied = replyCount > 0;
            //logger.debug("机器人 {} 对评论 {} 的回复数量: {}, 已回复: {}",
            //            robotId, commentId, replyCount, hasReplied);
            
            return hasReplied;
            
        } catch (Exception e) {
            logger.error("检查机器人回复状态失败: robotId={}, commentId={}, error={}",
                        robotId, commentId, e.getMessage(), e);
            return false;
        }
    }
} 