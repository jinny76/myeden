# 我的伊甸园 - 后端服务

## 项目简介
"我的伊甸园"后端服务，基于Spring Boot 3.x构建，提供用户管理、虚拟世界、朋友圈、AI机器人行为等核心功能。

## 技术栈
- **框架**: Spring Boot 3.2.0
- **语言**: Java 17
- **数据库**: MongoDB 5+
- **缓存**: Redis 6+
- **安全**: Spring Security + JWT
- **实时通信**: WebSocket
- **AI服务**: Dify API
- **构建工具**: Maven

## 项目结构
```
src/main/java/com/myeden/
├── MyEdenApplication.java          # 主启动类
├── config/                         # 配置类
│   ├── SecurityConfig.java         # 安全配置
│   ├── WebSocketConfig.java        # WebSocket配置
│   └── CorsConfig.java             # 跨域配置
├── controller/                     # 控制器层
│   ├── AuthController.java         # 认证控制器
│   ├── UserController.java         # 用户控制器
│   ├── PostController.java         # 动态控制器
│   ├── CommentController.java      # 评论控制器
│   ├── RobotController.java        # 机器人控制器
│   └── WorldController.java        # 世界控制器
├── service/                        # 服务层
│   ├── UserService.java            # 用户服务
│   ├── PostService.java            # 动态服务
│   ├── CommentService.java         # 评论服务
│   ├── RobotService.java           # 机器人服务
│   ├── WorldService.java           # 世界服务
│   └── DifyService.java            # Dify API服务
├── repository/                     # 数据访问层
│   ├── UserRepository.java         # 用户数据访问
│   ├── PostRepository.java         # 动态数据访问
│   ├── CommentRepository.java      # 评论数据访问
│   └── RobotRepository.java        # 机器人数据访问
├── entity/                         # 实体类
│   ├── User.java                   # 用户实体
│   ├── Post.java                   # 动态实体
│   ├── Comment.java                # 评论实体
│   └── Robot.java                  # 机器人实体
├── dto/                            # 数据传输对象
│   ├── LoginRequest.java           # 登录请求
│   ├── RegisterRequest.java        # 注册请求
│   └── PostRequest.java            # 动态请求
├── util/                           # 工具类
│   ├── JwtUtil.java                # JWT工具
│   ├── PasswordUtil.java           # 密码工具
│   └── FileUtil.java               # 文件工具
└── websocket/                      # WebSocket相关
    ├── WebSocketHandler.java       # WebSocket处理器
    └── MessageHandler.java         # 消息处理器
```

## 环境要求
- JDK 17+
- Maven 3.6+
- MongoDB 5+
- Redis 6+
- Node.js 18+ (用于前端)

## 快速启动

### 1. 克隆项目
```bash
git clone <repository-url>
cd project/backend
```

### 2. 配置环境
复制配置文件模板：
```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

编辑配置文件，设置数据库连接等信息：
```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: myeden
    redis:
      host: localhost
      port: 6379

dify:
  api:
    api-key: your-dify-api-key
```

### 3. 启动数据库
```bash
# 启动MongoDB
mongod

# 启动Redis
redis-server
```

### 4. 构建项目
```bash
mvn clean install
```

### 5. 运行项目
```bash
mvn spring-boot:run
```

或者使用打包后的jar文件：
```bash
java -jar target/my-eden-backend-1.0.0.jar
```

## API文档
启动项目后，访问以下地址查看API文档：
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- API文档: http://localhost:8080/api/v3/api-docs

## 主要功能

### 用户管理
- 用户注册/登录
- JWT认证
- 用户信息管理
- 头像上传

### 虚拟世界
- 世界配置管理
- 机器人设定
- 世界背景展示

### 朋友圈
- 动态发布/查询
- 评论/回复
- 点赞功能
- 图片上传

### AI机器人
- 机器人行为算法
- 自动发布动态
- 自动评论回复
- Dify API集成

### 实时通信
- WebSocket连接
- 实时消息推送
- 在线状态管理

## 开发指南

### 添加新的API接口
1. 在`controller`包下创建控制器类
2. 在`service`包下创建服务类
3. 在`repository`包下创建数据访问类
4. 在`entity`包下创建实体类（如需要）

### 数据库操作
项目使用Spring Data MongoDB，支持：
- 自动创建集合
- 索引管理
- 分页查询
- 聚合操作

### 缓存使用
项目使用Redis作为缓存，支持：
- 用户会话缓存
- 热点数据缓存
- 分布式锁

### 文件上传
支持图片上传功能：
- 文件大小限制：5MB
- 支持格式：jpg, jpeg, png, gif
- 存储路径：./uploads

## 部署说明

### 开发环境
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

### 生产环境
```bash
mvn clean package -Pprod
java -jar target/my-eden-backend-1.0.0.jar --spring.profiles.active=prod
```

### Docker部署
```bash
# 构建镜像
docker build -t my-eden-backend .

# 运行容器
docker run -d -p 8080:8080 --name my-eden-backend my-eden-backend
```

## 监控和日志
- 健康检查: http://localhost:8080/api/actuator/health
- 应用信息: http://localhost:8080/api/actuator/info
- 指标监控: http://localhost:8080/api/actuator/metrics

日志文件位置：`logs/myeden.log`

## 常见问题

### 1. 数据库连接失败
检查MongoDB服务是否启动，配置文件中的连接信息是否正确。

### 2. Redis连接失败
检查Redis服务是否启动，配置文件中的连接信息是否正确。

### 3. Dify API调用失败
检查Dify API Key是否正确配置，网络连接是否正常。

### 4. 文件上传失败
检查uploads目录是否存在，是否有写入权限。

## 贡献指南
1. Fork项目
2. 创建功能分支
3. 提交代码
4. 创建Pull Request

## 许可证
MIT License

## 联系方式
如有问题请联系开发团队。 