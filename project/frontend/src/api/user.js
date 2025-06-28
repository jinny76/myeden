import request from '@/utils/request'

/**
 * 用户管理API
 * 
 * 功能说明：
 * - 提供用户注册、登录、信息管理等API接口
 * - 支持用户头像上传和个人资料更新
 * - 提供用户查询和统计功能
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

/**
 * 用户注册
 * @param {Object} data - 注册数据
 * @param {string} data.phone - 手机号
 * @param {string} data.password - 密码
 * @returns {Promise} 注册结果
 */
export function register(data) {
  return request({
    url: '/users/register',
    method: 'post',
    data
  })
}

/**
 * 用户登录
 * @param {Object} data - 登录数据
 * @param {string} data.phone - 手机号
 * @param {string} data.password - 密码
 * @returns {Promise} 登录结果
 */
export function login(data) {
  return request({
    url: '/users/login',
    method: 'post',
    data
  })
}

/**
 * 获取用户信息
 * @returns {Promise} 用户信息
 */
export function getUserInfo() {
  return request({
    url: '/users/me',
    method: 'get'
  })
}

/**
 * 更新用户信息
 * @param {string} userId - 用户ID
 * @param {Object} data - 用户信息
 * @returns {Promise} 更新结果
 */
export function updateUserInfo(userId, data) {
  return request({
    url: `/users/${userId}`,
    method: 'put',
    data
  })
}

/**
 * 上传用户头像
 * @param {string} userId - 用户ID
 * @param {FormData} formData - 文件数据
 * @returns {Promise} 上传结果
 */
export function uploadAvatar(userId, formData) {
  return request({
    url: `/users/${userId}/avatar`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 完成首次登录
 * @param {string} userId - 用户ID
 * @returns {Promise} 操作结果
 */
export function completeFirstLogin(userId) {
  return request({
    url: `/users/${userId}/complete-first-login`,
    method: 'post'
  })
}

/**
 * 检查手机号是否存在
 * @param {string} phone - 手机号
 * @returns {Promise} 检查结果
 */
export function checkPhone(phone) {
  return request({
    url: '/users/check-phone',
    method: 'get',
    params: { phone }
  })
}

/**
 * 检查昵称是否存在
 * @param {string} nickname - 昵称
 * @returns {Promise} 检查结果
 */
export function checkNickname(nickname) {
  return request({
    url: '/users/check-nickname',
    method: 'get',
    params: { nickname }
  })
}

/**
 * 搜索用户
 * @param {string} nickname - 昵称关键词
 * @param {number} limit - 限制数量
 * @returns {Promise} 搜索结果
 */
export function searchUsers(nickname, limit = 10) {
  return request({
    url: '/users/search',
    method: 'get',
    params: { nickname, limit }
  })
}

/**
 * 获取最近注册的用户
 * @param {number} limit - 限制数量
 * @returns {Promise} 用户列表
 */
export function getRecentUsers(limit = 10) {
  return request({
    url: '/users/recent',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取用户统计信息
 * @returns {Promise} 统计信息
 */
export function getUserStatistics() {
  return request({
    url: '/users/statistics',
    method: 'get'
  })
}

// 导出所有API方法
export const userApi = {
  register,
  login,
  getUserInfo,
  updateUserInfo,
  uploadAvatar,
  completeFirstLogin,
  checkPhone,
  checkNickname,
  searchUsers,
  getRecentUsers,
  getUserStatistics
} 