<template>
  <div class="world-container">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="floating-orb orb-1"></div>
      <div class="floating-orb orb-2"></div>
      <div class="floating-orb orb-3"></div>
      <div class="gradient-overlay"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 加载状态 -->
      <div v-if="worldStore.loading" class="loading-container">
        <div class="loading-content">
          <div class="loading-spinner"></div>
          <p>正在加载伊甸园世界...</p>
        </div>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="worldStore.error" class="error-container">
        <div class="error-content">
          <div class="error-icon">
            <el-icon size="64"><CircleClose /></el-icon>
          </div>
          <h3>加载失败</h3>
          <p>{{ worldStore.error }}</p>
          <div class="custom-button retry-button" @click="retryLoad">
            <el-icon><Refresh /></el-icon>
            <span>重新加载</span>
          </div>
        </div>
      </div>

      <!-- 主要内容 -->
      <div v-else-if="worldStore.isWorldLoaded" class="content-wrapper">
        <!-- 新增：机器人每日计划入口 -->
        <!-- 世界基本信息 -->
        <div class="world-info-section">
          <div class="world-info-card">
            <div class="world-header">
              <div class="world-title">
                <h1>{{ worldStore.worldInfo.name }}</h1>
                <div class="world-badge">
                  <el-icon><Compass /></el-icon>
                  <span>伊甸园世界</span>
                </div>
              </div>
              <p class="world-description">{{ worldStore.worldInfo.description }}</p>
              <div class="world-stats">
                <div class="stat-item">
                  <span class="stat-number">{{ worldStore.totalRobots }}</span>
                  <span class="stat-label">个天使</span>
                </div>
                <div class="stat-item">
                  <span class="stat-number">{{ worldStore.worldStatistics?.activeRobots || 0 }}</span>
                  <span class="stat-label">在线天使</span>
                </div>
                <div class="stat-item">
                  <span class="stat-number">{{ worldStore.worldStatistics?.totalPosts || 0 }}</span>
                  <span class="stat-label">总动态</span>
                </div>
              </div>
            </div>
            <div class="world-card-bg"></div>
          </div>
        </div>

        <!-- 机器人列表 -->
        <div class="robots-section">
          <div class="section-header">
            <div class="header-left">
              <h2>天使们</h2>
              <p>与天使们进行互动交流，体验温暖的社交氛围</p>
            </div>
            <div class="header-right">
              <button class="header-action-btn daily-plan-btn" @click="$router.push('/robot-daily-plan')">
                <el-icon><Calendar /></el-icon>
                <span>天使的每一天</span>
              </button>              
              <button class="header-action-btn create-btn" @click="createRobot">
                <el-icon><Plus /></el-icon>
                <span>创建天使</span>
              </button>            
            </div>
          </div>
          
          <!-- 过滤控制区域 -->
          <div class="filter-controls">
            <div class="filter-group">
              <el-input
                  v-model="searchKeyword"
                  placeholder="搜索天使名称或描述..."
                  clearable
                  class="search-input"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </div>
              <div class="status-filter-wrapper">
                <el-select
                  v-model="statusFilter"
                  placeholder="选择状态"
                  class="status-filter"
                  clearable
                >
                  <el-option label="全部" value="all" />
                  <el-option label="已连接" value="linked" />
                  <el-option label="未连接" value="unlinked" />
                </el-select>
              </div>
            </div>
            <div class="filter-stats">
              <span class="filter-count">显示 {{ filteredRobots.length }} / {{ worldStore.robotList.length }} 个天使</span>
            </div>
          </div>
          
          <div class="robots-grid">
            <div 
              v-for="robot in filteredRobots" 
              :key="robot.id" 
              class="robot-card"
            >
              <div class="robot-content">
                <div class="robot-avatar-section" 
                @click="goToChat(robot, $event)">
                  <div class="robot-avatar">
                    <!-- 熟悉度进度环 - 套在头像外侧 -->
                    <div class="avatar-progress-ring" v-if="isRobotLinkCreated(robot.id)">
                      <svg class="avatar-progress-circle" viewBox="0 0 100 100">
                        <!-- 背景圆环 -->
                        <circle
                          cx="50"
                          cy="50"
                          r="46"
                          fill="none"
                          stroke="rgba(255, 255, 255, 0.1)"
                          stroke-width="4"
                        />
                        <!-- 进度圆环 -->
                        <circle
                          cx="50"
                          cy="50"
                          r="46"
                          fill="none"
                          :stroke="getFamiliarityColor(robot.id)"
                          stroke-width="4"
                          stroke-linecap="round"
                          :stroke-dasharray="289"
                          :stroke-dashoffset="289 - (289 * getFamiliarityProgress(robot.id))"
                          class="avatar-progress-bar"
                        />
                      </svg>
                    </div>
                    <el-avatar :src="getRobotAvatarUrl(robot)" :size="80" :title="getFamiliarityTooltip(robot.id)" />
                    <!-- 消息红点 -->
                    <div v-if="hasUnreadMessage(robot.id)" class="message-red-dot"></div>
                    <div class="robot-status" :class="{ active: robot.active }">
                      <el-icon v-if="robot.active" class="status-icon">
                        <CircleCheck />
                      </el-icon>
                      <el-icon v-else class="status-icon">
                        <CircleClose />
                      </el-icon>
                      {{ robot.active ? '在线' : '离线' }}
                    </div>
                  </div>
                  <div class="robot-quick-info">
                    <h3 :title="getFamiliarityTooltip(robot.id)">{{ robot.name }}</h3>
                    <!-- <div class="robot-personality">
                      <el-tag size="small" type="info" style="width: 100px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ robot.personality }}</el-tag>
                    </div> -->
                  </div>
                </div>
                <div class="robot-details">
                  <p class="robot-intro">{{ robot.description }}</p>
                  <div class="robot-tags">
                    <span class="tag-item">👼 {{ robot.nickname }}</span>
                    <!-- 链接状态标签 -->
                    <div class="link-status-tag" :class="getLinkStatusClass(robot.id)">
                      <el-icon><Connection /></el-icon>
                      <span>{{ getLinkStatusText(robot.id) }}</span>
                    </div>
                    <button class="detail-btn" @click="showRobotDetail(robot)" title="查看详情">
                      <el-icon><InfoFilled /></el-icon>
                      <span>详情</span>
                    </button>
                  </div>
                  
                  <!-- 机器人控制区域 -->
                  <div class="robot-controls">
                    <!-- 操作按钮 -->
                    <div class="robot-action-buttons">
                      <button 
                        class="action-btn link-btn"
                        :class="{ 
                          'linked': isRobotLinked(robot.id),
                          'loading': linkLoadingStates.get(robot.id)
                        }"
                        @click="toggleRobotLink(robot)"
                        :disabled="linkLoadingStates.get(robot.id)"
                      >
                        <div v-if="linkLoadingStates.get(robot.id)" class="loading-spinner-small"></div>
                        <el-icon v-else>
                          <Connection />
                        </el-icon>
                        <span>{{ isRobotLinked(robot.id) ? '断开' : '链接' }}</span>
                      </button>

                      <button 
                        v-if="isMyRobot(robot.id)" 
                        class="action-btn edit-btn"
                        @click="editRobot(robot.id)"
                        title="编辑机器人"
                      >
                        <el-icon><Edit /></el-icon>
                        <span>编辑</span>
                      </button>
                      <button
                        v-if="isRobotLinkCreated(robot.id)"
                        class="action-btn impression-btn"
                        @click="openImpressionPanel(robot)"
                      >
                        <el-icon><ChatLineRound /></el-icon>
                        <span>印象</span>
                      </button>
                    </div>                                      
                  </div>
                </div>
              </div>
              <div class="robot-card-bg"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

  <!-- 印象编辑弹层 -->
  <div
    v-if="impressionPanelVisible"
    class="impression-overlay"
    @click.self="closeImpressionPanel"
  >
    <div
      class="impression-panel"
      :class="{ mobile: isMobile }"
    >
      <div class="impression-header">
        <span>编辑机器人对我的印象</span>
        <button class="close-btn" @click="closeImpressionPanel">×</button>
      </div>
      <textarea
        v-model="impressionText"
        rows="6"
        maxlength="500"
        class="impression-textarea"
        placeholder="请输入你希望机器人对你的印象（如性格、习惯、兴趣等）"
      ></textarea>
      <div class="impression-footer">
        <span class="word-limit">{{ impressionText.length }}/500</span>
        <button class="save-btn" @click="saveImpression">保存</button>
      </div>
    </div>
  </div>
  <!-- 专家主题选择菜单 -->
  <div v-if="themeMenuVisible" class="theme-menu-overlay" @click.self="closeThemeMenu">
    <transition name="theme-menu-fade">
      <div
        v-if="themeMenuVisible"
        class="theme-menu"
        :class="{ mobile: isMobile }"
        :style="!isMobile ? { left: themeMenuPosition.x + 'px', top: themeMenuPosition.y + 'px' } : {}"
      >
        <div class="theme-menu-title">请选择聊天主题</div>
        <div class="theme-menu-list">
          <div v-for="item in themeMenuOptions" :key="item.id" class="theme-menu-item" @click="selectTheme(item)">
            {{ item.name }}
          </div>
        </div>
        <div class="theme-menu-cancel" @click="closeThemeMenu">取消</div>
      </div>
    </transition>
  </div>

  <!-- 机器人详情弹窗 -->
  <div v-if="robotDetailVisible" class="robot-detail-overlay" @click.self="closeRobotDetail">
    <div class="robot-detail-modal" :class="{ mobile: isMobile }">
      <div class="robot-detail-header">
        <div class="robot-detail-title">
          <div class="robot-avatar-wrapper">
            <el-avatar :src="getRobotAvatarUrl(currentRobotDetail)" :size="60" />
            <div class="robot-status-badge" :class="{ active: currentRobotDetail?.active }">
              <el-icon v-if="currentRobotDetail?.active" class="status-icon"><CircleCheck /></el-icon>
              <el-icon v-else class="status-icon"><CircleClose /></el-icon>
              {{ currentRobotDetail?.active ? '在线' : '离线' }}
            </div>
          </div>
        </div>
        <button class="close-btn" @click="closeRobotDetail">×</button>
      </div>
      
      <div class="robot-detail-content">
        <div class="robot-info-section">
          <h4>基本信息</h4>
          <div class="robot-info-grid">
            <div class="info-row">
              <div class="info-item">
                <span class="info-label">姓名：</span>
                <span class="info-value">{{ currentRobotDetail?.name }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">昵称：</span>
                <span class="info-value">{{ currentRobotDetail?.nickname }}</span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-item">
                <span class="info-label">性别：</span>
                <span class="info-value">{{ getGenderText(currentRobotDetail?.gender) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">年龄：</span>
                <span class="info-value">{{ currentRobotDetail?.age }}岁</span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-item full-width">
                <span class="info-label">性格特点：</span>
                <span class="info-value">{{ currentRobotDetail?.personality }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="robot-description-section">
          <h4>详细介绍</h4>
          <div class="robot-description-scroll">
            <div class="robot-description-content">
                <p>{{ currentRobotDetail.description }} {{ currentRobotDetail.background }} {{ currentRobotDetail.family }} 目前在{{ currentRobotDetail.location }}</p>
            </div>
          </div>
        </div>
            
      </div>
      
      <div class="robot-detail-footer">
        <button class="chat-btn" @click="goToChat(currentRobotDetail); closeRobotDetail()">
          <el-icon><ChatDotRound /></el-icon>
          <span>开始聊天</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useWorldStore } from '@/stores/world'
import { ElMessageBox } from 'element-plus'
import { message } from '@/utils/message'
import { CircleCheck, CircleClose, Refresh, Menu, Close, House, ChatDotRound, Compass, User, SwitchButton, Search, Plus, Edit, Calendar, ChatLineRound, InfoFilled, Connection } from '@element-plus/icons-vue'
import { getUserAvatarUrl, getRobotAvatarUrl } from '@/utils/avatar'
import { 
  createUserRobotLink, 
  deleteUserRobotLink, 
  activateUserRobotLink, 
  deactivateUserRobotLink,
  getUserRobotLinks,
  updateUserRobotLink,
  clearPendingMessage
} from '@/api/userRobotLink'
import { getMyRobots } from '@/api/robotEditor'

// 响应式数据
const router = useRouter()
const userStore = useUserStore()
const worldStore = useWorldStore()
const activeMenu = ref('/world')
const isMobileMenuOpen = ref(false)

// 用户机器人链接状态
const userRobotLinks = ref(new Map()) // 存储用户与机器人的链接状态
const linkLoadingStates = ref(new Map()) // 存储链接操作的加载状态

// 用户拥有的机器人
const myRobots = ref(new Set()) // 存储用户拥有的机器人ID集合

// 过滤相关状态
const searchKeyword = ref('')
const statusFilter = ref('all')

// 印象相关状态
const impressionPanelVisible = ref(false)
const impressionText = ref('')
const editingRobotId = ref(null)
const isMobile = computed(() => window.innerWidth <= 600)

// 专家主题菜单相关
const themeMenuVisible = ref(false)
const themeMenuOptions = ref([])
const themeMenuRobot = ref(null)
const themeMenuPosition = ref({ x: 0, y: 0 })
const selectedThemeId = ref(null)

// 机器人详情弹窗相关
const robotDetailVisible = ref(false)
const currentRobotDetail = ref(null)

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 过滤后的机器人列表
const filteredRobots = computed(() => {
  let robots = worldStore.robotList

  // 关键词过滤
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase().trim()
    robots = robots.filter(robot => 
      robot.name.toLowerCase().includes(keyword) ||
      robot.description.toLowerCase().includes(keyword) ||
      robot.personality.toLowerCase().includes(keyword) ||
      robot.nickname.toLowerCase().includes(keyword)
    )
  }

  // 状态过滤
  if (statusFilter.value && statusFilter.value !== 'all') {
    robots = robots.filter(robot => {
      const isLinked = isRobotLinked(robot.id)
      return statusFilter.value === 'linked' ? isLinked : !isLinked
    })
  }

  // 按照优先级排序：有消息最前 > 熟悉度由多到少 > 有link的在前面 > 在线状态
  return robots.sort((a, b) => {
    const aLink = userRobotLinks.value.get(a.id)
    const bLink = userRobotLinks.value.get(b.id)
    
    // 1. 有待处理消息的优先级最高
    const aHasPendingMessage = aLink?.hasPendingMessage || false
    const bHasPendingMessage = bLink?.hasPendingMessage || false
    
    if (aHasPendingMessage !== bHasPendingMessage) {
      return bHasPendingMessage ? 1 : -1
    }
    
    // 2. 按熟悉度积分由多到少排序
    const aFamiliarityScore = aLink?.familiarityScore || 0
    const bFamiliarityScore = bLink?.familiarityScore || 0
    
    if (aFamiliarityScore !== bFamiliarityScore) {
      return bFamiliarityScore - aFamiliarityScore
    }
    
    // 3. 有link的在前面
    const aHasLink = isRobotLinkCreated(a.id)
    const bHasLink = isRobotLinkCreated(b.id)
    
    if (aHasLink !== bHasLink) {
      return bHasLink ? 1 : -1
    }
    
    // 4. 在线状态排序（在线在前）
    const aActive = a.active || false
    const bActive = b.active || false
    
    if (aActive !== bActive) {
      return bActive ? 1 : -1
    }
    
    // 5. 最后按机器人名称排序
    return a.name.localeCompare(b.name)
  })
})

// 检查机器人是否已链接
const isRobotLinked = (robotId) => {
  return userRobotLinks.value.has(robotId) && userRobotLinks.value.get(robotId).active
}

// 检查机器人是否已创建链接（包括非激活状态）
const isRobotLinkCreated = (robotId) => {
  return userRobotLinks.value.has(robotId)
}

// 方法
const retryLoad = async () => {
  try {
    await worldStore.initWorld()
    message.success('重新加载成功')
  } catch (error) {
    console.error('重新加载失败:', error)
    message.error('重新加载失败')
  }
}

// 加载用户机器人链接
const loadUserRobotLinks = async () => {
  try {
    const response = await getUserRobotLinks()
    if (response.code === 200 && response.data) {
      const linksMap = new Map()
      response.data.forEach(link => {
        // 将后端的status字段转换为前端期望的active字段
        const convertedLink = {
          ...link,
          active: link.status === 'active'
        }
        linksMap.set(link.robotId, convertedLink)
        console.log(`转换链接数据 - robotId: ${link.robotId}, status: ${link.status}, active: ${convertedLink.active}`)
      })
      userRobotLinks.value = linksMap
      console.log('用户机器人链接加载成功:', linksMap)
    }
  } catch (error) {
    console.error('加载用户机器人链接失败:', error)
  }
}

// 加载用户拥有的机器人
const loadMyRobots = async () => {
  try {
    const response = await getMyRobots()
    if (response.code === 200 && response.data) {
      const robotIds = new Set()
      response.data.forEach(robot => {
        robotIds.add(robot.robotId)
      })
      myRobots.value = robotIds
      console.log('用户拥有的机器人加载成功:', robotIds)
    }
  } catch (error) {
    console.error('加载用户拥有的机器人失败:', error)
  }
}

// 切换机器人链接状态
const toggleRobotLink = async (robot) => {
  const robotId = robot.id
  const isLinked = isRobotLinked(robotId)
  const isCreated = isRobotLinkCreated(robotId)
  
  // 设置加载状态
  linkLoadingStates.value.set(robotId, true)
  
  try {
    if (isLinked) {
      // 如果已链接，则停用链接
      await deactivateUserRobotLink(robotId)
      userRobotLinks.value.get(robotId).active = false
      message.success(`已停用与 ${robot.name} 的链接`)
    } else if (isCreated) {
      // 如果已创建但未激活，则激活链接
      await activateUserRobotLink(robotId)
      userRobotLinks.value.get(robotId).active = true
      message.success(`已激活与 ${robot.name} 的链接`)
    } else {
      // 如果未创建链接，则创建并激活
      const response = await createUserRobotLink(robotId)
      if (response.code === 200 && response.data) {
        userRobotLinks.value.set(robotId, {
          robotId: robotId,
          active: true,
          status: 'active',
          createdAt: response.data.createdAt
        })
        message.success(`已创建与 ${robot.name} 的链接`)
      }
    }
  } catch (error) {
    console.error('切换机器人链接失败:', error)
    message.error('操作失败，请重试')
  } finally {
    linkLoadingStates.value.set(robotId, false)
  }
}

// 获取链接状态文本
const getLinkStatusText = (robotId) => {
  if (!isRobotLinkCreated(robotId)) {
    return '未链接'
  }
  return isRobotLinked(robotId) ? '已链接' : '已停用'
}

// 获取链接状态样式类
const getLinkStatusClass = (robotId) => {
  if (!isRobotLinkCreated(robotId)) {
    return 'link-status-unlinked'
  }
  return isRobotLinked(robotId) ? 'link-status-linked' : 'link-status-inactive'
}

// 计算属性：是否为开发环境
const isDevelopment = computed(() => import.meta.env.DEV)

// 生命周期
onMounted(async () => {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }
  
  try {
    await worldStore.initWorld()
    // 加载用户机器人链接
    await loadUserRobotLinks()
    // 加载用户拥有的机器人
    await loadMyRobots()
  } catch (error) {
    console.error('初始化世界数据失败:', error)
    message.error('加载世界数据失败')
  }
  
  // 添加点击外部关闭移动端菜单的监听
  document.addEventListener('click', handleClickOutside)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

// 点击外部区域关闭移动端菜单
const handleClickOutside = (event) => {
  const header = document.querySelector('.header')
  if (header && !header.contains(event.target) && isMobileMenuOpen.value) {
    isMobileMenuOpen.value = false
  }
}

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

const navigateTo = (path) => {
  router.push(path)
  // 移动端导航后关闭菜单
  isMobileMenuOpen.value = false
}

// 创建机器人
const createRobot = () => {
  router.push('/robot-editor')
}

// 编辑机器人
const editRobot = (robotId) => {
  router.push(`/robot-editor/${robotId}`)
}

// 检查机器人是否为用户拥有
const isMyRobot = (robotId) => {
  return myRobots.value.has(robotId)
}

function openImpressionPanel(robot) {
  editingRobotId.value = robot.id
  impressionText.value = userRobotLinks.value.get(robot.id)?.impression || ''
  impressionPanelVisible.value = true
}
function closeImpressionPanel() {
  impressionPanelVisible.value = false
}
async function saveImpression() {
  const link = userRobotLinks.value.get(editingRobotId.value)
  if (link) {
    link.impression = impressionText.value
    await updateUserRobotLink(link)
    impressionPanelVisible.value = false
    message.success('印象已保存')
  }
}

// 专家主题菜单相关
function showThemeMenu(robot, event) {
  themeMenuRobot.value = robot
  themeMenuOptions.value = [
    ...(robot.expertThemes || []).map(t => ({ id: t.id, name: t.name })),
    { id: '', name: '随便聊聊' }
  ]
  if (!isMobile.value && event) {
    themeMenuPosition.value = { x: event.clientX, y: event.clientY }
  } else {
    themeMenuPosition.value = { x: 0, y: 0 }
  }
  themeMenuVisible.value = true
  nextTick(() => {})
}
function closeThemeMenu() {
  themeMenuVisible.value = false
}
function selectTheme(theme) {
  closeThemeMenu()
  // 跳转到chat，带themeId参数
  router.push({
    path: `/chat/${themeMenuRobot.value.id}`,
    query: theme.id ? { themeId: theme.id } : {}
  })
}

// 修改goToChat
const goToChat = async (robot, event) => {
  // 清除红点
  await clearMessageRedDot(robot.id)
  if (robot.expertThemes && robot.expertThemes.length > 0) {
    showThemeMenu(robot, event)
  } else {
    router.push(`/chat/${robot.id}`)
  }
}

// 熟悉度相关方法
const getFamiliarityProgress = (robotId) => {
  const link = userRobotLinks.value.get(robotId)
  if (!link) return 0
  // 以每个等级10分为例，计算当前等级内的进度
  const score = link.familiarityScore || 0
  const level = link.familiarityLevel || 0
  let min = 0, max = 0
  if (level === 0) { min = 0; max = 1 }
  else if (level === 1) { min = 1; max = 10 }
  else if (level === 2) { min = 11; max = 30 }
  else if (level === 3) { min = 31; max = 60 }
  else if (level === 4) { min = 61; max = 100 }
  // 进度百分比
  return max > min ? (score - min) / (max - min) : 0
}

const getFamiliarityLevelText = (robotId) => {
  const link = userRobotLinks.value.get(robotId)
  if (!link) return '陌生人'
  return link.familiarityLevelName || '陌生人'
}

const getFamiliarityColor = (robotId) => {
  const link = userRobotLinks.value.get(robotId)
  if (!link) return '#666'
  const level = link.familiarityLevel || 0
  const colors = {
    0: '#8B5CF6', // 陌生人-紫
    1: '#06B6D4', // 初识-青
    2: '#10B981', // 朋友-绿
    3: '#F59E0B', // 好友-黄
    4: '#EF4444'  // 密友-红
  }
  return colors[level] || '#666'
}

// 新增方法getFamiliarityTooltip(robotId): 返回如"朋友（11-30分）：已经比较熟悉，可以进行日常聊天"
const getFamiliarityTooltip = (robotId) => {
  const link = userRobotLinks.value.get(robotId)
  if (!link) return '陌生人（0分）：还不太熟悉的陌生人'
  const level = link.familiarityLevel || 0
  const name = link.familiarityLevelName || '陌生人'
  const score = link.familiarityScore || 0
  let range = '', desc = ''
  if (level === 0) { range = '0分'; desc = '还不太熟悉的陌生人' }
  else if (level === 1) { range = '1-10分'; desc = '刚刚认识的新朋友，还在相互了解中' }
  else if (level === 2) { range = '11-30分'; desc = '已经比较熟悉，可以进行日常聊天' }
  else if (level === 3) { range = '31-60分'; desc = '关系很好的朋友，经常互动交流' }
  else if (level === 4) { range = '61分及以上'; desc = '非常亲密的伙伴，彼此信任和依赖' }
  return `${name}（${range}，当前${score}分）：${desc}`
}

// 消息红点相关方法
const hasUnreadMessage = (robotId) => {
  const link = userRobotLinks.value.get(robotId)
  return link?.hasPendingMessage || false
}

const clearMessageRedDot = async (robotId) => {
  try {
    await clearPendingMessage(robotId)
    // 更新本地状态
    const link = userRobotLinks.value.get(robotId)
    if (link) {
      link.hasPendingMessage = false
    }
  } catch (error) {
    console.error('清除消息红点失败:', error)
  }
}

// 显示机器人详情
const showRobotDetail = (robot) => {
  currentRobotDetail.value = robot
  robotDetailVisible.value = true
}

// 关闭机器人详情
const closeRobotDetail = () => {
  robotDetailVisible.value = false
  currentRobotDetail.value = null
}

// 获取性别中文显示
const getGenderText = (gender) => {
  const genderMap = {
    'male': '男',
    'female': '女',
    'other': '其他'
  }
  return genderMap[gender] || gender || '未知'
}
</script>

<style scoped>
.world-container {
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
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.1), rgba(74, 222, 128, 0.05));
  filter: blur(40px);
  animation: float 20s ease-in-out infinite;
}

.orb-1 {
  width: 300px;
  height: 300px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 200px;
  height: 200px;
  top: 60%;
  right: 15%;
  animation-delay: -7s;
}

.orb-3 {
  width: 150px;
  height: 150px;
  bottom: 20%;
  left: 20%;
  animation-delay: -14s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  33% { transform: translateY(-30px) rotate(120deg); }
  66% { transform: translateY(20px) rotate(240deg); }
}

.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 50% 50%, transparent 0%, rgba(0, 0, 0, 0.02) 100%);
}

.main-content {
  position: relative;
  z-index: 1;
  max-width: 1400px;
  margin: 0 auto;
  padding: 80px 20px 40px;
}

/* 加载状态 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.loading-content {
  text-align: center;
  color: var(--color-text);
}

.loading-spinner {
  width: 60px;
  height: 60px;
  border: 3px solid rgba(34, 211, 107, 0.2);
  border-top: 3px solid #22d36b;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-content p {
  font-size: 1.1rem;
  opacity: 0.8;
}

/* 错误状态 */
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.error-content {
  text-align: center;
  color: var(--color-text);
  max-width: 400px;
}

.error-icon {
  color: #ff4d4f;
  margin-bottom: 20px;
}

.error-content h3 {
  font-size: 1.5rem;
  margin-bottom: 12px;
  color: var(--color-text);
}

.error-content p {
  font-size: 1rem;
  opacity: 0.8;
  margin-bottom: 30px;
  line-height: 1.6;
}

.custom-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  outline: none;
}

.retry-button {
  background: linear-gradient(135deg, #22d36b, #4ade80);
  color: white;
  box-shadow: 0 4px 12px rgba(34, 211, 107, 0.3);
}

.retry-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(34, 211, 107, 0.4);
}

/* 世界信息区域 */
.world-info-section {
  margin-bottom: 60px;
}

.world-info-card {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 60px 40px;
  overflow: hidden;
}

.world-header {
  text-align: center;
  position: relative;
  z-index: 2;
}

.world-title {
  margin-bottom: 24px;
}

.world-title h1 {
  font-size: 3rem;
  font-weight: 800;
  background: linear-gradient(135deg, #22d36b, #4ade80, #86efac);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 16px;
  line-height: 1.1;
}

.world-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(34, 211, 107, 0.1);
  color: #22d36b;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

.world-description {
  font-size: 1.2rem;
  line-height: 1.7;
  color: var(--color-text);
  opacity: 0.9;
  margin-bottom: 40px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.world-stats {
  display: flex;
  justify-content: center;
  gap: 60px;
  flex-wrap: wrap;
}

.stat-item {
  text-align: center;
  min-width: 100px;
}

.stat-number {
  display: block;
  font-size: 2.5rem;
  font-weight: 700;
  color: #22d36b;
  margin-bottom: 8px;
  line-height: 1;
}

.stat-label {
  font-size: 1rem;
  color: var(--color-text);
  opacity: 0.7;
  white-space: nowrap;
}

.world-card-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.02), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.world-info-card:hover .world-card-bg {
  opacity: 1;
}

/* 机器人区域 */
.robots-section {
  margin-bottom: 60px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 50px;
  gap: 20px;
}

.header-left {
  flex: 1;
}

.header-right {
  flex-shrink: 0;
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 头部操作按钮样式 */
.header-action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid rgba(64, 158, 255, 0.3);
  border-radius: 12px;
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  outline: none;
  white-space: nowrap;
}

.header-action-btn:hover {
  background: rgba(64, 158, 255, 0.2);
  border-color: #409eff;
  transform: translateY(-1px);
}

.header-action-btn:active {
  transform: translateY(0);
}

/* 每日计划按钮特殊样式 */
.header-action-btn.daily-plan-btn {
  border-color: rgba(255, 193, 7, 0.3);
  background: rgba(255, 193, 7, 0.1);
  color: #ffc107;
}

.header-action-btn.daily-plan-btn:hover {
  background: rgba(255, 193, 7, 0.2);
  border-color: #ffc107;
}

/* 创建按钮特殊样式 */
.header-action-btn.create-btn {
  border-color: rgba(34, 211, 107, 0.3);
  background: rgba(34, 211, 107, 0.1);
  color: #22d36b;
}

.header-action-btn.create-btn:hover {
  background: rgba(34, 211, 107, 0.2);
  border-color: #22d36b;
}

/* 过滤控制区域 */
.filter-controls {
  margin-bottom: 40px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
}

.filter-group {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}

.search-input-wrapper {
  flex: 1;
  min-width: 0;
}

.search-input {
  width: 100%;
}

.search-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  box-shadow: none;
  transition: all 0.3s ease;
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(34, 211, 107, 0.3);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #22d36b;
  box-shadow: 0 0 0 2px rgba(34, 211, 107, 0.1);
}

.search-input :deep(.el-input__inner) {
  color: var(--color-text);
  font-size: 0.95rem;
}

.search-input :deep(.el-input__inner::placeholder) {
  color: var(--color-text);
  opacity: 0.5;
}

.search-input :deep(.el-input__prefix) {
  color: rgba(255, 255, 255, 0.6);
}

.status-filter-wrapper {
  flex-shrink: 0;
  min-width: 140px;
}

.status-filter {
  width: 100%;
}

.status-filter :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  box-shadow: none;
  transition: all 0.3s ease;
}

.status-filter :deep(.el-input__wrapper:hover) {
  border-color: rgba(34, 211, 107, 0.3);
}

.status-filter :deep(.el-input__wrapper.is-focus) {
  border-color: #22d36b;
  box-shadow: 0 0 0 2px rgba(34, 211, 107, 0.1);
}

.status-filter :deep(.el-input__inner) {
  color: var(--color-text);
  font-size: 0.95rem;
}

.status-filter :deep(.el-select__caret) {
  color: rgba(255, 255, 255, 0.6);
}

.filter-stats {
  display: flex;
  justify-content: flex-end;
}

.filter-count {
  font-size: 0.9rem;
  color: var(--color-text);
  opacity: 0.7;
  font-weight: 500;
}

.section-header h2 {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 16px;
}

.section-header p {
  font-size: 1.1rem;
  color: var(--color-text);
  opacity: 0.7;
}

.robots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 30px;
}

.robot-card {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 30px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.robot-card:hover {
  transform: translateY(-8px);
  border-color: rgba(34, 211, 107, 0.3);
  box-shadow: 0 20px 60px rgba(34, 211, 107, 0.15);
}

.robot-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.robot-avatar-section {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  cursor: pointer;
}

.robot-avatar {
  position: relative;
}

.robot-status {
  position: absolute;
  bottom: -4px;
  right: -4px;
  background: #ff4d4f;
  color: #fff;
  padding: 4px 12px 4px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  border: 3px solid var(--color-bg);
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
  font-weight: 600;
  z-index: 2;
  transition: all 0.3s ease;
}

.robot-status.active {
  background: #22d36b;
}

.status-icon {
  font-size: 10px;
  filter: drop-shadow(0 0 2px #0008);
}

.robot-quick-info {
  text-align: center;
}

.robot-quick-info h3 {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.robot-personality {
  display: flex;
  justify-content: center;
}

.robot-details {
  flex: 1;
  min-width: 200px;
}

.robot-intro {
  color: var(--color-text);
  line-height: 1.6;
  font-size: 0.95rem;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.robot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  background: rgba(34, 211, 107, 0.1);
  color: #22d36b;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 0.8rem;
  font-weight: 500;
}

.tag-item.online {
  background: rgba(34, 211, 107, 0.15);
  color: #22d36b;
}

.tag-item.offline {
  background: rgba(255, 77, 79, 0.15);
  color: #ff4d4f;
}

/* 链接状态标签样式 */
.link-status-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 0.8rem;
  font-weight: 500;
  white-space: nowrap;
}

.link-status-tag.link-status-unlinked {
  background: rgba(255, 255, 255, 0.08);
  color: var(--color-text);
  opacity: 0.7;
}

.link-status-tag.link-status-linked {
  background: rgba(34, 211, 107, 0.15);
  color: #22d36b;
}

.link-status-tag.link-status-inactive {
  background: rgba(255, 193, 7, 0.15);
  color: #ffc107;
}

.detail-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border: 1px solid rgba(64, 158, 255, 0.3);
  border-radius: 12px;
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  outline: none;
}

.detail-btn:hover {
  background: rgba(64, 158, 255, 0.2);
  border-color: #409eff;
  transform: translateY(-1px);
}

.detail-btn:active {
  transform: translateY(0);
}

.robot-card-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.02), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.robot-card:hover .robot-card-bg {
  opacity: 1;
}

/* 机器人控制区域样式 */
.robot-controls {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

/* 机器人操作按钮容器 */
.robot-action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

/* 统一的操作按钮样式 */
.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border: 1px solid rgba(64, 158, 255, 0.3);
  border-radius: 12px;
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  outline: none;
  white-space: nowrap;
}

.action-btn:hover:not(:disabled) {
  background: rgba(64, 158, 255, 0.2);
  border-color: #409eff;
  transform: translateY(-1px);
}

.action-btn:active {
  transform: translateY(0);
}

/* 链接按钮特殊样式 */
.action-btn.link-btn {
  border-color: rgba(34, 211, 107, 0.3);
  background: rgba(34, 211, 107, 0.1);
  color: #22d36b;
}

.action-btn.link-btn:hover:not(:disabled) {
  background: rgba(34, 211, 107, 0.2);
  border-color: #22d36b;
}

.action-btn.link-btn.linked {
  background: rgba(255, 77, 79, 0.1);
  border-color: rgba(255, 77, 79, 0.3);
  color: #ff4d4f;
}

.action-btn.link-btn.linked:hover:not(:disabled) {
  background: rgba(255, 77, 79, 0.2);
  border-color: #ff4d4f;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 印象按钮使用与编辑按钮相同的蓝色系样式 */
.action-btn.impression-btn {
  /* 使用默认的蓝色系样式，与编辑按钮保持一致 */
}

.loading-spinner-small {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(34, 211, 107, 0.2);
  border-top: 2px solid #22d36b;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.action-btn.link-btn.linked .loading-spinner-small {
  border-color: rgba(255, 77, 79, 0.2);
  border-top-color: #ff4d4f;
}

/* 头像进度环样式 */
.avatar-progress-ring {
  position: absolute;
  top: -6px;
  left: -6px;
  width: 92px;
  height: 92px;
  z-index: 1;
}

/* 消息红点样式 */
.message-red-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 16px;
  height: 16px;
  background: #ff4757;
  border-radius: 50%;
  border: 2px solid var(--color-bg);
  z-index: 3;
  animation: pulse-dot 2s infinite;
}

@keyframes pulse-dot {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.avatar-progress-circle {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
  overflow: visible;
}

.avatar-progress-bar {
  transition: stroke-dashoffset 0.6s ease-in-out;
  filter: drop-shadow(0 0 3px rgba(0, 0, 0, 0.2));
}

.impression-overlay {
  position: fixed;
  z-index: 2000;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.25);
  display: flex;
  align-items: center;
  justify-content: center;
}
.impression-panel {
  background: var(--color-bg, rgba(255,255,255,0.95));
  color: var(--color-text, #222);
  border-radius: 18px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.18);
  padding: 28px 24px 18px;
  min-width: 340px;
  max-width: 90vw;
  width: 400px;
  backdrop-filter: blur(16px);
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: all 0.2s;
}
.impression-panel.mobile {
  width: 100vw;
  max-width: 100vw;
  min-width: 0;
  border-radius: 18px 18px 0 0;
  position: fixed;
  left: 0; right: 0; bottom: 0;
  top: auto;
  padding: 24px 12px 12px;
}
.impression-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 1.1rem;
  margin-bottom: 8px;
}
.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--color-text, #222);
  cursor: pointer;
  opacity: 0.7;
  transition: opacity 0.2s;
}
.close-btn:hover { opacity: 1; }
.impression-textarea {
  width: 100%;
  min-height: 90px;
  border-radius: 10px;
  border: 1px solid var(--color-border, #e0e0e0);
  background: var(--color-bg, rgba(255,255,255,0.95));
  color: var(--color-text, #222);
  font-size: 1rem;
  padding: 12px;
  resize: vertical;
  box-sizing: border-box;
  outline: none;
  transition: border 0.2s;
}
.impression-textarea:focus {
  border-color: #22d36b;
}
.impression-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.word-limit {
  font-size: 0.9rem;
  color: var(--color-text, #888);
  opacity: 0.7;
}
.save-btn {
  background: linear-gradient(135deg, #22d36b, #4ade80);
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 8px 22px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(34,211,107,0.08);
  transition: background 0.2s;
}
.save-btn:hover {
  background: linear-gradient(135deg, #16a34a, #22d36b);
}
@media (max-width: 600px) {
  .impression-panel {
    width: 100vw;
    max-width: 100vw;
    min-width: 0;
    border-radius: 18px 18px 0 0;
    position: fixed;
    left: 0; right: 0; bottom: 0;
    top: auto;
    padding: 24px 12px 12px;
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .robots-grid {
    grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  }
  
  .world-title h1 {
    font-size: 2.5rem;
  }
  
  .world-stats {
    gap: 40px;
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 80px 16px 40px;
  }
  
  .filter-controls {
    padding: 20px;
    margin-bottom: 30px;
  }
  
  .filter-group {
    flex-direction: column;
    gap: 12px;
  }
  
  .search-input-wrapper {
    width: 100%;
  }
  
  .status-filter-wrapper {
    width: 100%;
    min-width: auto;
  }
  
  .filter-stats {
    justify-content: center;
  }
  
  .filter-count {
    font-size: 0.85rem;
  }
  
  .world-info-card {
    padding: 40px 24px;
  }
  
  .world-title h1 {
    font-size: 2rem;
  }
  
  .world-description {
    font-size: 1rem;
    margin-bottom: 30px;
  }
  
  .world-stats {
    gap: 30px;
  }
  
  .stat-item {
    min-width: 80px;
  }
  
  .stat-number {
    font-size: 2rem;
  }
  
  .stat-label {
    font-size: 0.9rem;
  }
  
  .section-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 16px;
  }
  
  .header-left {
    text-align: center;
  }
  
  .section-header h2 {
    font-size: 2rem;
  }
  
  .section-header p {
    font-size: 1rem;
  }
  
  .header-right {
    flex-direction: column;
    gap: 8px;
    width: 100%;
  }
  
  .header-action-btn {
    width: 100%;
    max-width: 200px;
    justify-content: center;
  }
  
  .robots-grid {
    grid-template-columns: 1fr;
  }
  
  .robot-card {
    padding: 24px;
  }
  
  .robot-content {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
  
  .robot-avatar-section {
    flex-direction: row;
    justify-content: center;
    gap: 20px;
  }
  
  .robot-quick-info {
    text-align: left;
  }
  
  .robot-quick-info h3 {
    font-size: 1.2rem;
  }
  
  .robot-intro {
    font-size: 0.9rem;
    text-align: center;
  }
  
  .robot-tags {
    justify-content: center;
  }
  
  .robot-controls {
    gap: 16px;
  }
  
  .robot-action-buttons {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }
  
  .action-btn {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 80px 12px 40px;
  }
  
  .filter-controls {
    padding: 16px;
    margin-bottom: 25px;
  }
  
  .filter-group {
    gap: 10px;
  }
  
  .search-input :deep(.el-input__inner) {
    font-size: 0.9rem;
  }
  
  .status-filter :deep(.el-input__inner) {
    font-size: 0.9rem;
  }
  
  .filter-count {
    font-size: 0.8rem;
  }
  
  .world-info-card {
    padding: 30px 20px;
  }
  
  .world-title h1 {
    font-size: 1.8rem;
  }
  
  .world-badge {
    font-size: 0.8rem;
    padding: 6px 12px;
  }
  
  .world-description {
    font-size: 0.9rem;
    margin-bottom: 25px;
  }
  
  .world-stats {
    gap: 20px;
  }
  
  .stat-item {
    min-width: 70px;
  }
  
  .stat-number {
    font-size: 1.8rem;
  }
  
  .stat-label {
    font-size: 0.8rem;
  }
  
  .section-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 12px;
  }
  
  .header-left {
    text-align: center;
  }
  
  .section-header h2 {
    font-size: 1.8rem;
  }
  
  .section-header p {
    font-size: 0.9rem;
  }
  
  .header-action-btn {
    width: 100%;
    max-width: 180px;
    padding: 10px 16px;
    font-size: 0.85rem;
  }
  
  .robot-card {
    padding: 20px;
  }
  
  .robot-avatar-section {
    gap: 16px;
  }
  
  .robot-quick-info h3 {
    font-size: 1.1rem;
  }
  
  .robot-intro {
    font-size: 0.85rem;
  }
  
  .robot-tags {
    gap: 6px;
  }
  
  .tag-item {
    font-size: 0.75rem;
    padding: 3px 10px;
  }
  
  .link-status-tag {
    font-size: 0.75rem;
    padding: 3px 10px;
    gap: 3px;
  }
  
  .robot-controls {
    margin-top: 12px;
    padding-top: 12px;
    gap: 12px;
  }
  
  .robot-action-buttons {
    gap: 6px;
  }
  
  .action-btn {
    padding: 6px 10px;
    font-size: 0.75rem;
  }
}
.theme-menu-overlay {
  position: fixed;
  left: 0; top: 0; right: 0; bottom: 0;
  z-index: 3000;
  background: rgba(0,0,0,0.18);
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
}
.theme-menu {
  position: absolute;
  min-width: 200px;
  background: var(--color-card);
  color: var(--color-text);
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.18);
  padding: 22px 0 10px 0;
  margin-top: 8px;
  font-size: 1.08rem;
  border: 1px solid var(--color-border);
  transition: background 0.2s, color 0.2s;
}
.theme-menu.mobile {
  position: fixed;
  left: 0; right: 0; bottom: 0; top: auto;
  width: 100vw;
  min-width: 0;
  border-radius: 18px 18px 0 0;
  margin: 0;
  padding: 28px 0 16px 0;
  box-shadow: 0 -4px 24px rgba(0,0,0,0.18);
  font-size: 1.18rem;
  animation: slideUp 0.25s cubic-bezier(.4,0,.2,1);
}
@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
.theme-menu-title {
  font-weight: 700;
  font-size: 1.18rem;
  padding: 0 24px 14px 24px;
  color: var(--color-primary);
}
.theme-menu-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}
.theme-menu-item {
  padding: 16px 24px;
  cursor: pointer;
  border-radius: 10px;
  transition: background 0.18s, color 0.18s;
  font-size: 1.08em;
  font-weight: 500;
  user-select: none;
  background: transparent;
}
.theme-menu-item:hover, .theme-menu-item:active {
  background: var(--color-primary, #22d36b);
  color: #fff;
}
.theme-menu-cancel {
  text-align: center;
  color: var(--color-text-secondary, #888);
  font-size: 1.05rem;
  padding: 12px 0 0 0;
  cursor: pointer;
  font-weight: 500;
  background: transparent;
}
.theme-menu-cancel:hover {
  color: var(--color-primary);
}
.theme-menu-fade-enter-active, .theme-menu-fade-leave-active {
  transition: opacity 0.18s;
}
.theme-menu-fade-enter-from, .theme-menu-fade-leave-to {
  opacity: 0;
}
@media (max-width: 600px) {
  .theme-menu {
    min-width: 0;
    width: 100vw;
    left: 0 !important;
    right: 0 !important;
    border-radius: 18px 18px 0 0;
    font-size: 1.18rem;
    padding: 28px 0 16px 0;
  }
}

/* 机器人详情弹窗样式 */
.robot-detail-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  backdrop-filter: blur(4px);
}

.robot-detail-modal {
  background: var(--color-bg);
  border-radius: 20px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.robot-detail-modal.mobile {
  width: 95%;
  max-width: 95%;
  max-height: 85vh;
}

.robot-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 30px 30px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.robot-detail-title {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.robot-avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.robot-status-badge {
  position: absolute;
  bottom: -4px;
  right: -4px;
  background: #ff4d4f;
  color: #fff;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.7rem;
  border: 2px solid var(--color-bg);
  display: flex;
  align-items: center;
  gap: 2px;
  font-weight: 600;
}

.robot-status-badge.active {
  background: #22d36b;
}

.robot-title-info h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 4px 0;
}

.robot-nickname {
  font-size: 0.9rem;
  color: var(--color-text);
  opacity: 0.7;
  margin: 0;
}

.robot-detail-content {
  padding: 20px 30px;
}

.robot-info-section,
.robot-description-section,
.robot-themes-section {
  margin-bottom: 24px;
}

.robot-info-section h4,
.robot-description-section h4,
.robot-themes-section h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 12px 0;
}

.robot-info-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.info-item.full-width {
  flex: 1;
}

.info-label {
  font-size: 0.9rem;
  color: var(--color-text);
  opacity: 0.7;
  min-width: 80px;
}

.info-value {
  font-size: 0.9rem;
  color: var(--color-text);
  font-weight: 500;
}

.robot-description-scroll {
  max-height: 200px;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 16px;
}

.robot-description-scroll::-webkit-scrollbar {
  width: 6px;
}

.robot-description-scroll::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.robot-description-scroll::-webkit-scrollbar-thumb {
  background: rgba(34, 211, 107, 0.3);
  border-radius: 3px;
}

.robot-description-scroll::-webkit-scrollbar-thumb:hover {
  background: rgba(34, 211, 107, 0.5);
}

.robot-description-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.description-item {
  margin: 0;
}

.description-item h5 {
  font-size: 0.85rem;
  color: #22d36b;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.description-item p {
  font-size: 0.9rem;
  color: var(--color-text);
  line-height: 1.6;
  margin: 0;
  white-space: pre-wrap;
}

.theme-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.theme-tag {
  background: rgba(34, 211, 107, 0.1);
  color: #22d36b;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 0.8rem;
  font-weight: 500;
}

.robot-detail-footer {
  padding: 20px 30px 30px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: center;
}

.chat-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 32px;
  background: linear-gradient(135deg, #22d36b, #4ade80);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(34, 211, 107, 0.3);
}

.chat-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(34, 211, 107, 0.4);
}

.chat-btn:active {
  transform: translateY(0);
}

@media (max-width: 768px) {
  .robot-detail-modal {
    width: 95%;
    max-height: 90vh;
  }
  
  .robot-detail-header {
    padding: 20px 20px 16px;
  }
  
  .robot-detail-content {
    padding: 16px 20px;
  }
  
  .robot-detail-footer {
    padding: 16px 20px 20px;
  }
  
  .robot-detail-title {
    justify-content: center;
  }
  
  .robot-avatar-wrapper {
    align-self: center;
  }
  
  .info-row {
    gap: 12px;
  }
  
  .info-item {
    gap: 4px;
  }
  
  .info-label {
    min-width: auto;
  }
}
</style> 