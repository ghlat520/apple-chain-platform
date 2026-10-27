/**
 * Maturity / harvest recommendation API.
 * Backend: MaturityController (/api/planting/maturity/*)
 */
import { httpGet, httpPost } from './request';

export function recordMeasurement(data: Record<string, any>) { return httpPost('/planting/maturity/record', data); }
export function recommend(orchardId: number | string) { return httpGet(`/planting/maturity/recommend/${orchardId}`); }
export function listStandards() { return httpGet('/planting/maturity/standards'); }
export function listOrchardRecords(orchardId: number | string, limit = 20) { return httpGet(`/planting/maturity/records/${orchardId}`, { params: { limit } }); }
