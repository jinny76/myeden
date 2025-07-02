package com.myeden.controller;

import com.myeden.entity.Robot;
import com.myeden.service.RobotBehaviorService;
import com.myeden.service.WorldService;
import com.myeden.service.PostService;
import com.myeden.service.PromptService;
import com.myeden.service.RobotService;
import com.myeden.service.JwtService;
import com.myeden.repository.RobotRepository;
import com.myeden.dto.RobotEditRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 机器人行为管理控制器
 * 提供AI机器人行为触发和管理的API接口
 * 
 * @author MyEden Team
 * @version 1.0.0
 */
@Tag(name = "机器人相关接口")
@RestController
@RequestMapping("/api/v1/robots")
public class RobotController {
    
    private static final Logger logger = LoggerFactory.getLogger(RobotController.class);
    
    @Autowired
    private RobotBehaviorService robotBehaviorService;
    
    @Autowired
    private WorldService worldService;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private PromptService promptService;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private RobotService robotService;
    
    @Autowired
    private JwtService jwtService;
    
    /**
     * 获取机器人列表
     * @param isActive 是否只返回激活的机器人
     * @return 机器人列表
     */
    @GetMapping
    public ResponseEntity<EventResponse> getRobotList(@RequestParam(required = false) Boolean isActive) {
        try {
            List<WorldService.RobotSummary> robots = worldService.getRobotList();
            // 过滤掉已删除的机器人
            robots = robots.stream()
                .filter(r -> r.getIsDeleted() == null || !r.getIsDeleted())
                .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(EventResponse.success(robots, "获取机器人列表成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error("获取机器人列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取机器人详情
     * @param robotId 机器人ID
     * @return 机器人详细信息
     */
    @GetMapping("/{robotId}")
    public ResponseEntity<EventResponse> getRobotDetail(@PathVariable String robotId) {
        try {
            WorldService.RobotDetail robot = worldService.getRobotDetail(robotId);
            if (robot != null) {
                return ResponseEntity.ok(EventResponse.success(robot, "获取机器人详情成功"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error("获取机器人详情失败: " + e.getMessage()));
        }
    }
    
    /**
     * 触发机器人发布动态
     * @param robotId 机器人ID
     * @return 触发结果
     */
    @PostMapping("/{robotId}/posts")
    public ResponseEntity<EventResponse> triggerRobotPost(@PathVariable String robotId) {
        try {
            boolean success = robotBehaviorService.triggerRobotPost(robotId);
            if (success) {
                return ResponseEntity.ok(EventResponse.success(null, "机器人发布动态成功"));
            } else {
                return ResponseEntity.badRequest().body(EventResponse.error("机器人发布动态失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error("触发机器人发布动态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 触发机器人发表评论
     * @param robotId 机器人ID
     * @param postId 动态ID
     * @return 触发结果
     */
    @PostMapping("/{robotId}/comments")
    public ResponseEntity<EventResponse> triggerRobotComment(
            @PathVariable String robotId,
            @RequestParam String postId) {
        try {
            boolean success = robotBehaviorService.triggerRobotComment(robotId, postId);
            if (success) {
                return ResponseEntity.ok(EventResponse.success(null, "机器人发表评论成功"));
            } else {
                return ResponseEntity.badRequest().body(EventResponse.error("机器人发表评论失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error("触发机器人发表评论失败: " + e.getMessage()));
        }
    }
    
    /**
     * 触发机器人回复评论
     * @param robotId 机器人ID
     * @param commentId 评论ID
     * @return 触发结果
     */
    @PostMapping("/{robotId}/replies")
    public ResponseEntity<EventResponse> triggerRobotReply(
            @PathVariable String robotId,
            @RequestParam String commentId) {
        try {
            boolean success = robotBehaviorService.triggerRobotReply(robotId, commentId);
            if (success) {
                return ResponseEntity.ok(EventResponse.success(null, "机器人回复评论成功"));
            } else {
                return ResponseEntity.badRequest().body(EventResponse.error("机器人回复评论失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error("触发机器人回复评论失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取机器人今日行为统计
     * @param robotId 机器人ID
     * @return 行为统计信息
     */
    @GetMapping("/{robotId}/stats")
    public ResponseEntity<EventResponse> getRobotDailyStats(@PathVariable String robotId) {
        try {
            String stats = robotBehaviorService.getRobotDailyStats(robotId);
            return ResponseEntity.ok(EventResponse.success(stats, "获取机器人统计成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error("获取机器人统计失败: " + e.getMessage()));
        }
    }
    
    /**
     * 重置机器人行为统计
     * @param robotId 机器人ID
     * @return 重置结果
     */
    @PostMapping("/{robotId}/stats/reset")
    public ResponseEntity<EventResponse> resetRobotDailyStats(@PathVariable String robotId) {
        try {
            robotBehaviorService.resetRobotDailyStats(robotId);
            return ResponseEntity.ok(EventResponse.success(null, "重置机器人统计成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error("重置机器人统计失败: " + e.getMessage()));
        }
    }
    
    /**
     * 启动机器人行为调度
     * @return 启动结果
     */
    @PostMapping("/scheduler/start")
    public ResponseEntity<EventResponse> startBehaviorScheduler() {
        try {
            robotBehaviorService.startBehaviorScheduler();
            return ResponseEntity.ok(EventResponse.success(null, "启动机器人行为调度成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error("启动机器人行为调度失败: " + e.getMessage()));
        }
    }
    
    /**
     * 停止机器人行为调度
     * @return 停止结果
     */
    @PostMapping("/scheduler/stop")
    public ResponseEntity<EventResponse> stopBehaviorScheduler() {
        try {
            robotBehaviorService.stopBehaviorScheduler();
            return ResponseEntity.ok(EventResponse.success(null, "停止机器人行为调度成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error("停止机器人行为调度失败: " + e.getMessage()));
        }
    }
    
    /**
     * 手动刷新机器人在线状态
     * 根据机器人的活跃时间配置更新所有机器人的在线状态
     * @return 刷新结果
     */
    @PostMapping("/status/refresh")
    public ResponseEntity<EventResponse> refreshRobotStatus() {
        try {
            robotBehaviorService.refreshRobotActiveStatus();
            return ResponseEntity.ok(EventResponse.success(null, "刷新机器人在线状态成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error("刷新机器人在线状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取用户拥有的机器人列表
     * @return 用户机器人列表
     */
    @GetMapping("/my-robots")
    @Operation(summary = "获取我的机器人列表", description = "获取当前用户拥有的所有机器人（包括已删除的）")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未提供有效的认证token")
    })
    public ResponseEntity<EventResponse> getMyRobots(HttpServletRequest request) {
        try {
            // 从请求头获取token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "未提供有效的认证token"));
            }
            
            String token = authHeader.substring(7);
            String userId = jwtService.extractUserId(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "Token无效"));
            }
            
            List<Robot> robots = robotService.getRobotsByOwner(userId);
            return ResponseEntity.ok(EventResponse.success(robots, "获取机器人列表成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取机器人编辑信息
     * @param robotId 机器人ID
     * @return 机器人编辑信息
     */
    @GetMapping("/{robotId}/edit")
    @Operation(summary = "获取机器人编辑信息", description = "获取指定机器人的完整编辑信息")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未提供有效的认证token"),
        @ApiResponse(responseCode = "403", description = "无权访问此机器人"),
        @ApiResponse(responseCode = "404", description = "机器人不存在")
    })
    public ResponseEntity<EventResponse> getRobotForEdit(
            @PathVariable String robotId,
            HttpServletRequest request) {
        try {
            // 从请求头获取token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "未提供有效的认证token"));
            }
            
            String token = authHeader.substring(7);
            String userId = jwtService.extractUserId(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "Token无效"));
            }
            
            Robot robot = robotService.getRobotByRobotId(robotId);
            if (robot == null) {
                return ResponseEntity.status(404).body(EventResponse.error(404, "机器人不存在"));
            }
            
            // 检查权限
            if (!robot.isOwnedBy(userId)) {
                return ResponseEntity.status(403).body(EventResponse.error(403, "无权访问此机器人"));
            }
            
            return ResponseEntity.ok(EventResponse.success(robot, "获取机器人编辑信息成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 创建新机器人
     * @param request 机器人创建请求
     * @return 创建结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建新机器人", description = "创建新的机器人")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "401", description = "未提供有效的认证token"),
        @ApiResponse(responseCode = "400", description = "参数错误")
    })
    public ResponseEntity<EventResponse> createRobot(
            @RequestBody RobotEditRequest request,
            HttpServletRequest httpRequest) {
        try {
            logger.info("开始创建机器人，请求URL: {}", httpRequest.getRequestURI());
            
            // 从请求头获取token
            String authHeader = httpRequest.getHeader("Authorization");
            logger.debug("Authorization头: {}", authHeader);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Authorization头不存在或格式不正确");
                return ResponseEntity.status(401).body(EventResponse.error(401, "未提供有效的认证token"));
            }
            
            String token = authHeader.substring(7);
            logger.debug("提取的token: {}", token.substring(0, Math.min(20, token.length())) + "...");
            
            String userId = jwtService.extractUserId(token);
            logger.info("从token中提取的用户ID: {}", userId);
            
            if (userId == null) {
                logger.warn("Token无效，无法提取用户ID");
                return ResponseEntity.status(401).body(EventResponse.error(401, "Token无效"));
            }
            
            // 生成机器人ID
            String robotId = "robot_" + System.currentTimeMillis();
            
            // 创建机器人
            Robot robot = request.toRobot(robotId, userId);
            Robot savedRobot = robotService.saveRobot(robot);
            
            return ResponseEntity.ok(EventResponse.success(savedRobot, "机器人创建成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 更新机器人信息
     * @param robotId 机器人ID
     * @param request 机器人更新请求
     * @return 更新结果
     */
    @PutMapping("/{robotId}/update")
    @Operation(summary = "更新机器人信息", description = "整体更新机器人的所有信息")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "401", description = "未提供有效的认证token"),
        @ApiResponse(responseCode = "403", description = "无权编辑此机器人"),
        @ApiResponse(responseCode = "404", description = "机器人不存在")
    })
    public ResponseEntity<EventResponse> updateRobot(
            @PathVariable String robotId,
            @RequestBody RobotEditRequest request,
            HttpServletRequest httpRequest) {
        try {
            // 从请求头获取token
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "未提供有效的认证token"));
            }
            
            String token = authHeader.substring(7);
            String userId = jwtService.extractUserId(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "Token无效"));
            }
            
            // 获取现有机器人
            Robot existingRobot = robotService.getRobotByRobotId(robotId);
            if (existingRobot == null) {
                return ResponseEntity.status(404).body(EventResponse.error(404, "机器人不存在"));
            }
            
            // 检查权限
            if (!existingRobot.isOwnedBy(userId)) {
                return ResponseEntity.status(403).body(EventResponse.error(403, "无权编辑此机器人"));
            }
            
            // 更新机器人信息
            request.updateRobot(existingRobot);
            Robot updatedRobot = robotService.saveRobot(existingRobot);
            
            return ResponseEntity.ok(EventResponse.success(updatedRobot, "机器人更新成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 软删除机器人
     * @param robotId 机器人ID
     * @return 删除结果
     */
    @DeleteMapping("/{robotId}")
    @Operation(summary = "删除机器人", description = "软删除机器人（标记为已删除）")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "401", description = "未提供有效的认证token"),
        @ApiResponse(responseCode = "403", description = "无权删除此机器人"),
        @ApiResponse(responseCode = "404", description = "机器人不存在")
    })
    public ResponseEntity<EventResponse> deleteRobot(
            @PathVariable String robotId,
            HttpServletRequest request) {
        try {
            // 从请求头获取token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "未提供有效的认证token"));
            }
            
            String token = authHeader.substring(7);
            String userId = jwtService.extractUserId(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "Token无效"));
            }
            
            // 获取机器人
            Robot robot = robotService.getRobotByRobotId(robotId);
            if (robot == null) {
                return ResponseEntity.status(404).body(EventResponse.error(404, "机器人不存在"));
            }
            
            // 检查权限
            if (!robot.isOwnedBy(userId)) {
                return ResponseEntity.status(403).body(EventResponse.error(403, "无权删除此机器人"));
            }
            
            // 软删除
            robot.softDelete();
            robotService.saveRobot(robot);
            
            return ResponseEntity.ok(EventResponse.success(null, "机器人删除成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 恢复机器人
     * @param robotId 机器人ID
     * @return 恢复结果
     */
    @PostMapping("/{robotId}/restore")
    @Operation(summary = "恢复机器人", description = "恢复已删除的机器人")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "恢复成功"),
        @ApiResponse(responseCode = "401", description = "未提供有效的认证token"),
        @ApiResponse(responseCode = "403", description = "无权恢复此机器人"),
        @ApiResponse(responseCode = "404", description = "机器人不存在")
    })
    public ResponseEntity<EventResponse> restoreRobot(
            @PathVariable String robotId,
            HttpServletRequest request) {
        try {
            // 从请求头获取token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "未提供有效的认证token"));
            }
            
            String token = authHeader.substring(7);
            String userId = jwtService.extractUserId(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "Token无效"));
            }
            
            // 获取机器人
            Robot robot = robotService.getRobotByRobotId(robotId);
            if (robot == null) {
                return ResponseEntity.status(404).body(EventResponse.error(404, "机器人不存在"));
            }
            
            // 检查权限
            if (!robot.isOwnedBy(userId)) {
                return ResponseEntity.status(403).body(EventResponse.error(403, "无权恢复此机器人"));
            }
            
            // 恢复机器人
            robot.restore();
            Robot restoredRobot = robotService.saveRobot(robot);
            
            return ResponseEntity.ok(EventResponse.success(restoredRobot, "机器人恢复成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 复制机器人
     * @param robotId 机器人ID
     * @param request 复制请求
     * @return 复制结果
     */
    @PostMapping("/{robotId}/copy")
    @Operation(summary = "复制机器人", description = "复制现有机器人创建新的机器人")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "复制成功"),
        @ApiResponse(responseCode = "401", description = "未提供有效的认证token"),
        @ApiResponse(responseCode = "403", description = "无权复制此机器人"),
        @ApiResponse(responseCode = "404", description = "机器人不存在")
    })
    public ResponseEntity<EventResponse> copyRobot(
            @PathVariable String robotId,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            // 从请求头获取token
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "未提供有效的认证token"));
            }
            
            String token = authHeader.substring(7);
            String userId = jwtService.extractUserId(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "Token无效"));
            }
            
            // 获取原机器人
            Robot originalRobot = robotService.getRobotByRobotId(robotId);
            if (originalRobot == null) {
                return ResponseEntity.status(404).body(EventResponse.error(404, "机器人不存在"));
            }
            
            // 检查权限
            if (!originalRobot.isOwnedBy(userId)) {
                return ResponseEntity.status(403).body(EventResponse.error(403, "无权复制此机器人"));
            }
            
            // 获取新名称
            String newName = request.get("newName");
            if (newName == null || newName.trim().isEmpty()) {
                newName = originalRobot.getName() + " (副本)";
            }
            
            // 创建新机器人
            String newRobotId = "robot_" + System.currentTimeMillis();
            Robot newRobot = new Robot(newRobotId, newName, userId);
            
            // 复制所有属性
            newRobot.setAvatar(originalRobot.getAvatar());
            newRobot.setGender(originalRobot.getGender());
            newRobot.setAge(originalRobot.getAge());
            newRobot.setDescription(originalRobot.getDescription());
            newRobot.setPersonality(originalRobot.getPersonality());
            newRobot.setMbti(originalRobot.getMbti());
            newRobot.setBloodType(originalRobot.getBloodType());
            newRobot.setZodiac(originalRobot.getZodiac());
            newRobot.setLocation(originalRobot.getLocation());
            newRobot.setOccupation(originalRobot.getOccupation());
            newRobot.setBackground(originalRobot.getBackground());
            newRobot.setEducation(originalRobot.getEducation());
            newRobot.setRelationship(originalRobot.getRelationship());
            newRobot.setFamily(originalRobot.getFamily());
            newRobot.setTraits(originalRobot.getTraits());
            newRobot.setInterests(originalRobot.getInterests());
            newRobot.setSpeakingStyle(originalRobot.getSpeakingStyle());
            newRobot.setBehaviorPatterns(originalRobot.getBehaviorPatterns());
            newRobot.setReplySpeed(originalRobot.getReplySpeed());
            newRobot.setReplyFrequency(originalRobot.getReplyFrequency());
            newRobot.setShareFrequency(originalRobot.getShareFrequency());
            newRobot.setActiveHours(originalRobot.getActiveHours());
            newRobot.setTopics(originalRobot.getTopics());
            newRobot.setIsActive(false); // 新复制的机器人默认不激活
            
            Robot savedRobot = robotService.saveRobot(newRobot);
            
            return ResponseEntity.ok(EventResponse.success(savedRobot, "机器人复制成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 上传机器人头像
     * @param robotId 机器人ID
     * @param file 头像文件
     * @return 上传结果
     */
    @PostMapping("/{robotId}/avatar")
    @Operation(summary = "上传机器人头像", description = "上传机器人的头像图片")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "上传成功"),
        @ApiResponse(responseCode = "401", description = "未提供有效的认证token"),
        @ApiResponse(responseCode = "403", description = "无权上传此机器人头像"),
        @ApiResponse(responseCode = "404", description = "机器人不存在"),
        @ApiResponse(responseCode = "400", description = "文件格式错误或上传失败")
    })
    public ResponseEntity<EventResponse> uploadRobotAvatar(
            @PathVariable String robotId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            // 从请求头获取token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "未提供有效的认证token"));
            }
            
            String token = authHeader.substring(7);
            String userId = jwtService.extractUserId(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "Token无效"));
            }
            
            // 检查是否为临时机器人ID（创建模式）
            boolean isTempRobot = robotId.startsWith("temp_");
            
            if (!isTempRobot) {
                // 编辑模式：获取现有机器人并检查权限
                Robot robot = robotService.getRobotByRobotId(robotId);
                if (robot == null) {
                    return ResponseEntity.status(404).body(EventResponse.error(404, "机器人不存在"));
                }
                
                // 检查权限
                if (!robot.isOwnedBy(userId)) {
                    return ResponseEntity.status(403).body(EventResponse.error(403, "无权上传此机器人头像"));
                }
            }
            
            // 执行头像上传
            String avatarUrl = robotService.uploadAvatar(robotId, file);
            
            return ResponseEntity.ok(EventResponse.success(Map.of("avatarUrl", avatarUrl), "头像上传成功"));
            
        } catch (Exception e) {
            logger.error("头像上传失败", e);
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
} 