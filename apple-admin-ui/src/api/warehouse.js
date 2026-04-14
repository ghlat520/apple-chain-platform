import request from './request.js'

// ========== 仓库管理 (WarehouseController — /api/warehouse/warehouses) ==========
export const warehouseApi = {
  list: (params) => request.get('/warehouse/warehouses/list', { params }),
  get: (id) => request.get(`/warehouse/warehouses/${id}`),
  create: (data) => request.post('/warehouse/warehouses', data),
  update: (id, data) => request.put(`/warehouse/warehouses/${id}`, data),
  delete: (id) => request.delete(`/warehouse/warehouses/${id}`),
  updateStatus: (id, data) => request.put(`/warehouse/warehouses/${id}/status`, data),
  alerts: (params) => request.get('/warehouse/warehouses/alerts', { params }),
}

// ========== 仓单管理 (ReceiptController — /api/warehouse/receipts) ==========
export const receiptApi = {
  list: (params) => request.get('/warehouse/receipts/list', { params }),
  get: (id) => request.get(`/warehouse/receipts/${id}`),
  create: (data) => request.post('/warehouse/receipts', data),
  update: (id, data) => request.put(`/warehouse/receipts/${id}`, data),
  updateStatus: (id, data) => request.put(`/warehouse/receipts/${id}/status`, data),
  delete: (id) => request.delete(`/warehouse/receipts/${id}`),
}

// ========== 出入库记录 (RecordController — /api/warehouse/records) ==========
export const recordApi = {
  list: (params) => request.get('/warehouse/records/list', { params }),
  get: (id) => request.get(`/warehouse/records/${id}`),
  create: (data) => request.post('/warehouse/records', data),
  delete: (id) => request.delete(`/warehouse/records/${id}`),
}

// ========== 仓储统计 (WarehouseStatsController — /api/warehouse/statistics) ==========
export const warehouseStatsApi = {
  summary: () => request.get('/warehouse/statistics/summary'),
  turnover: (params) => request.get('/warehouse/statistics/turnover', { params }),
  loss: (params) => request.get('/warehouse/statistics/loss', { params }),
}
