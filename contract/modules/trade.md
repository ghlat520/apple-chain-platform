# M4 交易 (Trade) 模块契约

> 模块：`apple-module-trade`
> URL 前缀：`/api/trade/{supply,need,order,inspection,match,negotiation,chat,contract,payment,invoice,statistics}`
> 错误码段：`500000 ~ 599999`（CONVENTIONS.md:137 权威）
> 最近更新：2026-04-12
> 权威源：本文件
> 后端实现：`apple-module-trade`

---

## ⚠️ 双 Controller 声明（必读）

**TradeOrderController（`/api/trade/order`）为唯一权威 Controller**，操作表 `td_trade_order`，状态机：`DRAFT→CONFIRMED→DELIVERED→COMPLETED`，本文件第 4 章完整描述。

**TradeOrderMvpController（`/api/trade/orders`）为 MVP 演示版，已废弃，待删除**：
- 操作表 `trade_order`（无前缀），与 `td_trade_order` 是**两张不同的表**
- 状态值不同（`PENDING/SHIPPED` vs 正式版 `DRAFT/DELIVERED`）
- 支付状态不同（`UNPAID/PAID` vs 正式版 `PENDING/PAID/REFUNDED`）
- 缺少 `supplyId`/`needId` 关联，无法支撑完整业务链路
- **前端禁止调用 `/api/trade/orders` 路径的任何接口**；所有端点的冲突明细见第 11.1 节

---

## 1. 接口索引（37 端点）

| # | 接口中文名 | URL | 方法 | 认证 |
|---|-----------|-----|------|------|
| 1 | [供货列表（分页）](#21-供货列表分页) | `/api/trade/supply/list` | GET | ✅ |
| 2 | [供货详情](#22-供货详情) | `/api/trade/supply/{id}` | GET | ✅ |
| 3 | [创建供货信息](#23-创建供货信息) | `/api/trade/supply` | POST | ✅ |
| 4 | [更新供货信息](#24-更新供货信息) | `/api/trade/supply/{id}` | PUT | ✅ |
| 5 | [发布供货信息](#25-发布供货信息) | `/api/trade/supply/{id}/publish` | PUT | ✅ |
| 6 | [删除供货信息](#26-删除供货信息) | `/api/trade/supply/{id}` | DELETE | ✅ |
| 7 | [导出供货信息 CSV](#27-导出供货信息-csv) | `/api/trade/supply/export` | GET | ✅ |
| 8 | [采购需求列表（分页）](#31-采购需求列表分页) | `/api/trade/need/list` | GET | ✅ |
| 9 | [采购需求详情](#32-采购需求详情) | `/api/trade/need/{id}` | GET | ✅ |
| 10 | [创建采购需求](#33-创建采购需求) | `/api/trade/need` | POST | ✅ |
| 11 | [更新采购需求](#34-更新采购需求) | `/api/trade/need/{id}` | PUT | ✅ |
| 12 | [发布采购需求](#35-发布采购需求) | `/api/trade/need/{id}/publish` | PUT | ✅ |
| 13 | [删除采购需求](#36-删除采购需求) | `/api/trade/need/{id}` | DELETE | ✅ |
| 14 | [导出采购需求 CSV](#37-导出采购需求-csv) | `/api/trade/need/export` | GET | ✅ |
| 15 | [订单列表（分页）](#41-订单列表分页) | `/api/trade/order/list` | GET | ✅ |
| 16 | [订单详情](#42-订单详情) | `/api/trade/order/{id}` | GET | ✅ |
| 17 | [创建交易订单](#43-创建交易订单) | `/api/trade/order` | POST | ✅ |
| 18 | [确认订单](#44-确认订单) | `/api/trade/order/{id}/confirm` | PUT | ✅ |
| 19 | [标记发货](#45-标记发货) | `/api/trade/order/{id}/deliver` | PUT | ✅ |
| 20 | [完成订单](#46-完成订单) | `/api/trade/order/{id}/complete` | PUT | ✅ |
| 21 | [取消订单](#47-取消订单) | `/api/trade/order/{id}/cancel` | PUT | ✅ |
| 22 | [导出订单 CSV](#48-导出订单-csv) | `/api/trade/order/export` | GET | ✅ |
| 23 | [计算撮合候选](#51-计算撮合候选) | `/api/trade/match/compute` | POST | ✅ |
| 24 | [查询撮合缓存](#52-查询撮合缓存) | `/api/trade/match/supply/{supplyId}/top` | GET | ✅ |
| 25 | [发起议价](#53-发起议价) | `/api/trade/negotiation/start` | POST | ✅ |
| 26 | [报价还价](#54-报价还价) | `/api/trade/negotiation/{id}/offer` | POST | ✅ |
| 27 | [接受报价→自动建单](#55-接受报价自动建单) | `/api/trade/negotiation/{id}/accept` | POST | ✅ |
| 28 | [取消议价](#56-取消议价) | `/api/trade/negotiation/{id}/cancel` | POST | ✅ |
| 29 | [发送聊天消息](#57-发送聊天消息) | `/api/trade/chat/send` | POST | ✅ |
| 30 | [拉取历史消息](#58-拉取历史消息) | `/api/trade/chat/{sessionId}/history` | GET | ✅ |
| 31 | [标记消息已读](#59-标记消息已读) | `/api/trade/chat/{sessionId}/read` | POST | ✅ |
| 32 | [创建质检单](#61-创建质检单) | `/api/trade/inspection` | POST | ✅ |
| 33 | [质检列表（分页）](#62-质检列表分页) | `/api/trade/inspection` | GET | ✅ |
| 34 | [质检详情](#63-质检详情) | `/api/trade/inspection/{id}` | GET | ✅ |
| 35 | [录入检验结果](#64-录入检验结果) | `/api/trade/inspection/{id}/inspect` | PUT | ✅ |
| 36 | [验收通过](#65-验收通过) | `/api/trade/inspection/{id}/accept` | PUT | ✅ |
| 37 | [发起争议](#66-发起争议) | `/api/trade/inspection/{id}/dispute` | PUT | ✅ |

> M8 合同支付开票（5 端点）、交易统计（4 端点）见第 7、8 章，总计 46 端点

| # | 接口中文名 | URL | 方法 | 认证 |
|---|-----------|-----|------|------|
| 38 | [创建合同](#71-创建合同) | `/api/trade/contract/create` | POST | ✅ |
| 39 | [签署合同](#72-签署合同) | `/api/trade/contract/{orderId}/sign` | POST | ✅ |
| 40 | [创建支付](#73-创建支付) | `/api/trade/payment/create` | POST | ✅ |
| 41 | [支付回调](#74-支付回调) | `/api/trade/payment/callback/wechat` | POST | ❌ |
| 42 | [手动开票](#75-手动开票) | `/api/trade/invoice/issue` | POST | ✅ |
| 43 | [交易汇总统计](#81-交易汇总统计) | `/api/trade/statistics/summary` | GET | ✅ |
| 44 | [品种维度统计](#82-品种维度统计) | `/api/trade/statistics/variety` | GET | ✅ |
| 45 | [月度趋势统计](#83-月度趋势统计) | `/api/trade/statistics/trend` | GET | ✅ |
| 46 | [品种价格指数](#84-品种价格指数) | `/api/trade/statistics/price-index` | GET | ✅ |

---

## 2. 供应信息 SupplyInfoController（`/api/trade/supply`）

### 2.1 供货列表（分页）

**URL**：`GET /api/trade/supply/list`
**认证**：需要 JWT Bearer
**权限**：`trade:supply:read`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，从 1 开始，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 供应编号/品种模糊搜索 | `红富士` |
| `variety` | string | ❌ | 品种精确筛选 | `红富士` |
| `status` | string | ❌ | 状态 code：`DRAFT`/`PUBLISHED`/`MATCHED`/`CLOSED` | `PUBLISHED` |
| `farmerId` | number | ❌ | 按农户 ID 筛选 | `100001` |

```typescript
export interface SupplyPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  variety?: string;
  status?: string;
  farmerId?: number;
}
```

#### 响应体 `R<PageResult<SupplyInfoResponse>>`

```typescript
export interface SupplyInfoResponse {
  id: number;
  supplyNo: string;
  farmerId: number;
  orchardId: number | null;
  variety: string;
  quantity: number;
  priceExpected: number;
  harvestDate: string | null;        // yyyy-MM-dd
  validUntil: string | null;         // yyyy-MM-dd
  quality: EnumValue<string>;        // A/B/C
  location: string | null;
  description: string | null;
  status: EnumValue<string>;         // 见 SupplyInfoStatus
  batchCode: string | null;
  traceCode: string | null;
  createdAt: string;                 // yyyy-MM-dd HH:mm:ss
  updatedAt: string;
}
```

**JSON 示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 123456789,
        "supplyNo": "SUP20260411001",
        "farmerId": 100001,
        "orchardId": 200001,
        "variety": "红富士",
        "quantity": 5000.00,
        "priceExpected": 5.50,
        "harvestDate": "2026-04-10",
        "validUntil": "2026-05-10",
        "quality": { "code": "A", "desc": "优等品" },
        "location": "陕西省洛川县",
        "description": "今年新鲜苹果，品质优良",
        "status": { "code": "PUBLISHED", "desc": "已发布" },
        "batchCode": "CB202601010001",
        "traceCode": "TC20260301001",
        "createdAt": "2026-04-11 10:00:00",
        "updatedAt": "2026-04-11 10:00:00"
      }
    ],
    "total": 30,
    "current": 1,
    "size": 10
  }
}
```

**错误码**：`500001`（供应信息不存在）、`500900`（参数错误）

---

### 2.2 供货详情

**URL**：`GET /api/trade/supply/{id}`
**认证**：需要 JWT Bearer
**权限**：`trade:supply:read`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 供货信息 ID |

#### 响应体 `R<SupplyInfoResponse>`

**JSON 示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 123456789,
    "supplyNo": "SUP20260411001",
    "farmerId": 100001,
    "orchardId": 200001,
    "variety": "红富士",
    "quantity": 5000.00,
    "priceExpected": 5.50,
    "harvestDate": "2026-04-10",
    "validUntil": "2026-05-10",
    "quality": { "code": "A", "desc": "优等品" },
    "location": "陕西省洛川县",
    "description": "今年新鲜苹果，品质优良",
    "status": { "code": "PUBLISHED", "desc": "已发布" },
    "batchCode": "CB202601010001",
    "traceCode": "TC20260301001",
    "createdAt": "2026-04-11 10:00:00",
    "updatedAt": "2026-04-11 10:00:00"
  }
}
```

**错误码**：`500001`（供应信息不存在）

---

### 2.3 创建供货信息

**URL**：`POST /api/trade/supply`
**认证**：需要 JWT Bearer
**权限**：`trade:supply:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体

```typescript
export interface SupplyCreateRequest {
  farmerId: number;
  orchardId?: number;
  variety: string;
  quantity: number;
  priceExpected: number;
  harvestDate?: string;       // yyyy-MM-dd
  validUntil?: string;        // yyyy-MM-dd
  quality: string;            // A / B / C
  location?: string;
  description?: string;
  batchCode?: string;
  traceCode?: string;
}
```

**JSON 示例（请求）**：

```json
{
  "farmerId": 100001,
  "orchardId": 200001,
  "variety": "红富士",
  "quantity": 5000,
  "priceExpected": 5.50,
  "harvestDate": "2026-04-10",
  "validUntil": "2026-05-10",
  "quality": "A",
  "location": "陕西省洛川县",
  "description": "今年新鲜苹果，品质优良"
}
```

#### 响应体 `R<SupplyInfoResponse>`

**业务规则**：
- 初始状态自动设为 `DRAFT`
- `supplyNo` 由后端自动生成，格式：`SUP` + `yyyyMMdd` + 4位序列号

**错误码**：`500002`（农户不存在）、`500003`（果园不存在）、`500900`（参数缺失）

---

### 2.4 更新供货信息

**URL**：`PUT /api/trade/supply/{id}`
**认证**：需要 JWT Bearer
**权限**：`trade:supply:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 供货信息 ID |

#### 请求体

```typescript
export interface SupplyUpdateRequest {
  variety?: string;
  quantity?: number;
  priceExpected?: number;
  harvestDate?: string;
  validUntil?: string;
  quality?: string;
  location?: string;
  description?: string;
}
```

#### 响应体 `R<SupplyInfoResponse>`

**业务规则**：
- 只有 `DRAFT` 状态可以更新，`PUBLISHED`/`MATCHED`/`CLOSED` 状态禁止更新
- `supplyNo` 不允许修改

**错误码**：`500001`（供应信息不存在）、`500004`（非草稿状态禁止更新）

---

### 2.5 发布供货信息

**URL**：`PUT /api/trade/supply/{id}/publish`
**认证**：需要 JWT Bearer
**权限**：`trade:supply:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 供货信息 ID |

#### 响应体 `R<SupplyInfoResponse>`

**状态机**：`DRAFT` → `PUBLISHED`

**业务规则**：只有 `DRAFT` 状态可以发布

**JSON 示例**：

```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "id": 123456789,
    "status": { "code": "PUBLISHED", "desc": "已发布" }
  }
}
```

**错误码**：`500001`（供应信息不存在）、`500005`（非草稿状态禁止发布）

---

### 2.6 删除供货信息

**URL**：`DELETE /api/trade/supply/{id}`
**认证**：需要 JWT Bearer
**权限**：`trade:supply:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 供货信息 ID |

#### 响应体 `R<null>`

**JSON 示例**：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**错误码**：`500001`（供应信息不存在）

---

### 2.7 导出供货信息 CSV

**URL**：`GET /api/trade/supply/export`
**认证**：需要 JWT Bearer
**权限**：`trade:supply:read`
**变更历史**：2026-04-12 | init | 架构师

> **注意**：本接口直接返回 CSV 文件流，**不使用 `R<T>` 包装**，`Content-Type: text/csv;charset=UTF-8`。

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | ❌ | 关键词 |
| `variety` | string | ❌ | 品种 |
| `status` | string | ❌ | 状态 code |
| `farmerId` | number | ❌ | 农户 ID |

**CSV 列**：供应编号, 农户ID, 品种, 数量(kg), 期望价格(元/kg), 采收日期, 质量等级, 状态

**实现提示**：`SupplyInfoController.export()` → `supplyInfoService.exportSupplies()`

---

## 3. 采购需求 PurchaseNeedController（`/api/trade/need`）

### 3.1 采购需求列表（分页）

**URL**：`GET /api/trade/need/list`
**认证**：需要 JWT Bearer
**权限**：`trade:need:read`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10 | `10` |
| `keyword` | string | ❌ | 需求编号模糊搜索 | `NED2026` |
| `variety` | string | ❌ | 品种精确筛选 | `红富士` |
| `status` | string | ❌ | 状态 code | `PUBLISHED` |
| `buyerId` | number | ❌ | 买家 ID | `400001` |

```typescript
export interface PurchaseNeedPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  variety?: string;
  status?: string;
  buyerId?: number;
}
```

#### 响应体 `R<PageResult<PurchaseNeedResponse>>`

```typescript
export interface PurchaseNeedResponse {
  id: number;
  needNo: string;
  buyerId: number;
  variety: string;
  quantity: number;
  priceMax: number;
  requireDate: string | null;        // yyyy-MM-dd
  quality: EnumValue<string>;        // A/B/C
  deliveryAddr: string | null;
  description: string | null;
  status: EnumValue<string>;         // 见 PurchaseNeedStatus
  createdAt: string;
  updatedAt: string;
}
```

**JSON 示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 200001,
        "needNo": "NED202604110001",
        "buyerId": 400001,
        "variety": "红富士",
        "quantity": 2000.00,
        "priceMax": 6.00,
        "requireDate": "2026-04-30",
        "quality": { "code": "A", "desc": "优等品" },
        "deliveryAddr": "北京市朝阳区",
        "description": "需要优质红富士，用于超市销售",
        "status": { "code": "PUBLISHED", "desc": "已发布" },
        "createdAt": "2026-04-11 09:00:00",
        "updatedAt": "2026-04-11 09:00:00"
      }
    ],
    "total": 20,
    "current": 1,
    "size": 10
  }
}
```

**错误码**：`500900`（参数错误）

---

### 3.2 采购需求详情

**URL**：`GET /api/trade/need/{id}`
**认证**：需要 JWT Bearer
**权限**：`trade:need:read`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 采购需求 ID |

#### 响应体 `R<PurchaseNeedResponse>`

**错误码**：`500101`（采购需求不存在）

---

### 3.3 创建采购需求

**URL**：`POST /api/trade/need`
**认证**：需要 JWT Bearer
**权限**：`trade:need:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体

```typescript
export interface PurchaseNeedCreateRequest {
  buyerId: number;
  variety: string;
  quantity: number;
  priceMax: number;
  requireDate?: string;      // yyyy-MM-dd
  quality: string;           // A / B / C
  deliveryAddr?: string;
  description?: string;
}
```

**JSON 示例（请求）**：

```json
{
  "buyerId": 400001,
  "variety": "红富士",
  "quantity": 2000,
  "priceMax": 6.00,
  "requireDate": "2026-04-30",
  "quality": "A",
  "deliveryAddr": "北京市朝阳区",
  "description": "需要优质红富士"
}
```

#### 响应体 `R<PurchaseNeedResponse>`

**业务规则**：
- `needNo` 自动生成：`NED` + `yyyyMMdd` + 4位序列号
- 初始状态为 `DRAFT`

**错误码**：`500102`（买家不存在）、`500900`（参数缺失）

---

### 3.4 更新采购需求

**URL**：`PUT /api/trade/need/{id}`
**认证**：需要 JWT Bearer
**权限**：`trade:need:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 采购需求 ID |

#### 请求体

```typescript
export interface PurchaseNeedUpdateRequest {
  variety?: string;
  quantity?: number;
  priceMax?: number;
  requireDate?: string;
  quality?: string;
  deliveryAddr?: string;
  description?: string;
}
```

#### 响应体 `R<PurchaseNeedResponse>`

**业务规则**：`needNo` 不允许修改，更新时保留原编号

**错误码**：`500101`（采购需求不存在）

---

### 3.5 发布采购需求

**URL**：`PUT /api/trade/need/{id}/publish`
**认证**：需要 JWT Bearer
**权限**：`trade:need:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 采购需求 ID |

#### 响应体 `R<PurchaseNeedResponse>`

**状态机**：`DRAFT` → `PUBLISHED`

**业务规则**：只有 `DRAFT` 状态可以发布

**JSON 示例**：

```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "id": 200001,
    "needNo": "NED202604110001",
    "status": { "code": "PUBLISHED", "desc": "已发布" }
  }
}
```

**错误码**：`500101`（采购需求不存在）、`500103`（只有草稿状态可以发布）

---

### 3.6 删除采购需求

**URL**：`DELETE /api/trade/need/{id}`
**认证**：需要 JWT Bearer
**权限**：`trade:need:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 采购需求 ID |

#### 响应体 `R<null>`

**JSON 示例**：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**错误码**：`500101`（采购需求不存在）

---

### 3.7 导出采购需求 CSV

**URL**：`GET /api/trade/need/export`
**认证**：需要 JWT Bearer
**权限**：`trade:need:read`
**变更历史**：2026-04-12 | init | 架构师

> **注意**：直接返回 CSV 文件流，**不使用 `R<T>` 包装**，`Content-Type: text/csv;charset=UTF-8`。

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | ❌ | 关键词 |
| `variety` | string | ❌ | 品种 |
| `status` | string | ❌ | 状态 code |
| `buyerId` | number | ❌ | 买家 ID |

**CSV 列**：需求编号, 买家ID, 品种, 数量(kg), 最高价格(元/kg), 需求日期, 质量等级, 状态

**实现提示**：`PurchaseNeedController.export()` 内联实现 CSV 导出，使用 BOM(`\uFEFF`) 兼容 Excel

---

## 4. 交易订单 TradeOrderController（`/api/trade/order`）— 权威

> **权威 Controller**，操作表 `td_trade_order`。`TradeOrderMvpController`（`/api/trade/orders`）已废弃，见第 11.1 节。

### 订单完整状态机

```
                   ┌──────────────────┐
                   │      DRAFT       │──────────────────────┐
                   └──────────────────┘                      │
                            │ confirm                        │ cancel
                            ▼                                │
                   ┌──────────────────┐                      │
                   │   CONFIRMED      │──────────────────┐   │
                   └──────────────────┘                  │   │
                            │ deliver                    │   │
                            ▼                        cancel  │
                   ┌──────────────────┐                  │   │
                   │   DELIVERED      │                  │   │
                   └──────────────────┘                  │   │
                            │ complete                   │   │
                            ▼                            ▼   ▼
                   ┌──────────────────┐        ┌──────────────────┐
                   │   COMPLETED      │        │   CANCELLED      │
                   └──────────────────┘        └──────────────────┘

支付状态：PENDING ──→ PAID ──→ REFUNDED
```

### 4.1 订单列表（分页）

**URL**：`GET /api/trade/order/list`
**认证**：需要 JWT Bearer
**权限**：`trade:order:read`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10 | `10` |
| `keyword` | string | ❌ | 订单号模糊搜索 | `ORD2026` |
| `status` | string | ❌ | 订单状态 code | `CONFIRMED` |
| `farmerId` | number | ❌ | 农户 ID | `100001` |
| `buyerId` | number | ❌ | 买家 ID | `400001` |

```typescript
export interface TradeOrderPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;          // TradeOrderStatus code
  farmerId?: number;
  buyerId?: number;
}
```

#### 响应体 `R<PageResult<TradeOrderResponse>>`

```typescript
export interface TradeOrderResponse {
  id: number;
  orderNo: string;
  supplyId: number | null;
  needId: number | null;
  farmerId: number;
  buyerId: number;
  variety: string;
  quantity: number;
  unitPrice: number;
  totalAmount: number;
  tradeDate: string | null;          // yyyy-MM-dd
  deliveryDate: string | null;       // yyyy-MM-dd
  paymentStatus: EnumValue<string>;  // 见 PaymentStatus
  orderStatus: EnumValue<string>;    // 见 TradeOrderStatus
  contractFile: string | null;
  remark: string | null;
  createdAt: string;
  updatedAt: string;
}
```

**JSON 示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 300001,
        "orderNo": "ORD202604110001",
        "supplyId": 123456789,
        "needId": 200001,
        "farmerId": 100001,
        "buyerId": 400001,
        "variety": "红富士",
        "quantity": 1000.00,
        "unitPrice": 5.50,
        "totalAmount": 5500.00,
        "tradeDate": "2026-04-11",
        "deliveryDate": "2026-04-15",
        "paymentStatus": { "code": "PENDING", "desc": "待支付" },
        "orderStatus": { "code": "DRAFT", "desc": "草稿" },
        "contractFile": null,
        "remark": "",
        "createdAt": "2026-04-11 10:00:00",
        "updatedAt": "2026-04-11 10:00:00"
      }
    ],
    "total": 50,
    "current": 1,
    "size": 10
  }
}
```

**错误码**：`500900`（参数错误）

---

### 4.2 订单详情

**URL**：`GET /api/trade/order/{id}`
**认证**：需要 JWT Bearer
**权限**：`trade:order:read`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 订单 ID |

#### 响应体 `R<TradeOrderResponse>`

**错误码**：`500201`（订单不存在）

---

### 4.3 创建交易订单

**URL**：`POST /api/trade/order`
**认证**：需要 JWT Bearer
**权限**：`trade:order:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体

```typescript
export interface TradeOrderCreateRequest {
  supplyId?: number;
  needId?: number;
  farmerId: number;
  buyerId: number;
  variety: string;
  quantity: number;
  unitPrice: number;
  tradeDate?: string;       // yyyy-MM-dd
  deliveryDate?: string;    // yyyy-MM-dd
  remark?: string;
}
```

**JSON 示例（请求）**：

```json
{
  "supplyId": 123456789,
  "needId": 200001,
  "farmerId": 100001,
  "buyerId": 400001,
  "variety": "红富士",
  "quantity": 1000,
  "unitPrice": 5.50,
  "tradeDate": "2026-04-11",
  "deliveryDate": "2026-04-15",
  "remark": ""
}
```

#### 响应体 `R<TradeOrderResponse>`

**业务规则**：
- `orderNo` 自动生成：`ORD` + `yyyyMMdd` + 4位序列号
- `totalAmount` 自动计算：`quantity × unitPrice`
- 初始 `orderStatus = DRAFT`，`paymentStatus = PENDING`

**错误码**：`500202`（农户不存在）、`500203`（买家不存在）、`500204`（关联供应不存在）、`500900`（参数缺失）

---

### 4.4 确认订单

**URL**：`PUT /api/trade/order/{id}/confirm`
**认证**：需要 JWT Bearer
**权限**：`trade:order:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 订单 ID |

#### 响应体 `R<TradeOrderResponse>`

**状态机**：`DRAFT` → `CONFIRMED`

**JSON 示例**：

```json
{
  "code": 200,
  "message": "订单已确认",
  "data": {
    "id": 300001,
    "orderNo": "ORD202604110001",
    "orderStatus": { "code": "CONFIRMED", "desc": "已确认" }
  }
}
```

**错误码**：`500201`（订单不存在）、`500205`（非草稿状态不可确认）

---

### 4.5 标记发货

**URL**：`PUT /api/trade/order/{id}/deliver`
**认证**：需要 JWT Bearer
**权限**：`trade:order:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 订单 ID |

#### 响应体 `R<TradeOrderResponse>`

**状态机**：`CONFIRMED` → `DELIVERED`

**业务规则**：
- 发货前建议已关联质检单且质检通过（`QualityInspection.result = PASS`）
- 关联冷链运输任务（跨模块，见 `coldchain.md`）

**错误码**：`500201`（订单不存在）、`500206`（非已确认状态不可发货）

---

### 4.6 完成订单

**URL**：`PUT /api/trade/order/{id}/complete`
**认证**：需要 JWT Bearer
**权限**：`trade:order:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 订单 ID |

#### 响应体 `R<TradeOrderResponse>`

**状态机**：`DELIVERED` → `COMPLETED`

**错误码**：`500201`（订单不存在）、`500207`（非已发货状态不可完成）

---

### 4.7 取消订单

**URL**：`PUT /api/trade/order/{id}/cancel`
**认证**：需要 JWT Bearer
**权限**：`trade:order:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 订单 ID |

#### 响应体 `R<TradeOrderResponse>`

**状态机**：`DRAFT` 或 `CONFIRMED` → `CANCELLED`

**业务规则**：`DELIVERED`/`COMPLETED` 状态禁止取消

**错误码**：`500201`（订单不存在）、`500208`（当前状态不可取消）

---

### 4.8 导出订单 CSV

**URL**：`GET /api/trade/order/export`
**认证**：需要 JWT Bearer
**权限**：`trade:order:read`
**变更历史**：2026-04-12 | init | 架构师

> **注意**：直接返回 CSV 文件流，**不使用 `R<T>` 包装**，`Content-Type: text/csv;charset=UTF-8`。

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | ❌ | 订单号关键词 |
| `status` | string | ❌ | 订单状态 code |
| `farmerId` | number | ❌ | 农户 ID |
| `buyerId` | number | ❌ | 买家 ID |

**CSV 列**：订单号, 农户ID, 买家ID, 品种, 数量(kg), 单价(元/kg), 总金额(元), 交易日期, 交货日期, 订单状态, 支付状态

---

## 5. 订单撮合 MatchController（`/api/trade`）

> MatchController 聚合了撮合（match）、议价（negotiation）、聊天（chat）三个子资源，统一挂载在 `/api/trade` 下。

### 撮合状态机

```
TradeMatch.status（整型常量）:
  0 = CANDIDATE  候选（初始）
  1 = CONTACTED  已联系
  2 = NEGOTIATE  议价中
  3 = DEALT      成交
  4 = REJECTED   已拒绝

TradeNegotiation.status（整型常量）:
  0 = ONGOING    议价中
  1 = DEALT      已成交 → 自动创建 TradeOrder
  2 = CANCELED   已取消
```

### 5.1 计算撮合候选

**URL**：`POST /api/trade/match/compute`
**认证**：需要 JWT Bearer
**权限**：`trade:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `supplyId` | number | ✅ | 供应 ID | `123456789` |
| `topK` | number | ❌ | 返回候选数量，默认 10 | `10` |

#### 响应体 `R<TradeMatch[]>`

```typescript
export interface TradeMatchResponse {
  id: number;
  supplyId: number;
  demandId: number;
  matchScore: number;
  matchTime: string;         // yyyy-MM-dd HH:mm:ss
  status: number;            // 0=CANDIDATE 1=CONTACTED 2=NEGOTIATE 3=DEALT 4=REJECTED
  createdAt: string;
  updatedAt: string;
}
```

**JSON 示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 500001,
      "supplyId": 123456789,
      "demandId": 200001,
      "matchScore": 0.92,
      "matchTime": "2026-04-11 10:00:00",
      "status": 0,
      "createdAt": "2026-04-11 10:00:00",
      "updatedAt": "2026-04-11 10:00:00"
    }
  ]
}
```

**业务规则**：根据品种、价格区间、数量等维度计算撮合分数，结果写入 `td_trade_match` 表并缓存

**错误码**：`500301`（供应不存在）、`500302`（无可用采购需求）

---

### 5.2 查询撮合缓存

**URL**：`GET /api/trade/match/supply/{supplyId}/top`
**认证**：需要 JWT Bearer
**权限**：`trade:read`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path + Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `supplyId` | number | ✅ | 供应 ID（Path） |
| `topK` | number | ❌ | 返回数量，默认 10（Query） |

#### 响应体 `R<TradeMatch[]>`

**错误码**：`500301`（供应不存在）

---

### 5.3 发起议价

**URL**：`POST /api/trade/negotiation/start`
**认证**：需要 JWT Bearer
**权限**：`trade:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体

```typescript
export interface StartNegotiationRequest {
  matchId: number;
  supplyUserId: number;
  demandUserId: number;
  price: number;
  quantity: number;
}
```

**JSON 示例（请求）**：

```json
{
  "matchId": 500001,
  "supplyUserId": 100001,
  "demandUserId": 400001,
  "price": 5.20,
  "quantity": 800
}
```

#### 响应体 `R<TradeNegotiationResponse>`

```typescript
export interface TradeNegotiationResponse {
  id: number;
  matchId: number;
  supplyUserId: number;
  demandUserId: number;
  currentPrice: number;
  currentQuantity: number;
  lastOfferBy: number | null;
  status: number;            // 0=ONGOING 1=DEALT 2=CANCELED
  createdAt: string;
  updatedAt: string;
}
```

**JSON 示例**：

```json
{
  "code": 200,
  "message": "议价已发起",
  "data": {
    "id": 600001,
    "matchId": 500001,
    "supplyUserId": 100001,
    "demandUserId": 400001,
    "currentPrice": 5.20,
    "currentQuantity": 800.00,
    "lastOfferBy": 100001,
    "status": 0,
    "createdAt": "2026-04-11 10:05:00",
    "updatedAt": "2026-04-11 10:05:00"
  }
}
```

**错误码**：`500303`（撮合记录不存在）、`500304`（议价已存在）

---

### 5.4 报价还价

**URL**：`POST /api/trade/negotiation/{id}/offer`
**认证**：需要 JWT Bearer
**权限**：`trade:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 议价 ID |

#### 请求体

```typescript
export interface OfferRequest {
  byUserId: number;
  price: number;
  quantity: number;
}
```

**JSON 示例（请求）**：

```json
{
  "byUserId": 400001,
  "price": 5.30,
  "quantity": 900
}
```

#### 响应体 `R<TradeNegotiationResponse>`

**业务规则**：更新 `currentPrice`、`currentQuantity`、`lastOfferBy`，状态保持 `ONGOING`

**错误码**：`500305`（议价不存在）、`500306`（议价已结束不可报价）、`500307`（无权限参与此议价）

---

### 5.5 接受报价→自动建单

**URL**：`POST /api/trade/negotiation/{id}/accept`
**认证**：需要 JWT Bearer
**权限**：`trade:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 议价 ID |

#### 请求体

```typescript
export interface AcceptRequest {
  byUserId: number;
}
```

#### 响应体 `R<TradeOrderResponse>`

**业务规则**：
1. 议价状态更新为 `DEALT (1)`
2. 自动创建 `TradeOrder`，`orderStatus = DRAFT`，`totalAmount = currentPrice × currentQuantity`
3. 返回新创建的订单对象

**JSON 示例**：

```json
{
  "code": 200,
  "message": "报价已接受，订单已创建",
  "data": {
    "id": 300002,
    "orderNo": "ORD202604110002",
    "orderStatus": { "code": "DRAFT", "desc": "草稿" },
    "totalAmount": 4770.00
  }
}
```

**错误码**：`500305`（议价不存在）、`500308`（议价非进行中状态）

---

### 5.6 取消议价

**URL**：`POST /api/trade/negotiation/{id}/cancel`
**认证**：需要 JWT Bearer
**权限**：`trade:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 议价 ID |

#### 请求体

```typescript
export interface CancelNegotiationRequest {
  byUserId: number;
}
```

#### 响应体 `R<TradeNegotiationResponse>`

**状态机**：`ONGOING(0)` → `CANCELED(2)`

**错误码**：`500305`（议价不存在）、`500309`（议价已结束不可取消）

---

### 5.7 发送聊天消息

**URL**：`POST /api/trade/chat/send`
**认证**：需要 JWT Bearer
**权限**：`trade:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体

```typescript
export interface SendChatRequest {
  sessionId: string;
  fromUserId: number;
  toUserId: number;
  msgType: string;           // TEXT / PRICE_OFFER / IMAGE / CONTRACT
  content: string;
}
```

**JSON 示例（请求）**：

```json
{
  "sessionId": "neg-600001",
  "fromUserId": 100001,
  "toUserId": 400001,
  "msgType": "TEXT",
  "content": "这个价格可以商量一下吗？"
}
```

#### 响应体 `R<ChatMessageResponse>`

```typescript
export interface ChatMessageResponse {
  id: number;
  sessionId: string;
  fromUserId: number;
  toUserId: number;
  msgType: string;
  content: string;
  sendTime: string;          // yyyy-MM-dd HH:mm:ss
  readFlag: number;          // 0=未读 1=已读
  createdAt: string;
}
```

**JSON 示例**：

```json
{
  "code": 200,
  "message": "发送成功",
  "data": {
    "id": 700001,
    "sessionId": "neg-600001",
    "fromUserId": 100001,
    "toUserId": 400001,
    "msgType": "TEXT",
    "content": "这个价格可以商量一下吗？",
    "sendTime": "2026-04-11 10:10:00",
    "readFlag": 0,
    "createdAt": "2026-04-11 10:10:00"
  }
}
```

**错误码**：`500310`（会话不存在）、`500311`（消息类型不支持）

---

### 5.8 拉取历史消息

**URL**：`GET /api/trade/chat/{sessionId}/history`
**认证**：需要 JWT Bearer
**权限**：`trade:read`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path + Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `sessionId` | string | ✅ | 会话 ID（Path） | `neg-600001` |
| `limit` | number | ❌ | 返回消息数，默认 100（Query） | `100` |

#### 响应体 `R<ChatMessageResponse[]>`

**JSON 示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 700001,
      "sessionId": "neg-600001",
      "fromUserId": 100001,
      "toUserId": 400001,
      "msgType": "TEXT",
      "content": "这个价格可以商量一下吗？",
      "sendTime": "2026-04-11 10:10:00",
      "readFlag": 0,
      "createdAt": "2026-04-11 10:10:00"
    }
  ]
}
```

**错误码**：`500310`（会话不存在）

---

### 5.9 标记消息已读

**URL**：`POST /api/trade/chat/{sessionId}/read`
**认证**：需要 JWT Bearer
**权限**：`trade:read`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path + Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `sessionId` | string | ✅ | 会话 ID（Path） |
| `userId` | number | ✅ | 用户 ID（Query） |

#### 响应体 `R<number>`

**JSON 示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 5
}
```

> `data` 为标记已读的消息数量。

**错误码**：`500310`（会话不存在）

---

## 6. 质量检验 QualityInspectionController（`/api/trade/inspection`）

### 质检状态机

```
PENDING ──→ INSPECTED ──→ ACCEPTED
                      ──→ DISPUTED

result 字段（录入检验结果时自动判定）：
  PASS        全部指标通过
  FAIL        任一指标不通过
  CONDITIONAL 有条件通过（预留）
```

### 6.1 创建质检单

**URL**：`POST /api/trade/inspection`
**认证**：需要 JWT Bearer
**权限**：`trade:inspection:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体

```typescript
export interface QualityInspectionCreateRequest {
  orderId: number;
  supplyId: number;
  variety: string;
  grade: string;               // A / B / C
  inspectionDate?: string;     // yyyy-MM-dd，默认当天
}
```

**JSON 示例（请求）**：

```json
{
  "orderId": 300001,
  "supplyId": 123456789,
  "variety": "红富士",
  "grade": "A",
  "inspectionDate": "2026-04-11"
}
```

#### 响应体 `R<QualityInspectionResponse>`

```typescript
export interface QualityInspectionResponse {
  id: number;
  inspectionNo: string;
  orderId: number;
  supplyId: number;
  variety: string;
  grade: EnumValue<string>;          // A/B/C
  brixValue: number | null;
  firmnessValue: number | null;
  colorScore: number | null;
  defectRate: number | null;
  inspectorName: string | null;
  inspectionDate: string | null;     // yyyy-MM-dd
  reportUrl: string | null;
  result: EnumValue<string> | null;  // 见 QualityInspectionResult
  status: EnumValue<string>;         // 见 QualityInspectionStatus
  remark: string | null;
  createdAt: string;
  updatedAt: string;
}
```

**业务规则**：
- `inspectionNo` 自动生成：`QI` + `yyyyMMdd` + 4位序列号
- 初始状态为 `PENDING`，`result` 为 null

**错误码**：`500401`（关联订单不存在）、`500402`（关联供应不存在）、`500403`（该订单已有质检单）

---

### 6.2 质检列表（分页）

**URL**：`GET /api/trade/inspection`
**认证**：需要 JWT Bearer
**权限**：`trade:inspection:read`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10 | `10` |
| `orderId` | number | ❌ | 按订单 ID 筛选 | `300001` |
| `status` | string | ❌ | 状态 code | `INSPECTED` |
| `result` | string | ❌ | 结果 code：`PASS`/`FAIL`/`CONDITIONAL` | `PASS` |

#### 响应体 `R<PageResult<QualityInspectionResponse>>`

**JSON 示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 800001,
        "inspectionNo": "QI202604110001",
        "orderId": 300001,
        "supplyId": 123456789,
        "variety": "红富士",
        "grade": { "code": "A", "desc": "优等品" },
        "brixValue": 13.5,
        "firmnessValue": 7.2,
        "colorScore": 85.0,
        "defectRate": 2.0,
        "inspectorName": "质检员张三",
        "inspectionDate": "2026-04-11",
        "reportUrl": "/reports/qi-20260411-001.pdf",
        "result": { "code": "PASS", "desc": "通过" },
        "status": { "code": "ACCEPTED", "desc": "验收通过" },
        "remark": null,
        "createdAt": "2026-04-11 11:00:00",
        "updatedAt": "2026-04-11 11:30:00"
      }
    ],
    "total": 10,
    "current": 1,
    "size": 10
  }
}
```

---

### 6.3 质检详情

**URL**：`GET /api/trade/inspection/{id}`
**认证**：需要 JWT Bearer
**权限**：`trade:inspection:read`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 质检单 ID |

#### 响应体 `R<QualityInspectionResponse>`

**错误码**：`500404`（质检单不存在）

---

### 6.4 录入检验结果

**URL**：`PUT /api/trade/inspection/{id}/inspect`
**认证**：需要 JWT Bearer
**权限**：`trade:inspection:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 质检单 ID |

#### 请求体

```typescript
export interface QualityInspectionInspectRequest {
  brixValue: number;           // 糖度，阈值 ≥ 12
  firmnessValue: number;       // 硬度，阈值 ≥ 6
  colorScore: number;          // 色泽评分，阈值 ≥ 70
  defectRate: number;          // 缺陷率%，阈值 ≤ 5
  inspectorName: string;
  reportUrl?: string;
}
```

**JSON 示例（请求）**：

```json
{
  "brixValue": 13.5,
  "firmnessValue": 7.2,
  "colorScore": 85.0,
  "defectRate": 2.0,
  "inspectorName": "质检员张三",
  "reportUrl": "/reports/qi-20260411-001.pdf"
}
```

#### 响应体 `R<QualityInspectionResponse>`

**状态机**：`PENDING` → `INSPECTED`

**自动判定规则**：

| 指标 | 合格阈值 | 不合格判定 |
|------|---------|---------|
| 糖度 (brixValue) | ≥ 12 | 低于则 FAIL |
| 硬度 (firmnessValue) | ≥ 6 | 低于则 FAIL |
| 色泽评分 (colorScore) | ≥ 70 | 低于则 FAIL |
| 缺陷率 (defectRate) | ≤ 5% | 超过则 FAIL |

全部通过 → `result = PASS`；任一不通过 → `result = FAIL`

**错误码**：`500404`（质检单不存在）、`500405`（非待检状态不可录入结果）

---

### 6.5 验收通过

**URL**：`PUT /api/trade/inspection/{id}/accept`
**认证**：需要 JWT Bearer
**权限**：`trade:inspection:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 质检单 ID |

#### 响应体 `R<QualityInspectionResponse>`

**状态机**：`INSPECTED` → `ACCEPTED`

**业务规则**：只有 `INSPECTED` 状态可以验收；`FAIL` 结果的质检单验收通过后，需记录风险日志

**JSON 示例**：

```json
{
  "code": 200,
  "message": "验收通过",
  "data": {
    "id": 800001,
    "status": { "code": "ACCEPTED", "desc": "验收通过" }
  }
}
```

**错误码**：`500404`（质检单不存在）、`500406`（非已检验状态不可验收）

---

### 6.6 发起争议

**URL**：`PUT /api/trade/inspection/{id}/dispute`
**认证**：需要 JWT Bearer
**权限**：`trade:inspection:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 质检单 ID |

#### 请求体

```typescript
export interface DisputeRequest {
  reason: string;
}
```

**JSON 示例（请求）**：

```json
{
  "reason": "糖度检测结果与实际不符，要求复检"
}
```

#### 响应体 `R<QualityInspectionResponse>`

**状态机**：`INSPECTED` → `DISPUTED`

**业务规则**：争议原因记录到 `remark` 字段；质检异常可触发溯源异常追踪（见 `trace.md`）

**JSON 示例**：

```json
{
  "code": 200,
  "message": "争议已发起",
  "data": {
    "id": 800001,
    "status": { "code": "DISPUTED", "desc": "争议中" },
    "remark": "糖度检测结果与实际不符，要求复检"
  }
}
```

**错误码**：`500404`（质检单不存在）、`500407`（非已检验状态不可发起争议）

---

## 7. 交易工作流 M8WorkflowController（`/api/trade`）

> M8 外部接口（电子合同、第三方支付、税务开票）均为 Mock 实现，通过 `finance-gateway` 模块的 `@ConditionalOnProperty` 切换真实实现。
> 
> M8 工作流流程：**创建合同 → 签署合同 → 创建支付 → 支付回调 → 手动开票**

### 合同状态机

```
TradeContract.status（整型常量）:
  0 = DRAFT    草稿
  1 = PENDING  待签署（甲乙双方至少一方未签）
  2 = SIGNED   双方已签署
  3 = CANCELED 已取消
```

### 支付状态机

```
TradePayment.status（整型常量）:
  0 = PENDING  待支付
  1 = PAYING   支付中
  2 = SUCCESS  支付成功
  3 = FAILED   支付失败
  4 = REFUND   已退款
```

### 发票状态机

```
TradeInvoice.status（整型常量）:
  0 = APPLYING 开票中
  1 = SUCCESS  开票成功
  2 = FAILED   开票失败
```

### 7.1 创建合同

**URL**：`POST /api/trade/contract/create`
**认证**：需要 JWT Bearer
**权限**：`trade:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orderId` | number | ✅ | 订单 ID |
| `partyA` | number | ✅ | 甲方用户 ID（农户/供应方） |
| `partyB` | number | ✅ | 乙方用户 ID（买家/需求方） |

#### 响应体 `R<TradeContractResponse>`

```typescript
export interface TradeContractResponse {
  id: number;
  orderId: number;
  contractNo: string;
  templateId: string | null;
  partyA: number;
  partyB: number;
  partyASigned: number;              // 0=未签 1=已签
  partyASignTime: string | null;
  partyBSigned: number;              // 0=未签 1=已签
  partyBSignTime: string | null;
  contractFileUrl: string | null;
  status: number;                    // 0=DRAFT 1=PENDING 2=SIGNED 3=CANCELED
  createdAt: string;
  updatedAt: string;
}
```

**JSON 示例**：

```json
{
  "code": 200,
  "message": "合同已创建",
  "data": {
    "id": 900001,
    "orderId": 300001,
    "contractNo": "CON202604110001",
    "partyA": 100001,
    "partyB": 400001,
    "partyASigned": 0,
    "partyBSigned": 0,
    "contractFileUrl": null,
    "status": 0,
    "createdAt": "2026-04-11 12:00:00",
    "updatedAt": "2026-04-11 12:00:00"
  }
}
```

**错误码**：`500501`（订单不存在）、`500502`（该订单已有合同）

---

### 7.2 签署合同

**URL**：`POST /api/trade/contract/{orderId}/sign`
**认证**：需要 JWT Bearer
**权限**：`trade:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path + Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orderId` | number | ✅ | 订单 ID（Path） |
| `signerUid` | number | ✅ | 签署人用户 ID（Query） |

#### 响应体 `R<TradeContractResponse>`

**业务规则**：
- 判断 `signerUid` 是甲方还是乙方，更新对应签署标志和时间
- 双方均已签署时，合同状态自动变为 `SIGNED(2)`

**错误码**：`500501`（订单不存在）、`500503`（合同不存在）、`500504`（签署人不在合同当事方）

---

### 7.3 创建支付

**URL**：`POST /api/trade/payment/create`
**认证**：需要 JWT Bearer
**权限**：`trade:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `orderId` | number | ✅ | 订单 ID | `300001` |
| `channel` | string | ❌ | 支付渠道，默认 `WECHAT`：`WECHAT`/`ALIPAY`/`BANK` | `WECHAT` |

#### 响应体 `R<TradePaymentResponse>`

```typescript
export interface TradePaymentResponse {
  id: number;
  orderId: number;
  paymentNo: string;
  channel: string;
  amount: number;
  thirdTradeNo: string | null;
  status: number;                    // 0=PENDING 1=PAYING 2=SUCCESS 3=FAILED 4=REFUND
  payTime: string | null;
  callbackTime: string | null;
  createdAt: string;
  updatedAt: string;
}
```

**JSON 示例**：

```json
{
  "code": 200,
  "message": "支付单已创建",
  "data": {
    "id": 950001,
    "orderId": 300001,
    "paymentNo": "PAY202604110001",
    "channel": "WECHAT",
    "amount": 5500.00,
    "thirdTradeNo": null,
    "status": 0,
    "payTime": null,
    "callbackTime": null,
    "createdAt": "2026-04-11 13:00:00",
    "updatedAt": "2026-04-11 13:00:00"
  }
}
```

**错误码**：`500501`（订单不存在）、`500505`（支付渠道不支持）、`500506`（订单已有待处理支付单）

---

### 7.4 支付回调

**URL**：`POST /api/trade/payment/callback/wechat`
**认证**：不需要（第三方回调）
**变更历史**：2026-04-12 | init | 架构师

> **注意**：Mock 模式下可手动调用此接口模拟支付成功；真实模式需验证微信签名。

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `paymentNo` | string | ✅ | 支付流水号 |

#### 响应体 `R<TradePaymentResponse>`

**业务规则**：
- 查找支付单，标记 `status = SUCCESS(2)`，记录 `payTime`
- 同步更新关联订单 `paymentStatus = PAID`

**JSON 示例**：

```json
{
  "code": 200,
  "message": "支付已确认",
  "data": {
    "id": 950001,
    "status": 2,
    "payTime": "2026-04-11 13:05:00"
  }
}
```

**错误码**：`500507`（支付单不存在）、`500508`（支付单状态异常）

---

### 7.5 手动开票

**URL**：`POST /api/trade/invoice/issue`
**认证**：需要 JWT Bearer
**权限**：`trade:write`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orderId` | number | ✅ | 订单 ID |
| `taxPayer` | string | ✅ | 开票方名称 |
| `taxNo` | string | ✅ | 纳税人识别号 |

#### 响应体 `R<TradeInvoiceResponse>`

```typescript
export interface TradeInvoiceResponse {
  id: number;
  orderId: number;
  paymentId: number | null;
  invoiceNo: string;
  invoiceCode: string | null;
  taxPayer: string;
  taxNo: string;
  amount: number;
  taxAmount: number | null;
  pdfUrl: string | null;
  issueTime: string | null;
  status: number;            // 0=APPLYING 1=SUCCESS 2=FAILED
  createdAt: string;
  updatedAt: string;
}
```

**JSON 示例**：

```json
{
  "code": 200,
  "message": "开票申请已提交",
  "data": {
    "id": 980001,
    "orderId": 300001,
    "invoiceNo": "INV202604110001",
    "taxPayer": "陕西苹果合作社有限公司",
    "taxNo": "91610000XXXXXXXX",
    "amount": 5500.00,
    "status": 0,
    "createdAt": "2026-04-11 14:00:00",
    "updatedAt": "2026-04-11 14:00:00"
  }
}
```

**错误码**：`500501`（订单不存在）、`500509`（订单未支付不可开票）、`500510`（已存在开票记录）

---

## 8. 交易统计 TradeStatisticsController（`/api/trade/statistics`）

### 8.1 交易汇总统计

**URL**：`GET /api/trade/statistics/summary`
**认证**：需要 JWT Bearer
**权限**：`trade:statistics:read`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `R<TradeSummaryResponse>`

```typescript
export interface TradeSummaryResponse {
  totalOrders: number;
  totalVolume: number;                // 总成交量 kg
  totalAmount: number;                // 总成交额 元
  avgUnitPrice: number;               // 平均单价 元/kg
  ordersByStatus: {                   // 各状态订单数
    DRAFT: number;
    CONFIRMED: number;
    DELIVERED: number;
    COMPLETED: number;
    CANCELLED: number;
  };
}
```

**JSON 示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalOrders": 150,
    "totalVolume": 75000.00,
    "totalAmount": 412500.00,
    "avgUnitPrice": 5.50,
    "ordersByStatus": {
      "DRAFT": 10,
      "CONFIRMED": 20,
      "DELIVERED": 30,
      "COMPLETED": 80,
      "CANCELLED": 10
    }
  }
}
```

**实现提示**：`TradeStatisticsController.summary()` → `TradeStatisticsService.getSummary()`，返回 `TradeStatisticsVO`，需迁移为 `TradeSummaryResponse` DTO（见 11.4 节）

---

### 8.2 品种维度统计

**URL**：`GET /api/trade/statistics/variety`
**认证**：需要 JWT Bearer
**权限**：`trade:statistics:read`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `R<VarietyStatsResponse[]>`

```typescript
export interface VarietyStatsResponse {
  variety: string;
  volume: number;            // 总成交量 kg
  amount: number;            // 总成交额 元
  avgPrice: number;          // 平均单价 元/kg
}
```

**JSON 示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "variety": "红富士",
      "volume": 50000.00,
      "amount": 275000.00,
      "avgPrice": 5.50
    },
    {
      "variety": "嘎啦",
      "volume": 25000.00,
      "amount": 137500.00,
      "avgPrice": 5.50
    }
  ]
}
```

---

### 8.3 月度趋势统计

**URL**：`GET /api/trade/statistics/trend`
**认证**：需要 JWT Bearer
**权限**：`trade:statistics:read`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `months` | number | ❌ | 统计近 N 个月，默认 6 | `6` |

#### 响应体 `R<MonthlyStatsResponse[]>`

```typescript
export interface MonthlyStatsResponse {
  month: string;             // 格式 yyyy-MM，如 2026-03
  volume: number;            // 成交量 kg
  amount: number;            // 成交额 元
  orderCount: number;        // 订单数
}
```

**JSON 示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "month": "2026-04",
      "volume": 15000.00,
      "amount": 82500.00,
      "orderCount": 30
    },
    {
      "month": "2026-03",
      "volume": 12000.00,
      "amount": 66000.00,
      "orderCount": 24
    }
  ]
}
```

---

### 8.4 品种价格指数

**URL**：`GET /api/trade/statistics/price-index`
**认证**：需要 JWT Bearer
**权限**：`trade:statistics:read`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `variety` | string | ✅ | 品种名称 | `红富士` |
| `days` | number | ❌ | 统计近 N 天，默认 30 | `30` |

#### 响应体 `R<MonthlyStatsResponse[]>`

> 复用 `MonthlyStatsResponse` 结构，`month` 字段此时表示日期（`yyyy-MM-dd`）。

**实现提示**：`TradeStatisticsController.priceIndex()` 返回 `List<TradeStatisticsVO.MonthlyStats>`，需迁移为 `MonthlyStatsResponse`

---

## 9. 错误码清单

> 错误码段：`500000 ~ 599999`（CONVENTIONS.md:137 权威）

### 9.1 供应信息 SupplyInfo（500001–500099）

| 错误码 | 名称 | 说明 | HTTP 状态码 |
|--------|------|------|------------|
| `500001` | `SUPPLY_NOT_FOUND` | 供应信息不存在 | 404 |
| `500002` | `SUPPLY_FARMER_NOT_FOUND` | 关联农户不存在 | 400 |
| `500003` | `SUPPLY_ORCHARD_NOT_FOUND` | 关联果园不存在 | 400 |
| `500004` | `SUPPLY_NOT_DRAFT` | 非草稿状态禁止更新 | 400 |
| `500005` | `SUPPLY_PUBLISH_INVALID_STATUS` | 非草稿状态禁止发布 | 400 |
| `500006` | `SUPPLY_NO_DUPLICATE` | 供应编号重复 | 400 |

### 9.2 采购需求 PurchaseNeed（500100–500199）

| 错误码 | 名称 | 说明 | HTTP 状态码 |
|--------|------|------|------------|
| `500101` | `NEED_NOT_FOUND` | 采购需求不存在 | 404 |
| `500102` | `NEED_BUYER_NOT_FOUND` | 关联买家不存在 | 400 |
| `500103` | `NEED_PUBLISH_INVALID_STATUS` | 只有草稿状态可以发布 | 400 |
| `500104` | `NEED_NO_DUPLICATE` | 需求编号重复 | 400 |

### 9.3 交易订单 TradeOrder（500200–500299）

| 错误码 | 名称 | 说明 | HTTP 状态码 |
|--------|------|------|------------|
| `500201` | `ORDER_NOT_FOUND` | 订单不存在 | 404 |
| `500202` | `ORDER_FARMER_NOT_FOUND` | 关联农户不存在 | 400 |
| `500203` | `ORDER_BUYER_NOT_FOUND` | 关联买家不存在 | 400 |
| `500204` | `ORDER_SUPPLY_NOT_FOUND` | 关联供应信息不存在 | 400 |
| `500205` | `ORDER_CONFIRM_INVALID_STATUS` | 非草稿状态不可确认 | 400 |
| `500206` | `ORDER_DELIVER_INVALID_STATUS` | 非已确认状态不可发货 | 400 |
| `500207` | `ORDER_COMPLETE_INVALID_STATUS` | 非已发货状态不可完成 | 400 |
| `500208` | `ORDER_CANCEL_INVALID_STATUS` | 当前状态不可取消 | 400 |
| `500209` | `ORDER_NO_DUPLICATE` | 订单号重复 | 400 |

### 9.4 撮合议价 Match（500300–500399）

| 错误码 | 名称 | 说明 | HTTP 状态码 |
|--------|------|------|------------|
| `500301` | `MATCH_SUPPLY_NOT_FOUND` | 撮合对应供应不存在 | 404 |
| `500302` | `MATCH_NO_DEMAND` | 无可用采购需求 | 400 |
| `500303` | `MATCH_NOT_FOUND` | 撮合记录不存在 | 404 |
| `500304` | `NEGOTIATION_ALREADY_EXISTS` | 该撮合下议价已存在 | 400 |
| `500305` | `NEGOTIATION_NOT_FOUND` | 议价记录不存在 | 404 |
| `500306` | `NEGOTIATION_OFFER_INVALID_STATUS` | 议价已结束不可报价 | 400 |
| `500307` | `NEGOTIATION_NO_PERMISSION` | 无权限参与此议价 | 403 |
| `500308` | `NEGOTIATION_ACCEPT_INVALID_STATUS` | 议价非进行中状态 | 400 |
| `500309` | `NEGOTIATION_CANCEL_INVALID_STATUS` | 议价已结束不可取消 | 400 |
| `500310` | `CHAT_SESSION_NOT_FOUND` | 聊天会话不存在 | 404 |
| `500311` | `CHAT_MSG_TYPE_UNSUPPORTED` | 消息类型不支持 | 400 |

### 9.5 质量检验 QualityInspection（500400–500499）

| 错误码 | 名称 | 说明 | HTTP 状态码 |
|--------|------|------|------------|
| `500401` | `INSPECTION_ORDER_NOT_FOUND` | 质检关联订单不存在 | 400 |
| `500402` | `INSPECTION_SUPPLY_NOT_FOUND` | 质检关联供应不存在 | 400 |
| `500403` | `INSPECTION_ALREADY_EXISTS` | 该订单已有质检单 | 400 |
| `500404` | `INSPECTION_NOT_FOUND` | 质检单不存在 | 404 |
| `500405` | `INSPECTION_INSPECT_INVALID_STATUS` | 非待检状态不可录入结果 | 400 |
| `500406` | `INSPECTION_ACCEPT_INVALID_STATUS` | 非已检验状态不可验收 | 400 |
| `500407` | `INSPECTION_DISPUTE_INVALID_STATUS` | 非已检验状态不可发起争议 | 400 |

### 9.6 M8 工作流（500500–500599）

| 错误码 | 名称 | 说明 | HTTP 状态码 |
|--------|------|------|------------|
| `500501` | `WORKFLOW_ORDER_NOT_FOUND` | 工作流关联订单不存在 | 404 |
| `500502` | `CONTRACT_ALREADY_EXISTS` | 该订单已有合同 | 400 |
| `500503` | `CONTRACT_NOT_FOUND` | 合同不存在 | 404 |
| `500504` | `CONTRACT_SIGNER_NOT_PARTY` | 签署人不在合同当事方 | 403 |
| `500505` | `PAYMENT_CHANNEL_UNSUPPORTED` | 支付渠道不支持 | 400 |
| `500506` | `PAYMENT_PENDING_EXISTS` | 已有待处理支付单 | 400 |
| `500507` | `PAYMENT_NOT_FOUND` | 支付单不存在 | 404 |
| `500508` | `PAYMENT_CALLBACK_STATUS_ERROR` | 支付单状态异常 | 400 |
| `500509` | `INVOICE_ORDER_UNPAID` | 订单未支付不可开票 | 400 |
| `500510` | `INVOICE_ALREADY_EXISTS` | 已存在开票记录 | 400 |

### 9.7 通用参数/权限（500900–500999）

| 错误码 | 名称 | 说明 | HTTP 状态码 |
|--------|------|------|------------|
| `500900` | `TRADE_PARAM_INVALID` | 参数缺失或格式错误 | 400 |
| `500901` | `TRADE_FORBIDDEN` | 无权限操作 | 403 |
| `500902` | `TRADE_EXPORT_FAILED` | 导出失败 | 500 |

---

## 10. 枚举集中声明

### 10.1 SupplyInfoStatus — 供应信息状态

| code | desc | 说明 |
|------|------|------|
| `DRAFT` | 草稿 | 初始状态，未发布 |
| `PUBLISHED` | 已发布 | 对外公开，可被撮合 |
| `MATCHED` | 已撮合 | 有议价/订单关联 |
| `CLOSED` | 已关闭 | 完成交易或手动关闭 |

```typescript
export type SupplyInfoStatus = 'DRAFT' | 'PUBLISHED' | 'MATCHED' | 'CLOSED';
```

### 10.2 PurchaseNeedStatus — 采购需求状态

| code | desc | 说明 |
|------|------|------|
| `DRAFT` | 草稿 | 初始状态，未发布 |
| `PUBLISHED` | 已发布 | 对外公开，可被撮合 |
| `MATCHED` | 已撮合 | 有议价/订单关联 |
| `CLOSED` | 已关闭 | 完成交易或手动关闭 |

```typescript
export type PurchaseNeedStatus = 'DRAFT' | 'PUBLISHED' | 'MATCHED' | 'CLOSED';
```

### 10.3 TradeOrderStatus — 交易订单状态（正式版）

> 仅 `TradeOrderController`（`td_trade_order` 表）使用。

| code | desc | 说明 |
|------|------|------|
| `DRAFT` | 草稿 | 初始状态 |
| `CONFIRMED` | 已确认 | 双方确认 |
| `DELIVERED` | 已发货 | 货物已发出 |
| `COMPLETED` | 已完成 | 买家确认收货 |
| `CANCELLED` | 已取消 | 取消 |

```typescript
export type TradeOrderStatus = 'DRAFT' | 'CONFIRMED' | 'DELIVERED' | 'COMPLETED' | 'CANCELLED';
```

### 10.4 PaymentStatus — 支付状态

| code | desc | 说明 |
|------|------|------|
| `PENDING` | 待支付 | 初始支付状态 |
| `PAID` | 已支付 | 回调确认后更新 |
| `REFUNDED` | 已退款 | 退款完成 |

```typescript
export type PaymentStatus = 'PENDING' | 'PAID' | 'REFUNDED';
```

### 10.5 QualityInspectionResult — 质检结果

| code | desc | 说明 |
|------|------|------|
| `PASS` | 通过 | 全部指标合格 |
| `FAIL` | 不通过 | 任一指标不合格 |
| `CONDITIONAL` | 有条件通过 | 预留，需人工确认 |

```typescript
export type QualityInspectionResult = 'PASS' | 'FAIL' | 'CONDITIONAL';
```

### 10.6 QualityInspectionStatus — 质检状态

| code | desc | 说明 |
|------|------|------|
| `PENDING` | 待检验 | 质检单创建后初始状态 |
| `INSPECTED` | 已检验 | 录入检验结果后 |
| `ACCEPTED` | 验收通过 | 买家验收 |
| `DISPUTED` | 争议中 | 买家发起争议 |

```typescript
export type QualityInspectionStatus = 'PENDING' | 'INSPECTED' | 'ACCEPTED' | 'DISPUTED';
```

### 10.7 QualityGrade — 质量等级

| code | desc | 说明 |
|------|------|------|
| `A` | 优等品 | 最高等级 |
| `B` | 一等品 | 中等质量 |
| `C` | 二等品 | 较低质量 |

### 10.8 TradeMatchStatus — 撮合状态（整型）

| code | desc | 说明 |
|------|------|------|
| `0` | 候选 CANDIDATE | 系统计算出的候选 |
| `1` | 已联系 CONTACTED | 已发起议价联系 |
| `2` | 议价中 NEGOTIATE | 议价进行中 |
| `3` | 已成交 DEALT | 议价成功，订单创建 |
| `4` | 已拒绝 REJECTED | 拒绝撮合 |

### 10.9 TradeNegotiationStatus — 议价状态（整型）

| code | desc | 说明 |
|------|------|------|
| `0` | 议价中 ONGOING | 初始状态 |
| `1` | 已成交 DEALT | 接受报价，自动建单 |
| `2` | 已取消 CANCELED | 取消议价 |

### 10.10 ContractStatus — 合同状态（整型）

| code | desc | 说明 |
|------|------|------|
| `0` | 草稿 DRAFT | 创建后初始状态 |
| `1` | 待签署 PENDING | 至少一方未签 |
| `2` | 已签署 SIGNED | 双方均已签 |
| `3` | 已取消 CANCELED | 合同取消 |

### 10.11 PaymentInternalStatus — 支付单内部状态（整型）

| code | desc | 说明 |
|------|------|------|
| `0` | 待支付 PENDING | 创建后初始 |
| `1` | 支付中 PAYING | 调起支付后 |
| `2` | 支付成功 SUCCESS | 回调确认 |
| `3` | 支付失败 FAILED | 超时或失败 |
| `4` | 已退款 REFUND | 退款完成 |

### 10.12 InvoiceStatus — 发票状态（整型）

| code | desc | 说明 |
|------|------|------|
| `0` | 开票中 APPLYING | 申请开票后 |
| `1` | 开票成功 SUCCESS | 税局回调成功 |
| `2` | 开票失败 FAILED | 开票失败 |

### 10.13 ChatMessageType — 消息类型

| code | desc | 说明 |
|------|------|------|
| `TEXT` | 文本 | 普通文字消息 |
| `PRICE_OFFER` | 报价 | 价格报盘消息 |
| `IMAGE` | 图片 | 图片消息 |
| `CONTRACT` | 合同 | 合同文件消息 |

### 10.14 PaymentChannel — 支付渠道

| code | desc |
|------|------|
| `WECHAT` | 微信支付 |
| `ALIPAY` | 支付宝 |
| `BANK` | 银行转账 |

---

## 11. 实现注记

### 11.1 TradeOrderController 双 Controller 冲突解决（最重要）

#### 两个 Controller 对比

| 维度 | TradeOrderController（权威） | TradeOrderMvpController（废弃） |
|------|------------------------------|--------------------------------|
| 路径前缀 | `/api/trade/order` | `/api/trade/orders` |
| 操作表 | `td_trade_order` | `trade_order`（无前缀，MVP 临时表） |
| 实体类 | `TradeOrder` | `TradeOrderMvp` |
| 服务类 | `TradeOrderService` | `TradeOrderMvpService` |
| orderStatus 值 | `DRAFT/CONFIRMED/DELIVERED/COMPLETED/CANCELLED` | `PENDING/CONFIRMED/SHIPPED/COMPLETED/CANCELLED` |
| paymentStatus 值 | `PENDING/PAID/REFUNDED` | `UNPAID/PAID` |
| 关联字段 | `supplyId`, `needId`, `farmerId`, `buyerId` | 无 `supplyId`/`needId`，有 `buyerName`, `buyerPhone`, `orchardName` |
| 状态迁移接口 | 各状态独立端点（confirm/deliver/complete/cancel） | 单一 `PUT /{id}/status` 传 status 参数 |

#### 端点冲突逐条说明

| MVP 端点（禁用） | 正式端点（保留） | 差异 |
|-----------------|----------------|------|
| `GET /api/trade/orders` | `GET /api/trade/order/list` | MVP 无分页，正式有完整分页 |
| `GET /api/trade/orders/{id}` | `GET /api/trade/order/{id}` | 操作不同表 |
| `POST /api/trade/orders` | `POST /api/trade/order` | 字段集合不同 |
| `PUT /api/trade/orders/{id}` | 无对应（正式版不支持整体更新，仅状态流转） | MVP 设计缺陷 |
| `PUT /api/trade/orders/{id}/status` | 拆分为 confirm/deliver/complete/cancel | MVP 通用 status 参数易误操作 |
| `DELETE /api/trade/orders/{id}` | 无对应（正式版不支持删除，仅取消） | MVP 硬删除，正式版软状态流转 |
| `GET /api/trade/orders/export` | `GET /api/trade/order/export` | 操作不同表 |

#### 处置方案

1. **立即**：在 `TradeOrderMvpController` 类上添加 `@Deprecated` 注解和注释说明
2. **短期**：前端全部切换到 `/api/trade/order` 路径
3. **下一个 Sprint**：待前端验证完毕后，删除 `TradeOrderMvpController`、`TradeOrderMvpService`、`TradeOrderMvp` 实体，以及 `trade_order` 表

---

### 11.2 String→EnumValue 迁移清单

当前后端 Entity 中枚举字段均为 `String` 类型存储，响应需序列化为 `EnumValue<T>` 对象。需在 Service 层或序列化层处理：

| 实体 | 字段 | 当前存储值 | 目标 EnumValue |
|------|------|---------|---------------|
| `SupplyInfo` | `status` | String | `SupplyInfoStatus` |
| `SupplyInfo` | `quality` | String | `QualityGrade` |
| `PurchaseNeed` | `status` | String | `PurchaseNeedStatus` |
| `PurchaseNeed` | `quality` | String | `QualityGrade` |
| `TradeOrder` | `orderStatus` | String | `TradeOrderStatus` |
| `TradeOrder` | `paymentStatus` | String | `PaymentStatus` |
| `QualityInspection` | `status` | String | `QualityInspectionStatus` |
| `QualityInspection` | `result` | String | `QualityInspectionResult` |
| `QualityInspection` | `grade` | String | `QualityGrade` |

> **注意**：`TradeMatch.status`、`TradeNegotiation.status`、`TradeContract.status`、`TradePayment.status`、`TradeInvoice.status` 均为 `Integer` 类型，使用整型常量，响应中直接返回 `number`，不需要 EnumValue 包装。

---

### 11.3 Entity→Request/Response DTO 重命名

当前 Controller 直接使用 Entity 作为请求/响应类型，需按以下规则重命名：

| 当前类型 | 目标 Request | 目标 Response |
|---------|------------|--------------|
| `SupplyInfo`（请求） | `SupplyCreateRequest` / `SupplyUpdateRequest` | `SupplyInfoResponse` |
| `PurchaseNeed`（请求） | `PurchaseNeedCreateRequest` / `PurchaseNeedUpdateRequest` | `PurchaseNeedResponse` |
| `TradeOrder`（请求） | `TradeOrderCreateRequest` | `TradeOrderResponse` |
| `QualityInspection`（请求） | `QualityInspectionCreateRequest` / `QualityInspectionInspectRequest` | `QualityInspectionResponse` |
| `TradeMatch` | 无（查询结果） | `TradeMatchResponse` |
| `TradeNegotiation` | —（使用专用 Request） | `TradeNegotiationResponse` |
| `ChatMessage` | `SendChatRequest` | `ChatMessageResponse` |
| `TradeContract` | —（Query Param） | `TradeContractResponse` |
| `TradePayment` | —（Query Param） | `TradePaymentResponse` |
| `TradeInvoice` | —（Query Param） | `TradeInvoiceResponse` |

> `MatchController` 已有 `M7Requests.*`（`StartNegotiationRequest`/`OfferRequest`/`AcceptRequest`），符合规范，可直接保留。

---

### 11.4 统计 Map→DTO 迁移

`TradeStatisticsController` 当前使用 `TradeStatisticsVO` 内部类 `VarietyStats` 和 `MonthlyStats`。需：

1. 将 `TradeStatisticsVO` 重命名为 `TradeSummaryResponse`
2. 将 `VarietyStats` 重命名为 `VarietyStatsResponse`
3. 将 `MonthlyStats` 重命名为 `MonthlyStatsResponse`
4. `ordersByStatus` 当前为 `Map<String, Long>`，迁移为结构化字段（见 8.1 节定义）

---

### 11.5 分页参数迁移

当前 Controller 使用散列参数（`@RequestParam int page, @RequestParam int size`），迁移目标为 `PageParam`：

```java
// 迁移前（SupplyInfoController 当前实现）
public R<PageResult<SupplyInfo>> list(@RequestParam(defaultValue="1") int page,
                                       @RequestParam(defaultValue="10") int size, ...)

// 迁移后（契约目标）
public R<PageResult<SupplyInfoResponse>> list(@ModelAttribute SupplyPageRequest req)
```

影响的 Controller：`SupplyInfoController`、`PurchaseNeedController`、`TradeOrderController`、`QualityInspectionController`

---

### 11.6 关联契约跨模块依赖

| 业务场景 | 本模块触发 | 目标模块 | 关联字段 |
|---------|----------|---------|---------|
| 订单发货→创建运输任务 | `TradeOrderController.deliver()` | `coldchain.md` | `orderId` |
| 订单完成→出库核销 | `TradeOrderController.complete()` | `warehouse.md` | `orderId` |
| 质检异常→溯源记录 | `QualityInspectionController.dispute()` | `trace.md` | `traceCode` (via `SupplyInfo`) |
| 订单支付→金融流水 | `M8WorkflowController.wechatCallback()` | `finance.md` | `orderId`, `paymentNo` |
| 用户身份校验 | 所有需鉴权接口 | `user.md` | `farmerId`, `buyerId` |

---

### 11.7 PurchaseNeedController 内联实现问题

当前 `PurchaseNeedController` 直接注入 `PurchaseNeedMapper`，绕过 Service 层，业务逻辑（分页查询、状态机、导出）写在 Controller 中。迁移目标：

1. 新建 `PurchaseNeedService` 接口和实现
2. 将 Controller 中的业务逻辑下移到 Service 层
3. Controller 仅负责参数接收和响应包装

---

### 11.8 TradeOrderMvpController 中的 `SHIPPED` 状态说明

`TradeOrderMvpController` 的状态值 `SHIPPED` 对应正式版的 `DELIVERED`。两者语义相同（货物已发出），但正式版选用 `DELIVERED` 以更准确描述"货物已送达（待确认）"的业务含义。迁移时注意前端状态映射。

---

## 12. 质量 checklist

- [x] 端点全覆盖：46 端点（SupplyInfo 7 + PurchaseNeed 7 + TradeOrder 8 + Match/Negotiation/Chat 9 + QualityInspection 6 + M8Workflow 5 + Statistics 4）
- [x] ≥25 个错误码，全在 500000-599999：共定义 **29 个**错误码
- [x] 所有枚举声明完整：14 个枚举/状态集合（第 10 章）
- [x] JSON 示例全用 `message` 字段（禁 `msg`）
- [x] 分页使用 PageParam + PageResult<T>：第 2.1、3.1、4.1、6.2 节均已定义
- [x] CSV 导出（供货/采购需求/订单）明确标注非 `R<T>` 包装
- [x] TradeOrderController 双 Controller 权威已锁定（第 11.1 节），冲突逐条列出
- [x] 订单状态机完整（第 4 章状态机图）
- [x] 撮合状态机完整（第 5 章状态机定义）
- [x] 议价状态机完整（第 5.3-5.6 节）
- [x] 质检状态机完整（第 6 章状态机图）
- [x] M8 工作流流程已入文档（第 7 章，5 步流程）
- [x] 跨模块依赖（5 个模块）已列出（第 11.6 节）
- [x] 实现注记覆盖：双 Controller 冲突 + Enum 迁移 + DTO 重命名 + 统计 DTO + 分页迁移 + 跨模块依赖 + 内联实现问题 + 状态映射说明

---

## 关联契约

- [`warehouse.md`](./warehouse.md) — 订单发货触发出库
- [`coldchain.md`](./coldchain.md) — 订单发货触发冷链运输任务
- [`finance.md`](./finance.md) — 订单支付流水、融资
- [`trace.md`](./trace.md) — 质检异常 → AnomalyTrace
- [`user.md`](./user.md) — 买方/卖方/农户身份校验
