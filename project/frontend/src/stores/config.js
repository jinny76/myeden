import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

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
  // 配置项定义
  const config = ref({
    // 通知设置
    notifications: {
      userOnline: false, // 用户上线消息通知，默认关闭
    },
    
    // 主题设置
    theme: {
      mode: 'dark', // light, dark, auto
    }
  })

  // 从localStorage加载配置
  const loadConfig = () => {
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
          }
        }
      }
    } catch (error) {
      console.error('加载配置失败:', error)
    }
  }

  // 保存配置到localStorage
  const saveConfig = () => {
    try {
      localStorage.setItem('myeden_config', JSON.stringify(config.value))
    } catch (error) {
      console.error('保存配置失败:', error)
    }
  }

  // 监听配置变化，自动保存
  watch(config, saveConfig, { deep: true })

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
  const updateNotification = (key, value) => {
    config.value.notifications[key] = value
  }

  const updateTheme = (key, value) => {
    config.value.theme[key] = value
    // 立即应用主题
    applyTheme()
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

  const resetConfig = () => {
    config.value = {
      notifications: {
        userOnline: false,
      },
      theme: {
        mode: 'light',
      }
    }
    applyTheme()
  }

  // 监听系统主题变化（当设置为auto时）
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  mediaQuery.addEventListener('change', () => {
    if (config.value.theme.mode === 'auto') {
      applyTheme()
    }
  })

  // 初始化时加载配置并应用主题
  loadConfig()
  applyTheme()

  return {
    // 状态
    config,
    
    // 计算属性
    isDarkMode,
    currentTheme,
    
    // 方法
    updateNotification,
    updateTheme,
    applyTheme,
    resetConfig,
    loadConfig,
    saveConfig
  }
}) 