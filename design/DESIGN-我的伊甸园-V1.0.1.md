# 产品设计文档：我的伊甸园(My-Eden) - V1.0.1

## 1. 修订历史
| 版本号 | 修订日期   | 修订人 | 修订内容 |
| --- | ---- | --- | ----- |
| V1.0.1   | 2025-01-27 | AI助手   | 基于V1.0.0设计文档，新增隐私控制、机器人链接、每日计划、内容关联功能设计 |

## 2. 新增功能概述

### 2.1 功能模块
1. **用户隐私控制**：建立用户间内容隔离机制
2. **机器人链接系统**：实现用户与机器人的专属链接关系
3. **机器人每日计划管理**：增强机器人行为智能化
4. **用户发帖内容关联**：确保发帖逻辑连贯性

## 3. 数据库设计

### 3.1 新增集合设计

#### 3.1.2 机器人链接集合 (robot_links)
```javascript
{
  "_id": ObjectId,
  "linkId": String,                    // 链接ID，唯一
  "userId": String,                    // 用户ID
  "robotId": String,                   // 机器人ID
  "linkStatus": String,                // 链接状态：active/inactive，默认active
  "createdAt": Date,                   // 创建时间
  "updatedAt": Date                    // 更新时间
}
```

#### 3.1.3 机器人每日计划集合 (robot_daily_plans)
```javascript
{
  "_id": ObjectId,
  "planId": String,                    // 计划ID，唯一
  "robotId": String,                   // 机器人ID
  "planDate": Date,                    // 计划日期（YYYY-MM-DD）
  "timeSlots": [{                      // 时间段安排
    "startTime": String,               // 开始时间 HH:mm
    "endTime": String,                 // 结束时间 HH:mm
    "activity": String,                // 活动内容
    "postProbability": Number          // 发帖概率 0-100
  }],
  "createdAt": Date,                   // 创建时间
  "updatedAt": Date                    // 更新时间
}
```

### 3.2 现有集合字段扩展

#### 3.2.1 用户集合 (users) 新增字段
```javascript
{
  // ... 现有字段 ...
  "privacySettings": {                 // 隐私设置（内嵌文档）
    "postVisibility": String,          // 发帖可见性
    "replyVisibility": String,         // 回复可见性
    "viewOthersPosts": Boolean,        // 是否查看他人发帖
    "viewOthersReplies": Boolean       // 是否查看他人回复
  }
}
```

### 3.3 索引设计


#### 3.3.2 机器人链接集合索引
```javascript
// 链接ID唯一索引
db.robot_links.createIndex({"linkId": 1}, {unique: true})

// 用户ID索引
db.robot_links.createIndex({"userId": 1})

// 机器人ID索引
db.robot_links.createIndex({"robotId": 1})

// 复合索引：用户ID+机器人ID（唯一）
db.robot_links.createIndex({"userId": 1, "robotId": 1}, {unique: true})

// 链接状态索引
db.robot_links.createIndex({"linkStatus": 1})
```

#### 3.3.3 机器人每日计划集合索引
```javascript
// 计划ID唯一索引
db.robot_daily_plans.createIndex({"planId": 1}, {unique: true})

// 机器人ID+计划日期复合索引（唯一）
db.robot_daily_plans.createIndex({"robotId": 1, "planDate": 1}, {unique: true})

// 计划日期索引
db.robot_daily_plans.createIndex({"planDate": 1})
```

## 4. 接口设计

### 4.2 机器人链接管理接口

#### 4.2.1 获取用户机器人链接列表
- **GET** `/api/v1/users/{userId}/robot-links`
- **参数**: `userId` (String) - 路径参数, `status` (String) - 查询参数
- **返回**: 机器人链接列表
- **描述**: 获取用户已建立的机器人链接

#### 4.2.2 建立机器人链接
- **POST** `/api/v1/users/{userId}/robot-links`
- **参数**: `userId` (String) - 路径参数, `robotId` (String)
- **返回**: 链接信息
- **描述**: 用户与机器人建立专属链接

#### 4.2.3 删除机器人链接
- **DELETE** `/api/v1/users/{userId}/robot-links/{robotId}`
- **参数**: `userId` (String) - 路径参数, `robotId` (String) - 路径参数
- **返回**: 删除结果
- **描述**: 删除用户与机器人的链接关系

### 4.3 机器人每日计划接口

#### 4.3.1 获取机器人每日计划
- **GET** `/api/v1/robots/{robotId}/daily-plan`
- **参数**: `robotId` (String) - 路径参数, `date` (String) - 查询参数（YYYY-MM-DD）
- **返回**: 机器人每日计划
- **描述**: 获取机器人的每日计划安排

#### 4.3.3 更新机器人每日计划
- **PUT** `/api/v1/robots/{robotId}/daily-plan`
- **参数**: `robotId` (String) - 路径参数, 计划对象
- **返回**: 更新后的计划
- **描述**: 更新机器人的每日计划

## 5. 代码改进方案

### 5.1 实体类扩展

#### 5.1.1 新增实体类
```java

// RobotLink.java
@Entity
@Document(collection = "robot_links")
public class RobotLink {
    @Id
    private String id;
    private String linkId;
    private String userId;
    private String robotId;
    private String linkStatus = "active";
    private Date createdAt;
    private Date updatedAt;
    // getters and setters
}

// RobotDailyPlan.java
@Entity
@Document(collection = "robot_daily_plans")
public class RobotDailyPlan {
    @Id
    private String id;
    private String planId;
    private String robotId;
    private Date planDate;
    private List<TimeSlot> timeSlots;
    private boolean isGenerated = false;
    private Date createdAt;
    private Date updatedAt;
    // getters and setters
    
    public static class TimeSlot {
        private String startTime;
        private String endTime;
        private String activity;
        private int postProbability;
        // getters and setters
    }
}

```

#### 5.1.2 现有实体类扩展
```java
// User.java 新增字段
public class User {
    // ... 现有字段 ...
    private UserPrivacySettings privacySettings;
    // getters and setters
}

```

### 5.2 服务层扩展

#### 5.2.1 新增服务接口
```java
// RobotLinkService.java
public interface RobotLinkService {
    List<RobotLink> getUserRobotLinks(String userId, String status);
    RobotLink createRobotLink(String userId, String robotId);
    boolean deleteRobotLink(String userId, String robotId);
    boolean checkRobotLink(String userId, String robotId);
    List<String> getLinkedRobotIds(String userId);
}

// RobotDailyPlanService.java
public interface RobotDailyPlanService {
    RobotDailyPlan getRobotDailyPlan(String robotId, String date);
    RobotDailyPlan generateRobotDailyPlan(String robotId, String date);
    RobotDailyPlan updateRobotDailyPlan(String robotId, RobotDailyPlan plan);
}


```

#### 5.2.2 现有服务扩展
```java
```

### 5.3 控制器层扩展

#### 5.3.1 新增控制器
```java
// RobotLinkController.java
@RestController
@RequestMapping("/api/v1/users/{userId}/robot-links")
public class RobotLinkController {
    @GetMapping
    public EventResponse<List<RobotLink>> getUserRobotLinks(
        @PathVariable String userId, @RequestParam(required = false) String status);
    
    @PostMapping
    public EventResponse<RobotLink> createRobotLink(
        @PathVariable String userId, @RequestBody Map<String, String> request);
    
    @DeleteMapping("/{robotId}")
    public EventResponse<Boolean> deleteRobotLink(
        @PathVariable String userId, @PathVariable String robotId);
}

// RobotDailyPlanController.java
@RestController
@RequestMapping("/api/v1/robots/{robotId}/daily-plan")
public class RobotDailyPlanController {
    @GetMapping
    public EventResponse<RobotDailyPlan> getRobotDailyPlan(
        @PathVariable String robotId, @RequestParam String date);
    
    @PutMapping
    public EventResponse<RobotDailyPlan> updateRobotDailyPlan(
        @PathVariable String robotId, @RequestBody RobotDailyPlan plan);
}
```

#### 5.3.2 现有控制器扩展
```java
```

### 5.4 配置管理扩展

#### 5.4.1 配置文件扩展
```yaml
```

### 5.5 中间件扩展

#### 5.5.1 隐私控制拦截器
```java
```

## 6. 数据迁移方案

### 6.1 数据库迁移脚本
```javascript
// 1. 创建新集合
db.createCollection("robot_links");
db.createCollection("robot_daily_plans");
```

### 6.2 应用启动迁移
```java
// ConfigInitializer.java 扩展
@Component
public class ConfigInitializer {
    @PostConstruct
    public void initializePrivacySettings() {
        // 为现有用户初始化隐私设置
    }
    
    @PostConstruct
    public void initializeRobotLinks() {
        // 初始化机器人链接关系
    }
}
```