import request from '@/utils/request'

/**
 * 用户机器人链接API服务
 * 
 * 功能说明：
 * - 提供用户机器人链接的CRUD操作
 * - 支持链接状态管理和强度评估
 * - 提供链接统计和分析功能
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

/**
 * 创建用户机器人链接
 * @param {string} robotId 机器人ID
 * @returns {Promise} API响应
 */
export function createUserRobotLink(robotId) {
  return request({
    url: '/user-robot-links',
    method: 'post',
    data: {
      robotId: robotId
    }
  })
}

/**
 * 删除用户机器人链接
 * @param {string} robotId 机器人ID
 * @returns {Promise} API响应
 */
export function deleteUserRobotLink(robotId) {
  return request({
    url: `/user-robot-links/${robotId}`,
    method: 'delete'
  })
}

/**
 * 激活用户机器人链接
 * @param {string} robotId 机器人ID
 * @returns {Promise} API响应
 */
export function activateUserRobotLink(robotId) {
  return request({
    url: `/user-robot-links/${robotId}/activate`,
    method: 'post'
  })
}

/**
 * 停用用户机器人链接
 * @param {string} robotId 机器人ID
 * @returns {Promise} API响应
 */
export function deactivateUserRobotLink(robotId) {
  return request({
    url: `/user-robot-links/${robotId}/deactivate`,
    method: 'post'
  })
}

/**
 * 获取用户的链接列表
 * @returns {Promise} API响应
 */
export function getUserRobotLinks() {
  return request({
    url: '/user-robot-links',
    method: 'get'
  })
}

/**
 * 获取用户的激活链接列表
 * @returns {Promise} API响应
 */
export function getUserActiveRobotLinks() {
  return request({
    url: '/user-robot-links/active',
    method: 'get'
  })
}

/**
 * 获取链接详情
 * @param {string} robotId 机器人ID
 * @returns {Promise} API响应
 */
export function getUserRobotLinkDetail(robotId) {
  return request({
    url: `/user-robot-links/${robotId}`,
    method: 'get'
  })
}

/**
 * 更新链接强度
 * @param {string} robotId 机器人ID
 * @param {number} strength 强度值
 * @returns {Promise} API响应
 */
export function updateUserRobotLinkStrength(robotId, strength) {
  return request({
    url: `/user-robot-links/${robotId}/strength`,
    method: 'put',
    data: { strength }
  })
} 