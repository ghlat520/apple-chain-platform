import request, { downloadFile } from './request.js'

export const tradeApi = {
  // 供货信息
  getSupplies: (params) => request.get('/trade/supply/list', { params }),
  getSupply: (id) => request.get(`/trade/supply/${id}`),
  createSupply: (data) => request.post('/trade/supply', data),
  updateSupply: (id, data) => request.put(`/trade/supply/${id}`, data),
  deleteSupply: (id) => request.delete(`/trade/supply/${id}`),
  exportSupplies: () => downloadFile('/api/trade/supply/export', '供货信息.csv'),

  // 交易订单
  getOrders: (params) => request.get('/trade/orders', { params }),
  getOrder: (id) => request.get(`/trade/orders/${id}`),
  createOrder: (data) => request.post('/trade/orders', data),
  updateOrder: (id, data) => request.put(`/trade/orders/${id}`, data),
  updateOrderStatus: (id, status) => request.put(`/trade/orders/${id}/status`, null, { params: { status } }),
  deleteOrder: (id) => request.delete(`/trade/orders/${id}`),
  exportOrders: () => downloadFile('/api/trade/orders/export', '交易订单.csv')
}
