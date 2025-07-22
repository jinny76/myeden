<template>
  <div class="chat-window">
    <div class="chat-header">
      <div class="header-left">
        <el-button @click="goToWorld" circle>
          <el-icon><Back /></el-icon>
        </el-button>
        <span style="margin-left: 10px;">{{ robot?.name || '天使' }} 聊天</span>
      </div>
      <!-- 专家模式结束按钮 -->
      <el-button 
        v-if="currentThemeId" 
        type="warning" 
        @click="endExpertSession"
        class="end-session-btn header-end-btn"
        title="结束专家会话并保存记忆"
      >
        结束会话
      </el-button>
    </div>
    <div class="chat-messages" ref="messagesContainer">
      <div v-if="loadingHistory" class="loading-history">历史消息加载中...</div>
      <div v-for="(msg, idx) in messages" :key="msg.id || ('user-' + idx)">
        <!-- 时间分割线 -->
        <div v-if="shouldShowTime(idx)" class="chat-time-divider">
          {{ formatTime(msg.createdAt) }}
        </div>
        <div :class="['chat-message', msg.senderType, { 'voice-message': msg.asrResult }]">
          <el-avatar :src="getAvatar(msg)" />
          <div 
            class="message-content"
            :class="{ 'clickable': msg.senderType === 'robot' || msg.senderType === 'ai' }"
            @click="handleMessageClick(msg)"
            :title="(msg.senderType === 'robot' || msg.senderType === 'ai') ? '点击播放语音' : ''"
          >
            <template v-if="msg.asrResult">           
              {{ msg.asrResult.text || getContent(msg.content) }}
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
              {{ getContent(msg.content) }}
            </template>          
          </div>
        </div>
      </div>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-if="isRobotReplying" class="replying-tip">
        天使正在回复
        <span class="dot"></span><span class="dot"></span><span class="dot"></span>
      </div>
    </div>
    <div class="chat-input" style="position:relative;">
      <el-input 
        v-model="input" 
        @keyup.enter="sendMessage" 
        :placeholder="isRecording ? '正在录音...' : '输入消息或长按语音'"
        @touchstart="handleInputTouchStart"
        @touchend="handleInputTouchEnd"
        @mousedown="handleInputMouseDown"
        @mouseup="handleInputMouseUp"
        :class="{ 'recording-input': isRecording }"
        ref="inputRef"
      />
      <button 
        v-if="input.trim()" 
        @click="sendMessage"
        class="send-btn"
        title="发送消息"
      >
        <el-icon><Position /></el-icon>
      </button>
      <!-- 摄像头按钮 -->
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
      <!-- 切换摄像头按钮，只在视频模式下显示 -->
      <span
        v-if="cameraActive"
        class="switch-icon"
        @click="switchCamera"
        title="切换摄像头"
      >
        <el-icon>
          <Refresh />
        </el-icon>
      </span>
    </div>
    <!-- 语音录制面板 -->
    <div v-if="isRecording" class="voice-recording-panel">
      <div class="recording-visual">
        <div class="mic-icon-wrapper">
          <el-icon class="mic-icon"><Microphone /></el-icon>
        </div>
        <div class="recording-waves">
          <div class="wave wave1"></div>
          <div class="wave wave2"></div>
          <div class="wave wave3"></div>
        </div>
      </div>
      <div class="recording-text">正在录音，松手发送</div>
      <div class="recording-hint">向上滑动取消</div>
    </div>
    <!-- 摄像头视频窗口浮动显示在右上角，仅在cameraActive时显示 -->
    <div v-if="cameraActive" class="floating-video-window">
      <video ref="videoRef" autoplay playsinline muted style="display:block;" />
    </div>
    <canvas ref="canvasRef" style="display:none"></canvas>
    <!-- 全屏视频播放蒙层 -->
    <div v-if="showFullVideo" class="fullscreen-video-mask" @click="onFullVideoEnded">
      <video
        ref="fullVideoRef"
        :src="fullVideoUrl"
        class="fullscreen-video"
        @ended="onFullVideoEnded"
        @canplay="onFullVideoCanPlay"
        autoplay
        playsinline
        webkit-playsinline
      ></video>
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
import { Back, VideoCamera, Refresh, Microphone, VideoPlay, Position } from '@element-plus/icons-vue'
import { message } from '@/utils/message'
import { tts } from '@/api/tts'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const robotId = ref('')
const messages = ref([])
const input = ref('')
const loading = ref(false)
const robot = ref(null)
const conversationId = ref(null)
const userStore = useUserStore()
const messagesContainer = ref(null)
const inputRef = ref(null)
const websocketStore = useWebSocketStore && useWebSocketStore()
const isRobotReplying = ref(false)
// 新增：当前聊天主题Id
const currentThemeId = ref(route.query.themeId || '')

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

const getContent = (msg) => {
  if (!msg) return ''

  let parts = msg.split('|')
  if (parts.length > 1) {
    return parts[0]
  }
  return msg
}

const getEmotion = (msg) => {
  if (!msg) return null

  let parts = msg.split('|')
  if (parts.length > 1) {
    return parts[1]
  }
  return null;
}

const loadHistory = async (append = false) => {
  if (loadingHistory.value || !hasMoreHistory.value) return
  loadingHistory.value = true
  
  // 构建查询参数，包含主题ID过滤
  const queryParams = { ...params.value }
  if (append && historyCursor.value) {
    queryParams.before = historyCursor.value
  }
  
  // 添加expertThemeId过滤参数
  if (currentThemeId.value) {
    // 专家模式：只加载该主题的消息
    queryParams.expertThemeId = currentThemeId.value
  } else {
    // 随便聊聊模式：只加载没有主题ID的消息（expertThemeId为null或空）
    queryParams.expertThemeId = null
  }
  
  const res = await getChatHistory(robotId.value, queryParams)
  if (res.code === 200) {
    let newMsgs = (res.data || []).filter(msg => !loadedMessageIds.value.has(msg.id))
    newMsgs.forEach(msg => loadedMessageIds.value.add(msg.id))
    newMsgs = newMsgs.reverse() // 后端降序，前端reverse
    if (append) {
      messages.value = [...newMsgs, ...messages.value]
    } else {
      messages.value = newMsgs
      // 检查最后一条消息（显示在最下方的消息）是否是当天的机器人主动消息，如果是则复用conversationId
      if (newMsgs.length > 0) {
        const lastMsg = newMsgs[newMsgs.length - 1] // reverse后最新的消息在数组末尾
        // 如果最后一条消息是机器人主动消息，则无论何时都复用conversationId
        // 如果是普通机器人消息，则只在2小时内复用conversationId
        if (lastMsg.conversationId && 
            (lastMsg.senderType === 'robot' || lastMsg.senderType === 'ai')) {
          if (lastMsg.isProactiveMessage || lastMsg.createdAt > Date.now() - 2 * 60 * 60 * 1000) {
            conversationId.value = lastMsg.conversationId
          }
        }
      }
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
const longPressTimer = ref(null)
const isLongPressing = ref(false)

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
        const res = await sendChatMessage(robotId.value, null, conversationId.value, null, base64Audio, currentThemeId.value, currentThemeId.value ? 'expert' : 'normal')
        isRecording.value = false
        // 检查语音识别结果，如果没有内容则不触发机器人回复
        if (res && res.code === 200 && res.data && res.data.content && res.data.content.trim()) {
          isRobotReplying.value = true
          nextTick(() => {
            scrollToBottom()
          })
        } else {
          // 语音识别为空或失败，显示提示
          if (res && res.code === 200 && (!res.data.content || !res.data.content.trim())) {
            message.warning('语音识别内容为空，请重新录制')
          }
        }
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

// 长按检测处理函数
const handleInputTouchStart = (e) => {
  longPressTimer.value = setTimeout(() => {
    isLongPressing.value = true
    startRecording(e)
  }, 500) // 500ms后开始录音
}

const handleInputTouchEnd = (e) => {
  if (longPressTimer.value) {
    clearTimeout(longPressTimer.value)
    longPressTimer.value = null
  }
  
  if (isLongPressing.value) {
    isLongPressing.value = false
    stopRecording()
    e.preventDefault()
  }
}

const handleInputMouseDown = (e) => {
  longPressTimer.value = setTimeout(() => {
    isLongPressing.value = true
    startRecording(e)
  }, 500) // 500ms后开始录音
}

const handleInputMouseUp = (e) => {
  if (longPressTimer.value) {
    clearTimeout(longPressTimer.value)
    longPressTimer.value = null
  }
  
  if (isLongPressing.value) {
    isLongPressing.value = false
    stopRecording()
    e.preventDefault()
  }
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
  // 发送消息时带上themeId
  const res = await sendChatMessage(robotId.value, input.value, conversationId.value, imageBase64, undefined, currentThemeId.value, currentThemeId.value ? 'expert' : 'normal')
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

// 监听路由变化，保持主题Id同步
watch(() => route.query.themeId, (val) => {
  currentThemeId.value = val || ''
})

function goToWorld() {
  router.push('/world')
}

const showFullVideo = ref(false)
const fullVideoUrl = ref('')
const fullVideoRef = ref(null)

/**
 * 播放全屏视频
 * @param {string} url 视频地址
 */
function playFullVideo(url) {
  fullVideoUrl.value = url
  showFullVideo.value = true
  nextTick(() => {
    if (fullVideoRef.value) {
      fullVideoRef.value.currentTime = 0
      fullVideoRef.value.play()
    }
  })
}

function onFullVideoEnded() {
  showFullVideo.value = false
  fullVideoUrl.value = ''
}

function onFullVideoCanPlay() {
  // 可选：自动全屏
  const video = fullVideoRef.value
  if (video && video.requestFullscreen) {
    video.requestFullscreen()
  }
}

/**
 * 过滤括号内容，只保留非括号部分
 * @param {string} text
 * @returns {string}
 */
function filterBracketText(text) {
  if (!text) return ''
  // 去除所有中英文括号内的内容，包括多组
  return text.replace(/\([^\)]*\)|（[^）]*）/g, '').replace(/\s+/g, ' ').trim()
}

// 聊天窗口背景音乐播放器
let bgmAudio = null

/**
 * 播放背景音乐
 * @param {string} robotId - 当前聊天机器人ID
 */
function playBgm(robotId) {
  if (!robotId) return
  stopBgm() // 先停止已有的背景音乐，防止多实例冲突
  const url = `/api/v1/files/bgm/${robotId}.mp3`
  bgmAudio = new Audio(url)
  bgmAudio.loop = true // 循环播放
  bgmAudio.volume = 0.4 // 音量较低，避免干扰语音消息
  bgmAudio.onerror = () => {
    // 加载失败时释放资源
    bgmAudio = null
  }
  // 尝试自动播放（部分浏览器需用户交互后才能播放）
  bgmAudio.play().catch(() => {
    // 可在用户首次交互时再次尝试播放
  })
}

/**
 * 停止背景音乐
 */
function stopBgm() {
  if (bgmAudio) {
    bgmAudio.pause()
    bgmAudio.currentTime = 0
    bgmAudio = null
  }
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
  playBgm(robotId.value) // 进入页面时自动播放背景音乐
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
  stopBgm() // 离开页面时自动停止背景音乐
  
  // 停止当前音频
  stopCurrentAudio()
  
  // 清理长按定时器
  if (longPressTimer.value) {
    clearTimeout(longPressTimer.value)
    longPressTimer.value = null
  }
})

function handleAIChatMessage(e) {
  const msg = e.detail
  if (
    (msg.senderId === robotId.value && msg.receiverId === userStore.userInfo?.userId) ||
    (msg.senderId === userStore.userInfo?.userId && msg.receiverId === robotId.value)
  ) {    
    conversationId.value = msg.conversationId
    
    if (msg.senderType === 'ai' || msg.senderType === 'robot') {
      // 如果上一条消息是自己发的语音，自动朗读AI回复
      const lastMsg = messages.value[messages.value.length - 1]
      if (lastMsg && lastMsg.senderType === 'user' && lastMsg.asrResult) {
        // 在播放新语音前先停止当前播放的语音
        stopCurrentAudio()
        playSpeech(msg)
      }  else {
        messages.value.push(msg)
        scrollToBottom()
        isRobotReplying.value = false
        //playEmotion(msg.content)
      }
    } else {
      messages.value.push(msg)
      scrollToBottom()     
    }
  }
}

function getVoiceType(msg) {
  const gender = robot.value?.gender
  const age = robot.value?.age
  const index = parseInt(robot.value?.id.substring(6)) % 10;
  const voiceFemale = [    
    'zh_female_shuangkuaisisi_emo_v2_mars_bigtts',
    'zh_female_tianxinxiaomei_emo_v2_mars_bigtts',
    'zh_female_gaolengyujie_emo_v2_mars_bigtts',
    'zh_female_tianmeitaozi_mars_bigtts',
    'zh_female_qingxinnvsheng_mars_bigtts',
    'zh_female_kailangjiejie_moon_bigtts',
    'zh_female_tianmeiyueyue_moon_bigtts',
    'zh_female_meilinvyou_emo_v2_mars_bigtts',
    'zh_female_roumeinvyou_emo_v2_mars_bigtts',
    'ICL_zh_female_wenrouwenya_tob',
  ]

  if (!gender) gender = 'female'
  if (!age) age = 20
  if (gender === 'male') {
    if (age <= 12) return 'zh_male_naiqimengwa_mars_bigtts'
    if (age <= 18) return 'zh_male_linjiananhai_moon_bigtts'
    if (age <= 45) return 'zh_male_junlangnanyou_emo_v2_mars_bigtts'
    return 'ICL_zh_male_youmodaye_tob'
  } else {
    if (age <= 12) return 'zh_female_mengyatou_mars_bigtts'
    if (age <= 18) return 'zh_female_tianxinxiaomei_emo_v2_mars_bigtts'
    if (age <= 45) return voiceFemale[index]
    return 'ICL_zh_female_heainainai_tob'
  }
}

/**
 * 处理消息点击事件
 * @param {Object} msg - 消息对象
 */
const handleMessageClick = async (msg) => {
  // 只有机器人或AI消息才可点击播放
  if (msg.senderType !== 'robot' && msg.senderType !== 'ai') {
    return
  }
  
  // 检查是否有内容可播放
  if (!msg.content || typeof msg.content !== 'string') {
    message.warning('无可播放内容')
    return
  }
  
  // 调用播放语音功能
  await playMessageSpeech(msg)
}

// 全局音频播放控制
let currentAudio = null

/**
 * 停止当前正在播放的音频
 */
const stopCurrentAudio = () => {
  // 停止TTS音频
  if (currentAudio) {
    currentAudio.pause()
    currentAudio.currentTime = 0
    currentAudio = null
  }
  // 停止浏览器语音合成
  if (window.speechSynthesis) {
    window.speechSynthesis.cancel()
  }
}

/**
 * 播放消息语音
 * @param {Object} msg - 消息对象
 */
const playMessageSpeech = async (msg) => {
  if (!msg.content || typeof msg.content !== 'string') {
    message.warning('无可朗读内容')
    return
  }
  // 打断当前音频
  stopCurrentAudio()
  // 1. 优先调用后端TTS接口
  try {
    const voiceType = getVoiceType(msg)
    const textToRead = filterBracketText(getContent(msg.content))
    const resp = await tts(textToRead, voiceType)
    if (resp.code === 200) {
      const data = resp.data
      const audioUrl = data.url.replace('./uploads/', '/api/v1/files/')
      const audio = new Audio(audioUrl)
      currentAudio = audio
      // 监听播放错误事件
      audio.onerror = (e) => {
        console.error('音频播放失败:', e)
      }
      playEmotion(msg.content)
      await audio.play()
      return
    }
  } catch (e) {
    console.warn('TTS接口失败，降级为speechSynthesis', e)
    // 2. 降级为浏览器speechSynthesis
    if (!window.speechSynthesis) {
      message.warning('当前浏览器不支持语音朗读')
      return
    }
    window.speechSynthesis.cancel()
    const textToRead = filterBracketText(getContent(msg.content))
    const utter = new window.SpeechSynthesisUtterance(textToRead)
    utter.rate = 1
    utter.pitch = 1
    utter.volume = 1
    utter.lang = 'zh-CN'
    utter.onerror = (e) => {
      console.error('语音合成失败:', e)
    }
    window.speechSynthesis.speak(utter)
    playEmotion(msg.content)
  }
}

/**
 * 播放情绪视频
 * @param {Object} msg - 消息对象
 */
const playEmotion = async (msg) => {
  const emotion = getEmotion(msg)
  if (emotion) {
    const videoUrl = `/api/v1/files/video/${robotId.value}_${emotion}.mp4`
    // check if video exists
    const resp = await fetch(videoUrl)
    if (resp.status === 200) {
      playFullVideo(videoUrl)
    }
  }
}

const playSpeech = async (msg) => {
  if (!msg.content || typeof msg.content !== 'string') {
    message.warning('无可朗读内容')
    return
  }
  // 打断当前音频
  stopCurrentAudio()
  // 1. 优先调用后端TTS接口
  try {
    const voiceType = getVoiceType()
    const textToRead = filterBracketText(getContent(msg.content))
    const resp = await tts(textToRead, voiceType)
    if (resp.code === 200) {
      const data = resp.data
      const audioUrl = data.url.replace('./uploads/', '/api/v1/files/')
      const audio = new Audio(audioUrl)
      currentAudio = audio
      messages.value.push(msg)
      scrollToBottom()
      isRobotReplying.value = false
      audio.play()
      playEmotion(msg.content)
      return
    }
  } catch (e) {
    // TTS接口失败降级
    messages.value.push(msg)
    scrollToBottom()
    isRobotReplying.value = false
    console.warn('TTS接口失败，降级为speechSynthesis', e)
    if (!window.speechSynthesis) {
      message.warning('当前浏览器不支持语音朗读')
      return
    }
    window.speechSynthesis.cancel()
    const textToRead = filterBracketText(getContent(msg.content))
    const utter = new window.SpeechSynthesisUtterance(textToRead)
    utter.rate = 1
    utter.pitch = 1
    utter.volume = 1
    utter.lang = 'zh-CN'
    utter.onerror = (e) => {}
    window.speechSynthesis.speak(utter)
    playEmotion(msg.content)
  }
}

/**
 * 结束专家会话并保存记忆
 */
const endExpertSession = async () => {
  if (!currentThemeId.value) {
    message.warning('当前不在专家模式')
    return
  }
  
  try {
    // 发送退出消息触发记忆保存
    const res = await sendChatMessage(robotId.value, '/退出', conversationId.value, null, undefined, currentThemeId.value, 'expert')
    if (res.code === 200) {
      message.success('专家会话已结束，正在记录这次沟通...')
      // 清空当前主题ID，退出专家模式
      currentThemeId.value = ''
      // 可选：跳转回世界页面
      setTimeout(() => {
        router.push('/world')
      }, 1000)
    } else {
      message.error('结束会话失败，请重试')
    }
  } catch (error) {
    console.error('结束专家会话失败:', error)
    message.error('结束会话失败，请重试')
  }
}

/**
 * 判断当前消息是否需要显示时间分割线
 * @param {number} idx - 当前消息索引
 * @returns {boolean}
 */
function shouldShowTime(idx) {
  if (idx === 0) return true
  const prev = messages.value[idx - 1]
  const curr = messages.value[idx]
  if (!prev || !curr) return false
  const prevTime = dayjs(prev.createdAt)
  const currTime = dayjs(curr.createdAt)
  return currTime.diff(prevTime, 'minute') >= 10
}

/**
 * 格式化时间
 * @param {string|number} ts
 * @returns {string}
 */
function formatTime(ts) {
  const now = dayjs()
  const msgTime = dayjs(ts)
  if (msgTime.isSame(now, 'day')) {
    // 今天的消息只显示时分
    return msgTime.format('HH:mm')
  } else {
    // 非今天的消息显示完整日期
    return msgTime.format('YYYY-MM-DD HH:mm')
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

.header-end-btn {
  font-size: 14px !important;
  padding: 8px 16px !important;
  height: 36px !important;
  margin-left: auto;
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
  position: relative;
}

/* 可点击的机器人消息样式 */
.message-content.clickable {
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.message-content.clickable:hover {
  background: #2a2f35 !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.message-content.clickable:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
}

/* 播放图标样式 */
.play-icon {
  position: absolute;
  top: 50%;
  right: 8px;
  transform: translateY(-50%);
  color: #67c23a;
  font-size: 14px;
  opacity: 0.7;
  transition: opacity 0.2s ease;
}

.message-content.clickable:hover .play-icon {
  opacity: 1;
  color: #85ce61;
}

/* 播放时的动画效果 */
.message-content.clickable:active .play-icon {
  transform: translateY(-50%) scale(0.9);
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
  gap: 8px;
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

.end-session-btn {
  background: linear-gradient(135deg, #f56c6c 0%, #e6a23c 100%) !important;
  color: #fff !important;
  border: none !important;
  border-radius: 12px !important;
  font-weight: 600 !important;
  box-shadow: 0 2px 8px rgba(245,108,108,0.20) !important;
  transition: all 0.2s !important;
  margin-left: 8px;
}

.end-session-btn:hover {
  background: linear-gradient(135deg, #e6a23c 0%, #f56c6c 100%) !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245,108,108,0.30) !important;
}

.camera-icon {
  cursor: pointer;
  font-size: 22px;
  color: #888;
  transition: color 0.2s;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: transparent;
}
.camera-icon.active {
  color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
}
.switch-icon {
  cursor: pointer;
  font-size: 20px;
  color: #888;
  transition: color 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: transparent;
}
.switch-icon:hover {
  color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
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

.send-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translate(-50%, -50%);
}

.send-btn:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 6px 20px rgba(62, 181, 117, 0.4);
  background: linear-gradient(135deg, #1eae98 0%, #3eb575 100%);
}

.send-btn:hover::before {
  width: 100%;
  height: 100%;
}

.send-btn:active {
  transform: translateY(-1px) scale(1.02);
  box-shadow: 0 4px 12px rgba(62, 181, 117, 0.3);
}

.send-btn .el-icon {
  position: relative;
  z-index: 1;
  transform: rotate(-45deg);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.send-btn:hover .el-icon {
  transform: rotate(-45deg) scale(1.1);
}

/* 录音状态输入框样式 */
.recording-input .el-input__wrapper {
  border-color: #67c23a !important;
  box-shadow: 0 0 0 2px rgba(103, 194, 58, 0.2) !important;
  background: rgba(103, 194, 58, 0.05) !important;
}

/* 语音录制面板 */
.voice-recording-panel {
  position: fixed;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 30px 24px 24px 24px;
  z-index: 1000;
  text-align: center;
  min-width: 240px;
  animation: slideUp 0.3s ease-out;
  overflow: hidden;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

.recording-visual {
  position: relative;
  margin-bottom: 16px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mic-icon-wrapper {
  position: relative;
  z-index: 2;
  background: #67c23a;
  border-radius: 50%;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  box-shadow: 0 4px 20px rgba(103, 194, 58, 0.3);
}

.mic-icon {
  font-size: 24px;
  color: white;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.recording-waves {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1;
  width: 120px;
  height: 120px;
}

.wave {
  position: absolute;
  border: 2px solid rgba(103, 194, 58, 0.5);
  border-radius: 50%;
  animation: wave 2s infinite;
}

.wave1 {
  width: 80px;
  height: 80px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: 0s;
}

.wave2 {
  width: 100px;
  height: 100px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: 0.5s;
}

.wave3 {
  width: 120px;
  height: 120px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: 1s;
}

@keyframes wave {
  0% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0;
  }
}

.recording-text {
  color: white;
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
}

.recording-hint {
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
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

.chat-time-divider {
  text-align: center;
  color: rgba(170, 170, 170, 0.5);
  font-size: 11px;
  margin: 12px 0 4px 0;
  letter-spacing: 1px;
}

.fullscreen-video-mask {
  position: fixed;
  left: 0; top: 0; right: 0; bottom: 0;
  z-index: 9999;
  background: rgba(0,0,0,0.92);
  display: flex;
  align-items: center;
  justify-content: center;
}
.fullscreen-video {
  max-width: 90vw;
  max-height: 90vh;
  width: auto;
  height: auto;
  object-fit: contain;
  background: #000;
  pointer-events: none;
  display: block;
  margin: auto;
}

@media (max-width: 600px) {
  .chat-window {
    border-radius: 0;
    min-height: 100dvh;
    height: 100dvh;
    max-width: 100vw;
    margin: 0;
  }
  .chat-header, .chat-input {
    border-radius: 0;
  }
  .chat-header {
    height: 60px;
    padding: 0 12px;
    position: fixed;
    top: 60px;
    left: 0;
    right: 0;
    z-index: 1000;
  }
  .chat-messages {
    padding-top: 135px;
    margin-top: 0;
  }
  .header-end-btn {
    font-size: 12px !important;
    padding: 6px 12px !important;
    height: 32px !important;
    min-width: 70px !important;
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
  
  .header-end-btn {
    font-size: 12px !important;
    padding: 4px 8px !important;
    height: 28px !important;
    min-width: 60px !important;
  }
  .chat-messages {
    padding: 8px;
    padding-top: 48px;
    margin-top: 70px;
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
  
  .send-btn {
    width: 38px !important;
    height: 38px !important;
    font-size: 16px !important;
    box-shadow: 0 2px 8px rgba(62, 181, 117, 0.25) !important;
  }
  
  .send-btn:hover {
    transform: translateY(-1px) scale(1.02) !important;
    box-shadow: 0 4px 12px rgba(62, 181, 117, 0.35) !important;
  }
  
  /* 移动端语音面板 */
  .voice-recording-panel {
    bottom: 80px !important;
    min-width: 200px !important;
    padding: 25px 20px 20px 20px !important;
  }
  
  .recording-visual {
    height: 70px !important;
  }
  
  .mic-icon-wrapper {
    width: 50px !important;
    height: 50px !important;
    margin: 0 !important;
  }
  
  .mic-icon {
    font-size: 20px !important;
  }
  
  .recording-text {
    font-size: 14px !important;
  }
  
  .recording-hint {
    font-size: 11px !important;
  }
  .el-avatar {
    width: 32px !important;
    height: 32px !important;
  }
  
  /* 移动端播放图标样式调整 */
  .play-icon {
    font-size: 12px;
    right: 6px;
  }
  
  .message-content.clickable:hover {
    transform: none;
  }
}
</style> 