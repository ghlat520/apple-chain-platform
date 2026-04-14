# AppChain 前后端契约规范

> 本目录是**规范说明**（元文档），告诉开发者如何遵守契约。
> **契约本体**（权威源）在 [`apple-chain-platform/contract/`](../../contract/)。

## 核心原则：设计文档即契约（Spec-First）

AppChain 采用**设计优先**模式：

```
 ┌─────────────────────────────────┐
 │  contract/**/*.md               │ ← 唯一权威源（Markdown 设计文档）
 └────────┬────────────────────────┘
          │
   ┌──────┴──────┐
   ▼             ▼
 后端 Java     前端 JavaScript
 按文档实现    按文档消费
```

- ✅ 先改 `contract/modules/*.md`，再改代码（前后端都一样）
- ✅ 后端 Knife4j `/doc.html` 是**对照验证工具**，不是契约源
- ✅ 前端 TypeScript 类型**从 Markdown 文档中的 interface 代码块复制**
- ❌ 代码写完了再补文档——违规
- ❌ 一方单独改代码不同步文档——违规

## 文档清单

| 文档 | 面向 | 作用 |
|------|------|------|
| [BACKEND-CONTRACT.md](./BACKEND-CONTRACT.md) | 后端开发者 | 后端实现时必须遵守的编码规范（命名、注解、异常、枚举） |
| [FRONTEND-CONTRACT.md](./FRONTEND-CONTRACT.md) | 前端开发者 | 前端实现时必须遵守的编码规范（API 客户端、Mock、类型同步） |
| [CHANGE-FLOW.md](./CHANGE-FLOW.md) | 所有人 | 契约变更流程（改 Markdown → 前后端同步实现 → 对照验证） |

## 契约本体（不在本目录）

| 路径 | 内容 |
|------|------|
| [`contract/README.md`](../../contract/README.md) | 契约目录使用说明 |
| [`contract/CONVENTIONS.md`](../../contract/CONVENTIONS.md) | 全局约定（R 包装、分页、时间、错误码号段、命名） |
| [`contract/modules/<module>.md`](../../contract/modules/) | 按模块拆分的 API 设计文档（接口定义的权威源） |

## 快速入口

### 我是**架构师/产品**，要新增一个接口
1. 读 [`contract/CONVENTIONS.md`](../../contract/CONVENTIONS.md) 理解全局约定
2. 在 [`contract/modules/<module>.md`](../../contract/modules/) 里按模板写清楚：URL、Method、请求体、响应体、错误码
3. 提交 PR **仅改 contract/**
4. 通知前后端按文档实现

### 我是**后端开发者**
1. 读 `contract/modules/<module>.md` 对应小节
2. 读 [BACKEND-CONTRACT.md](./BACKEND-CONTRACT.md) 了解编码规范
3. 实现 Controller + DTO，补 `@Schema` 注解
4. 启动后对照 `/doc.html`，与契约文档**逐字段核对**

### 我是**前端开发者**
1. 读 `contract/modules/<module>.md` 对应小节
2. 读 [FRONTEND-CONTRACT.md](./FRONTEND-CONTRACT.md) 了解编码规范
3. 复制文档中的 `interface` 代码块到 `src/api/types/<module>.d.ts`
4. 在 `src/api/<module>.js` 实现 API 函数
5. Mock 数据按文档的响应示例写

### 我是**改接口的开发者**（破坏性变更）
1. 读 [CHANGE-FLOW.md](./CHANGE-FLOW.md) 走完整流程
2. **先改文档**，走 PR 让前后端看到
3. 通知前后端对齐时间窗口
4. 同一天合并，避免半成品

## 禁止事项

- ❌ 前端**禁止**手写与 `contract/modules/*.md` 不一致的类型定义
- ❌ 前端**禁止**在组件里直接调 `axios`，必须通过 `src/api/<module>.js`
- ❌ 后端**禁止**返回契约文档未定义的字段
- ❌ 后端**禁止**绕过 `BizException(ErrorCode)` 抛业务异常
- ❌ 双方**禁止**"先这样，回头补文档"

## 违规后果

- CI 流水线会拦截 PR
- 合入后的违规会在每周契约巡检中被标记
- 连续违规会升级为技术债进入排期
