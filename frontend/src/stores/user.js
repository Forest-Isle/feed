import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi, refreshToken as refreshTokenApi, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    accessToken: localStorage.getItem('accessToken') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),

  getters: {
    isLoggedIn: (state) => !!state.accessToken,
    userId: (state) => state.userInfo?.id,
    username: (state) => state.userInfo?.username,
    avatar: (state) => state.userInfo?.avatar || '/default-avatar.png',
    nickname: (state) => state.userInfo?.nickname
  },

  actions: {
    // 登录
    async login(credentials) {
      const data = await loginApi(credentials)
      this.setAuth(data)
      return data
    },

    // 注册
    async register(userData) {
      const data = await registerApi(userData)
      this.setAuth(data)
      return data
    },

    // 刷新Token
    async refreshToken() {
      const data = await refreshTokenApi(this.refreshToken)
      this.setAuth(data)
      return data.accessToken
    },

    // 登出
    async logout() {
      try {
        await logoutApi()
      } catch (error) {
        console.error('登出失败：', error)
      } finally {
        this.clearAuth()
      }
    },

    // 设置认证信息
    setAuth(data) {
      this.accessToken = data.accessToken
      this.refreshToken = data.refreshToken
      this.userInfo = data.user

      localStorage.setItem('accessToken', data.accessToken)
      localStorage.setItem('refreshToken', data.refreshToken)
      localStorage.setItem('userInfo', JSON.stringify(data.user))
    },

    // 清除认证信息
    clearAuth() {
      this.accessToken = ''
      this.refreshToken = ''
      this.userInfo = null

      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
    },

    // 更新用户信息
    updateUserInfo(userInfo) {
      this.userInfo = { ...this.userInfo, ...userInfo }
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    }
  }
})
