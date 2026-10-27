import request from './request.js'

export const authApi = {
  login: (data) => request.post('/user/auth/login', data),
  logout: () => request.post('/user/auth/logout'),
  getProfile: () => request.get('/user/auth/profile'),
  // /me returns user + roles + permissions in a single call (M1 RBAC).
  getMe: () => request.get('/user/auth/me'),
  updatePassword: (data) => request.put('/user/auth/password', data)
}

export const userApi = {
  listUsers: (params) => request.get('/user/list', { params }),
  createUser: (data) => request.post('/user/create', data),
  updateUser: (id, data) => request.put(`/user/${id}`, data),
  deleteUser: (id) => request.delete(`/user/${id}`),
  exportUsers: (params) => request.get('/user/export', { params, responseType: 'blob' }),
}

export const rbacApi = {
  listRoles: () => request.get('/admin/roles'),
  listPermissions: () => request.get('/admin/permissions'),
  getMatrix: () => request.get('/admin/permissions/matrix'),
  getUserRoles: (userId) => request.get(`/admin/users/${userId}/roles`),
  assignRoles: (userId, roleCodes) =>
    request.post(`/admin/users/${userId}/roles`, { roleCodes })
}
