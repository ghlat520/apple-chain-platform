<template>
  <div class="page-container">
    <div class="page-header">
      <h2>数据质量规则</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增规则</el-button>
    </div>
    <div class="filter-bar">
      <el-select v-model="query.ruleType" placeholder="规则类型" clearable style="width:140px" @change="loadData">
        <el-option label="NOT_NULL" value="NOT_NULL" />
        <el-option label="UNIQUE" value="UNIQUE" />
        <el-option label="RANGE" value="RANGE" />
        <el-option label="REGEX" value="REGEX" />
        <el-option label="ENUM" value="ENUM" />
        <el-option label="FRESHNESS" value="FRESHNESS" />
      </el-select>
      <el-input v-model="query.assetId" placeholder="资产ID" clearable style="width:140px" @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="ruleCode" label="规则编码" width="160" />
      <el-table-column prop="ruleName" label="规则名称" width="200" />
      <el-table-column prop="assetCode" label="关联资产" width="180" />
      <el-table-column prop="fieldName" label="字段" width="120" />
      <el-table-column prop="ruleType" label="类型" width="120" />
      <el-table-column prop="severity" label="严重程度" width="120">
        <template #default="{ row }">
          <el-tag :type="severityType(row.severity)" size="small">{{ row.severity }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastScore" label="最近得分" width="100" class-name="nums-tabular" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="success" size="small" @click="executeCheck(row)" :loading="row._checking">执行</el-button>
            <el-button link type="primary" size="small" @click="viewHistory(row)">历史</el-button>
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

    <!-- History Drawer -->
    <el-drawer v-model="historyVisible" title="检查历史" size="500px">
      <el-timeline v-if="historyData.length">
        <el-timeline-item
          v-for="item in historyData"
          :key="item.id"
          :timestamp="item.checkTime"
          placement="top"
        >
          <el-card shadow="never">
            <p>得分: <span class="nums-tabular" :style="{ color: item.score >= 80 ? '#43A047' : item.score >= 60 ? '#F57C00' : '#D32F2F' }">{{ item.score }}</span></p>
            <p>通过: <span class="nums-tabular">{{ item.pass === 1 ? '是' : '否' }}</span> &nbsp; 总行: <span class="nums-tabular">{{ item.totalRows }}</span> &nbsp; 异常行: <span class="nums-tabular">{{ item.badRows }}</span></p>
            <p v-if="item.sampleBadRows">样本: {{ item.sampleBadRows }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无检查记录" />
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑规则' : '新增规则'" width="550px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="规则编码" prop="ruleCode">
          <el-input v-model="form.ruleCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" />
        </el-form-item>
        <el-form-item label="资产编码" prop="assetCode">
          <el-input v-model="form.assetCode" />
        </el-form-item>
        <el-form-item label="字段名">
          <el-input v-model="form.fieldName" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="form.ruleType" style="width:100%">
            <el-option label="NOT_NULL" value="NOT_NULL" />
            <el-option label="UNIQUE" value="UNIQUE" />
            <el-option label="RANGE" value="RANGE" />
            <el-option label="REGEX" value="REGEX" />
            <el-option label="ENUM" value="ENUM" />
            <el-option label="FRESHNESS" value="FRESHNESS" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度" prop="severity">
          <el-select v-model="form.severity" style="width:100%">
            <el-option label="BLOCK" value="BLOCK" />
            <el-option label="ERROR" value="ERROR" />
            <el-option label="WARN" value="WARN" />
            <el-option label="INFO" value="INFO" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则表达式">
          <el-input v-model="form.ruleExpr" type="textarea" :rows="2" placeholder="如: range(0,100)" />
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
import { dqApi } from '@/api/bigdata.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, ruleType: '', assetId: '' })

const historyVisible = ref(false)
const historyData = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  ruleCode: '',
  ruleName: '',
  assetCode: '',
  fieldName: '',
  ruleType: 'NOT_NULL',
  severity: 'WARN',
  ruleExpr: ''
})
const formRules = {
  ruleCode: [{ required: true, message: '请输入规则编码', trigger: 'blur' }],
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  severity: [{ required: true, message: '请选择严重程度', trigger: 'change' }]
}

function severityType(s) {
  return { BLOCK: 'danger', ERROR: 'danger', WARN: 'warning', INFO: 'primary' }[s] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await dqApi.list(query)
    tableData.value = (res?.records || res?.list || []).map(r => ({ ...r, _checking: false }))
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function executeCheck(row) {
  row._checking = true
  try {
    await dqApi.check(row.id)
    ElMessage.success('检查已执行')
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    row._checking = false
  }
}

async function viewHistory(row) {
  historyVisible.value = true
  try {
    const res = await dqApi.results(row.id, { page: 1, size: 20 })
    historyData.value = res?.records || res?.list || res || []
  } catch (e) {
    historyData.value = []
  }
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    ruleCode: '',
    ruleName: '',
    assetCode: '',
    fieldName: '',
    ruleType: 'NOT_NULL',
    severity: 'WARN',
    ruleExpr: ''
  })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    ruleCode: row.ruleCode,
    ruleName: row.ruleName,
    assetCode: row.assetCode || '',
    fieldName: row.fieldName || '',
    ruleType: row.ruleType,
    severity: row.severity,
    ruleExpr: row.ruleExpr || ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await dqApi.update(form.id, form)
    } else {
      await dqApi.create(form)
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
  await ElMessageBox.confirm(`确认删除规则 ${row.ruleName}?`, '提示', { type: 'warning' })
  try {
    await dqApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
