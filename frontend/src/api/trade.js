import request from './request'

export const tradeApi = {
  // 修正: /trade/orders(废弃 MvpController) → /trade/order (TradeOrderController 权威)
  list: (params) => request.get('/trade/order/list', { params }),
  getById: (id) => request.get(`/trade/order/${id}`),
  create: (data) => request.post('/trade/order', data),
  confirmOrder: (id) => request.put(`/trade/order/${id}/confirm`),
  deliverOrder: (id) => request.put(`/trade/order/${id}/deliver`),
  completeOrder: (id) => request.put(`/trade/order/${id}/complete`),
  cancelOrder: (id) => request.put(`/trade/order/${id}/cancel`),
  delete: (id) => request.delete(`/trade/order/${id}`),
  export: () => request.get('/trade/order/export', { responseType: 'blob' }),

  // 供货信息
  supplyList: (params) => request.get('/trade/supply/list', { params }),
  supplyDetail: (id) => request.get(`/trade/supply/${id}`),
  createSupply: (data) => request.post('/trade/supply', data),
  updateSupply: (id, data) => request.put(`/trade/supply/${id}`, data),
  deleteSupply: (id) => request.delete(`/trade/supply/${id}`),

  // 采购需求
  needList: (params) => request.get('/trade/need/list', { params }),
  needDetail: (id) => request.get(`/trade/need/${id}`),
  createNeed: (data) => request.post('/trade/need', data),
  updateNeed: (id, data) => request.put(`/trade/need/${id}`, data),
  publishNeed: (id) => request.put(`/trade/need/${id}/publish`),
  deleteNeed: (id) => request.delete(`/trade/need/${id}`)
}
