/**
 * Farm management API (orchards + farmers).
 * Backend: OrchardController, FarmerController
 */
import { httpGet, httpPost, httpPut, httpDelete } from './request';

// Orchards
export function getOrchards(params?: Record<string, any>) { return httpGet('/planting/orchard/list', { params }); }
export function getOrchard(id: number | string) { return httpGet(`/planting/orchard/${id}`); }
export function createOrchard(data: Record<string, any>) { return httpPost('/planting/orchard', data); }
export function updateOrchard(id: number | string, data: Record<string, any>) { return httpPut(`/planting/orchard/${id}`, data); }
export function deleteOrchard(id: number | string) { return httpDelete(`/planting/orchard/${id}`); }

// Farmers
export function getFarmers(params?: Record<string, any>) { return httpGet('/farm/farmers', { params }); }
export function getFarmer(id: number | string) { return httpGet(`/farm/farmers/${id}`); }
export function createFarmer(data: Record<string, any>) { return httpPost('/farm/farmers', data); }
export function updateFarmer(id: number | string, data: Record<string, any>) { return httpPut(`/farm/farmers/${id}`, data); }
export function deleteFarmer(id: number | string) { return httpDelete(`/farm/farmers/${id}`); }

// Export helpers
export function exportFarmers() {
  const token = localStorage.getItem('token');
  return fetch('/api/farm/farmers/export', {
    headers: { Authorization: `Bearer ${token}` }
  }).then(r => { if (!r.ok) throw new Error('导出失败'); return r.blob(); })
    .then(blob => {
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = '农户列表.csv';
      document.body.appendChild(a); a.click(); document.body.removeChild(a);
      URL.revokeObjectURL(a.href);
    });
}

export function exportOrchards() {
  const token = localStorage.getItem('token');
  return fetch('/api/planting/orchard/export', {
    headers: { Authorization: `Bearer ${token}` }
  }).then(r => { if (!r.ok) throw new Error('导出失败'); return r.blob(); })
    .then(blob => {
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = '果园列表.csv';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(a.href);
    });
}
