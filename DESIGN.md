# DESIGN.md — apple-chain-platform 苹果产业链平台

> **UI 唯一真源。生成任何 UI 代码前必须先读本文件。**
> 技术栈：Vue 3 + Vant 4 + ECharts + 高德地图
> 基于 `docs/design/brand-soul.md` 落地 15 段大师版

---

## 0. Project Context

### 产品定位
苹果产业链数字化协同平台 —— 从果农种植到冷链物流到消费者溯源的全链路管理系统，**同时服务农业生产者、合作社、监管机构、金融机构**四类角色。

### 目标用户
6 角色 RBAC：**果农 / 合作社 / 冷链司机 / 管理员 / 金融机构 / 监管**
详见 `docs/design/brand-soul.md` 角色快照。

### 品牌灵魂三词
1. **扎根** — 政务级踏实可信
2. **清澈** — 溯源透明无灰色
3. **温厚** — 农业人情味

### 内在张力
"像农业部的专业系统一样可信，但像微信一样果农一键就会用。"

### 绝对禁忌（Top 5）
1. 禁止霓虹色 / 荧光色 / 高饱和紫洋红
2. 禁止深色模式作为默认（户外强光不可读）
3. 禁止彩色卡通插画 / 拟人化果农
4. 禁止交易产品式红绿涨跌动画
5. 禁止 Bloomberg 风高密度仪表盘（桌面管理端例外）

---

## 1. Visual Theme & Atmosphere

apple-chain 的视觉哲学是"**果园清晨的暖光 + 政务系统的纸张感**"。基底不是冷白（`#FFFFFF`），而是微微偏暖的**纸张米白** `#FAFAF8`——这一点点黄色偏移让整个平台离开"SaaS 科技感"的冷气质，靠近"农业档案室"的踏实气质。这是本产品和任何互联网 SaaS 最明显的视觉分水岭。

主品牌色是**苹果红** `#D32F2F`（Material Red 700），严格作为唯一品牌点缀出现——主按钮、焦点环、关键通知徽标、选中态。绝不做背景大面积铺设（那是赌场/电商的做法）。辅助色是**果叶绿** `#43A047`（Material Green 600），只在溯源通过、操作成功、健康状态等**语义正向**场景出现，不做装饰。

字体选择遵循微信生态原则——**完全依赖系统字体**，不引入自定义字体。西文 Inter + 中文 PingFang SC (iOS) / 思源黑体 (Android)，正文字号 16px（比 WeUI 的 17sp 略小，但比互联网产品的 14px 大 2px 以顾及 40–60 岁用户视力），行高放宽到 1.6。数字场景使用 JetBrains Mono 等宽字体 + tabular-nums，仅用于价格、重量、温度、GPS 坐标这些**需要对齐**的数据列。

布局节奏学习 Vant 原生——15px 左右 padding + Cell 为核心原子组件——但在组件内部细节加入更多的圆角和温度。Vant 默认圆角 0/2px 的硬边被替换为 8px 的亲和圆角（按钮/卡片），营造"可以摸得到"的触感。这是本产品和 Vant 默认视觉最主要的差异。

动效极其克制。所有过渡 ≤ 250ms，禁止无限循环动画。唯一允许的"强调动画"是溯源码扫码成功时的 400ms 勾选放大反馈，以及冷链温度超限时的 2s 一次边框呼吸。这些是**功能性动画**，不是装饰。

**Key Characteristics：**
- 暖米白底 `#FAFAF8`，不是冷白
- 苹果红 `#D32F2F` + 果叶绿 `#43A047` 双点缀色，绝不大面积铺设
- 圆角 8px 亲和触感，替代 Vant 默认硬边
- 系统字体 + 正文 16px 宽松行高 1.6（照顾中老年视力）
- 数字场景独立使用等宽字体
- 信息密度分级：果农场景疏朗 / 管理端密集
- 户外强光优先设计 —— 对比度 ≥ 7:1 远超 WCAG AA
- 禁止无限动画，只保留功能性反馈
- 深色模式存在但默认关闭，仅用户主动开启

---

## 2. Color Palette & Roles

### Primary / Brand
- **Apple Red** (`#D32F2F`): 主按钮 / 焦点环 / 关键徽标 / 品牌签名 —— **唯一**品牌色
- **Apple Red Hover** (`#C62828`): hover 态
- **Apple Red Active** (`#B71C1C`): 按下态
- **Apple Red Light** (`#FFEBEE`): 极轻微的红色背景（通知条、高亮行）
- **Apple Red Disabled** (`rgba(211, 47, 47, 0.35)`): 禁用态

### Accent
- **Leaf Green** (`#43A047`): 溯源通过 / 认证图标 / 健康状态 —— 语义正向专用
- **Leaf Green Light** (`#E8F5E9`): 成功态背景

### Text
- **Text Primary** (`#2C2E32`): 主文字，偏暖的黑，比纯黑 `#000` 更柔和
- **Text Secondary** (`#5E6368`): 次要文字 / 说明
- **Text Tertiary** (`#8A9099`): 占位符 / 辅助信息
- **Text Disabled** (`#BDC1C7`): 禁用态
- **Text Inverse** (`#FFFFFF`): 深色底上的白字
- **Text Link** (`#D32F2F`): 链接用苹果红（和主色一致，符合"一个品牌色"原则）

### Surface
- **Surface Base** (`#FAFAF8`): 页面最底层，**暖米白，非冷白**
- **Surface Raised** (`#FFFFFF`): 卡片 / Cell / 白色背景
- **Surface Overlay** (`#FFFFFF`): Modal / Dialog / Popover（带阴影）
- **Surface Sunken** (`#F5F5F2`): Input 凹陷 / 分组间隔底色

### Border & Divider
- **Border Default** (`rgba(44, 46, 50, 0.08)`): 默认边框
- **Border Strong** (`rgba(44, 46, 50, 0.16)`): 强调边框
- **Divider** (`rgba(44, 46, 50, 0.06)`): 0.5px 分隔线

### Semantic
- **Success** (`#43A047`): 成功 —— 和 Leaf Green 共用
- **Success BG** (`#E8F5E9`)
- **Warning** (`#F57C00`): 警告 / 待处理（比 Material Orange 深一档，避免商业感）
- **Warning BG** (`#FFF3E0`)
- **Error** (`#D32F2F`): 错误 —— 和品牌苹果红共用（本产品非交易，可以这样复用）
- **Error BG** (`#FFEBEE`)
- **Info** (`#1976D2`): 信息提示（可信的政务蓝）
- **Info BG** (`#E3F2FD`)

### 🌡️ IoT / 冷链数据可视化（非交易涨跌）
- **Temperature Normal** (`#1976D2`): 温度正常范围（蓝色 = 冷藏安全）
- **Temperature Warning** (`#F57C00`): 温度偏离（橙色）
- **Temperature Critical** (`#D32F2F`): 温度超限（红色）
- **Humidity** (`#00838F`): 湿度（青色）
- **GPS Track** (`#43A047`): 物流轨迹（绿色）

### 图表序列调色盘（管理端仪表盘，8 色色盲友好）
1. `#D32F2F` 苹果红（品牌）
2. `#43A047` 果叶绿
3. `#1976D2` 政务蓝
4. `#F57C00` 橙
5. `#00838F` 青
6. `#6A1B9A` 紫（只在图表序列出现）
7. `#FBC02D` 黄
8. `#546E7A` 灰蓝

> **禁止**把品牌苹果红用于图表中的其他数据序列（只代表品牌本身或关键指标）。

### Dark Mode（非默认，仅用户主动启用）
- Surface Base: `#141414`
- Surface Raised: `#1E1E1E`
- Text Primary: `rgba(255,255,255,0.92)`
- 其他反转对应

---

## 3. Typography Rules

### Font Family
- **西文 Body/Display**: `Inter, -apple-system, "Helvetica Neue", sans-serif`
- **中文 Body**: `"PingFang SC", "Source Han Sans SC", "Microsoft YaHei", sans-serif`
- **Mono（数字专用）**: `"JetBrains Mono", "SF Mono", Consolas, monospace`
- **禁止**：引入自定义字体文件（增大包体，户外弱网加载慢）

### Hierarchy

| 角色 | 字号 | 字重 | 行高 | 字间距 | 用途 |
|------|------|------|------|--------|------|
| Display L | 28px | 600 | 1.3 | 0 | 页面顶部大标题（罕用） |
| Heading 1 | 22px | 600 | 1.35 | 0 | 区块主标题 |
| Heading 2 | 18px | 600 | 1.4 | 0 | 子区块标题 / 卡片标题 |
| Heading 3 | 16px | 500 | 1.5 | 0 | 列表项标题 |
| Body L | 17px | 400 | 1.6 | 0 | 长文本 / 说明文档 |
| **Body** | **16px** | **400** | **1.6** | **0** | **默认正文（比互联网+2px）** |
| Body S | 14px | 400 | 1.5 | 0 | 次要正文 |
| Caption | 13px | 400 | 1.4 | 0 | 时间戳 / 辅助 |
| Overline | 12px | 500 | 1.3 | 0.04em | 分类标签 |
| Number L | 24px | 500 | 1.1 | 0 | 温度 / 重量 / 价格大数（mono） |
| Number | 16px | 500 | 1.2 | 0 | 表格数字（mono） |

### Principles
- **字重范围**：400 / 500 / 600 三档。**禁止 300（Light）** 显廉价，**禁止 700+（Bold）** 过重
- **正文 16px 是铁律**：不能为了塞更多信息缩到 14px。果农目标用户视力不允许。
- **长文使用 17px 放宽**：溯源详情、使用说明等长阅读场景用 Body L
- **数字必须等宽** + `font-variant-numeric: tabular-nums`
- **行高 1.6**：比互联网产品的 1.4–1.5 更宽，提升中老年阅读舒适度
- **冷链司机场景**：通过 density mode 切到 spacious 自动加大到 18px

---

## 4. Component Stylings

### Button

#### Primary（主按钮）
- Background: `#D32F2F`
- Text: `#FFFFFF`, 16px / 500
- Padding: 12px 24px
- Height: 48px (默认，适配 Vant) / 56px (spacious/司机) / 40px (compact/管理员)
- **Radius: 8px**（覆盖 Vant 默认的 2px）
- Shadow: none（默认）/ `0 2px 4px rgba(211,47,47,0.2)`（强调态）
- Transition: `background-color 150ms ease-out, transform 150ms ease-out`

**4 态**：
- `:hover` → `#C62828`
- `:active` → `#B71C1C` + `scale(0.98)`
- `:focus-visible` → 2px solid `#D32F2F` + 3px offset + `box-shadow 0 0 0 4px rgba(211,47,47,0.15)`
- `:disabled` → `rgba(211,47,47,0.35)`, cursor not-allowed

#### Secondary
- Background: `#FFFFFF`
- Border: 1px solid `Border Default`
- Text: `Text Primary`
- hover: border `Border Strong`

#### Ghost（次要辅助）
- Background: transparent
- Text: `#D32F2F`
- hover: bg `#FFEBEE`

#### Success（溯源通过 / 审核通过）
- Background: `#43A047`
- Text: `#FFFFFF`
- 其他同 Primary

### Input
- Background: `Surface Sunken`
- Border: 1px solid `Border Default`
- Padding: 0 16px
- Height: 48px（和 Button 对齐）
- Radius: 8px
- Font: Body 16px / 400
- `:focus` → border `#D32F2F`, box-shadow `0 0 0 3px rgba(211,47,47,0.12)`
- Placeholder: `Text Tertiary`

### Cell（列表/表单核心原子，覆盖 Vant 默认）
- Background: `Surface Raised`
- Min height: 56px（比 Vant 默认 44px 高——中老年触控友好）
- Padding: 14px 16px
- Divider: 0.5px solid `Divider`, left margin 16px
- Hover（Desktop）: bg `#FAFAF8`
- Active: bg `#F5F5F2`

### Card
- Background: `Surface Raised`
- Border: 1px solid `Border Default`（默认）
- Radius: 12px（比 Button 大一档，制造卡片感）
- Padding: 16px
- Shadow: none（默认）/ `0 2px 8px rgba(0,0,0,0.04)`（悬浮态）

### Modal / Dialog（Vant van-dialog 覆盖）
- Background: `Surface Overlay`
- Radius: 16px（最大圆角，制造柔和感）
- Padding: 24px
- Shadow: `0 16px 48px rgba(0,0,0,0.15)`
- Backdrop: `rgba(0,0,0,0.5)` + backdrop-blur 2px
- 入场: fade + scale(0.94→1) 250ms ease-out

### Toast（Vant van-toast 覆盖）
- Background: `rgba(44,46,50,0.92)`
- Text: `#FFFFFF`, 14px
- Radius: 8px
- Padding: 12px 16px
- 位置: 屏幕中上部 20% 处
- 时长: 2.5s

### Navigation / Tab Bar（移动底部）
- Background: `#FFFFFF`
- Top border: 0.5px `Divider`
- Height: 54px + safe area
- 未选中: icon 24px, Text Secondary, 10px 字号
- 选中: icon 24px filled, `#D32F2F`, 10px 字号
- 切换动效: fill-in 150ms

### Tag / Badge
- 状态标签（认证/溯源通过/冷链中等）: bg `Leaf Green Light`, text `Leaf Green`
- 预警标签: bg `Warning BG`, text `Warning`
- 错误标签: bg `Apple Red Light`, text `Apple Red`
- Radius: 4px
- Padding: 2px 8px
- Font: 12px / 500

---

## 5. Layout Principles & Spacing Scale

### Spacing Scale

| Token | 值 | 用途 |
|-------|-----|------|
| `space-0` | 0 | |
| `space-1` | 4px | icon 间距 |
| `space-2` | 8px | 紧凑内距 |
| `space-3` | 12px | |
| `space-4` | 16px | **默认间距 / Cell padding** |
| `space-5` | 20px | |
| `space-6` | 24px | 组件间距 |
| `space-8` | 32px | 区块间距 |
| `space-12` | 48px | 大区块 |
| `space-16` | 64px | section |

### 移动端布局（Vant 主场景）
- **页面边距**：16px 左右（比 WeUI 的 15px 略宽）
- **Cell padding**：14px 上下 + 16px 左右
- **分组间距**：20–24px（用 Surface Base 底色分隔）
- **Safe area**：必须处理 iPhone 刘海和 Home 横条，用 CSS env(safe-area-inset-*)

### 桌面管理端布局
- **最大宽度**：1440px 居中
- **侧边栏**：240px 固定
- **主内容区 padding**：24px
- **表格行高**：48px（comfortable）/ 36px（compact）

### Breakpoints

| Name | Min Width | 场景 |
|------|-----------|------|
| xs | 0 | **默认（移动）** |
| sm | 576px | 大屏手机横屏 |
| md | 768px | 平板 |
| lg | 1024px | 桌面 / 管理端 |
| xl | 1280px | 大桌面 |

### Container 约束
- 移动端单列，最大宽度 100%
- 管理端 Card 网格：2 列（<lg）→ 3 列（lg+）→ 4 列（xl+）

---

## 6. Depth & Elevation

### Elevation

| Level | 用途 | Shadow |
|-------|------|--------|
| 0 | 页面底 | none |
| 1 | Card 微浮 | `0 1px 2px rgba(44,46,50,0.04)` |
| 2 | Dropdown / Popover | `0 4px 12px rgba(44,46,50,0.08)` |
| 3 | Modal | `0 16px 48px rgba(0,0,0,0.15)` |
| 4 | Toast / 悬浮按钮 | `0 8px 24px rgba(0,0,0,0.12)` |

### 哲学
**优先用边框 + surface 色差做层次**，阴影仅用于强调浮起感。与 WeUI 的"只用分隔线"相比稍松，但仍远离 Material Design 的大阴影派。

---

## 7. Motion & Easing

### Duration

| Token | 时长 | 用途 |
|-------|------|------|
| `motion-instant` | 0ms | 主题切换 |
| `motion-fast` | 150ms | Button hover / tick |
| `motion-base` | 250ms | Modal / Tab / Popover |
| `motion-slow` | 400ms | 页面过渡 / 扫码成功勾选 |

### Easing

| Token | Value |
|-------|-------|
| `ease-out` | `cubic-bezier(0.16, 1, 0.3, 1)` — **默认入场** |
| `ease-in` | `cubic-bezier(0.7, 0, 0.84, 0)` — 出场 |
| `ease-spring` | `cubic-bezier(0.34, 1.56, 0.64, 1)` — 勾选反馈 |

### Principles
1. 入场 ease-out，出场 ease-in
2. 大部分 ≤ 250ms
3. 只动 `transform` 和 `opacity`
4. **禁止 `transition: all`**
5. **禁止无限循环动画**（唯一例外：冷链超温 2s 一次边框呼吸 + loading skeleton）
6. 遵守 `prefers-reduced-motion`
7. **扫码成功专属动画**：勾选圆圈 scale(0→1) 400ms ease-spring（这是产品核心愉悦时刻）

---

## 8. Density Modes（6 角色差异化）

### 三档密度

| Mode | 用途 | 字号偏移 | 按钮高度 | Cell 高度 |
|------|------|---------|---------|----------|
| **spacious** | 果农 / 冷链司机 | +2px | 56px | 64px |
| **comfortable**（默认） | 合作社 / 监管 | 0 | 48px | 56px |
| **compact** | 管理员 / 金融机构桌面 | -1px | 40px | 44px |

### 实现
```js
// 通过 data-density 属性切换，CSS 变量响应
<body data-density="spacious">
```

CSS 变量自动切换所有高度/间距。具体实现见 `src/design/tokens.js`。

### 角色到密度映射
- 登录后根据 RBAC role 自动设置默认密度
- 用户可在设置页覆盖偏好

---

## 9. Data Visualization（农业场景）

### 关键认知：**本产品不是金融交易产品**

因此**禁止**：
- ❌ 红涨绿跌动画
- ❌ 股价 K 线式价格跳动
- ❌ 涨跌百分比滚动数字

**允许的数据可视化**：
- ✓ 冷链温度曲线（单一指标随时间）
- ✓ 果园产量柱状图（年度对比）
- ✓ 溯源地图轨迹（GPS 路径）
- ✓ 供应链节点图（状态流转）
- ✓ 管理端仪表盘（KPI 卡片 + 多序列对比）

### 色彩使用
- **冷链温度**：使用专属温度色阶（见第 2 段 IoT 色板）
- **产量对比**：使用序列调色盘 1-4
- **GPS 轨迹**：使用 `#43A047` 绿色
- **异常标记**：使用 `#F57C00` 橙（不是红——红保留给品牌）

### ECharts 主题覆盖
所有 ECharts 必须通过 `apple-chain-theme` 主题加载，禁止 inline 配色。主题定义在 `src/design/echarts-theme.js`（待生成）。

### 数字格式
- **温度**：`2.5°C`（1 位小数，带单位）
- **重量**：`1,234 kg` / `12.5 吨`（三位分隔 + 中文单位）
- **价格**：`¥12.50 /kg`（2 位小数）
- **时间**：`2026-04-11 14:30`（完整格式，农业场景需要明确日期）
- **GPS**：`34.2658°N, 108.9541°E`（4 位小数，等宽字体）
- **溯源码**：`AC-2026-0411-001`（破折号分组，等宽字体）

---

## 10. State System

### Empty（空态）
- **图标**：线性灰度 `Lucide` 图标，64px，Text Tertiary 色
- **禁止**：彩色插画 / 卡通人物 / 果农拟人化
- **文案**：专业冷静 + 引导 CTA
  - ✅ "暂无果园记录。点击下方'新建果园'开始。"
  - ❌ "哎呀~还没有数据呢！"
- **组件**：全局 `<AppEmpty icon="..." title="..." cta="..." />`

### Loading
- **骨架屏 Skeleton**（首选）：`van-skeleton`，1.2s linear pulse
- **Spinner**（按钮内）：16px 圆圈，Brand 色
- **禁止**：整屏 spinner（除首次加载 < 1s）
- **弱网处理**：3s 后如果还在 loading，自动提示"网络较慢，请耐心等待..."

### Error
- **全局错误**：顶部 banner，`Warning BG` + 左边 4px `Warning` 色条
- **字段错误**：input 下方 13px 红字 + 2px 红边框
- **网络错误**：全屏错误页，大图标 + "网络异常" + "重试"按钮
- **文案原则**：说用户能做什么
  - ✅ "网络中断，点击重试"
  - ❌ "HTTP 500: Server Internal Error"

### Success（溯源扫码 / 订单提交）
- **Toast**：屏幕中上，`Leaf Green` 勾选图标 + 短文案，2.5s 消失
- **扫码专属**：全屏 400ms 勾选放大动画 + "溯源通过" + 自动 1.5s 后跳转详情
- **禁止**：大型弹窗成功提示

### 状态切换动效
- Empty → Loading: 骨架屏 150ms fade in
- Loading → Content: 250ms fade
- Any → Error: 200ms shake + 红边框

---

## 11. Voice & Tone

### 声音三档
- **政务严肃**：合规提示 / 溯源证书 / 监管通知 / 金融条款
- **专业克制**（默认）：主流程 / 数据展示 / 操作反馈
- **温暖友好**：空态引导 / 新手教程 / 欢迎页

### 按钮文案规则
- **动词优先**："新建果园" > "添加"
- **对称**：主"确认提交" + 次"放弃修改"
- **破坏性加对象**："删除此条溯源记录" > "删除"

### 禁止词
- ❌ "亲 / 小可爱 / 宝宝 / 哎呀" (cutesy 陷阱)
- ❌ "对不起 / 抱歉" 过度道歉
- ❌ 感叹号连用（一句话最多一个）
- ❌ Emoji 出现在严肃路径（金融 / 监管 / 溯源）
- ❌ "立即 / 马上 / 赶紧" 营销话术

### 允许 Emoji 的场景
- 果农端的非核心提示（例如"今日天气 ☀️"）
- 冷链司机端的状态图标（非装饰）

### 反馈文案模板

| 场景 | 模板 | 示例 |
|------|------|------|
| 成功 | [动作]已[结果] | "果园已创建 / 订单已提交" |
| 错误 | [问题]+[动作] | "网络中断，点击重试" |
| 加载 | 正在[动作]... | "正在加载果园列表..." |
| 空态 | 暂无[对象]+引导 | "暂无溯源记录，扫码开始" |
| 确认 | 确定要[动作][对象]吗？ | "确定要删除此条记录吗？" |

---

## 12. Iconography

### 图标库
- **主库**：**@vant/icons**（现有依赖，不引入新库）
- **补充**：线性 SVG 自定义图标（果园/溯源/冷链专属）
- **禁止**：混用 Material Icons / Font Awesome / AntD Icons

### 尺寸

| Size | px | 用途 |
|------|-----|------|
| xs | 14 | 内联文字 |
| sm | 16 | 默认 |
| md | 20 | 按钮 / 导航 |
| lg | 24 | Tab Bar |
| xl | 32 | 空态 |
| 2xl | 64 | 错误页 |

### 风格
- **线条粗细**：1.5–2px（比 aiinstock 的 1.5 略粗，农业场景户外可读）
- **颜色**：`currentColor` 继承
- **圆角**：内部圆角 2px

---

## 13. Accessibility Contract

### 硬性约束（比 WCAG AA 严格）
- **颜色对比度**：正文 ≥ **7:1**（WCAG AAA，因户外强光）
- **触控目标**：最小 **48×48pt**（比 iOS HIG 的 44pt 更严）
- **字号**：正文 16px 硬下限，冷链司机场景 18px
- **焦点环**：必须可见，禁止 `outline: none`
- **屏幕阅读器**：
  - icon-only 按钮必须 `aria-label`
  - 温度/数值变化 `aria-live="polite"`
  - 扫码结果 `role="status"`
- **表单**：`<label for>` 关联必填
- **动画**：响应 `prefers-reduced-motion`

### 对比度验证
- Text Primary `#2C2E32` on Surface Base `#FAFAF8`: **14.5:1** ✓（AAA）
- Text Secondary `#5E6368` on Surface Base: **7.8:1** ✓
- Apple Red `#D32F2F` on Surface Base: **5.8:1** ✓（AA Large）

### 反模式（禁止）
- 只用颜色传达信息（色盲不可见）
- `outline: none`
- 正文 < 14px
- 禁用 pinch-to-zoom（果农需要缩放查看）

---

## 14. Do's and Don'ts

### ❌ Don'ts（硬禁区）

- **禁止霓虹色 / 高饱和紫洋红 / 荧光色**
- **禁止深色模式默认**
- **禁止彩色卡通插画 / 果农拟人化**
- **禁止红绿涨跌动画**（非金融产品语义污染）
- **禁止正文 < 16px**（中老年视力）
- **禁止自定义字体**（包体 + 弱网）
- **禁止 Light 300 字重**（显廉价）
- **禁止 `transition: all`**
- **禁止无限循环动画**（除 skeleton 和超温呼吸）
- **禁止 `outline: none`**
- **禁止 `!important`**
- **禁止硬编码任何 hex / px**（必须走 tokens）
- **禁止整屏 spinner**（除 < 1s 冷启动）
- **禁止大型 Dialog 做成功提示**
- **禁止 Material Design 大阴影卡片浮起感**
- **禁止 "亲 / 宝宝 / 立即" 等 cutesy / 营销话术**
- **禁止 Emoji 出现在金融/监管/溯源严肃路径**
- **禁止未经 `docs/contract/` 审核的 DTO 字段直接绑定到 UI**（契约铁律）

### ✅ Do's

- 用 **苹果红 `#D32F2F`** 作为唯一品牌点缀
- 用 **果叶绿 `#43A047`** 仅做语义正向
- **暖米白底 `#FAFAF8`**（非冷白）
- **正文 16px + 行高 1.6** 照顾中老年
- **数字等宽 tabular-nums**
- **圆角 8px / 12px / 16px** 三档（按钮 / 卡片 / 弹窗）
- **边框优先于阴影**做层次
- **Vant 原生组件优先**，不重复造轮子
- 新组件必须 4 交互态 + 4 系统态齐全
- **CSS 变量覆盖 Vant 主题**（见 `src/design/vant-theme.css`）
- `:focus-visible` 永远可见
- 所有 UI 改动必须跑 `bash scripts/design-lint.sh`

---

## 15. Agent Prompt Guide

### 快速色值速查

```
品牌苹果红:    #D32F2F
果叶绿:        #43A047
暖米白底:      #FAFAF8
白卡:          #FFFFFF
主文字:        #2C2E32
次文字:        #5E6368
弱文字:        #8A9099
分隔线:        rgba(44,46,50,0.06)
警告橙:        #F57C00
政务蓝:        #1976D2
冷链温度正常:  #1976D2
冷链温度超限:  #D32F2F
```

### 给 AI 的系统提示词（已写入 CLAUDE.md）

见 `CLAUDE.md` 末尾 **"UI 代码生成硬契约"** 段落。生成任何 Vue 组件前必须遵守。

### 常见页面模板

**溯源详情页（C 端扫码结果）**：
- 顶部：溯源码 + 产品名（大字号 Heading 1）
- 中部：Card 组合
  - 基础信息（产地、品种、采摘日期、批次）
  - 种植档案（果园名、种植户、种植方法、施肥记录）
  - 冷链记录（温度曲线图 + 运输节点时间轴）
  - 质量检测（检测报告照片 + 合格证）
- 底部：分享 + 举报 两个按钮

**果园管理页（果农端）**：
- 顶部：欢迎语 + 今日天气（允许 Emoji）
- 列表：果园 Cell（照片 + 名称 + 面积 + 本季产量预估）
- 底部悬浮：新建果园按钮（FAB 风格苹果红圆形 56×56）

**订单列表页（合作社端）**：
- 顶部：状态 Tab（待处理 / 进行中 / 已完成）
- 搜索栏：日期 + 状态筛选
- Cell 列表：订单号 / 采购商 / 数量 / 金额 / 状态标签
- 行动：点击进入详情

**仪表盘（管理员桌面）**：
- compact 密度
- 4 列 KPI 卡片（今日订单 / 活跃果农 / 冷链在途 / 预警数）
- 2 列 图表（30 日订单趋势 / 地区分布）
- 底部：最近操作日志表格

**冷链司机端（超 spacious）**：
- 字号全部放大到 18px
- 按钮高度 56px
- 核心信息：当前温度 Number L 24px + 目标温度 + 偏差
- 顶部大按钮：签到 / 签收 / 求助

---

## 更新日志

- **2026-04-11** 初版，基于 brand-soul.md 落地，适配 Vue 3 + Vant 4 技术栈
