import request from './request'

/**
 * 副本相关 API
 */
export const copyApi = {
  /** 获取某图书全部副本 */
  listByBook(bookId) { return request.get('/copies/book/' + bookId) },

  /** 新增副本 */
  add(data) { return request.post('/copies', data) },

  /** 批量新增副本 */
  batchAdd(data) { return request.post('/copies/batch', data) },

  /** 更新副本 */
  update(id, data) { return request.put(`/copies/${id}`, data) },

  /** 删除副本 */
  delete(id) { return request.delete(`/copies/${id}`) },
}
