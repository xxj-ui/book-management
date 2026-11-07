package com.bookmgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图书
 */
@Data
@TableName("t_book")
public class Book {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String isbn;

    private String title;

    private String author;

    private String publisher;

    /** 出版年份 */
    private String publishYear;

    private Long categoryId;

    private String description;

    private String coverUrl;

    /** 状态 1=上架 0=下架 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ===== 非数据库字段 =====
    @TableField(exist = false)
    private String categoryName;

    @TableField(exist = false)
    private Integer totalCopies;

    @TableField(exist = false)
    private Integer availableCopies;

    /** 是否为新书 */
    @TableField(exist = false)
    private Boolean isNew;

    /** 累计借阅次数 */
    @TableField(exist = false)
    private Integer totalBorrows;
}
