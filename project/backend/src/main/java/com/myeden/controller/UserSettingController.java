package com.myeden.controller;

import com.myeden.entity.UserSetting;
import com.myeden.service.UserSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户设置控制器
 * 
 * 功能说明：
 * - 提供用户个性化设置的REST API接口
 * - 支持设置的获取、保存、更新和重置
 * - 提供主题模式和通知设置的专门接口
 * - 确保用户只能操作自己的设置
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/user-settings")
public class UserSettingController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserSettingController.class);
    
    @Autowired
    private UserSettingService userSettingService;
    
    /**
     * 获取当前用户的设置
     * @return 用户设置信息
     */
    @GetMapping
    public ResponseEntity<EventResponse> getUserSetting() {
        try {
            String userId = getCurrentUserId();
            logger.info("获取用户设置，用户ID: {}", userId);
            
            UserSetting setting = userSettingService.getUserSetting(userId);
            
            logger.info("获取用户设置成功，用户ID: {}, 主题模式: {}", 
                       setting.getUserId(), setting.getThemeMode());
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "获取用户设置成功",
                setting
            ));
            
        } catch (Exception e) {
            logger.error("获取用户设置失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "获取用户设置失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 保存或更新当前用户的设置
     * @param setting 用户设置信息
     * @return 保存后的设置信息
     */
    @PostMapping
    public ResponseEntity<EventResponse> saveUserSetting(@RequestBody UserSetting setting) {
        try {
            String userId = getCurrentUserId();
            logger.info("保存用户设置，用户ID: {}", userId);
            
            if (setting == null) {
                return ResponseEntity.badRequest().body(new EventResponse(
                    400,
                    "用户设置不能为空",
                    null
                ));
            }
            
            // 强制设置用户ID为当前用户
            setting.setUserId(userId);
            
            UserSetting savedSetting = userSettingService.saveUserSetting(setting);
            
            logger.info("保存用户设置成功，用户ID: {}, 主题模式: {}", 
                       savedSetting.getUserId(), savedSetting.getThemeMode());
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "保存用户设置成功",
                savedSetting
            ));
            
        } catch (Exception e) {
            logger.error("保存用户设置失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "保存用户设置失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 更新当前用户设置的部分字段
     * @param setting 要更新的设置字段（只更新非null字段）
     * @return 更新后的设置信息
     */
    @PutMapping
    public ResponseEntity<EventResponse> updateUserSetting(@RequestBody UserSetting setting) {
        try {
            String userId = getCurrentUserId();
            logger.info("更新用户设置，用户ID: {}", userId);
            
            if (setting == null) {
                return ResponseEntity.badRequest().body(new EventResponse(
                    400,
                    "用户设置不能为空",
                    null
                ));
            }
            
            UserSetting updatedSetting = userSettingService.updateUserSetting(userId, setting);
            
            logger.info("更新用户设置成功，用户ID: {}, 主题模式: {}", 
                       updatedSetting.getUserId(), updatedSetting.getThemeMode());
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "更新用户设置成功",
                updatedSetting
            ));
            
        } catch (Exception e) {
            logger.error("更新用户设置失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "更新用户设置失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 更新当前用户的主题模式
     * @param themeMode 主题模式（light/dark/auto）
     * @return 更新后的设置信息
     */
    @PutMapping("/theme")
    public ResponseEntity<EventResponse> updateThemeMode(@RequestParam String themeMode) {
        try {
            String userId = getCurrentUserId();
            logger.info("更新用户主题模式，用户ID: {}, 主题模式: {}", userId, themeMode);
            
            if (themeMode == null || themeMode.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new EventResponse(
                    400,
                    "主题模式不能为空",
                    null
                ));
            }
            
            UserSetting updatedSetting = userSettingService.updateThemeMode(userId, themeMode);
            
            logger.info("更新用户主题模式成功，用户ID: {}, 主题模式: {}", 
                       updatedSetting.getUserId(), updatedSetting.getThemeMode());
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "更新主题模式成功",
                updatedSetting
            ));
            
        } catch (Exception e) {
            logger.error("更新用户主题模式失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "更新主题模式失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 更新当前用户的通知设置
     * @param notificationType 通知类型
     * @param enabled 是否启用
     * @return 更新后的设置信息
     */
    @PutMapping("/notification")
    public ResponseEntity<EventResponse> updateNotificationSetting(
            @RequestParam String notificationType,
            @RequestParam Boolean enabled) {
        try {
            String userId = getCurrentUserId();
            logger.info("更新用户通知设置，用户ID: {}, 通知类型: {}, 启用状态: {}", 
                       userId, notificationType, enabled);
            
            if (notificationType == null || notificationType.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new EventResponse(
                    400,
                    "通知类型不能为空",
                    null
                ));
            }
            
            if (enabled == null) {
                return ResponseEntity.badRequest().body(new EventResponse(
                    400,
                    "启用状态不能为空",
                    null
                ));
            }
            
            UserSetting updatedSetting = userSettingService.updateNotificationSetting(userId, notificationType, enabled);
            
            logger.info("更新用户通知设置成功，用户ID: {}, 通知类型: {}, 启用状态: {}", 
                       updatedSetting.getUserId(), notificationType, enabled);
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "更新通知设置成功",
                updatedSetting
            ));
            
        } catch (Exception e) {
            logger.error("更新用户通知设置失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "更新通知设置失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 重置当前用户的设置为默认值
     * @return 重置后的设置信息
     */
    @PostMapping("/reset")
    public ResponseEntity<EventResponse> resetUserSetting() {
        try {
            String userId = getCurrentUserId();
            logger.info("重置用户设置，用户ID: {}", userId);
            
            UserSetting resetSetting = userSettingService.resetUserSetting(userId);
            
            logger.info("重置用户设置成功，用户ID: {}", resetSetting.getUserId());
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "重置用户设置成功",
                resetSetting
            ));
            
        } catch (Exception e) {
            logger.error("重置用户设置失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "重置用户设置失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 删除当前用户的设置（恢复为默认设置）
     * @return 删除结果
     */
    @DeleteMapping
    public ResponseEntity<EventResponse> deleteUserSetting() {
        try {
            String userId = getCurrentUserId();
            logger.info("删除用户设置，用户ID: {}", userId);
            
            boolean deleted = userSettingService.deleteUserSetting(userId);
            
            if (deleted) {
                logger.info("删除用户设置成功，用户ID: {}", userId);
                return ResponseEntity.ok(new EventResponse(
                    200,
                    "删除用户设置成功",
                    null
                ));
            } else {
                logger.warn("用户设置不存在，用户ID: {}", userId);
                return ResponseEntity.ok(new EventResponse(
                    200,
                    "用户设置不存在",
                    null
                ));
            }
            
        } catch (Exception e) {
            logger.error("删除用户设置失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "删除用户设置失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 检查当前用户是否有自定义设置
     * @return 是否有自定义设置
     */
    @GetMapping("/has-custom")
    public ResponseEntity<EventResponse> hasCustomSetting() {
        try {
            String userId = getCurrentUserId();
            logger.info("检查用户自定义设置，用户ID: {}", userId);
            
            boolean hasCustom = userSettingService.hasCustomSetting(userId);
            
            logger.info("检查用户自定义设置完成，用户ID: {}, 有自定义设置: {}", userId, hasCustom);
            
            return ResponseEntity.ok(new EventResponse(
                200,
                "检查完成",
                hasCustom
            ));
            
        } catch (Exception e) {
            logger.error("检查用户自定义设置失败", e);
            return ResponseEntity.badRequest().body(new EventResponse(
                400,
                "检查失败: " + e.getMessage(),
                null
            ));
        }
    }
    
    /**
     * 获取当前用户ID
     * @return 当前用户ID
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
} 