package com.bookmgt.dto;

import lombok.Data;

/**
 * 分类请求
 */
@Data
public class CategoryRequest {
    private Long id;
    private String name;
    private Integer status;
}
