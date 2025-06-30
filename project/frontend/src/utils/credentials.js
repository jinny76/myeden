/**
 * 凭据管理工具
 * 用于保存和获取用户登录凭据，实现"记住我"功能
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-12-19
 */

const CREDENTIALS_KEY = 'myeden_credentials'

/**
 * 保存用户凭据到本地存储
 * @param {Object} credentials - 用户凭据
 * @param {string} credentials.phone - 手机号
 * @param {string} credentials.password - 密码
 */
export const saveCredentials = (credentials) => {
  try {
    if (credentials && credentials.phone && credentials.password) {
      // 简单加密：Base64编码（实际项目中应使用更安全的加密方式）
      const encodedCredentials = btoa(JSON.stringify(credentials))
      localStorage.setItem(CREDENTIALS_KEY, encodedCredentials)
      console.log('✅ 用户凭据已保存')
    }
  } catch (error) {
    console.error('保存凭据失败:', error)
  }
}

/**
 * 从本地存储获取用户凭据
 * @returns {Object|null} 用户凭据对象或null
 */
export const getCredentials = () => {
  try {
    const encodedCredentials = localStorage.getItem(CREDENTIALS_KEY)
    if (encodedCredentials) {
      const credentials = JSON.parse(atob(encodedCredentials))
      console.log('✅ 获取到保存的凭据')
      return credentials
    }
    return null
  } catch (error) {
    console.error('获取凭据失败:', error)
    // 如果解析失败，清除可能损坏的数据
    clearCredentials()
    return null
  }
}

/**
 * 清除本地存储的用户凭据
 */
export const clearCredentials = () => {
  try {
    localStorage.removeItem(CREDENTIALS_KEY)
    console.log('✅ 用户凭据已清除')
  } catch (error) {
    console.error('清除凭据失败:', error)
  }
}

/**
 * 检查是否有保存的凭据
 * @returns {boolean} 是否有保存的凭据
 */
export const hasCredentials = () => {
  return getCredentials() !== null
}

/**
 * 自动登录功能
 * 使用保存的凭据进行自动登录
 * @param {Function} loginFunction - 登录函数
 * @returns {Promise<boolean>} 是否登录成功
 */
export const autoLogin = async (loginFunction) => {
  try {
    const credentials = getCredentials()
    if (!credentials) {
      console.log('没有保存的凭据，跳过自动登录')
      return false
    }

    console.log('🔄 尝试自动登录...')
    const response = await loginFunction(credentials)
    
    if (response && response.code === 200) {
      console.log('✅ 自动登录成功')
      return true
    } else {
      console.log('❌ 自动登录失败，清除凭据')
      clearCredentials()
      return false
    }
  } catch (error) {
    console.error('自动登录失败:', error)
    // 登录失败时清除凭据
    clearCredentials()
    return false
  }
}

/**
 * 测试凭据管理功能
 * 仅在开发环境使用
 */
export const testCredentials = () => {
  if (import.meta.env.DEV) {
    console.log('🧪 测试凭据管理功能...')
    
    // 测试保存凭据
    const testCredentials = {
      phone: '13800138000',
      password: 'test123456'
    }
    saveCredentials(testCredentials)
    
    // 测试获取凭据
    const retrieved = getCredentials()
    console.log('获取的凭据:', retrieved)
    
    // 测试检查凭据
    const hasCreds = hasCredentials()
    console.log('是否有凭据:', hasCreds)
    
    // 测试清除凭据
    clearCredentials()
    const afterClear = hasCredentials()
    console.log('清除后是否有凭据:', afterClear)
    
    console.log('✅ 凭据管理功能测试完成')
  }
} 