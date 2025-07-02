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
            <div v-for="slot in plan.slots" :key="slot.start + '-' + slot.end" class="slot-item">
              <span class="slot-time">{{ slot.start }} - {{ slot.end }}</span>
              <span class="slot-events">
                <span v-for="event in slot.events" :key="event.content" class="event-item">
                  {{ event.content }}<span v-if="event.mood">（{{ event.mood }}）</span>
                </span>
              </span>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api/robot' // 假设有robot相关API
import dayjs from 'dayjs'

const robotList = ref([])
const selectedRobotId = ref()
const dateRange = ref([])
const plans = ref([])

function getRobotName(robotId) {
  const robot = robotList.value.find(r => r.id === robotId)
  return robot ? robot.name : robotId
}

async function fetchRobots() {
  // 获取机器人列表
  const res = await api.getRobotList()
  robotList.value = res.data || []
}

async function fetchPlans() {
  try {
    const params = {
      robotId: selectedRobotId.value,
      startDate: dateRange.value?.[0] ? dayjs(dateRange.value[0]).format('YYYY-MM-DD') : undefined,
      endDate: dateRange.value?.[1] ? dayjs(dateRange.value[1]).format('YYYY-MM-DD') : undefined,
      page: 0,
      size: 20
    }
    const res = await api.getDailyPlanList(params)
    plans.value = res.data || []
  } catch (e) {
    ElMessage.error('获取计划失败')
  }
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
.slot-time {
  color: #22d36b;
  font-weight: 600;
  min-width: 100px;
}
@media (prefers-color-scheme: dark) {
  .slot-time {
    color: #4ade80;
  }
}
.slot-events {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.event-item {
  background: rgba(34,211,107,0.08);
  color: #22d36b;
  border-radius: 12px;
  padding: 2px 10px;
  font-size: 0.98em;
  margin-right: 0;
  margin-bottom: 2px;
}
@media (prefers-color-scheme: dark) {
  .event-item {
    background: rgba(34,211,107,0.13);
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