package com.myeden.service;

import com.myeden.entity.UserSetting;

/**
 * 用户设置服务接口
 * 
 * 功能说明：
 * - 提供用户个性化设置的业务逻辑
 * - 支持设置的获取、保存、更新和重置
 * - 提供默认设置机制
 * - 支持部分设置的更新
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface UserSettingService {
    
    /**
     * 获取用户设置（如无则返回默认设置）
     * @param userId 用户ID
     * @return 用户设置
     */
    UserSetting getUserSetting(String userId);
    
    /**
     * 保存或更新用户设置
     * @param setting 用户设置
     * @return 保存后的设置
     */
    UserSetting saveUserSetting(UserSetting setting);
    
    /**
     * 更新用户设置的部分字段
     * @param userId 用户ID
     * @param setting 要更新的设置（只更新非null字段）
     * @return 更新后的设置
     */
    UserSetting updateUserSetting(String userId, UserSetting setting);
    
    /**
     * 更新主题模式
     * @param userId 用户ID
     * @param themeMode 主题模式
     * @return 更新后的设置
     */
    UserSetting updateThemeMode(String userId, String themeMode);
    
    /**
     * 更新通知设置
     * @param userId 用户ID
     * @param notificationType 通知类型
     * @param enabled 是否启用
     * @return 更新后的设置
     */
    UserSetting updateNotificationSetting(String userId, String notificationType, Boolean enabled);
    
    /**
     * 重置用户设置为默认值
     * @param userId 用户ID
     * @return 重置后的设置
     */
    UserSetting resetUserSetting(String userId);
    
    /**
     * 删除用户设置
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteUserSetting(String userId);
    
    /**
     * 检查用户是否有自定义设置
     * @param userId 用户ID
     * @return 是否有自定义设置
     */
    boolean hasCustomSetting(String userId);
} 