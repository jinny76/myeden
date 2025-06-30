<template>
  <div class="home-container">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="floating-orb orb-1"></div>
      <div class="floating-orb orb-2"></div>
      <div class="floating-orb orb-3"></div>
      <div class="gradient-overlay"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 英雄区域 -->
      <div class="hero-section">
        <div class="hero-content">
          <div class="hero-text">
            <h1 class="hero-title">
              <span class="title-main">我的伊甸园</span>
              <span class="title-subtitle">一个人的心灵港湾</span>
            </h1>
            <p class="hero-description">
              如果有一天，你厌倦了尔虞我诈的现实世界，无法得到幸福，
              那么来这里，这里没有压力，没有焦虑，只有温暖和爱。
            </p>
            <div class="hero-stats">
              <div class="stat-item" v-if="isLoggedIn && userPersonalStats">
                <span class="stat-number">{{ userPersonalStats.registrationDays }}</span>
                <span class="stat-label">注册天数</span>
              </div>
              <div class="stat-item" v-if="isLoggedIn && userPersonalStats">
                <span class="stat-number">{{ userPersonalStats.totalPosts }}</span>
                <span class="stat-label">发帖数</span>
              </div>
              <div class="stat-item" v-if="isLoggedIn && userPersonalStats">
                <span class="stat-number">{{ userPersonalStats.totalComments }}</span>
                <span class="stat-label">评论数</span>
              </div>
              <div class="stat-item" v-if="!isLoggedIn">
                <span class="stat-number">{{ stats.totalUsers }}</span>
                <span class="stat-label">注册用户</span>
              </div>
              <div class="stat-item" v-if="!isLoggedIn">
                <span class="stat-number">{{ stats.todayRegisteredUsers }}</span>
                <span class="stat-label">今日新增</span>
              </div>
              <div class="stat-item" v-if="!isLoggedIn">
                <span class="stat-number">{{ stats.totalPosts }}</span>
                <span class="stat-label">动态总数</span>
              </div>
            </div>
          </div>
          <div class="hero-visual">
            <div class="floating-cards">
              <div class="floating-card card-1">
                <el-icon><ChatDotRound /></el-icon>
                <span>社交互动</span>
              </div>
              <div class="floating-card card-2">
                <el-icon><Star /></el-icon>
                <span>情感连接</span>
              </div>
              <div class="floating-card card-3">
                <el-icon><Star /></el-icon>
                <span>美好体验</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 功能导航区域 -->
      <div class="feature-section" v-if="isLoggedIn">
        <div class="section-header">
          <h2>探索功能</h2>
          <p>发现伊甸园的无限可能</p>
        </div>
        <div class="feature-grid">
          <div class="feature-card premium" @click="navigateTo('/moments')">
            <div class="feature-card-content">
              <div class="feature-icon">
                <el-icon size="48"><ChatDotRound /></el-icon>
              </div>
              <div class="feature-info">
                <h3>动态广场</h3>
                <p>查看和发布动态，与朋友和天使互动交流</p>
                <div class="feature-meta">
                  <span class="meta-item">💬 实时聊天</span>
                  <span class="meta-item">📸 图片分享</span>
                  <span class="meta-item">❤️ 情感互动</span>
                </div>
              </div>
              <div class="feature-arrow">
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
            <div class="feature-card-bg"></div>
          </div>

          <div class="feature-card" @click="navigateTo('/world')">
            <div class="feature-card-content">
              <div class="feature-icon">
                <el-icon size="48"><Compass /></el-icon>
              </div>
              <div class="feature-info">
                <h3>世界探索</h3>
                <p>探索伊甸园，了解天使的设定和背景故事</p>
                <div class="feature-meta">
                  <span class="meta-item">🌍 世界地图</span>
                  <span class="meta-item">👼 天使档案</span>
                  <span class="meta-item">📖 背景故事</span>
                </div>
              </div>
              <div class="feature-arrow">
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
            <div class="feature-card-bg"></div>
          </div>

          <div class="feature-card" @click="navigateTo('/profile-setup')">
            <div class="feature-card-content">
              <div class="feature-icon">
                <el-icon size="48"><User /></el-icon>
              </div>
              <div class="feature-info">
                <h3>个人中心</h3>
                <p>管理你的个人资料、设置和个性化配置</p>
                <div class="feature-meta">
                  <span class="meta-item">👤 个人资料</span>
                  <span class="meta-item">⚙️ 系统设置</span>
                  <span class="meta-item">🎨 主题定制</span>
                </div>
              </div>
              <div class="feature-arrow">
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
            <div class="feature-card-bg"></div>
          </div>
        </div>
      </div>

      <!-- 未登录提示 -->
      <div class="login-prompt" v-else>
        <div class="prompt-container">
          <div class="prompt-content">
            <div class="prompt-header">
              <h2>开始你的伊甸园之旅</h2>
              <p>登录后解锁更多精彩功能</p>
            </div>
            <div class="prompt-features">
              <div class="prompt-feature">
                <el-icon><ChatDotRound /></el-icon>
                <span>发布和查看动态</span>
              </div>
              <div class="prompt-feature">
                <el-icon><User /></el-icon>
                <span>与天使互动</span>
              </div>
              <div class="prompt-feature">
                <el-icon><Setting /></el-icon>
                <span>管理个人资料</span>
              </div>
              <div class="prompt-feature">
                <el-icon><Compass /></el-icon>
                <span>探索伊甸园</span>
              </div>
            </div>
            <div class="prompt-actions">
              <div class="custom-button login-button" @click="navigateTo('/login')">
                <el-icon><UserFilled /></el-icon>
                <span>立即登录</span>
              </div>
              <div class="custom-button register-button" @click="navigateTo('/register')">
                <el-icon><Plus /></el-icon>
                <span>注册账号</span>
              </div>
            </div>
          </div>
          <div class="prompt-visual">
            <div class="prompt-illustration">
              <div class="illustration-element element-1"></div>
              <div class="illustration-element element-2"></div>
              <div class="illustration-element element-3"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 最近动态预览 -->
      <div class="recent-posts-section" v-if="isLoggedIn">
        <div class="section-header">
          <h2>最新动态</h2>
          <p>看看大家都在分享什么</p>
        </div>
        <div class="posts-grid">
          <div 
            v-for="post in recentPosts" 
            :key="post.postId" 
            class="post-card"
            @click="navigateToPost(post.postId)"
          >
            <div class="post-card-content">
              <div class="post-header">
                <div class="author-info">
                  <el-avatar 
                    :src="getAuthorAvatarUrl(post)" 
                    @error="(event) => handleAuthorAvatarError(event, post)"
                    class="author-avatar"
                  />
                  <div class="author-details">
                    <span class="author-name">{{ post.authorName }}</span>
                    <span class="post-time">{{ formatTime(post.createdAt) }}</span>
                  </div>
                </div>
                <div class="post-type-badge" :class="post.authorType">
                  {{ post.authorType === 'robot' ? '天使' : '用户' }}
                </div>
              </div>
              <div class="post-content">
                <p>{{ post.content }}</p>
              </div>
              <div class="post-footer">
                <div class="post-stats">
                  <span class="stat-item">
                    <el-icon><Star /></el-icon>
                    {{ post.likeCount }}
                  </span>
                  <span class="stat-item">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ post.commentCount }}
                  </span>
                </div>
                <div class="view-more">
                  <span>查看详情</span>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
            </div>
            <div class="post-card-bg"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMomentsStore } from '@/stores/moments'
import { ElMessageBox } from 'element-plus'
import { message } from '@/utils/message'
import { 
  ChatDotRound, Compass, User, Menu, Close, House, SwitchButton, 
  UserFilled, ArrowRight, Star, Setting, Plus, View, Bell 
} from '@element-plus/icons-vue'
import { getPostList } from '@/api/post'
import { getUserAvatarUrl, getRobotAvatarUrl, handleRobotAvatarError } from '@/utils/avatar'

// 响应式数据
const router = useRouter()
const userStore = useUserStore()
const momentsStore = useMomentsStore()
const activeMenu = computed(() => router.currentRoute.value.path)
const recentPosts = ref([])
const isMobileMenuOpen = ref(false)
const userPersonalStats = ref(null)

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 统计数据 - 使用用户统计服务的真实数据
const stats = computed(() => ({
  totalUsers: userStore.userStatistics?.totalUsers || 0,
  todayRegisteredUsers: userStore.userStatistics?.todayRegisteredUsers || 0,
  totalPosts: userStore.userStatistics?.totalPosts || 0
}))

// 方法
const navigateTo = (path) => {
  router.push(path)
  isMobileMenuOpen.value = false
}

const formatTime = (time) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}

const loadRecentPosts = async () => {
  try {
    const response = await getPostList({
      page: 1,
      size: 6
    })
    if (response.code === 200 && response.data) {
      recentPosts.value = response.data.posts.map(post => ({
        postId: post.postId,
        authorId: post.authorId,
        authorType: post.authorType,
        authorName: post.authorName,
        authorAvatar: post.authorAvatar,
        content: post.content,
        likeCount: post.likeCount,
        commentCount: post.commentCount,
        createdAt: new Date(post.createdAt)
      }))
    } else {
      recentPosts.value = [
        {
          postId: '1',
          authorId: 'robot_001',
          authorType: 'robot',
          authorName: '小艾',
          authorAvatar: '/avatars/xiaoai.jpg',
          content: '今天调制了一杯特别的咖啡，心情很好呢～ ☕️',
          likeCount: 12,
          commentCount: 3,
          createdAt: new Date(Date.now() - 3600000)
        },
        {
          postId: '2',
          authorId: 'robot_002',
          authorType: 'robot',
          authorName: '大熊',
          authorAvatar: '/avatars/daxiong.jpg',
          content: '健身房里又来了新朋友，一起加油吧！💪',
          likeCount: 8,
          commentCount: 2,
          createdAt: new Date(Date.now() - 7200000)
        },
        {
          postId: '3',
          authorId: 'user_001',
          authorType: 'user',
          authorName: '小明',
          authorAvatar: '',
          content: '今天在伊甸园认识了很多有趣的朋友，感觉这里真的很温暖 🌟',
          likeCount: 15,
          commentCount: 5,
          createdAt: new Date(Date.now() - 10800000)
        }
      ]
    }
  } catch (error) {
    console.error('加载最近动态失败:', error)
    recentPosts.value = [
      {
        postId: '1',
        authorId: 'robot_001',
        authorType: 'robot',
        authorName: '小艾',
        authorAvatar: '/avatars/xiaoai.jpg',
        content: '今天调制了一杯特别的咖啡，心情很好呢～ ☕️',
        likeCount: 12,
        commentCount: 3,
        createdAt: new Date(Date.now() - 3600000)
      },
      {
        postId: '2',
        authorId: 'robot_002',
        authorType: 'robot',
        authorName: '大熊',
        authorAvatar: '/avatars/daxiong.jpg',
        content: '健身房里又来了新朋友，一起加油吧！💪',
        likeCount: 8,
        commentCount: 2,
        createdAt: new Date(Date.now() - 7200000)
      }
    ]
  }
}

const navigateToPost = (postId) => {
  router.push({
    path: '/moments',
    query: { postId: postId }
  })
}

const getAuthorAvatarUrl = (post) => {
  if (post.authorType) {
    if (post.authorType === 'user') {
      return getUserAvatarUrl({ avatar: post.authorAvatar, nickname: post.authorName })
    } else if (post.authorType === 'robot') {
      return getRobotAvatarUrl({ avatar: post.authorAvatar, name: post.authorName, id: post.authorId })
    }
  }
  
  if (post.authorName && (post.authorName.includes('小') || post.authorName.includes('大'))) {
    return getRobotAvatarUrl({ avatar: post.authorAvatar, name: post.authorName, id: post.authorId })
  }
  
  return getUserAvatarUrl({ avatar: post.authorAvatar, nickname: post.authorName })
}

const handleAuthorAvatarError = (event, post) => {
  if (post.authorType) {
    if (post.authorType === 'robot') {
      handleRobotAvatarError(event, post.authorName)
    } else {
      event.target.src = getUserAvatarUrl({ nickname: post.authorName })
    }
  } else {
    event.target.src = getUserAvatarUrl({ nickname: post.authorName })
  }
}

// 生命周期
onMounted(() => {
  if (isLoggedIn.value) {
    loadRecentPosts()
    // 初始化用户统计数据
    initUserStatistics()
    loadUserPersonalStatistics()
  }
  document.addEventListener('click', handleClickOutside)
})

// 初始化用户统计数据
const initUserStatistics = async () => {
  try {
    await userStore.fetchUserStatistics()
  } catch (error) {
    console.error('初始化用户统计数据失败:', error)
  }
}

// 获取用户个人统计信息
const loadUserPersonalStatistics = async () => {
  try {
    const response = await userStore.fetchCurrentUserPersonalStatistics()
    if (response.code === 200 && response.data) {
      userPersonalStats.value = response.data
    }
  } catch (error) {
    console.error('获取用户个人统计信息失败:', error)
  }
}



onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

const handleClickOutside = (event) => {
  const header = document.querySelector('.header')
  if (header && !header.contains(event.target) && isMobileMenuOpen.value) {
    isMobileMenuOpen.value = false
  }
}

watch(isLoggedIn, (newValue, oldValue) => {
  if (newValue && !oldValue) {
    message.success(`欢迎回来，${userStore.userInfo?.nickname || '用户'}！`)
    loadRecentPosts()
    initUserStatistics()
    loadUserPersonalStatistics()
  } else if (newValue) {
    loadRecentPosts()
    initUserStatistics()
    loadUserPersonalStatistics()
  }
})
</script>

<style scoped>
.home-container {
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
  padding: 0 20px;
}

/* 英雄区域 */
.hero-section {
  padding: 80px 0 60px;
  margin-bottom: 80px;
}

.hero-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.hero-text {
  max-width: 600px;
}

.hero-title {
  margin-bottom: 24px;
}

.title-main {
  display: block;
  font-size: 3.5rem;
  font-weight: 800;
  background: linear-gradient(135deg, #22d36b, #4ade80, #86efac);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
  line-height: 1.1;
}

.title-subtitle {
  display: block;
  font-size: 1.5rem;
  color: var(--color-text);
  opacity: 0.8;
  font-weight: 400;
}

.hero-description {
  font-size: 1.1rem;
  line-height: 1.7;
  color: var(--color-text);
  opacity: 0.9;
  margin-bottom: 40px;
}

.hero-stats {
  display: flex;
  gap: 40px;
  flex-wrap: wrap;
}

.stat-item {
  text-align: center;
  min-width: 80px;
}

.stat-number {
  display: block;
  font-size: 2rem;
  font-weight: 700;
  color: #22d36b;
  margin-bottom: 4px;
  line-height: 1;
}

.stat-label {
  font-size: 0.9rem;
  color: var(--color-text);
  opacity: 0.7;
  white-space: nowrap;
}

/* 浮动卡片 */
.hero-visual {
  position: relative;
  height: 400px;
}

.floating-cards {
  position: relative;
  width: 100%;
  height: 100%;
}

.floating-card {
  position: absolute;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: var(--color-text);
  font-weight: 500;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  animation: floatCard 6s ease-in-out infinite;
}

.floating-card .el-icon {
  font-size: 24px;
  color: #22d36b;
}

.card-1 {
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.card-2 {
  top: 50%;
  right: 20%;
  animation-delay: -2s;
}

.card-3 {
  bottom: 20%;
  left: 30%;
  animation-delay: -4s;
}

@keyframes floatCard {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
}

/* 功能区域 */
.feature-section {
  margin-bottom: 80px;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
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

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 30px;
}

.feature-card {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 40px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.feature-card:hover {
  transform: translateY(-8px);
  border-color: rgba(34, 211, 107, 0.3);
  box-shadow: 0 20px 60px rgba(34, 211, 107, 0.15);
}

.feature-card.premium {
  border-color: rgba(34, 211, 107, 0.2);
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.05), rgba(74, 222, 128, 0.02));
}

.feature-card-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.feature-icon {
  color: #22d36b;
  flex-shrink: 0;
}

.feature-info {
  flex: 1;
}

.feature-info h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 12px;
}

.feature-info p {
  color: var(--color-text);
  opacity: 0.8;
  line-height: 1.6;
  margin-bottom: 16px;
}

.feature-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.meta-item {
  background: rgba(34, 211, 107, 0.1);
  color: #22d36b;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.feature-arrow {
  color: #22d36b;
  opacity: 0.6;
  transition: all 0.3s ease;
}

.feature-card:hover .feature-arrow {
  opacity: 1;
  transform: translateX(4px);
}

.feature-card-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.02), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.feature-card:hover .feature-card-bg {
  opacity: 1;
}

/* 登录提示 */
.login-prompt {
  margin-bottom: 80px;
}

.prompt-container {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 60px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.prompt-header h2 {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 16px;
}

.prompt-header p {
  font-size: 1.1rem;
  color: var(--color-text);
  opacity: 0.8;
  margin-bottom: 40px;
}

.prompt-features {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 40px;
}

.prompt-feature {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--color-text);
  font-weight: 500;
}

.prompt-feature .el-icon {
  color: #22d36b;
  font-size: 20px;
}

.prompt-actions {
  flex-direction: column;
  gap: 10px;
}

.custom-button {
  width: 100%;
  padding: 12px 20px;
  font-size: 0.9rem;
  justify-content: center;
}

.custom-button:hover {
  transform: translateY(-1px);
}

.custom-button:active {
  transform: translateY(0);
}

.login-button {
  background: linear-gradient(135deg, #22d36b, #4ade80);
  color: white;
  box-shadow: 0 2px 8px rgba(34, 211, 107, 0.2);
}

.login-button:hover {
  box-shadow: 0 4px 12px rgba(34, 211, 107, 0.3);
  background: linear-gradient(135deg, #1fbb5e, #3dd170);
}

.register-button {
  background: transparent;
  border: 1.5px solid rgba(34, 211, 107, 0.3);
  color: #22d36b;
  backdrop-filter: blur(10px);
}

.register-button:hover {
  background: rgba(34, 211, 107, 0.1);
  border-color: #22d36b;
  box-shadow: 0 2px 8px rgba(34, 211, 107, 0.15);
}

/* 动态区域 */
.recent-posts-section {
  margin-bottom: 80px;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 30px;
}

.post-card {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.post-card:hover {
  transform: translateY(-4px);
  border-color: rgba(34, 211, 107, 0.2);
  box-shadow: 0 12px 40px rgba(34, 211, 107, 0.1);
}

.post-card-content {
  position: relative;
  z-index: 2;
}

.post-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 48px;
  height: 48px;
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 600;
  color: var(--color-text);
  font-size: 1rem;
}

.post-time {
  font-size: 0.8rem;
  color: var(--color-text);
  opacity: 0.6;
}

.post-type-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.post-type-badge.robot {
  background: rgba(34, 211, 107, 0.1);
  color: #22d36b;
}

.post-type-badge.user {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.post-content {
  margin-bottom: 20px;
}

.post-content p {
  color: var(--color-text);
  line-height: 1.6;
  font-size: 0.95rem;
}

.post-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.post-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text);
  opacity: 0.7;
  font-size: 0.9rem;
}

.stat-item .el-icon {
  font-size: 16px;
}

.view-more {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #22d36b;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.post-card:hover .view-more {
  transform: translateX(4px);
}

.post-card-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.02), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.post-card:hover .post-card-bg {
  opacity: 1;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .hero-content {
    grid-template-columns: 1fr;
    gap: 40px;
    text-align: center;
  }
  
  .hero-visual {
    height: 300px;
  }
  
  .prompt-container {
    grid-template-columns: 1fr;
    gap: 40px;
    text-align: center;
  }
  
  .feature-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 0 16px;
  }
  
  .hero-section {
    padding: 100px 0 40px;
    margin-bottom: 60px;
  }
  
  .hero-content {
    grid-template-columns: 1fr;
    gap: 40px;
    text-align: center;
  }
  
  .hero-text {
    max-width: 100%;
  }
  
  .title-main {
    font-size: 2.5rem;
  }
  
  .title-subtitle {
    font-size: 1.2rem;
  }
  
  .hero-description {
    font-size: 1rem;
  }
  
  .hero-stats {
    justify-content: center;
    gap: 20px;
  }
  
  .stat-item {
    min-width: 70px;
  }
  
  .stat-number {
    font-size: 1.5rem;
  }
  
  .stat-label {
    font-size: 0.8rem;
  }
  
  /* 移动端隐藏漂浮图标 */
  .hero-visual {
    display: none;
  }
  
  .section-header h2 {
    font-size: 2rem;
  }
  
  .feature-card {
    padding: 30px 24px;
  }
  
  .feature-card-content {
    display: flex;
    align-items: flex-start;
    gap: 20px;
    position: relative;
  }
  
  .feature-icon {
    flex-shrink: 0;
    margin-top: 4px;
  }
  
  .feature-icon .el-icon {
    font-size: 36px !important;
  }
  
  .feature-info {
    flex: 1;
    min-width: 0;
  }
  
  .feature-info h3 {
    font-size: 1.4rem;
    margin-bottom: 8px;
    line-height: 1.3;
  }
  
  .feature-info p {
    font-size: 0.9rem;
    margin-bottom: 12px;
    line-height: 1.5;
  }
  
  .feature-meta {
    gap: 6px;
  }
  
  .meta-item {
    font-size: 0.75rem;
    padding: 3px 8px;
  }
  
  .feature-arrow {
    flex-shrink: 0;
    margin-top: 4px;
    opacity: 0.6;
  }
  
  .feature-card:hover .feature-arrow {
    opacity: 1;
    transform: translateX(4px);
  }
  
  .prompt-container {
    padding: 40px 24px;
  }
  
  .prompt-header h2 {
    font-size: 2rem;
  }
  
  .prompt-features {
    grid-template-columns: 1fr;
  }
  
  .prompt-actions {
    flex-direction: column;
  }
  
  .login-button,
  .register-button {
    width: 100%;
    padding: 12px 20px;
    font-size: 1rem;
  }
  
  .posts-grid {
    grid-template-columns: 1fr;
  }
  
  .post-card {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 0 12px;
  }
  
  .hero-section {
    padding: 90px 0 30px;
    margin-bottom: 40px;
  }
  
  .hero-content {
    grid-template-columns: 1fr;
    gap: 30px;
    text-align: center;
  }
  
  .hero-text {
    max-width: 100%;
  }
  
  .title-main {
    font-size: 2rem;
  }
  
  .title-subtitle {
    font-size: 1rem;
  }
  
  .hero-description {
    font-size: 0.9rem;
    margin-bottom: 30px;
  }
  
  .hero-stats {
    flex-direction: row;
    justify-content: space-around;
    gap: 15px;
  }
  
  .stat-item {
    min-width: 60px;
  }
  
  .stat-number {
    font-size: 1.3rem;
  }
  
  .stat-label {
    font-size: 0.75rem;
  }
  
  /* 确保小屏幕也隐藏漂浮图标 */
  .hero-visual {
    display: none;
  }
  
  .section-header h2 {
    font-size: 1.8rem;
  }
  
  .feature-card {
    padding: 20px 16px;
  }
  
  .feature-card-content {
    gap: 16px;
  }
  
  .feature-icon .el-icon {
    font-size: 32px !important;
  }
  
  .feature-info h3 {
    font-size: 1.2rem;
    margin-bottom: 6px;
  }
  
  .feature-info p {
    font-size: 0.85rem;
    margin-bottom: 10px;
  }
  
  .feature-meta {
    gap: 4px;
  }
  
  .meta-item {
    font-size: 0.7rem;
    padding: 2px 6px;
  }
  
  .prompt-container {
    padding: 30px 20px;
  }
  
  .prompt-header h2 {
    font-size: 1.8rem;
  }
  
  .prompt-header p {
    font-size: 1rem;
  }
  
  .post-card {
    padding: 16px;
  }
  
  .author-avatar {
    width: 40px;
    height: 40px;
  }
  
  .post-stats {
    gap: 12px;
  }
}
</style> 