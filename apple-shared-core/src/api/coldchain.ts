import { httpGet, httpPost, httpPut, httpDelete } from './request';

export function getVehicles(params?: Record<string, any>) { return httpGet('/coldchain/vehicles/list'); }
export function getVehicle(id: number | string) { return httpGet(`/coldchain/vehicles/${id}`); }
export function createVehicle(data: Record<string, any>) { return httpPost('/coldchain/vehicles', data); }
export function updateVehicle(id: number | string, data: Record<string, any>) { return httpPut(`/coldchain/vehicles/${id}`, data); }
export function deleteVehicle(id: number | string) { return httpDelete(`/coldchain/vehicles/${id}`); }
export function getTasks(params?: Record<string, any>) { return httpGet('/coldchain/tasks/list'); }
export function getTask(id: number | string) { return httpGet(`/coldchain/tasks/${id}`); }
export function createTask(data: Record<string, any>) { return httpPost('/coldchain/tasks', data); }
export function updateTask(id: number | string, data: Record<string, any>) { return httpPut(`/coldchain/tasks/${id}`, data); }
export function departTask(id: number | string) { return httpPost(`/coldchain/tasks/${id}/depart`); }
export function deliverTask(id: number | string) { return httpPost(`/coldchain/tasks/${id}/deliver`); }
export function deleteTask(id: number | string) { return httpDelete(`/coldchain/tasks/${id}`); }
export function getTemperatures(params?: Record<string, any>) { return httpGet('/coldchain/temperatures/list'); }
export function getTemperaturesByTask(taskId: number | string) { return httpGet('/coldchain/temperatures/by-task'); }
export function createTemperature(data: Record<string, any>) { return httpPost('/coldchain/temperatures', data); }
export function deleteTemperature(id: number | string) { return httpDelete(`/coldchain/temperatures/${id}`); }
export function getDeliveries(params?: Record<string, any>) { return httpGet('/coldchain/deliveries/list'); }
export function getDelivery(id: number | string) { return httpGet(`/coldchain/deliveries/${id}`); }
export function createDelivery(data: Record<string, any>) { return httpPost('/coldchain/deliveries', data); }
export function updateDelivery(id: number | string, data: Record<string, any>) { return httpPut(`/coldchain/deliveries/${id}`, data); }
export function signDelivery(id: number | string, data: Record<string, any>) { return httpPost(`/coldchain/deliveries/${id}/sign`, data); }
export function deleteDelivery(id: number | string) { return httpDelete(`/coldchain/deliveries/${id}`); }
export function getPrecooling(params?: Record<string, any>) { return httpGet('/coldchain/precool'); }
export function getPrecoolTask(id: number | string) { return httpGet(`/coldchain/precool/${id}`); }
export function createPrecooling(data: Record<string, any>) { return httpPost('/coldchain/precool', data); }
export function startPrecooling(id: number | string) { return httpPut(`/coldchain/precool/${id}/start`); }
export function completePrecooling(id: number | string) { return httpPut(`/coldchain/precool/${id}/complete`); }
export function getStatisticsSummary() { return httpGet('/coldchain/statistics/summary'); }
export function getTransportTrend(params?: Record<string, any>) { return httpGet('/coldchain/statistics/transport-trend'); }
export function getColdchainAlarms(days = 30) { return httpGet('/coldchain/statistics/alarms'); }
export function changeVehicleStatus(id: number | string, status?: Record<string, any>) { return httpPut(`/coldchain/vehicles/${id}/status`); }
export function exportVehicles() { const t=localStorage.getItem('token');return fetch('/api/coldchain/vehicles/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='车辆列表.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }
export function exportTasks() { const t=localStorage.getItem('token');return fetch('/api/coldchain/tasks/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='运输任务.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }
export function exportDeliveries() { const t=localStorage.getItem('token');return fetch('/api/coldchain/deliveries/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='配送记录.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }