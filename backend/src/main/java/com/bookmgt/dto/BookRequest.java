package com.bookmgt.dto;

import lombok.Data;

/**
 * 图书新增/编辑请求
 */
@Data
public class BookRequest {
    private Long id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String publishYear;
    private Long categoryId;
    private String description;
    private String coverUrl;
    private Integer status;

    // 新增时同时添加的副本
    private java.util.List<CopyRequest> copies;
}
