package com.bookmgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图书评论
 */
@Data
@TableName("t_comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bookId;

    private Long userId;

    /** 评分 1-5 */
    private Integer rating;

    private String content;

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
    private String isbn;
}
