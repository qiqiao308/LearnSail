<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const enrollments = ref([])
const loading = ref(false)

const pagination = reactive({
  pageNum: 1,
  pageSize: 12,
  total: 0
})

async function loadEnrollments() {
  loading.value = true
  try {
    const data = await request.post('/enrollment/loadMyEnrollments', {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    enrollments.value = data.list || []
    pagination.total = data.total || 0
  } catch (e) {
    ElMessage.error('加载我的课程失败')
  } finally {
    loading.value = false
  }
}

function goToDetail(courseId) {
  router.push(`/courses/${courseId}`)
}

function continueLearning(enrollment) {
  router.push(`/courses/${enrollment.courseId}`)
}

function handlePageChange(page) {
  pagination.pageNum = page
  loadEnrollments()
}

function getProgressStatus(progress) {
  if (progress >= 100) return 'success'
  if (progress > 0) return ''
  return 'warning'
}

const statusMap = { ENROLLED: '学习中', COMPLETED: '已完成', DROPPED: '已退出' }

function getStatusType(status) {
  const map = { ENROLLED: 'primary', COMPLETED: 'success', DROPPED: 'info' }
  return map[status] || 'info'
}

onMounted(() => {
  loadEnrollments()
})
</script>

<template>
  <div class="my-enrollments">
    <div class="page-header">
      <h2>我的课程</h2>
      <p>共 {{ pagination.total }} 门课程</p>
    </div>

    <el-empty v-if="!loading && enrollments.length === 0" description="还没有加入任何课程">
      <el-button type="primary" @click="router.push('/courses')">去选课</el-button>
    </el-empty>

    <div v-loading="loading" class="enrollment-grid">
      <el-row :gutter="20">
        <el-col
          v-for="item in enrollments"
          :key="item.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          style="margin-bottom: 20px"
        >
          <el-card
            :body-style="{ padding: 0 }"
            shadow="hover"
            class="enrollment-card"
          >
            <div class="card-cover" @click="goToDetail(item.courseId)">
              <img v-if="item.courseCover" :src="item.courseCover" alt="封面" />
              <div v-else class="cover-placeholder">
                <el-icon :size="48"><Reading /></el-icon>
              </div>
              <el-tag
                class="status-tag"
                :type="getStatusType(item.status)"
              >
                {{ statusMap[item.status] || item.status }}
              </el-tag>
            </div>
            <div class="card-body">
              <h3 class="card-title" @click="goToDetail(item.courseId)">{{ item.courseTitle }}</h3>
              <div class="card-meta">
                <span>
                  <el-icon><User /></el-icon>
                  {{ item.teacherName || '未知教师' }}
                </span>
              </div>
              <div class="progress-section">
                <el-progress
                  :percentage="item.progress || 0"
                  :stroke-width="8"
                  :status="getProgressStatus(item.progress)"
                />
                <span class="progress-text">
                  {{ item.completedSections || 0 }}/{{ item.totalSections || 0 }} 节
                </span>
              </div>
              <div class="card-actions">
                <el-button size="small" type="primary" @click="continueLearning(item)">
                  {{ (item.progress || 0) > 0 ? '继续学习' : '开始学习' }}
                </el-button>
                <span class="enroll-time">加入于 {{ item.enrollTime || '-' }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="pagination-wrapper" v-if="pagination.total > pagination.pageSize">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        layout="prev, pager, next, total"
        background
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.my-enrollments {
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
  color: #909399;
  margin: 0;
  font-size: 14px;
}
.enrollment-card {
  border-radius: 10px;
  overflow: hidden;
  transition: transform 0.2s;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.enrollment-card:hover {
  transform: translateY(-4px);
}
.card-cover {
  position: relative;
  height: 160px;
  overflow: hidden;
  background: linear-gradient(135deg, #e0f7fa 0%, #f3e5f5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-placeholder {
  color: #b0bec5;
}
.status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}
.card-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  flex: 1;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}
.card-title:hover {
  color: #409eff;
}
.card-meta {
  display: flex;
  align-items: center;
  color: #909399;
  font-size: 13px;
  margin-bottom: 12px;
}
.card-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}
.progress-section {
  margin-bottom: 12px;
}
.progress-text {
  display: block;
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}
.card-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}
.enroll-time {
  font-size: 12px;
  color: #c0c4cc;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
