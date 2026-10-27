# M7 金融服务模块 API 文档

**基础路径**：`/api/finance`  
**模块**：`apple-module-finance`  
**网关层**：`apple-module-finance-gateway`  
**版本**：2026-04-11  

---

## 目录

1. [贷款管理（LoanController）](#1-贷款管理)
2. [仓单质押（PledgeController）](#2-仓单质押)
3. [信用评级（CreditRatingController）](#3-信用评级)
4. [风控管理（RiskRecordController）](#4-风控管理)
5. [金融统计（FinanceStatisticsController）](#5-金融统计)
6. [实体字段说明](#6-实体字段说明)
7. [网关接口（Gateway）](#7-网关接口)

---

## 通用响应结构

所有接口（导出 CSV 除外）均返回统一包装体：

```json
{
  "code": 200,
  "message": "ok",
  "data": { ... }
}
```

分页接口的 `data` 字段格式：

```json
{
  "records": [ ... ],
  "total": 100,
  "size": 10,
  "current": 1
}
```

---

## 1. 贷款管理

**Controller**：`LoanController`  
**Tag**：贷款管理  
**Base Path**：`/api/finance/loans`

---

### 1.1 贷款列表（分页）

**GET** `/api/finance/loans/list`

查询贷款记录列表，支持关键词、贷款类型、状态过滤，按创建时间倒序排列。

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | - | 模糊匹配贷款编号或借款人名称 |
| loanType | String | 否 | - | 贷款类型：`PLEDGE` / `RECEIVABLE` / `CREDIT` / `PLANT` / `WAREHOUSE` / `TRADE` / `EXPORT` |
| status | String | 否 | - | 状态：`PENDING` / `APPROVED` / `REJECTED` / `DISBURSED` / `REPAID` / `OVERDUE` / `SETTLED` |

#### 响应

```json
{
  "code": 200,
  "data": {
    "records": [ { /* Loan 对象，见第6节 */ } ],
    "total": 50,
    "size": 10,
    "current": 1
  }
}
```

---

### 1.2 贷款详情

**GET** `/api/finance/loans/{id}`

#### 路径参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 贷款ID |

#### 响应

```json
{
  "code": 200,
  "data": { /* Loan 对象 */ }
}
```

**业务规则**：ID 不存在时返回业务异常（NOT_FOUND）。

---

### 1.3 创建贷款申请

**POST** `/api/finance/loans`

#### 请求体（JSON）

`Loan` 对象，见第6节字段说明。`loanCode`、`status`、`repaidAmount` 由系统自动生成，无需传入。

#### 响应

返回创建后的完整 `Loan` 对象。

**业务规则**：
- 系统自动生成贷款编号，格式：`LN{yyyyMMdd}{4位序号}`，例如 `LN202604110001`。
- 初始状态固定为 `PENDING`。
- `repaidAmount` 初始值为 `0`。

---

### 1.4 更新贷款

**PUT** `/api/finance/loans/{id}`

#### 路径参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 贷款ID |

#### 请求体（JSON）

`Loan` 对象（只修改传入字段，`loanCode` 不可修改）。

#### 业务规则

- 状态为 `DISBURSED`（已放款）或 `REPAID`（已还清）的贷款**禁止修改**。

---

### 1.5 审批通过

**POST** `/api/finance/loans/{id}/approve`

#### 路径参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 贷款ID |

#### 业务规则

- 仅允许 `PENDING` → `APPROVED` 状态转换。
- 自动记录 `approveDate` 为当日。

---

### 1.6 审批拒绝

**POST** `/api/finance/loans/{id}/reject`

#### 路径参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 贷款ID |

#### 业务规则

- 仅允许 `PENDING` → `REJECTED` 状态转换。
- 自动记录 `approveDate` 为当日。

---

### 1.7 放款

**POST** `/api/finance/loans/{id}/disburse`

#### 路径参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 贷款ID |

#### 业务规则

- 仅允许 `APPROVED` → `DISBURSED` 状态转换。
- 自动记录 `disburseDate` 为当日。
- 自动计算 `dueDate` = 放款日期 + `termMonths` 个月。

---

### 1.8 还款

**POST** `/api/finance/loans/{id}/repay`

#### 路径参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 贷款ID |

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| amount | BigDecimal | 是 | 本次还款金额 |

#### 业务规则

- 累计 `repaidAmount` += 本次还款金额。
- 当 `repaidAmount >= amount`（贷款总额）时，状态自动切换为 `REPAID`。

---

### 1.9 结清贷款

**POST** `/api/finance/loans/{id}/settle`

#### 路径参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 贷款ID |

#### 业务规则

- 仅允许 `REPAID` → `SETTLED` 状态转换。

---

### 1.10 标记逾期

**POST** `/api/finance/loans/{id}/overdue`

#### 路径参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 贷款ID |

#### 业务规则

- 仅允许 `DISBURSED` → `OVERDUE` 状态转换。

---

### 1.11 删除贷款

**DELETE** `/api/finance/loans/{id}`

#### 业务规则

- 状态为 `DISBURSED`（已放款）的贷款**禁止删除**。

---

### 1.12 申请种植贷款

**POST** `/api/finance/loans/apply/plant`

申请苹果种植环节专属贷款，与创建贷款接口一致，但系统自动将 `loanType` 设置为 `PLANT`。

#### 请求体（JSON）

`Loan` 对象。如需传入种植贷款专属数据，使用 `productSpecificData` 字段（JSON 字符串）。

---

### 1.13 申请仓单贷款

**POST** `/api/finance/loans/apply/warehouse`

系统自动将 `loanType` 设置为 `WAREHOUSE`。

---

### 1.14 申请贸易贷款

**POST** `/api/finance/loans/apply/trade`

系统自动将 `loanType` 设置为 `TRADE`。

---

### 1.15 申请出口贷款

**POST** `/api/finance/loans/apply/export`

系统自动将 `loanType` 设置为 `EXPORT`。

---

### 1.16 贷款工作流（合同 + 发票 + 支付）

**POST** `/api/finance/loans/{id}/process-workflow`

对指定贷款依次执行三步自动化工作流：创建并签署电子合同、开具增值税发票、创建支付会话并确认支付。

#### 路径参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 贷款ID |

#### 响应

```json
{
  "code": 200,
  "message": "工作流处理成功",
  "data": null
}
```

#### 业务规则

工作流三步骤顺序执行，任一步骤失败将抛出业务异常：

1. **合同步骤**：调用 `ContractGateway.createContract(loanId, borrowerId, 1L)` 创建合同草稿，再调用 `signContract(contractNo, borrowerId)` 完成签署，返回签署后 PDF 地址。
2. **发票步骤**：调用 `InvoiceGateway.issue(loanId, amount, borrowerName, "UNKNOWN")` 开具增值税发票。
3. **支付步骤**：调用 `PaymentGateway.createPayment(loanId, amount, "BANK_TRANSFER")` 创建预支付，再调用 `confirmPayment(paymentNo)` 确认支付，返回第三方交易号。

当前实现均为 Mock 网关，详见[第7节](#7-网关接口)。

---

### 1.17 导出贷款 CSV

**GET** `/api/finance/loans/export`

导出符合条件的所有贷款为 CSV 文件（UTF-8 BOM 编码）。

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 否 | 同列表接口 |
| loanType | String | 否 | 同列表接口 |
| status | String | 否 | 同列表接口 |

#### 响应

HTTP 文件下载，`Content-Type: text/csv;charset=UTF-8`，文件名：`贷款记录.csv`。

CSV 列顺序：贷款编号、借款人、类型、贷款类型、金额、利率、期限(月)、申请日期、审批日期、放款日期、到期日期、已还金额、状态。

---

### 贷款状态机

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

---

## 2. 仓单质押

**Controller**：`PledgeController`  
**Tag**：仓单质押  
**Base Path**：`/api/finance/pledges`

---

### 2.1 质押列表（分页）

**GET** `/api/finance/pledges/list`

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | - | 模糊匹配质押编号或出质人名称 |
| status | String | 否 | - | 状态：`PENDING` / `ACTIVE` / `RELEASED` / `DEFAULTED` |

---

### 2.2 质押详情

**GET** `/api/finance/pledges/{id}`

---

### 2.3 创建质押

**POST** `/api/finance/pledges`

#### 请求体（JSON）

`Pledge` 对象，见第6节字段说明。`pledgeCode`、`status`、`loanAmount` 由系统自动生成。

**业务规则**：
- 系统自动生成质押编号，格式：`PL{yyyyMMdd}{4位序号}`，例如 `PL202604110001`。
- 初始状态为 `PENDING`。
- 若同时传入 `appraisedValue`（评估价值）和 `pledgeRate`（质押率），系统自动计算 `loanAmount = appraisedValue × pledgeRate`。

---

### 2.4 更新质押

**PUT** `/api/finance/pledges/{id}`

**业务规则**：状态为 `ACTIVE`（生效中）的质押**禁止修改**。`pledgeCode` 不可修改。

---

### 2.5 激活质押

**POST** `/api/finance/pledges/{id}/activate`

**业务规则**：仅允许 `PENDING` → `ACTIVE` 状态转换，自动记录 `startDate` 为当日。

---

### 2.6 解除质押

**POST** `/api/finance/pledges/{id}/release`

**业务规则**：仅允许 `ACTIVE` → `RELEASED` 状态转换，自动记录 `endDate` 为当日。

---

### 2.7 标记质押违约

**POST** `/api/finance/pledges/{id}/default`

**业务规则**：仅允许 `ACTIVE` → `DEFAULTED` 状态转换。

---

### 2.8 删除质押

**DELETE** `/api/finance/pledges/{id}`

**业务规则**：状态为 `ACTIVE`（生效中）的质押**禁止删除**。

---

### 2.9 导出质押 CSV

**GET** `/api/finance/pledges/export`

#### 请求参数（Query）

| 参数名 | 类型 | 说明 |
|--------|------|------|
| keyword | String | 同列表接口 |
| status | String | 同列表接口 |

文件名：`仓单质押.csv`。

CSV 列顺序：质押编号、仓单编码、出质人、质权人、质押物、数量、单位、评估价值、质押率、可贷金额、起始日、到期日、状态。

---

### 质押状态机

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

## 3. 信用评级

**Controller**：`CreditRatingController`  
**Tag**：信用评级  
**Base Path**：`/api/finance/credits`

---

### 3.1 信用评级列表（分页）

**GET** `/api/finance/credits/list`

按 `creditScore` 倒序排列。

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | - | 模糊匹配主体名称 |
| entityType | String | 否 | - | 主体类型，如 `FARMER`、`SUPPLIER`、`COMPANY` 等 |
| creditLevel | String | 否 | - | 信用等级：`A` / `B` / `C` / `D` |

---

### 3.2 信用评级详情

**GET** `/api/finance/credits/{id}`

---

### 3.3 创建信用评级

**POST** `/api/finance/credits`

#### 请求体（JSON）

`CreditRating` 对象，见第6节字段说明。

**业务规则**：初始状态固定为 `ACTIVE`。

---

### 3.4 更新信用评级

**PUT** `/api/finance/credits/{id}`

---

### 3.5 删除信用评级

**DELETE** `/api/finance/credits/{id}`

---

### 3.6 计算信用评分

**POST** `/api/finance/credits/{entityId}/calculate`

对指定主体自动计算信用评分并创建新的评级记录。

#### 路径参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| entityId | Long | 主体ID（农户/供应商/企业等） |

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| entityType | String | 是 | 主体类型 |

#### 响应

返回新创建的 `CreditRating` 对象，包含计算后的各维度分值和综合信用等级。

#### 评分算法

| 维度 | 取值范围 | 权重 |
|------|----------|------|
| 交易评分（tradeScore） | 60 ~ 95 | 40% |
| 生产评分（productionScore） | 50 ~ 90 | 30% |
| 财务评分（financialScore） | 55 ~ 85 | 30% |

**综合评分** = `tradeScore × 0.4 + productionScore × 0.3 + financialScore × 0.3`（四舍五入取整）

**信用等级映射**：

| 综合评分 | 信用等级 |
|----------|----------|
| ≥ 85 | A |
| 70 ~ 84 | B |
| 55 ~ 69 | C |
| < 55 | D |

**业务规则**：
- 评级有效期为评定日期起 1 年（`validUntil = assessmentDate + 1年`）。
- 初始状态为 `ACTIVE`。
- 当前评分数据使用模拟随机生成，生产环境应对接真实数据模型。

---

### 3.7 导出信用评级 CSV

**GET** `/api/finance/credits/export`

#### 请求参数（Query）

| 参数名 | 类型 | 说明 |
|--------|------|------|
| keyword | String | 同列表接口 |
| entityType | String | 同列表接口 |
| creditLevel | String | 同列表接口 |

文件名：`信用评级.csv`。

CSV 列顺序：主体类型、主体名称、信用评分、信用等级、评定日期、有效期至、交易分、生产分、财务分、状态。

---

### 信用评级状态枚举

| 状态值 | 含义 |
|--------|------|
| ACTIVE | 有效 |
| EXPIRED | 已过期 |
| REVOKED | 已撤销 |

---

## 4. 风控管理

**Controller**：`RiskRecordController`  
**Tag**：风控管理  
**Base Path**：`/api/finance/risks`

---

### 4.1 风控记录列表（分页）

**GET** `/api/finance/risks/list`

按创建时间倒序排列，关键词模糊匹配 `description`（风险描述）字段。

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | - | 模糊匹配风险描述 |
| riskLevel | String | 否 | - | 风险等级：`HIGH` / `MEDIUM` / `LOW` |
| status | String | 否 | - | 处理状态：`OPEN` / `HANDLING` / `RESOLVED` / `CLOSED` |

---

### 4.2 风控记录详情

**GET** `/api/finance/risks/{id}`

---

### 4.3 创建风控记录

**POST** `/api/finance/risks`

#### 请求体（JSON）

`RiskRecord` 对象，见第6节字段说明。`riskCode`、`status` 由系统自动生成。

**业务规则**：
- 系统自动生成风控编号，格式：`RK{yyyyMMdd}{4位序号}`，例如 `RK202604110001`。
- 初始状态固定为 `OPEN`。

---

### 4.4 更新风控记录

**PUT** `/api/finance/risks/{id}`

`riskCode` 不可修改。

---

### 4.5 删除风控记录

**DELETE** `/api/finance/risks/{id}`

---

### 4.6 导出风控记录 CSV

**GET** `/api/finance/risks/export`

#### 请求参数（Query）

| 参数名 | 类型 | 说明 |
|--------|------|------|
| keyword | String | 同列表接口 |
| riskLevel | String | 同列表接口 |
| status | String | 同列表接口 |

文件名：`风控记录.csv`。

CSV 列顺序：风控编号、关联类型、风险等级、风险类型、描述、处置措施、处理人、处理时间、状态。

---

### 风控记录状态说明

| 状态值 | 含义 |
|--------|------|
| OPEN | 待处理 |
| HANDLING | 处理中 |
| RESOLVED | 已解决 |
| CLOSED | 已关闭 |

### 风险类型枚举

| 类型值 | 含义 |
|--------|------|
| OVERDUE | 逾期风险 |
| PRICE_DROP | 价格下跌风险 |
| QUALITY | 质量风险 |
| FRAUD | 欺诈风险 |

---

## 5. 金融统计

**Controller**：`FinanceStatisticsController`  
**Tag**：金融统计  
**Base Path**：`/api/finance/statistics`

---

### 5.1 贷款汇总统计

**GET** `/api/finance/statistics/summary`

汇总全量贷款数据，返回总数、总金额及按类型、按状态的分布。

#### 无请求参数

#### 响应示例

```json
{
  "code": 200,
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

**业务规则**：遍历全量贷款记录（无分页），状态为 null 的贷款不计入分类统计。

---

### 5.2 风险统计

**GET** `/api/finance/statistics/risk`

返回贷款逾期率及当前有效（ACTIVE）信用评级按等级分布。

#### 无请求参数

#### 响应示例

```json
{
  "code": 200,
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

**业务规则**：
- `overdueRate` = 状态为 `OVERDUE` 的贷款数 / 全量贷款总数（无贷款时为 0）。
- `riskByLevel` 仅统计 `status = 'ACTIVE'` 的信用评级记录，按 `creditLevel` 分组计数。

---

## 6. 实体字段说明

### 6.1 Loan（贷款）

表名：`sf_loan`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 主键（BaseEntity 继承） |
| createTime | LocalDateTime | 创建时间（BaseEntity 继承） |
| updateTime | LocalDateTime | 更新时间（BaseEntity 继承） |
| loanCode | String | 贷款编号，系统自动生成，格式 `LN{yyyyMMdd}{4位序号}` |
| borrowerName | String | 借款人名称 |
| borrowerType | String | 借款人类型（如 FARMER、SUPPLIER、COMPANY） |
| borrowerId | Long | 借款人ID |
| loanType | String | 贷款类型：`PLEDGE`（质押贷款）/ `RECEIVABLE`（应收款融资）/ `CREDIT`（信用贷款）/ `PLANT`（种植贷款）/ `WAREHOUSE`（仓单贷款）/ `TRADE`（贸易贷款）/ `EXPORT`（出口贷款） |
| amount | BigDecimal | 贷款金额 |
| interestRate | BigDecimal | 年利率 |
| termMonths | Integer | 贷款期限（月） |
| applyDate | LocalDate | 申请日期 |
| approveDate | LocalDate | 审批日期（系统自动填写） |
| disburseDate | LocalDate | 放款日期（系统自动填写） |
| dueDate | LocalDate | 到期日期（系统自动计算：放款日期 + termMonths 月） |
| repaidAmount | BigDecimal | 已还款金额（累计，系统自动累加） |
| status | String | 状态，见状态机 |
| pledgeId | Long | 关联质押ID（质押贷款时使用） |
| productSpecificData | String | 特定产品附属数据（JSON 字符串，用于种植/仓单/贸易/出口贷款专属字段） |
| remark | String | 备注 |

---

### 6.2 Pledge（仓单质押）

表名：`sf_pledge`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 主键 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| pledgeCode | String | 质押编号，系统自动生成，格式 `PL{yyyyMMdd}{4位序号}` |
| receiptId | Long | 关联仓单ID |
| receiptCode | String | 仓单编码 |
| pledgorName | String | 出质人名称 |
| pledgeeName | String | 质权人名称 |
| commodity | String | 质押物名称（苹果品种等） |
| quantity | BigDecimal | 质押数量 |
| unit | String | 数量单位（如 kg、吨） |
| appraisedValue | BigDecimal | 评估价值 |
| pledgeRate | BigDecimal | 质押率（如 0.70 表示 70%） |
| loanAmount | BigDecimal | 可贷金额 = appraisedValue × pledgeRate（创建时自动计算） |
| startDate | LocalDate | 质押起始日（激活时自动填写） |
| endDate | LocalDate | 质押到期日（解除时自动填写） |
| status | String | 状态，见状态机 |
| remark | String | 备注 |

---

### 6.3 CreditRating（信用评级）

表名：`sf_credit_rating`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 主键 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| entityType | String | 主体类型（如 FARMER、SUPPLIER、COMPANY） |
| entityId | Long | 主体ID |
| entityName | String | 主体名称 |
| creditScore | Integer | 综合信用评分（0~100） |
| creditLevel | String | 信用等级：`A` / `B` / `C` / `D` |
| assessmentDate | LocalDate | 评定日期 |
| validUntil | LocalDate | 有效期至 |
| tradeScore | Integer | 交易评分 |
| productionScore | Integer | 生产评分 |
| financialScore | Integer | 财务评分 |
| assessor | String | 评定人 |
| status | String | `ACTIVE`（有效）/ `EXPIRED`（已过期）/ `REVOKED`（已撤销） |
| remark | String | 备注 |

---

### 6.4 RiskRecord（风控记录）

表名：`sf_risk_record`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 主键 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| riskCode | String | 风控编号，系统自动生成，格式 `RK{yyyyMMdd}{4位序号}` |
| relatedType | String | 关联业务类型（如 LOAN、PLEDGE） |
| relatedId | Long | 关联业务ID |
| riskLevel | String | 风险等级：`HIGH` / `MEDIUM` / `LOW` |
| riskType | String | 风险类型：`OVERDUE` / `PRICE_DROP` / `QUALITY` / `FRAUD` |
| description | String | 风险描述 |
| measure | String | 处置措施 |
| handler | String | 处理人 |
| handleTime | LocalDateTime | 处理时间 |
| status | String | `OPEN` / `HANDLING` / `RESOLVED` / `CLOSED` |
| remark | String | 备注 |

---

### 6.5 FinanceStatisticsVO（统计结果）

| 字段名 | 类型 | 说明 | 接口 |
|--------|------|------|------|
| totalLoans | Long | 贷款总数 | summary |
| totalAmount | BigDecimal | 贷款总金额 | summary |
| loansByType | Map&lt;String, Long&gt; | 按贷款类型统计数量 | summary |
| loansByStatus | Map&lt;String, Long&gt; | 按状态统计数量 | summary |
| overdueRate | Double | 逾期率（0~1） | risk |
| riskByLevel | Map&lt;String, Long&gt; | 按信用等级统计有效评级数量 | risk |

---

## 7. 网关接口

`apple-module-finance-gateway` 模块定义了三个外部系统网关接口，通过 Spring 配置属性切换 Mock 实现和真实实现。

---

### 7.1 ContractGateway（电子合同网关）

**对接系统**：e签宝（生产环境）  
**切换属性**：`finance.gateway.contract.real`（默认 `false`，使用 Mock）

#### 接口方法

**createContract**

| 参数 | 类型 | 说明 |
|------|------|------|
| orderId | Long | 业务单据ID |
| partyAUid | Long | 甲方用户ID |
| partyBUid | Long | 乙方用户ID |

返回：`String`，外部合同编号。

**signContract**

| 参数 | 类型 | 说明 |
|------|------|------|
| contractNo | String | 合同编号 |
| signerUid | Long | 签署人用户ID |

返回：`String`，签署后 PDF 文件地址（MinIO `mock-contracts/` 路径或真实 URL）。

#### Mock 实现（MockESignContractGateway）

- `createContract` 返回格式：`MOCK-CONTRACT-{orderId}-{6位序号}`
- `signContract` 返回格式：`mock://contracts/{contractNo}/{signerUid}.pdf`
- 使用原子计数器保证序号全局唯一，不依赖 MinIO（测试友好）。
- 激活条件：`finance.gateway.contract.real=false`（或不配置）。

---

### 7.2 InvoiceGateway（发票网关）

**对接系统**：百望云 / 金税三期（生产环境）  
**切换属性**：`finance.gateway.invoice.real`（默认 `false`，使用 Mock）

#### 接口方法

**issue**

| 参数 | 类型 | 说明 |
|------|------|------|
| orderId | Long | 业务单据ID |
| amount | BigDecimal | 开票金额（含税） |
| taxPayer | String | 纳税人名称 |
| taxNo | String | 纳税人识别号 |

返回：`InvoiceResult` 记录类：

| 字段 | 类型 | 说明 |
|------|------|------|
| invoiceNo | String | 发票号码 |
| invoiceCode | String | 发票代码 |
| pdfUrl | String | 发票 PDF 地址 |
| taxAmount | BigDecimal | 税额 |

**注意**：真实实现可能耗时最长 30 秒；Mock 立即返回。

#### Mock 实现（MockBaiwangInvoiceGateway）

- `invoiceNo` 格式：`MOCK-INV-{6位序号}`
- `invoiceCode` 格式：`MOCK-CODE-{orderId}`
- `taxAmount` = `amount × 0.13`（四舍五入保留2位，采用农产品适用的 13% 增值税率）
- `pdfUrl` 格式：`mock://invoices/{invoiceNo}.pdf`
- 激活条件：`finance.gateway.invoice.real=false`（或不配置）。

---

### 7.3 PaymentGateway（支付网关）

**对接系统**：微信支付 / 银联 / 银行转账（生产环境）  
**切换属性**：`finance.gateway.payment.real`（默认 `false`，使用 Mock）

#### 接口方法

**createPayment**

| 参数 | 类型 | 说明 |
|------|------|------|
| orderId | Long | 业务单据ID |
| amount | BigDecimal | 支付金额 |
| channel | String | 支付渠道：`BANK_TRANSFER` / `WECHAT` / `UNIONPAY` 等 |

返回：`PaymentInit` 记录类：

| 字段 | 类型 | 说明 |
|------|------|------|
| paymentNo | String | 内部支付单号 |
| prepayParams | String | 不透明预支付参数（二维码、跳转 URL 等，格式依渠道而定） |

**confirmPayment**

| 参数 | 类型 | 说明 |
|------|------|------|
| paymentNo | String | 内部支付单号 |

返回：`String`，第三方交易号（回调处理和 Mock 模拟均使用此接口）。

#### Mock 实现（MockWechatPaymentGateway）

- `paymentNo` 格式：`MOCK-PAY-{orderId}-{6位序号}`
- `prepayParams` 格式：`mock://prepay?pn={paymentNo}&amt={amount}&ch={channel}`
- `confirmPayment` 返回：`MOCK-WX-TRADE-{paymentNo}`
- 激活条件：`finance.gateway.payment.real=false`（或不配置）。

---

### 网关配置汇总

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `finance.gateway.contract.real` | `false` | `true` 启用真实 e签宝，`false` 使用 Mock |
| `finance.gateway.invoice.real` | `false` | `true` 启用真实百望云，`false` 使用 Mock |
| `finance.gateway.payment.real` | `false` | `true` 启用真实微信支付，`false` 使用 Mock |

所有 Mock 实现均使用 `@ConditionalOnProperty(havingValue = "false", matchIfMissing = true)` 注解，即未配置时默认启用 Mock。
