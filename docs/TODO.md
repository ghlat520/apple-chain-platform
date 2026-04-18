# TODO — 苹果产业链平台前端缺失功能清单

> 生成日期：2026-04-11  |  最近更新：2026-04-17
> 交付架构：**PC 端（apple-admin-ui）+ Mobile 端（H5 + 小程序，共享 apple-shared-core）**
> 进度摘要：契约已补齐 14/14 ✅ | PC 端大数据 10 页已完成 ✅ | Mobile 统一方案（apple-mobile-ui dual-build）骨架就位，迁移未开始

---

## 一、全景统计

### 1.1 交付架构（2026-04-17 终态，uni-app 多端统一）

```
PC 端：
  └── apple-admin-ui ............ Vue 3 + Element Plus（Vite），管理员/金融/监管（56 页）

Mobile 一套代码多端（uni-app + Vue 3 + uView Plus）：
  └── apple-mobile-ui ........... 改造为 uni-app，编译产出：
        ├── yarn build:h5-full   → 工作端 H5（对位旧 apple-web-ui 65 页）
        ├── yarn build:h5-lite   → C 端公开 H5（对位旧 apple-h5-ui 7 页）
        ├── yarn build:mp-weixin → 微信小程序
        └── yarn build:app       → uni 原生 App（可选）
        场景分化：`#ifdef H5 / MP-WEIXIN / APP` 条件编译 + pages.json subPackages

共享业务层（纯 TS，保持不动）：
  └── apple-shared-core ......... 19 API 客户端 + 7 types 模块，基于 axios
        小程序端兼容：在 uni-app 入口加 axios → uni.request adapter（约 10 行），
        shared-core **不需要重构**，19 API 模块 import 零改动

遗留 / 非交付物（冻结新功能，按需迁移）：
  ├── apple-web-ui .............. 旧版 Vue 3 + Vant Web，63 页生产，迁移到 apple-mobile-ui（Vant → uView Plus，HTML 标签 → uni-app 标签）
  ├── apple-h5-ui ............... 新启 H5，5 页，迁入 apple-mobile-ui `build:h5-lite`
  └── frontend .................. Mock 演示版，31 页（架构师沙盒，非交付物，保留）

已废弃（不建）：
  └── apple-miniapp ............. 原计划独立小程序项目；uni-app 方案已覆盖，不再独立立项
```

> **关键约束**：移动端坚持"一套代码多端适配"，不重复开发。Vant（Web）无法跑小程序运行时，故 UI 组件库全量换 uView Plus。

### 1.2 数字矩阵

| 维度 | 原基线（04-11） | 04-17 实际 | 说明 |
|------|---------------|-----------|------|
| 后端 Controller | 67 | 67 | — |
| 后端端点总数 | 385 | 385 | — |
| **PC 端**（apple-admin-ui） | — | **56 页** | 交付物 |
| **Mobile H5**（apple-h5-ui） | — | **7 页** | 交付物（新启，依赖 shared-core） |
| **Mobile 小程序**（apple-miniapp） | — | **不立项** | uni-app `build:mp-weixin` 直接产出，apple-miniapp 目录不存在 |
| apple-mobile-ui（待改造 uni-app） | — | **63 页（full）+ 5 页（lite）** | 现状为 Vite + Vue 3，改造为 uni-app |
| apple-shared-core API 客户端 | — | **19 模块** | auth/planting/trace/coldchain/finance/bigdata 等全覆盖 |
| apple-shared-core types | — | **7 模块** | auth/common/file/logistics/planting/trace + index |
| apple-web-ui（遗留） | 34 | **65 页**（Vant 使用 3262 处 / 67 文件） | **待迁移，非交付** |
| frontend（演示 Mock） | 29 | 31 页 | 非交付 |
| 契约模块化覆盖 | 1/13 (7.7%) | **14/14 (100%) ✅** | 已补齐 |
| **后端已实现、前端完全空白** | 10 Controller / ~88 端点 | ~4 Controller / ~30 端点 | 大幅缩小 |

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
| 1 | `trade/Inspection.vue` ✅ admin-ui 已实现（web-ui/frontend 仍缺，04-17 确认） | QualityInspectionController | 6 | 质检管理：创建质检单、录入检验结果、验收通过、发起争议 |
| 2 | `trade/Matching.vue` ✅ admin-ui 已实现（web-ui/frontend 仍缺，04-17 确认） | MatchController | 9 | 供需撮合：计算撮合候选、查询缓存、议价（发起/报价/接受/取消） |
| 3 | `trade/TradeStats.vue` | TradeStatisticsController | 4 | 交易统计：汇总统计、品种维度、月度趋势、价格指数 |
| 4 | `trade/Chat.vue` | (MatchController 内) | 2 | 议价聊天：发送消息、聊天历史、标记已读（可作为 Matching 子组件） |

**API 文件**：`trade.js` 新增 `inspection*`, `match*`, `negotiation*`, `chat*`, `tradeStatistics*`

**业务价值**：这是从"信息发布"到"撮合成交"的关键闭环，没有这些页面交易模块只能做简单的订单 CRUD。

---

### 3.2 溯源信任链（M3）

后端已实现 2 个 Controller 共 10 个端点，前端完全空白。

| # | 页面 | 后端Controller | 端点 | 说明 |
|---|------|---------------|------|------|
| 5 | `trace/Anomaly.vue` ✅ web-ui + admin-ui 已实现（04-17 确认） | AnomalyTraceController | 6 | 异常追溯：上报异常、列表、详情、开始调查、解决异常、影响范围分析 |
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

## 五、P2 — 两端功能合并统一 ⚠️ 已被 apple-mobile-ui dual-build 方案取代

> **原目标**：消除 apple-web-ui / frontend 双前端差异。
> **新方向（04-17 修正）**：两端都是遗留 / 非交付物，真正的"合并"是把 **apple-web-ui 63 页 + apple-h5-ui 5 页** 迁移到 **apple-mobile-ui dual-build**（`--mode full` 产 web 等价 bundle / `--mode lite` 产 H5 等价 bundle），并启动小程序。详见 §十三。
>
> 以下互补关系保留作为"迁移时必带的功能清单"（不是合并到 frontend，而是合并到 apple-mobile-ui）。

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
| 24 | `trace/CodeGenerate.vue` ✅ web-ui 已实现（04-17 确认） | apple-web-ui | 一果一码生成（BOX 级 + FRUIT 级） |
| 25 | `trace/PublicScan.vue` | apple-web-ui | 公开扫码查询页（无需登录） |
| 26 | `coldchain/Temperatures.vue` | apple-web-ui | 温度监控曲线图 |
| 27 | `coldchain/Deliveries.vue` | apple-web-ui | 配送签收管理 |
| 28 | `admin/Roles.vue` | apple-web-ui | RBAC 角色权限矩阵 |
| 29 | `trade/PurchaseNeeds.vue` | apple-web-ui | 采购需求管理 |
| 30 | `trade/Supply.vue` | apple-web-ui | 供货信息管理 |

---

## 六、P3 — M8 大数据管理子模块 ✅ 已完成（04-17 审计确认）

> apple-web-ui + apple-admin-ui 均已实现 10 页（资产/源/质量/采集/血系/指标/报告/审计/API/屏配）。
> 以下 10 项已全部交付，保留作为交付清单。

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

- [~] P0：6 核心中 3 已交付（Inspection-admin / Matching-admin / Anomaly-双端 / CodeGenerate-web）
- [ ] P0：剩余 Blockchain / TradeStats / Users.vue / 跨端补齐
- [ ] P1：7 个重要页面全部实现，含 ECharts 图表
- [ ] P2：双前端功能完全对齐，任一前端可独立使用
- [x] P3：M8 大数据管理后台 10 页已实现（04-17 审计确认）
- [ ] 全部 47 个现有 API + 新增 88 个 API 端点均通过前端可达性测试
- [ ] Playwright E2E 覆盖所有关键业务流程
- [ ] 后端占位补丁 5 项（Aliyun SMS / OSS / 支付网关 / 健康探针 / 微信支付，见 §十一）
- [ ] 生产就绪 11 项（DDL / 压测 / 监控 / CI-CD / E2E 等，见 §十）
- [ ] 模块决策 3 项（chain 空壳 / iot 前端 / 双前端合并，见 §十二）

---

## 十、后端生产就绪补课（11 项，来源：AUDIT-2026-04-12 §维度E，就绪度 43%）

| # | 项 | 现状 | 阻塞度 | 建议动作 |
|---|-----|------|-------|---------|
| E1 | 远程 MySQL Flyway 建表 | 仅本地/H2 有表 | 🔴 上生产阻塞 | 用户手动执行 `aiin_dev_db01` DDL |
| E2 | 演示数据集 seed SQL | 缺失 | 🔴 对客演示阻塞 | 准备 demo-seed.sql（果园/农户/批次/订单样本） |
| E3 | JMeter 性能压测基线 | 无 | 🟡 生产容量未知 | 至少覆盖登录 + 溯源扫码 + 订单下单三场景 |
| E4 | OWASP ZAP 安全扫描 | 无 | 🟡 安全审计未过 | CI 集成一次 passive scan |
| E5 | Prometheus + Grafana 监控告警 | 未配 | 🔴 线上故障响应慢 | 补 actuator + prom-exporter + 关键 SLI 告警 |
| E6 | 日志聚合（ELK / 云日志） | 未知 | 🟡 排查效率低 | 接 logback-appender → Loki / ES |
| E7 | 备份 / 回滚 runbook | 未演练 | 🟡 容灾缺失 | 文档 + 每月一次演练 |
| E8 | 熔断 / 限流（Sentinel / Resilience4j） | 无 | 🟡 流量无降级 | 至少保护登录 + 微信支付回调 |
| E9 | CI/CD（GitHub Actions） | 未知 | 🔴 合并保护缺失 | `mvn clean verify` + 前端 build + design-lint 必过 |
| E10 | Playwright E2E 关键业务流 | 无 | 🟡 回归靠手工 | 覆盖 6 角色登录 + 溯源扫码 + 下单 + 贷款申请 |
| E11 | 数据闭环端到端通测 | 物理就位未通测 | 🟡 大数据 Demo 不可信 | Canal → SeaTunnel → ClickHouse → Dashboard 串一次 |

---

## 十一、后端占位补丁（5 项，2026-04-17 新发现）

> 这些不是"功能缺失"，是"已写但未完成"——原 TODO.md 未登记。

| # | 文件 | 问题 | 阻塞场景 | 建议动作 |
|---|------|------|---------|---------|
| B1 | `apple-module-user/.../AliyunSmsSenderServiceImpl.java:17` | `throw UnsupportedOperationException("Aliyun SMS not yet integrated")` | 短信验证码生产发送 | 接入阿里云 SMS SDK + 环境变量配置 AK/SK |
| B2 | `apple-module-bigdata/.../OpsMonitorController.java:32` | Kafka / Canal / ClickHouse / DolphinScheduler 健康检查返回 `UNKNOWN` | 数据闭环可观测 | 实现真实探针（复用 actuator HealthIndicator） |
| B3 | `apple-module-finance-gateway/*` | ContractGateway / InvoiceGateway / PaymentGateway 全为 Mock，生产网关仅 `@ConditionalOnProperty` 声明 | 金融对客（贷款放款 / 发票开具 / 合同签署） | 对接真实金融机构 API，配置切换到 prod profile |
| B4 | `apple-module-file/.../OssFileStorageService` | 占位类未加 `@Service` 注解 | 文件上传走本地磁盘，OSS 未启用 | 实现 OSS SDK 调用 + 加 `@Service` + 配置切换开关 |
| B5 | `contract/modules/wechat-pay.md` | 契约已定义但后端 `apple-module-finance-gateway` 无对应 Controller 实现；Mock 返回 `qrCodeUrl: "mock://..."` | 扫码支付 | 实现 WxPayController + 对接商户号 + 回调签名校验 |

---

## 十二、模块决策项（3 项，需产品/架构拍板）

| # | 模块 | 现状 | 待决策 | 建议 |
|---|------|------|-------|------|
| D1 | `apple-module-chain` | 0 Controller / 0 Service，`ChainController` 功能已并入 `apple-module-trace` | 删除空壳 or 保留模块占位（未来独立）| 建议：**保留**。区块链存证独立性强，后续可能分离部署。至少补一个 `README.md` 说明"当前实现位置" |
| D2 | `apple-module-iot` | 仅 1 Controller / 4 端点；契约预期 5+ 接口；web-ui 有 `iot.js` API 但 **0 页面绑定**；admin-ui/frontend 全无 | 是否补 IoT 监控页 + 告警规则管理 API | 建议：**先补前端页**（传感器列表 / 实时数据曲线 / 告警历史），后端 API 已够用 |
| D3 | Mobile 端架构 | 4 套并存：apple-web-ui（63 页旧 Vant Web）/ apple-h5-ui（5 页新）/ apple-mobile-ui（0 页 Vite 骨架）/ apple-miniapp（未建）| 确认终态技术栈 + 一套代码多端适配 | **已决策（04-17）**：apple-mobile-ui 改造为 **uni-app（Vue 3 + uView Plus）单一代码库**，编译产出 4 个目标：`build:h5-full`（工作端）/ `build:h5-lite`（C 端）/ `build:mp-weixin`（小程序）/ `build:app`（可选）。**apple-miniapp 不再独立立项**；apple-web-ui / apple-h5-ui 逐步归档。详见 §十三 |
| D4 | frontend 定位 | Mock 版 31 页，非交付物 | 保留 / 废弃 | 建议：**保留**为架构师沙盒（contract 验证用），但明确不作交付物 |

---

## 十三、Mobile 统一迁移路线（uni-app 方案，2026-04-17 定稿；2026-04-18 精简）

> **目标**：H5 + 微信小程序 + 未来 App，一套代码多端编译，不重复维护
> **范围**：纯前端 UI 迁移，**不动后端 API**，apple-shared-core 保持不变

### 13.1 技术栈

| 维度 | 选择 | 理由 |
|------|------|------|
| 多端框架 | **uni-app**（Vue 3 + TS） | 国内小程序生态最成熟，`#ifdef` 条件编译原生支持 |
| UI 组件库 | **uView Plus 3.3.x**（版本锁定） | H5 / 小程序 / App 全端通用，API 接近 Vant |
| 构建 | Vite + `@dcloudio/vite-plugin-uni` | 与现有工具链兼容 |
| 状态管理 | Pinia | 与 apple-admin-ui 对齐 |
| 路由 | pages.json + subPackages | uni-app 原生 |

### 13.2 小程序 HTTP 兼容（轻量 adapter）

apple-shared-core 保持不变（19 API 模块继续用 axios）。小程序端通过 adapter 让 axios 走 uni.request：

```typescript
// apple-mobile-ui/src/main.ts
// #ifdef MP-WEIXIN
import axios from 'axios'
import { uniAxiosAdapter } from './adapters/axios-uni-adapter'
axios.defaults.adapter = uniAxiosAdapter  // ~10 行实现
// #endif
```

可直接用 npm 包 `@uni-helper/axios-adapter`，或自写 10 行 adapter 把 axios config 翻译成 uni.request 调用。

### 13.3 apple-mobile-ui 改造步骤

1. `git tag mobile-ui-pre-uniapp-2026-04-18` + `git branch legacy` 备份现状
2. `package.json` 重建：`@dcloudio/uni-app` / `@dcloudio/uni-h5` / `@dcloudio/uni-mp-weixin` / `uview-plus@3.3.x`
3. `vite.config.ts` 接入 `@dcloudio/vite-plugin-uni`
4. 建 `src/pages.json` / `src/main.ts`（Vue 3 createSSRApp）/ `src/App.vue`
5. 接入 uView Plus + 全局配置 `styleIsolation: 'shared'`
6. `src/design/tokens.uni.ts`：px ↔ rpx 双模式按平台条件输出

### 13.4 页面迁移方式

| 项 | 处理方式 |
|----|----------|
| `<script setup>` 业务逻辑 / API 调用 / store | **基本不动**（80-90% 保留） |
| Vant 组件（`<van-*>`） | 70% 机械替换为 `<u-*>`，少数 slot 结构微调 |
| HTML 标签（div/span/img/a） | codemod 批量替换为 view/text/image/navigator |
| vue-router | 改写到 pages.json |
| CSS 单位 | 走 tokens.uni.ts 按平台输出（H5 px / 小程序 rpx） |

**自建组件**（uView Plus 无对标）：
- `<AppleUploader>`：统一 H5 `<input type=file>` + 小程序 `uni.chooseImage` + `uni.uploadFile`
- `<AppleAreaPicker>`：省市区级联（前端打包 GB/T 2260 JSON，~200KB）

### 13.5 工作端 / C 端分化 + 小程序分包

- 主包（≤ 2MB）：登录 / 首页 / 扫码 / 公开查询 / 个人中心（5-6 页硬上限）
- subPackages 按角色分：`pages-work/{farmer,coop,regulator,cold,finance,admin}`
- `build:h5-lite` 通过环境变量屏蔽 `pages-work/*` 路由
- 条件编译：`#ifdef H5 / MP-WEIXIN / APP` 处理端差异

### 13.6 迁移顺序

| 阶段 | 周期 | 动作 |
|------|------|------|
| 阶段 1 | 1-2 周 | 骨架：uni-app + uView Plus + axios adapter + 三端 Hello World 产物 |
| 阶段 2 | 2-3 周 | P0 核心页（登录 / 扫码 / 首页 / 个人中心） |
| 阶段 3 | 4-5 周 | P1 业务页（果园 / 农事 / 订单 / 冷链 / 贷款） |
| 阶段 4 | 4-5 周 | P2 剩余页 + 6 角色菜单密度适配 |
| 阶段 5 | 常态 | apple-web-ui / apple-h5-ui 归档 |

**总计 11-15 周**（2-3 人），按实际进度滚动调整。

### 13.7 要注意的坑

- Vant Uploader / Area 无 uView Plus 对标 → 自建（各 1-2 天）
- 小程序主包 2MB 限制 → subPackages 分包
- `<input type=file>` 在小程序不可用 → `uni.chooseImage`
- uView Plus 版本锁定（3.3.x） → 升级必跑回归
- DESIGN.md 6 角色密度 + rpx 适配 → tokens.uni.ts 统一管理
- 小程序 CSS 变量只能挂 `page { ... }`，不能挂 `<html>`

### 13.8 完成标准

- [ ] apple-mobile-ui 改造为 uni-app，三端产物可出（`build:h5-full / build:h5-lite / build:mp-weixin`）
- [ ] uView Plus 接入 + DESIGN.md tokens 映射
- [ ] axios + uni.request adapter 跑通小程序端请求
- [ ] `<AppleUploader>` + `<AppleAreaPicker>` 两个自建组件就位
- [ ] P0/P1/P2 页面逐步迁移完成
- [ ] 工作端 H5 + C 端 H5 + 微信小程序三端同步上线
- [ ] apple-web-ui / apple-h5-ui 归档（README 标 deprecated）
