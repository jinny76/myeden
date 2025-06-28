<template>
  <div class="home-container">
    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 欢迎区域 -->
      <div class="welcome-section">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <h2>欢迎来到我的伊甸园</h2>
            <p>这是一个i人的社交世界，在这里你可以：</p>
            <ul>
              <li>与小天使们进行自然的社交互动</li>
              <li>发布动态，分享你的生活点滴</li>
              <li>查看和评论其他人的动态</li>
              <li>体验真实的社交氛围</li>
            </ul>
          </div>
        </el-card>
      </div>

      <!-- 功能导航区域 -->
      <div class="feature-section" v-if="isLoggedIn">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
            <el-card class="feature-card" @click="navigateTo('/moments')">
              <div class="feature-icon">
                <el-icon size="40"><ChatDotRound /></el-icon>
              </div>
              <h3>动态</h3>
              <p>查看和发布动态，与朋友和天使互动</p>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
            <el-card class="feature-card" @click="navigateTo('/world')">
              <div class="feature-icon">
                <el-icon size="40"><Compass /></el-icon>
              </div>
              <h3>介绍</h3>
              <p>探索伊甸园，了解天使的设定和背景</p>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
            <el-card class="feature-card" @click="navigateTo('/profile-setup')">
              <div class="feature-icon">
                <el-icon size="40"><User /></el-icon>
              </div>
              <h3>个人资料</h3>
              <p>管理你的个人资料和设置</p>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 未登录提示 -->
      <div class="login-prompt" v-else>
        <el-card class="prompt-card">
          <div class="prompt-content">
            <h3>登录后体验更多功能</h3>
            <p>登录后你可以：</p>
            <ul>
              <li>发布和查看动态</li>
              <li>与天使互动</li>
              <li>管理个人资料</li>
              <li>探索伊甸园</li>
            </ul>
            <div class="prompt-actions">
              <el-button type="primary" size="small" @click="navigateTo('/login')" class="login-button">立即登录</el-button>
              <el-button size="small" @click="navigateTo('/register')" style="margin-left: 15px;" class="register-button">注册账号</el-button>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 最近动态预览 -->
      <div class="recent-posts-section" v-if="isLoggedIn">
        <h3>最近动态</h3>
        <div class="posts-preview">
          <el-card 
            v-for="post in recentPosts" 
            :key="post.postId" 
            class="post-preview-card"
            @click="navigateToPost(post.postId)"
          >
            <div class="post-header">
              <el-avatar 
                :src="getAuthorAvatarUrl(post)" 
                @error="(event) => handleAuthorAvatarError(event, post)"
              />
              <span class="author-name">{{ post.authorName }}</span>
              <span class="post-time">{{ formatTime(post.createdAt) }}</span>
              <el-icon class="click-hint"><ArrowRight /></el-icon>
            </div>
            <div class="post-content">
              <p>{{ post.content }}</p>
            </div>
            <div class="post-footer">
              <span class="like-count">❤️ {{ post.likeCount }}</span>
              <span class="comment-count">💬 {{ post.commentCount }}</span>
              <span class="view-detail">点击查看详情 →</span>
            </div>
          </el-card>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, Compass, User, Menu, Close, House, SwitchButton, UserFilled, ArrowRight } from '@element-plus/icons-vue'
import { getPostList } from '@/api/post'
import { getUserAvatarUrl, getRobotAvatarUrl, handleRobotAvatarError } from '@/utils/avatar'

// 响应式数据
const router = useRouter()
const userStore = useUserStore()
const momentsStore = useMomentsStore()
const activeMenu = computed(() => router.currentRoute.value.path)
const recentPosts = ref([])
const isMobileMenuOpen = ref(false)

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 方法
const navigateTo = (path) => {
  router.push(path)
  // 移动端导航后关闭菜单
  isMobileMenuOpen.value = false
}

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
    ElMessage.success('退出登录成功')
    router.push('/login')
    // 移动端退出后关闭菜单
    isMobileMenuOpen.value = false
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('退出登录失败')
    }
  }
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
    // 调用API获取最近动态
    const response = await getPostList({
      page: 1,
      size: 5
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
      // 如果API调用失败，使用模拟数据
      recentPosts.value = [
        {
          postId: '1',
          authorId: 'robot_001',
          authorType: 'robot',
          authorName: '小艾',
          authorAvatar: '/avatars/xiaoai.jpg',
          content: '今天调制了一杯特别的咖啡，心情很好呢～',
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
  } catch (error) {
    console.error('加载最近动态失败:', error)
    // 使用模拟数据作为备用
    recentPosts.value = [
      {
        postId: '1',
        authorId: 'robot_001',
        authorType: 'robot',
        authorName: '小艾',
        authorAvatar: '/avatars/xiaoai.jpg',
        content: '今天调制了一杯特别的咖啡，心情很好呢～',
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
  // 跳转到动态详情页
  router.push(`/post/${postId}`)
}

const getAuthorAvatarUrl = (post) => {
  // 如果post有authorType字段，根据类型处理
  if (post.authorType) {
    if (post.authorType === 'user') {
      return getUserAvatarUrl({ avatar: post.authorAvatar, nickname: post.authorName })
    } else if (post.authorType === 'robot') {
      return getRobotAvatarUrl({ avatar: post.authorAvatar, name: post.authorName, id: post.authorId })
    }
  }
  
  // 如果没有authorType字段，尝试判断是否为机器人（通过名称或头像路径）
  if (post.authorName && (post.authorName.includes('小') || post.authorName.includes('大'))) {
    return getRobotAvatarUrl({ avatar: post.authorAvatar, name: post.authorName, id: post.authorId })
  }
  
  // 默认为用户头像
  return getUserAvatarUrl({ avatar: post.authorAvatar, nickname: post.authorName })
}

const handleAuthorAvatarError = (event, post) => {
  // 如果post有authorType字段，根据类型处理
  if (post.authorType) {
    if (post.authorType === 'robot') {
      handleRobotAvatarError(event, post.authorName)
    } else {
      event.target.src = getUserAvatarUrl({ nickname: post.authorName })
    }
  } else {
    // 默认为用户头像
    event.target.src = getUserAvatarUrl({ nickname: post.authorName })
  }
}

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

// 生命周期
onMounted(() => {
  // 如果用户已登录，加载最近动态
  if (isLoggedIn.value) {
    loadRecentPosts()
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

// 添加watch监听用户登录状态变化，自动加载数据
watch(isLoggedIn, (newValue, oldValue) => {
  if (newValue && !oldValue) {
    // 用户刚登录，显示欢迎提示
    ElMessage.success(`欢迎回来，${userStore.userInfo?.nickname || '用户'}！`)
    loadRecentPosts()
  } else if (newValue) {
    // 用户已登录，加载数据
    loadRecentPosts()
  }
})
</script>

<style scoped>
.home-container {
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
  padding: 0;
  height: auto;
  min-height: 60px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
}

.logo h1 {
  margin: 0;
  color: var(--color-text);
  font-size: 24px;
  font-weight: bold;
  white-space: nowrap;
}

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}

.desktop-menu {
  display: block;
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
  white-space: nowrap;
}

.auth-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
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
  color: var(--color-text);
}

.mobile-nav-item span {
  font-size: 16px;
}

.mobile-nav-divider {
  height: 1px;
  background-color: var(--color-border);
  margin: 12px 0;
}

.main-content {
  padding-top: 80px;
  max-width: 1200px;
  margin: 0 auto;
  padding-left: 20px;
  padding-right: 20px;
}

.welcome-section {
  margin-bottom: 40px;
}

.welcome-card {
  background: var(--color-card);
  backdrop-filter: blur(10px);
  border: none;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.welcome-content h2 {
  color: var(--color-text);
  margin-bottom: 16px;
  font-size: 28px;
  font-weight: bold;
}

.welcome-content p {
  color: var(--color-text);
  margin-bottom: 16px;
  font-size: 16px;
  line-height: 1.6;
}

.welcome-content ul {
  color: var(--color-text);
  line-height: 1.8;
}

.welcome-content li {
  margin-bottom: 8px;
}

.feature-section {
  margin-bottom: 40px;
}

.feature-card {
  background: var(--color-card);
  backdrop-filter: blur(10px);
  border: none;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  padding: 30px 20px;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  margin-bottom: 20px;
  color: var(--color-text);
}

.feature-card h3 {
  color: var(--color-text);
  margin-bottom: 12px;
  font-size: 20px;
  font-weight: bold;
}

.feature-card p {
  color: var(--color-text);
  line-height: 1.6;
}

.recent-posts-section {
  margin-bottom: 40px;
}

.recent-posts-section h3 {
  color: var(--color-text);
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: bold;
}

.posts-preview {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.post-preview-card {
  background: var(--color-card);
  backdrop-filter: blur(10px);
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 16px;
  position: relative;
  overflow: hidden;
}

.post-preview-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(102, 126, 234, 0.02) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.post-preview-card:hover::before {
  opacity: 1;
}

.post-preview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.post-preview-card:active {
  transform: translateY(0);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.post-preview-card:active::before {
  opacity: 0.5;
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  position: relative;
  z-index: 1;
}

.author-name {
  margin-left: 12px;
  font-weight: 500;
  color: var(--color-text);
}

.post-time {
  margin-left: auto;
  color: var(--color-text);
  font-size: 12px;
}

.click-hint {
  margin-left: 8px;
  color: var(--color-primary);
  font-size: 14px;
  opacity: 0.6;
  transition: all 0.3s ease;
}

.post-preview-card:hover .click-hint {
  opacity: 1;
  transform: translateX(2px);
}

.post-content {
  margin-bottom: 12px;
  position: relative;
  z-index: 1;
}

.post-content p {
  color: var(--color-text);
  line-height: 1.6;
  margin: 0;
}

.post-footer {
  display: flex;
  gap: 16px;
  color: var(--color-text);
  font-size: 14px;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 1;
}

.like-count, .comment-count {
  display: flex;
  align-items: center;
  gap: 4px;
}

.view-detail {
  color: var(--color-primary);
  font-size: 12px;
  opacity: 0.8;
  transition: opacity 0.3s ease;
}

.post-preview-card:hover .view-detail {
  opacity: 1;
}

.login-prompt {
  margin-bottom: 40px;
}

.prompt-card {
  background: var(--color-card);
  backdrop-filter: blur(10px);
  border: none;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.prompt-content {
  padding: 30px;
}

.prompt-content h3 {
  color: var(--color-text);
  margin-bottom: 16px;
  font-size: 24px;
  font-weight: bold;
}

.prompt-content p {
  color: var(--color-text);
  margin-bottom: 16px;
  line-height: 1.6;
}

.prompt-content ul {
  color: var(--color-text);
  line-height: 1.8;
}

.prompt-content li {
  margin-bottom: 8px;
}

.prompt-actions {
  text-align: right;
}

.prompt-actions .el-button {
  margin: 0 8px;
}

.login-button,
.register-button {
  width: 100px !important;
  height: 36px !important;
  font-size: 15px !important;
  padding: 0 10px !important;
  border-radius: 6px !important;
  min-width: 0 !important;
}

.prompt-actions {
  gap: 10px;
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
  
  .posts-preview {
    grid-template-columns: 1fr;
  }
  
  .feature-section .el-col {
    margin-bottom: 16px;
  }
  
  .welcome-content h2 {
    font-size: 24px;
  }
  
  .welcome-content p {
    font-size: 14px;
  }
  
  .feature-card {
    padding: 20px 16px;
  }
  
  .feature-card h3 {
    font-size: 18px;
  }
  
  .feature-card p {
    font-size: 14px;
  }
  
  .prompt-content {
    padding: 20px;
  }
  
  .prompt-content h3 {
    font-size: 20px;
  }
  
  .prompt-actions {
    text-align: center;
  }
  
  .prompt-actions .el-button {
    margin: 0 8px;
  }
  
  .recent-posts-section h3 {
    font-size: 20px;
  }
  
  .post-preview-card {
    margin-bottom: 16px;
  }
  
  .post-header {
    flex-wrap: wrap;
  }
  
  .post-time {
    font-size: 11px;
  }
  
  .click-hint {
    font-size: 12px;
  }
  
  .post-content p {
    font-size: 13px;
  }
  
  .post-footer {
    font-size: 11px;
    gap: 12px;
  }
  
  .view-detail {
    font-size: 10px;
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
  
  .welcome-content h2 {
    font-size: 22px;
  }
  
  .welcome-content p {
    font-size: 13px;
  }
  
  .feature-card {
    padding: 16px 12px;
  }
  
  .feature-card h3 {
    font-size: 16px;
  }
  
  .feature-card p {
    font-size: 13px;
  }
  
  .prompt-content {
    padding: 16px;
  }
  
  .prompt-content h3 {
    font-size: 18px;
  }
  
  .prompt-actions {
    flex-direction: column;
    gap: 12px;
  }
  
  .prompt-actions .el-button {
    margin: 0;
    width: 100%;
    height: 36px;
    font-size: 15px;
    padding: 0 10px;
    border-radius: 6px;
  }
  
  .recent-posts-section h3 {
    font-size: 18px;
  }
  
  .post-preview-card {
    padding: 12px;
  }
  
  .post-header {
    margin-bottom: 8px;
  }
  
  .post-content {
    margin-bottom: 8px;
  }
  
  .post-content p {
    font-size: 13px;
  }
  
  .click-hint {
    font-size: 11px;
  }
  
  .post-footer {
    font-size: 11px;
    gap: 12px;
  }
  
  .view-detail {
    font-size: 10px;
  }
}
</style> 