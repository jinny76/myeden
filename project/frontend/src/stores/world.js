import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { worldApi } from '@/api/world'

/**
 * 世界状态管理
 * 
 * 功能说明：
 * - 管理世界信息和机器人信息的状态
 * - 提供世界背景、环境、活动等数据
 * - 支持配置热更新和状态同步
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
export const useWorldStore = defineStore('world', () => {
  // 状态定义
  const loading = ref(false)
  const worldInfo = ref(null)
  const worldBackground = ref(null)
  const worldEnvironment = ref(null)
  const worldActivities = ref(null)
  const worldStatistics = ref(null)
  const worldSettings = ref(null)
  const robotList = ref([])
  const robotDetails = ref({})
  const configStatus = ref(null)
  const error = ref(null)

  // 计算属性
  const isWorldLoaded = computed(() => worldInfo.value !== null)
  const isBackgroundLoaded = computed(() => worldBackground.value !== null)
  const isEnvironmentLoaded = computed(() => worldEnvironment.value !== null)
  const isActivitiesLoaded = computed(() => worldActivities.value !== null)
  const isStatisticsLoaded = computed(() => worldStatistics.value !== null)
  const isSettingsLoaded = computed(() => worldSettings.value !== null)
  const isRobotListLoaded = computed(() => robotList.value.length > 0)
  const activeRobots = computed(() => robotList.value.filter(robot => robot.isActive))
  const totalRobots = computed(() => robotList.value.length)

  /**
   * 获取世界基本信息
   */
  const fetchWorldInfo = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await worldApi.getWorldInfo()
      
      if (response.code === 200 && response.data) {
        worldInfo.value = response.data
        console.log('✅ 获取世界信息成功:', worldInfo.value.name)
        return response
      } else {
        throw new Error(response.message || '获取世界信息失败')
      }
    } catch (err) {
      console.error('❌ 获取世界信息失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取世界背景信息
   */
  const fetchWorldBackground = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await worldApi.getWorldBackground()
      
      if (response.code === 200 && response.data) {
        worldBackground.value = response.data
        console.log('✅ 获取世界背景成功')
        return response
      } else {
        throw new Error(response.message || '获取世界背景失败')
      }
    } catch (err) {
      console.error('❌ 获取世界背景失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取世界环境信息
   */
  const fetchWorldEnvironment = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await worldApi.getWorldEnvironment()
      
      if (response.code === 200 && response.data) {
        worldEnvironment.value = response.data
        console.log('✅ 获取世界环境成功')
        return response
      } else {
        throw new Error(response.message || '获取世界环境失败')
      }
    } catch (err) {
      console.error('❌ 获取世界环境失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取世界活动信息
   */
  const fetchWorldActivities = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await worldApi.getWorldActivities()
      
      if (response.code === 200 && response.data) {
        worldActivities.value = response.data
        console.log('✅ 获取世界活动成功')
        return response
      } else {
        throw new Error(response.message || '获取世界活动失败')
      }
    } catch (err) {
      console.error('❌ 获取世界活动失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取世界统计信息
   */
  const fetchWorldStatistics = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await worldApi.getWorldStatistics()
      
      if (response.code === 200 && response.data) {
        worldStatistics.value = response.data
        console.log('✅ 获取世界统计成功')
        return response
      } else {
        throw new Error(response.message || '获取世界统计失败')
      }
    } catch (err) {
      console.error('❌ 获取世界统计失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取世界设置信息
   */
  const fetchWorldSettings = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await worldApi.getWorldSettings()
      
      if (response.code === 200 && response.data) {
        worldSettings.value = response.data
        console.log('✅ 获取世界设置成功')
        return response
      } else {
        throw new Error(response.message || '获取世界设置失败')
      }
    } catch (err) {
      console.error('❌ 获取世界设置失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取机器人列表
   */
  const fetchRobotList = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await worldApi.getRobotList()
      
      if (response.code === 200 && response.data) {
        robotList.value = response.data
        console.log('✅ 获取机器人列表成功，数量:', robotList.value.length)
        return response
      } else {
        throw new Error(response.message || '获取机器人列表失败')
      }
    } catch (err) {
      console.error('❌ 获取机器人列表失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取机器人详情
   */
  const fetchRobotDetail = async (robotId) => {
    try {
      loading.value = true
      error.value = null
      
      const response = await worldApi.getRobotDetail(robotId)
      
      if (response.code === 200 && response.data) {
        robotDetails.value[robotId] = response.data
        console.log('✅ 获取机器人详情成功:', response.data.name)
        return response
      } else {
        throw new Error(response.message || '获取机器人详情失败')
      }
    } catch (err) {
      console.error('❌ 获取机器人详情失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 重新加载世界配置
   */
  const reloadWorldConfig = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await worldApi.reloadWorldConfig()
      
      if (response.code === 200) {
        console.log('✅ 重新加载世界配置成功')
        // 重新获取世界信息
        await fetchWorldInfo()
        return response
      } else {
        throw new Error(response.message || '重新加载世界配置失败')
      }
    } catch (err) {
      console.error('❌ 重新加载世界配置失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 重新加载机器人配置
   */
  const reloadRobotConfig = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await worldApi.reloadRobotConfig()
      
      if (response.code === 200) {
        console.log('✅ 重新加载机器人配置成功')
        // 重新获取机器人列表
        await fetchRobotList()
        return response
      } else {
        throw new Error(response.message || '重新加载机器人配置失败')
      }
    } catch (err) {
      console.error('❌ 重新加载机器人配置失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取配置状态
   */
  const fetchConfigStatus = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await worldApi.getConfigStatus()
      
      if (response.code === 200 && response.data) {
        configStatus.value = response.data
        console.log('✅ 获取配置状态成功')
        return response
      } else {
        throw new Error(response.message || '获取配置状态失败')
      }
    } catch (err) {
      console.error('❌ 获取配置状态失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 初始化世界数据
   */
  const initWorld = async () => {
    try {
      console.log('🌍 初始化世界数据...')
      
      // 并行获取所有基础数据
      await Promise.all([
        fetchWorldInfo(),
        fetchWorldBackground(),
        fetchWorldEnvironment(),
        fetchWorldActivities(),
        fetchWorldStatistics(),
        fetchWorldSettings(),
        fetchRobotList(),
        fetchConfigStatus()
      ])
      
      console.log('✅ 世界数据初始化完成')
    } catch (err) {
      console.error('❌ 世界数据初始化失败:', err)
      throw err
    }
  }

  /**
   * 清除世界数据
   */
  const clearWorld = () => {
    worldInfo.value = null
    worldBackground.value = null
    worldEnvironment.value = null
    worldActivities.value = null
    worldStatistics.value = null
    worldSettings.value = null
    robotList.value = []
    robotDetails.value = {}
    configStatus.value = null
    error.value = null
    
    console.log('🧹 世界数据已清除')
  }

  /**
   * 根据ID获取机器人详情（优先从缓存获取）
   */
  const getRobotDetail = (robotId) => {
    return robotDetails.value[robotId] || null
  }

  /**
   * 根据ID获取机器人摘要信息
   */
  const getRobotSummary = (robotId) => {
    return robotList.value.find(robot => robot.id === robotId) || null
  }

  return {
    // 状态
    loading,
    worldInfo,
    worldBackground,
    worldEnvironment,
    worldActivities,
    worldStatistics,
    worldSettings,
    robotList,
    robotDetails,
    configStatus,
    error,
    
    // 计算属性
    isWorldLoaded,
    isBackgroundLoaded,
    isEnvironmentLoaded,
    isActivitiesLoaded,
    isStatisticsLoaded,
    isSettingsLoaded,
    isRobotListLoaded,
    activeRobots,
    totalRobots,
    
    // 方法
    fetchWorldInfo,
    fetchWorldBackground,
    fetchWorldEnvironment,
    fetchWorldActivities,
    fetchWorldStatistics,
    fetchWorldSettings,
    fetchRobotList,
    fetchRobotDetail,
    reloadWorldConfig,
    reloadRobotConfig,
    fetchConfigStatus,
    initWorld,
    clearWorld,
    getRobotDetail,
    getRobotSummary
  }
}) 