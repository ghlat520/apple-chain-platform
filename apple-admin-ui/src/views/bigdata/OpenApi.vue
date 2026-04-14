<template>
  <div class="page-container">
    <div class="page-header">
      <h2>开放API管理</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">注册客户端</el-button>
    </div>
    <div class="filter-bar">
      <el-button type="primary" @click="loadData" icon="Search">刷新</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="clientName" label="名称" width="180" />
      <el-table-column prop="appKey" label="AppKey" width="260">
        <template #default="{ row }">
          <span class="nums-tabular" style="font-size:12px">{{ row.appKey }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="rateLimitQps" label="QPS限制" width="100" class-name="nums-tabular" />
      <el-table-column prop="dailyQuota" label="日配额" width="100" class-name="nums-tabular" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="validUntil" label="有效期" width="120" />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="viewLogs(row)">日志</el-button>
            <el-button link type="warning" size="small" @click="rotateKey(row)">轮转</el-button>
            <el-button link :type="row.status === 'ACTIVE' ? 'danger' : 'success'" size="small" @click="toggleStatus(row)">{{ row.status === 'ACTIVE' ? '停用' : '启用' }}</el-button>
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

    <!-- Logs Drawer -->
    <el-drawer v-model="logsVisible" title="调用日志" size="650px">
      <el-table :data="logsData" stripe border size="small" v-loading="logsLoading">
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column prop="apiPath" label="API路径" width="200" />
        <el-table-column prop="method" label="方法" width="80" />
        <el-table-column prop="statusCode" label="状态码" width="80" class-name="nums-tabular" />
        <el-table-column prop="durationMs" label="耗时(ms)" width="90" class-name="nums-tabular" />
      </el-table>
    </el-drawer>

    <!-- Register Dialog -->
    <el-dialog v-model="dialogVisible" title="注册客户端" width="450px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="名称" prop="clientName">
          <el-input v-model="form.clientName" />
        </el-form-item>
        <el-form-item label="QPS限制" prop="rateLimitQps">
          <el-input-number v-model="form.rateLimitQps" :min="1" :max="10000" />
        </el-form-item>
        <el-form-item label="日配额" prop="dailyQuota">
          <el-input-number v-model="form.dailyQuota" :min="100" :max="10000000" />
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
import { openApi } from '@/api/bigdata.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20 })

const logsVisible = ref(false)
const logsLoading = ref(false)
const logsData = ref([])

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ clientName: '', rateLimitQps: 100, dailyQuota: 10000 })
const formRules = {
  clientName: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

function statusType(s) {
  return { ACTIVE: 'success', DISABLED: 'danger', REVOKED: 'info' }[s] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await openApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function viewLogs(row) {
  logsVisible.value = true
  logsLoading.value = true
  try {
    const res = await openApi.logs(row.id, { page: 1, size: 50 })
    logsData.value = res?.records || res?.list || res || []
  } catch (e) {
    logsData.value = []
  } finally {
    logsLoading.value = false
  }
}

async function rotateKey(row) {
  await ElMessageBox.confirm(`确认轮转 ${row.clientName} 的密钥? 旧密钥将立即失效。`, '提示', { type: 'warning' })
  try {
    await openApi.rotateKey(row.id)
    ElMessage.success('密钥已轮转')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function toggleStatus(row) {
  const newStatus = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  try {
    await openApi.updateStatus(row.id, newStatus)
    ElMessage.success('状态已更新')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

function openCreate() {
  Object.assign(form, { clientName: '', rateLimitQps: 100, dailyQuota: 10000 })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await openApi.register(form)
    ElMessage.success('注册成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
