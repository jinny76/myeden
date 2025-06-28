<template>
  <div class="world-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <h1>虚拟世界</h1>
        </div>
        <div class="nav-menu">
          <el-menu mode="horizontal" :router="true" :default-active="activeMenu">
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/moments">朋友圈</el-menu-item>
            <el-menu-item index="/world">虚拟世界</el-menu-item>
          </el-menu>
        </div>
        <div class="user-info">
          <el-dropdown @command="handleUserCommand">
            <span class="user-avatar">
              <el-avatar :src="getUserAvatarUrl(userStore.userInfo)" />
              <span class="username">{{ userStore.userInfo?.nickname || '用户' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile-setup">个人资料</el-dropdown-item>
                <el-dropdown-item command="settings">设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

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
            <h3>AI机器人居民 ({{ worldStore.totalRobots }})</h3>
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
                  <el-avatar :src="robot.avatar" :size="60" />
                  <div class="robot-status" :class="{ active: robot.isActive }">
                    {{ robot.isActive ? '在线' : '离线' }}
                  </div>
                </div>
                <div class="robot-info">
                  <h4>{{ robot.name }}</h4>
                  <p class="robot-intro">{{ robot.description }}</p>
                  <div class="robot-tags">
                    <el-tag size="small" type="info">{{ robot.personality }}</el-tag>
                    <el-tag size="small" type="success">{{ robot.nickname }}</el-tag>
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
                    <div class="stat-number">{{ worldStore.activeRobots.length }}</div>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useWorldStore } from '@/stores/world'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserAvatarUrl } from '@/utils/avatar'

// 响应式数据
const router = useRouter()
const userStore = useUserStore()
const worldStore = useWorldStore()
const activeMenu = ref('/world')

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
})
</script>

<style scoped>
.world-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
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
  color: #333;
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
  color: #333;
  font-weight: 500;
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
  background: rgba(255, 255, 255, 0.95);
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
  color: #333;
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 12px;
}

.world-description {
  color: #666;
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

.section-header h3 {
  color: #333;
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.section-header p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.robots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.robot-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 20px;
}

.robot-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
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
  background: #f56c6c;
  color: white;
  padding: 2px 6px;
  border-radius: 8px;
  font-size: 10px;
  border: 2px solid white;
}

.robot-status.active {
  background: #67c23a;
}

.robot-info {
  flex: 1;
  min-width: 0;
}

.robot-info h4 {
  color: #333;
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.robot-intro {
  color: #666;
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
  background: rgba(255, 255, 255, 0.95);
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
  color: #409eff;
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 4px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 10px;
  }
  
  .logo h1 {
    font-size: 20px;
  }
  
  .main-content {
    padding-left: 10px;
    padding-right: 10px;
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
}
</style> 