# P2-10 IoT 物联网模块契约

> 模块：`apple-module-iot`
> URL 前缀：`/api/iot/{telemetry,alerts}`
> 错误码段：`900000 ~ 999999`（CONVENTIONS.md:141 权威）
> 最近更新：2026-04-12
> 权威源：本文件
> 后端实现：`apple-module-iot`
> 关联契约：`coldchain.md`（温度传感器数据）/ `trace.md`（IoT 数据进入溯源链）

---

## 模块概述

IoT 物联网模块负责**边缘网关数据接入**与**实时告警引擎**，核心能力：

1. **遥测数据接入**：边缘网关通过批量接口上报冷链车辆/仓库的 GPS、温度（4 路传感器）、湿度、门磁等数据，支持幂等写入（`device_sn + collect_time` 唯一索引去重）
2. **实时告警引擎**：每条遥测数据自动经过 4 条基线规则评估，产生温度过高/过低、门磁异常、传感器偏差等告警
3. **离线设备扫描**：定时扫描超时未上报的冷链车辆，自动标记离线并产生告警

### 数据流

```
边缘网关 ──POST /telemetry/batch──▶ TelemetryIngestService ──▶ iot_telemetry 表
                                                     │
                                                     ▼
                                             AlertEngineService ──▶ iot_alert 表
                                                     │
                                                     ▼
                                             触发设备状态更新 ──▶ iot_device.lastOnlineTime
```

---

## 1. 接口索引（4 端点）

| # | 接口中文名 | URL | 方法 | 认证 | 权限 |
|---|-----------|-----|------|------|------|
| 1 | [批量上报遥测数据](#21-批量上报遥测数据) | `/api/iot/telemetry/batch` | POST | JWT | `logistics:write` |
| 2 | [查询设备最近遥测点](#22-查询设备最近遥测点) | `/api/iot/telemetry/{deviceSn}/latest` | GET | JWT | `logistics:read` |
| 3 | [查询设备遥测序列](#23-查询设备遥测序列) | `/api/iot/telemetry/{deviceSn}/series` | GET | JWT | `logistics:read` |
| 4 | [未处理告警列表](#24-未处理告警列表) | `/api/iot/alerts/open` | GET | JWT | `logistics:read` |

---

## 2. 遥测数据 Telemetry（`/api/iot/telemetry`）

### 2.1 批量上报遥测数据

**URL**：`POST /api/iot/telemetry/batch`
**认证**：需要 JWT Bearer
**权限**：`logistics:write`
**错误码**：`900001` / `900002`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `List<TelemetryRequest>`

数组形式批量上报，每条为一个遥测采样点。

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `deviceSn` | string | Y | 设备序列号（如冷车 SN） | `TRUCK-001` |
| `collectTime` | string | Y | 采集时间，ISO 格式 `yyyy-MM-ddTHH:mm:ss` | `2026-04-12T08:30:00` |
| `latitude` | number | N | 纬度（精度 7 位） | `37.4638123` |
| `longitude` | number | N | 经度（精度 7 位） | `121.4405123` |
| `altitude` | number | N | 海拔（米，精度 2 位） | `25.50` |
| `speed` | number | N | 速度（km/h，精度 2 位） | `45.30` |
| `temp1` | number | N | 温度传感器 1（°C，精度 2 位） | `2.50` |
| `temp2` | number | N | 温度传感器 2（°C） | `2.80` |
| `temp3` | number | N | 温度传感器 3（°C） | `2.60` |
| `temp4` | number | N | 温度传感器 4（°C） | `2.40` |
| `humidity` | number | N | 湿度（%，精度 2 位） | `90.50` |
| `doorStatus` | integer | N | 门磁状态：0=关闭，1=开启 | `0` |
| `signalType` | string | N | 信号类型（如 `5G`、`4G`） | `5G` |

```typescript
export interface TelemetryRequest {
  deviceSn: string;
  collectTime: string;
  latitude?: number;
  longitude?: number;
  altitude?: number;
  speed?: number;
  temp1?: number;
  temp2?: number;
  temp3?: number;
  temp4?: number;
  humidity?: number;
  doorStatus?: number;
  signalType?: string;
}
```

#### 业务规则

- **幂等性**：`device_sn + collect_time` 组合唯一，重复写入被静默忽略（不报错）。
- **批量上限**：单次最多 1000 条，超出返回错误码 `900001`。
- **自动告警**：每条遥测数据写入后自动经过 `AlertEngineService` 评估 4 条规则（见 §5）。
- **设备心跳**：写入后自动更新 `iot_device.lastOnlineTime` 并将设备状态置为 `ONLINE`。

#### 响应体 `R<Integer>`

| 字段 | 类型 | 说明 |
|------|------|------|
| `data` | number | 实际插入条数（去重后） |

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 3
}
```

#### 响应示例（失败 — 批量超限）

```json
{
  "code": 900001,
  "message": "批量上报条数超过上限1000",
  "data": null
}
```

#### 响应示例（失败 — 参数校验）

```json
{
  "code": 900002,
  "message": "遥测数据上报失败：deviceSn 不能为空",
  "data": null
}
```

---

### 2.2 查询设备最近遥测点

**URL**：`GET /api/iot/telemetry/{deviceSn}/latest`
**认证**：需要 JWT Bearer
**权限**：`logistics:read`
**错误码**：`900003`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `deviceSn` | string | Y | 设备序列号 | `TRUCK-001` |

#### 请求参数

无。

#### 响应体 `TelemetryResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | Y | 遥测记录 ID |
| `deviceSn` | string | Y | 设备序列号 |
| `collectTime` | string | Y | 采集时间，`yyyy-MM-dd HH:mm:ss` |
| `latitude` | number \| null | N | 纬度 |
| `longitude` | number \| null | N | 经度 |
| `altitude` | number \| null | N | 海拔（米） |
| `speed` | number \| null | N | 速度（km/h） |
| `temp1` | number \| null | N | 温度传感器 1（°C） |
| `temp2` | number \| null | N | 温度传感器 2（°C） |
| `temp3` | number \| null | N | 温度传感器 3（°C） |
| `temp4` | number \| null | N | 温度传感器 4（°C） |
| `humidity` | number \| null | N | 湿度（%） |
| `doorStatus` | integer \| null | N | 门磁状态 |
| `signalType` | string \| null | N | 信号类型 |
| `createdAt` | string | Y | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | Y | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface TelemetryResponse {
  id: number;
  deviceSn: string;
  collectTime: string;
  latitude: number | null;
  longitude: number | null;
  altitude: number | null;
  speed: number | null;
  temp1: number | null;
  temp2: number | null;
  temp3: number | null;
  temp4: number | null;
  humidity: number | null;
  doorStatus: number | null;
  signalType: string | null;
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
    "id": 1001,
    "deviceSn": "TRUCK-001",
    "collectTime": "2026-04-12 08:30:00",
    "latitude": 37.4638123,
    "longitude": 121.4405123,
    "altitude": 25.50,
    "speed": 45.30,
    "temp1": 2.50,
    "temp2": 2.80,
    "temp3": 2.60,
    "temp4": 2.40,
    "humidity": 90.50,
    "doorStatus": 0,
    "signalType": "5G",
    "createdAt": "2026-04-12 08:30:05",
    "updatedAt": "2026-04-12 08:30:05"
  }
}
```

#### 响应示例（失败 — 设备无数据）

```json
{
  "code": 900003,
  "message": "未找到设备遥测数据",
  "data": null
}
```

---

### 2.3 查询设备遥测序列

**URL**：`GET /api/iot/telemetry/{deviceSn}/series`
**认证**：需要 JWT Bearer
**权限**：`logistics:read`
**错误码**：`900004`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `deviceSn` | string | Y | 设备序列号 | `TRUCK-001` |

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `since` | string | Y | — | 起始时间，ISO 格式 `yyyy-MM-ddTHH:mm:ss` |
| `limit` | number | N | `1000` | 返回条数，最大 5000，最小 1 |

```typescript
export interface TelemetrySeriesRequest {
  since: string;
  limit?: number;
}
```

#### 业务规则

- 结果按 `collectTime ASC` 排序（时间升序）。
- `limit` 自动钳位到 `[1, 5000]`。

#### 响应体 `R<List<TelemetryResponse>>`

返回 `TelemetryResponse` 数组，字段同 §2.2。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 998,
      "deviceSn": "TRUCK-001",
      "collectTime": "2026-04-12 08:28:00",
      "latitude": 37.4638000,
      "longitude": 121.4405000,
      "altitude": 24.80,
      "speed": 42.10,
      "temp1": 2.30,
      "temp2": 2.50,
      "temp3": 2.40,
      "temp4": 2.20,
      "humidity": 89.00,
      "doorStatus": 0,
      "signalType": "5G",
      "createdAt": "2026-04-12 08:28:05",
      "updatedAt": "2026-04-12 08:28:05"
    },
    {
      "id": 1001,
      "deviceSn": "TRUCK-001",
      "collectTime": "2026-04-12 08:30:00",
      "latitude": 37.4638123,
      "longitude": 121.4405123,
      "altitude": 25.50,
      "speed": 45.30,
      "temp1": 2.50,
      "temp2": 2.80,
      "temp3": 2.60,
      "temp4": 2.40,
      "humidity": 90.50,
      "doorStatus": 0,
      "signalType": "5G",
      "createdAt": "2026-04-12 08:30:05",
      "updatedAt": "2026-04-12 08:30:05"
    }
  ]
}
```

#### 响应示例（失败）

```json
{
  "code": 900004,
  "message": "遥测序列查询失败",
  "data": null
}
```

---

## 3. 告警 Alerts（`/api/iot/alerts`）

### 3.1 未处理告警列表

**URL**：`GET /api/iot/alerts/open`
**认证**：需要 JWT Bearer
**权限**：`logistics:read`
**错误码**：`900005`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `limit` | number | N | `100` | 返回条数，自动钳位到 `[1, 500]` |

#### 响应体 `R<List<IotAlertResponse>>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | Y | 告警 ID |
| `deviceSn` | string | Y | 设备序列号 |
| `alertType` | `EnumValue<string>` | Y | 告警类型，见 AlertType |
| `alertLevel` | `EnumValue<number>` | Y | 告警级别，见 AlertLevel |
| `thresholdValue` | string | Y | 阈值 |
| `actualValue` | string | Y | 实际值 |
| `message` | string | Y | 告警描述 |
| `handled` | number | Y | 是否已处理（0=未处理，1=已处理） |
| `createdAt` | string | Y | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | Y | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface IotAlertResponse {
  id: number;
  deviceSn: string;
  alertType: EnumValue<string>;
  alertLevel: EnumValue<number>;
  thresholdValue: string;
  actualValue: string;
  message: string;
  handled: number;
  createdAt: string;
  updatedAt: string;
}
```

#### 业务规则

- 仅返回 `handled = 0` 的未处理告警。
- 按 `id DESC` 排序（最新告警在前）。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 201,
      "deviceSn": "TRUCK-001",
      "alertType": { "code": "TEMP_HIGH", "desc": "温度过高" },
      "alertLevel": { "code": 2, "desc": "警告" },
      "thresholdValue": "4.0",
      "actualValue": "6.20",
      "message": "温度过高: 6.20°C",
      "handled": 0,
      "createdAt": "2026-04-12 08:30:06",
      "updatedAt": "2026-04-12 08:30:06"
    },
    {
      "id": 200,
      "deviceSn": "TRUCK-003",
      "alertType": { "code": "DOOR_ANOMALY", "desc": "门磁异常" },
      "alertLevel": { "code": 2, "desc": "警告" },
      "thresholdValue": "speed<5.0",
      "actualValue": "speed=52.30",
      "message": "行驶中门未关: 速度 52.30km/h",
      "handled": 0,
      "createdAt": "2026-04-12 08:29:10",
      "updatedAt": "2026-04-12 08:29:10"
    },
    {
      "id": 199,
      "deviceSn": "TRUCK-001",
      "alertType": { "code": "SENSOR_FAULT", "desc": "传感器偏差" },
      "alertLevel": { "code": 1, "desc": "提示" },
      "thresholdValue": "1.0",
      "actualValue": "1.50",
      "message": "传感器偏差过大: 2.10→3.60",
      "handled": 0,
      "createdAt": "2026-04-12 08:30:06",
      "updatedAt": "2026-04-12 08:30:06"
    }
  ]
}
```

#### 响应示例（失败）

```json
{
  "code": 900005,
  "message": "告警列表查询失败",
  "data": null
}
```

---

## 4. 数据模型

### 4.1 IotDevice（设备表 `iot_device`）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint | 主键（雪花） |
| `deviceSn` | varchar | 设备序列号（唯一标识） |
| `deviceType` | varchar | 设备类型，见 DeviceType |
| `vehicleNo` | varchar | 关联车牌号（冷链车辆时） |
| `warehouseId` | bigint | 关联仓库 ID（仓库设备时） |
| `status` | int | 设备状态，见 DeviceStatus |
| `lastOnlineTime` | datetime | 最近在线时间 |
| `createdAt` | datetime | 创建时间 |
| `updatedAt` | datetime | 更新时间 |
| `deleted` | int | 逻辑删除 |

### 4.2 IotTelemetry（遥测表 `iot_telemetry`）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint | 主键（雪花） |
| `deviceSn` | varchar | 设备序列号 |
| `collectTime` | datetime | 采集时间 |
| `latitude` | decimal(10,7) | 纬度 |
| `longitude` | decimal(10,7) | 经度 |
| `altitude` | decimal(6,2) | 海拔（米） |
| `speed` | decimal(6,2) | 速度（km/h） |
| `temp1` | decimal(4,2) | 温度传感器 1（°C） |
| `temp2` | decimal(4,2) | 温度传感器 2（°C） |
| `temp3` | decimal(4,2) | 温度传感器 3（°C） |
| `temp4` | decimal(4,2) | 温度传感器 4（°C） |
| `humidity` | decimal(5,2) | 湿度（%） |
| `doorStatus` | int | 门磁状态（0=关，1=开） |
| `signalType` | varchar | 信号类型 |
| `createdAt` | datetime | 创建时间 |
| `updatedAt` | datetime | 更新时间 |
| `deleted` | int | 逻辑删除 |

> **唯一索引**：`uk_device_collect`（`device_sn`, `collect_time`）— 保证幂等。

### 4.3 IotAlert（告警表 `iot_alert`）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint | 主键（雪花） |
| `deviceSn` | varchar | 设备序列号 |
| `alertType` | varchar | 告警类型，见 AlertType |
| `alertLevel` | int | 告警级别，见 AlertLevel |
| `thresholdValue` | varchar | 规则阈值 |
| `actualValue` | varchar | 实际值 |
| `message` | varchar | 告警描述 |
| `handled` | int | 是否已处理（0=未处理，1=已处理） |
| `createdAt` | datetime | 创建时间 |
| `updatedAt` | datetime | 更新时间 |
| `deleted` | int | 逻辑删除 |

---

## 5. 告警规则引擎

`AlertEngineService` 在每条遥测数据写入后自动执行，共 4 条基线规则：

### 规则 1: TEMP_HIGH（温度过高）

| 属性 | 值 |
|------|-----|
| 条件 | `max(temp1, temp2, temp3, temp4) > 4.0` |
| 级别 | WARN（2） |
| 告警描述 | `温度过高: {maxTemp}°C` |

### 规则 2: TEMP_LOW（温度过低）

| 属性 | 值 |
|------|-----|
| 条件 | `min(temp1, temp2, temp3, temp4) < 0.0` |
| 级别 | WARN（2） |
| 告警描述 | `温度过低: {minTemp}°C` |

### 规则 3: DOOR_ANOMALY（门磁异常）

| 属性 | 值 |
|------|-----|
| 条件 | `doorStatus = 1`（门开）且 `speed > 5.0 km/h` |
| 级别 | WARN（2） |
| 告警描述 | `行驶中门未关: 速度 {speed}km/h` |

### 规则 4: SENSOR_FAULT（传感器偏差）

| 属性 | 值 |
|------|-----|
| 条件 | `max(temps) - min(temps) > 1.0°C` |
| 级别 | INFO（1） |
| 告警描述 | `传感器偏差过大: {min}→{max}` |

### 离线扫描（定时任务）

`AlertEngineService.sweepOfflineDevices(maxStaleMinutes)` 扫描所有在线冷链车辆：

| 属性 | 值 |
|------|-----|
| 条件 | `lastOnlineTime < now - maxStaleMinutes` |
| 告警类型 | OFFLINE |
| 级别 | CRITICAL（3） |
| 副作用 | 设备状态自动置为 OFFLINE |

> **注意**：离线扫描当前未暴露为 HTTP 端点，由内部定时任务调用。

### 告警阈值常量

| 常量 | 值 | 说明 |
|------|-----|------|
| `TEMP_MAX` | `4.0°C` | 冷链温度上限 |
| `TEMP_MIN` | `0.0°C` | 冷链温度下限 |
| `SENSOR_DEVIATION_LIMIT` | `1.0°C` | 传感器偏差阈值 |
| `DOOR_OPEN_SPEED_THRESHOLD` | `5.0 km/h` | 门开时速度阈值 |

---

## 6. 枚举声明

### 6.1 DeviceType（设备类型）

| code | desc |
|------|------|
| `COLD_TRUCK` | 冷链运输车 |
| `WAREHOUSE` | 仓库传感器 |
| `PRE_COOLING` | 预冷设备 |

### 6.2 DeviceStatus（设备状态）

| code | desc |
|------|------|
| `0` | 离线 |
| `1` | 在线 |
| `2` | 故障 |

### 6.3 AlertType（告警类型）

| code | desc |
|------|------|
| `TEMP_HIGH` | 温度过高 |
| `TEMP_LOW` | 温度过低 |
| `DOOR_ANOMALY` | 门磁异常 |
| `OFFLINE` | 设备离线 |
| `SENSOR_FAULT` | 传感器偏差 |

### 6.4 AlertLevel（告警级别）

| code | desc |
|------|------|
| `1` | 提示（INFO） |
| `2` | 警告（WARN） |
| `3` | 严重（CRITICAL） |

### 6.5 DoorStatus（门磁状态）

| code | desc |
|------|------|
| `0` | 关闭 |
| `1` | 开启 |

### 6.6 SignalType（信号类型）

| code | desc |
|------|------|
| `5G` | 5G 网络 |
| `4G` | 4G 网络 |
| `NB_IOT` | NB-IoT |
| `LORA` | LoRa |

---

## 7. 错误码表

| 错误码 | HTTP 状态码 | 说明 |
|--------|-----------|------|
| `900001` | 400 | 批量上报条数超过上限1000 |
| `900002` | 400 | 遥测数据上报失败（参数校验不通过） |
| `900003` | 404 | 未找到设备遥测数据 |
| `900004` | 500 | 遥测序列查询失败 |
| `900005` | 500 | 告警列表查询失败 |
| `900006` | 400 | 设备序列号不能为空 |
| `900007` | 400 | 采集时间不能为空 |
| `900008` | 500 | 遥测数据写入失败（数据库异常） |
| `900009` | 404 | 设备不存在 |
| `900010` | 500 | 告警规则引擎执行失败 |
| `900011` | 403 | 无权限访问（缺少 logistics:read 或 logistics:write） |
| `900012` | 429 | 上报频率超限（限流保护） |
| `900013` | 400 | 请求参数格式无效 |

---

## 8. 实现注记

### 8.1 端点总览

| 端点 | Controller 方法 | 数据源 | 说明 |
|------|----------------|--------|------|
| `POST /api/iot/telemetry/batch` | `IotController.ingest` | Write | 边缘网关批量上报，幂等 |
| `GET /api/iot/telemetry/{deviceSn}/latest` | `IotController.latest` | Read | 最新单点 |
| `GET /api/iot/telemetry/{deviceSn}/series` | `IotController.series` | Read | 时间序列 |
| `GET /api/iot/alerts/open` | `IotController.openAlerts` | Read | 未处理告警 |

### 8.2 幂等机制

批量上报端点依赖 `iot_telemetry` 表的唯一索引 `uk_device_collect (device_sn, collect_time)` 实现幂等。`DuplicateKeyException` 被静默捕获并跳过，不中断批次处理。

### 8.3 告警产生时机

告警**不在** Controller 层产生，而是在 `TelemetryIngestServiceImpl.ingestBatch()` 内部，每条遥测写入后调用 `AlertEngineService.evaluateAndPersist()`。前端无感知，告警自动落入 `iot_alert` 表。

### 8.4 Mock 数据生成

`MockTelemetryGenerator` 通过 `@ConditionalOnProperty(name = "iot.mock.enabled", havingValue = "true")` 控制，默认关闭。启用后每 30s 为所有在线冷链车辆生成模拟遥测数据（含 5% 温度超限概率用于触发告警）。

### 8.5 String -> EnumValue 迁移

当前后端 Entity 中以下字段为 String/int 类型，需迁移为 `EnumValue` 序列化：

| Entity | 字段 | 当前类型 | 目标类型 |
|--------|------|---------|---------|
| IotDevice | `deviceType` | String | `EnumValue<DeviceType>` |
| IotDevice | `status` | int | `EnumValue<DeviceStatus>` |
| IotAlert | `alertType` | String | `EnumValue<AlertType>` |
| IotAlert | `alertLevel` | int | `EnumValue<AlertLevel>` |

共 4 个字段需迁移。迁移后前端需适配，读取 `alertType.code` 而非直接读 `alertType`。

### 8.6 Entity -> Request/Response DTO 重命名

当前 Controller 直接返回 Entity 对象（`IotTelemetry`、`IotAlert`），不符合 CONVENTIONS.md DTO 命名约定。

| Controller 方法 | 当前返回 | 目标 Request | 目标 Response |
|----------------|---------|-------------|--------------|
| ingest | Integer | `List<TelemetryRequest>`（已有 TelemetryDTO） | 无（返回 R\<Integer\>） |
| latest | IotTelemetry | 无 | `TelemetryResponse` |
| series | List\<IotTelemetry\> | `TelemetrySeriesRequest` | `List<TelemetryResponse>` |
| openAlerts | List\<IotAlert\> | 无 | `List<IotAlertResponse>` |

**迁移风险**：
- Entity 字段 `createTime`/`updateTime`（BaseEntity 继承）需映射为 Response 的 `createdAt`/`updatedAt`。
- `TelemetryDTO` 命名不符合约定（禁用 `DTO` 后缀），但仅用于入库不暴露到 API，可保留。

### 8.7 未暴露为 HTTP 端点的内部能力

| 能力 | 方法 | 说明 |
|------|------|------|
| 离线扫描 | `AlertEngineService.sweepOfflineDevices()` | 定时任务调用，扫描离线设备并产生 OFFLINE 告警 |
| 告警评估（纯函数） | `AlertEngineService.evaluate()` | 不持久化，可用于单元测试 |
| 超温计数 | `IotTelemetryMapper.countOverTempSince()` | 统计某设备某时段超温次数 |

### 8.8 跨模块依赖

| 依赖方向 | 说明 | 触发时机 |
|---------|------|---------|
| `iot -> coldchain.md` | 温度传感器数据是冷链模块的核心数据源 | 遥测数据写入后，冷链模块可查询温度曲线 |
| `iot -> trace.md` | IoT 数据（GPS、温度）进入溯源链 | 溯源链记录物流环节的温控数据 |
| `iot -> warehouse.md` | 仓库设备关联 `warehouseId` | 查询仓库绑定的传感器列表 |

### 8.9 权限模型

当前所有端点使用 `@RequirePerm` 注解控制：

| 端点 | 权限 |
|------|------|
| `POST /api/iot/telemetry/batch` | `logistics:write` |
| `GET /api/iot/telemetry/{deviceSn}/latest` | `logistics:read` |
| `GET /api/iot/telemetry/{deviceSn}/series` | `logistics:read` |
| `GET /api/iot/alerts/open` | `logistics:read` |

---

## 质量 Checklist

- [x] 4 个端点全覆盖（1 Controller：IotController）
- [x] 13 个错误码（>=10），全在 900000-999999 范围
- [x] 6 个枚举声明完整（DeviceType / DeviceStatus / AlertType / AlertLevel / DoorStatus / SignalType）
- [x] JSON 示例全用 `message` 字段（禁用 `msg`）
- [x] 告警规则引擎 4 条规则入文档（§5）
- [x] 离线扫描逻辑入文档（§5）
- [x] 告警阈值常量入文档（§5）
- [x] 幂等机制说明（§8.2）
- [x] Mock 数据生成说明（§8.4）
- [x] 4 个 String->EnumValue 迁移清单（§8.5）
- [x] Entity->Request/Response DTO 重命名（§8.6）
- [x] 跨模块依赖（coldchain / trace / warehouse）（§8.8）
- [x] 权限模型说明（§8.9）
- [x] 数据模型 3 张表完整描述（§4）
