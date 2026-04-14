# 苹果产业链平台 - API 连调核对报告

> 生成时间: 2026-04-11 | 核对范围: 8 模块 | 数据来源: API文档 ↔ 后端Controller ↔ 前端调用

## 一、总览

| 模块 | 文档API | 后端实现 | apple-web-ui | frontend | 状态 |
|------|---------|---------|-------------|----------|------|
| M1 种植管理 | 48 | 48 | 46 | 8 | ⚠️ 路径不一致+14个API未接入 |
| M2 农资投入 | 35 | 35 | 35 | 35 | ✅ 完全对齐 |
| M3 追溯管理 | 33 | 17 | 15 | - | ❌ 批次Controller缺失+10 API未接入 |
| M4 交易管理 | 40 | 41 | 16 | - | ❌ 双版本冲突+24 API未接入 |
| M5 仓储管理 | 23 | 23 | 20 | 22 | ⚠️ 统计模块apple-web-ui缺失 |
| M6 冷链物流 | 31 | 30 | 17 | 13 | ❌ frontend路径错误严重 |
| M7 金融服务 | 25 | 27 | 16 | 19 | ❌ HTTP方法+路径错误 |
| M8 大数据 | 75 | 75 | 11 | - | ❌ 85%未接入前端 |
| **合计** | **310** | **296** | **~176** | **~97** | |

**说明**: 两套前端的 axios baseURL 均为 `/api`，前端调用 `/finance/loans/list` 实际请求 `/api/finance/loans/list`，因此"缺少 /api 前缀"不是问题。但显式写了 `/api/xxx` 的调用会导致双重前缀 404。

---

## 二、关键发现

### 2.1 全局性问题

| # | 问题 | 影响范围 | 严重度 |
|---|------|---------|--------|
| G1 | **两套前端API覆盖度差异大** — apple-web-ui 和 frontend 实现不一致 | 全局 | 高 |
| G2 | **大量后端API无前端调用** — 310个API中约130个前端未集成 | M3/M4/M7/M8 | 高 |
| G3 | **数据库表未在远程MySQL建表** — 项目用本地MySQL(apple_chain)或H2 | 全局 | 中 |
| G4 | **frontend项目多处路径错误** — 部分路径与后端不匹配 | M4/M6/M7 | 高 |

### 2.2 双重 /api 前缀风险

前端 `baseURL: '/api'` + 代码中显式写 `/api/xxx` → 实际请求 `/api/api/xxx` → **404**

需排查的文件:
- `apple-web-ui/src/api/farm.js` 中 `/api/farm/orchards/export`
- `apple-web-ui/src/api/cultivation.js` 中 `/api/cultivation/batches/export`
- `apple-web-ui/src/api/farm.js` 中 `/api/farm/farmers/export`

---

## 三、按模块详情

### M1 种植管理 — ⚠️ 路径不一致 + 功能未接入

**后端实现**: 48/48 ✅ (100%)

**问题清单**:

| # | 问题 | 详情 | 文件位置 |
|---|------|------|---------|
| 1 | 果园路径不一致 | apple-web-ui用`/farm/orchards`，后端是`/planting/orchard` | apple-web-ui/src/api/farm.js |
| 2 | frontend路径更不一致 | frontend用`/orchards`，缺模块前缀 | frontend/src/api/orchard.js |
| 3 | 任务计划前端未接入 | 4个API(task-plan)后端已实现但前端未调用 | - |
| 4 | 种植分析前端未接入 | 4个API(analysis)后端已实现但前端未调用 | - |
| 5 | 成熟度管理前端部分接入 | 4个API中部分已在apple-web-ui中调用 | - |

### M2 农资投入 — ✅ 完全对齐

**后端实现**: 35/35 ✅ | **前端调用**: 35/35 ✅

所有API文档、后端Controller、前端调用完全对齐。**唯一零问题模块**。

### M3 追溯管理 — ❌ 批次Controller缺失

**后端实现**: 17/33 (52%) — **16个API后端未实现**

**严重问题**:

| # | 问题 | 影响 |
|---|------|------|
| 1 | **TraceBatchController完全缺失** | 9个批次管理API后端未实现，前端已在调用→必404 |
| 2 | **异常追溯前端未集成** | 6个API后端已实现但前端未调用 |
| 3 | **区块链存证前端未集成** | 4个API后端已实现但前端未调用 |

**缺失的后端Controller**: TraceBatchController（批次管理）

### M4 交易管理 — ❌ 双版本冲突

**后端实现**: 41/40 (102% — 多了一个MVP版本)

**严重问题**:

| # | 问题 | 详情 |
|---|------|------|
| 1 | **双Controller版本** | 标准`TradeOrderController`(/trade/order/*) + MVP`TradeOrderMvpController`(/trade/orders/*)，前端用MVP版 |
| 2 | **frontend路径错误** | 用`/trades`而非`/trade` |
| 3 | **质检功能前端未接入** | 6个API |
| 4 | **撮合议价前端未接入** | 9个API(含聊天) |
| 5 | **合同支付开票前端未接入** | 5个API |
| 6 | **交易统计前端未接入** | 4个API |

### M5 仓储管理 — ⚠️ 统计缺失

**后端实现**: 23/23 ✅ (100%)

| 问题 | apple-web-ui | frontend |
|------|-------------|----------|
| 统计模块(3个API) | ❌ 缺失 | ✅ 已实现 |
| 仓库状态变更 | ❌ 缺失 | ✅ 已实现 |
| 仓库预警列表 | ❌ 缺失 | ❌ 缺失 |
| 仓单状态参数 | ❌ 传参方式有误 | ✅ 正确 |

### M6 冷链物流 — ❌ frontend路径错误严重

**后端实现**: 30/31 (97%)

**frontend项目路径错误**:

| 错误路径 | 正确路径 |
|---------|---------|
| `/coldchain/transports/*` | `/coldchain/tasks/*` |
| `/coldchain/precooling/*` | `/coldchain/precool/*` |
| `/statistics/transport-trend` | 不存在 |

**apple-web-ui缺失**: 预冷任务API、统计API、物流查询API

### M7 金融服务 — ❌ HTTP方法+路径错误

**后端实现**: 27/25 (108% — 含额外接口)

**frontend项目问题**:

| # | 问题 | 详情 |
|---|------|------|
| 1 | HTTP方法错误 | approve/reject/disburse/repay/settle 用PUT，后端期望POST |
| 2 | 路径错误 | `credit-ratings` 应为 `credits` |
| 3 | 调用不存在的接口 | `/resolve`, `/escalate`, `/trend` |
| 4 | 质押操作方法不匹配 | activate/release/default 文档POST vs 后端PUT |

### M8 大数据 — ❌ 85%未接入前端

**后端实现**: 75/75 ✅ (100%) | **前端调用**: ~11/75 (15%)

仅 Dashboard 和 Statistics API 在前端有调用。以下全部未接入:
- 数据资产管理(8 API)
- 数据源管理(6 API)
- 数据质量规则(7 API)
- 采集任务(7 API)
- 数据血缘(5 API)
- 指标中心(7 API)
- 开放API管理(5 API)
- 运维监控(2 API)
- 大屏配置(8 API)
- 分析报告(4 API)

---

## 四、数据库状态

| 项目 | 配置 |
|------|------|
| 数据库 | `apple_chain` (本地MySQL 127.0.0.1:3306) |
| 建表方式 | Flyway V1-V21 自动迁移 |
| 测试DB | H2内存数据库 |
| 远程MySQL(aiin_dev_db01) | 无苹果平台业务表 |

**结论**: 数据库验证需本地启动服务后执行。

---

## 五、修复优先级建议

### P0 — 必须立即修复（会导致页面报错404）

| # | 模块 | 修复项 | 工作量 |
|---|------|--------|-------|
| 1 | M3 | 实现 TraceBatchController（9个API） | 大 |
| 2 | M6 | 修正 frontend 项目冷链路径（transports→tasks, precooling→precool） | 小 |
| 3 | M4 | 统一订单API版本，删除MVP或标准版其中一个 | 中 |
| 4 | M1 | 修正果园路径（/farm/orchards → /planting/orchard） | 小 |
| 5 | M7 | 修正 frontend HTTP方法（PUT→POST for 状态变更） | 小 |
| 6 | 全局 | 排查双重 `/api` 前缀的调用 | 小 |

### P1 — 重要（功能缺失但不影响现有页面）

| # | 模块 | 修复项 |
|---|------|--------|
| 1 | M4 | 接入质检、撮合、合同、统计前端界面 |
| 2 | M7 | 修正 credit-ratings → credits 路径 |
| 3 | M5 | apple-web-ui 补充统计模块 |
| 4 | M8 | 逐步接入各管理功能前端 |

### P2 — 优化（后端已实现但前端未使用）

- M1: 任务计划、种植分析前端界面
- M3: 异常追溯、区块链存证前端界面
- M8: 数据资产、质量规则、采集任务等管理界面

---

## 六、自动化测试建议

### 测试策略

| 层级 | 工具 | 覆盖目标 | 优先执行 |
|------|------|---------|---------|
| 编译验证 | `mvn clean compile test-compile` | 后端编译无错误 | ✅ 立即 |
| 单元测试 | JUnit (已有46个) | Service层逻辑 | ✅ 立即 |
| API冒烟测试 | curl / HTTPie | 每个endpoint 200验证 | 本地启动后 |
| 关键流程E2E | Playwright | 种植→采收→追溯→交易 链路 | 前端修复后 |
| 数据一致性 | 自定义断言 | 前端展示=后端返回 | API冒烟后 |

### bmad-agent-qa 使用建议

1. **先修复 P0 问题**，再生成测试 — 否则测试全是红的
2. **逐模块生成**: `/bmad-agent-qa` 每次处理一个模块
3. **API测试先于E2E**: 先验证后端API可达性，再测前端页面
4. **测试报告格式**: 每模块输出 endpoint × HTTP状态码 矩阵

---

## 七、下一步行动

1. **立即**: 修复 P0 的 6 项问题（预计 2-3 小时）
2. **编译验证**: `mvn clean compile test-compile` 确保后端编译通过
3. **本地启动**: 启动服务，Flyway自动建表，插入种子数据
4. **API冒烟**: 对每个endpoint执行curl验证200
5. **前端修复**: 修正路径和方法错误
6. **自动化测试**: 调用 `/bmad-agent-qa` 逐模块生成测试
