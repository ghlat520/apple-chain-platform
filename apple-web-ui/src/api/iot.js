import request from './request.js'

export function batchTelemetry(data) { return request.post('/iot/telemetry/batch', data) }
export function getLatestTelemetry(deviceSn) { return request.get(`/iot/telemetry/${deviceSn}/latest`) }
export function getTelemetrySeries(deviceSn, params) { return request.get(`/iot/telemetry/${deviceSn}/series`, { params }) }
export function getOpenAlerts(params) { return request.get('/iot/alerts/open', { params }) }
