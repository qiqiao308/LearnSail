import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { setToken, removeToken, setUserInfo, removeUserInfo, getToken, getUserInfo } from '@/utils/auth'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(getUserInfo() || null)

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role || '')
  const userId = computed(() => userInfo.value?.userId || null)

  async function login(loginForm) {
    const data = await request.post('/adminUser/login', loginForm)
    token.value = data.token
    userInfo.value = {
      userId: data.userId,
      username: data.username,
      realName: data.realName,
      role: data.role,
      avatar: data.avatar
    }
    setToken(data.token)
    setUserInfo(userInfo.value)
    return data
  }

  async function fetchUserInfo() {
    try {
      const data = await request.get('/adminUser/getCurrentUser')
      if (data) {
        userInfo.value = {
          userId: data.id,
          username: data.username,
          realName: data.realName,
          role: data.role,
          avatar: data.avatar
        }
        setUserInfo(userInfo.value)
      }
    } catch (e) {
      console.error('获取用户信息失败', e)
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    removeToken()
    removeUserInfo()
  }

  return { token, userInfo, isLoggedIn, role, userId, login, fetchUserInfo, logout }
})
