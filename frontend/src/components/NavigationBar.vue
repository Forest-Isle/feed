<template>
  <nav class="navigation-bar">
    <div class="nav-container">
      <!-- Logo -->
      <router-link to="/" class="logo">
        <i class="fas fa-home"></i>
        <span>Feed</span>
      </router-link>

      <!-- 搜索框 -->
      <div class="search-box">
        <i class="fas fa-search"></i>
        <input type="text" placeholder="搜索你想看的内容..." v-model="searchKeyword" @keyup.enter="handleSearch">
      </div>

      <!-- 导航菜单 -->
      <div class="nav-menu">
        <router-link to="/" class="nav-item" active-class="active">
          <i class="fas fa-home"></i>
          <span>首页</span>
        </router-link>

        <router-link to="/publish" class="nav-item publish-btn" v-if="isLoggedIn">
          <i class="fas fa-plus"></i>
          <span>发布</span>
        </router-link>

        <div v-if="isLoggedIn" class="user-menu">
          <div class="user-avatar" @click="toggleUserMenu">
            <img :src="userAvatar" :alt="username">
          </div>

          <div v-if="showUserMenu" class="user-dropdown">
            <router-link to="/profile" class="dropdown-item">
              <i class="fas fa-user"></i>
              <span>个人中心</span>
            </router-link>
            <div class="dropdown-item" @click="handleLogout">
              <i class="fas fa-sign-out-alt"></i>
              <span>退出登录</span>
            </div>
          </div>
        </div>

        <router-link v-else to="/login" class="nav-item login-btn">
          <span>登录</span>
        </router-link>
      </div>
    </div>
  </nav>
</template>

<script>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

export default {
  name: 'NavigationBar',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()

    const searchKeyword = ref('')
    const showUserMenu = ref(false)

    const isLoggedIn = computed(() => userStore.isLoggedIn)
    const username = computed(() => userStore.username)
    const userAvatar = computed(() => userStore.avatar)

    const handleSearch = () => {
      if (searchKeyword.value.trim()) {
        router.push({ path: '/search', query: { q: searchKeyword.value } })
      }
    }

    const toggleUserMenu = () => {
      showUserMenu.value = !showUserMenu.value
    }

    const handleLogout = async () => {
      await userStore.logout()
      showUserMenu.value = false
      router.push('/login')
    }

    // 点击其他地方关闭菜单
    const handleClickOutside = (e) => {
      if (!e.target.closest('.user-menu')) {
        showUserMenu.value = false
      }
    }

    if (typeof window !== 'undefined') {
      document.addEventListener('click', handleClickOutside)
    }

    return {
      searchKeyword,
      showUserMenu,
      isLoggedIn,
      username,
      userAvatar,
      handleSearch,
      toggleUserMenu,
      handleLogout
    }
  }
}
</script>

<style scoped>
.navigation-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  z-index: 1000;
}

.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  font-weight: bold;
  color: #ff2442;
  text-decoration: none;
}

.logo i {
  font-size: 28px;
}

.search-box {
  flex: 1;
  max-width: 500px;
  margin: 0 40px;
  position: relative;
}

.search-box i {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
}

.search-box input {
  width: 100%;
  height: 40px;
  padding: 0 15px 0 45px;
  border: 1px solid #e5e5e5;
  border-radius: 20px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
}

.search-box input:focus {
  border-color: #ff2442;
  background: #fff;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  color: #333;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s;
}

.nav-item:hover {
  background: #f7f7f7;
}

.nav-item.active {
  color: #ff2442;
  background: #fff0f3;
}

.publish-btn {
  background: #ff2442;
  color: #fff;
}

.publish-btn:hover {
  background: #e6203b;
}

.login-btn {
  background: #ff2442;
  color: #fff;
  padding: 8px 24px;
}

.login-btn:hover {
  background: #e6203b;
}

.user-menu {
  position: relative;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid #ff2442;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-dropdown {
  position: absolute;
  top: 50px;
  right: 0;
  min-width: 160px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  animation: slideDown 0.2s ease;
}

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

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  color: #333;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s;
}

.dropdown-item:hover {
  background: #f7f7f7;
}

.dropdown-item i {
  width: 16px;
  color: #666;
}
</style>
