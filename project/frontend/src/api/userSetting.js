import request from '@/utils/request'

/**
 * 用户设置API服务, request已经有了前缀/api/v1, 所以这里不需要再添加
 * 
 * 功能说明：
 * - 提供用户个性化设置的API接口
 * - 支持设置的获取、保存、更新和重置
 * - 与后端UserSetting服务交互
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

/**
 * 获取当前用户的设置
 * @returns {Promise} 用户设置信息
 */
export function getUserSetting() {
  return request({
    url: '/user-settings',
    method: 'get'
  })
}

/**
 * 保存或更新当前用户的设置
 * @param {Object} setting 用户设置信息
 * @returns {Promise} 保存后的设置信息
 */
export function saveUserSetting(setting) {
  return request({
    url: '/user-settings',
    method: 'post',
    data: setting
  })
}

/**
 * 更新当前用户设置的部分字段
 * @param {Object} setting 要更新的设置字段（只更新非null字段）
 * @returns {Promise} 更新后的设置信息
 */
export function updateUserSetting(setting) {
  return request({
    url: '/user-settings',
    method: 'put',
    data: setting
  })
}

/**
 * 更新当前用户的主题模式
 * @param {string} themeMode 主题模式（light/dark/auto）
 * @returns {Promise} 更新后的设置信息
 */
export function updateThemeMode(themeMode) {
  return request({
    url: '/user-settings/theme',
    method: 'put',
    params: { themeMode }
  })
}

/**
 * 更新当前用户的通知设置
 * @param {string} notificationType 通知类型
 * @param {boolean} enabled 是否启用
 * @returns {Promise} 更新后的设置信息
 */
export function updateNotificationSetting(notificationType, enabled) {
  return request({
    url: '/user-settings/notification',
    method: 'put',
    params: { notificationType, enabled }
  })
}

/**
 * 重置当前用户的设置为默认值
 * @returns {Promise} 重置后的设置信息
 */
export function resetUserSetting() {
  return request({
    url: '/user-settings/reset',
    method: 'post'
  })
}

/**
 * 删除当前用户的设置（恢复为默认设置）
 * @returns {Promise} 删除结果
 */
export function deleteUserSetting() {
  return request({
    url: '/user-settings',
    method: 'delete'
  })
}

/**
 * 检查当前用户是否有自定义设置
 * @returns {Promise} 是否有自定义设置
 */
export function hasCustomSetting() {
  return request({
    url: '/user-settings/has-custom',
    method: 'get'
  })
} 