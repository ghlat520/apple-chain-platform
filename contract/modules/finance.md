# M7 金融服务 (Finance) 模块契约

> 模块：`apple-module-finance`
> URL 前缀：`/api/finance/{loans,pledges,credits,risks,statistics}`
> 错误码段：`1000000 ~ 1099999`（CONVENTIONS.md:142 权威）
> 最近更新：2026-04-12
> 权威源：本文件
> 后端实现：`apple-module-finance`
> 关联契约：`warehouse.md`（仓单质押）/ `trade.md`（订单融资）/ `user.md`（农户信用评级）

---

## 1. 接口索引（41 端点）

| # | 接口中文名 | URL | 方法 | 认证 |
|---|-----------|-----|------|------|
| 1 | [贷款列表（分页）](#21-贷款列表分页) | `/api/finance/loans/list` | GET | ✅ |
| 2 | [贷款详情](#22-贷款详情) | `/api/finance/loans/{id}` | GET | ✅ |
| 3 | [创建贷款申请](#23-创建贷款申请) | `/api/finance/loans` | POST | ✅ |
| 4 | [更新贷款](#24-更新贷款) | `/api/finance/loans/{id}` | PUT | ✅ |
| 5 | [审批通过](#25-审批通过) | `/api/finance/loans/{id}/approve` | POST | ✅ |
| 6 | [审批拒绝](#26-审批拒绝) | `/api/finance/loans/{id}/reject` | POST | ✅ |
| 7 | [放款](#27-放款) | `/api/finance/loans/{id}/disburse` | POST | ✅ |
| 8 | [还款](#28-还款) | `/api/finance/loans/{id}/repay` | POST | ✅ |
| 9 | [结清贷款](#29-结清贷款) | `/api/finance/loans/{id}/settle` | POST | ✅ |
| 10 | [标记逾期](#210-标记逾期) | `/api/finance/loans/{id}/overdue` | POST | ✅ |
| 11 | [删除贷款](#211-删除贷款) | `/api/finance/loans/{id}` | DELETE | ✅ |
| 12 | [申请种植贷款](#212-申请种植贷款) | `/api/finance/loans/apply/plant` | POST | ✅ |
| 13 | [申请仓单贷款](#213-申请仓单贷款) | `/api/finance/loans/apply/warehouse` | POST | ✅ |
| 14 | [申请贸易贷款](#214-申请贸易贷款) | `/api/finance/loans/apply/trade` | POST | ✅ |
| 15 | [申请出口贷款](#215-申请出口贷款) | `/api/finance/loans/apply/export` | POST | ✅ |
| 16 | [贷款工作流](#216-贷款工作流合同发票支付) | `/api/finance/loans/{id}/process-workflow` | POST | ✅ |
| 17 | [导出贷款 CSV](#217-导出贷款-csv) | `/api/finance/loans/export` | GET | ✅ |
| 18 | [质押列表（分页）](#31-质押列表分页) | `/api/finance/pledges/list` | GET | ✅ |
| 19 | [质押详情](#32-质押详情) | `/api/finance/pledges/{id}` | GET | ✅ |
| 20 | [创建质押](#33-创建质押) | `/api/finance/pledges` | POST | ✅ |
| 21 | [更新质押](#34-更新质押) | `/api/finance/pledges/{id}` | PUT | ✅ |
| 22 | [激活质押](#35-激活质押) | `/api/finance/pledges/{id}/activate` | POST | ✅ |
| 23 | [解除质押](#36-解除质押) | `/api/finance/pledges/{id}/release` | POST | ✅ |
| 24 | [标记质押违约](#37-标记质押违约) | `/api/finance/pledges/{id}/default` | POST | ✅ |
| 25 | [删除质押](#38-删除质押) | `/api/finance/pledges/{id}` | DELETE | ✅ |
| 26 | [导出质押 CSV](#39-导出质押-csv) | `/api/finance/pledges/export` | GET | ✅ |
| 27 | [信用评级列表（分页）](#41-信用评级列表分页) | `/api/finance/credits/list` | GET | ✅ |
| 28 | [信用评级详情](#42-信用评级详情) | `/api/finance/credits/{id}` | GET | ✅ |
| 29 | [创建信用评级](#43-创建信用评级) | `/api/finance/credits` | POST | ✅ |
| 30 | [更新信用评级](#44-更新信用评级) | `/api/finance/credits/{id}` | PUT | ✅ |
| 31 | [删除信用评级](#45-删除信用评级) | `/api/finance/credits/{id}` | DELETE | ✅ |
| 32 | [计算信用评分](#46-计算信用评分) | `/api/finance/credits/{entityId}/calculate` | POST | ✅ |
| 33 | [导出信用评级 CSV](#47-导出信用评级-csv) | `/api/finance/credits/export` | GET | ✅ |
| 34 | [风控记录列表（分页）](#51-风控记录列表分页) | `/api/finance/risks/list` | GET | ✅ |
| 35 | [风控记录详情](#52-风控记录详情) | `/api/finance/risks/{id}` | GET | ✅ |
| 36 | [创建风控记录](#53-创建风控记录) | `/api/finance/risks` | POST | ✅ |
| 37 | [更新风控记录](#54-更新风控记录) | `/api/finance/risks/{id}` | PUT | ✅ |
| 38 | [删除风控记录](#55-删除风控记录) | `/api/finance/risks/{id}` | DELETE | ✅ |
| 39 | [导出风控记录 CSV](#56-导出风控记录-csv) | `/api/finance/risks/export` | GET | ✅ |
| 40 | [贷款汇总统计](#61-贷款汇总统计) | `/api/finance/statistics/summary` | GET | ✅ |
| 41 | [风险统计](#62-风险统计) | `/api/finance/statistics/risk` | GET | ✅ |

---

## 2. 贷款管理 LoanController（`/api/finance/loans`）

### 2.1 贷款列表（分页）

**URL**：`GET /api/finance/loans/list`
**认证**：需要 JWT Bearer
**错误码**：`1000100`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，从 1 开始，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 模糊匹配贷款编号或借款人名称 | `张` |
| `loanType` | string | ❌ | 贷款类型枚举 code，见 LoanType | `PLANT` |
| `status` | string | ❌ | 贷款状态枚举 code，见 LoanStatus | `PENDING` |

```typescript
export interface LoanPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  loanType?: string;
  status?: string;
}
```

#### 响应体 `PageResult<LoanResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 贷款 ID（雪花） |
| `loanCode` | string | ✅ | 贷款编号，格式 `LN{yyyyMMdd}{4位序号}` |
| `borrowerName` | string | ✅ | 借款人名称 |
| `borrowerType` | `EnumValue<string>` | ✅ | 借款人类型，见 EntityType |
| `borrowerId` | number | ✅ | 借款人 ID |
| `loanType` | `EnumValue<string>` | ✅ | 贷款类型，见 LoanType |
| `amount` | number | ✅ | 贷款金额（精度 2 位） |
| `interestRate` | number | ✅ | 年利率（如 `0.05` 表示 5%） |
| `termMonths` | number | ✅ | 贷款期限（月） |
| `applyDate` | string | ✅ | 申请日期，格式 `yyyy-MM-dd` |
| `approveDate` | string \| null | ❌ | 审批日期 |
| `disburseDate` | string \| null | ❌ | 放款日期 |
| `dueDate` | string \| null | ❌ | 到期日期 |
| `repaidAmount` | number | ✅ | 已还款金额（累计，精度 2 位） |
| `status` | `EnumValue<string>` | ✅ | 贷款状态，见 LoanStatus |
| `pledgeId` | number \| null | ❌ | 关联质押 ID |
| `productSpecificData` | string \| null | ❌ | 特定产品附属数据（JSON 字符串） |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface LoanResponse {
  id: number;
  loanCode: string;
  borrowerName: string;
  borrowerType: EnumValue<string>;
  borrowerId: number;
  loanType: EnumValue<string>;
  amount: number;
  interestRate: number;
  termMonths: number;
  applyDate: string;
  approveDate: string | null;
  disburseDate: string | null;
  dueDate: string | null;
  repaidAmount: number;
  status: EnumValue<string>;
  pledgeId: number | null;
  productSpecificData: string | null;
  remark: string | null;
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
    "records": [
      {
        "id": 18910001,
        "loanCode": "LN202604110001",
        "borrowerName": "张三",
        "borrowerType": { "code": "FARMER", "desc": "果农" },
        "borrowerId": 1001,
        "loanType": { "code": "PLANT", "desc": "种植贷款" },
        "amount": 50000.00,
        "interestRate": 0.045,
        "termMonths": 12,
        "applyDate": "2026-04-11",
        "approveDate": null,
        "disburseDate": null,
        "dueDate": null,
        "repaidAmount": 0,
        "status": { "code": "PENDING", "desc": "待审批" },
        "pledgeId": null,
        "productSpecificData": null,
        "remark": null,
        "createdAt": "2026-04-11 10:00:00",
        "updatedAt": "2026-04-11 10:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000100,
  "message": "贷款列表查询失败",
  "data": null
}
```

---

### 2.2 贷款详情

**URL**：`GET /api/finance/loans/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1000101`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 贷款 ID（雪花） | `18910001` |

#### 响应体 `LoanResponse`

字段同 [2.1 响应体 LoanResponse](#21-贷款列表分页)。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 18910001,
    "loanCode": "LN202604110001",
    "borrowerName": "张三",
    "borrowerType": { "code": "FARMER", "desc": "果农" },
    "borrowerId": 1001,
    "loanType": { "code": "PLANT", "desc": "种植贷款" },
    "amount": 50000.00,
    "interestRate": 0.045,
    "termMonths": 12,
    "applyDate": "2026-04-11",
    "approveDate": null,
    "disburseDate": null,
    "dueDate": null,
    "repaidAmount": 0,
    "status": { "code": "PENDING", "desc": "待审批" },
    "pledgeId": null,
    "productSpecificData": null,
    "remark": null,
    "createdAt": "2026-04-11 10:00:00",
    "updatedAt": "2026-04-11 10:00:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000101,
  "message": "贷款不存在",
  "data": null
}
```

---

### 2.3 创建贷款申请

**URL**：`POST /api/finance/loans`
**认证**：需要 JWT Bearer
**错误码**：`1000102`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 CreateLoanRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `borrowerName` | string | ✅ | 借款人名称 | `张三` |
| `borrowerType` | string | ✅ | 借款人类型枚举 code，见 EntityType | `FARMER` |
| `borrowerId` | number | ✅ | 借款人 ID | `1001` |
| `loanType` | string | ✅ | 贷款类型枚举 code，见 LoanType | `CREDIT` |
| `amount` | number | ✅ | 贷款金额 | `50000.00` |
| `interestRate` | number | ✅ | 年利率 | `0.045` |
| `termMonths` | number | ✅ | 贷款期限（月） | `12` |
| `applyDate` | string | ❌ | 申请日期，默认当日 | `2026-04-11` |
| `pledgeId` | number | ❌ | 关联质押 ID（质押贷款时必填） | `2001` |
| `productSpecificData` | string | ❌ | 特定产品附属数据（JSON 字符串） | `null` |
| `remark` | string | ❌ | 备注 | `null` |

```typescript
export interface CreateLoanRequest {
  borrowerName: string;
  borrowerType: string;
  borrowerId: number;
  loanType: string;
  amount: number;
  interestRate: number;
  termMonths: number;
  applyDate?: string;
  pledgeId?: number;
  productSpecificData?: string;
  remark?: string;
}
```

#### 响应体 `LoanResponse`

字段同 [2.1 响应体 LoanResponse](#21-贷款列表分页)。

#### 业务规则

- 系统自动生成 `loanCode`，格式 `LN{yyyyMMdd}{4位序号}`，如 `LN202604110001`。
- 初始 `status` 固定为 `PENDING`。
- 初始 `repaidAmount` 为 `0`。
- `loanCode`、`status`、`repaidAmount` 由系统生成，传入无效。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 18910002,
    "loanCode": "LN202604110002",
    "borrowerName": "李四",
    "borrowerType": { "code": "SUPPLIER", "desc": "供应商" },
    "borrowerId": 1002,
    "loanType": { "code": "CREDIT", "desc": "信用贷款" },
    "amount": 100000.00,
    "interestRate": 0.05,
    "termMonths": 24,
    "applyDate": "2026-04-11",
    "approveDate": null,
    "disburseDate": null,
    "dueDate": null,
    "repaidAmount": 0,
    "status": { "code": "PENDING", "desc": "待审批" },
    "pledgeId": null,
    "productSpecificData": null,
    "remark": null,
    "createdAt": "2026-04-11 10:30:00",
    "updatedAt": "2026-04-11 10:30:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000102,
  "message": "贷款金额必须大于0",
  "data": null
}
```

---

### 2.4 更新贷款

**URL**：`PUT /api/finance/loans/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1000103` / `1000104`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 贷款 ID | `18910001` |

#### 请求体 UpdateLoanRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `borrowerName` | string | ❌ | 借款人名称 | `张三` |
| `amount` | number | ❌ | 贷款金额 | `60000.00` |
| `interestRate` | number | ❌ | 年利率 | `0.05` |
| `termMonths` | number | ❌ | 贷款期限（月） | `12` |
| `remark` | string | ❌ | 备注 | `更新备注` |

```typescript
export interface UpdateLoanRequest {
  borrowerName?: string;
  amount?: number;
  interestRate?: number;
  termMonths?: number;
  remark?: string;
}
```

#### 业务规则

- `loanCode` 不可修改。
- 状态为 `DISBURSED`（已放款）或 `REPAID`（已还清）的贷款禁止修改。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* LoanResponse，同 2.1 */ }
}
```

#### 响应示例（失败 — 状态不允许）

```json
{
  "code": 1000103,
  "message": "已放款的贷款禁止修改",
  "data": null
}
```

#### 响应示例（失败 — 不存在）

```json
{
  "code": 1000104,
  "message": "贷款不存在",
  "data": null
}
```

---

### 2.5 审批通过

**URL**：`POST /api/finance/loans/{id}/approve`
**认证**：需要 JWT Bearer
**错误码**：`1000105`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 贷款 ID | `18910001` |

#### 业务规则

- 仅允许 `PENDING` → `APPROVED` 状态转换。
- 自动记录 `approveDate` 为当日。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* LoanResponse，status 变为 APPROVED */ }
}
```

#### 响应示例（失败 — 状态不允许）

```json
{
  "code": 1000105,
  "message": "仅待审批状态的贷款可以审批通过",
  "data": null
}
```

---

### 2.6 审批拒绝

**URL**：`POST /api/finance/loans/{id}/reject`
**认证**：需要 JWT Bearer
**错误码**：`1000106`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 贷款 ID | `18910001` |

#### 业务规则

- 仅允许 `PENDING` → `REJECTED` 状态转换。
- 自动记录 `approveDate` 为当日。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* LoanResponse，status 变为 REJECTED */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000106,
  "message": "仅待审批状态的贷款可以拒绝",
  "data": null
}
```

---

### 2.7 放款

**URL**：`POST /api/finance/loans/{id}/disburse`
**认证**：需要 JWT Bearer
**错误码**：`1000107`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 贷款 ID | `18910001` |

#### 业务规则

- 仅允许 `APPROVED` → `DISBURSED` 状态转换。
- 自动记录 `disburseDate` 为当日。
- 自动计算 `dueDate` = 放款日期 + `termMonths` 个月。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* LoanResponse，status 变为 DISBURSED，disburseDate 和 dueDate 已填写 */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000107,
  "message": "仅已审批通过的贷款可以放款",
  "data": null
}
```

---

### 2.8 还款

**URL**：`POST /api/finance/loans/{id}/repay`
**认证**：需要 JWT Bearer
**错误码**：`1000108` / `1000109`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 贷款 ID | `18910001` |

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `amount` | number | ✅ | 本次还款金额（精度 2 位） | `10000.00` |

#### 业务规则

- 累计 `repaidAmount` += 本次还款金额。
- 当 `repaidAmount >= amount`（贷款总额）时，状态自动切换为 `REPAID`。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* LoanResponse，repaidAmount 已累加 */ }
}
```

#### 响应示例（失败 — 金额无效）

```json
{
  "code": 1000108,
  "message": "还款金额必须大于0",
  "data": null
}
```

#### 响应示例（失败 — 状态不允许）

```json
{
  "code": 1000109,
  "message": "仅已放款或逾期状态的贷款可以还款",
  "data": null
}
```

---

### 2.9 结清贷款

**URL**：`POST /api/finance/loans/{id}/settle`
**认证**：需要 JWT Bearer
**错误码**：`1000110`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 贷款 ID | `18910001` |

#### 业务规则

- 仅允许 `REPAID` → `SETTLED` 状态转换。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* LoanResponse，status 变为 SETTLED */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000110,
  "message": "仅已还清状态的贷款可以结清",
  "data": null
}
```

---

### 2.10 标记逾期

**URL**：`POST /api/finance/loans/{id}/overdue`
**认证**：需要 JWT Bearer
**错误码**：`1000111`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 贷款 ID | `18910001` |

#### 业务规则

- 仅允许 `DISBURSED` → `OVERDUE` 状态转换。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* LoanResponse，status 变为 OVERDUE */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000111,
  "message": "仅已放款状态的贷款可以标记逾期",
  "data": null
}
```

---

### 2.11 删除贷款

**URL**：`DELETE /api/finance/loans/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1000112` / `1000113`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 贷款 ID | `18910001` |

#### 业务规则

- 状态为 `DISBURSED`（已放款）的贷款禁止删除。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

#### 响应示例（失败 — 状态不允许）

```json
{
  "code": 1000112,
  "message": "已放款的贷款禁止删除",
  "data": null
}
```

#### 响应示例（失败 — 不存在）

```json
{
  "code": 1000113,
  "message": "贷款不存在",
  "data": null
}
```

---

### 2.12 申请种植贷款

**URL**：`POST /api/finance/loans/apply/plant`
**认证**：需要 JWT Bearer
**错误码**：`1000114`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 ApplyPlantLoanRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `borrowerName` | string | ✅ | 借款人名称 | `张三` |
| `borrowerType` | string | ✅ | 借款人类型枚举 code | `FARMER` |
| `borrowerId` | number | ✅ | 借款人 ID | `1001` |
| `amount` | number | ✅ | 贷款金额 | `50000.00` |
| `interestRate` | number | ✅ | 年利率 | `0.045` |
| `termMonths` | number | ✅ | 贷款期限（月） | `12` |
| `applyDate` | string | ❌ | 申请日期 | `2026-04-11` |
| `productSpecificData` | string | ❌ | 种植贷款专属数据（JSON 字符串） | `null` |
| `remark` | string | ❌ | 备注 | `null` |

```typescript
export interface ApplyPlantLoanRequest {
  borrowerName: string;
  borrowerType: string;
  borrowerId: number;
  amount: number;
  interestRate: number;
  termMonths: number;
  applyDate?: string;
  productSpecificData?: string;
  remark?: string;
}
```

#### 业务规则

- 系统自动将 `loanType` 设为 `PLANT`，忽略请求中传入的 `loanType`。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* LoanResponse，loanType 固定为 PLANT */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000114,
  "message": "种植贷款申请失败",
  "data": null
}
```

---

### 2.13 申请仓单贷款

**URL**：`POST /api/finance/loans/apply/warehouse`
**认证**：需要 JWT Bearer
**错误码**：`1000115`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 ApplyWarehouseLoanRequest

字段同 [2.12 ApplyPlantLoanRequest](#212-申请种植贷款)，额外字段：

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `pledgeId` | number | ✅ | 关联质押 ID | `2001` |

#### 业务规则

- 系统自动将 `loanType` 设为 `WAREHOUSE`。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* LoanResponse，loanType 固定为 WAREHOUSE */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000115,
  "message": "仓单贷款申请失败",
  "data": null
}
```

---

### 2.14 申请贸易贷款

**URL**：`POST /api/finance/loans/apply/trade`
**认证**：需要 JWT Bearer
**错误码**：`1000116`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 ApplyTradeLoanRequest

字段同 [2.12 ApplyPlantLoanRequest](#212-申请种植贷款)。

#### 业务规则

- 系统自动将 `loanType` 设为 `TRADE`。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* LoanResponse，loanType 固定为 TRADE */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000116,
  "message": "贸易贷款申请失败",
  "data": null
}
```

---

### 2.15 申请出口贷款

**URL**：`POST /api/finance/loans/apply/export`
**认证**：需要 JWT Bearer
**错误码**：`1000117`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 ApplyExportLoanRequest

字段同 [2.12 ApplyPlantLoanRequest](#212-申请种植贷款)。

#### 业务规则

- 系统自动将 `loanType` 设为 `EXPORT`。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* LoanResponse，loanType 固定为 EXPORT */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000117,
  "message": "出口贷款申请失败",
  "data": null
}
```

---

### 2.16 贷款工作流（合同+发票+支付）

**URL**：`POST /api/finance/loans/{id}/process-workflow`
**认证**：需要 JWT Bearer
**错误码**：`1000118` / `1000119` / `1000120`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 贷款 ID | `18910001` |

#### 业务规则

对指定贷款依次执行三步自动化工作流：

1. **合同步骤**：调用 `ContractGateway.createContract(loanId, borrowerId, 1L)` 创建合同草稿，再调用 `signContract(contractNo, borrowerId)` 完成签署。
2. **发票步骤**：调用 `InvoiceGateway.issue(loanId, amount, borrowerName, "UNKNOWN")` 开具增值税发票。
3. **支付步骤**：调用 `PaymentGateway.createPayment(loanId, amount, "BANK_TRANSFER")` 创建预支付，再调用 `confirmPayment(paymentNo)` 确认支付。

任一步骤失败将抛出业务异常。当前所有网关为 Mock 实现，详见 [第 7 节](#7-网关接口)。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "工作流处理成功",
  "data": null
}
```

#### 响应示例（失败 — 合同创建失败）

```json
{
  "code": 1000118,
  "message": "合同创建失败",
  "data": null
}
```

#### 响应示例（失败 — 发票开具失败）

```json
{
  "code": 1000119,
  "message": "发票开具失败",
  "data": null
}
```

#### 响应示例（失败 — 支付确认失败）

```json
{
  "code": 1000120,
  "message": "支付确认失败",
  "data": null
}
```

---

### 2.17 导出贷款 CSV

**URL**：`GET /api/finance/loans/export`
**认证**：需要 JWT Bearer
**错误码**：`1000121`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `keyword` | string | ❌ | 模糊匹配贷款编号或借款人名称 | `张` |
| `loanType` | string | ❌ | 贷款类型枚举 code | `PLANT` |
| `status` | string | ❌ | 贷款状态枚举 code | `PENDING` |

#### 响应

**非 R\<T\> 包装**。直接返回文件下载：

- `Content-Type: text/csv;charset=UTF-8`
- `Content-Disposition: attachment; filename=贷款记录.csv`
- 编码：UTF-8 BOM

CSV 列顺序：贷款编号、借款人、类型、贷款类型、金额、利率、期限(月)、申请日期、审批日期、放款日期、到期日期、已还金额、状态。

---

## 3. 仓单质押 PledgeController（`/api/finance/pledges`）

### 3.1 质押列表（分页）

**URL**：`GET /api/finance/pledges/list`
**认证**：需要 JWT Bearer
**错误码**：`1000200`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，从 1 开始，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 模糊匹配质押编号或出质人名称 | `张` |
| `status` | string | ❌ | 质押状态枚举 code，见 PledgeStatus | `ACTIVE` |

```typescript
export interface PledgePageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;
}
```

#### 响应体 `PageResult<PledgeResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 质押 ID（雪花） |
| `pledgeCode` | string | ✅ | 质押编号，格式 `PL{yyyyMMdd}{4位序号}` |
| `receiptId` | number | ✅ | 关联仓单 ID |
| `receiptCode` | string | ✅ | 仓单编码 |
| `pledgorName` | string | ✅ | 出质人名称 |
| `pledgeeName` | string | ✅ | 质权人名称 |
| `commodity` | string | ✅ | 质押物名称（苹果品种等） |
| `quantity` | number | ✅ | 质押数量（精度 2 位） |
| `unit` | string | ✅ | 数量单位（如 `kg`、`吨`） |
| `appraisedValue` | number | ✅ | 评估价值（精度 2 位） |
| `pledgeRate` | number | ✅ | 质押率（如 `0.70` 表示 70%） |
| `loanAmount` | number | ✅ | 可贷金额 = appraisedValue * pledgeRate（精度 2 位） |
| `startDate` | string \| null | ❌ | 质押起始日 |
| `endDate` | string \| null | ❌ | 质押到期日 |
| `status` | `EnumValue<string>` | ✅ | 质押状态，见 PledgeStatus |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface PledgeResponse {
  id: number;
  pledgeCode: string;
  receiptId: number;
  receiptCode: string;
  pledgorName: string;
  pledgeeName: string;
  commodity: string;
  quantity: number;
  unit: string;
  appraisedValue: number;
  pledgeRate: number;
  loanAmount: number;
  startDate: string | null;
  endDate: string | null;
  status: EnumValue<string>;
  remark: string | null;
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
    "records": [
      {
        "id": 18920001,
        "pledgeCode": "PL202604110001",
        "receiptId": 3001,
        "receiptCode": "WR202604110001",
        "pledgorName": "张三",
        "pledgeeName": "农业银行",
        "commodity": "富士苹果",
        "quantity": 5000.00,
        "unit": "kg",
        "appraisedValue": 100000.00,
        "pledgeRate": 0.70,
        "loanAmount": 70000.00,
        "startDate": null,
        "endDate": null,
        "status": { "code": "PENDING", "desc": "待激活" },
        "remark": null,
        "createdAt": "2026-04-11 11:00:00",
        "updatedAt": "2026-04-11 11:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000200,
  "message": "质押列表查询失败",
  "data": null
}
```

---

### 3.2 质押详情

**URL**：`GET /api/finance/pledges/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1000201`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 质押 ID | `18920001` |

#### 响应体 `PledgeResponse`

字段同 [3.1 响应体 PledgeResponse](#31-质押列表分页)。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* PledgeResponse */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000201,
  "message": "质押记录不存在",
  "data": null
}
```

---

### 3.3 创建质押

**URL**：`POST /api/finance/pledges`
**认证**：需要 JWT Bearer
**错误码**：`1000202`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 CreatePledgeRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `receiptId` | number | ✅ | 关联仓单 ID | `3001` |
| `receiptCode` | string | ✅ | 仓单编码 | `WR202604110001` |
| `pledgorName` | string | ✅ | 出质人名称 | `张三` |
| `pledgeeName` | string | ✅ | 质权人名称 | `农业银行` |
| `commodity` | string | ✅ | 质押物名称 | `富士苹果` |
| `quantity` | number | ✅ | 质押数量 | `5000.00` |
| `unit` | string | ✅ | 数量单位 | `kg` |
| `appraisedValue` | number | ✅ | 评估价值 | `100000.00` |
| `pledgeRate` | number | ✅ | 质押率 | `0.70` |
| `remark` | string | ❌ | 备注 | `null` |

```typescript
export interface CreatePledgeRequest {
  receiptId: number;
  receiptCode: string;
  pledgorName: string;
  pledgeeName: string;
  commodity: string;
  quantity: number;
  unit: string;
  appraisedValue: number;
  pledgeRate: number;
  remark?: string;
}
```

#### 业务规则

- 系统自动生成 `pledgeCode`，格式 `PL{yyyyMMdd}{4位序号}`。
- 初始 `status` 固定为 `PENDING`。
- 若同时传入 `appraisedValue` 和 `pledgeRate`，系统自动计算 `loanAmount = appraisedValue * pledgeRate`。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* PledgeResponse，含自动生成的 pledgeCode 和计算的 loanAmount */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000202,
  "message": "质押创建失败",
  "data": null
}
```

---

### 3.4 更新质押

**URL**：`PUT /api/finance/pledges/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1000203` / `1000204`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 质押 ID | `18920001` |

#### 请求体 UpdatePledgeRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `commodity` | string | ❌ | 质押物名称 | `红富士` |
| `quantity` | number | ❌ | 质押数量 | `6000.00` |
| `appraisedValue` | number | ❌ | 评估价值 | `120000.00` |
| `pledgeRate` | number | ❌ | 质押率 | `0.65` |
| `remark` | string | ❌ | 备注 | `更新评估` |

```typescript
export interface UpdatePledgeRequest {
  commodity?: string;
  quantity?: number;
  appraisedValue?: number;
  pledgeRate?: number;
  remark?: string;
}
```

#### 业务规则

- `pledgeCode` 不可修改。
- 状态为 `ACTIVE`（生效中）的质押禁止修改。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* PledgeResponse */ }
}
```

#### 响应示例（失败 — 状态不允许）

```json
{
  "code": 1000203,
  "message": "生效中的质押禁止修改",
  "data": null
}
```

#### 响应示例（失败 — 不存在）

```json
{
  "code": 1000204,
  "message": "质押记录不存在",
  "data": null
}
```

---

### 3.5 激活质押

**URL**：`POST /api/finance/pledges/{id}/activate`
**认证**：需要 JWT Bearer
**错误码**：`1000205`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 质押 ID | `18920001` |

#### 业务规则

- 仅允许 `PENDING` → `ACTIVE` 状态转换。
- 自动记录 `startDate` 为当日。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* PledgeResponse，status 变为 ACTIVE */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000205,
  "message": "仅待激活状态的质押可以激活",
  "data": null
}
```

---

### 3.6 解除质押

**URL**：`POST /api/finance/pledges/{id}/release`
**认证**：需要 JWT Bearer
**错误码**：`1000206`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 质押 ID | `18920001` |

#### 业务规则

- 仅允许 `ACTIVE` → `RELEASED` 状态转换。
- 自动记录 `endDate` 为当日。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* PledgeResponse，status 变为 RELEASED */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000206,
  "message": "仅生效中的质押可以解除",
  "data": null
}
```

---

### 3.7 标记质押违约

**URL**：`POST /api/finance/pledges/{id}/default`
**认证**：需要 JWT Bearer
**错误码**：`1000207`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 质押 ID | `18920001` |

#### 业务规则

- 仅允许 `ACTIVE` → `DEFAULTED` 状态转换。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* PledgeResponse，status 变为 DEFAULTED */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000207,
  "message": "仅生效中的质押可以标记违约",
  "data": null
}
```

---

### 3.8 删除质押

**URL**：`DELETE /api/finance/pledges/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1000208` / `1000209`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 质押 ID | `18920001` |

#### 业务规则

- 状态为 `ACTIVE`（生效中）的质押禁止删除。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

#### 响应示例（失败 — 状态不允许）

```json
{
  "code": 1000208,
  "message": "生效中的质押禁止删除",
  "data": null
}
```

#### 响应示例（失败 — 不存在）

```json
{
  "code": 1000209,
  "message": "质押记录不存在",
  "data": null
}
```

---

### 3.9 导出质押 CSV

**URL**：`GET /api/finance/pledges/export`
**认证**：需要 JWT Bearer
**错误码**：`1000210`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `keyword` | string | ❌ | 模糊匹配质押编号或出质人名称 | `张` |
| `status` | string | ❌ | 质押状态枚举 code | `ACTIVE` |

#### 响应

**非 R\<T\> 包装**。直接返回文件下载：

- `Content-Type: text/csv;charset=UTF-8`
- `Content-Disposition: attachment; filename=仓单质押.csv`
- 编码：UTF-8 BOM

CSV 列顺序：质押编号、仓单编码、出质人、质权人、质押物、数量、单位、评估价值、质押率、可贷金额、起始日、到期日、状态。

---

## 4. 信用评级 CreditRatingController（`/api/finance/credits`）

### 4.1 信用评级列表（分页）

**URL**：`GET /api/finance/credits/list`
**认证**：需要 JWT Bearer
**错误码**：`1000300`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，从 1 开始，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 模糊匹配主体名称 | `张` |
| `entityType` | string | ❌ | 主体类型枚举 code，见 EntityType | `FARMER` |
| `creditLevel` | string | ❌ | 信用等级枚举 code，见 CreditLevel | `A` |

```typescript
export interface CreditRatingPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  entityType?: string;
  creditLevel?: string;
}
```

#### 响应体 `PageResult<CreditRatingResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 评级 ID（雪花） |
| `entityType` | `EnumValue<string>` | ✅ | 主体类型，见 EntityType |
| `entityId` | number | ✅ | 主体 ID |
| `entityName` | string | ✅ | 主体名称 |
| `creditScore` | number | ✅ | 综合信用评分（0~100） |
| `creditLevel` | `EnumValue<string>` | ✅ | 信用等级，见 CreditLevel |
| `assessmentDate` | string | ✅ | 评定日期，格式 `yyyy-MM-dd` |
| `validUntil` | string | ✅ | 有效期至，格式 `yyyy-MM-dd` |
| `tradeScore` | number | ✅ | 交易评分（60~95） |
| `productionScore` | number | ✅ | 生产评分（50~90） |
| `financialScore` | number | ✅ | 财务评分（55~85） |
| `assessor` | string \| null | ❌ | 评定人 |
| `status` | `EnumValue<string>` | ✅ | 评级状态，见 CreditRatingStatus |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface CreditRatingResponse {
  id: number;
  entityType: EnumValue<string>;
  entityId: number;
  entityName: string;
  creditScore: number;
  creditLevel: EnumValue<string>;
  assessmentDate: string;
  validUntil: string;
  tradeScore: number;
  productionScore: number;
  financialScore: number;
  assessor: string | null;
  status: EnumValue<string>;
  remark: string | null;
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
    "records": [
      {
        "id": 18930001,
        "entityType": { "code": "FARMER", "desc": "果农" },
        "entityId": 1001,
        "entityName": "张三",
        "creditScore": 85,
        "creditLevel": { "code": "A", "desc": "A级（优秀）" },
        "assessmentDate": "2026-04-11",
        "validUntil": "2027-04-11",
        "tradeScore": 90,
        "productionScore": 80,
        "financialScore": 85,
        "assessor": "系统",
        "status": { "code": "ACTIVE", "desc": "有效" },
        "remark": null,
        "createdAt": "2026-04-11 12:00:00",
        "updatedAt": "2026-04-11 12:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000300,
  "message": "信用评级列表查询失败",
  "data": null
}
```

---

### 4.2 信用评级详情

**URL**：`GET /api/finance/credits/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1000301`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 评级 ID | `18930001` |

#### 响应体 `CreditRatingResponse`

字段同 [4.1 响应体 CreditRatingResponse](#41-信用评级列表分页)。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* CreditRatingResponse */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000301,
  "message": "信用评级记录不存在",
  "data": null
}
```

---

### 4.3 创建信用评级

**URL**：`POST /api/finance/credits`
**认证**：需要 JWT Bearer
**错误码**：`1000302`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 CreateCreditRatingRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `entityType` | string | ✅ | 主体类型枚举 code，见 EntityType | `FARMER` |
| `entityId` | number | ✅ | 主体 ID | `1001` |
| `entityName` | string | ✅ | 主体名称 | `张三` |
| `creditScore` | number | ✅ | 综合信用评分（0~100） | `85` |
| `creditLevel` | string | ✅ | 信用等级枚举 code，见 CreditLevel | `A` |
| `assessmentDate` | string | ✅ | 评定日期 | `2026-04-11` |
| `validUntil` | string | ✅ | 有效期至 | `2027-04-11` |
| `tradeScore` | number | ❌ | 交易评分 | `90` |
| `productionScore` | number | ❌ | 生产评分 | `80` |
| `financialScore` | number | ❌ | 财务评分 | `85` |
| `assessor` | string | ❌ | 评定人 | `系统` |
| `remark` | string | ❌ | 备注 | `null` |

```typescript
export interface CreateCreditRatingRequest {
  entityType: string;
  entityId: number;
  entityName: string;
  creditScore: number;
  creditLevel: string;
  assessmentDate: string;
  validUntil: string;
  tradeScore?: number;
  productionScore?: number;
  financialScore?: number;
  assessor?: string;
  remark?: string;
}
```

#### 业务规则

- 初始 `status` 固定为 `ACTIVE`。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* CreditRatingResponse */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000302,
  "message": "信用评级创建失败",
  "data": null
}
```

---

### 4.4 更新信用评级

**URL**：`PUT /api/finance/credits/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1000303`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 评级 ID | `18930001` |

#### 请求体 UpdateCreditRatingRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `creditScore` | number | ❌ | 综合信用评分 | `88` |
| `creditLevel` | string | ❌ | 信用等级枚举 code | `A` |
| `tradeScore` | number | ❌ | 交易评分 | `92` |
| `productionScore` | number | ❌ | 生产评分 | `82` |
| `financialScore` | number | ❌ | 财务评分 | `88` |
| `remark` | string | ❌ | 备注 | `更新评分` |

```typescript
export interface UpdateCreditRatingRequest {
  creditScore?: number;
  creditLevel?: string;
  tradeScore?: number;
  productionScore?: number;
  financialScore?: number;
  remark?: string;
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* CreditRatingResponse */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000303,
  "message": "信用评级记录不存在",
  "data": null
}
```

---

### 4.5 删除信用评级

**URL**：`DELETE /api/finance/credits/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1000304`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 评级 ID | `18930001` |

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

#### 响应示例（失败）

```json
{
  "code": 1000304,
  "message": "信用评级记录不存在",
  "data": null
}
```

---

### 4.6 计算信用评分

**URL**：`POST /api/finance/credits/{entityId}/calculate`
**认证**：需要 JWT Bearer
**错误码**：`1000305` / `1000306`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `entityId` | number | ✅ | 主体 ID（农户/供应商/企业等） | `1001` |

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `entityType` | string | ✅ | 主体类型枚举 code，见 EntityType | `FARMER` |

#### 业务规则

对指定主体自动计算信用评分并创建新的评级记录：

| 维度 | 取值范围 | 权重 |
|------|----------|------|
| 交易评分（tradeScore） | 60 ~ 95 | 40% |
| 生产评分（productionScore） | 50 ~ 90 | 30% |
| 财务评分（financialScore） | 55 ~ 85 | 30% |

**综合评分** = `tradeScore * 0.4 + productionScore * 0.3 + financialScore * 0.3`（四舍五入取整）

**信用等级映射**：

| 综合评分 | 信用等级 |
|----------|----------|
| >= 85 | A |
| 70 ~ 84 | B |
| 55 ~ 69 | C |
| < 55 | D |

- 评级有效期：`validUntil = assessmentDate + 1年`。
- 初始状态为 `ACTIVE`。
- 当前评分数据使用模拟随机生成。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 18930002,
    "entityType": { "code": "FARMER", "desc": "果农" },
    "entityId": 1001,
    "entityName": "张三",
    "creditScore": 78,
    "creditLevel": { "code": "B", "desc": "B级（良好）" },
    "assessmentDate": "2026-04-12",
    "validUntil": "2027-04-12",
    "tradeScore": 85,
    "productionScore": 72,
    "financialScore": 76,
    "assessor": "系统",
    "status": { "code": "ACTIVE", "desc": "有效" },
    "remark": null,
    "createdAt": "2026-04-12 09:00:00",
    "updatedAt": "2026-04-12 09:00:00"
  }
}
```

#### 响应示例（失败 — 主体不存在）

```json
{
  "code": 1000305,
  "message": "评级主体不存在",
  "data": null
}
```

#### 响应示例（失败 — 计算失败）

```json
{
  "code": 1000306,
  "message": "信用评分计算失败",
  "data": null
}
```

---

### 4.7 导出信用评级 CSV

**URL**：`GET /api/finance/credits/export`
**认证**：需要 JWT Bearer
**错误码**：`1000307`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `keyword` | string | ❌ | 模糊匹配主体名称 | `张` |
| `entityType` | string | ❌ | 主体类型枚举 code | `FARMER` |
| `creditLevel` | string | ❌ | 信用等级枚举 code | `A` |

#### 响应

**非 R\<T\> 包装**。直接返回文件下载：

- `Content-Type: text/csv;charset=UTF-8`
- `Content-Disposition: attachment; filename=信用评级.csv`
- 编码：UTF-8 BOM

CSV 列顺序：主体类型、主体名称、信用评分、信用等级、评定日期、有效期至、交易分、生产分、财务分、状态。

---

## 5. 风控管理 RiskRecordController（`/api/finance/risks`）

### 5.1 风控记录列表（分页）

**URL**：`GET /api/finance/risks/list`
**认证**：需要 JWT Bearer
**错误码**：`1000400`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `page` | number | ❌ | 页码，从 1 开始，默认 1 | `1` |
| `size` | number | ❌ | 每页条数，默认 10，最大 100 | `10` |
| `keyword` | string | ❌ | 模糊匹配风险描述 | `逾期` |
| `riskLevel` | string | ❌ | 风险等级枚举 code，见 RiskLevel | `HIGH` |
| `status` | string | ❌ | 处理状态枚举 code，见 RiskRecordStatus | `OPEN` |

```typescript
export interface RiskRecordPageRequest {
  page?: number;
  size?: number;
  keyword?: string;
  riskLevel?: string;
  status?: string;
}
```

#### 响应体 `PageResult<RiskRecordResponse>`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `id` | number | ✅ | 风控记录 ID（雪花） |
| `riskCode` | string | ✅ | 风控编号，格式 `RK{yyyyMMdd}{4位序号}` |
| `relatedType` | string | ✅ | 关联业务类型（如 `LOAN`、`PLEDGE`） |
| `relatedId` | number | ✅ | 关联业务 ID |
| `riskLevel` | `EnumValue<string>` | ✅ | 风险等级，见 RiskLevel |
| `riskType` | `EnumValue<string>` | ✅ | 风险类型，见 RiskType |
| `description` | string | ✅ | 风险描述 |
| `measure` | string \| null | ❌ | 处置措施 |
| `handler` | string \| null | ❌ | 处理人 |
| `handleTime` | string \| null | ❌ | 处理时间，`yyyy-MM-dd HH:mm:ss` |
| `status` | `EnumValue<string>` | ✅ | 处理状态，见 RiskRecordStatus |
| `remark` | string \| null | ❌ | 备注 |
| `createdAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | string | ✅ | `yyyy-MM-dd HH:mm:ss` |

```typescript
export interface RiskRecordResponse {
  id: number;
  riskCode: string;
  relatedType: string;
  relatedId: number;
  riskLevel: EnumValue<string>;
  riskType: EnumValue<string>;
  description: string;
  measure: string | null;
  handler: string | null;
  handleTime: string | null;
  status: EnumValue<string>;
  remark: string | null;
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
    "records": [
      {
        "id": 18940001,
        "riskCode": "RK202604110001",
        "relatedType": "LOAN",
        "relatedId": 18910001,
        "riskLevel": { "code": "HIGH", "desc": "高风险" },
        "riskType": { "code": "OVERDUE", "desc": "逾期风险" },
        "description": "贷款已逾期超过30天",
        "measure": null,
        "handler": null,
        "handleTime": null,
        "status": { "code": "OPEN", "desc": "待处理" },
        "remark": null,
        "createdAt": "2026-04-11 14:00:00",
        "updatedAt": "2026-04-11 14:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000400,
  "message": "风控记录列表查询失败",
  "data": null
}
```

---

### 5.2 风控记录详情

**URL**：`GET /api/finance/risks/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1000401`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 风控记录 ID | `18940001` |

#### 响应体 `RiskRecordResponse`

字段同 [5.1 响应体 RiskRecordResponse](#51-风控记录列表分页)。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* RiskRecordResponse */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000401,
  "message": "风控记录不存在",
  "data": null
}
```

---

### 5.3 创建风控记录

**URL**：`POST /api/finance/risks`
**认证**：需要 JWT Bearer
**错误码**：`1000402`
**变更历史**：2026-04-12 | init | 架构师

#### 请求体 CreateRiskRecordRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `relatedType` | string | ✅ | 关联业务类型（如 `LOAN`、`PLEDGE`） | `LOAN` |
| `relatedId` | number | ✅ | 关联业务 ID | `18910001` |
| `riskLevel` | string | ✅ | 风险等级枚举 code，见 RiskLevel | `HIGH` |
| `riskType` | string | ✅ | 风险类型枚举 code，见 RiskType | `OVERDUE` |
| `description` | string | ✅ | 风险描述 | `贷款已逾期超过30天` |
| `measure` | string | ❌ | 处置措施 | `启动催收流程` |
| `handler` | string | ❌ | 处理人 | `王五` |
| `remark` | string | ❌ | 备注 | `null` |

```typescript
export interface CreateRiskRecordRequest {
  relatedType: string;
  relatedId: number;
  riskLevel: string;
  riskType: string;
  description: string;
  measure?: string;
  handler?: string;
  remark?: string;
}
```

#### 业务规则

- 系统自动生成 `riskCode`，格式 `RK{yyyyMMdd}{4位序号}`。
- 初始 `status` 固定为 `OPEN`。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* RiskRecordResponse */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000402,
  "message": "风控记录创建失败",
  "data": null
}
```

---

### 5.4 更新风控记录

**URL**：`PUT /api/finance/risks/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1000403`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 风控记录 ID | `18940001` |

#### 请求体 UpdateRiskRecordRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `riskLevel` | string | ❌ | 风险等级枚举 code | `MEDIUM` |
| `riskType` | string | ❌ | 风险类型枚举 code | `OVERDUE` |
| `description` | string | ❌ | 风险描述 | `逾期风险可控` |
| `measure` | string | ❌ | 处置措施 | `已协商还款计划` |
| `handler` | string | ❌ | 处理人 | `王五` |
| `status` | string | ❌ | 处理状态枚举 code，见 RiskRecordStatus | `HANDLING` |
| `remark` | string | ❌ | 备注 | `跟进中` |

```typescript
export interface UpdateRiskRecordRequest {
  riskLevel?: string;
  riskType?: string;
  description?: string;
  measure?: string;
  handler?: string;
  status?: string;
  remark?: string;
}
```

#### 业务规则

- `riskCode` 不可修改。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* RiskRecordResponse */ }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000403,
  "message": "风控记录不存在",
  "data": null
}
```

---

### 5.5 删除风控记录

**URL**：`DELETE /api/finance/risks/{id}`
**认证**：需要 JWT Bearer
**错误码**：`1000404`
**变更历史**：2026-04-12 | init | 架构师

#### 路径参数

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | ✅ | 风控记录 ID | `18940001` |

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

#### 响应示例（失败）

```json
{
  "code": 1000404,
  "message": "风控记录不存在",
  "data": null
}
```

---

### 5.6 导出风控记录 CSV

**URL**：`GET /api/finance/risks/export`
**认证**：需要 JWT Bearer
**错误码**：`1000405`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数（Query）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `keyword` | string | ❌ | 模糊匹配风险描述 | `逾期` |
| `riskLevel` | string | ❌ | 风险等级枚举 code | `HIGH` |
| `status` | string | ❌ | 处理状态枚举 code | `OPEN` |

#### 响应

**非 R\<T\> 包装**。直接返回文件下载：

- `Content-Type: text/csv;charset=UTF-8`
- `Content-Disposition: attachment; filename=风控记录.csv`
- 编码：UTF-8 BOM

CSV 列顺序：风控编号、关联类型、风险等级、风险类型、描述、处置措施、处理人、处理时间、状态。

---

## 6. 金融统计 FinanceStatisticsController（`/api/finance/statistics`）

### 6.1 贷款汇总统计

**URL**：`GET /api/finance/statistics/summary`
**认证**：需要 JWT Bearer
**错误码**：`1000500`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `FinanceSummaryResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `totalLoans` | number | ✅ | 贷款总数 |
| `totalAmount` | number | ✅ | 贷款总金额（精度 2 位） |
| `loansByType` | `Record<string, number>` | ✅ | 按贷款类型统计数量，key 为 LoanType code |
| `loansByStatus` | `Record<string, number>` | ✅ | 按状态统计数量，key 为 LoanStatus code |
| `overdueRate` | number \| null | ❌ | null（此接口不计算） |
| `riskByLevel` | `Record<string, number>` \| null | ❌ | null（此接口不计算） |

```typescript
export interface FinanceSummaryResponse {
  totalLoans: number;
  totalAmount: number;
  loansByType: Record<string, number>;
  loansByStatus: Record<string, number>;
  overdueRate: number | null;
  riskByLevel: Record<string, number> | null;
}
```

#### 业务规则

- 遍历全量贷款记录（无分页），状态为 null 的贷款不计入分类统计。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalLoans": 120,
    "totalAmount": 45800000.00,
    "loansByType": {
      "PLANT": 30,
      "WAREHOUSE": 50,
      "TRADE": 25,
      "EXPORT": 15
    },
    "loansByStatus": {
      "PENDING": 10,
      "APPROVED": 5,
      "DISBURSED": 80,
      "REPAID": 20,
      "OVERDUE": 5
    },
    "overdueRate": null,
    "riskByLevel": null
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000500,
  "message": "贷款汇总统计查询失败",
  "data": null
}
```

---

### 6.2 风险统计

**URL**：`GET /api/finance/statistics/risk`
**认证**：需要 JWT Bearer
**错误码**：`1000501`
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `FinanceRiskResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `totalLoans` | number \| null | ❌ | null（此接口不计算） |
| `totalAmount` | number \| null | ❌ | null（此接口不计算） |
| `loansByType` | `Record<string, number>` \| null | ❌ | null（此接口不计算） |
| `loansByStatus` | `Record<string, number>` \| null | ❌ | null（此接口不计算） |
| `overdueRate` | number | ✅ | 逾期率（0~1，如 `0.0417` 表示 4.17%） |
| `riskByLevel` | `Record<string, number>` | ✅ | 按信用等级统计有效评级数量，key 为 CreditLevel code |

```typescript
export interface FinanceRiskResponse {
  totalLoans: number | null;
  totalAmount: number | null;
  loansByType: Record<string, number> | null;
  loansByStatus: Record<string, number> | null;
  overdueRate: number;
  riskByLevel: Record<string, number>;
}
```

#### 业务规则

- `overdueRate` = 状态为 `OVERDUE` 的贷款数 / 全量贷款总数（无贷款时为 0）。
- `riskByLevel` 仅统计 `status = ACTIVE` 的信用评级记录，按 `creditLevel` 分组计数。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalLoans": null,
    "totalAmount": null,
    "loansByType": null,
    "loansByStatus": null,
    "overdueRate": 0.0417,
    "riskByLevel": {
      "A": 45,
      "B": 120,
      "C": 60,
      "D": 12
    }
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 1000501,
  "message": "风险统计查询失败",
  "data": null
}
```

---

## 7. 网关接口

`apple-module-finance-gateway` 模块定义了三个外部系统网关接口，通过 Spring 配置属性切换 Mock 实现和真实实现。

### 7.1 ContractGateway（电子合同网关）

**对接系统**：e签宝（生产环境）
**切换属性**：`finance.gateway.contract.real`（默认 `false`，使用 Mock）

| 方法 | 参数 | 返回 |
|------|------|------|
| `createContract` | orderId, partyAUid, partyBUid | `String` 外部合同编号 |
| `signContract` | contractNo, signerUid | `String` 签署后 PDF 地址 |

Mock 编号格式：`MOCK-CONTRACT-{orderId}-{6位序号}`

### 7.2 InvoiceGateway（发票网关）

**对接系统**：百望云 / 金税三期（生产环境）
**切换属性**：`finance.gateway.invoice.real`（默认 `false`，使用 Mock）

| 方法 | 参数 | 返回 |
|------|------|------|
| `issue` | orderId, amount, taxPayer, taxNo | `InvoiceResult`（invoiceNo, invoiceCode, pdfUrl, taxAmount） |

Mock 发票号格式：`MOCK-INV-{6位序号}`，税率 13%（农产品增值税）。

### 7.3 PaymentGateway（支付网关）

**对接系统**：微信支付 / 银联 / 银行转账（生产环境）
**切换属性**：`finance.gateway.payment.real`（默认 `false`，使用 Mock）

| 方法 | 参数 | 返回 |
|------|------|------|
| `createPayment` | orderId, amount, channel | `PaymentInit`（paymentNo, prepayParams） |
| `confirmPayment` | paymentNo | `String` 第三方交易号 |

Mock 支付单号格式：`MOCK-PAY-{orderId}-{6位序号}`。

### 网关配置汇总

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `finance.gateway.contract.real` | `false` | `true` 启用真实 e签宝，`false` 使用 Mock |
| `finance.gateway.invoice.real` | `false` | `true` 启用真实百望云，`false` 使用 Mock |
| `finance.gateway.payment.real` | `false` | `true` 启用真实微信支付，`false` 使用 Mock |

所有 Mock 实现均使用 `@ConditionalOnProperty(havingValue = "false", matchIfMissing = true)` 注解。

---

## 8. 状态机

### 8.1 贷款状态机 LoanStatus

```
                  ┌─────────┐
                  │ PENDING │
                  └────┬────┘
          ┌────────────┴────────────┐
          ▼                         ▼
     ┌──────────┐             ┌──────────┐
     │ APPROVED │             │ REJECTED │
     └────┬─────┘             └──────────┘
          │ disburse
          ▼
     ┌──────────┐
     │ DISBURSED│──── markOverdue ──▶ ┌─────────┐
     └────┬─────┘                     │ OVERDUE │
          │ repay (累计还清)            └─────────┘
          ▼
     ┌────────┐
     │ REPAID │
     └────┬───┘
          │ settle
          ▼
     ┌────────┐
     │SETTLED │
     └────────┘
```

### 8.2 质押状态机 PledgeStatus

```
┌─────────┐
│ PENDING │
└────┬────┘
     │ activate
     ▼
┌────────┐
│ ACTIVE │──── release ──▶ ┌──────────┐
└────┬───┘                 │ RELEASED │
     │                     └──────────┘
     │ markDefault
     ▼
┌───────────┐
│ DEFAULTED │
└───────────┘
```

---

## 9. 枚举声明

### 9.1 LoanType（贷款类型）

| code | desc |
|------|------|
| `PLEDGE` | 质押贷款 |
| `RECEIVABLE` | 应收款融资 |
| `CREDIT` | 信用贷款 |
| `PLANT` | 种植贷款 |
| `WAREHOUSE` | 仓单贷款 |
| `TRADE` | 贸易贷款 |
| `EXPORT` | 出口贷款 |

### 9.2 LoanStatus（贷款状态）

| code | desc |
|------|------|
| `PENDING` | 待审批 |
| `APPROVED` | 已审批 |
| `REJECTED` | 已拒绝 |
| `DISBURSED` | 已放款 |
| `REPAID` | 已还清 |
| `OVERDUE` | 已逾期 |
| `SETTLED` | 已结清 |

### 9.3 PledgeStatus（质押状态）

| code | desc |
|------|------|
| `PENDING` | 待激活 |
| `ACTIVE` | 生效中 |
| `RELEASED` | 已解除 |
| `DEFAULTED` | 已违约 |

### 9.4 CreditRatingStatus（信用评级状态）

| code | desc |
|------|------|
| `ACTIVE` | 有效 |
| `EXPIRED` | 已过期 |
| `REVOKED` | 已撤销 |

### 9.5 CreditLevel（信用等级）

| code | desc |
|------|------|
| `A` | A级（优秀） |
| `B` | B级（良好） |
| `C` | C级（一般） |
| `D` | D级（较差） |

### 9.6 RiskLevel（风险等级）

| code | desc |
|------|------|
| `HIGH` | 高风险 |
| `MEDIUM` | 中风险 |
| `LOW` | 低风险 |

### 9.7 RiskType（风险类型）

| code | desc |
|------|------|
| `OVERDUE` | 逾期风险 |
| `PRICE_DROP` | 价格下跌风险 |
| `QUALITY` | 质量风险 |
| `FRAUD` | 欺诈风险 |

### 9.8 RiskRecordStatus（风控处理状态）

| code | desc |
|------|------|
| `OPEN` | 待处理 |
| `HANDLING` | 处理中 |
| `RESOLVED` | 已解决 |
| `CLOSED` | 已关闭 |

### 9.9 EntityType（主体类型）

| code | desc |
|------|------|
| `FARMER` | 果农 |
| `SUPPLIER` | 供应商 |
| `COMPANY` | 企业 |

---

## 10. 错误码表

| 错误码 | HTTP 状态码 | 说明 |
|--------|-----------|------|
| `1000100` | 500 | 贷款列表查询失败 |
| `1000101` | 404 | 贷款不存在 |
| `1000102` | 400 | 贷款创建失败（参数校验不通过） |
| `1000103` | 409 | 已放款/已还清的贷款禁止修改 |
| `1000104` | 404 | 贷款不存在（更新时） |
| `1000105` | 409 | 仅待审批状态的贷款可以审批通过 |
| `1000106` | 409 | 仅待审批状态的贷款可以拒绝 |
| `1000107` | 409 | 仅已审批通过的贷款可以放款 |
| `1000108` | 400 | 还款金额必须大于0 |
| `1000109` | 409 | 仅已放款或逾期状态的贷款可以还款 |
| `1000110` | 409 | 仅已还清状态的贷款可以结清 |
| `1000111` | 409 | 仅已放款状态的贷款可以标记逾期 |
| `1000112` | 409 | 已放款的贷款禁止删除 |
| `1000113` | 404 | 贷款不存在（删除时） |
| `1000114` | 500 | 种植贷款申请失败 |
| `1000115` | 500 | 仓单贷款申请失败 |
| `1000116` | 500 | 贸易贷款申请失败 |
| `1000117` | 500 | 出口贷款申请失败 |
| `1000118` | 500 | 合同创建失败 |
| `1000119` | 500 | 发票开具失败 |
| `1000120` | 500 | 支付确认失败 |
| `1000121` | 500 | 贷款导出失败 |
| `1000200` | 500 | 质押列表查询失败 |
| `1000201` | 404 | 质押记录不存在 |
| `1000202` | 400 | 质押创建失败 |
| `1000203` | 409 | 生效中的质押禁止修改 |
| `1000204` | 404 | 质押记录不存在（更新时） |
| `1000205` | 409 | 仅待激活状态的质押可以激活 |
| `1000206` | 409 | 仅生效中的质押可以解除 |
| `1000207` | 409 | 仅生效中的质押可以标记违约 |
| `1000208` | 409 | 生效中的质押禁止删除 |
| `1000209` | 404 | 质押记录不存在（删除时） |
| `1000210` | 500 | 质押导出失败 |
| `1000300` | 500 | 信用评级列表查询失败 |
| `1000301` | 404 | 信用评级记录不存在 |
| `1000302` | 400 | 信用评级创建失败 |
| `1000303` | 404 | 信用评级记录不存在（更新时） |
| `1000304` | 404 | 信用评级记录不存在（删除时） |
| `1000305` | 404 | 评级主体不存在 |
| `1000306` | 500 | 信用评分计算失败 |
| `1000307` | 500 | 信用评级导出失败 |
| `1000400` | 500 | 风控记录列表查询失败 |
| `1000401` | 404 | 风控记录不存在 |
| `1000402` | 400 | 风控记录创建失败 |
| `1000403` | 404 | 风控记录不存在（更新时） |
| `1000404` | 404 | 风控记录不存在（删除时） |
| `1000405` | 500 | 风控记录导出失败 |
| `1000500` | 500 | 贷款汇总统计查询失败 |
| `1000501` | 500 | 风险统计查询失败 |

---

## 11. 实现注记

### 11.1 HTTP 方法审计结果

**已验证无 HTTP 方法问题。**

逐端点审计 5 个 Controller 共 41 个端点：

| Controller | 状态变更端点 | HTTP 方法 | 结论 |
|-----------|-------------|----------|------|
| LoanController | approve, reject, disburse, repay, settle, markOverdue, process-workflow | 全部 POST | 正确 |
| PledgeController | activate, release, markDefault | 全部 POST | 正确 |
| CreditRatingController | calculate | POST | 正确 |
| RiskRecordController | 无状态变更端点（仅 CRUD） | - | 正确 |
| FinanceStatisticsController | 无状态变更端点（仅查询） | - | 正确 |

CRUD 操作均使用标准 HTTP 方法（GET 查询、POST 创建、PUT 更新、DELETE 删除），语义正确。

### 11.2 String → EnumValue 迁移

当前后端 Entity 中以下字段为 `String` 类型，需迁移为 `EnumValue` 序列化（返回 `{code, desc}` 对象）：

| Entity | 字段 | 当前类型 | 目标类型 |
|--------|------|---------|---------|
| Loan | `loanType` | String | `EnumValue<LoanType>` |
| Loan | `borrowerType` | String | `EnumValue<EntityType>` |
| Loan | `status` | String | `EnumValue<LoanStatus>` |
| Pledge | `status` | String | `EnumValue<PledgeStatus>` |
| CreditRating | `entityType` | String | `EnumValue<EntityType>` |
| CreditRating | `creditLevel` | String | `EnumValue<CreditLevel>` |
| CreditRating | `status` | String | `EnumValue<CreditRatingStatus>` |
| RiskRecord | `riskLevel` | String | `EnumValue<RiskLevel>` |
| RiskRecord | `riskType` | String | `EnumValue<RiskType>` |
| RiskRecord | `status` | String | `EnumValue<RiskRecordStatus>` |

共 10 个 String 字段需迁移。迁移后前端需适配，读取 `status.code` 而非直接读 `status`。

### 11.3 Entity → Request/Response DTO 重命名

当前 5 个 Controller 全部直接返回 Entity 对象（Loan、Pledge、CreditRating、RiskRecord），不符合 CONVENTIONS.md DTO 命名约定。

**目标状态**：

| Controller | 当前返回 | 目标 Request | 目标 Response |
|-----------|---------|-------------|--------------|
| LoanController | `Loan` | `CreateLoanRequest` / `UpdateLoanRequest` | `LoanResponse` |
| PledgeController | `Pledge` | `CreatePledgeRequest` / `UpdatePledgeRequest` | `PledgeResponse` |
| CreditRatingController | `CreditRating` | `CreateCreditRatingRequest` / `UpdateCreditRatingRequest` | `CreditRatingResponse` |
| RiskRecordController | `RiskRecord` | `CreateRiskRecordRequest` / `UpdateRiskRecordRequest` | `RiskRecordResponse` |
| FinanceStatisticsController | `FinanceStatisticsVO` | 无（无入参） | `FinanceSummaryResponse` / `FinanceRiskResponse` |

**注意**：`FinanceStatisticsVO` 命名不符合约定（禁用 `VO` 后缀），需重命名为两个独立 Response DTO。

**迁移风险**：
- `FinanceStatisticsVO` 同时服务 summary 和 risk 两个接口，部分字段互为 null。建议拆分为 `FinanceSummaryResponse` 和 `FinanceRiskResponse`。
- Entity 字段 `createTime`/`updateTime`（BaseEntity 继承）需映射为 DTO 的 `createdAt`/`updatedAt`（CONVENTIONS.md 命名约定）。
- Entity 字段 `loanType` 等枚举字段迁移为 `EnumValue` 后，需在 DTO 转换层处理。

### 11.4 分页参数迁移 PageParam

当前各 Controller 直接使用 `@RequestParam int page, int size, ...`，目标迁移为全局 `PageParam` 对象。

### 11.5 跨模块依赖

| 依赖方向 | 说明 | 触发时机 |
|---------|------|---------|
| `finance → warehouse.md` | 仓单质押关联 `receiptId`，校验仓单存在性 | 创建质押时 |
| `finance → trade.md` | 贸易贷款关联订单融资 | 申请贸易贷款时 |
| `finance → user.md` | 农户信用评级关联农户信息 | 计算信用评分时 |
| `warehouse.md → finance` | 仓储模块引用仓单质押数据 | 关联契约 |
| `trade.md → finance` | 交易模块引用订单融资数据 | 关联契约 |

---

## 质量 Checklist

- [x] 41 个端点全覆盖（17+9+7+6+2）
- [x] 52 个错误码（>=20），全在 1000000-1099999 范围
- [x] 9 个枚举声明完整（LoanType / LoanStatus / PledgeStatus / CreditRatingStatus / CreditLevel / RiskLevel / RiskType / RiskRecordStatus / EntityType）
- [x] JSON 示例全用 `message` 字段（禁用 `msg`）
- [x] 分页使用 PageParam 语义 + PageResult\<T\> 结构
- [x] 4 个 CSV 导出（loans/pledges/credits/risks）明确标注非 R\<T\> 包装
- [x] 贷款状态机（PENDING→APPROVED→DISBURSED→REPAID→SETTLED，PENDING→REJECTED，DISBURSED→OVERDUE）入文档（§8.1）
- [x] 质押状态机（PENDING→ACTIVE→RELEASED/DEFAULTED）入文档（§8.2）
- [x] 网关接口（ContractGateway/InvoiceGateway/PaymentGateway）入文档（§7）
- [x] 信用评分算法（三维度加权 + 等级映射）入文档（§4.6）
- [x] HTTP 方法审计：5 个 Controller 全部 41 端点已验证，无 GET 用于状态变更（§11.1）
- [x] 10 个 String→EnumValue 迁移清单（§11.2）
- [x] Entity→Request/Response DTO 重命名（§11.3）
- [x] 跨模块依赖（warehouse/trade/user）入文档（§11.5）
