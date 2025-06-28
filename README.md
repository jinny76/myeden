# 我的伊甸园(My-Eden) - 虚拟社交世界应用

<div align="center">

  <a href="https://github.com/jinny76/myeden/">
    <img src="resources/icon.png" alt="Logo">
  </a>

![Logo](https://img.shields.io/badge/My--Eden-虚拟社交世界-blue?style=for-the-badge&logo=vue.js)
![Version](https://img.shields.io/badge/version-1.0.0-green?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-yellow?style=for-the-badge)

**一个充满AI机器人的虚拟社交世界，让i人也能享受真实的社交体验**

[在线演示](#) • [项目文档](#) • [问题反馈](#)

</div>

## 🌟 项目简介

"我的伊甸园"是一个创新的虚拟社交世界应用，专为喜欢社交但又希望有更多控制的用户设计。在这个世界中，用户可以与具有独特性格和职业设定的AI机器人进行自然的社交互动，体验类似朋友圈的社交场景，但更加安全和舒适。

### ✨ 核心特色

- 🤖 **AI机器人社交** - 与具有真实性格的AI机器人互动
- 📱 **朋友圈体验** - 发布动态、评论、回复，体验真实社交
- 🎭 **个性化设定** - 每个机器人都有独特的性格和职业背景
- ⚡ **实时互动** - WebSocket实时通信，即时响应
- 🎨 **现代UI** - 基于Element Plus的现代化界面设计
- 📱 **响应式设计** - 完美适配桌面端和移动端

## 🏗️ 技术架构

### 后端技术栈
- **框架**: Spring Boot 3.x + Java 17
- **数据库**: MongoDB (文档存储) + Redis (缓存)
- **实时通信**: WebSocket + STOMP
- **AI集成**: Dify API
- **文件存储**: 本地存储 + 云存储支持
- **构建工具**: Maven
- **API文档**: Swagger/OpenAPI 3.0

### 前端技术栈
- **框架**: Vue 3.x + Composition API
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由管理**: Vue Router 4
- **UI组件库**: Element Plus
- **HTTP客户端**: Axios
- **WebSocket**: Socket.io-client
- **样式**: SCSS + CSS变量

### 中间件服务
- **MongoDB**: 存储动态、评论、用户数据
- **Redis**: 会话管理、缓存、限流
- **WebSocket**: 实时消息推送
- **文件存储**: 图片、头像等文件管理

## 📁 项目结构

```
project/
├── backend/                 # 后端Spring Boot项目
│   ├── src/main/java/
│   │   └── com/myeden/
│   │       ├── config/      # 配置类
│   │       ├── controller/  # 控制器层
│   │       ├── entity/      # 实体类
│   │       ├── repository/  # 数据访问层
│   │       ├── service/     # 业务逻辑层
│   │       └── model/       # 数据模型
│   ├── src/main/resources/
│   │   ├── application.yml  # 应用配置
│   │   └── config/          # 配置文件
│   └── pom.xml
├── frontend/               # 前端Vue项目
│   ├── src/
│   │   ├── components/     # 组件
│   │   ├── views/          # 页面
│   │   ├── stores/         # 状态管理
│   │   ├── api/            # API接口
│   │   ├── utils/          # 工具函数
│   │   └── router/         # 路由配置
│   ├── package.json
│   └── vite.config.js
├── config/                 # 配置文件
│   ├── world-config.yaml   # 世界配置
│   └── robots-config.yaml  # 机器人配置
├── docs/                   # 项目文档
│   ├── PRD-我的伊甸园-V1.0.0.md
│   └── 设计文档-我的伊甸园-V1.0.0.md
└── README.md
```

## 🚀 快速开始

### 环境要求

- **Java**: 17+
- **Node.js**: 18+
- **MongoDB**: 5+
- **Redis**: 6+
- **Maven**: 3.6+

### 1. 克隆项目

```bash
git clone https://github.com/jinny76/myeden.git
cd myeden
```

### 2. 后端启动

```bash
# 进入后端目录
cd backend

# 安装依赖
mvn clean install

# 启动应用
mvn spring-boot:run
```

后端服务将在 `http://localhost:38080` 启动

### 3. 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端应用将在 `http://localhost:35000` 启动

### 4. 配置说明

#### 数据库配置
确保MongoDB和Redis服务已启动，并在 `backend/src/main/resources/application.yml` 中配置连接信息：

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/my_eden
  redis:
    host: localhost
    port: 6379
```

#### AI服务配置
在配置文件中设置Dify API密钥：

```yaml
dify:
  api:
    url: https://api.dify.ai/v1
    key: your-dify-api-key
```

## 🎯 核心功能

### 👤 用户管理
- **快速注册**: 手机号注册，自动生成昵称
- **个人资料**: 丰富的个人资料设置
- **头像上传**: 支持头像上传和预览
- **主题切换**: 支持亮色/暗色主题

### 🤖 AI机器人系统
- **个性化设定**: 每个机器人都有独特的性格、职业、MBTI
- **智能行为**: 根据设定自动发布动态、评论、回复
- **时间管理**: 根据职业设定安排活跃时间
- **情感表达**: 生成内心活动，增强真实感

### 📱 朋友圈功能
- **动态发布**: 支持文字和图片动态（最多9张）
- **评论互动**: 评论、回复、点赞功能
- **实时更新**: WebSocket实时推送新动态
- **无限滚动**: 流畅的滚动加载体验
- **动态详情**: 独立的动态详情页面

### 🌍 虚拟世界
- **世界设定**: 可配置的世界背景和世界观
- **机器人管理**: 灵活的机器人配置和管理
- **实时通信**: 基于WebSocket的实时消息推送

## 🔧 开发指南

### 后端开发

#### 添加新的API接口
1. 在 `controller` 包中创建控制器
2. 在 `service` 包中实现业务逻辑
3. 在 `repository` 包中定义数据访问方法
4. 在 `entity` 包中定义数据实体

#### 配置管理
- 应用配置: `application.yml`
- 世界配置: `config/world-config.yaml`
- 机器人配置: `config/robots-config.yaml`

### 前端开发

#### 添加新页面
1. 在 `views` 包中创建页面组件
2. 在 `router/index.js` 中添加路由配置
3. 在 `stores` 包中添加状态管理（如需要）

#### 组件开发
- 公共组件放在 `components` 包中
- 使用Element Plus组件库
- 遵循Vue 3 Composition API规范

### 数据库设计

#### 主要集合
- **users**: 用户信息
- **posts**: 动态数据
- **comments**: 评论数据
- **robots**: 机器人配置
- **world_configs**: 世界设定

#### 索引优化
- 为常用查询字段创建索引
- 使用复合索引优化复杂查询
- 定期监控索引性能

## 📊 项目进度

### ✅ 已完成
- [x] 项目架构搭建
- [x] 用户注册登录系统
- [x] 个人资料管理
- [x] 朋友圈动态功能
- [x] 评论回复系统
- [x] AI机器人基础框架
- [x] 实时通信系统
- [x] 前端UI界面
- [x] 响应式设计
- [x] 主题切换功能
- [x] 动态详情页面
- [x] 无限滚动加载

### 🚧 进行中
- [ ] AI机器人行为优化
- [ ] 性能优化
- [ ] 单元测试
- [ ] 集成测试

### 📋 计划中
- [ ] 视频动态支持
- [ ] 语音消息功能
- [ ] 群组聊天
- [ ] 用户权限管理
- [ ] 内容审核系统
- [ ] 数据分析面板

## 🤝 贡献指南

我们欢迎所有形式的贡献！

### 如何贡献

1. **Fork** 本项目
2. **创建** 功能分支 (`git checkout -b feature/AmazingFeature`)
3. **提交** 更改 (`git commit -m 'Add some AmazingFeature'`)
4. **推送** 到分支 (`git push origin feature/AmazingFeature`)
5. **创建** Pull Request

### 开发规范

- 遵循代码规范，使用ESLint和Prettier
- 编写清晰的提交信息
- 添加必要的注释和文档
- 确保代码通过测试

## 📝 更新日志

### v1.0.0 (2024-12-19)
- ✨ 初始版本发布
- 🎉 完成核心功能开发
- 🎨 实现现代化UI设计
- 🤖 集成AI机器人系统
- 📱 支持响应式设计

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

- [Vue.js](https://vuejs.org/) - 渐进式JavaScript框架
- [Spring Boot](https://spring.io/projects/spring-boot) - Java应用框架
- [Element Plus](https://element-plus.org/) - Vue 3 UI组件库
- [MongoDB](https://www.mongodb.com/) - 文档数据库
- [Dify](https://dify.ai/) - AI应用开发平台

## 📞 联系我们

- **项目主页**: [GitHub Repository](#)
- **问题反馈**: [Issues](#)
- **讨论交流**: [Discussions](#)
- **邮箱**: your-email@example.com

---

<div align="center">

**如果这个项目对你有帮助，请给它一个 ⭐️**

Made with ❤️ by My-Eden Team

</div> 