<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const announcements = ref([])
const loading = ref(false)
const expandedId = ref(null)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

async function loadAnnouncements() {
  loading.value = true
  try {
    const data = await request.post('/announcement/loadAnnouncementList', {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    announcements.value = data.list || []
    pagination.total = data.total || 0
  } catch (e) {
    ElMessage.error('加载公告列表失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  pagination.pageNum = page
  loadAnnouncements()
}

function toggleExpand(announcement) {
  if (expandedId.value === announcement.id) {
    expandedId.value = null
  } else {
    expandedId.value = announcement.id
  }
}

async function markAsRead(announcement) {
  try {
    await request.post('/announcement/markRead', null, {
      params: { announcementId: announcement.id }
    })
  } catch (e) {
    // Silently fail - marking as read is not critical
  }
  toggleExpand(announcement)
}

function getTypeTag(type) {
  const map = { SYSTEM: '系统通知', COURSE: '课程通知' }
  return map[type] || type
}

function getTypeTagStyle(type) {
  const map = { SYSTEM: 'danger', COURSE: 'primary' }
  return map[type] || 'info'
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<template>
  <div class="announcement-list">
    <div class="page-header">
      <h2>通知公告</h2>
      <p>查看最新的系统通知和课程公告</p>
    </div>

    <div v-loading="loading" class="announcement-content">
      <el-empty v-if="!loading && announcements.length === 0" description="暂无公告" />

      <div v-for="item in announcements" :key="item.id" class="announcement-card">
        <div class="card-header" @click="markAsRead(item)">
          <div class="header-left">
            <el-tag :type="getTypeTagStyle(item.type)" size="small">
              {{ getTypeTag(item.type) }}
            </el-tag>
            <h3 class="card-title">{{ item.title }}</h3>
          </div>
          <div class="header-right">
            <span class="publisher">{{ item.publisherName || '管理员' }}</span>
            <span class="time">{{ item.createTime || '' }}</span>
            <el-icon class="expand-icon" :class="{ rotated: expandedId === item.id }">
              <ArrowDown />
            </el-icon>
          </div>
        </div>

        <div v-if="expandedId === item.id" class="card-body">
          <div class="body-content" v-html="item.content"></div>
        </div>
      </div>
    </div>

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
</template>

<style scoped>
.announcement-list {
  padding: 10px 0;
}
.page-header {
  margin-bottom: 24px;
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
.announcement-content {
  min-height: 200px;
}
.announcement-card {
  background: #fff;
  border-radius: 10px;
  margin-bottom: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  cursor: pointer;
  transition: background 0.2s;
  flex-wrap: wrap;
  gap: 10px;
}
.card-header:hover {
  background: #fafafa;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}
.card-title {
  margin: 0;
  font-size: 15px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}
.publisher {
  font-size: 13px;
  color: #606266;
}
.time {
  font-size: 12px;
  color: #c0c4cc;
}
.expand-icon {
  transition: transform 0.3s;
}
.expand-icon.rotated {
  transform: rotate(180deg);
}
.card-body {
  padding: 0 20px 20px;
  border-top: 1px solid #ebeef5;
}
.body-content {
  padding-top: 16px;
  line-height: 1.8;
  color: #606266;
}
.body-content :deep(img) {
  max-width: 100%;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
