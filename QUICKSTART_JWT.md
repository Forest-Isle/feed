# JWT认证模块快速开始

## 一、数据库迁移

在启动应用之前，需要先执行数据库迁移脚本：

```bash
# 方式1：使用mysql命令行
mysql -u root -p feed_system < src/main/resources/db/migration/V2__add_user_auth_fields.sql

# 方式2：直接在MySQL客户端执行
mysql -u root -p
USE feed_system;
SOURCE /path/to/feed/src/main/resources/db/migration/V2__add_user_auth_fields.sql;
```

## 二、启动应用

```bash
# 1. 确保Redis已启动
redis-server

# 2. 确保MySQL已启动并创建了数据库
# 数据库名：feed_system

# 3. 编译并启动应用
mvn clean install
mvn spring-boot:run

# 或直接运行主类
java -jar target/feed-1.0-SNAPSHOT.jar
```

## 三、快速测试

### 1. 注册新用户

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "alice123",
    "email": "alice@example.com",
    "phone": "13800138001",
    "nickname": "Alice"
  }'
```

**预期响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "tokenType": "Bearer",
    "expiresIn": 7200000,
    "user": {
      "id": 1,
      "username": "alice",
      "email": "alice@example.com",
      "nickname": "Alice"
    }
  }
}
```

### 2. 用户登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "alice123"
  }'
```

### 3. 获取当前用户信息（需要Token）

```bash
# 将YOUR_ACCESS_TOKEN替换为上面登录返回的accessToken
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 4. 测试受保护的接口

```bash
# 发布内容（需要认证）
curl -X POST http://localhost:8080/api/post/publish \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Hello, this is my first post!",
    "topic": "test"
  }'

# 获取Feed流（需要认证）
curl -X GET "http://localhost:8080/api/feed/user/1?page=1&size=10" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 5. 刷新Token

```bash
# 当accessToken过期时，使用refreshToken获取新token
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

### 6. 登出

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 四、使用Swagger UI测试

1. 打开浏览器访问：http://localhost:8080/api/swagger-ui.html

2. 在Swagger UI中找到"认证管理"分组

3. 执行登录接口 `/api/auth/login`，复制返回的 `accessToken`

4. 点击页面右上角的 **Authorize** 按钮

5. 输入：`Bearer {你的accessToken}`（注意Bearer后有空格）

6. 点击 **Authorize** 完成认证

7. 现在你可以测试所有需要认证的接口了！

## 五、常见问题排查

### 问题1：Token无效或过期

**现象：**
```json
{
  "code": 1101,
  "message": "Token已过期"
}
```

**解决方案：**
- 使用refresh token调用 `/api/auth/refresh` 接口
- 或重新登录获取新token

---

### 问题2：缺少Token

**现象：**
```json
{
  "code": 1105,
  "message": "缺少Token"
}
```

**解决方案：**
- 检查请求头是否包含：`Authorization: Bearer {token}`
- 确保"Bearer"和token之间有空格

---

### 问题3：数据库字段不存在

**现象：**
```
Column 'password' not found in table 'user'
```

**解决方案：**
- 执行数据库迁移脚本：
```bash
mysql -u root -p feed_system < src/main/resources/db/migration/V2__add_user_auth_fields.sql
```

---

### 问题4：用户名或邮箱已存在

**现象：**
```json
{
  "code": 1002,
  "message": "用户已存在"
}
```

**解决方案：**
- 更换用户名或邮箱重新注册
- 或直接使用现有账号登录

---

### 问题5：密码错误

**现象：**
```json
{
  "code": 1004,
  "message": "密码错误"
}
```

**解决方案：**
- 检查密码是否正确
- 密码区分大小写

---

## 六、验证JWT Token

你可以在 https://jwt.io 网站上验证和解析JWT token：

1. 复制你的accessToken
2. 访问 https://jwt.io
3. 将token粘贴到左侧的"Encoded"区域
4. 右侧会显示解析后的Payload内容：
   ```json
   {
     "userId": 1,
     "username": "alice",
     "tokenVersion": 0,
     "tokenType": "access",
     "iss": "feed-system",
     "iat": 1704096000,
     "exp": 1704103200
   }
   ```

## 七、测试数据清理

如需清理测试数据：

```sql
-- 清空用户表（保留表结构）
TRUNCATE TABLE user;

-- 重置自增ID
ALTER TABLE user AUTO_INCREMENT = 1;

-- 清除Redis缓存
redis-cli FLUSHDB
```

## 八、生产环境配置

部署到生产环境前，请务必修改以下配置：

### 1. 修改JWT密钥

编辑 `application.yml` 或使用环境变量：

```yaml
jwt:
  secret: ${JWT_SECRET:your-production-secret-key-at-least-256-bits}
```

生成强密钥（推荐）：
```bash
# 生成256位随机密钥
openssl rand -base64 64
```

### 2. 调整Token过期时间

根据业务需求调整：

```yaml
jwt:
  # 生产环境建议缩短access token时间
  access-token-expiration: 3600000  # 1小时
  # refresh token可以保持7天
  refresh-token-expiration: 604800000  # 7天
```

### 3. 配置Redis密码

```yaml
spring:
  data:
    redis:
      password: ${REDIS_PASSWORD}
```

### 4. 启用HTTPS

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_PASSWORD}
    key-store-type: PKCS12
```

### 5. 数据库连接池优化

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 10
      maximum-pool-size: 50
      connection-timeout: 20000
```

## 九、下一步

- 查看完整文档：[JWT_AUTHENTICATION_GUIDE.md](./JWT_AUTHENTICATION_GUIDE.md)
- 集成前端应用
- 实现密码重置功能
- 添加OAuth2第三方登录
- 配置HTTPS和反向代理

---

**祝开发顺利！** 🚀
