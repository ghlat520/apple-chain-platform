<template>
  <div class="page-container">
    <div class="page-header">
      <h2>配送管理</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增配送</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索配送编码/收货人" clearable style="width:220px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:140px" @change="loadData">
        <el-option label="待配送" value="PENDING" />
        <el-option label="配送中" value="DELIVERING" />
        <el-option label="已签收" value="SIGNED" />
        <el-option label="已拒收" value="REJECTED" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="deliveryCode" label="配送编码" width="180" class-name="nums-tabular" />
      <el-table-column prop="taskId" label="任务ID" width="100" class-name="nums-tabular" />
      <el-table-column prop="receiverName" label="收货人" width="110" />
      <el-table-column prop="receiverAddr" label="收货地址" min-width="200" />
      <el-table-column prop="receiverPhone" label="联系电话" width="140" class-name="nums-tabular" />
      <el-table-column prop="deliveryTime" label="配送时间" width="180" />
      <el-table-column label="质量检查" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.qualityCheck" :type="qualityType(row.qualityCheck)" size="small">
            {{ qualityLabel(row.qualityCheck) }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="signTime" label="签收时间" width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button
              v-if="row.status === 'DELIVERING'"
              link type="success" size="small"
              @click="openSign(row)"
            >签收</el-button>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑配送' : '新增配送'" width="520px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="任务ID" prop="taskId">
          <el-input-number v-model="form.taskId" :min="1" style="width:100%" placeholder="关联运输任务ID" />
        </el-form-item>
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="form.receiverName" />
        </el-form-item>
        <el-form-item label="收货地址" prop="receiverAddr">
          <el-input v-model="form.receiverAddr" />
        </el-form-item>
        <el-form-item label="联系电话" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" />
        </el-form-item>
        <el-form-item label="配送时间">
          <el-date-picker v-model="form.deliveryTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
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

    <!-- Sign Dialog -->
    <el-dialog v-model="signVisible" title="确认签收" width="460px">
      <el-form ref="signFormRef" :model="signForm" :rules="signRules" label-width="110px">
        <el-form-item label="签收时间" prop="signTime">
          <el-date-picker v-model="signForm.signTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="质量检查" prop="qualityCheck">
          <el-select v-model="signForm.qualityCheck" style="width:100%">
            <el-option label="合格" value="PASSED" />
            <el-option label="不合格" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="质量备注">
          <el-input v-model="signForm.qualityRemark" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="signForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="signVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSign" :loading="submitting">确认签收</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { deliveryApi } from '@/api/coldchain.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', status: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  taskId: null,
  receiverName: '',
  receiverAddr: '',
  receiverPhone: '',
  deliveryTime: '',
  remark: '',
})
const formRules = {
  taskId: [{ required: true, message: '请输入任务ID', trigger: 'blur' }],
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  receiverAddr: [{ required: true, message: '请输入收货地址', trigger: 'blur' }],
  receiverPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
}

const signVisible = ref(false)
const signFormRef = ref()
const signForm = reactive({ id: null, signTime: '', qualityCheck: 'PASSED', qualityRemark: '', remark: '' })
const signRules = {
  signTime: [{ required: true, message: '请选择签收时间', trigger: 'change' }],
  qualityCheck: [{ required: true, message: '请选择质量检查结果', trigger: 'change' }],
}

function statusType(code) {
  const map = { PENDING: 'info', DELIVERING: 'primary', SIGNED: 'success', REJECTED: 'danger' }
  return map[code] || 'info'
}

function statusLabel(code) {
  const map = { PENDING: '待配送', DELIVERING: '配送中', SIGNED: '已签收', REJECTED: '已拒收' }
  return map[code] || code
}

function qualityType(code) {
  const map = { PENDING: 'info', PASSED: 'success', REJECTED: 'danger' }
  return map[code] || 'info'
}

function qualityLabel(code) {
  const map = { PENDING: '待检', PASSED: '合格', REJECTED: '不合格' }
  return map[code] || code
}

async function loadData() {
  loading.value = true
  try {
    const res = await deliveryApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { id: null, taskId: null, receiverName: '', receiverAddr: '', receiverPhone: '', deliveryTime: '', remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    taskId: row.taskId,
    receiverName: row.receiverName,
    receiverAddr: row.receiverAddr,
    receiverPhone: row.receiverPhone,
    deliveryTime: row.deliveryTime || '',
    remark: row.remark || '',
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await deliveryApi.update(form.id, form)
    } else {
      await deliveryApi.create(form)
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

function openSign(row) {
  signForm.id = row.id
  signForm.signTime = ''
  signForm.qualityCheck = 'PASSED'
  signForm.qualityRemark = ''
  signForm.remark = ''
  signVisible.value = true
}

async function handleSign() {
  const valid = await signFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await deliveryApi.sign(signForm.id, {
      signTime: signForm.signTime,
      qualityCheck: signForm.qualityCheck,
      qualityRemark: signForm.qualityRemark,
      remark: signForm.remark,
    })
    ElMessage.success('签收成功')
    signVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除配送单 ' + row.deliveryCode + '?', '提示', { type: 'warning' })
  try {
    await deliveryApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
