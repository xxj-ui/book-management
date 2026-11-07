# 图书管理系统 API 文档

代码实际暴露 **18 个控制器 / 85 个端点**，本文档逐条对照 controller 源码核对（2026-11）。
接口有变动时请以 controller 与 Knife4j 为准，并同步本文件的端点数。

**Base URL** `http://localhost:8080/api`（`server.servlet.context-path=/api`）

**认证** `Authorization: Bearer <token>`（Sa-Token 双 token）。全局拦截器只放行 `/auth/login`、`/auth/sms-login`、`/auth/sms-code`、`/auth/refresh` 与文档路径，其余端点均需登录。

**响应** 统一 `Result<T>`：`{ code, message, data }`；分页为 `PageResult<T>`：`{ total, current, size, records }`。

## 目录

| 模块 | 前缀 | 端点数 |
|---|---|---|
| 认证 | `/auth` | 10 |
| 学生 | `/students` | 6 |
| 教师 | `/teachers` | 5 |
| 图书 | `/books` | 7 |
| 图书封面 | `/books` | 3 |
| 图书副本 | `/copies` | 5 |
| 图书分类 | `/categories` | 4 |
| 借阅 | `/borrows` | 9 |
| 滞纳金 | `/fines` | 2 |
| 借阅规则与假期 | `/configs` | 5 |
| 归还上架 | `/dashboard` | 4 |
| 统计报表 | `/reports` | 4 |
| 图书评论 | `/comments` | 3 |
| 首页轮播 | `/banners` | 5 |
| 好书推荐 | `/recommends` | 4 |
| 站内通知 | `/notifications` | 4 |
| 组织架构字典 | `/dictionary` | 4 |
| 文件上传 | `/files` | 1 |
| **合计** | | **85** |

---

## 1. 认证（`/auth`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| POST | `/auth/login` | 密码登录（免 token） | body LoginRequest |
| POST | `/auth/sms-login` | 短信验证码登录（免 token） | body SmsLoginRequest |
| POST | `/auth/sms-code` | 发送短信验证码（免 token） | 查询 phone |
| POST | `/auth/refresh` | 用 refresh token 换新的 access token（免 token） | body Map<String,String> |
| POST | `/auth/verify-identity` | 改密前校验身份（手机号 + 原密码） | body PasswordChangeRequest |
| POST | `/auth/verify-sms-code` | 校验短信验证码 | body Map<String,String> |
| POST | `/auth/change-password` | 修改密码 | body PasswordChangeRequest |
| POST | `/auth/change-phone` | 换绑手机号 | body PhoneChangeRequest |
| GET | `/auth/user-info` | 当前登录用户信息 | — |
| POST | `/auth/logout` | 退出登录（注销 token） | — |

## 2. 学生（`/students`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| GET | `/students` | 分页查询 | 查询 page=1、size=10、keyword?、classId?、educationLevel? |
| GET | `/students/{id}` | 详情 | 路径 id |
| GET | `/students/userId/{userId}` | 按学号/工号查详情 | 路径 userId |
| POST | `/students` | 新增 | body UserRequest |
| PUT | `/students/{id}` | 更新 | 路径 id；body UserRequest |
| DELETE | `/students/{id}` | 删除 | 路径 id |

## 3. 教师（`/teachers`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| GET | `/teachers` | 分页查询 | 查询 page=1、size=10、keyword? |
| GET | `/teachers/{id}` | 详情 | 路径 id |
| POST | `/teachers` | 新增 | body UserRequest |
| PUT | `/teachers/{id}` | 更新 | 路径 id；body UserRequest |
| DELETE | `/teachers/{id}` | 删除 | 路径 id |

## 4. 图书（`/books`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| GET | `/books` | 分页查询 | 查询 page=1、size=10、keyword?、categoryId? |
| GET | `/books/isbn/{isbn}` | 按 ISBN 查图书详情 | 路径 isbn |
| POST | `/books` | 新增 | body BookRequest |
| PUT | `/books/{id}` | 更新 | 路径 id；body BookRequest |
| DELETE | `/books/{id}` | 删除 | 路径 id |
| GET | `/books/new` | 新书推荐（按入库时间 30 天内） | 查询 limit=6 |
| GET | `/books/hot` | 热门图书（按借阅次数） | 查询 limit=5 |

## 5. 图书封面（`/books`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| POST | `/books/{id}/fetch-cover` | 为单本图书抓取/生成封面 | 路径 id |
| POST | `/books/covers/fetch-all` | 批量抓取缺失封面 | — |
| GET | `/books/covers/generate` | 批量生成封面（Java2D 绘制，浏览器可直接访问） | — |

## 6. 图书副本（`/copies`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| GET | `/copies/book/{bookId}` | listByBook | 路径 bookId |
| POST | `/copies` | 新增 | body CopyRequest |
| POST | `/copies/batch` | 批量新增副本 | body List<CopyRequest> |
| PUT | `/copies/{id}` | 更新 | 路径 id；body CopyRequest |
| DELETE | `/copies/{id}` | 删除 | 路径 id |

## 7. 图书分类（`/categories`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| GET | `/categories` | 列表 | — |
| POST | `/categories` | 新增 | body CategoryRequest |
| PUT | `/categories/{id}` | 更新 | 路径 id；body CategoryRequest |
| DELETE | `/categories/{id}` | 删除 | 路径 id |

## 8. 借阅（`/borrows`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| GET | `/borrows` | 分页查询 | 查询 page=1、size=10、keyword?、status?、startDate?、endDate? |
| GET | `/borrows/user/{userId}` | 按用户查借阅记录 | 路径 userId |
| GET | `/borrows/overdue` | 超期列表 | — |
| GET | `/borrows/{id}` | 详情 | 路径 id |
| POST | `/borrows` | 借书 | body BorrowRequest |
| PUT | `/borrows/{id}/return` | 还书（副本转待上架） | 路径 id |
| PUT | `/borrows/{id}/renew` | 续借 | 路径 id |
| PUT | `/borrows/extend` | 按假期延期 | body ExtendRequest |
| POST | `/borrows/batch-remind` | 批量催还（写站内通知） | body List<Long> |

## 9. 滞纳金（`/fines`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| GET | `/fines` | 列表 | — |
| PUT | `/fines/{id}/pay` | 缴纳滞纳金 | 路径 id |

## 10. 借阅规则与假期（`/configs`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| POST | `/configs/holiday/batch-extend` | 假期批量延期（按假期区间延期所有在借记录） | — |
| GET | `/configs/borrow` | 获取借阅规则（全部角色/学历档） | — |
| PUT | `/configs/borrow` | 批量更新借阅规则 | body List<BorrowConfig> |
| GET | `/configs/holiday` | 获取假期配置 | — |
| PUT | `/configs/holiday` | 更新假期配置 | body HolidayConfig |

## 11. 归还上架（`/dashboard`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| GET | `/dashboard/returns` | 待上架的归还记录 | — |
| PUT | `/dashboard/returns/{id}/shelf` | 确认上架 | 路径 id |
| PUT | `/dashboard/returns/{id}/lost` | 标记副本丢失 | 路径 id |
| POST | `/dashboard/returns/batch-shelf` | 批量确认上架 | body List<Long> |

## 12. 统计报表（`/reports`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| GET | `/reports/overview` | 概览统计 | 查询 startDate?、endDate?、departmentId?、majorId?、className? |
| GET | `/reports/monthly-trend` | 月度借阅趋势 | 查询 startDate?、endDate?、departmentId?、majorId?、className? |
| GET | `/reports/category-distribution` | 分类分布 | 查询 startDate?、endDate?、departmentId?、majorId?、className? |
| GET | `/reports/export` | 导出报表（Excel 多 sheet） | 查询 startDate?、endDate?、departmentId?、majorId?、className? |

## 13. 图书评论（`/comments`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| GET | `/comments/book/{bookId}` | listByBook | 路径 bookId |
| POST | `/comments` | 新增 | body CommentRequest |
| DELETE | `/comments/{id}` | 删除 | 路径 id |

## 14. 首页轮播（`/banners`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| GET | `/banners` | 列表 | — |
| GET | `/banners/admin` | 管理端列表（含已禁用） | — |
| POST | `/banners` | 新增 | body Banner |
| PUT | `/banners/{id}` | 更新 | 路径 id；body Banner |
| DELETE | `/banners/{id}` | 删除 | 路径 id |

## 15. 好书推荐（`/recommends`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| GET | `/recommends` | 列表 | — |
| POST | `/recommends` | 新增 | body Recommend |
| DELETE | `/recommends/{id}` | 删除 | 路径 id |
| PUT | `/recommends/sort` | 批量调整推荐排序 | body List<Recommend> |

## 16. 站内通知（`/notifications`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| GET | `/notifications` | 列表 | — |
| GET | `/notifications/unread-count` | 未读通知数量 | — |
| PUT | `/notifications/{id}/read` | 标记单条已读 | 路径 id |
| PUT | `/notifications/read-all` | 全部标记已读 | — |

## 17. 组织架构字典（`/dictionary`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| GET | `/dictionary/departments` | 学院列表 | — |
| GET | `/dictionary/majors` | 专业列表（可按学院过滤） | 查询 departmentId? |
| GET | `/dictionary/classes` | 班级列表（可按专业过滤） | 查询 majorId? |
| GET | `/dictionary/all-classes` | 全部班级（含学院/专业，Redis 缓存） | — |

## 18. 文件上传（`/files`）

| 方法 | 路径 | 说明 | 入参 |
|---|---|---|---|
| POST | `/files/upload` | 上传文件到 MinIO | multipart: file |
