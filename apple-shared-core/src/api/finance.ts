import { httpGet, httpPost, httpPut, httpDelete } from './request';

export function getCredits(params?: Record<string, any>) { return httpGet('/finance/credits/list'); }
export function getCredit(id: number | string) { return httpGet(`/finance/credits/${id}`); }
export function createCredit(data: Record<string, any>) { return httpPost('/finance/credits', data); }
export function updateCredit(id: number | string, data: Record<string, any>) { return httpPut(`/finance/credits/${id}`, data); }
export function deleteCredit(id: number | string) { return httpDelete(`/finance/credits/${id}`); }
export function getLoans(params?: Record<string, any>) { return httpGet('/finance/loans/list'); }
export function getLoan(id: number | string) { return httpGet(`/finance/loans/${id}`); }
export function createLoan(data: Record<string, any>) { return httpPost('/finance/loans', data); }
export function updateLoan(id: number | string, data: Record<string, any>) { return httpPut(`/finance/loans/${id}`, data); }
export function approveLoan(id: number | string) { return httpPost(`/finance/loans/${id}/approve`); }
export function rejectLoan(id: number | string) { return httpPost(`/finance/loans/${id}/reject`); }
export function disburseLoan(id: number | string) { return httpPost(`/finance/loans/${id}/disburse`); }
export function deleteLoan(id: number | string) { return httpDelete(`/finance/loans/${id}`); }
export function getPledges(params?: Record<string, any>) { return httpGet('/finance/pledges/list'); }
export function getPledge(id: number | string) { return httpGet(`/finance/pledges/${id}`); }
export function createPledge(data: Record<string, any>) { return httpPost('/finance/pledges', data); }
export function updatePledge(id: number | string, data: Record<string, any>) { return httpPut(`/finance/pledges/${id}`, data); }
export function activatePledge(id: number | string) { return httpPost(`/finance/pledges/${id}/activate`); }
export function releasePledge(id: number | string) { return httpPost(`/finance/pledges/${id}/release`); }
export function deletePledge(id: number | string) { return httpDelete(`/finance/pledges/${id}`); }
export function getRisks(params?: Record<string, any>) { return httpGet('/finance/risks/list'); }
export function getRisk(id: number | string) { return httpGet(`/finance/risks/${id}`); }
export function createRisk(data: Record<string, any>) { return httpPost('/finance/risks', data); }
export function updateRisk(id: number | string, data: Record<string, any>) { return httpPut(`/finance/risks/${id}`, data); }
export function deleteRisk(id: number | string) { return httpDelete(`/finance/risks/${id}`); }
export function getStatisticsSummary() { return httpGet('/finance/statistics/summary'); }
export function getStatisticsTrend(params?: Record<string, any>) { return httpGet('/finance/statistics/trend'); }
export function getFinanceRisk() { return httpGet('/finance/statistics/risk'); }
export function processLoanWorkflow(id: number | string, data: Record<string, any>) { return httpPost(`/finance/loans/${id}/process-workflow`, data); }
export function markLoanOverdue(id: number | string) { return httpPost(`/finance/loans/${id}/overdue`); }
export function applyPlantLoan(data: Record<string, any>) { return httpPost('/finance/loans/apply/plant', data); }
export function applyWarehouseLoan(data: Record<string, any>) { return httpPost('/finance/loans/apply/warehouse', data); }
export function applyTradeLoan(data: Record<string, any>) { return httpPost('/finance/loans/apply/trade', data); }
export function exportLoanApply(data: Record<string, any>) { return httpPost('/finance/loans/apply/export', data); }
export function exportCredits() { const t=localStorage.getItem('token');return fetch('/api/finance/credits/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='信用评级.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }
export function exportLoans() { const t=localStorage.getItem('token');return fetch('/api/finance/loans/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='贷款记录.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }
export function exportPledges() { const t=localStorage.getItem('token');return fetch('/api/finance/pledges/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='仓单质押.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }
export function exportRisks() { const t=localStorage.getItem('token');return fetch('/api/finance/risks/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='风控记录.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }