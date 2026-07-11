# 智慧校园 — 高校在线学习平台

## 📖 项目简介

智慧校园高校在线学习平台是一套**前后端分离**的在线教育系统，面向高校师生提供完整的线上教学解决方案。平台支持课程管理、在线学习、作业提交与批阅、在线考试、讨论互动、数据统计等核心教学场景。

系统分为**管理后台**（管理员/教师使用）和**用户端**（学生/教师使用）两个前端，后端采用微模块架构，管理接口与用户接口独立部署。

---

## 🛠 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.0 | Java 框架 |
| MySQL | 8.0+ | 关系型数据库 |
| MyBatis Plus | 3.5.5 | ORM 框架 |
| Redis | 7.0+ | 缓存与 Token 管理 |
| Spring Security | 6.x | 安全认证框架 |
| JWT (jjwt) | 0.12.3 | 无状态身份认证 |
| Lombok | 1.18.x | 代码简化 |
| Hutool | 5.8.25 | Java 工具库 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.x | 渐进式 JavaScript 框架 |
| Vite | 8.x | 构建工具 |
| Element Plus | 2.x | UI 组件库 |
| Pinia | 3.x | 状态管理 |
| Vue Router | 5.x | 路由管理 |
| Axios | 1.x | HTTP 客户端 |

---

## 🎯 功能模块

### 1. 用户管理
- 三角色体系：**学生** / **教师** / **管理员**
- 注册登录（JWT 无状态认证）
- 个人信息管理、密码修改
- 管理员：用户列表、状态启停、密码重置、角色管理

### 2. 课程管理
- 多级课程分类（支持树形结构）
- 课程 CRUD（标题、描述、封面、价格）
- 课程状态管理：草稿 → 已发布 → 已关闭
- 章节与小节管理（支持排序）

### 3. 内容管理
- 三种内容类型：**视频** / **文档** / **富文本**
- 视频支持 HTTP Range 请求（拖拽播放）
- 文件统一上传管理

### 4. 选课与学习
- 课程广场浏览、搜索、分类筛选
- 课程收藏
- 选课 / 退课
- 学习进度追踪（按小节标记完成）
- 课程进度百分比展示

### 5. 作业管理
- 教师：创建作业（关联课程/章节）、设置截止时间
- 学生：提交作业（文本 + 文件附件）
- 教师：批阅打分、填写评语
- 截止时间校验

### 6. 在线考试
- 四种题型：**单选题** / **多选题** / **判断题** / **简答题**
- 限时考试（前端倒计时 + 后端时间校验）
- 客观题自动评分、简答题教师批阅
- 成绩报告（每题对错、得分详情）

### 7. 讨论区
- 课程级讨论板块
- 发帖 / 回复 / 楼中楼嵌套回复
- 帖子回复数统计

### 8. 通知公告
- 两种类型：**系统公告**（全平台）/ **课程公告**（指定课程）
- 已读/未读状态追踪
- 管理员：公告 CRUD

### 9. 数据统计
- **管理员仪表盘**：用户总数、课程总数、选课总数、日活统计、用户增长趋势、热门课程排行
- **学生仪表盘**：已选课程、已完成课程、学习时长、平均成绩

### 10. 文件管理
- 统一文件上传接口
- 本地存储（按日期分目录）
- 文件元数据管理
- 可扩展 OSS 存储（接口抽象）

---

## 🏗 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层                                │
│  ┌──────────────────────┐  ┌──────────────────────┐        │
│  │  front-admin (3001)  │  │  front-web (3000)    │        │
│  │  管理后台 (Vue3)      │  │  用户端 (Vue3)        │        │
│  │  Element Plus        │  │  Element Plus        │        │
│  └──────────┬───────────┘  └──────────┬───────────┘        │
│             │        /api 代理         │                    │
├─────────────┼─────────────────────────┼────────────────────┤
│             ▼                         ▼                    │
│  ┌──────────────────────┐  ┌──────────────────────┐        │
│  │  admin (6061)        │  │  web (6060)          │        │
│  │  Spring Boot 3       │  │  Spring Boot 3       │        │
│  │  Security + JWT      │  │  Security + JWT      │        │
│  └──────────┬───────────┘  └──────────┬───────────┘        │
│             │                         │                    │
│             └─────────┬───────────────┘                    │
│                       ▼                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │               common 公共模块                          │  │
│  │  PO · DTO · VO · Mapper · Utils · Config · Exception │  │
│  └──────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                        数据层                                │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │  MySQL 8         │  │  Redis           │                │
│  │  业务数据存储      │  │  Token 缓存/黑名单 │                │
│  └──────────────────┘  └──────────────────┘                │
└─────────────────────────────────────────────────────────────┘
```

### 项目依赖关系

```
common (公共模块) ── 无依赖
    ├── admin (管理后台) ── 依赖 common
    └── web (用户端)    ── 依赖 common
```

---

## 📁 目录结构

```
smart-campus/
├── smart-campus-java/                          # 后端 Maven 多模块项目
│   ├── pom.xml                                 # 父 POM
│   ├── common/                                 # 公共模块
│   │   ├── pom.xml
│   │   └── src/main/java/com/smartcampus/common/
│   │       ├── po/           # 数据库实体 (20个)
│   │       ├── dto/          # 请求参数对象 (20个)
│   │       ├── vo/           # 响应视图对象 (24个)
│   │       ├── enums/        # 枚举 (5个)
│   │       ├── mapper/       # MyBatis Plus Mapper (20个)
│   │       ├── exception/    # 异常处理
│   │       ├── utils/        # 工具类 (JWT/BCrypt/Redis/File)
│   │       ├── config/       # 配置 (Security/MyBatisPlus/Redis/CORS)
│   │       └── service/      # 公共服务 (文件存储)
│   ├── admin/                                  # 管理后台模块 (端口 6061)
│   │   ├── pom.xml
│   │   └── src/main/java/com/smartcampus/admin/
│   │       ├── SmartCampusAdminApplication.java
│   │       ├── config/SecurityConfig.java
│   │       ├── controller/   # 10个控制器
│   │       └── service/      # 10个服务接口 + 实现
│   └── web/                                    # 用户端模块 (端口 6060)
│       ├── pom.xml
│       └── src/main/java/com/smartcampus/web/
│           ├── SmartCampusWebApplication.java
│           ├── config/SecurityConfig.java
│           ├── controller/   # 10个控制器
│           └── service/      # 10个服务接口 + 实现
│
├── smart-campus-front/
│   ├── smart-campus-front-admin/               # 管理后台前端 (端口 3001)
│   │   ├── index.html
│   │   ├── package.json
│   │   └── src/
│   │       ├── api/           # API 接口模块
│   │       ├── components/    # 公共组件
│   │       ├── layouts/       # AdminLayout.vue
│   │       ├── router/        # 路由 (12个路由)
│   │       ├── stores/        # Pinia 状态管理
│   │       ├── utils/         # Axios 封装、Auth 工具
│   │       └── views/         # 页面 (11个)
│   │           ├── login/
│   │           ├── dashboard/
│   │           ├── user/
│   │           ├── course/
│   │           ├── exam/
│   │           ├── assignment/
│   │           ├── announcement/
│   │           ├── file/
│   │           └── log/
│   └── smart-campus-front-web/                 # 用户端前端 (端口 3000)
│       ├── index.html
│       ├── package.json
│       └── src/
│           ├── api/           # API 接口模块
│           ├── components/    # 公共组件
│           ├── layouts/       # WebLayout.vue
│           ├── router/        # 路由 (14个路由)
│           ├── stores/        # Pinia 状态管理
│           ├── utils/         # Axios 封装、Auth 工具
│           └── views/         # 页面 (14个)
│               ├── login/
│               ├── course/
│               ├── learn/
│               ├── enrollment/
│               ├── assignment/
│               ├── exam/
│               ├── discussion/
│               ├── announcement/
│               ├── user/
│               └── stat/
```

---

## 🗄 数据库设计

共 **20 张表**，分为 7 大类：

### 用户与认证
| 表名 | 说明 |
|------|------|
| `user` | 用户表（学生/教师/管理员） |

### 课程体系
| 表名 | 说明 |
|------|------|
| `course_category` | 课程分类（树形结构） |
| `course` | 课程表 |
| `course_chapter` | 课程章 |
| `course_section` | 课程节（视频/文档/富文本） |

### 选课与进度
| 表名 | 说明 |
|------|------|
| `enrollment` | 选课记录 |
| `user_favorite` | 课程收藏 |
| `learning_progress` | 学习进度（小节级） |

### 作业体系
| 表名 | 说明 |
|------|------|
| `assignment` | 作业表 |
| `assignment_submission` | 作业提交表 |

### 考试体系
| 表名 | 说明 |
|------|------|
| `exam` | 考试表 |
| `question` | 试题表（单选/多选/判断/简答） |
| `exam_record` | 考试记录 |
| `exam_question_record` | 答题记录 |

### 互动与通知
| 表名 | 说明 |
|------|------|
| `discussion_post` | 讨论帖 |
| `discussion_reply` | 讨论回复（支持嵌套） |
| `announcement` | 公告 |
| `announcement_read` | 公告已读记录 |

### 系统
| 表名 | 说明 |
|------|------|
| `file_info` | 文件信息 |
| `system_log` | 系统操作日志 |

---

## 🔌 API 接口规范

### 命名约定

所有接口遵循 `模块前缀/方法名` 格式：

```
POST /api/courseInfo/loadCourseInfoList      # 加载课程列表
POST /api/courseInfo/getCourseInfo           # 获取课程详情
POST /api/enrollment/enrollCourse            # 选课
POST /api/adminCourse/saveCourse              # 管理端-保存课程
POST /api/adminUser/loadUserList              # 管理端-用户列表
```

### 统一响应格式

```json
{
  "status": "success",
  "code": 200,
  "info": "成功",
  "data": {}
}
```

### Web 模块 API（用户端，端口 6060）

| 模块前缀 | 主要方法 | 说明 |
|----------|----------|------|
| `user` | login, register, getCurrentUser, updateProfile, changePassword | 用户认证与信息 |
| `courseInfo` | loadCourseInfoList, getCourseInfo, loadChapterList | 课程浏览 |
| `enrollment` | enrollCourse, dropCourse, loadMyEnrollments, addFavorite, removeFavorite | 选课与收藏 |
| `progress` | markSectionComplete, getCourseProgress | 学习进度 |
| `assignment` | loadAssignmentList, submitAssignment, getMySubmission, getGrade | 作业 |
| `exam` | loadExamList, startExam, submitExam, getExamScore | 考试 |
| `discussion` | loadPostList, createPost, loadReplyList, createReply | 讨论区 |
| `announcement` | loadAnnouncementList, markRead | 公告 |
| `studentStat` | getMyDashboard, getLearningStats | 学习统计 |
| `file` | uploadFile, downloadFile/{fileId} | 文件 |

### Admin 模块 API（管理端，端口 6061）

| 模块前缀 | 主要方法 | 说明 |
|----------|----------|------|
| `adminUser` | login, loadUserList, updateUserStatus, deleteUser, resetPassword | 用户管理 |
| `adminCourse` | loadCourseList, saveCourse, updateCourse, deleteCourse, publishCourse | 课程管理 |
| `adminCategory` | loadCategoryTree, saveCategory, updateCategory, deleteCategory | 分类管理 |
| `adminChapter` | loadChapterList, saveChapter, updateChapter, deleteChapter, saveSection, updateSection, deleteSection | 章节管理 |
| `adminAssignment` | loadAssignmentList, saveAssignment, deleteAssignment, loadSubmissionList, gradeSubmission | 作业管理 |
| `adminExam` | loadExamList, saveExam, deleteExam, loadQuestionList, saveQuestion, deleteQuestion, loadExamRecords, gradeShortAnswer | 考试管理 |
| `adminAnnouncement` | loadAnnouncementList, saveAnnouncement, updateAnnouncement, deleteAnnouncement | 公告管理 |
| `adminStat` | getDashboardStats, getUserStats, getCourseStats, getDailyActivity | 数据统计 |
| `adminFile` | uploadFile, loadFileList, deleteFile | 文件管理 |
| `adminLog` | loadLogList | 操作日志 |

---

## 🚀 快速启动

### 环境要求

- **JDK** 17+
- **MySQL** 8.0+
- **Redis** 7.0+
- **Node.js** 22.18+ / 24.12+
- **Maven** 3.8+

### 第一步：数据库初始化

```bash
# 1. 连接 MySQL，执行初始化脚本
mysql -u root -p < smart-campus-java/common/src/main/resources/init.sql
```

> ⚠️ **重要**：`init.sql` 中的默认密码为占位符，实际使用前请生成正确的 BCrypt 哈希：
> ```java
> // 在任意 Java 环境执行
> System.out.println(new BCryptPasswordEncoder().encode("admin123"));
> ```
> 将输出替换 `init.sql` 中默认用户的 `password` 字段值。

### 第二步：配置后端

编辑 `smart-campus-java/common/src/main/resources/application-common.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_campus?...  # 修改连接地址
    username: root     # 修改用户名
    password: root     # 修改密码
  data:
    redis:
      host: localhost  # 修改 Redis 地址
      password:         # 修改 Redis 密码（如有）
```

### 第三步：启动后端

```bash
cd smart-campus-java

# 编译打包
mvn clean package -DskipTests

# 启动管理后台模块（端口 6061）
java -jar admin/target/smart-campus-admin-1.0.0.jar

# 新开终端，启动用户端模块（端口 6060）
java -jar web/target/smart-campus-web-1.0.0.jar
```

### 第四步：启动前端

```bash
# 启动管理后台前端（端口 3001）
cd smart-campus-front/smart-campus-front-admin
npm run dev

# 新开终端，启动用户端前端（端口 3000）
cd smart-campus-front/smart-campus-front-web
npm run dev
```

### 第五步：访问系统

| 端 | 地址 | 默认账号 |
|----|------|----------|
| 管理后台 | http://localhost:3001 | `admin` / `admin123` |
| 用户端 | http://localhost:3000 | `student` / `admin123` |

### 端口汇总

| 服务 | 端口 | 说明 |
|------|------|------|
| Admin Backend | 6061 | Spring Boot |
| Web Backend | 6060 | Spring Boot |
| Admin Frontend | 3001 | Vite Dev Server → proxy `/api` → 6061 |
| Web Frontend | 3000 | Vite Dev Server → proxy `/api` → 6060 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |

---

## 👤 默认账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| `admin` | `admin123` | ADMIN | 系统管理员，可访问管理后台全部功能 |
| `teacher` | `admin123` | TEACHER | 教师，可管理课程、作业、考试 |
| `student` | `admin123` | STUDENT | 学生，可浏览课程、学习、提交作业 |

---

## 📐 开发规范

### 后端规范
- **严格分离 PO / DTO / VO**：Controller 不接触 PO，Service 不返回 PO
- **统一响应**：所有接口返回 `ResponseVO<T>` 格式
- **接口命名**：`模块前缀/方法名`（驼峰命名）
- **异常处理**：服务层抛 `BusinessException`，全局 `@ControllerAdvice` 统一捕获
- **分页**：统一使用 `PageDTO`（请求） / `PageVO`（响应）

### 前端规范
- **Vue 3 Composition API**：所有组件使用 `<script setup>`
- **状态管理**：使用 Pinia，按模块拆分 Store
- **HTTP 请求**：统一使用 `@/utils/request.js` 封装的 Axios 实例
- **权限控制**：路由 `meta.requiresAuth` + `beforeEach` 守卫
- **组件库**：统一使用 Element Plus，不引入其他 UI 库

### 代码风格
- 保持与现有代码风格完全一致
- 命名规范统一（Java 驼峰、数据库下划线）
- 优先复用现有模块、工具、枚举
- 禁止修改无关代码
- 禁止覆盖手动修改的代码

---

## 📊 项目统计

| 指标 | 数量 |
|------|------|
| Java 源文件 | 169 |
| Vue/JS 文件 | 42 |
| 数据库表 | 20 |
| API 端点 | 60+ |
| 总源文件 | 233 |

---

## 📄 License

内部项目，仅供学习与教学使用。
