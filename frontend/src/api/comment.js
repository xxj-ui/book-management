import request from './request'

/**
 * 评论 API
 */
export const commentApi = {
  /** 获取图书评论 */
  listByBook(bookId) { return request.get(`/comments/book/${bookId}`) },

  /** 发表评论 */
  add(data) { return request.post('/comments', data) },

  /** 删除评论 */
  delete(id) { return request.delete(`/comments/${id}`) },
}
