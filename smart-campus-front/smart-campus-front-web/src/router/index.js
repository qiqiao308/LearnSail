import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import WebLayout from '@/layouts/WebLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: WebLayout,
    redirect: '/courses',
    children: [
      {
        path: 'courses',
        name: 'CourseBrowse',
        component: () => import('@/views/course/CourseBrowse.vue'),
        meta: { title: '课程广场' }
      },
      {
        path: 'courses/:id',
        name: 'CourseDetail',
        component: () => import('@/views/course/CourseDetail.vue'),
        meta: { title: '课程详情' }
      },
      {
        path: 'my-courses',
        name: 'MyEnrollments',
        component: () => import('@/views/enrollment/MyEnrollments.vue'),
        meta: { requiresAuth: true, title: '我的课程' }
      },
      {
        path: 'learn/:courseId/:sectionId',
        name: 'LearnSection',
        component: () => import('@/views/learn/LearnSection.vue'),
        meta: { requiresAuth: true, title: '正在学习' }
      },
      {
        path: 'assignments',
        name: 'StudentAssignments',
        component: () => import('@/views/assignment/StudentAssignmentList.vue'),
        meta: { requiresAuth: true, title: '我的作业' }
      },
      {
        path: 'assignments/:id/submit',
        name: 'AssignmentSubmit',
        component: () => import('@/views/assignment/AssignmentSubmit.vue'),
        meta: { requiresAuth: true, title: '提交作业' }
      },
      {
        path: 'exams',
        name: 'StudentExams',
        component: () => import('@/views/exam/StudentExamList.vue'),
        meta: { requiresAuth: true, title: '我的考试' }
      },
      {
        path: 'exam/:id/take',
        name: 'ExamTake',
        component: () => import('@/views/exam/ExamTake.vue'),
        meta: { requiresAuth: true, title: '正在考试' }
      },
      {
        path: 'discussion/:courseId',
        name: 'DiscussionBoard',
        component: () => import('@/views/discussion/DiscussionBoard.vue'),
        meta: { title: '讨论区' }
      },
      {
        path: 'announcements',
        name: 'Announcements',
        component: () => import('@/views/announcement/AnnouncementList.vue'),
        meta: { title: '通知公告' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { requiresAuth: true, title: '个人中心' }
      },
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('@/views/stat/StudentDashboard.vue'),
        meta: { requiresAuth: true, title: '学习概览' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/courses'
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
  } else if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
    next('/courses')
  } else {
    next()
  }
})

export default router
