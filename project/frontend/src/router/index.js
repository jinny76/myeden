import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

/**
 * 路由配置
 * 
 * 功能说明：
 * - 定义应用的所有页面路由
 * - 配置路由守卫和权限控制
 * - 处理路由切换和导航
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

// 路由配置
const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: {
      title: '首页 - 我的伊甸园',
      requiresAuth: false
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: {
      title: '登录 - 我的伊甸园',
      requiresAuth: false
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: {
      title: '注册 - 我的伊甸园',
      requiresAuth: false
    }
  },
  {
    path: '/profile-setup',
    name: 'ProfileSetup',
    component: () => import('@/views/ProfileSetup.vue'),
    meta: {
      title: '完善资料 - 我的伊甸园',
      requiresAuth: true
    }
  },
  {
    path: '/world',
    name: 'World',
    component: () => import('@/views/World.vue'),
    meta: {
      title: '虚拟世界 - 我的伊甸园',
      requiresAuth: true
    }
  },
  {
    path: '/moments',
    name: 'Moments',
    component: () => import('@/views/Moments.vue'),
    meta: {
      title: '朋友圈 - 我的伊甸园',
      requiresAuth: true
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 路由切换时的滚动行为
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }
  
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    const userStore = useUserStore()
    
    // 检查用户是否已登录
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      next('/login')
      return
    }
    
    // 检查用户状态是否有效
    try {
      await userStore.checkAuth()
    } catch (error) {
      console.error('认证检查失败:', error)
      ElMessage.error('登录状态已过期，请重新登录')
      userStore.logout()
      next('/login')
      return
    }
  }
  
  // 如果已登录用户访问登录/注册页面，重定向到首页
  if (to.name === 'Login' || to.name === 'Register') {
    const userStore = useUserStore()
    if (userStore.isLoggedIn) {
      next('/')
      return
    }
  }
  
  next()
})

// 全局后置钩子
router.afterEach((to, from) => {
  // 路由切换后的处理
  console.log(`路由切换: ${from.path} -> ${to.path}`)
  
  // 更新用户活跃时间
  const userStore = useUserStore()
  if (userStore.isLoggedIn) {
    userStore.updateActiveTime()
  }
})

// 路由错误处理
router.onError((error) => {
  console.error('路由错误:', error)
  ElMessage.error('页面加载失败，请稍后重试')
})

export default router 