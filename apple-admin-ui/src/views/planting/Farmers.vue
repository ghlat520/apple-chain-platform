<template>
  <div class="page-container">
    <div class="page-header">
      <h2>农户管理</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增农户</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索编码/姓名/手机" clearable style="width:220px" @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="farmerCode" label="农户编码" width="140" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="idCard" label="身份证号" width="190" />
      <el-table-column prop="registeredArea" label="登记面积(亩)" width="130" class-name="nums-tabular" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
            {{ row.status === 'ACTIVE' ? '正常' : row.status === 'INACTIVE' ? '禁用' : (row.status?.desc || row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
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
    <el-drawer v-model="drawerVisible" title="农户详情" size="480px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="农户编码">{{ currentRow.farmerCode }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentRow.name }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentRow.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ currentRow.idCard || '-' }}</el-descriptions-item>
          <el-descriptions-item label="登记面积(亩)" class-name="nums-tabular">{{ currentRow.registeredArea ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentRow.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ currentRow.status === 'ACTIVE' ? '正常' : currentRow.status === 'INACTIVE' ? '禁用' : (currentRow.status?.desc || currentRow.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentRow.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ currentRow.address || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑农户' : '新增农户'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="农户编码" v-if="isEdit">
          <el-input v-model="form.farmerCode" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="form.idCard" />
        </el-form-item>
        <el-form-item label="登记面积(亩)">
          <el-input-number v-model="form.registeredArea" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" type="textarea" :rows="2" />
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
import { farmerApi } from '@/api/planting.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '' })

const drawerVisible = ref(false)
const currentRow = ref(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  farmerCode: '',
  name: '',
  phone: '',
  idCard: '',
  registeredArea: 0,
  address: ''
})
const formRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await farmerApi.list(query)
    const raw = res?.records || res?.list || res
    tableData.value = Array.isArray(raw) ? raw : []
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
  Object.assign(form, { id: null, farmerCode: '', name: '', phone: '', idCard: '', registeredArea: 0, address: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    farmerCode: row.farmerCode,
    name: row.name,
    phone: row.phone || '',
    idCard: row.idCard || '',
    registeredArea: row.registeredArea || 0,
    address: row.address || ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await farmerApi.update(form.id, form)
    } else {
      await farmerApi.create(form)
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
  await ElMessageBox.confirm(`确认删除农户 ${row.name}?`, '提示', { type: 'warning' })
  try {
    await farmerApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
