<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const fileList = ref([])
const total = ref(0)
const uploading = ref(false)
const uploadRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  fileType: ''
})

const fileTypeOptions = [
  { label: '全部', value: '' },
  { label: '图片', value: 'IMAGE' },
  { label: '视频', value: 'VIDEO' },
  { label: '文档', value: 'DOCUMENT' },
  { label: '其他', value: 'OTHER' }
]

function formatFileSize(bytes) {
  if (!bytes) return '0 B'
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(2) + ' ' + sizes[i]
}

async function loadFileList() {
  loading.value = true
  try {
    const data = await request.post('/adminFile/loadFileList', queryParams)
    fileList.value = data.list || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载文件列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  loadFileList()
}

function handlePageChange(page) {
  queryParams.pageNum = page
  loadFileList()
}

function handleSizeChange(size) {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  loadFileList()
}

async function handleUploadFile(options) {
  const { file } = options
  const formData = new FormData()
  formData.append('file', file)
  uploading.value = true
  try {
    await request.post('/adminFile/uploadFile', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    ElMessage.success(`文件「${file.name}」上传成功`)
    loadFileList()
  } catch (e) {
    ElMessage.error('文件上传失败')
  } finally {
    uploading.value = false
  }
}

function handleBeforeUpload(file) {
  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isLt100M) {
    ElMessage.error('文件大小不能超过 100MB')
    return false
  }
  return true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除文件「${row.originalName}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await request.delete('/adminFile/deleteFile', { params: { fileId: row.id } })
    ElMessage.success('删除成功')
    loadFileList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadFileList()
})
</script>

<template>
  <div class="file-manager">
    <el-card class="upload-card">
      <el-row :gutter="20" align="middle">
        <el-col :span="12">
          <el-upload
            ref="uploadRef"
            :http-request="handleUploadFile"
            :before-upload="handleBeforeUpload"
            :show-file-list="false"
            multiple
          >
            <el-button type="primary" :loading="uploading">
              <el-icon><Upload /></el-icon>
              上传文件
            </el-button>
            <template #tip>
              <span class="upload-tip">支持任意类型文件，单文件不超过 100MB</span>
            </template>
          </el-upload>
        </el-col>
        <el-col :span="12" style="text-align: right;">
          <el-select v-model="queryParams.fileType" placeholder="文件类型筛选" style="width: 160px" @change="handleSearch">
            <el-option v-for="opt in fileTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="table-card">
      <el-table :data="fileList" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="originalName" label="文件名" min-width="220" show-overflow-tooltip />
        <el-table-column label="大小" width="110" align="center">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.fileType === 'IMAGE' ? 'success' : row.fileType === 'VIDEO' ? 'warning' : 'info'">
              {{ row.fileType || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="mimeType" label="MIME类型" width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="上传时间" width="170" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.url"
              type="primary"
              link
              size="small"
              @click="window.open(row.url)"
            >
              下载
            </el-button>
            <span v-else class="no-url">无链接</span>
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
.file-manager {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.upload-card {
  border-radius: 8px;
}
.upload-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 13px;
}
.table-card {
  border-radius: 8px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.no-url {
  color: #c0c4cc;
  font-size: 12px;
}
</style>
