import request from './request'

export const tradeApi = {
  list: (params) => request.get('/trades', { params }),
  getById: (id) => request.get(`/trades/${id}`),
  create: (data) => request.post('/trades', data),
  updateStatus: (id, status) => request.put(`/trades/${id}/status`, { status }),
  export: () => request.get('/trades/export', { responseType: 'blob' })
}
