<template>
  <div class="page-container">
    <div class="page-header">
      <h2>信用评级</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增评级</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索主体名称" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.entityType" placeholder="主体类型" clearable style="width:150px" @change="loadData">
        <el-option label="农户" value="FARMER" />
        <el-option label="企业" value="ENTERPRISE" />
      </el-select>
      <el-select v-model="query.creditLevel" placeholder="信用等级" clearable style="width:120px" @change="loadData">
        <el-option label="AAA" value="AAA" />
        <el-option label="AA" value="AA" />
        <el-option label="A" value="A" />
        <el-option label="BBB" value="BBB" />
        <el-option label="BB" value="BB" />
        <el-option label="B" value="B" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="entityName" label="主体名称" width="180" />
      <el-table-column label="主体类型" width="120">
        <template #default="{ row }">{{ entityTypeLabel(row.entityType) }}</template>
      </el-table-column>
      <el-table-column prop="creditScore" label="信用评分" width="110" class-name="nums-tabular" />
      <el-table-column label="信用等级" width="100">
        <template #default="{ row }">
          <el-tag :type="creditLevelType(row.creditLevel)" size="small">{{ row.creditLevel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="tradeScore" label="交易评分" width="110" class-name="nums-tabular" />
      <el-table-column prop="productionScore" label="生产评分" width="110" class-name="nums-tabular" />
      <el-table-column prop="financialScore" label="财务评分" width="110" class-name="nums-tabular" />
      <el-table-column prop="assessmentDate" label="评估日期" width="120" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="creditStatusType(row.status)" size="small">{{ creditStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="success" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handleCalculate(row)">计算评分</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-if="total > 0"
      style="margin-top:16px;justify-content:flex-end"
      background
      layout="total, prev, pager, next"
      :total="total"
      :page-size="query.size"
      :current-page="query.page"
      @current-change="(p) => { query.page = p; loadData() }"
    />

    <!-- Detail Drawer -->
    <el-drawer v-model="drawerVisible" title="信用评级详情" size="560px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="主体名称">{{ currentRow.entityName }}</el-descriptions-item>
          <el-descriptions-item label="主体类型">{{ entityTypeLabel(currentRow.entityType) }}</el-descriptions-item>
          <el-descriptions-item label="信用评分" class-name="nums-tabular">{{ currentRow.creditScore }}</el-descriptions-item>
          <el-descriptions-item label="信用等级">
            <el-tag :type="creditLevelType(currentRow.creditLevel)" size="small">{{ currentRow.creditLevel }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="评估日期">{{ currentRow.assessmentDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="有效期至">{{ currentRow.validUntil || '-' }}</el-descriptions-item>
          <el-descriptions-item label="评估机构">{{ currentRow.assessor || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="creditStatusType(currentRow.status)" size="small">{{ creditStatusLabel(currentRow.status) }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <h4 style="margin:20px 0 10px">评分明细</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="交易评分" class-name="nums-tabular">{{ currentRow.tradeScore ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="生产评分" class-name="nums-tabular">{{ currentRow.productionScore ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="财务评分" class-name="nums-tabular">{{ currentRow.financialScore ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ currentRow.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑评级' : '新增评级'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="主体名称" prop="entityName">
          <el-input v-model="form.entityName" />
        </el-form-item>
        <el-form-item label="主体类型" prop="entityType">
          <el-select v-model="form.entityType" style="width:100%">
            <el-option label="农户" value="FARMER" />
            <el-option label="企业" value="ENTERPRISE" />
          </el-select>
        </el-form-item>
        <el-form-item label="信用评分" prop="creditScore">
          <el-input-number v-model="form.creditScore" :min="0" :max="1000" :precision="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="信用等级" prop="creditLevel">
          <el-select v-model="form.creditLevel" style="width:100%">
            <el-option label="AAA" value="AAA" />
            <el-option label="AA" value="AA" />
            <el-option label="A" value="A" />
            <el-option label="BBB" value="BBB" />
            <el-option label="BB" value="BB" />
            <el-option label="B" value="B" />
          </el-select>
        </el-form-item>
        <el-form-item label="交易评分">
          <el-input-number v-model="form.tradeScore" :min="0" :max="1000" :precision="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="生产评分">
          <el-input-number v-model="form.productionScore" :min="0" :max="1000" :precision="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="财务评分">
          <el-input-number v-model="form.financialScore" :min="0" :max="1000" :precision="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { creditApi } from '@/api/finance.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', entityType: '', creditLevel: '' })

const drawerVisible = ref(false)
const currentRow = ref(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ id: null, entityName: '', entityType: 'FARMER', creditScore: null, creditLevel: 'B', tradeScore: null, productionScore: null, financialScore: null, remark: '' })
const formRules = {
  entityName: [{ required: true, message: '请输入主体名称', trigger: 'blur' }],
  entityType: [{ required: true, message: '请选择主体类型', trigger: 'change' }],
  creditScore: [{ required: true, message: '请输入信用评分', trigger: 'blur' }],
  creditLevel: [{ required: true, message: '请选择信用等级', trigger: 'change' }],
}

function entityTypeLabel(type) {
  const raw = type?.code || type
  return { FARMER: '农户', ENTERPRISE: '企业', '农户': '农户' }[raw] || raw
}

function creditStatusLabel(status) {
  const raw = status?.code || status
  return { ACTIVE: '有效', EXPIRED: '已过期', REVOKED: '已撤销' }[raw] || raw
}

function creditStatusType(status) {
  const raw = status?.code || status
  return { ACTIVE: 'success', EXPIRED: 'warning', REVOKED: 'danger' }[raw] || 'info'
}

function creditLevelType(level) {
  const raw = level?.code || level
  return { AAA: 'success', AA: 'primary', A: '', BBB: 'warning', BB: 'danger', B: 'danger' }[raw] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await creditApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function viewDetail(row) {
  currentRow.value = row
  drawerVisible.value = true
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { id: null, entityName: '', entityType: 'FARMER', creditScore: null, creditLevel: 'B', tradeScore: null, productionScore: null, financialScore: null, remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    entityName: row.entityName,
    entityType: row.entityType?.code || row.entityType,
    creditScore: row.creditScore,
    creditLevel: row.creditLevel?.code || row.creditLevel,
    tradeScore: row.tradeScore,
    productionScore: row.productionScore,
    financialScore: row.financialScore,
    remark: row.remark || '',
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await creditApi.update(form.id, form)
    } else {
      await creditApi.create(form)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleCalculate(row) {
  const entityType = row.entityType?.code || row.entityType
  if (!entityType) {
    ElMessage.warning('缺少主体类型，无法计算评分')
    return
  }
  try {
    await creditApi.calculate(row.entityId || row.id, entityType)
    ElMessage.success('评分计算已触发，请稍后刷新查看结果')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除 ${row.entityName} 的信用评级?`, '提示', { type: 'warning' })
  try {
    await creditApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
