/**
 * Planting types — mirror apple-module-planting/entity/*.java
 *
 * IRON RULE: fields are hand-copied from the Java entity. Do not invent names.
 */
import type { BaseEntity } from './common';

/** Orchard.java — @TableName("farm_orchard") */
export interface Orchard extends BaseEntity {
  /** ORD+yyyyMMdd+seq */
  orchardNo?: string;
  orchardName?: string;
  /** FK to uc_user.id */
  farmerId?: number;
  /** Area in mu (legacy, prefer areaMu) */
  area?: string | number;
  /** Apple variety e.g. 红富士 / 嘎拉 / 黄元帅 */
  variety?: string;
  location?: string;
  longitude?: string | number;
  latitude?: string | number;
  centerLat?: string | number;
  centerLng?: string | number;
  boundaryGeojson?: string;
  /** M4: derived area in mu */
  areaMu?: string | number;
  treeAge?: number;
  /** NORMAL / DORMANT / HARVESTED */
  status?: OrchardStatus;
  remark?: string;
}

export type OrchardStatus = 'NORMAL' | 'DORMANT' | 'HARVESTED';

/** GrowthRecord.java — @TableName("pt_growth_record") */
export interface GrowthRecord extends BaseEntity {
  orchardId?: number;
  /** FERTILIZE / SPRAY / IRRIGATE / PRUNE / PEST_CONTROL */
  recordType?: GrowthRecordType;
  /** yyyy-MM-dd */
  operateDate?: string;
  operator?: string;
  /** JSON array string: [{name, amount, unit}] */
  materials?: string;
  weather?: string;
  notes?: string;
  recordedBy?: number;
}

export type GrowthRecordType =
  | 'FERTILIZE'
  | 'SPRAY'
  | 'IRRIGATE'
  | 'PRUNE'
  | 'PEST_CONTROL';

export const GROWTH_RECORD_TYPE_LABEL: Record<GrowthRecordType, string> = {
  FERTILIZE: '施肥',
  SPRAY: '打药',
  IRRIGATE: '灌溉',
  PRUNE: '修剪',
  PEST_CONTROL: '病虫害防治',
};
