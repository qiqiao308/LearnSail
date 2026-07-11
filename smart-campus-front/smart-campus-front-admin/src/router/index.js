import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import AdminLayout from '@/layouts/AdminLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: AdminLayout,
    meta: { requiresAuth: true },
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/AdminDashboard.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'users',
        name: 'UserList',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理', role: 'ADMIN' }
      },
      {
        path: 'courses',
        name: 'CourseList',
        component: () => import('@/views/course/CourseList.vue'),
        meta: { title: '课程管理' }
      },
      {
        path: 'courses/create',
        name: 'CourseCreate',
        component: () => import('@/views/course/CourseForm.vue'),
        meta: { title: '创建课程' }
      },
      {
        path: 'courses/:id/edit',
        name: 'CourseEdit',
        component: () => import('@/views/course/CourseForm.vue'),
        meta: { title: '编辑课程' }
      },
      {
        path: 'courses/:id/chapters',
        name: 'ChapterManage',
        component: () => import('@/views/course/CourseChapterManage.vue'),
        meta: { title: '章节管理' }
      },
      {
        path: 'categories',
        name: 'CategoryList',
        component: () => import('@/views/course/CategoryList.vue'),
        meta: { title: '分类管理', role: 'ADMIN' }
      },
      {
        path: 'exams',
        name: 'ExamList',
        component: () => import('@/views/exam/ExamList.vue'),
        meta: { title: '考试管理' }
      },
      {
        path: 'assignments',
        name: 'AssignmentList',
        component: () => import('@/views/assignment/AssignmentList.vue'),
        meta: { title: '作业管理' }
      },
      {
        path: 'announcements',
        name: 'AnnouncementList',
        component: () => import('@/views/announcement/AnnouncementList.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'files',
        name: 'FileManager',
        component: () => import('@/views/file/FileManager.vue'),
        meta: { title: '文件管理' }
      },
      {
        path: 'logs',
        name: 'SystemLog',
        component: () => import('@/views/log/SystemLog.vue'),
        meta: { title: '操作日志', role: 'ADMIN' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth !== false && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.meta.role && userStore.role !== to.meta.role && userStore.role !== 'ADMIN') {
    next('/dashboard')
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
