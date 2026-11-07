package com.bookmgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 滞纳金
 */
@Data
@TableName("t_fine")
public class Fine {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    /** 超期天数 */
    private Integer overdueDays;

    /** 滞纳金金额 */
    private BigDecimal fineAmount;

    /** 状态 1=未支付 2=已支付 3=已免除 */
    private Integer status;

    /** 软删除标记 */
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
