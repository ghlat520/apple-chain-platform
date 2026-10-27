import request from './request.js'

export const authApi = {
  login: (data) => request.post('/user/auth/login', data),
  logout: () => request.post('/user/auth/logout'),
  getMe: () => request.get('/user/auth/me'),
  updatePassword: (data) => request.put('/user/auth/password', data)
}
