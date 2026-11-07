import request from './request'

/**
 * 图书相关 API
 */
export const bookApi = {
  /** 分页查询图书 */
  list(params) { return request.get('/books', { params }) },

  /** 通过 ISBN 获取图书详情 */
  getByIsbn(isbn) { return request.get(`/books/isbn/${isbn}`) },

  /** 新增图书 */
  add(data) { return request.post('/books', data) },

  /** 更新图书 */
  update(id, data) { return request.put(`/books/${id}`, data) },

  /** 删除图书 */
  delete(id) { return request.delete(`/books/${id}`) },

  /** 新书推荐 */
  getNewBooks(limit = 6) { return request.get('/books/new', { params: { limit } }) },

  /** 热门图书 */
  getHotBooks(limit = 5) { return request.get('/books/hot', { params: { limit } }) },

  /** 获取图书封面（从网络搜索并保存到 MinIO） */
  fetchCover(id) { return request.post(`/books/${id}/fetch-cover`) },

  /** 批量获取所有无封面的图书封面 */
  fetchAllCovers() { return request.post('/books/covers/fetch-all') },
}
