<template>
  <div class="page-container">
    <div class="page-header">
      <h2>数据资产目录</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增资产</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索编码/名称" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.securityLevel" placeholder="安全等级" clearable style="width:150px" @change="loadData">
        <el-option label="PUBLIC" value="PUBLIC" />
        <el-option label="INTERNAL" value="INTERNAL" />
        <el-option label="SENSITIVE" value="SENSITIVE" />
        <el-option label="SECRET" value="SECRET" />
        <el-option label="CONFIDENTIAL" value="CONFIDENTIAL" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="assetCode" label="资产编码" width="160" />
      <el-table-column prop="bizName" label="业务名称" width="180" />
      <el-table-column label="数据库.表名" width="260">
        <template #default="{ row }">{{ row.databaseName }}.{{ row.tableName }}</template>
      </el-table-column>
      <el-table-column prop="owner" label="负责人" width="100" />
      <el-table-column prop="securityLevel" label="安全等级" width="120">
        <template #default="{ row }">
          <el-tag :type="levelType(row.securityLevel)" size="small">{{ row.securityLevel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="recordCount" label="记录数" width="120" class-name="nums-tabular" />
      <el-table-column prop="lastUpdateTime" label="最后更新" width="180" />
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
    <el-drawer v-model="drawerVisible" title="资产详情" size="600px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="资产编码">{{ currentRow.assetCode }}</el-descriptions-item>
          <el-descriptions-item label="业务名称">{{ currentRow.bizName }}</el-descriptions-item>
          <el-descriptions-item label="数据库.表名">{{ currentRow.databaseName }}.{{ currentRow.tableName }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ currentRow.owner || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源系统">{{ currentRow.sourceSystem || '-' }}</el-descriptions-item>
          <el-descriptions-item label="标签">{{ currentRow.tags || '-' }}</el-descriptions-item>
          <el-descriptions-item label="安全等级">
            <el-tag :type="levelType(currentRow.securityLevel)" size="small">{{ currentRow.securityLevel }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="记录数" class-name="nums-tabular">{{ currentRow.recordCount }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ currentRow.bizDescription || '-' }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin:20px 0 10px">字段列表</h4>
        <el-table :data="fields" stripe border size="small">
          <el-table-column prop="fieldName" label="字段名" width="150" />
          <el-table-column prop="fieldType" label="类型" width="120" />
          <el-table-column prop="bizMeaning" label="注释" />
          <el-table-column prop="nullable" label="可空" width="80">
            <template #default="{ row }">{{ row.nullable === 1 ? '是' : '否' }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑资产' : '新增资产'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="资产编码" prop="assetCode">
          <el-input v-model="form.assetCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="业务名称" prop="bizName">
          <el-input v-model="form.bizName" />
        </el-form-item>
        <el-form-item label="数据库名" prop="databaseName">
          <el-input v-model="form.databaseName" />
        </el-form-item>
        <el-form-item label="表名" prop="tableName">
          <el-input v-model="form.tableName" />
        </el-form-item>
        <el-form-item label="安全等级" prop="securityLevel">
          <el-select v-model="form.securityLevel" style="width:100%">
            <el-option label="PUBLIC" value="PUBLIC" />
            <el-option label="INTERNAL" value="INTERNAL" />
            <el-option label="SENSITIVE" value="SENSITIVE" />
            <el-option label="SECRET" value="SECRET" />
            <el-option label="CONFIDENTIAL" value="CONFIDENTIAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务描述">
          <el-input v-model="form.bizDescription" type="textarea" :rows="3" />
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
import { assetApi } from '@/api/bigdata.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', securityLevel: '' })

const drawerVisible = ref(false)
const currentRow = ref(null)
const fields = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  assetCode: '',
  bizName: '',
  databaseName: '',
  tableName: '',
  securityLevel: 'PUBLIC',
  bizDescription: ''
})
const formRules = {
  assetCode: [{ required: true, message: '请输入资产编码', trigger: 'blur' }],
  bizName: [{ required: true, message: '请输入业务名称', trigger: 'blur' }],
  databaseName: [{ required: true, message: '请输入数据库名', trigger: 'blur' }],
  tableName: [{ required: true, message: '请输入表名', trigger: 'blur' }],
  securityLevel: [{ required: true, message: '请选择安全等级', trigger: 'change' }]
}

function levelType(level) {
  const map = { PUBLIC: 'success', INTERNAL: 'primary', SENSITIVE: 'warning', SECRET: 'danger', CONFIDENTIAL: 'danger' }
  return map[level] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await assetApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function viewDetail(row) {
  currentRow.value = row
  drawerVisible.value = true
  try {
    const res = await assetApi.listFields(row.id)
    fields.value = res || []
  } catch (e) {
    fields.value = []
  }
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    assetCode: '',
    bizName: '',
    databaseName: '',
    tableName: '',
    securityLevel: 'PUBLIC',
    bizDescription: ''
  })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    assetCode: row.assetCode,
    bizName: row.bizName,
    databaseName: row.databaseName || '',
    tableName: row.tableName || '',
    securityLevel: row.securityLevel,
    bizDescription: row.bizDescription || ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await assetApi.update(form.id, form)
    } else {
      await assetApi.create(form)
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
  await ElMessageBox.confirm(`确认删除资产 ${row.bizName}?`, '提示', { type: 'warning' })
  try {
    await assetApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
