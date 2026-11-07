import request from './request'

/**
 * 借阅相关 API
 */
export const borrowApi = {
  /** 分页查询借阅记录 */
  list(params) { return request.get('/borrows', { params }) },

  /** 获取当前用户的借阅记录 */
  listByUser(userId) { return request.get(`/borrows/user/${userId}`) },

  /** 获取超期列表 */
  listOverdue() { return request.get('/borrows/overdue') },

  /** 获取借阅详情 */
  getDetail(id) { return request.get(`/borrows/${id}`) },

  /** 借书 */
  borrow(data) { return request.post('/borrows', data) },

  /** 还书 */
  returnBook(id) { return request.put(`/borrows/${id}/return`) },

  /** 续借 */
  renew(id) { return request.put(`/borrows/${id}/renew`) },

  /** 延期 */
  extend(data) { return request.put('/borrows/extend', data) },

  /** 批量催还 */
  batchRemind(ids) { return request.post('/borrows/batch-remind', ids) },
}
