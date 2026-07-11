<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const courseList = ref([])
const total = ref(0)
const categoryTree = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  categoryId: '',
  status: ''
})

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已关闭', value: 'CLOSED' }
]

function getStatusTagType(status) {
  switch (status) {
    case 'PUBLISHED': return 'success'
    case 'DRAFT': return 'warning'
    case 'CLOSED': return 'danger'
    default: return 'info'
  }
}

function getStatusLabel(status) {
  switch (status) {
    case 'PUBLISHED': return '已发布'
    case 'DRAFT': return '草稿'
    case 'CLOSED': return '已关闭'
    default: return status || '未知'
  }
}

function flattenTree(tree, result = []) {
  tree.forEach(item => {
    result.push({ label: item.name, value: item.id })
    if (item.children && item.children.length > 0) {
      flattenTree(item.children, result)
    }
  })
  return result
}

const categoryOptions = ref([])

async function loadCategoryTree() {
  try {
    const data = await request.get('/adminCategory/loadCategoryTree')
    categoryTree.value = data || []
    categoryOptions.value = flattenTree(data || [])
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

async function loadCourseList() {
  loading.value = true
  try {
    const data = await request.post('/adminCourse/loadCourseList', queryParams)
    courseList.value = data.list || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载课程列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  loadCourseList()
}

function handleReset() {
  queryParams.title = ''
  queryParams.categoryId = ''
  queryParams.status = ''
  queryParams.pageNum = 1
  loadCourseList()
}

function handlePageChange(page) {
  queryParams.pageNum = page
  loadCourseList()
}

function handleSizeChange(size) {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  loadCourseList()
}

function handleCreate() {
  router.push('/courses/create')
}

function handleEdit(row) {
  router.push(`/courses/${row.id}/edit`)
}

function handleChapterManage(row) {
  router.push(`/courses/${row.id}/chapters`)
}

async function handlePublish(row) {
  try {
    await request.put('/adminCourse/publishCourse', { courseId: row.id })
    ElMessage.success('发布成功')
    loadCourseList()
  } catch (e) {
    ElMessage.error('发布失败')
  }
}

async function handleClose(row) {
  try {
    await ElMessageBox.confirm('确定要关闭该课程吗？关闭后学生将无法学习。', '关闭确认', {
      type: 'warning',
      confirmButtonText: '确定关闭',
      cancelButtonText: '取消'
    })
    await request.put('/adminCourse/closeCourse', { courseId: row.id })
    ElMessage.success('关闭成功')
    loadCourseList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('关闭失败')
    }
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除课程「${row.title}」吗？删除后不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await request.delete('/adminCourse/deleteCourse', { params: { courseId: row.id } })
    ElMessage.success('删除成功')
    loadCourseList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadCategoryTree()
  loadCourseList()
})
</script>

<template>
  <div class="course-list">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="课程名称">
          <el-input v-model="queryParams.title" placeholder="请输入课程名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryParams.categoryId" placeholder="请选择分类" clearable>
            <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" @click="handleCreate">创建课程</el-button>
      </div>
      <el-table :data="courseList" v-loading="loading" stripe border style="width: 100%">
        <el-table-column label="封面" width="80" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.coverImage"
              :src="row.coverImage"
              style="width: 50px; height: 50px; border-radius: 4px"
              fit="cover"
              :preview-src-list="[row.coverImage]"
            />
            <span v-else class="no-cover">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="课程名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="teacherName" label="讲师" width="100" />
        <el-table-column label="价格" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.price === 0" style="color: #67C23A">免费</span>
            <span v-else>&yen;{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="studentCount" label="学生数" width="90" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="handleChapterManage(row)">章节</el-button>
            <el-button
              v-if="row.status === 'DRAFT'"
              type="success"
              link
              size="small"
              @click="handlePublish(row)"
            >
              发布
            </el-button>
            <el-button
              v-if="row.status === 'PUBLISHED'"
              type="warning"
              link
              size="small"
              @click="handleClose(row)"
            >
              关闭
            </el-button>
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
  </div>
</template>

<style scoped>
.course-list {
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
.toolbar {
  margin-bottom: 16px;
}
.no-cover {
  color: #c0c4cc;
  font-size: 12px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
