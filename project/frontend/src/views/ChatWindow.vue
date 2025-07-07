<template>
  <div class="chat-window">
    <div class="chat-header">
      <el-button @click="goToWorld" circle>
        <el-icon><Back /></el-icon>
      </el-button>
      <span>{{ robot?.name || '天使' }} 聊天</span>
    </div>
    <div class="chat-messages" ref="messagesContainer">
      <div v-if="loadingHistory" class="loading-history">历史消息加载中...</div>
      <div v-for="msg in messages" :key="msg.id" :class="['chat-message', msg.senderType]">
        <el-avatar :src="getAvatar(msg)" />
        <div class="message-content">{{ msg.content }}</div>
      </div>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-if="isRobotReplying" class="replying-tip">
        天使正在回复
        <span class="dot"></span><span class="dot"></span><span class="dot"></span>
      </div>
    </div>
    <div class="chat-input">
      <el-input v-model="input" @keyup.enter="sendMessage" placeholder="输入消息..." />
      <el-button type="primary" @click="sendMessage">发送</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getChatHistory, sendChatMessage } from '@/api/chat'
import { getRobotById } from '@/api/robot'
import { getUserAvatarUrl, getRobotAvatarUrl } from '@/utils/avatar'
import { useUserStore } from '@/stores/user'
import { useWebSocketStore } from '@/stores/websocket'
import { Back } from '@element-plus/icons-vue'

const route = useRoute()
const robotId = ref('')
const messages = ref([])
const input = ref('')
const loading = ref(false)
const robot = ref(null)
const conversationId = ref(null)
const userStore = useUserStore()
const messagesContainer = ref(null)
const websocketStore = useWebSocketStore && useWebSocketStore()
const isRobotReplying = ref(false)

// 游标分页相关
const loadedMessageIds = ref(new Set())
const pageSize = 20
const hasMoreHistory = ref(true)
const loadingHistory = ref(false)
const historyCursor = ref(null) // 最早一条createdAt

const params = ref({
  limit: pageSize,
  offset: 0
})

const loadHistory = async (append = false) => {
  if (loadingHistory.value || !hasMoreHistory.value) return
  loadingHistory.value = true
  if (append && historyCursor.value) params.value.before = historyCursor.value
  const res = await getChatHistory(robotId.value, params.value)
  if (res.code === 200) {
    let newMsgs = (res.data || []).filter(msg => !loadedMessageIds.value.has(msg.id))
    newMsgs.forEach(msg => loadedMessageIds.value.add(msg.id))
    newMsgs = newMsgs.reverse() // 后端降序，前端reverse
    if (append) {
      messages.value = [...newMsgs, ...messages.value]
    } else {
      messages.value = newMsgs
    }
    if (newMsgs.length > 0) {
      historyCursor.value = newMsgs[0].createdAt
    }
    if (newMsgs.length < pageSize) hasMoreHistory.value = false
  }
  loadingHistory.value = false
}

const scrollToBottom = () => {
  nextTick(() => {
    const el = messagesContainer.value
    if (el) {
      el.scrollTop = el.scrollHeight
    }
  })
}

const onScroll = () => {
  const el = messagesContainer.value
  if (el && el.scrollTop <= 10 && hasMoreHistory.value && !loadingHistory.value) {
    const oldHeight = el.scrollHeight
    loadHistory(true).then(() => {
      nextTick(() => {
        el.scrollTop = el.scrollHeight - oldHeight
      })
    })
  }
}

const sendMessage = async () => {
  if (!input.value.trim()) return
  const res = await sendChatMessage(robotId.value, input.value, conversationId.value)
  if (res.code === 200) {
    /* if (Array.isArray(res.data)) {
      messages.value.push(...res.data)
    } else {
      messages.value.push(res.data)
    } */
    input.value = ''
    scrollToBottom()
    isRobotReplying.value = true // 用户发消息后，显示“天使正在回复...”
  }
}

const getAvatar = (msg) => {
  if (msg.senderType === 'user') {
    return getUserAvatarUrl({ avatar: userStore.userInfo?.avatar, nickname: userStore.userInfo?.nickname })
  } else if (msg.senderType === 'robot' || msg.senderType === 'ai') {
    return robot.value ? getRobotAvatarUrl({ avatar: robot.value.avatar, name: robot.value.name, id: robot.value.id }) : ''
  }
  return '/default-avatar.png'
}

// 监听消息变化自动滚动
watch(messages, () => {
  scrollToBottom()
})

const router = useRouter()
function goToWorld() {
  router.push('/world')
}

onMounted(async () => {
  // 确保 robotId 始终为字符串
  robotId.value = typeof route.params.robotId === 'string'
    ? route.params.robotId
    : (route.params.robotId?.id || route.params.robotId?.value || '')
  console.log('robotId 类型', typeof robotId.value, robotId.value)
  // 加载机器人信息
  const robotRes = await getRobotById(robotId.value)
  if (robotRes.code === 200) robot.value = robotRes.data
  // 初始化游标分页
  historyCursor.value = null
  hasMoreHistory.value = true
  loadedMessageIds.value.clear()
  await loadHistory(false)
  // 监听AI聊天广播消息
  window.addEventListener('ai-chat-message', handleAIChatMessage)
  // 监听滚动加载历史
  if (messagesContainer.value) {
    messagesContainer.value.addEventListener('scroll', onScroll)
  }
})

onUnmounted(() => {
  if (websocketStore && websocketStore.off) {
    websocketStore.off('chat')
    websocketStore.off('message')
  }
  window.removeEventListener('ai-chat-message', handleAIChatMessage)
  if (messagesContainer.value) {
    messagesContainer.value.removeEventListener('scroll', onScroll)
  }
})

function handleAIChatMessage(e) {
  const msg = e.detail
  if (
    (msg.senderId === robotId.value && msg.receiverId === userStore.userInfo?.userId) ||
    (msg.senderId === userStore.userInfo?.userId && msg.receiverId === robotId.value)
  ) {
    messages.value.push(msg)
    conversationId.value = msg.conversationId
    scrollToBottom()
    if (msg.senderType === 'ai' || msg.senderType === 'robot') {
      isRobotReplying.value = false
    }
  }
}
</script>

<style scoped>
.chat-window {
  max-width: 600px;
  margin: 0 auto;
  background: #181c20;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.18);
  display: flex;
  flex-direction: column;
  min-height: 100dvh;
  height: 100dvh;
  color: #e0e0e0;
}

.chat-header {
  padding: 16px;
  border-bottom: 1px solid #23272b;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 12px;
  background: #23272b;
  color: #fff;
  border-radius: 16px 16px 0 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px 16px 16px 16px;
  background: #181c20;
  scrollbar-width: thin;
  scrollbar-color: #222 transparent;
}

.chat-messages::-webkit-scrollbar {
  width: 8px;
  background: transparent;
}
.chat-messages::-webkit-scrollbar-thumb {
  background: #222;
  border-radius: 4px;
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
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
}

.message-content {
  max-width: 70%;
  word-break: break-all;
  font-size: 1.05rem;
  line-height: 1.7;
  padding: 10px 18px;
  border-radius: 18px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.10);
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
}

.loading {
  text-align: center;
  color: #aaa;
  margin: 12px 0;
}

.loading-history {
  text-align: center;
  color: #aaa;
  font-size: 0.95rem;
  margin: 8px 0;
}

.replying-tip {
  text-align: left;
  color: #3eb575;
  font-size: 0.98rem;
  margin: 8px 0 0 48px;
  opacity: 0.85;
  min-height: 24px;
  position: relative;
  display: flex;
  align-items: center;
}
.replying-tip .dot {
  display: inline-block;
  width: 0.5em;
  height: 0.5em;
  margin-left: 2px;
  border-radius: 50%;
  background: #3eb575;
  opacity: 0.3;
  animation: dotBlink 1.2s infinite;
}
.replying-tip .dot:nth-child(2) { animation-delay: 0.2s; }
.replying-tip .dot:nth-child(3) { animation-delay: 0.4s; }
.replying-tip .dot:nth-child(4) { animation-delay: 0.6s; }
@keyframes dotBlink {
  0%, 80%, 100% { opacity: 0.3; }
  40% { opacity: 1; }
}

.chat-input {
  display: flex;
  gap: 8px;
  padding: 16px;
  border-top: 1px solid #23272b;
  background: #23272b;
  border-radius: 0 0 16px 16px;
  position: sticky;
  bottom: 0;
  z-index: 10;
  padding-bottom: env(safe-area-inset-bottom);
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

.el-button {
  background: linear-gradient(135deg, #3eb575 0%, #1eae98 100%);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(62,181,117,0.10);
  transition: background 0.2s;
}

.el-button:hover {
  background: linear-gradient(135deg, #1eae98 0%, #3eb575 100%);
}

@media (max-width: 600px) {
  .chat-window {
    border-radius: 0;
    min-height: 100dvh;
    height: 100dvh;
    max-width: 100vw;
  }
  .chat-header, .chat-input {
    border-radius: 0;
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
  }
  .replying-tip {
    margin-left: 36px;
    font-size: 0.95rem;
  }
}
</style> 