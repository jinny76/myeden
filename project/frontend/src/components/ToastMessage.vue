<template>
  <TransitionGroup
    name="toast"
    tag="div"
    class="toast-container"
  >
    <div
      v-for="toast in toasts"
      :key="toast.id"
      :class="['toast-message', `toast-${toast.type}`]"
      @click="removeToast(toast.id)"
    >
      <div class="toast-icon">
        <component :is="getIcon(toast.type)" />
      </div>
      <div class="toast-content">
        <div class="toast-title">{{ toast.title }}</div>
        <div v-if="toast.message" class="toast-message-text">{{ toast.message }}</div>
      </div>
      <div class="toast-close" @click.stop="removeToast(toast.id)">
        <Close />
      </div>
      <div class="toast-progress" :style="{ animationDuration: `${toast.duration}ms` }"></div>
    </div>
  </TransitionGroup>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { 
  CircleCheck, 
  CircleClose, 
  Warning, 
  InfoFilled, 
  Close 
} from '@element-plus/icons-vue'

// 响应式数据
const toasts = ref([])
let toastId = 0

// 图标映射
const getIcon = (type) => {
  const icons = {
    success: CircleCheck,
    error: CircleClose,
    warning: Warning,
    info: InfoFilled
  }
  return icons[type] || InfoFilled
}

// 添加消息
const addToast = (type, title, message = '', duration = 3000) => {
  const id = ++toastId
  const toast = {
    id,
    type,
    title,
    message,
    duration
  }
  
  toasts.value.push(toast)
  
  // 自动移除
  if (duration > 0) {
    setTimeout(() => {
      removeToast(id)
    }, duration)
  }
  
  return id
}

// 移除消息
const removeToast = (id) => {
  const index = toasts.value.findIndex(toast => toast.id === id)
  if (index > -1) {
    toasts.value.splice(index, 1)
  }
}

// 清空所有消息
const clearAll = () => {
  toasts.value = []
}

// 暴露方法给全局使用
const toast = {
  success: (title, message, duration) => addToast('success', title, message, duration),
  error: (title, message, duration) => addToast('error', title, message, duration),
  warning: (title, message, duration) => addToast('warning', title, message, duration),
  info: (title, message, duration) => addToast('info', title, message, duration),
  remove: removeToast,
  clear: clearAll
}

// 挂载到全局
onMounted(() => {
  window.$toast = toast
})

onUnmounted(() => {
  delete window.$toast
})

// 暴露给父组件
defineExpose({
  addToast,
  removeToast,
  clearAll,
  toast
})
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 12px;
  pointer-events: none;
}

.toast-message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  min-width: 280px;
  max-width: 320px;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: auto;
  transform-origin: top right;
}

.toast-message::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.toast-message:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.18);
}

.toast-message:hover::before {
  opacity: 1;
}

.toast-message:active {
  transform: translateY(-1px) scale(0.98);
  transition: all 0.1s ease;
}

.toast-success {
  border-left: 4px solid #67c23a;
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.05) 0%, rgba(255, 255, 255, 0.95) 100%);
}

.toast-error {
  border-left: 4px solid #f56c6c;
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.05) 0%, rgba(255, 255, 255, 0.95) 100%);
}

.toast-warning {
  border-left: 4px solid #e6a23c;
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.05) 0%, rgba(255, 255, 255, 0.95) 100%);
}

.toast-info {
  border-left: 4px solid #409eff;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.05) 0%, rgba(255, 255, 255, 0.95) 100%);
}

.toast-icon {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 2px;
  transition: all 0.3s ease;
}

.toast-message:hover .toast-icon {
  transform: scale(1.1) rotate(5deg);
}

.toast-success .toast-icon {
  color: #67c23a;
}

.toast-error .toast-icon {
  color: #f56c6c;
}

.toast-warning .toast-icon {
  color: #e6a23c;
}

.toast-info .toast-icon {
  color: #409eff;
}

.toast-content {
  flex: 1;
  min-width: 0;
}

.toast-title {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.4;
  word-break: break-word;
  transition: color 0.3s ease;
}

.toast-message-text {
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
  word-break: break-word;
  transition: color 0.3s ease;
}

.toast-close {
  flex-shrink: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-top: 2px;
  position: relative;
  overflow: hidden;
}

.toast-close::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: all 0.3s ease;
}

.toast-close:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #606266;
  transform: scale(1.1);
}

.toast-close:hover::before {
  width: 100%;
  height: 100%;
}

.toast-close:active {
  transform: scale(0.9);
}

.toast-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  background: linear-gradient(90deg, #409eff, #67c23a);
  animation: progress linear forwards;
  border-radius: 0 0 12px 12px;
}

.toast-success .toast-progress {
  background: linear-gradient(90deg, #67c23a, #85ce61);
}

.toast-error .toast-progress {
  background: linear-gradient(90deg, #f56c6c, #f78989);
}

.toast-warning .toast-progress {
  background: linear-gradient(90deg, #e6a23c, #ebb563);
}

.toast-info .toast-progress {
  background: linear-gradient(90deg, #409eff, #66b1ff);
}

@keyframes progress {
  from {
    width: 100%;
  }
  to {
    width: 0%;
  }
}

/* 高级动画效果 */
.toast-enter-active {
  transition: all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.toast-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%) scale(0.8) rotateY(-15deg);
  filter: blur(4px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%) scale(0.8) rotateY(15deg);
  filter: blur(4px);
}

.toast-move {
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 脉冲动画 */
@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.toast-message:hover .toast-progress {
  animation: progress linear forwards, pulse 2s ease-in-out infinite;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .toast-container {
    top: 6px;
    right: 6px;
    left: 6px;
    gap: 6px;
  }
  .toast-message {
    min-width: auto;
    max-width: none;
    width: 100%;
    padding: 8px 12px;
    border-radius: 7px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.10);
    gap: 7px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }
  .toast-message:hover {
    transform: translateY(-2px) scale(1.01);
  }
  .toast-title {
    font-size: 13px;
    margin-bottom: 2px;
  }
  .toast-message-text {
    font-size: 12px;
  }
  .toast-icon {
    width: 16px;
    height: 16px;
    margin-top: 1px;
  }
  .toast-close {
    width: 13px;
    height: 13px;
    margin-top: 1px;
  }
  .toast-progress {
    height: 2px;
  }
}

/* 小屏幕优化 */
@media (max-width: 480px) {
  .toast-container {
    top: 4px;
    right: 4px;
    left: 4px;
    gap: 4px;
  }
  .toast-message {
    padding: 6px 10px;
    border-radius: 6px;
    gap: 6px;
  }
  .toast-title {
    font-size: 12px;
    margin-bottom: 1px;
  }
  .toast-message-text {
    font-size: 11px;
  }
  .toast-icon {
    width: 14px;
    height: 14px;
  }
  .toast-close {
    width: 12px;
    height: 12px;
  }
  .toast-progress {
    height: 1px;
  }
}

/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
  .toast-message {
    background: rgba(30, 30, 30, 0.95);
    border: 1px solid rgba(255, 255, 255, 0.1);
  }
  .toast-title {
    color: #e5e5e5;
  }
  .toast-message-text {
    color: #a0a0a0;
  }
  .toast-close {
    color: #808080;
  }
  .toast-close:hover {
    color: #a0a0a0;
    background: rgba(255, 255, 255, 0.1);
  }
}
</style> 