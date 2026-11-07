package com.bookmgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 假期延期配置
 */
@Data
@TableName("t_holiday_config")
public class HolidayConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 假期类型 1=暑假 2=寒假 */
    private Integer type;

    private LocalDate startDate;

    private LocalDate endDate;

    /** 延期天数 */
    private Integer extensionDays;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
