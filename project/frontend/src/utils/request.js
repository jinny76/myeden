import axios from 'axios'
import { ElMessageBox } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'
import { useUserStore } from '@/stores/user'
import { message } from '@/utils/message'
import router from '@/router'

/**
 * HTTP请求工具
 * 
 * 功能说明：
 * - 配置axios实例
 * - 请求和响应拦截器
 * - 统一错误处理
 * - Token自动添加和刷新
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 添加Token到请求头
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 添加请求时间戳
    config.headers['X-Request-Time'] = Date.now()
    
    // 对于文件上传，不设置默认的Content-Type，让浏览器自动设置
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    
    console.log('🚀 发送请求:', config.method?.toUpperCase(), config.url)
    return config
  },
  (error) => {
    console.error('❌ 请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    console.log('✅ 响应成功:', response.config.url, response.status)
    
    // 处理业务错误
    const { code, message: responseMessage, data } = response.data
    
    if (code === 200) {
      return response.data
    } else if (code === 401) {
      // Token过期或无效
      handleTokenExpired()
      return Promise.reject(new Error(responseMessage || '登录已过期'))
    } else if (code === 403) {
      // 权限不足
      message.error(responseMessage || '权限不足')
      return Promise.reject(new Error(responseMessage || '权限不足'))
    } else {
      // 其他业务错误
      message.error(responseMessage || '请求失败')
      return Promise.reject(new Error(responseMessage || '请求失败'))
    }
  },
  (error) => {
    console.error('❌ 响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 400:
          message.error(data?.message || '请求参数错误')
          break
        case 401:
          handleTokenExpired()
          break
        case 403:
          message.error('权限不足')
          break
        case 404:
          message.silentError('请求的资源不存在')
          break
        case 500:
          message.silentError('服务器内部错误')
          break
        case 502:
          message.silentError('网关错误')
          break
        case 503:
          message.silentError('服务不可用')
          break
        default:
          message.smartError(data?.message || '网络错误', error)
      }
    } else if (error.request) {
      // 网络错误
      message.silentError('网络连接失败，请检查网络设置', error)
    } else {
      // 其他错误
      message.smartError(error.message || '请求失败', error)
    }
    
    return Promise.reject(error)
  }
)

/**
 * 处理Token过期
 */
const handleTokenExpired = async () => {
  const userStore = useUserStore()
  
  try {
    // 尝试刷新Token
    await userStore.refreshToken()
    message.lightSuccess('登录状态已刷新')
  } catch (error) {
    // 刷新失败，清除用户状态并跳转到登录页
    userStore.logout()
    
    await ElMessageBox.alert(
      '登录已过期，请重新登录',
      '提示',
      {
        confirmButtonText: '确定',
        type: 'warning'
      }
    )
    
    router.push('/login')
  }
}

/**
 * 重试机制
 * @param {Function} fn - 要重试的函数
 * @param {number} maxRetries - 最大重试次数
 * @param {number} delay - 重试延迟（毫秒）
 */
export const retryRequest = async (fn, maxRetries = 3, delay = 1000) => {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await fn()
    } catch (error) {
      if (i === maxRetries - 1) {
        throw error
      }
      
      console.log(`重试请求 (${i + 1}/${maxRetries})`)
      await new Promise(resolve => setTimeout(resolve, delay))
    }
  }
}

/**
 * 取消请求的Token
 */
export const createCancelToken = () => {
  return axios.CancelToken.source()
}

/**
 * 检查是否为取消请求错误
 */
export const isCancelRequest = (error) => {
  return axios.isCancel(error)
}

export default service 