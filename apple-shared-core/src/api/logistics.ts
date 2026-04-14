/**
 * Logistics (cold chain delivery) API client.
 *
 * Backend source (verified):
 * - apple-module-coldchain/controller/DeliveryController.java → /api/coldchain/deliveries/*
 */
import { httpGet, httpPost } from './request';
import type { PageResult } from '../types/common';
import type { Delivery } from '../types/logistics';

export interface DeliveryListParams {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;
}

/** GET /api/coldchain/deliveries/list */
export function findDeliveries(
  params: DeliveryListParams = {}
): Promise<PageResult<Delivery>> {
  return httpGet<PageResult<Delivery>>('/coldchain/deliveries/list', {
    params,
  });
}

/** GET /api/coldchain/deliveries/{id} */
export function getDelivery(id: number): Promise<Delivery> {
  return httpGet<Delivery>(`/coldchain/deliveries/${id}`);
}

/**
 * POST /api/coldchain/deliveries/{id}/sign
 * Backend signature:
 *   sign(@PathVariable Long id, @RequestBody Delivery delivery)
 *
 * Caller supplies signer-side fields on a Delivery:
 *   signTime, signPhoto, qualityCheck (PENDING/PASSED/REJECTED), qualityRemark, remark
 * Status will transition to SIGNED on server.
 */
export function acknowledgeDelivery(
  id: number,
  payload: Partial<Delivery>
): Promise<Delivery> {
  return httpPost<Delivery>(`/coldchain/deliveries/${id}/sign`, payload);
}

/**
 * Convenience: find a pending/delivering delivery by its deliveryCode.
 * Uses the keyword search on the list endpoint (backend performs LIKE on code/receiver).
 */
export async function findDeliveryByCode(
  deliveryCode: string
): Promise<Delivery | null> {
  const page = await findDeliveries({
    page: 1,
    size: 5,
    keyword: deliveryCode,
  });
  const hit =
    page.records.find((d) => d.deliveryCode === deliveryCode) ??
    page.records[0] ??
    null;
  return hit ?? null;
}
