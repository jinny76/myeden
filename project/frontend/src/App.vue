<template>
  <div id="app">
  <router-view />
    <ToastMessage ref="toastRef" />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useWebSocketStore } from '@/stores/websocket'
import { useConfigStore } from '@/stores/config'
import { message } from '@/utils/message'
import { getToken, removeToken } from '@/utils/auth'
import ToastMessage from '@/components/ToastMessage.vue'
import { sendUserOnlineMessage } from '@/api/websocket'

/**
 * 应用根组件
 * 
 * 功能说明：
 * - 提供应用整体布局
 * - 处理路由切换动画
 * - 初始化用户状态和WebSocket连接
 * - 全局错误处理
 * - 页面可见性监听和用户上线消息
 * - 管理WebSocket帖子相关事件监听（在无法增量刷新时停止）
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

const router = useRouter()
const userStore = useUserStore()
const websocketStore = useWebSocketStore()
const configStore = useConfigStore()

// 防重复发送机制
let lastNotificationTime = 0
const NOTIFICATION_COOLDOWN = 5000 // 5秒冷却时间，与WebSocket Store保持一致
let notificationTimeout = null

// WebSocket帖子相关事件监听状态
let postEventListeners = {
  'post-update': null,
  'comment-update': null,
  'robot-post': null,
  'robot-comment': null,
  'robot-like': null,
  'robot-reply': null
}

// 增量刷新能力检测
const canIncrementalRefresh = ref(true) // 默认假设支持增量刷新

// PWA 相关状态 - 已关闭
// const pwaUpdateAvailable = ref(false)
// const pwaRegistration = ref(null)

/**
 * 停止监听WebSocket帖子相关事件
 * 在无法增量刷新的情况下，避免全量刷新影响性能
 */
const stopPostEventListeners = () => {
  console.log('🛑 停止监听WebSocket帖子相关事件')
  
  Object.keys(postEventListeners).forEach(eventType => {
    if (postEventListeners[eventType]) {
      window.removeEventListener(eventType, postEventListeners[eventType])
      postEventListeners[eventType] = null
      console.log(`🛑 已停止监听事件: ${eventType}`)
    }
  })
}

/**
 * 启动监听WebSocket帖子相关事件
 * 仅在支持增量刷新的情况下启用
 */
const startPostEventListeners = () => {
  if (!canIncrementalRefresh.value) {
    console.log('⚠️ 检测到无法增量刷新，跳过启动帖子相关事件监听')
    return
  }
  
  console.log('✅ 启动监听WebSocket帖子相关事件')
  
  // 定义事件处理函数
  const handlePostUpdate = async () => {
    console.log('📝 收到动态更新事件，执行增量刷新')
    // 这里可以添加增量刷新逻辑
  }
  
  const handleCommentUpdate = async () => {
    console.log('💬 收到评论更新事件，执行增量刷新')
    // 这里可以添加增量刷新逻辑
  }
  
  const handleRobotAction = async () => {
    console.log('🤖 收到机器人行为事件，执行增量刷新')
    // 这里可以添加增量刷新逻辑
  }
  
  // 注册事件监听器
  postEventListeners['post-update'] = handlePostUpdate
  postEventListeners['comment-update'] = handleCommentUpdate
  postEventListeners['robot-post'] = handleRobotAction
  postEventListeners['robot-comment'] = handleRobotAction
  postEventListeners['robot-like'] = handleRobotAction
  postEventListeners['robot-reply'] = handleRobotAction
  
  // 添加事件监听
  Object.entries(postEventListeners).forEach(([eventType, handler]) => {
    if (handler) {
      window.addEventListener(eventType, handler)
      console.log(`✅ 已启动监听事件: ${eventType}`)
    }
  })
}

/**
 * 检测增量刷新能力
 * 可以根据实际情况判断是否支持增量刷新
 */
const detectIncrementalRefreshCapability = () => {
  // 这里可以添加检测逻辑，例如：
  // - 检查API是否支持增量更新
  // - 检查前端状态管理是否支持增量更新
  // - 检查网络环境是否适合增量更新
  
  // 暂时设置为false，表示无法增量刷新
  canIncrementalRefresh.value = false
  console.log('🔍 增量刷新能力检测结果:', canIncrementalRefresh.value)
}

/**
 * 注册 Service Worker - 已关闭
 */
const registerServiceWorker = async () => {
  // PWA 功能已关闭
  console.log('🚫 PWA 功能已关闭，跳过 Service Worker 注册')
  return
}

/**
 * 请求通知权限
 */
const requestNotificationPermission = async () => {
  if ('Notification' in window) {
    if (Notification.permission === 'default') {
      const permission = await Notification.requestPermission()
      console.log('📱 通知权限状态:', permission)
    }
  }
}

/**
 * 发送推送通知
 */
const sendPushNotification = (title, body, data = {}) => {
  if ('Notification' in window && Notification.permission === 'granted') {
    const notification = new Notification(title, {
      body,
      icon: 'icons/icon-192x192.png',
      badge: 'icons/badge-72x72.png',
      data
    })
    
    notification.onclick = () => {
      window.focus()
      notification.close()
    }
  }
}

/**
 * 动态控制帖子相关事件监听
 * @param {boolean} enable - 是否启用监听
 */
const togglePostEventListeners = (enable) => {
  if (enable) {
    canIncrementalRefresh.value = true
    startPostEventListeners()
    console.log('✅ 已启用帖子相关事件监听')
  } else {
    canIncrementalRefresh.value = false
    stopPostEventListeners()
    console.log('🛑 已禁用帖子相关事件监听')
  }
}

// 暴露给全局，供其他组件调用
window.togglePostEventListeners = togglePostEventListeners

/**
 * 发送用户上线消息
 */
const sendUserOnlineNotification = async () => {
  try {
    // 检查冷却时间，避免重复发送
    const now = Date.now()
    if (now - lastNotificationTime < NOTIFICATION_COOLDOWN) {
      console.log('⏰ 用户上线消息发送过于频繁，跳过', {
        timeSinceLast: now - lastNotificationTime,
        cooldown: NOTIFICATION_COOLDOWN
      })
      return
    }
    
    // 使用WebSocket Store中的发送函数
    if (userStore.userInfo?.userId && websocketStore.isConnected) {
      console.log('📢 App.vue调用WebSocket Store发送用户上线消息')
      await websocketStore.sendUserOnlineNotification()
      lastNotificationTime = now
      console.log('✅ 用户上线消息发送成功，时间戳:', now)
    } else {
      console.log('❌ 无法发送用户上线消息:', {
        hasUserId: !!userStore.userInfo?.userId,
        websocketConnected: websocketStore.isConnected,
        userLoggedIn: userStore.isLoggedIn
      })
    }
  } catch (error) {
    console.error('❌ 发送用户上线消息失败:', error)
    // 不更新lastNotificationTime，允许重试
  }
}

/**
 * 防抖发送用户上线消息
 */
const debouncedSendNotification = () => {
  // 清除之前的定时器
  if (notificationTimeout) {
    clearTimeout(notificationTimeout)
  }
  
  // 设置新的定时器，延迟100ms执行
  notificationTimeout = setTimeout(() => {
    sendUserOnlineNotification()
  }, 100)
}

/**
 * 处理页面可见性变化
 */
const handleVisibilityChange = () => {
  if (!document.hidden && userStore.isLoggedIn) {
    // 页面变为可见时，发送用户上线消息
    console.log('👁️ 页面变为可见，发送上线消息')
    debouncedSendNotification()
  }
}

/**
 * 处理窗口焦点变化
 */
const handleWindowFocus = () => {
  if (userStore.isLoggedIn) {
    // 窗口获得焦点时，发送用户上线消息
    console.log('🎯 窗口获得焦点，发送上线消息')
    debouncedSendNotification()
  }
}

// 组件挂载时的初始化
onMounted(async () => {
  try {
    // 初始化用户状态
    const initSuccess = await userStore.initUser()
    
    // 加载用户设置（如果用户已登录）
    if (userStore.isLoggedIn) {
      await configStore.loadUserSetting()
    }
    
    // 应用主题配置
    configStore.applyTheme()
    
    // 检测增量刷新能力
    detectIncrementalRefreshCapability()
    
    // 注册 Service Worker - 已关闭
    // await registerServiceWorker()
    
    // 请求通知权限
    await requestNotificationPermission()
    
    // 如果用户已登录，连接WebSocket
    if (userStore.isLoggedIn && initSuccess) {
      await websocketStore.connect()
    }
    
    // 根据增量刷新能力决定是否启动帖子相关事件监听
    if (canIncrementalRefresh.value) {
      startPostEventListeners()
    } else {
      console.log('⚠️ 无法增量刷新，已停止监听帖子相关事件')
    }
    
    // 添加页面可见性监听
    document.addEventListener('visibilitychange', handleVisibilityChange)
    
    // 添加窗口焦点监听
    window.addEventListener('focus', handleWindowFocus)
    
    // 注册全局错误监听
    window.addEventListener('error', handleGlobalError)
    window.addEventListener('unhandledrejection', (event) => {
      console.error('未处理的Promise拒绝:', event.reason)
      message.error('操作失败，请稍后重试')
    })
    
    console.log('✅ 应用初始化完成')
  } catch (error) {
    console.error('❌ 应用初始化失败:', error)
    // 不显示错误消息，让用户正常使用
  }
})

// 组件卸载时的清理
onUnmounted(() => {
  // 清理定时器
  if (notificationTimeout) {
    clearTimeout(notificationTimeout)
    notificationTimeout = null
  }
  
  // 停止监听帖子相关事件
  stopPostEventListeners()
  
  // 断开WebSocket连接
  websocketStore.disconnect()
  console.log('🔌 WebSocket连接已断开')
  
  // 移除事件监听
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.removeEventListener('focus', handleWindowFocus)
  window.removeEventListener('error', handleGlobalError)
})

// 全局错误处理
const handleGlobalError = (event) => {
  console.error('全局错误:', event.error)
  message.error('系统发生错误，请稍后重试')
}
</script>

<style lang="scss" scoped>
.app-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
}

// 路由切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 响应式设计
@media (max-width: 768px) {
  .app-container {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }
}

// 深色模式支持
@media (prefers-color-scheme: dark) {
  .app-container {
    background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  }
}
</style> 