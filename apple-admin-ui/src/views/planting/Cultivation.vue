<template>
  <div class="page-container">
    <div class="page-header">
      <h2>种植批次</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">
        {{ activeTab === 'batches' ? '新增批次' : '新增农事操作' }}
      </el-button>
    </div>
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="种植批次" name="batches">
        <div class="filter-bar">
          <el-input v-model="batchQuery.keyword" placeholder="搜索批次号/果园" clearable style="width:220px" @clear="loadBatches" @keyup.enter="loadBatches" />
          <el-select v-model="batchQuery.status" placeholder="状态" clearable style="width:140px" @change="loadBatches">
            <el-option label="种植中" value="PLANTING" />
            <el-option label="生长中" value="GROWING" />
            <el-option label="待采收" value="READY_FOR_HARVEST" />
            <el-option label="已采收" value="HARVESTED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
          <el-button type="primary" @click="loadBatches" icon="Search">查询</el-button>
        </div>
        <el-table :data="batchData" stripe v-loading="batchLoading" border>
          <el-table-column prop="batchCode" label="批次号" width="160" />
          <el-table-column prop="orchardName" label="果园名称" width="180" />
          <el-table-column prop="appleVariety" label="苹果品种" width="120" />
          <el-table-column prop="plantYear" label="种植年份" width="110" class-name="nums-tabular" />
          <el-table-column prop="harvestDate" label="采收日期" width="130" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="batchStatusType(row.status?.code || row.status)" size="small">
                {{ batchStatusLabel(row.status?.code || row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button link type="primary" size="small" @click="viewBatchDetail(row)">详情</el-button>
                <el-button link type="primary" size="small" @click="openEditBatch(row)">编辑</el-button>
                <el-button link type="danger" size="small" @click="handleDeleteBatch(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-if="batchTotal > 0"
          style="margin-top:16px;justify-content:flex-end"
          background
          layout="total, prev, pager, next"
          :total="batchTotal"
          :page-size="batchQuery.size"
          :current-page="batchQuery.page"
          @current-change="(p) => { batchQuery.page = p; loadBatches() }"
        />
      </el-tab-pane>

      <el-tab-pane label="农事操作" name="ops">
        <div class="filter-bar">
          <el-input v-model="opQuery.batchId" placeholder="批次ID" clearable style="width:150px" @clear="loadOps" @keyup.enter="loadOps" />
          <el-select v-model="opQuery.operationType" placeholder="操作类型" clearable style="width:150px" @change="loadOps">
            <el-option label="施肥" value="FERTILIZE" />
            <el-option label="灌溉" value="IRRIGATE" />
            <el-option label="修剪" value="PRUNE" />
            <el-option label="打药" value="PESTICIDE" />
            <el-option label="采收" value="HARVEST" />
            <el-option label="其他" value="OTHER" />
          </el-select>
          <el-button type="primary" @click="loadOps" icon="Search">查询</el-button>
        </div>
        <el-table :data="opData" stripe v-loading="opLoading" border>
          <el-table-column prop="batchCode" label="批次号" width="160" />
          <el-table-column label="操作类型" width="120">
            <template #default="{ row }">{{ opTypeLabel(row.operationType?.desc || row.operationType) }}</template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" width="120" />
          <el-table-column prop="operationDate" label="操作日期" width="130" />
          <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button link type="primary" size="small" @click="openEditOp(row)">编辑</el-button>
                <el-button link type="danger" size="small" @click="handleDeleteOp(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-if="opTotal > 0"
          style="margin-top:16px;justify-content:flex-end"
          background
          layout="total, prev, pager, next"
          :total="opTotal"
          :page-size="opQuery.size"
          :current-page="opQuery.page"
          @current-change="(p) => { opQuery.page = p; loadOps() }"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- Batch Detail Drawer -->
    <el-drawer v-model="drawerVisible" title="批次详情" size="480px">
      <template v-if="currentBatch">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="批次号">{{ currentBatch.batchCode }}</el-descriptions-item>
          <el-descriptions-item label="果园名称">{{ currentBatch.orchardName }}</el-descriptions-item>
          <el-descriptions-item label="苹果品种">{{ currentBatch.appleVariety || '-' }}</el-descriptions-item>
          <el-descriptions-item label="种植年份">{{ currentBatch.plantYear || '-' }}</el-descriptions-item>
          <el-descriptions-item label="采收日期">{{ currentBatch.harvestDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="batchStatusType(currentBatch.status?.code || currentBatch.status)" size="small">
              {{ batchStatusLabel(currentBatch.status?.code || currentBatch.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentBatch.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- Batch Create/Edit Dialog -->
    <el-dialog v-model="batchDialogVisible" :title="isEdit ? '编辑批次' : '新增批次'" width="500px">
      <el-form ref="batchFormRef" :model="batchForm" :rules="batchFormRules" label-width="100px">
        <el-form-item label="批次号" v-if="isEdit">
          <el-input v-model="batchForm.batchCode" disabled />
        </el-form-item>
        <el-form-item label="果园ID" prop="orchardId">
          <el-input-number v-model="batchForm.orchardId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="苹果品种" prop="appleVariety">
          <el-input v-model="batchForm.appleVariety" placeholder="如: 红富士/嘎拉/黄元帅/秦冠" />
        </el-form-item>
        <el-form-item label="种植年份" prop="plantYear">
          <el-input-number v-model="batchForm.plantYear" :min="2000" :max="2100" :controls="false" style="width:100%" />
        </el-form-item>
        <el-form-item label="预计产量(kg)">
          <el-input-number v-model="batchForm.expectedYield" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="实际产量(kg)">
          <el-input-number v-model="batchForm.actualYield" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="采收日期">
          <el-date-picker v-model="batchForm.harvestDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="batchForm.status" style="width:100%">
            <el-option label="种植中" value="PLANTING" />
            <el-option label="生长中" value="GROWING" />
            <el-option label="待采收" value="READY_FOR_HARVEST" />
            <el-option label="已采收" value="HARVESTED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="batchForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- Op Create/Edit Dialog -->
    <el-dialog v-model="opDialogVisible" :title="isEdit ? '编辑农事操作' : '新增农事操作'" width="500px">
      <el-form ref="opFormRef" :model="opForm" :rules="opFormRules" label-width="100px">
        <el-form-item label="批次ID" prop="batchId">
          <el-input-number v-model="opForm.batchId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="批次编码">
          <el-input v-model="opForm.batchCode" />
        </el-form-item>
        <el-form-item label="操作类型" prop="operationType">
          <el-select v-model="opForm.operationType" style="width:100%">
            <el-option label="施肥" value="FERTILIZE" />
            <el-option label="灌溉" value="IRRIGATE" />
            <el-option label="修剪" value="PRUNE" />
            <el-option label="打药" value="PESTICIDE" />
            <el-option label="采收" value="HARVEST" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="opForm.operator" />
        </el-form-item>
        <el-form-item label="操作日期" prop="operationDate">
          <el-date-picker v-model="opForm.operationDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="物资投入">
          <el-input v-model="opForm.materials" type="textarea" :rows="2" placeholder="JSON格式物资信息" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="opForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="opDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleOpSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { cultivationApi } from '@/api/planting.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('batches')

// Batches
const batchLoading = ref(false)
const batchData = ref([])
const batchTotal = ref(0)
const batchQuery = reactive({ page: 1, size: 20, keyword: '', status: '' })
const drawerVisible = ref(false)
const currentBatch = ref(null)
const batchDialogVisible = ref(false)
const batchFormRef = ref()
const batchForm = reactive({ id: null, batchCode: '', orchardId: null, appleVariety: '', plantYear: new Date().getFullYear(), expectedYield: null, actualYield: null, harvestDate: '', status: 'PLANTING', remark: '' })
const batchFormRules = {
  orchardId: [{ required: true, message: '请输入果园ID', trigger: 'blur' }],
  appleVariety: [{ required: true, message: '请输入苹果品种', trigger: 'blur' }],
  plantYear: [{ required: true, message: '请输入种植年份', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// Ops
const opLoading = ref(false)
const opData = ref([])
const opTotal = ref(0)
const opQuery = reactive({ page: 1, size: 20, batchId: '', operationType: '' })
const opDialogVisible = ref(false)
const opFormRef = ref()
const opForm = reactive({ id: null, batchId: null, batchCode: '', operationType: '', operator: '', operationDate: '', materials: '', remark: '' })
const opFormRules = {
  batchId: [{ required: true, message: '请输入批次ID', trigger: 'blur' }],
  operationType: [{ required: true, message: '请选择操作类型', trigger: 'change' }],
  operationDate: [{ required: true, message: '请选择操作日期', trigger: 'change' }]
}

const OP_TYPE_MAP = { FERTILIZE: '施肥', IRRIGATE: '灌溉', PRUNE: '修剪', PESTICIDE: '打药', HARVEST: '采收', OTHER: '其他' }
function opTypeLabel(type) { return OP_TYPE_MAP[type] || type || '-' }

const isEdit = ref(false)
const submitting = ref(false)

function batchStatusType(code) {
  const map = { PLANTING: 'primary', GROWING: 'success', READY_FOR_HARVEST: 'warning', HARVESTED: 'info', CLOSED: 'info' }
  return map[code] || 'info'
}

function batchStatusLabel(code) {
  const map = { PLANTING: '种植中', GROWING: '生长中', READY_FOR_HARVEST: '待采收', HARVESTED: '已采收', CLOSED: '已关闭' }
  return map[code] || code
}

async function loadBatches() {
  batchLoading.value = true
  try {
    const res = await cultivationApi.listBatches(batchQuery)
    batchData.value = res?.records || res?.list || []
    batchTotal.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    batchLoading.value = false
  }
}

async function loadOps() {
  opLoading.value = true
  try {
    const res = await cultivationApi.listOps(opQuery)
    opData.value = res?.records || res?.list || []
    opTotal.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    opLoading.value = false
  }
}

function handleTabChange(tab) {
  if (tab === 'batches') loadBatches()
  else loadOps()
}

function viewBatchDetail(row) {
  currentBatch.value = row
  drawerVisible.value = true
}

function openCreate() {
  isEdit.value = false
  if (activeTab.value === 'batches') {
    Object.assign(batchForm, { id: null, batchCode: '', orchardId: null, appleVariety: '', plantYear: new Date().getFullYear(), expectedYield: null, actualYield: null, harvestDate: '', status: 'PLANTING', remark: '' })
    batchDialogVisible.value = true
  } else {
    Object.assign(opForm, { id: null, batchId: null, batchCode: '', operationType: '', operator: '', operationDate: '', materials: '', remark: '' })
    opDialogVisible.value = true
  }
}

function openEditBatch(row) {
  isEdit.value = true
  Object.assign(batchForm, {
    id: row.id,
    batchCode: row.batchCode,
    orchardId: row.orchardId,
    appleVariety: row.appleVariety || '',
    plantYear: row.plantYear || new Date().getFullYear(),
    expectedYield: row.expectedYield ?? null,
    actualYield: row.actualYield ?? null,
    harvestDate: row.harvestDate || '',
    status: row.status?.code || row.status,
    remark: row.remark || ''
  })
  batchDialogVisible.value = true
}

function openEditOp(row) {
  isEdit.value = true
  Object.assign(opForm, {
    id: row.id,
    batchId: row.batchId,
    batchCode: row.batchCode || '',
    operationType: row.operationType?.code || row.operationType,
    operator: row.operator || '',
    operationDate: row.operationDate || '',
    materials: row.materials || '',
    remark: row.remark || ''
  })
  opDialogVisible.value = true
}

async function handleBatchSubmit() {
  const valid = await batchFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await cultivationApi.updateBatch(batchForm.id, batchForm)
    } else {
      await cultivationApi.createBatch(batchForm)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    batchDialogVisible.value = false
    loadBatches()
  } catch (e) {
    // error shown by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleOpSubmit() {
  const valid = await opFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await cultivationApi.updateOp(opForm.id, opForm)
    } else {
      await cultivationApi.createOp(opForm)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    opDialogVisible.value = false
    loadOps()
  } catch (e) {
    // error shown by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDeleteBatch(row) {
  await ElMessageBox.confirm(`确认删除批次 ${row.batchCode}?`, '提示', { type: 'warning' })
  try {
    await cultivationApi.deleteBatch(row.id)
    ElMessage.success('删除成功')
    loadBatches()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleDeleteOp(row) {
  await ElMessageBox.confirm(`确认删除该农事操作记录?`, '提示', { type: 'warning' })
  try {
    await cultivationApi.deleteOp(row.id)
    ElMessage.success('删除成功')
    loadOps()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadBatches)
</script>
