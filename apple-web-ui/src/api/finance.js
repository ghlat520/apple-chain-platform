import request, { downloadFile } from './request.js'

export const financeApi = {
  // Credit Rating
  getCredits: (params) => request.get('/finance/credits/list', { params }),
  getCredit: (id) => request.get(`/finance/credits/${id}`),
  createCredit: (data) => request.post('/finance/credits', data),
  updateCredit: (id, data) => request.put(`/finance/credits/${id}`, data),
  deleteCredit: (id) => request.delete(`/finance/credits/${id}`),
  exportCredits: () => downloadFile('/api/finance/credits/export', '信用评级.csv'),

  // Loans
  getLoans: (params) => request.get('/finance/loans/list', { params }),
  getLoan: (id) => request.get(`/finance/loans/${id}`),
  createLoan: (data) => request.post('/finance/loans', data),
  updateLoan: (id, data) => request.put(`/finance/loans/${id}`, data),
  approveLoan: (id) => request.post(`/finance/loans/${id}/approve`),
  rejectLoan: (id) => request.post(`/finance/loans/${id}/reject`),
  disburseLoan: (id) => request.post(`/finance/loans/${id}/disburse`),
  deleteLoan: (id) => request.delete(`/finance/loans/${id}`),
  exportLoans: () => downloadFile('/api/finance/loans/export', '贷款记录.csv'),

  // Pledges
  getPledges: (params) => request.get('/finance/pledges/list', { params }),
  getPledge: (id) => request.get(`/finance/pledges/${id}`),
  createPledge: (data) => request.post('/finance/pledges', data),
  updatePledge: (id, data) => request.put(`/finance/pledges/${id}`, data),
  activatePledge: (id) => request.post(`/finance/pledges/${id}/activate`),
  releasePledge: (id) => request.post(`/finance/pledges/${id}/release`),
  deletePledge: (id) => request.delete(`/finance/pledges/${id}`),
  exportPledges: () => downloadFile('/api/finance/pledges/export', '仓单质押.csv'),

  // Risk Records
  getRisks: (params) => request.get('/finance/risks/list', { params }),
  getRisk: (id) => request.get(`/finance/risks/${id}`),
  createRisk: (data) => request.post('/finance/risks', data),
  updateRisk: (id, data) => request.put(`/finance/risks/${id}`, data),
  deleteRisk: (id) => request.delete(`/finance/risks/${id}`),
  exportRisks: () => downloadFile('/api/finance/risks/export', '风控记录.csv'),

  // Statistics
  getStatisticsSummary: () => request.get('/finance/statistics/summary'),
  getStatisticsTrend: (params) => request.get('/finance/statistics/trend', { params }),
  getFinanceRisk: () => request.get('/finance/statistics/risk'),

  // Contract-lint 补齐
  processLoanWorkflow: (id, data) => request.post(`/finance/loans/${id}/process-workflow`, data),

  // 逾期 / 分类贷款申请 / 导出
  markLoanOverdue: (id) => request.post(`/finance/loans/${id}/overdue`),
  applyPlantLoan: (data) => request.post('/finance/loans/apply/plant', data),
  applyWarehouseLoan: (data) => request.post('/finance/loans/apply/warehouse', data),
  applyTradeLoan: (data) => request.post('/finance/loans/apply/trade', data),
  exportLoanApply: (data) => request.post('/finance/loans/apply/export', data),
}
