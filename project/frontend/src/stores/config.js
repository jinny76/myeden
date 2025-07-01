import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { getUserSetting, saveUserSetting, updateUserSetting } from '@/api/userSetting'
import { useUserStore } from '@/stores/user'

/**
 * 配置管理Store
 * 
 * 功能说明：
 * - 管理用户偏好设置
 * - 持久化配置到localStorage
 * - 提供主题模式管理
 * - 管理上线通知设置
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

export const useConfigStore = defineStore('config', () => {
  const userStore = useUserStore()
  
  // 配置项定义
  const config = ref({
    // 通知设置
    notifications: {
      userOnline: false, // 用户上线消息通知，默认关闭
    },
    
    // 隐私设置
    privacy: {
      publicPosts: false, // 公开我的帖子，默认否
    },
    
    // 主题设置
    theme: {
      mode: 'auto', // light, dark, auto
    }
  })

  // 加载状态
  const loading = ref(false)
  const initialized = ref(false)

  // 从localStorage加载配置（作为备用）
  const loadLocalConfig = () => {
    try {
      const savedConfig = localStorage.getItem('myeden_config')
      if (savedConfig) {
        const parsed = JSON.parse(savedConfig)
        // 合并配置，保留默认值
        config.value = {
          ...config.value,
          ...parsed,
          notifications: {
            ...config.value.notifications,
            ...parsed.notifications
          },
          theme: {
            ...config.value.theme,
            ...parsed.theme
          },
          privacy: {
            ...config.value.privacy,
            ...parsed.privacy
          }
        }
      }
    } catch (error) {
      console.error('加载本地配置失败:', error)
    }
  }

  // 从后端加载用户设置
  const loadUserSetting = async () => {
    if (!userStore.isLoggedIn) {
      console.log('用户未登录，使用本地配置')
      loadLocalConfig()
      return
    }

    try {
      loading.value = true
      console.log('从后端加载用户设置...')
      
      const response = await getUserSetting()
      if (response.code === 200 && response.data) {
        const userSetting = response.data
        
        // 将后端数据转换为前端配置格式
        config.value = {
          notifications: {
            userOnline: userSetting.notifyUserOnline || false,
          },
          privacy: {
            publicPosts: userSetting.publicPosts || false,
          },
          theme: {
            mode: userSetting.themeMode || 'auto',
          }
        }
        
        console.log('用户设置加载成功:', config.value)
      } else {
        console.log('后端无用户设置，使用默认配置')
        loadLocalConfig()
      }
    } catch (error) {
      console.error('加载用户设置失败:', error)
      console.log('使用本地配置作为备用')
      loadLocalConfig()
    } finally {
      loading.value = false
      initialized.value = true
    }
  }

  // 保存配置到localStorage（作为备用）
  const saveLocalConfig = () => {
    try {
      localStorage.setItem('myeden_config', JSON.stringify(config.value))
    } catch (error) {
      console.error('保存本地配置失败:', error)
    }
  }

  // 保存配置到后端
  const saveUserSettingToBackend = async () => {
    if (!userStore.isLoggedIn) {
      console.log('用户未登录，只保存到本地')
      saveLocalConfig()
      return
    }

    try {
      console.log('保存用户设置到后端...')
      
      const userSettingData = {
        themeMode: config.value.theme.mode,
        notifyUserOnline: config.value.notifications.userOnline,
        publicPosts: config.value.privacy.publicPosts,
      }
      
      const response = await saveUserSetting(userSettingData)
      if (response.code === 200) {
        console.log('用户设置保存成功')
      } else {
        console.error('保存用户设置失败:', response.message)
        // 保存到本地作为备用
        saveLocalConfig()
      }
    } catch (error) {
      console.error('保存用户设置失败:', error)
      // 保存到本地作为备用
      saveLocalConfig()
    }
  }

  // 监听配置变化，自动保存
  watch(config, saveUserSettingToBackend, { deep: true })

  // 计算属性
  const isDarkMode = computed(() => {
    if (config.value.theme.mode === 'auto') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    return config.value.theme.mode === 'dark'
  })

  const currentTheme = computed(() => {
    return {
      ...config.value.theme,
      isDark: isDarkMode.value
    }
  })

  // 方法
  const updateNotification = async (key, value) => {
    config.value.notifications[key] = value
    
    // 如果是用户上线通知，同步到后端
    if (key === 'userOnline' && userStore.isLoggedIn) {
      try {
        await updateNotificationSetting('user_online', value)
      } catch (error) {
        console.error('更新通知设置失败:', error)
      }
    }
  }

  const updatePrivacy = async (key, value) => {
    config.value.privacy[key] = value
    
    // 同步到后端
    if (userStore.isLoggedIn) {
      try {
        await saveUserSettingToBackend()
      } catch (error) {
        console.error('更新隐私设置失败:', error)
      }
    }
  }

  const updateTheme = async (key, value) => {
    config.value.theme[key] = value
    
    // 立即应用主题
    applyTheme()
    
    // 同步到后端
    if (key === 'mode' && userStore.isLoggedIn) {
      try {
        await updateThemeMode(value)
      } catch (error) {
        console.error('更新主题设置失败:', error)
      }
    }
  }

  const applyTheme = () => {
    const root = document.documentElement
    const mode = config.value.theme.mode
    
    // 根据模式直接判断是否为暗色模式
    let isDark = false
    if (mode === 'auto') {
      isDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    } else {
      isDark = mode === 'dark'
    }
    
    // 设置暗色模式类
    if (isDark) {
      root.classList.add('dark-mode')
      root.setAttribute('data-theme', 'dark')
    } else {
      root.classList.remove('dark-mode')
      root.setAttribute('data-theme', 'light')
    }
    
    console.log('主题已应用:', { mode, isDark })
  }

  const resetConfig = async () => {
    config.value = {
      notifications: {
        userOnline: false,
      },
      privacy: {
        publicPosts: false,
      },
      theme: {
        mode: 'auto',
      }
    }
    applyTheme()
    
    // 同步到后端
    if (userStore.isLoggedIn) {
      try {
        await resetUserSetting()
      } catch (error) {
        console.error('重置用户设置失败:', error)
      }
    }
  }

  // 监听系统主题变化（当设置为auto时）
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  mediaQuery.addEventListener('change', () => {
    if (config.value.theme.mode === 'auto') {
      applyTheme()
    }
  })

  // 初始化时加载配置并应用主题
  loadLocalConfig()
  applyTheme()

  return {
    // 状态
    config,
    loading,
    initialized,
    
    // 计算属性
    isDarkMode,
    currentTheme,
    
    // 方法
    updateNotification,
    updatePrivacy,
    updateTheme,
    applyTheme,
    resetConfig,
    loadUserSetting,
    loadLocalConfig,
    saveLocalConfig
  }
}) 