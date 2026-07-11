<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const courseId = ref(route.params.id)
const courseTitle = ref('')

const loading = ref(false)
const chapterList = ref([])
const activeNames = ref([])

// Chapter dialog
const chapterDialogVisible = ref(false)
const chapterDialogTitle = ref('添加章')
const chapterSubmitting = ref(false)
const chapterFormRef = ref(null)
const isEditChapter = ref(false)
const editingChapterId = ref(null)

const chapterForm = reactive({
  courseId: Number(courseId.value),
  title: '',
  sortOrder: 1
})

// Section dialog
const sectionDialogVisible = ref(false)
const sectionDialogTitle = ref('添加节')
const sectionSubmitting = ref(false)
const sectionFormRef = ref(null)
const isEditSection = ref(false)
const editingSectionId = ref(null)
const currentChapterId = ref(null)

const sectionForm = reactive({
  chapterId: null,
  courseId: Number(courseId.value),
  title: '',
  contentType: 'VIDEO',
  contentUrl: '',
  contentText: '',
  duration: 0,
  sortOrder: 1
})

const contentTypeOptions = [
  { label: '视频', value: 'VIDEO' },
  { label: '文档', value: 'DOCUMENT' },
  { label: '富文本', value: 'RICH_TEXT' }
]

function getContentTypeTagType(type) {
  switch (type) {
    case 'VIDEO': return 'primary'
    case 'DOCUMENT': return 'success'
    case 'RICH_TEXT': return 'warning'
    default: return 'info'
  }
}

function getContentTypeLabel(type) {
  switch (type) {
    case 'VIDEO': return '视频'
    case 'DOCUMENT': return '文档'
    case 'RICH_TEXT': return '富文本'
    default: return type || '未知'
  }
}

async function loadCourseInfo() {
  try {
    const data = await request.post('/adminCourse/loadCourseList', {
      pageNum: 1,
      pageSize: 1,
      title: '',
      categoryId: '',
      status: ''
    })
    const list = data.list || []
    const match = list.find(item => String(item.id) === String(courseId.value))
    if (match) {
      courseTitle.value = match.title || ''
    }
  } catch (e) {
    console.error('加载课程信息失败', e)
  }
}

async function loadChapterList() {
  loading.value = true
  try {
    const data = await request.get('/adminChapter/loadChapterList', { params: { courseId: courseId.value } })
    chapterList.value = data || []
    activeNames.value = (data || []).map(item => String(item.id))
  } catch (e) {
    ElMessage.error('加载章节列表失败')
  } finally {
    loading.value = false
  }
}

// ===== Chapter CRUD =====
function handleAddChapter() {
  isEditChapter.value = false
  editingChapterId.value = null
  chapterDialogTitle.value = '添加章'
  chapterForm.title = ''
  chapterForm.sortOrder = chapterList.value.length + 1
  chapterForm.courseId = Number(courseId.value)
  chapterDialogVisible.value = true
}

function handleEditChapter(row) {
  isEditChapter.value = true
  editingChapterId.value = row.id
  chapterDialogTitle.value = '编辑章'
  chapterForm.title = row.title
  chapterForm.sortOrder = row.sortOrder
  chapterForm.courseId = Number(courseId.value)
  chapterDialogVisible.value = true
}

async function handleDeleteChapter(row) {
  try {
    await ElMessageBox.confirm(`确定要删除章「${row.title}」及其下的所有节吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await request.delete('/adminChapter/deleteChapter', { params: { chapterId: row.id } })
    ElMessage.success('删除成功')
    loadChapterList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

async function handleSubmitChapter() {
  if (!chapterFormRef.value) return
  await chapterFormRef.value.validate(async (valid) => {
    if (!valid) return
    chapterSubmitting.value = true
    try {
      if (isEditChapter.value) {
        await request.put('/adminChapter/updateChapter', {
          id: editingChapterId.value,
          ...chapterForm
        })
        ElMessage.success('章更新成功')
      } else {
        await request.post('/adminChapter/saveChapter', chapterForm)
        ElMessage.success('章添加成功')
      }
      chapterDialogVisible.value = false
      loadChapterList()
    } catch (e) {
      ElMessage.error(isEditChapter.value ? '章更新失败' : '章添加失败')
    } finally {
      chapterSubmitting.value = false
    }
  })
}

// ===== Section CRUD =====
function handleAddSection(chapterId) {
  isEditSection.value = false
  editingSectionId.value = null
  currentChapterId.value = chapterId
  sectionDialogTitle.value = '添加节'
  sectionForm.chapterId = chapterId
  sectionForm.courseId = Number(courseId.value)
  sectionForm.title = ''
  sectionForm.contentType = 'VIDEO'
  sectionForm.contentUrl = ''
  sectionForm.contentText = ''
  sectionForm.duration = 0
  sectionForm.sortOrder = 1
  sectionDialogVisible.value = true
}

function handleEditSection(row, chapterId) {
  isEditSection.value = true
  editingSectionId.value = row.id
  currentChapterId.value = chapterId
  sectionDialogTitle.value = '编辑节'
  sectionForm.chapterId = chapterId
  sectionForm.courseId = Number(courseId.value)
  sectionForm.title = row.title || ''
  sectionForm.contentType = row.contentType || 'VIDEO'
  sectionForm.contentUrl = row.contentUrl || ''
  sectionForm.contentText = row.contentText || ''
  sectionForm.duration = row.duration || 0
  sectionForm.sortOrder = row.sortOrder || 1
  sectionDialogVisible.value = true
}

async function handleDeleteSection(row) {
  try {
    await ElMessageBox.confirm(`确定要删除节「${row.title}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await request.delete('/adminChapter/deleteSection', { params: { sectionId: row.id } })
    ElMessage.success('删除成功')
    loadChapterList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

async function handleSubmitSection() {
  if (!sectionFormRef.value) return
  await sectionFormRef.value.validate(async (valid) => {
    if (!valid) return
    sectionSubmitting.value = true
    try {
      const params = { ...sectionForm }
      if (isEditSection.value) {
        params.id = editingSectionId.value
        await request.put('/adminChapter/updateSection', params)
        ElMessage.success('节更新成功')
      } else {
        await request.post('/adminChapter/saveSection', params)
        ElMessage.success('节添加成功')
      }
      sectionDialogVisible.value = false
      loadChapterList()
    } catch (e) {
      ElMessage.error(isEditSection.value ? '节更新失败' : '节添加失败')
    } finally {
      sectionSubmitting.value = false
    }
  })
}

onMounted(() => {
  loadCourseInfo()
  loadChapterList()
})
</script>

<template>
  <div class="chapter-manage">
    <el-card class="header-card">
      <div class="page-header">
        <div class="header-info">
          <h3>章节管理</h3>
          <span class="course-title">课程：{{ courseTitle || '加载中...' }}</span>
        </div>
        <el-button type="primary" @click="handleAddChapter">
          <el-icon><Plus /></el-icon>
          添加章
        </el-button>
      </div>
    </el-card>

    <el-card class="content-card" v-loading="loading">
      <div v-if="chapterList.length === 0" class="empty-state">
        <el-empty description="暂无章节，请添加" />
      </div>
      <el-collapse v-else v-model="activeNames" accordion>
        <el-collapse-item
          v-for="chapter in chapterList"
          :key="chapter.id"
          :name="String(chapter.id)"
        >
          <template #title>
            <div class="chapter-title">
              <span class="chapter-name">{{ chapter.title }}</span>
              <el-tag size="small" type="info" class="chapter-sort">排序: {{ chapter.sortOrder }}</el-tag>
              <span class="section-count">{{ (chapter.sections || []).length }} 节</span>
            </div>
          </template>
          <div class="chapter-actions">
            <el-button type="primary" link size="small" @click.stop="handleAddSection(chapter.id)">
              <el-icon><Plus /></el-icon>
              添加节
            </el-button>
            <el-button type="warning" link size="small" @click.stop="handleEditChapter(chapter)">编辑</el-button>
            <el-button type="danger" link size="small" @click.stop="handleDeleteChapter(chapter)">删除</el-button>
          </div>
          <el-table :data="chapter.sections || []" border stripe size="small" class="section-table">
            <el-table-column prop="title" label="节标题" min-width="180" />
            <el-table-column label="内容类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getContentTypeTagType(row.contentType)" size="small">
                  {{ getContentTypeLabel(row.contentType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="时长(分钟)" width="110" align="center" />
            <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
            <el-table-column label="操作" width="160" align="center">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditSection(row, chapter.id)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDeleteSection(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <!-- Chapter Dialog -->
    <el-dialog v-model="chapterDialogVisible" :title="chapterDialogTitle" width="500px" destroy-on-close>
      <el-form ref="chapterFormRef" :model="chapterForm" label-width="80px">
        <el-form-item label="章标题" prop="title" :rules="[{ required: true, message: '请输入章标题', trigger: 'blur' }]">
          <el-input v-model="chapterForm.title" placeholder="请输入章标题" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="chapterForm.sortOrder" :min="1" :step="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="chapterDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="chapterSubmitting" @click="handleSubmitChapter">确定</el-button>
      </template>
    </el-dialog>

    <!-- Section Dialog -->
    <el-dialog v-model="sectionDialogVisible" :title="sectionDialogTitle" width="600px" destroy-on-close>
      <el-form ref="sectionFormRef" :model="sectionForm" label-width="90px">
        <el-form-item label="节标题" prop="title" :rules="[{ required: true, message: '请输入节标题', trigger: 'blur' }]">
          <el-input v-model="sectionForm.title" placeholder="请输入节标题" />
        </el-form-item>
        <el-form-item label="内容类型" prop="contentType" :rules="[{ required: true, message: '请选择内容类型', trigger: 'change' }]">
          <el-select v-model="sectionForm.contentType" placeholder="请选择内容类型">
            <el-option v-for="item in contentTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="sectionForm.contentType === 'VIDEO' || sectionForm.contentType === 'DOCUMENT'" label="内容URL" prop="contentUrl">
          <el-input v-model="sectionForm.contentUrl" placeholder="请输入内容URL地址" />
        </el-form-item>
        <el-form-item v-if="sectionForm.contentType === 'RICH_TEXT'" label="文本内容" prop="contentText">
          <el-input v-model="sectionForm.contentText" type="textarea" :rows="6" placeholder="请输入富文本内容" />
        </el-form-item>
        <el-form-item label="时长(分钟)" prop="duration">
          <el-input-number v-model="sectionForm.duration" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="sectionForm.sortOrder" :min="1" :step="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sectionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="sectionSubmitting" @click="handleSubmitSection">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.chapter-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.header-card {
  border-radius: 8px;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-info h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
}
.course-title {
  color: #909399;
  font-size: 14px;
}
.content-card {
  border-radius: 8px;
  min-height: 200px;
}
.empty-state {
  padding: 40px 0;
}
.chapter-title {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}
.chapter-name {
  font-weight: 600;
  font-size: 15px;
}
.chapter-sort {
  flex-shrink: 0;
}
.section-count {
  color: #909399;
  font-size: 13px;
  margin-left: auto;
  margin-right: 16px;
}
.chapter-actions {
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 8px;
}
.section-table {
  margin-top: 8px;
}
</style>
