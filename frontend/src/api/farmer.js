import request from './request'

export const farmerApi = {
  list: (params) => request.get('/farm/farmers', { params }),
  getById: (id) => request.get(`/farm/farmers/${id}`),
  create: (data) => request.post('/farm/farmers', data),
  update: (id, data) => request.put(`/farm/farmers/${id}`, data),
  export: () => request.get('/farm/farmers/export', { responseType: 'blob' })
}
