import request from './request.js'

export const statsApi = {
  getSummary: () => request.get('/stats/summary'),
  getTrend: () => request.get('/stats/trend'),
  getTopVarieties: () => request.get('/stats/top-varieties'),
  getPending: () => request.get('/stats/pending'),
  getDashboardStats: () => request.get('/dashboard/stats'),
  getDashboardTrend: () => request.get('/dashboard/trend'),
  getTopOrchards: () => request.get('/dashboard/top-orchards'),
  getPendingActions: () => request.get('/dashboard/pending-actions')
}
