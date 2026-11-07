import request from './request'

/**
 * 统计报表 API
 */
export const reportApi = {
  /** 获取概览统计 */
  getOverview(params) { return request.get('/reports/overview', { params }) },

  /** 获取月度借阅趋势 */
  getMonthlyTrend(params) { return request.get('/reports/monthly-trend', { params }) },

  /** 获取分类分布 */
  getCategoryDistribution(params) { return request.get('/reports/category-distribution', { params }) },

  /** 导出 Excel */
  exportExcel(params) { return request.get('/reports/export', { params, responseType: 'blob' }) },
}
