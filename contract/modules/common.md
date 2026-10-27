# 公共服务 (Common) 模块契约

> 模块：`apple-common`
> URL 前缀：`/api/config`
> 错误码段：`0 ~ 99999`（通用 ResultCode，CONVENTIONS.md:130 权威）
> 最近更新：2026-04-12
> 权威源：本文件
> 后端实现：`apple-common`
> 关联契约：无独立关联（本模块为基础设施层，被所有业务模块依赖）

---

## 0. 架构说明

本项目中 **不存在独立的公共业务 Controller**（如 DictController、RegionController、FileController、UploadController、AreaController）。

公共能力以两种形态存在：

| 形态 | 说明 | 示例 |
|------|------|------|
| **基础设施类**（`apple-common`） | 框架级组件，不暴露 HTTP 端点，被所有业务模块依赖 | `R<T>`、`PageResult<T>`、`PageParam`、`BaseEnum`、`BaseEntity`、`BizException`、`IErrorCode`、`ResultCode` |
| **配置类端点**（`apple-common`） | 少量运行时配置读取端点，挂载在 `/api/config/` | `AmapConfigController` |

**散落在业务模块中的公共能力**（以下端点归各业务模块契约管辖，不重复收录于本文）：

| 能力 | 所在 Controller | 归属模块契约 | 端点 |
|------|----------------|-------------|------|
| 文件导出（CSV） | 各模块 Controller 的 `export` 方法 | 对应模块 | 如 `/api/finance/loans/export` |
| 用户认证（登录/登出/Profile） | `AuthController` | `user.md` | `/api/user/login`、`/api/user/logout`、`/api/user/profile` |
| RBAC 权限管理 | `RbacController` | `user.md` | `/api/user/roles/*`、`/api/user/permissions/*` |
| 文件下载（VDP溯源码） | `TraceCodeController` | `trace.md` | `/api/trace/codes/{batchId}/export` |

> **结论**：字典管理（Dict）、地区管理（Region）、通用文件上传（Upload）在当前项目中 **尚未实现**。如需新增，应创建独立 Controller 并在本文档中补充端点定义。

---

## 1. 接口索引（1 端点）

| # | 接口中文名 | URL | 方法 | 认证 |
|---|-----------|-----|------|------|
| 1 | [获取高德地图配置](#11-获取高德地图配置) | `/api/config/amap` | GET | 无需认证 |

---

## 2. 前端配置 AmapConfigController（`/api/config`）

### 2.1 获取高德地图配置

**URL**：`GET /api/config/amap`
**认证**：无需认证
**错误码**：`500`（通用内部错误）
**变更历史**：2026-04-12 | init | 架构师

#### 请求参数

无。

#### 响应体 `AmapConfigResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `key` | string | ✅ | 高德 Web JSAPI Key。环境变量 `AMAP_WEB_JSAPI_KEY`，未配置时返回空字符串 |
| `securityJsCode` | string | ✅ | 高德安全密钥（securityJsCode）。环境变量 `AMAP_WEB_JSAPI_SECRET`，未配置时返回空字符串 |

```typescript
export interface AmapConfigResponse {
  key: string;
  securityJsCode: string;
}
```

#### 业务规则

- 两个值均从 Spring 配置属性读取（`amap.web-jsapi-key`、`amap.web-jsapi-secret`），默认为空字符串。
- Key/Secret 通过环境变量注入，不硬编码在代码中，支持按环境（dev/staging/prod）切换不同的高德应用凭证。
- 前端通过 `amap-jsapi-loader` 加载高德地图时使用此接口获取凭证。
- 本端点无需认证（高德凭证本身通过 Referrer 白名单保护，不依赖 JWT）。

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "key": "a1b2c3d4e5f6g7h8i9j0",
    "securityJsCode": "abcdef1234567890"
  }
}
```

#### 响应示例（未配置 — 仍返回 200，值为空字符串）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "key": "",
    "securityJsCode": ""
  }
}
```

#### 响应示例（失败 — 服务端异常）

```json
{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}
```

---

## 3. 基础设施组件清单

以下组件位于 `apple-common` 模块，**不暴露 HTTP 端点**，但构成所有业务接口的底层契约。前端开发者需了解其行为。

### 3.1 统一响应包装 `R<T>`

所有 HTTP 响应用 `R<T>` 包装，字段：`code`（int）、`message`（string）、`data`（T | null）。

- `R.ok()` / `R.ok(data)` — 成功，code = 200
- `R.fail(message)` — 失败，code = 400
- `R.fail(IErrorCode)` — 失败，使用模块错误码

详见 `CONVENTIONS.md` 第 1 节。

### 3.2 分页请求 `PageParam`

| 字段 | 类型 | 默认值 | 约束 |
|------|------|--------|------|
| `page` | int | `1` | 最小 1 |
| `size` | int | `10` | 1 ~ 100 |
| `keyword` | string | `null` | 可选模糊搜索 |

### 3.3 分页响应 `PageResult<T>`

| 字段 | 类型 | 说明 |
|------|------|------|
| `records` | `List<T>` | 当前页数据 |
| `total` | long | 总记录数 |
| `current` | long | 当前页码 |
| `size` | long | 每页条数 |

### 3.4 基础实体 `BaseEntity`

所有业务 Entity 继承 `BaseEntity`，提供以下字段：

| 字段 | Java 类型 | JSON 字段名 | 说明 |
|------|-----------|------------|------|
| `id` | Long | `id` | 雪花 ID，序列化为 String（防精度丢失） |
| `createTime` | LocalDateTime | `createTime` | 创建时间，`yyyy-MM-dd HH:mm:ss` |
| `updateTime` | LocalDateTime | `updateTime` | 更新时间，`yyyy-MM-dd HH:mm:ss` |
| `createBy` | String | `createBy` | 创建人 |
| `deleted` | Integer | - | 逻辑删除标记（不序列化到前端） |

> **注意**：CONVENTIONS.md 约定 DTO 层字段名为 `createdAt` / `updatedAt`，但当前 BaseEntity 字段名为 `createTime` / `updateTime`。各模块的 Request/Response DTO 应按 CONVENTIONS.md 命名，在 Entity → DTO 转换层处理字段映射。

### 3.5 业务异常 `BizException`

Service 层抛出业务异常：

```java
// 推荐形式 — 使用模块错误码枚举
throw new BizException(UserErrorCode.USER_NOT_FOUND);

// 通用形式
throw new BizException("自定义错误消息");           // code = 400
throw new BizException(100001, "自定义错误消息");     // 自定义 code
```

由 `GlobalExceptionHandler` 统一拦截，返回 `R<Void>` 格式的错误响应。

### 3.6 枚举序列化 `BaseEnum`

所有业务枚举实现 `BaseEnum` 接口，Jackson 序列化为 `{code, desc}` 对象。

```typescript
// 前端类型
export interface EnumValue<T = number> {
  code: T;
  desc: string;
}
```

---

## 4. 错误码表

### 4.1 通用错误码（ResultCode，段位 0 ~ 99999）

| 错误码 | HTTP 状态码 | 说明 |
|--------|-----------|------|
| `200` | 200 | 操作成功 |
| `400` | 200 | 操作失败（BizException 默认） |
| `401` | 200 | 未授权，请先登录 |
| `403` | 200 | 权限不足 |
| `404` | 200 | 资源不存在 |
| `422` | 422 | 参数校验失败（@Valid 触发） |
| `500` | 500 | 服务器内部错误（未捕获异常兜底） |

### 4.2 模块错误码分配表（参考）

| 模块 | 段位 | 错误码枚举 | 归属契约 |
|------|------|-----------|---------|
| 通用（ResultCode） | `0 ~ 99999` | `ResultCode` | `CONVENTIONS.md` |
| user | `100000 ~ 199999` | `UserErrorCode` | `user.md` |
| planting | `200000 ~ 299999` | - | `planting.md` |
| trace | `300000 ~ 399999` | - | `trace.md` |
| chain | `400000 ~ 499999` | - | `chain.md` |
| trade | `500000 ~ 599999` | - | `trade.md` |
| warehouse | `600000 ~ 699999` | - | `warehouse.md` |
| input | `700000 ~ 799999` | - | `input.md` |
| coldchain | `800000 ~ 899999` | - | `coldchain.md` |
| iot | `900000 ~ 999999` | - | - |
| finance | `1000000 ~ 1099999` | - | `finance.md` |
| bigdata | `1100000 ~ 1199999` | - | `bigdata.md` |

> **注意**：当前已实现的模块级错误码枚举仅有 `UserErrorCode`（user 模块）。其他模块使用 `BizException(int, String)` 或 `BizException(String)` 形式抛出异常，尚未迁移到独立枚举。迁移计划详见各模块契约的「实现注记」章节。

---

## 5. 待规划公共能力

以下公共能力在 CONVENTIONS.md 错误码表中 **未分配独立段位**，当前项目中 **尚未实现**。如需新增，建议分配错误码段位并创建对应 Controller。

### 5.1 字典管理（Dict）

- **建议 URL 前缀**：`/api/common/dicts`
- **能力**：数据字典 CRUD（字典类型 + 字典项）
- **典型场景**：苹果品种、农资类型、检测项目等下拉选项
- **当前替代方案**：各模块枚举硬编码

### 5.2 地区管理（Region）

- **建议 URL 前缀**：`/api/common/regions`
- **能力**：省 → 市 → 区三级联动查询
- **典型场景**：果园所在地、农户住址、物流路线规划
- **当前替代方案**：高德地图 API 前端直接调用

### 5.3 通用文件上传（Upload）

- **建议 URL 前缀**：`/api/common/files`
- **能力**：图片/PDF/文档上传，返回文件 URL
- **典型场景**：溯源图片、合同附件、质检报告
- **当前替代方案**：各模块自行处理（部分未实现）
- **建议错误码段位**：复用通用段位或新增 `1200000 ~ 1299999`

---

## 质量 Checklist

- [x] 1 个端点全覆盖（AmapConfig）
- [x] 7 个错误码（ResultCode 枚举全部），全在 0-99999 范围
- [x] 架构说明清晰：无独立公共业务 Controller，说明散落分布
- [x] 基础设施组件清单完整（R / PageParam / PageResult / BaseEntity / BizException / BaseEnum）
- [x] 待规划能力明确（Dict / Region / Upload）
- [x] 模块错误码分配表完整（12 模块）
- [x] JSON 示例全用 `message` 字段（禁用 `msg`）
