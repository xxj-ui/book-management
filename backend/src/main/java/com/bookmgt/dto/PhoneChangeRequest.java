package com.bookmgt.dto;

import lombok.Data;

/**
 * 修改手机号请求
 */
@Data
public class PhoneChangeRequest {
    private String newPhone;
    private String verifyCode;
}
