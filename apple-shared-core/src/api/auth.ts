/**
 * Auth API client.
 *
 * Backend source (verified):
 * - apple-module-user/controller/AuthController.java → /api/user/auth/*
 *
 * NOTE — No SMS verification code endpoint exists. Login is username + password only.
 *        H5 login UI accepts a phone number as the `username` field.
 */
import { httpGet, httpPost, httpPut, httpDelete } from './request';
import type { CurrentUser, LoginRequest, LoginResponse } from '../types/auth';

/** POST /api/user/auth/login */
export function login(body: LoginRequest): Promise<LoginResponse> {
  return httpPost<LoginResponse>('/user/auth/login', body);
}

/** POST /api/user/auth/logout */
export function logout(): Promise<void> {
  return httpPost<void>('/user/auth/logout');
}

/** GET /api/user/auth/me */
export function getCurrentUser(): Promise<CurrentUser> {
  return httpGet<CurrentUser>('/user/auth/me');
}

/** Alias — legacy code uses getMe instead of getCurrentUser */
export function getMe(): Promise<CurrentUser> {
  return getCurrentUser();
}

// ---------- RBAC (admin) ----------

export function listRoles() { return httpGet('/admin/roles'); }
export function getMatrix() { return httpGet('/admin/permissions/matrix'); }
export function assignRoles(userId: number | string, roleCodes: string[]) {
  return httpPost(`/admin/users/${userId}/roles`, roleCodes);
}

// ---------- User management ----------

export function listUsers(params?: Record<string, any>) { return httpGet('/user/list', { params }); }
export function createUser(data: Record<string, any>) { return httpPost('/user/create', data); }
export function updateUser(id: number | string, data: Record<string, any>) { return httpPut(`/user/${id}`, data); }
export function deleteUser(id: number | string) { return httpDelete(`/user/${id}`); }
export function exportUsers(params?: Record<string, any>) { return httpGet('/user/export', { params } as any); }
