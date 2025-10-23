<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧背景 -->
      <div class="login-bg">
        <h1>Feed</h1>
        <p>记录美好生活</p>
      </div>

      <!-- 右侧表单 -->
      <div class="login-form-wrapper">
        <div class="login-form">
          <h2>{{ isRegister ? '注册账号' : '欢迎回来' }}</h2>
          <p class="subtitle">{{ isRegister ? '加入我们，开始分享' : '登录继续使用' }}</p>

          <form @submit.prevent="handleSubmit">
            <!-- 用户名 -->
            <div class="form-group">
              <label>
                <i class="fas fa-user"></i>
                <input
                  type="text"
                  v-model="formData.username"
                  :placeholder="isRegister ? '用户名（3-20个字符）' : '用户名或邮箱'"
                  required
                >
              </label>
            </div>

            <!-- 邮箱（仅注册） -->
            <div class="form-group" v-if="isRegister">
              <label>
                <i class="fas fa-envelope"></i>
                <input
                  type="email"
                  v-model="formData.email"
                  placeholder="邮箱"
                  required
                >
              </label>
            </div>

            <!-- 昵称（仅注册） -->
            <div class="form-group" v-if="isRegister">
              <label>
                <i class="fas fa-signature"></i>
                <input
                  type="text"
                  v-model="formData.nickname"
                  placeholder="昵称"
                  required
                >
              </label>
            </div>

            <!-- 手机号（仅注册，可选） -->
            <div class="form-group" v-if="isRegister">
              <label>
                <i class="fas fa-phone"></i>
                <input
                  type="tel"
                  v-model="formData.phone"
                  placeholder="手机号（可选）"
                >
              </label>
            </div>

            <!-- 密码 -->
            <div class="form-group">
              <label>
                <i class="fas fa-lock"></i>
                <input
                  :type="showPassword ? 'text' : 'password'"
                  v-model="formData.password"
                  :placeholder="isRegister ? '密码（6-20个字符）' : '密码'"
                  required
                >
                <i
                  :class="['toggle-password', showPassword ? 'fas fa-eye-slash' : 'fas fa-eye']"
                  @click="showPassword = !showPassword"
                ></i>
              </label>
            </div>

            <!-- 错误提示 -->
            <div v-if="errorMessage" class="error-message">
              <i class="fas fa-exclamation-circle"></i>
              {{ errorMessage }}
            </div>

            <!-- 提交按钮 -->
            <button type="submit" class="submit-btn" :disabled="loading">
              <span v-if="!loading">{{ isRegister ? '注册' : '登录' }}</span>
              <span v-else>
                <i class="fas fa-spinner fa-spin"></i>
                {{ isRegister ? '注册中...' : '登录中...' }}
              </span>
            </button>
          </form>

          <!-- 切换注册/登录 -->
          <div class="switch-mode">
            <span v-if="!isRegister">
              还没有账号？
              <a @click="toggleMode">立即注册</a>
            </span>
            <span v-else>
              已有账号？
              <a @click="toggleMode">立即登录</a>
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()

    const isRegister = ref(false)
    const showPassword = ref(false)
    const loading = ref(false)
    const errorMessage = ref('')

    const formData = reactive({
      username: '',
      password: '',
      email: '',
      nickname: '',
      phone: ''
    })

    const toggleMode = () => {
      isRegister.value = !isRegister.value
      errorMessage.value = ''
      resetForm()
    }

    const resetForm = () => {
      formData.username = ''
      formData.password = ''
      formData.email = ''
      formData.nickname = ''
      formData.phone = ''
    }

    const validateForm = () => {
      if (isRegister.value) {
        if (formData.username.length < 3 || formData.username.length > 20) {
          errorMessage.value = '用户名长度必须在3-20个字符之间'
          return false
        }
        if (formData.password.length < 6 || formData.password.length > 20) {
          errorMessage.value = '密码长度必须在6-20个字符之间'
          return false
        }
        if (!formData.email) {
          errorMessage.value = '请输入邮箱'
          return false
        }
        if (!formData.nickname) {
          errorMessage.value = '请输入昵称'
          return false
        }
      }
      return true
    }

    const handleSubmit = async () => {
      errorMessage.value = ''

      if (!validateForm()) {
        return
      }

      loading.value = true

      try {
        if (isRegister.value) {
          await userStore.register({
            username: formData.username,
            password: formData.password,
            email: formData.email,
            nickname: formData.nickname,
            phone: formData.phone || undefined
          })
        } else {
          await userStore.login({
            username: formData.username,
            password: formData.password
          })
        }

        // 登录成功，跳转到首页
        router.push('/')
      } catch (error) {
        errorMessage.value = error.message || (isRegister.value ? '注册失败' : '登录失败')
      } finally {
        loading.value = false
      }
    }

    return {
      isRegister,
      showPassword,
      loading,
      errorMessage,
      formData,
      toggleMode,
      handleSubmit
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-container {
  display: flex;
  width: 900px;
  max-width: 95%;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-bg {
  flex: 1;
  background: linear-gradient(135deg, #ff2442 0%, #ff6b81 100%);
  color: #fff;
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-bg h1 {
  font-size: 48px;
  margin: 0 0 20px 0;
}

.login-bg p {
  font-size: 20px;
  opacity: 0.9;
}

.login-form-wrapper {
  flex: 1;
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-form {
  width: 100%;
  max-width: 400px;
}

.login-form h2 {
  font-size: 28px;
  margin: 0 0 10px 0;
  color: #333;
}

.subtitle {
  color: #999;
  margin: 0 0 30px 0;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  position: relative;
  display: block;
}

.form-group i {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
}

.form-group input {
  width: 100%;
  height: 50px;
  padding: 0 45px;
  border: 2px solid #e5e5e5;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.3s;
  outline: none;
}

.form-group input:focus {
  border-color: #ff2442;
}

.toggle-password {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  color: #999;
}

.error-message {
  background: #ffebee;
  color: #c62828;
  padding: 12px 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.submit-btn {
  width: 100%;
  height: 50px;
  background: #ff2442;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:hover:not(:disabled) {
  background: #e6203b;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 36, 66, 0.4);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.switch-mode {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.switch-mode a {
  color: #ff2442;
  cursor: pointer;
  text-decoration: none;
  font-weight: bold;
}

.switch-mode a:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .login-bg {
    display: none;
  }

  .login-form-wrapper {
    padding: 40px 20px;
  }
}
</style>
