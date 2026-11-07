package com.bookmgt.common.constant;

/**
 * 角色常量
 */
public interface RoleConst {

    /** 学生 */
    int STUDENT = 1;
    /** 教师 */
    int TEACHER = 2;
    /** 管理员 */
    int ADMIN = 3;

    /** 状态: 正常 */
    int STATUS_NORMAL = 1;
    /** 状态: 超期锁定 */
    int STATUS_OVERDUE_LOCKED = 2;
    /** 状态: 禁用 */
    int STATUS_DISABLED = 3;

    /** 学历: 高职 */
    int EDU_VOCATIONAL = 1;
    /** 学历: 本科 */
    int EDU_BACHELOR = 2;
    /** 学历: 硕士 */
    int EDU_MASTER = 3;
    /** 学历: 博士 */
    int EDU_DOCTOR = 4;

    /** 借阅状态: 借阅中 */
    int BORROW_BORROWING = 1;
    /** 借阅状态: 已归还 */
    int BORROW_RETURNED = 2;
    /** 借阅状态: 超期 */
    int BORROW_OVERDUE = 3;

    /** 借阅状态: 已归还待审核（读者已归还，等待管理员确认上架） */
    int BORROW_RETURN_PENDING = 4;

    /** 副本状态: 在架 */
    int COPY_AVAILABLE = 1;
    /** 副本状态: 借出 */
    int COPY_BORROWED = 2;
    /** 副本状态: 待上架 */
    int COPY_PENDING = 3;
    /** 副本状态: 丢失 */
    int COPY_LOST = 4;

    /** 图书状态: 上架 */
    int BOOK_ACTIVE = 1;
    /** 图书状态: 下架 */
    int BOOK_INACTIVE = 0;

    /** 滞纳金状态: 未支付 */
    int FINE_UNPAID = 1;
    /** 滞纳金状态: 已支付 */
    int FINE_PAID = 2;
    /** 滞纳金状态: 已免除 */
    int FINE_WAIVED = 3;
}
