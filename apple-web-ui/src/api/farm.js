import request, { downloadFile } from './request.js'

export const farmApi = {
  getOrchards: (params) => request.get('/farm/orchards', { params }),
  getOrchard: (id) => request.get(`/farm/orchards/${id}`),
  createOrchard: (data) => request.post('/farm/orchards', data),
  updateOrchard: (id, data) => request.put(`/farm/orchards/${id}`, data),
  deleteOrchard: (id) => request.delete(`/farm/orchards/${id}`),
  exportOrchards: () => downloadFile('/api/farm/orchards/export', '果园列表.csv'),

  // 农户管理
  getFarmers: (params) => request.get('/farm/farmers', { params }),
  getFarmer: (id) => request.get(`/farm/farmers/${id}`),
  createFarmer: (data) => request.post('/farm/farmers', data),
  updateFarmer: (id, data) => request.put(`/farm/farmers/${id}`, data),
  deleteFarmer: (id) => request.delete(`/farm/farmers/${id}`),
  exportFarmers: () => downloadFile('/api/farm/farmers/export', '农户列表.csv')
}
