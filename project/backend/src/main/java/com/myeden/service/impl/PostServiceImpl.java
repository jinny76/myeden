package com.myeden.service.impl;

import com.myeden.entity.Post;
import com.myeden.entity.User;
import com.myeden.entity.Robot;
import com.myeden.repository.PostRepository;
import com.myeden.repository.UserRepository;
import com.myeden.repository.RobotRepository;
import com.myeden.service.PostService;
import com.myeden.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private FileService fileService;
    
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
            
            // TODO: 获取点赞用户列表和当前用户是否点赞状态
            List<String> likedUsers = new ArrayList<>();
            boolean isLiked = false;
            
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
            
            // TODO: 检查用户是否已经点赞
            // 这里需要实现点赞记录表来跟踪用户的点赞状态
            
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
            
            // TODO: 检查用户是否已经点赞
            // 这里需要实现点赞记录表来跟踪用户的点赞状态
            
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
    
    /**
     * 将Post实体转换为PostSummary
     */
    private PostSummary convertToPostSummary(Post post) {
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
        
        // TODO: 获取当前用户是否点赞状态
        boolean isLiked = false;
        
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
} 