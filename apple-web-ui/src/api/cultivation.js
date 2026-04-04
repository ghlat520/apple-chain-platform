import request, { downloadFile } from './request.js'

export const cultivationApi = {
  // 种植批次
  getBatches: (params) => request.get('/cultivation/batches', { params }),
  getBatch: (id) => request.get(`/cultivation/batches/${id}`),
  createBatch: (data) => request.post('/cultivation/batches', data),
  updateBatch: (id, data) => request.put(`/cultivation/batches/${id}`, data),
  deleteBatch: (id) => request.delete(`/cultivation/batches/${id}`),
  exportBatches: () => downloadFile('/api/cultivation/batches/export', '种植批次.csv'),

  // 种植作业
  getOperations: (params) => request.get('/cultivation/operations', { params }),
  createOperation: (data) => request.post('/cultivation/operations', data),
  updateOperation: (id, data) => request.put(`/cultivation/operations/${id}`, data),
  deleteOperation: (id) => request.delete(`/cultivation/operations/${id}`),

  // 生长记录
  getGrowthRecords: (params) => request.get('/planting/record/list', { params }),
  getGrowthRecord: (id) => request.get(`/planting/record/${id}`),
  createGrowthRecord: (data) => request.post('/planting/record', data),
  updateGrowthRecord: (id, data) => request.put(`/planting/record/${id}`, data),
  deleteGrowthRecord: (id) => request.delete(`/planting/record/${id}`)
}
