<template>
  <div class="chat-window">
    <div class="chat-header">
      <div class="header-left">
        <el-button @click="goToWorld" circle>
          <el-icon>
            <Back />
          </el-icon>
        </el-button>
        <span style="margin-left: 10px;">{{ chatRoom?.roomName || '我的聊天室' }}</span>
      </div>
      <div class="header-actions">
        <el-button @click="showManageModal = true" type="primary" size="small">
          <el-icon>
            <Setting />
          </el-icon>
          管理
        </el-button>
      </div>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div v-for="message in messages" :key="message.id" class="chat-message" :class="message.senderType.toLowerCase()">
        <el-avatar :src="message.senderAvatar || getDefaultAvatar(message.senderType)" />
        <div class="message-content">
          {{ message.content }}
        </div>
      </div>

      <!-- 加载更多 -->
      <div v-if="hasMore && !loading" class="load-more">
        <el-button @click="loadMoreMessages" text>加载更多消息</el-button>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="loading-messages">
        <el-icon class="is-loading">
          <Loading />
        </el-icon>
        加载中...
      </div>
    </div>
    <div class="chat-input">
      <el-input v-model="newMessage" @keyup.enter="sendMessage" placeholder="输入消息..." ref="inputRef" />
      <button v-if="newMessage.trim()" @click="sendMessage" class="send-btn" title="发送消息">
        <el-icon>
          <Position />
        </el-icon>
      </button>
      <!-- 添加机器人按钮 -->
      <span class="add-robot-icon" @click="showManageModal = true" title="添加机器人">
        <el-icon>
          <User />
        </el-icon>
      </span>
    </div>
  </div>

  <!-- 聊天室管理弹窗 -->
  <ChatRoomManageModal v-model="showManageModal" :chat-room="chatRoom" :members="members"
    @room-updated="handleRoomUpdated" @member-added="handleMemberAdded" @member-removed="handleMemberRemoved" />
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting, ChatLineSquare, Loading, Close, CircleCheck,
  StarFilled, User, Picture, Back, Position
} from '@element-plus/icons-vue'
import AppHeader from '@/components/AppHeader.vue'
import ChatRoomManageModal from '@/components/ChatRoomManageModal.vue'
import { useChatRoomStore } from '@/stores/chatroom'
import { useUserStore } from '@/stores/user'
import { useRobotStore } from '@/stores/robot'
import { useWebSocketStore } from '@/stores/websocket'
import { formatTime } from '@/utils/time'

const route = useRoute()
const router = useRouter()
const chatroomStore = useChatRoomStore()
const userStore = useUserStore()
const robotStore = useRobotStore()
const wsStore = useWebSocketStore()

// 响应式数据
const chatRoom = ref(null)
const messages = ref([])
const members = ref([])
const newMessage = ref('')
const replyingTo = ref(null)
const showMembers = ref(false)
const showManageModal = ref(false)
const loading = ref(false)
const sending = ref(false)
const hasMore = ref(true)
const messagesContainer = ref(null)
const heartbeatTimer = ref(null)
const onlineCount = ref(0)

// 计算属性
const memberCount = computed(() => members.value.length)
const canSendMessage = computed(() =>
  newMessage.value.trim().length > 0 && !sending.value
)

// 初始化聊天室
onMounted(async () => {
  try {
    const roomId = route.params.roomId || userStore.userInfo?.userId
    if (!roomId) {
      ElMessage.error('无效的聊天室ID')
      router.push('/world')
      return
    }

    // 创建或获取聊天室
    chatRoom.value = await chatroomStore.createOrGetChatRoom()

    // 标记用户进入聊天室（触发高频模式）
    await enterChatRoom()

    // 加载成员列表
    await loadMembers()

    // 加载消息历史
    await loadMessages()

    // 监听WebSocket消息
    setupWebSocketListeners()

    // 启动心跳机制
    startHeartbeat()

    // 滚动到底部
    nextTick(() => {
      scrollToBottom()
    })

  } catch (error) {
    console.error('初始化聊天室失败:', error)
    ElMessage.error('聊天室初始化失败')
  }
})

// 清理
onUnmounted(async () => {
  // 清理心跳定时器
  stopHeartbeat()
  
  // 标记用户离开聊天室（可能触发低频模式）
  await leaveChatRoom()
  
  // 清理WebSocket监听器
  if (chatRoom.value?.chatSubscriptionId) {
    wsStore.unsubscribe(chatRoom.value.chatSubscriptionId)
  }
  if (chatRoom.value?.memberSubscriptionId) {
    wsStore.unsubscribe(chatRoom.value.memberSubscriptionId)
  }
})

// 监听消息变化，自动滚动到底部
watch(messages, () => {
  nextTick(() => {
    scrollToBottom()
  })
}, { deep: true })

// 加载消息
const loadMessages = async (page = 0) => {
  try {
    loading.value = true

    // 确保机器人store已加载数据
    if (robotStore.robots.length === 0) {
      await robotStore.fetchRobotList()
    }

    const response = await chatroomStore.getChatHistory(chatRoom.value.roomId, page, 20)

    if (page === 0) {
      messages.value = response.content.reverse()
    } else {
      messages.value.unshift(...response.content.reverse())
    }

    // 根据发送者类型设置头像
    messages.value.forEach(message => {
      message.senderAvatar = getSenderAvatar(message)
    });

    hasMore.value = !response.last

  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败')
  } finally {
    loading.value = false
  }
}

// 加载更多消息
const loadMoreMessages = async () => {
  const currentPage = Math.floor(messages.value.length / 20)
  await loadMessages(currentPage)
}

// 加载成员列表
const loadMembers = async () => {
  try {
    members.value = await chatroomStore.getChatRoomMembers(chatRoom.value.roomId)
  } catch (error) {
    console.error('加载成员列表失败:', error)
  }
}

// 发送消息
const sendMessage = async () => {
  if (!canSendMessage.value) return

  try {
    sending.value = true

    const messageData = {
      content: newMessage.value.trim(),
      replyToId: replyingTo.value?.id
    }

    await chatroomStore.sendMessage(chatRoom.value.roomId, messageData)

    // 清空输入
    newMessage.value = ''
    replyingTo.value = null

  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  } finally {
    sending.value = false
  }
}

// 处理回车键
const handleEnterKey = (event) => {
  if (event.ctrlKey) {
    sendMessage()
  } else {
    // 单独按回车键时换行（默认行为）
  }
}

// 回复消息
const replyToMessage = (message) => {
  replyingTo.value = message
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
}

// 处理图片选择
const handleImageSelect = (file) => {
  // TODO: 实现图片上传和发送
  console.log('选择图片:', file)
}

// 获取默认头像
const getDefaultAvatar = (senderType) => {
  return senderType === 'ROBOT' ? '/default-robot-avatar.png' : '/default-user-avatar.png'
}

// 根据发送者类型获取头像
const getSenderAvatar = (message) => {
  if (!message) return null

  // 如果消息已经有头像，直接返回
  if (message.senderAvatar && message.senderAvatar.indexOf('/api/v1/files') > -1) {
    return message.senderAvatar
  }

  // 根据发送者类型获取头像
  if (message.senderType === 'USER') {
    // 从用户store获取头像
    if (message.senderId === userStore.userId) {
      // 当前用户
      return userStore.avatar ? `/api/v1/files${userStore.avatar.replaceAll('/uploads/', '/')}` : null
    } else {
      // 其他用户，可以从members中查找
      const member = members.value.find(m => m.memberId === message.senderId && m.memberType === 'USER')
      if (member && member.user && member.user.avatar) {
        return `/api/v1/files${member.user.avatar.replaceAll('/uploads/', '/')}`
      }
    }
  } else if (message.senderType === 'ROBOT') {
    // 从机器人store获取头像
    const robot = robotStore.robots.find(r => r.id === message.senderId || r.robotId === message.senderId)
    if (robot && robot.avatar) {
      return `/api/v1/files${robot.avatar.replaceAll('/uploads/', '/')}`
    }

    // 如果从store中找不到，尝试从members中查找
    const member = members.value.find(m => m.memberId === message.senderId && m.memberType === 'ROBOT')
    if (member && member.robot && member.robot.avatar) {
      return `/api/v1/files${member.robot.avatar.replaceAll('/uploads/', '/')}`
    }
  }

  return null
}

// 返回世界页面
const goToWorld = () => {
  router.push('/world')
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 设置WebSocket监听器
const setupWebSocketListeners = () => {
  // 监听聊天室消息
  const chatSubscriptionId = wsStore.subscribe('/topic/chatroom/' + chatRoom.value.roomId, (data) => {
    // 检查是否是聊天室消息
    if (data.data && data.data.roomId === chatRoom.value.roomId) {
      // 为新消息设置头像
      const newMessage = { ...data.data }
      newMessage.senderAvatar = getSenderAvatar(newMessage)
      
      messages.value.push(newMessage)
    }
  })
  
  // 监听成员更新
  const memberSubscriptionId = wsStore.subscribe('/topic/chatroom/members/' + chatRoom.value.roomId, (data) => {
    if (data.roomId === chatRoom.value.roomId) {
      loadMembers()
    }
  })
  
  // 保存订阅ID以便清理
  chatRoom.value.chatSubscriptionId = chatSubscriptionId
  chatRoom.value.memberSubscriptionId = memberSubscriptionId
}

// 聊天室管理事件处理
const handleRoomUpdated = (updatedRoom) => {
  chatRoom.value = updatedRoom
}

const handleMemberAdded = (member) => {
  // 重新加载成员列表以确保数据同步
  loadMembers()
}

const handleMemberRemoved = (memberId) => {
  const member = members.value.find(m => m.memberId === memberId)
  if (member) {
    members.value = members.value.filter(m => m.memberId !== memberId)
    ElMessage.success(`${member.memberNickname} 离开了聊天室`)
  }
}

// 用户进入聊天室（触发高频模式）
const enterChatRoom = async () => {
  try {
    const roomId = chatRoom.value?.roomId
    if (!roomId) return
    
    const response = await chatroomStore.enterChatRoom(roomId)
    if (response.success) {
      onlineCount.value = response.data.onlineCount
      console.log(`进入聊天室成功，在线用户数: ${onlineCount.value}`)
    }
  } catch (error) {
    console.error('进入聊天室失败:', error)
  }
}

// 用户离开聊天室（可能触发低频模式）
const leaveChatRoom = async () => {
  try {
    const roomId = chatRoom.value?.roomId
    if (!roomId) return
    
    const response = await chatroomStore.leaveChatRoom(roomId)
    if (response.success) {
      onlineCount.value = response.data.onlineCount
      console.log(`离开聊天室成功，在线用户数: ${onlineCount.value}`)
    }
  } catch (error) {
    console.error('离开聊天室失败:', error)
  }
}

// 启动心跳机制
const startHeartbeat = () => {
  // 每30秒发送一次心跳
  heartbeatTimer.value = setInterval(async () => {
    try {
      const roomId = chatRoom.value?.roomId
      if (!roomId) return
      
      const response = await chatroomStore.sendHeartbeat(roomId)
      if (response.success) {
        onlineCount.value = response.data.onlineCount
      }
    } catch (error) {
      console.error('心跳发送失败:', error)
    }
  }, 30000) // 30秒间隔
}

// 停止心跳机制
const stopHeartbeat = () => {
  if (heartbeatTimer.value) {
    clearInterval(heartbeatTimer.value)
    heartbeatTimer.value = null
  }
}
</script>

<style scoped>
.chat-window {
  max-width: 600px;
  margin: 80px auto 0 auto;
  background: #181c20;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  min-height: calc(100dvh - 144px);
  /* 减去header高度(64px)和margin-top(80px) */
  height: calc(100dvh - 144px);
  color: #e0e0e0;
}

.chat-header {
  flex-shrink: 0;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid #23272e;
  background: #23272e;
  z-index: 2;
}

.chat-header .header-left {
  display: flex;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.chat-messages {
  flex: 1 1 0;
  overflow-y: auto;
  padding: 16px;
  padding-top: 56px;
  margin-top: -40px;
  background: #181c20;
  scrollbar-width: thin;
  scrollbar-color: #444 #23272e;
  width: 100%;
  box-sizing: border-box;
}

.chat-message {
  display: flex;
  align-items: flex-start;
  margin-bottom: 18px;
  gap: 10px;
}

.chat-message.user {
  flex-direction: row-reverse;
}

.el-avatar {
  border: 2px solid #2c3136;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.message-content {
  max-width: 70%;
  word-break: break-all;
  font-size: 1.05rem;
  line-height: 1.7;
  padding: 10px 18px;
  border-radius: 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.10);
  transition: background 0.2s;
}

.chat-message.user .message-content {
  background: linear-gradient(135deg, #3eb575 0%, #1eae98 100%);
  color: #fff;
  border-bottom-right-radius: 6px;
  border-bottom-left-radius: 18px;
  border-top-left-radius: 18px;
  border-top-right-radius: 18px;
  align-self: flex-end;
}

.chat-message.robot .message-content {
  background: #23272b;
  color: #e0e0e0;
  border-bottom-left-radius: 6px;
  border-bottom-right-radius: 18px;
  border-top-left-radius: 18px;
  border-top-right-radius: 18px;
  align-self: flex-start;
  border: 1px solid #23272b;
  position: relative;
}

.system-message {
  text-align: center;
  color: #aaa;
  font-size: 0.95rem;
  margin: 8px 0;
}

.chat-input {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  padding: 16px;
  background: #23272e;
  border-top: 1px solid #23272e;
  border-radius: 0 0 16px 16px;
  gap: 8px;
  width: 100%;
  box-sizing: border-box;
}

.el-input {
  flex: 1;
}

.el-input__wrapper {
  background: #23272b !important;
  border: 1px solid #2c3136 !important;
  color: #fff !important;
  border-radius: 12px !important;
}

.el-input__inner {
  background: transparent !important;
  color: #fff !important;
  font-size: 1rem;
}

.el-input__inner::placeholder {
  color: #888 !important;
  opacity: 0.7;
}

.send-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #3eb575 0%, #1eae98 100%);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(62, 181, 117, 0.3);
  position: relative;
  overflow: hidden;
}

.send-btn:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 6px 20px rgba(62, 181, 117, 0.4);
  background: linear-gradient(135deg, #1eae98 0%, #3eb575 100%);
}

.add-robot-icon {
  cursor: pointer;
  font-size: 22px;
  color: #888;
  transition: color 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: transparent;
}

.add-robot-icon:hover {
  color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
}

.loading {
  text-align: center;
  color: #aaa;
  margin: 12px 0;
}

/* 响应式设计 */
@media (max-width: 600px) {
  .chat-window {
    border-radius: 0;
    min-height: calc(100dvh - 64px);
    /* 减去header高度 */
    height: calc(100dvh - 64px);
    max-width: 100vw;
    margin: 0;
  }

  .chat-header,
  .chat-input {
    border-radius: 0;
  }

  .chat-header {
    height: 60px;
    padding: 0 12px;
    position: fixed;
    top: 64px;
    /* 调整top位置，考虑header高度 */
    left: 0;
    right: 0;
    z-index: 1000;
  }

  .chat-messages {
    padding-top: 135px;
    margin-top: 0;
  }

  .chat-message {
    gap: 6px;
    margin-bottom: 10px;
  }

  .el-avatar {
    width: 32px !important;
    height: 32px !important;
    min-width: 32px !important;
    min-height: 32px !important;
  }

  .message-content {
    max-width: 90%;
    font-size: 0.98rem;
    padding: 8px 12px;
  }

  .chat-input {
    margin-bottom: 8px;
    padding-bottom: 8px;
    width: 100%;
    box-sizing: border-box;
  }
}

/* 超小屏幕适配 */
@media (max-width: 480px) {
  .chat-window {
    min-height: calc(100dvh - 60px);
    /* 480px以下header高度为60px */
    height: calc(100dvh - 60px);
  }

  .chat-header {
    top: 60px;
    /* 调整top位置 */
  }
}
</style>