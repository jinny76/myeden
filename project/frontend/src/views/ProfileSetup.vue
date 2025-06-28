<template>
  <div class="profile-setup-container">
    <div class="profile-setup-card">
      <div class="profile-setup-header">
        <h2>完善个人资料</h2>
        <p>让我们更好地了解你</p>
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
              <img v-if="avatarUrl" :src="avatarUrl" class="avatar" @error="handleAvatarError" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <p class="upload-tip">点击上传头像</p>
          </div>
        </el-form-item>
        
        <el-form-item label="昵称" prop="nickname">
          <el-input
            v-model="formData.nickname"
            placeholder="请输入昵称"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="formData.gender">
            <el-radio label="male">男</el-radio>
            <el-radio label="female">女</el-radio>
            <el-radio label="other">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="formData.birthday"
            type="date"
            placeholder="选择生日"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item label="介绍你自己给天使们" prop="bio">
          <el-input
            v-model="formData.bio"
            type="textarea"
            :rows="3"
            placeholder="介绍一下自己吧..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="save-button"
            @click="handleSave"
          >
            {{ loading ? '保存中...' : '保存并完成' }}
          </el-button>
        </el-form-item>
      </el-form>
      <el-divider style="margin: 32px 0 16px 0;" />
      <div class="theme-setting">
        <span>主题配色：</span>
        <el-radio-group v-model="theme" @change="onThemeChange">
          <el-radio label="light">亮色</el-radio>
          <el-radio label="dark">暗色</el-radio>
        </el-radio-group>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { getUserAvatarUrl, buildAvatarUrl, generateDefaultAvatar, handleAvatarError as handleAvatarErrorUtil } from '@/utils/avatar'
import { setTheme, getTheme } from '@/utils/theme'

const router = useRouter()
const userStore = useUserStore()

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
const theme = ref(getTheme())
const onThemeChange = (val) => { setTheme(val) }

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
    ElMessage.error('加载用户信息失败，请重新登录')
    router.push('/login')
  }
}

// 组件挂载时初始化
onMounted(() => {
  // 延迟初始化，确保路由守卫执行完成
  setTimeout(() => {
    initUserProfile()
  }, 100)
  theme.value = getTheme()
})

// 头像上传前的验证
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isJPG) {
    ElMessage.error('头像只能是JPG或PNG格式!')
  }
  if (!isLt5M) {
    ElMessage.error('头像大小不能超过5MB!')
  }
  return isJPG && isLt5M
}

// 头像上传处理
const handleAvatarUpload = async (options) => {
  try {
    const userId = userStore.userInfo?.userId
    if (!userId) {
      ElMessage.error('用户信息不存在，请重新登录')
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
        ElMessage.success('头像上传成功')
      } else {
        ElMessage.error('头像URL获取失败')
      }
    } else {
      ElMessage.error(response.message || '头像上传失败')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败，请重试')
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
      ElMessage.error('用户信息不存在，请重新登录')
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
      
      ElMessage.success('个人资料保存成功！')
      
      // 跳转到首页
      router.push('/')
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.message || '保存失败，请重试')
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
  ElMessage.error('头像加载失败，已切换到默认头像')
}
</script>

<style scoped lang="scss">
.profile-setup-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
  padding: 20px;
}

.profile-setup-card {
  background: none;
  border-radius: 0;
  box-shadow: none;
  padding: 40px;
  width: 100%;
  max-width: 500px;
}

.profile-setup-header {
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

.profile-setup-form {
  .el-form-item {
    margin-bottom: 20px;
  }
  
  .el-input,
  .el-textarea {
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

.avatar-upload {
  text-align: center;
  
  .avatar-uploader {
    display: inline-block;
    margin-bottom: 10px;
  }
  
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 100px;
    height: 100px;
    line-height: 100px;
    text-align: center;
    border: 1px dashed var(--color-border);
    border-radius: 50%;
    cursor: pointer;
    
    &:hover {
      border-color: var(--color-primary);
      color: var(--color-primary);
    }
  }
  
  .avatar {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    object-fit: cover;
  }
  
  .upload-tip {
    color: var(--color-text);
    font-size: 12px;
    margin: 0;
  }
}

.save-button {
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

// 响应式设计
@media (max-width: 480px) {
  .profile-setup-card {
    padding: 30px 20px;
  }
  
  .profile-setup-header h2 {
    font-size: 20px;
  }
  
  .profile-setup-form {
    .el-form-item__label {
      width: 60px !important;
    }
  }
}

.theme-setting {
  text-align: center;
  margin-top: 16px;
  
  span {
    color: #666;
    font-size: 14px;
    margin-right: 10px;
  }
}
</style> 