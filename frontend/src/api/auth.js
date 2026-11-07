import request from './request'

/**
 * 认证相关 API（Sa-Token 双 token 版本）
 *
 * 登录响应格式:
 * {
 *   token: "xxx",          // access token（30 分钟有效）
 *   refreshToken: "xxx",   // refresh token（7 天有效）
 *   expiresIn: 1700000000, // 过期时间戳
 *   user: { ... }          // 用户信息
 * }
 */
export const authApi = {
  /** 密码登录 */
  login(data) { return request.post('/auth/login', data) },

  /** 短信验证码登录 */
  smsLogin(data) { return request.post('/auth/sms-login', data) },

  /** 发送短信验证码 */
  sendSmsCode(phone) { return request.post('/auth/sms-code', null, { params: { phone } }) },

  /** 刷新 access token */
  refreshToken(refreshToken) { return request.post('/auth/refresh', { refreshToken }) },

  /** 验证身份（当前密码 + 验证码） */
  verifyIdentity(data) { return request.post('/auth/verify-identity', data) },

  /** 仅验证短信验证码 */
  verifySmsCode(data) { return request.post('/auth/verify-sms-code', data) },

  /** 修改密码 */
  changePassword(data) { return request.post('/auth/change-password', data) },

  /** 修改手机号 */
  changePhone(data) { return request.post('/auth/change-phone', data) },

  /** 获取当前用户信息 */
  getUserInfo() { return request.get('/auth/user-info') },

  /** 退出登录 */
  logout() { return request.post('/auth/logout') },
}
