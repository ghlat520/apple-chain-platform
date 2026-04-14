import request, { downloadFile } from './request.js'

/**
 * Planting module API client.
 * Backend: apple-module-planting.
 */
export const plantingApi = {
  // === Harvest Batch (采收批次) ===

  /** GET /api/planting/harvest/list - paginated list */
  getHarvestBatches: (params) => request.get('/planting/harvest/list', { params }),

  /** GET /api/planting/harvest/{id} - batch detail */
  getHarvestBatch: (id) => request.get(`/planting/harvest/${id}`),

  /** POST /api/planting/harvest - create batch */
  createHarvestBatch: (data) => request.post('/planting/harvest', data),

  /** PUT /api/planting/harvest/{id} - update batch */
  updateHarvestBatch: (id, data) => request.put(`/planting/harvest/${id}`, data),

  /** PUT /api/planting/harvest/{id}/confirm - confirm harvest (generate trace code) */
  confirmHarvestBatch: (id) => request.put(`/planting/harvest/${id}/confirm`),

  /** DELETE /api/planting/harvest/{id} - soft delete */
  deleteHarvestBatch: (id) => request.delete(`/planting/harvest/${id}`),

  /** GET /api/planting/harvest/export - export CSV */
  exportHarvestBatches: () => downloadFile('/api/planting/harvest/export', '采收批次.csv'),

  // === AI 作业计划 (TaskPlan) ===

  /** POST /api/planting/task-plan/generate?orchardId=&months=1 */
  generateTaskPlan: (orchardId, months = 1) =>
    request.post('/planting/task-plan/generate', null, { params: { orchardId, months } }),

  /** GET /api/planting/task-plan/list?orchardId=&from=...&to=... */
  getTaskPlans: (params) =>
    request.get('/planting/task-plan/list', { params }),

  /** POST /api/planting/task-plan/{id}/done?actualOperationId= */
  markTaskDone: (id, actualOperationId) =>
    request.post(`/planting/task-plan/${id}/done`, null, { params: { actualOperationId } }),

  /** POST /api/planting/task-plan/{id}/skip?reason= */
  skipTask: (id, reason) =>
    request.post(`/planting/task-plan/${id}/skip`, null, { params: { reason } }),

  // === 种植分析 (PlantingAnalysisController) ===

  /** GET /api/planting/analysis/yield?variety=&year= */
  getYieldRanking: (params) => request.get('/planting/analysis/yield', { params }),

  /** GET /api/planting/analysis/premium?variety=&year= */
  getPremiumRates: (params) => request.get('/planting/analysis/premium', { params }),

  /** GET /api/planting/analysis/pest?year= */
  getPestIncidences: (params) => request.get('/planting/analysis/pest', { params }),

  /** GET /api/planting/analysis/compare?orchardIds=1,2,3 */
  comparePlots: (orchardIds) => request.get('/planting/analysis/compare', { params: { orchardIds } }),
}
