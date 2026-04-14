<template>
  <div class="page-container">
    <div class="page-header">
      <h2>风控记录</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增记录</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索风险编号/主体名称" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.riskLevel" placeholder="风险等级" clearable style="width:130px" @change="loadData">
        <el-option label="高" value="HIGH" />
        <el-option label="中" value="MEDIUM" />
        <el-option label="低" value="LOW" />
      </el-select>
      <el-select v-model="query.status" placeholder="处理状态" clearable style="width:130px" @change="loadData">
        <el-option label="待处理" value="OPEN" />
        <el-option label="处理中" value="HANDLING" />
        <el-option label="已解决" value="RESOLVED" />
        <el-option label="已关闭" value="CLOSED" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="riskCode" label="风险编号" width="180" />
      <el-table-column label="风险类型" width="120">
        <template #default="{ row }">{{ riskTypeLabel(row.riskType) }}</template>
      </el-table-column>
      <el-table-column label="关联类型" width="100">
        <template #default="{ row }">{{ relatedTypeLabel(row.relatedType) }}</template>
      </el-table-column>
      <el-table-column label="风险等级" width="100">
        <template #default="{ row }">
          <el-tag :type="riskLevelType(row.riskLevel)" size="small">{{ riskLevelLabel(row.riskLevel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="风险描述" min-width="200" />
      <el-table-column prop="handler" label="处理人" width="100" />
      <el-table-column label="处理状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
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
    <el-drawer v-model="drawerVisible" title="风控记录详情" size="560px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="风险编号">{{ currentRow.riskCode }}</el-descriptions-item>
          <el-descriptions-item label="关联类型">{{ relatedTypeLabel(currentRow.relatedType) }}</el-descriptions-item>
          <el-descriptions-item label="关联ID">{{ currentRow.relatedId }}</el-descriptions-item>
          <el-descriptions-item label="风险类型">{{ riskTypeLabel(currentRow.riskType) }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">
            <el-tag :type="riskLevelType(currentRow.riskLevel)" size="small">{{ riskLevelLabel(currentRow.riskLevel) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="statusType(currentRow.status)" size="small">{{ statusLabel(currentRow.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理人">{{ currentRow.handler || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ currentRow.handleTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="风险描述" :span="2">{{ currentRow.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理措施" :span="2">{{ currentRow.measure || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentRow.updateTime || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑风控记录' : '新增风控记录'" width="520px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="关联类型">
          <el-select v-model="form.relatedType" style="width:100%">
            <el-option label="贷款" value="LOAN" />
            <el-option label="质押" value="PLEDGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联ID">
          <el-input v-model="form.relatedId" type="number" />
        </el-form-item>
        <el-form-item label="风险类型" prop="riskType">
          <el-select v-model="form.riskType" style="width:100%">
            <el-option label="逾期风险" value="OVERDUE" />
            <el-option label="价格下跌" value="PRICE_DROP" />
            <el-option label="质量问题" value="QUALITY" />
            <el-option label="欺诈风险" value="FRAUD" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级" prop="riskLevel">
          <el-select v-model="form.riskLevel" style="width:100%">
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态" prop="status">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="待处理" value="OPEN" />
            <el-option label="处理中" value="HANDLING" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="处理措施">
          <el-input v-model="form.measure" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="处理人">
          <el-input v-model="form.handler" />
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
import { riskApi } from '@/api/finance.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', riskLevel: '', status: '' })

const drawerVisible = ref(false)
const currentRow = ref(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ id: null, relatedType: 'LOAN', relatedId: null, riskType: 'OVERDUE', riskLevel: 'MEDIUM', status: 'OPEN', description: '', measure: '', handler: '' })
const formRules = {
  riskType: [{ required: true, message: '请选择风险类型', trigger: 'change' }],
  riskLevel: [{ required: true, message: '请选择风险等级', trigger: 'change' }],
  status: [{ required: true, message: '请选择处理状态', trigger: 'change' }],
  description: [{ required: true, message: '请输入风险描述', trigger: 'blur' }],
}

function riskTypeLabel(type) {
  const raw = type?.code || type
  return { OVERDUE: '逾期风险', PRICE_DROP: '价格下跌', QUALITY: '质量问题', FRAUD: '欺诈风险' }[raw] || raw
}

function relatedTypeLabel(type) {
  const raw = type?.code || type
  return { LOAN: '贷款', PLEDGE: '质押' }[raw] || raw
}

function riskLevelLabel(level) {
  const raw = level?.code || level
  return { HIGH: '高', MEDIUM: '中', LOW: '低' }[raw] || raw
}

function riskLevelType(level) {
  const raw = level?.code || level
  return { HIGH: 'danger', MEDIUM: 'warning', LOW: 'success' }[raw] || 'info'
}

function statusLabel(status) {
  const raw = status?.code || status
  return { OPEN: '待处理', HANDLING: '处理中', RESOLVED: '已解决', CLOSED: '已关闭' }[raw] || raw
}

function statusType(status) {
  const raw = status?.code || status
  return { OPEN: 'danger', HANDLING: 'warning', RESOLVED: 'success', CLOSED: 'info' }[raw] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await riskApi.list(query)
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
  Object.assign(form, { id: null, relatedType: 'LOAN', relatedId: null, riskType: 'OVERDUE', riskLevel: 'MEDIUM', status: 'OPEN', description: '', measure: '', handler: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    relatedType: row.relatedType?.code || row.relatedType || 'LOAN',
    relatedId: row.relatedId,
    riskType: row.riskType?.code || row.riskType,
    riskLevel: row.riskLevel?.code || row.riskLevel,
    status: row.status?.code || row.status,
    description: row.description || '',
    measure: row.measure || '',
    handler: row.handler || '',
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await riskApi.update(form.id, form)
    } else {
      await riskApi.create(form)
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

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除风控记录 ${row.riskCode}?`, '提示', { type: 'warning' })
  try {
    await riskApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
