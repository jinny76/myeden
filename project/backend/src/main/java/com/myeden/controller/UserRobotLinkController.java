package com.myeden.controller;

import com.myeden.service.UserRobotLinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import com.myeden.entity.UserRobotLink;

/**
 * 用户机器人链接管理控制器
 * 
 * 功能说明：
 * - 提供用户机器人链接的CRUD API接口
 * - 支持链接状态管理和强度评估
 * - 提供链接统计和分析功能
 * - 支持链接查询和过滤
 * 
 * @author MyEden Team
 * @version 1.0.1
 * @since 2025-01-27
 */
@RestController
@RequestMapping("/api/v1/user-robot-links")
public class UserRobotLinkController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRobotLinkController.class);
    
    @Autowired
    private UserRobotLinkService userRobotLinkService;
    
    /**
     * 创建用户机器人链接
     * @param request 创建链接请求
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<EventResponse> createLink(@RequestBody CreateLinkRequest request) {
        try {
            logger.info("收到创建用户机器人链接请求，机器人ID: {}", request.getRobotId());
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            logger.info("当前认证用户ID: {}", currentUserId);
            logger.info("认证信息: {}", authentication);
            
            // 创建链接（使用当前用户ID，忽略请求中的用户ID）
            UserRobotLinkService.LinkResult result = userRobotLinkService.createLink(currentUserId, request.getRobotId());
            
            logger.info("用户机器人链接创建成功，链接ID: {}", result.getLinkId());
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "链接创建成功",
                result
            ));
            
        } catch (Exception e) {
            logger.error("创建用户机器人链接失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "创建链接失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 删除用户机器人链接
     * @param robotId 机器人ID
     * @return 删除结果
     */
    @DeleteMapping("/{robotId}")
    public ResponseEntity<EventResponse> deleteLink(@PathVariable String robotId) {
        try {
            logger.info("收到删除用户机器人链接请求，机器人ID: {}", robotId);
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // 删除链接
            boolean result = userRobotLinkService.deleteLink(currentUserId, robotId);
            
            if (result) {
                logger.info("用户机器人链接删除成功");
                return ResponseEntity.ok(new EventResponse(
                    200,
                    "链接删除成功",
                    null
                ));
            } else {
                return ResponseEntity.badRequest().body(new EventResponse(
                    400,
                    "链接不存在或删除失败",
                    null
                ));
            }
            
        } catch (Exception e) {
            logger.error("删除用户机器人链接失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "删除链接失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 激活链接
     * @param robotId 机器人ID
     * @return 激活结果
     */
    @PostMapping("/{robotId}/activate")
    public ResponseEntity<EventResponse> activateLink(@PathVariable String robotId) {
        try {
            logger.info("收到激活用户机器人链接请求，机器人ID: {}", robotId);
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // 激活链接
            boolean result = userRobotLinkService.activateLink(currentUserId, robotId);
            
            if (result) {
                logger.info("用户机器人链接激活成功");
                return ResponseEntity.ok(new EventResponse(
                    200,
                    "链接激活成功",
                    null
                ));
            } else {
                return ResponseEntity.badRequest().body(new EventResponse(
                    400,
                    "链接不存在或激活失败",
                    null
                ));
            }
            
        } catch (Exception e) {
            logger.error("激活用户机器人链接失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "激活链接失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 停用链接
     * @param robotId 机器人ID
     * @return 停用结果
     */
    @PostMapping("/{robotId}/deactivate")
    public ResponseEntity<EventResponse> deactivateLink(@PathVariable String robotId) {
        try {
            logger.info("收到停用用户机器人链接请求，机器人ID: {}", robotId);
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // 停用链接
            boolean result = userRobotLinkService.deactivateLink(currentUserId, robotId);
            
            if (result) {
                logger.info("用户机器人链接停用成功");
                return ResponseEntity.ok(new EventResponse(
                    200,
                    "链接停用成功",
                    null
                ));
            } else {
                return ResponseEntity.badRequest().body(new EventResponse(
                    400,
                    "链接不存在或停用失败",
                    null
                ));
            }
            
        } catch (Exception e) {
            logger.error("停用用户机器人链接失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "停用链接失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 更新链接强度
     * @param robotId 机器人ID
     * @param request 更新强度请求
     * @return 更新结果
     */
    @PutMapping("/{robotId}/strength")
    public ResponseEntity<EventResponse> updateLinkStrength(@PathVariable String robotId, @RequestBody UpdateStrengthRequest request) {
        try {
            logger.info("收到更新用户机器人链接强度请求，机器人ID: {}, 强度: {}", robotId, request.getStrength());
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // 更新链接强度
            boolean result = userRobotLinkService.updateLinkStrength(currentUserId, robotId, request.getStrength());
            
            if (result) {
                logger.info("用户机器人链接强度更新成功");
                return ResponseEntity.ok(new EventResponse(
                    200,
                    "链接强度更新成功",
                    null
                ));
            } else {
                return ResponseEntity.badRequest().body(new EventResponse(
                    400,
                    "链接不存在或更新失败",
                    null
                ));
            }
            
        } catch (Exception e) {
            logger.error("更新用户机器人链接强度失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "更新链接强度失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 更新用户-机器人连接对象（如impression字段）
     * @param robotId 机器人ID
     * @param updateData 前端传递的部分更新数据
     * @return 更新结果
     */
    @PutMapping("/{robotId}")
    public ResponseEntity<EventResponse> updateUserRobotLink(
            @PathVariable String robotId,
            @RequestBody UserRobotLink updateData) {
        try {
            // 获取当前用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();

            // 查询现有连接
            Optional<UserRobotLink> linkOpt = userRobotLinkService.getLink(currentUserId, robotId);
            if (linkOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(new EventResponse(404, "连接不存在", null));
            }
            UserRobotLink link = linkOpt.get();

            // 只允许更新impression字段
            link.setImpression(updateData.getImpression());
            // 可根据需要同步更新时间等

            userRobotLinkService.save(link);

            return ResponseEntity.ok(new EventResponse(200, "更新成功", link));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new EventResponse(500, "更新失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 获取用户的链接列表
     * @return 链接列表
     */
    @GetMapping
    public ResponseEntity<EventResponse> getUserLinks() {
        try {
            logger.info("收到获取用户链接列表请求");
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // 获取链接列表
            List<UserRobotLinkService.LinkSummary> links = userRobotLinkService.getUserLinks(currentUserId);
            
            logger.info("获取用户链接列表成功，总数: {}", links.size());
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "获取链接列表成功",
                links
            ));
            
        } catch (Exception e) {
            logger.error("获取用户链接列表失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "获取链接列表失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 获取用户的激活链接列表
     * @return 激活链接列表
     */
    @GetMapping("/active")
    public ResponseEntity<EventResponse> getUserActiveLinks() {
        try {
            logger.info("收到获取用户激活链接列表请求");
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // 获取激活链接列表
            List<UserRobotLinkService.LinkSummary> links = userRobotLinkService.getUserActiveLinks(currentUserId);
            
            logger.info("获取用户激活链接列表成功，总数: {}", links.size());
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "获取激活链接列表成功",
                links
            ));
            
        } catch (Exception e) {
            logger.error("获取用户激活链接列表失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "获取激活链接列表失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 获取链接详情
     * @param robotId 机器人ID
     * @return 链接详情
     */
    @GetMapping("/{robotId}")
    public ResponseEntity<EventResponse> getLinkDetail(@PathVariable String robotId) {
        try {
            logger.info("收到获取链接详情请求，机器人ID: {}", robotId);
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // 获取链接详情
            UserRobotLinkService.LinkDetail detail = userRobotLinkService.getLinkDetail(currentUserId, robotId);
            
            logger.info("获取链接详情成功");
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "获取链接详情成功",
                detail
            ));
            
        } catch (Exception e) {
            logger.error("获取链接详情失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "获取链接详情失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 获取用户最强链接
     * @return 最强链接
     */
    @GetMapping("/strongest")
    public ResponseEntity<EventResponse> getStrongestLink() {
        try {
            logger.info("收到获取用户最强链接请求");
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // 获取最强链接
            UserRobotLinkService.LinkSummary link = userRobotLinkService.getStrongestLink(currentUserId);
            
            if (link != null) {
                logger.info("获取用户最强链接成功");
                return ResponseEntity.ok(new EventResponse(
                    200,
                    "获取最强链接成功",
                    link
                ));
            } else {
                return ResponseEntity.ok(new EventResponse(
                    200,
                    "用户没有链接",
                    null
                ));
            }
            
        } catch (Exception e) {
            logger.error("获取用户最强链接失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "获取最强链接失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 获取用户最活跃链接
     * @return 最活跃链接
     */
    @GetMapping("/most-active")
    public ResponseEntity<EventResponse> getMostActiveLink() {
        try {
            logger.info("收到获取用户最活跃链接请求");
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // 获取最活跃链接
            UserRobotLinkService.LinkSummary link = userRobotLinkService.getMostActiveLink(currentUserId);
            
            if (link != null) {
                logger.info("获取用户最活跃链接成功");
                return ResponseEntity.ok(new EventResponse(
                    200,
                    "获取最活跃链接成功",
                    link
                ));
            } else {
                return ResponseEntity.ok(new EventResponse(
                    200,
                    "用户没有链接",
                    null
                ));
            }
            
        } catch (Exception e) {
            logger.error("获取用户最活跃链接失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "获取最活跃链接失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 获取链接统计信息
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<EventResponse> getLinkStatistics() {
        try {
            logger.info("收到获取用户链接统计请求");
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // 获取统计信息
            UserRobotLinkService.LinkStatistics statistics = userRobotLinkService.getLinkStatistics(currentUserId);
            
            logger.info("获取用户链接统计成功");
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "获取链接统计成功",
                statistics
            ));
            
        } catch (Exception e) {
            logger.error("获取用户链接统计失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "获取链接统计失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 批量为所有用户与所有机器人创建链接（数据迁移/初始化用）
     * @return 迁移结果
     */
    @PostMapping("/migrate-all")
    public ResponseEntity<EventResponse> migrateAllUserRobotLinks() {
        try {
            logger.info("开始批量为所有用户与所有机器人创建链接（数据迁移）");

            // 1. 获取所有用户ID
            List<String> userIds = userRobotLinkService.getAllUserIds();
            // 2. 获取所有机器人ID
            List<String> robotIds = userRobotLinkService.getAllRobotIds();

            int created = 0;
            int skipped = 0;

            for (String userId : userIds) {
                for (String robotId : robotIds) {
                    // 已存在则跳过
                    if (userRobotLinkService.hasLink(userId, robotId)) {
                        skipped++;
                        continue;
                    }
                    try {
                        userRobotLinkService.createLink(userId, robotId);
                        created++;
                    } catch (Exception e) {
                        logger.warn("创建链接失败，userId={}, robotId={}, err={}", userId, robotId, e.getMessage());
                    }
                }
            }

            String msg = String.format("批量创建完成，新增链接%d条，已存在跳过%d条", created, skipped);
            logger.info(msg);
            return ResponseEntity.ok(new EventResponse(200, msg, null));
        } catch (Exception e) {
            logger.error("批量创建用户机器人链接失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(400, "批量创建失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 创建链接请求类
     */
    public static class CreateLinkRequest {
        private String robotId;
        
        public CreateLinkRequest() {}
        
        public CreateLinkRequest(String robotId) {
            this.robotId = robotId;
        }
        
        public String getRobotId() { return robotId; }
        public void setRobotId(String robotId) { this.robotId = robotId; }
    }
    
    /**
     * 更新强度请求类
     */
    public static class UpdateStrengthRequest {
        private Integer strength;
        
        public UpdateStrengthRequest() {}
        
        public UpdateStrengthRequest(Integer strength) {
            this.strength = strength;
        }
        
        public Integer getStrength() { return strength; }
        public void setStrength(Integer strength) { this.strength = strength; }
    }
} 