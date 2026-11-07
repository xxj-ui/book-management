package com.bookmgt.vo;

import lombok.Data;

/**
 * 登录响应 VO — 双 token（access token + refresh token）
 */
@Data
public class LoginVO {
    /** 访问令牌（短有效期：30 分钟） */
    private String token;

    /** 刷新令牌（长有效期：7 天，用于自动续期） */
    private String refreshToken;

    /** 过期时间戳（毫秒） */
    private Long expiresIn;

    /** 用户信息 */
    private UserVO user;

    @Data
    public static class UserVO {
        private String id;
        /** 数据库 ID（用于 API 请求） */
        private Long dbId;
        private String name;
        private String role;
        private String phone;
        private String department;
        private String major;
        private String className;
        private String educationLevel;
        private String status;
        private Integer currentBorrows;
        private Integer totalBorrows;
        private Integer maxBooks;
        private Integer maxDays;
    }
}
