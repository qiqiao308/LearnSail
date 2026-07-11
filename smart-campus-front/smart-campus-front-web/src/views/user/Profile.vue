<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const activeTab = ref('profile')

// Profile form
const profileForm = reactive({
  realName: '',
  email: '',
  phone: '',
  avatar: ''
})

const profileLoading = ref(false)
const profileSaving = ref(false)

// Password form
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordSaving = ref(false)

// Avatar upload
const avatarUploadLoading = ref(false)
const avatarUrl = ref('')

async function loadProfile() {
  profileLoading.value = true
  try {
    await userStore.fetchUserInfo()
    const info = userStore.userInfo
    profileForm.realName = info?.realName || ''
    profileForm.email = info?.email || ''
    profileForm.phone = info?.phone || ''
    profileForm.avatar = info?.avatar || ''
    avatarUrl.value = info?.avatar || ''
  } catch (e) {
    ElMessage.error('加载用户信息失败')
  } finally {
    profileLoading.value = false
  }
}

async function handleUpdateProfile() {
  if (!profileForm.realName.trim()) {
    ElMessage.warning('请输入真实姓名')
    return
  }

  profileSaving.value = true
  try {
    await request.put('/user/updateProfile', {
      realName: profileForm.realName,
      email: profileForm.email,
      phone: profileForm.phone,
      avatar: avatarUrl.value
    })
    ElMessage.success('个人信息更新成功')
    userStore.fetchUserInfo()
  } catch (e) {
    ElMessage.error(e.message || '更新失败')
  } finally {
    profileSaving.value = false
  }
}

async function handleChangePassword() {
  if (!passwordForm.oldPassword) {
    ElMessage.warning('请输入旧密码')
    return
  }
  if (!passwordForm.newPassword || passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  passwordSaving.value = true
  try {
    await request.put('/user/changePassword', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (e) {
    ElMessage.error(e.message || '密码修改失败')
  } finally {
    passwordSaving.value = false
  }
}

async function handleAvatarUpload(options) {
  avatarUploadLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    const data = await request.post('/file/uploadFile', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    avatarUrl.value = data.url || data.fileUrl || data
    profileForm.avatar = avatarUrl.value
    // Update profile immediately with new avatar
    await request.put('/user/updateProfile', {
      realName: profileForm.realName,
      email: profileForm.email,
      phone: profileForm.phone,
      avatar: avatarUrl.value
    })
    ElMessage.success('头像更新成功')
    userStore.fetchUserInfo()
  } catch (e) {
    ElMessage.error('头像上传失败')
  } finally {
    avatarUploadLoading.value = false
  }
}

const roleLabel = computed(() => {
  const map = { STUDENT: '学生', TEACHER: '教师', ADMIN: '管理员' }
  return map[userStore.role] || userStore.role
})

onMounted(() => {
  loadProfile()
})
</script>

<template>
  <div class="profile-page" v-loading="profileLoading">
    <div class="page-header">
      <h2>个人中心</h2>
    </div>

    <div class="profile-layout">
      <!-- Sidebar card -->
      <div class="profile-sidebar">
        <div class="user-card">
          <div class="avatar-section">
            <el-avatar :size="80" :src="avatarUrl">
              {{ (userStore.userInfo?.realName || userStore.userInfo?.username || '?')[0]?.toUpperCase() }}
            </el-avatar>
            <el-upload
              class="avatar-upload"
              :show-file-list="false"
              :http-request="handleAvatarUpload"
              accept="image/*"
            >
              <el-button text size="small" type="primary" :loading="avatarUploadLoading">
                更换头像
              </el-button>
            </el-upload>
          </div>
          <h3 class="user-name">{{ userStore.userInfo?.realName || userStore.userInfo?.username || '未设置' }}</h3>
          <el-tag>{{ roleLabel }}</el-tag>
          <p class="user-username">@{{ userStore.userInfo?.username }}</p>
        </div>

        <div class="menu-card">
          <div
            class="menu-item"
            :class="{ active: activeTab === 'profile' }"
            @click="activeTab = 'profile'"
          >
            <el-icon><User /></el-icon>
            个人信息
          </div>
          <div
            class="menu-item"
            :class="{ active: activeTab === 'password' }"
            @click="activeTab = 'password'"
          >
            <el-icon><Lock /></el-icon>
            修改密码
          </div>
        </div>
      </div>

      <!-- Main area -->
      <div class="profile-main">
        <!-- Profile Edit -->
        <div v-if="activeTab === 'profile'" class="section-card">
          <h3>个人信息</h3>
          <el-form label-position="top" size="large">
            <el-form-item label="用户名">
              <el-input :model-value="userStore.userInfo?.username" disabled />
            </el-form-item>
            <el-form-item label="真实姓名">
              <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profileSaving" @click="handleUpdateProfile">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- Password Change -->
        <div v-if="activeTab === 'password'" class="section-card">
          <h3>修改密码</h3>
          <el-form label-position="top" size="large" style="max-width: 400px">
            <el-form-item label="旧密码">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="至少6位新密码" show-password />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="passwordSaving" @click="handleChangePassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  padding: 10px 0;
}
.page-header {
  margin-bottom: 24px;
}
.page-header h2 {
  font-size: 24px;
  color: #303133;
  margin: 0;
}
.profile-layout {
  display: flex;
  gap: 24px;
}
.profile-sidebar {
  width: 260px;
  flex-shrink: 0;
}
.user-card {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  text-align: center;
  margin-bottom: 16px;
}
.avatar-section {
  margin-bottom: 16px;
}
.avatar-upload {
  display: block;
  margin-top: 10px;
}
.user-name {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #303133;
}
.user-username {
  margin: 8px 0 0 0;
  font-size: 13px;
  color: #c0c4cc;
}
.menu-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: all 0.2s;
}
.menu-item:hover {
  background: #ecf5ff;
  color: #409eff;
}
.menu-item.active {
  background: #ecf5ff;
  color: #409eff;
  font-weight: 600;
}
.profile-main {
  flex: 1;
  min-width: 0;
}
.section-card {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
}
.section-card h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  color: #303133;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

@media (max-width: 768px) {
  .profile-layout {
    flex-direction: column;
  }
  .profile-sidebar {
    width: 100%;
  }
}
</style>
