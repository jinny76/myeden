import request from '@/utils/request'

/**
 * 动态管理API接口
 * 
 * 功能说明：
 * - 提供动态发布、查询、删除等API接口封装
 * - 支持图片上传和处理
 * - 管理动态的点赞和评论统计
 * - 支持分页查询和排序
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

/**
 * 发布动态
 * @param {FormData|Object} data - 动态数据（FormData或对象）
 * @param {string} data.content - 动态内容（当data为对象时）
 * @param {Array} data.images - 图片文件列表（当data为对象时）
 * @returns {Promise} 动态发布结果
 */
export function createPost(data) {
  let formData
  let headers = {}
  
  // 如果传入的是FormData，直接使用
  if (data instanceof FormData) {
    formData = data
    headers = {
      'Content-Type': 'multipart/form-data'
    }
  } else {
    // 如果传入的是对象，转换为FormData
    formData = new FormData()
    formData.append('content', data.content)
    
    if (data.images && data.images.length > 0) {
      data.images.forEach((image, index) => {
        formData.append('images', image)
      })
    }
    
    headers = {
      'Content-Type': 'multipart/form-data'
    }
  }
  
  return request({
    url: '/posts',
    method: 'post',
    data: formData,
    headers
  })
}

/**
 * 统一查询动态列表（新方法）
 * 使用新的统一查询接口，支持分页、作者类型过滤、关键词搜索等功能
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码（从1开始）
 * @param {number} params.size - 每页大小
 * @param {string} params.authorType - 作者类型过滤：user(用户)、robot(机器人)
 * @param {string} params.keyword - 关键词搜索（内容或作者名称）
 * @returns {Promise} 动态列表和分页信息
 */
export function queryPosts(params) {
  return request({
    url: '/posts/query',
    method: 'get',
    params
  })
}

/**
 * 获取动态列表（兼容性方法，现在使用统一查询接口）
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码（从1开始）
 * @param {number} params.size - 每页大小
 * @param {string} params.authorType - 作者类型过滤（可选）
 * @returns {Promise} 动态列表和分页信息
 */
export function getPostList(params) {
  // 将参数转换为统一查询接口的格式
  const queryParams = {
    page: params.page,
    size: params.size
  }
  
  // 如果有作者类型过滤，添加到查询参数
  if (params.authorType) {
    queryParams.authorType = params.authorType
  }
  
  return queryPosts(queryParams)
}

/**
 * 获取动态详情
 * @param {string} postId - 动态ID
 * @returns {Promise} API响应
 */
export const getPostDetail = (postId) => {
  return request({
    url: `/posts/${postId}`,
    method: 'get'
  })
}

/**
 * 删除动态
 * @param {string} postId - 动态ID
 * @returns {Promise} 删除结果
 */
export function deletePost(postId) {
  return request({
    url: `/posts/${postId}`,
    method: 'delete'
  })
}

/**
 * 点赞动态
 * @param {string} postId - 动态ID
 * @returns {Promise} 点赞结果
 */
export function likePost(postId) {
  return request({
    url: `/posts/${postId}/like`,
    method: 'post'
  })
}

/**
 * 取消点赞动态
 * @param {string} postId - 动态ID
 * @returns {Promise} 取消点赞结果
 */
export function unlikePost(postId) {
  return request({
    url: `/posts/${postId}/like`,
    method: 'delete'
  })
}

/**
 * 获取用户的动态列表（兼容性方法，现在使用统一查询接口）
 * @param {string} authorId - 作者ID
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @returns {Promise} 用户动态列表
 */
export function getUserPosts(authorId, params) {
  // 使用统一查询接口，通过关键词搜索特定用户
  const queryParams = {
    page: params.page,
    size: params.size,
    keyword: authorId, // 使用作者ID作为关键词
    searchType: 'author' // 指定搜索类型为作者
  }
  
  return queryPosts(queryParams)
}

/**
 * 搜索动态（兼容性方法，现在使用统一查询接口）
 * @param {Object} params - 搜索参数
 * @param {string} params.keyword - 搜索关键字
 * @param {string} params.searchType - 搜索类型：content(内容)、author(发帖人)、all(全部)
 * @param {number} params.page - 页码（从1开始）
 * @param {number} params.size - 每页大小
 * @returns {Promise} 搜索结果和分页信息
 */
export function searchPosts(params) {
  // 将搜索参数转换为统一查询接口的格式
  const queryParams = {
    page: params.page,
    size: params.size,
    keyword: params.keyword
  }
  
  // 如果有搜索类型，可以在这里处理
  // 注意：新的统一查询接口会自动处理关键词搜索，不需要显式指定搜索类型
  
  return queryPosts(queryParams)
}

/**
 * 获取动态的所有点赞信息
 * @param {string} postId - 动态ID
 * @returns {Promise} 点赞信息列表
 */
export function getPostLikes(postId) {
  return request({
    url: `/posts/${postId}/likes`,
    method: 'get'
  })
} 