import request from './request'

export const warehouseApi = {
  list: (params) => request.get('/warehouse', { params }),
  getById: (id) => request.get(`/warehouse/${id}`),
  create: (data) => request.post('/warehouse', data),
  update: (id, data) => request.put(`/warehouse/${id}`, data),
  export: () => request.get('/warehouse/export', { responseType: 'blob' })
}
