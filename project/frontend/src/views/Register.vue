<template>
  <div class="register-container">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="floating-orb orb-1"></div>
      <div class="floating-orb orb-2"></div>
      <div class="floating-orb orb-3"></div>
      <div class="gradient-overlay"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">欢迎加入</h1>
        <p class="page-subtitle">幸福就差一步，来这里，开启美好之旅</p>
      </div>

      <!-- 注册卡片 -->
      <div class="register-card">
        <div class="register-card-content">
          <div class="register-header">
            <div class="register-icon">
              <el-icon size="48"><Plus /></el-icon>
            </div>
            <h2>创建账户</h2>
            <p>请填写以下信息完成注册</p>
          </div>
          
          <el-form
            ref="registerForm"
            :model="formData"
            :rules="registerRules"
            class="register-form"
            @submit.prevent="handleRegister"
          >
            <el-form-item prop="phone">
              <el-input
                v-model="formData.phone"
                placeholder="请输入手机号, 可以不是真的, 你记得住就行"
                size="large"
                clearable
                autocomplete="off"
                autocorrect="off"
                autocapitalize="off"
                spellcheck="false"
              >
                <template #prefix>
                  <el-icon><Phone /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="formData.password"
                type="password"
                placeholder="请输入密码，不少于6位"
                size="large"
                show-password
                clearable
                autocomplete="new-password"
                autocorrect="off"
                autocapitalize="off"
                spellcheck="false"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="formData.confirmPassword"
                type="password"
                placeholder="请确认密码"
                size="large"
                show-password
                clearable
                autocomplete="new-password"
                autocorrect="off"
                autocapitalize="off"
                spellcheck="false"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item>
              <div class="custom-button register-button" @click="handleRegister">
                <el-icon v-if="!loading"><Plus /></el-icon>
                <el-icon v-else class="is-loading"><Loading /></el-icon>
                <span>{{ loading ? '注册中...' : '立即注册' }}</span>
              </div>
            </el-form-item>
          </el-form>
          
          <div class="register-footer">
            <p>已有账户？ 
              <router-link to="/login" class="login-link">
                <el-icon><UserFilled /></el-icon>
                立即登录
              </router-link>
            </p>
          </div>
        </div>
        <div class="register-card-bg"></div>
      </div>

      <!-- 装饰元素 -->
      <div class="decoration-elements">
        <div class="decoration-element element-1"></div>
        <div class="decoration-element element-2"></div>
        <div class="decoration-element element-3"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { message } from '@/utils/message'
import { User, Lock, View, Hide, ArrowRight, Phone } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const registerForm = ref(null)

// 加载状态
const loading = ref(false)

// 注册表单数据
const formData = reactive({
  phone: '',
  password: '',
  confirmPassword: ''
})

// 表单验证规则
const registerRules = {
  phone: [
    { required: true, message: '请输入手机号, 可以不是真的, 你记得住就行', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码，不少于6位', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== formData.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 处理注册
const handleRegister = async () => {
  try {
    // 表单验证
    await registerForm.value.validate()
    
    loading.value = true
    
    // 使用store的register方法
    const response = await userStore.register({
      phone: formData.phone,
      password: formData.password
    })
    
    if (response.code === 200) {
      message.success('注册成功！')
      
      // 跳转到首页
      router.push('/')
    } else {
      message.error(response.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    message.error(error.message || '注册失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background: var(--color-bg);
  position: relative;
  overflow-x: hidden;
}

/* 背景装饰 */
.background-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.floating-orb {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.1), rgba(74, 222, 128, 0.05));
  filter: blur(40px);
  animation: float 20s ease-in-out infinite;
}

.orb-1 {
  width: 300px;
  height: 300px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 200px;
  height: 200px;
  top: 60%;
  right: 15%;
  animation-delay: -7s;
}

.orb-3 {
  width: 150px;
  height: 150px;
  bottom: 20%;
  left: 20%;
  animation-delay: -14s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  33% { transform: translateY(-30px) rotate(120deg); }
  66% { transform: translateY(20px) rotate(240deg); }
}

.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 50% 50%, transparent 0%, rgba(0, 0, 0, 0.02) 100%);
}

.main-content {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 60px;
}

.page-title {
  font-size: 3.5rem;
  font-weight: 800;
  background: linear-gradient(135deg, #22d36b, #4ade80, #86efac);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 16px;
  line-height: 1.1;
}

.page-subtitle {
  font-size: 1.2rem;
  color: var(--color-text);
  opacity: 0.8;
  font-weight: 400;
}

/* 注册卡片 */
.register-card {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 60px;
  width: 100%;
  max-width: 480px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.register-card:hover {
  transform: translateY(-4px);
  border-color: rgba(34, 211, 107, 0.2);
  box-shadow: 0 20px 60px rgba(34, 211, 107, 0.1);
}

.register-card-content {
  position: relative;
  z-index: 2;
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.register-icon {
  margin-bottom: 20px;
  color: #22d36b;
}

.register-header h2 {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 12px;
}

.register-header p {
  font-size: 1rem;
  color: var(--color-text);
  opacity: 0.8;
  margin: 0;
}

/* 表单样式 */
.register-form {
  margin-bottom: 30px;
}

.register-form .el-form-item {
  margin-bottom: 24px;
}

.register-form .el-input {
  --el-input-border-radius: 12px;
  --el-input-bg-color: rgba(255, 255, 255, 0.05);
  --el-input-border-color: rgba(255, 255, 255, 0.1);
  --el-input-hover-border-color: rgba(34, 211, 107, 0.3);
  --el-input-focus-border-color: #22d36b;
  --el-input-text-color: var(--color-text);
  --el-input-placeholder-color: var(--color-text);
  opacity: 0.8;
}

.register-form .el-input__wrapper {
  backdrop-filter: blur(10px);
  box-shadow: none;
  border: 1px solid var(--el-input-border-color);
  transition: all 0.3s ease;
}

.register-form .el-input__wrapper:hover {
  border-color: var(--el-input-hover-border-color);
  background: rgba(255, 255, 255, 0.08);
}

.register-form .el-input__wrapper.is-focus {
  border-color: var(--el-input-focus-border-color);
  box-shadow: 0 0 0 2px rgba(34, 211, 107, 0.2);
  background: rgba(255, 255, 255, 0.1);
}

.register-form .el-input__prefix {
  color: #22d36b;
  font-size: 18px;
}

/* 修复浏览器自动填充样式 */
.register-form .el-input__wrapper input:-webkit-autofill,
.register-form .el-input__wrapper input:-webkit-autofill:hover,
.register-form .el-input__wrapper input:-webkit-autofill:focus,
.register-form .el-input__wrapper input:-webkit-autofill:active {
  -webkit-box-shadow: 0 0 0 30px rgba(255, 255, 255, 0.05) inset !important;
  -webkit-text-fill-color: var(--color-text) !important;
  transition: background-color 5000s ease-in-out 0s;
  background-color: rgba(255, 255, 255, 0.05) !important;
}

/* 确保输入框内容颜色正确 */
.register-form .el-input__inner {
  color: var(--color-text) !important;
}

.register-form .el-input__inner::placeholder {
  color: var(--color-text);
  opacity: 0.6;
}

/* 阻止自动填充的额外样式 */
.register-form .el-input__wrapper input[autocomplete="off"] {
  background-color: rgba(255, 255, 255, 0.05) !important;
}

.register-form .el-input__wrapper input[autocomplete="new-password"] {
  background-color: rgba(255, 255, 255, 0.05) !important;
}

/* 自定义按钮 */
.custom-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 16px 24px;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  outline: none;
  position: relative;
  overflow: hidden;
}

.custom-button:hover {
  transform: translateY(-2px);
}

.custom-button:active {
  transform: translateY(0);
}

.register-button {
  background: linear-gradient(135deg, #22d36b, #4ade80);
  color: white;
  box-shadow: 0 4px 16px rgba(34, 211, 107, 0.3);
}

.register-button:hover {
  background: linear-gradient(135deg, #1fbb5e, #3dd170);
  box-shadow: 0 6px 20px rgba(34, 211, 107, 0.4);
}

.register-button .el-icon {
  font-size: 18px;
}

.register-button .is-loading {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 注册页脚 */
.register-footer {
  text-align: center;
  margin-top: 30px;
}

.register-footer p {
  color: var(--color-text);
  font-size: 0.95rem;
  margin: 0;
  opacity: 0.8;
}

.login-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #22d36b;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
  margin-left: 4px;
}

.login-link:hover {
  color: #1fbb5e;
  transform: translateX(2px);
}

.login-link .el-icon {
  font-size: 14px;
}

/* 注册卡片背景 */
.register-card-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.02), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.register-card:hover .register-card-bg {
  opacity: 1;
}

/* 装饰元素 */
.decoration-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.decoration-element {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.05), rgba(74, 222, 128, 0.02));
  animation: floatElement 15s ease-in-out infinite;
}

.element-1 {
  width: 80px;
  height: 80px;
  top: 20%;
  right: 10%;
  animation-delay: 0s;
}

.element-2 {
  width: 60px;
  height: 60px;
  bottom: 30%;
  left: 5%;
  animation-delay: -5s;
}

.element-3 {
  width: 40px;
  height: 40px;
  top: 60%;
  right: 20%;
  animation-delay: -10s;
}

@keyframes floatElement {
  0%, 100% { transform: translateY(0px) scale(1); }
  50% { transform: translateY(-20px) scale(1.1); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    padding: 0 16px;
    padding-top: 100px;
  }
  
  .page-title {
    font-size: 2.5rem;
  }
  
  .page-subtitle {
    font-size: 1rem;
  }
  
  .register-card {
    padding: 40px 24px;
    margin: 0 16px;
  }
  
  .register-header h2 {
    font-size: 1.8rem;
  }
  
  .custom-button {
    width: auto;
    min-width: 200px;
    padding: 14px 24px;
    font-size: 0.95rem;
    margin: 0 auto;
  }
  
  .register-button .el-icon {
    font-size: 16px;
  }
  
  .login-link {
    font-size: 0.9rem;
  }
  
  .login-link .el-icon {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 0 12px;
    padding-top: 80px;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .page-subtitle {
    font-size: 0.9rem;
  }
  
  .register-card {
    padding: 30px 20px;
    margin: 0 12px;
  }
  
  .register-header h2 {
    font-size: 1.6rem;
  }
  
  .register-form .el-form-item {
    margin-bottom: 20px;
  }
  
  .custom-button {
    width: auto;
    min-width: 160px;
    padding: 12px 20px;
    font-size: 0.9rem;
    gap: 6px;
    margin: 0 auto;
  }
  
  .register-button .el-icon {
    font-size: 14px;
  }
  
  .register-footer {
    margin-top: 24px;
  }
  
  .register-footer p {
    font-size: 0.85rem;
  }
  
  .login-link {
    font-size: 0.85rem;
    gap: 3px;
    margin-left: 3px;
  }
  
  .login-link .el-icon {
    font-size: 11px;
  }
}
</style> 