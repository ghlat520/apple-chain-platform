<template>
  <div class="page-container">
    <div class="page-header">
      <h2>作业计划</h2>
      <el-button type="primary" @click="openGenerate" icon="Plus">生成计划</el-button>
    </div>
    <div class="filter-bar">
      <el-select v-model="query.orchardId" placeholder="选择果园（必选）" clearable style="width:240px" @change="loadData">
        <el-option v-for="o in orchardList" :key="o.id" :label="o.orchardName" :value="o.id" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        style="width:280px"
        @change="loadData"
      />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:130px" @change="loadData">
        <el-option label="待执行" value="PENDING" />
        <el-option label="进行中" value="IN_PROGRESS" />
        <el-option label="已完成" value="DONE" />
        <el-option label="已跳过" value="SKIPPED" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="planDate" label="计划日期" width="130" />
      <el-table-column prop="taskName" label="任务名称" min-width="160" show-overflow-tooltip />
      <el-table-column label="作业类型" width="120">
        <template #default="{ row }">{{ operationLabel(row.operationType) }}</template>
      </el-table-column>
      <el-table-column label="优先级" width="90">
        <template #default="{ row }">
          <el-tag :type="row.priority === 1 ? 'danger' : row.priority === 2 ? 'warning' : 'info'" size="small">
            P{{ row.priority || '-' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="materialSuggestion" label="物资建议" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <template v-if="row.status === 'PENDING' || row.status === 'IN_PROGRESS'">
              <el-button link type="success" size="small" @click="handleDone(row)">完成</el-button>
              <el-button link type="warning" size="small" @click="handleSkip(row)">跳过</el-button>
            </template>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- Detail Drawer -->
    <el-drawer v-model="drawerVisible" title="计划详情" size="480px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="计划日期">{{ currentRow.planDate }}</el-descriptions-item>
          <el-descriptions-item label="任务名称">{{ currentRow.taskName }}</el-descriptions-item>
          <el-descriptions-item label="作业类型">{{ operationLabel(currentRow.operationType) }}</el-descriptions-item>
          <el-descriptions-item label="优先级">P{{ currentRow.priority || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(currentRow.status)" size="small">{{ statusLabel(currentRow.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="生成方式">{{ currentRow.generatedBy === 'AUTO' ? 'AI自动' : '手动' }}</el-descriptions-item>
          <el-descriptions-item label="物资建议" :span="2">{{ currentRow.materialSuggestion || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- Generate Dialog -->
    <el-dialog v-model="generateDialogVisible" title="生成作业计划（AI）" width="480px">
      <el-form ref="genFormRef" :model="genForm" :rules="genFormRules" label-width="100px">
        <el-form-item label="选择果园" prop="orchardId">
          <el-select v-model="genForm.orchardId" placeholder="请选择果园" style="width:100%">
            <el-option v-for="o in orchardList" :key="o.id" :label="o.orchardName" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划月数" prop="months">
          <el-input-number v-model="genForm.months" :min="1" :max="6" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGenerate" :loading="submitting">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { taskPlanApi, orchardApi } from '@/api/planting.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const orchardList = ref([])
// 默认显示全年数据，避免后端只返回当月
const now = new Date()
const yearStart = `${now.getFullYear()}-01-01`
const yearEnd = `${now.getFullYear()}-12-31`
const dateRange = ref([yearStart, yearEnd])
const query = reactive({ orchardId: null, status: '' })

const drawerVisible = ref(false)
const currentRow = ref(null)

const generateDialogVisible = ref(false)
const submitting = ref(false)
const genFormRef = ref()
const genForm = reactive({ orchardId: null, months: 1 })
const genFormRules = {
  orchardId: [{ required: true, message: '请选择果园', trigger: 'change' }],
  months: [{ required: true, message: '请输入月数', trigger: 'blur' }]
}

const OP_MAP = {
  FERTILIZE: '施肥', IRRIGATE: '灌溉', PRUNE: '修剪',
  PESTICIDE: '打药', THIN: '疏果', HARVEST: '采收'
}
const STATUS_MAP = { PENDING: '待执行', IN_PROGRESS: '进行中', DONE: '已完成', SKIPPED: '已跳过' }

function operationLabel(type) { return OP_MAP[type] || type || '-' }
function statusLabel(s) { return STATUS_MAP[s] || s || '-' }
function statusType(s) {
  return { PENDING: 'warning', IN_PROGRESS: 'primary', DONE: 'success', SKIPPED: 'info' }[s] || 'info'
}

async function loadOrchards() {
  try {
    const res = await orchardApi.list({ page: 1, size: 200 })
    orchardList.value = res?.records || res?.list || []
    // 自动选中第一个果园并加载数据
    if (orchardList.value.length > 0 && !query.orchardId) {
      query.orchardId = orchardList.value[0].id
      loadData()
    }
  } catch (e) { console.error(e) }
}

async function loadData() {
  if (!query.orchardId) {
    tableData.value = []
    return
  }
  loading.value = true
  try {
    const params = { orchardId: query.orchardId }
    if (dateRange.value) {
      params.from = dateRange.value[0]
      params.to = dateRange.value[1]
    }
    const res = await taskPlanApi.list(params)
    let list = Array.isArray(res) ? res : (res?.records || res?.list || [])
    if (query.status) {
      list = list.filter(item => item.status === query.status)
    }
    tableData.value = list
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

function openGenerate() {
  Object.assign(genForm, { orchardId: null, months: 1 })
  generateDialogVisible.value = true
}

async function handleGenerate() {
  const valid = await genFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await taskPlanApi.generate(null, { params: { orchardId: genForm.orchardId, months: genForm.months } })
    ElMessage.success('计划生成成功')
    generateDialogVisible.value = false
    query.orchardId = genForm.orchardId
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDone(row) {
  await ElMessageBox.confirm('确认完成该计划?', '提示', { type: 'warning' })
  try {
    await taskPlanApi.done(row.id)
    ElMessage.success('已标记完成')
    loadData()
  } catch (e) { /* error shown by interceptor */ }
}

async function handleSkip(row) {
  await ElMessageBox.confirm('确认跳过该计划?', '提示', { type: 'warning' })
  try {
    await taskPlanApi.skip(row.id)
    ElMessage.success('已跳过')
    loadData()
  } catch (e) { /* error shown by interceptor */ }
}

onMounted(() => { loadOrchards() })
</script>
