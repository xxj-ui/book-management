// Mock data for the library management system
// All data is based on the HTML prototype

export const mockBooks = [
  { isbn: '978-7-302-64075-2', title: '算法设计与分析（第3版）', author: '王晓东', publisher: '清华大学出版社', publishDate: '2025', category: '计算机', categoryId: 1, description: '本书系统地介绍了算法设计与分析的基本理论和主要方法，包括递归与分治、动态规划、贪心算法、回溯法、分支限界法等。第3版新增了在线算法和近似算法等内容，配有丰富的例题和习题，适合作为计算机专业教材。', coverUrl: '', isNew: 1, isActive: 1, totalCopies: 2, availableCopies: 1 },
  { isbn: '978-7-111-69347-7', title: '深度学习', author: 'Ian Goodfellow 等', publisher: '机械工业出版社', publishDate: '2022', category: '计算机', categoryId: 1, description: '本书涵盖了深度学习的理论基础和工程实践，包括深度前馈网络、卷积网络、循环网络、生成对抗网络等。', coverUrl: '', isNew: 0, isActive: 1, totalCopies: 3, availableCopies: 2 },
  { isbn: '978-7-302-60149-4', title: '数据结构（C++版）', author: '邓俊辉', publisher: '清华大学出版社', publishDate: '2023', category: '计算机', categoryId: 1, description: '本书以C++为描述语言，系统介绍了数据结构的基础知识和典型算法。', coverUrl: '', isNew: 0, isActive: 1, totalCopies: 5, availableCopies: 3 },
  { isbn: '978-7-115-59886-5', title: 'Java核心技术·卷I', author: 'Cay Horstmann', publisher: '人民邮电出版社', publishDate: '2023', category: '计算机', categoryId: 1, description: '本书是Java编程的经典参考书，全面介绍了Java SE的核心特性。', coverUrl: '', isNew: 0, isActive: 1, totalCopies: 4, availableCopies: 4 },
  { isbn: '978-7-302-62568-1', title: '计算机网络（第8版）', author: '谢希仁', publisher: '清华大学出版社', publishDate: '2021', category: '计算机', categoryId: 1, description: '本书系统介绍了计算机网络的基本概念、原理和技术。', coverUrl: '', isNew: 0, isActive: 1, totalCopies: 3, availableCopies: 1 },
  { isbn: '978-7-04-058130-8', title: '高等数学（第七版·上册）', author: '同济大学数学系', publisher: '高等教育出版社', publishDate: '2020', category: '理工', categoryId: 4, description: '本书是高等院校理工科通用的高等数学教材。', coverUrl: '', isNew: 0, isActive: 1, totalCopies: 10, availableCopies: 7 },
  { isbn: '978-7-111-68742-3', title: '数据库系统概念', author: 'Silberschatz', publisher: '机械工业出版社', publishDate: '2021', category: '计算机', categoryId: 1, description: '本书是数据库系统领域的经典教材。', coverUrl: '', isNew: 0, isActive: 1, totalCopies: 3, availableCopies: 0 },
]

export const mockCopies = [
  { copyId: 1, isbn: '978-7-302-64075-2', barcode: 'JG20260001', shelf_floor: '3F', shelf_zone: 'A区', shelf_no: '12架', status: '在架', purchaseDate: '2026-06-01', price: 59.00 },
  { copyId: 2, isbn: '978-7-302-64075-2', barcode: 'JG20260002', shelf_floor: '3F', shelf_zone: 'A区', shelf_no: '12架', status: '借出', purchaseDate: '2026-06-01', price: 59.00 },
  { copyId: 3, isbn: '978-7-111-69347-7', barcode: 'JG20260010', shelf_floor: '3F', shelf_zone: 'B区', shelf_no: '5架', status: '在架', purchaseDate: '2026-01-15', price: 89.00 },
  { copyId: 4, isbn: '978-7-111-69347-7', barcode: 'JG20260011', shelf_floor: '3F', shelf_zone: 'B区', shelf_no: '5架', status: '在架', purchaseDate: '2026-01-15', price: 89.00 },
  { copyId: 5, isbn: '978-7-111-69347-7', barcode: 'JG20260012', shelf_floor: '3F', shelf_zone: 'B区', shelf_no: '5架', status: '借出', purchaseDate: '2026-01-15', price: 89.00 },
  { copyId: 6, isbn: '978-7-302-60149-4', barcode: 'JG20260020', shelf_floor: '3F', shelf_zone: 'A区', shelf_no: '8架', status: '在架', purchaseDate: '2026-03-01', price: 45.00 },
  { copyId: 7, isbn: '978-7-302-60149-4', barcode: 'JG20260021', shelf_floor: '3F', shelf_zone: 'A区', shelf_no: '8架', status: '在架', purchaseDate: '2026-03-01', price: 45.00 },
  { copyId: 8, isbn: '978-7-302-60149-4', barcode: 'JG20260022', shelf_floor: '3F', shelf_zone: 'A区', shelf_no: '8架', status: '在架', purchaseDate: '2026-03-01', price: 45.00 },
  { copyId: 9, isbn: '978-7-302-60149-4', barcode: 'JG20260023', shelf_floor: '3F', shelf_zone: 'A区', shelf_no: '8架', status: '借出', purchaseDate: '2026-03-01', price: 45.00 },
  { copyId: 10, isbn: '978-7-302-60149-4', barcode: 'JG20260024', shelf_floor: '3F', shelf_zone: 'A区', shelf_no: '8架', status: '借出', purchaseDate: '2026-03-01', price: 45.00 },
  { copyId: 11, isbn: '978-7-115-59886-5', barcode: 'JG20260030', shelf_floor: '3F', shelf_zone: 'C区', shelf_no: '2架', status: '在架', purchaseDate: '2026-02-01', price: 79.00 },
  { copyId: 12, isbn: '978-7-302-62568-1', barcode: 'JG20260040', shelf_floor: '3F', shelf_zone: 'A区', shelf_no: '15架', status: '在架', purchaseDate: '2025-09-01', price: 49.00 },
  { copyId: 13, isbn: '978-7-302-62568-1', barcode: 'JG20260041', shelf_floor: '3F', shelf_zone: 'A区', shelf_no: '15架', status: '借出', purchaseDate: '2025-09-01', price: 49.00 },
  { copyId: 14, isbn: '978-7-302-62568-1', barcode: 'JG20260042', shelf_floor: '3F', shelf_zone: 'A区', shelf_no: '15架', status: '借出', purchaseDate: '2025-09-01', price: 49.00 },
  { copyId: 15, isbn: '978-7-04-058130-8', barcode: 'JG20260050', shelf_floor: '2F', shelf_zone: 'A区', shelf_no: '1架', status: '在架', purchaseDate: '2025-06-01', price: 38.00 },
  { copyId: 16, isbn: '978-7-111-68742-3', barcode: 'JG20260060', shelf_floor: '3F', shelf_zone: 'B区', shelf_no: '10架', status: '借出', purchaseDate: '2025-12-01', price: 69.00 },
]

export const mockUsers = [
  { userId: '202421332001', username: 'zhangming', realName: '学生示例', role: 'student', educationLevel: '本科', department: '计算机工程学院', major: '软件工程', classInfo: '软件2412', email: '', phone: '13800000001', status: 'active', currentBorrows: 3, totalBorrows: 26 },
  { userId: '202421332052', username: 'xiaoxiangjian', realName: '学生示例2', role: 'student', educationLevel: '本科', department: '计算机工程学院', major: '软件工程', classInfo: '软件2412', email: '', phone: '13800000002', status: 'active', currentBorrows: 1, totalBorrows: 15 },
  { userId: '202422150001', username: 'lihua', realName: '示例用户1', role: 'student', educationLevel: '硕士', department: '计算机工程学院', major: '计算机技术', classInfo: '软件研2411', email: '', phone: '13800000003', status: 'active', currentBorrows: 5, totalBorrows: 48 },
  { userId: '202320010001', username: 'wangfang', realName: '示例用户2', role: 'student', educationLevel: '博士', department: '计算机工程学院', major: '计算机科学', classInfo: '博2111', email: '', phone: '13800000004', status: 'active', currentBorrows: 8, totalBorrows: 92 },
  { userId: '202421330088', username: 'chenqiang', realName: '示例用户3', role: 'student', educationLevel: '高职', department: '计算机工程学院', major: '计算机网络', classInfo: '网络移动2411', email: '', phone: '13800000005', status: 'overdue_locked', currentBorrows: 4, totalBorrows: 12 },
  { userId: 'T2020018', username: 'liujiaoshou', realName: '示例用户4', role: 'teacher', educationLevel: '', department: '计算机工程学院', major: '计算机科学', classInfo: '', email: '', phone: '13900000007', status: 'active', currentBorrows: 2, totalBorrows: 156 },
  { userId: '202422180001', username: 'zhaoxue', realName: '示例用户5', role: 'student', educationLevel: '本科', department: '理学院', major: '数学与应用数学', classInfo: '数学2411', email: '', phone: '13800000006', status: 'active', currentBorrows: 2, totalBorrows: 18 },
  { userId: '202422180015', username: 'sunli', realName: '学生示例', role: 'student', educationLevel: '硕士', department: '理学院', major: '统计学', classInfo: '数学研2411', email: '', phone: '13800000007', status: 'active', currentBorrows: 0, totalBorrows: 7 },
  { userId: '202423450003', username: 'zhouwei', realName: '示例用户6', role: 'student', educationLevel: '本科', department: '经济管理学院', major: '会计学', classInfo: '会计2411', email: '', phone: '13800000008', status: 'active', currentBorrows: 1, totalBorrows: 9 },
  { userId: '202423450020', username: 'wuting', realName: '示例用户7', role: 'student', educationLevel: '本科', department: '经济管理学院', major: '电子商务', classInfo: '电商2411', email: '', phone: '13800000009', status: 'active', currentBorrows: 3, totalBorrows: 22 },
  { userId: '202424560001', username: 'zhengjie', realName: '示例用户8', role: 'student', educationLevel: '本科', department: '外国语学院', major: '英语', classInfo: '英语2411', email: '', phone: '13800000010', status: 'active', currentBorrows: 0, totalBorrows: 5 },
  { userId: 'T2021016', username: 'wanglaoshi', realName: '示例用户9', role: 'teacher', educationLevel: '', department: '理学院', major: '数学', classInfo: '', email: '', phone: '13900000008', status: 'active', currentBorrows: 1, totalBorrows: 43 },
  // 2023级学生
  { userId: '202321330001', username: 'huanglei', realName: '示例用户10', role: 'student', educationLevel: '本科', department: '计算机工程学院', major: '软件工程', classInfo: '软件2311', email: '', phone: '13800000011', status: 'active', currentBorrows: 2, totalBorrows: 8 },
  { userId: '202321330015', username: 'linmei', realName: '示例用户11', role: 'student', educationLevel: '本科', department: '计算机工程学院', major: '计算机科学', classInfo: '计算2312', email: '', phone: '13800000012', status: 'active', currentBorrows: 0, totalBorrows: 3 },
  { userId: '202322180010', username: 'chenyu', realName: '示例用户12', role: 'student', educationLevel: '本科', department: '理学院', major: '数学与应用数学', classInfo: '数学2311', email: '', phone: '13800000013', status: 'active', currentBorrows: 1, totalBorrows: 6 },
]

export const mockBorrowRecords = [
  { recordId: 1, borrowNo: 'B20260628001', userId: '202421332001', userName: '学生示例', userRole: '本科', copyId: 2, bookTitle: '算法设计与分析（第3版）', isbn: '978-7-302-64075-2', borrowDate: '2026-06-28', dueDate: '2026-07-28', returnDate: null, status: 'borrowing', renewCount: 0 },
  { recordId: 2, borrowNo: 'B20260627003', userId: '202422150001', userName: '示例用户1', userRole: '硕士', copyId: 5, bookTitle: '深度学习', isbn: '978-7-111-69347-7', borrowDate: '2026-05-20', dueDate: '2026-07-19', returnDate: null, status: 'borrowing', renewCount: 0 },
  { recordId: 3, borrowNo: 'B20260615008', userId: '202421330088', userName: '示例用户3', userRole: '高职', copyId: 16, bookTitle: '高等数学（上）', isbn: '978-7-04-058130-8', borrowDate: '2026-05-10', dueDate: '2026-05-25', returnDate: null, status: 'overdue', renewCount: 0 },
  { recordId: 4, borrowNo: 'B20260601002', userId: 'T2020018', userName: '示例用户4', userRole: '教师', copyId: 14, bookTitle: '深度学习', isbn: '978-7-111-69347-7', borrowDate: '2026-03-01', dueDate: '2026-08-28', returnDate: null, status: 'borrowing', renewCount: 1 },
  { recordId: 5, borrowNo: 'B20260528012', userId: '202320010001', userName: '示例用户2', userRole: '博士', copyId: 13, bookTitle: '计算机网络', isbn: '978-7-302-62568-1', borrowDate: '2026-03-15', dueDate: '2026-06-13', returnDate: null, status: 'overdue', renewCount: 0 },
]

export const mockFineRecords = [
  { fineId: 1, recordId: 3, userId: '202421330088', overdueDays: 34, fineAmount: 3.40, paidAmount: 0, status: 'unpaid' },
  { fineId: 2, recordId: 5, userId: '202320010001', overdueDays: 15, fineAmount: 1.50, paidAmount: 0, status: 'unpaid' },
]

export const mockCategories = [
  { categoryId: 1, categoryName: '计算机', bookCount: 42, isActive: 1 },
  { categoryId: 2, categoryName: '文学', bookCount: 28, isActive: 1 },
  { categoryId: 3, categoryName: '社科', bookCount: 21, isActive: 1 },
  { categoryId: 4, categoryName: '理工', bookCount: 18, isActive: 1 },
  { categoryId: 5, categoryName: '艺术', bookCount: 12, isActive: 1 },
  { categoryId: 6, categoryName: '编程语言', bookCount: 15, isActive: 1 },
  { categoryId: 7, categoryName: '算法理论', bookCount: 8, isActive: 1 },
]

export const mockBorrowConfig = [
  { role: 'student', educationLevel: '高职', maxBooks: 5, maxDays: 15, finePerDay: 0.10, maxRenewCount: 1, maxNewBooks: 2 },
  { role: 'student', educationLevel: '本科', maxBooks: 8, maxDays: 30, finePerDay: 0.10, maxRenewCount: 1, maxNewBooks: 3 },
  { role: 'student', educationLevel: '硕士', maxBooks: 10, maxDays: 60, finePerDay: 0.10, maxRenewCount: 2, maxNewBooks: 5 },
  { role: 'student', educationLevel: '博士', maxBooks: 15, maxDays: 90, finePerDay: 0.10, maxRenewCount: 2, maxNewBooks: 8 },
  { role: 'teacher', educationLevel: '', maxBooks: 20, maxDays: 180, finePerDay: 0.10, maxRenewCount: 3, maxNewBooks: 10 },
]

export const mockClasses = [
  // 计算机工程学院
  { classId: 1, className: '软件2412', department: '计算机工程学院', major: '软件工程', grade: '2024' },
  { classId: 2, className: '计算2411', department: '计算机工程学院', major: '计算机科学', grade: '2024' },
  { classId: 3, className: '软件2411', department: '计算机工程学院', major: '软件工程', grade: '2024' },
  { classId: 4, className: '软件研2411', department: '计算机工程学院', major: '计算机技术', grade: '2024' },
  { classId: 5, className: '博2111', department: '计算机工程学院', major: '计算机科学', grade: '2021' },
  { classId: 6, className: '网络移动2411', department: '计算机工程学院', major: '计算机网络', grade: '2024' },
  { classId: 18, className: '软件2311', department: '计算机工程学院', major: '软件工程', grade: '2023' },
  { classId: 19, className: '计算2312', department: '计算机工程学院', major: '计算机科学', grade: '2023' },
  // 理学院
  { classId: 7, className: '数学2411', department: '理学院', major: '数学与应用数学', grade: '2024' },
  { classId: 8, className: '物理2411', department: '理学院', major: '应用物理学', grade: '2024' },
  { classId: 9, className: '统计2411', department: '理学院', major: '统计学', grade: '2024' },
  { classId: 10, className: '数学研2411', department: '理学院', major: '数学', grade: '2024' },
  { classId: 20, className: '数学2311', department: '理学院', major: '数学与应用数学', grade: '2023' },
  // 经济管理学院
  { classId: 11, className: '会计2411', department: '经济管理学院', major: '会计学', grade: '2024' },
  { classId: 12, className: '工商2411', department: '经济管理学院', major: '工商管理', grade: '2024' },
  { classId: 13, className: '电商2411', department: '经济管理学院', major: '电子商务', grade: '2024' },
  { classId: 14, className: '金融2411', department: '经济管理学院', major: '金融学', grade: '2024' },
  // 外国语学院
  { classId: 15, className: '英语2411', department: '外国语学院', major: '英语', grade: '2024' },
  { classId: 16, className: '日语2411', department: '外国语学院', major: '日语', grade: '2024' },
  { classId: 17, className: '翻译2411', department: '外国语学院', major: '翻译', grade: '2024' },
]

export const mockReturnRecords = [
  { id: 1, title: '算法设计与分析（第3版）', barcode: 'JGLIB-2024-00001', isbn: '978-7-302-64075-2', returner: '张三', returnerRole: '本科', returnTime: '2026-06-28 10:30', shelf: '3F-A区-12架', isNew: true, overdueDays: null },
  { id: 2, title: 'Java核心技术（卷I）', barcode: 'JGLIB-2024-00015', isbn: '978-7-115-59886-5', returner: '示例用户1', returnerRole: '硕士', returnTime: '2026-06-28 09:45', shelf: '2F-B区-08架', isNew: true, overdueDays: null },
  { id: 3, title: '高等数学（上）', barcode: 'JGLIB-2024-00089', isbn: '978-7-04-058130-8', returner: '示例用户3', returnerRole: '高职', returnTime: '2026-06-28 08:20', shelf: '1F-C区-05架', isNew: false, overdueDays: 34 },
  { id: 4, title: '计算机网络（第8版）', barcode: 'JGLIB-2024-00032', isbn: '978-7-302-62568-1', returner: '赵六', returnerRole: '教师', returnTime: '2026-06-28 08:05', shelf: '3F-A区-10架', isNew: false, overdueDays: null },
  { id: 5, title: '数据库系统概念', barcode: 'JGLIB-2024-00045', isbn: '978-7-111-68742-3', returner: '示例用户2', returnerRole: '博士', returnTime: '2026-06-28 07:40', shelf: '3F-A区-15架', isNew: true, overdueDays: null },
  { id: 6, title: '软件工程导论（第6版）', barcode: 'JGLIB-2024-00068', isbn: '978-7-302-39874-5', returner: '陈静', returnerRole: '硕士', returnTime: '2026-06-27 16:45', shelf: '2F-A区-06架', isNew: false, overdueDays: null },
  { id: 7, title: '人工智能：一种现代方法', barcode: 'JGLIB-2024-00093', isbn: '978-7-302-52137-2', returner: '黄教授', returnerRole: '教师', returnTime: '2026-06-27 11:30', shelf: '3F-A区-01架', isNew: true, overdueDays: null },
]

export const mockOverdueList = [
  { userId: '202421330088', userName: '示例用户3', educationLevel: '高职', bookTitle: '高等数学（上）', isbn: '978-7-04-058130-8', borrowDate: '2026-05-10', dueDate: '2026-05-25', overdueDays: 34, fineAmount: 3.40 },
  { userId: '202320010001', userName: '示例用户2', educationLevel: '博士', bookTitle: '计算机网络', isbn: '978-7-302-62568-1', borrowDate: '2026-03-15', dueDate: '2026-06-13', overdueDays: 15, fineAmount: 1.50 },
  { userId: '202421332088', userName: '林小燕', educationLevel: '本科', bookTitle: 'Java核心技术', isbn: '978-7-115-59886-5', borrowDate: '2026-04-01', dueDate: '2026-05-01', overdueDays: 58, fineAmount: 5.80 },
  { userId: '202421332099', userName: '赵同学', educationLevel: '本科', bookTitle: '数据库系统概念', isbn: '978-7-111-68742-3', borrowDate: '2026-01-10', dueDate: '2026-02-10', overdueDays: 138, fineAmount: 13.80 },
]

export const mockComments = [
  { commentId: 1, isbn: '978-7-302-64075-2', userId: '202421332001', userName: '学生示例', userRole: '本科', rating: 5, content: '非常经典的算法教材，讲解清晰，例题丰富，适合自学和考试复习。', createdAt: '2026-06-15' },
  { commentId: 2, isbn: '978-7-302-64075-2', userId: '202422150001', userName: '示例用户1', userRole: '硕士', rating: 4, content: '内容全面，但有些章节的推导可以更详细一些。总体来说是本好书。', createdAt: '2026-06-20' },
  { commentId: 3, isbn: '978-7-302-64075-2', userId: '202320010001', userName: '示例用户2', userRole: '博士', rating: 5, content: '作为算法课程的参考书非常合适，第3版新增的内容很实用。', createdAt: '2026-06-22' },
  { commentId: 4, isbn: '978-7-111-69347-7', userId: '202421332052', userName: '学生示例2', userRole: '本科', rating: 5, content: '深度学习入门必读，虽然有一定难度但收获很大。', createdAt: '2026-06-18' },
  { commentId: 5, isbn: '978-7-302-62568-1', userId: '202422180001', userName: '示例用户5', userRole: '本科', rating: 4, content: '计网经典教材，谢老师写得很清楚。', createdAt: '2026-06-10' },
  { commentId: 6, isbn: '978-7-302-64075-2', userId: '202423450003', userName: '示例用户6', userRole: '本科', rating: 3, content: '内容不错，但纸质一般，而且书比较厚不方便携带。', createdAt: '2026-06-25' },
]
