import request from '@/utils/request'

/**
 * 用户相关API接口
 * 
 * 功能说明：
 * - 用户注册、登录、登出
 * - 用户信息获取和更新
 * - 头像上传
 * - Token刷新
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

export const userApi = {
  /**
   * 用户注册
   * @param {Object} data - 注册数据
   * @param {string} data.phone - 手机号
   * @param {string} data.password - 密码
   * @param {string} data.nickname - 昵称
   * @returns {Promise} API响应
   */
  register(data) {
    return request({
      url: '/auth/register',
      method: 'post',
      data
    })
  },

  /**
   * 用户登录
   * @param {Object} data - 登录数据
   * @param {string} data.phone - 手机号
   * @param {string} data.password - 密码
   * @returns {Promise} API响应
   */
  login(data) {
    return request({
      url: '/auth/login',
      method: 'post',
      data
    })
  },

  /**
   * 用户登出
   * @returns {Promise} API响应
   */
  logout() {
    return request({
      url: '/auth/logout',
      method: 'post'
    })
  },

  /**
   * 获取用户信息
   * @returns {Promise} API响应
   */
  getUserInfo() {
    return request({
      url: '/user/info',
      method: 'get'
    })
  },

  /**
   * 更新用户信息
   * @param {Object} data - 更新的用户信息
   * @returns {Promise} API响应
   */
  updateUserInfo(data) {
    return request({
      url: '/user/info',
      method: 'put',
      data
    })
  },

  /**
   * 上传头像
   * @param {File} file - 头像文件
   * @returns {Promise} API响应
   */
  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('avatar', file)
    
    return request({
      url: '/user/avatar',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 刷新Token
   * @returns {Promise} API响应
   */
  refreshToken() {
    return request({
      url: '/auth/refresh',
      method: 'post'
    })
  },

  /**
   * 修改密码
   * @param {Object} data - 密码数据
   * @param {string} data.oldPassword - 旧密码
   * @param {string} data.newPassword - 新密码
   * @returns {Promise} API响应
   */
  changePassword(data) {
    return request({
      url: '/user/password',
      method: 'put',
      data
    })
  },

  /**
   * 获取用户统计信息
   * @returns {Promise} API响应
   */
  getUserStats() {
    return request({
      url: '/user/stats',
      method: 'get'
    })
  },

  /**
   * 获取用户动态列表
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} API响应
   */
  getUserPosts(params) {
    return request({
      url: '/user/posts',
      method: 'get',
      params
    })
  },

  /**
   * 获取用户评论列表
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} API响应
   */
  getUserComments(params) {
    return request({
      url: '/user/comments',
      method: 'get',
      params
    })
  },

  /**
   * 获取用户点赞列表
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} API响应
   */
  getUserLikes(params) {
    return request({
      url: '/user/likes',
      method: 'get',
      params
    })
  },

  /**
   * 删除用户账号
   * @param {Object} data - 删除确认数据
   * @param {string} data.password - 密码确认
   * @returns {Promise} API响应
   */
  deleteAccount(data) {
    return request({
      url: '/user/account',
      method: 'delete',
      data
    })
  }
} 