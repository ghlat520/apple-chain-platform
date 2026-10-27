# M4 交易管理模块 API 文档

> 模块路径：`apple-module-trade`
> 基础路径：`/api/trade`
> 生成日期：2026-04-11

---

## 目录

1. [供货管理 SupplyInfoController](#1-供货管理-supplyinfocontroller)
2. [采购需求 PurchaseNeedController](#2-采购需求-purchaseneedcontroller)
3. [交易订单 TradeOrderController](#3-交易订单-tradeordercontroller)
4. [质量检验 QualityInspectionController](#4-质量检验-qualityinspectioncontroller)
5. [撮合议价 MatchController (M7)](#5-撮合议价-matchcontroller-m7)
6. [合同支付开票 M8WorkflowController (M8)](#6-合同支付开票-m8workflowcontroller-m8)
7. [交易统计 TradeStatisticsController](#7-交易统计-tradestatisticscontroller)
8. [实体字段说明](#8-实体字段说明)

---

## 1. 供货管理 SupplyInfoController

**Base URL**：`/api/trade/supply`

---

### 1.1 供货列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trade/supply/list` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | — | 关键词搜索 |
| variety | String | 否 | — | 品种筛选 |
| status | String | 否 | — | 状态筛选 |
| farmerId | Long | 否 | — | 按农户筛选 |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 123456789,
        "farmerId": 100001,
        "variety": "红富士",
        "quantity": 5000.0,
        "unitPrice": 5.50,
        "quality": "A",
        "status": "DRAFT",
        "availableDate": "2026-04-15"
      }
    ],
    "total": 30,
    "size": 10,
    "current": 1
  }
}
```

---

### 1.2 供货详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trade/supply/{id}` |

---

### 1.3 创建供货信息

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trade/supply` |

**Request Body**：SupplyInfo 实体 JSON

**业务规则**

- 初始状态为 `DRAFT`

---

### 1.4 更新供货信息

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/trade/supply/{id}` |

---

### 1.5 发布供货信息

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/trade/supply/{id}/publish` |

**状态机**：`DRAFT` → `PUBLISHED`

---

### 1.6 删除供货信息

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/trade/supply/{id}` |

---

### 1.7 导出供货信息 CSV

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trade/supply/export` |

---

## 2. 采购需求 PurchaseNeedController

**Base URL**：`/api/trade/need`

---

### 2.1 采购需求列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trade/need/list` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | — | 需求编号模糊搜索 |
| variety | String | 否 | — | 品种筛选 |
| status | String | 否 | — | 状态筛选：`DRAFT` / `PUBLISHED` |
| buyerId | Long | 否 | — | 按买家筛选 |

---

### 2.2 采购需求详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trade/need/{id}` |

---

### 2.3 创建采购需求

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trade/need` |

**业务规则**

- 自动生成 `needNo`：格式 `NED` + `yyyyMMdd` + 4位序列号
- 初始状态为 `DRAFT`

---

### 2.4 更新采购需求

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/trade/need/{id}` |

**业务规则**

- 更新时保留原 `needNo`，不允许修改编号

---

### 2.5 发布采购需求

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/trade/need/{id}/publish` |

**状态机**：`DRAFT` → `PUBLISHED`

**业务规则**

- 只有 `DRAFT` 状态可以发布

---

### 2.6 删除采购需求

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/trade/need/{id}` |

---

### 2.7 导出采购需求 CSV

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trade/need/export` |

---

## 3. 交易订单 TradeOrderController

**Base URL**：`/api/trade/order`

---

### 3.1 订单列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trade/order/list` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | — | 订单号模糊搜索 |
| status | String | 否 | — | 订单状态筛选 |
| farmerId | Long | 否 | — | 按农户筛选 |
| buyerId | Long | 否 | — | 按买家筛选 |

---

### 3.2 订单详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trade/order/{id}` |

---

### 3.3 创建交易订单

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trade/order` |

**Request Body**

```json
{
  "supplyId": 100001,
  "needId": 200001,
  "farmerId": 300001,
  "buyerId": 400001,
  "variety": "红富士",
  "quantity": 1000.0,
  "unitPrice": 5.50,
  "tradeDate": "2026-04-11",
  "deliveryDate": "2026-04-15",
  "remark": ""
}
```

**业务规则**

- 自动生成 `orderNo`：格式 `ORD` + `yyyyMMdd` + 序列号
- 自动计算 `totalAmount` = `quantity` * `unitPrice`
- 初始 `orderStatus` = `DRAFT`，`paymentStatus` = `PENDING`

---

### 3.4 确认订单

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/trade/order/{id}/confirm` |

**状态机**：`DRAFT` → `CONFIRMED`

---

### 3.5 标记发货

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/trade/order/{id}/deliver` |

**状态机**：`CONFIRMED` → `DELIVERED`

---

### 3.6 完成订单

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/trade/order/{id}/complete` |

**状态机**：`DELIVERED` → `COMPLETED`

---

### 3.7 取消订单

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/trade/order/{id}/cancel` |

**状态机**：`DRAFT`/`CONFIRMED` → `CANCELLED`

---

### 3.8 导出订单 CSV

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trade/order/export` |

---

## 4. 质量检验 QualityInspectionController

**Base URL**：`/api/trade/inspection`

---

### 4.1 创建质检单

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trade/inspection` |

**Request Body**

```json
{
  "orderId": 100001,
  "supplyId": 200001,
  "variety": "红富士",
  "grade": "A",
  "inspectionDate": "2026-04-11"
}
```

**业务规则**

- 自动生成 `inspectionNo`：格式 `QI` + `yyyyMMdd` + 4位序列号
- 初始状态为 `PENDING`
- `inspectionDate` 默认当天

---

### 4.2 质检列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trade/inspection` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| orderId | Long | 否 | — | 按订单筛选 |
| status | String | 否 | — | 状态筛选：`PENDING` / `INSPECTED` / `ACCEPTED` / `DISPUTED` |
| result | String | 否 | — | 结果筛选：`PASS` / `FAIL` |

---

### 4.3 质检详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trade/inspection/{id}` |

---

### 4.4 录入检验结果

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/trade/inspection/{id}/inspect` |

**Request Body**

```json
{
  "brixValue": 13.5,
  "firmnessValue": 7.2,
  "colorScore": 85.0,
  "defectRate": 2.0,
  "inspectorName": "质检员张三",
  "reportUrl": "/reports/qi-20260411-001.pdf"
}
```

**状态机**：`PENDING` → `INSPECTED`

**自动判定规则**

| 指标 | 阈值 | 判定 |
|------|------|------|
| 糖度 (brixValue) | ≥ 12 | 低于则 FAIL |
| 硬度 (firmnessValue) | ≥ 6 | 低于则 FAIL |
| 色泽评分 (colorScore) | ≥ 70 | 低于则 FAIL |
| 缺陷率 (defectRate) | ≤ 5% | 超过则 FAIL |

- 所有指标通过 → `PASS`
- 任一指标不通过 → `FAIL`

---

### 4.5 验收通过

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/trade/inspection/{id}/accept` |

**状态机**：`INSPECTED` → `ACCEPTED`

**业务规则**

- 只有 `INSPECTED` 状态可以验收

---

### 4.6 发起争议

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/trade/inspection/{id}/dispute` |

**Request Body**

```json
{
  "reason": "糖度检测结果与实际不符，要求复检"
}
```

**状态机**：`INSPECTED` → `DISPUTED`

**业务规则**

- 只有 `INSPECTED` 状态可以发起争议
- 争议原因记录到 `remark` 字段

---

## 5. 撮合议价 MatchController (M7)

**Base URL**：`/api/trade`

---

### 5.1 为某供应计算 top-K 撮合候选

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trade/match/compute` |
| 权限 | `trade:write` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| supplyId | Long | 是 | — | 供应ID |
| topK | int | 否 | 10 | 返回候选数量 |

**业务规则**

- 根据品种匹配、价格区间、数量等维度计算撮合分数
- 结果缓存供后续查询

---

### 5.2 查询某供应的 top-K 缓存撮合

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trade/match/supply/{supplyId}/top` |
| 权限 | `trade:read` |

---

### 5.3 发起议价

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trade/negotiation/start` |
| 权限 | `trade:write` |

**Request Body**：StartNegotiationRequest

```json
{
  "matchId": 1001,
  "supplyUserId": 300001,
  "demandUserId": 400001,
  "price": 5.20,
  "quantity": 800
}
```

---

### 5.4 报价/还价

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trade/negotiation/{id}/offer` |
| 权限 | `trade:write` |

**Request Body**：OfferRequest

```json
{
  "byUserId": 300001,
  "price": 5.30,
  "quantity": 900
}
```

---

### 5.5 接受当前报价 → 自动建单

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trade/negotiation/{id}/accept` |
| 权限 | `trade:write` |

**Request Body**：AcceptRequest

```json
{
  "byUserId": 400001
}
```

**业务规则**

- 接受报价后自动创建交易订单（TradeOrder）
- 返回创建的订单对象

---

### 5.6 取消议价

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trade/negotiation/{id}/cancel` |
| 权限 | `trade:write` |

---

### 5.7 发送聊天消息

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trade/chat/send` |
| 权限 | `trade:write` |

**Request Body**：SendChatRequest

```json
{
  "sessionId": "neg-123",
  "fromUserId": 300001,
  "toUserId": 400001,
  "msgType": "TEXT",
  "content": "这个价格可以商量"
}
```

---

### 5.8 拉取会话历史消息

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trade/chat/{sessionId}/history` |
| 权限 | `trade:read` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| limit | int | 否 | 100 | 返回消息数量 |

---

### 5.9 标记会话内消息为已读

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trade/chat/{sessionId}/read` |
| 权限 | `trade:read` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

---

## 6. 合同支付开票 M8WorkflowController (M8)

**Base URL**：`/api/trade`

> 外部接口（电子合同、第三方支付、税务开票）均为 **Mock 实现**，通过 `finance-gateway` 模块的 `@ConditionalOnProperty` 切换。

---

### 6.1 创建合同

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trade/contract/create` |
| 权限 | `trade:write` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |
| partyA | Long | 是 | 甲方用户ID |
| partyB | Long | 是 | 乙方用户ID |

---

### 6.2 签署合同

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trade/contract/{orderId}/sign` |
| 权限 | `trade:write` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| signerUid | Long | 是 | 签署人用户ID |

---

### 6.3 创建支付

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trade/payment/create` |
| 权限 | `trade:write` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| orderId | Long | 是 | — | 订单ID |
| channel | String | 否 | WECHAT | 支付渠道：`WECHAT` / `ALIPAY` / `BANK` |

---

### 6.4 支付成功回调

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trade/payment/callback/wechat` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| paymentNo | String | 是 | 支付流水号 |

**业务规则**

- Mock 模式下直接确认支付
- 真实模式需验证微信签名

---

### 6.5 手动开票

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/trade/invoice/issue` |
| 权限 | `trade:write` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |
| taxPayer | String | 是 | 开票方名称 |
| taxNo | String | 是 | 纳税人识别号 |

---

## 7. 交易统计 TradeStatisticsController

**Base URL**：`/api/trade/statistics`

---

### 7.1 交易汇总统计

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trade/statistics/summary` |

**响应结构**：`TradeStatisticsVO`

```json
{
  "totalOrders": 150,
  "totalVolume": 75000.0,
  "totalAmount": 412500.00,
  "avgUnitPrice": 5.50,
  "ordersByStatus": {
    "DRAFT": 10,
    "CONFIRMED": 20,
    "DELIVERED": 30,
    "COMPLETED": 80,
    "CANCELLED": 10
  }
}
```

---

### 7.2 品种维度统计

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trade/statistics/variety` |

**响应结构**：`List<VarietyStats>`

| 字段 | 类型 | 说明 |
|------|------|------|
| variety | String | 品种 |
| volume | BigDecimal | 总成交量（kg） |
| amount | BigDecimal | 总成交额（元） |
| avgPrice | BigDecimal | 平均单价 |

---

### 7.3 月度趋势统计

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trade/statistics/trend` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| months | int | 否 | 6 | 统计近N个月 |

**响应结构**：`List<MonthlyStats>`

| 字段 | 类型 | 说明 |
|------|------|------|
| month | String | 月份（如 2026-03） |
| volume | BigDecimal | 成交量 |
| amount | BigDecimal | 成交额 |
| orderCount | long | 订单数 |

---

### 7.4 品种价格指数

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/trade/statistics/price-index` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| variety | String | 是 | — | 品种名称 |
| days | int | 否 | 30 | 统计近N天 |

---

## 8. 实体字段说明

### TradeOrder（交易订单）— 表 `td_trade_order`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 雪花ID |
| orderNo | String | 订单号：ORD+yyyyMMdd+seq |
| supplyId | Long | 供应ID |
| needId | Long | 采购需求ID |
| farmerId | Long | 农户ID |
| buyerId | Long | 买家ID |
| variety | String | 品种 |
| quantity | BigDecimal | 数量（kg） |
| unitPrice | BigDecimal | 单价（元/kg） |
| totalAmount | BigDecimal | 总金额 = quantity * unitPrice |
| tradeDate | LocalDate | 交易日期 |
| deliveryDate | LocalDate | 交货日期 |
| paymentStatus | String | 支付状态：`PENDING` / `PAID` / `REFUNDED` |
| orderStatus | String | 订单状态：`DRAFT` / `CONFIRMED` / `DELIVERED` / `COMPLETED` / `CANCELLED` |
| contractFile | String | 合同文件路径（MinIO） |
| remark | String | 备注 |

### QualityInspection（质量检验）— 表 `td_quality_inspection`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 雪花ID |
| inspectionNo | String | 质检单号：QI+yyyyMMdd+seq |
| orderId | Long | 关联订单ID |
| supplyId | Long | 关联供应ID |
| variety | String | 品种 |
| grade | String | 等级 |
| brixValue | BigDecimal | 糖度（阈值 ≥ 12） |
| firmnessValue | BigDecimal | 硬度（阈值 ≥ 6） |
| colorScore | BigDecimal | 色泽评分（阈值 ≥ 70） |
| defectRate | BigDecimal | 缺陷率%（阈值 ≤ 5） |
| inspectorName | String | 质检员姓名 |
| inspectionDate | LocalDate | 质检日期 |
| reportUrl | String | 质检报告路径 |
| result | String | 结果：`PASS` / `FAIL` / `CONDITIONAL` |
| status | String | 状态：`PENDING` / `INSPECTED` / `ACCEPTED` / `DISPUTED` |
| remark | String | 备注 |

---

## 状态机

### 订单状态流转

```
DRAFT ──→ CONFIRMED ──→ DELIVERED ──→ COMPLETED
  │            │
  └────────────┴──→ CANCELLED
```

### 支付状态流转

```
PENDING ──→ PAID ──→ REFUNDED
```

### 质检状态流转

```
PENDING ──→ INSPECTED ──→ ACCEPTED
                      ──→ DISPUTED
```

### 供货/需求状态

```
DRAFT ──→ PUBLISHED
```

### 议价状态

```
NEGOTIATING ──→ ACCEPTED（自动创建订单）
             ──→ CANCELLED
```
