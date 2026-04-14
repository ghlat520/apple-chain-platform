# 契约变更流程（CHANGE-FLOW）

> **适用场景**：任何涉及前后端数据交互的变更
> **强制级别**：P0（不遵守 → CR 驳回 + CI 拦截）

## 核心原则

1. **Markdown 设计文档是唯一权威源**——先改 `contract/modules/*.md`，再改代码
2. **文档 PR 必须独立先行**——不允许"文档和代码一起提"
3. **前后端独立按文档实现**——双方都不允许自行脑补字段
4. **破坏性变更必须前置通知**——字段重命名、字段删除、类型变更

---

## 完整流程图

```
┌─────────────────────────────────────────────────────────────┐
│   1. 提出变更者（架构师 / 产品 / 业务负责人）               │
│   在 contract/modules/<module>.md 修改接口定义              │
│   - 新增：按 CONVENTIONS.md 模板补完整 6 块                 │
│   - 修改：更新字段表 + interface + 响应示例 + 变更历史      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│   2. 仅改 contract/ 的 PR                                   │
│   git add contract/modules/<module>.md                      │
│   git commit -m "contract(user): 登录响应增加 captcha 字段" │
│   → 不带任何后端/前端代码改动                               │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│   3. Code Review（契约层）                                  │
│   - 命名合规？符合 CONVENTIONS §8？                         │
│   - 字段类型合规？符合 CONVENTIONS §3/§4？                  │
│   - 错误码在分配号段内？符合 CONVENTIONS §5？               │
│   - interface 代码块与字段表一致？                          │
│   - 响应示例与 interface 一致？                             │
│   合并后自动通知前后端                                      │
└──────────────────────┬──────────────────────────────────────┘
                       │
             ┌─────────┴──────────┐
             ▼                    ▼
┌─────────────────────┐ ┌─────────────────────┐
│ 4a. 后端实现 PR     │ │ 4b. 前端实现 PR     │
│                     │ │                     │
│ - 按 BACKEND-       │ │ - 按 FRONTEND-      │
│   CONTRACT.md 规范  │ │   CONTRACT.md 规范  │
│ - 修改 Controller/  │ │ - 复制 interface 到 │
│   DTO/Enum          │ │   src/api/types/    │
│ - 补 @Schema 注解   │ │ - 写 API 函数       │
│ - 启动对照          │ │ - 写 Mock 数据      │
│   /doc.html         │ │ - 组件接入          │
│ - 人工与文档逐字段  │ │ - 本地切换 Mock/    │
│   核对              │ │   真实后端验证      │
└──────────┬──────────┘ └──────────┬──────────┘
           │                       │
           ▼                       ▼
┌─────────────────────────────────────────────────────────────┐
│   5. 联调验证                                               │
│   - 前端本地 VITE_ENABLE_MOCK=off 对接真实后端              │
│   - 字段对齐？响应包装对齐？错误码对齐？                    │
│   - 发现差异 → 回到第 1 步改 contract/，不要私自补字段     │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│   6. 对照验证（合并前的硬门禁）                             │
│   - 后端启动 → 访问 /v3/api-docs 下载 openapi.json          │
│   - 运行对照脚本：scripts/verify-contract.sh                │
│     比对 /v3/api-docs 与 contract/modules/*.md 的字段       │
│   - 不一致 → CI 失败，拒绝合并                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 场景手册

### 场景 A：新增字段（非破坏性）

**例**：`LoginResponse` 新增 `lastLoginAt` 字段

1. 架构师改 `contract/modules/user.md`：
   - 字段表新增一行
   - `interface LoginResponse` 加一个 `lastLoginAt: string | null;`
   - 响应示例加 `"lastLoginAt": "2026-04-11 10:00:00"`
   - 变更历史写：`2026-04-11 新增 lastLoginAt 字段`
2. 提交 PR 仅改 `contract/user.md`，CR 通过合并
3. 后端开发者：在 `LoginResponse.java` 加字段 + `@Schema` 注解
4. 前端开发者：复制新 interface 到 `src/api/types/user.d.ts`，在需要展示的组件使用

**是否需要通知**：否（字段新增属于向后兼容）。

---

### 场景 B：重命名字段（破坏性）

**例**：`UserResponse.userName` → `UserResponse.username`

1. **提前**在契约 PR 描述标记【破坏性变更】
2. **提前**在群里通知前后端负责人，约定合并窗口
3. 改 `contract/modules/user.md`：字段表、interface、示例全部更新，**变更历史**必须写清楚影响范围
4. 文档 PR 合并
5. 后端和前端**同一天**提交实现 PR，同步合并、同步发布

**兼容期策略**（强烈推荐）：
- 后端同时保留 `userName` 和 `username` 两个字段 1 个版本
- 前端迁移完成后，再提第二个契约 PR 删除 `userName`

---

### 场景 C：新增错误码

**例**：新增 `100006 密码复杂度不够`

1. 改 `contract/modules/user.md`：
   - 在相关接口的「错误码」字段加 `100006`
   - 在模块末尾的「错误码表」加一行
2. 契约 PR 合并
3. 后端：在 `UserErrorCode.java` 加一条枚举 + 对应校验逻辑
4. 前端：在 `src/api/request.js` 的错误处理中增加 `100006` 的 UI 提示

---

### 场景 D：新增整个模块

**例**：新增 `report` 模块

1. 架构师新建 `contract/modules/report.md`，按 CONVENTIONS 模板写完整
2. 申请错误码段（比如 `1200000 ~ 1299999`），更新 `CONVENTIONS.md §5`
3. 契约 PR 合并
4. 后端：新建 `apple-module-report` 模块，按规范实现
5. 前端：新建 `src/api/report.js` 和 `src/api/types/report.d.ts`

---

## 破坏性变更定义

以下变更**必须**标记为破坏性并提前通知：

| 变更类型 | 示例 |
|---------|------|
| 字段删除 | 删除 `UserResponse.avatar` |
| 字段重命名 | `userName` → `username` |
| 字段类型变更 | `age: number` → `age: string` |
| 必填性变更 | 可选字段变必填（前端不传会失败） |
| 枚举值删除 | `UserStatus` 删除 `LOCKED` |
| URL 路径变更 | `/api/user/login` → `/api/auth/login` |
| HTTP 方法变更 | `POST` → `PUT` |
| 错误码语义变更 | `100001` 从"用户不存在"改为"密码错误" |

---

## 对照验证机制

### 机制一：Knife4j 人工对照

- 后端开发者实现完成后，启动应用 → 访问 `http://localhost:8080/doc.html`
- 与 `contract/modules/<module>.md` **逐字段**核对
- 发现不一致 → 改代码（如果是实现错）或改文档（如果文档不准）

### 机制二：脚本对照（CI 守护）

> **Phase 3 交付物**（本规范文档提到但尚未实现）：`scripts/verify-contract.sh`

```bash
# 伪代码
1. mvn -Pcontract verify  # 后端生成 /v3/api-docs → contract/openapi.json（对照产物，不是权威源）
2. 从 contract/modules/*.md 提取所有 interface 代码块，解析成 JSON Schema
3. 比对 openapi.json 中的 schemas 与提取的 JSON Schema
4. 任何字段差异 → 报错并列出差异路径
```

- 本对照脚本仅用于**验证后端实现是否忠实于契约文档**
- 脚本报错 → 必须改代码或改文档使两者一致
- **权威源始终是 Markdown 文档**

### 机制三：前端 Mock 数据校验

- Mock 数据按 `contract/modules/*.md` 的响应示例编写
- `scripts/validate-mock.js` 从 Markdown 提取 JSON Schema → `ajv` 校验 Mock 数据
- 不符合 → CI 失败

---

## 常见问题

### Q1: 文档和代码不一致了，哪个算数？

A: **文档算数**。代码改到与文档一致。如果发现文档真的有错，先改文档（走契约 PR），再改代码。

### Q2: 紧急修复线上 bug，能跳过契约 PR 吗？

A: 可以**顺序倒置**但不能跳过：
1. 先改代码发布止血
2. 当天内补契约 PR 更新文档
3. 两个 PR 关联起来，变更历史注明"紧急修复后补文档"
4. 不允许：永远只改代码不改文档。

### Q3: 我只是加了一个字段，前端用不上，还要走契约 PR 吗？

A: 必须走。今天前端用不上，明天就可能用。而且没有文档变更记录，半年后没人知道这个字段是干什么的。

### Q4: 后端 `@Schema` 注解和 Markdown 文档有矛盾，以哪个为准？

A: Markdown 为准。`@Schema` 是后端开发者**对照文档写的实现细节**，Markdown 才是设计。两者矛盾说明后端实现偏离了设计，要么改注解对齐，要么走契约 PR 修订设计。

### Q5: 两套前端（frontend / apple-web-ui）都要同步吗？

A: 是。两者都从 `contract/modules/*.md` 复制 interface，都按 `FRONTEND-CONTRACT.md` 规范写代码。规范验证先在 `frontend` 试点，稳定后推广到 `apple-web-ui`。

### Q6: 文档中的 `interface` 代码块可以嵌入更复杂的 TS 类型吗（比如泛型）？

A: 可以，但要保证**前端复制过去可以直接编译**。如果需要引用公共类型（如 `ApiResponse<T>`），在文件顶部说明 import 路径。

---

## 应急处理

### 契约 PR 合并后发现错误

1. 立即提第二个契约 PR 修正
2. 在变更历史里注明"修正 XXX 错误"
3. 如果前后端已按错的文档实现 → 一并回滚

### 前后端实现偏离契约文档

1. 在 CR 阶段发现 → 打回重改
2. 合入后发现 → 开紧急 PR 对齐
3. 反复偏离同一人 → 走 1-1 对齐，不是规范问题

### CI 对照脚本误报

1. 核对：是不是 Markdown 格式问题（比如 interface 代码块缺 language tag）
2. 如果确是脚本 bug → 提 PR 修脚本，不允许临时禁用对照

---

## 相关文档

- [README.md](./README.md) — 规范总览
- [BACKEND-CONTRACT.md](./BACKEND-CONTRACT.md) — 后端实现规范
- [FRONTEND-CONTRACT.md](./FRONTEND-CONTRACT.md) — 前端实现规范
- [../../contract/README.md](../../contract/README.md) — 契约本体目录
- [../../contract/CONVENTIONS.md](../../contract/CONVENTIONS.md) — 全局约定
