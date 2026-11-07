package com.bookmgt.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 延期请求
 */
@Data
public class ExtendRequest {
    private Long recordId;
    private LocalDate newDueDate;
}
