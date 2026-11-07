import request from './request'

/**
 * 分类相关 API
 */
export const categoryApi = {
  /** 获取全部分类 */
  list() { return request.get('/categories') },

  /** 新增分类 */
  add(data) { return request.post('/categories', data) },

  /** 更新分类 */
  update(id, data) { return request.put(`/categories/${id}`, data) },

  /** 删除分类 */
  delete(id) { return request.delete(`/categories/${id}`) },
}
