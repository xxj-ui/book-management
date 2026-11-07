import request from './request'

/**
 * 仪表盘 API
 */
export const dashboardApi = {
  /** 获取归还记录 */
  getReturns() { return request.get('/dashboard/returns') },

  /** 确认上架 */
  confirmShelf(id) { return request.put(`/dashboard/returns/${id}/shelf`) },

  /** 标记丢失 */
  markLost(id) { return request.put(`/dashboard/returns/${id}/lost`) },

  /** 批量上架 */
  batchShelf(ids) { return request.post('/dashboard/returns/batch-shelf', ids) },
}
