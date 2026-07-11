<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()

const assignmentId = computed(() => route.params.id)

const assignment = ref(null)
const submission = ref(null)
const loading = ref(false)
const submitLoading = ref(false)
const uploadLoading = ref(false)

const submitForm = reactive({
  content: '',
  fileUrl: ''
})

const fileList = ref([])

async function loadData() {
  loading.value = true
  try {
    const [assignData, subData] = await Promise.all([
      request.post('/assignment/loadAssignmentList', {
        pageNum: 1,
        pageSize: 1
      }, { params: { courseId: '' } }).catch(() => null),
      request.get('/assignment/getMySubmission', { params: { assignmentId: assignmentId.value } }).catch(() => null)
    ])

    // Load assignment detail - we need it from somewhere. Since loadAssignmentList requires courseId,
    // we load it from getMySubmission response or try alternative approach
    try {
      // Try to get assignment data through submission
      const gradeData = await request.get('/assignment/getGrade', {
        params: { assignmentId: assignmentId.value }
      }).catch(() => null)

      if (gradeData) {
        assignment.value = {
          id: assignmentId.value,
          title: gradeData.title || '作业',
          maxScore: gradeData.maxScore || 100,
          description: gradeData.description || '',
          deadline: gradeData.deadline || null,
          myStatus: gradeData.myStatus || 'NOT_SUBMITTED',
          myScore: gradeData.myScore,
          comment: gradeData.comment || ''
        }
      }
    } catch (e) {
      // Fallback
      assignment.value = {
        id: assignmentId.value,
        title: '作业',
        maxScore: 100,
        description: '',
        deadline: null,
        myStatus: 'NOT_SUBMITTED'
      }
    }

    submission.value = subData
    if (subData) {
      submitForm.content = subData.content || ''
      submitForm.fileUrl = subData.fileUrl || ''
      if (subData.fileUrl) {
        fileList.value = [{ name: '已上传文件', url: subData.fileUrl }]
      }
    }
  } catch (e) {
    ElMessage.error('加载作业信息失败')
  } finally {
    loading.value = false
  }
}

const isPastDeadline = computed(() => {
  if (!assignment.value || !assignment.value.deadline) return false
  return new Date(assignment.value.deadline) < new Date()
})

const canSubmit = computed(() => {
  return assignment.value &&
    (assignment.value.myStatus === 'NOT_SUBMITTED') &&
    !isPastDeadline.value
})

const isGraded = computed(() => {
  return assignment.value && assignment.value.myStatus === 'GRADED'
})

const isSubmittedNotGraded = computed(() => {
  return assignment.value && assignment.value.myStatus === 'SUBMITTED'
})

async function handleUpload(options) {
  uploadLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    const data = await request.post('/file/uploadFile', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    submitForm.fileUrl = data.url || data.fileUrl || data
    fileList.value = [{ name: options.file.name, url: submitForm.fileUrl }]
    ElMessage.success('文件上传成功')
  } catch (e) {
    ElMessage.error('文件上传失败')
  } finally {
    uploadLoading.value = false
  }
}

function handleRemoveFile() {
  submitForm.fileUrl = ''
  fileList.value = []
}

async function handleSubmit() {
  if (!submitForm.content.trim() && !submitForm.fileUrl) {
    ElMessage.warning('请填写作业内容或上传文件')
    return
  }

  try {
    await ElMessageBox.confirm('确认提交作业？提交后将无法修改。', '确认提交', {
      confirmButtonText: '确认提交',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return
  }

  submitLoading.value = true
  try {
    await request.post('/assignment/submitAssignment', {
      assignmentId: Number(assignmentId.value),
      content: submitForm.content,
      fileUrl: submitForm.fileUrl
    })
    ElMessage.success('作业提交成功')
    assignment.value.myStatus = 'SUBMITTED'
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="assignment-submit" v-loading="loading">
    <div class="page-header">
      <el-button text @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <template v-if="assignment">
      <div class="assignment-info">
        <h2>{{ assignment.title }}</h2>
        <div class="info-row">
          <el-tag type="info">满分: {{ assignment.maxScore }} 分</el-tag>
          <el-tag v-if="assignment.deadline" :type="isPastDeadline ? 'danger' : 'success'">
            截止: {{ assignment.deadline }}
          </el-tag>
        </div>
        <div v-if="assignment.description" class="description">
          <h4>作业描述</h4>
          <div v-html="assignment.description"></div>
        </div>
      </div>

      <!-- Past deadline warning -->
      <el-alert
        v-if="isPastDeadline && assignment.myStatus === 'NOT_SUBMITTED'"
        title="已超过截止时间，无法提交作业"
        type="error"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <!-- Graded view -->
      <div v-if="isGraded" class="graded-view">
        <el-result icon="success" title="作业已批阅">
          <template #sub-title>
            <div class="grade-detail">
              <p>得分：<strong class="score">{{ assignment.myScore }}</strong> / {{ assignment.maxScore }}</p>
              <p v-if="assignment.comment">教师评语：{{ assignment.comment }}</p>
            </div>
          </template>
        </el-result>

        <div class="submission-preview">
          <h4>我的提交</h4>
          <div v-if="submitForm.content" class="preview-content">{{ submitForm.content }}</div>
          <div v-if="submitForm.fileUrl" class="preview-file">
            <el-link type="primary" :href="submitForm.fileUrl" target="_blank">
              <el-icon><Link /></el-icon>
              查看已上传文件
            </el-link>
          </div>
        </div>
      </div>

      <!-- Submitted but not graded -->
      <div v-else-if="isSubmittedNotGraded" class="submitted-view">
        <el-alert title="作业已提交，等待教师批阅" type="success" :closable="false" show-icon style="margin-bottom: 20px" />

        <div class="submission-preview">
          <h4>我的提交</h4>
          <div v-if="submitForm.content" class="preview-content">{{ submitForm.content }}</div>
          <div v-if="submitForm.fileUrl" class="preview-file">
            <el-link type="primary" :href="submitForm.fileUrl" target="_blank">
              <el-icon><Link /></el-icon>
              查看已上传文件
            </el-link>
          </div>
        </div>
      </div>

      <!-- Not submitted -->
      <div v-else-if="canSubmit" class="submit-form">
        <el-form label-position="top">
          <el-form-item label="作业内容">
            <el-input
              v-model="submitForm.content"
              type="textarea"
              :rows="8"
              placeholder="请输入作业内容..."
            />
          </el-form-item>

          <el-form-item label="附件上传">
            <el-upload
              :file-list="fileList"
              :http-request="handleUpload"
              :on-remove="handleRemoveFile"
              :limit="1"
              list-type="text"
            >
              <el-button type="primary" :loading="uploadLoading">
                <el-icon><Upload /></el-icon>
                上传文件
              </el-button>
              <template #tip>
                <div class="upload-tip">支持上传文档、图片等文件</div>
              </template>
            </el-upload>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
              提交作业
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </template>

    <el-empty v-else description="作业不存在" />
  </div>
</template>

<style scoped>
.assignment-submit {
  padding: 10px 0;
  max-width: 800px;
}
.page-header {
  margin-bottom: 16px;
}
.assignment-info {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  margin-bottom: 20px;
}
.assignment-info h2 {
  margin: 0 0 12px 0;
  font-size: 22px;
  color: #303133;
}
.info-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}
.description {
  color: #606266;
  line-height: 1.8;
}
.description h4 {
  margin: 0 0 8px 0;
}
.graded-view, .submitted-view, .submit-form {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
}
.grade-detail p {
  margin: 8px 0;
}
.score {
  color: #67c23a;
  font-size: 20px;
}
.submission-preview {
  margin-top: 16px;
}
.submission-preview h4 {
  margin: 0 0 12px 0;
  color: #303133;
}
.preview-content {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 6px;
  white-space: pre-wrap;
  line-height: 1.6;
  color: #606266;
}
.preview-file {
  margin-top: 12px;
}
.upload-tip {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}
</style>
