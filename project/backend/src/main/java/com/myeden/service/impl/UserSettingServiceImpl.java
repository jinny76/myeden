package com.myeden.service.impl;

import com.myeden.entity.UserSetting;
import com.myeden.repository.UserSettingRepository;
import com.myeden.service.UserSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 用户设置服务实现类
 * 
 * 功能说明：
 * - 实现用户个性化设置的业务逻辑
 * - 提供设置的获取、保存、更新和重置功能
 * - 支持默认设置机制，确保新用户有良好的初始体验
 * - 提供部分设置的更新功能
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
public class UserSettingServiceImpl implements UserSettingService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserSettingServiceImpl.class);
    
    @Autowired
    private UserSettingRepository userSettingRepository;
    
    @Override
    public UserSetting getUserSetting(String userId) {
        try {
            logger.info("获取用户设置，用户ID: {}", userId);
            
            if (!StringUtils.hasText(userId)) {
                logger.warn("用户ID为空，返回默认设置");
                return UserSetting.getDefaultSetting("default");
            }
            
            // 查找用户设置
            Optional<UserSetting> settingOpt = userSettingRepository.findByUserId(userId);
            
            if (settingOpt.isPresent()) {
                UserSetting setting = settingOpt.get();
                logger.info("找到用户设置，用户ID: {}, 主题模式: {}", userId, setting.getThemeMode());
                return setting;
            } else {
                // 返回默认设置
                UserSetting defaultSetting = UserSetting.getDefaultSetting(userId);
                logger.info("用户设置不存在，返回默认设置，用户ID: {}", userId);
                return defaultSetting;
            }
            
        } catch (Exception e) {
            logger.error("获取用户设置失败，用户ID: {}", userId, e);
            // 发生异常时返回默认设置
            return UserSetting.getDefaultSetting(userId);
        }
    }
    
    @Override
    public UserSetting saveUserSetting(UserSetting setting) {
        try {
            logger.info("保存用户设置，用户ID: {}", setting.getUserId());
            
            if (setting == null || !StringUtils.hasText(setting.getUserId())) {
                throw new IllegalArgumentException("用户设置或用户ID不能为空");
            }
            
            // 验证设置参数
            setting.validate();
            
            // 更新时间戳
            setting.updateTimestamp();
            
            // 保存设置
            UserSetting savedSetting = userSettingRepository.save(setting);
            
            logger.info("用户设置保存成功，用户ID: {}, 主题模式: {}", 
                       savedSetting.getUserId(), savedSetting.getThemeMode());
            
            return savedSetting;
            
        } catch (Exception e) {
            logger.error("保存用户设置失败，用户ID: {}", setting != null ? setting.getUserId() : "null", e);
            throw e;
        }
    }
    
    @Override
    public UserSetting updateUserSetting(String userId, UserSetting setting) {
        try {
            logger.info("更新用户设置，用户ID: {}", userId);
            
            if (!StringUtils.hasText(userId)) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            
            // 获取现有设置
            UserSetting existingSetting = getUserSetting(userId);
            
            // 只更新非null字段
            if (setting.getThemeMode() != null) {
                existingSetting.setThemeMode(setting.getThemeMode());
            }
            if (setting.getNotifyUserOnline() != null) {
                existingSetting.setNotifyUserOnline(setting.getNotifyUserOnline());
            }
            if (setting.getNotifyPostPublished() != null) {
                existingSetting.setNotifyPostPublished(setting.getNotifyPostPublished());
            }
            if (setting.getNotifyCommentReceived() != null) {
                existingSetting.setNotifyCommentReceived(setting.getNotifyCommentReceived());
            }
            if (setting.getNotifyLikeReceived() != null) {
                existingSetting.setNotifyLikeReceived(setting.getNotifyLikeReceived());
            }
            if (setting.getNotifyRobotInteraction() != null) {
                existingSetting.setNotifyRobotInteraction(setting.getNotifyRobotInteraction());
            }
            if (setting.getLanguage() != null) {
                existingSetting.setLanguage(setting.getLanguage());
            }
            if (setting.getTimezone() != null) {
                existingSetting.setTimezone(setting.getTimezone());
            }
            
            // 验证并保存
            existingSetting.validate();
            existingSetting.updateTimestamp();
            
            UserSetting updatedSetting = userSettingRepository.save(existingSetting);
            
            logger.info("用户设置更新成功，用户ID: {}, 主题模式: {}", 
                       updatedSetting.getUserId(), updatedSetting.getThemeMode());
            
            return updatedSetting;
            
        } catch (Exception e) {
            logger.error("更新用户设置失败，用户ID: {}", userId, e);
            throw e;
        }
    }
    
    @Override
    public UserSetting updateThemeMode(String userId, String themeMode) {
        try {
            logger.info("更新用户主题模式，用户ID: {}, 主题模式: {}", userId, themeMode);
            
            if (!StringUtils.hasText(userId)) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            
            if (!StringUtils.hasText(themeMode)) {
                throw new IllegalArgumentException("主题模式不能为空");
            }
            
            // 验证主题模式
            if (!themeMode.equals("light") && !themeMode.equals("dark") && !themeMode.equals("auto")) {
                throw new IllegalArgumentException("无效的主题模式: " + themeMode);
            }
            
            // 获取现有设置
            UserSetting existingSetting = getUserSetting(userId);
            
            // 更新主题模式
            existingSetting.setThemeMode(themeMode);
            existingSetting.updateTimestamp();
            
            UserSetting updatedSetting = userSettingRepository.save(existingSetting);
            
            logger.info("用户主题模式更新成功，用户ID: {}, 主题模式: {}", 
                       updatedSetting.getUserId(), updatedSetting.getThemeMode());
            
            return updatedSetting;
            
        } catch (Exception e) {
            logger.error("更新用户主题模式失败，用户ID: {}, 主题模式: {}", userId, themeMode, e);
            throw e;
        }
    }
    
    @Override
    public UserSetting updateNotificationSetting(String userId, String notificationType, Boolean enabled) {
        try {
            logger.info("更新用户通知设置，用户ID: {}, 通知类型: {}, 启用状态: {}", 
                       userId, notificationType, enabled);
            
            if (!StringUtils.hasText(userId)) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            
            if (!StringUtils.hasText(notificationType)) {
                throw new IllegalArgumentException("通知类型不能为空");
            }
            
            if (enabled == null) {
                throw new IllegalArgumentException("启用状态不能为空");
            }
            
            // 获取现有设置
            UserSetting existingSetting = getUserSetting(userId);
            
            // 根据通知类型更新相应设置
            switch (notificationType.toLowerCase()) {
                case "user_online":
                    existingSetting.setNotifyUserOnline(enabled);
                    break;
                case "post_published":
                    existingSetting.setNotifyPostPublished(enabled);
                    break;
                case "comment_received":
                    existingSetting.setNotifyCommentReceived(enabled);
                    break;
                case "like_received":
                    existingSetting.setNotifyLikeReceived(enabled);
                    break;
                case "robot_interaction":
                    existingSetting.setNotifyRobotInteraction(enabled);
                    break;
                default:
                    throw new IllegalArgumentException("无效的通知类型: " + notificationType);
            }
            
            existingSetting.updateTimestamp();
            
            UserSetting updatedSetting = userSettingRepository.save(existingSetting);
            
            logger.info("用户通知设置更新成功，用户ID: {}, 通知类型: {}, 启用状态: {}", 
                       updatedSetting.getUserId(), notificationType, enabled);
            
            return updatedSetting;
            
        } catch (Exception e) {
            logger.error("更新用户通知设置失败，用户ID: {}, 通知类型: {}", userId, notificationType, e);
            throw e;
        }
    }
    
    @Override
    public UserSetting resetUserSetting(String userId) {
        try {
            logger.info("重置用户设置，用户ID: {}", userId);
            
            if (!StringUtils.hasText(userId)) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            
            // 创建默认设置
            UserSetting defaultSetting = UserSetting.getDefaultSetting(userId);
            
            // 保存默认设置
            UserSetting savedSetting = userSettingRepository.save(defaultSetting);
            
            logger.info("用户设置重置成功，用户ID: {}", savedSetting.getUserId());
            
            return savedSetting;
            
        } catch (Exception e) {
            logger.error("重置用户设置失败，用户ID: {}", userId, e);
            throw e;
        }
    }
    
    @Override
    public boolean deleteUserSetting(String userId) {
        try {
            logger.info("删除用户设置，用户ID: {}", userId);
            
            if (!StringUtils.hasText(userId)) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            
            // 检查设置是否存在
            if (!userSettingRepository.existsByUserId(userId)) {
                logger.warn("用户设置不存在，用户ID: {}", userId);
                return false;
            }
            
            // 删除设置
            userSettingRepository.deleteByUserId(userId);
            
            logger.info("用户设置删除成功，用户ID: {}", userId);
            
            return true;
            
        } catch (Exception e) {
            logger.error("删除用户设置失败，用户ID: {}", userId, e);
            throw e;
        }
    }
    
    @Override
    public boolean hasCustomSetting(String userId) {
        try {
            if (!StringUtils.hasText(userId)) {
                return false;
            }
            
            return userSettingRepository.existsByUserId(userId);
            
        } catch (Exception e) {
            logger.error("检查用户自定义设置失败，用户ID: {}", userId, e);
            return false;
        }
    }
} 