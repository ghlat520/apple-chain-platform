import request from './request.js'

export const orchardGeoApi = {
  /** 视窗 bbox 查询果园地理信息 */
  bbox: (params) => request.get('/planting/orchard/geo/bbox', { params }),

  /** 保存果园多边形边界 GeoJSON */
  saveBoundary: (id, geojson) =>
    request.post(`/planting/orchard/geo/${id}/boundary`, geojson),
}

export const amapConfigApi = {
  /** 获取高德 SDK 配置（Key + securityJsCode） */
  fetch: () => request.get('/config/amap'),
}
