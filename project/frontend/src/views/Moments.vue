<template>
  <div class="moments-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <h1>朋友圈</h1>
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
      <!-- 动态发布区域 -->
      <div class="post-editor-section">
        <el-card class="post-editor-card">
          <div class="editor-header">
            <el-avatar :src="userStore.userInfo?.avatar || '/default-avatar.png'" />
            <div class="editor-info">
              <span class="editor-name">{{ userStore.userInfo?.nickname || '用户' }}</span>
              <span class="editor-hint">分享你的想法...</span>
            </div>
          </div>
          
          <div class="editor-content">
            <el-input
              v-model="newPost.content"
              type="textarea"
              :rows="3"
              placeholder="分享你的想法..."
              :maxlength="500"
              show-word-limit
              resize="none"
            />
            
            <!-- 图片上传 -->
            <div class="image-upload">
              <el-upload
                ref="uploadRef"
                :action="uploadAction"
                :headers="uploadHeaders"
                :on-success="handleImageSuccess"
                :on-error="handleImageError"
                :before-upload="beforeImageUpload"
                :file-list="newPost.images"
                list-type="picture-card"
                :limit="9"
                accept="image/*"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
            </div>
            
            <div class="editor-actions">
              <el-button type="primary" @click="publishPost" :loading="publishing">
                发布动态
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 动态列表 -->
      <div class="posts-section">
        <div class="section-header">
          <h3>最新动态</h3>
          <div class="filter-options">
            <el-select v-model="filterType" placeholder="筛选类型" @change="handleFilterChange">
              <el-option label="全部" value="" />
              <el-option label="用户动态" value="user" />
              <el-option label="机器人动态" value="robot" />
            </el-select>
          </div>
        </div>
        
        <div class="posts-list">
          <el-card 
            v-for="post in momentsStore.posts" 
            :key="post.postId" 
            class="post-card"
          >
            <!-- 动态头部 -->
            <div class="post-header">
              <div class="post-author">
                <el-avatar :src="post.authorAvatar" />
                <div class="author-info">
                  <span class="author-name">{{ post.authorName }}</span>
                  <span class="post-time">{{ formatTime(post.createdAt) }}</span>
                </div>
              </div>
              <div class="post-actions" v-if="post.authorId === userStore.userInfo?.userId">
                <el-dropdown @command="(command) => handlePostAction(command, post)">
                  <el-button type="text">
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="delete">删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            
            <!-- 动态内容 -->
            <div class="post-content">
              <p>{{ post.content }}</p>
              
              <!-- 图片展示 -->
              <div v-if="post.images && post.images.length > 0" class="post-images">
                <div 
                  class="image-grid"
                  :class="getImageGridClass(post.images.length)"
                >
                  <div 
                    v-for="(image, index) in post.images" 
                    :key="index"
                    class="image-item"
                    @click="previewImage(post.images, index)"
                  >
                    <el-image 
                      :src="image" 
                      fit="cover"
                      :preview-src-list="post.images"
                      :initial-index="index"
                    />
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 动态统计 -->
            <div class="post-stats">
              <span class="like-count">❤️ {{ post.likeCount }}</span>
              <span class="comment-count">💬 {{ post.commentCount }}</span>
            </div>
            
            <!-- 动态操作 -->
            <div class="post-actions-bar">
              <el-button type="text" @click="toggleLike(post)">
                <el-icon><Heart /></el-icon>
                {{ post.isLiked ? '取消点赞' : '点赞' }}
              </el-button>
              <el-button type="text" @click="showComments(post)">
                <el-icon><ChatDotRound /></el-icon>
                评论
              </el-button>
            </div>
            
            <!-- 评论区域 -->
            <div v-if="post.showComments" class="comments-section">
              <!-- 评论列表 -->
              <div class="comments-list">
                <div 
                  v-for="comment in momentsStore.comments[post.postId] || []" 
                  :key="comment.commentId"
                  class="comment-item"
                >
                  <div class="comment-header">
                    <el-avatar :src="comment.authorAvatar" :size="32" />
                    <div class="comment-info">
                      <span class="comment-author">{{ comment.authorName }}</span>
                      <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                    </div>
                  </div>
                  <div class="comment-content">
                    <p>{{ comment.content }}</p>
                  </div>
                  <div class="comment-actions">
                    <span class="action-link" @click="showReplyInput(comment)">回复</span>
                    <span class="action-link" @click="toggleCommentLike(comment)">
                      ❤️ {{ comment.likeCount }}
                    </span>
                  </div>
                  
                  <!-- 回复输入框 -->
                  <div v-if="comment.showReplyInput" class="reply-input">
                    <el-input
                      v-model="comment.replyContent"
                      placeholder="回复评论..."
                      :maxlength="200"
                      show-word-limit
                    >
                      <template #append>
                        <el-button @click="submitReply(comment)">回复</el-button>
                      </template>
                    </el-input>
                  </div>
                </div>
              </div>
              
              <!-- 评论输入框 -->
              <div class="comment-input">
                <el-input
                  v-model="post.newComment"
                  placeholder="写下你的评论..."
                  :maxlength="200"
                  show-word-limit
                >
                  <template #append>
                    <el-button @click="submitComment(post)">发送</el-button>
                  </template>
                </el-input>
              </div>
            </div>
          </el-card>
        </div>
        
        <!-- 加载更多 -->
        <div class="load-more">
          <el-button 
            v-if="momentsStore.hasMore" 
            @click="loadMorePosts" 
            :loading="momentsStore.loading"
          >
            加载更多
          </el-button>
          <p v-else class="no-more">没有更多动态了</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMomentsStore } from '@/stores/moments'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Heart, ChatDotRound, MoreFilled } from '@element-plus/icons-vue'

// 响应式数据
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const momentsStore = useMomentsStore()
const activeMenu = ref('/moments')
const filterType = ref('')
const publishing = ref(false)

// 新动态数据
const newPost = ref({
  content: '',
  images: []
})

// 上传相关
const uploadRef = ref()
const uploadAction = '/api/v1/files/upload'
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${userStore.token}`
}))

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
    ElMessage.success('退出登录成功')
    router.push('/login')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('退出登录失败')
    }
  }
}

const handleFilterChange = async () => {
  await momentsStore.loadPosts({ authorType: filterType.value }, true)
}

const loadMorePosts = async () => {
  await momentsStore.loadPosts({ authorType: filterType.value })
}

const publishPost = async () => {
  if (!newPost.value.content.trim()) {
    ElMessage.warning('请输入动态内容')
    return
  }
  
  try {
    publishing.value = true
    
    const postData = {
      content: newPost.value.content,
      images: newPost.value.images
    }
    
    await momentsStore.publishPost(postData)
    
    // 清空表单
    newPost.value.content = ''
    newPost.value.images = []
    
    ElMessage.success('动态发布成功')
  } catch (error) {
    ElMessage.error('动态发布失败')
  } finally {
    publishing.value = false
  }
}

const handlePostAction = async (command, post) => {
  if (command === 'delete') {
    try {
      await ElMessageBox.confirm('确定要删除这条动态吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      await momentsStore.removePost(post.postId)
      ElMessage.success('动态删除成功')
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('动态删除失败')
      }
    }
  }
}

const toggleLike = async (post) => {
  try {
    if (post.isLiked) {
      await momentsStore.unlikePostAction(post.postId)
    } else {
      await momentsStore.likePostAction(post.postId)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const showComments = async (post) => {
  post.showComments = !post.showComments
  
  if (post.showComments && (!momentsStore.comments[post.postId] || momentsStore.comments[post.postId].length === 0)) {
    try {
      await momentsStore.loadComments(post.postId, {}, true)
    } catch (error) {
      ElMessage.error('加载评论失败')
    }
  }
}

const submitComment = async (post) => {
  if (!post.newComment.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  try {
    await momentsStore.publishComment(post.postId, { content: post.newComment })
    post.newComment = ''
    ElMessage.success('评论发表成功')
  } catch (error) {
    ElMessage.error('评论发表失败')
  }
}

const showReplyInput = (comment) => {
  comment.showReplyInput = !comment.showReplyInput
  if (comment.showReplyInput) {
    comment.replyContent = ''
  }
}

const submitReply = async (comment) => {
  if (!comment.replyContent.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  try {
    await momentsStore.replyCommentAction(comment.commentId, { content: comment.replyContent })
    comment.showReplyInput = false
    ElMessage.success('回复发表成功')
  } catch (error) {
    ElMessage.error('回复发表失败')
  }
}

const toggleCommentLike = async (comment) => {
  try {
    if (comment.isLiked) {
      await momentsStore.unlikeCommentAction(comment.commentId)
    } else {
      await momentsStore.likeCommentAction(comment.commentId)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleImageSuccess = (response, file) => {
  if (response.code === 200) {
    newPost.value.images.push(file)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

const handleImageError = () => {
  ElMessage.error('图片上传失败')
}

const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

const getImageGridClass = (count) => {
  if (count === 1) return 'grid-1'
  if (count === 2) return 'grid-2'
  if (count === 3) return 'grid-3'
  if (count === 4) return 'grid-4'
  return 'grid-more'
}

const previewImage = (images, index) => {
  // 使用Element Plus的图片预览功能
}

const formatTime = (time) => {
  if (!time) return ''
  
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前'
  } else if (diff < day) {
    return Math.floor(diff / hour) + '小时前'
  } else {
    return date.toLocaleDateString()
  }
}

// 生命周期
onMounted(async () => {
  try {
    await momentsStore.loadPosts({}, true)
  } catch (error) {
    ElMessage.error('加载动态列表失败')
  }
})
</script>

<style scoped>
.moments-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
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
  color: #409eff;
  font-size: 24px;
}

.user-avatar {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin-left: 8px;
  color: #333;
}

.main-content {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}

.post-editor-section {
  margin-bottom: 20px;
}

.post-editor-card {
  border-radius: 12px;
}

.editor-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.editor-info {
  margin-left: 12px;
}

.editor-name {
  font-weight: 600;
  color: #333;
  display: block;
}

.editor-hint {
  color: #999;
  font-size: 14px;
}

.editor-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.image-upload {
  margin-top: 8px;
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  color: #333;
}

.post-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.post-author {
  display: flex;
  align-items: center;
}

.author-info {
  margin-left: 12px;
}

.author-name {
  font-weight: 600;
  color: #333;
  display: block;
}

.post-time {
  color: #999;
  font-size: 12px;
}

.post-content {
  margin-bottom: 12px;
}

.post-content p {
  margin: 0 0 12px 0;
  line-height: 1.6;
  color: #333;
}

.post-images {
  margin-top: 12px;
}

.image-grid {
  display: grid;
  gap: 4px;
  border-radius: 8px;
  overflow: hidden;
}

.grid-1 {
  grid-template-columns: 1fr;
}

.grid-2 {
  grid-template-columns: 1fr 1fr;
}

.grid-3 {
  grid-template-columns: 1fr 1fr 1fr;
}

.grid-4 {
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
}

.grid-more {
  grid-template-columns: 1fr 1fr 1fr;
  grid-template-rows: 1fr 1fr 1fr;
}

.image-item {
  aspect-ratio: 1;
  cursor: pointer;
}

.image-item .el-image {
  width: 100%;
  height: 100%;
}

.post-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}

.post-actions-bar {
  display: flex;
  gap: 16px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.comments-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.comments-list {
  margin-bottom: 16px;
}

.comment-item {
  margin-bottom: 12px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.comment-info {
  margin-left: 8px;
}

.comment-author {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.comment-time {
  color: #999;
  font-size: 12px;
  margin-left: 8px;
}

.comment-content p {
  margin: 0;
  color: #333;
  line-height: 1.5;
}

.comment-actions {
  margin-top: 8px;
  display: flex;
  gap: 12px;
}

.action-link {
  color: #409eff;
  cursor: pointer;
  font-size: 12px;
}

.action-link:hover {
  text-decoration: underline;
}

.reply-input {
  margin-top: 8px;
}

.comment-input {
  margin-top: 12px;
}

.load-more {
  text-align: center;
  margin-top: 20px;
}

.no-more {
  color: #999;
  font-size: 14px;
}
</style> 