# M3 溯源管理模块 API 文档

> 模块路径：`apple-module-trace`
> 基础路径：`/api/trace`
> 生成日期：2026-04-11

---

## 目录

1. [产品溯源 TraceController](#1-产品溯源-tracecontroller)
2. [溯源批次 TraceBatchController](#2-溯源批次-tracebatchcontroller)
3. [三级溯源码 TraceCodeController](#3-三级溯源码-tracecodecontroller)
4. [异常追溯 AnomalyTraceController](#4-异常追溯-anomalytracecontroller)
5. [区块链存证 ChainController](#5-区块链存证-chaincontroller)
6. [实体字段说明](#6-实体字段说明)

---

## 1. 产品溯源 TraceController

**Base URL**：`/api/trace`

---

### 1.1 溯源链列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trace/list` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | — | 溯源码/产品名模糊搜索 |
| status | String | 否 | — | 状态筛选 |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 123456789,
        "traceCode": "TC20260411001",
        "productName": "红富士苹果",
        "batchNo": "HB20260411001",
        "status": "ACTIVE",
        "createTime": "2026-04-11 10:00:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1
  }
}
```

---

### 1.2 溯源详情（含时间轴节点）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trace/{traceCode}` |

**Path 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| traceCode | String | 是 | 溯源码 |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "traceChain": { "id": 123, "traceCode": "TC20260411001", "productName": "红富士苹果", "status": "ACTIVE" },
    "nodes": [
      { "id": 1, "nodeName": "种植", "operator": "张三", "operateTime": "2026-04-01", "description": "施有机肥" },
      { "id": 2, "nodeName": "采收", "operator": "张三", "operateTime": "2026-04-10", "description": "采收红富士" }
    ]
  }
}
```

---

### 1.3 公众扫码查询（无需登录）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trace/scan/{traceCode}` |

**业务规则**

- 无需认证，面向消费者公开接口
- 返回简化版溯源信息

---

### 1.4 公众扫码 - 全链路详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trace/scan/{traceCode}/full` |

**响应结构**：`TraceFullChainVO`

| 字段 | 类型 | 说明 |
|------|------|------|
| traceCode | String | 溯源码 |
| batchNo | String | 批次号 |
| variety | String | 品种 |
| currentStatus | String | 当前状态 |
| orchardName | String | 果园名称（跨模块查询 pt_orchard） |
| farmerName | String | 农户姓名（跨模块查询 uc_user） |
| region | String | 产地（跨模块查询 pt_orchard） |
| inputMaterials | List\<Map\> | 农资使用记录（跨模块查询 agri_usage） |
| warehouseRecords | List\<Map\> | 仓储记录（跨模块查询 wh_warehouse_record） |
| tradeInfo | Map | 交易信息（跨模块查询 td_trade_order） |
| timeline | List\<TraceNode\> | 溯源时间轴节点 |
| chainTxHash | String | 区块链交易哈希 |
| chainStatus | Integer | 区块链状态（0待上链/1成功/2失败） |

**业务规则**

- **跨模块聚合**：使用 JdbcTemplate 查询种植、农资、仓储、交易各模块表
- 面向消费者展示完整溯源链路

---

### 1.5 生成溯源二维码 PNG

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trace/qrcode/{traceCode}` |
| Content-Type | `image/png` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| size | int | 否 | 300 | 二维码尺寸（像素） |

**业务规则**

- 先校验溯源码存在，再生成 QR Code
- 二维码内容指向公开扫码 URL

---

### 1.6 添加溯源节点

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trace/node` |

**Request Body**：TraceNode 实体 JSON

---

### 1.7 导出溯源链 CSV

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trace/export` |

---

## 2. 溯源批次 TraceBatchController

**Base URL**：`/api/trace/batches`

---

### 2.1 批次列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trace/batches` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | — | 批次号/产品名模糊搜索 |
| status | String | 否 | — | 状态筛选 |

---

### 2.2 批次详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trace/batches/{id}` |

---

### 2.3 公众扫码查询（无需登录）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trace/batches/scan/{batchCode}` |

**业务规则**

- 无需认证，按批次码查询溯源信息

---

### 2.4 创建溯源批次

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trace/batches` |

**业务规则**

- 自动生成区块链哈希（区块链上链异步处理）

---

### 2.5 更新批次状态

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/trace/batches/{id}/status` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 是 | 目标状态 |

---

### 2.6 删除批次（软删除）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/trace/batches/{id}` |

---

### 2.7 添加溯源记录

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trace/batches/{id}/records` |

**Request Body**：TraceRecord 实体 JSON

---

### 2.8 查询批次溯源记录

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trace/batches/{id}/records` |

---

### 2.9 导出批次 CSV

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trace/batches/export` |

---

## 3. 三级溯源码 TraceCodeController

**Base URL**：`/api/trace/code`

三级溯源体系：BATCH → BOX → FRUIT

---

### 3.1 生成 BOX 级编码

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trace/code/generate-box` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| batchId | Long | 是 | 批次ID |
| boxCount | int | 是 | 生成箱码数量 |

**业务规则**

- 基于批次生成指定数量的箱级溯源码
- 每个码含 CRC16 校验

---

### 3.2 生成 FRUIT 级编码

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trace/code/generate-fruit` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| boxCode | String | 是 | 箱级溯源码 |
| fruitCount | int | 是 | 生成果码数量 |

---

### 3.3 校验溯源码（CRC16 + 状态）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trace/code/verify/{code}` |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "valid": true,
    "codeLevel": "BOX",
    "batchId": 123456789,
    "status": "ACTIVE",
    "crcValid": true
  }
}
```

---

### 3.4 VDP 印刷文件导出

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trace/code/vdp-export/{batchId}` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| format | String | 否 | csv | 导出格式：`csv` / `txt` |

**业务规则**

- 导出可变数据印刷文件，用于标签印刷
- UTF-8 编码

---

## 4. 异常追溯 AnomalyTraceController

**Base URL**：`/api/trace/anomaly`

---

### 4.1 上报异常

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trace/anomaly` |

**Request Body**

```json
{
  "traceCode": "TC20260411001",
  "batchId": 123456789,
  "anomalyType": "TEMP_VIOLATION",
  "description": "运输温度超出规定范围",
  "severity": "HIGH"
}
```

**字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| traceCode | String | 关联溯源码 |
| batchId | Long | 关联批次ID |
| anomalyType | String | 异常类型：`PESTICIDE_EXCESS` / `TEMP_VIOLATION` / `QUALITY_FAIL` / `OTHER` |
| description | String | 异常描述 |
| severity | String | 严重程度：`HIGH` / `MEDIUM` / `LOW` |

**业务规则**

- 创建后状态为 `OPEN`
- 记录上报时间和上报人

---

### 4.2 异常列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trace/anomaly` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| type | String | 否 | — | 异常类型筛选 |
| status | String | 否 | — | 状态筛选：`OPEN` / `INVESTIGATING` / `RESOLVED` |
| severity | String | 否 | — | 严重程度筛选 |

---

### 4.3 异常详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trace/anomaly/{id}` |

---

### 4.4 开始调查

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/trace/anomaly/{id}/investigate` |

**状态机**：`OPEN` → `INVESTIGATING`

**业务规则**

- 只有 `OPEN` 状态可以开始调查

---

### 4.5 解决异常

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/trace/anomaly/{id}/resolve` |

**Request Body**

```json
{
  "rootCause": "冷链车制冷系统故障导致温度超标",
  "resolvedBy": "admin"
}
```

**状态机**：`INVESTIGATING` → `RESOLVED`

**业务规则**

- 只有 `INVESTIGATING` 状态可以解决
- 记录根因分析和解决人

---

### 4.6 影响分析

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trace/anomaly/impact/{traceCode}` |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "traceCode": "TC20260411001",
    "affectedBatches": 3,
    "affectedFruits": 1500,
    "downstreamOrders": [
      { "orderId": 1001, "orderNo": "ORD20260411001", "status": "DELIVERED" }
    ]
  }
}
```

**业务规则**

- 基于溯源码查询下游所有受影响的批次和订单
- 使用跨模块查询（溯源批次、交易订单）

---

## 5. 区块链存证 ChainController

**Base URL**：分散路径（内部接口和管理接口分开）

> 区块链接口为 **Mock 实现**，通过 `@ConditionalOnProperty` 切换真实/模拟模式。

---

### 5.1 提交业务快照上链

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trace/chain/submit` |
| 权限 | `trace:write` |

**Request Body**：ChainSubmitRequest

```json
{
  "traceCode": "TC20260411001",
  "businessType": "HARVEST",
  "businessId": 123456789,
  "snapshot": "{\"variety\":\"红富士\",\"quantity\":500,\"grade\":\"A\"}"
}
```

**业务规则**

- 内部接口，自动哈希后异步上链
- 返回 ChainRecord，初始状态为待上链(0)

---

### 5.2 公开校验链上记录

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trace/chain/verify/{traceCode}` |

**业务规则**

- 无需认证，面向消费者验证
- 返回该溯源码的链上存证记录

---

### 5.3 管理：上链记录列表

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/admin/chain/records` |
| 权限 | `trace:read` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| status | Integer | 否 | — | 状态：0待上链 / 1成功 / 2失败 / 3重试中 |
| limit | int | 否 | 100 | 返回条数 |

---

### 5.4 管理：手动重试上链失败的记录

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/admin/chain/retry/{id}` |
| 权限 | `trace:write` |

---

## 6. 实体字段说明

### AnomalyTrace（异常追溯）— 表 `tr_anomaly_trace`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 雪花ID |
| traceCode | String | 关联溯源码 |
| batchId | Long | 关联批次ID |
| anomalyType | String | 异常类型：`PESTICIDE_EXCESS` / `TEMP_VIOLATION` / `QUALITY_FAIL` / `OTHER` |
| description | String | 异常描述 |
| severity | String | 严重程度：`HIGH` / `MEDIUM` / `LOW` |
| status | String | 状态：`OPEN` / `INVESTIGATING` / `RESOLVED` |
| affectedBatchCount | Integer | 受影响批次数 |
| affectedFruitCount | Integer | 受影响果实数 |
| rootCauseAnalysis | String | 根因分析 |
| resolvedBy | String | 解决人 |
| resolvedTime | LocalDateTime | 解决时间 |
| remark | String | 备注 |

### TraceCode（三级溯源码）— 表 `tr_trace_code`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 雪花ID |
| code | String | 溯源码（含 CRC16） |
| codeLevel | String | 级别：`BATCH` / `BOX` / `FRUIT` |
| batchId | Long | 关联批次ID |
| parentCode | String | 上级溯源码（BOX→BATCH, FRUIT→BOX） |
| status | String | 状态：`ACTIVE` / `USED` / `VOID` |

### ChainRecord（区块链存证记录）— 表 `tr_chain_record`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 雪花ID |
| traceCode | String | 溯源码 |
| businessType | String | 业务类型 |
| businessId | Long | 业务ID |
| snapshotHash | String | 快照哈希 |
| txHash | String | 区块链交易哈希 |
| status | Integer | 状态：0待上链 / 1成功 / 2失败 / 3重试中 |
| retryCount | Integer | 重试次数 |

---

## 状态机

### 异常追溯状态

```
OPEN ──→ INVESTIGATING ──→ RESOLVED
```

- `OPEN`：初始状态，异常已上报
- `INVESTIGATING`：正在调查中
- `RESOLVED`：已解决，记录根因分析

### 区块链上链状态

```
0(待上链) ──→ 1(成功)
          ──→ 2(失败) ──→ 3(重试中) ──→ 1(成功)
                                      ──→ 2(失败)
```
