import request from './request.js'

// ========== 数据资产目录 (DataAssetController — /api/bigdata/asset) ==========
export const assetApi = {
  list: (params) => request.get('/bigdata/asset', { params }),
  get: (id) => request.get(`/bigdata/asset/${id}`),
  create: (data) => request.post('/bigdata/asset', data),
  update: (id, data) => request.put(`/bigdata/asset/${id}`, data),
  delete: (id) => request.delete(`/bigdata/asset/${id}`),
  listFields: (id) => request.get(`/bigdata/asset/${id}/fields`),
  createField: (id, data) => request.post(`/bigdata/asset/${id}/fields`, data),
  deleteField: (fieldId) => request.delete(`/bigdata/asset/fields/${fieldId}`),
}

// ========== 数据源注册 (DataSourceController — /api/bigdata/source) ==========
export const sourceApi = {
  list: (params) => request.get('/bigdata/source', { params }),
  get: (id) => request.get(`/bigdata/source/${id}`),
  create: (data) => request.post('/bigdata/source', data),
  update: (id, data) => request.put(`/bigdata/source/${id}`, data),
  delete: (id) => request.delete(`/bigdata/source/${id}`),
  test: (id) => request.post(`/bigdata/source/${id}/test`),
}

// ========== 数据质量 (DqRuleController — /api/bigdata/dq/rules) ==========
export const dqApi = {
  list: (params) => request.get('/bigdata/dq/rules', { params }),
  get: (id) => request.get(`/bigdata/dq/rules/${id}`),
  create: (data) => request.post('/bigdata/dq/rules', data),
  update: (id, data) => request.put(`/bigdata/dq/rules/${id}`, data),
  delete: (id) => request.delete(`/bigdata/dq/rules/${id}`),
  check: (id) => request.post(`/bigdata/dq/rules/${id}/check`),
  results: (id, params) => request.get(`/bigdata/dq/rules/${id}/results`, { params }),
}

// ========== 采集任务 (CollectJobController — /api/bigdata/job) ==========
export const jobApi = {
  list: (params) => request.get('/bigdata/job', { params }),
  get: (id) => request.get(`/bigdata/job/${id}`),
  create: (data) => request.post('/bigdata/job', data),
  update: (id, data) => request.put(`/bigdata/job/${id}`, data),
  delete: (id) => request.delete(`/bigdata/job/${id}`),
  trigger: (id) => request.post(`/bigdata/job/${id}/trigger`),
  runs: (id, params) => request.get(`/bigdata/job/${id}/runs`, { params }),
}

// ========== 数据血缘 (DataLineageController — /api/bigdata/lineage) ==========
export const lineageApi = {
  list: (params) => request.get('/bigdata/lineage', { params }),
  create: (data) => request.post('/bigdata/lineage', data),
  delete: (id) => request.delete(`/bigdata/lineage/${id}`),
  downstream: (params) => request.get('/bigdata/lineage/downstream', { params }),
  upstream: (params) => request.get('/bigdata/lineage/upstream', { params }),
}

// ========== 指标中心 (MetricController — /api/bigdata/metric) ==========
export const metricApi = {
  list: (params) => request.get('/bigdata/metric', { params }),
  get: (id) => request.get(`/bigdata/metric/${id}`),
  create: (data) => request.post('/bigdata/metric', data),
  update: (id, data) => request.put(`/bigdata/metric/${id}`, data),
  delete: (id) => request.delete(`/bigdata/metric/${id}`),
  values: (code, params) => request.get(`/bigdata/metric/${code}/values`, { params }),
  createValue: (data) => request.post('/bigdata/metric/values', data),
}

// ========== 分析报告 (AnalysisReportController — /api/bigdata/report) ==========
export const reportApi = {
  list: (params) => request.get('/bigdata/report', { params }),
  get: (id) => request.get(`/bigdata/report/${id}`),
  generate: (params) => request.post('/bigdata/report/generate', null, { params }),
  publish: (id) => request.put(`/bigdata/report/${id}/publish`),
}

// ========== 审计日志 (AuditLogQueryController — /api/bigdata/audit) ==========
export const auditApi = {
  list: (params) => request.get('/bigdata/audit', { params }),
}

// ========== 开放 API (OpenApiController — /api/bigdata/openapi/clients) ==========
export const openApi = {
  list: (params) => request.get('/bigdata/openapi/clients', { params }),
  register: (data) => request.post('/bigdata/openapi/clients', data),
  updateStatus: (id, status) => request.put(`/bigdata/openapi/clients/${id}/status`, null, { params: { status } }),
  logs: (id, params) => request.get(`/bigdata/openapi/clients/${id}/logs`, { params }),
  rotateKey: (id) => request.post(`/bigdata/openapi/clients/${id}/rotate-key`),
}

// ========== 大屏配置 (DashboardConfigController — /api/bigdata/screen) ==========
export const screenApi = {
  list: (params) => request.get('/bigdata/screen', { params }),
  get: (id) => request.get(`/bigdata/screen/${id}`),
  create: (data) => request.post('/bigdata/screen', data),
  update: (id, data) => request.put(`/bigdata/screen/${id}`, data),
  delete: (id) => request.delete(`/bigdata/screen/${id}`),
  publish: (id) => request.post(`/bigdata/screen/${id}/publish`),
  widgets: (id) => request.get(`/bigdata/screen/${id}/widgets`),
  createWidget: (id, data) => request.post(`/bigdata/screen/${id}/widgets`, data),
  deleteWidget: (widgetId) => request.delete(`/bigdata/screen/widgets/${widgetId}`),
}

// ========== Dashboard 大屏数据 ==========
export const dashboardApi = {
  getDashboardStats: () => request.get('/bigdata/dashboard/stats'),
  getHarvestTrend: (params) => request.get('/bigdata/dashboard/harvest-trend', { params }),
  getTradeTrend: (params) => request.get('/bigdata/dashboard/trade-trend', { params }),
  getVarietyDistribution: () => request.get('/bigdata/dashboard/variety-distribution'),
  getTopOrchards: (params) => request.get('/bigdata/dashboard/top-orchards', { params }),
  getSupplyDemand: () => request.get('/bigdata/dashboard/supply-demand'),
  getTraceStats: () => request.get('/bigdata/dashboard/trace-stats'),
  getWarehouseStock: () => request.get('/bigdata/dashboard/warehouse-stock'),
  getFinanceScale: () => request.get('/bigdata/dashboard/finance-scale'),
  getPriceTrend: (params) => request.get('/bigdata/dashboard/price-trend', { params }),
  getLossRate: (params) => request.get('/bigdata/dashboard/loss-rate', { params }),
}
