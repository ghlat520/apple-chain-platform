import request from './request.js'

export const supplyApi = {
  list: (params) => request.get('/trade/supply/list', { params }),
  get: (id) => request.get(`/trade/supply/${id}`),
  create: (data) => request.post('/trade/supply', data),
  update: (id, data) => request.put(`/trade/supply/${id}`, data),
  publish: (id) => request.put(`/trade/supply/${id}/publish`),
  delete: (id) => request.delete(`/trade/supply/${id}`),
}

export const needApi = {
  list: (params) => request.get('/trade/need/list', { params }),
  get: (id) => request.get(`/trade/need/${id}`),
  create: (data) => request.post('/trade/need', data),
  update: (id, data) => request.put(`/trade/need/${id}`, data),
  publish: (id) => request.put(`/trade/need/${id}/publish`),
  delete: (id) => request.delete(`/trade/need/${id}`),
}

export const orderApi = {
  list: (params) => request.get('/trade/order/list', { params }),
  get: (id) => request.get(`/trade/order/${id}`),
  create: (data) => request.post('/trade/order', data),
  confirm: (id) => request.put(`/trade/order/${id}/confirm`),
  deliver: (id) => request.put(`/trade/order/${id}/deliver`),
  complete: (id) => request.put(`/trade/order/${id}/complete`),
  cancel: (id) => request.put(`/trade/order/${id}/cancel`),
}

export const matchApi = {
  compute: (params) => request.post('/trade/match/compute', null, { params }),
  top: (supplyId, topK = 10) => request.get(`/trade/match/supply/${supplyId}/top`, { params: { topK } }),
}

export const inspectionApi = {
  list: (params) => request.get('/trade/inspection', { params }),
  get: (id) => request.get(`/trade/inspection/${id}`),
  create: (data) => request.post('/trade/inspection', data),
  inspect: (id, data) => request.put(`/trade/inspection/${id}/inspect`, data),
  accept: (id) => request.put(`/trade/inspection/${id}/accept`),
  dispute: (id, data) => request.put(`/trade/inspection/${id}/dispute`, data),
}

export const tradeStatsApi = {
  summary: (params) => request.get('/trade/statistics/summary', { params }),
  variety: (params) => request.get('/trade/statistics/variety', { params }),
  trend: (params) => request.get('/trade/statistics/trend', { params }),
  priceIndex: (params) => request.get('/trade/statistics/price-index', { params }),
}
