/**
 * 头像处理工具函数
 * 
 * 功能说明：
 * - 处理用户头像URL的构建和转换
 * - 提供默认头像生成功能
 * - 统一头像显示逻辑
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

/**
 * 构建完整的头像URL
 * @param {string} avatarUrl - 原始头像URL
 * @returns {string} 完整的头像URL
 */
export const buildAvatarUrl = (avatarUrl) => {
  if (!avatarUrl) {
    return null
  }
  
  // 如果已经是完整URL，直接返回
  if (avatarUrl.startsWith('http')) {
    return avatarUrl
  }
  
  // 如果是相对路径，转换为API路径
  if (avatarUrl.includes('/uploads/')) {
    const apiAvatarUrl = avatarUrl.replace('/uploads/', '/api/v1/files/')
    return `${window.location.origin}${apiAvatarUrl}`
  }

  if (avatarUrl.includes('/avatars/')) {
    const apiAvatarUrl = avatarUrl.replace('/avatars/', '/api/v1/files/avatars/')
    return `${window.location.origin}${apiAvatarUrl}`
  }
  
  // 如果是其他相对路径，添加域名
  if (avatarUrl.startsWith('/')) {
    return `${window.location.origin}${avatarUrl}`
  }
  
  return avatarUrl
}

/**
 * 生成默认头像URL
 * @param {string} nickname - 用户昵称
 * @param {string} userId - 用户ID（可选）
 * @returns {string} 默认头像URL
 */
export const generateDefaultAvatar = (nickname, userId = null) => {
  const name = nickname || 'User'
  
  // 使用UI Avatars服务生成基于昵称的头像
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=random&color=fff&size=200`
}

/**
 * 获取用户头像URL（包含默认头像处理）
 * @param {Object} userInfo - 用户信息对象
 * @returns {string} 头像URL
 */
export const getUserAvatarUrl = (userInfo) => {
  if (!userInfo) {
    return generateDefaultAvatar('User')
  }
  
  // 如果用户有头像，构建完整URL
  if (userInfo.avatar) {
    const fullAvatarUrl = buildAvatarUrl(userInfo.avatar)
    if (fullAvatarUrl) {
      return fullAvatarUrl
    }
  }
  
  // 如果没有头像，生成默认头像
  return generateDefaultAvatar(userInfo.nickname, userInfo.userId)
}

/**
 * 生成机器人默认头像URL
 * @param {string} robotName - 机器人名称
 * @param {string} robotId - 机器人ID（可选）
 * @returns {string} 默认机器人头像URL
 */
export const generateRobotDefaultAvatar = (robotName, robotId = null) => {
  const name = robotName || 'Robot'
  
  // 使用UI Avatars服务生成基于机器人名称的头像
  // 为机器人使用不同的背景色和样式
  const colors = ['ff6b6b', '4ecdc4', '45b7d1', '96ceb4', 'feca57', 'ff9ff3', '54a0ff', '5f27cd']
  const randomColor = colors[Math.floor(Math.random() * colors.length)]
  
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=${randomColor}&color=fff&size=200&bold=true`
}

/**
 * 获取机器人头像URL（包含默认头像处理）
 * @param {Object} robotInfo - 机器人信息对象
 * @returns {string} 头像URL
 */
export const getRobotAvatarUrl = (robotInfo) => {
  if (!robotInfo) {
    return generateRobotDefaultAvatar('Robot')
  }
  
  // 如果机器人有头像，构建完整URL
  if (robotInfo.avatar) {
    const fullAvatarUrl = buildAvatarUrl(robotInfo.avatar)
    if (fullAvatarUrl) {
      return fullAvatarUrl
    }
  }
  
  // 如果没有头像，生成默认头像
  return generateRobotDefaultAvatar(robotInfo.name, robotInfo.id)
}

/**
 * 处理头像加载错误
 * @param {Event} event - 错误事件
 * @param {string} nickname - 用户昵称
 * @returns {string} 默认头像URL
 */
export const handleAvatarError = (event, nickname) => {
  console.warn('头像加载失败，使用默认头像:', event.target.src)
  const defaultAvatar = generateDefaultAvatar(nickname)
  event.target.src = defaultAvatar
  return defaultAvatar
}

/**
 * 处理机器人头像加载错误
 * @param {Event} event - 错误事件
 * @param {string} robotName - 机器人名称
 * @returns {string} 默认头像URL
 */
export const handleRobotAvatarError = (event, robotName) => {
  console.warn('机器人头像加载失败，使用默认头像:', event.target.src)
  const defaultAvatar = generateRobotDefaultAvatar(robotName)
  event.target.src = defaultAvatar
  return defaultAvatar
}

/**
 * 测试头像URL构建
 * @param {string} originalUrl - 原始URL
 * @returns {string} 构建后的URL
 */
export const testAvatarUrlBuild = (originalUrl) => {
  console.log('=== 头像URL构建测试 ===')
  console.log('原始URL:', originalUrl)
  
  const result = buildAvatarUrl(originalUrl)
  console.log('构建结果:', result)
  console.log('=== 测试结束 ===')
  
  return result
} 