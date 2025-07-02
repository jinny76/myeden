import request from '@/utils/request'

/**
 * 机器人API接口封装
 * 提供机器人行为管理和查询功能
 */

/**
 * 获取机器人列表
 * @param {boolean} isActive - 是否只返回激活的机器人
 * @returns {Promise} 机器人列表
 */
export function getRobotList(isActive = null) {
  const params = {}
  if (isActive !== null) {
    params.isActive = isActive
  }
  return request({
    url: '/robots',
    method: 'get',
    params
  })
}

/**
 * 获取机器人详情
 * @param {string} robotId - 机器人ID
 * @returns {Promise} 机器人详细信息
 */
export function getRobotDetail(robotId) {
  return request({
    url: `/robots/${robotId}`,
    method: 'get'
  })
}

/**
 * 触发机器人发布动态
 * @param {string} robotId - 机器人ID
 * @returns {Promise} 触发结果
 */
export function triggerRobotPost(robotId) {
  return request({
    url: `/robots/${robotId}/posts`,
    method: 'post'
  })
}

/**
 * 触发机器人发表评论
 * @param {string} robotId - 机器人ID
 * @param {string} postId - 动态ID
 * @returns {Promise} 触发结果
 */
export function triggerRobotComment(robotId, postId) {
  return request({
    url: `/robots/${robotId}/comments`,
    method: 'post',
    params: { postId }
  })
}

/**
 * 触发机器人回复评论
 * @param {string} robotId - 机器人ID
 * @param {string} commentId - 评论ID
 * @returns {Promise} 触发结果
 */
export function triggerRobotReply(robotId, commentId) {
  return request({
    url: `/robots/${robotId}/replies`,
    method: 'post',
    params: { commentId }
  })
}

/**
 * 获取机器人今日行为统计
 * @param {string} robotId - 机器人ID
 * @returns {Promise} 行为统计信息
 */
export function getRobotDailyStats(robotId) {
  return request({
    url: `/robots/${robotId}/stats`,
    method: 'get'
  })
}

/**
 * 重置机器人行为统计
 * @param {string} robotId - 机器人ID
 * @returns {Promise} 重置结果
 */
export function resetRobotDailyStats(robotId) {
  return request({
    url: `/robots/${robotId}/stats/reset`,
    method: 'post'
  })
}

/**
 * 启动机器人行为调度
 * @returns {Promise} 启动结果
 */
export function startBehaviorScheduler() {
  return request({
    url: '/robots/scheduler/start',
    method: 'post'
  })
}

/**
 * 停止机器人行为调度
 * @returns {Promise} 停止结果
 */
export function stopBehaviorScheduler() {
  return request({
    url: '/robots/scheduler/stop',
    method: 'post'
  })
}

/**
 * 刷新机器人在线状态
 * 根据机器人的活跃时间配置更新所有机器人的在线状态
 * @returns {Promise} 刷新结果
 */
export function refreshRobotStatus() {
  return request({
    url: '/robots/status/refresh',
    method: 'post'
  })
}

/**
 * 查询机器人每日计划（只读）
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getDailyPlanList(params) {
  return request({
    url: '/robot/plan/list',
    method: 'get',
    params
  })
}

export default {
  getRobotList,
  getRobotDetail,
  triggerRobotPost,
  triggerRobotComment,
  triggerRobotReply,
  getRobotDailyStats,
  resetRobotDailyStats,
  startBehaviorScheduler,
  stopBehaviorScheduler,
  refreshRobotStatus,
  getDailyPlanList
} 