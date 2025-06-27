# 我的伊甸园 - 前端应用

## 项目简介
"我的伊甸园"前端应用，基于Vue 3.x构建，提供用户界面和交互功能，包括用户管理、虚拟世界、朋友圈、AI机器人等模块。

## 技术栈
- **框架**: Vue 3.3.8
- **构建工具**: Vite 5.0.0
- **路由**: Vue Router 4.2.5
- **状态管理**: Pinia 2.1.7
- **UI组件库**: Element Plus 2.4.4
- **HTTP客户端**: Axios 1.6.2
- **实时通信**: Socket.io 4.7.4
- **样式**: SCSS
- **包管理**: npm

## 项目结构
```
src/
├── main.js                          # 应用入口
├── App.vue                          # 根组件
├── router/                          # 路由配置
│   └── index.js                     # 路由定义
├── stores/                          # 状态管理
│   ├── user.js                      # 用户状态
│   ├── posts.js                     # 动态状态
│   ├── robots.js                    # 机器人状态
│   └── websocket.js                 # WebSocket状态
├── views/                           # 页面组件
│   ├── Home.vue                     # 首页
│   ├── auth/                        # 认证页面
│   │   ├── Login.vue                # 登录页
│   │   └── Register.vue             # 注册页
│   ├── world/                       # 虚拟世界
│   │   └── World.vue                # 世界页面
│   ├── friends/                     # 朋友圈
│   │   ├── Friends.vue              # 朋友圈主页
│   │   └── PostDetail.vue           # 动态详情
│   ├── robots/                      # AI机器人
│   │   ├── Robots.vue               # 机器人列表
│   │   └── RobotDetail.vue          # 机器人详情
│   ├── profile/                     # 个人资料
│   │   └── Profile.vue              # 个人资料页
│   ├── settings/                    # 设置
│   │   └── Settings.vue             # 设置页面
│   └── error/                       # 错误页面
│       └── 404.vue                  # 404页面
├── components/                      # 公共组件
│   ├── common/                      # 通用组件
│   │   ├── Header.vue               # 头部组件
│   │   ├── Footer.vue               # 底部组件
│   │   └── Loading.vue              # 加载组件
│   ├── posts/                       # 动态相关组件
│   │   ├── PostCard.vue             # 动态卡片
│   │   ├── PostForm.vue             # 动态发布表单
│   │   └── CommentList.vue          # 评论列表
│   ├── robots/                      # 机器人相关组件
│   │   ├── RobotCard.vue            # 机器人卡片
│   │   └── RobotChat.vue            # 机器人聊天
│   └── world/                       # 世界相关组件
│       └── WorldInfo.vue            # 世界信息
├── api/                             # API接口
│   ├── user.js                      # 用户相关API
│   ├── posts.js                     # 动态相关API
│   ├── robots.js                    # 机器人相关API
│   └── world.js                     # 世界相关API
├── utils/                           # 工具函数
│   ├── request.js                   # HTTP请求工具
│   ├── auth.js                      # 认证工具
│   ├── websocket.js                 # WebSocket工具
│   └── common.js                    # 通用工具
├── styles/                          # 样式文件
│   ├── index.scss                   # 全局样式
│   ├── variables.scss               # 样式变量
│   └── components/                  # 组件样式
├── assets/                          # 静态资源
│   ├── images/                      # 图片资源
│   ├── icons/                       # 图标资源
│   └── fonts/                       # 字体资源
└── constants/                       # 常量定义
    ├── api.js                       # API常量
    └── config.js                    # 配置常量
```

## 环境要求
- Node.js 18+
- npm 9+
- 现代浏览器（支持ES6+）

## 快速启动

### 1. 克隆项目
```bash
git clone <repository-url>
cd project/frontend
```

### 2. 安装依赖
```bash
npm install
```

### 3. 配置环境变量
创建环境变量文件：
```bash
cp .env.example .env.local
```

编辑`.env.local`文件：
```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_URL=ws://localhost:8080/ws
VITE_APP_TITLE=我的伊甸园
```

### 4. 启动开发服务器
```bash
npm run dev
```

访问 http://localhost:5173 查看应用。

### 5. 构建生产版本
```bash
npm run build
```

构建完成后，`dist`目录包含生产版本文件。

### 6. 预览生产版本
```bash
npm run preview
```

## 开发指南

### 组件开发规范
1. 使用Composition API
2. 组件名使用PascalCase
3. 文件名与组件名保持一致
4. 使用TypeScript类型注解（可选）

### 样式开发规范
1. 使用SCSS预处理器
2. 遵循BEM命名规范
3. 使用CSS变量定义主题
4. 支持响应式设计

### 状态管理
使用Pinia进行状态管理：
- 用户状态：登录信息、用户资料
- 动态状态：朋友圈数据、分页信息
- 机器人状态：机器人列表、聊天记录
- WebSocket状态：连接状态、实时消息

### API接口规范
- 统一使用axios进行HTTP请求
- 请求拦截器自动添加Token
- 响应拦截器统一处理错误
- 支持请求重试和取消

### 路由配置
- 使用Vue Router 4
- 支持路由守卫和权限控制
- 懒加载页面组件
- 路由切换动画

## 主要功能

### 用户界面
- 响应式设计，支持移动端
- 现代化UI设计
- 深色模式支持
- 国际化支持（预留）

### 用户管理
- 用户注册/登录
- 个人资料管理
- 头像上传
- 密码修改

### 虚拟世界
- 世界背景展示
- 机器人设定查看
- 世界规则说明
- 活动信息展示

### 朋友圈
- 动态发布/查看
- 评论/回复功能
- 点赞/分享功能
- 图片上传/预览

### AI机器人
- 机器人列表展示
- 机器人详情查看
- 机器人聊天功能
- 机器人行为观察

### 实时通信
- WebSocket连接管理
- 实时消息推送
- 在线状态显示
- 消息通知

## 构建和部署

### 开发环境
```bash
npm run dev
```

### 生产环境
```bash
npm run build
```

### 代码检查
```bash
npm run lint
```

### 代码格式化
```bash
npm run format
```

### Docker部署
```bash
# 构建镜像
docker build -t my-eden-frontend .

# 运行容器
docker run -d -p 80:80 --name my-eden-frontend my-eden-frontend
```

## 性能优化

### 代码分割
- 路由级别的代码分割
- 组件级别的懒加载
- 第三方库的按需导入

### 资源优化
- 图片懒加载
- 资源压缩
- CDN加速
- 缓存策略

### 用户体验
- 骨架屏加载
- 进度条提示
- 错误边界处理
- 离线支持

## 浏览器支持
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## 常见问题

### 1. 依赖安装失败
```bash
# 清除缓存
npm cache clean --force

# 删除node_modules重新安装
rm -rf node_modules package-lock.json
npm install
```

### 2. 开发服务器启动失败
检查端口是否被占用：
```bash
lsof -i :5173
```

### 3. API请求失败
检查后端服务是否启动，环境变量配置是否正确。

### 4. WebSocket连接失败
检查WebSocket服务是否启动，网络连接是否正常。

## 贡献指南
1. Fork项目
2. 创建功能分支
3. 提交代码
4. 创建Pull Request

## 许可证
MIT License

## 联系方式
如有问题请联系开发团队。 