<template>
  <div class="page-container">
    <div class="page-header">
      <h2>果园管理</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增果园</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索编码/名称" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:140px" @change="loadData">
        <el-option label="正常" value="NORMAL" />
        <el-option label="休眠" value="DORMANT" />
        <el-option label="已采收" value="HARVESTED" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
      <el-button @click="handleExport" icon="Download">导出</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="orchardNo" label="果园编码" width="160" />
      <el-table-column prop="orchardName" label="果园名称" width="180" />
      <el-table-column prop="farmerId" label="农户ID" width="100" />
      <el-table-column prop="area" label="面积(亩)" width="110" class-name="nums-tabular" />
      <el-table-column prop="variety" label="品种" width="120" />
      <el-table-column prop="treeAge" label="树龄(年)" width="100" />
      <el-table-column prop="location" label="位置" width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status?.code || row.status)" size="small">
            {{ row.status?.desc || row.status }}
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
    <el-drawer v-model="drawerVisible" title="果园详情" size="520px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="果园编码">{{ currentRow.orchardNo }}</el-descriptions-item>
          <el-descriptions-item label="果园名称">{{ currentRow.orchardName }}</el-descriptions-item>
          <el-descriptions-item label="农户ID">{{ currentRow.farmerId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="面积(亩)" class-name="nums-tabular">{{ currentRow.area }}</el-descriptions-item>
          <el-descriptions-item label="品种">{{ currentRow.variety || '-' }}</el-descriptions-item>
          <el-descriptions-item label="树龄(年)">{{ currentRow.treeAge || '-' }}</el-descriptions-item>
          <el-descriptions-item label="位置" :span="2">{{ currentRow.location || '-' }}</el-descriptions-item>
          <el-descriptions-item label="经度">{{ currentRow.longitude || '-' }}</el-descriptions-item>
          <el-descriptions-item label="纬度">{{ currentRow.latitude || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(currentRow.status?.code || currentRow.status)" size="small">
              {{ currentRow.status?.desc || currentRow.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentRow.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑果园' : '新增果园'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="果园编码" v-if="isEdit">
          <el-input v-model="form.orchardNo" disabled />
        </el-form-item>
        <el-form-item label="果园名称" prop="orchardName">
          <el-input v-model="form.orchardName" />
        </el-form-item>
        <el-form-item label="农户ID" prop="farmerId">
          <el-input-number v-model="form.farmerId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="面积(亩)" prop="area">
          <el-input-number v-model="form.area" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="品种" prop="variety">
          <el-input v-model="form.variety" />
        </el-form-item>
        <el-form-item label="树龄(年)">
          <el-input-number v-model="form.treeAge" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input-number v-model="form.longitude" :precision="6" :min="-180" :max="180" style="width:100%" />
        </el-form-item>
        <el-form-item label="纬度">
          <el-input-number v-model="form.latitude" :precision="6" :min="-90" :max="90" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="正常" value="NORMAL" />
            <el-option label="休眠" value="DORMANT" />
            <el-option label="已采收" value="HARVESTED" />
          </el-select>
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
import { orchardApi } from '@/api/planting.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', status: '' })

const drawerVisible = ref(false)
const currentRow = ref(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  orchardNo: '',
  orchardName: '',
  farmerId: null,
  area: 0,
  variety: '',
  treeAge: null,
  location: '',
  longitude: null,
  latitude: null,
  status: 'NORMAL',
  remark: ''
})
const formRules = {
  orchardName: [{ required: true, message: '请输入果园名称', trigger: 'blur' }],
  farmerId: [{ required: true, message: '请输入农户ID', trigger: 'blur' }],
  area: [{ required: true, message: '请输入面积', trigger: 'blur' }],
  variety: [{ required: true, message: '请输入品种', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function statusType(code) {
  const map = { NORMAL: 'success', DORMANT: 'info', HARVESTED: 'warning' }
  return map[code] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await orchardApi.list(query)
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
  Object.assign(form, { id: null, orchardNo: '', orchardName: '', farmerId: null, area: 0, variety: '', treeAge: null, location: '', longitude: null, latitude: null, status: 'NORMAL', remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    orchardNo: row.orchardNo,
    orchardName: row.orchardName,
    farmerId: row.farmerId,
    area: row.area,
    variety: row.variety || '',
    treeAge: row.treeAge || null,
    location: row.location || '',
    longitude: row.longitude || null,
    latitude: row.latitude || null,
    status: row.status?.code || row.status,
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
      await orchardApi.update(form.id, form)
    } else {
      await orchardApi.create(form)
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
  await ElMessageBox.confirm(`确认删除果园 ${row.orchardName}?`, '提示', { type: 'warning' })
  try {
    await orchardApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleExport() {
  try {
    const blob = await orchardApi.export(query)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '果园列表.xlsx'
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error(e)
  }
}

onMounted(loadData)
</script>
