import request from './request.js'

export const vehicleApi = {
  list: (params) => request.get('/coldchain/vehicles/list', { params }),
  get: (id) => request.get(`/coldchain/vehicles/${id}`),
  create: (data) => request.post('/coldchain/vehicles', data),
  update: (id, data) => request.put(`/coldchain/vehicles/${id}`, data),
  delete: (id) => request.delete(`/coldchain/vehicles/${id}`),
  updateStatus: (id, newStatus) => request.put(`/coldchain/vehicles/${id}/status`, null, { params: { newStatus } }),
}

export const taskApi = {
  list: (params) => request.get('/coldchain/tasks/list', { params }),
  get: (id) => request.get(`/coldchain/tasks/${id}`),
  create: (data) => request.post('/coldchain/tasks', data),
  update: (id, data) => request.put(`/coldchain/tasks/${id}`, data),
  depart: (id) => request.post(`/coldchain/tasks/${id}/depart`),
  deliver: (id) => request.post(`/coldchain/tasks/${id}/deliver`),
  cancel: (id) => request.post(`/coldchain/tasks/${id}/cancel`),
  delete: (id) => request.delete(`/coldchain/tasks/${id}`),
}

export const deliveryApi = {
  list: (params) => request.get('/coldchain/deliveries/list', { params }),
  get: (id) => request.get(`/coldchain/deliveries/${id}`),
  create: (data) => request.post('/coldchain/deliveries', data),
  update: (id, data) => request.put(`/coldchain/deliveries/${id}`, data),
  sign: (id, data) => request.post(`/coldchain/deliveries/${id}/sign`, data),
  delete: (id) => request.delete(`/coldchain/deliveries/${id}`),
}

export const precoolApi = {
  list: (params) => request.get('/coldchain/precool', { params }),
  get: (id) => request.get(`/coldchain/precool/${id}`),
  create: (data) => request.post('/coldchain/precool', data),
  start: (id) => request.put(`/coldchain/precool/${id}/start`),
  complete: (id) => request.put(`/coldchain/precool/${id}/complete`),
}

export const temperatureApi = {
  list: (params) => request.get('/coldchain/temperatures/list', { params }),
  byTask: (params) => request.get('/coldchain/temperatures/by-task', { params }),
  create: (data) => request.post('/coldchain/temperatures', data),
  delete: (id) => request.delete(`/coldchain/temperatures/${id}`),
}

export const logisticsApi = {
  full: (taskId) => request.get(`/coldchain/logistics/${taskId}/full`),
}

export const coldchainStatsApi = {
  summary: () => request.get('/coldchain/statistics/summary'),
  alarms: (params) => request.get('/coldchain/statistics/alarms', { params }),
}
