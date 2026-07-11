<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const assignmentList = ref([])
const total = ref(0)
const courseOptions = ref([])
const chapterOptions = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  courseId: ''
})

// Assignment dialog
const dialogVisible = ref(false)
const dialogTitle = ref('创建作业')
const submitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const assignmentForm = reactive({
  id: null,
  courseId: '',
  chapterId: '',
  title: '',
  description: '',
  deadline: '',
  maxScore: 100
})

// Submission dialog
const submissionDialogVisible = ref(false)
const submissionLoading = ref(false)
const submissionList = ref([])
const submissionTotal = ref(0)
const submissionQuery = reactive({
  pageNum: 1,
  pageSize: 10
})
const currentAssignmentForSubmission = ref(null)

async function loadCourseOptions() {
  try {
    const data = await request.post('/adminCourse/loadCourseList', {
      pageNum: 1,
      pageSize: 500,
      title: '',
      categoryId: '',
      status: ''
    })
    courseOptions.value = (data.list || []).map(item => ({
      label: item.title,
      value: item.id
    }))
  } catch (e) {
    console.error('加载课程列表失败', e)
  }
}

async function loadChapterOptions(courseId) {
  if (!courseId) {
    chapterOptions.value = []
    return
  }
  try {
    const data = await request.get('/adminChapter/loadChapterList', { params: { courseId } })
    const list = data || []
    const result = []
    list.forEach(chapter => {
      result.push({ label: `章: ${chapter.title}`, value: chapter.id })
      ;(chapter.sections || []).forEach(section => {
        result.push({ label: `  └ 节: ${section.title}`, value: section.id })
      })
    })
    chapterOptions.value = result
  } catch (e) {
    console.error('加载章节失败', e)
  }
}

async function loadAssignmentList() {
  loading.value = true
  try {
    const data = await request.post('/adminAssignment/loadAssignmentList', queryParams)
    assignmentList.value = data.list || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载作业列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  loadAssignmentList()
}

function handlePageChange(page) {
  queryParams.pageNum = page
  loadAssignmentList()
}

function handleSizeChange(size) {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  loadAssignmentList()
}

// ===== Assignment CRUD =====
function handleAdd() {
  isEdit.value = false
  assignmentForm.id = null
  assignmentForm.courseId = queryParams.courseId || ''
  assignmentForm.chapterId = ''
  assignmentForm.title = ''
  assignmentForm.description = ''
  assignmentForm.deadline = ''
  assignmentForm.maxScore = 100
  dialogTitle.value = '创建作业'
  dialogVisible.value = true
  loadChapterOptions(assignmentForm.courseId)
}

function handleEdit(row) {
  isEdit.value = true
  assignmentForm.id = row.id
  assignmentForm.courseId = row.courseId
  assignmentForm.chapterId = row.chapterId || ''
  assignmentForm.title = row.title
  assignmentForm.description = row.description || ''
  assignmentForm.deadline = row.deadline || ''
  assignmentForm.maxScore = row.maxScore || 100
  dialogTitle.value = '编辑作业'
  dialogVisible.value = true
  loadChapterOptions(row.courseId)
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除作业「${row.title}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await request.delete('/adminAssignment/deleteAssignment', { params: { assignmentId: row.id } })
    ElMessage.success('删除成功')
    loadAssignmentList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        await request.post('/adminAssignment/saveAssignment', { ...assignmentForm })
        ElMessage.success('作业更新成功')
      } else {
        await request.post('/adminAssignment/saveAssignment', { ...assignmentForm })
        ElMessage.success('作业创建成功')
      }
      dialogVisible.value = false
      loadAssignmentList()
    } catch (e) {
      ElMessage.error(isEdit.value ? '作业更新失败' : '作业创建失败')
    } finally {
      submitting.value = false
    }
  })
}

function onCourseChange(val) {
  assignmentForm.chapterId = ''
  loadChapterOptions(val)
}

// ===== Submission Management =====
async function handleViewSubmissions(row) {
  currentAssignmentForSubmission.value = row
  submissionQuery.pageNum = 1
  submissionDialogVisible.value = true
  await loadSubmissionList()
}

async function loadSubmissionList() {
  if (!currentAssignmentForSubmission.value) return
  submissionLoading.value = true
  try {
    const data = await request.post('/adminAssignment/loadSubmissionList', {
      pageNum: submissionQuery.pageNum,
      pageSize: submissionQuery.pageSize
    }, { params: { assignmentId: currentAssignmentForSubmission.value.id } })
    submissionList.value = data.list || []
    submissionTotal.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载提交列表失败')
  } finally {
    submissionLoading.value = false
  }
}

function handleSubmissionPageChange(page) {
  submissionQuery.pageNum = page
  loadSubmissionList()
}

function handleSubmissionSizeChange(size) {
  submissionQuery.pageSize = size
  submissionQuery.pageNum = 1
  loadSubmissionList()
}

async function handleGradeSubmission(row) {
  try {
    await ElMessageBox.prompt('请输入评分', '批改作业', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: row.score || '',
      inputPlaceholder: '请输入分数',
      inputType: 'number'
    }).then(async ({ value: scoreVal }) => {
      const score = Number(scoreVal)
      if (isNaN(score)) {
        ElMessage.error('请输入有效的分数')
        return
      }
      try {
        await ElMessageBox.prompt('请输入评语（可选）', '批改评语', {
          confirmButtonText: '确定',
          cancelButtonText: '跳过',
          inputValue: row.comment || '',
          inputPlaceholder: '请输入评语'
        }).then(async ({ value: comment }) => {
          await request.put('/adminAssignment/gradeSubmission', {
            submissionId: row.id,
            score,
            comment
          })
          ElMessage.success('批改成功')
          loadSubmissionList()
        }).catch(async (err) => {
          if (err === 'cancel') {
            await request.put('/adminAssignment/gradeSubmission', {
              submissionId: row.id,
              score,
              comment: ''
            })
            ElMessage.success('批改成功')
            loadSubmissionList()
          }
        })
      } catch (e) {
        ElMessage.error('批改失败')
      }
    }).catch(() => {})
  } catch (e) {
    // user cancelled
  }
}

onMounted(() => {
  loadCourseOptions()
  loadAssignmentList()
})
</script>

<template>
  <div class="assignment-list">
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="所属课程">
          <el-select v-model="queryParams.courseId" placeholder="请选择课程" clearable filterable style="width: 220px">
            <el-option v-for="opt in courseOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button type="success" @click="handleAdd">创建作业</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="assignmentList" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="作业标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="courseTitle" label="所属课程" min-width="150" show-overflow-tooltip />
        <el-table-column prop="deadline" label="截止时间" width="160" />
        <el-table-column prop="maxScore" label="满分" width="80" align="center" />
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewSubmissions(row)">查看提交</el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- Assignment Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="assignmentForm" label-width="90px">
        <el-form-item label="所属课程" prop="courseId" :rules="[{ required: true, message: '请选择课程', trigger: 'change' }]">
          <el-select v-model="assignmentForm.courseId" placeholder="请选择课程" filterable style="width: 100%" @change="onCourseChange">
            <el-option v-for="opt in courseOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属章节">
          <el-select v-model="assignmentForm.chapterId" placeholder="请选择章节（可选）" filterable style="width: 100%" clearable>
            <el-option v-for="opt in chapterOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="作业标题" prop="title" :rules="[{ required: true, message: '请输入作业标题', trigger: 'blur' }]">
          <el-input v-model="assignmentForm.title" placeholder="请输入作业标题" />
        </el-form-item>
        <el-form-item label="作业描述">
          <el-input v-model="assignmentForm.description" type="textarea" :rows="4" placeholder="请输入作业描述" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="截止时间" prop="deadline">
              <el-date-picker
                v-model="assignmentForm.deadline"
                type="datetime"
                placeholder="选择截止时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="满分" prop="maxScore">
              <el-input-number v-model="assignmentForm.maxScore" :min="1" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Submission Dialog -->
    <el-dialog
      v-model="submissionDialogVisible"
      :title="`提交记录 - ${currentAssignmentForSubmission?.title || ''}`"
      width="900px"
      destroy-on-close
    >
      <el-table :data="submissionList" v-loading="submissionLoading" stripe border size="small" style="width: 100%">
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="content" label="提交内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="附件" width="120" align="center">
          <template #default="{ row }">
            <el-button v-if="row.fileUrl" type="primary" link size="small" @click="window.open(row.fileUrl)">
              下载
            </el-button>
            <span v-else class="no-file">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="160" />
        <el-table-column label="得分" width="110" align="center">
          <template #default="{ row }">
            <span v-if="row.score != null">{{ row.score }}/{{ currentAssignmentForSubmission?.maxScore }}</span>
            <el-tag v-else type="info" size="small">未批改</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评语" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleGradeSubmission(row)">批改</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap" style="margin-top: 12px;">
        <el-pagination
          v-model:current-page="submissionQuery.pageNum"
          v-model:page-size="submissionQuery.pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="submissionTotal"
          layout="total, sizes, prev, pager, next"
          @current-change="handleSubmissionPageChange"
          @size-change="handleSubmissionSizeChange"
        />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.assignment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.search-card {
  border-radius: 8px;
}
.table-card {
  border-radius: 8px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.no-file {
  color: #c0c4cc;
  font-size: 12px;
}
</style>
