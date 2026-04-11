# M5 仓储管理模块 API 文档

> 生成日期：2026-04-11
> 模块路径：`apple-module-warehouse`
> 基础路径：`/api/warehouse`

---

## 目录

1. [实体字段说明](#实体字段说明)
2. [仓库管理（WarehouseController）](#仓库管理)
3. [智能仓单（WarehouseReceiptController）](#智能仓单)
4. [出入库记录（WarehouseRecordController）](#出入库记录)
5. [仓储统计（WarehouseStatisticsController）](#仓储统计)
6. [状态机说明](#状态机说明)
7. [通用响应结构](#通用响应结构)

---

## 实体字段说明

### BaseEntity（所有实体继承）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `Long` | 雪花算法主键（自动生成） |
| `createTime` | `LocalDateTime` | 创建时间（自动填充） |
| `updateTime` | `LocalDateTime` | 更新时间（自动填充） |
| `createBy` | `String` | 创建人（自动填充） |
| `deleted` | `Integer` | 逻辑删除标记（0=正常，1=已删除） |

---

### Warehouse（仓库）

表名：`wh_warehouse`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `warehouseCode` | `String` | 自动生成 | 仓库编码，格式 `WH{yyyyMMdd}{4位序号}`，如 `WH202604110001` |
| `name` | `String` | 是 | 仓库名称 |
| `type` | `String` | 是 | 仓库类型：`NORMAL`（普通库）/ `COLD`（冷库）/ `ATMOSPHERE`（气调库） |
| `location` | `String` | 是 | 地理位置 |
| `capacity` | `BigDecimal` | 是 | 总容量（吨） |
| `usedCapacity` | `BigDecimal` | 自动 | 已用容量（吨），创建时自动置 0 |
| `temperature` | `BigDecimal` | 否 | 当前温度（℃） |
| `humidity` | `BigDecimal` | 否 | 当前湿度（%） |
| `manager` | `String` | 否 | 仓库管理员姓名 |
| `phone` | `String` | 否 | 联系电话 |
| `status` | `String` | 自动 | 状态（见状态机说明），创建时自动置 `ACTIVE` |
| `remark` | `String` | 否 | 备注 |

---

### WarehouseReceipt（仓单）

表名：`wh_receipt`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `receiptNo` | `String` | 自动生成 | 仓单编号，格式 `WR{yyyyMMdd}{4位序号}` |
| `warehouseId` | `Long` | 是 | 关联仓库 ID（FK `wh_warehouse.id`） |
| `warehouseName` | `String` | 自动 | 仓库名称（冗余，自动从仓库表读取） |
| `farmerId` | `Long` | 否 | 货主（果农）ID |
| `farmerName` | `String` | 否 | 货主姓名 |
| `batchCode` | `String` | 否 | 关联种植批次编码 |
| `variety` | `String` | 否 | 苹果品种 |
| `grade` | `String` | 否 | 质量等级（如 A / B / C） |
| `quantity` | `BigDecimal` | 是 | 存储数量（kg） |
| `unitValue` | `BigDecimal` | 否 | 每公斤估值（元/kg） |
| `totalValue` | `BigDecimal` | 自动 | 总估值（元），`unitValue × quantity` 自动计算 |
| `inboundDate` | `LocalDate` | 否 | 入库日期 |
| `validUntil` | `LocalDate` | 否 | 仓单有效期截止日 |
| `traceCode` | `String` | 否 | 区块链溯源码 |
| `status` | `String` | 自动 | 仓单状态，创建时自动置 `VALID`（见状态机说明） |
| `remark` | `String` | 否 | 备注 |
| `statusChangeBy` | `String` | 自动 | 状态变更操作人 |
| `statusChangeTime` | `LocalDateTime` | 自动 | 状态变更时间 |
| `statusChangeReason` | `String` | 否 | 状态变更原因 |

---

### WarehouseRecord（出入库记录）

表名：`wh_record`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `recordNo` | `String` | 自动生成 | 记录编号，格式 `WR{yyyyMMdd}{4位序号}` |
| `warehouseId` | `Long` | 是 | 关联仓库 ID（FK `wh_warehouse.id`） |
| `warehouseName` | `String` | 自动 | 仓库名称（冗余，自动填充） |
| `recordType` | `String` | 是 | 记录类型：`INBOUND`（入库）/ `OUTBOUND`（出库） |
| `batchCode` | `String` | 否 | 关联种植批次编码 |
| `variety` | `String` | 否 | 苹果品种 |
| `grade` | `String` | 否 | 质量等级（A / B / C） |
| `quantity` | `BigDecimal` | 是 | 操作数量（kg） |
| `temperature` | `BigDecimal` | 否 | 操作时温度（℃），未提供时系统按仓库类型自动模拟 |
| `humidity` | `BigDecimal` | 否 | 操作时湿度（%），未提供时系统自动模拟（60%~95%） |
| `operator` | `String` | 否 | 操作人姓名 |
| `recordDate` | `LocalDate` | 否 | 操作日期，未提供时取当天 |
| `traceCode` | `String` | 否 | 区块链溯源码 |
| `remark` | `String` | 否 | 备注 |

---

### WarehouseStatisticsVO（仓储统计视图对象）

**WarehouseStatisticsVO（概览）**

| 字段 | 类型 | 说明 |
|------|------|------|
| `totalWarehouses` | `int` | 仓库总数 |
| `totalCapacity` | `BigDecimal` | 全部仓库总容量（吨） |
| `totalUsed` | `BigDecimal` | 全部仓库已用容量（吨） |
| `utilizationRate` | `BigDecimal` | 整体利用率（%），`totalUsed / totalCapacity × 100` |
| `warehousesByType` | `Map<String, Long>` | 按类型分组计数，key 为 `NORMAL` / `COLD` / `ATMOSPHERE` |
| `warehousesByStatus` | `Map<String, Long>` | 按状态分组计数，key 为 `ACTIVE` / `MAINTENANCE` / `FULL` / `CLOSED` |

**TurnoverVO（周转率）**

| 字段 | 类型 | 说明 |
|------|------|------|
| `warehouseId` | `Long` | 查询的仓库 ID（全局查询时为 null） |
| `totalInbound` | `BigDecimal` | 周期内入库总量（kg） |
| `totalOutbound` | `BigDecimal` | 周期内出库总量（kg） |
| `turnoverRate` | `BigDecimal` | 周转率，`outbound / ((inbound + outbound) / 2)`，四舍五入 4 位小数 |
| `recordCount` | `int` | 周期内记录总条数 |

**LossVO（损耗率）**

| 字段 | 类型 | 说明 |
|------|------|------|
| `totalInbound` | `BigDecimal` | 周期内入库总量（kg） |
| `totalOutbound` | `BigDecimal` | 周期内出库总量（kg） |
| `difference` | `BigDecimal` | 差值 `inbound - outbound`（kg），正值表示库内留存或损耗 |
| `lossRate` | `BigDecimal` | 损耗率（%），`(inbound - outbound) / inbound × 100` |

---

## 仓库管理

**Controller**：`WarehouseController`
**Base Path**：`/api/warehouse/warehouses`

---

### 1. 仓库列表（分页）

```
GET /api/warehouse/warehouses/list
```

**摘要**：按条件分页查询仓库，按创建时间倒序排列。

**Query 参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | 否 | `1` | 页码 |
| `size` | `int` | 否 | `10` | 每页条数 |
| `keyword` | `String` | 否 | — | 仓库名称模糊匹配 |
| `type` | `String` | 否 | — | 仓库类型精确匹配：`NORMAL` / `COLD` / `ATMOSPHERE` |
| `status` | `String` | 否 | — | 状态精确匹配：`ACTIVE` / `MAINTENANCE` / `FULL` / `CLOSED` |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 25,
    "size": 10,
    "current": 1,
    "records": [
      {
        "id": 1,
        "warehouseCode": "WH202604110001",
        "name": "陕西果业冷链一号库",
        "type": "COLD",
        "location": "陕西省延安市洛川县",
        "capacity": 500.00,
        "usedCapacity": 320.00,
        "temperature": 2.5,
        "humidity": 85.0,
        "manager": "张三",
        "phone": "13900000001",
        "status": "ACTIVE",
        "createTime": "2026-04-11T08:00:00"
      }
    ]
  }
}
```

---

### 2. 仓库详情

```
GET /api/warehouse/warehouses/{id}
```

**摘要**：查询单个仓库的完整信息。

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `Long` | 是 | 仓库 ID |

**响应**：返回单个 `Warehouse` 对象，字段同实体字段说明。

**错误场景**：仓库不存在时返回 `NOT_FOUND` 错误。

---

### 3. 创建仓库

```
POST /api/warehouse/warehouses
Content-Type: application/json
```

**摘要**：新增一条仓库记录，系统自动生成编码并初始化状态。

**请求体**（JSON，必填字段见实体字段说明）

```json
{
  "name": "气调保鲜库一号",
  "type": "ATMOSPHERE",
  "location": "陕西省咸阳市礼泉县",
  "capacity": 200.00,
  "manager": "李四",
  "phone": "13900000002",
  "remark": "新建气调库"
}
```

**业务规则**
- `warehouseCode` 自动生成，格式 `WH{yyyyMMdd}{0001}`，基于当天已有序号自增。
- `usedCapacity` 自动初始化为 `0`。
- `status` 自动设为 `ACTIVE`，不可在创建时手动指定。

**响应**：返回含自动生成字段的完整 `Warehouse` 对象。

---

### 4. 更新仓库

```
PUT /api/warehouse/warehouses/{id}
Content-Type: application/json
```

**摘要**：修改仓库基本信息（名称、位置、容量、环境参数等）。

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `Long` | 是 | 仓库 ID |

**请求体**：同创建接口，`warehouseCode` 字段即使传入也会被忽略。

**业务规则**
- 状态为 `CLOSED` 的仓库**不允许修改**，调用将返回业务异常。
- `warehouseCode` 不可修改（服务层自动清除该字段）。

---

### 5. 删除仓库

```
DELETE /api/warehouse/warehouses/{id}
```

**摘要**：逻辑删除仓库记录。

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `Long` | 是 | 仓库 ID |

**业务规则**
- `usedCapacity > 0` 时（仓库内尚有库存）**禁止删除**，系统返回业务异常：`仓库尚有库存，不能删除`。

**响应**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 6. 变更仓库状态

```
PUT /api/warehouse/warehouses/{id}/status
Content-Type: application/json
```

**摘要**：手动变更仓库运营状态，遵循状态机约束。

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `Long` | 是 | 仓库 ID |

**请求体**

```json
{
  "status": "MAINTENANCE"
}
```

**业务规则**
- 状态跳转约束见「[仓库状态机](#仓库状态机)」章节。
- 目标状态为 `CLOSED` 时，若 `usedCapacity > 0` 则**禁止关闭**（返回：`仓库尚有库存，不能关闭`）。
- 已处于 `CLOSED` 状态的仓库**不能再变更状态**。

**响应**：返回更新后的完整 `Warehouse` 对象。

---

### 7. 仓库预警列表

```
GET /api/warehouse/warehouses/alerts
```

**摘要**：返回所有需要关注的异常仓库，满足以下**任一**条件即进入预警列表：

| 预警类型 | 触发条件 |
|---------|---------|
| 近满载 | `usedCapacity / capacity ≥ 90%`（且容量 > 0） |
| 空置激活 | 状态为 `ACTIVE` 且 `usedCapacity = 0` |
| 维护中 | 状态为 `MAINTENANCE` |

**无查询参数**。

**响应**：返回 `Warehouse` 对象数组（过滤掉状态为 `CLOSED` 的仓库后再按上述规则筛选）。

---

### 8. 导出仓库 CSV

```
GET /api/warehouse/warehouses/export
```

**摘要**：按条件导出仓库列表为 CSV 文件（UTF-8 BOM 编码，兼容 Excel 直接打开）。

**Query 参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | `String` | 否 | 仓库名称模糊匹配 |
| `type` | `String` | 否 | 仓库类型 |
| `status` | `String` | 否 | 仓库状态 |

**响应**：`Content-Type: text/csv; charset=UTF-8`，触发浏览器文件下载，文件名：`仓库列表.csv`。

**CSV 列顺序**：仓库编码、名称、类型、位置、总容量(吨)、已用容量(吨)、温度(℃)、湿度(%)、管理员、电话、状态

---

## 智能仓单

**Controller**：`WarehouseReceiptController`
**Base Path**：`/api/warehouse/receipts`

---

### 1. 仓单列表（分页）

```
GET /api/warehouse/receipts/list
```

**摘要**：按条件分页查询仓单，按创建时间倒序排列。

**Query 参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | 否 | `1` | 页码 |
| `size` | `int` | 否 | `10` | 每页条数 |
| `keyword` | `String` | 否 | — | 仓单编号模糊匹配 |
| `status` | `String` | 否 | — | 状态精确匹配：`VALID` / `PLEDGED` / `TRANSFERRED` / `CANCELLED` |

**响应**：分页结构，`records` 为 `WarehouseReceipt` 对象数组。

---

### 2. 仓单详情

```
GET /api/warehouse/receipts/{id}
```

**摘要**：查询单张仓单的完整信息。

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `Long` | 是 | 仓单 ID |

**错误场景**：仓单不存在时返回 `NOT_FOUND` 错误。

---

### 3. 创建仓单

```
POST /api/warehouse/receipts
Content-Type: application/json
```

**摘要**：为入库货物签发新仓单，系统自动生成编号、填充仓库名称并计算总估值。

**请求体**

```json
{
  "warehouseId": 1,
  "farmerId": 100,
  "farmerName": "王五",
  "batchCode": "BATCH20260401001",
  "variety": "红富士",
  "grade": "A",
  "quantity": 5000.00,
  "unitValue": 3.50,
  "inboundDate": "2026-04-11",
  "validUntil": "2026-10-11",
  "traceCode": "TRACE000001",
  "remark": "首批春季货"
}
```

**业务规则**
- `receiptNo` 自动生成，格式 `WR{yyyyMMdd}{0001}`。
- `warehouseName` 自动从 `warehouseId` 对应仓库读取，若仓库不存在则报错。
- `status` 自动置为 `VALID`，不可手动指定。
- 若同时提供 `unitValue` 和 `quantity`，则 `totalValue = unitValue × quantity` 自动计算。

---

### 4. 更新仓单

```
PUT /api/warehouse/receipts/{id}
Content-Type: application/json
```

**摘要**：修改仓单基本信息（数量、估值、有效期等）。

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `Long` | 是 | 仓单 ID |

**请求体**：同创建接口字段，`receiptNo` 和 `status` 即使传入也会被忽略。

**业务规则**
- **只有状态为 `VALID` 的仓单可以修改**，其他状态返回业务异常：`只有有效状态的仓单可以修改`。
- `totalValue` 在 `unitValue` 和 `quantity` 均有变更时自动重新计算。

---

### 5. 变更仓单状态

```
PUT /api/warehouse/receipts/{id}/status
Content-Type: application/json
```

**摘要**：变更仓单的流转状态（质押、转让、注销），遵循状态机约束。

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `Long` | 是 | 仓单 ID |

**请求体**

```json
{
  "status": "PLEDGED"
}
```

**业务规则**
- 状态跳转约束见「[仓单状态机](#仓单状态机)」章节。
- 传入非法状态值（不在 `VALID/PLEDGED/TRANSFERRED/CANCELLED` 范围内）直接报错。
- 变更成功后自动记录 `statusChangeTime`（系统时间）和 `statusChangeBy`（`SYSTEM`）。

---

### 6. 删除仓单

```
DELETE /api/warehouse/receipts/{id}
```

**摘要**：逻辑删除仓单。

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `Long` | 是 | 仓单 ID |

**业务规则**
- 状态为 `PLEDGED`（质押中）的仓单**禁止删除**，返回：`质押中的仓单不能删除`。
- 其他状态（`VALID`、`TRANSFERRED`、`CANCELLED`）均可删除。

---

### 7. 导出仓单 CSV

```
GET /api/warehouse/receipts/export
```

**摘要**：按条件导出仓单列表为 CSV 文件。

**Query 参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | `String` | 否 | 仓单编号模糊匹配 |
| `status` | `String` | 否 | 仓单状态 |

**响应**：`Content-Type: text/csv; charset=UTF-8`，文件名：`仓单列表.csv`。

**CSV 列顺序**：仓单编号、仓库名称、货主姓名、批次编码、品种、等级、数量(kg)、单价(元/kg)、总估值(元)、入库日期、有效期至、溯源码、状态

---

## 出入库记录

**Controller**：`WarehouseRecordController`
**Base Path**：`/api/warehouse/records`

---

### 1. 出入库记录列表（分页）

```
GET /api/warehouse/records/list
```

**摘要**：按条件分页查询出入库操作记录，按创建时间倒序排列。

**Query 参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | 否 | `1` | 页码 |
| `size` | `int` | 否 | `10` | 每页条数 |
| `keyword` | `String` | 否 | — | 记录编号模糊匹配 |
| `recordType` | `String` | 否 | — | 类型精确匹配：`INBOUND`（入库）/ `OUTBOUND`（出库） |
| `warehouseId` | `Long` | 否 | — | 按仓库 ID 精确过滤 |

---

### 2. 出入库记录详情

```
GET /api/warehouse/records/{id}
```

**摘要**：查询单条出入库记录详情。

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `Long` | 是 | 记录 ID |

**错误场景**：记录不存在时返回 `NOT_FOUND` 错误。

---

### 3. 创建出入库记录

```
POST /api/warehouse/records
Content-Type: application/json
```

**摘要**：记录一次货物入库或出库操作，同步更新仓库已用容量，并在达到容量阈值时自动切换仓库状态。

**请求体**

```json
{
  "warehouseId": 1,
  "recordType": "INBOUND",
  "batchCode": "BATCH20260401001",
  "variety": "红富士",
  "grade": "A",
  "quantity": 2000.00,
  "operator": "李操作员",
  "recordDate": "2026-04-11",
  "traceCode": "TRACE000002",
  "remark": "第一批入库"
}
```

**业务规则**

**入库（INBOUND）**
- 数量单位为 kg，系统自动换算为吨（÷ 1000）后叠加到 `usedCapacity`。
- 若 `newUsedCapacity > capacity`，返回业务异常：`仓库容量不足，剩余容量: X 吨`。
- 若 `newUsedCapacity ≥ capacity`，仓库状态自动切换为 `FULL`。
- `temperature` 未传时，系统按仓库类型自动模拟（见下表）。
- `humidity` 未传时，系统自动模拟为 60%~95% 随机值。

| 仓库类型 | 模拟温度范围 |
|---------|------------|
| `COLD` | -2.0 ℃ ~ 4.0 ℃ |
| `ATMOSPHERE` | 0.0 ℃ ~ 2.5 ℃ |
| `NORMAL` | 10.0 ℃ ~ 25.0 ℃ |

**出库（OUTBOUND）**
- 若当前 `usedCapacity < 出库量（吨）`，返回业务异常：`库存不足，当前库存: X 吨`。
- 出库后若仓库从 `FULL` 降至低于满载，状态自动恢复为 `ACTIVE`。

**公共**
- `recordDate` 未传时自动取当天日期。
- `recordNo` 自动生成，格式 `WR{yyyyMMdd}{序号}`。

---

### 4. 删除出入库记录

```
DELETE /api/warehouse/records/{id}
```

**摘要**：逻辑删除出入库记录。

> **注意**：删除记录不会回滚对应的仓库已用容量变更，仅为数据归档操作。

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `Long` | 是 | 记录 ID |

---

### 5. 导出出入库记录 CSV

```
GET /api/warehouse/records/export
```

**摘要**：按条件导出出入库记录为 CSV 文件。

**Query 参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | `String` | 否 | 记录编号模糊匹配 |
| `recordType` | `String` | 否 | 记录类型 |
| `warehouseId` | `Long` | 否 | 仓库 ID |

**响应**：`Content-Type: text/csv; charset=UTF-8`，文件名：`出入库记录.csv`。

**CSV 列顺序**：记录编号、仓库名称、类型、批次编码、品种、等级、数量(kg)、温度、湿度、操作人、操作日期、溯源码

---

## 仓储统计

**Controller**：`WarehouseStatisticsController`
**Base Path**：`/api/warehouse/statistics`

---

### 1. 仓储概览统计

```
GET /api/warehouse/statistics/summary
```

**摘要**：汇总全部仓库的容量使用情况，并按类型、状态分组统计数量。

**无请求参数**。

**响应示例**

```json
{
  "code": 200,
  "data": {
    "totalWarehouses": 5,
    "totalCapacity": 1500.00,
    "totalUsed": 820.00,
    "utilizationRate": 54.6700,
    "warehousesByType": {
      "COLD": 2,
      "NORMAL": 2,
      "ATMOSPHERE": 1
    },
    "warehousesByStatus": {
      "ACTIVE": 3,
      "FULL": 1,
      "MAINTENANCE": 1
    }
  }
}
```

**计算方式**：统计所有仓库（含 CLOSED）的总容量和已用容量；利用率 = `totalUsed / totalCapacity × 100`，保留 4 位小数。

---

### 2. 周转率统计

```
GET /api/warehouse/statistics/turnover
```

**摘要**：统计指定仓库（或全部仓库）在最近 N 天内的出入库周转率。

**Query 参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `warehouseId` | `Long` | 否 | — | 指定仓库 ID；不传则统计所有仓库 |
| `days` | `int` | 否 | `30` | 统计周期（天），从今天向前推算 |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "warehouseId": 1,
    "totalInbound": 10000.00,
    "totalOutbound": 8000.00,
    "turnoverRate": 0.8889,
    "recordCount": 15
  }
}
```

**计算公式**：`turnoverRate = outbound / ((inbound + outbound) / 2)`，保留 4 位小数；若分母为 0 则返回 0。

---

### 3. 损耗率统计

```
GET /api/warehouse/statistics/loss
```

**摘要**：统计指定仓库（或全部仓库）在最近 N 天内的货物损耗情况。

**Query 参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `warehouseId` | `Long` | 否 | — | 指定仓库 ID；不传则统计所有仓库 |
| `days` | `int` | 否 | `30` | 统计周期（天） |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "totalInbound": 10000.00,
    "totalOutbound": 9600.00,
    "difference": 400.00,
    "lossRate": 4.0000
  }
}
```

**计算公式**：`lossRate = (inbound - outbound) / inbound × 100`，保留 4 位小数；若入库量为 0 则返回 0。

> **说明**：`difference` 包含当前仍在库的货物，不完全等同于实际损耗。如需精确损耗统计，应结合实盘数据。

---

## 状态机说明

### 仓库状态机

仓库状态（`Warehouse.status`）遵循以下跳转规则：

```
        +--MAINTENANCE--+
        |               |
        v               |
ACTIVE --FULL-------+   +---> ACTIVE
   |                |
   |               ACTIVE
   +---CLOSED (终态，需库存为空)

详细跳转表：
ACTIVE      → MAINTENANCE  ✓
ACTIVE      → FULL         ✓ (仅系统自动触发，入库达满载时)
ACTIVE      → CLOSED       ✓ (需 usedCapacity = 0)
MAINTENANCE → ACTIVE       ✓
FULL        → ACTIVE       ✓ (仅系统自动触发，出库后低于满载时)
CLOSED      → 任何状态     ✗ (终态，不可变更)
```

| 当前状态 | 可变更为 | 限制条件 |
|---------|---------|---------|
| `ACTIVE` | `MAINTENANCE`、`CLOSED`、`FULL` | 变更为 `CLOSED` 时 `usedCapacity` 必须为 0 |
| `MAINTENANCE` | `ACTIVE` | 无 |
| `FULL` | `ACTIVE` | 无 |
| `CLOSED` | — | 终态，任何变更均被拒绝 |

**状态含义**

| 状态 | 说明 |
|------|------|
| `ACTIVE` | 正常运营中 |
| `MAINTENANCE` | 维护中，暂停收货 |
| `FULL` | 已满载，无法继续入库 |
| `CLOSED` | 永久关闭（终态） |

---

### 仓单状态机

仓单状态（`WarehouseReceipt.status`）遵循以下跳转规则：

```
VALID → PLEDGED → CANCELLED
VALID → TRANSFERRED
VALID → CANCELLED
```

| 当前状态 | 可变更为 | 说明 |
|---------|---------|------|
| `VALID` | `PLEDGED`、`TRANSFERRED`、`CANCELLED` | 有效仓单可质押、转让或注销 |
| `PLEDGED` | `CANCELLED` | 质押中的仓单只能注销（解押须先变更至其他状态，当前版本无解押流程） |
| `TRANSFERRED` | — | 已转让，终态 |
| `CANCELLED` | — | 已注销，终态 |

**状态含义**

| 状态 | 说明 |
|------|------|
| `VALID` | 有效仓单，可正常流转 |
| `PLEDGED` | 质押中，不可修改、不可删除 |
| `TRANSFERRED` | 已转让给第三方 |
| `CANCELLED` | 已注销，货物已出库或作废 |

---

## 通用响应结构

所有接口（导出 CSV 除外）均返回统一 JSON 信封：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

**分页响应结构**（`PageResult<T>`）

```json
{
  "code": 200,
  "data": {
    "records": [ ... ],
    "total": 100,
    "size": 10,
    "current": 1
  }
}
```

**错误响应示例**

```json
{
  "code": 404,
  "message": "仓库不存在",
  "data": null
}
```

```json
{
  "code": 500,
  "message": "仓库尚有库存，不能删除",
  "data": null
}
```

**常见错误码**

| HTTP 状态码 | 业务含义 |
|------------|---------|
| `200` | 成功 |
| `400` | 参数错误 |
| `404` | 资源不存在 |
| `500` | 业务规则校验失败（BizException） |
