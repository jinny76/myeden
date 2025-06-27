package com.myeden.controller;

import com.myeden.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 动态管理控制器
 * 
 * 功能说明：
 * - 提供动态发布、查询、删除API接口
 * - 支持图片上传和处理
 * - 管理动态的点赞和评论统计
 * - 支持分页查询和排序
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/posts")
@CrossOrigin(origins = "*")
public class PostController {
    
    private static final Logger logger = LoggerFactory.getLogger(PostController.class);
    
    @Autowired
    private PostService postService;
    
    /**
     * 发布动态
     * @param content 动态内容
     * @param images 图片文件列表
     * @return 动态发布结果
     */
    @PostMapping
    public ResponseEntity<EventResponse> createPost(
            @RequestParam("content") String content,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        
        try {
            logger.info("收到发布动态请求，内容长度: {}", content.length());
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 发布动态
            PostService.PostResult result = postService.createPost(userId, "user", content, images);
            
            logger.info("动态发布成功，动态ID: {}", result.getPostId());
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "动态发布成功",
                result
            ));
            
        } catch (Exception e) {
            logger.error("发布动态失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "发布动态失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 获取动态列表
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param authorType 作者类型过滤（可选）
     * @return 动态列表和分页信息
     */
    @GetMapping
    public ResponseEntity<EventResponse> getPostList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "authorType", required = false) String authorType) {
        
        try {
            logger.info("获取动态列表，页码: {}, 大小: {}, 作者类型: {}", page, size, authorType);
            
            // 参数验证
            if (page < 1) {
                page = 1;
            }
            if (size < 1 || size > 50) {
                size = 10;
            }
            
            // 获取动态列表
            PostService.PostListResult result = postService.getPostList(page, size, authorType);
            
            logger.info("获取动态列表成功，总数: {}", result.getTotal());
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "获取动态列表成功",
                result
            ));
            
        } catch (Exception e) {
            logger.error("获取动态列表失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "获取动态列表失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 获取动态详情
     * @param postId 动态ID
     * @return 动态详细信息
     */
    @GetMapping("/{postId}")
    public ResponseEntity<EventResponse> getPostDetail(@PathVariable String postId) {
        
        try {
            logger.info("获取动态详情，动态ID: {}", postId);
            
            // 获取动态详情
            PostService.PostDetail result = postService.getPostDetail(postId);
            
            logger.info("获取动态详情成功");
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "获取动态详情成功",
                result
            ));
            
        } catch (Exception e) {
            logger.error("获取动态详情失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "获取动态详情失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 删除动态
     * @param postId 动态ID
     * @return 删除结果
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<EventResponse> deletePost(@PathVariable String postId) {
        
        try {
            logger.info("删除动态，动态ID: {}", postId);
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 删除动态
            boolean result = postService.deletePost(postId, userId);
            
            if (result) {
                logger.info("动态删除成功");
                return ResponseEntity.ok(new EventResponse(
                    200,
                    "动态删除成功",
                    null
                ));
            } else {
                return ResponseEntity.badRequest().body(new EventResponse(
                    400,
                    "动态删除失败",
                    null
                ));
            }
            
        } catch (Exception e) {
            logger.error("删除动态失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "删除动态失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 点赞动态
     * @param postId 动态ID
     * @return 点赞结果
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<EventResponse> likePost(@PathVariable String postId) {
        
        try {
            logger.info("点赞动态，动态ID: {}", postId);
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 点赞动态
            boolean result = postService.likePost(postId, userId);
            
            if (result) {
                logger.info("动态点赞成功");
                return ResponseEntity.ok(new EventResponse(
                    200,
                    "点赞成功",
                    null
                ));
            } else {
                return ResponseEntity.badRequest().body(new EventResponse(
                    400,
                    "点赞失败",
                    null
                ));
            }
            
        } catch (Exception e) {
            logger.error("点赞动态失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "点赞失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 取消点赞动态
     * @param postId 动态ID
     * @return 取消点赞结果
     */
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<EventResponse> unlikePost(@PathVariable String postId) {
        
        try {
            logger.info("取消点赞动态，动态ID: {}", postId);
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 取消点赞动态
            boolean result = postService.unlikePost(postId, userId);
            
            if (result) {
                logger.info("取消点赞成功");
                return ResponseEntity.ok(new EventResponse(
                    200,
                    "取消点赞成功",
                    null
                ));
            } else {
                return ResponseEntity.badRequest().body(new EventResponse(
                    400,
                    "取消点赞失败",
                    null
                ));
            }
            
        } catch (Exception e) {
            logger.error("取消点赞失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "取消点赞失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 获取用户的动态列表
     * @param authorId 作者ID
     * @param page 页码
     * @param size 每页大小
     * @return 用户动态列表
     */
    @GetMapping("/user/{authorId}")
    public ResponseEntity<EventResponse> getUserPosts(
            @PathVariable String authorId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        try {
            logger.info("获取用户动态列表，用户ID: {}, 页码: {}, 大小: {}", authorId, page, size);
            
            // 参数验证
            if (page < 1) {
                page = 1;
            }
            if (size < 1 || size > 50) {
                size = 10;
            }
            
            // 获取用户动态列表
            PostService.PostListResult result = postService.getUserPosts(authorId, page, size);
            
            logger.info("获取用户动态列表成功，总数: {}", result.getTotal());
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "获取用户动态列表成功",
                result
            ));
            
        } catch (Exception e) {
            logger.error("获取用户动态列表失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "获取用户动态列表失败: " + e.getMessage(),
                null
            ));
        }
    }
} 