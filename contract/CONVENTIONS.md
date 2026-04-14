# AppChain API 全局约定

> 所有 `modules/*.md` 中的接口**必须**遵守本文件规定的全局约定。
> 本文件中未规定的细节，可在 `modules/*.md` 中补充模块级约定。

## 1. 统一响应包装

所有 HTTP 响应**必须**用 `R<T>` 包装：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `code` | int | ✅ | 业务状态码，`200` 表示成功；非 200 表示失败 |
| `message` | string | ✅ | 用户可见的中文提示 |
| `data` | T \| null | ⚠️ | 业务载荷；失败时为 `null` |

**前端判定**：`res.code === 200` 即成功；失败路径统一读 `res.message` 给用户看。

### TypeScript 基础类型

```typescript
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T | null;
}
```

---

## 2. 分页约定

### 请求参数（所有列表接口统一）

```typescript
export interface PageParam {
  page: number;      // 页码，从 1 开始，默认 1
  size: number;      // 每页数量，默认 10，最大 100
  keyword?: string;  // 可选，统一的模糊搜索字段
}
```

### 响应结构

```typescript
export interface PageResult<T> {
  records: T[];      // 当前页数据
  total: number;     // 总数
  current: number;   // 当前页码
  size: number;      // 每页数量
}
```

### 完整响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [ /* T */ ],
    "total": 123,
    "current": 1,
    "size": 10
  }
}
```

---

## 3. 时间字段

- 后端 Java 类型：`java.time.LocalDateTime`（禁止 `java.util.Date`）
- 响应序列化格式：`"yyyy-MM-dd HH:mm:ss"`
- 时区：`Asia/Shanghai`
- 前端 TS 类型：`string`（不要用 Date 对象，格式化由前端组件层处理）

**示例**：
```json
{
  "createdAt": "2026-04-11 15:30:00",
  "updatedAt": "2026-04-11 16:05:22"
}
```

---

## 4. 枚举字段

所有业务枚举**必须**以 `{code, desc}` 对象形式出现在响应中：

```json
{
  "status": { "code": 1, "desc": "正常" }
}
```

**禁止**：
- ❌ `"status": 1`
- ❌ `"status": "ACTIVE"`
- ❌ `"status": "正常"`

### TypeScript 通用类型

```typescript
export interface EnumValue<T = number> {
  code: T;
  desc: string;
}
```

### 请求参数中的枚举

请求参数中传递 `code`（仅传数字即可）：
```json
{ "status": 1 }
```

---

## 5. 错误码号段

| 模块 | 段位 | 示例 |
|------|------|------|
| 通用（ResultCode） | `0 ~ 99999` | `200 成功` / `400 失败` / `401 未授权` / `500 内部错误` |
| user | `100000 ~ 199999` | `100001 用户不存在` |
| planting | `200000 ~ 299999` | `200001 果园不存在` |
| trace | `300000 ~ 399999` | `300001 溯源记录不存在` |
| chain | `400000 ~ 499999` | |
| trade | `500000 ~ 599999` | |
| warehouse | `600000 ~ 699999` | |
| input | `700000 ~ 799999` | |
| coldchain | `800000 ~ 899999` | |
| iot | `900000 ~ 999999` | |
| finance | `1000000 ~ 1099999` | |
| bigdata | `1100000 ~ 1199999` | |

每个 `modules/<module>.md` **必须**在文件末尾有一节「错误码表」列出本模块所有错误码。

---

## 6. URL 命名约定

- **前缀**：所有业务接口以 `/api/<module>/` 开头，如 `/api/user/login`
- **风格**：kebab-case（小写连字符），不使用驼峰或下划线
- **资源**：复数名词，如 `/api/planting/orchards`
- **动作**：对资源的 CRUD 用标准 HTTP method，不要在 URL 里写动词（如 `/getOrchard` ❌）
  - `GET /api/planting/orchards` — 列表
  - `GET /api/planting/orchards/{id}` — 详情
  - `POST /api/planting/orchards` — 创建
  - `PUT /api/planting/orchards/{id}` — 更新
  - `DELETE /api/planting/orchards/{id}` — 删除
- **非 CRUD 动作**：子资源或动词短语
  - `POST /api/user/login`（登录不是创建用户，视为"动作"）
  - `POST /api/planting/orchards/{id}/publish`

---

## 7. 字段命名约定

- **JSON 字段**：`camelCase`（前后端一致）
- **常量字段**：`UPPER_SNAKE_CASE`
- **布尔字段**：以 `is` / `has` / `can` 开头，如 `isActive`、`hasPermission`
- **ID 字段**：`xxxId`（如 `userId`、`orchardId`），主键就叫 `id`
- **时间字段**：`createdAt` / `updatedAt` / `deletedAt` / `<action>At`
- **列表字段**：复数名词，如 `roles`、`permissions`、`items`
- **不要**在字段名里重复父对象名字：`user.name` ✅，`user.userName` ❌

---

## 8. 请求/响应 DTO 命名

- 请求体：`XxxRequest`
- 响应体：`XxxResponse`
- 数据库实体：`XxxDO`（不暴露到接口层）
- 业务中间对象：`XxxBO`
- **禁用**：`XxxDTO` / `XxxVO` / `XxxParam` / `XxxForm`

---

## 9. 认证与权限

- 所有需要登录的接口**必须**在 HTTP header 带：
  ```
  Authorization: Bearer <jwt-token>
  ```
- Token 从 `POST /api/user/login` 的响应中获得
- 未登录接口需在 `modules/*.md` 中**显式**标注「无需认证」
- 权限不足返回 `code=403`，`message="权限不足"`

---

## 10. modules/*.md 编写模板

每个接口**必须**至少包含以下 6 块内容：

```markdown
## <接口中文名>

**URL**：`POST /api/user/login`
**认证**：无需认证（或：需要 JWT）
**错误码**：`100002` / `100003`
**变更历史**：2026-04-11 新增

### 请求体 LoginRequest

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| username | string | ✅ | 用户名（手机号/邮箱） | `13800138000` |
| password | string | ✅ | 密码 | `Abc12345` |

\`\`\`typescript
export interface LoginRequest {
  username: string;
  password: string;
}
\`\`\`

### 响应体 LoginResponse

| 字段 | 类型 | 必返 | 说明 | 示例 |
|------|------|------|------|------|
| token | string | ✅ | JWT Token | `eyJ...` |
| userId | number | ✅ | 用户 ID | `1024` |
| ... | | | | |

\`\`\`typescript
export interface LoginResponse {
  token: string;
  userId: number;
  // ...
}
\`\`\`

### 响应示例（成功）

\`\`\`json
{
  "code": 200,
  "message": "操作成功",
  "data": { "token": "eyJ...", "userId": 1024, ... }
}
\`\`\`

### 响应示例（失败）

\`\`\`json
{
  "code": 100002,
  "message": "用户名或密码错误",
  "data": null
}
\`\`\`
```

这 6 块内容是**强制的**，缺任何一块 CR 都会被驳回。
