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
  const userStatistics = ref(null)
  const isLoggedIn = ref(false)
  const loading = ref(false)
  const error = ref(null)

  // 计算属性
  const userId = computed(() => userInfo.value?.userId || '')
  const nickname = computed(() => userInfo.value?.nickname || '')
  const avatar = computed(() => userInfo.value?.avatar || '')
  const phone = computed(() => userInfo.value?.phone || '')
  const isFirstLogin = computed(() => userInfo.value?.isFirstLogin || false)

  /**
   * 初始化用户状态
   */
  const initUser = async () => {
    try {
      const savedToken = getToken()
      if (savedToken) {
        token.value = savedToken
        // 尝试获取用户信息，但不抛出错误
        try {
          await fetchUserInfo()
          return true
        } catch (error) {
          console.warn('获取用户信息失败，可能是token过期:', error)
          // 不清除token，让用户手动处理
          return false
        }
      }
      return false
    } catch (error) {
      console.error('初始化用户状态失败:', error)
      return false
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
      error.value = null
      
      const response = await userApi.login(loginData)
      
      if (response.code === 200 && response.data) {
        const { userId, token: newToken, isFirstLogin, user } = response.data
        
        // 保存token和用户信息
        token.value = newToken
        userInfo.value = user || {
          userId,
          isFirstLogin
        }
        isLoggedIn.value = true
        
        // 保存到本地存储
        setToken(newToken)
        
        console.log('✅ 用户登录成功:', userInfo.value.nickname)
        return response
      } else {
        throw new Error(response.message || '登录失败')
      }
    } catch (err) {
      console.error('❌ 用户登录失败:', err)
      error.value = err.message
      throw err
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
      error.value = null
      
      const response = await userApi.register(registerData)
      
      if (response.code === 200 && response.data) {
        const { userId, nickname, token: newToken } = response.data
        
        // 保存token和用户信息
        token.value = newToken
        userInfo.value = {
          userId,
          nickname,
          phone: registerData.phone,
          isFirstLogin: true
        }
        isLoggedIn.value = true
        
        // 保存到本地存储
        setToken(newToken)
        
        console.log('✅ 用户注册成功:', nickname)
        return response
      } else {
        throw new Error(response.message || '注册失败')
      }
    } catch (err) {
      console.error('❌ 用户注册失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取用户信息
   */
  const fetchUserInfo = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await userApi.getUserInfo()
      
      if (response.code === 200 && response.data) {
        userInfo.value = response.data
        isLoggedIn.value = true
        
        console.log('✅ 获取用户信息成功:', userInfo.value.nickname)
        return response
      } else {
        throw new Error(response.message || '获取用户信息失败')
      }
    } catch (err) {
      console.error('❌ 获取用户信息失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取用户统计信息
   */
  const fetchUserStatistics = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await userApi.getUserStatistics()
      
      if (response.code === 200 && response.data) {
        userStatistics.value = response.data
        console.log('✅ 获取用户统计信息成功')
        return response
      } else {
        throw new Error(response.message || '获取用户统计信息失败')
      }
    } catch (err) {
      console.error('❌ 获取用户统计信息失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取当前用户个人统计信息
   */
  const fetchCurrentUserPersonalStatistics = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await userApi.getCurrentUserPersonalStatistics()
      
      if (response.code === 200 && response.data) {
        // 这里可以存储个人统计信息，如果需要的话
        console.log('✅ 获取当前用户个人统计信息成功')
        return response
      } else {
        throw new Error(response.message || '获取个人统计信息失败')
      }
    } catch (err) {
      console.error('❌ 获取个人统计信息失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新用户信息
   * @param {Object} updateData - 更新的用户信息
   */
  const updateUserInfo = async (userId, updateData) => {
    try {
      loading.value = true
      error.value = null
      
      const response = await userApi.updateUserInfo(userId, updateData)
      
      if (response.code === 200 && response.data) {
        // 更新本地用户信息
        userInfo.value = { ...userInfo.value, ...response.data }
        
        console.log('✅ 更新用户信息成功')
        return response
      } else {
        throw new Error(response.message || '更新用户信息失败')
      }
    } catch (err) {
      console.error('❌ 更新用户信息失败:', err)
      error.value = err.message
      throw err
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
      error.value = null
      
      const response = await userApi.uploadAvatar(file)
      
      if (response.code === 200 && response.data) {
        // 更新头像信息
        userInfo.value.avatar = response.data.avatar
        
        console.log('✅ 上传头像成功')
        return response
      } else {
        throw new Error(response.message || '上传头像失败')
      }
    } catch (err) {
      console.error('❌ 上传头像失败:', err)
      error.value = err.message
      throw err
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
    userStatistics.value = null
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

  /**
   * 完成首次登录
   */
  const completeFirstLogin = async (userId) => {
    try {
      loading.value = true
      error.value = null
      
      const response = await userApi.completeFirstLogin(userId)
      
      if (response.code === 200) {
        userInfo.value = { ...userInfo.value, isFirstLogin: false }
        console.log('✅ 完成首次登录')
        return response
      } else {
        throw new Error(response.message || '完成首次登录失败')
      }
    } catch (err) {
      console.error('❌ 完成首次登录失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 检查手机号是否存在
   */
  const checkPhone = async (phone) => {
    try {
      const response = await userApi.checkPhone(phone)
      return response.code === 200 ? response.data.exists : false
    } catch (err) {
      console.error('检查手机号失败:', err)
      return false
    }
  }

  /**
   * 检查昵称是否存在
   */
  const checkNickname = async (nickname) => {
    try {
      const response = await userApi.checkNickname(nickname)
      return response.code === 200 ? response.data.exists : false
    } catch (err) {
      console.error('检查昵称失败:', err)
      return false
    }
  }

  /**
   * 搜索用户
   */
  const searchUsers = async (nickname, limit = 10) => {
    try {
      const response = await userApi.searchUsers(nickname, limit)
      return response.code === 200 ? response.data : []
    } catch (err) {
      console.error('搜索用户失败:', err)
      return []
    }
  }

  /**
   * 获取最近注册的用户
   */
  const getRecentUsers = async (limit = 10) => {
    try {
      const response = await userApi.getRecentUsers(limit)
      return response.code === 200 ? response.data : []
    } catch (err) {
      console.error('获取最近用户失败:', err)
      return []
    }
  }

  return {
    // 状态
    token,
    userInfo,
    userStatistics,
    isLoggedIn,
    loading,
    error,
    
    // 计算属性
    userId,
    nickname,
    avatar,
    phone,
    isFirstLogin,
    
    // 方法
    initUser,
    login,
    register,
    fetchUserInfo,
    fetchUserStatistics,
    fetchCurrentUserPersonalStatistics,
    updateUserInfo,
    uploadAvatar,
    updateAvatar,
    checkAuth,
    logout,
    updateActiveTime,
    refreshToken,
    completeFirstLogin,
    checkPhone,
    checkNickname,
    searchUsers,
    getRecentUsers
  }
}) 