# Admin UI → shared-core 迁移清单

> Phase 4 准备产物 — 本文档为迁移设计参考，未实际删除或改动任何 API 调用。
> 生成时间：2026-04-15

## 一、现状概览

- 前端：`apple-admin-ui`（Vue 3 + Element Plus + Vite 5，纯 JS 项目，无 tsconfig.json）
- HTTP 层：`src/api/request.js` — 自建 axios 拦截器（请求注入 Bearer、响应解包 R 信封、401 自动跳 `/login`、ElMessage 提示）
- API 模块：`src/api/*.js` 共 13 个文件（含 request.js），总计 **12 个业务模块**、**58 个 xxxApi 对象**、**~312 个具名方法**
- 目标：后续迁移到 `@apple/shared-core`，通过 `configureApi({ getToken, onUnauthorized, onError, baseURL, timeout })` 注入 Vue/Element 侧效应

## 二、文件对照表

| 现有文件 | 主要导出对象 | 目标 shared-core 模块 | 覆盖度 | 备注 |
|---------|-------------|----------------------|--------|------|
| src/api/auth.js | authApi（4 个方法：login, logout, getUserInfo, updatePassword） | `@apple/shared-core` → `api/auth.ts`（login, logout, getCurrentUser） | **部分** | shared-core 缺 updatePassword；getCurrentUser ≠ getUserInfo，需核对返回类型 |
| src/api/planting.js | orchardApi, farmerApi, cultivationApi, taskPlanApi, growthRecordApi, harvestApi, maturityApi, plantingAnalysisApi（43 方法） | `api/planting.ts`（findOrchards, getOrchard, findGrowthRecords, getGrowthRecord, uploadGrowthRecord, updateGrowthRecord, deleteGrowthRecord） | **仅 Orchard + GrowthRecord** | shared-core 当前只覆盖 H5 移动端读取场景；farmer/cultivation/taskPlan/harvest/maturity/plantingAnalysis 全部**缺失**，需新增 |
| src/api/trace.js | traceApi, batchApi, codeApi, anomalyApi（21 方法） | `api/trace.ts`（findTraceByCode, findTraceFullChain, scanBatch, getTraceBatch） | **薄层** | 只覆盖 H5 扫码场景；admin 端 list/CRUD/anomaly 全部缺失 |
| src/api/coldchain.js | vehicleApi, taskApi, temperatureApi, precoolApi, logisticsApi, deliveryApi, coldchainStatsApi（32 方法） | `api/logistics.ts`（findDeliveries, getDelivery, acknowledgeDelivery） | **极少** | 命名分歧：admin 的 `coldchain/*` vs shared-core 的 `logistics` 模块；物理域重合但路由/字段需对齐 |
| src/api/input.js | supplierApi, agriProductApi, purchaseApi, inventoryApi, usageApi, inputTraceApi（39 方法） | 无 | **缺失** | 需在 shared-core 新建 `api/input.ts` 模块 |
| src/api/trade.js | supplyApi, needApi, matchApi, orderApi, inspectionApi, tradeStatsApi（31 方法） | 无 | **缺失** | 需新建 `api/trade.ts` |
| src/api/warehouse.js | warehouseApi, receiptApi, recordApi, warehouseStatsApi（20 方法） | 无 | **缺失** | 需新建 `api/warehouse.ts` |
| src/api/finance.js | loanApi, creditApi, pledgeApi, riskApi, financeStatsApi（32 方法） | 无 | **缺失** | 需新建 `api/finance.ts` |
| src/api/risk.js | riskRuleApi, riskEventApi（9 方法） | 无 | **缺失** | 注意与 finance.js 的 riskApi 区分（risk.js 是风控规则/事件，finance 的 riskApi 是贷前风险评估） |
| src/api/bigdata.js | dashboardApi, assetApi, metricApi, lineageApi, dqApi, jobApi, sourceApi, reportApi, screenApi, openApi, auditApi（70 方法） | 无 | **缺失** | 体量最大；多数 admin 专用，H5 端未必需要 |
| src/api/stats.js | statsApi（4 方法） | 无 | **缺失** | 简单聚合接口 |
| src/api/wxpay.js | wxPayApi, wxRefundApi, wxReconcileApi（7 方法） | 无 | **缺失** | 微信支付 Mock；admin 管理端+H5 都可能用 |
| src/api/request.js | default export（axios 实例）+ downloadFile 工具 | `api/request.ts`（httpGet/httpPost/httpPut/httpDelete + rawClient + configureApi） | **对应但接口不同** | 迁移后 `request.js` 整体删除；`downloadFile` 工具函数需单独保留或抽到 `src/utils/download.ts`（shared-core 无对应实现） |

## 三、调用点统计

- 受影响 `.vue` 文件数：**55**
- 受影响 `.js/.ts` 文件数：**1**（`src/router` 或 `src/store` 内某个引用）
- 总 `import ... from '@/api/xxx'` 语句数：**58**
- 业务模块 xxxApi 对象：**58 个**
- 业务方法总数（xxxApi 对象下的命名方法）：**约 312**

## 四、配套组件差异

| 维度 | apple-admin-ui 现状 | apple-shared-core 目标 |
|------|--------------------|------------------------|
| Token 存储键 | `localStorage.getItem('token')` + `localStorage.getItem('user')` | 由 UI 侧通过 `getToken` 回调决定；admin 继续用 `token` 键即可 |
| 401 处理 | `window.location.href = '/login'`（硬跳） | `onUnauthorized()` 回调；建议 admin 改用 `vue-router` 软跳 `/login` |
| 错误提示 | `ElMessage.error(msg)` | `onError(msg)` 回调；admin 接 `ElMessage` |
| 响应解包 | `res.code === 200 ? res.data : reject` | shared-core 一致（R 信封），行为等价 |
| 空参剥离 | 请求拦截器剥 `'' / null / undefined` params | shared-core **未实现**，需要新增（重要：admin 多个列表页依赖此行为避免后端匹配错误） |
| downloadFile | `src/api/request.js` 导出的 fetch-based 工具 | shared-core 未提供；迁移后建议抽到 admin 本地 `src/utils/download.js` |

## 五、风险点

1. **空参剥离缺失（P0）**
   `src/api/request.js:14-20` 会把 `params` 中值为空串/null/undefined 的键整体删掉，这是 admin 大量列表筛选页的隐式契约。shared-core 的 `request.ts` **没有**做这件事。直接切换会导致所有筛选条件"未选"的字段被发成 `?status=` 或 `?status=null`，后端匹配失败。
   → **行动**：shared-core 侧增加同样的 request 拦截器，或在 admin 的 API 包装层本地处理。

2. **401 跳转方式差异（P1）**
   现状硬跳 `window.location.href`；shared-core 只给回调。迁移后用 `router.push('/login')` 软跳，要确保 Pinia store 中的 user 状态同步清理。

3. **downloadFile 丢失（P1）**
   `request.js` 同时导出的 `downloadFile` 被导出类/报表页使用。迁移时不能随 `request.js` 一并删除，需抽出到独立模块。

4. **模块覆盖率低（P0）**
   shared-core 当前只覆盖 auth / planting 部分 / trace 部分 / logistics 部分。**12 个业务模块里 10 个完全缺失**。一次性切换不可行；应按模块分批：**auth → planting（补齐缺失子域）→ trace → 其他**。

5. **命名域分歧（P1）**
   admin 的 `coldchain` 在 shared-core 叫 `logistics`。若按 shared-core 命名反哺会造成 admin 端 import 全量改写。建议：shared-core 保留 `logistics`，admin 侧保留原有 `coldchain` 外层 API 文件名，内部从 shared-core re-export。

6. **字段大小写 / 枚举值（P0 — 参考 feedback_frontend_field_mapping_definitive）**
   admin 每个 API 模块配套 `contract/modules/*.md` 字段。迁移前**必须**逐字段比对 shared-core types/*.ts 与 admin 侧实际消费的字段名，避免重演"plateNo vs plateNumber"四次修复事故。

7. **TS 支持（P2）**
   admin 无 tsconfig.json，但 Vite 5 + esbuild 可直接处理 `.ts`。新增 `src/plugins/shared-core.ts` 可正常编译；若后续需要 IDE 类型提示，可按需补 `tsconfig.json`。

## 六、建议迁移顺序（非本次实施）

1. **Phase 4a**：shared-core 补齐 planting / trace / coldchain(logistics) 的完整 admin 方法 + 空参剥离拦截器
2. **Phase 4b**：shared-core 新增 input / trade / warehouse / finance / risk / bigdata / stats / wxpay 模块（按业务优先级分批）
3. **Phase 4c**：admin 开启 `VITE_USE_SHARED_CORE=true`，按模块逐个把 `src/api/xxx.js` 改为从 `@apple/shared-core` re-export
4. **Phase 4d**：验证无回归后删除 `src/api/request.js`，将 `downloadFile` 迁到 `src/utils/download.js`

## 七、本次准备物

- `src/plugins/shared-core.ts` — 注入器骨架（默认未激活）
- `src/main.js` — 新增 `setupSharedCore()` 调用（环境变量未开启时 no-op）
- `package.json` — 暂**未**新增 `@apple/shared-core` 依赖（留给用户审核，见报告）
