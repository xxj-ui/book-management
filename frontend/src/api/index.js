/**
 * API 统一导出
 *
 * 使用方式:
 *   import { authApi, bookApi } from '@/api'
 *   authApi.login({ phone: '13800000001', password: '123456' }).then(res => { ... })
 */
export { authApi } from './auth'
export { bookApi } from './book'
export { categoryApi } from './category'
export { copyApi } from './copy'
export { studentApi } from './student'
export { teacherApi } from './teacher'
export { borrowApi } from './borrow'
export { commentApi } from './comment'
export { configApi } from './config'
export { dictApi } from './dictionary'
export { dashboardApi } from './dashboard'
export { reportApi } from './report'
export { fileApi } from './file'
export { notificationApi } from './notification'
export { fineApi } from './fine'
export { recommendApi } from './recommend'
export { bannerApi } from './banner'
export { default as request } from './request'
