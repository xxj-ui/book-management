import request from './request'

export const fineApi = {
  list() { return request.get('/fines') },
  pay(id) { return request.put(`/fines/${id}/pay`) },
}
