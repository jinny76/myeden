import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getPostList, createPost, deletePost, likePost, unlikePost } from '@/api/post'
import { getCommentList, createComment, replyComment, deleteComment, likeComment, unlikeComment } from '@/api/comment'

/**
 * 朋友圈状态管理
 * 
 * 功能说明：
 * - 管理动态列表、评论列表状态
 * - 提供动态发布、删除、点赞功能
 * - 提供评论发布、回复、删除功能
 * - 支持分页加载和缓存管理
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
export const useMomentsStore = defineStore('moments', () => {
  // 状态定义
  const posts = ref([]) // 动态列表
  const comments = ref({}) // 评论列表，按动态ID分组
  const loading = ref(false) // 加载状态
  const currentPage = ref(1) // 当前页码
  const pageSize = ref(10) // 每页大小
  const total = ref(0) // 总数
  const hasMore = ref(true) // 是否有更多数据
  
  // 计算属性
  const totalPages = computed(() => Math.ceil(total.value / pageSize.value))
  
  /**
   * 加载动态列表
   * @param {Object} params - 查询参数
   * @param {boolean} refresh - 是否刷新（重置列表）
   */
  const loadPosts = async (params = {}, refresh = false) => {
    try {
      loading.value = true
      
      const queryParams = {
        page: refresh ? 1 : currentPage.value + 1,
        size: pageSize.value,
        ...params
      }
      
      const response = await getPostList(queryParams)
      
      if (response.code === 200) {
        const { posts: newPosts, total: totalCount, page, size } = response.data
        
        if (refresh) {
          posts.value = newPosts
          currentPage.value = 1
        } else {
          posts.value.push(...newPosts)
        }
        
        total.value = totalCount
        currentPage.value = page
        pageSize.value = size
        hasMore.value = page < Math.ceil(totalCount / size)
      }
    } catch (error) {
      console.error('加载动态列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 发布动态
   * @param {Object} data - 动态数据
   */
  const publishPost = async (data) => {
    try {
      const response = await createPost(data)
      
      if (response.code === 200) {
        // 刷新动态列表
        await loadPosts({}, true)
        return response.data
      }
    } catch (error) {
      console.error('发布动态失败:', error)
      throw error
    }
  }
  
  /**
   * 删除动态
   * @param {string} postId - 动态ID
   */
  const removePost = async (postId) => {
    try {
      const response = await deletePost(postId)
      
      if (response.code === 200) {
        // 从列表中移除
        const index = posts.value.findIndex(post => post.postId === postId)
        if (index > -1) {
          posts.value.splice(index, 1)
        }
        return true
      }
    } catch (error) {
      console.error('删除动态失败:', error)
      throw error
    }
  }
  
  /**
   * 点赞动态
   * @param {string} postId - 动态ID
   */
  const likePostAction = async (postId) => {
    try {
      const response = await likePost(postId)
      
      if (response.code === 200) {
        // 更新本地状态
        const post = posts.value.find(p => p.postId === postId)
        if (post) {
          post.likeCount++
          post.isLiked = true
        }
        return true
      }
    } catch (error) {
      console.error('点赞动态失败:', error)
      throw error
    }
  }
  
  /**
   * 取消点赞动态
   * @param {string} postId - 动态ID
   */
  const unlikePostAction = async (postId) => {
    try {
      const response = await unlikePost(postId)
      
      if (response.code === 200) {
        // 更新本地状态
        const post = posts.value.find(p => p.postId === postId)
        if (post && post.likeCount > 0) {
          post.likeCount--
          post.isLiked = false
        }
        return true
      }
    } catch (error) {
      console.error('取消点赞动态失败:', error)
      throw error
    }
  }
  
  /**
   * 加载评论列表
   * @param {string} postId - 动态ID
   * @param {Object} params - 查询参数
   * @param {boolean} refresh - 是否刷新
   */
  const loadComments = async (postId, params = {}, refresh = false) => {
    try {
      const queryParams = {
        page: 1,
        size: 20,
        ...params
      }
      
      const response = await getCommentList(postId, queryParams)
      
      if (response.code === 200) {
        const { comments: newComments } = response.data
        
        if (refresh) {
          comments.value[postId] = newComments
        } else {
          if (!comments.value[postId]) {
            comments.value[postId] = []
          }
          comments.value[postId].push(...newComments)
        }
      }
    } catch (error) {
      console.error('加载评论列表失败:', error)
      throw error
    }
  }
  
  /**
   * 发表评论
   * @param {string} postId - 动态ID
   * @param {Object} data - 评论数据
   */
  const publishComment = async (postId, data) => {
    try {
      const response = await createComment(postId, data)
      
      if (response.code === 200) {
        // 刷新评论列表
        await loadComments(postId, {}, true)
        
        // 不再自动更新评论数，由前端根据实际加载的评论数量更新
        // 这样可以确保评论数与实际显示的评论数量一致
        
        return response.data
      }
    } catch (error) {
      console.error('发表评论失败:', error)
      throw error
    }
  }
  
  /**
   * 回复评论
   * @param {string} commentId - 评论ID
   * @param {Object} data - 回复数据
   */
  const replyCommentAction = async (commentId, data) => {
    try {
      const response = await replyComment(commentId, data)
      
      if (response.code === 200) {
        // 找到对应的动态ID并刷新评论列表
        for (const [postId, commentList] of Object.entries(comments.value)) {
          const comment = commentList.find(c => c.commentId === commentId)
          if (comment) {
            await loadComments(postId, {}, true)
            break
          }
        }
        
        return response.data
      }
    } catch (error) {
      console.error('回复评论失败:', error)
      throw error
    }
  }
  
  /**
   * 删除评论
   * @param {string} commentId - 评论ID
   */
  const removeComment = async (commentId) => {
    try {
      const response = await deleteComment(commentId)
      
      if (response.code === 200) {
        // 从评论列表中移除
        for (const [postId, commentList] of Object.entries(comments.value)) {
          const index = commentList.findIndex(c => c.commentId === commentId)
          if (index > -1) {
            commentList.splice(index, 1)
            
            // 不再自动更新评论数，由前端根据实际加载的评论数量更新
            // 这样可以确保评论数与实际显示的评论数量一致
            break
          }
        }
        return true
      }
    } catch (error) {
      console.error('删除评论失败:', error)
      throw error
    }
  }
  
  /**
   * 点赞评论
   * @param {string} commentId - 评论ID
   */
  const likeCommentAction = async (commentId) => {
    try {
      const response = await likeComment(commentId)
      
      if (response.code === 200) {
        // 更新本地状态
        for (const commentList of Object.values(comments.value)) {
          const comment = commentList.find(c => c.commentId === commentId)
          if (comment) {
            comment.likeCount++
            comment.isLiked = true
            break
          }
        }
        return true
      }
    } catch (error) {
      console.error('点赞评论失败:', error)
      throw error
    }
  }
  
  /**
   * 取消点赞评论
   * @param {string} commentId - 评论ID
   */
  const unlikeCommentAction = async (commentId) => {
    try {
      const response = await unlikeComment(commentId)
      
      if (response.code === 200) {
        // 更新本地状态
        for (const commentList of Object.values(comments.value)) {
          const comment = commentList.find(c => c.commentId === commentId)
          if (comment && comment.likeCount > 0) {
            comment.likeCount--
            comment.isLiked = false
            break
          }
        }
        return true
      }
    } catch (error) {
      console.error('取消点赞评论失败:', error)
      throw error
    }
  }
  
  /**
   * 重置状态
   */
  const reset = () => {
    posts.value = []
    comments.value = {}
    loading.value = false
    currentPage.value = 1
    total.value = 0
    hasMore.value = true
  }
  
  return {
    // 状态
    posts,
    comments,
    loading,
    currentPage,
    pageSize,
    total,
    hasMore,
    
    // 计算属性
    totalPages,
    
    // 方法
    loadPosts,
    publishPost,
    removePost,
    likePostAction,
    unlikePostAction,
    loadComments,
    publishComment,
    replyCommentAction,
    removeComment,
    likeCommentAction,
    unlikeCommentAction,
    reset
  }
}) 