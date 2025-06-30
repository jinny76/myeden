<template>
  <div class="world-container">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="floating-orb orb-1"></div>
      <div class="floating-orb orb-2"></div>
      <div class="floating-orb orb-3"></div>
      <div class="gradient-overlay"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 加载状态 -->
      <div v-if="worldStore.loading" class="loading-container">
        <div class="loading-content">
          <div class="loading-spinner"></div>
          <p>正在加载伊甸园世界...</p>
        </div>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="worldStore.error" class="error-container">
        <div class="error-content">
          <div class="error-icon">
            <el-icon size="64"><CircleClose /></el-icon>
          </div>
          <h3>加载失败</h3>
          <p>{{ worldStore.error }}</p>
          <div class="custom-button retry-button" @click="retryLoad">
            <el-icon><Refresh /></el-icon>
            <span>重新加载</span>
          </div>
        </div>
      </div>

      <!-- 主要内容 -->
      <div v-else-if="worldStore.isWorldLoaded" class="content-wrapper">
        <!-- 世界基本信息 -->
        <div class="world-info-section">
          <div class="world-info-card">
            <div class="world-header">
              <div class="world-title">
                <h1>{{ worldStore.worldInfo.name }}</h1>
                <div class="world-badge">
                  <el-icon><Compass /></el-icon>
                  <span>伊甸园世界</span>
                </div>
              </div>
              <p class="world-description">{{ worldStore.worldInfo.description }}</p>
              <div class="world-stats">
                <div class="stat-item">
                  <span class="stat-number">{{ worldStore.totalRobots }}</span>
                  <span class="stat-label">个天使</span>
                </div>
                <div class="stat-item">
                  <span class="stat-number">{{ worldStore.worldStatistics?.activeRobots || 0 }}</span>
                  <span class="stat-label">在线天使</span>
                </div>
                <div class="stat-item">
                  <span class="stat-number">{{ worldStore.worldStatistics?.totalPosts || 0 }}</span>
                  <span class="stat-label">总动态</span>
                </div>
              </div>
            </div>
            <div class="world-card-bg"></div>
          </div>
        </div>

        <!-- 机器人列表 -->
        <div class="robots-section">
          <div class="section-header">
            <h2>天使们</h2>
            <p>与天使们进行互动交流，体验温暖的社交氛围</p>
          </div>
          
          <div class="robots-grid">
            <div 
              v-for="robot in worldStore.robotList" 
              :key="robot.id" 
              class="robot-card"
            >
              <div class="robot-content">
                <div class="robot-avatar-section">
                  <div class="robot-avatar">
                    <el-avatar :src="getRobotAvatarUrl(robot)" :size="80" />
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
                  <div class="robot-quick-info">
                    <h3>{{ robot.name }}</h3>
                    <div class="robot-personality">
                      <el-tag size="small" type="info">{{ robot.personality }}</el-tag>
                    </div>
                  </div>
                </div>
                <div class="robot-details">
                  <p class="robot-intro">{{ robot.description }}</p>
                  <div class="robot-tags">
                    <span class="tag-item">👼 {{ robot.nickname }}</span>
                    <span class="tag-item" :class="{ 'online': robot.active, 'offline': !robot.active }">
                      {{ robot.active ? '🟢 在线' : '🔴 离线' }}
                    </span>
                  </div>
                </div>
              </div>
              <div class="robot-card-bg"></div>
            </div>
          </div>
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
import { ElMessageBox } from 'element-plus'
import { message } from '@/utils/message'
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
const retryLoad = async () => {
  try {
    await worldStore.initWorld()
    message.success('重新加载成功')
  } catch (error) {
    console.error('重新加载失败:', error)
    message.error('重新加载失败')
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
    message.error('加载世界数据失败')
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
  max-width: 1400px;
  margin: 0 auto;
  padding: 80px 20px 40px;
}

/* 加载状态 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.loading-content {
  text-align: center;
  color: var(--color-text);
}

.loading-spinner {
  width: 60px;
  height: 60px;
  border: 3px solid rgba(34, 211, 107, 0.2);
  border-top: 3px solid #22d36b;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-content p {
  font-size: 1.1rem;
  opacity: 0.8;
}

/* 错误状态 */
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.error-content {
  text-align: center;
  color: var(--color-text);
  max-width: 400px;
}

.error-icon {
  color: #ff4d4f;
  margin-bottom: 20px;
}

.error-content h3 {
  font-size: 1.5rem;
  margin-bottom: 12px;
  color: var(--color-text);
}

.error-content p {
  font-size: 1rem;
  opacity: 0.8;
  margin-bottom: 30px;
  line-height: 1.6;
}

.custom-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  outline: none;
}

.retry-button {
  background: linear-gradient(135deg, #22d36b, #4ade80);
  color: white;
  box-shadow: 0 4px 12px rgba(34, 211, 107, 0.3);
}

.retry-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(34, 211, 107, 0.4);
}

/* 世界信息区域 */
.world-info-section {
  margin-bottom: 60px;
}

.world-info-card {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 60px 40px;
  overflow: hidden;
}

.world-header {
  text-align: center;
  position: relative;
  z-index: 2;
}

.world-title {
  margin-bottom: 24px;
}

.world-title h1 {
  font-size: 3rem;
  font-weight: 800;
  background: linear-gradient(135deg, #22d36b, #4ade80, #86efac);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 16px;
  line-height: 1.1;
}

.world-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(34, 211, 107, 0.1);
  color: #22d36b;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

.world-description {
  font-size: 1.2rem;
  line-height: 1.7;
  color: var(--color-text);
  opacity: 0.9;
  margin-bottom: 40px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.world-stats {
  display: flex;
  justify-content: center;
  gap: 60px;
  flex-wrap: wrap;
}

.stat-item {
  text-align: center;
  min-width: 100px;
}

.stat-number {
  display: block;
  font-size: 2.5rem;
  font-weight: 700;
  color: #22d36b;
  margin-bottom: 8px;
  line-height: 1;
}

.stat-label {
  font-size: 1rem;
  color: var(--color-text);
  opacity: 0.7;
  white-space: nowrap;
}

.world-card-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.02), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.world-info-card:hover .world-card-bg {
  opacity: 1;
}

/* 机器人区域 */
.robots-section {
  margin-bottom: 60px;
}

.section-header {
  text-align: center;
  margin-bottom: 50px;
}

.section-header h2 {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 16px;
}

.section-header p {
  font-size: 1.1rem;
  color: var(--color-text);
  opacity: 0.7;
}

.robots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 30px;
}

.robot-card {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 30px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.robot-card:hover {
  transform: translateY(-8px);
  border-color: rgba(34, 211, 107, 0.3);
  box-shadow: 0 20px 60px rgba(34, 211, 107, 0.15);
}

.robot-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.robot-avatar-section {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.robot-avatar {
  position: relative;
}

.robot-status {
  position: absolute;
  bottom: -4px;
  right: -4px;
  background: #ff4d4f;
  color: #fff;
  padding: 4px 12px 4px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  border: 3px solid var(--color-bg);
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
  font-weight: 600;
  z-index: 2;
  transition: all 0.3s ease;
}

.robot-status.active {
  background: #22d36b;
}

.status-icon {
  font-size: 10px;
  filter: drop-shadow(0 0 2px #0008);
}

.robot-quick-info {
  text-align: center;
}

.robot-quick-info h3 {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.robot-personality {
  display: flex;
  justify-content: center;
}

.robot-details {
  flex: 1;
  min-width: 0;
}

.robot-intro {
  color: var(--color-text);
  line-height: 1.6;
  font-size: 0.95rem;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.robot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  background: rgba(34, 211, 107, 0.1);
  color: #22d36b;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 0.8rem;
  font-weight: 500;
}

.tag-item.online {
  background: rgba(34, 211, 107, 0.15);
  color: #22d36b;
}

.tag-item.offline {
  background: rgba(255, 77, 79, 0.15);
  color: #ff4d4f;
}

.robot-card-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.02), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.robot-card:hover .robot-card-bg {
  opacity: 1;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .robots-grid {
    grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  }
  
  .world-title h1 {
    font-size: 2.5rem;
  }
  
  .world-stats {
    gap: 40px;
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 80px 16px 40px;
  }
  
  .world-info-card {
    padding: 40px 24px;
  }
  
  .world-title h1 {
    font-size: 2rem;
  }
  
  .world-description {
    font-size: 1rem;
    margin-bottom: 30px;
  }
  
  .world-stats {
    gap: 30px;
  }
  
  .stat-item {
    min-width: 80px;
  }
  
  .stat-number {
    font-size: 2rem;
  }
  
  .stat-label {
    font-size: 0.9rem;
  }
  
  .section-header h2 {
    font-size: 2rem;
  }
  
  .section-header p {
    font-size: 1rem;
  }
  
  .robots-grid {
    grid-template-columns: 1fr;
  }
  
  .robot-card {
    padding: 24px;
  }
  
  .robot-content {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
  
  .robot-avatar-section {
    flex-direction: row;
    justify-content: center;
    gap: 20px;
  }
  
  .robot-quick-info {
    text-align: left;
  }
  
  .robot-quick-info h3 {
    font-size: 1.2rem;
  }
  
  .robot-intro {
    font-size: 0.9rem;
    text-align: center;
  }
  
  .robot-tags {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 80px 12px 40px;
  }
  
  .world-info-card {
    padding: 30px 20px;
  }
  
  .world-title h1 {
    font-size: 1.8rem;
  }
  
  .world-badge {
    font-size: 0.8rem;
    padding: 6px 12px;
  }
  
  .world-description {
    font-size: 0.9rem;
    margin-bottom: 25px;
  }
  
  .world-stats {
    gap: 20px;
  }
  
  .stat-item {
    min-width: 70px;
  }
  
  .stat-number {
    font-size: 1.8rem;
  }
  
  .stat-label {
    font-size: 0.8rem;
  }
  
  .section-header h2 {
    font-size: 1.8rem;
  }
  
  .section-header p {
    font-size: 0.9rem;
  }
  
  .robot-card {
    padding: 20px;
  }
  
  .robot-avatar-section {
    gap: 16px;
  }
  
  .robot-quick-info h3 {
    font-size: 1.1rem;
  }
  
  .robot-intro {
    font-size: 0.85rem;
  }
  
  .robot-tags {
    gap: 6px;
  }
  
  .tag-item {
    font-size: 0.75rem;
    padding: 3px 10px;
  }
}
</style> 