import request from '@/utils/request'

/**
 * 世界管理API
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

/**
 * 获取世界基本信息
 * @returns {Promise} 世界基本信息
 */
export function getWorldInfo() {
  return request({
    url: '/world',
    method: 'get'
  })
}

/**
 * 获取世界背景信息
 * @returns {Promise} 世界背景信息
 */
export function getWorldBackground() {
  return request({
    url: '/world/background',
    method: 'get'
  })
}

/**
 * 获取世界环境信息
 * @returns {Promise} 世界环境信息
 */
export function getWorldEnvironment() {
  return request({
    url: '/world/environment',
    method: 'get'
  })
}

/**
 * 获取世界活动信息
 * @returns {Promise} 世界活动信息
 */
export function getWorldActivities() {
  return request({
    url: '/world/activities',
    method: 'get'
  })
}

/**
 * 获取世界统计信息
 * @returns {Promise} 世界统计信息
 */
export function getWorldStatistics() {
  return request({
    url: '/world/statistics',
    method: 'get'
  })
}

/**
 * 获取世界设置信息
 * @returns {Promise} 世界设置信息
 */
export function getWorldSettings() {
  return request({
    url: '/world/settings',
    method: 'get'
  })
}

/**
 * 获取机器人列表
 * @returns {Promise} 机器人列表
 */
export function getRobotList() {
  return request({
    url: '/world/robots',
    method: 'get'
  })
}

/**
 * 获取机器人详情
 * @param {string} robotId - 机器人ID
 * @returns {Promise} 机器人详情
 */
export function getRobotDetail(robotId) {
  return request({
    url: `/world/robots/${robotId}`,
    method: 'get'
  })
}

/**
 * 重新加载世界配置
 * @returns {Promise} 操作结果
 */
export function reloadWorldConfig() {
  return request({
    url: '/world/reload-config',
    method: 'post'
  })
}

/**
 * 重新加载机器人配置
 * @returns {Promise} 操作结果
 */
export function reloadRobotConfig() {
  return request({
    url: '/world/reload-robots',
    method: 'post'
  })
}

/**
 * 获取配置状态
 * @returns {Promise} 配置状态
 */
export function getConfigStatus() {
  return request({
    url: '/world/config-status',
    method: 'get'
  })
}

// 导出所有API方法
export const worldApi = {
  getWorldInfo,
  getWorldBackground,
  getWorldEnvironment,
  getWorldActivities,
  getWorldStatistics,
  getWorldSettings,
  getRobotList,
  getRobotDetail,
  reloadWorldConfig,
  reloadRobotConfig,
  getConfigStatus
} 