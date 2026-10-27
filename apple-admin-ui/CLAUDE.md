# apple-admin-ui 前端开发规范

## 强制技能

### 生成前端页面 — 必须使用 /gen-page

**任何新增或修改 Vue 页面时，必须使用 `/gen-page` Skill。**

这个 Skill 强制执行"后端优先"流程：
1. Phase 1: 读后端 Controller → Entity → Enum → 权限码（只读）
2. 输出 Backend Context（用户确认）
3. Phase 2: 基于确认的字段名写前端代码

**禁止：** 不读后端代码直接写 Vue 页面。

使用方式：
```
/gen-page planting/orchards
/gen-page input/suppliers
/gen-page --module finance
```

详见: `.claude/skills/gen-page.md`

## 技术栈

- Vue 3 Composition API (`<script setup>`)
- Element Plus UI 框架
- ECharts 数据可视化
- Vite 构建工具
- Pinia 状态管理
- Axios HTTP 客户端

## 编码规范

1. 模板参考: `src/views/bigdata/DataAsset.vue` (CRUD) / `MetricCenter.vue` (图表)
2. API 模式: `export const xxxApi = { list, get, create, update, delete }`
3. 响应解包: `const raw = res?.records || res?.list || res; const data = Array.isArray(raw) ? raw : []`
4. 枚举显示: `row.fieldName?.desc || row.fieldName`
5. 删除确认: `ElMessageBox.confirm(...)`
6. 表单验证: `formRef.value.validate().catch(() => false)`
7. ECharts: 品牌色 `#D32F2F`，`nextTick()` 后初始化，try/catch 包裹
