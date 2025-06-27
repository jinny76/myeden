import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { io } from 'socket.io-client'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

/**
 * WebSocket状态管理
 * 
 * 功能说明：
 * - 管理WebSocket连接状态
 * - 处理实时消息推送
 * - 管理连接重连机制
 * - 处理消息队列和去重
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

export const useWebSocketStore = defineStore('websocket', () => {
  // 状态定义
  const socket = ref(null)
  const isConnected = ref(false)
  const isConnecting = ref(false)
  const reconnectAttempts = ref(0)
  const maxReconnectAttempts = ref(5)
  const reconnectDelay = ref(1000)
  const messageQueue = ref([])
  const messageHistory = ref([])
  const maxMessageHistory = ref(100)

  // 计算属性
  const connectionStatus = computed(() => {
    if (isConnected.value) return 'connected'
    if (isConnecting.value) return 'connecting'
    return 'disconnected'
  })

  const canReconnect = computed(() => {
    return reconnectAttempts.value < maxReconnectAttempts.value
  })

  /**
   * 连接WebSocket
   */
  const connect = async () => {
    if (isConnected.value || isConnecting.value) {
      console.log('WebSocket已连接或正在连接中')
      return
    }

    try {
      isConnecting.value = true
      const userStore = useUserStore()
      
      if (!userStore.token) {
        throw new Error('用户未登录，无法建立WebSocket连接')
      }

      // 创建Socket.io连接
      socket.value = io(import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws', {
        auth: {
          token: userStore.token
        },
        transports: ['websocket', 'polling'],
        timeout: 20000,
        reconnection: false, // 手动控制重连
        autoConnect: false
      })

      // 连接事件监听
      socket.value.on('connect', handleConnect)
      socket.value.on('disconnect', handleDisconnect)
      socket.value.on('connect_error', handleConnectError)
      socket.value.on('message', handleMessage)
      socket.value.on('notification', handleNotification)
      socket.value.on('post_update', handlePostUpdate)
      socket.value.on('comment_update', handleCommentUpdate)
      socket.value.on('robot_action', handleRobotAction)

      // 建立连接
      socket.value.connect()
      
      console.log('🔌 正在连接WebSocket...')
    } catch (error) {
      console.error('❌ WebSocket连接失败:', error)
      isConnecting.value = false
      ElMessage.error('WebSocket连接失败')
      throw error
    }
  }

  /**
   * 断开WebSocket连接
   */
  const disconnect = () => {
    if (socket.value) {
      socket.value.disconnect()
      socket.value = null
    }
    isConnected.value = false
    isConnecting.value = false
    reconnectAttempts.value = 0
    console.log('🔌 WebSocket连接已断开')
  }

  /**
   * 发送消息
   * @param {string} event - 事件名称
   * @param {any} data - 消息数据
   */
  const sendMessage = (event, data) => {
    if (!isConnected.value || !socket.value) {
      console.warn('WebSocket未连接，消息已加入队列')
      messageQueue.value.push({ event, data, timestamp: Date.now() })
      return
    }

    try {
      socket.value.emit(event, data)
      console.log('📤 发送消息:', event, data)
    } catch (error) {
      console.error('❌ 发送消息失败:', error)
      ElMessage.error('消息发送失败')
    }
  }

  /**
   * 处理连接成功
   */
  const handleConnect = () => {
    isConnected.value = true
    isConnecting.value = false
    reconnectAttempts.value = 0
    console.log('✅ WebSocket连接成功')
    
    // 发送连接成功消息
    ElMessage.success('实时连接已建立')
    
    // 处理消息队列
    processMessageQueue()
  }

  /**
   * 处理连接断开
   */
  const handleDisconnect = (reason) => {
    isConnected.value = false
    isConnecting.value = false
    console.log('🔌 WebSocket连接断开:', reason)
    
    // 如果不是主动断开，尝试重连
    if (reason !== 'io client disconnect' && canReconnect.value) {
      scheduleReconnect()
    }
  }

  /**
   * 处理连接错误
   */
  const handleConnectError = (error) => {
    isConnecting.value = false
    console.error('❌ WebSocket连接错误:', error)
    
    if (canReconnect.value) {
      scheduleReconnect()
    } else {
      ElMessage.error('WebSocket连接失败，请刷新页面重试')
    }
  }

  /**
   * 处理接收到的消息
   */
  const handleMessage = (message) => {
    console.log('📥 收到消息:', message)
    addToMessageHistory('message', message)
    
    // 根据消息类型处理
    switch (message.type) {
      case 'post':
        handlePostMessage(message)
        break
      case 'comment':
        handleCommentMessage(message)
        break
      case 'notification':
        handleNotificationMessage(message)
        break
      default:
        console.log('未知消息类型:', message.type)
    }
  }

  /**
   * 处理通知消息
   */
  const handleNotification = (notification) => {
    console.log('📢 收到通知:', notification)
    addToMessageHistory('notification', notification)
    
    // 显示通知
    ElMessage({
      message: notification.message,
      type: notification.type || 'info',
      duration: notification.duration || 3000
    })
  }

  /**
   * 处理动态更新
   */
  const handlePostUpdate = (post) => {
    console.log('📝 动态更新:', post)
    addToMessageHistory('post_update', post)
    
    // 触发动态更新事件
    window.dispatchEvent(new CustomEvent('post-update', { detail: post }))
  }

  /**
   * 处理评论更新
   */
  const handleCommentUpdate = (comment) => {
    console.log('💬 评论更新:', comment)
    addToMessageHistory('comment_update', comment)
    
    // 触发评论更新事件
    window.dispatchEvent(new CustomEvent('comment-update', { detail: comment }))
  }

  /**
   * 处理机器人行为
   */
  const handleRobotAction = (action) => {
    console.log('🤖 机器人行为:', action)
    addToMessageHistory('robot_action', action)
    
    // 触发机器人行为事件
    window.dispatchEvent(new CustomEvent('robot-action', { detail: action }))
  }

  /**
   * 处理动态消息
   */
  const handlePostMessage = (message) => {
    // 可以在这里添加特定的动态消息处理逻辑
    console.log('处理动态消息:', message)
  }

  /**
   * 处理评论消息
   */
  const handleCommentMessage = (message) => {
    // 可以在这里添加特定的评论消息处理逻辑
    console.log('处理评论消息:', message)
  }

  /**
   * 处理通知消息
   */
  const handleNotificationMessage = (message) => {
    // 可以在这里添加特定的通知消息处理逻辑
    console.log('处理通知消息:', message)
  }

  /**
   * 安排重连
   */
  const scheduleReconnect = () => {
    if (!canReconnect.value) {
      console.log('已达到最大重连次数')
      return
    }

    reconnectAttempts.value++
    const delay = reconnectDelay.value * Math.pow(2, reconnectAttempts.value - 1)
    
    console.log(`🔄 ${delay}ms后尝试重连 (${reconnectAttempts.value}/${maxReconnectAttempts.value})`)
    
    setTimeout(() => {
      if (!isConnected.value) {
        connect()
      }
    }, delay)
  }

  /**
   * 处理消息队列
   */
  const processMessageQueue = () => {
    if (messageQueue.value.length === 0) return
    
    console.log(`📤 处理消息队列 (${messageQueue.value.length}条消息)`)
    
    while (messageQueue.value.length > 0) {
      const { event, data } = messageQueue.value.shift()
      sendMessage(event, data)
    }
  }

  /**
   * 添加消息到历史记录
   */
  const addToMessageHistory = (type, data) => {
    const message = {
      id: Date.now() + Math.random(),
      type,
      data,
      timestamp: Date.now()
    }
    
    messageHistory.value.unshift(message)
    
    // 限制历史记录数量
    if (messageHistory.value.length > maxMessageHistory.value) {
      messageHistory.value = messageHistory.value.slice(0, maxMessageHistory.value)
    }
  }

  /**
   * 清空消息历史
   */
  const clearMessageHistory = () => {
    messageHistory.value = []
  }

  /**
   * 获取消息历史
   */
  const getMessageHistory = (type = null, limit = 50) => {
    let messages = messageHistory.value
    
    if (type) {
      messages = messages.filter(msg => msg.type === type)
    }
    
    return messages.slice(0, limit)
  }

  /**
   * 设置重连配置
   */
  const setReconnectConfig = (maxAttempts, delay) => {
    maxReconnectAttempts.value = maxAttempts
    reconnectDelay.value = delay
  }

  /**
   * 检查连接状态
   */
  const checkConnection = () => {
    return isConnected.value && socket.value && socket.value.connected
  }

  return {
    // 状态
    socket,
    isConnected,
    isConnecting,
    reconnectAttempts,
    messageQueue,
    messageHistory,
    
    // 计算属性
    connectionStatus,
    canReconnect,
    
    // 方法
    connect,
    disconnect,
    sendMessage,
    clearMessageHistory,
    getMessageHistory,
    setReconnectConfig,
    checkConnection
  }
}) 