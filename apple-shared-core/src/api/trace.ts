/**
 * Trace API client.
 *
 * Backend sources (verified):
 * - apple-module-trace/controller/TraceController.java      → /api/trace/*
 * - apple-module-trace/controller/TraceBatchController.java → /api/trace/batches/*
 */
import { httpGet } from './request';
import type { TraceBatch, TraceFullChain } from '../types/trace';

/**
 * GET /api/trace/scan/{traceCode}    — public, no auth required.
 * Returns Map<String, Object> per TraceController.java line 51-54.
 */
export function findTraceByCode(
  traceCode: string
): Promise<Record<string, unknown>> {
  return httpGet<Record<string, unknown>>(
    `/trace/scan/${encodeURIComponent(traceCode)}`
  );
}

/**
 * GET /api/trace/scan/{traceCode}/full — public full-chain aggregation.
 * Returns TraceFullChainVO.
 */
export function findTraceFullChain(
  traceCode: string
): Promise<TraceFullChain> {
  return httpGet<TraceFullChain>(
    `/trace/scan/${encodeURIComponent(traceCode)}/full`
  );
}

/** GET /api/trace/batches/scan/{batchCode} — batch-level public scan */
export function scanBatch(
  batchCode: string
): Promise<Record<string, unknown>> {
  return httpGet<Record<string, unknown>>(
    `/trace/batches/scan/${encodeURIComponent(batchCode)}`
  );
}

/** GET /api/trace/batches/{id} */
export function getTraceBatch(id: number): Promise<TraceBatch> {
  return httpGet<TraceBatch>(`/trace/batches/${id}`);
}
