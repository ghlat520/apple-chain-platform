import request from './request'

export const traceApi = {
  list: (params) => request.get('/trace', { params }),
  getByBatchCode: (batchCode) => request.get(`/trace/${batchCode}`),
  create: (data) => request.post('/trace', data),
  export: () => request.get('/trace/export', { responseType: 'blob' })
}
