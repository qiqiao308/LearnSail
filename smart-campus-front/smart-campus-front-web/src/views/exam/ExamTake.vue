<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()

const examId = computed(() => route.params.id)

const examData = ref(null)
const questions = ref([])
const examRecordId = ref(null)
const loading = ref(false)
const submitLoading = ref(false)

// Timer
const totalSeconds = ref(0)
const remainingSeconds = ref(0)
const timerInterval = ref(null)

// Answers: keyed by questionId
const answers = reactive({})

// UI state
const currentQuestionIndex = ref(0)

// Submitted state
const isSubmitted = ref(false)

const remainingTime = computed(() => {
  const mins = Math.floor(remainingSeconds.value / 60)
  const secs = remainingSeconds.value % 60
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
})

const timerPercentage = computed(() => {
  if (totalSeconds.value === 0) return 0
  return Math.round((remainingSeconds.value / totalSeconds.value) * 100)
})

const timerStatus = computed(() => {
  if (remainingSeconds.value < 60) return 'exception'
  if (remainingSeconds.value < 300) return 'warning'
  return ''
})

const currentQuestion = computed(() => {
  return questions.value[currentQuestionIndex.value] || null
})

const answeredCount = computed(() => {
  return Object.keys(answers).filter(key => {
    const val = answers[key]
    if (val === null || val === undefined || val === '') return false
    if (Array.isArray(val)) return val.length > 0
    return true
  }).length
})

function getAnswerStatus(qId) {
  const val = answers[qId]
  if (val === null || val === undefined || val === '') return ''
  if (Array.isArray(val)) return val.length > 0 ? 'answered' : ''
  return 'answered'
}

async function startExam() {
  loading.value = true
  try {
    const data = await request.post('/exam/startExam', null, {
      params: { examId: examId.value }
    })
    examData.value = data
    questions.value = data.questions || []
    examRecordId.value = data.examRecordId

    // Initialize answers
    questions.value.forEach(q => {
      if (q.questionType === 'MULTIPLE_CHOICE') {
        answers[q.id] = []
      } else {
        answers[q.id] = ''
      }
    })

    // Start timer
    const duration = data.durationMinutes || 60
    totalSeconds.value = duration * 60
    remainingSeconds.value = totalSeconds.value
    startTimer()
  } catch (e) {
    ElMessage.error('开始考试失败')
    router.back()
  } finally {
    loading.value = false
  }
}

function startTimer() {
  timerInterval.value = setInterval(() => {
    remainingSeconds.value--
    if (remainingSeconds.value <= 0) {
      clearInterval(timerInterval.value)
      handleAutoSubmit()
    }
  }, 1000)
}

function navigateToQuestion(index) {
  currentQuestionIndex.value = index
}

function goToPrev() {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

function goToNext() {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++
  }
}

function parseOptions(question) {
  // options could be JSON string or object
  if (!question.options) return {}
  if (typeof question.options === 'string') {
    try {
      return JSON.parse(question.options)
    } catch (e) {
      return {}
    }
  }
  return question.options
}

function getOptionKeys(options) {
  return Object.keys(options).sort()
}

function getQuestionTypeLabel(type) {
  const map = {
    SINGLE_CHOICE: '单选题',
    MULTIPLE_CHOICE: '多选题',
    TRUE_FALSE: '判断题',
    SHORT_ANSWER: '简答题'
  }
  return map[type] || type
}

async function handleSubmit() {
  try {
    await ElMessageBox.confirm(
      `你已完成 ${answeredCount.value}/${questions.value.length} 题，确认提交试卷吗？提交后将无法修改。`,
      '确认交卷',
      {
        confirmButtonText: '确认交卷',
        cancelButtonText: '继续答题',
        type: 'warning',
        distinguishCancelAndClose: true
      }
    )
  } catch (e) {
    return
  }

  await doSubmit()
}

async function handleAutoSubmit() {
  ElMessage.warning('考试时间已到，系统将自动交卷')
  await doSubmit()
}

async function doSubmit() {
  if (isSubmitted.value) return
  submitLoading.value = true
  try {
    const answerList = questions.value.map(q => ({
      questionId: q.id,
      userAnswer: formatAnswer(answers[q.id], q.questionType)
    }))

    await request.post('/exam/submitExam', {
      examRecordId: examRecordId.value,
      answers: answerList
    })
    isSubmitted.value = true
    clearInterval(timerInterval.value)
    ElMessage.success('交卷成功')
    router.push('/exams')
  } catch (e) {
    ElMessage.error(e.message || '交卷失败')
  } finally {
    submitLoading.value = false
  }
}

function formatAnswer(answer, questionType) {
  if (questionType === 'MULTIPLE_CHOICE') {
    if (Array.isArray(answer)) return answer.join(',')
    return answer
  }
  return answer
}

onMounted(() => {
  startExam()
})

onUnmounted(() => {
  if (timerInterval.value) {
    clearInterval(timerInterval.value)
  }
})
</script>

<template>
  <div class="exam-take" v-loading="loading">
    <template v-if="questions.length > 0 && !isSubmitted">
      <!-- Top bar -->
      <div class="exam-top-bar">
        <div class="exam-title">{{ examData?.title || '考试中' }}</div>
        <div class="timer-section">
          <el-icon><Clock /></el-icon>
          <span class="timer-text" :class="{ 'timer-warning': remainingSeconds < 300, 'timer-danger': remainingSeconds < 60 }">
            {{ remainingTime }}
          </span>
          <el-progress
            :percentage="timerPercentage"
            :stroke-width="6"
            :status="timerStatus"
            :show-text="false"
            style="width: 120px"
          />
        </div>
        <div class="answer-stats">
          已答：{{ answeredCount }} / {{ questions.length }}
        </div>
        <el-button
          type="danger"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          提交试卷
        </el-button>
      </div>

      <div class="exam-body">
        <!-- Question nav sidebar -->
        <div class="question-nav">
          <div class="nav-title">答题卡</div>
          <div class="nav-grid">
            <div
              v-for="(q, index) in questions"
              :key="q.id"
              class="nav-item"
              :class="{
                active: index === currentQuestionIndex,
                [getAnswerStatus(q.id)]: true
              }"
              @click="navigateToQuestion(index)"
            >
              {{ index + 1 }}
            </div>
          </div>
          <div class="nav-legend">
            <span class="legend-item">
              <span class="legend-dot active-dot"></span> 当前
            </span>
            <span class="legend-item">
              <span class="legend-dot answered-dot"></span> 已答
            </span>
            <span class="legend-item">
              <span class="legend-dot empty-dot"></span> 未答
            </span>
          </div>
        </div>

        <!-- Question area -->
        <div class="question-area">
          <div v-if="currentQuestion" class="question-card">
            <div class="question-header">
              <el-tag>{{ getQuestionTypeLabel(currentQuestion.questionType) }}</el-tag>
              <span class="question-score">{{ currentQuestion.score }} 分</span>
            </div>

            <div class="question-content">
              <h3>{{ currentQuestionIndex + 1 }}. {{ currentQuestion.content }}</h3>
            </div>

            <!-- Single Choice -->
            <div v-if="currentQuestion.questionType === 'SINGLE_CHOICE'" class="options-area">
              <el-radio-group
                v-model="answers[currentQuestion.id]"
                class="option-group"
              >
                <div
                  v-for="key in getOptionKeys(parseOptions(currentQuestion))"
                  :key="key"
                  class="option-item"
                >
                  <el-radio :value="key">
                    {{ key }}. {{ parseOptions(currentQuestion)[key] }}
                  </el-radio>
                </div>
              </el-radio-group>
            </div>

            <!-- Multiple Choice -->
            <div v-else-if="currentQuestion.questionType === 'MULTIPLE_CHOICE'" class="options-area">
              <el-checkbox-group
                v-model="answers[currentQuestion.id]"
                class="option-group"
              >
                <div
                  v-for="key in getOptionKeys(parseOptions(currentQuestion))"
                  :key="key"
                  class="option-item"
                >
                  <el-checkbox :value="key">
                    {{ key }}. {{ parseOptions(currentQuestion)[key] }}
                  </el-checkbox>
                </div>
              </el-checkbox-group>
            </div>

            <!-- True/False -->
            <div v-else-if="currentQuestion.questionType === 'TRUE_FALSE'" class="options-area">
              <el-radio-group
                v-model="answers[currentQuestion.id]"
                class="option-group"
              >
                <div class="option-item">
                  <el-radio value="1">正确</el-radio>
                </div>
                <div class="option-item">
                  <el-radio value="0">错误</el-radio>
                </div>
              </el-radio-group>
            </div>

            <!-- Short Answer -->
            <div v-else-if="currentQuestion.questionType === 'SHORT_ANSWER'" class="options-area">
              <el-input
                v-model="answers[currentQuestion.id]"
                type="textarea"
                :rows="6"
                placeholder="请输入你的答案..."
              />
            </div>

            <!-- Navigation buttons -->
            <div class="question-nav-buttons">
              <el-button :disabled="currentQuestionIndex === 0" @click="goToPrev">
                <el-icon><ArrowLeft /></el-icon>
                上一题
              </el-button>
              <el-button
                v-if="currentQuestionIndex < questions.length - 1"
                type="primary"
                @click="goToNext"
              >
                下一题
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <el-empty v-else-if="!loading" description="考试已结束或无可用的题目" />
  </div>
</template>

<style scoped>
.exam-take {
  min-height: calc(100vh - 150px);
}
.exam-top-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 12px 20px;
  background: #fff;
  border-radius: 10px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  position: sticky;
  top: 84px;
  z-index: 50;
}
.exam-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  flex: 1;
}
.timer-section {
  display: flex;
  align-items: center;
  gap: 8px;
}
.timer-text {
  font-size: 20px;
  font-weight: 700;
  font-family: monospace;
  color: #303133;
  min-width: 64px;
}
.timer-warning {
  color: #e6a23c;
}
.timer-danger {
  color: #f56c6c;
  animation: blink 1s infinite;
}
@keyframes blink {
  50% { opacity: 0.5; }
}
.answer-stats {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
}
.exam-body {
  display: flex;
  gap: 20px;
}
.question-nav {
  width: 200px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  position: sticky;
  top: 156px;
  align-self: flex-start;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}
.nav-title {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  margin-bottom: 12px;
}
.nav-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}
.nav-item {
  width: 30px;
  height: 30px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  color: #606266;
  background: #fff;
  transition: all 0.2s;
}
.nav-item.active {
  border-color: #409eff;
  background: #409eff;
  color: #fff;
}
.nav-item.answered {
  background: #ecf5ff;
  border-color: #b3d8ff;
  color: #409eff;
}
.nav-item.active.answered {
  background: #409eff;
  color: #fff;
}
.nav-legend {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
}
.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  border: 1px solid #dcdfe6;
}
.active-dot {
  background: #409eff;
  border-color: #409eff;
}
.answered-dot {
  background: #ecf5ff;
  border-color: #b3d8ff;
}
.empty-dot {
  background: #fff;
}
.question-area {
  flex: 1;
  min-width: 0;
}
.question-card {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
}
.question-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.question-score {
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
}
.question-content h3 {
  margin: 0 0 24px 0;
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
}
.options-area {
  padding: 0 8px;
}
.option-group {
  display: flex;
  flex-direction: column;
  gap: 0;
}
.option-item {
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid transparent;
  transition: background 0.2s;
}
.option-item:hover {
  background: #f5f7fa;
}
.option-item :deep(.el-radio),
.option-item :deep(.el-checkbox) {
  width: 100%;
}
.question-nav-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

@media (max-width: 768px) {
  .exam-body {
    flex-direction: column;
  }
  .question-nav {
    width: 100%;
    position: static;
    max-height: none;
  }
  .nav-grid {
    grid-template-columns: repeat(10, 1fr);
  }
  .exam-top-bar {
    flex-wrap: wrap;
    gap: 10px;
  }
}
</style>
