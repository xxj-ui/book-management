import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

/** 是否正在刷新 token */
let isRefreshing = false
/** 等待队列（存储刷新 token 期间等待的请求） */
let refreshQueue = []

/** 从本地存储获取 access token */
function getAccessToken() {
  return sessionStorage.getItem('token')
}

/** 从本地存储获取 refresh token */
function getRefreshToken() {
  return sessionStorage.getItem('refreshToken')
}

/** 保存双 token */
function saveTokens(token, refreshToken, expiresIn) {
  sessionStorage.setItem('token', token)
  if (refreshToken) sessionStorage.setItem('refreshToken', refreshToken)
  if (expiresIn) sessionStorage.setItem('expiresIn', expiresIn)
}

/** 清除 token */
function clearTokens() {
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('refreshToken')
  sessionStorage.removeItem('expiresIn')
  sessionStorage.removeItem('user')
}

// 请求拦截器 - 添加 Token
request.interceptors.request.use(config => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = token  // Sa-Token 默认不需要 Bearer 前缀
  }
  return config
})

// 响应拦截器 - 统一处理错误 + 自动刷新 token
request.interceptors.response.use(
  response => {
    const res = response.data
    // Blob 响应直接返回（文件下载）
    if (res instanceof Blob) return response
    if (res.code === 200) return res
    if (res.code === 401) {
      // Token 过期，尝试用 refresh token 续期
      return handleTokenRefresh(response.config)
    }
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  error => {
    if (error.response?.status === 401) {
      // 401 未授权，尝试刷新 token
      const originalRequest = error.config
      // 防止死循环（避免刷新接口自己又触发 401）
      if (originalRequest.url === '/auth/refresh') {
        clearTokens()
        window.location.href = '/login'
        return Promise.reject(new Error('登录已过期，请重新登录'))
      }
      return handleTokenRefresh(originalRequest)
    }
    const msg = error.response?.data?.msg || '网络错误'
    return Promise.reject(new Error(msg))
  }
)

/**
 * 处理 token 刷新
 * 多个并发请求同时遇到 401 时，只发起一次刷新请求
 */
function handleTokenRefresh(originalRequest) {
  if (!isRefreshing) {
    isRefreshing = true
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      clearTokens()
      window.location.href = '/login'
      return Promise.reject(new Error('登录已过期'))
    }

    return axios.post('/api/auth/refresh', { refreshToken })
      .then(res => {
        const data = res.data.data
        saveTokens(data.token, data.refreshToken, data.expiresIn)
        // 重试原始请求
        originalRequest.headers.Authorization = data.token
        refreshQueue.forEach(({ resolve, config }) => {
          config.headers.Authorization = data.token
          resolve(request(config))
        })
        refreshQueue = []
        return request(originalRequest)
      })
      .catch(err => {
        clearTokens()
        refreshQueue.forEach(({ reject }) => reject(new Error('登录已过期')))
        refreshQueue = []
        window.location.href = '/login'
        return Promise.reject(new Error('登录已过期，请重新登录'))
      })
      .finally(() => {
        isRefreshing = false
      })
  } else {
    // 正在刷新中，将请求加入等待队列
    return new Promise((resolve, reject) => {
      refreshQueue.push({ resolve, reject, config: originalRequest })
    })
  }
}

export default request
export { getAccessToken, getRefreshToken, saveTokens, clearTokens }
