<template>
  <div class="robot-editor-wizard" :class="{ 'dark-mode': isDarkMode }">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="floating-orb orb-1"></div>
      <div class="floating-orb orb-2"></div>
      <div class="floating-orb orb-3"></div>
      <div class="gradient-overlay"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 向导头部 -->
      <div class="wizard-header">
        <div class="header-content">
          <div class="header-left">
            <div class="back-link" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              <span class="back-text">返回</span>
            </div>
            <div class="wizard-title">
              <h1>{{ isEdit ? '编辑天使' : '创建天使' }}</h1>
              <p class="wizard-subtitle">让我们一步步完善</p>
            </div>
          </div>
          <div class="header-right">
            <div 
              class="save-action" 
              :class="{ 'loading': saving }"
              @click="saveRobot"
            >
              <el-icon v-if="!saving"><Check /></el-icon>
              <el-icon v-else class="loading-icon"><Loading /></el-icon>
              <span class="save-text">{{ isEdit ? '保存修改' : '创建天使' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 向导步骤指示器 -->
      <div class="wizard-steps">
        <div 
          v-for="(step, index) in steps" 
          :key="index"
          class="step-item"
          :class="{ 
            'active': currentStep === index,
            'completed': currentStep > index
          }"
          @click="goToStep(index)"
        >
          <div class="step-number">
            <el-icon v-if="currentStep > index"><Check /></el-icon>
            <span v-else>{{ index + 1 }}</span>
          </div>
          <div class="step-info">
            <span class="step-title">{{ step.title }}</span>
            <span class="step-desc">{{ step.description }}</span>
          </div>
        </div>
      </div>

      <!-- 向导内容区域 -->
      <div class="wizard-content">
        <!-- 步骤1：基础信息 -->
        <div v-show="currentStep === 0" class="step-content">
          <div class="step-header">
            <h2>基础信息</h2>
            <p>设置天使的基本信息和外观</p>
          </div>
          
          <div class="form-section">
            <!-- 头像上传 -->
            <div class="avatar-section">
              <h3>天使头像</h3>
              <el-form-item label="头像" prop="avatar">
                <div class="avatar-upload">
                  <el-upload
                    ref="avatarUpload"
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    :http-request="handleAvatarUpload"
                    accept="image/jpeg,image/png,image/gif"
                    class="avatar-uploader"
                  >
                    <div class="avatar-container">
                      <img v-if="avatarPreviewUrl" :src="avatarPreviewUrl" class="avatar" @error="handleAvatarError" />
                      <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                      <div class="avatar-overlay">
                        <el-icon><Camera /></el-icon>
                        <span>更换头像</span>
                      </div>
                    </div>
                  </el-upload>
                  <div class="avatar-controls">
                    <el-button 
                      v-if="avatarPreviewUrl" 
                      type="danger" 
                      size="large"
                      @click="removeAvatar"
                    >
                      <el-icon><Delete /></el-icon>
                      删除头像
                    </el-button>
                  </div>
                  <p class="upload-tip">点击上传头像，支持JPG、PNG格式</p>
                </div>
              </el-form-item>
            </div>

            <!-- 基本信息 -->
            <div class="basic-info-section">
              <h3>基本信息</h3>
              <div class="form-fields">
                <el-form-item label="天使名称" prop="name">
                  <el-input 
                    v-model="robotData.name" 
                    placeholder="为你的天使起一个名字"
                    size="large"
                  />
                </el-form-item>
                
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="robotData.gender" size="large">
                    <el-radio-button label="female">女</el-radio-button>
                    <el-radio-button label="male">男</el-radio-button>
                    <el-radio-button label="other">其他</el-radio-button>
                  </el-radio-group>
                </el-form-item>
                
                <el-form-item label="年龄" prop="age">
                  <div class="age-slider-container">
                    <div class="age-display">{{ robotData.age }}岁</div>
                    <el-slider
                      v-model="robotData.age"
                      :min="1"
                      :max="100"
                      :marks="{1: '1岁', 25: '25岁', 50: '50岁', 75: '75岁', 100: '100岁'}"
                      show-stops
                      size="large"
                    />
                  </div>
                </el-form-item>
                
                <el-form-item label="一句话简介" prop="introduction">
                  <el-input 
                    v-model="robotData.introduction" 
                    type="textarea" 
                    :rows="3"
                    placeholder="用一句话描述这个天使"
                    size="large"
                  />
                </el-form-item>
              </div>
            </div>
          </div>
        </div>

        <!-- 步骤2：性格设定 -->
        <div v-show="currentStep === 1" class="step-content">
          <div class="step-header">
            <h2>性格设定</h2>
            <p>定义天使的性格特征和行为模式</p>
          </div>
          
          <div class="form-section">
            <div class="personality-section">
              <h3>性格描述</h3>
              <el-form-item prop="personality">
                <el-input 
                  v-model="robotData.personality" 
                  type="textarea" 
                  :rows="6"
                  placeholder="详细描述天使的性格特点、行为习惯、说话方式等"
                  size="large"
                />
              </el-form-item>
            </div>

            <div class="traits-section">
              <h3>性格特征</h3>
              <div class="traits-container">
                <el-tag
                  v-for="(trait, index) in robotData.traits"
                  :key="index"
                  closable
                  @close="removeTrait(index)"
                  class="trait-tag"
                  size="large"
                >
                  {{ trait }}
                </el-tag>
                <el-input
                  v-if="showTraitInput"
                  ref="traitInput"
                  v-model="newTrait"
                  size="large"
                  @keyup.enter="addTrait"
                  @blur="addTrait"
                  placeholder="输入特征后按回车"
                  style="width: 200px"
                />
                <el-button v-else size="large" @click="showTraitInput = true" type="dashed">
                  <el-icon><Plus /></el-icon>
                  添加特征
                </el-button>
              </div>
            </div>

            <div class="interests-section">
              <h3>兴趣爱好</h3>
              <div class="interests-container">
                <el-tag
                  v-for="(interest, index) in robotData.interests"
                  :key="index"
                  closable
                  @close="removeInterest(index)"
                  class="interest-tag"
                  size="large"
                >
                  {{ interest }}
                </el-tag>
                <el-input
                  v-if="showInterestInput"
                  ref="interestInput"
                  v-model="newInterest"
                  size="large"
                  @keyup.enter="addInterest"
                  @blur="addInterest"
                  placeholder="输入兴趣后按回车"
                  style="width: 200px"
                />
                <el-button v-else size="large" @click="showInterestInput = true" type="dashed">
                  <el-icon><Plus /></el-icon>
                  添加兴趣
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 步骤3：个人信息 -->
        <div v-show="currentStep === 2" class="step-content">
          <div class="step-header">
            <h2>个人信息</h2>
            <p>完善天使的个人背景和详细信息</p>
          </div>
          
          <div class="form-section">
            <div class="personal-info-section">
              <h3>基本信息</h3>
              <div class="form-fields">
                <el-form-item label="MBTI" prop="mbti">
                  <el-select v-model="robotData.mbti" placeholder="选择MBTI类型" size="large" style="width: 100%">
                    <el-option label="INTJ" value="INTJ" />
                    <el-option label="INTP" value="INTP" />
                    <el-option label="ENTJ" value="ENTJ" />
                    <el-option label="ENTP" value="ENTP" />
                    <el-option label="INFJ" value="INFJ" />
                    <el-option label="INFP" value="INFP" />
                    <el-option label="ENFJ" value="ENFJ" />
                    <el-option label="ENFP" value="ENFP" />
                    <el-option label="ISTJ" value="ISTJ" />
                    <el-option label="ISFJ" value="ISFJ" />
                    <el-option label="ESTJ" value="ESTJ" />
                    <el-option label="ESFJ" value="ESFJ" />
                    <el-option label="ISTP" value="ISTP" />
                    <el-option label="ISFP" value="ISFP" />
                    <el-option label="ESTP" value="ESTP" />
                    <el-option label="ESFP" value="ESFP" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="血型" prop="bloodType">
                  <el-select v-model="robotData.bloodType" placeholder="选择血型" size="large" style="width: 100%">
                    <el-option label="A型" value="A" />
                    <el-option label="B型" value="B" />
                    <el-option label="O型" value="O" />
                    <el-option label="AB型" value="AB" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="星座" prop="zodiac">
                  <el-select v-model="robotData.zodiac" placeholder="选择星座" size="large" style="width: 100%">
                    <el-option label="白羊座" value="白羊座" />
                    <el-option label="金牛座" value="金牛座" />
                    <el-option label="双子座" value="双子座" />
                    <el-option label="巨蟹座" value="巨蟹座" />
                    <el-option label="狮子座" value="狮子座" />
                    <el-option label="处女座" value="处女座" />
                    <el-option label="天秤座" value="天秤座" />
                    <el-option label="天蝎座" value="天蝎座" />
                    <el-option label="射手座" value="射手座" />
                    <el-option label="摩羯座" value="摩羯座" />
                    <el-option label="水瓶座" value="水瓶座" />
                    <el-option label="双鱼座" value="双鱼座" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="所在地" prop="location">
                  <el-input v-model="robotData.location" placeholder="所在城市" size="large" />
                </el-form-item>
                
                <el-form-item label="职业" prop="occupation">
                  <el-input v-model="robotData.occupation" placeholder="职业" size="large" />
                </el-form-item>
                
                <el-form-item label="学历" prop="education">
                  <el-select v-model="robotData.education" placeholder="选择学历" size="large" style="width: 100%">
                    <el-option label="高中" value="高中" />
                    <el-option label="大专" value="大专" />
                    <el-option label="本科" value="本科" />
                    <el-option label="硕士" value="硕士" />
                    <el-option label="博士" value="博士" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="感情状态" prop="relationship">
                  <el-select v-model="robotData.relationship" placeholder="选择感情状态" size="large" style="width: 100%">
                    <el-option label="单身" value="single" />
                    <el-option label="恋爱中" value="in_relationship" />
                    <el-option label="已婚" value="married" />
                    <el-option label="离异" value="divorced" />
                  </el-select>
                </el-form-item>
              </div>
            </div>

            <div class="background-section">
              <h3>背景故事</h3>
              <el-form-item prop="background">
                <el-input 
                  v-model="robotData.background" 
                  type="textarea" 
                  :rows="8"
                  placeholder="详细描述天使的背景故事、成长经历、重要事件等"
                  size="large"
                />
              </el-form-item>
              
              <h3>家庭背景</h3>
              <el-form-item prop="family">
                <el-input 
                  v-model="robotData.family" 
                  type="textarea" 
                  :rows="4"
                  placeholder="描述家庭背景、家庭成员等"
                  size="large"
                />
              </el-form-item>
            </div>
          </div>
        </div>

        <!-- 步骤4：行为设置 -->
        <div v-show="currentStep === 3" class="step-content">
          <div class="step-header">
            <h2>行为设置</h2>
            <p>配置天使的互动行为参数</p>
          </div>
          
          <div class="form-section">
            <div class="behavior-section">
              <h3>互动参数</h3>
              <div class="slider-group">
                <div class="slider-item">
                  <label>回复速度</label>
                  <el-slider
                    v-model="robotData.replySpeed"
                    :min="0"
                    :max="1"
                    :step="0.1"
                    :marks="{0: '慢', 0.5: '中等', 1: '快'}"
                    show-stops
                    size="large"
                  />
                </div>
                
                <div class="slider-item">
                  <label>回复频度</label>
                  <el-slider
                    v-model="robotData.replyFrequency"
                    :min="0"
                    :max="1"
                    :step="0.1"
                    :marks="{0: '低', 0.5: '中等', 1: '高'}"
                    show-stops
                    size="large"
                  />
                </div>
                
                <div class="slider-item">
                  <label>分享频度</label>
                  <el-slider
                    v-model="robotData.shareFrequency"
                    :min="0"
                    :max="1"
                    :step="0.1"
                    :marks="{0: '低', 0.5: '中等', 1: '高'}"
                    show-stops
                    size="large"
                  />
                </div>
              </div>
            </div>

            <div class="activation-section">
              <h3>激活状态</h3>
              <div class="activation-card">
                <div class="activation-info">
                  <el-icon size="24" :class="{ 'active': robotData.isActive }">
                    <component :is="robotData.isActive ? 'CircleCheck' : 'CircleClose'" />
                  </el-icon>
                  <div class="activation-text">
                    <h4>{{ robotData.isActive ? '天使已激活' : '天使未激活' }}</h4>
                    <p>{{ robotData.isActive ? '天使将开始活跃并与其他用户互动' : '天使暂时处于休眠状态' }}</p>
                  </div>
                </div>
                <el-switch 
                  v-model="robotData.isActive" 
                  size="large"
                  active-text="激活"
                  inactive-text="休眠"
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 向导底部导航 -->
      <div class="wizard-footer">
        <table class="footer-table">
          <tr>
            <td class="footer-left">
              <div 
                v-if="currentStep > 0" 
                class="nav-action prev-action"
                @click="prevStep"
              >
                <el-icon><ArrowLeft /></el-icon>
                <span class="nav-text">上一步</span>
              </div>
            </td>
            <td class="footer-right" style="text-align: right;">
              <div 
                v-if="currentStep < steps.length - 1" 
                class="nav-action next-action"
                @click="nextStep"
              >
                <span class="nav-text">下一步</span>
                <el-icon><ArrowRight /></el-icon>
              </div>
            </td>
          </tr>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  User, Upload, Delete, Plus, Check, ArrowLeft, ArrowRight,
  CircleCheck, CircleClose, Camera, Loading
} from '@element-plus/icons-vue'
import { 
  getRobotForEdit, 
  createRobot, 
  updateRobot 
} from '@/api/robotEditor'
import { uploadRobotAvatar } from '@/api/robotEditor'
import { buildAvatarUrl, generateRobotDefaultAvatar, handleRobotAvatarError } from '@/utils/avatar'
import { useConfigStore } from '@/stores/config'

// 路由相关
const route = useRoute()
const router = useRouter()

// 配置Store
const configStore = useConfigStore()

// 响应式数据
const robotForm = ref(null)
const saving = ref(false)
const showTraitInput = ref(false)
const showInterestInput = ref(false)
const newTrait = ref('')
const newInterest = ref('')
const avatarUpload = ref(null)
const avatarPreviewUrl = ref('')
const currentStep = ref(0)

// 向导步骤配置
const steps = [
  {
    title: '基础信息',
    description: '设置基本信息和头像'
  },
  {
    title: '性格设定',
    description: '定义性格特征和兴趣'
  },
  {
    title: '个人信息',
    description: '完善背景和详细信息'
  },
  {
    title: '行为设置',
    description: '配置互动参数'
  }
]

// 机器人数据
const robotData = reactive({
  name: '',
  avatar: '',
  gender: '',
  age: 25,
  introduction: '',
  personality: '',
  mbti: '',
  bloodType: '',
  zodiac: '',
  location: '',
  occupation: '',
  education: '',
  relationship: '',
  background: '',
  family: '',
  traits: [],
  interests: [],
  replySpeed: 0.5,
  replyFrequency: 0.5,
  shareFrequency: 0.5,
  isActive: true
})

// 计算属性
const isEdit = computed(() => !!route.params.robotId)
const isDarkMode = computed(() => configStore.isDarkMode)

// 方法
const goBack = () => {
  router.back()
}

const goToStep = (stepIndex) => {
  if (stepIndex <= currentStep.value) {
    currentStep.value = stepIndex
  }
}

const nextStep = () => {
  if (currentStep.value < steps.length - 1) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const addTrait = () => {
  if (newTrait.value.trim()) {
    robotData.traits.push(newTrait.value.trim())
    newTrait.value = ''
  }
  showTraitInput.value = false
}

const removeTrait = (index) => {
  robotData.traits.splice(index, 1)
}

const addInterest = () => {
  if (newInterest.value.trim()) {
    robotData.interests.push(newInterest.value.trim())
    newInterest.value = ''
  }
  showInterestInput.value = false
}

const removeInterest = (index) => {
  robotData.interests.splice(index, 1)
}

// 头像上传相关方法
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleAvatarUpload = async (options) => {
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    
    let response
    if (isEdit.value) {
      response = await uploadRobotAvatar(route.params.robotId, formData)
    } else {
      const tempRobotId = 'temp_' + Date.now()
      response = await uploadRobotAvatar(tempRobotId, formData)
    }
    
    if (response.code === 200) {
      // 从响应中获取头像URL
      const avatarUrl = response.data.avatarUrl || response.data
      robotData.avatar = avatarUrl
      avatarPreviewUrl.value = buildAvatarUrl(avatarUrl)
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(response.message || '头像上传失败')
    }
  } catch (error) {
    ElMessage.error('头像上传失败')
    console.error('头像上传失败:', error)
  }
}

const removeAvatar = () => {
  robotData.avatar = ''
  avatarPreviewUrl.value = ''
  ElMessage.success('头像已删除')
}

const handleAvatarError = (event) => {
  // 设置默认头像
  const defaultAvatar = generateRobotDefaultAvatar(robotData.name || 'Robot')
  event.target.src = defaultAvatar
  ElMessage.error('头像加载失败，已切换到默认头像')
}

const loadRobotData = async () => {
  if (!isEdit.value) return
  
  try {
    const response = await getRobotForEdit(route.params.robotId)
    if (response.code === 200 && response.data) {
      const robot = response.data
      Object.assign(robotData, {
        name: robot.name || '',
        avatar: robot.avatar || '',
        gender: robot.gender || '',
        age: robot.age || 25,
        introduction: robot.introduction || '',
        personality: robot.personality || '',
        mbti: robot.mbti || '',
        bloodType: robot.bloodType || '',
        zodiac: robot.zodiac || '',
        location: robot.location || '',
        occupation: robot.occupation || '',
        education: robot.education || '',
        relationship: robot.relationship || '',
        background: robot.background || '',
        family: robot.family || '',
        traits: robot.traits || [],
        interests: robot.interests || [],
        replySpeed: robot.replySpeed || 0.5,
        replyFrequency: robot.replyFrequency || 0.5,
        shareFrequency: robot.shareFrequency || 0.5,
        isActive: robot.isActive !== undefined ? robot.isActive : true
      })
      
      // 设置头像预览
      if (robot.avatar) {
        avatarPreviewUrl.value = buildAvatarUrl(robot.avatar)
      }
    }
  } catch (error) {
    ElMessage.error('加载机器人数据失败')
    console.error('加载机器人数据失败:', error)
  }
}

const saveRobot = async () => {
  try {
    saving.value = true
    
    const data = { ...robotData }
    
    if (isEdit.value) {
      await updateRobot(route.params.robotId, data)
      ElMessage.success('保存成功')
    } else {
      await createRobot(data)
      ElMessage.success('创建成功')
    }
    
    router.push('/world')
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error(isEdit.value ? '保存失败' : '创建失败')
    }
    console.error('保存机器人失败:', error)
  } finally {
    saving.value = false
  }
}

// 生命周期
onMounted(() => {
  loadRobotData()
})
</script>

<style scoped>
.robot-editor-wizard {
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
  margin: 80 auto;
  padding: 20px;
  padding-top: 100px; /* 避免与标题栏重叠 */
}

/* 向导头部 */
.wizard-header {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  margin-bottom: 24px;
  padding: 20px;
  transition: all 0.3s ease;
}

.dark-mode .wizard-header {
  background: rgba(255, 255, 255, 0.02);
  border-color: rgba(255, 255, 255, 0.05);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.back-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text);
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  flex-shrink: 0;
  font-size: 14px;
}

.back-link:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateX(-2px);
  color: var(--color-primary);
}

.back-link .el-icon {
  font-size: 16px;
}

.back-text {
  font-size: 14px;
}

.wizard-title {
  flex: 1;
  min-width: 0;
}

.wizard-title h1 {
  margin: 0;
  color: var(--color-text);
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-primary), #4ade80);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.wizard-subtitle {
  margin: 4px 0 0 0;
  color: var(--color-text);
  opacity: 0.7;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.save-action {
  display: flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, var(--color-primary), #4ade80);
  color: white;
  font-weight: 600;
  padding: 10px 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(34, 211, 107, 0.3);
  white-space: nowrap;
  font-size: 14px;
}

.save-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(34, 211, 107, 0.4);
}

.save-action:active {
  transform: translateY(0);
}

.save-action.loading {
  opacity: 0.8;
  cursor: not-allowed;
}

.save-action .el-icon {
  font-size: 16px;
}

.save-text {
  font-size: 14px;
}

.loading-icon {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 向导步骤 */
.wizard-steps {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;
  gap: 8px;
  flex-wrap: wrap;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 160px;
  max-width: 180px;
}

.dark-mode .step-item {
  background: rgba(255, 255, 255, 0.02);
  border-color: rgba(255, 255, 255, 0.05);
}

.step-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.dark-mode .step-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.step-item.active {
  background: rgba(34, 211, 107, 0.1);
  border-color: var(--color-primary);
  box-shadow: 0 4px 15px rgba(34, 211, 107, 0.3);
}

.step-item.completed {
  background: rgba(34, 211, 107, 0.05);
  border-color: rgba(34, 211, 107, 0.3);
}

.step-number {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text);
  font-weight: 600;
  font-size: 12px;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.dark-mode .step-number {
  background: rgba(255, 255, 255, 0.1);
}

.step-item.active .step-number {
  background: var(--color-primary);
  color: white;
}

.step-item.completed .step-number {
  background: var(--color-primary);
  color: white;
}

.step-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.step-title {
  color: var(--color-text);
  font-weight: 600;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.step-desc {
  color: var(--color-text);
  opacity: 0.7;
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 向导内容 */
.wizard-content {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
  transition: all 0.3s ease;
}

.dark-mode .wizard-content {
  background: rgba(255, 255, 255, 0.02);
  border-color: rgba(255, 255, 255, 0.05);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.step-content {
  min-height: 500px;
}

.step-header {
  text-align: center;
  margin-bottom: 40px;
}

.step-header h2 {
  color: var(--color-text);
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
  background: linear-gradient(135deg, var(--color-primary), #4ade80);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.step-header p {
  color: var(--color-text);
  opacity: 0.7;
  font-size: 16px;
  margin: 0;
}

.form-section {
  margin-bottom: 40px;
}

.avatar-section,
.basic-info-section,
.personality-section,
.traits-section,
.interests-section,
.personal-info-section,
.background-section,
.behavior-section,
.activation-section {
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  transition: all 0.3s ease;
}

.dark-mode .avatar-section,
.dark-mode .basic-info-section,
.dark-mode .personality-section,
.dark-mode .traits-section,
.dark-mode .interests-section,
.dark-mode .personal-info-section,
.dark-mode .background-section,
.dark-mode .behavior-section,
.dark-mode .activation-section {
  background: rgba(255, 255, 255, 0.02);
  border-color: rgba(255, 255, 255, 0.05);
}

.avatar-section h3,
.basic-info-section h3,
.personality-section h3,
.traits-section h3,
.interests-section h3,
.personal-info-section h3,
.background-section h3,
.behavior-section h3,
.activation-section h3 {
  color: var(--color-text);
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  padding-bottom: 8px;
  border-bottom: 2px solid var(--color-primary);
}

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

.avatar-preview {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid var(--color-border);
  background: var(--color-card);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.dark-mode .avatar-preview {
  border-color: rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.02);
}

.avatar-image {
  width: 100%;
  height: 100%;
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: var(--color-text);
  opacity: 0.5;
  gap: 8px;
}

.avatar-placeholder span {
  font-size: 12px;
}

.avatar-controls {
  display: flex;
  gap: 12px;
}

.form-fields {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.traits-container,
.interests-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.trait-tag,
.interest-tag {
  background: linear-gradient(135deg, var(--color-primary), #4ade80);
  color: white;
  border: none;
  transition: all 0.3s ease;
}

.trait-tag:hover,
.interest-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(34, 211, 107, 0.3);
}

.slider-group {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.slider-item {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.slider-item label {
  font-weight: 600;
  color: var(--color-text);
  font-size: 14px;
}

.age-slider-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.age-display {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-primary);
  text-align: center;
  padding: 8px 12px;
  background: rgba(34, 211, 107, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(34, 211, 107, 0.2);
}

.activation-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.dark-mode .activation-card {
  background: rgba(255, 255, 255, 0.02);
  border-color: rgba(255, 255, 255, 0.05);
}

.activation-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.activation-info .el-icon {
  color: #dc3545;
  transition: all 0.3s ease;
}

.activation-info .el-icon.active {
  color: var(--color-primary);
}

.activation-text h4 {
  margin: 0 0 4px 0;
  color: var(--color-text);
  font-size: 16px;
  font-weight: 600;
}

.activation-text p {
  margin: 0;
  color: var(--color-text);
  opacity: 0.7;
  font-size: 14px;
}

/* 向导底部 */
.wizard-footer {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 24px;
  transition: all 0.3s ease;
}

.dark-mode .wizard-footer {
  background: rgba(255, 255, 255, 0.02);
  border-color: rgba(255, 255, 255, 0.05);
}

.footer-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  table-layout: fixed;
}

.footer-table td {
  vertical-align: middle;
  padding: 0;
  width: 50%;
}

.footer-left, 
.footer-right {
  width: 100%;
}

.nav-action {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
  font-size: 14px;
  white-space: nowrap;
  margin: 8px 0;
}

.prev-action {
  background: rgba(255, 255, 255, 0.1);
  color: var(--color-text);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.prev-action:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateX(-2px);
}

.next-action {
  background: linear-gradient(135deg, var(--color-primary), #4ade80);
  color: white;
  border: none;
  box-shadow: 0 4px 15px rgba(34, 211, 107, 0.3);
}

.next-action:hover {
  transform: translateX(2px);
  box-shadow: 0 6px 20px rgba(34, 211, 107, 0.4);
}

.nav-action:active {
  transform: translateY(1px);
}

.nav-action .el-icon {
  font-size: 16px;
}

.nav-text {
  font-size: 14px;
}

@media (max-width: 768px) {
  .wizard-footer {
    padding: 12px;
  }
  .nav-action {
    padding: 8px 12px;
    font-size: 13px;
    gap: 6px;
  }
  .nav-action .el-icon {
    font-size: 14px;
  }
  .nav-text {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .wizard-footer {
    padding: 8px;
  }
  .nav-action {
    padding: 6px 8px;
    font-size: 12px;
    gap: 4px;
  }
  .nav-action .el-icon {
    font-size: 12px;
  }
  .nav-text {
    font-size: 12px;
  }
}

/* 表单字段布局优化 */
.form-fields .el-form-item {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 20px;
  align-items: center;
  margin-bottom: 0;
}

.form-fields .el-form-item__label {
  text-align: right;
  padding-right: 12px;
  line-height: 32px;
  margin-bottom: 0;
  font-weight: 600;
  color: var(--color-text);
}

.form-fields .el-form-item__content {
  margin-left: 0 !important;
}

/* Element Plus 组件样式覆盖 */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--color-text);
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
  background-color: var(--color-bg);
  border-color: var(--color-border);
}

:deep(.el-input__wrapper:hover) {
  border-color: var(--color-primary);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 1px var(--color-primary);
}

:deep(.el-input__inner) {
  color: var(--color-text);
}

:deep(.el-textarea__inner) {
  color: var(--color-text);
  background-color: var(--color-bg);
  border-color: var(--color-border);
}

:deep(.el-textarea__inner:hover) {
  border-color: var(--color-primary);
}

:deep(.el-textarea__inner:focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 1px var(--color-primary);
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-select .el-input__wrapper) {
  background-color: var(--color-bg);
}

:deep(.el-select-dropdown) {
  background-color: var(--color-bg);
  border-color: var(--color-border);
}

:deep(.el-select-dropdown__item) {
  color: var(--color-text);
}

:deep(.el-select-dropdown__item:hover) {
  background-color: rgba(34, 211, 107, 0.1);
}

:deep(.el-select-dropdown__item.selected) {
  background-color: var(--color-primary);
  color: white;
}

:deep(.el-slider) {
  width: 100%;
}

:deep(.el-slider__runway) {
  background-color: var(--color-border);
}

:deep(.el-slider__bar) {
  background-color: var(--color-primary);
}

:deep(.el-slider__button) {
  border-color: var(--color-primary);
}

:deep(.el-radio-button__inner) {
  border-color: var(--color-border);
  color: var(--color-text);
  background-color: var(--color-bg);
}

:deep(.el-radio-button__inner:hover) {
  color: var(--color-primary);
  border-color: var(--color-primary);
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: var(--color-primary);
  border-color: var(--color-primary);
  color: white;
}

:deep(.el-switch__core) {
  border-color: var(--color-border);
  background-color: var(--color-border);
}

:deep(.el-switch.is-checked .el-switch__core) {
  background-color: var(--color-primary);
  border-color: var(--color-primary);
}

:deep(.el-button) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, var(--color-primary), #4ade80);
  border: none;
  box-shadow: 0 4px 15px rgba(34, 211, 107, 0.3);
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(34, 211, 107, 0.4);
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #f56565, #e53e3e);
  border: none;
  box-shadow: 0 4px 15px rgba(245, 101, 101, 0.3);
}

:deep(.el-button--danger:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(245, 101, 101, 0.4);
}

:deep(.el-tag) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-tag:hover) {
  transform: translateY(-2px);
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__wrapper) {
  background-color: var(--color-bg);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-content {
    padding: 16px;
  }
  
  .wizard-steps {
    flex-direction: column;
    gap: 8px;
  }
  
  .step-item {
    min-width: auto;
    width: 100%;
  }
}

@media (max-width: 768px) {
  .main-content {
    margin-top: 80px;
    padding: 12px;
  }
  
  .wizard-header {
    padding: 16px;
  }
  
  .header-content {
    gap: 12px;
  }
  
  .header-left {
    gap: 12px;
  }
  
  .back-link {
    padding: 6px 10px;
    font-size: 13px;
  }
  
  .back-link .el-icon {
    font-size: 14px;
  }
  
  .back-text {
    font-size: 13px;
  }
  
  .wizard-title h1 {
    font-size: 20px;
  }
  
  .wizard-subtitle {
    font-size: 12px;
  }
  
  .save-action {
    padding: 8px 16px;
    font-size: 13px;
  }
  
  .save-action .el-icon {
    font-size: 14px;
  }
  
  .save-text {
    font-size: 13px;
  }
  
  .wizard-steps {
    flex-direction: column;
    gap: 8px;
  }
  
  .step-item {
    padding: 12px 16px;
    min-width: auto;
    max-width: none;
  }
  
  .wizard-content {
    padding: 20px;
  }
  
  .step-header h2 {
    font-size: 24px;
  }
  
  .avatar-upload {
    .avatar-uploader-icon {
      width: 80px;
      height: 80px;
      line-height: 80px;
      font-size: 24px;
    }
    
    .avatar {
      width: 80px;
      height: 80px;
    }
    
    .avatar-overlay {
      .el-icon {
        font-size: 16px;
      }
      
      span {
        font-size: 10px;
      }
    }
  }
  
  .avatar-controls {
    flex-direction: column;
    width: 100%;
  }
  
  /* 移动端表单字段改为单列布局 */
  .form-fields .el-form-item {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .form-fields .el-form-item__label {
    text-align: left;
    padding-right: 0;
    padding-bottom: 4px;
  }
  
  .age-display {
    font-size: 14px;
    padding: 6px 10px;
  }
  
  .traits-container,
  .interests-container {
    justify-content: center;
  }
  
  .activation-card {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .footer-content {
    flex-direction: column;
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 8px;
  }
  
  .wizard-header {
    padding: 12px;
  }
  
  .header-content {
    gap: 8px;
  }
  
  .header-left {
    gap: 8px;
  }
  
  .back-link {
    padding: 4px 8px;
    font-size: 12px;
  }
  
  .back-link .el-icon {
    font-size: 12px;
  }
  
  .back-text {
    font-size: 12px;
  }
  
  .wizard-title h1 {
    font-size: 18px;
  }
  
  .wizard-subtitle {
    font-size: 11px;
  }
  
  .save-action {
    padding: 6px 12px;
    font-size: 12px;
  }
  
  .save-action .el-icon {
    font-size: 12px;
  }
  
  .save-text {
    font-size: 12px;
  }
  
  .wizard-content {
    padding: 16px;
  }
  
  .step-header h2 {
    font-size: 20px;
  }
  
  .step-header p {
    font-size: 14px;
  }
  
  .avatar-section,
  .basic-info-section,
  .personality-section,
  .traits-section,
  .interests-section,
  .personal-info-section,
  .background-section,
  .behavior-section,
  .activation-section {
    padding: 16px;
  }
  
  .avatar-upload {
    .avatar-uploader-icon {
      width: 70px;
      height: 70px;
      line-height: 70px;
      font-size: 20px;
    }
    
    .avatar {
      width: 70px;
      height: 70px;
    }
    
    .avatar-overlay {
      .el-icon {
        font-size: 14px;
      }
      
      span {
        font-size: 9px;
      }
    }
  }
  
  .avatar-controls {
    flex-direction: column;
    width: 100%;
  }
  
  /* 小屏幕进一步优化表单布局 */
  .form-fields .el-form-item {
    grid-template-columns: 1fr;
    gap: 6px;
  }
  
  .form-fields .el-form-item__label {
    font-size: 14px;
    line-height: 28px;
  }
  
  .age-display {
    font-size: 12px;
    padding: 4px 8px;
  }
  
  .traits-container,
  .interests-container {
    justify-content: center;
  }
  
  .activation-card {
    padding: 16px;
  }
  
  .wizard-footer {
    padding: 16px;
  }
  
  /* 小屏幕步骤指示器优化 */
  .step-item {
    padding: 10px 12px;
  }
  
  .step-number {
    width: 24px;
    height: 24px;
    font-size: 11px;
  }
  
  .step-title {
    font-size: 12px;
  }
  
  .step-desc {
    font-size: 10px;
  }
}
</style> 