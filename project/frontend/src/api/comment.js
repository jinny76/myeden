import request from '@/utils/request'

/**
 * 评论管理API接口
 * 
 * 功能说明：
 * - 提供评论发布、查询、删除等API接口封装
 * - 支持评论回复功能
 * - 管理评论的点赞和回复统计
 * - 支持分页查询和排序
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

/**
 * 发表评论
 * @param {string} postId - 动态ID
 * @param {Object} data - 评论数据
 * @param {string} data.content - 评论内容
 * @returns {Promise} 评论发布结果
 */
export function createComment(postId, data) {
  return request({
    url: `/posts/${postId}/comments`,
    method: 'post',
    data
  })
}

/**
 * 回复评论
 * @param {string} commentId - 评论ID
 * @param {Object} data - 回复数据
 * @param {string} data.content - 回复内容
 * @returns {Promise} 回复发布结果
 */
export function replyComment(commentId, data) {
  return request({
    url: `/comments/${commentId}/replies`,
    method: 'post',
    data
  })
}

/**
 * 获取动态的评论列表
 * @param {string} postId - 动态ID
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码（从1开始）
 * @param {number} params.size - 每页大小
 * @returns {Promise} 评论列表和分页信息
 */
export function getCommentList(postId, params) {
  return request({
    url: `/posts/${postId}/comments`,
    method: 'get',
    params
  })
}

/**
 * 获取评论的回复列表
 * @param {string} commentId - 评论ID
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码（从1开始）
 * @param {number} params.size - 每页大小
 * @returns {Promise} 回复列表和分页信息
 */
export function getReplyList(commentId, params) {
  return request({
    url: `/comments/${commentId}/replies`,
    method: 'get',
    params
  })
}

/**
 * 获取评论详情
 * @param {string} commentId - 评论ID
 * @returns {Promise} 评论详细信息
 */
export function getCommentDetail(commentId) {
  return request({
    url: `/comments/${commentId}`,
    method: 'get'
  })
}

/**
 * 删除评论
 * @param {string} commentId - 评论ID
 * @returns {Promise} 删除结果
 */
export function deleteComment(commentId) {
  return request({
    url: `/comments/${commentId}`,
    method: 'delete'
  })
}

/**
 * 点赞评论
 * @param {string} commentId - 评论ID
 * @returns {Promise} 点赞结果
 */
export function likeComment(commentId) {
  return request({
    url: `/comments/${commentId}/like`,
    method: 'post'
  })
}

/**
 * 取消点赞评论
 * @param {string} commentId - 评论ID
 * @returns {Promise} 取消点赞结果
 */
export function unlikeComment(commentId) {
  return request({
    url: `/comments/${commentId}/like`,
    method: 'delete'
  })
} 