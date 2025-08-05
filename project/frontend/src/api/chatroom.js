/**
 * 聊天室API模块
 * 
 * 功能说明：
 * - 聊天室创建、获取、更新
 * - 成员管理（添加、移除、静音）
 * - 消息发送和历史记录
 * - 聊天室统计信息
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-07-16
 */

import api from '@/utils/request'

/**
 * 创建或获取用户的聊天室
 * @returns {Promise<Object>} 聊天室信息
 */
export const createOrGetChatRoom = async () => {
  const response = await api.post('/chatroom/create', {})
  return response
}

/**
 * 获取聊天室信息
 * @param {string} roomId - 聊天室ID
 * @returns {Promise<Object>} 聊天室信息
 */
export const getChatRoom = async (roomId) => {
  const response = await api.get(`/chatroom/${roomId}`)
  return response
}

/**
 * 更新聊天室名称
 * @param {string} roomId - 聊天室ID
 * @param {string} roomName - 新的聊天室名称
 * @returns {Promise<Object>} 更新结果
 */
export const updateChatRoomName = async (roomId, roomName) => {
  const response = await api.put(`/chatroom/${roomId}/name`, { roomName })
  return response
}

/**
 * 切换聊天室状态
 * @param {string} roomId - 聊天室ID
 * @param {string} status - 新状态（active/inactive）
 * @returns {Promise<Object>} 更新结果
 */
export const switchRoomStatus = async (roomId, status) => {
  const response = await api.put(`/chatroom/${roomId}/status`, { status })
  return response
}

/**
 * 获取聊天室成员列表
 * @param {string} roomId - 聊天室ID
 * @returns {Promise<Array>} 成员列表
 */
export const getChatRoomMembers = async (roomId) => {
  const response = await api.get(`/chatroom/${roomId}/members`)
  return response
}

/**
 * 添加机器人到聊天室
 * @param {string} roomId - 聊天室ID
 * @param {Object} robotData - 机器人数据
 * @returns {Promise<Object>} 添加结果
 */
export const addRobotToRoom = async (roomId, robotData) => {
  const response = await api.post(`/chatroom/${roomId}/members/robot`, robotData)
  return response
}

/**
 * 从聊天室移除成员
 * @param {string} roomId - 聊天室ID
 * @param {string} memberId - 成员ID
 * @returns {Promise<Object>} 移除结果
 */
export const removeMember = async (roomId, memberId) => {
  const response = await api.delete(`/chatroom/${roomId}/members/${memberId}`)
  return response
}



/**
 * 发送消息到聊天室
 * @param {string} roomId - 聊天室ID
 * @param {Object} messageData - 消息数据
 * @returns {Promise<Object>} 发送结果
 */
export const sendMessage = async (roomId, messageData) => {
  const response = await api.post(`/chatroom/${roomId}/messages`, messageData)
  return response
}

/**
 * 获取聊天历史记录
 * @param {string} roomId - 聊天室ID
 * @param {number} page - 页码
 * @param {number} size - 每页大小
 * @returns {Promise<Object>} 历史记录
 */
export const getChatHistory = async (roomId, page = 0, size = 20) => {
  const response = await api.get(`/chatroom/${roomId}/messages`, {
    params: { page, size }
  })
  return response
}

/**
 * 获取聊天室统计信息
 * @param {string} roomId - 聊天室ID
 * @returns {Promise<Object>} 统计信息
 */
export const getChatRoomStats = async (roomId) => {
  const response = await api.get(`/chatroom/${roomId}/stats`)
  return response
} 