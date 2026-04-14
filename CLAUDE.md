# apple-chain-platform 项目说明

> 苹果产业链平台（AppChain）：Spring Boot 3.2.3 + Java 17 + Maven 多模块 + Vue 3 前端

## ⛔ 契约变更铁律（P0，必读）

**任何涉及前后端数据交互的修改，必须先读 `docs/contract/` 下的规范文档。**

- **任何改动前** → 先读 [`contract/modules/<module>.md`](contract/modules/) 查契约（**唯一权威源**）
- **后端改 Controller/DTO/Enum/ErrorCode** → 读 [`docs/contract/BACKEND-CONTRACT.md`](docs/contract/BACKEND-CONTRACT.md)
- **前端改 API 调用** → 读 [`docs/contract/FRONTEND-CONTRACT.md`](docs/contract/FRONTEND-CONTRACT.md)
- **修改契约本身** → 读 [`docs/contract/CHANGE-FLOW.md`](docs/contract/CHANGE-FLOW.md) 走完整流程

核心原则：**Markdown 设计文档即契约**（Single Source of Truth）。后端按 `contract/modules/*.md` 实现 Java Controller/DTO；前端从 Markdown 的 `interface` 代码块**手动复制**到 `src/api/types/*.d.ts`，禁止从代码反向生成、禁止自行脑补字段。Knife4j `/doc.html` 是**对照验证工具**，不是契约源。

## 项目结构

```
apple-chain-platform/
├── apple-common/          # 公共基础（R、PageResult、异常、常量）
├── apple-domain/          # 领域层（DO、Mapper）
├── apple-module-user/     # 用户模块
├── apple-module-planting/ # 种植模块（果园、农民）
├── apple-module-trace/    # 溯源模块
├── apple-module-chain/    # 产业链
├── apple-module-trade/    # 交易
├── apple-module-warehouse/# 仓储
├── apple-module-coldchain/# 冷链
├── apple-module-input/    # 农资
├── apple-module-iot/      # IoT
├── apple-module-finance/  # 金融
├── apple-module-finance-gateway/
├── apple-module-bigdata/  # 大数据
├── apple-web/             # 聚合启动入口（单体 8080 端口）
├── apple-web-ui/          # 生产前端（Vue 3 + Vant，RBAC 完整）
├── frontend/              # 演示前端（Vue 3 + Vant，带 Mock 层）
├── contract/              # 契约权威源（Markdown 设计文档）
│   ├── README.md
│   ├── CONVENTIONS.md     # 全局约定（R 包装/分页/时间/错误码号段/命名）
│   └── modules/           # 按模块拆分的 API 设计文档
└── docs/
    └── contract/          # 契约规范文档（如何按 contract/ 实现代码）
```

## 本地启动

```bash
mvn clean compile test-compile  # 验证编译（含测试）
mvn -pl apple-web spring-boot:run  # 启动单体
# 访问 http://localhost:8080/doc.html 查看 Knife4j API 文档
```

## 关键约定（摘要，详见 docs/contract/）

- 统一返回：`R<T>` 包装 `{code, message, data}`
- 分页：`PageParam` 入参，`PageResult<T>` 出参
- 时间：`LocalDateTime` + `yyyy-MM-dd HH:mm:ss` + Asia/Shanghai
- 异常：`throw new BizException(XxxErrorCode.YYY)`，全局 `GlobalExceptionHandler` 统一处理
- 枚举：实现 `BaseEnum` 接口，序列化为 `{code, desc}` 对象
- DTO 命名：`XxxRequest` / `XxxResponse` / `XxxDO` / `XxxBO`（禁用 `DTO` / `VO` / `Param`）

## 相关工具

- **Knife4j**：http://localhost:8080/doc.html （中文 Swagger UI）
- **OpenAPI JSON**：http://localhost:8080/v3/api-docs
- **契约权威源**：`contract/modules/*.md`（架构师手写，PR 管理）
- **对照验证产物**：`contract/openapi.json`（`mvn -Pcontract verify` 生成，仅用于与 Markdown 对照校验，非权威源）

---

## ⛔ UI 代码生成硬契约（P0，必读）

**策略：存量冻结 / 增量合规**
- **存量页面**（已存在的 .vue 文件）：不主动重构，保持现状
- **新增页面**：严格遵循 `DESIGN.md`，lint 必须 exit 0
- **存量页面修改**：改到的地方顺手合规化（童子军规则），未改动区域不动

**所有新增前端 UI 代码都必须遵循 `DESIGN.md`。**

### 生成前必读（不可跳过）
1. Read `./DESIGN.md` — UI 唯一真源（15 段大师版）
2. Read `./apple-web-ui/src/design/tokens.js` — 可用 token
3. Read `./apple-web-ui/src/design/vant-theme.css` — CSS 变量和 Vant 主题覆盖
4. Glob `./apple-web-ui/src/components/**/*.vue` — 优先复用已有组件
5. 开发新组件前，启动 dev server 访问 `/styleguide` 看色板/字体/组件/状态

### Lint 使用模式
- **日常提交**：`bash scripts/design-lint.sh --staged`（仅扫已 staged 文件，pre-commit）
- **PR 检查**：`bash scripts/design-lint.sh --diff origin/main`（仅扫本分支变更）
- **基线审计**：`bash scripts/design-lint.sh`（全量，季度审计用，不卡流程）
- **指定文件**：`bash scripts/design-lint.sh src/views/Xxx.vue`

### 硬性约束（增量模式下违反即被拦截）
- **禁止硬编码颜色** — 必须用 CSS 变量（`var(--color-brand-primary)`）或 tokens.js
- **禁止硬编码 px 尺寸** — 必须用 `var(--space-4)` / `var(--density-button-height)`
- **禁止硬编码 font-family** — 必须用 `var(--font-body)` / `var(--font-mono)`
- **禁止 `transition: all`** — 必须指定具体属性
- **禁止 `outline: none`** — 破坏焦点环无障碍
- **禁止 `!important`**
- **禁止绑定未经契约审核的 DTO 字段**（见前述契约铁律）

### 本产品专属禁忌
- **禁止霓虹色 / 荧光色 / 高饱和紫洋红** — 破坏政务可信度
- **禁止深色模式作为默认** — 果农户外强光不可读
- **禁止彩色卡通插画 / 果农拟人化**
- **禁止红绿涨跌动画**（本产品非金融交易）
- **禁止正文 < 16px**（中老年视力）
- **禁止自定义字体**（包体 + 弱网）
- **禁止 Light 300 字重**（显廉价）
- **禁止 "亲 / 宝宝 / 立即" 等 cutesy / 营销话术**
- **禁止 Emoji 出现在金融/监管/溯源严肃路径**

### 组件状态齐全性
- **交互 4 态**：default / hover / active / disabled + `:focus-visible`
- **系统 4 态**：empty / loading / error / success

### 数据可视化规则
- 本产品**非金融交易产品**，禁止股价红绿涨跌语义
- 冷链温度用专属色板（`--color-temp-normal` / warning / critical）
- ECharts 必须通过 `apple-chain-theme` 加载，禁止 inline 配色
- 数字字段必须加 `.nums-tabular` class + mono 字体

### 6 角色密度差异
- 果农 / 冷链司机 → `[data-density="spacious"]`
- 合作社 / 监管 → `[data-density="comfortable"]`（默认）
- 管理员 / 金融机构 → `[data-density="compact"]`
- 登录后按 RBAC 自动切换，用户可在设置页覆盖

### 生成后自检（必须输出）
```
## 自检清单
- [ ] 所有颜色走 CSS 变量或 tokens.js
- [ ] 所有尺寸走 CSS 变量或 tokens.spacing
- [ ] 组件 4 交互态齐全
- [ ] 页面 4 系统态齐全
- [ ] :focus-visible 可见
- [ ] prefers-reduced-motion 已处理
- [ ] 正文字号 ≥ 16px
- [ ] 数字字段用 .nums-tabular 对齐
- [ ] 已跑 bash scripts/design-lint.sh 且 exit 0
```

### 参考资料
- 完整规范：`./DESIGN.md`
- 品牌灵魂：`./docs/design/brand-soul.md`
- Tokens 代码：`./apple-web-ui/src/design/tokens.js`
- Vant 主题覆盖：`./apple-web-ui/src/design/vant-theme.css`
- 视觉 QA 页：dev server 后访问 `/styleguide`
- Lint 脚本：`bash scripts/design-lint.sh`
- 共享设计库：`/Applications/soft/CodeSpace/design-md-library/`

---

## ⛔ Hard Gate：前端页面生成/修复（P0，四次血泪教训）

> 04-02 / 04-12 / 04-13 / 04-13 共四次全量修复，根因相同：子Agent没读后端代码就写前端。

**触发词**：生成前端页面 / 修复前端 / 字段映射 / Vue页面 / 批量修复页面

### 铁律：每次调用 Frontend Developer Agent 处理 Vue 页面时，prompt 中必须包含以下指令

```
IRON RULE — 前端字段映射（违反此规则的产出将全部作废）:
1. 修改任何 Vue 页面前，必须先 Read 对应的后端 Controller.java → 提取精确 API 路径 + HTTP 方法 + @RequestParam 参数名
2. 必须 Read 对应的 Entity/DO 类 → 提取精确字段名和类型（是 plateNumber 还是 plateNo？是 createTime 还是 createdAt？）
3. 必须从 Entity 注释或枚举类提取精确枚举值（是 PENDING 还是 PLANNED？是 APPROVED 还是 ACTIVE？）
4. 后端没有的接口，前端禁止调用（不要假设"应该有 delete"就写 delete）
5. 后端 @RequestParam 的参数，前端不能发 JSON body；后端 @RequestBody 的参数，前端不能发 query param
6. 禁止猜测任何字段名。没读过的字段 = 不存在的字段。
```

### 六大类高频问题速查

| 类型 | 占比 | 典型错误 | 防御 |
|------|------|---------|------|
| 字段名不匹配 | 60% | `plateNo` vs `plateNumber` | 从Entity逐字复制 |
| 枚举值不同步 | 15% | `PLANNED` vs `PENDING` | 读Entity注释/枚举类 |
| API请求方式错 | 10% | JSON body vs @RequestParam | 读Controller方法签名 |
| 响应结构不匹配 | 5% | 期望数组，实际是Map | 读返回的VO类 |
| 后端不存在的功能 | 5% | 调用不存在的delete接口 | 确认Controller有此endpoint |
| 数据类型不匹配 | 5% | boolean vs Integer 0/1 | 读Entity字段类型 |

### 审计后置验证

大批量修改后，必须启动独立审计Agent做交叉验证：对比每个Vue页面的字段与后端Entity的字段是否一一对应。
