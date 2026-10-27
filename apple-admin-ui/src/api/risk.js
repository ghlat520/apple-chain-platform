import request from './request.js'

// ========== 风控预警 - 规则管理 (RiskRuleController — /risk/rules) ==========
//
// IRON RULE 字段映射：以下字段全部从后端 Entity 1:1 复制
//   RiskRule (entity/RiskRule.java):
//     id, name, ruleType, threshold, severity, enabled,
//     creatorId, createTime, updateTime
//   RiskSeverity (enums/RiskSeverity.java): LOW(1) / MEDIUM(2) / HIGH(3) / CRITICAL(4)
export const riskRuleApi = {
  list: (params) => request.get('/risk/rules', { params }),
  get: (id) => request.get(`/risk/rules/${id}`),
  create: (data) => request.post('/risk/rules', data),
  update: (id, data) => request.put(`/risk/rules/${id}`, data),
  delete: (id) => request.delete(`/risk/rules/${id}`),
  // 后端：PUT /api/risk/rules/{id}/enable?enabled=true|false (query 参数)
  setEnabled: (id, enabled) => request.put(`/risk/rules/${id}/enable`, null, { params: { enabled } }),
}

// ========== 风控预警 - 事件管理 (RiskEventController — /risk/events) ==========
//
// IRON RULE 字段映射：以下字段全部从后端 Entity 1:1 复制
//   RiskEvent (entity/RiskEvent.java):
//     id, ruleId, targetType, targetId, severity, triggerValue,
//     status, assigneeId, handleRemark, triggerTime, handleTime, createTime
//   RiskEventStatus (enums/RiskEventStatus.java):
//     PENDING(1) / HANDLING(2) / RESOLVED(3) / IGNORED(4)
export const riskEventApi = {
  list: (params) => request.get('/risk/events', { params }),
  get: (id) => request.get(`/risk/events/${id}`),
  // 后端：PUT /api/risk/events/{id}/handle  RequestBody: { status, handleRemark, assigneeId }
  handle: (id, data) => request.put(`/risk/events/${id}/handle`, data),
}
