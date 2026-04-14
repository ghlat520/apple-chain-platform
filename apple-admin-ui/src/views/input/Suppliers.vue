<template>
  <div class="page-container">
    <div class="page-header">
      <h2>供应商管理</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增供应商</el-button>
    </div>
    <div class="filter-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索供应商名称/联系人"
        clearable
        style="width:220px"
        @clear="loadData"
        @keyup.enter="loadData"
      />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:140px" @change="loadData">
        <el-option label="待审核" value="PENDING" />
        <el-option label="已通过" value="APPROVED" />
        <el-option label="已拒绝" value="REJECTED" />
        <el-option label="已拉黑" value="BLACKLISTED" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="supplierCode" label="供应商编号" width="140" />
      <el-table-column prop="name" label="供应商名称" min-width="160" />
      <el-table-column prop="contactPerson" label="联系人" width="120" />
      <el-table-column prop="phone" label="联系电话" width="140" />
      <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="supplierStatusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="creditScore" label="信用分" width="90" class-name="nums-tabular" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button
              link
              type="warning"
              size="small"
              @click="openAudit(row)"
              v-if="row.status === 'PENDING' || row.status === 'REJECTED'"
            >审核</el-button>
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
    <el-drawer v-model="drawerVisible" title="供应商详情" size="520px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="供应商名称" :span="2">{{ currentRow.name }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentRow.contactPerson || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentRow.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ currentRow.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="supplierStatusType(currentRow.status)" size="small">
              {{ statusLabel(currentRow.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="信用分" class-name="nums-tabular">{{ currentRow.creditScore ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="营业执照" :span="2">{{ currentRow.license || '-' }}</el-descriptions-item>
          <el-descriptions-item label="资质证书" :span="2">{{ currentRow.qualification || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentRow.createTime || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑供应商' : '新增供应商'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="营业执照号">
          <el-input v-model="form.license" />
        </el-form-item>
        <el-form-item label="资质证书">
          <el-input v-model="form.qualification" />
        </el-form-item>
        <el-form-item label="信用分">
          <el-input-number v-model="form.creditScore" :min="1" :max="100" class="nums-tabular" style="width:100%" />
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

    <!-- Audit Dialog -->
    <el-dialog v-model="auditVisible" title="供应商审核" width="420px">
      <el-form ref="auditFormRef" :model="auditForm" :rules="auditRules" label-width="80px">
        <el-form-item label="审核结果" prop="decision">
          <el-radio-group v-model="auditForm.decision">
            <el-radio value="APPROVE">通过</el-radio>
            <el-radio value="REJECT">拒绝</el-radio>
            <el-radio value="BLACKLIST">拉黑</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="auditForm.reason" type="textarea" :rows="3" placeholder="请输入审核意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAudit" :loading="auditSubmitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { supplierApi } from '@/api/input.js'
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
  name: '',
  contactPerson: '',
  phone: '',
  address: '',
  license: '',
  qualification: '',
  creditScore: null,
  remark: ''
})
const formRules = {
  name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const auditVisible = ref(false)
const auditSubmitting = ref(false)
const auditFormRef = ref()
const auditTargetId = ref(null)
const auditForm = reactive({ decision: 'APPROVE', reason: '' })
const auditRules = {
  decision: [{ required: true, message: '请选择审核结果', trigger: 'change' }]
}

function supplierStatusType(status) {
  const map = { APPROVED: 'success', PENDING: 'warning', REJECTED: 'danger', BLACKLISTED: 'info' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { APPROVED: '已通过', PENDING: '待审核', REJECTED: '已拒绝', BLACKLISTED: '已拉黑' }
  return map[status] || status || '-'
}

async function loadData() {
  loading.value = true
  try {
    const res = await supplierApi.list(query)
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
  Object.assign(form, { id: null, name: '', contactPerson: '', phone: '', address: '', license: '', qualification: '', creditScore: null, remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    contactPerson: row.contactPerson,
    phone: row.phone,
    address: row.address || '',
    license: row.license || '',
    qualification: row.qualification || '',
    creditScore: row.creditScore ?? null,
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

function openAudit(row) {
  auditTargetId.value = row.id
  Object.assign(auditForm, { decision: 'APPROVE', reason: '' })
  auditVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await supplierApi.update(form.id, form)
    } else {
      await supplierApi.create(form)
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

async function handleAudit() {
  const valid = await auditFormRef.value.validate().catch(() => false)
  if (!valid) return
  auditSubmitting.value = true
  try {
    await supplierApi.audit(auditTargetId.value, auditForm)
    ElMessage.success('审核完成')
    auditVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    auditSubmitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除供应商 "${row.name}"?`, '提示', { type: 'warning' })
  try {
    await supplierApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
