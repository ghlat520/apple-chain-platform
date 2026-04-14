import request from './request'

export const coldchainApi = {
  // 车辆管理
  vehicleList: (params) => request.get('/coldchain/vehicles/list', { params }),
  vehicleDetail: (id) => request.get(`/coldchain/vehicles/${id}`),
  vehicleCreate: (data) => request.post('/coldchain/vehicles', data),
  vehicleUpdate: (id, data) => request.put(`/coldchain/vehicles/${id}`, data),
  vehicleDelete: (id) => request.delete(`/coldchain/vehicles/${id}`),
  vehicleExport: (params) => request.get('/coldchain/vehicles/export', { params, responseType: 'blob' }),

  // 运输任务 (修正: transports → tasks, start → depart, complete → deliver)
  transportList: (params) => request.get('/coldchain/tasks/list', { params }),
  transportDetail: (id) => request.get(`/coldchain/tasks/${id}`),
  transportCreate: (data) => request.post('/coldchain/tasks', data),
  transportUpdate: (id, data) => request.put(`/coldchain/tasks/${id}`, data),
  transportDelete: (id) => request.delete(`/coldchain/tasks/${id}`),
  transportDepart: (id) => request.post(`/coldchain/tasks/${id}/depart`),
  transportDeliver: (id) => request.post(`/coldchain/tasks/${id}/deliver`),
  transportCancel: (id) => request.post(`/coldchain/tasks/${id}/cancel`),
  transportExport: (params) => request.get('/coldchain/tasks/export', { params, responseType: 'blob' }),

  // 预冷管理 (修正: precooling → precool)
  precoolingList: (params) => request.get('/coldchain/precool', { params }),
  precoolingDetail: (id) => request.get(`/coldchain/precool/${id}`),
  precoolingCreate: (data) => request.post('/coldchain/precool', data),
  precoolingStart: (id) => request.put(`/coldchain/precool/${id}/start`),
  precoolingComplete: (id) => request.put(`/coldchain/precool/${id}/complete`),

  // 配送管理
  deliveryList: (params) => request.get('/coldchain/deliveries/list', { params }),
  deliveryDetail: (id) => request.get(`/coldchain/deliveries/${id}`),
  deliveryCreate: (data) => request.post('/coldchain/deliveries', data),
  deliverySign: (id, data) => request.post(`/coldchain/deliveries/${id}/sign`, data),

  // 统计
  statisticsSummary: () => request.get('/coldchain/statistics/summary'),
  statisticsAlarms: () => request.get('/coldchain/statistics/alarms'),

  // 物流全链路查询
  logisticsFull: (taskId) => request.get(`/coldchain/logistics/${taskId}/full`)
}
