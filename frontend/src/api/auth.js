import request from './request'

export const authApi = {
  login: (data) => request.post('/auth/login', data),
  logout: () => request.post('/auth/logout'),
  getProfile: () => request.get('/auth/profile')
}
