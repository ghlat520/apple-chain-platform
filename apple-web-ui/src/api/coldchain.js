import request, { downloadFile } from './request.js'

export const coldchainApi = {
  // Vehicles
  getVehicles: (params) => request.get('/coldchain/vehicles/list', { params }),
  getVehicle: (id) => request.get(`/coldchain/vehicles/${id}`),
  createVehicle: (data) => request.post('/coldchain/vehicles', data),
  updateVehicle: (id, data) => request.put(`/coldchain/vehicles/${id}`, data),
  deleteVehicle: (id) => request.delete(`/coldchain/vehicles/${id}`),
  exportVehicles: () => downloadFile('/api/coldchain/vehicles/export', '车辆列表.csv'),

  // Transport Tasks
  getTasks: (params) => request.get('/coldchain/tasks/list', { params }),
  getTask: (id) => request.get(`/coldchain/tasks/${id}`),
  createTask: (data) => request.post('/coldchain/tasks', data),
  updateTask: (id, data) => request.put(`/coldchain/tasks/${id}`, data),
  departTask: (id) => request.post(`/coldchain/tasks/${id}/depart`),
  deliverTask: (id) => request.post(`/coldchain/tasks/${id}/deliver`),
  deleteTask: (id) => request.delete(`/coldchain/tasks/${id}`),
  exportTasks: () => downloadFile('/api/coldchain/tasks/export', '运输任务.csv'),

  // Temperature Records
  getTemperatures: (params) => request.get('/coldchain/temperatures/list', { params }),
  getTemperaturesByTask: (taskId) => request.get('/coldchain/temperatures/by-task', { params: { taskId } }),
  createTemperature: (data) => request.post('/coldchain/temperatures', data),
  deleteTemperature: (id) => request.delete(`/coldchain/temperatures/${id}`),

  // Deliveries
  getDeliveries: (params) => request.get('/coldchain/deliveries/list', { params }),
  getDelivery: (id) => request.get(`/coldchain/deliveries/${id}`),
  createDelivery: (data) => request.post('/coldchain/deliveries', data),
  updateDelivery: (id, data) => request.put(`/coldchain/deliveries/${id}`, data),
  signDelivery: (id, data) => request.post(`/coldchain/deliveries/${id}/sign`, data),
  deleteDelivery: (id) => request.delete(`/coldchain/deliveries/${id}`),
  exportDeliveries: () => downloadFile('/api/coldchain/deliveries/export', '配送记录.csv'),

  // Precooling (PreCoolTaskController: list + detail + create + start + complete)
  getPrecooling: (params) => request.get('/coldchain/precool', { params }),
  getPrecoolTask: (id) => request.get(`/coldchain/precool/${id}`),
  createPrecooling: (data) => request.post('/coldchain/precool', data),
  startPrecooling: (id) => request.put(`/coldchain/precool/${id}/start`),
  completePrecooling: (id) => request.put(`/coldchain/precool/${id}/complete`),

  // Statistics
  getStatisticsSummary: () => request.get('/coldchain/statistics/summary'),
  getTransportTrend: (params) => request.get('/coldchain/statistics/transport-trend', { params }),
  getColdchainAlarms: (days = 30) => request.get('/coldchain/statistics/alarms', { params: { days } }),

  // 车辆状态变更
  changeVehicleStatus: (id, status) => request.put(`/coldchain/vehicles/${id}/status`, { status }),
}
