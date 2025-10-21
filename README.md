# Feed流系统

一个类似小红书和抖音的Feed流系统实现，采用Java + Spring Boot + Redis + MySQL技术栈，支持推拉结合的混合策略。

## 系统架构

### 技术栈

- **Spring Boot 3.2.0** - 主框架
- **MyBatis Plus 3.5.5** - ORM框架
- **Redis** - 缓存和Feed流存储
- **MySQL 8.0** - 持久化存储
- **Lombok** - 简化代码
- **Hutool** - Java工具类库

### 核心特性

1. **多策略Feed流分发**
   - 推模式（Push）：适合粉丝数少的用户
   - 拉模式（Pull）：适合大V用户
   - 混合模式（Hybrid）：根据粉丝数智能选择

2. **高性能缓存**
   - Redis ZSet存储Feed流，按时间戳排序
   - 热点数据缓存，减少数据库压力
   - 支持Feed流滚动加载

3. **关注关系管理**
   - 关注/取消关注
   - 粉丝列表和关注列表缓存
   - 活跃用户识别

4. **内容管理**
   - 支持文本、图片、视频
   - 点赞、评论、分享统计
   - 浏览数统计

## 系统设计

### Feed流策略

#### 1. 推模式（Push）

**适用场景**：粉丝数 ≤ 1000

**工作原理**：
- 用户发布内容时，主动推送到所有活跃粉丝的收件箱
- 数据存储在`feed_inbox`表和Redis ZSet
- 优点：读取速度快，用户体验好
- 缺点：写入成本高，不适合大V

**实现位置**：`com.senyu.strategy.impl.PushFeedStrategy`

#### 2. 拉模式（Pull）

**适用场景**：粉丝数 ≥ 10000

**工作原理**：
- 内容发布到作者的发件箱
- 粉丝访问时主动拉取关注人的最新内容
- 数据存储在`feed_outbox`表和Redis ZSet
- 优点：写入成本低，适合大V
- 缺点：读取时需要实时聚合，性能稍差

**实现位置**：`com.senyu.strategy.impl.PullFeedStrategy`

#### 3. 混合模式（Hybrid）

**适用场景**：1000 < 粉丝数 < 10000

**工作原理**：
- 推送给活跃粉丝（推模式）
- 同时存入发件箱（拉模式）
- 活跃用户从收件箱读取，非活跃用户主动拉取
- 优点：平衡读写性能

**实现位置**：`com.senyu.strategy.impl.HybridFeedStrategy`

### 数据库设计

```sql
-- 用户表
user (id, username, nickname, follower_count, following_count, post_count, is_active)

-- 内容表
post (id, user_id, content, images, video_url, topic, like_count, comment_count)

-- 关注关系表
follow (id, follower_id, followee_id)

-- Feed收件箱表（推模式）
feed_inbox (id, user_id, post_id, author_id, created_at)

-- Feed发件箱表（拉模式）
feed_outbox (id, user_id, post_id, created_at)
```

### Redis数据结构

```
# 用户Feed流（ZSet，score为时间戳）
feed:timeline:{userId}

# 用户发件箱（ZSet，score为时间戳）
feed:outbox:{userId}

# 用户信息缓存（String）
user:info:{userId}

# 内容详情缓存（String）
post:info:{postId}

# 粉丝列表（Set）
follower:list:{userId}

# 关注列表（Set）
following:list:{userId}

# 热门内容（ZSet，score为热度值）
post:hot
```

## 项目结构

```
src/main/java/com/senyu/
├── FeedApplication.java              # 主应用入口
├── common/                            # 通用类
│   ├── Result.java                   # 统一响应结果
│   ├── ResultCode.java               # 响应状态码
│   └── PageResult.java               # 分页结果
├── config/                            # 配置类
│   ├── FeedConfig.java               # Feed流配置
│   ├── MybatisPlusConfig.java        # MyBatis Plus配置
│   └── RedisConfig.java              # Redis配置
├── controller/                        # 控制器层
│   ├── FeedController.java           # Feed流接口
│   ├── PostController.java           # 内容接口
│   └── FollowController.java         # 关注接口
├── entity/                            # 实体类
│   ├── User.java                     # 用户实体
│   ├── Post.java                     # 内容实体
│   ├── Follow.java                   # 关注关系实体
│   ├── FeedInbox.java                # Feed收件箱实体
│   └── FeedOutbox.java               # Feed发件箱实体
├── mapper/                            # 数据访问层
│   ├── UserMapper.java
│   ├── PostMapper.java
│   ├── FollowMapper.java
│   ├── FeedInboxMapper.java
│   └── FeedOutboxMapper.java
├── service/                           # 服务层
│   ├── FeedService.java              # Feed流服务
│   ├── PostService.java              # 内容服务
│   └── FollowService.java            # 关注服务
├── strategy/                          # 策略模式
│   ├── FeedStrategy.java             # Feed策略接口
│   └── impl/
│       ├── PushFeedStrategy.java     # 推模式实现
│       ├── PullFeedStrategy.java     # 拉模式实现
│       └── HybridFeedStrategy.java   # 混合模式实现
└── util/                              # 工具类
    └── RedisKeyUtil.java             # Redis Key工具
```

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.8+

### 安装步骤

1. **克隆项目**

```bash
git clone <repository-url>
cd feed
```

2. **初始化数据库**

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source src/main/resources/schema.sql
```

3. **配置数据库和Redis**

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/feed_system
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password  # 如果没有密码可留空
```

4. **启动项目**

```bash
mvn clean install
mvn spring-boot:run
```

服务将在 `http://localhost:8080` 启动

## API接口文档

### 内容相关

#### 发布内容

```http
POST /api/post/publish
Content-Type: application/json
```

请求体：
```json
{
  "userId": 1,
  "content": "分享一个美好的瞬间",
  "images": ["https://example.com/image1.jpg"],
  "topic": "生活"
}
```

#### 获取内容详情

```http
GET /api/post/{postId}
```

#### 点赞内容

```http
POST /api/post/like/{postId}
```

### 关注相关

#### 关注用户

```http
POST /api/follow/{followeeId}
Header: userId=1
```

#### 取消关注

```http
DELETE /api/follow/{followeeId}
Header: userId=1
```

#### 检查关注状态

```http
GET /api/follow/check/{followeeId}
Header: userId=1
```

### Feed流相关

#### 获取关注Feed流（推荐使用）

支持滚动加载，性能最优。

```http
GET /api/feed/timeline?maxId=100&pageSize=20
Header: userId=1
```

参数说明：
- `maxId`：游标，上一页最后一条内容的ID（首次请求不传）
- `pageSize`：每页大小，默认20

响应示例：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [...],
    "nextCursor": 80,
    "hasNext": true
  }
}
```

#### 获取推荐Feed流

基于热门内容推荐。

```http
GET /api/feed/recommend?page=1&pageSize=20
Header: userId=1
```

#### 刷新Feed缓存

```http
POST /api/feed/refresh
Header: userId=1
```

## 配置说明

在 `application.yml` 中可以配置Feed流相关参数：

```yaml
feed:
  # 推模式粉丝数阈值（≤1000使用推模式）
  push-fan-threshold: 1000

  # 拉模式粉丝数阈值（≥10000使用拉模式）
  pull-fan-threshold: 10000

  # Feed流缓存时长（秒）
  cache-ttl: 86400

  # Feed流最大长度
  max-feed-size: 1000

  # 单次拉取Feed数量
  page-size: 20
```

## 性能优化

### 1. 缓存策略

- **多级缓存**：Redis + 本地缓存（可扩展）
- **热点数据**：用户信息、内容详情、Feed流缓存
- **过期策略**：根据数据特性设置不同的TTL

### 2. 数据库优化

- **索引优化**：在关键查询字段添加索引
- **读写分离**：可配置主从复制（待实现）
- **分库分表**：按用户ID分片（待实现）

### 3. 异步处理

- **Feed分发**：内容发布后异步分发到粉丝
- **统计更新**：点赞、评论等计数异步更新

### 4. 限流降级

- **接口限流**：防止恶意请求（待实现）
- **降级策略**：缓存失效时的降级方案

## 扩展功能

### 待实现功能

- [ ] 用户认证和授权（JWT）
- [ ] 评论系统
- [ ] 消息通知
- [ ] 搜索功能（ElasticSearch）
- [ ] 推荐算法优化
- [ ] 视频流处理
- [ ] CDN加速
- [ ] 监控和告警
- [ ] 压力测试

## 常见问题

### Q1: 大V用户发布内容，粉丝如何及时看到？

A: 采用混合策略，推送给活跃粉丝，同时存入发件箱。活跃用户能即时看到，非活跃用户下次访问时拉取。

### Q2: Feed流如何保证时间顺序？

A: 使用Redis ZSet，以时间戳作为score，天然支持按时间排序。

### Q3: 如何防止Feed流过大？

A: 设置最大长度限制（默认1000），超出部分自动淘汰旧数据。

### Q4: 缓存雪崩如何处理？

A: 设置随机过期时间，缓存穿透使用布隆过滤器，缓存击穿使用分布式锁。

## 贡献指南

欢迎提交Issue和Pull Request！

## 许可证

MIT License

## 作者

senyu - Feed流系统

## 参考资料

- [小红书技术架构](https://www.xiaohongshu.com/)
- [抖音推荐系统](https://www.douyin.com/)
- [Redis ZSet实战](https://redis.io/commands/zadd)
- [MyBatis Plus官方文档](https://baomidou.com/)
