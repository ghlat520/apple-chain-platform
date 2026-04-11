# M1 种植管理模块 API 文档

> 模块路径：`apple-module-planting`
> 基础路径：`/api/planting`（部分为 `/api/farm`、`/api/cultivation`）
> 生成日期：2026-04-11

---

## 目录

1. [果园管理 OrchardController](#1-果园管理-orchardcontroller)
2. [果园 GIS 地图 OrchardGeoController](#2-果园-gis-地图-orchardgeocontroller)
3. [农户管理 FarmerController](#3-农户管理-farmercontroller)
4. [农事记录 GrowthRecordController](#4-农事记录-growthrecordcontroller)
5. [采收批次 HarvestBatchController](#5-采收批次-harvestbatchcontroller)
6. [种植批次 CultivationBatchController](#6-种植批次-cultivationbatchcontroller)
7. [农事操作 CultivationOperationController](#7-农事操作-cultivationoperationcontroller)
8. [成熟度与采收推荐 MaturityController](#8-成熟度与采收推荐-maturitycontroller)
9. [作业计划 TaskPlanController](#9-作业计划-taskplancontroller)
10. [种植分析 PlantingAnalysisController](#10-种植分析-plantinganalysiscontroller)
11. [实体字段说明](#11-实体字段说明)

---

## 1. 果园管理 OrchardController

**Base URL**：`/api/planting/orchard`

---

### 1.1 果园列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/planting/orchard/list` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | — | 果园名称/编号模糊搜索 |
| status | String | 否 | — | 状态筛选：`NORMAL` / `DORMANT` / `HARVESTED` |
| farmerId | Long | 否 | — | 按农户ID筛选 |

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 123456789,
        "orchardNo": "ORD202604110001",
        "orchardName": "红富士1号园",
        "farmerId": 100001,
        "area": 15.5,
        "variety": "红富士",
        "location": "陕西省洛川县",
        "longitude": 109.43,
        "latitude": 35.76,
        "treeAge": 8,
        "status": "NORMAL",
        "remark": null
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1
  }
}
```

---

### 1.2 果园详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/planting/orchard/{id}` |

**Path 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 果园ID |

---

### 1.3 创建果园

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/planting/orchard` |

**Request Body**

```json
{
  "orchardName": "红富士1号园",
  "farmerId": 100001,
  "area": 15.5,
  "variety": "红富士",
  "location": "陕西省洛川县",
  "longitude": 109.43,
  "latitude": 35.76,
  "treeAge": 8,
  "status": "NORMAL",
  "remark": ""
}
```

**业务规则**

- 自动生成 `orchardNo`：格式 `ORD` + `yyyyMMdd` + 4位序列号
- `status` 默认 `NORMAL`

---

### 1.4 更新果园

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/planting/orchard/{id}` |

---

### 1.5 删除果园（软删除）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/planting/orchard/{id}` |

---

### 1.6 导出果园 CSV

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/planting/orchard/export` |

**Query 参数**：同列表接口的筛选参数。返回 CSV 文件（UTF-8 BOM）。

---

## 2. 果园 GIS 地图 OrchardGeoController

**Base URL**：`/api/planting/orchard/geo`

---

### 2.1 视窗（bbox）果园查询

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/planting/orchard/geo/bbox` |
| 权限 | `orchard:read` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| lng1 | double | 是 | 矩形左下角经度 (-180~180) |
| lat1 | double | 是 | 矩形左下角纬度 (-90~90) |
| lng2 | double | 是 | 矩形右上角经度 (-180~180) |
| lat2 | double | 是 | 矩形右上角纬度 (-90~90) |

**业务规则**

- 返回中心点落在矩形范围内的果园列表
- 不含 `boundaryGeojson` 大字段，优化传输

---

### 2.2 保存果园边界

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/planting/orchard/geo/{id}/boundary` |
| 权限 | `orchard:write` |

**Request Body**

```json
{
  "boundaryGeojson": "{\"type\":\"Polygon\",\"coordinates\":[[[109.4,35.7],[109.5,35.7],[109.5,35.8],[109.4,35.8],[109.4,35.7]]]}"
}
```

**业务规则**

- 传 GeoJSON Polygon，服务端自动计算质心（centerLat/centerLng）和面积（areaMu）
- 质心字段用于 bbox 快速查询

---

## 3. 农户管理 FarmerController

**Base URL**：`/api/farm/farmers`

---

### 3.1 农户列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/farm/farmers` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | — | 姓名/手机号模糊搜索 |
| status | String | 否 | — | 状态筛选 |

---

### 3.2 农户详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/farm/farmers/{id}` |

---

### 3.3 创建农户

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/farm/farmers` |

**Request Body**：Farmer 实体 JSON（`@Valid` 校验）

---

### 3.4 更新农户

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/farm/farmers/{id}` |

---

### 3.5 删除农户（软删除）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/farm/farmers/{id}` |

---

### 3.6 导出农户 CSV

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/farm/farmers/export` |

---

## 4. 农事记录 GrowthRecordController

**Base URL**：`/api/planting/record`

---

### 4.1 农事记录列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/planting/record/list` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| orchardId | Long | 否 | — | 按果园筛选 |
| recordType | String | 否 | — | 记录类型筛选 |

**业务规则**

- 按 `operateDate` 降序排列

---

### 4.2 农事记录详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/planting/record/{id}` |

---

### 4.3 创建农事记录

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/planting/record` |

---

### 4.4 更新农事记录

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/planting/record/{id}` |

---

### 4.5 删除农事记录（软删除）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/planting/record/{id}` |

---

## 5. 采收批次 HarvestBatchController

**Base URL**：`/api/planting/harvest`

---

### 5.1 采收批次列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/planting/harvest/list` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| orchardId | Long | 否 | — | 按果园筛选 |
| status | String | 否 | — | 状态筛选 |

---

### 5.2 采收批次详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/planting/harvest/{id}` |

---

### 5.3 创建采收批次

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/planting/harvest` |

---

### 5.4 更新采收批次

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/planting/harvest/{id}` |

---

### 5.5 确认采收批次（生成溯源码）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/planting/harvest/{id}/confirm` |

**业务规则**

- 确认后自动生成溯源码
- 状态流转到已确认

---

### 5.6 删除采收批次（软删除）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/planting/harvest/{id}` |

---

### 5.7 导出采收批次 CSV

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/planting/harvest/export` |

---

## 6. 种植批次 CultivationBatchController

**Base URL**：`/api/cultivation/batches`

---

### 6.1 种植批次列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/cultivation/batches` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | — | 关键词搜索 |
| status | String | 否 | — | 状态筛选 |
| orchardId | Long | 否 | — | 按果园筛选 |

---

### 6.2 批次详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/cultivation/batches/{id}` |

---

### 6.3 创建种植批次

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/cultivation/batches` |

**业务规则**

- 自动生成 `batchCode`
- `@Valid` 校验

---

### 6.4 更新种植批次

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/cultivation/batches/{id}` |

---

### 6.5 删除种植批次（软删除）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/cultivation/batches/{id}` |

---

### 6.6 导出种植批次 CSV

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/cultivation/batches/export` |

---

## 7. 农事操作 CultivationOperationController

**Base URL**：`/api/cultivation/operations`

记录具体农事操作：施肥(fertilize)、打药(pesticide)、修剪(prune)、采收(harvest)、灌溉(irrigate)。

---

### 7.1 操作记录列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/cultivation/operations` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| batchId | Long | 否 | — | 按批次筛选 |
| operationType | String | 否 | — | 操作类型：`fertilize` / `pesticide` / `prune` / `harvest` / `irrigate` |

---

### 7.2 创建操作记录

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/cultivation/operations` |

---

### 7.3 更新操作记录

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/cultivation/operations/{id}` |

---

### 7.4 删除操作记录（软删除）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/cultivation/operations/{id}` |

---

## 8. 成熟度与采收推荐 MaturityController

**Base URL**：`/api/planting/maturity`

---

### 8.1 录入成熟度采样

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/planting/maturity/record` |

**Request Body**：MaturityRecord 实体 JSON（`@Valid` 校验）

---

### 8.2 获取最佳采收窗口推荐

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/planting/maturity/recommend/{orchardId}` |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "orchardId": 123456789,
    "recommendedDate": "2026-04-20",
    "windowStart": "2026-04-18",
    "windowEnd": "2026-04-25",
    "confidence": 0.85,
    "factors": { "brix": 13.2, "firmness": 7.5, "color": 82 }
  }
}
```

**业务规则**

- 基于品种成熟度标准 + 最近采样数据，智能推荐采收窗口
- 返回 `MaturityRecommendation` 对象

---

### 8.3 查询品种成熟度标准列表

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/planting/maturity/standards` |

---

### 8.4 查询果园最近采样记录

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/planting/maturity/records/{orchardId}` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| limit | int | 否 | 20 | 返回记录数量 |

---

## 9. 作业计划 TaskPlanController

**Base URL**：`/api/planting/task-plan`

---

### 9.1 生成作业计划

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/planting/task-plan/generate` |
| 权限 | `cultivation:write` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| orchardId | Long | 是 | — | 果园ID |
| months | int | 否 | 1 | 生成未来N个月的计划 |

---

### 9.2 查询计划列表

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/planting/task-plan/list` |
| 权限 | `cultivation:read` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orchardId | Long | 是 | 果园ID |
| from | LocalDate | 是 | 起始日期 (ISO格式) |
| to | LocalDate | 是 | 结束日期 (ISO格式) |

---

### 9.3 标记完成

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/planting/task-plan/{id}/done` |
| 权限 | `cultivation:write` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| actualOperationId | Long | 否 | 关联的实际操作记录ID |

---

### 9.4 跳过计划

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/planting/task-plan/{id}/skip` |
| 权限 | `cultivation:write` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | String | 否 | 跳过原因 |

---

## 10. 种植分析 PlantingAnalysisController

**Base URL**：`/api/planting/analysis`

---

### 10.1 亩产排名

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/planting/analysis/yield` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| variety | String | 否 | 品种筛选 |
| year | Integer | 否 | 年份筛选 |

**响应结构**：`List<YieldPerMu>`

| 字段 | 类型 | 说明 |
|------|------|------|
| orchardId | Long | 果园ID |
| orchardName | String | 果园名称 |
| variety | String | 品种 |
| areaMu | BigDecimal | 面积（亩） |
| totalYield | BigDecimal | 总产量（kg） |
| yieldPerMu | BigDecimal | 亩产（kg/亩）= totalYield / areaMu |

---

### 10.2 优果率统计

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/planting/analysis/premium` |

**Query 参数**：同上

**响应结构**：`List<PremiumRate>`

| 字段 | 类型 | 说明 |
|------|------|------|
| orchardId | Long | 果园ID |
| orchardName | String | 果园名称 |
| gradeAWeight | BigDecimal | A级果重量（kg） |
| totalWeight | BigDecimal | 总重量（kg） |
| premiumRate | BigDecimal | 优果率 = gradeAWeight / totalWeight |

---

### 10.3 病虫害发生率

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/planting/analysis/pest` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| year | Integer | 否 | 年份筛选 |

**响应结构**：`List<PestIncidence>`

| 字段 | 类型 | 说明 |
|------|------|------|
| orchardId | Long | 果园ID |
| orchardName | String | 果园名称 |
| totalOperations | Long | 总操作次数 |
| pestOperations | Long | 农药操作次数 |
| incidenceRate | BigDecimal | 发生率 = pestOperations / totalOperations |

---

### 10.4 地块对比分析

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/planting/analysis/compare` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orchardIds | List\<Long\> | 是 | 待对比的果园ID列表 |

**响应结构**：`List<PlotComparison>`

每个 PlotComparison 包含嵌套的 YieldPerMu + PremiumRate + PestIncidence。

---

## 11. 实体字段说明

### Orchard（果园）— 表 `pt_orchard`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 雪花ID（BaseEntity） |
| orchardNo | String | 果园编号，格式：ORD+yyyyMMdd+seq |
| orchardName | String | 果园名称 |
| farmerId | Long | 关联农户ID |
| area | BigDecimal | 面积（亩） |
| variety | String | 品种：红富士/嘎拉/黄元帅等 |
| location | String | 地址描述 |
| longitude | BigDecimal | 经度 |
| latitude | BigDecimal | 纬度 |
| centerLat | BigDecimal | GIS：多边形质心纬度 |
| centerLng | BigDecimal | GIS：多边形质心经度 |
| boundaryGeojson | String | GIS：边界 GeoJSON Polygon |
| areaMu | BigDecimal | GIS：由边界计算的面积（亩） |
| treeAge | Integer | 树龄（年） |
| status | String | 状态：NORMAL / DORMANT / HARVESTED |
| remark | String | 备注 |

### HarvestBatch（采收批次）— 表 `pt_harvest_batch`

标准 CRUD 实体，含 orchardId、variety、quantity、grade、harvestDate、status 等字段。

### CultivationBatch（种植批次）— 表 `pt_cultivation_batch`

标准 CRUD 实体，含 batchCode、orchardId、variety、startDate、endDate、status 等字段。

### GrowthRecord（农事记录）— 表 `pt_growth_record`

含 orchardId、recordType、operateDate、content、operatorId 等字段。

### CultivationOperation（农事操作）— 表 `pt_cultivation_operation`

含 batchId、operationType（fertilize/pesticide/prune/harvest/irrigate）、operateDate 等字段。

### Farmer（农户）— 表 `pt_farmer`

标准 CRUD 实体，含 name、phone、idCard、address、status 等字段。

---

## 状态机

### 果园状态

```
NORMAL ──→ DORMANT ──→ HARVESTED
  ↑                         │
  └─────────────────────────┘
```

### 采收批次状态

```
DRAFT ──→ CONFIRMED（确认后自动生成溯源码）
```

### 作业计划状态

```
PLANNED ──→ DONE（标记完成）
        ──→ SKIPPED（跳过）
```
