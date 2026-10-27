/**
 * File upload / download API client.
 *
 * Backend source (verified):
 * - apple-module-file/controller/FileController.java
 *   POST   /api/files/upload      multipart/form-data (file, bizType?)
 *   GET    /api/files/{fileId}    binary download (inline)
 */
import { rawClient } from './request';
import type { FileUploadResponse } from '../types/file';

export interface UploadOptions {
  bizType?: string;
  /** Progress callback (0..1). */
  onProgress?: (fraction: number) => void;
}

/**
 * POST /api/files/upload — multipart/form-data.
 *
 * Note: {@link rawClient} is used so we can send FormData directly without the
 * JSON content-type override, and still benefit from the R<T> envelope
 * interceptor for the response.
 */
export async function uploadFile(
  file: Blob | File,
  options: UploadOptions = {}
): Promise<FileUploadResponse> {
  const formData = new FormData();
  formData.append('file', file);
  if (options.bizType) {
    formData.append('bizType', options.bizType);
  }
  const response = await rawClient().post<FileUploadResponse>(
    '/files/upload',
    formData,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (evt) => {
        if (options.onProgress && evt.total) {
          options.onProgress(evt.loaded / evt.total);
        }
      },
    }
  );
  // request.ts interceptor already unwrapped R<T> → response.data is FileUploadResponse
  return response.data;
}

/** Build a download URL for a fileId (absolute path for <img src>). */
export function buildFileUrl(fileId: string): string {
  return `/api/files/${fileId}`;
}
