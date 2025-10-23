<template>
  <div class="profile-page">
    <NavigationBar />

    <div class="profile-container">
      <!-- 用户信息卡片 -->
      <div class="profile-card">
        <div class="profile-header">
          <div class="avatar-section">
            <img :src="userInfo.avatar || '/default-avatar.png'" :alt="userInfo.nickname" class="user-avatar">
          </div>

          <div class="user-info">
            <h2>{{ userInfo.nickname }}</h2>
            <p class="username">@{{ userInfo.username }}</p>
            <p class="bio" v-if="userInfo.bio">{{ userInfo.bio }}</p>
            <p class="bio" v-else>这个人很懒，什么都没写~</p>

            <div class="user-stats">
              <div class="stat-item">
                <strong>{{ userInfo.followingCount || 0 }}</strong>
                <span>关注</span>
              </div>
              <div class="stat-item">
                <strong>{{ userInfo.followerCount || 0 }}</strong>
                <span>粉丝</span>
              </div>
              <div class="stat-item">
                <strong>{{ userInfo.postCount || 0 }}</strong>
                <span>笔记</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 内容标签页 -->
      <div class="content-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          :class="['tab-btn', { active: activeTab === tab.value }]"
          @click="activeTab = tab.value"
        >
          <i :class="tab.icon"></i>
          {{ tab.label }}
        </button>
      </div>

      <!-- 我的笔记 -->
      <div v-if="activeTab === 'posts'" class="waterfall-container">
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

      <!-- 收藏 -->
      <div v-if="activeTab === 'favorites'" class="empty-state">
        <i class="fas fa-heart"></i>
        <p>暂无收藏</p>
      </div>

      <!-- 点赞 -->
      <div v-if="activeTab === 'likes'" class="empty-state">
        <i class="fas fa-thumbs-up"></i>
        <p>暂无点赞</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useFeedStore } from '@/stores/feed'
import NavigationBar from '@/components/NavigationBar.vue'
import PostCard from '@/components/PostCard.vue'

export default {
  name: 'Profile',
  components: {
    NavigationBar,
    PostCard
  },
  setup() {
    const userStore = useUserStore()
    const feedStore = useFeedStore()

    const activeTab = ref('posts')
    const columnCount = ref(4)

    const tabs = [
      { label: '笔记', value: 'posts', icon: 'fas fa-th' },
      { label: '收藏', value: 'favorites', icon: 'fas fa-heart' },
      { label: '点赞', value: 'likes', icon: 'fas fa-thumbs-up' }
    ]

    const userInfo = computed(() => userStore.userInfo || {})
    const feedList = computed(() => feedStore.feedList)

    const getColumnPosts = (columnIndex) => {
      return feedList.value.filter((_, index) => index % columnCount.value === columnIndex)
    }

    const loadUserPosts = async () => {
      if (userStore.userId) {
        await feedStore.fetchUserFeed(userStore.userId, true)
      }
    }

    onMounted(() => {
      loadUserPosts()
    })

    return {
      activeTab,
      columnCount,
      tabs,
      userInfo,
      feedList,
      getColumnPosts
    }
  }
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-top: 60px;
}

.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.profile-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  margin-bottom: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.profile-header {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

.user-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #ff2442;
}

.user-info {
  flex: 1;
}

.user-info h2 {
  font-size: 28px;
  margin: 0 0 8px 0;
  color: #333;
}

.username {
  color: #999;
  margin: 0 0 15px 0;
}

.bio {
  color: #666;
  margin: 0 0 20px 0;
  line-height: 1.6;
}

.user-stats {
  display: flex;
  gap: 40px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.stat-item strong {
  font-size: 24px;
  color: #333;
}

.stat-item span {
  color: #999;
  font-size: 14px;
}

.content-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
  background: #fff;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.tab-btn {
  flex: 1;
  padding: 12px 20px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.tab-btn:hover {
  background: #f7f7f7;
}

.tab-btn.active {
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

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #999;
  background: #fff;
  border-radius: 16px;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
  display: block;
}

.empty-state p {
  font-size: 16px;
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .user-stats {
    justify-content: center;
  }
}
</style>
