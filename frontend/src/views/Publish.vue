<template>
  <div class="publish-page">
    <NavigationBar />

    <div class="publish-container">
      <div class="publish-card">
        <h1>发布笔记</h1>

        <form @submit.prevent="handlePublish">
          <!-- 图片上传区域 -->
          <div class="image-upload-section">
            <div class="upload-tip">
              <i class="fas fa-image"></i>
              <p>上传图片（最多9张）</p>
              <span>支持拖拽上传</span>
            </div>

            <div class="image-preview" v-if="imageUrls.length > 0">
              <div v-for="(url, index) in imageUrls" :key="index" class="preview-item">
                <img :src="url" alt="预览图">
                <button type="button" class="remove-btn" @click="removeImage(index)">
                  <i class="fas fa-times"></i>
                </button>
              </div>
              <label v-if="imageUrls.length < 9" class="add-more">
                <i class="fas fa-plus"></i>
                <input type="file" accept="image/*" @change="handleImageUpload" hidden>
              </label>
            </div>

            <label v-else class="upload-box">
              <i class="fas fa-cloud-upload-alt"></i>
              <span>点击或拖拽上传图片</span>
              <input type="file" accept="image/*" @change="handleImageUpload" hidden multiple>
            </label>
          </div>

          <!-- 内容输入 -->
          <div class="content-section">
            <textarea
              v-model="formData.content"
              placeholder="分享你的生活... (最少10个字)"
              maxlength="1000"
              required
            ></textarea>
            <div class="char-count">{{ formData.content.length }}/1000</div>
          </div>

          <!-- 话题选择 -->
          <div class="topic-section">
            <label>
              <i class="fas fa-hashtag"></i>
              添加话题
            </label>
            <input
              type="text"
              v-model="formData.topic"
              placeholder="如：美食、旅行、摄影..."
            >
          </div>

          <!-- 发布按钮 -->
          <div class="action-buttons">
            <button type="button" class="cancel-btn" @click="handleCancel">
              取消
            </button>
            <button type="submit" class="publish-btn" :disabled="loading || !canPublish">
              <span v-if="!loading">发布</span>
              <span v-else>
                <i class="fas fa-spinner fa-spin"></i>
                发布中...
              </span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { publishPost } from '@/api/post'
import NavigationBar from '@/components/NavigationBar.vue'

export default {
  name: 'Publish',
  components: {
    NavigationBar
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const imageUrls = ref([])

    const formData = reactive({
      content: '',
      topic: '',
      images: []
    })

    const canPublish = computed(() => {
      return formData.content.length >= 10
    })

    const handleImageUpload = (e) => {
      const files = Array.from(e.target.files)
      files.forEach(file => {
        if (imageUrls.value.length >= 9) return

        const reader = new FileReader()
        reader.onload = (e) => {
          imageUrls.value.push(e.target.result)
          formData.images.push(e.target.result)
        }
        reader.readAsDataURL(file)
      })
    }

    const removeImage = (index) => {
      imageUrls.value.splice(index, 1)
      formData.images.splice(index, 1)
    }

    const handlePublish = async () => {
      if (!canPublish.value) {
        alert('内容至少需要10个字')
        return
      }

      loading.value = true

      try {
        await publishPost({
          content: formData.content,
          topic: formData.topic || null,
          images: formData.images.length > 0 ? JSON.stringify(formData.images) : null
        })

        alert('发布成功！')
        router.push('/')
      } catch (error) {
        alert(error.message || '发布失败')
      } finally {
        loading.value = false
      }
    }

    const handleCancel = () => {
      if (confirm('确定要取消发布吗？')) {
        router.back()
      }
    }

    return {
      loading,
      imageUrls,
      formData,
      canPublish,
      handleImageUpload,
      removeImage,
      handlePublish,
      handleCancel
    }
  }
}
</script>

<style scoped>
.publish-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-top: 60px;
}

.publish-container {
  max-width: 800px;
  margin: 40px auto;
  padding: 20px;
}

.publish-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.publish-card h1 {
  font-size: 28px;
  margin: 0 0 30px 0;
  color: #333;
}

.image-upload-section {
  margin-bottom: 30px;
}

.upload-tip {
  text-align: center;
  padding: 20px;
  color: #999;
}

.upload-tip i {
  font-size: 48px;
  margin-bottom: 10px;
  display: block;
}

.upload-tip p {
  font-size: 16px;
  margin: 10px 0 5px 0;
}

.upload-tip span {
  font-size: 12px;
}

.upload-box {
  display: block;
  border: 2px dashed #e5e5e5;
  border-radius: 12px;
  padding: 60px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-box:hover {
  border-color: #ff2442;
  background: #fff5f7;
}

.upload-box i {
  font-size: 48px;
  color: #ccc;
  display: block;
  margin-bottom: 15px;
}

.image-preview {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
}

.preview-item {
  position: relative;
  padding-bottom: 100%;
  border-radius: 8px;
  overflow: hidden;
}

.preview-item img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-more {
  padding-bottom: 100%;
  border: 2px dashed #e5e5e5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 32px;
  color: #ccc;
  transition: all 0.3s;
}

.add-more:hover {
  border-color: #ff2442;
  color: #ff2442;
}

.content-section {
  position: relative;
  margin-bottom: 30px;
}

.content-section textarea {
  width: 100%;
  min-height: 200px;
  padding: 15px;
  border: 2px solid #e5e5e5;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.6;
  resize: vertical;
  outline: none;
  transition: all 0.3s;
}

.content-section textarea:focus {
  border-color: #ff2442;
}

.char-count {
  text-align: right;
  margin-top: 8px;
  color: #999;
  font-size: 12px;
}

.topic-section {
  margin-bottom: 30px;
}

.topic-section label {
  display: block;
  margin-bottom: 10px;
  color: #666;
  font-size: 14px;
}

.topic-section label i {
  margin-right: 5px;
  color: #ff2442;
}

.topic-section input {
  width: 100%;
  height: 45px;
  padding: 0 15px;
  border: 2px solid #e5e5e5;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
}

.topic-section input:focus {
  border-color: #ff2442;
}

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
}

.cancel-btn, .publish-btn {
  padding: 12px 40px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.cancel-btn:hover {
  background: #e5e5e5;
}

.publish-btn {
  background: #ff2442;
  color: #fff;
}

.publish-btn:hover:not(:disabled) {
  background: #e6203b;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 36, 66, 0.4);
}

.publish-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
