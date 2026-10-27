import request, { downloadFile } from './request.js'

export const warehouseApi = {
  // Warehouses
  getWarehouses: (params) => request.get('/warehouse/warehouses/list', { params }),
  getWarehouse: (id) => request.get(`/warehouse/warehouses/${id}`),
  createWarehouse: (data) => request.post('/warehouse/warehouses', data),
  updateWarehouse: (id, data) => request.put(`/warehouse/warehouses/${id}`, data),
  deleteWarehouse: (id) => request.delete(`/warehouse/warehouses/${id}`),
  exportWarehouses: () => downloadFile('/api/warehouse/warehouses/export', '仓库列表.csv'),

  // Records
  getRecords: (params) => request.get('/warehouse/records/list', { params }),
  getRecord: (id) => request.get(`/warehouse/records/${id}`),
  createRecord: (data) => request.post('/warehouse/records', data),
  deleteRecord: (id) => request.delete(`/warehouse/records/${id}`),
  exportRecords: () => downloadFile('/api/warehouse/records/export', '出入库记录.csv'),

  // Receipts
  getReceipts: (params) => request.get('/warehouse/receipts/list', { params }),
  getReceipt: (id) => request.get(`/warehouse/receipts/${id}`),
  createReceipt: (data) => request.post('/warehouse/receipts', data),
  updateReceipt: (id, data) => request.put(`/warehouse/receipts/${id}`, data),
  updateReceiptStatus: (id, status) => request.put(`/warehouse/receipts/${id}/status`, null, { params: { status } }),
  deleteReceipt: (id) => request.delete(`/warehouse/receipts/${id}`),
  exportReceipts: () => downloadFile('/api/warehouse/receipts/export', '仓单列表.csv'),

  // Alerts
  getWarehouseAlerts: (params) => request.get('/warehouse/warehouses/alerts', { params }),

  // Statistics
  getStatisticsSummary: () => request.get('/warehouse/statistics/summary'),
  getTurnoverStats: (params) => request.get('/warehouse/statistics/turnover', { params }),
  getLossStats: (params) => request.get('/warehouse/statistics/loss', { params }),
}
