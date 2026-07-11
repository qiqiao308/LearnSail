<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const courseId = computed(() => route.params.courseId)
const sectionId = computed(() => route.params.sectionId)

const course = ref(null)
const currentSection = ref(null)
const allSections = ref([])
const loading = ref(false)
const markLoading = ref(false)
const completedSections = ref(new Set())

function flattenSections(chapters) {
  const result = []
  if (!chapters) return result
  chapters.forEach((ch, ci) => {
    if (ch.sections) {
      ch.sections.forEach((sec, si) => {
        result.push({ ...sec, chapterIndex: ci, sectionIndex: si, chapterTitle: ch.title })
      })
    }
  })
  return result
}

function findSectionById(sectionId) {
  return allSections.value.find(s => String(s.id) === String(sectionId))
}

const prevSection = computed(() => {
  const idx = allSections.value.findIndex(s => String(s.id) === String(sectionId.value))
  return idx > 0 ? allSections.value[idx - 1] : null
})

const nextSection = computed(() => {
  const idx = allSections.value.findIndex(s => String(s.id) === String(sectionId.value))
  return idx < allSections.value.length - 1 ? allSections.value[idx + 1] : null
})

const isCurrentCompleted = computed(() => {
  return currentSection.value ? completedSections.value.has(String(currentSection.value.id)) : false
})

async function loadCourseData() {
  loading.value = true
  try {
    const data = await request.get('/courseInfo/getCourseInfo', { params: { courseId: courseId.value } })
    course.value = data
    allSections.value = flattenSections(data.chapters || [])
    completedSections.value = new Set()
    allSections.value.forEach(s => {
      if (s.isCompleted) completedSections.value.add(String(s.id))
    })
    const sec = findSectionById(sectionId.value)
    if (sec) {
      currentSection.value = sec
    } else if (allSections.value.length > 0) {
      currentSection.value = allSections.value[0]
      router.replace(`/learn/${courseId.value}/${allSections.value[0].id}`)
    }
  } catch (e) {
    ElMessage.error('加载课程内容失败')
  } finally {
    loading.value = false
  }
}

function switchSection(section) {
  if (String(section.id) === String(sectionId.value)) return
  currentSection.value = section
  router.push(`/learn/${courseId.value}/${section.id}`)
}

function goToPrev() {
  if (prevSection.value) switchSection(prevSection.value)
}

function goToNext() {
  if (nextSection.value) switchSection(nextSection.value)
}

async function markComplete() {
  if (isCurrentCompleted.value) return
  markLoading.value = true
  try {
    await request.post('/progress/markSectionComplete', null, {
      params: { sectionId: currentSection.value.id, courseId: courseId.value }
    })
    completedSections.value.add(String(currentSection.value.id))
    ElMessage.success('已标记为完成')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    markLoading.value = false
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

onMounted(() => {
  loadCourseData()
})

watch(sectionId, () => {
  if (course.value) {
    const sec = findSectionById(sectionId.value)
    if (sec) currentSection.value = sec
  }
})
</script>

<template>
  <div class="learn-section" v-loading="loading">
    <el-empty v-if="!loading && !currentSection" description="内容不存在" />

    <template v-if="currentSection">
      <div class="learn-layout">
        <!-- Sidebar -->
        <div class="learn-sidebar">
          <div class="sidebar-header">
            <h3>{{ course?.title || '课程内容' }}</h3>
            <el-button text size="small" @click="router.push(`/courses/${courseId}`)">
              返回课程
            </el-button>
          </div>
          <div class="chapter-tree">
            <div v-for="chapter in (course?.chapters || [])" :key="chapter.id" class="ch-sidebar-node">
              <div class="ch-sidebar-header">
                <el-icon><FolderOpened /></el-icon>
                <span>{{ chapter.title }}</span>
              </div>
              <div
                v-for="section in (chapter.sections || [])"
                :key="section.id"
                class="sec-sidebar-item"
                :class="{
                  active: String(section.id) === String(sectionId),
                  completed: completedSections.has(String(section.id))
                }"
                @click="switchSection(section)"
              >
                <el-icon v-if="completedSections.has(String(section.id))" class="done-icon"><CircleCheckFilled /></el-icon>
                <el-icon v-else><component :is="getContentTypeIcon(section.contentType)" /></el-icon>
                <span class="sec-title">{{ section.title }}</span>
                <span v-if="section.duration" class="sec-duration">{{ section.duration }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Main Content -->
        <div class="learn-main">
          <div class="content-header">
            <h2>{{ currentSection.title }}</h2>
            <el-tag>{{ getContentTypeLabel(currentSection.contentType) }}</el-tag>
          </div>

          <div class="content-body">
            <!-- Video -->
            <div v-if="currentSection.contentType === 'VIDEO'" class="video-wrapper">
              <video
                v-if="currentSection.contentUrl"
                :src="currentSection.contentUrl"
                controls
                class="video-player"
                preload="metadata"
              >
                您的浏览器不支持视频播放。
              </video>
              <el-empty v-else description="暂无视频资源" />
            </div>

            <!-- Document -->
            <div v-else-if="currentSection.contentType === 'DOCUMENT'" class="document-wrapper">
              <iframe
                v-if="currentSection.contentUrl"
                :src="currentSection.contentUrl"
                class="document-frame"
                frameborder="0"
              />
              <el-empty v-else description="暂无文档资源" />
            </div>

            <!-- Rich Text -->
            <div v-else-if="currentSection.contentType === 'RICH_TEXT'" class="richtext-wrapper">
              <div v-if="currentSection.contentText" v-html="currentSection.contentText"></div>
              <el-empty v-else description="暂无内容" />
            </div>

            <div v-else class="unsupported-wrapper">
              <el-empty description="暂不支持此内容类型" />
            </div>
          </div>

          <!-- Bottom Navigation -->
          <div class="content-footer">
            <div class="footer-left">
              <el-button :disabled="!prevSection" @click="goToPrev">
                <el-icon><ArrowLeft /></el-icon>
                上一节
              </el-button>
              <el-button :disabled="!nextSection" @click="goToNext">
                下一节
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
            <div class="footer-right">
              <el-button
                v-if="!isCurrentCompleted"
                type="success"
                :loading="markLoading"
                @click="markComplete"
              >
                <el-icon><Check /></el-icon>
                标记为已完成
              </el-button>
              <el-tag v-else type="success" size="large">
                <el-icon><CircleCheckFilled /></el-icon>
                已完成
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.learn-section {
  min-height: 600px;
}
.learn-layout {
  display: flex;
  gap: 20px;
  min-height: calc(100vh - 150px);
}
.learn-sidebar {
  width: 280px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 150px);
  position: sticky;
  top: 84px;
}
.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.sidebar-header h3 {
  margin: 0;
  font-size: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}
.chapter-tree {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.ch-sidebar-node {
  margin-bottom: 8px;
}
.ch-sidebar-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 13px;
  color: #303133;
  padding: 6px 8px;
}
.sec-sidebar-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 8px 8px 22px;
  font-size: 13px;
  color: #606266;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}
.sec-sidebar-item:hover {
  background: #ecf5ff;
}
.sec-sidebar-item.active {
  background: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}
.sec-sidebar-item.completed {
  color: #67c23a;
}
.sec-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sec-duration {
  font-size: 11px;
  color: #c0c4cc;
}
.done-icon {
  color: #67c23a;
}
.learn-main {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
}
.content-header {
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  gap: 12px;
}
.content-header h2 {
  margin: 0;
  font-size: 20px;
  flex: 1;
}
.content-body {
  flex: 1;
  padding: 24px;
  min-height: 400px;
}
.video-wrapper {
  display: flex;
  justify-content: center;
}
.video-player {
  width: 100%;
  max-width: 960px;
  border-radius: 8px;
  background: #000;
}
.document-wrapper {
  height: 600px;
}
.document-frame {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}
.richtext-wrapper {
  line-height: 1.8;
  color: #606266;
}
.richtext-wrapper :deep(img) {
  max-width: 100%;
}
.content-footer {
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.footer-left {
  display: flex;
  gap: 12px;
}
.footer-right {
  display: flex;
  align-items: center;
}

@media (max-width: 768px) {
  .learn-layout {
    flex-direction: column;
  }
  .learn-sidebar {
    width: 100%;
    max-height: 200px;
    position: static;
  }
}
</style>
