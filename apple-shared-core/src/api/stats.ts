/**
 * Stats & Dashboard API client.
 * Backend: StatsController, DashboardController
 */
import { httpGet } from './request';

export function getSummary() { return httpGet('/stats/summary'); }
export function getTrend() { return httpGet('/stats/trend'); }
export function getTopVarieties() { return httpGet('/stats/top-varieties'); }
export function getPending() { return httpGet('/stats/pending'); }
export function getDashboardStats() { return httpGet('/dashboard/stats'); }
export function getDashboardTrend() { return httpGet('/dashboard/trend'); }
export function getTopOrchards() { return httpGet('/dashboard/top-orchards'); }
export function getPendingActions() { return httpGet('/dashboard/pending-actions'); }
