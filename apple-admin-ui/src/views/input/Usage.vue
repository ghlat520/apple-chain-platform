<template>
  <div class="page-container">
    <div class="page-header">
      <h2>使用记录</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增记录</el-button>
    </div>
    <div class="filter-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索产品名称/操作人"
        clearable
        style="width:220px"
        @clear="loadData"
        @keyup.enter="loadData"
      />
      <el-select v-model="query.method" placeholder="施用方式" clearable style="width:140px" @change="loadData">
        <el-option label="撒施" value="撒施" />
        <el-option label="喷洒" value="喷洒" />
        <el-option label="滴灌" value="滴灌" />
        <el-option label="��施" value="穴施" />
      </el-select>
      <el-input v-model="query.orchardId" placeholder="果园ID" clearable style="width:150px" @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
      <el-button @click="handleExport" icon="Download">导出</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="usageDate" label="使用日期" width="130" />
      <el-table-column prop="productName" label="产品名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="orchardName" label="果园名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="batchCode" label="批次编码" width="160" />
      <el-table-column prop="quantity" label="用量" width="100" class-name="nums-tabular" />
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="method" label="施用方式" width="100" />
      <el-table-column prop="operator" label="操作人" width="120" />
      <el-table-column prop="traceCode" label="溯源码" width="160" />
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
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
    <el-empty v-if="!loading && tableData.length === 0" description="暂无使用记录" style="margin-top:40px" />
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
    <el-drawer v-model="drawerVisible" title="使用���录详情" size="520px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="产品名称">{{ currentRow.productName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="果园名称">{{ currentRow.orchardName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="批次编码">{{ currentRow.batchCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="使用日期">{{ currentRow.usageDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="用量" class-name="nums-tabular">{{ currentRow.quantity }}</el-descriptions-item>
          <el-descriptions-item label="单位">{{ currentRow.unit || '-' }}</el-descriptions-item>
          <el-descriptions-item label="施用方式">{{ currentRow.method || '-' }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ currentRow.operator || '-' }}</el-descriptions-item>
          <el-descriptions-item label="溯源码">{{ currentRow.traceCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentRow.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑记录' : '新增记录'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="产品ID" prop="productId">
          <el-input-number v-model="form.productId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="form.productName" />
        </el-form-item>
        <el-form-item label="批次ID">
          <el-input-number v-model="form.batchId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="批次编码">
          <el-input v-model="form.batchCode" />
        </el-form-item>
        <el-form-item label="果园ID">
          <el-input-number v-model="form.orchardId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="用量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="kg/L/袋/瓶" />
        </el-form-item>
        <el-form-item label="使用日期" prop="usageDate">
          <el-date-picker v-model="form.usageDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="施用方式">
          <el-select v-model="form.method" style="width:100%" clearable>
            <el-option label="撒施" value="撒施" />
            <el-option label="喷洒" value="喷洒" />
            <el-option label="滴灌" value="滴灌" />
            <el-option label="穴施" value="穴施" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="form.operator" />
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
import { usageApi } from '@/api/input.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', method: '', orchardId: '' })

const drawerVisible = ref(false)
const currentRow = ref(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  productId: null,
  productName: '',
  batchId: null,
  batchCode: '',
  orchardId: null,
  quantity: 0,
  unit: '',
  usageDate: '',
  method: '',
  operator: '',
  remark: ''
})
const formRules = {
  productId: [{ required: true, message: '请输入产品ID', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入用量', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  usageDate: [{ required: true, message: '请选择使用日期', trigger: 'change' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await usageApi.list(query)
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
  Object.assign(form, { id: null, productId: null, productName: '', batchId: null, batchCode: '', orchardId: null, quantity: 0, unit: '', usageDate: '', method: '', operator: '', remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    productId: row.productId,
    productName: row.productName || '',
    batchId: row.batchId,
    batchCode: row.batchCode || '',
    orchardId: row.orchardId,
    quantity: row.quantity || 0,
    unit: row.unit || '',
    usageDate: row.usageDate || '',
    method: row.method || '',
    operator: row.operator || '',
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
      await usageApi.update(form.id, form)
    } else {
      await usageApi.create(form)
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
  await ElMessageBox.confirm('确认删除该使用记录?', '提示', { type: 'warning' })
  try {
    await usageApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleExport() {
  try {
    const blob = await usageApi.export(query)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '使用记录.csv'
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error(e)
  }
}

onMounted(loadData)
</script>
