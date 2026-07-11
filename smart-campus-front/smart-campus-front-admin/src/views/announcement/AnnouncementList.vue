<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const announcementList = ref([])
const total = ref(0)
const courseOptions = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const dialogVisible = ref(false)
const dialogTitle = ref('创建公告')
const submitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const announcementForm = reactive({
  id: null,
  title: '',
  content: '',
  type: 'SYSTEM',
  courseId: ''
})

const typeOptions = [
  { label: '系统公告', value: 'SYSTEM' },
  { label: '课程公告', value: 'COURSE' }
]

function getTypeTagType(type) {
  switch (type) {
    case 'SYSTEM': return 'danger'
    case 'COURSE': return 'primary'
    default: return 'info'
  }
}

function getTypeLabel(type) {
  switch (type) {
    case 'SYSTEM': return '系统公告'
    case 'COURSE': return '课程公告'
    default: return type || '未知'
  }
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

async function loadAnnouncementList() {
  loading.value = true
  try {
    const data = await request.post('/adminAnnouncement/loadAnnouncementList', queryParams)
    announcementList.value = data.list || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载公告列表失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  queryParams.pageNum = page
  loadAnnouncementList()
}

function handleSizeChange(size) {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  loadAnnouncementList()
}

function handleAdd() {
  isEdit.value = false
  announcementForm.id = null
  announcementForm.title = ''
  announcementForm.content = ''
  announcementForm.type = 'SYSTEM'
  announcementForm.courseId = ''
  dialogTitle.value = '创建公告'
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  announcementForm.id = row.id
  announcementForm.title = row.title || ''
  announcementForm.content = row.content || ''
  announcementForm.type = row.type || 'SYSTEM'
  announcementForm.courseId = row.courseId || ''
  dialogTitle.value = '编辑公告'
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除公告「${row.title}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await request.delete('/adminAnnouncement/deleteAnnouncement', { params: { announcementId: row.id } })
    ElMessage.success('删除成功')
    loadAnnouncementList()
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
      const params = { ...announcementForm }
      if (params.type === 'SYSTEM') {
        params.courseId = ''
      }
      if (isEdit.value) {
        await request.put('/adminAnnouncement/updateAnnouncement', params)
        ElMessage.success('公告更新成功')
      } else {
        await request.post('/adminAnnouncement/saveAnnouncement', params)
        ElMessage.success('公告创建成功')
      }
      dialogVisible.value = false
      loadAnnouncementList()
    } catch (e) {
      ElMessage.error(isEdit.value ? '公告更新失败' : '公告创建失败')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadCourseOptions()
  loadAnnouncementList()
})
</script>

<template>
  <div class="announcement-list">
    <el-card class="toolbar-card">
      <div class="toolbar">
        <span class="page-title">公告管理</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          创建公告
        </el-button>
      </div>
    </el-card>

    <el-card class="table-card">
      <el-table :data="announcementList" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="公告标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="content" label="公告内容" min-width="250" show-overflow-tooltip />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="courseTitle" label="关联课程" width="150" show-overflow-tooltip />
        <el-table-column prop="publisherName" label="发布人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="announcementForm" label-width="80px">
        <el-form-item label="公告标题" prop="title" :rules="[{ required: true, message: '请输入公告标题', trigger: 'blur' }]">
          <el-input v-model="announcementForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告内容" prop="content" :rules="[{ required: true, message: '请输入公告内容', trigger: 'blur' }]">
          <el-input v-model="announcementForm.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>
        <el-form-item label="公告类型" prop="type" :rules="[{ required: true, message: '请选择公告类型', trigger: 'change' }]">
          <el-radio-group v-model="announcementForm.type">
            <el-radio v-for="opt in typeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="announcementForm.type === 'COURSE'"
          label="关联课程"
          prop="courseId"
          :rules="[{ required: true, message: '请选择课程', trigger: 'change' }]"
        >
          <el-select v-model="announcementForm.courseId" placeholder="请选择课程" filterable style="width: 100%">
            <el-option v-for="opt in courseOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.toolbar-card {
  border-radius: 8px;
}
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
}
.table-card {
  border-radius: 8px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
