import request from './request'

export const dashboardApi = {
  getStats: () => request.get('/dashboard/stats'),
  getTrend: (params) => request.get('/dashboard/trend', { params }),
  getTopOrchards: () => request.get('/dashboard/top-orchards'),
  getPendingTodos: () => request.get('/dashboard/todos')
}
