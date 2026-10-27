/**
 * File upload types — mirror apple-module-file/dto/FileUploadResponse.java.
 */

export interface FileUploadResponse {
  fileId: string;
  url: string;
  filename: string;
  size: number;
  contentType: string;
  bizType?: string | null;
  /** ISO-ish string `yyyy-MM-dd HH:mm:ss` per global Jackson format. */
  uploadTime: string;
}
