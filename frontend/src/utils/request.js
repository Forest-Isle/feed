import axios from 'axios'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    const token = userStore.accessToken

    // 如果有token，添加到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  error => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data

    // 请求成功
    if (res.code === 200) {
      return res.data
    }

    // Token过期，尝试刷新
    if (res.code === 1101) {
      return handleTokenExpired(response.config)
    }

    // Token无效或其他认证错误，跳转登录
    if ([1102, 1103, 1104, 1105].includes(res.code)) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      return Promise.reject(new Error(res.message))
    }

    // 其他业务错误
    console.error('业务错误：', res.message)
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => {
    console.error('响应错误：', error)

    if (error.response) {
      const { status } = error.response

      switch (status) {
        case 401:
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          break
        case 403:
          console.error('没有权限访问')
          break
        case 404:
          console.error('请求的资源不存在')
          break
        case 500:
          console.error('服务器错误')
          break
        default:
          console.error('请求失败')
      }
    }

    return Promise.reject(error)
  }
)

// 处理Token过期
let isRefreshing = false
let failedQueue = []

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

async function handleTokenExpired(originalRequest) {
  const userStore = useUserStore()

  if (isRefreshing) {
    // 如果正在刷新token，将请求放入队列
    return new Promise((resolve, reject) => {
      failedQueue.push({ resolve, reject })
    }).then(token => {
      originalRequest.headers.Authorization = `Bearer ${token}`
      return request(originalRequest)
    })
  }

  isRefreshing = true

  try {
    const newToken = await userStore.refreshToken()
    processQueue(null, newToken)
    originalRequest.headers.Authorization = `Bearer ${newToken}`
    return request(originalRequest)
  } catch (error) {
    processQueue(error, null)
    userStore.logout()
    router.push('/login')
    return Promise.reject(error)
  } finally {
    isRefreshing = false
  }
}

export default request
