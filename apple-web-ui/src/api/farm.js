import request, { downloadFile } from './request.js'

export const farmApi = {
  // 果园管理 — 修正路径匹配后端 OrchardController @RequestMapping("/api/planting/orchard")
  getOrchards: (params) => request.get('/planting/orchard/list', { params }),
  getOrchard: (id) => request.get(`/planting/orchard/${id}`),
  createOrchard: (data) => request.post('/planting/orchard', data),
  updateOrchard: (id, data) => request.put(`/planting/orchard/${id}`, data),
  deleteOrchard: (id) => request.delete(`/planting/orchard/${id}`),
  exportOrchards: () => downloadFile('/api/planting/orchard/export', '果园列表.csv'),

  // 农户管理 — 路径正确 (/farm/farmers → baseURL拼接后 /api/farm/farmers)
  getFarmers: (params) => request.get('/farm/farmers', { params }),
  getFarmer: (id) => request.get(`/farm/farmers/${id}`),
  createFarmer: (data) => request.post('/farm/farmers', data),
  updateFarmer: (id, data) => request.put(`/farm/farmers/${id}`, data),
  deleteFarmer: (id) => request.delete(`/farm/farmers/${id}`),
  exportFarmers: () => downloadFile('/api/farm/farmers/export', '农户列表.csv')
}
