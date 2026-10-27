<template>
  <div class="page-container">
    <div class="page-header">
      <h2>数据源管理</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增数据源</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索编码/名称" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.category" placeholder="分类" clearable style="width:140px" @change="loadData">
        <el-option label="内部" value="INTERNAL" />
        <el-option label="外部" value="EXTERNAL" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="sourceCode" label="编码" width="160" />
      <el-table-column prop="sourceName" label="名称" width="180" />
      <el-table-column prop="sourceType" label="类型" width="130">
        <template #default="{ row }">
          <el-tag :type="sourceTypeColor(row.sourceType)" size="small">{{ row.sourceType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : row.status === 'ERROR' ? 'danger' : 'info'" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastTestTime" label="最近测试" width="180" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="success" size="small" @click="testConnection(row)" :loading="row._testing">测试</el-button>
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

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑数据源' : '新增数据源'" width="550px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="编码" prop="sourceCode">
          <el-input v-model="form.sourceCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="名称" prop="sourceName">
          <el-input v-model="form.sourceName" />
        </el-form-item>
        <el-form-item label="类型" prop="sourceType">
          <el-select v-model="form.sourceType" style="width:100%">
            <el-option label="MYSQL" value="MYSQL" />
            <el-option label="CLICKHOUSE" value="CLICKHOUSE" />
            <el-option label="HTTP_API" value="HTTP_API" />
            <el-option label="KAFKA" value="KAFKA" />
            <el-option label="RSS" value="RSS" />
            <el-option label="FILE" value="FILE" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width:100%">
            <el-option label="内部" value="INTERNAL" />
            <el-option label="外部" value="EXTERNAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="连接URL" prop="connectUrl">
          <el-input v-model="form.connectUrl" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.passwordEnc" type="password" show-password />
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
import { sourceApi } from '@/api/bigdata.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', category: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  sourceCode: '',
  sourceName: '',
  sourceType: 'MYSQL',
  category: 'INTERNAL',
  connectUrl: '',
  username: '',
  passwordEnc: '',
  remark: ''
})
const formRules = {
  sourceCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  sourceName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  sourceType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  connectUrl: [{ required: true, message: '请输入连接URL', trigger: 'blur' }]
}

function sourceTypeColor(type) {
  const map = { MYSQL: 'primary', CLICKHOUSE: 'warning', HTTP_API: 'success', KAFKA: 'danger', RSS: 'info', FILE: '' }
  return map[type] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await sourceApi.list(query)
    tableData.value = (res?.records || res?.list || []).map(r => ({ ...r, _testing: false }))
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function testConnection(row) {
  row._testing = true
  try {
    await sourceApi.test(row.id)
    ElMessage.success('连接测试成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    row._testing = false
  }
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    sourceCode: '',
    sourceName: '',
    sourceType: 'MYSQL',
    category: 'INTERNAL',
    connectUrl: '',
    username: '',
    passwordEnc: '',
    remark: ''
  })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    sourceCode: row.sourceCode,
    sourceName: row.sourceName,
    sourceType: row.sourceType,
    category: row.category || 'INTERNAL',
    connectUrl: row.connectUrl || '',
    username: row.username || '',
    passwordEnc: '',
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
      await sourceApi.update(form.id, form)
    } else {
      await sourceApi.create(form)
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
  await ElMessageBox.confirm(`确认删除数据源 ${row.sourceName}?`, '提示', { type: 'warning' })
  try {
    await sourceApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
