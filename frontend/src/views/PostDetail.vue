<template>
  <div class="post-detail-page">
    <NavigationBar />

    <div class="detail-container" v-if="post">
      <!-- 内容区域 -->
      <div class="content-section">
        <!-- 作者信息 -->
        <div class="author-info">
          <img :src="post.authorAvatar || '/default-avatar.png'" :alt="post.authorName" class="author-avatar">
          <div class="author-details">
            <h3>{{ post.authorName }}</h3>
            <p>{{ formatDate(post.createdAt) }}</p>
          </div>
          <button class="follow-btn">+ 关注</button>
        </div>

        <!-- 图片展示 -->
        <div class="images-gallery" v-if="post.images && post.images.length > 0">
          <div
            v-for="(image, index) in post.images"
            :key="index"
            class="image-item"
            @click="viewImage(index)"
          >
            <img :src="image" :alt="`图片${index + 1}`">
          </div>
        </div>

        <!-- 内容文本 -->
        <div class="post-content">
          <h2>{{ post.title || '分享生活' }}</h2>
          <p>{{ post.content }}</p>

          <!-- 话题标签 -->
          <div class="topic-tags" v-if="post.topic">
            <span class="tag">#{{ post.topic }}</span>
          </div>
        </div>

        <!-- 互动按钮 -->
        <div class="interaction-bar">
          <button class="interact-btn" @click="handleLike">
            <i :class="['fas fa-heart', { liked: isLiked }]"></i>
            <span>{{ post.likeCount || 0 }}</span>
          </button>
          <button class="interact-btn">
            <i class="fas fa-comment"></i>
            <span>{{ post.commentCount || 0 }}</span>
          </button>
          <button class="interact-btn">
            <i class="fas fa-star"></i>
            <span>收藏</span>
          </button>
          <button class="interact-btn">
            <i class="fas fa-share"></i>
            <span>分享</span>
          </button>
        </div>

        <!-- 评论区 -->
        <div class="comments-section">
          <h3>评论 {{ post.commentCount || 0 }}</h3>

          <!-- 评论输入框 -->
          <div class="comment-input">
            <img :src="currentUserAvatar" alt="头像" class="comment-avatar">
            <input type="text" placeholder="说说你的看法..." v-model="commentText" @keyup.enter="submitComment">
            <button @click="submitComment" :disabled="!commentText.trim()">发送</button>
          </div>

          <!-- 评论列表 -->
          <div class="comments-list">
            <div class="empty-comments" v-if="comments.length === 0">
              <i class="fas fa-comments"></i>
              <p>暂无评论，快来抢沙发~</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 侧边栏 -->
      <div class="sidebar">
        <!-- 相关推荐 -->
        <div class="recommend-section">
          <h3>相关推荐</h3>
          <div class="recommend-list">
            <div class="recommend-item" v-for="item in recommendPosts" :key="item.id">
              <img :src="item.coverImage || '/default-cover.jpg'" :alt="item.content">
              <p>{{ item.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-state">
      <i class="fas fa-spinner fa-spin"></i>
      <p>加载中...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else class="error-state">
      <i class="fas fa-exclamation-circle"></i>
      <p>内容不存在或已被删除</p>
      <button @click="$router.push('/')">返回首页</button>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPostDetail, likePost } from '@/api/post'
import NavigationBar from '@/components/NavigationBar.vue'

export default {
  name: 'PostDetail',
  components: {
    NavigationBar
  },
  setup() {
    const route = useRoute()
    const userStore = useUserStore()

    const post = ref(null)
    const loading = ref(true)
    const isLiked = ref(false)
    const commentText = ref('')
    const comments = ref([])
    const recommendPosts = ref([])

    const currentUserAvatar = computed(() => userStore.avatar)

    const formatDate = (dateStr) => {
      const date = new Date(dateStr)
      const now = new Date()
      const diff = now - date

      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
      if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
      return date.toLocaleDateString()
    }

    const loadPostDetail = async () => {
      loading.value = true
      try {
        const postId = route.params.id
        const data = await getPostDetail(postId)
        post.value = data

        // 如果有图片字段是JSON字符串，解析它
        if (data.images && typeof data.images === 'string') {
          post.value.images = JSON.parse(data.images)
        }
      } catch (error) {
        console.error('加载内容失败：', error)
      } finally {
        loading.value = false
      }
    }

    const handleLike = async () => {
      try {
        await likePost(post.value.id)
        isLiked.value = !isLiked.value
        post.value.likeCount += isLiked.value ? 1 : -1
      } catch (error) {
        console.error('点赞失败：', error)
      }
    }

    const submitComment = () => {
      if (!commentText.value.trim()) return

      // TODO: 调用评论API
      alert('评论功能待实现')
      commentText.value = ''
    }

    const viewImage = (index) => {
      // TODO: 实现图片预览功能
      console.log('查看图片', index)
    }

    onMounted(() => {
      loadPostDetail()
    })

    return {
      post,
      loading,
      isLiked,
      commentText,
      comments,
      recommendPosts,
      currentUserAvatar,
      formatDate,
      handleLike,
      submitComment,
      viewImage
    }
  }
}
</script>

<style scoped>
.post-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-top: 60px;
}

.detail-container {
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
  display: flex;
  gap: 30px;
}

.content-section {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.author-details {
  flex: 1;
}

.author-details h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #333;
}

.author-details p {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.follow-btn {
  padding: 8px 24px;
  background: #ff2442;
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.follow-btn:hover {
  background: #e6203b;
}

.images-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px;
  margin-bottom: 30px;
}

.image-item {
  position: relative;
  padding-bottom: 100%;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
}

.image-item img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.image-item:hover img {
  transform: scale(1.05);
}

.post-content h2 {
  font-size: 24px;
  margin: 0 0 15px 0;
  color: #333;
}

.post-content p {
  line-height: 1.8;
  color: #666;
  margin-bottom: 20px;
}

.topic-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.tag {
  padding: 6px 15px;
  background: #fff0f3;
  color: #ff2442;
  border-radius: 15px;
  font-size: 13px;
}

.interaction-bar {
  display: flex;
  gap: 20px;
  padding: 20px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  margin: 30px 0;
}

.interact-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: transparent;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
  font-size: 14px;
}

.interact-btn:hover {
  background: #f7f7f7;
}

.interact-btn i.liked {
  color: #ff2442;
}

.comments-section h3 {
  font-size: 18px;
  margin: 0 0 20px 0;
}

.comment-input {
  display: flex;
  gap: 15px;
  align-items: center;
  margin-bottom: 30px;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.comment-input input {
  flex: 1;
  height: 40px;
  padding: 0 15px;
  border: 1px solid #e5e5e5;
  border-radius: 20px;
  outline: none;
}

.comment-input button {
  padding: 8px 24px;
  background: #ff2442;
  color: #fff;
  border: none;
  border-radius: 20px;
  cursor: pointer;
}

.comment-input button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.empty-comments {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-comments i {
  font-size: 48px;
  margin-bottom: 15px;
  display: block;
}

.sidebar {
  width: 320px;
}

.recommend-section {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.recommend-section h3 {
  font-size: 16px;
  margin: 0 0 20px 0;
}

.recommend-item {
  margin-bottom: 15px;
  cursor: pointer;
}

.recommend-item img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 8px;
}

.recommend-item p {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.loading-state, .error-state {
  text-align: center;
  padding: 100px 20px;
  color: #999;
}

.loading-state i, .error-state i {
  font-size: 48px;
  margin-bottom: 20px;
  display: block;
}

.error-state button {
  margin-top: 20px;
  padding: 10px 30px;
  background: #ff2442;
  color: #fff;
  border: none;
  border-radius: 20px;
  cursor: pointer;
}

@media (max-width: 1024px) {
  .sidebar {
    display: none;
  }
}
</style>
