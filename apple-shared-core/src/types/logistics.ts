/**
 * Logistics (cold chain) types — mirror apple-module-coldchain/entity/Delivery.java
 */
import type { BaseEntity } from './common';

/** Delivery.java — @TableName("cc_delivery") */
export interface Delivery extends BaseEntity {
  deliveryCode?: string;
  taskId?: number;
  receiverName?: string;
  receiverPhone?: string;
  receiverAddr?: string;
  /** yyyy-MM-dd HH:mm:ss */
  deliveryTime?: string;
  /** yyyy-MM-dd HH:mm:ss */
  signTime?: string;
  signPhoto?: string;
  /** PENDING / PASSED / REJECTED */
  qualityCheck?: DeliveryQualityCheck;
  qualityRemark?: string;
  /** PENDING / DELIVERING / SIGNED / REJECTED */
  status?: DeliveryStatus;
  remark?: string;
}

export type DeliveryStatus =
  | 'PENDING'
  | 'DELIVERING'
  | 'SIGNED'
  | 'REJECTED';

export type DeliveryQualityCheck = 'PENDING' | 'PASSED' | 'REJECTED';

export const DELIVERY_STATUS_LABEL: Record<DeliveryStatus, string> = {
  PENDING: '待配送',
  DELIVERING: '配送中',
  SIGNED: '已签收',
  REJECTED: '已拒收',
};

export const QUALITY_CHECK_LABEL: Record<DeliveryQualityCheck, string> = {
  PENDING: '待检',
  PASSED: '合格',
  REJECTED: '不合格',
};
