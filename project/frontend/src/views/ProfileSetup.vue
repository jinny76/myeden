<template>
  <div class="profile-setup-container">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="floating-orb orb-1"></div>
      <div class="floating-orb orb-2"></div>
      <div class="floating-orb orb-3"></div>
      <div class="gradient-overlay"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="profile-setup-card">
        <div class="profile-setup-header">
          <div class="header-icon">
            <el-icon size="32"><User /></el-icon>
          </div>
          <h1>完善个人资料</h1>
          <p>让我们更好地了解你，为你打造专属的伊甸园体验</p>
        </div>
        
        <el-form
          ref="profileForm"
          :model="formData"
          :rules="profileRules"
          class="profile-setup-form"
          label-width="80px"
        >
          <el-form-item label="头像" prop="avatar">
            <div class="avatar-upload">
              <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleAvatarUpload"
                accept="image/jpeg,image/png,image/gif"
              >
                <div class="avatar-container">
                  <img v-if="avatarUrl" :src="avatarUrl" class="avatar" @error="handleAvatarError" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                  <div class="avatar-overlay">
                    <el-icon><Camera /></el-icon>
                    <span>更换头像</span>
                  </div>
                </div>
              </el-upload>
              <p class="upload-tip">点击上传头像，支持JPG、PNG格式</p>
            </div>
          </el-form-item>
          
          <el-form-item label="昵称" prop="nickname">
            <el-input
              v-model="formData.nickname"
              placeholder="请输入你的昵称"
              clearable
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item label="性别" prop="gender">
            <div class="gender-options">
              <div 
                class="gender-option" 
                :class="{ active: formData.gender === 'male' }"
                @click="formData.gender = 'male'"
              >
                <el-icon><Male /></el-icon>
                <span>男</span>
              </div>
              <div 
                class="gender-option" 
                :class="{ active: formData.gender === 'female' }"
                @click="formData.gender = 'female'"
              >
                <el-icon><Female /></el-icon>
                <span>女</span>
              </div>
              <div 
                class="gender-option" 
                :class="{ active: formData.gender === 'other' }"
                @click="formData.gender = 'other'"
              >
                <el-icon><User /></el-icon>
                <span>其他</span>
              </div>
            </div>
          </el-form-item>
          
          <el-form-item label="生日" prop="birthday">
            <el-date-picker
              v-model="formData.birthday"
              type="date"
              placeholder="选择你的生日"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              class="custom-date-picker"
            />
          </el-form-item>
          
          <el-form-item label="个人简介" prop="bio">
            <el-input
              v-model="formData.bio"
              type="textarea"
              :rows="3"
              placeholder="介绍一下你自己，让天使们更好地了解你..."
              maxlength="500"
              show-word-limit
              class="custom-textarea"
            />
          </el-form-item>
          
          <el-form-item>
            <div class="save-button" @click="handleSave">
              <el-icon v-if="!loading"><Check /></el-icon>
              <div v-else class="loading-spinner"></div>
              <span>{{ loading ? '保存中...' : '保存并完成' }}</span>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from '@/utils/message'
import { Plus, Camera, Male, Female, User, Check, Brush } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useConfigStore } from '@/stores/config'
import { userApi } from '@/api/user'
import { getUserAvatarUrl, buildAvatarUrl, generateDefaultAvatar, handleAvatarError as handleAvatarErrorUtil } from '@/utils/avatar'

const router = useRouter()
const userStore = useUserStore()
const configStore = useConfigStore()

// 表单引用
const profileForm = ref(null)

// 加载状态
const loading = ref(false)

// 头像URL
const avatarUrl = ref('')

// 个人资料表单数据
const formData = reactive({
  nickname: '',
  gender: 'male',
  birthday: '',
  bio: ''
})

// 表单验证规则
const profileRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在2到20个字符', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  birthday: [
    { required: true, message: '请选择生日', trigger: 'change' }
  ],
  bio: [
    { max: 200, message: '个性签名不能超过200个字符', trigger: 'blur' }
  ]
}

// 主题
const theme = computed(() => configStore.config.theme.mode)
const onThemeChange = (val) => { 
  configStore.updateTheme('mode', val)
}

// 初始化头像URL
const initAvatarUrl = () => {
  // 使用工具函数获取头像URL
  const userAvatarUrl = getUserAvatarUrl(userStore.userInfo)
  avatarUrl.value = userAvatarUrl
}

// 初始化用户资料
const initUserProfile = () => {
  const userInfo = userStore.userInfo
  if (userInfo) {
    // 加载用户信息到表单
    formData.nickname = userInfo.nickname || ''
    formData.gender = userInfo.gender || 'male'
    formData.birthday = userInfo.birthday || ''
    formData.bio = userInfo.introduction || ''
    
    // 初始化头像
    initAvatarUrl()
  } else {
    // 如果store中没有用户信息，尝试重新获取
    loadUserInfo()
  }
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    await userStore.fetchUserInfo()
    initUserProfile()
  } catch (error) {
    console.error('加载用户信息失败:', error)
    message.error('加载用户信息失败，请重新登录')
    router.push('/login')
  }
}

// 组件挂载时初始化
onMounted(() => {
  // 延迟初始化，确保路由守卫执行完成
  setTimeout(() => {
    initUserProfile()
  }, 100)
})

// 头像上传前的验证
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isJPG) {
    message.error('头像只能是JPG或PNG格式!')
  }
  if (!isLt5M) {
    message.error('头像大小不能超过5MB!')
  }
  return isJPG && isLt5M
}

// 头像上传处理
const handleAvatarUpload = async (options) => {
  try {
    const userId = userStore.userInfo?.userId
    if (!userId) {
      message.error('用户信息不存在，请重新登录')
      return
    }
    
    const formData = new FormData()
    formData.append('file', options.file)
    
    const response = await userApi.uploadAvatar(userId, formData)
    
    if (response.code === 200 && response.data) {
      // 正确获取头像URL
      const avatarUrlFromResponse = response.data.avatarUrl
      
      if (avatarUrlFromResponse) {
        // 使用工具函数构建完整头像URL
        const fullAvatarUrl = buildAvatarUrl(avatarUrlFromResponse)
        
        // 更新本地头像URL显示
        avatarUrl.value = fullAvatarUrl
        // 更新用户store中的头像信息（保存相对路径）
        userStore.updateAvatar(avatarUrlFromResponse)
        message.success('头像上传成功')
      } else {
        message.error('头像URL获取失败')
      }
    } else {
      message.error(response.message || '头像上传失败')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    message.error('头像上传失败，请重试')
  }
}

// 处理保存
const handleSave = async () => {
  try {
    // 表单验证
    await profileForm.value.validate()
    
    loading.value = true
    
    // 获取当前用户ID
    const userId = userStore.userInfo?.userId
    if (!userId) {
      message.error('用户信息不存在，请重新登录')
      router.push('/login')
      return
    }
    
    // 使用store的updateUserInfo方法
    const response = await userStore.updateUserInfo(userId, {
      nickname: formData.nickname,
      gender: formData.gender,
      birthday: formData.birthday,
      introduction: formData.bio
    })
    
    if (response.code === 200) {
      // 完成首次登录
      await userApi.completeFirstLogin(userId)
      
      message.success('个人资料保存成功！')
      
      // 跳转到首页
      router.push('/')
    } else {
      message.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    message.error(error.message || '保存失败，请重试')
  } finally {
    loading.value = false
  }
}

// 监听用户信息变化
watch(() => userStore.userInfo, (newValue) => {
  if (newValue) {
    initUserProfile()
  }
})

// 处理头像错误
const handleAvatarError = (event) => {
  const nickname = userStore.userInfo?.nickname || 'User'
  handleAvatarErrorUtil(event, nickname)
  message.error('头像加载失败，已切换到默认头像')
}
</script>

<style scoped lang="scss">
.profile-setup-container {
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
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 120px 20px 40px;
}

.profile-setup-card {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 40px 40px;
  width: 100%;
  max-width: 550px;
  overflow: hidden;
}

.profile-setup-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.02), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.profile-setup-card:hover::before {
  opacity: 1;
}

.profile-setup-header {
  text-align: center;
  margin-bottom: 30px;
  position: relative;
  z-index: 2;
}

.header-icon {
  color: #22d36b;
  margin-bottom: 16px;
}

.profile-setup-header h1 {
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #22d36b, #4ade80, #86efac);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12px;
  line-height: 1.2;
}

.profile-setup-header p {
  font-size: 1rem;
  color: var(--color-text);
  opacity: 0.8;
  line-height: 1.5;
  margin: 0;
}

.profile-setup-form {
  position: relative;
  z-index: 2;
  
  .el-form-item {
    margin-bottom: 20px;
  }
  
  .el-form-item__label {
    color: var(--color-text);
    font-weight: 500;
    font-size: 0.95rem;
  }
}

/* 自定义输入框样式 */
.custom-input,
.custom-textarea,
.custom-date-picker {
  .el-input__wrapper {
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 10px;
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;
    
    &:hover {
      border-color: rgba(34, 211, 107, 0.3);
      box-shadow: 0 2px 8px rgba(34, 211, 107, 0.1);
    }
    
    &.is-focus {
      border-color: #22d36b;
      box-shadow: 0 0 0 2px rgba(34, 211, 107, 0.2);
    }
  }
  
  .el-input__inner {
    color: var(--color-text);
    font-size: 0.95rem;
    
    &::placeholder {
      color: var(--color-text);
      opacity: 0.5;
    }
  }
}

.custom-textarea {
  .el-textarea__inner {
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 10px;
    color: var(--color-text);
    font-size: 0.95rem;
    resize: vertical;
    
    &:hover {
      border-color: rgba(34, 211, 107, 0.3);
    }
    
    &:focus {
      border-color: #22d36b;
      box-shadow: 0 0 0 2px rgba(34, 211, 107, 0.2);
    }
    
    &::placeholder {
      color: var(--color-text);
      opacity: 0.5;
    }
  }
}

/* 头像上传样式 */
.avatar-upload {
  text-align: center;
  
  .avatar-uploader {
    display: inline-block;
    margin-bottom: 12px;
  }
  
  .avatar-container {
    position: relative;
    display: inline-block;
    cursor: pointer;
    border-radius: 50%;
    overflow: hidden;
    transition: all 0.3s ease;
    
    &:hover .avatar-overlay {
      opacity: 1;
    }
  }
  
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 100px;
    height: 100px;
    line-height: 100px;
    text-align: center;
    border: 2px dashed rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.05);
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;
    
    &:hover {
      border-color: #22d36b;
      color: #22d36b;
      background: rgba(34, 211, 107, 0.1);
    }
  }
  
  .avatar {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    object-fit: cover;
    border: 3px solid rgba(255, 255, 255, 0.1);
  }
  
  .avatar-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: white;
    opacity: 0;
    transition: opacity 0.3s ease;
    
    .el-icon {
      font-size: 20px;
      margin-bottom: 4px;
    }
    
    span {
      font-size: 11px;
      font-weight: 500;
    }
  }
  
  .upload-tip {
    color: var(--color-text);
    font-size: 0.85rem;
    opacity: 0.7;
    margin: 0;
  }
}

/* 性别选择样式 */
.gender-options {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: nowrap;
}

.gender-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 16px 20px;
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 70px;
  flex-shrink: 0;
  
  .el-icon {
    font-size: 20px;
    color: var(--color-text);
    opacity: 0.7;
  }
  
  span {
    font-size: 0.85rem;
    color: var(--color-text);
    font-weight: 500;
    white-space: nowrap;
  }
  
  &:hover {
    border-color: rgba(34, 211, 107, 0.3);
    background: rgba(34, 211, 107, 0.05);
    transform: translateY(-1px);
  }
  
  &.active {
    border-color: #22d36b;
    background: rgba(34, 211, 107, 0.1);
    
    .el-icon {
      color: #22d36b;
      opacity: 1;
    }
    
    span {
      color: #22d36b;
    }
  }
}

/* 保存按钮样式 */
.save-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 12px 20px;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  outline: none;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #22d36b, #4ade80);
  color: white;
  box-shadow: 0 2px 8px rgba(34, 211, 107, 0.3);
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(34, 211, 107, 0.4);
  }
  
  &:active {
    transform: translateY(0);
  }
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 分割线样式 */
.divider {
  position: relative;
  text-align: center;
  margin: 30px 0 20px;
  
  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  }
  
  span {
    background: var(--color-bg);
    padding: 0 16px;
    color: var(--color-text);
    font-size: 0.85rem;
    font-weight: 500;
    opacity: 0.8;
  }
}

/* 主题设置样式 */
.theme-setting {
  position: relative;
  z-index: 2;
}

.setting-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  color: var(--color-text);
  font-weight: 500;
  font-size: 0.95rem;
  
  .el-icon {
    color: #22d36b;
    font-size: 18px;
  }
}

.theme-options {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: nowrap;
}

.theme-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
  
  &:hover {
    transform: translateY(-1px);
  }
  
  &.active {
    .theme-preview {
      border-color: #22d36b;
      box-shadow: 0 2px 8px rgba(34, 211, 107, 0.3);
    }
    
    span {
      color: #22d36b;
    }
  }
  
  span {
    font-size: 0.85rem;
    color: var(--color-text);
    font-weight: 500;
    transition: color 0.3s ease;
    white-space: nowrap;
  }
}

.theme-preview {
  width: 60px;
  height: 45px;
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  overflow: hidden;
  transition: all 0.3s ease;
  
  .preview-header {
    height: 15px;
    background: #f5f5f5;
  }
  
  .preview-content {
    height: 30px;
    background: white;
  }
  
  &.dark-theme {
    .preview-header {
      background: #2c2c2c;
    }
    
    .preview-content {
      background: #1a1a1a;
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    padding: 100px 16px 20px;
  }
  
  .profile-setup-card {
    padding: 30px 24px;
  }
  
  .profile-setup-header h1 {
    font-size: 1.8rem;
  }
  
  .profile-setup-header p {
    font-size: 0.9rem;
  }
  
  .gender-options {
    gap: 8px;
  }
  
  .gender-option {
    padding: 12px 16px;
    min-width: 60px;
  }
  
  .theme-options {
    gap: 12px;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 90px 12px 20px;
  }
  
  .profile-setup-card {
    padding: 24px 20px;
  }
  
  .profile-setup-header h1 {
    font-size: 1.6rem;
  }
  
  .profile-setup-header p {
    font-size: 0.85rem;
  }
  
  .avatar-uploader-icon,
  .avatar {
    width: 80px;
    height: 80px;
    line-height: 80px;
  }
  
  .gender-option {
    padding: 10px 12px;
    min-width: 50px;
  }
  
  .gender-option .el-icon {
    font-size: 18px;
  }
  
  .gender-option span {
    font-size: 0.8rem;
  }
  
  .theme-preview {
    width: 50px;
    height: 38px;
  }
  
  .theme-preview .preview-header {
    height: 12px;
  }
  
  .theme-preview .preview-content {
    height: 26px;
  }
  
  .save-button {
    padding: 12px 16px;
    font-size: 0.95rem;
  }
}
</style> 