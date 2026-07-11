-- ============================================
-- 高校在线学习平台 - 数据库初始化脚本
-- Database: smart_campus
-- MySQL 8.0+
-- ============================================

CREATE DATABASE IF NOT EXISTS smart_campus
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE smart_campus;

-- ============================================
-- 1. 用户表
-- ============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `role` VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT '角色: STUDENT/TEACHER/ADMIN',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用 0=禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 课程分类表
-- ============================================
DROP TABLE IF EXISTS `course_category`;
CREATE TABLE `course_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID, 0=顶级',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程分类表';

-- ============================================
-- 3. 课程表
-- ============================================
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
    `title` VARCHAR(200) NOT NULL COMMENT '课程标题',
    `description` TEXT COMMENT '课程描述',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `teacher_id` BIGINT NOT NULL COMMENT '教师ID(关联user表)',
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '价格',
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PUBLISHED/CLOSED',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_teacher_id` (`teacher_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- ============================================
-- 4. 课程章表
-- ============================================
DROP TABLE IF EXISTS `course_chapter`;
CREATE TABLE `course_chapter` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '章ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `title` VARCHAR(200) NOT NULL COMMENT '章标题',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程章表';

-- ============================================
-- 5. 课程节表
-- ============================================
DROP TABLE IF EXISTS `course_section`;
CREATE TABLE `course_section` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '节ID',
    `chapter_id` BIGINT NOT NULL COMMENT '章ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `title` VARCHAR(200) NOT NULL COMMENT '节标题',
    `content_type` VARCHAR(20) NOT NULL DEFAULT 'VIDEO' COMMENT '内容类型: VIDEO/DOCUMENT/RICH_TEXT',
    `content_url` VARCHAR(500) DEFAULT NULL COMMENT '内容URL(视频/文档地址)',
    `content_text` LONGTEXT COMMENT '富文本内容',
    `duration` INT NOT NULL DEFAULT 0 COMMENT '时长(秒)',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_chapter_id` (`chapter_id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程节表';

-- ============================================
-- 6. 选课表
-- ============================================
DROP TABLE IF EXISTS `enrollment`;
CREATE TABLE `enrollment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '选课记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ENROLLED' COMMENT '状态: ENROLLED/COMPLETED/DROPPED',
    `enroll_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_course` (`user_id`, `course_id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选课表';

-- ============================================
-- 7. 课程收藏表
-- ============================================
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_course` (`user_id`, `course_id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程收藏表';

-- ============================================
-- 8. 学习进度表
-- ============================================
DROP TABLE IF EXISTS `learning_progress`;
CREATE TABLE `learning_progress` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '进度ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `section_id` BIGINT NOT NULL COMMENT '节ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `is_completed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否完成: 0=未完成 1=已完成',
    `completed_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_section` (`user_id`, `section_id`),
    KEY `idx_user_course` (`user_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习进度表';

-- ============================================
-- 9. 作业表
-- ============================================
DROP TABLE IF EXISTS `assignment`;
CREATE TABLE `assignment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '作业ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `chapter_id` BIGINT DEFAULT NULL COMMENT '章ID(可选,关联章节)',
    `title` VARCHAR(200) NOT NULL COMMENT '作业标题',
    `description` TEXT COMMENT '作业描述',
    `deadline` DATETIME DEFAULT NULL COMMENT '截止时间',
    `max_score` INT NOT NULL DEFAULT 100 COMMENT '满分',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业表';

-- ============================================
-- 10. 作业提交表
-- ============================================
DROP TABLE IF EXISTS `assignment_submission`;
CREATE TABLE `assignment_submission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提交ID',
    `assignment_id` BIGINT NOT NULL COMMENT '作业ID',
    `user_id` BIGINT NOT NULL COMMENT '学生ID',
    `content` TEXT COMMENT '提交内容(文本)',
    `file_url` VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
    `score` INT DEFAULT NULL COMMENT '得分',
    `comment` TEXT COMMENT '教师评语',
    `submit_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `grade_time` DATETIME DEFAULT NULL COMMENT '批阅时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_assignment_user` (`assignment_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业提交表';

-- ============================================
-- 11. 考试表
-- ============================================
DROP TABLE IF EXISTS `exam`;
CREATE TABLE `exam` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '考试ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `title` VARCHAR(200) NOT NULL COMMENT '考试标题',
    `description` TEXT COMMENT '考试描述',
    `duration_minutes` INT NOT NULL DEFAULT 60 COMMENT '考试时长(分钟)',
    `total_score` INT NOT NULL DEFAULT 100 COMMENT '总分',
    `pass_score` INT NOT NULL DEFAULT 60 COMMENT '及格分',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' COMMENT '状态: NOT_STARTED/IN_PROGRESS/ENDED',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试表';

-- ============================================
-- 12. 试题表
-- ============================================
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '试题ID',
    `exam_id` BIGINT NOT NULL COMMENT '考试ID',
    `question_type` VARCHAR(20) NOT NULL COMMENT '题型: SINGLE_CHOICE/MULTIPLE_CHOICE/TRUE_FALSE/SHORT_ANSWER',
    `content` TEXT NOT NULL COMMENT '题目内容',
    `options` TEXT COMMENT '选项JSON: [{"key":"A","value":"选项A"}]',
    `answer` VARCHAR(500) NOT NULL COMMENT '正确答案(单选=A, 多选=A,B, 判断=T/F, 简答=参考答案)',
    `score` INT NOT NULL DEFAULT 5 COMMENT '分值',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    PRIMARY KEY (`id`),
    KEY `idx_exam_id` (`exam_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试题表';

-- ============================================
-- 13. 考试记录表
-- ============================================
DROP TABLE IF EXISTS `exam_record`;
CREATE TABLE `exam_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '考试记录ID',
    `exam_id` BIGINT NOT NULL COMMENT '考试ID',
    `user_id` BIGINT NOT NULL COMMENT '学生ID',
    `score` INT DEFAULT NULL COMMENT '得分',
    `status` VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT '状态: IN_PROGRESS/SUBMITTED/GRADED',
    `start_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    `submit_time` DATETIME DEFAULT NULL COMMENT '提交时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_exam_user` (`exam_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试记录表';

-- ============================================
-- 14. 答题记录表
-- ============================================
DROP TABLE IF EXISTS `exam_question_record`;
CREATE TABLE `exam_question_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '答题记录ID',
    `exam_record_id` BIGINT NOT NULL COMMENT '考试记录ID',
    `question_id` BIGINT NOT NULL COMMENT '试题ID',
    `user_answer` VARCHAR(500) DEFAULT NULL COMMENT '学生答案',
    `is_correct` TINYINT DEFAULT NULL COMMENT '是否正确: 0=错误 1=正确',
    `score` INT NOT NULL DEFAULT 0 COMMENT '得分',
    `question_type` VARCHAR(20) NOT NULL COMMENT '题型',
    PRIMARY KEY (`id`),
    KEY `idx_exam_record_id` (`exam_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答题记录表';

-- ============================================
-- 15. 讨论帖表
-- ============================================
DROP TABLE IF EXISTS `discussion_post`;
CREATE TABLE `discussion_post` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) NOT NULL COMMENT '帖子标题',
    `content` TEXT NOT NULL COMMENT '帖子内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='讨论帖表';

-- ============================================
-- 16. 讨论回复表
-- ============================================
DROP TABLE IF EXISTS `discussion_reply`;
CREATE TABLE `discussion_reply` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '回复ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `parent_reply_id` BIGINT DEFAULT NULL COMMENT '父回复ID(楼中楼)',
    `content` TEXT NOT NULL COMMENT '回复内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_parent_reply_id` (`parent_reply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='讨论回复表';

-- ============================================
-- 17. 公告表
-- ============================================
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content` TEXT NOT NULL COMMENT '公告内容',
    `type` VARCHAR(20) NOT NULL DEFAULT 'SYSTEM' COMMENT '类型: SYSTEM/COURSE',
    `course_id` BIGINT DEFAULT NULL COMMENT '课程ID(课程公告时填写)',
    `publisher_id` BIGINT NOT NULL COMMENT '发布人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- ============================================
-- 18. 公告已读表
-- ============================================
DROP TABLE IF EXISTS `announcement_read`;
CREATE TABLE `announcement_read` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '已读ID',
    `announcement_id` BIGINT NOT NULL COMMENT '公告ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `read_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_announcement_user` (`announcement_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告已读表';

-- ============================================
-- 19. 文件信息表
-- ============================================
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `original_name` VARCHAR(500) NOT NULL COMMENT '原始文件名',
    `stored_name` VARCHAR(500) NOT NULL COMMENT '存储文件名(UUID)',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件相对路径',
    `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
    `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型(image/video/document/other)',
    `mime_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    `uploader_id` BIGINT NOT NULL COMMENT '上传人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件信息表';

-- ============================================
-- 20. 系统日志表
-- ============================================
DROP TABLE IF EXISTS `system_log`;
CREATE TABLE `system_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `module` VARCHAR(50) NOT NULL COMMENT '操作模块',
    `operation` VARCHAR(100) NOT NULL COMMENT '操作类型',
    `params` TEXT COMMENT '请求参数',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';


-- ============================================
-- 示例数据
-- ============================================
-- 密码明文均为: 123456
-- BCrypt: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh
-- ⚠️ 生产环境请在Java中用 new BCryptPasswordEncoder().encode("123456") 重新生成
-- ============================================


-- ============================================
-- 🔹 用户数据 (15人: 1管理员 + 4教师 + 10学生)
-- ============================================
INSERT INTO `user` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `role`, `avatar`, `status`, `create_time`) VALUES
(1,  'admin',    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', 'admin@smartcampus.edu.cn',  '13800000001', 'ADMIN',  NULL, 1, '2026-01-01 00:00:00'),
(2,  'zhangwei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '张伟',       'zhangwei@smartcampus.edu.cn', '13800000002', 'TEACHER', NULL, 1, '2026-01-15 08:00:00'),
(3,  'wangfang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '王芳',       'wangfang@smartcampus.edu.cn', '13800000003', 'TEACHER', NULL, 1, '2026-01-16 09:00:00'),
(4,  'zhaoqiang','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '赵强',       'zhaoqiang@smartcampus.edu.cn','13800000004', 'TEACHER', NULL, 1, '2026-01-17 10:00:00'),
(5,  'chenli',   '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '陈丽',       'chenli@smartcampus.edu.cn',   '13800000005', 'TEACHER', NULL, 1, '2026-01-18 11:00:00'),
(6,  'liuyang',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '刘阳',       'liuyang@smartcampus.edu.cn',  '13800000006', 'STUDENT', NULL, 1, '2026-02-20 12:00:00'),
(7,  'sunyue',   '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '孙悦',       'sunyue@smartcampus.edu.cn',   '13800000007', 'STUDENT', NULL, 1, '2026-02-20 12:30:00'),
(8,  'zhoujie',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '周杰',       'zhoujie@smartcampus.edu.cn',  '13800000008', 'STUDENT', NULL, 1, '2026-02-21 13:00:00'),
(9,  'wumei',    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '吴梅',       'wumei@smartcampus.edu.cn',    '13800000009', 'STUDENT', NULL, 1, '2026-02-21 14:00:00'),
(10, 'zhenghao', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '郑浩',       'zhenghao@smartcampus.edu.cn', '13800000010', 'STUDENT', NULL, 1, '2026-02-22 15:00:00'),
(11, 'huangli',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '黄丽',       'huangli@smartcampus.edu.cn',  '13800000011', 'STUDENT', NULL, 1, '2026-02-22 16:00:00'),
(12, 'xuming',   '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '许明',       'xuming@smartcampus.edu.cn',   '13800000012', 'STUDENT', NULL, 1, '2026-02-23 17:00:00'),
(13, 'linxin',   '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '林欣',       'linxin@smartcampus.edu.cn',   '13800000013', 'STUDENT', NULL, 1, '2026-02-23 18:00:00'),
(14, 'mayun',    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '马芸',       'mayun@smartcampus.edu.cn',    '13800000014', 'STUDENT', NULL, 1, '2026-02-24 19:00:00'),
(15, 'luwei',    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '陆伟',       'luwei@smartcampus.edu.cn',    '13800000015', 'STUDENT', NULL, 0, '2026-02-24 20:00:00');


-- ============================================
-- 🔹 课程分类 (14条: 4个顶级 + 10个子分类)
-- ============================================
INSERT INTO `course_category` (`id`, `name`, `parent_id`, `sort_order`) VALUES
-- 顶级分类
(1,  '计算机科学', 0, 1),
(2,  '数学与统计', 0, 2),
(3,  '外国语言',   0, 3),
(4,  '人文社科',   0, 4),
-- 计算机科学子分类
(5,  '程序设计',   1, 1),
(6,  '数据结构与算法', 1, 2),
(7,  '人工智能',   1, 3),
(8,  'Web前端开发', 1, 4),
-- 数学与统计子分类
(9,  '高等数学',   2, 1),
(10, '线性代数',   2, 2),
(11, '概率论与数理统计', 2, 3),
-- 外国语言子分类
(12, '大学英语',   3, 1),
(13, '商务英语',   3, 2),
-- 人文社科子分类
(14, '经济学原理', 4, 1);


-- ============================================
-- 🔹 课程 (6门, 教师ID对应: 2=张伟 3=王芳 4=赵强 5=陈丽 6=刘阳...换个老师)
-- ============================================
INSERT INTO `course` (`id`, `title`, `description`, `cover_image`, `category_id`, `teacher_id`, `price`, `status`, `create_time`) VALUES
(1, 'Java程序设计入门',
    '本课程面向零基础学生，系统讲解Java编程语言的核心知识。从环境搭建、基本语法、面向对象编程，到集合框架、IO流、多线程等高级特性，通过大量实战案例帮助学员掌握企业级Java开发技能。课程包含视频讲解、课后练习和项目实战。',
    '/upload/covers/java-course.jpg', 5, 2, 0.00, 'PUBLISHED', '2026-03-01 08:00:00'),

(2, '数据结构与算法精讲',
    '数据结构是计算机专业的核心基础课。本课程深入讲解线性表、栈、队列、树、图等经典数据结构，以及排序、查找、动态规划、贪心、回溯等核心算法。每个知识点配有LeetCode真题练习，帮助学员提升编程思维和面试竞争力。',
    '/upload/covers/data-structure.jpg', 6, 3, 0.00, 'PUBLISHED', '2026-03-05 09:00:00'),

(3, 'Python人工智能入门',
    '从零开始学习Python编程与人工智能基础。课程涵盖Python语法、NumPy/Pandas数据处理、Matplotlib可视化、Scikit-learn机器学习实战，以及深度学习入门（TensorFlow/Keras）。适合对AI感兴趣的初学者，无需编程基础。',
    '/upload/covers/python-ai.jpg', 7, 4, 99.00, 'PUBLISHED', '2026-03-10 10:00:00'),

(4, 'Web前端开发实战',
    '全面掌握HTML5、CSS3、JavaScript（ES6+）以及Vue3框架。课程以项目驱动，带领学员从零搭建一个完整的电商前端项目，涵盖响应式布局、组件化开发、状态管理、路由设计、API对接等实用技能。',
    '/upload/covers/web-frontend.jpg', 8, 5, 0.00, 'PUBLISHED', '2026-03-15 11:00:00'),

(5, '高等数学精讲（上）',
    '系统讲解函数与极限、导数与微分、微分中值定理、不定积分、定积分、微分方程等高等数学核心内容。注重概念理解与解题技巧并重，每章配有精选习题讲解。适合大一新生及考研备考学生。',
    '/upload/covers/advanced-math.jpg', 9, 2, 0.00, 'PUBLISHED', '2026-03-20 12:00:00'),

(6, '大学英语四级通关秘籍',
    '针对CET-4考试的系统备考课程。涵盖听力、阅读、写作、翻译四大模块的解题技巧与真题精讲。包含高频词汇速记、长难句分析、作文模板等实用内容，帮助学员高效备考、顺利通过四级考试。',
    '/upload/covers/cet4.jpg', 12, 3, 49.90, 'DRAFT', '2026-04-01 08:00:00');


-- ============================================
-- 🔹 课程章节与小节
-- ============================================

-- ==== 课程1: Java程序设计入门 ====
INSERT INTO `course_chapter` (`id`, `course_id`, `title`, `sort_order`) VALUES
(1, 1, '第一章 Java语言概述', 1),
(2, 1, '第二章 面向对象编程基础', 2),
(3, 1, '第三章 面向对象高级特性', 3),
(4, 1, '第四章 Java常用类与集合框架', 4),
(5, 1, '第五章 IO流与多线程', 5);

INSERT INTO `course_section` (`id`, `chapter_id`, `course_id`, `title`, `content_type`, `content_url`, `content_text`, `duration`, `sort_order`) VALUES
-- 第一章
(1,  1, 1, '1.1 Java发展历史与特性',     'VIDEO',      '/upload/videos/java-1-1.mp4', NULL, 1200, 1),
(2,  1, 1, '1.2 JDK安装与环境配置',       'RICH_TEXT',  NULL, '<h2>JDK安装与环境配置</h2><p>本节详细介绍Windows、macOS、Linux系统下JDK的下载与安装步骤，以及环境变量PATH和JAVA_HOME的配置方法。</p><h3>第一步：下载JDK</h3><p>访问Oracle官网下载JDK 17 LTS版本...</p><h3>第二步：安装</h3><p>双击安装包，按提示完成安装...</p><h3>第三步：配置环境变量</h3><p>Windows系统：右键"此电脑" → 属性 → 高级系统设置 → 环境变量...</p>', 0, 2),
(3,  1, 1, '1.3 第一个Java程序：Hello World','VIDEO',   '/upload/videos/java-1-3.mp4', NULL, 900,  3),
-- 第二章
(4,  2, 1, '2.1 类与对象的基本概念',      'VIDEO',      '/upload/videos/java-2-1.mp4', NULL, 1500, 1),
(5,  2, 1, '2.2 构造方法与this关键字',    'RICH_TEXT',  NULL, '<h2>构造方法与this关键字</h2><p>构造方法是类的特殊成员，用于创建对象时初始化对象的状态...</p><h3>构造方法的定义</h3><pre><code>public class Student {\n    private String name;\n    \n    // 无参构造\n    public Student() {}\n    \n    // 有参构造\n    public Student(String name) {\n        this.name = name;\n    }\n}</code></pre>', 0, 2),
(6,  2, 1, '2.3 封装与访问控制',          'VIDEO',      '/upload/videos/java-2-3.mp4', NULL, 1100, 3),
(7,  2, 1, '2.4 static关键字与代码块',    'DOCUMENT',   '/upload/docs/java-static.pdf', NULL, 0, 4),
-- 第三章
(8,  3, 1, '3.1 继承与super关键字',      'VIDEO',      '/upload/videos/java-3-1.mp4', NULL, 1300, 1),
(9,  3, 1, '3.2 多态与抽象类',            'VIDEO',      '/upload/videos/java-3-2.mp4', NULL, 1400, 2),
(10, 3, 1, '3.3 接口与默认方法',          'RICH_TEXT',  NULL, '<h2>接口与默认方法</h2><p>Java 8引入了接口的默认方法（default method）和静态方法...</p><h3>接口的定义</h3><pre><code>public interface Flyable {\n    void fly();\n    \n    default void land() {\n        System.out.println("Landing...");\n    }\n}</code></pre>', 0, 3),
-- 第四章
(11, 4, 1, '4.1 String与StringBuilder',  'VIDEO',      '/upload/videos/java-4-1.mp4', NULL, 1000, 1),
(12, 4, 1, '4.2 ArrayList与LinkedList',  'VIDEO',      '/upload/videos/java-4-2.mp4', NULL, 1600, 2),
(13, 4, 1, '4.3 HashMap与HashSet原理',   'DOCUMENT',   '/upload/docs/java-hashmap.pdf', NULL, 0, 3),
-- 第五章
(14, 5, 1, '5.1 File类与字节流',         'VIDEO',      '/upload/videos/java-5-1.mp4', NULL, 1200, 1),
(15, 5, 1, '5.2 线程的创建与启动',       'VIDEO',      '/upload/videos/java-5-2.mp4', NULL, 1500, 2);

-- ==== 课程2: 数据结构与算法精讲 ====
INSERT INTO `course_chapter` (`id`, `course_id`, `title`, `sort_order`) VALUES
(6,  2, '第一章 线性结构', 1),
(7,  2, '第二章 树与图',   2),
(8,  2, '第三章 查找与排序', 3),
(9,  2, '第四章 算法设计策略', 4);

INSERT INTO `course_section` (`id`, `chapter_id`, `course_id`, `title`, `content_type`, `content_url`, `content_text`, `duration`, `sort_order`) VALUES
(16, 6,  2, '1.1 数组与稀疏数组',       'VIDEO',   '/upload/videos/ds-1-1.mp4', NULL, 1100, 1),
(17, 6,  2, '1.2 单向链表与双向链表',   'VIDEO',   '/upload/videos/ds-1-2.mp4', NULL, 1800, 2),
(18, 6,  2, '1.3 栈的应用：表达式求值', 'RICH_TEXT', NULL, '<h2>栈的应用：表达式求值</h2><p>栈是一种后进先出（LIFO）的数据结构。在中缀表达式转后缀表达式并求值的问题中，栈发挥着关键作用...</p>', 0, 3),
(19, 7,  2, '2.1 二叉树遍历（递归与非递归）', 'VIDEO', '/upload/videos/ds-2-1.mp4', NULL, 2000, 1),
(20, 7,  2, '2.2 二叉搜索树与平衡树',   'VIDEO',   '/upload/videos/ds-2-2.mp4', NULL, 1700, 2),
(21, 7,  2, '2.3 图的DFS与BFS遍历',     'DOCUMENT','/upload/docs/ds-graph.pdf', NULL, 0, 3),
(22, 8,  2, '3.1 二分查找与变体',       'VIDEO',   '/upload/videos/ds-3-1.mp4', NULL, 1200, 1),
(23, 8,  2, '3.2 快速排序与归并排序',   'VIDEO',   '/upload/videos/ds-3-2.mp4', NULL, 1900, 2),
(24, 9,  2, '4.1 动态规划入门',         'VIDEO',   '/upload/videos/ds-4-1.mp4', NULL, 1600, 1),
(25, 9,  2, '4.2 贪心算法与回溯法',     'RICH_TEXT', NULL, '<h2>贪心算法与回溯法</h2><p>贪心算法每一步都选择当前最优解，而回溯法通过试探性搜索寻找全局最优...</p><h3>经典问题：0-1背包</h3><p>贪心策略无法求解0-1背包，但可以用动态规划或回溯法...</p>', 0, 2);

-- ==== 课程3: Python人工智能入门 ====
INSERT INTO `course_chapter` (`id`, `course_id`, `title`, `sort_order`) VALUES
(10, 3, '第一章 Python基础语法', 1),
(11, 3, '第二章 数据处理与可视化', 2),
(12, 3, '第三章 机器学习基础', 3);

INSERT INTO `course_section` (`id`, `chapter_id`, `course_id`, `title`, `content_type`, `content_url`, `content_text`, `duration`, `sort_order`) VALUES
(26, 10, 3, '1.1 Python环境搭建与Jupyter', 'VIDEO',   '/upload/videos/py-1-1.mp4', NULL, 1000, 1),
(27, 10, 3, '1.2 变量、数据类型与运算符', 'VIDEO',   '/upload/videos/py-1-2.mp4', NULL, 1400, 2),
(28, 10, 3, '1.3 列表、元组与字典',      'RICH_TEXT', NULL, '<h2>Python核心数据结构</h2><p>列表(list)、元组(tuple)和字典(dict)是Python最常用的三种内置数据结构...</p>', 0, 3),
(29, 11, 3, '2.1 NumPy数组操作',         'VIDEO',   '/upload/videos/py-2-1.mp4', NULL, 1500, 1),
(30, 11, 3, '2.2 Pandas数据分析',        'VIDEO',   '/upload/videos/py-2-2.mp4', NULL, 1800, 2),
(31, 11, 3, '2.3 Matplotlib数据可视化',  'DOCUMENT','/upload/docs/py-matplotlib.pdf', NULL, 0, 3),
(32, 12, 3, '3.1 Scikit-learn入门',     'VIDEO',   '/upload/videos/py-3-1.mp4', NULL, 1600, 1),
(33, 12, 3, '3.2 分类与回归实战',       'VIDEO',   '/upload/videos/py-3-2.mp4', NULL, 2000, 2);

-- ==== 课程4: Web前端开发实战 ====
INSERT INTO `course_chapter` (`id`, `course_id`, `title`, `sort_order`) VALUES
(13, 4, '第一章 HTML5与CSS3', 1),
(14, 4, '第二章 JavaScript核心', 2),
(15, 4, '第三章 Vue3实战', 3);

INSERT INTO `course_section` (`id`, `chapter_id`, `course_id`, `title`, `content_type`, `content_url`, `content_text`, `duration`, `sort_order`) VALUES
(34, 13, 4, '1.1 HTML5语义化标签',   'VIDEO',   '/upload/videos/web-1-1.mp4', NULL, 900,  1),
(35, 13, 4, '1.2 CSS3弹性布局与Grid',  'VIDEO',   '/upload/videos/web-1-2.mp4', NULL, 1300, 2),
(36, 13, 4, '1.3 响应式设计与媒体查询', 'RICH_TEXT', NULL, '<h2>响应式设计</h2><p>响应式Web设计（Responsive Web Design）是指网站能够自适应不同设备的屏幕尺寸...</p><h3>媒体查询</h3><pre><code>@media (max-width: 768px) {\n  .container {\n    flex-direction: column;\n  }\n}</code></pre>', 0, 3),
(37, 14, 4, '2.1 ES6新特性（箭头函数、解构、Promise）', 'VIDEO', '/upload/videos/web-2-1.mp4', NULL, 1600, 1),
(38, 14, 4, '2.2 DOM操作与事件机制',   'VIDEO',   '/upload/videos/web-2-2.mp4', NULL, 1400, 2),
(39, 15, 4, '3.1 Vue3组合式API',       'VIDEO',   '/upload/videos/web-3-1.mp4', NULL, 1500, 1),
(40, 15, 4, '3.2 Vue Router与Pinia状态管理', 'VIDEO', '/upload/videos/web-3-2.mp4', NULL, 1700, 2);

-- ==== 课程5: 高等数学精讲（上） ====
INSERT INTO `course_chapter` (`id`, `course_id`, `title`, `sort_order`) VALUES
(16, 5, '第一章 函数与极限', 1),
(17, 5, '第二章 导数与微分', 2),
(18, 5, '第三章 不定积分与定积分', 3);

INSERT INTO `course_section` (`id`, `chapter_id`, `course_id`, `title`, `content_type`, `content_url`, `content_text`, `duration`, `sort_order`) VALUES
(41, 16, 5, '1.1 函数概念与初等函数',   'VIDEO', '/upload/videos/math-1-1.mp4', NULL, 1200, 1),
(42, 16, 5, '1.2 数列极限的定义与计算', 'VIDEO', '/upload/videos/math-1-2.mp4', NULL, 1800, 2),
(43, 16, 5, '1.3 无穷小与无穷大',      'RICH_TEXT', NULL, '<h2>无穷小与无穷大</h2><p>如果函数f(x)当x→x₀时的极限为0，则称f(x)为当x→x₀时的无穷小...</p><h3>无穷小的比较</h3><p>设α和β是同一过程中的两个无穷小：</p><ul><li>若lim(α/β)=0，称α是比β高阶的无穷小</li><li>若lim(α/β)=c≠0，称α与β是同阶无穷小</li></ul>', 0, 3),
(44, 17, 5, '2.1 导数的定义与几何意义', 'VIDEO', '/upload/videos/math-2-1.mp4', NULL, 1500, 1),
(45, 17, 5, '2.2 求导法则与高阶导数',   'VIDEO', '/upload/videos/math-2-2.mp4', NULL, 1400, 2),
(46, 18, 5, '3.1 不定积分的概念与换元法', 'VIDEO', '/upload/videos/math-3-1.mp4', NULL, 1600, 1),
(47, 18, 5, '3.2 定积分的应用',         'DOCUMENT', '/upload/docs/math-integral.pdf', NULL, 0, 2);


-- ============================================
-- 🔹 选课记录
-- ============================================
INSERT INTO `enrollment` (`id`, `user_id`, `course_id`, `status`, `enroll_time`, `complete_time`) VALUES
-- 学生刘阳(6): 选了3门课
(1,  6, 1, 'ENROLLED',  '2026-03-10 08:00:00', NULL),
(2,  6, 2, 'ENROLLED',  '2026-03-12 09:00:00', NULL),
(3,  6, 5, 'DROPPED',  '2026-03-15 10:00:00', NULL),
-- 学生孙悦(7): 选了4门课
(4,  7, 1, 'ENROLLED',  '2026-03-11 08:30:00', NULL),
(5,  7, 3, 'ENROLLED',  '2026-03-20 10:00:00', NULL),
(6,  7, 4, 'COMPLETED', '2026-03-18 09:00:00', '2026-06-20 18:00:00'),
(7,  7, 5, 'ENROLLED',  '2026-04-01 11:00:00', NULL),
-- 学生周杰(8): 选了2门课
(8,  8, 2, 'ENROLLED',  '2026-03-14 10:00:00', NULL),
(9,  8, 4, 'ENROLLED',  '2026-03-25 14:00:00', NULL),
-- 学生吴梅(9): 选了2门课
(10, 9, 1, 'COMPLETED', '2026-03-05 08:00:00', '2026-06-10 17:00:00'),
(11, 9, 3, 'ENROLLED',  '2026-03-22 15:00:00', NULL),
-- 学生郑浩(10): 选了3门课
(12, 10, 2, 'ENROLLED',  '2026-03-16 11:00:00', NULL),
(13, 10, 4, 'ENROLLED',  '2026-04-02 09:00:00', NULL),
(14, 10, 5, 'ENROLLED',  '2026-03-28 13:00:00', NULL),
-- 学生黄丽(11): 选了1门课
(15, 11, 3, 'ENROLLED',  '2026-04-05 10:00:00', NULL),
-- 学生许明(12): 选了2门课
(16, 12, 1, 'ENROLLED',  '2026-03-08 09:00:00', NULL),
(17, 12, 2, 'ENROLLED',  '2026-03-20 14:00:00', NULL),
-- 学生林欣(13): 选了2门课
(18, 13, 4, 'ENROLLED',  '2026-04-10 08:00:00', NULL),
(19, 13, 5, 'ENROLLED',  '2026-04-12 16:00:00', NULL),
-- 学生马芸(14): 选了1门课
(20, 14, 3, 'ENROLLED',  '2026-04-15 12:00:00', NULL);


-- ============================================
-- 🔹 课程收藏
-- ============================================
INSERT INTO `user_favorite` (`id`, `user_id`, `course_id`, `create_time`) VALUES
(1,  6,  3, '2026-03-18 10:00:00'),
(2,  7,  2, '2026-03-20 11:00:00'),
(3,  8,  3, '2026-03-25 14:00:00'),
(4,  9,  4, '2026-03-28 09:00:00'),
(5,  10, 3, '2026-04-01 15:00:00'),
(6,  11, 1, '2026-04-05 16:00:00'),
(7,  12, 4, '2026-04-08 13:00:00'),
(8,  13, 1, '2026-04-12 10:00:00');


-- ============================================
-- 🔹 学习进度
-- ============================================

-- 刘阳(6) 课程1(Java) 完成了前3节
INSERT INTO `learning_progress` (`id`, `user_id`, `section_id`, `course_id`, `is_completed`, `completed_time`) VALUES
(1,  6, 1,  1, 1, '2026-03-10 10:00:00'),
(2,  6, 2,  1, 1, '2026-03-11 09:00:00'),
(3,  6, 3,  1, 1, '2026-03-12 10:30:00'),
-- 刘阳(6) 课程2(数据结构) 完成了前2节
(4,  6, 16, 2, 1, '2026-03-20 11:00:00'),
(5,  6, 17, 2, 1, '2026-03-22 14:00:00'),

-- 孙悦(7) 课程1(Java) 完成了前5节
(6,  7, 1,  1, 1, '2026-03-11 09:00:00'),
(7,  7, 2,  1, 1, '2026-03-12 10:00:00'),
(8,  7, 3,  1, 1, '2026-03-13 11:00:00'),
(9,  7, 4,  1, 1, '2026-03-15 09:30:00'),
(10, 7, 5,  1, 1, '2026-03-16 14:00:00'),
-- 孙悦(7) 课程4(Web前端) 完成了全部小节 (= COMPLETED)
(11, 7, 34, 4, 1, '2026-03-20 10:00:00'),
(12, 7, 35, 4, 1, '2026-03-22 11:00:00'),
(13, 7, 36, 4, 1, '2026-03-25 15:00:00'),
(14, 7, 37, 4, 1, '2026-04-01 09:00:00'),
(15, 7, 38, 4, 1, '2026-04-05 10:30:00'),
(16, 7, 39, 4, 1, '2026-04-10 14:00:00'),
(17, 7, 40, 4, 1, '2026-04-15 16:00:00'),
-- 孙悦(7) 课程3(Python) 完成了前2节
(18, 7, 26, 3, 1, '2026-03-25 09:00:00'),
(19, 7, 27, 3, 1, '2026-03-27 10:00:00'),

-- 吴梅(9) 课程1(Java) 全部完成 (= COMPLETED)
(20, 9, 1,  1, 1, '2026-03-06 08:00:00'),
(21, 9, 2,  1, 1, '2026-03-07 09:00:00'),
(22, 9, 3,  1, 1, '2026-03-08 10:00:00'),
(23, 9, 4,  1, 1, '2026-03-10 08:30:00'),
(24, 9, 5,  1, 1, '2026-03-12 09:00:00'),
(25, 9, 6,  1, 1, '2026-03-15 10:00:00'),
(26, 9, 7,  1, 1, '2026-03-18 11:00:00'),
(27, 9, 8,  1, 1, '2026-03-20 08:00:00'),
(28, 9, 9,  1, 1, '2026-03-22 09:30:00'),
(29, 9, 10, 1, 1, '2026-03-25 14:00:00'),
(30, 9, 11, 1, 1, '2026-03-28 10:00:00'),
(31, 9, 12, 1, 1, '2026-04-01 11:00:00'),
(32, 9, 13, 1, 1, '2026-04-05 15:00:00'),
(33, 9, 14, 1, 1, '2026-04-10 09:00:00'),
(34, 9, 15, 1, 1, '2026-04-15 10:00:00'),

-- 许明(12) 课程1(Java) 完成了前2节
(35, 12, 1, 1, 1, '2026-03-09 10:00:00'),
(36, 12, 2, 1, 1, '2026-03-10 09:00:00'),

-- 郑浩(10) 课程2(数据结构) 完成了前4节
(37, 10, 16, 2, 1, '2026-03-18 10:00:00'),
(38, 10, 17, 2, 1, '2026-03-20 11:30:00'),
(39, 10, 18, 2, 1, '2026-03-23 14:00:00'),
(40, 10, 19, 2, 1, '2026-03-28 09:00:00');


-- ============================================
-- 🔹 作业 (8个, 分布在4门课程)
-- ============================================
INSERT INTO `assignment` (`id`, `course_id`, `chapter_id`, `title`, `description`, `deadline`, `max_score`, `create_time`) VALUES
-- 课程1: Java程序设计
(1, 1, 1, '第一章作业：编写HelloWorld程序',
     '请编写一个Java程序，在控制台输出"Hello, World!"，并解释main方法的各个组成部分。要求代码规范、注释清晰。',
     '2026-04-15 23:59:59', 100, '2026-03-10 08:00:00'),
(2, 1, 2, '第二章作业：设计学生类',
     '定义一个Student类，包含学号、姓名、年龄三个私有属性，提供构造方法和getter/setter方法，并编写测试类验证。要求体现封装思想。',
     '2026-05-01 23:59:59', 100, '2026-03-20 08:00:00'),
(3, 1, 4, '第四章作业：集合框架练习',
     '使用ArrayList存储10个随机整数，完成以下操作：1) 排序输出 2) 查找最大值和最小值 3) 去重。提交完整代码和运行截图。',
     '2026-05-20 23:59:59', 100, '2026-04-01 08:00:00'),

-- 课程2: 数据结构与算法
(4, 2, 6, '第一章作业：实现单链表',
     '请手动实现一个单链表（LinkedList），包含add、remove、get、size方法。要求使用泛型，不得使用Java内置集合类。提交完整代码。',
     '2026-04-30 23:59:59', 100, '2026-03-15 08:00:00'),
(5, 2, 8, '第三章作业：排序算法比较',
     '编写程序对比冒泡排序、快速排序和归并排序在处理不同规模数据（1000、10000、100000条）时的性能差异。提交代码和对比分析报告。',
     '2026-06-01 23:59:59', 100, '2026-04-01 08:00:00'),

-- 课程3: Python人工智能入门
(6, 3, 10, '第一章作业：Python基础练习',
     '完成以下Python编程题：1) 列表推导式练习 2) 字典操作练习 3) 函数定义与lambda表达式练习。每题需有输入输出示例。',
     '2026-04-20 23:59:59', 100, '2026-03-20 08:00:00'),

-- 课程4: Web前端开发实战
(7, 4, 13, '第一章作业：个人主页设计',
     '使用HTML5和CSS3设计一个个人主页，要求：1) 使用语义化标签 2) 使用Flexbox或Grid布局 3) 包含导航栏、个人简介、技能列表和联系表单 4) 适配移动端。提交HTML和CSS文件。',
     '2026-04-25 23:59:59', 100, '2026-03-25 08:00:00'),
(8, 4, 15, '第三章作业：Vue3 Todo应用',
     '使用Vue3组合式API开发一个Todo待办事项应用。功能要求：1) 添加/删除/编辑待办事项 2) 标记完成状态 3) 筛选（全部/已完成/未完成）4) 使用Pinia管理状态 5) 数据持久化到localStorage。',
     '2026-06-15 23:59:59', 100, '2026-04-20 08:00:00');


-- ============================================
-- 🔹 作业提交 (12条)
-- ============================================
INSERT INTO `assignment_submission` (`id`, `assignment_id`, `user_id`, `content`, `file_url`, `score`, `comment`, `submit_time`, `grade_time`) VALUES
-- 刘阳(6) 提交作业1和2
(1,  1, 6,  '我编写了HelloWorld程序，main方法是Java程序的入口点，public static void main(String[] args)中每个关键字都有特定含义...', NULL, 95, '代码规范，注释清晰，继续保持！', '2026-04-10 15:00:00', '2026-04-12 10:00:00'),
(2,  2, 6,  '定义了Student类，包含id、name、age三个私有属性，提供了全参构造和无参构造，以及完整的getter/setter方法。测试类创建了3个Student对象并打印信息。', '/upload/submissions/student6-as2.zip', 88, '封装思想理解到位，建议属性增加合法性校验。', '2026-04-25 16:00:00', '2026-04-28 09:00:00'),
-- 孙悦(7) 提交作业1、4、7
(3,  1, 7,  'HelloWorld程序已编写，同时我还额外演示了使用System.out.println和System.out.print的区别...', NULL, 98, '非常优秀！额外的探索精神值得表扬。', '2026-04-08 14:00:00', '2026-04-11 11:00:00'),
(4,  4, 7,  '实现了泛型单链表LinkedList<T>，包含Node内部类，add方法支持尾插法，remove按索引删除...', '/upload/submissions/student7-as4.zip', 92, '代码结构清晰，建议考虑边界情况（空链表删除等）。', '2026-04-20 17:00:00', '2026-04-23 10:00:00'),
(5,  7, 7,  '个人主页设计完成，使用了header/nav/main/section/footer语义化标签，Flexbox布局，媒体查询适配了手机和桌面端...', '/upload/submissions/student7-as7.zip', 96, '设计美观，响应式效果很好！', '2026-04-18 16:00:00', '2026-04-21 14:00:00'),
-- 周杰(8) 提交作业4和7
(6,  4, 8,  '单链表实现完成，使用了泛型，add/remove/get/size方法均已实现并通过测试。', '/upload/submissions/student8-as4.zip', 85, '功能实现正确，但代码注释偏少，建议增加关键方法的注释。', '2026-04-22 10:00:00', '2026-04-25 15:00:00'),
(7,  7, 8,  '个人主页已提交，包含导航栏、个人简介、技能列表和联系表单。移动端适配使用了768px断点。', NULL, 78, '基本功能实现，但移动端细节有待优化，导航栏在小屏幕上体验不好。', '2026-04-24 18:00:00', '2026-04-27 09:00:00'),
-- 吴梅(9) 提交作业1、2、3（全部分高）
(8,  1, 9,  'HelloWorld程序及main方法详解...', NULL, 97, '优秀', '2026-04-05 10:00:00', '2026-04-08 10:00:00'),
(9,  2, 9,  'Student类设计及测试...', '/upload/submissions/student9-as2.zip', 95, '代码规范，测试充分。', '2026-04-22 11:00:00', '2026-04-25 10:00:00'),
(10, 3, 9,  '集合框架练习完成...包含ArrayList排序、查找最值、去重的完整实现。', '/upload/submissions/student9-as3.zip', NULL, NULL, '2026-05-15 14:00:00', NULL),
-- 郑浩(10) 提交作业4
(11, 4, 10, '单链表实现，支持泛型，包含完整的add/remove/get/size方法...', '/upload/submissions/student10-as4.zip', 80, '基本功能正确，但remove方法效率可优化，建议维护size变量。', '2026-04-25 20:00:00', '2026-04-28 16:00:00'),
-- 黄丽(11) 提交作业6
(12, 6, 11, 'Python基础练习完成，包含列表推导式、字典操作和lambda表达式三道题...', '/upload/submissions/student11-as6.ipynb', 90, 'Python基础扎实，lambda表达式使用正确。', '2026-04-15 12:00:00', '2026-04-18 10:00:00');


-- ============================================
-- 🔹 考试 (4场)
-- ============================================
INSERT INTO `exam` (`id`, `course_id`, `title`, `description`, `duration_minutes`, `total_score`, `pass_score`, `start_time`, `end_time`, `status`, `create_time`) VALUES
-- 考试1: Java期末 (已结束)
(1, 1, 'Java程序设计期末测验',
    '本次测验覆盖Java程序设计课程的全部内容，包括Java基础语法、面向对象编程、集合框架、IO流和多线程。请认真作答，考试时间为90分钟。',
    90, 100, 60, '2026-05-01 09:00:00', '2026-05-01 23:59:59', 'ENDED', '2026-04-20 08:00:00'),
-- 考试2: 数据结构期中 (进行中)
(2, 2, '数据结构与算法期中测验',
    '考查线性结构和树结构的基础知识，包括数组、链表、栈、队列、二叉树遍历等核心概念的理解与编程实现能力。',
    60, 100, 60, '2026-07-01 00:00:00', '2026-08-31 23:59:59', 'IN_PROGRESS', '2026-06-15 08:00:00'),
-- 考试3: Web前端基础 (未开始)
(3, 4, 'Web前端开发基础测验',
    '覆盖HTML5语义化标签、CSS3布局方式、JavaScript核心概念和ES6新特性的基础知识。',
    45, 80, 48, '2026-09-01 00:00:00', '2026-12-31 23:59:59', 'NOT_STARTED', '2026-08-01 08:00:00'),
-- 考试4: Python入门 (已结束)
(4, 3, 'Python编程基础测验',
    '考查Python基础语法、数据结构、函数式编程和基本数据处理能力。',
    60, 100, 60, '2026-06-01 00:00:00', '2026-06-30 23:59:59', 'ENDED', '2026-05-15 08:00:00');


-- ============================================
-- 🔹 试题 (25道: 10单选 + 6多选 + 5判断 + 4简答)
-- ============================================

-- ==== 考试1: Java期末 (10道题: 4单选 + 2多选 + 2判断 + 2简答) ====
INSERT INTO `question` (`id`, `exam_id`, `question_type`, `content`, `options`, `answer`, `score`, `sort_order`) VALUES
-- 单选
(1, 1, 'SINGLE_CHOICE', 'Java中，以下哪个关键字用于实现继承？',
    '[{"key":"A","value":"implements"},{"key":"B","value":"extends"},{"key":"C","value":"inherit"},{"key":"D","value":"super"}]', 'B', 5, 1),
(2, 1, 'SINGLE_CHOICE', '以下哪个不是Java的基本数据类型？',
    '[{"key":"A","value":"int"},{"key":"B","value":"float"},{"key":"C","value":"String"},{"key":"D","value":"boolean"}]', 'C', 5, 2),
(3, 1, 'SINGLE_CHOICE', 'ArrayList的底层数据结构是什么？',
    '[{"key":"A","value":"链表"},{"key":"B","value":"数组"},{"key":"C","value":"哈希表"},{"key":"D","value":"二叉树"}]', 'B', 5, 3),
(4, 1, 'SINGLE_CHOICE', '以下哪个修饰符表示方法可以被同一包内的类访问？',
    '[{"key":"A","value":"private"},{"key":"B","value":"public"},{"key":"C","value":"protected"},{"key":"D","value":"默认（无修饰符）"}]', 'D', 5, 4),
-- 多选
(5, 1, 'MULTIPLE_CHOICE', '以下关于Java接口的说法，正确的有哪些？',
    '[{"key":"A","value":"接口中的方法默认是public abstract的"},{"key":"B","value":"一个类可以实现多个接口"},{"key":"C","value":"接口可以包含实例变量"},{"key":"D","value":"Java 8中接口可以有默认方法"}]', 'A,B,D', 10, 5),
(6, 1, 'MULTIPLE_CHOICE', '以下哪些是Java集合框架中的接口？',
    '[{"key":"A","value":"List"},{"key":"B","value":"Set"},{"key":"C","value":"Map"},{"key":"D","value":"ArrayList"}]', 'A,B,C', 10, 6),
-- 判断
(7, 1, 'TRUE_FALSE', 'Java中，一个子类可以同时继承多个父类。',
    '[{"key":"T","value":"正确"},{"key":"F","value":"错误"}]', 'F', 5, 7),
(8, 1, 'TRUE_FALSE', 'HashMap允许使用null作为键（key）。',
    '[{"key":"T","value":"正确"},{"key":"F","value":"错误"}]', 'T', 5, 8),
-- 简答
(9,  1, 'SHORT_ANSWER', '请简述Java中重载（Overload）和重写（Override）的区别。',
    NULL, '重载：同一个类中方法名相同但参数列表不同（个数/类型/顺序），与返回值类型无关，编译时多态。重写：子类重新定义父类的方法，方法签名完全相同，运行时多态，访问权限不能更严格。', 15, 9),
(10, 1, 'SHORT_ANSWER', '请解释Java异常处理中throw和throws的区别。',
    NULL, 'throw：在方法体内使用，用于手动抛出一个异常对象；throws：在方法声明处使用，用于声明该方法可能抛出的异常类型，将异常交给上层调用者处理。', 15, 10);

-- ==== 考试2: 数据结构期中 (8道题: 3单选 + 2多选 + 2判断 + 1简答) ====
INSERT INTO `question` (`id`, `exam_id`, `question_type`, `content`, `options`, `answer`, `score`, `sort_order`) VALUES
(11, 2, 'SINGLE_CHOICE', '栈的操作特性是什么？',
    '[{"key":"A","value":"先进先出(FIFO)"},{"key":"B","value":"先进后出(LIFO)"},{"key":"C","value":"随机访问"},{"key":"D","value":"按键值访问"}]', 'B', 5, 1),
(12, 2, 'SINGLE_CHOICE', '具有n个节点的完全二叉树的深度是多少？（根节点深度为1）',
    '[{"key":"A","value":"n"},{"key":"B","value":"log₂n"},{"key":"C","value":"⌊log₂n⌋+1"},{"key":"D","value":"2^n"}]', 'C', 5, 2),
(13, 2, 'SINGLE_CHOICE', '以下哪种排序算法在最好情况下的时间复杂度是O(n)？',
    '[{"key":"A","value":"快速排序"},{"key":"B","value":"冒泡排序（优化版）"},{"key":"C","value":"选择排序"},{"key":"D","value":"归并排序"}]', 'B', 5, 3),
-- 多选
(14, 2, 'MULTIPLE_CHOICE', '以下哪些属于线性数据结构？',
    '[{"key":"A","value":"数组"},{"key":"B","value":"链表"},{"key":"C","value":"树"},{"key":"D","value":"栈"}]', 'A,B,D', 10, 4),
(15, 2, 'MULTIPLE_CHOICE', '关于二叉搜索树（BST），以下说法正确的有哪些？',
    '[{"key":"A","value":"左子树所有节点值小于根节点值"},{"key":"B","value":"右子树所有节点值大于根节点值"},{"key":"C","value":"BST的中序遍历结果是有序的"},{"key":"D","value":"BST一定是一棵平衡树"}]', 'A,B,C', 10, 5),
-- 判断
(16, 2, 'TRUE_FALSE', '队列是一种先进先出（FIFO）的数据结构。',
    '[{"key":"T","value":"正确"},{"key":"F","value":"错误"}]', 'T', 5, 6),
(17, 2, 'TRUE_FALSE', '在单链表中删除某个节点时，已知该节点的指针，删除操作的时间复杂度是O(1)。',
    '[{"key":"T","value":"正确"},{"key":"F","value":"错误"}]', 'F', 5, 7),
-- 简答
(18, 2, 'SHORT_ANSWER', '请简述深度优先搜索（DFS）和广度优先搜索（BFS）的核心思想，并说明各自适用的场景。',
    NULL, 'DFS：从起点出发，沿一条路径尽可能深入，回溯后再探索其他分支，通常用递归或栈实现。适合路径查找、拓扑排序、连通性检测。BFS：从起点出发，逐层向外扩展访问，使用队列实现。适合最短路径、层序遍历。DFS空间复杂度O(h)，BFS空间复杂度O(w)，其中h为深度、w为宽度。', 20, 8);

-- ==== 考试3: Web前端基础 (4道题: 2单选 + 1多选 + 1简答) ====
INSERT INTO `question` (`id`, `exam_id`, `question_type`, `content`, `options`, `answer`, `score`, `sort_order`) VALUES
(19, 3, 'SINGLE_CHOICE', 'CSS中，以下哪个属性用于设置元素的外边距？',
    '[{"key":"A","value":"padding"},{"key":"B","value":"margin"},{"key":"C","value":"border"},{"key":"D","value":"spacing"}]', 'B', 5, 1),
(20, 3, 'SINGLE_CHOICE', 'JavaScript中，以下哪个方法可以将JSON字符串转换为JavaScript对象？',
    '[{"key":"A","value":"JSON.stringify()"},{"key":"B","value":"JSON.parse()"},{"key":"C","value":"JSON.convert()"},{"key":"D","value":"JSON.toObject()"}]', 'B', 5, 2),
(21, 3, 'MULTIPLE_CHOICE', '以下哪些是HTML5新增的语义化标签？',
    '[{"key":"A","value":"<header>"},{"key":"B","value":"<section>"},{"key":"C","value":"<div>"},{"key":"D","value":"<nav>"}]', 'A,B,D', 10, 3),
(22, 3, 'SHORT_ANSWER', '请简述Vue3中组合式API（Composition API）相比选项式API（Options API）的优势。',
    NULL, '1）更好的逻辑复用：通过组合函数将逻辑提取复用；2）更灵活的代码组织：相关逻辑可以放在一起而非分散在不同选项中；3）更好的TypeScript支持；4）更小的打包体积。', 20, 4);

-- ==== 考试4: Python入门 (3道题: 1单选 + 1多选 + 1判断) ====
INSERT INTO `question` (`id`, `exam_id`, `question_type`, `content`, `options`, `answer`, `score`, `sort_order`) VALUES
(23, 4, 'SINGLE_CHOICE', 'Python中，以下哪个数据类型是不可变的（immutable）？',
    '[{"key":"A","value":"列表(list)"},{"key":"B","value":"字典(dict)"},{"key":"C","value":"元组(tuple)"},{"key":"D","value":"集合(set)"}]', 'C', 5, 1),
(24, 4, 'MULTIPLE_CHOICE', '以下哪些是Python中合法的变量命名？',
    '[{"key":"A","value":"_name"},{"key":"B","value":"2value"},{"key":"C","value":"my_var"},{"key":"D","value":"user-name"}]', 'A,C', 10, 2),
(25, 4, 'TRUE_FALSE', 'Python使用缩进来定义代码块，通常建议使用4个空格作为一个缩进层级。',
    '[{"key":"T","value":"正确"},{"key":"F","value":"错误"}]', 'T', 5, 3);


-- ============================================
-- 🔹 考试记录 + 答题记录
-- ============================================

-- 考试1(Java期末): 吴梅(9) 已提交 — 90分
INSERT INTO `exam_record` (`id`, `exam_id`, `user_id`, `score`, `status`, `start_time`, `submit_time`) VALUES
(1, 1, 9, 85, 'GRADED', '2026-05-01 10:00:00', '2026-05-01 11:20:00');
INSERT INTO `exam_question_record` (`id`, `exam_record_id`, `question_id`, `user_answer`, `is_correct`, `score`, `question_type`) VALUES
(1,  1, 1,  'B', 1, 5,  'SINGLE_CHOICE'),
(2,  1, 2,  'C', 1, 5,  'SINGLE_CHOICE'),
(3,  1, 3,  'B', 1, 5,  'SINGLE_CHOICE'),
(4,  1, 4,  'C', 0, 0,  'SINGLE_CHOICE'),  -- 错了, 应该是D(默认)
(5,  1, 5,  'A,B,D', 1, 10, 'MULTIPLE_CHOICE'),
(6,  1, 6,  'A,B,C', 1, 10, 'MULTIPLE_CHOICE'),
(7,  1, 7,  'F', 1, 5,  'TRUE_FALSE'),
(8,  1, 8,  'T', 1, 5,  'TRUE_FALSE'),
(9,  1, 9,  '重载是同一个类中方法名相同参数不同，重写是子类重新定义父类方法，方法签名相同。', NULL, 13, 'SHORT_ANSWER'),
(10, 1, 10, 'throw用于方法体内抛出异常，throws用于方法声明处声明可能抛出的异常。', NULL, 12, 'SHORT_ANSWER');

-- 考试1(Java期末): 刘阳(6) 已提交 — 75分
INSERT INTO `exam_record` (`id`, `exam_id`, `user_id`, `score`, `status`, `start_time`, `submit_time`) VALUES
(2, 1, 6, 70, 'GRADED', '2026-05-01 14:00:00', '2026-05-01 15:15:00');
INSERT INTO `exam_question_record` (`id`, `exam_record_id`, `question_id`, `user_answer`, `is_correct`, `score`, `question_type`) VALUES
(11, 2, 1,  'B', 1, 5,  'SINGLE_CHOICE'),
(12, 2, 2,  'C', 1, 5,  'SINGLE_CHOICE'),
(13, 2, 3,  'A', 0, 0,  'SINGLE_CHOICE'),  -- 错了
(14, 2, 4,  'D', 1, 5,  'SINGLE_CHOICE'),
(15, 2, 5,  'A,B', 0, 5,  'MULTIPLE_CHOICE'),  -- 漏选D
(16, 2, 6,  'A,B,C', 1, 10, 'MULTIPLE_CHOICE'),
(17, 2, 7,  'F', 1, 5,  'TRUE_FALSE'),
(18, 2, 8,  'F', 0, 0,  'TRUE_FALSE'),  -- 错了, HashMap允许null key
(19, 2, 9,  '重载参数不同，重写方法相同。', NULL, 8,  'SHORT_ANSWER'),
(20, 2, 10, 'throw是抛出异常，throws是声明异常。', NULL, 10, 'SHORT_ANSWER');

-- 考试4(Python): 孙悦(7) 已提交 — 95分
INSERT INTO `exam_record` (`id`, `exam_id`, `user_id`, `score`, `status`, `start_time`, `submit_time`) VALUES
(3, 4, 7, 20, 'SUBMITTED', '2026-06-10 09:00:00', '2026-06-10 09:40:00');
INSERT INTO `exam_question_record` (`id`, `exam_record_id`, `question_id`, `user_answer`, `is_correct`, `score`, `question_type`) VALUES
(21, 3, 23, 'C', 1, 5,  'SINGLE_CHOICE'),
(22, 3, 24, 'A,C', 1, 10, 'MULTIPLE_CHOICE'),
(23, 3, 25, 'T', 1, 5,  'TRUE_FALSE');


-- ============================================
-- 🔹 讨论帖 (10条, 分布在3门课程)
-- ============================================
INSERT INTO `discussion_post` (`id`, `course_id`, `user_id`, `title`, `content`, `create_time`) VALUES
-- 课程1: Java程序设计
(1, 1, 6, '关于static关键字的疑问',
    '老师好，我在学习static关键字的时候有些困惑。静态变量和实例变量的区别我理解了，但是静态代码块和构造代码块的执行顺序是怎样的？能不能给一个具体的例子说明一下？谢谢！', '2026-03-15 10:00:00'),
(2, 1, 7, '分享一个好用的Java学习资源',
    '推荐一个非常好用的在线Java练习平台——LeetCode的Java专题，从简单题开始循序渐进，对理解数据结构和算法帮助很大。大家有在用吗？', '2026-03-18 14:00:00'),
(3, 1, 9, '面向对象中多态的困惑',
    '我在理解多态的时候遇到一个问题：为什么父类引用指向子类对象时，只能调用父类中定义的方法，而不能直接调用子类特有的方法？这样做的好处是什么？', '2026-03-22 16:00:00'),
(4, 1, 12, '作业2中关于封装的问题',
    '在Student类设计作业中，所有属性都是private的，但是我的getter/setter都是public的，这样岂不是相当于没有封装？封装的核心意义到底是什么？', '2026-04-05 09:00:00'),

-- 课程2: 数据结构与算法
(5, 2, 6, '递归实现链表反转的思路',
    '我在用递归实现单链表反转的时候卡住了，迭代法还好理解，但是递归的栈回溯过程让我很困惑。有没有同学能分享一下递归的思路？', '2026-03-25 11:00:00'),
(6, 2, 10, '快速排序的优化方案',
    '我在做排序算法比较的作业，发现快速排序在基本有序的数组上性能很差。查了一些资料，随机选择pivot和三者取中法可以缓解这个问题。大家还有没有其他优化思路？', '2026-04-10 15:00:00'),
(7, 2, 8, 'BFS和DFS的典型应用场景总结',
    '我整理了一些BFS和DFS的经典应用场景，分享给大家：BFS适用于最短路径、层次遍历、最小步数问题；DFS适用于全排列、子集、连通性、回溯问题。欢迎补充！', '2026-04-20 08:00:00'),

-- 课程4: Web前端开发实战
(8, 4, 7, 'CSS Grid vs Flexbox 使用场景?',
    '老师上课提到了Grid和Flexbox两种布局方式，但我不太清楚什么情况下该用Grid，什么情况下该用Flexbox。大家有什么建议吗？', '2026-04-05 10:00:00'),
(9, 4, 10, 'Vue3组合式API中ref和reactive的选择',
    '我在学习Vue3组合式API时，发现ref和reactive都可以创建响应式数据。官方文档说ref适合基本类型，reactive适合对象。但实际开发中ref对对象也有效。到底该怎么选？', '2026-05-01 09:00:00'),
(10, 4, 13, '个人主页设计作业展示与互评',
    '我的个人主页做好了，用了深色主题和玻璃拟态效果，欢迎大家来看看给点建议！互相学习进步~', '2026-04-15 14:00:00');


-- ============================================
-- 🔹 讨论回复 (20条)
-- ============================================
INSERT INTO `discussion_reply` (`id`, `post_id`, `user_id`, `parent_reply_id`, `content`, `create_time`) VALUES
-- 帖子1的回复
(1,  1, 2,  NULL, '静态代码块在类加载时执行且只执行一次，构造代码块在每次创建对象时执行，且先于构造方法执行。执行顺序：静态代码块 → 构造代码块 → 构造方法。具体例子我整理了一个文档发在课件里。', '2026-03-15 14:00:00'),
(2,  1, 6,  1,    '谢谢老师！我理解了，所以静态代码块适合做一次性的初始化工作，比如加载数据库驱动，对吗？', '2026-03-15 15:00:00'),
(3,  1, 2,  2,    '没错，这是一个典型的应用场景。另外静态代码块也常用于初始化静态成员变量。', '2026-03-15 16:00:00'),
-- 帖子2的回复
(4,  2, 9,  NULL, '谢谢推荐！我最近在用，确实不错。配合《Java核心技术》这本书效果更好。', '2026-03-18 16:00:00'),
(5,  2, 12, NULL, '同推荐！而且LeetCode的评论区经常有大神分享很巧妙的解法，能学到很多课本上没有的技巧。', '2026-03-19 10:00:00'),
-- 帖子3的回复
(6,  3, 2,  NULL, '好问题！这样做的好处是实现了"面向接口编程"——调用者只需要知道父类定义的接口，不需要关心具体的子类实现。如果需要调用子类特有方法，可以通过强制类型转换（向下转型），但要先用instanceof判断。', '2026-03-22 17:00:00'),
(7,  3, 7,  NULL, '补充一下老师说的：多态最大的好处是提高了代码的可扩展性。比如Collections.sort()方法可以排序任何实现了Comparable接口的类的对象列表，而不用为每种类型写不同的排序方法。', '2026-03-23 09:00:00'),
-- 帖子5的回复
(8,  5, 3,  NULL, '递归反转链表的思路：把链表看成 head → 剩余部分。递归反转剩余部分后，把head的next节点的next指向head，再把head的next置为null。关键是要理解递归的返回值始终是新链表的头节点。', '2026-03-25 15:00:00'),
(9,  5, 10, NULL, '我画了个图来理解这个过程，确实比迭代法简洁很多。递归版本只需要几行代码，核心就是head.next.next = head; head.next = null;', '2026-03-26 10:00:00'),
-- 帖子6的回复
(10, 6, 8,  NULL, '除了随机pivot和三者取中，还可以在子数组长度小于某个阈值时切换到插入排序，因为小数组上插入排序的常数因子更小。这个方法在Java的Arrays.sort()中就有应用（Dual-Pivot Quicksort + Insertion Sort混合）。', '2026-04-10 17:00:00'),
(11, 6, 3,  NULL, '说得很对！这就是TimSort的思路来源——结合归并排序和插入排序的优点。做作业的时候可以尝试实现一下混合排序，加分项。', '2026-04-11 08:00:00'),
-- 帖子7的回复
(12, 7, 6,  NULL, '总结得很全面！我再加一个：BFS还可以用来解决无权图的最短路径问题（比如迷宫问题），DFS可以用来检测图中是否有环。', '2026-04-20 10:00:00'),
-- 帖子8的回复
(13, 8, 5,  NULL, '简单来说：Flexbox适合一维布局（要么行要么列），比如导航栏、卡片列表；Grid适合二维布局（同时控制行和列），比如整体页面布局、图片画廊。当然两者可以嵌套使用，取长补短。', '2026-04-05 14:00:00'),
(14, 8, 13, NULL, '我在做作业的时候用的就是Grid做整体布局（header/main/sidebar/footer），然后在main内部用Flexbox排列卡片，效果很不错。', '2026-04-05 16:00:00'),
-- 帖子9的回复
(15, 9, 5,  NULL, '推荐统一使用ref，因为ref对基本类型和对象都支持，而且可以通过.value明确区分原始值和响应式引用。reactive有一些限制——不能替换整个对象、解构会丢失响应性。实际项目中ref的使用率远高于reactive。', '2026-05-01 12:00:00'),
(16, 9, 7,  NULL, '老师说得很清楚！我补充一个场景：如果要管理一组相关联的状态（比如表单数据），用reactive包裹一个对象会比较方便。但单个值或者需要在组件间传递的值，还是ref更好。', '2026-05-02 09:00:00'),
-- 帖子10的回复
(17, 10, 8,  NULL, '做得太漂亮了！玻璃拟态效果是怎么实现的？求分享CSS代码~', '2026-04-15 16:00:00'),
(18, 10, 13, 17,   '主要是用backdrop-filter: blur(10px)配合半透明背景，再加个border-radius和微弱的border就出效果了。代码我贴在下面：background: rgba(255,255,255,0.2); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.3); border-radius: 16px;', '2026-04-15 18:00:00'),
(19, 10, 10, NULL, '深色主题真的很惊艳！提个小建议，文字对比度可以再高一点，白色文字在浅色玻璃背景上不太容易阅读。', '2026-04-16 10:00:00'),
(20, 10, 13, 19,   '谢谢建议！我调整一下，把文字颜色改成#f0f0f0，确实好了很多。', '2026-04-16 12:00:00');


-- ============================================
-- 🔹 公告 (6条: 3条系统公告 + 3条课程公告)
-- ============================================
INSERT INTO `announcement` (`id`, `title`, `content`, `type`, `course_id`, `publisher_id`, `create_time`) VALUES
-- 系统公告 (publisher_id=1 admin)
(1, '欢迎使用智慧校园在线学习平台！',
    '各位老师和同学，智慧校园在线学习平台已正式上线运行。平台提供课程管理、在线学习、作业提交、在线考试、讨论交流等功能。如遇任何问题，请联系系统管理员（admin@smartcampus.edu.cn）。祝大家学习愉快！',
    'SYSTEM', NULL, 1, '2026-03-01 08:00:00'),
(2, '关于平台系统升级维护的通知',
    '各位用户，平台计划于2026年7月15日（周三）凌晨2:00-6:00进行系统升级维护，届时平台将暂时无法访问。请各位老师和同学提前安排好学习和教学计划。升级后平台将新增数据看板功能和移动端适配优化。给您带来的不便敬请谅解。',
    'SYSTEM', NULL, 1, '2026-07-01 09:00:00'),
(3, '2026年暑期学习活动通知',
    '为鼓励同学们充分利用暑期时间提升自我，平台将从7月20日至8月31日开展"暑期学习挑战赛"活动。活动期间完成任意一门课程学习且通过期末测验的同学，将获得学习证书和平台积分奖励。详情请关注后续通知。',
    'SYSTEM', NULL, 1, '2026-07-05 10:00:00'),

-- 课程公告
(4, 'Java课程第三章更新通知',
    '各位选修Java程序设计入门课程的同学们，第三章"面向对象高级特性"的内容已经全部更新上线，包括继承与super关键字、多态与抽象类、接口与默认方法三个小节的视频和课件。请大家及时完成学习，第四章的作业将于本周五发布。',
    'COURSE', 1, 2, '2026-03-20 08:00:00'),
(5, '数据结构期中测验通知',
    '各位同学，数据结构与算法精讲课程的期中测验已经发布，考试时间为60分钟，满分100分，及格线60分。考试窗口为7月1日至8月31日，请大家在方便的时间完成测验。测验内容覆盖线性结构和树结构章节。',
    'COURSE', 2, 3, '2026-06-28 10:00:00'),
(6, 'Web前端课程项目实战说明',
    '各位选修Web前端开发实战的同学们，课程项目实战环节的具体要求已经发布在第三章作业中。项目要求使用Vue3开发一个Todo应用，具体要求见作业详情。优秀项目作品将有机会在平台首页展示！',
    'COURSE', 4, 5, '2026-04-20 09:00:00');


-- ============================================
-- 🔹 公告已读记录
-- ============================================
INSERT INTO `announcement_read` (`id`, `announcement_id`, `user_id`, `read_time`) VALUES
(1,  1, 6,  '2026-03-01 10:00:00'),
(2,  1, 7,  '2026-03-01 11:00:00'),
(3,  1, 9,  '2026-03-02 09:00:00'),
(4,  2, 6,  '2026-07-01 14:00:00'),
(5,  2, 7,  '2026-07-02 10:00:00'),
(6,  4, 6,  '2026-03-20 09:00:00'),
(7,  4, 7,  '2026-03-20 10:00:00'),
(8,  4, 9,  '2026-03-20 11:00:00'),
(9,  4, 12, '2026-03-21 08:00:00'),
(10, 5, 6,  '2026-06-28 14:00:00'),
(11, 5, 10, '2026-06-29 09:00:00'),
(12, 6, 7,  '2026-04-20 10:00:00'),
(13, 6, 10, '2026-04-21 11:00:00');


-- ============================================
-- 🔹 系统日志
-- ============================================
INSERT INTO `system_log` (`id`, `user_id`, `module`, `operation`, `params`, `ip`, `create_time`) VALUES
(1, 1, '用户管理', '登录系统', '{"username":"admin"}', '192.168.1.100', '2026-07-01 08:00:00'),
(2, 1, '用户管理', '创建用户', '{"username":"zhangwei","role":"TEACHER"}', '192.168.1.100', '2026-01-15 08:00:00'),
(3, 2, '课程管理', '发布课程', '{"courseId":1,"title":"Java程序设计入门"}', '192.168.1.101', '2026-03-01 08:30:00'),
(4, 6, '选课管理', '加入课程', '{"courseId":1}', '192.168.1.200', '2026-03-10 08:00:00'),
(5, 7, '选课管理', '加入课程', '{"courseId":1}', '192.168.1.201', '2026-03-11 08:30:00'),
(6, 9, '作业管理', '提交作业', '{"assignmentId":1}', '192.168.1.203', '2026-04-05 10:00:00'),
(7, 1, '系统管理', '系统升级', '{"version":"2.0.0"}', '192.168.1.100', '2026-07-01 02:00:00'),
(8, 7, '考试管理', '开始考试', '{"examId":4}', '192.168.1.201', '2026-06-10 09:00:00');


-- ============================================
-- ⚠️ 注意事项
-- ============================================
-- 1. 以上 BCrypt 密码哈希为占位符，请在 Java 中执行以下代码生成正确的哈希值：
--    new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("123456")
--    将生成的哈希值替换所有用户 password 字段中的占位符
-- 2. 视频和文档的 content_url 为示例路径，实际使用时需替换为真实的文件地址
-- 3. 考试时间根据 current_date 设置，当前默认时间为 2026 年
-- 4. AUTO_INCREMENT 从指定 id 开始，确保后续新增数据不会与示例数据冲突
-- ============================================
