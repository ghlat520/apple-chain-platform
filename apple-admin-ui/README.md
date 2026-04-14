# apple-admin-ui

苹果产业链平台 **PC 管理后台**（管理员 / 金融机构 / 监管角色）。

技术栈：Vue 3 + Vite + Element Plus 2.x + ECharts + Pinia + Axios。

## 设计体系

本项目与 `apple-web-ui`（Vant 移动端）共享同一套设计语言：颜色、间距、字号、字重、圆角、阴影、动效。
**唯一真源**是项目根目录的 `DESIGN.md`，组件库不同（Element Plus vs Vant）只是终端差异。

### 三件套（必读）

| 文件 | 职责 |
|------|------|
| `src/design/tokens.js` | JS tokens（与 `apple-web-ui/src/design/tokens.js` 字段一致） |
| `src/design/element-theme.css` | `:root` 注入 CSS 变量 + 覆盖 Element Plus `--el-*` 变量 |
| `src/design/global.css` | 字体栈 / `.nums-tabular` / 焦点环 / `prefers-reduced-motion` |

三者已在 `src/main.js` 中按顺序导入：

```js
import 'element-plus/dist/index.css'
import './design/element-theme.css'   // 必须在 element-plus 之后
import './design/global.css'
import './styles/index.scss'           // 项目布局样式 (存量)
```

### 新增页面铁律

- **禁止硬编码** 颜色 / 尺寸 / 字体 — 必须走 CSS 变量（`var(--color-brand-primary)` / `var(--space-4)` / `var(--font-body)`）或从 `tokens.js` 引入
- **禁止 `transition: all`** — 必须指定具体属性
- **禁止 `outline: none`** — 焦点环由 `global.css` 统一管理
- **禁止 `!important`**
- **禁止引入新的 UI 库** — 只用 Element Plus 已有组件
- **禁止升级 Element Plus 版本** — 锁定 `^2.6.1`
- **数字字段** 必须加 `class="nums-tabular"` 保证表格列对齐
- **新增页面前** 必须用 `/gen-page` Skill 跑后端优先流程（详见 `apple-admin-ui/CLAUDE.md`）

### 密度模式

PC 后台默认 `compact`（按钮 40px / 字号 14px），通过 `data-density` 切换：

```html
<body data-density="comfortable">  <!-- 48px / 16px -->
<body data-density="spacious">     <!-- 56px / 18px -->
```

切换后 Element Plus 组件高度、字号、Cell padding 自动响应。

### Lint

```bash
# 仅扫 staged 文件 (pre-commit)
bash scripts/design-lint.sh --staged

# 扫指定文件
bash scripts/design-lint.sh apple-admin-ui/src/views/login/Index.vue

# 全量基线审计
bash scripts/design-lint.sh
```

## 开发

```bash
npm install
npm run dev      # http://localhost:5174
npm run build
```

## 后端联调

- 默认代理 `/api` → `http://localhost:8080`（见 `vite.config.js`）
- API 调用前必读 `docs/contract/FRONTEND-CONTRACT.md`
