<template>
  <div class="page-container">
    <div class="page-header">
      <h2>农事记录</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增记录</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.orchardId" placeholder="果园ID" clearable style="width:150px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.recordType" placeholder="操作类型" clearable style="width:150px" @change="loadData">
        <el-option label="施肥" value="FERTILIZE" />
        <el-option label="灌溉" value="IRRIGATE" />
        <el-option label="修剪" value="PRUNE" />
        <el-option label="打药" value="SPRAY" />
        <el-option label="病虫害防治" value="PEST_CONTROL" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="operateDate" label="记录日期" width="130" />
      <el-table-column label="果园名称" width="180">
        <template #default="{ row }">{{ orchardMap[row.orchardId] || row.orchardId }}</template>
      </el-table-column>
      <el-table-column label="操作类型" width="120">
        <template #default="{ row }">{{ recordTypeMap[row.recordType] || row.recordType }}</template>
      </el-table-column>
      <el-table-column prop="operator" label="操作人" width="120" />
      <el-table-column prop="notes" label="备注" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="200" fixed="right">
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
    <el-drawer v-model="drawerVisible" title="农事记录详情" size="480px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="记录日期">{{ currentRow.operateDate }}</el-descriptions-item>
          <el-descriptions-item label="果园名称">{{ orchardMap[currentRow.orchardId] || currentRow.orchardId }}</el-descriptions-item>
          <el-descriptions-item label="操作类型">{{ recordTypeMap[currentRow.recordType] || currentRow.recordType }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ currentRow.operator || '-' }}</el-descriptions-item>
          <el-descriptions-item label="天气">{{ currentRow.weather || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRow.notes || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑记录' : '新增记录'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="果园ID" prop="orchardId">
          <el-input v-model="form.orchardId" />
        </el-form-item>
        <el-form-item label="操作类型" prop="recordType">
          <el-select v-model="form.recordType" style="width:100%">
            <el-option label="施肥" value="FERTILIZE" />
            <el-option label="灌溉" value="IRRIGATE" />
            <el-option label="修剪" value="PRUNE" />
            <el-option label="打药" value="SPRAY" />
            <el-option label="病虫害防治" value="PEST_CONTROL" />
          </el-select>
        </el-form-item>
        <el-form-item label="记录日期" prop="operateDate">
          <el-date-picker v-model="form.operateDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="form.operator" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" :rows="3" />
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
import { growthRecordApi, orchardApi } from '@/api/planting.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const recordTypeMap = {
  FERTILIZE: '施肥',
  IRRIGATE: '灌溉',
  PRUNE: '修剪',
  SPRAY: '打药',
  PEST_CONTROL: '病虫害防治'
}
const orchardMap = ref({})

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, orchardId: '', recordType: '' })

const drawerVisible = ref(false)
const currentRow = ref(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  orchardId: '',
  recordType: '',
  operateDate: '',
  operator: '',
  notes: ''
})
const formRules = {
  orchardId: [{ required: true, message: '请输入果园ID', trigger: 'blur' }],
  recordType: [{ required: true, message: '请选择操作类型', trigger: 'change' }],
  operateDate: [{ required: true, message: '请选择记录日期', trigger: 'change' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await growthRecordApi.list(query)
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
  Object.assign(form, { id: null, orchardId: '', recordType: '', operateDate: '', operator: '', notes: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    orchardId: row.orchardId,
    recordType: row.recordType || '',
    operateDate: row.operateDate || '',
    operator: row.operator || '',
    notes: row.notes || ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await growthRecordApi.update(form.id, form)
    } else {
      await growthRecordApi.create(form)
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
  await ElMessageBox.confirm(`确认删除该农事记录?`, '提示', { type: 'warning' })
  try {
    await growthRecordApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function loadOrchards() {
  try {
    const res = await orchardApi.list({ page: 1, size: 1000 })
    const list = res?.records || res?.list || []
    const map = {}
    list.forEach(o => { map[o.id] = o.orchardName })
    orchardMap.value = map
  } catch (e) {
    console.error(e)
  }
}

onMounted(async () => {
  await loadOrchards()
  loadData()
})
</script>
