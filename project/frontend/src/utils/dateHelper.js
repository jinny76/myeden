/**
 * 日期处理工具函数
 * 
 * 功能说明：
 * - 提供时区安全的日期格式化
 * - 解决 toISOString 时区问题
 * - 统一日期处理逻辑
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-07-16
 */

/**
 * 格式化日期为 YYYY-MM-DD 格式（时区安全）
 * @param {Date} date - 要格式化的日期
 * @returns {string} YYYY-MM-DD 格式的日期字符串
 */
export const formatDateToYMD = (date) => {
  if (!date || !(date instanceof Date)) {
    return ''
  }
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  
  return `${year}-${month}-${day}`
}

/**
 * 格式化日期为本地化显示格式
 * @param {Date} date - 要格式化的日期
 * @returns {string} 本地化日期字符串
 */
export const formatDateToLocal = (date) => {
  if (!date || !(date instanceof Date)) {
    return ''
  }
  
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

/**
 * 解析日期字符串为 Date 对象
 * @param {string} dateString - YYYY-MM-DD 格式的日期字符串
 * @returns {Date} Date 对象
 */
export const parseDateFromYMD = (dateString) => {
  if (!dateString || typeof dateString !== 'string') {
    return new Date()
  }
  
  const [year, month, day] = dateString.split('-').map(Number)
  return new Date(year, month - 1, day)
}

/**
 * 获取过去指定天数的日期
 * @param {number} days - 天数
 * @returns {Date} 过去的日期
 */
export const getDateBeforeDays = (days) => {
  const date = new Date()
  date.setDate(date.getDate() - days)
  return date
}

/**
 * 获取过去一年的日期范围
 * @returns {Object} 包含 startDate 和 endDate 的对象
 */
export const getYearRange = () => {
  const endDate = new Date()
  const startDate = getDateBeforeDays(364) // 过去一年
  
  return {
    startDate: formatDateToYMD(startDate),
    endDate: formatDateToYMD(endDate)
  }
}

/**
 * 检查日期是否为今天
 * @param {Date} date - 要检查的日期
 * @returns {boolean} 是否为今天
 */
export const isToday = (date) => {
  if (!date || !(date instanceof Date)) {
    return false
  }
  
  const today = new Date()
  return formatDateToYMD(date) === formatDateToYMD(today)
}

/**
 * 检查日期是否在指定范围内
 * @param {Date} date - 要检查的日期
 * @param {Date} startDate - 开始日期
 * @param {Date} endDate - 结束日期
 * @returns {boolean} 是否在范围内
 */
export const isDateInRange = (date, startDate, endDate) => {
  if (!date || !startDate || !endDate) {
    return false
  }
  
  const checkDate = new Date(date)
  const start = new Date(startDate)
  const end = new Date(endDate)
  
  // 只比较日期部分，不比较时间
  checkDate.setHours(0, 0, 0, 0)
  start.setHours(0, 0, 0, 0)
  end.setHours(0, 0, 0, 0)
  
  return checkDate >= start && checkDate <= end
}

/**
 * 获取两个日期之间的天数差
 * @param {Date} date1 - 第一个日期
 * @param {Date} date2 - 第二个日期
 * @returns {number} 天数差
 */
export const getDaysDifference = (date1, date2) => {
  if (!date1 || !date2) {
    return 0
  }
  
  const oneDay = 24 * 60 * 60 * 1000
  const firstDate = new Date(date1)
  const secondDate = new Date(date2)
  
  // 只比较日期部分
  firstDate.setHours(0, 0, 0, 0)
  secondDate.setHours(0, 0, 0, 0)
  
  return Math.round((secondDate - firstDate) / oneDay)
}

/**
 * 获取一周的开始日期（星期一）
 * @param {Date} date - 基准日期
 * @returns {Date} 这一周的星期一
 */
export const getWeekStart = (date) => {
  if (!date || !(date instanceof Date)) {
    return new Date()
  }
  
  const result = new Date(date)
  const dayOfWeek = result.getDay()
  const daysToMonday = dayOfWeek === 0 ? 6 : dayOfWeek - 1
  
  result.setDate(result.getDate() - daysToMonday)
  return result
}

/**
 * 生成日期范围内的所有日期
 * @param {Date} startDate - 开始日期
 * @param {Date} endDate - 结束日期
 * @returns {Array<Date>} 日期数组
 */
export const generateDateRange = (startDate, endDate) => {
  if (!startDate || !endDate) {
    return []
  }
  
  const dates = []
  const current = new Date(startDate)
  const end = new Date(endDate)
  
  while (current <= end) {
    dates.push(new Date(current))
    current.setDate(current.getDate() + 1)
  }
  
  return dates
}