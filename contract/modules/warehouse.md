# M5 仓储 (Warehouse) 模块契约

> 模块：`apple-module-warehouse`
> URL 前缀：`/api/warehouse/warehouses`、`/api/warehouse/receipts`、`/api/warehouse/records`、`/api/warehouse/statistics`
> 错误码段：`600000 ~ 699999`（CONVENTIONS.md:138 权威）
> 最近更新：2026-04-12
> 权威源：本文件
> 后端实现：`apple-module-warehouse`
> 关联契约：`finance.md`（仓单质押）/ `planting.md`（harvest_batch）/ `coldchain.md`（温度数据）/ `iot.md`（IoT 传感）/ `trace.md`（溯源码）

---

## 1. 接口索引（23 端点）

| # | 接口中文名 | URL | 方法 | 认证 |
|---|-----------|-----|------|------|
| 1 | [仓库列表（分页）](#21-仓库列表分页) | `/api/warehouse/warehouses/list` | GET | ✅ |
| 2 | [仓库详情](#22-仓库详情) | `/api/warehouse/warehouses/{id}` | GET | ✅ |
| 3 | [创建仓库](#23-创建仓库) | `/api/warehouse/warehouses` | POST | ✅ |
| 4 | [更新仓库](#24-更新仓库) | `/api/warehouse/warehouses/{id}` | PUT | ✅ |
| 5 | [删除仓库](#25-删除仓库) | `/api/warehouse/warehouses/{id}` | DELETE | ✅ |
| 6 | [仓库预警列表](#26-仓库预警列表) | `/api/warehouse/warehouses/alerts` | GET | ✅ |
| 7 | [变更仓库状态](#27-变更仓库状态) | `/api/warehouse/warehouses/{id}/status` | PUT | ✅ |
| 8 | [导出仓库 CSV](#28-导出仓库-csv) | `/api/warehouse/warehouses/export` | GET | ✅ |
| 9 | [仓单列表（分页）](#31-仓单列表分页) | `/api/warehouse/receipts/list` | GET | ✅ |
| 10 | [仓单详情](#32-仓单详情) | `/api/warehouse/receipts/{id}` | GET | ✅ |
| 11 | [创建仓单](#33-创建仓单) | `/api/warehouse/receipts` | POST | ✅ |
| 12 | [更新仓单](#34-更新仓单) | `/api/warehouse/receipts/{id}` | PUT | ✅ |
| 13 | [变更仓单状态](#35-变更仓单状态) | `/api/warehouse/receipts/{id}/status` | PUT | ✅ |
| 14 | [删除仓单](#36-删除仓单) | `/api/warehouse/receipts/{id}` | DELETE | ✅ |
| 15 | [导出仓单 CSV](#37-导出仓单-csv) | `/api/warehouse/receipts/export` | GET | ✅ |
| 16 | [出入库记录列表（分页）](#41-出入库记录列表分页) | `/api/warehouse/records/list` | GET | ✅ |
| 17 | [出入库记录详情](#42-出入库记录详情) | `/api/warehouse/records/{id}` | GET | ✅ |
| 18 | [创建出入库记录](#43-创建出入库记录) | `/api/warehouse/records` | POST | ✅ |
| 19 | [删除出入库记录](#44-删除出入库记录) | `/api/warehouse/records/{id}` | DELETE | ✅ |
| 20 | [导出出入库记录 CSV](#45-导出出入库记录-csv) | `/api/warehouse/records/export` | GET | ✅ |
| 21 | [概览统计](#51-概览统计) | `/api/warehouse/statistics/summary` | GET | ✅ |
| 22 | [周转率统计](#52-周转率统计) | `/api/warehouse/statistics/turnover` | GET | ✅ |
| 23 | [损耗率统计](#53-损耗率统计) | `/api/warehouse/statistics/loss` | GET | ✅ |

---

## 2. 仓库管理 WarehouseController（`/api/warehouse/warehouses`）

### 2.1 仓库列表（分页）

**URL**：`GET /api/warehouse/warehouses/list`
**认证**：需要 JWT Bearer
**权限**：`warehouse:read`
**错误码**：`600900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，从 1 开始，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 仓库名称/编码模糊搜索 | `冷库` |
| `type` | number | ❌ | 仓库类型码（见 WarehouseType 枚举） | `2` |
| `status` | number | ❌ | 仓库状态码（见 WarehouseStatus 枚举） | `1` |

```typescript
export interface WarehousePageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  type?: number;
  status?: number;
}
```

#### 响应体 `PageResult<WarehouseResponse>`

`WarehouseResponse` 字段：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 仓库 ID（雪花） |
| `warehouseCode` | string | ✅ | 仓库编码，格式 `WH+yyyyMMdd+序号` |
| `name` | string | ✅ | 仓库名称 |
| `type` | `EnumValue<number>` | ✅ | 仓库类型，见 WarehouseType |
| `location` | string \| null | ❌ | 仓库位置 |
| `capacity` | number | ✅ | 总容量（吨），精度 2 位小数 |
| `usedCapacity` | number | ✅ | 已用容量（吨），精度 2 位小数 |
| `temperature` | number \| null | ❌ | 当前温度（℃） |
| `humidity` | number \| null | ❌ | 当前湿度（%） |
| `manager` | string \| null | ❌ | 仓库管理员姓名 |
| `phone` | string \| null | ❌ | 联系电话 |
| `status` | `EnumValue<number>` | ✅ | 仓库状态，见 WarehouseStatus |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface WarehouseResponse {
  id: number;
  warehouseCode: string;
  name: string;
  type: EnumValue<number>;
  location: string | null;
  capacity: number;
  usedCapacity: number;
  temperature: number | null;
  humidity: number | null;
  manager: string | null;
  phone: string | null;
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
        "id": 1001,
        "warehouseCode": "WH20260412001",
        "name": "陕西富县冷链中心A库",
        "type": { "code": 2, "desc": "冷库" },
        "location": "陕西省延安市富县苹果产业园区",
        "capacity": 500.00,
        "usedCapacity": 320.50,
        "temperature": 2.5,
        "humidity": 85.0,
        "manager": "王建国",
        "phone": "18691234567",
        "status": { "code": 1, "desc": "正常" },
        "remark": null,
        "createdAt": "2026-04-01 10:00:00",
        "updatedAt": "2026-04-12 08:30:00"
      }
    ],
    "total": 12,
    "current": 1,
    "size": 10
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 600900,
  "message": "参数非法：size 不能超过 100",
  "data": null
}
```

#### 业务规则

- 容量利用率 = `usedCapacity / capacity × 100%`，可在前端计算后展示
- 仅返回未软删除的仓库

---

### 2.2 仓库详情

**URL**：`GET /api/warehouse/warehouses/{id}`
**认证**：需要 JWT Bearer
**权限**：`warehouse:read`
**错误码**：`600001`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 仓库 ID |

#### 响应体 `WarehouseResponse`

与 2.1 中 `WarehouseResponse` 结构相同，返回单条仓库详情。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "warehouseCode": "WH20260412001",
    "name": "陕西富县冷链中心A库",
    "type": { "code": 2, "desc": "冷库" },
    "location": "陕西省延安市富县苹果产业园区",
    "capacity": 500.00,
    "usedCapacity": 320.50,
    "temperature": 2.5,
    "humidity": 85.0,
    "manager": "王建国",
    "phone": "18691234567",
    "status": { "code": 1, "desc": "正常" },
    "remark": null,
    "createdAt": "2026-04-01 10:00:00",
    "updatedAt": "2026-04-12 08:30:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 600001,
  "message": "仓库不存在",
  "data": null
}
```

---

### 2.3 创建仓库

**URL**：`POST /api/warehouse/warehouses`
**认证**：需要 JWT Bearer
**权限**：`warehouse:write`
**错误码**：`600002` / `600005` / `600004` / `600900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `WarehouseCreateRequest`

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `name` | string | ✅ | 仓库名称，≤100 字符 | `陕西富县冷链中心A库` |
| `type` | number | ✅ | 仓库类型码（见 WarehouseType） | `2` |
| `location` | string | ❌ | 位置描述 | `陕西省延安市富县苹果产业园区` |
| `capacity` | number | ✅ | 总容量（吨），> 0，精度 2 位小数 | `500.00` |
| `temperature` | number | ❌ | 当前温度（℃） | `2.5` |
| `humidity` | number | ❌ | 当前湿度（%），0-100 | `85.0` |
| `manager` | string | ❌ | 管理员姓名 | `王建国` |
| `phone` | string | ❌ | 联系电话 | `18691234567` |
| `remark` | string | ❌ | 备注 | `A号冷库` |

```typescript
export interface WarehouseCreateRequest {
  name: string;
  type: number;
  location?: string;
  capacity: number;
  temperature?: number;
  humidity?: number;
  manager?: string;
  phone?: string;
  remark?: string;
}
```

#### 响应体 `WarehouseResponse`

返回新创建的仓库完整信息（含系统生成的 `warehouseCode`、`id`、`usedCapacity=0`、`status=ACTIVE`）。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "warehouseCode": "WH20260412001",
    "name": "陕西富县冷链中心A库",
    "type": { "code": 2, "desc": "冷库" },
    "location": "陕西省延安市富县苹果产业园区",
    "capacity": 500.00,
    "usedCapacity": 0.00,
    "temperature": 2.5,
    "humidity": 85.0,
    "manager": "王建国",
    "phone": "18691234567",
    "status": { "code": 1, "desc": "正常" },
    "remark": null,
    "createdAt": "2026-04-12 10:00:00",
    "updatedAt": "2026-04-12 10:00:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 600002,
  "message": "仓库编码重复",
  "data": null
}
```

#### 业务规则

- `warehouseCode` 由系统自动生成，格式 `WH+yyyyMMdd+3 位序号`，全局唯一
- 初始 `usedCapacity` = 0，状态默认 `ACTIVE`
- `type` 必须为合法 WarehouseType code（1/2/3），否则抛 `600005`
- `capacity` 必须 > 0

---

### 2.4 更新仓库

**URL**：`PUT /api/warehouse/warehouses/{id}`
**认证**：需要 JWT Bearer
**权限**：`warehouse:write`
**错误码**：`600001` / `600005` / `600900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 仓库 ID |

#### 请求体 `WarehouseUpdateRequest`

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `name` | string | ❌ | 仓库名称 | `A库（扩建后）` |
| `type` | number | ❌ | 仓库类型码 | `2` |
| `location` | string | ❌ | 位置描述 | `陕西省延安市富县` |
| `capacity` | number | ❌ | 总容量（吨），> 0 | `600.00` |
| `temperature` | number | ❌ | 当前温度（℃） | `2.0` |
| `humidity` | number | ❌ | 当前湿度（%） | `88.0` |
| `manager` | string | ❌ | 管理员姓名 | `李明` |
| `phone` | string | ❌ | 联系电话 | `18691112222` |
| `remark` | string | ❌ | 备注 | `更新备注` |

```typescript
export interface WarehouseUpdateRequest {
  name?: string;
  type?: number;
  location?: string;
  capacity?: number;
  temperature?: number;
  humidity?: number;
  manager?: string;
  phone?: string;
  remark?: string;
}
```

#### 响应体 `WarehouseResponse`

返回更新后的仓库完整信息。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "warehouseCode": "WH20260412001",
    "name": "A库（扩建后）",
    "type": { "code": 2, "desc": "冷库" },
    "location": "陕西省延安市富县",
    "capacity": 600.00,
    "usedCapacity": 320.50,
    "temperature": 2.0,
    "humidity": 88.0,
    "manager": "李明",
    "phone": "18691112222",
    "status": { "code": 1, "desc": "正常" },
    "remark": "更新备注",
    "createdAt": "2026-04-01 10:00:00",
    "updatedAt": "2026-04-12 14:00:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 600001,
  "message": "仓库不存在",
  "data": null
}
```

#### 业务规则

- 更新 `capacity` 时，新容量必须 ≥ `usedCapacity`，否则抛 `600004`
- `warehouseCode` 不可修改
- `status` 字段通过专用端点 [2.7](#27-变更仓库状态) 变更，本端点忽略 `status` 字段

---

### 2.5 删除仓库

**URL**：`DELETE /api/warehouse/warehouses/{id}`
**认证**：需要 JWT Bearer
**权限**：`warehouse:delete`
**错误码**：`600001` / `600006`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 仓库 ID |

#### 响应体

无 data（`data: null`）。

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
  "code": 600006,
  "message": "仓库有活跃出入库记录，无法删除",
  "data": null
}
```

#### 业务规则

- 软删除（逻辑删除），通过 BaseEntity.deleted 标记
- 若仓库存在状态为有效的仓单（`ReceiptStatus=VALID/PLEDGED`）或 `usedCapacity > 0`，抛 `600006`
- 删除后该仓库在列表接口中不再返回

---

### 2.6 仓库预警列表

**URL**：`GET /api/warehouse/warehouses/alerts`
**认证**：需要 JWT Bearer
**权限**：`warehouse:read`
**错误码**：无业务错误
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无参数。

#### 响应体 `List<WarehouseResponse>`

返回需要预警的仓库列表（非分页）。

预警触发条件（满足任一）：
- 容量利用率 ≥ 90%（`usedCapacity / capacity ≥ 0.9`）
- 状态为 `FULL` 或 `MAINTENANCE`
- 温度超出安全阈值（冷库 > 8℃ 或 < -2℃，气调库 > 5℃）

```typescript
export type WarehouseAlertsResponse = WarehouseResponse[];
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1002,
      "warehouseCode": "WH20260401002",
      "name": "气调库B区",
      "type": { "code": 3, "desc": "气调库" },
      "location": "陕西省延安市宝塔区",
      "capacity": 200.00,
      "usedCapacity": 192.00,
      "temperature": 6.5,
      "humidity": 90.0,
      "manager": "张伟",
      "phone": "18699887766",
      "status": { "code": 3, "desc": "已满" },
      "remark": "温度超出安全阈值",
      "createdAt": "2026-04-01 09:00:00",
      "updatedAt": "2026-04-12 07:00:00"
    }
  ]
}
```

#### 业务规则

- 返回空列表（`[]`）而非 null，当无预警时
- 该接口适合定时轮询，建议前端每 5 分钟刷新一次

---

### 2.7 变更仓库状态

**URL**：`PUT /api/warehouse/warehouses/{id}/status`
**认证**：需要 JWT Bearer
**权限**：`warehouse:write`
**错误码**：`600001` / `600003`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 仓库 ID |

#### 请求体 `WarehouseStatusChangeRequest`

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `status` | number | ✅ | 目标状态码（见 WarehouseStatus） | `2` |
| `reason` | string | ❌ | 变更原因 | `进入定期维护` |

```typescript
export interface WarehouseStatusChangeRequest {
  status: number;
  reason?: string;
}
```

#### 响应体 `WarehouseResponse`

返回变更后的仓库完整信息。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "warehouseCode": "WH20260412001",
    "name": "陕西富县冷链中心A库",
    "type": { "code": 2, "desc": "冷库" },
    "location": "陕西省延安市富县苹果产业园区",
    "capacity": 500.00,
    "usedCapacity": 0.00,
    "temperature": 2.5,
    "humidity": 85.0,
    "manager": "王建国",
    "phone": "18691234567",
    "status": { "code": 2, "desc": "维护中" },
    "remark": null,
    "createdAt": "2026-04-01 10:00:00",
    "updatedAt": "2026-04-12 15:00:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 600003,
  "message": "仓库状态流转非法：FULL 不能直接变更为 CLOSED",
  "data": null
}
```

#### 业务规则（状态机）

合法的状态流转路径：

```
ACTIVE  →  MAINTENANCE（进入维护）
ACTIVE  →  FULL（容量已满）
ACTIVE  →  CLOSED（直接关闭）
MAINTENANCE  →  ACTIVE（维护完成）
MAINTENANCE  →  CLOSED（维护期间关闭）
FULL  →  ACTIVE（有出库记录后自动或手动恢复）
CLOSED  →  （终态，不可再变更）
```

- 状态码必须为合法 WarehouseStatus code（1/2/3/4），否则抛 `600003`
- 不在上述路径中的流转一律抛 `600003`

> **实现注记**：当前 Controller 接收 `Map<String, String>` 且 status 为 String 类型，契约目标态为接收 `WarehouseStatusChangeRequest`（status 为 number code）。迁移清单见 §8.2。

---

### 2.8 导出仓库 CSV

**URL**：`GET /api/warehouse/warehouses/export`
**认证**：需要 JWT Bearer
**权限**：`warehouse:export`
**错误码**：`600900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `keyword` | string | ❌ | 仓库名称/编码模糊搜索 | `冷库` |
| `type` | number | ❌ | 仓库类型码 | `2` |
| `status` | number | ❌ | 仓库状态码 | `1` |

#### 响应

**非 `R<T>` 包装**，直接返回文件流。

| Header | 值 |
|--------|----|
| `Content-Type` | `text/csv;charset=UTF-8` |
| `Content-Disposition` | `attachment; filename=warehouses_<timestamp>.csv` |

CSV 列顺序：`id, warehouseCode, name, type, location, capacity, usedCapacity, temperature, humidity, manager, phone, status, remark, createdAt`

#### 响应示例（失败，无法导出时返回 R 错误）

```json
{
  "code": 600900,
  "message": "导出参数非法",
  "data": null
}
```

#### 业务规则

- 导出条件与列表接口筛选条件一致
- 文件名包含时间戳以避免缓存问题
- 中文内容使用 UTF-8 with BOM 确保 Excel 正常打开

---

## 3. 智能仓单 WarehouseReceiptController（`/api/warehouse/receipts`）

### 3.1 仓单列表（分页）

**URL**：`GET /api/warehouse/receipts/list`
**认证**：需要 JWT Bearer
**权限**：`warehouse:receipt:read`
**错误码**：`600900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 仓单编号/货主姓名模糊搜索 | `WR20260` |
| `status` | number | ❌ | 仓单状态码（见 ReceiptStatus） | `1` |

```typescript
export interface WarehouseReceiptPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  status?: number;
}
```

#### 响应体 `PageResult<WarehouseReceiptResponse>`

`WarehouseReceiptResponse` 字段：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 仓单 ID（雪花） |
| `receiptNo` | string | ✅ | 仓单编号，格式 `WR+yyyyMMdd+序号` |
| `warehouseId` | number | ✅ | 关联仓库 ID |
| `warehouseName` | string | ✅ | 仓库名称（冗余） |
| `farmerId` | number | ✅ | 货主（果农）ID |
| `farmerName` | string | ✅ | 货主姓名 |
| `batchCode` | string \| null | ❌ | 种植批次编码（关联 planting.md） |
| `variety` | string \| null | ❌ | 苹果品种 |
| `grade` | `EnumValue<string>` \| null | ❌ | 质量等级，见 AppleGrade |
| `quantity` | number | ✅ | 存储数量（kg），精度 2 位小数 |
| `unitValue` | number \| null | ❌ | 每公斤估值（元） |
| `totalValue` | number \| null | ❌ | 总估值（元） |
| `inboundDate` | string | ✅ | 入库日期，格式 `yyyy-MM-dd` |
| `validUntil` | string | ✅ | 有效期至，格式 `yyyy-MM-dd` |
| `traceCode` | string \| null | ❌ | 溯源码（关联 trace.md） |
| `status` | `EnumValue<string>` | ✅ | 仓单状态，见 ReceiptStatus |
| `statusChangeBy` | string \| null | ❌ | 状态变更操作人 |
| `statusChangeTime` | string \| null | ❌ | 状态变更时间，`yyyy-MM-dd HH:mm:ss` |
| `statusChangeReason` | string \| null | ❌ | 状态变更原因 |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface WarehouseReceiptResponse {
  id: number;
  receiptNo: string;
  warehouseId: number;
  warehouseName: string;
  farmerId: number;
  farmerName: string;
  batchCode: string | null;
  variety: string | null;
  grade: EnumValue<string> | null;
  quantity: number;
  unitValue: number | null;
  totalValue: number | null;
  inboundDate: string;
  validUntil: string;
  traceCode: string | null;
  status: EnumValue<string>;
  statusChangeBy: string | null;
  statusChangeTime: string | null;
  statusChangeReason: string | null;
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
        "receiptNo": "WR20260412001",
        "warehouseId": 1001,
        "warehouseName": "陕西富县冷链中心A库",
        "farmerId": 30001,
        "farmerName": "张三丰",
        "batchCode": "BATCH20261001001",
        "variety": "红富士",
        "grade": { "code": "A", "desc": "特级" },
        "quantity": 5000.00,
        "unitValue": 6.50,
        "totalValue": 32500.00,
        "inboundDate": "2026-10-15",
        "validUntil": "2027-04-15",
        "traceCode": "TR20261001001",
        "status": { "code": "VALID", "desc": "有效" },
        "statusChangeBy": null,
        "statusChangeTime": null,
        "statusChangeReason": null,
        "remark": null,
        "createdAt": "2026-10-15 09:00:00",
        "updatedAt": "2026-10-15 09:00:00"
      }
    ],
    "total": 25,
    "current": 1,
    "size": 10
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 600900,
  "message": "参数非法：size 不能超过 100",
  "data": null
}
```

---

### 3.2 仓单详情

**URL**：`GET /api/warehouse/receipts/{id}`
**认证**：需要 JWT Bearer
**权限**：`warehouse:receipt:read`
**错误码**：`600100`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 仓单 ID |

#### 响应体 `WarehouseReceiptResponse`

与 3.1 中 `WarehouseReceiptResponse` 结构相同，返回单条仓单详情。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 2001,
    "receiptNo": "WR20260412001",
    "warehouseId": 1001,
    "warehouseName": "陕西富县冷链中心A库",
    "farmerId": 30001,
    "farmerName": "张三丰",
    "batchCode": "BATCH20261001001",
    "variety": "红富士",
    "grade": { "code": "A", "desc": "特级" },
    "quantity": 5000.00,
    "unitValue": 6.50,
    "totalValue": 32500.00,
    "inboundDate": "2026-10-15",
    "validUntil": "2027-04-15",
    "traceCode": "TR20261001001",
    "status": { "code": "VALID", "desc": "有效" },
    "statusChangeBy": null,
    "statusChangeTime": null,
    "statusChangeReason": null,
    "remark": null,
    "createdAt": "2026-10-15 09:00:00",
    "updatedAt": "2026-10-15 09:00:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 600100,
  "message": "仓单不存在",
  "data": null
}
```

---

### 3.3 创建仓单

**URL**：`POST /api/warehouse/receipts`
**认证**：需要 JWT Bearer
**权限**：`warehouse:receipt:write`
**错误码**：`600001` / `600101` / `600105` / `600900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `WarehouseReceiptCreateRequest`

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `warehouseId` | number | ✅ | 关联仓库 ID | `1001` |
| `farmerId` | number | ✅ | 货主（果农）ID | `30001` |
| `farmerName` | string | ✅ | 货主姓名 | `张三丰` |
| `batchCode` | string | ❌ | 种植批次编码 | `BATCH20261001001` |
| `variety` | string | ❌ | 苹果品种 | `红富士` |
| `grade` | string | ❌ | 质量等级 code（见 AppleGrade） | `A` |
| `quantity` | number | ✅ | 存储数量（kg），> 0 | `5000.00` |
| `unitValue` | number | ❌ | 每公斤估值（元），> 0 | `6.50` |
| `inboundDate` | string | ✅ | 入库日期，`yyyy-MM-dd` | `2026-10-15` |
| `validUntil` | string | ✅ | 有效期至，`yyyy-MM-dd`，必须 > inboundDate | `2027-04-15` |
| `traceCode` | string | ❌ | 溯源码 | `TR20261001001` |
| `remark` | string | ❌ | 备注 | `` |

```typescript
export interface WarehouseReceiptCreateRequest {
  warehouseId: number;
  farmerId: number;
  farmerName: string;
  batchCode?: string;
  variety?: string;
  grade?: string;
  quantity: number;
  unitValue?: number;
  inboundDate: string;
  validUntil: string;
  traceCode?: string;
  remark?: string;
}
```

#### 响应体 `WarehouseReceiptResponse`

返回新创建的仓单完整信息（含系统生成的 `receiptNo`、初始 `status=VALID`）。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 2001,
    "receiptNo": "WR20261015001",
    "warehouseId": 1001,
    "warehouseName": "陕西富县冷链中心A库",
    "farmerId": 30001,
    "farmerName": "张三丰",
    "batchCode": "BATCH20261001001",
    "variety": "红富士",
    "grade": { "code": "A", "desc": "特级" },
    "quantity": 5000.00,
    "unitValue": 6.50,
    "totalValue": 32500.00,
    "inboundDate": "2026-10-15",
    "validUntil": "2027-04-15",
    "traceCode": "TR20261001001",
    "status": { "code": "VALID", "desc": "有效" },
    "statusChangeBy": null,
    "statusChangeTime": null,
    "statusChangeReason": null,
    "remark": null,
    "createdAt": "2026-10-15 09:00:00",
    "updatedAt": "2026-10-15 09:00:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 600105,
  "message": "存储数量非法：数量必须大于 0",
  "data": null
}
```

#### 业务规则

- `receiptNo` 由系统自动生成，格式 `WR+yyyyMMdd+3 位序号`，全局唯一
- `totalValue = quantity × unitValue`（若 unitValue 不为空则自动计算）
- `validUntil` 必须晚于 `inboundDate`，否则抛 `600105`
- 创建仓单会增加关联仓库的 `usedCapacity`，若导致 `usedCapacity > capacity` 则抛 `600004`（仓库容量溢出）
- 初始状态为 `VALID`

---

### 3.4 更新仓单

**URL**：`PUT /api/warehouse/receipts/{id}`
**认证**：需要 JWT Bearer
**权限**：`warehouse:receipt:write`
**错误码**：`600100` / `600103` / `600105` / `600900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 仓单 ID |

#### 请求体 `WarehouseReceiptUpdateRequest`

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `variety` | string | ❌ | 苹果品种 | `嘎拉` |
| `grade` | string | ❌ | 质量等级 code | `B` |
| `quantity` | number | ❌ | 存储数量（kg），> 0 | `4800.00` |
| `unitValue` | number | ❌ | 每公斤估值（元） | `6.00` |
| `validUntil` | string | ❌ | 有效期至，`yyyy-MM-dd` | `2027-05-15` |
| `traceCode` | string | ❌ | 溯源码 | `TR20261001002` |
| `remark` | string | ❌ | 备注 | `修正数量` |

```typescript
export interface WarehouseReceiptUpdateRequest {
  variety?: string;
  grade?: string;
  quantity?: number;
  unitValue?: number;
  validUntil?: string;
  traceCode?: string;
  remark?: string;
}
```

#### 响应体 `WarehouseReceiptResponse`

返回更新后的仓单完整信息。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 2001,
    "receiptNo": "WR20261015001",
    "warehouseId": 1001,
    "warehouseName": "陕西富县冷链中心A库",
    "farmerId": 30001,
    "farmerName": "张三丰",
    "batchCode": "BATCH20261001001",
    "variety": "嘎拉",
    "grade": { "code": "B", "desc": "一级" },
    "quantity": 4800.00,
    "unitValue": 6.00,
    "totalValue": 28800.00,
    "inboundDate": "2026-10-15",
    "validUntil": "2027-05-15",
    "traceCode": "TR20261001002",
    "status": { "code": "VALID", "desc": "有效" },
    "statusChangeBy": null,
    "statusChangeTime": null,
    "statusChangeReason": null,
    "remark": "修正数量",
    "createdAt": "2026-10-15 09:00:00",
    "updatedAt": "2026-04-12 10:00:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 600103,
  "message": "仓单已质押，不能修改",
  "data": null
}
```

#### 业务规则

- 仓单状态为 `PLEDGED` 时，**禁止**修改任何字段，抛 `600103`
- 仓单状态为 `TRANSFERRED` 或 `CANCELLED` 时，**禁止**修改，抛 `600102`
- `warehouseId`、`farmerId`、`receiptNo`、`inboundDate` 不可修改
- 修改 `quantity` 时同步更新关联仓库 `usedCapacity`（差值更新），若溢出则抛 `600004`

---

### 3.5 变更仓单状态

**URL**：`PUT /api/warehouse/receipts/{id}/status`
**认证**：需要 JWT Bearer
**权限**：`warehouse:receipt:write`
**错误码**：`600100` / `600102` / `600103` / `600104`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 仓单 ID |

#### 请求体 `WarehouseReceiptStatusChangeRequest`

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `status` | string | ✅ | 目标状态 code（见 ReceiptStatus） | `PLEDGED` |
| `reason` | string | ❌ | 状态变更原因 | `申请金融质押` |
| `operator` | string | ❌ | 操作人姓名 | `李行长` |

```typescript
export interface WarehouseReceiptStatusChangeRequest {
  status: string;
  reason?: string;
  operator?: string;
}
```

#### 响应体 `WarehouseReceiptResponse`

返回变更后的仓单完整信息（含 `statusChangeBy`、`statusChangeTime`、`statusChangeReason`）。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 2001,
    "receiptNo": "WR20261015001",
    "warehouseId": 1001,
    "warehouseName": "陕西富县冷链中心A库",
    "farmerId": 30001,
    "farmerName": "张三丰",
    "batchCode": "BATCH20261001001",
    "variety": "红富士",
    "grade": { "code": "A", "desc": "特级" },
    "quantity": 5000.00,
    "unitValue": 6.50,
    "totalValue": 32500.00,
    "inboundDate": "2026-10-15",
    "validUntil": "2027-04-15",
    "traceCode": "TR20261001001",
    "status": { "code": "PLEDGED", "desc": "质押中" },
    "statusChangeBy": "李行长",
    "statusChangeTime": "2026-04-12 11:00:00",
    "statusChangeReason": "申请金融质押",
    "remark": null,
    "createdAt": "2026-10-15 09:00:00",
    "updatedAt": "2026-04-12 11:00:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 600104,
  "message": "仓单已过期，无法变更状态",
  "data": null
}
```

#### 业务规则（状态机）

合法的状态流转路径：

```
VALID  →  PLEDGED（质押申请，关联 finance.md 融资单）
VALID  →  TRANSFERRED（所有权转让）
VALID  →  CANCELLED（注销）
PLEDGED  →  VALID（质押解除）
PLEDGED  →  CANCELLED（质押结束注销）
TRANSFERRED  →  （终态，不可再变更）
CANCELLED  →  （终态，不可再变更）
```

- 仓单 `validUntil < 今日` 时，状态为过期，禁止任何流转，抛 `600104`
- `PLEDGED` 状态的仓单不可 `TRANSFERRED`，抛 `600103`
- 状态 code 不在合法枚举中，抛 `600102`

> **实现注记**：当前 Controller 接收 `Map<String, String>` body，契约目标态为接收 `WarehouseReceiptStatusChangeRequest`。同时 `statusChangeBy`/`statusChangeTime`/`statusChangeReason` 需在 Service 层填入。迁移清单见 §8.2。

---

### 3.6 删除仓单

**URL**：`DELETE /api/warehouse/receipts/{id}`
**认证**：需要 JWT Bearer
**权限**：`warehouse:receipt:delete`
**错误码**：`600100` / `600102` / `600103`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 仓单 ID |

#### 响应体

无 data（`data: null`）。

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
  "code": 600103,
  "message": "仓单已质押，不能删除",
  "data": null
}
```

#### 业务规则

- 软删除（逻辑删除）
- 仓单状态为 `PLEDGED` 时，**禁止**删除，抛 `600103`
- 仓单状态为 `VALID` 时需同步减少关联仓库 `usedCapacity`
- 仓单状态已为 `CANCELLED` 时可删除

---

### 3.7 导出仓单 CSV

**URL**：`GET /api/warehouse/receipts/export`
**认证**：需要 JWT Bearer
**权限**：`warehouse:receipt:export`
**错误码**：`600900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `keyword` | string | ❌ | 仓单编号/货主姓名模糊搜索 | `张三` |
| `status` | string | ❌ | 仓单状态 code | `VALID` |

#### 响应

**非 `R<T>` 包装**，直接返回文件流。

| Header | 值 |
|--------|----|
| `Content-Type` | `text/csv;charset=UTF-8` |
| `Content-Disposition` | `attachment; filename=receipts_<timestamp>.csv` |

CSV 列顺序：`id, receiptNo, warehouseName, farmerName, variety, grade, quantity, unitValue, totalValue, inboundDate, validUntil, status, traceCode, createdAt`

#### 响应示例（失败）

```json
{
  "code": 600900,
  "message": "导出参数非法",
  "data": null
}
```

---

## 4. 出入库记录 WarehouseRecordController（`/api/warehouse/records`）

### 4.1 出入库记录列表（分页）

**URL**：`GET /api/warehouse/records/list`
**认证**：需要 JWT Bearer
**权限**：`warehouse:record:read`
**错误码**：`600900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 记录编号/操作人/批次编码模糊搜索 | `WR` |
| `recordType` | string | ❌ | 出入库类型 code（见 RecordType） | `INBOUND` |
| `warehouseId` | number | ❌ | 按仓库 ID 筛选 | `1001` |

```typescript
export interface WarehouseRecordPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  recordType?: string;
  warehouseId?: number;
}
```

#### 响应体 `PageResult<WarehouseRecordResponse>`

`WarehouseRecordResponse` 字段：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 记录 ID（雪花） |
| `recordNo` | string | ✅ | 记录编号 |
| `warehouseId` | number | ✅ | 关联仓库 ID |
| `warehouseName` | string | ✅ | 仓库名称（冗余） |
| `recordType` | `EnumValue<string>` | ✅ | 出入库类型，见 RecordType |
| `batchCode` | string \| null | ❌ | 关联种植批次编码 |
| `variety` | string \| null | ❌ | 苹果品种 |
| `grade` | `EnumValue<string>` \| null | ❌ | 质量等级，见 AppleGrade |
| `quantity` | number | ✅ | 数量（kg），精度 2 位小数 |
| `temperature` | number \| null | ❌ | 操作时温度（℃） |
| `humidity` | number \| null | ❌ | 操作时湿度（%） |
| `operator` | string \| null | ❌ | 操作人 |
| `recordDate` | string | ✅ | 操作日期，格式 `yyyy-MM-dd` |
| `traceCode` | string \| null | ❌ | 溯源码 |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface WarehouseRecordResponse {
  id: number;
  recordNo: string;
  warehouseId: number;
  warehouseName: string;
  recordType: EnumValue<string>;
  batchCode: string | null;
  variety: string | null;
  grade: EnumValue<string> | null;
  quantity: number;
  temperature: number | null;
  humidity: number | null;
  operator: string | null;
  recordDate: string;
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
        "recordNo": "REC20261015001",
        "warehouseId": 1001,
        "warehouseName": "陕西富县冷链中心A库",
        "recordType": { "code": "INBOUND", "desc": "入库" },
        "batchCode": "BATCH20261001001",
        "variety": "红富士",
        "grade": { "code": "A", "desc": "特级" },
        "quantity": 5000.00,
        "temperature": 2.5,
        "humidity": 85.0,
        "operator": "王建国",
        "recordDate": "2026-10-15",
        "traceCode": "TR20261001001",
        "remark": null,
        "createdAt": "2026-10-15 09:30:00",
        "updatedAt": "2026-10-15 09:30:00"
      }
    ],
    "total": 88,
    "current": 1,
    "size": 10
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 600900,
  "message": "参数非法",
  "data": null
}
```

---

### 4.2 出入库记录详情

**URL**：`GET /api/warehouse/records/{id}`
**认证**：需要 JWT Bearer
**权限**：`warehouse:record:read`
**错误码**：`600200`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 记录 ID |

#### 响应体 `WarehouseRecordResponse`

与 4.1 中 `WarehouseRecordResponse` 结构相同，返回单条记录详情。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 3001,
    "recordNo": "REC20261015001",
    "warehouseId": 1001,
    "warehouseName": "陕西富县冷链中心A库",
    "recordType": { "code": "INBOUND", "desc": "入库" },
    "batchCode": "BATCH20261001001",
    "variety": "红富士",
    "grade": { "code": "A", "desc": "特级" },
    "quantity": 5000.00,
    "temperature": 2.5,
    "humidity": 85.0,
    "operator": "王建国",
    "recordDate": "2026-10-15",
    "traceCode": "TR20261001001",
    "remark": null,
    "createdAt": "2026-10-15 09:30:00",
    "updatedAt": "2026-10-15 09:30:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 600200,
  "message": "出入库记录不存在",
  "data": null
}
```

---

### 4.3 创建出入库记录

**URL**：`POST /api/warehouse/records`
**认证**：需要 JWT Bearer
**权限**：`warehouse:record:write`
**错误码**：`600001` / `600201` / `600202` / `600203` / `600900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `WarehouseRecordCreateRequest`

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `warehouseId` | number | ✅ | 关联仓库 ID | `1001` |
| `recordType` | string | ✅ | 出入库类型 code（`INBOUND`/`OUTBOUND`） | `INBOUND` |
| `batchCode` | string | ❌ | 种植批次编码（关联 planting.md） | `BATCH20261001001` |
| `variety` | string | ❌ | 苹果品种 | `红富士` |
| `grade` | string | ❌ | 质量等级 code（见 AppleGrade） | `A` |
| `quantity` | number | ✅ | 数量（kg），> 0 | `5000.00` |
| `temperature` | number | ❌ | 操作时温度（℃） | `2.5` |
| `humidity` | number | ❌ | 操作时湿度（%） | `85.0` |
| `operator` | string | ❌ | 操作人姓名 | `王建国` |
| `recordDate` | string | ✅ | 操作日期，`yyyy-MM-dd` | `2026-10-15` |
| `traceCode` | string | ❌ | 溯源码 | `TR20261001001` |
| `remark` | string | ❌ | 备注 | `` |

```typescript
export interface WarehouseRecordCreateRequest {
  warehouseId: number;
  recordType: string;
  batchCode?: string;
  variety?: string;
  grade?: string;
  quantity: number;
  temperature?: number;
  humidity?: number;
  operator?: string;
  recordDate: string;
  traceCode?: string;
  remark?: string;
}
```

#### 响应体 `WarehouseRecordResponse`

返回新创建的出入库记录完整信息（含系统生成的 `recordNo`）。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 3001,
    "recordNo": "REC20261015001",
    "warehouseId": 1001,
    "warehouseName": "陕西富县冷链中心A库",
    "recordType": { "code": "INBOUND", "desc": "入库" },
    "batchCode": "BATCH20261001001",
    "variety": "红富士",
    "grade": { "code": "A", "desc": "特级" },
    "quantity": 5000.00,
    "temperature": 2.5,
    "humidity": 85.0,
    "operator": "王建国",
    "recordDate": "2026-10-15",
    "traceCode": "TR20261001001",
    "remark": null,
    "createdAt": "2026-10-15 09:30:00",
    "updatedAt": "2026-10-15 09:30:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 600202,
  "message": "库存不足，当前库存 200.00 kg，无法出库 500.00 kg",
  "data": null
}
```

#### 业务规则

- **入库（INBOUND）**：仓库 `usedCapacity += quantity`；若入库后 `usedCapacity > capacity`，抛 `600004`（仓库容量溢出）
- **出库（OUTBOUND）**：仓库 `usedCapacity -= quantity`；若 `usedCapacity < quantity`（库存不足），抛 `600202`
- `recordType` 必须为 `INBOUND` 或 `OUTBOUND`，否则抛 `600201`
- `warehouseId` 对应仓库不存在则抛 `600001`
- `recordNo` 由系统自动生成
- 出库操作自动校验仓库归属（`warehouseId` 与货物来源一致），不一致抛 `600203`

---

### 4.4 删除出入库记录

**URL**：`DELETE /api/warehouse/records/{id}`
**认证**：需要 JWT Bearer
**权限**：`warehouse:record:delete`
**错误码**：`600200`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 记录 ID |

#### 响应体

无 data（`data: null`）。

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
  "code": 600200,
  "message": "出入库记录不存在",
  "data": null
}
```

#### 业务规则

- 软删除（逻辑删除）
- 删除记录**不回滚**仓库容量（历史记录仅归档，不影响当前库存）
- 如需回滚库存，业务上应创建反向出入库记录（出库抵消入库）

---

### 4.5 导出出入库记录 CSV

**URL**：`GET /api/warehouse/records/export`
**认证**：需要 JWT Bearer
**权限**：`warehouse:record:export`
**错误码**：`600900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `keyword` | string | ❌ | 记录编号/操作人/批次编码模糊搜索 | `王建国` |
| `recordType` | string | ❌ | 出入库类型 code | `OUTBOUND` |
| `warehouseId` | number | ❌ | 按仓库 ID 筛选 | `1001` |

#### 响应

**非 `R<T>` 包装**，直接返回文件流。

| Header | 值 |
|--------|----|
| `Content-Type` | `text/csv;charset=UTF-8` |
| `Content-Disposition` | `attachment; filename=records_<timestamp>.csv` |

CSV 列顺序：`id, recordNo, warehouseName, recordType, batchCode, variety, grade, quantity, temperature, humidity, operator, recordDate, traceCode, createdAt`

#### 响应示例（失败）

```json
{
  "code": 600900,
  "message": "导出参数非法",
  "data": null
}
```

---

## 5. 仓储统计 WarehouseStatisticsController（`/api/warehouse/statistics`）

### 5.1 概览统计

**URL**：`GET /api/warehouse/statistics/summary`
**认证**：需要 JWT Bearer
**权限**：`warehouse:statistics:read`
**错误码**：`600300`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无参数。

#### 响应体 `WarehouseSummaryResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `totalWarehouses` | number | ✅ | 仓库总数 |
| `totalCapacity` | number | ✅ | 总容量（吨），精度 2 位小数 |
| `totalUsed` | number | ✅ | 已用容量（吨），精度 2 位小数 |
| `utilizationRate` | number | ✅ | 综合利用率，0.00-1.00（保留 4 位小数） |
| `warehousesByType` | object | ✅ | 按类型分组统计：`{ "NORMAL": 3, "COLD": 5, "ATMOSPHERE": 2 }` |
| `warehousesByStatus` | object | ✅ | 按状态分组统计：`{ "ACTIVE": 8, "MAINTENANCE": 1, "FULL": 1, "CLOSED": 0 }` |

```typescript
export interface WarehouseSummaryResponse {
  totalWarehouses: number;
  totalCapacity: number;
  totalUsed: number;
  utilizationRate: number;
  warehousesByType: Record<string, number>;
  warehousesByStatus: Record<string, number>;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalWarehouses": 10,
    "totalCapacity": 3500.00,
    "totalUsed": 2100.50,
    "utilizationRate": 0.6001,
    "warehousesByType": {
      "NORMAL": 3,
      "COLD": 5,
      "ATMOSPHERE": 2
    },
    "warehousesByStatus": {
      "ACTIVE": 8,
      "MAINTENANCE": 1,
      "FULL": 1,
      "CLOSED": 0
    }
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 600300,
  "message": "统计数据为空，暂无仓库数据",
  "data": null
}
```

#### 业务规则

- `utilizationRate = totalUsed / totalCapacity`，totalCapacity 为 0 时返回 0
- `warehousesByType` / `warehousesByStatus` 的 key 为枚举 code（String），value 为数量

---

### 5.2 周转率统计

**URL**：`GET /api/warehouse/statistics/turnover`
**认证**：需要 JWT Bearer
**权限**：`warehouse:statistics:read`
**错误码**：`600001` / `600301` / `600302`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `warehouseId` | number | ❌ | 仓库 ID，不传则统计全部 | `1001` |
| `days` | number | ❌ | 统计天数，默认 30，范围 1-365 | `30` |

#### 响应体 `WarehouseTurnoverResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `warehouseId` | number \| null | ❌ | 仓库 ID，全局统计时为 null |
| `totalInbound` | number | ✅ | 统计期内总入库量（kg），精度 2 位小数 |
| `totalOutbound` | number | ✅ | 统计期内总出库量（kg），精度 2 位小数 |
| `turnoverRate` | number | ✅ | 周转率（次/周期），精度 4 位小数。公式：`totalOutbound / avgInventory` |
| `recordCount` | number | ✅ | 统计期内出入库记录总数 |

```typescript
export interface WarehouseTurnoverResponse {
  warehouseId: number | null;
  totalInbound: number;
  totalOutbound: number;
  turnoverRate: number;
  recordCount: number;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "warehouseId": 1001,
    "totalInbound": 12000.00,
    "totalOutbound": 8500.50,
    "turnoverRate": 2.1250,
    "recordCount": 36
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 600302,
  "message": "周转率计算失败：统计期内无有效记录",
  "data": null
}
```

#### 业务规则

- `days` 取值范围 1-365，超出范围抛 `600301`
- `days=0` 或负数抛 `600301`
- 若指定 `warehouseId` 不存在，抛 `600001`
- 若统计期内无记录，`turnoverRate=0`，`recordCount=0`（不抛错）
- 若计算过程异常（如分母为 0），抛 `600302`

---

### 5.3 损耗率统计

**URL**：`GET /api/warehouse/statistics/loss`
**认证**：需要 JWT Bearer
**权限**：`warehouse:statistics:read`
**错误码**：`600001` / `600301` / `600303`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `warehouseId` | number | ❌ | 仓库 ID，不传则统计全部 | `1001` |
| `days` | number | ❌ | 统计天数，默认 30，范围 1-365 | `30` |

#### 响应体 `WarehouseLossResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `totalInbound` | number | ✅ | 统计期内总入库量（kg），精度 2 位小数 |
| `totalOutbound` | number | ✅ | 统计期内总出库量（kg），精度 2 位小数 |
| `difference` | number | ✅ | 损耗量（kg）= totalInbound - totalOutbound，精度 2 位小数 |
| `lossRate` | number | ✅ | 损耗率（0.00-1.00），精度 4 位小数。公式：`difference / totalInbound` |

```typescript
export interface WarehouseLossResponse {
  totalInbound: number;
  totalOutbound: number;
  difference: number;
  lossRate: number;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalInbound": 12000.00,
    "totalOutbound": 11520.00,
    "difference": 480.00,
    "lossRate": 0.0400
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 600303,
  "message": "损耗率计算失败：统计期内无入库记录",
  "data": null
}
```

#### 业务规则

- `days` 取值范围 1-365，超出范围抛 `600301`
- 若指定 `warehouseId` 不存在，抛 `600001`
- `totalInbound = 0` 时，`lossRate = 0`，`difference = 0`（不抛错）
- 正常情况下 `difference ≥ 0`；若出现 `difference < 0`（出库量 > 入库量），应记录为异常并抛 `600303`

---

## 6. 错误码清单

| 错误码 | 常量名 | 中文说明 | 触发场景 |
|--------|--------|---------|---------|
| `600001` | `WAREHOUSE_NOT_FOUND` | 仓库不存在 | 按 ID 查询/更新/删除仓库时找不到 |
| `600002` | `WAREHOUSE_CODE_DUPLICATE` | 仓库编码重复 | 创建时自动生成的编码重复（极小概率） |
| `600003` | `WAREHOUSE_STATUS_INVALID` | 仓库状态流转非法 | 不在合法状态机路径中的变更 |
| `600004` | `WAREHOUSE_CAPACITY_EXCEEDED` | 仓库容量溢出 | 入库/创建仓单后 usedCapacity > capacity |
| `600005` | `WAREHOUSE_TYPE_INVALID` | 仓库类型非法 | 创建/更新时 type 不在枚举范围内 |
| `600006` | `WAREHOUSE_HAS_ACTIVE_RECORDS` | 仓库有活跃记录无法删除 | 删除仓库时仍有有效仓单或库存 > 0 |
| `600100` | `RECEIPT_NOT_FOUND` | 仓单不存在 | 按 ID 查询/更新/删除仓单时找不到 |
| `600101` | `RECEIPT_NO_DUPLICATE` | 仓单编号重复 | 创建时自动生成编号重复（极小概率） |
| `600102` | `RECEIPT_STATUS_INVALID` | 仓单状态流转非法 | 不在合法状态机路径中的变更，或 status code 非法 |
| `600103` | `RECEIPT_ALREADY_PLEDGED` | 仓单已质押，不能操作 | 更新/删除/转让 PLEDGED 状态仓单 |
| `600104` | `RECEIPT_EXPIRED` | 仓单已过期 | validUntil < 今日时尝试状态变更 |
| `600105` | `RECEIPT_QUANTITY_INVALID` | 存储数量非法 | 数量 ≤ 0 或 validUntil ≤ inboundDate |
| `600200` | `RECORD_NOT_FOUND` | 出入库记录不存在 | 按 ID 查询/删除记录时找不到 |
| `600201` | `RECORD_TYPE_INVALID` | 出入库类型非法 | recordType 不为 INBOUND/OUTBOUND |
| `600202` | `RECORD_QUANTITY_INSUFFICIENT` | 库存不足无法出库 | 出库量 > 仓库当前 usedCapacity |
| `600203` | `RECORD_WAREHOUSE_MISMATCH` | 仓库不匹配 | 出库记录的 warehouseId 与货物来源不一致 |
| `600300` | `STATISTICS_DATA_EMPTY` | 统计数据为空 | 系统内无任何仓库数据时调用概览统计 |
| `600301` | `STATISTICS_DAYS_INVALID` | 统计天数非法 | days ≤ 0 或 days > 365 |
| `600302` | `TURNOVER_CALC_FAILED` | 周转率计算失败 | 计算过程异常（如除零保护触发） |
| `600303` | `LOSS_CALC_FAILED` | 损耗率计算失败 | 出库量 > 入库量异常或计算过程异常 |
| `600900` | `WAREHOUSE_PARAM_INVALID` | 参数非法 | 通用参数校验失败（如 size > 100） |
| `600901` | `WAREHOUSE_UNAUTHORIZED` | 无仓储操作权限 | 用户缺少对应权限标识 |

---

## 7. 枚举集中声明

> 所有枚举在响应中以 `{ code, desc }` 形式返回；请求中仅传 `code` 值。

### 7.1 WarehouseType — 仓库类型

| code | desc | 说明 |
|------|------|------|
| `1` | `普通仓` | 常温储存，无特殊温控设备 |
| `2` | `冷库` | 低温储存，适合苹果冷链保鲜 |
| `3` | `气调库` | 气调储存，通过调节气体成分延长保鲜期 |

```typescript
export type WarehouseTypeCode = 1 | 2 | 3;

export const WarehouseType = {
  NORMAL: { code: 1, desc: '普通仓' },
  COLD:   { code: 2, desc: '冷库' },
  ATMOSPHERE: { code: 3, desc: '气调库' },
} as const;
```

> 实现注记：后端 `Warehouse.type` 为 `String`（存储 `"NORMAL"/"COLD"/"ATMOSPHERE"`），契约目标态序列化为 `{ code: number, desc: string }`。迁移清单见 §8.1。

### 7.2 WarehouseStatus — 仓库状态

| code | desc | 说明 |
|------|------|------|
| `1` | `正常` | 仓库正常运营 |
| `2` | `维护中` | 仓库进入定期维护，暂停收货 |
| `3` | `已满` | 仓库容量已达上限 |
| `4` | `已关闭` | 仓库永久关闭（终态） |

```typescript
export type WarehouseStatusCode = 1 | 2 | 3 | 4;

export const WarehouseStatus = {
  ACTIVE:      { code: 1, desc: '正常' },
  MAINTENANCE: { code: 2, desc: '维护中' },
  FULL:        { code: 3, desc: '已满' },
  CLOSED:      { code: 4, desc: '已关闭' },
} as const;
```

> 实现注记：后端 `Warehouse.status` 为 `String`（存储 `"ACTIVE"/"MAINTENANCE"/"FULL"/"CLOSED"`），需迁移。见 §8.1。

### 7.3 RecordType — 出入库类型

| code | desc | 说明 |
|------|------|------|
| `"INBOUND"` | `入库` | 货物进入仓库 |
| `"OUTBOUND"` | `出库` | 货物离开仓库 |

```typescript
export type RecordTypeCode = 'INBOUND' | 'OUTBOUND';

export const RecordType = {
  INBOUND:  { code: 'INBOUND',  desc: '入库' },
  OUTBOUND: { code: 'OUTBOUND', desc: '出库' },
} as const;
```

> 实现注记：后端 `WarehouseRecord.recordType` 为 `String`，响应需包装为 `{ code, desc }`。见 §8.1。

### 7.4 AppleGrade — 苹果质量等级

> 与 `planting.md` AppleGrade 枚举一致，复用同一定义。

| code | desc | 说明 |
|------|------|------|
| `"A"` | `特级` | 外观、重量、甜度均达顶级标准 |
| `"B"` | `一级` | 达到普通优质标准 |
| `"C"` | `二级` | 次等，仍可销售但价格较低 |

```typescript
export type AppleGradeCode = 'A' | 'B' | 'C';

export const AppleGrade = {
  A: { code: 'A', desc: '特级' },
  B: { code: 'B', desc: '一级' },
  C: { code: 'C', desc: '二级' },
} as const;
```

> 实现注记：后端 `WarehouseRecord.grade` / `WarehouseReceipt.grade` 均为 `String`，响应需包装为 `{ code, desc }`。见 §8.1。

### 7.5 ReceiptStatus — 仓单状态

| code | desc | 说明 |
|------|------|------|
| `"VALID"` | `有效` | 仓单有效，可正常使用 |
| `"PLEDGED"` | `质押中` | 仓单已质押给金融机构（关联 finance.md） |
| `"TRANSFERRED"` | `已转让` | 仓单所有权已转让（终态） |
| `"CANCELLED"` | `已注销` | 仓单已注销（终态） |

```typescript
export type ReceiptStatusCode = 'VALID' | 'PLEDGED' | 'TRANSFERRED' | 'CANCELLED';

export const ReceiptStatus = {
  VALID:       { code: 'VALID',       desc: '有效' },
  PLEDGED:     { code: 'PLEDGED',     desc: '质押中' },
  TRANSFERRED: { code: 'TRANSFERRED', desc: '已转让' },
  CANCELLED:   { code: 'CANCELLED',   desc: '已注销' },
} as const;
```

> 实现注记：后端 `WarehouseReceipt.status` 为 `String`，响应需包装为 `{ code, desc }`。见 §8.1。

---

## 8. 实现注记

### 8.1 String → EnumValue 迁移清单

以下字段当前后端以 `String` 存储和响应，契约目标态为 `{ code, desc }` EnumValue 对象。前端先按契约消费，后端待迁移。

| 实体 | 字段 | 当前类型 | 目标类型 | 枚举定义 |
|------|------|---------|---------|---------|
| `Warehouse` | `type` | `String`（`"NORMAL"/"COLD"/"ATMOSPHERE"`） | `EnumValue<number>` | WarehouseType §7.1 |
| `Warehouse` | `status` | `String`（`"ACTIVE"/"MAINTENANCE"/"FULL"/"CLOSED"`） | `EnumValue<number>` | WarehouseStatus §7.2 |
| `WarehouseRecord` | `recordType` | `String`（`"INBOUND"/"OUTBOUND"`） | `EnumValue<string>` | RecordType §7.3 |
| `WarehouseRecord` | `grade` | `String`（`"A"/"B"/"C"`） | `EnumValue<string>` | AppleGrade §7.4 |
| `WarehouseReceipt` | `grade` | `String`（`"A"/"B"/"C"`） | `EnumValue<string>` | AppleGrade §7.4 |
| `WarehouseReceipt` | `status` | `String`（`"VALID"/"PLEDGED"/"TRANSFERRED"/"CANCELLED"`） | `EnumValue<string>` | ReceiptStatus §7.5 |

**迁移策略**：在 Service 层或序列化层转换，Entity 可保持 String，但 Response DTO 需组装为 `{ code, desc }` 输出。

### 8.2 Entity → Request/Response DTO 重命名清单

当前部分接口直接接收/返回 Entity（Warehouse/WarehouseReceipt/WarehouseRecord），契约目标态为明确的 Request/Response DTO：

| 当前（Controller 接收） | 契约目标 Request | 契约目标 Response |
|------------------------|----------------|-----------------|
| `@RequestBody Warehouse` | `WarehouseCreateRequest` / `WarehouseUpdateRequest` | `WarehouseResponse` |
| `@RequestBody Map<String, String>` | `WarehouseStatusChangeRequest` | `WarehouseResponse` |
| `@RequestBody WarehouseReceipt` | `WarehouseReceiptCreateRequest` / `WarehouseReceiptUpdateRequest` | `WarehouseReceiptResponse` |
| `@RequestBody Map<String, String>` | `WarehouseReceiptStatusChangeRequest` | `WarehouseReceiptResponse` |
| `@RequestBody WarehouseRecord` | `WarehouseRecordCreateRequest` | `WarehouseRecordResponse` |
| `WarehouseStatisticsVO` | — | `WarehouseSummaryResponse` |
| `WarehouseStatisticsVO.TurnoverVO` | — | `WarehouseTurnoverResponse` |
| `WarehouseStatisticsVO.LossVO` | — | `WarehouseLossResponse` |

> **禁用**：`XxxDTO` / `XxxVO` / `XxxParam`（含已有 `WarehouseStatisticsVO`），迁移时统一改名为 `XxxResponse`。

### 8.3 分页参数迁移到 PageParam

当前后端 3 个列表接口使用 `int page, int size, String keyword` 等独立 Query 参数，契约目标态为 `PageParam`。

| 接口 | 当前参数 | 契约 PageParam 字段 | 额外筛选字段 |
|------|---------|-------------------|------------|
| `GET /warehouses/list` | `page, size, keyword, type, status` | `page, size, keyword` | `type, status`（扩展参数） |
| `GET /receipts/list` | `page, size, keyword, status` | `page, size, keyword` | `status`（扩展参数） |
| `GET /records/list` | `page, size, keyword, recordType, warehouseId` | `page, size, keyword` | `recordType, warehouseId`（扩展参数） |

> 迁移时可封装 `WarehousePageRequest extends PageParam` 增加扩展字段，向后兼容独立参数模式。

### 8.4 关联契约跨模块依赖

| 本模块字段 | 关联模块 | 关联说明 |
|-----------|---------|---------|
| `WarehouseReceipt.batchCode` | `planting.md` | 种植/采收批次编码，harvest_batch 的 batchCode |
| `WarehouseReceipt.traceCode` | `trace.md` | 苹果溯源码，trace 模块生成 |
| `WarehouseRecord.batchCode` | `planting.md` | 出入库操作关联的种植批次 |
| `WarehouseRecord.traceCode` | `trace.md` | 出入库操作关联的溯源码 |
| `WarehouseReceipt status=PLEDGED` | `finance.md` | 仓单质押状态变更由 finance.md 融资申请触发 |
| `Warehouse.temperature/humidity` | `coldchain.md` / `iot.md` | 温湿度数据由冷链/IoT 传感器实时写入 |

---

## 质量 Checklist

- [x] 23 个端点全覆盖（§1 索引表已列出，§2-§5 各有完整规范）
- [x] ≥ 22 个错误码，全在 600000-699999（§6 列出 22 条：600001-600006, 600100-600105, 600200-600203, 600300-600303, 600900-600901）
- [x] 5 个枚举声明完整（§7：WarehouseType / WarehouseStatus / RecordType / AppleGrade / ReceiptStatus）
- [x] JSON 示例全用 `message` 字段（无 `msg` 字段）
- [x] 分页接口使用 PageParam + PageResult<T>（§2.1 / §3.1 / §4.1 均含分页结构）
- [x] 时间用 LocalDate（仓单 inboundDate/validUntil，记录 recordDate）/ LocalDateTime（时间戳 createdAt/updatedAt/statusChangeTime）格式正确
- [x] 3 个 CSV 导出明确非 R<T>（§2.8 / §3.7 / §4.5 均标注"非 R<T> 包装"）
- [x] 仓库容量校验规则已入文档（§2.3 创建仓库/§4.3 创建记录 业务规则）
- [x] 仓单质押/转让/过期规则已入文档（§3.5 业务规则状态机完整描述）
- [x] 出库库存检查规则已入文档（§4.3 业务规则：出库前检查 usedCapacity）
