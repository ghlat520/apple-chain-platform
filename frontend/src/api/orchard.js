import request from './request'

export const orchardApi = {
  // 修正: /orchards → /planting/orchard (匹配后端 OrchardController @RequestMapping("/api/planting/orchard"))
  list: (params) => request.get('/planting/orchard/list', { params }),
  getById: (id) => request.get(`/planting/orchard/${id}`),
  create: (data) => request.post('/planting/orchard', data),
  update: (id, data) => request.put(`/planting/orchard/${id}`, data),
  delete: (id) => request.delete(`/planting/orchard/${id}`),
  export: () => request.get('/planting/orchard/export', { responseType: 'blob' }),

  // GIS
  geoBbox: (params) => request.get('/planting/orchard/geo/bbox', { params }),
  saveBoundary: (id, data) => request.post(`/planting/orchard/geo/${id}/boundary`, data)
}
