import request from './request'

export const orchardApi = {
  list: (params) => request.get('/orchards', { params }),
  getById: (id) => request.get(`/orchards/${id}`),
  create: (data) => request.post('/orchards', data),
  update: (id, data) => request.put(`/orchards/${id}`, data),
  delete: (id) => request.delete(`/orchards/${id}`),
  export: () => request.get('/orchards/export', { responseType: 'blob' })
}
