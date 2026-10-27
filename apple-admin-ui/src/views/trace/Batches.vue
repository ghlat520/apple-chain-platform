<template>
  <div class="page-container">
    <div class="page-header">
      <h2>溯源批次</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新建批次</el-button>
    </div>
    <div class="filter-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索批次编码/产品名称"
        clearable
        style="width:220px"
        @clear="loadData"
        @keyup.enter="loadData"
      />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:140px" @change="loadData">
        <el-option label="已创建" value="CREATED" />
        <el-option label="处理中" value="PROCESSING" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已发货" value="SHIPPED" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="batchCode" label="批次编码" width="180" />
      <el-table-column prop="variety" label="品种" width="120" />
      <el-table-column prop="orchardName" label="果园名称" width="160" />
      <el-table-column prop="weight" label="重量(kg)" width="100" class-name="nums-tabular" />
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="viewDetail(row)">记录</el-button>
            <el-button link type="primary" size="small" @click="openUpdateStatus(row)">更新状态</el-button>
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

    <!-- Records Drawer -->
    <el-drawer v-model="drawerVisible" :title="`批次记录 — ${currentRow?.batchCode || ''}`" size="700px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border size="small" style="margin-bottom:20px">
          <el-descriptions-item label="批次编码">{{ currentRow.batchCode }}</el-descriptions-item>
          <el-descriptions-item label="品种">{{ currentRow.variety }}</el-descriptions-item>
          <el-descriptions-item label="果园名称">{{ currentRow.orchardName }}</el-descriptions-item>
          <el-descriptions-item label="重量(kg)" class-name="nums-tabular">{{ currentRow.weight ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="采收日期">{{ currentRow.harvestDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="等级">{{ currentRow.grade || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(currentRow.status)" size="small">
              {{ statusLabel(currentRow.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
          <span style="font-weight:600">溯源记录列表</span>
          <el-button size="small" type="primary" @click="openAddRecord">新增记录</el-button>
        </div>
        <el-table :data="records" stripe border size="small" v-loading="recordsLoading">
          <el-table-column prop="stage" label="阶段" width="120">
            <template #default="{ row }">{{ stageLabel(row.stage) }}</template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" width="100" />
          <el-table-column prop="location" label="地点" />
          <el-table-column prop="recordTime" label="记录时间" width="180" />
        </el-table>
        <el-pagination
          v-if="recordsTotal > 0"
          style="margin-top:12px;justify-content:flex-end"
          background
          layout="total, prev, pager, next"
          :total="recordsTotal"
          :page-size="recordsQuery.size"
          :current-page="recordsQuery.page"
          @current-change="(p) => { recordsQuery.page = p; loadRecords() }"
          small
        />
      </template>
    </el-drawer>

    <!-- Create Batch Dialog -->
    <el-dialog v-model="dialogVisible" title="新建批次" width="480px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="批次编码" prop="batchCode">
          <el-input v-model="form.batchCode" placeholder="请输入批次编码" />
        </el-form-item>
        <el-form-item label="品种" prop="variety">
          <el-input v-model="form.variety" />
        </el-form-item>
        <el-form-item label="果园ID" prop="orchardId">
          <el-input-number v-model="form.orchardId" :min="1" class="nums-tabular" style="width:100%" placeholder="请输入果园ID" />
        </el-form-item>
        <el-form-item label="果园名称" prop="orchardName">
          <el-input v-model="form.orchardName" />
        </el-form-item>
        <el-form-item label="采收日期" prop="harvestDate">
          <el-date-picker v-model="form.harvestDate" type="date" value-format="YYYY-MM-DD" placeholder="选择采收日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="form.grade" style="width:100%" clearable>
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item label="重量(kg)">
          <el-input-number v-model="form.weight" :min="0" :precision="1" class="nums-tabular" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- Update Status Dialog -->
    <el-dialog v-model="statusDialogVisible" title="更新批次状态" width="400px">
      <el-form ref="statusFormRef" :model="statusForm" :rules="statusRules" label-width="80px">
        <el-form-item label="新状态" prop="status">
          <el-select v-model="statusForm.status" style="width:100%">
            <el-option label="已创建" value="CREATED" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已发货" value="SHIPPED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="statusForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateStatus" :loading="statusSubmitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- Add Record Dialog -->
    <el-dialog v-model="recordDialogVisible" title="新增溯源记录" width="480px">
      <el-form ref="recordFormRef" :model="recordForm" :rules="recordRules" label-width="100px">
        <el-form-item label="阶段" prop="stage">
          <el-select v-model="recordForm.stage" style="width:100%">
            <el-option label="采摘" value="HARVEST" />
            <el-option label="入库" value="STORAGE" />
            <el-option label="运输" value="TRANSPORT" />
            <el-option label="销售" value="SALE" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="recordForm.operator" />
        </el-form-item>
        <el-form-item label="地点" prop="location">
          <el-input v-model="recordForm.location" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="recordForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddRecord" :loading="recordSubmitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { batchApi } from '@/api/trace.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', status: '' })

// Drawer state
const drawerVisible = ref(false)
const currentRow = ref(null)
const records = ref([])
const recordsLoading = ref(false)
const recordsTotal = ref(0)
const recordsQuery = reactive({ page: 1, size: 10 })

// Create dialog
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ batchCode: '', variety: '', orchardId: null, orchardName: '', harvestDate: '', grade: '', weight: null, remark: '' })
const formRules = {
  batchCode: [{ required: true, message: '请输入批次编码', trigger: 'blur' }],
  variety: [{ required: true, message: '请输入品种', trigger: 'blur' }],
  orchardId: [{ required: true, message: '请输入果园ID', trigger: 'blur' }],
  orchardName: [{ required: true, message: '请输入果园名称', trigger: 'blur' }],
  harvestDate: [{ required: true, message: '请选择采收日期', trigger: 'change' }],
}

// Status update dialog
const statusDialogVisible = ref(false)
const statusSubmitting = ref(false)
const statusFormRef = ref()
const statusForm = reactive({ id: null, status: '', remark: '' })
const statusRules = {
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

// Add record dialog
const recordDialogVisible = ref(false)
const recordSubmitting = ref(false)
const recordFormRef = ref()
const recordForm = reactive({ stage: '', operator: '', location: '', remark: '' })
const recordRules = {
  stage: [{ required: true, message: '请选择阶段', trigger: 'change' }],
  operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }],
}

function statusType(status) {
  const map = { CREATED: 'info', PROCESSING: 'warning', COMPLETED: 'success', SHIPPED: 'primary' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { CREATED: '已创建', PROCESSING: '处理中', COMPLETED: '已完成', SHIPPED: '已发货' }
  return map[status] || status || '-'
}

function stageLabel(stage) {
  const map = { HARVEST: '采摘', STORAGE: '入库', TRANSPORT: '运输', SALE: '销售' }
  return map[stage] || stage || '-'
}

async function loadData() {
  loading.value = true
  try {
    const res = await batchApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadRecords() {
  if (!currentRow.value) return
  recordsLoading.value = true
  try {
    const res = await batchApi.getRecords(currentRow.value.id, recordsQuery)
    // Backend returns R<List<TraceRecord>>, not PageResult
    records.value = Array.isArray(res) ? res : (res?.records || res?.list || [])
    recordsTotal.value = records.value.length
  } catch (e) {
    records.value = []
  } finally {
    recordsLoading.value = false
  }
}

function viewDetail(row) {
  currentRow.value = row
  recordsQuery.page = 1
  drawerVisible.value = true
  loadRecords()
}

function openCreate() {
  Object.assign(form, { batchCode: '', variety: '', orchardId: null, orchardName: '', harvestDate: '', grade: '', weight: null, remark: '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await batchApi.create(form)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    submitting.value = false
  }
}

function openUpdateStatus(row) {
  Object.assign(statusForm, { id: row.id, status: row.status || '', remark: '' })
  statusDialogVisible.value = true
}

async function handleUpdateStatus() {
  const valid = await statusFormRef.value.validate().catch(() => false)
  if (!valid) return
  statusSubmitting.value = true
  try {
    await batchApi.updateStatus(statusForm.id, statusForm.status)
    ElMessage.success('状态更新成功')
    statusDialogVisible.value = false
    loadData()
    if (drawerVisible.value && currentRow.value?.id === statusForm.id) {
      currentRow.value = { ...currentRow.value, status: statusForm.status }
    }
  } catch (e) {
    // error shown by interceptor
  } finally {
    statusSubmitting.value = false
  }
}

function openAddRecord() {
  Object.assign(recordForm, { stage: '', operator: '', location: '', remark: '' })
  recordDialogVisible.value = true
}

async function handleAddRecord() {
  const valid = await recordFormRef.value.validate().catch(() => false)
  if (!valid) return
  recordSubmitting.value = true
  try {
    await batchApi.addRecord(currentRow.value.id, recordForm)
    ElMessage.success('记录添加成功')
    recordDialogVisible.value = false
    loadRecords()
  } catch (e) {
    // error shown by interceptor
  } finally {
    recordSubmitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除批次 ${row.batchCode}?`, '提示', { type: 'warning' })
  try {
    await batchApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
