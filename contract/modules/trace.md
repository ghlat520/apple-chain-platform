# M3 溯源 (Trace) 模块契约

## 元信息

| 项目 | 值 |
|------|-----|
| 模块 | M3 溯源管理 |
| URL 前缀 | `/api/trace`、`/api/trace/batches`、`/api/trace/code`、`/api/trace/anomaly` |
| 错误码段 | `300000 ~ 399999`（与 `contract/CONVENTIONS.md:135` 一致） |
| 最近更新 | 2026-04-12 |
| 权威源 | 本文件 |
| 后端实现 | `apple-module-trace` |
| 关联契约 | `chain.md`（区块链上链）/ `planting.md`（orchard/farmer/harvest_batch）/ `warehouse.md`（仓储）/ `trade.md`（交易） |

> **路径权威性声明**：本文件 4 个 Controller 的 `@RequestMapping` 路径是权威。区块链上链 Controller（`/api/trace/chain/*`、`/api/admin/chain/*`）归属 P0-6 `chain.md`，不写入本文件；本文件末尾"关联契约"章节指向 `chain.md`。

---

## 1. 接口索引

### 1.1 溯源链 TraceController（`/api/trace`）

| # | 接口名 | URL | 方法 | 认证 | 权限 |
|---|--------|-----|------|------|------|
| 1 | [溯源链列表](#21-溯源链列表) | `/api/trace/list` | GET | JWT | `trace:read` |
| 2 | [溯源详情](#22-溯源详情) | `/api/trace/{traceCode}` | GET | JWT | `trace:read` |
| 3 | [公众扫码查询](#23-公众扫码查询) | `/api/trace/scan/{traceCode}` | GET | **公开** | — |
| 4 | [公众扫码全链路](#24-公众扫码全链路) | `/api/trace/scan/{traceCode}/full` | GET | **公开** | — |
| 5 | [生成溯源二维码](#25-生成溯源二维码) | `/api/trace/qrcode/{traceCode}` | GET | JWT | `trace:read` |
| 6 | [添加溯源节点](#26-添加溯源节点) | `/api/trace/node` | POST | JWT | `trace:write` |
| 7 | [导出溯源链 CSV](#27-导出溯源链-csv) | `/api/trace/export` | GET | JWT | `trace:export` |

### 1.2 溯源批次 TraceBatchController（`/api/trace/batches`）

| # | 接口名 | URL | 方法 | 认证 | 权限 |
|---|--------|-----|------|------|------|
| 8 | [批次列表](#31-批次列表) | `/api/trace/batches` | GET | JWT | `trace:read` |
| 9 | [批次详情](#32-批次详情) | `/api/trace/batches/{id}` | GET | JWT | `trace:read` |
| 10 | [公众扫码批次查询](#33-公众扫码批次查询) | `/api/trace/batches/scan/{batchCode}` | GET | **公开** | — |
| 11 | [创建溯源批次](#34-创建溯源批次) | `/api/trace/batches` | POST | JWT | `trace:write` |
| 12 | [更新批次状态](#35-更新批次状态) | `/api/trace/batches/{id}/status` | PUT | JWT | `trace:write` |
| 13 | [删除批次](#36-删除批次) | `/api/trace/batches/{id}` | DELETE | JWT | `trace:delete` |
| 14 | [添加溯源记录](#37-添加溯源记录) | `/api/trace/batches/{id}/records` | POST | JWT | `trace:write` |
| 15 | [查询批次溯源记录](#38-查询批次溯源记录) | `/api/trace/batches/{id}/records` | GET | JWT | `trace:read` |
| 16 | [导出批次 CSV](#39-导出批次-csv) | `/api/trace/batches/export` | GET | JWT | `trace:export` |

### 1.3 三级溯源码 TraceCodeController（`/api/trace/code`）

| # | 接口名 | URL | 方法 | 认证 | 权限 |
|---|--------|-----|------|------|------|
| 17 | [生成 BOX 级编码](#41-生成-box-级编码) | `/api/trace/code/generate-box` | POST | JWT | `trace:write` |
| 18 | [生成 FRUIT 级编码](#42-生成-fruit-级编码) | `/api/trace/code/generate-fruit` | POST | JWT | `trace:write` |
| 19 | [校验溯源码](#43-校验溯源码) | `/api/trace/code/verify/{code}` | GET | JWT | `trace:read` |
| 20 | [VDP 印刷文件导出](#44-vdp-印刷文件导出) | `/api/trace/code/vdp-export/{batchId}` | GET | JWT | `trace:export` |

### 1.4 异常追溯 AnomalyTraceController（`/api/trace/anomaly`）

| # | 接口名 | URL | 方法 | 认证 | 权限 |
|---|--------|-----|------|------|------|
| 21 | [上报异常](#51-上报异常) | `/api/trace/anomaly` | POST | JWT | `trace:write` |
| 22 | [异常列表](#52-异常列表) | `/api/trace/anomaly` | GET | JWT | `trace:read` |
| 23 | [异常详情](#53-异常详情) | `/api/trace/anomaly/{id}` | GET | JWT | `trace:read` |
| 24 | [开始调查](#54-开始调查) | `/api/trace/anomaly/{id}/investigate` | PUT | JWT | `trace:write` |
| 25 | [解决异常](#55-解决异常) | `/api/trace/anomaly/{id}/resolve` | PUT | JWT | `trace:write` |
| 26 | [影响分析](#56-影响分析) | `/api/trace/anomaly/impact/{traceCode}` | GET | JWT | `trace:read` |

**公开接口（无需认证）共 3 个**：#3、#4、#10

---

## 2. 溯源链 TraceController（`/api/trace`）

### 2.1 溯源链列表

**URL**：`GET /api/trace/list`
**认证**：需要 JWT Bearer Token
**权限**：`trace:read`
**错误码**：`300900`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `page` | number | ❌ | 页码，从 1 开始，默认 1 | `1` |
| `size` | number | ❌ | 每页数量，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 溯源码/产品类型模糊搜索 | `TCHB` |
| `status` | string | ❌ | 状态筛选，传 `TraceChainStatus.code`（见枚举） | `PLANTED` |

```typescript
export interface TraceChainPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;
}
```

#### 响应体 `PageResult<TraceChainResponse>`

| 字段 | 类型 | 必返 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 溯源链 ID | `1001` |
| `traceCode` | string | ✅ | 唯一溯源码，如 TCHB20250101001 | `TCHB20260101001` |
| `productType` | string | ✅ | 产品类型 | `红富士` |
| `batchNo` | string | ✅ | 关联采收批次编号 | `BN20260101001` |
| `orchardId` | number | ✅ | 关联果园 ID | `101` |
| `farmerId` | number | ✅ | 关联农户 ID | `201` |
| `currentStatus` | EnumValue\<string\> | ✅ | 当前溯源状态（见 TraceChainStatus） | `{"code":"PLANTED","desc":"已种植"}` |
| `chainStatus` | EnumValue\<number\> | ✅ | 上链状态（见 ChainStatus） | `{"code":0,"desc":"待上链"}` |
| `dataHash` | string | ❌ | SHA-256 数据完整性哈希 | `abc123...` |
| `remark` | string | ❌ | 备注 | `— ` |
| `createdAt` | string | ✅ | 创建时间 | `2026-01-01 08:00:00` |
| `updatedAt` | string | ✅ | 更新时间 | `2026-01-10 12:00:00` |

```typescript
export interface TraceChainResponse {
  id: number;
  traceCode: string;
  productType: string;
  batchNo: string;
  orchardId: number;
  farmerId: number;
  currentStatus: EnumValue<string>;
  chainStatus: EnumValue<number>;
  dataHash?: string;
  remark?: string;
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
        "id": 1001,
        "traceCode": "TCHB20260101001",
        "productType": "红富士",
        "batchNo": "BN20260101001",
        "orchardId": 101,
        "farmerId": 201,
        "currentStatus": { "code": "PLANTED", "desc": "已种植" },
        "chainStatus": { "code": 0, "desc": "待上链" },
        "dataHash": "a1b2c3d4e5f6...",
        "remark": null,
        "createdAt": "2026-01-01 08:00:00",
        "updatedAt": "2026-01-01 08:00:00"
      }
    ],
    "total": 52,
    "current": 1,
    "size": 10
  }
}
```

#### 业务规则

- 支持 `keyword`（溯源码/产品类型）和 `status` 组合筛选
- 按 `createdAt` 倒序排列
- 分页参数遵循全局 `PageParam` 约定

#### 实现提示

- Controller：`TraceController#list`
- 当前实现直接返回 `TraceChain` Entity（String status 字段），契约目标为 `EnumValue<string>` 对象，后端需迁移（见 §8.1）

---

### 2.2 溯源详情

**URL**：`GET /api/trace/{traceCode}`
**认证**：需要 JWT Bearer Token
**权限**：`trace:read`
**错误码**：`300001`、`300900`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `traceCode` | string | ✅ | 溯源码 | `TCHB20260101001` |

#### 响应体 `TraceDetailResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `chain` | TraceChainResponse | ✅ | 溯源链基本信息 |
| `nodes` | TraceNodeResponse[] | ✅ | 时间轴节点列表，按 `nodeTime` 升序 |

```typescript
export interface TraceDetailResponse {
  chain: TraceChainResponse;
  nodes: TraceNodeResponse[];
}

export interface TraceNodeResponse {
  id: number;
  traceCode: string;
  nodeType: EnumValue<string>;
  nodeTime: string;
  operatorId: number;
  operatorName: string;
  summary: string;
  detail?: string;
  location?: string;
  dataHash?: string;
  createdAt: string;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "chain": {
      "id": 1001,
      "traceCode": "TCHB20260101001",
      "productType": "红富士",
      "batchNo": "BN20260101001",
      "orchardId": 101,
      "farmerId": 201,
      "currentStatus": { "code": "PLANTED", "desc": "已种植" },
      "chainStatus": { "code": 0, "desc": "待上链" },
      "createdAt": "2026-01-01 08:00:00",
      "updatedAt": "2026-01-15 10:00:00"
    },
    "nodes": [
      {
        "id": 2001,
        "traceCode": "TCHB20260101001",
        "nodeType": { "code": "PLANT", "desc": "种植" },
        "nodeTime": "2026-01-01 07:30:00",
        "operatorId": 301,
        "operatorName": "张三",
        "summary": "完成春季定植",
        "location": "洛川县苹果园A区",
        "createdAt": "2026-01-01 08:00:00"
      }
    ]
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 300001,
  "message": "溯源链不存在",
  "data": null
}
```

#### 业务规则

- `traceCode` 不存在时返回 `300001`
- `nodes` 按 `nodeTime` 升序排列，构成时间轴

#### 实现提示

- Controller：`TraceController#detail`，返回 `Map<String, Object>`
- 契约目标结构化为 `TraceDetailResponse`（含 chain + nodes 两个子对象）

---

### 2.3 公众扫码查询

**URL**：`GET /api/trace/scan/{traceCode}`
**认证**：**公开，无需 JWT**
**错误码**：`300001`、`300002`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `traceCode` | string | ✅ | 溯源码（来自二维码扫描） | `TCHB20260101001` |

#### 响应体 `TraceScanResponse`

公众可见的精简信息，不包含内部 ID 或敏感字段。

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `traceCode` | string | ✅ | 溯源码 |
| `productType` | string | ✅ | 产品类型 |
| `currentStatus` | EnumValue\<string\> | ✅ | 当前溯源状态 |
| `orchardName` | string | ❌ | 果园名称（跨模块查询） |
| `farmerName` | string | ❌ | 农户姓名（脱敏） |
| `batchNo` | string | ✅ | 批次编号 |
| `nodes` | TraceNodePublicResponse[] | ✅ | 公众可见节点列表 |

```typescript
export interface TraceScanResponse {
  traceCode: string;
  productType: string;
  currentStatus: EnumValue<string>;
  orchardName?: string;
  farmerName?: string;
  batchNo: string;
  nodes: TraceNodePublicResponse[];
}

export interface TraceNodePublicResponse {
  nodeType: EnumValue<string>;
  nodeTime: string;
  operatorName: string;
  summary: string;
  location?: string;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "traceCode": "TCHB20260101001",
    "productType": "红富士",
    "currentStatus": { "code": "HARVESTED", "desc": "已采收" },
    "orchardName": "洛川县富民苹果园",
    "farmerName": "张**",
    "batchNo": "BN20260101001",
    "nodes": [
      {
        "nodeType": { "code": "PLANT", "desc": "种植" },
        "nodeTime": "2026-01-01 07:30:00",
        "operatorName": "张三",
        "summary": "完成春季定植",
        "location": "洛川县苹果园A区"
      },
      {
        "nodeType": { "code": "HARVEST", "desc": "采收" },
        "nodeTime": "2026-10-05 06:00:00",
        "operatorName": "张三",
        "summary": "人工采收，优果率 92%",
        "location": "洛川县苹果园A区"
      }
    ]
  }
}
```

#### 业务规则

- 此接口**不需要认证**，面向消费者扫码场景
- 农户手机号等敏感信息必须脱敏（姓名保留姓氏，手机号不返回）
- 需要在后端安全白名单（Spring Security permitAll）中配置该路径

#### 实现提示

- Controller：`TraceController#scan`，当前返回 `Map<String, Object>`
- 契约目标类型为 `TraceScanResponse`，需后端创建 DTO 并对敏感字段脱敏

---

### 2.4 公众扫码全链路

**URL**：`GET /api/trace/scan/{traceCode}/full`
**认证**：**公开，无需 JWT**
**错误码**：`300001`、`300003`、`300900`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `traceCode` | string | ✅ | 溯源码 | `TCHB20260101001` |

#### 响应体 `TraceFullChainResponse`

跨模块聚合响应，整合 planting / trace / warehouse / trade 各模块数据。

| 字段 | 类型 | 必返 | 说明 | 跨模块来源 |
|------|------|------|------|-----------|
| `traceCode` | string | ✅ | 溯源码 | trace |
| `productType` | string | ✅ | 产品类型 | trace |
| `currentStatus` | EnumValue\<string\> | ✅ | 当前溯源状态 | trace |
| `orchard` | OrchardSummary | ❌ | 果园信息 | **planting 模块** |
| `farmer` | FarmerSummary | ❌ | 农户信息（脱敏） | **planting 模块** |
| `harvestBatch` | HarvestBatchSummary | ❌ | 采收批次信息 | **planting 模块** |
| `traceNodes` | TraceNodePublicResponse[] | ✅ | 溯源节点列表 | trace |
| `warehouseInfo` | WarehouseSummary | ❌ | 仓储信息 | **warehouse 模块** |
| `tradeInfo` | TradeSummary | ❌ | 交易信息 | **trade 模块** |

```typescript
export interface TraceFullChainResponse {
  traceCode: string;
  productType: string;
  currentStatus: EnumValue<string>;
  orchard?: OrchardSummary;
  farmer?: FarmerSummary;
  harvestBatch?: HarvestBatchSummary;
  traceNodes: TraceNodePublicResponse[];
  warehouseInfo?: WarehouseSummary;
  tradeInfo?: TradeSummary;
}

export interface OrchardSummary {
  id: number;
  name: string;
  address: string;
  area: number;
}

export interface FarmerSummary {
  id: number;
  name: string;       // 脱敏：保留姓氏
  certNo?: string;    // 脱敏：不返回完整证件号
}

export interface HarvestBatchSummary {
  batchNo: string;
  harvestDate: string;
  variety: string;
  weight: number;
  grade: EnumValue<string>;
}

export interface WarehouseSummary {
  warehouseName: string;
  inboundTime?: string;
  outboundTime?: string;
}

export interface TradeSummary {
  orderNo?: string;
  tradeTime?: string;
  buyerRegion?: string;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "traceCode": "TCHB20260101001",
    "productType": "红富士",
    "currentStatus": { "code": "SOLD", "desc": "已售出" },
    "orchard": {
      "id": 101,
      "name": "洛川县富民苹果园",
      "address": "陕西省延安市洛川县",
      "area": 12.5
    },
    "farmer": {
      "id": 201,
      "name": "张**",
      "certNo": null
    },
    "harvestBatch": {
      "batchNo": "BN20260101001",
      "harvestDate": "2026-10-05 00:00:00",
      "variety": "红富士",
      "weight": 1200.5,
      "grade": { "code": "A", "desc": "特级" }
    },
    "traceNodes": [
      {
        "nodeType": { "code": "PLANT", "desc": "种植" },
        "nodeTime": "2026-01-01 07:30:00",
        "operatorName": "张**",
        "summary": "完成春季定植",
        "location": "洛川县苹果园A区"
      }
    ],
    "warehouseInfo": {
      "warehouseName": "洛川冷链中心1号库",
      "inboundTime": "2026-10-06 14:00:00",
      "outboundTime": "2026-10-10 09:00:00"
    },
    "tradeInfo": {
      "orderNo": "ORD20261010001",
      "tradeTime": "2026-10-10 10:00:00",
      "buyerRegion": "北京市"
    }
  }
}
```

#### 业务规则

- 此接口**不需要认证**，面向消费者全链路溯源场景
- 内部跨模块查询失败（如 warehouse 模块无数据）时，对应字段返回 `null`，整体接口不报错
- 农户敏感信息必须脱敏

#### 实现提示

- Controller：`TraceController#scanFull`，调用 `TraceAggregationService#getFullChain`
- 后端已有 `TraceFullChainVO`（`apple-module-trace/dto/TraceFullChainVO`），契约目标命名对齐为 `TraceFullChainResponse`
- 跨模块查询需防止级联失败：各模块调用使用 try-catch，失败时填 null 字段

---

### 2.5 生成溯源二维码

**URL**：`GET /api/trace/qrcode/{traceCode}`
**认证**：需要 JWT Bearer Token
**权限**：`trace:read`
**响应类型**：`image/png`（**非 R\<T\> 包装**）
**错误码**：`300001`、`300005`
**变更历史**：2026-04-12 初版契约

#### 请求参数

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| `traceCode` | Path | string | ✅ | 溯源码 | `TCHB20260101001` |
| `size` | Query | number | ❌ | 二维码像素尺寸，默认 300 | `300` |

#### 响应说明

- HTTP `Content-Type`：`image/png`
- 响应体为**原始 PNG 二进制流**，不经过 `R<T>` 包装
- 二维码内容指向公众扫码 URL：`/scan/{traceCode}`
- 前端直接将响应体作为图片显示（`<img src="/api/trace/qrcode/TCHB...">` 方式使用）

#### 错误响应（HTTP 404 / 500，JSON 格式）

```json
{
  "code": 300001,
  "message": "溯源链不存在",
  "data": null
}
```

```json
{
  "code": 300005,
  "message": "二维码生成失败",
  "data": null
}
```

#### 业务规则

- 生成前先验证 `traceCode` 是否存在，不存在返回 `300001`
- QR 编码库使用 ZXing（`QRCodeWriter`），输出 `MatrixToImageWriter` → PNG
- 尺寸参数 `size` 范围建议：100～1000，超出范围服务端截断

#### 实现提示

- Controller：`TraceController#qrcode`，`produces = MediaType.IMAGE_PNG_VALUE`
- 已实现，与契约一致，无需迁移

---

### 2.6 添加溯源节点

**URL**：`POST /api/trace/node`
**认证**：需要 JWT Bearer Token
**权限**：`trace:write`
**错误码**：`300001`、`300004`、`300900`
**变更历史**：2026-04-12 初版契约

#### 请求体 `TraceNodeCreateRequest`

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `traceCode` | string | ✅ | 关联溯源码 | `TCHB20260101001` |
| `nodeType` | string | ✅ | 节点类型 code（见 TraceNodeType 枚举） | `HARVEST` |
| `nodeTime` | string | ✅ | 节点发生时间，格式 `yyyy-MM-dd HH:mm:ss` | `2026-10-05 06:00:00` |
| `operatorId` | number | ✅ | 操作人 ID | `301` |
| `operatorName` | string | ✅ | 操作人姓名 | `张三` |
| `summary` | string | ✅ | 节点摘要（面向公众展示） | `人工采收，优果率92%` |
| `detail` | string | ❌ | JSON 格式详细信息（任意扩展字段） | `{"temperature":18}` |
| `location` | string | ❌ | 操作地点 | `洛川县苹果园A区` |

```typescript
export interface TraceNodeCreateRequest {
  traceCode: string;
  nodeType: string;        // TraceNodeType.code
  nodeTime: string;        // yyyy-MM-dd HH:mm:ss
  operatorId: number;
  operatorName: string;
  summary: string;
  detail?: string;
  location?: string;
}
```

#### 响应体 `TraceNodeResponse`

（字段见 §2.2 `TraceNodeResponse`）

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "节点添加成功",
  "data": {
    "id": 2002,
    "traceCode": "TCHB20260101001",
    "nodeType": { "code": "HARVEST", "desc": "采收" },
    "nodeTime": "2026-10-05 06:00:00",
    "operatorId": 301,
    "operatorName": "张三",
    "summary": "人工采收，优果率92%",
    "location": "洛川县苹果园A区",
    "dataHash": "d4e5f6a1b2c3...",
    "createdAt": "2026-10-05 08:00:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 300001,
  "message": "溯源链不存在",
  "data": null
}
```

#### 业务规则

- 添加节点前须验证 `traceCode` 存在，不存在返回 `300001`
- 节点创建后自动计算并存储 `dataHash`（SHA-256）
- `nodeType` 值必须在 `TraceNodeType` 枚举中，否则返回 `300900`
- `nodeTime` 不可晚于当前时间（防止未来时间伪造）

#### 实现提示

- Controller：`TraceController#addNode`，当前接收 `TraceNode` Entity
- 契约目标：后端需创建 `TraceNodeCreateRequest` DTO 替代 Entity（见 §8.2）

---

### 2.7 导出溯源链 CSV

**URL**：`GET /api/trace/export`
**认证**：需要 JWT Bearer Token
**权限**：`trace:export`
**响应类型**：`text/csv`（**非 R\<T\> 包装**）
**错误码**：`300900`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `keyword` | string | ❌ | 关键词筛选 | `红富士` |
| `status` | string | ❌ | 状态筛选 | `PLANTED` |

#### 响应说明

- HTTP `Content-Type`：`text/csv;charset=UTF-8`
- `Content-Disposition`：`attachment; filename="trace_chains_yyyyMMdd.csv"`
- 响应体为 **CSV 文件流**，不经过 `R<T>` 包装
- CSV 列：溯源码、产品类型、批次编号、果园ID、农户ID、当前状态、上链状态、创建时间

#### 业务规则

- 导出数据量上限建议 10000 条，超出提示用户缩小筛选范围
- CSV 编码 UTF-8 BOM（兼容 Excel 直接打开）

#### 实现提示

- Controller：`TraceController#export`，通过 `HttpServletResponse` 流式输出
- 已实现，与契约基本一致

---

## 3. 溯源批次 TraceBatchController（`/api/trace/batches`）

### 3.1 批次列表

**URL**：`GET /api/trace/batches`
**认证**：需要 JWT Bearer Token
**权限**：`trace:read`
**错误码**：`300900`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `page` | number | ❌ | 页码，默认 1 | `1` |
| `size` | number | ❌ | 每页，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 批次编号/果园名称模糊搜索 | `TB20260101` |
| `status` | string | ❌ | 批次状态筛选（见 TraceBatchStatus） | `CREATED` |

```typescript
export interface TraceBatchPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;
}
```

#### 响应体 `PageResult<TraceBatchResponse>`

| 字段 | 类型 | 必返 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 批次 ID | `3001` |
| `batchCode` | string | ✅ | 自动生成批次编号：TB+yyyyMMdd+4位序 | `TB202601010001` |
| `orchardId` | number | ✅ | 关联果园 ID | `101` |
| `orchardName` | string | ❌ | 果园名称（冗余字段） | `洛川富民果园` |
| `harvestDate` | string | ✅ | 采收日期 `yyyy-MM-dd HH:mm:ss` | `2026-10-05 00:00:00` |
| `variety` | string | ❌ | 品种 | `红富士` |
| `grade` | EnumValue\<string\> | ❌ | 等级（见 TraceBatchGrade） | `{"code":"A","desc":"特级"}` |
| `weight` | number | ❌ | 总重量（kg） | `1200.5` |
| `status` | EnumValue\<string\> | ✅ | 批次状态（见 TraceBatchStatus） | `{"code":"CREATED","desc":"已创建"}` |
| `blockchainHash` | string | ❌ | SHA-256 区块链哈希 | `a1b2c3...` |
| `createdAt` | string | ✅ | 创建时间 | `2026-01-01 08:00:00` |
| `updatedAt` | string | ✅ | 更新时间 | `2026-01-01 08:00:00` |

```typescript
export interface TraceBatchResponse {
  id: number;
  batchCode: string;
  orchardId: number;
  orchardName?: string;
  harvestDate: string;
  variety?: string;
  grade?: EnumValue<string>;
  weight?: number;
  status: EnumValue<string>;
  blockchainHash?: string;
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
        "batchCode": "TB202601010001",
        "orchardId": 101,
        "orchardName": "洛川富民果园",
        "harvestDate": "2026-10-05 00:00:00",
        "variety": "红富士",
        "grade": { "code": "A", "desc": "特级" },
        "weight": 1200.5,
        "status": { "code": "CREATED", "desc": "已创建" },
        "blockchainHash": "a1b2c3d4e5f6...",
        "createdAt": "2026-01-01 08:00:00",
        "updatedAt": "2026-01-01 08:00:00"
      }
    ],
    "total": 18,
    "current": 1,
    "size": 10
  }
}
```

#### 业务规则

- `status` 筛选传 `TraceBatchStatus.code` 字符串
- 按 `createdAt` 倒序排列

#### 实现提示

- Controller：`TraceBatchController#list`，当前返回 `PageResult<TraceBatch>`（Entity）
- 需迁移到 `TraceBatchResponse` DTO，status/grade 字段迁移为 EnumValue（见 §8.1）

---

### 3.2 批次详情

**URL**：`GET /api/trace/batches/{id}`
**认证**：需要 JWT Bearer Token
**权限**：`trace:read`
**错误码**：`300100`、`300900`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `id` | number | ✅ | 批次 ID | `3001` |

#### 响应体 `TraceBatchResponse`

（字段同 §3.1，完整返回）

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 3001,
    "batchCode": "TB202601010001",
    "orchardId": 101,
    "orchardName": "洛川富民果园",
    "harvestDate": "2026-10-05 00:00:00",
    "variety": "红富士",
    "grade": { "code": "A", "desc": "特级" },
    "weight": 1200.5,
    "status": { "code": "CREATED", "desc": "已创建" },
    "blockchainHash": "a1b2c3d4e5f6...",
    "createdAt": "2026-01-01 08:00:00",
    "updatedAt": "2026-01-01 08:00:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 300100,
  "message": "溯源批次不存在",
  "data": null
}
```

#### 业务规则

- ID 不存在返回 `300100`

#### 实现提示

- Controller：`TraceBatchController#detail`

---

### 3.3 公众扫码批次查询

**URL**：`GET /api/trace/batches/scan/{batchCode}`
**认证**：**公开，无需 JWT**
**错误码**：`300100`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `batchCode` | string | ✅ | 批次编号 | `TB202601010001` |

#### 响应体 `TraceBatchScanResponse`

公众可见的批次信息（不含内部 ID 敏感字段）。

```typescript
export interface TraceBatchScanResponse {
  batchCode: string;
  orchardName: string;
  harvestDate: string;
  variety: string;
  grade: EnumValue<string>;
  weight: number;
  status: EnumValue<string>;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "batchCode": "TB202601010001",
    "orchardName": "洛川富民果园",
    "harvestDate": "2026-10-05 00:00:00",
    "variety": "红富士",
    "grade": { "code": "A", "desc": "特级" },
    "weight": 1200.5,
    "status": { "code": "SHIPPED", "desc": "已发货" }
  }
}
```

#### 业务规则

- 此接口**不需要认证**，面向消费者扫码场景
- `batchCode` 不存在时返回 `300100`

#### 实现提示

- Controller：`TraceBatchController#scan`，当前返回 `Map<String, Object>`
- 需在后端白名单中配置此路径为 permitAll

---

### 3.4 创建溯源批次

**URL**：`POST /api/trace/batches`
**认证**：需要 JWT Bearer Token
**权限**：`trace:write`
**错误码**：`300100`、`300102`、`300103`、`300104`、`300900`
**变更历史**：2026-04-12 初版契约

#### 请求体 `TraceBatchCreateRequest`

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `orchardId` | number | ✅ | 关联果园 ID | `101` |
| `orchardName` | string | ❌ | 果园名称（冗余，可选） | `洛川富民果园` |
| `harvestDate` | string | ✅ | 采收日期，格式 `yyyy-MM-dd HH:mm:ss` | `2026-10-05 00:00:00` |
| `variety` | string | ❌ | 苹果品种 | `红富士` |
| `grade` | string | ❌ | 等级 code（见 TraceBatchGrade） | `A` |
| `weight` | number | ❌ | 总重量（kg） | `1200.5` |

```typescript
export interface TraceBatchCreateRequest {
  orchardId: number;
  orchardName?: string;
  harvestDate: string;       // yyyy-MM-dd HH:mm:ss
  variety?: string;
  grade?: string;            // TraceBatchGrade.code
  weight?: number;
}
```

#### 响应体 `TraceBatchResponse`

（字段同 §3.1，含自动生成的 `batchCode` 和 `blockchainHash`）

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "溯源批次创建成功",
  "data": {
    "id": 3001,
    "batchCode": "TB202601010001",
    "orchardId": 101,
    "orchardName": "洛川富民果园",
    "harvestDate": "2026-10-05 00:00:00",
    "variety": "红富士",
    "grade": { "code": "A", "desc": "特级" },
    "weight": 1200.5,
    "status": { "code": "CREATED", "desc": "已创建" },
    "blockchainHash": "sha256_a1b2c3d4e5f6...",
    "createdAt": "2026-01-01 08:00:00",
    "updatedAt": "2026-01-01 08:00:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 300103,
  "message": "采收日期非法：不可设置未来日期",
  "data": null
}
```

#### 业务规则

- `batchCode` 由系统自动生成（TB + yyyyMMdd + 4 位序列号），不可由前端传入
- `blockchainHash` = SHA-256(batchCode + orchardId + harvestDate + variety + weight)，创建时自动计算
- `harvestDate` 不得晚于当前日期，违反返回 `300103`
- `orchardId` 对应果园不存在时返回 `300900`（跨模块查询失败）

#### 实现提示

- Controller：`TraceBatchController#create`，当前接收 `TraceBatch` Entity
- 需创建 `TraceBatchCreateRequest` DTO（见 §8.2）

---

### 3.5 更新批次状态

**URL**：`PUT /api/trace/batches/{id}/status`
**认证**：需要 JWT Bearer Token
**权限**：`trace:write`
**错误码**：`300100`、`300101`、`300900`
**变更历史**：2026-04-12 初版契约

#### 请求参数

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| `id` | Path | number | ✅ | 批次 ID | `3001` |
| `status` | Query | string | ✅ | 目标状态 code（见 TraceBatchStatus） | `PROCESSING` |

#### 响应体 `TraceBatchResponse`

更新后的完整批次信息。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "批次状态更新成功",
  "data": {
    "id": 3001,
    "batchCode": "TB202601010001",
    "status": { "code": "PROCESSING", "desc": "处理中" },
    "updatedAt": "2026-01-05 09:00:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 300101,
  "message": "批次状态流转非法：已发货状态不可回退",
  "data": null
}
```

#### 业务规则

**状态流转规则（单向状态机）**：

```
CREATED → PROCESSING → COMPLETED → SHIPPED
```

- 只允许向前流转，不可回退
- 跳级流转不被允许（如 CREATED → SHIPPED 返回 `300101`）
- `id` 不存在返回 `300100`

#### 实现提示

- Controller：`TraceBatchController#updateStatus`
- 已实现，需在 Service 层添加状态机校验逻辑（当前实现直接更新，无校验）

---

### 3.6 删除批次

**URL**：`DELETE /api/trace/batches/{id}`
**认证**：需要 JWT Bearer Token
**权限**：`trace:delete`
**错误码**：`300100`、`300101`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `id` | number | ✅ | 批次 ID | `3001` |

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

#### 响应示例（失败）

```json
{
  "code": 300101,
  "message": "批次状态流转非法：已发货批次不可删除",
  "data": null
}
```

#### 业务规则

- 软删除（设置 `deletedAt`），不物理删除
- `SHIPPED` 状态的批次不允许删除，返回 `300101`
- `id` 不存在返回 `300100`

#### 实现提示

- Controller：`TraceBatchController#delete`
- 当前实现为软删除，需补充状态校验

---

### 3.7 添加溯源记录

**URL**：`POST /api/trace/batches/{id}/records`
**认证**：需要 JWT Bearer Token
**权限**：`trace:write`
**错误码**：`300100`、`300900`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `id` | number | ✅ | 批次 ID | `3001` |

#### 请求体 `TraceRecordCreateRequest`

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `stage` | string | ✅ | 阶段 code（见 TraceRecordStage） | `HARVEST` |
| `operator` | string | ✅ | 操作人姓名 | `李四` |
| `operatorPhone` | string | ❌ | 操作人联系方式 | `13912345678` |
| `location` | string | ❌ | 操作地点 | `洛川冷链中心` |
| `temperature` | number | ❌ | 温度（℃） | `2.5` |
| `humidity` | number | ❌ | 湿度（%） | `85.0` |
| `remark` | string | ❌ | 备注 | `入库检验合格` |
| `recordTime` | string | ✅ | 记录时间，格式 `yyyy-MM-dd HH:mm:ss` | `2026-10-06 14:00:00` |

```typescript
export interface TraceRecordCreateRequest {
  stage: string;          // TraceRecordStage.code
  operator: string;
  operatorPhone?: string;
  location?: string;
  temperature?: number;
  humidity?: number;
  remark?: string;
  recordTime: string;     // yyyy-MM-dd HH:mm:ss
}
```

#### 响应体 `TraceRecordResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 记录 ID |
| `batchId` | number | ✅ | 批次 ID |
| `batchCode` | string | ✅ | 批次编号 |
| `stage` | EnumValue\<string\> | ✅ | 阶段枚举 |
| `operator` | string | ✅ | 操作人 |
| `operatorPhone` | string | ❌ | 操作人联系方式 |
| `location` | string | ❌ | 操作地点 |
| `temperature` | number | ❌ | 温度 |
| `humidity` | number | ❌ | 湿度 |
| `remark` | string | ❌ | 备注 |
| `recordTime` | string | ✅ | 记录时间 |
| `createTime` | string | ✅ | 创建时间 |

```typescript
export interface TraceRecordResponse {
  id: number;
  batchId: number;
  batchCode: string;
  stage: EnumValue<string>;
  operator: string;
  operatorPhone?: string;
  location?: string;
  temperature?: number;
  humidity?: number;
  remark?: string;
  recordTime: string;
  createTime: string;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "溯源记录添加成功",
  "data": {
    "id": 5001,
    "batchId": 3001,
    "batchCode": "TB202601010001",
    "stage": { "code": "HARVEST", "desc": "采收" },
    "operator": "李四",
    "operatorPhone": "139****5678",
    "location": "洛川县苹果园A区",
    "temperature": null,
    "humidity": null,
    "remark": "采收完成，质量良好",
    "recordTime": "2026-10-05 06:00:00",
    "createTime": "2026-10-05 08:00:00"
  }
}
```

#### 业务规则

- `batchId` 不存在返回 `300100`
- `stage` 不在枚举范围内返回 `300900`
- `TraceRecord` 表无软删除（审计链路必须保留）

#### 实现提示

- Controller：`TraceBatchController#addRecord`，当前接收 `TraceRecord` Entity
- 需创建 `TraceRecordCreateRequest` DTO（见 §8.2）

---

### 3.8 查询批次溯源记录

**URL**：`GET /api/trace/batches/{id}/records`
**认证**：需要 JWT Bearer Token
**权限**：`trace:read`
**错误码**：`300100`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `id` | number | ✅ | 批次 ID | `3001` |

#### 响应体 `List<TraceRecordResponse>`

（字段见 §3.7 `TraceRecordResponse`）

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 5001,
      "batchId": 3001,
      "batchCode": "TB202601010001",
      "stage": { "code": "HARVEST", "desc": "采收" },
      "operator": "李四",
      "location": "洛川县苹果园A区",
      "temperature": null,
      "humidity": null,
      "recordTime": "2026-10-05 06:00:00",
      "createTime": "2026-10-05 08:00:00"
    },
    {
      "id": 5002,
      "batchId": 3001,
      "batchCode": "TB202601010001",
      "stage": { "code": "STORAGE", "desc": "仓储" },
      "operator": "王五",
      "location": "洛川冷链中心1号库",
      "temperature": 2.5,
      "humidity": 85.0,
      "recordTime": "2026-10-06 14:00:00",
      "createTime": "2026-10-06 14:30:00"
    }
  ]
}
```

#### 业务规则

- 按 `recordTime` 升序排列，构成时间轴
- `id` 不存在返回 `300100`

#### 实现提示

- Controller：`TraceBatchController#listRecords`
- 当前返回 `List<TraceRecord>`，需迁移到 `List<TraceRecordResponse>`（含 EnumValue）

---

### 3.9 导出批次 CSV

**URL**：`GET /api/trace/batches/export`
**认证**：需要 JWT Bearer Token
**权限**：`trace:export`
**响应类型**：`text/csv`（**非 R\<T\> 包装**）
**错误码**：`300900`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `keyword` | string | ❌ | 关键词筛选 | `TB2026` |
| `status` | string | ❌ | 状态筛选 | `CREATED` |

#### 响应说明

- HTTP `Content-Type`：`text/csv;charset=UTF-8`
- `Content-Disposition`：`attachment; filename="trace_batches_yyyyMMdd.csv"`
- 响应体为 **CSV 文件流**，不经过 `R<T>` 包装
- CSV 列：批次编号、果园ID、果园名称、采收日期、品种、等级、重量(kg)、状态、区块链哈希、创建时间

#### 实现提示

- Controller：`TraceBatchController#export`，通过 `HttpServletResponse` 流式输出

---

## 4. 三级溯源码 TraceCodeController（`/api/trace/code`）

### 4.1 生成 BOX 级编码

**URL**：`POST /api/trace/code/generate-box`
**认证**：需要 JWT Bearer Token
**权限**：`trace:write`
**错误码**：`300100`、`300201`、`300900`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `batchId` | number | ✅ | 关联批次 ID | `3001` |
| `boxCount` | number | ✅ | 生成箱数，范围 1～1000 | `50` |

#### 响应体 `List<TraceCodeResponse>`

| 字段 | 类型 | 必返 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 溯源码 ID | `6001` |
| `code` | string | ✅ | 溯源码，格式：{batchCode}-B{###}-{CRC4} | `TB202601010001-B001-A1B2` |
| `granularity` | EnumValue\<string\> | ✅ | 粒度（BOX） | `{"code":"BOX","desc":"箱"}` |
| `parentCode` | string | ✅ | 父级编码（batchCode） | `TB202601010001` |
| `batchId` | number | ✅ | 关联批次 ID | `3001` |
| `crc16` | string | ✅ | CRC-16 校验码（4位大写十六进制） | `A1B2` |
| `qrUrl` | string | ✅ | 公众扫码 URL | `/public/scan/TB202601010001-B001-A1B2` |
| `status` | EnumValue\<string\> | ✅ | 状态（见 TraceCodeStatus） | `{"code":"ACTIVE","desc":"激活"}` |
| `createdAt` | string | ✅ | 创建时间 | `2026-01-01 08:00:00` |

```typescript
export interface TraceCodeResponse {
  id: number;
  code: string;
  granularity: EnumValue<string>;
  parentCode: string;
  batchId: number;
  crc16: string;
  qrUrl: string;
  status: EnumValue<string>;
  createdAt: string;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "BOX 级编码生成成功",
  "data": [
    {
      "id": 6001,
      "code": "TB202601010001-B001-A1B2",
      "granularity": { "code": "BOX", "desc": "箱" },
      "parentCode": "TB202601010001",
      "batchId": 3001,
      "crc16": "A1B2",
      "qrUrl": "/public/scan/TB202601010001-B001-A1B2",
      "status": { "code": "ACTIVE", "desc": "激活" },
      "createdAt": "2026-01-01 08:00:00"
    }
  ]
}
```

#### 业务规则

- `batchId` 不存在返回 `300100`
- `boxCount` ≤ 0 或 > 1000 返回 `300201`
- 编码格式：`{batchCode}-B{3位序}-{CRC16[0..3]}（大写十六进制）`
- CRC-16 算法：CRC-16/CCITT-FALSE

#### 实现提示

- Controller：`TraceCodeController#generateBox`
- 已实现，Status/granularity 字段需迁移为 EnumValue

---

### 4.2 生成 FRUIT 级编码

**URL**：`POST /api/trace/code/generate-fruit`
**认证**：需要 JWT Bearer Token
**权限**：`trace:write`
**错误码**：`300202`、`300201`、`300900`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `boxCode` | string | ✅ | 父级箱码 | `TB202601010001-B001-A1B2` |
| `fruitCount` | number | ✅ | 生成果数，范围 1～200 | `30` |

#### 响应体 `List<TraceCodeResponse>`

（字段同 §4.1，`granularity.code = "FRUIT"`，`parentCode = boxCode`）

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "FRUIT 级编码生成成功",
  "data": [
    {
      "id": 7001,
      "code": "TB202601010001-B001-A1B2-F0001-C3D4",
      "granularity": { "code": "FRUIT", "desc": "果" },
      "parentCode": "TB202601010001-B001-A1B2",
      "batchId": 3001,
      "crc16": "C3D4",
      "qrUrl": "/public/scan/TB202601010001-B001-A1B2-F0001-C3D4",
      "status": { "code": "ACTIVE", "desc": "激活" },
      "createdAt": "2026-01-01 08:00:00"
    }
  ]
}
```

#### 业务规则

- `boxCode` 不存在（在 trace_code 表中找不到 BOX 级编码）返回 `300202`
- `fruitCount` ≤ 0 或 > 200 返回 `300201`
- 编码格式：`{boxCode}-F{4位序}-{CRC16[0..3]}`

#### 实现提示

- Controller：`TraceCodeController#generateFruit`
- 已实现，需 EnumValue 迁移

---

### 4.3 校验溯源码

**URL**：`GET /api/trace/code/verify/{code}`
**认证**：需要 JWT Bearer Token
**权限**：`trace:read`
**错误码**：`300002`、`300003`、`300200`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `code` | string | ✅ | 待校验溯源码 | `TB202601010001-B001-A1B2` |

#### 响应体 `TraceCodeVerifyResponse`

| 字段 | 类型 | 必返 | 说明 | 示例 |
|------|------|------|------|------|
| `code` | string | ✅ | 溯源码 | `TB202601010001-B001-A1B2` |
| `valid` | boolean | ✅ | 整体校验结果 | `true` |
| `crcValid` | boolean | ✅ | CRC16 校验是否通过 | `true` |
| `exists` | boolean | ✅ | 数据库中是否存在 | `true` |
| `status` | EnumValue\<string\> | ❌ | 状态（存在时返回） | `{"code":"ACTIVE","desc":"激活"}` |
| `granularity` | EnumValue\<string\> | ❌ | 粒度（存在时返回） | `{"code":"BOX","desc":"箱"}` |
| `batchId` | number | ❌ | 关联批次 ID | `3001` |
| `message` | string | ✅ | 校验结果说明 | `校验通过` |

```typescript
export interface TraceCodeVerifyResponse {
  code: string;
  valid: boolean;
  crcValid: boolean;
  exists: boolean;
  status?: EnumValue<string>;
  granularity?: EnumValue<string>;
  batchId?: number;
  message: string;
}
```

#### 响应示例（成功-校验通过）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "code": "TB202601010001-B001-A1B2",
    "valid": true,
    "crcValid": true,
    "exists": true,
    "status": { "code": "ACTIVE", "desc": "激活" },
    "granularity": { "code": "BOX", "desc": "箱" },
    "batchId": 3001,
    "message": "校验通过"
  }
}
```

#### 响应示例（失败-CRC 校验失败）

```json
{
  "code": 300200,
  "message": "CRC16 校验失败",
  "data": null
}
```

#### 业务规则

- 校验顺序：① 格式校验（300002）→ ② CRC16 校验（300200）→ ③ 数据库存在校验（300003）
- 格式非法（不符合三级码规则）返回 `300002`
- CRC 不匹配返回 `300200`
- 数据库无记录返回 `300003`
- 返回 HTTP 200（`R.code = 200`），`valid = false` 表示业务校验不通过（区别于错误响应）

#### 实现提示

- Controller：`TraceCodeController#verify`，当前返回 `Map<String, Object>`
- 需结构化为 `TraceCodeVerifyResponse`

---

### 4.4 VDP 印刷文件导出

**URL**：`GET /api/trace/code/vdp-export/{batchId}`
**认证**：需要 JWT Bearer Token
**权限**：`trace:export`
**响应类型**：`text/csv` 或 `text/plain`（**非 R\<T\> 包装**）
**错误码**：`300100`、`300203`
**变更历史**：2026-04-12 初版契约

#### 请求参数

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| `batchId` | Path | number | ✅ | 批次 ID | `3001` |
| `format` | Query | string | ❌ | 导出格式：`csv`（默认）或 `txt` | `csv` |

#### 响应说明

- HTTP `Content-Type`：`text/csv;charset=UTF-8`（`format=csv`）或 `text/plain;charset=UTF-8`（`format=txt`）
- `Content-Disposition`：`attachment; filename="vdp_{batchCode}_{yyyyMMdd}.{format}"`
- 响应体为 **文件流**，不经过 `R<T>` 包装
- CSV/TXT 列：溯源码、粒度、父级码、CRC16、二维码 URL

#### 业务规则

- `batchId` 不存在返回 `300100`
- 批次下无溯源码时返回 `300203`（VDP 导出失败）
- VDP 文件供印刷厂使用，包含一码一行格式

#### 实现提示

- Controller：`TraceCodeController#exportVdp`，通过 `HttpServletResponse` 流式输出
- 已实现，与契约基本一致

---

## 5. 异常追溯 AnomalyTraceController（`/api/trace/anomaly`）

### 5.1 上报异常

**URL**：`POST /api/trace/anomaly`
**认证**：需要 JWT Bearer Token
**权限**：`trace:write`
**错误码**：`300302`、`300303`、`300900`
**变更历史**：2026-04-12 初版契约

#### 请求体 `AnomalyReportRequest`

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `traceCode` | string | ✅ | 关联溯源码 | `TCHB20260101001` |
| `batchId` | number | ❌ | 关联批次 ID | `3001` |
| `anomalyType` | string | ✅ | 异常类型 code（见 AnomalyType） | `PESTICIDE_EXCESS` |
| `description` | string | ✅ | 异常描述 | `氯氟氰菊酯残留超标 2.3 倍` |
| `severity` | string | ✅ | 严重程度 code（见 AnomalySeverity） | `HIGH` |
| `affectedBatchCount` | number | ❌ | 受影响批次数 | `3` |
| `affectedFruitCount` | number | ❌ | 受影响果品数 | `1500` |
| `remark` | string | ❌ | 备注 | `已通知农户停止销售` |

```typescript
export interface AnomalyReportRequest {
  traceCode: string;
  batchId?: number;
  anomalyType: string;          // AnomalyType.code
  description: string;
  severity: string;             // AnomalySeverity.code
  affectedBatchCount?: number;
  affectedFruitCount?: number;
  remark?: string;
}
```

#### 响应体 `AnomalyTraceResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 异常记录 ID |
| `traceCode` | string | ✅ | 关联溯源码 |
| `batchId` | number | ❌ | 关联批次 ID |
| `anomalyType` | EnumValue\<string\> | ✅ | 异常类型 |
| `description` | string | ✅ | 异常描述 |
| `severity` | EnumValue\<string\> | ✅ | 严重程度 |
| `status` | EnumValue\<string\> | ✅ | 当前状态（初始 OPEN） |
| `affectedBatchCount` | number | ❌ | 受影响批次数 |
| `affectedFruitCount` | number | ❌ | 受影响果品数 |
| `rootCauseAnalysis` | string | ❌ | 根因分析（解决后填充） |
| `resolvedBy` | string | ❌ | 解决人 |
| `resolvedTime` | string | ❌ | 解决时间 |
| `remark` | string | ❌ | 备注 |
| `createdAt` | string | ✅ | 上报时间 |
| `updatedAt` | string | ✅ | 更新时间 |

```typescript
export interface AnomalyTraceResponse {
  id: number;
  traceCode: string;
  batchId?: number;
  anomalyType: EnumValue<string>;
  description: string;
  severity: EnumValue<string>;
  status: EnumValue<string>;
  affectedBatchCount?: number;
  affectedFruitCount?: number;
  rootCauseAnalysis?: string;
  resolvedBy?: string;
  resolvedTime?: string;
  remark?: string;
  createdAt: string;
  updatedAt: string;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "异常上报成功",
  "data": {
    "id": 8001,
    "traceCode": "TCHB20260101001",
    "batchId": 3001,
    "anomalyType": { "code": "PESTICIDE_EXCESS", "desc": "农残超标" },
    "description": "氯氟氰菊酯残留超标 2.3 倍",
    "severity": { "code": "HIGH", "desc": "高" },
    "status": { "code": "OPEN", "desc": "已上报" },
    "affectedBatchCount": 3,
    "affectedFruitCount": 1500,
    "rootCauseAnalysis": null,
    "resolvedBy": null,
    "resolvedTime": null,
    "remark": "已通知农户停止销售",
    "createdAt": "2026-10-12 09:00:00",
    "updatedAt": "2026-10-12 09:00:00"
  }
}
```

#### 业务规则

- 初始状态强制为 `OPEN`，前端传入的 status 字段忽略
- `anomalyType` 不在枚举中返回 `300302`
- `severity` 不在枚举中返回 `300303`
- `traceCode` 不存在返回 `300900`（跨模块查询失败）

#### 实现提示

- Controller：`AnomalyTraceController#report`，当前接收 `AnomalyTrace` Entity
- 需创建 `AnomalyReportRequest` DTO（见 §8.2）

---

### 5.2 异常列表

**URL**：`GET /api/trace/anomaly`
**认证**：需要 JWT Bearer Token
**权限**：`trace:read`
**错误码**：`300900`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `page` | number | ❌ | 页码，默认 1 | `1` |
| `size` | number | ❌ | 每页，默认 10 | `10` |
| `type` | string | ❌ | 异常类型筛选（AnomalyType.code） | `PESTICIDE_EXCESS` |
| `status` | string | ❌ | 状态筛选（AnomalyStatus.code） | `OPEN` |
| `severity` | string | ❌ | 严重程度筛选（AnomalySeverity.code） | `HIGH` |

```typescript
export interface AnomalyPageRequest {
  page?: number;
  size?: number;
  type?: string;
  status?: string;
  severity?: string;
}
```

#### 响应体 `PageResult<AnomalyTraceResponse>`

（字段见 §5.1 `AnomalyTraceResponse`）

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 8001,
        "traceCode": "TCHB20260101001",
        "batchId": 3001,
        "anomalyType": { "code": "PESTICIDE_EXCESS", "desc": "农残超标" },
        "description": "氯氟氰菊酯残留超标 2.3 倍",
        "severity": { "code": "HIGH", "desc": "高" },
        "status": { "code": "OPEN", "desc": "已上报" },
        "affectedBatchCount": 3,
        "affectedFruitCount": 1500,
        "createdAt": "2026-10-12 09:00:00",
        "updatedAt": "2026-10-12 09:00:00"
      }
    ],
    "total": 5,
    "current": 1,
    "size": 10
  }
}
```

#### 业务规则

- 支持 `type`、`status`、`severity` 三维筛选
- 按 `createdAt` 倒序排列

#### 实现提示

- Controller：`AnomalyTraceController#list`
- 当前返回 `PageResult<AnomalyTrace>`（Entity），需迁移

---

### 5.3 异常详情

**URL**：`GET /api/trace/anomaly/{id}`
**认证**：需要 JWT Bearer Token
**权限**：`trace:read`
**错误码**：`300300`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `id` | number | ✅ | 异常记录 ID | `8001` |

#### 响应体 `AnomalyTraceResponse`

（字段见 §5.1，完整返回所有字段）

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 8001,
    "traceCode": "TCHB20260101001",
    "batchId": 3001,
    "anomalyType": { "code": "PESTICIDE_EXCESS", "desc": "农残超标" },
    "description": "氯氟氰菊酯残留超标 2.3 倍",
    "severity": { "code": "HIGH", "desc": "高" },
    "status": { "code": "INVESTIGATING", "desc": "调查中" },
    "affectedBatchCount": 3,
    "affectedFruitCount": 1500,
    "rootCauseAnalysis": null,
    "resolvedBy": null,
    "resolvedTime": null,
    "remark": "已通知农户停止销售",
    "createdAt": "2026-10-12 09:00:00",
    "updatedAt": "2026-10-12 10:30:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 300300,
  "message": "异常记录不存在",
  "data": null
}
```

#### 实现提示

- Controller：`AnomalyTraceController#detail`

---

### 5.4 开始调查

**URL**：`PUT /api/trace/anomaly/{id}/investigate`
**认证**：需要 JWT Bearer Token
**权限**：`trace:write`
**错误码**：`300300`、`300301`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `id` | number | ✅ | 异常记录 ID | `8001` |

#### 无请求体

#### 响应体 `AnomalyTraceResponse`

更新后的异常记录（`status = INVESTIGATING`）。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "调查已开始",
  "data": {
    "id": 8001,
    "traceCode": "TCHB20260101001",
    "status": { "code": "INVESTIGATING", "desc": "调查中" },
    "updatedAt": "2026-10-12 10:30:00"
  }
}
```

#### 响应示例（失败-状态非法）

```json
{
  "code": 300301,
  "message": "异常状态流转非法：已调查中状态不可重复开始调查",
  "data": null
}
```

#### 业务规则

**状态流转规则**：

```
OPEN → INVESTIGATING → RESOLVED
```

- 只有 `OPEN` 状态才能调用此接口（`INVESTIGATING` / `RESOLVED` 状态返回 `300301`）
- `id` 不存在返回 `300300`

#### 实现提示

- Controller：`AnomalyTraceController#investigate`
- 需补充状态机校验：当前实现直接更新

---

### 5.5 解决异常

**URL**：`PUT /api/trace/anomaly/{id}/resolve`
**认证**：需要 JWT Bearer Token
**权限**：`trace:write`
**错误码**：`300300`、`300301`、`300900`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `id` | number | ✅ | 异常记录 ID | `8001` |

#### 请求体 `AnomalyResolveRequest`

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `rootCause` | string | ✅ | 根因分析描述 | `施药间隔期不足导致残留超标` |
| `resolvedBy` | string | ✅ | 解决人姓名/工号 | `质检员-王五` |

```typescript
export interface AnomalyResolveRequest {
  rootCause: string;
  resolvedBy: string;
}
```

#### 响应体 `AnomalyTraceResponse`

更新后的异常记录（`status = RESOLVED`，含 `rootCauseAnalysis`、`resolvedBy`、`resolvedTime`）。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "异常已解决",
  "data": {
    "id": 8001,
    "traceCode": "TCHB20260101001",
    "status": { "code": "RESOLVED", "desc": "已解决" },
    "rootCauseAnalysis": "施药间隔期不足导致残留超标",
    "resolvedBy": "质检员-王五",
    "resolvedTime": "2026-10-15 16:00:00",
    "updatedAt": "2026-10-15 16:00:00"
  }
}
```

#### 响应示例（失败-状态非法）

```json
{
  "code": 300301,
  "message": "异常状态流转非法：只有调查中状态才可解决",
  "data": null
}
```

#### 业务规则

**状态流转规则**：

```
OPEN → INVESTIGATING → RESOLVED
```

- 只有 `INVESTIGATING` 状态才能调用此接口（`OPEN` / `RESOLVED` 返回 `300301`）
- `resolvedTime` 由服务端自动填入当前时间
- `id` 不存在返回 `300300`

#### 实现提示

- Controller：`AnomalyTraceController#resolve`，当前通过 `Map<String, String> body` 接收 `rootCause` 和 `resolvedBy`
- 需创建 `AnomalyResolveRequest` DTO（见 §8.2）

---

### 5.6 影响分析

**URL**：`GET /api/trace/anomaly/impact/{traceCode}`
**认证**：需要 JWT Bearer Token
**权限**：`trace:read`
**错误码**：`300001`、`300304`
**变更历史**：2026-04-12 初版契约

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `traceCode` | string | ✅ | 溯源码 | `TCHB20260101001` |

#### 响应体 `AnomalyImpactResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `traceCode` | string | ✅ | 溯源码 |
| `anomalyList` | AnomalyTraceResponse[] | ✅ | 该溯源链所有异常记录 |
| `affectedBatchCount` | number | ✅ | 汇总受影响批次数 |
| `affectedFruitCount` | number | ✅ | 汇总受影响果品数 |
| `riskLevel` | EnumValue\<string\> | ✅ | 综合风险等级（基于 severity 汇总） |
| `relatedTraceCodesCount` | number | ✅ | 同批次关联溯源链数量 |

```typescript
export interface AnomalyImpactResponse {
  traceCode: string;
  anomalyList: AnomalyTraceResponse[];
  affectedBatchCount: number;
  affectedFruitCount: number;
  riskLevel: EnumValue<string>;      // 复用 AnomalySeverity 枚举
  relatedTraceCodesCount: number;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "traceCode": "TCHB20260101001",
    "anomalyList": [
      {
        "id": 8001,
        "anomalyType": { "code": "PESTICIDE_EXCESS", "desc": "农残超标" },
        "severity": { "code": "HIGH", "desc": "高" },
        "status": { "code": "INVESTIGATING", "desc": "调查中" },
        "createdAt": "2026-10-12 09:00:00",
        "updatedAt": "2026-10-12 10:30:00"
      }
    ],
    "affectedBatchCount": 3,
    "affectedFruitCount": 1500,
    "riskLevel": { "code": "HIGH", "desc": "高" },
    "relatedTraceCodesCount": 2
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 300001,
  "message": "溯源链不存在",
  "data": null
}
```

#### 业务规则

- `traceCode` 不存在返回 `300001`
- `riskLevel` = 所有关联异常中 severity 最高值（HIGH > MEDIUM > LOW）
- 影响分析失败（内部计算异常）返回 `300304`
- 无异常记录时，`anomalyList` 为空数组，其余汇总字段为 0

#### 实现提示

- Controller：`AnomalyTraceController#impact`，当前返回 `Map<String, Object>`
- 需结构化为 `AnomalyImpactResponse`

---

## 6. 错误码清单

| code | 常量 | HTTP 状态 | message | 说明 |
|------|------|-----------|---------|------|
| **TraceChain 溯源链（300001-300099）** | | | | |
| 300001 | `TRACE_CHAIN_NOT_FOUND` | 404 | 溯源链不存在 | traceCode 在 tr_trace_chain 中无记录 |
| 300002 | `TRACE_CODE_INVALID` | 400 | 溯源码格式非法 | 不符合三级码格式规则 |
| 300003 | `TRACE_CODE_NOT_FOUND` | 404 | 溯源码不存在 | trace_code 表中无该码 |
| 300004 | `TRACE_NODE_CREATE_FAILED` | 500 | 溯源节点创建失败 | 节点写入失败（DB 异常） |
| 300005 | `TRACE_QRCODE_GENERATE_FAILED` | 500 | 二维码生成失败 | ZXing 生成 PNG 异常 |
| **TraceBatch 溯源批次（300100-300199）** | | | | |
| 300100 | `TRACE_BATCH_NOT_FOUND` | 404 | 溯源批次不存在 | trace_batch 中无该 ID |
| 300101 | `TRACE_BATCH_STATUS_INVALID` | 422 | 批次状态流转非法 | 回退/跳级/删除已发货批次 |
| 300102 | `TRACE_BATCH_DUPLICATE_CODE` | 409 | 批次编码重复 | batchCode 唯一索引冲突 |
| 300103 | `TRACE_BATCH_HARVEST_DATE_INVALID` | 400 | 采收日期非法 | 日期晚于当前日期 |
| 300104 | `TRACE_BATCH_BLOCKCHAIN_HASH_FAILED` | 500 | 区块链哈希生成失败 | SHA-256 计算异常 |
| **TraceCode 三级溯源码（300200-300299）** | | | | |
| 300200 | `TRACE_CODE_CRC_MISMATCH` | 400 | CRC16 校验失败 | 码末段与重算值不一致 |
| 300201 | `TRACE_CODE_BOX_COUNT_INVALID` | 400 | 码数量非法 | boxCount/fruitCount ≤ 0 或超上限 |
| 300202 | `TRACE_CODE_PARENT_NOT_FOUND` | 404 | 上级溯源码不存在 | boxCode 在 trace_code 表中不存在 |
| 300203 | `TRACE_CODE_VDP_EXPORT_FAILED` | 500 | VDP 导出失败 | 批次下无溯源码或文件写入失败 |
| **AnomalyTrace 异常追溯（300300-300399）** | | | | |
| 300300 | `ANOMALY_NOT_FOUND` | 404 | 异常记录不存在 | tr_anomaly_trace 中无该 ID |
| 300301 | `ANOMALY_STATUS_INVALID` | 422 | 异常状态流转非法 | 违反 OPEN→INVESTIGATING→RESOLVED 单向状态机 |
| 300302 | `ANOMALY_TYPE_INVALID` | 400 | 异常类型非法 | anomalyType 不在 AnomalyType 枚举中 |
| 300303 | `ANOMALY_SEVERITY_INVALID` | 400 | 严重程度非法 | severity 不在 AnomalySeverity 枚举中 |
| 300304 | `ANOMALY_IMPACT_ANALYSIS_FAILED` | 500 | 影响分析失败 | 内部跨模块查询或计算异常 |
| **通用（300900-300999）** | | | | |
| 300900 | `TRACE_PARAM_INVALID` | 400 | 参数非法 | 枚举值/必填字段/格式校验失败 |
| 300901 | `TRACE_CROSS_MODULE_QUERY_FAILED` | 502 | 跨模块查询失败 | 调用 planting/warehouse/trade 模块失败 |

> 本契约错误码段 `300000-399999` 与 `contract/CONVENTIONS.md:135` 一致。

---

## 7. 枚举集中声明

所有枚举序列化为 `{code, desc}` 对象（遵循 CONVENTIONS §4）。

### 7.1 TraceChainStatus — 溯源链状态

```typescript
export type TraceChainStatusCode = 'PLANTED' | 'HARVESTED' | 'IN_STORAGE' | 'IN_TRANSIT' | 'SOLD';

export interface TraceChainStatus {
  code: TraceChainStatusCode;
  desc: string;
}
```

| code | desc | 说明 |
|------|------|------|
| `PLANTED` | 已种植 | 初始状态，完成种植环节 |
| `HARVESTED` | 已采收 | 完成采收节点 |
| `IN_STORAGE` | 在库 | 进入仓储环节 |
| `IN_TRANSIT` | 运输中 | 进入物流运输环节 |
| `SOLD` | 已售出 | 交易完成，终态 |

---

### 7.2 ChainStatus — 区块链上链状态

```typescript
export type ChainStatusCode = 0 | 1;

export interface ChainStatus {
  code: ChainStatusCode;
  desc: string;
}
```

| code | desc | 说明 |
|------|------|------|
| `0` | 待上链 | 数据已写入本地，等待上链（默认） |
| `1` | 已上链 | 区块链哈希已确认，详见 chain.md |

---

### 7.3 TraceNodeType — 溯源节点类型

```typescript
export type TraceNodeTypeCode = 'PLANT' | 'GROW' | 'HARVEST' | 'STORAGE' | 'LOGISTICS' | 'TRADE';

export interface TraceNodeType {
  code: TraceNodeTypeCode;
  desc: string;
}
```

| code | desc | 说明 |
|------|------|------|
| `PLANT` | 种植 | 定植、施肥等种植操作 |
| `GROW` | 生长 | 生长期管理记录 |
| `HARVEST` | 采收 | 人工/机械采收 |
| `STORAGE` | 仓储 | 入库、出库操作 |
| `LOGISTICS` | 物流 | 运输中途节点 |
| `TRADE` | 交易 | 交付给买方 |

---

### 7.4 TraceBatchStatus — 溯源批次状态

```typescript
export type TraceBatchStatusCode = 'CREATED' | 'PROCESSING' | 'COMPLETED' | 'SHIPPED';

export interface TraceBatchStatus {
  code: TraceBatchStatusCode;
  desc: string;
}
```

| code | desc | 说明 |
|------|------|------|
| `CREATED` | 已创建 | 批次已建立，初始状态 |
| `PROCESSING` | 处理中 | 分拣/检测/包装过程中 |
| `COMPLETED` | 已完成 | 处理完成，准备出库 |
| `SHIPPED` | 已发货 | 已离开仓库，终态（不可删除） |

**状态机**：`CREATED → PROCESSING → COMPLETED → SHIPPED`（单向）

---

### 7.5 TraceBatchGrade — 溯源批次等级

```typescript
export type TraceBatchGradeCode = 'A' | 'B' | 'C';

export interface TraceBatchGrade {
  code: TraceBatchGradeCode;
  desc: string;
}
```

| code | desc | 说明 |
|------|------|------|
| `A` | 特级 | 优果率 ≥ 90%，直径 ≥ 80mm |
| `B` | 一级 | 优果率 75%-90%，直径 70-80mm |
| `C` | 二级 | 其余合格果品 |

---

### 7.6 TraceRecordStage — 溯源记录阶段

```typescript
export type TraceRecordStageCode = 'HARVEST' | 'STORAGE' | 'TRANSPORT' | 'SALE';

export interface TraceRecordStage {
  code: TraceRecordStageCode;
  desc: string;
}
```

| code | desc | 说明 |
|------|------|------|
| `HARVEST` | 采收 | 采收阶段记录（温度/湿度等） |
| `STORAGE` | 仓储 | 仓储环境监控记录 |
| `TRANSPORT` | 运输 | 运输途中温控记录 |
| `SALE` | 销售 | 交付销售节点记录 |

---

### 7.7 TraceCodeGranularity — 溯源码粒度

```typescript
export type TraceCodeGranularityCode = 'BATCH' | 'BOX' | 'FRUIT';

export interface TraceCodeGranularity {
  code: TraceCodeGranularityCode;
  desc: string;
}
```

| code | desc | 说明 |
|------|------|------|
| `BATCH` | 批次 | 顶层批次码，与 TraceBatch.batchCode 对应 |
| `BOX` | 箱 | 箱级编码，格式：`{batchCode}-B{###}-{CRC4}` |
| `FRUIT` | 果 | 果级编码，格式：`{boxCode}-F{####}-{CRC4}` |

---

### 7.8 TraceCodeStatus — 溯源码状态

```typescript
export type TraceCodeStatusCode = 'ACTIVE' | 'VOID' | 'CONSUMED';

export interface TraceCodeStatus {
  code: TraceCodeStatusCode;
  desc: string;
}
```

| code | desc | 说明 |
|------|------|------|
| `ACTIVE` | 激活 | 正常可用，默认状态 |
| `VOID` | 作废 | 已作废，扫码提示异常 |
| `CONSUMED` | 已消费 | 消费者已扫描消费 |

---

### 7.9 AnomalyType — 异常类型

```typescript
export type AnomalyTypeCode = 'PESTICIDE_EXCESS' | 'TEMP_VIOLATION' | 'QUALITY_FAIL' | 'OTHER';

export interface AnomalyType {
  code: AnomalyTypeCode;
  desc: string;
}
```

| code | desc | 说明 |
|------|------|------|
| `PESTICIDE_EXCESS` | 农残超标 | 检测到农药残留超出标准 |
| `TEMP_VIOLATION` | 温度违规 | 冷链运输/储存温度超标 |
| `QUALITY_FAIL` | 质量不合格 | 到货检验不合格 |
| `OTHER` | 其他 | 其他异常类型 |

---

### 7.10 AnomalySeverity — 严重程度

```typescript
export type AnomalySeverityCode = 'HIGH' | 'MEDIUM' | 'LOW';

export interface AnomalySeverity {
  code: AnomalySeverityCode;
  desc: string;
}
```

| code | desc | 说明 |
|------|------|------|
| `HIGH` | 高 | 涉及食品安全/监管红线，需立即处置 |
| `MEDIUM` | 中 | 影响较大，需在 48 小时内处置 |
| `LOW` | 低 | 轻微问题，按正常流程处理 |

---

### 7.11 AnomalyStatus — 异常状态

```typescript
export type AnomalyStatusCode = 'OPEN' | 'INVESTIGATING' | 'RESOLVED';

export interface AnomalyStatus {
  code: AnomalyStatusCode;
  desc: string;
}
```

| code | desc | 说明 |
|------|------|------|
| `OPEN` | 已上报 | 初始状态，等待处理 |
| `INVESTIGATING` | 调查中 | 已开始根因调查 |
| `RESOLVED` | 已解决 | 根因明确，问题已解决，终态 |

**状态机**：`OPEN → INVESTIGATING → RESOLVED`（单向）

---

## 8. 实现注记（后端迁移任务）

### 8.1 String → EnumValue 迁移清单

以下字段当前后端返回 `String`，契约目标为 `EnumValue<T>` 对象序列化。需在对应 Entity 或 Response DTO 上配置 Jackson 序列化器或重写 getter。

| Entity | 字段 | 当前类型 | 目标类型 | 枚举 |
|--------|------|---------|---------|------|
| `TraceChain` | `currentStatus` | `String` | `EnumValue<String>` | `TraceChainStatus` |
| `TraceChain` | `chainStatus` | `Integer` | `EnumValue<Integer>` | `ChainStatus` |
| `TraceNode` | `nodeType` | `String` | `EnumValue<String>` | `TraceNodeType` |
| `TraceBatch` | `status` | `String` | `EnumValue<String>` | `TraceBatchStatus` |
| `TraceBatch` | `grade` | `String` | `EnumValue<String>` | `TraceBatchGrade` |
| `TraceRecord` | `stage` | `String` | `EnumValue<String>` | `TraceRecordStage` |
| `TraceCode` | `granularity` | `String` | `EnumValue<String>` | `TraceCodeGranularity` |
| `TraceCode` | `status` | `String` | `EnumValue<String>` | `TraceCodeStatus` |
| `AnomalyTrace` | `anomalyType` | `String` | `EnumValue<String>` | `AnomalyType` |
| `AnomalyTrace` | `severity` | `String` | `EnumValue<String>` | `AnomalySeverity` |
| `AnomalyTrace` | `status` | `String` | `EnumValue<String>` | `AnomalyStatus` |

**推荐实现方式**：创建 Response DTO（见 §8.2），在 Service 层 Entity → DTO 转换时注入 EnumValue，避免污染 Entity 层。

---

### 8.2 Entity → Request DTO 重命名清单

以下接口当前直接接收 Entity 作为 RequestBody，契约目标命名为独立 Request DTO。

| Controller 方法 | 当前接收类型 | 目标 DTO | 优先级 |
|----------------|------------|---------|--------|
| `TraceBatchController#create` | `TraceBatch` | `TraceBatchCreateRequest` | P1 |
| `TraceBatchController#updateStatus` | `@RequestParam String status` | （已 OK，Query 参数无需 DTO） | — |
| `TraceBatchController#addRecord` | `TraceRecord` | `TraceRecordCreateRequest` | P1 |
| `TraceController#addNode` | `TraceNode` | `TraceNodeCreateRequest` | P1 |
| `AnomalyTraceController#report` | `AnomalyTrace` | `AnomalyReportRequest` | P1 |
| `AnomalyTraceController#resolve` | `Map<String, String>` | `AnomalyResolveRequest` | P2 |

---

### 8.3 公开接口白名单（Spring Security 配置）

以下 3 个接口需在 Spring Security 配置中设置为 `permitAll()`，无需 JWT：

```java
// SecurityConfig.java 参考配置
.requestMatchers(
    "/api/trace/scan/**",           // 公众扫码查询 + 全链路
    "/api/trace/batches/scan/**"    // 公众批次扫码查询
).permitAll()
```

| 接口 | URL 模式 |
|------|---------|
| 公众扫码查询 | `GET /api/trace/scan/{traceCode}` |
| 公众扫码全链路 | `GET /api/trace/scan/{traceCode}/full` |
| 公众批次扫码查询 | `GET /api/trace/batches/scan/{batchCode}` |

---

### 8.4 跨模块聚合依赖（§2.4 TraceAggregationService）

`/api/trace/scan/{traceCode}/full` 内部调用以下模块：

| 目标数据 | 来源模块 | 查询方式 | 失败策略 |
|---------|---------|---------|---------|
| 果园信息（`orchard`） | planting 模块 | 按 `orchardId` 查询 | catch → null |
| 农户信息（`farmer`） | planting 模块 | 按 `farmerId` 查询 | catch → null |
| 采收批次（`harvestBatch`） | planting 模块 | 按 `batchNo` 查询 | catch → null |
| 仓储信息（`warehouseInfo`） | warehouse 模块 | 按 `traceCode` 查询 | catch → null |
| 交易信息（`tradeInfo`） | trade 模块 | 按 `traceCode` 查询 | catch → null |

所有跨模块调用必须独立 try-catch，单模块失败不影响整体响应。

---

### 8.5 错误码号段

本契约错误码段 `300000-399999` 与 `contract/CONVENTIONS.md:135` 一致。

---

## 9. 关联契约

| 契约文件 | 关联关系 | 具体依赖 |
|---------|---------|---------|
| `chain.md` | 区块链上链 | `TraceChain.chainStatus` 变更由 chain.md 管理；ChainController（`/api/trace/chain/*`、`/api/admin/chain/*`）归属 chain.md |
| `planting.md` | 果园/农户/采收批次 | 全链路扫码（§2.4）聚合 orchard/farmer/harvest_batch 数据；`batchNo` 关联 `pt_harvest_batch.batch_no` |
| `warehouse.md` | 仓储信息 | 全链路扫码聚合 warehouseInfo |
| `trade.md` | 交易信息 | 全链路扫码聚合 tradeInfo |

---

## 质量自检清单

- [x] **26 个端点全部覆盖**（§1.1~§1.4 索引：7+9+4+6=26）
- [x] **至少 20 个错误码，全在 300000-399999 段**（定义 22 个：300001-300005, 300100-300104, 300200-300203, 300300-300304, 300900-300901）
- [x] **11 个枚举全部声明**（§7.1~§7.11：TraceChainStatus/ChainStatus/TraceNodeType/TraceBatchStatus/TraceBatchGrade/TraceRecordStage/TraceCodeGranularity/TraceCodeStatus/AnomalyType/AnomalySeverity/AnomalyStatus）
- [x] **所有 JSON 示例用 `message` 不用 `msg`**
- [x] **所有分页接口使用 PageParam + PageResult\<T\>**（§2.1、§3.1、§5.2）
- [x] **时间字段用 LocalDateTime + `yyyy-MM-dd HH:mm:ss`**（所有示例一致）
- [x] **公开扫码接口 3 个标注"公开"**（§2.3、§2.4、§3.3，索引表 #3/#4/#10 均标注"公开"）
- [x] **二维码/CSV 导出非 R\<T\> 包装已明确**（§2.5 image/png、§2.7/§3.9/§4.4 text/csv 均有说明）
- [x] **Entity→Request DTO 重命名在实现注记列出**（§8.2）
- [x] **String→EnumValue 迁移在实现注记列出**（§8.1）
