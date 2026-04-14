# 文件上传模块 API 契约

> 模块：`apple-module-file`
> URL 前缀：`/api/files`
> 错误码段：`800000 ~ 809999`
> 最近更新：2026-04-14

## 接口索引

| # | 接口 | URL | 方法 | 认证 |
|---|------|-----|------|------|
| 1 | [上传文件](#1-上传文件) | `/api/files/upload` | POST | ✅ |
| 2 | [下载文件](#2-下载文件) | `/api/files/{fileId}` | GET | ✅ |

---

## 1. 上传文件

**URL**：`POST /api/files/upload`
**Content-Type**：`multipart/form-data`
**认证**：JWT Bearer Token（由全局 `AuthInterceptor` 校验）
**错误码**：`800001 ~ 800006`

### 请求参数

| 字段 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| `file` | form-data | File | ✅ | 文件内容，大小 ≤ 10 MB |
| `bizType` | form-data | string | ❌ | 业务类型标记，例如 `growth-record` / `orchard-photo` |

**服务端校验**：
- 非空：`file.isEmpty() == false`
- 大小：`file.size ≤ apple.file.max-size`（默认 10485760 = 10 MB）
- 类型白名单：`apple.file.allowed-content-types`（默认 jpg/jpeg/png/webp/heic/heif）
- 文件名清洗：剥离路径分隔符与控制字符，生成 `{uuid32}.{ext}` 防覆盖/遍历

### 响应体 `FileUploadResponse`

| 字段 | 类型 | 必返 | 说明 | 示例 |
|------|------|------|------|------|
| `fileId` | string | ✅ | 32 位十六进制 UUID（无扩展名） | `ab12cd34ef56...` |
| `url` | string | ✅ | 公开访问路径 | `/api/files/ab12cd34...` |
| `filename` | string | ✅ | 物理文件名 | `ab12cd34...jpg` |
| `size` | number | ✅ | 字节数 | `204800` |
| `contentType` | string | ✅ | MIME 类型 | `image/jpeg` |
| `bizType` | string \| null | ❌ | 与入参一致 | `growth-record` |
| `uploadTime` | string | ✅ | `yyyy-MM-dd HH:mm:ss` | `2026-04-14 12:30:00` |

```typescript
export interface FileUploadResponse {
  fileId: string;
  url: string;
  filename: string;
  size: number;
  contentType: string;
  bizType?: string | null;
  uploadTime: string;
}
```

### 错误码

| Code | 含义 | 触发条件 |
|------|------|---------|
| `800001` | 上传文件不能为空 | `file` 缺失或 0 字节 |
| `800002` | 文件大小超过限制 | 超出 `apple.file.max-size` |
| `800003` | 不支持的文件类型 | contentType 不在白名单 |
| `800005` | 文件存储失败 | 本地磁盘 IOException 等 |
| `800006` | 文件名非法 | 路径穿越嫌疑 |

---

## 2. 下载文件

**URL**：`GET /api/files/{fileId}`
**认证**：JWT Bearer Token
**响应**：二进制流 + `Content-Disposition: inline; filename*=UTF-8''...`
**错误码**：`800004`（不存在）/ `800006`（fileId 非法）

### 路径参数

| 字段 | 类型 | 校验 |
|------|------|------|
| `fileId` | string | 正则 `[a-zA-Z0-9]{1,64}` |

### 响应头

| Header | 说明 |
|--------|------|
| `Content-Type` | 存储时的 MIME（例：`image/jpeg`） |
| `Content-Length` | 字节数 |
| `Content-Disposition` | `inline; filename*=UTF-8''{original-name-url-encoded}` |
| `Cache-Control` | `private, max-age=3600` |

---

## 存储实现

- **接口**：`com.apple.chain.file.storage.FileStorageService`
- **默认实现**：`LocalFileStorageService`（本地磁盘，`apple.file.storage.type=local`）
- **预留实现**：`OssFileStorageService`（占位，未 `@Service` 注解）

**目录布局**：`{apple.file.storage.local.root}/{yyyy}/{MM}/{dd}/{fileId}.{ext}`

**切换 OSS**：将 `OssFileStorageService` 加上 `@Service` +
`@ConditionalOnProperty(prefix="apple.file.storage", name="type", havingValue="oss")`
并接入 OSS SDK（不在本次变更范围内）。

---

## 数据库

**表**：`file_record`（由 `V30__file_upload.sql` 创建）

| 列 | 类型 | 说明 |
|----|------|------|
| `id` | BIGINT PK | 雪花 ID |
| `file_id` | VARCHAR(64) UNIQUE | 对外 UUID |
| `filename` | VARCHAR(255) | 物理文件名 |
| `original_name` | VARCHAR(255) | 原始文件名（已清洗） |
| `storage_path` | VARCHAR(512) | 存储路径 |
| `url` | VARCHAR(512) | 公网 URL |
| `size` | BIGINT | 字节数 |
| `content_type` | VARCHAR(64) | MIME |
| `biz_type` | VARCHAR(32) NULL | 业务标签 |
| `biz_id` | BIGINT NULL | 关联业务 ID |
| `uploader_id` | BIGINT NULL | 上传者 `uc_user.id` |
| `upload_time` | DATETIME | 上传时间 |
| `create_time / update_time / create_by / deleted` | — | `BaseEntity` 审计列 |

**索引**：`uk_file_record_file_id` / `idx_file_record_biz(biz_type, biz_id)` / `idx_file_record_uploader` / `idx_file_record_upload_time`

---

## 下游关联：`pt_growth_record.photo_urls`

由 `V31__growth_record_photo_urls.sql` 新增 `VARCHAR(2000)` 列，存 JSON 数组字符串：
```json
["/api/files/ab12cd34...", "/api/files/ef56ab78..."]
```
H5 `GrowthRecordPage.vue` 上传照片后将返回的 `url` 收集为该字段，**替代之前 `notes` 字符串中的 `[photos]N` 临时 hack**。
