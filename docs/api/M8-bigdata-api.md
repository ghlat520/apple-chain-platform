# M8 大数据模块 API 文档

> 模块路径：`apple-module-bigdata`
> 基础路径：`/api/bigdata`（部分为 `/api/stats`）
> 生成日期：2026-04-11

---

## 目录

1. [数据大屏 DashboardController](#1-数据大屏-dashboardcontroller)
2. [大屏补充统计 DashboardSupplementController](#2-大屏补充统计-dashboardsupplementcontroller)
3. [数据统计 StatisticsController](#3-数据统计-statisticscontroller)
4. [分析报告 AnalysisReportController](#4-分析报告-analysisreportcontroller)
5. [数据资产目录 DataAssetController](#5-数据资产目录-dataassetcontroller)
6. [数据源注册 DataSourceController](#6-数据源注册-datasourcecontroller)
7. [数据质量 DqRuleController](#7-数据质量-dqrulecontroller)
8. [采集任务 CollectJobController](#8-采集任务-collectjobcontroller)
9. [数据血缘 DataLineageController](#9-数据血缘-datalineagecontroller)
10. [指标中心 MetricController](#10-指标中心-metriccontroller)
11. [开放API客户端 OpenApiController](#11-开放api客户端-openapicontroller)
12. [运营监控 OpsMonitorController](#12-运营监控-opsmonitorcontroller)
13. [其他管理端 Controller](#13-其他管理端-controller)

---

## 1. 数据大屏 DashboardController

**Base URL**：`/api/bigdata/dashboard`

所有数据来自真实 SQL 聚合查询，无硬编码数据。

---

### 1.1 KPI 汇总统计

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/dashboard/stats` |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "totalOrchards": 120,
    "totalFarmers": 85,
    "totalOrders": 350,
    "totalTradeAmount": 1925000.00,
    "totalTraceChains": 280
  }
}
```

---

### 1.2 近6个月采收趋势（按月）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/dashboard/harvest-trend` |

**响应结构**：`List<Map<String, Object>>`

| 字段 | 类型 | 说明 |
|------|------|------|
| month | String | 月份 |
| harvestAmount | Number | 采收量（kg） |

---

### 1.3 近6个月交易趋势（按月）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/dashboard/trade-trend` |

---

### 1.4 苹果品种分布（按采收量）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/dashboard/variety-distribution` |

---

### 1.5 今年采收量 TOP5 果园

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/dashboard/top-orchards` |

---

### 1.6 供需对比（按品种）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/dashboard/supply-demand` |

---

### 1.7 溯源链状态分布

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/dashboard/trace-stats` |

---

## 2. 大屏补充统计 DashboardSupplementController

**Base URL**：`/api/bigdata/dashboard`

使用 JdbcTemplate **跨模块查询**，聚合仓储、金融、交易等模块数据。

---

### 2.1 仓储总量统计

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/dashboard/warehouse-stock` |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "warehouseCount": 5,
    "totalCapacity": 500000,
    "usedCapacity": 320000
  }
}
```

**数据来源**：`wh_warehouse` 表

---

### 2.2 金融规模统计

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/dashboard/finance-scale` |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "loanCount": 25,
    "totalAmount": 3500000.00
  }
}
```

**数据来源**：`fn_loan` 表

---

### 2.3 品种价格趋势（按日均价）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/dashboard/price-trend` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| variety | String | 否 | 红富士 | 品种名称 |
| days | int | 否 | 30 | 统计近N天 |

**数据来源**：`td_trade_order` 表，`AVG(unit_price) GROUP BY trade_date`

---

### 2.4 损耗率统计

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/dashboard/loss-rate` |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "inbound": 100000,
    "outbound": 95000,
    "lossRatePct": 5.0
  }
}
```

**数据来源**：`wh_warehouse_record` 表

**计算公式**：`lossRate = (inbound - outbound) / inbound * 100`

---

## 3. 数据统计 StatisticsController

**Base URL**：`/api/stats`

---

### 3.1 KPI 汇总

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/stats/summary` |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "totalOrchards": 120,
    "totalBatches": 85,
    "totalOrders": 350,
    "totalUsers": 200,
    "activeOrchards": 95,
    "completedOrders": 280,
    "totalTradeAmount": 1925000.00,
    "totalProducts": 45,
    "lowInventory": 3,
    "totalUsages": 560,
    "totalWarehouses": 5
  }
}
```

**数据来源**：StatisticsMapper，使用 `COUNT(*)`/`SUM()` 真实聚合

---

### 3.2 近6个月月度交易趋势

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/stats/trend` |

**数据来源**：`DATE_FORMAT` + `GROUP BY MONTH`

---

### 3.3 TOP5 苹果品种

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/stats/top-varieties` |

**数据来源**：`ORDER BY COUNT(*) DESC LIMIT 5`

---

### 3.4 各模块待处理事项统计

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/stats/pending` |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "pendingOrders": 15,
    "unpaidOrders": 8,
    "draftBatches": 5,
    "inactiveFarmers": 12
  }
}
```

---

## 4. 分析报告 AnalysisReportController

**Base URL**：`/api/bigdata/report`

---

### 4.1 生成分析报告

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/bigdata/report/generate` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | String | 是 | 报告类型：`WEEKLY` / `MONTHLY` / `SEASONAL` |
| period | String | 是 | 报告周期（如 `2026-W15`、`2026-04`、`2026-Q2`） |

**业务规则**

- 自动聚合各模块数据生成报告
- 报告包含：交易额、采收量、品种分布、异常统计等
- 生成后状态为 `DRAFT`

---

### 4.2 分页查询报告列表

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/report` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |

---

### 4.3 查询报告详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/report/{id}` |

---

### 4.4 发布报告

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/bigdata/report/{id}/publish` |

**状态机**：`DRAFT` → `PUBLISHED`

---

## 5. 数据资产目录 DataAssetController

**Base URL**：`/api/bigdata/asset`

数据资产 = 表级元数据 + 字级元数据的目录浏览器。

---

### 5.1 分页查询资产

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/asset` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 20 | 每页条数 |
| keyword | String | 否 | — | 资产编码/名称模糊搜索 |
| securityLevel | String | 否 | — | 安全等级筛选 |

---

### 5.2 获取资产详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/asset/{id}` |

---

### 5.3 新增资产

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/bigdata/asset` |

---

### 5.4 更新资产

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/bigdata/asset/{id}` |

---

### 5.5 删除资产

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/bigdata/asset/{id}` |

---

### 5.6 列出资产字段

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/asset/{id}/fields` |

---

### 5.7 新增字段

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/bigdata/asset/{id}/fields` |

---

### 5.8 删除字段

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/bigdata/asset/fields/{fieldId}` |

---

## 6. 数据源注册 DataSourceController

**Base URL**：`/api/bigdata/source`

管理 8 个内部模块 + 4 个外部 API 数据源。

---

### 6.1 分页查询数据源

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/source` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 20 | 每页条数 |
| category | String | 否 | — | 数据源分类 |
| keyword | String | 否 | — | 名称/编码模糊搜索 |

---

### 6.2 获取数据源详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/source/{id}` |

---

### 6.3 新增数据源

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/bigdata/source` |

---

### 6.4 更新数据源

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/bigdata/source/{id}` |

---

### 6.5 删除数据源

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/bigdata/source/{id}` |

---

### 6.6 连通性测试

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/bigdata/source/{id}/test` |

**响应示例**

```json
{
  "code": 200,
  "data": "连接成功 (延迟: 15ms)"
}
```

---

## 7. 数据质量 DqRuleController

**Base URL**：`/api/bigdata/dq`

---

### 7.1 分页查询规则

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/dq/rules` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 20 | 每页条数 |
| ruleType | String | 否 | — | 规则类型 |
| assetId | Long | 否 | — | 关联资产ID |

---

### 7.2 获取规则

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/dq/rules/{id}` |

---

### 7.3 新增规则

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/bigdata/dq/rules` |

---

### 7.4 更新规则

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/bigdata/dq/rules/{id}` |

---

### 7.5 删除规则

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/bigdata/dq/rules/{id}` |

---

### 7.6 手动触发检查

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/bigdata/dq/rules/{id}/check` |

**响应结构**：`BdDqCheckResult`

```json
{
  "code": 200,
  "data": {
    "ruleId": 1,
    "passed": false,
    "failCount": 5,
    "totalChecked": 100,
    "checkTime": "2026-04-11 10:30:00"
  }
}
```

---

### 7.7 检查结果历史

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/dq/rules/{id}/results` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 20 | 每页条数 |

---

## 8. 采集任务 CollectJobController

**Base URL**：`/api/bigdata/job`

---

### 8.1 分页查询采集任务

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/job` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 20 | 每页条数 |
| jobType | String | 否 | — | 任务类型（CDC/SCHEDULED/MANUAL） |
| sourceId | Long | 否 | — | 数据源ID |

---

### 8.2 获取采集任务详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/job/{id}` |

---

### 8.3 新增采集任务

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/bigdata/job` |

---

### 8.4 更新采集任务

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/bigdata/job/{id}` |

---

### 8.5 删除采集任务

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/bigdata/job/{id}` |

---

### 8.6 手动触发任务

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/bigdata/job/{id}/trigger` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| triggeredBy | String | 否 | admin | 触发人 |

**响应结构**：`BdCollectJobRun`

---

### 8.7 运行历史

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/job/{id}/runs` |

---

## 9. 数据血缘 DataLineageController

**Base URL**：`/api/bigdata/lineage`

> 注意：此为 DataOps 概念的数据血缘（上下游 DAG），与 apple-module-trace 的产品溯源链不同。

---

### 9.1 分页查询血缘边

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/lineage` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 20 | 每页条数 |
| relationType | String | 否 | — | 关系类型 |

---

### 9.2 新增血缘边

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/bigdata/lineage` |

---

### 9.3 删除血缘边

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/bigdata/lineage/{id}` |

---

### 9.4 下游依赖

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/lineage/downstream` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | String | 是 | 节点类型 |
| id | String | 是 | 节点ID |

---

### 9.5 上游血缘

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/lineage/upstream` |

**Query 参数**：同下游依赖

---

## 10. 指标中心 MetricController

**Base URL**：`/api/bigdata/metric`

---

### 10.1 分页查询指标

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/metric` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 20 | 每页条数 |
| category | String | 否 | — | 指标分类 |
| isCore | Integer | 否 | — | 是否核心指标（1/0） |

---

### 10.2 指标详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/metric/{id}` |

---

### 10.3 新增指标

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/bigdata/metric` |

---

### 10.4 更新指标

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/bigdata/metric/{id}` |

---

### 10.5 删除指标

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/bigdata/metric/{id}` |

---

### 10.6 指标取值序列

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/metric/{code}/values` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| from | LocalDate | 否 | 起始日期 (ISO格式) |
| to | LocalDate | 否 | 结束日期 (ISO格式) |

---

### 10.7 记录指标取值

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/bigdata/metric/values` |

---

## 11. 开放API客户端 OpenApiController

**Base URL**：`/api/bigdata/openapi/clients`

---

### 11.1 注册新API客户端

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/bigdata/openapi/clients` |

**Request Body**

```json
{
  "clientName": "合作伙伴A",
  "contactEmail": "partner@example.com",
  "description": "数据对接"
}
```

**业务规则**

- 自动生成 `appKey`：格式 `ak_` + 16位随机字符
- 自动生成 `appSecretEnc`（加密存储）
- 初始状态 `ACTIVE`

---

### 11.2 分页查询API客户端列表

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/openapi/clients` |

---

### 11.3 更新客户端状态

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/bigdata/openapi/clients/{id}/status` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 是 | `ACTIVE` / `DISABLED` / `REVOKED` |

---

### 11.4 查询客户端调用日志

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/openapi/clients/{id}/logs` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 20 | 每页条数 |

---

### 11.5 轮转密钥

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/bigdata/openapi/clients/{id}/rotate-key` |

**业务规则**

- 重新生成 `appKey` + `appSecretEnc`
- 旧密钥立即失效
- 新密钥仅此一次返回明文

---

## 12. 运营监控 OpsMonitorController

**Base URL**：`/api/bigdata/ops`

---

### 12.1 平台健康概览

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/ops/health` |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "status": "UP",
    "uptimeMs": 86400000,
    "javaVersion": "17.0.6",
    "components": {
      "mysql": "UP",
      "redis": "UP",
      "kafka": "UNKNOWN",
      "canal": "UNKNOWN",
      "clickhouse": "UNKNOWN",
      "dolphinscheduler": "UNKNOWN"
    }
  }
}
```

---

### 12.2 JVM 指标

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/bigdata/ops/metrics` |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "heapUsed": 268435456,
    "heapMax": 536870912,
    "nonHeapUsed": 67108864,
    "availableProcessors": 8,
    "threadCount": 120
  }
}
```

---

## 13. 其他管理端 Controller

以下 Controller 为管理后台辅助功能，标准 CRUD：

| Controller | 路径 | 说明 |
|-----------|------|------|
| AuditLogQueryController | `/api/bigdata/audit` | 审计日志查询 |
| CollectJobCallbackController | `/api/bigdata/job/callback` | 采集任务回调 |
| DashboardConfigController | `/api/bigdata/dashboard/config` | 大屏配置管理 |
| RolePermissionController | `/api/bigdata/role` | 角色/权限管理 |

---

## 状态机

### 分析报告状态

```
DRAFT ──→ PUBLISHED
```

### API 客户端状态

```
ACTIVE ──→ DISABLED ──→ REVOKED
```

### 采集任务运行状态

```
PENDING ──→ RUNNING ──→ SUCCESS
                       ──→ FAILED ──→ RETRYING
```

### 数据质量检查状态

```
PENDING ──→ RUNNING ──→ PASSED
                      ──→ FAILED
```
