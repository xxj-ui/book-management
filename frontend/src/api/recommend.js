import request from './request'

export const recommendApi = {
  list() { return request.get('/recommends') },
  add(data) { return request.post('/recommends', data) },
  delete(id) { return request.delete(`/recommends/${id}`) },
  updateSort(data) { return request.put('/recommends/sort', data) },
}
