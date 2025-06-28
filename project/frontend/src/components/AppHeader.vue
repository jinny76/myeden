<template>
  <el-header class="header">
    <div class="header-content">
      <!-- Logo区域 -->
      <div class="logo" @click="navigateTo('/')">
        <h1>我的伊甸园</h1>
      </div>
      <!-- 桌面端导航菜单 -->
      <div class="nav-menu desktop-menu">
        <el-menu mode="horizontal" :router="true" :default-active="activeMenu">
          <el-menu-item index="/">伊甸园</el-menu-item>
          <el-menu-item index="/moments">动态</el-menu-item>
          <el-menu-item index="/world">介绍</el-menu-item>
        </el-menu>
      </div>
      <!-- 用户信息区域 -->
      <div class="user-info">
        <template v-if="isLoggedIn">
          <el-dropdown @command="handleUserCommand">
            <span class="user-avatar">
              <el-avatar :src="getUserAvatarUrl(userStore.userInfo)" />
              <span class="username">{{ userStore.userInfo?.nickname || '用户' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile-setup">个人资料</el-dropdown-item>
                <el-dropdown-item command="settings">设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <div class="auth-buttons">
            <el-button type="primary" size="small" @click="navigateTo('/login')">登录</el-button>
            <el-button size="small" @click="navigateTo('/register')">注册</el-button>
          </div>
        </template>
      </div>
      <!-- 移动端菜单按钮 -->
      <div class="mobile-menu-toggle" @click="toggleMobileMenu($event)">
        <el-icon size="24">
          <Menu v-if="!isMobileMenuOpen" />
          <Close v-else />
        </el-icon>
      </div>
    </div>
    <!-- 移动端导航菜单 -->
    <div class="mobile-menu" :class="{ 'mobile-menu-open': isMobileMenuOpen }" @click.stop>
      <div class="mobile-menu-content">
        <div class="mobile-nav-item" @click="navigateTo('/')">
          <el-icon><House /></el-icon>
          <span>伊甸园</span>
        </div>
        <div class="mobile-nav-item" @click="navigateTo('/moments')">
          <el-icon><ChatDotRound /></el-icon>
          <span>动态</span>
        </div>
        <div class="mobile-nav-item" @click="navigateTo('/world')">
          <el-icon><Compass /></el-icon>
          <span>介绍</span>
        </div>
        <div class="mobile-nav-divider"></div>
        <template v-if="isLoggedIn">
          <div class="mobile-nav-item" @click="navigateTo('/profile-setup')">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </div>
          <div class="mobile-nav-item" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </div>
        </template>
        <template v-else>
          <div class="mobile-nav-item" @click="navigateTo('/login')">
            <el-icon><UserFilled /></el-icon>
            <span>登录</span>
          </div>
          <div class="mobile-nav-item" @click="navigateTo('/register')">
            <el-icon><UserFilled /></el-icon>
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
      message.info('设置功能开发中...')
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
/***** 复用Home.vue头部样式，可根据需要调整主题变量 *****/
.header {
  background: var(--color-card);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-border);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 0;
  height: auto;
  min-height: 60px;
}
.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
}
.logo h1 {
  margin: 0;
  color: var(--color-text);
  font-size: 24px;
  font-weight: bold;
  white-space: nowrap;
}
.logo {
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: background-color 0.3s;
  user-select: none;
}
.logo:hover {
  background-color: rgba(0, 0, 0, 0.05);
}
.logo:active {
  background-color: rgba(0, 0, 0, 0.1);
}
.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}
.desktop-menu {
  display: block;
}
.user-info {
  display: flex;
  align-items: center;
}
.user-avatar {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background-color 0.3s;
}
.user-avatar:hover {
  background-color: rgba(0, 0, 0, 0.05);
}
.username {
  margin-left: 8px;
  color: var(--color-text);
  font-weight: 500;
  white-space: nowrap;
}
.auth-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}
.mobile-menu-toggle {
  display: none;
  cursor: pointer;
  padding: 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
  color: var(--color-text);
  min-width: 44px;
  min-height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  user-select: none;
  -webkit-tap-highlight-color: transparent;
}
.mobile-menu-toggle:hover {
  background-color: rgba(0, 0, 0, 0.05);
}
.mobile-menu-toggle:active {
  background-color: rgba(0, 0, 0, 0.1);
}
.mobile-menu {
  display: none;
  background: var(--color-card);
  backdrop-filter: blur(10px);
  border-top: 1px solid var(--color-border);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 2000;
}
.mobile-menu.mobile-menu-open {
  display: block;
}
.mobile-menu-content {
  padding: 16px 20px;
}
.mobile-nav-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.3s;
  color: var(--color-text);
  font-weight: 500;
}
.mobile-nav-item:hover {
  background-color: rgba(102, 126, 234, 0.1);
}
.mobile-nav-item:active {
  background-color: rgba(102, 126, 234, 0.2);
}
.mobile-nav-item .el-icon {
  margin-right: 12px;
  font-size: 18px;
  color: var(--color-primary);
}
.mobile-nav-item span {
  font-size: 16px;
}
.mobile-nav-divider {
  height: 1px;
  background-color: var(--color-border);
  margin: 12px 0;
}
@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
    height: 56px;
  }
  .logo h1 {
    font-size: 20px;
  }
  .logo {
    padding: 10px 8px;
    min-height: 36px;
    display: flex;
    align-items: center;
  }
  .desktop-menu {
    display: none;
  }
  .user-info {
    display: none;
  }
  .mobile-menu-toggle {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 14px 12px;
    min-width: 48px;
    min-height: 48px;
  }
}
@media (max-width: 480px) {
  .header-content {
    padding: 0 12px;
    height: 52px;
  }
  .logo h1 {
    font-size: 18px;
  }
  .logo {
    padding: 8px 6px;
    min-height: 32px;
  }
  .mobile-menu-toggle {
    padding: 12px 10px;
    min-width: 44px;
    min-height: 44px;
  }
}
</style> 