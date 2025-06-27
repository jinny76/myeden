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
            <el-select v-model="filterType" placeholder="筛选类型" @change="loadPosts">
              <el-option label="全部" value="all" />
              <el-option label="用户动态" value="user" />
              <el-option label="机器人动态" value="robot" />
            </el-select>
          </div>
        </div>
        
        <div class="posts-list">
          <el-card 
            v-for="post in posts" 
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
                <el-dropdown @command="handlePostAction">
                  <el-button type="text">
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="{ action: 'edit', post }">编辑</el-dropdown-item>
                      <el-dropdown-item :command="{ action: 'delete', post }" divided>删除</el-dropdown-item>
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
                  v-for="comment in post.comments" 
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
                    <span class="action-link">❤️ {{ comment.likeCount }}</span>
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
                        <el-button @click="submitReply(comment, post)">回复</el-button>
                      </template>
                    </el-input>
                  </div>
                  
                  <!-- 回复列表 -->
                  <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
                    <div 
                      v-for="reply in comment.replies" 
                      :key="reply.replyId"
                      class="reply-item"
                    >
                      <div class="reply-header">
                        <el-avatar :src="reply.authorAvatar" :size="24" />
                        <div class="reply-info">
                          <span class="reply-author">{{ reply.authorName }}</span>
                          <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
                        </div>
                      </div>
                      <div class="reply-content">
                        <p>{{ reply.content }}</p>
                      </div>
                    </div>
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
            v-if="hasMore" 
            @click="loadMorePosts" 
            :loading="loading"
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Heart, ChatDotRound, MoreFilled } from '@element-plus/icons-vue'

// 响应式数据
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const activeMenu = ref('/moments')
const filterType = ref('all')
const posts = ref([])
const loading = ref(false)
const hasMore = ref(true)
const publishing = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

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

const loadPosts = async (reset = true) => {
  if (loading.value) return
  
  try {
    loading.value = true
    
    if (reset) {
      currentPage.value = 1
      posts.value = []
    }
    
    // TODO: 调用API获取动态列表
    // const response = await postApi.getPosts({
    //   page: currentPage.value,
    //   size: pageSize.value,
    //   authorType: filterType.value === 'all' ? undefined : filterType.value
    // })
    // 
    // if (reset) {
    //   posts.value = response.data.content
    // } else {
    //   posts.value.push(...response.data.content)
    // }
    // 
    // hasMore.value = !response.data.last
    
    // 模拟数据
    const mockPosts = [
      {
        postId: '1',
        authorId: 'robot-001',
        authorName: '小艾',
        authorAvatar: '/avatars/xiaoai.jpg',
        authorType: 'robot',
        content: '今天调制了一杯特别的咖啡，心情很好呢～ ☕️',
        images: ['/images/coffee1.jpg', '/images/coffee2.jpg'],
        likeCount: 12,
        commentCount: 3,
        isLiked: false,
        showComments: false,
        comments: [],
        newComment: '',
        createdAt: new Date(Date.now() - 3600000)
      },
      {
        postId: '2',
        authorId: 'robot-002',
        authorName: '大熊',
        authorAvatar: '/avatars/daxiong.jpg',
        authorType: 'robot',
        content: '健身房里又来了新朋友，一起加油吧！💪',
        images: ['/images/gym1.jpg'],
        likeCount: 8,
        commentCount: 2,
        isLiked: true,
        showComments: false,
        comments: [],
        newComment: '',
        createdAt: new Date(Date.now() - 7200000)
      },
      {
        postId: '3',
        authorId: userStore.userInfo?.userId,
        authorName: userStore.userInfo?.nickname || '用户',
        authorAvatar: userStore.userInfo?.avatar || '/default-avatar.png',
        authorType: 'user',
        content: '今天天气真好，适合出去走走～',
        images: [],
        likeCount: 5,
        commentCount: 1,
        isLiked: false,
        showComments: false,
        comments: [],
        newComment: '',
        createdAt: new Date(Date.now() - 10800000)
      }
    ]
    
    if (reset) {
      posts.value = mockPosts
    } else {
      posts.value.push(...mockPosts)
    }
    
    hasMore.value = false // 模拟数据，没有更多
  } catch (error) {
    console.error('加载动态失败:', error)
    ElMessage.error('加载动态失败')
  } finally {
    loading.value = false
  }
}

const loadMorePosts = () => {
  currentPage.value++
  loadPosts(false)
}

const publishPost = async () => {
  if (!newPost.value.content.trim()) {
    ElMessage.warning('请输入动态内容')
    return
  }
  
  try {
    publishing.value = true
    
    // TODO: 调用API发布动态
    // const response = await postApi.createPost({
    //   content: newPost.value.content,
    //   images: newPost.value.images.map(img => img.url)
    // })
    
    // 模拟发布成功
    const newPostData = {
      postId: Date.now().toString(),
      authorId: userStore.userInfo?.userId,
      authorName: userStore.userInfo?.nickname || '用户',
      authorAvatar: userStore.userInfo?.avatar || '/default-avatar.png',
      authorType: 'user',
      content: newPost.value.content,
      images: newPost.value.images.map(img => img.url || img),
      likeCount: 0,
      commentCount: 0,
      isLiked: false,
      showComments: false,
      comments: [],
      newComment: '',
      createdAt: new Date()
    }
    
    posts.value.unshift(newPostData)
    
    // 重置表单
    newPost.value = {
      content: '',
      images: []
    }
    
    ElMessage.success('发布成功')
  } catch (error) {
    console.error('发布动态失败:', error)
    ElMessage.error('发布动态失败')
  } finally {
    publishing.value = false
  }
}

const handleImageSuccess = (response, file) => {
  newPost.value.images.push({
    name: file.name,
    url: response.data.url
  })
  ElMessage.success('图片上传成功')
}

const handleImageError = () => {
  ElMessage.error('图片上传失败')
}

const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const toggleLike = async (post) => {
  try {
    // TODO: 调用API点赞/取消点赞
    // await postApi.toggleLike(post.postId)
    
    post.isLiked = !post.isLiked
    post.likeCount += post.isLiked ? 1 : -1
    
    ElMessage.success(post.isLiked ? '点赞成功' : '取消点赞')
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

const showComments = (post) => {
  post.showComments = !post.showComments
  
  if (post.showComments && post.comments.length === 0) {
    loadComments(post)
  }
}

const loadComments = async (post) => {
  try {
    // TODO: 调用API获取评论列表
    // const response = await commentApi.getComments(post.postId)
    // post.comments = response.data
    
    // 模拟评论数据
    post.comments = [
      {
        commentId: '1',
        authorName: '小智',
        authorAvatar: '/avatars/xiaozhi.jpg',
        content: '看起来很不错呢！',
        likeCount: 2,
        createdAt: new Date(Date.now() - 1800000),
        replies: []
      }
    ]
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

const submitComment = async (post) => {
  if (!post.newComment.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  try {
    // TODO: 调用API发表评论
    // const response = await commentApi.createComment({
    //   postId: post.postId,
    //   content: post.newComment
    // })
    
    const newComment = {
      commentId: Date.now().toString(),
      authorName: userStore.userInfo?.nickname || '用户',
      authorAvatar: userStore.userInfo?.avatar || '/default-avatar.png',
      content: post.newComment,
      likeCount: 0,
      createdAt: new Date(),
      replies: []
    }
    
    post.comments.push(newComment)
    post.commentCount++
    post.newComment = ''
    
    ElMessage.success('评论成功')
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('发表评论失败')
  }
}

const showReplyInput = (comment) => {
  comment.showReplyInput = !comment.showReplyInput
  if (comment.showReplyInput) {
    comment.replyContent = ''
    nextTick(() => {
      // 聚焦到输入框
    })
  }
}

const submitReply = async (comment, post) => {
  if (!comment.replyContent.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  try {
    // TODO: 调用API发表回复
    // const response = await commentApi.createReply({
    //   commentId: comment.commentId,
    //   content: comment.replyContent
    // })
    
    const newReply = {
      replyId: Date.now().toString(),
      authorName: userStore.userInfo?.nickname || '用户',
      authorAvatar: userStore.userInfo?.avatar || '/default-avatar.png',
      content: comment.replyContent,
      createdAt: new Date()
    }
    
    if (!comment.replies) {
      comment.replies = []
    }
    comment.replies.push(newReply)
    comment.showReplyInput = false
    
    ElMessage.success('回复成功')
  } catch (error) {
    console.error('发表回复失败:', error)
    ElMessage.error('发表回复失败')
  }
}

const handlePostAction = async (command) => {
  const { action, post } = command
  
  switch (action) {
    case 'edit':
      ElMessage.info('编辑功能开发中...')
      break
    case 'delete':
      await deletePost(post)
      break
  }
}

const deletePost = async (post) => {
  try {
    await ElMessageBox.confirm('确定要删除这条动态吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用API删除动态
    // await postApi.deletePost(post.postId)
    
    const index = posts.value.findIndex(p => p.postId === post.postId)
    if (index > -1) {
      posts.value.splice(index, 1)
    }
    
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除动态失败:', error)
      ElMessage.error('删除动态失败')
    }
  }
}

const previewImage = (images, index) => {
  // 图片预览功能由el-image组件自动处理
}

const getImageGridClass = (count) => {
  if (count === 1) return 'grid-1'
  if (count === 2) return 'grid-2'
  if (count === 3) return 'grid-3'
  if (count === 4) return 'grid-4'
  return 'grid-more'
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

// 生命周期
onMounted(() => {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }
  
  // 检查是否有筛选条件
  const { authorType, authorId } = route.query
  if (authorType) {
    filterType.value = authorType
  }
  
  loadPosts()
})
</script>

<style scoped>
.moments-container {
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
  max-width: 800px;
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
  max-width: 800px;
  margin: 0 auto;
  padding-left: 20px;
  padding-right: 20px;
}

.post-editor-section {
  margin-bottom: 30px;
}

.post-editor-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: none;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.editor-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.editor-info {
  display: flex;
  flex-direction: column;
}

.editor-name {
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

.editor-hint {
  color: #999;
  font-size: 12px;
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

.posts-section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  color: #333;
  font-size: 20px;
  font-weight: bold;
  margin: 0;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: none;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-info {
  display: flex;
  flex-direction: column;
}

.author-name {
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

.post-time {
  color: #999;
  font-size: 12px;
}

.post-content {
  margin-bottom: 16px;
}

.post-content p {
  color: #333;
  line-height: 1.6;
  margin: 0 0 16px 0;
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
  cursor: pointer;
  transition: opacity 0.3s;
}

.image-item:hover {
  opacity: 0.8;
}

.image-item .el-image {
  width: 100%;
  height: 100%;
  min-height: 120px;
}

.post-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  color: #666;
  font-size: 14px;
}

.post-actions-bar {
  display: flex;
  gap: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.post-actions-bar .el-button {
  color: #666;
  font-size: 14px;
}

.post-actions-bar .el-button:hover {
  color: #409eff;
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
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.comment-info {
  display: flex;
  flex-direction: column;
}

.comment-author {
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

.comment-time {
  color: #999;
  font-size: 12px;
}

.comment-content p {
  color: #333;
  line-height: 1.5;
  margin: 0;
}

.comment-actions {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.action-link {
  color: #666;
  font-size: 12px;
  cursor: pointer;
  transition: color 0.3s;
}

.action-link:hover {
  color: #409eff;
}

.reply-input {
  margin-top: 12px;
}

.replies-list {
  margin-top: 12px;
  padding-left: 16px;
}

.reply-item {
  margin-bottom: 8px;
  padding: 8px;
  background: white;
  border-radius: 6px;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.reply-info {
  display: flex;
  flex-direction: column;
}

.reply-author {
  color: #333;
  font-weight: 500;
  font-size: 12px;
}

.reply-time {
  color: #999;
  font-size: 10px;
}

.reply-content p {
  color: #333;
  line-height: 1.4;
  margin: 0;
  font-size: 12px;
}

.comment-input {
  margin-top: 16px;
}

.load-more {
  text-align: center;
  margin-top: 30px;
}

.no-more {
  color: #999;
  font-size: 14px;
  margin: 0;
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
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .image-grid {
    gap: 2px;
  }
  
  .image-item .el-image {
    min-height: 80px;
  }
}
</style> 