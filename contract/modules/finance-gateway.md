# P2-11 金融网关 (Finance Gateway) 模块契约

> 模块：`apple-module-finance-gateway`
> URL 前缀：无（本模块不暴露 REST 端点，仅供内部服务调用）
> 错误码段：`1000000 ~ 1099999`（继承 finance 模块号段，CONVENTIONS.md:142 权威）
> 最近更新：2026-04-12
> 权威源：本文件
> 后端实现：`apple-module-finance-gateway`
> 关联契约：`finance.md`（调用方，§7 引用本模块三个网关）/ `user.md`（签约主体）

---

## 1. 模块定位与边界

`apple-module-finance-gateway` 是 `apple-module-finance` 的子模块，负责封装与外部金融系统的通信：

| 网关 | 对接系统（生产环境） | 职责 |
|------|-------------------|------|
| ContractGateway | e签宝（或法大大） | 电子合同创建、签署 |
| InvoiceGateway | 百望云 / 金税三期 | 增值税发票开具 |
| PaymentGateway | 微信支付 / 银联 / 银行转账 | 预支付创建、支付确认 |

### 与 finance.md 的边界

| 维度 | finance.md | finance-gateway.md |
|------|-----------|-------------------|
| REST 端点 | 41 个（Controller） | 0 个（无 Controller） |
| 错误码 | `1000100` ~ `1000501`（52 个） | 无独立错误码；调用失败由 finance 模块抛出 `1000118`/`1000119`/`1000120` |
| 调用方式 | HTTP REST | Spring Bean 注入（`@Autowired`） |
| Mock/Real 切换 | 无 | 通过 `ConditionalOnProperty` 切换 |

---

## 2. 接口索引（0 REST 端点）

本模块不暴露任何 REST 端点。全部功能通过 Spring Bean 接口提供，由 `apple-module-finance` 的 `LoanController#processWorkflow` 调用。

---

## 3. ContractGateway（电子合同网关）

**接口**：`com.apple.chain.finance.gateway.ContractGateway`
**切换属性**：`finance.gateway.contract.real`（默认 `false`，使用 Mock）
**生产对接**：e签宝 SDK

### 3.1 createContract -- 创建合同草稿

```java
String createContract(Long orderId, Long partyAUid, Long partyBUid);
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orderId` | Long | ✅ | 业务订单 ID（如贷款 ID） |
| `partyAUid` | Long | ✅ | 甲方用户 ID |
| `partyBUid` | Long | ✅ | 乙方用户 ID |

**返回**：`String` -- 外部合同编号（由 e签宝分配）

**Mock 实现**：`MockESignContractGateway`
- 编号格式：`MOCK-CONTRACT-{orderId}-{6位序号}`
- 序号自增（`AtomicLong`），全局唯一
- 示例：`MOCK-CONTRACT-18910001-000001`

**调用方**：`LoanController#processWorkflow` -- 合同步骤

### 3.2 signContract -- 签署合同

```java
String signContract(String contractNo, Long signerUid);
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `contractNo` | String | ✅ | 外部合同编号（`createContract` 返回值） |
| `signerUid` | Long | ✅ | 签署人用户 ID |

**返回**：`String` -- 已签署 PDF 的 URL

**Mock 实现**：`MockESignContractGateway`
- URL 格式：`mock://contracts/{contractNo}/{signerUid}.pdf`
- 示例：`mock://contracts/MOCK-CONTRACT-18910001-000001/1001.pdf`

**调用方**：`LoanController#processWorkflow` -- 合同步骤

---

## 4. InvoiceGateway（发票网关）

**接口**：`com.apple.chain.finance.gateway.InvoiceGateway`
**切换属性**：`finance.gateway.invoice.real`（默认 `false`，使用 Mock）
**生产对接**：百望云 / 金税三期

### 4.1 InvoiceResult -- 发票结果内部类

```java
record InvoiceResult(
    String invoiceNo,      // 发票号码
    String invoiceCode,    // 发票代码
    String pdfUrl,         // PDF 下载地址
    BigDecimal taxAmount   // 税额
) {}
```

### 4.2 issue -- 开具增值税发票

```java
InvoiceResult issue(Long orderId, BigDecimal amount, String taxPayer, String taxNo);
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orderId` | Long | ✅ | 业务订单 ID |
| `amount` | BigDecimal | ✅ | 开票金额（含税） |
| `taxPayer` | String | ✅ | 纳税人名称 |
| `taxNo` | String | ✅ | 纳税人识别号 |

**返回**：`InvoiceResult` -- 发票号码、代码、PDF URL、税额

**Mock 实现**：`MockBaiwangInvoiceGateway`
- 发票号码格式：`MOCK-INV-{6位序号}`
- 发票代码格式：`MOCK-CODE-{orderId}`
- PDF URL 格式：`mock://invoices/{invoiceNo}.pdf`
- 税率：13%（农产品增值税标准税率）
- 税额计算：`amount * 0.13`，精度 2 位，四舍五入
- 示例：金额 100.00 -> 税额 13.00

**生产注意**：真实实现可能耗时最长 30 秒（百望云同步接口），Mock 立即返回。

**调用方**：`LoanController#processWorkflow` -- 发票步骤

---

## 5. PaymentGateway（支付网关）

**接口**：`com.apple.chain.finance.gateway.PaymentGateway`
**切换属性**：`finance.gateway.payment.real`（默认 `false`，使用 Mock）
**生产对接**：微信支付 / 银联 / 银行转账

### 5.1 PaymentInit -- 支付初始化结果内部类

```java
record PaymentInit(
    String paymentNo,      // 内部支付单号
    String prepayParams    // 预支付参数（二维码、跳转 URL 等，不透明字符串）
) {}
```

### 5.2 createPayment -- 创建预支付

```java
PaymentInit createPayment(Long orderId, BigDecimal amount, String channel);
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orderId` | Long | ✅ | 业务订单 ID |
| `amount` | BigDecimal | ✅ | 支付金额 |
| `channel` | String | ✅ | 支付渠道，见 PaymentChannel 枚举 |

**返回**：`PaymentInit` -- 支付单号 + 预支付参数

**Mock 实现**：`MockWechatPaymentGateway`
- 支付单号格式：`MOCK-PAY-{orderId}-{6位序号}`
- 预支付参数格式：`mock://prepay?pn={paymentNo}&amt={amount}&ch={channel}`
- 序号自增（`AtomicLong`），全局唯一

**调用方**：`LoanController#processWorkflow` -- 支付步骤

### 5.3 confirmPayment -- 确认支付

```java
String confirmPayment(String paymentNo);
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `paymentNo` | String | ✅ | 支付单号（`createPayment` 返回的 `PaymentInit.paymentNo`） |

**返回**：`String` -- 第三方交易号

**Mock 实现**：`MockWechatPaymentGateway`
- 交易号格式：`MOCK-WX-TRADE-{paymentNo}`
- 示例：`MOCK-WX-TRADE-MOCK-PAY-18910001-000001`

**调用方**：`LoanController#processWorkflow` -- 支付步骤（以及回调处理器）

---

## 6. 配置属性汇总

| 配置项 | 默认值 | `true` 时行为 | `false` 时行为 |
|--------|--------|-------------|--------------|
| `finance.gateway.contract.real` | `false` | 使用真实 e签宝 SDK | 使用 `MockESignContractGateway` |
| `finance.gateway.invoice.real` | `false` | 使用真实百望云 API | 使用 `MockBaiwangInvoiceGateway` |
| `finance.gateway.payment.real` | `false` | 使用真实微信支付 SDK | 使用 `MockWechatPaymentGateway` |

**实现机制**：所有 Mock 类使用 `@ConditionalOnProperty(havingValue = "false", matchIfMissing = true)` 注解，`matchIfMissing = true` 确保未配置属性时默认走 Mock。

---

## 7. 工作流调用链

finance 模块的 `POST /api/finance/loans/{id}/process-workflow` 按以下顺序调用本模块三个网关：

```
processWorkflow(loanId)
  │
  ├─ Step 1: ContractGateway.createContract(loanId, borrowerId, 1L)
  │           ContractGateway.signContract(contractNo, borrowerId)
  │
  ├─ Step 2: InvoiceGateway.issue(loanId, amount, borrowerName, "UNKNOWN")
  │
  └─ Step 3: PaymentGateway.createPayment(loanId, amount, "BANK_TRANSFER")
             PaymentGateway.confirmPayment(paymentNo)
```

任一步骤失败抛出 `BizException`，前端收到的错误码来自 finance 模块（`1000118` / `1000119` / `1000120`），详见 [`finance.md`](./finance.md) §2.16。

---

## 8. 枚举声明

### 8.1 PaymentChannel（支付渠道）

| code | desc | 说明 |
|------|------|------|
| `WECHAT` | 微信支付 | 扫码 / H5 |
| `ALIPAY` | 支付宝 | 扫码 / H5 |
| `BANK_TRANSFER` | 银行转账 | B2B 大额 |
| `UNIONPAY` | 银联 | 线上快捷 |
| `OFFLINE` | 线下转账 | 财务确认 |

> **注意**：`PaymentChannel` 当前未定义为 Java 枚举类，仅在 `processWorkflow` 中硬编码为 `"BANK_TRANSFER"` 字符串。生产对接时需提取为正式枚举。

---

## 9. 错误码表

本模块无独立错误码。调用失败时由 finance 模块抛出以下错误码：

| 错误码 | HTTP 状态码 | 说明 | 触发网关 |
|--------|-----------|------|---------|
| `1000118` | 500 | 合同创建失败 | ContractGateway |
| `1000119` | 500 | 发票开具失败 | InvoiceGateway |
| `1000120` | 500 | 支付确认失败 | PaymentGateway |

完整错误码表见 [`finance.md`](./finance.md) §10。

---

## 10. 实现注记

### 10.1 模块结构

```
apple-module-finance-gateway/
├── src/main/java/com/apple/chain/finance/gateway/
│   ├── ContractGateway.java           # 接口：电子合同
│   ├── InvoiceGateway.java            # 接口：发票
│   ├── PaymentGateway.java            # 接口：支付
│   └── impl/
│       ├── MockESignContractGateway.java   # Mock 实现
│       ├── MockBaiwangInvoiceGateway.java  # Mock 实现
│       └── MockWechatPaymentGateway.java   # Mock 实现
└── src/test/java/com/apple/chain/finance/gateway/
    └── MockGatewayTest.java           # 3 个单元测试
```

- 3 个接口 + 3 个 Mock 实现 + 1 个测试类
- 无 Controller、无 Entity、无 Mapper、无 REST 端点
- 纯粹的 SPI（Service Provider Interface）模块

### 10.2 生产实现 TODO

| 网关 | 真实实现需完成 | 依赖 |
|------|-------------|------|
| ContractGateway | e签宝 SDK 集成（`createContract` + `signContract`） | e签宝 Java SDK + MinIO 存储 PDF |
| InvoiceGateway | 百望云 API 集成（同步开票，最长 30s） | 百望云 Java SDK |
| PaymentGateway | 微信支付 SDK 集成（JSAPI/Native） | 微信支付 Java SDK + 回调处理器 |

真实实现类需使用 `@ConditionalOnProperty(name = "xxx.real", havingValue = "true")` 注册，与 Mock 互斥。

### 10.3 测试覆盖

`MockGatewayTest` 包含 3 个测试用例：

| 测试方法 | 覆盖内容 |
|---------|---------|
| `contractGateway` | 唯一性 contractNo 生成 + PDF URL 格式 |
| `paymentGateway` | PaymentInit 字段 + confirmPayment 交易号 |
| `invoiceGateway` | 13% VAT 税额计算 + 字段格式 |

---

## 质量 Checklist

- [x] 3 个网关接口全覆盖（ContractGateway / InvoiceGateway / PaymentGateway）
- [x] 3 个 Mock 实现全覆盖
- [x] 0 个 REST 端点（纯 SPI 模块，无 Controller）
- [x] 错误码无独立号段（继承 finance 模块 `1000000 ~ 1099999`）
- [x] 与 finance.md 边界清晰（§1 边界表格）
- [x] 工作流调用链文档化（§7）
- [x] 配置属性汇总（§6）
- [x] 生产实现 TODO 列出（§10.2）
- [x] 枚举声明（1 个：PaymentChannel，§8.1）
