import { defineStore } from 'pinia'
import { getRecommendFeed, getUserFeed } from '@/api/feed'

export const useFeedStore = defineStore('feed', {
  state: () => ({
    feedList: [],
    currentPage: 1,
    pageSize: 20,
    hasMore: true,
    loading: false
  }),

  actions: {
    // 获取推荐Feed
    async fetchRecommendFeed(refresh = false) {
      if (this.loading) return

      if (refresh) {
        this.currentPage = 1
        this.feedList = []
        this.hasMore = true
      }

      this.loading = true

      try {
        const data = await getRecommendFeed({
          page: this.currentPage,
          size: this.pageSize
        })

        if (refresh) {
          this.feedList = data.records || []
        } else {
          this.feedList.push(...(data.records || []))
        }

        this.hasMore = this.currentPage < (data.pages || 1)
        if (this.hasMore) {
          this.currentPage++
        }
      } catch (error) {
        console.error('获取Feed失败：', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 获取用户Feed
    async fetchUserFeed(userId, refresh = false) {
      if (this.loading) return

      if (refresh) {
        this.currentPage = 1
        this.feedList = []
        this.hasMore = true
      }

      this.loading = true

      try {
        const data = await getUserFeed(userId, {
          page: this.currentPage,
          size: this.pageSize
        })

        if (refresh) {
          this.feedList = data.records || []
        } else {
          this.feedList.push(...(data.records || []))
        }

        this.hasMore = this.currentPage < (data.pages || 1)
        if (this.hasMore) {
          this.currentPage++
        }
      } catch (error) {
        console.error('获取用户Feed失败：', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 重置状态
    reset() {
      this.feedList = []
      this.currentPage = 1
      this.hasMore = true
      this.loading = false
    }
  }
})
