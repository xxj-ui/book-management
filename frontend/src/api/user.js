import request from './request'

/**
 * 用户相关 API
 */
export const userApi = {
  /** 分页查询用户 */
  list(params) { return request.get('/users', { params }) },

  /** 获取用户详情 */
  get(id) { return request.get(`/users/${id}`) },

  /** 通过学号/工号获取用户 */
  getByUserId(userId) { return request.get(`/users/userId/${userId}`) },

  /** 新增用户 */
  add(data) { return request.post('/users', data) },

  /** 更新用户 */
  update(id, data) { return request.put(`/users/${id}`, data) },

  /** 删除用户 */
  delete(id) { return request.delete(`/users/${id}`) },
}
