// 消息队列，避免同时显示多个消息
let messageQueue = []
let isShowingMessage = false

// 消息类型配置
const messageConfig = {
  success: {
    duration: 2000,
    type: 'success'
  },
  error: {
    duration: 3000,
    type: 'error'
  },
  warning: {
    duration: 2500,
    type: 'warning'
  },
  info: {
    duration: 2000,
    type: 'info'
  }
}

// 静默错误列表 - 这些错误不会显示给用户
const silentErrors = [
  '网络连接失败',
  '请求超时',
  '服务器内部错误',
  '加载失败',
  '操作失败'
]

// 轻量级提示列表 - 这些消息使用更短的显示时间
const lightMessages = [
  '保存成功',
  '操作成功',
  '更新成功',
  '删除成功',
  '发布成功',
  '评论发表成功',
  '回复发表成功',
  '头像上传成功',
  '个人资料保存成功',
  '登录成功',
  '注册成功',
  '退出登录成功',
  '重新加载成功'
]

// 判断是否为静默错误
const isSilentError = (msg) => {
  return silentErrors.some(error => msg.includes(error))
}

// 判断是否为轻量级消息
const isLightMessage = (msg) => {
  return lightMessages.some(light => msg.includes(light))
}

// 获取全局toast方法
const getToast = () => {
  if (typeof window !== 'undefined' && window.$toast) {
    return window.$toast
  } else {
    // 兜底：如未挂载，直接console
    return {
      success: (title, message, duration) => console.log('[Toast:success]', title, message),
      error: (title, message, duration) => console.log('[Toast:error]', title, message),
      warning: (title, message, duration) => console.log('[Toast:warning]', title, message),
      info: (title, message, duration) => console.log('[Toast:info]', title, message)
    }
  }
}

// 显示消息的核心函数
const showMessage = (type, msg, customConfig = {}) => {
  // 静默错误处理
  if (type === 'error' && isSilentError(msg)) {
    console.error(`[静默错误] ${msg}`)
    return
  }

  // 轻量级消息处理
  let duration = messageConfig[type]?.duration || 2000
  if (isLightMessage(msg)) {
    duration = 1500
  }
  if (customConfig.duration) {
    duration = customConfig.duration
  }

  // 标题与内容区分（如有需要可扩展）
  const title = msg
  const message = customConfig.detail || ''

  // 直接调用全局Toast
  getToast()[type](title, message, duration)
}

// 静默错误处理 - 只在控制台输出，不显示给用户
const silentError = (msg, error = null) => {
  console.error(`[静默错误] ${msg}`, error)
}

// 轻量级成功提示 - 更短的显示时间
const lightSuccess = (msg) => {
  showMessage('success', msg, { duration: 1500 })
}

// 轻量级信息提示 - 更短的显示时间
const lightInfo = (msg) => {
  showMessage('info', msg, { duration: 1500 })
}

// 智能错误处理 - 根据错误类型决定是否显示
const smartError = (msg, error = null) => {
  if (isSilentError(msg)) {
    silentError(msg, error)
  } else {
    showMessage('error', msg)
  }
}

// 导出工具函数
export const message = {
  // 标准消息提示
  success: (msg, config) => showMessage('success', msg, config),
  error: (msg, config) => showMessage('error', msg, config),
  warning: (msg, config) => showMessage('warning', msg, config),
  info: (msg, config) => showMessage('info', msg, config),

  // 轻量级提示
  lightSuccess,
  lightInfo,

  // 静默错误
  silentError,

  // 智能错误处理
  smartError,

  // 清空消息队列（ToastMessage.vue有clear方法）
  clear: () => {
    if (getToast().clear) getToast().clear()
  }
}

// 默认导出
export default message 