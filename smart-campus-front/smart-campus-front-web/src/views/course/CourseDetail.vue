<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const courseId = computed(() => route.params.id)

const course = ref(null)
const loading = ref(false)
const activeTab = ref('chapters')

const isEnrolled = ref(false)
const isFavorite = ref(false)
const progress = ref(0)

async function loadCourse() {
  loading.value = true
  try {
    const data = await request.get('/courseInfo/getCourseInfo', { params: { courseId: courseId.value } })
    course.value = data
    isEnrolled.value = data.isEnrolled || false
    isFavorite.value = data.isFavorite || false
    progress.value = data.progress || 0
  } catch (e) {
    ElMessage.error('加载课程详情失败')
  } finally {
    loading.value = false
  }
}

async function handleEnroll() {
  if (!userStore.isLoggedIn) {
    router.push(`/login?redirect=/courses/${courseId.value}`)
    return
  }
  try {
    await request.post('/enrollment/enrollCourse', null, { params: { courseId: courseId.value } })
    ElMessage.success('成功加入课程')
    isEnrolled.value = true
  } catch (e) {
    ElMessage.error(e.message || '加入课程失败')
  }
}

async function handleDropCourse() {
  try {
    await ElMessageBox.confirm('确定要退出该课程吗？退出后学习记录将保留但无法继续学习。', '确认退出', {
      confirmButtonText: '确定退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.post('/enrollment/dropCourse', null, { params: { courseId: courseId.value } })
    ElMessage.success('已退出课程')
    isEnrolled.value = false
    progress.value = 0
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    router.push(`/login?redirect=/courses/${courseId.value}`)
    return
  }
  try {
    if (isFavorite.value) {
      await request.post('/enrollment/removeFavorite', null, { params: { courseId: courseId.value } })
      isFavorite.value = false
      ElMessage.success('已取消收藏')
    } else {
      await request.post('/enrollment/addFavorite', null, { params: { courseId: courseId.value } })
      isFavorite.value = true
      ElMessage.success('已添加收藏')
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

function goToLearn(sectionId) {
  if (!isEnrolled.value) return
  router.push(`/learn/${courseId.value}/${sectionId}`)
}

function continueLearning() {
  if (!course.value || !course.value.chapters) return
  for (const chapter of course.value.chapters) {
    if (chapter.sections) {
      for (const section of chapter.sections) {
        if (!section.isCompleted) {
          goToLearn(section.id)
          return
        }
      }
    }
  }
  const firstSection = course.value.chapters[0]?.sections?.[0]
  if (firstSection) {
    goToLearn(firstSection.id)
  }
}

function getContentTypeIcon(type) {
  const map = { VIDEO: 'VideoPlay', DOCUMENT: 'Document', RICH_TEXT: 'Reading' }
  return map[type] || 'Document'
}

function getContentTypeLabel(type) {
  const map = { VIDEO: '视频', DOCUMENT: '文档', RICH_TEXT: '图文' }
  return map[type] || type
}

function formatPrice(price) {
  if (!price || price === 0) return '免费'
  return `¥${price}`
}

function goToDiscussion() {
  router.push(`/discussion/${courseId.value}`)
}

onMounted(() => {
  loadCourse()
})
</script>

<template>
  <div v-loading="loading" class="course-detail">
    <template v-if="course">
      <!-- Hero Section -->
      <div class="hero-section" :style="course.coverImage ? { backgroundImage: `url(${course.coverImage})` } : {}">
        <div class="hero-overlay">
          <div class="hero-content">
            <el-tag size="small" type="info">{{ course.categoryName }}</el-tag>
            <h1 class="hero-title">{{ course.title }}</h1>
            <div class="hero-meta">
              <span><el-icon><User /></el-icon> {{ course.teacherName || '未知教师' }}</span>
              <span><el-icon><UserFilled /></el-icon> {{ course.studentCount || 0 }} 名学员</span>
              <span><el-icon><Collection /></el-icon> {{ course.chapterCount || 0 }} 章节</span>
            </div>
            <div class="hero-price">
              <span class="price-text">{{ formatPrice(course.price) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Body -->
      <div class="detail-body">
        <div class="detail-main">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="课程介绍" name="description">
              <div class="description-content" v-html="course.description || '暂无课程介绍'"></div>
            </el-tab-pane>

            <el-tab-pane label="课程目录" name="chapters">
              <div v-if="!course.chapters || course.chapters.length === 0" class="empty-hint">
                暂无章节内容
              </div>
              <div v-else class="chapter-tree">
                <div v-for="(chapter, ci) in course.chapters" :key="chapter.id" class="chapter-node">
                  <div class="chapter-header">
                    <el-icon><FolderOpened /></el-icon>
                    <span class="chapter-title">{{ ci + 1 }}. {{ chapter.title }}</span>
                  </div>
                  <div v-if="chapter.sections" class="section-list">
                    <div
                      v-for="(section, si) in chapter.sections"
                      :key="section.id"
                      class="section-item"
                      :class="{ clickable: isEnrolled, completed: section.isCompleted }"
                      @click="goToLearn(section.id)"
                    >
                      <div class="section-left">
                        <el-icon v-if="section.isCompleted" class="completed-icon"><CircleCheckFilled /></el-icon>
                        <el-icon v-else><component :is="getContentTypeIcon(section.contentType)" /></el-icon>
                        <span class="section-name">{{ ci + 1 }}.{{ si + 1 }} {{ section.title }}</span>
                      </div>
                      <div class="section-right">
                        <el-tag size="small" type="info">{{ getContentTypeLabel(section.contentType) }}</el-tag>
                        <span v-if="section.duration" class="duration">{{ section.duration }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- Sidebar -->
        <div class="detail-sidebar">
          <div class="sidebar-card">
            <div v-if="!isEnrolled" class="enroll-section">
              <el-button
                v-if="userStore.isLoggedIn"
                type="primary"
                size="large"
                style="width: 100%"
                @click="handleEnroll"
              >
                立即加入
              </el-button>
              <el-button
                v-else
                type="primary"
                size="large"
                style="width: 100%"
                @click="router.push(`/login?redirect=/courses/${courseId}`)"
              >
                登录后加入
              </el-button>
            </div>

            <div v-else class="enrolled-section">
              <div class="progress-bar">
                <span class="progress-label">学习进度</span>
                <el-progress :percentage="progress" :stroke-width="10" />
              </div>
              <el-button type="primary" style="width: 100%" @click="continueLearning">
                {{ progress > 0 ? '继续学习' : '开始学习' }}
              </el-button>
              <el-button style="width: 100%; margin-top: 8px" @click="handleDropCourse">
                退出课程
              </el-button>
            </div>

            <el-divider />

            <div class="action-buttons">
              <el-button
                :type="isFavorite ? 'warning' : 'default'"
                style="width: 100%"
                @click="toggleFavorite"
              >
                <el-icon><Star /></el-icon>
                {{ isFavorite ? '已收藏' : '收藏课程' }}
              </el-button>
              <el-button style="width: 100%; margin-top: 8px" @click="goToDiscussion">
                <el-icon><ChatDotRound /></el-icon>
                讨论区
              </el-button>
            </div>
          </div>

          <div class="sidebar-card course-info-card">
            <h4>课程信息</h4>
            <div class="info-item">
              <span class="info-label">授课教师</span>
              <span class="info-value">{{ course.teacherName || '未知' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">课程分类</span>
              <span class="info-value">{{ course.categoryName || '未分类' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">学员人数</span>
              <span class="info-value">{{ course.studentCount || 0 }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">章节数</span>
              <span class="info-value">{{ course.chapterCount || 0 }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ course.createTime || '-' }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>

    <el-empty v-else description="课程不存在" />
  </div>
</template>

<style scoped>
.course-detail {
  min-height: 400px;
}
.hero-section {
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: cover;
  background-position: center;
  margin-bottom: 24px;
}
.hero-overlay {
  background: rgba(0, 0, 0, 0.55);
  padding: 50px 40px;
}
.hero-content {
  color: #fff;
}
.hero-title {
  font-size: 28px;
  margin: 12px 0 16px;
  font-weight: 700;
}
.hero-meta {
  display: flex;
  gap: 24px;
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 12px;
}
.hero-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}
.hero-price .price-text {
  font-size: 24px;
  font-weight: 700;
  color: #ffd54f;
}
.detail-body {
  display: flex;
  gap: 24px;
}
.detail-main {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 10px;
  padding: 24px;
}
.description-content {
  line-height: 1.8;
  color: #606266;
  min-height: 200px;
}
.description-content :deep(img) {
  max-width: 100%;
}
.empty-hint {
  text-align: center;
  padding: 40px;
  color: #c0c4cc;
}
.chapter-node {
  margin-bottom: 20px;
}
.chapter-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
  color: #303133;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 8px;
}
.section-list {
  padding-left: 16px;
}
.section-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 6px;
  margin-bottom: 4px;
  transition: background 0.2s;
  color: #606266;
}
.section-item.clickable {
  cursor: pointer;
}
.section-item.clickable:hover {
  background: #ecf5ff;
}
.section-item.completed {
  color: #67c23a;
}
.section-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}
.section-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.completed-icon {
  color: #67c23a;
}
.section-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.duration {
  font-size: 12px;
  color: #c0c4cc;
}
.detail-sidebar {
  width: 300px;
  flex-shrink: 0;
}
.sidebar-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 16px;
}
.progress-bar {
  margin-bottom: 16px;
}
.progress-label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}
.action-buttons {
  margin-top: 4px;
}
.course-info-card h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #303133;
}
.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 13px;
  border-bottom: 1px dashed #ebeef5;
}
.info-item:last-child {
  border-bottom: none;
}
.info-label {
  color: #909399;
}
.info-value {
  color: #303133;
  font-weight: 500;
}

@media (max-width: 768px) {
  .detail-body {
    flex-direction: column;
  }
  .detail-sidebar {
    width: 100%;
  }
}
</style>
