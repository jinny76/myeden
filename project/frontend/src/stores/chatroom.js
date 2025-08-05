import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useUserStore } from './user'
import { useRobotStore } from './robot'
import {
  createOrGetChatRoom as createOrGetChatRoomApi,
  getChatRoom as getChatRoomApi,
  updateChatRoomName as updateChatRoomNameApi,
  switchRoomStatus as switchRoomStatusApi,
  getChatRoomMembers as getChatRoomMembersApi,
  addRobotToRoom as addRobotToRoomApi,
  removeMember as removeMemberApi,
  sendMessage as sendMessageApi,
  getChatHistory as getChatHistoryApi,
  getChatRoomStats as getChatRoomStatsApi
} from '@/api/chatroom'

/**
 * 聊天室状态管理
 * 管理聊天室信息、成员、消息等状态
 */
export const useChatRoomStore = defineStore('chatroom', () => {
  // 状态
  const currentChatRoom = ref(null)
  const chatRooms = ref([])
  const members = ref([])
  const messages = ref([])
  const loading = ref(false)
  const error = ref(null)

  // 获取用户信息
  const userStore = useUserStore()

  /**
   * 创建或获取用户的聊天室
   */
  const createOrGetChatRoom = async () => {
    try {
      loading.value = true
      error.value = null

      const response = await createOrGetChatRoomApi()

      if (response.code === 200 && response.data) {
        currentChatRoom.value = response.data
        return currentChatRoom.value
      } else {
        throw new Error(response.message || '创建聊天室失败')
      }
    } catch (err) {
      error.value = err.message || '创建聊天室失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取聊天室信息
   */
  const getChatRoom = async (roomId) => {
    try {
      loading.value = true
      error.value = null

      const response = await getChatRoomApi(roomId)

      if (response.code === 200 && response.data) {
        currentChatRoom.value = response.data
        return currentChatRoom.value
      } else {
        throw new Error(response.message || '获取聊天室信息失败')
      }
    } catch (err) {
      error.value = err.message || '获取聊天室信息失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新聊天室名称
   */
  const updateChatRoomName = async (roomId, roomName) => {
    try {
      const response = await updateChatRoomNameApi(roomId, roomName)

      if (response.code === 200 && response.data) {
        if (currentChatRoom.value && currentChatRoom.value.roomId === roomId) {
          currentChatRoom.value.roomName = roomName
        }
        return response.data
      } else {
        throw new Error(response.message || '更新聊天室名称失败')
      }
    } catch (err) {
      throw err
    }
  }

  /**
   * 切换聊天室状态
   */
  const switchRoomStatus = async (roomId, status) => {
    try {
      const response = await switchRoomStatusApi(roomId, status)

      if (response.code === 200 && response.data) {
        if (currentChatRoom.value && currentChatRoom.value.roomId === roomId) {
          currentChatRoom.value.status = status
        }
        return response.data
      } else {
        throw new Error(response.message || '切换聊天室状态失败')
      }
    } catch (err) {
      throw err
    }
  }

  /**
   * 获取聊天室成员列表
   */
  const getChatRoomMembers = async (roomId) => {
    try {
      const response = await getChatRoomMembersApi(roomId)

      if (response.code === 200 && response.data) {
        // 获取用户和机器人信息
        const userStore = useUserStore()
        const robotStore = useRobotStore()
        
        // 确保机器人列表已加载
        if (robotStore.robots.length === 0) {
          await robotStore.fetchRobotList()
        }
        
        // 处理成员数据，为每个成员添加相应的用户或机器人信息
        const processedMembers = response.data.map(member => {
          const processedMember = { ...member }
          
          if (member.memberType === 'USER') {
            // 如果是用户，设置当前用户信息
            if (member.memberId === userStore.userInfo?.userId) {
              processedMember.user = userStore.userInfo
            } else {
              // 对于其他用户，可以在这里添加获取用户信息的逻辑
              processedMember.user = {
                userId: member.memberId,
                nickname: member.memberNickname,
                avatar: member.memberAvatar
              }
            }
            processedMember.avatar = "/api/v1/files" + processedMember.user.avatar.replaceAll('/uploads/', '/')
          } else if (member.memberType === 'ROBOT') {
            // 如果是机器人，从机器人store中查找对应的机器人信息
            const robot = robotStore.robots.find(r => r.id === member.memberId || r.robotId === member.memberId)
            if (robot) {
              processedMember.robot = robot
            } else {
              // 如果找不到机器人信息，使用成员的基本信息
              processedMember.robot = {
                id: member.memberId,
                name: member.memberNickname,
                avatar: member.memberAvatar
              }
            }
            processedMember.avatar = "/api/v1/files" + processedMember.robot.avatar
          }
          
          return processedMember
        })
        
        members.value = processedMembers
        return members.value
      } else {
        throw new Error(response.message || '获取成员列表失败')
      }
    } catch (err) {
      throw err
    }
  }

  /**
   * 添加机器人到聊天室
   */
  const addRobotToRoom = async (roomId, robotData) => {
    try {
      const response = await addRobotToRoomApi(roomId, robotData)

      if (response.code === 200 && response.data) {
        // 获取机器人信息
        const robotStore = useRobotStore()
        
        // 确保机器人列表已加载
        if (robotStore.robots.length === 0) {
          await robotStore.fetchRobotList()
        }
        
        // 处理新成员数据
        const newMember = { ...response.data }
        
        // 查找对应的机器人信息
        const robot = robotStore.robots.find(r => r.id === newMember.memberId || r.robotId === newMember.memberId)
        if (robot) {
          newMember.robot = robot
        } else {
          // 如果找不到机器人信息，使用成员的基本信息
          newMember.robot = {
            id: newMember.memberId,
            name: newMember.memberNickname,
            avatar: newMember.memberAvatar
          }
        }
        
        members.value.push(newMember)
        return newMember
      } else {
        throw new Error(response.message || '添加机器人失败')
      }
    } catch (err) {
      throw err
    }
  }

  /**
   * 从聊天室移除成员
   */
  const removeMember = async (roomId, memberId) => {
    try {
      const response = await removeMemberApi(roomId, memberId)

      if (response.code === 200 && response.data) {
        // 更新本地成员列表
        members.value = members.value.filter(member => member.memberId !== memberId)
        return true
      } else {
        throw new Error(response.message || '移除成员失败')
      }
    } catch (err) {
      throw err
    }
  }



  /**
   * 发送群聊消息
   */
  const sendMessage = async (roomId, messageData) => {
    try {
      const response = await sendMessageApi(roomId, messageData)

      if (response.code === 200 && response.data) {
        const newMessage = response.data
        // 更新本地消息列表
        messages.value.push(newMessage)
        return newMessage
      } else {
        throw new Error(response.message || '发送消息失败')
      }
    } catch (err) {
      throw err
    }
  }

  /**
   * 获取聊天历史
   */
  const getChatHistory = async (roomId, page = 0, size = 20) => {
    try {
      const response = await getChatHistoryApi(roomId, page, size)

      if (response.code === 200 && response.data) {
        const pageData = response.data
        
        // 如果是第一页，替换消息列表；否则插入到前面
        if (page === 0) {
          messages.value = pageData.content || []
        } else {
          messages.value.unshift(...(pageData.content || []))
        }
        
        return pageData
      } else {
        throw new Error(response.message || '获取聊天历史失败')
      }
    } catch (err) {
      throw err
    }
  }

  /**
   * 获取聊天室统计信息
   */
  const getChatRoomStats = async (roomId) => {
    try {
      const response = await getChatRoomStatsApi(roomId)

      if (response.code === 200 && response.data) {
        // 后端现在返回Map对象，直接使用
        const stats = response.data
        
        // 确保返回的统计信息包含所需字段
        return {
          totalMessages: stats.totalMessages || 0,
          activeMembers: stats.activeMembers || stats.onlineMembers || 0,
          robotMessages: stats.robotMessages || 0,
          activeDuration: stats.activeDuration || 0,
          totalMembers: stats.totalMembers || 0,
          robotMembers: stats.robotMembers || 0,
          roomName: stats.roomName || '',
          status: stats.status || 'active'
        }
      } else {
        throw new Error(response.message || '获取统计信息失败')
      }
    } catch (err) {
      throw err
    }
  }

  /**
   * 添加消息到本地列表（用于WebSocket接收消息）
   */
  const addMessage = (message) => {
    messages.value.push(message)
  }

  /**
   * 更新成员在线状态
   */
  const updateMemberStatus = (memberId, isOnline) => {
    const member = members.value.find(m => m.memberId === memberId)
    if (member) {
      member.isOnline = isOnline
    }
  }

  /**
   * 清空聊天室数据
   */
  const clearChatRoomData = () => {
    currentChatRoom.value = null
    members.value = []
    messages.value = []
    error.value = null
  }

  /**
   * 重置错误状态
   */
  const clearError = () => {
    error.value = null
  }

  return {
    // 状态
    currentChatRoom,
    chatRooms,
    members,
    messages,
    loading,
    error,

    // 方法
    createOrGetChatRoom,
    getChatRoom,
    updateChatRoomName,
    switchRoomStatus,
    getChatRoomMembers,
    addRobotToRoom,
    removeMember,
    sendMessage,
    getChatHistory,
    getChatRoomStats,
    addMessage,
    updateMemberStatus,
    clearChatRoomData,
    clearError
  }
})