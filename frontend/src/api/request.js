import axios from 'axios'
import { showToast } from 'vant'
import { routeMock } from '@/mock/routes'
import { shouldMockBeforeRequest, shouldMockOnFailure } from '@/mock/shouldMock'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`

  // Mock 强制模式：请求前直接返回 Mock，不发真实请求
  if (shouldMockBeforeRequest()) {
    const url = config.url || ''
    const method = (config.method || 'get').toLowerCase()
    const params = config.params || {}
    const dataBody = config.data
      ? (typeof config.data === 'string' ? JSON.parse(config.data) : config.data)
      : {}
    const mockData = routeMock(url, method, params, dataBody)
    if (mockData !== null) {
      // 通过 adapter 短路返回
      config.adapter = () =>
        Promise.resolve({
          data: mockData,
          status: 200,
          statusText: 'OK (Mock-On)',
          headers: {},
          config,
          request: {}
        })
    }
  }
  return config
})

// Mock fallback interceptor —— 真实请求失败时降级
request.interceptors.response.use(
  res => res,
  async error => {
    if (!shouldMockOnFailure()) {
      return Promise.reject(error)
    }
    const url = error.config?.url || ''
    const method = (error.config?.method || 'get').toLowerCase()
    const params = error.config?.params || {}
    const dataBody = error.config?.data ? JSON.parse(error.config.data) : {}
    const mockData = routeMock(url, method, params, dataBody)
    if (mockData !== null) {
      await new Promise(r => setTimeout(r, 150))
      console.warn(`[Mock Fallback] ${method.toUpperCase()} ${url}`)
      return { data: mockData, status: 200, statusText: 'OK (Mock-Fallback)' }
    }
    return Promise.reject(error)
  }
)

// Application-level response handler (unwrap data, show errors)
request.interceptors.response.use(
  res => res.data,
  err => {
    const msg = err.response?.data?.message || '请求失败，请稍后重试'
    showToast(msg)
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default request
