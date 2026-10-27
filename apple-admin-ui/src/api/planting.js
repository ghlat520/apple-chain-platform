import request from './request.js'

// OrchardController — /api/planting/orchard
export const orchardApi = {
  list: (params) => request.get('/planting/orchard/list', { params }),
  get: (id) => request.get(`/planting/orchard/${id}`),
  create: (data) => request.post('/planting/orchard', data),
  update: (id, data) => request.put(`/planting/orchard/${id}`, data),
  delete: (id) => request.delete(`/planting/orchard/${id}`),
  export: (params) => request.get('/planting/orchard/export', { params, responseType: 'blob' }),
}

// FarmerController — /api/farm/farmers
export const farmerApi = {
  list: (params) => request.get('/farm/farmers', { params }),
  get: (id) => request.get(`/farm/farmers/${id}`),
  create: (data) => request.post('/farm/farmers', data),
  update: (id, data) => request.put(`/farm/farmers/${id}`, data),
  delete: (id) => request.delete(`/farm/farmers/${id}`),
}

// HarvestBatchController — /api/planting/harvest
export const harvestApi = {
  list: (params) => request.get('/planting/harvest/list', { params }),
  get: (id) => request.get(`/planting/harvest/${id}`),
  create: (data) => request.post('/planting/harvest', data),
  update: (id, data) => request.put(`/planting/harvest/${id}`, data),
  confirm: (id) => request.put(`/planting/harvest/${id}/confirm`),
  delete: (id) => request.delete(`/planting/harvest/${id}`),
}

// CultivationBatchController — /api/cultivation/batches + /api/cultivation/operations
export const cultivationApi = {
  listBatches: (params) => request.get('/cultivation/batches', { params }),
  getBatch: (id) => request.get(`/cultivation/batches/${id}`),
  createBatch: (data) => request.post('/cultivation/batches', data),
  updateBatch: (id, data) => request.put(`/cultivation/batches/${id}`, data),
  deleteBatch: (id) => request.delete(`/cultivation/batches/${id}`),
  listOps: (params) => request.get('/cultivation/operations', { params }),
  createOp: (data) => request.post('/cultivation/operations', data),
  updateOp: (id, data) => request.put(`/cultivation/operations/${id}`, data),
  deleteOp: (id) => request.delete(`/cultivation/operations/${id}`),
}

// MaturityController — /api/planting/maturity
export const maturityApi = {
  record: (data) => request.post('/planting/maturity/record', data),
  recommend: (orchardId) => request.get(`/planting/maturity/recommend/${orchardId}`),
  standards: (params) => request.get('/planting/maturity/standards', { params }),
  records: (orchardId, params) => request.get(`/planting/maturity/records/${orchardId}`, { params }),
}

// TaskPlanController — /api/planting/task-plan
export const taskPlanApi = {
  list: (params) => request.get('/planting/task-plan/list', { params }),
  generate: (data, config) => request.post('/planting/task-plan/generate', data, config),
  done: (id) => request.post(`/planting/task-plan/${id}/done`),
  skip: (id) => request.post(`/planting/task-plan/${id}/skip`),
}

// GrowthRecordController — /api/planting/record
export const growthRecordApi = {
  list: (params) => request.get('/planting/record/list', { params }),
  get: (id) => request.get(`/planting/record/${id}`),
  create: (data) => request.post('/planting/record', data),
  update: (id, data) => request.put(`/planting/record/${id}`, data),
  delete: (id) => request.delete(`/planting/record/${id}`),
}

// PlantingAnalysisController — /api/planting/analysis
export const plantingAnalysisApi = {
  yield: (params) => request.get('/planting/analysis/yield', { params }),
  premium: (params) => request.get('/planting/analysis/premium', { params }),
  pest: (params) => request.get('/planting/analysis/pest', { params }),
  compare: (params) => request.get('/planting/analysis/compare', { params }),
  // compare 需要传 orchardIds 参数: { orchardIds: '1,2,3' }
}
