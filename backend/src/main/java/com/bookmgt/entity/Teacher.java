package com.bookmgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_teacher")
public class Teacher {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    @JsonIgnore
    private String password;
    private String name;
    private Long majorId;
    private String phone;
    private Integer status;

    /** 软删除标记 */
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String majorName;
    @TableField(exist = false)
    private String departmentName;
    @TableField(exist = false)
    private Integer currentBorrows;
    @TableField(exist = false)
    private Integer totalBorrows;
}
