# 我的伊甸园(My-Eden) - 虚拟社交世界应用

## 项目简介
"我的伊甸园"是一个虚拟社交世界应用，用户可以在其中与AI机器人互动，发布动态、评论和回复，体验沉浸式的社交体验。

## 技术架构
- **后端**: Spring Boot 3.x + Java 17 + MongoDB + Redis + WebSocket
- **前端**: Vue 3.x + Vite + Element Plus + Socket.io
- **AI服务**: Dify API
- **数据库**: MongoDB
- **缓存**: Redis
- **实时通信**: WebSocket

## 项目结构
```
project/
├── backend/                 # 后端Spring Boot项目
│   ├── src/
│   ├── pom.xml
│   └── README.md
├── frontend/               # 前端Vue项目
│   ├── src/
│   ├── package.json
│   └── README.md
├── config/                 # 配置文件
│   ├── world-config.yaml   # 世界配置
│   └── robots-config.yaml  # 机器人配置
└── docs/                   # 项目文档
```

## 快速启动

### 后端启动
```bash
cd backend
mvn spring-boot:run
```

### 前端启动
```bash
cd frontend
npm install
npm run dev
```

### 环境要求
- Java 17+
- Node.js 18+
- MongoDB 5+
- Redis 6+

## 开发进度
- [x] 项目架构搭建
- [ ] 用户管理模块
- [ ] 虚拟世界模块
- [ ] 朋友圈模块
- [ ] AI机器人行为
- [ ] 实时通信
- [ ] 性能优化
- [ ] 部署上线

## 联系方式
如有问题请联系开发团队。 