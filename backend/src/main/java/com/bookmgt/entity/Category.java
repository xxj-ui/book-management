package com.bookmgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图书分类
 */
@Data
@TableName("t_category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 状态 1=启用 0=禁用 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private Integer bookCount;
}
