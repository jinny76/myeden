<template>
  <div id="app">
  <router-view />
    <ToastMessage ref="toastRef" />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
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
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

const router = useRouter()
const userStore = useUserStore()
const websocketStore = useWebSocketStore()
const configStore = useConfigStore()

/**
 * 发送用户上线消息
 */
const sendUserOnlineNotification = async () => {
  try {
    // 只有当用户开启了上线通知时才发送消息
    if (userStore.userInfo?.userId && websocketStore.isConnected && configStore.config.notifications.userOnline) {
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
 * 处理页面可见性变化
 */
const handleVisibilityChange = () => {
  if (!document.hidden && userStore.isLoggedIn) {
    // 页面变为可见时，发送用户上线消息
    console.log('👁️ 页面变为可见，发送上线消息')
    sendUserOnlineNotification()
  }
}

/**
 * 处理窗口焦点变化
 */
const handleWindowFocus = () => {
  if (userStore.isLoggedIn) {
    // 窗口获得焦点时，发送用户上线消息
    console.log('🎯 窗口获得焦点，发送上线消息')
    sendUserOnlineNotification()
  }
}

// 组件挂载时的初始化
onMounted(async () => {
  try {
    // 初始化用户状态
    const initSuccess = await userStore.initUser()
    
    // 应用主题配置
    configStore.applyTheme()
    
    // 如果用户已登录，连接WebSocket
    if (userStore.isLoggedIn && initSuccess) {
      await websocketStore.connect()
    }
    
    // 添加页面可见性监听
    document.addEventListener('visibilitychange', handleVisibilityChange)
    
    // 添加窗口焦点监听
    window.addEventListener('focus', handleWindowFocus)
    
    console.log('✅ 应用初始化完成')
  } catch (error) {
    console.error('❌ 应用初始化失败:', error)
    // 不显示错误消息，让用户正常使用
  }
})

// 组件卸载时的清理
onUnmounted(() => {
  // 断开WebSocket连接
  websocketStore.disconnect()
  console.log('🔌 WebSocket连接已断开')
  
  // 移除事件监听
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.removeEventListener('focus', handleWindowFocus)
})

// 全局错误处理
const handleGlobalError = (event) => {
  console.error('全局错误:', event.error)
  message.error('系统发生错误，请稍后重试')
}

// 注册全局错误监听
onMounted(() => {
  window.addEventListener('error', handleGlobalError)
  window.addEventListener('unhandledrejection', (event) => {
    console.error('未处理的Promise拒绝:', event.reason)
    message.error('操作失败，请稍后重试')
  })
})

onUnmounted(() => {
  window.removeEventListener('error', handleGlobalError)
})
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