import { httpGet, httpPost, httpPut, httpDelete } from './request';

export function getProducts(params?: Record<string, any>) { return httpGet('/input/products/list'); }
export function getProduct(id: number | string) { return httpGet(`/input/products/${id}`); }
export function createProduct(data: Record<string, any>) { return httpPost('/input/products', data); }
export function updateProduct(id: number | string, data: Record<string, any>) { return httpPut(`/input/products/${id}`, data); }
export function deleteProduct(id: number | string) { return httpDelete(`/input/products/${id}`); }
export function getSuppliers(params?: Record<string, any>) { return httpGet('/input/suppliers/list'); }
export function getSupplier(id: number | string) { return httpGet(`/input/suppliers/${id}`); }
export function createSupplier(data: Record<string, any>) { return httpPost('/input/suppliers', data); }
export function updateSupplier(id: number | string, data: Record<string, any>) { return httpPut(`/input/suppliers/${id}`, data); }
export function deleteSupplier(id: number | string) { return httpDelete(`/input/suppliers/${id}`); }
export function getPurchases(params?: Record<string, any>) { return httpGet('/input/purchases/list'); }
export function getPurchase(id: number | string) { return httpGet(`/input/purchases/${id}`); }
export function createPurchase(data: Record<string, any>) { return httpPost('/input/purchases', data); }
export function updatePurchase(id: number | string, data: Record<string, any>) { return httpPut(`/input/purchases/${id}`, data); }
export function deletePurchase(id: number | string) { return httpDelete(`/input/purchases/${id}`); }
export function getInventory(params?: Record<string, any>) { return httpGet('/input/inventory/list'); }
export function getInventoryItem(id: number | string) { return httpGet(`/input/inventory/${id}`); }
export function createInventory(data: Record<string, any>) { return httpPost('/input/inventory', data); }
export function updateInventory(id: number | string, data: Record<string, any>) { return httpPut(`/input/inventory/${id}`, data); }
export function deleteInventory(id: number | string) { return httpDelete(`/input/inventory/${id}`); }
export function getInventoryAlerts() { return httpGet('/input/inventory/alerts'); }
export function getUsageList(params?: Record<string, any>) { return httpGet('/input/usage/list'); }
export function getUsage(id: number | string) { return httpGet(`/input/usage/${id}`); }
export function createUsage(data: Record<string, any>) { return httpPost('/input/usage', data); }
export function updateUsage(id: number | string, data: Record<string, any>) { return httpPut(`/input/usage/${id}`, data); }
export function deleteUsage(id: number | string) { return httpDelete(`/input/usage/${id}`); }
export function getUsageByBatch(batchId: number | string) { return httpGet('/input/usage/by-batch'); }
export function getUsageByTrace(traceCode?: Record<string, any>) { return httpGet('/input/usage/by-trace'); }
export function exportProducts() { const t=localStorage.getItem('token');return fetch('/api/input/products/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='农资产品.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }
export function exportSuppliers() { const t=localStorage.getItem('token');return fetch('/api/input/suppliers/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='农资供应商.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }
export function exportPurchases() { const t=localStorage.getItem('token');return fetch('/api/input/purchases/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='农资采购.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }
export function exportInventory() { const t=localStorage.getItem('token');return fetch('/api/input/inventory/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='农资库存.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }
export function exportUsage() { const t=localStorage.getItem('token');return fetch('/api/input/usage/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='农资使用记录.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }