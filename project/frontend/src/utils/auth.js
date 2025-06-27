import Cookies from 'js-cookie'

/**
 * 认证工具函数
 * 
 * 功能说明：
 * - Token的存储和获取
 * - Token的删除
 * - Token的有效性检查
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

// Token存储的key
const TOKEN_KEY = 'myeden_token'
const REFRESH_TOKEN_KEY = 'myeden_refresh_token'

// Token过期时间（天）
const TOKEN_EXPIRE_DAYS = 1
const REFRESH_TOKEN_EXPIRE_DAYS = 7

/**
 * 设置Token
 * @param {string} token - JWT Token
 */
export const setToken = (token) => {
  if (token) {
    Cookies.set(TOKEN_KEY, token, { 
      expires: TOKEN_EXPIRE_DAYS,
      secure: import.meta.env.PROD,
      sameSite: 'strict'
    })
  }
}

/**
 * 获取Token
 * @returns {string|null} Token字符串或null
 */
export const getToken = () => {
  return Cookies.get(TOKEN_KEY) || null
}

/**
 * 删除Token
 */
export const removeToken = () => {
  Cookies.remove(TOKEN_KEY)
  Cookies.remove(REFRESH_TOKEN_KEY)
}

/**
 * 设置刷新Token
 * @param {string} refreshToken - 刷新Token
 */
export const setRefreshToken = (refreshToken) => {
  if (refreshToken) {
    Cookies.set(REFRESH_TOKEN_KEY, refreshToken, { 
      expires: REFRESH_TOKEN_EXPIRE_DAYS,
      secure: import.meta.env.PROD,
      sameSite: 'strict'
    })
  }
}

/**
 * 获取刷新Token
 * @returns {string|null} 刷新Token字符串或null
 */
export const getRefreshToken = () => {
  return Cookies.get(REFRESH_TOKEN_KEY) || null
}

/**
 * 检查Token是否存在
 * @returns {boolean} 是否存在Token
 */
export const hasToken = () => {
  return !!getToken()
}

/**
 * 检查Token是否即将过期
 * @param {number} thresholdMinutes - 过期阈值（分钟），默认30分钟
 * @returns {boolean} 是否即将过期
 */
export const isTokenExpiringSoon = (thresholdMinutes = 30) => {
  const token = getToken()
  if (!token) {
    return true
  }
  
  try {
    // 解析JWT Token（不验证签名）
    const payload = JSON.parse(atob(token.split('.')[1]))
    const exp = payload.exp * 1000 // 转换为毫秒
    const now = Date.now()
    const threshold = thresholdMinutes * 60 * 1000 // 转换为毫秒
    
    return (exp - now) < threshold
  } catch (error) {
    console.error('解析Token失败:', error)
    return true
  }
}

/**
 * 获取Token过期时间
 * @returns {Date|null} 过期时间或null
 */
export const getTokenExpiration = () => {
  const token = getToken()
  if (!token) {
    return null
  }
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return new Date(payload.exp * 1000)
  } catch (error) {
    console.error('解析Token失败:', error)
    return null
  }
}

/**
 * 获取Token中的用户信息
 * @returns {Object|null} 用户信息或null
 */
export const getTokenUserInfo = () => {
  const token = getToken()
  if (!token) {
    return null
  }
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return {
      id: payload.sub,
      phone: payload.phone,
      nickname: payload.nickname,
      role: payload.role
    }
  } catch (error) {
    console.error('解析Token失败:', error)
    return null
  }
}

/**
 * 清除所有认证相关的数据
 */
export const clearAuthData = () => {
  removeToken()
  
  // 清除localStorage中的用户相关数据
  localStorage.removeItem('userInfo')
  localStorage.removeItem('userSettings')
  
  // 清除sessionStorage中的临时数据
  sessionStorage.clear()
}

/**
 * 设置用户信息到本地存储
 * @param {Object} userInfo - 用户信息
 */
export const setUserInfo = (userInfo) => {
  if (userInfo) {
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
  }
}

/**
 * 获取本地存储的用户信息
 * @returns {Object|null} 用户信息或null
 */
export const getUserInfo = () => {
  try {
    const userInfo = localStorage.getItem('userInfo')
    return userInfo ? JSON.parse(userInfo) : null
  } catch (error) {
    console.error('解析用户信息失败:', error)
    return null
  }
}

/**
 * 清除本地存储的用户信息
 */
export const removeUserInfo = () => {
  localStorage.removeItem('userInfo')
} 