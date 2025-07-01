<template>
  <el-header class="header">
    <div class="header-content">
      <!-- Logo区域 -->
      <div class="logo" @click="navigateTo('/')">
        <h1>我的伊甸园</h1>
      </div>
      
      <!-- 桌面端导航菜单 -->
      <div class="nav-menu desktop-menu">
        <div class="nav-item" 
          :class="{ active: activeMenu === '/' }" 
          @click="navigateTo('/')"
        >
          <el-icon><House /></el-icon>
          <span>伊甸园</span>
        </div>
        <div class="nav-item" 
          :class="{ active: activeMenu === '/moments' }" 
          @click="navigateTo('/moments')"
        >
          <el-icon><ChatDotRound /></el-icon>
          <span>动态</span>
        </div>
        <div class="nav-item" 
          :class="{ active: activeMenu === '/world' }" 
          @click="navigateTo('/world')"
        >
          <el-icon><Compass /></el-icon>
          <span>介绍</span>
        </div>
      </div>
      
      <!-- 用户信息区域 -->
      <div class="user-info">
        <template v-if="isLoggedIn">
          <el-dropdown @command="handleUserCommand">
            <div class="user-avatar">
              <el-avatar :src="getUserAvatarUrl(userStore.userInfo)" />
              <span class="username">{{ userStore.userInfo?.nickname || '用户' }}</span>
              <el-icon class="dropdown-arrow"><ArrowRight /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile-setup">
                  <el-icon><User /></el-icon>
                  <span>个人资料</span>
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  <span>设置</span>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <div class="auth-buttons">
            <div class="auth-button login-btn" @click="navigateTo('/login')">
              <el-icon><UserFilled /></el-icon>
              <span>登录</span>
            </div>
            <div class="auth-button register-btn" @click="navigateTo('/register')">
              <el-icon><Plus /></el-icon>
              <span>注册</span>
            </div>
          </div>
        </template>
      </div>
      
      <!-- 移动端快速导航 -->
      <div class="mobile-quick-nav">
        <div class="quick-nav-item" 
          :class="{ active: activeMenu === '/' }" 
          @click="navigateTo('/')"
        >
          <el-icon><House /></el-icon>
          <span>首页</span>
        </div>
        <div class="quick-nav-item" 
          :class="{ active: activeMenu === '/moments' }" 
          @click="navigateTo('/moments')"
        >
          <el-icon><ChatDotRound /></el-icon>
          <span>动态</span>
        </div>
      </div>
      
      <!-- 移动端用户菜单按钮 -->
      <div class="mobile-user-menu" @click="toggleMobileMenu($event)">
        <template v-if="isLoggedIn">
          <el-avatar :src="getUserAvatarUrl(userStore.userInfo)" size="small" />
        </template>
        <template v-else>
          <el-icon size="20">
            <Menu v-if="!isMobileMenuOpen" />
            <Close v-else />
          </el-icon>
        </template>
      </div>
    </div>
    
    <!-- 移动端用户菜单 -->
    <div class="mobile-menu" :class="{ 'mobile-menu-open': isMobileMenuOpen }" @click.stop>
      <div class="mobile-menu-content">
        <template v-if="isLoggedIn">
          <!-- 用户信息 -->
          <div class="mobile-user-info">
            <el-avatar :src="getUserAvatarUrl(userStore.userInfo)" size="large" />
            <div class="user-details">
              <span class="user-name">{{ userStore.userInfo?.nickname || '用户' }}</span>
              <span class="user-status">已登录</span>
            </div>
          </div>
          
          <!-- 用户菜单 -->
          <div class="mobile-nav-item" @click="navigateTo('/profile-setup')">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </div>
          <div class="mobile-nav-item" @click="navigateTo('/settings')">
            <el-icon><Setting /></el-icon>
            <span>设置</span>
          </div>
          <div class="mobile-nav-item logout-item" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </div>
        </template>
        <template v-else>
          <!-- 未登录状态 -->
          <div class="mobile-nav-item" @click="navigateTo('/login')">
            <el-icon><UserFilled /></el-icon>
            <span>登录</span>
          </div>
          <div class="mobile-nav-item" @click="navigateTo('/register')">
            <el-icon><Plus /></el-icon>
            <span>注册</span>
          </div>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { message } from '@/utils/message'
import { 
  ChatDotRound, Compass, User, Menu, Close, House, SwitchButton, 
  UserFilled, ArrowRight, Star, Setting, Plus, View, Bell 
} from '@element-plus/icons-vue'
import { getUserAvatarUrl } from '@/utils/avatar'

const router = useRouter()
const userStore = useUserStore()
const activeMenu = computed(() => router.currentRoute.value.path)
const isMobileMenuOpen = ref(false)
const isLoggedIn = computed(() => userStore.isLoggedIn)

const navigateTo = (path) => {
  router.push(path)
  isMobileMenuOpen.value = false
}

const handleUserCommand = async (command) => {
  switch (command) {
    case 'profile-setup':
      router.push('/profile-setup')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      await handleLogout()
      break
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userStore.logout()
    message.success('退出登录成功')
    router.push('/login')
    isMobileMenuOpen.value = false
  } catch (error) {
    if (error !== 'cancel') {
      message.error('退出登录失败')
    }
  }
}

const toggleMobileMenu = (event) => {
  if (event) event.stopPropagation();
  isMobileMenuOpen.value = !isMobileMenuOpen.value;
}

const handleClickOutside = (event) => {
  const header = document.querySelector('.header')
  if (header && !header.contains(event.target) && isMobileMenuOpen.value) {
    isMobileMenuOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 0;
  height: auto;
  min-height: 64px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  position: relative;
}

/* Logo样式 */
.logo {
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.05), rgba(74, 222, 128, 0.02));
  border: 1px solid rgba(34, 211, 107, 0.1);
}

.logo:hover {
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.1), rgba(74, 222, 128, 0.05));
  border-color: rgba(34, 211, 107, 0.2);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(34, 211, 107, 0.15);
}

.logo:active {
  transform: translateY(0);
}

.logo h1 {
  margin: 0;
  background: linear-gradient(135deg, #22d36b, #4ade80);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 20px;
  font-weight: 700;
  white-space: nowrap;
}

/* 导航菜单 */
.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
  gap: 8px;
}

.desktop-menu {
  display: flex;
}

.nav-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: var(--color-text);
  font-weight: 500;
  font-size: 14px;
  position: relative;
  overflow: hidden;
  min-height: 44px;
}

.nav-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.1), rgba(74, 222, 128, 0.05));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.nav-item:hover {
  background: rgba(34, 211, 107, 0.08);
  transform: translateY(-1px);
}

.nav-item:hover::before {
  opacity: 1;
}

.nav-item.active {
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.15), rgba(74, 222, 128, 0.08));
  color: #22d36b;
  border: 1px solid rgba(34, 211, 107, 0.2);
}

.nav-item .el-icon {
  font-size: 16px;
  transition: transform 0.3s ease;
}

.nav-item:hover .el-icon {
  transform: scale(1.1);
}

/* 用户信息区域 */
.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.2);
  min-height: 44px;
}

.user-avatar:hover {
  background: rgba(255, 255, 255, 0.8);
  border-color: rgba(34, 211, 107, 0.2);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.username {
  color: var(--color-text);
  font-weight: 500;
  font-size: 14px;
  white-space: nowrap;
}

.dropdown-arrow {
  color: var(--color-text);
  opacity: 0.6;
  font-size: 12px;
  transition: transform 0.3s ease;
}

.user-avatar:hover .dropdown-arrow {
  transform: rotate(90deg);
}

/* 认证按钮 */
.auth-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.auth-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
  font-size: 14px;
  border: 1px solid transparent;
  min-height: 40px;
}

.login-btn {
  background: linear-gradient(135deg, #22d36b, #4ade80);
  color: white;
  box-shadow: 0 2px 8px rgba(34, 211, 107, 0.2);
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(34, 211, 107, 0.3);
  background: linear-gradient(135deg, #1fbb5e, #3dd170);
}

.register-btn {
  background: transparent;
  border: 1px solid rgba(34, 211, 107, 0.3);
  color: #22d36b;
  backdrop-filter: blur(10px);
}

.register-btn:hover {
  background: rgba(34, 211, 107, 0.1);
  border-color: #22d36b;
  box-shadow: 0 2px 8px rgba(34, 211, 107, 0.15);
  transform: translateY(-1px);
}

/* 移动端快速导航 */
.mobile-quick-nav {
  display: none;
  align-items: center;
  gap: 8px;
  flex: 1;
  justify-content: center;
  margin: 0 16px;
}

.quick-nav-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: var(--color-text);
  font-weight: 500;
  font-size: 12px;
  position: relative;
  min-width: 60px;
  min-height: 60px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.quick-nav-item:hover {
  background: rgba(34, 211, 107, 0.1);
  border-color: rgba(34, 211, 107, 0.2);
  transform: translateY(-1px);
}

.quick-nav-item.active {
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.15), rgba(74, 222, 128, 0.08));
  color: #22d36b;
  border-color: rgba(34, 211, 107, 0.3);
  box-shadow: 0 2px 8px rgba(34, 211, 107, 0.2);
}

.quick-nav-item .el-icon {
  font-size: 16px;
  transition: transform 0.3s ease;
}

.quick-nav-item:hover .el-icon {
  transform: scale(1.1);
}

.quick-nav-item span {
  font-size: 12px;
  line-height: 1;
  white-space: nowrap;
}

/* 移动端用户菜单按钮 */
.mobile-user-menu {
  display: none;
  cursor: pointer;
  padding: 8px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: var(--color-text);
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.1);
  user-select: none;
  -webkit-tap-highlight-color: transparent;
  min-width: 40px;
  min-height: 40px;
  align-items: center;
  justify-content: center;
}

.mobile-user-menu:hover {
  background: rgba(255, 255, 255, 0.8);
  border-color: rgba(34, 211, 107, 0.2);
  transform: translateY(-1px);
}

.mobile-user-menu:active {
  transform: translateY(0);
}

/* 移动端菜单 */
.mobile-menu {
  display: none;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 2000;
  animation: slideDown 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.mobile-menu.mobile-menu-open {
  display: block;
}

.mobile-menu-content {
  padding: 16px 24px;
}

.mobile-nav-item {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  padding: 14px 16px;
  cursor: pointer;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: var(--color-text);
  font-weight: 500;
  font-size: 16px;
  margin-bottom: 4px;
  min-height: 48px;
}

.mobile-nav-item:hover {
  background: rgba(34, 211, 107, 0.1);
  transform: translateX(4px);
}

.mobile-nav-item:active {
  background: rgba(34, 211, 107, 0.2);
  transform: translateX(2px);
}

.mobile-nav-item .el-icon {
  font-size: 18px;
  color: #22d36b;
  transition: transform 0.3s ease;
}

.mobile-nav-item:hover .el-icon {
  transform: scale(1.1);
}

.mobile-nav-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(34, 211, 107, 0.2), transparent);
  margin: 16px 0;
}

/* 移动端用户信息 */
.mobile-user-info {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.1), rgba(74, 222, 128, 0.05));
  border-radius: 12px;
  margin-bottom: 16px;
  border: 1px solid rgba(34, 211, 107, 0.2);
  min-height: 60px;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text);
}

.user-status {
  font-size: 12px;
  color: #22d36b;
  font-weight: 500;
  opacity: 0.8;
}

/* 退出登录项 */
.logout-item {
  color: #ff4757;
}

.logout-item .el-icon {
  color: #ff4757;
}

.logout-item:hover {
  background: rgba(255, 71, 87, 0.1);
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .header-content {
    max-width: 100%;
    padding: 0 20px;
  }
  
  .nav-item {
    padding: 8px 12px;
    font-size: 13px;
  }
  
  .nav-item .el-icon {
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
    height: 56px;
  }
  
  .logo h1 {
    font-size: 18px;
  }
  
  .logo {
    padding: 6px 10px;
  }
  
  .desktop-menu {
    display: none;
  }
  
  .user-info {
    display: none;
  }
  
  .mobile-quick-nav {
    display: flex;
  }
  
  .mobile-user-menu {
    display: flex;
  }
}

@media (max-width: 480px) {
  .header-content {
    padding: 0 12px;
    height: 52px;
  }
  
  .logo h1 {
    font-size: 16px;
  }
  
  .logo {
    padding: 4px 8px;
  }
  
  .mobile-quick-nav {
    margin: 0 6px;
    gap: 4px;
  }
  
  .quick-nav-item {
    padding: 4px 6px;
    min-width: 44px;
    min-height: 44px;
    gap: 4px;
  }
  
  .quick-nav-item .el-icon {
    font-size: 14px;
  }
  
  .quick-nav-item span {
    font-size: 10px;
  }
  
  .mobile-user-menu {
    padding: 4px;
    min-width: 32px;
    min-height: 32px;
  }
  
  .mobile-menu-content {
    padding: 12px 16px;
  }
  
  .mobile-nav-item {
    padding: 12px 14px;
    font-size: 15px;
  }
}

/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
  .header {
    background: rgba(30, 30, 30, 0.95);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .logo {
    background: linear-gradient(135deg, rgba(34, 211, 107, 0.1), rgba(74, 222, 128, 0.05));
    border-color: rgba(34, 211, 107, 0.2);
  }
  
  .nav-item {
    color: #e5e5e5;
  }
  
  .nav-item:hover {
    background: rgba(34, 211, 107, 0.15);
  }
  
  .user-avatar {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.1);
  }
  
  .user-avatar:hover {
    background: rgba(255, 255, 255, 0.15);
  }
  
  .username {
    color: #e5e5e5;
  }
  
  .mobile-quick-nav .quick-nav-item {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.1);
    color: #e5e5e5;
  }
  
  .mobile-quick-nav .quick-nav-item:hover {
    background: rgba(34, 211, 107, 0.15);
  }
  
  .mobile-user-menu {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.1);
    color: #e5e5e5;
  }
  
  .mobile-user-menu:hover {
    background: rgba(255, 255, 255, 0.15);
  }
  
  .mobile-menu {
    background: rgba(30, 30, 30, 0.95);
    border-top-color: rgba(255, 255, 255, 0.1);
  }
  
  .mobile-nav-item {
    color: #e5e5e5;
  }
  
  .mobile-user-info {
    background: linear-gradient(135deg, rgba(34, 211, 107, 0.15), rgba(74, 222, 128, 0.08));
    border-color: rgba(34, 211, 107, 0.3);
  }
  
  .user-name {
    color: #e5e5e5;
  }
}
</style> 