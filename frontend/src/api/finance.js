import request from './request'

export const financeApi = {
  // ---- Loan ----
  // 修正: approve/reject/disburse/repay/settle 从 PUT → POST (匹配后端 @PostMapping)
  loanList: (params) => request.get('/finance/loans/list', { params }),
  loanDetail: (id) => request.get(`/finance/loans/${id}`),
  loanCreate: (data) => request.post('/finance/loans', data),
  loanUpdate: (id, data) => request.put(`/finance/loans/${id}`, data),
  loanApprove: (id) => request.post(`/finance/loans/${id}/approve`),
  loanReject: (id) => request.post(`/finance/loans/${id}/reject`),
  loanDisburse: (id) => request.post(`/finance/loans/${id}/disburse`),
  loanRepay: (id) => request.post(`/finance/loans/${id}/repay`),
  loanSettle: (id) => request.post(`/finance/loans/${id}/settle`),
  loanDelete: (id) => request.delete(`/finance/loans/${id}`),
  loanExport: (params) => request.get('/finance/loans/export', { params, responseType: 'blob' }),

  // ---- Pledge ----
  // 修正: activate/release/default 从 PUT → POST (匹配后端 @PostMapping)
  pledgeList: (params) => request.get('/finance/pledges/list', { params }),
  pledgeDetail: (id) => request.get(`/finance/pledges/${id}`),
  pledgeCreate: (data) => request.post('/finance/pledges', data),
  pledgeUpdate: (id, data) => request.put(`/finance/pledges/${id}`, data),
  pledgeActivate: (id) => request.post(`/finance/pledges/${id}/activate`),
  pledgeRelease: (id) => request.post(`/finance/pledges/${id}/release`),
  pledgeDefault: (id) => request.post(`/finance/pledges/${id}/default`),
  pledgeDelete: (id) => request.delete(`/finance/pledges/${id}`),
  pledgeExport: (params) => request.get('/finance/pledges/export', { params, responseType: 'blob' }),

  // ---- Credit Rating ----
  // 修正: credit-ratings → credits (匹配后端 CreditRatingController @RequestMapping("/api/finance/credits"))
  ratingList: (params) => request.get('/finance/credits/list', { params }),
  ratingDetail: (id) => request.get(`/finance/credits/${id}`),
  ratingCreate: (data) => request.post('/finance/credits', data),
  ratingCalculate: (entityId) => request.post(`/finance/credits/${entityId}/calculate`),
  ratingExport: (params) => request.get('/finance/credits/export', { params, responseType: 'blob' }),

  // ---- Risk ----
  // 删除: riskResolve/riskEscalate (后端不存在这两个接口)
  riskList: (params) => request.get('/finance/risks/list', { params }),
  riskDetail: (id) => request.get(`/finance/risks/${id}`),
  riskCreate: (data) => request.post('/finance/risks', data),
  riskUpdate: (id, data) => request.put(`/finance/risks/${id}`, data),
  riskDelete: (id) => request.delete(`/finance/risks/${id}`),
  riskExport: (params) => request.get('/finance/risks/export', { params, responseType: 'blob' }),

  // ---- Statistics ----
  // 删除: statisticsTrend (后端不存在)
  statisticsSummary: () => request.get('/finance/statistics/summary'),
  statisticsRisk: () => request.get('/finance/statistics/risk')
}
