<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const isEdit = ref(false)
const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const uploadRef = ref(null)

const courseForm = reactive({
  id: null,
  title: '',
  description: '',
  coverImage: '',
  categoryId: null,
  price: 0,
  status: 'DRAFT'
})

const categoryTree = ref([])

const rules = {
  title: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入课程描述', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

async function loadCategoryTree() {
  try {
    const data = await request.get('/adminCategory/loadCategoryTree')
    categoryTree.value = data || []
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

async function loadCourseDetail() {
  const id = route.params.id
  if (!id) return
  isEdit.value = true
  loading.value = true
  try {
    const listData = await request.post('/adminCourse/loadCourseList', {
      pageNum: 1,
      pageSize: 1000,
      title: '',
      categoryId: '',
      status: ''
    })
    const course = (listData.list || []).find(item => String(item.id) === String(id))
    if (course) {
      Object.assign(courseForm, {
        id: course.id,
        title: course.title || '',
        description: course.description || '',
        coverImage: course.coverImage || '',
        categoryId: course.categoryId || null,
        price: course.price || 0,
        status: course.status || 'DRAFT'
      })
    }
  } catch (e) {
    ElMessage.error('加载课程详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}

async function handleUploadFile(options) {
  const { file } = options
  const formData = new FormData()
  formData.append('file', file)
  try {
    const data = await request.post('/adminFile/uploadFile', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    courseForm.coverImage = data.url || data.fileUrl || data
    ElMessage.success('封面上传成功')
  } catch (e) {
    ElMessage.error('封面上传失败')
  }
}

function handleBeforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        await request.put('/adminCourse/updateCourse', { ...courseForm })
        ElMessage.success('课程更新成功')
      } else {
        await request.post('/adminCourse/saveCourse', { ...courseForm })
        ElMessage.success('课程创建成功')
      }
      router.push('/courses')
    } catch (e) {
      ElMessage.error(isEdit.value ? '课程更新失败' : '课程创建失败')
    } finally {
      submitting.value = false
    }
  })
}

function handleCancel() {
  router.push('/courses')
}

onMounted(() => {
  loadCategoryTree()
  if (route.params.id) {
    loadCourseDetail()
  }
})
</script>

<template>
  <div class="course-form" v-loading="loading">
    <el-card class="form-card">
      <template #header>
        <span class="form-title">{{ isEdit ? '编辑课程' : '创建课程' }}</span>
      </template>
      <el-form ref="formRef" :model="courseForm" :rules="rules" label-width="100px" style="max-width: 720px">
        <el-form-item label="课程名称" prop="title">
          <el-input v-model="courseForm.title" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input v-model="courseForm.description" type="textarea" :rows="4" placeholder="请输入课程描述" />
        </el-form-item>
        <el-form-item label="封面图片">
          <div class="cover-upload">
            <el-input v-model="courseForm.coverImage" placeholder="图片URL地址" style="margin-bottom: 8px;" />
            <el-upload
              ref="uploadRef"
              :http-request="handleUploadFile"
              :before-upload="handleBeforeUpload"
              :show-file-list="false"
              accept="image/*"
            >
              <div v-if="courseForm.coverImage" class="cover-preview">
                <el-image :src="courseForm.coverImage" style="width: 120px; height: 90px; border-radius: 4px;" fit="cover" />
                <span class="change-tip">点击更换图片</span>
              </div>
              <el-button v-else type="primary" plain>
                <el-icon><Upload /></el-icon>
                上传封面
              </el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="所属分类" prop="categoryId">
          <el-cascader
            v-model="courseForm.categoryId"
            :options="categoryTree"
            :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: false, emitPath: false }"
            placeholder="请选择分类"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="课程价格">
          <el-input-number v-model="courseForm.price" :min="0" :precision="2" :step="1" />
          <span class="unit-hint">元（0 表示免费）</span>
        </el-form-item>
        <el-form-item label="课程状态">
          <el-radio-group v-model="courseForm.status">
            <el-radio value="DRAFT">草稿</el-radio>
            <el-radio value="PUBLISHED">发布</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '创建课程' }}
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.course-form {
  display: flex;
  flex-direction: column;
}
.form-card {
  border-radius: 8px;
}
.form-title {
  font-size: 18px;
  font-weight: 600;
}
.cover-upload {
  width: 100%;
}
.cover-preview {
  position: relative;
  cursor: pointer;
  display: inline-block;
}
.change-tip {
  display: block;
  text-align: center;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.unit-hint {
  margin-left: 8px;
  font-size: 13px;
  color: #909399;
}
</style>
