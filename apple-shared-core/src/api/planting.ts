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
