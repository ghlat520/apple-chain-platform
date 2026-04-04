import request from './request.js'

export const authApi = {
  login: (data) => request.post('/user/auth/login', data),
  logout: () => request.post('/user/auth/logout'),
  getProfile: () => request.get('/user/auth/profile'),
  updatePassword: (data) => request.put('/user/auth/password', data)
}
