import request from './request'

/**
 * M4 GIS map endpoints — separate from orchard.js so the existing CRUD client
 * stays untouched. Paths match OrchardGeoController (/api/planting/orchard/geo).
 */
export const orchardGeoApi = {
  /** Viewport bounding-box query */
  bbox: ({ lng1, lat1, lng2, lat2 }) =>
    request.get('/planting/orchard/geo/bbox', { params: { lng1, lat1, lng2, lat2 } }),

  /** Save polygon boundary; backend computes centroid + area */
  saveBoundary: (id, boundaryGeojson) =>
    request.post(`/planting/orchard/geo/${id}/boundary`, { boundaryGeojson })
}

/** Cross-cutting: fetch AMap JS SDK key + securityJsCode at runtime */
export const amapConfigApi = {
  fetch: () => request.get('/config/amap')
}
