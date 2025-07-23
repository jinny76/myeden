<template>
  <div class="robot-daily-plan-page">
    <div class="page-header">
      <h1>天使的每一天</h1>
      <div class="search-bar">
        <el-select v-model="selectedRobotId" placeholder="选择天使" clearable style="width: 200px; margin-right: 16px;">
          <el-option v-for="robot in robotList" :key="robot.id" :label="robot.name" :value="robot.id" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 320px; margin-right: 16px;"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
      </div>
    </div>
    <div class="plan-list">
      <!-- 初始加载状态 -->
      <div v-if="initialLoading" class="loading-container">
        <el-icon class="is-loading loading-icon">
          <Loading />
        </el-icon>
        <span class="loading-text">正在加载计划...</span>
      </div>
      
      <!-- 空状态 -->
      <el-empty v-else-if="plans.length === 0 && !initialLoading" description="暂无计划" />
      
      <!-- 计划列表 -->
      <div v-else>
        <el-card v-for="plan in plans" :key="plan.id" class="plan-card">
          <div class="plan-header">
            <span class="robot-name">{{ getRobotName(plan) }}</span>
            <span class="plan-date">{{ plan.planDate }}</span>
          </div>
          <div class="plan-diary">{{ plan.diary }}</div>
          <div class="plan-slots">
            <template v-for="slot in plan.slots" :key="slot && slot.start ? slot.start + '-' + slot.end : Math.random()">
              <div
                v-if="slot && slot.start && shouldShowSlot(plan.planDate, slot.start)"
                class="slot-item"
              >
                <span class="slot-time">
                  {{ slot.start }} -
                  <template v-if="isLastStartedSlot(plan, slot)">
                    {{ currentTimeStr }}
                  </template>
                  <template v-else>
                    {{ slot.end }}
                  </template>
                </span>
                <span class="slot-events">
                  <span v-for="event in (slot.events || [])" :key="event.content" class="event-item">
                    {{ event.content }}<span v-if="event.mood">（{{ event.mood }}）</span>
                  </span>
                </span>
              </div>
            </template>
          </div>
        </el-card>
        
        <!-- 加载更多状态指示器 -->
        <div v-if="plans.length > 0" class="load-more-container">
          <!-- 正在加载更多 -->
          <div v-if="loading" class="loading-more">
            <el-icon class="is-loading loading-icon">
              <Loading />
            </el-icon>
            <span class="loading-text">正在加载更多...</span>
          </div>
          
          <!-- 没有更多数据 -->
          <div v-else-if="!hasMore" class="no-more">
            <span class="no-more-text">没有更多的活动了</span>
          </div>
          
          <!-- 手动加载更多按钮（备用） -->
          <div v-else class="manual-load">
            <el-button type="text" @click="loadMorePlans" class="load-more-btn">
              滚动到底部自动加载更多
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import api from '@/api/robot' // 假设有robot相关API
import dayjs from 'dayjs'

const robotList = ref([])
const selectedRobotId = ref()
const dateRange = ref([])
const plans = ref([])
const currentTimeStr = computed(() => dayjs().format('HH:mm'))

// 分页状态管理
const currentPage = ref(0)
const pageSize = ref(20)
const hasMore = ref(true)
const loading = ref(false)
const initialLoading = ref(false)

/**
 * 获取机器人名称
 * @param {string} robotId 机器人ID
 * @returns {string} 机器人名称
 */
function getRobotName(plan) {
  return plan.robotName || robotList.value.find(r => r.id === plan.robotId)?.name || plan.robotId
}

/**
 * 判断某个slot是否应显示（当天只要已开始就显示）
 * @param {string} planDate 计划日期（YYYY-MM-DD）
 * @param {string} slotStart slot开始时间（HH:mm）
 * @returns {boolean} 是否显示
 */
function shouldShowSlot(planDate, slotStart) {
  if (!slotStart) return false
  const todayStr = dayjs().format('YYYY-MM-DD')
  if (planDate !== todayStr) return true
  const slotStartTime = dayjs(`${planDate} ${slotStart}`)
  return slotStartTime.isBefore(dayjs())
}

/**
 * 获取机器人列表
 */
async function fetchRobots() {
  const res = await api.getRobotList()
  robotList.value = res.data || []
}

/**
 * 滚动监听处理函数
 */
function handleScroll() {
  // 防抖：如果正在加载或没有更多数据，直接返回
  if (loading.value || !hasMore.value) return
  
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight
  
  // 当滚动到距离底部100px时触发加载
  if (scrollTop + windowHeight >= documentHeight - 100) {
    loadMorePlans()
  }
}

/**
 * 处理搜索按钮点击事件
 */
async function handleSearch() {
  // 重置分页状态
  currentPage.value = 0
  hasMore.value = true
  plans.value = []
  
  // 执行搜索
  await fetchPlans(false)
}

/**
 * 获取计划列表。如果未选择日期范围，默认最近三天（含今天）。
 * @param {boolean} isLoadMore 是否为加载更多（追加数据）
 */
async function fetchPlans(isLoadMore = false) {
  try {
    // 设置加载状态
    if (isLoadMore) {
      loading.value = true
    } else {
      initialLoading.value = true
    }
    
    // 如果未选择日期范围，默认最近三天（含今天）
    if (!dateRange.value || dateRange.value.length !== 2) {
      const today = dayjs()
      const twoDaysAgo = today.subtract(2, 'day')
      dateRange.value = [twoDaysAgo.toDate(), today.toDate()]
    }
    
    const params = {
      robotId: selectedRobotId.value,
      startDate: dayjs(dateRange.value[0]).format('YYYY-MM-DD'),
      endDate: dayjs(dateRange.value[1]).format('YYYY-MM-DD'),
      page: currentPage.value,
      size: pageSize.value
    }
    
    console.log('fetchPlans params:', params) // 添加调试日志
    
    const res = await api.getDailyPlanList(params)
    const newPlans = res.data || []
    
    console.log('fetchPlans result:', newPlans.length, 'plans') // 添加调试日志
    
    if (isLoadMore) {
      // 追加数据
      plans.value = [...plans.value, ...newPlans]
    } else {
      // 替换数据
      plans.value = newPlans
    }
    
    // 判断是否还有更多数据
    hasMore.value = newPlans.length === pageSize.value
    
    // 如果成功加载，页码+1（为下次加载更多做准备）
    if (newPlans.length > 0) {
      currentPage.value++
    }
    
  } catch (e) {
    console.error('fetchPlans error:', e) // 添加错误日志
    ElMessage.error('获取计划失败')
    hasMore.value = false
  } finally {
    loading.value = false
    initialLoading.value = false
  }
}

/**
 * 加载更多计划数据
 */
async function loadMorePlans() {
  if (loading.value || !hasMore.value) return
  await fetchPlans(true)
}

/**
 * 获取当天最后一个已开始slot的start时间
 * @param {Array} slots slot数组
 * @param {string} planDate 计划日期
 * @returns {string|null} 最后一个已开始slot的start时间
 */
function getLastStartedSlotStart(slots, planDate) {
  if (!Array.isArray(slots)) return null
  const now = dayjs()
  const todayStr = now.format('YYYY-MM-DD')
  if (planDate !== todayStr) return null
  // 过滤出已开始的slot
  const startedSlots = slots.filter(slot => {
    if (!slot || !slot.start) return false
    return dayjs(`${planDate} ${slot.start}`).isBefore(now) || dayjs(`${planDate} ${slot.start}`).isSame(now)
  })
  if (startedSlots.length === 0) return null
  // 找到start最大的slot
  const lastSlot = startedSlots.reduce((a, b) => {
    return dayjs(`${planDate} ${a.start}`).isAfter(dayjs(`${planDate} ${b.start}`)) ? a : b
  })
  return lastSlot.start
}

/**
 * 判断当前slot是否为当天最后一个已开始slot
 * @param {object} plan 当前计划
 * @param {object} slot 当前slot
 * @returns {boolean}
 */
function isLastStartedSlot(plan, slot) {
  const lastStart = getLastStartedSlotStart(plan.slots, plan.planDate)
  return slot && slot.start === lastStart
}

onMounted(() => {
  fetchRobots()
  fetchPlans()
  // 添加滚动监听
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  // 移除滚动监听
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
/* 页面整体容器 */
.robot-daily-plan-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 0 60px;
  position: relative;
  z-index: 1;
}

/* 页头区域 */
.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 32px;
  margin-top: 80px;
}
.page-header h1 {
  font-size: 2.2rem;
  font-weight: 800;
  background: linear-gradient(135deg, #22d36b, #4ade80, #86efac);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 10px;
  line-height: 1.1;
}

/* 搜索栏区域，响应式折行 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 12px;
  flex-wrap: wrap;
}
.search-bar > * {
  min-width: 0;
}
@media (max-width: 600px) {
  .search-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  .search-bar > * {
    width: 100% !important;
    margin-right: 0 !important;
  }
}

/* 计划列表区域 */
.plan-list {
  margin-top: 24px;
}

/* 卡片样式，参考Home.vue浮层卡片风格 */
.plan-card {
  background: rgba(255,255,255,0.7);
  backdrop-filter: blur(16px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(34,211,107,0.08);
  border: 1px solid rgba(34,211,107,0.08);
  margin-bottom: 28px;
  padding: 28px 28px 20px 28px;
  transition: box-shadow 0.3s, border-color 0.3s;
}
.plan-card:hover {
  box-shadow: 0 16px 48px rgba(34,211,107,0.13);
  border-color: rgba(34,211,107,0.18);
}

/* 暗色模式适配 */
@media (prefers-color-scheme: dark) {
  .plan-card {
    background: rgba(30,32,34,0.85);
    border: 1px solid rgba(34,211,107,0.13);
    color: #e6f4ea;
  }
}

/* 卡片头部：机器人名+日期 */
.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  margin-bottom: 10px;
  font-size: 1.1rem;
}
.robot-name {
  color: #22d36b;
  font-weight: 700;
  font-size: 1.1em;
}
.plan-date {
  color: #888;
  font-size: 0.98em;
}

/* 日记内容 */
.plan-diary {
  margin-bottom: 12px;
  color: #666;
  font-size: 1.02em;
  line-height: 1.7;
  padding-left: 2px;
}
@media (prefers-color-scheme: dark) {
  .plan-diary {
    color: #b2e5c7;
  }
}

/* 时间段与事件分区 */
.plan-slots {
  padding-left: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.slot-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  font-size: 15px;
  margin-bottom: 2px;
}
/* 柔和绿色主色，浅色模式下更舒适 */
.slot-time {
  color: var(--color-primary);
  font-weight: 600;
  min-width: 100px;
}
.event-item {
  color: var(--color-primary);
  padding: 2px 0px 2px 0px;
  font-size: 0.98em;
  margin-right: 0;
  margin-bottom: 2px;
}
@media (prefers-color-scheme: dark) {
  .slot-time {
    color: #86efac;
  }
  .event-item {
    color: #b2e5c7;
  }
}

/* 空状态样式优化 */
.el-empty {
  margin: 60px 0 40px 0;
}

/* 加载状态指示器样式 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--color-text);
}

.load-more-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 32px 20px;
  margin-top: 20px;
}

.loading-more,
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.loading-icon {
  font-size: 24px;
  color: var(--color-primary);
}

.loading-text {
  font-size: 14px;
  color: var(--color-text);
  opacity: 0.7;
}

.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-more-text {
  font-size: 14px;
  color: var(--color-text);
  opacity: 0.5;
  padding: 8px 16px;
  border-radius: 20px;
  background: rgba(34, 211, 107, 0.05);
  border: 1px solid rgba(34, 211, 107, 0.1);
}

.manual-load {
  display: flex;
  align-items: center;
  justify-content: center;
}

.load-more-btn {
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.6;
  transition: all 0.3s ease;
}

.load-more-btn:hover {
  color: var(--color-primary);
  opacity: 1;
}

/* 响应式优化 */
@media (max-width: 900px) {
  .robot-daily-plan-page {
    padding: 24px 8px 40px;
  }
  .plan-card {
    padding: 18px 10px 14px 14px;
  }
  .loading-container {
    padding: 40px 20px;
  }
  .load-more-container {
    padding: 24px 20px;
  }
}
@media (max-width: 600px) {
  .robot-daily-plan-page {
    padding: 12px 2px 24px;
  }
  .plan-card {
    padding: 10px 4px 10px 8px;
    border-radius: 14px;
  }
  .plan-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }
  .loading-container {
    padding: 32px 16px;
  }
  .load-more-container {
    padding: 20px 16px;
  }
  .loading-icon {
    font-size: 20px;
  }
  .loading-text {
    font-size: 13px;
  }
  .no-more-text {
    font-size: 13px;
    padding: 6px 12px;
  }
  .load-more-btn {
    font-size: 12px;
  }
}
</style> 