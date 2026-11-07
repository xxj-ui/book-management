package com.bookmgt.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.bookmgt.service.AuthService;
import com.bookmgt.vo.LoginVO;
import com.bookmgt.common.result.Result;
import com.bookmgt.dto.LoginRequest;
import com.bookmgt.dto.PasswordChangeRequest;
import com.bookmgt.dto.PhoneChangeRequest;
import com.bookmgt.dto.SmsLoginRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * 双 token 机制说明:
 *   - 登录后返回 { token, refreshToken, expiresIn }
 *   - access token 30 分钟过期，需要携带在 Authorization 请求头
 *   - access token 过期后，使用 refreshToken 调用 /auth/refresh 获取新的 token
 *   - refresh token 7 天过期，过期后需重新登录
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 密码登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest req) {
        return Result.success(authService.login(req));
    }

    /** 短信验证码登录 */
    @PostMapping("/sms-login")
    public Result<LoginVO> smsLogin(@Valid @RequestBody SmsLoginRequest req) {
        return Result.success(authService.smsLogin(req));
    }

    /** 发送验证码 */
    @PostMapping("/sms-code")
    public Result<Void> sendSmsCode(@RequestParam String phone) {
        authService.sendSmsCode(phone);
        return Result.successMsg("验证码已发送");
    }

    /**
     * 刷新 access token
     * 请求体: { "refreshToken": "xxx" }
     */
    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@RequestBody java.util.Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return Result.error(400, "refreshToken 不能为空");
        }
        LoginVO vo = authService.refreshToken(refreshToken);
        return Result.success(vo);
    }

    /** 验证身份（当前密码 + 验证码，不改密码） */
    @PostMapping("/verify-identity")
    public Result<Void> verifyIdentity(@Valid @RequestBody PasswordChangeRequest req) {
        Long userId = StpUtil.getLoginIdAsLong();
        authService.verifyIdentity(userId, req);
        return Result.successMsg("身份验证通过");
    }

    /** 仅验证短信验证码（用于修改手机号等场景） */
    @PostMapping("/verify-sms-code")
    public Result<Void> verifySmsCode(@RequestBody java.util.Map<String, String> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        String code = body.get("verifyCode");
        if (code == null || code.isBlank()) {
            return Result.error(400, "验证码不能为空");
        }
        authService.verifySmsCode(userId, code);
        return Result.successMsg("验证码正确");
    }

    /** 修改密码 */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest req) {
        Long userId = StpUtil.getLoginIdAsLong();
        authService.changePassword(userId, req);
        return Result.successMsg("密码修改成功");
    }

    /** 修改手机号 */
    @PostMapping("/change-phone")
    public Result<Void> changePhone(@Valid @RequestBody PhoneChangeRequest req) {
        Long userId = StpUtil.getLoginIdAsLong();
        authService.changePhone(userId, req);
        return Result.successMsg("手机号修改成功");
    }

    /** 获取当前用户信息 */
    @GetMapping("/user-info")
    public Result<?> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(authService.getCurrentUser(userId));
    }

    /** 退出登录 */
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.successMsg("退出成功");
    }
}
