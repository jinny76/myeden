import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

/**
 * 获取与机器人相关的聊天历史
 * @param {string} robotId 机器人ID
 * @param {object} params {limit, offset}
 * @returns Promise
 */
export function getChatHistory(robotId, params = { limit: 20, offset: 0 }) {
  const userStore = useUserStore()
  const userId = userStore.userInfo?.userId
  return request({
    url: `/chat/history/robot`,
    method: 'get',
    params: { userId, robotId, ...params }
  })
}

/**
 * 发送聊天消息
 * @param {string} robotId 机器人ID
 * @param {string} content 消息内容
 * @returns Promise
 */
export function sendChatMessage(robotId, content, conversationId, imageBase64, audioBase64) {
  const userStore = useUserStore()
  const userId = userStore.userInfo?.userId
  return request({
    url: `/chat/send`,
    method: 'post',
    data: {
      senderId: userId,
      senderType: 'user',
      receiverId: robotId,
      receiverType: 'robot',
      content,
      conversationId,
      imageBase64,
      audioBase64
    }
  })
}

/**
 * 根据会话ID获取聊天历史
 * @param {string} conversationId
 * @returns {Promise<{code: number, data: Array}>}
 */
export function getChatHistoryByConversationId(conversationId) {
  return request({
    url: `/chat/history/${conversationId}`,
    method: 'get'
  })
} 