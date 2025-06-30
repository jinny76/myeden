import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { Client } from '@stomp/stompjs'
import { message } from '@/utils/message'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores/user'
import { useConfigStore } from '@/stores/config'
import { sendUserOnlineMessage } from '@/api/websocket'

/**
 * WebSocket状态管理
 * 
 * 功能说明：
 * - 管理WebSocket连接状态
 * - 处理实时消息推送
 * - 管理连接重连机制
 * - 处理消息队列和去重
 * - 使用Spring WebSocket + STOMP协议
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

export const useWebSocketStore = defineStore('websocket', () => {
  // 状态定义
  const stompClient = ref(null)
  const isConnected = ref(false)
  const isConnecting = ref(false)
  const reconnectAttempts = ref(0)
  const maxReconnectAttempts = ref(5)
  const reconnectDelay = ref(1000)
  const messageQueue = ref([])
  const messageHistory = ref([])
  const maxMessageHistory = ref(100)
  const subscriptions = ref(new Map())

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
   * 获取WebSocket连接URL
   */
  const getWebSocketUrl = () => {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    return `${protocol}//${host}/ws`
  }

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

      // 根据当前访问的网址推算WebSocket URL
      const wsUrl = getWebSocketUrl()
      console.log('🔌 正在连接WebSocket...', wsUrl)

      // 创建STOMP客户端
      stompClient.value = new Client({
        brokerURL: wsUrl,
        connectHeaders: {
          'Authorization': `Bearer ${userStore.token}`
        },
        debug: function (str) {
          console.log('STOMP Debug:', str)
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000
      })

      // 连接成功回调
      stompClient.value.onConnect = (frame) => {
        console.log('✅ STOMP连接成功:', frame)
        handleConnect()
      }

      // 连接错误回调
      stompClient.value.onStompError = (frame) => {
        console.error('❌ STOMP连接错误:', frame)
        handleConnectError(new Error(frame.headers.message || 'STOMP连接错误'))
      }

      // 连接断开回调
      stompClient.value.onDisconnect = () => {
        console.log('🔌 STOMP连接断开')
        handleDisconnect('STOMP disconnected')
      }

      // 启动连接
      stompClient.value.activate()

    } catch (error) {
      console.error('❌ WebSocket连接失败:', error)
      isConnecting.value = false
      message.error('WebSocket连接失败')
      throw error
    }
  }

  // 开发环境下添加测试函数
  if (import.meta.env.DEV) {
    window.testWebSocketUrl = () => {
      console.log('=== WebSocket URL 测试 ===')
      console.log('当前页面URL:', window.location.href)
      console.log('推算的WebSocket URL:', getWebSocketUrl())
      console.log('环境变量 VITE_WS_URL:', import.meta.env.VITE_WS_URL)
      console.log('STOMP客户端是否可用:', !!Client)
      console.log('=== 测试结束 ===')
    }
  }

  /**
   * 断开WebSocket连接
   */
  const disconnect = () => {
    if (stompClient.value) {
      try {
        // 取消所有订阅
        subscriptions.value.forEach((subscription, id) => {
          subscription.unsubscribe()
          console.log('📡 取消订阅:', id)
        })
        subscriptions.value.clear()
        
        // 断开连接
        stompClient.value.deactivate()
        console.log('🔌 WebSocket连接已断开')
      } catch (error) {
        console.error('❌ 断开WebSocket连接失败:', error)
      } finally {
        isConnected.value = false
        isConnecting.value = false
        stompClient.value = null
      }
    }
  }

  /**
   * 发送消息
   * @param {string} destination - 目标地址
   * @param {any} data - 消息数据
   * @param {object} headers - 消息头
   */
  const sendMessage = (destination, data, headers = {}) => {
    if (!isConnected.value || !stompClient.value) {
      console.warn('WebSocket未连接，消息已加入队列')
      messageQueue.value.push({ destination, data, headers, timestamp: Date.now() })
      return
    }

    try {
      stompClient.value.publish({
        destination: destination,
        headers: headers,
        body: JSON.stringify(data)
      })
      console.log('📤 发送消息:', destination, data)
    } catch (error) {
      console.error('❌ 发送消息失败:', error)
      message.error('消息发送失败')
    }
  }

  /**
   * 订阅主题
   * @param {string} destination - 订阅地址
   * @param {function} callback - 回调函数
   * @param {string} id - 订阅ID
   */
  const subscribe = (destination, callback, id = null) => {
    if (!isConnected.value || !stompClient.value) {
      console.warn('WebSocket未连接，无法订阅')
      return null
    }

    try {
      const subscription = stompClient.value.subscribe(destination, (message) => {
        try {
          const data = JSON.parse(message.body)
          console.log('📥 收到消息:', destination, data)
          addToMessageHistory('message', { destination, data })
          callback(data, message)
        } catch (error) {
          console.error('❌ 解析消息失败:', error)
        }
      })

      const subscriptionId = id || `sub_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
      subscriptions.value.set(subscriptionId, subscription)
      
      console.log('📡 订阅成功:', destination, subscriptionId)
      return subscriptionId
    } catch (error) {
      console.error('❌ 订阅失败:', error)
      return null
    }
  }

  /**
   * 取消订阅
   * @param {string} subscriptionId - 订阅ID
   */
  const unsubscribe = (subscriptionId) => {
    const subscription = subscriptions.value.get(subscriptionId)
    if (subscription) {
      subscription.unsubscribe()
      subscriptions.value.delete(subscriptionId)
      console.log('📡 取消订阅:', subscriptionId)
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
    //ElMessage.success('实时连接已建立')
    
    // 处理消息队列
    processMessageQueue()
    
    // 订阅消息主题
    subscribeToTopics()
    
    // 发送用户上线消息
    sendUserOnlineNotification()
  }

  /**
   * 发送用户上线通知
   */
  const sendUserOnlineNotification = async () => {
    try {
      const userStore = useUserStore()
      if (userStore.userInfo?.userId) {
        const userInfo = {
          nickname: userStore.userInfo.nickname,
          avatar: userStore.userInfo.avatar
        }
        
        await sendUserOnlineMessage(userStore.userInfo.userId, userInfo)
        console.log('📢 用户上线消息已发送')
      }
    } catch (error) {
      console.error('❌ 发送用户上线消息失败:', error)
    }
  }

  /**
   * 订阅消息主题
   */
  const subscribeToTopics = () => {
    // 订阅广播消息
    subscribe('/topic/broadcast', handleBroadcastMessage, 'broadcast')
    
    // 订阅用户个人消息
    const userStore = useUserStore()
    if (userStore.userInfo?.userId) {
      subscribe(`/user/${userStore.userInfo.userId}/queue/messages`, handleUserMessage, 'user-messages')
    }
    
    console.log('📡 WebSocket消息订阅完成')
  }

  /**
   * 处理广播消息
   */
  const handleBroadcastMessage = (message) => {
    console.log('📢 收到广播消息:', message)
    
    try {
      // 根据消息类型处理
      switch (message.type) {
        case 'POST_UPDATE':
          handlePostUpdateMessage(message)
          break
        case 'COMMENT_UPDATE':
          handleCommentUpdateMessage(message)
          break
        case 'ROBOT_ACTION':
          handleRobotActionMessage(message)
          break
        case 'NOTIFICATION':
          handleNotificationMessage(message)
          break
        case 'SYSTEM_MESSAGE':
          handleSystemMessage(message)
          break
        case 'HEARTBEAT':
          // 心跳消息，不需要特殊处理
          console.log('💓 收到心跳消息')
          break
        default:
          console.log('未知消息类型:', message.type)
      }
    } catch (error) {
      console.error('处理广播消息失败:', error)
    }
  }

  /**
   * 处理用户个人消息
   */
  const handleUserMessage = (message) => {
    console.log('👤 收到用户消息:', message)
    
    try {
      // 显示通知
      if (message.title && message.content) {
        message.info(message.content)
      }
    } catch (error) {
      console.error('处理用户消息失败:', error)
    }
  }

  /**
   * 处理动态更新消息
   */
  const handlePostUpdateMessage = (message) => {
    console.log('📝 处理动态更新消息:', message)
    
    // 触发动态列表刷新
    // 这里可以通过事件总线或直接调用store方法来刷新数据
    window.dispatchEvent(new CustomEvent('post-update', { 
      detail: message.data 
    }))
    
    // 显示通知
    if (message.title && message.content) {
      message.success(message.content)
    }
  }

  /**
   * 处理评论更新消息
   */
  const handleCommentUpdateMessage = (message) => {
    console.log('💬 处理评论更新消息:', message)
    
    // 触发评论列表刷新
    window.dispatchEvent(new CustomEvent('comment-update', { 
      detail: message.data 
    }))
    
    // 显示通知
    if (message.title && message.content) {
      message.info(message.content)
    }
  }

  /**
   * 处理通知消息
   */
  const handleNotificationMessage = (message) => {
    console.log('📢 处理通知消息:', message)
    
    // 获取配置Store
    const configStore = useConfigStore()
    
    // 检查是否是用户上线消息
    if (message.title === '用户上线' && message.data) {
      const userData = message.data
      const userStore = useUserStore()
      
      // 如果不是自己上线，且用户开启了上线通知，才显示提示
      if (userStore.userInfo?.userId !== userData.userId && configStore.config.notifications.userOnline) {
        message.info(`${userData.userName} 来到了伊甸园`)
      }
    } else {
      // 显示其他通知
      if (message.title && message.content) {
        message.info(message.content)
      }
    }
  }

  /**
   * 处理系统消息
   */
  const handleSystemMessage = (message) => {
    console.log('🔧 处理系统消息:', message)
    
    // 显示系统通知
    if (message.title && message.content) {
      message.warning(message.content)
    }
  }

  /**
   * 获取行为文本描述
   */
  const getActionText = (actionType) => {
    switch (actionType) {
      case 'post':
        return '发布了新动态'
      case 'comment':
        return '发表了评论'
      case 'like':
        return '点赞了动态'
      case 'reply':
        return '回复了评论'
      default:
        return '执行了操作'
    }
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
      message.error('WebSocket连接失败，请刷新页面重试')
    }
  }

  /**
   * 重新订阅主题
   */
  const resubscribeTopics = () => {
    // 重新订阅之前的主题
    subscribeToTopics()
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
   * 处理动态更新
   */
  const handlePostUpdate = (post) => {
    console.log('📝 动态更新:', post)
    addToMessageHistory('post_update', post)
    
    // 这里可以触发动态列表更新
    // 例如：刷新动态列表、更新特定动态等
  }

  /**
   * 处理评论更新
   */
  const handleCommentUpdate = (comment) => {
    console.log('💬 评论更新:', comment)
    addToMessageHistory('comment_update', comment)
    
    // 这里可以触发评论列表更新
    // 例如：刷新评论列表、更新评论数量等
  }

  /**
   * 处理机器人动作
   */
  const handleRobotAction = (action) => {
    console.log('🤖 机器人动作:', action)
    addToMessageHistory('robot_action', action)
    
    // 这里可以处理机器人相关的实时更新
    // 例如：机器人状态变化、动作执行结果等
  }

  /**
   * 处理动态消息
   */
  const handlePostMessage = (message) => {
    console.log('📝 处理动态消息:', message)
    // 处理动态相关的消息
  }

  /**
   * 处理评论消息
   */
  const handleCommentMessage = (message) => {
    console.log('💬 处理评论消息:', message)
    // 处理评论相关的消息
  }

  /**
   * 处理机器人行为消息
   */
  const handleRobotActionMessage = (message) => {
    console.log('🤖 处理机器人行为消息:', message)
    
    const actionData = message.data
    if (!actionData) return
    
    // 获取配置Store
    const configStore = useConfigStore()
    
    // 根据机器人行为类型处理
    switch (actionData.actionType) {
      case 'post':
        // 机器人发布动态
        window.dispatchEvent(new CustomEvent('robot-post', { 
          detail: actionData 
        }))
        break
      case 'comment':
        // 机器人发表评论
        window.dispatchEvent(new CustomEvent('robot-comment', { 
          detail: actionData 
        }))
        break
      case 'like':
        // 机器人点赞
        window.dispatchEvent(new CustomEvent('robot-like', { 
          detail: actionData 
        }))
        break
      case 'reply':
        // 机器人回复
        window.dispatchEvent(new CustomEvent('robot-reply', { 
          detail: actionData 
        }))
        break
      default:
        console.log('未知机器人行为类型:', actionData.actionType)
    }
    
    // 显示机器人行为通知（根据配置决定）
    if (actionData.robotName && actionData.actionType && configStore.config.notifications.robotAction) {
      const actionText = getActionText(actionData.actionType)
      message.info(`天使 ${actionData.robotName} ${actionText}`)
    }
  }

  /**
   * 安排重连
   */
  const scheduleReconnect = () => {
    if (!canReconnect.value) {
      console.log('❌ 已达到最大重连次数')
      return
    }

    reconnectAttempts.value++
    const delay = reconnectDelay.value * Math.pow(2, reconnectAttempts.value - 1)
    
    console.log(`🔄 ${delay}ms后尝试重连 (${reconnectAttempts.value}/${maxReconnectAttempts.value})`)
    
    setTimeout(() => {
      if (!isConnected.value) {
        connect().catch(error => {
          console.error('❌ 重连失败:', error)
        })
      }
    }, delay)
  }

  /**
   * 处理消息队列
   */
  const processMessageQueue = () => {
    if (messageQueue.value.length === 0) return
    
    console.log(`📤 处理消息队列 (${messageQueue.value.length}条消息)`)
    
    const queue = [...messageQueue.value]
    messageQueue.value = []
    
    queue.forEach(({ destination, data, headers }) => {
      sendMessage(destination, data, headers)
    })
  }

  /**
   * 添加消息到历史记录
   */
  const addToMessageHistory = (type, data) => {
    const message = {
      id: Date.now() + Math.random().toString(36).substr(2, 9),
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
    console.log('🗑️ 消息历史已清空')
  }

  /**
   * 获取消息历史
   */
  const getMessageHistory = (type = null, limit = 50) => {
    let history = messageHistory.value
    
    if (type) {
      history = history.filter(msg => msg.type === type)
    }
    
    return history.slice(0, limit)
  }

  /**
   * 设置重连配置
   */
  const setReconnectConfig = (maxAttempts, delay) => {
    maxReconnectAttempts.value = maxAttempts
    reconnectDelay.value = delay
    console.log(`⚙️ 重连配置已更新: 最大次数=${maxAttempts}, 延迟=${delay}ms`)
  }

  /**
   * 检查连接状态
   */
  const checkConnection = () => {
    return {
      isConnected: isConnected.value,
      isConnecting: isConnecting.value,
      reconnectAttempts: reconnectAttempts.value,
      maxReconnectAttempts: maxReconnectAttempts.value,
      messageQueueLength: messageQueue.value.length,
      messageHistoryLength: messageHistory.value.length,
      subscriptionsCount: subscriptions.value.size
    }
  }

  return {
    // 状态
    isConnected,
    isConnecting,
    connectionStatus,
    canReconnect,
    
    // 方法
    connect,
    disconnect,
    sendMessage,
    subscribe,
    unsubscribe,
    setReconnectConfig,
    checkConnection,
    clearMessageHistory,
    getMessageHistory
  }
})