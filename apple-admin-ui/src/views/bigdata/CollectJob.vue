<template>
  <div class="page-container">
    <div class="page-header">
      <h2>采集任务</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增任务</el-button>
    </div>
    <div class="filter-bar">
      <el-select v-model="query.jobType" placeholder="任务类型" clearable style="width:140px" @change="loadData">
        <el-option label="CDC" value="CDC" />
        <el-option label="定时" value="SCHEDULED" />
        <el-option label="手动" value="MANUAL" />
        <el-option label="流式" value="STREAMING" />
      </el-select>
      <el-input v-model="query.sourceId" placeholder="数据源ID" clearable style="width:140px" @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="jobName" label="任务名" width="200" />
      <el-table-column prop="jobCode" label="编码" width="180" />
      <el-table-column prop="jobType" label="类型" width="110">
        <template #default="{ row }">
          <el-tag size="small">{{ jobTypeLabel(row.jobType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="cronExpr" label="Cron" width="140" />
      <el-table-column prop="targetTable" label="目标表" width="220" />
      <el-table-column label="启用" width="70">
        <template #default="{ row }">
          <el-tag :type="row.enabled === 1 ? 'success' : 'info'" size="small">{{ row.enabled === 1 ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastRunStatus" label="最近运行" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.lastRunStatus" :type="row.lastRunStatus === 'SUCCESS' ? 'success' : row.lastRunStatus === 'FAILED' ? 'danger' : 'warning'" size="small">{{ row.lastRunStatus }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="success" size="small" @click="triggerJob(row)">触发</el-button>
            <el-button link type="primary" size="small" @click="viewRuns(row)">记录</el-button>
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-if="total > 0" style="margin-top:16px;justify-content:flex-end" background layout="total, prev, pager, next" :total="total" :page-size="query.size" :current-page="query.page" @current-change="(p) => { query.page = p; loadData() }" />

    <!-- Runs Drawer -->
    <el-drawer v-model="runsVisible" title="运行记录" size="500px">
      <el-timeline v-if="runsData.length">
        <el-timeline-item v-for="item in runsData" :key="item.id" :timestamp="item.startTime" placement="top">
          <el-card shadow="never">
            <p>状态: <el-tag :type="item.runStatus === 'SUCCESS' ? 'success' : item.runStatus === 'FAILED' ? 'danger' : 'warning'" size="small">{{ item.runStatus }}</el-tag></p>
            <p v-if="item.endTime">结束: {{ item.endTime }}</p>
            <p v-if="item.errorMessage">错误: {{ item.errorMessage }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无运行记录" />
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑任务' : '新增任务'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="任务名" prop="jobName">
          <el-input v-model="form.jobName" />
        </el-form-item>
        <el-form-item label="编码" prop="jobCode">
          <el-input v-model="form.jobCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="Cron" prop="cronExpr">
          <el-input v-model="form.cronExpr" placeholder="0 0 2 * * ?" />
        </el-form-item>
        <el-form-item label="目标表" prop="targetTable">
          <el-input v-model="form.targetTable" />
        </el-form-item>
        <el-form-item label="任务类型" prop="jobType">
          <el-select v-model="form.jobType" style="width:100%">
            <el-option label="CDC" value="CDC" />
            <el-option label="定时" value="SCHEDULED" />
            <el-option label="手动" value="MANUAL" />
            <el-option label="流式" value="STREAMING" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据源ID">
          <el-input v-model="form.sourceId" type="number" />
        </el-form-item>
        <el-form-item label="脚本">
          <el-input v-model="form.script" type="textarea" :rows="3" placeholder="采集脚本/SQL" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="重试次数">
          <el-input-number v-model="form.retryTimes" :min="0" :max="10" />
        </el-form-item>
        <el-form-item label="超时(秒)">
          <el-input-number v-model="form.timeoutSeconds" :min="10" :max="7200" />
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
import { jobApi } from '@/api/bigdata.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, jobType: '', sourceId: '' })

const runsVisible = ref(false)
const runsData = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ id: null, jobName: '', jobCode: '', jobType: 'SCHEDULED', cronExpr: '', targetTable: '', sourceId: '', script: '', enabled: 1, retryTimes: 0, timeoutSeconds: 300 })
const formRules = {
  jobName: [{ required: true, message: '请输入任务名', trigger: 'blur' }],
  jobCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  cronExpr: [{ required: true, message: '请输入Cron表达式', trigger: 'blur' }],
  targetTable: [{ required: true, message: '请输入目标表', trigger: 'blur' }]
}

function jobTypeLabel(t) {
  return { CDC: 'CDC', SCHEDULED: '定时', MANUAL: '手动', STREAMING: '流式' }[t] || t
}

async function loadData() {
  loading.value = true
  try {
    const res = await jobApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {} finally {
    loading.value = false
  }
}

async function triggerJob(row) {
  await ElMessageBox.confirm(`确认手动触发任务 ${row.jobName}?`, '提示', { type: 'warning' })
  try {
    await jobApi.trigger(row.id)
    ElMessage.success('已触发')
    loadData()
  } catch (e) {}
}

async function viewRuns(row) {
  runsVisible.value = true
  try {
    const res = await jobApi.runs(row.id, { page: 1, size: 20 })
    runsData.value = res?.records || res?.list || res || []
  } catch (e) {
    runsData.value = []
  }
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { id: null, jobName: '', jobCode: '', jobType: 'SCHEDULED', cronExpr: '', targetTable: '', sourceId: '', script: '', enabled: 1, retryTimes: 0, timeoutSeconds: 300 })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await jobApi.update(form.id, form)
    } else {
      await jobApi.create(form)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {} finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除任务 ${row.jobName}?`, '提示', { type: 'warning' })
  try {
    await jobApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}

onMounted(loadData)
</script>
