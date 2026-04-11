# M6 冷链物流模块 API 文档

> 模块路径：`apple-module-coldchain`
> 基础路径前缀：`/api/coldchain`
> 响应格式：统一使用 `R<T>` 包装，分页使用 `PageResult<T>`

---

## 通用说明

### 统一响应结构

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 分页响应结构（PageResult）

```json
{
  "records": [...],
  "total": 100,
  "size": 10,
  "current": 1,
  "pages": 10
}
```

### 公共实体字段（BaseEntity）

所有实体均继承以下基础字段（雪花 ID + 审计字段 + 软删除）：

| 字段         | 类型            | 说明                    |
|------------|-----------------|-------------------------|
| id         | Long            | 主键（雪花算法自动生成） |
| createTime | LocalDateTime   | 创建时间（自动填充）     |
| updateTime | LocalDateTime   | 更新时间（自动填充）     |
| createBy   | String          | 创建人（自动填充）       |
| deleted    | Integer         | 软删除标记（0=正常，1=已删除） |

---

## 一、车辆管理（VehicleController）

**基础路径**：`/api/coldchain/vehicles`  
**数据表**：`cc_vehicle`

### 实体字段说明（Vehicle）

| 字段           | 类型       | 说明                                               |
|--------------|------------|---------------------------------------------------|
| vehicleCode  | String     | 车辆编码（系统自动生成，格式：VH + yyyyMMdd + 4位序号） |
| plateNumber  | String     | 车牌号                                             |
| vehicleType  | String     | 车辆类型：`REFRIGERATED`（冷藏）/ `INSULATED`（保温）/ `NORMAL`（普通） |
| brand        | String     | 品牌                                               |
| capacity     | BigDecimal | 载重（吨）                                          |
| volume       | BigDecimal | 容积（m³）                                          |
| temperatureMin | BigDecimal | 最低控温（℃）                                     |
| temperatureMax | BigDecimal | 最高控温（℃）                                     |
| driverName   | String     | 驾驶员姓名                                          |
| driverPhone  | String     | 驾驶员电话                                          |
| status       | String     | 状态：`IDLE`（空闲）/ `IN_TRANSIT`（运输中）/ `MAINTENANCE`（维修中）/ `RETIRED`（已退役） |
| remark       | String     | 备注                                               |

---

### 1.1 车辆列表（分页）

**GET** `/api/coldchain/vehicles/list`

**摘要**：分页查询车辆列表，支持按关键词、车辆类型、状态筛选。

**请求参数（Query）**：

| 参数名       | 类型    | 必填 | 默认值 | 说明                                   |
|------------|---------|------|--------|----------------------------------------|
| page       | int     | 否   | 1      | 页码                                   |
| size       | int     | 否   | 10     | 每页条数                               |
| keyword    | String  | 否   | -      | 模糊搜索车牌号或驾驶员姓名            |
| vehicleType| String  | 否   | -      | 车辆类型过滤（REFRIGERATED/INSULATED/NORMAL）|
| status     | String  | 否   | -      | 状态过滤（IDLE/IN_TRANSIT/MAINTENANCE/RETIRED）|

**响应**：`R<PageResult<Vehicle>>`

---

### 1.2 车辆详情

**GET** `/api/coldchain/vehicles/{id}`

**摘要**：根据 ID 查询单辆车详情。

**路径参数**：

| 参数名 | 类型 | 说明    |
|------|------|---------|
| id   | Long | 车辆 ID |

**响应**：`R<Vehicle>`

---

### 1.3 创建车辆

**POST** `/api/coldchain/vehicles`

**摘要**：新增一辆车辆档案。

**请求体（JSON，Vehicle）**：

| 字段           | 类型       | 必填 | 说明                              |
|--------------|------------|------|-----------------------------------|
| plateNumber  | String     | 是   | 车牌号                            |
| vehicleType  | String     | 是   | 车辆类型（REFRIGERATED/INSULATED/NORMAL） |
| brand        | String     | 否   | 品牌                              |
| capacity     | BigDecimal | 否   | 载重（吨）                         |
| volume       | BigDecimal | 否   | 容积（m³）                         |
| temperatureMin | BigDecimal | 否 | 最低控温（℃）                     |
| temperatureMax | BigDecimal | 否 | 最高控温（℃）                     |
| driverName   | String     | 否   | 驾驶员姓名                         |
| driverPhone  | String     | 否   | 驾驶员电话                         |
| remark       | String     | 否   | 备注                              |

**业务规则**：
- 系统自动生成 `vehicleCode`，格式：`VH` + `yyyyMMdd` + 4位序号（如：`VH202604110001`）。
- 初始状态固定为 `IDLE`（空闲）。

**响应**：`R<Vehicle>`

---

### 1.4 更新车辆

**PUT** `/api/coldchain/vehicles/{id}`

**摘要**：修改车辆基础信息。

**路径参数**：

| 参数名 | 类型 | 说明    |
|------|------|---------|
| id   | Long | 车辆 ID |

**请求体（JSON，Vehicle）**：同创建接口字段，`vehicleCode` 字段传入后会被忽略（不允许修改编码）。

**响应**：`R<Vehicle>`

---

### 1.5 删除车辆

**DELETE** `/api/coldchain/vehicles/{id}`

**摘要**：软删除车辆档案。

**路径参数**：

| 参数名 | 类型 | 说明    |
|------|------|---------|
| id   | Long | 车辆 ID |

**业务规则**：
- 状态为 `IN_TRANSIT`（运输中）的车辆**禁止删除**，调用将返回业务异常。

**响应**：`R<Void>`

---

### 1.6 变更车辆状态

**PUT** `/api/coldchain/vehicles/{id}/status`

**摘要**：手动切换车辆状态（维修出入库、退役）。

**路径参数**：

| 参数名 | 类型 | 说明    |
|------|------|---------|
| id   | Long | 车辆 ID |

**请求参数（Query）**：

| 参数名    | 类型   | 必填 | 说明                      |
|---------|--------|------|---------------------------|
| newStatus | String | 是 | 目标状态（见业务规则）     |

**业务规则 - 状态机（手动变更允许的转换）**：

```
IDLE       ──→  MAINTENANCE  （送修）
MAINTENANCE ──→  IDLE        （修复归还）
IDLE       ──→  RETIRED      （退役）
```

注意：`IN_TRANSIT` 状态由运输任务发车 / 送达接口自动驱动，不允许通过本接口变更进入或离开。

**响应**：`R<Vehicle>`

---

### 1.7 导出车辆 CSV

**GET** `/api/coldchain/vehicles/export`

**摘要**：按条件导出车辆档案为 CSV 文件，UTF-8 BOM 编码。

**请求参数（Query）**：

| 参数名       | 类型   | 必填 | 说明         |
|------------|--------|------|--------------|
| keyword    | String | 否   | 关键词筛选   |
| vehicleType| String | 否   | 车辆类型筛选 |
| status     | String | 否   | 状态筛选     |

**响应**：CSV 文件流，`Content-Disposition: attachment; filename=车辆列表.csv`

**CSV 列**：车辆编码、车牌号、车辆类型、品牌、载重(吨)、容积(m³)、最低控温、最高控温、驾驶员、电话、状态

---

## 二、运输任务（TransportTaskController）

**基础路径**：`/api/coldchain/tasks`  
**数据表**：`cc_transport_task`

### 实体字段说明（TransportTask）

| 字段          | 类型          | 说明                                                    |
|-------------|---------------|---------------------------------------------------------|
| taskCode    | String        | 任务编码（系统自动生成，格式：TT + yyyyMMdd + 4位序号） |
| vehicleId   | Long          | 关联车辆 ID                                              |
| orderId     | Long          | 关联订单 ID                                              |
| origin      | String        | 出发地                                                   |
| destination | String        | 目的地                                                   |
| cargoDesc   | String        | 货物描述                                                 |
| cargoWeight | BigDecimal    | 货物重量（kg）                                           |
| requiredTemp| BigDecimal    | 要求运输温度（℃）                                       |
| planDepart  | LocalDateTime | 计划发车时间                                             |
| actualDepart| LocalDateTime | 实际发车时间（发车时自动填写）                           |
| planArrive  | LocalDateTime | 计划到达时间                                             |
| actualArrive| LocalDateTime | 实际到达时间（送达时自动填写）                           |
| distance    | BigDecimal    | 运输距离（km）                                           |
| cost        | BigDecimal    | 运输费用                                                 |
| status      | String        | 状态：`PENDING`（待发车）/ `IN_TRANSIT`（运输中）/ `DELIVERED`（已送达）/ `CANCELLED`（已取消） |
| remark      | String        | 备注                                                     |

---

### 2.1 运输任务列表（分页）

**GET** `/api/coldchain/tasks/list`

**摘要**：分页查询运输任务，支持关键词和状态筛选。

**请求参数（Query）**：

| 参数名   | 类型   | 必填 | 默认值 | 说明                                              |
|--------|--------|------|--------|---------------------------------------------------|
| page   | int    | 否   | 1      | 页码                                              |
| size   | int    | 否   | 10     | 每页条数                                          |
| keyword| String | 否   | -      | 模糊搜索任务编码或货物描述                        |
| status | String | 否   | -      | 状态过滤（PENDING/IN_TRANSIT/DELIVERED/CANCELLED）|

**响应**：`R<PageResult<TransportTask>>`

---

### 2.2 运输任务详情

**GET** `/api/coldchain/tasks/{id}`

**摘要**：查询单个运输任务详情。

**路径参数**：

| 参数名 | 类型 | 说明      |
|------|------|-----------|
| id   | Long | 任务 ID   |

**响应**：`R<TransportTask>`

---

### 2.3 创建运输任务

**POST** `/api/coldchain/tasks`

**摘要**：创建新的运输任务。

**请求体（JSON，TransportTask）**：

| 字段          | 类型          | 必填 | 说明                   |
|-------------|---------------|------|------------------------|
| vehicleId   | Long          | 否   | 关联车辆 ID            |
| orderId     | Long          | 否   | 关联订单 ID            |
| origin      | String        | 是   | 出发地                 |
| destination | String        | 是   | 目的地                 |
| cargoDesc   | String        | 否   | 货物描述               |
| cargoWeight | BigDecimal    | 否   | 货物重量（kg）         |
| requiredTemp| BigDecimal    | 否   | 要求温度（℃）          |
| planDepart  | LocalDateTime | 否   | 计划发车时间           |
| planArrive  | LocalDateTime | 否   | 计划到达时间           |
| distance    | BigDecimal    | 否   | 预估距离（km）         |
| cost        | BigDecimal    | 否   | 费用                   |
| remark      | String        | 否   | 备注                   |

**业务规则**：
- 系统自动生成 `taskCode`，格式：`TT` + `yyyyMMdd` + 4位序号。
- 初始状态固定为 `PENDING`（待发车）。

**响应**：`R<TransportTask>`

---

### 2.4 更新运输任务

**PUT** `/api/coldchain/tasks/{id}`

**摘要**：更新运输任务信息。

**路径参数**：

| 参数名 | 类型 | 说明    |
|------|------|---------|
| id   | Long | 任务 ID |

**请求体**：同创建接口，`taskCode` 传入后被忽略。

**业务规则**：
- 状态为 `DELIVERED`（已送达）的任务**禁止修改**。

**响应**：`R<TransportTask>`

---

### 2.5 发车

**POST** `/api/coldchain/tasks/{id}/depart`

**摘要**：触发运输任务发车，自动记录实际发车时间，并将关联车辆置为运输中。

**路径参数**：

| 参数名 | 类型 | 说明    |
|------|------|---------|
| id   | Long | 任务 ID |

**业务规则**：
- 前置状态必须为 `PENDING`，否则报业务异常。
- 发车后任务状态变为 `IN_TRANSIT`，`actualDepart` 自动记录为当前时间。
- **联动操作**：关联车辆状态同步变更为 `IN_TRANSIT`。

**状态转换**：`PENDING` → `IN_TRANSIT`

**响应**：`R<TransportTask>`

---

### 2.6 确认送达

**POST** `/api/coldchain/tasks/{id}/deliver`

**摘要**：确认货物送达目的地，自动记录实际到达时间，并将关联车辆释放为空闲。

**路径参数**：

| 参数名 | 类型 | 说明    |
|------|------|---------|
| id   | Long | 任务 ID |

**业务规则**：
- 前置状态必须为 `IN_TRANSIT`，否则报业务异常。
- 送达后任务状态变为 `DELIVERED`，`actualArrive` 自动记录为当前时间。
- **联动操作**：关联车辆状态同步恢复为 `IDLE`。

**状态转换**：`IN_TRANSIT` → `DELIVERED`

**响应**：`R<TransportTask>`

---

### 2.7 取消任务

**POST** `/api/coldchain/tasks/{id}/cancel`

**摘要**：取消尚未发车的运输任务。

**路径参数**：

| 参数名 | 类型 | 说明    |
|------|------|---------|
| id   | Long | 任务 ID |

**业务规则**：
- 前置状态必须为 `PENDING`，否则报业务异常（运输中的任务无法取消）。

**状态转换**：`PENDING` → `CANCELLED`

**响应**：`R<TransportTask>`

---

### 2.8 删除运输任务

**DELETE** `/api/coldchain/tasks/{id}`

**摘要**：软删除运输任务。

**路径参数**：

| 参数名 | 类型 | 说明    |
|------|------|---------|
| id   | Long | 任务 ID |

**业务规则**：
- 状态为 `IN_TRANSIT`（运输中）的任务**禁止删除**。

**响应**：`R<Void>`

---

### 2.9 导出运输任务 CSV

**GET** `/api/coldchain/tasks/export`

**摘要**：按条件导出运输任务为 CSV 文件，UTF-8 BOM 编码。

**请求参数（Query）**：

| 参数名   | 类型   | 必填 | 说明         |
|--------|--------|------|--------------|
| keyword| String | 否   | 关键词筛选   |
| status | String | 否   | 状态筛选     |

**响应**：CSV 文件流，`Content-Disposition: attachment; filename=运输任务.csv`

**CSV 列**：任务编码、出发地、目的地、货物描述、重量(kg)、要求温度、计划发车、实际发车、计划到达、实际到达、距离(km)、费用、状态

---

### 运输任务完整状态机

```
创建
  │
  ▼
PENDING（待发车）
  │  \
  │   └──→ CANCELLED（已取消）  [取消接口]
  │
  ▼ [发车接口 → 车辆同步 IN_TRANSIT]
IN_TRANSIT（运输中）
  │
  ▼ [送达接口 → 车辆同步 IDLE]
DELIVERED（已送达）
```

---

## 三、配送管理（DeliveryController）

**基础路径**：`/api/coldchain/deliveries`  
**数据表**：`cc_delivery`

### 实体字段说明（Delivery）

| 字段          | 类型          | 说明                                                           |
|-------------|---------------|----------------------------------------------------------------|
| deliveryCode | String       | 配送编码（系统自动生成，格式：DL + yyyyMMdd + 4位序号）        |
| taskId      | Long          | 关联运输任务 ID                                                 |
| receiverName| String        | 收货人姓名                                                      |
| receiverPhone| String       | 收货人电话                                                      |
| receiverAddr| String        | 收货地址                                                        |
| deliveryTime| LocalDateTime | 配送时间                                                        |
| signTime    | LocalDateTime | 签收时间（签收时自动填写）                                      |
| signPhoto   | String        | 签收照片 URL                                                    |
| qualityCheck| String        | 质检结果：`PENDING`（待检）/ `PASSED`（通过）/ `REJECTED`（不通过） |
| qualityRemark| String       | 质检备注                                                        |
| status      | String        | 状态：`PENDING`（待配送）/ `DELIVERING`（配送中）/ `SIGNED`（已签收）/ `REJECTED`（已拒收） |
| remark      | String        | 备注                                                            |

---

### 3.1 配送列表（分页）

**GET** `/api/coldchain/deliveries/list`

**摘要**：分页查询配送记录，支持关键词和状态筛选。

**请求参数（Query）**：

| 参数名   | 类型   | 必填 | 默认值 | 说明                                                      |
|--------|--------|------|--------|-----------------------------------------------------------|
| page   | int    | 否   | 1      | 页码                                                      |
| size   | int    | 否   | 10     | 每页条数                                                  |
| keyword| String | 否   | -      | 模糊搜索配送编码或收货人姓名                              |
| status | String | 否   | -      | 状态过滤（PENDING/DELIVERING/SIGNED/REJECTED）            |

**响应**：`R<PageResult<Delivery>>`

---

### 3.2 配送详情

**GET** `/api/coldchain/deliveries/{id}`

**摘要**：查询单个配送记录详情。

**路径参数**：

| 参数名 | 类型 | 说明      |
|------|------|-----------|
| id   | Long | 配送 ID   |

**响应**：`R<Delivery>`

---

### 3.3 创建配送

**POST** `/api/coldchain/deliveries`

**摘要**：为运输任务创建配送单。

**请求体（JSON，Delivery）**：

| 字段          | 类型          | 必填 | 说明              |
|-------------|---------------|------|-------------------|
| taskId      | Long          | 是   | 关联运输任务 ID   |
| receiverName| String        | 是   | 收货人姓名        |
| receiverPhone| String       | 否   | 收货人电话        |
| receiverAddr| String        | 是   | 收货地址          |
| deliveryTime| LocalDateTime | 否   | 预计配送时间      |
| remark      | String        | 否   | 备注              |

**业务规则**：
- 系统自动生成 `deliveryCode`，格式：`DL` + `yyyyMMdd` + 4位序号。
- 初始状态固定为 `PENDING`（待配送）。
- 初始质检状态固定为 `PENDING`（待检）。

**响应**：`R<Delivery>`

---

### 3.4 更新配送

**PUT** `/api/coldchain/deliveries/{id}`

**摘要**：更新配送信息。

**路径参数**：

| 参数名 | 类型 | 说明      |
|------|------|-----------|
| id   | Long | 配送 ID   |

**请求体**：同创建接口，`deliveryCode` 传入后被忽略。

**业务规则**：
- 状态为 `SIGNED`（已签收）的配送**禁止修改**。

**响应**：`R<Delivery>`

---

### 3.5 签收确认

**POST** `/api/coldchain/deliveries/{id}/sign`

**摘要**：完成签收操作，记录签收时间与质检结果。

**路径参数**：

| 参数名 | 类型 | 说明      |
|------|------|-----------|
| id   | Long | 配送 ID   |

**请求体（JSON，Delivery 部分字段）**：

| 字段          | 类型   | 必填 | 说明                                              |
|-------------|--------|------|---------------------------------------------------|
| qualityCheck| String | 否   | 质检结果（PASSED/REJECTED），不传默认为 `PASSED`  |
| qualityRemark| String| 否   | 质检备注（不合格时建议填写原因）                  |
| signPhoto   | String | 否   | 签收照片 URL                                      |

**业务规则**：
- 已处于 `SIGNED` 状态的配送**禁止重复签收**。
- `signTime` 自动记录为当前时间。
- `qualityCheck` 未传时默认设为 `PASSED`。

**状态转换**：`DELIVERING` / `PENDING` → `SIGNED`

**响应**：`R<Delivery>`

---

### 3.6 删除配送

**DELETE** `/api/coldchain/deliveries/{id}`

**摘要**：软删除配送记录。

**路径参数**：

| 参数名 | 类型 | 说明      |
|------|------|-----------|
| id   | Long | 配送 ID   |

**业务规则**：
- 状态为 `SIGNED`（已签收）的配送**禁止删除**。

**响应**：`R<Void>`

---

### 3.7 导出配送 CSV

**GET** `/api/coldchain/deliveries/export`

**摘要**：按条件导出配送记录为 CSV 文件，UTF-8 BOM 编码。

**请求参数（Query）**：

| 参数名   | 类型   | 必填 | 说明         |
|--------|--------|------|--------------|
| keyword| String | 否   | 关键词筛选   |
| status | String | 否   | 状态筛选     |

**响应**：CSV 文件流，`Content-Disposition: attachment; filename=配送记录.csv`

**CSV 列**：配送编码、运输任务ID、收货人、电话、地址、配送时间、签收时间、质检状态、状态

---

### 配送状态机

```
创建
  │
  ▼
PENDING（待配送）
  │
  ▼
DELIVERING（配送中）
  │      \
  │       └──→ REJECTED（已拒收）
  ▼
SIGNED（已签收）  [签收接口，质检结果同时录入]
```

---

## 四、预冷任务（PreCoolTaskController）

**基础路径**：`/api/coldchain/precool`  
**数据表**：`cc_pre_cool_task`

### 实体字段说明（PreCoolTask）

| 字段        | 类型          | 说明                                                            |
|-----------|---------------|-----------------------------------------------------------------|
| taskNo    | String        | 任务编号（系统自动生成，格式：PC + yyyyMMdd + 4位序号）         |
| vehicleId | Long          | 关联车辆 ID                                                      |
| batchCode | String        | 批次编码（关联苹果采购/入库批次）                                |
| startTemp | BigDecimal    | 初始温度（℃）                                                   |
| targetTemp| BigDecimal    | 目标温度（℃），默认 2.0℃                                        |
| startTime | LocalDateTime | 开始冷却时间（开始操作时自动填写）                               |
| endTime   | LocalDateTime | 结束时间（完成操作时自动填写）                                   |
| duration  | Integer       | 实际冷却时长（分钟，完成时自动计算）                             |
| status    | String        | 状态：`PENDING`（待执行）/ `COOLING`（冷却中）/ `COMPLETED`（已完成）/ `FAILED`（失败） |
| operator  | String        | 操作人                                                           |
| remark    | String        | 备注                                                             |

---

### 4.1 预冷任务列表（分页）

**GET** `/api/coldchain/precool`

**摘要**：分页查询预冷任务列表，支持按车辆 ID 和状态筛选。

**请求参数（Query）**：

| 参数名    | 类型   | 必填 | 默认值 | 说明                                                |
|---------|--------|------|--------|-----------------------------------------------------|
| page    | int    | 否   | 1      | 页码                                                |
| size    | int    | 否   | 10     | 每页条数                                            |
| vehicleId| Long  | 否   | -      | 按车辆 ID 过滤                                      |
| status  | String | 否   | -      | 状态过滤（PENDING/COOLING/COMPLETED/FAILED）        |

**响应**：`R<PageResult<PreCoolTask>>`

---

### 4.2 预冷任务详情

**GET** `/api/coldchain/precool/{id}`

**摘要**：查询单个预冷任务详情。

**路径参数**：

| 参数名 | 类型 | 说明        |
|------|------|-------------|
| id   | Long | 预冷任务 ID |

**响应**：`R<PreCoolTask>`

---

### 4.3 创建预冷任务

**POST** `/api/coldchain/precool`

**摘要**：为指定车辆创建一次预冷任务。

**请求体（JSON，PreCoolTask）**：

| 字段        | 类型       | 必填 | 说明                                   |
|-----------|------------|------|----------------------------------------|
| vehicleId | Long       | 是   | 关联车辆 ID                            |
| batchCode | String     | 否   | 批次编码                               |
| startTemp | BigDecimal | 否   | 初始温度（℃）                          |
| targetTemp| BigDecimal | 否   | 目标温度（℃），不填默认 2.0℃          |
| operator  | String     | 否   | 操作人                                 |
| remark    | String     | 否   | 备注                                   |

**业务规则**：
- 系统自动生成 `taskNo`，格式：`PC` + `yyyyMMdd` + 4位序号。
- 初始状态固定为 `PENDING`（待执行）。
- `targetTemp` 不填时默认为 `2.0℃`。

**响应**：`R<PreCoolTask>`

---

### 4.4 开始冷却

**PUT** `/api/coldchain/precool/{id}/start`

**摘要**：启动预冷任务，开始冷却计时。

**路径参数**：

| 参数名 | 类型 | 说明        |
|------|------|-------------|
| id   | Long | 预冷任务 ID |

**业务规则**：
- 前置状态必须为 `PENDING`，否则报业务异常。
- `startTime` 自动记录为当前时间。

**状态转换**：`PENDING` → `COOLING`

**响应**：`R<PreCoolTask>`

---

### 4.5 完成冷却

**PUT** `/api/coldchain/precool/{id}/complete`

**摘要**：完成预冷任务，自动计算冷却时长。

**路径参数**：

| 参数名 | 类型 | 说明        |
|------|------|-------------|
| id   | Long | 预冷任务 ID |

**业务规则**：
- 前置状态必须为 `COOLING`，否则报业务异常。
- `endTime` 自动记录为当前时间。
- `duration`（冷却时长，单位分钟）自动计算为 `endTime - startTime`。

**状态转换**：`COOLING` → `COMPLETED`

**响应**：`R<PreCoolTask>`

---

### 预冷任务状态机

```
创建
  │
  ▼
PENDING（待执行）
  │
  ▼ [开始冷却接口，自动记录 startTime]
COOLING（冷却中）
  │
  ▼ [完成冷却接口，自动计算 duration]
COMPLETED（已完成）

注：FAILED 状态目前保留，暂无系统自动触发路径
```

**删除限制**：
- `COOLING`（冷却中）状态的预冷任务**禁止删除**。
- `COMPLETED`（已完成）或 `COOLING`（冷却中）状态的任务**禁止修改**。

---

## 五、温度监控（TemperatureRecordController）

**基础路径**：`/api/coldchain/temperatures`  
**数据表**：`cc_temperature_record`

### 实体字段说明（TemperatureRecord）

| 字段        | 类型          | 说明                                                  |
|-----------|---------------|-------------------------------------------------------|
| taskId    | Long          | 关联运输任务 ID                                        |
| vehicleId | Long          | 关联车辆 ID                                            |
| temperature| BigDecimal   | 实测温度（℃）                                          |
| humidity  | BigDecimal    | 实测湿度（%）                                          |
| location  | String        | 采集位置或传感器标识                                   |
| recordTime| LocalDateTime | 记录时间                                               |
| isAlarm   | Integer       | 是否报警：`0`=正常，`1`=报警                           |
| alarmMsg  | String        | 报警消息（系统自动生成，如"温度异常: 8.5℃，要求范围 [0.0, 4.0]℃"） |

---

### 5.1 温度记录列表（分页）

**GET** `/api/coldchain/temperatures/list`

**摘要**：分页查询温度监控记录，支持按任务 ID 和是否报警筛选。

**请求参数（Query）**：

| 参数名   | 类型    | 必填 | 默认值 | 说明                             |
|--------|---------|------|--------|----------------------------------|
| page   | int     | 否   | 1      | 页码                             |
| size   | int     | 否   | 10     | 每页条数                         |
| taskId | Long    | 否   | -      | 按运输任务 ID 过滤               |
| isAlarm| Integer | 否   | -      | 0=查正常记录，1=查报警记录       |

**响应**：`R<PageResult<TemperatureRecord>>`，按 `recordTime` 降序排列

---

### 5.2 按任务查询温度记录

**GET** `/api/coldchain/temperatures/by-task`

**摘要**：查询指定运输任务的完整温度曲线数据（全量不分页，按时间升序）。

**请求参数（Query）**：

| 参数名  | 类型 | 必填 | 说明            |
|-------|------|------|-----------------|
| taskId| Long | 是   | 运输任务 ID     |

**响应**：`R<List<TemperatureRecord>>`，按 `recordTime` 升序排列，适合绘制温度折线图

---

### 5.3 新增温度记录

**POST** `/api/coldchain/temperatures`

**摘要**：上报一条温度采集记录，系统自动判断是否触发报警。

**请求体（JSON，TemperatureRecord）**：

| 字段        | 类型          | 必填 | 说明                         |
|-----------|---------------|------|------------------------------|
| taskId    | Long          | 是   | 关联运输任务 ID              |
| vehicleId | Long          | 否   | 关联车辆 ID                  |
| temperature| BigDecimal   | 是   | 实测温度（℃）                |
| humidity  | BigDecimal    | 否   | 实测湿度（%）                |
| location  | String        | 否   | 采集位置                     |
| recordTime| LocalDateTime | 否   | 记录时间（不填时可使用系统时间） |

**业务规则 - 自动报警逻辑**：

1. 若 `taskId` 对应的运输任务设置了 `requiredTemp`（要求温度），则报警区间为：
   - **下限** = `requiredTemp - 2.0`
   - **上限** = `requiredTemp + 2.0`
2. 若运输任务未设置 `requiredTemp`，则使用默认区间：`[0℃, 4℃]`
3. 若实测温度超出上述区间，系统自动将 `isAlarm` 置为 `1`，并生成 `alarmMsg`，格式示例：
   > `温度异常: 8.5℃，要求范围 [0.0, 4.0]℃`

**响应**：`R<TemperatureRecord>`

---

### 5.4 删除温度记录

**DELETE** `/api/coldchain/temperatures/{id}`

**摘要**：删除单条温度记录（物理删除）。

**路径参数**：

| 参数名 | 类型 | 说明          |
|------|------|---------------|
| id   | Long | 温度记录 ID   |

**响应**：`R<Void>`

---

## 六、运输统计（TransportStatisticsController）

**基础路径**：`/api/coldchain/statistics`

---

### 6.1 统计概览

**GET** `/api/coldchain/statistics/summary`

**摘要**：获取冷链系统整体运营概览数据，包含任务数量、车辆状态分布和报警总数。

**请求参数**：无

**响应**：`R<Map<String, Object>>`

**响应数据结构**：

| 字段            | 类型               | 说明                                         |
|---------------|--------------------|----------------------------------------------|
| totalTasks    | Long               | 运输任务总数                                 |
| taskByStatus  | Map<String, Long>  | 按状态分组的任务数量（PENDING/IN_TRANSIT/DELIVERED/CANCELLED） |
| totalVehicles | Long               | 车辆总数                                     |
| vehicleByStatus| Map<String, Long> | 按状态分组的车辆数量（IDLE/IN_TRANSIT/MAINTENANCE/RETIRED） |
| totalAlarms   | Long               | 温度报警总次数（历史累计）                   |

**响应示例**：

```json
{
  "code": 200,
  "data": {
    "totalTasks": 158,
    "taskByStatus": {
      "PENDING": 12,
      "IN_TRANSIT": 5,
      "DELIVERED": 138,
      "CANCELLED": 3
    },
    "totalVehicles": 20,
    "vehicleByStatus": {
      "IDLE": 14,
      "IN_TRANSIT": 5,
      "MAINTENANCE": 1,
      "RETIRED": 0
    },
    "totalAlarms": 47
  }
}
```

---

### 6.2 近 N 天报警统计

**GET** `/api/coldchain/statistics/alarms`

**摘要**：查询近 N 天内的温度报警记录，按任务维度汇总报警次数。

**请求参数（Query）**：

| 参数名 | 类型 | 必填 | 默认值 | 说明               |
|------|------|------|--------|--------------------|
| days | int  | 否   | 30     | 统计周期（天数）   |

**响应**：`R<Map<String, Object>>`

**响应数据结构**：

| 字段         | 类型               | 说明                                                |
|------------|--------------------|----------------------------------------------------|
| days       | Integer            | 统计周期（天数）                                    |
| since      | LocalDateTime      | 统计起始时间                                        |
| totalAlarms| Long               | 周期内报警总次数                                    |
| alarmByTask| Map<String, Long>  | 按任务 ID 分组的报警次数（key 为 taskId 字符串形式） |
| records    | List\<TemperatureRecord\> | 周期内全部报警记录明细，按记录时间倒序            |

**响应示例**：

```json
{
  "code": 200,
  "data": {
    "days": 30,
    "since": "2026-03-12T10:00:00",
    "totalAlarms": 15,
    "alarmByTask": {
      "1001": 8,
      "1002": 4,
      "1003": 3
    },
    "records": [...]
  }
}
```

---

## 七、物流全链路查询（LogisticsQueryController）

**基础路径**：`/api/coldchain/logistics`

---

### 7.1 获取任务完整物流信息

**GET** `/api/coldchain/logistics/{taskId}/full`

**摘要**：一次性获取指定运输任务的全链路物流信息，聚合任务详情、车辆信息、温度记录及配送单。

**路径参数**：

| 参数名  | 类型 | 说明        |
|-------|------|-------------|
| taskId| Long | 运输任务 ID |

**响应**：`R<LogisticsFullVO>`

**响应结构（LogisticsFullVO）**：

| 字段         | 类型                       | 说明                                           |
|------------|----------------------------|------------------------------------------------|
| task       | TransportTask              | 运输任务完整信息                               |
| vehicle    | Vehicle                    | 关联车辆信息（若任务未绑定车辆则为 null）      |
| tempRecords| List\<TemperatureRecord\>  | 该任务全部温度记录，按记录时间升序排列         |
| delivery   | Delivery                   | 最新配送单信息（按创建时间取最新一条，可为 null）|
| alarmCount | long                       | 温度报警次数（从 tempRecords 中统计 isAlarm=1 的数量） |

**响应示例**：

```json
{
  "code": 200,
  "data": {
    "task": {
      "id": 1001,
      "taskCode": "TT202604110001",
      "origin": "陕西西安",
      "destination": "北京朝阳",
      "status": "DELIVERED",
      ...
    },
    "vehicle": {
      "id": 5,
      "plateNumber": "陕A12345",
      "vehicleType": "REFRIGERATED",
      ...
    },
    "tempRecords": [...],
    "delivery": {
      "deliveryCode": "DL202604110001",
      "status": "SIGNED",
      ...
    },
    "alarmCount": 2
  }
}
```

---

## 附录：状态枚举汇总

### 车辆状态（Vehicle.status）

| 枚举值      | 含义     | 说明                         |
|-----------|----------|------------------------------|
| IDLE        | 空闲   | 可接受运输任务               |
| IN_TRANSIT  | 运输中 | 由运输任务发车/送达接口自动驱动 |
| MAINTENANCE | 维修中 | 手动变更状态接口驱动         |
| RETIRED     | 已退役 | 手动变更，终态               |

### 运输任务状态（TransportTask.status）

| 枚举值     | 含义   | 触发接口         |
|----------|--------|------------------|
| PENDING    | 待发车 | 创建时初始化     |
| IN_TRANSIT | 运输中 | `/depart`        |
| DELIVERED  | 已送达 | `/deliver`       |
| CANCELLED  | 已取消 | `/cancel`        |

### 配送状态（Delivery.status）

| 枚举值     | 含义   |
|----------|--------|
| PENDING    | 待配送 |
| DELIVERING | 配送中 |
| SIGNED     | 已签收 |
| REJECTED   | 已拒收 |

### 配送质检状态（Delivery.qualityCheck）

| 枚举值   | 含义   |
|---------|--------|
| PENDING  | 待检验 |
| PASSED   | 质检通过 |
| REJECTED | 质检不通过 |

### 预冷任务状态（PreCoolTask.status）

| 枚举值    | 含义   | 触发接口          |
|---------|--------|-------------------|
| PENDING   | 待执行 | 创建时初始化      |
| COOLING   | 冷却中 | `/start`          |
| COMPLETED | 已完成 | `/complete`       |
| FAILED    | 失败   | 预留，暂无自动触发 |

### 温度报警标记（TemperatureRecord.isAlarm）

| 值 | 含义   |
|----|--------|
| 0  | 温度正常 |
| 1  | 温度报警 |
