import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { Client } from '@stomp/stompjs'
import { message } from '@/utils/message'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores/user'
import { useConfigStore } from '@/stores/config'
import { sendUserOnlineMessage } from '@/api/websocket'
import { watch } from 'vue'

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
  
  // 防重复发送机制
  let lastOnlineNotificationTime = 0
  const ONLINE_NOTIFICATION_COOLDOWN = 5000 // 5秒冷却时间
  
  // 消息去重机制
  const processedMessages = ref(new Set())
  const MESSAGE_DEDUPLICATION_WINDOW = 10000 // 10秒去重窗口
  
  // 全局连接实例管理
  let globalConnectionId = null
  const CONNECTION_ID = `ws_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`

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
    // 拼接token参数
    const userStore = useUserStore()
    const token = userStore.token
    return `${protocol}//${host}/ws${token ? `?token=${encodeURIComponent(token)}` : ''}`
  }

  /**
   * 连接WebSocket
   */
  const connect = async () => {
    // 检查是否已经有全局连接
    if (globalConnectionId && globalConnectionId !== CONNECTION_ID) {
      console.log('🔌 检测到其他连接实例，跳过连接')
      return
    }
    
    if (isConnected.value || isConnecting.value) {
      console.log('WebSocket已连接或正在连接中')
      return
    }

    try {
      isConnecting.value = true
      globalConnectionId = CONNECTION_ID
      
      const userStore = useUserStore()
      
      if (!userStore.token) {
        throw new Error('用户未登录，无法建立WebSocket连接')
      }

      // 根据当前访问的网址推算WebSocket URL
      const wsUrl = getWebSocketUrl()
      console.log('🔌 正在连接WebSocket...', wsUrl, '连接ID:', CONNECTION_ID)

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
        console.log('✅ STOMP连接成功:', frame, '连接ID:', CONNECTION_ID)
        console.log('🔍 连接详情:', {
          sessionId: frame.headers['user-name'],
          connected: true,
          timestamp: new Date().toISOString(),
          connectionId: CONNECTION_ID
        })
        handleConnect()
      }

      // 连接错误回调
      stompClient.value.onStompError = (frame) => {
        console.error('❌ STOMP连接错误:', frame, '连接ID:', CONNECTION_ID)
        handleConnectError(new Error(frame.headers.message || 'STOMP连接错误'))
      }

      // 连接断开回调
      stompClient.value.onDisconnect = () => {
        console.log('🔌 STOMP连接断开', '连接ID:', CONNECTION_ID)
        handleDisconnect('STOMP disconnected')
      }

      // 启动连接
      stompClient.value.activate()

    } catch (error) {
      console.error('❌ WebSocket连接失败:', error, '连接ID:', CONNECTION_ID)
      isConnecting.value = false
      globalConnectionId = null
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
        console.log('🔌 WebSocket连接已断开', '连接ID:', CONNECTION_ID)
      } catch (error) {
        console.error('❌ 断开WebSocket连接失败:', error)
      } finally {
        isConnected.value = false
        isConnecting.value = false
        stompClient.value = null
        // 清理全局连接实例
        if (globalConnectionId === CONNECTION_ID) {
          globalConnectionId = null
        }
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
      const subscription = stompClient.value.subscribe(destination, (stompMessage) => {
        try {
          const data = JSON.parse(stompMessage.body)
          console.log('📥 收到消息:', destination, data)
          addToMessageHistory('message', { destination, data })
          callback(data, stompMessage)
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
    console.log('✅ WebSocket连接已建立')
    
    // 订阅消息主题
    subscribeToTopics()
    
    // 发送用户上线消息
    console.log('📢 准备发送用户上线消息...')
    sendUserOnlineNotification()
  }

  /**
   * 发送用户上线通知
   */
  const sendUserOnlineNotification = async () => {
    try {
      // 检查冷却时间，避免重复发送
      const now = Date.now()
      if (now - lastOnlineNotificationTime < ONLINE_NOTIFICATION_COOLDOWN) {
        console.log('⏰ 用户上线消息发送过于频繁，跳过', {
          timeSinceLast: now - lastOnlineNotificationTime,
          cooldown: ONLINE_NOTIFICATION_COOLDOWN
        })
        return
      }
      
      const userStore = useUserStore()
      if (userStore.userInfo?.userId) {
        const userInfo = {
          nickname: userStore.userInfo.nickname,
          avatar: userStore.userInfo.avatar
        }
        
        console.log('📢 WebSocket Store准备发送用户上线消息:', {
          userId: userStore.userInfo.userId,
          userInfo,
          timestamp: new Date().toISOString(),
          stack: new Error().stack
        })
        
        await sendUserOnlineMessage(userStore.userInfo.userId, userInfo)
        lastOnlineNotificationTime = now
        console.log('📢 WebSocket Store用户上线消息已发送，时间戳:', now)
      }
    } catch (error) {
      console.error('❌ 发送用户上线消息失败:', error)
    }
  }

  /**
   * 订阅消息主题
   */
  const subscribeToTopics = () => {
    // 先取消所有现有订阅
    subscriptions.value.forEach((subscription, id) => {
      subscription.unsubscribe()
      console.log('📡 取消旧订阅:', id)
    })
    subscriptions.value.clear()
    
    // 订阅广播消息
    subscribe('/topic/broadcast', handleBroadcastMessage, 'broadcast')
    
    // 订阅用户个人消息
    const userStore = useUserStore()
    if (userStore.userInfo?.userId) {
      const userId = userStore.userInfo.userId
      subscribe(`/topic/${userId}/queue/messages`, handleUserMessage, `user-messages-${userId}`)
    } else {
      console.warn('未检测到用户ID，无法订阅个人消息')
    }
    
    console.log('📡 WebSocket消息订阅完成')
  }

  // 监听用户登录状态变化，自动重新订阅和重连
  const userStore = useUserStore()
  watch(() => userStore.userInfo?.userId, (newUserId, oldUserId) => {
    if (newUserId && newUserId !== oldUserId) {
      disconnect()
      connect()
    }
  })
  // 监听token变化，token变化时自动断开重连
  watch(() => userStore.token, (newToken, oldToken) => {
    if (newToken && newToken !== oldToken) {
      disconnect()
      connect()
    }
  })

  /**
   * 检查消息是否已处理过（去重）
   */
  const isMessageProcessed = (messageId) => {
    if (!messageId) return false
    
    const now = Date.now()
    const processed = processedMessages.value.has(messageId)
    
    if (processed) {
      console.log('🔄 消息已处理过，跳过:', messageId)
      return true
    }
    
    // 添加到已处理集合
    processedMessages.value.add(messageId)
    
    // 10秒后自动清理
    setTimeout(() => {
      processedMessages.value.delete(messageId)
    }, MESSAGE_DEDUPLICATION_WINDOW)
    
    return false
  }

  /**
   * 处理广播消息
   */
  const handleBroadcastMessage = (wsMessage) => {
    console.log('📢 收到广播消息:', wsMessage)
    
    // 检查消息去重
    if (isMessageProcessed(wsMessage.messageId || `${wsMessage.type}_${Date.now()}`)) {
      return
    }
    
    try {
      // 根据消息类型处理
      switch (wsMessage.type) {
        case 'POST_UPDATE':
          handlePostUpdateMessage(wsMessage)
          break
        case 'COMMENT_UPDATE':
          handleCommentUpdateMessage(wsMessage)
          break
        case 'ROBOT_ACTION':
          handleRobotActionMessage(wsMessage)
          break
        case 'NOTIFICATION':
          handleNotificationMessage(wsMessage)
          break
        case 'SYSTEM_MESSAGE':
          handleSystemMessage(wsMessage)
          break
        case 'HEARTBEAT':
          // 心跳消息，不需要特殊处理
          console.log('💓 收到心跳消息')
          break
        case 'CHAT':
          window.dispatchEvent(new CustomEvent('ai-chat-message', { detail: wsMessage.data }))
          break
        default:
          console.log('未知消息类型:', wsMessage.type)
      }
    } catch (error) {
      console.error('处理广播消息失败:', error)
    }
  }

  /**
   * 处理用户个人消息
   */
  const handleUserMessage = (wsMessage) => {
    console.log('👤 收到用户消息:', wsMessage)
    
    // 检查消息去重
    if (isMessageProcessed(wsMessage.messageId || `user_${Date.now()}`)) {
      return
    }
    
    try {
      // 只处理用户上线消息，其他消息不显示提示
      if (wsMessage.title === '用户上线' && wsMessage.data) {
        const userData = wsMessage.data
        const userStore = useUserStore()
        const configStore = useConfigStore()
        
        console.log('👤 收到用户上线消息:', {
          userData,
          currentUserId: userStore.userInfo?.userId,
          isOwnMessage: userStore.userInfo?.userId === userData.userId,
          notificationsEnabled: configStore.config.notifications.userOnline
        })
        
        // 如果不是自己上线，且用户开启了上线通知，才显示提示
        if (userStore.userInfo?.userId !== userData.userId && configStore.config.notifications.userOnline) {
          console.log('📢 显示用户上线通知:', `${userData.userName} 来到了伊甸园`)
          message.info(`${userData.userName} 来到了伊甸园`)
        } else {
          console.log('📢 跳过用户上线通知:', {
            reason: userStore.userInfo?.userId === userData.userId ? '自己的消息' : '通知已关闭'
          })
        }
      } else {
        if (wsMessage.type === 'CHAT') {
          window.dispatchEvent(new CustomEvent('ai-chat-message', { detail: wsMessage.data }))
        }
        // 其他用户消息不显示提示，只记录日志
        console.log('📢 收到其他用户消息，不显示提示:', wsMessage.title, wsMessage.content)
      }
    } catch (error) {
      console.error('处理用户消息失败:', error)
    }
  }

  /**
   * 处理动态更新消息
   */
  const handlePostUpdateMessage = (wsMessage) => {
    console.log('📝 处理动态更新消息:', wsMessage)
    
    // 触发动态列表刷新
    // 这里可以通过事件总线或直接调用store方法来刷新数据
    window.dispatchEvent(new CustomEvent('post-update', { 
      detail: wsMessage.data 
    }))
    
    // 不显示通知，只记录日志
    console.log('📝 动态更新消息已处理，不显示提示')
  }

  /**
   * 处理评论更新消息
   */
  const handleCommentUpdateMessage = (wsMessage) => {
    console.log('💬 处理评论更新消息:', wsMessage)
    
    // 触发评论列表刷新
    window.dispatchEvent(new CustomEvent('comment-update', { 
      detail: wsMessage.data 
    }))
    
    // 不显示通知，只记录日志
    console.log('💬 评论更新消息已处理，不显示提示')
  }

  /**
   * 处理通知消息
   */
  const handleNotificationMessage = (wsMessage) => {
    console.log('📢 处理通知消息:', wsMessage)
    
    // 获取配置Store
    const configStore = useConfigStore()
    
    // 检查是否是用户上线消息
    if (wsMessage.title === '用户上线' && wsMessage.data) {
      const userData = wsMessage.data
      const userStore = useUserStore()
      
      console.log('👤 收到用户上线消息:', {
        userData,
        currentUserId: userStore.userInfo?.userId,
        isOwnMessage: userStore.userInfo?.userId === userData.userId,
        notificationsEnabled: configStore.config.notifications.userOnline
      })
      
      // 如果不是自己上线，且用户开启了上线通知，才显示提示
      if (userStore.userInfo?.userId !== userData.userId && configStore.config.notifications.userOnline) {
        console.log('📢 显示用户上线通知:', `${userData.userName} 来到了伊甸园`)
        message.info(`${userData.userName} 来到了伊甸园`)
      } else {
        console.log('📢 跳过用户上线通知:', {
          reason: userStore.userInfo?.userId === userData.userId ? '自己的消息' : '通知已关闭'
        })
      }
    } else {
      // 其他通知消息不显示提示，只记录日志
      console.log('📢 收到其他通知消息，不显示提示:', wsMessage.title, wsMessage.content)
    }
  }

  /**
   * 处理系统消息
   */
  const handleSystemMessage = (wsMessage) => {
    console.log('🔧 处理系统消息:', wsMessage)
    
    // 不显示系统通知，只记录日志
    console.log('🔧 系统消息已处理，不显示提示')
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
  const handleMessage = (wsMessage) => {
    console.log('📥 收到消息:', wsMessage)
    addToMessageHistory('message', wsMessage)
    
    // 根据消息类型处理
    switch (wsMessage.type) {
      case 'post':
        handlePostMessage(wsMessage)
        break
      case 'comment':
        handleCommentMessage(wsMessage)
        break
      case 'notification':
        handleNotificationMessage(wsMessage)
        break
      default:
        console.log('未知消息类型:', wsMessage.type)
    }
  }

  /**
   * 处理动态消息
   */
  const handlePostMessage = (wsMessage) => {
    console.log('📝 处理动态消息:', wsMessage)
    // 处理动态相关的消息
  }

  /**
   * 处理评论消息
   */
  const handleCommentMessage = (wsMessage) => {
    console.log('💬 处理评论消息:', wsMessage)
    // 处理评论相关的消息
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
   * 处理机器人行为消息
   */
  const handleRobotActionMessage = (wsMessage) => {
    console.log('🤖 处理机器人行为消息:', wsMessage)
    
    const actionData = wsMessage.data
    if (!actionData) return
    
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
    
    // 不显示机器人行为通知，只记录日志
    console.log('🤖 机器人行为消息已处理，不显示提示')
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
      subscriptionsCount: subscriptions.value.size,
      connectionId: CONNECTION_ID,
      globalConnectionId: globalConnectionId
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
    sendUserOnlineNotification,
    setReconnectConfig,
    checkConnection,
    clearMessageHistory,
    getMessageHistory
  }
})