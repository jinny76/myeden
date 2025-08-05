/**
 * 时间处理工具函数
 * 
 * 功能说明：
 * - 提供智能的时间格式化显示
 * - 支持相对时间显示（如"刚刚"、"5分钟前"）
 * - 处理时区问题
 * - 统一时间处理逻辑
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-07-16
 */

/**
 * 格式化时间为相对时间显示
 * 
 * 功能特点：
 * - 智能显示相对时间（刚刚、分钟前、小时前等）
 * - 处理时区问题，支持服务器GMT+9时区
 * - 支持多种时间格式输入
 * - 超过一个月显示具体日期
 * 
 * @param {string|number|Date} time - 要格式化的时间
 * @returns {string} 格式化后的时间字符串
 * 
 * @example
 * formatTime('2025-07-20T22:55:58.201') // "刚刚"
 * formatTime('2025-07-20T20:55:58.201') // "2小时前"
 * formatTime('2025-07-19T22:55:58.201') // "1天前"
 * formatTime('2025-06-20T22:55:58.201') // "7-20" (同年)
 * formatTime('2024-07-20T22:55:58.201') // "2024-7-20" (不同年)
 */
export const formatTime = (time) => {
  if (!time) return ''
  
  // 缓存时间常量，提高性能
  const MINUTE = 60 * 1000
  const HOUR = 60 * MINUTE
  const DAY = 24 * HOUR
  const WEEK = 7 * DAY
  const MONTH = 30 * DAY
  
  // 优化时间解析，处理不同格式
  let date
  if (typeof time === 'string') {
    // 检测时间格式并处理时区问题
    if (time.includes('T') && !time.includes('Z') && !time.includes('+')) {
      // 格式："2025-07-20T22:55:58.201" (无时区信息)
      // 服务器在GMT+9时区，添加+09:00后缀
      date = new Date(time + '+09:00')
    } else {
      date = new Date(time)
    }
  } else {
    date = new Date(time)
  }
  
  // 验证日期有效性
  if (isNaN(date.getTime())) {
    return '时间格式错误'
  }
  
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  // 处理未来时间
  if (diff < 0) {
    return '即将发布'
  }
  
  // 优化时间判断逻辑，按频率排序
  if (diff < MINUTE) {
    return '刚刚'
  }
  
  if (diff < HOUR) {
    const minutes = Math.floor(diff / MINUTE)
    return `${minutes}分钟前`
  }
  
  if (diff < DAY) {
    const hours = Math.floor(diff / HOUR)
    return `${hours}小时前`
  }
  
  if (diff < WEEK) {
    const days = Math.floor(diff / DAY)
    return `${days}天前`
  }
  
  if (diff < MONTH) {
    const weeks = Math.floor(diff / WEEK)
    return `${weeks}周前`
  }
  
  // 超过一个月显示具体日期，格式更友好
  const year = date.getFullYear()
  const currentYear = now.getFullYear()
  
  if (year === currentYear) {
    // 同年只显示月日
    return date.toLocaleDateString('zh-CN', { 
      month: 'numeric', 
      day: 'numeric' 
    })
  } else {
    // 不同年显示年月日
    return date.toLocaleDateString('zh-CN', { 
      year: 'numeric', 
      month: 'numeric', 
      day: 'numeric' 
    })
  }
}

/**
 * 格式化时间为完整日期时间显示
 * 
 * @param {string|number|Date} time - 要格式化的时间
 * @param {boolean} showTime - 是否显示时间部分
 * @returns {string} 格式化后的日期时间字符串
 */
export const formatDateTime = (time, showTime = true) => {
  if (!time) return ''
  
  let date
  if (typeof time === 'string') {
    if (time.includes('T') && !time.includes('Z') && !time.includes('+')) {
      date = new Date(time + '+09:00')
    } else {
      date = new Date(time)
    }
  } else {
    date = new Date(time)
  }
  
  if (isNaN(date.getTime())) {
    return '时间格式错误'
  }
  
  if (showTime) {
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } else {
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  }
}

/**
 * 获取时间戳
 * 
 * @param {string|number|Date} time - 时间
 * @returns {number} 时间戳
 */
export const getTimestamp = (time) => {
  if (!time) return 0
  
  let date
  if (typeof time === 'string') {
    if (time.includes('T') && !time.includes('Z') && !time.includes('+')) {
      date = new Date(time + '+09:00')
    } else {
      date = new Date(time)
    }
  } else {
    date = new Date(time)
  }
  
  return date.getTime()
}

/**
 * 检查是否为今天
 * 
 * @param {string|number|Date} time - 时间
 * @returns {boolean} 是否为今天
 */
export const isToday = (time) => {
  if (!time) return false
  
  const date = new Date(time)
  const today = new Date()
  
  return date.toDateString() === today.toDateString()
}

/**
 * 检查是否为昨天
 * 
 * @param {string|number|Date} time - 时间
 * @returns {boolean} 是否为昨天
 */
export const isYesterday = (time) => {
  if (!time) return false
  
  const date = new Date(time)
  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)
  
  return date.toDateString() === yesterday.toDateString()
} 