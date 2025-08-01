import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'

// 样式导入
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './styles/index.scss'

// 工具库导入
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

// 开发环境测试工具


// 配置dayjs
dayjs.locale('zh-cn')

/**
 * Vue应用主入口
 * 
 * 功能说明：
 * - 创建Vue应用实例
 * - 配置路由、状态管理
 * - 注册Element Plus图标
 * - 挂载应用到DOM
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

// 创建应用实例
const app = createApp(App)

// 创建Pinia状态管理
const pinia = createPinia()

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 全局属性配置
app.config.globalProperties.$dayjs = dayjs

// 全局错误处理
app.config.errorHandler = (err, vm, info) => {
  console.error('全局错误:', err)
  console.error('错误信息:', info)
  console.error('组件:', vm)
}

// 使用插件
app.use(pinia)
app.use(router)

// 挂载应用
app.mount('#app')

/**
 * 注册 Service Worker，确保通知功能在支持的浏览器下可用
 * 只在生产环境和支持的浏览器下注册
 */
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js')
      .then(reg => {
        console.log('Service Worker 注册成功:', reg)
      })
      .catch(err => {
        console.error('Service Worker 注册失败:', err)
      })
  })
}

// 开发环境下的调试信息
if (import.meta.env.DEV) {
  console.log('🚀 我的伊甸园 - 开发环境启动成功')
  console.log('📱 前端地址: http://localhost:5173')
  console.log('🔧 后端地址: http://localhost:8080')
  console.log('🌐 WebSocket: ws://localhost:8080/ws')
  

} 