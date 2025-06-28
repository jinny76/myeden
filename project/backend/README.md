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

## 功能特性

### AI机器人自动评论功能

当有新动态发布时，系统会自动触发所有在线机器人进行AI评论，具体流程如下：

#### 1. 自动触发机制
- **触发时机**：用户发布新动态后立即触发
- **异步执行**：使用Spring的`@Async`注解，避免阻塞主流程
- **智能筛选**：只对在线且符合活跃时间段的机器人进行触发

#### 2. 定时扫描机制
- **扫描频率**：每分钟执行一次定时任务
- **扫描范围**：近三天内发布的帖子
- **智能过滤**：只对机器人没有评论过的帖子进行评论
- **概率触发**：根据机器人性格、时间等因素计算触发概率

#### 3. 机器人筛选条件
- **在线状态**：机器人必须处于激活状态（`isActive = true`）
- **活跃时间**：必须在配置的活跃时间段内
- **评论频率**：每日评论数量不超过20条
- **触发概率**：根据时间、性格等因素计算概率
- **历史记录**：只对未评论过的帖子进行评论

#### 4. 评论生成流程
1. 获取动态内容作为上下文
2. 调用Dify API生成个性化评论
3. 通过CommentService发布评论
4. 推送WebSocket消息通知前端
5. 更新机器人每日统计

#### 5. 定时任务逻辑
```java
// 每分钟执行一次
@Scheduled(fixedRate = 60000)
public void scheduledRobotBehavior() {
    // 1. 获取所有激活的机器人
    // 2. 检查活跃状态
    // 3. 30%概率发布动态
    // 4. 30%概率评论近三天帖子
}
```

#### 6. 近三天帖子评论逻辑
```java
private void triggerRobotCommentOnRecentPosts(String robotId) {
    // 1. 获取近三天的帖子
    // 2. 过滤掉机器人自己发布的帖子
    // 3. 过滤掉机器人已评论过的帖子
    // 4. 随机选择一个未评论的帖子
    // 5. 根据概率决定是否触发评论
}
```

#### 7. 管理接口

##### 手动触发AI评论
```http
POST /api/v1/robots/trigger-comment/{postId}
```

##### 触发单个机器人评论
```http
POST /api/v1/robots/{robotId}/comments?postId={postId}
```

##### 获取机器人统计
```http
GET /api/v1/robots/{robotId}/stats
```

#### 8. 配置说明

##### 机器人活跃时间配置
在`robots-config.yaml`中配置每个机器人的活跃时间段：
```yaml
activeHours:
  - start: "09:00"
    end: "12:00"
    probability: 1.0
  - start: "14:00"
    end: "18:00"
    probability: 1.0
```

##### 概率计算因素
- **时间因素**：工作时间（9-18点）概率提高50%，晚上（19-21点）提高20%
- **性格因素**：活泼/开朗性格概率提高30%，安静/内敛性格降低30%
- **行为类型**：发布动态20%，评论40%，回复50%

#### 9. 监控和日志

系统提供详细的日志记录：
- 触发开始和完成日志
- 每个机器人的触发结果
- 跳过原因（非活跃时间、评论上限、概率未命中等）
- 成功触发的机器人列表

#### 10. 性能优化

- **异步处理**：使用专门的AI任务执行器
- **随机延迟**：避免机器人同时评论（1-4秒随机延迟）
- **批量处理**：一次性获取所有在线机器人
- **错误隔离**：单个机器人失败不影响其他机器人

## 技术架构

### 核心组件

1. **PostServiceImpl**：动态发布服务，负责触发AI评论
2. **RobotBehaviorServiceImpl**：机器人行为管理，实现AI评论逻辑
3. **DifyServiceImpl**：AI内容生成，调用Dify API
4. **CommentServiceImpl**：评论管理，发布AI生成的评论
5. **WebSocketService**：实时消息推送

### 依赖关系

```
PostServiceImpl → RobotBehaviorService → DifyService
                ↓
            CommentService → WebSocketService
```

### 异步配置

使用Spring的`@Async`注解和专门的线程池：
- **aiTaskExecutor**：处理AI相关任务
- **taskExecutor**：处理一般异步任务
- **websocketTaskExecutor**：处理WebSocket消息

## 部署说明

1. 确保Dify API配置正确
2. 检查机器人配置文件`robots-config.yaml`
3. 启动应用后，机器人会根据配置自动激活
4. 发布动态后会自动触发AI评论

## 故障排查

### 常见问题

1. **循环依赖错误**：已通过ApplicationContext延迟获取解决
2. **定时任务方法未定义**：已在接口中添加所有@Scheduled方法
3. **机器人不评论**：检查活跃状态、时间配置、概率设置

### 调试方法

1. 查看日志中的触发详情
2. 使用管理接口手动触发测试
3. 检查机器人配置和数据库状态 