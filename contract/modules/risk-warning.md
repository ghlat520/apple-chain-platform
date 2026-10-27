# 风控预警 (Risk Warning) 模块契约

> 模块：`apple-module-finance`
> URL 前缀：`/api/risk/{rules,events}`
> 错误码段：`1000000 ~ 1099999`（沿用 finance 段，详见 `CONVENTIONS.md:142`）
> 最近更新：2026-04-14
> 权威源：本文件
> 后端实现：`apple-module-finance`（`RiskRuleController` / `RiskEventController`）
> 关联契约：`finance.md`（贷款/质押作为预警 target）

---

## 1. 概念

- **RiskRule（规则）** — 配置型阈值规则。规则 = 类型 + 阈值 + 严重度。规则可启用/停用。
- **RiskEvent（事件）** — 规则被触发后产生的一次预警实例。事件有生命周期：
  ```
  PENDING ──→ HANDLING ──→ RESOLVED
        │             │
        └─────────────┴──→ IGNORED
  ```
  RESOLVED / IGNORED 为终态，不可再次处置。

---

## 2. 接口索引

| # | 接口中文名 | URL | 方法 | 认证 |
|---|-----------|-----|------|------|
| 1 | 规则列表（分页） | `/api/risk/rules` | GET | ✅ |
| 2 | 规则详情 | `/api/risk/rules/{id}` | GET | ✅ |
| 3 | 创建规则 | `/api/risk/rules` | POST | ✅ |
| 4 | 更新规则 | `/api/risk/rules/{id}` | PUT | ✅ |
| 5 | 启用 / 停用规则 | `/api/risk/rules/{id}/enable` | PUT | ✅ |
| 6 | 删除规则 | `/api/risk/rules/{id}` | DELETE | ✅ |
| 7 | 事件列表（分页 + 过滤） | `/api/risk/events` | GET | ✅ |
| 8 | 事件详情 | `/api/risk/events/{id}` | GET | ✅ |
| 9 | 处置事件 | `/api/risk/events/{id}/handle` | PUT | ✅ |

---

## 3. 共享枚举

### RiskSeverity

| code | name | desc |
|------|------|------|
| 1 | `LOW` | 低 |
| 2 | `MEDIUM` | 中 |
| 3 | `HIGH` | 高 |
| 4 | `CRITICAL` | 严重 |

### RiskEventStatus

| code | name | desc |
|------|------|------|
| 1 | `PENDING` | 待处理 |
| 2 | `HANDLING` | 处理中 |
| 3 | `RESOLVED` | 已解决 |
| 4 | `IGNORED` | 已忽略 |

```typescript
export interface EnumValue<T = number> { code: T; desc: string }
export type RiskSeverity = EnumValue<1 | 2 | 3 | 4>;
export type RiskEventStatus = EnumValue<1 | 2 | 3 | 4>;
```

请求体里枚举字段允许传 enum **name**（如 `"HIGH"`）或 enum **code**（如 `3`），后端统一兼容；响应里**始终**返回 `{code, desc}` 对象形态。

---

## 4. 规则管理 RiskRuleController

### 4.1 规则列表（分页）

**URL**：`GET /api/risk/rules`
**认证**：需要 JWT
**变更历史**：2026-04-14 | init

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | ❌ | 默认 1 |
| `size` | number | ❌ | 默认 10 |
| `keyword` | string | ❌ | 模糊匹配 name / ruleType |
| `severity` | string | ❌ | LOW/MEDIUM/HIGH/CRITICAL |
| `enabled` | boolean | ❌ | true / false |

#### 响应体 `R<PageResult<RiskRuleResponse>>`

```typescript
export interface RiskRuleResponse {
  id: string;            // 雪花 ID 字符串化
  name: string;
  ruleType: string;      // 例如 OVERDUE_DAYS
  threshold: number;     // 阈值（数值，前端展示需结合 ruleType）
  severity: EnumValue;   // {code, desc}
  enabled: boolean;
  creatorId: string | null;
  createTime: string;    // yyyy-MM-dd HH:mm:ss
  updateTime: string;
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [{
      "id": "2801",
      "name": "贷款逾期超过30天",
      "ruleType": "OVERDUE_DAYS",
      "threshold": 30.0,
      "severity": { "code": 3, "desc": "高" },
      "enabled": true,
      "creatorId": "1",
      "createTime": "2026-04-14 10:00:00",
      "updateTime": "2026-04-14 10:00:00"
    }],
    "total": 10, "current": 1, "size": 10
  }
}
```

### 4.2 创建规则

**URL**：`POST /api/risk/rules`

#### 请求体 `RiskRuleRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | ✅ | 规则名称，≤100 字 |
| `ruleType` | string | ✅ | 规则类型 code |
| `threshold` | number | ✅ | 阈值 |
| `severity` | string \| number | ✅ | RiskSeverity name 或 code |
| `enabled` | boolean | ❌ | 不传默认 true |

```typescript
export interface RiskRuleRequest {
  name: string;
  ruleType: string;
  threshold: number;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  enabled?: boolean;
}
```

### 4.3 更新规则

**URL**：`PUT /api/risk/rules/{id}` — 请求体同 `RiskRuleRequest`。`enabled` 不传则保持现状。

### 4.4 启用 / 停用规则

**URL**：`PUT /api/risk/rules/{id}/enable?enabled=true`
**说明**：`enabled` 为 query 参数，幂等（状态相同则不写库）。

### 4.5 删除规则

**URL**：`DELETE /api/risk/rules/{id}` — 软删除（沿用 BaseEntity.deleted）。

---

## 5. 事件管理 RiskEventController

### 5.1 事件列表

**URL**：`GET /api/risk/events`

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` / `size` | number | ❌ | 默认 1 / 10 |
| `severity` | string | ❌ | LOW/MEDIUM/HIGH/CRITICAL |
| `status` | string | ❌ | PENDING/HANDLING/RESOLVED/IGNORED |
| `targetType` | string | ❌ | 业务对象类型（LOAN/PLEDGE/FARMER/COLD_CHAIN/...） |

#### 响应体 `R<PageResult<RiskEventResponse>>`

```typescript
export interface RiskEventResponse {
  id: string;
  ruleId: string;
  targetType: string;
  targetId: string;
  severity: EnumValue;
  triggerValue: number | null;
  status: EnumValue;
  assigneeId: string | null;
  handleRemark: string | null;
  triggerTime: string;
  handleTime: string | null;
  createTime: string;
}
```

### 5.2 处置事件

**URL**：`PUT /api/risk/events/{id}/handle`

#### 请求体 `RiskEventHandleRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `status` | string | ✅ | 仅允许 HANDLING / RESOLVED / IGNORED |
| `handleRemark` | string | ❌ | ≤500 字 |
| `assigneeId` | number | ❌ | 处理人用户 ID |

```typescript
export interface RiskEventHandleRequest {
  status: 'HANDLING' | 'RESOLVED' | 'IGNORED';
  handleRemark?: string;
  assigneeId?: number;
}
```

#### 状态机约束

| 当前状态 | 允许的下一状态 |
|---------|---------------|
| PENDING | HANDLING / RESOLVED / IGNORED |
| HANDLING | RESOLVED / IGNORED |
| RESOLVED / IGNORED | (终态，不可处置) |

转入 RESOLVED / IGNORED 时后端自动写入 `handleTime = now()`。
回退到 PENDING 一律拒绝。

---

## 6. 错误码

复用 `finance` 段；本模块暂未自定义业务错误码。校验失败统一走通用：

| code | message | 触发场景 |
|------|---------|---------|
| 400 | 操作失败 | 状态机非法转换（"已结案"/"回退"等） |
| 404 | 资源不存在 | 规则 / 事件 ID 找不到 |
| 422 | 参数校验失败 | request 体校验未通过 |

---

## 7. 变更历史

| 日期 | 版本 | 变更 | 作者 |
|------|------|------|------|
| 2026-04-14 | init | 新增风控预警模块（规则 + 事件 + 状态机） | risk-warning agent |
