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
                :on-exceed="handleImageExceed"
                :file-list="newPost.images"
                list-type="picture-card"
                :limit="9"
                accept="image/*"
                multiple
                drag
                :show-file-list="true"
                class="image-uploader"
              >
                <template #trigger>
                  <div class="upload-trigger">
                    <el-icon class="upload-icon"><Plus /></el-icon>
                    <div class="upload-text">
                      <span class="upload-title">点击或拖拽上传图片</span>
                      <span class="upload-hint">支持 JPG、PNG、GIF 格式，单张不超过 10MB</span>
                    </div>
                  </div>
                </template>
                <template #tip>
                  <div class="upload-tip">
                    <span>最多可上传 9 张图片，总大小不超过 90MB ({{ newPost.images.length }}/9)</span>
                  </div>
                </template>
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
                :on-exceed="handleImageExceed"
                :file-list="newPost.images"
                list-type="picture-card"
                :limit="9"
                accept="image/*"
                multiple
                :show-file-list="true"
                class="mobile-image-uploader"
              >
                <template #trigger>
                  <div class="mobile-upload-trigger">
                    <el-icon class="mobile-upload-icon"><Plus /></el-icon>
                    <div class="mobile-upload-text">
                      <span class="mobile-upload-title">选择图片</span>
                      <span class="mobile-upload-hint">{{ newPost.images.length }}/9 (≤90MB)</span>
                    </div>
                  </div>
                </template>
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
                <div v-if="post.images && post.images.length > 0" class="post-images" @click.stop>
                  <div 
                    class="image-grid"
                    :class="getImageGridClass(post.images.length)"
                  >
                    <div 
                      v-for="(image, index) in post.images" 
                      :key="`${post.postId}-${index}`"
                      class="image-item"
                      @click.stop
                    >
                      <el-image 
                        :src="buildImageUrl(image)" 
                        fit="cover"
                        :preview-src-list="getImagePreviewList(post.images)"
                        :initial-index="index"
                        :preview-teleported="true"
                        :hide-on-click-modal="false"
                        @click.stop
                        @load="handleImagePreviewStart"
                        @error="handleImagePreviewClose"
                      />
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 动态统计（点赞/评论） -->
              <div class="post-stats">
                <span class="stat-item like-stat" @click="toggleLike(post)">
                  <el-icon :class="{ 'liked': post.isLiked }">
                    <StarFilled v-if="post.isLiked" />
                    <Star v-else />
                  </el-icon>
                  <span>{{ (post.likes || []).length }}</span>
                </span>
                <span class="stat-item">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>{{ getActualCommentCount(post) }}</span>
                </span>
                <!-- 添加查看内心活动按钮 -->
                <span v-if="post.innerThoughts" class="stat-item inner-thoughts-stat" @click="showInnerThoughts(post)">
                  <el-icon><View /></el-icon>
                </span>
              </div>
              
              <!-- 点赞用户头像列表 -->
              <div v-if="(post.likes || []).length > 0" class="liked-users-section">
                <div class="liked-users-list">
                  <div 
                    v-for="like in post.likes || []" 
                    :key="like.userId"
                    class="liked-user-item"
                    :title="`${like.userName} (${like.userType === 'robot' ? '天使' : '用户'})`"
                  >
                    <el-avatar 
                      :src="getLikedUserAvatarUrl(like)" 
                      :size="24"
                      @error="(event) => handleLikedUserAvatarError(event, like)"
                    />
                  </div>
                </div>
              </div>
              
              <!-- 评论区域 -->
              <div class="comments-section" @click.stop>
                <!-- 评论列表 -->
                <div class="comments-list">
                  <div 
                    v-for="comment in getTopLevelComments(post)" 
                    :key="comment.commentId"
                    class="comment-item"
                    @click.stop
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
                    <div class="comment-actions" @click.stop>
                      <span class="action-link" @click.stop="showReplyInput(comment)">回复</span>
                      <span class="action-link" @click.stop="toggleCommentLike(comment)">
                        <el-icon>
                          <StarFilled v-if="comment.isLiked" />
                          <Star v-else />
                        </el-icon>
                        {{ comment.likeCount || 0 }}
                      </span>
                      <!-- 显示回复数量 -->
                      <span v-if="comment.replyCount > 0" class="action-link">
                        <el-icon><ChatDotRound /></el-icon>
                        {{ comment.replyCount }}
                      </span>
                      <!-- 添加查看内心活动按钮 -->
                      <span v-if="comment.innerThoughts" class="action-link" @click.stop="showInnerThoughts(comment)">
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
                    <div v-if="getCommentReplies(comment, post).length > 0" class="replies-section">
                      <div class="replies-list">
                        <div 
                          v-for="reply in getCommentReplies(comment, post)" 
                          :key="reply.commentId"
                          class="reply-item"
                          @click.stop
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
                          <div class="reply-actions" @click.stop>
                            <span class="action-link" @click.stop="toggleCommentLike(reply)">
                              <el-icon>
                                <StarFilled v-if="reply.isLiked" />
                                <Star v-else />
                              </el-icon>
                              {{ reply.likeCount || 0 }}
                            </span>
                            <!-- 添加查看内心活动按钮 -->
                            <span v-if="reply.innerThoughts" class="action-link" @click.stop="showInnerThoughts(reply)">
                              <el-icon><View /></el-icon>
                            </span>
                          </div>
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
    <div class="refresh-indicator" :class="{ 
      'show': isRefreshing || isPulling,
      'refreshing': isRefreshing,
      'pulling': isPulling && !isRefreshing
    }">
      <div class="refresh-content">
        <div class="refresh-icon">
          <div class="refresh-circle" :style="{ transform: `rotate(${refreshRotation}deg)` }">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <circle 
                cx="12" 
                cy="12" 
                r="10" 
                stroke="currentColor" 
                stroke-width="2" 
                stroke-linecap="round"
                stroke-dasharray="31.416"
                stroke-dashoffset="31.416"
                :style="{ 
                  strokeDashoffset: isRefreshing ? '0' : '31.416',
                  transition: isRefreshing ? 'stroke-dashoffset 1s ease-in-out' : 'none'
                }"
              />
            </svg>
          </div>
        </div>
        <div class="refresh-text">
          <span v-if="isRefreshing" class="refreshing-text">正在刷新...</span>
          <span v-else-if="isPulling" class="pulling-text">
            {{ refreshProgress >= 1 ? '释放刷新' : '下拉刷新' }}
          </span>
        </div>
      </div>
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
import { useWebSocketStore } from '@/stores/websocket'
import { ElMessageBox, ElPopover } from 'element-plus'
import { message } from '@/utils/message'
import { Plus, ChatDotRound, MoreFilled, Close, Loading, Menu, House, User, SwitchButton, Search, Star, StarFilled, View } from '@element-plus/icons-vue'
import { getUserAvatarUrl, getRobotAvatarUrl, handleRobotAvatarError } from '@/utils/avatar'
import { getCommentList, createComment, replyComment, deleteComment, likeComment, unlikeComment } from '@/api/comment'
import { createPost, searchPosts, getPostDetail } from '@/api/post'

// 响应式数据
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const momentsStore = useMomentsStore()
const websocketStore = useWebSocketStore()
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
const refreshProgress = ref(0) // 下拉进度 (0-1)
const refreshRotation = ref(0) // 旋转角度
const lastVibrationTime = ref(0) // 上次震动时间，用于触觉反馈

// 新动态数据
const newPost = ref({
  content: '',
  images: []
})



// 搜索相关状态
const searchKeyword = ref('')
const searchType = ref('all')
const searchTimeout = ref(null)
const isSearching = ref(false)

// 内心活动相关状态
const showInnerThoughtsDialog = ref(false)
const currentThoughtsItem = ref(null)

// 图片预览状态管理
const imagePreviewActive = ref(false)

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn)

watch(showInnerThoughtsDialog, (val) => {
  if (val) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})

// 方法
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
    refreshProgress.value = 0
    refreshRotation.value = 0
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
    
    // 计算下拉进度，使用缓动函数让动画更自然
    const progress = Math.min(deltaY / refreshThreshold, 1.2)
    refreshProgress.value = progress
    
    // 计算旋转角度，使用缓动函数
    const rotation = Math.min(deltaY / refreshThreshold * 180, 180)
    refreshRotation.value = rotation
    
    // 添加下拉效果 - 使用更微妙的变换
    const pullDistance = Math.min(deltaY * 0.25, refreshThreshold * 0.6) // 减少移动距离
    const scale = 1 + (deltaY / refreshThreshold) * 0.01 // 更微妙的缩放效果
    const opacity = Math.min(deltaY / refreshThreshold * 0.3, 0.3) // 微妙的透明度变化
    
    document.body.style.transform = `translateY(${pullDistance}px) scale(${scale})`
    document.body.style.transformOrigin = 'top center'
    document.body.style.transition = 'none' // 确保实时响应
    
    // 添加微妙的背景模糊效果
    if (deltaY > refreshThreshold * 0.5) {
      document.body.style.filter = `blur(${opacity}px)`
    }
    
    // 触觉反馈 - 当达到刷新阈值时
    const now = Date.now()
    if (deltaY >= refreshThreshold && now - lastVibrationTime.value > 100) {
      // 检查是否支持震动API
      if ('vibrate' in navigator) {
        navigator.vibrate(50) // 短震动
        lastVibrationTime.value = now
      }
    }
  }
}

const handleTouchEnd = async (event) => {
  if (!isPulling.value) return
  
  const deltaY = currentY.value - startY.value
  
  // 添加平滑的恢复动画
  document.body.style.transition = 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)'
  document.body.style.transform = ''
  document.body.style.transformOrigin = ''
  document.body.style.filter = ''
  
  isPulling.value = false
  
  // 如果下拉距离足够，触发刷新
  if (deltaY > refreshThreshold && window.pageYOffset === 0) {
    await performRefresh()
  } else {
    // 重置进度，添加延迟让动画完成
    setTimeout(() => {
      refreshProgress.value = 0
      refreshRotation.value = 0
    }, 400)
  }
  
  // 清除过渡效果
  setTimeout(() => {
    document.body.style.transition = ''
  }, 400)
}

/**
 * 执行刷新操作
 */
const performRefresh = async () => {
  if (isRefreshing.value) return
  try {
    isRefreshing.value = true
    refreshProgress.value = 1
    refreshRotation.value = 180
    
    // 刷新数据
    await momentsStore.loadPosts({}, true)
    await loadAllCommentsAndReplies()
    // 清除搜索和筛选状态
    if (searchKeyword.value.trim()) {
      searchKeyword.value = ''
      searchType.value = 'all'
    }
    filterType.value = ''
    
    // 刷新成功时的触觉反馈
    if ('vibrate' in navigator) {
      navigator.vibrate([50, 100, 50]) // 成功反馈：短-长-短
    }
    
    // 不再弹出任何 message
  } catch (error) {
    // 不弹窗，仅可选地在控制台输出
    console.error('刷新失败:', error)
    
    // 刷新失败时的触觉反馈
    if ('vibrate' in navigator) {
      navigator.vibrate([200, 100, 200]) // 失败反馈：长-短-长
    }
  } finally {
    isRefreshing.value = false
    // 延迟重置状态，让动画完成
    setTimeout(() => {
      refreshProgress.value = 0
      refreshRotation.value = 0
    }, 300)
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
        // 使用新的加载方式，获取动态详情（包含评论）
        await loadPostWithComments(post)

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
  
  // 检查图片总大小
  if (newPost.value.images && newPost.value.images.length > 0) {
    const totalSize = newPost.value.images.reduce((total, fileObj) => {
      return total + (fileObj.raw ? fileObj.raw.size : 0)
    }, 0)
    const totalSizeMB = totalSize / 1024 / 1024
    
    if (totalSizeMB > 90) {
      message.error(`图片总大小不能超过 90MB，当前总大小: ${totalSizeMB.toFixed(1)}MB`)
      return
    }
    
    console.log(`准备发布动态，包含 ${newPost.value.images.length} 张图片，总大小: ${totalSizeMB.toFixed(1)}MB`)
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
        likes: [], // 初始化点赞信息为空数组
        createdAt: newPostData.createdAt,
        updatedAt: newPostData.createdAt,
        showComments: true // 设置评论区域为展开状态
      }
      
      momentsStore.posts.unshift(postData)
      
      // 为新发布的动态加载评论和回复
      await loadPostWithComments(postData)
      
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
    console.error('发布动态失败:', error)
    if (error.response?.status === 413) {
      message.error('图片总大小超过服务器限制，请减少图片数量或压缩图片')
    } else if (error.message?.includes('size')) {
      message.error('图片大小超过限制，请选择较小的图片')
    } else {
      message.error('动态发布失败，请重试')
    }
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
  
  // 检查图片总大小
  if (newPost.value.images && newPost.value.images.length > 0) {
    const totalSize = newPost.value.images.reduce((total, fileObj) => {
      return total + (fileObj.raw ? fileObj.raw.size : 0)
    }, 0)
    const totalSizeMB = totalSize / 1024 / 1024
    
    if (totalSizeMB > 90) {
      message.error(`图片总大小不能超过 90MB，当前总大小: ${totalSizeMB.toFixed(1)}MB`)
      return
    }
    
    console.log(`准备发布动态，包含 ${newPost.value.images.length} 张图片，总大小: ${totalSizeMB.toFixed(1)}MB`)
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
        likes: [], // 初始化点赞信息为空数组
        createdAt: newPostData.createdAt,
        updatedAt: newPostData.createdAt,
        showComments: true // 设置评论区域为展开状态
      }
      
      momentsStore.posts.unshift(postData)
      
      // 为新发布的动态加载评论和回复
      await loadPostWithComments(postData)
    
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
    console.error('发布动态失败:', error)
    if (error.response?.status === 413) {
      message.error('图片总大小超过服务器限制，请减少图片数量或压缩图片')
    } else if (error.message?.includes('size')) {
      message.error('图片大小超过限制，请选择较小的图片')
    } else {
      message.error('动态发布失败，请重试')
    }
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
  // 保存原始状态
  const originalIsLiked = post.isLiked
  
  try {
    console.log(`开始点赞操作，动态ID: ${post.postId}，当前状态: ${post.isLiked}`)
    
    if (post.isLiked) {
      await momentsStore.unlikePostAction(post.postId)
      // 立即更新本地状态
      post.isLiked = false
      console.log('取消点赞成功，本地状态已更新为: false')
    } else {
      await momentsStore.likePostAction(post.postId)
      // 立即更新本地状态
      post.isLiked = true
      console.log('点赞成功，本地状态已更新为: true')
    }
    
    // 点赞操作成功后，立即刷新点赞信息
    await loadPostWithComments(post)
    console.log(`点赞操作完成，最终状态: ${post.isLiked}`)
  } catch (error) {
    // 如果操作失败，恢复原始状态
    post.isLiked = originalIsLiked
    console.error('点赞操作失败，已恢复原始状态:', error)
    message.error('操作失败')
  }
}



/**
 * 获取点赞用户的头像URL
 * @param {Object} like - 点赞信息对象
 * @returns {string} 头像URL
 */
const getLikedUserAvatarUrl = (like) => {
  if (like.userType === 'robot') {
    return getRobotAvatarUrl({ avatar: like.userAvatar, name: like.userName, id: like.userId })
  } else {
    return getUserAvatarUrl({ avatar: like.userAvatar, nickname: like.userName })
  }
}

/**
 * 处理点赞用户头像加载错误
 * @param {Event} event - 错误事件
 * @param {Object} like - 点赞信息对象
 */
const handleLikedUserAvatarError = (event, like) => {
  if (like.userType === 'robot') {
    handleRobotAvatarError(event, like.userName)
  } else {
    event.target.src = getUserAvatarUrl({ nickname: like.userName })
  }
}

const showComments = async (post) => {
  post.showComments = !post.showComments
  
  // 如果评论还没有加载过，则加载评论和回复
  if (post.showComments && (!momentsStore.comments[post.postId] || momentsStore.comments[post.postId].length === 0)) {
    try {
      // 使用新的加载方式，获取动态详情（包含评论）
      await loadPostWithComments(post)
    } catch (error) {
      message.error('加载评论失败')
    }
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
    
    // 重新加载动态详情以获取最新的评论列表
    await loadPostWithComments(post)
    
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
    
    // 找到对应的动态并重新加载详情以获取最新的评论和回复列表
    for (const post of momentsStore.posts) {
      const commentList = post.comments || momentsStore.comments[post.postId]
      if (commentList && commentList.find(c => c.commentId === comment.commentId)) {
        await loadPostWithComments(post)
        break
      }
    }
    
    message.success('回复发表成功')
  } catch (error) {
    message.error('回复发表失败')
  } finally {
    comment.submittingReply = false
  }
}

const toggleCommentLike = async (comment) => {
  // 保存原始状态
  const originalIsLiked = comment.isLiked
  
  try {
    if (comment.isLiked) {
      await momentsStore.unlikeCommentAction(comment.commentId)
      // 立即更新本地状态
      comment.isLiked = false
    } else {
      await momentsStore.likeCommentAction(comment.commentId)
      // 立即更新本地状态
      comment.isLiked = true
    }
  } catch (error) {
    // 如果操作失败，恢复原始状态
    comment.isLiked = originalIsLiked
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
  
  // 检查总大小限制（9张图片，每张最大10MB，总大小不超过90MB）
  const totalSize = fileList.reduce((total, fileObj) => {
    return total + (fileObj.raw ? fileObj.raw.size : 0)
  }, 0)
  const totalSizeMB = totalSize / 1024 / 1024
  
  if (totalSizeMB > 90) {
    message.error(`图片总大小不能超过 90MB，当前总大小: ${totalSizeMB.toFixed(1)}MB`)
    return false
  }
  
  // 为文件对象添加URL用于预览
  if (file.raw && !file.url) {
    file.url = URL.createObjectURL(file.raw)
  }
  
  // 更新图片列表，保持完整的文件对象结构
  newPost.value.images = fileList
  
  // 显示当前总大小信息
  if (fileList.length > 0) {
    console.log(`已选择 ${fileList.length} 张图片，总大小: ${totalSizeMB.toFixed(1)}MB`)
  }
}

const handleImageRemove = (file, fileList) => {
  // 更新图片列表
  newPost.value.images = fileList
}

const handleImageExceed = (files, fileList) => {
  message.error('图片数量超过限制，最多只能上传 9 张图片')
}

const getImageGridClass = (count) => {
  if (count === 1) return 'grid-1'
  if (count === 2) return 'grid-2'
  if (count === 3) return 'grid-3'
  if (count === 4) return 'grid-4'
  return 'grid-more'
}

/**
 * 获取图片预览列表，优化性能避免重复计算
 * @param {Array} images - 图片数组
 * @returns {Array} 预览URL列表
 */
const getImagePreviewList = (images) => {
  if (!images || images.length === 0) return []
  return images.map(img => buildImageUrl(img))
}

/**
 * 处理图片预览开始
 */
const handleImagePreviewStart = () => {
  imagePreviewActive.value = true
  console.log('图片预览开始')
}

/**
 * 处理图片预览结束
 */
const handleImagePreviewClose = () => {
  imagePreviewActive.value = false
  console.log('图片预览结束')
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

  // WebSocket事件处理函数 - 仅在支持增量刷新时启用
  let handlePostUpdate, handleCommentUpdate, handleRobotAction
  
  if (window.canIncrementalRefresh !== false) {
    handlePostUpdate = async () => {
      console.log('📝 Moments.vue收到动态更新事件')
      // 检查是否支持增量刷新，如果不支持则跳过
      if (!window.canIncrementalRefresh) {
        console.log('⚠️ 不支持增量刷新，跳过动态更新处理')
        return
      }
      await momentsStore.loadPosts({}, true)
      await loadAllCommentsAndReplies()
    }
    
    handleCommentUpdate = async () => {
      console.log('💬 Moments.vue收到评论更新事件')
      // 检查是否支持增量刷新，如果不支持则跳过
      if (!window.canIncrementalRefresh) {
        console.log('⚠️ 不支持增量刷新，跳过评论更新处理')
        return
      }
      await momentsStore.loadPosts({}, true)
      await loadAllCommentsAndReplies()
    }
    
    handleRobotAction = async () => {
      console.log('🤖 Moments.vue收到机器人行为事件')
      // 检查是否支持增量刷新，如果不支持则跳过
      if (!window.canIncrementalRefresh) {
        console.log('⚠️ 不支持增量刷新，跳过机器人行为处理')
        return
      }
      await momentsStore.loadPosts({}, true)
      await loadAllCommentsAndReplies()
    }
  }
  
  // 仅在支持增量刷新时添加事件监听
  if (window.canIncrementalRefresh !== false && handlePostUpdate) {
    window.addEventListener('post-update', handlePostUpdate)
    window.addEventListener('comment-update', handleCommentUpdate)
    window.addEventListener('robot-post', handleRobotAction)
    window.addEventListener('robot-comment', handleRobotAction)
    window.addEventListener('robot-like', handleRobotAction)
    window.addEventListener('robot-reply', handleRobotAction)
    console.log('✅ Moments.vue已添加WebSocket事件监听')
  } else {
    console.log('⚠️ Moments.vue跳过WebSocket事件监听（不支持增量刷新）')
  }
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

  // 清理WebSocket事件监听（仅在已添加的情况下）
  if (window.canIncrementalRefresh !== false && handlePostUpdate) {
    window.removeEventListener('post-update', handlePostUpdate)
    window.removeEventListener('comment-update', handleCommentUpdate)
    window.removeEventListener('robot-post', handleRobotAction)
    window.removeEventListener('robot-comment', handleRobotAction)
    window.removeEventListener('robot-like', handleRobotAction)
    window.removeEventListener('robot-reply', handleRobotAction)
    console.log('🛑 Moments.vue已清理WebSocket事件监听')
  }
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
 * 现在使用后端返回的评论列表，避免多次API调用
 */
const loadAllCommentsAndReplies = async () => {
  for (const post of momentsStore.posts) {
    // 设置评论区域为展开状态
    post.showComments = true
    
    try {
      // 如果动态没有评论数据，则从后端获取动态详情（包含评论）
      if (!post.comments || post.comments.length === 0) {
        await loadPostWithComments(post)
      } else {
        // 如果已有评论数据，直接使用
        momentsStore.comments[post.postId] = post.comments
      }
      

    } catch (error) {
      console.error(`加载动态 ${post.postId} 的评论失败:`, error)
    }
  }
}

/**
 * 加载动态详情（包含评论、回复和点赞信息）
 * @param {Object} post - 动态对象
 */
const loadPostWithComments = async (post) => {
  try {
    // 调用获取动态详情API，该API现在会返回评论列表和点赞信息
    const response = await getPostDetail(post.postId)
    
    if (response.code === 200) {
      const postDetail = response.data
      
      // 更新动态的评论数据
      post.comments = postDetail.comments || []
      momentsStore.comments[post.postId] = postDetail.comments || []
      
      // 更新动态的点赞数据
      post.likes = postDetail.likes || []
      
      // 确保正确获取点赞状态，支持多种字段名
      const rawIsLiked = postDetail.isLiked !== undefined ? postDetail.isLiked : 
                        (postDetail.liked !== undefined ? postDetail.liked : false)
      
      // 检查当前用户是否在点赞列表中
      const currentUserId = userStore.userInfo?.userId
      let computedIsLiked = rawIsLiked
      
      if (currentUserId && post.likes && post.likes.length > 0) {
        const userLike = post.likes.find(like => like.userId === currentUserId)
        computedIsLiked = !!userLike
        console.log(`用户 ${currentUserId} 在点赞列表中的状态: ${computedIsLiked}`)
      }
      
      post.isLiked = computedIsLiked
      post.likeCount = postDetail.likeCount || 0
      
      // 更新评论数为一级评论的数量（不包括回复）
      const topLevelComments = postDetail.comments ? postDetail.comments.filter(comment => !comment.parentId) : []
      post.commentCount = topLevelComments.length
      
      console.log(`动态 ${post.postId} 加载完成，评论数量: ${post.commentCount}，点赞数量: ${post.likeCount}，当前用户是否点赞: ${post.isLiked}`)
      console.log('点赞详情:', postDetail.likes)
      console.log('原始isLiked字段:', rawIsLiked)
      console.log('计算后的isLiked字段:', computedIsLiked)
      
      // 调试评论和回复信息
      if (postDetail.comments && postDetail.comments.length > 0) {
        console.log('完整评论列表:', postDetail.comments)
        
        // 检查回复
        const replies = postDetail.comments.filter(comment => comment.parentId)
        const topLevelComments = postDetail.comments.filter(comment => !comment.parentId)
        
        console.log('一级评论数量:', topLevelComments.length)
        console.log('回复数量:', replies.length)
        console.log('一级评论列表:', topLevelComments)
        
        // 检查每个一级评论的回复
        topLevelComments.forEach(comment => {
          const commentReplies = replies.filter(reply => reply.parentId === comment.commentId)
          console.log(`评论 ${comment.commentId} 的回复数量: ${commentReplies.length}`)
          if (commentReplies.length > 0) {
            console.log(`评论 ${comment.commentId} 的回复:`, commentReplies)
          }
        })
      }
    }
  } catch (error) {
    console.error(`加载动态详情失败，动态ID: ${post.postId}`, error)
    // 如果获取详情失败，回退到原来的评论加载方式
    await momentsStore.loadComments(post.postId, {}, true)
    await loadAllReplies(post.postId)
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
        for (const post of momentsStore.posts) {
          post.showComments = true
          try {
            await loadPostWithComments(post)
          } catch (error) {
            console.error(`加载动态 ${post.postId} 的评论失败:`, error)
          }
        }
        
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




/**
 * 显示内心活动弹窗
 * @param {Object} item - 包含内心活动的项目（动态或评论）
 */
const showInnerThoughts = (item) => {
  currentThoughtsItem.value = item
  showInnerThoughtsDialog.value = true
}

/**
 * 获取动态的实际评论数量（基于已加载的评论）
 * @param {Object} post - 动态对象
 * @returns {number} 实际评论数量
 */
const getActualCommentCount = (post) => {
  // 只计算一级评论的数量，不包括回复
  const topLevelComments = getTopLevelComments(post)
  return topLevelComments.length
}

/**
 * 获取指定评论的回复列表
 * @param {Object} comment - 评论对象
 * @param {Object} post - 动态对象
 * @returns {Array} 回复列表
 */
const getCommentReplies = (comment, post) => {
  const commentList = post.comments || momentsStore.comments[post.postId] || []
  const replies = commentList.filter(reply => reply.parentId === comment.commentId)
  
  // 调试信息
  if (replies.length > 0) {
    console.log(`评论 ${comment.commentId} 的回复:`, replies)
  }
  
  return replies
}

/**
 * 获取动态的一级评论列表（排除回复）
 * @param {Object} post - 动态对象
 * @returns {Array} 一级评论列表
 */
const getTopLevelComments = (post) => {
  const commentList = post.comments || momentsStore.comments[post.postId] || []
  const topLevelComments = commentList.filter(comment => !comment.parentId)
  
  console.log(`动态 ${post.postId} 的一级评论数量: ${topLevelComments.length}`)
  
  return topLevelComments
}
</script>

<style scoped>
@import url('../styles/moment.scss');
</style> 