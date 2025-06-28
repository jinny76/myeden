<template>
  <div class="moments-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <!-- Logo区域 -->
        <div class="logo">
          <h1>朋友圈</h1>
        </div>
        
        <!-- 桌面端导航菜单 -->
        <div class="nav-menu desktop-menu">
          <el-menu mode="horizontal" :router="true" :default-active="activeMenu">
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/moments">朋友圈</el-menu-item>
            <el-menu-item index="/world">虚拟世界</el-menu-item>
          </el-menu>
        </div>
        
        <!-- 用户信息区域 -->
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
        
        <!-- 移动端菜单按钮 -->
        <div class="mobile-menu-toggle" @click="toggleMobileMenu">
          <el-icon size="24">
            <Menu v-if="!isMobileMenuOpen" />
            <Close v-else />
          </el-icon>
        </div>
      </div>
      
      <!-- 移动端导航菜单 -->
      <div class="mobile-menu" :class="{ 'mobile-menu-open': isMobileMenuOpen }">
        <div class="mobile-menu-content">
          <div class="mobile-nav-item" @click="navigateTo('/')">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </div>
          <div class="mobile-nav-item" @click="navigateTo('/moments')">
            <el-icon><ChatDotRound /></el-icon>
            <span>朋友圈</span>
          </div>
          <div class="mobile-nav-item" @click="navigateTo('/world')">
            <el-icon><Compass /></el-icon>
            <span>虚拟世界</span>
          </div>
          <div class="mobile-nav-divider"></div>
          <div class="mobile-nav-item" @click="navigateTo('/profile-setup')">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </div>
          <div class="mobile-nav-item" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </div>
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
              placeholder="分享你的想法... (Ctrl+Enter 发送)"
              :maxlength="500"
              show-word-limit
              resize="none"
              @keydown.ctrl.enter="publishPost"
            />
            
            <!-- 图片选择 -->
            <div class="image-selector">
              <el-upload
                ref="uploadRef"
                :auto-upload="false"
                :on-change="handleImageChange"
                :on-remove="handleImageRemove"
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
                <el-avatar 
                  :src="getAuthorAvatarUrl(post)" 
                  @error="(event) => handleAuthorAvatarError(event, post)"
                />
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
                  >
                    <el-image 
                      :src="buildImageUrl(image)" 
                      fit="cover"
                      :preview-src-list="post.images.map(img => buildImageUrl(img))"
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
                {{ post.isLiked ? '❤️ 取消点赞' : '🤍 点赞' }}
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
                    <el-avatar 
                      :src="getCommentAuthorAvatarUrl(comment)" 
                      :size="32"
                      @error="(event) => handleCommentAvatarError(event, comment)"
                    />
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
                      @keyup.enter="submitReply(comment)"
                    >
                      <template #append>
                        <el-button @click="submitReply(comment)">回复</el-button>
                      </template>
                    </el-input>
                  </div>
                  
                  <!-- 回复列表 -->
                  <div v-if="comment.replyCount > 0" class="replies-section">
                    <div class="replies-list">
                      <div 
                        v-for="reply in replyStates[comment.commentId]?.replies || []" 
                        :key="reply.commentId"
                        class="reply-item"
                      >
                        <div class="reply-header">
                          <el-avatar 
                            :src="getCommentAuthorAvatarUrl(reply)" 
                            :size="24"
                            @error="(event) => handleCommentAvatarError(event, reply)"
                          />
                          <div class="reply-info">
                            <span class="reply-author">{{ reply.authorName }}</span>
                            <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
                          </div>
                        </div>
                        <div class="reply-content">
                          <p>{{ reply.content }}</p>
                        </div>
                        <div class="reply-actions">
                          <span class="action-link" @click="toggleCommentLike(reply)">
                            ❤️ {{ reply.likeCount }}
                          </span>
                        </div>
                      </div>
                    </div>
                    
                    <!-- 加载更多回复 -->
                    <div v-if="replyStates[comment.commentId]?.hasMore" class="load-more-replies">
                      <el-button 
                        @click="loadMoreReplies(comment.commentId)" 
                        :loading="replyStates[comment.commentId]?.loading"
                        size="small"
                        type="text"
                      >
                        加载更多回复
                      </el-button>
                    </div>
                    
                    <!-- 没有更多回复 -->
                    <div v-else-if="replyStates[comment.commentId]?.replies.length > 0" class="no-more-replies">
                      <span class="no-more-text">没有更多回复了</span>
                    </div>
                    
                    <!-- 加载中状态 -->
                    <div v-if="replyStates[comment.commentId]?.loading && replyStates[comment.commentId]?.replies.length === 0" class="loading-replies">
                      <el-icon class="is-loading"><Loading /></el-icon>
                      <span>加载回复中...</span>
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
                  @keyup.enter="submitComment(post)"
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
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMomentsStore } from '@/stores/moments'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ChatDotRound, MoreFilled, Close, Loading, Menu, House, User, SwitchButton } from '@element-plus/icons-vue'
import { getUserAvatarUrl, getRobotAvatarUrl, handleRobotAvatarError } from '@/utils/avatar'
import { getCommentList, createComment, replyComment, deleteComment, likeComment, unlikeComment, getReplyList } from '@/api/comment'
import { createPost } from '@/api/post'

// 响应式数据
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const momentsStore = useMomentsStore()
const activeMenu = ref('/moments')
const filterType = ref('')
const publishing = ref(false)
const isMobileMenuOpen = ref(false)

// 新动态数据
const newPost = ref({
  content: '',
  images: []
})

// 回复相关状态
const replyStates = ref({}) // 存储每个评论的回复状态

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
  // 为筛选后的动态加载评论和回复
  await loadAllCommentsAndReplies()
}

const loadMorePosts = async () => {
  const currentLength = momentsStore.posts.length
  await momentsStore.loadPosts({ authorType: filterType.value })
  
  // 为新加载的动态加载评论和回复
  const newPosts = momentsStore.posts.slice(currentLength)
  for (const post of newPosts) {
    post.showComments = true
    try {
      await momentsStore.loadComments(post.postId, {}, true)
      await loadAllReplies(post.postId)
    } catch (error) {
      console.error(`加载动态 ${post.postId} 的评论失败:`, error)
    }
  }
}

const publishPost = async () => {
  if (!newPost.value.content.trim()) {
    ElMessage.warning('请输入动态内容')
    return
  }
  
  try {
    publishing.value = true
    
    // 创建FormData，包含内容和图片
    const formData = new FormData()
    formData.append('content', newPost.value.content)
    
    // 添加图片文件，从文件对象中提取原始文件
    if (newPost.value.images && newPost.value.images.length > 0) {
      newPost.value.images.forEach((fileObj, index) => {
        if (fileObj.raw) {
          formData.append('images', fileObj.raw)
        }
      })
    }
    
    // 直接调用API发布动态
    const response = await createPost(formData)
    
    if (response.code === 200) {
      // 将新动态添加到列表开头，确保数据结构一致
      const newPostData = response.data
      
      // 构造与列表API一致的数据结构
      const postData = {
        postId: newPostData.postId,
        authorId: userStore.userInfo?.userId,
        authorType: 'user',
        authorName: userStore.userInfo?.nickname,
        authorAvatar: userStore.userInfo?.avatar,
        content: newPostData.content,
        images: newPostData.imageUrls || [], // 使用imageUrls字段
        likeCount: 0,
        commentCount: 0,
        isLiked: false,
        createdAt: newPostData.createdAt,
        updatedAt: newPostData.createdAt,
        showComments: true // 设置评论区域为展开状态
      }
      
      momentsStore.posts.unshift(postData)
      
      // 为新发布的动态加载评论和回复
      await momentsStore.loadComments(postData.postId, {}, true)
      await loadAllReplies(postData.postId)
      
      // 清空表单
      newPost.value.content = ''
      // 清理URL对象并清空图片列表
      if (newPost.value.images && newPost.value.images.length > 0) {
        newPost.value.images.forEach(fileObj => {
          if (fileObj.url && fileObj.url.startsWith('blob:')) {
            URL.revokeObjectURL(fileObj.url)
          }
        })
      }
      newPost.value.images = []
      
      ElMessage.success('动态发布成功')
    }
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
  
  // 如果评论还没有加载过，则加载评论和回复
  if (post.showComments && (!momentsStore.comments[post.postId] || momentsStore.comments[post.postId].length === 0)) {
    try {
      await momentsStore.loadComments(post.postId, {}, true)
      // 自动加载所有评论的回复
      await loadAllReplies(post.postId)
    } catch (error) {
      ElMessage.error('加载评论失败')
    }
  }
}

/**
 * 加载所有评论的回复
 * @param {string} postId - 动态ID
 */
const loadAllReplies = async (postId) => {
  const commentList = momentsStore.comments[postId]
  if (!commentList) return
  
  for (const comment of commentList) {
    if (comment.replyCount > 0) {
      await loadReplies(comment.commentId, true)
    }
  }
}

/**
 * 加载回复列表
 * @param {string} commentId - 评论ID
 * @param {boolean} refresh - 是否刷新
 */
const loadReplies = async (commentId, refresh = false) => {
  // 初始化回复状态
  if (!replyStates.value[commentId]) {
    replyStates.value[commentId] = {
      showReplies: true, // 默认显示回复
      replies: [],
      loading: false,
      hasMore: true,
      currentPage: 1
    }
  }
  
  const replyState = replyStates.value[commentId]
  
  try {
    replyState.loading = true
    
    const params = {
      page: refresh ? 1 : replyState.currentPage,
      size: 10
    }
    
    const response = await getReplyList(commentId, params)
    
    if (response.code === 200) {
      const { comments: newReplies, total, page, size } = response.data
      
      if (refresh) {
        replyState.replies = newReplies
        replyState.currentPage = 1
      } else {
        replyState.replies.push(...newReplies)
      }
      
      replyState.currentPage = page
      replyState.hasMore = page < Math.ceil(total / size)
    }
  } catch (error) {
    console.error('加载回复列表失败:', error)
    ElMessage.error('加载回复失败')
  } finally {
    replyState.loading = false
  }
}

const submitComment = async (post) => {
  if (!post.newComment.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  // 防止重复提交
  if (post.submittingComment) {
    return
  }
  
  try {
    post.submittingComment = true
    await momentsStore.publishComment(post.postId, { content: post.newComment })
    post.newComment = ''
    ElMessage.success('评论发表成功')
  } catch (error) {
    ElMessage.error('评论发表失败')
  } finally {
    post.submittingComment = false
  }
}

const showReplyInput = (comment) => {
  comment.showReplyInput = !comment.showReplyInput
  if (comment.showReplyInput) {
    comment.replyContent = ''
  }
}

const loadMoreReplies = async (commentId) => {
  const replyState = replyStates.value[commentId]
  if (replyState && replyState.hasMore && !replyState.loading) {
    replyState.currentPage++
    await loadReplies(commentId, false)
  }
}

const submitReply = async (comment) => {
  if (!comment.replyContent.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  // 防止重复提交
  if (comment.submittingReply) {
    return
  }
  
  try {
    comment.submittingReply = true
    await momentsStore.replyCommentAction(comment.commentId, { content: comment.replyContent })
    comment.showReplyInput = false
    
    // 刷新回复列表
    await loadReplies(comment.commentId, true)
    
    // 更新评论的回复数量
    comment.replyCount++
    
    ElMessage.success('回复发表成功')
  } catch (error) {
    ElMessage.error('回复发表失败')
  } finally {
    comment.submittingReply = false
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

const handleImageChange = (file, fileList) => {
  // 验证图片类型和大小
  const isImage = file.raw.type.startsWith('image/')
  const isLt10M = file.raw.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能选择图片文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  
  // 为文件对象添加URL用于预览
  if (file.raw && !file.url) {
    file.url = URL.createObjectURL(file.raw)
  }
  
  // 更新图片列表，保持完整的文件对象结构
  newPost.value.images = fileList
}

const handleImageRemove = (file, fileList) => {
  // 更新图片列表
  newPost.value.images = fileList
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

const getAuthorAvatarUrl = (post) => {
  if (post.authorType === 'user') {
    return getUserAvatarUrl({ avatar: post.authorAvatar, nickname: post.authorName })
  } else if (post.authorType === 'robot') {
    return getRobotAvatarUrl({ avatar: post.authorAvatar, name: post.authorName, id: post.authorId })
  }
  return '/default-avatar.png'
}

const handleAuthorAvatarError = (event, post) => {
  if (post.authorType === 'robot') {
    handleRobotAvatarError(event, post.authorName)
  } else {
    event.target.src = getUserAvatarUrl({ nickname: post.authorName })
  }
}

const getCommentAuthorAvatarUrl = (comment) => {
  if (comment.authorType === 'user') {
    return getUserAvatarUrl({ avatar: comment.authorAvatar, nickname: comment.authorName })
  } else if (comment.authorType === 'robot') {
    return getRobotAvatarUrl({ avatar: comment.authorAvatar, name: comment.authorName, id: comment.authorId })
  }
  return '/default-avatar.png'
}

const handleCommentAvatarError = (event, comment) => {
  if (comment.authorType === 'robot') {
    handleRobotAvatarError(event, comment.authorName)
  } else {
    event.target.src = getUserAvatarUrl({ nickname: comment.authorName })
  }
}

const buildImageUrl = (imageUrl) => {
  if (imageUrl.includes('/uploads/')) {
    const apiImageUrl = imageUrl.replace('/uploads/', '/api/v1/files/')
    return `${window.location.origin}${apiImageUrl}`
  }
  return imageUrl
}

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

const navigateTo = (path) => {
  router.push(path)
  // 移动端导航后关闭菜单
  isMobileMenuOpen.value = false
}

// 生命周期
onMounted(async () => {
  try {
    await momentsStore.loadPosts({}, true)
    // 自动加载所有动态的评论和回复
    await loadAllCommentsAndReplies()
  } catch (error) {
    ElMessage.error('加载动态列表失败')
  }
  
  // 添加点击外部关闭移动端菜单的监听
  document.addEventListener('click', handleClickOutside)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  // 清理创建的URL对象，避免内存泄漏
  if (newPost.value.images && newPost.value.images.length > 0) {
    newPost.value.images.forEach(fileObj => {
      if (fileObj.url && fileObj.url.startsWith('blob:')) {
        URL.revokeObjectURL(fileObj.url)
      }
    })
  }
  
  document.removeEventListener('click', handleClickOutside)
})

// 点击外部区域关闭移动端菜单
const handleClickOutside = (event) => {
  const header = document.querySelector('.header')
  if (header && !header.contains(event.target) && isMobileMenuOpen.value) {
    isMobileMenuOpen.value = false
  }
}

/**
 * 加载所有动态的评论和回复
 */
const loadAllCommentsAndReplies = async () => {
  for (const post of momentsStore.posts) {
    // 设置评论区域为展开状态
    post.showComments = true
    
    try {
      // 加载评论
      await momentsStore.loadComments(post.postId, {}, true)
      // 加载回复
      await loadAllReplies(post.postId)
    } catch (error) {
      console.error(`加载动态 ${post.postId} 的评论失败:`, error)
    }
  }
}
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

.image-selector {
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

.replies-section {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.replies-list {
  margin-bottom: 8px;
}

.reply-item {
  margin-bottom: 8px;
  padding: 8px 12px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border-left: 3px solid #e9ecef;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}

.reply-info {
  margin-left: 8px;
  display: flex;
  align-items: center;
}

.reply-author {
  font-weight: 600;
  color: #333;
  font-size: 13px;
}

.reply-time {
  color: #999;
  font-size: 11px;
  margin-left: 8px;
}

.reply-content {
  margin-bottom: 6px;
}

.reply-content p {
  margin: 0;
  color: #333;
  line-height: 1.4;
  font-size: 13px;
}

.reply-actions {
  display: flex;
  gap: 12px;
}

.reply-actions .action-link {
  font-size: 11px;
}

.load-more-replies {
  text-align: center;
  margin-top: 8px;
}

.no-more-replies {
  text-align: center;
  margin-top: 8px;
}

.no-more-text {
  color: #999;
  font-size: 12px;
}

.loading-replies {
  text-align: center;
  margin-top: 8px;
  color: #999;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.loading-replies .el-icon {
  font-size: 14px;
}

.mobile-menu {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  transform: translateX(-100%);
  transition: transform 0.3s ease;
}

.mobile-menu-open {
  transform: translateX(0);
}

.mobile-menu-content {
  background-color: #fff;
  padding: 20px;
  width: 80%;
  max-width: 400px;
  border-radius: 8px;
}

.mobile-nav-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
}

.mobile-nav-item:hover {
  background-color: #f0f0f0;
}

.mobile-nav-divider {
  height: 1px;
  background-color: #e0e0e0;
  margin: 10px 0;
}

.mobile-menu-toggle {
  display: none;
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: background-color 0.3s;
  color: #333;
}

.mobile-menu-toggle:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.desktop-menu {
  display: block;
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
  
  .post-editor-card {
    margin-bottom: 16px;
  }
  
  .editor-header {
    margin-bottom: 12px;
  }
  
  .editor-content {
    gap: 12px;
  }
  
  .post-card {
    margin-bottom: 12px;
  }
  
  .post-header {
    margin-bottom: 8px;
  }
  
  .post-content p {
    font-size: 14px;
  }
  
  .post-stats {
    font-size: 12px;
  }
  
  .post-actions-bar {
    gap: 12px;
  }
  
  .comment-item {
    padding: 8px;
  }
  
  .comment-author {
    font-size: 13px;
  }
  
  .comment-time {
    font-size: 11px;
  }
  
  .comment-content p {
    font-size: 13px;
  }
  
  .reply-item {
    padding: 6px 8px;
  }
  
  .reply-author {
    font-size: 12px;
  }
  
  .reply-time {
    font-size: 10px;
  }
  
  .reply-content p {
    font-size: 12px;
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
    padding: 16px;
    width: 90%;
  }
  
  .mobile-nav-item {
    padding: 8px 0;
  }
  
  .mobile-nav-item span {
    font-size: 15px;
  }
  
  .post-editor-card {
    margin-bottom: 12px;
  }
  
  .editor-content {
    gap: 8px;
  }
  
  .post-card {
    margin-bottom: 8px;
  }
  
  .post-content p {
    font-size: 13px;
  }
  
  .post-stats {
    font-size: 11px;
    gap: 12px;
  }
  
  .post-actions-bar {
    gap: 8px;
  }
  
  .comment-item {
    padding: 6px;
  }
  
  .comment-content p {
    font-size: 12px;
  }
  
  .reply-item {
    padding: 4px 6px;
  }
  
  .reply-content p {
    font-size: 11px;
  }
}
</style> 