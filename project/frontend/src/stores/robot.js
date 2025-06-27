import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { 
  getRobotList, 
  getRobotDetail, 
  triggerRobotPost, 
  triggerRobotComment, 
  triggerRobotReply,
  getRobotDailyStats,
  resetRobotDailyStats,
  startBehaviorScheduler,
  stopBehaviorScheduler
} from '@/api/robot'

/**
 * 机器人状态管理
 * 管理AI机器人的状态、行为和统计信息
 */
export const useRobotStore = defineStore('robot', () => {
  // 状态
  const robots = ref([])
  const robotDetails = ref({})
  const robotStats = ref({})
  const loading = ref(false)
  const error = ref(null)
  const schedulerStatus = ref('stopped') // stopped, running

  // 计算属性
  const activeRobots = computed(() => {
    return robots.value.filter(robot => robot.isActive)
  })

  const robotCount = computed(() => {
    return robots.value.length
  })

  const activeRobotCount = computed(() => {
    return activeRobots.value.length
  })

  // 获取机器人列表
  const fetchRobotList = async (isActive = null) => {
    try {
      loading.value = true
      error.value = null
      const response = await getRobotList(isActive)
      if (response.code === 200) {
        robots.value = response.data || []
      } else {
        error.value = response.message || '获取机器人列表失败'
      }
    } catch (err) {
      error.value = err.message || '获取机器人列表失败'
      console.error('获取机器人列表失败:', err)
    } finally {
      loading.value = false
    }
  }

  // 获取机器人详情
  const fetchRobotDetail = async (robotId) => {
    try {
      loading.value = true
      error.value = null
      const response = await getRobotDetail(robotId)
      if (response.code === 200) {
        robotDetails.value[robotId] = response.data
        return response.data
      } else {
        error.value = response.message || '获取机器人详情失败'
        return null
      }
    } catch (err) {
      error.value = err.message || '获取机器人详情失败'
      console.error('获取机器人详情失败:', err)
      return null
    } finally {
      loading.value = false
    }
  }

  // 触发机器人发布动态
  const triggerPost = async (robotId) => {
    try {
      loading.value = true
      error.value = null
      const response = await triggerRobotPost(robotId)
      if (response.code === 200) {
        return true
      } else {
        error.value = response.message || '触发机器人发布动态失败'
        return false
      }
    } catch (err) {
      error.value = err.message || '触发机器人发布动态失败'
      console.error('触发机器人发布动态失败:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  // 触发机器人发表评论
  const triggerComment = async (robotId, postId) => {
    try {
      loading.value = true
      error.value = null
      const response = await triggerRobotComment(robotId, postId)
      if (response.code === 200) {
        return true
      } else {
        error.value = response.message || '触发机器人发表评论失败'
        return false
      }
    } catch (err) {
      error.value = err.message || '触发机器人发表评论失败'
      console.error('触发机器人发表评论失败:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  // 触发机器人回复评论
  const triggerReply = async (robotId, commentId) => {
    try {
      loading.value = true
      error.value = null
      const response = await triggerRobotReply(robotId, commentId)
      if (response.code === 200) {
        return true
      } else {
        error.value = response.message || '触发机器人回复评论失败'
        return false
      }
    } catch (err) {
      error.value = err.message || '触发机器人回复评论失败'
      console.error('触发机器人回复评论失败:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  // 获取机器人今日行为统计
  const fetchRobotStats = async (robotId) => {
    try {
      loading.value = true
      error.value = null
      const response = await getRobotDailyStats(robotId)
      if (response.code === 200) {
        robotStats.value[robotId] = response.data
        return response.data
      } else {
        error.value = response.message || '获取机器人统计失败'
        return null
      }
    } catch (err) {
      error.value = err.message || '获取机器人统计失败'
      console.error('获取机器人统计失败:', err)
      return null
    } finally {
      loading.value = false
    }
  }

  // 重置机器人行为统计
  const resetStats = async (robotId) => {
    try {
      loading.value = true
      error.value = null
      const response = await resetRobotDailyStats(robotId)
      if (response.code === 200) {
        await fetchRobotStats(robotId)
        return true
      } else {
        error.value = response.message || '重置机器人统计失败'
        return false
      }
    } catch (err) {
      error.value = err.message || '重置机器人统计失败'
      console.error('重置机器人统计失败:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  // 启动机器人行为调度
  const startScheduler = async () => {
    try {
      loading.value = true
      error.value = null
      const response = await startBehaviorScheduler()
      if (response.code === 200) {
        schedulerStatus.value = 'running'
        return true
      } else {
        error.value = response.message || '启动机器人行为调度失败'
        return false
      }
    } catch (err) {
      error.value = err.message || '启动机器人行为调度失败'
      console.error('启动机器人行为调度失败:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  // 停止机器人行为调度
  const stopScheduler = async () => {
    try {
      loading.value = true
      error.value = null
      const response = await stopBehaviorScheduler()
      if (response.code === 200) {
        schedulerStatus.value = 'stopped'
        return true
      } else {
        error.value = response.message || '停止机器人行为调度失败'
        return false
      }
    } catch (err) {
      error.value = err.message || '停止机器人行为调度失败'
      console.error('停止机器人行为调度失败:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  // 清除错误
  const clearError = () => {
    error.value = null
  }

  // 重置状态
  const reset = () => {
    robots.value = []
    robotDetails.value = {}
    robotStats.value = {}
    loading.value = false
    error.value = null
    schedulerStatus.value = 'stopped'
  }

  return {
    // 状态
    robots,
    robotDetails,
    robotStats,
    loading,
    error,
    schedulerStatus,
    
    // 计算属性
    activeRobots,
    robotCount,
    activeRobotCount,
    
    // 方法
    fetchRobotList,
    fetchRobotDetail,
    triggerPost,
    triggerComment,
    triggerReply,
    fetchRobotStats,
    resetStats,
    startScheduler,
    stopScheduler,
    clearError,
    reset
  }
}) 