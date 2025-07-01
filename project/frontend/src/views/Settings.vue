<template>
  <div class="settings-page">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="floating-orb orb-1"></div>
      <div class="floating-orb orb-2"></div>
      <div class="floating-orb orb-3"></div>
      <div class="gradient-overlay"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="settings-card">
        <div class="settings-header">
          <div class="header-icon">
            <el-icon size="32"><Setting /></el-icon>
          </div>
          <h1>个性化设置</h1>
          <p>自定义你的伊甸园体验，打造专属的视觉风格</p>
        </div>
        
        <!-- 主题设置 -->
        <div class="settings-section">
          <div class="section-header">
            <el-icon><Brush /></el-icon>
            <span>主题设置</span>
          </div>
          <div class="setting-item">
            <label class="setting-label">主题模式</label>
            <div class="setting-control">
              <div class="theme-options">
                <div 
                  class="theme-option" 
                  :class="{ active: config.theme.mode === 'light' }"
                  @click="updateTheme('mode', 'light')"
                >
                  <div class="theme-preview light-theme">
                    <div class="preview-header"></div>
                    <div class="preview-content"></div>
                  </div>
                  <span>浅色模式</span>
                </div>
                <div 
                  class="theme-option" 
                  :class="{ active: config.theme.mode === 'dark' }"
                  @click="updateTheme('mode', 'dark')"
                >
                  <div class="theme-preview dark-theme">
                    <div class="preview-header"></div>
                    <div class="preview-content"></div>
                  </div>
                  <span>深色模式</span>
                </div>
                <div 
                  class="theme-option" 
                  :class="{ active: config.theme.mode === 'auto' }"
                  @click="updateTheme('mode', 'auto')"
                >
                  <div class="theme-preview auto-theme">
                    <div class="preview-header"></div>
                    <div class="preview-content"></div>
                  </div>
                  <span>跟随系统</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 通知设置 -->
        <div class="settings-section">
          <div class="section-header">
            <el-icon><Bell /></el-icon>
            <span>通知设置</span>
          </div>
          <div class="setting-item">
            <label class="setting-label">用户上线通知</label>
            <div class="setting-control">
              <label class="switch">
                <input 
                  type="checkbox" 
                  v-model="config.notifications.userOnline"
                  @change="updateNotification('userOnline', config.notifications.userOnline)"
                >
                <span class="slider"></span>
              </label>
              <span class="setting-description">
                {{ config.notifications.userOnline ? '开启' : '关闭' }}
              </span>
            </div>
          </div>
        </div>

        <!-- 隐私设置 -->
        <div class="settings-section">
          <div class="section-header">
            <el-icon><Lock /></el-icon>
            <span>隐私设置</span>
          </div>
          <div class="setting-item">
            <label class="setting-label">公开我的帖子</label>
            <div class="setting-control">
              <label class="switch">
                <input 
                  type="checkbox" 
                  v-model="config.privacy.publicPosts"
                  @change="updatePrivacy('publicPosts', config.privacy.publicPosts)"
                >
                <span class="slider"></span>
              </label>
              <span class="setting-description">
                {{ config.privacy.publicPosts ? '公开' : '私密' }}
              </span>
            </div>
            <div class="setting-hint">
              开启后，其他用户可以查看你发布的帖子
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="settings-actions">
          <button @click="resetSettings" class="reset-btn">
            <el-icon><Refresh /></el-icon>
            <span>重置设置</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useConfigStore } from '@/stores/config'
import { useUserStore } from '@/stores/user'
import { Setting, Brush, Bell, Refresh, Monitor, Lock } from '@element-plus/icons-vue'
import { message } from '@/utils/message'

/**
 * 设置页面组件
 * 
 * 功能说明：
 * - 提供用户偏好设置界面
 * - 支持主题模式切换（浅色/深色/跟随系统）
 * - 管理通知设置
 * - 提供设置重置功能
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

const configStore = useConfigStore()
const userStore = useUserStore()

// 计算属性
const config = computed(() => configStore.config)
const loading = computed(() => configStore.loading)
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 生命周期
onMounted(async () => {
  if (isLoggedIn.value) {
    await configStore.loadUserSetting()
  }
})

// 方法
const updateTheme = async (key, value) => {
  try {
  console.log('主题切换:', { key, value })
    await configStore.updateTheme(key, value)
    
    if (isLoggedIn.value) {
      message.success('主题设置已保存')
    }
    
  // 添加延迟检查，确保主题已应用
  setTimeout(() => {
    const root = document.documentElement
    console.log('当前主题状态:', {
      mode: config.value.theme.mode,
      hasDarkModeClass: root.classList.contains('dark-mode'),
      dataTheme: root.getAttribute('data-theme'),
      computedStyle: getComputedStyle(root).getPropertyValue('--color-bg')
    })
  }, 100)
  } catch (error) {
    console.error('主题切换失败:', error)
    message.error('主题设置保存失败')
  }
}

const updateNotification = async (key, value) => {
  try {
    await configStore.updateNotification(key, value)
    
    if (isLoggedIn.value) {
      message.success('通知设置已保存')
    }
  } catch (error) {
    console.error('通知设置更新失败:', error)
    message.error('通知设置保存失败')
  }
}

const updatePrivacy = async (key, value) => {
  try {
    await configStore.updatePrivacy(key, value)
    
    if (isLoggedIn.value) {
      message.success('隐私设置已保存')
    }
  } catch (error) {
    console.error('隐私设置更新失败:', error)
    message.error('隐私设置保存失败')
  }
}

const resetSettings = async () => {
  if (confirm('确定要重置所有设置吗？')) {
    try {
      await configStore.resetConfig()
      message.success('设置已重置')
    } catch (error) {
      console.error('重置设置失败:', error)
      message.error('重置设置失败')
    }
  }
}

const testTheme = () => {
  const root = document.documentElement
  const currentMode = config.value.theme.mode
  
  console.log('=== 主题测试 ===')
  console.log('当前主题模式:', currentMode)
  console.log('HTML元素类:', root.classList.toString())
  console.log('data-theme属性:', root.getAttribute('data-theme'))
  console.log('CSS变量 --color-bg:', getComputedStyle(root).getPropertyValue('--color-bg'))
  console.log('CSS变量 --color-text:', getComputedStyle(root).getPropertyValue('--color-text'))
  console.log('CSS变量 --color-card:', getComputedStyle(root).getPropertyValue('--color-card'))
  
  // 测试主题切换
  const testModes = ['light', 'dark', 'auto']
  const currentIndex = testModes.indexOf(currentMode)
  const nextMode = testModes[(currentIndex + 1) % testModes.length]
  
  console.log('切换到主题:', nextMode)
  configStore.updateTheme('mode', nextMode)
  
  // 延迟检查切换结果
  setTimeout(() => {
    console.log('=== 切换后状态 ===')
    console.log('主题模式:', config.value.theme.mode)
    console.log('HTML元素类:', root.classList.toString())
    console.log('data-theme属性:', root.getAttribute('data-theme'))
    console.log('CSS变量 --color-bg:', getComputedStyle(root).getPropertyValue('--color-bg'))
  }, 200)
}
</script>

<style scoped lang="scss">
.settings-page {
  min-height: 100vh;
  background: var(--color-bg);
  position: relative;
  overflow-x: hidden;
}

/* 背景装饰 */
.background-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.floating-orb {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.1), rgba(74, 222, 128, 0.05));
  filter: blur(40px);
  animation: float 20s ease-in-out infinite;
}

.orb-1 {
  width: 300px;
  height: 300px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 200px;
  height: 200px;
  top: 60%;
  right: 15%;
  animation-delay: -7s;
}

.orb-3 {
  width: 150px;
  height: 150px;
  bottom: 20%;
  left: 20%;
  animation-delay: -14s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  33% { transform: translateY(-30px) rotate(120deg); }
  66% { transform: translateY(20px) rotate(240deg); }
}

.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 50% 50%, transparent 0%, rgba(0, 0, 0, 0.02) 100%);
}

.main-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 120px 20px 40px;
}

.settings-card {
  position: relative;
  background: var(--color-card);
  backdrop-filter: blur(20px);
  border: 1px solid var(--color-border);
  border-radius: 24px;
  padding: 40px 40px;
  width: 100%;
  max-width: 600px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.settings-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.02), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.settings-card:hover::before {
  opacity: 1;
}

.settings-header {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
  z-index: 2;
}

.header-icon {
  color: var(--color-primary);
  margin-bottom: 16px;
}

.settings-header h1 {
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #22d36b, #4ade80, #86efac);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12px;
  line-height: 1.2;
}

.settings-header p {
  font-size: 1rem;
  color: var(--color-text);
  opacity: 0.8;
  line-height: 1.5;
  margin: 0;
}

.settings-section {
  margin-bottom: 32px;
  position: relative;
  z-index: 2;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  color: var(--color-text);
  font-weight: 600;
  font-size: 1.1rem;
  
  .el-icon {
    color: var(--color-primary);
    font-size: 20px;
  }
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  
  &:hover {
    border-color: var(--color-primary);
    background: rgba(34, 211, 107, 0.05);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.setting-label {
  font-size: 1rem;
  font-weight: 500;
  color: var(--color-text);
}

.setting-control {
  display: flex;
  align-items: center;
  gap: 12px;
}

.setting-description {
  font-size: 0.9rem;
  color: var(--color-text);
  opacity: 0.7;
  min-width: 40px;
  text-align: center;
}

.setting-hint {
  font-size: 0.85rem;
  color: var(--color-text);
  opacity: 0.6;
  margin-top: 8px;
  line-height: 1.4;
}

/* 主题选择器 */
.theme-options {
  display: flex;
  gap: 16px;
  align-items: center;
}

.theme-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 8px;
  border-radius: 8px;
  
  &:hover {
    transform: translateY(-2px);
    background: rgba(34, 211, 107, 0.1);
  }
  
  &.active {
    .theme-preview {
      border-color: var(--color-primary);
      box-shadow: 0 2px 8px rgba(34, 211, 107, 0.3);
    }
    
    span {
      color: var(--color-primary);
      font-weight: 600;
    }
  }
  
  span {
    font-size: 0.85rem;
    color: var(--color-text);
    font-weight: 500;
    transition: color 0.3s ease;
    white-space: nowrap;
  }
}

.theme-preview {
  width: 60px;
  height: 45px;
  border: 2px solid var(--color-border);
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  
  .preview-header {
    height: 15px;
    background: #f5f5f5;
  }
  
  .preview-content {
    height: 30px;
    background: white;
  }
  
  &.dark-theme {
    .preview-header {
      background: #2c2c2c;
    }
    
    .preview-content {
      background: #1a1a1a;
    }
  }
  
  &.auto-theme {
    .preview-header {
      background: linear-gradient(90deg, #f5f5f5 50%, #2c2c2c 50%);
    }
    
    .preview-content {
      background: linear-gradient(90deg, white 50%, #1a1a1a 50%);
    }
  }
}

/* 开关样式 */
.switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--color-border);
  transition: 0.3s;
  border-radius: 24px;
  border: 1px solid var(--color-border);
}

.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 2px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

input:checked + .slider {
  background-color: var(--color-primary);
  border-color: var(--color-primary);
}

input:checked + .slider:before {
  transform: translateX(26px);
}

/* 操作按钮 */
.settings-actions {
  text-align: center;
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border);
  position: relative;
  z-index: 2;
}

.test-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
  border: 1px solid rgba(245, 108, 108, 0.3);
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  
  &:hover {
    background: rgba(245, 108, 108, 0.2);
    border-color: #f56c6c;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
  }
  
  &:active {
    transform: translateY(0);
  }
  
  .el-icon {
    font-size: 16px;
  }
}

.reset-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
  border: 1px solid rgba(245, 108, 108, 0.3);
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  
  &:hover {
    background: rgba(245, 108, 108, 0.2);
    border-color: #f56c6c;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
  }
  
  &:active {
    transform: translateY(0);
  }
  
  .el-icon {
    font-size: 16px;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    padding: 100px 16px 20px;
  }
  
  .settings-card {
    padding: 30px 24px;
  }
  
  .settings-header h1 {
    font-size: 1.8rem;
  }
  
  .settings-header p {
    font-size: 0.9rem;
  }
  
  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .setting-control {
    width: 100%;
    justify-content: space-between;
  }
  
  .theme-options {
    gap: 12px;
  }
  
  .theme-preview {
    width: 50px;
    height: 38px;
  }
  
  .theme-preview .preview-header {
    height: 12px;
  }
  
  .theme-preview .preview-content {
    height: 26px;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 90px 12px 20px;
  }
  
  .settings-card {
    padding: 24px 20px;
  }
  
  .settings-header h1 {
    font-size: 1.6rem;
  }
  
  .settings-header p {
    font-size: 0.85rem;
  }
  
  .setting-item {
    padding: 16px;
  }
  
  .theme-options {
    gap: 8px;
  }
  
  .theme-option {
    padding: 6px;
  }
  
  .theme-preview {
    width: 45px;
    height: 34px;
  }
  
  .theme-preview .preview-header {
    height: 10px;
  }
  
  .theme-preview .preview-content {
    height: 24px;
  }
  
  .reset-btn {
    padding: 12px 20px;
    font-size: 0.95rem;
  }
}
</style> 