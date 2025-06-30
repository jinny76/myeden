<template>
  <AppHeader />
  <div class="moments-container">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="floating-orb orb-1"></div>
      <div class="floating-orb orb-2"></div>
      <div class="floating-orb orb-3"></div>
      <div class="gradient-overlay"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">动态广场</h1>
        <p class="page-subtitle">分享你的想法，与朋友和天使互动</p>
      </div>

      <!-- 动态发布区域 -->
      <div class="post-editor-section">
        <div class="post-editor-card">
          <div class="editor-header">
            <el-avatar :src="getUserAvatarUrl({ avatar: userStore.userInfo?.avatar, nickname: userStore.userInfo?.nickname })" />
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
              :maxlength="5000"
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
              <el-button type="primary" @click="publishPost" :loading="publishing" class="publish-button">
                <el-icon><Plus /></el-icon>
                发布动态
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 移动端浮动发布按钮 -->
      <div class="mobile-fab-container">
        <div class="mobile-fab" @click="showMobileEditor = true">
          <el-icon><Plus /></el-icon>
        </div>
      </div>

      <!-- 移动端发布弹窗 -->
      <el-dialog
        v-model="showMobileEditor"
        title="发布动态"
        width="90%"
        :close-on-click-modal="false"
        class="mobile-editor-dialog"
      >
        <div class="mobile-editor-content">
          <div class="mobile-editor-header">
            <el-avatar :src="getUserAvatarUrl({ avatar: userStore.userInfo?.avatar, nickname: userStore.userInfo?.nickname })" />
            <div class="mobile-editor-info">
              <span class="mobile-editor-name">{{ userStore.userInfo?.nickname || '用户' }}</span>
              <span class="mobile-editor-hint">分享你的想法...</span>
            </div>
          </div>
          
          <div class="mobile-editor-body">
            <el-input
              v-model="newPost.content"
              type="textarea"
              :rows="6"
              placeholder="分享你的想法..."
              :maxlength="5000"
              show-word-limit
              resize="none"
            />
            
            <!-- 移动端图片选择 -->
            <div class="mobile-image-selector">
              <el-upload
                ref="mobileUploadRef"
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
          </div>
        </div>
        
        <template #footer>
          <div class="mobile-editor-footer">
            <el-button @click="showMobileEditor = false">取消</el-button>
            <el-button type="primary" @click="publishPostMobile" :loading="publishing">
              <el-icon><Plus /></el-icon>
              发布动态
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 搜索和筛选区域 -->
      <div class="search-filter-section">
        <div class="search-container">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索动态内容或发帖人..."
            clearable
            @input="handleSearchInput"
            @clear="handleSearchClear"
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-select 
            v-model="searchType" 
            placeholder="搜索类型" 
            @change="handleSearchTypeChange"
            class="search-type-select"
          >
            <el-option label="全部" value="all" />
            <el-option label="内容" value="content" />
            <el-option label="发帖人" value="author" />
          </el-select>
          
          <el-select v-model="filterType" placeholder="筛选类型" @change="handleFilterChange" class="filter-select">
            <el-option label="全部" value="" />
            <el-option label="用户动态" value="user" />
            <el-option label="机器人动态" value="robot" />
          </el-select>
        </div>
        
        <!-- 搜索结果提示 -->
        <div v-if="searchKeyword && !momentsStore.loading" class="search-result-info">
          <el-tag type="info" closable @close="clearSearch">
            搜索"{{ searchKeyword }}"的结果 ({{ momentsStore.posts.length }} 条)
          </el-tag>
        </div>
      </div>

      <!-- 动态列表 -->
      <div class="posts-section">
        <div class="posts-list">
          <div 
            v-for="post in momentsStore.posts" 
            :key="post.postId" 
            class="post-card"
            :data-post-id="post.postId"
          >
            <div class="post-card-content">
              <!-- 动态头部 -->
              <div class="post-header">
                <div class="post-author">
                  <el-avatar 
                    :src="getAuthorAvatarUrl(post)" 
                    @error="(event) => handleAuthorAvatarError(event, post)"
                    class="author-avatar"
                  />
                  <div class="author-info">
                    <span class="author-name">{{ post.authorName }}</span>
                    <span class="post-time">{{ formatTime(post.createdAt) }}</span>
                  </div>
                </div>
                <div class="post-type-badge" :class="post.authorType">
                  {{ post.authorType === 'robot' ? '天使' : '用户' }}
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
              
              <!-- 动态统计（点赞/评论） -->
              <div class="post-stats">
                <span class="stat-item like-stat" @click="toggleLike(post)">
                  <el-icon :class="{ 'liked': post.isLiked }"><Star /></el-icon>
                  <span>{{ post.likeCount }}</span>
                </span>
                <span class="stat-item">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>{{ post.commentCount }}</span>
                </span>
                <!-- 添加查看内心活动按钮 -->
                <span v-if="post.innerThoughts" class="stat-item inner-thoughts-stat" @click="showInnerThoughts(post)">
                  <el-icon><View /></el-icon>
                </span>
              </div>
              
              <!-- 评论区域 -->
              <div class="comments-section" @click.stop>
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
                        <el-icon><Star /></el-icon>
                        {{ comment.likeCount }}
                      </span>
                      <!-- 添加查看内心活动按钮 -->
                      <span v-if="comment.innerThoughts" class="action-link" @click="showInnerThoughts(comment)">
                        <el-icon><View /></el-icon>
                      </span>
                    </div>
                    
                    <!-- 回复输入框 -->
                    <div v-if="comment.showReplyInput" class="reply-input">
                      <el-input
                        v-model="comment.replyContent"
                        placeholder="回复评论..."
                        :maxlength="2000"
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
                              <el-icon><Star /></el-icon>
                              {{ reply.likeCount }}
                            </span>
                            <!-- 添加查看内心活动按钮 -->
                            <span v-if="reply.innerThoughts" class="action-link" @click="showInnerThoughts(reply)">
                              <el-icon><View /></el-icon>
                            </span>
                          </div>
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
                
                <!-- 评论输入框 -->
                <div class="comment-input">
                  <el-input
                    v-model="post.newComment"
                    placeholder="写下你的评论..."
                    :maxlength="2000"
                    show-word-limit
                    @keyup.enter="submitComment(post)"
                  >
                    <template #append>
                      <el-button @click="submitComment(post)">发送</el-button>
                    </template>
                  </el-input>
                </div>
              </div>
            </div>
            <div class="post-card-bg"></div>
          </div>
        </div>
        
        <!-- 滚动加载指示器 -->
        <div v-if="isLoadingMore" class="scroll-loading-indicator">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>正在加载更多动态...</span>
        </div>
        
        <!-- 没有更多内容提示 -->
        <div v-else-if="!momentsStore.hasMore && momentsStore.posts.length > 0" class="no-more-content">
          <span>没有更多动态了 (共 {{ momentsStore.posts.length }} 条)</span>
        </div>
        
        <!-- 空状态提示 -->
        <div v-else-if="momentsStore.posts.length === 0 && !momentsStore.loading" class="empty-state">
          <el-empty description="暂无动态" />
        </div>
      </div>
    </div>

    <!-- 下拉刷新指示器 -->
    <div class="refresh-indicator" :class="{ show: isRefreshing || isPulling }">
      <el-icon v-if="isRefreshing"><Loading /></el-icon>
      <svg v-else width="20" height="20" viewBox="0 0 50 50" class="refresh-svg">
        <circle cx="25" cy="25" r="20" fill="none" stroke="#fff" stroke-width="4" stroke-dasharray="90" stroke-dashoffset="30">
          <animateTransform attributeName="transform" type="rotate" from="0 25 25" to="360 25 25" dur="1s" repeatCount="indefinite"/>
        </circle>
      </svg>
      <span>
        {{ isRefreshing ? '正在刷新...' : (isPulling ? '下拉刷新' : '') }}
      </span>
    </div>

    <!-- 内心活动弹窗 -->
    <el-dialog
      v-model="showInnerThoughtsDialog"
      :title="currentThoughtsItem?.authorType === 'robot' ? '天使内心活动' : '内心活动'"
      width="90%"
      :close-on-click-modal="true"
      class="inner-thoughts-dialog"
    >
      <div class="inner-thoughts-content" @click="showInnerThoughtsDialog = false" style="cursor:pointer">
        <div class="thoughts-header">
          <el-avatar 
            :src="currentThoughtsItem ? (currentThoughtsItem.postId ? getAuthorAvatarUrl(currentThoughtsItem) : getCommentAuthorAvatarUrl(currentThoughtsItem)) : ''" 
            :size="40"
          />
          <div class="thoughts-info">
            <span class="thoughts-author">{{ currentThoughtsItem?.authorName }}</span>
            <span class="thoughts-time">{{ currentThoughtsItem ? formatTime(currentThoughtsItem.createdAt) : '' }}</span>
            <span class="thoughts-type">
              {{ currentThoughtsItem?.authorType === 'robot' ? '天使' : '用户' }} · 
              {{ currentThoughtsItem?.postId ? '动态' : '评论' }}
            </span>
          </div>
        </div>
        <div class="thoughts-body">
          <div class="thoughts-content">
            <h4>内心想法：</h4>
            <p>{{ currentThoughtsItem?.innerThoughts }}</p>
          </div>
          <div class="thoughts-original">
            <h4>实际表达：</h4>
            <p>{{ currentThoughtsItem?.content }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMomentsStore } from '@/stores/moments'
import { ElMessageBox, ElPopover } from 'element-plus'
import { message } from '@/utils/message'
import { Plus, ChatDotRound, MoreFilled, Close, Loading, Menu, House, User, SwitchButton, Search, Star, View } from '@element-plus/icons-vue'
import { getUserAvatarUrl, getRobotAvatarUrl, handleRobotAvatarError } from '@/utils/avatar'
import { getCommentList, createComment, replyComment, deleteComment, likeComment, unlikeComment, getReplyList } from '@/api/comment'
import { createPost, searchPosts } from '@/api/post'

// 响应式数据
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const momentsStore = useMomentsStore()
const activeMenu = ref('/moments')
const filterType = ref('')
const publishing = ref(false)
const isMobileMenuOpen = ref(false)

// 移动端发布相关
const showMobileEditor = ref(false)

// 滚动加载相关状态
const isLoadingMore = ref(false)
const scrollThreshold = 100 // 距离底部多少像素时触发加载

// 下拉刷新相关状态
const isRefreshing = ref(false)
const refreshThreshold = 80 // 下拉多少像素触发刷新
const startY = ref(0)
const currentY = ref(0)
const isPulling = ref(false)

// 新动态数据
const newPost = ref({
  content: '',
  images: []
})

// 回复相关状态
const replyStates = ref({}) // 存储每个评论的回复状态

// 搜索相关状态
const searchKeyword = ref('')
const searchType = ref('all')
const searchTimeout = ref(null)
const isSearching = ref(false)

// 内心活动相关状态
const showInnerThoughtsDialog = ref(false)
const currentThoughtsItem = ref(null)

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 方法
const handleUserCommand = async (command) => {
  switch (command) {
    case 'profile-setup':
      router.push('/profile-setup')
      break
    case 'settings':
      message.info('设置功能开发中...')
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
    message.success('退出登录成功')
    router.push('/login')
  } catch (error) {
    if (error !== 'cancel') {
      message.error('退出登录失败')
    }
  }
}

const handleFilterChange = async () => {
  // 如果有搜索关键字，优先使用搜索
  if (searchKeyword.value.trim()) {
    await performSearch()
  } else {
    await momentsStore.loadPosts({ authorType: filterType.value }, true)
    // 为筛选后的动态加载评论和回复
    await loadAllCommentsAndReplies()
  }
}

/**
 * 下拉刷新处理函数
 */
const handleTouchStart = (event) => {
  // 检查是否在主内容区域，且不是按钮或其他交互元素
  const target = event.target
  const mainContent = document.querySelector('.main-content')
  
  // 检查目标元素是否是可交互元素
  const isInteractiveElement = target.closest('button, input, select, textarea, a, [role="button"]')
  
  // 只在页面顶部、在主内容区域、且不是交互元素时启用下拉刷新
  if (window.pageYOffset === 0 && 
      mainContent && 
      mainContent.contains(target) && 
      !isInteractiveElement) {
    startY.value = event.touches[0].clientY
    isPulling.value = true
  }
}

const handleTouchMove = (event) => {
  if (!isPulling.value || isRefreshing.value) return
  
  currentY.value = event.touches[0].clientY
  const deltaY = currentY.value - startY.value
  
  // 只处理向下滑动，且距离足够大才阻止默认行为
  if (deltaY > 10 && window.pageYOffset === 0) {
    // 阻止默认滚动行为
    event.preventDefault()
    
    // 添加下拉效果
    const pullDistance = Math.min(deltaY * 0.5, refreshThreshold)
    document.body.style.transform = `translateY(${pullDistance}px)`
  }
}

const handleTouchEnd = async (event) => {
  if (!isPulling.value) return
  
  const deltaY = currentY.value - startY.value
  
  // 重置下拉效果
  document.body.style.transform = ''
  isPulling.value = false
  
  // 如果下拉距离足够，触发刷新
  if (deltaY > refreshThreshold && window.pageYOffset === 0) {
    await performRefresh()
  }
}

/**
 * 执行刷新操作
 */
const performRefresh = async () => {
  if (isRefreshing.value) return
  try {
    isRefreshing.value = true
    // 刷新数据
    await momentsStore.loadPosts({}, true)
    await loadAllCommentsAndReplies()
    // 清除搜索和筛选状态
    if (searchKeyword.value.trim()) {
      searchKeyword.value = ''
      searchType.value = 'all'
    }
    filterType.value = ''
    // 不再弹出任何 message
  } catch (error) {
    // 不弹窗，仅可选地在控制台输出
    console.error('刷新失败:', error)
  } finally {
    isRefreshing.value = false
  }
}

/**
 * 滚动事件处理函数
 * 监听页面滚动，当接近底部时自动加载更多内容
 */
const handleScroll = async () => {
  // 如果正在加载或没有更多数据，则不处理
  if (isLoadingMore.value || !momentsStore.hasMore) {
    return
  }
  
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight
  
  // 当滚动到距离底部指定像素时触发加载
  if (scrollTop + windowHeight >= documentHeight - scrollThreshold) {
    await loadMorePosts()
  }
}

/**
 * 节流函数，限制滚动事件的触发频率
 * @param {Function} func - 要节流的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 节流后的函数
 */
const throttle = (func, delay) => {
  let timeoutId
  let lastExecTime = 0
  return function (...args) {
    const currentTime = Date.now()
    
    if (currentTime - lastExecTime > delay) {
      func.apply(this, args)
      lastExecTime = currentTime
    } else {
      clearTimeout(timeoutId)
      timeoutId = setTimeout(() => {
        func.apply(this, args)
        lastExecTime = Date.now()
      }, delay - (currentTime - lastExecTime))
    }
  }
}

// 创建节流后的滚动处理函数
const throttledHandleScroll = throttle(handleScroll, 200)

const loadMorePosts = async () => {
  // 防止重复加载
  if (isLoadingMore.value || !momentsStore.hasMore) {
    return
  }
  
  try {
    isLoadingMore.value = true
    const currentLength = momentsStore.posts.length
    console.log(`开始加载更多动态，当前动态数量: ${currentLength}`)
    
    // 调用store的loadPosts方法，它会自动处理排重
    await momentsStore.loadPosts({ authorType: filterType.value })
    
    // 获取新加载的动态（排重后的）
    const newPosts = momentsStore.posts.slice(currentLength)
    console.log(`加载完成，新增动态数量: ${newPosts.length}`)
    
    // 为新加载的动态加载评论和回复
    for (const post of newPosts) {
      post.showComments = true
      try {
        await momentsStore.loadComments(post.postId, {}, true)
        await loadAllReplies(post.postId)
      } catch (error) {
        console.error(`加载动态 ${post.postId} 的评论失败:`, error)
      }
    }
    
    // 如果没有加载到新内容，但hasMore仍然为true，可能是后端数据问题
    if (newPosts.length === 0 && momentsStore.hasMore) {
      console.warn('滚动加载未获取到新内容，可能存在数据重复或分页问题')
    }
  } catch (error) {
    console.error('加载更多动态失败:', error)
    message.error('加载更多动态失败')
  } finally {
    isLoadingMore.value = false
  }
}

const publishPost = async () => {
  if (!newPost.value.content.trim()) {
    message.warning('请输入动态内容')
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
      
      message.success('动态发布成功')
    }
  } catch (error) {
    message.error('动态发布失败')
  } finally {
    publishing.value = false
  }
}

/**
 * 移动端发布动态
 */
const publishPostMobile = async () => {
  if (!newPost.value.content.trim()) {
    message.warning('请输入动态内容')
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
      
      // 关闭移动端编辑器
      showMobileEditor.value = false
      
      message.success('动态发布成功')
    }
  } catch (error) {
    message.error('动态发布失败')
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
      message.success('动态删除成功')
    } catch (error) {
      if (error !== 'cancel') {
        message.error('动态删除失败')
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
    message.error('操作失败')
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
      message.error('加载评论失败')
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
    message.error('加载回复失败')
  } finally {
    replyState.loading = false
  }
}

const submitComment = async (post) => {
  if (!post.newComment.trim()) {
    message.warning('请输入评论内容')
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
    message.success('评论发表成功')
  } catch (error) {
    message.error('评论发表失败')
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
    message.warning('请输入回复内容')
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
    
    message.success('回复发表成功')
  } catch (error) {
    message.error('回复发表失败')
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
    message.error('操作失败')
  }
}

const handleImageChange = (file, fileList) => {
  // 验证图片类型和大小
  const isImage = file.raw.type.startsWith('image/')
  const isLt10M = file.raw.size / 1024 / 1024 < 10

  if (!isImage) {
    message.error('只能选择图片文件')
    return false
  }
  if (!isLt10M) {
    message.error('图片大小不能超过 10MB')
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

/**
 * 滚动定位到指定的分享
 * @param {string} postId - 分享ID
 */
const scrollToPost = async (postId) => {
  try {
    // 等待DOM更新
    await nextTick()
    
    // 查找对应的分享元素
    const postElement = document.querySelector(`[data-post-id="${postId}"]`)
    
    if (postElement) {
      // 滚动到分享位置
      postElement.scrollIntoView({ 
        behavior: 'smooth', 
        block: 'center' 
      })
      
      // 添加高亮效果
      postElement.classList.add('highlight-post')
      
      // 3秒后移除高亮效果
      setTimeout(() => {
        postElement.classList.remove('highlight-post')
      }, 3000)
      
      // 清除URL参数
      //router.replace({ path: '/moments', query: {} })
    } else {
      // 如果分享不在当前页面，尝试加载更多内容
      console.log(`分享 ${postId} 不在当前页面，尝试加载更多内容`)
      
      // 这里可以添加逻辑来加载更多内容直到找到目标分享
      // 暂时显示提示信息
      message.info('该分享可能已被删除或不在当前页面')
    }
  } catch (error) {
    console.error('定位分享失败:', error)
  }
}

const goToPostDetail = (post) => {
  // 明细页已被移除，此函数不再需要
  console.log('明细页功能已被移除')
}

// 生命周期
onMounted(async () => {
  try {
    await momentsStore.loadPosts({}, true)
    // 自动加载所有动态的评论和回复
    await loadAllCommentsAndReplies()
    
    // 检查URL参数，如果有postId则定位到对应分享
    if (route.query.postId) {
      await scrollToPost(route.query.postId)
    }
  } catch (error) {
    console.error('加载动态列表失败:', error)
    message.error('加载动态列表失败')
  }
  
  // 添加滚动事件监听器
  window.addEventListener('scroll', throttledHandleScroll, { passive: true })
  
  // 只在移动端添加触摸事件监听器（下拉刷新）
  const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
  if (isMobile) {
    document.addEventListener('touchstart', handleTouchStart, { passive: true })
    document.addEventListener('touchmove', handleTouchMove, { passive: false })
    document.addEventListener('touchend', handleTouchEnd, { passive: true })
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
  
  // 移除滚动事件监听器
  window.removeEventListener('scroll', throttledHandleScroll)
  
  // 只在移动端移除触摸事件监听器
  const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
  if (isMobile) {
    document.removeEventListener('touchstart', handleTouchStart)
    document.removeEventListener('touchmove', handleTouchMove)
    document.removeEventListener('touchend', handleTouchEnd)
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

// 搜索相关方法
/**
 * 处理搜索输入，实现防抖搜索
 */
const handleSearchInput = () => {
  // 清除之前的定时器
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
  
  // 如果搜索关键字为空，恢复显示所有动态
  if (!searchKeyword.value.trim()) {
    clearSearch()
    return
  }
  
  // 设置防抖延迟，500ms后执行搜索
  searchTimeout.value = setTimeout(() => {
    performSearch()
  }, 500)
}

/**
 * 处理搜索输入框清除
 */
const handleSearchClear = () => {
  clearSearch()
}

/**
 * 处理搜索类型变化
 */
const handleSearchTypeChange = async () => {
  if (searchKeyword.value.trim()) {
    await performSearch()
  }
}

/**
 * 执行搜索
 */
const performSearch = async () => {
  if (!searchKeyword.value.trim()) {
    return
  }
  
  try {
    isSearching.value = true
    
    const params = {
      keyword: searchKeyword.value.trim(),
      searchType: searchType.value,
      page: 1,
      size: 10
    }
    
    // 调用搜索API
    const response = await searchPosts(params)
    
    if (response.code === 200) {
      const { posts, total } = response.data
      
      // 清空现有动态列表
      momentsStore.posts = []
      
      // 添加搜索结果
      if (posts && posts.length > 0) {
        momentsStore.posts = posts.map(post => ({
          ...post,
          showComments: true // 设置评论区域为展开状态
        }))
        
        // 为搜索结果的动态加载评论和回复
        await loadAllCommentsAndReplies()
        
        message.success(`找到 ${total} 条相关动态`)
      } else {
        message.info('没有找到相关动态')
      }
      
      // 更新hasMore状态
      momentsStore.hasMore = total > posts.length
    }
  } catch (error) {
    console.error('搜索失败:', error)
    message.error('搜索失败，请重试')
  } finally {
    isSearching.value = false
  }
}

/**
 * 清除搜索
 */
const clearSearch = async () => {
  searchKeyword.value = ''
  searchType.value = 'all'
  
  // 清除定时器
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
    searchTimeout.value = null
  }
  
  // 恢复显示所有动态
  try {
    await momentsStore.loadPosts({ authorType: filterType.value }, true)
    await loadAllCommentsAndReplies()
  } catch (error) {
    message.error('恢复动态列表失败')
  }
}

// 内心活动相关方法
const showInnerThoughts = (item) => {
  currentThoughtsItem.value = item
  showInnerThoughtsDialog.value = true
}
</script>

<style scoped>
.moments-container {
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
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
  padding-top: 80px;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #22d36b, #4ade80, #86efac);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12px;
}

.page-subtitle {
  font-size: 1.1rem;
  color: var(--color-text);
  opacity: 0.8;
}

/* 动态发布区域 */
.post-editor-section {
  margin-bottom: 40px;
}

.post-editor-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.post-editor-card:hover {
  border-color: rgba(34, 211, 107, 0.2);
  box-shadow: 0 8px 32px rgba(34, 211, 107, 0.1);
}

.editor-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.editor-info {
  margin-left: 16px;
}

.editor-name {
  font-weight: 600;
  color: var(--color-text);
  display: block;
  font-size: 1.1rem;
}

.editor-hint {
  color: var(--color-text);
  font-size: 0.9rem;
  opacity: 0.7;
}

.editor-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.publish-button {
  background: linear-gradient(135deg, #22d36b, #4ade80);
  border: none;
  padding: 12px 24px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.publish-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(34, 211, 107, 0.3);
}

/* 搜索和筛选区域 */
.search-filter-section {
  margin-bottom: 30px;
}

.search-container {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.search-input {
  flex: 1;
  min-width: 200px;
}

.search-type-select,
.filter-select {
  width: 120px;
}

.search-result-info {
  margin-bottom: 16px;
}

.search-result-info .el-tag {
  cursor: pointer;
  background: rgba(34, 211, 107, 0.1);
  border-color: rgba(34, 211, 107, 0.2);
  color: #22d36b;
}

/* 动态列表 */
.posts-section {
  margin-bottom: 40px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
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
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.post-author {
  display: flex;
  align-items: center;
}

.author-avatar {
  width: 48px;
  height: 48px;
}

.author-info {
  margin-left: 12px;
}

.author-name {
  font-weight: 600;
  color: var(--color-text);
  display: block;
  font-size: 1rem;
}

.post-time {
  color: var(--color-text);
  font-size: 0.8rem;
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
  margin: 0 0 16px 0;
  line-height: 1.6;
  color: var(--color-text);
  font-size: 0.95rem;
}

.post-images {
  margin-top: 16px;
}

.image-grid {
  display: grid;
  gap: 8px;
  border-radius: 12px;
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
  border-radius: 8px;
  overflow: hidden;
}

.image-item .el-image {
  width: 100%;
  height: 100%;
}

.post-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
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
  color: #22d36b;
}

.post-actions-bar {
  display: flex;
  flex-direction: row;
  gap: 8px;
  justify-content: flex-start;
}

.like-button {
  justify-content: center;
  padding: 10px 12px;
  min-width: 0;
}

.like-button .el-icon {
  font-size: 14px;
}

.like-text {
  font-size: 0.8rem;
}

.comments-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.comments-list {
  margin-bottom: 20px;
}

.comment-item {
  margin-bottom: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.comment-info {
  margin-left: 12px;
}

.comment-author {
  font-weight: 600;
  color: var(--color-text);
  font-size: 0.9rem;
}

.comment-time {
  color: var(--color-text);
  font-size: 0.75rem;
  margin-left: 8px;
  opacity: 0.6;
}

.comment-content {
  margin-bottom: 12px;
}

.comment-content p {
  margin: 0;
  color: var(--color-text);
  line-height: 1.5;
  font-size: 0.9rem;
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.action-link {
  color: #22d36b;
  cursor: pointer;
  font-size: 0.8rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
}

.action-link:hover {
  opacity: 0.8;
  transform: translateY(-1px);
}

.action-link .el-icon {
  font-size: 14px;
}

.reply-input {
  margin-top: 12px;
}

.comment-input {
  margin-top: 16px;
}

.replies-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.replies-list {
  margin-bottom: 12px;
}

.reply-item {
  margin-bottom: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 8px;
  border-left: 3px solid rgba(34, 211, 107, 0.2);
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.reply-info {
  margin-left: 8px;
  display: flex;
  align-items: center;
}

.reply-author {
  font-weight: 600;
  color: var(--color-text);
  font-size: 0.8rem;
}

.reply-time {
  color: var(--color-text);
  font-size: 0.7rem;
  margin-left: 8px;
  opacity: 0.6;
}

.reply-content {
  margin-bottom: 8px;
}

.reply-content p {
  margin: 0;
  color: var(--color-text);
  line-height: 1.4;
  font-size: 0.8rem;
}

.reply-actions {
  display: flex;
  gap: 12px;
}

.reply-actions .action-link {
  font-size: 0.75rem;
}

.load-more-replies {
  text-align: center;
  margin-top: 12px;
}

.no-more-replies {
  text-align: center;
  margin-top: 12px;
}

.no-more-text {
  color: var(--color-text);
  font-size: 0.8rem;
  opacity: 0.6;
}

.loading-replies {
  text-align: center;
  margin-top: 12px;
  color: var(--color-text);
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.loading-replies .el-icon {
  font-size: 14px;
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

/* 滚动加载指示器样式 */
.scroll-loading-indicator {
  text-align: center;
  margin: 20px 0;
  padding: 16px;
  color: var(--color-text);
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.scroll-loading-indicator .el-icon {
  font-size: 16px;
  color: #22d36b;
}

/* 没有更多内容提示样式 */
.no-more-content {
  text-align: center;
  margin: 20px 0;
  padding: 16px;
  color: var(--color-text);
  font-size: 0.9rem;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  margin: 40px 0;
  padding: 40px 20px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  /* 隐藏桌面端发布区域 */
  .post-editor-section {
    display: none;
  }
  
  /* 显示移动端浮动按钮 */
  .mobile-fab-container {
    display: block;
  }
  
  .main-content {
    padding: 0 16px;
  }
  
  .page-header {
    padding-top: 16px;
    margin-bottom: 30px;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .page-subtitle {
    font-size: 1rem;
  }
  
  .search-container {
    flex-direction: column;
    gap: 8px;
  }
  
  .search-input,
  .search-type-select,
  .filter-select {
    width: 100%;
  }
  
  .post-card {
    padding: 20px;
  }
  
  .post-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .post-type-badge {
    align-self: flex-end;
  }
  
  .post-actions-bar {
    flex-direction: row;
    gap: 8px;
    justify-content: space-around;
  }
  
  .like-button {
    flex: 1;
    justify-content: center;
    padding: 10px 8px;
    min-width: 0;
  }
  
  .like-button .el-icon {
    font-size: 14px;
  }
  
  .like-text {
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 0 12px;
  }
  
  .page-header {
    padding-top: 16px;
    margin-bottom: 24px;
  }
  
  .page-title {
    font-size: 1.8rem;
  }
  
  .page-subtitle {
    font-size: 0.9rem;
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
  
  .post-actions-bar {
    gap: 6px;
  }
  
  .like-button {
    padding: 8px 6px;
    gap: 4px;
  }
  
  .like-button .el-icon {
    font-size: 13px;
  }
  
  .like-text {
    font-size: 0.75rem;
  }
  
  .comment-item {
    padding: 12px;
  }
  
  .reply-item {
    padding: 8px;
  }
  
  /* 移动端浮动按钮位置调整 */
  .mobile-fab-container {
    bottom: 70px;
    right: 16px;
  }
  
  .mobile-fab {
    width: 52px;
    height: 52px;
    font-size: 22px;
  }
  
  /* 移动端弹窗样式调整 */
  .mobile-editor-dialog :deep(.el-dialog) {
    margin: 10px;
    width: calc(100% - 20px) !important;
  }
  
  .mobile-editor-content {
    padding: 16px;
  }
  
  .mobile-editor-header {
    margin-bottom: 16px;
    padding-bottom: 12px;
  }
  
  .mobile-editor-footer {
    padding: 16px;
  }
  
  .mobile-editor-footer .el-button {
    padding: 10px 16px;
    font-size: 0.9rem;
  }
}

/* 移动端浮动按钮 */
.mobile-fab-container {
  display: none;
  position: fixed;
  bottom: 80px;
  right: 20px;
  z-index: 1000;
}

.mobile-fab {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #22d36b, #4ade80);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  box-shadow: 0 8px 25px rgba(34, 211, 107, 0.3);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
}

.mobile-fab:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 12px 35px rgba(34, 211, 107, 0.4);
}

.mobile-fab:active {
  transform: translateY(-2px) scale(1.02);
}

/* 移动端编辑器弹窗 */
.mobile-editor-dialog {
  border-radius: 16px;
}

.mobile-editor-dialog :deep(.el-dialog) {
  border-radius: 16px;
  background: var(--color-bg);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.mobile-editor-dialog :deep(.el-dialog__header) {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px 16px 0 0;
  padding: 20px;
}

.mobile-editor-dialog :deep(.el-dialog__title) {
  color: var(--color-text);
  font-weight: 600;
  font-size: 1.2rem;
}

.mobile-editor-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.mobile-editor-dialog :deep(.el-dialog__footer) {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 0 0 16px 16px;
  padding: 20px;
}

.mobile-editor-content {
  padding: 20px;
}

.mobile-editor-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.mobile-editor-info {
  margin-left: 16px;
}

.mobile-editor-name {
  font-weight: 600;
  color: var(--color-text);
  display: block;
  font-size: 1.1rem;
}

.mobile-editor-hint {
  color: var(--color-text);
  font-size: 0.9rem;
  opacity: 0.7;
}

.mobile-editor-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.mobile-image-selector {
  margin-top: 8px;
}

.mobile-editor-footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.mobile-editor-footer .el-button {
  flex: 1;
  padding: 12px 20px;
  font-weight: 600;
  border-radius: 12px;
}

.mobile-editor-footer .el-button--primary {
  background: linear-gradient(135deg, #22d36b, #4ade80);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.mobile-editor-footer .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(34, 211, 107, 0.3);
}

@media (max-width: 768px) {
  /* 隐藏桌面端发布区域 */
  .post-editor-section {
    display: none;
  }
  
  /* 显示移动端浮动按钮 */
  .mobile-fab-container {
    display: block;
  }
  
  .main-content {
    padding: 0 16px;
  }
  
  .page-header {
    padding-top: 16px;
    margin-bottom: 30px;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .page-subtitle {
    font-size: 1rem;
  }
  
  .search-container {
    flex-direction: column;
    gap: 8px;
  }
  
  .search-input,
  .search-type-select,
  .filter-select {
    width: 100%;
  }
  
  .post-card {
    padding: 20px;
  }
  
  .post-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .post-type-badge {
    align-self: flex-end;
  }
  
  .post-actions-bar {
    flex-direction: row;
    gap: 8px;
    justify-content: space-around;
  }
  
  .like-button {
    flex: 1;
    justify-content: center;
    padding: 10px 8px;
    min-width: 0;
  }
  
  .like-button .el-icon {
    font-size: 14px;
  }
  
  .like-text {
    font-size: 0.8rem;
  }
}

.like-stat {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;
  user-select: none;
}
.like-stat:hover .el-icon,
.like-stat:active .el-icon {
  color: #22d36b;
  transform: scale(1.1);
}
.like-stat .el-icon.liked {
  color: #22d36b;
  transform: scale(1.1);
  transition: all 0.2s;
}

/* 分享高亮效果 */
.post-card.highlight-post {
  animation: highlightPulse 3s ease-in-out;
  border-color: #22d36b !important;
  box-shadow: 0 0 20px rgba(34, 211, 107, 0.3) !important;
}

@keyframes highlightPulse {
  0% {
    transform: scale(1);
    border-color: rgba(255, 255, 255, 0.1);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  }
  20% {
    transform: scale(1.02);
    border-color: #22d36b;
    box-shadow: 0 0 30px rgba(34, 211, 107, 0.4);
  }
  100% {
    transform: scale(1);
    border-color: rgba(255, 255, 255, 0.1);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  }
}

/* 下拉刷新指示器 */
.refresh-indicator {
  position: fixed;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(34, 211, 107, 0.95);
  color: white;
  padding: 8px 24px;
  border-radius: 0 0 16px 16px;
  font-size: 1rem;
  font-weight: 500;
  z-index: 1001;
  display: flex;
  align-items: center;
  gap: 12px;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s;
  box-shadow: 0 4px 16px rgba(34,211,107,0.15);
}

.refresh-indicator.show {
  opacity: 1;
  pointer-events: auto;
}

.refresh-indicator .el-icon {
  font-size: 20px;
  animation: spin 1s linear infinite;
}

.refresh-svg {
  display: block;
  margin-right: 4px;
}

@keyframes spin {
  from { transform: rotate(0deg);}
  to { transform: rotate(360deg);}
}

/* 下拉刷新时的页面效果 */
body.pulling {
  transition: transform 0.3s ease;
}

body.refreshing {
  transition: transform 0.3s ease;
}

/* 内心活动弹窗 */
.inner-thoughts-dialog {
  border-radius: 16px;
}

.inner-thoughts-dialog :deep(.el-dialog) {
  border-radius: 16px;
  background: var(--color-bg);
  border: 1px solid rgba(255, 255, 255, 0.1);
  max-width: 90vw;
  max-height: 80vh;
  overflow: hidden;
}

.inner-thoughts-dialog :deep(.el-dialog__header) {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px 16px 0 0;
  padding: 20px;
}

.inner-thoughts-dialog :deep(.el-dialog__title) {
  color: var(--color-text);
  font-weight: 600;
  font-size: 1.2rem;
}

.inner-thoughts-dialog :deep(.el-dialog__body) {
  padding: 0;
  max-height: 60vh;
  overflow-y: auto;
}

.inner-thoughts-dialog :deep(.el-dialog__footer) {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 0 0 16px 16px;
  padding: 20px;
}

.inner-thoughts-content {
  padding: 20px;
}

.thoughts-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.thoughts-info {
  margin-left: 16px;
}

.thoughts-author {
  font-weight: 600;
  color: var(--color-text);
  display: block;
  font-size: 1.1rem;
}

.thoughts-time {
  color: var(--color-text);
  font-size: 0.9rem;
  opacity: 0.7;
}

.thoughts-type {
  color: var(--color-text);
  font-size: 0.8rem;
  opacity: 0.6;
  margin-top: 4px;
  display: block;
  padding: 2px 8px;
  background: rgba(34, 211, 107, 0.1);
  border-radius: 12px;
  width: fit-content;
}

.thoughts-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.thoughts-content,
.thoughts-original {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s ease;
}

.thoughts-content:hover,
.thoughts-original:hover {
  border-color: rgba(34, 211, 107, 0.2);
  background: rgba(34, 211, 107, 0.02);
}

.thoughts-content h4,
.thoughts-original h4 {
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 12px;
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

.thoughts-content h4::before {
  content: "💭";
  font-size: 1.2rem;
}

.thoughts-original h4::before {
  content: "💬";
  font-size: 1.2rem;
}

.thoughts-content p,
.thoughts-original p {
  color: var(--color-text);
  font-size: 0.95rem;
  line-height: 1.6;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .inner-thoughts-dialog :deep(.el-dialog) {
    margin: 10px;
    width: calc(100% - 20px) !important;
    max-width: none;
    max-height: 85vh;
  }
  
  .inner-thoughts-dialog :deep(.el-dialog__body) {
    max-height: 65vh;
  }
  
  .inner-thoughts-content {
    padding: 16px;
  }
  
  .thoughts-header {
    margin-bottom: 16px;
    padding-bottom: 12px;
  }
  
  .thoughts-content,
  .thoughts-original {
    padding: 12px;
  }
  
  .thoughts-content h4,
  .thoughts-original h4 {
    font-size: 0.95rem;
  }
  
  .thoughts-content p,
  .thoughts-original p {
    font-size: 0.9rem;
  }
}

.like-stat {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;
  user-select: none;
}
.like-stat:hover .el-icon,
.like-stat:active .el-icon {
  color: #22d36b;
  transform: scale(1.1);
}
.like-stat .el-icon.liked {
  color: #22d36b;
  transform: scale(1.1);
  transition: all 0.2s;
}

/* 内心活动按钮样式 */
.inner-thoughts-stat {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
  user-select: none;
}

.inner-thoughts-stat:hover .el-icon,
.inner-thoughts-stat:active .el-icon {
  color: #667eea;
  transform: scale(1.1);
}

.inner-thoughts-stat .el-icon {
  color: #667eea;
  transition: all 0.2s;
}
</style> 