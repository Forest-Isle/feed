## Feed流系统 - 生产环境部署指南

### 目录
- [系统要求](#系统要求)
- [Docker部署](#docker部署)
- [手动部署](#手动部署)
- [性能优化](#性能优化)
- [监控告警](#监控告警)
- [故障排查](#故障排查)
- [安全加固](#安全加固)

---

## 系统要求

### 硬件要求

**最小配置**（支持1000并发）：
- CPU: 4核
- 内存: 8GB
- 磁盘: 100GB SSD
- 网络: 100Mbps

**推荐配置**（支持5000+并发）：
- CPU: 8核
- 内存: 16GB
- 磁盘: 500GB SSD
- 网络: 1Gbps

### 软件要求
- 操作系统: Linux (Ubuntu 20.04+ / CentOS 7+)
- Docker: 20.10+
- Docker Compose: 2.0+
- JDK: 17+ (手动部署需要)
- Maven: 3.8+ (手动部署需要)

---

## Docker部署（推荐）

### 1. 快速启动

```bash
# 克隆项目
git clone <repository-url>
cd feed

# 一键启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f feed-app
```

访问地址：
- 应用: http://localhost:8080/api
- Swagger: http://localhost:8080/swagger-ui.html (开发环境)
- 健康检查: http://localhost:8080/actuator/health
- 指标监控: http://localhost:8080/actuator/prometheus

### 2. 生产环境部署

#### 2.1 修改配置

创建 `.env` 文件：

```bash
# MySQL配置
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_DATABASE=feed_system
MYSQL_USER=feed_user
MYSQL_PASSWORD=your_secure_password

# Redis配置
REDIS_PASSWORD=your_redis_password

# 应用配置
SPRING_PROFILES_ACTIVE=prod
JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC
```

#### 2.2 启动服务

```bash
# 仅启动核心服务
docker-compose up -d mysql redis feed-app

# 启动包含监控的完整服务
docker-compose --profile monitoring up -d
```

#### 2.3 验证部署

```bash
# 检查服务状态
docker-compose ps

# 检查健康状态
curl http://localhost:8080/actuator/health

# 查看日志
docker-compose logs -f feed-app
```

### 3. 常用命令

```bash
# 停止服务
docker-compose stop

# 重启服务
docker-compose restart feed-app

# 查看资源使用
docker stats

# 进入容器
docker exec -it feed-app sh

# 备份数据
docker exec feed-mysql mysqldump -u root -p feed_system > backup.sql

# 清理资源
docker-compose down -v
```

---

## 手动部署

### 1. 环境准备

#### 安装MySQL 8.0

```bash
# Ubuntu
sudo apt update
sudo apt install mysql-server-8.0

# 初始化数据库
mysql -u root -p < src/main/resources/schema.sql
```

#### 安装Redis

```bash
# Ubuntu
sudo apt install redis-server

# 启动Redis
sudo systemctl start redis-server
sudo systemctl enable redis-server
```

#### 安装JDK 17

```bash
# Ubuntu
sudo apt install openjdk-17-jdk

# 验证
java -version
```

### 2. 编译打包

```bash
# 克隆项目
git clone <repository-url>
cd feed

# 编译打包（跳过测试）
mvn clean package -DskipTests

# 打包结果
ls -lh target/*.jar
```

### 3. 配置修改

编辑 `src/main/resources/application-prod.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://your-mysql-host:3306/feed_system
    username: feed_user
    password: your_password
  data:
    redis:
      host: your-redis-host
      password: your_redis_password
```

### 4. 启动应用

#### 方式1: 直接运行

```bash
java -jar \
  -Xms1g -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -Dspring.profiles.active=prod \
  target/feed-1.0-SNAPSHOT.jar
```

#### 方式2: Systemd服务

创建 `/etc/systemd/system/feed-system.service`：

```ini
[Unit]
Description=Feed Stream System
After=syslog.target network.target

[Service]
Type=simple
User=feed
WorkingDirectory=/opt/feed
ExecStart=/usr/bin/java -jar \
  -Xms1g -Xmx2g \
  -XX:+UseG1GC \
  -Dspring.profiles.active=prod \
  /opt/feed/feed-1.0-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务：

```bash
# 重载配置
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start feed-system

# 开机自启
sudo systemctl enable feed-system

# 查看状态
sudo systemctl status feed-system

# 查看日志
sudo journalctl -u feed-system -f
```

---

## 性能优化

### 1. JVM参数优化

```bash
# G1垃圾回收器（推荐）
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:InitiatingHeapOccupancyPercent=45

# 堆内存设置
-Xms2g -Xmx2g

# GC日志
-Xlog:gc*:file=/app/logs/gc.log:time,uptime:filecount=10,filesize=100M

# 内存溢出dump
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/app/logs/heapdump.hprof
```

### 2. MySQL优化

```sql
-- my.cnf配置
[mysqld]
max_connections = 1000
innodb_buffer_pool_size = 4G
innodb_log_file_size = 512M
innodb_flush_log_at_trx_commit = 2
innodb_flush_method = O_DIRECT
query_cache_type = 0
```

### 3. Redis优化

```bash
# redis.conf配置
maxmemory 4gb
maxmemory-policy allkeys-lru
maxclients 10000
timeout 300
tcp-keepalive 60
```

### 4. 应用层优化

application-prod.yml:

```yaml
server:
  tomcat:
    threads:
      max: 200
      min-spare: 20
    accept-count: 100
    max-connections: 10000

spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
```

---

## 监控告警

### 1. Prometheus + Grafana

```bash
# 启动监控服务
docker-compose --profile monitoring up -d

# 访问Grafana
http://localhost:3000
默认账号: admin / admin123
```

添加Prometheus数据源：
- URL: http://prometheus:9090

### 2. 关键指标监控

**应用指标**：
- JVM堆内存使用率: `jvm_memory_used_bytes / jvm_memory_max_bytes`
- GC次数: `jvm_gc_pause_seconds_count`
- 请求QPS: `http_server_requests_seconds_count`
- 请求延迟: `http_server_requests_seconds_sum / http_server_requests_seconds_count`

**Redis指标**：
- 内存使用率: `redis_memory_used_bytes / redis_memory_max_bytes`
- 命中率: `redis_keyspace_hits_total / (redis_keyspace_hits_total + redis_keyspace_misses_total)`
- 连接数: `redis_connected_clients`

**MySQL指标**：
- 慢查询: `mysql_global_status_slow_queries`
- 连接数: `mysql_global_status_threads_connected`
- QPS: `rate(mysql_global_status_questions[1m])`

### 3. 告警规则

```yaml
groups:
  - name: feed-system-alerts
    rules:
      # 应用存活
      - alert: ApplicationDown
        expr: up{job="feed-app"} == 0
        for: 1m
        annotations:
          summary: "Feed应用不可用"

      # JVM内存告警
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.85
        for: 5m
        annotations:
          summary: "JVM堆内存使用率超过85%"

      # Redis内存告警
      - alert: RedisHighMemory
        expr: redis_memory_used_bytes / redis_memory_max_bytes > 0.9
        for: 5m
        annotations:
          summary: "Redis内存使用率超过90%"

      # API响应时间告警
      - alert: HighApiLatency
        expr: http_server_requests_seconds_sum / http_server_requests_seconds_count > 1
        for: 5m
        annotations:
          summary: "API平均响应时间超过1秒"
```

---

## 故障排查

### 1. 应用无法启动

```bash
# 检查日志
docker-compose logs feed-app

# 常见问题
- MySQL连接失败: 检查数据库是否启动，用户名密码是否正确
- Redis连接失败: 检查Redis是否启动
- 端口被占用: netstat -tlnp | grep 8080
```

### 2. 内存溢出

```bash
# 分析heap dump
jhat /app/logs/heapdump.hprof

# 或使用MAT工具分析
```

### 3. 性能问题

```bash
# 查看线程栈
jstack <pid> > thread.txt

# 查看GC情况
jstat -gc <pid> 1000

# 查看慢SQL
# 在MySQL中
SELECT * FROM mysql.slow_log ORDER BY query_time DESC LIMIT 10;
```

### 4. Redis问题

```bash
# 连接Redis
redis-cli

# 查看慢日志
SLOWLOG GET 10

# 查看内存使用
INFO memory

# 查看连接数
INFO clients
```

---

## 安全加固

### 1. 网络安全

```bash
# 配置防火墙
sudo ufw allow 8080/tcp
sudo ufw enable

# 仅允许内网访问MySQL和Redis
sudo ufw deny 3306
sudo ufw deny 6379
```

### 2. 应用安全

**启用HTTPS**：

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: your-password
    key-store-type: PKCS12
```

**添加认证**：

```yaml
spring:
  security:
    user:
      name: admin
      password: your-secure-password
```

### 3. 数据库安全

```sql
-- 创建专用用户
CREATE USER 'feed_user'@'%' IDENTIFIED BY 'strong_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON feed_system.* TO 'feed_user'@'%';

-- 禁用root远程登录
DELETE FROM mysql.user WHERE User='root' AND Host NOT IN ('localhost', '127.0.0.1', '::1');
FLUSH PRIVILEGES;
```

### 4. Redis安全

```bash
# redis.conf
requirepass your-strong-password
rename-command FLUSHDB ""
rename-command FLUSHALL ""
bind 127.0.0.1
```

---

## 备份与恢复

### 1. 数据库备份

```bash
# 全量备份
docker exec feed-mysql mysqldump -u root -p feed_system > backup_$(date +%Y%m%d).sql

# 定时备份（crontab）
0 2 * * * /path/to/backup.sh
```

### 2. Redis备份

```bash
# RDB备份
docker exec feed-redis redis-cli SAVE

# 复制备份文件
docker cp feed-redis:/data/dump.rdb ./redis_backup_$(date +%Y%m%d).rdb
```

### 3. 数据恢复

```bash
# MySQL恢复
mysql -u root -p feed_system < backup_20251021.sql

# Redis恢复
docker cp redis_backup.rdb feed-redis:/data/dump.rdb
docker-compose restart redis
```

---

## 扩展性方案

### 1. 应用水平扩展

```bash
# 使用Nginx负载均衡
upstream feed-backend {
    server 192.168.1.10:8080;
    server 192.168.1.11:8080;
    server 192.168.1.12:8080;
}
```

### 2. 数据库分库分表

使用ShardingSphere进行分库分表：
- 按用户ID分片
- 读写分离
- 主从同步

### 3. Redis集群

部署Redis Cluster或Redis Sentinel实现高可用。

---

## 总结

本文档覆盖了Feed流系统的完整生产部署流程。根据实际情况选择Docker部署或手动部署方式，并做好监控、安全加固和备份工作。

如有问题，请查阅日志或联系技术支持。
