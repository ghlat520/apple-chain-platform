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

Mobile 三端一套代码（uni-app + Vue 3 + uView Plus）：
  └── apple-mobile-ui ........... 现状：Vite + Vue 3，已有 63 业务页（src/modules/full/）+ lite 5 页，
                                  改造为 uni-app 后 full 63 页作废重写（沉没成本，决策者须盖章），
                                  lite 5 页可评估保留为 h5-lite 子项目（原本就是 vue-router H5 架构）
                                  编译目标：
        ├── yarn build:h5-full   → 工作端 H5（内部员工，对位旧 apple-web-ui 65 页）
        ├── yarn build:h5-lite   → C 端公开 H5（扫码溯源等，对位旧 apple-h5-ui 7 页）
        ├── yarn build:mp-weixin → 微信小程序（C 端扫码为主 + 工作端核心能力）
        └── yarn build:app       → uni 原生 App（iOS / Android，可选，按业务决定）
        场景分化：通过 `#ifdef H5 / MP-WEIXIN / APP` 条件编译，工作端/C端通过路由分组或独立 pages.json 分组

共享业务层（框架无关）：
  └── apple-shared-core ......... 纯 TS（19 API 客户端 + 7 types 模块）
        ⚠️ 现状：19 API 模块全部直接 `import { httpGet } from './request'` 硬编码绑定 axios，
            无 HttpClient 抽象层、无 setHttpClient() 注入点；`configureApi()` 仅注入
            getToken/onUnauthorized/onError 业务钩子，**不能切换 HTTP 引擎**。
        改造必需：新增 src/http/ 目录 → HttpClient 接口 → axios + uni.request 双实现
            → 重构 request.ts 为 delegating wrapper → 保持 19 API 模块 import 不变（向后兼容）

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
| apple-mobile-ui（待改造 uni-app） | — | **63 页（full）+ 5 页（lite）**，非骨架 | full 沉没重写 / lite 可评估保留 |
| apple-shared-core API 客户端 | — | **19 模块** | auth/planting/trace/coldchain/finance/bigdata 等全覆盖 |
| apple-shared-core types | — | **7 模块** | auth/common/file/logistics/planting/trace + index |
| apple-shared-core HttpClient 抽象 | — | ❌ **缺失** | 19 API 硬编码 axios，阶段 0 必须重构 |
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

## 十三、Mobile 统一迁移路线（uni-app 方案，2026-04-17 定稿）

> **终态**：**PC 端（apple-admin-ui，Element Plus）+ Mobile 端（apple-mobile-ui，uni-app 一套代码 → H5 工作端 + H5 C 端 + 微信小程序 + 可选 App）**
> **指导原则**：移动端坚持"**一套代码多端适配，不重复开发**"；Vant 不兼容小程序运行时，UI 组件库全量换 uView Plus。
> **现状**：共享层 19 API 客户端 / 7 types 就位；apple-mobile-ui 现为 Vite + Vue 3 骨架（0 页），**需改造为 uni-app**；旧 apple-web-ui（63 页 Vant Web）和 apple-h5-ui（5 页）等待迁入。

### 13.1 技术栈决策

| 维度 | 选择 | 理由 |
|------|------|------|
| 多端框架 | **uni-app** | 国内小程序生态最成熟；Vue 3 + TS 原生支持；H5/小程序/App 单一代码；条件编译 `#ifdef` 成熟；文档/社区优于 Taro Vue |
| UI 组件库 | **uView Plus**（uni-app 专用） | 组件 API 接近 Vant（迁移成本低）；官方维护；支持 H5/小程序/App 全端 |
| 构建工具 | HBuilderX CLI / @dcloudio/uni-cli（Vite 驱动） | 与现有 Vite 工具链兼容 |
| 状态管理 | Pinia | 与 apple-admin-ui 对齐，降低心智成本 |
| 路由/页面 | uni-app `pages.json` | 工作端/C 端通过 **subPackages + 路由分组** 分离 |
| TypeScript | 是 | apple-shared-core 纯 TS，最大化复用 |

**apple-miniapp 原计划废弃**：uni-app 的 `build:mp-weixin` 直接产出微信小程序，无需单独立项。

### 13.2 apple-shared-core 适配（关键前置，已修正 2026-04-17 审查）

| 层 | 现状（实测） | 缺口（必补）|
|----|--------|------|
| API 客户端 19 模块 | ✅ auth / planting / cultivation / trace / farm / coldchain / logistics / warehouse / finance / input / iot / bigdata / stats / trade / maturity / orchardGeo / file / request / index | 调用方式绑死 axios：全部 `import { httpGet/httpPost/... } from './request'` |
| Types 7 模块 | ✅ auth / common / file / logistics / planting / trace | ❌ 缺 9：coldchain / warehouse / finance / input / iot / bigdata / trade / farm / cultivation |
| HttpClient 抽象层 | ❌ **不存在**（`src/http/` 目录未建，`HttpClient` 接口未定义，`setHttpClient()` 函数未实现） | 需从零设计抽象层并重构 19 API 模块（见下方改造清单） |
| 业务拦截逻辑 | ⚠️ R<T> envelope 解包 / 401 redirect / onError toast / Bearer token 注入 **全部耦合在 axios 拦截器内**（request.ts 第 75-108 行） | 需上移到平台无关的通用层（适配器共用） |
| Composables | 目录已建 | ❌ 需审计函数覆盖度 |

**阶段 0 改造清单**（8-11 人日，不是"1 周加 200 行"）：

1. **新增抽象接口**（`apple-shared-core/src/http/HttpClient.ts`）：
   ```
   interface HttpClient {
     request<T>(config: HttpConfig): Promise<HttpResponse<T>>
     get / post / put / delete / upload / addInterceptor / cancel
   }
   interface HttpConfig  { url, method, baseURL?, headers?, params?, data?, timeout?, signal? }
   interface HttpResponse<T> { data: T, status: number, headers: Record<string,string> }
   ```

2. **上移业务逻辑**到 `src/http/response-pipeline.ts`（两个适配器共用）：
   - R<T> envelope 解包（code===200 → data；否则抛 ApiError）
   - 401 → `hooks.onUnauthorized()`
   - 非 2xx 统一 reject（axios 自然行为；uni.request 需手动判断 statusCode）
   - onError toast

3. **两个适配器实现**：
   - `src/http/axios-client.ts` — 现 request.ts 内容迁入（H5 / App / PC 端共用）
   - `src/http/uni-client.ts` — 新建（小程序专用）

4. **改造 `request.ts` 为 delegating wrapper**：
   ```
   export function httpGet<T>(url, config) {
     return getHttpClient().get<T>(url, config).then(r => r.data)
   }
   ```
   → 19 个 API 模块**调用不变**（向后兼容），只是底层实现可切换

5. **入口注入**（`apple-mobile-ui/src/main.ts`）：
   ```
   // #ifdef MP-WEIXIN || MP-ALIPAY || MP-BAIDU
   import { setHttpClient, createUniClient } from '@apple/shared-core/http'
   setHttpClient(createUniClient({ baseURL, getToken, onUnauthorized, onError }))
   // #endif
   // H5 / App 默认走 axios，无需显式注入
   ```

6. **回归测试（必须）**：apple-admin-ui（PC 端，已依赖 shared-core）在新版本下契约测试 100% 通过，否则阻断发布。

**uni.request 适配器关键处理点**（§13.2 原 4 行伪代码漏掉的核心逻辑，~200 行真实实现）：

| 关键点 | axios 行为 | uni.request 差异 | 适配动作 |
|---|---|---|---|
| baseURL 拼接 | 自动 | 不支持 | 手动 `baseURL + url` |
| statusCode 4xx/5xx | 抛进 `error.response` | **仍走 success 回调** | success 内 `if (statusCode >= 400) reject(...)` |
| fail 参数类型 | `AxiosError` 实例 | `{errMsg, errno}` 普通对象 | 包装成 Error，伪造 `error.response.status` 保持下游兼容 |
| 响应字段名 | `{data, status, headers, ...}` | `{data, statusCode, header, cookies}` | 翻译到统一形状 |
| 拦截器 | `interceptors.use()` | 不支持 | 自建 req/res 两层拦截器链 |
| 取消请求 | `AbortController` | `requestTask.abort()` | `config.signal.addEventListener('abort', () => task.abort())` |
| 文件上传 | FormData + axios | **必须独立用 `uni.uploadFile`**（API 完全不同） | 单独 `httpUpload()` 函数，`filePath` 是小程序临时路径而非 File 对象 |
| upload 响应 | 自动 JSON | `res.data` 是字符串 | `JSON.parse(res.data)` |
| 上传进度 | `onUploadProgress` | `requestTask.onProgressUpdate()` | 事件订阅 |
| params 序列化 | 自动 | 需手动拼 querystring | `url + '?' + new URLSearchParams(params)` |
| 默认 headers | 配置即生效 | 每次 merge | 每个 request 手动 merge `Authorization` 等 |

**代码审查共识**：§13.2 原伪代码（4 行 success/fail）是"等接口定义好了再实现"的占位符，**不是可落地方案**。阶段 0 必须先完成抽象层重构（第 1-5 步），再实现适配器。

### 13.3 apple-mobile-ui 改造（Vite → uni-app）

**⚠️ 存量处置（决策者须盖章）**：

apple-mobile-ui 现非"0 页骨架"，已有：
- `src/modules/full/` — 63 个业务页（farm/trace/coldchain/...），全部基于 vue-router + Vant + 原生 HTML 标签，**按 §13.4 迁移口径 ≈ 30-60% 工时重写**
- `src/modules/lite/` — 5 个 C 端页，同样 vue-router 架构，**可评估保留为 h5-lite 子项目**（原本就不走小程序）
- `main.js / main-lite.js / layouts-lite / stores-lite` — 双入口结构

**决策选项**（产品/架构拍板）：

| 选项 | 63 页 full | 5 页 lite | 成本 | 风险 |
|---|---|---|---|---|
| A：全部作废 | 沉没重写 | 沉没重写 | 最高（63+5 页 uni-app 重写） | 最低（全新架构，无历史包袱） |
| B：lite 保留 | 沉没重写 | **保留为 h5-lite 独立项目**（vue-router H5） | 中（仅 63 页重写） | lite 与主仓割裂，两套 CI |
| C：Git 备份 + 渐进迁移 | **归档到 legacy-full 分支**，P0/P1 功能在新 uni-app 下重写，P2 旧页冻结维护 | 同 B | 中低 | 存量一段时间双轨 |

**建议**：选 **C** — 创建 `git tag mobile-ui-pre-uniapp-2026-04-17` + `legacy-full` 分支保存现状，主干 main 上做 uni-app 改造；若线上生产回退需要，可检出 legacy 分支快速恢复。

**改造动作**（基于选 C）：
- `git tag mobile-ui-pre-uniapp-2026-04-17` + `git branch legacy-full`（存量快照）
- 废弃现有 `main.js / main-lite.js / layouts-lite / stores-lite` 双入口结构
- `package.json` 重建：`@dcloudio/uni-app` / `@dcloudio/uni-h5` / `@dcloudio/uni-mp-weixin` / `uview-plus` 3.3.x（**版本锁定**，不同版本组件完整度差异大）
- `vite.config.ts` 接入 `@dcloudio/vite-plugin-uni`
- 创建 `src/pages.json`（uni-app 路由清单）/ `src/main.ts`（Vue 3 createSSRApp）/ `src/App.vue`
- 新增 `src/design/tokens.uni.ts`（px ↔ rpx 双模式，按 `UNI_PLATFORM` 条件编译）
- uView Plus 全局配置 `styleIsolation: 'shared'`，验证 CSS 变量在组件内继承（DESIGN.md 6 角色密度依赖）
- **工作端 vs C 端分离策略**：
  - `pages.json` 主包放 C 端公开页（登录/扫码溯源/公开查询，≤ 2MB）
  - `subPackages` 按角色分组放工作端页（果农 / 合作社 / 监管 / 冷链司机 / 金融 / 管理员）
  - 条件编译 `#ifdef MP-WEIXIN` 在小程序内屏蔽部分工作端重模块（超 2MB 限制）
- Build 产物：
  - `yarn build:h5-full` → `dist/build/h5-full/`（工作端 H5，裸域部署）
  - `yarn build:h5-lite` → `dist/build/h5-lite/`（C 端 H5，公开入口域部署）
  - `yarn build:mp-weixin` → `dist/build/mp-weixin/`（微信开发者工具导入上传）
  - `yarn build:app` → uni 原生（可选，按业务决定启动与否）

### 13.4 旧代码迁移成本（apple-web-ui 65 页 + apple-h5-ui 7 页 + mobile-ui/full 63 页，实测 2026-04-17）

**口径说明**（原版本未明口径，此处按 LOC 统一，含 template + script + style 三段）：

| 项 | 保留率 | 说明（修正后） |
|----|--------|------|
| `<script setup>` 业务逻辑（Pinia store / API 调用 / 校验 / computed / watch） | **80-90% 保留** | shared-core API 不变、Pinia 不变；仅 `onMounted→onShow/onLoad`、`useRoute→onLoad(options)`、`useRouter→uni.navigateTo` 需翻译（约占 script 段 5-10%） |
| `<template>` 组件层 | **40-60% 改动** | 70-80% props 同名可自动替换；Form 校验规则 / Uploader / Area / VideoPlayer / ImagePreview / Sticky 需 slot 重构 |
| `<template>` HTML 标签（div/span/img/a） | **100% 机械替换** | codemod（AST）脚本自动替换 → `view/text/image/navigator`；约 30 秒/页人工 review |
| 路由 / 导航 | **100% 重写** | vue-router → pages.json（subPackages 按角色分组） |
| 生命周期 | **85-90% 保留** | Vue 3 setup/watch/computed/ref 完全保留；仅路由钩子 `onBeforeRouteLeave→onUnload` 需改 |
| CSS 结构 | **80% 保留** | Flex/Grid 布局不动 |
| CSS 单位 | 通过 **tokens 层集中转换** | `px → rpx` 不是逐行改，而是在 `tokens.uni.ts` 内按平台输出不同单位，业务代码用 `var(--space-4)` 不动 |

**Vant → uView Plus 组件迁移量化**（基于 web-ui 中 Vant 引用 3262 处 / 67 文件的实测分析）：

| 类别 | 占比 | 操作 | 估时 |
|---|---|---|---|
| 简单组件替换（Button / Cell / Field / Tabs / Popup / Toast / Search / NavBar）| **60%** | codemod 批量替换前缀 + 微调 props | 1-2 人日可处理 1800+ 处 |
| Slot 结构重写（Form / List / Tabbar / Swipe / Collapse / Calendar / DatetimePicker）| **25%** | 人工 review 每页 | 15 min/页 × 65 页 ≈ **2 人周** |
| 深度重写（Uploader / Area / VideoPlayer / ImagePreview / Sticky） | **15%** | uView Plus 无对标 / API 差异巨大，需自建 | **3-5 人周**（先做 PoC） |

**Vant → uView Plus 总工时：5-7 人周**（不是"100% 重写"那种 10+ 人周叙事）。

**单页工时参考**（给 PM 算账用）：

| 页类型 | 典型 LOC | 工时 | 举例 |
|---|---|---|---|
| 简单表单/展示页 | < 200 | **0.5 人日** | 登录、个人信息、公告详情 |
| 中等页（列表+分页+弹窗）| 200-400 | **1.0-1.5 人日** | 订单列表、果园列表、农事记录 |
| 复杂页（图表/上传/地图/扫码）| > 400 | **2-3 人日** | 冷链温度曲线、溯源链路图、贷款申请 |
| 首批 P0 踩坑附加 | — | **+50%** | 阶段 2 前 5-6 页带踩坑 buffer |
| 6 角色菜单/权限/密度适配 | — | **3-5 人日** 一次性 | 阶段 4 末 |

**总量估算**：65 页迁移 + 5-7 人周 UI 库换装 + 60-90 人日业务迁移 ≈ **3 人团队 4-6 周**（不含踩坑、测试、合规）。

**决策：采用方案 C 渐进迁移**（legacy-full 分支备份 + 新 uni-app 按 P0/P1/P2 顺序迁入，存量双轨维护过渡期）。

**迁移顺序**（按业务价值）：
1. P0 — 登录 / 溯源扫码 / 个人中心 / 订单核心
2. P1 — 果园 / 农事记录 / 冷链温度 / 贷款申请
3. P2 — 异常上报 / 统计图表 / 完整角色菜单

### 13.5 工作端 / C 端分化 + 小程序分包策略

**小程序 2MB 主包硬限**（已量化）：
- 每页 uni-app 编译后 **15-25 KB gzip**；每组件 5-10 KB
- uView Plus tree-shake 后核心库 **300-400 KB**
- Pinia + 业务 store **50-80 KB**；shared-core 全量 **100-150 KB**
- → **主包 hard limit ≈ 15-25 个业务页**；工作端 80-100 页**必须分包**

**路由分组**（同一 uni-app 项目内）：
```
src/pages/            ← 主包（≤2MB）：登录/首页/扫码/公开查询/个人中心 = 5-6 页硬上限
src/pages-work/       ← 工作端 subPackages（63 页按角色分组）
  ├── farmer/         ← 果农
  ├── coop/           ← 合作社
  ├── regulator/      ← 监管
  ├── cold/           ← 冷链司机
  ├── finance/        ← 金融
  └── admin/          ← 管理员
```

**`pages.json` 真实分包配置**（不是 `%VITE_UI_MODE%` 这种伪造方案，uni-app 编译时静态解析 pages.json）：
```json
{
  "pages": [
    { "path": "pages/login/login" },
    { "path": "pages/index/index" },
    { "path": "pages/trace/scan" },
    { "path": "pages/trace/query" },
    { "path": "pages/profile/index" }
  ],
  "subPackages": [
    { "root": "pages-work/farmer",    "pages": ["orchard/list", "orchard/detail", "growth/record"] },
    { "root": "pages-work/coop",      "pages": [...] },
    { "root": "pages-work/regulator", "pages": [...] },
    { "root": "pages-work/cold",      "pages": [...] },
    { "root": "pages-work/finance",   "pages": [...], "independent": true },
    { "root": "pages-work/admin",     "pages": [...] }
  ],
  "preloadRule": {
    "pages/login/login": { "network": "wifi", "packages": ["pages-work/farmer"] }
  }
}
```

**构建分化**（正确方式）：
- `build:h5-full` → 全量打包所有 subPackages（工作端 + C 端）
- `build:h5-lite` → 通过 `define` / 条件编译 `#ifdef H5-LITE` 在运行时屏蔽 `pages-work/*` 路由，或用 **独立 manifest.json + pages.lite.json** 两套配置文件切换
- `build:mp-weixin` → 保留 subPackages，**金融独立分包**（`independent: true`，避审核连坐）

**小程序分包硬约束**（§13.5 原稿遗漏）：
- ❌ **分包之间不能相互 import** → 公共组件必须放 `src/components/` 进主包，占主包配额
- ❌ **preloadRule 是编译时静态配置**，不能按 RBAC 动态预加载（只能在 login 成功后用 `wx.preloadSubpackage` 手动触发）
- ✅ **独立分包（`independent: true`）**：C 端扫码入口适合，不需先下载主包；但**不能 import 主包代码**，需独立维护共享逻辑

**主包超限降级顺序**：
1. 图片/icon 改 iconfont（省 100-200 KB）
2. 公共 util 按需 import（`rollup-plugin-visualizer` / `vite-bundle-analyzer` 检查）
3. 非高频工作端分包（金融/管理员）改独立分包
4. **重模块（图表/富文本/地图/大数据可视化）跳 H5** — 小程序 webview 嵌入 `build:h5-full` 产物
5. 兜底：小程序做"轻量工作端"，重功能仅 H5

**条件编译**（运行时）：
```vue
<!-- #ifdef H5 -->
<!-- H5 专用：文件上传 <input type=file> -->
<!-- #endif -->
<!-- #ifdef MP-WEIXIN -->
<!-- 小程序专用：uni.chooseImage + uni.uploadFile + wx.login -->
<!-- #endif -->
```

### 13.6 迁移时序（修正版 2026-04-17，从 10-12 周 → 16-18 周）

> 原版本时序**严重低估阶段 0** + 遗漏测试/CI/灰度/审核 4 条支线。以下为 3 Agent 审查后的修正版。

| 阶段 | **周期** | 动作 | 产出 / 验收 |
|------|------|------|------|
| **阶段 -1（新增）Spike 预研** | **1 周** | ① 硬伤组件 PoC：Uploader / Area / VideoPlayer 在 uView Plus 下是否可行<br>② HttpClient 抽象层设计评审（接口 + 适配器骨架）<br>③ 小程序合规预审：appid 类目 / 金融资质 / 隐私协议<br>④ 团队 uni-app 熟手度评估 + 必要培训 | 3 个 PoC 页 + HttpClient 接口评审通过 + 合规决策 Go/NoGo |
| **阶段 0a** shared-core transport 抽象化 | **1.5 周** | 新增 `src/http/` → HttpClient 接口 + axios-client + uni-client + response-pipeline 业务逻辑上移 + request.ts delegating wrapper | apple-admin-ui 契约回归测试 100% 通过（阻断项） |
| **阶段 0b** Types 补齐 | **0.5 周**（与 0a 并行） | 补 9 个 types 模块（coldchain/warehouse/finance/input/iot/bigdata/trade/farm/cultivation），对齐 `contract/modules/*.md` | shared-core types 16/16，TypeScript 编译通过 |
| **阶段 1** 骨架 | **1.5 周** | apple-mobile-ui 改造 uni-app（含 legacy 分支备份）+ uView Plus 3.3.x 接入 + tokens.uni.ts + **多端 CI 流水线首通**（`build:h5-full/h5-lite/mp-weixin` + miniprogram-ci 上传）+ **小程序登录联调**（后端新增 `/auth/miniapp/login` + UnionID） | Hello World 三端产物 + wx.login 能跑通 |
| **阶段 2** P0 闭环 | **2.5-3 周** | 5-6 页（登录 + 溯源扫码 + 个人中心 + 首页导航）+ 隐私协议弹窗 + 手机号授权 + 内容安全接入 + 首批踩坑 + **E2E 自动化框架搭建** | 三端同步跑通 P0；单页测试覆盖率 ≥ 60% |
| **阶段 3** P1 闭环 | **4 周** | 20-25 页（果园 / 农事 / 订单 / 冷链 / 贷款 / 异常）+ subPackages 分包验证 + **阶段末提交测试版审核**（启动小程序审核周期） | 工作端 H5 + 小程序 P1 上线候选 |
| **阶段 4** P2 + 6 角色适配 | **4-5 周** | 30+ 页 P2 + 6 角色密度（rpx + CSS 变量验证）+ 权限菜单 + 金融模块降级处理（若无持牌资质，改 webview 跳 H5） | 全量替换 apple-web-ui 功能覆盖 |
| **阶段 4.5（新增）** 灰度发布 | **1 周** | ① 灰度策略：按域名 / 按角色 / 按比例<br>② 回退预案：legacy-full 分支 1 小时内可切回<br>③ 小程序审核缓冲（首次 1-3 天 + 可能打回 + 金融类目 1-4 周） | 生产灰度 10% → 50% → 100% 切换完成 |
| **阶段 5** 归档 | 常态化 | apple-web-ui / apple-h5-ui 标 deprecated（README 指向 apple-mobile-ui）+ `apple-miniapp` 目录确认移除 | 遗留清零 |

**总周期**：**16-18 周**（2.5-3 人团队并行，含合规缓冲和审核周期）

**人力分工建议**（2.5-3 人）：
- **后端 0.5 人**：阶段 1 新增 `/auth/miniapp/login` + `user_wechat` 表；阶段 2-4 应对前端联调
- **前端 Leader 1 人**：阶段 -1 ~ 阶段 1 攻坚 shared-core + 骨架；阶段 2-4 架构保障
- **前端迁移 1-1.5 人**：阶段 2-4 页面迁移 + uView Plus 适配
- **QA 0.25 人**（阶段 2 起半职）：E2E 自动化 + 三端回归

**回退触发条件**（任一满足 → 回到阶段 -1 重新评估）：
- 阶段 0 末 apple-admin-ui 契约测试不通过
- 阶段 1 末三端 Hello World 任一产出失败
- 阶段 2 末 P0 未达 5 页
- 小程序审核连续打回 3 次（考虑放弃小程序原生方案，改 H5 + webview）

### 13.7 风险清单（已扩充 11 项，2026-04-17 审查）

| # | 风险 | 优先级 | 影响 | 缓解 | 回退动作 |
|---|------|---|------|------|---|
| R1 | **shared-core transport 改造连坐 apple-admin-ui（PC 端）破坏** | **P0** | PC 端 + 移动端 + 小程序**三端同炸** | 阶段 0a 末强制 apple-admin-ui 契约回归测试 100% 通过；保留 axios-client 作为默认兜底 | 回滚 shared-core 版本，legacy-full 分支持续维护 |
| R2 | **apple-mobile-ui 现有 63 页沉没成本** | **P0** | 2-4 人周已投入代码作废 | §13.3 选项 C（git tag + legacy-full 分支备份） | 选 B（lite 5 页独立为 h5-lite）|
| R3 | **微信小程序金融类目审核** | **P0** | 贷款/授信需持牌资质，不具备则**金融模块小程序审核必拒** | 阶段 -1 合规预审确认资质 | 金融页仅 H5，小程序 `webview` 打开 H5 页 |
| R4 | **小程序登录 wx.login / unionid 后端未就绪** | **P0** | 阶段 2 阻断 | 阶段 1 与 0 并行：后端新增 `/auth/miniapp/login` + `user_wechat` 表 + Nacos 配 appid/secret | 账号密码登录兜底（小程序内输入手机号 + 短信） |
| R5 | **uView Plus 硬伤组件（Uploader/Area/VideoPlayer/Sticky）** | P1 | Vant 功能降级 | 阶段 -1 Spike 3 个组件 PoC；缺失组件自建 + uni-uploader 封装层 | 重模块页改 webview 跳 H5 |
| R6 | **小程序 2MB 主包限制** | P1 | 工作端 80+ 页塞不下 | subPackages（6 个角色）+ 金融独立分包 + 分包预加载 + iconfont | 重模块仅 H5 |
| R7 | **分包之间不能相互 import** | P1 | 公共组件占主包配额 | 公共组件库统一放 `src/components/` + tree-shake | 部分组件下沉复制到各分包 |
| R8 | **DESIGN.md 6 角色 × 3 端 × 3 密度 = 54 视觉矩阵** | P1 | 回归爆炸，UI bug 激增 | tokens.uni.ts 条件编译 + 每阶段末视觉回归；小程序组件 `styleIsolation: 'shared'` | 密度系统在小程序端降级（仅 comfortable 一档） |
| R9 | **H5 PC 宽屏下 rpx 失效** | P1 | 工作端 H5 桌面浏览器打开布局崩溃 | `body { max-width: 750px; margin: 0 auto; }` 或 tokens.uni.ts 在 H5 下输出 px | 工作端 H5 强制移动端浏览器打开（UA 检测） |
| R10 | **团队 uni-app 熟手数量** | P1 | 零经验 = 产出打 0.5 折，时序失真 100%+ | 阶段 -1 评估 + 1-2 周 Spike 培训；引入外部顾问 | 延长总周期 + 减少 P2 范围 |
| R11 | **小程序合规（隐私协议/手机号授权/内容安全）** | P1 | 审核被拒 | 阶段 1 补 privacy.json + 阶段 2 接入 `security.imgSecCheck / msgSecCheck` | 非必需内容延后 P2 |
| R12 | **HTML 标签全量改写 + codemod 脚本缺陷** | P2 | AST 替换只能处理 40-60% 机械场景，剩下靠人工 | 阶段 1 沉淀 codemod 基础版；阶段 2 按页人工 review | 按页估时加 15 min/页 buffer |
| R13 | **uni-app 版本碎片化**（HBuilderX 与 CLI 不同步、vue3 分支周更 break change） | P2 | 升级后踩坑 | 锁定 `@dcloudio/uni-app` 3.0.0-alpha-xxx 固定版本；升级必跑回归 | 锁旧版本维持到 P1 稳定 |
| R14 | **前端字段映射与契约铁律冲突** | P2 | runtime 字段错误（CLAUDE.md 04-13 复盘同源） | uni-app 也必须遵守 `contract/modules/*.md` 铁律；子 Agent prompt 强制 IRON RULE | 回退到上一稳定 commit |
| R15 | **多端 CI 流水线搭建工作量被忽略** | P2 | 阶段 2 开始每次手工打三份 | 阶段 1 末必须就绪（`build:h5-full/h5-lite/mp-weixin` + miniprogram-ci） | 临时脚本化 + 人工上传 |

### 13.8 完成标准（扩充 2026-04-17）

**阶段 -1 Spike 完成标准**
- [ ] Uploader / Area / VideoPlayer 3 个 PoC 页可跑通 H5 + 小程序
- [ ] HttpClient 接口定义 + 适配器骨架评审通过（不是"等实现"占位符）
- [ ] 合规决策：金融类目资质 Go / NoGo；appid 主体类目确认
- [ ] 团队 uni-app 熟手 ≥ 1 人（否则补培训）

**阶段 0 shared-core 完成标准**
- [ ] `apple-shared-core/src/http/` 目录存在，含 HttpClient.ts 接口 + axios-client.ts + uni-client.ts + response-pipeline.ts
- [ ] `setHttpClient()` / `getHttpClient()` / `createUniClient()` 均已实现
- [ ] request.ts 重构为 delegating wrapper，19 API 模块 import 路径**不变**
- [ ] axios-client 单测覆盖率 ≥ 80%，uni-client 覆盖率 ≥ 70%
- [ ] **apple-admin-ui 契约回归测试 100% 通过**（阻断项）
- [ ] shared-core types 16/16 模块（补齐 9 个）

**阶段 1 骨架完成标准**
- [ ] `git tag mobile-ui-pre-uniapp-2026-04-17` + `legacy-full` 分支已创建
- [ ] `yarn build:h5-full / build:h5-lite / build:mp-weixin` 三端产物均可出（Hello World 级别）
- [ ] uView Plus 3.3.x 版本锁定，`styleIsolation: 'shared'` 验证 CSS 变量继承
- [ ] `src/design/tokens.uni.ts` 双模式（H5 px / 小程序 rpx）验证通过
- [ ] 多端 CI 流水线（GitHub Actions / 内部 Jenkins）产物归档 + 版本号管理 + `miniprogram-ci` 自动上传
- [ ] 后端 `/auth/miniapp/login` + `user_wechat` 表上线，appid/secret 配置在 Nacos
- [ ] **wx.login 在小程序 Hello World 页能跑通**（获取 openid/unionid，签发 JWT）

**阶段 2 P0 完成标准**
- [ ] 5-6 个 P0 页（登录 / 扫码 / 个人中心 / 首页导航）三端同步
- [ ] 隐私协议弹窗 + privacy.json 配置完整
- [ ] `security.imgSecCheck` / `security.msgSecCheck` 内容安全接入
- [ ] 手机号授权 `<button open-type="getPhoneNumber">` 可用
- [ ] E2E 自动化框架（playwright / uni-automator）搭建完成
- [ ] 三端 E2E 覆盖 P0 核心流程

**阶段 3 P1 完成标准**
- [ ] 20-25 个 P1 页（果园 / 农事 / 订单 / 冷链 / 贷款 / 异常）三端同步
- [ ] subPackages 分包验证：主包 ≤ 1.5MB（留 0.5MB buffer），各分包 ≤ 2MB
- [ ] 小程序测试版提交审核（启动审核周期）
- [ ] 金融模块若无持牌资质，已降级为 webview 跳 H5

**阶段 4 + 4.5 完成标准**
- [ ] 30+ P2 页 + 6 角色菜单/权限/密度适配全量
- [ ] 生产灰度 10% → 50% → 100% 切换完成，监控 P99 延迟/错误率无回退
- [ ] 小程序审核通过，正式发布
- [ ] legacy-full 分支保留至少 3 个月（回退保险）

**阶段 5 归档标准**
- [ ] apple-web-ui / apple-h5-ui 标 deprecated（README 指向 apple-mobile-ui）
- [ ] apple-miniapp 目录确认不存在
- [ ] TODO.md §十三 标记为已完成，归档到 `docs/archive/`
