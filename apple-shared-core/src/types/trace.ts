/**
 * Trace types — mirror apple-module-trace/entity/*.java
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
 * Full-chain scan response shape from GET /api/trace/scan/{traceCode}/full
 * Backend returns TraceFullChainVO — keep as loose shape until the VO is read.
 */
export interface TraceFullChain {
  traceCode?: string;
  batch?: TraceBatch;
  nodes?: TraceNode[];
  [k: string]: unknown;
}

export interface TraceNode {
  id?: number;
  nodeType?: string;
  nodeName?: string;
  operator?: string;
  operateTime?: string;
  location?: string;
  remark?: string;
  [k: string]: unknown;
}
