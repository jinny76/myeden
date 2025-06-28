<template>
  <AppHeader />
  <div class="post-detail-container">
    <!-- 返回按钮 -->
    <div class="back-button-section">
      <el-button 
        @click="goBack" 
        type="text" 
        class="back-button"
      >
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-section">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 动态详情 -->
      <div v-else-if="post" class="post-detail-section">
        <el-card class="post-detail-card">
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
            <span class="stat-item like-stat" @click="toggleLike(post)">
              <el-icon :class="{ 'liked': post.isLiked }"><Star /></el-icon>
              <span>{{ post.likeCount }}</span>
            </span>
            <span class="stat-item">
              <el-icon><ChatDotRound /></el-icon>
              <span>{{ post.commentCount }}</span>
            </span>
          </div>
        </el-card>

        <!-- 评论区域 -->
        <div class="comments-section" ref="commentsSection">
          <div class="section-header">
            <h3>评论 ({{ post.commentCount }})</h3>
          </div>
          
          <!-- 评论列表 -->
          <div class="comments-list">
            <div 
              v-for="comment in comments" 
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
                  <el-icon :class="{ 'liked': comment.isLiked }"><Star /></el-icon>
                  {{ comment.likeCount }}
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
                        <el-icon :class="{ 'liked': reply.isLiked }"><Star /></el-icon>
                        {{ reply.likeCount }}
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
              v-model="newComment"
              placeholder="写下你的评论..."
              :maxlength="2000"
              show-word-limit
              @keyup.enter="submitComment"
            >
              <template #append>
                <el-button @click="submitComment">发送</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>

      <!-- 错误状态 -->
      <div v-else class="error-section">
        <el-empty description="动态不存在或已被删除">
          <el-button type="primary" @click="goBack">返回</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMomentsStore } from '@/stores/moments'
import { ElMessageBox } from 'element-plus'
import { message } from '@/utils/message'
import { ArrowLeft, ChatDotRound, MoreFilled, Loading, Message, Star } from '@element-plus/icons-vue'
import { getUserAvatarUrl, getRobotAvatarUrl, handleRobotAvatarError } from '@/utils/avatar'
import { getPostDetail, deletePost, likePost, unlikePost } from '@/api/post'
import { getCommentList, createComment, createReply, replyComment, likeComment, unlikeComment, getReplyList } from '@/api/comment'

// 响应式数据
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const momentsStore = useMomentsStore()

const loading = ref(true)
const post = ref(null)
const comments = ref([])
const newComment = ref('')
const replyStates = ref({})
const commentsSection = ref(null)

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 方法
const goBack = () => {
  router.back()
}

const scrollToComments = () => {
  nextTick(() => {
    if (commentsSection.value) {
      commentsSection.value.scrollIntoView({ behavior: 'smooth' })
    }
  })
}

const loadPostDetail = async () => {
  try {
    loading.value = true
    const postId = route.params.postId
    
    const response = await getPostDetail(postId)
    
    if (response.code === 200) {
      post.value = response.data
      await loadComments()
      await loadAllReplies()
    } else {
      post.value = null
    }
  } catch (error) {
    console.error('加载动态详情失败:', error)
    message.error('加载动态详情失败')
    post.value = null
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  try {
    const response = await getCommentList(post.value.postId, { page: 1, size: 50 })
    
    if (response.code === 200) {
      comments.value = response.data.comments || []
    }
  } catch (error) {
    console.error('加载评论失败:', error)
    message.error('加载评论失败')
  }
}

const loadAllReplies = async () => {
  for (const comment of comments.value) {
    if (comment.replyCount > 0) {
      await loadReplies(comment.commentId, true)
    }
  }
}

const loadReplies = async (commentId, refresh = false) => {
  if (!replyStates.value[commentId]) {
    replyStates.value[commentId] = {
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
      goBack()
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
      post.likeCount--
      post.isLiked = false
    } else {
      await momentsStore.likePostAction(post.postId)
      post.likeCount++
      post.isLiked = true
    }
  } catch (error) {
    message.error('操作失败')
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) {
    message.warning('请输入评论内容')
    return
  }
  
  try {
    const response = await createComment(post.value.postId, { content: newComment.value })
    
    if (response.code === 200) {
      newComment.value = ''
      await loadComments()
      post.value.commentCount++
      message.success('评论发表成功')
    }
  } catch (error) {
    message.error('评论发表失败')
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
  
  try {
    const response = await replyComment(comment.commentId, { content: comment.replyContent })
    
    if (response.code === 200) {
      comment.showReplyInput = false
      await loadReplies(comment.commentId, true)
      comment.replyCount++
      message.success('回复发表成功')
    }
  } catch (error) {
    message.error('回复发表失败')
  }
}

const toggleCommentLike = async (comment) => {
  try {
    if (comment.isLiked) {
      await unlikeComment(comment.commentId)
      comment.likeCount--
      comment.isLiked = false
    } else {
      await likeComment(comment.commentId)
      comment.likeCount++
      comment.isLiked = true
    }
  } catch (error) {
    message.error('操作失败')
  }
}

const getImageGridClass = (count) => {
  if (count === 1) return 'grid-1'
  if (count === 2) return 'grid-2'
  if (count === 3) return 'grid-3'
  if (count === 4) return 'grid-4'
  return 'grid-more'
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

// 生命周期
onMounted(() => {
  loadPostDetail()
})
</script>

<style scoped>
.post-detail-container {
  min-height: 100vh;
  background: var(--color-bg);
}

.back-button-section {
  background: var(--color-card);
  border-bottom: 1px solid var(--color-border);
  padding: 12px 20px;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--color-text);
  font-size: 14px;
}

.back-button:hover {
  color: var(--color-primary);
}

.main-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: var(--color-bg);
}

.loading-section {
  margin-bottom: 20px;
}

.post-detail-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-detail-card {
  border-radius: 12px;
  background: var(--color-card);
  color: var(--color-text);
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
  color: var(--color-text);
  display: block;
}

.post-time {
  color: var(--color-text);
  font-size: 12px;
}

.post-content {
  margin-bottom: 12px;
}

.post-content p {
  margin: 0 0 12px 0;
  line-height: 1.6;
  color: var(--color-text);
  font-size: 16px;
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
  color: var(--color-text);
  font-size: 14px;
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

.post-actions-bar {
  display: flex;
  gap: 16px;
  padding-top: 8px;
  border-top: 1px solid var(--color-border);
}

.comments-section {
  background: var(--color-card);
  border-radius: 12px;
  padding: 20px;
  color: var(--color-text);
}

.section-header {
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  color: var(--color-text);
  font-size: 18px;
}

.comments-list {
  margin-bottom: 20px;
}

.comment-item {
  margin-bottom: 16px;
  padding: 16px;
  background: var(--color-bg);
  border-radius: 8px;
  border: 1px solid var(--color-border);
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
  color: var(--color-text);
  font-size: 14px;
}

.comment-time {
  color: var(--color-text);
  font-size: 12px;
  margin-left: 8px;
}

.comment-content p {
  margin: 0;
  color: var(--color-text);
  line-height: 1.5;
  font-size: 14px;
}

.comment-actions {
  margin-top: 8px;
  display: flex;
  gap: 12px;
}

.action-link {
  color: var(--color-primary);
  cursor: pointer;
  font-size: 12px;
}

.action-link .el-icon {
  font-size: 14px;
  color: #22d36b;
  transition: all 0.2s;
}

.action-link .el-icon.liked {
  color: #22d36b;
  transform: scale(1.1);
}

.reply-input {
  margin-top: 8px;
}

.comment-input {
  margin-top: 16px;
}

.replies-section {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--color-border);
}

.replies-list {
  margin-bottom: 8px;
}

.reply-item {
  margin-bottom: 8px;
  padding: 8px 12px;
  background: var(--color-card);
  border-radius: 6px;
  border-left: 3px solid var(--color-border);
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
  color: var(--color-text);
  font-size: 13px;
}

.reply-time {
  color: var(--color-text);
  font-size: 11px;
  margin-left: 8px;
}

.reply-content {
  margin-bottom: 6px;
}

.reply-content p {
  margin: 0;
  color: var(--color-text);
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
  color: var(--color-text);
  font-size: 12px;
}

.loading-replies {
  text-align: center;
  margin-top: 8px;
  color: var(--color-text);
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.loading-replies .el-icon {
  font-size: 14px;
}

.error-section {
  text-align: center;
  margin: 40px 0;
}

@media (max-width: 768px) {
  .back-button-section {
    padding: 8px 16px;
  }
  
  .back-button {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    padding: 8px 12px;
  }
  
  .back-button .el-icon {
    font-size: 16px;
  }
  
  .main-content {
    padding: 16px;
    padding-top: 80px;
  }
  
  .post-detail-section {
    gap: 16px;
  }
  
  .post-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .post-actions {
    align-self: flex-end;
  }
  
  .post-content p {
    font-size: 15px;
  }
  
  .comments-section {
    padding: 16px;
  }
  
  .section-header h3 {
    font-size: 16px;
  }
  
  .comment-item {
    padding: 12px;
    margin-bottom: 12px;
  }
  
  .comment-content p {
    font-size: 13px;
  }
  
  .reply-item {
    padding: 6px 8px;
  }
  
  .reply-content p {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .back-button-section {
    padding: 6px 12px;
  }
  
  .back-button {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    padding: 6px 10px;
  }
  
  .back-button .el-icon {
    font-size: 14px;
  }
  
  .main-content {
    padding: 12px;
    padding-top: 70px;
  }
  
  .post-detail-section {
    gap: 12px;
  }
  
  .post-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
  
  .post-actions {
    align-self: flex-end;
  }
  
  .post-content p {
    font-size: 14px;
  }
  
  .comments-section {
    padding: 12px;
  }
  
  .section-header h3 {
    font-size: 15px;
  }
  
  .comment-item {
    padding: 8px;
    margin-bottom: 8px;
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