# Feed 前端项目

基于 Vue 3 + Vite 构建的仿小红书风格社交媒体前端应用。

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vue Router 4** - 官方路由管理器
- **Pinia** - 新一代状态管理
- **Axios** - HTTP 客户端
- **Vite** - 下一代前端构建工具
- **Font Awesome** - 图标库

## 功能特性

### ✅ 已实现功能

1. **用户认证**
   - 用户注册（用户名、邮箱、密码验证）
   - 用户登录（支持用户名或邮箱登录）
   - JWT Token 自动刷新
   - 登出功能

2. **首页 Feed 流**
   - 瀑布流布局（响应式列数）
   - 无限滚动加载
   - 分类标签切换
   - 内容卡片展示

3. **内容发布**
   - 图片上传（最多9张）
   - 内容编辑（最多1000字）
   - 话题标签
   - 发布状态提示

4. **个人中心**
   - 用户信息展示
   - 关注/粉丝/笔记统计
   - 个人笔记瀑布流
   - 收藏和点赞列表（UI准备）

5. **内容详情**
   - 图片画廊展示
   - 点赞、评论、收藏、分享
   - 评论输入框
   - 相关推荐

6. **全局组件**
   - 顶部导航栏
   - 搜索框
   - 用户下拉菜单
   - 内容卡片组件

## 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API 接口
│   │   ├── auth.js        # 认证相关
│   │   ├── post.js        # 内容相关
│   │   ├── feed.js        # Feed流相关
│   │   └── follow.js      # 关注相关
│   ├── components/        # 公共组件
│   │   ├── NavigationBar.vue   # 导航栏
│   │   └── PostCard.vue        # 内容卡片
│   ├── router/            # 路由配置
│   │   └── index.js
│   ├── stores/            # 状态管理
│   │   ├── user.js        # 用户状态
│   │   └── feed.js        # Feed流状态
│   ├── utils/             # 工具函数
│   │   └── request.js     # Axios 封装
│   ├── views/             # 页面组件
│   │   ├── Home.vue       # 首页
│   │   ├── Login.vue      # 登录/注册
│   │   ├── Publish.vue    # 发布内容
│   │   ├── Profile.vue    # 个人中心
│   │   └── PostDetail.vue # 内容详情
│   ├── App.vue            # 根组件
│   └── main.js            # 入口文件
├── index.html             # HTML 模板
├── package.json           # 依赖配置
├── vite.config.js         # Vite 配置
└── README.md              # 项目说明
```

## 快速开始

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问：http://localhost:3000

### 3. 构建生产版本

```bash
npm run build
```

### 4. 预览生产构建

```bash
npm run preview
```

## 环境要求

- Node.js >= 16.0.0
- npm >= 7.0.0

## 配置说明

### API 代理配置

在 `vite.config.js` 中配置了开发环境的 API 代理：

```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

这样所有 `/api` 开头的请求都会被代理到后端服务器 `http://localhost:8080`。

### 路由模式

使用 HTML5 History 模式，需要服务器配置支持。

生产环境 Nginx 配置示例：

```nginx
location / {
  try_files $uri $uri/ /index.html;
}
```

## 设计特色

### 1. 小红书风格设计

- **配色方案**：主色调为小红书红（#ff2442）
- **瀑布流布局**：响应式多列瀑布流
- **圆角卡片**：现代化的卡片设计
- **悬停效果**：流畅的过渡动画

### 2. 响应式设计

- 大屏（≥1400px）：5列瀑布流
- 中屏（≥1200px）：4列瀑布流
- 平板（≥768px）：3列瀑布流
- 手机（<768px）：2列瀑布流

### 3. 用户体验优化

- 无限滚动加载
- Token 自动刷新
- 路由守卫保护
- 加载状态提示
- 错误处理

## 核心功能说明

### JWT 认证流程

1. **登录/注册** → 获取 `accessToken` 和 `refreshToken`
2. **存储 Token** → localStorage 持久化
3. **请求拦截** → 自动添加 Authorization 头
4. **Token 过期** → 自动使用 refreshToken 刷新
5. **刷新失败** → 跳转登录页

### 状态管理

使用 Pinia 管理全局状态：

- **userStore**：用户信息、登录状态、Token
- **feedStore**：Feed 列表、分页、加载状态

### API 请求封装

`src/utils/request.js` 封装了 Axios，提供：

- 请求/响应拦截
- Token 自动携带
- Token 自动刷新
- 统一错误处理

## 待实现功能

- [ ] 评论功能完整实现
- [ ] 图片预览/放大
- [ ] 搜索功能
- [ ] 关注/取消关注
- [ ] 收藏功能
- [ ] 消息通知
- [ ] 用户资料编辑
- [ ] 图片实际上传（当前为 base64）
- [ ] 懒加载优化
- [ ] PWA 支持

## 注意事项

1. **图片上传**：当前使用 base64 编码，生产环境建议使用 OSS
2. **Token 安全**：生产环境建议使用 HttpOnly Cookie
3. **性能优化**：大量图片时需要实现虚拟滚动
4. **浏览器兼容**：现代浏览器（Chrome/Firefox/Safari/Edge）

## 常见问题

### Q1: 启动后无法访问后端API？

A: 检查后端服务是否启动在 `http://localhost:8080`，或修改 `vite.config.js` 中的代理配置。

### Q2: Token 过期后一直跳转登录？

A: 检查 refreshToken 是否有效，或清除 localStorage 重新登录。

### Q3: 图片上传后过大导致卡顿？

A: 建议添加图片压缩功能，或使用 OSS 上传。

### Q4: 瀑布流列数不响应？

A: 检查浏览器窗口宽度，或查看控制台是否有错误。

## 开发建议

1. **代码规范**：使用 ESLint + Prettier
2. **Git提交**：遵循 Conventional Commits
3. **组件拆分**：保持单一职责原则
4. **性能优化**：使用 Vue DevTools 分析

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT

---

**祝开发愉快！** 🎉
