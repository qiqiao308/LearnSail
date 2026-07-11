<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const searchKeyword = ref('')

const navItems = [
  { path: '/courses', title: '课程广场' },
  { path: '/my-courses', title: '我的课程', needAuth: true },
  { path: '/announcements', title: '通知公告' }
]

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/courses', query: { keyword: searchKeyword.value.trim() } })
  }
}

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'dashboard') {
    router.push('/dashboard')
  }
}
</script>

<template>
  <div class="web-layout">
    <el-header class="web-header">
      <div class="header-content">
        <div class="header-left" @click="router.push('/')" style="cursor:pointer">
          <img src="/favicon.ico" alt="logo" class="logo-img" />
          <span class="logo-text">智慧校园在线学习</span>
        </div>

        <div class="header-center">
          <el-menu
            mode="horizontal"
            :default-active="route.path"
            router
            class="nav-menu"
          >
            <el-menu-item v-for="item in navItems" :key="item.path" :index="item.path">
              {{ item.title }}
            </el-menu-item>
          </el-menu>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课程..."
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>

        <div class="header-right">
          <template v-if="userStore.isLoggedIn">
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                <el-avatar :size="32" :src="userStore.userInfo?.avatar" />
                <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="dashboard">学习概览</el-dropdown-item>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="router.push('/login')">登录</el-button>
            <el-button @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>

    <el-main class="web-main">
      <router-view />
    </el-main>

    <el-footer class="web-footer">
      <p>© 2026 智慧校园在线学习平台. All rights reserved.</p>
    </el-footer>
  </div>
</template>

<style scoped>
.web-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}
.web-header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  padding: 0;
  height: 64px;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.logo-img {
  width: 36px;
  height: 36px;
}
.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}
.header-center {
  flex: 1;
  display: flex;
  align-items: center;
  margin: 0 30px;
}
.nav-menu {
  border-bottom: none !important;
  margin-right: 20px;
}
.nav-menu .el-menu-item {
  height: 64px;
  line-height: 64px;
}
.search-input {
  width: 280px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.username {
  font-size: 14px;
  color: #333;
}
.web-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 20px auto;
  padding: 0 20px;
}
.web-footer {
  text-align: center;
  background: #fff;
  padding: 20px;
  color: #999;
  font-size: 13px;
}
</style>
