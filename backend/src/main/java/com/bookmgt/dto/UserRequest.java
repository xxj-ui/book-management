package com.bookmgt.dto;

import lombok.Data;

/**
 * 用户新增/编辑请求
 */
@Data
public class UserRequest {
    private Long id;
    private String userId;
    private String password;
    private String name;
    private Integer role;
    private Integer educationLevel;
    private Long classId;
    private Long majorId;
    private String phone;
    private Integer status;
}
