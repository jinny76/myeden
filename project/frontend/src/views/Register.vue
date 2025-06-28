<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h2>欢迎来到我的伊甸园</h2>
        <p>创建一个新账户开始你的伊甸园之旅</p>
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
            placeholder="请输入手机号"
            prefix-icon="Phone"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="formData.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="register-button"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '立即注册' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <p>已有账户？ <router-link to="/login">立即登录</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
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
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
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
      ElMessage.success('注册成功！')
      
      // 跳转到首页
      router.push('/')
    } else {
      ElMessage.error(response.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.message || '注册失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
  padding: 20px;
}

.register-card {
  background: var(--color-card);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
  
  h2 {
    color: var(--color-text);
    margin-bottom: 8px;
    font-size: 24px;
    font-weight: 600;
  }
  
  p {
    color: var(--color-text);
    font-size: 14px;
    margin: 0;
  }
}

.register-form {
  .el-form-item {
    margin-bottom: 20px;
  }
  
  .el-input {
    .el-input__wrapper {
      border-radius: 8px;
      border: 1px solid var(--color-border);
      background: var(--color-card);
      color: var(--color-text);
      
      &:hover {
        border-color: var(--color-primary);
      }
      
      &.is-focus {
        border-color: var(--color-primary);
        box-shadow: 0 0 0 2px rgba(34, 211, 107, 0.2);
      }
    }
  }
}

.register-button {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: var(--color-primary);
  border: none;
  color: #fff;
  
  &:hover {
    background: #1db35b;
  }
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  
  p {
    color: var(--color-text);
    font-size: 14px;
    margin: 0;
    
    a {
      color: var(--color-primary);
      text-decoration: none;
      
      &:hover {
        text-decoration: underline;
      }
    }
  }
}

// 响应式设计
@media (max-width: 480px) {
  .register-card {
    padding: 30px 20px;
  }
  
  .register-header h2 {
    font-size: 20px;
  }
}
</style> 