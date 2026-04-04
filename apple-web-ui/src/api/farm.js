import request, { downloadFile } from './request.js'

export const farmApi = {
  getOrchards: (params) => request.get('/farm/orchards', { params }),
  getOrchard: (id) => request.get(`/farm/orchards/${id}`),
  createOrchard: (data) => request.post('/farm/orchards', data),
  updateOrchard: (id, data) => request.put(`/farm/orchards/${id}`, data),
  deleteOrchard: (id) => request.delete(`/farm/orchards/${id}`),
  exportOrchards: () => downloadFile('/api/farm/orchards/export', '果园列表.csv')
}
