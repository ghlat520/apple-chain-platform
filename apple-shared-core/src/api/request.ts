/**
 * HTTP client — axios wrapper that unwraps the R<T> envelope from apple-common/result/R.java.
 *
 * Responsibilities:
 * - Inject Bearer token (from injected token provider)
 * - Unwrap { code, message, data } → if code === 200 return data; else throw ApiError
 * - Surface typed ApiError for UI layer to toast
 *
 * UI layer (Vue / wx) supplies 3 side-effect callbacks via `configureApi()`:
 *   - getToken()        → string | null
 *   - onUnauthorized()  → redirect to login, clear stored token
 *   - onError(message)  → user-visible toast
 *
 * This keeps the core framework-free.
 */
import axios, {
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
  InternalAxiosRequestConfig,
} from 'axios';
import type { R } from '../types/common';

export class ApiError extends Error {
  constructor(
    public readonly code: number,
    message: string,
    public readonly raw?: unknown
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export interface ApiHooks {
  getToken: () => string | null;
  onUnauthorized: () => void;
  onError: (message: string) => void;
  baseURL?: string;
  timeout?: number;
}

let hooks: ApiHooks = {
  getToken: () => null,
  onUnauthorized: () => {},
  onError: () => {},
  baseURL: '/api',
  timeout: 15000,
};

let instance: AxiosInstance | null = null;

export function configureApi(next: Partial<ApiHooks>): void {
  hooks = { ...hooks, ...next };
  instance = null; // force rebuild with new hooks
}

function buildInstance(): AxiosInstance {
  const client = axios.create({
    baseURL: hooks.baseURL ?? '/api',
    timeout: hooks.timeout ?? 15000,
  });

  client.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      const token = hooks.getToken();
      if (token && config.headers) {
        config.headers.set('Authorization', `Bearer ${token}`);
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  client.interceptors.response.use(
    // Backend always returns R<T> envelope (per apple-common/result/R.java)
    (response: AxiosResponse<R<unknown>>) => {
      const payload = response.data;
      // Non-JSON responses (e.g. QR code PNG) — pass through as-is
      if (
        payload == null ||
        typeof payload !== 'object' ||
        !('code' in payload)
      ) {
        return response;
      }
      if (payload.code === 200) {
        // Unwrap: replace response.data with the inner data
        response.data = payload.data as never;
        return response;
      }
      const msg = payload.message || '请求失败';
      hooks.onError(msg);
      return Promise.reject(new ApiError(payload.code, msg, payload));
    },
    (error) => {
      if (error.response?.status === 401) {
        hooks.onUnauthorized();
        return Promise.reject(new ApiError(401, '未登录或登录已过期'));
      }
      const msg =
        error.response?.data?.message || error.message || '网络错误';
      hooks.onError(msg);
      return Promise.reject(
        new ApiError(error.response?.status ?? -1, msg, error)
      );
    }
  );

  return client;
}

function getInstance(): AxiosInstance {
  if (!instance) instance = buildInstance();
  return instance;
}

/** Typed GET returning unwrapped data (R.data). */
export async function httpGet<T>(
  url: string,
  config?: AxiosRequestConfig
): Promise<T> {
  const res = await getInstance().get<unknown, AxiosResponse<T>>(url, config);
  return res.data;
}

export async function httpPost<T>(
  url: string,
  body?: unknown,
  config?: AxiosRequestConfig
): Promise<T> {
  const res = await getInstance().post<unknown, AxiosResponse<T>>(
    url,
    body,
    config
  );
  return res.data;
}

export async function httpPut<T>(
  url: string,
  body?: unknown,
  config?: AxiosRequestConfig
): Promise<T> {
  const res = await getInstance().put<unknown, AxiosResponse<T>>(
    url,
    body,
    config
  );
  return res.data;
}

export async function httpDelete<T>(
  url: string,
  config?: AxiosRequestConfig
): Promise<T> {
  const res = await getInstance().delete<unknown, AxiosResponse<T>>(
    url,
    config
  );
  return res.data;
}

export function rawClient(): AxiosInstance {
  return getInstance();
}
