<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const examList = ref([])
const total = ref(0)
const courseOptions = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  courseId: ''
})

// Exam dialog
const examDialogVisible = ref(false)
const examDialogTitle = ref('创建考试')
const examSubmitting = ref(false)
const examFormRef = ref(null)
const isEditExam = ref(false)

const examForm = reactive({
  id: null,
  courseId: '',
  title: '',
  description: '',
  durationMinutes: 60,
  totalScore: 100,
  passScore: 60,
  startTime: '',
  endTime: ''
})

// Question dialog
const questionDialogVisible = ref(false)
const questionDialogTitle = ref('添加试题')
const questionSubmitting = ref(false)
const questionFormRef = ref(null)
const isEditQuestion = ref(false)
const currentExamId = ref(null)

const questionForm = reactive({
  id: null,
  examId: null,
  questionType: 'SINGLE_CHOICE',
  content: '',
  options: '',
  answer: '',
  score: 5,
  sortOrder: 1
})

const questionList = ref([])
const questionLoading = ref(false)
const questionDialogForExam = ref(false)
const currentExamForQuestions = ref(null)

const questionTypeOptions = [
  { label: '单选题', value: 'SINGLE_CHOICE' },
  { label: '多选题', value: 'MULTIPLE_CHOICE' },
  { label: '判断题', value: 'TRUE_FALSE' },
  { label: '简答题', value: 'SHORT_ANSWER' }
]

function getQuestionTypeLabel(type) {
  const opt = questionTypeOptions.find(o => o.value === type)
  return opt ? opt.label : (type || '未知')
}

function getExamStatus(row) {
  const now = new Date()
  const start = row.startTime ? new Date(row.startTime) : null
  const end = row.endTime ? new Date(row.endTime) : null
  if (!start || !end) return { label: '未设置', type: 'info' }
  if (now < start) return { label: '未开始', type: 'info' }
  if (now > end) return { label: '已结束', type: 'danger' }
  return { label: '进行中', type: 'warning' }
}

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

async function loadExamList() {
  loading.value = true
  try {
    const data = await request.post('/adminExam/loadExamList', queryParams)
    examList.value = data.list || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载考试列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  loadExamList()
}

function handlePageChange(page) {
  queryParams.pageNum = page
  loadExamList()
}

function handleSizeChange(size) {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  loadExamList()
}

// ===== Exam CRUD =====
function handleAddExam() {
  isEditExam.value = false
  examForm.id = null
  examForm.courseId = queryParams.courseId || ''
  examForm.title = ''
  examForm.description = ''
  examForm.durationMinutes = 60
  examForm.totalScore = 100
  examForm.passScore = 60
  examForm.startTime = ''
  examForm.endTime = ''
  examDialogTitle.value = '创建考试'
  examDialogVisible.value = true
}

function handleEditExam(row) {
  isEditExam.value = true
  examForm.id = row.id
  examForm.courseId = row.courseId
  examForm.title = row.title
  examForm.description = row.description || ''
  examForm.durationMinutes = row.durationMinutes || 60
  examForm.totalScore = row.totalScore || 100
  examForm.passScore = row.passScore || 60
  examForm.startTime = row.startTime || ''
  examForm.endTime = row.endTime || ''
  examDialogTitle.value = '编辑考试'
  examDialogVisible.value = true
}

async function handleDeleteExam(row) {
  try {
    await ElMessageBox.confirm(`确定要删除考试「${row.title}」吗？删除后不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await request.delete('/adminExam/deleteExam', { params: { examId: row.id } })
    ElMessage.success('删除成功')
    loadExamList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

async function handleSubmitExam() {
  if (!examFormRef.value) return
  await examFormRef.value.validate(async (valid) => {
    if (!valid) return
    examSubmitting.value = true
    try {
      const params = { ...examForm }
      if (isEditExam.value) {
        await request.post('/adminExam/saveExam', params)
        ElMessage.success('考试更新成功')
      } else {
        await request.post('/adminExam/saveExam', params)
        ElMessage.success('考试创建成功')
      }
      examDialogVisible.value = false
      loadExamList()
    } catch (e) {
      ElMessage.error(isEditExam.value ? '考试更新失败' : '考试创建失败')
    } finally {
      examSubmitting.value = false
    }
  })
}

// ===== Question Management =====
async function handleManageQuestions(row) {
  currentExamForQuestions.value = row
  questionDialogForExam.value = true
  await loadQuestionList(row.id)
}

async function loadQuestionList(examId) {
  questionLoading.value = true
  try {
    const data = await request.get('/adminExam/loadQuestionList', { params: { examId } })
    questionList.value = data || []
  } catch (e) {
    ElMessage.error('加载试题列表失败')
  } finally {
    questionLoading.value = false
  }
}

function handleAddQuestion() {
  isEditQuestion.value = false
  questionForm.id = null
  questionForm.examId = currentExamForQuestions.value.id
  questionForm.questionType = 'SINGLE_CHOICE'
  questionForm.content = ''
  questionForm.options = ''
  questionForm.answer = ''
  questionForm.score = 5
  questionForm.sortOrder = questionList.value.length + 1
  questionDialogTitle.value = '添加试题'
  questionDialogVisible.value = true
}

function handleEditQuestion(row) {
  isEditQuestion.value = true
  questionForm.id = row.id
  questionForm.examId = row.examId
  questionForm.questionType = row.questionType
  questionForm.content = row.content || ''
  questionForm.options = row.options || ''
  questionForm.answer = row.answer || ''
  questionForm.score = row.score || 5
  questionForm.sortOrder = row.sortOrder || 1
  questionDialogTitle.value = '编辑试题'
  questionDialogVisible.value = true
}

async function handleDeleteQuestion(row) {
  try {
    await ElMessageBox.confirm('确定要删除该试题吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await request.delete('/adminExam/deleteQuestion', { params: { questionId: row.id } })
    ElMessage.success('删除成功')
    loadQuestionList(currentExamForQuestions.value.id)
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

async function handleSubmitQuestion() {
  if (!questionFormRef.value) return
  await questionFormRef.value.validate(async (valid) => {
    if (!valid) return
    questionSubmitting.value = true
    try {
      await request.post('/adminExam/saveQuestion', { ...questionForm })
      ElMessage.success(isEditQuestion.value ? '试题更新成功' : '试题添加成功')
      questionDialogVisible.value = false
      loadQuestionList(currentExamForQuestions.value.id)
    } catch (e) {
      ElMessage.error(isEditQuestion.value ? '试题更新失败' : '试题添加失败')
    } finally {
      questionSubmitting.value = false
    }
  })
}

onMounted(() => {
  loadCourseOptions()
  loadExamList()
})
</script>

<template>
  <div class="exam-list">
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="所属课程">
          <el-select v-model="queryParams.courseId" placeholder="请选择课程" clearable filterable style="width: 220px">
            <el-option v-for="opt in courseOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button type="success" @click="handleAddExam">创建考试</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="examList" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="考试标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="courseTitle" label="所属课程" width="150" show-overflow-tooltip />
        <el-table-column prop="durationMinutes" label="时长(分钟)" width="100" align="center" />
        <el-table-column prop="totalScore" label="总分" width="80" align="center" />
        <el-table-column prop="passScore" label="及格分" width="80" align="center" />
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getExamStatus(row).type" size="small">{{ getExamStatus(row).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleManageQuestions(row)">试题管理</el-button>
            <el-button type="warning" link size="small" @click="handleEditExam(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDeleteExam(row)">删除</el-button>
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

    <!-- Exam Dialog -->
    <el-dialog v-model="examDialogVisible" :title="examDialogTitle" width="650px" destroy-on-close>
      <el-form ref="examFormRef" :model="examForm" label-width="100px">
        <el-form-item label="所属课程" prop="courseId" :rules="[{ required: true, message: '请选择课程', trigger: 'change' }]">
          <el-select v-model="examForm.courseId" placeholder="请选择课程" filterable style="width: 100%">
            <el-option v-for="opt in courseOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试标题" prop="title" :rules="[{ required: true, message: '请输入考试标题', trigger: 'blur' }]">
          <el-input v-model="examForm.title" placeholder="请输入考试标题" />
        </el-form-item>
        <el-form-item label="考试描述">
          <el-input v-model="examForm.description" type="textarea" :rows="3" placeholder="请输入考试描述" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="时长(分钟)" prop="durationMinutes">
              <el-input-number v-model="examForm.durationMinutes" :min="1" :step="5" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总分" prop="totalScore">
              <el-input-number v-model="examForm.totalScore" :min="1" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="及格分" prop="passScore">
              <el-input-number v-model="examForm.passScore" :min="0" :step="5" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="examForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="examForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="examDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="examSubmitting" @click="handleSubmitExam">确定</el-button>
      </template>
    </el-dialog>

    <!-- Question Management Dialog -->
    <el-dialog
      v-model="questionDialogForExam"
      :title="`试题管理 - ${currentExamForQuestions?.title || ''}`"
      width="900px"
      destroy-on-close
    >
      <div class="question-toolbar">
        <el-button type="primary" size="small" @click="handleAddQuestion">
          <el-icon><Plus /></el-icon>
          添加试题
        </el-button>
        <span class="question-score-hint">
          试题总分: {{ questionList.reduce((sum, q) => sum + (q.score || 0), 0) }} / {{ currentExamForQuestions?.totalScore || 0 }}
        </span>
      </div>
      <el-table :data="questionList" v-loading="questionLoading" stripe border size="small" style="width: 100%; margin-top: 12px">
        <el-table-column prop="sortOrder" label="排序" width="60" align="center" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ getQuestionTypeLabel(row.questionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="题目内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="answer" label="答案" width="120" show-overflow-tooltip />
        <el-table-column prop="score" label="分值" width="70" align="center" />
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEditQuestion(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDeleteQuestion(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- Question Form Dialog -->
    <el-dialog v-model="questionDialogVisible" :title="questionDialogTitle" width="600px" destroy-on-close>
      <el-form ref="questionFormRef" :model="questionForm" label-width="80px">
        <el-form-item label="题目类型" prop="questionType" :rules="[{ required: true, message: '请选择题目类型', trigger: 'change' }]">
          <el-select v-model="questionForm.questionType" placeholder="请选择题目类型" style="width: 100%">
            <el-option v-for="opt in questionTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" prop="content" :rules="[{ required: true, message: '请输入题目内容', trigger: 'blur' }]">
          <el-input v-model="questionForm.content" type="textarea" :rows="3" placeholder="请输入题目内容" />
        </el-form-item>
        <el-form-item
          v-if="questionForm.questionType === 'SINGLE_CHOICE' || questionForm.questionType === 'MULTIPLE_CHOICE'"
          label="选项(JSON)"
          prop="options"
        >
          <el-input
            v-model="questionForm.options"
            type="textarea"
            :rows="4"
            placeholder='请输入JSON格式选项，如: {"A":"选项A","B":"选项B","C":"选项C","D":"选项D"}'
          />
        </el-form-item>
        <el-form-item label="正确答案" prop="answer" :rules="[{ required: true, message: '请输入正确答案', trigger: 'blur' }]">
          <el-input v-model="questionForm.answer" placeholder="请输入正确答案" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分值" prop="score">
              <el-input-number v-model="questionForm.score" :min="1" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="questionForm.sortOrder" :min="1" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="questionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="questionSubmitting" @click="handleSubmitQuestion">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.exam-list {
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
.question-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.question-score-hint {
  color: #e6a23c;
  font-size: 14px;
  font-weight: 500;
}
</style>
