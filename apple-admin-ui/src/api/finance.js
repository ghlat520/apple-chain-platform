import request from './request.js'

// ========== 贷款管理 (LoanController — /finance/loans) ==========
export const loanApi = {
  list: (params) => request.get('/finance/loans/list', { params }),
  get: (id) => request.get(`/finance/loans/${id}`),
  create: (data) => request.post('/finance/loans', data),
  update: (id, data) => request.put(`/finance/loans/${id}`, data),
  approve: (id) => request.post(`/finance/loans/${id}/approve`),
  reject: (id) => request.post(`/finance/loans/${id}/reject`),
  disburse: (id) => request.post(`/finance/loans/${id}/disburse`),
  repay: (id, amount) => request.post(`/finance/loans/${id}/repay`, null, { params: { amount } }),
  settle: (id) => request.post(`/finance/loans/${id}/settle`),
  markOverdue: (id) => request.post(`/finance/loans/${id}/overdue`),
  delete: (id) => request.delete(`/finance/loans/${id}`),
}

// ========== 质押管理 (PledgeController — /finance/pledges) ==========
export const pledgeApi = {
  list: (params) => request.get('/finance/pledges/list', { params }),
  get: (id) => request.get(`/finance/pledges/${id}`),
  create: (data) => request.post('/finance/pledges', data),
  update: (id, data) => request.put(`/finance/pledges/${id}`, data),
  activate: (id) => request.post(`/finance/pledges/${id}/activate`),
  release: (id) => request.post(`/finance/pledges/${id}/release`),
  defaultPledge: (id) => request.post(`/finance/pledges/${id}/default`),
  delete: (id) => request.delete(`/finance/pledges/${id}`),
}

// ========== 信用评级 (CreditController — /finance/credits) ==========
export const creditApi = {
  list: (params) => request.get('/finance/credits/list', { params }),
  get: (id) => request.get(`/finance/credits/${id}`),
  create: (data) => request.post('/finance/credits', data),
  update: (id, data) => request.put(`/finance/credits/${id}`, data),
  delete: (id) => request.delete(`/finance/credits/${id}`),
  calculate: (entityId, entityType) => request.post(`/finance/credits/${entityId}/calculate`, null, { params: { entityType } }),
}

// ========== 风控记录 (RiskController — /finance/risks) ==========
export const riskApi = {
  list: (params) => request.get('/finance/risks/list', { params }),
  get: (id) => request.get(`/finance/risks/${id}`),
  create: (data) => request.post('/finance/risks', data),
  update: (id, data) => request.put(`/finance/risks/${id}`, data),
  delete: (id) => request.delete(`/finance/risks/${id}`),
}

// ========== 金融统计 (FinanceStatisticsController — /finance/statistics) ==========
export const financeStatsApi = {
  summary: () => request.get('/finance/statistics/summary'),
  risk: (params) => request.get('/finance/statistics/risk', { params }),
}
