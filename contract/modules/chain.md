# M2-C 区块链存证 (Chain) 模块契约

## 元信息

| 项目 | 值 |
|------|-----|
| 模块标识 | M2-C 区块链存证 |
| 物理归属 | `apple-module-trace`（`com.apple.chain.trace.controller.ChainController`） |
| 逻辑独立 | 是（业务上独立为区块链存证模块，无独立的 `apple-module-chain`） |
| URL 前缀 | `/api/trace/chain`（业务接口）、`/api/admin/chain`（管理接口） |
| 错误码段 | `400000 ~ 499999`（CONVENTIONS.md:136 权威） |
| 最近更新 | 2026-04-12 |
| 权威源 | 本文件 |
| 后端实现 | `apple-module-trace` 内 `ChainController` + `ChainSubmitService` |
| 数据表 | `tr_chain_record` |
| 关联契约 | `trace.md`（溯源码→上链入口）/ `warehouse.md`（仓储快照）/ `trade.md`（交易快照）/ `coldchain.md`（温度快照） |

> **路径权威性声明**：本文件是 `/api/trace/chain/*` 与 `/api/admin/chain/*` 路径的唯一权威源。`trace.md` 末尾"关联契约"章节指向本文件，两者不重复定义接口。

---

## 1. 接口索引（4 端点）

| # | 接口中文名 | URL | 方法 | 认证 | 权限 |
|---|-----------|-----|------|------|------|
| 1 | [提交业务快照上链](#21-提交业务快照上链) | `/api/trace/chain/submit` | POST | JWT | `trace:write` |
| 2 | [公开校验链上记录](#22-公开校验链上记录) | `/api/trace/chain/verify/{traceCode}` | GET | **公开** | — |
| 3 | [管理：上链记录列表](#23-管理上链记录列表) | `/api/admin/chain/records` | GET | JWT | `trace:read` |
| 4 | [管理：手动重试失败记录](#24-管理手动重试失败记录) | `/api/admin/chain/retry/{id}` | POST | JWT | `trace:write` |

---

## 2. 上链管理 ChainController

### 2.1 提交业务快照上链

**URL**：`POST /api/trace/chain/submit`
**认证**：需要 JWT Bearer
**权限**：`@RequirePerm("trace:write")`
**用途**：内部接口，将业务快照（JSON Map）提交上链队列。服务端自动规范化快照 JSON、计算 SHA-256 哈希后异步写入区块链（奥链）。
**错误码**：`400003` / `400004` / `400005` / `400010` / `400900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 ChainSubmitRequest

| 字段 | 类型 | 必填 | 校验 | 说明 | 示例 |
|------|------|------|------|------|------|
| `traceCode` | string | ✅ | `@NotBlank`，max 64 | 溯源码（三级：BATCH/BOX/FRUIT） | `"BATCH-20260412-001"` |
| `businessType` | string | ✅ | `@NotBlank`，max 32 | 业务类型，见 ChainBusinessType 枚举 | `"BATCH"` |
| `businessId` | number | ❌ | — | 业务记录主键（可选），追踪用 | `10086` |
| `snapshot` | object | ✅ | 非空 Map | 业务快照 JSON，服务端规范化后计算哈希；字段自由结构 | `{"batchCode":"B001","weight":500}` |

```typescript
export interface ChainSubmitRequest {
  traceCode: string;            // max 64
  businessType: string;         // 见 ChainBusinessType
  businessId?: number;          // 可选
  snapshot: Record<string, unknown>;  // 业务快照 JSON
}
```

#### 响应体 ChainRecord

服务端创建 `ChainRecord` 记录，初始状态为 `PENDING(0)`。快照字段以规范化后的 JSON 字符串持久化，响应时原样返回 string。

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 记录主键（雪花 ID） |
| `traceCode` | string | ✅ | 溯源码 |
| `businessType` | string | ✅ | 业务类型（原始字符串） |
| `businessId` | number \| null | ❌ | 业务记录主键 |
| `dataSnapshot` | string | ✅ | 规范化后的 JSON 快照字符串 |
| `dataHash` | string | ✅ | SHA-256 哈希 hex（64位小写） |
| `chainTxHash` | string \| null | ❌ | 链上交易哈希（上链前为 null） |
| `chainBlockHeight` | number \| null | ❌ | 区块高度（上链前为 null） |
| `chainStatus` | `EnumValue<number>` | ✅ | 上链状态，见 ChainRecordStatus |
| `errorMsg` | string \| null | ❌ | 失败原因（max 512 chars，成功为 null） |
| `retryCount` | number | ✅ | 已重试次数，初始 0 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface ChainRecord {
  id: number;
  traceCode: string;
  businessType: string;
  businessId: number | null;
  dataSnapshot: string;         // 规范化 JSON 字符串
  dataHash: string;             // SHA-256 hex，64位小写
  chainTxHash: string | null;   // 上链前为 null
  chainBlockHeight: number | null;
  chainStatus: EnumValue<number>;  // 见 ChainRecordStatus
  errorMsg: string | null;
  retryCount: number;
  createdAt: string;
  updatedAt: string;
}
```

#### 响应示例（成功，状态 PENDING）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "traceCode": "BATCH-20260412-001",
    "businessType": "BATCH",
    "businessId": 10086,
    "dataSnapshot": "{\"batchCode\":\"B001\",\"weight\":500}",
    "dataHash": "a3f5e2c1d9b47e3082f6a1c0e5d2b8f4a3f5e2c1d9b47e3082f6a1c0e5d2b8f4",
    "chainTxHash": null,
    "chainBlockHeight": null,
    "chainStatus": { "code": 0, "desc": "待上链" },
    "errorMsg": null,
    "retryCount": 0,
    "createdAt": "2026-04-12 10:00:00",
    "updatedAt": "2026-04-12 10:00:00"
  }
}
```

#### 响应示例（失败，快照为空）

```json
{
  "code": 400003,
  "message": "快照数据为空",
  "data": null
}
```

#### 业务规则

1. `snapshot` 为空 Map（`{}`）或 null → 拒绝，抛 `400003 CHAIN_SNAPSHOT_EMPTY`
2. 服务端对 snapshot 进行 key 排序规范化后序列化，再计算 SHA-256 哈希，保证相同业务数据生成相同哈希
3. 接口返回时记录处于 PENDING 状态，实际上链由异步服务（`ChainSubmitService`）完成
4. 同一个 `traceCode` 可多次提交（版本追溯），每次产生独立记录

#### 实现提示

- Mock 模式下（`@ConditionalOnProperty(name="chain.mock", havingValue="true")`），异步上链直接赋值假 `chainTxHash`，记录跳转为 `SUCCESS`
- 真实模式调用奥链 SDK 异步上传，回调更新 `chainTxHash` 与 `chainBlockHeight`

---

### 2.2 公开校验链上记录

**URL**：`GET /api/trace/chain/verify/{traceCode}`
**认证**：**无需认证**（公开接口，消费者/监管扫码验证用）
**用途**：根据溯源码查询所有上链记录，返回结构化验证结果；前端/小程序扫码后直接调用
**错误码**：`400002` / `400006` / `400009`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `traceCode` | string | ✅ | 溯源码（URL encode 后传入） | `BATCH-20260412-001` |

#### 响应体 ChainVerifyResponse

> **目标状态**：当前后端返回 `Map<String, Object>`，契约定义目标 DTO `ChainVerifyResponse`。前端按此接口处理，后端迁移计划见 §5.3。

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `traceCode` | string | ✅ | 溯源码 |
| `verified` | boolean | ✅ | 是否存在至少一条 SUCCESS 记录 |
| `recordCount` | number | ✅ | 关联上链记录总数（含 pending/failed） |
| `successCount` | number | ✅ | 成功上链记录数 |
| `records` | `ChainVerifyRecord[]` | ✅ | 上链记录列表（按 createdAt 降序） |
| `verifiedAt` | string | ✅ | 本次验证时间，`yyyy-MM-dd HH:mm:ss` |

`ChainVerifyRecord` 子对象：

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 记录 ID |
| `businessType` | string | ✅ | 业务类型 |
| `dataHash` | string | ✅ | SHA-256 哈希 hex |
| `chainTxHash` | string \| null | ❌ | 链上交易哈希 |
| `chainBlockHeight` | number \| null | ❌ | 区块高度 |
| `chainStatus` | `EnumValue<number>` | ✅ | 上链状态 |
| `createdAt` | string | ✅ | 提交时间 |

```typescript
export interface ChainVerifyRecord {
  id: number;
  businessType: string;
  dataHash: string;
  chainTxHash: string | null;
  chainBlockHeight: number | null;
  chainStatus: EnumValue<number>;
  createdAt: string;
}

export interface ChainVerifyResponse {
  traceCode: string;
  verified: boolean;
  recordCount: number;
  successCount: number;
  records: ChainVerifyRecord[];
  verifiedAt: string;
}
```

#### 响应示例（成功，已上链验证通过）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "traceCode": "BATCH-20260412-001",
    "verified": true,
    "recordCount": 2,
    "successCount": 1,
    "records": [
      {
        "id": 1001,
        "businessType": "BATCH",
        "dataHash": "a3f5e2c1d9b47e3082f6a1c0e5d2b8f4a3f5e2c1d9b47e3082f6a1c0e5d2b8f4",
        "chainTxHash": "0xabc123def456abc123def456abc123def456abc123def456abc123def456abc1",
        "chainBlockHeight": 8800123,
        "chainStatus": { "code": 1, "desc": "已上链" },
        "createdAt": "2026-04-12 10:00:00"
      },
      {
        "id": 1002,
        "businessType": "BOX",
        "dataHash": "b9e3d1a0c8f52e4173g7b2d1f6e3a9c5b9e3d1a0c8f52e4173g7b2d1f6e3a9c5",
        "chainTxHash": null,
        "chainBlockHeight": null,
        "chainStatus": { "code": 0, "desc": "待上链" },
        "createdAt": "2026-04-12 11:30:00"
      }
    ],
    "verifiedAt": "2026-04-12 14:00:00"
  }
}
```

#### 响应示例（失败，溯源码不存在）

```json
{
  "code": 400002,
  "message": "溯源码不存在",
  "data": null
}
```

#### 业务规则

1. `traceCode` 不存在任何上链记录 → 返回 `400002 CHAIN_TRACE_CODE_NOT_FOUND`
2. `verified = true` 当且仅当 `successCount >= 1`
3. `records` 按 `createdAt` 降序排列，不分页（全量返回）
4. 无需 JWT，任何客户端（小程序、H5 扫码）均可访问
5. Mock 模式下 `chainTxHash` 为 mock 值（如 `"mock_tx_hash_xxx"`），前端需能处理

---

### 2.3 管理：上链记录列表

**URL**：`GET /api/admin/chain/records`
**认证**：需要 JWT Bearer
**权限**：`@RequirePerm("trace:read")`
**用途**：管理后台查看全部上链记录，支持按状态过滤，有 limit 上限（无分页）
**错误码**：`400900`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `status` | number | ❌ | 过滤状态码（见 ChainRecordStatus），不传返回全部 | `2` |
| `limit` | number | ❌ | 最多返回条数，默认 100，最大 1000 | `100` |

```typescript
export interface ChainRecordsQuery {
  status?: number;    // 见 ChainRecordStatus code
  limit?: number;     // 默认 100，最大 1000
}
```

#### 响应体 `List<ChainRecord>`

返回 `ChainRecord` 列表（结构同 §2.1），按 `createdAt` 降序。

```typescript
// ApiResponse<ChainRecord[]>
export type ChainRecordsResponse = ApiResponse<ChainRecord[]>;
```

#### 响应示例（成功，过滤 status=2 失败记录）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 2005,
      "traceCode": "BATCH-20260411-099",
      "businessType": "BATCH",
      "businessId": 99,
      "dataSnapshot": "{\"batchCode\":\"B099\",\"weight\":300}",
      "dataHash": "c7d4e2a0b8f31e5264h8c3e2g7f4b0d6c7d4e2a0b8f31e5264h8c3e2g7f4b0d6",
      "chainTxHash": null,
      "chainBlockHeight": null,
      "chainStatus": { "code": 2, "desc": "失败" },
      "errorMsg": "奥链节点超时，连接拒绝",
      "retryCount": 3,
      "createdAt": "2026-04-11 22:00:00",
      "updatedAt": "2026-04-11 22:05:00"
    }
  ]
}
```

#### 响应示例（失败，权限不足）

```json
{
  "code": 403,
  "message": "权限不足",
  "data": null
}
```

#### 业务规则

1. `status` 不传时返回全部状态记录（PENDING + SUCCESS + FAILED + RETRYING）
2. `limit` 超过 1000 时服务端截断为 1000，不报错
3. 结果按 `createdAt` 降序排列
4. 此接口不分页，适用于管理运维场景（记录量通常有限）；若未来记录超万条，迁移至分页接口（届时版本升级）

---

### 2.4 管理：手动重试失败记录

**URL**：`POST /api/admin/chain/retry/{id}`
**认证**：需要 JWT Bearer
**权限**：`@RequirePerm("trace:write")`
**用途**：对 `chainStatus = FAILED(2)` 的记录发起手动重试，重置状态为 RETRYING(3) 并触发重新上链
**错误码**：`400001` / `400007` / `400008` / `400010`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | ChainRecord 主键（雪花 ID） | `2005` |

#### 响应体 ChainRecord

返回重试后的 `ChainRecord`（结构同 §2.1），状态变为 `RETRYING(3)`。

#### 响应示例（成功，开始重试）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 2005,
    "traceCode": "BATCH-20260411-099",
    "businessType": "BATCH",
    "businessId": 99,
    "dataSnapshot": "{\"batchCode\":\"B099\",\"weight\":300}",
    "dataHash": "c7d4e2a0b8f31e5264h8c3e2g7f4b0d6c7d4e2a0b8f31e5264h8c3e2g7f4b0d6",
    "chainTxHash": null,
    "chainBlockHeight": null,
    "chainStatus": { "code": 3, "desc": "重试中" },
    "errorMsg": null,
    "retryCount": 4,
    "createdAt": "2026-04-11 22:00:00",
    "updatedAt": "2026-04-12 09:00:00"
  }
}
```

#### 响应示例（失败，记录不存在）

```json
{
  "code": 400001,
  "message": "上链记录不存在",
  "data": null
}
```

#### 响应示例（失败，状态不允许重试）

```json
{
  "code": 400008,
  "message": "只有 failed(2) 状态可重试",
  "data": null
}
```

#### 响应示例（失败，超过最大重试次数）

```json
{
  "code": 400007,
  "message": "超过最大重试次数（MAX_RETRY=3）",
  "data": null
}
```

#### 业务规则

1. `id` 不存在 → `400001 CHAIN_RECORD_NOT_FOUND`
2. 当前 `chainStatus != FAILED(2)` → `400008 CHAIN_RETRY_STATUS_INVALID`（PENDING/SUCCESS/RETRYING 均不可手动重试）
3. `retryCount >= MAX_RETRY(3)` → `400007 CHAIN_RETRY_MAX_EXCEEDED`；此时记录需人工介入（如检查奥链节点状态）
4. 通过校验后：`chainStatus → RETRYING(3)`，`retryCount += 1`，`errorMsg → null`，触发异步重新上链
5. 重试成功后：`chainStatus → SUCCESS(1)`，`chainTxHash` 与 `chainBlockHeight` 填充
6. 重试再次失败：`chainStatus → FAILED(2)`，`errorMsg` 更新

---

## 3. 错误码清单

> 号段：`400000 ~ 499999`（CONVENTIONS.md:136 权威）

| 错误码 | 常量名 | 中文描述 | 触发场景 |
|--------|--------|---------|---------|
| `400001` | `CHAIN_RECORD_NOT_FOUND` | 上链记录不存在 | `retry/{id}` 中 id 不存在 |
| `400002` | `CHAIN_TRACE_CODE_NOT_FOUND` | 溯源码不存在 | `verify/{traceCode}` 找不到任何记录 |
| `400003` | `CHAIN_SNAPSHOT_EMPTY` | 快照数据为空 | `submit` 时 snapshot 为 null 或空 Map |
| `400004` | `CHAIN_HASH_COMPUTE_FAILED` | SHA-256 计算失败 | 序列化或哈希计算异常 |
| `400005` | `CHAIN_SUBMIT_FAILED` | 上链提交失败 | 异步队列满或提交链节点失败 |
| `400006` | `CHAIN_TX_NOT_CONFIRMED` | 交易未确认 | verify 时所有记录均处于 PENDING 状态 |
| `400007` | `CHAIN_RETRY_MAX_EXCEEDED` | 超过最大重试次数 | `retryCount >= 3`（MAX_RETRY=3） |
| `400008` | `CHAIN_RETRY_STATUS_INVALID` | 只有 failed(2) 状态可重试 | retry 时状态不为 FAILED |
| `400009` | `CHAIN_VERIFY_FAILED` | 链上验证失败 | 链上哈希与本地 dataHash 不一致 |
| `400010` | `CHAIN_SERVICE_UNAVAILABLE` | 区块链服务不可用 | 奥链节点离线或连接超时 |
| `400900` | `CHAIN_PARAM_INVALID` | 参数非法 | 通用参数校验失败（`@NotBlank` 等） |
| `400901` | `CHAIN_UNAUTHORIZED` | 无权限 | 缺少 `trace:write` / `trace:read` 权限（业务层补充，HTTP 层返回 403） |

---

## 4. 枚举集中声明

### 4.1 ChainRecordStatus（Integer code）

> 对应 `ChainRecord.chainStatus`，枚举值响应格式：`{ "code": 0, "desc": "待上链" }`

| code | 枚举名 | 中文描述 | 说明 |
|------|--------|---------|------|
| `0` | `PENDING` | 待上链 | 初始状态，异步上链队列中 |
| `1` | `SUCCESS` | 已上链 | 上链成功，`chainTxHash` 和 `chainBlockHeight` 已填充 |
| `2` | `FAILED` | 失败 | 上链失败，`errorMsg` 有详情，可手动重试 |
| `3` | `RETRYING` | 重试中 | 手动/自动重试触发，正在重新上链 |

常量对应（Java）：
- `ChainRecord.STATUS_PENDING = 0`
- `ChainRecord.STATUS_SUCCESS = 1`
- `ChainRecord.STATUS_FAILED = 2`
- `ChainRecord.STATUS_RETRY = 3`
- `ChainRecord.MAX_RETRY = 3`

```typescript
export enum ChainRecordStatusCode {
  PENDING = 0,
  SUCCESS = 1,
  FAILED = 2,
  RETRYING = 3,
}
```

### 4.2 ChainBusinessType（String code）

> 对应 `ChainRecord.businessType`，请求传原始字符串，响应回原始字符串（非 EnumValue，因为历史兼容设计为 String 列）

| code（字符串） | 中文描述 | 说明 |
|----------------|---------|------|
| `BATCH` | 批次 | 采收批次级别快照 |
| `BOX` | 箱 | 箱级别快照（装箱时触发） |
| `FRUIT` | 果 | 果/单品级别快照 |
| `CULTIVATION` | 种植 | 种植过程快照（扩展中） |
| `HARVEST` | 采收 | 采收过程快照（扩展中） |
| `TRADE` | 交易 | 交易快照（对接 trade.md） |

```typescript
export type ChainBusinessType =
  | 'BATCH'
  | 'BOX'
  | 'FRUIT'
  | 'CULTIVATION'
  | 'HARVEST'
  | 'TRADE';
```

---

## 5. 实现注记

### 5.1 物理模块归属说明

`ChainController` 物理上位于 `apple-module-trace` 模块（`com.apple.chain.trace.controller`），**没有独立的 `apple-module-chain`**。这是因为区块链存证在早期版本与溯源模块共包部署。逻辑上，区块链存证已独立为 M2-C 子模块，本契约文件是其唯一权威源。

若未来拆分为独立物理模块，需同步更新：
1. 本文件"物理归属"行
2. `trace.md` "关联契约"章节中的 chain.md 引用注释
3. Maven `pom.xml` 模块结构

### 5.2 Mock / 真实模式切换（@ConditionalOnProperty）

```yaml
# application.yml（本地开发）
chain:
  mock: true    # true = Mock 模式，false = 真实奥链模式
```

| 配置 | 行为 |
|------|------|
| `chain.mock=true` | `ChainSubmitService` 使用 Mock 实现：异步任务直接赋值假 `chainTxHash`（格式：`"mock_tx_hash_" + id`），记录状态跳转为 `SUCCESS(1)` |
| `chain.mock=false`（默认） | 使用真实奥链 SDK，异步提交后通过回调更新 `chainTxHash` 和 `chainBlockHeight` |

**前端注意**：Mock 模式下 `chainTxHash` 格式为 `"mock_tx_hash_XXX"`，不是真实链上哈希。`Blockchain.vue` 展示时可判断前缀，加 `[Mock]` 标记。

### 5.3 verify 接口：Map → ChainVerifyResponse DTO 迁移计划

当前后端 `verify` 端点返回 `R<Map<String, Object>>`，契约已定义目标 DTO `ChainVerifyResponse`（§2.2）。

**迁移步骤**（待后端完成）：
1. 新建 `ChainVerifyResponse` 和 `ChainVerifyRecord` Java DTO
2. `ChainController.verify()` 返回类型改为 `R<ChainVerifyResponse>`
3. `ChainSubmitService.verifyByTraceCode()` 返回类型改为 `ChainVerifyResponse`
4. 修改本文件"变更历史"，版本 v1.1

**前端兼容策略**：`Blockchain.vue` 按 `ChainVerifyResponse` 结构解析响应；在后端迁移完成前，如果 Map 中的字段名与契约一致，无需前端改动。

### 5.4 MAX_RETRY=3 重试策略

| 阶段 | 触发方式 | 说明 |
|------|---------|------|
| 自动重试 | 定时任务（间隔 5 分钟） | 扫描 `FAILED` 记录，`retryCount < 3` 时自动触发 |
| 手动重试 | `POST /api/admin/chain/retry/{id}` | 管理员在后台手动触发，`retryCount < 3` 时允许 |
| 放弃重试 | — | `retryCount >= 3` 后不再自动重试，需人工介入排查奥链节点 |

`retryCount` 语义：历史已发生的重试次数（不含本次）。达到 `MAX_RETRY=3` 后状态永久为 `FAILED`，`errorMsg` 保留最后一次失败原因。

### 5.5 数据完整性：dataHash = SHA-256(canonicalize(snapshot))

规范化规则：
1. Map 按 key 字典序排列（递归）
2. 序列化为无空白的紧凑 JSON 字符串
3. UTF-8 编码后计算 SHA-256
4. 输出 64 位小写 hex 字符串

示例：
```
snapshot = { "weight": 500, "batchCode": "B001" }
canonicalized = '{"batchCode":"B001","weight":500}'
dataHash = sha256('{"batchCode":"B001","weight":500}') → "a3f5e2c1..."
```

**前端 Blockchain.vue 验证逻辑**：
- 展示 `dataHash` 供用户或监管方核对
- 若需前端验证，可用 Web Crypto API：`crypto.subtle.digest('SHA-256', encoded)` 对 `dataSnapshot` 字段计算后与 `dataHash` 对比

### 5.6 关联契约跨模块依赖

| 上游模块 | 触发上链的业务动作 | 传入 businessType |
|---------|-----------------|-----------------|
| `trace.md` | 溯源批次创建/状态变更 | `BATCH` |
| `trace.md` | BOX 级编码生成 | `BOX` |
| `trace.md` | FRUIT 级编码生成 | `FRUIT` |
| `warehouse.md` | 仓单入库确认 | `BATCH`（仓储快照） |
| `trade.md` | 交易订单完成 | `TRADE` |
| `coldchain.md` | 冷链温度异常记录 | `BATCH`（温度快照） |

**调用方式**：上游模块调用 `POST /api/trace/chain/submit`（内部服务间调用，需携带有效 JWT 或使用内部服务账号），传入对应 `businessType` 和 `snapshot`。
