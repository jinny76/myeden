<template>
  <div class="contribution-graph">
    <div class="graph-header">
      <h3>活动记录</h3>
      <div class="activity-summary">
        <span class="total-count">{{ totalActivities }} 次活动</span>
        <span class="date-range">{{ startDate }} - {{ endDate }}</span>
      </div>
    </div>

    <div class="graph-container">
      <div class="graph-wrapper">
        <!-- 贡献图网格 -->
        <div class="graph-grid">
          <div
            v-for="week in weeks"
            :key="week.key"
            class="week-column"
          >
            <div
              v-for="day in week.days"
              :key="day.date"
              :class="['day-cell', `level-${day.level}`]"
              :data-date="day.date"
              :data-count="day.count"
              :data-activities="JSON.stringify(day.activities)"
              @mouseenter="showTooltip"
              @mouseleave="hideTooltip"
            ></div>
          </div>
        </div>

        <!-- Tooltip -->
        <div
          v-if="tooltip.visible"
          ref="tooltip"
          class="graph-tooltip"
          :style="{
            left: tooltip.x + 'px',
            top: tooltip.y + 'px'
          }"
        >
          <div class="tooltip-date">{{ tooltip.date }}</div>
          <div class="tooltip-count">{{ tooltip.count }} 次活动</div>
          <div v-if="tooltip.activities && tooltip.activities.length > 0" class="tooltip-activities">
            <div
              v-for="activity in tooltip.activities"
              :key="activity.type"
              class="activity-item"
            >
              <span class="activity-icon">{{ getActivityIcon(activity.type) }}</span>
              <span class="activity-text">{{ getActivityText(activity.type) }}</span>
              <span class="activity-count">{{ activity.count }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 图例 -->
      <div class="graph-legend">
        <span class="legend-text">少</span>
        <div class="legend-levels">
          <div class="legend-level level-0"></div>
          <div class="legend-level level-1"></div>
          <div class="legend-level level-2"></div>
          <div class="legend-level level-3"></div>
          <div class="legend-level level-4"></div>
        </div>
        <span class="legend-text">多</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { getUserContributionData } from '@/api/activity'

const userStore = useUserStore()

// 组件属性
const props = defineProps({
  userId: {
    type: String,
    default: ''
  }
})

// 响应式数据
const activityData = ref([])
const loading = ref(true)
const tooltip = reactive({
  visible: false,
  x: 0,
  y: 0,
  date: '',
  count: 0,
  activities: []
})

// 计算属性
const totalActivities = computed(() => {
  return activityData.value.reduce((sum, day) => sum + day.count, 0)
})

const startDate = computed(() => {
  const date = new Date()
  date.setDate(date.getDate() - 364) // 过去一年
  return formatDate(date)
})

const endDate = computed(() => {
  return formatDate(new Date())
})

// 生成过去一年的日期网格
const weeks = computed(() => {
  const result = []
  const today = new Date()
  const startDate = new Date(today)
  startDate.setDate(today.getDate() - 364) // 过去一年
  
  // 找到起始日期所在周的星期一
  const startWeek = new Date(startDate)
  const dayOfWeek = startWeek.getDay()
  const daysToMonday = dayOfWeek === 0 ? 6 : dayOfWeek - 1
  startWeek.setDate(startWeek.getDate() - daysToMonday)
  
  let currentWeek = new Date(startWeek)
  let weekIndex = 0
  
  while (currentWeek <= today) {
    const week = {
      key: `week-${weekIndex}`,
      days: []
    }
    
    // 生成一周的7天
    for (let day = 0; day < 7; day++) {
      const currentDay = new Date(currentWeek)
      currentDay.setDate(currentWeek.getDate() + day)
      
      // 如果日期超过今天，跳出循环
      if (currentDay > today) {
        break
      }
      
      const dateStr = formatDateKey(currentDay)
      const dayData = activityData.value.find(d => d.date === dateStr)
      
      week.days.push({
        date: dateStr,
        count: dayData ? dayData.count : 0,
        level: dayData ? getDayLevel(dayData.count) : 0,
        activities: dayData ? dayData.activities : []
      })
    }
    
    result.push(week)
    currentWeek.setDate(currentWeek.getDate() + 7)
    weekIndex++
    
    // 如果当前周已经超过今天，停止生成
    if (currentWeek > today) {
      break
    }
  }
  
  return result
})


// 工具方法
const formatDate = (date) => {
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const formatDateKey = (date) => {
  return date.toISOString().split('T')[0]
}

const getDayLevel = (count) => {
  if (count === 0) return 0
  if (count <= 3) return 1
  if (count <= 6) return 2
  if (count <= 10) return 3
  return 4
}

const getActivityIcon = (type) => {
  const icons = {
    'chat': '💬',
    'post': '📝',
    'comment': '💭',
    'reply': '↩️',
    'like': '👍'
  }
  return icons[type] || '✨'
}

const getActivityText = (type) => {
  const texts = {
    'chat': '聊天',
    'post': '发帖',
    'comment': '评论',
    'reply': '回复',
    'like': '点赞'
  }
  return texts[type] || '活动'
}

// 提示框方法
const showTooltip = (event) => {
  const cell = event.target
  const rect = cell.getBoundingClientRect()
  const containerRect = cell.closest('.graph-container').getBoundingClientRect()
  
  tooltip.visible = true
  tooltip.x = rect.left - containerRect.left + rect.width / 2
  tooltip.y = rect.top - containerRect.top - 10
  tooltip.date = formatDate(new Date(cell.dataset.date))
  tooltip.count = parseInt(cell.dataset.count) || 0
  
  // 解析活动数据
  try {
    tooltip.activities = cell.dataset.activities ? JSON.parse(cell.dataset.activities) : []
  } catch (e) {
    tooltip.activities = []
  }
  
  nextTick(() => {
    const tooltipEl = document.querySelector('.graph-tooltip')
    if (tooltipEl) {
      const tooltipRect = tooltipEl.getBoundingClientRect()
      const containerRect = tooltipEl.closest('.graph-container').getBoundingClientRect()
      
      // 防止tooltip超出容器边界
      if (tooltip.x + tooltipRect.width > containerRect.width) {
        tooltip.x = containerRect.width - tooltipRect.width - 10
      }
      if (tooltip.x < 0) {
        tooltip.x = 10
      }
    }
  })
}

const hideTooltip = () => {
  tooltip.visible = false
}

// 加载活动数据
const loadActivityData = async () => {
  try {
    loading.value = true
    
    // 获取用户ID，优先使用props传入的，否则从userStore获取
    const userId = props.userId || userStore.userInfo?.userId
    
    if (!userId) {
      console.warn('用户ID未提供，使用模拟数据')
      const mockData = generateMockData()
      activityData.value = mockData
      return Promise.resolve()
    }
    
    // 调用API获取活动数据
    const response = await getUserContributionData(userId)
    if (response.code === 200) {
      // 转换API数据格式
      activityData.value = response.data.map(item => ({
        date: item.date,
        count: item.totalCount,
        activities: item.activities || []
      }))
    } else {
      console.error('获取活动数据失败:', response.message)
      // 失败时使用模拟数据
      const mockData = generateMockData()
      activityData.value = mockData
    }
    
    return Promise.resolve()
    
  } catch (error) {
    console.error('加载活动数据失败:', error)
    // 出错时使用模拟数据
    const mockData = generateMockData()
    activityData.value = mockData
    return Promise.resolve()
  } finally {
    loading.value = false
  }
}

// 生成模拟数据
const generateMockData = () => {
  const data = []
  const today = new Date()
  
  for (let i = 0; i < 365; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() - i)
    
    // 随机生成活动数据
    const shouldHaveActivity = Math.random() > 0.4
    if (shouldHaveActivity) {
      const activities = []
      const activityTypes = ['chat', 'post', 'comment', 'reply', 'like']
      
      activityTypes.forEach(type => {
        if (Math.random() > 0.6) {
          activities.push({
            type,
            count: Math.floor(Math.random() * 3) + 1
          })
        }
      })
      
      if (activities.length > 0) {
        data.push({
          date: formatDateKey(date),
          count: activities.reduce((sum, a) => sum + a.count, 0),
          activities
        })
      }
    }
  }
  
  return data
}

// 滚动到最右端（最新日期）
const scrollToLatest = () => {
  nextTick(() => {
    setTimeout(() => {
      const graphWrapper = document.querySelector('.graph-wrapper')
      if (graphWrapper) {
        // 在移动端滚动到最右端
        if (window.innerWidth <= 768) {
          graphWrapper.scrollLeft = graphWrapper.scrollWidth - graphWrapper.clientWidth
        }
      }
    }, 100) // 延迟100ms确保DOM完全渲染
  })
}

// 监听窗口大小变化
const handleResize = () => {
  scrollToLatest()
}

// 生命周期
onMounted(() => {
  loadActivityData().then(() => {
    scrollToLatest()
  })
  
  // 添加窗口大小变化监听
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 移除窗口大小变化监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.contribution-graph {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 20px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  
  &:hover {
    transform: translateY(-2px);
    border-color: rgba(34, 211, 107, 0.2);
    box-shadow: 0 8px 30px rgba(34, 211, 107, 0.1);
  }
  
  &::before {
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
  
  &:hover::before {
    opacity: 1;
  }
}

.graph-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h3 {
    margin: 0;
    color: var(--color-text);
    font-size: 1.1rem;
    font-weight: 600;
  }
  
  .activity-summary {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 4px;
    
    .total-count {
      font-size: 0.9rem;
      color: var(--color-text);
      font-weight: 500;
    }
    
    .date-range {
      font-size: 0.8rem;
      color: var(--color-text);
      opacity: 0.7;
    }
  }
}

.graph-container {
  position: relative;
}

.graph-wrapper {
  position: relative;
  overflow-x: auto;
  padding: 20px 0;
  
  /* 隐藏滚动条但保持可滚动 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
  
  &::-webkit-scrollbar {
    display: none; /* Chrome, Safari, Opera */
  }
}

.graph-grid {
  display: flex;
  gap: 3px;
  min-width: max-content;
  justify-content: center;
}

.week-column {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.day-cell {
  width: 11px;
  height: 11px;
  border-radius: 2px;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    outline: 1px solid rgba(255, 255, 255, 0.5);
    outline-offset: 1px;
  }
  
  &.level-0 {
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  &.level-1 {
    background: rgba(34, 211, 107, 0.2);
    border: 1px solid rgba(34, 211, 107, 0.3);
  }
  
  &.level-2 {
    background: rgba(34, 211, 107, 0.4);
    border: 1px solid rgba(34, 211, 107, 0.5);
  }
  
  &.level-3 {
    background: rgba(34, 211, 107, 0.6);
    border: 1px solid rgba(34, 211, 107, 0.7);
  }
  
  &.level-4 {
    background: rgba(34, 211, 107, 0.8);
    border: 1px solid rgba(34, 211, 107, 1);
  }
}

.graph-tooltip {
  position: absolute;
  background: rgba(0, 0, 0, 0.9);
  color: white;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 0.8rem;
  white-space: nowrap;
  z-index: 1000;
  pointer-events: none;
  transform: translateX(-50%) translateY(-100%);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  
  .tooltip-date {
    font-weight: 600;
    margin-bottom: 4px;
  }
  
  .tooltip-count {
    color: #22d36b;
    margin-bottom: 6px;
  }
  
  .tooltip-activities {
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    padding-top: 6px;
    
    .activity-item {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-bottom: 2px;
      
      .activity-icon {
        font-size: 0.7rem;
      }
      
      .activity-text {
        flex: 1;
        font-size: 0.75rem;
      }
      
      .activity-count {
        color: #22d36b;
        font-weight: 500;
        font-size: 0.75rem;
      }
    }
  }
}

.graph-legend {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  margin-top: 16px;
  
  .legend-text {
    font-size: 0.75rem;
    color: var(--color-text);
    opacity: 0.7;
  }
  
  .legend-levels {
    display: flex;
    gap: 2px;
  }
  
  .legend-level {
    width: 11px;
    height: 11px;
    border-radius: 2px;
    
    &.level-0 {
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.1);
    }
    
    &.level-1 {
      background: rgba(34, 211, 107, 0.2);
      border: 1px solid rgba(34, 211, 107, 0.3);
    }
    
    &.level-2 {
      background: rgba(34, 211, 107, 0.4);
      border: 1px solid rgba(34, 211, 107, 0.5);
    }
    
    &.level-3 {
      background: rgba(34, 211, 107, 0.6);
      border: 1px solid rgba(34, 211, 107, 0.7);
    }
    
    &.level-4 {
      background: rgba(34, 211, 107, 0.8);
      border: 1px solid rgba(34, 211, 107, 1);
    }
  }
}

/* 响应式设计 */
@media (min-width: 1200px) {
  .contribution-graph {
    max-width: none;
    margin: 0;
  }
  
  .graph-wrapper {
    overflow-x: visible;
  }
  
  .graph-grid {
    justify-content: center;
    width: 100%;
    max-width: none;
  }
}

@media (max-width: 1024px) {
  .contribution-graph {
    padding: 18px;
  }
  
  .graph-header {
    margin-bottom: 18px;
  }
  
  .graph-header h3 {
    font-size: 1rem;
  }
  
  .activity-summary .total-count {
    font-size: 0.85rem;
  }
  
  .activity-summary .date-range {
    font-size: 0.75rem;
  }
  
  .graph-wrapper {
    padding: 18px 0;
  }
  
  .graph-grid {
    gap: 2px;
  }
  
  .week-column {
    gap: 2px;
  }
  
  .day-cell {
    width: 10px;
    height: 10px;
  }
  
  .graph-legend {
    margin-top: 14px;
  }
  
  .legend-text {
    font-size: 0.7rem;
  }
  
  .legend-level {
    width: 10px;
    height: 10px;
  }
}

@media (max-width: 768px) {
  .contribution-graph {
    padding: 24px;
    border-radius: 16px;
  }
  
  .graph-header {
    margin-bottom: 20px;
    padding: 0;
  }
  
  .graph-header h3 {
    font-size: 0.95rem;
  }
  
  .activity-summary .total-count {
    font-size: 0.8rem;
  }
  
  .activity-summary .date-range {
    font-size: 0.7rem;
  }
  
  .graph-wrapper {
    padding: 16px 0;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
  
  .graph-grid {
    gap: 2px;
    min-width: 750px; /* 确保有足够空间显示完整的图表 */
  }
  
  .week-column {
    gap: 2px;
  }
  
  .day-cell {
    width: 9px;
    height: 9px;
  }
  
  .graph-legend {
    margin-top: 12px;
    padding: 0 16px;
  }
  
  .legend-text {
    font-size: 0.65rem;
  }
  
  .legend-level {
    width: 9px;
    height: 9px;
  }
  
  .graph-tooltip {
    font-size: 0.75rem;
    padding: 6px 10px;
    border-radius: 4px;
  }
  
  .tooltip-activities .activity-item {
    margin-bottom: 1px;
  }
  
  .tooltip-activities .activity-icon {
    font-size: 0.65rem;
  }
  
  .tooltip-activities .activity-text,
  .tooltip-activities .activity-count {
    font-size: 0.7rem;
  }
}

@media (max-width: 480px) {
  .contribution-graph {
    padding: 20px 16px;
    border-radius: 16px;
  }
  
  .graph-header {
    margin-bottom: 16px;
    padding: 0;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }
  
  .graph-header h3 {
    font-size: 0.9rem;
  }
  
  .activity-summary {
    align-items: flex-end;
    gap: 2px;
  }
  
  .activity-summary .total-count {
    font-size: 0.75rem;
  }
  
  .activity-summary .date-range {
    font-size: 0.65rem;
  }
  
  .graph-wrapper {
    padding: 14px 0;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
  
  .graph-grid {
    gap: 2px;
    min-width: 700px; /* 小屏幕适当缩小但保持可读性 */
  }
  
  .week-column {
    gap: 2px;
  }
  
  .day-cell {
    width: 8px;
    height: 8px;
    border-radius: 1px;
  }
  
  .graph-legend {
    margin-top: 10px;
    padding: 0;
    gap: 4px;
  }
  
  .legend-text {
    font-size: 0.6rem;
  }
  
  .legend-level {
    width: 8px;
    height: 8px;
    border-radius: 1px;
  }
  
  .graph-tooltip {
    font-size: 0.7rem;
    padding: 5px 8px;
    border-radius: 3px;
    max-width: 200px;
  }
  
  .tooltip-date {
    font-size: 0.7rem;
    margin-bottom: 3px;
  }
  
  .tooltip-count {
    font-size: 0.7rem;
    margin-bottom: 4px;
  }
  
  .tooltip-activities {
    padding-top: 4px;
  }
  
  .tooltip-activities .activity-item {
    margin-bottom: 1px;
    gap: 4px;
  }
  
  .tooltip-activities .activity-icon {
    font-size: 0.6rem;
  }
  
  .tooltip-activities .activity-text,
  .tooltip-activities .activity-count {
    font-size: 0.65rem;
  }
}

@media (max-width: 360px) {
  .contribution-graph {
    padding: 16px 12px;
    border-radius: 16px;
  }
  
  .graph-header {
    padding: 0;
  }
  
  .graph-grid {
    gap: 2px;
    min-width: 650px; /* 超小屏幕进一步缩小 */
  }
  
  .day-cell {
    width: 7px;
    height: 7px;
  }
  
  .legend-level {
    width: 7px;
    height: 7px;
  }
  
  .graph-legend {
    padding: 0;
  }
}
</style>