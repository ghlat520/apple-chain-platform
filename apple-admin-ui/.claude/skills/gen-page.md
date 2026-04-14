# /gen-page — 前端页面生成 Skill（后端优先）

> 强制流程：读后端代码 → 输出上下文 → 用户确认 → 写前端代码
> 不可跳过任何步骤。违反即终止。

## 适用场景

- 新增前端 CRUD 页面
- 新增前端统计/图表页面
- 批量生成模块页面
- 修复字段映射问题

## 使用方式

```
/gen-page <模块名>/<页面名>
/gen-page planting/orchards
/gen-page input/suppliers
/gen-page finance/loans
/gen-page --module planting   # 批量生成整个模块
```

---

## Phase 1: 收集后端上下文（只读，禁止写任何文件）

### Step 1.1: 定位后端模块

根据模块名映射到后端 Maven 模块路径：

| 前端模块 | 后端模块路径 | API 前缀 |
|---------|-------------|---------|
| planting | `apple-module-planting` | `/api/planting/*`, `/api/farm/*`, `/api/cultivation/*` |
| input | `apple-module-input` | `/api/input/*` |
| trace | `apple-module-trace` | `/api/trace/*` |
| trade | `apple-module-trade` | `/api/trade/*` |
| warehouse | `apple-module-warehouse` | `/api/warehouse/*` |
| coldchain | `apple-module-coldchain` | `/api/coldchain/*`, `/api/logistics/*` |
| finance | `apple-module-finance` | `/api/finance/*` |
| bigdata | `apple-module-bigdata` | `/api/bigdata/*` |

后端基路径: `/Applications/soft/CodeSpace/apple-chain-platform/`

### Step 1.2: 读 Controller（提取 API 端点）

```
Glob: {backend-module}/src/main/java/**/controller/*Controller.java
Read: 每个相关的 Controller 文件
```

**必须提取并记录：**
- 类上的 `@RequestMapping` 值（API 基路径）
- 每个方法的 HTTP 方法 + 路径
- 每个方法的参数名和类型（`@RequestParam`, `@PathVariable`, `@RequestBody`）
- 返回类型（是 `R<PageResult<Xxx>>` 还是 `R<List<Xxx>>` 还是 `R<Xxx>`）
- `@Tag(name=...)` 或注释中的中文功能名

### Step 1.3: 读 Entity/DO（提取字段名，这是最关键的一步）

```
Glob: {backend-module}/src/main/java/**/entity/*.java
Glob: {backend-module}/src/main/java/**/domain/*.java
Read: 每个相关 Entity 文件
```

**必须逐字段提取并记录：**
- 字段名（Java camelCase，这就是 JSON 返回的 key）
- 字段类型（String/Long/BigDecimal/Integer/LocalDateTime 等）
- 字段注释（中文说明）
- 继承自 `BaseEntity` 的通用字段（id/createdAt/updatedAt/deleted）

**常见陷阱（必须避免）：**
| 你可能猜的 | 后端实际字段名 | 正确做法 |
|-----------|-------------|---------|
| `name` | `orchardName` | 读 Entity |
| `productName` | `name` | 读 Entity |
| `plateNumber` | `plateNo` | 读 Entity |
| `createTime` | `createdAt` | 读 BaseEntity |
| `code` | `orchardNo` | 读 Entity |
| `type` | `planType` | 读 Entity |

### Step 1.4: 读枚举（提取状态映射）

```
Grep: 在 {backend-module} 中搜索 implements BaseEnum
Grep: 或搜索 enum 关键字找到枚举类
Read: 每个枚举类
```

**后端枚举序列化格式（已确认）：** `{code: int, desc: "中文描述"}`

前端显示方式：
```vue
<!-- 枚举字段显示 -->
{{ row.status?.desc || row.status }}

<!-- 枚举字段筛选 -->
<el-option label="新建" :value="1" />  <!-- 用 code 值，不是字符串 -->
```

**必须记录：** 每个枚举的 code 值和中文 desc，用于前端筛选下拉。

### Step 1.5: 读权限码（提取 @RequiresPermissions）

```
Grep: 在 {backend-module} 中搜索 @RequiresPermissions
```

**必须提取并记录：** 每个权限码字符串（如 `orchard:read`, `orchard:write`）。

**注意：** 权限码可能和模块名不同！例如：
- planting 模块的权限码是 `orchard:read`（不是 `planting:read`）
- coldchain 模块的权限码是 `logistics:read`（不是 `coldchain:read`）

### Step 1.6: curl 验证（如果后端在运行）

```bash
curl -s http://localhost:8080/api/{path}/list?page=1&size=1 -H "Authorization: Bearer {token}"
```

从响应中确认：
- 数据在 `data.records` 还是 `data.list` 还是 `data`？
- 字段名是否与 Entity 一致？
- 枚举是否真的是 `{code, desc}` 对象？

---

## Phase 1 输出（必须输出，未输出则禁止进入 Phase 2）

输出以下格式的上下文文档：

```markdown
## [Backend Context: {模块名}/{页面名}]

### API Endpoints
| 方法 | 路径 | 参数 | 返回类型 |
|------|------|------|---------|
| GET | /api/planting/orchard/list | page,size,keyword,status,farmerId | R<PageResult<Orchard>> |
| GET | /api/planting/orchard/{id} | id | R<Orchard> |
| POST | /api/planting/orchard | @RequestBody Orchard | R<Orchard> |
| PUT | /api/planting/orchard/{id} | id, @RequestBody Orchard | R<Orchard> |
| DELETE | /api/planting/orchard/{id} | id | R<Void> |

### Entity Fields (Orchard extends BaseEntity)
| 字段名 | 类型 | 中文说明 | 用于 |
|--------|------|---------|------|
| orchardNo | String | 果园编号 | 表格列 |
| orchardName | String | 果园名称 | 表格列+搜索 |
| farmerId | Long | 农户ID | 关联查询 |
| area | BigDecimal | 面积(亩) | 表格列 |
| variety | String | 品种 | 表格列 |
| location | String | 位置 | 表格列 |
| status | String | 状态 | 表格列+筛选 |
| remark | String | 备注 | 表格列 |
| id | Long | ID | 操作列(隐藏) |
| createdAt | LocalDateTime | 创建时间 | 表格列 |

### Enum Values
无独立枚举类，status 为 String 字段，值: NORMAL/DORMANT/HARVESTED

### Permission Codes
- orchard:read
- orchard:write

### Response Structure
分页: { code: 0, data: { records: [...], total: N } }
详情: { code: 0, data: { ... } }

---
[Backend Context Verified] ✅
```

**等待用户确认后才能进入 Phase 2。**

---

## Phase 2: 生成前端代码

### Step 2.1: 生成 API 文件

创建 `src/api/{module}.js`（如果不存在），添加 API 对象：

```javascript
import request from './request.js'

export const xxxApi = {
  list: (params) => request.get('/api/{path}/list', { params }),
  get: (id) => request.get(`/api/{path}/${id}`),
  create: (data) => request.post('/api/{path}', data),
  update: (id, data) => request.put(`/api/{path}/${id}`, data),
  delete: (id) => request.delete(`/api/{path}/${id}`)
}
```

**API 路径必须 100% 来自 Phase 1 的 Controller @RequestMapping。**

### Step 2.2: 生成 Vue 页面

参考模板文件（按优先级读取）：
1. `src/views/bigdata/DataAsset.vue` — CRUD 页面模板
2. `src/views/bigdata/MetricCenter.vue` — 图表统计页模板

**字段映射铁律：**
- 表格 `prop` 必须与 Phase 1 Entity 字段名 **完全一致**
- 不允许猜测字段名
- 不允许用驼峰以外的命名方式

**枚举处理铁律：**
- 使用 `row.fieldName?.desc || row.fieldName` 显示
- 筛选下拉用枚举的 code 值（`:value="CODE"` 不是字符串）
- 状态标签用 el-tag + type 映射

**响应解包铁律：**
```javascript
// 必须防御性处理
const raw = res?.records || res?.list || res
const data = Array.isArray(raw) ? raw : []
const total = res?.total || 0
```

**删除确认：**
```javascript
ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
```

**表单验证：**
```javascript
await formRef.value.validate().catch(() => false)
```

### Step 2.3: 注册路由

在 `src/router/index.js` 的 children 数组中添加路由（如果不存在）：

```javascript
{
  path: '{module}/{page}',
  name: '{PageName}',
  component: () => import('@/views/{module}/{PageName}.vue'),
  meta: { perm: '{Phase1提取的权限码}' }
}
```

### Step 2.4: 更新侧栏

在 `src/layout/Sidebar.vue` 对应的 `el-sub-menu` 中添加菜单项（如果不存在）。

### Step 2.5: 更新面包屑

在 `src/layout/Navbar.vue` 的 `titleMap` 中添加映射（如果不存在）。

---

## 质量检查（生成后必须输出）

```
## 自检清单
- [ ] 所有表格 prop 来自 Phase 1 Entity 字段（非猜测）
- [ ] 所有 API 路径来自 Phase 1 Controller（非猜测）
- [ ] 权限码来自 Phase 1 @RequiresPermissions（非猜测）
- [ ] 枚举显示用 row.field?.desc || row.field
- [ ] 响应解包有 Array.isArray 防御
- [ ] 删除有 ElMessageBox.confirm
- [ ] 表单有 validate
- [ ] 时间字段有格式化（非 ISO 带 T）
- [ ] 统计页 ECharts 有 try/catch 包裹
- [ ] 页面 4 系统态（empty/loading/error/success）
```

---

## 批量模式 (--module)

当使用 `--module` 参数时，对模块内所有 Controller 重复执行 Phase 1 → Phase 2：

1. 先收集整个模块的所有 Controller/Entity/Enum
2. 输出完整的 Backend Context（所有页面）
3. 用户一次性确认
4. 并行生成所有页面（每个页面一个独立上下文）

---

## 路径速查

```
后端模块: /Applications/soft/CodeSpace/apple-chain-platform/apple-module-{name}/
Controller: apple-module-{name}/src/main/java/**/controller/*Controller.java
Entity:     apple-module-{name}/src/main/java/**/entity/*.java
Enum:       apple-module-{name}/src/main/java/**/enums/*.java
BaseEnum:   apple-common/src/main/java/**/enums/BaseEnum.java
前端页面:   apple-admin-ui/src/views/{module}/{Page}.vue
前端API:    apple-admin-ui/src/api/{module}.js
路由:       apple-admin-ui/src/router/index.js
侧栏:       apple-admin-ui/src/layout/Sidebar.vue
面包屑:     apple-admin-ui/src/layout/Navbar.vue
```
