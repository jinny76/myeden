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
              <button class="publish-button" @click="publishPost" :disabled="publishing">
                <div v-if="publishing" class="loading-spinner"></div>
                <el-icon v-else><Plus /></el-icon>
                <span>{{ publishing ? '发布中...' : '发布动态' }}</span>
              </button>
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
          >
            <div class="post-card-content">
              <!-- 动态头部 -->
              <div class="post-header" :data-post-id="post.postId">
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
                <p>
                  {{ post.content }}
                  <el-icon class="speech-icon" style="cursor:pointer; margin-left:8px; vertical-align:middle;" 
                    @click="playSpeech(post.content, getAuthorGenderAge(post))">
                    <svg viewBox="0 0 24 24" width="18" height="18"><path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.06c1.48-.74 2.5-2.26 2.5-4.03zm2.5 0c0 2.53-1.54 4.71-3.75 5.65v2.13c3.45-1.01 6-4.13 6-7.78s-2.55-6.77-6-7.78v2.13C17.46 7.29 19 9.47 19 12z" fill="currentColor"/></svg>
                  </el-icon>
                </p>
                
                <!-- 图片展示 -->
                <div v-if="post.linkInfo == null && post.images && post.images.length > 0" class="post-images" @click.stop>
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
                        @close="handleImagePreviewClose"
                        @error="handleImageError"
                      >
                        <template #error>
                          <div class="image-error-placeholder">
                            <el-icon class="error-icon"><Picture /></el-icon>
                            <span class="error-text">图片加载失败</span>
                          </div>
                        </template>
                        <template #placeholder>
                          <div class="image-loading-placeholder">
                            <el-icon class="loading-icon is-loading"><Loading /></el-icon>
                            <span class="loading-text">加载中...</span>
                          </div>
                        </template>
                      </el-image>
                    </div>
                  </div>
                </div>

                <!-- 视频链接展示 -->
                <div v-if="post.linkInfo && post.linkInfo.dataType === 'video'" class="post-video-link" @click.stop>
                  <div class="video-card" @click="playVideo(post.linkInfo)">
                    <div class="video-thumbnail">
                      <el-image 
                        :src="post.linkInfo.image.startsWith('http') ? post.linkInfo.image : 'https:' + post.linkInfo.image" 
                        fit="cover"
                        class="thumbnail-image"
                        @error="handleVideoThumbnailError"
                      >
                        <template #error>
                          <div class="video-thumbnail-placeholder">
                            <div class="video-icon">
                              <el-icon><VideoPlay /></el-icon>
                            </div>
                            <div class="video-placeholder-text">视频封面</div>
                          </div>
                        </template>
                      </el-image>
                    </div>
                    <div class="video-info">
                      <h4 class="video-title">{{ post.linkInfo.title || '视频内容' }}</h4>
                      <div class="video-source">
                        <el-icon class="source-icon"><Link /></el-icon>
                        <span class="source-text">{{ getVideoSource(post.linkInfo.url) }}</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Three.js 三维动画展示 -->
                <div v-if="post.threeDSceneCode" class="post-animation" @click.stop>
                  <div class="animation-container">
                    <div class="animation-header">
                      <el-icon class="animation-icon"><MagicStick /></el-icon>
                      <span class="animation-title">三维动画</span>
                      <div class="animation-controls">
                        <el-button 
                          v-if="!animationStates[post.postId]?.playing" 
                          @click="playAnimation(post.postId)" 
                          type="primary" 
                          size="small"
                          circle
                        >
                          <el-icon><VideoPlay /></el-icon>
                        </el-button>
                        <el-button 
                          v-else 
                          @click="pauseAnimation(post.postId)" 
                          type="primary" 
                          size="small"
                          circle
                        >
                          <el-icon><VideoPause /></el-icon>
                        </el-button>
                        <el-button 
                          @click="resetAnimation(post.postId)" 
                          size="small"
                          circle
                        >
                          <el-icon><RefreshRight /></el-icon>
                        </el-button>
                      </div>
                    </div>
                    <div 
                      :id="`three-canvas-${post.postId}`" 
                      class="three-canvas-container"
                      :class="{ 'animation-error': animationStates[post.postId]?.error }"
                    >
                      <div v-if="animationStates[post.postId]?.loading" class="animation-loading">
                        <el-icon class="loading-icon is-loading"><Loading /></el-icon>
                        <span class="loading-text">正在加载动画...</span>
                      </div>
                      <div v-if="animationStates[post.postId]?.error" class="animation-error-placeholder">
                        <el-icon class="error-icon"><Warning /></el-icon>
                        <span class="error-text">动画加载失败</span>
                        <el-button @click="retryAnimation(post.postId)" size="small" type="text">重试</el-button>
                      </div>
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
                    :data-comment-id="comment.commentId"
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
                      <p>
                        {{ comment.content }}
                        <el-icon class="speech-icon" style="cursor:pointer; margin-left:8px; vertical-align:middle;" 
                          @click="playSpeech(comment.content, getAuthorGenderAge(comment))">
                          <svg viewBox="0 0 24 24" width="16" height="16"><path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.06c1.48-.74 2.5-2.26 2.5-4.03zm2.5 0c0 2.53-1.54 4.71-3.75 5.65v2.13c3.45-1.01 6-4.13 6-7.78s-2.55-6.77-6-7.78v2.13C17.46 7.29 19 9.47 19 12z" fill="currentColor"/></svg>
                        </el-icon>
                      </p>
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
                          <div class="reply-send-icon" @click="submitReply(comment)" :class="{ 'disabled': !comment.replyContent?.trim() }">
                            <el-icon><Promotion /></el-icon>
                          </div>
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
                          :data-reply-id="reply.commentId"
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
                            <p>
                              {{ reply.content }}
                              <el-icon class="speech-icon" style="cursor:pointer; margin-left:8px; vertical-align:middle;" 
                                @click="playSpeech(reply.content, getAuthorGenderAge(reply))">
                                <svg viewBox="0 0 24 24" width="14" height="14"><path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.06c1.48-.74 2.5-2.26 2.5-4.03zm2.5 0c0 2.53-1.54 4.71-3.75 5.65v2.13c3.45-1.01 6-4.13 6-7.78s-2.55-6.77-6-7.78v2.13C17.46 7.29 19 9.47 19 12z" fill="currentColor"/></svg>
                              </el-icon>
                            </p>
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
                      <div class="comment-send-icon" @click="submitComment(post)" :class="{ 'disabled': !post.newComment?.trim() }">
                        <el-icon><Promotion /></el-icon>
                      </div>
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
            <p>
              {{ currentThoughtsItem?.innerThoughts }}
              <el-icon class="speech-icon" style="cursor:pointer; margin-left:8px; vertical-align:middle;" 
                @click="playSpeech(currentThoughtsItem?.innerThoughts, getAuthorGenderAge(currentThoughtsItem))">
                <svg viewBox="0 0 24 24" width="16" height="16"><path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.06c1.48-.74 2.5-2.26 2.5-4.03zm2.5 0c0 2.53-1.54 4.71-3.75 5.65v2.13c3.45-1.01 6-4.13 6-7.78s-2.55-6.77-6-7.78v2.13C17.46 7.29 19 9.47 19 12z" fill="currentColor"/></svg>
              </el-icon>
            </p>
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
import { ref, computed, onMounted, nextTick, onUnmounted, watch, markRaw } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMomentsStore } from '@/stores/moments'
import { useWebSocketStore } from '@/stores/websocket'
import { useRobotStore } from '@/stores/robot'
import { ElMessageBox, ElPopover } from 'element-plus'
import { message } from '@/utils/message'
import { Plus, ChatDotRound, MoreFilled, Close, Loading, Menu, House, User, SwitchButton, Search, Star, StarFilled, View, Promotion, VideoPlay, VideoPause, Link, Picture, MagicStick, Warning, RefreshRight } from '@element-plus/icons-vue'
import { getUserAvatarUrl, getRobotAvatarUrl, handleRobotAvatarError } from '@/utils/avatar'
import { getCommentList, createComment, replyComment, deleteComment, likeComment, unlikeComment } from '@/api/comment'
import { createPost, searchPosts, getPostDetail, queryPosts } from '@/api/post'
import { tts } from '@/api/tts'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'

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

// Three.js 动画相关状态管理
const animationStates = ref({})
const threeScenes = ref({})

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
const searchTimeout = ref(null)
const isSearching = ref(false)

// 内心活动相关状态
const showInnerThoughtsDialog = ref(false)
const currentThoughtsItem = ref(null)

// 图片预览状态管理
const imagePreviewActive = ref(false)

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn)

const robotStore = useRobotStore()
const robotList = ref([])

let observer = null;
/**
 * 监听所有动态、评论、回复元素
 */
const observeAll = () => {
  if (!observer) return
  // 监听所有动态
  /* document.querySelectorAll('.post-header[data-post-id]').forEach(el => {
    el.setAttribute('data-type', 'post')
    el.setAttribute('data-id', el.getAttribute('data-post-id'))
    observer.observe(el)
  })
  // 监听所有一级评论
  document.querySelectorAll('.comment-item[data-comment-id]').forEach(el => {
    el.setAttribute('data-type', 'comment')
    el.setAttribute('data-id', el.getAttribute('data-comment-id'))
    observer.observe(el)
  })
  // 监听所有回复
  document.querySelectorAll('.reply-item[data-reply-id]').forEach(el => {
    el.setAttribute('data-type', 'reply')
    el.setAttribute('data-id', el.getAttribute('data-reply-id'))
    observer.observe(el)
  }) */
}

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
    // 使用新的统一查询接口，支持作者类型过滤
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
 * 执行刷新操作（使用新的统一查询接口）
 */
const performRefresh = async () => {
  if (isRefreshing.value) return
  try {
    isRefreshing.value = true
    refreshProgress.value = 1
    refreshRotation.value = 180
    
    // 使用新的统一查询接口刷新数据
    await momentsStore.loadPosts({}, true)
    await loadAllCommentsAndReplies()
    // 清除搜索和筛选状态
    if (searchKeyword.value.trim()) {
      searchKeyword.value = ''
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
    
    // 使用新的统一查询接口加载更多动态
    await momentsStore.loadPosts({ authorType: filterType.value })
    
    // 获取新加载的动态（store内部已处理排重）
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
  // 去除内容前后空格
  const content = newPost.value.content.trim()
  if (!content) {
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
    formData.append('content', content)
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
  // 去除内容前后空格
  const content = newPost.value.content.trim()
  if (!content) {
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
    formData.append('content', content)
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
  // 去除评论内容前后空格
  const commentContent = post.newComment.trim()
  if (!commentContent) {
    message.warning('请输入评论内容')
    return
  }
  // 防止重复提交
  if (post.submittingComment) {
    return
  }
  try {
    post.submittingComment = true
    await momentsStore.publishComment(post.postId, { content: commentContent })
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
  // 去除回复内容前后空格
  const replyContent = comment.replyContent.trim()
  if (!replyContent) {
    message.warning('请输入回复内容')
    return
  }
  // 防止重复提交
  if (comment.submittingReply) {
    return
  }
  try {
    comment.submittingReply = true
    await momentsStore.replyCommentAction(comment.commentId, { content: replyContent })
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
  // 主动移除 overflow: hidden，恢复页面滚动
  document.body.style.overflow = ''
  document.documentElement.style.overflow = ''
  document.body.classList.remove('el-popup-parent--hidden')
  console.log('图片预览结束，已恢复滚动')
}

const formatTime = (time) => {
  if (!time) return ''
  
  // 缓存时间常量
  const MINUTE = 60 * 1000
  const HOUR = 60 * MINUTE
  const DAY = 24 * HOUR
  const WEEK = 7 * DAY
  const MONTH = 30 * DAY
  
  // 优化时间解析
  let date
  if (typeof time === 'string') {
    // 检测时间格式并处理时区问题
    if (time.includes('T') && !time.includes('Z') && !time.includes('+')) {
      // 格式："2025-07-20T22:55:58.201" (无时区信息)
      // 服务器在GMT+9时区，添加+09:00后缀
      date = new Date(time + '+09:00')
    } else {
      date = new Date(time)
    }
  } else {
    date = new Date(time)
  }
  
  // 验证日期有效性
  if (isNaN(date.getTime())) {
    return '时间格式错误'
  }
  
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  // 处理未来时间
  if (diff < 0) {
    return '即将发布'
  }
  
  // 优化时间判断逻辑，按频率排序
  if (diff < MINUTE) {
    return '刚刚'
  }
  
  if (diff < HOUR) {
    const minutes = Math.floor(diff / MINUTE)
    return `${minutes}分钟前`
  }
  
  if (diff < DAY) {
    const hours = Math.floor(diff / HOUR)
    return `${hours}小时前`
  }
  
  if (diff < WEEK) {
    const days = Math.floor(diff / DAY)
    return `${days}天前`
  }
  
  if (diff < MONTH) {
    const weeks = Math.floor(diff / WEEK)
    return `${weeks}周前`
  }
  
  // 超过一个月显示具体日期，格式更友好
  const year = date.getFullYear()
  const currentYear = now.getFullYear()
  
  if (year === currentYear) {
    // 同年只显示月日
    return date.toLocaleDateString('zh-CN', { 
      month: 'numeric', 
      day: 'numeric' 
    })
  } else {
    // 不同年显示年月日
    return date.toLocaleDateString('zh-CN', { 
      year: 'numeric', 
      month: 'numeric', 
      day: 'numeric' 
    })
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
// 预处理 Three.js 代码，支持标记提取和import移除
const preprocessThreeJsCode = (code) => {
  let processedCode = code.trim()
  
  // 优先使用标记提取代码
  const startMarker = '// ===THREEJS_CODE_START==='
  const endMarker = '// ===THREEJS_CODE_END==='
  
  const startIndex = processedCode.indexOf(startMarker)
  const endIndex = processedCode.indexOf(endMarker)
  
  if (startIndex >= 0 && endIndex > startIndex) {
    // 找到标记，提取标记之间的代码
    processedCode = processedCode.substring(startIndex + startMarker.length, endIndex).trim()
    console.log('使用标记提取Three.js代码')
  } else {
    console.log('未找到代码标记，使用原始代码处理')
  }
  
  // 检查是否是完整的函数声明（以function开头并包含参数列表）
  const functionRegex = /^function\s*\([^)]*\)\s*\{([\s\S]*)\}$/
  const match = processedCode.match(functionRegex)
  
  if (match) {
    // 如果是完整函数声明，提取函数体内容
    processedCode = match[1].trim()
    console.log('检测到函数声明，已提取函数体')
  }
  
  // 移除 import 语句
  processedCode = processedCode.replace(/import\s+.*?from\s+['"][^'"]*['"];?\s*/g, '')
  
  // 移除 ES6 模块导入语法
  processedCode = processedCode.replace(/import\s*\*\s*as\s*\w+\s*from\s*['"][^'"]*['"];?\s*/g, '')
  processedCode = processedCode.replace(/import\s*\{[^}]*\}\s*from\s*['"][^'"]*['"];?\s*/g, '')
  
  // 移除markdown代码块标记
  if (processedCode.startsWith('```')) {
    const firstNewline = processedCode.indexOf('\n')
    if (firstNewline > 0) {
      processedCode = processedCode.substring(firstNewline + 1)
    }
  }
  if (processedCode.endsWith('```')) {
    processedCode = processedCode.substring(0, processedCode.lastIndexOf('```'))
  }
  
  return processedCode.trim()
}

// Three.js 动画相关函数
const initializeAnimation = async (postId, threeDSceneCode) => {
  try {
    console.log('initializeAnimation', postId, threeDSceneCode);
    // 设置加载状态
    if (!animationStates.value[postId]) {
      animationStates.value[postId] = {}
    }
    animationStates.value[postId].loading = true
    animationStates.value[postId].error = false

    const container = document.getElementById(`three-canvas-${postId}`)
    if (!container) {
      throw new Error('Canvas container not found')
    }

    // 清理之前的场景
    cleanupAnimation(postId)

    // 等待容器渲染完成
    await nextTick()

    // 检查容器尺寸
    console.log('Container dimensions:', container.clientWidth, container.clientHeight)
    const width = container.clientWidth || 400
    const height = 300

    // 创建 Three.js 场景
    const scene = new THREE.Scene()
    const camera = new THREE.PerspectiveCamera(75, width / height, 0.1, 1000)
    const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
    
    renderer.setSize(width, height)
    renderer.setClearColor(0x87ceeb, 1) // 设置天蓝色背景
    
    // 启用阴影系统以支持更精致的渲染效果
    renderer.shadowMap.enabled = true
    renderer.shadowMap.type = THREE.PCFSoftShadowMap
    
    // 启用物理正确的光照
    renderer.physicallyCorrectLights = true
    
    // 设置色调映射以获得更好的视觉效果
    renderer.toneMapping = THREE.ACESFilmicToneMapping
    renderer.toneMappingExposure = 1.0
    
    container.appendChild(renderer.domElement)

    // 预处理代码，移除 import 语句
    const processedCode = preprocessThreeJsCode(threeDSceneCode)

    // 执行生成的 Three.js 代码，提供所有必要的模块
    console.log('Executing Three.js code:', processedCode)
    let animationResult = null
    
    try {
      const animationFunction = new Function(
        'scene', 'camera', 'renderer', 'THREE', 'OrbitControls', 'GLTFLoader',
        processedCode
      )
      
      animationResult = animationFunction(
        scene, camera, renderer, THREE, 
        OrbitControls, GLTFLoader
      )
      
      console.log('Animation result:', animationResult)
      console.log('Scene children:', scene.children.length)
      
      // 如果场景为空，添加一个测试立方体
      if (scene.children.length === 0) {
        console.log('Scene is empty, adding test cube')
        const geometry = new THREE.BoxGeometry()
        const material = new THREE.MeshBasicMaterial({ color: 0xff6666 })
        const cube = new THREE.Mesh(geometry, material)
        scene.add(cube)
        camera.position.z = 5
      }
      
    } catch (error) {
      console.error('Error executing Three.js code:', error)
      // 创建一个简单的测试场景
      const geometry = new THREE.BoxGeometry()
      const material = new THREE.MeshBasicMaterial({ color: 0x00ff00 })
      const cube = new THREE.Mesh(geometry, material)
      scene.add(cube)
      camera.position.z = 5
    }

    // 存储场景和动画相关对象（使用markRaw防止响应式化）
    threeScenes.value[postId] = markRaw({
      scene,
      camera,
      renderer,
      container,
      animate: animationResult?.animate || null,
      cleanup: animationResult?.cleanup || null,
      isPlaying: false
    })

    // 立即渲染一次场景
    renderer.render(scene, camera)

    // 设置完成状态
    animationStates.value[postId].loading = false
    animationStates.value[postId].playing = false

    // 等待下一个tick，确保场景数据已存储，然后自动开始播放动画
    await nextTick()
    setTimeout(() => {
      console.log('自动播放动画:', postId)
      playAnimation(postId)
    }, 200)

  } catch (error) {
    console.error(`动画初始化失败 (Post: ${postId}):`, error)
    animationStates.value[postId].loading = false
    animationStates.value[postId].error = true
  }
}

const playAnimation = (postId) => {
  console.log('playAnimation called for:', postId)
  const sceneData = threeScenes.value[postId]
  if (!sceneData) {
    console.log('No scene data found for:', postId)
    return
  }

  console.log('Starting animation for:', postId)
  sceneData.isPlaying = true
  animationStates.value[postId].playing = true

  const animate = () => {
    if (!sceneData.isPlaying) return
    
    // 如果有自定义动画函数，调用它
    if (sceneData.animate) {
      sceneData.animate()
    }
    
    // 始终渲染场景
    sceneData.renderer.render(sceneData.scene, sceneData.camera)
    requestAnimationFrame(animate)
  }
  animate()
}

const pauseAnimation = (postId) => {
  const sceneData = threeScenes.value[postId]
  if (!sceneData) return

  sceneData.isPlaying = false
  animationStates.value[postId].playing = false
}

const resetAnimation = (postId) => {
  const post = momentsStore.posts.find(p => p.postId === postId)
  if (!post || !post.threeDSceneCode) return

  pauseAnimation(postId)
  // 重新初始化动画
  nextTick(() => {
    initializeAnimation(postId, post.threeDSceneCode)
  })
}

const retryAnimation = (postId) => {
  const post = momentsStore.posts.find(p => p.postId === postId)
  if (!post || !post.threeDSceneCode) return

  nextTick(() => {
    initializeAnimation(postId, post.threeDSceneCode)
  })
}

const cleanupAnimation = (postId) => {
  const sceneData = threeScenes.value[postId]
  if (!sceneData) return

  sceneData.isPlaying = false
  
  // 执行自定义清理函数
  if (sceneData.cleanup) {
    sceneData.cleanup()
  }

  // 清理 Three.js 对象
  if (sceneData.renderer) {
    sceneData.renderer.dispose()
    if (sceneData.container && sceneData.renderer.domElement) {
      sceneData.container.removeChild(sceneData.renderer.domElement)
    }
  }

  delete threeScenes.value[postId]
}

// WebSocket事件处理函数 - 仅在支持增量刷新时启用
let handlePostUpdate, handleCommentUpdate, handleRobotAction;

// 生命周期
onMounted(async () => {
  await robotStore.fetchRobotList()
  robotList.value = robotStore.robots
  try {
    // 使用新的统一查询接口加载初始动态列表
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

      // 自动初始化新加载的 Three.js 动画
      console.log('检查posts中的三维动画:', momentsStore.posts.length)
      momentsStore.posts.forEach(post => {
        if (post.threeDSceneCode && !threeScenes.value[post.postId]) {
          console.log('发现新的三维动画:', post.postId)
          nextTick(() => {
            initializeAnimation(post.postId, post.threeDSceneCode)
          })
        }
      })
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

  // 记录已朗读内容，避免重复
  const spokenSet = new Set()

  /**
   * 创建IntersectionObserver，监听动态、评论、回复进入视口
   */
  const createObserver = () => {
    if (observer) observer.disconnect()
    observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const el = entry.target
          const type = el.getAttribute('data-type')
          const id = el.getAttribute('data-id')
          const key = `${type}-${id}`
          if (!spokenSet.has(key)) {
            spokenSet.add(key)
            // 获取内容和作者信息
            let text = ''
            let item = null
            if (type === 'post') {
              const post = momentsStore.posts.find(p => p.postId == id)
              if (post) {
                text = post.content
                item = post
              }
            } else if (type === 'comment') {
              for (const post of momentsStore.posts) {
                const comment = (post.comments || momentsStore.comments[post.postId] || []).find(c => c.commentId == id)
                if (comment) {
                  text = comment.content
                  item = comment
                  break
                }
              }
            } else if (type === 'reply') {
              for (const post of momentsStore.posts) {
                const reply = (post.comments || momentsStore.comments[post.postId] || []).find(c => c.commentId == id)
                if (reply) {
                  text = reply.content
                  item = reply
                  break
                }
              }
            }
            if (text && item) playSpeech(text, getAuthorGenderAge(item))
          }
        }
      })
    }, { threshold: 0.5 }) // 50%进入视口时触发
  }

  createObserver()
  nextTick(() => {
    observeAll()
  })
})

watch(() => momentsStore.posts, () => {
  nextTick(() => {
    observeAll()
    
    // 自动初始化新的Three.js动画
    momentsStore.posts.forEach(post => {
      if (post.threeDSceneCode && !threeScenes.value[post.postId]) {
        console.log('在watch中发现新的三维动画:', post.postId)
        nextTick(() => {
          initializeAnimation(post.postId, post.threeDSceneCode)
        })
      }
    })
  })
}, { deep: true })

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
  
  // 清理所有 Three.js 动画
  Object.keys(threeScenes.value).forEach(postId => {
    cleanupAnimation(postId)
  })
  
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

  if (observer) observer.disconnect()
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
 * 执行搜索（使用新的统一查询接口）
 */
const performSearch = async () => {
  if (!searchKeyword.value.trim()) {
    return
  }
  
  try {
    isSearching.value = true
    
    // 构建查询参数，使用新的统一查询接口
    const params = {
      keyword: searchKeyword.value.trim(),
      page: 1,
      size: 10
    }
    
    // 如果有作者类型筛选，添加到查询参数
    if (filterType.value) {
      params.authorType = filterType.value
    }
    
    // 使用新的统一查询接口进行搜索
    const response = await queryPosts(params)
    
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
 * 清除搜索（使用新的统一查询接口）
 */
const clearSearch = async () => {
  searchKeyword.value = ''
  
  // 清除定时器
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
    searchTimeout.value = null
  }
  
  // 恢复显示所有动态，使用新的统一查询接口
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

/**
 * 根据性别和年龄推断TTS voice_type
 */
function getVoiceType(gender, age, id) {
  const index = parseInt(id.substring(6)) % 10;
  const voiceFemale = [
    'zh_female_roumeinvyou_emo_v2_mars_bigtts',
    'zh_female_meilinvyou_emo_v2_mars_bigtts',
    'zh_female_shuangkuaisisi_emo_v2_mars_bigtts',
    'zh_female_tianxinxiaomei_emo_v2_mars_bigtts',
    'zh_female_gaolengyujie_emo_v2_mars_bigtts',
    'zh_female_tianmeitaozi_mars_bigtts',
    'zh_female_qingxinnvsheng_mars_bigtts',
    'zh_female_kailangjiejie_moon_bigtts',
    'zh_female_tianmeiyueyue_moon_bigtts',
    'ICL_zh_female_wenrouwenya_tob',
  ]

  if (!gender) gender = 'female'
  if (!age) age = 20
  if (gender === 'male') {
    if (age <= 12) return 'zh_male_linjiananhai_moon_bigtts'
    if (age <= 18) return 'zh_male_linjiananhai_moon_bigtts'
    if (age <= 45) return 'zh_male_junlangnanyou_emo_v2_mars_bigtts'
    return 'ICL_zh_male_youmodaye_tob'
  } else {
    if (age <= 12) return 'zh_female_linjianvhai_moon_bigtts'
    if (age <= 18) return 'zh_female_tianxinxiaomei_emo_v2_mars_bigtts'
    if (age <= 45) return voiceFemale[index]
    return 'ICL_zh_female_heainainai_tob'
  }
}

/**
 * 过滤括号内容和标签内容，只保留非括号部分
 * @param {string} text - 原始文本
 * @returns {string} 过滤后的文本
 */
function filterBracketText(text) {
  if (!text) return ''
  // 去除所有中英文括号内的内容，包括多组
  let filteredText = text.replace(/\([^\)]*\)|（[^）]*）/g, '')
  // 去除所有标签内容（如emoji、标签等）
  filteredText = filteredText.replace(/#[^\s#]+/g, '') // 去除#标签（只有一个井号在最前）
  filteredText = filteredText.replace(/\[[^\]]*\]/g, '') // 去除[标签]
  filteredText = filteredText.replace(/【[^】]*】/g, '') // 去除【标签】
  // 清理多余的空格
  return filteredText.replace(/\s+/g, ' ').trim()
}

/**
 * 语音合成播放文本（优先TTS接口，失败降级为浏览器speechSynthesis）
 * @param {string} text - 要朗读的文本内容
 * @param {Object} [author] - 作者信息（可选），用于选择voice
 * @param {string} [author.gender] - 性别 'male' | 'female'
 * @param {number} [author.age] - 年龄
 */
const playSpeech = async (text, author = {}) => {
  if (!text || typeof text !== 'string') {
    message.warning('无可朗读内容')
    return
  }
  // 先过滤括号内容
  const textToRead = filterBracketText(text)
  if (!textToRead) {
    message.warning('无可朗读内容')
    return
  }
  // 1. 优先调用后端TTS接口
  try {
    const voiceType = getVoiceType(author.gender, author.age, author.id)
    const resp = await tts(textToRead, voiceType)
    if (resp.code === 200) {
      const data = resp.data
      const audioUrl = data.url.replace('./uploads/', '/api/v1/files/')
      const audio = new Audio(audioUrl)
      audio.play()
      return
    }
  } catch (e) {
    // TTS接口失败降级
    console.warn('TTS接口失败，降级为speechSynthesis', e)
    // 2. 降级为浏览器speechSynthesis
    if (!window.speechSynthesis) {
      message.warning('当前浏览器不支持语音朗读')
      return
    }
    window.speechSynthesis.cancel()
    const utter = new window.SpeechSynthesisUtterance(textToRead)
    utter.rate = 1
    utter.pitch = 1
    utter.volume = 1
    utter.lang = 'zh-CN'
    utter.onerror = (e) => {}
    window.speechSynthesis.speak(utter)
  }
}

/**
 * 获取作者性别和年龄
 * @param {Object} item - 动态/评论/回复对象
 * @returns {Object} { gender, age }
 */
const getAuthorGenderAge = (item) => {
  if (item.authorGender && item.authorAge) {
    return { gender: item.authorGender, age: item.authorAge, id: item.authorId }
  }
  if (item.authorType === 'robot' && item.authorId && robotList.value.length > 0) {
    const robot = robotList.value.find(r => r.id == item.authorId)
    if (robot) {
      return { gender: robot.gender || 'female', age: robot.age || 20, id: robot.id }
    }
  }
  return { gender: 'female', age: 20, id: 'robot_001' }
}

/**
 * 处理视频缩略图加载错误
 * @param {Event} event - 错误事件
 */
const handleVideoThumbnailError = (event) => {
  console.warn('视频缩略图加载失败:', event)
  // Element Plus 会自动显示 error 插槽内容
}

/**
 * 处理图片加载错误
 * @param {Event} event - 错误事件
 */
const handleImageError = (event) => {
  console.warn('图片加载失败:', event)
  // Element Plus 会自动显示 error 插槽内容
}

/**
 * 将视频URL转换为可嵌入的播放器URL
 * @param {string} url - 原始视频URL
 * @returns {string} 转换后的嵌入播放器URL
 */
const convertToEmbedUrl = (url) => {
  if (!url) return url
  
  try {
    const urlObj = new URL(url)
    const hostname = urlObj.hostname.toLowerCase()
    
    // B站视频转换
    if (hostname.includes('bilibili.com')) {
      // 匹配BV号：https://www.bilibili.com/video/BV1Mr4y1P7CX/
      const bvMatch = url.match(/\/video\/(BV[A-Za-z0-9]+)/)
      if (bvMatch) {
        const bvid = bvMatch[1]
        return `https://player.bilibili.com/player.html?isOutside=true&bvid=${bvid}&p=1&autoplay=0&danmaku=0`
      }
      
      // 匹配AV号：https://www.bilibili.com/video/av123456/
      const avMatch = url.match(/\/video\/av(\d+)/)
      if (avMatch) {
        const aid = avMatch[1]
        return `https://player.bilibili.com/player.html?isOutside=true&aid=${aid}&p=1&autoplay=0&danmaku=0`
      }
    }
    
    // YouTube视频转换
    if (hostname.includes('youtube.com') || hostname.includes('youtu.be')) {
      let videoId = ''
      
      if (hostname.includes('youtu.be')) {
        // https://youtu.be/dQw4w9WgXcQ
        videoId = urlObj.pathname.slice(1)
      } else if (urlObj.searchParams.has('v')) {
        // https://www.youtube.com/watch?v=dQw4w9WgXcQ
        videoId = urlObj.searchParams.get('v')
      }
      
      if (videoId) {
        return `https://www.youtube.com/embed/${videoId}?autoplay=0&rel=0`
      }
    }
    
    // 腾讯视频转换
    if (hostname.includes('v.qq.com')) {
      // https://v.qq.com/x/cover/abc123.html
      const vidMatch = url.match(/\/x\/cover\/([^\/\.]+)/)
      if (vidMatch) {
        const vid = vidMatch[1]
        return `https://v.qq.com/txp/iframe/player.html?vid=${vid}&auto=0`
      }
    }
    
    // 爱奇艺视频转换
    if (hostname.includes('iqiyi.com')) {
      // https://www.iqiyi.com/v_abc123.html
      const vidMatch = url.match(/\/v_([^\/\.]+)/)
      if (vidMatch) {
        const vid = vidMatch[1]
        return `https://www.iqiyi.com/iframe/${vid}`
      }
    }
    
    // 优酷视频转换
    if (hostname.includes('youku.com')) {
      // https://v.youku.com/v_show/id_XMzQ2ODY2NzQ4OA==.html
      const idMatch = url.match(/id_([^\/\.]+)/)
      if (idMatch) {
        const id = idMatch[1]
        return `https://player.youku.com/embed/${id}`
      }
    }
    
    // 西瓜视频转换
    if (hostname.includes('ixigua.com')) {
      // https://www.ixigua.com/123456789
      const idMatch = url.match(/ixigua\.com\/(\d+)/)
      if (idMatch) {
        const id = idMatch[1]
        return `https://www.ixigua.com/iframe/${id}`
      }
    }
    
    // 如果无法转换，返回原URL（可能会被X-Frame-Options阻止）
    return url
  } catch (error) {
    console.error('转换嵌入URL失败:', error)
    return url
  }
}

/**
 * 播放视频（全屏iframe模式）
 * @param {Object} linkInfo - 链接信息对象
 */
const playVideo = (linkInfo) => {
  if (!linkInfo || !linkInfo.url) {
    message.warning('视频链接无效')
    return
  }
  
  try {
    // 转换为可嵌入的播放器URL
    const embedUrl = convertToEmbedUrl(linkInfo.url)
    console.log('原始URL:', linkInfo.url)
    console.log('转换后的嵌入URL:', embedUrl)
    
    // 创建全屏容器
    const videoContainer = document.createElement('div')
    videoContainer.className = 'fullscreen-video-container'
    videoContainer.style.cssText = `
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      background: rgba(0, 0, 0, 0.9);
      z-index: 9999;
      display: flex;
      flex-direction: column;
      padding: 20px;
      box-sizing: border-box;
    `
    
    // 创建头部栏（标题和关闭按钮）
    const header = document.createElement('div')
    header.style.cssText = `
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      color: white;
      z-index: 10001;
    `
    
    // 创建标题
    const titleElement = document.createElement('div')
    titleElement.textContent = linkInfo.title || '视频播放'
    titleElement.style.cssText = `
      font-size: 18px;
      font-weight: 500;
      flex: 1;
      margin-right: 20px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    `
    
    // 创建关闭按钮
    const closeButton = document.createElement('div')
    closeButton.innerHTML = '✕'
    closeButton.style.cssText = `
      color: white;
      font-size: 24px;
      font-weight: bold;
      cursor: pointer;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 50%;
      width: 40px;
      height: 40px;
      display: flex;
      justify-content: center;
      align-items: center;
      transition: background 0.3s ease;
    `
    
    // 创建iframe容器
    const iframeContainer = document.createElement('div')
    iframeContainer.style.cssText = `
      flex: 1;
      background: black;
      border-radius: 8px;
      overflow: hidden;
      position: relative;
    `
    
    // 创建iframe
    const iframe = document.createElement('iframe')
    iframe.src = embedUrl
    iframe.style.cssText = `
      width: 100%;
      height: 100%;
      border: none;
      background: black;
    `
    
    // 设置iframe属性
    iframe.setAttribute('frameborder', '0')
    iframe.setAttribute('allowfullscreen', 'true')
    iframe.setAttribute('allow', 'accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share')
    iframe.setAttribute('referrerpolicy', 'strict-origin-when-cross-origin')
    
    // 创建加载指示器
    const loadingIndicator = document.createElement('div')
    loadingIndicator.innerHTML = `
      <div style="display: flex; flex-direction: column; align-items: center; gap: 16px;">
        <div style="width: 40px; height: 40px; border: 3px solid #f3f3f3; border-top: 3px solid #409EFF; border-radius: 50%; animation: spin 1s linear infinite;"></div>
        <div style="color: #fff; font-size: 14px;">正在加载视频...</div>
      </div>
    `
    loadingIndicator.style.cssText = `
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      z-index: 10;
    `
    
    // 创建备用提示（当iframe被阻止时显示）
    const fallbackElement = document.createElement('div')
    fallbackElement.innerHTML = `
      <div style="display: flex; flex-direction: column; align-items: center; gap: 16px; padding: 40px; text-align: center;">
        <div style="color: #409EFF; font-size: 48px;">🎬</div>
        <div style="color: #fff; font-size: 16px; font-weight: 500;">无法在此处播放视频</div>
        <div style="color: #ccc; font-size: 14px; line-height: 1.5;">
          该视频平台不支持嵌入播放<br>
          点击下方按钮在新窗口中打开视频
        </div>
        <button id="openVideo" style="
          background: #409EFF;
          color: white;
          border: none;
          padding: 10px 20px;
          border-radius: 4px;
          cursor: pointer;
          font-size: 14px;
          margin-top: 10px;
        " onmouseover="this.style.background='#66b1ff'" onmouseout="this.style.background='#409EFF'">
          在新窗口中打开视频
        </button>
      </div>
    `
    fallbackElement.style.cssText = `
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.8);
      display: none;
      justify-content: center;
      align-items: center;
      z-index: 11;
    `
    
    // 添加旋转动画
    const style = document.createElement('style')
    style.textContent = `
      @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
      }
    `
    document.head.appendChild(style)
    
    // 关闭函数
    const closeVideo = () => {
      document.body.removeChild(videoContainer)
      document.body.style.overflow = ''
      document.head.removeChild(style)
    }
    
    // 添加事件监听
    closeButton.addEventListener('click', (e) => {
      e.stopPropagation()
      closeVideo()
    })
    
    closeButton.addEventListener('mouseenter', () => {
      closeButton.style.background = 'rgba(255, 255, 255, 0.2)'
    })
    
    closeButton.addEventListener('mouseleave', () => {
      closeButton.style.background = 'rgba(255, 255, 255, 0.1)'
    })
    
    videoContainer.addEventListener('click', (e) => {
      if (e.target === videoContainer) {
        closeVideo()
      }
    })
    
    // 阻止iframe容器点击事件冒泡
    iframeContainer.addEventListener('click', (e) => {
      e.stopPropagation()
    })
    
    // ESC键关闭
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') {
        closeVideo()
        document.removeEventListener('keydown', handleKeyDown)
      }
    }
    document.addEventListener('keydown', handleKeyDown)
    
    // iframe加载完成后隐藏加载指示器
    iframe.addEventListener('load', () => {
      if (loadingIndicator.parentNode) {
        loadingIndicator.remove()
      }
    })
    
    // iframe加载出错处理
    iframe.addEventListener('error', () => {
      if (loadingIndicator.parentNode) {
        loadingIndicator.remove()
      }
      fallbackElement.style.display = 'flex'
    })
    
    // 检测iframe是否被X-Frame-Options阻止
    setTimeout(() => {
      try {
        // 尝试访问iframe内容，如果被阻止会抛出异常
        iframe.contentDocument
      } catch (e) {
        if (loadingIndicator.parentNode) {
          loadingIndicator.remove()
        }
        fallbackElement.style.display = 'flex'
      }
    }, 3000) // 3秒后检测
    
    // 组装元素
    header.appendChild(titleElement)
    header.appendChild(closeButton)
    
    iframeContainer.appendChild(loadingIndicator)
    iframeContainer.appendChild(iframe)
    iframeContainer.appendChild(fallbackElement)
    
    videoContainer.appendChild(header)
    videoContainer.appendChild(iframeContainer)
    
    // 添加到页面
    document.body.appendChild(videoContainer)
    document.body.style.overflow = 'hidden'
    
    // 为备用按钮添加事件监听
    setTimeout(() => {
      const openVideoBtn = document.getElementById('openVideo')
      if (openVideoBtn) {
        openVideoBtn.addEventListener('click', () => {
          window.open(linkInfo.url, '_blank')
        })
      }
    }, 100)
    
    console.log('开始加载视频页面:', embedUrl)
  } catch (error) {
    console.error('播放视频失败:', error)
    message.error('播放视频失败')
  }
}

/**
 * 从URL中提取视频源名称
 * @param {string} url - 视频URL
 * @returns {string} 视频源名称
 */
const getVideoSource = (url) => {
  if (!url) return '未知来源'
  
  try {
    const hostname = new URL(url).hostname.toLowerCase()
    
    if (hostname.includes('bilibili.com') || hostname.includes('b23.tv')) {
      return 'B站'
    } else if (hostname.includes('youtube.com') || hostname.includes('youtu.be')) {
      return 'YouTube'
    } else if (hostname.includes('douyin.com')) {
      return '抖音'
    } else if (hostname.includes('kuaishou.com')) {
      return '快手'
    } else if (hostname.includes('ixigua.com')) {
      return '西瓜视频'
    } else if (hostname.includes('qq.com')) {
      return '腾讯视频'
    } else if (hostname.includes('iqiyi.com')) {
      return '爱奇艺'
    } else if (hostname.includes('youku.com')) {
      return '优酷'
    } else if (hostname.includes('sohu.com')) {
      return '搜狐视频'
    } else {
      return hostname
    }
  } catch (error) {
    console.error('解析视频源失败:', error)
    return '未知来源'
  }
}
</script>

<style scoped>
@import url('../styles/moment.scss');

/* 视频链接展示样式 */
.post-video-link {
  margin-top: 12px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  overflow: hidden;
  background: var(--color-card);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.post-video-link:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
  border-color: var(--color-primary);
}

.video-card {
  cursor: pointer;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.video-thumbnail {
  position: relative;
  width: 100%;
  height: 200px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  overflow: hidden;
}

.thumbnail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.video-card:hover .thumbnail-image {
  transform: scale(1.08);
}

/* 视频缩略图占位符 - 高级设计 */
.video-thumbnail-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 30%, #0f3460 100%);
  color: white;
  gap: 12px;
  position: relative;
  z-index: 1;
  overflow: hidden;
  
  /* 添加动态背景效果 */
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
    animation: shimmer 4s ease-in-out infinite;
    pointer-events: none;
  }
  
  /* 添加网格纹理效果 */
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image: 
      linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px);
    background-size: 20px 20px;
    pointer-events: none;
    opacity: 0.5;
  }
}

.video-icon {
  font-size: 48px;
  opacity: 0.95;
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 50%;
  backdrop-filter: blur(12px);
  border: 2px solid rgba(255, 255, 255, 0.25);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 8px 24px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  cursor: pointer;
  
  /* 添加脉冲效果 */
  &::before {
    content: '';
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.3);
    opacity: 0;
    animation: videoPulse 2s ease-in-out infinite;
    pointer-events: none;
  }
  
  &:hover {
    transform: scale(1.1);
    background: rgba(255, 255, 255, 0.18);
    border-color: rgba(255, 255, 255, 0.4);
    box-shadow: 
      0 12px 32px rgba(0, 0, 0, 0.4),
      inset 0 1px 0 rgba(255, 255, 255, 0.15);
    
    &::before {
      opacity: 1;
    }
  }
  
  .el-icon {
    font-size: 32px;
    margin-left: 3px; /* 微调播放按钮位置 */
    filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.4));
    color: rgba(255, 255, 255, 0.95);
    transition: all 0.2s ease;
  }
  
  &:hover .el-icon {
    color: white;
    filter: drop-shadow(0 3px 12px rgba(0, 0, 0, 0.5));
  }
}

.video-placeholder-text {
  font-size: 15px;
  font-weight: 600;
  opacity: 0.9;
  letter-spacing: 0.8px;
  text-align: center;
  color: white;
  position: relative;
  z-index: 2;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 18px;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  
  &::before {
    content: '🎬';
    margin-right: 6px;
    font-size: 16px;
    filter: drop-shadow(0 1px 3px rgba(0, 0, 0, 0.3));
  }
}

/* 播放按钮 */
.play-button {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 64px;
  height: 64px;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(8px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.play-button:hover {
  background: rgba(0, 0, 0, 0.85);
  transform: translate(-50%, -50%) scale(1.15);
  border-color: rgba(255, 255, 255, 0.5);
}

.play-icon {
  color: white;
  font-size: 26px;
  margin-left: 3px; /* 微调播放图标位置 */
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

/* 视频标识角标 */
.video-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  color: white;
  padding: 4px 8px;
  border-radius: 16px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 2px;
  opacity: 0.9;
}

.badge-icon {
  font-size: 12px;
}

/* 视频信息区域 */
.video-info {
  padding: 16px;
  background: var(--color-card);
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.video-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 42px; /* 确保两行文本的最小高度 */
}

.video-source {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: var(--color-primary);
  gap: 6px;
  font-weight: 500;
  margin-top: auto;
}

.source-icon {
  font-size: 14px;
  opacity: 0.8;
}

.source-text {
  letter-spacing: 0.3px;
}

/* 视频占位符动画 */
@keyframes videoPulse {
  0%, 100% {
    opacity: 0;
    transform: scale(1);
  }
  50% {
    opacity: 0.3;
    transform: scale(1.1);
  }
}

/* 暗黑模式样式 */
html.dark .video-thumbnail {
  background: linear-gradient(135deg, #2d3748 0%, #4a5568 100%);
}

html.dark .video-thumbnail-placeholder {
  background: linear-gradient(135deg, #1f2937 0%, #374151 30%, #4b5563 100%) !important;
}

html.dark .play-button {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.2);
}

html.dark .play-button:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.4);
}

html.dark .video-badge {
  background: rgba(255, 255, 255, 0.15);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .video-thumbnail {
    height: 180px;
  }
  
  .play-button {
    width: 56px;
    height: 56px;
  }
  
  .play-icon {
    font-size: 22px;
  }
  
  .video-icon {
    font-size: 40px;
  }
  
  .video-info {
    padding: 14px;
  }
  
  .video-title {
    font-size: 14px;
    min-height: 38px;
  }
  
  .video-source {
    font-size: 12px;
  }
  
  /* 修复移动端视频缩略图占位符显示 */
  .video-thumbnail-placeholder {
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 30%, #0f3460 100%) !important;
    color: white !important;
    min-height: 180px;
    display: flex !important;
    flex-direction: column !important;
    align-items: center !important;
    justify-content: center !important;
  }
  
  .thumbnail-image {
    background: transparent !important;
  }
  
  .video-placeholder-text {
    font-size: 13px !important;
    font-weight: 600 !important;
    padding: 5px 12px !important;
  }
  
  .video-icon {
    width: 60px !important;
    height: 60px !important;
    
    .el-icon {
      font-size: 28px !important;
    }
  }
}

@media (max-width: 480px) {
  .video-thumbnail {
    height: 160px;
  }
  
  .play-button {
    width: 48px;
    height: 48px;
  }
  
  .play-icon {
    font-size: 20px;
    margin-left: 2px;
  }
  
  .video-info {
    padding: 12px;
  }
  
  .video-title {
    font-size: 13px;
    min-height: 36px;
  }
  
  /* 确保超小屏幕下的视频缩略图占位符正确显示 */
  .video-thumbnail-placeholder {
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 30%, #0f3460 100%) !important;
    color: white !important;
    min-height: 160px;
    display: flex !important;
    flex-direction: column !important;
    align-items: center !important;
    justify-content: center !important;
  }
  
  .video-placeholder-text {
    font-size: 12px !important;
    font-weight: 600 !important;
    padding: 4px 10px !important;
  }
  
  .video-icon {
    width: 56px !important;
    height: 56px !important;
    
    .el-icon {
      font-size: 26px !important;
    }
  }
}

/* Three.js 动画展示样式 */
.post-animation {
  margin-top: 16px;
  border: 1px solid rgba(var(--el-border-color-light-rgb), 0.8);
  border-radius: 12px;
  background: linear-gradient(135deg, 
    rgba(var(--el-color-primary-rgb), 0.03) 0%, 
    rgba(var(--el-color-primary-rgb), 0.01) 100%);
  overflow: hidden;
  transition: all 0.3s ease;
}

.post-animation:hover {
  border-color: rgba(var(--el-color-primary-rgb), 0.3);
  box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb), 0.1);
}

.animation-container {
  display: flex;
  flex-direction: column;
}

.animation-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: rgba(var(--el-color-primary-rgb), 0.05);
  border-bottom: 1px solid rgba(var(--el-border-color-light-rgb), 0.5);
}

.animation-icon {
  color: var(--el-color-primary);
  margin-right: 8px;
  font-size: 16px;
}

.animation-title {
  color: var(--el-color-primary);
  font-weight: 600;
  font-size: 14px;
  flex: 1;
  letter-spacing: 0.5px;
}

.animation-controls {
  display: flex;
  gap: 8px;
}

.three-canvas-container {
  position: relative;
  width: 100%;
  height: 300px;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 50%, #0a0a0a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.three-canvas-container canvas {
  display: block;
  width: 100% !important;
  height: 100% !important;
  object-fit: contain;
}

.animation-loading,
.animation-error-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.animation-loading .loading-icon,
.animation-error-placeholder .error-icon {
  font-size: 24px;
}

.animation-loading .loading-icon {
  color: var(--el-color-primary);
}

.animation-error-placeholder .error-icon {
  color: var(--el-color-danger);
}

.animation-error-placeholder .el-button {
  margin-top: 8px;
  color: var(--el-color-primary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .animation-header {
    padding: 10px 12px;
  }
  
  .animation-title {
    font-size: 13px;
  }
  
  .animation-controls .el-button {
    padding: 6px;
  }
  
  .three-canvas-container {
    height: 250px;
  }
}

/* 图片占位符样式 */
.image-loading-placeholder,
.image-error-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 12px;
  color: var(--color-text);
  background: var(--color-card);
  border: 1px dashed var(--color-border);
}

.image-loading-placeholder {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(64, 158, 255, 0.05));
  border-color: rgba(64, 158, 255, 0.3);
  color: rgba(64, 158, 255, 0.8);
}

.image-error-placeholder {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.1), rgba(245, 108, 108, 0.05));
  border-color: rgba(245, 108, 108, 0.3);
  color: rgba(245, 108, 108, 0.8);
}

.loading-icon,
.error-icon {
  font-size: 24px;
  opacity: 0.8;
}

.loading-text,
.error-text {
  font-size: 12px;
  font-weight: 500;
  opacity: 0.9;
  text-align: center;
  letter-spacing: 0.3px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .loading-icon,
  .error-icon {
    font-size: 20px;
  }
  
  .loading-text,
  .error-text {
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .loading-icon,
  .error-icon {
    font-size: 18px;
  }
  
  .loading-text,
  .error-text {
    font-size: 10px;
  }
}
</style>

<style>
html.dark .inner-thoughts-dialog .el-dialog {
  background: #23272e !important;
  color: #f0f0f0 !important;
}
html.dark .inner-thoughts-dialog .el-dialog__header {
  background: #23272e !important;
  color: #f0f0f0 !important;
}
html.dark .inner-thoughts-dialog .el-dialog__body {
  background: #23272e !important;
  color: #f0f0f0 !important;
}
html.dark .inner-thoughts-dialog .el-dialog__title {
  color: #f0f0f0 !important;
}
html.dark .inner-thoughts-dialog .el-dialog__footer {
  background: #23272e !important;
}
</style> 