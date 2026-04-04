import request, { downloadFile } from './request.js'

export const tradeApi = {
  // 供货信息
  getSupplies: (params) => request.get('/trade/supply/list', { params }),
  getSupply: (id) => request.get(`/trade/supply/${id}`),
  createSupply: (data) => request.post('/trade/supply', data),
  updateSupply: (id, data) => request.put(`/trade/supply/${id}`, data),
  deleteSupply: (id) => request.delete(`/trade/supply/${id}`),
  exportSupplies: () => downloadFile('/api/trade/supply/export', '供货信息.csv'),

  // 交易订单 (对接 TradeOrderMvpController: /api/trade/orders)
  getOrders: (params) => request.get('/trade/orders', { params }),
  getOrder: (id) => request.get(`/trade/orders/${id}`),
  createOrder: (data) => request.post('/trade/orders', data),
  updateOrder: (id, data) => request.put(`/trade/orders/${id}`, data),
  updateOrderStatus: (id, status) => request.put(`/trade/orders/${id}/status`, null, { params: { status } }),
  deleteOrder: (id) => request.delete(`/trade/orders/${id}`),
  exportOrders: () => downloadFile('/api/trade/orders/export', '交易订单.csv'),

  // 采购需求
  getPurchaseNeeds: (params) => request.get('/trade/need/list', { params }),
  getPurchaseNeed: (id) => request.get(`/trade/need/${id}`),
  createPurchaseNeed: (data) => request.post('/trade/need', data),
  updatePurchaseNeed: (id, data) => request.put(`/trade/need/${id}`, data),
  publishPurchaseNeed: (id) => request.put(`/trade/need/${id}/publish`),
  deletePurchaseNeed: (id) => request.delete(`/trade/need/${id}`),
  exportPurchaseNeeds: () => downloadFile('/api/trade/need/export', '采购需求.csv')
}
