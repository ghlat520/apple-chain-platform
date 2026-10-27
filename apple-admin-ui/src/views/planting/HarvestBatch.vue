<template>
  <div class="page-container">
    <div class="page-header">
      <h2>采收批次</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增批次</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.orchardId" placeholder="果园ID" clearable style="width:150px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:140px" @change="loadData">
        <el-option label="待确认" value="DRAFT" />
        <el-option label="已确认" value="CONFIRMED" />
        <el-option label="已入库" value="IN_STORAGE" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="batchNo" label="批次号" width="160" />
      <el-table-column prop="orchardId" label="果园ID" width="100" />
      <el-table-column prop="harvestDate" label="采收日期" width="130" />
      <el-table-column prop="totalWeight" label="总重量(kg)" width="120" class-name="nums-tabular" />
      <el-table-column prop="gradeA" label="A级(kg)" width="110" class-name="nums-tabular" />
      <el-table-column prop="gradeB" label="B级(kg)" width="110" class-name="nums-tabular" />
      <el-table-column prop="gradeC" label="C级(kg)" width="110" class-name="nums-tabular" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status?.code || row.status)" size="small">
            {{ row.status?.desc || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="(row.status?.code || row.status) === 'DRAFT'"
              link type="success" size="small"
              @click="handleConfirm(row)"
            >确认采收</el-button>
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
    <el-drawer v-model="drawerVisible" title="采收批次详情" size="520px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="批次号">{{ currentRow.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="果园ID">{{ currentRow.orchardId }}</el-descriptions-item>
          <el-descriptions-item label="采收日期">{{ currentRow.harvestDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="总重量(kg)" class-name="nums-tabular">{{ currentRow.totalWeight || '-' }}</el-descriptions-item>
          <el-descriptions-item label="A级(kg)" class-name="nums-tabular">{{ currentRow.gradeA ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="B级(kg)" class-name="nums-tabular">{{ currentRow.gradeB ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="C级(kg)" class-name="nums-tabular">{{ currentRow.gradeC ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(currentRow.status?.code || currentRow.status)" size="small">
              {{ currentRow.status?.desc || currentRow.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑批次' : '新增批次'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item label="批次号" prop="batchNo">
          <el-input v-model="form.batchNo" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="果园ID" prop="orchardId">
          <el-input v-model="form.orchardId" placeholder="请输入果园ID" />
        </el-form-item>
        <el-form-item label="采收日期" prop="harvestDate">
          <el-date-picker v-model="form.harvestDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="总重量(kg)">
          <el-input-number v-model="form.totalWeight" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="A级(kg)">
          <el-input-number v-model="form.gradeA" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="B级(kg)">
          <el-input-number v-model="form.gradeB" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="C级(kg)">
          <el-input-number v-model="form.gradeC" :min="0" :precision="2" style="width:100%" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { harvestApi } from '@/api/planting.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, orchardId: '', status: '' })

const drawerVisible = ref(false)
const currentRow = ref(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  batchNo: '',
  orchardId: '',
  harvestDate: '',
  totalWeight: 0,
  gradeA: 0,
  gradeB: 0,
  gradeC: 0,
  remark: ''
})
const formRules = {
  batchNo: [{ required: true, message: '请输入批次号', trigger: 'blur' }],
  orchardId: [{ required: true, message: '请输入果园ID', trigger: 'blur' }],
  harvestDate: [{ required: true, message: '请选择采收日期', trigger: 'change' }]
}

function statusType(code) {
  const map = { DRAFT: 'warning', CONFIRMED: 'success', IN_STORAGE: 'primary' }
  return map[code] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await harvestApi.list(query)
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
  Object.assign(form, { id: null, batchNo: '', orchardId: '', harvestDate: '', totalWeight: 0, gradeA: 0, gradeB: 0, gradeC: 0, remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    batchNo: row.batchNo,
    orchardId: row.orchardId,
    harvestDate: row.harvestDate || '',
    totalWeight: row.totalWeight || 0,
    gradeA: row.gradeA || 0,
    gradeB: row.gradeB || 0,
    gradeC: row.gradeC || 0,
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await harvestApi.update(form.id, form)
    } else {
      await harvestApi.create(form)
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

async function handleConfirm(row) {
  await ElMessageBox.confirm(`确认采收批次 ${row.batchNo}?`, '提示', { type: 'warning' })
  try {
    await harvestApi.confirm(row.id)
    ElMessage.success('采收已确认')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除批次 ${row.batchNo}?`, '提示', { type: 'warning' })
  try {
    await harvestApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
