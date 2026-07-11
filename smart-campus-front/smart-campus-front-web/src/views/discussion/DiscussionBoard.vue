<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const courseId = computed(() => route.params.courseId)

const posts = ref([])
const loading = ref(false)
const createDialogVisible = ref(false)
const creating = ref(false)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const newPost = reactive({
  title: '',
  content: ''
})

// Expanded post with replies
const expandedPostId = ref(null)
const replies = ref([])
const repliesLoading = ref(false)

// Reply state
const replyContent = ref('')
const replyTargetId = ref(null) // parentReplyId
const replySubmitting = ref(false)

async function loadPosts() {
  loading.value = true
  try {
    const data = await request.post('/discussion/loadPostList', {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }, {
      params: { courseId: courseId.value }
    })
    posts.value = data.list || []
    pagination.total = data.total || 0
  } catch (e) {
    ElMessage.error('加载讨论列表失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  pagination.pageNum = page
  loadPosts()
}

async function handleCreatePost() {
  if (!newPost.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!newPost.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  creating.value = true
  try {
    await request.post('/discussion/createPost', {
      courseId: Number(courseId.value),
      title: newPost.title,
      content: newPost.content
    })
    ElMessage.success('发布成功')
    createDialogVisible.value = false
    newPost.title = ''
    newPost.content = ''
    pagination.pageNum = 1
    loadPosts()
  } catch (e) {
    ElMessage.error(e.message || '发布失败')
  } finally {
    creating.value = false
  }
}

async function toggleReplies(post) {
  if (expandedPostId.value === post.id) {
    expandedPostId.value = null
    replies.value = []
    return
  }

  expandedPostId.value = post.id
  repliesLoading.value = true
  try {
    const data = await request.get('/discussion/loadReplyList', {
      params: { postId: post.id }
    })
    replies.value = data || []
  } catch (e) {
    ElMessage.error('加载回复失败')
  } finally {
    repliesLoading.value = false
  }
}

function handleReplyTo(reply) {
  replyTargetId.value = reply ? reply.id : null
}

async function handleSubmitReply() {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  replySubmitting.value = true
  try {
    await request.post('/discussion/createReply', {
      postId: expandedPostId.value,
      parentReplyId: replyTargetId.value,
      content: replyContent.value
    })
    ElMessage.success('回复成功')
    replyContent.value = ''
    replyTargetId.value = null

    // Reload replies
    const data = await request.get('/discussion/loadReplyList', {
      params: { postId: expandedPostId.value }
    })
    replies.value = data || []
  } catch (e) {
    ElMessage.error(e.message || '回复失败')
  } finally {
    replySubmitting.value = false
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<template>
  <div class="discussion-board">
    <div class="board-header">
      <div class="header-left">
        <h2>课程讨论区</h2>
        <el-button text @click="router.push(`/courses/${courseId}`)">
          <el-icon><ArrowLeft /></el-icon>
          返回课程
        </el-button>
      </div>
      <el-button type="primary" @click="createDialogVisible = true">
        <el-icon><Edit /></el-icon>
        发帖
      </el-button>
    </div>

    <div v-loading="loading" class="post-list">
      <el-empty v-if="!loading && posts.length === 0" description="暂无讨论">
        <el-button type="primary" @click="createDialogVisible = true">发布第一个帖子</el-button>
      </el-empty>

      <div v-for="post in posts" :key="post.id" class="post-card">
        <div class="post-main" @click="toggleReplies(post)">
          <div class="post-avatar">
            <el-avatar :size="40" :src="post.avatar">
              {{ (post.username || '?')[0]?.toUpperCase() }}
            </el-avatar>
          </div>
          <div class="post-content">
            <div class="post-top">
              <span class="post-author">{{ post.username || '匿名用户' }}</span>
              <span class="post-time">{{ post.createTime || '' }}</span>
            </div>
            <h4 class="post-title">{{ post.title }}</h4>
            <p class="post-preview">{{ post.content }}</p>
            <div class="post-footer">
              <span class="reply-count">
                <el-icon><ChatDotRound /></el-icon>
                {{ post.replyCount || 0 }} 回复
              </span>
            </div>
          </div>
        </div>

        <!-- Replies -->
        <div v-if="expandedPostId === post.id" v-loading="repliesLoading" class="replies-section">
          <div v-if="replies.length === 0" class="no-replies">暂无回复</div>
          <div v-for="reply in replies" :key="reply.id" class="reply-item">
            <div class="reply-avatar">
              <el-avatar :size="32" :src="reply.avatar">
                {{ (reply.username || '?')[0]?.toUpperCase() }}
              </el-avatar>
            </div>
            <div class="reply-body">
              <div class="reply-top">
                <span class="reply-author">{{ reply.username || '匿名用户' }}</span>
                <span class="reply-time">{{ reply.createTime || '' }}</span>
              </div>
              <div class="reply-text">{{ reply.content }}</div>
              <div class="reply-actions">
                <el-button text size="small" @click="handleReplyTo(reply)">
                  回复
                </el-button>
              </div>
              <div v-if="replyTargetId === reply.id" class="reply-editor">
                <el-input
                  v-model="replyContent"
                  type="textarea"
                  :rows="3"
                  placeholder="回复..."
                />
                <div class="reply-editor-actions">
                  <el-button size="small" @click="replyTargetId = null">取消</el-button>
                  <el-button size="small" type="primary" :loading="replySubmitting" @click="handleSubmitReply">
                    回复
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- New reply to post -->
          <div v-if="replyTargetId !== null && !replies.find(r => r.id === replyTargetId)" class="reply-editor" style="margin-top: 12px">
            <el-input
              v-model="replyContent"
              type="textarea"
              :rows="3"
              placeholder="写下你的回复..."
            />
            <div class="reply-editor-actions">
              <el-button size="small" @click="replyTargetId = null">取消</el-button>
              <el-button size="small" type="primary" :loading="replySubmitting" @click="handleSubmitReply">
                回复帖子
              </el-button>
            </div>
          </div>
          <div v-else class="reply-editor" style="margin-top: 12px">
            <el-input
              v-model="replyContent"
              type="textarea"
              :rows="3"
              placeholder="写下你的回复..."
            />
            <div class="reply-editor-actions">
              <el-button size="small" type="primary" :loading="replySubmitting" @click="handleSubmitReply">
                回复帖子
              </el-button>
            </div>
          </div>
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

    <!-- Create post dialog -->
    <el-dialog v-model="createDialogVisible" title="发布新帖" width="560px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="标题" required>
          <el-input v-model="newPost.title" placeholder="请输入标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input
            v-model="newPost.content"
            type="textarea"
            :rows="6"
            placeholder="请输入内容..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreatePost">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.discussion-board {
  padding: 10px 0;
}
.board-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-left h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}
.post-list {
  min-height: 200px;
}
.post-card {
  background: #fff;
  border-radius: 10px;
  margin-bottom: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.post-main {
  display: flex;
  padding: 20px;
  cursor: pointer;
  transition: background 0.2s;
}
.post-main:hover {
  background: #fafafa;
}
.post-avatar {
  flex-shrink: 0;
  margin-right: 14px;
}
.post-content {
  flex: 1;
  min-width: 0;
}
.post-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.post-author {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}
.post-time {
  font-size: 12px;
  color: #c0c4cc;
}
.post-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}
.post-preview {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.post-footer {
  display: flex;
  align-items: center;
  gap: 16px;
}
.reply-count {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}
.replies-section {
  border-top: 1px solid #ebeef5;
  padding: 16px 20px 20px;
  background: #fafbfc;
}
.no-replies {
  text-align: center;
  color: #c0c4cc;
  padding: 20px;
  font-size: 13px;
}
.reply-item {
  display: flex;
  margin-bottom: 16px;
}
.reply-avatar {
  flex-shrink: 0;
  margin-right: 10px;
}
.reply-body {
  flex: 1;
  min-width: 0;
}
.reply-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.reply-author {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}
.reply-time {
  font-size: 11px;
  color: #c0c4cc;
}
.reply-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 6px;
}
.reply-actions {
  margin-bottom: 8px;
}
.reply-editor {
  margin-top: 8px;
}
.reply-editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
