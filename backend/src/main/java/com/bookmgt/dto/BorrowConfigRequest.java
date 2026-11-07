package com.bookmgt.dto;

import lombok.Data;

/**
 * 借阅规则更新请求
 */
@Data
public class BorrowConfigRequest {
    private Long id;
    private Integer role;
    private Integer educationLevel;
    private Integer maxBooks;
    private Integer maxDays;
    private java.math.BigDecimal finePerDay;
    private Integer maxNewBooks;
}
