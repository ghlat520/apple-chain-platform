import { httpGet, httpPost, httpPut, httpDelete } from './request';

export function getWarehouses(params?: Record<string, any>) { return httpGet('/warehouse/warehouses/list'); }
export function getWarehouse(id: number | string) { return httpGet(`/warehouse/warehouses/${id}`); }
export function createWarehouse(data: Record<string, any>) { return httpPost('/warehouse/warehouses', data); }
export function updateWarehouse(id: number | string, data: Record<string, any>) { return httpPut(`/warehouse/warehouses/${id}`, data); }
export function deleteWarehouse(id: number | string) { return httpDelete(`/warehouse/warehouses/${id}`); }
export function getRecords(params?: Record<string, any>) { return httpGet('/warehouse/records/list'); }
export function getRecord(id: number | string) { return httpGet(`/warehouse/records/${id}`); }
export function createRecord(data: Record<string, any>) { return httpPost('/warehouse/records', data); }
export function deleteRecord(id: number | string) { return httpDelete(`/warehouse/records/${id}`); }
export function getReceipts(params?: Record<string, any>) { return httpGet('/warehouse/receipts/list'); }
export function getReceipt(id: number | string) { return httpGet(`/warehouse/receipts/${id}`); }
export function createReceipt(data: Record<string, any>) { return httpPost('/warehouse/receipts', data); }
export function updateReceipt(id: number | string, data: Record<string, any>) { return httpPut(`/warehouse/receipts/${id}`, data); }
export function updateReceiptStatus(id: number | string, status?: Record<string, any>) { return httpPut(`/warehouse/receipts/${id}/status`, null, { params: { id, status } } as any); }
export function deleteReceipt(id: number | string) { return httpDelete(`/warehouse/receipts/${id}`); }
export function getWarehouseAlerts(params?: Record<string, any>) { return httpGet('/warehouse/warehouses/alerts'); }
export function getStatisticsSummary() { return httpGet('/warehouse/statistics/summary'); }
export function getTurnoverStats(params?: Record<string, any>) { return httpGet('/warehouse/statistics/turnover'); }
export function getLossStats(params?: Record<string, any>) { return httpGet('/warehouse/statistics/loss'); }
export function exportWarehouses() { const t=localStorage.getItem('token');return fetch('/api/warehouse/warehouses/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='仓库列表.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }
export function exportRecords() { const t=localStorage.getItem('token');return fetch('/api/warehouse/records/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='出入库记录.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }
export function exportReceipts() { const t=localStorage.getItem('token');return fetch('/api/warehouse/receipts/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='仓单列表.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }