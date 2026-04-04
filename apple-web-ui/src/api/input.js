import request, { downloadFile } from './request.js'

export const inputApi = {
  // Products
  getProducts: (params) => request.get('/input/products/list', { params }),
  getProduct: (id) => request.get(`/input/products/${id}`),
  createProduct: (data) => request.post('/input/products', data),
  updateProduct: (id, data) => request.put(`/input/products/${id}`, data),
  deleteProduct: (id) => request.delete(`/input/products/${id}`),
  exportProducts: () => downloadFile('/api/input/products/export', '农资产品.csv'),

  // Suppliers
  getSuppliers: (params) => request.get('/input/suppliers/list', { params }),
  getSupplier: (id) => request.get(`/input/suppliers/${id}`),
  createSupplier: (data) => request.post('/input/suppliers', data),
  updateSupplier: (id, data) => request.put(`/input/suppliers/${id}`, data),
  deleteSupplier: (id) => request.delete(`/input/suppliers/${id}`),
  exportSuppliers: () => downloadFile('/api/input/suppliers/export', '农资供应商.csv'),

  // Purchases
  getPurchases: (params) => request.get('/input/purchases/list', { params }),
  getPurchase: (id) => request.get(`/input/purchases/${id}`),
  createPurchase: (data) => request.post('/input/purchases', data),
  updatePurchase: (id, data) => request.put(`/input/purchases/${id}`, data),
  deletePurchase: (id) => request.delete(`/input/purchases/${id}`),
  exportPurchases: () => downloadFile('/api/input/purchases/export', '农资采购.csv'),

  // Inventory
  getInventory: (params) => request.get('/input/inventory/list', { params }),
  getInventoryItem: (id) => request.get(`/input/inventory/${id}`),
  createInventory: (data) => request.post('/input/inventory', data),
  updateInventory: (id, data) => request.put(`/input/inventory/${id}`, data),
  deleteInventory: (id) => request.delete(`/input/inventory/${id}`),
  exportInventory: () => downloadFile('/api/input/inventory/export', '农资库存.csv'),
  getInventoryAlerts: () => request.get('/input/inventory/alerts'),

  // Usage
  getUsageList: (params) => request.get('/input/usage/list', { params }),
  getUsage: (id) => request.get(`/input/usage/${id}`),
  createUsage: (data) => request.post('/input/usage', data),
  updateUsage: (id, data) => request.put(`/input/usage/${id}`, data),
  deleteUsage: (id) => request.delete(`/input/usage/${id}`),
  getUsageByBatch: (batchId) => request.get('/input/usage/by-batch', { params: { batchId } }),
  getUsageByTrace: (traceCode) => request.get('/input/usage/by-trace', { params: { traceCode } }),
  exportUsage: () => downloadFile('/api/input/usage/export', '农资使用记录.csv'),
}
