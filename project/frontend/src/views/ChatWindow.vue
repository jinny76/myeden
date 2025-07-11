<template>
  <div class="chat-window">
    <div class="chat-header">
      <el-button @click="goToWorld" circle>
        <el-icon><Back /></el-icon>
      </el-button>
      <span style="margin-left: 10px;">{{ robot?.name || '天使' }} 聊天</span>
    </div>
    <div class="chat-messages" ref="messagesContainer">
      <div v-if="loadingHistory" class="loading-history">历史消息加载中...</div>
      <div v-for="msg in messages" :key="msg.id" :class="['chat-message', msg.senderType, { 'voice-message': msg.asrResult }]">
        <el-avatar :src="getAvatar(msg)" />
        <div class="message-content">
          <template v-if="msg.asrResult">           
            {{ msg.asrResult.text || msg.content }}
            <span v-if="msg.asrResult.emotion && msg.asrResult.emotion !== 'NEUTRAL'" class="emotion-label">
              <template v-if="msg.asrResult.emotion === 'HAPPY'">😊</template>
              <template v-else-if="msg.asrResult.emotion === 'SAD'">😢</template>
              <template v-else-if="msg.asrResult.emotion === 'ANGRY'">😠</template>
              <template v-else-if="msg.asrResult.emotion === 'SURPRISED'">😲</template>
              <template v-else-if="msg.asrResult.emotion === 'DISGUSTED'">😒</template>
              <template v-else-if="msg.asrResult.emotion === 'FEARFUL'">😨</template>              
              <template v-else-if="msg.asrResult.emotion === 'CONFUSED'">😕</template>
            </span>
          </template>
          <template v-else>
            {{ msg.content }}
          </template>
        </div>
      </div>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-if="isRobotReplying" class="replying-tip">
        天使正在回复
        <span class="dot"></span><span class="dot"></span><span class="dot"></span>
      </div>
    </div>
    <div class="chat-input" style="position:relative;">
      <el-button
        class="voice-btn"
        @touchstart.prevent="startRecording"
        @touchend.prevent="stopRecording"
        @mousedown.prevent="startRecording"
        @mouseup.prevent="stopRecording"
      >
        <el-icon><Microphone /></el-icon>
      </el-button>
      <span v-if="isRecording" class="recording-tip">正在录音，松手发送</span>
      <el-input v-model="input" @keyup.enter="sendMessage" placeholder="输入消息..." />
      <el-button type="primary" @click="sendMessage">发送</el-button>
      <!-- 浮动摄像头图标 -->
      <span
        class="switch-icon"
        @click="switchCamera"
        title="切换摄像头"
      >
        <el-icon>
          <Refresh />
        </el-icon>
      </span>
      <span
        class="camera-icon"
        :class="{ active: cameraActive }"
        @click="toggleCamera"
        title="视频聊天"
      >
        <el-icon>
          <VideoCamera />
        </el-icon>
      </span>
    </div>
    <!-- 摄像头视频窗口浮动显示在右上角，仅在cameraActive时显示 -->
    <div v-if="cameraActive" class="floating-video-window">
      <video ref="videoRef" autoplay playsinline muted style="display:block;" />
    </div>
    <canvas ref="canvasRef" style="display:none"></canvas>
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
import { Back, VideoCamera, Refresh, Microphone } from '@element-plus/icons-vue'
import { message } from '@/utils/message'
import { tts } from '@/api/tts'

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

// 摄像头相关
const cameraActive = ref(false)
const stream = ref(null)
const videoRef = ref(null)
const canvasRef = ref(null)
const facingMode = ref('user') // 'user'前置, 'environment'后置

const toggleCamera = async () => {
  if (cameraActive.value) {
    if (stream.value) {
      stream.value.getTracks().forEach(track => track.stop())
    }
    cameraActive.value = false
    stream.value = null
  } else {
    stream.value = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: facingMode.value }
    })
    cameraActive.value = true
    await nextTick()
    if (videoRef.value) {
      videoRef.value.srcObject = stream.value
    }
  }
}

const switchCamera = async () => {
  facingMode.value = facingMode.value === 'user' ? 'environment' : 'user'
  if (cameraActive.value) {
    if (stream.value) {
      stream.value.getTracks().forEach(track => track.stop())
    }
    stream.value = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: facingMode.value }
    })
    await nextTick()
    if (videoRef.value) {
      videoRef.value.srcObject = stream.value
    }
  }
}

const isRecording = ref(false)
const recorder = ref(null)
const audioChunks = ref([])
const recordStartY = ref(0)

const startRecording = async (e) => {
  if (!navigator.mediaDevices || !window.MediaRecorder) {
    message.error('当前浏览器不支持录音')
    return
  }
  isRecording.value = true
  audioChunks.value = []
  try {
    const streamObj = await navigator.mediaDevices.getUserMedia({ audio: true })
    recorder.value = new MediaRecorder(streamObj)
    recorder.value.ondataavailable = (event) => {
      if (event.data.size > 0) audioChunks.value.push(event.data)
    }
    recorder.value.onstop = async () => {
      const blob = new Blob(audioChunks.value, { type: 'audio/webm' })
      const reader = new FileReader()
      reader.onloadend = async () => {
        const base64Audio = reader.result
        await sendChatMessage(robotId.value, null, conversationId.value, null, base64Audio)
        isRecording.value = false
        isRobotReplying.value = true
        nextTick(() => {
          scrollToBottom()
        })
      }
      reader.readAsDataURL(blob)
    }
    recorder.value.start()
    if (e && e.touches) recordStartY.value = e.touches[0].clientY
  } catch (err) {
    message.error('无法访问麦克风: ' + err.message)
    isRecording.value = false
  }
}

const stopRecording = () => {
  if (recorder.value && recorder.value.state !== 'inactive') {
    recorder.value.stop()
  }
  isRecording.value = false
}

const sendMessage = async () => {
  if (!input.value.trim()) return
  let imageBase64 = null
  if (cameraActive.value && videoRef.value && canvasRef.value) {
    const video = videoRef.value
    const canvas = canvasRef.value
    if (video.videoWidth > 0 && video.videoHeight > 0) {
      canvas.width = video.videoWidth
      canvas.height = video.videoHeight
      const ctx = canvas.getContext('2d')
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
      imageBase64 = canvas.toDataURL('image/png')
    }
  }
  // 假设sendChatMessage支持imageBase64参数
  const res = await sendChatMessage(robotId.value, input.value, conversationId.value, imageBase64)
  if (res.code === 200) {
    input.value = ''
    scrollToBottom()
    isRobotReplying.value = true
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
      // 如果上一条消息是自己发的语音，自动朗读AI回复
      const lastMsg = messages.value[messages.value.length - 2]
      if (lastMsg && lastMsg.senderType === 'user' && lastMsg.asrResult) {
        playSpeech(msg.content, )
      }
    }
  }
}

function getVoiceType() {
  const gender = robot.value?.gender
  const age = robot.value?.age

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
    if (age <= 45) return 'zh_female_meilinvyou_emo_v2_mars_bigtts'
    return 'ICL_zh_female_heainainai_tob'
  }
}

const playSpeech = async (text) => {
  if (!text || typeof text !== 'string') {
    message.warning('无可朗读内容')
    return
  }
  // 1. 优先调用后端TTS接口
  try {
    const voiceType = getVoiceType()
    const resp = await tts(text, voiceType)
    if (resp.code === 200) {
      const data = resp.data
      const audioUrl = data.url.replace('/uploads/', '/api/v1/files/')
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
    const utter = new window.SpeechSynthesisUtterance(text)
    utter.rate = 1
    utter.pitch = 1
    utter.volume = 1
    utter.lang = 'zh-CN'
    utter.onerror = (e) => {}
    window.speechSynthesis.speak(utter)
  }
}
</script>

<style scoped>
.chat-window {
  max-width: 600px;
  margin: 80px auto 0 auto;
  background: #181c20;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.18);
  display: flex;
  flex-direction: column;
  min-height: calc(100dvh - 80px);
  height: calc(100dvh - 80px);
  color: #e0e0e0;
}

.chat-header {
  flex-shrink: 0;
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid #23272e;
  background: #23272e;
  z-index: 2;
}

.chat-messages {
  flex: 1 1 0;
  overflow-y: auto;
  padding: 16px;
  padding-top: 56px;
  margin-top: -40px;
  background: #181c20;
  scrollbar-width: thin; /* Firefox */
  scrollbar-color: #444 #23272e; /* Firefox */
}

/* Webkit 浏览器（Chrome/Edge/Safari） */
.chat-messages::-webkit-scrollbar {
  width: 8px;
  background: transparent;
}
.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(80, 80, 80, 0.5);
  border-radius: 8px;
  transition: background 0.2s;
}
.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(120, 120, 120, 0.7);
}
.chat-messages::-webkit-scrollbar-track {
  background: transparent;
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
  flex-shrink: 0;
  display: flex;
  align-items: center;
  padding: 16px;
  background: #23272e;
  border-top: 1px solid #23272e;
  border-radius: 0 0 16px 16px;
  /* 移除position:sticky，保证flex布局下始终在底部 */
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

.camera-icon {
  position: absolute;
  right: 80px;
  top: 58%;
  transform: translateY(-50%);
  cursor: pointer;
  font-size: 22px;
  color: #888;
  transition: color 0.2s;
  z-index: 2;
}
.camera-icon.active {
  color: #67c23a;
}
.switch-icon {
  position: absolute;
  right: 120px;
  top: 58%;
  transform: translateY(-50%);
  cursor: pointer;
}

.floating-video-window {
  position: fixed;
  top: 80px;
  right: 10px;
  width: 260px;
  aspect-ratio: 4/3;
  min-width: 180px;
  max-width: 320px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0,0,0,0.18);
  z-index: 10;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}
.floating-video-window video {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #000;
  border-radius: 12px;
}

.voice-btn {
  margin-right: 8px;
  background: transparent !important;
  color: #67c23a;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.recording-tip {
  position: absolute;
  left: 50%;
  top: -40px;
  transform: translateX(-50%);
  background: #23272e;
  color: #fff;
  padding: 8px 18px;
  border-radius: 18px;
  font-size: 1rem;
  z-index: 20;
  box-shadow: 0 2px 8px rgba(0,0,0,0.18);
  animation: pulse 1.2s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 0.7; }
  50% { opacity: 1; }
}

.voice-message .message-content {
  background: linear-gradient(90deg, #f7d774 0%, #ffe9b0 100%);
  color: #333;
  border: 1px solid #f7d774;
  position: relative;
}
.voice-label {
  color: #e67e22;
  font-weight: bold;
  margin-right: 6px;
}
.emotion-label {
  color: #67c23a;
  margin-left: 8px;
  font-size: 0.95em;
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

@media (max-width: 768px) {
  .chat-window {
    max-width: 100vw;
    min-width: 0;
    margin: 0;
    border-radius: 0;
    height: 100dvh;
    min-height: 100dvh;
    box-shadow: none;
  }
  .chat-header {
    height: 48px;
    padding: 0 8px;
    font-size: 16px;
  }
  .chat-messages {
    padding: 8px;
    padding-top: 48px;
    margin-top: -20px;
    font-size: 15px;
  }
  .chat-input {
    padding: 8px;
    font-size: 15px;
    border-radius: 0;
  }
  .el-input__inner, .el-button {
    font-size: 15px !important;
    height: 36px !important;
    min-height: 36px !important;
    border-radius: 6px !important;
  }
  .el-avatar {
    width: 32px !important;
    height: 32px !important;
  }
}
</style> 