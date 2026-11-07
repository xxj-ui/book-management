import request from './request'

export const notificationApi = {
  list() { return request.get('/notifications') },
  unreadCount() { return request.get('/notifications/unread-count') },
  markRead(id) { return request.put(`/notifications/${id}/read`) },
  markAllRead() { return request.put('/notifications/read-all') },
}
