import { httpGet, httpPost, httpPut, httpDelete } from './request';

export function getSupplies(params?: Record<string, any>) { return httpGet('/trade/supply/list'); }
export function getSupply(id: number | string) { return httpGet(`/trade/supply/${id}`); }
export function createSupply(data: Record<string, any>) { return httpPost('/trade/supply', data); }
export function updateSupply(id: number | string, data: Record<string, any>) { return httpPut(`/trade/supply/${id}`, data); }
export function deleteSupply(id: number | string) { return httpDelete(`/trade/supply/${id}`); }
export function getOrders(params?: Record<string, any>) { return httpGet('/trade/order/list'); }
export function getOrder(id: number | string) { return httpGet(`/trade/order/${id}`); }
export function createOrder(data: Record<string, any>) { return httpPost('/trade/order', data); }
export function confirmOrder(id: number | string) { return httpPut(`/trade/order/${id}/confirm`); }
export function deliverOrder(id: number | string) { return httpPut(`/trade/order/${id}/deliver`); }
export function completeOrder(id: number | string) { return httpPut(`/trade/order/${id}/complete`); }
export function cancelOrder(id: number | string) { return httpPut(`/trade/order/${id}/cancel`); }
export function deleteOrder(id: number | string) { return httpDelete(`/trade/order/${id}`); }
export function getPurchaseNeeds(params?: Record<string, any>) { return httpGet('/trade/need/list'); }
export function getPurchaseNeed(id: number | string) { return httpGet(`/trade/need/${id}`); }
export function createPurchaseNeed(data: Record<string, any>) { return httpPost('/trade/need', data); }
export function updatePurchaseNeed(id: number | string, data: Record<string, any>) { return httpPut(`/trade/need/${id}`, data); }
export function publishPurchaseNeed(id: number | string) { return httpPut(`/trade/need/${id}/publish`); }
export function deletePurchaseNeed(id: number | string) { return httpDelete(`/trade/need/${id}`); }
export function getInspections(params?: Record<string, any>) { return httpGet('/trade/inspection'); }
export function getInspection(id: number | string) { return httpGet(`/trade/inspection/${id}`); }
export function createInspection(data: Record<string, any>) { return httpPost('/trade/inspection', data); }
export function inspectResult(id: number | string, data: Record<string, any>) { return httpPut(`/trade/inspection/${id}/inspect`, data); }
export function acceptInspection(id: number | string) { return httpPut(`/trade/inspection/${id}/accept`); }
export function disputeInspection(id: number | string, reason?: string) { return httpPut(`/trade/inspection/${id}/dispute`); }
export function getTradeSummary() { return httpGet('/trade/statistics/summary'); }
export function getTradeVariety() { return httpGet('/trade/statistics/variety'); }
export function getTradeTrend(months = 6) { return httpGet('/trade/statistics/trend'); }
export function getTradePriceIndex(variety?: string, days = 30) { return httpGet('/trade/statistics/price-index'); }
export function computeMatch(supplyId: number | string, topK = 10) { return httpPost('/trade/match/compute', null, { params: { supplyId, topK } }); }
export function getMatchTop(supplyId: number | string, topK = 10) { return httpGet(`/trade/match/supply/${supplyId}/top`); }
export function startNegotiation(data: Record<string, any>) { return httpPost('/trade/negotiation/start', data); }
export function offerNegotiation(id: number | string, data: Record<string, any>) { return httpPost(`/trade/negotiation/${id}/offer`, data); }
export function acceptNegotiation(id: number | string, data: Record<string, any>) { return httpPost(`/trade/negotiation/${id}/accept`, data); }
export function cancelNegotiation(id: number | string, data: Record<string, any>) { return httpPost(`/trade/negotiation/${id}/cancel`, data); }
export function sendChat(data: Record<string, any>) { return httpPost('/trade/chat/send', data); }
export function getChatHistory(sessionId: number | string, limit = 100) { return httpGet(`/trade/chat/${sessionId}/history`); }
export function markChatRead(sessionId: number | string, userId: number | string) { return httpPost(`/trade/chat/${sessionId}/read`, null, { params: { sessionId, userId } }); }
export function exportSupplies() { const t=localStorage.getItem('token');return fetch('/api/trade/supply/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='供货信息.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }
export function exportOrders() { const t=localStorage.getItem('token');return fetch('/api/trade/order/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='交易订单.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }
export function exportPurchaseNeeds() { const t=localStorage.getItem('token');return fetch('/api/trade/need/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='采购需求.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }
export function exportInspections() { const t=localStorage.getItem('token');return fetch('/api/trade/inspection/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='质量检验记录.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }