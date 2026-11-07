package com.bookmgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 借阅规则配置
 */
@Data
@TableName("t_borrow_config")
public class BorrowConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 适用角色 1=学生 2=教师 */
    private Integer role;

    /** 学历 */
    private Integer educationLevel;

    /** 最大借阅册数 */
    private Integer maxBooks;

    /** 最大借阅天数 */
    private Integer maxDays;

    /** 超期费率 */
    private BigDecimal finePerDay;

    /** 最大新书借阅数 */
    private Integer maxNewBooks;
}
