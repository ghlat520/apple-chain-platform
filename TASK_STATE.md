# 当前任务状态 — 2026-04-13

## 任务：苹果产业链平台 前后端字段映射全量审计

### 已完成
- [x] Agent1: 浏览器遍历所有页面截图审计
- [x] Agent2: 后端API/数据库字段全量提取
- [x] Agent3: 前端页面字段全量提取
- [x] 字段差异比对
- [x] 修复 apple-web-ui/Orchards.vue: `form.name` → `form.orchardName`
- [x] 修复 frontend/LoanList.vue: `loanNo→loanCode, rate→interestRate, startDate→applyDate, endDate→dueDate`
- [x] 移除无效 `|| item.name` fallback（改为 `|| '未命名果园'`）
- [x] 编译验证通过
- [x] code-review + security-review 通过

### 遗留项（非本次引入，pre-existing）
- [ ] owner/phone 字段后端不支持，前端保留但数据不持久（业务决策：是否加到后端）
- [ ] Controller 缺少 @Valid 校验（安全建议）
- [ ] Controller 缺少认证/授权注解（安全建议）

### 结论
**大部分模块字段映射正确**，仅果园模块（apple-web-ui）和贷款模块（frontend demo）存在字段名不匹配。已全部修复。
