import request from './request.js'

export const dashboardApi = {
  /** 获取总览统计 */
  getStats: () => request.get('/dashboard/stats'),

  /** 获取趋势数据 */
  getTrend: (params) => request.get('/dashboard/trend', { params }),

  /** 获取优质果园排行 */
  getTopOrchards: () => request.get('/dashboard/top-orchards'),

  /** 获取待办事项 */
  getPendingTodos: () => request.get('/dashboard/todos'),
}
