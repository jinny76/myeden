<template>
  <div id="app" class="app-container">
    <!-- 路由视图 -->
    <router-view v-slot="{ Component, route }">
      <transition name="fade" mode="out-in">
        <component :is="Component" :key="route.path" />
      </transition>
    </router-view>
    
    <!-- 全局消息提示 -->
    <el-backtop :right="40" :bottom="40" />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useWebSocketStore } from '@/stores/websocket'
import { ElMessage } from 'element-plus'

/**
 * 应用根组件
 * 
 * 功能说明：
 * - 提供应用整体布局
 * - 处理路由切换动画
 * - 初始化用户状态和WebSocket连接
 * - 全局错误处理
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

const router = useRouter()
const userStore = useUserStore()
const websocketStore = useWebSocketStore()

// 组件挂载时的初始化
onMounted(async () => {
  try {
    // 初始化用户状态
    await userStore.initUser()
    
    // 如果用户已登录，连接WebSocket
    if (userStore.isLoggedIn) {
      await websocketStore.connect()
    }
    
    console.log('✅ 应用初始化完成')
  } catch (error) {
    console.error('❌ 应用初始化失败:', error)
    ElMessage.error('应用初始化失败，请刷新页面重试')
  }
})

// 组件卸载时的清理
onUnmounted(() => {
  // 断开WebSocket连接
  websocketStore.disconnect()
  console.log('🔌 WebSocket连接已断开')
})

// 全局错误处理
const handleGlobalError = (event) => {
  console.error('全局错误:', event.error)
  ElMessage.error('系统发生错误，请稍后重试')
}

// 注册全局错误监听
onMounted(() => {
  window.addEventListener('error', handleGlobalError)
  window.addEventListener('unhandledrejection', (event) => {
    console.error('未处理的Promise拒绝:', event.reason)
    ElMessage.error('操作失败，请稍后重试')
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