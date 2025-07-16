import request from '@/utils/request'
import { formatDateToYMD, getYearRange } from '@/utils/dateHelper'

/**
 * 活动数据API
 * 
 * 功能说明：
 * - 提供用户活动数据的前端API接口
 * - 支持获取活动统计和贡献图数据
 * - 支持记录用户活动行为
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-01-27
 */

/**
 * 获取用户活动数据
 * 
 * @param {string} userId - 用户ID
 * @param {string} startDate - 开始日期 (YYYY-MM-DD)
 * @param {string} endDate - 结束日期 (YYYY-MM-DD)
 * @returns {Promise} 活动数据
 */
export const getUserActivityData = (userId, startDate, endDate) => {
  const params = {}
  if (startDate) params.startDate = startDate
  if (endDate) params.endDate = endDate
  
  return request({
    url: `/activity/user/${userId}`,
    method: 'GET',
    params
  })
}

/**
 * 获取用户活动统计
 * 
 * @param {string} userId - 用户ID
 * @param {string} startDate - 开始日期 (YYYY-MM-DD)
 * @param {string} endDate - 结束日期 (YYYY-MM-DD)
 * @returns {Promise} 活动统计数据
 */
export const getUserActivityStats = (userId, startDate, endDate) => {
  const params = {}
  if (startDate) params.startDate = startDate
  if (endDate) params.endDate = endDate
  
  return request({
    url: `/activity/user/${userId}/stats`,
    method: 'GET',
    params
  })
}

/**
 * 记录用户活动
 * 
 * @param {string} userId - 用户ID
 * @param {string} activityType - 活动类型 (chat, post, comment, reply, like)
 * @returns {Promise} 记录结果
 */
export const recordUserActivity = (userId, activityType) => {
  return request({
    url: `/activity/user/${userId}/record`,
    method: 'POST',
    params: {
      activityType
    }
  })
}

/**
 * 获取用户过去一年的活动数据（用于贡献图）
 * 
 * @param {string} userId - 用户ID
 * @returns {Promise} 活动数据
 */
export const getUserContributionData = (userId) => {
  const { startDate, endDate } = getYearRange()
  
  return getUserActivityData(userId, startDate, endDate)
}

/**
 * 获取用户活动概览统计
 * 
 * @param {string} userId - 用户ID
 * @returns {Promise} 活动统计
 */
export const getUserActivityOverview = (userId) => {
  const { startDate, endDate } = getYearRange()
  
  return getUserActivityStats(userId, startDate, endDate)
}

/**
 * 批量记录用户活动（用于优化性能）
 * 
 * @param {string} userId - 用户ID
 * @param {Array<string>} activities - 活动类型数组
 * @returns {Promise} 记录结果
 */
export const recordUserActivities = async (userId, activities) => {
  const promises = activities.map(activityType => 
    recordUserActivity(userId, activityType)
  )
  
  try {
    const results = await Promise.all(promises)
    return {
      success: true,
      results
    }
  } catch (error) {
    console.error('批量记录用户活动失败:', error)
    return {
      success: false,
      error
    }
  }
}

/**
 * 获取用户最近活动数据
 * 
 * @param {string} userId - 用户ID
 * @param {number} days - 天数（默认30天）
 * @returns {Promise} 活动数据
 */
export const getUserRecentActivity = (userId, days = 30) => {
  const endDate = new Date()
  const startDate = new Date(endDate)
  startDate.setDate(endDate.getDate() - days)
  
  return getUserActivityData(
    userId,
    formatDateToYMD(startDate),
    formatDateToYMD(endDate)
  )
}