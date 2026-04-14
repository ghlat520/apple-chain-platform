# M8 大数据模块契约

> 模块：`apple-module-bigdata`
> URL 前缀：`/api/bigdata/{dashboard,report,asset,source,dq,job,lineage,metric,openapi,ops,audit,screen,role}` + `/api/stats` + `/api/dashboard`
> 错误码段：`1100000 ~ 1199999`（CONVENTIONS.md:143 权威）
> 最近更新：2026-04-12
> 权威源：本文件
> 后端实现：`apple-module-bigdata`
> 关联契约：全模块（大数据是跨模块聚合层，数据源覆盖 planting / trade / warehouse / finance / trace / iot / coldchain / input）

---

## 模块概述

大数据模块是苹果产业链平台的**跨模块聚合层**，提供三类核心能力：

1. **数据大屏（Dashboard）**：KPI 汇总、趋势图、品种分布、TOP 排行等可视化数据，全部来自 MySQL 实时聚合查询（JdbcTemplate / MyBatis），**无硬编码数据**
2. **数据治理（DataOps）**：数据资产目录、数据源注册、数据质量规则、数据血缘、采集任务、指标中心、分析报告、开放 API 客户端
3. **运营监控（Ops）**：平台健康检查、JVM 指标、审计日志、角色权限矩阵

### 数据源标注

| 数据源 | 用途 | 涉及端点 |
|--------|------|---------|
| **MySQL 聚合**（JdbcTemplate / StatisticsMapper） | KPI 统计、趋势、TOP 排行 | §2, §3, §4, §5（补充统计跨 wh/fn/td 表） |
| **MySQL CRUD**（MyBatis-Plus） | 资产、数据源、规则、任务、血缘、指标、报告等 | §6 ~ §15 |
| **DS 回调**（HTTP POST） | DolphinScheduler 采集任务运行结果回写 | §8.8 |

> **注意**：当前阶段（M2）无 WebSocket 实时推送端点。大屏数据通过 HTTP GET 轮询获取。ClickHouse / Kafka / Canal 为 M5 规划中的数据源，当前 `ops/health` 返回 `UNKNOWN`。

---

## 1. 接口索引（90 端点）

| # | 接口中文名 | URL | 方法 | 认证 | 数据源 |
|---|-----------|-----|------|------|--------|
| 1 | [KPI 汇总统计](#21-kpi-汇总统计) | `/api/bigdata/dashboard/stats` | GET | ✅ | MySQL 聚合 |
| 2 | [近6个月采收趋势](#22-近6个月采收趋势按月) | `/api/bigdata/dashboard/harvest-trend` | GET | ✅ | MySQL 聚合 |
| 3 | [近6个月交易趋势](#23-近6个月交易趋势按月) | `/api/bigdata/dashboard/trade-trend` | GET | ✅ | MySQL 聚合 |
| 4 | [苹果品种分布](#24-苹果品种分布按采收量) | `/api/bigdata/dashboard/variety-distribution` | GET | ✅ | MySQL 聚合 |
| 5 | [今年采收量 TOP5 果园](#25-今年采收量-top5-果园) | `/api/bigdata/dashboard/top-orchards` | GET | ✅ | MySQL 聚合 |
| 6 | [供需对比（按品种）](#26-供需对比按品种) | `/api/bigdata/dashboard/supply-demand` | GET | ✅ | MySQL 聚合 |
| 7 | [溯源链状态分布](#27-溯源链状态分布) | `/api/bigdata/dashboard/trace-stats` | GET | ✅ | MySQL 聚合 |
| 8 | [仓储总量统计](#31-仓储总量统计) | `/api/bigdata/dashboard/warehouse-stock` | GET | ✅ | MySQL 跨模块（wh_warehouse） |
| 9 | [金融规模统计](#32-金融规模统计) | `/api/bigdata/dashboard/finance-scale` | GET | ✅ | MySQL 跨模块（fn_loan） |
| 10 | [品种价格趋势](#33-品种价格趋势按日均价) | `/api/bigdata/dashboard/price-trend` | GET | ✅ | MySQL 跨模块（td_trade_order） |
| 11 | [损耗率统计](#34-损耗率统计) | `/api/bigdata/dashboard/loss-rate` | GET | ✅ | MySQL 跨模块（wh_warehouse_record） |
| 12 | [MVP KPI 汇总](#41-mvp-kpi-汇总统计) | `/api/dashboard/stats` | GET | ✅ | MySQL 聚合 |
| 13 | [MVP 交易趋势](#42-mvp-近6个月交易趋势) | `/api/dashboard/trend` | GET | ✅ | MySQL 聚合 |
| 14 | [MVP TOP5 果园交易量](#43-mvp-top5-果园交易量排名) | `/api/dashboard/top-orchards` | GET | ✅ | MySQL 聚合 |
| 15 | [MVP 待处理事项](#44-mvp-待处理事项统计) | `/api/dashboard/pending-actions` | GET | ✅ | MySQL 聚合 |
| 16 | [全量 KPI 汇总](#51-kpi-汇总) | `/api/stats/summary` | GET | ✅ | MySQL 聚合 |
| 17 | [月度交易趋势](#52-近6个月月度交易趋势) | `/api/stats/trend` | GET | ✅ | MySQL 聚合 |
| 18 | [TOP5 苹果品种](#53-top5-苹果品种) | `/api/stats/top-varieties` | GET | ✅ | MySQL 聚合 |
| 19 | [各模块待处理统计](#54-各模块待处理事项统计) | `/api/stats/pending` | GET | ✅ | MySQL 聚合 |
| 20 | [生成分析报告](#61-生成分析报告) | `/api/bigdata/report/generate` | POST | ✅ | MySQL CRUD |
| 21 | [报告列表（分页）](#62-报告列表分页) | `/api/bigdata/report` | GET | ✅ | MySQL CRUD |
| 22 | [报告详情](#63-报告详情) | `/api/bigdata/report/{id}` | GET | ✅ | MySQL CRUD |
| 23 | [发布报告](#64-发布报告) | `/api/bigdata/report/{id}/publish` | PUT | ✅ | MySQL CRUD |
| 24 | [资产列表（分页）](#71-资产列表分页) | `/api/bigdata/asset` | GET | ✅ | MySQL CRUD |
| 25 | [资产详情](#72-资产详情) | `/api/bigdata/asset/{id}` | GET | ✅ | MySQL CRUD |
| 26 | [新增资产](#73-新增资产) | `/api/bigdata/asset` | POST | ✅ | MySQL CRUD |
| 27 | [更新资产](#74-更新资产) | `/api/bigdata/asset/{id}` | PUT | ✅ | MySQL CRUD |
| 28 | [删除资产](#75-删除资产) | `/api/bigdata/asset/{id}` | DELETE | ✅ | MySQL CRUD |
| 29 | [列出资产字段](#76-列出资产字段) | `/api/bigdata/asset/{id}/fields` | GET | ✅ | MySQL CRUD |
| 30 | [新增字段](#77-新增字段) | `/api/bigdata/asset/{id}/fields` | POST | ✅ | MySQL CRUD |
| 31 | [删除字段](#78-删除字段) | `/api/bigdata/asset/fields/{fieldId}` | DELETE | ✅ | MySQL CRUD |
| 32 | [数据源列表（分页）](#81-数据源列表分页) | `/api/bigdata/source` | GET | ✅ | MySQL CRUD |
| 33 | [数据源详情](#82-数据源详情) | `/api/bigdata/source/{id}` | GET | ✅ | MySQL CRUD |
| 34 | [新增数据源](#83-新增数据源) | `/api/bigdata/source` | POST | ✅ | MySQL CRUD |
| 35 | [更新数据源](#84-更新数据源) | `/api/bigdata/source/{id}` | PUT | ✅ | MySQL CRUD |
| 36 | [删除数据源](#85-删除数据源) | `/api/bigdata/source/{id}` | DELETE | ✅ | MySQL CRUD |
| 37 | [连通性测试](#86-连通性测试) | `/api/bigdata/source/{id}/test` | POST | ✅ | 运行时探测 |
| 38 | [DQ 规则列表（分页）](#91-dq-规则列表分页) | `/api/bigdata/dq/rules` | GET | ✅ | MySQL CRUD |
| 39 | [DQ 规则详情](#92-dq-规则详情) | `/api/bigdata/dq/rules/{id}` | GET | ✅ | MySQL CRUD |
| 40 | [新增 DQ 规则](#93-新增-dq-规则) | `/api/bigdata/dq/rules` | POST | ✅ | MySQL CRUD |
| 41 | [更新 DQ 规则](#94-更新-dq-规则) | `/api/bigdata/dq/rules/{id}` | PUT | ✅ | MySQL CRUD |
| 42 | [删除 DQ 规则](#95-删除-dq-规则) | `/api/bigdata/dq/rules/{id}` | DELETE | ✅ | MySQL CRUD |
| 43 | [手动触发检查](#96-手动触发检查) | `/api/bigdata/dq/rules/{id}/check` | POST | ✅ | 运行时执行 |
| 44 | [检查结果历史](#97-检查结果历史) | `/api/bigdata/dq/rules/{id}/results` | GET | ✅ | MySQL CRUD |
| 45 | [采集任务列表（分页）](#101-采集任务列表分页) | `/api/bigdata/job` | GET | ✅ | MySQL CRUD |
| 46 | [采集任务详情](#102-采集任务详情) | `/api/bigdata/job/{id}` | GET | ✅ | MySQL CRUD |
| 47 | [新增采集任务](#103-新增采集任务) | `/api/bigdata/job` | POST | ✅ | MySQL CRUD |
| 48 | [更新采集任务](#104-更新采集任务) | `/api/bigdata/job/{id}` | PUT | ✅ | MySQL CRUD |
| 49 | [删除采集任务](#105-删除采集任务) | `/api/bigdata/job/{id}` | DELETE | ✅ | MySQL CRUD |
| 50 | [手动触发任务](#106-手动触发任务) | `/api/bigdata/job/{id}/trigger` | POST | ✅ | 运行时执行 |
| 51 | [运行历史](#107-运行历史) | `/api/bigdata/job/{id}/runs` | GET | ✅ | MySQL CRUD |
| 52 | [DS 回调](#108-ds-回调) | `/api/bigdata/job/run/callback` | POST | Token | DS 回调 |
| 53 | [血缘边列表（分页）](#111-血缘边列表分页) | `/api/bigdata/lineage` | GET | ✅ | MySQL CRUD |
| 54 | [新增血缘边](#112-新增血缘边) | `/api/bigdata/lineage` | POST | ✅ | MySQL CRUD |
| 55 | [删除血缘边](#113-删除血缘边) | `/api/bigdata/lineage/{id}` | DELETE | ✅ | MySQL CRUD |
| 56 | [下游依赖](#114-下游依赖) | `/api/bigdata/lineage/downstream` | GET | ✅ | MySQL CRUD |
| 57 | [上游血缘](#115-上游血缘) | `/api/bigdata/lineage/upstream` | GET | ✅ | MySQL CRUD |
| 58 | [指标列表（分页）](#121-指标列表分页) | `/api/bigdata/metric` | GET | ✅ | MySQL CRUD |
| 59 | [指标详情](#122-指标详情) | `/api/bigdata/metric/{id}` | GET | ✅ | MySQL CRUD |
| 60 | [新增指标](#123-新增指标) | `/api/bigdata/metric` | POST | ✅ | MySQL CRUD |
| 61 | [更新指标](#124-更新指标) | `/api/bigdata/metric/{id}` | PUT | ✅ | MySQL CRUD |
| 62 | [删除指标](#125-删除指标) | `/api/bigdata/metric/{id}` | DELETE | ✅ | MySQL CRUD |
| 63 | [指标取值序列](#126-指标取值序列) | `/api/bigdata/metric/{code}/values` | GET | ✅ | MySQL CRUD |
| 64 | [记录指标取值](#127-记录指标取值) | `/api/bigdata/metric/values` | POST | ✅ | MySQL CRUD |
| 65 | [注册 API 客户端](#131-注册-api-客户端) | `/api/bigdata/openapi/clients` | POST | ✅ | MySQL CRUD |
| 66 | [API 客户端列表](#132-api-客户端列表) | `/api/bigdata/openapi/clients` | GET | ✅ | MySQL CRUD |
| 67 | [更新客户端状态](#133-更新客户端状态) | `/api/bigdata/openapi/clients/{id}/status` | PUT | ✅ | MySQL CRUD |
| 68 | [客户端调用日志](#134-客户端调用日志) | `/api/bigdata/openapi/clients/{id}/logs` | GET | ✅ | MySQL CRUD |
| 69 | [轮转密钥](#135-轮转密钥) | `/api/bigdata/openapi/clients/{id}/rotate-key` | POST | ✅ | 运行时生成 |
| 70 | [平台健康概览](#141-平台健康概览) | `/api/bigdata/ops/health` | GET | ✅ | 运行时探测 |
| 71 | [JVM 指标](#142-jvm-指标) | `/api/bigdata/ops/metrics` | GET | ✅ | 运行时探测 |
| 72 | [审计日志查询](#151-审计日志查询) | `/api/bigdata/audit` | GET | ✅ | MySQL CRUD |
| 73 | [大屏列表（分页）](#161-大屏列表分页) | `/api/bigdata/screen` | GET | ✅ | MySQL CRUD |
| 74 | [大屏详情](#162-大屏详情) | `/api/bigdata/screen/{id}` | GET | ✅ | MySQL CRUD |
| 75 | [新增大屏](#163-新增大屏) | `/api/bigdata/screen` | POST | ✅ | MySQL CRUD |
| 76 | [更新大屏](#164-更新大屏) | `/api/bigdata/screen/{id}` | PUT | ✅ | MySQL CRUD |
| 77 | [删除大屏](#165-删除大屏) | `/api/bigdata/screen/{id}` | DELETE | ✅ | MySQL CRUD |
| 78 | [发布大屏](#166-发布大屏) | `/api/bigdata/screen/{id}/publish` | POST | ✅ | MySQL CRUD |
| 79 | [列出 Widget](#167-列出-widget) | `/api/bigdata/screen/{id}/widgets` | GET | ✅ | MySQL CRUD |
| 80 | [新增 Widget](#168-新增-widget) | `/api/bigdata/screen/{id}/widgets` | POST | ✅ | MySQL CRUD |
| 81 | [删除 Widget](#169-删除-widget) | `/api/bigdata/screen/widgets/{widgetId}` | DELETE | ✅ | MySQL CRUD |
| 82 | [列出所有角色](#171-列出所有角色) | `/api/bigdata/role` | GET | ✅ | MySQL CRUD |
| 83 | [新增角色](#172-新增角色) | `/api/bigdata/role` | POST | ✅ | MySQL CRUD |
| 84 | [更新角色](#173-更新角色) | `/api/bigdata/role/{id}` | PUT | ✅ | MySQL CRUD |
| 85 | [删除角色](#176-删除角色) | `/api/bigdata/role/{id}` | DELETE | ✅ | MySQL CRUD |
| 86 | [列出所有权限](#174-列出所有权限) | `/api/bigdata/role/permissions` | GET | ✅ | MySQL CRUD |
| 87 | [新增权限](#175-新增权限) | `/api/bigdata/role/permissions` | POST | ✅ | MySQL CRUD |
| 88 | [删除权限](#177-删除权限) | `/api/bigdata/role/permissions/{id}` | DELETE | ✅ | MySQL CRUD |
| 89 | [查询角色已绑定权限](#178-查询角色已绑定权限) | `/api/bigdata/role/{roleCode}/permissions` | GET | ✅ | MySQL CRUD |
| 90 | [覆盖式分配角色权限](#179-覆盖式分配角色权限) | `/api/bigdata/role/{roleCode}/permissions` | POST | ✅ | MySQL CRUD |

---

## 2. 数据大屏 DashboardController（`/api/bigdata/dashboard`）

> 所有端点返回聚合数据，数据来自真实 SQL 查询，无硬编码。数据源：MySQL（JdbcTemplate）。

### 2.1 KPI 汇总统计

**URL**：`GET /api/bigdata/dashboard/stats`
**认证**：需要 JWT Bearer
**错误码**：`1100100`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `DashboardStatsResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `totalOrchards` | number | ✅ | 果园总数 |
| `totalFarmers` | number | ✅ | 农户总数 |
| `totalOrders` | number | ✅ | 订单总数 |
| `totalTradeAmount` | number | ✅ | 交易总额（精度 2 位） |
| `totalTraceChains` | number | ✅ | 溯源链总数 |

```typescript
export interface DashboardStatsResponse {
  totalOrchards: number;
  totalFarmers: number;
  totalOrders: number;
  totalTradeAmount: number;
  totalTraceChains: number;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalOrchards": 120,
    "totalFarmers": 85,
    "totalOrders": 350,
    "totalTradeAmount": 1925000.00,
    "totalTraceChains": 280
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 1100100,
  "message": "KPI统计查询失败",
  "data": null
}
```

---

### 2.2 近6个月采收趋势（按月）

**URL**：`GET /api/bigdata/dashboard/harvest-trend`
**认证**：需要 JWT Bearer
**错误码**：`1100100`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `List<HarvestTrendItem>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `month` | string | ✅ | 月份，格式 `yyyy-MM` |
| `harvestAmount` | number | ✅ | 采收量（kg） |

```typescript
export interface HarvestTrendItem {
  month: string;
  harvestAmount: number;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    { "month": "2025-11", "harvestAmount": 45000 },
    { "month": "2025-12", "harvestAmount": 52000 },
    { "month": "2026-01", "harvestAmount": 38000 },
    { "month": "2026-02", "harvestAmount": 29000 },
    { "month": "2026-03", "harvestAmount": 41000 },
    { "month": "2026-04", "harvestAmount": 33000 }
  ]
}
```

---

### 2.3 近6个月交易趋势（按月）

**URL**：`GET /api/bigdata/dashboard/trade-trend`
**认证**：需要 JWT Bearer
**错误码**：`1100100`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `List<TradeTrendItem>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `month` | string | ✅ | 月份，格式 `yyyy-MM` |
| `tradeAmount` | number | ✅ | 交易额（精度 2 位） |
| `orderCount` | number | ✅ | 订单数 |

```typescript
export interface TradeTrendItem {
  month: string;
  tradeAmount: number;
  orderCount: number;
}
```

---

### 2.4 苹果品种分布（按采收量）

**URL**：`GET /api/bigdata/dashboard/variety-distribution`
**认证**：需要 JWT Bearer
**错误码**：`1100100`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `List<VarietyDistributionItem>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `variety` | string | ✅ | 品种名称 |
| `harvestAmount` | number | ✅ | 采收量（kg） |
| `percentage` | number | ✅ | 占比（0~100，精度 2 位） |

```typescript
export interface VarietyDistributionItem {
  variety: string;
  harvestAmount: number;
  percentage: number;
}
```

---

### 2.5 今年采收量 TOP5 果园

**URL**：`GET /api/bigdata/dashboard/top-orchards`
**认证**：需要 JWT Bearer
**错误码**：`1100100`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `List<TopOrchardItem>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `orchardName` | string | ✅ | 果园名称 |
| `orchardId` | number | ✅ | 果园 ID |
| `harvestAmount` | number | ✅ | 采收量（kg） |

```typescript
export interface TopOrchardItem {
  orchardName: string;
  orchardId: number;
  harvestAmount: number;
}
```

---

### 2.6 供需对比（按品种）

**URL**：`GET /api/bigdata/dashboard/supply-demand`
**认证**：需要 JWT Bearer
**错误码**：`1100100`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `List<SupplyDemandItem>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `variety` | string | ✅ | 品种名称 |
| `supplyAmount` | number | ✅ | 供应量（kg） |
| `demandAmount` | number | ✅ | 需求量（kg） |

```typescript
export interface SupplyDemandItem {
  variety: string;
  supplyAmount: number;
  demandAmount: number;
}
```

---

### 2.7 溯源链状态分布

**URL**：`GET /api/bigdata/dashboard/trace-stats`
**认证**：需要 JWT Bearer
**错误码**：`1100100`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `List<TraceStatusItem>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `status` | `EnumValue<string>` | ✅ | 溯源链状态 |
| `count` | number | ✅ | 数量 |

```typescript
export interface TraceStatusItem {
  status: EnumValue<string>;
  count: number;
}
```

---

## 3. 大屏补充统计 DashboardSupplementController（`/api/bigdata/dashboard`）

> 使用 JdbcTemplate **跨模块查询**，聚合仓储、金融、交易等模块数据。

### 3.1 仓储总量统计

**URL**：`GET /api/bigdata/dashboard/warehouse-stock`
**认证**：需要 JWT Bearer
**错误码**：`1100200`
**变更历史**：2026-04-12 | init | 架构师
**数据来源**：`wh_warehouse` 表

#### 请求参数

无。

#### 响应体 `WarehouseStockResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `warehouseCount` | number | ✅ | 仓库数量 |
| `totalCapacity` | number | ✅ | 总容量（kg） |
| `usedCapacity` | number | ✅ | 已用容量（kg） |

```typescript
export interface WarehouseStockResponse {
  warehouseCount: number;
  totalCapacity: number;
  usedCapacity: number;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "warehouseCount": 5,
    "totalCapacity": 500000,
    "usedCapacity": 320000
  }
}
```

---

### 3.2 金融规模统计

**URL**：`GET /api/bigdata/dashboard/finance-scale`
**认证**：需要 JWT Bearer
**错误码**：`1100201`
**变更历史**：2026-04-12 | init | 架构师
**数据来源**：`fn_loan` 表

#### 请求参数

无。

#### 响应体 `FinanceScaleResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `loanCount` | number | ✅ | 贷款笔数 |
| `totalAmount` | number | ✅ | 贷款总额（精度 2 位） |

```typescript
export interface FinanceScaleResponse {
  loanCount: number;
  totalAmount: number;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "loanCount": 25,
    "totalAmount": 3500000.00
  }
}
```

---

### 3.3 品种价格趋势（按日均价）

**URL**：`GET /api/bigdata/dashboard/price-trend`
**认证**：需要 JWT Bearer
**错误码**：`1100202`
**变更历史**：2026-04-12 | init | 架构师
**数据来源**：`td_trade_order` 表，`AVG(unit_price) GROUP BY trade_date`

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `variety` | string | ❌ | `红富士` | 品种名称 |
| `days` | number | ❌ | `30` | 统计近 N 天 |

```typescript
export interface PriceTrendRequest {
  variety?: string;
  days?: number;
}
```

#### 响应体 `List<PriceTrendItem>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `tradeDate` | string | ✅ | 交易日期，格式 `yyyy-MM-dd` |
| `avgPrice` | number | ✅ | 日均价（精度 2 位） |

```typescript
export interface PriceTrendItem {
  tradeDate: string;
  avgPrice: number;
}
```

---

### 3.4 损耗率统计

**URL**：`GET /api/bigdata/dashboard/loss-rate`
**认证**：需要 JWT Bearer
**错误码**：`1100203`
**变更历史**：2026-04-12 | init | 架构师
**数据来源**：`wh_warehouse_record` 表
**计算公式**：`lossRatePct = (inbound - outbound) / inbound * 100`

#### 请求参数

无。

#### 响应体 `LossRateResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `inbound` | number | ✅ | 入库量（kg） |
| `outbound` | number | ✅ | 出库量（kg） |
| `lossRatePct` | number | ✅ | 损耗率百分比（精度 2 位） |

```typescript
export interface LossRateResponse {
  inbound: number;
  outbound: number;
  lossRatePct: number;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "inbound": 100000,
    "outbound": 95000,
    "lossRatePct": 5.0
  }
}
```

---

## 4. 数据看板 MVP DashboardMvpController（`/api/dashboard`）

> MVP 版本看板接口，面向演示场景。数据来自 `DashboardMvpMapper` 真实 SQL 聚合。

### 4.1 MVP KPI 汇总统计

**URL**：`GET /api/dashboard/stats`
**认证**：需要 JWT Bearer
**错误码**：`1100300`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `MvpDashboardStatsResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `totalOrchards` | number | ✅ | 活跃果园总数 |
| `totalBatches` | number | ✅ | 批次总数 |
| `totalTradeAmount` | number | ✅ | 交易总额（精度 2 位） |
| `pendingOrders` | number | ✅ | 待处理订单数 |

```typescript
export interface MvpDashboardStatsResponse {
  totalOrchards: number;
  totalBatches: number;
  totalTradeAmount: number;
  pendingOrders: number;
}
```

---

### 4.2 MVP 近6个月交易趋势

**URL**：`GET /api/dashboard/trend`
**认证**：需要 JWT Bearer
**错误码**：`1100300`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `List<MvpTrendItem>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `month` | string | ✅ | 月份，格式 `yyyy-MM` |
| `tradeAmount` | number | ✅ | 交易额（精度 2 位） |

```typescript
export interface MvpTrendItem {
  month: string;
  tradeAmount: number;
}
```

---

### 4.3 MVP TOP5 果园交易量排名

**URL**：`GET /api/dashboard/top-orchards`
**认证**：需要 JWT Bearer
**错误码**：`1100300`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `List<MvpTopOrchardItem>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `orchardName` | string | ✅ | 果园名称 |
| `tradeVolume` | number | ✅ | 交易量 |
| `rank` | number | ✅ | 排名 |

```typescript
export interface MvpTopOrchardItem {
  orchardName: string;
  tradeVolume: number;
  rank: number;
}
```

---

### 4.4 MVP 待处理事项统计

**URL**：`GET /api/dashboard/pending-actions`
**认证**：需要 JWT Bearer
**错误码**：`1100300`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `List<MvpPendingActionItem>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `actionType` | string | ✅ | 事项类型 |
| `count` | number | ✅ | 数量 |

```typescript
export interface MvpPendingActionItem {
  actionType: string;
  count: number;
}
```

---

## 5. 全量统计 StatisticsController（`/api/stats`）

> 全量 KPI 统计，覆盖所有模块的 COUNT/SUM 聚合。数据来自 `StatisticsMapper`。

### 5.1 KPI 汇总

**URL**：`GET /api/stats/summary`
**认证**：需要 JWT Bearer
**错误码**：`1100400`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `StatsSummaryResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `totalOrchards` | number | ✅ | 果园总数 |
| `totalBatches` | number | ✅ | 批次总数 |
| `totalOrders` | number | ✅ | 订单总数 |
| `totalUsers` | number | ✅ | 用户总数 |
| `activeOrchards` | number | ✅ | 活跃果园数 |
| `completedOrders` | number | ✅ | 已完成订单数 |
| `totalTradeAmount` | number | ✅ | 交易总额（精度 2 位） |
| `totalProducts` | number | ✅ | 产品总数 |
| `lowInventory` | number | ✅ | 低库存产品数 |
| `totalUsages` | number | ✅ | 农资使用记录总数 |
| `totalWarehouses` | number | ✅ | 仓库总数 |

```typescript
export interface StatsSummaryResponse {
  totalOrchards: number;
  totalBatches: number;
  totalOrders: number;
  totalUsers: number;
  activeOrchards: number;
  completedOrders: number;
  totalTradeAmount: number;
  totalProducts: number;
  lowInventory: number;
  totalUsages: number;
  totalWarehouses: number;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
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

---

### 5.2 近6个月月度交易趋势

**URL**：`GET /api/stats/trend`
**认证**：需要 JWT Bearer
**错误码**：`1100400`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `List<MonthlyTrendItem>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `month` | string | ✅ | 月份，格式 `yyyy-MM` |
| `tradeAmount` | number | ✅ | 交易额（精度 2 位） |
| `orderCount` | number | ✅ | 订单数 |

```typescript
export interface MonthlyTrendItem {
  month: string;
  tradeAmount: number;
  orderCount: number;
}
```

---

### 5.3 TOP5 苹果品种

**URL**：`GET /api/stats/top-varieties`
**认证**：需要 JWT Bearer
**错误码**：`1100400`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `List<TopVarietyItem>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `variety` | string | ✅ | 品种名称 |
| `orderCount` | number | ✅ | 订单数 |
| `tradeAmount` | number | ✅ | 交易额（精度 2 位） |

```typescript
export interface TopVarietyItem {
  variety: string;
  orderCount: number;
  tradeAmount: number;
}
```

---

### 5.4 各模块待处理事项统计

**URL**：`GET /api/stats/pending`
**认证**：需要 JWT Bearer
**错误码**：`1100400`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `PendingStatsResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `pendingOrders` | number | ✅ | 待处理订单数 |
| `unpaidOrders` | number | ✅ | 未支付订单数 |
| `draftBatches` | number | ✅ | 草稿批次数 |
| `inactiveFarmers` | number | ✅ | 不活跃农户数 |

```typescript
export interface PendingStatsResponse {
  pendingOrders: number;
  unpaidOrders: number;
  draftBatches: number;
  inactiveFarmers: number;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pendingOrders": 15,
    "unpaidOrders": 8,
    "draftBatches": 5,
    "inactiveFarmers": 12
  }
}
```

---

## 6. 分析报告 AnalysisReportController（`/api/bigdata/report`）

### 6.1 生成分析报告

**URL**：`POST /api/bigdata/report/generate`
**认证**：需要 JWT Bearer
**错误码**：`1100500` / `1100501`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `type` | string | ✅ | 报告类型，见 ReportType | `WEEKLY` |
| `period` | string | ✅ | 报告周期 | `2026-W15` / `2026-04` / `2026-Q2` |

```typescript
export interface GenerateReportRequest {
  type: string;
  period: string;
}
```

**业务规则**：自动聚合各模块数据生成报告，生成后状态为 `DRAFT`。

#### 响应体 `AnalysisReportResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 报告 ID |
| `reportNo` | string | ✅ | 报告编号，格式 `RPT-2026-04-001` |
| `reportType` | `EnumValue<string>` | ✅ | 报告类型，见 ReportType |
| `title` | string | ✅ | 报告标题 |
| `period` | string | ✅ | 报告周期 |
| `generatedTime` | string | ✅ | 生成时间 |
| `plantingSection` | string \| null | ❌ | 种植板块聚合（JSON） |
| `tradeSection` | string \| null | ❌ | 交易板块聚合（JSON） |
| `warehouseSection` | string \| null | ❌ | 仓储板块聚合（JSON） |
| `financeSection` | string \| null | ❌ | 金融板块聚合（JSON） |
| `summary` | string \| null | ❌ | 报告摘要 |
| `status` | `EnumValue<string>` | ✅ | 报告状态，见 ReportStatus |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface AnalysisReportResponse {
  id: number;
  reportNo: string;
  reportType: EnumValue<string>;
  title: string;
  period: string;
  generatedTime: string;
  plantingSection: string | null;
  tradeSection: string | null;
  warehouseSection: string | null;
  financeSection: string | null;
  summary: string | null;
  status: EnumValue<string>;
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
    "id": 1,
    "reportNo": "RPT-2026-04-001",
    "reportType": { "code": "WEEKLY", "desc": "周报" },
    "title": "第15周分析报告",
    "period": "2026-W15",
    "generatedTime": "2026-04-12 10:00:00",
    "plantingSection": null,
    "tradeSection": null,
    "warehouseSection": null,
    "financeSection": null,
    "summary": null,
    "status": { "code": "DRAFT", "desc": "草稿" },
    "createdAt": "2026-04-12 10:00:00",
    "updatedAt": "2026-04-12 10:00:00"
  }
}
```

---

### 6.2 报告列表（分页）

**URL**：`GET /api/bigdata/report`
**认证**：需要 JWT Bearer
**错误码**：`1100502`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | number | ❌ | `1` | 页码 |
| `size` | number | ❌ | `10` | 每页条数 |

#### 响应体 `IPage<AnalysisReportResponse>`

使用 MyBatis-Plus `IPage` 结构（与 PageResult 类似）：

| 字段 | 类型 | 说明 |
|------|------|------|
| `records` | AnalysisReportResponse[] | 当前页数据 |
| `total` | number | 总数 |
| `current` | number | 当前页码 |
| `size` | number | 每页数量 |

---

### 6.3 报告详情

**URL**：`GET /api/bigdata/report/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1100503`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `AnalysisReportResponse`

同 §6.1 响应体。

#### 响应示例（失败）

```json
{
  "code": 1100503,
  "message": "报告不存在",
  "data": null
}
```

---

### 6.4 发布报告

**URL**：`PUT /api/bigdata/report/{id}/publish`
**认证**：需要 JWT Bearer
**错误码**：`1100504`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

路径参数：`id` — 报告 ID。

#### 响应体

`R<Void>` — 无 data。

**状态机**：`DRAFT` → `PUBLISHED`

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

## 7. 数据资产目录 DataAssetController（`/api/bigdata/asset`）

### 7.1 资产列表（分页）

**URL**：`GET /api/bigdata/asset`
**认证**：需要 JWT Bearer
**错误码**：`1100600`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | number | ❌ | `1` | 页码 |
| `size` | number | ❌ | `20` | 每页条数 |
| `keyword` | string | ❌ | — | 资产编码/名称模糊搜索 |
| `securityLevel` | string | ❌ | — | 安全等级筛选，见 SecurityLevel |

```typescript
export interface DataAssetPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  securityLevel?: string;
}
```

#### 响应体 `PageResult<DataAssetResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 资产 ID |
| `assetCode` | string | ✅ | 资产编码 |
| `databaseName` | string | ✅ | 数据库名 |
| `schemaName` | string \| null | ❌ | Schema 名 |
| `tableName` | string | ✅ | 表名 |
| `bizName` | string | ✅ | 业务名称 |
| `bizDescription` | string \| null | ❌ | 业务描述 |
| `owner` | string \| null | ❌ | 负责人 |
| `securityLevel` | `EnumValue<string>` | ✅ | 安全等级，见 SecurityLevel |
| `sourceSystem` | string \| null | ❌ | 来源系统 |
| `recordCount` | number \| null | ❌ | 记录数 |
| `lastUpdateTime` | string \| null | ❌ | 最近更新时间 |
| `tags` | string \| null | ❌ | 标签（逗号分隔） |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface DataAssetResponse {
  id: number;
  assetCode: string;
  databaseName: string;
  schemaName: string | null;
  tableName: string;
  bizName: string;
  bizDescription: string | null;
  owner: string | null;
  securityLevel: EnumValue<string>;
  sourceSystem: string | null;
  recordCount: number | null;
  lastUpdateTime: string | null;
  tags: string | null;
  createdAt: string;
  updatedAt: string;
}
```

---

### 7.2 资产详情

**URL**：`GET /api/bigdata/asset/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1100601`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `DataAssetResponse`

同 §7.1 响应体。

---

### 7.3 新增资产

**URL**：`POST /api/bigdata/asset`
**认证**：需要 JWT Bearer
**错误码**：`1100602`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `CreateDataAssetRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `assetCode` | string | ✅ | 资产编码（唯一） |
| `databaseName` | string | ✅ | 数据库名 |
| `schemaName` | string | ❌ | Schema 名 |
| `tableName` | string | ✅ | 表名 |
| `bizName` | string | ✅ | 业务名称 |
| `bizDescription` | string | ❌ | 业务描述 |
| `owner` | string | ❌ | 负责人 |
| `securityLevel` | string | ✅ | 安全等级 code，见 SecurityLevel |
| `sourceSystem` | string | ❌ | 来源系统 |
| `tags` | string | ❌ | 标签（逗号分隔） |

```typescript
export interface CreateDataAssetRequest {
  assetCode: string;
  databaseName: string;
  schemaName?: string;
  tableName: string;
  bizName: string;
  bizDescription?: string;
  owner?: string;
  securityLevel: string;
  sourceSystem?: string;
  tags?: string;
}
```

#### 响应体 `DataAssetResponse`

---

### 7.4 更新资产

**URL**：`PUT /api/bigdata/asset/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1100603`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `UpdateDataAssetRequest`

同 `CreateDataAssetRequest`，所有字段可选。

---

### 7.5 删除资产

**URL**：`DELETE /api/bigdata/asset/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1100604`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体

`R<Boolean>` — `data: true` 表示删除成功。

---

### 7.6 列出资产字段

**URL**：`GET /api/bigdata/asset/{id}/fields`
**认证**：需要 JWT Bearer
**错误码**：`1100605`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `List<DataAssetFieldResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 字段 ID |
| `assetId` | number | ✅ | 所属资产 ID |
| `fieldName` | string | ✅ | 字段名 |
| `fieldType` | string | ✅ | 字段类型 |
| `bizMeaning` | string \| null | ❌ | 业务含义 |
| `isSensitive` | number | ✅ | 是否敏感（1=是，0=否） |
| `sensitiveType` | string \| null | ❌ | 敏感类型，见 SensitiveType |
| `isPk` | number | ✅ | 是否主键（1=是，0=否） |
| `nullable` | number | ✅ | 是否可空（1=是，0=否） |
| `defaultValue` | string \| null | ❌ | 默认值 |
| `ordinal` | number | ✅ | 字段顺序 |

```typescript
export interface DataAssetFieldResponse {
  id: number;
  assetId: number;
  fieldName: string;
  fieldType: string;
  bizMeaning: string | null;
  isSensitive: number;
  sensitiveType: string | null;
  isPk: number;
  nullable: number;
  defaultValue: string | null;
  ordinal: number;
}
```

---

### 7.7 新增字段

**URL**：`POST /api/bigdata/asset/{id}/fields`
**认证**：需要 JWT Bearer
**错误码**：`1100606`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `CreateDataAssetFieldRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `fieldName` | string | ✅ | 字段名 |
| `fieldType` | string | ✅ | 字段类型 |
| `bizMeaning` | string | ❌ | 业务含义 |
| `isSensitive` | number | ❌ | 是否敏感，默认 0 |
| `sensitiveType` | string | ❌ | 敏感类型 |
| `isPk` | number | ❌ | 是否主键，默认 0 |
| `nullable` | number | ❌ | 是否可空，默认 1 |
| `defaultValue` | string | ❌ | 默认值 |
| `ordinal` | number | ❌ | 字段顺序 |

#### 响应体 `DataAssetFieldResponse`

---

### 7.8 删除字段

**URL**：`DELETE /api/bigdata/asset/fields/{fieldId}`
**认证**：需要 JWT Bearer
**错误码**：`1100607`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体

`R<Boolean>` — `data: true` 表示删除成功。

---

## 8. 数据源注册 DataSourceController（`/api/bigdata/source`）

> 管理 8 个内部模块 + 4 个外部 API 数据源。

### 8.1 数据源列表（分页）

**URL**：`GET /api/bigdata/source`
**认证**：需要 JWT Bearer
**错误码**：`1100700`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | number | ❌ | `1` | 页码 |
| `size` | number | ❌ | `20` | 每页条数 |
| `category` | string | ❌ | — | 数据源分类，见 DataSourceCategory |
| `keyword` | string | ❌ | — | 名称/编码模糊搜索 |

```typescript
export interface DataSourcePageRequest {
  page?: number;
  size?: number;
  category?: string;
  keyword?: string;
}
```

#### 响应体 `PageResult<DataSourceResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 数据源 ID |
| `sourceCode` | string | ✅ | 数据源编码 |
| `sourceName` | string | ✅ | 数据源名称 |
| `sourceType` | `EnumValue<string>` | ✅ | 数据源类型，见 DataSourceType |
| `category` | `EnumValue<string>` | ✅ | 数据源分类，见 DataSourceCategory |
| `connectUrl` | string \| null | ❌ | 连接 URL |
| `username` | string \| null | ❌ | 用户名 |
| `owner` | string \| null | ❌ | 负责人 |
| `status` | `EnumValue<string>` | ✅ | 状态，见 DataSourceStatus |
| `lastTestTime` | string \| null | ❌ | 最近测试时间 |
| `lastTestResult` | string \| null | ❌ | 最近测试结果 |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

> **注意**：`passwordEnc` 和 `extraConfig` 字段不返回给客户端。

```typescript
export interface DataSourceResponse {
  id: number;
  sourceCode: string;
  sourceName: string;
  sourceType: EnumValue<string>;
  category: EnumValue<string>;
  connectUrl: string | null;
  username: string | null;
  owner: string | null;
  status: EnumValue<string>;
  lastTestTime: string | null;
  lastTestResult: string | null;
  remark: string | null;
  createdAt: string;
  updatedAt: string;
}
```

---

### 8.2 数据源详情

**URL**：`GET /api/bigdata/source/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1100701`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `DataSourceResponse`

同 §8.1。

---

### 8.3 新增数据源

**URL**：`POST /api/bigdata/source`
**认证**：需要 JWT Bearer
**错误码**：`1100702`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `CreateDataSourceRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `sourceCode` | string | ✅ | 数据源编码（唯一） |
| `sourceName` | string | ✅ | 数据源名称 |
| `sourceType` | string | ✅ | 类型 code，见 DataSourceType |
| `category` | string | ✅ | 分类 code，见 DataSourceCategory |
| `connectUrl` | string | ❌ | 连接 URL |
| `username` | string | ❌ | 用户名 |
| `passwordEnc` | string | ❌ | AES 加密密码 |
| `extraConfig` | string | ❌ | 额外配置（JSON） |
| `owner` | string | ❌ | 负责人 |
| `remark` | string | ❌ | 备注 |

---

### 8.4 更新数据源

**URL**：`PUT /api/bigdata/source/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1100703`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `UpdateDataSourceRequest`

同 `CreateDataSourceRequest`，所有字段可选。

---

### 8.5 删除数据源

**URL**：`DELETE /api/bigdata/source/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1100704`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体

`R<Boolean>` — `data: true`。

---

### 8.6 连通性测试

**URL**：`POST /api/bigdata/source/{id}/test`
**认证**：需要 JWT Bearer
**错误码**：`1100705`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

路径参数：`id` — 数据源 ID。

#### 响应体

`R<String>` — `data` 为连接结果描述。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "连接成功 (延迟: 15ms)"
}
```

---

## 9. 数据质量 DqRuleController（`/api/bigdata/dq`）

### 9.1 DQ 规则列表（分页）

**URL**：`GET /api/bigdata/dq/rules`
**认证**：需要 JWT Bearer
**错误码**：`1100800`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | number | ❌ | `1` | 页码 |
| `size` | number | ❌ | `20` | 每页条数 |
| `ruleType` | string | ❌ | — | 规则类型，见 DqRuleType |
| `assetId` | number | ❌ | — | 关联资产 ID |

#### 响应体 `PageResult<DqRuleResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 规则 ID |
| `ruleCode` | string | ✅ | 规则编码 |
| `ruleName` | string | ✅ | 规则名称 |
| `assetId` | number \| null | ❌ | 关联资产 ID |
| `assetCode` | string \| null | ❌ | 关联资产编码 |
| `fieldName` | string \| null | ❌ | 检查字段名 |
| `ruleType` | `EnumValue<string>` | ✅ | 规则类型，见 DqRuleType |
| `ruleExpr` | string \| null | ❌ | 规则表达式 |
| `severity` | `EnumValue<string>` | ✅ | 严重级别，见 DqSeverity |
| `enabled` | number | ✅ | 是否启用（1=是，0=否） |
| `scheduleCron` | string \| null | ❌ | 定时 Cron |
| `owner` | string \| null | ❌ | 负责人 |
| `lastRunTime` | string \| null | ❌ | 最近运行时间 |
| `lastScore` | number \| null | ❌ | 最近得分（精度 2 位） |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface DqRuleResponse {
  id: number;
  ruleCode: string;
  ruleName: string;
  assetId: number | null;
  assetCode: string | null;
  fieldName: string | null;
  ruleType: EnumValue<string>;
  ruleExpr: string | null;
  severity: EnumValue<string>;
  enabled: number;
  scheduleCron: string | null;
  owner: string | null;
  lastRunTime: string | null;
  lastScore: number | null;
  remark: string | null;
  createdAt: string;
  updatedAt: string;
}
```

---

### 9.2 DQ 规则详情

**URL**：`GET /api/bigdata/dq/rules/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1100801`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `DqRuleResponse`

同 §9.1。

---

### 9.3 新增 DQ 规则

**URL**：`POST /api/bigdata/dq/rules`
**认证**：需要 JWT Bearer
**错误码**：`1100802`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `CreateDqRuleRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `ruleCode` | string | ✅ | 规则编码（唯一） |
| `ruleName` | string | ✅ | 规则名称 |
| `assetId` | number | ❌ | 关联资产 ID |
| `assetCode` | string | ❌ | 关联资产编码 |
| `fieldName` | string | ❌ | 检查字段名 |
| `ruleType` | string | ✅ | 规则类型 code，见 DqRuleType |
| `ruleExpr` | string | ❌ | 规则表达式 |
| `severity` | string | ✅ | 严重级别 code，见 DqSeverity |
| `enabled` | number | ❌ | 是否启用，默认 1 |
| `scheduleCron` | string | ❌ | 定时 Cron |
| `owner` | string | ❌ | 负责人 |
| `remark` | string | ❌ | 备注 |

---

### 9.4 更新 DQ 规则

**URL**：`PUT /api/bigdata/dq/rules/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1100803`
**变更历史**：2026-04-12 | init | 架构师

---

### 9.5 删除 DQ 规则

**URL**：`DELETE /api/bigdata/dq/rules/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1100804`
**变更历史**：2026-04-12 | init | 架构师

---

### 9.6 手动触发检查

**URL**：`POST /api/bigdata/dq/rules/{id}/check`
**认证**：需要 JWT Bearer
**错误码**：`1100805`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `DqCheckResultResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 结果 ID |
| `ruleId` | number | ✅ | 规则 ID |
| `ruleCode` | string | ✅ | 规则编码 |
| `checkTime` | string | ✅ | 检查时间 |
| `totalRows` | number | ✅ | 总检查行数 |
| `badRows` | number | ✅ | 不合格行数 |
| `score` | number | ✅ | 得分（0~100，精度 2 位） |
| `pass` | number | ✅ | 是否通过（1=是，0=否） |
| `sampleBadRows` | string \| null | ❌ | 不合格样例（JSON） |
| `errorMessage` | string \| null | ❌ | 错误信息 |

```typescript
export interface DqCheckResultResponse {
  id: number;
  ruleId: number;
  ruleCode: string;
  checkTime: string;
  totalRows: number;
  badRows: number;
  score: number;
  pass: number;
  sampleBadRows: string | null;
  errorMessage: string | null;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "ruleId": 1,
    "ruleCode": "R001",
    "checkTime": "2026-04-11 10:30:00",
    "totalRows": 100,
    "badRows": 5,
    "score": 95.00,
    "pass": 0,
    "sampleBadRows": null,
    "errorMessage": null
  }
}
```

---

### 9.7 检查结果历史

**URL**：`GET /api/bigdata/dq/rules/{id}/results`
**认证**：需要 JWT Bearer
**错误码**：`1100806`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | number | ❌ | `1` | 页码 |
| `size` | number | ❌ | `20` | 每页条数 |

#### 响应体 `PageResult<DqCheckResultResponse>`

同 §9.6 响应结构。

---

## 10. 采集任务 CollectJobController（`/api/bigdata/job`）

### 10.1 采集任务列表（分页）

**URL**：`GET /api/bigdata/job`
**认证**：需要 JWT Bearer
**错误码**：`1100900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | number | ❌ | `1` | 页码 |
| `size` | number | ❌ | `20` | 每页条数 |
| `jobType` | string | ❌ | — | 任务类型，见 JobType |
| `sourceId` | number | ❌ | — | 数据源 ID |

#### 响应体 `PageResult<CollectJobResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 任务 ID |
| `jobCode` | string | ✅ | 任务编码 |
| `jobName` | string | ✅ | 任务名称 |
| `sourceId` | number \| null | ❌ | 数据源 ID |
| `jobType` | `EnumValue<string>` | ✅ | 任务类型，见 JobType |
| `cronExpr` | string \| null | ❌ | Cron 表达式 |
| `targetTable` | string \| null | ❌ | 目标表 |
| `script` | string \| null | ❌ | 脚本内容 |
| `enabled` | number | ✅ | 是否启用（1=是，0=否） |
| `retryTimes` | number \| null | ❌ | 重试次数 |
| `timeoutSeconds` | number \| null | ❌ | 超时秒数 |
| `lastRunTime` | string \| null | ❌ | 最近运行时间 |
| `lastRunStatus` | `EnumValue<string>` | ✅ | 最近运行状态，见 JobRunStatus |
| `owner` | string \| null | ❌ | 负责人 |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface CollectJobResponse {
  id: number;
  jobCode: string;
  jobName: string;
  sourceId: number | null;
  jobType: EnumValue<string>;
  cronExpr: string | null;
  targetTable: string | null;
  script: string | null;
  enabled: number;
  retryTimes: number | null;
  timeoutSeconds: number | null;
  lastRunTime: string | null;
  lastRunStatus: EnumValue<string>;
  owner: string | null;
  remark: string | null;
  createdAt: string;
  updatedAt: string;
}
```

---

### 10.2 采集任务详情

**URL**：`GET /api/bigdata/job/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1100901`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `CollectJobResponse`

同 §10.1。

---

### 10.3 新增采集任务

**URL**：`POST /api/bigdata/job`
**认证**：需要 JWT Bearer
**错误码**：`1100902`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `CreateCollectJobRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `jobCode` | string | ✅ | 任务编码（唯一） |
| `jobName` | string | ✅ | 任务名称 |
| `sourceId` | number | ❌ | 数据源 ID |
| `jobType` | string | ✅ | 任务类型 code，见 JobType |
| `cronExpr` | string | ❌ | Cron 表达式 |
| `targetTable` | string | ❌ | 目标表 |
| `script` | string | ❌ | 脚本内容 |
| `enabled` | number | ❌ | 是否启用，默认 1 |
| `retryTimes` | number | ❌ | 重试次数 |
| `timeoutSeconds` | number | ❌ | 超时秒数 |
| `owner` | string | ❌ | 负责人 |
| `remark` | string | ❌ | 备注 |

---

### 10.4 更新采集任务

**URL**：`PUT /api/bigdata/job/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1100903`
**变更历史**：2026-04-12 | init | 架构师

---

### 10.5 删除采集任务

**URL**：`DELETE /api/bigdata/job/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1100904`
**变更历史**：2026-04-12 | init | 架构师

---

### 10.6 手动触发任务

**URL**：`POST /api/bigdata/job/{id}/trigger`
**认证**：需要 JWT Bearer
**错误码**：`1100905`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `triggeredBy` | string | ❌ | `admin` | 触发人 |

#### 响应体 `CollectJobRunResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 运行记录 ID |
| `jobId` | number | ✅ | 任务 ID |
| `jobCode` | string | ✅ | 任务编码 |
| `runStatus` | `EnumValue<string>` | ✅ | 运行状态，见 RunStatus |
| `startTime` | string | ✅ | 开始时间 |
| `endTime` | string \| null | ❌ | 结束时间 |
| `durationMs` | number \| null | ❌ | 耗时（毫秒） |
| `rowsRead` | number \| null | ❌ | 读取行数 |
| `rowsWritten` | number \| null | ❌ | 写入行数 |
| `errorMessage` | string \| null | ❌ | 错误信息 |
| `triggerType` | `EnumValue<string>` | ✅ | 触发类型，见 TriggerType |
| `triggeredBy` | string | ✅ | 触发人 |

```typescript
export interface CollectJobRunResponse {
  id: number;
  jobId: number;
  jobCode: string;
  runStatus: EnumValue<string>;
  startTime: string;
  endTime: string | null;
  durationMs: number | null;
  rowsRead: number | null;
  rowsWritten: number | null;
  errorMessage: string | null;
  triggerType: EnumValue<string>;
  triggeredBy: string;
}
```

---

### 10.7 运行历史

**URL**：`GET /api/bigdata/job/{id}/runs`
**认证**：需要 JWT Bearer
**错误码**：`1100906`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | number | ❌ | `1` | 页码 |
| `size` | number | ❌ | `20` | 每页条数 |

#### 响应体 `PageResult<CollectJobRunResponse>`

同 §10.6 响应结构。

---

### 10.8 DS 回调

**URL**：`POST /api/bigdata/job/run/callback`
**认证**：`X-DS-Token` Header（非 JWT）
**错误码**：`1100907` / `1100908`
**变更历史**：2026-04-12 | init | 架构师

> 此端点由 DolphinScheduler Shell 节点调用，非前端直接调用。Token 配置：`ds.callback.token`。

#### 请求体 `JobRunCallbackRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `jobId` | number | ✅ | 采集任务 ID |
| `jobCode` | string | ✅ | 任务编码 |
| `runStatus` | string | ✅ | 运行状态：`SUCCESS` / `FAILED` / `TIMEOUT` |
| `startTime` | string | ✅ | 开始时间 |
| `endTime` | string | ❌ | 结束时间 |
| `durationMs` | number | ❌ | 耗时（毫秒） |
| `rowsRead` | number | ❌ | 读取行数 |
| `rowsWritten` | number | ❌ | 写入行数 |
| `errorMessage` | string | ❌ | 错误信息 |
| `triggerType` | string | ❌ | 触发类型：`SCHEDULED` / `MANUAL` / `RETRY` |
| `triggeredBy` | string | ❌ | 触发人 |
| `dsWorkflowInstanceId` | string | ❌ | DS 工作流实例 ID |

```typescript
export interface JobRunCallbackRequest {
  jobId: number;
  jobCode: string;
  runStatus: string;
  startTime: string;
  endTime?: string;
  durationMs?: number;
  rowsRead?: number;
  rowsWritten?: number;
  errorMessage?: string;
  triggerType?: string;
  triggeredBy?: string;
  dsWorkflowInstanceId?: string;
}
```

#### 响应体

`R<Long>` — `data` 为运行记录 ID。

#### 响应示例（失败 — Token 无效）

```json
{
  "code": 1100907,
  "message": "invalid callback token",
  "data": null
}
```

---

## 11. 数据血缘 DataLineageController（`/api/bigdata/lineage`）

> 注意：此为 DataOps 概念的数据血缘（上下游 DAG），与 apple-module-trace 的产品溯源链不同。

### 11.1 血缘边列表（分页）

**URL**：`GET /api/bigdata/lineage`
**认证**：需要 JWT Bearer
**错误码**：`1101000`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | number | ❌ | `1` | 页码 |
| `size` | number | ❌ | `20` | 每页条数 |
| `relationType` | string | ❌ | — | 关系类型，见 LineageRelationType |

#### 响应体 `PageResult<DataLineageResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 血缘边 ID |
| `upstreamType` | `EnumValue<string>` | ✅ | 上游节点类型，见 LineageNodeType |
| `upstreamId` | string | ✅ | 上游节点 ID |
| `upstreamName` | string \| null | ❌ | 上游节点名称 |
| `downstreamType` | `EnumValue<string>` | ✅ | 下游节点类型 |
| `downstreamId` | string | ✅ | 下游节点 ID |
| `downstreamName` | string \| null | ❌ | 下游节点名称 |
| `relationType` | `EnumValue<string>` | ✅ | 关系类型，见 LineageRelationType |
| `pipelineJobId` | number \| null | ❌ | 关联采集任务 ID |
| `description` | string \| null | ❌ | 描述 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface DataLineageResponse {
  id: number;
  upstreamType: EnumValue<string>;
  upstreamId: string;
  upstreamName: string | null;
  downstreamType: EnumValue<string>;
  downstreamId: string;
  downstreamName: string | null;
  relationType: EnumValue<string>;
  pipelineJobId: number | null;
  description: string | null;
  createdAt: string;
  updatedAt: string;
}
```

---

### 11.2 新增血缘边

**URL**：`POST /api/bigdata/lineage`
**认证**：需要 JWT Bearer
**错误码**：`1101001`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `CreateDataLineageRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `upstreamType` | string | ✅ | 上游节点类型 code |
| `upstreamId` | string | ✅ | 上游节点 ID |
| `upstreamName` | string | ❌ | 上游节点名称 |
| `downstreamType` | string | ✅ | 下游节点类型 code |
| `downstreamId` | string | ✅ | 下游节点 ID |
| `downstreamName` | string | ❌ | 下游节点名称 |
| `relationType` | string | ✅ | 关系类型 code |
| `pipelineJobId` | number | ❌ | 关联采集任务 ID |
| `description` | string | ❌ | 描述 |

---

### 11.3 删除血缘边

**URL**：`DELETE /api/bigdata/lineage/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1101002`
**变更历史**：2026-04-12 | init | 架构师

---

### 11.4 下游依赖

**URL**：`GET /api/bigdata/lineage/downstream`
**认证**：需要 JWT Bearer
**错误码**：`1101003`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | ✅ | 节点类型 code |
| `id` | string | ✅ | 节点 ID |

#### 响应体 `List<DataLineageResponse>`

同 §11.1 响应结构（列表，非分页）。

---

### 11.5 上游血缘

**URL**：`GET /api/bigdata/lineage/upstream`
**认证**：需要 JWT Bearer
**错误码**：`1101004`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

同 §11.4。

#### 响应体 `List<DataLineageResponse>`

同 §11.4。

---

## 12. 指标中心 MetricController（`/api/bigdata/metric`）

### 12.1 指标列表（分页）

**URL**：`GET /api/bigdata/metric`
**认证**：需要 JWT Bearer
**错误码**：`1101100`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | number | ❌ | `1` | 页码 |
| `size` | number | ❌ | `20` | 每页条数 |
| `category` | string | ❌ | — | 指标分类，见 MetricCategory |
| `isCore` | number | ❌ | — | 是否核心指标（1/0） |

#### 响应体 `PageResult<MetricDefinitionResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 指标 ID |
| `metricCode` | string | ✅ | 指标编码 |
| `metricName` | string | ✅ | 指标名称 |
| `category` | `EnumValue<string>` | ✅ | 指标分类，见 MetricCategory |
| `unit` | string \| null | ❌ | 单位 |
| `calcFormula` | string \| null | ❌ | 计算公式 |
| `sqlExpression` | string \| null | ❌ | SQL 表达式 |
| `dataSourceId` | number \| null | ❌ | 数据源 ID |
| `refreshCron` | string \| null | ❌ | 刷新 Cron |
| `owner` | string \| null | ❌ | 负责人 |
| `isCore` | number | ✅ | 是否核心指标（1=是，0=否） |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface MetricDefinitionResponse {
  id: number;
  metricCode: string;
  metricName: string;
  category: EnumValue<string>;
  unit: string | null;
  calcFormula: string | null;
  sqlExpression: string | null;
  dataSourceId: number | null;
  refreshCron: string | null;
  owner: string | null;
  isCore: number;
  remark: string | null;
  createdAt: string;
  updatedAt: string;
}
```

---

### 12.2 指标详情

**URL**：`GET /api/bigdata/metric/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1101101`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `MetricDefinitionResponse`

同 §12.1。

---

### 12.3 新增指标

**URL**：`POST /api/bigdata/metric`
**认证**：需要 JWT Bearer
**错误码**：`1101102`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `CreateMetricRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `metricCode` | string | ✅ | 指标编码（唯一） |
| `metricName` | string | ✅ | 指标名称 |
| `category` | string | ✅ | 指标分类 code，见 MetricCategory |
| `unit` | string | ❌ | 单位 |
| `calcFormula` | string | ❌ | 计算公式 |
| `sqlExpression` | string | ❌ | SQL 表达式 |
| `dataSourceId` | number | ❌ | 数据源 ID |
| `refreshCron` | string | ❌ | 刷新 Cron |
| `owner` | string | ❌ | 负责人 |
| `isCore` | number | ❌ | 是否核心指标，默认 0 |
| `remark` | string | ❌ | 备注 |

---

### 12.4 更新指标

**URL**：`PUT /api/bigdata/metric/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1101103`
**变更历史**：2026-04-12 | init | 架构师

---

### 12.5 删除指标

**URL**：`DELETE /api/bigdata/metric/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1101104`
**变更历史**：2026-04-12 | init | 架构师

---

### 12.6 指标取值序列

**URL**：`GET /api/bigdata/metric/{code}/values`
**认证**：需要 JWT Bearer
**错误码**：`1101105`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `from` | string | ❌ | 起始日期（ISO 格式 `yyyy-MM-dd`） |
| `to` | string | ❌ | 结束日期（ISO 格式 `yyyy-MM-dd`） |

#### 响应体 `List<MetricValueResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 值 ID |
| `metricCode` | string | ✅ | 指标编码 |
| `statDate` | string | ✅ | 统计日期，格式 `yyyy-MM-dd` |
| `statPeriod` | `EnumValue<string>` | ✅ | 统计周期，见 StatPeriod |
| `metricValue` | number | ✅ | 指标值（精度 2 位） |
| `dimJson` | string \| null | ❌ | 维度信息（JSON） |

```typescript
export interface MetricValueResponse {
  id: number;
  metricCode: string;
  statDate: string;
  statPeriod: EnumValue<string>;
  metricValue: number;
  dimJson: string | null;
}
```

---

### 12.7 记录指标取值

**URL**：`POST /api/bigdata/metric/values`
**认证**：需要 JWT Bearer
**错误码**：`1101106`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `CreateMetricValueRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `metricCode` | string | ✅ | 指标编码 |
| `statDate` | string | ✅ | 统计日期，格式 `yyyy-MM-dd` |
| `statPeriod` | string | ✅ | 统计周期 code，见 StatPeriod |
| `metricValue` | number | ✅ | 指标值 |
| `dimJson` | string | ❌ | 维度信息（JSON） |

#### 响应体 `MetricValueResponse`

---

## 13. 开放 API 客户端 OpenApiController（`/api/bigdata/openapi/clients`）

### 13.1 注册 API 客户端

**URL**：`POST /api/bigdata/openapi/clients`
**认证**：需要 JWT Bearer
**错误码**：`1101200`
**变更历史**：2026-04-12 | init | 架构师

**业务规则**：
- 自动生成 `appKey`：格式 `ak_` + 16 位随机字符
- 自动生成 `appSecretEnc`（加密存储）
- 初始状态 `ACTIVE`

#### 请求体 `RegisterApiClientRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `clientName` | string | ✅ | 客户端名称 |
| `owner` | string | ❌ | 负责人 |
| `rateLimitQps` | number | ❌ | 限流 QPS |
| `dailyQuota` | number | ❌ | 日配额 |
| `validUntil` | string | ❌ | 有效期至，格式 `yyyy-MM-dd` |
| `remark` | string | ❌ | 备注 |

```typescript
export interface RegisterApiClientRequest {
  clientName: string;
  owner?: string;
  rateLimitQps?: number;
  dailyQuota?: number;
  validUntil?: string;
  remark?: string;
}
```

#### 响应体 `ApiClientResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 客户端 ID |
| `clientName` | string | ✅ | 客户端名称 |
| `appKey` | string | ✅ | 应用 Key |
| `appSecretEnc` | string | ✅ | 加密密钥（仅注册时返回明文） |
| `owner` | string \| null | ❌ | 负责人 |
| `rateLimitQps` | number \| null | ❌ | 限流 QPS |
| `dailyQuota` | number \| null | ❌ | 日配额 |
| `status` | `EnumValue<string>` | ✅ | 状态，见 ApiClientStatus |
| `validUntil` | string \| null | ❌ | 有效期至 |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface ApiClientResponse {
  id: number;
  clientName: string;
  appKey: string;
  appSecretEnc: string;
  owner: string | null;
  rateLimitQps: number | null;
  dailyQuota: number | null;
  status: EnumValue<string>;
  validUntil: string | null;
  remark: string | null;
  createdAt: string;
  updatedAt: string;
}
```

---

### 13.2 API 客户端列表

**URL**：`GET /api/bigdata/openapi/clients`
**认证**：需要 JWT Bearer
**错误码**：`1101201`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | number | ❌ | `1` | 页码 |
| `size` | number | ❌ | `10` | 每页条数 |

#### 响应体 `IPage<ApiClientResponse>`

> **注意**：此端点使用 MyBatis-Plus `IPage`（非 `PageResult`），返回字段 `appSecretEnc` 不应为明文。

---

### 13.3 更新客户端状态

**URL**：`PUT /api/bigdata/openapi/clients/{id}/status`
**认证**：需要 JWT Bearer
**错误码**：`1101202` / `1101203`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `status` | string | ✅ | 状态 code：`ACTIVE` / `DISABLED` / `REVOKED` |

#### 响应体

`R<Void>`

---

### 13.4 客户端调用日志

**URL**：`GET /api/bigdata/openapi/clients/{id}/logs`
**认证**：需要 JWT Bearer
**错误码**：`1101204` / `1101205`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | number | ❌ | `1` | 页码 |
| `size` | number | ❌ | `20` | 每页条数 |

#### 响应体 `IPage<ApiCallLogResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 日志 ID |
| `appKey` | string | ✅ | 应用 Key |
| `clientIp` | string \| null | ❌ | 客户端 IP |
| `apiPath` | string | ✅ | 请求路径 |
| `method` | string | ✅ | HTTP 方法 |
| `statusCode` | number | ✅ | 响应状态码 |
| `durationMs` | number \| null | ❌ | 耗时（毫秒） |
| `requestBytes` | number \| null | ❌ | 请求体大小 |
| `responseBytes` | number \| null | ❌ | 响应体大小 |
| `errorMessage` | string \| null | ❌ | 错误信息 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface ApiCallLogResponse {
  id: number;
  appKey: string;
  clientIp: string | null;
  apiPath: string;
  method: string;
  statusCode: number;
  durationMs: number | null;
  requestBytes: number | null;
  responseBytes: number | null;
  errorMessage: string | null;
  createdAt: string;
}
```

---

### 13.5 轮转密钥

**URL**：`POST /api/bigdata/openapi/clients/{id}/rotate-key`
**认证**：需要 JWT Bearer
**错误码**：`1101206` / `1101207`
**变更历史**：2026-04-12 | init | 架构师

**业务规则**：重新生成 `appKey` + `appSecretEnc`，旧密钥立即失效，新密钥仅此一次返回明文。

#### 响应体 `ApiClientResponse`

同 §13.1，含新的 `appKey` 和 `appSecretEnc`。

---

## 14. 运营监控 OpsMonitorController（`/api/bigdata/ops`）

### 14.1 平台健康概览

**URL**：`GET /api/bigdata/ops/health`
**认证**：需要 JWT Bearer
**错误码**：`1101300`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `HealthResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `status` | string | ✅ | 总体状态：`UP` / `DOWN` |
| `uptimeMs` | number | ✅ | 运行时长（毫秒） |
| `javaVersion` | string | ✅ | Java 版本 |
| `components` | object | ✅ | 各组件状态 |

```typescript
export interface HealthResponse {
  status: string;
  uptimeMs: number;
  javaVersion: string;
  components: {
    mysql: string;
    redis: string;
    kafka: string;
    canal: string;
    clickhouse: string;
    dolphinscheduler: string;
  };
}
```

> **M5 TODO**：`kafka` / `canal` / `clickhouse` / `dolphinscheduler` 当前返回 `UNKNOWN`，M5 上线后接入真实探测。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
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

### 14.2 JVM 指标

**URL**：`GET /api/bigdata/ops/metrics`
**认证**：需要 JWT Bearer
**错误码**：`1101301`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `JvmMetricsResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `heapUsed` | number | ✅ | 堆已用（字节） |
| `heapMax` | number | ✅ | 堆最大（字节） |
| `nonHeapUsed` | number | ✅ | 非堆已用（字节） |
| `availableProcessors` | number | ✅ | 可用处理器数 |
| `threadCount` | number | ✅ | 线程数 |

```typescript
export interface JvmMetricsResponse {
  heapUsed: number;
  heapMax: number;
  nonHeapUsed: number;
  availableProcessors: number;
  threadCount: number;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
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

## 15. 审计日志 AuditLogQueryController（`/api/bigdata/audit`）

### 15.1 审计日志查询

**URL**：`GET /api/bigdata/audit`
**认证**：需要 JWT Bearer
**错误码**：`1101400`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `username` | string | ❌ | — | 用户名筛选 |
| `module` | string | ❌ | — | 模块筛选 |
| `action` | string | ❌ | — | 操作筛选 |
| `page` | number | ❌ | `1` | 页码 |
| `size` | number | ❌ | `20` | 每页条数 |

#### 响应体 `PageResult<AuditLogResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 日志 ID |
| `userId` | number | ✅ | 用户 ID |
| `username` | string | ✅ | 用户名 |
| `roleCode` | string \| null | ❌ | 角色编码 |
| `module` | string | ✅ | 模块 |
| `action` | string | ✅ | 操作 |
| `targetType` | string \| null | ❌ | 目标类型 |
| `targetId` | string \| null | ❌ | 目标 ID |
| `requestIp` | string \| null | ❌ | 请求 IP |
| `requestUri` | string \| null | ❌ | 请求 URI |
| `requestMethod` | string \| null | ❌ | HTTP 方法 |
| `requestParams` | string \| null | ❌ | 请求参数 |
| `responseStatus` | number \| null | ❌ | 响应状态码 |
| `durationMs` | number \| null | ❌ | 耗时（毫秒） |
| `errorMessage` | string \| null | ❌ | 错误信息 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface AuditLogResponse {
  id: number;
  userId: number;
  username: string;
  roleCode: string | null;
  module: string;
  action: string;
  targetType: string | null;
  targetId: string | null;
  requestIp: string | null;
  requestUri: string | null;
  requestMethod: string | null;
  requestParams: string | null;
  responseStatus: number | null;
  durationMs: number | null;
  errorMessage: string | null;
  createdAt: string;
}
```

---

## 16. 大屏配置 DashboardConfigController（`/api/bigdata/screen`）

### 16.1 大屏列表（分页）

**URL**：`GET /api/bigdata/screen`
**认证**：需要 JWT Bearer
**错误码**：`1101500`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | number | ❌ | `1` | 页码 |
| `size` | number | ❌ | `20` | 每页条数 |
| `category` | string | ❌ | — | 大屏分类，见 DashboardCategory |

#### 响应体 `PageResult<DashboardConfigResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 大屏 ID |
| `dashboardCode` | string | ✅ | 大屏编码 |
| `dashboardName` | string | ✅ | 大屏名称 |
| `category` | `EnumValue<string>` | ✅ | 分类，见 DashboardCategory |
| `layoutJson` | string \| null | ❌ | 布局 JSON |
| `theme` | string \| null | ❌ | 主题 |
| `visibility` | `EnumValue<string>` | ✅ | 可见性，见 DashboardVisibility |
| `visibleRoles` | string \| null | ❌ | 可见角色（逗号分隔） |
| `published` | number | ✅ | 是否已发布（1=是，0=否） |
| `owner` | string \| null | ❌ | 负责人 |
| `refreshSeconds` | number \| null | ❌ | 自动刷新间隔（秒） |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface DashboardConfigResponse {
  id: number;
  dashboardCode: string;
  dashboardName: string;
  category: EnumValue<string>;
  layoutJson: string | null;
  theme: string | null;
  visibility: EnumValue<string>;
  visibleRoles: string | null;
  published: number;
  owner: string | null;
  refreshSeconds: number | null;
  remark: string | null;
  createdAt: string;
  updatedAt: string;
}
```

---

### 16.2 大屏详情

**URL**：`GET /api/bigdata/screen/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1101501`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `DashboardConfigResponse`

同 §16.1。

---

### 16.3 新增大屏

**URL**：`POST /api/bigdata/screen`
**认证**：需要 JWT Bearer
**错误码**：`1101502`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `CreateDashboardConfigRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `dashboardCode` | string | ✅ | 大屏编码（唯一） |
| `dashboardName` | string | ✅ | 大屏名称 |
| `category` | string | ✅ | 分类 code，见 DashboardCategory |
| `layoutJson` | string | ❌ | 布局 JSON |
| `theme` | string | ❌ | 主题 |
| `visibility` | string | ❌ | 可见性 code，见 DashboardVisibility |
| `visibleRoles` | string | ❌ | 可见角色 |
| `owner` | string | ❌ | 负责人 |
| `refreshSeconds` | number | ❌ | 自动刷新间隔（秒） |
| `remark` | string | ❌ | 备注 |

---

### 16.4 更新大屏

**URL**：`PUT /api/bigdata/screen/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1101503`
**变更历史**：2026-04-12 | init | 架构师

---

### 16.5 删除大屏

**URL**：`DELETE /api/bigdata/screen/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1101504`
**变更历史**：2026-04-12 | init | 架构师

---

### 16.6 发布大屏

**URL**：`POST /api/bigdata/screen/{id}/publish`
**认证**：需要 JWT Bearer
**错误码**：`1101505`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体

`R<Boolean>` — `data: true`。

---

### 16.7 列出 Widget

**URL**：`GET /api/bigdata/screen/{id}/widgets`
**认证**：需要 JWT Bearer
**错误码**：`1101506`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `List<DashboardWidgetResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | Widget ID |
| `dashboardId` | number | ✅ | 所属大屏 ID |
| `widgetCode` | string | ✅ | Widget 编码 |
| `widgetName` | string | ✅ | Widget 名称 |
| `widgetType` | `EnumValue<string>` | ✅ | Widget 类型，见 WidgetType |
| `dataSource` | `EnumValue<string>` | ✅ | 数据源类型，见 WidgetDataSource |
| `dataRef` | string \| null | ❌ | 数据引用（指标编码/SQL/API 路径） |
| `positionJson` | string \| null | ❌ | 位置 JSON |
| `optionJson` | string \| null | ❌ | 图表配置 JSON |
| `ordinal` | number | ✅ | 排列顺序 |

```typescript
export interface DashboardWidgetResponse {
  id: number;
  dashboardId: number;
  widgetCode: string;
  widgetName: string;
  widgetType: EnumValue<string>;
  dataSource: EnumValue<string>;
  dataRef: string | null;
  positionJson: string | null;
  optionJson: string | null;
  ordinal: number;
}
```

---

### 16.8 新增 Widget

**URL**：`POST /api/bigdata/screen/{id}/widgets`
**认证**：需要 JWT Bearer
**错误码**：`1101507`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `CreateDashboardWidgetRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `widgetCode` | string | ✅ | Widget 编码 |
| `widgetName` | string | ✅ | Widget 名称 |
| `widgetType` | string | ✅ | 类型 code，见 WidgetType |
| `dataSource` | string | ✅ | 数据源类型 code，见 WidgetDataSource |
| `dataRef` | string | ❌ | 数据引用 |
| `positionJson` | string | ❌ | 位置 JSON |
| `optionJson` | string | ❌ | 图表配置 JSON |
| `ordinal` | number | ❌ | 排列顺序 |

---

### 16.9 删除 Widget

**URL**：`DELETE /api/bigdata/screen/widgets/{widgetId}`
**认证**：需要 JWT Bearer
**错误码**：`1101508`
**变更历史**：2026-04-12 | init | 架构师

---

## 17. 权限矩阵 RolePermissionController（`/api/bigdata/role`）

> 大数据模块独立的 RBAC 权限矩阵，与 apple-module-user 的用户角色体系**独立并存**。

### 17.1 列出所有角色

**URL**：`GET /api/bigdata/role`
**认证**：需要 JWT Bearer
**错误码**：`1101600`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `List<RoleResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 角色 ID |
| `roleCode` | string | ✅ | 角色编码 |
| `roleName` | string | ✅ | 角色名称 |
| `description` | string \| null | ❌ | 描述 |
| `sortOrder` | number | ✅ | 排序 |
| `enabled` | number | ✅ | 是否启用（1=是，0=否） |

```typescript
export interface RoleResponse {
  id: number;
  roleCode: string;
  roleName: string;
  description: string | null;
  sortOrder: number;
  enabled: number;
}
```

---

### 17.2 新增角色

**URL**：`POST /api/bigdata/role`
**认证**：需要 JWT Bearer
**错误码**：`1101601`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `CreateRoleRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `roleCode` | string | ✅ | 角色编码（唯一） |
| `roleName` | string | ✅ | 角色名称 |
| `description` | string | ❌ | 描述 |
| `sortOrder` | number | ❌ | 排序 |
| `enabled` | number | ❌ | 是否启用，默认 1 |

---

### 17.3 更新角色

**URL**：`PUT /api/bigdata/role/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1101602`
**变更历史**：2026-04-12 | init | 架构师

---

### 17.4 删除角色

**URL**：`DELETE /api/bigdata/role/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1101603`
**变更历史**：2026-04-12 | init | 架构师

---

### 17.5 列出所有权限

**URL**：`GET /api/bigdata/role/permissions`
**认证**：需要 JWT Bearer
**错误码**：`1101604`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `List<PermissionResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 权限 ID |
| `permCode` | string | ✅ | 权限编码 |
| `permName` | string | ✅ | 权限名称 |
| `module` | string | ✅ | 所属模块 |
| `permType` | `EnumValue<string>` | ✅ | 权限类型，见 PermissionType |
| `resourcePattern` | string \| null | ❌ | 资源匹配模式 |
| `parentCode` | string \| null | ❌ | 父权限编码 |
| `sortOrder` | number | ✅ | 排序 |

```typescript
export interface PermissionResponse {
  id: number;
  permCode: string;
  permName: string;
  module: string;
  permType: EnumValue<string>;
  resourcePattern: string | null;
  parentCode: string | null;
  sortOrder: number;
}
```

---

### 17.6 新增权限

**URL**：`POST /api/bigdata/role/permissions`
**认证**：需要 JWT Bearer
**错误码**：`1101605`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 `CreatePermissionRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `permCode` | string | ✅ | 权限编码（唯一） |
| `permName` | string | ✅ | 权限名称 |
| `module` | string | ✅ | 所属模块 |
| `permType` | string | ✅ | 权限类型 code，见 PermissionType |
| `resourcePattern` | string | ❌ | 资源匹配模式 |
| `parentCode` | string | ❌ | 父权限编码 |
| `sortOrder` | number | ❌ | 排序 |

---

### 17.7 删除权限

**URL**：`DELETE /api/bigdata/role/permissions/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1101606`
**变更历史**：2026-04-12 | init | 架构师

---

### 17.8 查询角色已绑定权限

**URL**：`GET /api/bigdata/role/{roleCode}/permissions`
**认证**：需要 JWT Bearer
**错误码**：`1101607`
**变更历史**：2026-04-12 | init | 架构师

#### 响应体 `List<RolePermissionResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 映射 ID |
| `roleCode` | string | ✅ | 角色编码 |
| `permCode` | string | ✅ | 权限编码 |

```typescript
export interface RolePermissionResponse {
  id: number;
  roleCode: string;
  permCode: string;
}
```

---

### 17.9 覆盖式分配角色权限

**URL**：`POST /api/bigdata/role/{roleCode}/permissions`
**认证**：需要 JWT Bearer
**错误码**：`1101608`
**变更历史**：2026-04-12 | init | 架构师

**业务规则**：覆盖式分配，传入的 `permCode` 列表将替换该角色所有现有权限。

#### 请求体

`List<string>` — 权限编码列表，如 `["bigdata:asset:read", "bigdata:source:write"]`

#### 响应体

`R<Integer>` — `data` 为分配的权限数量。

---

## 18. 枚举定义

### ReportType（分析报告类型）

| code | desc |
|------|------|
| `WEEKLY` | 周报 |
| `MONTHLY` | 月报 |
| `SEASONAL` | 季报 |

### ReportStatus（分析报告状态）

```
DRAFT ──→ PUBLISHED
```

| code | desc |
|------|------|
| `DRAFT` | 草稿 |
| `PUBLISHED` | 已发布 |

### SecurityLevel（数据安全等级）

| code | desc |
|------|------|
| `PUBLIC` | 公开 |
| `INTERNAL` | 内部 |
| `SENSITIVE` | 敏感 |
| `SECRET` | 机密 |

### SensitiveType（字段敏感类型）

| code | desc |
|------|------|
| `PHONE` | 手机号 |
| `ID_CARD` | 身份证号 |
| `GPS` | 地理位置 |
| `NAME` | 姓名 |
| `EMAIL` | 邮箱 |

### DataSourceType（数据源类型）

| code | desc |
|------|------|
| `MYSQL` | MySQL 数据库 |
| `CLICKHOUSE` | ClickHouse |
| `KAFKA` | Kafka 消息队列 |
| `HTTP_API` | HTTP API |
| `RSS` | RSS 订阅 |
| `FILE` | 文件 |

### DataSourceCategory（数据源分类）

| code | desc |
|------|------|
| `INTERNAL` | 内部模块（8 个：planting, trade, warehouse, finance, trace, iot, coldchain, input） |
| `EXTERNAL` | 外部 API（4 个：天气/市场/政策/消费） |

### DataSourceStatus（数据源状态）

| code | desc |
|------|------|
| `ACTIVE` | 活跃 |
| `DISABLED` | 已禁用 |
| `ERROR` | 错误 |

### DqRuleType（数据质量规则类型）

| code | desc |
|------|------|
| `NOT_NULL` | 非空检查 |
| `UNIQUE` | 唯一性检查 |
| `RANGE` | 范围检查 |
| `REGEX` | 正则匹配 |
| `ENUM` | 枚举检查 |
| `FRESHNESS` | 时效性检查 |

### DqSeverity（数据质量严重级别）

| code | desc |
|------|------|
| `BLOCK` | 阻断 |
| `ERROR` | 错误 |
| `WARN` | 警告 |
| `INFO` | 信息 |

### JobType（采集任务类型）

| code | desc |
|------|------|
| `CDC` | 变更数据捕获 |
| `SCHEDULED` | 定时任务 |
| `MANUAL` | 手动任务 |
| `STREAMING` | 实时流 |

### JobRunStatus（任务运行状态）

| code | desc |
|------|------|
| `RUNNING` | 运行中 |
| `SUCCESS` | 成功 |
| `FAILED` | 失败 |
| `TIMEOUT` | 超时 |

### RunStatus（DQ 检查运行状态）

```
PENDING ──→ RUNNING ──→ PASSED
                      ──→ FAILED
```

| code | desc |
|------|------|
| `PENDING` | 待执行 |
| `RUNNING` | 执行中 |
| `PASSED` | 通过 |
| `FAILED` | 未通过 |

### TriggerType（触发类型）

| code | desc |
|------|------|
| `SCHEDULED` | 定时触发 |
| `MANUAL` | 手动触发 |
| `RETRY` | 重试 |

### LineageNodeType（血缘节点类型）

| code | desc |
|------|------|
| `TABLE` | 表 |
| `FIELD` | 字段 |
| `JOB` | 采集任务 |
| `METRIC` | 指标 |
| `DASHBOARD` | 大屏 |
| `SYSTEM` | 系统 |

### LineageRelationType（血缘关系类型）

| code | desc |
|------|------|
| `DERIVES_FROM` | 派生自 |
| `TRANSFORMS` | 转换 |
| `AGGREGATES` | 聚合 |
| `REFERENCES` | 引用 |

### MetricCategory（指标分类）

| code | desc |
|------|------|
| `PLANTING` | 种植 |
| `TRADE` | 交易 |
| `WAREHOUSE` | 仓储 |
| `LOGISTICS` | 物流 |
| `FINANCE` | 金融 |
| `TRACE` | 溯源 |

### StatPeriod（统计周期）

| code | desc |
|------|------|
| `DAY` | 日 |
| `WEEK` | 周 |
| `MONTH` | 月 |
| `YEAR` | 年 |

### ApiClientStatus（API 客户端状态）

```
ACTIVE ──→ DISABLED ──→ REVOKED
```

| code | desc |
|------|------|
| `ACTIVE` | 活跃 |
| `DISABLED` | 已禁用 |
| `REVOKED` | 已吊销 |

### DashboardCategory（大屏分类）

| code | desc |
|------|------|
| `GOV` | 政务大屏 |
| `OPS` | 运营大屏 |
| `INDUSTRY` | 产业大屏 |
| `CUSTOM` | 自定义 |

### DashboardVisibility（大屏可见性）

| code | desc |
|------|------|
| `PUBLIC` | 公开 |
| `PRIVATE` | 私有 |
| `ROLE` | 角色可见 |

### WidgetType（Widget 类型）

| code | desc |
|------|------|
| `KPI` | KPI 数字卡 |
| `LINE` | 折线图 |
| `BAR` | 柱状图 |
| `PIE` | 饼图 |
| `TABLE` | 表格 |
| `MAP` | 地图 |
| `TRACE` | 溯源链 |
| `TEXT` | 文本 |

### WidgetDataSource（Widget 数据源类型）

| code | desc |
|------|------|
| `METRIC` | 指标中心 |
| `SQL` | 自定义 SQL |
| `API` | API 接口 |

### PermissionType（权限类型）

| code | desc |
|------|------|
| `MENU` | 菜单 |
| `BUTTON` | 按钮 |
| `API` | API |
| `DATA` | 数据 |

---

## 19. 错误码表

| 错误码 | HTTP 状态码 | 说明 |
|--------|-----------|------|
| `1100100` | 500 | KPI 统计查询失败 |
| `1100200` | 500 | 仓储总量统计查询失败 |
| `1100201` | 500 | 金融规模统计查询失败 |
| `1100202` | 500 | 品种价格趋势查询失败 |
| `1100203` | 500 | 损耗率统计查询失败 |
| `1100300` | 500 | MVP 看板查询失败 |
| `1100400` | 500 | 全量统计查询失败 |
| `1100500` | 500 | 生成分析报告失败 |
| `1100501` | 400 | 报告类型/周期参数无效 |
| `1100502` | 500 | 报告列表查询失败 |
| `1100503` | 404 | 报告不存在 |
| `1100504` | 409 | 仅草稿状态可以发布 |
| `1100600` | 500 | 资产列表查询失败 |
| `1100601` | 404 | 资产不存在 |
| `1100602` | 400 | 资产创建失败 |
| `1100603` | 404 | 资产不存在（更新时） |
| `1100604` | 404 | 资产不存在（删除时） |
| `1100605` | 500 | 资产字段列表查询失败 |
| `1100606` | 400 | 资产字段创建失败 |
| `1100607` | 404 | 资产字段不存在（删除时） |
| `1100700` | 500 | 数据源列表查询失败 |
| `1100701` | 404 | 数据源不存在 |
| `1100702` | 400 | 数据源创建失败 |
| `1100703` | 404 | 数据源不存在（更新时） |
| `1100704` | 404 | 数据源不存在（删除时） |
| `1100705` | 500 | 连通性测试失败 |
| `1100800` | 500 | DQ 规则列表查询失败 |
| `1100801` | 404 | DQ 规则不存在 |
| `1100802` | 400 | DQ 规则创建失败 |
| `1100803` | 404 | DQ 规则不存在（更新时） |
| `1100804` | 404 | DQ 规则不存在（删除时） |
| `1100805` | 500 | DQ 检查执行失败 |
| `1100806` | 500 | DQ 检查结果查询失败 |
| `1100900` | 500 | 采集任务列表查询失败 |
| `1100901` | 404 | 采集任务不存在 |
| `1100902` | 400 | 采集任务创建失败 |
| `1100903` | 404 | 采集任务不存在（更新时） |
| `1100904` | 404 | 采集任务不存在（删除时） |
| `1100905` | 500 | 手动触发任务失败 |
| `1100906` | 500 | 运行历史查询失败 |
| `1100907` | 401 | DS 回调 Token 无效 |
| `1100908` | 404 | DS 回调任务不存在 |
| `1101000` | 500 | 血缘边列表查询失败 |
| `1101001` | 400 | 血缘边创建失败 |
| `1101002` | 404 | 血缘边不存在（删除时） |
| `1101003` | 500 | 下游依赖查询失败 |
| `1101004` | 500 | 上游血缘查询失败 |
| `1101100` | 500 | 指标列表查询失败 |
| `1101101` | 404 | 指标不存在 |
| `1101102` | 400 | 指标创建失败 |
| `1101103` | 404 | 指标不存在（更新时） |
| `1101104` | 404 | 指标不存在（删除时） |
| `1101105` | 500 | 指标取值序列查询失败 |
| `1101106` | 400 | 指标取值记录失败 |
| `1101200` | 400 | API 客户端注册失败 |
| `1101201` | 500 | API 客户端列表查询失败 |
| `1101202` | 404 | API 客户端不存在 |
| `1101203` | 400 | 无效的客户端状态值 |
| `1101204` | 404 | API 客户端不存在（日志查询时） |
| `1101205` | 500 | API 调用日志查询失败 |
| `1101206` | 404 | API 客户端不存在（轮转密钥时） |
| `1101207` | 500 | 密钥轮转失败 |
| `1101300` | 500 | 平台健康检查失败 |
| `1101301` | 500 | JVM 指标获取失败 |
| `1101400` | 500 | 审计日志查询失败 |
| `1101500` | 500 | 大屏列表查询失败 |
| `1101501` | 404 | 大屏不存在 |
| `1101502` | 400 | 大屏创建失败 |
| `1101503` | 404 | 大屏不存在（更新时） |
| `1101504` | 404 | 大屏不存在（删除时） |
| `1101505` | 500 | 大屏发布失败 |
| `1101506` | 500 | Widget 列表查询失败 |
| `1101507` | 400 | Widget 创建失败 |
| `1101508` | 404 | Widget 不存在（删除时） |
| `1101600` | 500 | 角色列表查询失败 |
| `1101601` | 400 | 角色创建失败 |
| `1101602` | 404 | 角色不存在（更新时） |
| `1101603` | 404 | 角色不存在（删除时） |
| `1101604` | 500 | 权限列表查询失败 |
| `1101605` | 400 | 权限创建失败 |
| `1101606` | 404 | 权限不存在（删除时） |
| `1101607` | 500 | 角色权限查询失败 |
| `1101608` | 500 | 角色权限分配失败 |

---

## 20. 实现注记

### 10.1 端点分类总览

| 分类 | 端点数 | Controller | 数据源 | 协议 |
|------|--------|-----------|--------|------|
| 数据大屏 | 7 | DashboardController | MySQL 聚合 | REST GET |
| 大屏补充统计 | 4 | DashboardSupplementController | MySQL 跨模块（JdbcTemplate） | REST GET |
| MVP 看板 | 4 | DashboardMvpController | MySQL 聚合 | REST GET |
| 全量统计 | 4 | StatisticsController | MySQL 聚合 | REST GET |
| 分析报告 | 4 | AnalysisReportController | MySQL CRUD | REST |
| 数据资产 | 8 | DataAssetController | MySQL CRUD | REST |
| 数据源 | 6 | DataSourceController | MySQL CRUD + 运行时 | REST |
| 数据质量 | 7 | DqRuleController | MySQL CRUD + 运行时 | REST |
| 采集任务 | 8 | CollectJobController + CallbackController | MySQL CRUD + DS 回调 | REST + Token |
| 数据血缘 | 5 | DataLineageController | MySQL CRUD | REST |
| 指标中心 | 7 | MetricController | MySQL CRUD | REST |
| 开放 API | 5 | OpenApiController | MySQL CRUD + 运行时 | REST |
| 运营监控 | 2 | OpsMonitorController | JVM 运行时 | REST GET |
| 审计日志 | 1 | AuditLogQueryController | MySQL CRUD | REST GET |
| 大屏配置 | 9 | DashboardConfigController | MySQL CRUD | REST |
| 权限矩阵 | 9 | RolePermissionController | MySQL CRUD | REST |
| **合计** | **90** | 17 Controller | | |

> **注意**：API 文档标注 64 个未用 API，实际 Controller 源码共 90 个端点（含 DashboardMvpController 4 个、DashboardConfigController 9 个、RolePermissionController 9 个、CollectJobCallbackController 1 个、AuditLogQueryController 1 个等补充端点）。

### 10.2 实时推送说明

当前版本（M2）**无 WebSocket 端点**。所有大屏数据通过 HTTP GET 轮询获取。前端可通过 `DashboardConfig.refreshSeconds` 字段配置轮询间隔。

**M5 规划**：接入 ClickHouse 实时聚合 + Kafka 消费，大屏数据将支持 WebSocket 推送。

### 10.3 DS 回调认证

`POST /api/bigdata/job/run/callback` 使用 `X-DS-Token` Header 认证（非 JWT），Token 值由配置项 `ds.callback.token` 控制。

### 10.4 String → EnumValue 迁移

当前后端 Entity 中以下字段为 `String` 类型，需迁移为 `EnumValue` 序列化：

| Entity | 字段 | 当前类型 | 目标类型 |
|--------|------|---------|---------|
| BdAnalysisReport | `reportType` | String | `EnumValue<ReportType>` |
| BdAnalysisReport | `status` | String | `EnumValue<ReportStatus>` |
| BdDataAsset | `securityLevel` | String | `EnumValue<SecurityLevel>` |
| BdDataSource | `sourceType` | String | `EnumValue<DataSourceType>` |
| BdDataSource | `category` | String | `EnumValue<DataSourceCategory>` |
| BdDataSource | `status` | String | `EnumValue<DataSourceStatus>` |
| BdDqRule | `ruleType` | String | `EnumValue<DqRuleType>` |
| BdDqRule | `severity` | String | `EnumValue<DqSeverity>` |
| BdCollectJob | `jobType` | String | `EnumValue<JobType>` |
| BdCollectJob | `lastRunStatus` | String | `EnumValue<JobRunStatus>` |
| BdCollectJobRun | `runStatus` | String | `EnumValue<RunStatus>` |
| BdCollectJobRun | `triggerType` | String | `EnumValue<TriggerType>` |
| BdDataLineage | `upstreamType` | String | `EnumValue<LineageNodeType>` |
| BdDataLineage | `downstreamType` | String | `EnumValue<LineageNodeType>` |
| BdDataLineage | `relationType` | String | `EnumValue<LineageRelationType>` |
| BdMetricDefinition | `category` | String | `EnumValue<MetricCategory>` |
| BdMetricValue | `statPeriod` | String | `EnumValue<StatPeriod>` |
| BdApiClient | `status` | String | `EnumValue<ApiClientStatus>` |
| BdDashboardConfig | `category` | String | `EnumValue<DashboardCategory>` |
| BdDashboardConfig | `visibility` | String | `EnumValue<DashboardVisibility>` |
| BdDashboardWidget | `widgetType` | String | `EnumValue<WidgetType>` |
| BdDashboardWidget | `dataSource` | String | `EnumValue<WidgetDataSource>` |
| BdPermission | `permType` | String | `EnumValue<PermissionType>` |
| BdDataAssetField | `sensitiveType` | String | `EnumValue<SensitiveType>` |

共 24 个 String 字段需迁移。

### 10.5 Entity → Request/Response DTO 重命名

当前 17 个 Controller 全部直接返回 Entity 对象（如 `BdDataAsset`、`BdCollectJob`），不符合 CONVENTIONS.md DTO 命名约定。需创建对应的 `XxxRequest` / `XxxResponse` DTO。

### 10.6 分页参数迁移 PageParam

当前各 Controller 直接使用 `@RequestParam int page, int size, ...`，目标迁移为全局 `PageParam` 对象。

### 10.7 IPage vs PageResult 不一致

`AnalysisReportController` 和 `OpenApiController` 使用 MyBatis-Plus `IPage` 返回分页，其余 Controller 使用 `PageResult<T>`。需统一为 `PageResult<T>`。

### 10.8 敏感字段保护

以下字段**禁止**返回给客户端：

| Entity | 字段 | 说明 |
|--------|------|------|
| BdDataSource | `passwordEnc` | AES 加密密码 |
| BdApiClient | `appSecretEnc` | AES 加密密钥（仅注册/轮转时返回） |
| BdDataSource | `extraConfig` | 可能含连接密钥 |

### 10.9 跨模块依赖

大数据模块作为聚合层，跨模块读取以下表：

| 读取目标 | 表 | 使用场景 |
|---------|-----|---------|
| planting | `pt_orchard` | 果园总数、活跃果园数 |
| trade | `td_trade_order` | 交易额、趋势、均价 |
| warehouse | `wh_warehouse` / `wh_warehouse_record` | 仓储容量、损耗率 |
| finance | `fn_loan` | 金融规模 |
| trace | `tc_trace_chain` | 溯源链状态 |
| user | `sys_user` | 用户总数、农户数 |

---

## 质量 Checklist

- [x] 90 个端点全覆盖（17 Controller，含 DashboardMvp + DashboardConfig + RolePermission + DS Callback 等补充端点）
- [x] 78 个错误码（>=20），全在 1100000-1199999 范围
- [x] 23 个枚举声明完整（ReportType / ReportStatus / SecurityLevel / SensitiveType / DataSourceType / DataSourceCategory / DataSourceStatus / DqRuleType / DqSeverity / JobType / JobRunStatus / RunStatus / TriggerType / LineageNodeType / LineageRelationType / MetricCategory / StatPeriod / ApiClientStatus / DashboardCategory / DashboardVisibility / WidgetType / WidgetDataSource / PermissionType）
- [x] JSON 示例全用 `message` 字段（禁用 `msg`）
- [x] 分页使用 PageParam 语义 + PageResult\<T\> 结构（已标注 IPage 不一致）
- [x] 每个端点标注数据源类型（MySQL 聚合 / MySQL CRUD / 运行时探测 / DS 回调）
- [x] 明确标注无 WebSocket 实时推送端点（M2 版本）
- [x] 大屏/Widget 相关端点完整描述（DashboardConfigController + DashboardSupplementController）
- [x] DS 回调端点认证机制说明（X-DS-Token 非 JWT）
- [x] 敏感字段保护清单（passwordEnc / appSecretEnc / extraConfig）
- [x] 24 个 String→EnumValue 迁移清单（§10.4）
- [x] Entity→Request/Response DTO 重命名（§10.5）
- [x] IPage vs PageResult 不一致（§10.7）
- [x] 跨模块依赖（6 个模块 6 张表）（§10.9）
