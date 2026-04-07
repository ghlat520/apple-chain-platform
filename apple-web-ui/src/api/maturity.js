import request from './request.js'

/**
 * M6 — Maturity / harvest recommendation API client.
 * Backend: apple-module-planting MaturityController.
 */
export const maturityApi = {
  /** POST /api/planting/maturity/record */
  recordMeasurement: (data) => request.post('/planting/maturity/record', data),

  /** GET /api/planting/maturity/recommend/{orchardId} */
  recommend: (orchardId) => request.get(`/planting/maturity/recommend/${orchardId}`),

  /** GET /api/planting/maturity/standards */
  listStandards: () => request.get('/planting/maturity/standards'),

  /** GET /api/planting/maturity/records/{orchardId} */
  listOrchardRecords: (orchardId, limit = 20) =>
    request.get(`/planting/maturity/records/${orchardId}`, { params: { limit } })
}
