<template>
  <div class="home-page">
    <NavigationBar />

    <div class="home-container">
      <!-- 顶部分类标签 -->
      <div class="category-tabs">
        <div class="tabs-wrapper">
          <button
            v-for="category in categories"
            :key="category.value"
            :class="['tab-item', { active: activeCategory === category.value }]"
            @click="changeCategory(category.value)"
          >
            {{ category.label }}
          </button>
        </div>
      </div>

      <!-- 瀑布流内容 -->
      <div class="waterfall-container" ref="waterfallRef">
        <div
          v-for="column in columnCount"
          :key="column"
          class="waterfall-column"
        >
          <PostCard
            v-for="post in getColumnPosts(column - 1)"
            :key="post.id"
            :post="post"
          />
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <i class="fas fa-spinner fa-spin"></i>
        <span>加载中...</span>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && feedList.length === 0" class="empty-state">
        <i class="fas fa-inbox"></i>
        <p>暂无内容</p>
      </div>

      <!-- 无更多内容 -->
      <div v-if="!loading && !hasMore && feedList.length > 0" class="no-more">
        没有更多内容了~
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useFeedStore } from '@/stores/feed'
import NavigationBar from '@/components/NavigationBar.vue'
import PostCard from '@/components/PostCard.vue'

export default {
  name: 'Home',
  components: {
    NavigationBar,
    PostCard
  },
  setup() {
    const feedStore = useFeedStore()
    const waterfallRef = ref(null)
    const columnCount = ref(4)
    const activeCategory = ref('recommend')

    const categories = [
      { label: '推荐', value: 'recommend' },
      { label: '关注', value: 'following' },
      { label: '美食', value: 'food' },
      { label: '旅行', value: 'travel' },
      { label: '时尚', value: 'fashion' },
      { label: '摄影', value: 'photography' }
    ]

    const feedList = computed(() => feedStore.feedList)
    const loading = computed(() => feedStore.loading)
    const hasMore = computed(() => feedStore.hasMore)

    // 计算每列应该显示的内容
    const getColumnPosts = (columnIndex) => {
      return feedList.value.filter((_, index) => index % columnCount.value === columnIndex)
    }

    // 切换分类
    const changeCategory = (category) => {
      activeCategory.value = category
      loadFeed(true)
    }

    // 加载Feed
    const loadFeed = async (refresh = false) => {
      try {
        await feedStore.fetchRecommendFeed(refresh)
      } catch (error) {
        console.error('加载Feed失败：', error)
      }
    }

    // 滚动加载更多
    const handleScroll = () => {
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop
      const windowHeight = window.innerHeight
      const documentHeight = document.documentElement.scrollHeight

      if (scrollTop + windowHeight >= documentHeight - 200) {
        if (!loading.value && hasMore.value) {
          loadFeed()
        }
      }
    }

    // 响应式调整列数
    const handleResize = () => {
      const width = window.innerWidth
      if (width >= 1400) {
        columnCount.value = 5
      } else if (width >= 1200) {
        columnCount.value = 4
      } else if (width >= 768) {
        columnCount.value = 3
      } else {
        columnCount.value = 2
      }
    }

    onMounted(() => {
      handleResize()
      loadFeed(true)
      window.addEventListener('scroll', handleScroll)
      window.addEventListener('resize', handleResize)
    })

    onUnmounted(() => {
      window.removeEventListener('scroll', handleScroll)
      window.removeEventListener('resize', handleResize)
    })

    return {
      waterfallRef,
      columnCount,
      activeCategory,
      categories,
      feedList,
      loading,
      hasMore,
      getColumnPosts,
      changeCategory
    }
  }
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-top: 60px;
}

.home-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.category-tabs {
  background: #fff;
  border-radius: 12px;
  padding: 15px 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 70px;
  z-index: 100;
}

.tabs-wrapper {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  scrollbar-width: none;
}

.tabs-wrapper::-webkit-scrollbar {
  display: none;
}

.tab-item {
  padding: 8px 20px;
  border: none;
  background: transparent;
  border-radius: 20px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s;
}

.tab-item:hover {
  background: #f7f7f7;
}

.tab-item.active {
  background: #ff2442;
  color: #fff;
}

.waterfall-container {
  display: flex;
  gap: 15px;
  align-items: flex-start;
}

.waterfall-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.loading-state {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 14px;
}

.loading-state i {
  font-size: 24px;
  margin-bottom: 10px;
  display: block;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
  display: block;
}

.empty-state p {
  font-size: 16px;
}

.no-more {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 14px;
}

@media (max-width: 768px) {
  .home-container {
    padding: 15px 10px;
  }

  .waterfall-container {
    gap: 10px;
  }

  .waterfall-column {
    gap: 10px;
  }
}
</style>
