-- ============================================================================
-- 图书管理系统 · 数据库建表脚本
-- ============================================================================
-- 版本  : v2.0
-- 数据库: MySQL 8.0+
-- 用法  : mysql -uroot -p < schema.sql     （可重复执行，每次重建全部表）
-- 配套  : data.sql（种子数据，必须在本脚本之后执行）
--
-- 表清单（17 张）
--   组织架构  t_department → t_major → t_class
--   用户      t_student（外键→t_class）、t_teacher（外键→t_major）、t_admin
--   馆藏      t_category → t_book → t_book_copy
--   流通      t_borrow_config、t_borrow_record（外键→t_book_copy）、t_fine（外键→t_borrow_record）
--   门户消息  t_banner、t_comment（外键→t_book）、t_recommend（外键→t_book）、
--             t_notification、t_holiday_config
--   共 10 个物理外键；t_borrow_record.user_id 不建外键（原因见该列注释）
--
-- 变更记录
--   v2.0 2026-11  整理为单一建表脚本，合并以下一次性脚本后删除原文件：
--        · optimize-indexes.sql     → 冗余索引收敛，改为 5 个组合索引（见各表 KEY）
--        · update-comments.sql      → 字段注释并入列定义（供 AI Text-to-SQL 读取）
--        · fix-teacher-borrow.sql   → t_borrow_record.user_id 不建外键（v1 基线已如此，
--                                      本版补注释说明「教师也可借阅，由应用层校验」）
--        · update-borrow-status.sql → t_borrow_record.status 增加 4=待审核 语义
--        · semester-analysis.sql    → 纯查询脚本，非建表/种子内容，移出脚本目录
--        另修两处隐患：
--        ① 补 SET SESSION FOREIGN_KEY_CHECKS=0：原脚本在表已存在时二次执行会失败
--           （DROP TABLE t_book_copy 早于引用它的 t_borrow_record，报 ERROR 3730）
--        ② 统一排序规则：t_banner / t_notification / t_recommend / t_teacher 原为
--           utf8mb4_0900_ai_ci，与其余表 utf8mb4_unicode_ci 不一致，跨表比较会报
--           Illegal mix of collations (1267)
--        索引收敛明细（删除的 7 个冗余索引，左前缀均已被保留索引覆盖）：
--           t_book.idx_book_isbn（与 UNIQUE isbn 重复）
--           t_book.idx_book_category → idx_book_category_status
--           t_book_copy.idx_copy_book + idx_copy_status → idx_copy_book_status
--           t_borrow_record.idx_borrow_user → idx_borrow_user_status
--           t_student.idx_user_user_id（与 UNIQUE user_id 重复）
--           t_student.idx_user_class → idx_student_class_name
--           t_fine.idx_fine_record → idx_fine_record_status
--   v1.0          初始表结构（mysqldump 导出）
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `book_management`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `book_management`;

SET NAMES utf8mb4;
SET SESSION FOREIGN_KEY_CHECKS = 0;

-- ─── 组织架构：学院 / 专业 / 班级 ───

DROP TABLE IF EXISTS `t_department`;
CREATE TABLE `t_department` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '学院名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学院';

DROP TABLE IF EXISTS `t_major`;
CREATE TABLE `t_major` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '专业名称',
  `department_id` int unsigned NOT NULL COMMENT '所属学院ID，外键关联 t_department.id',
  PRIMARY KEY (`id`),
  KEY `department_id` (`department_id`),
  CONSTRAINT `t_major_ibfk_1` FOREIGN KEY (`department_id`) REFERENCES `t_department` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业';

DROP TABLE IF EXISTS `t_class`;
CREATE TABLE `t_class` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '班级名称',
  `major_id` int unsigned NOT NULL COMMENT '所属专业ID，外键链：t_department → t_major → t_class → t_student',
  PRIMARY KEY (`id`),
  KEY `major_id` (`major_id`),
  CONSTRAINT `t_class_ibfk_1` FOREIGN KEY (`major_id`) REFERENCES `t_major` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级';

-- ─── 用户：学生 / 教师 / 管理员 ───

DROP TABLE IF EXISTS `t_student`;
CREATE TABLE `t_student` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_id` varchar(20) NOT NULL COMMENT '学号（唯一登录凭证）',
  `password` varchar(255) NOT NULL DEFAULT '' COMMENT '密码哈希（BCrypt）',
  `name` varchar(50) NOT NULL COMMENT '真实姓名（显示用）',
  `education_level` tinyint DEFAULT NULL COMMENT '学历 1=高职 2=本科 3=硕士 4=博士，影响借阅限额',
  `class_id` int unsigned DEFAULT NULL COMMENT '班级ID，外键关联 t_class.id',
  `phone` varchar(20) NOT NULL COMMENT '手机号（登录凭证）',
  `status` tinyint DEFAULT '1' COMMENT '用户状态 1=正常(可借书) 2=超期锁定(不可借书) 3=禁用(已被管理员禁用)',
  `deleted` tinyint DEFAULT '0' COMMENT '软删除标记，查询必须加 AND deleted=0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_user_phone` (`phone`),
  KEY `idx_student_class_name` (`class_id`,`name`),
  CONSTRAINT `t_student_ibfk_1` FOREIGN KEY (`class_id`) REFERENCES `t_class` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生';

DROP TABLE IF EXISTS `t_teacher`;
CREATE TABLE `t_teacher` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_id` varchar(20) NOT NULL COMMENT '工号（唯一登录凭证）',
  `password` varchar(255) NOT NULL DEFAULT '' COMMENT '密码哈希（BCrypt）',
  `name` varchar(50) NOT NULL COMMENT '真实姓名（显示用）',
  `major_id` int unsigned DEFAULT NULL COMMENT '所属专业ID，外键关联 t_major.id',
  `phone` varchar(20) NOT NULL COMMENT '手机号（登录凭证）',
  `status` tinyint DEFAULT '1' COMMENT '用户状态 1=正常(可借书) 2=超期锁定(不可借书) 3=禁用',
  `deleted` tinyint DEFAULT '0' COMMENT '软删除标记，查询必须加 AND deleted=0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `major_id` (`major_id`),
  CONSTRAINT `t_teacher_ibfk_1` FOREIGN KEY (`major_id`) REFERENCES `t_major` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师';

DROP TABLE IF EXISTS `t_admin`;
CREATE TABLE `t_admin` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `password` varchar(255) NOT NULL COMMENT '密码哈希（BCrypt）',
  `name` varchar(50) NOT NULL COMMENT '管理员姓名',
  `phone` varchar(20) DEFAULT '' COMMENT '手机号（登录凭证）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员';

-- ─── 馆藏：分类 / 图书 / 副本 ───

DROP TABLE IF EXISTS `t_category`;
CREATE TABLE `t_category` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `status` tinyint DEFAULT '1' COMMENT '分类状态 1=启用(下拉框可见) 0=禁用(下拉框不显示)',
  `deleted` tinyint DEFAULT '0' COMMENT '软删除标记',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书分类';

DROP TABLE IF EXISTS `t_book`;
CREATE TABLE `t_book` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `isbn` varchar(20) NOT NULL COMMENT 'ISBN 编号',
  `title` varchar(200) NOT NULL COMMENT '书名',
  `author` varchar(100) NOT NULL COMMENT '作者',
  `publisher` varchar(100) NOT NULL COMMENT '出版社',
  `publish_year` varchar(4) DEFAULT NULL COMMENT '出版年份',
  `category_id` int unsigned DEFAULT NULL COMMENT '所属分类ID，外键关联 t_category.id',
  `description` text COMMENT '内容简介',
  `cover_url` varchar(255) DEFAULT '' COMMENT '封面图片 URL',
  `status` tinyint DEFAULT '1' COMMENT '图书状态 1=上架(已发布) 0=下架；注意：这不代表副本能否借阅，可借阅要看 t_book_copy.status',
  `deleted` tinyint DEFAULT '0' COMMENT '软删除标记 0=未删 1=已删，所有查询必须加 AND deleted=0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间，距当前 30 天内视为新书（受 max_new_books 限制）',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `isbn` (`isbn`),
  KEY `idx_book_category_status` (`category_id`,`status`),
  CONSTRAINT `t_book_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `t_category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书';

DROP TABLE IF EXISTS `t_book_copy`;
CREATE TABLE `t_book_copy` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `book_id` int unsigned NOT NULL COMMENT '所属图书ID，外键关联 t_book.id',
  `barcode` varchar(50) NOT NULL COMMENT '条码号，每本实体书唯一标识',
  `shelf_floor` varchar(2) DEFAULT '' COMMENT '楼层（如 1F 2F 3F）',
  `shelf_zone` varchar(10) DEFAULT '' COMMENT '区域（如 A区 B区 C区）',
  `shelf_no` varchar(10) DEFAULT '' COMMENT '架号（如 01架 12架）',
  `status` tinyint DEFAULT '1' COMMENT '副本当前状态 1=在架(可借) 2=借出(不可借) 3=待上架(管理员确认中) 4=丢失(已标记丢失)',
  `deleted` tinyint DEFAULT '0' COMMENT '软删除标记，查询必须加 AND deleted=0，丢失的副本用 status=4 而非删除',
  `purchase_date` date DEFAULT NULL COMMENT '采购日期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `barcode` (`barcode`),
  KEY `idx_copy_book_status` (`book_id`,`status`),
  CONSTRAINT `t_book_copy_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `t_book` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=130 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书副本';

-- ─── 流通：借阅规则 / 借阅记录 / 滞纳金 ───

DROP TABLE IF EXISTS `t_borrow_config`;
CREATE TABLE `t_borrow_config` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `role` tinyint NOT NULL COMMENT '适用角色 1=学生 2=教师，注意：学生按学历细分规则，教师按统一规则',
  `education_level` tinyint DEFAULT NULL COMMENT '学历 1=高职 2=本科 3=硕士 4=博士，NULL 表示适用于该角色下所有学历的默认规则',
  `max_books` int NOT NULL DEFAULT '5' COMMENT '最大借阅册数，学生本科=7、硕士=12、博士=18、教师=20',
  `max_days` int NOT NULL DEFAULT '30' COMMENT '最大借阅天数',
  `fine_per_day` decimal(4,2) NOT NULL DEFAULT '0.10' COMMENT '超期滞纳金费率，元/天',
  `max_new_books` int DEFAULT '2' COMMENT '最大新书借阅数，新书定义为上架 30 天内的图书',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅规则';

DROP TABLE IF EXISTS `t_borrow_record`;
CREATE TABLE `t_borrow_record` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `borrow_no` varchar(30) NOT NULL COMMENT '借阅单号（唯一）',
  `user_id` int unsigned NOT NULL COMMENT '借阅人ID，逻辑关联 t_student.id 或 t_teacher.id，无外键约束（教师也可借阅，由应用层校验）',
  `copy_id` int unsigned NOT NULL COMMENT '借阅的副本ID，外键关联 t_book_copy.id',
  `borrow_date` date NOT NULL COMMENT '借书日期，用于计算借阅时长',
  `due_date` date NOT NULL COMMENT '应还日期，超过此日期未归还算超期',
  `return_date` date DEFAULT NULL COMMENT '实际归还日期，NULL 表示尚未归还',
  `status` tinyint DEFAULT '1' COMMENT '借阅状态 1=借阅中(当前在读) 2=已归还(已还书) 3=超期(逾期未还) 4=待审核(已申请还书等待管理员确认)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `borrow_no` (`borrow_no`),
  KEY `copy_id` (`copy_id`),
  KEY `idx_borrow_user_status` (`user_id`,`status`),
  KEY `idx_borrow_status` (`status`),
  KEY `idx_borrow_dates` (`borrow_date`,`due_date`),
  CONSTRAINT `t_borrow_record_ibfk_2` FOREIGN KEY (`copy_id`) REFERENCES `t_book_copy` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅记录';

DROP TABLE IF EXISTS `t_fine`;
CREATE TABLE `t_fine` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `record_id` int unsigned NOT NULL COMMENT '关联的借阅记录ID，外键关联 t_borrow_record.id',
  `overdue_days` int NOT NULL COMMENT '超期天数',
  `fine_amount` decimal(8,2) NOT NULL COMMENT '滞纳金金额',
  `status` tinyint DEFAULT '1' COMMENT '缴费状态 1=未支付(待缴费) 2=已支付 3=已免除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0' COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_fine_record_status` (`record_id`,`status`),
  CONSTRAINT `t_fine_ibfk_1` FOREIGN KEY (`record_id`) REFERENCES `t_borrow_record` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='滞纳金';

-- ─── 门户与消息：轮播图 / 评论 / 好书推荐 / 通知 / 假期配置 ───

DROP TABLE IF EXISTS `t_banner`;
CREATE TABLE `t_banner` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL COMMENT '轮播标题',
  `subtitle` varchar(200) DEFAULT '' COMMENT '副标题',
  `image_url` text COMMENT '轮播图 URL（MinIO 公开地址，不带预签名参数）',
  `sort_order` int DEFAULT '0' COMMENT '排序号，越小越靠前',
  `status` tinyint DEFAULT '1' COMMENT '轮播状态 1=启用(首页显示) 0=禁用(不显示)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0' COMMENT '软删除标记',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首页轮播图';

DROP TABLE IF EXISTS `t_comment`;
CREATE TABLE `t_comment` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `book_id` int unsigned NOT NULL COMMENT '被评论的图书ID，外键关联 t_book.id',
  `user_id` int unsigned NOT NULL COMMENT '评论用户ID，逻辑关联 t_student.id 或 t_teacher.id',
  `rating` tinyint NOT NULL DEFAULT '5' COMMENT '评分 1-5 星',
  `content` text NOT NULL COMMENT '评论内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `idx_comment_book` (`book_id`),
  CONSTRAINT `t_comment_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `t_book` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书评论';

DROP TABLE IF EXISTS `t_recommend`;
CREATE TABLE `t_recommend` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `book_id` int unsigned NOT NULL COMMENT '被推荐的图书ID，外键关联 t_book.id',
  `sort_order` int DEFAULT '0' COMMENT '排序号，越小越靠前',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0' COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  KEY `book_id` (`book_id`),
  CONSTRAINT `t_recommend_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `t_book` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首页好书推荐';

DROP TABLE IF EXISTS `t_notification`;
CREATE TABLE `t_notification` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int unsigned NOT NULL COMMENT '接收用户ID，逻辑关联 t_student.id 或 t_teacher.id',
  `type` tinyint NOT NULL COMMENT '通知类型 1=假期延期通知 2=超期催还通知 3=还书提醒通知',
  `title` varchar(100) NOT NULL COMMENT '通知标题',
  `content` text COMMENT '通知内容',
  `is_read` tinyint DEFAULT '0' COMMENT '已读状态 0=未读(新通知) 1=已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知';

DROP TABLE IF EXISTS `t_holiday_config`;
CREATE TABLE `t_holiday_config` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `type` tinyint NOT NULL COMMENT '假期类型 1=暑假 2=寒假，借阅到期日落在假期范围内自动延期',
  `start_date` date NOT NULL COMMENT '起始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `extension_days` int NOT NULL DEFAULT '0' COMMENT '延期天数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='假期延期设置';

SET SESSION FOREIGN_KEY_CHECKS = 1;
