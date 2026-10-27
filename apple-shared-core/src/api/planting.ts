/**
 * Planting API client.
 *
 * Backend sources (verified):
 * - apple-module-planting/controller/OrchardController.java      → /api/planting/orchard/*
 * - apple-module-planting/controller/GrowthRecordController.java → /api/planting/record/*
 */
import { httpGet, httpPost, httpPut, httpDelete } from './request';
import type { PageResult } from '../types/common';
import type { Orchard, GrowthRecord } from '../types/planting';

// ---------- Orchard ----------

export interface OrchardListParams {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;
  farmerId?: number;
}

/** GET /api/planting/orchard/list */
export function findOrchards(
  params: OrchardListParams = {}
): Promise<PageResult<Orchard>> {
  return httpGet<PageResult<Orchard>>('/planting/orchard/list', { params });
}

/** GET /api/planting/orchard/{id} */
export function getOrchard(id: number): Promise<Orchard> {
  return httpGet<Orchard>(`/planting/orchard/${id}`);
}

// ---------- GrowthRecord ----------

export interface GrowthRecordListParams {
  page?: number;
  size?: number;
  orchardId?: number;
  recordType?: string;
}

/** GET /api/planting/record/list */
export function findGrowthRecords(
  params: GrowthRecordListParams = {}
): Promise<PageResult<GrowthRecord>> {
  return httpGet<PageResult<GrowthRecord>>('/planting/record/list', { params });
}

/** GET /api/planting/record/{id} */
export function getGrowthRecord(id: number): Promise<GrowthRecord> {
  return httpGet<GrowthRecord>(`/planting/record/${id}`);
}

/**
 * POST /api/planting/record
 * Backend accepts @RequestBody GrowthRecord (JSON).
 * Required fields: orchardId, recordType, operateDate. (No backend validation on operator etc.)
 */
export function uploadGrowthRecord(
  record: GrowthRecord
): Promise<GrowthRecord> {
  return httpPost<GrowthRecord>('/planting/record', record);
}

/** PUT /api/planting/record/{id} */
export function updateGrowthRecord(
  id: number,
  record: GrowthRecord
): Promise<GrowthRecord> {
  return httpPut<GrowthRecord>(`/planting/record/${id}`, record);
}

/** DELETE /api/planting/record/{id} */
export function deleteGrowthRecord(id: number): Promise<void> {
  return httpDelete<void>(`/planting/record/${id}`);
}

// ---------- Harvest Batch ----------

export function getHarvestBatches(params?: Record<string, any>) { return httpGet('/planting/harvest/list', { params }); }
export function getHarvestBatch(id: number | string) { return httpGet(`/planting/harvest/${id}`); }
export function createHarvestBatch(data: Record<string, any>) { return httpPost('/planting/harvest', data); }
export function updateHarvestBatch(id: number | string, data: Record<string, any>) { return httpPut(`/planting/harvest/${id}`, data); }
export function confirmHarvestBatch(id: number | string) { return httpPut(`/planting/harvest/${id}/confirm`); }
export function deleteHarvestBatch(id: number | string) { return httpDelete(`/planting/harvest/${id}`); }

export function exportHarvestBatches() {
  const token = localStorage.getItem('token');
  return fetch('/api/planting/harvest/export', {
    headers: { Authorization: `Bearer ${token}` }
  }).then(r => { if (!r.ok) throw new Error('导出失败'); return r.blob(); })
    .then(blob => {
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = '采收批次.csv';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(a.href);
    });
}

// ---------- Task Plan (AI) ----------

export function generateTaskPlan(orchardId: number | string, months = 1) {
  return httpPost('/planting/task-plan/generate', null, { params: { orchardId, months } });
}
export function getTaskPlans(params?: Record<string, any>) { return httpGet('/planting/task-plan/list', { params }); }
export function markTaskDone(id: number | string, actualOperationId?: number | string) {
  return httpPost(`/planting/task-plan/${id}/done`, null, { params: { actualOperationId } });
}
export function skipTask(id: number | string, reason: string) {
  return httpPost(`/planting/task-plan/${id}/skip`, null, { params: { reason } });
}

// ---------- Planting Analysis ----------

export function getYieldRanking(params?: Record<string, any>) { return httpGet('/planting/analysis/yield', { params }); }
export function getPremiumRates(params?: Record<string, any>) { return httpGet('/planting/analysis/premium', { params }); }
export function getPestIncidences(params?: Record<string, any>) { return httpGet('/planting/analysis/pest', { params }); }
export function comparePlots(orchardIds: string) { return httpGet('/planting/analysis/compare', { params: { orchardIds } }); }
