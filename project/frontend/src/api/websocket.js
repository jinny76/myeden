import request from '@/utils/request'

/**
 * WebSocket API接口封装
 * 
 * 功能说明：
 * - 提供WebSocket连接状态查询接口
 * - 提供在线用户信息查询接口
 * - 提供手动消息推送接口
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

/**
 * 获取在线用户数量
 * 
 * @returns {Promise<Object>} 在线用户数量
 */
export function getOnlineUserCount() {
  return request({
    url: '/websocket/online-count',
    method: 'get'
  })
}

/**
 * 获取在线用户列表
 * 
 * @returns {Promise<Object>} 在线用户列表
 */
export function getOnlineUsers() {
  return request({
    url: '/websocket/online-users',
    method: 'get'
  })
}

/**
 * 检查用户是否在线
 * 
 * @param {string} userId 用户ID
 * @returns {Promise<Object>} 用户在线状态
 */
export function isUserOnline(userId) {
  return request({
    url: `/websocket/user/${userId}/online`,
    method: 'get'
  })
}

/**
 * 断开用户连接
 * 
 * @param {string} userId 用户ID
 * @returns {Promise<Object>} 操作结果
 */
export function disconnectUser(userId) {
  return request({
    url: `/websocket/user/${userId}/disconnect`,
    method: 'post'
  })
}

/**
 * 发送心跳消息
 * 
 * @returns {Promise<Object>} 操作结果
 */
export function sendHeartbeat() {
  return request({
    url: '/websocket/heartbeat',
    method: 'post'
  })
} 