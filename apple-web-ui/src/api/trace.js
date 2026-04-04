import request, { downloadFile } from './request.js'

export const traceApi = {
  // Trace chains: GET /api/trace/list, GET /api/trace/{traceCode}
  getChains: (params) => request.get('/trace/list', { params }),
  getChain: (traceCode) => request.get(`/trace/${traceCode}`),
  scanByCode: (traceCode) => request.get(`/trace/scan/${traceCode}`),
  getQrCodeUrl: (traceCode, size = 300) => `/api/trace/qrcode/${traceCode}?size=${size}`,
  addNode: (data) => request.post('/trace/node', data),
  exportChains: () => downloadFile('/api/trace/export', '溯源链.csv'),

  // Trace batches: GET /api/trace/batches, POST /api/trace/batches
  getBatches: (params) => request.get('/trace/batches', { params }),
  getBatch: (id) => request.get(`/trace/batches/${id}`),
  createBatch: (data) => request.post('/trace/batches', data),
  updateBatchStatus: (id, status) => request.put(`/trace/batches/${id}/status`, null, { params: { status } }),
  deleteBatch: (id) => request.delete(`/trace/batches/${id}`),
  addRecord: (batchId, data) => request.post(`/trace/batches/${batchId}/records`, data),
  listRecords: (batchId) => request.get(`/trace/batches/${batchId}/records`),
  exportBatches: () => downloadFile('/api/trace/batches/export', '溯源批次.csv')
}
