/**
 * Auth API client.
 *
 * Backend source (verified):
 * - apple-module-user/controller/AuthController.java → /api/user/auth/*
 *
 * NOTE — No SMS verification code endpoint exists. Login is username + password only.
 *        H5 login UI accepts a phone number as the `username` field.
 */
import { httpGet, httpPost } from './request';
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
