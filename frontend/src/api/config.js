import request from './request'

/**
 * 配置 API
 */
export const configApi = {
  /** 获取借阅规则配置 */
  getBorrowConfigs() { return request.get('/configs/borrow') },

  /** 更新借阅规则配置 */
  updateBorrowConfigs(data) { return request.put('/configs/borrow', data) },

  /** 获取假期配置 */
  getHolidayConfigs() { return request.get('/configs/holiday') },

  /** 更新假期配置 */
  updateHolidayConfig(data) { return request.put('/configs/holiday', data) },
}
