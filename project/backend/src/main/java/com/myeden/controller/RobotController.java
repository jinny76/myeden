package com.myeden.controller;

import com.myeden.entity.Robot;
import com.myeden.service.RobotBehaviorService;
import com.myeden.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 机器人行为管理控制器
 * 提供AI机器人行为触发和管理的API接口
 * 
 * @author MyEden Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/robots")
public class RobotController {
    
    @Autowired
    private RobotBehaviorService robotBehaviorService;
    
    @Autowired
    private WorldService worldService;
    
    /**
     * 获取机器人列表
     * @param isActive 是否只返回激活的机器人
     * @return 机器人列表
     */
    @GetMapping
    public ResponseEntity<EventResponse> getRobotList(@RequestParam(required = false) Boolean isActive) {
        try {
            List<WorldService.RobotSummary> robots = worldService.getRobotList();
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
            // 调用RobotBehaviorService的刷新方法
            robotBehaviorService.refreshRobotActiveStatus();
            return ResponseEntity.ok(EventResponse.success(null, "刷新机器人在线状态成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error("刷新机器人在线状态失败: " + e.getMessage()));
        }
    }
} 