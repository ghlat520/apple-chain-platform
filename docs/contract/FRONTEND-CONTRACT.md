# 前端契约规范（FRONTEND-CONTRACT）

> **适用范围**：`apple-chain-platform/frontend` 和 `apple-chain-platform/apple-web-ui` 两套前端仓库
> **强制级别**：P0（CR + CI 守护）
>
> ⚠️ **权威源不是生成产物**：契约权威源是 [`contract/modules/*.md`](../../contract/modules/) 中的 Markdown 设计文档。前端 TypeScript 类型**从 Markdown 中的 `interface` 代码块手动复制**，禁止从 `openapi.json` 自动生成。

## 目录

0. [实现前必读](#0-实现前必读)
1. [契约文件位置](#1-契约文件位置)
2. [类型同步工作流（手动复制）](#2-类型同步工作流手动复制)
3. [API 客户端规范](#3-api-客户端规范)
4. [Mock 降级机制](#4-mock-降级机制)
5. [环境开关](#5-环境开关)
6. [生产构建守护](#6-生产构建守护)
7. [Mock Schema 校验](#7-mock-schema-校验)
8. [违规检查清单](#8-违规检查清单)

---

## 0. 实现前必读

**写代码前**，打开对应的契约文档：

```
apple-chain-platform/contract/modules/<your-module>.md
```

- 文档中的 URL、Method、请求体、响应体、错误码是**已经拍板的设计**
- 你的任务是**忠实消费这些契约**，不是发挥创造
- 如果你发现设计有缺陷或实现不了：
  1. ❌ 不允许擅自改字段名、加字段、删字段
  2. ✅ 正确做法：找架构师/产品讨论 → 提 `contract/` 的修订 PR → 合并后再实现
- TypeScript `interface` **直接从契约文档的代码块复制**到 `src/api/types/<module>.d.ts`，字段名、类型、可空性必须完全一致

---

## 1. 契约文件位置

| 文件 | 来源 | 作用 | Git 版本化 |
|------|------|------|-----------|
| `apple-chain-platform/contract/modules/*.md` | 架构师手写 | **契约权威源** | ✅ 提交（在主仓） |
| `<frontend>/src/api/types/<module>.d.ts` | 从 Markdown 手动复制 | 前端 TS 类型 | ✅ 提交 |
| `<frontend>/src/api/<module>.js` | 手写 | API 函数 | ✅ 提交 |
| `<frontend>/src/mock/<module>.js` | 按 Markdown 响应示例编写 | Mock 数据 | ✅ 提交 |
| `<frontend>/scripts/validate-mock.js` | 手写 | ajv 校验 Mock 数据 | ✅ 提交 |

**关键**：前端不需要 `openapi.json`，也不需要 `openapi-typescript` 依赖。契约源只有一个——Markdown 设计文档。

---

## 2. 类型同步工作流（手动复制）

### 2.1 步骤

1. 打开 `apple-chain-platform/contract/modules/<module>.md`
2. 找到你要消费的接口小节，定位 `interface XxxRequest` / `interface XxxResponse` 代码块
3. 在前端仓库 `src/api/types/<module>.d.ts` 中**原样复制**该代码块
4. 在文件顶部加注释注明来源：

```typescript
// 该文件的类型来自 contract/modules/user.md
// 禁止手改 —— 契约变更请先改 Markdown 文档，再同步到此处
// 最近同步：2026-04-11

export interface LoginRequest {
  username: string;
  password: string;
}

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

### 2.2 公共类型

全局共用的 `ApiResponse<T>` / `PageParam` / `PageResult<T>` / `EnumValue<T>` 定义放在 `src/api/types/common.d.ts`，从 `contract/CONVENTIONS.md` 第 1-4 节复制。

### 2.3 为什么不自动生成

| 考虑 | 手动复制 | 自动生成 |
|------|---------|---------|
| 契约权威源清晰度 | ✅ Markdown 唯一源 | ❌ 会被误当成 openapi.json |
| 开发者对契约的感知 | ✅ 必须读文档才能写代码 | ❌ 跳过文档直接用生成产物 |
| 前端对"字段含义"的理解 | ✅ 读字段表看说明和示例 | ❌ 只有字段名和类型 |
| 与 JS 代码的配合 | ✅ 直接在 `.d.ts` 写干净的 interface | ⚠️ 生成的嵌套路径很长 |

---

## 3. API 客户端规范

### 3.1 目录结构

```
src/api/
├── request.js             # axios 实例 + interceptors
├── types/
│   ├── common.d.ts        # ApiResponse / PageParam / PageResult / EnumValue
│   ├── user.d.ts          # 从 contract/modules/user.md 复制
│   ├── planting.d.ts      # 从 contract/modules/planting.md 复制
│   └── ...
├── user.js                # 用户模块 API 函数
├── planting.js            # 种植模块 API 函数
└── ...                    # 每个后端模块一个文件
```

### 3.2 API 函数规范

```javascript
// src/api/user.js
import request from './request';

/**
 * 用户登录
 * @param {import('./types/user').LoginRequest} params
 * @returns {Promise<import('./types/user').LoginResponse>}
 */
export function login(params) {
  return request({
    url: '/api/user/login',
    method: 'post',
    data: params,
  });
}

/**
 * 获取当前用户
 * @returns {Promise<import('./types/user').CurrentUserResponse>}
 */
export function getCurrentUser() {
  return request({
    url: '/api/user/auth/me',
    method: 'get',
  });
}
```

要求：
- ✅ **必须**在 JSDoc 中用 `import(...)` 引用 `types/<module>.d.ts` 中的 interface
- ✅ **必须**集中在 `src/api/<module>.js`，禁止组件内直接 `axios.get`
- ✅ URL / Method / 函数名与 `contract/modules/*.md` 一一对应
- ❌ 禁止在组件里拼 URL
- ❌ 禁止手写与 `types/<module>.d.ts` 重复的 interface

### 3.3 组件消费

```vue
<script setup>
import { login } from '@/api/user';

async function handleLogin() {
  // params 会有类型提示（得益于 JSDoc import）
  const result = await login({ username: 'xxx', password: 'yyy' });
  // result 也有类型提示
}
</script>
```

---

## 4. Mock 降级机制

### 4.1 现有实现评估

`frontend` 当前已实现 error-interceptor 级 fallback Mock（见 `src/api/request.js:20-34`）：
- axios 请求失败 → error interceptor 捕获 → 调用 `routeMock(url, method, params, body)` → 返回 Mock 数据

**优点**：侵入性低，生产模式下如果后端正常，Mock 完全不激活。
**问题**：无环境开关，无 schema 校验，`main.js` 无条件导入 Mock。

### 4.2 改造方案

#### 改造 1：环境开关 `src/mock/shouldMock.js`

```javascript
/**
 * 判断是否启用 Mock
 * @returns {'on' | 'off' | 'fallback'}
 */
export function getMockMode() {
  const mode = import.meta.env.VITE_ENABLE_MOCK;
  if (mode === 'true' || mode === 'on') return 'on';
  if (mode === 'false' || mode === 'off') return 'off';
  return 'fallback';
}

export function shouldMockBeforeRequest() {
  return getMockMode() === 'on';
}

export function shouldMockOnFailure() {
  return getMockMode() === 'fallback';
}
```

#### 改造 2：`src/api/request.js`

```javascript
import { shouldMockOnFailure } from '@/mock/shouldMock';
import { routeMock } from '@/mock/routes';

service.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res.code === 200) return res.data;
    return Promise.reject(new Error(res.message));
  },
  (error) => {
    if (shouldMockOnFailure()) {
      const { config } = error;
      const mockData = routeMock(config.url, config.method, config.params, config.data);
      if (mockData) {
        console.warn('[Mock Fallback]', config.url);
        return Promise.resolve(mockData);
      }
    }
    return Promise.reject(error);
  }
);
```

#### 改造 3：`src/mock/routes.js` 顶部护栏

```javascript
import { getMockMode } from './shouldMock';

export function routeMock(url, method, params, body) {
  if (getMockMode() === 'off') return null;
  // 现有匹配逻辑...
}
```

#### 改造 4：`main.js` 按需导入

```javascript
import { getMockMode } from '@/mock/shouldMock';

if (getMockMode() !== 'off') {
  await import('@/mock/index');
}
```

### 4.3 Mock 数据的来源

**Mock 数据必须按 `contract/modules/*.md` 的「响应示例」节编写**，确保 key、类型、嵌套结构和真实后端一致。

```javascript
// src/mock/user.js
// 数据来自 contract/modules/user.md §1 响应示例（成功）
export const loginSuccess = {
  token: "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMDI0In0.xyz",
  userId: 1024,
  username: "zhangsan",
  realName: "张三",
  roleCode: "FARMER",
  orgName: "洛川县苹果合作社",
  avatar: "https://cdn.example.com/avatar/1024.png",
  roles: ["FARMER", "COOP_MEMBER"],
  permissions: ["orchard:read", "orchard:write", "trace:read"],
};
```

---

## 5. 环境开关

### `.env.development`（开发默认）

```
VITE_ENABLE_MOCK=fallback
VITE_API_BASE_URL=http://localhost:8080
```

### `.env.production`（生产强制禁用）

```
VITE_ENABLE_MOCK=off
VITE_API_BASE_URL=https://api.applechain.prod
```

### `.env.mock`（纯离线演示）

```
VITE_ENABLE_MOCK=on
VITE_API_BASE_URL=http://localhost:8080
```

运行方式：

```bash
npm run dev                      # 真实后端 + 失败降级
vite --mode mock                 # 纯离线演示
npm run build                    # 生产构建（禁 Mock）
```

---

## 6. 生产构建守护

### 6.1 Vite 守护（`vite.config.js`）

```javascript
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd());

  if (mode === 'production' && env.VITE_ENABLE_MOCK !== 'off') {
    throw new Error(
      '[契约守护] 生产构建必须设置 VITE_ENABLE_MOCK=off，禁止 Mock 进入生产包'
    );
  }

  return { /* ... */ };
});
```

### 6.2 Bundle 分析

生产构建后检查 `dist/assets/*.js` 不应包含 `mock/routes.js` 字符串。

---

## 7. Mock Schema 校验

> ⚠️ Phase 3 交付物，尚未实现。思路：从 Markdown 的 interface 代码块提取 JSON Schema，用 ajv 校验 Mock 数据。

### 7.1 脚本设计（`scripts/validate-mock.js`）

```javascript
// 伪代码
// 1. 读取 apple-chain-platform/contract/modules/*.md
// 2. 正则提取所有 ```typescript ... ``` 代码块
// 3. 通过 ts-morph 或 typescript compiler API 解析 interface → JSON Schema
// 4. 遍历 src/mock/**/*.js 导出的 Mock 数据
// 5. 按命名约定匹配 schema（loginSuccess → LoginResponse）
// 6. ajv.compile(schema) → validate(data)
// 7. 任一失败 → process.exit(1)
```

### 7.2 package.json 依赖

```json
{
  "devDependencies": {
    "ajv": "^8.12.0",
    "ajv-formats": "^2.1.1",
    "ts-morph": "^22.0.0"
  },
  "scripts": {
    "contract:validate": "node scripts/validate-mock.js"
  }
}
```

### 7.3 CI 集成

```
npm run contract:validate   # PR 必过
```

---

## 8. 违规检查清单

提交 PR 前自查：

- [ ] 新增 API 都在 `src/api/<module>.js` 中，不是组件里
- [ ] 每个 API 函数都有 JSDoc `@param` / `@returns` 引用 `types/<module>.d.ts`
- [ ] `types/<module>.d.ts` 中的 interface **逐字段**对齐 `contract/modules/<module>.md`
- [ ] 没有手写与契约文档不一致的类型定义
- [ ] Mock 数据按契约文档的响应示例编写
- [ ] `npm run contract:validate` 通过（Phase 3 启用后）
- [ ] 未在 `main.js` 无条件导入 Mock
- [ ] 生产构建 `VITE_ENABLE_MOCK=off` 能通过
- [ ] 如果契约文档有更新，已同步 `types/*.d.ts` 和 Mock 数据

---

## 参考文件（当前基线）

- `frontend/src/api/request.js` — 现有 axios 封装
- `frontend/src/mock/routes.js` — 现有 Mock 路由
- `frontend/src/mock/data.js` — 现有 Mock 数据
- `apple-web-ui/src/api/request.js` — 待改造的生产前端

## Phase 2 待新建/改造文件

- `frontend/src/api/types/common.d.ts` — 新建
- `frontend/src/api/types/user.d.ts` — 新建（从 `contract/modules/user.md` 复制）
- `frontend/src/mock/shouldMock.js` — 新建
- `frontend/scripts/validate-mock.js` — 新建（Phase 3）
- `frontend/.env.development` / `.env.production` / `.env.mock` — 新建/改造
- `frontend/vite.config.js` — 加守护
- `frontend/src/api/request.js` — 改造 interceptor
- `frontend/src/mock/routes.js` — 加护栏
- `frontend/main.js` — 按需导入
