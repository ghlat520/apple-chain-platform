import request from './request'

export const harvestApi = {
  list: (params) => request.get('/harvest/list', { params }),
  getById: (id) => request.get(`/harvest/${id}`)
}
