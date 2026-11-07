package com.bookmgt.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 副本请求
 */
@Data
public class CopyRequest {
    private Long id;
    private Long bookId;
    private String barcode;
    private String shelfFloor;
    private String shelfZone;
    private String shelfNo;
    private Integer status;
    private LocalDate purchaseDate;
}
