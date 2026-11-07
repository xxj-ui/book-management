package com.bookmgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 图书副本（物理图书）
 */
@Data
@TableName("t_book_copy")
public class BookCopy {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bookId;

    /** 条码号 */
    private String barcode;

    /** 楼层 */
    private String shelfFloor;

    /** 区域 */
    private String shelfZone;

    /** 架号 */
    private String shelfNo;

    /** 状态 1=在架 2=借出 3=待上架 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDate purchaseDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 当前借阅人姓名（非数据库字段） */
    @TableField(exist = false)
    private String borrowerName;
}
