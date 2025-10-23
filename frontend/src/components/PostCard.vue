<template>
  <div class="post-card" @click="goToDetail">
    <!-- 封面图片 -->
    <div class="post-cover">
      <img :src="post.coverImage || '/default-cover.jpg'" :alt="post.content">
      <div class="post-overlay">
        <div class="post-stats">
          <span class="stat-item">
            <i class="fas fa-heart"></i>
            {{ formatCount(post.likeCount) }}
          </span>
        </div>
      </div>
    </div>

    <!-- 内容信息 -->
    <div class="post-info">
      <h3 class="post-title">{{ post.content }}</h3>

      <!-- 作者信息 -->
      <div class="post-author">
        <img :src="post.authorAvatar || '/default-avatar.png'" :alt="post.authorName" class="author-avatar">
        <span class="author-name">{{ post.authorName }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'

export default {
  name: 'PostCard',
  props: {
    post: {
      type: Object,
      required: true
    }
  },
  setup(props) {
    const router = useRouter()

    const goToDetail = () => {
      router.push(`/post/${props.post.id}`)
    }

    const formatCount = (count) => {
      if (count >= 10000) {
        return (count / 10000).toFixed(1) + 'w'
      }
      return count || 0
    }

    return {
      goToDetail,
      formatCount
    }
  }
}
</script>

<style scoped>
.post-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.post-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12);
}

.post-cover {
  position: relative;
  width: 100%;
  padding-bottom: 133%;
  overflow: hidden;
  background: #f5f5f5;
}

.post-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.5), transparent);
  padding: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.post-card:hover .post-overlay {
  opacity: 1;
}

.post-stats {
  display: flex;
  gap: 15px;
  color: #fff;
  font-size: 13px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-info {
  padding: 12px;
}

.post-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.author-name {
  font-size: 12px;
  color: #666;
}
</style>
