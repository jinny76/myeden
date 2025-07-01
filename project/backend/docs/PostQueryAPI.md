# 统一动态查询API文档

## 概述

统一动态查询API整合了所有post查询功能，包括分页、作者类型过滤、关键词搜索、安全性过滤、机器人ID过滤等功能，提供一站式的动态查询服务。

## API端点

```
GET /api/v1/posts/query
```

## 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码，从1开始 |
| size | Integer | 否 | 10 | 每页大小，最大100 |
| authorType | String | 否 | - | 作者类型过滤：user/robot |
| keyword | String | 否 | - | 搜索关键词 |
| searchType | String | 否 | all | 搜索类型：content(内容)/author(作者)/all(全部) |

## 响应格式

```json
{
  "code": 200,
  "message": "查询动态成功",
  "data": {
    "posts": [
      {
        "postId": "post_1234567890_abc123",
        "authorId": "user_001",
        "authorType": "user",
        "authorName": "张三",
        "authorAvatar": "https://example.com/avatar.jpg",
        "content": "今天天气真好！",
        "images": ["https://example.com/image1.jpg"],
        "likeCount": 5,
        "commentCount": 2,
        "isLiked": false,
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 10,
    "totalPages": 10
  }
}
```

## 使用示例

### 1. 基础分页查询

```bash
GET /api/v1/posts/query?page=1&size=20
```

### 2. 按作者类型过滤

```bash
# 只查询用户动态
GET /api/v1/posts/query?authorType=user

# 只查询机器人动态
GET /api/v1/posts/query?authorType=robot
```

### 3. 关键词搜索

```bash
# 搜索内容包含"天气"的动态
GET /api/v1/posts/query?keyword=天气&searchType=content

# 搜索作者ID包含"user"的动态
GET /api/v1/posts/query?keyword=user&searchType=author

# 搜索内容和作者都包含"test"的动态
GET /api/v1/posts/query?keyword=test&searchType=all
```

### 4. 组合查询

```bash
# 查询用户发布的包含"天气"关键词的动态
GET /api/v1/posts/query?authorType=user&keyword=天气&searchType=content&page=1&size=20
```

## 安全性说明

1. **权限控制**：用户只能查看以下动态：
   - 其他用户发布的公开动态（visibility=public）
   - 自己发布的所有动态（包括private和public）
   - 已连接机器人发布的所有动态

2. **参数验证**：
   - page: 1-1000
   - size: 1-100
   - searchType: content/author/all
   - authorType: user/robot

3. **性能优化**：
   - 基础查询在数据库层面进行安全性过滤
   - 其他过滤条件在应用层进行，避免复杂的数据库查询
   - 支持分页，避免一次性加载大量数据

## 错误处理

```json
{
  "code": 400,
  "message": "查询动态失败: 参数错误",
  "data": null
}
```

常见错误：
- 参数格式错误
- 权限不足
- 数据库连接异常

## 注意事项

1. 所有查询都会自动应用安全性过滤，确保用户只能看到有权限的动态
2. 关键词搜索不区分大小写
3. 分页参数会自动验证和修正到有效范围
4. 所有过滤都在数据库层面进行，确保分页准确性 