/**
 * Trace types — mirror apple-module-trace entity/VO exactly.
 */
import type { BaseEntity } from './common';

/** TraceBatch.java — @TableName("trace_batch") */
export interface TraceBatch extends BaseEntity {
  /** TB + yyyyMMdd + 4-digit seq */
  batchCode?: string;
  orchardId?: number;
  orchardName?: string;
  /** yyyy-MM-dd */
  harvestDate?: string;
  variety?: string;
  /** A / B / C */
  grade?: TraceBatchGrade;
  /** kg */
  weight?: string | number;
  /** CREATED / PROCESSING / COMPLETED / SHIPPED */
  status?: TraceBatchStatus;
  blockchainHash?: string;
}

export type TraceBatchGrade = 'A' | 'B' | 'C';
export type TraceBatchStatus =
  | 'CREATED'
  | 'PROCESSING'
  | 'COMPLETED'
  | 'SHIPPED';

export const TRACE_STATUS_LABEL: Record<TraceBatchStatus, string> = {
  CREATED: '已创建',
  PROCESSING: '加工中',
  COMPLETED: '已完成',
  SHIPPED: '已发运',
};

/**
 * TraceFullChainVO.java — returned by GET /api/trace/scan/{traceCode}/full
 * Fields extracted from com.apple.chain.trace.dto.TraceFullChainVO (1:1 mirror).
 */
export interface TraceFullChain {
  traceCode?: string;
  batchNo?: string;
  variety?: string;
  currentStatus?: string;
  /** Planting info */
  orchardName?: string;
  farmerName?: string;
  region?: string;
  /** Input materials used — each entry is a loose map */
  inputMaterials?: Record<string, unknown>[];
  /** Warehouse events */
  warehouseRecords?: Record<string, unknown>[];
  /** Trade info */
  tradeInfo?: Record<string, unknown>;
  /** Timeline nodes */
  timeline?: TraceNode[];
  /** Blockchain verification */
  chainTxHash?: string;
  chainStatus?: number;
}

/** TraceNode.java — @TableName("tr_trace_node") */
export interface TraceNode {
  id?: number;
  nodeType?: string;
  nodeName?: string;
  operator?: string;
  operateTime?: string;
  location?: string;
  remark?: string;
}
