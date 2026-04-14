# M2 农资投入品 (Input) 模块契约

> 模块：`apple-module-input`
> URL 前缀：`/api/input/{suppliers,products,purchases,inventory,usage,trace}`
> 错误码段：`700000 ~ 799999`（CONVENTIONS.md:140 权威）
> 最近更新：2026-04-12
> 权威源：本文件
> 后端实现：`apple-module-input`
> 关联契约：`planting.md`（种植模块使用农资）/ `trace.md`（农资使用记录进入溯源）

---

## 1. 接口索引（33 端点）

| # | 接口中文名 | URL | 方法 | 认证 |
|---|-----------|-----|------|------|
| 1 | [供应商列表（分页）](#21-供应商列表分页) | `/api/input/suppliers/list` | GET | ✅ |
| 2 | [供应商详情](#22-供应商详情) | `/api/input/suppliers/{id}` | GET | ✅ |
| 3 | [创建供应商](#23-创建供应商) | `/api/input/suppliers` | POST | ✅ |
| 4 | [更新供应商](#24-更新供应商) | `/api/input/suppliers/{id}` | PUT | ✅ |
| 5 | [删除供应商](#25-删除供应商) | `/api/input/suppliers/{id}` | DELETE | ✅ |
| 6 | [审核供应商](#26-审核供应商) | `/api/input/suppliers/{id}/audit` | PUT | ✅ |
| 7 | [重新提交审核](#27-重新提交审核) | `/api/input/suppliers/{id}/reinstate` | PUT | ✅ |
| 8 | [导出供应商 CSV](#28-导出供应商-csv) | `/api/input/suppliers/export` | GET | ✅ |
| 9 | [农资产品列表（分页）](#31-农资产品列表分页) | `/api/input/products/list` | GET | ✅ |
| 10 | [农资产品详情](#32-农资产品详情) | `/api/input/products/{id}` | GET | ✅ |
| 11 | [创建农资产品](#33-创建农资产品) | `/api/input/products` | POST | ✅ |
| 12 | [更新农资产品](#34-更新农资产品) | `/api/input/products/{id}` | PUT | ✅ |
| 13 | [删除农资产品](#35-删除农资产品) | `/api/input/products/{id}` | DELETE | ✅ |
| 14 | [导出农资产品 CSV](#36-导出农资产品-csv) | `/api/input/products/export` | GET | ✅ |
| 15 | [采购列表（分页）](#41-采购列表分页) | `/api/input/purchases/list` | GET | ✅ |
| 16 | [采购详情](#42-采购详情) | `/api/input/purchases/{id}` | GET | ✅ |
| 17 | [创建采购单](#43-创建采购单) | `/api/input/purchases` | POST | ✅ |
| 18 | [更新采购单](#44-更新采购单) | `/api/input/purchases/{id}` | PUT | ✅ |
| 19 | [删除采购单](#45-删除采购单) | `/api/input/purchases/{id}` | DELETE | ✅ |
| 20 | [审批采购单](#46-审批采购单) | `/api/input/purchases/{id}/approve` | PUT | ✅ |
| 21 | [确认收货](#47-确认收货) | `/api/input/purchases/{id}/receive` | PUT | ✅ |
| 22 | [取消采购单](#48-取消采购单) | `/api/input/purchases/{id}/cancel` | PUT | ✅ |
| 23 | [导出采购记录 CSV](#49-导出采购记录-csv) | `/api/input/purchases/export` | GET | ✅ |
| 24 | [库存列表（分页）](#51-库存列表分页) | `/api/input/inventory/list` | GET | ✅ |
| 25 | [库存详情](#52-库存详情) | `/api/input/inventory/{id}` | GET | ✅ |
| 26 | [创建库存记录](#53-创建库存记录) | `/api/input/inventory` | POST | ✅ |
| 27 | [更新库存记录](#54-更新库存记录) | `/api/input/inventory/{id}` | PUT | ✅ |
| 28 | [删除库存记录](#55-删除库存记录) | `/api/input/inventory/{id}` | DELETE | ✅ |
| 29 | [调整库存数量](#56-调整库存数量) | `/api/input/inventory/{id}/adjust` | POST | ✅ |
| 30 | [库存预警列表](#57-库存预警列表) | `/api/input/inventory/alerts` | GET | ✅ |
| 31 | [导出库存 CSV](#58-导出库存-csv) | `/api/input/inventory/export` | GET | ✅ |
| 32 | [溯源链路查询](#61-溯源链路查询) | `/api/input/trace/{traceCode}` | GET | ✅ |
| 33 | [按批次查询使用记录](#附录-批量查询端点) | `/api/input/usage/by-batch` | GET | ✅ |

> 注：使用记录（AgriUsageController）共 8 个端点，因与溯源模块紧密关联，在 [附录](#附录-批量查询端点) 统一列出。

---

## 2. 农资供应商 AgriSupplierController（`/api/input/suppliers`）

### 2.1 供应商列表（分页）

**URL**：`GET /api/input/suppliers/list`
**认证**：需要 JWT Bearer
**错误码**：`700100`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，从 1 开始，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 供应商名称模糊搜索 | `张` |
| `status` | string | ❌ | 状态枚举 code，见 SupplierStatus | `PENDING` |

```typescript
export interface SupplierPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;
}
```

#### 响应体 `PageResult<SupplierResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 供应商 ID |
| `supplierCode` | string | ✅ | 供应商编码 |
| `name` | string | ✅ | 供应商名称 |
| `contactPerson` | string \| null | ❌ | 联系人 |
| `phone` | string \| null | ❌ | 联系电话 |
| `address` | string \| null | ❌ | 地址 |
| `license` | string \| null | ❌ | 营业执照号 |
| `qualification` | string \| null | ❌ | 资质证书 |
| `creditScore` | number \| null | ❌ | 信用评分（1-100） |
| `status` | `EnumValue<string>` | ✅ | 状态，见 SupplierStatus |
| `auditTime` | string \| null | ❌ | 审核时间，`yyyy-MM-dd HH:mm:ss` |
| `auditor` | string \| null | ❌ | 审核人 |
| `rejectReason` | string \| null | ❌ | 拒绝原因 |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface SupplierResponse {
  id: number;
  supplierCode: string;
  name: string;
  contactPerson: string | null;
  phone: string | null;
  address: string | null;
  license: string | null;
  qualification: string | null;
  creditScore: number | null;
  status: EnumValue<string>;
  auditTime: string | null;
  auditor: string | null;
  rejectReason: string | null;
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
        "id": 1001,
        "supplierCode": "SUP20260412001",
        "name": "绿丰农资有限公司",
        "contactPerson": "王经理",
        "phone": "13800138000",
        "address": "陕西省延安市洛川县",
        "license": "91610600MA6XXXXX",
        "qualification": "农药经营许可证",
        "creditScore": 85,
        "status": { "code": "APPROVED", "desc": "已通过" },
        "auditTime": "2026-04-12 10:00:00",
        "auditor": "管理员",
        "rejectReason": null,
        "remark": null,
        "createdAt": "2026-04-11 09:00:00",
        "updatedAt": "2026-04-12 10:00:00"
      }
    ],
    "total": 50,
    "current": 1,
    "size": 10
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 700100,
  "message": "供应商列表查询失败",
  "data": null
}
```

---

### 2.2 供应商详情

**URL**：`GET /api/input/suppliers/{id}`
**认证**：需要 JWT Bearer
**错误码**：`700101`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 供应商 ID | `1001` |

#### 响应体 `SupplierResponse`

字段同 [2.1 响应体 SupplierResponse](#21-供应商列表分页)。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* SupplierResponse */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 700101,
  "message": "供应商不存在",
  "data": null
}
```

---

### 2.3 创建供应商

**URL**：`POST /api/input/suppliers`
**认证**：需要 JWT Bearer
**错误码**：`700102`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 CreateSupplierRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `supplierCode` | string | ✅ | 供应商编码 | `SUP20260412001` |
| `name` | string | ✅ | 供应商名称 | `绿丰农资有限公司` |
| `contactPerson` | string | ❌ | 联系人 | `王经理` |
| `phone` | string | ❌ | 联系电话 | `13800138000` |
| `address` | string | ❌ | 地址 | `陕西省延安市洛川县` |
| `license` | string | ❌ | 营业执照号 | `91610600MA6XXXXX` |
| `qualification` | string | ❌ | 资质证书 | `农药经营许可证` |
| `creditScore` | number | ❌ | 信用评分（1-100） | `85` |
| `remark` | string | ❌ | 备注 | `null` |

```typescript
export interface CreateSupplierRequest {
  supplierCode: string;
  name: string;
  contactPerson?: string;
  phone?: string;
  address?: string;
  license?: string;
  qualification?: string;
  creditScore?: number;
  remark?: string;
}
```

#### 业务规则

- 创建时 `status` 强制设置为 `PENDING`，忽略请求中传入的状态值。
- 供应商初始进入待审核状态，须经审核才可使用。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* SupplierResponse，status 固定为 { "code": "PENDING", "desc": "待审核" } */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 700102,
  "message": "供应商创建失败",
  "data": null
}
```

---

### 2.4 更新供应商

**URL**：`PUT /api/input/suppliers/{id}`
**认证**：需要 JWT Bearer
**错误码**：`700103` / `700104`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 供应商 ID | `1001` |

#### 请求体 UpdateSupplierRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `name` | string | ❌ | 供应商名称 | `绿丰农资集团` |
| `contactPerson` | string | ❌ | 联系人 | `李经理` |
| `phone` | string | ❌ | 联系电话 | `13900139000` |
| `address` | string | ❌ | 地址 | `陕西省延安市宝塔区` |
| `license` | string | ❌ | 营业执照号 | `91610600MA6YYYYY` |
| `qualification` | string | ❌ | 资质证书 | `农药经营许可证（续期）` |
| `creditScore` | number | ❌ | 信用评分 | `88` |
| `remark` | string | ❌ | 备注 | `更新信息` |

```typescript
export interface UpdateSupplierRequest {
  name?: string;
  contactPerson?: string;
  phone?: string;
  address?: string;
  license?: string;
  qualification?: string;
  creditScore?: number;
  remark?: string;
}
```

#### 业务规则

- 若供应商不存在，抛出 `NOT_FOUND`。
- 强制使用 path 中的 `id`，忽略 body 中的 `id` 字段。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* SupplierResponse */ }
}
```

#### 响应示例（失败 — 不存在）

```json
{
  "code": 700103,
  "message": "供应商不存在",
  "data": null
}
```

#### 响应示例（失败 — 更新失败）

```json
{
  "code": 700104,
  "message": "供应商更新失败",
  "data": null
}
```

---

### 2.5 删除供应商

**URL**：`DELETE /api/input/suppliers/{id}`
**认证**：需要 JWT Bearer
**错误码**：`700105`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 供应商 ID | `1001` |

#### 业务规则

- 若供应商不存在，抛出 `NOT_FOUND`。

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
  "code": 700105,
  "message": "供应商不存在",
  "data": null
}
```

---

### 2.6 审核供应商

**URL**：`PUT /api/input/suppliers/{id}/audit`
**认证**：需要 JWT Bearer
**错误码**：`700106` / `700107`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 供应商 ID | `1001` |

#### 请求体 AuditSupplierRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `decision` | string | ✅ | 审核决策：`APPROVE` / `REJECT` / `BLACKLIST` | `APPROVE` |
| `reason` | string | ❌ | 拒绝原因（decision=REJECT 时建议填写） | `资质不全` |

```typescript
export interface AuditSupplierRequest {
  decision: string;
  reason?: string;
}
```

#### 业务规则

- 状态机转换（任意当前状态均可执行）：
  - `decision=APPROVE` → `APPROVED`
  - `decision=REJECT` → `REJECTED`（同时写入 rejectReason）
  - `decision=BLACKLIST` → `BLACKLISTED`
- `auditTime` 自动设置为当前时间。
- `decision` 不区分大小写（统一转大写后匹配）。
- 无效的 decision 值抛出业务异常。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* SupplierResponse，status 变为 APPROVED */ }
}
```

#### 响应示例（失败 — 供应商不存在）

```json
{
  "code": 700106,
  "message": "供应商不存在",
  "data": null
}
```

#### 响应示例（失败 — 无效审核决策）

```json
{
  "code": 700107,
  "message": "无效的审核决策，仅支持 APPROVE/REJECT/BLACKLIST",
  "data": null
}
```

---

### 2.7 重新提交审核

**URL**：`PUT /api/input/suppliers/{id}/reinstate`
**认证**：需要 JWT Bearer
**错误码**：`700108` / `700109`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 供应商 ID | `1001` |

#### 业务规则

- 仅 `REJECTED` 状态的供应商可执行此操作，否则抛出业务异常。
- 状态转换：`REJECTED` → `PENDING`（清空 rejectReason）。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* SupplierResponse，status 变为 PENDING，rejectReason 已清空 */ }
}
```

#### 响应示例（失败 — 状态不允许）

```json
{
  "code": 700108,
  "message": "仅已拒绝状态的供应商可重新提交审核",
  "data": null
}
```

#### 响应示例（失败 — 供应商不存在）

```json
{
  "code": 700109,
  "message": "供应商不存在",
  "data": null
}
```

---

### 2.8 导出供应商 CSV

**URL**：`GET /api/input/suppliers/export`
**认证**：需要 JWT Bearer
**错误码**：`700110`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `keyword` | string | ❌ | 供应商名称模糊搜索 | `绿丰` |
| `status` | string | ❌ | 状态枚举 code | `APPROVED` |

#### 响应

**非 R\<T\> 包装**。直接返回文件下载：

- `Content-Type: text/csv;charset=UTF-8`
- `Content-Disposition: attachment; filename=农资供应商.csv`
- 编码：UTF-8 BOM

CSV 列顺序：供应商编码、名称、联系人、电话、地址、营业执照号、资质证书、信用评分、状态。

---

## 3. 农资产品 AgriProductController（`/api/input/products`）

### 3.1 农资产品列表（分页）

**URL**：`GET /api/input/products/list`
**认证**：需要 JWT Bearer
**错误码**：`700200`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，从 1 开始，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 产品名称模糊搜索 | `尿素` |
| `type` | string | ❌ | 产品类型枚举 code，见 ProductType | `FERTILIZER` |
| `status` | string | ❌ | 状态枚举 code，见 ProductStatus | `ACTIVE` |

```typescript
export interface ProductPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  type?: string;
  status?: string;
}
```

#### 响应体 `PageResult<ProductResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 产品 ID |
| `productCode` | string | ✅ | 产品编码，如 `AP20250101001` |
| `name` | string | ✅ | 产品名称 |
| `type` | `EnumValue<string>` | ✅ | 产品类型，见 ProductType |
| `manufacturer` | string \| null | ❌ | 生产厂家 |
| `spec` | string \| null | ❌ | 规格，如 `50kg/袋` |
| `batchNo` | string \| null | ❌ | 生产批号 |
| `productionDate` | string \| null | ❌ | 生产日期，`yyyy-MM-dd` |
| `expiryDate` | string \| null | ❌ | 保质期至，`yyyy-MM-dd` |
| `registration` | string \| null | ❌ | 登记证号/农药登记号 |
| `status` | `EnumValue<string>` | ✅ | 状态，见 ProductStatus |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface ProductResponse {
  id: number;
  productCode: string;
  name: string;
  type: EnumValue<string>;
  manufacturer: string | null;
  spec: string | null;
  batchNo: string | null;
  productionDate: string | null;
  expiryDate: string | null;
  registration: string | null;
  status: EnumValue<string>;
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
        "id": 2001,
        "productCode": "AP20260412001",
        "name": "尿素（含氮量46%）",
        "type": { "code": "FERTILIZER", "desc": "化肥" },
        "manufacturer": "陕化集团",
        "spec": "50kg/袋",
        "batchNo": "B20260401",
        "productionDate": "2026-04-01",
        "expiryDate": "2027-04-01",
        "registration": "陕农肥（2026）0001号",
        "status": { "code": "ACTIVE", "desc": "在用" },
        "remark": null,
        "createdAt": "2026-04-12 09:00:00",
        "updatedAt": "2026-04-12 09:00:00"
      }
    ],
    "total": 80,
    "current": 1,
    "size": 10
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 700200,
  "message": "农资产品列表查询失败",
  "data": null
}
```

---

### 3.2 农资产品详情

**URL**：`GET /api/input/products/{id}`
**认证**：需要 JWT Bearer
**错误码**：`700201`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 产品 ID | `2001` |

#### 响应体 `ProductResponse`

字段同 [3.1 响应体 ProductResponse](#31-农资产品列表分页)。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* ProductResponse */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 700201,
  "message": "农资产品不存在",
  "data": null
}
```

---

### 3.3 创建农资产品

**URL**：`POST /api/input/products`
**认证**：需要 JWT Bearer
**错误码**：`700202`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 CreateProductRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `productCode` | string | ✅ | 产品编码 | `AP20260412002` |
| `name` | string | ✅ | 产品名称 | `草甘膦除草剂` |
| `type` | string | ✅ | 产品类型枚举 code，见 ProductType | `PESTICIDE` |
| `manufacturer` | string | ❌ | 生产厂家 | `XX化工` |
| `spec` | string | ❌ | 规格 | `500ml/瓶` |
| `batchNo` | string | ❌ | 生产批号 | `B20260301` |
| `productionDate` | string | ❌ | 生产日期 | `2026-03-01` |
| `expiryDate` | string | ❌ | 保质期至 | `2028-03-01` |
| `registration` | string | ❌ | 登记证号 | `PD20260001` |
| `remark` | string | ❌ | 备注 | `null` |

```typescript
export interface CreateProductRequest {
  productCode: string;
  name: string;
  type: string;
  manufacturer?: string;
  spec?: string;
  batchNo?: string;
  productionDate?: string;
  expiryDate?: string;
  registration?: string;
  remark?: string;
}
```

#### 业务规则

- 创建时 `status` 强制设置为 `ACTIVE`，忽略请求传入的状态值。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* ProductResponse，status 固定为 { "code": "ACTIVE", "desc": "在用" } */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 700202,
  "message": "农资产品创建失败",
  "data": null
}
```

---

### 3.4 更新农资产品

**URL**：`PUT /api/input/products/{id}`
**认证**：需要 JWT Bearer
**错误码**：`700203` / `700204`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 产品 ID | `2001` |

#### 请求体 UpdateProductRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `name` | string | ❌ | 产品名称 | `尿素（含氮量46.4%）` |
| `manufacturer` | string | ❌ | 生产厂家 | `陕化集团（二期）` |
| `spec` | string | ❌ | 规格 | `40kg/袋` |
| `remark` | string | ❌ | 备注 | `更新规格` |

```typescript
export interface UpdateProductRequest {
  name?: string;
  manufacturer?: string;
  spec?: string;
  remark?: string;
}
```

#### 业务规则

- 若产品不存在，抛出 `NOT_FOUND`。
- 强制使用 path 中的 `id`。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* ProductResponse */ }
}
```

#### 响应示例（失败 — 不存在）

```json
{
  "code": 700203,
  "message": "农资产品不存在",
  "data": null
}
```

#### 响应示例（失败 — 更新失败）

```json
{
  "code": 700204,
  "message": "农资产品更新失败",
  "data": null
}
```

---

### 3.5 删除农资产品

**URL**：`DELETE /api/input/products/{id}`
**认证**：需要 JWT Bearer
**错误码**：`700205`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 产品 ID | `2001` |

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
  "code": 700205,
  "message": "农资产品不存在",
  "data": null
}
```

---

### 3.6 导出农资产品 CSV

**URL**：`GET /api/input/products/export`
**认证**：需要 JWT Bearer
**错误码**：`700206`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `keyword` | string | ❌ | 产品名称模糊搜索 | `尿素` |
| `type` | string | ❌ | 产品类型枚举 code | `FERTILIZER` |
| `status` | string | ❌ | 状态枚举 code | `ACTIVE` |

#### 响应

**非 R\<T\> 包装**。直接返回文件下载：

- `Content-Type: text/csv;charset=UTF-8`
- `Content-Disposition: attachment; filename=农资产品.csv`
- 编码：UTF-8 BOM

CSV 列顺序：产品编码、产品名称、类型、生产厂家、规格、批号、生产日期、保质期至、登记证号、状态。

---

## 4. 农资采购 AgriPurchaseController（`/api/input/purchases`）

### 4.1 采购列表（分页）

**URL**：`GET /api/input/purchases/list`
**认证**：需要 JWT Bearer
**错误码**：`700300`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，从 1 开始，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 采购单号模糊搜索 | `PO` |
| `status` | string | ❌ | 状态枚举 code，见 PurchaseStatus | `PENDING` |
| `farmerId` | number | ❌ | 按农户 ID 筛选 | `1001` |

```typescript
export interface PurchasePageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;
  farmerId?: number;
}
```

#### 响应体 `PageResult<PurchaseResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 采购单 ID |
| `purchaseNo` | string | ✅ | 采购单号 |
| `productId` | number | ✅ | 农资产品 ID |
| `productName` | string | ✅ | 产品名称（冗余） |
| `supplierId` | number \| null | ❌ | 供应商 ID |
| `supplierName` | string \| null | ❌ | 供应商名称（冗余） |
| `quantity` | number | ✅ | 采购数量（精度 2 位） |
| `unit` | string | ✅ | 单位：`kg` / `L` / `袋` / `瓶` |
| `unitPrice` | number | ✅ | 单价（精度 2 位） |
| `totalAmount` | number | ✅ | 总金额（精度 2 位） |
| `purchaseDate` | string \| null | ❌ | 采购日期，`yyyy-MM-dd` |
| `farmerId` | number \| null | ❌ | 采购人（农户）ID |
| `status` | `EnumValue<string>` | ✅ | 采购单状态，见 PurchaseStatus |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface PurchaseResponse {
  id: number;
  purchaseNo: string;
  productId: number;
  productName: string;
  supplierId: number | null;
  supplierName: string | null;
  quantity: number;
  unit: string;
  unitPrice: number;
  totalAmount: number;
  purchaseDate: string | null;
  farmerId: number | null;
  status: EnumValue<string>;
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
        "purchaseNo": "PO20260412001",
        "productId": 2001,
        "productName": "尿素（含氮量46%）",
        "supplierId": 1001,
        "supplierName": "绿丰农资有限公司",
        "quantity": 100.00,
        "unit": "kg",
        "unitPrice": 2.50,
        "totalAmount": 250.00,
        "purchaseDate": "2026-04-12",
        "farmerId": 1001,
        "status": { "code": "PENDING", "desc": "待审批" },
        "remark": null,
        "createdAt": "2026-04-12 10:00:00",
        "updatedAt": "2026-04-12 10:00:00"
      }
    ],
    "total": 30,
    "current": 1,
    "size": 10
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 700300,
  "message": "采购列表查询失败",
  "data": null
}
```

---

### 4.2 采购详情

**URL**：`GET /api/input/purchases/{id}`
**认证**：需要 JWT Bearer
**错误码**：`700301`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 采购单 ID | `3001` |

#### 响应体 `PurchaseResponse`

字段同 [4.1 响应体 PurchaseResponse](#41-采购列表分页)。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* PurchaseResponse */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 700301,
  "message": "采购单不存在",
  "data": null
}
```

---

### 4.3 创建采购单

**URL**：`POST /api/input/purchases`
**认证**：需要 JWT Bearer
**错误码**：`700302`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 CreatePurchaseRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `productId` | number | ✅ | 农资产品 ID | `2001` |
| `productName` | string | ✅ | 产品名称（冗余） | `尿素（含氮量46%）` |
| `supplierId` | number | ❌ | 供应商 ID | `1001` |
| `supplierName` | string | ❌ | 供应商名称（冗余） | `绿丰农资有限公司` |
| `quantity` | number | ✅ | 采购数量 | `100.00` |
| `unit` | string | ✅ | 单位 | `kg` |
| `unitPrice` | number | ✅ | 单价 | `2.50` |
| `totalAmount` | number | ❌ | 总金额（不传则自动计算） | `250.00` |
| `purchaseDate` | string | ❌ | 采购日期 | `2026-04-12` |
| `farmerId` | number | ❌ | 采购人（农户）ID | `1001` |
| `remark` | string | ❌ | 备注 | `null` |

```typescript
export interface CreatePurchaseRequest {
  productId: number;
  productName: string;
  supplierId?: number;
  supplierName?: string;
  quantity: number;
  unit: string;
  unitPrice: number;
  totalAmount?: number;
  purchaseDate?: string;
  farmerId?: number;
  remark?: string;
}
```

#### 业务规则

- 创建时 `status` 强制设置为 `PENDING`。
- 若请求未传 `totalAmount`，但 `quantity` 和 `unitPrice` 均不为空，则自动计算：`totalAmount = quantity * unitPrice`。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* PurchaseResponse，status 固定为 PENDING */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 700302,
  "message": "采购单创建失败",
  "data": null
}
```

---

### 4.4 更新采购单

**URL**：`PUT /api/input/purchases/{id}`
**认证**：需要 JWT Bearer
**错误码**：`700303` / `700304` / `700305`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 采购单 ID | `3001` |

#### 请求体 UpdatePurchaseRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `quantity` | number | ❌ | 采购数量 | `120.00` |
| `unitPrice` | number | ❌ | 单价 | `2.60` |
| `remark` | string | ❌ | 备注 | `追加采购` |

```typescript
export interface UpdatePurchaseRequest {
  quantity?: number;
  unitPrice?: number;
  remark?: string;
}
```

#### 业务规则

- 已取消（`CANCELLED`）的采购单不允许修改，抛出业务异常。
- `purchaseNo` 字段不可通过更新接口修改，服务端强制置空后忽略。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* PurchaseResponse */ }
}
```

#### 响应示例（失败 — 状态不允许）

```json
{
  "code": 700303,
  "message": "已取消的采购单不允许修改",
  "data": null
}
```

#### 响应示例（失败 — 不存在）

```json
{
  "code": 700304,
  "message": "采购单不存在",
  "data": null
}
```

#### 响应示例（失败 — 更新失败）

```json
{
  "code": 700305,
  "message": "采购单更新失败",
  "data": null
}
```

---

### 4.5 删除采购单

**URL**：`DELETE /api/input/purchases/{id}`
**认证**：需要 JWT Bearer
**错误码**：`700306` / `700307`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 采购单 ID | `3001` |

#### 业务规则

- 已收货（`RECEIVED`）的采购单不能删除，抛出业务异常。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

#### 响应示例（失败 — 状态不允许）

```json
{
  "code": 700306,
  "message": "已收货的采购单不能删除",
  "data": null
}
```

#### 响应示例（失败 — 不存在）

```json
{
  "code": 700307,
  "message": "采购单不存在",
  "data": null
}
```

---

### 4.6 审批采购单

**URL**：`PUT /api/input/purchases/{id}/approve`
**认证**：需要 JWT Bearer
**错误码**：`700308`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 采购单 ID | `3001` |

#### 业务规则

- 仅允许 `PENDING` → `APPROVED` 状态转换。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* PurchaseResponse，status 变为 APPROVED */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 700308,
  "message": "仅待审批状态的采购单可以审批",
  "data": null
}
```

---

### 4.7 确认收货

**URL**：`PUT /api/input/purchases/{id}/receive`
**认证**：需要 JWT Bearer
**错误码**：`700309`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 采购单 ID | `3001` |

#### 业务规则

- 仅允许 `APPROVED` → `RECEIVED` 状态转换。
- 确认收货后，**自动更新对应产品的库存**：按 `productId` 查找 `agri_inventory` 中的第一条记录，将 `stockQuantity` 增加本次采购的 `quantity`，并重新计算库存状态（见[库存状态机](#81-采购单状态机-purchasestatus)）。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* PurchaseResponse，status 变为 RECEIVED */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 700309,
  "message": "仅已审批的采购单可以确认收货",
  "data": null
}
```

---

### 4.8 取消采购单

**URL**：`PUT /api/input/purchases/{id}/cancel`
**认证**：需要 JWT Bearer
**错误码**：`700310`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 采购单 ID | `3001` |

#### 业务规则

- 仅允许 `PENDING` → `CANCELLED` 状态转换。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* PurchaseResponse，status 变为 CANCELLED */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 700310,
  "message": "仅待审批状态的采购单可以取消",
  "data": null
}
```

---

### 4.9 导出采购记录 CSV

**URL**：`GET /api/input/purchases/export`
**认证**：需要 JWT Bearer
**错误码**：`700311`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `keyword` | string | ❌ | 采购单号模糊搜索 | `PO` |
| `status` | string | ❌ | 状态枚举 code | `PENDING` |
| `farmerId` | number | ❌ | 按农户筛选 | `1001` |

#### 响应

**非 R\<T\> 包装**。直接返回文件下载：

- `Content-Type: text/csv;charset=UTF-8`
- `Content-Disposition: attachment; filename=农资采购.csv`
- 编码：UTF-8 BOM

CSV 列顺序：采购单号、产品名称、供应商、数量、单位、单价、总金额、采购日期、状态。

---

## 5. 农资库存 AgriInventoryController（`/api/input/inventory`）

### 5.1 库存列表（分页）

**URL**：`GET /api/input/inventory/list`
**认证**：需要 JWT Bearer
**错误码**：`700400`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，从 1 开始，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 产品名称模糊搜索 | `尿素` |
| `status` | string | ❌ | 库存状态枚举 code，见 InventoryStatus | `LOW` |
| `farmerId` | number | ❌ | 按农户 ID 筛选 | `1001` |

```typescript
export interface InventoryPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;
  farmerId?: number;
}
```

#### 响应体 `PageResult<InventoryResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 库存记录 ID |
| `productId` | number | ✅ | 农资产品 ID |
| `productName` | string | ✅ | 产品名称（冗余） |
| `farmerId` | number \| null | ❌ | 所属农户 ID |
| `stockQuantity` | number | ✅ | 当前库存数量（精度 2 位） |
| `unit` | string | ✅ | 单位 |
| `warningLevel` | number \| null | ❌ | 库存预警阈值 |
| `maxLevel` | number \| null | ❌ | 超储上限 |
| `warehouse` | string \| null | ❌ | 存放位置 |
| `status` | `EnumValue<string>` | ✅ | 库存状态，见 InventoryStatus（系统自动计算） |
| `remark` | string \| null | ❌ | 备注（调整时可记录调整原因） |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface InventoryResponse {
  id: number;
  productId: number;
  productName: string;
  farmerId: number | null;
  stockQuantity: number;
  unit: string;
  warningLevel: number | null;
  maxLevel: number | null;
  warehouse: string | null;
  status: EnumValue<string>;
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
        "productId": 2001,
        "productName": "尿素（含氮量46%）",
        "farmerId": 1001,
        "stockQuantity": 50.00,
        "unit": "kg",
        "warningLevel": 20.00,
        "maxLevel": 500.00,
        "warehouse": "A区1号库",
        "status": { "code": "NORMAL", "desc": "正常" },
        "remark": null,
        "createdAt": "2026-04-12 11:00:00",
        "updatedAt": "2026-04-12 11:00:00"
      }
    ],
    "total": 40,
    "current": 1,
    "size": 10
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 700400,
  "message": "库存列表查询失败",
  "data": null
}
```

---

### 5.2 库存详情

**URL**：`GET /api/input/inventory/{id}`
**认证**：需要 JWT Bearer
**错误码**：`700401`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 库存记录 ID | `4001` |

#### 响应体 `InventoryResponse`

字段同 [5.1 响应体 InventoryResponse](#51-库存列表分页)。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* InventoryResponse */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 700401,
  "message": "库存记录不存在",
  "data": null
}
```

---

### 5.3 创建库存记录

**URL**：`POST /api/input/inventory`
**认证**：需要 JWT Bearer
**错误码**：`700402`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 CreateInventoryRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `productId` | number | ✅ | 农资产品 ID | `2001` |
| `productName` | string | ✅ | 产品名称（冗余） | `尿素（含氮量46%）` |
| `farmerId` | number | ❌ | 所属农户 ID | `1001` |
| `stockQuantity` | number | ✅ | 当前库存数量 | `50.00` |
| `unit` | string | ✅ | 单位 | `kg` |
| `warningLevel` | number | ❌ | 库存预警阈值 | `20.00` |
| `maxLevel` | number | ❌ | 超储上限 | `500.00` |
| `warehouse` | string | ❌ | 存放位置 | `A区1号库` |
| `remark` | string | ❌ | 备注 | `null` |

```typescript
export interface CreateInventoryRequest {
  productId: number;
  productName: string;
  farmerId?: number;
  stockQuantity: number;
  unit: string;
  warningLevel?: number;
  maxLevel?: number;
  warehouse?: string;
  remark?: string;
}
```

#### 业务规则

- `status` 由服务端根据 `stockQuantity`、`warningLevel`、`maxLevel` 自动计算（见[库存状态计算规则](#82-库存状态计算规则-inventorystatus)），忽略请求传入的 `status`。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* InventoryResponse，status 由系统自动计算 */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 700402,
  "message": "库存记录创建失败",
  "data": null
}
```

---

### 5.4 更新库存记录

**URL**：`PUT /api/input/inventory/{id}`
**认证**：需要 JWT Bearer
**错误码**：`700403` / `700404`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 库存记录 ID | `4001` |

#### 请求体 UpdateInventoryRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `stockQuantity` | number | ❌ | 当前库存数量 | `60.00` |
| `warningLevel` | number | ❌ | 库存预警阈值 | `25.00` |
| `maxLevel` | number | ❌ | 超储上限 | `600.00` |
| `warehouse` | string | ❌ | 存放位置 | `B区2号库` |
| `remark` | string | ❌ | 备注 | `调整阈值` |

```typescript
export interface UpdateInventoryRequest {
  stockQuantity?: number;
  warningLevel?: number;
  maxLevel?: number;
  warehouse?: string;
  remark?: string;
}
```

#### 业务规则

- `status` 自动重新计算；若记录不存在，抛出 `NOT_FOUND`。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* InventoryResponse，status 已重新计算 */ }
}
```

#### 响应示例（失败 — 不存在）

```json
{
  "code": 700403,
  "message": "库存记录不存在",
  "data": null
}
```

#### 响应示例（失败 — 更新失败）

```json
{
  "code": 700404,
  "message": "库存记录更新失败",
  "data": null
}
```

---

### 5.5 删除库存记录

**URL**：`DELETE /api/input/inventory/{id}`
**认证**：需要 JWT Bearer
**错误码**：`700405`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 库存记录 ID | `4001` |

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
  "code": 700405,
  "message": "库存记录不存在",
  "data": null
}
```

---

### 5.6 调整库存数量

**URL**：`POST /api/input/inventory/{id}/adjust`
**认证**：需要 JWT Bearer
**错误码**：`700406` / `700407` / `700408`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 库存记录 ID | `4001` |

#### 请求体 AdjustInventoryRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `delta` | number | ✅ | 调整量；正数增加，负数减少 | `10.00` |
| `reason` | string | ❌ | 调整原因，会写入 `remark` 字段 | `盘点补录` |

```typescript
export interface AdjustInventoryRequest {
  delta: number;
  reason?: string;
}
```

#### 业务规则

- 调整后库存（`currentStock + delta`）不能为负数，否则抛出业务异常。
- 调整后自动重新计算 `status`（见[库存状态计算规则](#82-库存状态计算规则-inventorystatus)）。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* InventoryResponse，stockQuantity 已调整，status 已重新计算 */ }
}
```

#### 响应示例（失败 — 库存记录不存在）

```json
{
  "code": 700406,
  "message": "库存记录不存在",
  "data": null
}
```

#### 响应示例（失败 — 库存不足）

```json
{
  "code": 700407,
  "message": "调整后库存不能为负数",
  "data": null
}
```

#### 响应示例（失败 — 调整失败）

```json
{
  "code": 700408,
  "message": "库存调整失败",
  "data": null
}
```

---

### 5.7 库存预警列表

**URL**：`GET /api/input/inventory/alerts`
**认证**：需要 JWT Bearer
**错误码**：`700409`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `List<InventoryResponse>`

返回所有 `status` 为 `LOW` 或 `EMPTY` 的库存记录，按 `stockQuantity` 升序排列（库存最少的排在前面）。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 4002,
      "productId": 2003,
      "productName": "草甘膦除草剂",
      "farmerId": 1001,
      "stockQuantity": 0,
      "unit": "L",
      "warningLevel": 5.00,
      "maxLevel": 100.00,
      "warehouse": "B区1号库",
      "status": { "code": "EMPTY", "desc": "无库存" },
      "remark": null,
      "createdAt": "2026-04-10 08:00:00",
      "updatedAt": "2026-04-12 15:00:00"
    },
    {
      "id": 4001,
      "productId": 2001,
      "productName": "尿素（含氮量46%）",
      "farmerId": 1001,
      "stockQuantity": 15.00,
      "unit": "kg",
      "warningLevel": 20.00,
      "maxLevel": 500.00,
      "warehouse": "A区1号库",
      "status": { "code": "LOW", "desc": "库存偏低" },
      "remark": null,
      "createdAt": "2026-04-12 11:00:00",
      "updatedAt": "2026-04-12 16:00:00"
    }
  ]
}
```

#### 响应示例（失败）

```json
{
  "code": 700409,
  "message": "库存预警查询失败",
  "data": null
}
```

---

### 5.8 导出库存 CSV

**URL**：`GET /api/input/inventory/export`
**认证**：需要 JWT Bearer
**错误码**：`700410`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `keyword` | string | ❌ | 产品名称模糊搜索 | `尿素` |
| `status` | string | ❌ | 状态枚举 code | `LOW` |
| `farmerId` | number | ❌ | 按农户筛选 | `1001` |

#### 响应

**非 R\<T\> 包装**。直接返回文件下载：

- `Content-Type: text/csv;charset=UTF-8`
- `Content-Disposition: attachment; filename=农资库存.csv`
- 编码：UTF-8 BOM

CSV 列顺序：产品名称、农户ID、库存数量、单位、预警阈值、存放位置、状态。

---

## 6. 农资溯源链路 AgriTraceController（`/api/input/trace`）

### 6.1 溯源链路查询

**URL**：`GET /api/input/trace/{traceCode}`
**认证**：需要 JWT Bearer
**错误码**：`700500` / `700501`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `traceCode` | string | ✅ | 溯源码 | `TC2025001` |

#### 溯源链路构建逻辑

```
traceCode
  → agri_usage（按 traceCode 查询使用记录列表）
      → 提取所有 productId
          → agri_purchase（按 productId IN 查询采购记录列表）
              → 提取所有 supplierId
                  → agri_supplier（按 supplierId IN 查询供应商列表）
```

#### 业务规则

- 若 `traceCode` 对应的使用记录不存在，抛出 `NOT_FOUND`。
- 溯源链路为快照数据，不涉及任何写操作。

#### 响应体 `TraceChainResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `traceCode` | string | ✅ | 溯源码 |
| `usages` | `UsageResponse[]` | ✅ | 该溯源码下的所有使用记录 |
| `purchases` | `PurchaseResponse[]` | ✅ | 涉及产品的所有采购记录 |
| `suppliers` | `SupplierResponse[]` | ✅ | 涉及供应商的完整信息 |

```typescript
export interface TraceChainResponse {
  traceCode: string;
  usages: UsageResponse[];
  purchases: PurchaseResponse[];
  suppliers: SupplierResponse[];
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "traceCode": "TC2025001",
    "usages": [ { /* UsageResponse */ } ],
    "purchases": [ { /* PurchaseResponse */ } ],
    "suppliers": [ { /* SupplierResponse */ } ]
  }
}
```

#### 响应示例（失败 — 溯源码不存在）

```json
{
  "code": 700500,
  "message": "溯源码对应的使用记录不存在: TC999999",
  "data": null
}
```

#### 响应示例（失败 — 查询失败）

```json
{
  "code": 700501,
  "message": "溯源链路查询失败",
  "data": null
}
```

---

## 附录：批量查询端点

> 以下端点由 `AgriUsageController` 提供，与溯源模块紧密关联。

### 使用记录 CRUD + 导出（8 端点）

| # | 接口中文名 | URL | 方法 | 认证 | 错误码 |
|---|-----------|-----|------|------|--------|
| A1 | 使用记录列表（分页） | `/api/input/usage/list` | GET | ✅ | `700600` |
| A2 | 使用记录详情 | `/api/input/usage/{id}` | GET | ✅ | `700601` |
| A3 | 创建使用记录 | `/api/input/usage` | POST | ✅ | `700602` / `700603` |
| A4 | 更新使用记录 | `/api/input/usage/{id}` | PUT | ✅ | `700604` |
| A5 | 删除使用记录 | `/api/input/usage/{id}` | DELETE | ✅ | `700605` |
| A6 | 导出使用记录 CSV | `/api/input/usage/export` | GET | ✅ | `700606` |
| A7 | 按批次查询使用记录 | `/api/input/usage/by-batch` | GET | ✅ | `700607` |
| A8 | 按溯源码查询使用记录 | `/api/input/usage/by-trace` | GET | ✅ | `700608` |

### 使用记录列表（分页）

**URL**：`GET /api/input/usage/list`

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，从 1 开始，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 产品名称模糊搜索 | `尿素` |
| `method` | string | ❌ | 施用方式：`撒施` / `喷洒` / `滴灌` / `穴施` | `喷洒` |
| `orchardId` | number | ❌ | 按果园 ID 筛选 | `5001` |

#### 响应体 `PageResult<UsageResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 使用记录 ID |
| `productId` | number | ✅ | 农资产品 ID |
| `productName` | string | ✅ | 产品名称（冗余） |
| `batchId` | number \| null | ❌ | 种植批次 ID |
| `batchCode` | string \| null | ❌ | 批次编码（冗余） |
| `operationId` | number \| null | ❌ | 作业操作 ID |
| `orchardId` | number \| null | ❌ | 果园 ID |
| `orchardName` | string \| null | ❌ | 果园名称（冗余） |
| `quantity` | number | ✅ | 使用量（精度 2 位） |
| `unit` | string | ✅ | 单位 |
| `usageDate` | string | ✅ | 使用日期，`yyyy-MM-dd` |
| `operator` | string \| null | ❌ | 操作人姓名 |
| `method` | `EnumValue<string>` | ✅ | 施用方式，见 UsageMethod |
| `traceCode` | string \| null | ❌ | 溯源码 |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface UsageResponse {
  id: number;
  productId: number;
  productName: string;
  batchId: number | null;
  batchCode: string | null;
  operationId: number | null;
  orchardId: number | null;
  orchardName: string | null;
  quantity: number;
  unit: string;
  usageDate: string;
  operator: string | null;
  method: EnumValue<string>;
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
        "id": 5001,
        "productId": 2001,
        "productName": "尿素（含氮量46%）",
        "batchId": 3001,
        "batchCode": "BATCH2026001",
        "operationId": null,
        "orchardId": 5001,
        "orchardName": "洛川红富士一号果园",
        "quantity": 5.00,
        "unit": "kg",
        "usageDate": "2026-04-12",
        "operator": "张三",
        "method": { "code": "撒施", "desc": "撒施" },
        "traceCode": "TC2025001",
        "remark": null,
        "createdAt": "2026-04-12 14:00:00",
        "updatedAt": "2026-04-12 14:00:00"
      }
    ],
    "total": 60,
    "current": 1,
    "size": 10
  }
}
```

### 使用记录详情

**URL**：`GET /api/input/usage/{id}`

字段同 [使用记录列表响应体 UsageResponse](#使用记录-crud--导出-8-端点)。

### 创建使用记录

**URL**：`POST /api/input/usage`

#### 请求体 CreateUsageRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `productId` | number | ✅ | 农资产品 ID | `2001` |
| `productName` | string | ✅ | 产品名称（冗余） | `尿素（含氮量46%）` |
| `batchId` | number | ❌ | 种植批次 ID | `3001` |
| `batchCode` | string | ❌ | 批次编码（冗余） | `BATCH2026001` |
| `operationId` | number | ❌ | 作业操作 ID | `null` |
| `orchardId` | number | ❌ | 果园 ID | `5001` |
| `orchardName` | string | ❌ | 果园名称（冗余） | `洛川红富士一号果园` |
| `quantity` | number | ✅ | 使用量 | `5.00` |
| `unit` | string | ✅ | 单位 | `kg` |
| `usageDate` | string | ✅ | 使用日期 | `2026-04-12` |
| `operator` | string | ❌ | 操作人姓名 | `张三` |
| `method` | string | ✅ | 施用方式 | `撒施` |
| `traceCode` | string | ❌ | 溯源码 | `TC2025001` |
| `remark` | string | ❌ | 备注 | `null` |

```typescript
export interface CreateUsageRequest {
  productId: number;
  productName: string;
  batchId?: number;
  batchCode?: string;
  operationId?: number;
  orchardId?: number;
  orchardName?: string;
  quantity: number;
  unit: string;
  usageDate: string;
  operator?: string;
  method: string;
  traceCode?: string;
  remark?: string;
}
```

#### 业务规则（库存联动）

- 记录保存后，系统自动按 `productId` 查找对应库存记录，并**扣减** `quantity` 数量。
- 若扣减后库存为负，抛出业务异常：`"库存不足，当前库存: {current} {unit}"`，事务回滚。
- 扣减后自动重新计算库存 `status`（见[库存状态计算规则](#82-库存状态计算规则-inventorystatus)）。

#### 响应示例（失败 — 库存不足）

```json
{
  "code": 700603,
  "message": "库存不足，当前库存: 3.00 kg",
  "data": null
}
```

### 更新使用记录

**URL**：`PUT /api/input/usage/{id}`

#### 业务规则

- 若记录不存在，抛出 `NOT_FOUND`。
- 更新操作**不触发**库存联动，如需修正库存需手动调用库存调整接口。

### 删除使用记录

**URL**：`DELETE /api/input/usage/{id}`

#### 业务规则（库存联动）

- 删除成功后，系统自动按 `productId` 查找对应库存记录，**恢复**（加回）该记录的 `quantity`。
- 恢复后自动重新计算库存 `status`。

### 导出使用记录 CSV

**URL**：`GET /api/input/usage/export`

**非 R\<T\> 包装**。直接返回文件下载：

- `Content-Type: text/csv;charset=UTF-8`
- `Content-Disposition: attachment; filename=农资使用记录.csv`
- 编码：UTF-8 BOM

CSV 列顺序：产品名称、批次编码、果园名称、使用量、单位、使用日期、操作人、施用方式、溯源码。

### 按批次查询使用记录

**URL**：`GET /api/input/usage/by-batch`

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `batchId` | number | ✅ | 种植批次 ID | `3001` |

响应：`R<List<UsageResponse>>`，按 `usageDate` 降序排列。

### 按溯源码查询使用记录

**URL**：`GET /api/input/usage/by-trace`

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `traceCode` | string | ✅ | 溯源码 | `TC2025001` |

响应：`R<List<UsageResponse>>`，按 `usageDate` 降序排列。

---

## 7. 状态机

### 7.1 采购单状态机 PurchaseStatus

```
                ┌─────────────────────┐
                │        创建         │
                └──────────┬──────────┘
                           │ status=PENDING
                           ▼
                       PENDING
                      /        \
          approve /              \ cancel
                 /                \
            APPROVED          CANCELLED（终态）
                |
         receive |
                 |
            RECEIVED（终态）
```

### 7.2 库存状态计算规则 InventoryStatus

库存状态由系统在写入时自动计算，非人工设置：

```
stockQuantity == null 或 stockQuantity <= 0        →  EMPTY
stockQuantity > maxLevel（maxLevel != null）        →  OVERSTOCKED
stockQuantity <= warningLevel（warningLevel != null） →  LOW
其他                                                →  NORMAL
```

**优先级**：EMPTY > OVERSTOCKED > LOW > NORMAL

### 7.3 供应商状态机 SupplierStatus

```
                     ┌─────────┐
                     │ PENDING │（创建默认）
                     └────┬────┘
            ┌────────────┼────────────┐
            ▼            ▼            ▼
      ┌──────────┐ ┌──────────┐ ┌────────────┐
      │ APPROVED │ │ REJECTED │ │ BLACKLISTED │（终态）
      └──────────┘ └────┬─────┘ └────────────┘
                       │ reinstate
                       ▼
                  ┌─────────┐
                  │ PENDING │
                  └─────────┘
```

---

## 8. 枚举声明

### 8.1 SupplierStatus（供应商状态）

| code | desc |
|------|------|
| `PENDING` | 待审核 |
| `APPROVED` | 已通过 |
| `REJECTED` | 已拒绝（可重新提交审核） |
| `BLACKLISTED` | 已列入黑名单（终态） |

### 8.2 ProductType（农资产品类型）

| code | desc |
|------|------|
| `FERTILIZER` | 化肥 |
| `PESTICIDE` | 农药 |
| `SEED` | 种子 |
| `TOOL` | 农具 |

### 8.3 ProductStatus（农资产品状态）

| code | desc |
|------|------|
| `ACTIVE` | 在用 |
| `DISCONTINUED` | 停用 |

### 8.4 PurchaseStatus（采购单状态）

| code | desc |
|------|------|
| `PENDING` | 待审批 |
| `APPROVED` | 已审批 |
| `RECEIVED` | 已收货（终态） |
| `CANCELLED` | 已取消（终态） |

### 8.5 InventoryStatus（库存状态）

| code | desc |
|------|------|
| `NORMAL` | 正常 |
| `LOW` | 库存偏低 |
| `EMPTY` | 无库存 |
| `OVERSTOCKED` | 超储 |

> 注意：此枚举由系统自动计算，不接受前端传入。

### 8.6 UsageMethod（施用方式）

| code | desc |
|------|------|
| `撒施` | 撒施 |
| `喷洒` | 喷洒 |
| `滴灌` | 滴灌 |
| `穴施` | 穴施 |

### 8.7 AuditDecision（审核决策）

| code | desc |
|------|------|
| `APPROVE` | 通过 |
| `REJECT` | 拒绝 |
| `BLACKLIST` | 列入黑名单 |

---

## 9. 错误码表

| 错误码 | HTTP 状态码 | 说明 |
|--------|-----------|------|
| `700100` | 500 | 供应商列表查询失败 |
| `700101` | 404 | 供应商不存在 |
| `700102` | 400 | 供应商创建失败 |
| `700103` | 404 | 供应商不存在（更新时） |
| `700104` | 500 | 供应商更新失败 |
| `700105` | 404 | 供应商不存在（删除时） |
| `700106` | 404 | 供应商不存在（审核时） |
| `700107` | 400 | 无效的审核决策 |
| `700108` | 409 | 仅已拒绝状态的供应商可重新提交审核 |
| `700109` | 404 | 供应商不存在（重新提交时） |
| `700110` | 500 | 供应商导出失败 |
| `700200` | 500 | 农资产品列表查询失败 |
| `700201` | 404 | 农资产品不存在 |
| `700202` | 400 | 农资产品创建失败 |
| `700203` | 404 | 农资产品不存在（更新时） |
| `700204` | 500 | 农资产品更新失败 |
| `700205` | 404 | 农资产品不存在（删除时） |
| `700206` | 500 | 农资产品导出失败 |
| `700300` | 500 | 采购列表查询失败 |
| `700301` | 404 | 采购单不存在 |
| `700302` | 400 | 采购单创建失败 |
| `700303` | 409 | 已取消的采购单不允许修改 |
| `700304` | 404 | 采购单不存在（更新时） |
| `700305` | 500 | 采购单更新失败 |
| `700306` | 409 | 已收货的采购单不能删除 |
| `700307` | 404 | 采购单不存在（删除时） |
| `700308` | 409 | 仅待审批状态的采购单可以审批 |
| `700309` | 409 | 仅已审批的采购单可以确认收货 |
| `700310` | 409 | 仅待审批状态的采购单可以取消 |
| `700311` | 500 | 采购导出失败 |
| `700400` | 500 | 库存列表查询失败 |
| `700401` | 404 | 库存记录不存在 |
| `700402` | 400 | 库存记录创建失败 |
| `700403` | 404 | 库存记录不存在（更新时） |
| `700404` | 500 | 库存记录更新失败 |
| `700405` | 404 | 库存记录不存在（删除时） |
| `700406` | 404 | 库存记录不存在（调整时） |
| `700407` | 400 | 调整后库存不能为负数 |
| `700408` | 500 | 库存调整失败 |
| `700409` | 500 | 库存预警查询失败 |
| `700410` | 500 | 库存导出失败 |
| `700500` | 404 | 溯源码对应的使用记录不存在 |
| `700501` | 500 | 溯源链路查询失败 |
| `700600` | 500 | 使用记录列表查询失败 |
| `700601` | 404 | 使用记录不存在 |
| `700602` | 400 | 使用记录创建失败 |
| `700603` | 400 | 库存不足 |
| `700604` | 404 | 使用记录不存在（更新时） |
| `700605` | 404 | 使用记录不存在（删除时） |
| `700606` | 500 | 使用记录导出失败 |
| `700607` | 500 | 按批次查询使用记录失败 |
| `700608` | 500 | 按溯源码查询使用记录失败 |

---

## 10. 实现注记

### 10.1 HTTP 方法审计结果

**存在 3 个 PUT 用于状态变更的端点，建议后续迁移为 POST。**

逐端点审计 6 个 Controller 共 33 个端点：

| Controller | 状态变更端点 | HTTP 方法 | 结论 |
|-----------|-------------|----------|------|
| AgriSupplierController | audit, reinstate | PUT | 建议迁移为 POST |
| AgriProductController | 无状态变更端点（仅 CRUD） | - | 正确 |
| AgriPurchaseController | approve, receive, cancel | PUT | 建议迁移为 POST |
| AgriInventoryController | adjust | POST | 正确 |
| AgriUsageController | 无状态变更端点（仅 CRUD） | - | 正确 |
| AgriTraceController | 无状态变更端点（仅查询） | - | 正确 |

CRUD 操作均使用标准 HTTP 方法（GET 查询、POST 创建、PUT 更新、DELETE 删除），语义正确。

**注意**：当前 Controller 实现中 `approve`、`receive`、`cancel`、`audit`、`reinstate` 使用 PUT 方法，与 CONVENTIONS.md 建议的「状态变更用 POST」不一致。本契约照实际 Controller 代码记录，后续迁移需同步更新本文件。

### 10.2 String → EnumValue 迁移

当前后端 Entity 中以下字段为 `String` 类型，需迁移为 `EnumValue` 序列化（返回 `{code, desc}` 对象）：

| Entity | 字段 | 当前类型 | 目标类型 |
|--------|------|---------|---------|
| AgriSupplier | `status` | String | `EnumValue<SupplierStatus>` |
| AgriProduct | `type` | String | `EnumValue<ProductType>` |
| AgriProduct | `status` | String | `EnumValue<ProductStatus>` |
| AgriPurchase | `status` | String | `EnumValue<PurchaseStatus>` |
| AgriInventory | `status` | String | `EnumValue<InventoryStatus>` |
| AgriUsage | `method` | String | `EnumValue<UsageMethod>` |

共 6 个 String 字段需迁移。迁移后前端需适配，读取 `status.code` 而非直接读 `status`。

### 10.3 Entity → Request/Response DTO 重命名

当前 6 个 Controller 全部直接返回 Entity 对象（AgriSupplier、AgriProduct 等），不符合 CONVENTIONS.md DTO 命名约定。

**目标状态**：

| Controller | 当前返回 | 目标 Request | 目标 Response |
|-----------|---------|-------------|--------------|
| AgriSupplierController | `AgriSupplier` | `CreateSupplierRequest` / `UpdateSupplierRequest` / `AuditSupplierRequest` | `SupplierResponse` |
| AgriProductController | `AgriProduct` | `CreateProductRequest` / `UpdateProductRequest` | `ProductResponse` |
| AgriPurchaseController | `AgriPurchase` | `CreatePurchaseRequest` / `UpdatePurchaseRequest` | `PurchaseResponse` |
| AgriInventoryController | `AgriInventory` | `CreateInventoryRequest` / `UpdateInventoryRequest` / `AdjustInventoryRequest` | `InventoryResponse` |
| AgriUsageController | `AgriUsage` | `CreateUsageRequest` / `UpdateUsageRequest` | `UsageResponse` |
| AgriTraceController | `AgriTraceChainVO` | 无（无入参） | `TraceChainResponse` |

**注意**：`AgriTraceChainVO` 命名不符合约定（禁用 `VO` 后缀），需重命名为 `TraceChainResponse`。

**迁移风险**：
- Entity 字段 `createTime`/`updateTime`（BaseEntity 继承）需映射为 DTO 的 `createdAt`/`updatedAt`（CONVENTIONS.md 命名约定）。
- `AgriUsage.method` 为中文枚举值（撒施/喷洒/滴灌/穴施），迁移为 EnumValue 时需保留中文 code。
- `audit` 端点当前使用 `Map<String, String>` 接收参数，需迁移为 `AuditSupplierRequest` DTO。
- `adjust` 端点当前使用 `Map<String, Object>` 接收参数，需迁移为 `AdjustInventoryRequest` DTO。

### 10.4 分页参数迁移 PageParam

当前各 Controller 直接使用 `@RequestParam int page, int size, ...`，目标迁移为全局 `PageParam` 对象。

### 10.5 跨模块依赖

| 依赖方向 | 说明 | 触发时机 |
|---------|------|---------|
| `input → planting.md` | 使用记录关联 `orchardId`（果园）和 `batchId`（种植批次） | 创建/查询使用记录时 |
| `input → trace.md` | 使用记录的 `traceCode` 进入溯源链路 | 创建使用记录写入溯源码时 |
| `planting.md → input` | 种植模块引用农资使用数据（施肥记录等） | 关联契约 |
| `trace.md → input` | 溯源模块查询农资溯源链路 | 查询溯源详情时 |

### 10.6 跨模块库存联动

农资采购和使用记录均会自动触发库存更新，联动规则如下：

| 触发操作 | 库存变化 | 触发条件 |
|----------|----------|----------|
| 确认收货（`/purchases/{id}/receive`） | `stockQuantity += purchase.quantity` | productId 不为空且库存记录存在 |
| 创建使用记录（`POST /usage`） | `stockQuantity -= usage.quantity` | productId 不为空且库存记录存在；若扣减后为负则回滚 |
| 删除使用记录（`DELETE /usage/{id}`） | `stockQuantity += usage.quantity`（恢复） | productId 不为空且库存记录存在 |

所有库存联动操作均在同一事务内完成，库存状态在每次变更后自动重新计算。

---

## 质量 Checklist

- [x] 33 个端点全覆盖（8+6+9+8+1+1，含附录 8 个使用记录端点）
- [x] 55 个错误码（>=15），全在 700000-799999 范围
- [x] 7 个枚举声明完整（SupplierStatus / ProductType / ProductStatus / PurchaseStatus / InventoryStatus / UsageMethod / AuditDecision）
- [x] JSON 示例全用 `message` 字段（禁用 `msg`）
- [x] 分页使用 PageParam 语义 + PageResult\<T\> 结构
- [x] 5 个 CSV 导出（suppliers/products/purchases/inventory/usage）明确标注非 R\<T\> 包装
- [x] 采购单状态机（PENDING→APPROVED→RECEIVED，PENDING→CANCELLED）入文档（§7.1）
- [x] 库存状态计算规则（EMPTY > OVERSTOCKED > LOW > NORMAL）入文档（§7.2）
- [x] 供应商状态机（PENDING→APPROVED/REJECTED/BLACKLISTED，REJECTED→PENDING）入文档（§7.3）
- [x] 溯源链路构建逻辑（usage→purchase→supplier）入文档（§6.1）
- [x] 库存联动规则（收货入库/使用扣减/删除恢复）入文档（§10.6）
- [x] HTTP 方法审计：6 个 Controller 全部 33 端点已验证，3 个状态变更端点用 PUT 需迁移（§10.1）
- [x] 6 个 String→EnumValue 迁移清单（§10.2）
- [x] Entity→Request/Response DTO 重命名（§10.3）
- [x] 跨模块依赖（planting/trace）入文档（§10.5）
