<template>
  <div class="page-container">
    <div class="page-header">
      <h2>库存管理</h2>
      <el-button type="primary" @click="openCreate" icon="Plus" v-if="activeTab === 'list'">新增库存</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="库存列表" name="list">
        <div class="filter-bar">
          <el-input
            v-model="query.keyword"
            placeholder="搜索产品名称/仓库"
            clearable
            style="width:220px"
            @clear="loadData"
            @keyup.enter="loadData"
          />
          <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
        </div>
        <el-table :data="tableData" stripe v-loading="loading" border>
          <el-table-column prop="productName" label="产品名称" min-width="160" />
          <el-table-column prop="warehouse" label="仓库名称" min-width="140" />
          <el-table-column prop="stockQuantity" label="当前库存" width="120" class-name="nums-tabular" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="warningLevel" label="预警阈值" width="120" class-name="nums-tabular" />
          <el-table-column prop="updateTime" label="最后更新" width="180" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'NORMAL'" type="success" size="small">正常</el-tag>
              <el-tag v-else-if="row.status === 'LOW'" type="warning" size="small">偏低</el-tag>
              <el-tag v-else-if="row.status === 'EMPTY'" type="danger" size="small">空仓</el-tag>
              <el-tag v-else-if="row.status === 'OVERSTOCKED'" type="info" size="small">超储</el-tag>
              <span v-else>{{ row.status || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
                <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
                <el-button link type="warning" size="small" @click="openAdjust(row)">调整库存</el-button>
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
      </el-tab-pane>

      <el-tab-pane label="预警列表" name="alerts">
        <div class="filter-bar">
          <el-input
            v-model="alertQuery.keyword"
            placeholder="搜索产品名称"
            clearable
            style="width:220px"
            @clear="loadAlerts"
            @keyup.enter="loadAlerts"
          />
          <el-button type="primary" @click="loadAlerts" icon="Search">查询</el-button>
        </div>
        <el-table :data="alertData" stripe v-loading="alertLoading" border>
          <el-table-column prop="productName" label="产品名称" min-width="160" />
          <el-table-column prop="warehouse" label="仓库名称" min-width="140" />
          <el-table-column prop="stockQuantity" label="当前库存" width="120" class-name="nums-tabular" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="warningLevel" label="预警阈值" width="120" class-name="nums-tabular" />
          <el-table-column label="预警状态" width="110">
            <template #default="{ row }">
              <el-tag type="danger" size="small">库存不足</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" label="最后更新" width="180" />
        </el-table>
        <el-pagination
          v-if="alertTotal > 0"
          style="margin-top:16px;justify-content:flex-end"
          background
          layout="total, prev, pager, next"
          :total="alertTotal"
          :page-size="alertQuery.size"
          :current-page="alertQuery.page"
          @current-change="(p) => { alertQuery.page = p; loadAlerts() }"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- Detail Drawer -->
    <el-drawer v-model="drawerVisible" title="库存详情" size="500px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="产品名称" :span="2">{{ currentRow.productName }}</el-descriptions-item>
          <el-descriptions-item label="仓库名称">{{ currentRow.warehouse || '-' }}</el-descriptions-item>
          <el-descriptions-item label="单位">{{ currentRow.unit || '-' }}</el-descriptions-item>
          <el-descriptions-item label="当前库存" class-name="nums-tabular">{{ currentRow.stockQuantity }}</el-descriptions-item>
          <el-descriptions-item label="预警阈值" class-name="nums-tabular">{{ currentRow.warningLevel }}</el-descriptions-item>
          <el-descriptions-item label="最后更新">{{ currentRow.updateTime || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑库存' : '新增库存'" width="480px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" />
        </el-form-item>
        <el-form-item label="仓库名称" prop="warehouse">
          <el-input v-model="form.warehouse" />
        </el-form-item>
        <el-form-item label="当前库存" prop="stockQuantity">
          <el-input-number v-model="form.stockQuantity" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" />
        </el-form-item>
        <el-form-item label="预警阈值">
          <el-input-number v-model="form.warningLevel" :min="0" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- Adjust Dialog -->
    <el-dialog v-model="adjustVisible" title="调整库存" width="420px">
      <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="100px">
        <el-form-item label="调整数量" prop="delta">
          <el-input-number v-model="adjustForm.delta" style="width:100%" placeholder="正数增加，负数减少" />
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input v-model="adjustForm.reason" type="textarea" :rows="2" placeholder="请输入调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdjust" :loading="adjustSubmitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { inventoryApi } from '@/api/input.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('list')

// -- list tab --
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '' })

// -- alerts tab --
const alertLoading = ref(false)
const alertData = ref([])
const alertTotal = ref(0)
const alertQuery = reactive({ page: 1, size: 20, keyword: '' })

const drawerVisible = ref(false)
const currentRow = ref(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  productName: '',
  warehouse: '',
  stockQuantity: 0,
  unit: '',
  warningLevel: 0
})
const formRules = {
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  warehouse: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }],
  stockQuantity: [{ required: true, message: '请输入当前库存', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }]
}

const adjustVisible = ref(false)
const adjustSubmitting = ref(false)
const adjustFormRef = ref()
const adjustTargetId = ref(null)
const adjustForm = reactive({ delta: 0, reason: '' })
const adjustRules = {
  delta: [{ required: true, message: '请输入调整数量', trigger: 'blur' }]
}

function onTabChange(tab) {
  if (tab === 'alerts') {
    loadAlerts()
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await inventoryApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadAlerts() {
  alertLoading.value = true
  try {
    const res = await inventoryApi.alerts()
    const list = Array.isArray(res) ? res : []
    alertData.value = list
    alertTotal.value = list.length
  } catch (e) {
    console.error(e)
  } finally {
    alertLoading.value = false
  }
}

function viewDetail(row) {
  currentRow.value = row
  drawerVisible.value = true
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { id: null, productName: '', warehouse: '', stockQuantity: 0, unit: '', warningLevel: 0 })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    productName: row.productName,
    warehouse: row.warehouse,
    stockQuantity: row.stockQuantity,
    unit: row.unit || '',
    warningLevel: row.warningLevel || 0
  })
  dialogVisible.value = true
}

function openAdjust(row) {
  adjustTargetId.value = row.id
  Object.assign(adjustForm, { delta: 0, reason: '' })
  adjustVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await inventoryApi.update(form.id, form)
    } else {
      await inventoryApi.create(form)
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

async function submitAdjust() {
  const valid = await adjustFormRef.value.validate().catch(() => false)
  if (!valid) return
  adjustSubmitting.value = true
  try {
    await inventoryApi.adjust(adjustTargetId.value, { delta: adjustForm.delta, reason: adjustForm.reason })
    ElMessage.success('库存调整成功')
    adjustVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    adjustSubmitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除库存记录 "${row.productName}"?`, '提示', { type: 'warning' })
  try {
    await inventoryApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
