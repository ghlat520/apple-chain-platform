# 微信支付 Mock 模块契约

> **演示原型，不对接真实微信支付。**
> 后端返回假 `prepayId` / `transactionId` / `refundId` 及 Mock 账单数据。
> 真实对接需商户号 + API 证书，本模块保留接口形状但内部逻辑为模拟。

**模块包路径：** `com.apple.chain.trade.wxpay`
**表：** `td_wx_payment`, `td_wx_refund`（Flyway `V29__wx_payment_refund.sql`）
**前端路由：** `/trade/wxpay/payments`, `/trade/wxpay/reconcile`

## 1. 金额单位铁律

后端所有金额字段单位为 **分（cents）**，类型 `Long`。
前端展示时统一除以 100，`¥YY.XX`（元，保留 2 位）。
禁止在后端使用 `BigDecimal` 元；禁止在前端把分当作元展示。

## 2. 实体字段

### `WxPayment`（td_wx_payment）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 雪花 ID |
| outTradeNo | String(64) | 商户订单号，唯一 |
| orderId | Long | 关联业务订单 ID（可为空） |
| amountCents | Long | 金额（分） |
| status | WxPaymentStatus | 支付状态 |
| payMethod | String(16) | 固定 `WECHAT` |
| prepayId | String(64) | Mock 预支付会话 ID |
| transactionId | String(64) | Mock 微信支付单号（仅 SUCCESS/REFUNDED 有） |
| payTime | LocalDateTime | 支付完成时间 |
| description | String(255) | 商品描述 |
| createTime / updateTime / createBy / deleted | 继承 BaseEntity | |

### `WxRefund`（td_wx_refund）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 雪花 ID |
| outRefundNo | String(64) | 商户退款单号，唯一 |
| paymentId | Long | 关联支付记录 ID |
| refundAmountCents | Long | 退款金额（分），≤ 原支付金额 |
| reason | String(255) | 退款原因 |
| status | WxRefundStatus | 退款状态 |
| refundId | String(64) | Mock 微信退款单号 |
| refundTime | LocalDateTime | 退款完成时间 |
| createTime / updateTime / createBy / deleted | 继承 BaseEntity | |

## 3. 枚举

### `WxPaymentStatus`（BaseEnum，序列化为 `{code, desc}`）

| name | code | desc |
|------|------|------|
| PENDING | 1 | 待支付 |
| SUCCESS | 2 | 支付成功 |
| FAILED | 3 | 支付失败 |
| REFUNDED | 4 | 已退款 |

### `WxRefundStatus`

| name | code | desc |
|------|------|------|
| PENDING | 1 | 退款处理中 |
| SUCCESS | 2 | 退款成功 |
| FAILED | 3 | 退款失败 |

## 4. REST 接口

### 4.1 支付

#### `POST /api/wxpay/payments`
创建 Mock 支付。初始状态 `PENDING`，返回 `prepayId` 供前端渲染二维码占位图（`qrCodeUrl` 为 `mock://...`）。

Request (JSON body):
```json
{
  "orderId": 1001,
  "amountCents": 12800,
  "description": "烟台红富士 100kg"
}
```

Response data:
```json
{
  "id": "9001",
  "outTradeNo": "WXPAY20260414100001...",
  "orderId": "1001",
  "amountCents": 12800,
  "status": { "code": 1, "desc": "待支付" },
  "payMethod": "WECHAT",
  "prepayId": "wx20260414...mock...",
  "transactionId": null,
  "qrCodeUrl": "mock://wxpay/qrcode/WXPAY...",
  "payTime": null,
  "createTime": "2026-04-14 10:00:00",
  "description": "烟台红富士 100kg"
}
```

#### `GET /api/wxpay/payments/{id}`
查询支付记录。若当前状态为 `PENDING`，按 **90% 概率** 推进为 `SUCCESS`（写入 `transactionId` + `payTime`），10% 概率保持 `PENDING`（演示轮询）。终态幂等。

#### `GET /api/wxpay/payments?page=&size=&keyword=&status=`
分页列表。`status` 取枚举 name（PENDING/SUCCESS/FAILED/REFUNDED）或留空。`keyword` 模糊匹配 `outTradeNo` / `transactionId` / `description`。

### 4.2 退款

#### `POST /api/wxpay/refunds`
Mock 退款，立即置 `SUCCESS`；同时将原支付记录状态同步为 `REFUNDED`。

Request (JSON body):
```json
{
  "paymentId": 9046,
  "refundAmountCents": 31200,
  "reason": "果品到货质量不符"
}
```

Response data:
```json
{
  "id": "9501",
  "outRefundNo": "WXRFD20260414...",
  "paymentId": "9046",
  "refundAmountCents": 31200,
  "reason": "果品到货质量不符",
  "status": { "code": 2, "desc": "退款成功" },
  "refundId": "5000020260414...",
  "refundTime": "2026-04-14 11:00:00",
  "createTime": "2026-04-14 10:55:00"
}
```

错误场景：
- 原支付非 `SUCCESS` → 400 "仅支付成功状态可退款..."
- `refundAmountCents > amountCents` → 400 "退款金额不能超过原支付金额"
- `refundAmountCents <= 0` → 400

#### `GET /api/wxpay/refunds/{id}`
查询退款详情。

#### `GET /api/wxpay/refunds?paymentId={paymentId}`
按支付记录查询其退款明细（数组）。

### 4.3 对账

#### `GET /api/wxpay/reconcile?date=YYYY-MM-DD`
按日聚合当日 `SUCCESS` 支付 + `SUCCESS` 退款，返回明细 + 汇总。

Response data:
```json
{
  "billDate": "2026-04-14",
  "paymentCount": 4,
  "paymentTotalCents": 158800,
  "refundCount": 1,
  "refundTotalCents": 58400,
  "netAmountCents": 100400,
  "payments": [ /* WxPayResponse[] */ ],
  "refunds": [ /* WxRefundResponse[] */ ]
}
```

## 5. 安全与限制

- 本模块 **不** 验证签名，**不** 对接真实微信商户平台。
- `qrCodeUrl` 是 `mock://` 前缀的假链接，前端渲染为占位图即可。
- 演示数据见 `V29` 迁移脚本 50 条种子记录。
- 本模块不会引入 `wechatpay-java` / `weixin-java-pay` 等 SDK。
