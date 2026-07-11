<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const stats = ref({
  totalUsers: 0,
  totalCourses: 0,
  totalEnrollments: 0,
  todayActiveUsers: 0,
  userGrowth: [],
  coursePopularity: []
})

const statCards = [
  { key: 'totalUsers', label: '总用户数', icon: 'User', color: '#409EFF', bgColor: '#ecf5ff' },
  { key: 'totalCourses', label: '总课程数', icon: 'Reading', color: '#67C23A', bgColor: '#f0f9eb' },
  { key: 'totalEnrollments', label: '总选课人次', icon: 'CollectionTag', color: '#E6A23C', bgColor: '#fdf6ec' },
  { key: 'todayActiveUsers', label: '今日活跃用户', icon: 'TrendCharts', color: '#F56C6C', bgColor: '#fef0f0' }
]

async function loadStats() {
  loading.value = true
  try {
    const data = await request.get('/adminStat/getDashboardStats')
    stats.value = data
  } catch (e) {
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

function getMaxValue(list) {
  if (!list || list.length === 0) return 1
  return Math.max(...list.map(item => item.value || 0), 1)
}

onMounted(() => {
  loadStats()
})
</script>

<template>
  <div class="dashboard" v-loading="loading">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="card in statCards" :key="card.key">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon" :style="{ backgroundColor: card.bgColor, color: card.color }">
              <el-icon :size="28"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats[card.key] || 0 }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户增长趋势</span>
            </div>
          </template>
          <div class="bar-chart" v-if="stats.userGrowth && stats.userGrowth.length > 0">
            <div class="bar-item" v-for="(item, index) in stats.userGrowth" :key="index">
              <span class="bar-label">{{ item.name }}</span>
              <div class="bar-track">
                <div
                  class="bar-fill"
                  :style="{ width: (item.value / getMaxValue(stats.userGrowth) * 100) + '%' }"
                ></div>
              </div>
              <span class="bar-value">{{ item.value }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>热门课程 TOP10</span>
            </div>
          </template>
          <div class="bar-chart" v-if="stats.coursePopularity && stats.coursePopularity.length > 0">
            <div class="bar-item" v-for="(item, index) in stats.coursePopularity" :key="index">
              <span class="bar-label">{{ item.name }}</span>
              <div class="bar-track">
                <div
                  class="bar-fill bar-fill-green"
                  :style="{ width: (item.value / getMaxValue(stats.coursePopularity) * 100) + '%' }"
                ></div>
              </div>
              <span class="bar-value">{{ item.value }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 0;
}
.stat-row {
  margin-bottom: 20px;
}
.stat-card {
  border-radius: 8px;
}
.stat-card-content {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
.chart-row {
  margin-bottom: 20px;
}
.chart-card {
  border-radius: 8px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  font-size: 16px;
}
.bar-chart {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.bar-item {
  display: flex;
  align-items: center;
  gap: 10px;
}
.bar-label {
  width: 80px;
  text-align: right;
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.bar-track {
  flex: 1;
  height: 18px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #409EFF, #66b1ff);
  border-radius: 4px;
  transition: width 0.6s ease;
  min-width: 4px;
}
.bar-fill-green {
  background: linear-gradient(90deg, #67C23A, #95d475);
}
.bar-value {
  width: 40px;
  font-size: 13px;
  color: #303133;
  font-weight: 600;
  text-align: left;
}
</style>
