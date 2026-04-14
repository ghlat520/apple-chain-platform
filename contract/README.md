# AppChain 前后端契约（权威源）

> 本目录是 AppChain 苹果产业链平台**前后端契约的唯一权威源**。
> 前端、后端、Mock 数据**全部**按本目录下的 Markdown 设计文档实现。
> 任何不一致，以本目录为准。

## 核心原则：设计文档即契约（Spec-First）

```
     ┌─────────────────────────────┐
     │  contract/**/*.md           │
     │  ↑ 唯一权威源 (Git 版本化)  │
     └────┬──────────────┬─────────┘
          │              │
          ▼              ▼
     后端 Java       前端 JavaScript
     按文档实现      按文档消费
          │              │
          ▼              ▼
      Knife4j        Mock 数据
     /doc.html       按文档写
          │              │
          └──── 对照 ────┘
              （人工 / 脚本）
```

- ✅ **先改文档**，再改代码（双侧都一样）
- ✅ **后端 Knife4j /doc.html** 仅作**对照验证**工具，不是契约源
- ✅ **前端 TypeScript 类型**从本目录的 Markdown 中的 `interface` 代码块提取
- ❌ 禁止"代码写完了再补文档"
- ❌ 禁止一方单独改代码不同步文档

## 目录结构

```
contract/
├── README.md              # 本文件 —— 总览与使用说明
├── CONVENTIONS.md         # 全局约定（返回结构/分页/时间/错误码/命名）
├── modules/               # 按后端模块拆分的 API 设计文档
│   ├── user.md            # 用户模块（登录/认证/权限）
│   ├── planting.md        # 种植模块（果园/农民）
│   ├── trace.md           # 溯源模块
│   ├── chain.md           # 产业链
│   ├── trade.md           # 交易
│   ├── warehouse.md       # 仓储
│   ├── coldchain.md       # 冷链
│   ├── input.md           # 农资
│   ├── iot.md             # IoT
│   ├── finance.md         # 金融
│   └── bigdata.md         # 大数据
└── CHANGELOG.md           # 契约变更历史（可选，大改时写）
```

## 模块契约索引

| 模块 | 文件 | 端点数 | 错误码段 | 状态 |
|------|------|--------|----------|------|
| 用户认证 | [user.md](modules/user.md) | — | 100000-199999 | ✅ 已完成 |
| M1 种植 | [planting.md](modules/planting.md) | 48 | 200000-299999 | ✅ 已完成 |
| M3 溯源 | [trace.md](modules/trace.md) | 26 | 300000-399999 | ✅ 已完成 |
| 区块链存证 | [chain.md](modules/chain.md) | 4 | 400000-499999 | ✅ 已完成 |
| M4 交易 | [trade.md](modules/trade.md) | 46 | 500000-599999 | ✅ 已完成 |
| M5 仓储 | [warehouse.md](modules/warehouse.md) | 23 | 600000-699999 | ✅ 已完成 |
| M6 冷链 | [coldchain.md](modules/coldchain.md) | 35 | 800000-899999 | ✅ 已完成 |

## 如何使用

### 我是**产品/架构师**，要新增一个接口
1. 先在 `modules/<module>.md` 里写清楚：URL、Method、请求体、响应体、错误码
2. 提交 PR **仅改 contract/**
3. 通知前后端按文档实现

### 我是**后端开发者**
1. 读 `modules/<module>.md` 对应小节
2. 按 `CONVENTIONS.md` 的命名/包装/异常规范实现 Controller + DTO
3. 补全 `@Schema` 注解，让 Knife4j `/doc.html` 与设计文档一致
4. **不允许**自行发明新字段；有需求先改 `modules/<module>.md`

### 我是**前端开发者**
1. 读 `modules/<module>.md` 对应小节
2. 把文档中的 `interface` 代码块复制到 `src/api/types/<module>.d.ts`
3. 按文档中的 URL/Method 在 `src/api/<module>.js` 写 API 函数
4. Mock 数据按文档中的响应示例写，`ajv` + 从 Markdown 中提取的 JSON Schema 校验
5. **不允许**自行发明字段；有需求先改 `modules/<module>.md`

## 相关规范文档

| 文档 | 内容 |
|------|------|
| [../docs/contract/BACKEND-CONTRACT.md](../docs/contract/BACKEND-CONTRACT.md) | 后端实现时的编码规范（命名、注解、异常、枚举） |
| [../docs/contract/FRONTEND-CONTRACT.md](../docs/contract/FRONTEND-CONTRACT.md) | 前端实现时的编码规范（API 客户端、Mock、环境开关） |
| [../docs/contract/CHANGE-FLOW.md](../docs/contract/CHANGE-FLOW.md) | 契约变更完整流程 |
| [./CONVENTIONS.md](./CONVENTIONS.md) | 全局数据约定（R 包装、分页、时间、错误码号段） |

## 禁止事项

- ❌ 手工编辑 `modules/*.md` 中的 TypeScript interface 代码块**不通知前后端**
- ❌ 后端 Controller 字段与 `modules/*.md` 不一致
- ❌ 前端手写与 `modules/*.md` 不一致的类型
- ❌ Mock 数据不符合 `modules/*.md` 响应示例的结构
- ❌ 跳过 `contract/` 直接改代码（"小改动回头补"——不行）

## 违规后果

- CI 守护：后端 Knife4j 产出的 `/v3/api-docs` 与 `contract/` 中文档的结构不一致会报警
- Code Review 一票否决
- 连续违规计入技术债跟踪
