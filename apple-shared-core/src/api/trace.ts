import { httpGet, httpPost, httpPut, httpDelete } from './request';

export function getChains(params?: Record<string, any>) { return httpGet('/trace/list'); }
export function getChain(traceCode?: Record<string, any>) { return httpGet(`/trace/${traceCode}`); }
export function findTraceFullChain(code: string) { return httpGet(`/trace/full-chain/${code}`); }
export function scanByCode(traceCode?: Record<string, any>) { return httpGet(`/trace/scan/${traceCode}`); }
export function addNode(data: Record<string, any>) { return httpPost('/trace/node', data); }
export function getBatches(params?: Record<string, any>) { return httpGet('/trace/batches'); }
export function getBatch(id: number | string) { return httpGet(`/trace/batches/${id}`); }
export function createBatch(data: Record<string, any>) { return httpPost('/trace/batches', data); }
export function updateBatchStatus(id: number | string, status?: Record<string, any>) { return httpPut(`/trace/batches/${id}/status`, null, { params: { id, status } } as any); }
export function deleteBatch(id: number | string) { return httpDelete(`/trace/batches/${id}`); }
export function addRecord(batchId: number | string, data: Record<string, any>) { return httpPost(`/trace/batches/${batchId}/records`, data); }
export function listRecords(batchId: number | string) { return httpGet(`/trace/batches/${batchId}/records`); }
export function generateBox(batchId: number | string, boxCount?: Record<string, any>) { return httpPost('/trace/code/generate-box', null, { params: { batchId, boxCount } } as any); }
export function generateFruit(boxCode?: Record<string, any>, fruitCount?: Record<string, any>) { return httpPost('/trace/code/generate-fruit', null, { params: { boxCode, fruitCount } } as any); }
export function verifyCode(code?: Record<string, any>) { return httpGet(`/trace/code/verify/${code}`); }
export function getAnomalies(params?: Record<string, any>) { return httpGet('/trace/anomaly'); }
export function getAnomaly(id: number | string) { return httpGet(`/trace/anomaly/${id}`); }
export function reportAnomaly(data: Record<string, any>) { return httpPost('/trace/anomaly', data); }
export function investigateAnomaly(id: number | string) { return httpPut(`/trace/anomaly/${id}/investigate`); }
export function resolveAnomaly(id: number | string, data: Record<string, any>) { return httpPut(`/trace/anomaly/${id}/resolve`, data); }
export function getAnomalyImpact(traceCode?: Record<string, any>) { return httpGet(`/trace/anomaly/impact/${traceCode}`); }
export function submitChain(data: Record<string, any>) { return httpPost('/trace/chain/submit', data); }
export function verifyChain(traceCode?: Record<string, any>) { return httpGet(`/trace/chain/verify/${traceCode}`); }
export function getChainRecords(params?: Record<string, any>) { return httpGet('/admin/chain/records'); }
export function retryChain(id: number | string) { return httpPost(`/admin/chain/retry/${id}`); }
export function getScanFull(traceCode?: Record<string, any>) { return httpGet(`/trace/scan/${traceCode}/full`); }
export function getQrcode(traceCode?: Record<string, any>, params?: Record<string, any>) { return httpGet(`/trace/qrcode/${traceCode}`); }
export function scanBatchByCode(batchCode?: Record<string, any>) { return httpGet(`/trace/batches/scan/${batchCode}`); }
export function exportChains() { const t=localStorage.getItem('token');return fetch('/api/trace/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='溯源链.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }
export function exportBatches() { const t=localStorage.getItem('token');return fetch('/api/trace/batches/export',{headers:{Authorization:`Bearer ${t}`}}).then(r=>{if(!r.ok)throw new Error('导出失败');return r.blob();}).then(b=>{const a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='溯源批次.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href);}); }
export function exportVdp(batchId: number | string, format: string = 'pdf') {
  const t = localStorage.getItem('token');
  const url = `/api/trace/code/vdp-export/${batchId}?format=${format}`;
  return fetch(url, { headers: { Authorization: `Bearer ${t}` } })
    .then(r => { if (!r.ok) throw new Error('导出失败'); return r.blob(); })
    .then(b => {
      const a = document.createElement('a');
      a.href = URL.createObjectURL(b);
      a.download = `VDP_${batchId}.${format}`;
      document.body.appendChild(a); a.click(); document.body.removeChild(a);
      URL.revokeObjectURL(a.href);
    });
}
