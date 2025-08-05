<template>
  <el-dialog 
    v-model="visible" 
    title="添加天使" 
    width="600px"
    :before-close="handleClose"
    class="add-robot-modal"
  >
    <div class="modal-content">
      <!-- 搜索框 -->
      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索天使..."
          :prefix-icon="Search"
          clearable
          @input="handleSearch"
        />
      </div>

      <!-- 天使列表 -->
      <div class="robots-section">
        <div v-if="loading" class="loading-container">
          <el-icon class="is-loading"><Loading /></el-icon>
          加载中...
        </div>
        
        <div v-else-if="filteredRobots.length === 0" class="empty-container">
          <el-empty description="没有找到可添加的天使" />
        </div>
        
        <div v-else class="robots-list">
          <div 
            v-for="robot in filteredRobots" 
            :key="robot.robotId || robot.id"
            class="robot-card"
            :class="{ 'selected': selectedRobots.includes(robot.robotId || robot.id) }"
          >
            <div class="robot-info" @click="toggleRobotSelection(robot)">
              <el-avatar 
                :src="getAvatar(robot.avatar) || getDefaultRobotAvatar()" 
                :size="50"
                class="robot-avatar"
              />
              <div class="robot-details">
                <div class="robot-name">{{ robot.nickname || robot.name }}</div>
                <div class="robot-description">
                  {{ robot.personality || robot.description || '这个天使很神秘...' }}
                </div>
                <div class="robot-stats">
                  <el-tag size="small" type="info">
                    活跃度: {{ robot.activity || 5 }}/10
                  </el-tag>
                  <el-tag 
                    v-if="robot.isActive" 
                    size="small" 
                    type="success"
                  >
                    在线
                  </el-tag>
                  <el-tag 
                    v-else 
                    size="small" 
                    type="info"
                  >
                    离线
                  </el-tag>
                </div>
              </div>
            </div>
            
            <div class="robot-actions" @click.stop>
              <el-checkbox 
                :model-value="selectedRobots.includes(robot.robotId || robot.id)"
                @change="toggleRobotSelection(robot)"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 选中统计 -->
      <div v-if="selectedRobots.length > 0" class="selection-summary">
        已选择 {{ selectedRobots.length }} 个天使
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          @click="addSelectedRobots"
          :disabled="selectedRobots.length === 0"
          :loading="adding"
        >
          添加天使 ({{ selectedRobots.length }})
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Loading } from '@element-plus/icons-vue'
import { useChatRoomStore } from '@/stores/chatroom'
import { useRobotStore } from '@/stores/robot'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  roomId: {
    type: String,
    required: true
  },
  existingMembers: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits([
  'update:modelValue',
  'robots-added'
])

const chatroomStore = useChatRoomStore()
const robotStore = useRobotStore()

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const searchKeyword = ref('')
const availableRobots = ref([])
const selectedRobots = ref([])
const loading = ref(false)
const adding = ref(false)

// 计算属性
const existingRobotIds = computed(() => 
  props.existingMembers
    .filter(member => member.memberType === 'ROBOT')
    .map(member => member.memberId)
)

const filteredRobots = computed(() => {
  let robots = availableRobots.value.filter(
    robot => !existingRobotIds.value.includes(robot.robotId || robot.id)
  )
  
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    robots = robots.filter(robot => 
      (robot.nickname || robot.name || '').toLowerCase().includes(keyword) ||
      (robot.personality || robot.description || '').toLowerCase().includes(keyword)
    )
  }
  
  return robots
})

// 监听弹窗显示状态
watch(visible, (newVisible) => {
  if (newVisible) {
    loadAvailableRobots()
    selectedRobots.value = []
    searchKeyword.value = ''
  }
})

// 加载可用天使
const loadAvailableRobots = async () => {
  try {
    loading.value = true
    
    // 获取所有天使列表
    await robotStore.fetchRobotList()
    availableRobots.value = robotStore.robots
    
  } catch (error) {
    console.error('加载天使列表失败:', error)
    ElMessage.error('加载天使列表失败')
  } finally {
    loading.value = false
  }
}

const getAvatar = (avatar) => {
  return "/api/v1/files" + avatar.replaceAll('/uploads/', '/')
}

// 切换天使选择状态
const toggleRobotSelection = (robot) => {
  const robotId = robot.robotId || robot.id
  const index = selectedRobots.value.indexOf(robotId)
  if (index > -1) {
    selectedRobots.value.splice(index, 1)
  } else {
    selectedRobots.value.push(robotId)
  }
}

// 处理搜索
const handleSearch = () => {
  // 搜索逻辑在计算属性中处理
}

// 添加选中的天使
const addSelectedRobots = async () => {
  try {
    adding.value = true
    
    // 批量添加天使
    for (const robotId of selectedRobots.value) {
      const robot = availableRobots.value.find(r => (r.robotId || r.id) === robotId)
      if (robot) {
        // 只通过API添加天使，不触发额外的事件
        await chatroomStore.addRobotToRoom(props.roomId, {
          robotId: robot.id || robot.robotId,
          robotNickname: robot.nickname || robot.name,
          robotAvatar: robot.avatar
        })
      }
    }
    
    ElMessage.success(`成功添加 ${selectedRobots.value.length} 个天使`)
    // 触发事件通知父组件
    emit('robots-added')
    visible.value = false
    
  } catch (error) {
    console.error('添加天使失败:', error)
    ElMessage.error('添加天使失败')
  } finally {
    adding.value = false
  }
}

// 关闭弹窗
const handleClose = () => {
  visible.value = false
}

// 获取默认天使头像
const getDefaultRobotAvatar = () => {
  return '/default-robot-avatar.png'
}
</script>

<style scoped>
.add-robot-modal :deep(.el-dialog) {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
}

.add-robot-modal :deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: white;
}

.add-robot-modal :deep(.el-dialog__title) {
  color: white;
  font-weight: 600;
}

.modal-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.search-section :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.search-section :deep(.el-input__inner) {
  color: white;
}

.search-section :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.5);
}

.robots-section {
  min-height: 300px;
  max-height: 400px;
  overflow-y: auto;
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: rgba(255, 255, 255, 0.6);
}

.loading-container {
  gap: 0.5rem;
}

.robots-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.robot-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  cursor: pointer;
  transition: all 0.2s ease;
}

.robot-card:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
}

.robot-card.selected {
  background: rgba(59, 130, 246, 0.15);
  border-color: rgba(59, 130, 246, 0.3);
}

.robot-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
}

.robot-details {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  flex: 1;
}

.robot-name {
  color: white;
  font-weight: 600;
  font-size: 1rem;
}

.robot-description {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
  line-height: 1.3;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.robot-stats {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.robot-actions {
  display: flex;
  align-items: center;
}

.selection-summary {
  padding: 0.75rem 1rem;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 0.5rem;
  color: rgba(59, 130, 246, 0.9);
  text-align: center;
  font-weight: 500;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

/* 自定义滚动条 */
.robots-section::-webkit-scrollbar {
  width: 6px;
}

.robots-section::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.robots-section::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
}

.robots-section::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.4);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .add-robot-modal :deep(.el-dialog) {
    width: 90vw;
    margin: 5vh auto;
  }
  
  .robot-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .robot-actions {
    align-self: flex-end;
  }
  
  .robot-description {
    max-width: none;
    white-space: normal;
    overflow: visible;
    text-overflow: initial;
  }
}
</style>