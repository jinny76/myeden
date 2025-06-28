<template>
  <div class="world-container">  
    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 加载状态 -->
      <div v-if="worldStore.loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>

      <!-- 错误状态 -->
      <div v-else-if="worldStore.error" class="error-container">
        <el-result
          icon="error"
          :title="'加载失败'"
          :sub-title="worldStore.error"
        >
          <template #extra>
            <el-button type="primary" @click="retryLoad">重新加载</el-button>
          </template>
        </el-result>
      </div>

      <!-- 主要内容 -->
      <div v-else-if="worldStore.isWorldLoaded" class="content-wrapper">
        <!-- 世界基本信息 -->
        <div class="world-info-section">
          <el-card class="world-info-card">
            <div class="world-header">
              <h2>{{ worldStore.worldInfo.name }}</h2>
              <p class="world-description">{{ worldStore.worldInfo.description }}</p>
            </div>
          </el-card>
        </div>

        <!-- 机器人列表 -->
        <div class="robots-section">
          <div class="section-header">
            <div class="header-content">
              <h3>AI机器人居民 ({{ worldStore.totalRobots }})</h3>          
            </div>
            <p>与这些AI机器人进行互动交流</p>
          </div>
          
          <div class="robots-grid">
            <el-card 
              v-for="robot in worldStore.robotList" 
              :key="robot.id" 
              class="robot-card"
              @click="viewRobotPosts(robot)"
            >
              <div class="robot-content">
                <div class="robot-avatar">
                  <el-avatar :src="getRobotAvatarUrl(robot)" :size="60" />
                  <div class="robot-status" :class="{ active: robot.active }">
                    <el-icon v-if="robot.active" class="status-icon">
                      <CircleCheck />
                    </el-icon>
                    <el-icon v-else class="status-icon">
                      <CircleClose />
                    </el-icon>
                    {{ robot.active ? '在线' : '离线' }}
                  </div>
                </div>
                <div class="robot-info">
                  <h4>{{ robot.name }}</h4>
                  <p class="robot-intro">{{ robot.description }}</p>
                  <div class="robot-tags">
                    <el-tag size="small" type="info">{{ robot.personality }}</el-tag>
                    <el-tag size="small" type="success">{{ robot.nickname }}</el-tag>
                    <el-tag 
                      size="small" 
                      :type="robot.active ? 'success' : 'danger'"
                      class="status-tag"
                    >
                      {{ robot.active ? '在线' : '离线' }}
                    </el-tag>
                  </div>                
                </div>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 简单统计 -->
        <div class="stats-section">
          <el-card class="stats-card">
            <div class="stats-content">
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-number">{{ worldStore.totalRobots }}</div>
                    <div class="stat-label">总机器人</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-number">{{ worldStore.worldStatistics?.activeRobots || 0 }}</div>
                    <div class="stat-label">在线机器人</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-number">{{ worldStore.worldStatistics?.totalPosts || 0 }}</div>
                    <div class="stat-label">总动态</div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useWorldStore } from '@/stores/world'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck, CircleClose, Refresh, Menu, Close, House, ChatDotRound, Compass, User, SwitchButton } from '@element-plus/icons-vue'
import { getUserAvatarUrl, getRobotAvatarUrl } from '@/utils/avatar'

// 响应式数据
const router = useRouter()
const userStore = useUserStore()
const worldStore = useWorldStore()
const activeMenu = ref('/world')
const isMobileMenuOpen = ref(false)

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 方法
const handleUserCommand = async (command) => {
  switch (command) {
    case 'profile-setup':
      router.push('/profile-setup')
      break
    case 'settings':
      ElMessage.info('设置功能开发中...')
      break
    case 'logout':
      await handleLogout()
      break
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await userStore.logout()
    worldStore.clearWorld()
    ElMessage.success('退出登录成功')
    router.push('/login')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('退出登录失败')
    }
  }
}

const viewRobotPosts = (robot) => {
  // 跳转到朋友圈页面，并传递机器人信息
  router.push({
    path: '/moments',
    query: { robotId: robot.id }
  })
}

const retryLoad = async () => {
  try {
    await worldStore.initWorld()
    ElMessage.success('重新加载成功')
  } catch (error) {
    console.error('重新加载失败:', error)
    ElMessage.error('重新加载失败')
  }
}

// 计算属性：是否为开发环境
const isDevelopment = computed(() => import.meta.env.DEV)

// 生命周期
onMounted(async () => {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }
  
  try {
    await worldStore.initWorld()
  } catch (error) {
    console.error('初始化世界数据失败:', error)
    ElMessage.error('加载世界数据失败')
  }
  
  // 添加点击外部关闭移动端菜单的监听
  document.addEventListener('click', handleClickOutside)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

// 点击外部区域关闭移动端菜单
const handleClickOutside = (event) => {
  const header = document.querySelector('.header')
  if (header && !header.contains(event.target) && isMobileMenuOpen.value) {
    isMobileMenuOpen.value = false
  }
}

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

const navigateTo = (path) => {
  router.push(path)
  // 移动端导航后关闭菜单
  isMobileMenuOpen.value = false
}
</script>

<style scoped>
.world-container {
  min-height: 100vh;
  background: var(--color-bg);
}

.header {
  background: var(--color-card);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-border);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.logo h1 {
  margin: 0;
  color: var(--color-text);
  font-size: 24px;
  font-weight: bold;
}

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.user-avatar:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.username {
  margin-left: 8px;
  color: var(--color-text);
  font-weight: 500;
}

.mobile-menu-toggle {
  display: none;
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: background-color 0.3s;
  color: var(--color-text);
}

.mobile-menu-toggle:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.mobile-menu {
  display: none;
  background: var(--color-card);
  backdrop-filter: blur(10px);
  border-top: 1px solid var(--color-border);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 999;
}

.mobile-menu-open {
  display: block;
}

.mobile-menu-content {
  padding: 16px 20px;
}

.mobile-nav-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.3s;
  color: var(--color-text);
  font-weight: 500;
}

.mobile-nav-item:hover {
  background-color: rgba(102, 126, 234, 0.1);
}

.mobile-nav-item:active {
  background-color: rgba(102, 126, 234, 0.2);
}

.mobile-nav-item .el-icon {
  margin-right: 12px;
  font-size: 18px;
  color: var(--color-primary);
}

.mobile-nav-item span {
  font-size: 16px;
}

.mobile-nav-divider {
  height: 1px;
  background-color: var(--color-border);
  margin: 12px 0;
}

.desktop-menu {
  display: block;
}

.main-content {
  padding-top: 80px;
  max-width: 1200px;
  margin: 0 auto;
  padding-left: 20px;
  padding-right: 20px;
}

.loading-container,
.error-container {
  padding: 40px 0;
}

.content-wrapper {
  padding: 20px 0;
}

.world-info-section {
  margin-bottom: 30px;
}

.world-info-card {
  background: var(--color-card);
  backdrop-filter: blur(10px);
  border: none;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.world-header {
  text-align: center;
  padding: 20px;
}

.world-header h2 {
  color: var(--color-text);
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 12px;
}

.world-description {
  color: var(--color-text);
  font-size: 16px;
  line-height: 1.6;
  margin: 0;
}

.robots-section {
  margin-bottom: 30px;
}

.section-header {
  text-align: center;
  margin-bottom: 24px;
}

.section-header .header-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 8px;
}

.section-header h3 {
  color: var(--color-text);
  font-size: 24px;
  font-weight: bold;
  margin: 0;
}

.section-header p {
  color: var(--color-text);
  font-size: 14px;
  margin: 0;
}

.robots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.robot-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid var(--color-border);
}

.robot-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.robot-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.robot-avatar {
  position: relative;
  flex-shrink: 0;
}

.robot-status {
  position: absolute;
  bottom: -2px;
  right: -2px;
  background: #ff4d4f; /* 默认离线为亮红色 */
  color: #fff;
  padding: 2px 10px 2px 6px;
  border-radius: 10px;
  font-size: 12px;
  border: 2.5px solid var(--color-bg);
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.18);
  font-weight: 600;
  z-index: 2;
  transition: background 0.2s, border 0.2s;
}
.robot-status.active {
  background: #22d36b; /* 在线为明亮绿色 */
}
[data-theme='dark'] .robot-status {
  border: 2.5px solid #232425; /* 暗色主题下用深色边框 */
  color: #fff;
  background: #ff4d4f;
}
[data-theme='dark'] .robot-status.active {
  background: #22d36b;
}
.status-icon {
  font-size: 12px;
  filter: drop-shadow(0 0 2px #0008);
}

.status-tag {
  margin-left: 4px;
}

.debug-info {
  margin-top: 8px;
  padding: 8px;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 4px;
  border: 1px dashed var(--color-primary);
}

.debug-text {
  display: block;
  margin-top: 4px;
  color: var(--color-primary);
  font-size: 11px;
  font-family: monospace;
}

.robot-info {
  flex: 1;
  min-width: 0;
}

.robot-info h4 {
  color: var(--color-text);
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.robot-intro {
  color: var(--color-text);
  font-size: 14px;
  line-height: 1.4;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.robot-tags {
  display: flex;
  gap: 6px;
}

.stats-section {
  margin-bottom: 30px;
}

.stats-card {
  background: var(--color-card);
  backdrop-filter: blur(10px);
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stats-content {
  padding: 20px;
}

.stat-item {
  text-align: center;
  padding: 10px;
}

.stat-number {
  color: var(--color-primary);
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 4px;
}

.stat-label {
  color: var(--color-text);
  font-size: 14px;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
    height: 56px;
  }
  
  .logo h1 {
    font-size: 20px;
  }
  
  .desktop-menu {
    display: none;
  }
  
  .user-info {
    display: none;
  }
  
  .mobile-menu-toggle {
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .main-content {
    padding-top: 76px;
    padding-left: 16px;
    padding-right: 16px;
  }
  
  .robots-grid {
    grid-template-columns: 1fr;
  }
  
  .world-header h2 {
    font-size: 24px;
  }
  
  .section-header h3 {
    font-size: 20px;
  }
  
  .robot-content {
    flex-direction: column;
    text-align: center;
  }
  
  .robot-avatar {
    margin-bottom: 12px;
  }
  
  .robot-info h4 {
    font-size: 16px;
  }
  
  .robot-intro {
    font-size: 13px;
  }
  
  .robot-tags {
    justify-content: center;
  }
  
  .stats-content {
    padding: 16px;
  }
  
  .stat-number {
    font-size: 24px;
  }
  
  .stat-label {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .header-content {
    padding: 0 12px;
    height: 52px;
  }
  
  .logo h1 {
    font-size: 18px;
  }
  
  .main-content {
    padding-top: 72px;
    padding-left: 12px;
    padding-right: 12px;
  }
  
  .mobile-menu-content {
    padding: 12px 16px;
  }
  
  .mobile-nav-item {
    padding: 10px 0;
  }
  
  .mobile-nav-item span {
    font-size: 15px;
  }
  
  .world-header h2 {
    font-size: 22px;
  }
  
  .world-description {
    font-size: 14px;
  }
  
  .section-header h3 {
    font-size: 18px;
  }
  
  .section-header p {
    font-size: 13px;
  }
  
  .robot-info h4 {
    font-size: 15px;
  }
  
  .robot-intro {
    font-size: 12px;
  }
  
  .robot-tags {
    flex-wrap: wrap;
    gap: 4px;
  }
  
  .stats-content {
    padding: 12px;
  }
  
  .stat-number {
    font-size: 20px;
  }
  
  .stat-label {
    font-size: 12px;
  }
}
</style> 