<template>
  <div class="report-detail-container">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="floating-orb orb-1"></div>
      <div class="floating-orb orb-2"></div>
      <div class="floating-orb orb-3"></div>
      <div class="gradient-overlay"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="report-detail-card">
        <!-- 头部导航 -->
        <div class="detail-header">
          <el-button type="primary" @click="goBack" class="back-button">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h1>沟通评估详情</h1>
          <div class="header-date">{{ formatDate(reportData?.createdAt) }}</div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <p>加载中...</p>
        </div>

        <!-- 报告内容 -->
        <div v-else-if="reportData" class="report-content">
          <!-- 总体评分 -->
          <div class="score-overview-section">
            <div class="total-score-card">
              <div class="score-circle">
                <div class="score-number">{{ reportData.score }}</div>
              </div>
              <div class="score-description">总体评分</div>
            </div>
            
            <div class="score-breakdown-card">
              <h3>评分明细</h3>
              <div class="breakdown-grid">
                <div class="breakdown-item">
                  <div class="item-info">
                    <span class="item-name">沟通深度</span>
                    <span class="item-score">{{ reportData.depthScore }}/10</span>
                  </div>
                  <div class="item-bar">
                    <div class="bar-fill" :style="{ width: (reportData.depthScore / 10) * 100 + '%' }"></div>
                  </div>
                </div>
                
                <div class="breakdown-item">
                  <div class="item-info">
                    <span class="item-name">情感表达</span>
                    <span class="item-score">{{ reportData.emotionScore }}/10</span>
                  </div>
                  <div class="item-bar">
                    <div class="bar-fill" :style="{ width: (reportData.emotionScore / 10) * 100 + '%' }"></div>
                  </div>
                </div>
                
                <div class="breakdown-item">
                  <div class="item-info">
                    <span class="item-name">互动质量</span>
                    <span class="item-score">{{ reportData.interactionScore }}/10</span>
                  </div>
                  <div class="item-bar">
                    <div class="bar-fill" :style="{ width: (reportData.interactionScore / 10) * 100 + '%' }"></div>
                  </div>
                </div>
                
                <div class="breakdown-item">
                  <div class="item-info">
                    <span class="item-name">语言表达</span>
                    <span class="item-score">{{ reportData.languageScore }}/10</span>
                  </div>
                  <div class="item-bar">
                    <div class="bar-fill" :style="{ width: (reportData.languageScore / 10) * 100 + '%' }"></div>
                  </div>
                </div>
                
                <div class="breakdown-item">
                  <div class="item-info">
                    <span class="item-name">共情能力</span>
                    <span class="item-score">{{ reportData.empathyScore }}/10</span>
                  </div>
                  <div class="item-bar">
                    <div class="bar-fill" :style="{ width: (reportData.empathyScore / 10) * 100 + '%' }"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 专业评价 -->
          <div class="evaluation-section">
            <div class="section-card">
              <div class="section-header">
                <el-icon><Document /></el-icon>
                <h3>专业评价</h3>
              </div>
              <div class="evaluation-content">
                {{ reportData.evaluation }}
              </div>
            </div>
          </div>

          <!-- 改进建议 -->
          <div class="suggestions-section">
            <div class="section-card">
              <div class="section-header">
                <el-icon><TrendCharts /></el-icon>
                <h3>改进建议</h3>
              </div>
              <div class="suggestions-content">
                {{ reportData.suggestions }}
              </div>
            </div>
          </div>

          <!-- 积分奖励 -->
          <div class="rewards-section">
            <div class="rewards-card">
              <div class="reward-icon">
                <el-icon><Star /></el-icon>
              </div>
              <div class="reward-info">
                <span class="reward-text">本次评估获得积分</span>
                <span class="reward-points">{{ reportData.pointsAwarded }}</span>
              </div>
            </div>
          </div>

          <!-- 沟通内容 -->
          <div class="chat-history-section">
            <div class="section-card">
              <div class="section-header">
                <el-icon><ChatLineSquare /></el-icon>
                <h3>本次沟通内容</h3>
              </div>
              
              <div v-if="loadingChat" class="chat-loading">
                <el-icon class="loading-icon"><Loading /></el-icon>
                <span>聊天内容加载中...</span>
              </div>
              
              <div v-else-if="chatMessages.length === 0" class="chat-empty">
                <el-icon><MessageBox /></el-icon>
                <span>暂无聊天记录</span>
              </div>
              
              <div v-else class="chat-messages-container">
                <div class="chat-messages-list">
                  <div
                    v-for="msg in chatMessages.slice(-20)"
                    :key="msg.id"
                    :class="['chat-message-item', msg.senderType === 'user' ? 'from-user' : 'from-robot']"
                  >
                    <div class="bubble-col">
                      <div class="msg-bubble">
                        {{ msg.content }}
                      </div>
                      <div class="msg-time">{{ formatTime(msg.createdAt) }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 错误状态 -->
        <div v-else class="error-container">
          <el-icon class="error-icon"><Warning /></el-icon>
          <p>加载失败，请重试</p>
          <el-button type="primary" @click="loadReportData">重新加载</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserAvatarUrl } from '@/utils/avatar'
import { getChatHistoryByConversationId } from '@/api/chat'
import { 
  ArrowLeft, 
  Loading, 
  Document, 
  TrendCharts, 
  Star, 
  ChatLineSquare, 
  MessageBox, 
  Warning 
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 数据状态
const loading = ref(true)
const loadingChat = ref(false)
const reportData = ref(null)
const chatMessages = ref([])

// 从路由参数获取报告数据
const reportId = computed(() => route.params.id)

// 加载报告数据
const loadReportData = async () => {
  try {
    loading.value = true
    
    // 从sessionStorage获取报告数据
    const reportDataStr = sessionStorage.getItem(`report_${reportId.value}`)
    if (reportDataStr) {
      reportData.value = JSON.parse(reportDataStr)
      // 使用完后清除sessionStorage
      sessionStorage.removeItem(`report_${reportId.value}`)
    } else {
      // 如果没有，可以通过API获取
      // const response = await getCommunicationReportById(reportId.value)
      // reportData.value = response.data
      console.log('没有找到报告数据，ID:', reportId.value)
    }
    
    // 加载聊天记录
    if (reportData.value?.conversationId) {
      await loadChatHistory()
    }
  } catch (error) {
    console.error('加载报告数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载聊天记录
const loadChatHistory = async () => {
  try {
    loadingChat.value = true
    const response = await getChatHistoryByConversationId(reportData.value.conversationId)
    if (response.code === 200) {
      chatMessages.value = response.data || []
    }
  } catch (error) {
    console.error('加载聊天记录失败:', error)
  } finally {
    loadingChat.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化时间
const formatTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 页面挂载时加载数据
onMounted(() => {
  loadReportData()
})
</script>

<style scoped lang="scss">
.report-detail-container {
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
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.08), rgba(74, 222, 128, 0.04));
  filter: blur(50px);
  animation: float 25s ease-in-out infinite;
}

.orb-1 {
  width: 320px;
  height: 320px;
  top: 5%;
  left: 5%;
  animation-delay: 0s;
}

.orb-2 {
  width: 240px;
  height: 240px;
  top: 55%;
  right: 10%;
  animation-delay: -8s;
}

.orb-3 {
  width: 180px;
  height: 180px;
  bottom: 15%;
  left: 25%;
  animation-delay: -16s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  33% { transform: translateY(-40px) rotate(120deg); }
  66% { transform: translateY(25px) rotate(240deg); }
}

.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 50% 50%, transparent 0%, rgba(0, 0, 0, 0.015) 100%);
}

.main-content {
  position: relative;
  z-index: 1;
  padding: 120px 20px 40px;
  max-width: 1000px;
  margin: 0 auto;
}

.report-detail-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 30px;
  overflow: hidden;
  position: relative;
}

.report-detail-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.02), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.report-detail-card:hover::before {
  opacity: 1;
}

/* 头部样式 */
.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 2;
}

.back-button {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: var(--color-text);
  
  &:hover {
    background: rgba(34, 211, 107, 0.1);
    border-color: rgba(34, 211, 107, 0.3);
  }
}

.detail-header h1 {
  font-size: 1.8rem;
  font-weight: 600;
  background: linear-gradient(135deg, #22d36b, #4ade80);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  flex: 1;
  text-align: center;
}

.header-date {
  font-size: 0.9rem;
  color: var(--color-text);
  opacity: 0.7;
  min-width: 120px;
  text-align: right;
}

/* 加载和错误状态 */
.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.loading-icon,
.error-icon {
  font-size: 48px;
  color: #22d36b;
  margin-bottom: 16px;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-icon {
  color: #f56565;
}

.loading-container p,
.error-container p {
  font-size: 1.1rem;
  color: var(--color-text);
  margin: 0 0 20px;
}

/* 报告内容样式 */
.report-content {
  position: relative;
  z-index: 2;
}

.score-overview-section {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 30px;
  margin-bottom: 30px;
}

.total-score-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 30px;
  text-align: center;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  
  &:hover {
    border-color: rgba(34, 211, 107, 0.3);
    transform: translateY(-2px);
  }
}

.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, #22d36b, #4ade80);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  position: relative;
  margin: 0 auto 20px;
  box-shadow: 0 8px 25px rgba(34, 211, 107, 0.3);
}

.score-number {
  font-size: 2.5rem;
  font-weight: 700;
  line-height: 1;
}

.score-max {
  font-size: 1rem;
  position: absolute;
  bottom: 20px;
  right: 20px;
  opacity: 0.9;
}

.score-description {
  font-size: 1.1rem;
  color: var(--color-text);
  font-weight: 500;
}

.score-breakdown-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 30px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  
  &:hover {
    border-color: rgba(34, 211, 107, 0.3);
    transform: translateY(-2px);
  }
  
  h3 {
    margin: 0 0 20px;
    font-size: 1.2rem;
    color: var(--color-text);
    font-weight: 600;
  }
}

.breakdown-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.breakdown-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.item-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-name {
  font-size: 1rem;
  color: var(--color-text);
  font-weight: 500;
}

.item-score {
  font-size: 1rem;
  font-weight: 600;
  color: #22d36b;
}

.item-bar {
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #22d36b, #4ade80);
  border-radius: 4px;
  transition: width 0.6s ease;
  position: relative;
}

.bar-fill::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 2px;
  height: 100%;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}

/* 区域卡片样式 */
.evaluation-section,
.suggestions-section,
.chat-history-section {
  margin-bottom: 30px;
}

.section-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 25px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  
  &:hover {
    border-color: rgba(34, 211, 107, 0.3);
    transform: translateY(-2px);
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  
  .el-icon {
    font-size: 20px;
    color: #22d36b;
  }
  
  h3 {
    margin: 0;
    font-size: 1.2rem;
    color: var(--color-text);
    font-weight: 600;
  }
}

.evaluation-content,
.suggestions-content {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 20px;
  border-radius: 12px;
  line-height: 1.7;
  color: var(--color-text);
  font-size: 1rem;
}

/* 奖励样式 */
.rewards-section {
  margin-bottom: 30px;
}

.rewards-card {
  background: rgba(243, 156, 18, 0.1);
  border: 1px solid rgba(243, 156, 18, 0.3);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  transition: all 0.3s ease;
  
  &:hover {
    border-color: rgba(243, 156, 18, 0.5);
    transform: translateY(-2px);
  }
}

.reward-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: rgba(243, 156, 18, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f39c12;
  font-size: 24px;
}

.reward-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
}

.reward-text {
  font-size: 1.1rem;
  color: var(--color-text);
  font-weight: 500;
}

.reward-points {
  font-size: 1.5rem;
  font-weight: 700;
  color: #f39c12;
}

/* 聊天记录样式 */
.chat-loading,
.chat-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 40px 20px;
  color: var(--color-text);
  opacity: 0.7;
}

.chat-messages-container {
  max-height: 400px;
  overflow-y: auto;
  padding: 10px 0;
}

.chat-messages-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.chat-message-item {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  
  &.from-user {
    flex-direction: row-reverse;
  }
}

.avatar-col {
  flex-shrink: 0;
}

.msg-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.1);
}

.bubble-col {
  max-width: 70%;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.msg-bubble {
  padding: 12px 18px;
  border-radius: 18px;
  font-size: 1rem;
  line-height: 1.6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  word-break: break-word;
}

.chat-message-item.from-user .msg-bubble {
  background: linear-gradient(135deg, #22d36b, #4ade80);
  color: white;
  border-bottom-right-radius: 6px;
}

.chat-message-item.from-robot .msg-bubble {
  background: rgba(255, 255, 255, 0.1);
  color: var(--color-text);
  border-bottom-left-radius: 6px;
}

.chat-message-item.from-user .bubble-col {
  align-items: flex-end;
}

.chat-message-item.from-robot .bubble-col {
  align-items: flex-start;
}

.msg-time {
  font-size: 0.8rem;
  color: var(--color-text);
  opacity: 0.5;
  padding: 0 5px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .score-overview-section {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .total-score-card {
    padding: 25px;
  }
  
  .score-circle {
    width: 100px;
    height: 100px;
  }
  
  .score-number {
    font-size: 2rem;
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 100px 15px 30px;
  }
  
  .report-detail-card {
    padding: 20px;
  }
  
  .detail-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .detail-header h1 {
    font-size: 1.5rem;
    text-align: left;
  }
  
  .header-date {
    text-align: left;
    min-width: auto;
  }
  
  .score-circle {
    width: 90px;
    height: 90px;
  }
  
  .score-number {
    font-size: 1.8rem;
  }
  
  .section-card {
    padding: 20px;
  }
  
  .chat-messages-container {
    max-height: 300px;
  }
  
  .msg-bubble {
    padding: 10px 15px;
    font-size: 0.95rem;
  }
  
  .bubble-col {
    max-width: 80%;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 90px 12px 20px;
  }
  
  .report-detail-card {
    padding: 16px;
  }
  
  .detail-header h1 {
    font-size: 1.3rem;
  }
  
  .score-circle {
    width: 80px;
    height: 80px;
  }
  
  .score-number {
    font-size: 1.5rem;
  }
  
  .section-card {
    padding: 16px;
  }
  
  .breakdown-grid {
    gap: 15px;
  }
  
  .rewards-card {
    padding: 16px;
  }
  
  .reward-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
  
  .reward-text {
    font-size: 1rem;
  }
  
  .reward-points {
    font-size: 1.3rem;
  }
  
  .msg-avatar {
    width: 35px;
    height: 35px;
  }
}
</style>