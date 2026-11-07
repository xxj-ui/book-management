package com.bookmgt.dto;

import lombok.Data;

/**
 * 修改密码请求
 */
@Data
public class PasswordChangeRequest {
    private String currentPassword;
    private String newPassword;
    private String verifyCode;
}
