/**
 * Orchard Geo & AMap config API.
 * Backend: OrchardGeoController, ConfigController
 */
import { httpGet, httpPost } from './request';

export function getOrchardGeoBbox(params?: Record<string, any>) { return httpGet('/planting/orchard/geo/bbox', { params }); }
export function saveOrchardBoundary(id: number | string, geojson: any) { return httpPost(`/planting/orchard/geo/${id}/boundary`, geojson); }
export function getAmapConfig() { return httpGet('/config/amap'); }

// Aliases for backward compatibility with web-ui naming
export function fetch() { return getAmapConfig(); }
export function bbox(params?: Record<string, any>) { return getOrchardGeoBbox(params); }
export function saveBoundary(id: number | string, geojson: any) { return saveOrchardBoundary(id, geojson); }
