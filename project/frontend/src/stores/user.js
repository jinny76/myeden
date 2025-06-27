import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '@/api/user'
import { setToken, getToken, removeToken } from '@/utils/auth'

/**
 * 用户状态管理
 * 
 * 功能说明：
 * - 管理用户登录状态
 * - 处理用户信息存储和更新
 * - 提供用户相关操作方法
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

export const useUserStore = defineStore('user', () => {
  // 状态定义
  const token = ref(getToken() || '')
  const userInfo = ref(null)
  const isLoggedIn = ref(false)
  const loading = ref(false)

  // 计算属性
  const userId = computed(() => userInfo.value?.userId || '')
  const nickname = computed(() => userInfo.value?.nickname || '')
  const avatar = computed(() => userInfo.value?.avatar || '')
  const phone = computed(() => userInfo.value?.phone || '')

  /**
   * 初始化用户状态
   */
  const initUser = async () => {
    try {
      const savedToken = getToken()
      if (savedToken) {
        token.value = savedToken
        await fetchUserInfo()
      }
    } catch (error) {
      console.error('初始化用户状态失败:', error)
      logout()
    }
  }

  /**
   * 用户登录
   * @param {Object} loginData - 登录数据
   * @param {string} loginData.phone - 手机号
   * @param {string} loginData.password - 密码
   */
  const login = async (loginData) => {
    try {
      loading.value = true
      const response = await userApi.login(loginData)
      
      // 适配新的后端响应格式 (EventResponse)
      if (response.code === 200 && response.data) {
        const { token: newToken, user } = response.data
        
        // 保存token和用户信息
        token.value = newToken
        userInfo.value = user
        isLoggedIn.value = true
        
        // 保存到本地存储
        setToken(newToken)
        
        console.log('✅ 用户登录成功:', user.nickname)
        return response
      } else {
        throw new Error(response.message || '登录失败')
      }
    } catch (error) {
      console.error('❌ 用户登录失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 用户注册
   * @param {Object} registerData - 注册数据
   * @param {string} registerData.phone - 手机号
   * @param {string} registerData.password - 密码
   * @param {string} registerData.nickname - 昵称
   */
  const register = async (registerData) => {
    try {
      loading.value = true
      const response = await userApi.register(registerData)
      
      // 适配新的后端响应格式 (EventResponse)
      if (response.code === 200) {
        console.log('✅ 用户注册成功')
        return response
      } else {
        throw new Error(response.message || '注册失败')
      }
    } catch (error) {
      console.error('❌ 用户注册失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取用户信息
   */
  const fetchUserInfo = async () => {
    try {
      const response = await userApi.getUserInfo()
      
      // 适配新的后端响应格式 (EventResponse)
      if (response.code === 200 && response.data) {
        userInfo.value = response.data
        isLoggedIn.value = true
        
        console.log('✅ 获取用户信息成功:', userInfo.value.nickname)
        return response
      } else {
        throw new Error(response.message || '获取用户信息失败')
      }
    } catch (error) {
      console.error('❌ 获取用户信息失败:', error)
      throw error
    }
  }

  /**
   * 更新用户信息
   * @param {Object} updateData - 更新的用户信息
   */
  const updateUserInfo = async (userId, updateData) => {
    try {
      loading.value = true
      const response = await userApi.updateUserInfo(userId, updateData)
      
      // 适配新的后端响应格式 (EventResponse)
      if (response.code === 200 && response.data) {
        // 更新本地用户信息
        userInfo.value = { ...userInfo.value, ...response.data }
        
        console.log('✅ 更新用户信息成功')
        return response
      } else {
        throw new Error(response.message || '更新用户信息失败')
      }
    } catch (error) {
      console.error('❌ 更新用户信息失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 上传头像
   * @param {File} file - 头像文件
   */
  const uploadAvatar = async (file) => {
    try {
      loading.value = true
      const response = await userApi.uploadAvatar(file)
      
      // 适配新的后端响应格式 (EventResponse)
      if (response.code === 200 && response.data) {
        // 更新头像信息
        userInfo.value.avatar = response.data.avatar
        
        console.log('✅ 上传头像成功')
        return response
      } else {
        throw new Error(response.message || '上传头像失败')
      }
    } catch (error) {
      console.error('❌ 上传头像失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新头像URL
   * @param {string} avatarUrl - 头像URL
   */
  const updateAvatar = (avatarUrl) => {
    if (userInfo.value) {
      userInfo.value.avatar = avatarUrl
      console.log('✅ 更新头像URL成功:', avatarUrl)
    }
  }

  /**
   * 检查用户认证状态
   */
  const checkAuth = async () => {
    try {
      if (!token.value) {
        throw new Error('Token不存在')
      }
      
      await fetchUserInfo()
      return true
    } catch (error) {
      console.error('认证检查失败:', error)
      logout()
      throw error
    }
  }

  /**
   * 用户登出
   */
  const logout = () => {
    // 清除状态
    token.value = ''
    userInfo.value = null
    isLoggedIn.value = false
    
    // 清除本地存储
    removeToken()
    
    console.log('🔌 用户已登出')
  }

  /**
   * 更新用户活跃时间
   */
  const updateActiveTime = () => {
    if (userInfo.value) {
      userInfo.value.lastActiveTime = new Date().toISOString()
    }
  }

  /**
   * 刷新Token
   */
  const refreshToken = async () => {
    try {
      const response = await userApi.refreshToken()
      
      // 适配新的后端响应格式 (EventResponse)
      if (response.code === 200 && response.data) {
        const { token: newToken } = response.data
        
        token.value = newToken
        setToken(newToken)
        
        console.log('✅ Token刷新成功')
        return response
      } else {
        throw new Error(response.message || 'Token刷新失败')
      }
    } catch (error) {
      console.error('❌ Token刷新失败:', error)
      logout()
      throw error
    }
  }

  return {
    // 状态
    token,
    userInfo,
    isLoggedIn,
    loading,
    
    // 计算属性
    userId,
    nickname,
    avatar,
    phone,
    
    // 方法
    initUser,
    login,
    register,
    fetchUserInfo,
    updateUserInfo,
    uploadAvatar,
    updateAvatar,
    checkAuth,
    logout,
    updateActiveTime,
    refreshToken
  }
}) 