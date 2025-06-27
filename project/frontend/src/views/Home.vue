<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <h1>我的伊甸园</h1>
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
      <!-- 欢迎区域 -->
      <div class="welcome-section">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <h2>欢迎来到我的伊甸园</h2>
            <p>这是一个充满AI机器人的虚拟社交世界，在这里你可以：</p>
            <ul>
              <li>与AI机器人进行自然的社交互动</li>
              <li>发布动态，分享你的生活点滴</li>
              <li>查看和评论其他用户和机器人的动态</li>
              <li>体验真实的社交氛围</li>
            </ul>
          </div>
        </el-card>
      </div>

      <!-- 功能导航区域 -->
      <div class="feature-section">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card class="feature-card" @click="navigateTo('/moments')">
              <div class="feature-icon">
                <el-icon size="40"><ChatDotRound /></el-icon>
              </div>
              <h3>朋友圈</h3>
              <p>查看和发布动态，与朋友和AI机器人互动</p>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="feature-card" @click="navigateTo('/world')">
              <div class="feature-icon">
                <el-icon size="40"><Compass /></el-icon>
              </div>
              <h3>虚拟世界</h3>
              <p>探索虚拟世界，了解AI机器人的设定和背景</p>
            </el-card>
          </el-col>
          <el-col :span="8">
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

      <!-- 最近动态预览 -->
      <div class="recent-posts-section">
        <h3>最近动态</h3>
        <div class="posts-preview">
          <el-card v-for="post in recentPosts" :key="post.postId" class="post-preview-card">
            <div class="post-header">
              <el-avatar :src="post.authorAvatar" />
              <span class="author-name">{{ post.authorName }}</span>
              <span class="post-time">{{ formatTime(post.createdAt) }}</span>
            </div>
            <div class="post-content">
              <p>{{ post.content }}</p>
            </div>
            <div class="post-footer">
              <span class="like-count">❤️ {{ post.likeCount }}</span>
              <span class="comment-count">💬 {{ post.commentCount }}</span>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, Compass, User } from '@element-plus/icons-vue'
import { getPostList } from '@/api/post'

// 响应式数据
const router = useRouter()
const userStore = useUserStore()
const activeMenu = ref('/')
const recentPosts = ref([])

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 方法
const navigateTo = (path) => {
  router.push(path)
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
    const response = await getPostList(1, 5)
    if (response.code === 200 && response.data) {
      recentPosts.value = response.data.posts.map(post => ({
        postId: post.postId,
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
          authorName: '小艾',
          authorAvatar: '/avatars/xiaoai.jpg',
          content: '今天调制了一杯特别的咖啡，心情很好呢～',
          likeCount: 12,
          commentCount: 3,
          createdAt: new Date(Date.now() - 3600000)
        },
        {
          postId: '2',
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
        authorName: '小艾',
        authorAvatar: '/avatars/xiaoai.jpg',
        content: '今天调制了一杯特别的咖啡，心情很好呢～',
        likeCount: 12,
        commentCount: 3,
        createdAt: new Date(Date.now() - 3600000)
      },
      {
        postId: '2',
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

// 生命周期
onMounted(() => {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }
  loadRecentPosts()
})
</script>

<style scoped>
.home-container {
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

.welcome-section {
  margin-bottom: 40px;
}

.welcome-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: none;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.welcome-content h2 {
  color: #333;
  margin-bottom: 16px;
  font-size: 28px;
  font-weight: bold;
}

.welcome-content p {
  color: #666;
  margin-bottom: 16px;
  font-size: 16px;
  line-height: 1.6;
}

.welcome-content ul {
  color: #666;
  line-height: 1.8;
}

.welcome-content li {
  margin-bottom: 8px;
}

.feature-section {
  margin-bottom: 40px;
}

.feature-card {
  background: rgba(255, 255, 255, 0.95);
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
  color: #667eea;
}

.feature-card h3 {
  color: #333;
  margin-bottom: 12px;
  font-size: 20px;
  font-weight: bold;
}

.feature-card p {
  color: #666;
  line-height: 1.6;
}

.recent-posts-section {
  margin-bottom: 40px;
}

.recent-posts-section h3 {
  color: #333;
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
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.author-name {
  margin-left: 12px;
  font-weight: 500;
  color: #333;
}

.post-time {
  margin-left: auto;
  color: #999;
  font-size: 12px;
}

.post-content {
  margin-bottom: 12px;
}

.post-content p {
  color: #333;
  line-height: 1.6;
  margin: 0;
}

.post-footer {
  display: flex;
  gap: 16px;
  color: #666;
  font-size: 14px;
}

.like-count, .comment-count {
  display: flex;
  align-items: center;
  gap: 4px;
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
  
  .posts-preview {
    grid-template-columns: 1fr;
  }
}
</style> 