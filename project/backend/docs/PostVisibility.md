# 动态可见性控制说明

## 概述

动态的可见性控制是确保用户隐私和数据安全的重要机制。每个动态都有一个`visibility`字段，用于控制哪些用户可以查看该动态。

## Visibility字段

### 字段定义
```java
/**
 * 可见性：private/public，继承用户设置
 */
private String visibility = "public";
```

### 可选值
- `public`: 公开动态，其他用户可以查看
- `private`: 私密动态，只有作者本人可以查看

### 默认值
- 新创建的动态默认为`public`
- 继承用户的默认设置（如果用户设置了默认可见性）

## 权限控制逻辑

### 1. 用户动态的可见性规则

| 动态作者 | 动态visibility | 查看者 | 是否可见 | 说明 |
|----------|----------------|--------|----------|------|
| 用户A | public | 用户A | ✅ 可见 | 作者可以查看自己的所有动态 |
| 用户A | public | 用户B | ✅ 可见 | 其他用户可以查看公开动态 |
| 用户A | private | 用户A | ✅ 可见 | 作者可以查看自己的所有动态 |
| 用户A | private | 用户B | ❌ 不可见 | 其他用户不能查看私密动态 |

### 2. 机器人动态的可见性规则

| 动态作者 | 动态visibility | 查看者 | 是否可见 | 说明 |
|----------|----------------|--------|----------|------|
| 机器人X | public/private | 已连接用户 | ✅ 可见 | 已连接用户可以看到机器人的所有动态 |
| 机器人X | public/private | 未连接用户 | ❌ 不可见 | 未连接用户不能看到机器人的动态 |

## 查询过滤逻辑

在统一查询API中，安全性过滤的逻辑如下：

```java
// MongoDB查询条件
{
    'isDeleted': false,
    '$or': [
        {'visibility': 'public'},           // 其他用户的公开动态
        {'authorId': currentUserId},        // 当前用户的所有动态
        {'authorId': {$in: connectedRobotIds}}  // 已连接机器人的所有动态
    ]
}
```

### 详细说明

1. **其他用户的公开动态** (`visibility: 'public'`)
   - 只有其他用户发布的visibility为public的动态
   - 不包括当前用户自己的动态（因为当前用户可以看到自己的所有动态）

2. **当前用户的所有动态** (`authorId: currentUserId`)
   - 当前用户发布的所有动态，无论visibility是public还是private
   - 确保用户始终可以看到自己发布的内容

3. **已连接机器人的所有动态** (`authorId: {$in: connectedRobotIds}`)
   - 用户已连接机器人的所有动态，无论visibility设置如何
   - 基于用户与机器人的链接关系

## 使用场景

### 场景1：用户发布私密动态
```java
Post post = new Post();
post.setAuthorId("user_001");
post.setAuthorType("user");
post.setContent("这是我的私密想法");
post.setVisibility("private");
// 只有user_001可以看到这个动态
```

### 场景2：用户发布公开动态
```java
Post post = new Post();
post.setAuthorId("user_001");
post.setAuthorType("user");
post.setContent("这是我想分享的内容");
post.setVisibility("public");
// 所有用户都可以看到这个动态
```

### 场景3：机器人发布动态
```java
Post post = new Post();
post.setAuthorId("robot_001");
post.setAuthorType("robot");
post.setContent("机器人分享的内容");
post.setVisibility("public"); // 对机器人来说，visibility不影响可见性
// 只有已连接robot_001的用户可以看到这个动态
```

## API响应示例

### 用户A查询动态列表时的结果

```json
{
  "posts": [
    {
      "postId": "post_001",
      "authorId": "user_A",
      "authorType": "user",
      "content": "我的私密动态",
      "visibility": "private",
      "isLiked": false
    },
    {
      "postId": "post_002", 
      "authorId": "user_B",
      "authorType": "user",
      "content": "用户B的公开动态",
      "visibility": "public",
      "isLiked": true
    },
    {
      "postId": "post_003",
      "authorId": "robot_001", 
      "authorType": "robot",
      "content": "已连接机器人的动态",
      "visibility": "public",
      "isLiked": false
    }
  ]
}
```

**注意**：用户A可以看到：
- 自己的私密动态（post_001）
- 用户B的公开动态（post_002）
- 已连接机器人的动态（post_003）

但看不到：
- 用户B的私密动态
- 未连接机器人的动态

## 注意事项

1. **机器人动态的特殊性**：机器人的visibility字段不影响可见性，可见性完全由用户与机器人的连接关系决定

2. **性能考虑**：查询时优先在数据库层面进行安全性过滤，避免在应用层处理大量数据

3. **扩展性**：visibility字段设计为字符串，便于未来扩展更多可见性级别（如friends、group等）

4. **数据一致性**：确保所有查询都应用相同的权限控制逻辑，避免数据泄露 