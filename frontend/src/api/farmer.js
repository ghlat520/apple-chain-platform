import request from './request'

export const farmerApi = {
  list: (params) => request.get('/farmers', { params }),
  getById: (id) => request.get(`/farmers/${id}`),
  create: (data) => request.post('/farmers', data),
  update: (id, data) => request.put(`/farmers/${id}`, data),
  export: () => request.get('/farmers/export', { responseType: 'blob' })
}
