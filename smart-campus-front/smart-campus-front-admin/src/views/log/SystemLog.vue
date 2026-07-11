<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const logList = ref([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 15
})

function truncateText(text, maxLen = 50) {
  if (!text) return '-'
  const str = typeof text === 'string' ? text : JSON.stringify(text)
  return str.length > maxLen ? str.slice(0, maxLen) + '...' : str
}

async function loadLogList() {
  loading.value = true
  try {
    const data = await request.post('/adminLog/loadLogList', queryParams)
    logList.value = data.list || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载操作日志失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  queryParams.pageNum = page
  loadLogList()
}

function handleSizeChange(size) {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  loadLogList()
}

onMounted(() => {
  loadLogList()
})
</script>

<template>
  <div class="system-log">
    <el-card class="header-card">
      <span class="page-title">操作日志</span>
    </el-card>

    <el-card class="table-card">
      <el-table :data="logList" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column prop="module" label="模块" width="140" show-overflow-tooltip />
        <el-table-column label="操作类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag
              :type="
                row.operation === 'DELETE' ? 'danger' :
                row.operation === 'UPDATE' || row.operation === 'PUT' ? 'warning' :
                row.operation === 'CREATE' || row.operation === 'POST' ? 'success' :
                'info'
              "
              size="small"
            >
              {{ row.operation || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="请求参数" min-width="200">
          <template #default="{ row }">
            <span class="log-params">{{ truncateText(row.params, 80) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="150" />
        <el-table-column prop="createTime" label="操作时间" width="170" />
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 15, 20, 50]"
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
.system-log {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.header-card {
  border-radius: 8px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
}
.table-card {
  border-radius: 8px;
}
.log-params {
  color: #606266;
  font-size: 13px;
  font-family: 'Courier New', Courier, monospace;
  word-break: break-all;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
