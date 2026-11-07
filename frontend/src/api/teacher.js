import request from './request'

/**
 * 教师管理 API
 */
export const teacherApi = {
  list(params) { return request.get('/teachers', { params }) },
  get(id) { return request.get(`/teachers/${id}`) },
  add(data) { return request.post('/teachers', data) },
  update(id, data) { return request.put(`/teachers/${id}`, data) },
  delete(id) { return request.delete(`/teachers/${id}`) },
}
