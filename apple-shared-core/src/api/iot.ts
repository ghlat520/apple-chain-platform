// Phase 1 骨架 - 实际接口路径待与后端 Knife4j 对齐
/**
 * IoT API client (skeleton).
 *
 * Backend module: apple-module-iot
 * URL convention (placeholder): /api/iot/*
 */
import { httpGet, httpPost, httpPut, httpDelete } from './request';
import type { PageResult } from '../types/common';

export type IotItem = unknown;
export type IotListParams = Record<string, unknown>;
export type IotCreateDTO = Record<string, unknown>;
export type IotUpdateDTO = Record<string, unknown>;

/** GET /api/iot/list */
export function listIot(
  params: IotListParams = {}
): Promise<PageResult<IotItem>> {
  return httpGet<PageResult<IotItem>>('/iot/list', { params });
}

/** GET /api/iot/{id} */
export function getIot(id: number | string): Promise<IotItem> {
  return httpGet<IotItem>(`/iot/${id}`);
}

/** POST /api/iot */
export function createIot(dto: IotCreateDTO): Promise<IotItem> {
  return httpPost<IotItem>('/iot', dto);
}

/** PUT /api/iot/{id} */
export function updateIot(
  id: number | string,
  dto: IotUpdateDTO
): Promise<IotItem> {
  return httpPut<IotItem>(`/iot/${id}`, dto);
}

/** DELETE /api/iot/{id} */
export function deleteIot(id: number | string): Promise<void> {
  return httpDelete<void>(`/iot/${id}`);
}
