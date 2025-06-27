package com.myeden.controller;

import com.myeden.service.WorldService;
import com.myeden.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 世界管理控制器
 * 
 * 功能说明：
 * - 提供世界信息查询API接口
 * - 管理世界背景和设定展示
 * - 提供机器人信息查询接口
 * - 支持配置管理功能
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/world")
public class WorldController {
    
    @Autowired
    private WorldService worldService;
    
    @Autowired
    private ConfigService configService;
    
    /**
     * 获取世界基本信息
     * GET /api/v1/world
     */
    @GetMapping
    public ResponseEntity<EventResponse> getWorldInfo() {
        try {
            WorldService.WorldInfo worldInfo = worldService.getWorldInfo();
            
            if (worldInfo == null) {
                return ResponseEntity.status(500).body(EventResponse.error(500, "获取世界信息失败"));
            }
            
            return ResponseEntity.ok(EventResponse.success(worldInfo, "获取世界信息成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取世界背景信息
     * GET /api/v1/world/background
     */
    @GetMapping("/background")
    public ResponseEntity<EventResponse> getWorldBackground() {
        try {
            WorldService.WorldBackground background = worldService.getWorldBackground();
            
            if (background == null) {
                return ResponseEntity.status(500).body(EventResponse.error(500, "获取世界背景失败"));
            }
            
            return ResponseEntity.ok(EventResponse.success(background, "获取世界背景成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取世界环境信息
     * GET /api/v1/world/environment
     */
    @GetMapping("/environment")
    public ResponseEntity<EventResponse> getWorldEnvironment() {
        try {
            WorldService.WorldEnvironment environment = worldService.getWorldEnvironment();
            
            if (environment == null) {
                return ResponseEntity.status(500).body(EventResponse.error(500, "获取世界环境失败"));
            }
            
            return ResponseEntity.ok(EventResponse.success(environment, "获取世界环境成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取世界活动信息
     * GET /api/v1/world/activities
     */
    @GetMapping("/activities")
    public ResponseEntity<EventResponse> getWorldActivities() {
        try {
            WorldService.WorldActivities activities = worldService.getWorldActivities();
            
            if (activities == null) {
                return ResponseEntity.status(500).body(EventResponse.error(500, "获取世界活动失败"));
            }
            
            return ResponseEntity.ok(EventResponse.success(activities, "获取世界活动成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取世界统计信息
     * GET /api/v1/world/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<EventResponse> getWorldStatistics() {
        try {
            WorldService.WorldStatistics statistics = worldService.getWorldStatistics();
            
            if (statistics == null) {
                return ResponseEntity.status(500).body(EventResponse.error(500, "获取世界统计失败"));
            }
            
            return ResponseEntity.ok(EventResponse.success(statistics, "获取世界统计成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取世界设置信息
     * GET /api/v1/world/settings
     */
    @GetMapping("/settings")
    public ResponseEntity<EventResponse> getWorldSettings() {
        try {
            WorldService.WorldSettings settings = worldService.getWorldSettings();
            
            if (settings == null) {
                return ResponseEntity.status(500).body(EventResponse.error(500, "获取世界设置失败"));
            }
            
            return ResponseEntity.ok(EventResponse.success(settings, "获取世界设置成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取机器人列表
     * GET /api/v1/world/robots
     */
    @GetMapping("/robots")
    public ResponseEntity<EventResponse> getRobotList() {
        try {
            List<WorldService.RobotSummary> robots = worldService.getRobotList();
            
            if (robots == null) {
                return ResponseEntity.status(500).body(EventResponse.error(500, "获取机器人列表失败"));
            }
            
            return ResponseEntity.ok(EventResponse.success(robots, "获取机器人列表成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取机器人详情
     * GET /api/v1/world/robots/{robotId}
     */
    @GetMapping("/robots/{robotId}")
    public ResponseEntity<EventResponse> getRobotDetail(@PathVariable String robotId) {
        try {
            WorldService.RobotDetail robot = worldService.getRobotDetail(robotId);
            
            if (robot == null) {
                return ResponseEntity.status(404).body(EventResponse.error(404, "机器人不存在"));
            }
            
            return ResponseEntity.ok(EventResponse.success(robot, "获取机器人详情成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 重新加载世界配置
     * POST /api/v1/world/reload-config
     */
    @PostMapping("/reload-config")
    public ResponseEntity<EventResponse> reloadWorldConfig() {
        try {
            boolean success = configService.reloadWorldConfig();
            
            if (!success) {
                return ResponseEntity.status(500).body(EventResponse.error(500, "重新加载世界配置失败"));
            }
            
            return ResponseEntity.ok(EventResponse.success(null, "重新加载世界配置成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 重新加载机器人配置
     * POST /api/v1/world/reload-robots
     */
    @PostMapping("/reload-robots")
    public ResponseEntity<EventResponse> reloadRobotConfig() {
        try {
            boolean success = configService.reloadRobotConfig();
            
            if (!success) {
                return ResponseEntity.status(500).body(EventResponse.error(500, "重新加载机器人配置失败"));
            }
            
            return ResponseEntity.ok(EventResponse.success(null, "重新加载机器人配置成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取配置状态
     * GET /api/v1/world/config-status
     */
    @GetMapping("/config-status")
    public ResponseEntity<EventResponse> getConfigStatus() {
        try {
            ConfigService.ConfigStatus status = configService.getConfigStatus();
            
            return ResponseEntity.ok(EventResponse.success(status, "获取配置状态成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
} 