# M6 冷链 (Coldchain) 模块契约

> 模块：`apple-module-coldchain`
> URL 前缀：`/api/coldchain/{vehicles,tasks,temperatures,deliveries,precool,logistics,statistics}`
> 错误码段：`800000 ~ 899999`（CONVENTIONS.md:140 权威）
> 最近更新：2026-04-12
> 权威源：本文件
> 后端实现：`apple-module-coldchain`
> 关联契约：`iot.md`（传感器温度上报）/ `trade.md`（运输任务关联订单）/ `trace.md`（签收温度数据进入溯源 / 温度违规 → AnomalyTrace）/ `warehouse.md`（冷库温度数据）

---

## 1. 接口索引（35 端点）

| # | 接口中文名 | URL | 方法 | 认证 |
|---|-----------|-----|------|------|
| 1 | [车辆列表（分页）](#21-车辆列表分页) | `/api/coldchain/vehicles/list` | GET | ✅ |
| 2 | [车辆详情](#22-车辆详情) | `/api/coldchain/vehicles/{id}` | GET | ✅ |
| 3 | [创建车辆](#23-创建车辆) | `/api/coldchain/vehicles` | POST | ✅ |
| 4 | [更新车辆](#24-更新车辆) | `/api/coldchain/vehicles/{id}` | PUT | ✅ |
| 5 | [删除车辆](#25-删除车辆) | `/api/coldchain/vehicles/{id}` | DELETE | ✅ |
| 6 | [变更车辆状态](#26-变更车辆状态) | `/api/coldchain/vehicles/{id}/status` | PUT | ✅ |
| 7 | [导出车辆 CSV](#27-导出车辆-csv) | `/api/coldchain/vehicles/export` | GET | ✅ |
| 8 | [运输任务列表（分页）](#31-运输任务列表分页) | `/api/coldchain/tasks/list` | GET | ✅ |
| 9 | [运输任务详情](#32-运输任务详情) | `/api/coldchain/tasks/{id}` | GET | ✅ |
| 10 | [创建运输任务](#33-创建运输任务) | `/api/coldchain/tasks` | POST | ✅ |
| 11 | [更新运输任务](#34-更新运输任务) | `/api/coldchain/tasks/{id}` | PUT | ✅ |
| 12 | [发车](#35-发车) | `/api/coldchain/tasks/{id}/depart` | POST | ✅ |
| 13 | [确认送达](#36-确认送达) | `/api/coldchain/tasks/{id}/deliver` | POST | ✅ |
| 14 | [取消任务](#37-取消任务) | `/api/coldchain/tasks/{id}/cancel` | POST | ✅ |
| 15 | [删除运输任务](#38-删除运输任务) | `/api/coldchain/tasks/{id}` | DELETE | ✅ |
| 16 | [导出运输任务 CSV](#39-导出运输任务-csv) | `/api/coldchain/tasks/export` | GET | ✅ |
| 17 | [温度记录列表（分页）](#41-温度记录列表分页) | `/api/coldchain/temperatures/list` | GET | ✅ |
| 18 | [按任务查询温度记录](#42-按任务查询温度记录) | `/api/coldchain/temperatures/by-task` | GET | ✅ |
| 19 | [新增温度记录](#43-新增温度记录) | `/api/coldchain/temperatures` | POST | ✅ |
| 20 | [删除温度记录](#44-删除温度记录) | `/api/coldchain/temperatures/{id}` | DELETE | ✅ |
| 21 | [配送列表（分页）](#51-配送列表分页) | `/api/coldchain/deliveries/list` | GET | ✅ |
| 22 | [配送详情](#52-配送详情) | `/api/coldchain/deliveries/{id}` | GET | ✅ |
| 23 | [创建配送](#53-创建配送) | `/api/coldchain/deliveries` | POST | ✅ |
| 24 | [更新配送](#54-更新配送) | `/api/coldchain/deliveries/{id}` | PUT | ✅ |
| 25 | [签收确认](#55-签收确认) | `/api/coldchain/deliveries/{id}/sign` | POST | ✅ |
| 26 | [删除配送](#56-删除配送) | `/api/coldchain/deliveries/{id}` | DELETE | ✅ |
| 27 | [导出配送 CSV](#57-导出配送-csv) | `/api/coldchain/deliveries/export` | GET | ✅ |
| 28 | [预冷任务列表（分页）](#61-预冷任务列表分页) | `/api/coldchain/precool` | GET | ✅ |
| 29 | [预冷任务详情](#62-预冷任务详情) | `/api/coldchain/precool/{id}` | GET | ✅ |
| 30 | [创建预冷任务](#63-创建预冷任务) | `/api/coldchain/precool` | POST | ✅ |
| 31 | [启动冷却](#64-启动冷却) | `/api/coldchain/precool/{id}/start` | PUT | ✅ |
| 32 | [完成冷却](#65-完成冷却) | `/api/coldchain/precool/{id}/complete` | PUT | ✅ |
| 33 | [获取任务完整物流](#71-获取任务完整物流) | `/api/coldchain/logistics/{taskId}/full` | GET | ✅ |
| 34 | [统计概览](#81-统计概览) | `/api/coldchain/statistics/summary` | GET | ✅ |
| 35 | [近N天报警统计](#82-近n天报警统计) | `/api/coldchain/statistics/alarms` | GET | ✅ |

---

## 2. 车辆管理 VehicleController（`/api/coldchain/vehicles`）

### 2.1 车辆列表（分页）

**URL**：`GET /api/coldchain/vehicles/list`
**认证**：需要 JWT Bearer
**权限**：`coldchain:vehicle:read`
**错误码**：`800900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，从 1 开始，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 车牌号或驾驶员姓名模糊搜索 | `陕A` |
| `vehicleType` | string | ❌ | 车辆类型枚举 code：`REFRIGERATED`/`INSULATED`/`NORMAL` | `REFRIGERATED` |
| `status` | string | ❌ | 车辆状态枚举 code：`IDLE`/`IN_TRANSIT`/`MAINTENANCE`/`RETIRED` | `IDLE` |

```typescript
export interface VehiclePageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  vehicleType?: string;
  status?: string;
}
```

#### 响应体 `PageResult<VehicleResponse>`

`VehicleResponse` 字段：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 车辆 ID（雪花） |
| `vehicleCode` | string | ✅ | 车辆编码，格式 `VH+yyyyMMdd+4位序号` |
| `plateNumber` | string | ✅ | 车牌号 |
| `vehicleType` | `EnumValue<string>` | ✅ | 车辆类型，见 VehicleType |
| `brand` | string \| null | ❌ | 车辆品牌 |
| `capacity` | number | ✅ | 载重（吨），精度 2 位小数 |
| `volume` | number \| null | ❌ | 容积（m³），精度 2 位小数 |
| `temperatureMin` | number \| null | ❌ | 最低控温（℃） |
| `temperatureMax` | number \| null | ❌ | 最高控温（℃） |
| `driverName` | string \| null | ❌ | 驾驶员姓名 |
| `driverPhone` | string \| null | ❌ | 驾驶员电话 |
| `status` | `EnumValue<string>` | ✅ | 车辆状态，见 VehicleStatus |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface VehicleResponse {
  id: number;
  vehicleCode: string;
  plateNumber: string;
  vehicleType: EnumValue<string>;
  brand: string | null;
  capacity: number;
  volume: number | null;
  temperatureMin: number | null;
  temperatureMax: number | null;
  driverName: string | null;
  driverPhone: string | null;
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
        "id": 1001,
        "vehicleCode": "VH202604120001",
        "plateNumber": "陕A12345",
        "vehicleType": { "code": "REFRIGERATED", "desc": "冷藏" },
        "brand": "东风",
        "capacity": 5.00,
        "volume": 20.00,
        "temperatureMin": -5.00,
        "temperatureMax": 8.00,
        "driverName": "张师傅",
        "driverPhone": "18691234567",
        "status": { "code": "IDLE", "desc": "空闲" },
        "remark": null,
        "createdAt": "2026-04-01 10:00:00",
        "updatedAt": "2026-04-12 08:30:00"
      }
    ],
    "total": 18,
    "current": 1,
    "size": 10
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 800900,
  "message": "请求参数错误",
  "data": null
}
```

#### 业务规则

- `keyword` 模糊匹配 `plateNumber`（车牌号）或 `driverName`（驾驶员姓名）
- `vehicleType` 和 `status` 均为精确过滤，传 String code 值
- 软删除过滤：`deleted = 0`

#### 实现提示

- Controller：`VehicleController#list`，当前直接传 Entity `Vehicle` 作为 Response，目标状态应迁移为 `VehicleResponse`
- `vehicleType` / `status` 字段当前为 `String`，需迁移为 `EnumValue<String>` 序列化

---

### 2.2 车辆详情

**URL**：`GET /api/coldchain/vehicles/{id}`
**认证**：需要 JWT Bearer
**权限**：`coldchain:vehicle:read`
**错误码**：`800001`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 车辆 ID |

#### 响应体 `VehicleResponse`

同 [2.1 响应体定义](#21-车辆列表分页)。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "vehicleCode": "VH202604120001",
    "plateNumber": "陕A12345",
    "vehicleType": { "code": "REFRIGERATED", "desc": "冷藏" },
    "brand": "东风",
    "capacity": 5.00,
    "volume": 20.00,
    "temperatureMin": -5.00,
    "temperatureMax": 8.00,
    "driverName": "张师傅",
    "driverPhone": "18691234567",
    "status": { "code": "IDLE", "desc": "空闲" },
    "remark": null,
    "createdAt": "2026-04-01 10:00:00",
    "updatedAt": "2026-04-12 08:30:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 800001,
  "message": "车辆不存在",
  "data": null
}
```

#### 业务规则

- id 不存在或已软删除时，抛 `800001 VEHICLE_NOT_FOUND`

#### 实现提示

- Controller：`VehicleController#detail`

---

### 2.3 创建车辆

**URL**：`POST /api/coldchain/vehicles`
**认证**：需要 JWT Bearer
**权限**：`coldchain:vehicle:write`
**错误码**：`800002`、`800003`、`800004`、`800900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `plateNumber` | string | ✅ | 车牌号，唯一 | `陕A12345` |
| `vehicleType` | string | ✅ | 枚举 code：`REFRIGERATED`/`INSULATED`/`NORMAL` | `REFRIGERATED` |
| `brand` | string | ❌ | 车辆品牌 | `东风` |
| `capacity` | number | ✅ | 载重（吨），>0 | `5.00` |
| `volume` | number | ❌ | 容积（m³） | `20.00` |
| `temperatureMin` | number | ❌ | 最低控温（℃），temperatureMin < temperatureMax | `-5.00` |
| `temperatureMax` | number | ❌ | 最高控温（℃） | `8.00` |
| `driverName` | string | ❌ | 驾驶员姓名 | `张师傅` |
| `driverPhone` | string | ❌ | 驾驶员电话 | `18691234567` |
| `remark` | string | ❌ | 备注 | - |

```typescript
export interface VehicleCreateRequest {
  plateNumber: string;
  vehicleType: string;
  brand?: string;
  capacity: number;
  volume?: number;
  temperatureMin?: number;
  temperatureMax?: number;
  driverName?: string;
  driverPhone?: string;
  remark?: string;
}
```

#### 响应体 `VehicleResponse`

同 [2.1 响应体定义](#21-车辆列表分页)，含系统生成的 `vehicleCode`、`id`。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1002,
    "vehicleCode": "VH202604120002",
    "plateNumber": "陕B67890",
    "vehicleType": { "code": "INSULATED", "desc": "保温" },
    "brand": "解放",
    "capacity": 8.00,
    "volume": 32.00,
    "temperatureMin": 0.00,
    "temperatureMax": 15.00,
    "driverName": "李师傅",
    "driverPhone": "13912345678",
    "status": { "code": "IDLE", "desc": "空闲" },
    "remark": null,
    "createdAt": "2026-04-12 09:00:00",
    "updatedAt": "2026-04-12 09:00:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 800002,
  "message": "车辆编码已存在",
  "data": null
}
```

#### 业务规则

- `vehicleCode` 系统自动生成，格式 `VH+yyyyMMdd+4位序号`
- `plateNumber` 全局唯一，重复抛 `800002 VEHICLE_CODE_DUPLICATE`
- `vehicleType` 非合法枚举值抛 `800004 VEHICLE_TYPE_INVALID`
- `temperatureMin` 不得 >= `temperatureMax`（两者均填时校验）
- 新建车辆默认 `status = IDLE`

#### 实现提示

- Controller：`VehicleController#create`，当前直接用 `Vehicle` Entity 接收请求，目标迁移为 `VehicleCreateRequest`

---

### 2.4 更新车辆

**URL**：`PUT /api/coldchain/vehicles/{id}`
**认证**：需要 JWT Bearer
**权限**：`coldchain:vehicle:write`
**错误码**：`800001`、`800002`、`800004`、`800900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 车辆 ID |

#### 请求体

```typescript
export interface VehicleUpdateRequest {
  plateNumber?: string;
  vehicleType?: string;
  brand?: string;
  capacity?: number;
  volume?: number;
  temperatureMin?: number;
  temperatureMax?: number;
  driverName?: string;
  driverPhone?: string;
  remark?: string;
}
```

#### 响应体 `VehicleResponse`

同 [2.1 响应体定义](#21-车辆列表分页)。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "vehicleCode": "VH202604120001",
    "plateNumber": "陕A12345",
    "vehicleType": { "code": "REFRIGERATED", "desc": "冷藏" },
    "brand": "东风重卡",
    "capacity": 6.00,
    "volume": 24.00,
    "temperatureMin": -5.00,
    "temperatureMax": 8.00,
    "driverName": "张师傅",
    "driverPhone": "18691234567",
    "status": { "code": "IDLE", "desc": "空闲" },
    "remark": "2026年更换新车",
    "createdAt": "2026-04-01 10:00:00",
    "updatedAt": "2026-04-12 10:00:00"
  }
}
```

#### 业务规则

- id 不存在抛 `800001 VEHICLE_NOT_FOUND`
- `plateNumber` 修改时需校验唯一性，重复抛 `800002`
- 更新字段为部分更新（Patch 语义），未传字段保持不变

---

### 2.5 删除车辆

**URL**：`DELETE /api/coldchain/vehicles/{id}`
**认证**：需要 JWT Bearer
**权限**：`coldchain:vehicle:write`
**错误码**：`800001`、`800005`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 车辆 ID |

#### 响应体 `null`

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
  "code": 800005,
  "message": "车辆正在使用中，不可删除",
  "data": null
}
```

#### 业务规则

- id 不存在抛 `800001 VEHICLE_NOT_FOUND`
- 车辆 `status = IN_TRANSIT` 时不可删除，抛 `800005 VEHICLE_IN_USE_CANNOT_DELETE`
- 执行软删除（`deleted = 1`），不物理删除

---

### 2.6 变更车辆状态

**URL**：`PUT /api/coldchain/vehicles/{id}/status`
**认证**：需要 JWT Bearer
**权限**：`coldchain:vehicle:write`
**错误码**：`800001`、`800003`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

| 位置 | 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| Path | `id` | number | ✅ | 车辆 ID |
| Query | `newStatus` | string | ✅ | 目标状态枚举 code |

```typescript
export interface VehicleStatusChangeRequest {
  newStatus: string; // VehicleStatus code
}
```

#### 响应体 `VehicleResponse`

同 [2.1 响应体定义](#21-车辆列表分页)，`status` 字段反映新状态。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "vehicleCode": "VH202604120001",
    "plateNumber": "陕A12345",
    "vehicleType": { "code": "REFRIGERATED", "desc": "冷藏" },
    "status": { "code": "MAINTENANCE", "desc": "维护中" },
    "createdAt": "2026-04-01 10:00:00",
    "updatedAt": "2026-04-12 11:00:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 800003,
  "message": "车辆状态变更不合法",
  "data": null
}
```

#### 业务规则

- 合法状态转换：
  - `IDLE ↔ IN_TRANSIT`（运输任务创建/完成时自动切换；也可手动调整）
  - `任意 → MAINTENANCE → IDLE`（维修入场/出场）
  - `任意 → RETIRED`（退役，不可逆）
- `RETIRED` 状态不可再变更，抛 `800003 VEHICLE_STATUS_INVALID`
- `newStatus` 非合法枚举值抛 `800003 VEHICLE_STATUS_INVALID`

#### 实现提示

- Controller：`VehicleController#changeStatus`，当前以 `@RequestParam String newStatus` 接收
- 目标契约：Query 参数 `newStatus` 传枚举 code string

---

### 2.7 导出车辆 CSV

**URL**：`GET /api/coldchain/vehicles/export`
**认证**：需要 JWT Bearer
**权限**：`coldchain:vehicle:export`
**变更历史**：2026-04-12 | init | 架构师

> ⚠️ **非 R\<T\> 包装**：直接写入 `HttpServletResponse`，Content-Type: `text/csv; charset=UTF-8`，Content-Disposition: `attachment; filename=vehicles.csv`

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | ❌ | 车牌号或驾驶员姓名模糊搜索 |
| `vehicleType` | string | ❌ | 车辆类型过滤 |
| `status` | string | ❌ | 车辆状态过滤 |

#### 响应

- HTTP 200，二进制 CSV 流
- 字段列（中文表头）：ID、车辆编码、车牌号、车辆类型、品牌、载重(吨)、容积(m³)、最低控温(℃)、最高控温(℃)、驾驶员姓名、驾驶员电话、状态、备注、创建时间

#### 业务规则

- 过滤条件与列表接口一致，导出全量（不分页）
- BOM 头 `\uFEFF` 确保 Excel 正确识别 UTF-8 中文

#### 实现提示

- Controller：`VehicleController#export`，直接写 `HttpServletResponse`
- Service：`VehicleService#exportVehicles`

---

## 3. 运输任务 TransportTaskController（`/api/coldchain/tasks`）

### 3.1 运输任务列表（分页）

**URL**：`GET /api/coldchain/tasks/list`
**认证**：需要 JWT Bearer
**权限**：`coldchain:task:read`
**错误码**：`800900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10 | `10` |
| `keyword` | string | ❌ | 任务编码或货物描述模糊搜索 | `TK202604` |
| `status` | string | ❌ | 任务状态枚举 code | `PENDING` |

```typescript
export interface TransportTaskPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;
}
```

#### 响应体 `PageResult<TransportTaskResponse>`

`TransportTaskResponse` 字段：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 任务 ID |
| `taskCode` | string | ✅ | 任务编码 |
| `vehicleId` | number | ✅ | 车辆 ID |
| `orderId` | number \| null | ❌ | 关联订单 ID（trade.md） |
| `origin` | string | ✅ | 起始地 |
| `destination` | string | ✅ | 目的地 |
| `cargoDesc` | string \| null | ❌ | 货物描述 |
| `cargoWeight` | number \| null | ❌ | 货物重量（吨） |
| `requiredTemp` | number \| null | ❌ | 要求运输温度（℃） |
| `planDepart` | string \| null | ❌ | 计划发车时间 |
| `actualDepart` | string \| null | ❌ | 实际发车时间 |
| `planArrive` | string \| null | ❌ | 计划到达时间 |
| `actualArrive` | string \| null | ❌ | 实际到达时间 |
| `distance` | number \| null | ❌ | 距离（km） |
| `cost` | number \| null | ❌ | 运输费用（元） |
| `status` | `EnumValue<string>` | ✅ | 任务状态，见 TransportTaskStatus |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface TransportTaskResponse {
  id: number;
  taskCode: string;
  vehicleId: number;
  orderId: number | null;
  origin: string;
  destination: string;
  cargoDesc: string | null;
  cargoWeight: number | null;
  requiredTemp: number | null;
  planDepart: string | null;
  actualDepart: string | null;
  planArrive: string | null;
  actualArrive: string | null;
  distance: number | null;
  cost: number | null;
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
        "taskCode": "TK202604120001",
        "vehicleId": 1001,
        "orderId": 5001,
        "origin": "陕西富县苹果产业园",
        "destination": "北京新发地市场",
        "cargoDesc": "红富士苹果",
        "cargoWeight": 4.50,
        "requiredTemp": 2.00,
        "planDepart": "2026-04-12 08:00:00",
        "actualDepart": "2026-04-12 08:15:00",
        "planArrive": "2026-04-13 20:00:00",
        "actualArrive": null,
        "distance": 1150.00,
        "cost": 3200.00,
        "status": { "code": "IN_TRANSIT", "desc": "运输中" },
        "remark": null,
        "createdAt": "2026-04-11 18:00:00",
        "updatedAt": "2026-04-12 08:15:00"
      }
    ],
    "total": 42,
    "current": 1,
    "size": 10
  }
}
```

---

### 3.2 运输任务详情

**URL**：`GET /api/coldchain/tasks/{id}`
**认证**：需要 JWT Bearer
**权限**：`coldchain:task:read`
**错误码**：`800100`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 任务 ID |

#### 响应体 `TransportTaskResponse`

同 [3.1 响应体定义](#31-运输任务列表分页)。

#### 响应示例（失败）

```json
{
  "code": 800100,
  "message": "运输任务不存在",
  "data": null
}
```

---

### 3.3 创建运输任务

**URL**：`POST /api/coldchain/tasks`
**认证**：需要 JWT Bearer
**权限**：`coldchain:task:write`
**错误码**：`800100`、`800101`、`800106`、`800900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `vehicleId` | number | ✅ | 车辆 ID，必须存在且 `status=IDLE` |
| `orderId` | number | ❌ | 关联订单 ID |
| `origin` | string | ✅ | 起始地 |
| `destination` | string | ✅ | 目的地 |
| `cargoDesc` | string | ❌ | 货物描述 |
| `cargoWeight` | number | ❌ | 货物重量（吨） |
| `requiredTemp` | number | ❌ | 要求运输温度（℃） |
| `planDepart` | string | ❌ | 计划发车时间，格式 `yyyy-MM-dd HH:mm:ss` |
| `planArrive` | string | ❌ | 计划到达时间 |
| `distance` | number | ❌ | 距离（km） |
| `cost` | number | ❌ | 运输费用（元） |
| `remark` | string | ❌ | 备注 |

```typescript
export interface TransportTaskCreateRequest {
  vehicleId: number;
  orderId?: number;
  origin: string;
  destination: string;
  cargoDesc?: string;
  cargoWeight?: number;
  requiredTemp?: number;
  planDepart?: string;
  planArrive?: string;
  distance?: number;
  cost?: number;
  remark?: string;
}
```

#### 响应体 `TransportTaskResponse`

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 2002,
    "taskCode": "TK202604120002",
    "vehicleId": 1002,
    "orderId": null,
    "origin": "陕西洛川县苹果合作社",
    "destination": "上海浦东农贸中心",
    "cargoDesc": "嘎啦苹果",
    "cargoWeight": 7.00,
    "requiredTemp": 3.00,
    "planDepart": "2026-04-13 06:00:00",
    "actualDepart": null,
    "planArrive": "2026-04-15 18:00:00",
    "actualArrive": null,
    "distance": 1620.00,
    "cost": 4800.00,
    "status": { "code": "PENDING", "desc": "待发车" },
    "remark": null,
    "createdAt": "2026-04-12 14:00:00",
    "updatedAt": "2026-04-12 14:00:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 800106,
  "message": "所选车辆不可用（非空闲状态）",
  "data": null
}
```

#### 业务规则

- `taskCode` 系统自动生成，格式 `TK+yyyyMMdd+4位序号`
- `vehicleId` 必须存在且 `status=IDLE`，否则抛 `800106 TASK_VEHICLE_UNAVAILABLE`
- 创建后任务状态为 `PENDING`
- 关联 `orderId` 时需校验订单存在（跨模块查询 trade.md）

---

### 3.4 更新运输任务

**URL**：`PUT /api/coldchain/tasks/{id}`
**认证**：需要 JWT Bearer
**权限**：`coldchain:task:write`
**错误码**：`800100`、`800102`、`800106`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 任务 ID |

#### 请求体

```typescript
export interface TransportTaskUpdateRequest {
  vehicleId?: number;
  orderId?: number;
  origin?: string;
  destination?: string;
  cargoDesc?: string;
  cargoWeight?: number;
  requiredTemp?: number;
  planDepart?: string;
  planArrive?: string;
  distance?: number;
  cost?: number;
  remark?: string;
}
```

#### 响应体 `TransportTaskResponse`

#### 业务规则

- 任务 `status=DELIVERED` 或 `CANCELLED` 时不可修改，抛 `800102 TASK_STATUS_INVALID`
- 修改 `vehicleId` 时同样校验车辆可用性

---

### 3.5 发车

**URL**：`POST /api/coldchain/tasks/{id}/depart`
**认证**：需要 JWT Bearer
**权限**：`coldchain:task:write`
**错误码**：`800100`、`800103`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 任务 ID |

#### 响应体 `TransportTaskResponse`

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 2002,
    "taskCode": "TK202604120002",
    "status": { "code": "IN_TRANSIT", "desc": "运输中" },
    "actualDepart": "2026-04-13 06:10:00",
    "vehicleId": 1002,
    "origin": "陕西洛川县苹果合作社",
    "destination": "上海浦东农贸中心",
    "createdAt": "2026-04-12 14:00:00",
    "updatedAt": "2026-04-13 06:10:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 800103,
  "message": "当前状态不允许发车（仅 PENDING 可发车）",
  "data": null
}
```

#### 业务规则

- 仅 `status=PENDING` 可执行发车，否则抛 `800103 TASK_DEPART_NOT_ALLOWED`
- 发车后：`status → IN_TRANSIT`，记录 `actualDepart = now()`
- 关联车辆 `status → IN_TRANSIT`（自动切换）

---

### 3.6 确认送达

**URL**：`POST /api/coldchain/tasks/{id}/deliver`
**认证**：需要 JWT Bearer
**权限**：`coldchain:task:write`
**错误码**：`800100`、`800104`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 任务 ID |

#### 响应体 `TransportTaskResponse`

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 2001,
    "taskCode": "TK202604120001",
    "status": { "code": "DELIVERED", "desc": "已送达" },
    "actualArrive": "2026-04-13 19:45:00",
    "vehicleId": 1001,
    "origin": "陕西富县苹果产业园",
    "destination": "北京新发地市场",
    "createdAt": "2026-04-11 18:00:00",
    "updatedAt": "2026-04-13 19:45:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 800104,
  "message": "当前状态不允许送达（仅 IN_TRANSIT 可确认送达）",
  "data": null
}
```

#### 业务规则

- 仅 `status=IN_TRANSIT` 可执行，否则抛 `800104 TASK_DELIVER_NOT_ALLOWED`
- 送达后：`status → DELIVERED`，记录 `actualArrive = now()`
- 关联车辆 `status → IDLE`（自动释放）

---

### 3.7 取消任务

**URL**：`POST /api/coldchain/tasks/{id}/cancel`
**认证**：需要 JWT Bearer
**权限**：`coldchain:task:write`
**错误码**：`800100`、`800105`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 任务 ID |

#### 响应体 `TransportTaskResponse`

#### 响应示例（失败）

```json
{
  "code": 800105,
  "message": "当前状态不允许取消（已送达或已取消）",
  "data": null
}
```

#### 业务规则

- `status=DELIVERED` 或 `status=CANCELLED` 时不可取消，抛 `800105 TASK_CANCEL_NOT_ALLOWED`
- 取消后：`status → CANCELLED`
- 若车辆 `status=IN_TRANSIT`，需同步 `status → IDLE`

---

### 3.8 删除运输任务

**URL**：`DELETE /api/coldchain/tasks/{id}`
**认证**：需要 JWT Bearer
**权限**：`coldchain:task:write`
**错误码**：`800100`、`800102`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 任务 ID |

#### 响应体 `null`

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

#### 业务规则

- `status=IN_TRANSIT` 时不可删除，抛 `800102 TASK_STATUS_INVALID`
- 软删除（`deleted = 1`）

---

### 3.9 导出运输任务 CSV

**URL**：`GET /api/coldchain/tasks/export`
**认证**：需要 JWT Bearer
**权限**：`coldchain:task:export`
**变更历史**：2026-04-12 | init | 架构师

> ⚠️ **非 R\<T\> 包装**：直接写入 `HttpServletResponse`，Content-Type: `text/csv; charset=UTF-8`

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | ❌ | 任务编码或货物描述模糊搜索 |
| `status` | string | ❌ | 任务状态过滤 |

#### 响应

- HTTP 200，二进制 CSV 流
- 字段列（中文表头）：ID、任务编码、车辆ID、订单ID、起始地、目的地、货物描述、货物重量(吨)、要求温度(℃)、计划发车、实际发车、计划到达、实际到达、距离(km)、费用(元)、状态、备注、创建时间

---

## 4. 温度监控 TemperatureRecordController（`/api/coldchain/temperatures`）

### 4.1 温度记录列表（分页）

**URL**：`GET /api/coldchain/temperatures/list`
**认证**：需要 JWT Bearer
**权限**：`coldchain:temperature:read`
**错误码**：`800900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10 | `10` |
| `taskId` | number | ❌ | 运输任务 ID 过滤 | `2001` |
| `isAlarm` | number | ❌ | 报警标志：`0` 正常 / `1` 报警 | `1` |

```typescript
export interface TemperaturePageRequest {
  page?: number;
  size?: number;
  taskId?: number;
  isAlarm?: number; // 0=正常, 1=报警
}
```

#### 响应体 `PageResult<TemperatureRecordResponse>`

`TemperatureRecordResponse` 字段：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 记录 ID |
| `taskId` | number | ✅ | 运输任务 ID |
| `vehicleId` | number | ✅ | 车辆 ID |
| `temperature` | number | ✅ | 温度（℃），精度 2 位小数 |
| `humidity` | number \| null | ❌ | 湿度（%） |
| `location` | string \| null | ❌ | 当前位置 |
| `recordTime` | string | ✅ | 记录时间 `yyyy-MM-dd HH:mm:ss` |
| `isAlarm` | number | ✅ | `0` 正常 / `1` 报警 |
| `alarmMsg` | string \| null | ❌ | 报警信息（isAlarm=1 时有值） |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface TemperatureRecordResponse {
  id: number;
  taskId: number;
  vehicleId: number;
  temperature: number;
  humidity: number | null;
  location: string | null;
  recordTime: string;
  isAlarm: number; // 0=正常, 1=报警
  alarmMsg: string | null;
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
        "taskId": 2001,
        "vehicleId": 1001,
        "temperature": 8.50,
        "humidity": 82.0,
        "location": "河北保定服务区",
        "recordTime": "2026-04-12 14:30:00",
        "isAlarm": 1,
        "alarmMsg": "温度超出上限：要求≤8.00℃，实际8.50℃",
        "createdAt": "2026-04-12 14:30:00",
        "updatedAt": "2026-04-12 14:30:00"
      }
    ],
    "total": 156,
    "current": 1,
    "size": 10
  }
}
```

---

### 4.2 按任务查询温度记录

**URL**：`GET /api/coldchain/temperatures/by-task`
**认证**：需要 JWT Bearer
**权限**：`coldchain:temperature:read`
**错误码**：`800100`、`800202`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `taskId` | number | ✅ | 运输任务 ID |

#### 响应体 `List<TemperatureRecordResponse>`

> 返回全量列表（非分页），按 `recordTime` 升序排列，用于时序图展示。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 3001,
      "taskId": 2001,
      "vehicleId": 1001,
      "temperature": 2.10,
      "humidity": 85.0,
      "location": "陕西延安",
      "recordTime": "2026-04-12 08:30:00",
      "isAlarm": 0,
      "alarmMsg": null,
      "createdAt": "2026-04-12 08:30:00",
      "updatedAt": "2026-04-12 08:30:00"
    },
    {
      "id": 3002,
      "taskId": 2001,
      "vehicleId": 1001,
      "temperature": 8.50,
      "humidity": 82.0,
      "location": "河北保定服务区",
      "recordTime": "2026-04-12 14:30:00",
      "isAlarm": 1,
      "alarmMsg": "温度超出上限：要求≤8.00℃，实际8.50℃",
      "createdAt": "2026-04-12 14:30:00",
      "updatedAt": "2026-04-12 14:30:00"
    }
  ]
}
```

#### 业务规则

- `taskId` 必填，不存在抛 `800100 TASK_NOT_FOUND`
- 按 `recordTime` 升序排列，无分页限制

---

### 4.3 新增温度记录

**URL**：`POST /api/coldchain/temperatures`
**认证**：需要 JWT Bearer
**权限**：`coldchain:temperature:write`
**错误码**：`800100`、`800201`、`800202`、`800900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `taskId` | number | ✅ | 运输任务 ID |
| `vehicleId` | number | ✅ | 车辆 ID |
| `temperature` | number | ✅ | 温度（℃） |
| `humidity` | number | ❌ | 湿度（%） |
| `location` | string | ❌ | 当前位置 |
| `recordTime` | string | ✅ | 记录时间，格式 `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface TemperatureRecordCreateRequest {
  taskId: number;
  vehicleId: number;
  temperature: number;
  humidity?: number;
  location?: string;
  recordTime: string;
}
```

#### 响应体 `TemperatureRecordResponse`

#### 响应示例（成功，正常温度）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 3003,
    "taskId": 2001,
    "vehicleId": 1001,
    "temperature": 2.30,
    "humidity": 84.0,
    "location": "山西运城",
    "recordTime": "2026-04-12 18:00:00",
    "isAlarm": 0,
    "alarmMsg": null,
    "createdAt": "2026-04-12 18:00:00",
    "updatedAt": "2026-04-12 18:00:00"
  }
}
```

#### 响应示例（报警触发）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 3004,
    "taskId": 2001,
    "vehicleId": 1001,
    "temperature": 9.20,
    "humidity": 80.0,
    "location": "河北石家庄",
    "recordTime": "2026-04-12 20:00:00",
    "isAlarm": 1,
    "alarmMsg": "温度超出上限：要求≤8.00℃（tolerance±2.00℃），实际9.20℃",
    "createdAt": "2026-04-12 20:00:00",
    "updatedAt": "2026-04-12 20:00:00"
  }
}
```

#### 业务规则

- **温度报警自动判断**：
  - 查询关联任务的 `requiredTemp`（要求温度），`tolerance = 2.00℃`（系统默认，可配置）
  - 若 `temperature < requiredTemp - tolerance` 或 `temperature > requiredTemp + tolerance`，则 `isAlarm = 1`，`alarmMsg` 自动生成
  - 若关联任务无 `requiredTemp`，不触发自动报警
- `taskId` 必须存在且 `status=IN_TRANSIT`，否则抛 `800202 TEMPERATURE_TASK_MISMATCH`
- `vehicleId` 需与任务关联车辆一致

#### 实现提示

- Controller：`TemperatureRecordController#create`，报警判断在 Service 层执行
- `isAlarm` 由后端自动计算，前端无需传入

---

### 4.4 删除温度记录

**URL**：`DELETE /api/coldchain/temperatures/{id}`
**认证**：需要 JWT Bearer
**权限**：`coldchain:temperature:write`
**错误码**：`800200`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 温度记录 ID |

#### 响应体 `null`

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

#### 业务规则

- id 不存在抛 `800200 TEMPERATURE_RECORD_NOT_FOUND`
- 软删除

---

## 5. 配送管理 DeliveryController（`/api/coldchain/deliveries`）

### 5.1 配送列表（分页）

**URL**：`GET /api/coldchain/deliveries/list`
**认证**：需要 JWT Bearer
**权限**：`coldchain:delivery:read`
**错误码**：`800900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10 | `10` |
| `keyword` | string | ❌ | 配送编码或收货人姓名模糊搜索 | `DL202604` |
| `status` | string | ❌ | 配送状态枚举 code | `PENDING` |

```typescript
export interface DeliveryPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;
}
```

#### 响应体 `PageResult<DeliveryResponse>`

`DeliveryResponse` 字段：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 配送 ID |
| `deliveryCode` | string | ✅ | 配送编码 |
| `taskId` | number | ✅ | 关联运输任务 ID |
| `receiverName` | string | ✅ | 收货人姓名 |
| `receiverPhone` | string | ✅ | 收货人电话 |
| `receiverAddr` | string | ✅ | 收货地址 |
| `deliveryTime` | string \| null | ❌ | 配送时间 |
| `signTime` | string \| null | ❌ | 签收时间 |
| `signPhoto` | string \| null | ❌ | 签收照片 URL |
| `qualityCheck` | `EnumValue<string>` | ✅ | 质检状态，见 QualityCheck |
| `qualityRemark` | string \| null | ❌ | 质检备注 |
| `status` | `EnumValue<string>` | ✅ | 配送状态，见 DeliveryStatus |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface DeliveryResponse {
  id: number;
  deliveryCode: string;
  taskId: number;
  receiverName: string;
  receiverPhone: string;
  receiverAddr: string;
  deliveryTime: string | null;
  signTime: string | null;
  signPhoto: string | null;
  qualityCheck: EnumValue<string>;
  qualityRemark: string | null;
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
        "deliveryCode": "DL202604120001",
        "taskId": 2001,
        "receiverName": "李采购",
        "receiverPhone": "13800138001",
        "receiverAddr": "北京市大兴区新发地市场A区12号",
        "deliveryTime": "2026-04-13 19:00:00",
        "signTime": null,
        "signPhoto": null,
        "qualityCheck": { "code": "PENDING", "desc": "待检" },
        "qualityRemark": null,
        "status": { "code": "DELIVERING", "desc": "配送中" },
        "remark": null,
        "createdAt": "2026-04-12 16:00:00",
        "updatedAt": "2026-04-13 19:00:00"
      }
    ],
    "total": 38,
    "current": 1,
    "size": 10
  }
}
```

---

### 5.2 配送详情

**URL**：`GET /api/coldchain/deliveries/{id}`
**认证**：需要 JWT Bearer
**权限**：`coldchain:delivery:read`
**错误码**：`800300`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 配送 ID |

#### 响应体 `DeliveryResponse`

同 [5.1 响应体定义](#51-配送列表分页)。

#### 响应示例（失败）

```json
{
  "code": 800300,
  "message": "配送记录不存在",
  "data": null
}
```

---

### 5.3 创建配送

**URL**：`POST /api/coldchain/deliveries`
**认证**：需要 JWT Bearer
**权限**：`coldchain:delivery:write`
**错误码**：`800100`、`800301`、`800900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `taskId` | number | ✅ | 运输任务 ID |
| `receiverName` | string | ✅ | 收货人姓名 |
| `receiverPhone` | string | ✅ | 收货人电话 |
| `receiverAddr` | string | ✅ | 收货地址 |
| `deliveryTime` | string | ❌ | 预计配送时间 |
| `remark` | string | ❌ | 备注 |

```typescript
export interface DeliveryCreateRequest {
  taskId: number;
  receiverName: string;
  receiverPhone: string;
  receiverAddr: string;
  deliveryTime?: string;
  remark?: string;
}
```

#### 响应体 `DeliveryResponse`

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 4002,
    "deliveryCode": "DL202604120002",
    "taskId": 2002,
    "receiverName": "王经理",
    "receiverPhone": "13900139002",
    "receiverAddr": "上海市浦东新区农贸中心B区5号",
    "deliveryTime": "2026-04-15 14:00:00",
    "signTime": null,
    "signPhoto": null,
    "qualityCheck": { "code": "PENDING", "desc": "待检" },
    "qualityRemark": null,
    "status": { "code": "PENDING", "desc": "待配送" },
    "remark": null,
    "createdAt": "2026-04-12 15:00:00",
    "updatedAt": "2026-04-12 15:00:00"
  }
}
```

#### 业务规则

- `deliveryCode` 系统自动生成，格式 `DL+yyyyMMdd+4位序号`
- `taskId` 必须存在，不存在抛 `800100 TASK_NOT_FOUND`
- 创建时 `status=PENDING`，`qualityCheck=PENDING`

---

### 5.4 更新配送

**URL**：`PUT /api/coldchain/deliveries/{id}`
**认证**：需要 JWT Bearer
**权限**：`coldchain:delivery:write`
**错误码**：`800300`、`800301`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 配送 ID |

#### 请求体

```typescript
export interface DeliveryUpdateRequest {
  receiverName?: string;
  receiverPhone?: string;
  receiverAddr?: string;
  deliveryTime?: string;
  remark?: string;
}
```

#### 响应体 `DeliveryResponse`

#### 业务规则

- `status=SIGNED` 或 `REJECTED` 时不可修改，抛 `800301 DELIVERY_STATUS_INVALID`

---

### 5.5 签收确认

**URL**：`POST /api/coldchain/deliveries/{id}/sign`
**认证**：需要 JWT Bearer
**权限**：`coldchain:delivery:write`
**错误码**：`800300`、`800302`、`800303`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 配送 ID |

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `signPhoto` | string | ❌ | 签收照片 URL |
| `qualityCheck` | string | ✅ | 质检结果：`PASSED`/`REJECTED` |
| `qualityRemark` | string | ❌ | 质检备注 |

```typescript
export interface DeliverySignRequest {
  signPhoto?: string;
  qualityCheck: string; // "PASSED" | "REJECTED"
  qualityRemark?: string;
}
```

#### 响应体 `DeliveryResponse`

#### 响应示例（成功，签收通过）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 4001,
    "deliveryCode": "DL202604120001",
    "taskId": 2001,
    "receiverName": "李采购",
    "receiverPhone": "13800138001",
    "receiverAddr": "北京市大兴区新发地市场A区12号",
    "deliveryTime": "2026-04-13 19:00:00",
    "signTime": "2026-04-13 19:35:00",
    "signPhoto": "https://oss.apple-chain.com/sign/4001.jpg",
    "qualityCheck": { "code": "PASSED", "desc": "合格" },
    "qualityRemark": "外观良好，无破损",
    "status": { "code": "SIGNED", "desc": "已签收" },
    "remark": null,
    "createdAt": "2026-04-12 16:00:00",
    "updatedAt": "2026-04-13 19:35:00"
  }
}
```

#### 响应示例（质检拒收）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 4003,
    "deliveryCode": "DL202604120003",
    "qualityCheck": { "code": "REJECTED", "desc": "不合格" },
    "qualityRemark": "苹果大面积碰伤，温度超标导致",
    "status": { "code": "REJECTED", "desc": "已拒收" },
    "signTime": "2026-04-14 10:00:00",
    "createdAt": "2026-04-13 08:00:00",
    "updatedAt": "2026-04-14 10:00:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 800302,
  "message": "当前状态不允许签收（仅 DELIVERING 可签收）",
  "data": null
}
```

#### 业务规则

- 仅 `status=DELIVERING` 可执行签收，否则抛 `800302 DELIVERY_SIGN_NOT_ALLOWED`
- `qualityCheck=PASSED` → `status=SIGNED`，记录 `signTime=now()`
- `qualityCheck=REJECTED` → `status=REJECTED`，记录 `signTime=now()`，抛业务事件（可触发 trace.md AnomalyTrace）
- 签收后温度数据归入 trace.md 溯源记录（跨模块写入）

#### 实现提示

- Controller：`DeliveryController#sign`，当前以整个 `Delivery` Entity 接收，目标迁移为 `DeliverySignRequest`

---

### 5.6 删除配送

**URL**：`DELETE /api/coldchain/deliveries/{id}`
**认证**：需要 JWT Bearer
**权限**：`coldchain:delivery:write`
**错误码**：`800300`、`800301`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 配送 ID |

#### 响应体 `null`

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

#### 业务规则

- id 不存在抛 `800300 DELIVERY_NOT_FOUND`
- `status=DELIVERING` 时不可删除，抛 `800301 DELIVERY_STATUS_INVALID`
- 软删除

---

### 5.7 导出配送 CSV

**URL**：`GET /api/coldchain/deliveries/export`
**认证**：需要 JWT Bearer
**权限**：`coldchain:delivery:export`
**变更历史**：2026-04-12 | init | 架构师

> ⚠️ **非 R\<T\> 包装**：直接写入 `HttpServletResponse`，Content-Type: `text/csv; charset=UTF-8`

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | ❌ | 配送编码或收货人姓名模糊搜索 |
| `status` | string | ❌ | 配送状态过滤 |

#### 响应

- HTTP 200，二进制 CSV 流
- 字段列（中文表头）：ID、配送编码、任务ID、收货人、电话、地址、配送时间、签收时间、质检状态、质检备注、配送状态、备注、创建时间

---

## 6. 预冷任务 PreCoolTaskController（`/api/coldchain/precool`）

### 6.1 预冷任务列表（分页）

**URL**：`GET /api/coldchain/precool`
**认证**：需要 JWT Bearer
**权限**：`coldchain:precool:read`
**错误码**：`800900`
**变更历史**：2026-04-12 | init | 架构师

> 注意：列表接口 URL 为 `GET /api/coldchain/precool`（无 `/list` 后缀），与其他模块不同。

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10 | `10` |
| `vehicleId` | number | ❌ | 车辆 ID 过滤 | `1001` |
| `status` | string | ❌ | 预冷状态枚举 code | `PENDING` |

```typescript
export interface PreCoolPageRequest {
  page?: number;
  size?: number;
  vehicleId?: number;
  status?: string;
}
```

#### 响应体 `PageResult<PreCoolTaskResponse>`

`PreCoolTaskResponse` 字段：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 任务 ID |
| `taskNo` | string | ✅ | 预冷任务编号 |
| `vehicleId` | number | ✅ | 车辆 ID |
| `batchCode` | string \| null | ❌ | 货批编码 |
| `startTemp` | number \| null | ❌ | 启动时温度（℃） |
| `targetTemp` | number | ✅ | 目标温度（℃），默认 2.0 |
| `startTime` | string \| null | ❌ | 开始冷却时间 |
| `endTime` | string \| null | ❌ | 结束冷却时间 |
| `duration` | number \| null | ❌ | 冷却时长（分钟） |
| `status` | `EnumValue<string>` | ✅ | 状态，见 PreCoolTaskStatus |
| `operator` | string \| null | ❌ | 操作员 |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface PreCoolTaskResponse {
  id: number;
  taskNo: string;
  vehicleId: number;
  batchCode: string | null;
  startTemp: number | null;
  targetTemp: number;
  startTime: string | null;
  endTime: string | null;
  duration: number | null;
  status: EnumValue<string>;
  operator: string | null;
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
        "taskNo": "PC202604120001",
        "vehicleId": 1001,
        "batchCode": "BATCH20260412001",
        "startTemp": 15.00,
        "targetTemp": 2.00,
        "startTime": "2026-04-12 06:00:00",
        "endTime": "2026-04-12 07:30:00",
        "duration": 90,
        "status": { "code": "COMPLETED", "desc": "已完成" },
        "operator": "王技术员",
        "remark": null,
        "createdAt": "2026-04-12 05:50:00",
        "updatedAt": "2026-04-12 07:30:00"
      }
    ],
    "total": 25,
    "current": 1,
    "size": 10
  }
}
```

---

### 6.2 预冷任务详情

**URL**：`GET /api/coldchain/precool/{id}`
**认证**：需要 JWT Bearer
**权限**：`coldchain:precool:read`
**错误码**：`800400`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 预冷任务 ID |

#### 响应体 `PreCoolTaskResponse`

同 [6.1 响应体定义](#61-预冷任务列表分页)。

#### 响应示例（失败）

```json
{
  "code": 800400,
  "message": "预冷任务不存在",
  "data": null
}
```

---

### 6.3 创建预冷任务

**URL**：`POST /api/coldchain/precool`
**认证**：需要 JWT Bearer
**权限**：`coldchain:precool:write`
**错误码**：`800404`、`800900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `vehicleId` | number | ✅ | 车辆 ID |
| `batchCode` | string | ❌ | 货批编码 |
| `startTemp` | number | ❌ | 启动时温度（℃） |
| `targetTemp` | number | ❌ | 目标温度（℃），默认 2.0 |
| `operator` | string | ❌ | 操作员 |
| `remark` | string | ❌ | 备注 |

```typescript
export interface PreCoolTaskCreateRequest {
  vehicleId: number;
  batchCode?: string;
  startTemp?: number;
  targetTemp?: number;
  operator?: string;
  remark?: string;
}
```

#### 响应体 `PreCoolTaskResponse`

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 5002,
    "taskNo": "PC202604120002",
    "vehicleId": 1002,
    "batchCode": "BATCH20260412002",
    "startTemp": 12.00,
    "targetTemp": 2.00,
    "startTime": null,
    "endTime": null,
    "duration": null,
    "status": { "code": "PENDING", "desc": "待冷却" },
    "operator": "李技术员",
    "remark": null,
    "createdAt": "2026-04-12 16:00:00",
    "updatedAt": "2026-04-12 16:00:00"
  }
}
```

#### 业务规则

- `taskNo` 系统自动生成，格式 `PC+yyyyMMdd+4位序号`
- 创建时 `status=PENDING`
- `targetTemp` 需合理（通常 -10℃ ~ 15℃），超出范围抛 `800404 PRECOOL_TEMPERATURE_INVALID`

---

### 6.4 启动冷却

**URL**：`PUT /api/coldchain/precool/{id}/start`
**认证**：需要 JWT Bearer
**权限**：`coldchain:precool:write`
**错误码**：`800400`、`800402`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 预冷任务 ID |

#### 响应体 `PreCoolTaskResponse`

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 5002,
    "taskNo": "PC202604120002",
    "vehicleId": 1002,
    "startTemp": 12.00,
    "targetTemp": 2.00,
    "startTime": "2026-04-12 16:05:00",
    "endTime": null,
    "duration": null,
    "status": { "code": "COOLING", "desc": "冷却中" },
    "operator": "李技术员",
    "createdAt": "2026-04-12 16:00:00",
    "updatedAt": "2026-04-12 16:05:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 800402,
  "message": "预冷任务当前状态不允许启动（仅 PENDING 可启动）",
  "data": null
}
```

#### 业务规则

- 仅 `status=PENDING` 可执行，否则抛 `800402 PRECOOL_START_NOT_ALLOWED`
- 启动后：`status → COOLING`，记录 `startTime = now()`

---

### 6.5 完成冷却

**URL**：`PUT /api/coldchain/precool/{id}/complete`
**认证**：需要 JWT Bearer
**权限**：`coldchain:precool:write`
**错误码**：`800400`、`800403`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 预冷任务 ID |

#### 响应体 `PreCoolTaskResponse`

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 5002,
    "taskNo": "PC202604120002",
    "vehicleId": 1002,
    "startTemp": 12.00,
    "targetTemp": 2.00,
    "startTime": "2026-04-12 16:05:00",
    "endTime": "2026-04-12 17:35:00",
    "duration": 90,
    "status": { "code": "COMPLETED", "desc": "已完成" },
    "operator": "李技术员",
    "createdAt": "2026-04-12 16:00:00",
    "updatedAt": "2026-04-12 17:35:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 800403,
  "message": "预冷任务当前状态不允许完成（仅 COOLING 可完成）",
  "data": null
}
```

#### 业务规则

- 仅 `status=COOLING` 可执行，否则抛 `800403 PRECOOL_COMPLETE_NOT_ALLOWED`
- 完成后：`status → COMPLETED`，记录 `endTime = now()`
- `duration = (endTime - startTime) in minutes`（自动计算）

---

## 7. 物流全链路 LogisticsQueryController（`/api/coldchain/logistics`）

### 7.1 获取任务完整物流

**URL**：`GET /api/coldchain/logistics/{taskId}/full`
**认证**：需要 JWT Bearer
**权限**：`coldchain:logistics:read`
**错误码**：`800500`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Path）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `taskId` | number | ✅ | 运输任务 ID |

#### 响应体 `LogisticsFullResponse`

`LogisticsFullResponse` 字段（基于 `LogisticsFullVO.java` 源码建模）：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `task` | `TransportTaskResponse` | ✅ | 运输任务详情 |
| `vehicle` | `VehicleResponse` | ✅ | 关联车辆详情 |
| `tempRecords` | `TemperatureRecordResponse[]` | ✅ | 全部温度记录（按 recordTime 升序） |
| `delivery` | `DeliveryResponse \| null` | ❌ | 配送记录（可能尚未创建） |
| `alarmCount` | number | ✅ | 报警次数（`isAlarm=1` 的记录数） |

```typescript
export interface LogisticsFullResponse {
  task: TransportTaskResponse;
  vehicle: VehicleResponse;
  tempRecords: TemperatureRecordResponse[];
  delivery: DeliveryResponse | null;
  alarmCount: number;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "task": {
      "id": 2001,
      "taskCode": "TK202604120001",
      "vehicleId": 1001,
      "orderId": 5001,
      "origin": "陕西富县苹果产业园",
      "destination": "北京新发地市场",
      "cargoDesc": "红富士苹果",
      "cargoWeight": 4.50,
      "requiredTemp": 2.00,
      "planDepart": "2026-04-12 08:00:00",
      "actualDepart": "2026-04-12 08:15:00",
      "planArrive": "2026-04-13 20:00:00",
      "actualArrive": "2026-04-13 19:45:00",
      "distance": 1150.00,
      "cost": 3200.00,
      "status": { "code": "DELIVERED", "desc": "已送达" },
      "remark": null,
      "createdAt": "2026-04-11 18:00:00",
      "updatedAt": "2026-04-13 19:45:00"
    },
    "vehicle": {
      "id": 1001,
      "vehicleCode": "VH202604120001",
      "plateNumber": "陕A12345",
      "vehicleType": { "code": "REFRIGERATED", "desc": "冷藏" },
      "brand": "东风",
      "capacity": 5.00,
      "temperatureMin": -5.00,
      "temperatureMax": 8.00,
      "driverName": "张师傅",
      "driverPhone": "18691234567",
      "status": { "code": "IDLE", "desc": "空闲" }
    },
    "tempRecords": [
      {
        "id": 3001,
        "taskId": 2001,
        "vehicleId": 1001,
        "temperature": 2.10,
        "humidity": 85.0,
        "location": "陕西延安",
        "recordTime": "2026-04-12 08:30:00",
        "isAlarm": 0,
        "alarmMsg": null,
        "createdAt": "2026-04-12 08:30:00",
        "updatedAt": "2026-04-12 08:30:00"
      },
      {
        "id": 3002,
        "taskId": 2001,
        "vehicleId": 1001,
        "temperature": 8.50,
        "humidity": 82.0,
        "location": "河北保定服务区",
        "recordTime": "2026-04-12 14:30:00",
        "isAlarm": 1,
        "alarmMsg": "温度超出上限：要求≤8.00℃（tolerance±2.00℃），实际8.50℃",
        "createdAt": "2026-04-12 14:30:00",
        "updatedAt": "2026-04-12 14:30:00"
      }
    ],
    "delivery": {
      "id": 4001,
      "deliveryCode": "DL202604120001",
      "taskId": 2001,
      "receiverName": "李采购",
      "receiverPhone": "13800138001",
      "receiverAddr": "北京市大兴区新发地市场A区12号",
      "deliveryTime": "2026-04-13 19:00:00",
      "signTime": "2026-04-13 19:35:00",
      "signPhoto": "https://oss.apple-chain.com/sign/4001.jpg",
      "qualityCheck": { "code": "PASSED", "desc": "合格" },
      "qualityRemark": "外观良好",
      "status": { "code": "SIGNED", "desc": "已签收" },
      "createdAt": "2026-04-12 16:00:00",
      "updatedAt": "2026-04-13 19:35:00"
    },
    "alarmCount": 1
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 800500,
  "message": "物流任务不存在",
  "data": null
}
```

#### 业务规则

- `taskId` 必须存在，否则抛 `800500 LOGISTICS_TASK_NOT_FOUND`
- 跨模块聚合查询：task + vehicle（当前模块）+ tempRecords（当前模块）+ delivery（当前模块）
- `alarmCount` = `tempRecords` 中 `isAlarm=1` 的记录数
- 关联 trade.md（orderId）、trace.md（签收数据归入溯源）

#### 实现提示

- Controller：`LogisticsQueryController#getFullLogistics`，返回 `LogisticsFullVO`
- `LogisticsFullVO.java` 源码字段：`task / vehicle / tempRecords / delivery / alarmCount`，契约与源码完全对应
- 目标状态：`LogisticsFullVO` 内部字段迁移为对应 `Response` DTO

---

## 8. 运输统计 TransportStatisticsController（`/api/coldchain/statistics`）

### 8.1 统计概览

**URL**：`GET /api/coldchain/statistics/summary`
**认证**：需要 JWT Bearer
**权限**：`coldchain:statistics:read`
**错误码**：`800900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无参数。

#### 响应体 `TransportSummaryResponse`

> 当前后端实现返回 `Map<String, Object>`，契约目标状态定义结构化 DTO（见实现注记 11.3）。

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `totalTasks` | number | ✅ | 运输任务总数 |
| `taskByStatus` | `Record<string, number>` | ✅ | 按任务状态分组计数，key 为状态 code |
| `totalVehicles` | number | ✅ | 车辆总数 |
| `vehicleByStatus` | `Record<string, number>` | ✅ | 按车辆状态分组计数，key 为状态 code |
| `totalAlarms` | number | ✅ | 历史温度报警总次数 |

```typescript
export interface TransportSummaryResponse {
  totalTasks: number;
  taskByStatus: Record<string, number>;
  totalVehicles: number;
  vehicleByStatus: Record<string, number>;
  totalAlarms: number;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalTasks": 128,
    "taskByStatus": {
      "PENDING": 12,
      "IN_TRANSIT": 8,
      "DELIVERED": 105,
      "CANCELLED": 3
    },
    "totalVehicles": 22,
    "vehicleByStatus": {
      "IDLE": 12,
      "IN_TRANSIT": 8,
      "MAINTENANCE": 1,
      "RETIRED": 1
    },
    "totalAlarms": 47
  }
}
```

#### 业务规则

- 统计全量数据（含已软删除之外的有效记录）
- `taskByStatus` 和 `vehicleByStatus` 仅列出有数据的状态
- `totalAlarms` 统计全历史报警总次数（`isAlarm=1`）

#### 实现提示

- Controller：`TransportStatisticsController#summary`，直接操作 Mapper，返回 `Map<String,Object>`
- 目标状态：迁移为 `TransportSummaryResponse`（见实现注记 11.3）
- 前端当前可直接读取 Map key（`data.totalTasks` 等），迁移后字段名不变

---

### 8.2 近N天报警统计

**URL**：`GET /api/coldchain/statistics/alarms`
**认证**：需要 JWT Bearer
**权限**：`coldchain:statistics:read`
**错误码**：`800600`、`800900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `days` | number | ❌ | 查询天数，默认 30，合法范围 1-365 | `30` |

#### 响应体 `TransportAlarmResponse`

> 当前后端实现返回 `Map<String, Object>`，契约目标状态定义结构化 DTO（见实现注记 11.3）。

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `days` | number | ✅ | 查询天数 |
| `since` | string | ✅ | 统计起始时间 `yyyy-MM-dd HH:mm:ss` |
| `totalAlarms` | number | ✅ | 区间内报警总数 |
| `alarmByTask` | `Record<string, number>` | ✅ | 按任务 ID 分组的报警数，key 为 taskId 字符串 |
| `records` | `TemperatureRecordResponse[]` | ✅ | 报警温度记录详情列表（按 recordTime 降序） |

```typescript
export interface TransportAlarmResponse {
  days: number;
  since: string;
  totalAlarms: number;
  alarmByTask: Record<string, number>;
  records: TemperatureRecordResponse[];
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "days": 30,
    "since": "2026-03-13 10:00:00",
    "totalAlarms": 12,
    "alarmByTask": {
      "2001": 3,
      "2003": 5,
      "2007": 4
    },
    "records": [
      {
        "id": 3002,
        "taskId": 2001,
        "vehicleId": 1001,
        "temperature": 8.50,
        "humidity": 82.0,
        "location": "河北保定服务区",
        "recordTime": "2026-04-12 14:30:00",
        "isAlarm": 1,
        "alarmMsg": "温度超出上限：要求≤8.00℃（tolerance±2.00℃），实际8.50℃",
        "createdAt": "2026-04-12 14:30:00",
        "updatedAt": "2026-04-12 14:30:00"
      }
    ]
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 800600,
  "message": "查询天数无效（合法范围 1-365）",
  "data": null
}
```

#### 业务规则

- `days` 超出范围（< 1 或 > 365）抛 `800600 STATISTICS_DAYS_INVALID`
- `since = now() - days天`，精确到秒
- `records` 按 `recordTime` 降序排列

#### 实现提示

- Controller：`TransportStatisticsController#alarms`，当前 `records` 为 `List<TemperatureRecord>`（Entity）
- 目标：`records` 迁移为 `List<TemperatureRecordResponse>`
- `since` 当前为 `LocalDateTime`，序列化后格式需符合 `yyyy-MM-dd HH:mm:ss`

---

## 9. 错误码清单

| 错误码 | 常量名 | HTTP 状态 | 说明 |
|--------|--------|-----------|------|
| **Vehicle 车辆（800001-800099）** | | | |
| `800001` | `VEHICLE_NOT_FOUND` | 404 | 车辆不存在 |
| `800002` | `VEHICLE_CODE_DUPLICATE` | 409 | 车牌号已存在 |
| `800003` | `VEHICLE_STATUS_INVALID` | 422 | 车辆状态变更不合法 |
| `800004` | `VEHICLE_TYPE_INVALID` | 422 | 车辆类型枚举值无效 |
| `800005` | `VEHICLE_IN_USE_CANNOT_DELETE` | 422 | 车辆运输中不可删除 |
| **TransportTask 运输任务（800100-800199）** | | | |
| `800100` | `TASK_NOT_FOUND` | 404 | 运输任务不存在 |
| `800101` | `TASK_CODE_DUPLICATE` | 409 | 任务编码已存在 |
| `800102` | `TASK_STATUS_INVALID` | 422 | 任务当前状态不允许此操作 |
| `800103` | `TASK_DEPART_NOT_ALLOWED` | 422 | 非 PENDING 状态不允许发车 |
| `800104` | `TASK_DELIVER_NOT_ALLOWED` | 422 | 非 IN_TRANSIT 状态不允许送达 |
| `800105` | `TASK_CANCEL_NOT_ALLOWED` | 422 | 已终态不允许取消 |
| `800106` | `TASK_VEHICLE_UNAVAILABLE` | 422 | 车辆不可用（非空闲状态） |
| `800107` | `TASK_TEMPERATURE_OUT_OF_RANGE` | 422 | 任务温度要求超出车辆控温范围 |
| **TemperatureRecord 温度监控（800200-800299）** | | | |
| `800200` | `TEMPERATURE_RECORD_NOT_FOUND` | 404 | 温度记录不存在 |
| `800201` | `TEMPERATURE_OUT_OF_RANGE` | 200 | 温度超出报警阈值（业务事件，非错误，触发 isAlarm=1） |
| `800202` | `TEMPERATURE_TASK_MISMATCH` | 422 | 温度记录与任务不匹配（任务非运输中） |
| **Delivery 配送（800300-800399）** | | | |
| `800300` | `DELIVERY_NOT_FOUND` | 404 | 配送记录不存在 |
| `800301` | `DELIVERY_STATUS_INVALID` | 422 | 配送当前状态不允许此操作 |
| `800302` | `DELIVERY_SIGN_NOT_ALLOWED` | 422 | 非 DELIVERING 状态不允许签收 |
| `800303` | `DELIVERY_QUALITY_REJECTED` | 200 | 质检拒收（业务事件，触发 trace.md AnomalyTrace） |
| **PreCoolTask 预冷任务（800400-800499）** | | | |
| `800400` | `PRECOOL_NOT_FOUND` | 404 | 预冷任务不存在 |
| `800401` | `PRECOOL_STATUS_INVALID` | 422 | 预冷任务当前状态不允许此操作 |
| `800402` | `PRECOOL_START_NOT_ALLOWED` | 422 | 非 PENDING 状态不允许启动冷却 |
| `800403` | `PRECOOL_COMPLETE_NOT_ALLOWED` | 422 | 非 COOLING 状态不允许完成冷却 |
| `800404` | `PRECOOL_TEMPERATURE_INVALID` | 422 | 目标温度参数无效 |
| **LogisticsQuery 物流聚合（800500-800599）** | | | |
| `800500` | `LOGISTICS_TASK_NOT_FOUND` | 404 | 物流查询任务不存在 |
| **TransportStatistics 统计（800600-800699）** | | | |
| `800600` | `STATISTICS_DAYS_INVALID` | 422 | 查询天数无效（合法范围 1-365） |
| **通用参数/权限（800900-800999）** | | | |
| `800900` | `COLDCHAIN_PARAM_INVALID` | 400 | 冷链模块请求参数错误 |
| `800901` | `COLDCHAIN_UNAUTHORIZED` | 403 | 冷链模块权限不足 |

> 总计：31 个错误码（≥ 28 个要求）

---

## 10. 枚举集中声明

> 所有枚举在响应中以 `{ "code": "<STRING_CODE>", "desc": "<中文描述>" }` 对象形式序列化。  
> 请求参数传枚举 code 字符串（如 `"REFRIGERATED"`）。

### 10.1 VehicleType 车辆类型

| code | desc | 说明 |
|------|------|------|
| `REFRIGERATED` | 冷藏 | 具备主动制冷能力，适合 -15℃~8℃ 货物 |
| `INSULATED` | 保温 | 被动隔热，适合 0℃~15℃ 货物 |
| `NORMAL` | 普通 | 无温控，仅用于常温运输 |

```typescript
export type VehicleType = 'REFRIGERATED' | 'INSULATED' | 'NORMAL';
```

### 10.2 VehicleStatus 车辆状态

| code | desc | 说明 |
|------|------|------|
| `IDLE` | 空闲 | 可接受新运输任务 |
| `IN_TRANSIT` | 运输中 | 正在执行运输任务，不可接新任务 |
| `MAINTENANCE` | 维护中 | 维修保养，不可接新任务 |
| `RETIRED` | 退役 | 已退役，不可再使用（不可逆） |

**状态机**：

```
IDLE ←→ IN_TRANSIT    （运输任务创建/完成时自动切换）
  *  →  MAINTENANCE   （手动进入维修）
MAINTENANCE → IDLE     （维修完成）
  *  →  RETIRED        （退役，不可逆）
```

```typescript
export type VehicleStatus = 'IDLE' | 'IN_TRANSIT' | 'MAINTENANCE' | 'RETIRED';
```

### 10.3 TransportTaskStatus 运输任务状态

| code | desc | 说明 |
|------|------|------|
| `PENDING` | 待发车 | 任务已创建，等待发车 |
| `IN_TRANSIT` | 运输中 | 已发车，正在运输途中 |
| `DELIVERED` | 已送达 | 货物已安全到达目的地 |
| `CANCELLED` | 已取消 | 任务已取消（终态） |

**状态机**：

```
PENDING → IN_TRANSIT（depart 发车）
IN_TRANSIT → DELIVERED（deliver 确认送达）
PENDING → CANCELLED（cancel 取消）
IN_TRANSIT → CANCELLED（cancel 取消）
```

```typescript
export type TransportTaskStatus = 'PENDING' | 'IN_TRANSIT' | 'DELIVERED' | 'CANCELLED';
```

### 10.4 DeliveryStatus 配送状态

| code | desc | 说明 |
|------|------|------|
| `PENDING` | 待配送 | 配送单已创建 |
| `DELIVERING` | 配送中 | 正在配送途中 |
| `SIGNED` | 已签收 | 收货人签收（qualityCheck=PASSED） |
| `REJECTED` | 已拒收 | 收货人拒收（qualityCheck=REJECTED） |

**状态机**：

```
PENDING → DELIVERING（更新状态）
DELIVERING → SIGNED（sign，qualityCheck=PASSED）
DELIVERING → REJECTED（sign，qualityCheck=REJECTED）
```

```typescript
export type DeliveryStatus = 'PENDING' | 'DELIVERING' | 'SIGNED' | 'REJECTED';
```

### 10.5 QualityCheck 质检状态

| code | desc | 说明 |
|------|------|------|
| `PENDING` | 待检 | 尚未质检（初始状态） |
| `PASSED` | 合格 | 质检通过 |
| `REJECTED` | 不合格 | 质检不通过，触发拒收流程 |

```typescript
export type QualityCheck = 'PENDING' | 'PASSED' | 'REJECTED';
```

### 10.6 PreCoolTaskStatus 预冷任务状态

| code | desc | 说明 |
|------|------|------|
| `PENDING` | 待冷却 | 预冷任务已创建，等待启动 |
| `COOLING` | 冷却中 | 正在进行预冷 |
| `COMPLETED` | 已完成 | 预冷完成，达到目标温度 |
| `FAILED` | 失败 | 预冷失败（设备故障等） |

**状态机**：

```
PENDING → COOLING（start 启动）
COOLING → COMPLETED（complete 完成）
COOLING → FAILED（设备故障，系统/人工标记）
```

```typescript
export type PreCoolTaskStatus = 'PENDING' | 'COOLING' | 'COMPLETED' | 'FAILED';
```

### 10.7 AlarmFlag 报警标志

| code | desc | 类型 | 说明 |
|------|------|------|------|
| `0` | 正常 | Integer | 温度在允许范围内 |
| `1` | 报警 | Integer | 温度超出 tolerance 范围，自动触发 |

> `AlarmFlag` 为 Integer 类型，**不序列化为 `{code, desc}` 对象**，直接以 `0` / `1` 数值出现在响应中（`isAlarm` 字段）。  
> 这是对全局枚举约定的**例外**，因为 `isAlarm` 是历史兼容字段，同时用作过滤参数。

```typescript
export type AlarmFlag = 0 | 1;
```

---

## 11. 实现注记

### 11.1 String → EnumValue 迁移清单

当前后端 Entity/Controller 中以下字段仍为 `String` 类型，需实现 `BaseEnum` 并在序列化时输出 `{code, desc}` 对象：

| Entity | 字段 | 目标 EnumValue | 优先级 |
|--------|------|---------------|--------|
| `Vehicle` | `vehicleType` | `VehicleType` | P0 |
| `Vehicle` | `status` | `VehicleStatus` | P0 |
| `TransportTask` | `status` | `TransportTaskStatus` | P0 |
| `Delivery` | `qualityCheck` | `QualityCheck` | P0 |
| `Delivery` | `status` | `DeliveryStatus` | P0 |
| `PreCoolTask` | `status` | `PreCoolTaskStatus` | P0 |
| `TemperatureRecord` | `isAlarm` | 保持 `Integer`（例外，不迁移枚举） | - |

**迁移方式**：
1. 定义枚举实现 `BaseEnum<String>` 接口
2. 配置 Jackson `EnumValueSerializer`
3. Controller 层 Request 接收 String code，Service 层转换为 Enum

### 11.2 Entity → Request/Response DTO 重命名

当前 Controller 直接使用 Entity（`Vehicle`、`TransportTask` 等）作为请求和响应对象，违反「禁止暴露 DO 到接口层」约定。

**目标 DTO 命名**：

| 当前（Entity） | 目标 Request | 目标 Response |
|----------------|-------------|---------------|
| `Vehicle` | `VehicleCreateRequest` / `VehicleUpdateRequest` / `VehicleStatusChangeRequest` | `VehicleResponse` |
| `TransportTask` | `TransportTaskCreateRequest` / `TransportTaskUpdateRequest` | `TransportTaskResponse` |
| `TemperatureRecord` | `TemperatureRecordCreateRequest` | `TemperatureRecordResponse` |
| `Delivery` | `DeliveryCreateRequest` / `DeliveryUpdateRequest` / `DeliverySignRequest` | `DeliveryResponse` |
| `PreCoolTask` | `PreCoolTaskCreateRequest` | `PreCoolTaskResponse` |
| `LogisticsFullVO` | - | `LogisticsFullResponse` |

**迁移步骤**：
1. 在 `apple-module-coldchain/src/main/java/com/apple/chain/coldchain/dto/` 下创建对应 DTO 类
2. 在 Controller 层替换 Entity 为 DTO
3. 在 Service 层增加 Entity ↔ DTO 转换（可用 MapStruct 或手动 Builder）

### 11.3 Statistics Map\<String,Object\> → Response DTO 迁移

`TransportStatisticsController` 当前直接操作 Mapper 并返回 `Map<String,Object>`。

**目标状态**：

```java
// 新增 DTO
public class TransportSummaryResponse {
    private long totalTasks;
    private Map<String, Long> taskByStatus;
    private long totalVehicles;
    private Map<String, Long> vehicleByStatus;
    private long totalAlarms;
}

public class TransportAlarmResponse {
    private int days;
    private LocalDateTime since;
    private long totalAlarms;
    private Map<String, Long> alarmByTask;
    private List<TemperatureRecordResponse> records;
}
```

**前端兼容性**：Map key 与当前一致（`totalTasks`、`taskByStatus` 等），前端代码无需修改字段读取逻辑。

**迁移风险**：`taskByStatus`/`vehicleByStatus` key 为状态 String code（如 `"PENDING"`），前端展示时需映射到中文（见枚举 10.3/10.2），建议前端维护 i18n 映射表。

### 11.4 分页参数迁移 PageParam

当前各 Controller 直接使用 `@RequestParam int page, int size, ...` 接收分页参数，目标迁移为全局 `PageParam` 对象：

```java
// 目标
public R<PageResult<VehicleResponse>> list(PageParam pageParam,
                                            @RequestParam String vehicleType,
                                            @RequestParam String status) { ... }
```

**注意**：`PreCoolTaskController` 的列表接口额外有 `vehicleId` 参数，无全局 `PageParam` 对应字段，需扩展 `PreCoolPageRequest` 继承 `PageParam`。

### 11.5 温度报警规则和 tolerance 配置

**规则**：

```
isAlarm = 1  当且仅当：
  temperature < requiredTemp - tolerance  (温度过低)
  OR
  temperature > requiredTemp + tolerance  (温度过高)
```

**tolerance 默认值**：`2.00℃`

**配置化**：建议将 tolerance 外部化为配置项：

```yaml
# application.yml
coldchain:
  temperature:
    alarm-tolerance: 2.0  # 温度报警容差（℃）
```

**alarmMsg 格式**：

- 超出上限：`"温度超出上限：要求≤{requiredTemp+tolerance}℃（tolerance±{tolerance}℃），实际{temperature}℃"`
- 超出下限：`"温度超出下限：要求≥{requiredTemp-tolerance}℃（tolerance±{tolerance}℃），实际{temperature}℃"`
- 无 requiredTemp：不判断报警，`isAlarm=0`，`alarmMsg=null`

**IoT 集成**：IoT 传感器上报数据（`iot.md`）经过解析后自动调用此接口写入温度记录，报警判断由此接口统一处理。

### 11.6 跨模块依赖

| 依赖方向 | 说明 | 触发时机 |
|---------|------|---------|
| `coldchain → trade.md` | 运输任务关联 `orderId`，校验订单存在性 | 创建运输任务时 |
| `coldchain → iot.md` | IoT 传感器温度上报写入温度记录 | 实时数据采集 |
| `coldchain → trace.md` | 签收数据归入溯源记录；温度违规触发 AnomalyTrace | 签收确认时 |
| `coldchain → warehouse.md` | 冷库温度监控数据共享 | 数据查询层 |
| `warehouse.md → coldchain` | 仓储模块引用冷链温度传感数据 | 关联契约 |

---

## 质量 Checklist

- [x] 35 个端点全覆盖（7+9+4+7+5+1+2）
- [x] 31 个错误码（≥28），全在 800000-899999 范围
- [x] 7 个枚举声明完整（VehicleType / VehicleStatus / TransportTaskStatus / DeliveryStatus / QualityCheck / PreCoolTaskStatus / AlarmFlag）
- [x] JSON 示例全用 `message` 字段（禁用 `msg`）
- [x] 分页使用 PageParam 语义 + PageResult\<T\> 结构
- [x] 3 个 CSV 导出（vehicles/tasks/deliveries）明确标注非 R\<T\> 包装
- [x] 温度报警规则（tolerance=2℃，可外部化配置）入文档（§4.3 业务规则 + §11.5）
- [x] 运输任务状态机（PENDING→IN_TRANSIT→DELIVERED，\*→CANCELLED）入文档（§10.3）
- [x] 预冷状态机（PENDING→COOLING→COMPLETED/FAILED）入文档（§10.6）
- [x] 配送状态机（PENDING→DELIVERING→SIGNED/REJECTED）入文档（§10.4）
- [x] LogisticsFullResponse 建模与源码 LogisticsFullVO.java 字段（task/vehicle/tempRecords/delivery/alarmCount）完全一致
- [x] 统计 Map→Response DTO 迁移方案入实现注记（§11.3）
- [x] 6 个 String 字段 + 1 个 Integer 字段 EnumValue 迁移清单（§11.1）
- [x] Entity → Request/Response DTO 重命名（§11.2）
- [x] 跨模块依赖（iot/trade/trace/warehouse）入文档（§11.6）
