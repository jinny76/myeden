import request from '@/utils/request'

/**
 * 沟通评估API服务
 * 
 * 功能说明：
 * - 提供沟通评估报告相关的API接口
 * - 支持获取用户沟通统计和报告列表
 * - 提供过滤和搜索功能
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-07-15
 */

/**
 * 获取用户沟通统计
 * @returns {Promise} API响应
 */
export function getUserCommunicationStatistics() {
  return request({
    url: '/communication-reports/statistics',
    method: 'get'
  })
}

/**
 * 获取用户沟通报告列表
 * @returns {Promise} API响应
 */
export function getUserCommunicationReports() {
  return request({
    url: '/communication-reports',
    method: 'get'
  })
}

/**
 * 获取过滤后的沟通报告列表
 * @param {Object} params 过滤参数
 * @param {string} params.robotId 机器人ID过滤（可选）
 * @param {string} params.startDate 开始日期过滤（可选，格式：yyyy-MM-dd）
 * @param {string} params.endDate 结束日期过滤（可选，格式：yyyy-MM-dd）
 * @returns {Promise} API响应
 */
export function getFilteredCommunicationReports(params = {}) {
  return request({
    url: '/communication-reports/filtered',
    method: 'get',
    params
  })
}

/**
 * 手动触发当前用户的沟通评价
 * @returns {Promise} API响应
 */
export function triggerCommunicationEvaluation() {
  return request({
    url: '/communication-reports/trigger-evaluation',
    method: 'post'
  })
}