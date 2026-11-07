package com.bookmgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 借阅记录
 */
@Data
@TableName("t_borrow_record")
public class BorrowRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 借阅单号 */
    private String borrowNo;

    private Long userId;

    private Long copyId;

    private LocalDate borrowDate;

    private LocalDate dueDate;

    private LocalDate returnDate;

    /** 状态 1=借阅中 2=已归还 3=超期 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ===== 非数据库字段 =====
    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String userRoleName;

    @TableField(exist = false)
    private String bookTitle;

    @TableField(exist = false)
    private String isbn;

    @TableField(exist = false)
    private String barcode;

    @TableField(exist = false)
    private Integer overdueDays;

    @TableField(exist = false)
    private Double fineAmount;

    @TableField(exist = false)
    private Integer renewCount;

    @TableField(exist = false)
    private Integer copyStatus;
}
