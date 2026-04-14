import request from './request.js'

// ========== 溯源主链路 (TraceController — /api/trace) ==========
export const traceApi = {
  list: (params) => request.get('/trace/list', { params }),
  get: (traceCode) => request.get(`/trace/${traceCode}`),
  scan: (traceCode) => request.get(`/trace/scan/${traceCode}/full`),
  addNode: (data) => request.post('/trace/node', data),
  export: (params) => request.get('/trace/export', { params, responseType: 'blob' }),
}

// ========== 溯源批次 (TraceBatchController — /api/trace/batches) ==========
export const batchApi = {
  list: (params) => request.get('/trace/batches', { params }),
  get: (id) => request.get(`/trace/batches/${id}`),
  create: (data) => request.post('/trace/batches', data),
  updateStatus: (id, status) => request.put(`/trace/batches/${id}/status`, null, { params: { status } }),
  delete: (id) => request.delete(`/trace/batches/${id}`),
  addRecord: (id, data) => request.post(`/trace/batches/${id}/records`, data),
  getRecords: (id, params) => request.get(`/trace/batches/${id}/records`, { params }),
}

// ========== 溯源码 (TraceCodeController — /api/trace/code) ==========
export const codeApi = {
  generateBox: (batchId, boxCount) => request.post('/trace/code/generate-box', null, { params: { batchId, boxCount } }),
  generateFruit: (boxCode, fruitCount) => request.post('/trace/code/generate-fruit', null, { params: { boxCode, fruitCount } }),
  verify: (code) => request.get(`/trace/code/verify/${code}`),
}

// ========== 异常追溯 (AnomalyTraceController — /api/trace/anomaly) ==========
export const anomalyApi = {
  report: (data) => request.post('/trace/anomaly', data),
  list: (params) => request.get('/trace/anomaly', { params }),
  get: (id) => request.get(`/trace/anomaly/${id}`),
  investigate: (id) => request.put(`/trace/anomaly/${id}/investigate`),
  resolve: (id, data) => request.put(`/trace/anomaly/${id}/resolve`, data),
  handle: (id, data) => {
    // Dispatch based on action: investigate vs resolve
    if (data && data.action === 'investigate') {
      return request.put(`/trace/anomaly/${id}/investigate`)
    }
    return request.put(`/trace/anomaly/${id}/resolve`, { rootCause: data.rootCause, resolvedBy: data.resolvedBy })
  },
}
