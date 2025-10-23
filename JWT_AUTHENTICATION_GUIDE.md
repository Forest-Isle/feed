# JWT 认证授权模块使用指南

## 概述

本项目已成功集成JWT（JSON Web Token）认证授权模块，提供完整的用户注册、登录、Token刷新和登出功能。

## 功能特性

### 1. 核心功能
- ✅ 用户注册（支持用户名、邮箱、手机号）
- ✅ 用户登录（支持用户名或邮箱登录）
- ✅ JWT Token生成（Access Token + Refresh Token双Token机制）
- ✅ Token自动刷新
- ✅ 用户登出（Token版本控制失效机制）
- ✅ 获取当前用户信息

### 2. 安全特性
- ✅ BCrypt密码加密
- ✅ Token版本控制（支持全局登出）
- ✅ Token黑名单机制
- ✅ Access Token短期有效（2小时）
- ✅ Refresh Token长期有效（7天）
- ✅ 用户状态检查（is_active）
- ✅ 限流保护（注册、登录、刷新）
- ✅ Redis缓存优化

### 3. 拦截器链
1. **LogInterceptor** - 请求日志记录
2. **JwtAuthenticationInterceptor** - JWT身份验证
3. **RateLimitInterceptor** - 请求限流

## 数据库变更

### User表新增字段

```sql
ALTER TABLE `user`
    ADD COLUMN `password` VARCHAR(255) NULL COMMENT '密码（BCrypt加密）',
    ADD COLUMN `email` VARCHAR(100) NULL COMMENT '邮箱',
    ADD COLUMN `phone` VARCHAR(20) NULL COMMENT '手机号',
    ADD COLUMN `token_version` INT NOT NULL DEFAULT 0 COMMENT 'Token版本号',
    ADD COLUMN `last_login_at` DATETIME NULL COMMENT '最后登录时间';

ALTER TABLE `user` ADD UNIQUE INDEX `uk_email` (`email`);
ALTER TABLE `user` ADD INDEX `idx_phone` (`phone`);
ALTER TABLE `user` ADD INDEX `idx_last_login` (`last_login_at`);
```

**执行方式：**
```bash
# 运行迁移脚本
mysql -u root -p feed_system < src/main/resources/db/migration/V2__add_user_auth_fields.sql
```

## API接口

### 基础URL
```
http://localhost:8080/api
```

### 1. 用户注册
**接口：** `POST /api/auth/register`

**请求体：**
```json
{
  "username": "john_doe",
  "password": "password123",
  "email": "john@example.com",
  "phone": "13800138000",
  "nickname": "John"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200000,
    "user": {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "nickname": "John",
      "followerCount": 0,
      "followingCount": 0,
      "postCount": 0,
      "isActive": true,
      "createdAt": "2024-01-01 12:00:00"
    }
  }
}
```

**限流规则：** 每IP每小时最多5次

---

### 2. 用户登录
**接口：** `POST /api/auth/login`

**请求体：**
```json
{
  "username": "john_doe",
  "password": "password123"
}
```

或使用邮箱登录：
```json
{
  "username": "john@example.com",
  "password": "password123"
}
```

**响应：** 同注册接口

**限流规则：** 每IP每5分钟最多10次

---

### 3. 刷新Token
**接口：** `POST /api/auth/refresh`

**请求体：**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**响应：** 返回新的Access Token和Refresh Token

**限流规则：** 全局每分钟最多20次

---

### 4. 用户登出
**接口：** `POST /api/auth/logout`

**请求头：**
```
Authorization: Bearer {accessToken}
```

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**说明：** 登出后会增加用户的token_version，使所有旧Token失效

---

### 5. 获取当前用户信息
**接口：** `GET /api/auth/me`

**请求头：**
```
Authorization: Bearer {accessToken}
```

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "phone": "13800138000",
    "nickname": "John",
    "avatar": null,
    "bio": null,
    "followerCount": 0,
    "followingCount": 0,
    "postCount": 0,
    "isActive": true,
    "createdAt": "2024-01-01 12:00:00"
  }
}
```

---

## 前端集成示例

### 1. 登录并保存Token

```javascript
async function login(username, password) {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ username, password })
  });

  const result = await response.json();

  if (result.code === 200) {
    // 保存Token到localStorage
    localStorage.setItem('accessToken', result.data.accessToken);
    localStorage.setItem('refreshToken', result.data.refreshToken);
    localStorage.setItem('user', JSON.stringify(result.data.user));
    return result.data;
  } else {
    throw new Error(result.message);
  }
}
```

### 2. 发送认证请求

```javascript
async function getFeed(userId) {
  const accessToken = localStorage.getItem('accessToken');

  const response = await fetch(`http://localhost:8080/api/feed/user/${userId}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    }
  });

  const result = await response.json();

  // Token过期处理
  if (result.code === 1101) {
    await refreshToken();
    return getFeed(userId); // 重试
  }

  return result.data;
}
```

### 3. Token自动刷新

```javascript
async function refreshToken() {
  const refreshToken = localStorage.getItem('refreshToken');

  const response = await fetch('http://localhost:8080/api/auth/refresh', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ refreshToken })
  });

  const result = await response.json();

  if (result.code === 200) {
    localStorage.setItem('accessToken', result.data.accessToken);
    localStorage.setItem('refreshToken', result.data.refreshToken);
    return result.data;
  } else {
    // Refresh token也过期，需要重新登录
    logout();
    window.location.href = '/login';
  }
}
```

### 4. 登出

```javascript
async function logout() {
  const accessToken = localStorage.getItem('accessToken');

  try {
    await fetch('http://localhost:8080/api/auth/logout', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${accessToken}`
      }
    });
  } finally {
    // 清除本地存储
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  }
}
```

---

## 拦截器配置

### 需要认证的接口
所有 `/api/**` 接口默认都需要JWT认证，除了：

**白名单（无需认证）：**
- `/api/auth/register` - 注册
- `/api/auth/login` - 登录
- `/api/auth/refresh` - 刷新Token
- `/actuator/**` - 监控接口
- `/swagger-ui/**` - Swagger UI
- `/v3/api-docs/**` - API文档

### 自定义白名单

如需添加新的白名单路径，编辑 `WebMvcConfig.java`:

```java
registry.addInterceptor(jwtAuthenticationInterceptor)
    .addPathPatterns("/api/**")
    .excludePathPatterns(
        "/api/auth/register",
        "/api/auth/login",
        "/api/auth/refresh",
        "/api/public/**",  // 新增公开接口
        // ...
    );
```

---

## 错误码说明

| 错误码 | 说明 | 处理方式 |
|--------|------|----------|
| 1001 | 用户不存在 | 检查用户名/邮箱是否正确 |
| 1002 | 用户已存在 | 更换用户名重新注册 |
| 1003 | 邮箱已存在 | 更换邮箱重新注册 |
| 1004 | 密码错误 | 检查密码是否正确 |
| 1005 | 用户已被禁用 | 联系管理员 |
| 1101 | Token已过期 | 使用refresh token刷新 |
| 1102 | 无效的Token | 重新登录 |
| 1103 | Token已被列入黑名单 | 重新登录 |
| 1104 | Token版本不匹配 | 重新登录 |
| 1105 | 缺少Token | 添加Authorization头 |

---

## 配置说明

### application.yml

```yaml
jwt:
  # JWT密钥（生产环境请务必修改）
  secret: feed-system-jwt-secret-key-please-change-this-in-production-environment-256-bits
  # Access Token过期时间（毫秒）- 2小时
  access-token-expiration: 7200000
  # Refresh Token过期时间（毫秒）- 7天
  refresh-token-expiration: 604800000
  # Token发行者
  issuer: feed-system
  # Token请求头名称
  header: Authorization
  # Token前缀
  prefix: 'Bearer '
```

**生产环境安全建议：**
1. 修改 `jwt.secret` 为强随机密钥（至少256位）
2. 根据需求调整Token过期时间
3. 启用HTTPS传输
4. 配置Redis密码保护
5. 定期轮换JWT密钥

---

## Redis缓存

### 缓存Key设计

```
user:info:{userId}              # 用户信息缓存（1小时）
token:blacklist:{token}         # Token黑名单
user:online                     # 在线用户集合
```

### 缓存策略
- 用户信息缓存：1小时自动过期
- Token黑名单：随Token过期时间动态设置
- 登出时清除用户信息缓存

---

## 测试

### 1. 使用curl测试

```bash
# 注册
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123",
    "email": "test@example.com",
    "nickname": "Test User"
  }'

# 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123"
  }'

# 访问受保护接口
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# 登出
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 2. 使用Swagger UI测试

访问：`http://localhost:8080/api/swagger-ui.html`

1. 找到"认证管理"标签
2. 先调用 `/auth/register` 或 `/auth/login` 获取Token
3. 点击页面右上角的"Authorize"按钮
4. 输入：`Bearer {你的accessToken}`
5. 现在可以测试其他需要认证的接口

---

## 常见问题

### Q1: Token过期后如何处理？
A: 使用refresh token调用 `/api/auth/refresh` 接口获取新的token，避免用户重新登录。

### Q2: 如何实现"记住我"功能？
A: Refresh Token默认有效期为7天，前端可以选择性保存refresh token实现长期登录。

### Q3: 如何踢掉其他设备的登录？
A: 调用 `/api/auth/logout` 接口，会增加token_version使所有设备的旧token失效。

### Q4: 密码忘记了怎么办？
A: 需要额外实现密码重置功能（通过邮箱验证码等方式），当前版本暂未包含。

### Q5: 如何限制用户并发登录？
A: 可以在Redis中维护用户的在线设备列表，在登录时检查并限制设备数量。

---

## 项目结构

```
src/main/java/com/senyu/
├── config/
│   ├── JwtConfig.java                     # JWT配置类
│   └── WebMvcConfig.java                  # 拦截器配置
├── controller/
│   └── AuthController.java                # 认证控制器
├── dto/
│   ├── LoginRequest.java                  # 登录请求DTO
│   ├── RegisterRequest.java               # 注册请求DTO
│   ├── AuthResponse.java                  # 认证响应DTO
│   ├── RefreshTokenRequest.java           # 刷新Token请求DTO
│   └── UserVO.java                        # 用户视图对象
├── entity/
│   └── User.java                          # 用户实体（已扩展）
├── interceptor/
│   └── JwtAuthenticationInterceptor.java  # JWT认证拦截器
├── service/
│   ├── UserService.java                   # 用户服务接口
│   └── impl/
│       └── UserServiceImpl.java           # 用户服务实现
├── util/
│   ├── JwtUtil.java                       # JWT工具类
│   ├── PasswordUtil.java                  # 密码加密工具类
│   └── RedisKeyUtil.java                  # Redis Key工具类（已扩展）
└── common/
    └── ResultCode.java                    # 响应码枚举（已扩展）
```

---

## 下一步优化建议

1. **OAuth2集成** - 支持第三方登录（微信、GitHub等）
2. **短信验证** - 手机号注册/登录支持验证码
3. **邮箱验证** - 注册后发送激活邮件
4. **密码重置** - 通过邮箱重置密码
5. **角色权限** - RBAC权限管理
6. **审计日志** - 记录用户登录历史
7. **设备管理** - 查看和管理登录设备
8. **二次验证** - 2FA双因素认证

---

## 联系与支持

- 作者：senyu
- 项目地址：Feed流系统
- 文档更新日期：2024-01-01

如有问题，请查阅项目文档或提交Issue。
