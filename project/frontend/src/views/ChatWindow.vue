<template>
  <div class="chat-window">
    <div class="chat-header">
      <el-button @click="$router.back()" icon="el-icon-arrow-left" circle />
      <span>{{ robot?.name || '天使' }} 聊天</span>
    </div>
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="msg in messages" :key="msg.id" :class="['chat-message', msg.senderType]">
        <el-avatar :src="getAvatar(msg)" />
        <div class="message-content">{{ msg.content }}</div>
      </div>
      <div v-if="loading" class="loading">加载中...</div>
    </div>
    <div class="chat-input">
      <el-input v-model="input" @keyup.enter="sendMessage" placeholder="输入消息..." />
      <el-button type="primary" @click="sendMessage">发送</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { getChatHistory, sendChatMessage } from '@/api/chat'
import { getRobotById } from '@/api/robot'
import { getUserAvatarUrl, getRobotAvatarUrl } from '@/utils/avatar'
import { useUserStore } from '@/stores/user'
// 如有WebSocket全局store
import { useWebSocketStore } from '@/stores/websocket'

const route = useRoute()
const robotId = route.params.robotId
const messages = ref([])
const input = ref('')
const loading = ref(false)
const robot = ref(null)
const userStore = useUserStore()
const messagesContainer = ref(null)
const websocketStore = useWebSocketStore && useWebSocketStore()

const loadHistory = async () => {
  loading.value = true
  const res = await getChatHistory(robotId, { limit: 20, offset: 0 })
  if (res.code === 200) {
    messages.value = res.data // 不再reverse，直接渲染
  }
  loading.value = false
  scrollToBottom()
}

const scrollToBottom = () => {
  nextTick(() => {
    const el = messagesContainer.value
    if (el) {
      el.scrollTop = el.scrollHeight
    }
  })
}

const sendMessage = async () => {
  if (!input.value.trim()) return
  const res = await sendChatMessage(robotId, input.value)
  if (res.code === 200) {
    // 立即追加用户消息
    if (Array.isArray(res.data)) {
      messages.value.push(...res.data)
    } else {
      messages.value.push(res.data)
    }
    input.value = ''
    scrollToBottom()
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

onMounted(async () => {
  // 加载机器人信息
  const robotRes = await getRobotById(robotId)
  if (robotRes.code === 200) robot.value = robotRes.data
  // 加载历史消息
  await loadHistory()
  // 监听 WebSocket 新消息（原有逻辑）
  if (websocketStore && websocketStore.on) {
    websocketStore.on('chat', (msg) => {
      // 判断是否属于当前会话
      if (msg.senderId === robotId || msg.receiverId === robotId) {
        messages.value.push(msg)
        scrollToBottom()
      }
    })
    // 新增：监听后端标准type为'CHAT'的消息
    websocketStore.on('message', (msgObj) => {
      let obj = msgObj
      if (typeof obj === 'string') {
        try { obj = JSON.parse(obj) } catch {}
      }
      if (obj && obj.type === 'CHAT' && obj.data) {
        const msg = obj.data
        if (
          (msg.senderId === robotId && msg.receiverId === userStore.userInfo?.userId) ||
          (msg.senderId === userStore.userInfo?.userId && msg.receiverId === robotId)
        ) {
          messages.value.push(msg)
          scrollToBottom()
        }
      }
    })
  }
  // 监听AI聊天广播消息
  window.addEventListener('ai-chat-message', handleAIChatMessage)
})

onUnmounted(() => {
  if (websocketStore && websocketStore.off) {
    websocketStore.off('chat')
    websocketStore.off('message')
  }
  window.removeEventListener('ai-chat-message', handleAIChatMessage)
})

function handleAIChatMessage(e) {
  const msg = e.detail
  if (
    (msg.senderId === robotId && msg.receiverId === userStore.userInfo?.userId)
  ) {
    messages.value.push(msg)
    scrollToBottom()
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
  height: 80vh;
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
  align-items: flex-end;
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

.chat-input {
  display: flex;
  gap: 8px;
  padding: 16px;
  border-top: 1px solid #23272b;
  background: #23272b;
  border-radius: 0 0 16px 16px;
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
    height: 100vh;
    max-width: 100vw;
  }
  .chat-header, .chat-input {
    border-radius: 0;
  }
}
</style> 