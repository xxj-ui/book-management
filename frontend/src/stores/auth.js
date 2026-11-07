import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import { saveTokens, clearTokens } from '@/api/request'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const isLoggedIn = ref(!!sessionStorage.getItem('token'))

  const role = computed(() => user.value?.role || '')
  const isAdmin = computed(() => role.value === 'admin')
  const isReader = computed(() => role.value === 'student' || role.value === 'teacher')

  // 初始化时自动恢复用户信息
  restoreUser()

  /**
   * 密码登录 — 接收 Sa-Token 双 token 响应
   */
  async function login(phone, password) {
    const res = await authApi.login({ phone, password })
    const { token, refreshToken, expiresIn, user: userData } = res.data
    // 存入本地存储
    saveTokens(token, refreshToken, expiresIn)
    sessionStorage.setItem('user', JSON.stringify(userData))
    user.value = userData
    isLoggedIn.value = true
    return userData
  }

  /**
   * 短信验证码登录
   */
  async function smsLogin(phone, code) {
    const res = await authApi.smsLogin({ phone, code })
    const { token, refreshToken, expiresIn, user: userData } = res.data
    saveTokens(token, refreshToken, expiresIn)
    sessionStorage.setItem('user', JSON.stringify(userData))
    user.value = userData
    isLoggedIn.value = true
    return userData
  }

  /**
   * 快速切换角色（开发/演示用 — 绕过后端直接构造本地数据）
   */
  function quickLogin(roleKey) {
    const roleMap = {
      admin: { name: '管理员', avatar: '管', tag: '系统管理员', roleLabel: '管理员' },
      teacher: { name: '教师示例', avatar: '师', tag: '教师·软件工程', roleLabel: '教师' },
      student: { name: '学生示例', avatar: '学', tag: '本科·软件2411', roleLabel: '学生' }
    }
    const info = roleMap[roleKey]
    if (!info) return
    const userData = {
      ...info,
      role: roleKey,
      id: roleKey === 'teacher' ? 'T2001011'
        : roleKey === 'admin' ? 'admin'
          : '2024110101',
      phone: roleKey === 'admin' ? '13900000000'
        : roleKey === 'teacher' ? '13900000001'
          : '13800000001',
      department: '计算机工程学院',
      major: '软件工程',
      className: roleKey === 'admin' ? '' : (roleKey === 'teacher' ? '' : '软件2411'),
      educationLevel: roleKey === 'student' ? '本科' : '',
      status: 'active',
      currentBorrows: 3,
      totalBorrows: 18,
      maxBooks: roleKey === 'teacher' ? 20 : 8,
      maxDays: roleKey === 'teacher' ? 180 : 30,
    }
    saveTokens('dev-token-' + Date.now(), 'dev-refresh-' + Date.now(), Date.now() + 86400000)
    sessionStorage.setItem('user', JSON.stringify(userData))
    user.value = userData
    isLoggedIn.value = true
  }

  /**
   * 退出登录
   */
  function logout() {
    try {
      authApi.logout().catch(() => {})
    } finally {
      clearTokens()
      user.value = null
      isLoggedIn.value = false
    }
  }

  /**
   * 从 sessionStorage 恢复用户信息
   */
  function restoreUser() {
    const saved = sessionStorage.getItem('user')
    if (saved) {
      try {
        user.value = JSON.parse(saved)
        isLoggedIn.value = true
      } catch {
        clearTokens()
      }
    }
  }

  return {
    user, isLoggedIn, role, isAdmin, isReader,
    login, smsLogin, quickLogin, logout, restoreUser
  }
})
