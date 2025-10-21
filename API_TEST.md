# Feed流系统 - API测试指南

本文档提供完整的API测试流程和示例，帮助你快速验证系统功能。

## 前置准备

1. 确保MySQL、Redis已启动
2. 执行`schema.sql`初始化数据库（包含测试数据）
3. 启动应用：`mvn spring-boot:run`
4. 基础URL：`http://localhost:8080/api`

## 测试数据

系统已自动初始化3个测试用户：

| 用户ID | 用户名 | 昵称 | 粉丝数 | 说明 |
|--------|--------|------|--------|------|
| 1 | user001 | 小红书博主 | 500 | 小V，使用推模式 |
| 2 | user002 | 抖音达人 | 50000 | 大V，使用拉模式 |
| 3 | user003 | 普通用户 | 10 | 普通用户 |

## 完整测试流程

### 1. 测试关注功能

#### 1.1 用户3关注用户1

```bash
curl -X POST http://localhost:8080/api/follow/1 \
  -H "userId: 3" \
  -H "Content-Type: application/json"
```

期望响应：
```json
{
  "code": 200,
  "message": "关注成功",
  "data": null,
  "timestamp": 1698765432000
}
```

#### 1.2 用户3关注用户2

```bash
curl -X POST http://localhost:8080/api/follow/2 \
  -H "userId: 3" \
  -H "Content-Type: application/json"
```

#### 1.3 检查关注状态

```bash
curl -X GET http://localhost:8080/api/follow/check/1 \
  -H "userId: 3"
```

期望响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true,
  "timestamp": 1698765432000
}
```

#### 1.4 获取关注列表

```bash
curl -X GET http://localhost:8080/api/follow/following/3 \
  -H "Content-Type: application/json"
```

期望响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [1, 2],
  "timestamp": 1698765432000
}
```

### 2. 测试内容发布（推模式）

#### 2.1 用户1发布内容（粉丝数500，触发推模式）

```bash
curl -X POST http://localhost:8080/api/post/publish \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "content": "今天天气真好，分享一下我的日常生活！",
    "images": ["https://example.com/image1.jpg", "https://example.com/image2.jpg"],
    "topic": "生活日常"
  }'
```

期望响应：
```json
{
  "code": 200,
  "message": "发布成功",
  "data": 1,
  "timestamp": 1698765432000
}
```

**验证推模式**：
- 检查日志，应该看到"使用推模式分发Feed"
- 内容会被推送到用户3的收件箱

### 3. 测试内容发布（拉模式）

#### 3.1 用户2发布内容（粉丝数50000，触发拉模式）

```bash
curl -X POST http://localhost:8080/api/post/publish \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "content": "感谢大家的支持，突破5万粉丝啦！",
    "images": ["https://example.com/celebrate.jpg"],
    "videoUrl": "https://example.com/video.mp4",
    "topic": "感恩"
  }'
```

期望响应：
```json
{
  "code": 200,
  "message": "发布成功",
  "data": 2,
  "timestamp": 1698765432000
}
```

**验证拉模式**：
- 检查日志，应该看到"使用拉模式分发Feed"
- 内容存入发件箱，不会主动推送

### 4. 测试Feed流获取

#### 4.1 获取用户3的Feed流（首次加载）

```bash
curl -X GET "http://localhost:8080/api/feed/timeline?pageSize=20" \
  -H "userId: 3"
```

期望响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "id": 2,
        "userId": 2,
        "content": "感谢大家的支持，突破5万粉丝啦！",
        "images": ["https://example.com/celebrate.jpg"],
        "videoUrl": "https://example.com/video.mp4",
        "topic": "感恩",
        "likeCount": 0,
        "commentCount": 0,
        "shareCount": 0,
        "viewCount": 1,
        "status": 1,
        "createdAt": "2025-10-21 12:00:00"
      },
      {
        "id": 1,
        "userId": 1,
        "content": "今天天气真好，分享一下我的日常生活！",
        "images": ["https://example.com/image1.jpg", "https://example.com/image2.jpg"],
        "topic": "生活日常",
        "likeCount": 0,
        "commentCount": 0,
        "shareCount": 0,
        "viewCount": 1,
        "status": 1,
        "createdAt": "2025-10-21 11:30:00"
      }
    ],
    "nextCursor": 1,
    "hasNext": false
  },
  "timestamp": 1698765432000
}
```

#### 4.2 滚动加载更多（使用游标）

```bash
curl -X GET "http://localhost:8080/api/feed/timeline?maxId=1&pageSize=20" \
  -H "userId: 3"
```

### 5. 测试内容详情

#### 5.1 获取内容详情

```bash
curl -X GET http://localhost:8080/api/post/1 \
  -H "Content-Type: application/json"
```

期望响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 1,
    "content": "今天天气真好，分享一下我的日常生活！",
    "images": ["https://example.com/image1.jpg", "https://example.com/image2.jpg"],
    "topic": "生活日常",
    "likeCount": 0,
    "commentCount": 0,
    "shareCount": 0,
    "viewCount": 2,
    "status": 1,
    "createdAt": "2025-10-21 11:30:00"
  },
  "timestamp": 1698765432000
}
```

### 6. 测试点赞功能

#### 6.1 点赞内容

```bash
curl -X POST http://localhost:8080/api/post/like/1 \
  -H "Content-Type: application/json"
```

期望响应：
```json
{
  "code": 200,
  "message": "点赞成功",
  "data": null,
  "timestamp": 1698765432000
}
```

#### 6.2 再次查看内容详情（验证点赞数+1）

```bash
curl -X GET http://localhost:8080/api/post/1 \
  -H "Content-Type: application/json"
```

期望看到 `likeCount: 1`

### 7. 测试推荐Feed流

#### 7.1 获取推荐内容

```bash
curl -X GET "http://localhost:8080/api/feed/recommend?page=1&pageSize=20" \
  -H "userId: 3"
```

期望响应：包含热门内容列表

### 8. 测试取消关注

#### 8.1 用户3取消关注用户1

```bash
curl -X DELETE http://localhost:8080/api/follow/1 \
  -H "userId: 3"
```

期望响应：
```json
{
  "code": 200,
  "message": "取消关注成功",
  "data": null,
  "timestamp": 1698765432000
}
```

### 9. 测试Feed缓存刷新

#### 9.1 手动刷新Feed缓存

```bash
curl -X POST http://localhost:8080/api/feed/refresh \
  -H "userId: 3"
```

期望响应：
```json
{
  "code": 200,
  "message": "刷新成功",
  "data": null,
  "timestamp": 1698765432000
}
```

## 异常场景测试

### 1. 重复关注

```bash
curl -X POST http://localhost:8080/api/follow/1 \
  -H "userId: 3"
```

期望响应：
```json
{
  "code": 500,
  "message": "已关注该用户",
  "timestamp": 1698765432000
}
```

### 2. 关注自己

```bash
curl -X POST http://localhost:8080/api/follow/3 \
  -H "userId: 3"
```

期望响应：
```json
{
  "code": 500,
  "message": "不能关注自己",
  "timestamp": 1698765432000
}
```

### 3. 获取不存在的内容

```bash
curl -X GET http://localhost:8080/api/post/999999 \
  -H "Content-Type: application/json"
```

期望响应：
```json
{
  "code": 500,
  "message": "内容不存在",
  "timestamp": 1698765432000
}
```

## 性能测试建议

### 1. 批量创建用户关注关系

使用脚本批量创建关注关系，测试推模式性能：

```bash
# 创建100个用户关注用户1
for i in {4..103}; do
  curl -X POST http://localhost:8080/api/follow/1 \
    -H "userId: $i" \
    -H "Content-Type: application/json"
done
```

### 2. 批量发布内容

```bash
# 用户1批量发布10条内容
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/post/publish \
    -H "Content-Type: application/json" \
    -d "{
      \"userId\": 1,
      \"content\": \"这是第${i}条测试内容\",
      \"topic\": \"测试\"
    }"
done
```

### 3. 并发测试

使用Apache Bench进行并发测试：

```bash
# 100并发，1000次请求
ab -n 1000 -c 100 -H "userId: 3" http://localhost:8080/api/feed/timeline
```

## 验证Redis缓存

使用Redis CLI查看缓存数据：

```bash
redis-cli

# 查看用户3的Feed流
ZREVRANGE feed:timeline:3 0 -1 WITHSCORES

# 查看用户1的发件箱
ZREVRANGE feed:outbox:1 0 -1 WITHSCORES

# 查看用户3的关注列表
SMEMBERS following:list:3

# 查看内容详情缓存
GET post:info:1
```

## 日志监控

启动应用后，观察控制台日志：

```
# 推模式日志
INFO  c.s.s.i.PushFeedStrategy - 使用推模式分发Feed，内容ID：1, 作者ID：1
INFO  c.s.s.i.PushFeedStrategy - 用户1有50个活跃粉丝，开始推送
INFO  c.s.s.i.PushFeedStrategy - 推模式分发完成，共推送给50个粉丝

# 拉模式日志
INFO  c.s.s.i.PullFeedStrategy - 使用拉模式分发Feed，内容ID：2, 作者ID：2
INFO  c.s.s.i.PullFeedStrategy - 拉模式分发完成，内容已存入发件箱

# 混合模式日志
INFO  c.s.s.i.HybridFeedStrategy - 使用混合模式分发Feed，内容ID：3, 作者ID：4
INFO  c.s.s.i.HybridFeedStrategy - 粉丝数 5000 处于中间区间，使用混合策略
```

## 常见问题

### Q1: Feed流为空？

A: 检查：
1. 是否已关注其他用户
2. 关注的用户是否发布了内容
3. Redis缓存是否正常

### Q2: 推模式没有推送到粉丝？

A: 检查：
1. 粉丝是否为活跃用户（`is_active=1`）
2. 查看日志确认分发策略
3. 检查数据库`feed_inbox`表

### Q3: 点赞数没有增加？

A:
1. 清除内容缓存：`redis-cli DEL post:info:1`
2. 重新查询内容详情

## 压力测试参考指标

| 指标 | 目标值 |
|------|--------|
| 发布内容QPS | > 1000 |
| 获取Feed流QPS | > 5000 |
| 关注/取消关注QPS | > 2000 |
| 平均响应时间 | < 100ms |
| P99响应时间 | < 500ms |

## 总结

通过以上测试流程，你可以验证：

1. ✅ 推拉混合策略正常工作
2. ✅ Feed流正确分发和获取
3. ✅ 关注关系正确管理
4. ✅ 缓存策略有效
5. ✅ 异常场景处理正确

如有问题，请查看日志或提交Issue。
