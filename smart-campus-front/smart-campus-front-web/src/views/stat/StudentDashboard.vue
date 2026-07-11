<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const dashboard = ref(null)
const loading = ref(false)

async function loadDashboard() {
  loading.value = true
  try {
    const data = await request.get('/studentStat/getMyDashboard')
    dashboard.value = data
  } catch (e) {
    // Fallback to getLearningStats
    try {
      const data = await request.get('/studentStat/getLearningStats')
      dashboard.value = data
    } catch (e2) {
      ElMessage.error('加载学习数据失败')
    }
  } finally {
    loading.value = false
  }
}

const totalLearningHours = computed(() => {
  const minutes = dashboard.value?.totalLearningMinutes || 0
  if (minutes < 60) return `${minutes} 分钟`
  return `${(minutes / 60).toFixed(1)} 小时`
})

const statsCards = computed(() => [
  {
    title: '在学课程',
    value: dashboard.value?.enrolledCourseCount ?? 0,
    icon: 'Collection',
    color: '#409eff',
    bgColor: '#ecf5ff'
  },
  {
    title: '已完成课程',
    value: dashboard.value?.completedCourseCount ?? 0,
    icon: 'CircleCheckFilled',
    color: '#67c23a',
    bgColor: '#f0f9eb'
  },
  {
    title: '学习时长',
    value: totalLearningHours.value,
    icon: 'Clock',
    color: '#e6a23c',
    bgColor: '#fdf6ec'
  },
  {
    title: '平均成绩',
    value: dashboard.value?.averageScore != null ? `${dashboard.value.averageScore} 分` : '暂无',
    icon: 'DataAnalysis',
    color: '#909399',
    bgColor: '#f4f4f5'
  }
])

const recentEnrollments = computed(() => {
  return dashboard.value?.recentEnrollments || []
})

function goToDetail(courseId) {
  router.push(`/courses/${courseId}`)
}

onMounted(() => {
  loadDashboard()
})
</script>

<template>
  <div class="student-dashboard" v-loading="loading">
    <div class="page-header">
      <h2>学习概览</h2>
      <p>欢迎回来，{{ userStore.userInfo?.realName || userStore.userInfo?.username || '同学' }}</p>
    </div>

    <!-- Stats cards -->
    <div class="stats-grid">
      <el-row :gutter="16">
        <el-col
          v-for="stat in statsCards"
          :key="stat.title"
          :xs="12"
          :sm="12"
          :md="6"
          style="margin-bottom: 16px"
        >
          <div class="stat-card">
            <div class="stat-icon" :style="{ backgroundColor: stat.bgColor, color: stat.color }">
              <el-icon :size="28">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stat.value }}</span>
              <span class="stat-label">{{ stat.title }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Recent enrollments -->
    <div class="section">
      <div class="section-header">
        <h3>最近学习的课程</h3>
        <el-button text type="primary" @click="router.push('/my-courses')">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <el-empty v-if="recentEnrollments.length === 0" description="还没有加入任何课程">
        <el-button type="primary" @click="router.push('/courses')">去选课</el-button>
      </el-empty>

      <el-row v-else :gutter="16">
        <el-col
          v-for="item in recentEnrollments"
          :key="item.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          style="margin-bottom: 16px"
        >
          <el-card
            :body-style="{ padding: 0 }"
            shadow="hover"
            class="recent-card"
            @click="goToDetail(item.courseId)"
          >
            <div class="recent-cover">
              <img v-if="item.courseCover" :src="item.courseCover" alt="封面" />
              <div v-else class="cover-placeholder">
                <el-icon :size="40"><Reading /></el-icon>
              </div>
            </div>
            <div class="recent-body">
              <h4>{{ item.courseTitle }}</h4>
              <el-progress
                :percentage="item.progress || 0"
                :stroke-width="6"
              />
              <span class="recent-progress-text">
                进度 {{ item.progress || 0 }}%
              </span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
.student-dashboard {
  padding: 10px 0;
}
.page-header {
  margin-bottom: 24px;
}
.page-header h2 {
  font-size: 24px;
  color: #303133;
  margin: 0 0 6px 0;
}
.page-header p {
  color: #606266;
  margin: 0;
  font-size: 15px;
}
.stats-grid {
  margin-bottom: 30px;
}
.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  transition: transform 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
.section {
  margin-bottom: 30px;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.section-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}
.recent-card {
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s;
}
.recent-card:hover {
  transform: translateY(-4px);
}
.recent-cover {
  height: 120px;
  background: linear-gradient(135deg, #e0f7fa 0%, #f3e5f5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.recent-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-placeholder {
  color: #b0bec5;
}
.recent-body {
  padding: 14px;
}
.recent-body h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.recent-progress-text {
  font-size: 12px;
  color: #c0c4cc;
  display: block;
  margin-top: 4px;
}
</style>
