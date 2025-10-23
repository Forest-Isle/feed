# Feed 前端开发指南

## 项目概述

这是一个基于 Vue 3 + Vite 构建的仿小红书风格的社交媒体前端应用，与后端 Spring Boot + JWT 认证系统完美集成。

## 快速开始

### 1. 环境准备

确保已安装：
- Node.js 16.0.0 或更高版本
- npm 7.0.0 或更高版本

### 2. 安装依赖

```bash
cd frontend
npm install
```

### 3. 启动开发服务器

```bash
# 确保后端服务已启动在 http://localhost:8080
npm run dev
```

前端服务将运行在：http://localhost:3000

### 4. 测试流程

#### 步骤1：注册账号

1. 访问 http://localhost:3000
2. 点击导航栏的"登录"按钮
3. 在登录页面切换到"注册"
4. 填写注册信息：
   - 用户名：3-20个字符，只能包含字母、数字、下划线
   - 密码：6-20个字符
   - 邮箱：有效的邮箱地址
   - 昵称：2-20个字符
   - 手机号：可选，11位数字
5. 点击"注册"按钮

#### 步骤2：浏览首页

注册成功后会自动跳转到首页，可以：
- 查看 Feed 流内容（瀑布流布局）
- 切换分类标签
- 滚动加载更多内容
- 点击内容卡片查看详情

#### 步骤3：发布内容

1. 点击导航栏的"发布"按钮
2. 上传图片（可选，最多9张）
3. 填写内容文字（至少10个字）
4. 添加话题标签（可选）
5. 点击"发布"按钮

#### 步骤4：查看个人中心

1. 点击导航栏右侧的头像
2. 选择"个人中心"
3. 查看个人信息和发布的笔记

#### 步骤5：查看内容详情

1. 在首页或个人中心点击任意内容卡片
2. 查看完整的图片、文字、评论
3. 可以点赞、收藏、分享

## 项目架构

### 技术栈

```
Vue 3.3.4          # 核心框架
Vue Router 4.2.4   # 路由管理
Pinia 2.1.6        # 状态管理
Axios 1.5.0        # HTTP 客户端
Vite 4.4.9         # 构建工具
Font Awesome 6.4.0 # 图标库
```

### 目录结构详解

```
frontend/
├── public/                      # 静态资源目录
│   ├── default-avatar.png       # 默认头像（需替换）
│   └── default-cover.jpg        # 默认封面（需替换）
│
├── src/
│   ├── api/                     # API 接口层
│   │   ├── auth.js              # 认证接口（登录、注册、刷新、登出）
│   │   ├── post.js              # 内容接口（发布、详情、点赞）
│   │   ├── feed.js              # Feed流接口（推荐、用户Feed）
│   │   └── follow.js            # 关注接口（关注、取关、列表）
│   │
│   ├── components/              # 公共组件
│   │   ├── NavigationBar.vue    # 顶部导航栏
│   │   └── PostCard.vue         # 内容卡片
│   │
│   ├── router/                  # 路由配置
│   │   └── index.js             # 路由定义 + 守卫
│   │
│   ├── stores/                  # Pinia 状态管理
│   │   ├── user.js              # 用户状态（登录、Token、用户信息）
│   │   └── feed.js              # Feed流状态（列表、分页、加载）
│   │
│   ├── utils/                   # 工具函数
│   │   └── request.js           # Axios 封装（拦截器、Token刷新）
│   │
│   ├── views/                   # 页面组件
│   │   ├── Home.vue             # 首页（Feed流 + 瀑布流）
│   │   ├── Login.vue            # 登录/注册页
│   │   ├── Publish.vue          # 发布内容页
│   │   ├── Profile.vue          # 个人中心页
│   │   └── PostDetail.vue       # 内容详情页
│   │
│   ├── App.vue                  # 根组件
│   └── main.js                  # 应用入口
│
├── index.html                   # HTML 模板
├── package.json                 # 依赖配置
├── vite.config.js               # Vite 配置
└── README.md                    # 项目说明
```

## 核心功能实现

### 1. JWT 认证流程

#### 登录流程

```javascript
// 1. 用户输入账号密码
// 2. 调用登录 API
const response = await login({ username, password })

// 3. 保存 Token 到 localStorage 和 Store
userStore.setAuth(response.data)
// response.data = { accessToken, refreshToken, user }

// 4. 跳转到首页
router.push('/')
```

#### Token 自动刷新

```javascript
// request.js 中的响应拦截器
if (res.code === 1101) { // Token 过期
  // 使用 refreshToken 刷新
  const newToken = await userStore.refreshToken()

  // 重试原请求
  originalRequest.headers.Authorization = `Bearer ${newToken}`
  return request(originalRequest)
}
```

#### 路由守卫

```javascript
router.beforeEach((to, from, next) => {
  const isLoggedIn = userStore.isLoggedIn

  // 需要登录的页面
  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
    return
  }

  // 已登录访问登录页，跳转首页
  if (to.meta.guest && isLoggedIn) {
    next('/')
    return
  }

  next()
})
```

### 2. 瀑布流布局实现

#### 响应式列数

```javascript
// Home.vue
const handleResize = () => {
  const width = window.innerWidth
  if (width >= 1400) columnCount.value = 5
  else if (width >= 1200) columnCount.value = 4
  else if (width >= 768) columnCount.value = 3
  else columnCount.value = 2
}
```

#### 内容分配

```javascript
// 将内容按列索引分配
const getColumnPosts = (columnIndex) => {
  return feedList.value.filter((_, index) =>
    index % columnCount.value === columnIndex
  )
}
```

#### 无限滚动

```javascript
const handleScroll = () => {
  const scrollTop = window.pageYOffset
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight

  // 距离底部 200px 时触发加载
  if (scrollTop + windowHeight >= documentHeight - 200) {
    if (!loading.value && hasMore.value) {
      loadFeed() // 加载下一页
    }
  }
}
```

### 3. 图片上传处理

```javascript
// Publish.vue
const handleImageUpload = (e) => {
  const files = Array.from(e.target.files)

  files.forEach(file => {
    if (imageUrls.value.length >= 9) return

    // 使用 FileReader 转为 base64
    const reader = new FileReader()
    reader.onload = (e) => {
      imageUrls.value.push(e.target.result)
      formData.images.push(e.target.result)
    }
    reader.readAsDataURL(file)
  })
}
```

**注意**：当前使用 base64 编码，生产环境建议：
1. 集成 OSS（阿里云、七牛云等）
2. 图片压缩处理
3. 上传进度显示

### 4. 状态管理（Pinia）

#### 用户状态

```javascript
// stores/user.js
export const useUserStore = defineStore('user', {
  state: () => ({
    accessToken: localStorage.getItem('accessToken') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),

  getters: {
    isLoggedIn: (state) => !!state.accessToken,
    userId: (state) => state.userInfo?.id
  },

  actions: {
    async login(credentials) { /*...*/ },
    async register(userData) { /*...*/ },
    async logout() { /*...*/ }
  }
})
```

#### Feed 状态

```javascript
// stores/feed.js
export const useFeedStore = defineStore('feed', {
  state: () => ({
    feedList: [],
    currentPage: 1,
    hasMore: true,
    loading: false
  }),

  actions: {
    async fetchRecommendFeed(refresh = false) {
      // 刷新时重置分页
      if (refresh) {
        this.currentPage = 1
        this.feedList = []
      }

      // 获取数据
      const data = await getRecommendFeed({
        page: this.currentPage,
        size: 20
      })

      // 追加或替换数据
      if (refresh) {
        this.feedList = data.records
      } else {
        this.feedList.push(...data.records)
      }
    }
  }
})
```

## 样式设计

### 主题色

```css
--primary-color: #ff2442;      /* 小红书红 */
--primary-hover: #e6203b;      /* 悬停色 */
--primary-light: #fff0f3;      /* 浅红色背景 */
--text-primary: #333;          /* 主文本 */
--text-secondary: #666;        /* 次要文本 */
--text-placeholder: #999;      /* 占位文本 */
--border-color: #e5e5e5;       /* 边框色 */
--background: #f5f5f5;         /* 背景色 */
```

### 响应式断点

```css
/* 手机 */
@media (max-width: 768px) { /*...*/ }

/* 平板 */
@media (min-width: 768px) and (max-width: 1024px) { /*...*/ }

/* 桌面 */
@media (min-width: 1024px) { /*...*/ }

/* 大屏 */
@media (min-width: 1400px) { /*...*/ }
```

### 常用动画

```css
/* 淡入 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 下拉展开 */
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```

## API 接口说明

所有接口已在 `src/api/` 目录下封装，直接调用即可：

```javascript
import { login, register } from '@/api/auth'
import { publishPost, getPostDetail } from '@/api/post'
import { getRecommendFeed } from '@/api/feed'

// 使用示例
const data = await login({ username: 'alice', password: '123456' })
```

接口返回格式：

```javascript
// 成功
{
  code: 200,
  message: "操作成功",
  data: { /*...*/ }
}

// 失败
{
  code: 1001,
  message: "用户不存在",
  data: null
}
```

## 常见问题

### Q1: 开发时跨域问题？

A: Vite 已配置代理，确保后端运行在 `http://localhost:8080`。如果端口不同，修改 `vite.config.js`：

```javascript
proxy: {
  '/api': {
    target: 'http://localhost:你的后端端口',
    changeOrigin: true
  }
}
```

### Q2: Token 一直过期？

A: 检查：
1. 后端 JWT 配置的过期时间
2. localStorage 中的 refreshToken 是否有效
3. 清除浏览器缓存重新登录

### Q3: 图片上传失败？

A: 当前是 base64 编码，检查：
1. 图片大小（建议 < 5MB）
2. 后端接口是否支持 JSON 格式的图片数据
3. 浏览器控制台的错误信息

### Q4: 页面刷新后丢失登录状态？

A: 检查 localStorage 是否被清除，userStore 会从 localStorage 恢复状态。

### Q5: 瀑布流布局错乱？

A: 检查：
1. 是否正确计算了列数
2. 浏览器控制台是否有样式警告
3. 图片是否正确加载

## 性能优化建议

### 1. 图片优化

```javascript
// 使用 lazy loading
<img loading="lazy" :src="imageUrl" alt="图片">

// 图片压缩（推荐使用 compressorjs）
import Compressor from 'compressorjs'

new Compressor(file, {
  quality: 0.8,
  success(result) {
    // 上传压缩后的图片
  }
})
```

### 2. 虚拟滚动

对于大量内容，建议使用虚拟滚动：

```bash
npm install vue-virtual-scroller
```

### 3. 路由懒加载

已实现：

```javascript
{
  path: '/',
  component: () => import('@/views/Home.vue')
}
```

### 4. 组件懒加载

```javascript
import { defineAsyncComponent } from 'vue'

const AsyncComponent = defineAsyncComponent(() =>
  import('@/components/HeavyComponent.vue')
)
```

## 部署指南

### 1. 构建生产版本

```bash
npm run build
```

构建产物在 `dist/` 目录。

### 2. Nginx 配置

```nginx
server {
    listen 80;
    server_name yourdomain.com;
    root /path/to/dist;
    index index.html;

    # SPA 路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理到后端
    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### 3. Docker 部署

创建 `Dockerfile`：

```dockerfile
FROM node:16-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 下一步开发

### 优先级功能

1. **评论系统完善**
   - 评论列表展示
   - 回复评论
   - 评论点赞

2. **图片功能增强**
   - 图片预览/放大
   - OSS 上传
   - 图片压缩

3. **搜索功能**
   - 内容搜索
   - 用户搜索
   - 话题搜索

4. **社交功能**
   - 关注/取消关注
   - 粉丝/关注列表
   - 消息通知

### 性能优化

1. 虚拟滚动
2. 图片懒加载
3. 代码分割
4. PWA 支持

---

**开发愉快！** 🚀

如有问题，请查阅：
- Vue 3 文档：https://vuejs.org
- Vite 文档：https://vitejs.dev
- Pinia 文档：https://pinia.vuejs.org
