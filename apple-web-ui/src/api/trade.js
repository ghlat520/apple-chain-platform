import request, { downloadFile } from './request.js'

// ========== 质检管理 — 独立具名导出（Inspection.vue 使用） ==========
export function listInspections(params) { return request.get('/trade/inspection', { params }) }
export function getInspection(id) { return request.get(`/trade/inspection/${id}`) }
export function createInspection(data) { return request.post('/trade/inspection', data) }
export function updateInspection(id, data) { return request.put(`/trade/inspection/${id}`, data) }
export function deleteInspection(id) { return request.delete(`/trade/inspection/${id}`) }
export function exportInspections() { return downloadFile('/api/trade/inspection/export', '质量检验记录.csv') }

export const tradeApi = {
  // 供货信息
  getSupplies: (params) => request.get('/trade/supply/list', { params }),
  getSupply: (id) => request.get(`/trade/supply/${id}`),
  createSupply: (data) => request.post('/trade/supply', data),
  updateSupply: (id, data) => request.put(`/trade/supply/${id}`, data),
  deleteSupply: (id) => request.delete(`/trade/supply/${id}`),
  exportSupplies: () => downloadFile('/api/trade/supply/export', '供货信息.csv'),

  // 交易订单 (对接 TradeOrderController: /api/trade/order — 权威，TradeOrderMvpController 已废弃)
  getOrders: (params) => request.get('/trade/order/list', { params }),
  getOrder: (id) => request.get(`/trade/order/${id}`),
  createOrder: (data) => request.post('/trade/order', data),
  confirmOrder: (id) => request.put(`/trade/order/${id}/confirm`),
  deliverOrder: (id) => request.put(`/trade/order/${id}/deliver`),
  completeOrder: (id) => request.put(`/trade/order/${id}/complete`),
  cancelOrder: (id) => request.put(`/trade/order/${id}/cancel`),
  deleteOrder: (id) => request.delete(`/trade/order/${id}`),
  exportOrders: () => downloadFile('/api/trade/order/export', '交易订单.csv'),

  // 采购需求
  getPurchaseNeeds: (params) => request.get('/trade/need/list', { params }),
  getPurchaseNeed: (id) => request.get(`/trade/need/${id}`),
  createPurchaseNeed: (data) => request.post('/trade/need', data),
  updatePurchaseNeed: (id, data) => request.put(`/trade/need/${id}`, data),
  publishPurchaseNeed: (id) => request.put(`/trade/need/${id}/publish`),
  deletePurchaseNeed: (id) => request.delete(`/trade/need/${id}`),
  exportPurchaseNeeds: () => downloadFile('/api/trade/need/export', '采购需求.csv'),

  // 质检管理
  getInspections: (params) => request.get('/trade/inspection', { params }),
  getInspection: (id) => request.get(`/trade/inspection/${id}`),
  createInspection: (data) => request.post('/trade/inspection', data),
  inspectResult: (id, data) => request.put(`/trade/inspection/${id}/inspect`, data),
  acceptInspection: (id) => request.put(`/trade/inspection/${id}/accept`),
  disputeInspection: (id, reason) => request.put(`/trade/inspection/${id}/dispute`, { reason }),
  exportInspections: () => downloadFile('/api/trade/inspection/export', '质量检验记录.csv'),

  // 交易统计
  getTradeSummary: () => request.get('/trade/statistics/summary'),
  getTradeVariety: () => request.get('/trade/statistics/variety'),
  getTradeTrend: (months = 6) => request.get('/trade/statistics/trend', { params: { months } }),
  getTradePriceIndex: (variety, days = 30) => request.get('/trade/statistics/price-index', { params: { variety, days } }),

  // 撮合匹配
  computeMatch: (supplyId, topK = 10) => request.post('/trade/match/compute', null, { params: { supplyId, topK } }),
  getMatchTop: (supplyId, topK = 10) => request.get(`/trade/match/supply/${supplyId}/top`, { params: { topK } }),
  // 议价
  startNegotiation: (data) => request.post('/trade/negotiation/start', data),
  offerNegotiation: (id, data) => request.post(`/trade/negotiation/${id}/offer`, data),
  acceptNegotiation: (id, data) => request.post(`/trade/negotiation/${id}/accept`, data),
  cancelNegotiation: (id, data) => request.post(`/trade/negotiation/${id}/cancel`, data),
  // 聊天
  sendChat: (data) => request.post('/trade/chat/send', data),
  getChatHistory: (sessionId, limit = 100) => request.get(`/trade/chat/${sessionId}/history`, { params: { limit } }),
  markChatRead: (sessionId, userId) => request.post(`/trade/chat/${sessionId}/read`, null, { params: { userId } }),
}

// ========== Contract-lint 补齐 ==========
export function publishSupply(id) { return request.put(`/trade/supply/${id}/publish`) }
export function createContract(data) { return request.post('/trade/contract/create', data) }
export function signContract(orderId, data) { return request.post(`/trade/contract/${orderId}/sign`, data) }
export function createPayment(data) { return request.post('/trade/payment/create', data) }
export function wechatPayCallback(data) { return request.post('/trade/payment/callback/wechat', data) }
export function issueInvoice(data) { return request.post('/trade/invoice/issue', data) }
