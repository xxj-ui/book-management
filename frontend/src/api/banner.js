import request from './request'

export const bannerApi = {
  list() { return request.get('/banners') },
  adminList() { return request.get('/banners/admin') },
  add(data) { return request.post('/banners', data) },
  update(id, data) { return request.put(`/banners/${id}`, data) },
  delete(id) { return request.delete(`/banners/${id}`) },
}
