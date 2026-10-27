import request from './request'

export const warehouseApi = {
  // 仓库管理
  list: (params) => request.get('/warehouse/warehouses/list', { params }),
  getById: (id) => request.get(`/warehouse/warehouses/${id}`),
  create: (data) => request.post('/warehouse/warehouses', data),
  update: (id, data) => request.put(`/warehouse/warehouses/${id}`, data),
  delete: (id) => request.delete(`/warehouse/warehouses/${id}`),
  changeStatus: (id, data) => request.put(`/warehouse/warehouses/${id}/status`, data),
  alerts: () => request.get('/warehouse/warehouses/alerts'),
  export: (params) => request.get('/warehouse/warehouses/export', { params, responseType: 'blob' }),

  // 仓单管理
  receiptList: (params) => request.get('/warehouse/receipts/list', { params }),
  receiptDetail: (id) => request.get(`/warehouse/receipts/${id}`),
  receiptCreate: (data) => request.post('/warehouse/receipts', data),
  receiptUpdate: (id, data) => request.put(`/warehouse/receipts/${id}`, data),
  receiptChangeStatus: (id, data) => request.put(`/warehouse/receipts/${id}/status`, data),
  receiptDelete: (id) => request.delete(`/warehouse/receipts/${id}`),
  receiptExport: (params) => request.get('/warehouse/receipts/export', { params, responseType: 'blob' }),

  // 出入库记录
  recordList: (params) => request.get('/warehouse/records/list', { params }),
  recordDetail: (id) => request.get(`/warehouse/records/${id}`),
  recordCreate: (data) => request.post('/warehouse/records', data),
  recordDelete: (id) => request.delete(`/warehouse/records/${id}`),
  recordExport: (params) => request.get('/warehouse/records/export', { params, responseType: 'blob' }),

  // 统计
  statisticsSummary: () => request.get('/warehouse/statistics/summary'),
  statisticsTurnover: (params) => request.get('/warehouse/statistics/turnover', { params }),
  statisticsLoss: (params) => request.get('/warehouse/statistics/loss', { params })
}
