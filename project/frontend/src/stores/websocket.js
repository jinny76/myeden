import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { Client } from '@stomp/stompjs'
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
      ElMessage.error('WebSocket连接失败')
      throw error
    }
  }

  /**
   * 根据当前访问的网址推算WebSocket URL
   */
  const getWebSocketUrl = () => {
    // 优先使用环境变量配置
    if (import.meta.env.VITE_WS_URL) {
      return import.meta.env.VITE_WS_URL
    }

    // 根据当前页面URL推算WebSocket URL
    const currentUrl = window.location
    const protocol = currentUrl.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = currentUrl.hostname
    const port = currentUrl.port || (currentUrl.protocol === 'https:' ? '443' : '80')
    
    // 如果是开发环境，使用默认的WebSocket端口
    if (import.meta.env.DEV) {
      return `${protocol}//${host}:38080/ws`
    }
    
    // 生产环境使用相同的主机和端口
    return `${protocol}//${host}:${port}/ws`
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
    if (stompClient.value && isConnected.value) {
      stompClient.value.deactivate()
      stompClient.value = null
    }
    isConnected.value = false
    isConnecting.value = false
    reconnectAttempts.value = 0
    
    // 清理订阅
    subscriptions.value.clear()
    
    console.log('🔌 WebSocket连接已断开')
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
      ElMessage.error('消息发送失败')
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
    ElMessage.success('实时连接已建立')
    
    // 处理消息队列
    processMessageQueue()
    
    // 重新订阅之前的主题
    resubscribeTopics()
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
   * 重新订阅主题
   */
  const resubscribeTopics = () => {
    // 这里可以保存和恢复之前的订阅
    console.log('🔄 重新订阅主题')
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
   * 处理通知消息
   */
  const handleNotificationMessage = (message) => {
    console.log('📢 处理通知消息:', message)
    // 处理通知相关的消息
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