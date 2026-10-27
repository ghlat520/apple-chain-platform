/**
 * Cultivation API (batches, operations, growth records).
 * Backend: CultivationController, PlantingRecordController
 */
import { httpGet, httpPost, httpPut, httpDelete } from './request';

// Batches
export function getBatches(params?: Record<string, any>) { return httpGet('/cultivation/batches', { params }); }
export function getBatch(id: number | string) { return httpGet(`/cultivation/batches/${id}`); }
export function createBatch(data: Record<string, any>) { return httpPost('/cultivation/batches', data); }
export function updateBatch(id: number | string, data: Record<string, any>) { return httpPut(`/cultivation/batches/${id}`, data); }
export function deleteBatch(id: number | string) { return httpDelete(`/cultivation/batches/${id}`); }

// Operations
export function getOperations(params?: Record<string, any>) { return httpGet('/cultivation/operations', { params }); }
export function createOperation(data: Record<string, any>) { return httpPost('/cultivation/operations', data); }
export function updateOperation(id: number | string, data: Record<string, any>) { return httpPut(`/cultivation/operations/${id}`, data); }
export function deleteOperation(id: number | string) { return httpDelete(`/cultivation/operations/${id}`); }

// Growth records
export function getGrowthRecords(params?: Record<string, any>) { return httpGet('/planting/record/list', { params }); }
export function getGrowthRecord(id: number | string) { return httpGet(`/planting/record/${id}`); }
export function createGrowthRecord(data: Record<string, any>) { return httpPost('/planting/record', data); }
export function updateGrowthRecord(id: number | string, data: Record<string, any>) { return httpPut(`/planting/record/${id}`, data); }
export function deleteGrowthRecord(id: number | string) { return httpDelete(`/planting/record/${id}`); }

// Export helpers
export function exportBatches() {
  const token = localStorage.getItem('token');
  return fetch('/api/cultivation/batches/export', {
    headers: { Authorization: `Bearer ${token}` }
  }).then(r => { if (!r.ok) throw new Error('导出失败'); return r.blob(); })
    .then(blob => {
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = '种植批次.csv';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(a.href);
    });
}
