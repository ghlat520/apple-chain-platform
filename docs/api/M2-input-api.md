# M2 农资投入品模块 API 文档

> 模块路径：`apple-module-input`  
> 基础路径：`/api/input`  
> 生成日期：2026-04-11

---

## 目录

1. [农资供应商 AgriSupplierController](#1-农资供应商-agrisuppliercontroller)
2. [农资产品 AgriProductController](#2-农资产品-agriproductcontroller)
3. [农资采购 AgriPurchaseController](#3-农资采购-agripurchasecontroller)
4. [农资库存 AgriInventoryController](#4-农资库存-agriinventorycontroller)
5. [农资使用记录 AgriUsageController](#5-农资使用记录-agriusagecontroller)
6. [农资溯源链路 AgriTraceController](#6-农资溯源链路-agritracecontroller)
7. [实体字段说明](#7-实体字段说明)

---

## 1. 农资供应商 AgriSupplierController

**Base URL**：`/api/input/suppliers`

---

### 1.1 供应商列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/suppliers/list` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | — | 供应商名称模糊搜索 |
| status | String | 否 | — | 状态筛选：`PENDING` / `APPROVED` / `REJECTED` / `BLACKLISTED` |

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [ { ...AgriSupplier... } ],
    "total": 100,
    "size": 10,
    "current": 1
  }
}
```

**业务规则**

- 按 `createTime` 降序排列。
- `keyword` 匹配供应商名称（`LIKE %keyword%`）；`status` 精确匹配。

---

### 1.2 供应商详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/suppliers/{id}` |

**Path 参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 供应商 ID |

**响应**：`R<AgriSupplier>` — 返回完整供应商对象；不存在时抛出 `NOT_FOUND`。

---

### 1.3 创建供应商

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/input/suppliers` |

**Request Body**：`AgriSupplier`（JSON）

**业务规则**

- 创建时 `status` 强制设置为 `PENDING`，忽略请求中传入的状态值。
- 供应商初始进入待审核状态，须经审核才可使用。

**响应**：`R<AgriSupplier>` — 返回已创建的供应商对象（含自增 ID）。

---

### 1.4 更新供应商

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/input/suppliers/{id}` |

**Path 参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 供应商 ID |

**Request Body**：`AgriSupplier`（JSON，仅传需要更新的字段）

**业务规则**

- 若供应商不存在，抛出 `NOT_FOUND`。
- 强制使用 path 中的 `id`，忽略 body 中的 `id` 字段。

**响应**：`R<AgriSupplier>` — 返回更新后的完整供应商对象。

---

### 1.5 删除供应商

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/input/suppliers/{id}` |

**Path 参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 供应商 ID |

**业务规则**：若供应商不存在，抛出 `NOT_FOUND`。

**响应**：`R<Void>`，msg = "删除成功"。

---

### 1.6 审核供应商

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/input/suppliers/{id}/audit` |

**Path 参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 供应商 ID |

**Request Body**（JSON）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| decision | String | 是 | 审核决策：`APPROVE` / `REJECT` / `BLACKLIST` |
| reason | String | 否 | 拒绝原因（decision=REJECT 时建议填写） |

**状态机转换**

```
任意状态
  ├── decision=APPROVE    → APPROVED
  ├── decision=REJECT     → REJECTED（同时写入 rejectReason）
  └── decision=BLACKLIST  → BLACKLISTED
```

**业务规则**

- `auditTime` 自动设置为当前时间。
- `decision` 不区分大小写（统一转大写后匹配）。
- 无效的 decision 值抛出业务异常。

**响应**：`R<AgriSupplier>` — 返回审核后的供应商对象。

---

### 1.7 重新提交审核

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/input/suppliers/{id}/reinstate` |

**Path 参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 供应商 ID |

**状态机转换**

```
REJECTED → PENDING（清空 rejectReason）
```

**业务规则**：仅 `REJECTED` 状态的供应商可执行此操作，否则抛出业务异常。

**响应**：`R<AgriSupplier>` — 返回重置后的供应商对象。

---

### 1.8 导出供应商 CSV

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/suppliers/export` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 否 | 供应商名称模糊搜索 |
| status | String | 否 | 状态筛选 |

**响应**：`text/csv; charset=UTF-8` 文件流，文件名 `农资供应商.csv`（BOM UTF-8）。

CSV 列顺序：`供应商编码, 名称, 联系人, 电话, 地址, 营业执照号, 资质证书, 信用评分, 状态`

---

## 2. 农资产品 AgriProductController

**Base URL**：`/api/input/products`

---

### 2.1 农资产品列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/products/list` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | — | 产品名称模糊搜索 |
| type | String | 否 | — | 产品类型：`FERTILIZER` / `PESTICIDE` / `SEED` / `TOOL` |
| status | String | 否 | — | 状态：`ACTIVE` / `DISCONTINUED` |

**响应**：`R<PageResult<AgriProduct>>`，按 `createTime` 降序。

---

### 2.2 农资产品详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/products/{id}` |

**Path 参数**：`id`（Long）

**响应**：`R<AgriProduct>`；不存在时抛出 `NOT_FOUND`。

---

### 2.3 创建农资产品

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/input/products` |

**Request Body**：`AgriProduct`（JSON）

**业务规则**：创建时 `status` 强制设置为 `ACTIVE`，忽略请求传入的状态值。

**响应**：`R<AgriProduct>` — 返回已创建的产品对象。

---

### 2.4 更新农资产品

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/input/products/{id}` |

**Path 参数**：`id`（Long）

**Request Body**：`AgriProduct`（JSON）

**业务规则**：若产品不存在，抛出 `NOT_FOUND`；强制使用 path 中的 `id`。

**响应**：`R<AgriProduct>` — 返回更新后的完整产品对象。

---

### 2.5 删除农资产品

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/input/products/{id}` |

**Path 参数**：`id`（Long）

**业务规则**：若产品不存在，抛出 `NOT_FOUND`。

**响应**：`R<Void>`，msg = "删除成功"。

---

### 2.6 导出农资产品 CSV

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/products/export` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 否 | 产品名称模糊搜索 |
| type | String | 否 | 产品类型筛选 |
| status | String | 否 | 状态筛选 |

**响应**：`text/csv; charset=UTF-8` 文件流，文件名 `农资产品.csv`（BOM UTF-8）。

CSV 列顺序：`产品编码, 产品名称, 类型, 生产厂家, 规格, 批号, 生产日期, 保质期至, 登记证号, 状态`

---

## 3. 农资采购 AgriPurchaseController

**Base URL**：`/api/input/purchases`

---

### 3.1 采购列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/purchases/list` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | — | 采购单号模糊搜索 |
| status | String | 否 | — | 状态：`PENDING` / `APPROVED` / `RECEIVED` / `CANCELLED` |
| farmerId | Long | 否 | — | 按农户 ID 筛选 |

**响应**：`R<PageResult<AgriPurchase>>`，按 `createTime` 降序。

---

### 3.2 采购详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/purchases/{id}` |

**Path 参数**：`id`（Long）

**响应**：`R<AgriPurchase>`；不存在时抛出 `NOT_FOUND`。

---

### 3.3 创建采购单

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/input/purchases` |

**Request Body**：`AgriPurchase`（JSON）

**业务规则**

- 创建时 `status` 强制设置为 `PENDING`。
- 若请求未传 `totalAmount`，但 `quantity` 和 `unitPrice` 均不为空，则自动计算：`totalAmount = quantity × unitPrice`。

**响应**：`R<AgriPurchase>` — 返回已创建的采购单对象。

---

### 3.4 更新采购单

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/input/purchases/{id}` |

**Path 参数**：`id`（Long）

**Request Body**：`AgriPurchase`（JSON）

**业务规则**

- 已取消（`CANCELLED`）的采购单不允许修改，抛出业务异常。
- `purchaseNo` 字段不可通过更新接口修改，服务端强制置空后忽略。

**响应**：`R<AgriPurchase>` — 返回更新后的采购单对象。

---

### 3.5 删除采购单

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/input/purchases/{id}` |

**Path 参数**：`id`（Long）

**业务规则**：已收货（`RECEIVED`）的采购单不能删除，抛出业务异常。

**响应**：`R<Void>`，msg = "删除成功"。

---

### 3.6 审批采购单

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/input/purchases/{id}/approve` |

**Path 参数**：`id`（Long）

**状态机转换**

```
PENDING → APPROVED
```

**业务规则**：仅 `PENDING` 状态的采购单可执行审批，否则抛出业务异常。

**响应**：`R<AgriPurchase>` — 返回审批后的采购单对象。

---

### 3.7 确认收货

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/input/purchases/{id}/receive` |

**Path 参数**：`id`（Long）

**状态机转换**

```
APPROVED → RECEIVED
```

**业务规则**

- 仅 `APPROVED` 状态的采购单可确认收货，否则抛出业务异常。
- 确认收货后，**自动更新对应产品的库存**：按 `productId` 查找 `agri_inventory` 中的第一条记录，将 `stockQuantity` 增加本次采购的 `quantity`，并重新计算库存状态（见[库存状态机](#库存状态机)）。

**响应**：`R<AgriPurchase>` — 返回收货后的采购单对象。

---

### 3.8 取消采购单

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/input/purchases/{id}/cancel` |

**Path 参数**：`id`（Long）

**状态机转换**

```
PENDING → CANCELLED
```

**业务规则**：仅 `PENDING` 状态的采购单可取消，否则抛出业务异常。

**响应**：`R<AgriPurchase>` — 返回取消后的采购单对象。

---

### 采购单状态机汇总

```
                ┌─────────────────────┐
                │        创建         │
                └──────────┬──────────┘
                           │ status=PENDING
                           ▼
                       PENDING
                      /        \
          approve /              \ cancel
                 /                \
            APPROVED          CANCELLED（终态）
                |
         receive |
                 |
            RECEIVED（终态）
```

---

### 3.9 导出采购记录 CSV

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/purchases/export` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 否 | 采购单号模糊搜索 |
| status | String | 否 | 状态筛选 |
| farmerId | Long | 否 | 按农户筛选 |

**响应**：`text/csv; charset=UTF-8` 文件流，文件名 `农资采购.csv`（BOM UTF-8）。

CSV 列顺序：`采购单号, 产品名称, 供应商, 数量, 单位, 单价, 总金额, 采购日期, 状态`

---

## 4. 农资库存 AgriInventoryController

**Base URL**：`/api/input/inventory`

---

### 4.1 库存列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/inventory/list` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | — | 产品名称模糊搜索 |
| status | String | 否 | — | 库存状态：`NORMAL` / `LOW` / `EMPTY` / `OVERSTOCKED` |
| farmerId | Long | 否 | — | 按农户 ID 筛选 |

**响应**：`R<PageResult<AgriInventory>>`，按 `createTime` 降序。

---

### 4.2 库存详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/inventory/{id}` |

**Path 参数**：`id`（Long）

**响应**：`R<AgriInventory>`；不存在时抛出 `NOT_FOUND`。

---

### 4.3 创建库存记录

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/input/inventory` |

**Request Body**：`AgriInventory`（JSON）

**业务规则**：`status` 由服务端根据 `stockQuantity`、`warningLevel`、`maxLevel` 自动计算（见[库存状态机](#库存状态机)），忽略请求传入的 `status`。

**响应**：`R<AgriInventory>` — 返回已创建的库存记录。

---

### 4.4 更新库存记录

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/input/inventory/{id}` |

**Path 参数**：`id`（Long）

**Request Body**：`AgriInventory`（JSON）

**业务规则**：`status` 自动重新计算；若记录不存在，抛出 `NOT_FOUND`。

**响应**：`R<AgriInventory>` — 返回更新后的库存记录。

---

### 4.5 删除库存记录

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/input/inventory/{id}` |

**Path 参数**：`id`（Long）

**响应**：`R<Void>`，msg = "删除成功"。

---

### 4.6 调整库存数量

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/input/inventory/{id}/adjust` |

**Path 参数**：`id`（Long）

**Request Body**（JSON）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| delta | Number（BigDecimal） | 是 | 调整量；正数增加，负数减少 |
| reason | String | 否 | 调整原因，会写入 `remark` 字段 |

**业务规则**

- 调整后库存（`currentStock + delta`）不能为负数，否则抛出业务异常。
- 调整后自动重新计算 `status`（见[库存状态机](#库存状态机)）。

**响应**：`R<AgriInventory>` — 返回调整后的库存记录。

---

### 4.7 库存预警列表

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/inventory/alerts` |

**Query 参数**：无

**业务规则**：返回所有 `status` 为 `LOW` 或 `EMPTY` 的库存记录，按 `stockQuantity` 升序排列（库存最少的排在前面）。

**响应**：`R<List<AgriInventory>>`

---

### 库存状态机

库存状态由系统在写入时自动计算，规则如下：

```
stockQuantity == null 或 stockQuantity <= 0  →  EMPTY
stockQuantity > maxLevel（maxLevel != null）   →  OVERSTOCKED
stockQuantity <= warningLevel（warningLevel != null） →  LOW
其他                                           →  NORMAL
```

**优先级**：EMPTY > OVERSTOCKED > LOW > NORMAL

---

### 4.8 导出库存 CSV

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/inventory/export` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 否 | 产品名称模糊搜索 |
| status | String | 否 | 状态筛选 |
| farmerId | Long | 否 | 按农户筛选 |

**响应**：`text/csv; charset=UTF-8` 文件流，文件名 `农资库存.csv`（BOM UTF-8）。

CSV 列顺序：`产品名称, 农户ID, 库存数量, 单位, 预警阈值, 存放位置, 状态`

---

## 5. 农资使用记录 AgriUsageController

**Base URL**：`/api/input/usage`

---

### 5.1 使用记录列表（分页）

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/usage/list` |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | — | 产品名称模糊搜索 |
| method | String | 否 | — | 施用方式筛选：`撒施` / `喷洒` / `滴灌` / `穴施` |
| orchardId | Long | 否 | — | 按果园 ID 筛选 |

**响应**：`R<PageResult<AgriUsage>>`，按 `createTime` 降序。

---

### 5.2 使用记录详情

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/usage/{id}` |

**Path 参数**：`id`（Long）

**响应**：`R<AgriUsage>`；不存在时抛出 `NOT_FOUND`。

---

### 5.3 创建使用记录

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `POST` |
| 路径 | `/api/input/usage` |

**Request Body**：`AgriUsage`（JSON）

**业务规则（库存联动）**

- 记录保存后，系统自动按 `productId` 查找对应库存记录，并**扣减** `quantity` 数量。
- 若扣减后库存为负，抛出业务异常：`"库存不足，当前库存: {current} {unit}"`，事务回滚。
- 扣减后自动重新计算库存 `status`（见[库存状态机](#库存状态机)）。

**响应**：`R<AgriUsage>` — 返回已创建的使用记录。

---

### 5.4 更新使用记录

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `PUT` |
| 路径 | `/api/input/usage/{id}` |

**Path 参数**：`id`（Long）

**Request Body**：`AgriUsage`（JSON）

**业务规则**

- 若记录不存在，抛出 `NOT_FOUND`。
- 更新操作**不触发**库存联动，如需修正库存需手动调用库存调整接口。

**响应**：`R<AgriUsage>` — 返回更新后的使用记录。

---

### 5.5 删除使用记录

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `DELETE` |
| 路径 | `/api/input/usage/{id}` |

**Path 参数**：`id`（Long）

**业务规则（库存联动）**

- 删除成功后，系统自动按 `productId` 查找对应库存记录，**恢复**（加回）该记录的 `quantity`。
- 恢复后自动重新计算库存 `status`。

**响应**：`R<Void>`，msg = "删除成功"。

---

### 5.6 导出使用记录 CSV

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/usage/export` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 否 | 产品名称模糊搜索 |
| method | String | 否 | 施用方式筛选 |
| orchardId | Long | 否 | 按果园筛选 |

**响应**：`text/csv; charset=UTF-8` 文件流，文件名 `农资使用记录.csv`（BOM UTF-8）。

CSV 列顺序：`产品名称, 批次编码, 果园名称, 使用量, 单位, 使用日期, 操作人, 施用方式, 溯源码`

---

### 5.7 按批次查询使用记录

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/usage/by-batch` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| batchId | Long | 是 | 种植批次 ID（关联 `cultivation_batch.id`） |

**响应**：`R<List<AgriUsage>>`，按 `usageDate` 降序排列。

---

### 5.8 按溯源码查询使用记录

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/usage/by-trace` |

**Query 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| traceCode | String | 是 | 溯源码 |

**响应**：`R<List<AgriUsage>>`，按 `usageDate` 降序排列。

---

## 6. 农资溯源链路 AgriTraceController

**Base URL**：`/api/input/trace`

---

### 6.1 根据溯源码查询完整溯源链路

| 属性 | 值 |
|------|-----|
| HTTP 方法 | `GET` |
| 路径 | `/api/input/trace/{traceCode}` |

**Path 参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| traceCode | String | 溯源码（由使用记录写入） |

**溯源链路构建逻辑**

```
traceCode
  → agri_usage（按 traceCode 查询使用记录列表）
      → 提取所有 productId
          → agri_purchase（按 productId IN 查询采购记录列表）
              → 提取所有 supplierId
                  → agri_supplier（按 supplierId IN 查询供应商列表）
```

**业务规则**

- 若 `traceCode` 对应的使用记录不存在，抛出 `NOT_FOUND`：`"溯源码对应的使用记录不存在: {traceCode}"`。
- 溯源链路为快照数据，不涉及任何写操作。

**响应结构**：`R<AgriTraceChainVO>`

```json
{
  "code": 200,
  "data": {
    "traceCode": "TC2025001",
    "usages": [ { ...AgriUsage... } ],
    "purchases": [ { ...AgriPurchase... } ],
    "suppliers": [ { ...AgriSupplier... } ]
  }
}
```

---

## 7. 实体字段说明

> 所有实体均继承 `BaseEntity`，包含公共字段：`id`（Long）、`createTime`（LocalDateTime）、`updateTime`（LocalDateTime）。

---

### 7.1 AgriSupplier — 农资供应商

表名：`agri_supplier`

| 字段名 | Java 类型 | 说明 |
|--------|-----------|------|
| id | Long | 主键（继承自 BaseEntity） |
| supplierCode | String | 供应商编码 |
| name | String | 供应商名称 |
| contactPerson | String | 联系人 |
| phone | String | 联系电话 |
| address | String | 地址 |
| license | String | 营业执照号 |
| qualification | String | 资质证书 |
| creditScore | Integer | 信用评分（1-100） |
| status | String | 状态：`PENDING` / `APPROVED` / `REJECTED` / `BLACKLISTED` |
| auditTime | LocalDateTime | 审核时间 |
| auditor | String | 审核人 |
| rejectReason | String | 拒绝原因 |
| remark | String | 备注 |
| createTime | LocalDateTime | 创建时间（继承） |
| updateTime | LocalDateTime | 更新时间（继承） |

**供应商状态说明**

| 状态 | 含义 |
|------|------|
| PENDING | 待审核（新建默认状态） |
| APPROVED | 已通过 |
| REJECTED | 已拒绝（可重新提交审核） |
| BLACKLISTED | 已列入黑名单（终态） |

---

### 7.2 AgriProduct — 农资产品

表名：`agri_product`

| 字段名 | Java 类型 | 说明 |
|--------|-----------|------|
| id | Long | 主键 |
| productCode | String | 产品编码，如 `AP20250101001` |
| name | String | 产品名称 |
| type | String | 产品类型：`FERTILIZER`（化肥） / `PESTICIDE`（农药） / `SEED`（种子） / `TOOL`（农具） |
| manufacturer | String | 生产厂家 |
| spec | String | 规格，如 `50kg/袋` |
| batchNo | String | 生产批号 |
| productionDate | LocalDate | 生产日期 |
| expiryDate | LocalDate | 保质期至 |
| registration | String | 登记证号 / 农药登记号 |
| status | String | 状态：`ACTIVE`（在用） / `DISCONTINUED`（停用） |
| remark | String | 备注 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

---

### 7.3 AgriPurchase — 农资采购记录

表名：`agri_purchase`

| 字段名 | Java 类型 | 说明 |
|--------|-----------|------|
| id | Long | 主键 |
| purchaseNo | String | 采购单号（业务唯一编号） |
| productId | Long | 农资产品 ID（FK → agri_product.id） |
| productName | String | 产品名称（冗余字段） |
| supplierId | Long | 供应商 ID（FK → agri_supplier.id） |
| supplierName | String | 供应商名称（冗余字段） |
| quantity | BigDecimal | 采购数量 |
| unit | String | 单位：`kg` / `L` / `袋` / `瓶` 等 |
| unitPrice | BigDecimal | 单价 |
| totalAmount | BigDecimal | 总金额（可自动计算：quantity × unitPrice） |
| purchaseDate | LocalDate | 采购日期 |
| farmerId | Long | 采购人（农户）ID |
| status | String | 采购单状态：`PENDING` / `APPROVED` / `RECEIVED` / `CANCELLED` |
| remark | String | 备注 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

---

### 7.4 AgriInventory — 农资库存

表名：`agri_inventory`

| 字段名 | Java 类型 | 说明 |
|--------|-----------|------|
| id | Long | 主键 |
| productId | Long | 农资产品 ID（FK → agri_product.id） |
| productName | String | 产品名称（冗余字段） |
| farmerId | Long | 所属农户 ID |
| stockQuantity | BigDecimal | 当前库存数量 |
| unit | String | 单位 |
| warningLevel | BigDecimal | 库存预警阈值（≤ 此值时状态变为 LOW） |
| maxLevel | BigDecimal | 超储上限（> 此值时状态变为 OVERSTOCKED） |
| warehouse | String | 存放位置 |
| status | String | 库存状态：`NORMAL` / `LOW` / `EMPTY` / `OVERSTOCKED`（系统自动计算） |
| remark | String | 备注（调整时可记录调整原因） |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

---

### 7.5 AgriUsage — 农资使用记录

表名：`agri_usage`

| 字段名 | Java 类型 | 说明 |
|--------|-----------|------|
| id | Long | 主键 |
| productId | Long | 农资产品 ID（FK → agri_product.id） |
| productName | String | 产品名称（冗余字段） |
| batchId | Long | 种植批次 ID（FK → cultivation_batch.id） |
| batchCode | String | 批次编码（冗余字段） |
| operationId | Long | 作业操作 ID（FK → cultivation_operation.id，可为空） |
| orchardId | Long | 果园 ID（FK → farm_orchard.id） |
| orchardName | String | 果园名称（冗余字段） |
| quantity | BigDecimal | 使用量 |
| unit | String | 单位 |
| usageDate | LocalDate | 使用日期 |
| operator | String | 操作人姓名 |
| method | String | 施用方式：`撒施` / `喷洒` / `滴灌` / `穴施` |
| traceCode | String | 溯源码（可为空，关联溯源链路查询） |
| remark | String | 备注 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

---

### 7.6 AgriTraceChainVO — 溯源链路视图对象

| 字段名 | Java 类型 | 说明 |
|--------|-----------|------|
| traceCode | String | 溯源码 |
| usages | List\<AgriUsage\> | 该溯源码下的所有使用记录 |
| purchases | List\<AgriPurchase\> | 涉及产品的所有采购记录 |
| suppliers | List\<AgriSupplier\> | 涉及供应商的完整信息 |

---

## 通用响应格式

所有接口（除 CSV 导出）均返回统一响应体 `R<T>`：

```json
{
  "code": 200,
  "msg": "success",
  "data": { }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码，200 表示成功 |
| msg | String | 提示信息 |
| data | T | 响应数据，失败时为 null |

分页接口的 `data` 为 `PageResult<T>`：

```json
{
  "records": [],
  "total": 100,
  "size": 10,
  "current": 1
}
```

---

## 跨模块库存联动说明

农资采购和使用记录均会自动触发库存更新，联动规则如下：

| 触发操作 | 库存变化 | 触发条件 |
|----------|----------|----------|
| 确认收货（`/purchases/{id}/receive`） | `stockQuantity += purchase.quantity` | productId 不为空且库存记录存在 |
| 创建使用记录（`POST /usage`） | `stockQuantity -= usage.quantity` | productId 不为空且库存记录存在；若扣减后为负则回滚 |
| 删除使用记录（`DELETE /usage/{id}`） | `stockQuantity += usage.quantity`（恢复） | productId 不为空且库存记录存在 |

所有库存联动操作均在同一事务内完成，库存状态（`NORMAL/LOW/EMPTY/OVERSTOCKED`）在每次变更后自动重新计算。
