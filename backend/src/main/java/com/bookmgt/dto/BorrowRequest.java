package com.bookmgt.dto;

import lombok.Data;

/**
 * 借书请求
 */
@Data
public class BorrowRequest {
    private Long userId;
    private Long bookId;
    private Long copyId;
}
