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
.robot-daily-plan-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 0;
}
.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 24px;
}
.search-bar {
  display: flex;
  align-items: center;
  margin-top: 12px;
}
.plan-list {
  margin-top: 24px;
}
.plan-card {
  margin-bottom: 20px;
}
.plan-header {
  display: flex;
  justify-content: space-between;
  font-weight: bold;
  margin-bottom: 8px;
}
.plan-diary {
  margin-bottom: 8px;
  color: #666;
}
.plan-slots {
  padding-left: 8px;
}
.slot-item {
  margin-bottom: 4px;
  font-size: 15px;
}
.slot-time {
  color: #409eff;
  margin-right: 8px;
}
.event-item {
  margin-right: 12px;
}
</style> 