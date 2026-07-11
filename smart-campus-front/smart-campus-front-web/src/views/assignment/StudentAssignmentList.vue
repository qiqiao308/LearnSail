<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()

const enrolledCourses = ref([])
const selectedCourseId = ref(null)
const assignments = ref([])
const loadingCourses = ref(false)
const loadingAssignments = ref(false)

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

async function loadAssignments() {
  if (!selectedCourseId.value) {
    assignments.value = []
    return
  }
  loadingAssignments.value = true
  try {
    const data = await request.post('/assignment/loadAssignmentList', {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }, {
      params: { courseId: selectedCourseId.value }
    })
    assignments.value = data.list || []
    pagination.total = data.total || 0
  } catch (e) {
    ElMessage.error('加载作业列表失败')
  } finally {
    loadingAssignments.value = false
  }
}

function handleCourseChange() {
  pagination.pageNum = 1
  loadAssignments()
}

function handlePageChange(page) {
  pagination.pageNum = page
  loadAssignments()
}

function goToSubmit(assignment) {
  router.push(`/assignments/${assignment.id}/submit`)
}

function getStatusTagType(myStatus) {
  const map = { NOT_SUBMITTED: 'warning', SUBMITTED: 'primary', GRADED: 'success' }
  return map[myStatus] || 'info'
}

function getStatusText(myStatus) {
  const map = { NOT_SUBMITTED: '未提交', SUBMITTED: '已提交', GRADED: '已批阅' }
  return map[myStatus] || myStatus
}

onMounted(() => {
  loadEnrolledCourses()
})
</script>

<template>
  <div class="assignment-list-page">
    <div class="page-header">
      <h2>我的作业</h2>
      <p>选择课程查看作业列表</p>
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
      <el-empty description="请先选择一个课程以查看作业" />
    </div>

    <div v-else v-loading="loadingAssignments" class="assignment-content">
      <el-empty v-if="!loadingAssignments && assignments.length === 0" description="该课程暂无作业" />

      <div v-else class="assignment-table">
        <el-table :data="assignments" stripe style="width: 100%">
          <el-table-column prop="title" label="作业标题" min-width="200" />
          <el-table-column prop="maxScore" label="满分" width="80" align="center" />
          <el-table-column label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.myStatus)">
                {{ getStatusText(row.myStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="我的分数" width="100" align="center">
            <template #default="{ row }">
              <span v-if="row.myStatus === 'GRADED'" class="score">{{ row.myScore }} / {{ row.maxScore }}</span>
              <span v-else class="score-pending">-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="goToSubmit(row)">
                {{ row.myStatus === 'GRADED' ? '查看成绩' : row.myStatus === 'SUBMITTED' ? '查看提交' : '去提交' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>

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
  </div>
</template>

<style scoped>
.assignment-list-page {
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
.assignment-content {
  min-height: 200px;
}
.assignment-table {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
}
.score {
  color: #67c23a;
  font-weight: 600;
}
.score-pending {
  color: #c0c4cc;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
