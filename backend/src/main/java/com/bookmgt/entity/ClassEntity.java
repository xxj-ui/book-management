package com.bookmgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 班级
 */
@Data
@TableName("t_class")
public class ClassEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long majorId;
}
