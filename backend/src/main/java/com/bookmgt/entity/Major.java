package com.bookmgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 专业
 */
@Data
@TableName("t_major")
public class Major {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long departmentId;
}
