import request from './request.js'

// ========== 供应商管理 (SupplierController — /api/input/suppliers) ==========
export const supplierApi = {
  list: (params) => request.get('/input/suppliers/list', { params }),
  get: (id) => request.get(`/input/suppliers/${id}`),
  create: (data) => request.post('/input/suppliers', data),
  update: (id, data) => request.put(`/input/suppliers/${id}`, data),
  delete: (id) => request.delete(`/input/suppliers/${id}`),
  audit: (id, data) => request.put(`/input/suppliers/${id}/audit`, data),
  reinstate: (id) => request.put(`/input/suppliers/${id}/reinstate`),
  export: (params) => request.get('/input/suppliers/export', { params, responseType: 'blob' }),
}

// ========== 农资产品 (AgriProductController — /api/input/products) ==========
export const agriProductApi = {
  list: (params) => request.get('/input/products/list', { params }),
  get: (id) => request.get(`/input/products/${id}`),
  create: (data) => request.post('/input/products', data),
  update: (id, data) => request.put(`/input/products/${id}`, data),
  delete: (id) => request.delete(`/input/products/${id}`),
  export: (params) => request.get('/input/products/export', { params, responseType: 'blob' }),
}

// ========== 采购管理 (PurchaseController — /api/input/purchases) ==========
export const purchaseApi = {
  list: (params) => request.get('/input/purchases/list', { params }),
  get: (id) => request.get(`/input/purchases/${id}`),
  create: (data) => request.post('/input/purchases', data),
  update: (id, data) => request.put(`/input/purchases/${id}`, data),
  delete: (id) => request.delete(`/input/purchases/${id}`),
  approve: (id, data) => request.put(`/input/purchases/${id}/approve`, data),
  receive: (id) => request.put(`/input/purchases/${id}/receive`),
  cancel: (id) => request.put(`/input/purchases/${id}/cancel`),
  export: (params) => request.get('/input/purchases/export', { params, responseType: 'blob' }),
}

// ========== 库存管理 (InventoryController — /api/input/inventory) ==========
export const inventoryApi = {
  list: (params) => request.get('/input/inventory/list', { params }),
  get: (id) => request.get(`/input/inventory/${id}`),
  create: (data) => request.post('/input/inventory', data),
  update: (id, data) => request.put(`/input/inventory/${id}`, data),
  delete: (id) => request.delete(`/input/inventory/${id}`),
  adjust: (id, data) => request.post(`/input/inventory/${id}/adjust`, data),
  alerts: (params) => request.get('/input/inventory/alerts', { params }),
}

// ========== 使用记录 (UsageController — /api/input/usage) ==========
export const usageApi = {
  list: (params) => request.get('/input/usage/list', { params }),
  get: (id) => request.get(`/input/usage/${id}`),
  create: (data) => request.post('/input/usage', data),
  update: (id, data) => request.put(`/input/usage/${id}`, data),
  delete: (id) => request.delete(`/input/usage/${id}`),
  export: (params) => request.get('/input/usage/export', { params, responseType: 'blob' }),
  byBatch: (params) => request.get('/input/usage/by-batch', { params }),
  byTrace: (params) => request.get('/input/usage/by-trace', { params }),
}

// ========== 溯源链路 (TraceController — /api/input/trace) ==========
export const inputTraceApi = {
  query: (traceCode) => request.get(`/input/trace/${traceCode}`),
}
