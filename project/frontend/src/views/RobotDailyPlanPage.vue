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
        <el-button type="primary" @click="fetchPlans">查询</el-button>
      </div>
    </div>
    <div class="plan-list">
      <el-empty v-if="plans.length === 0" description="暂无计划" />
      <div v-else>
        <el-card v-for="plan in plans" :key="plan.id" class="plan-card">
          <div class="plan-header">
            <span class="robot-name">{{ getRobotName(plan.robotId) }}</span>
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
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api/robot' // 假设有robot相关API
import dayjs from 'dayjs'

const robotList = ref([])
const selectedRobotId = ref()
const dateRange = ref([])
const plans = ref([])
const currentTimeStr = computed(() => dayjs().format('HH:mm'))

/**
 * 获取机器人名称
 * @param {string} robotId 机器人ID
 * @returns {string} 机器人名称
 */
function getRobotName(robotId) {
  const robot = robotList.value.find(r => r.id === robotId)
  return robot ? robot.name : robotId
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
 * 获取计划列表。如果未选择日期范围，默认最近三天（含今天）。
 */
async function fetchPlans() {
  try {
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
      page: 0,
      size: 20
    }
    const res = await api.getDailyPlanList(params)
    plans.value = res.data || []
  } catch (e) {
    ElMessage.error('获取计划失败')
  }
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

/* 响应式优化 */
@media (max-width: 900px) {
  .robot-daily-plan-page {
    padding: 24px 8px 40px;
  }
  .plan-card {
    padding: 18px 10px 14px 14px;
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
}
</style> 