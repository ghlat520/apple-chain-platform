import request from './request'

export const inputApi = {
  // ---- 供应商 ----
  supplierList: (params) => request.get('/input/suppliers/list', { params }),
  supplierDetail: (id) => request.get(`/input/suppliers/${id}`),
  supplierCreate: (data) => request.post('/input/suppliers', data),
  supplierUpdate: (id, data) => request.put(`/input/suppliers/${id}`, data),
  supplierDelete: (id) => request.delete(`/input/suppliers/${id}`),
  supplierAudit: (id, data) => request.put(`/input/suppliers/${id}/audit`, data),
  supplierReinstate: (id) => request.put(`/input/suppliers/${id}/reinstate`),
  supplierExport: (params) => request.get('/input/suppliers/export', { params, responseType: 'blob' }),

  // ---- 农资产品 ----
  productList: (params) => request.get('/input/products/list', { params }),
  productDetail: (id) => request.get(`/input/products/${id}`),
  productCreate: (data) => request.post('/input/products', data),
  productUpdate: (id, data) => request.put(`/input/products/${id}`, data),
  productDelete: (id) => request.delete(`/input/products/${id}`),
  productExport: (params) => request.get('/input/products/export', { params, responseType: 'blob' }),

  // ---- 采购 ----
  purchaseList: (params) => request.get('/input/purchases/list', { params }),
  purchaseDetail: (id) => request.get(`/input/purchases/${id}`),
  purchaseCreate: (data) => request.post('/input/purchases', data),
  purchaseUpdate: (id, data) => request.put(`/input/purchases/${id}`, data),
  purchaseDelete: (id) => request.delete(`/input/purchases/${id}`),
  purchaseApprove: (id) => request.put(`/input/purchases/${id}/approve`),
  purchaseReceive: (id) => request.put(`/input/purchases/${id}/receive`),
  purchaseCancel: (id) => request.put(`/input/purchases/${id}/cancel`),
  purchaseExport: (params) => request.get('/input/purchases/export', { params, responseType: 'blob' }),

  // ---- 库存 ----
  inventoryList: (params) => request.get('/input/inventory/list', { params }),
  inventoryDetail: (id) => request.get(`/input/inventory/${id}`),
  inventoryCreate: (data) => request.post('/input/inventory', data),
  inventoryUpdate: (id, data) => request.put(`/input/inventory/${id}`, data),
  inventoryDelete: (id) => request.delete(`/input/inventory/${id}`),
  inventoryAdjust: (id, data) => request.post(`/input/inventory/${id}/adjust`, data),
  inventoryAlerts: () => request.get('/input/inventory/alerts'),
  inventoryExport: (params) => request.get('/input/inventory/export', { params, responseType: 'blob' }),

  // ---- 使用记录 ----
  usageList: (params) => request.get('/input/usage/list', { params }),
  usageDetail: (id) => request.get(`/input/usage/${id}`),
  usageCreate: (data) => request.post('/input/usage', data),
  usageUpdate: (id, data) => request.put(`/input/usage/${id}`, data),
  usageDelete: (id) => request.delete(`/input/usage/${id}`),
  usageExport: (params) => request.get('/input/usage/export', { params, responseType: 'blob' }),
  usageByBatch: (batchId) => request.get('/input/usage/by-batch', { params: { batchId } }),
  usageByTrace: (traceCode) => request.get('/input/usage/by-trace', { params: { traceCode } }),

  // ---- 溯源链路 ----
  traceChain: (traceCode) => request.get(`/input/trace/${traceCode}`)
}
