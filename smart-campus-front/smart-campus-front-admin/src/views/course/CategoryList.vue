<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const categoryTree = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('添加分类')
const submitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const categoryForm = reactive({
  id: null,
  name: '',
  parentId: null,
  sortOrder: 1
})

function flattenForSelect(tree, prefix = '') {
  const result = []
  tree.forEach(item => {
    result.push({ label: prefix + item.name, value: item.id })
    if (item.children && item.children.length > 0) {
      result.push(...flattenForSelect(item.children, prefix + '-- '))
    }
  })
  return result
}

const parentOptions = ref([])

async function loadCategoryTree() {
  loading.value = true
  try {
    const data = await request.get('/adminCategory/loadCategoryTree')
    categoryTree.value = data || []
    parentOptions.value = [{ label: '顶级分类', value: null }]
    parentOptions.value.push(...flattenForSelect(data || []))
  } catch (e) {
    ElMessage.error('加载分类列表失败')
  } finally {
    loading.value = false
  }
}

function handleAdd(parentId = null) {
  isEdit.value = false
  categoryForm.id = null
  categoryForm.name = ''
  categoryForm.parentId = parentId
  categoryForm.sortOrder = 1
  dialogTitle.value = '添加分类'
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  categoryForm.id = row.id
  categoryForm.name = row.name
  categoryForm.parentId = row.parentId || null
  categoryForm.sortOrder = row.sortOrder || 1
  dialogTitle.value = '编辑分类'
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除分类「${row.name}」吗？如有子分类将一并删除。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await request.delete('/adminCategory/deleteCategory', { params: { categoryId: row.id } })
    ElMessage.success('删除成功')
    loadCategoryTree()
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
      if (isEdit.value) {
        await request.put('/adminCategory/updateCategory', { ...categoryForm })
        ElMessage.success('更新成功')
      } else {
        await request.post('/adminCategory/saveCategory', {
          name: categoryForm.name,
          parentId: categoryForm.parentId,
          sortOrder: categoryForm.sortOrder
        })
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadCategoryTree()
    } catch (e) {
      ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadCategoryTree()
})
</script>

<template>
  <div class="category-list">
    <el-card class="toolbar-card">
      <div class="toolbar">
        <span class="page-title">分类管理</span>
        <el-button type="primary" @click="handleAdd(null)">
          <el-icon><Plus /></el-icon>
          添加顶级分类
        </el-button>
      </div>
    </el-card>

    <el-card class="content-card" v-loading="loading">
      <div v-if="categoryTree.length === 0" class="empty-state">
        <el-empty description="暂无分类数据" />
      </div>
      <div v-else class="tree-list">
        <div v-for="item in categoryTree" :key="item.id">
          <div class="tree-node" :style="{ paddingLeft: (0 * 24 + 16) + 'px' }">
            <div class="tree-node-info">
              <span class="tree-node-name">{{ item.name }}</span>
              <span class="tree-node-sort">排序: {{ item.sortOrder }}</span>
            </div>
            <div class="tree-node-actions">
              <el-button type="primary" link size="small" @click="handleAdd(item.id)">添加子分类</el-button>
              <el-button type="warning" link size="small" @click="handleEdit(item)">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(item)">删除</el-button>
            </div>
          </div>
          <div v-if="item.children && item.children.length > 0" class="children-block">
            <div v-for="child in item.children" :key="child.id">
              <div class="tree-node" :style="{ paddingLeft: (1 * 24 + 16) + 'px' }">
                <div class="tree-node-info">
                  <span class="tree-node-name">{{ child.name }}</span>
                  <span class="tree-node-sort">排序: {{ child.sortOrder }}</span>
                </div>
                <div class="tree-node-actions">
                  <el-button type="primary" link size="small" @click="handleAdd(child.id)">添加子分类</el-button>
                  <el-button type="warning" link size="small" @click="handleEdit(child)">编辑</el-button>
                  <el-button type="danger" link size="small" @click="handleDelete(child)">删除</el-button>
                </div>
              </div>
              <div v-if="child.children && child.children.length > 0" class="children-block">
                <div v-for="grand in child.children" :key="grand.id">
                  <div class="tree-node" :style="{ paddingLeft: (2 * 24 + 16) + 'px' }">
                    <div class="tree-node-info">
                      <span class="tree-node-name">{{ grand.name }}</span>
                      <span class="tree-node-sort">排序: {{ grand.sortOrder }}</span>
                    </div>
                    <div class="tree-node-actions">
                      <el-button type="primary" link size="small" @click="handleAdd(grand.id)">添加子分类</el-button>
                      <el-button type="warning" link size="small" @click="handleEdit(grand)">编辑</el-button>
                      <el-button type="danger" link size="small" @click="handleDelete(grand)">删除</el-button>
                    </div>
                  </div>
                  <div v-if="grand.children && grand.children.length > 0" class="children-block">
                    <div v-for="deep in grand.children" :key="deep.id">
                      <div class="tree-node" :style="{ paddingLeft: (3 * 24 + 16) + 'px' }">
                        <div class="tree-node-info">
                          <span class="tree-node-name">{{ deep.name }}</span>
                          <span class="tree-node-sort">排序: {{ deep.sortOrder }}</span>
                        </div>
                        <div class="tree-node-actions">
                          <el-button type="warning" link size="small" @click="handleEdit(deep)">编辑</el-button>
                          <el-button type="danger" link size="small" @click="handleDelete(deep)">删除</el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="categoryForm" label-width="80px">
        <el-form-item label="分类名称" prop="name" :rules="[{ required: true, message: '请输入分类名称', trigger: 'blur' }]">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父级分类">
          <el-select v-model="categoryForm.parentId" placeholder="请选择父级分类" style="width: 100%">
            <el-option
              v-for="opt in parentOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
              :disabled="isEdit && opt.value === categoryForm.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sortOrder" :min="1" :step="1" />
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
.category-list {
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
.content-card {
  border-radius: 8px;
  min-height: 200px;
}
.empty-state {
  padding: 40px 0;
}
.tree-list {
  padding: 8px 0;
}
.tree-node-root {
  border-bottom: 1px solid #f0f0f0;
  padding: 4px 0;
}
.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-radius: 6px;
  transition: background 0.2s;
}
.tree-node:hover {
  background: #f5f7fa;
}
.tree-node-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.depth-indent {
  width: 24px;
  height: 1px;
  flex-shrink: 0;
}
.tree-node-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}
.tree-node-sort {
  color: #909399;
  font-size: 13px;
}
.tree-node-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}
.children-block {
  border-left: 1px dashed #dcdfe6;
  margin-left: 16px;
}
</style>
