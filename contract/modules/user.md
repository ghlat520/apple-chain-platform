# 用户模块 API 契约

> 模块：`apple-module-user`
> URL 前缀：`/api/user`
> 错误码段：`100000 ~ 199999`
> 最近更新：2026-04-11

## 接口索引

| # | 接口 | URL | 方法 | 认证 |
|---|------|-----|------|------|
| 1 | [用户登录](#1-用户登录) | `/api/user/auth/login` | POST | ❌ |
| 2 | [获取当前用户](#2-获取当前用户) | `/api/user/auth/me` | GET | ✅ |
| 3 | [修改密码](#3-修改密码) | `/api/user/auth/password` | PUT | ✅ |
| 4 | [用户登出](#4-用户登出) | `/api/user/auth/logout` | POST | ✅ |
| 5 | [分配角色](#5-分配角色) | `/api/admin/users/{id}/roles` | POST | ✅ |

---

## 1. 用户登录

**URL**：`POST /api/user/auth/login`
**认证**：无需认证
**错误码**：`100002` / `100003`
**变更历史**：2026-04-11 初版

### 请求体 `LoginRequest`

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `username` | string | ✅ | 用户名（手机号或邮箱） | `13800138000` |
| `password` | string | ✅ | 登录密码 | `Abc12345` |

```typescript
export interface LoginRequest {
  username: string;
  password: string;
}
```

### 响应体 `LoginResponse`

| 字段 | 类型 | 必返 | 说明 | 示例 |
|------|------|------|------|------|
| `token` | string | ✅ | JWT 访问令牌 | `eyJhbGciOiJIUzI1NiJ9...` |
| `userId` | number | ✅ | 用户唯一 ID | `1024` |
| `username` | string | ✅ | 登录用户名 | `zhangsan` |
| `realName` | string \| null | ❌ | 真实姓名 | `张三` |
| `roleCode` | string \| null | ❌ | 主角色编码（向后兼容，优先用 `roles`） | `FARMER` |
| `orgName` | string \| null | ❌ | 所属组织 | `洛川县苹果合作社` |
| `avatar` | string \| null | ❌ | 头像 URL | `https://cdn/1024.png` |
| `roles` | string[] | ✅ | 持有的全部角色编码 | `["FARMER","COOP_MEMBER"]` |
| `permissions` | string[] | ✅ | 解析后的权限编码（`resource:action`） | `["orchard:read","orchard:write"]` |

```typescript
export interface LoginResponse {
  token: string;
  userId: number;
  username: string;
  realName: string | null;
  roleCode: string | null;
  orgName: string | null;
  avatar: string | null;
  roles: string[];
  permissions: string[];
}
```

### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMDI0In0.xyz",
    "userId": 1024,
    "username": "zhangsan",
    "realName": "张三",
    "roleCode": "FARMER",
    "orgName": "洛川县苹果合作社",
    "avatar": "https://cdn.example.com/avatar/1024.png",
    "roles": ["FARMER", "COOP_MEMBER"],
    "permissions": ["orchard:read", "orchard:write", "trace:read"]
  }
}
```

### 响应示例（失败 - 密码错误）

```json
{
  "code": 100002,
  "message": "用户名或密码错误",
  "data": null
}
```

### 响应示例（失败 - 账号锁定）

```json
{
  "code": 100003,
  "message": "账号已锁定，请联系管理员",
  "data": null
}
```

---

## 2. 获取当前用户

**URL**：`GET /api/user/auth/me`
**认证**：需要 JWT
**错误码**：`401 未授权`
**变更历史**：2026-04-11 初版

### 请求参数

无

### 响应体 `CurrentUserResponse`

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| `userId` | number | ✅ | 用户 ID |
| `username` | string | ✅ | 用户名 |
| `realName` | string \| null | ❌ | 真实姓名 |
| `phone` | string \| null | ❌ | 手机号 |
| `email` | string \| null | ❌ | 邮箱 |
| `orgName` | string \| null | ❌ | 所属组织 |
| `avatar` | string \| null | ❌ | 头像 URL |
| `roleCode` | string \| null | ❌ | 主角色编码（向后兼容） |
| `roles` | string[] | ✅ | 全部角色编码 |
| `permissions` | string[] | ✅ | 权限编码列表 |

```typescript
export interface CurrentUserResponse {
  userId: number;
  username: string;
  realName: string | null;
  phone: string | null;
  email: string | null;
  orgName: string | null;
  avatar: string | null;
  roleCode: string | null;
  roles: string[];
  permissions: string[];
}
```

> **后端命名迁移提示**：当前后端代码里是 `CurrentUserDTO`，下次修改该文件时必须改名为 `CurrentUserResponse`（见 `docs/contract/BACKEND-CONTRACT.md` §1）。

### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1024,
    "username": "zhangsan",
    "realName": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "orgName": "洛川县苹果合作社",
    "avatar": "https://cdn.example.com/avatar/1024.png",
    "roleCode": "FARMER",
    "roles": ["FARMER", "COOP_MEMBER"],
    "permissions": ["orchard:read", "orchard:write"]
  }
}
```

---

## 3. 修改密码

**URL**：`PUT /api/user/auth/password`
**认证**：需要 JWT
**错误码**：`100004`
**变更历史**：2026-04-11 初版

### 请求体 `ChangePasswordRequest`

| 字段 | 类型 | 必填 | 说明 | 校验 |
|------|------|------|------|------|
| `oldPassword` | string | ✅ | 原密码 | 非空 |
| `newPassword` | string | ✅ | 新密码 | 长度 6~128 |

```typescript
export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}
```

### 响应体

`data: null`（仅返回成功/失败标记）

### 响应示例（成功）

```json
{ "code": 200, "message": "操作成功", "data": null }
```

### 响应示例（失败 - 原密码错误）

```json
{ "code": 100004, "message": "原密码错误", "data": null }
```

---

## 4. 用户登出

**URL**：`POST /api/user/auth/logout`
**认证**：需要 JWT
**错误码**：无业务错误码
**变更历史**：2026-04-11 初版

### 请求参数

无

### 响应体

`data: null`

### 响应示例

```json
{ "code": 200, "message": "操作成功", "data": null }
```

---

## 5. 分配角色

**URL**：`POST /api/admin/users/{id}/roles`
**认证**：需要 JWT + 权限 `user:assign-role`
**错误码**：`100001` / `100101` / `100201`
**变更历史**：2026-04-11 初版

### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | number | 目标用户 ID |

### 请求体 `AssignRolesRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `roleCodes` | string[] | ✅ | 要分配的角色编码列表，空数组表示清空角色 |

```typescript
export interface AssignRolesRequest {
  roleCodes: string[];
}
```

### 响应体

`data: null`

### 响应示例（成功）

```json
{ "code": 200, "message": "操作成功", "data": null }
```

### 响应示例（失败 - 用户不存在）

```json
{ "code": 100001, "message": "用户不存在", "data": null }
```

---

## 错误码表（用户模块）

| code | message | 触发场景 |
|------|---------|---------|
| `100001` | 用户不存在 | 查询或操作目标用户时数据库中无记录 |
| `100002` | 用户名或密码错误 | 登录时账号或密码校验失败 |
| `100003` | 账号已锁定，请联系管理员 | 登录时账号状态非 `ACTIVE` |
| `100004` | 原密码错误 | 修改密码时旧密码校验失败 |
| `100005` | 登录已过期，请重新登录 | JWT 过期或签名失败 |
| `100101` | 角色不存在 | 分配角色时角色编码无效 |
| `100201` | 没有执行此操作的权限 | 当前用户缺少所需权限码 |

> 错误码枚举实现：`apple-module-user/src/main/java/com/apple/chain/user/enums/UserErrorCode.java`
