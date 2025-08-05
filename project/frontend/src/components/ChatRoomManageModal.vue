<template>
  <el-dialog 
    v-model="visible" 
    title="聊天室管理" 
    width="600px"
    :before-close="handleClose"
    class="chatroom-manage-modal"
  >
    <div class="manage-content">
      <!-- 聊天室基本信息 -->
      <div class="section">
        <h3 class="section-title">基本信息</h3>
        <div class="room-info">
          <el-form :model="roomForm" label-width="80px">
            <el-form-item label="房间名称">
              <el-input 
                v-model="roomForm.roomName" 
                placeholder="输入聊天室名称"
                :maxlength="50"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </div>
      </div>

      <!-- 成员管理 -->
      <div class="section">
        <h3 class="section-title">
          成员管理 
          <span class="member-count">({{ members.length }}人)</span>
        </h3>
        
        <!-- 添加机器人 -->
        <div class="add-member-section">
          <el-button 
            @click="showAddRobotModal = true" 
            type="primary" 
            :icon="Plus"
            size="small"
          >
            添加机器人
          </el-button>
        </div>

        <!-- 成员列表 -->
        <div class="members-list">
          <div 
            v-for="member in members" 
            :key="member.id"
            class="member-card"
            :class="{ 'owner': member.role === 'owner' }"
          >
            <div class="member-info">
              <el-avatar 
                :src="getMemberAvatar(member)" 
                :size="40"
                class="member-avatar"
              />
              <div class="member-details">
                <div class="member-name">
                  {{ getMemberName(member) }}
                  <el-tag v-if="member.role === 'owner'" type="warning" size="small">房主</el-tag>
                  <el-tag 
                    v-if="member.memberType === 'ROBOT'" 
                    type="info" 
                    size="small"
                  >
                    机器人
                  </el-tag>
                </div>
                <div class="member-status">
                  <el-icon 
                    v-if="member.isOnline" 
                    class="online-indicator"
                  >
                    <CircleCheck />
                  </el-icon>
                  <span class="status-text">
                    {{ member.isOnline ? '在线' : '离线' }}
                  </span>

                </div>
              </div>
            </div>
            
            <!-- 成员操作 -->
            <div class="member-actions" v-if="member.role !== 'owner'">
              <el-dropdown @command="handleMemberAction">
                <el-button text>
                  <el-icon><More /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                                    
                    <el-dropdown-item 
                      :command="{ action: 'remove', member }"
                      class="danger-item"
                    >
                      <el-icon><Delete /></el-icon>
                      移除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>

      <!-- 聊天室统计 -->
      <div class="section">
        <h3 class="section-title">统计信息</h3>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-value">{{ stats.totalMessages || 0 }}</div>
            <div class="stat-label">总消息数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ stats.activeMembers || 0 }}</div>
            <div class="stat-label">活跃成员</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ stats.robotMessages || 0 }}</div>
            <div class="stat-label">机器人消息</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ formatDuration(stats.activeDuration) }}</div>
            <div class="stat-label">活跃时长</div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          @click="saveChanges"
          :loading="saving"
        >
          保存
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 添加机器人弹窗 -->
  <AddRobotModal 
    v-model="showAddRobotModal"
    :room-id="chatRoom?.roomId"
    :existing-members="members"
    @robot-added="handleRobotAdded"
  />
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, CircleCheck, More, Close, Microphone, Delete 
} from '@element-plus/icons-vue'
import AddRobotModal from './AddRobotModal.vue'
import { useChatRoomStore } from '@/stores/chatroom'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  chatRoom: {
    type: Object,
    default: null
  },
  members: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits([
  'update:modelValue',
  'room-updated', 
  'member-added', 
  'member-removed'
])

const chatroomStore = useChatRoomStore()

// 获取成员头像
const getMemberAvatar = (member) => {  
  return member.avatar
}

// 获取成员名称
const getMemberName = (member) => {
  if (member.memberType === 'USER' && member.user) {
    return member.user.nickname || member.memberNickname
  } else if (member.memberType === 'ROBOT' && member.robot) {
    return member.robot.name || member.memberNickname
  }
  return member.memberNickname
}

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const roomForm = reactive({
  roomName: '',
  status: 'active'
})

const showAddRobotModal = ref(false)
const saving = ref(false)
const stats = ref({
  totalMessages: 0,
  activeMembers: 0,
  robotMessages: 0,
  activeDuration: 0
})

// 监听chatRoom变化，更新表单
watch(() => props.chatRoom, (newRoom) => {
  if (newRoom) {
    roomForm.roomName = newRoom.roomName || ''
    roomForm.status = newRoom.status || 'active'
    loadStats()
  }
}, { immediate: true })

// 加载统计信息
const loadStats = async () => {
  if (!props.chatRoom?.roomId) return
  
  try {
    const statsData = await chatroomStore.getChatRoomStats(props.chatRoom.roomId)
    stats.value = statsData
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 保存更改
const saveChanges = async () => {
  try {
    saving.value = true
    
    // 更新房间名称
    if (roomForm.roomName !== props.chatRoom.roomName) {
      await chatroomStore.updateChatRoomName(
        props.chatRoom.roomId, 
        roomForm.roomName
      )
    }
    
    // 更新房间状态
    if (roomForm.status !== props.chatRoom.status) {
      await chatroomStore.switchRoomStatus(
        props.chatRoom.roomId, 
        roomForm.status
      )
    }
    
    // 触发更新事件
    emit('room-updated', {
      ...props.chatRoom,
      roomName: roomForm.roomName,
      status: roomForm.status
    })
    
    ElMessage.success('保存成功')
    visible.value = false
    
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 处理成员操作
const handleMemberAction = async ({ action, member }) => {
  try {
    switch (action) {
      case 'remove':
        await handleRemoveMember(member)
        break
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 移除成员
const handleRemoveMember = async (member) => {
  try {
    await ElMessageBox.confirm(
      `确定要移除 ${getMemberName(member)} 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await chatroomStore.removeMember(props.chatRoom.roomId, member.memberId)
    emit('member-removed', member.memberId)
    ElMessage.success(`${member.memberNickname} 已被移除`)
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除成员失败:', error)
      ElMessage.error('移除成员失败')
    }
  }
}

// 处理机器人添加
const handleRobotAdded = (robot) => {
  emit('member-added', robot)
  showAddRobotModal.value = false
}

// 关闭弹窗
const handleClose = () => {
  visible.value = false
}

// 格式化时长
const formatDuration = (minutes) => {
  if (!minutes) return '0分钟'
  
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  
  if (hours > 0) {
    return `${hours}小时${mins}分钟`
  } else {
    return `${mins}分钟`
  }
}
</script>

<style scoped>
.chatroom-manage-modal :deep(.el-dialog) {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
}

.chatroom-manage-modal :deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: white;
}

.chatroom-manage-modal :deep(.el-dialog__title) {
  color: white;
  font-weight: 600;
}

.manage-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.section-title {
  margin: 0;
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.member-count {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.9rem;
  font-weight: normal;
}

.room-info :deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.8);
}

.room-info :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.room-info :deep(.el-input__inner) {
  color: white;
}

.room-info :deep(.el-radio__label) {
  color: rgba(255, 255, 255, 0.8);
}

.add-member-section {
  display: flex;
  justify-content: flex-start;
}

.members-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  max-height: 300px;
  overflow-y: auto;
}

.member-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.2s ease;
}

.member-card:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.2);
}

.member-card.owner {
  border-color: rgba(251, 191, 36, 0.3);
  background: rgba(251, 191, 36, 0.05);
}

.member-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.member-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.member-name {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: white;
  font-weight: 500;
}

.member-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.6);
}

.online-indicator {
  color: #10b981;
  font-size: 0.6rem;
}

.muted-indicator {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #f59e0b;
}

.member-actions {
  display: flex;
  align-items: center;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 1rem;
}

.stat-card {
  padding: 1rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  text-align: center;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 600;
  color: white;
  margin-bottom: 0.25rem;
}

.stat-label {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.6);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

:deep(.danger-item) {
  color: #ef4444;
}

:deep(.danger-item:hover) {
  background: rgba(239, 68, 68, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chatroom-manage-modal :deep(.el-dialog) {
    width: 90vw;
    margin: 5vh auto;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .member-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
  
  .member-actions {
    align-self: flex-end;
  }
}
</style>