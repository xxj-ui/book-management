import request from './request'

/**
 * 学生管理 API
 */
export const studentApi = {
  list(params) { return request.get('/students', { params }) },
  get(id) { return request.get(`/students/${id}`) },
  getByUserId(userId) { return request.get(`/students/userId/${userId}`) },
  add(data) { return request.post('/students', data) },
  update(id, data) { return request.put(`/students/${id}`, data) },
  delete(id) { return request.delete(`/students/${id}`) },
}
