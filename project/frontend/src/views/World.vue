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
              <el-avatar :src="userStore.userInfo?.avatar || '/default-avatar.png'" />
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
      <!-- 世界背景介绍 -->
      <div class="world-intro-section">
        <el-card class="world-intro-card">
          <div class="world-header">
            <h2>{{ worldInfo.name }}</h2>
            <p class="world-description">{{ worldInfo.description }}</p>
          </div>
          <div class="world-background">
            <h3>世界背景</h3>
            <p>{{ worldInfo.background }}</p>
          </div>
          <div class="world-view">
            <h3>世界观</h3>
            <p>{{ worldInfo.worldview }}</p>
          </div>
        </el-card>
      </div>

      <!-- AI机器人列表 -->
      <div class="robots-section">
        <div class="section-header">
          <h3>AI机器人居民</h3>
          <p>在这个虚拟世界中，生活着各种性格迥异的AI机器人，他们会与你进行自然的社交互动</p>
        </div>
        
        <div class="robots-grid">
          <el-card 
            v-for="robot in robots" 
            :key="robot.robotId" 
            class="robot-card"
            @click="showRobotDetail(robot)"
          >
            <div class="robot-avatar">
              <el-avatar :src="robot.avatar" :size="80" />
              <div class="robot-status" :class="{ active: robot.isActive }">
                {{ robot.isActive ? '在线' : '离线' }}
              </div>
            </div>
            <div class="robot-info">
              <h4>{{ robot.name }}</h4>
              <p class="robot-intro">{{ robot.introduction }}</p>
              <div class="robot-tags">
                <el-tag size="small" type="info">{{ robot.profession }}</el-tag>
                <el-tag size="small" type="success">{{ robot.mbti }}</el-tag>
              </div>
              <div class="robot-stats">
                <div class="stat-item">
                  <span class="stat-label">回复速度</span>
                  <el-rate 
                    v-model="robot.replySpeed" 
                    :max="10" 
                    disabled 
                    show-score 
                    text-color="#ff9900"
                  />
                </div>
                <div class="stat-item">
                  <span class="stat-label">活跃度</span>
                  <el-progress 
                    :percentage="robot.replyFrequency * 10" 
                    :color="getProgressColor(robot.replyFrequency)"
                  />
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 机器人行为统计 -->
      <div class="behavior-stats-section">
        <el-card class="stats-card">
          <div class="stats-header">
            <h3>机器人行为统计</h3>
            <p>今日机器人活跃情况</p>
          </div>
          <div class="stats-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="stat-box">
                  <div class="stat-number">{{ todayStats.postCount }}</div>
                  <div class="stat-label">发布动态</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-box">
                  <div class="stat-number">{{ todayStats.commentCount }}</div>
                  <div class="stat-label">发表评论</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-box">
                  <div class="stat-number">{{ todayStats.replyCount }}</div>
                  <div class="stat-label">回复互动</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 机器人详情对话框 -->
    <el-dialog
      v-model="robotDetailVisible"
      :title="selectedRobot?.name + ' 的详细信息'"
      width="600px"
      class="robot-detail-dialog"
    >
      <div v-if="selectedRobot" class="robot-detail-content">
        <div class="detail-header">
          <el-avatar :src="selectedRobot.avatar" :size="100" />
          <div class="detail-info">
            <h3>{{ selectedRobot.name }}</h3>
            <p class="detail-intro">{{ selectedRobot.introduction }}</p>
            <div class="detail-tags">
              <el-tag type="info">{{ selectedRobot.profession }}</el-tag>
              <el-tag type="success">{{ selectedRobot.mbti }}</el-tag>
              <el-tag type="warning">{{ selectedRobot.gender }}</el-tag>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4>性格设定</h4>
          <p>{{ selectedRobot.personality }}</p>
        </div>

        <div class="detail-section">
          <h4>行为特征</h4>
          <div class="behavior-stats">
            <div class="behavior-item">
              <span>回复速度</span>
              <el-rate 
                v-model="selectedRobot.replySpeed" 
                :max="10" 
                disabled 
                show-score 
                text-color="#ff9900"
              />
            </div>
            <div class="behavior-item">
              <span>回复频度</span>
              <el-rate 
                v-model="selectedRobot.replyFrequency" 
                :max="10" 
                disabled 
                show-score 
                text-color="#ff9900"
              />
            </div>
            <div class="behavior-item">
              <span>分享频度</span>
              <el-rate 
                v-model="selectedRobot.shareFrequency" 
                :max="10" 
                disabled 
                show-score 
                text-color="#ff9900"
              />
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4>活跃时间段</h4>
          <div class="active-times">
            <el-tag 
              v-for="timeRange in selectedRobot.activeTimeRanges" 
              :key="timeRange.startTime"
              class="time-tag"
            >
              {{ timeRange.startTime }} - {{ timeRange.endTime }}
            </el-tag>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="robotDetailVisible = false">关闭</el-button>
          <el-button type="primary" @click="viewRobotPosts">查看动态</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useWorldStore } from '@/stores/world'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const router = useRouter()
const userStore = useUserStore()
const worldStore = useWorldStore()
const activeMenu = ref('/world')
const robotDetailVisible = ref(false)
const selectedRobot = ref(null)
const selectedRobotDetail = ref(null)

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

const showRobotDetail = async (robot) => {
  try {
    selectedRobot.value = robot
    robotDetailVisible.value = true
    
    // 获取机器人详情
    const cachedDetail = worldStore.getRobotDetail(robot.id)
    if (cachedDetail) {
      selectedRobotDetail.value = cachedDetail
    } else {
      await worldStore.fetchRobotDetail(robot.id)
      selectedRobotDetail.value = worldStore.getRobotDetail(robot.id)
    }
  } catch (error) {
    console.error('获取机器人详情失败:', error)
    ElMessage.error('获取机器人详情失败')
  }
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

const getProgressColor = (frequency) => {
  if (frequency >= 8) return '#67c23a'
  if (frequency >= 6) return '#e6a23c'
  return '#f56c6c'
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

.world-intro-section {
  margin-bottom: 40px;
}

.world-intro-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: none;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.world-header {
  text-align: center;
  margin-bottom: 30px;
}

.world-header h2 {
  color: #333;
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 16px;
}

.world-description {
  color: #666;
  font-size: 18px;
  line-height: 1.6;
  margin: 0;
}

.world-background,
.world-view {
  margin-bottom: 24px;
}

.world-background h3,
.world-view h3 {
  color: #333;
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 12px;
}

.world-background p,
.world-view p {
  color: #666;
  line-height: 1.8;
  margin: 0;
}

.robots-section {
  margin-bottom: 40px;
}

.section-header {
  text-align: center;
  margin-bottom: 30px;
}

.section-header h3 {
  color: #333;
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 12px;
}

.section-header p {
  color: #666;
  font-size: 16px;
  line-height: 1.6;
  margin: 0;
}

.robots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 24px;
}

.robot-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: none;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 24px;
}

.robot-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.robot-avatar {
  text-align: center;
  margin-bottom: 20px;
  position: relative;
}

.robot-status {
  position: absolute;
  bottom: 0;
  right: 50%;
  transform: translateX(50%);
  background: #f56c6c;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.robot-status.active {
  background: #67c23a;
}

.robot-info {
  text-align: center;
}

.robot-info h4 {
  color: #333;
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 8px;
}

.robot-intro {
  color: #666;
  line-height: 1.6;
  margin-bottom: 16px;
  min-height: 48px;
}

.robot-tags {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 16px;
}

.robot-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-label {
  color: #666;
  font-size: 14px;
  min-width: 80px;
}

.behavior-stats-section {
  margin-bottom: 40px;
}

.stats-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: none;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.stats-header {
  text-align: center;
  margin-bottom: 30px;
}

.stats-header h3 {
  color: #333;
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stats-header p {
  color: #666;
  margin: 0;
}

.stats-content {
  padding: 20px 0;
}

.stat-box {
  text-align: center;
  padding: 20px;
}

.stat-number {
  color: #409eff;
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.robot-detail-dialog {
  border-radius: 16px;
}

.robot-detail-content {
  padding: 20px 0;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.detail-info h3 {
  color: #333;
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.detail-intro {
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}

.detail-tags {
  display: flex;
  gap: 8px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  color: #333;
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 12px;
}

.detail-section p {
  color: #666;
  line-height: 1.6;
  margin: 0;
}

.behavior-stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.behavior-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.behavior-item span {
  color: #666;
  font-size: 14px;
  min-width: 80px;
}

.active-times {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.time-tag {
  font-size: 12px;
}

.dialog-footer {
  text-align: right;
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
    font-size: 24px;
  }
}
</style> 