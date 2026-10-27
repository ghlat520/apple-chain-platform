<template>
  <div class="page-container">
    <div class="page-header">
      <h2>出入库记录</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增记录</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索记录号/货品名" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
      <el-input-number v-model="query.warehouseId" placeholder="仓库ID" clearable style="width:140px" :min="1" :controls="false" @change="loadData" />
      <el-select v-model="query.recordType" placeholder="方向" clearable style="width:120px" @change="loadData">
        <el-option label="入库" value="INBOUND" />
        <el-option label="出库" value="OUTBOUND" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="recordNo" label="记录号" width="160" />
      <el-table-column prop="warehouseName" label="仓库名称" width="160" />
      <el-table-column prop="recordType" label="方向" width="90">
        <template #default="{ row }">
          <el-tag :type="recordTypeTag(row.recordType)" size="small">{{ recordTypeLabel(row.recordType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="variety" label="品种" width="100" />
      <el-table-column prop="grade" label="等级" width="80" />
      <el-table-column prop="quantity" label="数量(kg)" width="110" class-name="nums-tabular" />
      <el-table-column prop="temperature" label="温度(°C)" width="100" class-name="nums-tabular" />
      <el-table-column prop="humidity" label="湿度(%)" width="100" class-name="nums-tabular" />
      <el-table-column prop="operator" label="操作人" width="100" />
      <el-table-column prop="traceCode" label="溯源码" width="140" />
      <el-table-column prop="recordDate" label="记录日期" width="120" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
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

    <!-- Create Dialog -->
    <el-dialog v-model="dialogVisible" title="新增出入库记录" width="520px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="记录号" prop="recordNo">
          <el-input v-model="form.recordNo" />
        </el-form-item>
        <el-form-item label="仓库ID" prop="warehouseId">
          <el-input-number v-model="form.warehouseId" :min="1" style="width:100%" placeholder="关联仓库ID" />
        </el-form-item>
        <el-form-item label="仓库名称">
          <el-input v-model="form.warehouseName" />
        </el-form-item>
        <el-form-item label="方向" prop="recordType">
          <el-select v-model="form.recordType" style="width:100%">
            <el-option label="入库" value="INBOUND" />
            <el-option label="出库" value="OUTBOUND" />
          </el-select>
        </el-form-item>
        <el-form-item label="批次编码">
          <el-input v-model="form.batchCode" placeholder="种植批次编码" />
        </el-form-item>
        <el-form-item label="品种" prop="variety">
          <el-input v-model="form.variety" />
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="form.grade" style="width:100%">
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量(kg)" prop="quantity">
          <el-input-number v-model="form.quantity" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="form.operator" />
        </el-form-item>
        <el-form-item label="温度(°C)">
          <el-input-number v-model="form.temperature" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="湿度(%)">
          <el-input-number v-model="form.humidity" :min="0" :max="100" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="记录日期">
          <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="溯源码">
          <el-input v-model="form.traceCode" />
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
import { recordApi } from '@/api/warehouse.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', recordType: '', warehouseId: null })

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  recordNo: '',
  warehouseId: null,
  warehouseName: '',
  recordType: 'INBOUND',
  batchCode: '',
  variety: '',
  grade: 'A',
  quantity: 0,
  temperature: null,
  humidity: null,
  operator: '',
  recordDate: '',
  traceCode: '',
  remark: ''
})
const formRules = {
  recordNo: [{ required: true, message: '请输入记录号', trigger: 'blur' }],
  warehouseId: [{ required: true, message: '请输入仓库ID', trigger: 'blur' }],
  recordType: [{ required: true, message: '请选择方向', trigger: 'change' }],
  variety: [{ required: true, message: '请输入品种', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

function recordTypeTag(type) {
  return type === 'INBOUND' ? 'success' : 'warning'
}

function recordTypeLabel(type) {
  return type === 'INBOUND' ? '入库' : type === 'OUTBOUND' ? '出库' : type
}

async function loadData() {
  loading.value = true
  try {
    const res = await recordApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, {
    recordNo: '',
    warehouseId: null,
    warehouseName: '',
    recordType: 'INBOUND',
    batchCode: '',
    variety: '',
    grade: 'A',
    quantity: 0,
    temperature: null,
    humidity: null,
    operator: '',
    recordDate: '',
    traceCode: '',
    remark: ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await recordApi.create(form)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除记录 ${row.recordNo}?`, '提示', { type: 'warning' })
  try {
    await recordApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
