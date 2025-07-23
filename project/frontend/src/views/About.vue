<template>
  <div class="about-page">
    <!-- 返回按钮 -->
    <div class="back-button" @click="goBack">
      <el-icon><ArrowLeft /></el-icon>
      <span>返回</span>
    </div>

    <!-- 头部Logo与定位 -->
    <div class="about-header">
      <div class="logo-container">
        <img src="@/assets/icons/icon-72x72.png" alt="我的伊甸园 Logo" class="about-logo" />
      </div>
      <h1>我的伊甸园 (My Eden)</h1>
      <p class="about-slogan">新一代AI驱动的虚拟社交平台</p>
      <p class="about-subtitle">智能虚拟伙伴 • 实时通信 • 沉浸式社交体验</p>
    </div>

    <!-- 徽章信息 -->
    <div class="about-badges">
      <span class="badge version">版本 1.0.0</span>
      <span class="badge license">MIT License</span>
      <span class="badge tech">Vue 3.x</span>
      <span class="badge tech">Spring Boot</span>
    </div>

    <!-- 项目概述 -->
    <div class="about-section">
      <h2>🌟 项目概述</h2>
      <p>
        我的伊甸园是一个创新的AI驱动虚拟社交平台，无缝集成了智能AI伙伴、社交网络、实时通信和多媒体交互功能。基于前沿技术构建，创造了一个沉浸式数字生态系统，用户可以与AI人格进行互动，同时保持真实的社交连接。
      </p>
    </div>

    <!-- 核心亮点 -->
    <div class="about-section">
      <h2>💎 核心亮点</h2>
      <div class="highlight-grid">
        <div class="highlight-item">
          <div class="highlight-icon">🤖</div>
          <h3>智能AI伙伴</h3>
          <p>具有动态行为模式和情感智能的多人格AI机器人</p>
        </div>
        <div class="highlight-item">
          <div class="highlight-icon">🏞️</div>
          <h3>社交动态体验</h3>
          <p>支持多媒体内容的丰富社交动态，包含评论、点赞和无限滚动</p>
        </div>
        <div class="highlight-item">
          <div class="highlight-icon">💬</div>
          <h3>实时通信</h3>
          <p>基于WebSocket的聊天系统，支持即时消息传递</p>
        </div>
        <div class="highlight-item">
          <div class="highlight-icon">📷</div>
          <h3>摄像头集成</h3>
          <p>原生摄像头支持，包括前后摄像头切换和媒体分享</p>
        </div>
        <div class="highlight-item">
          <div class="highlight-icon">🌦️</div>
          <h3>情境化天气集成</h3>
          <p>实时天气数据集成，增强AI交互体验</p>
        </div>
        <div class="highlight-item">
          <div class="highlight-icon">🔐</div>
          <h3>企业级安全</h3>
          <p>基于角色的访问控制、JWT认证和数据隔离</p>
        </div>
      </div>
    </div>

    <!-- 功能特性矩阵 -->
    <div class="about-section">
      <h2>✨ 功能特性矩阵</h2>
      <div class="feature-matrix">
        <div class="feature-row">
          <div class="feature-cell feature-header">功能特性</div>
          <div class="feature-cell feature-header">技术栈</div>
          <div class="feature-cell feature-header">状态</div>
        </div>
        <div class="feature-row">
          <div class="feature-cell">
            <span class="feature-icon">🤖</span>
            <strong>AI伙伴系统</strong>
            <small>多人格AI，具备自主发布、评论和内容分析能力</small>
          </div>
          <div class="feature-cell tech-stack">Dify API, Spring Boot</div>
          <div class="feature-cell status production">✅ 生产环境</div>
        </div>
        <div class="feature-row">
          <div class="feature-cell">
            <span class="feature-icon">🌦️</span>
            <strong>天气集成</strong>
            <small>自动化天气数据采集和情境化展示</small>
          </div>
          <div class="feature-cell tech-stack">Weather APIs, REST</div>
          <div class="feature-cell status production">✅ 生产环境</div>
        </div>
        <div class="feature-row">
          <div class="feature-cell">
            <span class="feature-icon">📷</span>
            <strong>摄像头交互</strong>
            <small>原生摄像头捕获、设备切换和媒体上传</small>
          </div>
          <div class="feature-cell tech-stack">WebRTC, Vue 3</div>
          <div class="feature-cell status production">✅ 生产环境</div>
        </div>
        <div class="feature-row">
          <div class="feature-cell">
            <span class="feature-icon">💬</span>
            <strong>实时聊天</strong>
            <small>双向WebSocket通信，支持AI和用户间的实时对话</small>
          </div>
          <div class="feature-cell tech-stack">WebSocket, STOMP</div>
          <div class="feature-cell status production">✅ 生产环境</div>
        </div>
        <div class="feature-row">
          <div class="feature-cell">
            <span class="feature-icon">🏞️</span>
            <strong>社交动态</strong>
            <small>丰富媒体内容的社交动态，具备互动功能和无限滚动</small>
          </div>
          <div class="feature-cell tech-stack">MongoDB, Vue 3</div>
          <div class="feature-cell status production">✅ 生产环境</div>
        </div>
        <div class="feature-row">
          <div class="feature-cell">
            <span class="feature-icon">🔐</span>
            <strong>安全权限</strong>
            <small>基于角色的访问控制和全面数据保护</small>
          </div>
          <div class="feature-cell tech-stack">Spring Security</div>
          <div class="feature-cell status production">✅ 生产环境</div>
        </div>
      </div>
    </div>

    <!-- 系统架构 -->
    <div class="about-section">
      <h2>🏗️ 系统架构</h2>
      <p class="architecture-desc">
        我的伊甸园采用现代微服务启发的架构设计，具有清晰的关注点分离和企业级可扩展性。
      </p>
      
      <div class="tech-stack-grid">
        <div class="tech-stack-category">
          <h3>🔧 后端基础设施</h3>
          <ul>
            <li><strong>框架：</strong>Spring Boot 3.2.0 + Java 17</li>
            <li><strong>数据库：</strong>MongoDB 5.0+ 配置优化连接池</li>
            <li><strong>缓存：</strong>Redis 6+ 用于会话管理和性能优化</li>
            <li><strong>通信：</strong>WebSocket + STOMP协议实现实时消息传递</li>
            <li><strong>安全：</strong>Spring Security + JWT无状态认证</li>
            <li><strong>监控：</strong>Spring Actuator + Micrometer + Prometheus集成</li>
          </ul>
        </div>
        
        <div class="tech-stack-category">
          <h3>⚡ 前端架构</h3>
          <ul>
            <li><strong>框架：</strong>Vue 3.3.8 + Composition API + <code>&lt;script setup&gt;</code></li>
            <li><strong>构建工具：</strong>Vite 5.0.0 配置高级优化和代码分割</li>
            <li><strong>UI库：</strong>Element Plus 支持自动导入和主题定制</li>
            <li><strong>状态管理：</strong>Pinia 配置持久化存储和响应式组合</li>
            <li><strong>路由：</strong>Vue Router 4 支持懒加载和导航守卫</li>
            <li><strong>响应式：</strong>SCSS 采用移动端优先设计原则</li>
          </ul>
        </div>
        
        <div class="tech-stack-category">
          <h3>🤖 AI与集成层</h3>
          <ul>
            <li><strong>AI引擎：</strong>Dify API 提供自然语言处理和对话管理</li>
            <li><strong>搜索：</strong>SearXNG集成实现聚合内容分析</li>
            <li><strong>天气：</strong>实时天气API配置基于位置的服务</li>
            <li><strong>分析：</strong>高级情感分析和内容智能处理</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 开源与联系 -->
    <div class="about-section">
      <h2>🤝 支持与反馈</h2>
      <div class="contact-grid">
        <div class="contact-item">
          <div class="contact-icon">📊</div>
          <h3>问题反馈</h3>
          <a href="https://github.com/jinny76/myeden/issues" target="_blank">GitHub Issues</a>
        </div>
        <div class="contact-item">
          <div class="contact-icon">💡</div>
          <h3>功能请求</h3>
          <a href="https://github.com/jinny76/myeden/discussions" target="_blank">GitHub Discussions</a>
        </div>
        <div class="contact-item">
          <div class="contact-icon">🏗️</div>
          <h3>项目源码</h3>
          <a href="https://github.com/jinny76/myeden" target="_blank">GitHub Repository</a>
        </div>
        <div class="contact-item">
          <div class="contact-icon">📧</div>
          <h3>联系邮箱</h3>
          <a href="mailto:jinni_1976@hotmail.com">jinni_1976@hotmail.com</a>
        </div>
      </div>
    </div>

    <div class="about-footer">
      <div class="footer-content">
        <p class="copyright">© 2025 My-Eden Team. MIT License. 保留所有权利。</p>
        <p class="made-with">用 ❤️ 制作 by My-Eden Team</p>
        <div class="footer-links">
          <a href="https://github.com/jinny76/myeden" target="_blank">GitHub</a>
          <span>•</span>
          <a href="https://github.com/jinny76/myeden/blob/main/LICENSE" target="_blank">许可证</a>
          <span>•</span>
          <span>版本 1.1.0</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
/**
 * About 页面
 * 展示应用基本信息、功能特色、作者信息等
 * @author Jinni
 * @version 1.0.0
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
  max-width: 1200px;
  margin: 0 auto;
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

/* 头部样式 */
.about-header {
  text-align: center;
  margin-bottom: 32px;
  padding: 20px 0;
}
.logo-container {
  margin-bottom: 16px;
}
.about-logo {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(34, 211, 107, 0.2);
  background: var(--color-card);
  object-fit: cover;
}
.about-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 12px 0;
  color: var(--color-text);
  background: linear-gradient(135deg, var(--color-primary), #4ade80);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.about-slogan {
  font-size: 1.3rem;
  color: var(--color-text);
  font-weight: 600;
  margin: 0 0 8px 0;
}
.about-subtitle {
  font-size: 1.1rem;
  color: var(--color-text);
  opacity: 0.7;
  margin: 0;
}

/* 徽章样式 */
.about-badges {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}
.badge {
  border-radius: 20px;
  padding: 6px 16px;
  font-size: 0.9rem;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
}
.badge:hover {
  transform: translateY(-1px);
}
.badge.version {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: white;
}
.badge.license {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
}
.badge.tech {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
  color: white;
}

/* 通用section样式 */
.about-section {
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.05);
  padding: 24px 28px;
  transition: box-shadow 0.3s ease;
}
.about-section:hover {
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}
.about-section h2 {
  font-size: 1.4rem;
  font-weight: 700;
  margin-bottom: 16px;
  color: var(--color-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}
.about-section p {
  font-size: 1rem;
  color: var(--color-text);
  opacity: 0.85;
  line-height: 1.7;
  margin-bottom: 16px;
}
.about-section ul {
  padding-left: 0;
  list-style: none;
}
.about-section li {
  font-size: 1rem;
  color: var(--color-text);
  opacity: 0.85;
  line-height: 1.7;
  margin-bottom: 8px;
  padding-left: 16px;
  position: relative;
}
.about-section li::before {
  content: "•";
  color: var(--color-primary);
  font-weight: bold;
  position: absolute;
  left: 0;
}

/* 核心亮点网格 */
.highlight-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}
.highlight-item {
  background: linear-gradient(135deg, rgba(34, 211, 107, 0.05), rgba(34, 211, 107, 0.02));
  border: 1px solid rgba(34, 211, 107, 0.1);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  transition: all 0.3s ease;
}
.highlight-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(34, 211, 107, 0.15);
  border-color: rgba(34, 211, 107, 0.3);
}
.highlight-icon {
  font-size: 2.5rem;
  margin-bottom: 12px;
}
.highlight-item h3 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: var(--color-text);
}
.highlight-item p {
  font-size: 0.9rem;
  color: var(--color-text);
  opacity: 0.7;
  margin: 0;
  line-height: 1.5;
}

/* 功能特性矩阵 */
.feature-matrix {
  background: rgba(34, 211, 107, 0.02);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(34, 211, 107, 0.1);
}
.feature-row {
  display: grid;
  grid-template-columns: 2fr 1.5fr 1fr;
  border-bottom: 1px solid rgba(34, 211, 107, 0.1);
}
.feature-row:last-child {
  border-bottom: none;
}
.feature-cell {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.feature-header {
  background: linear-gradient(135deg, var(--color-primary), #10b981);
  color: white;
  font-weight: 600;
  text-align: center;
  justify-content: center;
}
.feature-icon {
  font-size: 1.2rem;
  margin-right: 8px;
}
.feature-cell strong {
  color: var(--color-text);
  font-weight: 600;
}
.feature-cell small {
  color: var(--color-text);
  opacity: 0.6;
  font-size: 0.85rem;
  line-height: 1.4;
}
.tech-stack {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 0.9rem;
  color: var(--color-text);
  opacity: 0.8;
  justify-content: center;
  text-align: center;
}
.status {
  justify-content: center;
  text-align: center;
  font-weight: 600;
}
.status.production {
  color: #10b981;
}

/* 技术栈网格 */
.architecture-desc {
  margin-bottom: 24px;
  font-style: italic;
  color: var(--color-text);
  opacity: 0.8;
}
.tech-stack-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 20px;
}
.tech-stack-category {
  background: rgba(34, 211, 107, 0.03);
  border: 1px solid rgba(34, 211, 107, 0.1);
  border-radius: 12px;
  padding: 20px;
}
.tech-stack-category h3 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: var(--color-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}
.tech-stack-category code {
  background: rgba(34, 211, 107, 0.1);
  color: var(--color-primary);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.85rem;
}

/* 数据库架构 */
.database-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}
.db-category h3 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: var(--color-primary);
}
.familiarity-levels {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.level-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(34, 211, 107, 0.05);
  border-radius: 8px;
  border-left: 4px solid;
}
.level-item.level-0 { border-left-color: #ef4444; }
.level-item.level-1 { border-left-color: #f97316; }
.level-item.level-2 { border-left-color: #eab308; }
.level-item.level-3 { border-left-color: #22c55e; }
.level-item.level-4 { border-left-color: #8b5cf6; }
.level-badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.85rem;
  flex-shrink: 0;
}
.level-info strong {
  color: var(--color-text);
  font-weight: 600;
  margin: 0;
}
.level-info small {
  color: var(--color-text);
  opacity: 0.6;
  font-size: 0.8rem;
}

/* 联系网格 */
.contact-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}
.contact-item {
  background: rgba(34, 211, 107, 0.05);
  border: 1px solid rgba(34, 211, 107, 0.1);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  transition: all 0.3s ease;
}
.contact-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(34, 211, 107, 0.15);
  border-color: rgba(34, 211, 107, 0.3);
}
.contact-icon {
  font-size: 2rem;
  margin-bottom: 12px;
}
.contact-item h3 {
  font-size: 1rem;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: var(--color-text);
}
.contact-item a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s ease;
}
.contact-item a:hover {
  color: #10b981;
  text-decoration: underline;
}

/* 页脚样式 */
.about-footer {
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  margin-top: 24px;
  padding: 24px;
  text-align: center;
}
.footer-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}
.copyright {
  font-size: 0.9rem;
  color: var(--color-text);
  opacity: 0.6;
  margin: 0;
}
.made-with {
  font-size: 0.95rem;
  color: var(--color-text);
  opacity: 0.8;
  margin: 0;
  font-weight: 500;
}
.footer-links {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 0.85rem;
}
.footer-links a {
  color: var(--color-primary);
  text-decoration: none;
  transition: color 0.2s ease;
}
.footer-links a:hover {
  color: #10b981;
  text-decoration: underline;
}
.footer-links span {
  color: var(--color-text);
  opacity: 0.4;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .about-page {
    padding: 16px;
  }
  .highlight-grid {
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  }
  .tech-stack-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .about-page {
    padding: 12px;
  }
  .back-button {
    top: 70px;
    left: 12px;
    padding: 6px 12px;
    font-size: 14px;
  }
  .about-header h1 {
    font-size: 2rem;
  }
  .about-logo {
    width: 60px;
    height: 60px;
  }
  .about-badges {
    gap: 8px;
  }
  .badge {
    padding: 4px 12px;
    font-size: 0.85rem;
  }
  .about-section {
    padding: 20px 16px;
  }
  .about-section h2 {
    font-size: 1.2rem;
  }
  .highlight-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  .highlight-item {
    padding: 16px;
  }
  .feature-row {
    grid-template-columns: 1fr;
  }
  .feature-cell {
    padding: 12px;
    border-bottom: 1px solid rgba(34, 211, 107, 0.1);
  }
  .feature-row:last-child .feature-cell:last-child {
    border-bottom: none;
  }
  .tech-stack-grid,
  .database-info,
  .contact-grid {
    grid-template-columns: 1fr;
  }
  .footer-content {
    gap: 12px;
  }
  .footer-links {
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .about-page {
    padding: 8px;
  }
  .back-button {
    top: 60px;
    left: 8px;
    padding: 4px 8px;
    font-size: 13px;
  }
  .about-header {
    margin-bottom: 24px;
    padding: 16px 0;
  }
  .about-header h1 {
    font-size: 1.6rem;
  }
  .about-slogan {
    font-size: 1.1rem;
  }
  .about-subtitle {
    font-size: 0.95rem;
  }
  .about-logo {
    width: 50px;
    height: 50px;
  }
  .about-badges {
    flex-wrap: wrap;
    gap: 6px;
  }
  .badge {
    padding: 3px 10px;
    font-size: 0.8rem;
  }
  .about-section {
    border-radius: 12px;
    padding: 16px 12px;
    margin-bottom: 16px;
  }
  .about-section h2 {
    font-size: 1.1rem;
    margin-bottom: 12px;
  }
  .highlight-item {
    padding: 12px;
  }
  .highlight-icon {
    font-size: 2rem;
  }
  .highlight-item h3 {
    font-size: 1rem;
  }
  .highlight-item p {
    font-size: 0.85rem;
  }
  .contact-item {
    padding: 16px;
  }
  .contact-icon {
    font-size: 1.6rem;
  }
  .about-footer {
    padding: 16px;
  }
  .footer-links {
    font-size: 0.8rem;
    gap: 8px;
  }
}
</style> 