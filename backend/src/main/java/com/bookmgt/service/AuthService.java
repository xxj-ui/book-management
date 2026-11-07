package com.bookmgt.service;

import com.bookmgt.vo.LoginVO;
import com.bookmgt.dto.LoginRequest;
import com.bookmgt.dto.PasswordChangeRequest;
import com.bookmgt.dto.PhoneChangeRequest;
import com.bookmgt.dto.SmsLoginRequest;

public interface AuthService {
    /** 密码登录 */
    LoginVO login(LoginRequest req);

    /** 短信验证码登录 */
    LoginVO smsLogin(SmsLoginRequest req);

    /** 发送验证码 */
    void sendSmsCode(String phone);

    /** 刷新 token */
    LoginVO refreshToken(String refreshToken);

    /** 验证身份（当前密码 + 短信验证码） */
    void verifyIdentity(Long userId, PasswordChangeRequest req);

    /** 仅验证短信验证码 */
    void verifySmsCode(Long userId, String code);

    /** 修改密码 */
    void changePassword(Long userId, PasswordChangeRequest req);

    /** 修改手机号 */
    void changePhone(Long userId, PhoneChangeRequest req);

    /** 获取当前用户 */
    Object getCurrentUser(Long userId);
}
