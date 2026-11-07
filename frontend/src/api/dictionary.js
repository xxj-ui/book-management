import request from './request'

/**
 * 字典 API（学院/专业/班级）
 */
export const dictApi = {
  /** 获取学院列表 */
  getDepartments() { return request.get('/dictionary/departments') },

  /** 获取专业列表 */
  getMajors(departmentId) { return request.get('/dictionary/majors', { params: { departmentId } }) },

  /** 获取班级列表 */
  getClasses(majorId) { return request.get('/dictionary/classes', { params: { majorId } }) },

  /** 获取全部班级（含学院专业信息） */
  getAllClasses() { return request.get('/dictionary/all-classes') },
}
