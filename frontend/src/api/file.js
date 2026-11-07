import request from './request'

/**
 * 文件上传 API
 */
export const fileApi = {
  /** 上传文件 */
  upload(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
}
