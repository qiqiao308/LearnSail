<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()

const enrolledCourses = ref([])
const selectedCourseId = ref(null)
const exams = ref([])
const loadingCourses = ref(false)
const loadingExams = ref(false)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

async function loadEnrolledCourses() {
  loadingCourses.value = true
  try {
    const data = await request.post('/enrollment/loadMyEnrollments', {
      pageNum: 1,
      pageSize: 100
    })
    enrolledCourses.value = (data.list || []).filter(e => e.status !== 'DROPPED')
  } catch (e) {
    ElMessage.error('加载课程列表失败')
  } finally {
    loadingCourses.value = false
  }
}

async function loadExams() {
  if (!selectedCourseId.value) {
    exams.value = []
    return
  }
  loadingExams.value = true
  try {
    const data = await request.post('/exam/loadExamList', {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }, {
      params: { courseId: selectedCourseId.value }
    })
    exams.value = data.list || []
    pagination.total = data.total || 0
  } catch (e) {
    ElMessage.error('加载考试列表失败')
  } finally {
    loadingExams.value = false
  }
}

function handleCourseChange() {
  pagination.pageNum = 1
  loadExams()
}

function handlePageChange(page) {
  pagination.pageNum = page
  loadExams()
}

function startExam(exam) {
  router.push(`/exam/${exam.id}/take`)
}

function viewScore(exam) {
  ElMessage.info('请从通知或成绩页面查看详细成绩')
}

function getExamStatusType(status) {
  const map = { NOT_STARTED: 'info', IN_PROGRESS: 'success', ENDED: 'danger' }
  return map[status] || 'info'
}

function getExamStatusText(status) {
  const map = { NOT_STARTED: '未开始', IN_PROGRESS: '进行中', ENDED: '已结束' }
  return map[status] || status
}

function getMyStatusText(row) {
  if (row.myStatus === 'COMPLETED') return '已完成'
  return '未参加'
}

function getMyStatusType(row) {
  if (row.myStatus === 'COMPLETED') return 'success'
  return 'warning'
}

function canStartExam(row) {
  return row.status === 'IN_PROGRESS' && row.myStatus !== 'COMPLETED'
}

onMounted(() => {
  loadEnrolledCourses()
})
</script>

<template>
  <div class="exam-list-page">
    <div class="page-header">
      <h2>我的考试</h2>
      <p>选择课程查看考试列表</p>
    </div>

    <div class="course-selector">
      <el-select
        v-model="selectedCourseId"
        placeholder="请选择课程"
        size="large"
        style="width: 100%; max-width: 400px"
        :loading="loadingCourses"
        clearable
        @change="handleCourseChange"
      >
        <el-option
          v-for="course in enrolledCourses"
          :key="course.courseId"
          :label="course.courseTitle"
          :value="course.courseId"
        />
      </el-select>
    </div>

    <div v-if="!selectedCourseId" class="empty-hint">
      <el-empty description="请先选择一个课程以查看考试" />
    </div>

    <div v-else v-loading="loadingExams" class="exam-content">
      <el-empty v-if="!loadingExams && exams.length === 0" description="该课程暂无考试" />

      <div v-else class="exam-cards">
        <el-card
          v-for="exam in exams"
          :key="exam.id"
          class="exam-card"
          shadow="hover"
        >
          <div class="exam-header">
            <h3>{{ exam.title }}</h3>
            <el-tag :type="getExamStatusType(exam.status)">
              {{ getExamStatusText(exam.status) }}
            </el-tag>
          </div>
          <div class="exam-meta">
            <div class="meta-item">
              <span class="label">时长</span>
              <span class="value">{{ exam.durationMinutes || 0 }} 分钟</span>
            </div>
            <div class="meta-item">
              <span class="label">总分</span>
              <span class="value">{{ exam.totalScore || 100 }} 分</span>
            </div>
            <div class="meta-item">
              <span class="label">合格分</span>
              <span class="value">{{ exam.passScore || 60 }} 分</span>
            </div>
            <div class="meta-item">
              <span class="label">我的状态</span>
              <el-tag size="small" :type="getMyStatusType(exam)">
                {{ getMyStatusText(exam) }}
              </el-tag>
            </div>
            <div v-if="exam.myScore !== null && exam.myScore !== undefined" class="meta-item">
              <span class="label">我的成绩</span>
              <span class="value score">{{ exam.myScore }} 分</span>
            </div>
          </div>
          <div v-if="exam.startTime" class="exam-time">
            时间：{{ exam.startTime }} ~ {{ exam.endTime }}
          </div>
          <div class="exam-actions">
            <el-button
              v-if="canStartExam(exam)"
              type="primary"
              @click="startExam(exam)"
            >
              开始考试
            </el-button>
            <el-button
              v-else-if="exam.myStatus === 'COMPLETED'"
              type="success"
              plain
              @click="viewScore(exam)"
            >
              查看成绩
            </el-button>
            <el-tag v-else type="info">暂不可参加</el-tag>
          </div>
        </el-card>
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
  </div>
</template>

<style scoped>
.exam-list-page {
  padding: 10px 0;
}
.page-header {
  margin-bottom: 20px;
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
.course-selector {
  margin-bottom: 24px;
}
.empty-hint {
  margin-top: 40px;
}
.exam-content {
  min-height: 200px;
}
.exam-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.exam-card {
  border-radius: 10px;
}
.exam-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.exam-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}
.exam-meta {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.meta-item .label {
  font-size: 12px;
  color: #909399;
}
.meta-item .value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.meta-item .score {
  color: #409eff;
}
.exam-time {
  font-size: 13px;
  color: #909399;
  margin-bottom: 16px;
}
.exam-actions {
  display: flex;
  gap: 10px;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
