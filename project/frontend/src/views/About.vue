<template>
  <div class="about-page">
    <!-- 返回按钮 -->
    <div class="back-button" @click="goBack">
      <el-icon><ArrowLeft /></el-icon>
      <span>返回</span>
    </div>

    <!-- 主要内容 -->
    <div class="about-content">
      <!-- 头部信息 -->
      <div class="about-header">
        <div class="app-logo">
          <el-icon><Compass /></el-icon>
        </div>
        <h1>我的伊甸园</h1>
        <p class="app-description">AI情感陪伴与成长社区</p>
      </div>

      <!-- 应用简介 -->
      <div class="about-card">
        <div class="card-content">
          <p>「我的伊甸园」是一款基于AI的情感陪伴与成长社区，致力于为用户提供温暖、智能的互动体验。通过个性化的AI天使，为每位用户创造专属的陪伴空间。</p>
        </div>
      </div>

      <!-- 版本信息 -->
      <div class="about-card">
        <h3>版本信息</h3>
        <div class="card-content">
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">版本</span>
              <span class="info-value">v1.1.0</span>
            </div>
            <div class="info-item">
              <span class="info-label">作者</span>
              <span class="info-value">Jinni</span>
            </div>
            <div class="info-item">
              <span class="info-label">更新时间</span>
              <span class="info-value">2025-07-24</span>
            </div>
            <div class="info-item">
              <span class="info-label">开源协议</span>
              <span class="info-value">MIT</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 核心功能 -->
      <div class="about-card">
        <h3>核心功能</h3>
        <div class="card-content">
          <div class="features-list">
            <div class="feature-item">
              <el-icon class="feature-icon"><ChatDotRound /></el-icon>
              <div class="feature-text">
                <h4>智能对话</h4>
                <p>与AI天使进行自然流畅的对话交流</p>
              </div>
            </div>
            <div class="feature-item">
              <el-icon class="feature-icon"><User /></el-icon>
              <div class="feature-text">
                <h4>个性化陪伴</h4>
                <p>根据您的喜好定制专属AI伙伴</p>
              </div>
            </div>
            <div class="feature-item">
              <el-icon class="feature-icon"><Star /></el-icon>
              <div class="feature-text">
                <h4>情感支持</h4>
                <p>提供温暖贴心的情感陪伴服务</p>
              </div>
            </div>
            <div class="feature-item">
              <el-icon class="feature-icon"><TrendCharts /></el-icon>
              <div class="feature-text">
                <h4>成长记录</h4>
                <p>记录并分析您的成长轨迹</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 联系信息 -->
      <div class="about-card">
        <h3>联系我们</h3>
        <div class="card-content">
          <div class="contact-list">
            <div class="contact-item">
              <el-icon><Message /></el-icon>
              <span>jinni_1976@hotmail.com</span>
            </div>
            <div class="contact-item">
              <el-icon><Link /></el-icon>
              <a href="https://github.com/jinni/myeden" target="_blank">GitHub 项目地址</a>
            </div>
          </div>
        </div>
      </div>

      <!-- 版权信息 -->
      <div class="about-footer">
        <p>© 2025 Jinni. 保留所有权利。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { 
  ArrowLeft, 
  Compass, 
  ChatDotRound, 
  User, 
  Star, 
  TrendCharts, 
  Message, 
  Link 
} from '@element-plus/icons-vue'

/**
 * About 页面
 * 展示应用基本信息、功能特色、作者信息等
 * 
 * @author Jinni
 * @version 1.0.9
 */

const router = useRouter()

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.about-page {
  min-height: 100vh;
  background: var(--color-bg);
  padding: 20px;
  color: var(--color-text);
}

.back-button {
  position: fixed;
  top: 80px;
  left: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--color-text);
  font-weight: 500;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.back-button:hover {
  background: var(--color-primary);
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(34, 211, 107, 0.3);
}

.about-content {
  max-width: 800px;
  margin: 0 auto;
  padding-top: 40px;
}

/* 头部区域 */
.about-header {
  text-align: center;
  margin-bottom: 32px;
}

.app-logo {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  background: var(--color-primary);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(34, 211, 107, 0.3);
}

.app-logo .el-icon {
  font-size: 32px;
  color: white;
}

.about-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--color-text);
  margin: 0 0 8px 0;
}

.app-description {
  font-size: 1.1rem;
  color: var(--color-text);
  opacity: 0.7;
  margin: 0;
}

/* 卡片样式 */
.about-card {
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.about-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.about-card h3 {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
  padding: 20px 24px 0 24px;
}

.card-content {
  padding: 20px 24px 24px 24px;
}

.about-card:first-of-type .card-content {
  padding: 24px;
}

.card-content p {
  font-size: 1rem;
  line-height: 1.6;
  color: var(--color-text);
  opacity: 0.8;
  margin: 0;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 0.9rem;
  color: var(--color-text);
  opacity: 0.6;
  font-weight: 500;
}

.info-value {
  font-size: 1rem;
  color: var(--color-primary);
  font-weight: 600;
}

/* 功能列表 */
.features-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  background: rgba(34, 211, 107, 0.05);
  border: 1px solid rgba(34, 211, 107, 0.1);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(34, 211, 107, 0.08);
  border-color: rgba(34, 211, 107, 0.2);
  transform: translateY(-1px);
}

.feature-icon {
  font-size: 1.5rem;
  color: var(--color-primary);
  margin-top: 2px;
  flex-shrink: 0;
}

.feature-text h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 4px 0;
}

.feature-text p {
  font-size: 0.9rem;
  color: var(--color-text);
  opacity: 0.7;
  margin: 0;
  line-height: 1.4;
}

/* 联系列表 */
.contact-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(34, 211, 107, 0.05);
  border: 1px solid rgba(34, 211, 107, 0.1);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.contact-item:hover {
  background: rgba(34, 211, 107, 0.08);
  border-color: rgba(34, 211, 107, 0.2);
}

.contact-item .el-icon {
  font-size: 1.1rem;
  color: var(--color-primary);
}

.contact-item a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.contact-item a:hover {
  text-decoration: underline;
}

/* 底部版权 */
.about-footer {
  text-align: center;
  padding: 24px 0;
  border-top: 1px solid var(--color-border);
  margin-top: 16px;
}

.about-footer p {
  font-size: 0.9rem;
  color: var(--color-text);
  opacity: 0.6;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .about-page {
    padding: 10px;
  }
  
  .back-button {
    top: 70px;
    left: 10px;
    padding: 6px 12px;
    font-size: 14px;
  }
  
  .about-content {
    padding-top: 15px;
  }
  
  .about-header {
    margin-bottom: 20px;
  }
  
  .app-logo {
    width: 48px;
    height: 48px;
    margin-bottom: 12px;
  }
  
  .app-logo .el-icon {
    font-size: 24px;
  }
  
  .about-header h1 {
    font-size: 1.8rem;
    margin-bottom: 6px;
  }
  
  .app-description {
    font-size: 1rem;
  }
  
  .about-card {
    margin-bottom: 16px;
    border-radius: 12px;
  }
  
  .about-card h3 {
    font-size: 1.1rem;
    padding: 16px 16px 0 16px;
  }
  
  .card-content {
    padding: 12px 16px 16px 16px;
  }
  
  .about-card:first-of-type .card-content {
    padding: 16px;
  }
  
  .card-content p {
    font-size: 0.95rem;
    line-height: 1.5;
  }
  
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .info-item {
    gap: 2px;
  }
  
  .info-label {
    font-size: 0.8rem;
  }
  
  .info-value {
    font-size: 0.9rem;
  }
  
  .features-list {
    gap: 12px;
  }
  
  .feature-item {
    padding: 12px;
    border-radius: 8px;
  }
  
  .feature-icon {
    font-size: 1.3rem;
  }
  
  .feature-text h4 {
    font-size: 1rem;
    margin-bottom: 2px;
  }
  
  .feature-text p {
    font-size: 0.85rem;
    line-height: 1.3;
  }
  
  .contact-list {
    gap: 10px;
  }
  
  .contact-item {
    padding: 10px 12px;
    font-size: 0.9rem;
  }
  
  .contact-item .el-icon {
    font-size: 1rem;
  }
  
  .about-footer {
    padding: 16px 0;
    margin-top: 12px;
  }
  
  .about-footer p {
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .about-page {
    padding: 8px;
  }
  
  .back-button {
    top: 65px;
    left: 8px;
    padding: 5px 10px;
    font-size: 13px;
  }
  
  .about-content {
    padding-top: 10px;
  }
  
  .about-header {
    margin-bottom: 16px;
  }
  
  .app-logo {
    width: 40px;
    height: 40px;
    margin-bottom: 10px;
    border-radius: 12px;
  }
  
  .app-logo .el-icon {
    font-size: 20px;
  }
  
  .about-header h1 {
    font-size: 1.6rem;
    margin-bottom: 4px;
  }
  
  .app-description {
    font-size: 0.9rem;
  }
  
  .about-card {
    margin-bottom: 12px;
    border-radius: 10px;
  }
  
  .about-card h3 {
    font-size: 1rem;
    padding: 12px 12px 0 12px;
  }
  
  .card-content {
    padding: 10px 12px 12px 12px;
  }
  
  .about-card:first-of-type .card-content {
    padding: 12px;
  }
  
  .card-content p {
    font-size: 0.9rem;
    line-height: 1.4;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .info-item {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    border-bottom: 1px solid var(--color-border);
  }
  
  .info-item:last-child {
    border-bottom: none;
  }
  
  .info-label {
    font-size: 0.85rem;
  }
  
  .info-value {
    font-size: 0.9rem;
  }
  
  .features-list {
    gap: 10px;
  }
  
  .feature-item {
    padding: 10px;
    border-radius: 6px;
  }
  
  .feature-icon {
    font-size: 1.2rem;
    margin-bottom: 6px;
  }
  
  .feature-text {
    text-align: center;
  }
  
  .feature-text h4 {
    font-size: 0.95rem;
    margin-bottom: 2px;
  }
  
  .feature-text p {
    font-size: 0.8rem;
    line-height: 1.2;
  }
  
  .contact-list {
    gap: 8px;
  }
  
  .contact-item {
    padding: 8px 10px;
    font-size: 0.85rem;
    border-radius: 6px;
  }
  
  .contact-item .el-icon {
    font-size: 0.9rem;
  }
  
  .about-footer {
    padding: 12px 0;
    margin-top: 8px;
  }
  
  .about-footer p {
    font-size: 0.75rem;
  }
}
</style> 