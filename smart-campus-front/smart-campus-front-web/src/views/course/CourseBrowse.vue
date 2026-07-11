<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const courses = ref([])
const loading = ref(false)
const searchTitle = ref('')

const pagination = reactive({
  pageNum: 1,
  pageSize: 12,
  total: 0
})

const categories = ref([
  { id: null, name: '全部课程' },
  { id: 1, name: '计算机科学' },
  { id: 2, name: '数学' },
  { id: 3, name: '外语' },
  { id: 4, name: '文学' },
  { id: 5, name: '经济管理' },
  { id: 6, name: '艺术设计' },
  { id: 7, name: '工程技术' },
  { id: 8, name: '自然科学' }
])
const activeCategory = ref(null)

async function loadCourses() {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      title: searchTitle.value || undefined,
      categoryId: activeCategory.value || undefined
    }
    const data = await request.post('/courseInfo/loadCourseInfoList', params)
    courses.value = data.list || []
    pagination.total = data.total || 0
  } catch (e) {
    ElMessage.error('加载课程列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadCourses()
}

function handleCategoryChange(categoryId) {
  activeCategory.value = categoryId
  pagination.pageNum = 1
  loadCourses()
}

function handlePageChange(page) {
  pagination.pageNum = page
  loadCourses()
}

function goToDetail(courseId) {
  router.push(`/courses/${courseId}`)
}

function formatPrice(price) {
  if (!price || price === 0) return '免费'
  return `¥${price}`
}

onMounted(() => {
  if (route.query.keyword) {
    searchTitle.value = route.query.keyword
  }
  loadCourses()
})

watch(() => route.query.keyword, (val) => {
  if (val !== undefined) {
    searchTitle.value = val
    pagination.pageNum = 1
    loadCourses()
  }
})
</script>

<template>
  <div class="course-browse">
    <div class="browse-header">
      <h2>课程广场</h2>
      <p>发现精彩课程，开启学习之旅</p>
    </div>

    <div class="category-bar">
      <el-radio-group v-model="activeCategory" @change="handleCategoryChange">
        <el-radio-button
          v-for="cat in categories"
          :key="cat.id"
          :value="cat.id"
        >
          {{ cat.name }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchTitle"
        placeholder="搜索课程名称..."
        clearable
        size="large"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>

    <el-empty v-if="!loading && courses.length === 0" description="暂无课程" />

    <div v-loading="loading" class="course-grid">
      <el-row :gutter="20">
        <el-col
          v-for="course in courses"
          :key="course.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          style="margin-bottom: 20px"
        >
          <el-card
            :body-style="{ padding: 0 }"
            shadow="hover"
            class="course-card"
            @click="goToDetail(course.id)"
          >
            <div class="card-cover">
              <img v-if="course.coverImage" :src="course.coverImage" alt="封面" />
              <div v-else class="cover-placeholder">
                <el-icon :size="48"><Reading /></el-icon>
              </div>
              <el-tag
                v-if="course.price && course.price > 0"
                class="price-tag"
                type="warning"
              >
                {{ formatPrice(course.price) }}
              </el-tag>
              <el-tag v-else class="price-tag" type="success">免费</el-tag>
            </div>
            <div class="card-body">
              <h3 class="card-title">{{ course.title }}</h3>
              <div class="card-meta">
                <span class="teacher-name">
                  <el-icon><User /></el-icon>
                  {{ course.teacherName || '未知教师' }}
                </span>
                <span class="student-count">
                  <el-icon><UserFilled /></el-icon>
                  {{ course.studentCount || 0 }} 学员
                </span>
              </div>
              <div class="card-footer">
                <el-tag size="small" type="info">{{ course.categoryName || '未分类' }}</el-tag>
                <span class="chapter-count">{{ course.chapterCount || 0 }} 章节</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
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
.course-browse {
  padding: 10px 0;
}
.browse-header {
  margin-bottom: 20px;
}
.browse-header h2 {
  font-size: 28px;
  color: #303133;
  margin: 0 0 6px 0;
}
.browse-header p {
  color: #909399;
  margin: 0;
  font-size: 14px;
}
.category-bar {
  margin-bottom: 20px;
  overflow-x: auto;
  white-space: nowrap;
  padding-bottom: 4px;
}
.search-bar {
  margin-bottom: 24px;
  max-width: 500px;
}
.course-card {
  cursor: pointer;
  border-radius: 10px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}
.card-cover {
  position: relative;
  height: 160px;
  overflow: hidden;
  background: linear-gradient(135deg, #e0f7fa 0%, #f3e5f5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-placeholder {
  color: #b0bec5;
}
.price-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}
.card-body {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #909399;
  font-size: 13px;
  margin-bottom: 12px;
}
.card-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}
.chapter-count {
  font-size: 12px;
  color: #c0c4cc;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
