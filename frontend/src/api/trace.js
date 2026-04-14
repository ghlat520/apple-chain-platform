import request from './request'

export const traceApi = {
  // Trace chains: GET /api/trace/list, GET /api/trace/{traceCode}
  list: (params) => request.get('/trace/list', { params }),
  getByBatchCode: (traceCode) => request.get(`/trace/${traceCode}`),
  scanByCode: (traceCode) => request.get(`/trace/scan/${traceCode}`),
  getQrCodeUrl: (traceCode, size = 300) => `/api/trace/qrcode/${traceCode}?size=${size}`,
  create: (data) => request.post('/trace', data),
  addNode: (data) => request.post('/trace/node', data),
  export: () => request.get('/trace/export', { responseType: 'blob' }),

  // Trace batches: GET /api/trace/batches
  getBatches: (params) => request.get('/trace/batches', { params }),
  getBatch: (id) => request.get(`/trace/batches/${id}`),
  createBatch: (data) => request.post('/trace/batches', data),
  updateBatchStatus: (id, status) => request.put(`/trace/batches/${id}/status`, null, { params: { status } }),
  deleteBatch: (id) => request.delete(`/trace/batches/${id}`),
  addRecord: (batchId, data) => request.post(`/trace/batches/${batchId}/records`, data),
  listRecords: (batchId) => request.get(`/trace/batches/${batchId}/records`),
  exportBatches: () => request.get('/trace/batches/export', { responseType: 'blob' }),

  // M12 — Three-level trace code (一果一码)
  generateBox: (batchId, boxCount) =>
    request.post('/trace/code/generate-box', null, { params: { batchId, boxCount } }),
  generateFruit: (boxCode, fruitCount) =>
    request.post('/trace/code/generate-fruit', null, { params: { boxCode, fruitCount } }),
  verifyCode: (code) => request.get(`/trace/code/verify/${code}`),
  exportVdp: (batchId, format = 'csv') =>
    request.get(`/trace/code/vdp-export/${batchId}`, { params: { format }, responseType: 'blob' })
}
