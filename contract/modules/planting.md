# 种植模块 API 契约

> 模块：`apple-module-planting`
> URL 前缀：`/api/planting`、`/api/farm`、`/api/cultivation`（M1 模块历史跨前缀）
> 错误码段：`200000 ~ 299999`
> 最近更新：2026-04-12

> **路径权威性声明**：本模块后端实际路径分布于 3 个前缀，其中 `/api/planting/orchard/*` 为果园的唯一权威路径。apple-web-ui 历史上使用 `/api/farm/orchards/*` 访问果园是前端 bug，应由前端（Track B2）改造对齐本契约，后端无需变更。

---

## 接口索引

| # | 接口 | URL | 方法 | 认证 |
|---|------|-----|------|------|
| 1 | [果园列表](#1-果园列表) | `/api/planting/orchard/list` | GET | ✅ |
| 2 | [果园详情](#2-果园详情) | `/api/planting/orchard/{id}` | GET | ✅ |
| 3 | [创建果园](#3-创建果园) | `/api/planting/orchard` | POST | ✅ |
| 4 | [更新果园](#4-更新果园) | `/api/planting/orchard/{id}` | PUT | ✅ |
| 5 | [删除果园](#5-删除果园) | `/api/planting/orchard/{id}` | DELETE | ✅ |
| 6 | [导出果园 CSV](#6-导出果园-csv) | `/api/planting/orchard/export` | GET | ✅ |
| 7 | [bbox 果园查询](#7-bbox-果园查询) | `/api/planting/orchard/geo/bbox` | GET | ✅ |
| 8 | [保存果园边界](#8-保存果园边界) | `/api/planting/orchard/geo/{id}/boundary` | POST | ✅ |
| 9 | [农户列表](#9-农户列表) | `/api/farm/farmers` | GET | ✅ |
| 10 | [农户详情](#10-农户详情) | `/api/farm/farmers/{id}` | GET | ✅ |
| 11 | [创建农户](#11-创建农户) | `/api/farm/farmers` | POST | ✅ |
| 12 | [更新农户](#12-更新农户) | `/api/farm/farmers/{id}` | PUT | ✅ |
| 13 | [删除农户](#13-删除农户) | `/api/farm/farmers/{id}` | DELETE | ✅ |
| 14 | [导出农户 CSV](#14-导出农户-csv) | `/api/farm/farmers/export` | GET | ✅ |
| 15 | [农事记录列表](#15-农事记录列表) | `/api/planting/record/list` | GET | ✅ |
| 16 | [农事记录详情](#16-农事记录详情) | `/api/planting/record/{id}` | GET | ✅ |
| 17 | [创建农事记录](#17-创建农事记录) | `/api/planting/record` | POST | ✅ |
| 18 | [更新农事记录](#18-更新农事记录) | `/api/planting/record/{id}` | PUT | ✅ |
| 19 | [删除农事记录](#19-删除农事记录) | `/api/planting/record/{id}` | DELETE | ✅ |
| 20 | [采收批次列表](#20-采收批次列表) | `/api/planting/harvest/list` | GET | ✅ |
| 21 | [采收批次详情](#21-采收批次详情) | `/api/planting/harvest/{id}` | GET | ✅ |
| 22 | [创建采收批次](#22-创建采收批次) | `/api/planting/harvest` | POST | ✅ |
| 23 | [更新采收批次](#23-更新采收批次) | `/api/planting/harvest/{id}` | PUT | ✅ |
| 24 | [确认采收批次](#24-确认采收批次) | `/api/planting/harvest/{id}/confirm` | PUT | ✅ |
| 25 | [删除采收批次](#25-删除采收批次) | `/api/planting/harvest/{id}` | DELETE | ✅ |
| 26 | [导出采收批次 CSV](#26-导出采收批次-csv) | `/api/planting/harvest/export` | GET | ✅ |
| 27 | [种植批次列表](#27-种植批次列表) | `/api/cultivation/batches` | GET | ✅ |
| 28 | [种植批次详情](#28-种植批次详情) | `/api/cultivation/batches/{id}` | GET | ✅ |
| 29 | [创建种植批次](#29-创建种植批次) | `/api/cultivation/batches` | POST | ✅ |
| 30 | [更新种植批次](#30-更新种植批次) | `/api/cultivation/batches/{id}` | PUT | ✅ |
| 31 | [删除种植批次](#31-删除种植批次) | `/api/cultivation/batches/{id}` | DELETE | ✅ |
| 32 | [导出种植批次 CSV](#32-导出种植批次-csv) | `/api/cultivation/batches/export` | GET | ✅ |
| 33 | [农事操作列表](#33-农事操作列表) | `/api/cultivation/operations` | GET | ✅ |
| 34 | [创建农事操作](#34-创建农事操作) | `/api/cultivation/operations` | POST | ✅ |
| 35 | [更新农事操作](#35-更新农事操作) | `/api/cultivation/operations/{id}` | PUT | ✅ |
| 36 | [删除农事操作](#36-删除农事操作) | `/api/cultivation/operations/{id}` | DELETE | ✅ |
| 37 | [录入成熟度采样](#37-录入成熟度采样) | `/api/planting/maturity/record` | POST | ✅ |
| 38 | [采收窗口推荐](#38-采收窗口推荐) | `/api/planting/maturity/recommend/{orchardId}` | GET | ✅ |
| 39 | [品种成熟度标准列表](#39-品种成熟度标准列表) | `/api/planting/maturity/standards` | GET | ✅ |
| 40 | [果园最近采样记录](#40-果园最近采样记录) | `/api/planting/maturity/records/{orchardId}` | GET | ✅ |
| 41 | [生成作业计划](#41-生成作业计划) | `/api/planting/task-plan/generate` | POST | ✅ |
| 42 | [查询计划列表](#42-查询计划列表) | `/api/planting/task-plan/list` | GET | ✅ |
| 43 | [标记计划完成](#43-标记计划完成) | `/api/planting/task-plan/{id}/done` | POST | ✅ |
| 44 | [跳过计划](#44-跳过计划) | `/api/planting/task-plan/{id}/skip` | POST | ✅ |
| 45 | [亩产排名](#45-亩产排名) | `/api/planting/analysis/yield` | GET | ✅ |
| 46 | [优果率统计](#46-优果率统计) | `/api/planting/analysis/premium` | GET | ✅ |
| 47 | [病虫害发生率](#47-病虫害发生率) | `/api/planting/analysis/pest` | GET | ✅ |
| 48 | [地块对比分析](#48-地块对比分析) | `/api/planting/analysis/compare` | GET | ✅ |

---

## §1 果园管理 OrchardController

### 1. 果园列表

**URL**：`GET /api/planting/orchard/list`
**认证**：需要 JWT
**权限**：`orchard:read`
**错误码**：无业务错误
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，默认 1 | `1` |
| `size` | number | ❌ | 每页，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 果园名称/编号模糊搜索 | `红富士` |
| `status` | number | ❌ | 状态码（见 OrchardStatus 枚举） | `1` |
| `farmerId` | number | ❌ | 按农户 ID 筛选 | `100001` |

```typescript
export interface OrchardPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  status?: number;
  farmerId?: number;
}
```

#### 响应体 `PageResult<OrchardResponse>`

`OrchardResponse` 字段：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 果园 ID（雪花） |
| `orchardNo` | string | ✅ | 果园编号，格式 `ORD+yyyyMMdd+4位序列` |
| `orchardName` | string | ✅ | 果园名称 |
| `farmerId` | number | ✅ | 关联农户 ID |
| `area` | number | ✅ | 面积（亩），精度 2 位小数 |
| `variety` | string | ✅ | 品种（红富士/嘎拉/黄元帅等） |
| `location` | string \| null | ❌ | 地址描述 |
| `longitude` | number \| null | ❌ | 经度 |
| `latitude` | number \| null | ❌ | 纬度 |
| `centerLat` | number \| null | ❌ | GIS 质心纬度 |
| `centerLng` | number \| null | ❌ | GIS 质心经度 |
| `areaMu` | number \| null | ❌ | GIS 计算面积（亩） |
| `treeAge` | number \| null | ❌ | 树龄（年） |
| `status` | `EnumValue<number>` | ✅ | 状态，见 OrchardStatus |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface OrchardResponse {
  id: number;
  orchardNo: string;
  orchardName: string;
  farmerId: number;
  area: number;
  variety: string;
  location: string | null;
  longitude: number | null;
  latitude: number | null;
  centerLat: number | null;
  centerLng: number | null;
  areaMu: number | null;
  treeAge: number | null;
  status: EnumValue<number>;
  remark: string | null;
  createdAt: string;
  updatedAt: string;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1024,
        "orchardNo": "ORD202604110001",
        "orchardName": "红富士1号园",
        "farmerId": 100001,
        "area": 15.5,
        "variety": "红富士",
        "location": "陕西省洛川县",
        "longitude": 109.43,
        "latitude": 35.76,
        "centerLat": 35.76,
        "centerLng": 109.43,
        "areaMu": 15.5,
        "treeAge": 8,
        "status": { "code": 1, "desc": "正常" },
        "remark": null,
        "createdAt": "2026-04-11 10:00:00",
        "updatedAt": "2026-04-11 15:30:00"
      }
    ],
    "total": 50,
    "current": 1,
    "size": 10
  }
}
```

---

### 2. 果园详情

**URL**：`GET /api/planting/orchard/{id}`
**认证**：需要 JWT
**权限**：`orchard:read`
**错误码**：`200001`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 果园 ID |

#### 响应体 `OrchardResponse`

同 [果园列表](#1-果园列表) 中的 `OrchardResponse`，额外包含：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `boundaryGeojson` | string \| null | ❌ | GIS 边界 GeoJSON Polygon 字符串 |

```typescript
export interface OrchardDetailResponse extends OrchardResponse {
  boundaryGeojson: string | null;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1024,
    "orchardNo": "ORD202604110001",
    "orchardName": "红富士1号园",
    "farmerId": 100001,
    "area": 15.5,
    "variety": "红富士",
    "location": "陕西省洛川县",
    "longitude": 109.43,
    "latitude": 35.76,
    "centerLat": 35.76,
    "centerLng": 109.43,
    "areaMu": 15.5,
    "treeAge": 8,
    "status": { "code": 1, "desc": "正常" },
    "boundaryGeojson": null,
    "remark": null,
    "createdAt": "2026-04-11 10:00:00",
    "updatedAt": "2026-04-11 15:30:00"
  }
}
```

#### 响应示例（失败）

```json
{ "code": 200001, "message": "果园不存在", "data": null }
```

---

### 3. 创建果园

**URL**：`POST /api/planting/orchard`
**认证**：需要 JWT
**权限**：`orchard:write`
**错误码**：`200002`
**变更历史**：2026-04-12 初版契约

#### 请求体 `OrchardCreateRequest`

| 字段 | 类型 | 必填 | 说明 | 校验 |
|------|------|------|------|------|
| `orchardName` | string | ✅ | 果园名称 | 非空，长度 ≤ 100 |
| `farmerId` | number | ✅ | 关联农户 ID | 非空 |
| `area` | number | ✅ | 面积（亩） | > 0 |
| `variety` | string | ✅ | 品种 | 非空 |
| `location` | string | ❌ | 地址描述 | 长度 ≤ 255 |
| `longitude` | number | ❌ | 经度 | -180~180 |
| `latitude` | number | ❌ | 纬度 | -90~90 |
| `treeAge` | number | ❌ | 树龄（年） | ≥ 0 |
| `status` | number | ❌ | 状态码，默认 1（正常） | 见 OrchardStatus |
| `remark` | string | ❌ | 备注 | 长度 ≤ 500 |

```typescript
export interface OrchardCreateRequest {
  orchardName: string;
  farmerId: number;
  area: number;
  variety: string;
  location?: string;
  longitude?: number;
  latitude?: number;
  treeAge?: number;
  status?: number;
  remark?: string;
}
```

**业务规则**：自动生成 `orchardNo`，格式 `ORD+yyyyMMdd+4位序列`。

#### 响应体

`data: OrchardResponse`（含生成的 `orchardNo` 和 `id`）

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1025,
    "orchardNo": "ORD202604120001",
    "orchardName": "嘎拉2号园",
    "farmerId": 100002,
    "area": 8.0,
    "variety": "嘎拉",
    "location": "陕西省延安市",
    "longitude": 109.49,
    "latitude": 36.59,
    "centerLat": null,
    "centerLng": null,
    "areaMu": null,
    "treeAge": 5,
    "status": { "code": 1, "desc": "正常" },
    "boundaryGeojson": null,
    "remark": null,
    "createdAt": "2026-04-12 09:00:00",
    "updatedAt": "2026-04-12 09:00:00"
  }
}
```

#### 响应示例（失败 - 编号冲突）

```json
{ "code": 200002, "message": "果园编号已存在", "data": null }
```

---

### 4. 更新果园

**URL**：`PUT /api/planting/orchard/{id}`
**认证**：需要 JWT
**权限**：`orchard:write`
**错误码**：`200001` / `200003`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 果园 ID |

#### 请求体 `OrchardUpdateRequest`

字段同 `OrchardCreateRequest`，所有字段可选（仅传需要修改的字段）。

```typescript
export interface OrchardUpdateRequest {
  orchardName?: string;
  farmerId?: number;
  area?: number;
  variety?: string;
  location?: string;
  longitude?: number;
  latitude?: number;
  treeAge?: number;
  status?: number;
  remark?: string;
}
```

#### 响应体

`data: OrchardResponse`

#### 响应示例（成功）

```json
{ "code": 200, "message": "操作成功", "data": { "id": 1024, "orchardName": "红富士1号园（更新）", "..." : "..." } }
```

#### 响应示例（失败）

```json
{ "code": 200001, "message": "果园不存在", "data": null }
```

---

### 5. 删除果园

**URL**：`DELETE /api/planting/orchard/{id}`
**认证**：需要 JWT
**权限**：`orchard:write`
**错误码**：`200001` / `200003`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 果园 ID |

**业务规则**：软删除（逻辑删除），设置 `deleted=1`。

#### 响应体

`data: null`

#### 响应示例（成功）

```json
{ "code": 200, "message": "操作成功", "data": null }
```

#### 响应示例（失败）

```json
{ "code": 200001, "message": "果园不存在", "data": null }
```

---

### 6. 导出果园 CSV

**URL**：`GET /api/planting/orchard/export`
**认证**：需要 JWT
**权限**：`orchard:read`
**错误码**：无业务错误
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

同 [果园列表](#1-果园列表) 的筛选参数（`keyword`/`status`/`farmerId`，不需要分页参数）。

#### 响应

返回 `Content-Type: text/csv; charset=UTF-8`（含 BOM），非 JSON。浏览器触发文件下载。

---

## §2 果园 GIS 地图 OrchardGeoController

### 7. bbox 果园查询

**URL**：`GET /api/planting/orchard/geo/bbox`
**认证**：需要 JWT
**权限**：`orchard:read`
**错误码**：`200902`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `lng1` | number | ✅ | 矩形左下角经度（-180~180） | `109.0` |
| `lat1` | number | ✅ | 矩形左下角纬度（-90~90） | `35.5` |
| `lng2` | number | ✅ | 矩形右上角经度（-180~180） | `110.0` |
| `lat2` | number | ✅ | 矩形右上角纬度（-90~90） | `36.5` |

```typescript
export interface OrchardBboxRequest {
  lng1: number;
  lat1: number;
  lng2: number;
  lat2: number;
}
```

**业务规则**：返回中心点（`centerLat`/`centerLng`）落在矩形范围内的果园，不含 `boundaryGeojson` 大字段。

#### 响应体 `OrchardResponse[]`

数组，字段同果园列表（不含 `boundaryGeojson`）。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1024,
      "orchardNo": "ORD202604110001",
      "orchardName": "红富士1号园",
      "farmerId": 100001,
      "area": 15.5,
      "variety": "红富士",
      "centerLat": 35.76,
      "centerLng": 109.43,
      "status": { "code": 1, "desc": "正常" },
      "createdAt": "2026-04-11 10:00:00",
      "updatedAt": "2026-04-11 15:30:00"
    }
  ]
}
```

#### 响应示例（失败 - 坐标超范围）

```json
{ "code": 200902, "message": "坐标超出范围", "data": null }
```

---

### 8. 保存果园边界

**URL**：`POST /api/planting/orchard/geo/{id}/boundary`
**认证**：需要 JWT
**权限**：`orchard:write`
**错误码**：`200001` / `200901`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 果园 ID |

#### 请求体 `OrchardBoundaryRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `boundaryGeojson` | string | ✅ | GeoJSON Polygon 字符串 |

```typescript
export interface OrchardBoundaryRequest {
  boundaryGeojson: string;
}
```

**业务规则**：服务端自动计算质心（`centerLat`/`centerLng`）和面积（`areaMu`）并写回果园记录。

#### 响应体

`data: null`

#### 响应示例（成功）

```json
{ "code": 200, "message": "操作成功", "data": null }
```

#### 响应示例（失败 - GeoJSON 格式错误）

```json
{ "code": 200901, "message": "GeoJSON 格式错误", "data": null }
```

---

## §3 农户管理 FarmerController

### 9. 农户列表

**URL**：`GET /api/farm/farmers`
**认证**：需要 JWT
**权限**：`farmer:read`
**错误码**：无业务错误
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，默认 1 | `1` |
| `size` | number | ❌ | 每页，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 姓名/手机号模糊搜索 | `张三` |
| `status` | number | ❌ | 状态码 | `1` |

```typescript
export interface FarmerPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  status?: number;
}
```

#### 响应体 `PageResult<FarmerResponse>`

`FarmerResponse` 字段：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 农户 ID（雪花） |
| `name` | string | ✅ | 姓名 |
| `phone` | string | ✅ | 手机号 |
| `idCard` | string \| null | ❌ | 身份证号（脱敏：中间 8 位用 `*` 替换） |
| `address` | string \| null | ❌ | 地址 |
| `status` | `EnumValue<number>` | ✅ | 状态（1-正常/2-禁用） |
| `orchardCount` | number | ✅ | 名下果园数量 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface FarmerResponse {
  id: number;
  name: string;
  phone: string;
  idCard: string | null;
  address: string | null;
  status: EnumValue<number>;
  orchardCount: number;
  createdAt: string;
  updatedAt: string;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 100001,
        "name": "张三",
        "phone": "13800138000",
        "idCard": "610102****1234",
        "address": "陕西省洛川县苹果路1号",
        "status": { "code": 1, "desc": "正常" },
        "orchardCount": 3,
        "createdAt": "2026-01-01 08:00:00",
        "updatedAt": "2026-04-11 10:00:00"
      }
    ],
    "total": 120,
    "current": 1,
    "size": 10
  }
}
```

---

### 10. 农户详情

**URL**：`GET /api/farm/farmers/{id}`
**认证**：需要 JWT
**权限**：`farmer:read`
**错误码**：`200101`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 农户 ID |

#### 响应体 `FarmerResponse`

同列表中的 `FarmerResponse`。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 100001,
    "name": "张三",
    "phone": "13800138000",
    "idCard": "610102****1234",
    "address": "陕西省洛川县苹果路1号",
    "status": { "code": 1, "desc": "正常" },
    "orchardCount": 3,
    "createdAt": "2026-01-01 08:00:00",
    "updatedAt": "2026-04-11 10:00:00"
  }
}
```

#### 响应示例（失败）

```json
{ "code": 200101, "message": "农户不存在", "data": null }
```

---

### 11. 创建农户

**URL**：`POST /api/farm/farmers`
**认证**：需要 JWT
**权限**：`farmer:write`
**错误码**：`200102`
**变更历史**：2026-04-12 初版契约

#### 请求体 `FarmerCreateRequest`

| 字段 | 类型 | 必填 | 说明 | 校验 |
|------|------|------|------|------|
| `name` | string | ✅ | 姓名 | 非空，长度 ≤ 50 |
| `phone` | string | ✅ | 手机号 | 11位数字，全局唯一 |
| `idCard` | string | ❌ | 身份证号 | 18位 |
| `address` | string | ❌ | 地址 | 长度 ≤ 255 |
| `status` | number | ❌ | 状态码，默认 1 | 见 FarmerStatus |

```typescript
export interface FarmerCreateRequest {
  name: string;
  phone: string;
  idCard?: string;
  address?: string;
  status?: number;
}
```

#### 响应体

`data: FarmerResponse`

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 100002,
    "name": "李四",
    "phone": "13900139000",
    "idCard": null,
    "address": "陕西省延安市",
    "status": { "code": 1, "desc": "正常" },
    "orchardCount": 0,
    "createdAt": "2026-04-12 09:00:00",
    "updatedAt": "2026-04-12 09:00:00"
  }
}
```

#### 响应示例（失败 - 手机号重复）

```json
{ "code": 200102, "message": "农户手机号重复", "data": null }
```

---

### 12. 更新农户

**URL**：`PUT /api/farm/farmers/{id}`
**认证**：需要 JWT
**权限**：`farmer:write`
**错误码**：`200101` / `200102`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 农户 ID |

#### 请求体 `FarmerUpdateRequest`

字段同 `FarmerCreateRequest`，所有字段可选。

```typescript
export interface FarmerUpdateRequest {
  name?: string;
  phone?: string;
  idCard?: string;
  address?: string;
  status?: number;
}
```

#### 响应体

`data: FarmerResponse`

#### 响应示例（成功）

```json
{ "code": 200, "message": "操作成功", "data": { "id": 100001, "..." : "..." } }
```

#### 响应示例（失败）

```json
{ "code": 200101, "message": "农户不存在", "data": null }
```

---

### 13. 删除农户

**URL**：`DELETE /api/farm/farmers/{id}`
**认证**：需要 JWT
**权限**：`farmer:write`
**错误码**：`200101`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 农户 ID |

**业务规则**：软删除。

#### 响应体

`data: null`

#### 响应示例（成功）

```json
{ "code": 200, "message": "操作成功", "data": null }
```

#### 响应示例（失败）

```json
{ "code": 200101, "message": "农户不存在", "data": null }
```

---

### 14. 导出农户 CSV

**URL**：`GET /api/farm/farmers/export`
**认证**：需要 JWT
**权限**：`farmer:read`
**错误码**：无业务错误
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

同 [农户列表](#9-农户列表) 的筛选参数，不需要分页参数。

#### 响应

返回 `Content-Type: text/csv; charset=UTF-8`（含 BOM），非 JSON。

---

## §4 农事记录 GrowthRecordController

### 15. 农事记录列表

**URL**：`GET /api/planting/record/list`
**认证**：需要 JWT
**权限**：`record:read`
**错误码**：无业务错误
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，默认 1 | `1` |
| `size` | number | ❌ | 每页，默认 10，最大 100 | `10` |
| `orchardId` | number | ❌ | 按果园筛选 | `1024` |
| `recordType` | number | ❌ | 记录类型码（见 GrowthRecordType） | `1` |

```typescript
export interface GrowthRecordPageRequest {
  page?: number;
  size?: number;
  orchardId?: number;
  recordType?: number;
}
```

**业务规则**：按 `operateDate` 降序排列。

#### 响应体 `PageResult<GrowthRecordResponse>`

`GrowthRecordResponse` 字段：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 记录 ID（雪花） |
| `orchardId` | number | ✅ | 果园 ID |
| `orchardName` | string | ✅ | 果园名称（冗余展示） |
| `recordType` | `EnumValue<number>` | ✅ | 记录类型，见 GrowthRecordType |
| `operateDate` | string | ✅ | 操作日期 `yyyy-MM-dd` |
| `content` | string | ✅ | 记录内容 |
| `operatorId` | number \| null | ❌ | 操作人 ID |
| `operatorName` | string \| null | ❌ | 操作人姓名 |
| `imageUrls` | string[] | ❌ | 图片 URL 列表 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface GrowthRecordResponse {
  id: number;
  orchardId: number;
  orchardName: string;
  recordType: EnumValue<number>;
  operateDate: string;
  content: string;
  operatorId: number | null;
  operatorName: string | null;
  imageUrls: string[];
  createdAt: string;
  updatedAt: string;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 5001,
        "orchardId": 1024,
        "orchardName": "红富士1号园",
        "recordType": { "code": 1, "desc": "施肥" },
        "operateDate": "2026-04-10",
        "content": "施用有机肥 200kg",
        "operatorId": 100001,
        "operatorName": "张三",
        "imageUrls": ["https://cdn.example.com/img/5001.jpg"],
        "createdAt": "2026-04-10 14:00:00",
        "updatedAt": "2026-04-10 14:00:00"
      }
    ],
    "total": 30,
    "current": 1,
    "size": 10
  }
}
```

---

### 16. 农事记录详情

**URL**：`GET /api/planting/record/{id}`
**认证**：需要 JWT
**权限**：`record:read`
**错误码**：`200201`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 记录 ID |

#### 响应体 `GrowthRecordResponse`

同列表中的 `GrowthRecordResponse`。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 5001,
    "orchardId": 1024,
    "orchardName": "红富士1号园",
    "recordType": { "code": 1, "desc": "施肥" },
    "operateDate": "2026-04-10",
    "content": "施用有机肥 200kg",
    "operatorId": 100001,
    "operatorName": "张三",
    "imageUrls": [],
    "createdAt": "2026-04-10 14:00:00",
    "updatedAt": "2026-04-10 14:00:00"
  }
}
```

#### 响应示例（失败）

```json
{ "code": 200201, "message": "农事记录不存在", "data": null }
```

---

### 17. 创建农事记录

**URL**：`POST /api/planting/record`
**认证**：需要 JWT
**权限**：`record:write`
**错误码**：`200001`
**变更历史**：2026-04-12 初版契约

#### 请求体 `GrowthRecordCreateRequest`

| 字段 | 类型 | 必填 | 说明 | 校验 |
|------|------|------|------|------|
| `orchardId` | number | ✅ | 果园 ID | 非空，果园必须存在 |
| `recordType` | number | ✅ | 记录类型码，见 GrowthRecordType | 非空 |
| `operateDate` | string | ✅ | 操作日期 `yyyy-MM-dd` | 非空 |
| `content` | string | ✅ | 记录内容 | 非空，长度 ≤ 2000 |
| `operatorId` | number | ❌ | 操作人 ID | — |
| `imageUrls` | string[] | ❌ | 图片 URL 列表 | — |

```typescript
export interface GrowthRecordCreateRequest {
  orchardId: number;
  recordType: number;
  operateDate: string;
  content: string;
  operatorId?: number;
  imageUrls?: string[];
}
```

#### 响应体

`data: GrowthRecordResponse`

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 5002,
    "orchardId": 1024,
    "orchardName": "红富士1号园",
    "recordType": { "code": 2, "desc": "打药" },
    "operateDate": "2026-04-12",
    "content": "喷施杀虫剂防治蚜虫",
    "operatorId": null,
    "operatorName": null,
    "imageUrls": [],
    "createdAt": "2026-04-12 10:00:00",
    "updatedAt": "2026-04-12 10:00:00"
  }
}
```

#### 响应示例（失败）

```json
{ "code": 200001, "message": "果园不存在", "data": null }
```

---

### 18. 更新农事记录

**URL**：`PUT /api/planting/record/{id}`
**认证**：需要 JWT
**权限**：`record:write`
**错误码**：`200201`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 记录 ID |

#### 请求体 `GrowthRecordUpdateRequest`

字段同 `GrowthRecordCreateRequest`，所有字段可选。

```typescript
export interface GrowthRecordUpdateRequest {
  orchardId?: number;
  recordType?: number;
  operateDate?: string;
  content?: string;
  operatorId?: number;
  imageUrls?: string[];
}
```

#### 响应体

`data: GrowthRecordResponse`

#### 响应示例（成功）

```json
{ "code": 200, "message": "操作成功", "data": { "id": 5001, "content": "施用有机肥 250kg（已更正）", "..." : "..." } }
```

#### 响应示例（失败）

```json
{ "code": 200201, "message": "农事记录不存在", "data": null }
```

---

### 19. 删除农事记录

**URL**：`DELETE /api/planting/record/{id}`
**认证**：需要 JWT
**权限**：`record:write`
**错误码**：`200201`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 记录 ID |

**业务规则**：软删除。

#### 响应体

`data: null`

#### 响应示例（成功）

```json
{ "code": 200, "message": "操作成功", "data": null }
```

#### 响应示例（失败）

```json
{ "code": 200201, "message": "农事记录不存在", "data": null }
```

---

## §5 采收批次 HarvestBatchController

### 20. 采收批次列表

**URL**：`GET /api/planting/harvest/list`
**认证**：需要 JWT
**权限**：`harvest:read`
**错误码**：无业务错误
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，默认 1 | `1` |
| `size` | number | ❌ | 每页，默认 10，最大 100 | `10` |
| `orchardId` | number | ❌ | 按果园筛选 | `1024` |
| `status` | number | ❌ | 状态码（见 HarvestBatchStatus） | `1` |

```typescript
export interface HarvestBatchPageRequest {
  page?: number;
  size?: number;
  orchardId?: number;
  status?: number;
}
```

#### 响应体 `PageResult<HarvestBatchResponse>`

`HarvestBatchResponse` 字段：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 批次 ID（雪花） |
| `orchardId` | number | ✅ | 果园 ID |
| `orchardName` | string | ✅ | 果园名称 |
| `variety` | string | ✅ | 品种 |
| `quantity` | number | ✅ | 采收数量（kg） |
| `grade` | string \| null | ❌ | 质量等级（A/B/C） |
| `harvestDate` | string | ✅ | 采收日期 `yyyy-MM-dd` |
| `status` | `EnumValue<number>` | ✅ | 状态，见 HarvestBatchStatus |
| `traceCode` | string \| null | ❌ | 溯源码（确认后生成） |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface HarvestBatchResponse {
  id: number;
  orchardId: number;
  orchardName: string;
  variety: string;
  quantity: number;
  grade: string | null;
  harvestDate: string;
  status: EnumValue<number>;
  traceCode: string | null;
  remark: string | null;
  createdAt: string;
  updatedAt: string;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 3001,
        "orchardId": 1024,
        "orchardName": "红富士1号园",
        "variety": "红富士",
        "quantity": 5000.0,
        "grade": "A",
        "harvestDate": "2026-10-15",
        "status": { "code": 1, "desc": "草稿" },
        "traceCode": null,
        "remark": null,
        "createdAt": "2026-10-15 08:00:00",
        "updatedAt": "2026-10-15 08:00:00"
      }
    ],
    "total": 15,
    "current": 1,
    "size": 10
  }
}
```

---

### 21. 采收批次详情

**URL**：`GET /api/planting/harvest/{id}`
**认证**：需要 JWT
**权限**：`harvest:read`
**错误码**：`200301`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 批次 ID |

#### 响应体 `HarvestBatchResponse`

同列表中的 `HarvestBatchResponse`。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 3001,
    "orchardId": 1024,
    "orchardName": "红富士1号园",
    "variety": "红富士",
    "quantity": 5000.0,
    "grade": "A",
    "harvestDate": "2026-10-15",
    "status": { "code": 2, "desc": "已确认" },
    "traceCode": "TC202610150001",
    "remark": null,
    "createdAt": "2026-10-15 08:00:00",
    "updatedAt": "2026-10-15 09:30:00"
  }
}
```

#### 响应示例（失败）

```json
{ "code": 200301, "message": "采收批次不存在", "data": null }
```

---

### 22. 创建采收批次

**URL**：`POST /api/planting/harvest`
**认证**：需要 JWT
**权限**：`harvest:write`
**错误码**：`200001` / `200303`
**变更历史**：2026-04-12 初版契约

#### 请求体 `HarvestBatchCreateRequest`

| 字段 | 类型 | 必填 | 说明 | 校验 |
|------|------|------|------|------|
| `orchardId` | number | ✅ | 果园 ID | 非空，果园必须存在 |
| `variety` | string | ✅ | 品种 | 非空 |
| `quantity` | number | ✅ | 采收数量（kg） | > 0 |
| `grade` | string | ❌ | 质量等级 A/B/C | — |
| `harvestDate` | string | ✅ | 采收日期 `yyyy-MM-dd` | 非空 |
| `remark` | string | ❌ | 备注 | 长度 ≤ 500 |

```typescript
export interface HarvestBatchCreateRequest {
  orchardId: number;
  variety: string;
  quantity: number;
  grade?: string;
  harvestDate: string;
  remark?: string;
}
```

#### 响应体

`data: HarvestBatchResponse`（`status` 默认草稿，`traceCode` 为 null）

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 3002,
    "orchardId": 1024,
    "orchardName": "红富士1号园",
    "variety": "红富士",
    "quantity": 3000.0,
    "grade": "B",
    "harvestDate": "2026-10-20",
    "status": { "code": 1, "desc": "草稿" },
    "traceCode": null,
    "remark": null,
    "createdAt": "2026-04-12 10:00:00",
    "updatedAt": "2026-04-12 10:00:00"
  }
}
```

#### 响应示例（失败 - 重量异常）

```json
{ "code": 200303, "message": "采收重量与果园面积不匹配", "data": null }
```

---

### 23. 更新采收批次

**URL**：`PUT /api/planting/harvest/{id}`
**认证**：需要 JWT
**权限**：`harvest:write`
**错误码**：`200301` / `200302`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 批次 ID |

**业务规则**：已确认（`status=2`）的批次不允许修改，返回 `200302`。

#### 请求体 `HarvestBatchUpdateRequest`

字段同 `HarvestBatchCreateRequest`，所有字段可选。

```typescript
export interface HarvestBatchUpdateRequest {
  variety?: string;
  quantity?: number;
  grade?: string;
  harvestDate?: string;
  remark?: string;
}
```

#### 响应体

`data: HarvestBatchResponse`

#### 响应示例（成功）

```json
{ "code": 200, "message": "操作成功", "data": { "id": 3001, "quantity": 5500.0, "..." : "..." } }
```

#### 响应示例（失败 - 状态不允许）

```json
{ "code": 200302, "message": "批次状态不允许确认", "data": null }
```

---

### 24. 确认采收批次

**URL**：`PUT /api/planting/harvest/{id}/confirm`
**认证**：需要 JWT
**权限**：`harvest:write`
**错误码**：`200301` / `200302`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 批次 ID |

**业务规则**：
- 只有草稿（`status=1`）的批次可确认
- 确认后自动生成溯源码（`traceCode`），格式 `TC+yyyyMMdd+4位序列`
- 状态流转到已确认（`status=2`）

#### 响应体

`data: HarvestBatchResponse`（含 `traceCode`）

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 3001,
    "orchardId": 1024,
    "orchardName": "红富士1号园",
    "variety": "红富士",
    "quantity": 5000.0,
    "grade": "A",
    "harvestDate": "2026-10-15",
    "status": { "code": 2, "desc": "已确认" },
    "traceCode": "TC202610150001",
    "remark": null,
    "createdAt": "2026-10-15 08:00:00",
    "updatedAt": "2026-10-15 09:30:00"
  }
}
```

#### 响应示例（失败 - 批次已确认）

```json
{ "code": 200302, "message": "批次状态不允许确认", "data": null }
```

---

### 25. 删除采收批次

**URL**：`DELETE /api/planting/harvest/{id}`
**认证**：需要 JWT
**权限**：`harvest:write`
**错误码**：`200301` / `200302`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 批次 ID |

**业务规则**：软删除；已确认批次不可删除。

#### 响应体

`data: null`

#### 响应示例（成功）

```json
{ "code": 200, "message": "操作成功", "data": null }
```

#### 响应示例（失败）

```json
{ "code": 200301, "message": "采收批次不存在", "data": null }
```

---

### 26. 导出采收批次 CSV

**URL**：`GET /api/planting/harvest/export`
**认证**：需要 JWT
**权限**：`harvest:read`
**错误码**：无业务错误
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

同 [采收批次列表](#20-采收批次列表) 的筛选参数，不需要分页参数。

#### 响应

返回 `Content-Type: text/csv; charset=UTF-8`（含 BOM），非 JSON。

---

## §6 种植批次 CultivationBatchController

### 27. 种植批次列表

**URL**：`GET /api/cultivation/batches`
**认证**：需要 JWT
**权限**：`cultivation:read`
**错误码**：无业务错误
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，默认 1 | `1` |
| `size` | number | ❌ | 每页，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 关键词模糊搜索（批次编号/品种） | `红富士` |
| `status` | number | ❌ | 状态码 | `1` |
| `orchardId` | number | ❌ | 按果园筛选 | `1024` |

```typescript
export interface CultivationBatchPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  status?: number;
  orchardId?: number;
}
```

#### 响应体 `PageResult<CultivationBatchResponse>`

`CultivationBatchResponse` 字段：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 批次 ID（雪花） |
| `batchCode` | string | ✅ | 批次编号（自动生成） |
| `orchardId` | number | ✅ | 果园 ID |
| `orchardName` | string | ✅ | 果园名称 |
| `variety` | string | ✅ | 品种 |
| `startDate` | string | ✅ | 种植开始日期 `yyyy-MM-dd` |
| `endDate` | string \| null | ❌ | 预计结束日期 `yyyy-MM-dd` |
| `status` | `EnumValue<number>` | ✅ | 状态（1-进行中/2-已完成/3-已取消） |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface CultivationBatchResponse {
  id: number;
  batchCode: string;
  orchardId: number;
  orchardName: string;
  variety: string;
  startDate: string;
  endDate: string | null;
  status: EnumValue<number>;
  remark: string | null;
  createdAt: string;
  updatedAt: string;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 4001,
        "batchCode": "CB202604120001",
        "orchardId": 1024,
        "orchardName": "红富士1号园",
        "variety": "红富士",
        "startDate": "2026-04-01",
        "endDate": "2026-10-31",
        "status": { "code": 1, "desc": "进行中" },
        "remark": null,
        "createdAt": "2026-04-01 08:00:00",
        "updatedAt": "2026-04-01 08:00:00"
      }
    ],
    "total": 8,
    "current": 1,
    "size": 10
  }
}
```

---

### 28. 种植批次详情

**URL**：`GET /api/cultivation/batches/{id}`
**认证**：需要 JWT
**权限**：`cultivation:read`
**错误码**：`200401`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 批次 ID |

#### 响应体 `CultivationBatchResponse`

同列表中的 `CultivationBatchResponse`。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 4001,
    "batchCode": "CB202604120001",
    "orchardId": 1024,
    "orchardName": "红富士1号园",
    "variety": "红富士",
    "startDate": "2026-04-01",
    "endDate": "2026-10-31",
    "status": { "code": 1, "desc": "进行中" },
    "remark": null,
    "createdAt": "2026-04-01 08:00:00",
    "updatedAt": "2026-04-01 08:00:00"
  }
}
```

#### 响应示例（失败）

```json
{ "code": 200401, "message": "种植批次不存在", "data": null }
```

---

### 29. 创建种植批次

**URL**：`POST /api/cultivation/batches`
**认证**：需要 JWT
**权限**：`cultivation:write`
**错误码**：`200001` / `200402`
**变更历史**：2026-04-12 初版契约

#### 请求体 `CultivationBatchCreateRequest`

| 字段 | 类型 | 必填 | 说明 | 校验 |
|------|------|------|------|------|
| `orchardId` | number | ✅ | 果园 ID | 非空，果园必须存在 |
| `variety` | string | ✅ | 品种 | 非空 |
| `startDate` | string | ✅ | 开始日期 `yyyy-MM-dd` | 非空 |
| `endDate` | string | ❌ | 预计结束日期 `yyyy-MM-dd` | — |
| `remark` | string | ❌ | 备注 | 长度 ≤ 500 |

```typescript
export interface CultivationBatchCreateRequest {
  orchardId: number;
  variety: string;
  startDate: string;
  endDate?: string;
  remark?: string;
}
```

**业务规则**：自动生成 `batchCode`，格式 `CB+yyyyMMdd+4位序列`。

#### 响应体

`data: CultivationBatchResponse`

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 4002,
    "batchCode": "CB202604120002",
    "orchardId": 1024,
    "orchardName": "红富士1号园",
    "variety": "红富士",
    "startDate": "2026-04-12",
    "endDate": null,
    "status": { "code": 1, "desc": "进行中" },
    "remark": null,
    "createdAt": "2026-04-12 10:00:00",
    "updatedAt": "2026-04-12 10:00:00"
  }
}
```

#### 响应示例（失败 - 编号冲突）

```json
{ "code": 200402, "message": "批次编号已存在", "data": null }
```

---

### 30. 更新种植批次

**URL**：`PUT /api/cultivation/batches/{id}`
**认证**：需要 JWT
**权限**：`cultivation:write`
**错误码**：`200401`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 批次 ID |

#### 请求体 `CultivationBatchUpdateRequest`

字段同 `CultivationBatchCreateRequest`，所有字段可选，另可更新 `status`。

```typescript
export interface CultivationBatchUpdateRequest {
  variety?: string;
  startDate?: string;
  endDate?: string;
  status?: number;
  remark?: string;
}
```

#### 响应体

`data: CultivationBatchResponse`

#### 响应示例（成功）

```json
{ "code": 200, "message": "操作成功", "data": { "id": 4001, "endDate": "2026-11-15", "..." : "..." } }
```

#### 响应示例（失败）

```json
{ "code": 200401, "message": "种植批次不存在", "data": null }
```

---

### 31. 删除种植批次

**URL**：`DELETE /api/cultivation/batches/{id}`
**认证**：需要 JWT
**权限**：`cultivation:write`
**错误码**：`200401`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 批次 ID |

**业务规则**：软删除。

#### 响应体

`data: null`

#### 响应示例（成功）

```json
{ "code": 200, "message": "操作成功", "data": null }
```

#### 响应示例（失败）

```json
{ "code": 200401, "message": "种植批次不存在", "data": null }
```

---

### 32. 导出种植批次 CSV

**URL**：`GET /api/cultivation/batches/export`
**认证**：需要 JWT
**权限**：`cultivation:read`
**错误码**：无业务错误
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

同 [种植批次列表](#27-种植批次列表) 的筛选参数，不需要分页参数。

#### 响应

返回 `Content-Type: text/csv; charset=UTF-8`（含 BOM），非 JSON。

---

## §7 农事操作 CultivationOperationController

### 33. 农事操作列表

**URL**：`GET /api/cultivation/operations`
**认证**：需要 JWT
**权限**：`cultivation:read`
**错误码**：无业务错误
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，默认 1 | `1` |
| `size` | number | ❌ | 每页，默认 10，最大 100 | `10` |
| `batchId` | number | ❌ | 按种植批次筛选 | `4001` |
| `operationType` | number | ❌ | 操作类型码（见 OperationType） | `1` |

```typescript
export interface CultivationOperationPageRequest {
  page?: number;
  size?: number;
  batchId?: number;
  operationType?: number;
}
```

#### 响应体 `PageResult<CultivationOperationResponse>`

`CultivationOperationResponse` 字段：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 操作记录 ID（雪花） |
| `batchId` | number | ✅ | 种植批次 ID |
| `batchCode` | string | ✅ | 批次编号 |
| `operationType` | `EnumValue<number>` | ✅ | 操作类型，见 OperationType |
| `operateDate` | string | ✅ | 操作日期 `yyyy-MM-dd` |
| `quantity` | number \| null | ❌ | 用量（单位依操作类型） |
| `unit` | string \| null | ❌ | 单位（kg/L 等） |
| `material` | string \| null | ❌ | 使用材料（农药/化肥名称等） |
| `operatorId` | number \| null | ❌ | 操作人 ID |
| `operatorName` | string \| null | ❌ | 操作人姓名 |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface CultivationOperationResponse {
  id: number;
  batchId: number;
  batchCode: string;
  operationType: EnumValue<number>;
  operateDate: string;
  quantity: number | null;
  unit: string | null;
  material: string | null;
  operatorId: number | null;
  operatorName: string | null;
  remark: string | null;
  createdAt: string;
  updatedAt: string;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 6001,
        "batchId": 4001,
        "batchCode": "CB202604120001",
        "operationType": { "code": 1, "desc": "施肥" },
        "operateDate": "2026-04-10",
        "quantity": 200.0,
        "unit": "kg",
        "material": "复合肥（N15P15K15）",
        "operatorId": 100001,
        "operatorName": "张三",
        "remark": null,
        "createdAt": "2026-04-10 14:00:00",
        "updatedAt": "2026-04-10 14:00:00"
      }
    ],
    "total": 20,
    "current": 1,
    "size": 10
  }
}
```

---

### 34. 创建农事操作

**URL**：`POST /api/cultivation/operations`
**认证**：需要 JWT
**权限**：`cultivation:write`
**错误码**：`200401` / `200502`
**变更历史**：2026-04-12 初版契约

#### 请求体 `CultivationOperationCreateRequest`

| 字段 | 类型 | 必填 | 说明 | 校验 |
|------|------|------|------|------|
| `batchId` | number | ✅ | 种植批次 ID | 非空，批次必须存在 |
| `operationType` | number | ✅ | 操作类型码，见 OperationType | 非空，合法枚举值 |
| `operateDate` | string | ✅ | 操作日期 `yyyy-MM-dd` | 非空 |
| `quantity` | number | ❌ | 用量 | > 0 |
| `unit` | string | ❌ | 单位 | 长度 ≤ 20 |
| `material` | string | ❌ | 使用材料 | 长度 ≤ 200 |
| `operatorId` | number | ❌ | 操作人 ID | — |
| `remark` | string | ❌ | 备注 | 长度 ≤ 500 |

```typescript
export interface CultivationOperationCreateRequest {
  batchId: number;
  operationType: number;
  operateDate: string;
  quantity?: number;
  unit?: string;
  material?: string;
  operatorId?: number;
  remark?: string;
}
```

#### 响应体

`data: CultivationOperationResponse`

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 6002,
    "batchId": 4001,
    "batchCode": "CB202604120001",
    "operationType": { "code": 2, "desc": "打药" },
    "operateDate": "2026-04-12",
    "quantity": 50.0,
    "unit": "L",
    "material": "阿维菌素",
    "operatorId": null,
    "operatorName": null,
    "remark": null,
    "createdAt": "2026-04-12 10:00:00",
    "updatedAt": "2026-04-12 10:00:00"
  }
}
```

#### 响应示例（失败 - 操作类型非法）

```json
{ "code": 200502, "message": "操作类型非法", "data": null }
```

---

### 35. 更新农事操作

**URL**：`PUT /api/cultivation/operations/{id}`
**认证**：需要 JWT
**权限**：`cultivation:write`
**错误码**：`200501`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 操作记录 ID |

#### 请求体 `CultivationOperationUpdateRequest`

字段同 `CultivationOperationCreateRequest`，所有字段可选（`batchId` 不可更改）。

```typescript
export interface CultivationOperationUpdateRequest {
  operationType?: number;
  operateDate?: string;
  quantity?: number;
  unit?: string;
  material?: string;
  operatorId?: number;
  remark?: string;
}
```

#### 响应体

`data: CultivationOperationResponse`

#### 响应示例（成功）

```json
{ "code": 200, "message": "操作成功", "data": { "id": 6001, "quantity": 250.0, "..." : "..." } }
```

#### 响应示例（失败）

```json
{ "code": 200501, "message": "操作记录不存在", "data": null }
```

---

### 36. 删除农事操作

**URL**：`DELETE /api/cultivation/operations/{id}`
**认证**：需要 JWT
**权限**：`cultivation:write`
**错误码**：`200501`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 操作记录 ID |

**业务规则**：软删除。

#### 响应体

`data: null`

#### 响应示例（成功）

```json
{ "code": 200, "message": "操作成功", "data": null }
```

#### 响应示例（失败）

```json
{ "code": 200501, "message": "操作记录不存在", "data": null }
```

---

## §8 成熟度与采收推荐 MaturityController

### 37. 录入成熟度采样

**URL**：`POST /api/planting/maturity/record`
**认证**：需要 JWT
**权限**：`maturity:write`
**错误码**：`200001`
**变更历史**：2026-04-12 初版契约

#### 请求体 `MaturityRecordCreateRequest`

| 字段 | 类型 | 必填 | 说明 | 校验 |
|------|------|------|------|------|
| `orchardId` | number | ✅ | 果园 ID | 非空 |
| `sampleDate` | string | ✅ | 采样日期 `yyyy-MM-dd` | 非空 |
| `brix` | number | ❌ | 糖度（°Brix） | 0~30 |
| `firmness` | number | ❌ | 硬度（kgf/cm²） | 0~20 |
| `color` | number | ❌ | 着色率（%） | 0~100 |
| `remark` | string | ❌ | 备注 | 长度 ≤ 500 |

```typescript
export interface MaturityRecordCreateRequest {
  orchardId: number;
  sampleDate: string;
  brix?: number;
  firmness?: number;
  color?: number;
  remark?: string;
}
```

#### 响应体

`data: MaturityRecordResponse`

`MaturityRecordResponse` 字段：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 记录 ID |
| `orchardId` | number | ✅ | 果园 ID |
| `sampleDate` | string | ✅ | 采样日期 `yyyy-MM-dd` |
| `brix` | number \| null | ❌ | 糖度 |
| `firmness` | number \| null | ❌ | 硬度 |
| `color` | number \| null | ❌ | 着色率 |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface MaturityRecordResponse {
  id: number;
  orchardId: number;
  sampleDate: string;
  brix: number | null;
  firmness: number | null;
  color: number | null;
  remark: string | null;
  createdAt: string;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 7001,
    "orchardId": 1024,
    "sampleDate": "2026-10-01",
    "brix": 12.8,
    "firmness": 8.2,
    "color": 75,
    "remark": null,
    "createdAt": "2026-10-01 09:00:00"
  }
}
```

#### 响应示例（失败）

```json
{ "code": 200001, "message": "果园不存在", "data": null }
```

---

### 38. 采收窗口推荐

**URL**：`GET /api/planting/maturity/recommend/{orchardId}`
**认证**：需要 JWT
**权限**：`maturity:read`
**错误码**：`200001` / `200601` / `200602`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `orchardId` | number | 果园 ID |

**业务规则**：基于品种成熟度标准 + 最近采样数据智能推荐采收窗口，需要有历史采样记录（`200601`）和品种标准（`200602`）。

#### 响应体 `MaturityRecommendResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `orchardId` | number | ✅ | 果园 ID |
| `recommendedDate` | string | ✅ | 推荐采收日期 `yyyy-MM-dd` |
| `windowStart` | string | ✅ | 采收窗口开始 `yyyy-MM-dd` |
| `windowEnd` | string | ✅ | 采收窗口结束 `yyyy-MM-dd` |
| `confidence` | number | ✅ | 置信度（0~1） |
| `factors` | object | ✅ | 参考指标 |
| `factors.brix` | number \| null | ❌ | 最新糖度 |
| `factors.firmness` | number \| null | ❌ | 最新硬度 |
| `factors.color` | number \| null | ❌ | 最新着色率 |

```typescript
export interface MaturityRecommendResponse {
  orchardId: number;
  recommendedDate: string;
  windowStart: string;
  windowEnd: string;
  confidence: number;
  factors: {
    brix: number | null;
    firmness: number | null;
    color: number | null;
  };
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "orchardId": 1024,
    "recommendedDate": "2026-10-20",
    "windowStart": "2026-10-18",
    "windowEnd": "2026-10-25",
    "confidence": 0.85,
    "factors": { "brix": 13.2, "firmness": 7.5, "color": 82 }
  }
}
```

#### 响应示例（失败 - 无采样记录）

```json
{ "code": 200601, "message": "成熟度采样记录不存在", "data": null }
```

#### 响应示例（失败 - 无品种标准）

```json
{ "code": 200602, "message": "未找到品种成熟度标准", "data": null }
```

---

### 39. 品种成熟度标准列表

**URL**：`GET /api/planting/maturity/standards`
**认证**：需要 JWT
**权限**：`maturity:read`
**错误码**：无业务错误
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

无（返回全部品种标准）

#### 响应体 `MaturityStandardResponse[]`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 标准 ID |
| `variety` | string | ✅ | 品种名称 |
| `minBrix` | number \| null | ❌ | 最低糖度（°Brix） |
| `minFirmness` | number \| null | ❌ | 最低硬度（kgf/cm²） |
| `minColor` | number \| null | ❌ | 最低着色率（%） |
| `harvestMonthStart` | number \| null | ❌ | 参考采收起始月份 |
| `harvestMonthEnd` | number \| null | ❌ | 参考采收结束月份 |

```typescript
export interface MaturityStandardResponse {
  id: number;
  variety: string;
  minBrix: number | null;
  minFirmness: number | null;
  minColor: number | null;
  harvestMonthStart: number | null;
  harvestMonthEnd: number | null;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "variety": "红富士",
      "minBrix": 12.5,
      "minFirmness": 7.0,
      "minColor": 80,
      "harvestMonthStart": 10,
      "harvestMonthEnd": 11
    }
  ]
}
```

---

### 40. 果园最近采样记录

**URL**：`GET /api/planting/maturity/records/{orchardId}`
**认证**：需要 JWT
**权限**：`maturity:read`
**错误码**：`200001`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `orchardId` | number | 果园 ID |

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `limit` | number | ❌ | 返回记录数量，默认 20 | `20` |

#### 响应体 `MaturityRecordResponse[]`

数组，字段同 [录入成熟度采样](#37-录入成熟度采样) 中的 `MaturityRecordResponse`，按采样日期降序。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 7001,
      "orchardId": 1024,
      "sampleDate": "2026-10-01",
      "brix": 12.8,
      "firmness": 8.2,
      "color": 75,
      "remark": null,
      "createdAt": "2026-10-01 09:00:00"
    }
  ]
}
```

#### 响应示例（失败）

```json
{ "code": 200001, "message": "果园不存在", "data": null }
```

---

## §9 作业计划 TaskPlanController

### 41. 生成作业计划

**URL**：`POST /api/planting/task-plan/generate`
**认证**：需要 JWT
**权限**：`cultivation:write`
**错误码**：`200001` / `200801`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `orchardId` | number | ✅ | 果园 ID | `1024` |
| `months` | number | ❌ | 生成未来 N 个月计划，默认 1 | `3` |

#### 响应体

`data: TaskPlanResponse[]`（新生成的计划列表）

`TaskPlanResponse` 字段：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 计划 ID（雪花） |
| `orchardId` | number | ✅ | 果园 ID |
| `orchardName` | string | ✅ | 果园名称 |
| `taskType` | string | ✅ | 任务类型（中文描述） |
| `plannedDate` | string | ✅ | 计划日期 `yyyy-MM-dd` |
| `status` | `EnumValue<number>` | ✅ | 状态，见 TaskPlanStatus |
| `actualOperationId` | number \| null | ❌ | 关联实际操作记录 ID |
| `skipReason` | string \| null | ❌ | 跳过原因 |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface TaskPlanResponse {
  id: number;
  orchardId: number;
  orchardName: string;
  taskType: string;
  plannedDate: string;
  status: EnumValue<number>;
  actualOperationId: number | null;
  skipReason: string | null;
  remark: string | null;
  createdAt: string;
  updatedAt: string;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 8001,
      "orchardId": 1024,
      "orchardName": "红富士1号园",
      "taskType": "春季施肥",
      "plannedDate": "2026-04-15",
      "status": { "code": 1, "desc": "已规划" },
      "actualOperationId": null,
      "skipReason": null,
      "remark": null,
      "createdAt": "2026-04-12 10:00:00",
      "updatedAt": "2026-04-12 10:00:00"
    }
  ]
}
```

#### 响应示例（失败 - 参数非法）

```json
{ "code": 200801, "message": "分析参数非法", "data": null }
```

---

### 42. 查询计划列表

**URL**：`GET /api/planting/task-plan/list`
**认证**：需要 JWT
**权限**：`cultivation:read`
**错误码**：`200001`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `orchardId` | number | ✅ | 果园 ID | `1024` |
| `from` | string | ✅ | 起始日期 `yyyy-MM-dd` | `2026-04-01` |
| `to` | string | ✅ | 结束日期 `yyyy-MM-dd` | `2026-04-30` |

```typescript
export interface TaskPlanListRequest {
  orchardId: number;
  from: string;
  to: string;
}
```

#### 响应体 `TaskPlanResponse[]`

数组，字段同 [生成作业计划](#41-生成作业计划) 中的 `TaskPlanResponse`，按计划日期升序。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 8001,
      "orchardId": 1024,
      "orchardName": "红富士1号园",
      "taskType": "春季施肥",
      "plannedDate": "2026-04-15",
      "status": { "code": 1, "desc": "已规划" },
      "actualOperationId": null,
      "skipReason": null,
      "remark": null,
      "createdAt": "2026-04-12 10:00:00",
      "updatedAt": "2026-04-12 10:00:00"
    }
  ]
}
```

---

### 43. 标记计划完成

**URL**：`POST /api/planting/task-plan/{id}/done`
**认证**：需要 JWT
**权限**：`cultivation:write`
**错误码**：`200701` / `200702`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 计划 ID |

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `actualOperationId` | number | ❌ | 关联实际操作记录 ID | `6001` |

**业务规则**：只有已规划（`status=1`）的计划可标记完成，状态流转到已完成（`status=2`）。

#### 响应体

`data: TaskPlanResponse`

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 8001,
    "orchardId": 1024,
    "orchardName": "红富士1号园",
    "taskType": "春季施肥",
    "plannedDate": "2026-04-15",
    "status": { "code": 2, "desc": "已完成" },
    "actualOperationId": 6001,
    "skipReason": null,
    "remark": null,
    "createdAt": "2026-04-12 10:00:00",
    "updatedAt": "2026-04-15 16:00:00"
  }
}
```

#### 响应示例（失败 - 计划不存在）

```json
{ "code": 200701, "message": "作业计划不存在", "data": null }
```

#### 响应示例（失败 - 状态不允许）

```json
{ "code": 200702, "message": "计划状态不允许此操作", "data": null }
```

---

### 44. 跳过计划

**URL**：`POST /api/planting/task-plan/{id}/skip`
**认证**：需要 JWT
**权限**：`cultivation:write`
**错误码**：`200701` / `200702`
**变更历史**：2026-04-12 初版契约

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 计划 ID |

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `reason` | string | ❌ | 跳过原因 | `天气原因推迟` |

**业务规则**：只有已规划（`status=1`）的计划可跳过，状态流转到已跳过（`status=3`）。

#### 响应体

`data: TaskPlanResponse`

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 8002,
    "orchardId": 1024,
    "orchardName": "红富士1号园",
    "taskType": "喷施农药",
    "plannedDate": "2026-04-20",
    "status": { "code": 3, "desc": "已跳过" },
    "actualOperationId": null,
    "skipReason": "天气原因推迟",
    "remark": null,
    "createdAt": "2026-04-12 10:00:00",
    "updatedAt": "2026-04-20 08:00:00"
  }
}
```

#### 响应示例（失败）

```json
{ "code": 200702, "message": "计划状态不允许此操作", "data": null }
```

---

## §10 种植分析 PlantingAnalysisController

### 45. 亩产排名

**URL**：`GET /api/planting/analysis/yield`
**认证**：需要 JWT
**权限**：`analysis:read`
**错误码**：`200801`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `variety` | string | ❌ | 品种筛选 | `红富士` |
| `year` | number | ❌ | 年份筛选 | `2026` |

```typescript
export interface YieldAnalysisRequest {
  variety?: string;
  year?: number;
}
```

#### 响应体 `YieldPerMuResponse[]`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `orchardId` | number | ✅ | 果园 ID |
| `orchardName` | string | ✅ | 果园名称 |
| `variety` | string | ✅ | 品种 |
| `areaMu` | number | ✅ | 面积（亩） |
| `totalYield` | number | ✅ | 总产量（kg） |
| `yieldPerMu` | number | ✅ | 亩产（kg/亩）= totalYield / areaMu |

```typescript
export interface YieldPerMuResponse {
  orchardId: number;
  orchardName: string;
  variety: string;
  areaMu: number;
  totalYield: number;
  yieldPerMu: number;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "orchardId": 1024,
      "orchardName": "红富士1号园",
      "variety": "红富士",
      "areaMu": 15.5,
      "totalYield": 62000.0,
      "yieldPerMu": 4000.0
    },
    {
      "orchardId": 1025,
      "orchardName": "嘎拉2号园",
      "variety": "嘎拉",
      "areaMu": 8.0,
      "totalYield": 28000.0,
      "yieldPerMu": 3500.0
    }
  ]
}
```

---

### 46. 优果率统计

**URL**：`GET /api/planting/analysis/premium`
**认证**：需要 JWT
**权限**：`analysis:read`
**错误码**：`200801`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `variety` | string | ❌ | 品种筛选 | `红富士` |
| `year` | number | ❌ | 年份筛选 | `2026` |

#### 响应体 `PremiumRateResponse[]`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `orchardId` | number | ✅ | 果园 ID |
| `orchardName` | string | ✅ | 果园名称 |
| `gradeAWeight` | number | ✅ | A 级果重量（kg） |
| `totalWeight` | number | ✅ | 总重量（kg） |
| `premiumRate` | number | ✅ | 优果率（0~1） |

```typescript
export interface PremiumRateResponse {
  orchardId: number;
  orchardName: string;
  gradeAWeight: number;
  totalWeight: number;
  premiumRate: number;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "orchardId": 1024,
      "orchardName": "红富士1号园",
      "gradeAWeight": 49600.0,
      "totalWeight": 62000.0,
      "premiumRate": 0.8
    }
  ]
}
```

---

### 47. 病虫害发生率

**URL**：`GET /api/planting/analysis/pest`
**认证**：需要 JWT
**权限**：`analysis:read`
**错误码**：`200801`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `year` | number | ❌ | 年份筛选 | `2026` |

#### 响应体 `PestIncidenceResponse[]`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `orchardId` | number | ✅ | 果园 ID |
| `orchardName` | string | ✅ | 果园名称 |
| `totalOperations` | number | ✅ | 总操作次数 |
| `pestOperations` | number | ✅ | 农药操作次数 |
| `incidenceRate` | number | ✅ | 发生率（0~1） |

```typescript
export interface PestIncidenceResponse {
  orchardId: number;
  orchardName: string;
  totalOperations: number;
  pestOperations: number;
  incidenceRate: number;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "orchardId": 1024,
      "orchardName": "红富士1号园",
      "totalOperations": 20,
      "pestOperations": 4,
      "incidenceRate": 0.2
    }
  ]
}
```

---

### 48. 地块对比分析

**URL**：`GET /api/planting/analysis/compare`
**认证**：需要 JWT
**权限**：`analysis:read`
**错误码**：`200001` / `200801`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `orchardIds` | string | ✅ | 果园 ID 列表，逗号分隔 | `1024,1025,1026` |

**注**：`orchardIds` 为逗号分隔字符串，后端 `@RequestParam List<Long>`。

#### 响应体 `PlotComparisonResponse[]`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `orchardId` | number | ✅ | 果园 ID |
| `orchardName` | string | ✅ | 果园名称 |
| `yield` | `YieldPerMuResponse` \| null | ❌ | 亩产数据 |
| `premium` | `PremiumRateResponse` \| null | ❌ | 优果率数据 |
| `pest` | `PestIncidenceResponse` \| null | ❌ | 病虫害数据 |

```typescript
export interface PlotComparisonResponse {
  orchardId: number;
  orchardName: string;
  yield: YieldPerMuResponse | null;
  premium: PremiumRateResponse | null;
  pest: PestIncidenceResponse | null;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "orchardId": 1024,
      "orchardName": "红富士1号园",
      "yield": { "orchardId": 1024, "orchardName": "红富士1号园", "variety": "红富士", "areaMu": 15.5, "totalYield": 62000.0, "yieldPerMu": 4000.0 },
      "premium": { "orchardId": 1024, "orchardName": "红富士1号园", "gradeAWeight": 49600.0, "totalWeight": 62000.0, "premiumRate": 0.8 },
      "pest": { "orchardId": 1024, "orchardName": "红富士1号园", "totalOperations": 20, "pestOperations": 4, "incidenceRate": 0.2 }
    }
  ]
}
```

#### 响应示例（失败 - 参数非法）

```json
{ "code": 200801, "message": "分析参数非法", "data": null }
```

---

## 错误码表（种植模块）

| code | message | 触发场景 |
|------|---------|---------|
| `200001` | 果园不存在 | 查询/更新/删除果园时 DB 无记录 |
| `200002` | 果园编号已存在 | 自动生成编号冲突（极少见） |
| `200003` | 果园状态不允许此操作 | 对特定状态果园执行受限操作 |
| `200101` | 农户不存在 | 查询/更新/删除农户时 DB 无记录 |
| `200102` | 农户手机号重复 | 创建或更新农户时手机号已被使用 |
| `200201` | 农事记录不存在 | 查询/更新/删除农事记录时 DB 无记录 |
| `200301` | 采收批次不存在 | 查询/更新/删除采收批次时 DB 无记录 |
| `200302` | 批次状态不允许确认 | 非草稿状态批次执行确认或修改操作 |
| `200303` | 采收重量与果园面积不匹配 | 录入采收量严重超出合理范围（业务校验） |
| `200401` | 种植批次不存在 | 查询/更新/删除种植批次时 DB 无记录 |
| `200402` | 批次编号已存在 | 自动生成批次编号冲突（极少见） |
| `200501` | 操作记录不存在 | 查询/更新/删除农事操作记录时 DB 无记录 |
| `200502` | 操作类型非法 | 传入的 `operationType` 不在枚举合法值范围内 |
| `200601` | 成熟度采样记录不存在 | 果园无任何采样记录，无法推荐采收窗口 |
| `200602` | 未找到品种成熟度标准 | 系统未维护该品种的成熟度标准数据 |
| `200701` | 作业计划不存在 | 查询/更新作业计划时 DB 无记录 |
| `200702` | 计划状态不允许此操作 | 非已规划状态计划执行完成或跳过操作 |
| `200801` | 分析参数非法 | 传入分析接口的参数格式错误或范围非法 |
| `200901` | GeoJSON 格式错误 | 传入的 `boundaryGeojson` 无法解析为合法 GeoJSON Polygon |
| `200902` | 坐标超出范围 | bbox 查询的经纬度超出 (-180~180, -90~90) |

> 错误码枚举实现：`apple-module-planting/src/main/java/com/apple/chain/planting/enums/PlantingErrorCode.java`（若该文件不存在，由后端按本契约创建）

---

## 枚举声明

> **全局约定**：所有枚举在 HTTP 响应中**必须**以 `{code, desc}` 对象形式序列化（见 CONVENTIONS §4）。请求参数中传递 `code`（整数）。

### OrchardStatus（果园状态）

```typescript
export const OrchardStatus = {
  NORMAL:    { code: 1, desc: "正常" },
  DORMANT:   { code: 2, desc: "休眠" },
  HARVESTED: { code: 3, desc: "已采收" },
} as const;

export type OrchardStatusCode = 1 | 2 | 3;
```

> **后端迁移提示**：当前后端 `pt_orchard.status` 为 `String` 类型（`NORMAL`/`DORMANT`/`HARVESTED`）。下次修改 `OrchardDO` 时必须改为 `Integer` 类型并实现 `BaseEnum` 接口，序列化输出 `{code, desc}` 对象（见 `docs/contract/BACKEND-CONTRACT.md`）。

### HarvestBatchStatus（采收批次状态）

```typescript
export const HarvestBatchStatus = {
  DRAFT:     { code: 1, desc: "草稿" },
  CONFIRMED: { code: 2, desc: "已确认" },
} as const;

export type HarvestBatchStatusCode = 1 | 2;
```

> **后端迁移提示**：当前后端 `pt_harvest_batch.status` 为 `String` 类型（`DRAFT`/`CONFIRMED`）。下次修改 `HarvestBatchDO` 时必须改为 `Integer` 类型并实现 `BaseEnum` 接口，序列化输出 `{code, desc}` 对象。

### TaskPlanStatus（作业计划状态）

```typescript
export const TaskPlanStatus = {
  PLANNED: { code: 1, desc: "已规划" },
  DONE:    { code: 2, desc: "已完成" },
  SKIPPED: { code: 3, desc: "已跳过" },
} as const;

export type TaskPlanStatusCode = 1 | 2 | 3;
```

> **后端迁移提示**：当前后端作业计划状态为 `String` 类型（`PLANNED`/`DONE`/`SKIPPED`）。下次修改 `TaskPlanDO` 时必须改为 `Integer` 类型并实现 `BaseEnum` 接口，序列化输出 `{code, desc}` 对象。

### OperationType（农事操作类型）

```typescript
export const OperationType = {
  FERTILIZE: { code: 1, desc: "施肥" },
  PESTICIDE: { code: 2, desc: "打药" },
  PRUNE:     { code: 3, desc: "修剪" },
  HARVEST:   { code: 4, desc: "采收" },
  IRRIGATE:  { code: 5, desc: "灌溉" },
} as const;

export type OperationTypeCode = 1 | 2 | 3 | 4 | 5;
```

> **后端迁移提示**：当前后端 `pt_cultivation_operation.operation_type` 为 `String` 类型（`fertilize`/`pesticide`/`prune`/`harvest`/`irrigate`）。下次修改 `CultivationOperationDO` 时必须改为 `Integer` 类型并实现 `BaseEnum` 接口，序列化输出 `{code, desc}` 对象。

### GrowthRecordType（农事记录类型）

```typescript
export const GrowthRecordType = {
  FERTILIZE: { code: 1, desc: "施肥" },
  PESTICIDE: { code: 2, desc: "打药" },
  PRUNE:     { code: 3, desc: "修剪" },
  HARVEST:   { code: 4, desc: "采收" },
  IRRIGATE:  { code: 5, desc: "灌溉" },
  INSPECTION:{ code: 6, desc: "巡检" },
  OTHER:     { code: 7, desc: "其他" },
} as const;

export type GrowthRecordTypeCode = 1 | 2 | 3 | 4 | 5 | 6 | 7;
```

> **后端迁移提示**：当前后端 `pt_growth_record.record_type` 为 `String` 类型（`fertilize`/`pesticide`/`prune`/`harvest`/`irrigate` 等）。下次修改 `GrowthRecordDO` 时必须改为 `Integer` 类型并实现 `BaseEnum` 接口，序列化输出 `{code, desc}` 对象。
