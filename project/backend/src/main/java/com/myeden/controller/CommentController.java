package com.myeden.controller;

import com.myeden.service.CommentService;
import com.myeden.service.UserSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 评论管理控制器
 * 
 * 功能说明：
 * - 提供评论发布、查询、删除API接口
 * - 支持评论回复功能
 * - 管理评论的点赞和回复统计
 * - 支持分页查询和排序
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1")
public class CommentController {
    
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private UserSettingService userSettingService;
    
    /**
     * 发表评论
     * @param postId 动态ID
     * @param request 评论请求数据
     * @return 评论发布结果
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<EventResponse> createComment(
            @PathVariable String postId,
            @RequestBody CommentRequest request) {
        
        try {
            logger.info("收到发表评论请求，动态ID: {}, 内容长度: {}", postId, request.getContent().length());
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 检查用户隐私设置
            String visibility = null; // 默认为null，表示继承用户设置
            try {
                var userSetting = userSettingService.getUserSetting(userId);
                if (userSetting != null && userSetting.getPublicPosts() != null && userSetting.getPublicPosts()) {
                    visibility = "public";
                    logger.info("用户允许公开评论，设置可见性为: public");
                } else {
                    logger.info("用户不允许公开评论，保持可见性为null（继承默认设置）");
                }
            } catch (Exception e) {
                logger.warn("获取用户隐私设置失败，使用默认设置", e);
            }
            
            // 发表评论
            CommentService.CommentResult result = commentService.createComment(postId, userId, "user", request.getContent(), visibility);
            
            logger.info("评论发表成功，评论ID: {}, 可见性: {}", result.getCommentId(), visibility);
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "评论发表成功",
                result
            ));
            
        } catch (Exception e) {
            logger.error("发表评论失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "发表评论失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 回复评论
     * @param commentId 评论ID
     * @param request 回复请求数据
     * @return 回复发布结果
     */
    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<EventResponse> replyComment(
            @PathVariable String commentId,
            @RequestBody CommentRequest request) {
        
        try {
            logger.info("收到回复评论请求，评论ID: {}, 内容长度: {}", commentId, request.getContent().length());
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 检查用户隐私设置
            String visibility = null; // 默认为null，表示继承用户设置
            try {
                var userSetting = userSettingService.getUserSetting(userId);
                if (userSetting != null && userSetting.getPublicPosts() != null && userSetting.getPublicPosts()) {
                    visibility = "public";
                    logger.info("用户允许公开回复，设置可见性为: public");
                } else {
                    logger.info("用户不允许公开回复，保持可见性为null（继承默认设置）");
                }
            } catch (Exception e) {
                logger.warn("获取用户隐私设置失败，使用默认设置", e);
            }
            
            // 回复评论
            CommentService.CommentResult result = commentService.replyComment(commentId, userId, "user", request.getContent(), visibility);
            
            logger.info("回复评论成功，回复ID: {}, 可见性: {}", result.getCommentId(), visibility);
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "回复评论成功",
                result
            ));
            
        } catch (Exception e) {
            logger.error("回复评论失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "回复评论失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 获取动态的评论列表
     * @param postId 动态ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 评论列表和分页信息
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<EventResponse> getCommentList(
            @PathVariable String postId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        try {
            logger.info("获取动态评论列表，动态ID: {}, 页码: {}, 大小: {}", postId, page, size);
            
            // 参数验证
            if (page < 1) {
                page = 1;
            }
            if (size < 1 || size > 50) {
                size = 10;
            }
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // 获取评论列表
            CommentService.CommentListResult result = commentService.getCommentList(postId, page, size, currentUserId);
            
            logger.info("获取动态评论列表成功，总数: {}", result.getTotal());
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "获取评论列表成功",
                result
            ));
            
        } catch (Exception e) {
            logger.error("获取动态评论列表失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "获取评论列表失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 获取评论的回复列表
     * @param commentId 评论ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 回复列表和分页信息
     */
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<EventResponse> getReplyList(
            @PathVariable String commentId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        try {
            logger.info("获取评论回复列表，评论ID: {}, 页码: {}, 大小: {}", commentId, page, size);
            
            // 参数验证
            if (page < 1) {
                page = 1;
            }
            if (size < 1 || size > 50) {
                size = 10;
            }
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // 获取回复列表
            CommentService.CommentListResult result = commentService.getReplyList(commentId, page, size, currentUserId);
            
            logger.info("获取评论回复列表成功，总数: {}", result.getTotal());
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "获取回复列表成功",
                result
            ));
            
        } catch (Exception e) {
            logger.error("获取评论回复列表失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "获取回复列表失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 获取评论详情
     * @param commentId 评论ID
     * @return 评论详细信息
     */
    @GetMapping("/comments/{commentId}")
    public ResponseEntity<EventResponse> getCommentDetail(@PathVariable String commentId) {
        
        try {
            logger.info("获取评论详情，评论ID: {}", commentId);
            
            // 获取评论详情
            CommentService.CommentDetail result = commentService.getCommentDetail(commentId);
            
            logger.info("获取评论详情成功");
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "获取评论详情成功",
                result
            ));
            
        } catch (Exception e) {
            logger.error("获取评论详情失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "获取评论详情失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 删除评论
     * @param commentId 评论ID
     * @return 删除结果
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<EventResponse> deleteComment(@PathVariable String commentId) {
        
        try {
            logger.info("删除评论，评论ID: {}", commentId);
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 删除评论
            boolean result = commentService.deleteComment(commentId, userId);
            
            if (result) {
                logger.info("评论删除成功");
                return ResponseEntity.ok(new EventResponse(
                    200,
                    "评论删除成功",
                    null
                ));
            } else {
                return ResponseEntity.badRequest().body(new EventResponse(
                    400,
                    "评论删除失败",
                    null
                ));
            }
            
        } catch (Exception e) {
            logger.error("删除评论失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "删除评论失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 点赞评论
     * @param commentId 评论ID
     * @return 点赞结果
     */
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<EventResponse> likeComment(@PathVariable String commentId) {
        
        try {
            logger.info("点赞评论，评论ID: {}", commentId);
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 点赞评论
            boolean result = commentService.likeComment(commentId, userId);
            
            if (result) {
                logger.info("评论点赞成功");
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
            logger.error("点赞评论失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "点赞失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 取消点赞评论
     * @param commentId 评论ID
     * @return 取消点赞结果
     */
    @DeleteMapping("/comments/{commentId}/like")
    public ResponseEntity<EventResponse> unlikeComment(@PathVariable String commentId) {
        
        try {
            logger.info("取消点赞评论，评论ID: {}", commentId);
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 取消点赞评论
            boolean result = commentService.unlikeComment(commentId, userId);
            
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
     * 评论请求数据类
     */
    public static class CommentRequest {
        private String content;
        
        public CommentRequest() {}
        
        public CommentRequest(String content) {
            this.content = content;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
} 