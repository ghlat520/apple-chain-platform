# TODO — 苹果产业链平台前端缺失功能清单

> 生成日期：2026-04-11
> 基线：后端 67 个 Controller / 385 个端点 | apple-web-ui 34 页 | frontend 29 页

---

## 一、全景统计

| 维度 | 数量 |
|------|------|
| 后端 Controller | 67 |
| 后端端点总数 | 385 |
| apple-web-ui 页面 | 34 |
| frontend 页面 | 29 |
| **后端已实现、前端完全空白** | **10 个 Controller / ~88 个端点** |
| 两端功能互补需合并 | 11 个页面 |

---

## 二、优先级定义

| 级别 | 含义 | 标准 |
|------|------|------|
| **P0** | 核心业务闭环缺失 | 交易闭环、溯源信任链、系统管理 |
| **P1** | 重要业务功能缺失 | 种植智能化、冷链/仓储/金融统计页 |
| **P2** | 两端功能合并统一 | 消除双前端差异 |
| **P3** | 管理后台功能 | M8 大数据管理子模块 |

---

## 三、P0 — 核心业务闭环缺失

> 后端已实现，前端零覆盖。补上才能形成完整业务流。

### 3.1 交易闭环（M4）

后端已实现 3 个 Controller 共 19 个端点，前端完全空白。

| # | 页面 | 后端Controller | 端点 | 说明 |
|---|------|---------------|------|------|
| 1 | `trade/Inspection.vue` | QualityInspectionController | 6 | 质检管理：创建质检单、录入检验结果、验收通过、发起争议 |
| 2 | `trade/Matching.vue` | MatchController | 9 | 供需撮合：计算撮合候选、查询缓存、议价（发起/报价/接受/取消） |
| 3 | `trade/TradeStats.vue` | TradeStatisticsController | 4 | 交易统计：汇总统计、品种维度、月度趋势、价格指数 |
| 4 | `trade/Chat.vue` | (MatchController 内) | 2 | 议价聊天：发送消息、聊天历史、标记已读（可作为 Matching 子组件） |

**API 文件**：`trade.js` 新增 `inspection*`, `match*`, `negotiation*`, `chat*`, `tradeStatistics*`

**业务价值**：这是从"信息发布"到"撮合成交"的关键闭环，没有这些页面交易模块只能做简单的订单 CRUD。

---

### 3.2 溯源信任链（M3）

后端已实现 2 个 Controller 共 10 个端点，前端完全空白。

| # | 页面 | 后端Controller | 端点 | 说明 |
|---|------|---------------|------|------|
| 5 | `trace/Anomaly.vue` | AnomalyTraceController | 6 | 异常追溯：上报异常、列表、详情、开始调查、解决异常、影响范围分析 |
| 6 | `trace/Blockchain.vue` | ChainController | 4 | 区块链存证：提交业务快照上链、公开校验、管理端记录列表、手动重试 |

**API 文件**：`trace.js` 新增 `anomaly*`, `chain*`

**业务价值**：异常追溯和区块链存证是溯源模块的差异化功能，没有它们溯源只是一个普通 CRUD。

---

### 3.3 系统管理

后端已实现，前端缺用户管理页面。

| # | 页面 | 后端Controller | 端点 | 说明 |
|---|------|---------------|------|------|
| 7 | `admin/Users.vue` | UserController | 5 | 用户管理：列表、创建、更新、删除、详情 |

**API 文件**：`auth.js` 新增 `userList`, `createUser`, `updateUser`, `deleteUser`

**已有参考**：`admin/Roles.vue` 已实现 RBAC 角色管理，Users 页面可复用其风格。

---

## 四、P1 — 重要业务功能缺失

### 4.1 种植智能化（M1）

后端已实现 3 个 Controller 共 15 个端点，前端完全空白。

| # | 页面 | 后端Controller | 端点 | 说明 |
|---|------|---------------|------|------|
| 8 | `planting/HarvestBatch.vue` | HarvestBatchController | 7 | 采收批次：列表、创建、更新、确认采收（自动生成溯源码）、删除、导出 |
| 9 | `planting/TaskPlan.vue` | TaskPlanController | 4 | AI 作业计划：生成计划、查询列表、标记完成、跳过 |
| 10 | `planting/Analysis.vue` | PlantingAnalysisController | 4 | 种植分析：亩产排名、优果率统计、病虫害发生率、地块对比 |

**API 文件**：新增 `harvest.js`, `taskPlan.js`, `plantingAnalysis.js` 或合并到 `cultivation.js`

---

### 4.2 各模块统计页面

后端已有统计接口，frontend 已有统计页面但 apple-web-ui 缺失。

| # | 页面 | 后端接口 | apple-web-ui | frontend |
|---|------|---------|-------------|----------|
| 11 | `warehouse/Statistics.vue` | WarehouseStatisticsController (3) | ❌ 缺 | ✅ 有 WarehouseStats.vue |
| 12 | `coldchain/Statistics.vue` | TransportStatisticsController (2) | ❌ 缺 | ✅ 有 ColdchainStats.vue |
| 13 | `finance/Statistics.vue` | FinanceStatisticsController (2) | ❌ 缺 | ✅ 有 FinanceStats.vue |

**做法**：参考 frontend 已有的统计页面，在 apple-web-ui 中实现对应版本。

---

### 4.3 冷链补充

| # | 页面 | 后端Controller | 端点 | 说明 |
|---|------|---------------|------|------|
| 14 | `coldchain/Precooling.vue` | PreCoolTaskController | 5 | 预冷管理：apple-web-ui 缺，frontend 已有 PrecoolingList.vue |

---

## 五、P2 — 两端功能合并统一

> 目标：消除双前端差异，合并为统一版本。以下按模块列出两边互补关系。

### 5.1 apple-web-ui 需要补充（frontend 已有）

| # | 页面 | 来源 | 说明 |
|---|------|------|------|
| 15 | `ModuleGrid.vue` | frontend | 九宫格模块入口首页（提升导航体验） |
| 16 | `farm/OrchardMap.vue` | frontend | 高德 GIS 地图页 |
| 17 | `farm/OrchardDetail.vue` | frontend | 果园详情页（Tab 展示：基本信息 + 种植记录 + 采收记录） |
| 18 | `trace/Detail.vue` | frontend | 溯源详情页（时间线展示完整溯源链路） |

### 5.2 frontend 需要补充（apple-web-ui 已有）

| # | 页面 | 来源 | 说明 |
|---|------|------|------|
| 19 | `cultivation/Batches.vue` | apple-web-ui | 种植批次管理 |
| 20 | `cultivation/Operations.vue` | apple-web-ui | 种植作业管理 |
| 21 | `cultivation/GrowthRecords.vue` | apple-web-ui | 农事记录（施肥/喷药/灌溉/修剪/病虫害） |
| 22 | `planting/MaturityRecord.vue` | apple-web-ui | 成熟度采样录入 |
| 23 | `planting/HarvestRecommend.vue` | apple-web-ui | 采收窗口推荐 |
| 24 | `trace/CodeGenerate.vue` | apple-web-ui | 一果一码生成（BOX 级 + FRUIT 级） |
| 25 | `trace/PublicScan.vue` | apple-web-ui | 公开扫码查询页（无需登录） |
| 26 | `coldchain/Temperatures.vue` | apple-web-ui | 温度监控曲线图 |
| 27 | `coldchain/Deliveries.vue` | apple-web-ui | 配送签收管理 |
| 28 | `admin/Roles.vue` | apple-web-ui | RBAC 角色权限矩阵 |
| 29 | `trade/PurchaseNeeds.vue` | apple-web-ui | 采购需求管理 |
| 30 | `trade/Supply.vue` | apple-web-ui | 供货信息管理 |

---

## 六、P3 — M8 大数据管理子模块

> 后端已完整实现 6 个 Controller 共 37 个端点 + 审计日志，前端零页面。
> 属于管理后台功能，优先级低于业务模块。

| # | 页面 | 后端Controller | 端点 | 说明 |
|---|------|---------------|------|------|
| 31 | `bigdata/DataAsset.vue` | DataAssetController | 8 | 数据资产目录（注册/发现/字段管理） |
| 32 | `bigdata/DataSource.vue` | DataSourceController | 6 | 数据源注册与管理（MySQL/Redis/API） |
| 33 | `bigdata/DataQuality.vue` | DqRuleController | 7 | 数据质量规则（创建/执行/查看结果） |
| 34 | `bigdata/CollectJob.vue` | CollectJobController | 7 | 采集任务（创建/调度/查看运行日志） |
| 35 | `bigdata/Lineage.vue` | DataLineageController | 5 | 数据血缘（上下游关系图谱） |
| 36 | `bigdata/MetricCenter.vue` | MetricController | 7 | 指标中心（定义/计算/查询历史值） |
| 37 | `bigdata/Report.vue` | AnalysisReportController | 4 | 分析报告（生成/列表/详情/下载） |
| 38 | `bigdata/AuditLog.vue` | AuditLogQueryController | 1 | 操作审计日志查询 |
| 39 | `bigdata/OpenApi.vue` | OpenApiController | 5 | 开放 API 客户端管理 |
| 40 | `bigdata/ScreenConfig.vue` | DashboardConfigController | 9 | 数据大屏配置（仪表盘/组件/布局） |

---

## 七、执行建议

### 阶段一：P0 核心闭环（预估 7 个新页面）

```
P0.1  trade/Inspection.vue     — 质检管理
P0.2  trade/Matching.vue       — 供需撮合 + 议价聊天
P0.3  trade/TradeStats.vue     — 交易统计图表
P0.4  trace/Anomaly.vue        — 异常追溯
P0.5  trace/Blockchain.vue     — 区块链存证
P0.6  admin/Users.vue          — 用户管理
```

### 阶段二：P1 重要功能（预估 7 个新页面）

```
P1.1  planting/HarvestBatch.vue  — 采收批次
P1.2  planting/TaskPlan.vue      — AI 作业计划
P1.3  planting/Analysis.vue      — 种植分析
P1.4  warehouse/Statistics.vue   — 仓储统计
P1.5  coldchain/Statistics.vue   — 冷链统计
P1.6  finance/Statistics.vue     — 金融统计
P1.7  coldchain/Precooling.vue   — 预冷管理
```

### 阶段三：P2 合并统一（预估 16 个页面搬迁）

```
P2.1  将 frontend 独有 4 个页面搬到 apple-web-ui
P2.2  将 apple-web-ui 独有 12 个页面搬到 frontend
```

### 阶段四：P3 管理后台（预估 10 个新页面）

```
P3.1  M8 大数据 10 个管理页面
```

---

## 八、技术约束

| 项 | 要求 |
|----|------|
| 前端框架 | Vue 3 + Vant 4（移动端优先） |
| API 基路径 | axios baseURL = `/api`，前端写相对路径如 `/trade/orders` |
| 鉴权 | JWT Bearer Token，公开路径在 WebMvcConfig 中配置 |
| RBAC | 按钮级权限控制，路由 meta.perm + v-permission 指令 |
| 状态管理 | Pinia (auth store) |
| 图表 | ECharts（已在 vite.config manualChunks 中拆包） |
| 地图 | 高德 Web JSAPI（Key/Secret 见环境变量，禁止 hardcode） |
| 编译验证 | 修改后端后必须 `mvn clean compile test-compile` |
| API 验证 | 前端代码必须以后端实际代码为上下文生成（先 curl 验证再写页面） |

---

## 九、完成标准

- [ ] P0：6 个核心页面全部实现，对应后端 API 全部 200
- [ ] P1：7 个重要页面全部实现，含 ECharts 图表
- [ ] P2：双前端功能完全对齐，任一前端可独立使用
- [ ] P3：M8 大数据管理后台 10 个页面全部实现
- [ ] 全部 47 个现有 API + 新增 88 个 API 端点均通过前端可达性测试
- [ ] Playwright E2E 覆盖所有关键业务流程
